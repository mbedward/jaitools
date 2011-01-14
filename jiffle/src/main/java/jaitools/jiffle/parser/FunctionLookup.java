/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.parser;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author michael
 */
public class FunctionLookup {
    
    private static final String EVALUATOR_CLASS_NAME = "TempEval";
    
    private static final String EVALUATOR_CLASS_BODY =
            "package jaitools.jiffle.parser; \n"
            + "public class " + EVALUATOR_CLASS_NAME + " implements CompileTimeEvaluator { \n"
            + "    public Double evaluate(CompileTimeVariables vars) { \n";
    
    
    private static final int MATH = 1; // function provided by java.lang.Math
    private static final int JIFFLE = 2; // function provided by JiffleFunctions class
    
    private static final int VARARG = -1;
    
    private static class FunctionInfo {
        final String jiffleName;
        final String runtimeName;
        final int numArgs;  // value of -1 indicates var args
        final int provider;
        final boolean isVolatile;

        public FunctionInfo(String jiffleName, String runtimeName, 
                int numArgs, int provider, boolean isVolatile) {
            this.jiffleName = jiffleName;
            this.runtimeName = runtimeName;
            this.numArgs = numArgs;
            this.provider = provider;
            this.isVolatile = isVolatile;
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
    }
    
    private List<FunctionInfo> lookup = new ArrayList<FunctionInfo>();
    

    public FunctionLookup() {
        lookup.add( new FunctionInfo("abs", "abs", 1, MATH, false) );
        lookup.add( new FunctionInfo( "acos", "acos", 1, MATH, false));
        lookup.add( new FunctionInfo( "asin", "asin", 1, MATH, false));
        lookup.add( new FunctionInfo( "atan", "atan", 1, MATH, false));
        lookup.add( new FunctionInfo( "cos", "cos", 1, MATH, false));
        lookup.add( new FunctionInfo( "degToRad", "degToRad", 1, JIFFLE, false));
        lookup.add( new FunctionInfo( "floor", "floor", 1, MATH, false));
        lookup.add( new FunctionInfo( "if", "if1Arg", 1, JIFFLE, false));
        lookup.add( new FunctionInfo( "if", "if2Arg", 2, JIFFLE, false));
        lookup.add( new FunctionInfo( "if", "if3Arg", 3, JIFFLE, false));
        lookup.add( new FunctionInfo( "if", "if4Arg", 4, JIFFLE, false));
        lookup.add( new FunctionInfo( "isinf", "isinf", 1, JIFFLE, false));
        lookup.add( new FunctionInfo( "isnan", "isnan", 1, JIFFLE, false));
        lookup.add( new FunctionInfo( "isnull", "isnull", 1, JIFFLE, false));
        lookup.add( new FunctionInfo( "log", "log", 1, MATH, false));
        lookup.add( new FunctionInfo( "log", "log2Arg", 2, JIFFLE, false));
        lookup.add( new FunctionInfo( "max", "max", VARARG, JIFFLE, false));
        lookup.add( new FunctionInfo( "mean", "mean", VARARG, JIFFLE, false));
        lookup.add( new FunctionInfo( "median", "median", VARARG, JIFFLE, false));
        lookup.add( new FunctionInfo( "min", "min", VARARG, JIFFLE, false));
        lookup.add( new FunctionInfo( "mode", "mode", VARARG, JIFFLE, false));
        lookup.add( new FunctionInfo( "null", "nullValue", 0, JIFFLE, false));
        lookup.add( new FunctionInfo( "radToDeg", "radToDeg", 1, JIFFLE, false));
        lookup.add( new FunctionInfo( "rand", "rand", 1, JIFFLE, true));
        lookup.add( new FunctionInfo( "randInt", "randInt", 1, JIFFLE, true));
        lookup.add( new FunctionInfo( "range", "range", 1, JIFFLE, false));
        lookup.add( new FunctionInfo( "round", "round", 1, MATH, false));
        lookup.add( new FunctionInfo( "round", "round2Arg", 2, JIFFLE, false));
        lookup.add( new FunctionInfo( "sdev", "sdev", VARARG, JIFFLE, false));
        lookup.add( new FunctionInfo( "sin", "sin", 1, MATH, false));
        lookup.add( new FunctionInfo( "sqrt", "sqrt", 1, MATH, false));
        lookup.add( new FunctionInfo( "tan", "tan", 1, MATH, false));
        lookup.add( new FunctionInfo( "variance", "variance", VARARG, JIFFLE, false));
        
        lookup.add( new FunctionInfo( "OR", "OR", 2, JIFFLE, false));
        lookup.add( new FunctionInfo( "AND", "AND", 2, JIFFLE, false));
        lookup.add( new FunctionInfo( "XOR", "XOR", 2, JIFFLE, false));
        lookup.add( new FunctionInfo( "GT", "GT", 2, JIFFLE, false));
        lookup.add( new FunctionInfo( "GE", "GE", 2, JIFFLE, false));
        lookup.add( new FunctionInfo( "LT", "LT", 2, JIFFLE, false));
        lookup.add( new FunctionInfo( "LE", "LE", 2, JIFFLE, false));
        lookup.add( new FunctionInfo( "EQ", "EQ", 2, JIFFLE, false));
        lookup.add( new FunctionInfo( "NE", "NE", 2, JIFFLE, false));
        lookup.add( new FunctionInfo( "NOT", "NOT", 1, JIFFLE, false));
    }
    
    public String getRuntimeExpr(String jiffleName, int numArgs) {
        try {
            return getByNameAndArgs(jiffleName, numArgs).getRuntimeExpr();
            
        } catch(UndefinedFunctionException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    public boolean isDefined(String jiffleName, int numArgs) {
        try {
            getByNameAndArgs(jiffleName, numArgs);
        } catch (UndefinedFunctionException ex) {
            return false;
        }
        
        return true;
    }
    
    public boolean isVolatile(String jiffleName) {
        try {
            List<FunctionInfo> list = getByName(jiffleName);
            return list.get(0).isVolatile;
            
        } catch (UndefinedFunctionException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    private FunctionInfo getByNameAndArgs(String jiffleName, int numArgs) throws UndefinedFunctionException {
        List<FunctionInfo> list = getByName(jiffleName);
        for (FunctionInfo info : list) {
            if (info.jiffleName.equals(jiffleName)
                    && (info.numArgs == numArgs || info.numArgs == VARARG)) {
                return info;
            }
        }

        throw new IllegalArgumentException("Unrecognized function name: " + jiffleName);
    }
    
    private List<FunctionInfo> getByName(String jiffleName) throws UndefinedFunctionException {
        List<FunctionInfo> list = new ArrayList<FunctionInfo>();
        for (FunctionInfo info : lookup) {
            list.add(info);
        }

        if (list.isEmpty()) {
            throw new UndefinedFunctionException(jiffleName);
        }
        
        return list;
    }

}
