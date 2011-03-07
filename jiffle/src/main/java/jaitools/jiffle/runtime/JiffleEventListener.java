/*
 * Copyright 2009-2011 Michael Bedward
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
 * Interface implemented by classes wishing to receive task progress
 * information from {@link JiffleExecutor}.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public interface JiffleEventListener {
   
    /**
     * Called when the task has been completed successfully.
     * 
     * @param ev the event
     */
    public void onCompletionEvent(JiffleEvent ev);

    /**
     * Called when the task has failed.
     * 
     * @param ev the event
     */
    public void onFailureEvent(JiffleEvent ev);
    
}

