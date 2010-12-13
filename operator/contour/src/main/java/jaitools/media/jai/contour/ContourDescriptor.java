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

package jaitools.media.jai.contour;

import java.awt.image.renderable.ParameterBlock;
import java.util.Collection;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ROI;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * Describes the "Contour" operation in which contour lines are interpolated
 * from values in a source image band. The desired contour values can be 
 * specified either by supplying a {@code Collection} of values via the
 * "levels" parameter, or a single value via the "interval" parameter. In the
 * interval case, the resulting contour values will be integer multiples of 
 * the interval. If both parameters are supplied the "levels" parameter takes
 * preference.
 * <p>
 * Contours are returned as a destination image property in the form of
 * a {@code Collection} of {@link com.vividsolutions.jts.geom.LineString} objects.
 * The source image value associated with each contour can be retrieved
 * with the {@link com.vividsolutions.jts.geom.LineString#getUserData()} method.
 * <p>
 * Source image pixels are passed through to the destination image unchanged.
 * <p>
 * Three boolean parameters control the form of the generated contours:
 * <ol type="1">
 * <li> 
 * <b>simplify</b>: if {@code true} (the default) colinear vertices are removed
 * to reduce contour memory requirements.
 * <li>
 * <b>mergeTiles</b>: if {@code true} (the default) contour lines are merged into
 * {@code LineStrings} across image tile boundaries; if {@code false} mergine
 * is skipped for faster processing.
 * </li>
 * <li>
 * <b>smooth</b>: if {@code true} contours are smoothed using Bezier interpolation
 * before being returned; if {@code false} (the default) no smoothing is done.
 * This option will probably have little effect on the form of contours unless
 * the source image resolution is coarse.
 * </li>
 * </ol>
 * Example of use:
 * <pre><code>
 * RenderedImage src = ...
 * ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
 * pb.setSource("source0", src);
 * 
 * // For contours at specific levels set the levels parameter
 * List&lt;Double&gt; levels = Arrays.asList(new double[]{1.0, 5.0, 10.0, 20.0, 50.0, 100.0});
 * pb.setParameter("levels", levels);
 * 
 * // Alternatively, set a constant interval between contours
 * pb.setParameter("interval", 10.0);
 * 
 * RenderedOp dest = JAI.create("Contour", pb);
 * Collection&lt;LineString&gt; contours = (Collection&lt;LineString&gt;) 
 *         dest.getProperty(ContourDescriptor.CONTOUR_PROPERTY_NAME);
 * 
 * for (LineString contour : contours) {
 *   // get this contour's value
 *   Double contourValue = (Double) contour.getUserData();
 *   ...
 * }
 * </code></pre>
 * 
 * The interpolation algorithm used is that of Paul Bourke: originally published
 * in Byte magazine (1987) as the CONREC contouring subroutine written in
 * FORTRAN. The implementation here was adapted from Paul Bourke's C code for the
 * algorithm available at: 
 * <a href="http://local.wasp.uwa.edu.au/~pbourke/papers/conrec/">
 * http://local.wasp.uwa.edu.au/~pbourke/papers/conrec/</a>
 * 
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class ContourDescriptor extends OperationDescriptorImpl {
    public final static String CONTOUR_PROPERTY_NAME = "contours";
    
    static final int ROI_ARG = 0;
    static final int BAND_ARG = 1;
    static final int LEVELS_ARG = 2;
    static final int INTERVAL_ARG = 3;
    static final int SIMPLIFY_ARG = 4;
    static final int MERGE_TILES_ARG = 5;
    static final int SMOOTH_ARG = 6;

    private static final String[] paramNames = {
        "roi",
        "band",
        "levels",
        "interval",
        "simplify",
        "mergeTiles",
        "smooth"
    };

    private static final Class[] paramClasses = {
         javax.media.jai.ROI.class,
         Integer.class,
         Collection.class,
         Number.class,
         Boolean.class,
         Boolean.class,
         Boolean.class
    };

    private static final Object[] paramDefaults = {
         (ROI) null,
         Integer.valueOf(0),
         (Collection) null,
         (Number) null,
         Boolean.TRUE,
         Boolean.TRUE,
         Boolean.FALSE,
    };

    

    public ContourDescriptor() {
        super(new String[][]{
                    {"GlobalName", "Contour"},
                    {"LocalName", "Contour"},
                    {"Vendor", "jaitools.media.jai"},
                    {"Description", "Traces contours based on source image values"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "1.1.0"},
                    
                    {"arg0Desc", paramNames[0] + " an optional ROI"},
                    
                    {"arg1Desc", paramNames[1] + " (Integer, default=0) " +
                              "the source image band to process"},
                    
                    {"arg2Desc", paramNames[2] + " (Collection<? extends Number>) " +
                              "values for which to generate contours"},
                    
                    {"arg2Desc", paramNames[3] + " (Number) " +
                              "interval between contour values (ignored if levels arg is supplied)"},
                    
                    {"arg3Desc", paramNames[4] + " (Boolean, default=true) " +
                              "whether to merge contour lines across source image tile boundaries"},
                    
                    {"arg4Desc", paramNames[5] + " (Boolean, default=true) " +
                              "whether to simplify contour lines by removing colinear vertices"},
                    
                    {"arg5Desc", paramNames[6] + " (Boolean, default=false) " +
                              "whether to smooth contour lines with Bezier interpolation"}
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
            Collection levels = (Collection) pb.getObjectParameter(LEVELS_ARG);
            Object objInterval = pb.getObjectParameter(INTERVAL_ARG);
            
            if (levels == null || levels.isEmpty()) {
                // No levels: check for a valid contour interval
                if (objInterval == null) {
                    ok = false;
                    msg.append("One of levels or interval parameters must be supplied");

                } else {
                    Double interval = ((Number) objInterval).doubleValue();
                    if (interval.isNaN() || interval.isInfinite()) {
                        ok = false;
                        msg.append("interval parameter must not be NaN or infinite");
                    }
                }
            } else {
                // Levels parameter supplied
                if (levels.isEmpty()) {
                    ok = false;
                    msg.append("levels parameter must be a Collection of one or more numbers");
                }
            }
        }
        
        return ok;
    }    
}
