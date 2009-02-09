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
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test JiffleVarClassifier: the class that checks and categorizes
 * variables in a jiffle script
 * 
 * @author Michael Bedward
 */
public class VarClassifierTest extends TreeWalkerTestBase {

    /**
     * Test identification of positional variables
     * @throws java.lang.Exception
     */
    @Test
    public void testClassification() throws Exception {
        System.out.println("   testClassification");
        String input =
                "x = x();" +
                "y = 2.0;" +
                "pos1 = 1 + x;" +
                "pos2 = y + x;" +
                "yadd1 = 1 + y;" +
                "pos3 = gen1 + pos1;" ;

        VarClassifier classifier = new VarClassifier(getAST(input));
        // classifier.setPrint(true);

        classifier.start();
        
        Set<String> posVars = classifier.getPositionalVars();
        assertFalse(posVars.contains("y"));
        assertFalse(posVars.contains("yadd1"));
        assertTrue(posVars.containsAll(Arrays.asList(new String[]{"x", "pos1", "pos2", "pos3"})));
    }
    
    /**
     * Test check for using variables before assigning a value to them
     * @throws java.lang.Exception
     */
    @Test
    public void testUnassignedVars() throws Exception {
        System.out.println("   testUnassignedVars");
        String input = 
                "a = 3;\n" +
                "b = a + c;\n" +  // c used before assignment
                "c = 2;\n" ;
                
        VarClassifier classifier = new VarClassifier(getAST(input));
        // classifier.setPrint(true);

        classifier.start();
        
        Set<String> vars = classifier.getUnassignedVars();
        assertFalse(vars.contains("a"));
        assertFalse(vars.contains("b"));
        assertTrue(vars.contains("c"));
    }
    
}
