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

package jaitools.jiffle.util;

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
        double result = SummaryStats.max(values);
        assertEquals(expResult, result);
    }

    @Test
    public void testMin() {
        System.out.println("min");
        double expResult = -5.0;
        double result = SummaryStats.min(values);
        assertEquals(expResult, result);
    }

    @Test
    public void testMedian() {
        System.out.println("median");
        double expResult = 3d;
        double result = SummaryStats.median(values);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testMode() {
        System.out.println("mode");
        double expResult = 3.0d;
        double result = SummaryStats.mode(values);
        assertEquals(expResult, result);
    }

    @Test
    public void testRange() {
        System.out.println("range");
        double expResult = 10.0d;
        double result = SummaryStats.range(values);
        assertEquals(expResult, result);
    }

}