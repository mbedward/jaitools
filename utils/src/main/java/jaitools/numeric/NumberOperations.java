/*
 * Copyright 2009-2010 Michael Bedward
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

package jaitools.numeric;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides a number of static helper methods for working with {@code Number}
 * objects without having to cast them to narrower types.
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class NumberOperations {

    /**
     * Information about the {@code Number} classes supported including
     * their relative ranks in terms of numeric precision
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
         * Get the relative rank of this type in terms of numeric precision.
         *
         * @return int value for rank
         */
        public int getRank() {
            return rank;
        }

        /**
         * Get the class that this type relates to
         *
         * @return the class
         */
        public Class<? extends Number> getNumberClass() {
            return clazz;
        }

        /**
         * Test if this is an integral type
         *
         * @return true if integral; false otherwise
         */
        public boolean isIntegral() {
            return isIntegral;
        }

        /**
         * Get a String representation of this type
         *
         * @return a String of the form "{@code ClassInfo<classname>}"
         */
        @Override
        public String toString() {
            return String.format("ClassInfo<%s>", clazz.getSimpleName());
        }

        /**
         * Get the ClassInfo type for the given class
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

    /**
     * Supported operations
     */
    private enum OpType {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE;
    }

    /**
     * Add two {@code Number} objects. The return value will be an instance
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
        ClassInfo ci = ClassInfo.get(highestClass(n1, n2));

        switch (ci) {
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

        return newInstance(result, ci.getNumberClass());
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

            case DOUBLE:
                Double d = (Double) number;
                if (d.isNaN()) {
                    value = 0;
                } else if (d.isInfinite()) {
                    value = d.equals(Double.NEGATIVE_INFINITY) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                } else {
                    value = (int) Math.max( Math.min(number.doubleValue(), Integer.MAX_VALUE), Integer.MIN_VALUE );
                }

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

            case DOUBLE:
                Double d = (Double) number;
                if (d.isNaN()) {
                    value = 0;
                } else if (d.isInfinite()) {
                    value = d.equals(Double.NEGATIVE_INFINITY) ? Long.MIN_VALUE : Long.MAX_VALUE;
                } else {
                    value = (long) Math.max( Math.min(number.doubleValue(), Long.MAX_VALUE), Long.MIN_VALUE );
                }

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
     * Return a new instance of class {@code clazz} taking its value from {@code number}
     *
     * @param number the number object whose value will be copied
     * @param clazz the class of the new instance
     *
     * @return a new instance of class {@code clazz}
     */
    public static Number newInstance(Number number, Class<? extends Number> clazz) {
        ClassInfo ci = ClassInfo.get(clazz);

        switch (ci) {
            case BYTE:
                return newByte(number);

            case SHORT:
                return newShort(number);

            case INTEGER:
                return newInteger(number);

            case LONG:
                return newLong(number);

            case FLOAT:
                return newFloat(number);

            case DOUBLE:
                return newDouble(number);

            default:
                throw new UnsupportedOperationException("Unrecognized class: " + clazz.getName());
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
     * Creates a new Long taking its value (possibly clamped)
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
        float value = floatValue(source);
        return Float.valueOf(value);
    }

    /**
     * Creates a new Double taking its value (possibly clamped)
     * from {@code source}
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
