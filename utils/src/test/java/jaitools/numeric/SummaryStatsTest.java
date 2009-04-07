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

import jaitools.numeric.SampleStats;
import jaitools.numeric.DoubleComparison;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Bedward
 */
public class SummaryStatsTest {
    
    static final Double[] values = {-5d, -2d, 1d, 1d, 1d, 3d, 3d, 3d, 3d, 3d, Double.NaN, 5d};

    @Test
    public void testMax() {
        System.out.println("max");
        double expResult = 5.0;
        double result = SampleStats.max(values, true);
        assertEquals(expResult, result);
    }

    @Test
    public void testMin() {
        System.out.println("min");
        double expResult = -5.0;
        double result = SampleStats.min(values, true);
        assertEquals(expResult, result);
    }

    @Test
    public void testMedian() {
        System.out.println("median");
        double expResult = 3d;
        double result = SampleStats.median(values, true);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testMode() {
        System.out.println("mode");
        double expResult = 3.0d;
        double result = SampleStats.mode(values, true);
        assertEquals(expResult, result);
    }

    @Test
    public void testRange() {
        System.out.println("range");
        double expResult = 10.0d;
        double result = SampleStats.range(values, true);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testMean() {
        System.out.println("mean");
        assertEquals(calcMean(), SampleStats.mean(values, true));
    }

    @Test
    public void testVariance() {
        System.out.println("variance");
        double v1 = calcVariance();
        double v2 = SampleStats.variance(values, true);
        assertTrue(DoubleComparison.dcomp(v1, v2) == 0);
    }

    private double calcMean() {
        int n = 0;
        double sum = 0.0d;
        for (int i = 0; i < values.length; i++) {
            if (!Double.isNaN(values[i])) {
                sum += values[i];
                n++ ;
            }
        }
        return sum / n;
    }

    private double calcVariance() {
        int n = 0;
        double var = 0.0d;
        double mean = calcMean();

        for (int i = 0; i < values.length; i++) {
            if (!Double.isNaN(values[i])) {
                var += (values[i] - mean)*(values[i] - mean);
                n++ ;
            }
        }

        return var / (n-1);
    }

}