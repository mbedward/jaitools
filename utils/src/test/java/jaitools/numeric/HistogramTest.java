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

package jaitools.numeric;


import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@code Histogram}.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class HistogramTest {

    Histogram<Double> hist;

    @Before
    public void setupBinner() {
        hist = new Histogram<Double>();
    }

    @Test
    public void testWithNoBinsDefined() {
        assertEquals(Histogram.NO_BIN, hist.getBinForValue(0.0));
    }

    @Test
    public void testWithSingleBin() {
        final double lower = 10;
        final double upper = 20;
        Range<Double> r = Range.create(lower, true, upper, false);
        hist.addBin(r);

        assertEquals(0, hist.getBinForValue(10.0));
        assertEquals(Histogram.NO_BIN, hist.getBinForValue(20.0));
    }

    @Test
    public void testOverlappingBinsThrowsException() {
        hist.addBin(Range.create(0.0, true, 0.1, true));

        try {
            // this bin overlap the first bin
            hist.addBin(Range.create(0.1, true, 0.2, true));
            fail("Expected an exception");
            
        } catch (HistogramException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Test
    public void testBinsAddedOutOfOrder() {
        hist.addBin(Range.create(0.2, true, 0.3, false));
        hist.addBin(Range.create(0.1, true, 0.2, false));
        hist.addBin(Range.create(0.0, true, 0.1, false));

        final double d = 0.1;
        for (int i = 0; i < 3; i++) {
            assertEquals(i, hist.getBinForValue(d * i));
        }
    }

    @Test
    public void testAddValue() {
        hist.addBin(Range.create(0.1, true, 0.2, false));
        hist.addBin(Range.create(0.2, true, 0.3, false));
        hist.addBin(Range.create(0.3, true, 0.4, false));

        hist.addValue(0.0);  // no bin
        hist.addValue(0.1);  // bin 0
        hist.addValue(0.15); // bin 0
        hist.addValue(0.2);  // bin 1
        hist.addValue(0.25); // bin 1
        hist.addValue(0.25); // bin 1
        hist.addValue(0.5);  // no bin

        assertEquals(Arrays.asList(2, 3, 0), hist.getCounts());
    }

}
