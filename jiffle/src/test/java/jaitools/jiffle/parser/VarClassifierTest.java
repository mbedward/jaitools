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

import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test JiffleVarClassifier: the class that checks and categorizes
 * variables in a jiffle script
 * 
 * @author Michael Bedward
 */
public class VarClassifierTest extends ParserUtils {

    /**
     * Test check for using variables before assigning a value to them
     * @throws java.lang.Exception
     */
    @Test
    public void varUsedBeforeAssignment() throws Exception {
        System.out.println("   testUnassignedVars");
        String input = 
                "a = 3;\n" +
                "b = a + c;\n" +  // c used here before assignment
                "c = 2;\n" ;
                
        VarClassifier classifier = new VarClassifier(getAST(input));

        // set a dummy image var to avoid the classifier complaining
        Map<String, Jiffle.ImageRole> imageParams = CollectionFactory.map();
        imageParams.put("foo", Jiffle.ImageRole.DEST);
        classifier.setImageParams(imageParams);
        
        classifier.start();
        
        Map<String, ErrorCode> errors = classifier.getErrors();
        assertTrue(errors.containsKey("c"));
        
        ErrorCode error = errors.get("c");
        assertEquals(ErrorCode.UNINIT_VAR, error);
    }
 
}
