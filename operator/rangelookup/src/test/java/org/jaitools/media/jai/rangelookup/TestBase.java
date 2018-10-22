/* 
 *  Copyright (c) 2011-13, Michael Bedward. All rights reserved. 
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
import java.lang.reflect.Array;

import javax.media.jai.JAI;
import javax.media.jai.util.ImagingListener;

import org.jaitools.imageutils.ImageDataType;
import org.jaitools.imageutils.ImageUtils;
import org.jaitools.numeric.NumberOperations;
import org.jaitools.numeric.Range;
import org.junit.BeforeClass;

/**
 * Base class for unit tests.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public abstract class TestBase {
    @BeforeClass
    public static void quiet() {
    	JAI jai = JAI.getDefaultInstance();
		final ImagingListener imagingListener = jai.getImagingListener();
    	if( imagingListener == null || imagingListener.getClass().getName().contains("ImagingListenerImpl")) {
    		jai.setImagingListener( new ImagingListener() {
				@Override
				public boolean errorOccurred(String message, Throwable thrown, Object where, boolean isRetryable)
						throws RuntimeException {
					if (message.contains("Continuing in pure Java mode")) {
						return false;
					}
					return imagingListener.errorOccurred(message, thrown, where, isRetryable);
				}    			
    		});
    	}
    }
    
    /**
     * Creates a lookup table.
     * @param breaks array of breakpoints for source image values
     * @param values array of lookup values for destination image value
     * 
     * @return the lookup table
     */
    protected <T extends Number & Comparable<? super T>, 
             U extends Number & Comparable<? super U>> 
            RangeLookupTable<T, U> createTableFromBreaks(T[] breaks, U[] values) {
        
        final int N = breaks.length;
        if (values.length != N + 1) {
            throw new IllegalArgumentException(
                    "values array length should be breaks array length + 1");
        }
        
        RangeLookupTable.Builder<T, U> builder = new RangeLookupTable.Builder<T, U>();
        Range<T> r;
        
        r = Range.create(null, false, breaks[0], false);
        builder.add(r, values[0]);
        
        for (int i = 1; i < N; i++) {
            r = Range.create(breaks[i-1], true, breaks[i], false);
            builder.add(r, values[i]);
        }
        
        r = Range.create(breaks[N-1], true, null, false);
        builder.add(r, values[N]);
        
        return builder.build();
    }
    
    /**
     * Creates a test image with sequential values.
     * 
     * @param startVal min image value
     * @param data array to fill and use as pixel values
     * 
     * @return  the test image
     */
    protected RenderedImage createTestImage(
            Number startVal, 
            ImageDataType dataType, 
            int width, int height) {
        
        Number value = startVal;
        Number delta = NumberOperations.newInstance(1, startVal.getClass());
        
        Number[] data = (Number[]) Array.newInstance(dataType.getDataClass(), width * height);
        
        for (int i = 0; i < data.length; i++) {
            data[i] = value;
            value = NumberOperations.add(value, delta);
        }

        return ImageUtils.createImageFromArray(data, width, height);
    }

}
