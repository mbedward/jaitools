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

package jaitools.media.jai.rangelookup;

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.net.URL;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;
import javax.media.jai.registry.RenderedRegistryMode;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the RangeLookup operation. The test image used is a
 * byte image, 100 rows and 256 cols, wiht pixel values equal to row
 * index.
 *
 * @author Michael Bedward
 */
public class TestRangeLookup {

    private static RenderedImage theByteImage;

    public TestRangeLookup() throws Exception {
        ensureRegistered();
        fixForOSX();

        if (theByteImage == null) {
            URL url = this.getClass().getResource("/images/byte_test_image.tif");
            theByteImage = ImageIO.read(url);
        }
    }

    @Test
    public void testByteImageLookup() throws Exception {
        System.out.println("   testing byte image lookup");

        RangeLookupTable<Byte> table = new RangeLookupTable<Byte>();

        Range<Byte> r = new Range<Byte>(null, true, (byte)33, false);
        table.add(r, (byte)0);

        r = new Range<Byte>((byte)33, true, (byte)66, false);
        table.add(r, (byte)1);

        r = new Range<Byte>((byte)66, true, null, true);
        table.add(r, (byte)2);

        ParameterBlockJAI pb = new ParameterBlockJAI("RangeLookup");
        pb.setSource("source0", theByteImage);
        pb.setParameter("table", table);
        RenderedImage lookupImg = JAI.create("RangeLookup", pb);

        checkImageValues(lookupImg);
    }

    @Test
    public void testShortImageLookup() throws Exception {
        System.out.println("   testing short image lookup");

        RenderedImage shortImg = formatImage(theByteImage, DataBuffer.TYPE_SHORT);

        RangeLookupTable<Short> table = new RangeLookupTable<Short>();

        Range<Short> r = new Range<Short>(null, true, (short)33, false);
        table.add(r, (short)0);

        r = new Range<Short>((short)33, true, (short)66, false);
        table.add(r, (short)1);

        r = new Range<Short>((short)66, true, null, true);
        table.add(r, (short)2);

        ParameterBlockJAI pb = new ParameterBlockJAI("RangeLookup");
        pb.setSource("source0", shortImg);
        pb.setParameter("table", table);
        RenderedImage lookupImg = JAI.create("RangeLookup", pb);

        checkImageValues(lookupImg);
    }

    @Test
    public void testIntImageLookup() throws Exception {
        System.out.println("   testing int image lookup");

        RenderedImage intImg = formatImage(theByteImage, DataBuffer.TYPE_INT);

        RangeLookupTable<Integer> table = new RangeLookupTable<Integer>();

        Range<Integer> r = new Range<Integer>(null, true,33, false);
        table.add(r,0);

        r = new Range<Integer>(33, true,66, false);
        table.add(r,1);

        r = new Range<Integer>(66, true, null, true);
        table.add(r,2);

        ParameterBlockJAI pb = new ParameterBlockJAI("RangeLookup");
        pb.setSource("source0", intImg);
        pb.setParameter("table", table);
        RenderedImage lookupImg = JAI.create("RangeLookup", pb);

        checkImageValues(lookupImg);
    }

    @Test
    public void testFloatImageLookup() throws Exception {
        System.out.println("   testing float image lookup");

        RenderedImage floatImg = formatImage(theByteImage, DataBuffer.TYPE_FLOAT);

        RangeLookupTable<Float> table = new RangeLookupTable<Float>();

        Range<Float> r = new Range<Float>(null, true, (float)33, false);
        table.add(r, (float)0);

        r = new Range<Float>((float)33, true, (float)66, false);
        table.add(r, (float)1);

        r = new Range<Float>((float)66, true, null, true);
        table.add(r, (float)2);

        ParameterBlockJAI pb = new ParameterBlockJAI("RangeLookup");
        pb.setSource("source0", floatImg);
        pb.setParameter("table", table);
        RenderedImage lookupImg = JAI.create("RangeLookup", pb);

        checkImageValues(lookupImg);
    }

    @Test
    public void testDoubleImageLookup() throws Exception {
        System.out.println("   testing double image lookup");

        RenderedImage doubleImg = formatImage(theByteImage, DataBuffer.TYPE_DOUBLE);

        RangeLookupTable<Double> table = new RangeLookupTable<Double>();

        Range<Double> r = new Range<Double>(null, true, (double)33, false);
        table.add(r, (double)0);

        r = new Range<Double>((double)33, true, (double)66, false);
        table.add(r, (double)1);

        r = new Range<Double>((double)66, true, null, true);
        table.add(r, (double)2);

        ParameterBlockJAI pb = new ParameterBlockJAI("RangeLookup");
        pb.setSource("source0", doubleImg);
        pb.setParameter("table", table);
        RenderedImage lookupImg = JAI.create("RangeLookup", pb);

        checkImageValues(lookupImg);
    }

    @Test
    public void testDefaultValueImageLookup() throws Exception {
        System.out.println("   testing default value lookup with empty table");

        byte defVal = (byte)42;
        RangeLookupTable<Byte> table = new RangeLookupTable<Byte>(defVal);

        ParameterBlockJAI pb = new ParameterBlockJAI("RangeLookup");
        pb.setSource("source0", theByteImage);
        pb.setParameter("table", table);
        RenderedImage lookupImg = JAI.create("RangeLookup", pb);

        RectIter iter = RectIterFactory.create(lookupImg, null);
        do {
            do {
                assertTrue(iter.getSample() == (int)defVal);
            } while (!iter.nextPixelDone());
            iter.startPixels();
        } while (!iter.nextLineDone());
    }

    /**
     * Helper function for test methods
     */
    private void checkImageValues(RenderedImage lookupImg) {

        int[][] expValues = new int[][] {
            {0, 32},
            {33, 65},
            {66, 99}
        };

        RectIter iter = RectIterFactory.create(lookupImg, null);
        int y = 0;
        do {
            do {
                int i = iter.getSample();
                assertTrue(y >= expValues[i][0] && y <= expValues[i][1]);

            } while (!iter.nextPixelDone());
            iter.startPixels();
            y++ ;

        } while (!iter.nextLineDone());
    }

    /**
     * Helper function to change the data type of the test image
     */
    private RenderedImage formatImage(RenderedImage img, int type) {
        ParameterBlockJAI pb = new ParameterBlockJAI("format");
        pb.setSource("source0", img);
        pb.setParameter("dataType", type);
        return JAI.create("format", pb);
    }


    /**
     * Register the operator with JAI if it is not already registered
     */
    private void ensureRegistered() {
        OperationRegistry reg = JAI.getDefaultInstance().getOperationRegistry();
        String[] names = reg.getDescriptorNames(RenderedRegistryMode.MODE_NAME);
        RangeLookupDescriptor desc = new RangeLookupDescriptor();
        String descName = desc.getName();
        for (String name : names) {
            if (descName.equalsIgnoreCase(name)) {
                return;
            }
        }

        RangeLookupSpi spi = new RangeLookupSpi();
        spi.updateRegistry(reg);
    }

    /**
     * If we are running on OSX, turn off native acceleration
     * for JAI operations so that operators work properly
     * with double and double data rasters.
     */
    private void fixForOSX() {
        Properties sys = new Properties(System.getProperties());
        if (sys.getProperty("os.name").compareToIgnoreCase("mac os x") == 0) {
            sys.put("com.sun.media.jai.disableMediaLib", "true");
        }
        System.setProperties(sys);
    }

}
