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

import jaitools.CollectionFactory;
import java.util.Collections;
import java.util.List;

/**
 * Used by the Jiffle compiler to accumulate error messages emitted by a
 * parser.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL: https://jai-tools.googlecode.com/svn/trunk/jiffle/src/main/java/jaitools/jiffle/parser/ConstantLookup.java $
 * @version $Id: ConstantLookup.java 1299 2011-01-18 03:26:15Z michael.bedward $
 */
public class DeferredErrorReporter implements ErrorReporter {
    
    private List<String> errors = CollectionFactory.list();
    
    public DeferredErrorReporter() {}

    @Override
    public void addError(String errorText) {
        errors.add(errorText);
    }
    
    @Override
    public int getNumErrors() {
        return errors.size();
    }
    
    @Override
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    
    @Override
    public void clear() {
        errors.clear();
    }
}