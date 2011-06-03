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

package jaitools.media.jai.rangelookup;

import jaitools.numeric.Range;

/**
 * Base class for unit tests.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public abstract class TestBase {
    /**
     * Creates a lookup table.
     * @param breaks array of breakpoints for source image values
     * @param values array of lookup values for destination image value
     * 
     * @return the lookup table
     */
    protected <T extends Number & Comparable<? super T>, 
             U extends Number & Comparable<? super U>> 
            RangeLookupTable<T, U> createTable(T[] breaks, U[] values) {
        
        final int N = breaks.length;
        if (values.length != N + 1) {
            throw new IllegalArgumentException(
                    "values array length should be breaks array length + 1");
        }
        
        RangeLookupTable<T, U> table = new RangeLookupTable<T, U>();
        Range<T> r;
        
        r = Range.create(null, false, breaks[0], false);
        table.add(r, values[0]);
        
        for (int i = 1; i < N; i++) {
            r = Range.create(breaks[i-1], true, breaks[i], false);
            table.add(r, values[i]);
        }
        
        r = Range.create(breaks[N-1], true, null, false);
        table.add(r, values[N]);
        
        return table;
    }

}
