/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.parser;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests basic parsing of scripts with and without an init block.
 * For unit tests of semantic error checking of init block contents 
 * see {@link VarClassifierTest}.
 * 
 * @author michael
 */
@Ignore(value="while working on init block processing")
public class InitBlockParsingTest extends ParserTestBase {
    
    @Test
    public void noBlock() throws Exception {
        System.out.println("   script without init block");
        String script = "dest = 42;";
        getAST(script);
    }

    @Test
    public void emptyBlock() throws Exception {
        System.out.println("   script with empty init block");
        String script = 
                  "init { } \n"
                + "dest = 42;";
        
        getAST(script);
    }

    @Test
    public void simpleBlock() throws Exception {
        System.out.println("   script with simple init block");
        String script = 
                  "init { foo = 42; } \n"
                + "dest = 42;";
        
        getAST(script);
    }
    
    @Test(expected=JiffleParserException.class)
    public void blockInWrongPlace() throws Exception {
        System.out.println("   block in wrong part of script");
        String script = 
                  "dest = 42;"
                + "init { foo = 42; } \n";
        
        getAST(script);
    }
    
}
