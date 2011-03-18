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

package jaitools.jiffle;

import jaitools.jiffle.runtime.JiffleRuntime;
import jaitools.jiffle.runtime.JiffleRuntimeException;

/**
 * A stub class used in unit tests.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class NullRuntime implements JiffleRuntime {

    public Double getVar(String varName) {
        throw new UnsupportedOperationException("Should not be called");
    }

    public void setVar(String varName, Double value) throws JiffleRuntimeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setBounds(int minx, int miny, int width, int height) {
        throw new UnsupportedOperationException("Should not be called");
    }

}
