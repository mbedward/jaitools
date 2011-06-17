/* 
 *  Copyright (c) 2009, Michael Bedward. All rights reserved. 
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
