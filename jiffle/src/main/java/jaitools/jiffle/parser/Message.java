/*
 * Copyright 2009-2011 Michael Bedward
 * 
 * This file is part of jai-tools.
 *
 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package jaitools.jiffle.parser;

/**
 * Constants used by the Jiffle script and tree parsers to report errors 
 * and warnings.
 * 
 * @see Level
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public enum Message {
    
    /**
     * Error: Invalid use of an image variable for both input and output
     */
    ASSIGNMENT_TO_SRC_IMAGE(Level.ERROR, 
            "Cannot assign a value to a non-destination image"),
    
    IMAGE_NOT_USED(Level.WARNING,
            "Image variable is defined but not used"),
    
    IMAGE_VAR_INIT_LHS(Level.ERROR,
            "An image var cannot be assigned to in the init block"),
    
    INVALID_ASSIGNMENT_OP_WITH_DEST_IMAGE(Level.ERROR,
            "Invalid assignment op with destination image"),
    
    IMAGE_POS_ON_DEST(Level.ERROR,
            "Image position cannot be specified for a destination image"),
    
    IMAGE_POS_ON_NON_IMAGE(Level.ERROR,
            "Image position specifier(s) used with a non-image variable"),
    
    READING_FROM_DEST_IMAGE(Level.ERROR, 
            "Cannot read a value from a destination image"),
    
    SRC_IMAGE_IN_INIT_BLOCK(Level.ERROR,
            "Source images cannot be referenced in an init block"),
    
    UNDEFINED_FUNCTION(Level.ERROR,
            "Call to undefined function"),
    
    UNINIT_VAR(Level.ERROR, 
            "Variable used before being assigned a value");
    
    private Level level;
    private String desc;

    private Message(Level level, String desc) {
        this.level = level;
        this.desc = desc;
    }
    
    /**
     * Tests if this is an error
     */
    public boolean isError() {
        return level == Level.ERROR;
    }
    
    /*
     * Tests if this is a warning.
     */
    public boolean isWarning() {
        return level == Level.WARNING;
    }

    /**
     * Return a formatted string for the error or warning
     */
    @Override
    public String toString() {
        if (isError()) {
            return "Error: " + desc;
        } else {
            return "Warning: " + desc;
        }
    }
}


