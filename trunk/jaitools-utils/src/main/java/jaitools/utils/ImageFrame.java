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

package jaitools.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.RenderedImage;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


/**
 * A simple display widget with an image pane and a status bar that
 * shows the image location and data value(s) of the mouse cursor.
 * <p>
 * Typical use is:
 *
 * <pre>{@code \u0000
 * ImageFrame frame = new ImageFrame();
 * frame.displayImage(imageToLookAt, imageWithData, "My beautiful image");
 * }</pre>
 * 
 * @author Michael Bedward
 */
public class ImageFrame extends JFrame implements FrameWithStatusBar {

    private JTextField statusBar;

    /**
     * Constructor. Sets the default close operation to
     * EXIT_ON_CLOSE but this can be changed by the client code
     * after construction.
     */
    public ImageFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Sets the image and title for this frame, then shows the frame.
     * Data reported in the status line will be drawn from this image.
     * 
     * @param img image to be displayed
     * @param title title for the frame
     */
    public void displayImage(RenderedImage img, String title) {
        displayImage(img, null, title);
    }


    /**
     * Sets the image to display, the image to draw data from,
     * and title for this frame, then shows the frame.
     *
     * Data reported in the status line will be drawn from dataImg unless
     * it is null, in which case the data will be drawn from displayImg.
     *
     * @param displayImg image to be displayed
     *
     * @param dataImg an image with bounds equal to, or enclosing, those of
     * displayImg and which contains data that will be reported in the status
     * bar.
     *
     * @param title title for the frame
     */
    public void displayImage(RenderedImage displayImg, RenderedImage dataImg, String title) {
        setTitle(title);

        ImagePane pane = new ImagePane(this, displayImg, dataImg);
        getContentPane().add(new JScrollPane(pane), BorderLayout.CENTER);

        statusBar = new JTextField();
        statusBar.setEditable(false);
        statusBar.setMinimumSize(new Dimension(100, 30));
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());

        getContentPane().add(statusBar, BorderLayout.SOUTH);

        setSize(500, 500);
        pack();

        setLocationByPlatform(true);
        setVisible(true);
    }

    /**
     * Set the status bar contents. This is used by {@linkplain ImagePane}
     * @param text the text to display
     */
    public void setStatusText(String text) {
        statusBar.setText(text);
    }

}
