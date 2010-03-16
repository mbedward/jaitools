/*
 * Copyright 2009 Michael Bedward
 *
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jaitools.imageutils;

import jaitools.numeric.DoubleComparison;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;
import javax.media.jai.iterator.WritableRectIter;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for SamplingBorderExtender
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class SamplingBorderExtenderTest {

    private static final int SOURCE_WIDTH = 100;
    private static final int BUFFER_WIDTH = 2;
    private static final int SAMPLE_DISTANCE = 5;

    private static PlanarImage sourceImage;

    @BeforeClass
    public static void setup() {
        TiledImage timg = ImageUtils.createDoubleImage(SOURCE_WIDTH, SOURCE_WIDTH);
        WritableRectIter iter = RectIterFactory.createWritable(timg, null);
        double y = 0d;
        double x = 0d;
        do {
            do {
                iter.setSample(x + y);
            } while (!iter.nextPixelDone());

            iter.startPixels();
            x = 0d;
            y++ ;

        } while (!iter.nextLineDone());

        sourceImage = timg;
    }

    @Test
    public void testExtendByte() {
        System.out.println("   image type BYTE");

        WritableRaster raster = createRaster(DataBuffer.TYPE_BYTE);

        SamplingBorderExtender ex = new SamplingBorderExtender(SAMPLE_DISTANCE);
        ex.extend(raster, sourceImage);

        checkResultAsInt(raster, sourceImage.getBounds());
    }

    @Test
    public void testExtendShort() {
        System.out.println("   image type SHORT");

        WritableRaster raster = createRaster(DataBuffer.TYPE_SHORT);

        SamplingBorderExtender ex = new SamplingBorderExtender(SAMPLE_DISTANCE);
        ex.extend(raster, sourceImage);

        checkResultAsInt(raster, sourceImage.getBounds());
    }

    @Test
    public void testExtendUShort() {
        System.out.println("   image type USHORT");

        WritableRaster raster = createRaster(DataBuffer.TYPE_USHORT);
        checkResultAsInt(raster, sourceImage.getBounds());
    }

    @Test
    public void testExtendInt() {
        System.out.println("   image type INT");

        WritableRaster raster = createRaster(DataBuffer.TYPE_INT);
        checkResultAsInt(raster, sourceImage.getBounds());
    }

    @Test
    public void testExtendFloat() {
        System.out.println("   image type FLOAT");

        WritableRaster raster = createRaster(DataBuffer.TYPE_FLOAT);
        checkResultAsDouble(raster, sourceImage.getBounds());
    }

    @Test
    public void testExtendDouble() {
        System.out.println("   image type DOUBLE");

        WritableRaster raster = createRaster(DataBuffer.TYPE_DOUBLE);
        checkResultAsDouble(raster, sourceImage.getBounds());
    }

    private void checkResultAsInt(WritableRaster raster, Rectangle srcRectangle) {
        SamplingBorderExtender ex = new SamplingBorderExtender(SAMPLE_DISTANCE);
        ex.extend(raster, sourceImage);

        Rectangle samplingArea = new Rectangle(0, 0, 2*SAMPLE_DISTANCE + 1, 2*SAMPLE_DISTANCE + 1);
        RectIter iter;

        for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
            for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                if (!srcRectangle.contains(x, y)) {
                    int value = raster.getSample(x, y, 0);

                    samplingArea.setLocation(x - SAMPLE_DISTANCE, y - SAMPLE_DISTANCE);
                    Rectangle xRect = samplingArea.intersection(srcRectangle);
                    iter = RectIterFactory.create(sourceImage, xRect);

                    boolean found = false;
                    do {
                        do {
                            if (iter.getSample() == value) {
                                found = true;
                            }
                        } while (!found && !iter.nextPixelDone());
                        iter.startPixels();
                    } while (!found && !iter.nextLineDone());

                    assertTrue(found);
                }
            }
        }
    }

    private void checkResultAsDouble(WritableRaster raster, Rectangle srcRectangle) {
        SamplingBorderExtender ex = new SamplingBorderExtender(SAMPLE_DISTANCE);
        ex.extend(raster, sourceImage);

        Rectangle samplingArea = new Rectangle(0, 0, 2*SAMPLE_DISTANCE + 1, 2*SAMPLE_DISTANCE + 1);
        RectIter iter;

        for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
            for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                if (!srcRectangle.contains(x, y)) {
                    double value = raster.getSampleDouble(x, y, 0);

                    samplingArea.setLocation(x - SAMPLE_DISTANCE, y - SAMPLE_DISTANCE);
                    Rectangle xRect = samplingArea.intersection(srcRectangle);
                    iter = RectIterFactory.create(sourceImage, xRect);

                    boolean found = false;
                    do {
                        do {
                            if (DoubleComparison.dequal(value, iter.getSample())) {
                                found = true;
                            }
                        } while (!found && !iter.nextPixelDone());
                        iter.startPixels();
                    } while (!found && !iter.nextLineDone());

                    assertTrue(found);
                }
            }
        }
    }

    private WritableRaster createRaster(int dataType) {
        return RasterFactory.createBandedRaster(
                dataType,
                SOURCE_WIDTH + 2 * BUFFER_WIDTH,
                SOURCE_WIDTH + 2 * BUFFER_WIDTH,
                1, new Point(-BUFFER_WIDTH, -BUFFER_WIDTH));
    }

}