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
package jaitools.demo.regionalize;

import jaitools.demo.DemoImageProvider;
import jaitools.demo.ImageReceiver;
import jaitools.media.jai.regionalize.RegionData;
import jaitools.media.jai.regionalize.RegionalizeDescriptor;
import jaitools.utils.ImageFrame;
import jaitools.utils.ImageUtils;
import java.awt.image.RenderedImage;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

/**
 * Demonstrates using the Regionalize operation to identify regions
 * of uniform value in a source image.
 *
 * @author Michael Bedward
 */
public class RegionalizeDemo implements ImageReceiver {

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
     * {@linkplain DemoImageProvider object}. When the image
     * has been created the receiveImage method will be called.
     */
    public void demo() {
        try {
            DemoImageProvider.getInstance().requestImage(
                    DemoImageProvider.CHESSBOARD, 320, 320, this);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Receives the test image from {@linkplain DemoImageProvider}
     * and calls the regionalizing method
     */
    public void receiveImage(RenderedImage image) {
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
        orthoImg.getAsBufferedImage();

        RegionData data = (RegionData) orthoImg.getProperty(RegionalizeDescriptor.REGION_DATA_PROPERTY);
        int numRegions = data.getData().size();

        /*
         * We use an ImageUtils method to make a nice colour image
         * of the regions to display
         */
        RenderedImage displayImg = ImageUtils.createDisplayImage(orthoImg, numRegions);

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

        // there will only be two regions
        RenderedImage diagDisplayImg = ImageUtils.createDisplayImage(diagImg, 2);

        frame = new ImageFrame(diagDisplayImg, diagImg, "Regions with diagonal connection");
        frame.setVisible(true);
    }

}
