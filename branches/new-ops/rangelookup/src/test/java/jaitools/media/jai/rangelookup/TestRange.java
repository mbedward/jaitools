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

package jaitools.media.jai.rangelookup;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Range class
 *
 * @author Michael Bedward
 */
public class TestRange {

    @Test
    public void testOpenBothEnds() {
        System.out.println("   test with both ends open");

        System.out.println("   - type Byte");
        testOpenBothEnds(Byte.MIN_VALUE, Byte.MAX_VALUE);

        System.out.println("   - type Short");
        testOpenBothEnds(Short.MIN_VALUE, Short.MAX_VALUE);

        System.out.println("   - type Integer");
        testOpenBothEnds(Integer.MIN_VALUE, Integer.MAX_VALUE);

        System.out.println("   - type Float");
        testOpenBothEnds(Float.MIN_VALUE, Float.MAX_VALUE);

        System.out.println("   - type Double");
        testOpenBothEnds(Double.MIN_VALUE, Double.MAX_VALUE);
    }

    @Test
    public void testOpenLowerInclusiveUpper() {
        System.out.println("   test with open lower bound, inclusive upper");

        System.out.println("   - type Byte");
        testOpenLower(Byte.valueOf((byte)10), true, Byte.MIN_VALUE, true);
        testOpenLower(Byte.valueOf((byte)10), true, (byte)10, true);

        System.out.println("   - type Short");
        testOpenLower(Short.valueOf((short)10), true, Short.MIN_VALUE, true);
        testOpenLower(Short.valueOf((short)10), true, (short)10, true);

        System.out.println("   - type Integer");
        testOpenLower(Integer.valueOf(10), true, Integer.MIN_VALUE, true);
        testOpenLower(Integer.valueOf(10), true, 10, true);

        System.out.println("   - type Float");
        testOpenLower(Float.valueOf(10f), true, Float.MIN_VALUE, true);
        testOpenLower(Float.valueOf(10f), true, 10f, true);

        System.out.println("   - type Double");
        testOpenLower(Double.valueOf(10d), true, Double.MIN_VALUE, true);
        testOpenLower(Double.valueOf(10d), true, 10d, true);
    }

    @Test
    public void testOpenLowerExclusiveUpper() {
        System.out.println("   test with open lower bound, exclusive upper");

        System.out.println("   - type Byte");
        testOpenLower(Byte.valueOf((byte)10), false, Byte.MIN_VALUE, true);
        testOpenLower(Byte.valueOf((byte)10), false, (byte)10, false);

        System.out.println("   - type Short");
        testOpenLower(Short.valueOf((short)10), false, Short.MIN_VALUE, true);
        testOpenLower(Short.valueOf((short)10), false, (short)10, false);

        System.out.println("   - type Integer");
        testOpenLower(Integer.valueOf(10), false, Integer.MIN_VALUE, true);
        testOpenLower(Integer.valueOf(10), false, 10, false);

        System.out.println("   - type Float");
        testOpenLower(Float.valueOf(10f), false, Float.MIN_VALUE, true);
        testOpenLower(Float.valueOf(10f), false, 10f, false);

        System.out.println("   - type Double");
        testOpenLower(Double.valueOf(10d), false, Double.MIN_VALUE, true);
        testOpenLower(Double.valueOf(10d), false, 10d, false);
    }

    @Test
    public void testOpenUpperInclusiveLower() {
        System.out.println("   test with open upper bound, inclusive lower");

        System.out.println("   - type Byte");
        testOpenUpper(Byte.valueOf((byte)-10), true, Byte.MAX_VALUE, true);
        testOpenUpper(Byte.valueOf((byte)-10), true, (byte)-10, true);

        System.out.println("   - type Short");
        testOpenUpper(Short.valueOf((short)-10), true, Short.MAX_VALUE, true);
        testOpenUpper(Short.valueOf((short)-10), true, (short)-10, true);

        System.out.println("   - type Integer");
        testOpenUpper(Integer.valueOf(-10), true, Integer.MAX_VALUE, true);
        testOpenUpper(Integer.valueOf(-10), true, -10, true);

        System.out.println("   - type Float");
        testOpenUpper(Float.valueOf(-10f), true, Float.MAX_VALUE, true);
        testOpenUpper(Float.valueOf(-10f), true, -10f, true);

        System.out.println("   - type Double");
        testOpenUpper(Double.valueOf(-10d), true, Double.MAX_VALUE, true);
        testOpenUpper(Double.valueOf(-10d), true, -10d, true);
    }

    public void testOpenUpperExclusiveLower() {
        System.out.println("   test with open upper bound, exclusive lower");

        System.out.println("   - type Byte");
        testOpenUpper(Byte.valueOf((byte)-10), false, Byte.MAX_VALUE, true);
        testOpenUpper(Byte.valueOf((byte)-10), false, (byte)-10, false);

        System.out.println("   - type Short");
        testOpenUpper(Short.valueOf((short)-10), false, Short.MAX_VALUE, true);
        testOpenUpper(Short.valueOf((short)-10), false, (short)-10, false);

        System.out.println("   - type Integer");
        testOpenUpper(Integer.valueOf(-10), false, Integer.MAX_VALUE, true);
        testOpenUpper(Integer.valueOf(-10), false, -10, false);

        System.out.println("   - type Float");
        testOpenUpper(Float.valueOf(-10f), false, Float.MAX_VALUE, true);
        testOpenUpper(Float.valueOf(-10f), false, -10f, false);

        System.out.println("   - type Double");
        testOpenUpper(Double.valueOf(-10d), false, Double.MAX_VALUE, true);
        testOpenUpper(Double.valueOf(-10d), false, -10d, false);
    }


    <T extends Number & Comparable> void testOpenBothEnds(T val1, T val2) {
        Range<T> r = new Range<T>(null, true, null, true);
        assertTrue(r.contains(val1));
        assertTrue(r.contains(val2));
    }

    <T extends Number & Comparable> void testOpenLower(
            T max, boolean maxInc, T val, boolean expValue) {

        Range<T> r = new Range<T>(null, true, max, maxInc);
        assertTrue(r.contains(val) == expValue);
    }

    <T extends Number & Comparable> void testOpenUpper(
            T min, boolean minInc, T val, boolean expValue) {

        Range<T> r = new Range<T>(min, minInc, null, true);
        assertTrue(r.contains(val) == expValue);
    }

}