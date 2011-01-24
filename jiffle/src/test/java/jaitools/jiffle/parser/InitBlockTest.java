/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.parser;

import org.junit.Test;

/**
 *
 * @author michael
 */
public class InitBlockTest extends TestingParser {
    
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
