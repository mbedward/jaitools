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

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Bedward
 */
public class RunningSampleStatsTest {

    public RunningSampleStatsTest() {
    }

    @Test
    public void testMean() {
        System.out.println("   test mean");
        RunningSampleStats stats = new RunningSampleStats();
        stats.setStatistic(Statistic.MEAN);

        for (int val = -1000; val <= 1000; val++) {
            stats.addSample((double)val);
        }

        double result = stats.getStatisticValue(Statistic.MEAN);
        assertTrue(DoubleComparison.dzero(result));
    }

    @Test
    public void testZeroVariance() {
        System.out.println("   test zero variance");
        RunningSampleStats stats = new RunningSampleStats();
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
        RunningSampleStats stats = new RunningSampleStats();
        stats.setStatistic(Statistic.VARIANCE);

        for (int val = -1000; val <= 1000; val++) {
            stats.addSample((double)val);
        }

        double expResult = 333833.5d;  // calculated with R
        double result = stats.getStatisticValue(Statistic.VARIANCE);
        assertTrue(DoubleComparison.dcomp(result, expResult) == 0);
    }

    @Test
    public void testMinMaxRange() {
        System.out.println("   test min, max and range");
        RunningSampleStats runStats = new RunningSampleStats();
        Statistic[] stats = {
            Statistic.MIN, Statistic.MAX, Statistic.RANGE
        };
        runStats.setStatistics(stats);

        Random rr = new Random();
        double limit = 1000d;
        double min = limit * 2;
        double max = limit * -2;
        for (int i = -1000; i <= 1000; i++) {
            double val = limit * (rr.nextDouble() - 2.0d);
            if (val < min) min = val;
            if (val > max) max = val;
            runStats.addSample(val);
        }

        double result = runStats.getStatisticValue(Statistic.MIN);
        assertTrue(DoubleComparison.dcomp(result, min) == 0);

        result = runStats.getStatisticValue(Statistic.MAX);
        assertTrue(DoubleComparison.dcomp(result, max) == 0);

        result = runStats.getStatisticValue(Statistic.RANGE);
        assertTrue(DoubleComparison.dcomp(result, max - min) == 0);
    }
}