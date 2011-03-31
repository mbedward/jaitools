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

package jaitools.media.jai.kernel;

import java.util.Arrays;



/**
 * A helper class for KernelFactory. This is package-private and not
 * intended for use by client code.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
class KernelFactoryHelper {
    private static final float FTOL = 1.0e-8f;
    private static final double DTOL = 1.0e-8d;


    /**
     * Constructor. Empty: defined only to ensure that it is package private.
     */
    KernelFactoryHelper() { }

    
    /**
     * Create a new float array with data for a raster circle outline
     * of specified radius
     */
    float[] makeCircle(int radius) {
        int w = 2*radius + 1;
        float[] m = new float[w*w];

        int[] offset = new int[w];
        for (int i = 0, o = 0; i < w; i++, o += w) offset[i] = o;

        int x = radius, y = 0;
        int r2 = radius * radius;
        while (x > 0) {
            int ix = radius + x;
            int iy = radius + y;
            m[ix + offset[iy]] = 1f;
            m[w - 1 - ix + offset[iy]] = 1f;
            iy = w - 1 - iy;
            m[ix + offset[iy]] = 1f;
            m[w - 1 - ix + offset[iy]] = 1f;
            y-- ;
            x = (int)Math.sqrt(r2 - y*y);
        }

        m[radius] = 1f;
        m[radius + offset[2*radius]] = 1f;

        return m;
    }

    /**
     * Creates an array for a rectangular kernel with
     * all elements set to {@code value}.
     *
     * @param w width
     * @param h height
     * @param value value for all elements
     */
    float[] makeRect(int w, int h, float value) {
        float[] m = new float[w*h];
        Arrays.fill(m, value);
        return m;
    }

    /**
     * Takes a float array with data for a closed raster shape
     * and fills the shape by setting element values to {@code value}
     * 
     * @param data data array of length {@code w} * {@code h}
     * @param w width
     * @param h height
     */
    void rowFill(float[] data, int w, int h, float value) {

        int k = 0;
        for (int y = 0; y < h; y++) {
            int left = -1, right = -1;
            for (int x = 0; x < w; x++, k++) {
                if (data[k] > 0) {
                    if (left < 0) {
                        left = k;
                    } else {
                        right = k;
                    }
                }
            }

            while (right > left+1) {
                data[--right] = value;
            }
        }
    }

    /**
     * Normalize the input angle to be between -PI and PI
     * @param angle input angle (radians)
     * @return normalized angle
     */
    float normalizeAngle(float angle) {
        while (angle < -Math.PI) {
            angle += Math.PI*2;
        }

        while (angle > Math.PI) {
            angle -= Math.PI*2;
        }

        return angle;
    }

    /**
     * Normalize the input angle to be between 0 and 2*PI
     * @param angle input angle (radians)
     * @return normalized angle
     */
    float normalizeAnglePositive(float angle) {
        while (angle < 0) {
            angle += Math.PI * 2;
        }

        while (angle > Math.PI * 2) {
            angle -= Math.PI * 2;
        }

        return angle;
    }

}
