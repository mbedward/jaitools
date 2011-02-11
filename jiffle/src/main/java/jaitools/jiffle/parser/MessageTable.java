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

package jaitools.jiffle.parser;

import java.util.List;
import java.util.Map;
import jaitools.CollectionFactory;
import java.util.Collections;

/**
 * Used by Jiffle parsers to record errors and warnings.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class MessageTable {

    private Map<String, List<Message>> errors = CollectionFactory.map();

    /**
     * Adds a message.
     * 
     * @param varName the variable that the message relates to
     * @param code the message code
     */
    public void add(String varName, Message code) {
        List<Message> codes = errors.get(varName);
        if (codes == null) {
            codes = CollectionFactory.list();
            errors.put(varName, codes);
        }
        codes.add(code);
    }
    
    /**
     * Checks if this table contains any error messages.
     * @return {@code true} if errors are present, {@code false} otherwise
     */
    public boolean hasErrors() {
        for (List<Message> codes : errors.values()) {
            for (Message code : codes) {
                if (code.isError()) return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if this table contains any warning messages.
     * @return {@code true} if warnings are present, {@code false} otherwise
     */
    public boolean hasWarnings() {
        for (List<Message> codes : errors.values()) {
            for (Message code : codes) {
                if (code.isWarning()) return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets all messages. The returned {@code Map} has variable names
     * as keys and {@code Lists} of messages as values.
     * 
     * @return all messages keyed by variable name
     */
    public Map<String, List<Message>> getMessages() {
        return Collections.unmodifiableMap(errors);
    }
}
