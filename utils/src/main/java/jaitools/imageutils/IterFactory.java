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
import java.awt.image.WritableRenderedImage;

/**
 * Factory to create random iterators.
 * <p>
 * Note: this only has trivial methods at the moment but we might experiment
 * with templated iterators etc.
 * 
 * @author Michael Bedward
 * @since 1.2
 * @version $Id$
 */
public class IterFactory {

    public static BoundedRandomIter createBoundedRandomIter(RenderedImage image, Rectangle bounds) {
        return new BoundedRandomIter(image, bounds);
    }
    
    public static WritableBoundedRandomIter createWritableBoundedRandomIter(WritableRenderedImage image, Rectangle bounds) {
        return new WritableBoundedRandomIter(image, bounds);
    }
    
}
