/*
 * Copyright 2009 Michael Bedward
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

package jaitools.tiledimage;

/**
 * Exception thrown when a given pixel / band location is outside an
 * image's bounds
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class PixelOutsideImageException extends IndexOutOfBoundsException {

    /**
     * Constructor
     * @param x pixel x coordinate
     * @param y pixel y coordinate
     * @param b band index
     */
    PixelOutsideImageException(int x, int y, int b) {
        super(String.format("Pixel location x=%d y=%d band=%d is outside image bounds", x, y, b));
    }

}
