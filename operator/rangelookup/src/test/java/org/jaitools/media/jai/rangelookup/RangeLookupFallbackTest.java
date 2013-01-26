/* 
 *  Copyright (c) 2013, Michael Bedward. All rights reserved. 
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

import java.awt.image.RenderedImage;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import org.jaitools.imageutils.ImageDataType;
import org.jaitools.imageutils.iterator.SimpleIterator;
import org.jaitools.numeric.Range;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests RangeLookup for unmatched source values.
 * 
 * @author Michael Bedward
 */
public class RangeLookupFallbackTest extends TestBase {
    
    private static final int WIDTH = 10;
    private static final int MATCH_VALUE = 1;
    private static final int DEFAULT_VALUE = 2;

    private RangeLookupTable.Builder<Integer, Integer> builder;

    @Before
    public void setup() {
        builder = new RangeLookupTable.Builder<Integer, Integer>();
    }
    
    
    @Test
    public void defaultValue() throws Exception {
        assertLookup(true);
    }
    
    @Test
    public void passThroughSourceValue() throws Exception {
        assertLookup(false);
    }
    
    
    private void assertLookup(boolean useDefault) {
        int minValue = 0;
        int maxValue = WIDTH * WIDTH;
        int third = (maxValue - minValue) / 3;
        
        RenderedImage srcImage = createTestImage(
                Integer.valueOf(minValue), ImageDataType.INT, WIDTH, WIDTH);

        // lookup table with gap in middle third of source value range
        builder.add(Range.create(minValue, true, minValue + third, true), MATCH_VALUE);
        builder.add(Range.create(maxValue - third, true, maxValue, true), MATCH_VALUE);
        RangeLookupTable<Integer, Integer> table = builder.build();
        
        ParameterBlockJAI pb = new ParameterBlockJAI("RangeLookup");
        pb.setSource("source0", srcImage);
        pb.setParameter("table", table);
        
        if (useDefault) {
            pb.setParameter("default", DEFAULT_VALUE);
        }
        
        RenderedOp destImage = JAI.create("RangeLookup", pb);
        
        SimpleIterator srcIter = new SimpleIterator(srcImage, null, null);
        SimpleIterator destIter = new SimpleIterator(destImage, null, null);
        do {
            int srcValue = srcIter.getSample().intValue();
            int destValue = destIter.getSample().intValue();
            
            LookupItem<Integer, Integer> item = table.getLookupItem(srcValue);
            if (item != null) {
                assertEquals(MATCH_VALUE, destValue);
                
            } else if (useDefault) {
                assertEquals(DEFAULT_VALUE, destValue);
                
            } else { // pass-through source values
                assertEquals(srcValue, destValue);
            }
            
        } while (srcIter.next() && destIter.next());
    }
}
