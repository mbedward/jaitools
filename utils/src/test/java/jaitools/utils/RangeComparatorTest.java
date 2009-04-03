/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.utils;

import jaitools.utils.RangeComparator.Result;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test each of the 18 comparison possibilities presented in
 * Hayes (2003) Figure 3.
 *
 * @author Michael Bedward
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