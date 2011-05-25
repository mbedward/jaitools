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
 * @version $Id$
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
    public void copy() {
        System.out.println("   copy");
        Number[] numbers = {
            Byte.valueOf((byte) 0),
            Short.valueOf((short) 0),
            Integer.valueOf(0),
            Float.valueOf(0f),
            Double.valueOf(0d)
        };

        for (int i = 0; i < numbers.length; i++) {
            Number result = NumberOperations.copy(numbers[i]);
            assertEquals(numbers[i].getClass(), result.getClass());
            assertEquals(numbers[i].intValue(), result.intValue());
        }
    }

    @Test
    public void copyWithNull() {
        System.out.println("   copy with null arg");
        Number result = NumberOperations.copy(null);
        assertNull(result);
    }

    @Test
    public void newInstance() {
        // Byte to Byte
        doNewInstance(Byte.valueOf((byte) 42), Byte.class, Byte.valueOf((byte) 42));

        // Short to Byte
        doNewInstance(Short.valueOf((short) 42), Byte.class, Byte.valueOf((byte) 42));

        // Short to Byte with clamping
        doNewInstance(Short.valueOf((short) 420), Byte.class, Byte.valueOf((byte) 255));

        // Short to Byte with clamping
        doNewInstance(Short.valueOf((short) -42), Byte.class, Byte.valueOf((byte) 0));

        // Integer to Byte with no clamping
        doNewInstance(Integer.valueOf(42), Byte.class, Byte.valueOf((byte) 42));

        // Integer to Byte with clamping
        doNewInstance(Integer.valueOf(420), Byte.class, Byte.valueOf((byte) 255));

        // Integer to Byte with clamping
        doNewInstance(Integer.valueOf(-42), Byte.class, Byte.valueOf((byte) 0));

        // Long to Byte with no clamping
        doNewInstance(Long.valueOf(42), Byte.class, Byte.valueOf((byte) 42));

        // Long to Byte with clamping
        doNewInstance(Long.valueOf(420), Byte.class, Byte.valueOf((byte) 255));

        // Long to Byte with clamping
        doNewInstance(Long.valueOf(-42), Byte.class, Byte.valueOf((byte) 0));

        // Float to Byte with no clamping
        doNewInstance(Float.valueOf(42.0f), Byte.class, Byte.valueOf((byte) 42));

        // Float to Byte with clamping
        doNewInstance(Float.valueOf(420.0f), Byte.class, Byte.valueOf((byte) 255));

        // Float to Byte with clamping
        doNewInstance(Float.valueOf(-42.0f), Byte.class, Byte.valueOf((byte) 0));

        // Double to Byte with no clamping
        doNewInstance(Double.valueOf(42.0d), Byte.class, Byte.valueOf((byte) 42));
        
        // Double to Byte with clamping
        doNewInstance(Double.valueOf(420.0d), Byte.class, Byte.valueOf((byte) 255));

        // Double to Byte with clamping
        doNewInstance(Double.valueOf(-42.0d), Byte.class, Byte.valueOf((byte) 0));

        // Byte to Short
        doNewInstance(Byte.valueOf((byte) 42), Short.class, Short.valueOf((short) 42));
        
        // Short to Short
        doNewInstance(Short.valueOf((short) 42), Short.class, Short.valueOf((short) 42));

        // Integer to Short with no clamping
        doNewInstance(Integer.valueOf(42), Short.class, Short.valueOf((short) 42));
        
        // Integer to Short with clamping
        doNewInstance(Integer.valueOf(Short.MAX_VALUE + 1), Short.class,
                Short.valueOf(Short.MAX_VALUE));

        // Integer to Short with clamping
        doNewInstance(Integer.valueOf(Short.MIN_VALUE - 1), Short.class,
                Short.valueOf(Short.MIN_VALUE));

        // Long to Short with no clamping
        doNewInstance(Long.valueOf(42), Short.class, Short.valueOf((short) 42));
        
        // Long to Short with clamping
        doNewInstance(Long.valueOf(Short.MAX_VALUE + 1), Short.class,
                Short.valueOf(Short.MAX_VALUE));

        // Long to Short with clamping
        doNewInstance(Long.valueOf(Short.MIN_VALUE - 1), Short.class,
                Short.valueOf(Short.MIN_VALUE));

        // Float to Short with no clamping
        doNewInstance(Float.valueOf(42.0f), Short.class, Short.valueOf((short) 42));
        
        // Float to Short with clamping
        doNewInstance(Float.valueOf(Short.MAX_VALUE + 1), Short.class,
                Short.valueOf(Short.MAX_VALUE));

        // Float to Short with clamping
        doNewInstance(Float.valueOf(Short.MIN_VALUE - 1), Short.class,
                Short.valueOf(Short.MIN_VALUE));

        // Double to Short with no clamping
        doNewInstance(Double.valueOf(42.0d), Short.class, Short.valueOf((short) 42));
        
        // Double to Short with clamping
        doNewInstance(Double.valueOf(Short.MAX_VALUE + 1), Short.class,
                Short.valueOf(Short.MAX_VALUE));

        // Double to Short with clamping
        doNewInstance(Double.valueOf(Short.MIN_VALUE - 1), Short.class,
                Short.valueOf(Short.MIN_VALUE));

        // Byte to Integer
        doNewInstance(Byte.valueOf((byte) 42), Integer.class, Integer.valueOf(42));
        
        // Short to Integer
        doNewInstance(Short.valueOf((short) 42), Integer.class, Integer.valueOf(42));

        // Integer to Integer
        doNewInstance(Integer.valueOf(42), Integer.class, Integer.valueOf(42));
        
        // Long to Integer with no clamping
        doNewInstance(Long.valueOf(42), Integer.class, Integer.valueOf(42));
        
        // Long to Integer with clamping
        doNewInstance(Long.valueOf((long)Integer.MAX_VALUE + 1), Integer.class,
                Integer.valueOf(Integer.MAX_VALUE));

        // Long to Integer with clamping
        doNewInstance(Long.valueOf((long)Integer.MIN_VALUE - 1), Integer.class,
                Integer.valueOf(Integer.MIN_VALUE));

        // Float to Integer with no clamping
        doNewInstance(Float.valueOf(42.0f), Integer.class, Integer.valueOf(42));
        
        // Float to Integer with clamping
        doNewInstance(Float.valueOf((float)Integer.MAX_VALUE + 1), Integer.class,
                Integer.valueOf(Integer.MAX_VALUE));

        // Float to Integer with clamping
        doNewInstance(Float.valueOf((float)Integer.MIN_VALUE - 1), Integer.class,
                Integer.valueOf(Integer.MIN_VALUE));

        // Double to Integer with no clamping
        doNewInstance(Double.valueOf(42.0d), Integer.class, Integer.valueOf(42));
        
        // Double to Integer with clamping
        doNewInstance(Double.valueOf((double)Integer.MAX_VALUE + 1), Integer.class,
                Integer.valueOf(Integer.MAX_VALUE));

        // Double to Integer with clamping
        doNewInstance(Double.valueOf((double)Integer.MIN_VALUE - 1), Integer.class,
                Integer.valueOf(Integer.MIN_VALUE));

        // Byte to Float
        doNewInstance(Byte.valueOf((byte) 42), Float.class, Float.valueOf(42));
        
        // Short to Float
        doNewInstance(Short.valueOf((short) 42), Float.class, Float.valueOf(42));

        // Integer to Float
        doNewInstance(Integer.valueOf(42), Float.class, Float.valueOf(42));
        
        // Long to Float 
        doNewInstance(Long.valueOf(42), Float.class, Float.valueOf(42));
        
        // Float to Float
        doNewInstance(Float.valueOf(42.0f), Float.class, Float.valueOf(42));
        
        // Double to Float with no clamping
        doNewInstance(Double.valueOf(42.0d), Float.class, Float.valueOf(42));
        
        // Double to Float with clamping
        doNewInstance(Double.valueOf((double)Float.MAX_VALUE + 1), Float.class,
                Float.valueOf(Float.MAX_VALUE));

        // Double to Float with clamping
        doNewInstance(Double.valueOf((double)Float.MIN_VALUE - 1), Float.class,
                Float.valueOf(Float.MIN_VALUE));
    }

    public void doNewInstance(Number number, Class<? extends Number> clazz, Number expected) {
        System.out.println("   newInstance " + number.getClass().getSimpleName() +
                " to " + clazz.getSimpleName());

        Number result = NumberOperations.newInstance(number, clazz);

        assertTrue("wrong class: expected " + expected.getClass().getSimpleName() +
                " got " + result.getClass().getSimpleName(), 
                result.getClass().equals(clazz));
        
        assertTrue("wrong value: expected " + expected + " got " + result,
                expected.equals(result));
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
