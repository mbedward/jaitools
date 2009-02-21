/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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