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
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.numeric.Range;
import java.util.Arrays;
import javax.media.jai.RenderedOp;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the RangeLookup operation.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class RangeLookupTest {

    private Map<String, Object> args;

    @Before
    public void setup() {
        args = CollectionFactory.map();
    }
    
    @Test
    public void byteToByte() throws Exception {
        System.out.println("   byte source to byte dest");
        
        Byte[] breaks = { 2, 4, 6, 8 };
        Byte[] values = { 0, 1, 2, 3, 4 };
        assertLookup(breaks, values, DataBuffer.TYPE_BYTE, Byte.class);
    }
    
    @Test
    public void shortToShort() throws Exception {
        System.out.println("   short source to short dest");
        
        Short[] breaks = { 2, 4, 6, 8 };
        
        // Note: need -ve values to force destination image to be TYPE_SHORT
        // otherwise it will be TYPE_USHORT
        Short[] values = { -50, -10, 0, 10, 50 };
        
        assertLookup(breaks, values, DataBuffer.TYPE_SHORT, Short.class);
    }
    
    @Test
    public void intToInt() throws Exception {
        System.out.println("   int source to int dest");
        
        Integer[] breaks = { 2, 4, 6, 8 };
        Integer[] values = { -50, -10, 0, 10, 50 };
        assertLookup(breaks, values, DataBuffer.TYPE_INT, Integer.class);
    }
    
    @Test
    public void floatToFloat() throws Exception {
        System.out.println("   float source to float dest");
        
        Float[] breaks = { 2f, 4f, 6f, 8f };
        Float[] values = { -50f, -10f, 0f, 10f, 50f };
        assertLookup(breaks, values, DataBuffer.TYPE_FLOAT, Float.class);
    }
    
    @Test
    public void doubleToDouble() throws Exception {
        System.out.println("   double source to double dest");
        
        Double[] breaks = { 2d, 4d, 6d, 8d };
        Double[] values = { -50d, -10d, 0d, 10d, 50d };
        assertLookup(breaks, values, DataBuffer.TYPE_DOUBLE, Double.class);
    }
    
    @Test
    public void floatToInt() throws Exception {
        System.out.println("   float source to int dest");
        
        Float[] breaks = { 2f, 4f, 6f, 8f };
        Integer[] values = { -50, -10, 0, 10, 50 };
        assertLookup(breaks, values, DataBuffer.TYPE_INT, Integer.class);
    }
    
    @Test
    public void floatToShort() throws Exception {
        System.out.println("   float source to short dest");
        
        Float[] breaks = { 2f, 4f, 6f, 8f };
        Short[] values = { -50, -10, 0, 10, 50 };
        assertLookup(breaks, values, DataBuffer.TYPE_SHORT, Short.class);
    }
    
    @Test
    public void floatToByte() throws Exception {
        System.out.println("   float source to byte dest");
        
        Float[] breaks = { 2f, 4f, 6f, 8f };
        Byte[] values = { 0, 1, 2, 3, 4 };
        assertLookup(breaks, values, DataBuffer.TYPE_BYTE, Byte.class);
    }
    
    @Test
    public void doubleToFloat() throws Exception {
        System.out.println("   double source to float dest");
        
        Double[] breaks = { 2d, 4d, 6d, 8d };
        Float[] values = { -50f, -10f, 0f, 10f, 50f };
        assertLookup(breaks, values, DataBuffer.TYPE_FLOAT, Float.class);
    }
    
    @Test
    public void doubleToInt() throws Exception {
        System.out.println("   double source to int dest");
        
        Double[] breaks = { 2d, 4d, 6d, 8d };
        Integer[] values = { -50, -10, 0, 10, 50 };
        assertLookup(breaks, values, DataBuffer.TYPE_INT, Integer.class);
    }
    
    @Test
    public void doubleToShort() throws Exception {
        System.out.println("   double source to short dest");
        
        Double[] breaks = { 2d, 4d, 6d, 8d };
        Short[] values = { -50, -10, 0, 10, 50 };
        assertLookup(breaks, values, DataBuffer.TYPE_SHORT, Short.class);
    }
    
    @Test
    public void doubleToByte() throws Exception {
        System.out.println("   double source to byte dest");
        
        Double[] breaks = { 2d, 4d, 6d, 8d };
        Byte[] values = { 0, 1, 2, 3, 4 };
        assertLookup(breaks, values, DataBuffer.TYPE_BYTE, Byte.class);
    }
    
    
    private <T extends Number & Comparable<? super T>, 
             U extends Number & Comparable<? super U>>
            void assertLookup(T[] breaks, U[] values, 
            int dataType, Class<? extends Number> dataClass) {
        
        RangeLookupTable<T, U> table = createTable(breaks, values);

        RenderedImage byteImg = createByteTestImage(10, 10);
        RenderedImage srcImg = null;
        if (dataType == DataBuffer.TYPE_BYTE) {
            srcImg = byteImg;
        } else {
            srcImg = formatImage(byteImg, dataType);
        }
        
        RenderedOp destImg = doOp(srcImg, table);

        // check data type
        assertEquals(dataType, destImg.getSampleModel().getDataType());
        assertImageValues(srcImg, destImg, table, dataClass);
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
     * Changes the data type of the test image
     * 
     * @param srcImg the source image
     * @param type the new data type
     * 
     * @return the destination image
     */
    private RenderedImage formatImage(RenderedImage srcImg, int type) {
        ParameterBlockJAI pb = new ParameterBlockJAI("format");
        pb.setSource("source0", srcImg);
        pb.setParameter("dataType", type);
        return JAI.create("format", pb);
    }

    private RenderedImage createByteTestImage(final int width, final int height) {
        final int N = width * height;
        Byte[] data = new Byte[N];
        byte value = 0;
        for (int pos = 0; pos < N; pos += width) {
            Arrays.fill(data, pos, pos + width, value++);
        }

        return ImageUtils.createImageFromArray(data, width, height);
    }

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

    private void assertImageValues(RenderedImage srcImg, RenderedImage destImg,
            RangeLookupTable table, Class<? extends Number> destClass) {

        RectIter iter = RectIterFactory.create(destImg, null);
        short line = 0;
        do {
            do {
                if (destClass.equals(Short.class)) {
                    assertEquals(table.getDestValue(line), (short) iter.getSample());
                }
                if (destClass.equals(Byte.class)) {
                    assertEquals(table.getDestValue(line), (byte) iter.getSample());
                }
                if (destClass.equals(Integer.class)) {
                    assertEquals(table.getDestValue(line), iter.getSample());
                }
                if (destClass.equals(Float.class)) {
                    assertEquals(table.getDestValue(line), iter.getSampleFloat());
                }
                if (destClass.equals(Double.class)) {
                    assertEquals(table.getDestValue(line), iter.getSampleDouble());
                }

            } while (!iter.nextPixelDone());
            iter.startPixels();
            line++;

        } while (!iter.nextLineDone());
    }

}
