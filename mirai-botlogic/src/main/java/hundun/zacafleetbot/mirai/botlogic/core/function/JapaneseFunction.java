package hundun.zacafleetbot.mirai.botlogic.core.function;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

import hundun.zacafleetbot.mirai.botlogic.core.behaviourtree.BlackBoard;
import hundun.zacafleetbot.mirai.botlogic.core.behaviourtree.ProcessResult;
import hundun.zacafleetbot.mirai.botlogic.core.data.EventInfo;
import hundun.zacafleetbot.mirai.botlogic.core.data.SessionId;
import hundun.zacafleetbot.mirai.botlogic.core.parser.statement.Statement;
import hundun.zacafleetbot.mirai.botlogic.core.parser.statement.SubFunctionCallStatement;

/**
 * @author hundun
 * Created on 2021/05/25
 */
@Component
public class JapaneseFunction extends BaseFunction {

    static Tokenizer tokenizer = new Tokenizer();

        

    @Override
    public ProcessResult process(BlackBoard blackBoard) {
        SessionId sessionId = blackBoard.getSessionId(); 
        EventInfo event = blackBoard.getEvent(); 
        Statement statement = blackBoard.getStatement();
        if (statement instanceof SubFunctionCallStatement) {
            SubFunctionCallStatement subFunctionCallStatement = (SubFunctionCallStatement)statement;
            if (subFunctionCallStatement.getSubFunction() == SubFunction.JAPANESE_TOOL) {
                StringBuilder allArg = new StringBuilder();
                subFunctionCallStatement.getArgs().forEach(item -> allArg.append(item).append(" "));
                String result = funAllLines(allArg.toString(), "\n");
                console.sendToGroup(event.getBot(), event.getGroupId(), result);
                return new ProcessResult(this, true);
            }
            
        }
        return new ProcessResult(this, false);
    }

    @Override
    public List<SubFunction> getSubFunctions() {
        return Arrays.asList(SubFunction.JAPANESE_TOOL);
    }
    
    
    
    public static String funAllLines(String text, String splict) {
        StringBuilder result = new StringBuilder();
        String[] lines = text.split(splict);
        for (String line : lines) {
            result.append(funOneLine(line));
            result.append("\n");
        }
        return result.toString();
    }
    
    public static String funOneLine(String line) {
        StringBuilder result = new StringBuilder();
        List<Token> tokens = tokenizer.tokenize(line);
        for (Token token : tokens) {
            result.append(token.getSurface());
            if (!isAllKana(token.getSurface()) && !token.getReading().equals("*") && !token.getReading().equals(token.getSurface())) {
                result.append("(").append(token.getPronunciation()).append(")");
            }
        }
        return result.toString();
    }
    
    public static void funLines(String text, String split) {
        StringBuilder result = new StringBuilder();
        
        String[] lines = text.split(split);
        for (String line : lines) {
            
            result.append("\n");
        }
        System.out.println(result.toString());
    }
    
    /**
     * 平假名（3040 - 309f）
     * 片假名（30a0 - 30ff）
     */
    //static Pattern kanaPattern = Pattern.compile("^[\u3040-\u309f\u30a0-\u30ff]+&");
    
    static Pattern kanaPattern = Pattern.compile("^["
            + "ぁあぃいぅうぇえぉおかがきぎくぐけげこごさざしじすずせぜそぞただちぢっつづてでとどなにぬねのはばぱひびぴふぶぷへべぺほぼぽまみむめもゃやゅゆょよらりるれろゎわゐゑをんゔゕゖ゙゚゛゜ゝゞゟ"
            + "゠ァアィイゥウェエォオカガキギクグケゲコゴサザシジスズセゼソゾタダチヂッツヅテデトドナニヌネノハバパヒビピフブプヘベペホボポマミムメモャヤュユョヨラリルレロヮワヰヱヲンヴヵヶヷヸヹヺ・ーヽヾヿ"
            + "]+$");
    
    
    public static boolean isAllKana(String text) {
        Matcher matcher = kanaPattern.matcher(text);
        return matcher.matches();
    }

}
