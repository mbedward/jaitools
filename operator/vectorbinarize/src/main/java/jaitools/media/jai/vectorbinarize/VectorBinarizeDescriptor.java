/*
 * Copyright 2010 Michael Bedward
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

package jaitools.media.jai.vectorbinarize;

import com.vividsolutions.jts.geom.Polygonal;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import jaitools.imageutils.PixelCoordType;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * Describes the "VectorBinarize" operation which creates a binary image based
 * on pixel inclusion in a polygonal {@code Geometry} object. Pixels are tested
 * for inclusion using either their corner coordinates (as per standard JAI
 * pixel indexing) or center coordinates (adding 0.5 to each ordinate) depending
 * on the "coordtype" parameter.
 * <p>
 * Example of use:
 * <pre><code>
 * // Using a JTS polygon object as the reference geometry
 * Polygon triangle = WKTReader.read("POLYGON((100 100, 4900 4900, 4900 100, 100 100))"); 
 * 
 * ParameterBlockJAI pb = new ParameterBlockJAI("VectorBinarize");
 * pb.setParameter("minx", 0);
 * pb.setParameter("miny", 0);
 * pb.setParameter("width", 5000);
 * pb.setParameter("height", 5000);
 * pb.setParameter("geometry", triangle);
 * 
 * // specify that we want to use center coordinates of pixels
 * pb.setParameter("coordtype", VectorBinaryDescriptor.CoordType.CENTER);
 * 
 * RenderedOp dest = JAI.create("VectorBinarize", pb);
 * </code></pre>
 * The reference polygon must be one of the following JTS classes:
 * {@code Polygon}, {@code MultiPolygon} or {@code PreparedGeometry}.
 * 
 * @author Michael Bedward
 * @author Andrea Aime
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class VectorBinarizeDescriptor extends OperationDescriptorImpl {
    
    static final int MINX_ARG = 0;
    static final int MINY_ARG = 1;
    static final int WIDTH_ARG = 2;
    static final int HEIGHT_ARG = 3;
    static final int GEOM_ARG = 4;
    static final int COORD_TYPE_ARG = 5;

    private static final String[] paramNames = {
        "minx",
        "miny",
        "width",
        "height",
        "geometry",
        "coordtype"
    };
    
    private static final Class[] paramClasses = {
        Integer.class,
        Integer.class,
        Integer.class,
        Integer.class,
        Object.class,
        PixelCoordType.class
    };

    private static final Object[] paramDefaults = {
        Integer.valueOf(0),
        Integer.valueOf(0),
        NO_PARAMETER_DEFAULT,
        NO_PARAMETER_DEFAULT,
        NO_PARAMETER_DEFAULT,
        PixelCoordType.CORNER
    };

    

    public VectorBinarizeDescriptor() {
        super(new String[][]{
                    {"GlobalName", "VectorBinarize"},
                    {"LocalName", "VectorBinarize"},
                    {"Vendor", "jaitools.media.jai"},
                    {"Description", "Creates a binary image based on the inclusion of pixels within a polygonal Geometry"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "1.1.0"},
                    
                    {"arg0Desc", paramNames[0] + " (Integer, default = 0) min image X"},
                    
                    {"arg1Desc", paramNames[1] + " (Integer, default = 0) min image Y"},
                    
                    {"arg2Desc", paramNames[2] + " (Integer) image width"},
                    
                    {"arg3Desc", paramNames[3] + " (Integer) image height"},
                    
                    {"arg4Desc", paramNames[4] + " the reference Geometry: either a Polygon, " +
                              "a MultiPolygon or a polygonal PreparedGeometry"},
                    
                    {"arg5Desc", paramNames[5] + " (PixelCoordType, default = CENTER) " +
                              "the type of pixel coordinates to use"}
                },
                new String[]{RenderedRegistryMode.MODE_NAME},   // supported modes
                
                0,                                              // number of sources
                
                paramNames,
                paramClasses,
                paramDefaults,
                    
                null                                            // valid values (none defined)
                );    
    }

    @Override
    protected boolean validateParameters(String modeName, ParameterBlock pb, StringBuffer msg) {
        boolean ok = super.validateParameters(modeName, pb, msg);
        
        if (ok) {
            Object obj = pb.getObjectParameter(GEOM_ARG);
            if (!(obj instanceof Polygonal || obj instanceof PreparedGeometry)) {
                ok = false;
                msg.append("The reference geometry must be either Polygon, MultiPolygon, or a "
                        + "polygonal PreparedGeometry");
            }
        }
        
        return ok;
    }    
}
