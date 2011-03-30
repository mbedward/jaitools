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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
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

    private RenderedImage image;
    private AffineTransform imageToDisplay;
    private AffineTransform displayToImage;
    private int margin;
    
    private final Object lock = new Object();
    

    
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
    
    public Point getImageCoords(Point paneCoords, Point imageCoords) {
        Point2D p = displayToImage.transform(paneCoords, null);
        
        if (imageCoords != null) {
            imageCoords.x = (int) p.getX();
            imageCoords.y = (int) p.getY();
            return imageCoords;
        }
        return new Point((int)p.getX(), (int)p.getY());
    }
    
    public Point getPaneCoords(Point imageCoords, Point paneCoords) {
        Point2D p = imageToDisplay.transform(imageCoords, null);
        
        if (paneCoords != null) {
            paneCoords.x = (int) p.getX();
            paneCoords.y = (int) p.getY();
            return paneCoords;
        }
        return new Point((int)p.getX(), (int)p.getY());
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        synchronized(lock) {
            if (image != null) {
                if (imageToDisplay == null) {
                    setTransform();
                }
                Graphics2D g2d = (Graphics2D) g;
                g2d.drawRenderedImage(image, imageToDisplay);
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
            
                if (imageToDisplay == null) {
                    imageToDisplay = new AffineTransform();
                }
            
                double xscale = (visr.getWidth() - 2*margin) / image.getWidth();
                double yscale = (visr.getHeight() - 2*margin) / image.getHeight();
                double scale = Math.min(xscale, yscale);
        
                double xoff = margin - (scale * image.getMinX());
                double yoff = margin - (scale * image.getMinY());
                
                imageToDisplay.setTransform(scale, 0, 0, scale, xoff, yoff);
                
                try {
                    displayToImage = imageToDisplay.createInverse();
                } catch (NoninvertibleTransformException ex) {
                    // we shouldn't ever be here
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    
}