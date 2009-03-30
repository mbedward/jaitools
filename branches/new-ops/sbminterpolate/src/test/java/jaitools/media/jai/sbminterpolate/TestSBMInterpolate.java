/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.media.jai.sbminterpolate;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ROI;
import javax.media.jai.RenderedOp;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Bedward
 */
public class TestSBMInterpolate {

    private static RenderedImage theImage = null;

    @Ignore
    @Test
    public void testSingle() throws Exception {
        System.out.println("   testing with single, central missing area");

        JAI.setDefaultTileSize(new Dimension(128, 128));
        
        RenderedImage testImg = getTestImage();
        int w = testImg.getWidth();
        int h = testImg.getHeight();

        ParameterBlockJAI pb = new ParameterBlockJAI("constant");
        pb.setParameter("width", (float)w/4);
        pb.setParameter("height", (float)h/4);
        pb.setParameter("bandValues", new Integer[]{1});
        RenderedOp roiImg = JAI.create("constant", pb);

        pb = new ParameterBlockJAI("translate");
        pb.setSource("source0", roiImg);
        pb.setParameter("xtrans", (float)w/8);
        pb.setParameter("ytrans", (float)h/8);
        RenderedOp roiImgT = JAI.create("translate", pb);

        ROI roi = new ROI(roiImgT, 0);

        pb = new ParameterBlockJAI("sbminterpolate");
        pb.setSource("source0", testImg);
        pb.setParameter("roi", roi);
        RenderedOp interpImg = JAI.create("sbminterpolate", pb);
    }

    private RenderedImage getTestImage() throws IOException {
        if (theImage == null) {
            URL url = this.getClass().getResource("/images/lena512color.tiff");
            theImage = ImageIO.read(url);
        }

        return theImage;
    }
}
