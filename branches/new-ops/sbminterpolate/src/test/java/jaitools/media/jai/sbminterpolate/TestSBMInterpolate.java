/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.media.jai.sbminterpolate;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RenderedRegistryMode;
import org.junit.Test;

/**
 * @author Michael Bedward
 */
public class TestSBMInterpolate {

    private static RenderedImage theImage = null;

    public TestSBMInterpolate() {
        ensureRegistered();
        fixForOSX();
    }

    @Test
    public void testSingle() throws Exception {
        System.out.println("   testing with single, missing area");

        Class jaiClass = Class.forName("javax.media.jai.JAI");
        Object jaiInstance = jaiClass.newInstance();
        String jaiVersion = jaiClass.getPackage().getImplementationVersion();
        System.out.println("JAI version " + jaiVersion);

        JAI.setDefaultTileSize(new Dimension(128, 128));
        
        RenderedImage testImg = getTestImage();
        int w = testImg.getWidth();
        int h = testImg.getHeight();

        ParameterBlockJAI pb = new ParameterBlockJAI("constant");
        pb.setParameter("width", (float)75);
        pb.setParameter("height", (float)75);
        pb.setParameter("bandValues", new Integer[]{1});
        RenderedOp roiImg = JAI.create("constant", pb);

        pb = new ParameterBlockJAI("translate");
        pb.setSource("source0", roiImg);
        pb.setParameter("xtrans", (float)75);
        pb.setParameter("ytrans", (float)250);
        RenderedOp roiImgT = JAI.create("translate", pb);

        ROI roi = new ROI(roiImgT, 0);

        pb = new ParameterBlockJAI("SBMInterpolate");
        pb.setSource("source0", testImg);
        pb.setParameter("roi", roi);
        RenderedOp interpImg = JAI.create("sbminterpolate", pb);
    }

    private RenderedImage getTestImage() throws IOException {
        if (theImage == null) {
            URL url = this.getClass().getResource("/images/lena512color.tiff");
            theImage = PlanarImage.wrapRenderedImage(ImageIO.read(url));
        }

        return theImage;
    }

    /**
     * Register the operator with JAI if it is not already registered
     */
    private void ensureRegistered() {
        OperationRegistry reg = JAI.getDefaultInstance().getOperationRegistry();
        String[] names = reg.getDescriptorNames(RenderedRegistryMode.MODE_NAME);
        SBMInterpolateDescriptor desc = new SBMInterpolateDescriptor();
        String descName = desc.getName();
        for (String name : names) {
            if (descName.equalsIgnoreCase(name)) {
                return;
            }
        }

        SBMInterpolateSpi spi = new SBMInterpolateSpi();
        spi.updateRegistry(reg);
    }

    /**
     * If we are running on OSX, turn off native acceleration
     * for JAI operations so that operators work properly
     * with double and double data rasters.
     */
    private void fixForOSX() {
        Properties sys = new Properties(System.getProperties());
        if (sys.getProperty("os.name").compareToIgnoreCase("mac os x") == 0) {
            sys.put("com.sun.media.jai.disableMediaLib", "true");
        }
        System.setProperties(sys);
    }

}
