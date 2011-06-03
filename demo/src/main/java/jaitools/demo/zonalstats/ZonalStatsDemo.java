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
package jaitools.demo.zonalstats;

import java.awt.image.RenderedImage;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import jaitools.demo.DemoImages;
import jaitools.media.jai.zonalstats.ZonalStats;
import jaitools.media.jai.zonalstats.ZonalStatsDescriptor;
import jaitools.numeric.Statistic;

/**
 * Demonstrates using the ZonalStats operator to calculate summary statistics of values
 * in a data image within zones defined by a zone image. In this example the data image
 * contains uniform random values and the zones are equal in size, so we expect the value
 * of each summary statistic to be very similar across zones.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class ZonalStatsDemo {

    private RenderedImage dataImg;
    private RenderedImage zoneImg;

    /**
     * Main method: constructs an instance of this class (which
     * causes test data to be generated) and runs the demo
     * @param args ignored
     * @throws java.lang.Exception if there was a problem generating
     * the test data
     */
    public static void main(String[] args) throws Exception {
        ZonalStatsDemo me = new ZonalStatsDemo();
        me.demo();
    }
    
    /**
     * Constructor: gets test images.
     */
    public ZonalStatsDemo() {
        dataImg = DemoImages.createUniformRandomImage(500, 500, 10.0);
        zoneImg = DemoImages.createBandedImage(500, 500, 5);
    }

    /**
     * Calculates min, max, median, approximate median and sample standard
     * deviation for a data image of uniformly distributed random values
     * between 0 and 10, within zones which are equal area horizontal bands.
     * The results should be very similar across zones and approximately
     * equal to: min=0; max=10; median = approx median = 5; sdev = 2.88
     */
    private void demo() {
        /*
         * Define the parameters for the ZonalStats operator. We let the
         * default values apply for data image band (0), roi (null),
         * zoneTransform (null / identity) and ignoreNaN (true)
         */
        ParameterBlockJAI pb = new ParameterBlockJAI("zonalstats");
        pb.setSource("dataImage", dataImg);
        pb.setSource("zoneImage", zoneImg);

        Statistic[] statistics = {
            Statistic.MIN,
            Statistic.MAX,
            Statistic.MEDIAN,
            Statistic.APPROX_MEDIAN,
            Statistic.SDEV
        };

        pb.setParameter("stats", statistics);

        RenderedOp zsImg = JAI.create("zonalstats", pb);

        ZonalStats zs = (ZonalStats) zsImg.getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
        System.out.println("                               exact    approx");
        System.out.println(" band zone      min      max   median   median     sdev");
        System.out.println("-----------------------------------------------------------");

        final int band = 0;
        for (int z : zs.getZones()) {
            System.out.printf(" %4d %4d", band, z);

            ZonalStats zoneSubset = zs.band(0).zone(z);
            for (Statistic s : statistics) {
                System.out.printf(" %8.4f", zoneSubset.statistic(s).results().get(0).getValue());
            }
            System.out.println();
        }
    }

}
