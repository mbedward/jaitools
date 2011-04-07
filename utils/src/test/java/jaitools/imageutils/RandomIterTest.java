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

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;
import java.util.Random;

import javax.media.jai.TiledImage;

import jaitools.numeric.CompareOp;
import java.awt.Rectangle;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import jaitools.CollectionFactory;
import java.io.ByteArrayOutputStream;
import java.util.logging.Formatter;
import java.util.logging.SimpleFormatter;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for the JAI-tools random image iterator classes.
 * 
 * @author Michael Bedward
 * @since 1.2
 * @version $Id$
 */
public class RandomIterTest {
    
    private final Random rand = new Random();
    
    private static final int[] DATA_TYPES = {
        DataBuffer.TYPE_BYTE,
        DataBuffer.TYPE_DOUBLE,
        DataBuffer.TYPE_FLOAT,
        DataBuffer.TYPE_INT,
        DataBuffer.TYPE_SHORT,
        DataBuffer.TYPE_USHORT
    };

    private static final Map<Integer, String> DATA_TYPE_NAMES = CollectionFactory.map();
    static {
        DATA_TYPE_NAMES.put(DataBuffer.TYPE_BYTE, "TYPE_BYTE");
        DATA_TYPE_NAMES.put(DataBuffer.TYPE_SHORT, "TYPE_SHORT");
        DATA_TYPE_NAMES.put(DataBuffer.TYPE_USHORT, "TYPE_USHORT");
        DATA_TYPE_NAMES.put(DataBuffer.TYPE_INT, "TYPE_INT");
        DATA_TYPE_NAMES.put(DataBuffer.TYPE_FLOAT, "TYPE_FLOAT");
        DATA_TYPE_NAMES.put(DataBuffer.TYPE_DOUBLE, "TYPE_DOUBLE");
    }
    


    @Test
    public void createIter() throws Exception {
        System.out.println("   create iterator");
        RenderedImage image = createImage(DataBuffer.TYPE_INT, 0, 0, 10, 10);
        BoundedRandomIter iter = IterFactory.createBoundedRandomIter(image, null);

        assertNotNull(iter);
    }

    @Test
    public void createWritableIter() throws Exception {
        System.out.println("   create writable iterator");
        WritableRenderedImage image = createImage(DataBuffer.TYPE_INT, 0, 0, 10, 10);
        BoundedRandomIter iter = IterFactory.createWritableBoundedRandomIter(image, null);

        assertNotNull(iter);
    }

    @Test
    public void testGetSample() throws Exception {
        System.out.println("   getSample for all data types");
        
        for (int dataType : DATA_TYPES) {
            TiledImage image = createRandomImage(dataType, -5, 5, 10, 10);
            BoundedRandomIter iter = IterFactory.createBoundedRandomIter(image, null);
            assertGetSample(dataType, iter, image, null);
        }
    }
    
    @Test
    public void testGetBoundedSample() throws Exception {
        System.out.println("   getSample within bounds for all data types");
        
        Rectangle bounds = new Rectangle(-2, 8, 5, 5);
        for (int dataType : DATA_TYPES) {
            TiledImage image = createRandomImage(dataType, -5, 5, 10, 10);
            BoundedRandomIter iter = IterFactory.createBoundedRandomIter(image, bounds);
            assertGetSample(dataType, iter, image, bounds);
        }
    }
    
    @Test
    public void testBoundsPartiallyOverlappingImage() throws Exception {
        System.out.println("   iterator bounds partially overlap image bounds");
        
        int dataType = DataBuffer.TYPE_INT;
        TiledImage image = createRandomImage(dataType, 0, 0, 10, 10);
        Rectangle bounds = new Rectangle(-5, -5, 10, 10);
        BoundedRandomIter iter = IterFactory.createBoundedRandomIter(image, bounds);
        Number sample = iter.getSample(-5, -5, 0);
    }
    
    @Test
    public void testBoundsOutsideImage() throws Exception {
        System.out.println("   iterator bounds fully outside image bounds");
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Formatter formatter = new SimpleFormatter();
        Handler handler = new StreamHandler(out, formatter);
        
        Logger logger = Logger.getLogger(BoundedRandomIter.class.getName());
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
        
        TiledImage image = createRandomImage(DataBuffer.TYPE_BYTE, 0, 0, 10, 10);
        Rectangle bounds = new Rectangle(100, 100, 10, 10);
        BoundedRandomIter iter = IterFactory.createBoundedRandomIter(image, bounds);
        
        handler.flush();
        String logMsg = out.toString();
        assertTrue(logMsg.contains("do not intersect"));
        
        logger.removeHandler(handler);
        logger.setUseParentHandlers(true);
    }
    
    private void assertGetSample(int dataType, BoundedRandomIter iter, 
            TiledImage image, Rectangle iterBounds) {
        
        final int w = image.getWidth();
        final int h = image.getHeight();
        if (iterBounds == null) {
            iterBounds = new Rectangle(
                    image.getMinX(), image.getMinY(), image.getWidth(), image.getHeight());
        }
        
        final String TYPE_NAME = DATA_TYPE_NAMES.get(dataType);
        
        for (int y = image.getMinY(), iy = 0; iy < h; y++, iy++) {
            for (int x = image.getMinX(), ix = 0; ix < w; x++, ix++) {
                Number sample = iter.getSample(x, y, 0);

                if (!iterBounds.contains(x, y)) {
                    assertNull(sample);
                    
                } else {
                    switch (dataType) {
                        case DataBuffer.TYPE_BYTE:
                            assertEquals(TYPE_NAME, image.getSample(x, y, 0) & 0xff,
                                    sample.byteValue() & 0xff);
                            break;

                        case DataBuffer.TYPE_INT:
                        case DataBuffer.TYPE_SHORT:
                        case DataBuffer.TYPE_USHORT:
                            assertEquals(TYPE_NAME, image.getSample(x, y, 0),
                                    sample.intValue());
                            break;

                        case DataBuffer.TYPE_DOUBLE:
                            assertEquals(TYPE_NAME, image.getSampleDouble(x, y, 0),
                                    sample.doubleValue(), CompareOp.DTOL);
                            break;

                        case DataBuffer.TYPE_FLOAT:
                            assertEquals(TYPE_NAME, image.getSampleFloat(x, y, 0),
                                    sample.floatValue(), CompareOp.FTOL);
                            break;
                    }
                }
            }
        }
    }

    private TiledImage createRandomImage(int dataType,
            final int xo, final int yo,
            final int width, final int height) {
        
        int ival;
        float fval;
        double dval;
        
        final double DMAX = 1.0e8;
        final float FMAX = 1.0e4f;

        TiledImage image = createImage(dataType, xo, yo, width, height);

        for (int y = yo, iy = 0; iy < height; y++, iy++) {
            for (int x = xo, ix = 0; ix < width; x++, ix++) {
                switch (dataType) {
                    case DataBuffer.TYPE_BYTE:
                        ival = rand.nextInt(256);
                        image.setSample(x, y, 0, ival & 0xff);
                        break;
                        
                    case DataBuffer.TYPE_DOUBLE:
                        dval = DMAX * (2 * rand.nextDouble() - 1);
                        image.setSample(x, y, 0, dval);
                        break;

                    case DataBuffer.TYPE_FLOAT:
                        fval = FMAX * (2 * rand.nextFloat() - 1);
                        image.setSample(x, y, 0, fval);
                        break;

                    case DataBuffer.TYPE_INT:
                        ival = rand.nextInt();
                        if (rand.nextDouble() < 0.5d) {
                            ival = -ival;
                        }
                        image.setSample(x, y, 0, ival);
                        break;

                    case DataBuffer.TYPE_SHORT:
                        ival = rand.nextInt(Short.MAX_VALUE);
                        if (rand.nextDouble() < 0.5d) {
                            ival = -ival;
                        }
                        image.setSample(x, y, 0, ival);
                        break;
                        
                    case DataBuffer.TYPE_USHORT:
                        ival = rand.nextInt(Short.MAX_VALUE);
                        image.setSample(x, y, 0, ival);
                        break;

                    default:
                        throw new IllegalArgumentException("Invalid value for dataType: " + dataType);
                }
            }
        }
        
        return image;
    }
    

    private TiledImage createImage(int dataType, int x, int y, int width, int height) {
        switch (dataType) {
            case DataBuffer.TYPE_BYTE:
                return ImageUtils.createConstantImage(x, y, width, height, Byte.valueOf((byte) 0));

            case DataBuffer.TYPE_DOUBLE:
                return ImageUtils.createConstantImage(x, y, width, height, 0d);

            case DataBuffer.TYPE_FLOAT:
                return ImageUtils.createConstantImage(x, y, width, height, 0f);

            case DataBuffer.TYPE_INT:
                return ImageUtils.createConstantImage(x, y, width, height, 0);

            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT:
                return ImageUtils.createConstantImage(x, y, width, height, Short.valueOf((short) 0));

            default:
                throw new IllegalArgumentException("Invalid value for dataType: " + dataType);
        }
    }

}
