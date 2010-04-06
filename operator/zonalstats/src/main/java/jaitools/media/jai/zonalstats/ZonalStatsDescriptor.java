/*
 * Copyright 2009-2010 Michael Bedward
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

package jaitools.media.jai.zonalstats;

import jaitools.numeric.Range;
import jaitools.numeric.RangeComparator;
import jaitools.numeric.RangeUtils;
import jaitools.numeric.Statistic;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.List;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ROI;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * Calculates a range of summary statistics, optionally for zones defined in a zone
 * image, for values in a data image. The operator can be used without a zone
 * image, in which case it will treat all data image pixels as being in the same
 * zone (labeled zone 0).
 * <p>
 * Optionally, an ROI can be provided to define which pixels in the data image
 * will contribute to the zonal statistics.
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
 * The user can also provide an {@linkplain AffineTransform} to map data image
 * positions to zone image positions. For example, multiple data image pixels
 * could be mapped to a single zone image pixel.
 * <p>
 * The range of data image values that contribute to the analysis can be constrained
 * by supplying a List of {@linkplain Range} objects, each of which defines a range 
 * of data values to <b>exclude</b>.
 * <p>
 * Use of this operator is similar to the standard JAI statistics operators such as
 * {@linkplain javax.media.jai.operator.HistogramDescriptor} where the source image is
 * simply passed through to the destination image and the results of the operation are
 * retrieved as a property. For this operator the property name can be reliably
 * referred to via the {@linkplain #ZONAL_STATS_PROPERTY} constant.
 * <p>
 * The operator uses the {@linkplain jaitools.numeric.StreamingSampleStats} class for its
 * calculations, allowing it to handle very large images for statistics other than
 * {@linkplain jaitools.numeric.Statistic#MEDIAN}, for which the
 * {@linkplain jaitools.numeric.Statistic#APPROX_MEDIAN} alternative is provided.
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
 * Excluding data values from the analysis with the "exclude" parameter:
 * <pre><code>
 * ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats");
 * ...
 *
 * // exclude values between -5 and 5 inclusive
 * List&lt;Range&lt;Double>> excludeList = CollectionFactory.newList();
 * excludeList.add(Range.create(-5, true, 5, true));
 * pb.setParameter("exclude", excludeList);
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
 * <tr align="right">
 * <td>Name</td><td>Type</td><td>Default value</td>
 * </tr>
 * <tr align="right">
 * <td>stats</td><td>Statistic[]</td><td>NO DEFAULT</td>
 * </tr>
 * <tr align="right">
 * <td>bands</td><td>Integer[]</td><td>{0}</td>
 * </tr>
 * <tr align="right">
 * <td>roi</td><td>ROI</td><td>null</td>
 * </tr>
 * <tr align="right">
 * <td>zoneTransform</td><td>AffineTransform</td><td>null (identity transform)</td>
 * </tr>
 * <tr align="right">
 * <td>exclude</td><td>List&lt;Range></td><td>null (include all data values)</td>
 * </tr>
 * </table>
 *
 * @see Result
 * @see jaitools.numeric.Statistic
 * @see jaitools.numeric.StreamingSampleStats
 * @see ZonalStats
 *
 * @author Michael Bedward
 * @author Andrea Antonello
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class ZonalStatsDescriptor extends OperationDescriptorImpl {

    private static final long serialVersionUID = -526208282980300507L;

    /** Property name used to retrieve the results */
    public static String ZONAL_STATS_PROPERTY = "ZonalStatsProperty";

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
    static final int RANGE_LOCAL_STATS = 6;

    private static final String[] paramNames = {"stats", "bands", "roi", "zoneTransform", "ranges", "rangesType", "rangeLocalStats"};

    private static final Class<?>[] paramClasses = {Statistic[].class, Integer[].class,
            javax.media.jai.ROI.class, AffineTransform.class, List.class, Range.Type.class, Boolean.class };

    private static final Object[] paramDefaults = {NO_PARAMETER_DEFAULT,
            new Integer[]{Integer.valueOf(0)}, (ROI) null, (AffineTransform) null, (List) null, Range.Type.UNDEFINED, Boolean.FALSE};

    /** Constructor. */
    public ZonalStatsDescriptor() {
        super(new String[][]{
                {"GlobalName", "ZonalStats"},
                {"LocalName", "ZonalStats"},
                {"Vendor", "jaitools.media.jai"},
                {"Description", "Calculate neighbourhood statistics"},
                {"DocURL", "http://code.google.com/p/jai-tools/"},
                {"Version", "1.0.0"},

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
                        String.format("%s (default %s) - an optional List of Ranges "
                                + "that define dataImage values to exclude from calculations",
                                paramNames[RANGES_ARG], paramDefaults[RANGES_ARG])},
                                
                {
                        "arg5Desc",
                        String.format("%s (default %s) - in case of Ranges, specify if they "
                        		+ "are included or excluded in calculations",
                            paramNames[RANGES_TYPE_ARG], paramDefaults[RANGES_TYPE_ARG])},
                                
                {
                        "arg6Desc",
                        String.format("%s (default %s) - an optional range argument type "
                            + "that define whether to calculate global statistics or splitted by ranges",
                            paramNames[RANGE_LOCAL_STATS], paramDefaults[RANGE_LOCAL_STATS])}

        },

        new String[]{RenderedRegistryMode.MODE_NAME}, // supported modes

                srcImageNames, srcImageClasses,

                paramNames, paramClasses, paramDefaults,

                null // valid values (none defined)
        );
    }

    /**
     * Convenience method which constructs a {@link ParameterBlockJAI} and
     * invokes {@code JAI.create("ZonalStats", params) }
     *
     * @param dataImage the data image
     *
     * @param zoneImage the zone image which must be of integral data type
     *
     * @param stats an array specifying the statistics required
     *
     * @param bands the array of bands of the data image to process (default single band == 0)
     *
     * @param roi optional roi (default is {@code null}) used to mask data values
     *
     * @param zoneTransform (default is {@code null}) an AffineTransform used to convert
     *        dataImage pixel coords to zoneImage pixel coords
     *
     * @param exclude a List of Ranges defining dataImage values to exclude from the analysis
     *        (may be empty or {@code null})
     *
     * @param hints an optional RenderingHints object
     *
     * @return a RenderedImage with a band for each requested statistic
     */
    public static RenderedImage create( RenderedImage dataImage, RenderedImage zoneImage,
            Statistic[] stats, Integer[] bands, ROI roi, AffineTransform zoneTransform,
            List<Range<Double>> ranges, RenderingHints hints ) {

        return create(dataImage, zoneImage, stats, bands, roi, zoneTransform, ranges, Range.Type.EXCLUDE, false, hints);
    }
    
    public static RenderedImage create( RenderedImage dataImage, RenderedImage zoneImage,
            Statistic[] stats, Integer[] bands, ROI roi, AffineTransform zoneTransform,
            List<Range<Double>> ranges, Range.Type rangesType, boolean rangeLocalStats, RenderingHints hints ) {

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats", RenderedRegistryMode.MODE_NAME);

        pb.setSource(srcImageNames[DATA_IMAGE], dataImage);
        pb.setSource(srcImageNames[ZONE_IMAGE], zoneImage);
        pb.setParameter(paramNames[STATS_ARG], stats);
        pb.setParameter(paramNames[BAND_ARG], bands);
        pb.setParameter(paramNames[ROI_ARG], roi);
        pb.setParameter(paramNames[ZONE_TRANSFORM_ARG], zoneTransform);
        pb.setParameter(paramNames[RANGES_ARG], ranges);
        pb.setParameter(paramNames[RANGES_TYPE_ARG], rangesType);
        pb.setParameter(paramNames[RANGE_LOCAL_STATS], rangeLocalStats);

        return JAI.create("ZonalStats", pb, hints);
    }

    /**
     * Returns true to indicate that properties are supported
     */
    @Override
    public boolean arePropertiesSupported() {
        return true;
    }

    /**
     * Checks parameters for the following:
     * <ul>
     * <li> Number of sources is 1 or 2
     * <li> Data image bands are valid
     * <li> Zone image, if provided, is an integral data type
     * <li> Zone image, if provided, overlaps the data image, taking into
     *      account any {@code AffineTransform}
     * </ul>
     */
    @SuppressWarnings("unchecked")
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
            if (rangeObject instanceof List) {
                Object range = ((List) rangeObject).get(0);
                if (!(range instanceof Range)) {
                	msg.append(paramNames[RANGES_ARG] + " arg has to be of type List<Range<Double>>");
                    ok = false;
                } else {
                	List ranges = (List)rangeObject;
                	List sortedRanges = RangeUtils.sort(ranges);
                	final int elements = sortedRanges.size();
                	if (elements > 1){
                		RangeComparator rc = new RangeComparator();
                		List<Range> rr = (List<Range>) sortedRanges; 
                		for (int i = 0; i<elements - 1; i++){
                			Range r1 = rr.get(i);
                			Range r2 = rr.get(i+1);
                			RangeComparator.Result result = rc.compare(r1, r2);
                    	    	if (RangeComparator.isIntersection(result)) {
                    	    		ok = false;
                    	    		msg.append(paramNames[RANGES_ARG] + " arg can't contain intersecting ranges");
                    	    		break;
                    	    	}
                			
                		}
                	}
                }
                
            } else {
                if (rangeObject != null) {
                    ok = false;
                    msg.append(paramNames[RANGES_ARG] + " arg has to be of type List<Range<Double>>");
                }
            }
            if (!ok) {
                return false;
            }
        }
        
        Object rangesType = pb.getObjectParameter(RANGES_TYPE_ARG);
        if (rangesType != null){
        	if (rangesType instanceof Range.Type){
        		Range.Type rt = (Range.Type) rangesType;
        		if (rangeObject != null && rt == Range.Type.UNDEFINED){
        			msg.append(paramNames[RANGES_TYPE_ARG] + " arg has to be of Type.EXCLUDED or Type.INCLUDED when specifying a Ranges List");
                    return false;
        		}
        	}
        }
        
        // CHECKING BANDS
        Object bandsObject = pb.getObjectParameter(BAND_ARG);
        Integer[] bands = null;
        if (!(bandsObject instanceof Integer[])) {
            msg.append(paramNames[BAND_ARG] + " arg has to be of type Integer[]");
            return false;
        } else {
            bands = (Integer[]) bandsObject;
        }

        // CHECKING DATA IMAGE
        RenderedImage dataImg = pb.getRenderedSource(DATA_IMAGE);
        for( Integer band : bands ) {
            if (band < 0 || band >= dataImg.getSampleModel().getNumBands()) {
                msg.append("band index out of bounds for source image: " + band);
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
            }
            else
            {
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
