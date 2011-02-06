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
import java.util.ArrayList;
import java.util.List;

/**
 * The default abstract base class for runtime classes that implement
 * indirect evaluation.
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public abstract class AbstractIndirectRuntime extends AbstractJiffleRuntime implements JiffleIndirectRuntime {
    
    /* 
     * Note: not using generics here because they are not
     * supported by the Janino compiler.
     */
    List sourceImageNames = new ArrayList();
    String destImageName;

    /** Processing bounds min X ordinate */
    protected int _minx;

    /** Processing bounds min Y ordinate */
    protected int _miny;

    /** Processing bounds width */
    protected int _width;

    /** Processing bounds height */
    protected int _height;

    public void setDestinationImage(String imageName) {
        destImageName = imageName;
    }

    public void setSourceImage(String imageName, RenderedImage image) {
        sourceImageNames.add(imageName);
    }

    public void setBounds(int minx, int miny, int width, int height) {
        _minx = minx;
        _miny = miny;
        _width = width;
        _height = height;

        initImageScopeVars();
    }

    public double readFromImage(String srcImageName, int x, int y, int band) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Initializes image-scope variables. These are fields in the runtime class.
     * They are initialized in a separate method rather than the constructor
     * because they may depend on expressions involving values that are not
     * known until the processing area is set (e.g. Jiffle's width() function).
     */
    protected abstract void initImageScopeVars();

}
