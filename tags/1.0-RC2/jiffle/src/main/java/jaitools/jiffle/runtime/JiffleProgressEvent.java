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

package jaitools.jiffle.runtime;

import jaitools.jiffle.Jiffle;

/**
 * Event objects of this class are published by {@code JiffleInterpreter}
 * to indicate progress of the runnign script
 *
 * @see JiffleInterpreter
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class JiffleProgressEvent extends JiffleEvent {
    
    private float progress;

    /**
     * Constructor
     * 
     * @param jobId an integer job ID issued by the controlling JiffleInterpreter
     * @param jiffle the Jiffle object that the event pertains to
     * @param progress proportion of task completed (0 -> 1)
     */
    public JiffleProgressEvent(int jobId, Jiffle jiffle, float progress) {
        super(jobId, jiffle);
        this.progress = progress;
    }
    
    /**
     * Get the proportion of the run task completed as a value between 0 and 1
     */
    public float getProgress() {
        return progress;
    }
}