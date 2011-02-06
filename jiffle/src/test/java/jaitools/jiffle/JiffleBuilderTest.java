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

import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

import static org.junit.Assert.*;
import org.junit.Test;

import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.runtime.JiffleDirectRuntime;
import jaitools.jiffle.runtime.StatementsTestBase;

/**
 * Unit tests for the JiffleBuilder helper class.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class JiffleBuilderTest extends StatementsTestBase {

    @Test
    public void runBasicScript() throws Exception {
        System.out.println("   basic script with provided dest image");
        String script = "dest = if (src1 > 10, src1, NULL);" ;

        TiledImage srcImg1 = createSequenceImage();
        TiledImage destImg = ImageUtils.createConstantImage(WIDTH, WIDTH, 0d);

        JiffleBuilder jb = new JiffleBuilder();
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

        JiffleBuilder jb = new JiffleBuilder();
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

        JiffleBuilder jb = new JiffleBuilder();
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

        JiffleBuilder jb = new JiffleBuilder();
        int w = 10;
        int h = 20;
        jb.dest("dest", new Rectangle(0, 0, w, h));
        RenderedImage img = jb.getImage("dest");
        assertNotNull(img);
        
        jb.clear();
        img = jb.getImage("dest");
        assertNull(img);
    }
}
