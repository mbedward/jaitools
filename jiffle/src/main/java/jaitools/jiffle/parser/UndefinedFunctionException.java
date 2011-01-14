/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.parser;

/**
 *
 * @author michael
 */
public class UndefinedFunctionException extends Exception {

    public UndefinedFunctionException(String funcName) {
        super("Undefined function: " + funcName);
    }
    
}
