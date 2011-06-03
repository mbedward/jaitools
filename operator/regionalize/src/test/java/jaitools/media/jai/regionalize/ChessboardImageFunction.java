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

package jaitools.media.jai.regionalize;

import javax.media.jai.ImageFunction;

/**
 * Creates a chessboard-style image with alternating squares of value
 * 1 and 0
 *
 * @author Michael Bedward
 */
public class ChessboardImageFunction implements ImageFunction {

    private int squareW;

    ChessboardImageFunction(int squareW) {
        this.squareW = squareW;
    }

    public boolean isComplex() {
        return false;
    }

    public int getNumElements() {
        return 1;
    }

    public void getElements(float startX, float startY, float deltaX, float deltaY,
            int countX, int countY, int element, float[] real, float[] imag) {

        int k = 0;
        for (float y = startY, ny = 0; ny < countY; ny++, y += deltaY) {
            int oddRow = ((int)(y+0.5) / squareW) % 2;

            for (float x = startX, nx = 0; nx < countX; nx++, x += deltaX, k++) {
                int oddCol = ((int)(x + 0.5) / squareW) % 2;

                if (oddRow == oddCol) {
                    real[k] = 1.0f;
                } else {
                    real[k] = 0.0f;
                }
            }
        }
    }

    public void getElements(double startX, double startY, double deltaX, double deltaY,
            int countX, int countY, int element, double[] real, double[] imag) {

        int k = 0;
        for (double y = startY, ny = 0; ny < countY; ny++, y += deltaY) {
            int oddRow = ((int)(y+0.5) / squareW) % 2;

            for (double x = startX, nx = 0; nx < countX; nx++, x += deltaX, k++) {
                int oddCol = ((int)(x + 0.5) / squareW) % 2;

                if (oddRow == oddCol) {
                    real[k] = 1.0f;
                } else {
                    real[k] = 0.0f;
                }
            }
        }
    }

}
