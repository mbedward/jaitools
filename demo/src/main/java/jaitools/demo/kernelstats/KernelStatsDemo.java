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

package jaitools.demo.kernelstats;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.util.List;

import javax.media.jai.BorderExtender;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.ParameterBlockJAI;

import jaitools.demo.DemoImages;
import jaitools.imageutils.ImageUtils;
import jaitools.media.jai.kernel.KernelFactory;
import jaitools.numeric.Statistic;
import jaitools.swing.ImageFrame;

/**
 * Demonstrates using the KernelStats operator to calculate summary
 * statistics for the neighbourhood of each pixel in a source image
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class KernelStatsDemo {

    public static void main(String[] args) {
        KernelStatsDemo me = new KernelStatsDemo();
        me.demo();
    }

    private void demo() {
        RenderedImage image = DemoImages.get(DemoImages.Choice.INTERFERENCE, 300, 300);
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
