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
package jaitools.jiffle.interpreter;

import jaitools.jiffle.util.CollectionFactory;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static jaitools.jiffle.util.DoubleComparison.*;

/**
 * A symbol table for jiffle functions, pre-loaded with a basic set of 
 * mathematical functions.
 *  
 * @author Michael Bedward
 */
public class FunctionTable {

    /**
     * Type of function
     */
    public enum Type {

        /** 
         * General function such as sqrt, rand
         */
        GENERAL,
        /**
         * Positional function such as x(), y()
         */
        POSITIONAL,
        /**
         * Image info functions such as width()
         */
        IMAGE_INFO,
        /**
         * User-defined function (not supported at present)
         */
        USER;
    }
    private static Random rr = new Random();
    private static Map<String, OpBase> lookup = null;
    

    static {
        lookup = CollectionFactory.newTreeMap();

        lookup.put("abs_1",
                new OpBase1() {
                    public double call(double x) {
                        return Math.abs(x);
                    }
                });

        lookup.put("acos_1",
                new OpBase1() {
                    public double call(double x) {
                        return Math.acos(x);
                    }
                });

        lookup.put("asin_1",
                new OpBase1() {
                    public double call(double x) {
                        return Math.asin(x);
                    }
                });

        lookup.put("atan_1",
                new OpBase1() {
                    public double call(double x) {
                        return Math.atan(x);
                    }
                });

        lookup.put("cos_1",
                new OpBase1() {
                    public double call(double x) {
                        return Math.cos(x);
                    }
                });

        lookup.put("degToRad_1",
                new OpBase1() {
                    public double call(double x) {
                        return Math.PI * x / 180d;
                    }
                });

        lookup.put("if_1",
                new OpBase1() {
                    public double call(double x) {
                        return dzero(x) ? 1d : 0d;
                    }
                });

        lookup.put("if_2",
                new OpBase2() {
                    public double call(double x, double a) {
                        return dzero(x) ? a : 0d;
                    }
                });

        lookup.put("if_3",
                new OpBase3() {
                    public double call(double x, double a, double b) {
                        return dzero(x) ? a : b;
                    }
                });

        lookup.put("if_4",
                new OpBase4() {
                    public double call(double x, double a, double b, double c) {
                        return dzero(x) ? b : (x > 0 ? a : c);
                    }
                });

        lookup.put("isinf_1",
                new OpBase1() {
                    public double call(double x) {
                        return Double.isInfinite(x) ? 1d : 0d;
                    }
                });

        lookup.put("isnan_1",
                new OpBase1() {
                    public double call(double x) {
                        return Double.isNaN(x) ? 1d : 0d;
                    }
                });

        lookup.put("isnull_1",
                new OpBase1() {
                    public double call(double x) {
                        return Double.isNaN(x) ? 1d : 0d;
                    }
                });

        lookup.put("log_1",
                new OpBase1() {
                    public double call(double x) {
                        return Math.log(x);
                    }
                });
                
        lookup.put("log_2",
                new OpBase2() {
                    public double call(double x, double y) {
                        return Math.log(x) / Math.log(y);
                    }
                });
                
        lookup.put("null_0",
                new OpBase0() {
                    public double call() {
                        return Double.NaN;
                    }
                });

        lookup.put("radToDeg_1",
                new OpBase1() {
                    public double call(double x) {
                        return x / Math.PI * 180d;
                    }
                });

        lookup.put("rand_1",
                new OpBase1() {
                    public double call(double x) {
                        return rr.nextDouble() * x;
                    }
                });

        lookup.put("randInt_1",
                new OpBase1() {
                    public double call(double x) {
                        return rr.nextInt((int) x);
                    }
                });

        lookup.put("round_1",
                new OpBase1() {
                    public double call(double x) {
                        return Math.round(x);
                    }
                });
                
        lookup.put("round_2",
                new OpBase2() {
                    public double call(double x, double fac) {
                        int ifac = (int)(fac + 0.5);
                        return Math.round(x / ifac) * ifac;
                    }
                });

        lookup.put("sin_1",
                new OpBase1() {
                    public double call(double x) {
                        return Math.sin(x);
                    }
                });

        lookup.put("sqrt_1",
                new OpBase1() {
                    public double call(double x) {
                        return Math.sqrt(x);
                    }
                });

        lookup.put("tan_1",
                new OpBase1() {
                    public double call(double x) {
                        return Math.tan(x);
                    }
                });

    }

    /**
     * Constructor
     */
    public FunctionTable() {
    }

    public boolean isDefined(String name, int numArgs) {
        OpBase op = getMethod(name, numArgs);
        return op != null;
    }

    public double invoke(String name, List<Double> args) {
        OpBase op = getMethod(name, args.size());
        if (op == null) {
            throw new RuntimeException("unknown function: " + name + 
                    "with " + args.size() + " args");
        }

        switch (args.size()) {
            case 0:
                return ((OpBase0) op).call();

            case 1:
                return ((OpBase1) op).call(args.get(0));

            case 2:
                return ((OpBase2) op).call(args.get(0), args.get(1));
                
            case 3:
                return ((OpBase3) op).call(args.get(0), args.get(1), args.get(2));

            case 4:
                return ((OpBase4) op).call(args.get(0), args.get(1), args.get(2), args.get(3));

            default:
                throw new IllegalStateException(
                        "unsupported function: " + name + " with " + args.size() + " args");
        }
    }
    
    private OpBase getMethod(String name, int numArgs) {
        return lookup.get(name + "_" + numArgs);
    }
}
