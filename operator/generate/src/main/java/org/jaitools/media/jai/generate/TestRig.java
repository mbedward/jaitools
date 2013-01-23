package org.jaitools.media.jai.generate;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import org.jaitools.imageutils.ImageDataType;
import org.jaitools.swing.ImageFrame;

/**
 * Temporary class for testing.
 *
 * @author michael
 */
public class TestRig {

    public static void main(String[] args) {
        new TestRig().demo();
    }
    
    private void demo() {
        Generator rg = new Generator() {

            public ImageDataType getDataType() {
                return ImageDataType.BYTE;
            }

            public Number getValue(int imageX, int imageY) {
                return (imageX + imageY) % 256;
            }

        };
        
        
        ParameterBlockJAI pb = new ParameterBlockJAI("generate");
        pb.setParameter("width", 300);
        pb.setParameter("height", 300);
        pb.setParameter("generators", new Generator[] {rg, rg, rg});
        
        RenderedOp image = JAI.create("generate", pb);
        ImageFrame.showImage(image.createInstance(), "generate");
    }
}
