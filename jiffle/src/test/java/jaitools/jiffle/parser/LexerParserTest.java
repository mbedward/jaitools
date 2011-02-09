/*
 * Copyright 2009 Michael Bedward
 * 
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public 
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package jaitools.jiffle.parser;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import static org.junit.Assert.*;
import org.junit.Test;


/**
 * Some simple tests for JiffleLexer, directed towards inputs that have
 * given problems such as block comments, blank lines, end-of-statement
 * detection.
 * 
 * @author Michael Bedward
 */
public class LexerParserTest {

    /**
     * Test tokenizing a block comment with embedded newlines
     * followed by a newline and an INT literal
     */
    @Test
    public void testBlockComment() {
        System.out.println("   block comment");
        
        String input = 
                "/* a block comment \n " +
                " * with newlines \n " +
                " * embedded followed by \n" +
                " * an INT_LITERAL */" +
                "42; \n";

        JiffleLexer lexer = lex(input);

        Token tok = null;
        tok = lexer.nextToken();
        assertTrue(tok.getChannel() == Token.HIDDEN_CHANNEL);
        assertTrue(tok.getType() == JiffleLexer.COMMENT);

        do {
            tok = lexer.nextToken();
        } while (tok.getChannel() == Token.HIDDEN_CHANNEL);
        
        assertTrue(tok.getType() == JiffleLexer.INT_LITERAL);
    }
    
    @Test
    public void testDotNames() {
        System.out.println("   dots in var names");
        
        String input = "my.var=5.0";
        JiffleLexer lexer = lex(input);
        Token tok;

        tok = lexer.nextToken();
        assertTrue(tok.getType() == JiffleLexer.ID);

        tok = lexer.nextToken();
        assertTrue(tok.getType() == JiffleLexer.EQ);

        tok = lexer.nextToken();
        assertTrue(tok.getType() == JiffleLexer.FLOAT_LITERAL);
    }
    
    @Test
    public void testNullKeywordAndFunction() throws Exception {
        System.out.println("   recognizing null keyword vs null() function");
        
        String input = "a=null; b=null();";
        JiffleLexer lexer = lex(input);
        JiffleTokenStream tokStrm = new JiffleTokenStream(lexer);
        JiffleParser parser = new JiffleParser(tokStrm);

        CommonTree ast = (CommonTree) parser.prog().getTree();
        
        int[] expType = {
            JiffleParser.ASSIGN,
            Token.DOWN,
            JiffleParser.EQ,
            JiffleParser.ID,
            JiffleParser.NULL,
            Token.UP,
            JiffleParser.ASSIGN,
            Token.DOWN,
            JiffleParser.EQ,
            JiffleParser.ID,
            JiffleParser.FUNC_CALL,
            Token.DOWN,
            JiffleParser.ID
        };
        
        CommonTreeNodeStream nodeStrm = new CommonTreeNodeStream(ast);
        int k = 0;
        while (k < expType.length) {
            CommonTree node = (CommonTree) nodeStrm.nextElement();
            assertEquals(node.getType(), expType[k++]);
        }

        assertTrue(k == expType.length);
    }
    
    /**
     * Helper method: creates a lexer for the test methods
     */
    private JiffleLexer lex(String input) {
        ANTLRStringStream strm = new ANTLRStringStream(input);
        JiffleLexer lexer = new JiffleLexer(strm);
        return lexer;
    }
}
