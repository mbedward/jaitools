/*
 * Copyright 2011 Michael Bedward
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

package jaitools.demo.contour;

import com.vividsolutions.jts.geom.LineString;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import jaitools.demo.DemoImages;
import jaitools.media.jai.contour.ContourDescriptor;
import jaitools.swing.ImageFrame;
import jaitools.swing.JTSFrame;

/**
 * Demonstrates the Contour operator. 
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class ContourDemo {
    
    public static void main(String[] args) throws Exception {
        JAI.setDefaultTileSize(new Dimension(512, 512));
        ContourDemo me = new ContourDemo();
        me.doDemo();
    }
    
    private void doDemo() throws Exception {
        RenderedImage image = 
                DemoImages.createSquircleImage(400, 400);

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
