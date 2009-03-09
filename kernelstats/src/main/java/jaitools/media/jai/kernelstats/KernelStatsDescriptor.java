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
import javax.media.jai.registry.CollectionRegistryMode;

/**
 * An {@code OperationDescriptor} for the "KernelStats" operation.
 * <p>
 * For each pixel in the input image, a range of summary statistics can
 * be calculated for the values in the pixel's neighbourhood, which is defined
 * using a KernelJAI object.
 *
 * @author Michael Bedward
 */
public class KernelStatsDescriptor extends OperationDescriptorImpl {

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
                    {"arg1Desc", "stats - an array of Strings specifying the statistics required"}
                },
                new String[]{CollectionRegistryMode.MODE_NAME},   // supported modes
                
                1,                                              // number of sources
                
                new String[]{"kernel", "stats"}, // parameter names
                
                new Class[]{                                    // param classes
                    javax.media.jai.KernelJAI.class,
                    String[].class},
                    
                new Object[]{                                   // param defaults
                    NO_PARAMETER_DEFAULT, 
                    NO_PARAMETER_DEFAULT},
                    
                null                                            // valid values (none defined)
                );
    }

    /**
     * Convenience method which constructs a {@link ParameterBlockJAI} and
     * invokes {@code JAI.create("kernelstats", params) }
     * @param source0 the image for which neighbourhood statistics are required
     * @param kernel a kernel defining the neighbourhood
     * @param stats an array specifying the statistics required
     * @param hints useful for specifying a border extender; may be null
     * @return a Map<String, RenderedOp> where the key is the lower-case name of
     * a requested statistic and the value is the corresponding result image
     * @throws IllegalArgumentException if any args are null
     */
    public static Collection createCollection(
            RenderedImage source0,
            KernelJAI kernel,
            String[] stats,
            RenderingHints hints) {
        ParameterBlockJAI pb =
                new ParameterBlockJAI("KernelStats",
                CollectionRegistryMode.MODE_NAME);

        pb.setSource("source0", source0);
        pb.setParameter("kernel", kernel);
        pb.setParameter("stats", stats);
        return JAI.createCollection("KernelStats", pb, hints);
    }
}

