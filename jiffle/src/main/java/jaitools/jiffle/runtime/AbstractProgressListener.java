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
    
    protected long updateInterval;
    protected Double updateProp;
    protected long taskSize;

    public AbstractProgressListener() {
        updateInterval = 1;
        taskSize = 0;
    }

    public void setUpdateInterval(long numPixels) {
        updateInterval = numPixels;
        updateProp = null;
    }

    public void setUpdateInterval(double propPixels) {
        updateProp = Math.min(Math.max(propPixels, 0.0), 1.0);
        init();
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

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
