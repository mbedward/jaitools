package org.jaitools.demo.generate;

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
        new GenerateDemo().demo();
    }
    
    private void demo() {
        
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
                return 128;
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
}
