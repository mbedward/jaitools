package jaitools.jiffle;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.runtime.JiffleRuntime;
import jaitools.swing.ImageFrame;

import java.util.Map;
import javax.media.jai.TiledImage;
import javax.swing.SwingUtilities;

public class NewRuntimeDemo {
    
    public static void main(String[] args) throws Exception {
        final TiledImage img = ImageUtils.createConstantImage(500, 500, Double.valueOf(0));
        
        Map<String, Jiffle.ImageRole> params = CollectionFactory.map();
        params.put("out", Jiffle.ImageRole.DEST);
        
        // script from the ripple demo
        String src = 
                  "xc = width() / 2; \n"
                + "yc = height() / 2; \n"
                + "dx = (x()-xc)/xc; \n"
                + "dy = (y()-yc)/yc; \n"
                + "d = sqrt(dx^2 + dy^2); \n"
                + "out = sin(8 * PI * d); \n";

        Jiffle jiffle = new Jiffle(src, params);
        
        JiffleRuntime jr = jiffle.getRuntimeInstance();
        jr.setDestinationImage("out", img);
        jr.evaluateAll();
        
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
