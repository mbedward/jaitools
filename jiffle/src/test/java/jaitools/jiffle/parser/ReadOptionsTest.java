/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.parser;

import java.util.Map;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class ReadOptionsTest {

    @Test
    public void noBlock() throws Exception {
        System.out.println("   script with no options block");
        String script =
                  "init { n = 0; } \n"
                + "dest = 42;" ;

        Map<String, String> options = parseOptions(script);
        assertEquals(0, options.size());
    }


    @Test
    public void simpleBlock() throws Exception {
        System.out.println("   script with simple options block");
        String script =
                  "options { format = jiffle; } \n"
                + "init { n = 0; } \n"
                + "dest = 42;" ;
        
        Map<String, String> options = parseOptions(script);
        assertEquals(1, options.size());
        assertEquals("jiffle", options.get("format"));
    }

    private Map<String, String> parseOptions(String script) throws Exception {
        ReadOptionsParser parser = new ReadOptionsParser( lexScript(script) );
        parser.start();
        return parser.getOptions();
    }

    private TokenStream lexScript(String script) {
        CharStream input = new ANTLRStringStream(script);
        ReadOptionsLexer lexer = new ReadOptionsLexer(input);
        TokenStream tokens = new CommonTokenStream(lexer);
        return tokens;
    }

}
