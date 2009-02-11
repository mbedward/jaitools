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
import java.awt.image.RenderedImage;
import java.util.Collections;
import java.util.Iterator;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

/**
 * 2todo Turn this into one or more unit tests
 * 
 * @author Michael Bedward and Murray Ellis
 */
public class ExpressionSimplifierDemo extends DemoBase {
    
    public static void main(String[] args) throws Exception {
        ExpressionSimplifierDemo instance = new ExpressionSimplifierDemo();
        instance.testSimplify();
    }

    public void testSimplify() throws Exception {
        System.out.println("testSimpleRebuild");
        
        String input = 
                "w = width();" +
                "h = height();" +
                "a = 1.0 - w;" +
                "b = 2.0 + h;" +
                "curx = x();" +
                "cury = y();" +
                "result = a + b + curx + cury;" ; // weird image :-)
        
        VarClassifier classifier = new VarClassifier(getAST(input));
        classifier.setImageVars(Collections.singleton("result"));
        classifier.setPrint(true);
        classifier.start();
        
        Metadata metadata = new Metadata(Collections.singletonMap("result", (RenderedImage)null));
        metadata.setVarData(classifier);
        
        System.out.println("metadata pos vars: " + metadata.getPositionalVars());
        
        TreeRebuilder rebuilder = new TreeRebuilder(getAST(input));
        rebuilder.setMetadata(metadata);
        rebuilder.setPrint(true);

        TreeRebuilder.start_return rebuilderRet = rebuilder.start();
        CommonTree tree = (CommonTree) rebuilderRet.getTree();
        System.out.println(tree.toStringTree());
        
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
        ExpressionSimplifier simplifier = new ExpressionSimplifier(nodes);
        ExpressionSimplifier.start_return simplifierRet = simplifier.start();
        
        CommonTree simplifiedTree = (CommonTree) simplifierRet.getTree();
        nodes = new CommonTreeNodeStream(simplifiedTree);
        Iterator iter = nodes.iterator();
        
        int i = 1;
        String prefix;
        while (iter.hasNext()) {
            Object o = iter.next();
            if (((CommonTree)o).getType() == ExpressionSimplifier.SIMPLE_EXPR) {
                prefix = "SIMPLE_EXPR:";
            } else {
                prefix = "";
            }
            
            System.out.println("" + (i++) + "  " + prefix + o.toString());
        }
    }
    
}
