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
import java.awt.image.RenderedImage;

import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

/**
 * A read-only image iterator which moves by column then row (pixel then line).
 *
 * @author michael
 */
public class SimpleIterator extends AbstractSimpleIterator {

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
            if (bounds == null || bounds.isEmpty()) {
                return null;
            }
            return RectIterFactory.create(image, bounds);
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
    public SimpleIterator(RenderedImage image, Rectangle bounds, Number outsideValue) {
        super(new Helper(), image, bounds, outsideValue);
    }

}
