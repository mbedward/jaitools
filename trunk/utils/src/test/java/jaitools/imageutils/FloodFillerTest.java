/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.imageutils;

import jaitools.tiledimage.DiskMemImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import javax.media.jai.PlanarImage;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Bedward
 */
public class FloodFillerTest {

    private static final int TILE_WIDTH = 128;
    private static final int IMAGE_WIDTH = 2 * TILE_WIDTH;


    public FloodFillerTest() {
    }

    /**
     * Test of fill method, of class FloodFiller.
     */
    @Test
    public void testFill() {
        System.out.println("   flood fill with single image");

        SampleModel sm = new ComponentSampleModel(
                DataBuffer.TYPE_BYTE,
                TILE_WIDTH,
                TILE_WIDTH,
                1, TILE_WIDTH,  // pixel stride and scan-line stride
                new int[]{0});  // band offset

        ColorModel cm = PlanarImage.createColorModel(sm);

        DiskMemImage image = new DiskMemImage( IMAGE_WIDTH, IMAGE_WIDTH, sm, cm );
        Graphics2D gr = image.createGraphics();
        gr.setBackground(Color.WHITE);
        gr.clearRect(0, 0, IMAGE_WIDTH, IMAGE_WIDTH);

        gr.setColor(Color.BLACK);

        // two rectangles meeting diagonally
        gr.fillRect(TILE_WIDTH/2, TILE_WIDTH/2, TILE_WIDTH/2, TILE_WIDTH/2);
        gr.fillRect(TILE_WIDTH, TILE_WIDTH, TILE_WIDTH/2, TILE_WIDTH/2);

        // diagonal fill into first rectangle should fill
        // second rectangle as well
        boolean diagonal = true;
        int fillValue = 64;
        FloodFiller filler = new FloodFiller(image, image, 0, 0, diagonal);
        FillResult fill = filler.fill(TILE_WIDTH / 2, TILE_WIDTH / 2, fillValue);
        assertTrue(fill.getNumPixels() == 2 * (TILE_WIDTH/2) * (TILE_WIDTH/2));

        // orthogonal fill into first rectangle should not fill
        // second rectangle
        diagonal = false;
        fillValue = 128;
        filler = new FloodFiller(image, image, 0, 0, diagonal);
        fill = filler.fill(TILE_WIDTH / 2, TILE_WIDTH / 2, fillValue);
        assertTrue(fill.getNumPixels() == (TILE_WIDTH/2) * (TILE_WIDTH/2));
    }

}