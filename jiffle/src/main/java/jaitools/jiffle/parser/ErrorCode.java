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

import jaitools.jiffle.parser.ErrorLevel;

/**
 * Constants used by the Jiffle compiler to report errors and warnings.
 * 
 * @see ErrorLevel
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public enum ErrorCode {
    
    /**
     * Error: Invalid use of an image variable for both input and output
     */
    ASSIGNMENT_TO_SRC_IMAGE(ErrorLevel.ERROR, 
            "Cannot assign a value to a non-destination image"),
    
    IMAGE_NOT_USED(ErrorLevel.WARNING,
            "Image variable is defined but not used"),
    
    NBR_REF_ON_DEST_IMAGE_VAR(ErrorLevel.ERROR, 
            "Neighbourhood reference cannot be used with a destination image"),
    
    NBR_REF_ON_NON_IMAGE_VAR(ErrorLevel.ERROR, 
            "Neighbourhood reference cannot be used with a non-image variable"),
    
    READING_FROM_DEST_IMAGE(ErrorLevel.ERROR, 
            "Cannot read a value from a destination image"),
    
    UNDEFINED_FUNCTION(ErrorLevel.ERROR,
            "Call to undefined function"),
    
    UNINIT_VAR(ErrorLevel.ERROR, 
            "Variable used before being assigned a value");
    
    private ErrorLevel level;
    private String desc;

    private ErrorCode(ErrorLevel level, String desc) {
        this.level = level;
        this.desc = desc;
    }
    
    /**
     * Query whether an error code is an error or a warning
     * @return true for error; false for warning
     */
    public boolean isError() {
        return level == ErrorLevel.ERROR;
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


