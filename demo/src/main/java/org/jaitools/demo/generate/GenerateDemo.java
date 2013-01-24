package org.jaitools.demo.generate;

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
        Generator redGen = new ConstantByteGenerator((byte) 128);
        Generator greenGen = redGen;
        Generator blueGen = new DiagonalGradientGenerator();

        ParameterBlockJAI pb = new ParameterBlockJAI("generate");
        pb.setParameter("width", IMAGE_SIDE_LEN);
        pb.setParameter("height", IMAGE_SIDE_LEN);

        Generator[] generators = {redGen, greenGen, blueGen};

        pb.setParameter("generators", generators);

        RenderedOp image = JAI.create("generate", pb);
        ImageFrame.showImage(image.createInstance(), "RGB Diagonal gradient");
    }

    private void uniformRandom() {
        ParameterBlockJAI pb = new ParameterBlockJAI("generate");
        pb.setParameter("width", IMAGE_SIDE_LEN);
        pb.setParameter("height", IMAGE_SIDE_LEN);

        pb.setParameter("generators", new UniformRandomGenerator());

        RenderedOp image = JAI.create("generate", pb);
        ImageFrame.showImage(image.createInstance(), "Single band uniform random");
    }

    private void fractal() {
        ParameterBlockJAI pb = new ParameterBlockJAI("generate");
        pb.setParameter("width", IMAGE_SIDE_LEN);
        pb.setParameter("height", IMAGE_SIDE_LEN);
        
        Generator gen = new MandelbrotGenerator(IMAGE_SIDE_LEN, IMAGE_SIDE_LEN);
        pb.setParameter("generators", gen);

        RenderedOp image = JAI.create("generate", pb);
        ImageFrame.showImage(image.createInstance(), "Fractal");
    }

    /**************************************************************************
     *
     * Generator classes used by the above methods
     *
     **************************************************************************/
    
    /*
     * Generates a constant byte value
     */
    static class ConstantByteGenerator implements Generator {

        private final byte value;

        ConstantByteGenerator(byte value) {
            this.value = value;
        }

        public ImageDataType getDataType() {
            return ImageDataType.BYTE;
        }

        public Number getValue(int imageX, int imageY) {
            return value;
        }
    }
    
    /*
     * Returns byte values on a diagonal gradient.
     */
    static class DiagonalGradientGenerator implements Generator {

        public ImageDataType getDataType() {
            return ImageDataType.BYTE;
        }

        public Number getValue(int imageX, int imageY) {
            return (imageX + imageY) % 256;
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

        public Number getValue(int imageX, int imageY) {
            return rand.nextDouble();
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
        private static final int MAX_ITERATIONS = 16;
        private final int width;
        private final int height;

        public MandelbrotGenerator(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public ImageDataType getDataType() {
            return ImageDataType.BYTE;
        }

        public Number getValue(int imageX, int imageY) {
            double a = XMIN + imageX * (XMAX - XMIN) / width;
            double b = YMIN + imageY * (YMAX - YMIN) / height;
            return escapes(a, b) ? 255 : 0;
        }

        private boolean escapes(double a, double b) {
            double x = 0.0;
            double y = 0.0;
            int numIter = 0;

            do {
                double xx = x * x - y * y + a;
                double yy = 2 * x * y + b;
                x = xx;
                y = yy;
                if (++numIter == MAX_ITERATIONS) {
                    return false;
                }
            } while (x <= 2 && y <= 2);

            return true;
        }
    }
}
