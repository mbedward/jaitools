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

/**
 * Convenience class that can be sub-classed when not all methods
 * declared in the JiffleEventListener interface are required
 * 
 * @author Michael Bedward
 */
public class JiffleEventAdapter implements JiffleEventListener {

    public void onCompletionEvent(JiffleCompletionEvent ev) {
    }

    public void onFailureEvent(JiffleFailureEvent ev) {
    }

    public void onProgressEvent(JiffleProgressEvent ev) {
    }

}
