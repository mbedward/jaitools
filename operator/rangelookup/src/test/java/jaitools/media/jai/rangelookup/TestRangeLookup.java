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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jaitools.numeric.Range;

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.io.IOException;
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

/**
 * Tests for the RangeLookup operation. The test image used is a
 * byte image, 100 rows and 256 cols, wiht pixel values equal to row
 * index.
 *
 * @author Michael Bedward
 */
public class TestRangeLookup {

    private static final RenderedImage theByteImage;
    static{
        URL url = TestRangeLookup.class.getResource("/images/byte_test_image.tif");
        RenderedImage img=null;
        try {
			img = ImageIO.read(url);
		} catch (IOException e) {
			img=null;
		} 
		
		// assign
		theByteImage=img;
    }

    public TestRangeLookup() throws Exception {
        ensureRegistered();
        fixForOSX();

    }

    @Test
    public void testByteImageLookup() throws Exception {
        System.out.println("   testing byte image lookup");

        RangeLookupTable<Byte,Byte> table = new RangeLookupTable<Byte,Byte>();

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

        // check data type
        assertEquals(0,lookupImg.getSampleModel().getDataType());
        checkImageValues(lookupImg,table,Byte.class);
    }

    @Test
    public void testUShortImageLookup() throws Exception {
        System.out.println("   testing short image lookup");

        RenderedImage shortImg = formatImage(theByteImage, DataBuffer.TYPE_SHORT);

        RangeLookupTable<Short,Short> table = new RangeLookupTable<Short,Short>();

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

        // check data type
        assertEquals(DataBuffer.TYPE_USHORT,lookupImg.getSampleModel().getDataType());
        checkImageValues(lookupImg,table,Short.class);
    }
    
    @Test
    public void testShortImageLookup() throws Exception {
        System.out.println("   testing short image lookup");

        RenderedImage shortImg = formatImage(theByteImage, DataBuffer.TYPE_SHORT);

        RangeLookupTable<Short,Short> table = new RangeLookupTable<Short,Short>();

        Range<Short> r = new Range<Short>(null, true, (short)33, false);
        table.add(r, (short)-1);

        r = new Range<Short>((short)33, true, (short)66, false);
        table.add(r, (short)10);

        r = new Range<Short>((short)66, true, null, true);
        table.add(r, (short)1000);

        ParameterBlockJAI pb = new ParameterBlockJAI("RangeLookup");
        pb.setSource("source0", shortImg);
        pb.setParameter("table", table);
        RenderedImage lookupImg = JAI.create("RangeLookup", pb);

        // check data type
        assertEquals(DataBuffer.TYPE_SHORT,lookupImg.getSampleModel().getDataType());
        checkImageValues(lookupImg,table, Short.class);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private  static void  checkImageValues(
    		final RenderedImage lookupImg,
			final RangeLookupTable table,
			final Class destClass) {
    	
        RectIter iter = RectIterFactory.create(lookupImg, null);
        short line = 0;
        do {
            do {
            	if(destClass.equals(Short.class))
            		assertEquals(table.getDestValue(line),(short)iter.getSample());
            	if(destClass.equals(Byte.class))
            		assertEquals(table.getDestValue(line),(byte)iter.getSample());       
            	if(destClass.equals(Integer.class))
            		assertEquals(table.getDestValue(line),iter.getSample());              	
            	if(destClass.equals(Float.class))
            		assertEquals(table.getDestValue(line),iter.getSampleFloat());
            	if(destClass.equals(Double.class))
            		assertEquals(table.getDestValue(line),iter.getSampleDouble());            	

            } while (!iter.nextPixelDone());
            iter.startPixels();
            line++ ;

        } while (!iter.nextLineDone());
	}

	@Test
    public void testIntImageLookup() throws Exception {
        System.out.println("   testing int image lookup");

        RenderedImage intImg = formatImage(theByteImage, DataBuffer.TYPE_INT);

        RangeLookupTable<Integer,Integer> table = new RangeLookupTable<Integer,Integer>();

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

        // check data type
        assertEquals(DataBuffer.TYPE_INT,lookupImg.getSampleModel().getDataType());
        checkImageValues(lookupImg,table,Integer.class);
    }

    @Test
    public void testFloatImageLookup() throws Exception {
        System.out.println("   testing float image lookup");

        RenderedImage floatImg = formatImage(theByteImage, DataBuffer.TYPE_FLOAT);

        RangeLookupTable<Float,Float> table = new RangeLookupTable<Float,Float>();

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

        // check data type
        assertEquals(DataBuffer.TYPE_FLOAT,lookupImg.getSampleModel().getDataType());
        checkImageValues(lookupImg,table,Float.class);
    }

    @Test
    public void testFloatToShortImageLookup() throws Exception {
        System.out.println("   testing float image lookup");

        RenderedImage floatImg = formatImage(theByteImage, DataBuffer.TYPE_FLOAT);

        RangeLookupTable<Float,Short> table = new RangeLookupTable<Float,Short>();

        Range<Float> r = new Range<Float>(null, true, (float)33, false);
        table.add(r, (short)-1);

        r = new Range<Float>((float)33, true, (float)66, false);
        table.add(r, (short)1);

        r = new Range<Float>((float)66, true, null, true);
        table.add(r, (short)2);

        ParameterBlockJAI pb = new ParameterBlockJAI("RangeLookup");
        pb.setSource("source0", floatImg);
        pb.setParameter("table", table);
        RenderedImage lookupImg = JAI.create("RangeLookup", pb);

        // check data type
        assertEquals(DataBuffer.TYPE_SHORT,lookupImg.getSampleModel().getDataType());
        checkImageValues(lookupImg,table,Short.class);
    }
    
    @Test
    public void testDoubleImageLookup() throws Exception {
        System.out.println("   testing double image lookup");

        RenderedImage doubleImg = formatImage(theByteImage, DataBuffer.TYPE_DOUBLE);

        RangeLookupTable<Double,Double> table = new RangeLookupTable<Double,Double>();

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

        // check data type
        assertEquals(DataBuffer.TYPE_DOUBLE,lookupImg.getSampleModel().getDataType());
        checkImageValues(lookupImg,table,Double.class);
    }

    @Test
    public void testDoubleToByteImageLookup() throws Exception {
        System.out.println("   testing double image lookup");

        RenderedImage doubleImg = formatImage(theByteImage, DataBuffer.TYPE_DOUBLE);

        RangeLookupTable<Double,Byte> table = new RangeLookupTable<Double,Byte>();

        Range<Double> r = new Range<Double>(null, true, (double)33, false);
        table.add(r, (byte)0);

        r = new Range<Double>((double)33, true, (double)66, false);
        table.add(r, (byte)1);

        r = new Range<Double>((double)66, true, null, true);
        table.add(r, (byte)2);

        ParameterBlockJAI pb = new ParameterBlockJAI("RangeLookup");
        pb.setSource("source0", doubleImg);
        pb.setParameter("table", table);
        RenderedImage lookupImg = JAI.create("RangeLookup", pb);

        // check data type
        assertEquals(DataBuffer.TYPE_BYTE,lookupImg.getSampleModel().getDataType());
        checkImageValues(lookupImg,table,Byte.class);
    }

    
    @Test
    public void testDefaultValueImageLookup() throws Exception {
        System.out.println("   testing default value lookup with empty table");

        byte defVal = (byte)42;
        RangeLookupTable<Byte,Byte> table = new RangeLookupTable<Byte,Byte>(defVal);

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
     * Helper function to change the data type of the test image
     */
    private static RenderedImage formatImage(RenderedImage img, int type) {
        ParameterBlockJAI pb = new ParameterBlockJAI("format");
        pb.setSource("source0", img);
        pb.setParameter("dataType", type);
        return JAI.create("format", pb);
    }


    /**
     * Register the operator with JAI if it is not already registered
     */
    private static void ensureRegistered() {
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
    private static void fixForOSX() {
        Properties sys = new Properties(System.getProperties());
        if (sys.getProperty("os.name").compareToIgnoreCase("mac os x") == 0) {
            sys.put("com.sun.media.jai.disableMediaLib", "true");
        }
        System.setProperties(sys);
    }

}
