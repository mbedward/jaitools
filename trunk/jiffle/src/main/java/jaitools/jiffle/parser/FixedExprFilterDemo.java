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
        
        Morph1 m1 = new Morph1(getAST(input));
        m1.setMetadata(metadata);
        m1.setPrint(true);

        Morph1.start_return m1Ret = m1.start();
        CommonTree tree = (CommonTree) m1Ret.getTree();
        
        System.out.println("After tree morph 1...");
        System.out.println(tree.toStringTree());
        
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
        Morph2 m2 = new Morph2(nodes);
        Morph2.start_return m2Ret = m2.start();
        
        CommonTree m2Tree = (CommonTree) m2Ret.getTree();
        System.out.println("After tree morph 2...");
        System.out.println(m2Tree.toStringTree());
        
        nodes = new CommonTreeNodeStream(m2Tree);
        Morph3 m3 = new Morph3(nodes);
        Morph3.start_return m3Ret = m3.start();
        
        CommonTree m3Tree = (CommonTree) m3Ret.getTree();
        System.out.println("After tree morph 3...");
        System.out.println(m3Tree.toStringTree());

        nodes = new CommonTreeNodeStream(m3Tree);
        VarTable sharedVarTable = new VarTable();
        
        Morph4 m4 = new Morph4(nodes);
        Morph4.start_return m4Ret = m4.start();
        
        // sharedVarTable will now contain any simple vars that
        // can be replaced by constants
        
        CommonTree m4Tree = (CommonTree) m4Ret.getTree();
        System.out.println("After tree morph 4...");
        System.out.println(m4Tree.toStringTree());
        
        nodes = new CommonTreeNodeStream(m4Tree);
        
        Morph5 m5 = new Morph5(nodes);
        m5.setVarTable(sharedVarTable);
        Morph5.start_return m5Ret = m5.start();
        
        CommonTree m5Tree = (CommonTree) m5Ret.getTree();
        System.out.println("After tree morph 5...");
        System.out.println(m5Tree.toStringTree());
        
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
                case Morph2.FIXED_EXPR:
                    prefix = "FIXED_EXPR:";
                    break;
                    
                case Morph2.POS_EXPR:
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
