/*
 * Copyright 2009 Michael Bedward
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
package jaitools.tiledimage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for drawing into a DiskMemImage object using
 * Graphics2D routines via the DiskMemImageGraphics class
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class ImageGraphicsTest extends TiledImageTestBase {

    private final int TILE_WIDTH = 128;
    private final int XTILES = 2;
    private final int YTILES = 2;
    private final int imgMinX = 0;
    private final int imgMaxX = TILE_WIDTH * XTILES - 1;
    private int imgMinY = 0;
    private int imgMaxY = TILE_WIDTH * YTILES - 1;
    private int imgW = imgMaxX - imgMinX + 1;
    private int imgH = imgMaxY - imgMinY + 1;

    private final int alpha = 0xff;
    private final int red = 0xaa;
    private final int green = 0xbb;
    private final int blue = 0xcc;
    private final int argb = (alpha << 24) | (red << 16) | (green << 8) | blue;

    // TODO: is this reliable ?
    private final int RED_INDEX = 0;
    private final int GREEN_INDEX = 1;
    private final int BLUE_INDEX = 2;
    private final int ALPHA_INDEX = 3;

    private long tileMemSize;
    private Graphics2D gr;

    @Before
    public void setUp() {
        image = makeImage(TILE_WIDTH, XTILES, YTILES);
        tileMemSize = image.getTileMemorySize();
        gr = image.createGraphics();
    }


    @Test
    public void drawShape() {
        System.out.println("   draw(Shape s)");

        Rectangle shp = new Rectangle(
                imgMinX + TILE_WIDTH / 2,
                imgMinY + TILE_WIDTH / 2,
                imgW - TILE_WIDTH,
                imgH - TILE_WIDTH);
        
        gr.setColor(new Color(argb));
        gr.draw(shp);

        /*
         * Check the pixels on the perimeter of the drawn
         * shape.
         *
         * Note that the right and bottom edges
         * will be 1 pixel beyond the shape's max x and y
         * coords
         */
        int shpMinX = shp.x;
        int shpMinY = shp.y;
        int shpMaxX = shp.x + shp.width;  // as drawn
        int shpMaxY = shp.y + shp.height; //

        RandomIter iter = getRandomIter();
        int[] data = new int[4];

        // top and bottom edges
        for (int x = shpMinX; x <= shpMaxX; x++) {
            iter.getPixel(x, shpMinY, data);
            assertTrue(data[RED_INDEX] == red);
            assertTrue(data[GREEN_INDEX] == green);
            assertTrue(data[BLUE_INDEX] == blue);
            assertTrue(data[ALPHA_INDEX] == alpha);

            iter.getPixel(x, shpMaxY, data);
            assertTrue(data[RED_INDEX] == red);
            assertTrue(data[GREEN_INDEX] == green);
            assertTrue(data[BLUE_INDEX] == blue);
            assertTrue(data[ALPHA_INDEX] == alpha);
        }

        // sides
        for (int y = shpMinY+1; y < shpMaxY; y++) {
            iter.getPixel(shpMinX, y, data);
            assertTrue(data[RED_INDEX] == red);
            assertTrue(data[GREEN_INDEX] == green);
            assertTrue(data[BLUE_INDEX] == blue);
            assertTrue(data[ALPHA_INDEX] == alpha);

            iter.getPixel(shpMaxX, y, data);
            assertTrue(data[RED_INDEX] == red);
            assertTrue(data[GREEN_INDEX] == green);
            assertTrue(data[BLUE_INDEX] == blue);
            assertTrue(data[ALPHA_INDEX] == alpha);
        }
    }

    @Test
    public void fillShape() {
        System.out.println("   fill(Shape s)");

        Rectangle shp = new Rectangle(
                imgMinX + TILE_WIDTH / 2,
                imgMinY + TILE_WIDTH / 2,
                imgW - TILE_WIDTH,
                imgH - TILE_WIDTH);

        gr.setColor(new Color(argb));
        gr.fill(shp);

        RectIter iter = getRectIter();
        int[] data = new int[4];

        int y = imgMinY;
        do {
            int x = imgMinX;
            do {

                iter.getPixel(data);
                if (shp.contains(x, y)) {
                    assertTrue(data[RED_INDEX] == red);
                    assertTrue(data[GREEN_INDEX] == green);
                    assertTrue(data[BLUE_INDEX] == blue);
                    assertTrue(data[ALPHA_INDEX] == alpha);

                } else {
                    assertTrue(data[RED_INDEX] == 0);
                    assertTrue(data[GREEN_INDEX] == 0);
                    assertTrue(data[BLUE_INDEX] == 0);
                    assertTrue(data[ALPHA_INDEX] == 0);
                }
                x++ ;

            } while (!iter.nextPixelDone());

            iter.startPixels();
            y++ ;

        } while (!iter.nextLineDone());

    }

    @Test
    public void drawLine() {
        System.out.println("   drawLine");

        gr.setColor(new Color(argb));

        // draw the diagonal of a square
        int endCoord = (imgW <= imgH ? imgMaxX : imgMaxY);
        gr.drawLine(imgMinX, imgMinY, endCoord, endCoord);

        // check the drawing
        int x = imgMinX;
        int y = imgMinY;
        RandomIter iter = getRandomIter();
        int[] data = new int[4];
        while (x <= endCoord) {
            iter.getPixel(x, y, data);
            assertTrue(data[RED_INDEX] == red);
            assertTrue(data[GREEN_INDEX] == green);
            assertTrue(data[BLUE_INDEX] == blue);
            assertTrue(data[ALPHA_INDEX] == alpha);
            x++;
            y++;
        }
    }

    @Test
    public void setStroke() {
        System.out.println("   drawing with set stroke width");
        
        gr.setColor(new Color(argb));
        gr.setStroke(new BasicStroke(3.0f));

        int yline = imgH / 2;
        gr.drawLine(imgMinX, yline, imgMaxX, yline);

        RandomIter iter = getRandomIter();
        int[] data = new int[4];
        for (int x = imgMinX; x <= imgMaxX; x++) {
            for (int y = yline-1; y <= yline+1; y++) {
                iter.getPixel(x, y, data);
                assertTrue(data[RED_INDEX] == red);
                assertTrue(data[GREEN_INDEX] == green);
                assertTrue(data[BLUE_INDEX] == blue);
                assertTrue(data[ALPHA_INDEX] == alpha);
            }
        }
    }

    @Test
    public void drawPolyline() {
        System.out.println("   drawPolyline");

        int minX = imgMinX + 10;
        int midX = imgMinX + imgW / 2;
        int maxX = imgMaxX - 10;

        int minY = imgMinY + 10;
        int maxY = imgMaxY - 10;

        int[] xPoints = { minX, midX, midX, maxX };
        int[] yPoints = { minY, minY, maxY, maxY };

        gr.setColor(new Color(argb));
        gr.drawPolyline(xPoints, yPoints, xPoints.length);

        RandomIter iter = getRandomIter();
        int[] data = new int[4];

        // first segment
        for (int x = minX; x <= midX; x++) {
            iter.getPixel(x, minY, data);
            assertTrue(data[RED_INDEX] == red);
            assertTrue(data[GREEN_INDEX] == green);
            assertTrue(data[BLUE_INDEX] == blue);
            assertTrue(data[ALPHA_INDEX] == alpha);
        }

        // second segment
        for (int y = minY; y <= maxY; y++) {
            iter.getPixel(midX, y, data);
            assertTrue(data[RED_INDEX] == red);
            assertTrue(data[GREEN_INDEX] == green);
            assertTrue(data[BLUE_INDEX] == blue);
            assertTrue(data[ALPHA_INDEX] == alpha);
        }

        // last segment
        for (int x = midX; x <= maxX; x++) {
            iter.getPixel(x, maxY, data);
            assertTrue(data[RED_INDEX] == red);
            assertTrue(data[GREEN_INDEX] == green);
            assertTrue(data[BLUE_INDEX] == blue);
            assertTrue(data[ALPHA_INDEX] == alpha);
        }
    }

    private RandomIter getRandomIter() {
        return RandomIterFactory.create(image, null);
    }

    private RectIter getRectIter() {
        return RectIterFactory.create(image, null);
    }
}
