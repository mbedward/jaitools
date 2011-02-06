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

import java.util.Collections;
import java.util.List;

import jaitools.CollectionFactory;

/**
 * Used by the Jiffle compiler to accumulate ANTLR parsing error messages.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class DeferredErrorReporter implements ParsingErrorReporter {
    
    private List<String> errors = CollectionFactory.list();
    
    public DeferredErrorReporter() {}

    public void addError(String errorText) {
        errors.add(errorText);
    }
    
    public int getNumErrors() {
        return errors.size();
    }
    
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    
    public void clear() {
        errors.clear();
    }
}