/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.runtime;

import java.awt.Rectangle;

/**
 * @author Michael Bedward
 */
public interface IJiffleRunner {

    /**
     * Run the jiffle script for the whole of the destination image(s).
     * 
     * @return true if the run completed successfully; false otherwise.
     */
    public boolean run() throws JiffleInterpreterException;

    /**
     * Run the jiffle script for the given destination image tile.
     *
     * @return true if the run completed successfully; false otherwise.
     */
    public boolean run(int tileX, int tileY) throws JiffleInterpreterException;

    /**
     * Run the jiffle script to compute the given area of the destination image.
     *
     * @return true if the run completed successfully; false otherwise.
     */
    public boolean run(Rectangle bounds) throws JiffleInterpreterException;
}
