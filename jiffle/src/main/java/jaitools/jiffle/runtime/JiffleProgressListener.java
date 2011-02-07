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
 * An interface implemented by classes wishing to receive task progress information
 * from a {@link JiffleExecutor}.
 * <p>
 * Note: all that the executor does is pass the progress listener to a the
 * {@link JiffleRuntime} object that is doing the processing. It is the this object
 * that updates the listener (in its {@code evaluateAll} method.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public interface JiffleProgressListener {

    /**
     * Called by the client to request that the listener be notified
     * of task progress after each {@code numPixels] number of destination
     * pixels have been processed by the runtime object.
     *
     * @param numPixels number of pixels between listener updates
     */
    public void setUpdateInterval(long numPixels);

    /**
     * Called by the client to request that the listener be notified
     * of task progress after each {@code propPixels} proportion of the
     * destination pixels has been processed by the runtime object.
     *
     * @param propPixels proportion of pixels between listener updates
     */
    public void setUpdateInterval(double propPixels);

    /**
     * Called by the runtime object before processing begins to get
     * the update interval as number of destination image pixels.
     *
     * @return update interval as number of pixels
     */
    public long getUpdateInterval();

    /**
     * Called by the runtime object to inform the listener of the total
     * number of pixels in the largest destination image that will be
     * processed.
     * 
     * @param numPixels number of destination image pixels
     */
    public void setTaskSize(long numPixels);
    
    /**
     * Called by the runtime object when the task starts.
     */
    public void start();
    
    /**
     * Called by the runtime object at update intervals as specified by
     * either {@link #setUpdateInterval(long)} or {@link #setUpdateInterval(double)}.
     * <p>
     * It is important to keep the amount of processing done in this method
     * to a minimum to avoid slowing down processing too much.
     * 
     * @param done number of pixels processed
     */
    public void update(long done);

    /**
     * Called by the runtime object when the task finishes.
     */
    public void finish();

}
