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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import jaitools.jiffle.JiffleBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for JiffleOpImage
 * 
 * @author Michael Bedward
 * @since 1.2
 * @version $Id$
 */
public class SimpleTest {
    
    private static final double TOL = 1.0e-8;
    
    private static final int WIDTH = 10;
    private Dimension savedTileSize;
    
    @Before
    public void init() {
        savedTileSize = JAI.getDefaultTileSize();
        JAI.setDefaultTileSize(new Dimension(WIDTH / 2, WIDTH / 2));
    }
    
    @After
    public void reset() {
        JAI.setDefaultTileSize(savedTileSize);
    }
    
    @Test
    public void createSequentialImage() throws Exception {
        ParameterBlockJAI pb = new ParameterBlockJAI("Jiffle");
        
        String script = "dest = y() * width() + x();" ;
        
        pb.setParameter("script", script);
        pb.setParameter("destName", "dest");
        
        Rectangle bounds = new Rectangle(0, 0, WIDTH, WIDTH);
        pb.setParameter("destBounds", bounds);
        
        RenderedOp op = JAI.create("Jiffle", pb);
        RenderedImage result = op.getRendering();
        
        assertResult(result, script);
    }

    private void assertResult(RenderedImage resultImage, String script) throws Exception {
        JiffleBuilder builder = new JiffleBuilder();
        builder.script(script).dest("dest", WIDTH, WIDTH).run();
        RenderedImage referenceImage = builder.getImage("dest");

        RectIter resultIter = RectIterFactory.create(resultImage, null);
        RectIter referenceIter = RectIterFactory.create(referenceImage, null);
        
        do {
            do {
                assertEquals(resultIter.getSample(), referenceIter.getSample());
                resultIter.nextPixelDone();
            } while (!referenceIter.nextPixelDone());
            
            resultIter.startPixels();
            resultIter.nextLineDone();
            referenceIter.startPixels();
            
        } while (!referenceIter.nextLineDone());
    }
}
