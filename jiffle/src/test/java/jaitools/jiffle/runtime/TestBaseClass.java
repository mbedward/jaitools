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
 * Used by CustomBaseClassTest.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public abstract class TestBaseClass implements JiffleRuntime {
    
    protected int _band = 0;

    public void setDestinationImage(String imageName, WritableRenderedImage image) {
    }

    public void setSourceImage(String imageName, RenderedImage image) {
    }

    public void evaluateAll() {
    }

    public double readFromImage(String srcImageName, int x, int y, int band) {
        return 0d;
    }

    public void writeToImage(String destImageName, int x, int y, int band, double value) {
    }

}
