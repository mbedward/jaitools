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
 * Mock object for unit tests.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
class MockJiffleRuntime extends AbstractDirectRuntime {

    private final long pixelTime;

    /**
     * Creates a new mock object. Its thread will sleep for {@code pixelTime}
     * milliseconds each time its {@link #evaluate(int, int)} method is 
     * called.
     * 
     * @param imageSize image size (number of pixels)
     * 
     * @param pixelTime time to spend pretending to process each pixel
     */
    public MockJiffleRuntime(int imageSize, long pixelTime) {
        this.pixelTime = pixelTime;
        
        // super class fields
        this._width = imageSize;
        this._height = 1;
        this._minx = 0;
        this._miny = 0;
    }


    /**
     * Does nothing.
     */
    @Override
    protected void initImageScopeVars() {
    }

    /**
     * Does nothing.
     */
    @Override
    protected void initOptionVars() {
    }

    /**
     * Pretends to process a pixel (very slowly).
     */
    public void evaluate(int x, int y) {
        try {
            Thread.sleep(pixelTime);

        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}