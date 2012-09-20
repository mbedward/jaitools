/* 
 *  Copyright (c) 2009-2011, Michael Bedward. All rights reserved. 
 *   
 *  Redistribution and use in source and binary forms, with or without modification, 
 *  are permitted provided that the following conditions are met: 
 *   
 *  - Redistributions of source code must retain the above copyright notice, this  
 *    list of conditions and the following disclaimer. 
 *   
 *  - Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.   
 *   
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */   

package org.jaitools.numeric;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides static methods to work directly with {@code Number} objects 
 * without having to cast them to narrower types.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class NumberOperations {

    /** Default tolerance for float comparisons. */
    public static final float DEFAULT_FLOAT_TOL = 1.0e-4f;

    private static float floatTol = DEFAULT_FLOAT_TOL;

    /** Default tolerance for double comparisons */
    public static final double DEFAULT_DOUBLE_TOL = 1.0e-8f;

    private static double doubleTol = DEFAULT_DOUBLE_TOL;

    /**
     * Information about the {@code Number} classes supported including
     * their rank in terms of numeric precision.
     */
    public enum ClassInfo {
        /** Byte: rank 0, integral type */
        BYTE(0, Byte.class, true),
        /** Short: rank 1, integral type */
        SHORT(1, Short.class, true),
        /** Integer: rank 2, integral type */
        INTEGER(2, Integer.class, true),
        /** Long: rank 3, integral type */
        LONG(3, Long.class, true),
        /** Float: rank 4, non-integral type */
        FLOAT(4, Float.class, false),
        /** Double: rank 5, non-integral type */
        DOUBLE(5, Double.class, false);

        /** Map for reverse lookup by class */
        private static final Map<Class<? extends Number>, ClassInfo> lookup;
        static {
            lookup = new HashMap<Class<? extends Number>, ClassInfo>();
            for (ClassInfo cr : ClassInfo.values()) {
                lookup.put(cr.clazz, cr);
            }
        }

        private int rank;
        private Class<? extends Number> clazz;
        private boolean isIntegral;

        /** Private constructor */
        private ClassInfo(int rank, Class<? extends Number> clazz, boolean isIntegral) {
            this.rank = rank;
            this.clazz = clazz;
            this.isIntegral = isIntegral;
        }

        /**
         * Gets the relative rank of this type in terms of numeric precision.
         *
         * @return int value for rank
         */
        public int getRank() {
            return rank;
        }

        /**
         * Gets the class that this type relates to
         *
         * @return the class
         */
        public Class<? extends Number> getNumberClass() {
            return clazz;
        }

        /**
         * Tests if this is an integral type
         *
         * @return true if integral; false otherwise
         */
        public boolean isIntegral() {
            return isIntegral;
        }

        /**
         * Gets a String representation of this type
         *
         * @return a String of the form "{@code ClassInfo<classname>}"
         */
        @Override
        public String toString() {
            return String.format("ClassInfo<%s>", clazz.getSimpleName());
        }

        /**
         * Gets the ClassInfo type for the given class
         *
         * @param clazz a Number class
         *
         * @return the ClassInfo type or {@code null} if the class is not
         *         recognized
         */
        public static ClassInfo get(Class<? extends Number> clazz) {
            return lookup.get(clazz);
        }
    }

    /** Supported operations. */
    private enum OpType {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE,
        COMPARE;
    }

    
    /**
     * Adds two {@code Number} objects. The return value will be an instance
     * of the highest ranking class of the two arguments (e.g. Byte + Integer
     * will give an Integer result)
     *
     * @param n1 first value
     * @param n2 second value
     *
     * @return the result as a new instance of the highest ranking argument class
     */
    public static Number add(Number n1, Number n2) {
        return calculate(OpType.ADD, n1, n2);
    }

    /**
     * Calculates {@code n2 - n1}. The return value will be an instance
     * of the highest ranking class of the two arguments (e.g. Byte - Integer
     * will give an Integer result)
     *
     * @param n1 first value
     * @param n2 second value
     *
     * @return the result as a new instance of the highest ranking argument class
     */
    public static Number subtract(Number n1, Number n2) {
        return calculate(OpType.SUBTRACT, n1, n2);
    }

    /**
     * Calculates {@code n2 * n1}. The return value will be an instance
     * of the highest ranking class of the two arguments (e.g. Byte * Integer
     * will give an Integer result)
     *
     * @param n1 first value
     * @param n2 second value
     *
     * @return the result as a new instance of the highest ranking argument class
     */
    public static Number multiply(Number n1, Number n2) {
        return calculate(OpType.MULTIPLY, n1, n2);
    }

    /**
     * Calculates {@code n2 / n1}. The return value will be an instance
     * of the highest ranking class of the two arguments (e.g. Byte / Integer
     * will give an Integer result)
     *
     * @param n1 first value
     * @param n2 second value
     *
     * @return the result as a new instance of the highest ranking argument class
     */
    public static Number divide(Number n1, Number n2) {
        return calculate(OpType.DIVIDE, n1, n2);
    }

    /**
     * Compares value {@code n1} to value {@code n2}. If one or both of the values are
     * Float or Double the comparison is done within the currently set float or double
     * tolerance.
     *
     * @param n1 the first value
     * @param n2 the second value
     *
     * @return -1 if the first value is less than the second; 1 if the first value
     *         is greater than the second; 0 if the two values are equal.
     *
     * @see #getFloatTolerance()
     * @see #setFloatTolerance(float)
     */
    public static int compare(Number n1, Number n2) {
        return (int)Math.round(calculate(OpType.COMPARE, n1, n2).doubleValue());
    }

    /**
     * Gets the current tolerance used for Float comparisons.
     *
     * @return the current tolerance
     */
    public static float getFloatTolerance() {
        return floatTol;
    }

    /**
     * Sets the tolerance used for Float comparisons.
     *
     * @param tol a small positive value
     */
    public static void setFloatTolerance(float tol) {
        floatTol = Math.abs(tol);
    }

    /**
     * Gets the current tolerance used for Double comparisons.
     *
     * @return the current tolerance
     */
    public static double getDoubleTolerance() {
        return doubleTol;
    }

    /**
     * Sets the tolerance used for Double comparisons.
     *
     * @param tol a small positive value
     */
    public static void setFloatTolerance(double tol) {
        doubleTol = Math.abs(tol);
    }

    /**
     * Helper method for the individual operations methods: add, subtract etc.
     *
     * @param type calculation type
     * @param n1 the first value
     * @param n2 the second value
     *
     * @return the result as a new instance of the highest ranking argument class
     */
    private static Number calculate(OpType type, Number n1, Number n2) {
        Number result = null;
        ClassInfo ciIn = ClassInfo.get(highestClass(n1, n2));
        ClassInfo ciOut = null; 
        if (type == OpType.COMPARE) {
            ciOut = ClassInfo.INTEGER;
        } else {
            ciOut = ciIn;
        }

        switch (ciIn) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
                result = integralCalculation(type, n1, n2);
                break;

            case FLOAT:
                result = floatCalculation(type, n1, n2);
                break;

            case DOUBLE:
                result = doubleCalculation(type, n1, n2);
                break;

            default:
                throw new UnsupportedOperationException("Unrecognized number class");
        }

        return newInstance(result, ciOut);
    }

    /**
     * Safely convert the argument to an {@code int} value. The following conventions apply:
     * <ul>
     * <li> Byte values are handled as unsigned (e.g. range 0 - 255)
     * <li> Float or Double NaN arguments are converted to 0
     * <li> Float or Double POSITIVE_INFINITY is converted to Integer.MAX_VALUE
     * <li> Float or Double NEGATIVE_INFINITY is converted to Integer.MIN_VALUE
     * </ul>
     * 
     * @param number the object to convert
     *
     * @return the object value as an {@code int}
     */
    public static int intValue(Number number) {
        ClassInfo ci = ClassInfo.get(number.getClass());
        int value = 0;

        switch (ci) {
            case BYTE:
                value = number.intValue() & 0xff;
                break;

            case SHORT:
            case INTEGER:
                value = number.intValue();
                break;

            case LONG:
                value = (int) Math.min(Math.max(number.longValue(), Integer.MIN_VALUE), Integer.MAX_VALUE);
                break;

            case FLOAT:
                Float f = (Float) number;
                if (f.isNaN()) {
                    value = 0;
                } else if (f.isInfinite()) {
                    value = f.equals(Float.NEGATIVE_INFINITY) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                } else {
                    value = (int) Math.max( Math.min(number.floatValue(), Integer.MAX_VALUE), Integer.MIN_VALUE );
                }
                break;

            case DOUBLE:
                Double d = (Double) number;
                if (d.isNaN()) {
                    value = 0;
                } else if (d.isInfinite()) {
                    value = d.equals(Double.NEGATIVE_INFINITY) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                } else {
                    value = (int) Math.max( Math.min(number.doubleValue(), Integer.MAX_VALUE), Integer.MIN_VALUE );
                }
                break;

            default:
                throw new IllegalStateException("Unrecognized number class: " + number.getClass().getName());
        }

        return value;
    }

    /**
     * Safely convert the argument to a {@code long} value. The following conventions apply:
     * <ul>
     * <li> Byte values are handled as unsigned (e.g. range 0 - 255)
     * <li> Float or Double NaN arguments are converted to 0
     * <li> Float or Double POSITIVE_INFINITY is converted to Long.MAX_VALUE
     * <li> Float or Double NEGATIVE_INFINITY is converted to Long.MIN_VALUE
     * </ul>
     *
     * @param number the object to convert
     *
     * @return the object value as a {@code long}
     */
    public static long longValue(Number number) {
        ClassInfo ci = ClassInfo.get(number.getClass());
        long value = 0;

        switch (ci) {
            case BYTE:
                value = number.intValue() & 0xff;
                break;

            case SHORT:
            case INTEGER:
            case LONG:
                value = number.longValue();
                break;

            case FLOAT:
                Float f = (Float) number;
                if (f.isNaN()) {
                    value = 0;
                } else if (f.isInfinite()) {
                    value = f.equals(Float.NEGATIVE_INFINITY) ? Long.MIN_VALUE : Long.MAX_VALUE;
                } else {
                    value = (long) Math.max( Math.min(number.floatValue(), Long.MAX_VALUE), Long.MIN_VALUE );
                }
                break;

            case DOUBLE:
                Double d = (Double) number;
                if (d.isNaN()) {
                    value = 0;
                } else if (d.isInfinite()) {
                    value = d.equals(Double.NEGATIVE_INFINITY) ? Long.MIN_VALUE : Long.MAX_VALUE;
                } else {
                    value = (long) Math.max( Math.min(number.doubleValue(), Long.MAX_VALUE), Long.MIN_VALUE );
                }
                break;

            default:
                throw new IllegalStateException("Unrecognized number class: " + number.getClass().getName());
        }

        return value;
    }

    /**
     * Safely convert the argument to a {@code float} value. The following conventions apply:
     * <ul>
     * <li> Byte values are handled as unsigned (e.g. range 0 - 255)
     * <li> Double.NaN is converted to Float.NaN
     * <li> Double POSITIVE_INFINITY is converted to Float.POSITIVE_INFINITY
     * <li> Double NEGATIVE_INFINITY is converted to Float.NEGATIVE_INFINITY
     * </ul>
     *
     * @param number the object to convert
     *
     * @return the object value as a {@code float}
     */
    public static float floatValue(Number number) {
        ClassInfo ci = ClassInfo.get(number.getClass());
        float value = 0.0F;

        switch (ci) {
            case BYTE:
                value = number.intValue() & 0xff;
                break;

            case SHORT:
            case INTEGER:
            case LONG:
            case FLOAT:
                value = number.floatValue();
                break;

            case DOUBLE:
                Double d = (Double) number;
                if (d.isNaN()) {
                    value = Float.NaN;
                } else if (d.isInfinite()) {
                    value = d.equals(Double.NEGATIVE_INFINITY) ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;

                } else {
                    value = (float) Math.max( Math.min(number.doubleValue(), Float.POSITIVE_INFINITY), Float.NEGATIVE_INFINITY );
                }
                break;

            default:
                throw new IllegalStateException("Unrecognized number class: " + number.getClass().getName());
        }

        return value;
    }

    /**
     * Safely convert the argument to a {@code double} value. The only difference
     * between this method and {@code Number.doubleValue()} is that Byte values
     * are handled as unsigned (e.g. range 0 - 255).
     *
     * @param number the object to convert
     *
     * @return the object value as a {@code double}
     */
    public static double doubleValue(Number number) {
        ClassInfo ci = ClassInfo.get(number.getClass());
        switch (ci) {
            case BYTE:
                return (double) (number.intValue() & 0xff);

            default:
                return number.doubleValue();
        }
    }
    
    /**
     * Copies the given {@code Number} object. This is a short-cut for 
     * {@code NumberOperations.newInstance(number, number.getClass())}.
     * Returns {@code null} if the input is {@code null}.
     * 
     * @param number the number to copy
     * @return a new instance with the same class and value as {@code number};
     *         or {@code null}
     */
    public static Number copy(Number number) {
        return (number == null ? null : newInstance(number, number.getClass()));
    }
    
    /**
     * Return a new instance of class {@code clazz} taking its value from {@code number}
     *
     * @param number the number object whose value will be copied
     * @param clazz the class of the new instance
     *
     * @return a new instance of the request class with value {@code number}
     */
    public static Number newInstance(Number number, Class<? extends Number> clazz) {
        ClassInfo ci = ClassInfo.get(clazz);
        return newInstance(number, ci, false);
    }
    
    /**
     * Cast the value from {@code number} to the class {@code clazz}  
     *
     * @param number the number object whose value will be copied
     * @param clazz the class of the new instance
     *
     * @return a new instance of the request class with value {@code number}
     */
    public static Number castNumber(Number number, Class<? extends Number> clazz) {
        ClassInfo ci = ClassInfo.get(clazz);
        return newInstance(number, ci, true);
    }
    
    /**
     * Return a new instance of the class described by {@code ci} taking its value 
     * from {@code number}
     *
     * @param number the number object whose value will be copied
     * @param ci a ClassInfo object
     *
     * @return a new instance of the request class with value {@code number}
     */
    private static Number newInstance(Number number, ClassInfo ci) {
        return newInstance(number, ci, false);
    }
    
    /**
     * Return an instance of the class described by {@code ci} taking its value 
     * from {@code number}. If {@code canReturnInput} is set to true, avoid 
     * creating a new object when the input object conforms to the class info.
     *
     * @param number the number object whose value will be copied
     * @param ci a ClassInfo object
     * @param canReturnInput avoid creating a new object if we can just return
     *     the input object
     *
     * @return a (possibly) new instance of the request class with value {@code number}
     */
    private static Number newInstance(Number number, ClassInfo ci, boolean canReturnInput) {
        final Class<? extends Number> numberClass = number.getClass();
        switch (ci) {
            case BYTE:
                if (number.getClass() == Byte.class) {
                    return canReturnInput ? number : Byte.valueOf(number.byteValue());
                }
                return newByte(number);

            case SHORT:
                if (numberClass == Short.class) {
                    return canReturnInput ? number : Short.valueOf(number.shortValue());
                }
                return newShort(number);

            case INTEGER:
                if (numberClass == Integer.class) {
                    return canReturnInput ? number : Integer.valueOf(number.intValue());
                }
                return newInteger(number);

            case LONG:
                if (numberClass == Long.class) {
                    return canReturnInput ? number : Long.valueOf(number.longValue());
                }
                return newLong(number);

            case FLOAT:
                if (numberClass == Float.class) {
                    return canReturnInput ? number : Float.valueOf(number.floatValue());
                }
                return newFloat(number);

            case DOUBLE:
                if (numberClass == Double.class) {
                    return canReturnInput ? number : Double.valueOf(number.doubleValue());
                }
                return newDouble(number);

            default:
                throw new IllegalArgumentException("Unrecognized ClassInfo arg: " + ci);
        }

    }

    /**
     * Determine the highest ranking class (in terms of numeric precision)
     * among the arguments
     *
     * @param numbers {@code Number} objects to compare
     *
     * @return the highest ranking class, or {@code null} if no arguments were supplied
     */
    public static Class<? extends Number> highestClass(Number ...numbers) {
        int maxRank = -1;
        Class<? extends Number> result = null;

        for (Number n : numbers) {
            int rank = ClassInfo.get(n.getClass()).getRank();
            if (rank > maxRank) {
                maxRank = rank;
                result = n.getClass();
            }
        }

        return result;
    }

    /**
     * Determine the lowest ranking class (in terms of numeric precision)
     * among the arguments
     *
     * @param numbers {@code Number} objects to compare
     *
     * @return the lowest ranking class, or {@code null} if no arguments were supplied
     */
    public static Class<? extends Number> lowestClass(Number ...numbers) {
        int minRank = ClassInfo.DOUBLE.getRank() + 1;
        Class<? extends Number> result = null;

        for (Number n : numbers) {
            int rank = ClassInfo.get(n.getClass()).getRank();
            if (rank < minRank) {
                minRank = rank;
                result = n.getClass();
            }
        }

        return result;
    }

    /**
     * Perform a calculation on two integral values. The calculations are done
     * with {@code long} values.
     *
     * @param type the type of calculation
     * @param n1 first value
     * @param n2 second value
     *
     * @return result as a new instance of {@code Long}
     */
    private static Long integralCalculation(OpType type, Number n1, Number n2) {
        long result = 0;

        long val1 = longValue(n1);
        long val2 = longValue(n2);

        switch (type) {
            case ADD:
                result = val1 + val2;
                break;

            case SUBTRACT:
                result = val1 - val2;
                break;

            case MULTIPLY:
                result = val1 * val2;
                break;

            case DIVIDE:
                result = val1 / val2;
                break;
                
            case COMPARE:
                result = (val1 < val2 ? -1 : (val1 > val2 ? 1 : 0));
                break;
                
            default:
                throw new IllegalArgumentException("Invalid OpType: " + type);
        }

        return Long.valueOf(result);
    }

    /**
     * Perform a calculation on two values using {@code float} precision.
     *
     * @param type the type of calculation
     * @param n1 first value
     * @param n2 second value
     *
     * @return result as a new instance of {@code Float}
     */
    private static Float floatCalculation(OpType opType, Number n1, Number n2) {
        float result = 0;

        if (opType == OpType.COMPARE) {
            return (float) floatComparison(n1, n2);
        }

        if (n1 instanceof Float) {
            Float fn1 = (Float) n1;
            if (fn1.isInfinite() || fn1.isNaN()) {
                return Float.valueOf(Float.NaN);
            }
        }

        if (n2 instanceof Float) {
            Float fn2 = (Float) n1;
            if (fn2.isInfinite() || fn2.isNaN()) {
                return Float.valueOf(Float.NaN);
            }
        }

        float val1 = floatValue(n1);
        float val2 = floatValue(n2);

        switch (opType) {
            case ADD:
                result = val1 + val2;
                break;

            case SUBTRACT:
                result = val1 - val2;
                break;

            case MULTIPLY:
                result = val1 * val2;
                break;

            case DIVIDE:
                result = val1 / val2;
                break;
        }

        return Float.valueOf(result);
    }

    /**
     * Compare two values as floats using the currently set tolerance
     *
     * @param n1 first value
     * @param n2 second value
     *
     * @return -1 if the first value is less than the second; 1 if the
     *         first value is greater than the second; 0 if the two values
     *         are equal
     */
    private static int floatComparison(Number n1, Number n2) {
        float val1 = floatValue(n1);
        float val2 = floatValue(n2);

        if (Float.isInfinite(val1) || Float.isNaN(val1) ||
            Float.isInfinite(val2) || Float.isNaN(val2)) {
            return Float.compare(val1, val2);
        }

        int result;
        if (Math.abs(val1 - val2) < floatTol) {
            result = 0;
        } else {
            result = (val1 < val2 ? -1 : 1);
        }

        return result;
    }

    /**
     * Perform a calculation on two values using double precision.
     *
     * @param type the type of calculation
     * @param n1 first value
     * @param n2 second value
     *
     * @return result as a new instance of {@code Double}
     */
    private static Double doubleCalculation(OpType opType, Number n1, Number n2) {
        double result = 0;

        if (opType == OpType.COMPARE) {
            return (double) doubleComparison(n1, n2);
        }

        if (n1 instanceof Double) {
            Double dn1 = (Double) n1;
            if (dn1.isInfinite() || dn1.isNaN()) {
                return Double.valueOf(Double.NaN);
            }
        }

        if (n2 instanceof Double) {
            Double dn2 = (Double) n1;
            if (dn2.isInfinite() || dn2.isNaN()) {
                return Double.valueOf(Double.NaN);
            }
        }

        double val1 = doubleValue(n1);
        double val2 = doubleValue(n2);

        switch (opType) {
            case ADD:
                result = val1 + val2;
                break;

            case SUBTRACT:
                result = val1 - val2;
                break;

            case MULTIPLY:
                result = val1 * val2;
                break;

            case DIVIDE:
                result = val1 / val2;
                break;
        }

        return Double.valueOf(result);
    }

    /**
     * Compare two values as floats using the currently set tolerance
     *
     * @param n1 first value
     * @param n2 second value
     *
     * @return -1 if the first value is less than the second; 1 if the
     *         first value is greater than the second; 0 if the two values
     *         are equal
     */
    private static int doubleComparison(Number n1, Number n2) {
        double val1 = doubleValue(n1);
        double val2 = doubleValue(n2);

        if (Double.isInfinite(val1) || Double.isNaN(val1) ||
            Double.isInfinite(val2) || Double.isNaN(val2)) {
            return Double.compare(val1, val2);
        }

        int result;
        if (Math.abs(val1 - val2) < doubleTol) {
            result = 0;
        } else {
            result = (val1 < val2 ? -1 : 1);
        }

        return result;
    }

    /**
     * Creates a new Byte taking its value (unsigned and possibly clamped)
     * from {@code source}
     *
     * @param source the value to copy
     *
     * @return a new Byte object
     */
    private static Byte newByte(Number source) {
        long value = longValue(source);

        if (value < 0) {
            value = 0;
        } else if (value > 255) {
            value = 255;
        }

        return Byte.valueOf((byte) value);
    }

    /**
     * Creates a new Short taking its value (possibly clamped)
     * from {@code source}
     *
     * @param source the value to copy
     *
     * @return a new Short object
     */
    private static Short newShort(Number source) {
        long value = longValue(source);

        if (value < Short.MIN_VALUE) {
            value = Short.MIN_VALUE;
        } else if (value > Short.MAX_VALUE) {
            value = Short.MAX_VALUE;
        }

        return Short.valueOf((short) value);
    }

    /**
     * Creates a new Integer taking its value (possibly clamped)
     * from {@code source}
     *
     * @param source the value to copy
     *
     * @return a new Integer object
     */
    private static Integer newInteger(Number source) {
        long value = longValue(source);

        if (value < Integer.MIN_VALUE) {
            value = Integer.MIN_VALUE;
        } else if (value > Integer.MAX_VALUE) {
            value = Integer.MAX_VALUE;
        }

        return Integer.valueOf((int) value);
    }

    /**
     * Creates a new Long taking its value
     * from {@code source}
     *
     * @param source the value to copy
     *
     * @return a new Long object
     */
    private static Long newLong(Number source) {
        long value = longValue(source);
        return Long.valueOf((int) value);
    }

    /**
     * Creates a new Float taking its value (possibly clamped)
     * from {@code source}
     *
     * @param source the value to copy
     *
     * @return a new Float object
     */
    private static Float newFloat(Number source) {
        double value = doubleValue(source);
        if (value < Float.MIN_VALUE) {
            value = Float.MIN_VALUE;
        } else if (value > Float.MAX_VALUE) {
            value = Float.MAX_VALUE;
        }
        
        return new Float((float) value);
    }

    /**
     * Creates a new Double taking its value from {@code source}
     *
     * @param source the value to copy
     *
     * @return a new Double object
     */
    private static Double newDouble(Number source) {
        double value = doubleValue(source);
        return Double.valueOf(value);
    }

}
