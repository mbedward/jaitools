/* 
 *  Copyright (c) 2009-2011, Michael Bedward. All rights reserved. 
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

package org.jaitools.media.jai.rangelookup;

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import org.jaitools.imageutils.ImageUtils;
import org.jaitools.numeric.NumberOperations;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the RangeLookup operation.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class RangeLookupTest extends TestBase {

    private static final int WIDTH = 10;
    
    @Test
    public void byteToByte() {
        System.out.println("   byte source to byte destination");
        Byte[] breaks = { 2, 4, 6, 8 };
        Byte[] values = { 0, 1, 2, 3, 4 };
        RenderedImage srcImg = createByteTestImage((byte)0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_BYTE);
    }
    
    @Test
    public void byteToShort() {
        System.out.println("   byte source to short destination");
        Byte[] breaks = { 2, 4, 6, 8 };
        Short[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createByteTestImage((byte)0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_SHORT);
    }
    
    @Test
    public void byteToInt() {
        System.out.println("   byte source to int destination");
        Byte[] breaks = { 2, 4, 6, 8 };
        Integer[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createByteTestImage((byte)0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_INT);
    }
    
    @Test
    public void byteToFloat() {
        System.out.println("   byte source to float destination");
        Byte[] breaks = { 2, 4, 6, 8 };
        Float[] values = { -50f, -10f, 0f, 10f, 50f };
        RenderedImage srcImg = createByteTestImage((byte)0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_FLOAT);
    }
    
    @Test
    public void byteToDouble() {
        System.out.println("   byte source to double destination");
        Byte[] breaks = { 2, 4, 6, 8 };
        Double[] values = { -50d, -10d, 0d, 10d, 50d };
        RenderedImage srcImg = createByteTestImage((byte)0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_DOUBLE);
    }
    
    @Test
    public void shortToByte() {
        System.out.println("   short source to byte destination");
        Short[] breaks = { 2, 4, 6, 8 };
        Byte[] values = { 0, 1, 2, 3, 4 };
        RenderedImage srcImg = createShortTestImage((short)0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_BYTE);
    }
    
    @Test
    public void shortToShort() {
        System.out.println("   short source to short destination");
        Short[] breaks = { 2, 4, 6, 8 };
        Short[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createShortTestImage((short)0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_SHORT);
    }
    
    @Test
    public void shortToInt() {
        System.out.println("   short source to int destination");
        Short[] breaks = { 2, 4, 6, 8 };
        Integer[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createShortTestImage((short)0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_INT);
    }
    
    @Test
    public void shortToFloat() {
        System.out.println("   short source to float destination");
        Short[] breaks = { 2, 4, 6, 8 };
        Float[] values = { -50f, -10f, 0f, 10f, 50f };
        RenderedImage srcImg = createShortTestImage((short)0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_FLOAT);
    }
    
    @Test
    public void shortToDouble() {
        System.out.println("   short source to double destination");
        Short[] breaks = { 2, 4, 6, 8 };
        Double[] values = { -50d, -10d, 0d, 10d, 50d };
        RenderedImage srcImg = createShortTestImage((short)0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_DOUBLE);
    }
    
    @Test
    public void ushortToByte() {
        System.out.println("   ushort source to byte destination");
        Short[] breaks = { 2, 4, 6, 8 };
        Byte[] values = { 0, 1, 2, 3, 4 };
        RenderedImage srcImg = createUShortTestImage((short)0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_BYTE);
    }
    
    @Test
    public void ushortToShort() {
        System.out.println("   ushort source to short destination");
        Short[] breaks = { 2, 4, 6, 8 };
        Short[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createUShortTestImage((short)0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_SHORT);
    }
    
    @Test
    public void ushortToInt() {
        System.out.println("   ushort source to int destination");
        Short[] breaks = { 2, 4, 6, 8 };
        Integer[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createUShortTestImage((short)0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_INT);
    }
    
    @Test
    public void ushortToFloat() {
        System.out.println("   ushort source to float destination");
        Short[] breaks = { 2, 4, 6, 8 };
        Float[] values = { -50f, -10f, 0f, 10f, 50f };
        RenderedImage srcImg = createUShortTestImage((short)0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_FLOAT);
    }
    
    @Test
    public void ushortToDouble() {
        System.out.println("   ushort source to double destination");
        Short[] breaks = { 2, 4, 6, 8 };
        Double[] values = { -50d, -10d, 0d, 10d, 50d };
        RenderedImage srcImg = createUShortTestImage((short)0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_DOUBLE);
    }
    
    @Test
    public void intToByte() {
        System.out.println("   int source to byte destination");
        Integer[] breaks = { 2, 4, 6, 8 };
        Byte[] values = { 0, 1, 2, 3, 4 };
        RenderedImage srcImg = createIntTestImage(0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_BYTE);
    }
    
    @Test
    public void intToShort() {
        System.out.println("   int source to short destination");
        Integer[] breaks = { 2, 4, 6, 8 };
        Short[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createIntTestImage(0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_SHORT);
    }
    
    @Test
    public void intToInt() {
        System.out.println("   int source to int destination");
        Integer[] breaks = { 2, 4, 6, 8 };
        Integer[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createIntTestImage(0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_INT);
    }
    
    @Test
    public void intToFloat() {
        System.out.println("   int source to float destination");
        Integer[] breaks = { 2, 4, 6, 8 };
        Float[] values = { -50f, -10f, 0f, 10f, 50f };
        RenderedImage srcImg = createIntTestImage(0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_FLOAT);
    }
    
    @Test
    public void intToDouble() {
        System.out.println("   int source to double destination");
        Integer[] breaks = { 2, 4, 6, 8 };
        Double[] values = { -50d, -10d, 0d, 10d, 50d };
        RenderedImage srcImg = createIntTestImage(0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_DOUBLE);
    }
    
    
    @Test
    public void floatToByte() {
        System.out.println("   float source to byte destination");
        Float[] breaks = { 2f, 4f, 6f, 8f };
        Byte[] values = { 0, 1, 2, 3, 4 };
        RenderedImage srcImg = createFloatTestImage(0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_BYTE);
    }
    
    @Test
    public void floatToShort() {
        System.out.println("   float source to short destination");
        Float[] breaks = { 2f, 4f, 6f, 8f };
        Short[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createFloatTestImage(0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_SHORT);
    }
    
    @Test
    public void floatToInt() {
        System.out.println("   float source to int destination");
        Float[] breaks = { 2f, 4f, 6f, 8f };
        Integer[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createFloatTestImage(0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_INT);
    }
    
    @Test
    public void floatToFloat() {
        System.out.println("   float source to float destination");
        Float[] breaks = { 2f, 4f, 6f, 8f };
        Float[] values = { -50f, -10f, 0f, 10f, 50f };
        RenderedImage srcImg = createFloatTestImage(0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_FLOAT);
    }
    
    @Test
    public void floatToDouble() {
        System.out.println("   float source to double destination");
        Float[] breaks = { 2f, 4f, 6f, 8f };
        Double[] values = { -50d, -10d, 0d, 10d, 50d };
        RenderedImage srcImg = createFloatTestImage(0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_DOUBLE);
    }
    
    @Test
    public void doubleToByte() {
        System.out.println("   double source to byte destination");
        Double[] breaks = { 2d, 4d, 6d, 8d };
        Byte[] values = { 0, 1, 2, 3, 4 };
        RenderedImage srcImg = createDoubleTestImage(0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_BYTE);
    }
    
    @Test
    public void doubleToShort() {
        System.out.println("   double source to short destination");
        Double[] breaks = { 2d, 4d, 6d, 8d };
        Short[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createDoubleTestImage(0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_SHORT);
    }
    
    @Test
    public void doubleToInt() {
        System.out.println("   double source to int destination");
        Double[] breaks = { 2d, 4d, 6d, 8d };
        Integer[] values = { -50, -10, 0, 10, 50 };
        RenderedImage srcImg = createDoubleTestImage(0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_INT);
    }
    
    @Test
    public void doubleToFloat() {
        System.out.println("   double source to float destination");
        Double[] breaks = { 2d, 4d, 6d, 8d };
        Float[] values = { -50f, -10f, 0f, 10f, 50f };
        RenderedImage srcImg = createDoubleTestImage(0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_FLOAT);
    }
    
    @Test
    public void doubleToDouble() {
        System.out.println("   double source to double destination");
        Double[] breaks = { 2d, 4d, 6d, 8d };
        Double[] values = { -50d, -10d, 0d, 10d, 50d };
        RenderedImage srcImg = createDoubleTestImage(0);
        assertLookup(breaks, values, srcImg, DataBuffer.TYPE_DOUBLE);
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
    
    
    /**
     * Runs the lookup operation and tests destination image values.
     *
     * @param breaks source image breakpoints
     * @param values lookup values
     * @param srcImg source image
     * @param destDataType expected destination image data type
     */
    private <T extends Number & Comparable<? super T>, 
             U extends Number & Comparable<? super U>>
            void assertLookup(
                    T[] breaks, U[] values, 
                    RenderedImage srcImg,
                    int destDataType) {
        
        RangeLookupTable<T, U> table = createTableFromBreaks(breaks, values);
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
                Number expectedVal = table.getLookupItem(srcVal).getValue();
                
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
