/* 
 *  Copyright (c) 2011, Michael Bedward. All rights reserved. 
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

package org.jaitools.demo.contour;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import org.locationtech.jts.geom.LineString;

import org.jaitools.demo.DemoImages;
import org.jaitools.media.jai.contour.ContourDescriptor;
import org.jaitools.swing.ImageFrame;
import org.jaitools.swing.JTSFrame;

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
