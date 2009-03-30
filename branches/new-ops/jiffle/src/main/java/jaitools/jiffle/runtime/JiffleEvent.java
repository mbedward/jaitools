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
 * Base class for events issued by the {@link JiffleInterpreter}
 * 
 * @see JiffleEventListener
 * 
 * @author Michael Bedward
 */
public abstract class JiffleEvent {
    protected int jobId;
    protected Jiffle jiffle;
    
    /**
     * Constructor
     * 
     * @param jobId an integer job ID issued by the controlling JiffleInterpreter
     * @param jiffle the Jiffle object that the event pertains to
     */
    public JiffleEvent(int jobId, Jiffle jiffle) {
        this.jobId = jobId;
        this.jiffle = jiffle;
    }
    
    /**
     * Get the job id for this event
     */
    public int getJobId() {
        return jobId;
    }
    
    /**
     * Get the Jiffle object that this event pertains to
     */
    public Jiffle getJiffle() {
        return jiffle;
    }
}
