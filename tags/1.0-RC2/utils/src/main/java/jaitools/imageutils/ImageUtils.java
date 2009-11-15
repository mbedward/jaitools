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

import jaitools.CollectionFactory;
import java.awt.Color;
import java.awt.image.RenderedImage;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import javax.media.jai.JAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RectIterFactory;
import javax.media.jai.iterator.WritableRectIter;

/**
 * Static helper functions for common image tasks.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class ImageUtils {

    /**
     * Creates a new TiledImage object with a single band filled with zero
     * values
     * @param width image width in pixels
     * @param height image height in pixels
     * @return a new TiledImage object
     *
     * @deprecated This method will be removed in version 1.1.
     *             Please use {@linkplain #createConstantImage(int, int, java.lang.Number)}
     */
    public static TiledImage createDoubleImage(int width, int height) {
        return createDoubleImage(width, height, new double[] {0});
    }

    /**
     * Creates a new TiledImage object with one or more bands filled with zero
     * values
     * @param width image width in pixels
     * @param height image height in pixels
     * @param numBands number of image bands (must be >= 1)
     * @return a new TiledImage object
     *
     * @deprecated This method will be removed in version 1.1.
     *             Please use {@linkplain #createConstantImage(int, int, java.lang.Number)}
     */
    public static TiledImage createDoubleImage(int width, int height, int numBands) {
        if (numBands < 1) {
            throw new IllegalArgumentException("numBands must be at least 1");
        }
        
        double[] bandValues = new double[numBands];
        for (int i = 0; i < numBands; i++) { bandValues[i] = 0d; }
        return createDoubleImage(width, height, bandValues);
    }

    
    /**
     * Creates a new TiledImage object with one or more bands of constant value.
     * The number of bands in the output image corresponds to the length of
     * the input values array
     * @param width image width in pixels
     * @param height image height in pixels
     * @param values array of double values (must contain at least one element)
     * @return a new TiledImage object
     *
     * @deprecated This method will be removed in version 1.1.
     *             Please use {@linkplain #createConstantImage(int width, int height, Number[] values)}
     */
    public static TiledImage createDoubleImage(int width, int height, double[] values) {
        if (values == null || values.length < 1) {
            throw new IllegalArgumentException("values array must contain at least 1 value");
        }
        
        Double[] dvalues = new Double[values.length];
        for (int i = 0; i < values.length; i++) {
            dvalues[i] = Double.valueOf(values[i]);
        }
        
        ParameterBlockJAI pb = new ParameterBlockJAI("constant");
        pb.setParameter("width", (float)width);
        pb.setParameter("height", (float)height);
        pb.setParameter("bandValues", dvalues);
        
        RenderedOp op = JAI.create("constant", pb);
        return new TiledImage(op, false);
    }

    /**
     * Creates a new TiledImage object with a single band of constant value.
     * The data type of the image corresponds to the class of {@code value}.
     *
     * @param width image width in pixels
     *
     * @param height image height in pixels
     *
     * @param value the constant value to fill the image
     *
     * @return a new TiledImage object
     */
    public static TiledImage createConstantImage(int width, int height, Number value) {
        return createConstantImage(width, height, new Number[] {value});
    }

    /**
     * Creates a new TiledImage object with one or more bands of constant value.
     * The number of bands in the output image corresponds to the length of
     * the input values array and the data type of the image corresponds to the
     * {@code Number} class used.
     *
     * @param width image width in pixels
     *
     * @param height image height in pixels
     *
     * @param values array of values (must contain at least one element)
     *
     * @return a new TiledImage object
     */
    public static TiledImage createConstantImage(int width, int height, Number[] values) {
        if (values == null || values.length < 1) {
            throw new IllegalArgumentException("values array must contain at least 1 value");
        }

        ParameterBlockJAI pb = new ParameterBlockJAI("constant");
        pb.setParameter("width", (float)width);
        pb.setParameter("height", (float)height);

        Number[] typedValues = null;
        if (values[0] instanceof Double) {
            typedValues = new Double[values.length];
        } else if (values[0] instanceof Float) {
            typedValues = new Float[values.length];
        } else if (values[0] instanceof Integer) {
            typedValues = new Integer[values.length];
        } else if (values[0] instanceof Short) {
            typedValues = new Short[values.length];
        } else if (values[0] instanceof Byte) {
            typedValues = new Byte[values.length];
        } else {
            throw new UnsupportedOperationException("Unsupported data type: " +
                    values[0].getClass().getName());
        }

        for (int i = 0; i < values.length; i++) {
            typedValues[i] = values[i];
        }
        
        pb.setParameter("bandValues", typedValues);

        RenderedOp op = JAI.create("constant", pb);
        return new TiledImage(op, false);
    }
    
    /**
     * Create a set of colours using a simple colour ramp algorithm in the HSB colour space.
     *
     * @param numColours number of colours required
     *
     * @return an array of colours sampled from the HSB space.
     */
    public static Color[] createRampColours(int numColours) {
        return createRampColours(numColours, 0.8f, 0.8f);
    }

    /**
     * Create a set of colours using a simple colour ramp algorithm in the HSB colour space.
     *
     * @param numColours number of colours required
     *
     * @param saturation the saturation of all colours (between 0 and 1)
     *
     * @param brightness the brightness of all colours (between 0 and 1)
     *
     * @return an array of colours sampled from the HSB space between the start and end hues
     */
    public static Color[] createRampColours(int numColours, float saturation, float brightness) {
        return createRampColours(numColours, 0.0f, 1.0f, saturation, brightness);
    }

    /**
     * Create a set of colours using a simple colour ramp algorithm in the HSB colour space.
     * All float arguments should be values between 0 and 1.
     *
     * @param numColours number of colours required
     *
     * @param startHue the starting hue
     *
     * @param endHue the ending hue
     *
     * @param saturation the saturation of all colours
     *
     * @param brightness the brightness of all colours
     *
     * @return an array of colours sampled from the HSB space between the start and end hues
     */
    public static Color[] createRampColours(int numColours, float startHue, float endHue,
            float saturation, float brightness) {

        Color[] colors = new Color[numColours];

        final float increment = numColours > 1 ? (endHue - startHue) / (float)(numColours - 1) : 0f;
        float hue = startHue;
        for (int i = 0; i < numColours; i++) {
            int rgb = Color.HSBtoRGB(hue, saturation, brightness);
            colors[i] = new Color(rgb);
            hue += increment;
        }

        return colors;
    }

    /**
     * A helper method that creates a 3-band colour image, using a colour ramp,
     * for a data image with integral data type and pixel values between 1 and
     * {@code ncol-1}.
     * <p>
     * This method is only preliminary and, presently, very limited in the
     * input images that it will deal with.
     *
     * @param dataImg the data image: presently assumed to be single-band with
     * an integral data type
     *
     * @param ncol number of colours to be created using a colour ramp
     *
     * @return a three band image with an RGB ColorModel
     * 
     * @deprecated This method will be removed in version 1.1.
     *             Please use {@linkplain #createDisplayImage(RenderedImage, Map)} instead.
     *             Colour ramp colours can be generated with
     *             {@linkplain #createRampColours(int)}.
     */
    public static RenderedImage createDisplayImage(RenderedImage dataImg, int ncol) {

        byte[][] lookup = new byte[3][ncol];

        float hue = 0f;
        float hueIncr = 1f / (float)ncol;
        for (int i = 0; i < ncol; i++) {
            int colour = Color.HSBtoRGB(hue, 0.7f, 0.7f);
            lookup[0][i] = (byte) ((colour & 0x00ff0000) >> 16);
            lookup[1][i] = (byte) ((colour & 0x0000ff00) >> 8);
            lookup[2][i] = (byte) (colour & 0x000000ff);
            hue += hueIncr;
        }

        ParameterBlockJAI pb = new ParameterBlockJAI("lookup");
        pb.setSource("source0", dataImg);
        pb.setParameter("table", new LookupTableJAI(lookup, 1));
        RenderedOp displayImg = JAI.create("lookup", pb);
        return displayImg;
    }

    /**
     * Creates a proxy RGB display image for the given data image. The data image should be
     * of integral data type. Only the first band of multi-band images will be used.
     *
     * @param dataImg the data image
     *
     * @param colourTable a lookup table giving colours for each data image value
     *
     * @return a new RGB image
     */
    public static RenderedImage createDisplayImage(RenderedImage dataImg, Map<Integer, Color> colourTable) {

        if (colourTable.size() > 256) {
            throw new IllegalArgumentException("Number of colours can't be more than 256");
        }

        Integer maxKey = null;
        Integer minKey = null;
        for (Integer key : colourTable.keySet()) {
            if (minKey == null) {
                minKey = maxKey = key;

            } else if (key < minKey) {
                minKey = key;
            } else if (key > maxKey) {
                maxKey = key;
            }
        }

        ParameterBlockJAI pb = null;
        RenderedImage lookupImg = dataImg;
        byte[][] lookup = null;
        int offset = 0;

        if (minKey < 0 || maxKey > 255) {
            lookupImg = createConstantImage(dataImg.getWidth(), dataImg.getHeight(), Integer.valueOf(0));

            TreeMap<Integer, Integer> keyTable = CollectionFactory.newTreeMap();
            int k = 0;
            for (Integer key : colourTable.keySet()) {
                keyTable.put(key, k++);
            }

            WritableRectIter iter = RectIterFactory.createWritable((TiledImage)lookupImg, null);
            do {
                do {
                    do {
                        iter.setSample( keyTable.get(iter.getSample()) );
                    } while (!iter.nextPixelDone());
                    iter.startPixels();
                } while (!iter.nextLineDone());
                iter.startLines();
            } while (!iter.nextBandDone());

            lookup = new byte[3][colourTable.size()];
            for (Integer key : keyTable.keySet()) {
                int index = keyTable.get(key);
                int colour = colourTable.get(key).getRGB();
                lookup[0][index] = (byte) ((colour & 0x00ff0000) >> 16);
                lookup[1][index] = (byte) ((colour & 0x0000ff00) >> 8);
                lookup[2][index] = (byte) (colour & 0x000000ff);
            }

        } else {
            lookup = new byte[3][maxKey - minKey + 1];
            offset = minKey;

            for (Integer key : colourTable.keySet()) {
                int colour = colourTable.get(key).getRGB();
                lookup[0][key - offset] = (byte) ((colour & 0x00ff0000) >> 16);
                lookup[1][key - offset] = (byte) ((colour & 0x0000ff00) >> 8);
                lookup[2][key - offset] = (byte) (colour & 0x000000ff);
            }
        }

        pb = new ParameterBlockJAI("Lookup");
        pb.setSource("source0", lookupImg);
        pb.setParameter("table", new LookupTableJAI(lookup, offset));
        RenderedOp displayImg = JAI.create("Lookup", pb);
        
        return displayImg;
    }

    /**
     * Get the bands of a multi-band image as a list of single-band images. This can
     * be used, for example, to separate the result image returned by the KernelStats
     * operator into separate result images.
     *
     * @param img the multi-band image
     * @return a List of new single-band images
     */
    public static List<RenderedImage> getBandsAsImages(RenderedImage img) {
        List<RenderedImage> images = CollectionFactory.newList();

        if (img != null) {
            int numBands = img.getSampleModel().getNumBands();
            for (int band = 0; band < numBands; band++) {
                ParameterBlockJAI pb = new ParameterBlockJAI("BandSelect");
                pb.setSource("source0", img);
                pb.setParameter("bandindices", new int[]{band});
                RenderedImage bandImg = JAI.create("BandSelect", pb);
                images.add(bandImg);
            }
        }

        return images;
    }

    /**
     * Get the specified bands of a multi-band image as a list of single-band images. This can
     * be used, for example, to separate the result image returned by the KernelStats
     * operator into separate result images.
     *
     * @param img the multi-band image
     * @param bandIndices a Collection of Integer indices in the range 0 <= i < number of bands
     * @return a List of new single-band images
     */
    public static List<RenderedImage> getBandsAsImages(RenderedImage img, Collection<Integer> bandIndices) {
        List<RenderedImage> images = CollectionFactory.newList();

        if (img != null) {
            int numBands = img.getSampleModel().getNumBands();
            SortedSet<Integer> sortedIndices = CollectionFactory.newTreeSet();
            sortedIndices.addAll(bandIndices);

            if (sortedIndices.first() < 0 || sortedIndices.last() >= numBands) {
                throw new IllegalArgumentException("band index out of bounds");
            }

            for (Integer band : sortedIndices) {
                ParameterBlockJAI pb = new ParameterBlockJAI("BandSelect");
                pb.setSource("source0", img);
                pb.setParameter("bandindices", new int[]{band});
                RenderedImage bandImg = JAI.create("BandSelect", pb);
                images.add(bandImg);
            }
        }

        return images;
    }
}
