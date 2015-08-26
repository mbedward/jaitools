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
package org.jaitools.tiledimage;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

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

    private DiskMemImage image;
    private Graphics2D gr;

    @Before
    public void setUp() {
        image = makeImage(TILE_WIDTH, XTILES, YTILES);
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
     
    @Test
    public void nullFontIsSilentlyIgnored() {
        System.out.println("   null arg to setFont is silently ignored");
        
        gr.setFont(null);
        assertNotNull(gr.getFont());
    }
    
    @Test
    public void testTransformationCopyOnCreation() {        
        //ensure base behaviour
        assertNotNull(gr.getTransform());
        assertEquals(1.0,gr.getTransform().getScaleX(), 0.0001);
        gr.scale(2, 2);
        assertEquals(2.0,gr.getTransform().getScaleX(), 0.0001);
        assertTrue(gr instanceof DiskMemImageGraphics);
        
        Graphics2D subgr = (Graphics2D) gr.create();
        assertEquals(2, subgr.getTransform().getScaleX(), 0.0001);
        subgr.scale(3, 3);
        assertEquals(6, subgr.getTransform().getScaleX(), 0.0001);
        
        //but the original transformation is not touched
        assertEquals(2.0,gr.getTransform().getScaleX(), 0.0001);
    }
}
