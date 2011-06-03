/* 
 *  Copyright (c) 2009, Michael Bedward. All rights reserved. 
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

package jaitools.media.jai.maskedconvolve;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
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
     * @param minCells the minimum number of non-zero kernel cells that be positioned over
     *        unmasked source image cells for convolution to be performed for the target cell;
     *        any of the following are accepted: <ol type = "1">
     *        <li> "ANY" (case-insensitive) (or the constant {@linkplain #MIN_CELLS_ANY});
     *             this is the default
     *        <li> "ALL" (case-insensitive) (or the constant {@linkplain #MIN_CELLS_ALL});
     *        <li> a {@code Number} with value between one and the number of non-zero kernel
     *             cells (inclusive)
     *        <li> a {@code String} representing a numeric value
     *        </ol>
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
 * @version $Id$
 */
public class MaskedConvolveDescriptor extends OperationDescriptorImpl {

    /**
     * Constant that can be used for the minCells parameter to specify
     * convolution will be performed for a terget cell if any non-zero kernel
     * cells overlap with unmasked source image cells. This is the default
     * parameter value.
     */
    public static final String MIN_CELLS_ANY = "ANY";

    /**
     * Constant that can be used for the minCells parameter to require
     * all non-zero kernel cells to overlap with unmasked source image
     * cells for convolution to be performed for a terget cell
     */
    public static final String MIN_CELLS_ALL = "ALL";

    /** Default value for nilValue parameter (0) */
    public static final Number DEFAULT_NIL_VALUE = Integer.valueOf(0);

    static final int KERNEL_ARG = 0;
    static final int ROI_ARG = 1;
    static final int MASKSRC_ARG = 2;
    static final int MASKDEST_ARG = 3;
    static final int NIL_VALUE_ARG = 4;
    static final int MIN_CELLS_ARG = 5;

    private static final String[] paramNames = {
        "kernel",
        "roi",
        "maskSource",
        "maskDest",
        "nilValue",
        "minCells"
    };

    private static final Class[] paramClasses = {
         javax.media.jai.KernelJAI.class,
         javax.media.jai.ROI.class,
         Boolean.class,
         Boolean.class,
         Number.class,
         Object.class
    };

    private static final Object[] paramDefaults = {
         NO_PARAMETER_DEFAULT,
         NO_PARAMETER_DEFAULT,
         Boolean.TRUE,
         Boolean.TRUE,
         DEFAULT_NIL_VALUE,
         MIN_CELLS_ANY
    };

    /** Constructor. */
    public MaskedConvolveDescriptor() {
        super(new String[][]{
                    {"GlobalName", "MaskedConvolve"},
                    {"LocalName", "MaskedConvolve"},
                    {"Vendor", "jaitools.media.jai"},
                    {"Description", "Convolve a rendered image masked by an associated ROI"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "1.0.0"},
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
                             "there is no convolution result"},
                    {"arg5Desc", paramNames[5] + " (String or Number, default=MIN_CELLS_ANY):" +
                             "the minimum number of non-zero kernel cells that must overlap" +
                             "unmasked source image cells for convolution to be performed"}

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
     * Constructs a {@link ParameterBlockJAI} and
     * invokes {@code JAI.create("maskedconvolve", params) }.
     * <p>
     * <b>Note:</b> with this method only integer values can be passed for the {@code minCells}
     * argument. To use the Strings "ANY" or "ALL" or the constants described in the class docs
     * use the {@code JAI.create} method with a parameter block rather than this method.
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
     * @param minCells the minimum number of non-zero kernel cells that be positioned over
     *        unmasked source image cells for convolution to be performed for the target cell
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
            int minCells,
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
        pb.setParameter(paramNames[MIN_CELLS_ARG], Integer.valueOf(minCells));

        return JAI.create("MaskedConvolve", pb, hints);
    }

    @Override
    protected boolean validateParameters(String modeName, ParameterBlock pb, StringBuffer msg) {

        final String minCellsErrorMsg = "minCells must be ANY, ALL or a numeric value" +
                " between 1 and the number of non-zero kernel cells";

        boolean ok = super.validateParameters(modeName, pb, msg);
        if (ok) {
            KernelJAI kernel = (KernelJAI) pb.getObjectParameter(KERNEL_ARG);
            Object minCells = pb.getObjectParameter(MIN_CELLS_ARG);
            int minCellsValue = parseMinCells(minCells, kernel);
            if (minCellsValue >= 1) {
                pb.set(Integer.valueOf(minCellsValue), MIN_CELLS_ARG);

            } else {
                msg.append(minCellsErrorMsg);
                ok = false;
            }
        }
        return ok;
    }

    /**
     * Attempt to parse the Object value of the minCells parameter.
     *
     * @param minCells Object from the parameter block
     *
     * @param numActiveKernelCells upper limit on the numeric value
     *
     * @return the parsed value as an int or -1 if parsing failed or the
     *         parsed value was out of range
     */
    private int parseMinCells(Object minCells, KernelJAI kernel) {
        int value = -1;

        int numActiveKernelCells = numActiveKernelCells(kernel);

        if (minCells instanceof String) {
            /*
             * First try to parse it as a String
             */
            String s = (String) minCells;
            if (s.trim().equalsIgnoreCase(MaskedConvolveDescriptor.MIN_CELLS_ALL)) {
                value = numActiveKernelCells;
            }

            if (s.trim().equalsIgnoreCase(MaskedConvolveDescriptor.MIN_CELLS_ANY)) {
                value = 1;
            }

            if (value < 0) {
                /*
                 * Try it as the String value of a number
                 */
                try {
                    int n = Double.valueOf(s).intValue();
                    if (n > 0 && n <= numActiveKernelCells) {
                        value = n;
                    }

                } catch (NumberFormatException ex) {
                    // do nothing
                }
            }

            /*
             * Try it as a Number object
             */
        } else if (minCells instanceof Number) {
            int n = ((Number) minCells).intValue();
            if (n > 0 && n <= numActiveKernelCells) {
                value = n;
            }

        } else {
            /*
             * Don't know what it is but we don't like it !
             */
        }

        return value;
    }

    /**
     * Counts the number of active cells (those with non-zero values) in the kernel.
     * A round-off tolerance of 1.0e-6 is used.
     *
     * @param kernel the kernel
     *
     * @return the number of non-zero cells
     */
    private int numActiveKernelCells(KernelJAI kernel) {
        final float TOL = 1.0e-6F;
        float[] data = kernel.getKernelData();

        int n = 0;
        for (float cellValue : data) {
            if (Math.abs(cellValue) > TOL) {
                n++ ;
            }
        }

        return n;
    }

}

