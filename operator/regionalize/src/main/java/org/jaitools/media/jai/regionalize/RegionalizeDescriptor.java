/* 
 *  Copyright (c) 2009, Michael Bedward. All rights reserved. 
 *   
 *  Redistribution and use in source and binary forms, with or without modification, 
 *  are permitted provided that the following conditions are met: 
 *   
 *  - Redistributions of source code must retain the above copyright notice, this  
 *    list of conditions and the following disclaimer. 
 *   
 *  - Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.   
 *   
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */   

package org.jaitools.media.jai.regionalize;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * Describes the "Regionalize" operation.
 * <p>
 * This operation takes a single source image and identifies regions
 * of connected pixels with uniform value, where value comparisons
 * take into account a user-specified tolerance.
 * <p>
 * Note: At present, this operator only deals with a single band.
 * <p>
 * <b>Algorithm</b><p>
 * The operator scans the source image left to right, top to bottom. When
 * it reaches a pixel that has not been allocated to a region yet it uses
 * that pixel as the starting point for a flood-fill search (similar to
 * flood-filling in a paint program). The value of the starting pixel is
 * recorded as the reference value for the new region. The search works
 * its way outwards from the starting pixel, testing other pixels for
 * inclusion in the region. A pixel will be included if: <br>
 * <pre>      |value - reference value| <= tolerance </pre>
 * where tolerance is a user-specified parameter.
 * <p>
 * If the diagonal parameter is set to true, the flood-fill search will
 * include pixels that can only be reached via a diagonal step; if false,
 * only orthogonal steps are taken.
 * <p>
 * The search continues until no further pixels can be added to the region.
 * The region is then allocated a unique integer ID and summary statistics
 * (bounds, number of pixels, reference value) are recorded for it.
 * <p>
 * The output of the operation is an image of data type TYPE_INT, where each
 * pixel's value is its region ID. A {@linkplain RegionData} object can be
 * retrieved as a property of the output image using the property name
 * {@linkplain RegionalizeDescriptor#REGION_DATA_PROPERTY}).
 * <p>
 * <b>Example</b>
 * <pre><code>
 * RenderedImage myImg = ...
 *
 * ParameterBlockJAI pb = new ParameterBlockJAI("regionalize");
 * pb.setSource("source0", myImg);
 * pb.setParameter("band", 0);
 * pb.setParameter("tolerance", 0.1d);
 * pb.setParameter("diagonal", false);
 * RenderedOp regionImg = JAI.create("Regionalize", pb);
 *
 * // have a look at the image (this will force rendering and
 * // the calculation of region data)
 *
 * // print the summary data
 * RegionData regData =
 *    (RegionData)op.getProperty(RegionalizeDescriptor.REGION_DATA_PROPERTY);
 *
 * List&lt;Region> regions = regData.getData();
 * Iterator&lt;Region> iter = regions.iterator();
 * System.out.println("ID\tValue\tSize\tMin X\tMax X\tMin Y\tMax Y");
 * while (iter.hasNext()) {
 *     Region r = iter.next();
 *     System.out.println( String.format("%d\t%.2f\t%d\t%d\t%d\t%d\t%d",
 *         r.getId(),
 *         r.getRefValue(),
 *         r.getNumPixels(),
 *         r.getMinX(),
 *         r.getMaxX(),
 *         r.getMinY(),
 *         r.getMaxY() ));
 * </code></pre>
 *
 * <b>Parameters</b>
 * <table border="1">
 * <tr align="right">
 * <td>Name</td><td>Type</td><td>Default value</td>
 * </tr>
 * <tr align="right">
 * <td>band</td><td>int</td><td>0</td>
 * </tr>
 * <tr align="right">
 * <td>tolerance</td><td>double</td><td>0d</td>
 * </tr>
 * <tr align="right">
 * <td>diagonal</td><td>boolean</td><td>false</td>
 * </tr>
 * </table>
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class RegionalizeDescriptor extends OperationDescriptorImpl {

    /**
     * The propoerty name to retrieve the {@linkplain RegionData}
     * object which holds summary data for regions identified in
     * the source image and depicted in the destination image
     */
    public static final String REGION_DATA_PROPERTY = "regiondata";

    static final int BAND_ARG_INDEX = 0;
    static final int TOLERANCE_ARG_INDEX = 1;
    static final int DIAGONAL_ARG_INDEX = 2;

    private static final String[] paramNames =
        {"band", "tolerance", "diagonal"};

    private static final Class[] paramClasses =
        {Integer.class, Double.class, Boolean.class};

    private static final Object[] paramDefaults =
        {Integer.valueOf(0), Double.valueOf(0d), Boolean.TRUE};

    /** Constructor. */
    public RegionalizeDescriptor() {
        super(new String[][]{
                    {"GlobalName", "Regionalize"},
                    {"LocalName", "Regionalize"},
                    {"Vendor", "org.jaitools.media.jai"},
                    {"Description", "Identifies sufficiently uniform regions in a source image"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "1.0.0"},
                    {"arg0Desc", "band (int) - the band to regionalize"},
                    {"arg1Desc", "tolerance (double) - tolerance for pixel value comparison"},
                    {"arg2Desc", "diagonal (boolean) - true to include diagonal neighbours;" +
                             "false for only orthogonal neighbours"}
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
    public boolean arePropertiesSupported() {
        return true;
    }

    /**
     * Convenience method which constructs a {@link ParameterBlockJAI} and
     * invokes {@code JAI.create("regionalize", params) }.
     * If an ImageLayout object is included in the RenderingHints passed to
     * this method, any specification of the SampleModel for the destination
     * image will be overridden such that the destination will always be
     * TYPE_INT.
     *
     * @param source0 the image to be regionalized
     * @param band the band to process
     * @param tolerance tolerance for pixel value comparisons
     * @param diagonal true to include diagonal connections; false for only
     * orthogonal connections
     * @param hints rendering hints (may be null)
     * @return the RenderedOp destination
     */
    public static RenderedOp create(
            RenderedImage source0,
            int band,
            double tolerance,
            boolean diagonal,
            RenderingHints hints) {
        ParameterBlockJAI pb =
                new ParameterBlockJAI("Regionalize",
                RenderedRegistryMode.MODE_NAME);

        pb.setSource("source0", source0);
        pb.setParameter(paramNames[BAND_ARG_INDEX], band);
        pb.setParameter(paramNames[TOLERANCE_ARG_INDEX], tolerance);
        pb.setParameter(paramNames[DIAGONAL_ARG_INDEX], diagonal);

        return JAI.create("Regionalize", pb, hints);
    }
}

