/*
 * Copyright 2010 Michael Bedward
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

/** 
 * Constants for the type of coordinates to use to index pixels. Options are:
 * {@link #CORNER} for standard JAI indexing with integer coordinates or
 * {@link #CENTER} for center coordinates (corner ordinates + 0.5).
 * <p>
 * Used with classes that honour double precision pixel coordinates such
 * as {@link ROIGeometry}.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public enum PixelCoordType {

    /** Standard JAI indexing with integer coordinates. */
    CORNER,
    /** Center coordinates (corner ordinates + 0.5). */
    CENTER
}
