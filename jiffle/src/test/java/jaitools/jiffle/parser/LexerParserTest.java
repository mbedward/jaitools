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
import org.junit.Test;
import static org.junit.Assert.*;

public class LexerParserTest {

    @Test
    public void testComments() {
        System.out.println("testComments");
        
        String input = 
                "/* a block comment \n " +
                " * with newlines \n " +
                " * embedded followed by \n" +
                " * an INT_LITERAL */ \n" +
                "42 \n";

        JiffleLexer lexer = lex(input);

        Token tok;
        
        System.out.println("   block comment");
        tok = lexer.nextToken();
        assertTrue(tok.getChannel() == Token.HIDDEN_CHANNEL);
        assertTrue(tok.getType() == JiffleLexer.BLOCK_COMMENT);

        do {
            tok = lexer.nextToken();
        } while (tok.getChannel() == Token.HIDDEN_CHANNEL);
        
        System.out.println("   INT literal");
        assertTrue(tok.getType() == JiffleLexer.INT_LITERAL);
    }
    
    private JiffleLexer lex(String input) {
        ANTLRStringStream strm = new ANTLRStringStream(input);
        JiffleLexer lexer = new JiffleLexer(strm);
        return lexer;
    }
}
