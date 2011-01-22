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

/**
 * Used by the {@link FunctionLookup} class when servicing lookup requests
 * from the Jiffle compiler.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class FunctionInfo {
    // function provided by java.lang.Math
    public static final int MATH = 1; 
    
    // function provided by JiffleFunctions class
    public static final int JIFFLE = 2; 
    
    // function that is a proxy for a runtime variable
    public static final int PROXY = 3;
    
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
                return "JiffleFunctions." + runtimeName;
            case PROXY:
                return runtimeName;
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

    public boolean isVarArg() {
        return numArgs == VARARG;
    }
    
    public boolean isProxy() {
        return provider == PROXY;
    }

}
