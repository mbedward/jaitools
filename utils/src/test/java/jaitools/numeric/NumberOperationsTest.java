/*
 * Copyright 2009 Michael Bedward
 *
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jaitools.numeric;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests of NumberOperations methods
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL: https://jai-tools.googlecode.com/svn/trunk/utils/src/test/java/jaitools/numeric/NumberOperationsTest.java $
 * @version $Id: NumberOperationsTest.java 1173 2010-04-16 10:48:48Z michael.bedward $
 */
public class NumberOperationsTest {

    @Test
    public void testAddByteInteger() {
        System.out.println("   add(Byte, Integer)");

        Number n1 = Byte.valueOf((byte)200);
        Number n2 = Integer.valueOf(0);

        Number result = NumberOperations.add(n1, n2);
        assertTrue(result.intValue() == 200);
        assertTrue(result instanceof Integer);
    }

    @Test
    public void testSubtractByteInteger() {
        System.out.println("   subtract(Byte, Integer)");

        Number n1 = Byte.valueOf((byte)200);
        Number n2 = Integer.valueOf(100);

        Number result = NumberOperations.subtract(n1, n2);
        assertTrue(result.intValue() == 100);
        assertTrue(result instanceof Integer);
    }

    @Test
    public void testMultiplyByteInteger() {
        System.out.println("   multiply(Byte, Integer)");

        Number n1 = Byte.valueOf((byte)200);
        Number n2 = Integer.valueOf(100);

        Number result = NumberOperations.multiply(n1, n2);
        assertTrue(result.intValue() == 20000);
        assertTrue(result instanceof Integer);
    }

    @Test
    public void DivideByteInteger() {
        System.out.println("   divide(Byte, Integer)");

        Number n1 = Byte.valueOf((byte)200);
        Number n2 = Integer.valueOf(100);

        Number result = NumberOperations.divide(n1, n2);
        assertTrue(result.intValue() == 2);
        assertTrue(result instanceof Integer);
    }

    @Test
    public void testNewInstanceByte() {
        System.out.println("   newInstance Byte");

        int i = 200;
        Integer iobj = Integer.valueOf(i);

        Number result = NumberOperations.newInstance(iobj, Byte.class);
        assertTrue((result.intValue() & 0xff) == i);
        assertTrue(result instanceof Byte);
    }

    @Test
    public void testNewInstanceShort() {
        System.out.println("   newInstance Short");

        int i = 200;
        Integer iobj = Integer.valueOf(i);

        Number result = NumberOperations.newInstance(iobj, Short.class);
        assertTrue(result.intValue() == i);
        assertTrue(result instanceof Short);
    }

    @Test
    public void testNewInstanceInteger() {
        System.out.println("   newInstance Integer");

        int i = 200;
        Integer iobj = Integer.valueOf(i);

        Number result = NumberOperations.newInstance(iobj, Integer.class);
        assertTrue(result.intValue() == i);
        assertTrue(result instanceof Integer);
    }

    @Test
    public void testNewInstanceLong() {
        System.out.println("   newInstance Long");

        int i = 200;
        Integer iobj = Integer.valueOf(i);

        Number result = NumberOperations.newInstance(iobj, Long.class);
        assertTrue(result.longValue() == i);
        assertTrue(result instanceof Long);
    }

    @Test
    public void testNewInstanceFloat() {
        System.out.println("   newInstance Float");

        int i = 200;
        Integer iobj = Integer.valueOf(i);

        Number result = NumberOperations.newInstance(iobj, Float.class);
        assertTrue(result.intValue() == i);
        assertTrue(result instanceof Float);
    }

    @Test
    public void testNewInstanceDouble() {
        System.out.println("   newInstance Double");

        int i = 200;
        Integer iobj = Integer.valueOf(i);

        Number result = NumberOperations.newInstance(iobj, Double.class);
        assertTrue(result.intValue() == i);
        assertTrue(result instanceof Double);
    }

    @Test
    public void testHighestClass() {
        System.out.println("   highestClass");

        Number[] numbers = {
            Byte.valueOf((byte)0),
            Short.valueOf((short)0),
            Integer.valueOf(0),
            Long.valueOf(0),
            Float.valueOf(0),
            Double.valueOf(0)
        };

        for (int i = 0; i < numbers.length; i++) {
            for (int j = 0; j <= i; j++) {
                Class<? extends Number> clazz = NumberOperations.highestClass(numbers[i], numbers[j]);
                assertTrue( clazz.isInstance(numbers[i]) );
            }
        }
    }

    @Test
    public void testLowestClass() {
        System.out.println("   lowestClass");

        Number[] numbers = {
            Byte.valueOf((byte)0),
            Short.valueOf((short)0),
            Integer.valueOf(0),
            Long.valueOf(0),
            Float.valueOf(0),
            Double.valueOf(0)
        };

        for (int i = 0; i < numbers.length; i++) {
            for (int j = 0; j <= i; j++) {
                Class<? extends Number> clazz = NumberOperations.lowestClass(numbers[i], numbers[j]);
                assertTrue( clazz.isInstance(numbers[j]) );
            }
        }
    }

    @Test
    public void testIntegralComparison() {
        System.out.println("   testIntegralComparison");

        Number a = Integer.valueOf(1);
        Number b = Integer.valueOf(2);

        assertEquals(0, NumberOperations.compare(a, a));
        assertEquals(-1, NumberOperations.compare(a, b));
        assertEquals(1, NumberOperations.compare(b, a));
    }

    @Test
    public void testFloatComparison() {
        System.out.println("   testFloatComparison");

        Number a = Float.valueOf(1);
        Number b = Float.valueOf(2);

        assertEquals(0, NumberOperations.compare(a, a));
        assertEquals(-1, NumberOperations.compare(a, b));
        assertEquals(1, NumberOperations.compare(b, a));
    }

    @Test
    public void testFloatComparisonSpecialValues() {
        System.out.println("   testFloatComparisonSpecialValues");

        assertEquals(0, NumberOperations.compare(Float.NaN, Float.NaN));
        assertEquals(1, NumberOperations.compare(Float.NaN, Float.POSITIVE_INFINITY));
        assertEquals(-1, NumberOperations.compare(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY));
    }

    @Test
    public void testDoubleComparison() {
        System.out.println("   testDoubleComparison");

        Number a = Double.valueOf(1);
        Number b = Double.valueOf(2);

        assertEquals(0, NumberOperations.compare(a, a));
        assertEquals(-1, NumberOperations.compare(a, b));
        assertEquals(1, NumberOperations.compare(b, a));
    }

    @Test
    public void testDoubleComparisonSpecialValues() {
        System.out.println("   testDoubleComparisonSpecialValues");

        assertEquals(0, NumberOperations.compare(Double.NaN, Double.NaN));
        assertEquals(1, NumberOperations.compare(Double.NaN, Double.POSITIVE_INFINITY));
        assertEquals(-1, NumberOperations.compare(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
    }

}