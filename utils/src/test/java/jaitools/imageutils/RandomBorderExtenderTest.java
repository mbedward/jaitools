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

package jaitools.imageutils;

import jaitools.numeric.DoubleComparison;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for RandomBorderExtender
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class RandomBorderExtenderTest {

    private static final int SOURCE_WIDTH = 100;
    private static final int BUFFER_WIDTH = 2;

    private static PlanarImage sourceImage;

    @BeforeClass
    public static void setup() {
       ParameterBlockJAI pb = new ParameterBlockJAI("constant");
        pb.setParameter("width", (float)SOURCE_WIDTH);
        pb.setParameter("height", (float)SOURCE_WIDTH);
        pb.setParameter("bandValues", new Integer[] { 0 });

        sourceImage = JAI.create("constant", pb);
    }

    @Test
    public void testExtendByte() {
        System.out.println("   image type BYTE");

        WritableRaster raster = createRaster(DataBuffer.TYPE_BYTE);

        RandomBorderExtender ex = new RandomBorderExtender(-10, 10);
        ex.extend(raster, sourceImage);

        checkResultAsInt(raster, sourceImage.getBounds(), 0, 10);
    }

    @Test
    public void testExtendShort() {
        System.out.println("   image type SHORT");

        WritableRaster raster = createRaster(DataBuffer.TYPE_SHORT);

        RandomBorderExtender ex = new RandomBorderExtender(-10, 10);
        ex.extend(raster, sourceImage);

        checkResultAsInt(raster, sourceImage.getBounds(), -10, 10);
    }

    @Test
    public void testExtendUShort() {
        System.out.println("   image type USHORT");

        WritableRaster raster = createRaster(DataBuffer.TYPE_USHORT);

        RandomBorderExtender ex = new RandomBorderExtender(-10, 10);
        ex.extend(raster, sourceImage);

        checkResultAsInt(raster, sourceImage.getBounds(), 0, 10);
    }

    @Test
    public void testExtendInt() {
        System.out.println("   image type INT");

        WritableRaster raster = createRaster(DataBuffer.TYPE_INT);

        RandomBorderExtender ex = new RandomBorderExtender(-10, 10);
        ex.extend(raster, sourceImage);

        checkResultAsInt(raster, sourceImage.getBounds(), -10, 10);
    }

    @Test
    public void testExtendFloat() {
        System.out.println("   image type FLOAT");

        WritableRaster raster = createRaster(DataBuffer.TYPE_FLOAT);

        RandomBorderExtender ex = new RandomBorderExtender(-10, 10);
        ex.extend(raster, sourceImage);

        checkResultAsDouble(raster, sourceImage.getBounds(), -10, 10);
    }

    @Test
    public void testExtendDouble() {
        System.out.println("   image type DOUBLE");

        WritableRaster raster = createRaster(DataBuffer.TYPE_DOUBLE);

        RandomBorderExtender ex = new RandomBorderExtender(-10, 10);
        ex.extend(raster, sourceImage);

        checkResultAsDouble(raster, sourceImage.getBounds(), -10, 10);
    }

    private void checkResultAsInt(WritableRaster raster, Rectangle srcRectangle, int minValue, int maxValue) {
        for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
            for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                if (!srcRectangle.contains(x, y)) {
                    int value = raster.getSample(x, y, 0);
                    assertTrue(value >= minValue && value < maxValue);
                }
            }
        }
    }

    private void checkResultAsDouble(WritableRaster raster, Rectangle srcRectangle, double minValue, double maxValue) {
        for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
            for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                if (!srcRectangle.contains(x, y)) {
                    double value = raster.getSampleDouble(x, y, 0);
                    assertTrue(DoubleComparison.dcomp(value, minValue) >= 0);
                    assertTrue(DoubleComparison.dcomp(value, maxValue) < 0);
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