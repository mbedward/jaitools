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
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package jaitools.jiffle;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;

import jaitools.jiffle.parser.JiffleLexer;
import jaitools.jiffle.parser.JiffleParser;
import jaitools.jiffle.parser.JiffleWalker;
import org.antlr.runtime.tree.CommonTreeNodeStream;

/**
 * A simple, and probably temporary, class to demonstrate that we
 * can actually parse jiffle statements.
 * 
 * @author Michael Bedward
 */
public class JiffleDemo {

    CommonTokenStream tokens;
    CommonTree tree;

    public static void main(String[] args) throws Exception {
        String s = "sqrt(2);" +
                "log(10);" +
                "rand(10);" +
                "randInt(10);" +
                "asin(sin(1.234));" +
                "x = radToDeg(PI / 6);" +
                "x;" +
                "sin(degToRad(x));" +
                "log(E);" +
                "55 % 15 % 6.5;";

        JiffleDemo me = new JiffleDemo();

        try {
            me.lex(s);
            me.parse();
            me.walkTree();
            
        } catch (RecognitionException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void lex(String script) {
        ANTLRStringStream input = new ANTLRStringStream(script);
        JiffleLexer lexer = new JiffleLexer(input);
        tokens = new CommonTokenStream(lexer);
    }

    private void parse() throws RecognitionException {
        JiffleParser parser = new JiffleParser(tokens);
        parser.setPrint(true);  // print debug output
        
        JiffleParser.jiffle_return r = parser.jiffle();
        tree = (CommonTree) r.getTree();
    }

    private void walkTree() throws RecognitionException {
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
        nodes.setTokenStream(tokens);
        JiffleWalker walker = new JiffleWalker(nodes);
        walker.jiffle();
    }
}
