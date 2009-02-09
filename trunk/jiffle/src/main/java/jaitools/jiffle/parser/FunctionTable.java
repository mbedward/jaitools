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

package jaitools.jiffle.parser;

import jaitools.jiffle.collection.CollectionFactory;
import jaitools.jiffle.interpreter.Foo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    
    private static Map<String, Type> typeLookup;
    
    static {
        typeLookup = CollectionFactory.newMap();
        
        typeLookup.put("col", Type.POSITIONAL);
        typeLookup.put("row", Type.POSITIONAL);
        typeLookup.put("x", Type.POSITIONAL);
        typeLookup.put("y", Type.POSITIONAL);
        
        typeLookup.put("bands", Type.IMAGE_INFO);
        typeLookup.put("height", Type.IMAGE_INFO);
        typeLookup.put("width", Type.IMAGE_INFO);
        
        typeLookup.put("abs",  Type.GENERAL);
        typeLookup.put("acos", Type.GENERAL);
        typeLookup.put("asin", Type.GENERAL);
        typeLookup.put("atan", Type.GENERAL);
        typeLookup.put("cos",  Type.GENERAL);
        typeLookup.put("degToRad", Type.GENERAL);
        typeLookup.put("log",  Type.GENERAL);
        typeLookup.put("rand", Type.GENERAL);
        typeLookup.put("radToDeg", Type.GENERAL);
        typeLookup.put("randInt", Type.GENERAL);
        typeLookup.put("sin",  Type.GENERAL);
        typeLookup.put("sqrt", Type.GENERAL);
        typeLookup.put("tan",  Type.GENERAL);
    }
    
    
    static class FunctionTableEntry {
        Type type;
        OpBase op;
        
        FunctionTableEntry(Type type, OpBase op) {this.type = type; this.op = op;}
    }
    
    private static Random rr = new Random();
    private static Map<String, FunctionTableEntry> generalFuncLookup = null;
    
    static {
        generalFuncLookup = new HashMap<String, FunctionTableEntry>();
        
        generalFuncLookup.put("log",
                new FunctionTableEntry(
                Type.POSITIONAL,
                new OpBase1() {
                    public double call(double x) {
                        return Math.log(x);
                    }
                }));

        generalFuncLookup.put("sqrt",
                new FunctionTableEntry(
                Type.POSITIONAL,
                new OpBase1() {

                    public double call(double x) {
                        return Math.sqrt(x);
                    }
                }));

        generalFuncLookup.put("abs",
                new FunctionTableEntry(
                Type.POSITIONAL,
                new OpBase1() {

                    public double call(double x) {
                        return Math.abs(x);
                    }
                }));

        generalFuncLookup.put("rand",
                new FunctionTableEntry(
                Type.POSITIONAL,
                new OpBase1() {
                    public double call(double x) {
                        return rr.nextDouble() * x;
                    }
                }));

        generalFuncLookup.put("randInt",
                new FunctionTableEntry(
                Type.POSITIONAL,
                new OpBase1() {
                    public double call(double x) {
                        return rr.nextInt((int) x);
                    }
                }));

        generalFuncLookup.put("sin",
                new FunctionTableEntry(
                Type.POSITIONAL,
                new OpBase1() {
                    public double call(double x) {
                        return Math.sin(x);
                    }
                }));

        generalFuncLookup.put("cos",
                new FunctionTableEntry(
                Type.POSITIONAL,
                new OpBase1() {
                    public double call(double x) {
                        return Math.cos(x);
                    }
                }));

        generalFuncLookup.put("tan",
                new FunctionTableEntry(
                Type.POSITIONAL,
                new OpBase1() {
                    public double call(double x) {
                        return Math.tan(x);
                    }
                }));

        generalFuncLookup.put("asin",
                new FunctionTableEntry(
                Type.POSITIONAL,
                new OpBase1() {
                    public double call(double x) {
                        return Math.asin(x);
                    }
                }));

        generalFuncLookup.put("acos",
                new FunctionTableEntry(
                Type.POSITIONAL,
                new OpBase1() {
                    public double call(double x) {
                        return Math.acos(x);
                    }
                }));

        generalFuncLookup.put("atan",
                new FunctionTableEntry(
                Type.POSITIONAL,
                new OpBase1() {
                    public double call(double x) {
                        return Math.atan(x);
                    }
                }));

        generalFuncLookup.put("degToRad",
                new FunctionTableEntry(
                Type.POSITIONAL,
                new OpBase1() {
                    public double call(double x) {
                        return Math.PI * x / 180d;
                    }
                }));

        generalFuncLookup.put("radToDeg",
                new FunctionTableEntry(
                Type.POSITIONAL,
                new OpBase1() {
                    public double call(double x) {
                        return x / Math.PI * 180d;
                    }
                }));

    }

    /*
     * @todo Foo is a holder for methods where I haven't decided
     * where they should live yet
     */
    private Foo foo;

    public static Type getFunctionType(String name) {
        return typeLookup.get(name);
    }

    /**
     * Constructor. Use this when position and image-info functions are
     * not required, e.g. direct evaluation of simple statements
     */
    public FunctionTable() {
        
    }
    
    /**
     * Constructor to be used when a jiffle script is being run by a 
     * JiffleInterpreter. Enables used of position and image info
     * functions.
     * 
     * @param foo ????
     */
    public FunctionTable(Foo foo) {
        this.foo = foo;
    }
    
    
    boolean isDefined(String name, int numArgs) {
        OpBase op = generalFuncLookup.get(name).op;
        if (op == null) {
            return false;
        }

        return op.getNumArgs() == numArgs;
    }
    
    double invoke(String name, List<Double> args) {
        OpBase op = null;
        Type t = typeLookup.get(name);
        if (t == Type.GENERAL) {
            op = generalFuncLookup.get(name).op;
        } else {
            // @todo WRITE ME !
            return 0;
        }
        
        switch (args.size()) {
            case 0:
                return ((OpBase0)op).call();
                
            case 1:
                return ((OpBase1)op).call(args.get(0));
                
            case 2:
                return ((OpBase2)op).call(args.get(0), args.get(1));
                
            default:
                throw new IllegalStateException(
                        "unsupported function: " + name + " with " + args.size() + " args");
        }
    }

}
