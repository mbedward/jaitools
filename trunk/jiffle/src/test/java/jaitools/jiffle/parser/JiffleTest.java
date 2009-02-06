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
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Some basic unit tests of expression evaluation
 * @author Michael Bedward
 */
public class JiffleTest {
    
    private static final double TOL = 1.0e-8d;
    
    @Test
    public void TestArithmetic() throws Exception {
        System.out.println("Test arithmetic");
        String input;
        
        System.out.println("   addition");
        input = "1234 + 4321;";
        double result = eval(input);
        assertTrue(deq(result, 5555));
        
        System.out.println("   subtraction");
        input = "5555 - 1234;";
        result = eval(input);
        assertTrue(deq(result, 4321));
        
        System.out.println("   multiplication");
        input = "2.5 * 2.5;";
        result = eval(input);
        assertTrue(deq(result, 6.25));

        System.out.println("   division");
        input = "100 / 12.5;";
        result = eval(input);
        assertTrue(deq(result, 8));
        
        System.out.println("   modulus");
        input = "23 % 8;";
        result = eval(input);
        assertTrue(deq(result, 7));
        
        System.out.println("   exponentiation");
        input = "0.5^3;";
        result = eval(input);
        assertTrue(deq(result, 1.0/8.0));
    }
    
    @Test
    public void testPrecedence() throws Exception {
        System.out.println("Testing arithmetic precedence");
        String input = "1 + 2*3^2 / 3;";
        double result = eval(input);
        assertTrue(deq(result, 7));
    }
    
    @Test
    public void testConditional() throws Exception {
        System.out.println("Testing conditional expression");
        
        System.out.println("   when true");
        String input = "5/3 < 2 ? 42 : 0;";
        double result = eval(input);
        assertTrue(deq(result, 42));

        System.out.println("   when false");
        input = "5/3 > 2 ? 0 : 42;";
        result = eval(input);
        assertTrue(deq(result, 42));
    }

    private double eval(String input) throws RecognitionException {
        ANTLRStringStream strm = new ANTLRStringStream(input);
        JiffleLexer lexer = new JiffleLexer(strm);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        JiffleParser parser = new JiffleParser(tokens);
        JiffleParser.prog_return r = parser.prog();
        CommonTree tree = (CommonTree) r.getTree();

        CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
        nodes.setTokenStream(tokens);
        JiffleWalker walker = new JiffleWalker(nodes);
        walker.prog();
        
        return walker.getResult();
    }
    
    private boolean deq(double d1, double d2) {
        return Math.abs(d1 - d2) < TOL;
    }
    
}
