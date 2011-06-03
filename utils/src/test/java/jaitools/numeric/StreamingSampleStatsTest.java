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

package jaitools.numeric;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import jaitools.CollectionFactory;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for StreamingSampleStats.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class StreamingSampleStatsTest {

    private static final double TOL = 1.0E-6;
    private final Double singleValue = 42.0;

    @Test
    public void testMean() {
        System.out.println("   testMean");

        StreamingSampleStats stats = new StreamingSampleStats();
        stats.setStatistic(Statistic.MEAN);

        for (int val = -1000; val <= 1000; val++) {
            stats.offer((double)val);
        }

        double result = stats.getStatisticValue(Statistic.MEAN);
        assertEquals(0.0, result, TOL);
    }

    @Test
    public void testMeanWithExcludedRange() {
        System.out.println("   testMeanWithExcludedRange");

        StreamingSampleStats stats = new StreamingSampleStats();
        stats.setStatistic(Statistic.MEAN);
        stats.addRange(Range.create(null, false, 0.0, false), Range.Type.EXCLUDE);

        for (int val = -1000; val <= 1000; val++) {
            stats.offer((double)val);
        }

        double result = stats.getStatisticValue(Statistic.MEAN);
        assertEquals(500.0, result, TOL);
    }

    @Test
    public void testMeanSingleValue() {
        System.out.println("   testMeanSingleValue");

        StreamingSampleStats stats = new StreamingSampleStats();
        stats.setStatistic(Statistic.MEAN);
        stats.offer(singleValue);

        assertEquals(singleValue, stats.getStatisticValue(Statistic.MEAN));
    }

    @Test
    public void testZeroVariance() {
        System.out.println("   testZeroVariance");

        StreamingSampleStats stats = new StreamingSampleStats();
        stats.setStatistic(Statistic.VARIANCE);

        for (int val = -1000; val <= 1000; val++) {
            stats.offer(42.0d);
        }

        double result = stats.getStatisticValue(Statistic.VARIANCE);
        assertEquals(0.0, result, TOL);
    }

    @Test
    public void testVariance() {
        System.out.println("   testVariance");

        StreamingSampleStats stats = new StreamingSampleStats();
        stats.setStatistic(Statistic.VARIANCE);

        for (int val = -1000; val <= 1000; val++) {
            stats.offer((double)val);
        }

        double expResult = 333833.5d;  // calculated with R
        double result = stats.getStatisticValue(Statistic.VARIANCE);
        assertEquals(expResult, result, TOL);
    }

    @Test
    public void testVarianceSingleValue() {
        System.out.println("   testVarianceSingleValue");

        StreamingSampleStats stats = new StreamingSampleStats();
        stats.setStatistic(Statistic.VARIANCE);
        stats.offer(singleValue);

        assertTrue(Double.isNaN(stats.getStatisticValue(Statistic.VARIANCE)));
    }

    @Test
    public void testMinMaxRange() {
        System.out.println("   testMinMaxRange");

        StreamingSampleStats streamStats = new StreamingSampleStats();
        Statistic[] stats = {
            Statistic.MIN, Statistic.MAX, Statistic.RANGE
        };
        streamStats.setStatistics(stats);

        Random rr = new Random();
        double limit = 1000d;
        double min = limit * 2;
        double max = limit * -2;
        for (int i = -1000; i <= 1000; i++) {
            double val = limit * (rr.nextDouble() - 2.0d);
            if (val < min) min = val;
            if (val > max) max = val;
            streamStats.offer(val);
        }

        double result = streamStats.getStatisticValue(Statistic.MIN);
        assertEquals(min, result, TOL);

        result = streamStats.getStatisticValue(Statistic.MAX);
        assertEquals(max, result, TOL);

        result = streamStats.getStatisticValue(Statistic.RANGE);
        assertEquals(max - min, result, TOL);
    }

    @Test
    public void testMinMaxRangeSingleValue() {
        System.out.println("   testMinMaxRangeSingleValue");

        StreamingSampleStats streamStats = new StreamingSampleStats();
        Statistic[] stats = {
            Statistic.MIN, Statistic.MAX, Statistic.RANGE
        };
        streamStats.setStatistics(stats);
        streamStats.offer(singleValue);

        assertEquals(singleValue, streamStats.getStatisticValue(Statistic.MIN));
        assertEquals(singleValue, streamStats.getStatisticValue(Statistic.MAX));
        assertEquals(Double.valueOf(0), streamStats.getStatisticValue(Statistic.RANGE));
    }

    @Test
    public void testMedian() {
        System.out.println("   testMedian");

        StreamingSampleStats streamStats = new StreamingSampleStats();
        streamStats.setStatistic(Statistic.MEDIAN);
        streamStats.setStatistic(Statistic.APPROX_MEDIAN);

        List<Double> values = CollectionFactory.list();

        for (int val = -1000, k = 0; val <= 1000; val++, k++) {
            values.add((double)val);
        }

        Collections.shuffle(values);
        for (Double val : values) {
            streamStats.offer(val);
        }

        double exact = streamStats.getStatisticValue(Statistic.MEDIAN);
        assertEquals(0.0, exact, TOL);

        double error = Math.abs(exact - streamStats.getStatisticValue(Statistic.APPROX_MEDIAN));
        assertTrue(error / values.size() <= 0.05);
    }

    @Test
    public void testMedianWithRange() {
        System.out.println("   testMedianWithRange");
        StreamingSampleStats streamStats = new StreamingSampleStats();
        streamStats.setStatistic(Statistic.MEDIAN);
        streamStats.setStatistic(Statistic.APPROX_MEDIAN);
        streamStats.addRange(Range.create(null, false, 0.0, false), Range.Type.EXCLUDE);

        List<Double> values = CollectionFactory.list();

        for (int val = -1000, k = 0; val <= 1000; val++, k++) {
            values.add((double)val);
        }

        Collections.shuffle(values);
        for (Double val : values) {
            streamStats.offer(val);
        }

        double exact = streamStats.getStatisticValue(Statistic.MEDIAN);
        assertEquals(500.0, exact, TOL);

        double error = Math.abs(exact - streamStats.getStatisticValue(Statistic.APPROX_MEDIAN));
        assertTrue(error / streamStats.getNumAccepted(Statistic.APPROX_MEDIAN) <= 0.05);
    }

    @Test
    public void testSum() {
        System.out.println("   testSum");
        StreamingSampleStats stats = new StreamingSampleStats();
        stats.setStatistic(Statistic.SUM);

        for (int val = -1000; val <= 1000; val++) {
            stats.offer((double)val);
        }

        double result = stats.getStatisticValue(Statistic.SUM);
        assertEquals(0.0, result, TOL);
    }

    @Test
    public void testExactMedianSingleValue() {
        System.out.println("   testExactMedianSingleValue");
        StreamingSampleStats streamStats = new StreamingSampleStats();
        streamStats.setStatistic(Statistic.MEDIAN);

        streamStats.offer(singleValue);
        assertEquals(singleValue, streamStats.getStatisticValue(Statistic.MEDIAN));
    }

    @Test
    public void testApproxMedianSingleValue() {
        System.out.println("   testApproxMedianSingleValue");

        StreamingSampleStats streamStats = new StreamingSampleStats();
        streamStats.setStatistic(Statistic.APPROX_MEDIAN);

        streamStats.offer(singleValue);
        assertEquals(singleValue, streamStats.getStatisticValue(Statistic.APPROX_MEDIAN));
    }

    @Test
    public void testNaNValues() {
        System.out.println("   testNaNValues");

        StreamingSampleStats streamStats = new StreamingSampleStats();
        streamStats.setStatistic(Statistic.SUM);

        streamStats.offer(new Double[] { 0.0, Double.NaN, 1.0, Double.NaN, 2.0});
        assertEquals(3.0, streamStats.getStatisticValue(Statistic.SUM), TOL);
        assertEquals(2, streamStats.getNumNaN(Statistic.SUM));
    }

    @Test
    public void testNoDataValues() {
        System.out.println("   testNoDataValues");

        StreamingSampleStats streamStats = new StreamingSampleStats();

        final Range<Double> noDataRange = Range.create(null, false, 0.0, true);
        streamStats.addNoDataRange(noDataRange);

        final double noDataValue = 1.0;
        streamStats.addNoDataValue(noDataValue);

        streamStats.setStatistic(Statistic.SUM);

        double total = 0;
        for (double d = -1.0; d <= 1.0; d += 0.01) {
            streamStats.offer(d);
            if (!noDataRange.contains(d) && !CompareOp.aequal(d, noDataValue)) {
                total += d;
            }
        }

        assertEquals(total, streamStats.getStatisticValue(Statistic.SUM), TOL);
    }

    @Test
    public void testNumNoData() {
        System.out.println("   testNumNoData");

        StreamingSampleStats streamStats = new StreamingSampleStats();

        final Range<Double> noDataRange = Range.create(null, false, 0.0, true);
        streamStats.addNoDataRange(noDataRange);

        final double noDataValue = 1.0;
        streamStats.addNoDataValue(noDataValue);

        streamStats.setStatistic(Statistic.SUM);

        for (double d : new double[] {Double.NaN, -5, 5, -3, 3, -1, 1, 0, Double.NaN}) {
            streamStats.offer(d);
        }

        assertEquals(7, streamStats.getNumNoData(Statistic.SUM));
    }
}
