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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the SampleStats class
 *
 * @author Michael Bedward
 */
public class SampleStatsTest {
    
    static final Double[] values;
    static {
        values = new Double[20];
        for (int i = 1, k=0; i <= 10; i++) {
            values[k++] = (double)i;
            values[k++] = Double.NaN;
        }
    }

    @Test
    public void testMax() {
        System.out.println("   max");
        double expResult = 10.0d;
        double result = SampleStats.max(values, true);
        assertTrue(DoubleComparison.dzero(expResult - result));
    }

    @Test
    public void testMin() {
        System.out.println("   min");
        double expResult = 1.0d;
        double result = SampleStats.min(values, true);
        assertTrue(DoubleComparison.dzero(expResult - result));
    }

    @Test
    public void testMedian() {
        System.out.println("   median");
        double expResult = 5.5d;
        double result = SampleStats.median(values, true);
        assertTrue(DoubleComparison.dzero(expResult - result));
    }
    
    @Test
    public void testRange() {
        System.out.println("   range");
        double expResult = 9.0d;
        double result = SampleStats.range(values, true);
        assertTrue(DoubleComparison.dzero(expResult - result));
    }
    
    @Test
    public void testMean() {
        System.out.println("   mean");
        double expResult = 5.5d;
        double result = SampleStats.mean(values, true);
        assertTrue(DoubleComparison.dzero(expResult - result));
    }

    @Test
    public void testVariance() {
        System.out.println("   variance");
        double expResult = 9.0d + 1.0d / 6;
        double result = SampleStats.variance(values, true);
        assertTrue(DoubleComparison.dcomp(expResult, result) == 0);
    }

}