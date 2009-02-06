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

package jaitools.jiffle;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;

import jaitools.jiffle.parser.JiffleLexer;
import jaitools.jiffle.parser.JiffleParser;
import jaitools.jiffle.parser.JiffleDirectEval;
import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.tree.CommonTreeNodeStream;

/**
 * A simple, and probably temporary, class to demonstrate that we
 * can actually evaluate simple jiffle statements.
 * 
 * Note that we are only demonstrating expressions that work at 
 * the moment :-)
 * 
 * @author Michael Bedward
 */
public class ExprEvalDemo {

    public static void main(String[] args) throws Exception {
        String[] stmts = {
            "2 * 3 + 16 / 4 - 2^3;",
            "55 % 15 % 6;",
            "sqrt(2);",
            "2^(1/2);",
            "sin(degToRad(30));",
            "sin(PI/6);"
        };
        
        ExprEvalDemo me = new ExprEvalDemo();
        
        List<String[]> output = new ArrayList<String[]>();
        int[] colWidth = new int[3];
        
        String[] header = {"Input stmt", "Parsed stmt", "Result"};
        for (int i = 0; i < header.length; i++) colWidth[i] = header[i].length();
        output.add(header);
        
        for (String stmt : stmts) {
            String[] out = me.eval(stmt);
            output.add(out);
            
            for (int i = 0; i < out.length; i++) {
                if (out[i].length() > colWidth[i]) {
                    colWidth[i] = out[i].length();
                }
            }
        }
        
        String[] fmts = new String[colWidth.length];
        for (int i = 0; i < colWidth.length; i++) {
            fmts[i] = " %" + colWidth[i] + "s |";
        }

        for (String[] out : output) {
            for (int i = 0; i < out.length; i++) {
                System.out.printf(fmts[i], out[i]);
            }
            System.out.println();
        }
    }
    
    private String[] eval(String input) {
        CommonTree tree = null;
        double result = 0;
        try {
            CommonTokenStream tokens = lex(input);
            tree = parse(tokens);
            result = walkTree(tree, tokens);
            
        } catch (RecognitionException ex) {
            System.out.println(ex.getMessage());
        }
        
        return new String[] {input, tree.toStringTree(), String.format("%.4f", result)};
    }

    private CommonTokenStream lex(String script) {
        ANTLRStringStream input = new ANTLRStringStream(script);
        JiffleLexer lexer = new JiffleLexer(input);
        return new CommonTokenStream(lexer);
    }

    private CommonTree parse(CommonTokenStream tokens) throws RecognitionException {
        JiffleParser parser = new JiffleParser(tokens);
        //parser.setPrint(true);  // print debug output
        JiffleParser.prog_return r = parser.prog();
        return (CommonTree) r.getTree();
    }

    private double walkTree(CommonTree tree, CommonTokenStream tokens) throws RecognitionException {
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
        nodes.setTokenStream(tokens);
        JiffleDirectEval eval = new JiffleDirectEval(nodes);
        //walker.setPrint(true);  // print debug output
        eval.prog();
        return eval.getResult();
    }
}
