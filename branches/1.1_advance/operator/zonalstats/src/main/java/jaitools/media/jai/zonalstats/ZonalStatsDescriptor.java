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
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ROI;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * An {@code OperationDescriptor} for the "ZonalStats" operation.
 * <p>
 * Calculates a range of summary statistics for zones, defined in a zone image,
 * based on values in a data image. The zone image must be of integral data
 * type and a zone must be defined for every data image pixel. By default, the
 * zone image is expected to have the same bounds as the data image. If this is not
 * the case an {@linkplain java.awt.geom.AffineTransform} can be provided to transform
 * from data image coordinates to zone image coordinates.
 * <p>
 * Use of this operator is similar to the standard JAI statistics operators such as
 * {@linkplain javax.media.jai.operator.HistogramDescriptor} where the source image is
 * simply passed through to the destination image and the results of the operation are
 * retrieved as a property. For this operator the property name can be reliably
 * referred to via the {@linkplain #ZONAL_STATS_PROPERTY} constant.
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
 *     Statistic.SDEV
 * };
 * 
 * pb.setParameter("stats", stats);
 * RenderedOp op = JAI.create("ZonalStats", pb);
 * 
 * ZonalStats results = (ZonalStats) op.getProperty(
 *     ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
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
 * This operator uses {@linkplain jaitools.numeric.StreamingSampleStats} for its
 * calculations, allowing it to handle very large images for statistics other than
 * {@linkplain jaitools.numeric.Statistic#MEDIAN}, for which the
 * {@linkplain jaitools.numeric.Statistic#APPROX_MEDIAN} alternative is provided.
 * <p>
 * Optionally, an ROI can be provided to define which pixels in the data image
 * will contribute to the zonal statistics.
 * <p>
 * Note that the source names for this operator are "dataImage" and "zoneImage"
 * rather than the more typical JAI names "source0", "source1".
 * <p>
 * <b>Parameters</b>
 * <table border="1">
 * <tr align="right">
 * <td>Name</td><td>Type</td><td>Default value</td>
 * </tr>
 * <tr align="right">
 * <td>stats</td><td>Statistic[]</td><td>NO DEFAULT</td>
 * </tr>
 * <tr align="right">
 * <td>band</td><td>Integer</td><td>0</td>
 * </tr>
 * <tr align="right">
 * <td>roi</td><td>ROI</td><td>null</td>
 * </tr>
 * <tr align="right">
 * <td>zoneTransform</td><td>AffineTransform</td><td>null (identity transform)</td>
 * </tr>
 * </table>
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class ZonalStatsDescriptor extends OperationDescriptorImpl {

    public static String ZONAL_STATS_PROPERTY = "ZonalStatsProperty";

    static final int DATA_IMAGE = 0;
    static final int ZONE_IMAGE = 1;
    
    private static final String[] srcImageNames =
        {"dataImage",
         "zoneImage"
        };

    private static final Class[][] srcImageClasses =
    {
        {RenderedImage.class, RenderedImage.class}
    };

    static final int STATS_ARG = 0;
    static final int BAND_ARG = 1;
    static final int ROI_ARG = 2;
    static final int ZONE_TRANSFORM = 3;

    private static final String[] paramNames =
        {"stats",
         "band",
         "roi",
         "zoneTransform"
        };

    private static final Class[] paramClasses =
        {Statistic[].class,
         Integer.class,
         javax.media.jai.ROI.class,
         AffineTransform.class,
        };

    private static final Object[] paramDefaults =
        {NO_PARAMETER_DEFAULT,
         Integer.valueOf(0),
         (ROI) null,
         (AffineTransform) null,
        };

    /** Constructor. */
    public ZonalStatsDescriptor() {
        super(new String[][]{
                    {"GlobalName", "ZonalStats"},
                    {"LocalName", "ZonalStats"},
                    {"Vendor", "jaitools.media.jai"},
                    {"Description", "Calculate neighbourhood statistics"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "1.0.0"},

                    {"arg0Desc", String.format(
                             "%s - an array of Statistic constants specifying the " +
                             "statistics required", 
                             paramNames[STATS_ARG])},

                    {"arg1Desc", String.format(
                             "%s (default %s) - the band of the data image to process",
                             paramNames[BAND_ARG], paramDefaults[BAND_ARG])},

                    {"arg2Desc", String.format(
                             "%s (default ) - an optional ROI for masking the data image",
                             paramNames[ROI_ARG], paramDefaults[ROI_ARG])},

                    {"arg3Desc", String.format(
                             "%s (default %s) - an optional AffineTransform to " +
                             "from dataImage pixel coords to zoneImage pixel coords",
                             paramNames[ZONE_TRANSFORM], paramDefaults[ZONE_TRANSFORM])}
                },

                new String[]{RenderedRegistryMode.MODE_NAME},   // supported modes

                srcImageNames,
                srcImageClasses,
                
                paramNames,
                paramClasses,
                paramDefaults,
                    
                null                                            // valid values (none defined)
                );
    }

    /**
     * Convenience method which constructs a {@link ParameterBlockJAI} and
     * invokes {@code JAI.create("ZonalStats", params) }
     * @param dataImage the data image
     * @param zoneImage the zone image which must be of integral data type
     * @param stats an array specifying the statistics required
     * @param band the band of the data image to process (default 0)
     * @param roi optional roi (default is null) used to mask data values
     * @param zoneTransform (default is null) an AffineTransform used to convert
     * dataImage pixel coords to zoneImage pixel coords
     * @param hints an optional RenderingHints object
     * @return a RenderedImage with a band for each requested statistic
     */
    public static RenderedImage create(
            RenderedImage dataImage,
            RenderedImage zoneImage,
            Statistic[] stats,
            Integer band,
            ROI roi,
            AffineTransform zoneTransform,
            RenderingHints hints) {

        ParameterBlockJAI pb =
                new ParameterBlockJAI("ZonalStats",
                RenderedRegistryMode.MODE_NAME);

        pb.setSource("dataImage", dataImage);
        pb.setSource("zoneImage", zoneImage);
        pb.setSource("zoneTransform", zoneTransform);
        pb.setParameter("stats", stats);
        pb.setParameter("band", band);
        pb.setParameter("roi", roi);

        return JAI.create("ZonalStats", pb, hints);
    }

    /**
     * Returns true to indicate that properties are supported
     * (the "ZonalStats" property)
     */
    @Override
    public boolean arePropertiesSupported() {
        return true;
    }

    /**
     * Validate the arguments set in the parameter block and return true
     * if all are OK; false otherwise
     */
    @Override
    public boolean validateArguments(String modeName, ParameterBlock pb, StringBuffer msg) {
        if (pb.getNumSources() == 0 || pb.getNumSources() > 2) {
            msg.append("ZonalStats operator takes 1 or 2 source images");
            return false;
        }

        int band = pb.getIntParameter(BAND_ARG);
        RenderedImage dataImg = pb.getRenderedSource(DATA_IMAGE);
        if (band < 0 || band >= dataImg.getSampleModel().getNumBands()) {
            msg.append("band arg out of bounds for source image: " + band);
            return false;
        }

        return true;
    }

}

