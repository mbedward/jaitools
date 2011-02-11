/*
 * Copyright 2011 Michael Bedward
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

import java.util.List;

import jaitools.CollectionFactory;

/**
 * A lookup service used by the Jiffle compiler when parsing function
 * calls in scripts.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class FunctionLookup {
    
    private List<FunctionInfo> lookup = CollectionFactory.list();
    
    public FunctionLookup() {
        lookup.add( new FunctionInfo("abs", "abs", 1, FunctionInfo.MATH, false) );
        lookup.add( new FunctionInfo( "acos", "acos", 1, FunctionInfo.MATH, false));
        lookup.add( new FunctionInfo( "asin", "asin", 1, FunctionInfo.MATH, false));
        lookup.add( new FunctionInfo( "atan", "atan", 1, FunctionInfo.MATH, false));
        lookup.add( new FunctionInfo( "cos", "cos", 1, FunctionInfo.MATH, false));
        lookup.add( new FunctionInfo( "degToRad", "degToRad", 1, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "floor", "floor", 1, FunctionInfo.MATH, false));
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
        lookup.add( new FunctionInfo( "sign", "sign", 1, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "sdev", "sdev", FunctionInfo.VARARG, FunctionInfo.JIFFLE, false));
        lookup.add( new FunctionInfo( "sin", "sin", 1, FunctionInfo.MATH, false));
        lookup.add( new FunctionInfo( "sqrt", "sqrt", 1, FunctionInfo.MATH, false));
        lookup.add( new FunctionInfo( "tan", "tan", 1, FunctionInfo.MATH, false));
        lookup.add( new FunctionInfo( "variance", "variance", FunctionInfo.VARARG, FunctionInfo.JIFFLE, false));
        
        /*
         * Logical operators
         */
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
        
        /*
         * Image functions which are proxies for runtime variables
         */
        lookup.add( new FunctionInfo( "width", "(double)_width", 0, FunctionInfo.PROXY, false));
        lookup.add( new FunctionInfo( "height", "(double)_height", 0, FunctionInfo.PROXY, false));
        lookup.add( new FunctionInfo( "size", "(double)_size", 0, FunctionInfo.PROXY, false));
        lookup.add( new FunctionInfo( "x", "(double)_x", 0, FunctionInfo.PROXY, false));
        lookup.add( new FunctionInfo( "y", "(double)_y", 0, FunctionInfo.PROXY, false));
        lookup.add( new FunctionInfo( "xmin", "(double)_minx", 0, FunctionInfo.PROXY, false));
        lookup.add( new FunctionInfo( "ymin", "(double)_miny", 0, FunctionInfo.PROXY, false));
        lookup.add( new FunctionInfo( "row", "(double)_row", 0, FunctionInfo.PROXY, false));
        lookup.add( new FunctionInfo( "col", "(double)_col", 0, FunctionInfo.PROXY, false));
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
        List<FunctionInfo> list = CollectionFactory.list();
        for (FunctionInfo info : lookup) {
            if (info.getJiffleName().equals(jiffleName)) {
                list.add(info);
            }
        }

        if (list.isEmpty()) {
            throw new UndefinedFunctionException(jiffleName);
        }
        
        return list;
    }

}
