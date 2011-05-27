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
import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;

import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;
import javax.media.jai.iterator.WritableRectIter;

/**
 *
 * @author michael
 */
public class WritableSimpleIter extends AbstractSinglePixelIterator {
    
    private static class Helper implements DelegateHelper {

        public RectIter create(RenderedImage image, Rectangle bounds) {
            if (image == null) {
                throw new IllegalArgumentException("image must not be null");
            }
            if (!(image instanceof WritableRenderedImage)) {
                throw new IllegalArgumentException("image must be a WritableRenderedImage");
            }
            if (bounds == null || bounds.isEmpty()) {
                return null;
            }
            return RectIterFactory.createWritable((WritableRenderedImage) image, bounds);
        }
        
    }

    public WritableSimpleIter(WritableRenderedImage image, Rectangle bounds, Number outsideValue) {
        super(new Helper(), image, bounds, outsideValue);
    }

    public boolean setSample(Number value) {
        return setSample(0, value);
    }


    public boolean setSample(int band, Number value) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        
        if (!finished && delegateBounds.contains(mainPos)) {
            WritableRenderedImage image = (WritableRenderedImage) imageRef.get();
            if (image == null) {
                throw new IllegalStateException("Target image has been deleted");
            }

            WritableRectIter writableIter = (WritableRectIter) delegateIter;
            
            switch (imageDataType) {
                case DataBuffer.TYPE_DOUBLE:
                    writableIter.setSample(band, value.doubleValue());
                    break;

                case DataBuffer.TYPE_FLOAT:
                    writableIter.setSample(band, value.floatValue());
                    break;

                default:
                    writableIter.setSample(band, value.intValue());
                    break;
            }
            return true;

        } else {
            return false;
        }
    }
    

}
