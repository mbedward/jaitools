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

package jaitools.media.jai.zonalstats;

import jaitools.numeric.Statistic;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

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
 * Map<Integer, ZonalStats> resultMap = (Map<Integer, ZonalStats>) op
 *               .getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
 * ZonalStats result = resultMap.get(0);
 *
 * // print results to console
 * for (Integer zone : results.getZones()) {
 *     System.out.println("Zone " + zone);
 *     Map<Statistic, Double> zoneResults = results.getZoneStats(zone);
 *     for (Entry<Statistic, Double> e : zoneResults.entrySet()) {
 *         System.out.println(String.format("%12s: %.4f", e.getKey(), e.getValue()));
 *     }
 * }
 *
 * </code></pre>
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
 * Map<Integer, ZonalStats> resultMap = (Map<Integer, ZonalStats>) op
 *               .getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
 * ZonalStats result = resultMap.get(0);
 *
 * // print results: since we didn't use a zone image all
 * // results will be labeled as zone 0
 * Map<Statistic, Double> results = results.getZoneStats(0);
 * for (Entry<Statistic, Double> e : results.entrySet()) {
 *     System.out.println(String.format("%12s: %.4f", e.getKey(), e.getValue()));
 * }
 *
 * </code></pre>
 * Using an {@code AffineTransform} to map data image position to zone image position...
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
 *
 * AffineTransform
 *
 * RenderedOp op = JAI.create("ZonalStats", pb);
 *
 * Map<Integer, ZonalStats> resultMap = (Map<Integer, ZonalStats>) op
 *               .getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
 * ZonalStats result = resultMap.get(0);
 *
 * // print results: since we didn't use a zone image all
 * // results will be labeled as zone 0
 * Map<Statistic, Double> results = results.getZoneStats(0);
 * for (Entry<Statistic, Double> e : results.entrySet()) {
 *     System.out.println(String.format("%12s: %.4f", e.getKey(), e.getValue()));
 * }
 *
 * </code></pre>
 * Asking for statistics on multiple bands. 
 * <p>
 * By default the stats are calculated on a single default 0 index band. It 
 * is possible also to request calculations on multiple bands, by passing the 
 * band indexes as a parameter.
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
 * // asking for stats on band 0 and 3 of the image 
 * pb.setParameter("bands", new Integer[]{0, 3});
 * RenderedOp op = JAI.create("ZonalStats", pb);
 * // get results back
 * Map<Integer, ZonalStats> result = (Map<Integer, ZonalStats>) op
 *               .getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
 * // results for band 0
 * ZonalStats zonalStats0 = result.get(0);
 * Map<Statistic, Double> stats0 = zonalStats0.getZoneStats(0);
 * // results for band 3
 * ZonalStats zonalStats3 = result.get(3);
 * Map<Statistic, Double> stats3 = zonalStats3.getZoneStats(0);
 * double minBand0 = stats0.get(Statistic.MIN).doubleValue();
 * double minBand3 = stats3.get(Statistic.MIN).doubleValue();
 *
 * </code></pre>
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
 * </table>
 *
 * @see jaitools.numeric.Statistic
 * @see jaitools.numeric.StreamingSampleStats
 *
 * @author Michael Bedward
 * @author Andrea Antonello
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
    static final int ZONE_TRANSFORM = 3;

    private static final String[] paramNames = {"stats", "bands", "roi", "zoneTransform"};

    private static final Class<?>[] paramClasses = {Statistic[].class, Integer[].class,
            javax.media.jai.ROI.class, AffineTransform.class,};

    private static final Object[] paramDefaults = {NO_PARAMETER_DEFAULT,
            new Integer[]{Integer.valueOf(0)}, (ROI) null, (AffineTransform) null,};

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
                                + "from dataImage pixel coords to zoneImage pixel coords",
                                paramNames[ZONE_TRANSFORM], paramDefaults[ZONE_TRANSFORM])}},

        new String[]{RenderedRegistryMode.MODE_NAME}, // supported modes

                srcImageNames, srcImageClasses,

                paramNames, paramClasses, paramDefaults,

                null // valid values (none defined)
        );
    }

    /**
     * Convenience method which constructs a {@link ParameterBlockJAI} and
     * invokes {@code JAI.create("ZonalStats", params) }
     * @param dataImage the data image
     * @param zoneImage the zone image which must be of integral data type
     * @param stats an array specifying the statistics required
     * @param bands the array of bands of the data image to process (default single band == 0)
     * @param roi optional roi (default is null) used to mask data values
     * @param zoneTransform (default is null) an AffineTransform used to convert
     * dataImage pixel coords to zoneImage pixel coords
     * @param hints an optional RenderingHints object
     * @return a RenderedImage with a band for each requested statistic
     */
    public static RenderedImage create( RenderedImage dataImage, RenderedImage zoneImage,
            Statistic[] stats, Integer[] bands, ROI roi, AffineTransform zoneTransform,
            RenderingHints hints ) {

        ParameterBlockJAI pb = new ParameterBlockJAI("ZonalStats", RenderedRegistryMode.MODE_NAME);

        pb.setSource("dataImage", dataImage);
        pb.setSource("zoneImage", zoneImage);
        pb.setSource("zoneTransform", zoneTransform);
        pb.setParameter("stats", stats);
        pb.setParameter("bands", bands);
        pb.setParameter("roi", roi);

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
    @Override
    public boolean validateArguments( String modeName, ParameterBlock pb, StringBuffer msg ) {
        if (pb.getNumSources() == 0 || pb.getNumSources() > 2) {
            msg.append("ZonalStats operator takes 1 or 2 source images");
            return false;
        }

        Object bandsObject = pb.getObjectParameter(BAND_ARG);
        Integer[] bands = null;
        if (!(bandsObject instanceof Integer[])) {
            msg.append("bands arg has to be of type Integer[]");
            return false;
        } else {
            bands = (Integer[]) bandsObject;
        }
        RenderedImage dataImg = pb.getRenderedSource(DATA_IMAGE);
        for( Integer band : bands ) {
            if (band < 0 || band >= dataImg.getSampleModel().getNumBands()) {
                msg.append("band arg out of bounds for source image: " + band);
                return false;
            }
        }

        if (pb.getNumSources() == 2) {
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

            Rectangle dataBounds = new Rectangle(dataImg.getMinX(), dataImg.getMinY(), dataImg
                    .getWidth(), dataImg.getHeight());

            Rectangle zoneBounds = new Rectangle(zoneImg.getMinX(), zoneImg.getMinY(), zoneImg
                    .getWidth(), zoneImg.getHeight());

            AffineTransform tr = (AffineTransform) pb.getObjectParameter(ZONE_TRANSFORM);
            if (tr != null && !tr.isIdentity()) {
                zoneBounds = tr.createTransformedShape(zoneBounds).getBounds();
            }
            Rectangle xRect = new Rectangle();
            Rectangle.intersect(dataBounds, zoneBounds, xRect);
            if (xRect.isEmpty()) {
                msg.append("Zone image bounds are outside the data image bounds");
                return false;
            }
        }

        return true;
    }

}
