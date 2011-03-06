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

import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.JiffleException;
import java.util.Map;

/**
 * Mock object for unit tests.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class MockJiffle extends Jiffle {
    
    private final int imageWidth;
    private final long pixelTime;

    /**
     * Creates a new instance. The mock runtime object that can be
     * retrieved from this will inherit the image size and pixel
     * processing time values.
     * 
     * @param imageSize image size (number of pixels)
     * @param pixelTime pixel processing time (mS)
     * 
     * @see MockJiffleRuntime
     */
    public MockJiffle(int imageSize, long pixelTime) {
        this.imageWidth = imageSize;
        this.pixelTime = pixelTime;
    }
    
    /**
     * {@inheritDoc}
     * @return always returns {@code true} for the mock object
     */
    @Override
    public boolean isCompiled() {
        return true;
    }

    /**
     * {@inheritDoc}
     * @return always returns an empty map
     */
    @Override
    public Map<String, ImageRole> getImageParams() {
        Map<String, ImageRole> emptyParams = CollectionFactory.map();
        return emptyParams;
    }

    /**
     * Returns a new instance of {@link MockJiffleRuntime} initialized with
     * the image size and pixel processing time values passed of this
     * object.
     * 
     * @return the mock runtime object
     */
    @Override
    public JiffleDirectRuntime getRuntimeInstance() throws JiffleException {
        return new MockJiffleRuntime(imageWidth, pixelTime);
    }
}
