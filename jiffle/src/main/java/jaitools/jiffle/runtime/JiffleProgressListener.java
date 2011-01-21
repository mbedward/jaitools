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
     * Sets the total task size (number of pixels to be processed).
     * 
     * @param n task size
     */
    public void setTaskSize(long size);
    
    /**
     * Called when the task starts.
     */
    public void start();
    
    /**
     * Called when each pixel is processed. If you don't want to slow down
     * your tasks, make sure that implementing classes keep this method
     * short and fast.
     * 
     * @param done number of pixels processed
     */
    public void update(long done);

    /**
     * Called when the task finishes.
     */
    public void finish();

}
