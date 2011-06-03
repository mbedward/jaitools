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
package jaitools.demo.regionalize;

import jaitools.CollectionFactory;
import java.awt.Color;
import java.awt.image.RenderedImage;
import java.util.List;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import jaitools.demo.DemoImages;
import jaitools.imageutils.ImageUtils;
import jaitools.media.jai.regionalize.Region;
import jaitools.media.jai.regionalize.RegionalizeDescriptor;
import jaitools.swing.ImageFrame;
import java.util.Map;

/**
 * Demonstrates using the Regionalize operation to identify regions
 * of uniform value in a source image.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class RegionalizeDemo {

    /**
     * Main method: simple calls the demo method
     * @param args ignored
     */
    public static void main(String[] args) {
        RegionalizeDemo me = new RegionalizeDemo();
        me.demo();
    }

    /**
     * Gets a test image (the chessboard image) from the
     * {@linkplain DemoImages object}. When the image
     * has been created the receiveImage method will be called.
     */
    public void demo() {
        RenderedImage image = DemoImages.createChessboardImage(320, 320);
        ImageFrame frame;
        frame = new ImageFrame(image, "Regionalize demo: test image");
        frame.setVisible(true);

        regionalizeImage(image);
    }


    /**
     * Regionalizes the test chessboard image in two ways:
     * firstly with only orthogonal connectedness; then
     * allowing diagonal connectedness. Displays the results
     * of each regionalization in an {@linkplain ImageFrame}.
     *
     * @param image the test image
     */
    public void regionalizeImage(RenderedImage image) {

        ImageFrame frame;

        /*
         * Regionalize the source chessboard image,
         * specifying orthogonal connectedness by setting the
         * diagonal parameter to false
         */
        ParameterBlockJAI pb = new ParameterBlockJAI("regionalize");
        pb.setSource("source0", image);
        pb.setParameter("diagonal", false);
        RenderedOp orthoImg = JAI.create("Regionalize", pb);

        /*
         * At present, we have to force JAI to render the image
         * before we can access the region data in the image
         * properties. Calling getAsBufferedImage() accomplishes
         * this.
         *
         * @todo remove this necessity
         */
        orthoImg.getData();

        /*
         * Get summary data for regions and print it to the console
         */
        List<Region> regions = (List<Region>) orthoImg.getProperty(RegionalizeDescriptor.REGION_DATA_PROPERTY);
        for (Region r : regions) {
            System.out.println(r);
        }
        
        /*
         * We use an ImageUtils method to make a nice colour image
         * of the regions to display
         */
        Color[] colors = ImageUtils.createRampColours(regions.size());
        Map<Integer, Color> colorMap = CollectionFactory.map();
        int k = 0;
        for (Region r : regions) {
            colorMap.put(r.getId(), colors[k++]);
        }
        
        RenderedImage displayImg = ImageUtils.createDisplayImage(orthoImg, colorMap);
        frame = new ImageFrame(displayImg, orthoImg, "Regions with orthogonal connection");
        frame.setVisible(true);

        /*
         * Repeat the regionalization of the source image
         * allowing diagonal connections within regions
         */

        pb = new ParameterBlockJAI("regionalize");
        pb.setSource("source0", image);
        pb.setParameter("diagonal", true);
        RenderedOp diagImg = JAI.create("regionalize", pb);

        colorMap.clear();
        colorMap.put(1, Color.CYAN);
        colorMap.put(2, Color.ORANGE);
        RenderedImage diagDisplayImg = ImageUtils.createDisplayImage(diagImg, colorMap);

        frame = new ImageFrame(diagDisplayImg, diagImg, "Regions with diagonal connection");
        frame.setVisible(true);
    }

}
