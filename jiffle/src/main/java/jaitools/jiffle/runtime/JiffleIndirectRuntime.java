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

import java.awt.image.RenderedImage;

/**
 * Defines methods implemented by indirect evaluation runtime classes.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public interface JiffleIndirectRuntime extends JiffleRuntime {
    /**
     * Specifies the variable in the Jiffle script which refers
     * to the destination image.
     * 
     * @param imageName variable name as used in the Jiffle script
     */
    void setDestinationImage(String imageName);
    
    /**
     * Associates a variable name in the Jiffle script with a
     * source image.
     * 
     * @param imageName image name as used in the Jiffle script
     * @param image writable image
     */
    void setSourceImage(String imageName, RenderedImage image);
    
    /**
     * Evaluates the script for the given image location.
     * 
     * @param x destination X ordinate
     * @param y destination Y ordinate
     * 
     * @return the result
     */
    double evaluate(int x, int y);

    /**
     * Gets a value from a source image as a double.
     * 
     * @param srcImageName the source image
     * @param x source X ordinate
     * @param y source Y ordinate
     * @param band source band
     * @return image value
     */
    double readFromImage(String srcImageName, int x, int y, int band);
    
}
