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

import java.util.Random;
import java.awt.Point;
import java.awt.Rectangle;

import javax.media.jai.PlanarImage;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for SimpleIterator.
 * 
 * @author michael
 */
public class SimpleIteratorTest extends TestBase {

    private static final int NUM_RESET_TESTS = 10;

    private static final int WIDTH = 17;
    private static final int HEIGHT = 19;
    private static final int NUM_BANDS = 3;
    private static final int OUTSIDE = -99;

    private SimpleIterator iter;
    private PlanarImage image;

    @Test(expected=IllegalArgumentException.class) 
    public void nullImageArg() {
        iter = new SimpleIterator(null, new Rectangle(0, 0, WIDTH, HEIGHT), OUTSIDE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void nullOrderArg() {
        image = createSequentialImage(WIDTH, HEIGHT, 1);
        iter = new SimpleIterator(image, image.getBounds(), OUTSIDE, null);
    }

    @Test
    public void nullToGetSample() {
        image = createSequentialImage(WIDTH, HEIGHT, 1);
        iter = new SimpleIterator(image, null, OUTSIDE);
        boolean gotEx = false;
        try {
            iter.getSample(null);
        } catch (IllegalArgumentException ex) {
            gotEx = true;
        }
        assertTrue(gotEx);
    }

    @Test
    public void nullToSetPosPoint() {
        image = createSequentialImage(WIDTH, HEIGHT, 1);
        iter = new SimpleIterator(image, null, OUTSIDE);
        boolean gotEx = false;
        try {
            iter.setPos(null);
        } catch (IllegalArgumentException ex) {
            gotEx = true;
        }
        assertTrue(gotEx);
    }

    @Test
    public void getSampleOutsideBoundsReturnsNull() {
        image = createSequentialImage(WIDTH, HEIGHT, 1);
        iter = new SimpleIterator(image, null, OUTSIDE);
        assertNull(iter.getSample(-1, -1));
    }

    @Test
    public void setPosOutsideBoundsReturnsFalse() {
        image = createSequentialImage(WIDTH, HEIGHT, 1);
        iter = new SimpleIterator(image, null, OUTSIDE);
        assertFalse(iter.setPos(-1, -1));
    }

    @Test
    public void getImage() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        iter = new SimpleIterator(image, null, OUTSIDE);
        assertTrue(image == iter.getImage());
    }

    @Test
    public void getStartPos() {
        final Point origin = new Point(-7, 11);
        image = createSequentialImage(origin.x, origin.y, WIDTH, HEIGHT, NUM_BANDS, 0);
        iter = new SimpleIterator(image, null, OUTSIDE);
        assertEquals(origin, iter.getStartPos());
    }

    @Test
    public void getEndPos() {
        final Point origin = new Point(-7, 11);
        final Point endPos = new Point(origin.x + WIDTH - 1, origin.y + HEIGHT - 1);
        image = createSequentialImage(origin.x, origin.y, WIDTH, HEIGHT, NUM_BANDS, 0);
        iter = new SimpleIterator(image, null, OUTSIDE);
        assertEquals(endPos, iter.getEndPos());
    }

    @Test
    public void isWithinImage() {
        image = createSequentialImage(WIDTH, HEIGHT, 1);
        final Rectangle imageBounds = image.getBounds();
        final Rectangle iterBounds = createAdjustedBounds(imageBounds, 5);
        iter = new SimpleIterator(image, iterBounds, OUTSIDE);

        do {
            assertEquals(imageBounds.contains(iter.getPos()), iter.isWithinImage());
        } while (iter.next());
    }

    @Test
    public void nullBoundsMeansImageBounds() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        iter = new SimpleIterator(image, null, null);
        Rectangle imageBounds = image.getBounds();
        assertEquals(imageBounds, iter.getBounds());
    }

    @Test
    public void iterateOverImage() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        iter = new SimpleIterator(image, null, null);
        assertSamples();
    }

    @Test
    public void iterateByTile() {
        image = createSequentialTiledImage(0, 0, WIDTH, HEIGHT, WIDTH / 2, HEIGHT / 2, NUM_BANDS, 0);
        iter = new SimpleIterator(image, null, null, SimpleIterator.Order.TILE_X_Y);
        assertSamples();
    }

    @Test
    public void boundsWhollyOutsideImage() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        Rectangle iterBounds = new Rectangle(-WIDTH, -HEIGHT, WIDTH, HEIGHT);
        iter = new SimpleIterator(image, iterBounds, OUTSIDE);
        assertSamples();
    }
    
    @Test
    public void iterBoundsBeyondImageBounds() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        Rectangle iterBounds = createAdjustedBounds(image.getBounds(), 5);
        iter = new SimpleIterator(image, iterBounds, OUTSIDE);
        assertSamples();
    }

    @Test
    public void iterBoundsBeyondImageBounds_ByTile() {
        image = createSequentialTiledImage(0, 0, WIDTH, HEIGHT, WIDTH / 2, HEIGHT / 2, NUM_BANDS, 0);
        Rectangle iterBounds = createAdjustedBounds(image.getBounds(), 5);
        iter = new SimpleIterator(image, iterBounds, OUTSIDE, SimpleIterator.Order.TILE_X_Y);
        assertSamples();
    }

    @Test
    public void imageWithNonZeroOrigin() {
        image = createSequentialImage(-7, 11, WIDTH, HEIGHT, NUM_BANDS, 0);
        iter = new SimpleIterator(image, null, OUTSIDE);
        assertSamples();
    }

    @Test
    public void tiledImageWithNonZeroOrigin() {
        image = createSequentialTiledImage(
                -7, 11, WIDTH, HEIGHT, WIDTH / 2, HEIGHT / 2, NUM_BANDS, 0);
        iter = new SimpleIterator(image, null, OUTSIDE, SimpleIterator.Order.TILE_X_Y);
        assertSamples();
    }

    @Test
    public void iterBoundsOutsideImageWithNonZeroOrigin() {
        image = createSequentialImage(-7, 11, WIDTH, HEIGHT, NUM_BANDS, 0);

        final int MARGIN = 5;
        Rectangle imageBounds = image.getBounds();
        Rectangle iterBounds = new Rectangle(imageBounds.x - MARGIN, imageBounds.y - MARGIN,
                imageBounds.width + 2 * MARGIN, imageBounds.height + 2 * MARGIN);

        iter = new SimpleIterator(image, iterBounds, OUTSIDE);
        assertSamples();
    }

    @Test
    public void iterBoundsOutsideImageWithNonZeroOrigin_ByTile() {
        image = createSequentialTiledImage(
                -7, 11, WIDTH, HEIGHT, WIDTH / 2, HEIGHT / 2, NUM_BANDS, 0);

        final int MARGIN = 5;
        Rectangle imageBounds = image.getBounds();
        Rectangle iterBounds = new Rectangle(imageBounds.x - MARGIN, imageBounds.y - MARGIN,
                imageBounds.width + 2 * MARGIN, imageBounds.height + 2 * MARGIN);

        iter = new SimpleIterator(image, iterBounds, OUTSIDE, SimpleIterator.Order.TILE_X_Y);
        assertSamples();
    }

    @Test
    public void iterBoundsShortAndWide() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);

        Rectangle imageBounds = image.getBounds();
        Rectangle iterBounds = new Rectangle(
                imageBounds.x - 10, imageBounds.y + imageBounds.height / 4,
                imageBounds.width + 20, imageBounds.height / 2);

        iter = new SimpleIterator(image, iterBounds, OUTSIDE);
        assertSamples();
    }

    @Test
    public void iterBoundsShortAndWide_ByTile() {
        image = createSequentialTiledImage(
                0, 0, WIDTH, HEIGHT, WIDTH / 2, HEIGHT / 2, NUM_BANDS, 0);

        Rectangle imageBounds = image.getBounds();
        Rectangle iterBounds = new Rectangle(
                imageBounds.x - 10, imageBounds.y + imageBounds.height / 4,
                imageBounds.width + 20, imageBounds.height / 2);

        iter = new SimpleIterator(image, iterBounds, OUTSIDE, SimpleIterator.Order.TILE_X_Y);
        assertSamples();
    }

    @Test
    public void iterBoundsTallAndThin() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);

        Rectangle imageBounds = image.getBounds();
        Rectangle iterBounds = new Rectangle(
                imageBounds.x + imageBounds.width / 4, imageBounds.y - 10,
                imageBounds.width / 2, imageBounds.height + 20);

        iter = new SimpleIterator(image, iterBounds, OUTSIDE);
        assertSamples();
    }

    @Test
    public void iterBoundsTallAndThin_ByTile() {
        image = createSequentialTiledImage(
                0, 0, WIDTH, HEIGHT, WIDTH / 2, HEIGHT / 2, NUM_BANDS, 0);

        Rectangle imageBounds = image.getBounds();
        Rectangle iterBounds = new Rectangle(
                imageBounds.x + imageBounds.width / 4, imageBounds.y - 10,
                imageBounds.width / 2, imageBounds.height + 20);

        iter = new SimpleIterator(image, iterBounds, OUTSIDE, SimpleIterator.Order.TILE_X_Y);
        assertSamples();
    }

    @Test
    public void resetIteratorWithInnerBounds() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        Rectangle iterBounds = createAdjustedBounds(image.getBounds(), -1);
        iter = new SimpleIterator(image, iterBounds, OUTSIDE);
        doResetTest();
    }

    @Test
    public void resetIteratorWithInnerBounds_ByTile() {
        image = createSequentialTiledImage(
                0, 0, WIDTH, HEIGHT, WIDTH / 2, HEIGHT / 2, NUM_BANDS, 0);
        Rectangle iterBounds = createAdjustedBounds(image.getBounds(), -1);
        iter = new SimpleIterator(image, iterBounds, OUTSIDE, SimpleIterator.Order.TILE_X_Y);
        doResetTest();
    }

    @Test
    public void resetIteratorWithOuterBounds() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        Rectangle iterBounds = createAdjustedBounds(image.getBounds(), 1);
        iter = new SimpleIterator(image, iterBounds, OUTSIDE);
        doResetTest();
    }

    @Test
    public void resetIteratorWithOuterBounds_ByTile() {
        image = createSequentialTiledImage(
                0, 0, WIDTH, HEIGHT, WIDTH / 2, HEIGHT / 2, NUM_BANDS, 0);
        Rectangle iterBounds = createAdjustedBounds(image.getBounds(), 1);
        iter = new SimpleIterator(image, iterBounds, OUTSIDE, SimpleIterator.Order.TILE_X_Y);
        doResetTest();
    }

    private void doResetTest() {
        // advance to the end of the iter bounds, then reset
        moveToEnd(iter);
        iter.reset();
        assertSamples();

        // Now test with random number of advances        
        Rectangle iterBounds = iter.getBounds();
        final int N = iterBounds.width * iterBounds.height;
        Random rand = new Random();
        for (int i = 0; i < NUM_RESET_TESTS; i++) {
            // Advance the iterator
            int n = rand.nextInt(N) + 1;
            while (n > 0) {
                iter.next();
                n-- ;
            }

            // Reset and test samples
            iter.reset();
            assertSamples();
        }
    }

    @Test 
    public void setPosBeforeNext() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        iter = new SimpleIterator(image, null, OUTSIDE);
        assertTrue( iter.setPos(WIDTH / 2, HEIGHT / 2) );
        assertSamples();
    }

    @Test 
    public void setPosAfterNext() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        iter = new SimpleIterator(image, null, OUTSIDE);
        moveToEnd(iter);
        
        assertTrue( iter.setPos(WIDTH / 2, HEIGHT / 2) );
        assertSamples();
    }

    @Test
    public void setPosOutsideBounds() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        iter = new SimpleIterator(image, null, OUTSIDE);
        Rectangle bounds = iter.getBounds();

        Point origin = new Point(bounds.x, bounds.y);
        
        assertFalse(iter.setPos(bounds.x - 1, bounds.y));
        assertEquals(origin, iter.getPos());
        assertFalse(iter.setPos(bounds.x, bounds.y - 1));
        assertEquals(origin, iter.getPos());
        assertFalse(iter.setPos(bounds.x + bounds.width, bounds.y));
        assertEquals(origin, iter.getPos());
        assertFalse(iter.setPos(bounds.x, bounds.y + bounds.height));
        assertEquals(origin, iter.getPos());
    }

    @Test
    public void getSampleForPosition() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        Rectangle imageBounds = image.getBounds();
        
        Rectangle iterBounds = createAdjustedBounds(imageBounds, 5);
        iter = new SimpleIterator(image, iterBounds, OUTSIDE);
        
        // sample by row then column
        Point pos = new Point();
        for (int x = iterBounds.x, ix = 0; ix < iterBounds.width; x++, ix++) {
            for (int y = iterBounds.y, iy = 0; iy < iterBounds.height; y++, iy++) {
                boolean inside = imageBounds.contains(x, y);
                pos.setLocation(x, y);

                // default - band 0
                Number sample = iter.getSample(pos);
                assertSample(x, y, 0, sample);

                // specified band
                for (int band = 0; band < NUM_BANDS; band++) {
                    sample = iter.getSample(pos, band);
                    assertSample(x, y, band, sample);
                }
            }
        }
    }

    private void assertSamples() {
        do {
            Point pos = iter.getPos();
            
            for (int band = 0; band < NUM_BANDS; band++) {
                Number sample = iter.getSample();
                assertSample(pos.x, pos.y, band, sample);
            }

        } while (iter.next());
    }

    private void assertSample(int x, int y, int band, Number sample) {
        final Rectangle imageBounds = image.getBounds();
        final int N = imageBounds.width * imageBounds.height;

        boolean inside = imageBounds.contains(x, y);
        int expected;
        if (inside) {
            expected = (y - imageBounds.y) * WIDTH + (x - imageBounds.x) + band * N;
        } else {
            expected = OUTSIDE;
        }

        assertEquals(expected, iter.getSample(band).intValue());
    }

}
