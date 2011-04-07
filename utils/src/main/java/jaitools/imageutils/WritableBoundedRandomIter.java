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
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.awt.image.WritableRenderedImage;

import jaitools.numeric.NumberOperations;

/**
 * A writable random-access image iterator.
 * 
 * @author Michael Bedward
 * @since 1.2
 * @version $Id$
 */
public class WritableBoundedRandomIter extends BoundedRandomIter {
    
    private final WritableRenderedImage writableImage;
    private final boolean floatType;

    /**
     * Creates a new instance. Package-private: to be called by {@link IterFactory}
     * methods.
     * 
     * @param image the writable image
     * @param bounds the iterator bounds or {@code null} for the image bounds
     * 
     * @throws IllegalArgumentException if {@code image} is {@code null}
     */
    WritableBoundedRandomIter(WritableRenderedImage image, Rectangle bounds) {
        super(image, bounds);
        
        this.writableImage = image;
        this.floatType = dataType == DataBuffer.TYPE_FLOAT;
    }

    /**
     * Sets an image value. If the location is outside this iterator's
     * bounds no change is made and the method returns {@code false}.
     * 
     * @param x X ordinate
     * @param y Y ordinate
     * @param band image band
     * @param value new value
     * 
     * @return {@code true} if the image value was set; {@code false}
     *         if the location was out of bounds for this iterator
     * 
     * @throws IllegalArgumentException if value is {@code null}
     */
    public boolean setSample(int x, int y, int band, Number value) {
        int ivalue;
        float fvalue;
        double dvalue;

        if (bounds.contains(x, y)) {
            int tileX = image.XToTileX(x);
            int tileY = image.YToTileY(y);
            WritableRaster tile = writableImage.getWritableTile(tileX, tileY);

            if (integralType) {
                int ival = NumberOperations.intValue(value);
                tile.setSample(x, y, band, ival);
                
            } else if (floatType) {
                float fval = NumberOperations.floatValue(value);
                tile.setSample(x, y, band, fval);
                
            } else {
                double dval = NumberOperations.doubleValue(value);
                tile.setSample(x, y, band, dval);
            }
            
            setLastPos(x, y);
            return true;
            
        } else {
            clearLastPos();
            return false;
        }
    }

}
