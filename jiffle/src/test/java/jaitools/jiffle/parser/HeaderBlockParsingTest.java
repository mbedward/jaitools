/*
 * Copyright 2011 Michael Bedward
 *
 * This file is part of jai-tools.
 *
 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jaitools.jiffle.parser;

import org.junit.Test;

/**
 * Tests basic parsing of scripts with and without init and options blocks.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class HeaderBlockParsingTest extends ParserTestBase {
    
    @Test
    public void noInitBlock() throws Exception {
        System.out.println("   script without init block");
        String script = "dest = 42;";
        getAST(script);
    }

    @Test
    public void emptyInitBlock() throws Exception {
        System.out.println("   script with empty init block");
        String script = 
                  "init { } \n"
                + "dest = 42;";
        
        getAST(script);
    }

    @Test
    public void simpleInitBlock() throws Exception {
        System.out.println("   script with simple init block");
        String script = 
                  "init { foo = 42; } \n"
                + "dest = 42;";
        
        getAST(script);
    }
    
    @Test(expected=JiffleParserException.class)
    public void misplacedInitBlock() throws Exception {
        System.out.println("   init block in wrong part of script");
        String script = 
                  "dest = 42;"
                + "init { foo = 42; } \n";
        
        getAST(script);
    }

    @Test
    public void initBlockWithNewLinesAndWhitespace() throws Exception {
        System.out.println("   init block with newlines");
        String script =
                  "init { \n\n"
                + "    n1 = 0; \n\n"
                + "    n2 = 42; \n\n"
                + "} \n"
                + "dest = n2 - n1;";

        getAST(script);
    }

    @Test
    public void noOptionsBlock() throws Exception {
        System.out.println("   script without options block");
        String script = "dest = 42;";
        getAST(script);
    }

    @Test
    public void emptyOptionsBlock() throws Exception {
        System.out.println("   script with empty options block");
        String script =
                  "options { } \n"
                + "dest = 42;";

        getAST(script);
    }

    @Test
    public void simpleOptionsBlock() throws Exception {
        System.out.println("   script with simple options block");
        String script =
                  "options { format = jiffle; } \n"
                + "dest = 42;";

        getAST(script);
    }

    @Test(expected=JiffleParserException.class)
    public void misplacedOptionsBlock1() throws Exception {
        System.out.println("   options block not at start of script");
        String script =
                  "dest = 42;"
                + "options { format = jiffle; } \n";

        getAST(script);
    }

    @Test
    public void optionsBlockWithNewLinesAndWhitespace() throws Exception {
        System.out.println("   options block with newlines");
        String script =
                  "options { \n\n"
                + "    format = jiffle; \n\n"
                + "} \n"
                + "dest = 42;";

        getAST(script);
    }

    @Test(expected=JiffleParserException.class)
    public void misplacedOptionsBlock2() throws Exception {
        System.out.println("   options block after init block");
        String script =
                  "init { n = 0; }"
                + "options { format = jiffle; }"
                + "dest = 42;" ;

        getAST(script);
    }

    @Test
    public void optionsAndInitBlock() throws Exception {
        System.out.println("   options and init blocks in script");
        String script =
                  "options { format = jiffle; }"
                + "init { n = 0; }"
                + "dest = 42;" ;

        getAST(script);
    }

}
