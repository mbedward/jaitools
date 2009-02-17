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
public class CompileDemo extends DemoBase {
    
    public static void main(String[] args) throws Exception {
        CompileDemo instance = new CompileDemo();
        instance.demo();
    }

    public void demo() throws Exception {
        String[] inputs = {
            "a = b = 42;\n" +
            "c = a + x();\n" +
            "result = c - b;\n",
            
            "a = 5; \n" +
            "result = a;\n",
            
            "result = abs(cos(PI) + sin(sqrt(PI)));\n",
            
            "result = x() + y();\n",
            
            "w = width();\n" +
            "fw = sqrt(w) + sin(1);\n" +
            "p = x() + fw;\n" +
            "c = p + w;\n" +
            "result = c;\n"
        };

        for (String input : inputs) {
            compile(input);
        }
    }

    public void compile(String input) throws Exception {
        System.out.println("\n\nInput program...");
        System.out.println(input);
        
        VarClassifier classifier = new VarClassifier(getAST(input));
        classifier.setImageVars(Collections.singleton("result"));
        //classifier.setPrint(true);
        classifier.start();
        
        Metadata metadata = new Metadata(Collections.singletonMap("result", (TiledImage)null));
        metadata.setVarData(classifier);
        
        System.out.println("metadata pos vars: " + metadata.getPositionalVars());
        System.out.println("metadata local vars: " + metadata.getLocalVars());
        
        Morph1 m1 = new Morph1(getAST(input));
        m1.setMetadata(metadata);
        Morph1.start_return m1Ret = m1.start();
        CommonTree tree = (CommonTree) m1Ret.getTree();
        
        System.out.println("After tree morph 1...");
        System.out.println(tree.toStringTree());
        
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
        nodes = new CommonTreeNodeStream(tree);
        
        Morph4 m4 = new Morph4(nodes);
        Morph4.start_return m4Ret = m4.start();
        tree = (CommonTree) m4Ret.getTree();
        
        // sharedVarTable will now contain any simple vars that
        // can be replaced by constants

        System.out.println("After tree morph 4...");
        System.out.println(tree.toStringTree());
        
        nodes = new CommonTreeNodeStream(tree);
        Morph5 m5 = new Morph5(nodes);
        VarTable sharedVarTable = new VarTable();
        m5.setVarTable(sharedVarTable);
        Morph5.start_return m5Ret = m5.start();
        
        tree = (CommonTree) m5Ret.getTree();
        System.out.println("After tree morph 5...");
        System.out.println(tree.toStringTree());
        
        nodes = new CommonTreeNodeStream(tree);
        Morph6 m6 = new Morph6(nodes);
        m6.setVarTable(sharedVarTable);
        Morph6.start_return m6Ret = m6.start();
        
        tree = (CommonTree) m6Ret.getTree();
        System.out.println("After tree morph 6...");
        System.out.println(tree.toStringTree());

        nodes = new CommonTreeNodeStream(tree);
        MakeRuntime rt = new MakeRuntime(nodes);
        MakeRuntime.start_return rtRet = rt.start();
        
        tree = (CommonTree) rtRet.getTree();
        System.out.println("After MakeRuntime...");
        System.out.println(tree.toStringTree());
        /*
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
