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

import jaitools.jiffle.util.CollectionFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * A symbol table for user-defined variables and pre-defined, named constants
 * (PI, E, NaN).
 * 
 * @author Michael Bedward
 */
public class VarTable {

    private static final int ASSIGN = 1;
    private static final int PLUS_EQ = 2;
    private static final int MINUS_EQ = 3;
    private static final int TIMES_EQ = 4;
    private static final int DIVIDE_EQ = 5;
    private static final int MOD_EQ = 6;

    private static Map<String, Integer> ops;
    static {
        ops = CollectionFactory.newTreeMap();

        ops.put("=", ASSIGN);
        ops.put("+=", PLUS_EQ);
        ops.put("-=", MINUS_EQ);
        ops.put("*=", TIMES_EQ);
        ops.put("/=", DIVIDE_EQ);
        ops.put("%=", MOD_EQ);
    }

    
    public enum Type {
        /**
         * A named constant such as PI
         */
        NAMED_CONSTANT,
        
        /**
         * A user-defined variable
         */
        USER;
    }
    
    private static Map<String, Double> constants;
    static {
        constants = CollectionFactory.newMap();
        constants.put("PI", Math.PI);
        constants.put("E", Math.E);
        constants.put("NaN", Double.NaN);
    }
    
    public static boolean isConstant(String varName) {
        return constants.containsKey(varName);
    }
    
    private HashMap<String, Number> lookup = null;
    
    public VarTable() {
        initLookup();
    }

    /**
     * Initialize the lookup map
     */
    private void initLookup() {
        lookup = new HashMap<String, Number>();
        lookup.putAll(constants);
    }

    /**
     * Assign a value to a variable
     * @param id the variable id
     * @param op operator symbol as a string
     * @param x value to assign
     * @throws IllegalStateExeption if an arithmetic assignment is applied to 
     * a variable that is not yet defined
     */
    public void assign(String id, String op, double x) {
        if (isConstant(id)) {
            throw new RuntimeException("Trying to assign a value to named constant: " + id);
        }
        
        Integer opCode = ops.get(op);
        
        if (opCode == ASSIGN) {
            lookup.put(id, x);
            return;
        }
        
        Number stored = lookup.get(id);
        if (stored != null) {
            switch (opCode) {
                case TIMES_EQ:
                    lookup.put(id, stored.doubleValue() * x);
                    break;
                    
                case DIVIDE_EQ:
                    lookup.put(id, stored.doubleValue() / x);
                    break;

                case PLUS_EQ:
                    lookup.put(id, stored.doubleValue() + x);
                    break;
                    
                case MINUS_EQ:
                    lookup.put(id, stored.doubleValue() - x);
                    break;
                    
                case MOD_EQ:
                    lookup.put(id, stored.doubleValue() % x);
                    break;
            }
            
        } else {
            throw new IllegalStateException("using undefined var " + id + " with " + op);
        }
    }

    /**
     * Assign a value to a variable
     * @param id variable name
     * @param x value to assign
     */
    public void set(String id, double x) {
        if (isConstant(id)) {
            throw new RuntimeException("Trying to assign a value to named constant: " + id);
        }

        lookup.put(id, x);
        return;
    }

    /**
     * Get the value for a variable
     * @param id variable name
     * @return value as double
     * @throws IllegalArgumentException if the variable is not defined
     */
    public double get(String id) {
        Number n = lookup.get(id);
        if (n == null) {
            throw new IllegalArgumentException();
        }

        return n.doubleValue();
    }

    /**
     * Check if a variable is present in the table
     * @param id name of the variable
     */
    public boolean contains(String id) {
        return lookup.containsKey(id);
    }
    
}
