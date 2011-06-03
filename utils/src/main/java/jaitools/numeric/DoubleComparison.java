/* 
 *  Copyright (c) 2009, Michael Bedward. All rights reserved. 
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

package jaitools.numeric;

/**
 * Provides static method to compare double values with either a default or
 * specified tolerance.
 * 
 * @deprecated This class will be removed in JAI-tools version 1.2. Please
 *             use {@linkplain CompareOp} instead.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class DoubleComparison {

    /** Default tolerance for comparisons: 1.0e-8 */
    public static final double TOL = 1.0e-8;

    /**
     * Tests if the given value is 0 within the default tolerance.
     * @param x the value
     * @return {@code true} if zero; {@code false} otherwise
     */
    public static boolean dzero(double x) {
        return Math.abs(x) < TOL;
    }

    /**
     * Tests if the given value is 0 within the user-specified tolerance.
     * @param x the value
     * @param tol the user-specified tolerance (<b>assumed</b> to be positive)
     * @return {@code true} if zero; {@code false} otherwise
     */
    public static boolean dzero(double x, double tol) {
        if (tol < TOL) {
            return dzero(x);
        }

        return Math.abs(x) < tol;
    }

    /**
     * Compares two values using the default tolerance.
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
     * Compares two values using the user-specified tolerance
     * @param x1 first value
     * @param x2 second value
     * @param tol the tolerance
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
     * Tests if two double values are equal within the default tolerance.
     * This is a short-cut for {@code dzero(x1 - x2)}.
     * @param x1 first value
     * @param x2 second value
     * @return {@code true} if equal; {@code false} otherwise
     */
    public static boolean dequal(double x1, double x2) {
        return dzero(x1 - x2);
    }

    /**
     * Tests if two double values are equal within the specified tolerance.
     * This is a short-cut for {@code dzero(x1 - x2, tol)}.
     * @param x1 first value
     * @param x2 second value
     * @param tol the tolerance
     * @return {@code true} if equal; {@code false} otherwise
     */
    public static boolean dequal(double x1, double x2, double tol) {
        return dzero(x1 - x2, tol);
    }

    /**
     * Converts a double value to integer taking into account the
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
