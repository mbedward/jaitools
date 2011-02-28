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

package jaitools.jiffle;

import java.util.List;

/**
 * Exception class for errors encountered while compiling a script
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class JiffleException extends Exception {

    /**
     * Create a new instance from an incoming exception.
     * 
     * @param thrwbl incoming exception
     */
    public JiffleException(Throwable thrwbl) {
        super(thrwbl);
    }

    /**
     * Creates a new instance with the given message.
     * 
     * @param msg error message
     */
    public JiffleException(String msg) {
        super(msg);
    }

    /**
     * Creates a new instance with the given list of messages.
     * This is used by Jiffle to take parsing error messages from 
     * {@link jaitools.jiffle.parser.ErrorReporter} objects.
     * 
     * @param errors list of error messages
     */
    public JiffleException(List<String> errors) {
        super(listToString(errors));
    }

    /**
     * Creates a new instance from the given message and base object.
     * This is used by Jiffle to wrap Janino exceptions.
     * 
     * @param string error message
     * @param thrwbl base {@code Throwable} object
     */
    public JiffleException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
    
    
    /**
     * Concatenates a list of messages, separating them with newline
     * characters.
     * 
     * @param msgList list of messages
     * 
     * @return the concatenated messages
     */
    private static String listToString(List<String> msgList) {
        StringBuilder sb = new StringBuilder();
        for (String msg : msgList) {
            sb.append(msg).append("\n");
        }
        return sb.toString();
    }

}
