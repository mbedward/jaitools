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

import jaitools.utils.CollectionFactory;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.List;
import javax.media.jai.ImageFunction;

/**
 * Creates images with diamond shaped regions of value 1.0 surrounded
 * by value 0.0
 *
 * @author Michael Bedward
 */
public class DiamondImageFunction implements ImageFunction {

    int width, height;
    List<Shape> diamonds;

    /**
     * Constructor
     * @param width image width
     * @param height image height
     */
    public DiamondImageFunction(int width, int height) {
        this.width = width;
        this.height = height;
        diamonds = CollectionFactory.newList();

        int N = 4;
        int[] xcoords = {10, 20, 10, 0};
        int[] ycoords = {0, 10, 20, 10};
        int[] xs = new int[xcoords.length];
        int[] ys = new int[xcoords.length];

        for (int y = 10; y < height-20; y += 30) {
            for (int i = 0; i < N; i++) {
                ys[i] = ycoords[i] + y;
            }

            for (int x = 10; x < width-20; x += 30) {
                for (int i = 0; i < N; i++) {
                    xs[i] = xcoords[i] + x;
                }

                Polygon diamond = new Polygon();
                for (int i = 0; i < N; i++) {
                    diamond.addPoint(xs[i], ys[i]);
                }
                diamonds.add(diamond);
            }
        }
    }

    public boolean isComplex() {
        return false;
    }

    public int getNumElements() {
        return 1;
    }

    public void getElements(float startX, float startY, float deltaX, float deltaY,
            int countX, int countY, int element, float[] real, float[] imag) {

        for (float y = startY, ny = 0; ny < countY; ny++, y += deltaY) {
            for (float x = startX, nx = 0; nx < countX; nx++, x += deltaX) {
                int index = (int)(y*width + x);
                real[index] = 0f;
                for (Shape diamond : diamonds) {
                    if (diamond.contains(x, y)) {
                        real[index] = 1f;
                        break;
                    }
                }
            }
        }
    }

    public void getElements(double startX, double startY, double deltaX, double deltaY,
            int countX, int countY, int element, double[] real, double[] imag) {

        for (double y = startY, ny = 0; ny < countY; ny++, y += deltaY) {
            for (double x = startX, nx = 0; nx < countX; nx++, x += deltaX) {
                int index = (int)(y*width + x);
                real[index] = 0f;
                for (Shape diamond : diamonds) {
                    if (diamond.contains(x, y)) {
                        real[index] = 1f;
                        break;
                    }
                }
            }
        }
    }

    /**
     * Get the number of diamond shapes that have been defined for
     * this image
     * @return count of shapes
     */
    public int getNumDiamonds() {
        return diamonds.size();
    }

}
