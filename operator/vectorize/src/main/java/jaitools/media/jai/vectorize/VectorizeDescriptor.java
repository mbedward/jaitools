/*
 * Copyright 2010-2011 Michael Bedward
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

package jaitools.media.jai.vectorize;

import java.awt.image.renderable.ParameterBlock;
import java.util.Collection;
import java.util.Collections;

import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ROI;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * Traces the boundaries of regions with uniform data and returns them as
 * vector polygons. The source image passes through to thedestination unchanged, 
 * similar to a JAI statistics operator, while the vectors are returned as
 * an image property.
 * <pre><code>
 * // Vectorize regions using default parameter settings
 * RenderedImage image = ...
 * ParameterBlockJAI pb = new ParameterBlockJAI("Vectorize");
 * pb.setSource("source0", image);
 * RenderedOp dest = JAI.create("Vectorize", pb);
 * 
 * // retrieve the vectors
 * Collection&lt;Polygon&gt; polys = (Collection&lt;Polygon&gt;) dest.getProperty(
 *         VectorizeDescriptor.VECTOR_PROPERTY_NAME);
 * </code></pre>
 * 
 * The vectors are JTS Polygon objects. Each polygon holds the value of its source image
 * region as a Double (regardless of the source image data type) as a <i>user data</>
 * attribute.
 * 
 * <pre><code>
 * // report source image region value and area (expressed as pixel units)
 * Collection&lt;Polygon&gt; polys = (Collection&lt;Polygon&gt;) dest.getProperty(
 *         VectorizeDescriptor.VECTOR_PROPERTY_NAME);
 * 
 * System.out.println("Region value  Perimeter       Area");
 * for (Polygon poly : polys) {
 *     Double value = (Double) poly.getUserData();
 *     double perimeter = poly.getLength();
 *     double area = poly.getArea();
 *     System.out.printf("%12.2f %10.2f %10.2f\n", value, perimeter, area);
 * }
 * </code></pre>
 * 
 * Optionally, polygons below a threshold area can be filtered from the output
 * by simple deletion, or by merging with a neighbour (where possible).
 * A neighbouring polygon is one that shares one or more boundary segments
 * with the target polygon (ie. lineal intersection). Two polygons that only
 * touch at a single vertex are not considered neighbours.
 * 
 * <pre><code>
 * ParameterBlockJAI pb = new ParameterBlockJAI("Vectorize");
 * pb.setSource("source0", myImage);
 * 
 * // Filter polygons with area up to 5 pixels by merging
 * // them with the largest neighbouring polygon. Where no neighbour
 * // exists (e.g. small region surrounded by NODATA) the polygon
 * // will be discarded.
 * pb.setParameter("filterThreshold", 5.1);
 * pb.setParameter("filterMethod", VectorizeDescriptor.FILTER_MERGE_LARGEST);
 * </code></pre>
 * 
 * 
 * The following parameters control the vectorizing process:
 * <table border="1" cellpadding="3">
 * <tr>
 * <th>Name</th>
 * <th>Class</th>
 * <th>Default</th>
 * <th>Description</th>
 * </tr>
 * 
 * <tr>
 * <td>roi</td>
 * <td>ROI</td>
 * <td>null</td>
 * <td>An optional ROI to define the vectorizing area.</td>
 * </tr>
 * 
 * <tr>
 * <td>band</td>
 * <td>Integer</td>
 * <td>0</td>
 * <td>The source image band to process.</td>
 * </tr>
 * 
 * <tr>
 * <td>outsideValues</td>
 * <td>Collection</td>
 * <td>empty</td>
 * <td>Values to treat as NODATA.</td>
 * </tr>
 * 
 * <tr>
 * <td>insideEdges</td>
 * <td>Boolean</td>
 * <td>Boolean.TRUE</td>
 * <td>
 * Whether to vectorize boundaries between data regions.
 * Setting this to false results in only the boundaries between NODATA
 * and data regions being returned.
 * </td>
 * </tr>
 * 
 * <tr>
 * <td>removeCollinear</td>
 * <td>Boolean</td>
 * <td>Boolean.TRUE</td>
 * <td>Whether to simplify polygons by removing collinear vertices.</td>
 * </tr>
 * 
 * <tr>
 * <td>filterThreshold</td>
 * <td>Double</td>
 * <td>0</td>
 * <td>
 * The area (pixel units) below which a polygon will be filtered from 
 * the output by merging or deletion. 
 * </td>
 * </tr>
 * 
 * <tr>
 * <td>filterMethod</td>
 * <td>Integer</td>
 * <td>{@link #FILTER_MERGE_LARGEST}</td>
 * <td>The method used to filter small polygons when filterThreshold &gt; 0.
 * Must be one of:<br>
 * {@link #FILTER_MERGE_LARGEST}<br>
 * {@link #FILTER_MERGE_RANDOM}<br>
 * {@link #FILTER_DELETE}<br></td>
 * </tr>
 * </table>
 * 
 * @see com.vividsolutions.jts.geom.Polygon
 * @see jaitools.media.jai.regionalizeRegionalizeDescriptor
 * @see jaitools.media.jai.rangelookup.RangeLookupDescriptor
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class VectorizeDescriptor extends OperationDescriptorImpl {
    
    /**
     * Name used to access the vectorized region boundaries as
     * a destination image property.
     */
    public static final String VECTOR_PROPERTY_NAME = "vectors";
    
    /**
     * Filter small polygons by merging each with its largest (area) neighbour.
     * This is the default.
     */
    public static final int FILTER_MERGE_LARGEST = 0;
    /**
     * Filter small polygons by merging each with a randomly chosen neighbour.
     */
    public static final int FILTER_MERGE_RANDOM = 1;
    /**
     * Filter small polygons by simple deletion.
     */
    public static final int FILTER_DELETE = 2;

    static final int ROI_ARG = 0;
    static final int BAND_ARG = 1;
    static final int OUTSIDE_VALUES_ARG = 2;
    static final int INSIDE_EDGES_ARG = 3;
    static final int REMOVE_COLLINEAR_ARG = 4;
    static final int FILTER_SMALL_POLYS_ARG = 5;
    static final int FILTER_METHOD_ARG = 6;

    private static final String[] paramNames = {
        "roi",
        "band",
        "outsideValues",
        "insideEdges",
        "removeCollinear",
        "filterThreshold",
        "filterMethod"
    };

    private static final Class[] paramClasses = {
         javax.media.jai.ROI.class,
         Integer.class,
         Collection.class,
         Boolean.class,
         Boolean.class,
         Double.class,
         Integer.class
    };

    private static final Object[] paramDefaults = {
         (ROI) null,
         Integer.valueOf(0),
         Collections.EMPTY_LIST,
         Boolean.TRUE,
         Boolean.TRUE,
         Double.valueOf(0.0),
         FILTER_MERGE_LARGEST
    };

    /** Constructor. */
    public VectorizeDescriptor() {
        super(new String[][]{
                    {"GlobalName", "Vectorize"},
                    {"LocalName", "Vectorize"},
                    {"Vendor", "jaitools.media.jai"},
                    {"Description", "Vecotirze boundaries of regions of uniform value"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "1.1.0"},
                    
                    {"arg0Desc", paramNames[0] + " an optional ROI"},
                    
                    {"arg1Desc", paramNames[1] + " (Integer, default=0) " +
                              "the source image band to process"},
                    
                    {"arg2Desc", paramNames[2] + " (Collection, default=null) " +
                              "optional set of values to treat as outside"},
                    
                    {"arg3Desc", paramNames[3] + " (Boolean, default=true) " +
                              "whether to vectorize boundaries between adjacent" +
                              "regions with non-outside values"},
                    {"arg4Desc", paramNames[4] + " (Boolean, default=false) " +
                              "whether to reduce collinear points in the resulting polygons"},
                    {"arg5Desc", paramNames[5] + " (Double, default=0) " +
                              "area (fractional pixels) below which polygons will be filtered"},
                    {"arg6Desc", paramNames[6] + " (Integer, default=FILTER_MERGE_LARGEST) " +
                              "filter method to use for polygons smaller than threshold area"}
                },
                new String[]{RenderedRegistryMode.MODE_NAME},   // supported modes
                
                1,                                              // number of sources
                
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
            int filterMethod = pb.getIntParameter(FILTER_METHOD_ARG);
            if ( !(filterMethod == FILTER_MERGE_LARGEST ||
                   filterMethod == FILTER_MERGE_RANDOM ||
                   filterMethod == FILTER_DELETE) ) {
                ok = false;
                msg.append("Invalid filter method: ").append(filterMethod);
            }
        }
        return ok;
    }

}

