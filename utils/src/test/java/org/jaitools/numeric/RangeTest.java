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

package org.jaitools.numeric;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Michael Bedward
 */
public class RangeTest {
    
    private static final int ivalue = 42;

    @Test
    public void testCreateInterval() {
        System.out.println("   testCreateInterval");


        Range<Integer> r = Range.create(null, false, ivalue, true);
        assertTrue(r.isMinOpen());
        assertTrue(r.isMinNegInf());
        assertNull(r.getMin());

        assertFalse(r.isMaxOpen());
        assertFalse(r.isMaxInf());
        assertNotNull(r.getMax());
        assertEquals(ivalue, r.getMax().intValue());
    }
    
    @Test
    public void testValueInRange() {
        System.out.println("   testValueInRange");
        
        Range<Integer> r = Range.create(ivalue / 2, true, 2 * ivalue, true);
        assertTrue(r.contains(ivalue));
    }
    
    @Test
    public void testValueOutOfRange() {
        System.out.println("   testValueOutOfRange");
        
        Range<Integer> r = Range.create(ivalue + 1, true, 2 * ivalue, true);
        assertFalse(r.contains(ivalue));
    }
    
    @Test
    public void testValueAtIncludedEndpoint() {
        System.out.println("   testValueAtIncludedEndpoint");
        
        Range<Integer> r = Range.create(ivalue, true, 2 * ivalue, true);
        assertTrue(r.contains(ivalue));
    }
    
    @Test
    public void testValueAtExcludedEndpoint() {
        System.out.println("   testValueAtExcludedEndpoint");
        
        Range<Integer> r = Range.create(ivalue, false, 2 * ivalue, true);
        assertFalse(r.contains(ivalue));
    }

    @Test
    public void testCreateInfiniteInterval() {
        System.out.println("   testCreateInfiniteInterval");
        Range<Integer> r1 = Range.create(null, true, null, true);
        assertTrue(r1.isMinNegInf());
        assertTrue(r1.isMaxInf());
    }

    @Test
    public void testEquals() {
        System.out.println("   testEquals");
        
        Range<Integer> r = Range.create(-10, true, 10, false);
        Range<Integer> same = Range.create(-10, true, 10, false);
        Range<Integer> different = Range.create(-10, false, 10, true);

        assertTrue(r.equals(same));
        assertFalse(r.equals(different));
    }

    @Test
    public void testInfinitePointNEQInfiniteInterval() {
        System.out.println("   testInfinitePointNEQInfiniteInterval");

        Range<Integer> p = Range.create(null, Range.INF);
        Range<Integer> r = Range.create(null, false, null, false);

        assertFalse(p.equals(r));
        assertFalse(r.equals(p));
    }

    @Test
    public void testFinitePointToString() {
        System.out.println("   testPointToString");

        Range<Integer> r = Range.create(10);
        assertEquals("[10]", r.toString());
    }

    @Test
    public void testInfinitePointToString() {
        System.out.println("   testInfinitePointToString");

        Range<Integer> r = Range.create(null, Range.INF);
        assertEquals("(Inf)", r.toString());

        r = Range.create(null, Range.NEG_INF);
        assertEquals("(-Inf)", r.toString());
    }
    
    @Test
    public void testFiniteIntervalToString() {
        System.out.println("   testFiniteIntervalToString");

        Range<Integer> r = Range.create(-10, true, 10, false);
        assertEquals("[-10, 10)", r.toString());
    }

    @Test
    public void testOpenIntervalToString() {
        System.out.println("   testOpenIntervalToString");

        Range<Integer> r = Range.create(null, true, 10, true);
        assertEquals("(-Inf, 10]", r.toString());
    }
}
