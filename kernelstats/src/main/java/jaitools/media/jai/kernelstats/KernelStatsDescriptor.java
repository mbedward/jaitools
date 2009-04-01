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

package jaitools.media.jai.kernelstats;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ROI;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * An {@code OperationDescriptor} for the "KernelStats" operation.
 * <p>
 * For each pixel in the input image, a range of summary statistics can
 * be calculated for the values in the pixel's neighbourhood, which is defined
 * using a KernelJAI object.
 * <p>
 * Two masking options are provided:
 * <ul type="1">
 * <li><b>source masking</b>, in which the ROI is used
 * to constrain which source image pixels contribute to the kernel calculation;
 * <li><b>destination masking</b> in which the ROI constrains the positioning of
 * the kernel such that destination image value will be 0 if a source
 * pixel is not contained in the ROI.
 * </ul>
 * The two options may be used together.
 *
 * @author Michael Bedward
 */
public class KernelStatsDescriptor extends OperationDescriptorImpl {

    static final int STATS_ARG_INDEX = 0;
    static final int KERNEL_ARG_INDEX = 1;
    static final int BAND_ARG_INDEX = 2;
    static final int ROI_ARG_INDEX = 3;
    static final int MASKSRC_ARG_INDEX = 4;
    static final int MASKDEST_ARG_INDEX = 5;
    static final int NAN_ARG_INDEX = 6;
    static final int NO_RESULT_VALUE_ARG_INDEX = 7;

    private static final String[] paramNames =
        {"stats",
         "kernel",
         "band",
         "roi",
         "maskSource",
         "maskDest",
         "ignoreNaN",
         "nilValue"
        };

    private static final Class[] paramClasses =
        {KernelStatistic[].class,
         javax.media.jai.KernelJAI.class,
         Integer.class,
         javax.media.jai.ROI.class,
         Boolean.class,
         Boolean.class,
         Boolean.class,
         Number.class
        };

    private static final Object[] paramDefaults =
        {NO_PARAMETER_DEFAULT,
         NO_PARAMETER_DEFAULT,
         Integer.valueOf(0),
         (ROI) null,
         Boolean.FALSE,
         Boolean.FALSE,
         Boolean.TRUE,
         Integer.valueOf(0)};

    /** Constructor. */
    public KernelStatsDescriptor() {
        super(new String[][]{
                    {"GlobalName", "KernelStats"},
                    {"LocalName", "KernelStats"},
                    {"Vendor", "jaitools.media.jai"},
                    {"Description", "Calculate neighbourhood statistics"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "1.0-SHAPSHOT"},
                    {"arg0Desc", "stats - an array of KernelStatistic constants specifying the " +
                             "statistics required"},
                    {"arg1Desc", "kernel - a JAI Kernel object"},
                    {"arg2Desc", "band (Integer, default 0) - the source image band to process"},
                    {"arg3Desc", "roi (default null) - an optional ROI object for source and/or" +
                             "destination masking"},
                    {"arg4Desc", "maskSource (Boolean, default TRUE) -" +
                             "if TRUE only the values of source pixels where" +
                             "roi.contains is true contribute to the statistic"},
                    {"arg5Desc", "maskdest (Boolean, default TRUE) - " +
                             "if TRUE calculation is only performed" +
                             "for pixels where roi.contains is true; when false" +
                             "the destination pixel is set to NaN"},
                    {"arg6Desc", "ignorenan (Boolean, default TRUE) - " +
                             "if TRUE, NaN values in source float or double images" +
                             "are ignored; if FALSE any NaN values in a pixel's neighbourhood" +
                             "will result in nilValue for the destination pixel"},
                    {"arg7Desc", "nilValue (Number, default 0) - the nil value for destination" +
                             "pixels that are outside the ROI (if destMask == TRUE), or that have" +
                             "no neighbourhood values as a result of source masking, or NaN values" +
                             "in the neighbourhood and ignoreNaN == FALSE"}
                },

                new String[]{RenderedRegistryMode.MODE_NAME},   // supported modes
                
                1,                                              // number of sources
                
                paramNames,
                paramClasses,
                paramDefaults,
                    
                null                                            // valid values (none defined)
                );
    }

    /**
     * Convenience method which constructs a {@link ParameterBlockJAI} and
     * invokes {@code JAI.create("kernelstats", params) }
     * @param source0 the image for which neighbourhood statistics are required
     * @param stats an array specifying the statistics required
     * @param kernel a kernel defining the neighbourhood
     * @param band the source image band to process (default 0)
     * @param roi optional roi (default is null) used for source and/or destination
     * masking
     * @param maskSource if TRUE only the values of source pixels where
     * {@code roi.contains} is true contribute to the calculation
     * @param maskDest if TRUE the statistic is only calculated for pixels where
     * {@code roi.contains} is true; when false the destination pixel is set
     * to NaN
     * @param ignoreNaN if TRUE, NaN values in input float or double images
     * are ignored in calculations
     * @param nilValue value to write to destination when there is no calculated
     * statistic for a pixel (e.g. due to destination masking or NaNs in neighbourhood)
     * @param hints useful for specifying a border extender; may be null
     * @return a RenderedImages a band for each requested statistic
     * @throws IllegalArgumentException if any args are null
     */
    public static RenderedImage create(
            RenderedImage source0,
            KernelStatistic[] stats,
            KernelJAI kernel,
            int band,
            ROI roi,
            Boolean maskSource,
            Boolean maskDest,
            Boolean ignoreNaN,
            Number nilValue,
            RenderingHints hints) {

        ParameterBlockJAI pb =
                new ParameterBlockJAI("KernelStats",
                RenderedRegistryMode.MODE_NAME);

        pb.setSource("source0", source0);
        pb.setParameter("stats", stats);
        pb.setParameter("kernel", kernel);
        pb.setParameter("band", band);
        pb.setParameter("roi", roi);
        pb.setParameter("maskSource", maskSource);
        pb.setParameter("maskDest", maskDest);
        pb.setParameter("ignoreNaN", ignoreNaN);
        pb.setParameter("nilValue", nilValue);

        return JAI.create("KernelStats", pb, hints);
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

