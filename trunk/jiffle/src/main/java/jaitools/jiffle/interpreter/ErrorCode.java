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

package jaitools.jiffle.interpreter;

/**
 *
 * @author Michael Bedward
 */
public enum ErrorCode {
    
    IMAGE_IO(ErrorLevel.ERROR, "Image being used for both input and output"),
    
    IMAGE_NO_OUT(ErrorLevel.ERROR, "No output image (?)"),
    
    IMAGE_UNUSED(ErrorLevel.WARNING, "Image var defined but missing from script"),
    
    INVALID_NBR_REF(ErrorLevel.ERROR, "Neighbourhood reference for non-image variable"),
    
    VAR_UNDEFINED(ErrorLevel.ERROR, "Variable used before being assigned a value"),
    
    FUNC_UNDEFINED(ErrorLevel.ERROR, "Undefined function");
    
    private ErrorLevel level;
    private String desc;

    private ErrorCode(ErrorLevel level, String desc) {
        this.level = level;
        this.desc = desc;
    }
    
    public boolean isError() {
        return level == ErrorLevel.ERROR;
    }

    @Override
    public String toString() {
        if (isError()) {
            return "Error: " + desc;
        } else {
            return "Warning: " + desc;
        }
    }
}

enum ErrorLevel {
    WARNING,
    ERROR;
}

