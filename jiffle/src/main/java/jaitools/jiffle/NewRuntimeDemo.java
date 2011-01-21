package jaitools.jiffle;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.runtime.JiffleEvent;
import jaitools.jiffle.runtime.JiffleEventListener;
import jaitools.jiffle.runtime.JiffleExecutor;
import jaitools.jiffle.runtime.JiffleExecutorResult;
import jaitools.jiffle.runtime.NullProgressListener;
import jaitools.swing.ImageFrame;
import java.awt.image.RenderedImage;

import java.util.Map;
import javax.swing.SwingUtilities;

public class NewRuntimeDemo implements JiffleEventListener {

    private JiffleExecutor executor;
    
    public static void main(String[] args) throws Exception {
        NewRuntimeDemo me = new NewRuntimeDemo();
        me.executorDemo();
    }
    
    public NewRuntimeDemo() {
        executor = new JiffleExecutor();
    }
    
    private void executorDemo() throws Exception {
        Map<String, Jiffle.ImageRole> params = CollectionFactory.map();
        params.put("out", Jiffle.ImageRole.DEST);
        
        // script from the ripple demo
        String src = 
                  "xc = width() / 2; \n"
                + "yc = height() / 2; \n"
                + "dx = (x()-xc)/xc; \n"
                + "dy = (y()-yc)/yc; \n"
                + "d = sqrt(dx^2 + dy^2); \n"
                + "out = sin(8 * M_PI * d); \n";

        Jiffle jiffle = new Jiffle(src, params);
        
        Map<String, RenderedImage> images = CollectionFactory.map();
        images.put("out", ImageUtils.createConstantImage(500, 500, Double.valueOf(0)));

        if (!executor.isListening(this)) {
            executor.addEventListener(this);
        }
        executor.submit(jiffle, images, new NullProgressListener());
    }

    public void onCompletionEvent(JiffleEvent ev) {
        JiffleExecutorResult result = ev.getResult();
        System.out.println( result.getJiffle().getRuntimeSource(true) );
        
        final RenderedImage img = result.getImages().get("out");
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ImageFrame f = new ImageFrame(img, "test image");
                int w = img.getWidth();
                f.setSize(w+50, w+50);
                f.setVisible(true);
            }
        });
    }

    public void onFailureEvent(JiffleEvent ev) {
        System.out.println("Execution failed");
    }

}
