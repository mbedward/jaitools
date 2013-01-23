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

    public static void main(String[] args) {
        GenerateDemo me = new GenerateDemo();
        me.uniformRandom();
        me.rgbGradient();
    }
    
    private void rgbGradient() {
        Generator diagonalGradient = new Generator() {
            public ImageDataType getDataType() {
                return ImageDataType.BYTE;
            }
            
            public Number getValue(int imageX, int imageY) {
                return (imageX + imageY) % 256;
            }
        };
        
        class Constant implements Generator {
            private final byte value;
            
            Constant(byte value) {
                this.value = value;
            }
            
            public ImageDataType getDataType() {
                return ImageDataType.BYTE;
            }
            
            public Number getValue(int imageX, int imageY) {
                return value;
            }
        }
        
        Constant const128 = new Constant((byte) 128);
        
        ParameterBlockJAI pb = new ParameterBlockJAI("generate");
        pb.setParameter("width", 300);
        pb.setParameter("height", 300);
        
        Generator[] generators = {const128, const128, diagonalGradient};
        
        pb.setParameter("generators", generators);
        
        RenderedOp image = JAI.create("generate", pb);
        ImageFrame.showImage(image.createInstance(), "RGB Diagonal gradient");
    }
    
    private void uniformRandom() {
        class UniformRandomGenerator implements Generator {
            private final Random rand = new Random();
            
            public ImageDataType getDataType() {
                return ImageDataType.DOUBLE;
            }

            public Number getValue(int imageX, int imageY) {
                return rand.nextDouble();
            }
        }

        ParameterBlockJAI pb = new ParameterBlockJAI("generate");
        pb.setParameter("width", 300);
        pb.setParameter("height", 300);
        
        pb.setParameter("generators", new UniformRandomGenerator());
        
        RenderedOp image = JAI.create("generate", pb);
        ImageFrame.showImage(image.createInstance(), "Single band uniform random");
    }
}
