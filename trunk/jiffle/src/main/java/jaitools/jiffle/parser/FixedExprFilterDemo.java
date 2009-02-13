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

import jaitools.jiffle.interpreter.Metadata;
import jaitools.jiffle.interpreter.VarTable;
import java.util.Collections;
import javax.media.jai.TiledImage;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

/**
 * 2todo Turn this into one or more unit tests
 * 
 * @author Michael Bedward and Murray Ellis
 */
public class FixedExprFilterDemo extends DemoBase {
    
    public static void main(String[] args) throws Exception {
        FixedExprFilterDemo instance = new FixedExprFilterDemo();
        instance.demo();
    }

    public void demo() throws Exception {
        String input1 = 
                "a = 5; \n" +
                "result = a;\n" ;

        String input2 =
                "w = width();\n" +            // fixed expr form built-in func
                "fw = sqrt(w) + sin(1);\n" +  // fixed expr from general funcs
                "p = x() + fw;\n" +           // pos expr because of x()
                "c = p + w;\n" +              // pos expr because of p
                "result = c;\n" ;

        compile(input1);
        compile(input2);
    }

    public void compile(String input) throws Exception {
        System.out.println("Input program...");
        System.out.println(input);
        
        VarClassifier classifier = new VarClassifier(getAST(input));
        classifier.setImageVars(Collections.singleton("result"));
        classifier.setPrint(true);
        classifier.start();
        
        Metadata metadata = new Metadata(Collections.singletonMap("result", (TiledImage)null));
        metadata.setVarData(classifier);
        
        System.out.println("metadata pos vars: " + metadata.getPositionalVars());
        
        TreeRebuilder rebuilder = new TreeRebuilder(getAST(input));
        rebuilder.setMetadata(metadata);
        rebuilder.setPrint(true);

        TreeRebuilder.start_return rebuilderRet = rebuilder.start();
        CommonTree tree = (CommonTree) rebuilderRet.getTree();
        
        System.out.println("After rebuilder...");
        System.out.println(tree.toStringTree());
        
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
        ExpressionSimplifier simplifier = new ExpressionSimplifier(nodes);
        ExpressionSimplifier.start_return simplifierRet = simplifier.start();
        
        CommonTree simplifiedTree = (CommonTree) simplifierRet.getTree();
        System.out.println("After simplifier...");
        System.out.println(simplifiedTree.toStringTree());
        
        nodes = new CommonTreeNodeStream(simplifiedTree);
        Filter1 filter1 = new Filter1(nodes);
        Filter1.start_return f1Ret = filter1.start();
        
        CommonTree f1Tree = (CommonTree) f1Ret.getTree();
        System.out.println("After filter1...");
        System.out.println(f1Tree.toStringTree());

        nodes = new CommonTreeNodeStream(f1Tree);
        VarTable sharedVarTable = new VarTable();
        
        Filter2 filter2 = new Filter2(nodes);
        filter2.setVarTable(sharedVarTable);
        Filter2.start_return f2Ret = filter2.start();
        
        // sharedVarTable will now contain any simple vars that
        // can be replaced by constants
        
        CommonTree f2Tree = (CommonTree) f2Ret.getTree();
        System.out.println("After filter2...");
        System.out.println(f2Tree.toStringTree());
        
        nodes = new CommonTreeNodeStream(f2Tree);
        
        Filter3 filter3 = new Filter3(nodes);
        filter3.setVarTable(sharedVarTable);
        Filter3.start_return f3Ret = filter3.start();
        
        CommonTree f3Tree = (CommonTree) f3Ret.getTree();
        System.out.println("After filter3...");
        System.out.println(f3Tree.toStringTree());
        
        System.out.println("-------------------------------");
        
        /*

        Iterator iter = nodes.iterator();
        
        int depth = 0;
        String prefix;
        while (iter.hasNext()) {
            if (depth == 0) {
                //System.out.print("(");
            }
            Object o = iter.next();
            int type = ((CommonTree) o).getType();
            switch (type) {
                case ExpressionSimplifier.FIXED_EXPR:
                    prefix = "FIXED_EXPR:";
                    break;
                    
                case ExpressionSimplifier.POS_EXPR:
                    prefix = "POS_EXPR:";
                    break;
                    
                default:
                    prefix = "";
            }
            
            if (type == TreeParser.DOWN) {
                depth++ ;
            } else if (type == TreeParser.UP) {
                depth-- ;
                if (depth == 0) {
                    System.out.println(") ");
                }
            } else {
                System.out.print(prefix + o.toString() + " ");
            }
        }

        */
    }
    
}
