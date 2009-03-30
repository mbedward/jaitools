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
 * Used by {@link JiffleInterpreter} to flag a problem in
 * the execution of a Jiffle script
 * 
 * @author Michael Bedward
 */
public class JiffleFailureEvent extends JiffleEvent {
    
    /**
     * Constructor
     * 
     * @param jobId an integer job ID issued by the controlling JiffleInterpreter
     * @param jiffle the Jiffle object that this event pertains to
     */
    public JiffleFailureEvent(int jobId, Jiffle jiffle) {
        super(jobId, jiffle);
    }

}
