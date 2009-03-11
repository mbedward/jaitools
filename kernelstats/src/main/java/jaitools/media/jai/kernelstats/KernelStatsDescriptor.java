/*
 * Copyright 2009 Michael Bedward
 * 
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public 
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package jaitools.media.jai.kernelstats;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.util.Collection;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ROI;
import javax.media.jai.registry.CollectionRegistryMode;

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

    static final int KERNEL_ARG_INDEX = 0;
    static final int STATS_ARG_INDEX = 1;
    static final int ROI_ARG_INDEX = 2;
    static final int MASKSRC_ARG_INDEX = 3;
    static final int MASKDEST_ARG_INDEX = 4;
    static final int NAN_ARG_INDEX = 5;

    private static final String[] paramNames =
        {"kernel",
         "stats",
         "roi",
         "masksource",
         "maskdest",
         "ignorenan"};

    private static final Class[] paramClasses =
        {javax.media.jai.KernelJAI.class,
         String[].class,
         javax.media.jai.ROI.class,
         Boolean.class,
         Boolean.class,
         Boolean.class,
        };

    private static final Object[] paramDefaults =
        {NO_PARAMETER_DEFAULT,
         NO_PARAMETER_DEFAULT,
         NO_PARAMETER_DEFAULT,
         Boolean.TRUE,
         Boolean.TRUE,
         Boolean.TRUE};

    /** Constructor. */
    public KernelStatsDescriptor() {
        super(new String[][]{
                    {"GlobalName", "KernelStats"},
                    {"LocalName", "KernelStats"},
                    {"Vendor", "jaitools.media.jai"},
                    {"Description", "Calculate neighbourhood statistics"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "1.0-SHAPSHOT"},
                    {"arg0Desc", "kernel - a JAI Kernel object"},
                    {"arg1Desc", "stats - an array of Strings specifying the statistics required"},
                    {"arg2Desc", "roi - an ROI object which must have the same pixel bounds" +
                        "as the source iamge"},
                    {"arg3Desc", "masksource (Boolean, default=true):" +
                             "if TRUE (default) only the values of source pixels where" +
                             "roi.contains is true contribute to the statistic"},
                    {"arg4Desc", "maskdest (Boolean): " +
                             "if TRUE (default) calculation is only performed" +
                             "for pixels where roi.contains is true; when false" +
                             "the destination pixel is set to NaN"},
                    {"arg5Desc", "ignorenan (Boolean): " +
                             "if TRUE (default) NaN values in source float or double images" +
                             "are ignored"}
                },

                new String[]{CollectionRegistryMode.MODE_NAME},   // supported modes
                
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
     * @param kernel a kernel defining the neighbourhood
     * @param stats an array specifying the statistics required
     * @param roi the roi controlling calculations (must have the same pixel bounds
     * as the source image)
     * @param maskSource if TRUE only the values of source pixels where
     * {@code roi.contains} is true contribute to the calculation
     * @param maskDest if TRUE the statistic is only calculated for pixels where
     * {@code roi.contains} is true; when false the destination pixel is set
     * to NaN
     * @param ignoreNaN if TRUE, NaN values in input float or double images
     * are ignored in calculations
     * @param hints useful for specifying a border extender; may be null
     * @return a Collection of RenderedImages corresponding to the
     * array of requested statistics
     * @throws IllegalArgumentException if any args are null
     */
    public static Collection<RenderedImage> createCollection(
            RenderedImage source0,
            KernelJAI kernel,
            String[] stats,
            ROI roi,
            Boolean maskSource,
            Boolean maskDest,
            Boolean ignoreNaN,
            RenderingHints hints) {
        ParameterBlockJAI pb =
                new ParameterBlockJAI("KernelStats",
                CollectionRegistryMode.MODE_NAME);

        pb.setSource("source0", source0);
        pb.setParameter("kernel", kernel);
        pb.setParameter("stats", stats);
        pb.setParameter("roi", roi);
        pb.setParameter("masksource", maskSource);
        pb.setParameter("maskdest", maskDest);
        pb.setParameter("ignorenan", ignoreNaN);

        return JAI.createCollection("KernelStats", pb, hints);
    }
}

