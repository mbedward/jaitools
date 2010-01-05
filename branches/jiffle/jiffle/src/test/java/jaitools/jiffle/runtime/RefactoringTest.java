/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.runtime;

import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
import jaitools.tiledimage.DiskMemImage;
import java.awt.Rectangle;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Michael Bedward
 */
public class RefactoringTest {

    private static final int OUT_WIDTH = 1000;
    private static final int OUT_HEIGHT = 500;
    private static final int TILE_WIDTH = 128;

    @Test
    public void newRunnerDesign() throws Exception {
        Map<String, RenderedImage> params = CollectionFactory.newMap();

        SampleModel sm = new ComponentSampleModel(
                DataBuffer.TYPE_DOUBLE, TILE_WIDTH, TILE_WIDTH, 1, TILE_WIDTH, new int[] {0});

        DiskMemImage outImg = new DiskMemImage(OUT_WIDTH, OUT_HEIGHT, sm);
        params.put("out", outImg);

        String script = String.format("out = floor(y() / %d) * %d + floor(x() / %d);",
                TILE_WIDTH, OUT_WIDTH / TILE_WIDTH, TILE_WIDTH);

        Jiffle jiffle = new Jiffle(script, params);
        IJiffleRunner runner = new JiffleRunner2(jiffle);

        runner.run(1, 0);
        Raster tile = outImg.getTile(1, 0);
        Rectangle bounds = tile.getBounds();
        assertEquals(1, tile.getSample(bounds.x, bounds.y, 0));
    }
}
