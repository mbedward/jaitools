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

package jaitools.jiffle.runtime;

/**
 * A simple progress listener base. Extend this and provide your own {@code start()},
 * {@code update(long numPixelsDone)} and {@code finish()} methods.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public abstract class AbstractProgressListener implements JiffleProgressListener {
    
    /** The number of pixels processed between each call to the listener. */
    protected long updateInterval;

    /** The proportion of pixels processed between each call to the listener. */
    protected Double updateProp;

    /** The total number of pixels to process. */
    protected long taskSize;

    /** 
     * Creates a new instance with an update interval of 1.
     */
    public AbstractProgressListener() {
        updateInterval = 1;
        taskSize = 0;
    }

    /**
     * Sets the update interval.
     * 
     * @param numPixels number of pixels processed between each call to the listener
     */
    public void setUpdateInterval(long numPixels) {
        updateInterval = numPixels;
        updateProp = null;
    }

    /**
     * Sets the update interval expressed a proportion of the total number of
     * pixels.
     * 
     * @param propPixels proportion of pixels processed between each call to the listener
     */
    public void setUpdateInterval(double propPixels) {
        updateProp = Math.min(Math.max(propPixels, 0.0), 1.0);
        init();
    }

    /**
     * Gets the update interval.
     * 
     * @return interval as number of pixels
     */
    public long getUpdateInterval() {
        return updateInterval;
    }

    /**
     * Called by the runtime object at the start of processing to set this
     * listener's task size field.
     * 
     * @param numPixels task size as number of pixels to process
     */
    public void setTaskSize(long numPixels) {
        taskSize = numPixels;
        init();
    }

    private void init() {
        if (taskSize > 0) {
            if (updateProp != null) {
                long n = (long)(taskSize * updateProp);
                updateInterval = Math.max(n, 1);
            }
        }
    }

}
