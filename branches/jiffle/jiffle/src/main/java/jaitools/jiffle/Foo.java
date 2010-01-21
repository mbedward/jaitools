/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.runtime.JiffleRunner;
import java.awt.image.RenderedImage;
import java.util.Map;

/**
 * @author Michael Bedward
 */
public class Foo {

    public static void main(String[] args) throws Exception {
        Map<String, RenderedImage> params = CollectionFactory.newMap();
        RenderedImage img = ImageUtils.createConstantImage(10, 10, new Double[]{0d});
        params.put("out", img);

        Jiffle jiffle = new Jiffle("out = y()", params);
        JiffleRunner runner = new JiffleRunner(jiffle);
        runner.run();
    }
}
