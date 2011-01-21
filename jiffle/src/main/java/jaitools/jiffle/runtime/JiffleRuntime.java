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
import java.awt.image.WritableRenderedImage;

/**
 * Defines the methods in Jiffle runtime classes.
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public interface JiffleRuntime {
    
    /**
     * Associates a name, as used in the Jiffle script, with a
     * destination image.
     * 
     * @param imageName image name as used in the Jiffle script
     * @param image writable image
     */
    void setDestinationImage(String imageName, WritableRenderedImage image);
    
    /**
     * Associates a name, as used in the Jiffle script, with a
     * source image.
     * 
     * @param imageName image name as used in the Jiffle script
     * @param image writable image
     */
    void setSourceImage(String imageName, RenderedImage image);
    
    /**
     * Evaluates the script for the given image location and writes
     * the result to the destination image(s).
     * 
     * @param x destination X ordinate
     * @param y destination Y ordinate
     */
    void evaluate(int x, int y);

    /**
     * Evaluates the script for all locations in the destination image(s).
     * 
     * @param pl an optional progress listener (may be {@code null}
     */
    void evaluateAll(JiffleProgressListener pl);
    
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
    
    /**
     * Writes a value to a destination image.
     * 
     * @param destImageName
     * @param x destination X ordinate
     * @param y destination Y ordinate
     * @param band destination band
     * @param value the value to write
     */
    void writeToImage(String destImageName, int x, int y, int band, double value);

}
