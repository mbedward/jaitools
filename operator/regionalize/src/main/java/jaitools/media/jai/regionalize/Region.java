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

import jaitools.imageutils.FillResult;
import java.awt.Rectangle;

/**
 * Holds summary data for a single region of uniform value identified in the image
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class Region {

    private int id;
    private int numPixels;
    private double refValue;
    Rectangle bounds;

    /**
     * Constructor is package-private.
     */
    Region(FillResult fill) {
        this.id = fill.getID();
        this.refValue = fill.getValue();
        this.bounds = fill.getBounds();
        this.numPixels = fill.getNumPixels();
    }

    /**
     * Get the unique integer ID of this region
     */
    public int getId() {
        return id;
    }

    /**
     * Get the bounding pixel coordinates of this region
     *
     * @return a copy of the region's bounding rectangle
     */
    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    /**
     * Get the number of pixels within this region
     */
    public int getNumPixels() {
        return numPixels;
    }

    /**
     * Get the reference value for this region as a double.
     * This is the value of the first pixel that was identified
     * within the region.
     */
    public double getRefValue() {
        return refValue;
    }

    @Override
    public String toString() {
        return String.format("Region(id=%d, ref value=%.4f, pixel count=%d)", id, refValue, numPixels);
    }
}
