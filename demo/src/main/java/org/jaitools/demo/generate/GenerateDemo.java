package org.jaitools.demo.generate;

import java.awt.Color;
import java.util.Random;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import org.jaitools.imageutils.ImageDataType;
import org.jaitools.media.jai.generate.Generator;
import org.jaitools.swing.ImageFrame;

/**
 * Demonstrates the Generate operation.
 *
 * @author michael
 */
public class GenerateDemo {
    
    private static int IMAGE_SIDE_LEN = 300;

    public static void main(String[] args) {
        GenerateDemo me = new GenerateDemo();
        me.uniformRandom();
        me.rgbGradient();
        me.fractal();
    }

    private void rgbGradient() {
        ParameterBlockJAI pb = new ParameterBlockJAI("generate");
        pb.setParameter("width", IMAGE_SIDE_LEN);
        pb.setParameter("height", IMAGE_SIDE_LEN);
        pb.setParameter("generator", new DiagonalGradientGenerator());

        RenderedOp image = JAI.create("generate", pb);
        ImageFrame.showImage(image.createInstance(), "RGB Diagonal gradient");
    }

    private void uniformRandom() {
        ParameterBlockJAI pb = new ParameterBlockJAI("generate");
        pb.setParameter("width", IMAGE_SIDE_LEN);
        pb.setParameter("height", IMAGE_SIDE_LEN);
        pb.setParameter("generator", new UniformRandomGenerator());

        RenderedOp image = JAI.create("generate", pb);
        ImageFrame.showImage(image.createInstance(), "Single band uniform random");
    }

    private void fractal() {
        ParameterBlockJAI pb = new ParameterBlockJAI("generate");
        pb.setParameter("width", IMAGE_SIDE_LEN);
        pb.setParameter("height", IMAGE_SIDE_LEN);
        pb.setParameter("generator", 
                new MandelbrotGenerator(IMAGE_SIDE_LEN, IMAGE_SIDE_LEN));

        RenderedOp image = JAI.create("generate", pb);
        ImageFrame.showImage(image.createInstance(), "Fractal");
    }

    /**************************************************************************
     *
     * Generator classes used by the above methods
     *
     **************************************************************************/
    
    /*
     * Returns values for RGB bands where the red and green components
     * are constants and the blue component varies along a diagonal
     * gradient.
     */
    static class DiagonalGradientGenerator implements Generator {

        public ImageDataType getDataType() {
            return ImageDataType.BYTE;
        }
        
        public int getNumBands() {
            return 3;
        }

        public Number[] getValues(int imageX, int imageY) {
            byte blue = (byte) ((imageX + imageY) % 256);
            byte c = (byte) 128;
            return new Number[] {c, c, blue};
        }
    }
    
    /*
     * Generates a uniform random value in the unit interval
     */
    static class UniformRandomGenerator implements Generator {

        private final Random rand = new Random();

        public ImageDataType getDataType() {
            return ImageDataType.DOUBLE;
        }

        public int getNumBands() {
            return 1;
        }

        public Number[] getValues(int imageX, int imageY) {
            return new Number[] {rand.nextDouble()};
        }
    }
 
    /*
     * Generates a binary image of the Mandelbrot fractal
     */
    class MandelbrotGenerator implements Generator {

        private static final double XMIN = -2;
        private static final double XMAX = 1;
        private static final double YMIN = -1.5;
        private static final double YMAX = 1.5;
        private static final int MAX_ITERATIONS = 32;
        private final int width;
        private final int height;

        public MandelbrotGenerator(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public ImageDataType getDataType() {
            return ImageDataType.BYTE;
        }

        public int getNumBands() {
            return 3;
        }

        public Number[] getValues(int imageX, int imageY) {
            double a = XMIN + imageX * (XMAX - XMIN) / width;
            double b = YMIN + imageY * (YMAX - YMIN) / height;

            Color c = getColor(a, b);
            return new Number[] {c.getRed(), c.getGreen(), c.getBlue()};
        }

        /*
         * A very quick and dirty Mandelbrot algorithm
         */
        private Color getColor(double a, double b) {
            double x = 0.0;
            double y = 0.0;
            int numIter = 0;

            while (numIter < MAX_ITERATIONS && x <= 2 && y <= 2) {
                double xx = x * x - y * y + a;
                double yy = 2 * x * y + b;
                x = xx;
                y = yy;
                numIter++ ;
            }

            if (numIter == MAX_ITERATIONS) {
                return Color.BLACK;
            } else {
                float hue = (float) numIter / MAX_ITERATIONS;
                return Color.getHSBColor(hue, 0.6f, 0.8f);
            }
        }
    }
}
