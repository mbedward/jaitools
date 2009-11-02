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

package jaitools.media.jai.maskedconvolve;

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
 * An {@code OperationDescriptor} describing the "MaskedConvolve" operation.
 * This is a variant of JAI's {@code convolve} operator which constrains the
 * convolution to pixels that are included in an {@link ROI}. 
 * <p>
 * Two masking options are provided: 
 * <ul type="1">
 * <li><b>source masking</b>, in which the ROI is used
 * to constrain which source image pixels contribute to the kernel calculation;
 * <li><b>destination masking</b> in which the ROI constrains the positioning of 
 * the convolution kernel such that destination image value will be 0 if a source
 * pixel is not contained in the ROI. 
 * </ul>The two options may be used together. If neither masking option
 * is required it is prefereable to use the "Convolve" operator for faster processing.
 * <p>
 * If there is no convolution result for a destination image pixel, either because it
 * was not included in a destination mask or had no kernel values included in a
 * source mask, it will be set to a flag value. This can be set using the {@code nilValue}
 * parameter (default is 0).
 *
 * Example of use:
 * <pre><code>
 * RenderedImage img = ...
 * 
 * float[] kernelData = new float[]{  // for neighbourhood sum
 *      0, 0, 1, 0, 0,
 *      0, 1, 1, 1, 0,
 *      1, 1, 1, 1, 1,
 *      0, 1, 1, 1, 0,
 *      0, 0, 1, 0, 0,
 * };
 *       
 * KernelJAI kernel = new KernelJAI(5, 5, kernelData);
 * ROI roi = new ROI(img, thresholdValue);
 * 
 * ParameterBlockJAI pb = new ParameterBlockJAI("maskedconvolve");
 * pb.setSource("source0", op0);
 * pb.setParameter("kernel", kernel);
 * pb.setParameter("roi", roi);
 * pb.setParameter("nilValue", Integer.valueOf(-1));
 * 
 * // no need to set masksource and maskdest params if we want to
 * // use their default values (TRUE)
 * 
 * BorderExtender extender = BorderExtender.createInstance(BorderExtender.BORDER_ZERO);
 * RenderingHints hints = new RenderingHints(JAI.KEY_BORDER_EXTENDER, extender);
 *
 * RenderedOp dest = JAI.create("maskedconvolve", pb, hints);
 *</code></pre>
 * 
 * @see <a href="http://java.sun.com/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/operator/ConvolveDescriptor.html">
 * ConvolveDescriptor</a>
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class MaskedConvolveDescriptor extends OperationDescriptorImpl {

    static final int KERNEL_ARG = 0;
    static final int ROI_ARG = 1;
    static final int MASKSRC_ARG = 2;
    static final int MASKDEST_ARG = 3;
    static final int NIL_VALUE_ARG = 4;

    private static final String[] paramNames =
        {"kernel",
         "roi",
         "masksource",
         "maskdest",
         "nilvalue"};

    private static final Class[] paramClasses =
        {javax.media.jai.KernelJAI.class,
         javax.media.jai.ROI.class,
         Boolean.class,
         Boolean.class,
         Number.class};

    private static final Object[] paramDefaults =
        {NO_PARAMETER_DEFAULT,
         NO_PARAMETER_DEFAULT,
         Boolean.TRUE,
         Boolean.TRUE,
         Integer.valueOf(0)};

    /** Constructor. */
    public MaskedConvolveDescriptor() {
        super(new String[][]{
                    {"GlobalName", "MaskedConvolve"},
                    {"LocalName", "MaskedConvolve"},
                    {"Vendor", "jaitools"},
                    {"Description", "Convolve a rendered image masked by an associated ROI"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "0.0.1"},
                    {"arg0Desc", paramNames[0] + " - a JAI Kernel object"},
                    {"arg1Desc", paramNames[1] + " - an ROI object which must have the same " +
                             "pixel bounds as the source iamge"},
                    {"arg2Desc", paramNames[2] + " (Boolean, default=true):" +
                             "if TRUE (default) only the values of source pixels where" +
                             "roi.contains is true contribute to the convolution"},
                    {"arg3Desc", paramNames[3] + " (Boolean): " +
                             "if TRUE (default) convolution is only performed" +
                             "for pixels where roi.contains is true"},
                    {"arg4Desc", paramNames[4] + " (Number): " +
                             "the value to write to the destination image for pixels where " +
                             "there is no convolution result"}

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
     * invokes {@code JAI.create("maskedconvolve", params) }
     * 
     * @param source0 the image to be convolved
     *
     * @param kernel convolution kernel
     *
     * @param roi the roi controlling convolution (must have the same pixel bounds
     *        as the source image)
     *
     * @param maskSource if TRUE only the values of source pixels where
     *        {@code roi.contains} is true contribute to the convolution
     *
     * @param maskDest if TRUE convolution is only performed for pixels where
     *        {@code roi.contains} is true
     *
     * @param nilValue value to write to the destination image for pixels where
     *        there is no convolution result
     * 
     * @param hints useful for specifying a border extender; may be null
     *
     * @return the RenderedOp destination
     * @throws IllegalArgumentException if any args are null
     */
    public static RenderedOp create(
            RenderedImage source0,
            KernelJAI kernel,
            ROI roi,
            Boolean maskSource,
            Boolean maskDest,
            Number nilValue,
            RenderingHints hints) {
        ParameterBlockJAI pb =
                new ParameterBlockJAI("MaskedConvolve",
                RenderedRegistryMode.MODE_NAME);

        pb.setSource("source0", source0);
        pb.setParameter(paramNames[KERNEL_ARG], kernel);
        pb.setParameter(paramNames[ROI_ARG], roi);
        pb.setParameter(paramNames[MASKSRC_ARG], maskSource);
        pb.setParameter(paramNames[MASKDEST_ARG], maskDest);
        pb.setParameter(paramNames[NIL_VALUE_ARG], nilValue);

        return JAI.create("MaskedConvolve", pb, hints);
    }
}

