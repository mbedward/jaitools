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

import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Point;

import javax.media.jai.TiledImage;

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

    private static final int OX = -3;
    private static final int OY = 5;
    private static final int WIDTH = 17;
    private static final int HEIGHT = 19;
    private static final int NUM_BANDS = 3;
    private static final Integer OUTSIDE = Integer.valueOf(-1);
    
    private TiledImage image;
    
    
    @Test
    public void allocateDestArray() {
        image = createSequentialImage(OX, OY, WIDTH, HEIGHT, 1, 0);
        WindowIterator iter = new WindowIterator(image, null, new Dimension(3,5), new Point(1,1));
        int[][] window = iter.getWindowInt(null);
        assertNotNull(window);
        assertEquals(5, window.length);
        assertEquals(3, window[0].length);
    }

    @Test
    public void hasNextVsNext() {
        image = createSequentialImage(OX, OY, WIDTH, HEIGHT, 1, 0);
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
        doWindowIntTest(new Dimension(3, 3), new Point(1, 1));
    }
     
    @Test
    public void getWindow5x7() {
        doWindowIntTest(new Dimension(5, 7), new Point(2, 2));   
    }

    @Test
    public void keyElementAtWindowCorner() {
        doWindowIntTest(new Dimension(3, 3), new Point(2, 2));
    }

    @Test
    public void getWindow3x3StepX2() {
        doWindowIntTest(new Dimension(3, 3), new Point(1, 1), 2, 1);
    }

    @Test
    public void getWindow3x3StepY2() {
        doWindowIntTest(new Dimension(3, 3), new Point(1, 1), 1, 2);
    }

    @Test
    public void getWindow3x3StepX2StepY2() {
        doWindowIntTest(new Dimension(3, 3), new Point(1, 1), 2, 2);
    }

    @Test
    public void stepDistanceGreaterThanWindowWidth() {
        doWindowIntTest(new Dimension(2, 2), new Point(0, 0), 3, 1);
    }

    @Test
    public void stepDistanceGreaterThanWindowHeight() {
        doWindowIntTest(new Dimension(2, 2), new Point(0, 0), 1, 3);
    }

    @Test
    public void stepDistanceGreaterThanWindowWidthAndHeight() {
        doWindowIntTest(new Dimension(2, 2), new Point(0, 0), 3, 3);
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
        image = createSequentialImage(OX, OY, WIDTH, HEIGHT, NUM_BANDS, 0);

        Rectangle bounds = createAdjustedBounds(image.getBounds(), 5);
        Dimension winDim = new Dimension(3, 3);
        Point key = new Point(1, 1);
        WindowIterator iter = new WindowIterator(image, bounds, winDim, key, OUTSIDE);
        doWindowIntTest(iter, bounds, winDim, key, 1, 1);
    }

    @Test
    public void iterBoundsWiderAndShorter() {
        image = createSequentialImage(OX, OY, WIDTH, HEIGHT, NUM_BANDS, 0);

        Rectangle iterBounds = new Rectangle(
                image.getMinX() - WIDTH / 2, image.getMinY() + HEIGHT / 4,
                WIDTH * 2, HEIGHT / 2);
        
        Dimension winDim = new Dimension(3, 3);
        Point key = new Point(1, 1);
        WindowIterator iter = new WindowIterator(image, iterBounds, winDim, key, OUTSIDE);
        doWindowIntTest(iter, iterBounds, winDim, key, 1, 1);
    }

    @Test
    public void iterBoundsNarrowerAndTaller() {
        image = createSequentialImage(OX, OY, WIDTH, HEIGHT, NUM_BANDS, 0);

        Rectangle iterBounds = new Rectangle(
                image.getMinX() + WIDTH / 4, image.getMinY() - HEIGHT / 2,
                WIDTH / 2, HEIGHT * 2);
        
        Dimension winDim = new Dimension(3, 3);
        Point key = new Point(1, 1);
        WindowIterator iter = new WindowIterator(image, iterBounds, winDim, key, OUTSIDE);
        doWindowIntTest(iter, iterBounds, winDim, key, 1, 1);
    }

    @Test
    public void dataWindowNumber_IntImage() {
        image = ImageUtils.createConstantImage(WIDTH, HEIGHT, Integer.valueOf(0));
        Dimension winDim = new Dimension(3, 3);
        Point key = new Point(1, 1);
        WindowIterator iter = new WindowIterator(image, null, winDim, key, OUTSIDE);
        
        Number[][] window = iter.getWindow(null);

        // Both the key element (image) value and the upper-left (outside) value
        // should be Integers
        assertTrue(window[0][0] instanceof Integer);
        assertTrue(window[key.y][key.x] instanceof Integer);
    }

    @Test
    public void dataWindowNumber_FloatImage() {
        image = ImageUtils.createConstantImage(WIDTH, HEIGHT, Float.valueOf(0f));
        Dimension winDim = new Dimension(3, 3);
        Point key = new Point(1, 1);
        WindowIterator iter = new WindowIterator(image, null, winDim, key, OUTSIDE);
        
        Number[][] window = iter.getWindow(null);

        // Both the key element (image) value and the upper-left (outside) value
        // should be Floats
        assertTrue(window[0][0] instanceof Float);
        assertTrue(window[key.y][key.x] instanceof Float);
    }

    @Test
    public void dataWindowNumber_DoubleImage() {
        image = ImageUtils.createConstantImage(WIDTH, HEIGHT, Double.valueOf(0d));
        Dimension winDim = new Dimension(3, 3);
        Point key = new Point(1, 1);
        WindowIterator iter = new WindowIterator(image, null, winDim, key, OUTSIDE);
        
        Number[][] window = iter.getWindow(null);

        // Both the key element (image) value and the upper-left (outside) value
        // should be Doubles
        assertTrue(window[0][0] instanceof Double);
        assertTrue(window[key.y][key.x] instanceof Double);
    }

    @Test(expected=IllegalArgumentException.class)
    public void nullImage() {
        WindowIterator iter = new WindowIterator(null, null, new Dimension(3, 3), new Point(1, 1));
    }

    @Test(expected=IllegalArgumentException.class)
    public void nullDimension() {
        image = ImageUtils.createConstantImage(WIDTH, HEIGHT, 0);
        WindowIterator iter = new WindowIterator(image, null, null, new Point(1, 1));
    }

    @Test(expected=IllegalArgumentException.class)
    public void nullKeyPoint() {
        image = ImageUtils.createConstantImage(WIDTH, HEIGHT, 0);
        WindowIterator iter = new WindowIterator(image, null, new Dimension(3, 3), null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void keyPointOutOfWindow() {
        image = ImageUtils.createConstantImage(WIDTH, HEIGHT, 0);
        WindowIterator iter = new WindowIterator(image, null, new Dimension(3, 3), new Point(3, 3));
    }

    @Test(expected=IllegalArgumentException.class)
    public void bandOutOfRange() {
        // 3 band image
        image = ImageUtils.createConstantImage(WIDTH, HEIGHT, new Integer[]{0, 0, 0});

        WindowIterator iter = new WindowIterator(image, null, new Dimension(3, 3), new Point(1, 1));
        iter.getWindowInt(null, 3); // out of range band arg
    }

    private void doGetPosTest(int xstep, int ystep) {
        image = createSequentialImage(OX, OY, WIDTH, HEIGHT, 1, 0);
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

    private void doWindowIntTest(Dimension dim, Point key) {
        doWindowIntTest(dim, key, 1, 1);
    }
    
    private void doWindowIntTest(Dimension dim, Point key, int xstep, int ystep) {
        image = createSequentialImage(OX, OY, WIDTH, HEIGHT, NUM_BANDS, 0);
        Rectangle bounds = image.getBounds();
        WindowIterator iter = new WindowIterator(image, null, dim, key, xstep, ystep, OUTSIDE);
        doWindowIntTest(iter, bounds, dim, key, xstep, ystep);
    }
    
    private void doWindowIntTest(WindowIterator iter, Rectangle iterBounds,
                Dimension dim, Point key, int xstep, int ystep) {

        if (image == null) {
            throw new IllegalStateException("You forgot to create the image first");
        }

        int[][] window = new int[dim.height][dim.width];
        int x = iterBounds.x;
        int y = iterBounds.y;
        int lastX = iterBounds.x + iterBounds.width - 1; 
        
        do {
            iter.getWindowInt(window);
            assertWindow(window, dim, key, x, y, 0);

            for (int b = 0; b < NUM_BANDS; b++) {
                iter.getWindowInt(window, b);
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
