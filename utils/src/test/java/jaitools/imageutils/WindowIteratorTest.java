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

import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Point;

import javax.media.jai.TiledImage;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for WindowIter moving window iterator.
 * 
 * @author Michael Bedward
 * @since 1.2
 * @version $Id$
 */
public class WindowIteratorTest extends TestBase {

    private static final int WIDTH = 17;
    private static final int HEIGHT = 19;
    private static final int NUM_BANDS = 3;
    private static final Integer OUTSIDE = Integer.valueOf(-1);
    
    private TiledImage image;
    
    
    @Before
    public void setup() {
        image = createSequentialImage(-3, 5, WIDTH, HEIGHT, NUM_BANDS, 0);
    }

    @Test
    public void allocateDestArray() {
        WindowIterator iter = new WindowIterator(image, null, new Dimension(3,5), new Point(1,1));
        int[][] window = iter.getWindow(null);
        assertNotNull(window);
        assertEquals(5, window.length);
        assertEquals(3, window[0].length);
    }

    @Test
    public void hasNextVsNext() {
        WindowIterator iter = new WindowIterator(image, null, new Dimension(3, 3), new Point(1,1));
        do {
            assertTrue(iter.next());
        } while (iter.hasNext());

        // now test the other way round
        iter = new WindowIterator(image, null, new Dimension(3, 3), new Point(1,1));
        do {
            // do nothing
        } while (iter.next());
        assertFalse(iter.hasNext());
    }

    @Test
    public void getWindow3x3() {
        doWindowTest(new Dimension(3, 3), new Point(1, 1));
    }
     
    @Test
    public void getWindow5x7() {
        doWindowTest(new Dimension(5, 7), new Point(2, 2));   
    }

    @Test
    public void keyElementAtWindowCorner() {
        doWindowTest(new Dimension(3, 3), new Point(2, 2));
    }

    @Test
    public void getWindow3x3StepX2() {
        doWindowTest(new Dimension(3, 3), new Point(1, 1), 2, 1);
    }

    @Test
    public void getWindow3x3StepY2() {
        doWindowTest(new Dimension(3, 3), new Point(1, 1), 1, 2);
    }

    @Test
    public void getWindow3x3StepX2StepY2() {
        doWindowTest(new Dimension(3, 3), new Point(1, 1), 2, 2);
    }

    @Test
    public void stepDistanceGreaterThanWindowWidth() {
        doWindowTest(new Dimension(2, 2), new Point(0, 0), 3, 1);
    }

    @Test
    public void stepDistanceGreaterThanWindowHeight() {
        doWindowTest(new Dimension(2, 2), new Point(0, 0), 1, 3);
    }

    @Test
    public void stepDistanceGreaterThanWindowWidthAndHeight() {
        doWindowTest(new Dimension(2, 2), new Point(0, 0), 3, 3);
    }

    @Test
    public void getIteratorPositionWithDefaultSteps() {
        doGetPosTest(1, 1);
    }

    @Test
    public void getIteratorPositionWithSpecifiedSteps() {
        doGetPosTest(2, 3);
    }

    @Test
    public void iterBoundsBeyondImageBounds() {
        Rectangle bounds = createAdjustedBounds(image.getBounds(), 5);
        Dimension winDim = new Dimension(3, 3);
        Point key = new Point(1, 1);
        WindowIterator iter = new WindowIterator(image, bounds, winDim, key, 1, 1, OUTSIDE);
        doWindowTest(iter, bounds, winDim, key, 1, 1);
    }

    @Test
    public void iterBoundsWiderAndShorter() {
        Rectangle iterBounds = new Rectangle(
                image.getMinX() - WIDTH / 2, image.getMinY() + HEIGHT / 4,
                WIDTH * 2, HEIGHT / 2);
        
        Dimension winDim = new Dimension(3, 3);
        Point key = new Point(1, 1);
        WindowIterator iter = new WindowIterator(image, iterBounds, winDim, key, 1, 1, OUTSIDE);
        doWindowTest(iter, iterBounds, winDim, key, 1, 1);
    }

    @Test
    public void iterBoundsNarrowerAndTaller() {
        Rectangle iterBounds = new Rectangle(
                image.getMinX() + WIDTH / 4, image.getMinY() - HEIGHT / 2,
                WIDTH / 2, HEIGHT * 2);
        
        Dimension winDim = new Dimension(3, 3);
        Point key = new Point(1, 1);
        WindowIterator iter = new WindowIterator(image, iterBounds, winDim, key, 1, 1, OUTSIDE);
        doWindowTest(iter, iterBounds, winDim, key, 1, 1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void nullImage() {
        WindowIterator iter = new WindowIterator(null, null, new Dimension(3, 3), new Point(1, 1));
    }

    @Test(expected=IllegalArgumentException.class)
    public void nullDimension() {
        WindowIterator iter = new WindowIterator(image, null, null, new Point(1, 1));
    }

    @Test(expected=IllegalArgumentException.class)
    public void nullKeyPoint() {
        WindowIterator iter = new WindowIterator(image, null, new Dimension(3, 3), null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void keyPointOutOfWindow() {
        WindowIterator iter = new WindowIterator(image, null, new Dimension(3, 3), new Point(3, 3));
    }

    @Test(expected=IllegalArgumentException.class)
    public void bandOutOfRange() {
        WindowIterator iter = new WindowIterator(image, null, new Dimension(3, 3), new Point(1, 1));
        iter.getWindow(null, NUM_BANDS);
    }

    private void doGetPosTest(int xstep, int ystep) {
        WindowIterator iter = new WindowIterator(image, null, 
                new Dimension(3, 3), new Point(1, 1),
                xstep, ystep, OUTSIDE);

        int x = image.getMinX();
        int y = image.getMinY();
        do {
            Point pos = iter.getPos();
            assertEquals(x, pos.x);
            assertEquals(y, pos.y);
            
            x += xstep;
            if (x >= image.getMinX() + image.getWidth()) {
                x = image.getMinX();
                y += ystep ;
            }
        } while (iter.next());
    }

    private void doWindowTest(Dimension dim, Point key) {
        doWindowTest(dim, key, 1, 1);
    }
    
    private void doWindowTest(Dimension dim, Point key, int xstep, int ystep) {
        Rectangle bounds = image.getBounds();
        WindowIterator iter = new WindowIterator(image, null, dim, key, xstep, ystep, OUTSIDE);
        doWindowTest(iter, bounds, dim, key, xstep, ystep);
    }
    
    private void doWindowTest(WindowIterator iter, Rectangle iterBounds,
                Dimension dim, Point key, int xstep, int ystep) {

        int[][] window = new int[dim.height][dim.width];
        int x = iterBounds.x;
        int y = iterBounds.y;
        int lastX = iterBounds.x + iterBounds.width - 1; 
        
        do {
            iter.getWindow(window);
            assertWindow(window, dim, key, x, y, 0);

            for (int b = 0; b < NUM_BANDS; b++) {
                iter.getWindow(window, b);
                assertWindow(window, dim, key, x, y, b);
            }
            
            x += xstep;
            if (x > lastX) {
                x = iterBounds.x;
                y += ystep ;
            }
        } while (iter.next());
    }

    private void assertWindow(int[][] window, Dimension dim, Point key, 
            int keyX, int keyY, int band) {
        
        final Rectangle imageBounds = image.getBounds();
        final int minx = keyX - key.x;
        final int maxx = keyX + dim.width - key.x - 1;
        final int miny = keyY - key.y;
        final int maxy = keyY + dim.height - key.y - 1;
        
        for (int y = miny, winY = 0; y <= maxy; y++, winY++) { 
            for (int x = minx, winX = 0; x <= maxx; x++, winX++) {
                if (imageBounds.contains(x, y)) {
                    assertEquals(String.format(
                            "key=(%d,%d) x=%d y=%d band=%d", keyX, keyY, x, y, band),
                            image.getSample(x, y, band), window[winY][winX]);
                } else {
                    assertEquals(String.format(
                            "key=(%d,%d) x=%d y=%d band=%d", keyX, keyY, x, y, band),
                            OUTSIDE.intValue(), window[winY][winX]);
                }
            }
        }
    }

}
