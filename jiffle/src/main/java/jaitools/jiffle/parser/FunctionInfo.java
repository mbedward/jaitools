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
 * @version $Id$
 */
public class FunctionInfo {
    
    /** Constants to indicate the runtime provider of a function */
    public enum Provider {
        /** Indicates a function provided by JiffleFunctions class */
        JIFFLE("jiffle"),
        /** Indicates a function provided by java.lang.Math */
        MATH("math"),
        /** Indicates a function that is a proxy for a runtime class variable */
        PROXY("proxy");

        private String name;
        private Provider(String name) {
            this.name = name;
        }

        /**
         * Gets the {@code Provider} for a given provider name.
         *
         * @param name the provider name to look up
         *
         * @return the {@code Provider} or null if the name was not found
         */
        public static Provider get(String name) {
            String s = name.toLowerCase().trim();
            for (Provider p : Provider.values()) {
                if (p.name.equals(s)) {
                    return p;
                }
            }
            return null;
        }
    }
    
    /** Flag value used with variable argument functions */
    public static final int VARARG = -1;
    
    private final String jiffleName;
    private final String runtimeName;
    private final int numArgs; // value of -1 indicates var args
    private final Provider provider;
    private final boolean isVolatile;

    /**
     * Creates a function info object.
     *
     * @param jiffleName name of the function used in Jiffle scripts
     * @param runtimeName Java name used in runtime class source
     * @param numArgs number of arguments or {@link #VARARG}
     * @param provider the provider: one of {@link #JIFFLE}, {@link #MATH} or {@link #PROXY}
     * @param isVolatile {@code true} if the function returns a new value on each
     *        invocation regardless of pixel position (e.g. rand()); {@code false}
     *        otherwise
     */
    public FunctionInfo(String jiffleName, String runtimeName, int numArgs, Provider provider, boolean isVolatile) {
        this.jiffleName = jiffleName;
        this.runtimeName = runtimeName;
        this.numArgs = numArgs;
        this.provider = provider;
        this.isVolatile = isVolatile;
    }

    /**
     * Gets the name of the function used in Jiffle scripts.
     *
     * @return Jiffle function name
     */
    public String getJiffleName() {
        return jiffleName;
    }

    /**
     * Gets the Java source for the function provider and name used
     * in the runtime class.
     *
     * @return runtime class source for the function
     */
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

    /**
     * Tests if this function is volatile, ie. returns a different value
     * on each invocation regardless of image position.
     *
     * @return {@code true} if volatile, {@code false} otherwise
     */
    public boolean isVolatile() {
        return isVolatile;
    }

    /**
     * Gets the number of arguments used by the function or {@link #VARARG}
     * for a variable argument function.
     *
     * @return number of arguments
     */
    public int getNumArgs() {
        return numArgs;
    }

    /**
     * Convenience function, equivalent to {@code getNumArgs() == FunctionInfo.VARARG}.
     *
     * @return {@code true} ir a variable argument function; {@code false} otherwise
     */
    public boolean isVarArg() {
        return numArgs == VARARG;
    }
    
    /**
     * Tests if this is a proxy function, ie. one that is translated to a
     * runtime class field defined by Jiffle. Examples are {@code x()} and
     * {@code width()}.
     *
     * @return {@code true} ir a proxy function; {@code false} otherwise
     */
    public boolean isProxy() {
        return provider == Provider.PROXY;
    }

}
