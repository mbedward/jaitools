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

import java.util.List;
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
public class VarClassifierTest extends ParserTestBase {
    
    private Map<String, Jiffle.ImageRole> imageParams;
    private MessageTable msgTable;
    
    @Before
    public void setup() {
        imageParams = CollectionFactory.map();
        msgTable = new MessageTable();
    }

    @Test
    public void varUsedBeforeAssignment() throws Exception {
        System.out.println("   using a var before assignment");

        String script = 
                "dest = src + 1; \n" +
                "src = 42; \n";
                
        runClassifier(script);
        assertGotError("src", Message.UNINIT_VAR);
    }
    
    @Test
    public void assignmentToSourceImage() throws Exception {
        System.out.println("   writing to a source image");
        
        String script = "dest = 42; \n";
        imageParams.put("dest", Jiffle.ImageRole.SOURCE);
        
        runClassifier(script);
        assertGotError("dest", Message.ASSIGNMENT_TO_SRC_IMAGE);
    }
    
    @Test
    public void readingFromDestinationImage() throws Exception {
        System.out.println("   reading from a destination image");
        
        String script = "x = dest; \n";
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        
        runClassifier(script);
        assertGotError("dest", Message.READING_FROM_DEST_IMAGE);
    }

    @Test
    public void imageNotUsed() throws Exception {
        System.out.println("   image defined but not used");
        
        String script = "dest = src; \n";
        
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        imageParams.put("src", Jiffle.ImageRole.SOURCE);
        imageParams.put("unused", Jiffle.ImageRole.SOURCE);
        
        runClassifier(script);
        assertGotError("unused", Message.IMAGE_NOT_USED);
    }
    
    @Test
    public void nbrRefOnDestImage() throws Exception {
        System.out.println("   referencing neighbourhood of destination image");
        
        String script = "dest[1,1] = 42; \n";
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        
        runClassifier(script);
        assertGotError("dest", Message.NBR_REF_ON_DEST_IMAGE_VAR);
    }

    @Test
    public void nbrRefOnNonImageVar() throws Exception {
        System.out.println("   referencing neighbourhood of non-image var");
        
        String script = "dest = x[1,1]; \n" +
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        
        runClassifier(script);
        assertGotError("x", Message.NBR_REF_ON_NON_IMAGE_VAR);
    }

    private void runClassifier(String script) throws Exception {
        VarClassifier classifier = new VarClassifier(getAST(script), imageParams, msgTable);
        
        // set an error handler to avoid parser messages on std err
        classifier.setErrorReporter(new NullErrorReporter());
        
        try {
            classifier.start();
        } catch (CompilerExitException ex) {
            
        } 
    }

    private void assertGotError(String key, Message code) {
        Map<String, List<Message>> messages = msgTable.getMessages();
        assertTrue(messages.containsKey(key));
        assertTrue(messages.get(key).contains(code));
    }
 
}
