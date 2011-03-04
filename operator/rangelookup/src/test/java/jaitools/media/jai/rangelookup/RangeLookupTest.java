/*
 * Copyright 2009-2011 Michael Bedward
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

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;

import jaitools.imageutils.ImageUtils;
import jaitools.numeric.NumberOperations;
import jaitools.numeric.Range;
import javax.media.jai.RenderedOp;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for the RangeLookup operation.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class RangeLookupTest {

    private static final int WIDTH = 10;

    @Test
    public void byteToByte() throws Exception {
        System.out.println("   byte source to byte dest");
        
        Byte[] breaks = { 2, 4, 6, 8 };
        Byte[] values = { 0, 1, 2, 3, 4 };
        RenderedImage srcImg = createByteTestImage((byte) 0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_BYTE);
    }
    
    @Test
    public void shortToShort() throws Exception {
        System.out.println("   short source to short dest");
        
        Short[] breaks = { 2, 4, 6, 8 };
        Short[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createShortTestImage((short) 0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_SHORT);
    }
    
    @Test
    public void shortToShortWithNoNegativeValues() throws Exception {
        System.out.println("   short source to short dest");
        
        Short[] breaks = { 2, 4, 6, 8 };
        Short[] values = { 0, 1, 2, 3, 4 };
        RenderedImage srcImg = createShortTestImage((short) 0);
        
        // The destination image shoule be TYPE_USHORT
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_USHORT);
    }
    
    @Test
    public void ushortToUShort() throws Exception {
        System.out.println("   ushort source to ushort dest");

        Short[] breaks = { 2, 4, 6, 8 };
        Short[] values = { 0, 1, 2, 3, 4 };
        RenderedImage srcImg = createUShortTestImage((short) 0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_USHORT);
    }
    
    @Test
    public void ushortSourceWithNegativeDestValues() throws Exception {
        System.out.println("   ushort source and negative lookup values");

        Short[] breaks = { 2, 4, 6, 8 };
        Short[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createUShortTestImage((short) 0);
        
        // The destination image should be TYPE_SHORT
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_SHORT);
    }
    
    @Test
    public void intToInt() throws Exception {
        System.out.println("   int source to int dest");
        
        Integer[] breaks = { 2, 4, 6, 8 };
        Integer[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createIntTestImage(0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_INT);
    }
    
    @Test
    public void floatToFloat() throws Exception {
        System.out.println("   float source to float dest");
        
        Float[] breaks = { 2f, 4f, 6f, 8f };
        Float[] values = { -50f, -10f, 0f, 10f, 50f };
        RenderedImage srcImg = createFloatTestImage(0f);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_FLOAT);
    }
    
    @Test
    public void doubleToDouble() throws Exception {
        System.out.println("   double source to double dest");
        
        Double[] breaks = { 2d, 4d, 6d, 8d };
        Double[] values = { -50d, -10d, 0d, 10d, 50d };
        RenderedImage srcImg = createDoubleTestImage(0d);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_DOUBLE);
    }
    
    @Test
    public void floatToInt() throws Exception {
        System.out.println("   float source to int dest");
        
        Float[] breaks = { 2f, 4f, 6f, 8f };
        Integer[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createFloatTestImage(0f);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_INT);
    }
    
    @Test
    public void floatToShort() throws Exception {
        System.out.println("   float source to short dest");
        
        Float[] breaks = { 2f, 4f, 6f, 8f };
        Short[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createFloatTestImage(0f);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_SHORT);
    }
    
    @Test
    public void floatToByte() throws Exception {
        System.out.println("   float source to byte dest");
        
        Float[] breaks = { 2f, 4f, 6f, 8f };
        Byte[] values = { 0, 1, 2, 3, 4 };
        RenderedImage srcImg = createFloatTestImage(0f);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_BYTE);
    }
    
    @Test
    public void doubleToFloat() throws Exception {
        System.out.println("   double source to float dest");
        
        Double[] breaks = { 2d, 4d, 6d, 8d };
        Float[] values = { -50f, -10f, 0f, 10f, 50f };
        RenderedImage srcImg = createDoubleTestImage(0d);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_FLOAT);
    }
    
    @Test
    public void doubleToInt() throws Exception {
        System.out.println("   double source to int dest");
        
        Double[] breaks = { 2d, 4d, 6d, 8d };
        Integer[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createDoubleTestImage(0d);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_INT);
    }
    
    @Test
    public void doubleToShort() throws Exception {
        System.out.println("   double source to short dest");
        
        Double[] breaks = { 2d, 4d, 6d, 8d };
        Short[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createDoubleTestImage(0d);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_SHORT);
    }
    
    @Test
    public void doubleToByte() throws Exception {
        System.out.println("   double source to byte dest");
        
        Double[] breaks = { 2d, 4d, 6d, 8d };
        Byte[] values = { 0, 1, 2, 3, 4 };
        RenderedImage srcImg = createDoubleTestImage(0d);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_BYTE);
    }
    
    private <T extends Number & Comparable<? super T>, 
             U extends Number & Comparable<? super U>>
            void assertLookup(
                    T[] breaks, U[] values, 
                    RenderedImage srcImg,
                    int destDataType) {
        
        RangeLookupTable<T, U> table = createTable(breaks, values);
        RenderedOp destImg = doOp(srcImg, table);

        // check data type
        assertEquals(destDataType, destImg.getSampleModel().getDataType());
        assertImageValues(srcImg, table, destImg);
    }

    /**
     * Runs the operation.
     * 
     * @param srcImg the source image
     * @param table the lookup table
     * 
     * @return the destination image
     */
    private RenderedOp doOp(RenderedImage srcImg, RangeLookupTable table) {
        ParameterBlockJAI pb = new ParameterBlockJAI("RangeLookup");
        pb.setSource("source0", srcImg);
        pb.setParameter("table", table);
        return JAI.create("RangeLookup", pb);
    }

    /**
     * Creates a TYPE_BYTE test image with sequential values.
     * 
     * @param startVal min image value
     * 
     * @return the image
     */
    private RenderedImage createByteTestImage(byte startVal) {
        return createTestImage(startVal, new Byte[WIDTH * WIDTH]);
    }

    /**
     * Creates a TYPE_SHORT test image with sequential values.
     * 
     * @param startVal min image value
     * 
     * @return the image
     */
    private RenderedImage createShortTestImage(short startVal) {
        return createTestImage(startVal, new Short[WIDTH * WIDTH]);
    }
    
    /**
     * Creates a TYPE_USHORT test image with sequential values.
     * 
     * @param startVal min image value
     * 
     * @return the image
     */
    private RenderedImage createUShortTestImage(short startVal) {
        if (startVal < 0) {
            throw new IllegalArgumentException("startVal must be >= 0");
        }
        
        RenderedImage img = createTestImage(startVal, new Short[WIDTH * WIDTH]);
        ParameterBlockJAI pb = new ParameterBlockJAI("format");
        pb.setSource("source0", img);
        pb.setParameter("dataType", DataBuffer.TYPE_USHORT);
        return JAI.create("format", pb);
    }
    
    /**
     * Creates a TYPE_INT test image with sequential values.
     * 
     * @param startVal min image value
     * 
     * @return the image
     */
    private RenderedImage createIntTestImage(int startVal) {
        return createTestImage(startVal, new Integer[WIDTH * WIDTH]);
    }
    
    /**
     * Creates a TYPE_FLOAT test image with sequential values.
     * 
     * @param startVal min image value
     * 
     * @return the image
     */
    private RenderedImage createFloatTestImage(float startVal) {
        return createTestImage(startVal, new Float[WIDTH * WIDTH]);
    }
    
    /**
     * Creates a TYPE_DOUBLE test image with sequential values.
     * 
     * @param startVal min image value
     * 
     * @return the image
     */
    private RenderedImage createDoubleTestImage(double startVal) {
        return createTestImage(startVal, new Double[WIDTH * WIDTH]);
    }
    
    
    /**
     * Creates a test image with sequential values.
     * 
     * @param startVal min image value
     * @param data array to fill and use as pixel values
     * 
     * @return  the test image
     */
    private RenderedImage createTestImage(Number startVal, Number[] data) {
        final int N = WIDTH * WIDTH;
        Number value = startVal;
        Number delta = NumberOperations.newInstance(1, startVal.getClass());
        for (int i = 0; i < data.length; i++) {
            data[i] = value;
            value = NumberOperations.add(value, delta);
        }

        return ImageUtils.createImageFromArray(data, WIDTH, WIDTH);
    }

    /**
     * Creates a lookup table.
     * @param breaks array of breakpoints for source image values
     * @param values array of lookup values for destination image value
     * 
     * @return the lookup table
     */
    private <T extends Number & Comparable<? super T>, 
             U extends Number & Comparable<? super U>> 
            RangeLookupTable<T, U> createTable(T[] breaks, U[] values) {
        
        final int N = breaks.length;
        if (values.length != N + 1) {
            throw new IllegalArgumentException(
                    "values array length should be breaks array length + 1");
        }
        
        RangeLookupTable<T, U> table = new RangeLookupTable<T, U>();
        Range<T> r;
        
        r = Range.create(null, false, breaks[0], false);
        table.add(r, values[0]);
        
        for (int i = 1; i < N; i++) {
            r = Range.create(breaks[i-1], true, breaks[i], false);
            table.add(r, values[i]);
        }
        
        r = Range.create(breaks[N-1], true, null, false);
        table.add(r, values[N]);
        
        return table;
    }

    /**
     * Tests that a destination image contains expected values given the
     * source image and lookup table.
     * 
     * @param srcImg source image
     * @param table lookup table
     * @param destImg destination image
     */
    private void assertImageValues(RenderedImage srcImg, RangeLookupTable table, 
            RenderedImage destImg) {
        
        final int srcImgType = srcImg.getSampleModel().getDataType();
        final int destImgType = destImg.getSampleModel().getDataType();
        
        RectIter srcIter = RectIterFactory.create(srcImg, null);
        RectIter destIter = RectIterFactory.create(destImg, null);

        do {
            do {
                Number srcVal = getSourceImageValue(srcIter, srcImgType);
                Number expectedVal = table.getDestValue(srcVal);
                
                switch (destImgType) {
                    case DataBuffer.TYPE_BYTE:
                        assertEquals(0, NumberOperations.compare(expectedVal, (byte) destIter.getSample()));
                        break;
                        
                    case DataBuffer.TYPE_SHORT:
                        assertEquals(0, NumberOperations.compare(expectedVal, (short) destIter.getSample()));
                        break;
                        
                    case DataBuffer.TYPE_INT:
                        assertEquals(0, NumberOperations.compare(expectedVal, destIter.getSample()));
                        break;
                        
                    case DataBuffer.TYPE_FLOAT:
                        assertEquals(0, NumberOperations.compare(expectedVal, destIter.getSampleFloat()));
                        break;
                        
                    case DataBuffer.TYPE_DOUBLE:
                        assertEquals(0, NumberOperations.compare(expectedVal, destIter.getSampleDouble()));
                        break;
                }

                srcIter.nextPixelDone();
                
            } while (!destIter.nextPixelDone());
            
            srcIter.nextLineDone();
            srcIter.startPixels();
            destIter.startPixels();

        } while (!destIter.nextLineDone());
    }

    /**
     * Helper method for {@link #assertImageValues}.
     * 
     * @param srcIter source image iterator
     * @param srcImgType source image data type
     * 
     * @return source image value as a Number
     */
    private Number getSourceImageValue(RectIter srcIter, final int srcImgType) {
        Number val = null;
        switch (srcImgType) {
            case DataBuffer.TYPE_BYTE:
                val = (byte) (srcIter.getSample() & 0xff);
                break;
                
            case DataBuffer.TYPE_SHORT:
                val = (short) srcIter.getSample();
                break;
                
            case DataBuffer.TYPE_USHORT:
                val = (short) (srcIter.getSample() & 0xffff);
                break;
                
            case DataBuffer.TYPE_INT:
                val = srcIter.getSample();
                break;
                
            case DataBuffer.TYPE_FLOAT:
                val = srcIter.getSampleFloat();
                break;
                
            case DataBuffer.TYPE_DOUBLE:
                val = (short) srcIter.getSampleDouble();
                break;
                
            default:
                throw new IllegalArgumentException("Unknown image type");
        }
        return val;
    }

}
