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
import java.util.Arrays;

import javax.media.jai.TiledImage;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for WindowIter moving window iterator.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class WindowIterTest {

    private static final int WIDTH = 17;
    private static final int HEIGHT = 19;
    private static final int NUM_BANDS = 3;
    private static final Integer PAD = Integer.valueOf(-1);
    
    private TiledImage image;
    
    
    @Before
    public void setup() {
        image = createSequentialImage();
    }

    @Test
    public void allocateDestArray() {
        WindowIter iter = new WindowIter(image, null, new Dimension(3,5), new Point(1,1));
        int[][] window = iter.getWindow(null);
        assertNotNull(window);
        assertEquals(5, window.length);
        assertEquals(3, window[0].length);
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

    private void doGetPosTest(int xstep, int ystep) {
        WindowIter iter = new WindowIter(image, null, 
                new Dimension(3, 3), new Point(1, 1),
                xstep, ystep, PAD);

        int x = 0;
        int y = 0;
        do {
            Point pos = iter.getPos();
            assertEquals(x, pos.x);
            assertEquals(y, pos.y);
            
            x = Math.min(x + xstep, WIDTH) % WIDTH;
            if (x == 0) {
                y += ystep ;
            }
        } while (iter.next());
    }

    @Test(expected=IllegalArgumentException.class)
    public void nullImage() {
        WindowIter iter = new WindowIter(null, null, new Dimension(3, 3), new Point(1, 1));
    }

    @Test(expected=IllegalArgumentException.class)
    public void nullDimension() {
        WindowIter iter = new WindowIter(image, null, null, new Point(1, 1));
    }

    @Test(expected=IllegalArgumentException.class)
    public void nullKeyPoint() {
        WindowIter iter = new WindowIter(image, null, new Dimension(3, 3), null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void keyPointOutOfWindow() {
        WindowIter iter = new WindowIter(image, null, new Dimension(3, 3), new Point(3, 3));
    }

    @Test(expected=IllegalArgumentException.class)
    public void bandOutOfRange() {
        WindowIter iter = new WindowIter(image, null, new Dimension(3, 3), new Point(1, 1));
        iter.getWindow(null, NUM_BANDS);
    }

    private void doWindowTest(Dimension dim, Point key) {
        doWindowTest(dim, key, 1, 1);
    }
    
    private void doWindowTest(Dimension dim, Point key, int xstep, int ystep) {
        Rectangle bounds = new Rectangle(0, 0, WIDTH, HEIGHT);
        WindowIter iter = new WindowIter(image, null, dim, key, xstep, ystep, PAD);

        int[][] window = new int[dim.height][dim.width];
        int[][] expected = new int[dim.height][dim.width];

        int x = 0;
        int y = 0;
        do {
            iter.getWindow(window);
            assertWindow(window, bounds, dim, key, x, y, 0);

            for (int b = 0; b < NUM_BANDS; b++) {
                iter.getWindow(window, b);
                assertWindow(window, bounds, dim, key, x, y, b);
            }
            
            x = Math.min(x + xstep, WIDTH) % WIDTH;
            if (x == 0) {
                y += ystep ;
            }
        } while (iter.next());
    }

    private void assertWindow(int[][] window, Rectangle bounds, 
            Dimension dim, Point key, 
            int x, int y, int band) {
        
        final int minx = x - key.x;
        final int maxx = x + dim.width - key.x - 1;
        final int miny = y - key.y;
        final int maxy = y + dim.height - key.y - 1;
        for (int imgY = miny, winY = 0; imgY <= maxy; imgY++, winY++) { 
            for (int imgX = minx, winX = 0; imgX <= maxx; imgX++, winX++) {
                if (bounds.contains(imgX, imgY)) {
                    assertEquals(String.format("x=%d y=%d band=%d", x, y, band),
                            image.getSample(imgX, imgY, band), window[winY][winX]);
                } else {
                    assertEquals(String.format("x=%d y=%d band=%d", x, y, band),
                            PAD.intValue(), window[winY][winX]);
                }
            }
        }
    }

    private TiledImage createSequentialImage() {
        Integer[] fillValues = new Integer[NUM_BANDS];
        Arrays.fill(fillValues, Integer.valueOf(0));
        TiledImage img = ImageUtils.createConstantImage(WIDTH, HEIGHT, fillValues);
        
        int k = 0;
        for (int b = 0; b < NUM_BANDS; b++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    img.setSample(x, y, b, k++);
                }
            }
        }

        return img;
    }
}
