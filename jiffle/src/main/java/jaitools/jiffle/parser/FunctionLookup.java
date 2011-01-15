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
            + "public class " + FunctionLookup.EVALUATOR_CLASS_NAME + " implements CompileTimeEvaluator { \n"
            + "    public Double evaluate(CompileTimeVariables vars) { \n";
    
    private List<FunctionInfo> lookup = new ArrayList<FunctionInfo>();
    

    public FunctionLookup() {
        lookup.add( new FunctionInfo("abs", "abs", 1, FunctionInfo.MATH, false) );
        lookup.add( new FunctionInfo( "acos", "acos", 1, FunctionInfo.MATH, false));
        lookup.add( new FunctionInfo( "asin", "asin", 1, FunctionInfo.MATH, false));
        lookup.add( new FunctionInfo( "atan", "atan", 1, FunctionInfo.MATH, false));
        lookup.add( new FunctionInfo( "cos", "cos", 1, FunctionInfo.MATH, false));
        lookup.add( new FunctionInfo( "degToRad", "degToRad", 1, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "floor", "floor", 1, FunctionInfo.MATH, false));
        lookup.add( new FunctionInfo( "if", "if1Arg", 1, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "if", "if2Arg", 2, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "if", "if3Arg", 3, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "if", "if4Arg", 4, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "isinf", "isinf", 1, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "isnan", "isnan", 1, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "isnull", "isnull", 1, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "log", "log", 1, FunctionInfo.MATH, false));
        lookup.add( new FunctionInfo( "log", "log2Arg", 2, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "max", "max", FunctionInfo.VARARG, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "mean", "mean", FunctionInfo.VARARG, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "median", "median", FunctionInfo.VARARG, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "min", "min", FunctionInfo.VARARG, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "mode", "mode", FunctionInfo.VARARG, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "null", "nullValue", 0, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "radToDeg", "radToDeg", 1, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "rand", "rand", 1, FunctionInfo.JIFFLE, true));
        lookup.add( new FunctionInfo( "randInt", "randInt", 1, FunctionInfo.JIFFLE, true));
        lookup.add( new FunctionInfo( "range", "range", 1, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "round", "round", 1, FunctionInfo.MATH, false));
        lookup.add( new FunctionInfo( "round", "round2Arg", 2, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "sdev", "sdev", FunctionInfo.VARARG, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "sin", "sin", 1, FunctionInfo.MATH, false));
        lookup.add( new FunctionInfo( "sqrt", "sqrt", 1, FunctionInfo.MATH, false));
        lookup.add( new FunctionInfo( "tan", "tan", 1, FunctionInfo.MATH, false));
        lookup.add( new FunctionInfo( "variance", "variance", FunctionInfo.VARARG, FunctionInfo.JIFFLE, false));
        
        lookup.add( new FunctionInfo( "OR", "OR", 2, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "AND", "AND", 2, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "XOR", "XOR", 2, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "GT", "GT", 2, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "GE", "GE", 2, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "LT", "LT", 2, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "LE", "LE", 2, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "EQ", "EQ", 2, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "NE", "NE", 2, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "NOT", "NOT", 1, FunctionInfo.JIFFLE, false));
    }
    
    
    public boolean isDefined(String jiffleName, int numArgs) {
        try {
            getInfo(jiffleName, numArgs);
        } catch (UndefinedFunctionException ex) {
            return false;
        }
        
        return true;
    }
    
    public FunctionInfo getInfo(String jiffleName, int numArgs) throws UndefinedFunctionException {
        List<FunctionInfo> list = getByName(jiffleName);
        for (FunctionInfo info : list) {
            if (info.getJiffleName().equals(jiffleName)
                    && (info.isVarArg() || info.getNumArgs() == numArgs)) {
                return info;
            }
        }
        
        // should never get here
        throw new IllegalStateException("Internal compiler error");
    }
    
    public String getRuntimeExpr(String jiffleName, int numArgs) throws UndefinedFunctionException {
        return getInfo(jiffleName, numArgs).getRuntimeExpr();
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
