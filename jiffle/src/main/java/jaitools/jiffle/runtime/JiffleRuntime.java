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

package jaitools.jiffle.runtime;

/**
 * The root interface for Jiffle runtime classes.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public interface JiffleRuntime {

    /**
     * Set the bounds of the processing area. Usually this will be
     * the bounds of the destination image(s).
     *
     * @param minx min X pixel ordinate
     * @param miny min Y pixel ordinate
     * @param width width of processing area
     * @param height height of processing area
     */
    void setBounds(int minx, int miny, int width, int height);
    
    /**
     * Returns the value of a variable that was declared in the
     * script's init block.
     *
     * @param varName variable name
     *
     * @return the values or {@code null} if the variable name is
     *         not found
     */
    Double getVar(String varName);

}
