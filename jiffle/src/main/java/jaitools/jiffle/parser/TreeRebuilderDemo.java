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
import java.util.Collections;
import javax.media.jai.TiledImage;
import org.antlr.runtime.tree.CommonTree;

/**
 * @todo Turn this into one or more unit tests
 * 
 * @author Michael Bedward and Murray Ellis
 */
public class TreeRebuilderDemo extends DemoBase {

    public void testSimpleRebuild() throws Exception {
        System.out.println("testSimpleRebuild");
        
        String input = 
                "w = width();" +
                "h = height();" +
                "a = 1.0;" +
                "b = 2.0;" +
                "curx = x();" +
                "cury = y();" +
                "result = a * w + h + curx + cury;" ; // weird image :-)
        
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
        CommonTree tree = rebuilder.start().tree;
        System.out.println(tree.toStringTree());
    }
}
