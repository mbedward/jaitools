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

package org.jaitools.demo.rangelookup;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.util.List;
import javax.media.jai.BorderExtender;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.ParameterBlockJAI;
import org.jaitools.demo.DemoImages;
import org.jaitools.demo.kernelstats.KernelStatsDemo;
import org.jaitools.imageutils.ImageUtils;
import org.jaitools.media.jai.kernel.KernelFactory;
import org.jaitools.media.jai.rangelookup.RangeLookupTable;
import org.jaitools.numeric.Range;
import org.jaitools.numeric.Statistic;
import org.jaitools.swing.ImageFrame;

/**
 *
 * @author michael
 */
public class RangeLookupDemo {
    
    /**
     * Run the example. An image is displayed and a set of statistics calculated
     * for the image values.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        RangeLookupDemo me = new RangeLookupDemo();
        me.demo();
    }

    private void demo() {
        final double maxValue = 1.0;
        RenderedImage image = DemoImages.createUniformRandomImage(300, 300, maxValue);
        ImageFrame frame = new ImageFrame(image, "RangeLookup: source image");
        frame.setVisible(true);

        doLookup(image, maxValue);
    }

    private void doLookup(RenderedImage dataImage, double maxImageValue) {
        // Divide image data into 5 bands
        double bandWidth = maxImageValue / 5;
        
        // Create the lookup table to map double source values
        // to integer destination values
        RangeLookupTable.Builder<Double, Integer> builder =
                new RangeLookupTable.Builder<Double, Integer>();
        
        // Add lookup ranges and associated result values
        double lo = -1;
        double hi = bandWidth;
        int destValue = 1;
        do {
            // we deliberately leave a gap in the lookup coverage
            if (destValue != 3) {
                builder.add(Range.create(lo, false, hi, true), destValue);
            }
            lo = hi;
            hi += bandWidth;
            destValue++ ;
        } while (lo <= maxImageValue);
        
        RangeLookupTable<Double, Integer> table = builder.build();
        
        ParameterBlockJAI pb = new ParameterBlockJAI("RangeLookup");
        pb.setSource("source0", dataImage);
        pb.setParameter("table", table);

        RenderedImage resultImg = JAI.create("RangeLookup", pb);
        ImageFrame frame = new ImageFrame(resultImg, "RangeLookup result");
        frame.setVisible(true);
    }
}
