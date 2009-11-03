/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.imageutils;

import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RectIterFactory;
import javax.media.jai.iterator.WritableRectIter;

/**
 * @author Michael Bedward
 */
public class Foo {

    private static final int SOURCE_WIDTH = 100;
    private static final int BUFFER_WIDTH = 2;
    private static final int SAMPLE_DISTANCE = 5;

    public static void main(String[] args) {
        TiledImage sourceImage = ImageUtils.createDoubleImage(SOURCE_WIDTH, SOURCE_WIDTH);
        WritableRectIter iter = RectIterFactory.createWritable(sourceImage, null);
        double x = 0d;
        double y = 0d;
        do {
            do {
                iter.setSample(x * y);
                x++ ;
            } while (!iter.nextPixelDone());

            iter.startPixels();
            x = 0d;
            y++ ;

        } while (!iter.nextLineDone());

        System.out.println("   image type BYTE");

        WritableRaster raster = createRaster(DataBuffer.TYPE_BYTE);

        SamplingBorderExtender ex = new SamplingBorderExtender(SAMPLE_DISTANCE);
        ex.extend(raster, sourceImage);
    }

   private static WritableRaster createRaster(int dataType) {
        return RasterFactory.createBandedRaster(
                dataType,
                SOURCE_WIDTH + 2 * BUFFER_WIDTH,
                SOURCE_WIDTH + 2 * BUFFER_WIDTH,
                1, new Point(-BUFFER_WIDTH, -BUFFER_WIDTH));
    }
}
