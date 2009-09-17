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

package jaitools.swing;

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
 * Note: the default close operation for the frame is JFrame.EXIT_ON_CLOSE.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class ImageFrame extends JFrame implements FrameWithStatusBar {

    private JTextField statusBar;

    /**
     * Constructor to display and draw data from a single image
     *
     * @param img the image to display
     * @param title title for the frame
     */
    public ImageFrame(RenderedImage img, String title) {
        this(img, null, title);
    }

    /**
     * Constructor for separate display and data images.
     *
     * @param displayImg image to be displayed
     *
     * @param dataImg an image with bounds equal to, or enclosing, those of
     * displayImg and which contains data that will be reported in the status
     * bar; if null data will be drawn from the display image
     *
     * @param title title for the frame
     */
    public ImageFrame(RenderedImage displayImg, RenderedImage dataImg, String title) {
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationByPlatform(true);

        ImagePane pane = new ImagePane(this, displayImg, dataImg);
        getContentPane().add(new JScrollPane(pane), BorderLayout.CENTER);

        statusBar = new JTextField();
        statusBar.setEditable(false);
        statusBar.setMinimumSize(new Dimension(100, 30));
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());

        getContentPane().add(statusBar, BorderLayout.SOUTH);

        setSize(500, 500);
        pack();
    }

    /**
     * Set the status bar contents. This is used by {@linkplain ImagePane}
     * @param text the text to display
     */
    public void setStatusText(String text) {
        statusBar.setText(text);
    }

}
