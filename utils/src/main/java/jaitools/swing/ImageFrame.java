/*
 * Copyright 2009-2011 Michael Bedward
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

package jaitools.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.RenderedImage;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTextField;


/**
 * A simple display widget with an image pane and a status bar that
 * shows the image location and data value(s) of the mouse cursor.
 * <p>
 * Typical use is:
 *
 * <pre><code>
 * ImageFrame frame = new ImageFrame();
 * frame.displayImage(imageToLookAt, imageWithData, "My beautiful image");
 * </code></pre>
 *
 * Note: the default close operation for the frame is JFrame.EXIT_ON_CLOSE.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class ImageFrame extends JFrame {

    private JTextField statusBar;
    private StringBuilder sb;

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
        getContentPane().add(pane, BorderLayout.CENTER);

        sb = new StringBuilder();

        statusBar = new JTextField();
        statusBar.setEditable(false);
        statusBar.setMinimumSize(new Dimension(100, 30));
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());

        getContentPane().add(statusBar, BorderLayout.SOUTH);

        pack();
        setSize(500, 500);
    }

    void setCursorInfo(Point imagePos, int[] imageValues) {
        Integer[] values = new Integer[imageValues.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = Integer.valueOf(imageValues[i]);
        }
        doSetCursorInfo(imagePos, values, "%d");
    }

    void setCursorInfo(Point imagePos, double[] imageValues) {
        Double[] values = new Double[imageValues.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = new Double(imageValues[i]);
        }
        doSetCursorInfo(imagePos, values, "%.4f");
    }
    
    private void doSetCursorInfo(Point imagePos, Number[] imageValues, String fmt) {
        sb.setLength(0);
        sb.append("x:").append(imagePos.x).append(" y:").append(imagePos.y);
        
        final int len = imageValues.length;
        if (len == 1) {
            sb.append("  value:");
        } else {
            sb.append("  values:");
        }
        
        for (int i = 0; i < len; i++) {
            sb.append(" ").append(String.format(fmt, imageValues[i]));
        }
        
        setStatusText(sb.toString());
    }

    /**
     * Set the status bar contents. This is used by {@linkplain ImagePane}
     * @param text the text to display
     */
    public void setStatusText(String text) {
        statusBar.setText(text);
    }

}
