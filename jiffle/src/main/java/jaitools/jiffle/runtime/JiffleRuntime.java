/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.runtime;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;

/**
 *
 * @author michael
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
     * Evaluates the script for the given image location and write
     * the result to the destination image(s).
     * 
     * @param x destination X ordinate
     * @param y destination Y ordinate
     * @param band destination band
     */
    void evaluate(int x, int y, int band);

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
