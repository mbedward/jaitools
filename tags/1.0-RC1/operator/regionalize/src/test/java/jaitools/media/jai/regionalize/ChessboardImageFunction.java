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
