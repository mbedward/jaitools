package org.jaitools.testing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;

import javax.media.jai.JAI;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;
import org.jaitools.swing.ImageFrame;

import org.jaitools.tiledimage.DiskMemImage;

public class RenderSample {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        BufferedImage image = new BufferedImage(600, 600,
                                            BufferedImage.TYPE_INT_RGB);

        SampleModel sampleModel = RasterFactory.createBandedSampleModel(
                DataBuffer.TYPE_BYTE,
                1024, 1024,
                3);
        ColorModel cm = TiledImage.createColorModel(sampleModel);

        /*
        DiskMemImage image = new DiskMemImage(0, 0,
                              600, 600,
                              0, 0,
                              sampleModel, cm);
        */
        
        /*
        TiledImage image = new TiledImage(0, 0, 600, 600, 0, 0, sampleModel, cm);
        */
        
        Graphics2D gr = image.createGraphics();
        gr.setPaint(Color.WHITE);
        gr.fill(new Rectangle(0, 0, 600, 600));

        // AffineTransform[[0.318755336305853, 0.0, 420.03106689453125], [0.0, 0.318755336305853, 245.5029296875]]
        AffineTransform transform = new AffineTransform(
                0.318755336305853, 0.0, 0.0, 0.318755336305853, 420.03106689453125, 245.5029296875);
        gr.setTransform(transform);

        Shape s = new Rectangle(0, 0, 96, 83);

        // create an enbedded graphics
        Graphics2D grr = (Graphics2D) gr.create();
        // AffineTransform[[1.0, 0.0, -343.9285583496093], [0.0, 1.0, -502.5158386230469]]
        grr.clip(s.getBounds());
        transform = new AffineTransform(
                1.0, 0.0, 0.0, 1.0, -343.9285583496093, -502.5158386230469);
        grr.transform(transform);

        AffineTransform t = new AffineTransform(transform);
        t.invert();
        s = t.createTransformedShape(s);

        System.out.println("s bounds: " + s.getBounds());
        System.out.println("grr transform: " + grr.getTransform());
        System.out.println("grr clip: " + grr.getClip());
        System.out.println("grr clip bounds: " + grr.getClipBounds());

        System.out.println("intersects: " +
                s.getBounds().intersects(grr.getClip().getBounds2D()));

        grr.setPaint(Color.BLUE);
        grr.draw(s);

        grr.dispose();

        JAI.create("filestore", image, "/tmp/sample.tif", "TIFF", null);
        ImageFrame.showImage(image, "test image");
    }


}