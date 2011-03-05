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

import jaitools.jiffle.Jiffle;
import jaitools.jiffle.JiffleException;

/**
 * Defines methods for classes that generate runtime Java source from
 * compiled Jiffle scripts.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public interface SourceGenerator {
    
    /**
     * Sets the runtime model.
     * 
     * @param model runtime model
     */
    void setRuntimeModel(Jiffle.RuntimeModel model);
    
    /**
     * Sets the runtime base class name.
     * 
     * @param baseClassName base class name
     */
    void setBaseClassName(String baseClassName);

    /**
     * Returns the source for the runtime class. The runtime model and base class
     * name must be set before calling this method.
     * 
     * @return source of the runtime class as a single String.
     * @throws JiffleException on errors creating source
     * @throws RuntimeException if the runtime model or base class name are not set
     */
    String getSource() throws JiffleException;

}
