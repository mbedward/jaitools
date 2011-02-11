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
 * Exception thrown by {@link JiffleExecutor} to signal runtime errors.
 *
 * @see JiffleExecutor
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class JiffleExecutorException extends Exception {

    /**
     * Creates a new exception.
     * 
     * @param msg error message.
     */
    public JiffleExecutorException(String msg) {
        super(msg);
    }

    /**
     * Creates a new exception.
     * 
     * @param thrwbl base exception
     */
    public JiffleExecutorException(Throwable thrwbl) {
        super(thrwbl);
    }
    
}
