/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.demo.contour;

import com.vividsolutions.jts.geom.LineString;

import jaitools.demo.DemoImages;
import jaitools.swing.JTSFrame;
import jaitools.media.jai.contour.ContourDescriptor;
import jaitools.swing.ImageFrame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;


/**
 *
 * @author michael
 */
public class ContourDemo {
    
    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JAI.setDefaultTileSize(new Dimension(512, 512));
        ContourDemo me = new ContourDemo();
        me.doDemo();
    }
    
    private void doDemo() throws Exception {
        RenderedImage image = 
                DemoImages.get(DemoImages.Choice.SQUIRCLE, 400, 400);

        List<Double> contourIntervals = new ArrayList<Double>();
        
        for (double level = 0.2; level < 1.41; level += 0.2) {
            contourIntervals.add(level);
        }

        ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
        pb.setSource("source0", image);
        pb.setParameter("levels", contourIntervals);

        RenderedOp dest = JAI.create("Contour", pb);
        Collection<LineString> contours = (Collection<LineString>) dest.getProperty(ContourDescriptor.CONTOUR_PROPERTY_NAME);
        
        JTSFrame jtsFrame = new JTSFrame("Contours from source image");
        for (LineString contour : contours) {
            jtsFrame.addGeometry(contour, Color.BLUE);
        }
        
        ImageFrame imgFrame = new ImageFrame(image, "Source image");
        imgFrame.setLocation(100, 100);
        imgFrame.setVisible(true);

        Dimension size = imgFrame.getSize();
        jtsFrame.setSize(size);
        jtsFrame.setLocation(100 + size.width + 5, 100);
        jtsFrame.setVisible(true);
    }
}
