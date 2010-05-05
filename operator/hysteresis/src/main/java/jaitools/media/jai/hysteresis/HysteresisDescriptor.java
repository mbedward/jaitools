/*
 * Copyright 2009 Michael Bedward
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
/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package jaitools.media.jai.hysteresis;

// JAI dependencies
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.registry.RenderedRegistryMode;


/**
 * The descriptor for the {@link Hysteresis} operation.
 *
 * @since 2.1
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/library/coverage/src/main/java/org/geotools/image/jai/HysteresisDescriptor.java $
 * @version $Id: HysteresisDescriptor.java 30643 2008-06-12 18:27:03Z acuster $
 * @author Lionel Flahaut (2ie Technologie, IRD)
 */
public class HysteresisDescriptor extends OperationDescriptorImpl {
    /**
     * The operation name, which is {@value}.
     */
    public static final String OPERATION_NAME = "Hysteresis";

    /**
     * Constructs the descriptor.
     */
    public HysteresisDescriptor() {
        super(new String[][]{{"GlobalName",  OPERATION_NAME},
                             {"LocalName",   OPERATION_NAME},
                             {"Vendor",      "org.geotools"},
                             {"Description", "Thresholding by hysteresis"},
                             {"DocURL",      "http://www.geotools.org/"}, // TODO: provides more accurate URL
                             {"Version",     "1.0"},
                             {"arg0Desc",    "The low threshold value, inclusive."},
                             {"arg1Desc",    "The high threshold value, inclusive."},
                             {"arg2Desc",    "The value to give to filtered pixel."}},
              new String[]   {RenderedRegistryMode.MODE_NAME}, 1,
              new String[]   {"low", "high", "padValue"}, // Argument names
              new Class []   {Double.class, Double.class, Double.class},    // Argument classes
              new Object[]   {NO_PARAMETER_DEFAULT, NO_PARAMETER_DEFAULT, 0.0},
              null // No restriction on valid parameter values.
       );
    }
}
