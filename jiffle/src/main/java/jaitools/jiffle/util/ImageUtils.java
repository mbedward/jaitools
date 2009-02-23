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

package jaitools.jiffle.util;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

/**
 * Static helper functions for common image tasks.
 * 
 * @author Michael Bedward
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

}
