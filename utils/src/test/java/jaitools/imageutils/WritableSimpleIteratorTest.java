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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import javax.media.jai.TiledImage;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Most of the methods in this class are inherited from its base class and
 * tested in SimpleIteratorTest. Here we have tests of the setSample methods.
 *
 * @author michael
 */
public class WritableSimpleIteratorTest extends TestBase {
    
    private static final int WIDTH = 17;
    private static final int HEIGHT = 19;

    private TiledImage image;
    private WritableSimpleIterator iter;

    @Test
    public void setSequentialDefaultBand() {
        image = ImageUtils.createConstantImage(-3, 3, WIDTH, HEIGHT, 0);
        iter = new WritableSimpleIterator(image, null, 0);
        int k = 100;
        do {
            iter.setSample(k++);
        } while (iter.next());

        assertImageValues(new int[]{100});
    }

    @Test
    public void setSequentialSpecifiedBand() {
        final int[] startValues = {100, 200, 300};
        
        Integer[] fill = new Integer[startValues.length];
        Arrays.fill(fill, 0);
        image = ImageUtils.createConstantImage(-3, 3, WIDTH, HEIGHT, fill);
        iter = new WritableSimpleIterator(image, null, 0);

        int k = 0;
        do {
            for (int band = 0; band < startValues.length; band++) {
                iter.setSample(band, startValues[band] + k);
            }
            k++ ;
        } while (iter.next());

        assertImageValues(startValues);
    }

    @Test
    public void setSampleReturnsFalseOutsideImage() {
        image = ImageUtils.createConstantImage(WIDTH, HEIGHT, 0);
        final Rectangle imageBounds = image.getBounds();
        Rectangle iterBounds = createAdjustedBounds(imageBounds, 5);
        iter = new WritableSimpleIterator(image, iterBounds, 0);

        do {
            boolean rtnValue = iter.setSample(1);
            assertEquals(imageBounds.contains(iter.getPos()), rtnValue);
        } while (iter.next());
    }

    @Test
    public void setSampleForPosition() {
        final int[] startValues = {100, 200, 300};
        
        Integer[] fill = new Integer[startValues.length];
        Arrays.fill(fill, 0);
        image = ImageUtils.createConstantImage(-3, 3, WIDTH, HEIGHT, fill);
        iter = new WritableSimpleIterator(image, null, 0);

        final int w = image.getWidth();
        final int h = image.getHeight();

        // set samples in row-col order but with values as if set
        // by col-row order so that assertImageValues still works
        Point pos = new Point();
        for (int x = image.getMinX(), ix = 0; ix < w; x++, ix++) {
            for (int y = image.getMinY(), iy = 0; iy < h; y++, iy++) {
                pos.setLocation(x, y);
                for (int band = 0; band < image.getNumBands(); band++) {
                    iter.setSample(pos, band, startValues[band] + iy * w + ix);
                }
            }
        }

        assertImageValues(startValues);
    }

    private void assertImageValues(int[] startValues) {
        if (image == null) {
            throw new IllegalStateException("You forgot to create the image first");
        }
        if (startValues.length != image.getNumBands()) {
            throw new IllegalArgumentException(
                    "startValues length should equals number of image bands");
        }

        final int w = image.getWidth();
        final int h = image.getHeight();
        
        int k = 0; 
        for (int y = image.getMinY(), iy = 0; iy < h; y++, iy++) {
            for (int x = image.getMinX(), ix = 0; ix < w; x++, ix++) {
                for (int band = 0; band < image.getNumBands(); band++) {
                    assertEquals(startValues[band] + k, image.getSample(x, y, band));
                }
                k++ ;
            }
        }
    }
}
