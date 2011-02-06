/*
 * Copyright 2011 Michael Bedward
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

package jaitools.jiffle.runtime;

import java.util.Random;

import static jaitools.numeric.DoubleComparison.dcomp;
import static jaitools.numeric.DoubleComparison.dzero;
import jaitools.numeric.SampleStats;

/**
 * Provides static functions than can be called by Jiffle runtime objects.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class JiffleFunctions {
    
    private static Random rr;
    
    /**
     * Converts an angle in degrees to radians.
     * 
     * @param x input angle in degrees
     * @return angle in radians
     */
    public static double degToRad(double x) {
        return Math.PI * x / 180d;
    }

    /**
     * Return the sign of {@code x} as an integer. This method is used 
     * by Jiffle to implement its various {@code if} functions.
     * <p>
     * 
     * @param x test value
     * 
     * @return -1 if x is negative; 0 if x is 0; 1 if x is positive; 
     *         or {@code null} if x is NaN
     */
    public static Integer sign(double x) {
        if (!Double.isNaN(x)) {
            return dcomp(x, 0);
        }
        return null;
    }
    
    /**
     * Tests if x is infinite (equal to Double.POSITIVE_INFINITY or 
     * Double.NEGATIVE_INFINITY).
     * 
     * @param x test value
     * @return 1 if x is infinite; 0 if finite; or {@code Double.Nan}
     *         if x is {@code Double.Nan}
     */
    public static double isinf(double x) {
        return (Double.isNaN(x) ? Double.NaN : (Double.isInfinite(x) ? 1d : 0d));
    }
    
    /**
     * Tests if x is equal to Double.NaN.
     * 
     * @param x test value
     * @return 1 if x is NaN; 0 otherwise
     */
    public static double isnan(double x) {
        return Double.isNaN(x) ? 1d : 0d;
    }
    
    /**
     * Tests if x is null. This is the same as {@link #isnan(double)}.
     * 
     * @param x the test value
     * @return 1 if x is null; 0 otherwise
     */
    public static double isnull(double x) {
        return Double.isNaN(x) ? 1d : 0d;
    }

    /**
     * Gets the log of x to base b.
     * 
     * @param x the value
     * @param b the base
     * @return log to base b
     */
    public static double log2Arg(double x, double b) {
        return Math.log(x) / Math.log(b);
    }
    
    /**
     * Gets the maximum of the input values. Double.Nan (null)
     * values are ignored.
     * 
     * @param values the input values
     * @return the maximum value
     */
    public static double max(Double[] values) {
        return SampleStats.max(values, true);
    }
    
    /**
     * Gets the mean of the input values. Double.Nan (null)
     * values are ignored.
     * 
     * @param values the input values
     * @return the mean value
     */
    public static double mean(Double[] values) {
        return SampleStats.mean(values, true);
    }
    
    /**
     * Gets the median of the input values. Double.Nan (null)
     * values are ignored.
     * 
     * @param values the input values
     * @return the median value
     */
    public static double median(Double[] values) {
        return SampleStats.median(values, true);
    }
    
    /**
     * Gets the minimum of the input values. Double.Nan (null)
     * values are ignored.
     * 
     * @param values the input values
     * @return the minimum value
     */
    public static double min(Double[] values) {
        return SampleStats.min(values, true);
    }
    
    /**
     * Gets the mode of the input values. Double.Nan (null)
     * values are ignored.
     * 
     * @param values the input values
     * @return the modal value
     */
    public static double mode(Double[] values) {
        return SampleStats.mode(values, true);
    }
    
    /**
     * Returns Double.NaN.
     * 
     * @return Double.NaN
     */
    public static double nullValue() {
        return Double.NaN;
    }

    /**
     * Converts an angle in radians to degrees.
     * 
     * @param x input angle in radians
     * @return angle in degrees
     */
    public static double radToDeg(double x) {
        return x / Math.PI * 180d;
    }
    
    /**
     * Gets a random value between 0 (inclusive) and x (exclusive).
     * 
     * @param x upper limit
     * @return the random value
     */
    public static double rand(double x) {
        return rr.nextDouble() * x;
    }
    
    /**
     * Gets a random integer value (actually a truncated double) between 
     * 0 (inclusive) and {@code floor(x)} (exclusive).
     * 
     * @param x upper limit
     * @return the random value
     */
    public static double randInt(double x) {
        return rr.nextInt((int) x);
    }
    
    /**
     * Gets the range of the input values. Double.Nan (null)
     * values are ignored.
     * 
     * @param values the input values
     * @return the range of the input values
     */
    public static double range(Double[] values) {
        return SampleStats.range(values, true);
    }
    
    /**
     * Rounds the input value to the given precision.
     * 
     * @param x the input value
     * @param prec the desired precision
     * @return the rounded value
     */
    public static double round2Arg(double x, double prec) {
        int ifac = (int) (prec + 0.5);
        return Math.round(x / ifac) * ifac;
    }
    
    /**
     * Gets the sample standard deviation of the input values. Double.Nan (null)
     * values are ignored.
     * 
     * @param values the input values
     * @return the standard deviation of the input values
     */
    public static double sdev(Double[] values) {
        return SampleStats.range(values, true);
    }

    /**
     * Gets the sample variance of the input values. Double.Nan (null)
     * values are ignored.
     * 
     * @param values the input values
     * @return the variance of the input values
     */
    public static double variance(Double[] values) {
        return SampleStats.variance(values, true);
    }
    
    /**
     * Tests if either x or y is non-zero.
     * 
     * @param x x value
     * @param y y value
     * @return 1 if either x or y is non-zero; 0 if this is not the case;
     *         or {@code Double.Nan} if either x or y is {@code Double.Nan}
     */
    public static double OR(double x, double y) {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return Double.NaN;
        }

        return (!dzero(x) || !dzero(y) ? 1d : 0d);
    }

    /**
     * Tests if both x and y are non-zero.
     * 
     * @param x x value
     * @param y y value
     * @return 1 if both x and y are non-zero; 0 if this is not the case;
     *         or {@code Double.Nan} if either x or y is {@code Double.Nan}
     */
    public static double AND(double x, double y) {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return Double.NaN;
        }

        return (!dzero(x) && !dzero(y) ? 1d : 0d);
    }

    /**
     * Tests if just one of x or y is non-zero.
     * 
     * @param x x value
     * @param y y value
     * @return 1 if just one of x or y is non-zero; 0 if this is not the case;
     *         or {@code Double.Nan} if either x or y is {@code Double.Nan}
     */
    public static double XOR(double x, double y) {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return Double.NaN;
        }

        return (!dzero(x) ^ !dzero(y) ? 1d : 0d);
    }

    /**
     * Tests if x is greater than y.
     * 
     * @param x x value
     * @param y y value
     * @return 1 if x is greater than y; 0 if this is not the case;
     *         or {@code Double.Nan} if either x or y is {@code Double.Nan}
     */
    public static double GT(double x, double y) {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return Double.NaN;
        }

        return (dcomp(x, y) > 0 ? 1d : 0d);
    }

    /**
     * Tests if x is greater than or equal to y.
     * 
     * @param x x value
     * @param y y value
     * @return 1 if x is greater than or equal to y; 0 if this is not the case;
     *         or {@code Double.Nan} if either x or y is {@code Double.Nan}
     */
    public static double GE(double x, double y) {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return Double.NaN;
        }

        return (dcomp(x, y) >= 0 ? 1d : 0d);
    }

    /**
     * Tests if x is less than y.
     * 
     * @param x x value
     * @param y y value
     * @return 1 if x is less than y; 0 if this is not the case;
     *         or {@code Double.Nan} if either x or y is {@code Double.Nan}
     */
    public static double LT(double x, double y) {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return Double.NaN;
        }

        return (dcomp(x, y) < 0 ? 1d : 0d);
    }

    /**
     * Tests if x is less than or equal to y.
     * 
     * @param x x value
     * @param y y value
     * @return 1 if x is less than or equal to y; 0 if this is not the case;
     *         or {@code Double.Nan} if either x or y is {@code Double.Nan}
     */
    public static double LE(double x, double y) {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return Double.NaN;
        }

        return (dcomp(x, y) <= 0 ? 1d : 0d);
    }

    /**
     * Tests if x is equal to y.
     * 
     * @param x x value
     * @param y y value
     * @return 1 if x is equal to y; 0 if this is not the case;
     *         or {@code Double.Nan} if either x or y is {@code Double.Nan}
     */
    public static double EQ(double x, double y) {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return Double.NaN;
        }

        return (dcomp(x, y) == 0 ? 1d : 0d);
    }

    /**
     * Tests if x is not equal to y.
     * 
     * @param x x value
     * @param y y value
     * @return 1 if x is not equal to y; 0 if this is not the case;
     *         or {@code Double.Nan} if either x or y is {@code Double.Nan}
     */
    public static double NE(double x, double y) {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return Double.NaN;
        }

        return (dcomp(x, y) != 0 ? 1d : 0d);
    }

    /**
     * Treats x as true if non-zero, or false if zero and then
     * returns the logical complement.
     * 
     * @param x the test value
     * @return 1 if x is zero; 0 if x is non-zero; 
     * or {@code Double.Nan} if x is {@code Double.Nan}
     */
    public static double NOT(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }

        return (dzero(x) ? 1d : 0d);
    }
}

