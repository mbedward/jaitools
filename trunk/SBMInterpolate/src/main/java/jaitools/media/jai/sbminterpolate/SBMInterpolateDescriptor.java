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

package jaitools.media.jai.sbminterpolate;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ROI;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * THIS IS NOT READY FOR USE YET !!!
 * 
 * Describes the "SBMInterpolate" operation.
 * 
 * @author Michael Bedward
 */
public class SBMInterpolateDescriptor extends OperationDescriptorImpl {

    static final int ROI_ARG_INDEX = 0;
    static final int KERNEL_ARG_INDEX = 1;

    private static final String[] paramNames =
        {"roi", "kernel"};

    private static final Class[] paramClasses =
        {ROI.class, KernelJAI.class};

    private static final KernelJAI defaultKernel =
            new KernelJAI(3, 3, new float[]{1f,1f,1f,1f,0f,1f,1f,1f,1f});

    private static final Object[] paramDefaults =
        {NO_PARAMETER_DEFAULT, defaultKernel};

    /** Constructor. */
    public SBMInterpolateDescriptor() {
        super(new String[][]{
                    {"GlobalName", "SBMInterpolate"},
                    {"LocalName", "SBMInterpolate"},
                    {"Vendor", "jaitools"},
                    {"Description", "Interpolates missing data in an image using am algorithm " +
                             "published by Sprott, Bolger and Mladenoff (2002)"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "1.0-SHAPSHOT"},
                    {"arg0Desc", "roi (ROI) - the ROI defining areas of missing data"},
                    {"arg1Desc", "kernel (KernelJAI) - the kernel that defines a pixel's " +
                             "sampling neighbourhood; default is the 8 nearest neighbours"}
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
     * Get (a copy of) the default sampling kernel
     * @return a new KernelJAI instance
     */
    public static KernelJAI getDefaultKernel() {
        float[] data = defaultKernel.getKernelData();
        int w = defaultKernel.getWidth();
        return new KernelJAI(w, w, data);
    }

    /**
     * Convenience method which constructs a {@link ParameterBlockJAI} and
     * invokes {@code JAI.create("SBMInterpolate", params) }.
     *
     * @param source0 the single source image
     * @param roi an ROI object which defines the areas of missing data in the source image
     * @param kernel a KernelJAI object that defines the sampling neighbourhood to use; if
     * null a default kernel is used that samples values from the 8 nearest neighbours
     * @param hints rendering hints (may be null); useful to specify a BorderExtender
     * @return the RenderedOp destination
     */
    public static RenderedOp create(
            RenderedImage source0,
            ROI roi,
            KernelJAI kernel,
            RenderingHints hints) {
        ParameterBlockJAI pb =
                new ParameterBlockJAI("SBMInterpolate",
                RenderedRegistryMode.MODE_NAME);

        pb.setSource("source0", source0);
        pb.setParameter(paramNames[ROI_ARG_INDEX], roi);
        pb.setParameter(paramNames[KERNEL_ARG_INDEX], kernel);

        return JAI.create("SBMInterpolate", pb, hints);
    }
}

