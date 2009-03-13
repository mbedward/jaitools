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

package jaitools.demo.utils;

import com.sun.media.jai.widget.DisplayJAI;
import java.awt.BorderLayout;
import java.awt.image.RenderedImage;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * Simple display widget
 * 
 * @author Michael Bedward
 */
public class ImageFrame {

    /**
     * Displays an image in a scrolling panel
     * 
     * @param img image to be displayed
     * @param title title for the frame
     */
    public void displayImage(RenderedImage img, String title) {
        JFrame frame = new JFrame(title);

        DisplayJAI disp = new DisplayJAI(img);
        frame.getContentPane().add(new JScrollPane(disp), BorderLayout.CENTER);

        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }
    
}
