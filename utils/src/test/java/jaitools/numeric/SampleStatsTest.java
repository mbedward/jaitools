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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the SampleStats class
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class SampleStatsTest {
    
    private static final double TOL = 1.0e-8;
    
    static final Double[] values;
    static {
        values = new Double[20];
        for (int i = 1, k=0; i <= 10; i++) {
            values[k++] = (double)i;
            values[k++] = Double.NaN;
        }
    }

    static final Double[] singleValue = { 42.0 };

    @Test
    public void testMax() {
        System.out.println("   max");
        double expResult = 10.0d;
        double result = SampleStats.max(values, true);
        assertEquals(expResult, result, TOL);
    }
    
    @Test
    public void testMaxSingleValue() {
        System.out.println("   max with single value");
        assertEquals(singleValue[0], Double.valueOf(SampleStats.max(singleValue, true)));
    }

    @Test
    public void testMin() {
        System.out.println("   min");
        double expResult = 1.0d;
        double result = SampleStats.min(values, true);
        assertEquals(expResult, result, TOL);
    }

    @Test
    public void testMinSingleValue() {
        System.out.println("   min with single value");
        assertEquals(singleValue[0], Double.valueOf(SampleStats.min(singleValue, true)));
    }

    @Test
    public void testMedian() {
        System.out.println("   median");
        double expResult = 5.5d;
        double result = SampleStats.median(values, true);
        assertEquals(expResult, result, TOL);
    }
    
    @Test
    public void testMedianSingleValue() {
        System.out.println("   median with single value");
        assertEquals(singleValue[0], Double.valueOf(SampleStats.median(singleValue, true)));
    }

    @Test
    public void testRange() {
        System.out.println("   range");
        double expResult = 9.0d;
        double result = SampleStats.range(values, true);
        assertEquals(expResult, result, TOL);
    }
    
    @Test
    public void testRangeSingleValue() {
        System.out.println("   range with single value");
        assertEquals(Double.valueOf(0), Double.valueOf(SampleStats.range(singleValue, true)));
    }

    @Test
    public void testMean() {
        System.out.println("   mean");
        double expResult = 5.5d;
        double result = SampleStats.mean(values, true);
        assertEquals(expResult, result, TOL);
    }

    @Test
    public void testMeanSingleValue() {
        System.out.println("   mean with single value");
        assertEquals(singleValue[0], Double.valueOf(SampleStats.mean(singleValue, true)));
    }

    @Test
    public void testVariance() {
        System.out.println("   variance");
        double expResult = 9.0d + 1.0d / 6;
        double result = SampleStats.variance(values, true);
        assertEquals(expResult, result, TOL);
    }
    
    @Test
    public void testSum() {
        System.out.println("   sum");
        double expResult = 55.0d;
        double result = SampleStats.sum(values, true);
        assertEquals(expResult, result, TOL);
    }

    @Test
    public void testVarianceSingleValue() {
        System.out.println("   variance with single value");
        assertTrue(Double.isNaN(SampleStats.variance(singleValue, true)));
    }

}
