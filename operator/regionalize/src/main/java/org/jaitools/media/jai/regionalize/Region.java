/* 
 *  Copyright (c) 2009, Michael Bedward. All rights reserved. 
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

package org.jaitools.media.jai.regionalize;

import java.awt.Rectangle;

import org.jaitools.imageutils.FillResult;


/**
 * Holds summary data for a single region of uniform value identified in the image
 *
 * @author Michael Bedward
 * @since 1.0
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
