/*
 * Copyright 2009 Michael Bedward
 *
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jaitools.jiffle.runtime;

/**
 * Defines constants and associated Jiffle function names for logical operators.
 * These are used by {@code FunctionTable} and {@code JiffleRunner} as well as
 * in the ANTLR grammar file ImageCalculator.g.
 * <p>
 * Logical operators are dealt with separately to arithmetic operators because of
 * the need to handle NaN values differently to the Java standard.
 *
 * @see FunctionTable
 * @see JiffleRunner#invokeLogicalOp(LogicalOp, Double)
 * @see JiffleRunner#invokeLogicalOp(LogicalOp, Double, Double)
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public enum LogicalOp {

    OR("logical-or", 2),
    AND("logical-and", 2),
    XOR("logical-xor", 2),
    GT("compare-gt", 2),
    GE("compare-ge", 2),
    LT("compare-lt", 2),
    LE("compare-le", 2),
    LOGICALEQ("compare-eq", 2),
    NE("compare-ne", 2),
    PREFIX_NOT("prefix-not", 1);

    private final String functionName;
    private final int numArgs;

    private LogicalOp(String functionName, int numArgs) {
        this.functionName = functionName;
        this.numArgs = numArgs;
    }

    /**
     * Returns the string representation which is the Jiffle function name
     *
     * @return Jiffle function name for this constant
     */
    @Override
    public String toString() {
        return functionName;
    }

    /**
     * Returns the number of arguments that the operator expects
     *
     * @return number of arguments
     */
    public int getNumArgs() {
        return numArgs;
    }

    /**
     * Returns the function name in the form {@code name_n} where {@code n} is
     * the number of arguments. This is used for method registering in
     * {@code FunctionTable}.
     *
     * @return function name with number of arguments appended
     */
    public String getFunctionName() {
        return String.format("%s_%d", functionName, numArgs);
    }
}
