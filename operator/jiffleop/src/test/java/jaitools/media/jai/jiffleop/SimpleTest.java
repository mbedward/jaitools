/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jaitools.media.jai.jiffleop;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.JiffleBuilder;
import jaitools.jiffle.runtime.JiffleDirectRuntime;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class SimpleTest {
    
    private static final double TOL = 1.0e-8;
    
    private static final int WIDTH = 10;
    
    @Test
    public void createSequentialImage() throws Exception {
        ParameterBlockJAI pb = new ParameterBlockJAI("Jiffle");
        
        String script = "init { n = 0; } dest = n++ ;" ;
        
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
