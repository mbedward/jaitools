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

package jaitools.jiffle;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.net.URL;

import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.runtime.JiffleDirectRuntime;
import jaitools.jiffle.runtime.StatementsTestBase;
import java.io.File;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the JiffleBuilder helper class.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class JiffleBuilderTest extends StatementsTestBase {
    
    private JiffleBuilder jb;
    
    @Before
    public void setup() {
        jb = new JiffleBuilder();
    }

    @Test
    public void runBasicScript() throws Exception {
        System.out.println("   basic script with provided dest image");
        String script = "dest = con(src1 > 10, src1, null);" ;

        TiledImage srcImg1 = createSequenceImage();
        TiledImage destImg = ImageUtils.createConstantImage(WIDTH, WIDTH, 0d);

        jb.script(script).source("src1", srcImg1).dest("dest", destImg);
        JiffleDirectRuntime runtime = jb.getRuntime();
        runtime.evaluateAll(null);

        Evaluator e = new Evaluator() {

            public double eval(double val) {
                return val > 10 ? val : Double.NaN;
            }
        };

        assertImage(srcImg1, destImg, e);
    }
    
    @Test
    public void builderCreatesDestImage() throws Exception {
        System.out.println("   builder creating dest image");
        String script = "init { n = 0; } dest = n++ ;" ;

        jb.dest("dest", WIDTH, WIDTH).script(script).getRuntime().evaluateAll(null);
        RenderedImage img = jb.getImage("dest");

        assertNotNull(img);
        
        RandomIter iter = RandomIterFactory.create(img, null);
        int k = 0;
        for (int y = 0; y < WIDTH; y++) {
            for (int x = 0; x < WIDTH; x++) {
                assertEquals(k, iter.getSample(x, y, 0));
                k++ ;
            }
        }
    }

    @Test
    public void destImageWithRect() throws Exception {
        System.out.println("   dest image from Rectangle bounds");

        int w = 10;
        int h = 20;
        jb.dest("dest", new Rectangle(0, 0, w, h));
        RenderedImage img = jb.getImage("dest");
        assertNotNull(img);
        assertEquals(w, img.getWidth());
        assertEquals(h, img.getHeight());
    }

    @Test
    public void clearingDestImage() throws Exception {
        System.out.println("   clear builder dest image");

        int w = 10;
        int h = 20;
        jb.dest("dest", new Rectangle(0, 0, w, h));
        RenderedImage img = jb.getImage("dest");
        assertNotNull(img);
        
        jb.clear();
        img = jb.getImage("dest");
        assertNull(img);
    }
    
    @Test
    public void runMethod() throws Exception {
        System.out.println("   using run() method");
        String script = "dest = x();";
        
        jb.script(script).dest("dest", 10, 10).run();
        RenderedImage img = jb.getImage("dest");
        assertNotNull(img);
        
        Evaluator e = new Evaluator() {
            int x = 0;
            public double eval(double val) {
                int xx = x;
                x = (x + 1) % WIDTH;
                return xx;
            }
        };
        
        assertImage(null, img, e);
    }

    @Test
    public void scriptFile() throws Exception {
        System.out.println("   loading script file");
        URL url = JiffleBuilderTest.class.getResource("constant.jfl");
        File scriptFile = new File(url.toURI());
        
        jb.script(scriptFile).dest("dest", 10, 10).run();
        
        // no checking of dest image - just as long as we didn't
        // get an exception we are happy
    }
    
    @Test
    public void removeImage() throws Exception {
        System.out.println("   remove image");
        String script = "dest = 42;" ;
        
        jb.script(script).dest("dest", 10, 10).run();
        RenderedImage image = jb.removeImage("dest");
        assertNotNull(image);
        
        image = jb.getImage("dest");
        assertNull(image);
    }
    
}
