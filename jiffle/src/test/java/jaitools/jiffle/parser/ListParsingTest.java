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

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import org.junit.Test;

/**
 * Tests for parsing list expressions.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class ListParsingTest extends ParserTestBase {

    @Test
    public void createEmptyList() throws Exception {
        System.out.println("   create empty list");
        
        String script = "foo = [];" ;
        CommonTreeNodeStream ast = getAST(script);
        
        int[] expected = {
            JiffleParser.EQ,
            Token.DOWN,
            JiffleParser.ID,
            JiffleParser.DECLARED_LIST,
            Token.DOWN,
            JiffleParser.EXPR_LIST,
            Token.UP,
            Token.UP
        };
        
        assertAST(ast, expected);
    }
    
    @Test
    public void createListWithElements() throws Exception {
        String script = " foo = [1, 2.0, bar, null];";
        
        CommonTreeNodeStream ast = getAST(script);

        int[] expected = {
            JiffleParser.EQ,
            Token.DOWN,
            JiffleParser.ID,
            JiffleParser.DECLARED_LIST,
            Token.DOWN,
            JiffleParser.EXPR_LIST,
            Token.DOWN,
            JiffleParser.INT_LITERAL,
            JiffleParser.FLOAT_LITERAL,
            JiffleParser.ID,
            JiffleParser.NULL,
            Token.UP,
            Token.UP,
            Token.UP
        };
        
        assertAST(ast, expected);
    }
    
    @Test
    public void appendWithOperator() throws Exception {
        String script = "foo = []; foo << 1; foo << bar;" ;

        CommonTreeNodeStream ast = getAST(script);
        
        int[] expected = {
            JiffleParser.EQ,
            Token.DOWN,
            JiffleParser.ID,
            JiffleParser.DECLARED_LIST,
            Token.DOWN,
            JiffleParser.EXPR_LIST,
            Token.UP,
            Token.UP,
            
            JiffleParser.APPEND,
            Token.DOWN,
            JiffleParser.ID,
            JiffleParser.INT_LITERAL,
            Token.UP,
            
            JiffleParser.APPEND,
            Token.DOWN,
            JiffleParser.ID,
            JiffleParser.ID,
            Token.UP
        };
        
        assertAST(ast, expected);
    }

}
