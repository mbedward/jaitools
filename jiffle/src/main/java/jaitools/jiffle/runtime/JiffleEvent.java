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
 * Used by {@link JiffleExecutor} to send task completion and failure messages
 * listeners.
 * 
 * @see JiffleEventListener
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class JiffleEvent {
    /** Holds results and other objects passed by the {@link JiffleExecutor}. */
    protected final JiffleExecutorResult result;
    
    /**
     * Creates a new event.
     * 
     * @param result result object
     */
    public JiffleEvent(JiffleExecutorResult result) {
        this.result = result;
    }
    
    /**
     * Gets the task result.
     * 
     * @return a result object
     */
    public JiffleExecutorResult getResult() {
        return result;
    }

}
