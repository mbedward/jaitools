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
package jaitools.jiffle.runtime;

import java.util.Map;
import javax.media.jai.TiledImage;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.Jiffle;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the evaluation of simple statements with a single source
 * and destination image.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class SimpleStatementsTest {
    
    private static final int WIDTH = 10;
    private static final double TOL = 1.0e-8;
    
    private Map<String, Jiffle.ImageRole> imageParams;

    interface Evaluator {
        double eval(double val);
    }
    
    @Before
    public void setup() {
        imageParams = CollectionFactory.map();
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        imageParams.put("src", Jiffle.ImageRole.SOURCE);
    }
    
    @Test
    public void srcValue() throws Exception {
        String src = "dest = src;";
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return val;
                    }
                });
    }

    @Test
    public void minusSrcValue() throws Exception {
        String src = "dest = -src;";
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return -val;
                    }
                });
    }

    @Test
    public void add() throws Exception {
        String src = "dest = src + 1;";
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return val + 1;
                    }
                });
    }
    
    @Test
    public void subtract() throws Exception {
        String src = "dest = src - 1;";
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return val - 1;
                    }
                });
    }

    @Test
    public void subtract2() throws Exception {
        String src = "dest = 1 - src;";
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return 1 - val;
                    }
                });
    }

    @Test
    public void multiply() throws Exception {
        String src = "dest = src * 2;";
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return val * 2;
                    }
                });
    }

    @Test
    public void divide() throws Exception {
        String src = "dest = src / 2;";
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return val / 2;
                    }
                });
    }

    /**
     * This will also test handling division by 0
     */
    @Test
    public void inverseDivide() throws Exception {
        String src = "dest = 1 / src;";
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return 1 / val;
                    }
                });
    }

    @Test
    public void modulo() throws Exception {
        String src = "dest = src % 11;";
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return val % 11;
                    }
                });
    }
    
    @Test
    public void power() throws Exception {
        String src = "dest = src^2;";
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return val * val;
                    }
                });
    }
    
    @Test
    public void precedence1() throws Exception {
        String src = "dest = 1 + src * 2;";
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return 1 + val * 2;
                    }
                });
    }
    
    @Test
    public void precedence2() throws Exception {
        String src = "dest = (1 + src) * 2;";
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return (1 + val) * 2;
                    }
                });
    }
    
    @Test
    public void precedence3() throws Exception {
        String src = "dest = 1 - src * 2;";
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return 1 - val * 2;
                    }
                });
    }
    
    @Test
    public void precedence4() throws Exception {
        String src = "dest = (1 - src) * 2;";
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return (1 - val) * 2;
                    }
                });
    }
    
    @Test
    public void precedence5() throws Exception {
        String src = "dest = src + 2 * 3 / 4 + 5;";
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return val + 2 * 3.0 / 4.0 + 5;
                    }
                });
    }
    

    private void testScript(String script, Evaluator evaluator) throws Exception {
        Jiffle jiffle = new Jiffle(script, imageParams);
        JiffleRuntime jr = jiffle.getRuntimeInstance();
        
        TiledImage srcImg = createSequenceImage();
        TiledImage destImg = ImageUtils.createConstantImage(WIDTH, WIDTH, 0d);
        
        jr.setSourceImage("src", srcImg);
        jr.setDestinationImage("dest", destImg);
        jr.evaluateAll();
        
        for (int y = 0; y < WIDTH; y++) {
            for (int x = 0; x < WIDTH; x++) {
                assertEquals(evaluator.eval(srcImg.getSampleDouble(x, y, 0)), 
                        destImg.getSampleDouble(x, y, 0), TOL);
            }
        }
    }
    
        
    private TiledImage createSequenceImage() {
        TiledImage img = ImageUtils.createConstantImage(WIDTH, WIDTH, 0d);
        int k = 0;
        for (int y = 0; y < WIDTH; y++) {
            for (int x = 0; x < WIDTH; x++) {
                img.setSample(x, y, 0, k++);
            }
        }
        return img;
    }
}
