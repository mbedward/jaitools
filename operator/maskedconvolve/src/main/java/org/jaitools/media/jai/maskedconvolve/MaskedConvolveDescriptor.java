/* 
 *  Copyright (c) 2009-2011, Michael Bedward. All rights reserved. 
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

package org.jaitools.media.jai.maskedconvolve;

import java.awt.image.renderable.ParameterBlock;
import java.util.Collection;

import javax.media.jai.KernelJAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.registry.RenderedRegistryMode;

import org.jaitools.numeric.Range;


/**
 * The "MaskedConvolve" operation extends JAI's {@code convolve} operator by providing
 * the option to select which source and or destination pixels are included in processing
 * using an associated {@code ROI} object.
 * <p>
 * Two masking options are provided: 
 * <ul type="1">
 * <li>
 * <b>source masking</b>, in which the ROI is used to constrain which source image 
 * pixels contribute to the kernel calculation;
 * </li>
 * <li>
 * <b>destination masking</b> in which the convolution kernel will only be placed
 * over pixels that are within the ROI.
 * </li>
 * </ul>
 * 
 * The two options may be used together. If neither masking option
 * is required it is better to use the standard "Convolve" operator as it will
 * be faster.
 * <p>
 * 
 * With this operator, a given destination pixel will have a <i>nil</i> result if any
 * of the following are true:
 * <ul>
 * <li>
 * The pixel was not included in a destination mask.
 * </li>
 * <li>
 * The pixel had no kernel values included in a source mask.
 * </li>
 * <li>
 * The requirement for the minimum number of unmasked source image values was not met
 * (explained below in relation to the {@code minCells} parameter).
 * </li>
 * </ul>
 * A flag value is returned for such pixels which you can specify with the {@code nilValue}
 * parameter (the default is 0).
 * <p>
 * 
 * You can specify the minimum number of non-zero kernel cells that must be positioned over
 * unmasked source image pixels for convolution to be performed via the {@code minCells} 
 * parameter. If there are NO_DATA values defined (see {@code nodata} parameter below) then
 * this parameter refers to the number of unmasked source image pixels with data.
 * Any of the following are accepted: 
 * <ol type = "1">
 * <li> 
 * "ANY" (case-insensitive) (or the constant {@linkplain #MIN_CELLS_ANY}).
 * With this option, convolution will be performed if there is at least one
 * unmasked source image value in the pixel neighbourhood.
 * This is the default.
 * </li>
 * <li> 
 * "ALL" (case-insensitive) (or the constant {@linkplain #MIN_CELLS_ALL}).
 * Convolution will only be performed if all source image pixels in the neighbourhood
 * are unmasked.
 * </li>
 * <li> 
 * A {@code Number} with value between one and the number of non-zero kernel cells (inclusive).
 * </li>
 * <li> 
 * A {@code String} representing a numeric value.
 * </li>
 * </ol>
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
 * <b>Summary of parameters:</b>
 * <table border="1" cellpadding="3">
 * <caption>Summary of parameters</caption>
 * <tr>
 * <th>Name</th>
 * <th>Class</th>
 * <th>Default</th>
 * <th>Description</th>
 * </tr>
 * 
 * <tr>
 * <td>kernel</td>
 * <td>KernelJAI</td>
 * <td>No default</td>
 * <td>Kernel defining the convolution neighbourhood</td>
 * </tr>
 * 
 * <tr>
 * <td>roi</td>
 * <td>ROI</td>
 * <td>No default</td>
 * <td>ROI to use for source and/or destination masking</td>
 * </tr>
 * 
 * <tr>
 * <td>maskSource</td>
 * <td>Boolean</td>
 * <td>true</td>
 * <td>Whether to apply source masking</td>
 * </tr>
 * 
 * <tr>
 * <td>maskDest</td>
 * <td>Boolean</td>
 * <td>true</td>
 * <td>Whether to apply destination masking</td>
 * </tr>
 * 
 * <tr>
 * <td>nilValue</td>
 * <td>Number</td>
 * <td>0</td>
 * <td>Value to return for pixels with no convolution result</td>
 * </tr>
 * 
 * <tr>
 * <td>minCells</td>
 * <td>See note above</td>
 * <td>MIN_CELLS_ANY</td>
 * <td>
 * Minimun number of non-zero kernel cells over unmasked source image pixels
 * for convolution to be performed
 * </td>
 * </tr>
 * 
 * <tr>
 * <td>nodata</td>
 * <td>Collection</td>
 * <td>null</td>
 * <td>
 * Values to be treated as NO_DATA. A value can be either a Number or a 
 * {@link org.jaitools.numeric.Range} (mixtures of both are permitted).
 * </td>
 * </tr>
 * 
 * <tr>
 * <td>strictNodata</td>
 * <td>Boolean</td>
 * <td>false</td>
 * <td>
 * If {@code true}, convolution will no be performed for a target pixel with
 * any NODATA values in its neighbourhood (non-zero kernel cells); if {@code false}
 * convolution can occur, subject to masking and {@code minCells} criteria, with 
 * NODATA values being ignored.
 * </td>
 * </tr>
 * 
 * </table>
 * 
 * @see org.jaitools.media.jai.kernel.KernelFactory
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
    static final int NO_DATA_ARG = 6;
    static final int STRICT_NO_DATA_ARG = 7;

    private static final String[] paramNames = {
        "kernel",
        "roi",
        "maskSource",
        "maskDest",
        "nilValue",
        "minCells",
        "nodata",
        "strictNodata"
    };

    private static final Class[] paramClasses = {
         javax.media.jai.KernelJAI.class,
         javax.media.jai.ROI.class,
         Boolean.class,
         Boolean.class,
         Number.class,
         Object.class,
         Collection.class,
         Boolean.class
    };

    private static final Object[] paramDefaults = {
         NO_PARAMETER_DEFAULT,
         NO_PARAMETER_DEFAULT,
         Boolean.TRUE,
         Boolean.TRUE,
         DEFAULT_NIL_VALUE,
         MIN_CELLS_ANY,
         (Collection) null,
         Boolean.FALSE
    };

    /** Constructor. */
    public MaskedConvolveDescriptor() {
        super(new String[][]{
                    {"GlobalName", "MaskedConvolve"},
                    {"LocalName", "MaskedConvolve"},
                    {"Vendor", "org.jaitools.media.jai"},
                    {"Description", "Convolve a rendered image masked by an associated ROI"},
                    {"DocURL", "http://code.google.com/p/jaitools/"},
                    {"Version", "1.0.0"},
                    
                    {"arg0Desc", paramNames[KERNEL_ARG] + " - a JAI Kernel object"},
                    
                    {"arg1Desc", paramNames[ROI_ARG] + 
                             " - an ROI object which must have the same " +
                             "pixel bounds as the source iamge"},
                    
                    {"arg2Desc", paramNames[MASKSRC_ARG] + " (Boolean, default=true):" +
                             "if TRUE (default) only the values of source pixels where" +
                             "roi.contains is true contribute to the convolution"},

                    {"arg3Desc", paramNames[MASKDEST_ARG] + " (Boolean): " +
                             "if TRUE (default) convolution is only performed" +
                             "for pixels where roi.contains is true"},

                    {"arg4Desc", paramNames[NIL_VALUE_ARG] + " (Number): " +
                             "the value to write to the destination image for pixels where " +
                             "there is no convolution result"},

                    {"arg5Desc", paramNames[MIN_CELLS_ARG] + 
                             " (String or Number, default=MIN_CELLS_ANY):" +
                             "the minimum number of non-zero kernel cells that must overlap" +
                             "unmasked source image cells for convolution to be performed"},

                    {"arg6Desc", paramNames[NO_DATA_ARG] + " (Collection) " +
                             "values to be treated as NO_DATA; elements can be Number and/or" +
                             " Range"},

                    {"arg6Desc", paramNames[STRICT_NO_DATA_ARG] + " (Collection) " +
                             "values to be treated as NO_DATA; elements can be Number and/or" +
                             " Range"}

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
     * Validates the supplied parameters.
     * 
     * @param modeName the rendering mode
     * @param pb the input parameters
     * @param msg a {@code StringBuffer} instance to receive error messages
     * 
     * @return {@code true} if parameters are valid; {@code false} otherwise
     */
    @Override
    protected boolean validateParameters(String modeName, ParameterBlock pb, StringBuffer msg) {

        final String minCellsErrorMsg = "minCells must be ANY, ALL or a numeric value" +
                " between 1 and the number of non-zero kernel cells";

        final String nodataErrorMsg =
                "nodata parameter must be a Collection of Numbers and/or Ranges";
        
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
            
            Object objc = pb.getObjectParameter(NO_DATA_ARG);
            if (objc != null) {
                if (!(objc instanceof Collection)) {
                    msg.append(nodataErrorMsg);
                    ok = false;
                } else {
                    Collection col = (Collection) objc;
                    for (Object oelem : col) {
                        if (!(oelem instanceof Number || oelem instanceof Range)) {
                            msg.append(nodataErrorMsg);
                            ok = false;
                            break;
                        }
                    }
                }
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

