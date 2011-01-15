/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle;

import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.runtime.JiffleRuntime;
import jaitools.swing.ImageFrame;

import java.awt.image.RenderedImage;
import java.util.HashMap;
import java.util.Map;
import javax.media.jai.TiledImage;
import javax.swing.SwingUtilities;

/**
 *
 * @author michael
 */
public class NewRuntimeDemo {
    
    public static void main(String[] args) throws Exception {
        final TiledImage img = ImageUtils.createConstantImage(500, 500, Double.valueOf(0));
        
        Map<String, RenderedImage> params = new HashMap<String, RenderedImage>();
        params.put("out", img);
        
        // script from the ripple demo
        String src = 
                  "xc = max(0, width() / 2); \n"
                + "yc = height() / 2; \n"
                + "dx = (x()-xc)/xc; \n"
                + "dy = (y()-yc)/yc; \n"
                + "d = sqrt(dx^2 + dy^2); \n"
                + "out = sin(8 * PI * d); \n";

        Jiffle jiffle = new Jiffle(src, params);
        
        JiffleRuntime jr = jiffle.getRuntimeInstance();
        jr.setDestinationImage("out", img);

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                jr.evaluate(x, y, 0);
            }
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ImageFrame f = new ImageFrame(img, "test image");
                int w = img.getWidth();
                f.setSize(w+50, w+50);
                f.setVisible(true);
            }
        });
        
        System.out.println("Input Jiffle script:");
        System.out.println(src);
        System.out.println();
        
        System.out.println("Translated into Java source:");
        System.out.println( jiffle.getRuntimeSource() );
    }

}
