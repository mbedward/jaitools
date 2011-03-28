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

package jaitools.media.jai.jiffleop;

import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.JiffleException;
import jaitools.jiffle.runtime.JiffleIndirectRuntime;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Map;
import java.util.Vector;
import javax.media.jai.ImageLayout;
import javax.media.jai.OpImage;
import javax.media.jai.PlanarImage;
import javax.media.jai.SourcelessOpImage;

/**
 * Jiffle operation.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class JiffleOpImage extends OpImage {
    
    private final JiffleIndirectRuntime runtime;
    
    // TESTING
    private final int band = 0;
    
    private final Rectangle bounds;

    public JiffleOpImage(Map<String, RenderedImage> sourceImages, 
            ImageLayout layout, 
            Map configuration,
            String script,
            String destVarName,
            Rectangle destBounds) {
        
        super(new Vector(sourceImages.values()), layout, configuration, false);
        
        try {
            Jiffle jiffle = new Jiffle();
            jiffle.setScript(script);
            
            Map<String, Jiffle.ImageRole> imageParams = CollectionFactory.map();
            for (String varName : sourceImages.keySet()) {
                imageParams.put(varName, Jiffle.ImageRole.SOURCE);
            }
            imageParams.put(destVarName, Jiffle.ImageRole.DEST);
            
            jiffle.setImageParams(imageParams);
            jiffle.compile();
            runtime = (JiffleIndirectRuntime) jiffle.getRuntimeInstance(Jiffle.RuntimeModel.INDIRECT);
            
            for (String varName : sourceImages.keySet()) {
                runtime.setSourceImage(varName, sourceImages.get(varName));
            }
            
            if (destBounds == null) {
                bounds = getSourceBounds();
                if (bounds == null) {
                    throw new IllegalArgumentException(
                            "No source images and no destination bounds specified");
                }
            } else {
                bounds = new Rectangle(destBounds);
            }
            
        } catch (JiffleException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    

    /**
     * For testing: returns null to indicate that all of the destination
     * could be affected.
     * 
     * @param sourceRect
     * @param sourceIndex
     * @return 
     */
    @Override
    public Rectangle mapSourceRect(Rectangle sourceRect, int sourceIndex) {
        return null;
    }

    /**
     * For testing: returns the source image bounds.
     * 
     * @param destRect
     * @param sourceIndex
     * @return 
     */
    @Override
    public Rectangle mapDestRect(Rectangle destRect, int sourceIndex) {
        return getSourceImage(sourceIndex).getBounds();
    }

    @Override
    protected void computeRect(PlanarImage[] sources, WritableRaster dest, Rectangle destRect) {
        for (int y = destRect.y, iy = 0; iy < destRect.height; y++, iy++) {
            for (int x = destRect.x, ix = 0; ix < destRect.width; x++, ix++) {
                dest.setSample(x, y, band, runtime.evaluate(x, y));
            }
        }
    }

    @Override
    protected void computeRect(Raster[] sources, WritableRaster dest, Rectangle destRect) {
        super.computeRect(sources, dest, destRect);
    }

    @Override
    public Raster computeTile(int tileX, int tileY) {
        return super.computeTile(tileX, tileY);
    }
    
    

    private Rectangle getSourceBounds() {
        Rectangle r = null;
        
        if (getNumSources() > 0) {
            r = new Rectangle(getSourceImage(0).getBounds());
            
            for (int i = 1; i < getNumSources(); i++) {
                r = r.union(getSourceImage(i).getBounds());
            }
        }
        
        return r;
    }

    
}
