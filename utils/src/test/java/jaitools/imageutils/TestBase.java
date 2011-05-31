/*
 * Copyright 2011 Michael Bedward
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

package jaitools.imageutils;

import java.awt.Rectangle;
import java.util.Arrays;
import javax.media.jai.TiledImage;

/**
 *
 * @author michael
 */
public abstract class TestBase {
    
    
    /**
     * Creates an image filled with sequential integer values, ordered by pixel, line, band.
     * 
     * @param width image width
     * @param height image height
     * @param numBands number of bands
     * 
     * @return the new image
     */
    protected TiledImage createSequentialImage(final int width, final int height, 
            final int numBands) {
        return createSequentialImage(width, height, numBands, 0);
    }

    /**
     * Creates an image filled with sequential integer values, ordered by pixel, line, band.
     * 
     * @param width image width
     * @param height image height
     * @param numBands number of bands
     * @param startValue the starting (minimum) value at image location (0,0,0)
     * 
     * @return the new image
     */
    protected TiledImage createSequentialImage(final int width, final int height, 
            final int numBands, final int startValue) {
        return createSequentialImage(0, 0, width, height, numBands, startValue);
    }

    /**
     * Creates an image filled with sequential integer values, ordered by pixel, line, band.
     * 
     * @param width image width
     * @param height image height
     * @param numBands number of bands
     * @param startValue the starting (minimum) value at image location (0,0,0)
     * 
     * @return the new image
     */
    protected TiledImage createSequentialImage(final int minx, final int miny,
            final int width, final int height, 
            final int numBands, final int startValue) {

        return createSequentialTiledImage(
                minx, miny, width, height, width, height, numBands, startValue);
    }

    protected TiledImage createSequentialTiledImage(
            final int minx, final int miny, final int width, final int height,
            final int tileWidth, final int tileHeight, final int numBands,
            final int startValue) {
        
        Integer[] fillValues = new Integer[numBands];
        Arrays.fill(fillValues, Integer.valueOf(0));
        
        TiledImage img = ImageUtils.createConstantImage(
                minx, miny, width, height, tileWidth, tileHeight, fillValues);
        
        int k = startValue;
        for (int b = 0; b < numBands; b++) {
            for (int y = miny, iy = 0; iy < height; y++, iy++) {
                for (int x = minx, ix = 0; ix < width; x++, ix++) {
                    img.setSample(x, y, b, k++);
                }
            }
        }

        return img;
    }
    
    protected Rectangle createAdjustedBounds(Rectangle src, int delta) {
        Rectangle dest = new Rectangle(
                src.x - delta, src.y - delta,
                src.width + 2 * delta, src.height + 2 * delta);

        return dest;
    }

    protected void moveToEnd(AbstractSimpleIterator iter) {
        if (iter == null) {
            throw new IllegalStateException("You forgot to create the iterator first");
        }
        
        Rectangle bounds = iter.getBounds();
        int n = bounds.width * bounds.height;
        while (n > 0) {
            iter.next();
            n-- ;
        }
    }
}
