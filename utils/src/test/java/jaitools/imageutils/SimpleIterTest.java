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

import java.awt.Point;
import java.awt.Rectangle;

import javax.media.jai.PlanarImage;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class SimpleIterTest extends TestBase {

    private static final int WIDTH = 17;
    private static final int HEIGHT = 19;
    private static final int NUM_BANDS = 3;
    private static final int OUTSIDE = -99;

    private SimpleIter iter;
    private PlanarImage image;


    @Test(expected=IllegalArgumentException.class) 
    public void nullImageArg() {
        iter = new SimpleIter(null, new Rectangle(0, 0, WIDTH, HEIGHT), OUTSIDE);
    }
    
    @Test
    public void nullBoundsMeansImageBounds() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        iter = new SimpleIter(image, null, null);
        Rectangle imageBounds = image.getBounds();
        assertEquals(imageBounds, iter.getBounds());
    }
    
    @Test
    public void iterateOverWholeImage() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        iter = new SimpleIter(image, null, null);
        assertSamples();
    }

    @Test
    public void iterBoundsBeyondImageBounds() {
        final int MARGIN = 5;
        
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);
        Rectangle imageBounds = image.getBounds();
        
        Rectangle iterBounds = new Rectangle(
                imageBounds.x - MARGIN, imageBounds.y - MARGIN,
                imageBounds.width + 2 * MARGIN, imageBounds.height + 2 * MARGIN);

        iter = new SimpleIter(image, iterBounds, OUTSIDE);
        assertSamples();
    }

    @Test
    public void imageWithNonZeroOrigin() {
        image = createSequentialImage(-7, 11, WIDTH, HEIGHT, NUM_BANDS, 0);
        iter = new SimpleIter(image, null, OUTSIDE);
        assertSamples();
    }

    @Test
    public void iterBoundsOutsideImageWithNonZeroOrigin() {
        image = createSequentialImage(-7, 11, WIDTH, HEIGHT, NUM_BANDS, 0);

        final int MARGIN = 5;
        Rectangle imageBounds = image.getBounds();
        Rectangle iterBounds = new Rectangle(imageBounds.x - MARGIN, imageBounds.y - MARGIN,
                imageBounds.width + 2 * MARGIN, imageBounds.height + 2 * MARGIN);

        iter = new SimpleIter(image, iterBounds, OUTSIDE);
        assertSamples();
    }

    @Test
    public void iterBoundsShortAndWide() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);

        Rectangle imageBounds = image.getBounds();
        Rectangle iterBounds = new Rectangle(
                imageBounds.x - 10, imageBounds.y + imageBounds.height / 4,
                imageBounds.width + 20, imageBounds.height / 2);

        iter = new SimpleIter(image, iterBounds, OUTSIDE);
        assertSamples();
    }

    @Test
    public void iterBoundsTallAndThin() {
        image = createSequentialImage(WIDTH, HEIGHT, NUM_BANDS);

        Rectangle imageBounds = image.getBounds();
        Rectangle iterBounds = new Rectangle(
                imageBounds.x + imageBounds.width / 4, imageBounds.y - 10,
                imageBounds.width / 2, imageBounds.height + 20);

        iter = new SimpleIter(image, iterBounds, OUTSIDE);
        assertSamples();
    }

    private void assertSamples() {
        final Rectangle iterBounds = iter.getBounds();
        final Rectangle imageBounds = image.getBounds();
        final int N = imageBounds.width * imageBounds.height;
        
        do {
            Point pos = iter.getPos();
            boolean inside = imageBounds.contains(pos);
            
            for (int band = 0; band < NUM_BANDS; band++) {
                int expected;
                if (inside) {
                    expected = (pos.y - imageBounds.y) * WIDTH + (pos.x - imageBounds.x) + band * N;
                } else {
                    expected = OUTSIDE;
                }
                assertEquals(expected, iter.getSample(band).intValue());
            }

        } while (iter.next());
    }
}
