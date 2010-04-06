/*
 * Copyright 2010 Michael Bedward
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

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the RangeUtils static helper methods.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class RangeUtilsTest {

    @Test
    public void testCreateComplementOfFinitePoint() {
        System.out.println("   testCreateComplementOfFinitePoint");

        final int value = 42;
        Range<Integer> point = Range.create(value);

        List<Range<Integer>> compList = RangeUtils.createComplement(point);
        assertEquals(2, compList.size());

        // expected ranges [-Inf, value) and (value, Inf]
        Range<Integer> expLow = Range.create(null, true, value, false);
        Range<Integer> expHigh = Range.create(value, false, null, true);

        // don't assume the ranges are in order
        Range<Integer> low, high;
        if (compList.get(0).isMinOpen()) {
            low = compList.get(0);
            high = compList.get(1);
        } else {
            low = compList.get(1);
            assertTrue(low.isMinOpen());
            high = compList.get(0);
        }
        assertTrue(high.isMaxOpen());

        assertEquals(value, low.getMax().intValue());
        assertFalse(low.isMaxIncluded());

        assertEquals(value, high.getMin().intValue());
        assertFalse(high.isMinIncluded());
    }

    /**
     * Create the complement of a point at infinity. By definition, the complement
     * is the interval from negative infinity to positive infinity.
     */
    @Test
    public void testCreateComplementOfInfinitePoint() {
        System.out.println("   testCreateComplementOfInfinitePoint");

        Range<Integer> point = Range.create(null, Range.INF);

        List<Range<Integer>> compList = RangeUtils.createComplement(point);
        assertEquals(1, compList.size());

        // expected range [-Inf, Inf]
        assertTrue(compList.get(0).isMinNegInf());
        assertTrue(compList.get(0).isMaxInf());
    }

    
    @Test
    public void testCreateComplementOfFiniteInterval() {
        System.out.println("   testCreateComplementOfFiniteInterval");

        Range<Integer> input = Range.create(-10, true, 10, false);
        List<Range<Integer>> compList = RangeUtils.createComplement(input);
        assertEquals(2, compList.size());

        // don't assume order
        Range<Integer> low, high;
        if (compList.get(0).isMinOpen()) {
            low = compList.get(0);
            high = compList.get(1);
        } else {
            low = compList.get(1);
            high = compList.get(0);
        }

        assertEquals(input.getMin().intValue(), low.getMax().intValue());
        assertFalse(low.isMaxIncluded());
        assertEquals(input.getMax().intValue(), high.getMin().intValue());
        assertTrue(high.isMinIncluded());
    }

    @Test
    public void createComplementOfOpenInterval() {
        System.out.println("   createComplementOfOpenInterval");

        // min open
        Range<Integer> input = Range.create(null, true, 10, true);
        List<Range<Integer>> compList = RangeUtils.createComplement(input);
        assertEquals(1, compList.size());

        Range<Integer> result = compList.get(0);
        assertEquals(input.getMax().intValue(), result.getMin().intValue());
        assertFalse(result.isMinIncluded());
        assertTrue(result.isMaxOpen());
        
        // max open
        input = Range.create(10, true, null, true);
        compList = RangeUtils.createComplement(input);
        assertEquals(1, compList.size());

        result = compList.get(0);
        assertEquals(input.getMin().intValue(), result.getMax().intValue());
        assertFalse(result.isMaxIncluded());
        assertTrue(result.isMinOpen());
    }

    @Test
    public void testSortRanges() {
        System.out.println("   testSortRanges");

        List<Range<Integer>> sortedInputs = new ArrayList<Range<Integer>>();
        sortedInputs.add( Range.create(null, true, 5, true) );
        sortedInputs.add( Range.create(-10, true, 5, true) );
        sortedInputs.add( Range.create(0) );
        sortedInputs.add( Range.create(5, true, 10, true) );
        sortedInputs.add( Range.create(5, true, null, true) );

        List<Range<Integer>> disorderedInputs = new ArrayList<Range<Integer>>();
        for (int i : new int[]{4, 2, 3, 1, 0}) {
            disorderedInputs.add(sortedInputs.get(i));
        }

        List<Range<Integer>> result = RangeUtils.sort(disorderedInputs);
        int k = 0;
        for (Range r : result) {
            assertTrue(r.equals(sortedInputs.get(k++)));
        }
    }

    @Test
    public void testSimplify() {
        System.out.println("   testSimplify");

        List<Range<Integer>> inputs = new ArrayList<Range<Integer>>();
        inputs.add( Range.create(null, true, 5, false));
        inputs.add( Range.create(0));
        inputs.add( Range.create(0, true, 5, false));
        inputs.add( Range.create(5, true, 10, true));
        inputs.add( Range.create(20, true, 30, true));

        List<Range<Integer>> result = RangeUtils.simplify(inputs);
        assertEquals(2, result.size());
        assertEquals(new Range<Integer>(null, true, 10, true), result.get(0));
        assertEquals(new Range<Integer>(20, true, 30, true), result.get(1));
    }

    @Test
    public void testIntersection() {
        System.out.println("   testIntersection");
        
        Range<Integer> r1 = Range.create(null, true, 5, false);
        Range<Integer> r2 = Range.create(-5, false, null, true);
        Range<Integer> result = RangeUtils.intersection(r2, r1);

        Range<Integer> expected = Range.create(-5, false, 5, false);
        assertEquals(expected, result);

        Range<Integer> r3 = Range.create(5, true, null, true);
        result = RangeUtils.intersection(r3, r1);
        assertNull(result);
    }

    @Test
    public void testSubtract() {
        System.out.println("   testSubtract");

        Range<Integer> r1 = Range.create(null, true, null, true);
        Range<Integer> r2 = Range.create(-5, false, 5, true);

        List<Range<Integer>> result = RangeUtils.subtact(r1, r2);
        assertEquals(0, result.size());

        result = RangeUtils.subtact(r2, r1);
        assertEquals(2, result.size());
        assertEquals(new Range<Integer>(null, true, -5, true), result.get(0));
        assertEquals(new Range<Integer>(5, false, null, true), result.get(1));
    }
}
