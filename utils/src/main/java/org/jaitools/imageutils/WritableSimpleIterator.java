/* 
 *  Copyright (c) 2011, Michael Bedward. All rights reserved. 
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

package org.jaitools.imageutils;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;

import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;
import javax.media.jai.iterator.WritableRectIter;

/**
 * A read-write image iterator which moves by column then row (pixel then line).
 *
 * @author michael
 */
public class WritableSimpleIterator extends AbstractSimpleIterator {
    
    /**
     * Provides a method to create the delegate iterator. Passing an instance of
     * this class to the super-class constructor allows the delegate to be a
     * final field in the super-class.
     */
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

    /**
     * Creates a new iterator. The bounds are allowed to extend beyond the bounds
     * of the target image. When the iterator is positioned outside the image the
     * specified outside value will be returned.
     * 
     * @param image the target image
     * @param bounds bounds for the iterator; if {@code null} the bounds of the target
     *     image will be used
     * @param outsideValue value to return when the iterator is positioned beyond
     *     the bounds of the target image; may be {@code null} 
     */
    public WritableSimpleIterator(WritableRenderedImage image, Rectangle bounds, Number outsideValue) {
        super(new Helper(), image, bounds, outsideValue, Order.IMAGE_X_Y);
    }

    /**
     * Creates a new iterator. The bounds are allowed to extend beyond the bounds
     * of the target image. When the iterator is positioned outside the image the
     * specified outside value will be returned.
     * 
     * @param image the target image
     * @param bounds bounds for the iterator; if {@code null} the bounds of the target
     *     image will be used
     * @param outsideValue value to return when the iterator is positioned beyond
     *     the bounds of the target image; may be {@code null} 
     * @param order processing order for this iterator when using the {@link #next()}
     *     method
     */
    public WritableSimpleIterator(WritableRenderedImage image, Rectangle bounds, Number outsideValue,
            Order order) {
        super(new Helper(), image, bounds, outsideValue, order);
    }

    /**
     * Sets the value in the first band of the image at the current position.
     * If the iterator is positioned outside the image bounds, no change is made
     * and this method returns {@code false}.
     * 
     * @param value 
     * @return {@code true} if the image value was set; {@code false} if the 
     *     iterator was positioned outside the bounds of the image
     */
    public boolean setSample(Number value) {
        return setSample(0, value);
    }

    /**
     * Sets the value in the first band of the image at the specified position.
     * If the position lies outside the image bounds, no change is made
     * and this method returns {@code false}.
     * 
     * @param pos the image position
     * @param value the new value
     * @return {@code true} if the image value was set; {@code false} if the 
     *     specified position was outside the bounds of the image
     * 
     * @throws IllegalArgumentException if either argument is {@code null}
     */
    public boolean setSample(Point pos, Number value) {
        if (pos == null) {
            throw new IllegalArgumentException("pos must not be null");
        }

        return setSample(pos.x, pos.y, value);
    }

    /**
     * Sets the value in the first band of the image at the specified position.
     * If the position lies outside the image bounds, no change is made
     * and this method returns {@code false}.
     * 
     * @param x image X-ordinate
     * @param y image Y-ordinate
     * @param value the new value
     * @return {@code true} if the image value was set; {@code false} if the 
     *     specified position was outside the bounds of the image
     * 
     * @throws IllegalArgumentException if {@code value} is {@code null}
     */
    public boolean setSample(int x, int y, Number value) {
        return (setPos(x, y) && setSample(value));
    }


    /**
     * Sets the value in the specified band of the image at the current position.
     * If the iterator is positioned outside the image bounds, no change is made
     * and this method returns {@code false}.
     * 
     * @param band image band
     * @param value the new value
     * @return {@code true} if the image value was set; {@code false} if the 
     *     iterator was positioned outside the bounds of the image
     * 
     * @throws IllegalArgumentException if {@code band} is out of range for the 
     *     target image or if {@code value} is {@code null}
     */
    public boolean setSample(int band, Number value) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        
        if (isInsideDelegateBounds()) {
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

    /**
     * Sets the value in the specified band of the image at the specified position.
     * If the position lies outside the image bounds, no change is made
     * and this method returns {@code false}.
     * 
     * @param pos the image position
     * @param band the image band
     * @param value the new value
     * @return {@code true} if the image value was set; {@code false} if the 
     *     specified position was outside the bounds of the image
     * 
     * @throws IllegalArgumentException if either {@code pos} or {@code value}
     *     is {@code null}; or if {@code band} is out of range
     */
    public boolean setSample(Point pos, int band, Number value) {
        if (pos == null) {
            throw new IllegalArgumentException("pos must not be null");
        }

        return setSample(pos.x, pos.y, band, value);
    }

    /**
     * Sets the value in the specified band of the image at the specified position.
     * If the position lies outside the image bounds, no change is made
     * and this method returns {@code false}.
     * 
     * @param x image X-ordinate
     * @param y image Y-ordinate
     * @param band the image band
     * @param value the new value
     * @return {@code true} if the image value was set; {@code false} if the 
     *     specified position was outside the bounds of the image
     * 
     * @throws IllegalArgumentException if {@code value} is {@code null}; 
     *     or if {@code band} is out of range
     */
    public boolean setSample(int x, int y, int band, Number value) {
        return (setPos(x, y) && setSample(band, value));
    }

}
