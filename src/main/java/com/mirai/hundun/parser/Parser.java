package com.mirai.hundun.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mirai.hundun.parser.statement.AtStatement;
import com.mirai.hundun.parser.statement.FunctionCallStatement;
import com.mirai.hundun.parser.statement.LiteralValueStatement;
import com.mirai.hundun.parser.statement.Statement;
import com.mirai.hundun.parser.statement.SyntaxsErrorStatement;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.PlainText;



/**
 * @author hundun
 * Created on 2021/04/27
 */
@Slf4j
public class Parser {

    public Tokenizer tokenizer = new Tokenizer();
    
    public SyntaxsTree syntaxsTree = new SyntaxsTree();
    
    public Parser() {
        
    }
    

    
    public Statement simpleParse(MessageChain messageChain) {
        
        
        List<Token> tokens = new ArrayList<>();
        for (Message message : messageChain) {
            List<Token> newTokens = tokenizer.simpleTokenize(message);
            tokens.addAll(newTokens);
        }
        
        StatementType type = syntaxsTree.root.accept(new ArrayList<>(tokens));
        if (type == null) {
            type = StatementType.SYNTAX_ERROR;
        }
        Statement statement;
        switch (type) {
            case AT:
                statement = new AtStatement(tokens);
                break;
            case FUNCTION_CALL:
                statement = new FunctionCallStatement(tokens);
                break;
            case SYNTAX_ERROR:
            default:
                type = StatementType.PLAIN_TEXT;
                statement = new LiteralValueStatement(tokens);
                break;
        }
        
        
        statement.setType(type);
        statement.setTokens(tokens);
        return statement;
    }
    
    public class SyntaxsTree {
        DFANode root = new DFANode();
        
        public void registerSyntaxs(List<List<TokenType>> grammars, StatementType type) {
            for (List<TokenType> grammar : grammars) {
                registerSyntax(grammar, type);
            }
        }
        
        public void registerSyntax(List<TokenType> grammar, StatementType type) {
            DFANode nowNode = root;

            for(int i = 0; i < grammar.size(); i++) {
                TokenType word = grammar.get(i);
                
                DFANode nextNode = nowNode.getChildNode(word);
                
                if (nextNode == null) {
                    nextNode = new DFANode();
                    nowNode.put(word, nextNode);
                }
                nowNode = nextNode;
                
                if (i == grammar.size() - 1) {
                    nowNode.endType = type;
                }
            }

            
        }
    }
    
    class DFANode {
        
        
        private Map<TokenType, DFANode> children;
        StatementType endType;
        
        public DFANode() {
            this.children = new HashMap<>();
            this.endType = null;
        }

        public DFANode getChildNode(TokenType input) {
            return children.get(input);
        }

        public void put(TokenType input, DFANode node) {
            if (children.containsKey(input)) {
                log.error("DFA node {} 已存在");
            }
            children.put(input, node);
        }
        
        public StatementType accept(List<Token> tokens) {
            if (tokens == null) {
                return null;
            }
            if (tokens.size() > 0) {
                Token top = tokens.remove(0);
                DFANode nextNode = getChildNode(top.type);
                if (nextNode == null) {
                    return StatementType.SYNTAX_ERROR;
                }
                return nextNode.accept(tokens);
            } else {
                return endType;
            }
        }

        public int size() {
            return children.size();
        }
        
        public Set<TokenType> getKeySet(){ 
            Set<TokenType> set;
            set = children.keySet();
            return set;
        }
        
        @Override
        public String toString() {
            return children.toString();
        }
    }
    
    
}
