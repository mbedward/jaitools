/*
 * Copyright 2011 Michael Bedward
 *
 * This file is part of jai-tools.
 *
 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jaitools.imageutils;

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
    
    @Test
    public void nullBoundsMeansImageBounds() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        iter = new SimpleIterator(image, null, null);
        Rectangle imageBounds = image.getBounds();
        assertEquals(imageBounds, iter.getBounds());
    }
    
    @Test
    public void iterateOverWholeImage() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        iter = new SimpleIterator(image, null, null);
        assertSamples();
    }

    @Test
    public void iterBoundsBeyondImageBounds() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        Rectangle iterBounds = createAdjustedBounds(5);
        iter = new SimpleIterator(image, iterBounds, OUTSIDE);
        assertSamples();
    }

    @Test
    public void imageWithNonZeroOrigin() {
        image = createSequentialImage(-7, 11, WIDTH, HEIGHT, NUM_BANDS, 0);
        iter = new SimpleIterator(image, null, OUTSIDE);
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
    public void resetIteratorWithInnerBounds() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        Rectangle iterBounds = createAdjustedBounds(-1);
        iter = new SimpleIterator(image, iterBounds, OUTSIDE);
        doResetTest();
    }

    @Test
    public void resetIteratorWithOuterBounds() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        Rectangle iterBounds = createAdjustedBounds(1);
        iter = new SimpleIterator(image, iterBounds, OUTSIDE);
        doResetTest();
    }

    private void doResetTest() {
        // advance to the end of the iter bounds, then reset
        moveToEnd();
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
        moveToEnd();
        
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
        
        Rectangle iterBounds = createAdjustedBounds(5);
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
        final Rectangle iterBounds = iter.getBounds();
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

    private Rectangle createAdjustedBounds(int delta) {
        if (image == null) {
            throw new IllegalStateException("You forgot to create the image first");
        }
        Rectangle imageBounds = image.getBounds();
        
        Rectangle iterBounds = new Rectangle(
                imageBounds.x - delta, imageBounds.y - delta,
                imageBounds.width + 2 * delta, imageBounds.height + 2 * delta);

        return iterBounds;
    }

    private void moveToEnd() {
        if (iter == null) {
            throw new IllegalStateException("You forgot to create the iterator first");
        }
        
        Rectangle bounds = iter.getBounds();
        int n = bounds.width * bounds.height;
        while (n > 0) {
            iter.next();
            n-- ;
        }
        assertFalse(iter.hasNext());
    }
}
