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

import java.awt.RenderingHints;
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
 * based on values in a data image.
 * <p>
 * Optionally, an ROI can be provided to define which pixels in the data image
 * will contribute to the zonal statistics.
 *
 * @author Michael Bedward
 */
public class ZonalStatsDescriptor extends OperationDescriptorImpl {

    static final int DATA_SOURCE_INDEX = 0;
    static final int ZONE_SOURCE_INDEX = 1;
    
    private static final String[] srcImageNames =
        {"dataImage",
         "zoneImage"
        };

    private static final Class[][] srcImageClasses =
    {
        {RenderedImage.class, RenderedImage.class}
    };

    static final int STATS_ARG_INDEX = 0;
    static final int BAND_ARG_INDEX = 1;
    static final int ROI_ARG_INDEX = 2;
    static final int NAN_ARG_INDEX = 3;
    static final int NO_RESULT_VALUE_ARG_INDEX = 4;

    private static final String[] paramNames =
        {"stats",
         "band",
         "roi",
         "ignoreNaN",
         "nilValue"
        };

    private static final Class[] paramClasses =
        {ZonalStatistic[].class,
         Integer.class,
         javax.media.jai.ROI.class,
         Boolean.class,
         Number.class
        };

    private static final Object[] paramDefaults =
        {NO_PARAMETER_DEFAULT,
         Integer.valueOf(0),
         (ROI) null,
         Boolean.TRUE,
         Integer.valueOf(0)};

    /** Constructor. */
    public ZonalStatsDescriptor() {
        super(new String[][]{
                    {"GlobalName", "KernelStats"},
                    {"LocalName", "KernelStats"},
                    {"Vendor", "jaitools.media.jai"},
                    {"Description", "Calculate neighbourhood statistics"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "1.0-SHAPSHOT"},
                    {"arg0Desc", "stats - an array of KernelStatistic constants specifying the " +
                             "statistics required"},
                    {"arg1Desc", "band (default 0) - the band of the data image to process"},
                    {"arg2Desc", "roi (default null) - an optional ROI for masking the data image"},
                    {"arg3Desc", "ignoreNaN (Boolean, default TRUE) - " +
                             "if TRUE, NaN values in source float or double images" +
                             "are ignored; if FALSE any NaN values in a pixel's zone" +
                             "will result in nilValue for the destination pixel"},
                    {"arg4Desc", "nilValue (Number, default 0) - the nil value for destination" +
                             "pixels have no zonal result due to source masking or NaN values in" +
                             "their zone (if ignoreNaN == FALSE)"}
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
     * @param ignoreNaN if TRUE, NaN values in input float or double images
     * are ignored in calculations
     * @param nilValue value to write to destination when there is no calculated
     * statistic for a pixel
     * @param hints an optional RenderingHints object
     * @return a RenderedImage with a band for each requested statistic
     */
    public static RenderedImage create(
            RenderedImage dataImage,
            RenderedImage zoneImage,
            ZonalStatistic[] stats,
            Integer band,
            ROI roi,
            Boolean ignoreNaN,
            Number nilValue,
            RenderingHints hints) {

        ParameterBlockJAI pb =
                new ParameterBlockJAI("ZonalStats",
                RenderedRegistryMode.MODE_NAME);

        pb.setSource("dataImage", dataImage);
        pb.setSource("zoneImage", zoneImage);
        pb.setParameter("stats", stats);
        pb.setParameter("band", band);
        pb.setParameter("roi", roi);
        pb.setParameter("ignoreNaN", ignoreNaN);
        pb.setParameter("nilValue", nilValue);

        return JAI.create("ZonalStats", pb, hints);
    }

    @Override
    public boolean validateArguments(String modeName, ParameterBlock pb, StringBuffer msg) {
        if (!super.validateArguments(modeName, pb, msg)) {
            return false;
        }

        int band = pb.getIntParameter(BAND_ARG_INDEX);
        if (band < 0 || band >= pb.getNumSources()) {
            msg.append("band arg out of bounds for source image: " + band);
            return false;
        }

        return true;
    }

}

