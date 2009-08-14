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

package jaitools.media.jai.infill;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ROI;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * An {@code OperationDescriptor} describing the "InFill" operation.
 * <p>
 * This operator fills areas of an image (e.g. of missing or corruped
 * data) with data selected from others areas of the same image. The areas
 * to be filled are defined in an accompanying ROI object.
 * <p>
 * The algorithm is adapted from that described by:
 * <blockquote>
 * Ruggero Pintus, Thomas Malzbender, Oliver Wang, Ruth Bergman,<br>
 * Hila Nachlieli and Gitit Ruckenstein (2009) <br>
 * Photo Repair and 3D Structure from Flatbed Scanners.<br>
 * Hewlett-Packard Development Company Technial Report HPL- 2009-37.<br>
 * http://www.hpl.hp.com/techreports/2009/HPL-2009-37.pdf
 * </blockquote>
 *
 * Areas to be filled are processed from the edge inwards. Each <b>target pixel</b> to
 * be filled is compared to <b>candidate pixels</b> in other areas of the image that lie
 * within a radius <i>candidateD</i>. Comparison is based on neighbourhood texture
 * which, for a given pixel <i>p</i>, is quantified as the mean and standard deviation
 * of pixel values within radius <i>nbrD</i>. Normally <i>nbrD</i> will be much
 * smaller than <i>candidateD</i>.
 * <p>
 * For efficiency, neighbourhood texture is pre-calculated for all candidate
 * pixels, ie. those outside areas to be filled but within distance <i>candidateD</i>
 * of at least one such area. This is not done for pixels within areas to be
 * filled because their neighbourhood texture depends on replacements that
 * are made around them as the algorithm proceeds.
 * <p>
 * For a given target pixel, a <b>best subset</b> of candidate pixels that
 * best match it in terms of neighbourhood texture are identified. Each pixel
 * in this subset is then re-compared to the target pixel by calculating
 * the sum of squared differences (SSD) between their respective neighbourhoods.
 * <p>
 * Final selection of a candidate pixel can be made in one of two ways:
 * <ol type="1">
 * <li> by taking the candidate with lowest SSD, with random choice to break ties
 * <li> taking a weighted average of all of the best subset candidates where the
 * weights are calculated from the product of the individual neighbourhood pixel
 * SSD values a 2D Gaussian kernel.
 * </ol>
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL: https://jai-tools.googlecode.com/svn/branches/new-ops/infill/src/main/java/jaitools/media/jai/infill/InFillDescriptor.java $
 * @version $Id: InFillDescriptor.java 535 2009-08-13 13:05:00Z michael.bedward $
 */
public class InFillDescriptor extends OperationDescriptorImpl {

    static final int ROI_ARG = 1;
    static final int NBR_RADIUS_ARG = 2;
    static final int CANDIDATE_RADIUS_ARG = 3;
    static final int CANDIDATE_SUBSET_PROP_ARG = 4;
    static final int DO_WIEGHTED_AVERAGE_ARG = 5;

    private static final String[] paramNames =
        {"roi",
         "nbrRadius",
         "candidateRadius",
         "subsetProp",
         "weightedAv"
        };

    private static final Class[] paramClasses =
        {javax.media.jai.ROI.class,
         Float.class,
         Float.class,
         Float.class,
         Boolean.class
        };

    private static final Object[] paramDefaults =
        {NO_PARAMETER_DEFAULT,
         Float.valueOf(5f),
         Float.valueOf(100f),
         Float.valueOf(0.1f),
         Boolean.TRUE
        };

    /** Constructor. */
    public InFillDescriptor() {
        super(new String[][]{
                    {"GlobalName", "InFill"},
                    {"LocalName", "InFill"},
                    {"Vendor", "jaitools"},
                    {"Description", "Fill selected areas in an image with data taken from other" +
                                    "areas chosen by comparing pixel neighbourhoods"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "1.0.0"},
                    
                    {"arg0Desc", paramNames[0] + ": an ROI object defining the areas to be filled"},

                    {"arg1Desc", paramNames[1] + ": radius of neighbourhood for pixel texture measure" +
                                 " (default " + paramDefaults[1] + ")"},

                    {"arg2Desc", paramNames[2] + ": radius within which to sample candidate pixels" +
                                 " (default " + paramDefaults[2] + ")"},

                    {"arg3Desc", paramNames[3] + ": proportion of best matching candidates to examine" +
                                 " (default " + paramDefaults[3] + ")"},

                    {"arg4Desc", paramNames[4] + ": whether to use weighted average of all best" +
                                 " matching candidates (if false, single best candidate is chosen;" +
                                 " default " + paramDefaults[4] + ")"}

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
     * invokes {@code JAI.create("InFill", params) }
     * @param source0 the image to be convolved
     * @param roi the roi that defines the areas to be filled
     * @param nbrRadius radius of neighbourhood for pixel texture measure
     * @param candidateRadius radius within which to sample candidate pixels
     * @param subsetProp proportion of best matching candidates to examine further
     * @param weightedAv whether to use weighted average of all of the subset candidates
     *        (if false, the single best candidate is chosen)
     * @param hints RenderingHints for this operation
     *
     * @return the RenderedOp destination
     * @throws IllegalArgumentException if any args are null or invalid
     */
    public static RenderedOp create(
            RenderedImage source0,
            ROI roi,
            float nbrRadius,
            float candidateRadius,
            float subsetProp,
            boolean weightedAv,
            RenderingHints hints) {

        ParameterBlockJAI pb =
                new ParameterBlockJAI("InFill",
                RenderedRegistryMode.MODE_NAME);

        pb.setSource("source0", source0);
        pb.setParameter(paramNames[0], roi);
        pb.setParameter(paramNames[1], nbrRadius);
        pb.setParameter(paramNames[2], candidateRadius);
        pb.setParameter(paramNames[3], subsetProp);
        pb.setParameter(paramNames[4], weightedAv);

        return JAI.create("InFill", pb, hints);
    }
}

