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

import jaitools.utils.CollectionFactory;
import jaitools.numeric.SampleStats;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static jaitools.numeric.DoubleComparison.*;

/**
 * A symbol table for jiffle functions, pre-loaded with a basic set of 
 * mathematical and conditional functions
 *  
 * @author Michael Bedward
 */
public class FunctionTable {

    private static Random rr = new Random();
    private static Map<String, OpBase> lookup = null;
    

    static {
        lookup = CollectionFactory.newTreeMap();

        lookup.put("abs_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.abs(x);
                    }
                });

        lookup.put("acos_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.acos(x);
                    }
                });

        lookup.put("asin_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.asin(x);
                    }
                });

        lookup.put("atan_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.atan(x);
                    }
                });

        lookup.put("cos_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.cos(x);
                    }
                });

        lookup.put("degToRad_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.PI * x / 180d;
                    }
                });

        lookup.put("floor_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.floor(x);
                    }
                });

        lookup.put("if_1",
                new Op1Arg() {
                    public double call(double x) {
                        return !dzero(x) ? 1d : 0d;
                    }
                });

        lookup.put("if_2",
                new Op2Arg() {
                    public double call(double x, double a) {
                        return !dzero(x) ? a : 0d;
                    }
                });

        lookup.put("if_3",
                new Op3Arg() {
                    public double call(double x, double a, double b) {
                        return !dzero(x) ? a : b;
                    }
                });

        lookup.put("if_4",
                new Op4Arg() {
                    public double call(double x, double a, double b, double c) {
                        return dzero(x) ? b : (x > 0 ? a : c);
                    }
                });

        lookup.put("isinf_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Double.isInfinite(x) ? 1d : 0d;
                    }
                });

        lookup.put("isnan_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Double.isNaN(x) ? 1d : 0d;
                    }
                });

        lookup.put("isnull_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Double.isNaN(x) ? 1d : 0d;
                    }
                });

        lookup.put("log_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.log(x);
                    }
                });
                
        lookup.put("log_2",
                new Op2Arg() {
                    public double call(double x, double y) {
                        return Math.log(x) / Math.log(y);
                    }
                });
                
        lookup.put("max_v",
                new OpVarArgs() {
                    public double call(Double ...values) {
                        return SampleStats.max(values, true);
                    }
                });

        lookup.put("mean_v",
                new OpVarArgs() {
                    public double call(Double ...values) {
                        return SampleStats.mean(values, true);
                    }
        });
        
        lookup.put("median_v",
                new OpVarArgs() {
                    public double call(Double ...values) {
                        return SampleStats.median(values, true);
                    }
                });
                
        lookup.put("min_v",
                new OpVarArgs() {
                    public double call(Double ...values) {
                        return SampleStats.min(values, true);
                    }
                });
                
        lookup.put("mode_v",
                new OpVarArgs() {
                    public double call(Double ...values) {
                        return SampleStats.mode(values, true);
                    }
                });
                
        lookup.put("null_0",
                new OpNoArg() {
                    public double call() {
                        return Double.NaN;
                    }
                });
                
        lookup.put("radToDeg_1",
                new Op1Arg() {
                    public double call(double x) {
                        return x / Math.PI * 180d;
                    }
                });

        lookup.put("rand_1",
                new Op1Arg() {
                    public double call(double x) {
                        return rr.nextDouble() * x;
                    }
                });

        lookup.put("randInt_1",
                new Op1Arg() {
                    public double call(double x) {
                        return rr.nextInt((int) x);
                    }
                });

        lookup.put("range_v",
                new OpVarArgs() {
                    public double call(Double ...values) {
                        return SampleStats.range(values, true);
                    }
                });
                
        lookup.put("round_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.round(x);
                    }
                });
                
        lookup.put("round_2",
                new Op2Arg() {
                    public double call(double x, double fac) {
                        int ifac = (int)(fac + 0.5);
                        return Math.round(x / ifac) * ifac;
                    }
                });

        lookup.put("sdev_v",
                new OpVarArgs() {
                    public double call(Double ...values) {
                        return SampleStats.sdev(values, true);
                    }
                });

        lookup.put("sin_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.sin(x);
                    }
                });

        lookup.put("sqrt_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.sqrt(x);
                    }
                });

        lookup.put("tan_1",
                new Op1Arg() {
                    public double call(double x) {
                        return Math.tan(x);
                    }
                });

        lookup.put("variance_v",
                new OpVarArgs() {
                    public double call(Double ...values) {
                        return SampleStats.variance(values, true);
                    }
                });

    }

    private static String[] volatileFuncs = {
        "rand", "randInt"
    };

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
        for (String vf : volatileFuncs) {
            if (vf.equals(name)) return true;
        }

        return false;
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
        op = lookup.get(name + "_v");
        
        if (op == null) {
            // check for a match with fixed arg functions
            return lookup.get(name + "_" + numArgs);
        }
        
        return op;
    }
}
