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

import jaitools.CollectionFactory;
import jaitools.numeric.SampleStats;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static jaitools.numeric.DoubleComparison.*;

/**
 * A symbol table for jiffle functions, pre-loaded with a basic set of 
 * mathematical and conditional functions
 *  
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class FunctionTable {

    private static final Random rr = new Random();
    private static final Map<String, OpBase> staticFunctions;

    static {
        staticFunctions = CollectionFactory.newTreeMap();

        staticFunctions.put("abs_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.abs(x);
                    }
                });

        staticFunctions.put("acos_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.acos(x);
                    }
                });

        staticFunctions.put("asin_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.asin(x);
                    }
                });

        staticFunctions.put("atan_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.atan(x);
                    }
                });

        staticFunctions.put("cos_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.cos(x);
                    }
                });

        staticFunctions.put("degToRad_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.PI * x / 180d;
                    }
                });

        staticFunctions.put("floor_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.floor(x);
                    }
                });

        staticFunctions.put("if_1",
                new Op1Arg() {
                    public double call(double x) {
                        if (!Double.isNaN(x)) {
                            return !dzero(x) ? 1d : 0d;
                        }
                        
                        return Double.NaN;
                    }
                });

        staticFunctions.put("if_2",
                new Op2Arg() {
                    public double call(double x, double a) {
                        if (!Double.isNaN(x)) {
                            return !dzero(x) ? a : 0d;
                        }

                        return Double.NaN;
                    }
                });

        staticFunctions.put("if_3",
                new Op3Arg() {
                    public double call(double x, double a, double b) {
                        if (!Double.isNaN(x)) {
                            return !dzero(x) ? a : b;
                        }

                        return Double.NaN;
                    }
                });

        staticFunctions.put("if_4",
                new Op4Arg() {
                    public double call(double x, double a, double b, double c) {
                        if (!Double.isNaN(x)) {
                            return dzero(x) ? b : (x > 0 ? a : c);
                        }

                        return Double.NaN;
                    }
                });

        staticFunctions.put("isinf_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Double.isInfinite(x) ? 1d : 0d;
                    }
                });

        staticFunctions.put("isnan_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Double.isNaN(x) ? 1d : 0d;
                    }
                });

        staticFunctions.put("isnull_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Double.isNaN(x) ? 1d : 0d;
                    }
                });

        staticFunctions.put("log_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.log(x);
                    }
                });
                
        staticFunctions.put("log_2",
                new Op2Arg() {
                    public double call(double x, double y) {
                        return Math.log(x) / Math.log(y);
                    }
                });
                
        staticFunctions.put("max_v",
                new OpVarArgs() {
                    public double call(Double ...values) {
                        return SampleStats.max(values, true);
                    }
                });

        staticFunctions.put("mean_v",
                new OpVarArgs() {
                    public double call(Double ...values) {
                        return SampleStats.mean(values, true);
                    }
        });
        
        staticFunctions.put("median_v",
                new OpVarArgs() {
                    public double call(Double ...values) {
                        return SampleStats.median(values, true);
                    }
                });
                
        staticFunctions.put("min_v",
                new OpVarArgs() {
                    public double call(Double ...values) {
                        return SampleStats.min(values, true);
                    }
                });
                
        staticFunctions.put("mode_v",
                new OpVarArgs() {
                    public double call(Double ...values) {
                        return SampleStats.mode(values, true);
                    }
                });
                
        staticFunctions.put("null_0",
                new OpNoArg() {
                    public double call() {
                        return Double.NaN;
                    }
                });
                
        staticFunctions.put("radToDeg_1",
                new Op1Arg() {
                    public double call(double x) {
                        return x / Math.PI * 180d;
                    }
                });

        staticFunctions.put("rand_1",
                new Op1Arg() {
                    public double call(double x) {
                        return rr.nextDouble() * x;
                    }
                });

        staticFunctions.put("randInt_1",
                new Op1Arg() {
                    public double call(double x) {
                        return rr.nextInt((int) x);
                    }
                });

        staticFunctions.put("range_v",
                new OpVarArgs() {
                    public double call(Double ...values) {
                        return SampleStats.range(values, true);
                    }
                });
                
        staticFunctions.put("round_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.round(x);
                    }
                });
                
        staticFunctions.put("round_2",
                new Op2Arg() {
                    public double call(double x, double fac) {
                        int ifac = (int)(fac + 0.5);
                        return Math.round(x / ifac) * ifac;
                    }
                });

        staticFunctions.put("sdev_v",
                new OpVarArgs() {
                    public double call(Double ...values) {
                        return SampleStats.sdev(values, true);
                    }
                });

        staticFunctions.put("sin_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.sin(x);
                    }
                });

        staticFunctions.put("sqrt_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.sqrt(x);
                    }
                });

        staticFunctions.put("tan_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.tan(x);
                    }
                });

        staticFunctions.put("variance_v",
                new OpVarArgs() {
                    public double call(Double ...values) {
                        return SampleStats.variance(values, true);
                    }
                });

        staticFunctions.put(LogicalOp.OR.getFunctionName(),
                new Op2Arg() {
                    public double call(double x1, double x2) {
                        if (Double.isNaN(x1) || Double.isNaN(x2)) {
                            return Double.NaN;
                        }

                        return (!dzero(x1) || !dzero(x2) ? 1d : 0d);
                    }
                });

        staticFunctions.put(LogicalOp.AND.getFunctionName(),
                new Op2Arg() {
                    public double call(double x1, double x2) {
                        if (Double.isNaN(x1) || Double.isNaN(x2)) {
                            return Double.NaN;
                        }

                        return (!dzero(x1) && !dzero(x2) ? 1d : 0d);
                    }
                });

        staticFunctions.put(LogicalOp.XOR.getFunctionName(),
                new Op2Arg() {
                    public double call(double x1, double x2) {
                        if (Double.isNaN(x1) || Double.isNaN(x2)) {
                            return Double.NaN;
                        }

                        return (!dzero(x1) ^ !dzero(x2) ? 1d : 0d);
                    }
                });

        staticFunctions.put(LogicalOp.GT.getFunctionName(),
                new Op2Arg() {
                    public double call(double x1, double x2) {
                        if (Double.isNaN(x1) || Double.isNaN(x2)) {
                            return Double.NaN;
                        }

                        return (dcomp(x1, x2) > 0 ? 1d : 0d);
                    }
                });

        staticFunctions.put(LogicalOp.GE.getFunctionName(),
                new Op2Arg() {
                    public double call(double x1, double x2) {
                        if (Double.isNaN(x1) || Double.isNaN(x2)) {
                            return Double.NaN;
                        }

                        return (dcomp(x1, x2) >= 0 ? 1d : 0d);
                    }
                });

        staticFunctions.put(LogicalOp.LT.getFunctionName(),
                new Op2Arg() {
                    public double call(double x1, double x2) {
                        if (Double.isNaN(x1) || Double.isNaN(x2)) {
                            return Double.NaN;
                        }

                        return (dcomp(x1, x2) < 0 ? 1d : 0d);
                    }
                });

        staticFunctions.put(LogicalOp.LE.getFunctionName(),
                new Op2Arg() {
                    public double call(double x1, double x2) {
                        if (Double.isNaN(x1) || Double.isNaN(x2)) {
                            return Double.NaN;
                        }

                        return (dcomp(x1, x2) <= 0 ? 1d : 0d);
                    }
                });

        staticFunctions.put(LogicalOp.LOGICALEQ.getFunctionName(),
                new Op2Arg() {
                    public double call(double x1, double x2) {
                        if (Double.isNaN(x1) || Double.isNaN(x2)) {
                            return Double.NaN;
                        }

                        return (dcomp(x1, x2) == 0 ? 1d : 0d);
                    }
                });

        staticFunctions.put(LogicalOp.NE.getFunctionName(),
                new Op2Arg() {
                    public double call(double x1, double x2) {
                        if (Double.isNaN(x1) || Double.isNaN(x2)) {
                            return Double.NaN;
                        }

                        return (dcomp(x1, x2) != 0 ? 1d : 0d);
                    }
                });

        staticFunctions.put(LogicalOp.PREFIX_NOT.getFunctionName(),
                new Op1Arg() {
                    public double call(double x) {
                        if (Double.isNaN(x)) {
                            return Double.NaN;
                        }

                        return (dzero(x) ? 1d : 0d);
                    }
                });

    }

    private Map<String, OpBase> instanceFunctions;

    protected void createInstanceFunctions() {
        instanceFunctions = CollectionFactory.newTreeMap();

        instanceFunctions.put("width_0",
                new OpNoArg() {
                    public double call() {
                        return runtimeBounds.getWidth();
                    }
                });

        instanceFunctions.put("height_0",
                new OpNoArg() {
                    public double call() {
                        return runtimeBounds.getHeight();
                    }
                });

        instanceFunctions.put("size_0",
                new OpNoArg() {
                    public double call() {
                        return runtimeNumPixels;
                    }
                });

        instanceFunctions.put("x_0",
                new OpNoArg() {
                    public double call() {
                        return runtimePosition.x;
                    }
                });

        instanceFunctions.put("y_0",
                new OpNoArg() {
                    public double call() {
                        return runtimePosition.y;
                    }
                });
    }


    private static final Set<String> imageInfoFuncs = new HashSet<String>();
    static {
        for (String name : new String[]{"width", "height", "size"}) {
            imageInfoFuncs.add(name);
        }
    }

    private static final Set<String> imagePositionFuncs = new HashSet<String>();
    static {
        for (String name : new String[]{"x", "y", "row", "col"}) {
            imagePositionFuncs.add(name);
        }
    }

    private static final Set<String> volatileFuncs = new HashSet<String>();
    static {
        volatileFuncs.add("rand");
        volatileFuncs.add("randInt");
        volatileFuncs.addAll(imagePositionFuncs);
    }

    private Rectangle runtimeBounds;
    private double runtimeNumPixels;
    private Point runtimePosition;

    /**
     * Constructor
     */
    public FunctionTable() {
    }

    /**
     * Check if a function with a given number of arguments is defined.
     * This checks both for functions with a fixed number of arguments
     * and those with a variable number of arguments.
     * 
     * @param name the function name
     * @param numArgs number of arguments
     * @return true if the function is defined; false otherwise
     */
    public static boolean isDefined(String name, int numArgs) {
        OpBase op = getMethod(name, numArgs);
        return op != null;
    }

    /**
     * Check if a function is volatile, ie. intended to be called each
     * time the image position changes
     *
     * @param name the function name
     * @return true if the function is volatile; false otherwise
     */
    public static boolean isVolatile(String name) {
        return volatileFuncs.contains(name);
    }

    /**
     * Query whether a function name refers to a positional
     * function, ie. one which returns the current pixel location
     * such as x().
     *
     * @param name function name
     * @return true if a positional function; false otherwise
     */
    public static boolean isPositionalFunction(String name) {
        return imagePositionFuncs.contains(name);
    }

    /**
     * Query if a function name refers to an image info function,
     * e.g. width() which returns image width in pixels
     *
     * @param name function name
     * @return true if an image info function; false otherwise
     */
    public static boolean isInfoFunction(String name) {
        return imageInfoFuncs.contains(name);
    }

    /**
     * Invokes the named function, passing the list of arguments to it
     * and returning the function's result.
     * 
     * @param name the function name
     * @param args list of double arguments (may be empty or null)
     * @return the result of the named function
     */
    public double invoke(String name, List<Double> args) {
        OpBase op = getMethod(name, args.size());
        if (op == null) {
            throw new RuntimeException("unknown function: " + name + 
                    "with " + args.size() + " args");
        }
        
        int numArgs = (args == null ? 0 : args.size());

        switch (numArgs) {
            case 0:
                return ((OpNoArg) op).call();

            case 1:
                return ((Op1Arg) op).call(args.get(0));

            case 2:
                return ((Op2Arg) op).call(args.get(0), args.get(1));
                
            case 3:
                return ((Op3Arg) op).call(args.get(0), args.get(1), args.get(2));

            case 4:
                return ((Op4Arg) op).call(args.get(0), args.get(1), args.get(2), args.get(3));

            default:
                throw new IllegalStateException(
                        "unsupported function: " + name + " with " + args.size() + " args");
        }
    }

    
    public double invoke(String name, Double x) {
        OpBase op = getMethod(name, 1);
        if (op == null) {
            throw new RuntimeException("unknown function: " + name + " with 1 arg");
        }

        return ((Op1Arg) op).call(x);
    }

    public double invoke(String name, Double x1, Double x2) {
        OpBase op = getMethod(name, 2);
        if (op == null) {
            throw new RuntimeException("unknown function: " + name + " with 2 args");
        }

        return ((Op2Arg) op).call(x1, x2);
    }

    /**
     * Get a function that matches the given name and number of
     * arguments. A search is made for a matching variable argument
     * function first and, if no match is found, the list of 
     * fixed argument functions with numArgs arguments is searched.
     * 
     * @param name the function name
     * @param numArgs the number of arguments
     * @return an OpBase reference if a match is found, or null otherwise
     */
    private static OpBase getMethod(String name, int numArgs) {
        OpBase op;
        
        // first check for a match with var args functions
        op = staticFunctions.get(name + "_v");
        
        if (op == null) {
            // check for a match with fixed arg functions
            return staticFunctions.get(name + "_" + numArgs);
        }
        
        return op;
    }

    void setRuntimeBounds(Rectangle bounds) {
        runtimeBounds = new Rectangle(bounds);
        runtimeNumPixels = runtimeBounds.getWidth() * runtimeBounds.getHeight();
    }
}
