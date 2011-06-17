/* 
 *  Copyright (c) 2009, Michael Bedward. All rights reserved. 
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
package org.jaitools.tiledimage;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;
import javax.media.jai.iterator.WritableRectIter;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests of DiskMemTilesImage: writing and retrieving data
 * at the image level
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class ImageWritingTest extends TiledImageTestBase {

    private static final int TILE_WIDTH = 128;
    private static final int XTILES = 2;
    private static final int YTILES = 3;

    private DiskMemImage image;

    @Before
    public void setup() {
        image = makeImage(TILE_WIDTH, XTILES, YTILES);
    }

    /**
     * Test setting and getting individual pixel / band values
     * as integers
     */
    @Test
    public void testPixelInt() {
        System.out.println("   setting and getting int values");

        Rectangle bounds = new Rectangle(TILE_WIDTH / 2, TILE_WIDTH / 2, TILE_WIDTH, TILE_WIDTH);
        int numBands = image.getNumBands();
        int numValues = 256 - numBands;

        int val = 0;
        for (int y = bounds.y, ny=0; ny < bounds.height; y++, ny++) {
            for (int x = bounds.x, nx = 0; nx < bounds.width; x++, nx++) {
                for (int band = 0; band < numBands; band++) {
                    image.setSample(x, y, band, val + band);
                }
                val = (val + 1) % numValues ;
            }
        }

        val = 0;
        for (int y = bounds.y, ny=0; ny < bounds.height; y++, ny++) {
            for (int x = bounds.x, nx = 0; nx < bounds.width; x++, nx++) {
                for (int band = 0; band < numBands; band++) {
                    int obs = image.getSample(x, y, band);
                    assertTrue(obs == (val + band));
                }
                val = (val + 1) % numValues ;
            }
        }

        /*
         * Out of bounds test
         */
        boolean gotException = false;
        try {
            image.getSample(image.getMinX() - 1, 0, 0);
        } catch (PixelOutsideImageException ex) {
            gotException = true;
        } catch (Exception ex) {
            fail("unexpected Exception type: " + ex.getClass().getSimpleName());
        }
        assertTrue(gotException);

        gotException = false;
        try {
            image.setSample(image.getMinX() - 1, 0, 0, 0);
        } catch (PixelOutsideImageException ex) {
            gotException = true;
        } catch (Exception ex) {
            fail("unexpected Exception type: " + ex.getClass().getSimpleName());
        }
        assertTrue(gotException);
    }

    /**
     * Test using JAI iterators to write and read image data
     */
    @Test
    public void testWriteIter() {
        System.out.println("   read/write using JAI iterators");

        WritableRectIter writeIter = RectIterFactory.createWritable(image, null);
        int i = 1;
        do {
            do {
                do {
                    writeIter.setSample(i);
                    i = (i % 31) + 1;
                } while (!writeIter.nextPixelDone());

                writeIter.startPixels();
            } while (!writeIter.nextLineDone());

            writeIter.startLines();
        } while (!writeIter.nextBandDone());


        RectIter readIter = RectIterFactory.create(image, null);
        i = 1;
        do {
            do {
                do {
                    assertTrue(readIter.getSample() == i);
                    i = (i % 31) + 1;
                } while (!readIter.nextPixelDone());

                readIter.startPixels();
            } while (!readIter.nextLineDone());

            readIter.startLines();
        } while (!readIter.nextBandDone());
    }


    /**
     * Test setting a rectangle of image data
     */
    @Test
    public void testSetData() {
        System.out.println("   setting a rectangle of image data");

        // create a raster that overlaps the image but extends partly
        // beyond it
        int minX = image.getMinX() + image.getWidth() / 2;
        int minY = image.getMinY() + image.getHeight() / 2;
        int maxX = image.getMaxX() + 10;
        int maxY = image.getMaxY() + 10;

        SampleModel sm = image.getSampleModel().createCompatibleSampleModel(maxX - minX + 1, maxY - minY + 1);
        WritableRaster data = Raster.createWritableRaster(sm, new Point(minX, minY));

        // write into the raster
        int numBands = sm.getNumBands();
        int value = 1;
        for (int b = 0; b < numBands; b++) {
            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    data.setSample(x, y, b, value);
                    value = (value % 31) + 1;
                }
            }
        }

        // copy the raster to the image
        image.setData(data);

        // read the image and check data values
        Rectangle bounds = image.getBounds();
        RandomIter iter = RandomIterFactory.create(image, null);
        value = 1;
        for (int b = 0; b < numBands; b++) {
            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    if (bounds.contains(x, y)) {
                        assertTrue(iter.getSample(x, y, b) == value);
                    }
                    value = (value % 31) + 1;
                }
            }
        }
    }

}
