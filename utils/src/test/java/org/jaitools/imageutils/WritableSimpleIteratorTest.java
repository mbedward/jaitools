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

package org.jaitools.imageutils;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.util.Arrays;

import javax.media.jai.TiledImage;

import org.jaitools.imageutils.AbstractSimpleIterator.Order;

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
    private static final int TILE_WIDTH = 7;
    private static final int TILE_HEIGHT = 11;

    private TiledImage image;
    private WritableSimpleIterator iter;

    @Test
    public void setSequentialDefaultBand() {
        image = createTestImage(-3, 3, 0);
        iter = new WritableSimpleIterator(image, null, 0);
        final int startValue = 100;
        int k = startValue;
        do {
            iter.setSample(k++);
        } while (iter.next());

        assertImageValues(new int[]{startValue}, Order.IMAGE_X_Y);
    }

    @Test
    public void setSequentialDefaultBand_ByTile() throws Exception {
        image = createTestImage(-3, 3, 0);
        iter = new WritableSimpleIterator(image, null, 0, Order.TILE_X_Y);
        final int startValue = 100;
        int k = startValue;
        do {
            iter.setSample(k++);
        } while (iter.next());
        
        /*
         * Uncomment to print image as text
         * 
        for (int y = image.getMinY(), ny = 0; ny < image.getHeight(); y++, ny++) {
            for (int x = image.getMinX(), nx = 0; nx < image.getWidth(); x++, nx++) {
                System.out.printf(" %03d", image.getSample(x, y, 0));
            }
            System.out.println();
        }
        * 
        */

        assertImageValues(new int[]{startValue}, Order.TILE_X_Y);
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

        assertImageValues(startValues, Order.IMAGE_X_Y);
    }

    @Test
    public void setSequentialSpecifiedBand_ByTile() {
        final int[] startValues = {100, 200, 300};
        
        Integer[] fill = new Integer[startValues.length];
        Arrays.fill(fill, 0);
        image = ImageUtils.createConstantImage(-3, 3, WIDTH, HEIGHT, fill);
        iter = new WritableSimpleIterator(image, null, 0, Order.TILE_X_Y);

        int k = 0;
        do {
            for (int band = 0; band < startValues.length; band++) {
                iter.setSample(band, startValues[band] + k);
            }
            k++ ;
        } while (iter.next());

        assertImageValues(startValues, Order.TILE_X_Y);
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

        assertImageValues(startValues, Order.IMAGE_X_Y);
    }

    private void assertImageValues(int[] startValues, Order order) {
        if (image == null) {
            throw new IllegalStateException("You forgot to create the image first");
        }
        if (startValues.length != image.getNumBands()) {
            throw new IllegalArgumentException(
                    "startValues length should equals number of image bands");
        }

        switch (order) {
            case IMAGE_X_Y:
                assertImageValuesImageOrder(startValues);
                break;
                
            case TILE_X_Y:
                assertImageValuesTileOrder(startValues);
                break;
        }
    }
    
    private void assertImageValuesImageOrder(int[] startValues) {
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

    private void assertImageValuesTileOrder(int[] startValues) {
        final Rectangle bounds = image.getBounds();
        final int ox = image.getMinX();
        
        int k = 0;
        for (int ty = image.getMinTileY(); ty <= image.getMaxTileY(); ty++) {
            for (int tx = image.getMinTileX(); tx <= image.getMaxTileX(); tx++) {
                Raster tile = image.getTile(tx, ty);
                for (int y = tile.getMinY(), iy = 0; iy < tile.getHeight(); y++, iy++) {
                    if (bounds.contains(ox, y)) {
                        for (int x = tile.getMinX(), ix = 0; ix < tile.getWidth(); x++, ix++) {
                            if (bounds.contains(x, y)) {
                                for (int band = 0; band < image.getNumBands(); band++) {
                                    assertEquals(startValues[band] + k, tile.getSample(x, y, band));
}
                                k++ ;
                            }
                        }
                    }
                }
            }
        }
    }

    private TiledImage createTestImage(int minX, int minY, int bandValue) {
        return createTestImage(minX, minY, new int[]{bandValue});
    }
    
    private TiledImage createTestImage(int minX, int minY, int[] bandValues) {
        Integer[] values = new Integer[bandValues.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = bandValues[i];
        }
        
        return ImageUtils.createConstantImage(
                minX, minY, WIDTH, HEIGHT, TILE_WIDTH, TILE_HEIGHT, values);
    }
    
}
