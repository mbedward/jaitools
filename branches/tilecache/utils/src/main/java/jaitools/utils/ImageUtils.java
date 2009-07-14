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

package jaitools.utils;

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import javax.media.jai.JAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

/**
 * Static helper functions for common image tasks.
 * 
 * @author Michael Bedward
 * @since 1.0
 * $Id$
 */
public class ImageUtils {

    /**
     * Creates a new TiledImage object with a single band filled with zero
     * values
     * @param width image width in pixels
     * @param height image height in pixels
     * @return a new TiledImage object
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
     * A helper method that creates a 3-band colour image, using a colour ramp,
     * for a data image with integral data type and pixel values between 0 and ncol.
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
     * Get the bands of a multi-band image as a List of single-band images.
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
     * Get the specified bands of a multi-band image as a List of single-band images.
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
