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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test error checking for image vars in a jiffle script
 * 
 * @author Michael Bedward
 */
public class ImageVarValidatorTest extends TreeWalkerTestBase {

    /**
     * Test checking for images being used for both input
     * and output
     * @throws java.lang.Exception
     */
    @Test
    public void testImageVarError() throws Exception {
        System.out.println("   testImageVarError");
        
        String input = 
                "imgY = y();" +
                "imgX = x();" +
                "a = imgX * imgZ;";  // error: imgX both input and output
        
        String[] imageVars = {"imgX", "imgY", "imgFoo"};
        
        ImageVarValidator validator = new ImageVarValidator(getAST(input));
        validator.setImageVars(imageVars);
        
        validator.start();
        
        assertTrue(validator.hasImageVarError());
        assertTrue(validator.getErrorImageVars().contains("imgX"));
        assertFalse(validator.getErrorImageVars().contains("imgY"));
        assertFalse(validator.getErrorImageVars().contains("imgZ"));
    }
    
}
