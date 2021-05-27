package com.hundun.mirai.plugin.function;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hundun.mirai.plugin.CustomBeanFactory;
import com.hundun.mirai.plugin.core.EventInfo;
import com.hundun.mirai.plugin.core.SessionId;
import com.hundun.mirai.plugin.parser.statement.QuickSearchStatement;
import com.hundun.mirai.plugin.parser.statement.Statement;
import com.hundun.mirai.plugin.service.BotService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author hundun
 * Created on 2021/05/18
 */
@Slf4j
public class QuickSearchFunction implements IFunction {

    
    class QuickSearchNode {
        List<String> conditions;
        String answerTemplate;
        boolean argUrlencolde;
        List<String> defaultArgs = new ArrayList<>(0);
    }
    
    List<QuickSearchNode> nodes = new ArrayList<>();
    
    BotService botService;
    @Override
    public void manualWired() {
        this.botService = CustomBeanFactory.getInstance().botService;
    }
    
    public QuickSearchFunction() {
        initNodes();
    }
    
    static final String ARG_PLACEHOLDER_START = "{ARG_";
    static final String ARG_PLACEHOLDER_END = "}";
    public void initNodes() {
        
        
        QuickSearchNode node;
        
        node = new QuickSearchNode();
        node.conditions = Arrays.asList("PRTS", "prts");
        node.answerTemplate = "http://prts.wiki/w/{ARG_0}";
        node.argUrlencolde = true;
        node.defaultArgs = Arrays.asList("首页");
        nodes.add(node);
        
        node = new QuickSearchNode();
        node.conditions = Arrays.asList("aog", "AOG","一图流");
        node.answerTemplate = "https://aog.wiki/";
        nodes.add(node);
        
        node = new QuickSearchNode();
        node.conditions = Arrays.asList("企鹅物流");
        node.answerTemplate = "https://penguin-stats.cn/";
        nodes.add(node);
        
        node = new QuickSearchNode();
        node.conditions = Arrays.asList("绿票一图流", "一图流绿票");
        node.answerTemplate = "https://hguandl.com/yituliu/yituliu.jsp";
        nodes.add(node);
        
        node = new QuickSearchNode();
        node.conditions = Arrays.asList("舰娘百科", "一图流绿票");
        node.answerTemplate = "https://hguandl.com/yituliu/yituliu.jsp";
        nodes.add(node);
        
        
    }
    
    
    
    
    
    
    @Override
    public boolean acceptStatement(SessionId sessionId, EventInfo event, Statement statement) {
        if (statement instanceof QuickSearchStatement) {
            QuickSearchStatement quickSearchStatement = (QuickSearchStatement)statement;
            for (QuickSearchNode node : nodes) {
                if (node.conditions.contains(quickSearchStatement.getMainArg())) {
                    String answer = node.answerTemplate;
                    
                    int placeholderStartIndex = answer.indexOf(ARG_PLACEHOLDER_START);
                    int placeholderEndIndex = answer.indexOf(ARG_PLACEHOLDER_END);
                    while (placeholderStartIndex > -1 && placeholderEndIndex > -1) {
                        String indexPart = node.answerTemplate.substring(placeholderStartIndex + ARG_PLACEHOLDER_START.length(), placeholderEndIndex);
                        String placeholderPart = ARG_PLACEHOLDER_START + indexPart + ARG_PLACEHOLDER_END;
                        int index = Integer.valueOf(indexPart);
                        String arg;
                        if (quickSearchStatement.getArgs().size() > index) {
                            arg = quickSearchStatement.getArgs().get(index);
                        } else if (node.defaultArgs.size() > index) {
                            arg = node.defaultArgs.get(index);
                        } else {
                            arg = "";
                        }
                        if (node.argUrlencolde) {
                            try {
                                arg = URLEncoder.encode(
                                        arg,
                                        java.nio.charset.StandardCharsets.UTF_8.toString()
                                      );
                            } catch (UnsupportedEncodingException e) {
                                log.warn("Urlencolde fail: {}", arg);
                            }
                        }
                        
                        answer = answer.replace(placeholderPart, arg);
                        
                        placeholderStartIndex = answer.indexOf(ARG_PLACEHOLDER_START);
                        placeholderEndIndex = answer.indexOf(ARG_PLACEHOLDER_END);
                    }
                    
                    
                    botService.sendToGroup(event.getGroupId(), answer);
                    return true;
                }
            }   
        }
        return false;
    }

    @Override
    public List<SubFunction> getSubFunctions() {
        return null;
    }

}
