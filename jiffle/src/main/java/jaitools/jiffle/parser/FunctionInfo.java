/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.parser;

/**
 *
 * @author michael
 */
public class FunctionInfo {
    public static final int MATH = 1; // function provided by java.lang.Math
    public static final int JIFFLE = 2; // function provided by JiffleFunctions class
    public static final int VARARG = -1;
    
    private final String jiffleName;
    private final String runtimeName;
    private final int numArgs; // value of -1 indicates var args
    private final int provider;
    private final boolean isVolatile;

    public FunctionInfo(String jiffleName, String runtimeName, int numArgs, int provider, boolean isVolatile) {
        this.jiffleName = jiffleName;
        this.runtimeName = runtimeName;
        this.numArgs = numArgs;
        this.provider = provider;
        this.isVolatile = isVolatile;
    }

    public String getJiffleName() {
        return jiffleName;
    }

    public String getRuntimeExpr() {
        switch (provider) {
            case MATH:
                return "Math." + runtimeName;
            case JIFFLE:
                return "jaitools.jiffle.runtime.JiffleFunctions." + runtimeName;
            default:
                throw new IllegalStateException("Internal compiler error: getRuntimeExpr");
        }
    }

    public boolean isVolatile() {
        return isVolatile;
    }

    public int getNumArgs() {
        return numArgs;
    }

    boolean isVarArg() {
        return numArgs == VARARG;
    }

}
