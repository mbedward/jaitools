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
import org.antlr.runtime.Token;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for the options block parser.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class OptionsLexerTest {

    @Test
    public void gettingOptionsFromMixedInput() throws Exception {
        String script =
                  "fake header\n"
                + "options { \n"
                + "  format = jiffle; \n"
                + "  other=foo; \n"
                + "} \n"
                + "init { n = 0; } \n"
                + "dest = 42;\n"
                + "some line that makes no sense \n"
                + "foo = 0;" ;
                
 
        ANTLRStringStream input = new ANTLRStringStream(script);
        OptionsLexer lexer = new OptionsLexer(input);
        
        Token t = null;
        do {
            t = lexer.nextToken();
        } while (t.getType() != Token.EOF);
        
        Map<String, String> options = lexer.getOptions();
        assertEquals(2, options.size());
        assertTrue(options.containsKey("format") && options.get("format").equals("jiffle"));
        assertTrue(options.containsKey("other") && options.get("other").equals("foo"));
    }
}

