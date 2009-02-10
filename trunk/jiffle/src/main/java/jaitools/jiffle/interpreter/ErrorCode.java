/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    
    VAR_UNDEFINED(ErrorLevel.ERROR, "Variable used before being assigned a value");
    
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

