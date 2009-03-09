/*
 * Copyright 2009 Michael Bedward
 * 
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public 
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package jaitools.media.jai.kernelstats;

import java.awt.RenderingHints;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.CollectionImage;
import javax.media.jai.CollectionImageFactory;
import javax.media.jai.CollectionOp;

/**
 * The image factory for the {@link KernelStatsOpImage} operation.
 *
 * @author Michael Bedward
 */
public class KernelStatsCIF implements CollectionImageFactory {

    /** Constructor */
    public KernelStatsCIF() {
    }

    public CollectionImage create(ParameterBlock arg0, RenderingHints arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CollectionImage update(ParameterBlock arg0, RenderingHints arg1, ParameterBlock arg2, RenderingHints arg3, CollectionImage arg4, CollectionOp arg5) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

