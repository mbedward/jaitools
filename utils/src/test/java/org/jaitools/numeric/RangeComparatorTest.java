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

package org.jaitools.numeric;

import org.jaitools.numeric.RangeComparator.Result;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test each of the 18 comparison possibilities presented in
 * Hayes (2003) Figure 3.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class RangeComparatorTest {

    public RangeComparatorTest() {
    }

    @Test
    public void testLLLL() {
        Result expType = RangeComparator.Result.LLLL;
        blurb(expType);

        Range<Integer> r1 = Range.create(null, false, 10, false);
        Range<Integer> r2 = Range.create(20, true, null, false);
        assertTrue(r1.compareTo(r2) == expType);

        r2 = Range.create(10, true, null, false);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(null, false, 10, true);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(null, false, null, false);
        assertFalse(r1.compareTo(r1) == expType);
    }

    @Test
    public void testLLLE() {
        Result expType = RangeComparator.Result.LLLE;
        blurb(expType);

        Range<Integer> r1 = Range.create(null, false, 10, true);
        Range<Integer> r2 = Range.create(10, true, null, false);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(null, false, 10, false);
        assertFalse(r1.compareTo(r2) == expType);

        r2 = Range.create(10, false, null, false);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(null, false, null, false);
        assertFalse(r1.compareTo(r1) == expType);
    }

    @Test
    public void testLLLG() {
        Result expType = RangeComparator.Result.LLLG;
        blurb(expType);

        Range<Integer> r1 = Range.create(null, false, 20, true);
        Range<Integer> r2 = Range.create(10, true, null, false);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(null, false, 10, true);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(null, false, null, false);
        assertFalse(r1.compareTo(r1) == expType);
    }

    @Test
    public void testLLEG() {
        Result expType = RangeComparator.Result.LLEG;
        blurb(expType);

        Range<Integer> r1 = Range.create(null, false, 20, true);
        Range<Integer> r2 = Range.create(10, true, 20, true);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(null, false, 20, false);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(15, true, 20, true);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(0, true, 20, false);
        r2 = Range.create(10, true, 20, false);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(null, false, null, false);
        assertFalse(r1.compareTo(r1) == expType);
    }

    @Test
    public void testLEGG() {
        Result expType = RangeComparator.Result.LEGG;
        blurb(expType);

        Range<Integer> r1 = Range.create(0, true, 20, true);
        Range<Integer> r2 = Range.create(0, true, 10, true);
        assertTrue(r1.compareTo(r2) == expType);

        r2 = Range.create(0, true, 20, false);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(0, true, 20, false);
        assertFalse(r1.compareTo(r2) == expType);

        r2 = Range.create(0, true, 20, true);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(null, false, 20, true);
        r2 = Range.create(null, false, 10, true);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(null, false, null, false);
        assertFalse(r1.compareTo(r1) == expType);
    }

    @Test
    public void testLLEE() {
        Result expType = RangeComparator.Result.LLEE;
        blurb(expType);

        Range<Integer> r1 = Range.create(0, true, 10, true);
        Range<Integer> r2 = Range.create(10);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(0, true, 10, false);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(0, true, 5, true);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(0, true, 15, true);
        assertFalse(r1.compareTo(r2) == expType);

        r2 = Range.create(null, Range.INF);
        r1 = Range.create(0, true, null, false);
        assertTrue(r1.compareTo(r2) == expType);
    }

    @Test
    public void testEEGG() {
        Result expType = RangeComparator.Result.EEGG;
        blurb(expType);

        Range<Integer> r1 = Range.create(10, true, null, false);
        Range<Integer> r2 = Range.create(10);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(10, false, null, false);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(null, false, 10, true);
        r2 = Range.create(null, Range.NEG_INF);
        assertTrue(r1.compareTo(r2) == expType);
    }

    @Test
    public void testLEEG() {
        Result expType = RangeComparator.Result.LEEG;
        blurb(expType);

        Range<Integer> r1 = Range.create(10, true, 20, true);
        Range<Integer> r2 = Range.create(10, true, 20, true);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(10, false, 20, true);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(10, true, 20, false);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(10, false, 20, false);
        r2 = Range.create(10, false, 20, false);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(null, false, null, false);
        assertTrue(r1.compareTo(r1) == expType);

        r1 = Range.create(10);
        assertFalse(r1.compareTo(r1) == expType);
    }

    @Test
    public void testEEEE() {
        Result expType = RangeComparator.Result.EEEE;
        blurb(expType);

        Range<Integer> r1 = Range.create(10);
        Range<Integer> r2 = Range.create(10);
        assertTrue(r1.compareTo(r2) == expType);

        r2 = Range.create(0);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(null, Range.INF);
        r2 = Range.create(null, Range.NEG_INF);
        assertFalse(r1.compareTo(r2) == expType);

        r2 = Range.create(null, Range.INF);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(10, true, 20, true);
        r2 = Range.create(10, true, 20, true);
        assertFalse(r1.compareTo(r2) == expType);
    }

    @Test
    public void testLLGG() {
        Result expType = RangeComparator.Result.LLGG;
        blurb(expType);

        Range<Integer> r1 = Range.create(0, true, 30, true);
        Range<Integer> r2 = Range.create(10, true, 20, true);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(10, true, 20, true);
        assertFalse(r1.compareTo(r2) == expType);

        r2 = Range.create(10, false, 20, false);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(null, false, null, false);
        assertTrue(r1.compareTo(r2) == expType);

        r2 = Range.create(null, false, null, false);
        assertFalse(r1.compareTo(r2) == expType);
    }

    @Test
    public void testLGLG() {
        Result expType = RangeComparator.Result.LGLG;
        blurb(expType);

        Range<Integer> r1 = Range.create(10, true, 20, true);
        Range<Integer> r2 = Range.create(0, true, 30, true);
        assertTrue(r1.compareTo(r2) == expType);

        r2 = Range.create(10, true, 20, true);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(10, false, 20, false);
        assertTrue(r1.compareTo(r2) == expType);

        r2 = Range.create(null, false, null, false);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(null, false, null, false);
        assertFalse(r1.compareTo(r2) == expType);
    }

    @Test
    public void testLGGG() {
        Result expType = RangeComparator.Result.LGGG;
        blurb(expType);

        Range<Integer> r1 = Range.create(10, true, 30, true);
        Range<Integer> r2 = Range.create(0, true, 20, true);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(10, true, 20, true);
        assertFalse(r1.compareTo(r2) == expType);

        r2 = Range.create(0, true, 20, false);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(10, false, 20, true);
        r2 = Range.create(10, true, 20, false);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(15, true, null, false);
        assertTrue(r1.compareTo(r2) == expType);

        r2 = Range.create(10, true, null, false);
        assertFalse(r1.compareTo(r2) == expType);
    }

    @Test
    public void testLGEG() {
        Result expType = RangeComparator.Result.LGEG;
        blurb(expType);

        Range<Integer> r1 = Range.create(10, true, 20, true);
        Range<Integer> r2 = Range.create(0, true, 20, true);
        assertTrue(r1.compareTo(r2) == expType);

        r2 = Range.create(0, true, 20, false);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(10, true, 20, false);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(-5, true, 20, false);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(null, false, 20, false);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(0, true, null, false);
        r2 = Range.create(null, false, null, false);
        assertTrue(r1.compareTo(r2) == expType);
    }

    @Test
    public void testLELG() {
        Result expType = RangeComparator.Result.LELG;
        blurb(expType);

        Range<Integer> r1 = Range.create(0, true, 10, true);
        Range<Integer> r2 = Range.create(0, true, 20, true);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(0, true, 20, false);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(0, true, 20, true);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(null, false, 10, true);
        r2 = Range.create(null, false, null, false);
        assertTrue(r1.compareTo(r2) == expType);
    }

    @Test
    public void testEGEG() {
        Result expType = RangeComparator.Result.EGEG;
        blurb(expType);

        Range<Integer> r1 = Range.create(10);
        Range<Integer> r2 = Range.create(0, true, 10, true);
        assertTrue(r1.compareTo(r2) == expType);

        r2 = Range.create(null, false, 10, true);
        assertTrue(r1.compareTo(r2) == expType);

        r2 = Range.create(0, true, 10, false);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(0);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(null, Range.INF);
        r2 = Range.create(0, true, null, false);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(null, Range.NEG_INF);
        assertFalse(r1.compareTo(r2) == expType);
    }

    @Test
    public void testLELE() {
        Result expType = RangeComparator.Result.LELE;
        blurb(expType);

        Range<Integer> r1 = Range.create(0);
        Range<Integer> r2 = Range.create(0, true, 10, true);
        assertTrue(r1.compareTo(r2) == expType);

        r2 = Range.create(0, false, 10, true);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(10);
        assertFalse(r1.compareTo(r2) == expType);

        r1 = Range.create(null, Range.NEG_INF);
        r2 = Range.create(null, false, null, false);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(null, Range.INF);
        assertFalse(r1.compareTo(r2) == expType);
    }

    @Test
    public void testEGGG() {
        Result expType = RangeComparator.Result.EGGG;
        blurb(expType);

        Range<Integer> r1 = Range.create(10, true, 20, true);
        Range<Integer> r2 = Range.create(0, true, 10, true);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(10, false, 20, true);
        assertFalse(r1.compareTo(r2) == expType);

        r2 =  Range.create(0, true, 10, false);
        assertFalse(r1.compareTo(r2) == expType);
    }

    @Test
    public void testGGGG() {
        Result expType = RangeComparator.Result.GGGG;
        blurb(expType);

        Range<Integer> r1 = Range.create(20, true, 30, true);
        Range<Integer> r2 = Range.create(0, true, 10, true);
        assertTrue(r1.compareTo(r2) == expType);

        r1 = Range.create(10, true, 30, true);
        assertFalse(r1.compareTo(r2) == expType);

        r2 = Range.create(0, true, 10, false);
        assertTrue(r1.compareTo(r2) == expType);
    }


    private void blurb(RangeComparator.Result t) {
        System.out.println(String.format("   Type.%s (%s): %s",
                t.name(), t.getNotation(), t.getDesc()));
    }
}
