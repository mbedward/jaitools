/* 
 *  Copyright (c) 2010, Michael Bedward. All rights reserved. 
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

import jaitools.CollectionFactory;
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

        List<Range<Integer>> expected = CollectionFactory.list();
        expected.add(Range.create(null, false, value, false));
        expected.add(Range.create(value, false, null, false));

        assertEquals(expected.size(), compList.size());
        assertTrue(compList.containsAll(expected));
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

        List<Range<Integer>> expected = CollectionFactory.list();
        expected.add(Range.create(null, false, -10, false));
        expected.add(Range.create(10, true, null, false));

        assertEquals(expected.size(), compList.size());
        assertTrue(compList.containsAll(expected));
    }

    @Test
    public void createComplementOfOpenInterval() {
        System.out.println("   createComplementOfOpenInterval");

        // min open
        Range<Integer> input = Range.create(null, false, 10, true);
        List<Range<Integer>> compList = RangeUtils.createComplement(input);

        List<Range<Integer>> expected = CollectionFactory.list();
        expected.add(Range.create(10, false, null, false));

        assertEquals(expected.size(), compList.size());
        assertTrue(compList.containsAll(expected));
        
        // max open
        input = Range.create(10, true, null, false);
        compList = RangeUtils.createComplement(input);

        expected.clear();
        expected.add(Range.create(null, false, 10, false));

        assertEquals(expected.size(), compList.size());
        assertTrue(compList.containsAll(expected));
    }

    @Test
    public void testCreateComplementOfTwoFiniteIntervals() {
        System.out.println("   testCreateComplementOfTwoFiniteIntervals");

        List<Range<Integer>> ranges = CollectionFactory.list();
        ranges.add(Range.create(-10, true, -5, false));
        ranges.add(Range.create(5, true, 10, false));
        List<Range<Integer>> compList = RangeUtils.createComplement(ranges);

        List<Range<Integer>> expected = CollectionFactory.list();
        expected.add(Range.create(null, false, -10, false));
        expected.add(Range.create(-5, true, 5, false));
        expected.add(Range.create(10, true, null, false));

        assertEquals(expected.size(), compList.size());
        assertTrue(compList.containsAll(expected));
    }

    @Test
    public void testCreateComplementOfOverlappingIntervals() {
        System.out.println("   testCreateComplementOfTwoFiniteIntervals");

        List<Range<Integer>> ranges = CollectionFactory.list();
        ranges.add(Range.create(-10, true, 5, false));
        ranges.add(Range.create(0, true, 10, false));
        List<Range<Integer>> compList = RangeUtils.createComplement(ranges);

        List<Range<Integer>> expected = CollectionFactory.list();
        expected.add(Range.create(null, false, -10, false));
        expected.add(Range.create(10, true, null, false));

        assertEquals(expected.size(), compList.size());
        assertTrue(compList.containsAll(expected));
    }

    @Test
    public void testCreateComplementOfOpenIntervals() {
        System.out.println("   testCreateComplementOfOpenIntervals");

        List<Range<Integer>> ranges = CollectionFactory.list();
        ranges.add(Range.create(null, false, -5, false));
        ranges.add(Range.create(5, true, null, false));
        List<Range<Integer>> compList = RangeUtils.createComplement(ranges);

        List<Range<Integer>> expected = CollectionFactory.list();
        expected.add(Range.create(-5, true, 5, false));

        assertEquals(expected.size(), compList.size());
        assertTrue(compList.containsAll(expected));
    }

    @Test
    public void testCreateComplementOfFinitePoints() {
        System.out.println("   testCreateComplementOfFinitePoints");

        List<Range<Integer>> ranges = CollectionFactory.list();
        ranges.add(Range.create(-5));
        ranges.add(Range.create(5));
        List<Range<Integer>> compList = RangeUtils.createComplement(ranges);

        List<Range<Integer>> expected = CollectionFactory.list();
        expected.add(Range.create(null, false, -5, false));
        expected.add(Range.create(-5, false, 5, false));
        expected.add(Range.create(5, false, null, false));

        assertEquals(expected.size(), compList.size());
        assertTrue(compList.containsAll(expected));
    }

    @Test
    public void testSortRanges() {
        System.out.println("   testSortRanges");

        List<Range<Integer>> sortedInputs = new ArrayList<Range<Integer>>();
        sortedInputs.add( Range.create(null, false, 5, true) );
        sortedInputs.add( Range.create(-10, true, 5, true) );
        sortedInputs.add( Range.create(0) );
        sortedInputs.add( Range.create(5, true, 10, true) );
        sortedInputs.add( Range.create(5, true, null, false) );

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
        inputs.add( Range.create(null, false, 5, false));
        inputs.add( Range.create(0));
        inputs.add( Range.create(0, true, 5, false));
        inputs.add( Range.create(5, true, 10, true));
        inputs.add( Range.create(20, true, 30, true));

        List<Range<Integer>> result = RangeUtils.simplify(inputs);
        assertEquals(2, result.size());
        assertEquals(new Range<Integer>(null, false, 10, true), result.get(0));
        assertEquals(new Range<Integer>(20, true, 30, true), result.get(1));
    }

    @Test
    public void testIntersection() {
        System.out.println("   testIntersection");
        
        Range<Integer> r1 = Range.create(null, false, 5, false);
        Range<Integer> r2 = Range.create(-5, false, null, false);
        Range<Integer> result = RangeUtils.intersection(r2, r1);

        Range<Integer> expected = Range.create(-5, false, 5, false);
        assertEquals(expected, result);

        Range<Integer> r3 = Range.create(5, true, null, false);
        result = RangeUtils.intersection(r3, r1);
        assertNull(result);
    }

    @Test
    public void testSubtract() {
        System.out.println("   testSubtract");

        Range<Integer> r1 = Range.create(null, false, null, false);
        Range<Integer> r2 = Range.create(-5, false, 5, true);

        List<Range<Integer>> result = RangeUtils.subtract(r1, r2);
        assertEquals(0, result.size());

        result = RangeUtils.subtract(r2, r1);
        assertEquals(2, result.size());
        assertEquals(new Range<Integer>(null, false, -5, true), result.get(0));
        assertEquals(new Range<Integer>(5, false, null, false), result.get(1));
    }
}
