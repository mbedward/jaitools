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
package jaitools.tiledimage;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for basic DiskMemImageGraphics methods.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class ImageGraphicsGeneralTest extends TiledImageTestBase {

    private final int TILE_WIDTH = 128;
    private final int XTILES = 2;
    private final int YTILES = 2;

    private long tileMemSize;
    private DiskMemImage image;
    private Graphics2D gr;

    @Before
    public void setUp() {
        image = makeImage(TILE_WIDTH, XTILES, YTILES);
        tileMemSize = image.getTileMemorySize();
        gr = image.createGraphics();
    }

    
    @Test
    public void setAndGetHint() {
        System.out.println("   set and get a rendering hint");
        
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        assertEquals(RenderingHints.VALUE_ANTIALIAS_ON, gr.getRenderingHint(RenderingHints.KEY_ANTIALIASING));
        
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        assertEquals(RenderingHints.VALUE_ANTIALIAS_OFF, gr.getRenderingHint(RenderingHints.KEY_ANTIALIASING));
    }

    @Test
    public void addHints() {
        System.out.println("   add rendering hints");
        
        // set an initial hint that will not be over-written
        gr.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        // set a hint that will be over-written
        gr.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);

        // set new hints
        RenderingHints hints = new RenderingHints(null);
        hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        gr.addRenderingHints(hints);

        // check hints
        assertEquals(gr.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL), RenderingHints.VALUE_STROKE_PURE);
        assertEquals(gr.getRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION), RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        assertEquals(gr.getRenderingHint(RenderingHints.KEY_COLOR_RENDERING), RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    }
        
    @Test
    public void setHints() {
        System.out.println("   set and get rendering hints");
        
        // set initial hints that will be over-written
        gr.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        gr.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);

        // set new hints
        RenderingHints newHints = new RenderingHints(null);
        newHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        newHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        gr.setRenderingHints(newHints);
        
        RenderingHints returnedHints = gr.getRenderingHints();
        assertTrue(returnedHints.equals(newHints));
    }
        
}
