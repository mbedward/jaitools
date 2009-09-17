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

package jaitools.numeric;

/**
 * Double value comparisons within a set tolerance
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class DoubleComparison {

    /** Default tolerance for comparisons: 1.0e-8 */
    public static final double TOL = 1.0e-8;

    /**
     * Check if the given value is 0 within the default tolerance
     * @param x the value
     * @return true if zero; false otherwise
     */
    public static boolean dzero(double x) {
        return Math.abs(x) < TOL;
    }

    /**
     * Check if the given value is 0 within the user-specified tolerance
     * @param x the value
     * @param tol the user-specified tolerance (<b>assumed</b> to be positive)
     * @return true if zero; false otherwise
     */
    public static boolean dzero(double x, double tol) {
        if (tol < TOL) {
            return dzero(x);
        }

        return Math.abs(x) < tol;
    }

    /**
     * Compare two values using the default tolerance
     * @param x1 first value
     * @param x2 second value
     * @return a value less than 0 if x1 is less than x2; 0 if x1 is equal to x2;
     * a value greater than 0 if x1 is greater than x2
     */
    public static int dcomp(double x1, double x2) {
        if (dzero(x1 - x2)) {
            return 0;
        } else {
            return Double.compare(x1, x2);
        }
    }

    /**
     * Compare two values using the user-specified tolerance
     * @param x1 first value
     * @param x2 second value
     * @param the user-specified tolerance (<b>assumed</b> to be positive)
     * @return a value less than 0 if x1 is less than x2; 0 if x1 is equal to x2;
     * a value greater than 0 if x1 is greater than x2
     */
    public static int dcomp(double x1, double x2, double tol) {
        if (tol < TOL) {
            return dcomp(x1, x2);
        }

        if (dzero(x1 - x2, tol)) {
            return 0;
        } else {
            return Double.compare(x1, x2);
        }
    }

    /**
     * Test if two double values are equal within the default tolerance.
     * This is a short-cut for {@code dzero(x1 - x2)}.
     * @param x1 first value
     * @param x2 second value
     * @return true if the two values are equal; false otherwise
     */
    public static boolean dequal(double x1, double x2) {
        return dzero(x1 - x2);
    }

    /**
     * Test if two double values are equal within the specified tolerance.
     * This is a short-cut for {@code dzero(x1 - x2, tol)}.
     * @param x1 first value
     * @param x2 second value
     * @param the user-specified tolerance (<b>assumed</b> to be positive)
     * @return true if the two values are equal; false otherwise
     */
    public static boolean dequal(double x1, double x2, double tol) {
        return dzero(x1 - x2, tol);
    }

    /**
     * Convert a double value to integer taking into account the
     * default tolerance when checking if it is zero, ie. values
     * within TOL either side of 0 will produce a 0 return value
     *
     * @param x the value to convert
     * @return the equivalent integer value
     */
    public static int toInt(double x) {
        int sign = dcomp(x, 0d);
        if (sign > 0) { // +ve value
            return (int)(x + 0.5);
            
        } else if (sign < 0) { // -ve value
            return (int)(x - 0.5);
            
        } else {
            return 0;
        }
        
    }
}
