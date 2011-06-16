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

package org.jaitools.demo.kernelstats;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.util.List;

import javax.media.jai.BorderExtender;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.ParameterBlockJAI;

import org.jaitools.demo.DemoImages;
import org.jaitools.media.jai.kernel.KernelFactory;
import org.jaitools.imageutils.ImageUtils;
import org.jaitools.numeric.Statistic;
import org.jaitools.swing.ImageFrame;

/**
 * Demonstrates using the KernelStats operator to calculate summary
 * statistics for the neighbourhood of each pixel in a source image
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class KernelStatsDemo {

    /**
     * Run the example. An image is displayed and a set of statistics calculated
     * for the image values.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        KernelStatsDemo me = new KernelStatsDemo();
        me.demo();
    }

    private void demo() {
        RenderedImage image = DemoImages.createInterferenceImage(300, 300);
        ImageFrame frame = new ImageFrame(image, "KernelStats: source image");
        frame.setVisible(true);

        calculateStats(image);
    }

    private void calculateStats(RenderedImage dataImage) {
        /**
         * now generate summary statistics for the base image
         */
        KernelJAI kernel = KernelFactory.createCircle(10);
        ParameterBlockJAI pb = new ParameterBlockJAI("KernelStats");

        pb.setSource("source0", dataImage);
        pb.setParameter("kernel", kernel);
        Statistic[] stats = new Statistic[]
        {
            Statistic.MEAN,
            Statistic.SDEV,
        };

        pb.setParameter("stats", stats);

        BorderExtender extender = BorderExtender.createInstance(BorderExtender.BORDER_REFLECT);
        RenderingHints hints = new RenderingHints(JAI.KEY_BORDER_EXTENDER, extender);

        RenderedImage multibandImg = JAI.create("KernelStats", pb, hints);
        /*
         * The bands of statsImg correspond to each of the statistics
         * that we requested. We can use the handy ImageUtils.getBandsAsImages
         * method to convert them to a list of separate images
         */
        List<RenderedImage> statImages = ImageUtils.getBandsAsImages(multibandImg);
        int k = 0;
        for (RenderedImage statImg : statImages) {
            ImageFrame frame = new ImageFrame(statImg, "KernelStats: " + stats[k++].toString());
            frame.setVisible(true);
        }

    }

}
