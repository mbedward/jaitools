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

import com.sun.media.jai.opimage.RIFUtil;
import java.awt.RenderingHints;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.BorderExtender;
import javax.media.jai.CollectionImage;
import javax.media.jai.CollectionImageFactory;
import javax.media.jai.CollectionOp;
import javax.media.jai.ImageLayout;
import javax.media.jai.KernelJAI;
import javax.media.jai.ROI;


    /**
 * The image factory for the {@link KernelStatsOpImage} operation.
 *
 * @author Michael Bedward
 */
public class KernelStatsCIF implements CollectionImageFactory {

    /** Constructor */
    public KernelStatsCIF() {
    }

    /**
     *
     * @param arg0
     * @param arg1
     * @return
     */
    public CollectionImage create(ParameterBlock paramBlock, RenderingHints renderHints) {
        // Get ImageLayout from renderHints if any.
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);

        // Get BorderExtender from renderHints if any.
        BorderExtender extender = RIFUtil.getBorderExtenderHint(renderHints);

        KernelJAI unRotatedKernel =
                (KernelJAI) paramBlock.getObjectParameter(KernelStatsDescriptor.KERNEL_ARG_INDEX);
        KernelJAI kJAI = unRotatedKernel.getRotatedKernel();

        KernelStatistic[] stats =
                (KernelStatistic[]) paramBlock.getObjectParameter(KernelStatsDescriptor.STATS_ARG_INDEX);

        ROI roi = (ROI) paramBlock.getObjectParameter(KernelStatsDescriptor.ROI_ARG_INDEX);

        Boolean maskSrc =
                (Boolean) paramBlock.getObjectParameter(KernelStatsDescriptor.MASKSRC_ARG_INDEX);

        Boolean maskDest =
                (Boolean) paramBlock.getObjectParameter(KernelStatsDescriptor.MASKDEST_ARG_INDEX);

        Boolean ignoreNaN =
                (Boolean) paramBlock.getObjectParameter(KernelStatsDescriptor.NAN_ARG_INDEX);

        return new KernelStatsOpImage(paramBlock.getRenderedSource(0),
                extender, renderHints, layout, kJAI, stats, roi, maskSrc, maskDest, ignoreNaN);
      }

    /**
     * Not intended for use
     */
    public CollectionImage update(ParameterBlock arg0, RenderingHints arg1, ParameterBlock arg2, RenderingHints arg3, CollectionImage arg4, CollectionOp arg5) {
        return null;
    }

}

