/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle;

/**
 * @author Michael Bedward
 */
public class CompilationProblem {
    private final ErrorCode code;

    public CompilationProblem(ErrorCode code) {
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }

    public boolean isError() {
        return code.isError();
    }

    @Override
    public String toString() {
        return code.toString();
    }

}
