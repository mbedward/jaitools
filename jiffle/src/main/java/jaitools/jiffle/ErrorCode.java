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

package jaitools.jiffle;

/**
 * Enum constants for compilation errors and warnings. Each constant has a description
 * and an error level (presently just ErrorLevel.ERROR or ErrorLevel.WARNING).
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
    IMAGE_IO(ErrorLevel.ERROR, "Image being used for both input and output"),
    
    /**
     * Error: No output image specified
     */
    IMAGE_NO_OUT(ErrorLevel.ERROR, "No output image (?)"),
    
    /**
     * Error: neighbourhood reference used with a variable that is not
     * an input image
     */
    INVALID_NBR_REF(ErrorLevel.ERROR, "Neighbourhood reference but not an input image"),
    
    /**
     * Error: a local variable is used before being assigned a value
     */
    VAR_UNDEFINED(ErrorLevel.ERROR, "Variable used before being assigned a value"),
    
    /**
     * Error: call to an unrecognized function
     */
    FUNC_UNDEFINED(ErrorLevel.ERROR, "Calling an unrecognized function"),
    
    /**
     * Warning: Image variable defined but not used in the script
     */
    IMAGE_UNUSED(ErrorLevel.WARNING, "Image var defined but missing from script");
    
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

/**
 * Error levels used by {@link ErrorCode}
 */
enum ErrorLevel {
    WARNING,
    ERROR;
}

