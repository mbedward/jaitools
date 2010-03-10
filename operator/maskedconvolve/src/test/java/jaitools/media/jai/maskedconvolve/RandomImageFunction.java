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

package jaitools.media.jai.maskedconvolve;

import java.util.Random;
import javax.media.jai.ImageFunction;

/**
 * An ImageFunction class to generate an image where pixel
 * values are randomly chosen from a specified range
 *
 * @author Michael Bedward
 */
public class RandomImageFunction implements ImageFunction {

    private Random rand;
    private float minValue;
    private float maxValue;

    /**
     * Constructor
     *
     * @param minValue min pixel value
     * @param maxValue max pixel value
     */
    public RandomImageFunction(float minValue, float maxValue) {
        this.rand = new Random();
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Whether the returned image is complex - always returns false
     */
    public boolean isComplex() {
        return false;
    }

    /**
     * Number of elements per pixel - always 1
     */
    public int getNumElements() {
        return 1;
    }

    /**
     * Called by JAI to generate a float image
     */
    public void getElements(float startX, float startY,
                            float deltaX, float deltaY,
                            int countX, int countY,
                            int element, float[] real, float[] imag) {

        int index = 0;
        float r = maxValue - minValue;
        for (int row = 0; row < countY; row++) {
            for (int col = 0; col < countX; col++, index++) {
                real[index] = (float) (minValue + r * rand.nextDouble());
            }
        }
    }

    /**
     * Not implemented - throws UnsupportedOperationException if called
     */
    public void getElements(double arg0, double arg1, double arg2, double arg3, int arg4, int arg5, int arg6, double[] arg7, double[] arg8) {
        throw new UnsupportedOperationException("This class doesn't support double images");
    }
}
