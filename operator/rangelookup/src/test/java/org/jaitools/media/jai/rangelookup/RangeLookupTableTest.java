/* 
 *  Copyright (c) 2011, Michael Bedward. All rights reserved. 
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

package org.jaitools.media.jai.rangelookup;

import org.jaitools.numeric.Range;

import org.junit.Test;
import static org.junit.Assert.*;


/**
 * Tests for RangeLookupTable. We don't do comprehensive testing of source
 * and lookup data types here because that is tested as part of the image
 * lookup tests in {@link RangeLookupTest}.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class RangeLookupTableTest extends TestBase {
    
    @Test
    public void simpleLookup() throws Exception {
        System.out.println("   simple integer lookup");
        
        Integer[] breaks = { -10, -5, 0, 5, 10 };
        Integer[] values = { -99, -1, 0, 1, 2, 99 };
        RangeLookupTable<Integer, Integer> table = createTable(breaks, values);

        final int N = breaks.length;
        final int startVal = breaks[0] - 1;
        final int endVal = breaks[N-1] + 1;
        
        int k = 0;
        int expected = values[0];
        for (int val = startVal; val <= endVal; val++) {
            if (val >= breaks[k]) {
                expected = values[k+1];
                if (k < N-1) k++ ;
            }
            assertEquals(expected, table.getDestValue(val).intValue());
        }
    }
    
    @Test
    public void defaultValue() throws Exception {
        System.out.println("   default lookup value");
        
        RangeLookupTable<Integer, Integer> table = new RangeLookupTable<Integer, Integer>(-1);
        table.add(Range.create(5, false, 15, false), 1);
        
        for (int val = 0; val <= 20; val++) {
            int expected = val > 5 && val < 15 ? 1 : -1; 
            int destVal = table.getDestValue(val);
            assertEquals(expected, destVal);
        }
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void defaultValueDisabled() throws Exception {
        System.out.println("   default lookup value disabled");
        
        RangeLookupTable<Integer, Integer> table = new RangeLookupTable<Integer, Integer>();
        table.add(Range.create(0, false, null, false), 1);

        table.getDestValue(0);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void disallowOverlappingRange() throws Exception {
        System.out.println("   throw exception on overlapping ranges");
        RangeLookupTable<Integer, Integer> table = new RangeLookupTable<Integer, Integer>();
        table.setOverlapAllowed(false);
        
        // lookup ranges that overlap between -10 and 10
        table.add(Range.create(null, false, 10, true), 1);
        table.add(Range.create(-10, true, null, false), 2);
    }
    
    @Test
    public void addOverlappedRange1() throws Exception {
        System.out.println("   add overlapping range");
        RangeLookupTable<Integer, Integer> table = new RangeLookupTable<Integer, Integer>();
        table.setOverlapAllowed(true);
        
        table.add(Range.create(5, true, 10, true), 1);
        
        // this range is overlapped by the first range
        table.add(Range.create(0, true, 20, true), 2);
        
        /*
         * The table should now be:
         *   [0, 5) => 2
         *   [5, 10] => 1
         *   (10, 20] => 2
         */
        for (int val = 0; val <= 20; val++) {
            int expected = val < 5 || val > 10 ? 2 : 1; 
            int destVal = table.getDestValue(val);
            assertEquals(expected, destVal);
        }
    }

    @Test
    public void addCompletelyOverlappedRange() throws Exception {
        System.out.println("   add completely overlapped range");
        RangeLookupTable<Integer, Integer> table = new RangeLookupTable<Integer, Integer>();
        table.setOverlapAllowed(true);

        table.add(Range.create(0, true, 20, true), 1);

        // this range is overlapped by the first range
        table.add(Range.create(5, true, 10, true), 2);

        for (int val = 0; val <= 20; val++) {
            int destVal = table.getDestValue(val);
            assertEquals(1, destVal);
        }
    }
}
