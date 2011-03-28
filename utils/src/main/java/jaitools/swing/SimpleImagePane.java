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

package jaitools.swing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import javax.swing.JPanel;


/**
 * A very basic Swing widget to display a {@code RenderedImage}.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class SimpleImagePane extends JPanel {

    RenderedImage image;
    AffineTransform transform;
    private final Object lock = new Object();
    
    private int margin;

    
    public SimpleImagePane() {
        margin = 0;
        
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent ce) {
                setTransform();
            }
        });
    }
    
    public void setImage(RenderedImage image) {
        this.image = image;
        setTransform();
        repaint();
    }
    
    public void clear() {
        image = null;
        repaint();
    }
    
    public void resetTransform() {
        setTransform();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        synchronized(lock) {
            if (image != null) {
                if (transform == null) {
                    setTransform();
                }
                Graphics2D g2d = (Graphics2D) g;
                g2d.drawRenderedImage(image, transform);
            }
        }
    }
    
    private void setTransform() {
        synchronized(lock) {
            if (image != null) {
                Rectangle visr = getVisibleRect();
                if (visr.isEmpty()) {
                    return;
                }
            
                if (transform == null) {
                    transform = new AffineTransform();
                }
            
                double xscale = (visr.getWidth() - 2*margin) / image.getWidth();
                double yscale = (visr.getHeight() - 2*margin) / image.getHeight();
                double scale = Math.min(xscale, yscale);
        
                double xoff = margin - (scale * image.getMinX());
                double yoff = margin - (scale * image.getMinY());
                
                transform.setTransform(scale, 0, 0, scale, xoff, yoff);
            }
        }
    }
    
}