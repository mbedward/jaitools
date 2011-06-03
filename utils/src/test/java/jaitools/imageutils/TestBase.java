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

package jaitools.imageutils;

import java.awt.Rectangle;
import java.util.Arrays;
import javax.media.jai.TiledImage;

/**
 *
 * @author michael
 */
public abstract class TestBase {
    
    
    /**
     * Creates an image filled with sequential integer values, ordered by pixel, line, band.
     * 
     * @param width image width
     * @param height image height
     * @param numBands number of bands
     * 
     * @return the new image
     */
    protected TiledImage createSequentialImage(final int width, final int height, 
            final int numBands) {
        return createSequentialImage(width, height, numBands, 0);
    }

    /**
     * Creates an image filled with sequential integer values, ordered by pixel, line, band.
     * 
     * @param width image width
     * @param height image height
     * @param numBands number of bands
     * @param startValue the starting (minimum) value at image location (0,0,0)
     * 
     * @return the new image
     */
    protected TiledImage createSequentialImage(final int width, final int height, 
            final int numBands, final int startValue) {
        return createSequentialImage(0, 0, width, height, numBands, startValue);
    }

    /**
     * Creates an image filled with sequential integer values, ordered by pixel, line, band.
     * 
     * @param width image width
     * @param height image height
     * @param numBands number of bands
     * @param startValue the starting (minimum) value at image location (0,0,0)
     * 
     * @return the new image
     */
    protected TiledImage createSequentialImage(final int minx, final int miny,
            final int width, final int height, 
            final int numBands, final int startValue) {

        return createSequentialTiledImage(
                minx, miny, width, height, width, height, numBands, startValue);
    }

    protected TiledImage createSequentialTiledImage(
            final int minx, final int miny, final int width, final int height,
            final int tileWidth, final int tileHeight, final int numBands,
            final int startValue) {
        
        Integer[] fillValues = new Integer[numBands];
        Arrays.fill(fillValues, Integer.valueOf(0));
        
        TiledImage img = ImageUtils.createConstantImage(
                minx, miny, width, height, tileWidth, tileHeight, fillValues);
        
        int k = startValue;
        for (int b = 0; b < numBands; b++) {
            for (int y = miny, iy = 0; iy < height; y++, iy++) {
                for (int x = minx, ix = 0; ix < width; x++, ix++) {
                    img.setSample(x, y, b, k++);
                }
            }
        }

        return img;
    }
    
    protected Rectangle createAdjustedBounds(Rectangle src, int delta) {
        Rectangle dest = new Rectangle(
                src.x - delta, src.y - delta,
                src.width + 2 * delta, src.height + 2 * delta);

        return dest;
    }

    protected void moveToEnd(AbstractSimpleIterator iter) {
        if (iter == null) {
            throw new IllegalStateException("You forgot to create the iterator first");
        }
        
        Rectangle bounds = iter.getBounds();
        int n = bounds.width * bounds.height;
        while (n > 0) {
            iter.next();
            n-- ;
        }
    }
}
