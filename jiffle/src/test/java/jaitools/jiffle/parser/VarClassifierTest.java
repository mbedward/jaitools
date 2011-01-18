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

import java.util.Map;

import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for VarClassifier: one of the ANTLR-generated classes used
 * by the Jiffle compiler to check use of variables in a script.
 * 
 * @author Michael Bedward
 */
public class VarClassifierTest extends TestingParser {
    
    private Map<String, Jiffle.ImageRole> imageParams;
    
    @Before
    public void setup() {
        imageParams = CollectionFactory.map();
    }

    @Test
    public void varUsedBeforeAssignment() throws Exception {
        System.out.println("   using a var before assignment");

        String script = 
                "foo = bar + 1; \n" +
                "bar = 42; \n";
                
        Map<String, ErrorCode> errors = runClassifier(script);
        
        assertTrue(errors.containsKey("bar"));
        assertEquals(ErrorCode.UNINIT_VAR, errors.get("bar"));
    }
    
    @Test
    public void assignmentToSourceImage() throws Exception {
        System.out.println("   writing to a source image");
        
        String script = "foo = 42; \n";
        imageParams.put("foo", Jiffle.ImageRole.SOURCE);
        Map<String, ErrorCode> errors = runClassifier(script);
        
        assertTrue(errors.containsKey("foo"));
        assertEquals(ErrorCode.ASSIGNMENT_TO_SRC_IMAGE, errors.get("foo"));
    }
    
    @Test
    public void readingFromDestinationImage() throws Exception {
        System.out.println("   reading from a destination image");
        
        String script = "x = foo; \n";
        imageParams.put("foo", Jiffle.ImageRole.DEST);
        Map<String, ErrorCode> errors = runClassifier(script);
        
        assertTrue(errors.containsKey("foo"));
        assertEquals(ErrorCode.READING_FROM_DEST_IMAGE, errors.get("foo"));
    }

    @Test
    public void imageNotUsed() throws Exception {
        System.out.println("   image defined but not used");
        
        String script = "foo = bar; \n";
        
        imageParams.put("foo", Jiffle.ImageRole.DEST);
        imageParams.put("bar", Jiffle.ImageRole.SOURCE);
        imageParams.put("unused", Jiffle.ImageRole.SOURCE);
        
        Map<String, ErrorCode> errors = runClassifier(script);
        
        assertTrue(errors.containsKey("unused"));
        ErrorCode error = errors.get("unused");
        assertEquals(ErrorCode.IMAGE_NOT_USED, error);
        assertTrue(error.isWarning());
    }
    
    @Test
    public void nbrRefOnDestImage() throws Exception {
        System.out.println("   referencing neighbourhood of destination image");
        
        String script = "foo[1,1] = 42; \n";
        imageParams.put("foo", Jiffle.ImageRole.DEST);
        
        Map<String, ErrorCode> errors = null;
        errors = runClassifier(script);

        assertTrue(errors.containsKey("foo"));
        assertEquals(ErrorCode.NBR_REF_ON_DEST_IMAGE_VAR, errors.get("foo"));
    }

    @Test
    public void nbrRefOnNonImageVar() throws Exception {
        System.out.println("   referencing neighbourhood of non-image var");
        
        String script = "foo = x[1,1]; \n" +
        imageParams.put("foo", Jiffle.ImageRole.DEST);
        
        Map<String, ErrorCode> errors = null;
        errors = runClassifier(script);

        assertTrue(errors.containsKey("x"));
        assertEquals(ErrorCode.NBR_REF_ON_NON_IMAGE_VAR, errors.get("x"));
    }

    private Map<String, ErrorCode> runClassifier(String script) throws Exception {
        VarClassifier classifier = new VarClassifier(getAST(script));
        classifier.setImageParams(imageParams);
        
        // set an error handler to avoid parser messages on std err
        classifier.setErrorReporter(new NullErrorReporter());
        
        Map<String, ErrorCode> errors = null;
        try {
            classifier.start();
        } catch (CompilerExitException ex) {
            
        } finally {
            errors = classifier.getErrors();
        }
        
        return errors;
    }
 
}
