/* 
 *  Copyright (c) 2009-2011, Michael Bedward. All rights reserved. 
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

package org.jaitools.media.jai.zonalstats;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.Collection;
import java.util.List;

import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ROI;
import javax.media.jai.registry.RenderedRegistryMode;

import org.jaitools.numeric.Range;
import org.jaitools.numeric.RangeComparator;
import org.jaitools.numeric.RangeUtils;
import org.jaitools.numeric.Statistic;


/**
 * Calculates a number of summary statistics, either for the whole data image or
 * within zones defined in a separate zone image. When used without a zone
 * image, all data image pixels are treated as belonging to a single zone 0.
 * Optionally, an ROI can be provided to constrain which areas of the data image
 * will be sampled for calculation of statistics.
 * <p>
 * Use of this operator is similar to the standard JAI statistics operators such as
 * {@link javax.media.jai.operator.HistogramDescriptor} where the source image is
 * simply passed through to the destination image and the results of the operation are
 * retrieved as a property. For this operator the property name can be reliably
 * referred to via the {@link #ZONAL_STATS_PROPERTY} constant.
 * <p>
 * The operator uses the {@link org.jaitools.numeric.StreamingSampleStats} class for its
 * calculations, allowing it to handle very large images for statistics other than
 * {@link org.jaitools.numeric.Statistic#MEDIAN}, for which the
 * {@link org.jaitools.numeric.Statistic#APPROX_MEDIAN} alternative is provided.
 * <p>
 * Note that the source names for this operator are "dataImage" and "zoneImage"
 * rather than the more typical JAI names "source0", "source1".
 * <p>
 * If a zone image is provided it must be of integral data type. By default, an
 * identity mapping is used between zone and data images, ie. zone image pixel at
 * <i>x, y</i> is mapped to the data image pixel at <i>x, y</i>. Any data image
 * pixels that do not have a corresponding zone image pixel are ignored, thus the
 * zone image bounds can be a subset of the data image bounds.
 * <p>
 * The user can also provide an {@link AffineTransform} to map data image
 * positions to zone image positions. For example, multiple data image pixels
 * could be mapped to a single zone image pixel.
 * <p>
 * The range of data image values that contribute to the analysis can be constrained
 * in two ways: with the "ranges" parameter and the "noDataRanges" parameter.
 * Each of these parameters take a {@code Collection} of {@link Range} objects.
 * <p>
 * The "ranges" parameter allows you to define values to include or exclude from 
 * the calculations, the choice being specified by the associated "rangesType" parameter.
 * If "rangesType" is {@code Range.Type#INCLUDE} you can also request that 
 * statistics be calculated separately for each range of values by setting the
 * "rangeLocalStats" parameter to {@code Boolean.TRUE}.
 * <p>
 * The "noDataRanges" parameter allows you to define values to treat as NODATA. 
 * As well as being excluded from calculations of statistics, the frequency of 
 * NODATA values is tracked by the operator and can be retrieved from the results.
 * <p>
 * Example of use...
 * <pre><code>
 * RenderedImage myData = ...
 * RenderedImage myZones = ...
 *
 * ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
 * pb.setSource("dataImage", myData);
 * pb.setSource("zoneImage", myZones);
 *
 * Statistic[] stats = {
 *     Statistic.MIN,
 *     Statistic.MAX,
 *     Statistic.MEAN,
 *     Statistic.APPROX_MEDIAN,
 *     Statistic.SDEV
 * };
 *
 * pb.setParameter("stats", stats);
 * RenderedOp op = JAI.create("ZonalStats", pb);
 *
 * ZonalStats stats = (ZonalStats) op.getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
 *
 * // print results to console
 * for (Result r : stats.results()) {
 *     System.out.println(r);
 * }
 * </code></pre>
 *
 * The {@code ZonalStats} object returned by the {@code getProperty} method above allows
 * you to examine results by image band, zone and statistic as shown here...
 * <pre><code>
 * ZonalStats stats = (ZonalStats) op.getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
 *
 * // print all results for band 0
 * for (Result r : stats.band(0).results()) {
 *     System.out.println(r);
 * }
 *
 * // print all result for band 2, zone 5
 * for (Result r : stats.band(2).zone(5).results()) {
 *     System.out.println(r);
 * }
 *
 * // print MEAN values for all zones in band 0
 * for (Result r : stats.band(0).statistics(Statistic.MEAN).results()) {
 *     System.out.println(r);
 * }
 * </code></pre>
 *
 * Using the operator to calculate statistics for an area within the data image...
 * <pre><code>
 * RenderedImage myData = ...
 * Rectangle areaBounds = ...
 * ROI roi = new ROIShape(areaBounds);
 *
 * ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
 * pb.setSource("dataImage", myData);
 * pb.setParameter("roi", roi);
 *
 * pb.setParameter("stats", someStats);
 * RenderedOp op = JAI.create("ZonalStats", pb);
 *
 * ZonalStats stats = (ZonalStats) op.getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
 *
 * // print results to console
 * for (Result r : stats.results()) {
 *     System.out.println(r);
 * }
 *
 * </code></pre>
 *
 * Using an {@code AffineTransform} to map data image position to zone image position...
 * <pre><code>
 * ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
 * pb.setSource("dataImage", myDataImage);
 * pb.setSource("zoneImage", myZoneImage);
 * pb.setParameter("stats", someStats);
 *
 * AffineTransform tr = new AffineTransform( ... );
 * pb.setParameter("zoneTransform", tr);
 * </code></pre>
 *
 * Asking for statistics on multiple bands.
 * <p>
 * By default the stats are calculated on a single default 0 index band. It
 * is possible also to request calculations on multiple bands, by passing the
 * band indexes as a parameter.
 * <pre><code>
 * ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
 * pb.setSource("dataImage", myDataImage);
 * pb.setSource("zoneImage", myZoneImage);
 * pb.setParameter("stats", someStats);
 *
 * // ask for stats on band 0 and 2 of the image
 * pb.setParameter("bands", new Integer[]{0, 2});
 * RenderedOp op = JAI.create("ZonalStats", pb);
 *
 * // get the results
 * ZonalStats> stats = (ZonalStats) op.getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
 *
 * // results by band
 * List<Result> resultsBand0 = stats.band(0).results;
 * List<Result> resultsBand2 = stats.band(2).results;
 * </code></pre>
 *
 * Excluding data values from the analysis with the "noDataRanges" parameter:
 * <pre><code>
 * ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
 * ...
 *
 * // exclude values between -5 and 5 inclusive
 * List&lt;Range&lt;Double>> excludeList = CollectionFactory.newList();
 * excludeList.add(Range.create(-5, true, 5, true));
 * pb.setParameter("noDataRanges", excludeList);
 *
 * // after we run the operator and get the results we can examine
 * // how many sample values were included in the calculation
 * List<Result> results = zonalStats.results();
 * for (Result r : results) {
 *     int numUsed = r.getNumAccepted();
 *     int numExcluded = r.getNumOffered() - numUsed;
 *     ...
 * }
 * </code></pre>
 *
 * <b>Parameters</b>
 * <table border="1">
 * <tr>
 * <th>Name</th><th>Type</th><th>Description</th><th>Default value</th>
 * </tr>
 * <tr>
 * <td>stats</td><td>Statistic[]</td><td>Statistics to calculate</td><td>NO DEFAULT</td>
 * </tr>
 * <tr>
 * <td>bands</td><td>Integer[]</td><td>Image bands to sample</td><td>{0}</td>
 * </tr>
 * <tr>
 * <td>roi</td><td>ROI</td><td>An optional ROI to constrain sampling</td><td>null</td>
 * </tr>
 * <tr>
 * <td>zoneTransform</td><td>AffineTransform</td>
 * <td>Maps data image positions to zone image positions</td>
 * <td>null (identity transform)</td>
 * </tr>
 * <tr>
 * <td>ranges</td><td>Collection&lt;Range></td><td>Ranges of values to include or exclude</td><td>null (include all data values)</td>
 * </tr>
 * <tr>
 * <td>rangesType</td><td>Range.Type</td>
 * <td>How to treat values supplied via the "ranges" parameter</td>
 * <td>Range.Type.INCLUDE</td>
 * </tr>
 * <tr>
 * <td>rangeLocalStats</td><td>Boolean</td>
 * <td>If Ranges are supplied via the "ranges" parameter, whether to calculate
 * statistics for each of them separately</td>
 * <td>Boolean.FALSE</td>
 * </tr>
 * <tr>
 * <td>noDataRanges</td><td>Collection&lt;Range></td>
 * <td>Ranges of values to treat specifically as NODATA
 * </td><td>null (no NODATA values defined)</td>
 * </tr>
 * </table>
 *
 * @see Result
 * @see Statistic
 * @see org.jaitools.numeric.StreamingSampleStats
 * @see ZonalStats
 *
 * @author Michael Bedward
 * @author Andrea Antonello
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 * @since 1.0
 * @version $Id$
 */
public class ZonalStatsDescriptor extends OperationDescriptorImpl {

    private static final long serialVersionUID = -526208282980300507L;

    /** Property name used to retrieve the results */
    public static final String ZONAL_STATS_PROPERTY = "ZonalStatsProperty";

    static final int DATA_IMAGE = 0;
    static final int ZONE_IMAGE = 1;

    private static final String[] srcImageNames = {"dataImage", "zoneImage"};

    private static final Class<?>[][] srcImageClasses = {{RenderedImage.class, RenderedImage.class}};

    static final int STATS_ARG = 0;
    static final int BAND_ARG = 1;
    static final int ROI_ARG = 2;
    static final int ZONE_TRANSFORM_ARG = 3;
    static final int RANGES_ARG = 4;
    static final int RANGES_TYPE_ARG = 5;
    static final int RANGE_LOCAL_STATS_ARG = 6;
    static final int NODATA_RANGES_ARG = 7;

    private static final String[] paramNames = {
        "stats", 
        "bands", 
        "roi", 
        "zoneTransform", 
        "ranges", 
        "rangesType", 
        "rangeLocalStats", 
        "noDataRanges"
    };

    private static final Class<?>[] paramClasses = {
        Statistic[].class, 
        Integer[].class,
        javax.media.jai.ROI.class, 
        AffineTransform.class, 
        Collection.class, 
        Range.Type.class, 
        Boolean.class, 
        Collection.class 
    };

    private static final Object[] paramDefaults = {
        NO_PARAMETER_DEFAULT,
        new Integer[]{Integer.valueOf(0)}, 
        (ROI) null, (AffineTransform) null, 
        (Collection) null, 
        Range.Type.UNDEFINED, 
        Boolean.FALSE, 
        (Collection) null
    };
    

    /** Constructor. */
    public ZonalStatsDescriptor() {
        super(new String[][]{
                {"GlobalName", "ZonalStats"},
                {"LocalName", "ZonalStats"},
                {"Vendor", "org.jaitools.media.jai"},
                {"Description", "Calculate neighbourhood statistics"},
                {"DocURL", "http://code.google.com/p/jaitools/"},
                {"Version", "1.2.0"},

                {
                        "arg0Desc",
                        String.format("%s - an array of Statistic constants specifying the "
                                + "statistics required", paramNames[STATS_ARG])},

                {
                        "arg1Desc",
                        String.format("%s (default %s) - the bands of the data image to process",
                                paramNames[BAND_ARG], paramDefaults[BAND_ARG])},

                {
                        "arg2Desc",
                        String.format("%s (default ) - an optional ROI for masking the data image",
                                paramNames[ROI_ARG], paramDefaults[ROI_ARG])},

                {
                        "arg3Desc",
                        String.format("%s (default %s) - an optional AffineTransform to "
                                + "map dataImage pixel coords to zoneImage pixel coords",
                                paramNames[ZONE_TRANSFORM_ARG], paramDefaults[ZONE_TRANSFORM_ARG])},

                {
                        "arg4Desc",
                        String.format("%s (default %s) - an optional Collection of Ranges "
                                + "that define dataImage values to include or exclude",
                                paramNames[RANGES_ARG], paramDefaults[RANGES_ARG])},

                {
                        "arg5Desc",
                        String.format("%s (default %s) - whether to include or exclude provided ranges",
                            paramNames[RANGES_TYPE_ARG], paramDefaults[RANGES_TYPE_ARG])},

                {
                        "arg6Desc",
                        String.format("%s (default %s) - whether to calculate statistics separately "
                                + "for ranges (when provided)",
                            paramNames[RANGE_LOCAL_STATS_ARG], paramDefaults[RANGE_LOCAL_STATS_ARG])},
                {
                        "arg7Desc",
                        String.format("%s (default %s) - an optional Collection of Ranges "
                            + "defining values to treat as NODATA",
                            paramNames[NODATA_RANGES_ARG], paramDefaults[NODATA_RANGES_ARG])},

        },

        new String[]{RenderedRegistryMode.MODE_NAME}, // supported modes

                srcImageNames, srcImageClasses,

                paramNames, paramClasses, paramDefaults,

                null // valid values (none defined)
        );
    }

    /**
     * Validates supplied parameters.
     * 
     * @param modeName the rendering mode
     * @param pb the parameter block
     * @param msg a {@code StringBuffer} to receive error messages
     * 
     * @return {@code true} if parameters are valid; {@code false} otherwise
     */
    @Override
    public boolean validateArguments( String modeName, ParameterBlock pb, StringBuffer msg ) {
        if (pb.getNumSources() == 0 || pb.getNumSources() > 2) {
            msg.append("ZonalStats operator takes 1 or 2 source images");
            return false;
        }

        // CHECKING RANGES
        Object rangeObject = pb.getObjectParameter(RANGES_ARG);
        if (rangeObject != null) {
            boolean ok = true;
            if (rangeObject instanceof Collection) {
                Collection coll = (Collection) rangeObject;
                if (!coll.isEmpty()) {
                    Object range = coll.iterator().next();
                    if (!(range instanceof Range)) {
                        msg.append(paramNames[RANGES_ARG]).append(" arg has to be of type List<Range<Double>>");
                        ok = false;

                    } else {
                        List sortedRanges = RangeUtils.sort(coll);
                        final int elements = sortedRanges.size();
                        if (elements > 1) {
                            RangeComparator rc = new RangeComparator();
                            List<Range> rr = (List<Range>) sortedRanges;
                            for (int i = 0; i < elements - 1; i++) {
                                Range r1 = rr.get(i);
                                Range r2 = rr.get(i + 1);
                                RangeComparator.Result result = rc.compare(r1, r2);
                                if (RangeComparator.isIntersection(result)) {
                                    ok = false;
                                    msg.append(paramNames[RANGES_ARG]).append(" arg can't contain intersecting ranges");
                                    break;
                                }
                            }
                        }
                    }
                }

            } else {
                if (rangeObject != null) {
                    ok = false;
                    msg.append(paramNames[RANGES_ARG]).append(" arg has to be of type List<Range<Double>>");
                }
            }
            if (!ok) {
                return false;
            }
        }

     // CHECKING NoData RANGES
        Object noDataRangeObject = pb.getObjectParameter(NODATA_RANGES_ARG);
        if (noDataRangeObject != null) {
            boolean ok = true;
            if (noDataRangeObject instanceof List) {
                Object range = ((List) noDataRangeObject).get(0);
                if (!(range instanceof Range)) {
                        msg.append(paramNames[NODATA_RANGES_ARG]).append(" arg has to be of type List<Range<Double>>");
                    ok = false;
                }
            } else {
                if (noDataRangeObject != null) {
                    ok = false;
                    msg.append(paramNames[NODATA_RANGES_ARG]).append(" arg has to be of type List<Range<Double>>");
                }
            }
            if (!ok) {
                return false;
            }
        }

        Object rangesType = pb.getObjectParameter(RANGES_TYPE_ARG);
        if (rangesType != null) {
            if (rangesType instanceof Range.Type) {
                Range.Type rt = (Range.Type) rangesType;
                if (rangeObject != null && rt == Range.Type.UNDEFINED) {
                    msg.append(paramNames[RANGES_TYPE_ARG]).append(" arg has to be of Type.EXCLUDED or Type.INCLUDED when specifying a Ranges List");
                    return false;
                }
            }
        }

        // CHECKING BANDS
        Object bandsObject = pb.getObjectParameter(BAND_ARG);
        Integer[] bands = null;
        if (!(bandsObject instanceof Integer[])) {
            msg.append(paramNames[BAND_ARG]).append(" arg has to be of type Integer[]");
            return false;
        } else {
            bands = (Integer[]) bandsObject;
        }

        // CHECKING DATA IMAGE
        RenderedImage dataImg = pb.getRenderedSource(DATA_IMAGE);
        for( Integer band : bands ) {
            if (band < 0 || band >= dataImg.getSampleModel().getNumBands()) {
                msg.append("band index out of bounds for source image: ").append(band);
                return false;
            }
        }

        Rectangle dataBounds = new Rectangle(
                dataImg.getMinX(), dataImg.getMinY(),
                dataImg.getWidth(), dataImg.getHeight());

        // CHECKING ROI
        Object roiObject = pb.getObjectParameter(ROI_ARG);
        if (roiObject != null) {
            if (!(roiObject instanceof ROI)) {
                msg.append("The supplied ROI is not a supported class");
                return false;
            }
            if (!((ROI)roiObject).intersects(dataBounds)) {
                msg.append("The supplied ROI does not intersect the source image");
                return false;
            }
        }

        // CHECKING ZONE IMAGE BOUNDS
        if (pb.getNumSources() == 2) {

            // get the zone image and check that it covers at least partially the data image
            RenderedImage zoneImg = pb.getRenderedSource(ZONE_IMAGE);
            int dataType = zoneImg.getSampleModel().getDataType();
            boolean integralType = false;
            if (dataType == DataBuffer.TYPE_BYTE || dataType == DataBuffer.TYPE_INT
                    || dataType == DataBuffer.TYPE_SHORT || dataType == DataBuffer.TYPE_USHORT) {
                integralType = true;
            }

            if (!integralType) {
                msg.append("The zone image must be an integral data type");
                return false;
            }

            AffineTransform tr = null;
            Object trObject = pb.getObjectParameter(ZONE_TRANSFORM_ARG);
            if (trObject != null) {
                if (!(trObject instanceof AffineTransform)) {
                    msg.append("The supplied transform should be an instance of AffineTransform");
                    return false;
                }
            }
            tr = (AffineTransform) trObject;

            final Rectangle zoneBounds = new Rectangle(zoneImg.getMinX(), zoneImg.getMinY(), zoneImg.getWidth(), zoneImg.getHeight());
            if (tr != null && !tr.isIdentity()) {
                // given that we are using an affine transform for this it might happen that we
                // are getting here is an elaborate shape that is badly approximated by its bbox
                final Shape zoneBoundsTransformed = tr.createTransformedShape(zoneBounds);
                if (!zoneBoundsTransformed.intersects(dataBounds)) {
                    msg.append("Zone image bounds are outside the data image bounds");
                    return false;
                }
            } else {
                // ok, in this case we can go with the simple bounds
                if (!dataBounds.intersects(zoneBounds)) {
                    msg.append("Zone image bounds are outside the data image bounds");
                    return false;
                }
            }

        }

        return true;
    }
}
