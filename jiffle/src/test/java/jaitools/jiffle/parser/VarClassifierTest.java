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

import java.util.Arrays;
import java.util.Set;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test JiffleVarClassifier: the class that categorizes variables as
 * positional or general for the interpreter
 * 
 * @author Michael Bedward
 */
public class VarClassifierTest {

    @Test
    public void testClassification() throws Exception {
        String input =
                "x = x();" +
                "y = 2.0;" +
                "pos1 = 1 + x;" +
                "pos2 = y + x;" +
                "yadd1 = 1 + y;" +
                "pos3 = gen1 + pos1;" ;

        Set<String> posVars = classify(input);
        assertFalse(posVars.contains("y"));
        assertFalse(posVars.contains("yadd1"));
        assertTrue(posVars.containsAll(Arrays.asList(new String[]{"x", "pos1", "pos2", "pos3"})));
        
        assertTrue(true);
    }
    
    private Set<String> classify(String input) throws Exception {
        ANTLRStringStream strm = new ANTLRStringStream(input);
        JiffleLexer lexer = new JiffleLexer(strm);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        JiffleParser parser = new JiffleParser(tokens);
        JiffleParser.prog_return r = parser.prog();
        CommonTree tree = (CommonTree) r.getTree();

        CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
        nodes.setTokenStream(tokens);
        JiffleVarClassifier classifier = new JiffleVarClassifier(nodes);
        // classifier.setPrint(true);
        classifier.start();
        return classifier.getPositionalVars();
    }
}
