package jaitools.jiffle;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.runtime.JiffleEvent;
import jaitools.jiffle.runtime.JiffleEventListener;
import jaitools.jiffle.runtime.JiffleExecutor;
import jaitools.jiffle.runtime.JiffleExecutorResult;
import jaitools.jiffle.runtime.JiffleRuntime;
import jaitools.jiffle.runtime.NullProgressListener;
import jaitools.swing.ImageFrame;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;

import java.util.Map;
import javax.swing.SwingUtilities;

public class NewRuntimeDemo implements JiffleEventListener {
    
    // script from the ripple demo
    private final String script =
            "xc = width() / 2; \n"
            + "yc = height() / 2; \n"
            + "dx = (x()-xc)/xc; \n"
            + "dy = (y()-yc)/yc; \n"
            + "d = sqrt(dx^2 + dy^2); \n"
            + "dest = sin(8 * M_PI * d); \n";

    private JiffleExecutor executor;
    
    public static void main(String[] args) throws Exception {
        NewRuntimeDemo me = new NewRuntimeDemo();
        // me.directRuntimeDemo();
        me.executorDemo();
    }
    
    public NewRuntimeDemo() {
        executor = new JiffleExecutor();
    }
    
    private void directRuntimeDemo() throws Exception {
        Map<String, Jiffle.ImageRole> params = CollectionFactory.map();
        params.put("dest", Jiffle.ImageRole.DEST);
        
        Jiffle jiffle = new Jiffle(script, params);
        
        JiffleRuntime runtime = jiffle.getRuntimeInstance();
        WritableRenderedImage destImg = ImageUtils.createConstantImage(500, 500, 0d);
        runtime.setDestinationImage("dest", destImg);
        runtime.evaluateAll(new NullProgressListener());
        
        displayImage(destImg);
        System.out.println(jiffle.getRuntimeSource(true));
    }
    
    private void executorDemo() throws Exception {
        Map<String, Jiffle.ImageRole> params = CollectionFactory.map();
        params.put("dest", Jiffle.ImageRole.DEST);
        
        Jiffle jiffle = new Jiffle(script, params);
        
        Map<String, RenderedImage> images = CollectionFactory.map();
        images.put("dest", ImageUtils.createConstantImage(500, 500, Double.valueOf(0)));

        if (!executor.isListening(this)) {
            executor.addEventListener(this);
        }
        executor.submit(jiffle, images, new NullProgressListener());
    }

    public void onCompletionEvent(JiffleEvent ev) {
        JiffleExecutorResult result = ev.getResult();
        System.out.println( result.getJiffle().getRuntimeSource(true) );
        
        RenderedImage img = result.getImages().get("dest");
        displayImage(img);
    }

    public void onFailureEvent(JiffleEvent ev) {
        System.out.println("Execution failed");
        System.exit(0);
    }

    private void displayImage(final RenderedImage img) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ImageFrame f = new ImageFrame(img, "New runtime demo");
                int w = img.getWidth();
                f.setSize(w+50, w+50);
                f.setVisible(true);
            }
        });
    }

}
