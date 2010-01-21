/*
 * Copyright 2009 Michael Bedward
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

import jaitools.CollectionFactory;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class StreamingSampleStatsTest {

    private final Double singleValue = 42.0;

    @Test
    public void testMean() {
        System.out.println("   test mean");
        StreamingSampleStats stats = new StreamingSampleStats();
        stats.setStatistic(Statistic.MEAN);

        for (int val = -1000; val <= 1000; val++) {
            stats.addSample((double)val);
        }

        double result = stats.getStatisticValue(Statistic.MEAN);
        assertTrue(DoubleComparison.dzero(result));
    }

    @Test
    public void testMeanSingleValue() {
        System.out.println("   test mean single value");
        StreamingSampleStats stats = new StreamingSampleStats();
        stats.setStatistic(Statistic.MEAN);
        stats.addSample(singleValue);

        assertEquals(singleValue, stats.getStatisticValue(Statistic.MEAN));
    }

    @Test
    public void testZeroVariance() {
        System.out.println("   test zero variance");
        StreamingSampleStats stats = new StreamingSampleStats();
        stats.setStatistic(Statistic.VARIANCE);

        for (int val = -1000; val <= 1000; val++) {
            stats.addSample(42.0d);
        }

        double result = stats.getStatisticValue(Statistic.VARIANCE);
        assertTrue(DoubleComparison.dzero(result));
    }

    @Test
    public void testVariance() {
        System.out.println("   test variance");
        StreamingSampleStats stats = new StreamingSampleStats();
        stats.setStatistic(Statistic.VARIANCE);

        for (int val = -1000; val <= 1000; val++) {
            stats.addSample((double)val);
        }

        double expResult = 333833.5d;  // calculated with R
        double result = stats.getStatisticValue(Statistic.VARIANCE);
        assertTrue(DoubleComparison.dcomp(result, expResult) == 0);
    }

    @Test
    public void testVarianceSingleValue() {
        System.out.println("   test variance single value");
        StreamingSampleStats stats = new StreamingSampleStats();
        stats.setStatistic(Statistic.VARIANCE);
        stats.addSample(singleValue);

        assertTrue(Double.isNaN(stats.getStatisticValue(Statistic.VARIANCE)));
    }

    @Test
    public void testMinMaxRange() {
        System.out.println("   test min, max and range");
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
            streamStats.addSample(val);
        }

        double result = streamStats.getStatisticValue(Statistic.MIN);
        assertTrue(DoubleComparison.dcomp(result, min) == 0);

        result = streamStats.getStatisticValue(Statistic.MAX);
        assertTrue(DoubleComparison.dcomp(result, max) == 0);

        result = streamStats.getStatisticValue(Statistic.RANGE);
        assertTrue(DoubleComparison.dcomp(result, max - min) == 0);
    }

    @Test
    public void testMinMaxRangeSingleValue() {
        System.out.println("   test min, max and range single value");
        StreamingSampleStats streamStats = new StreamingSampleStats();
        Statistic[] stats = {
            Statistic.MIN, Statistic.MAX, Statistic.RANGE
        };
        streamStats.setStatistics(stats);
        streamStats.addSample(singleValue);

        assertEquals(singleValue, streamStats.getStatisticValue(Statistic.MIN));
        assertEquals(singleValue, streamStats.getStatisticValue(Statistic.MAX));
        assertEquals(Double.valueOf(0), streamStats.getStatisticValue(Statistic.RANGE));
    }

    @Test
    public void testMedian() {
        System.out.println("   test exact and approximate median");
        StreamingSampleStats streamStats = new StreamingSampleStats();
        streamStats.setStatistic(Statistic.MEDIAN);
        streamStats.setStatistic(Statistic.APPROX_MEDIAN);

        List<Double> values = CollectionFactory.newList();

        for (int val = -1000, k = 0; val <= 1000; val++, k++) {
            values.add((double)val);
        }

        Collections.shuffle(values);
        for (Double val : values) {
            streamStats.addSample(val);
        }

        double exact = streamStats.getStatisticValue(Statistic.MEDIAN);
        assertTrue(DoubleComparison.dzero(exact));

        double error = Math.abs(exact - streamStats.getStatisticValue(Statistic.APPROX_MEDIAN));
        assertTrue(error / values.size() <= 0.05);
    }

    @Test
    public void testSum() {
        System.out.println("   test sum");
        StreamingSampleStats stats = new StreamingSampleStats();
        stats.setStatistic(Statistic.SUM);

        for (int val = -1000; val <= 1000; val++) {
            stats.addSample((double)val);
        }

        double result = stats.getStatisticValue(Statistic.SUM);
        assertTrue(DoubleComparison.dzero(result));
    }
    
    @Test
    public void testExactMedianSingleValue() {
        System.out.println("   test exact median single value");
        StreamingSampleStats streamStats = new StreamingSampleStats();
        streamStats.setStatistic(Statistic.MEDIAN);

        streamStats.addSample(singleValue);
        assertEquals(singleValue, streamStats.getStatisticValue(Statistic.MEDIAN));
    }

    @Test
    public void testApproxMedianSingleValue() {
        System.out.println("   test approx median single value");
        StreamingSampleStats streamStats = new StreamingSampleStats();
        streamStats.setStatistic(Statistic.APPROX_MEDIAN);

        streamStats.addSample(singleValue);
        assertEquals(singleValue, streamStats.getStatisticValue(Statistic.APPROX_MEDIAN));
    }
}