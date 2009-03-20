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

import jaitools.demo.DemoImageProvider;
import jaitools.demo.ImageReceiver;
import jaitools.utils.ImageFrame;
import jaitools.media.jai.kernel.KernelFactory;
import jaitools.media.jai.kernelstats.KernelStatistic;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.util.Collection;
import java.util.Iterator;
import javax.media.jai.BorderExtender;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.ParameterBlockJAI;
import javax.swing.JFrame;

/**
 * Demonstrates using the KernelStats operator to calculate summary
 * statistics for the neighbourhood of each pixel in a source image
 *
 * @author Michael Bedward
 */
public class KernelStatsDemo implements ImageReceiver {

    private static final String INPUT_IMAGE = "img";

    public static void main(String[] args) {
        KernelStatsDemo me = new KernelStatsDemo();
        me.demo();
    }

    private KernelStatsDemo() {
    }

    private void demo() {
        try {
            DemoImageProvider.getInstance().requestImage(
                    DemoImageProvider.INTERFERENCE, 300, 300, this);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void receiveImage(RenderedImage image) {
        ImageFrame frame;
        frame = new ImageFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.displayImage(image, "KernelStats demo");

        calculateStats(image);
    }

    private void calculateStats(RenderedImage img) {
        /**
         * now generate summary statistics for the base image
         */
        KernelJAI kernel = KernelFactory.createCircle(10);
        ParameterBlockJAI pb = new ParameterBlockJAI("KernelStats");

        pb.setSource("source0", img);
        pb.setParameter("kernel", kernel);
        KernelStatistic[] stats = new KernelStatistic[]
        {
            KernelStatistic.MEAN,
            KernelStatistic.SDEV,
        };

        pb.setParameter("stats", stats);

        BorderExtender extender = BorderExtender.createInstance(BorderExtender.BORDER_REFLECT);
        RenderingHints hints = new RenderingHints(JAI.KEY_BORDER_EXTENDER, extender);

        Collection<RenderedImage> statsImages = JAI.createCollection("KernelStats", pb, hints);
        Iterator<RenderedImage> iter = statsImages.iterator();

        int k = 0;
        while (iter.hasNext()) {
            ImageFrame frame = new ImageFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.displayImage(iter.next(), "Neighbourhood statistic: " + stats[k++].toString());
        }

    }

}
