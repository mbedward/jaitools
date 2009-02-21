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

package jaitools.jiffle;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

/**
 *
 * @author Michael Bedward
 */
public class JiffleUtilities {

    /**
     * Creates a new TiledImage object with one or more bands filled with zero
     * values
     * @param width image width in pixels
     * @param height image height in pixels
     * @param numBands number of image bands
     * @return a new TiledImage object
     */
    static TiledImage createDoubleImage(int width, int height, int numBands) {
        return createDoubleImage(width, height, new double[] {0});
    }

    
    /**
     * Creates a new TiledImage object with one or more bands of constant value.
     * The number of bands in the output image corresponds to the length of
     * the input values array
     * @param width image width in pixels
     * @param height image height in pixels
     * @param values array of double values 
     * @return a new TiledImage object
     */
    public static TiledImage createDoubleImage(int width, int height, double[] values) {
        
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
