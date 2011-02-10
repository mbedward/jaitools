/*
 * Copyright 2009-2011 Michael Bedward
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

import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.CharStream;

import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for the options block parser.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
@Ignore
public class ReadOptionsTest {

    @Test
    public void noBlock() throws Exception {
        System.out.println("   no options block");
        String script =
                  "init { n = 0; } \n"
                + "dest = 42;" ;

        Map<String, String> options = parseOptions(script);
        assertEquals(0, options.size());
    }

    @Test
    public void emptyBlock() throws Exception {
        System.out.println("   empty options block");
        String script =
                  "options {} \n"
                + "init { n = 0; } \n"
                + "dest = 42;" ;
    }

    @Test
    public void simpleBlock() throws Exception {
        System.out.println("   simple options block");
        String script =
                  "options { format = jiffle; } \n"
                + "init { n = 0; } \n"
                + "dest = 42;" ;
        
        Map<String, String> options = parseOptions(script);
        assertEquals(1, options.size());
        assertEquals("jiffle", options.get("format"));
    }

    @Test
    public void blockWithNewlines() throws Exception {
        System.out.println("   options block with embedded newlines");
        String script =
                  "options { \n"
                + "    format \n"
                + "    = \n"
                + "    jiffle \n"
                + "    ; \n"
                + "} \n"
                + "init { n = 0; } \n"
                + "dest = 42;" ;

        Map<String, String> options = parseOptions(script);
        assertEquals(1, options.size());
        assertEquals("jiffle", options.get("format"));
    }
    
    @Test
    public void optionsBlockInMapcalcFormatScript() throws Exception {
        System.out.println("   options block in mapcalc format script");
        String script =
                  "options { \n"
                + "    format = mapcalc; \n"
                + "} \n"
                + "foo = 0"
                + "bar = foo + 1"
                + "dest = bar" ;

        Map<String, String> options = parseOptions(script);
        assertEquals(1, options.size());
        assertEquals("mapcalc", options.get("format"));
    }

    private Map<String, String> parseOptions(String script) throws Exception {
        /*
        ReadOptionsParser parser = new ReadOptionsParser( lexScript(script) );
        parser.start();
        return parser.getOptions();
         * 
         */
        return null;
    }

    private CommonTokenStream lexScript(String script) {
        /*
        CharStream input = new ANTLRStringStream(script);
        ReadOptionsLexer lexer = new ReadOptionsLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return tokens;
         * 
         */
        return null;
    }

}
