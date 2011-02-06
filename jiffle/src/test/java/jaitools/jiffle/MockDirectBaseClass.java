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

import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;

import jaitools.jiffle.runtime.AbstractJiffleRuntime;
import jaitools.jiffle.runtime.JiffleDirectRuntime;
import jaitools.jiffle.runtime.JiffleProgressListener;


/**
 * A mock base class for direct evaluation used for unit testing.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public abstract class MockDirectBaseClass 
        extends AbstractJiffleRuntime implements JiffleDirectRuntime {
    
    public void setDestinationImage(String imageName, WritableRenderedImage image) {
        throw new UnsupportedOperationException("Should not be called");
    }

    public void setSourceImage(String imageName, RenderedImage image) {
        throw new UnsupportedOperationException("Should not be called");
    }

    public void setBounds(int minx, int miny, int width, int height) {
        throw new UnsupportedOperationException("Should not be called");
    }

    public void evaluateAll(JiffleProgressListener ignored) {
        throw new UnsupportedOperationException("Should not be called");
    }

    public double readFromImage(String srcImageName, int x, int y, int band) {
        throw new UnsupportedOperationException("Should not be called");
    }

    public void writeToImage(String destImageName, int x, int y, int band, double value) {
        throw new UnsupportedOperationException("Should not be called");
    }

}
