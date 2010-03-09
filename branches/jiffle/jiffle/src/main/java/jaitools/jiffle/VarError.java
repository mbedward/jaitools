/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle;

/**
 * @author Michael Bedward
 */
public class VarError extends CompilationProblem {
    private final String varName;

    public VarError(ErrorCode code, String varName) {
        super(code);
        this.varName = varName;
    }

    @Override
    public String toString() {
        return String.format("For variable %s: %s", varName, getCode().toString());
    }

}
