/*
 * Copyright 2010 Michael Bedward
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

package jaitools.media.jai.contour;

import jaitools.media.jai.AttributeOpImage;
import java.awt.image.RenderedImage;
import javax.media.jai.ROI;

/**
 * Generate smooth contours from a source image
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class ContourOpImage extends AttributeOpImage {

    public ContourOpImage(RenderedImage source, ROI roi) {
        super(source, roi);
    }

    @Override
    protected Object getAttribute(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String[] getAttributeNames() {
        return new String[] { ContourDescriptor.CONTOUR_PROPERTY_NAME };
    }

}
