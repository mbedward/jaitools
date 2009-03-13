/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jaitools.demo.kernelstats;

import jaitools.demo.utils.ImageFrame;
import jaitools.demo.utils.ProgressMeter;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.runtime.JiffleCompletionEvent;
import jaitools.jiffle.runtime.JiffleEventAdapter;
import jaitools.jiffle.runtime.JiffleInterpreter;
import jaitools.jiffle.runtime.JiffleProgressEvent;
import jaitools.media.jai.kernel.KernelFactory;
import jaitools.media.jai.kernelstats.KernelStatistic;
import jaitools.utils.CollectionFactory;
import jaitools.utils.ImageUtils;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.media.jai.BorderExtender;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.TiledImage;
import javax.swing.SwingUtilities;

public class KernelStatsDemo {

    private JiffleInterpreter interp;
    private ProgressMeter progMeter;
    private int baseImageJobID = -1;
    private int statsJobID = -1;
    private static final String INPUT_IMAGE = "img";

    public static void main(String[] args) {
        KernelStatsDemo me = new KernelStatsDemo();
        me.demo();
    }

    private KernelStatsDemo() {
        progMeter = new ProgressMeter();

        interp = new JiffleInterpreter();
        interp.addEventListener(new JiffleEventAdapter() {

            @Override
            public void onCompletionEvent(JiffleCompletionEvent ev) {
                onCompletion(ev);
            }

            @Override
            public void onProgressEvent(JiffleProgressEvent ev) {
                showProgress(ev);
            }
        });
    }

    private void demo() {

        /*
         * create an input image (with Jiffle of course !)
         * showing an interference pattern between two
         * sine functions
         */
        String script =
                "dx = x() / width();" +
                "dy = y() / height();" +
                "dxy = sqrt((dx-0.5)^2 + (dy-0.5)^2);" +
                INPUT_IMAGE + " = sin(dx * 100) + sin(dxy * 100);";

        TiledImage img = ImageUtils.createDoubleImage(400, 400);

        Map<String, TiledImage> imgParams = CollectionFactory.newMap();
        imgParams.put(INPUT_IMAGE, img);

        try {
            progMeter.setTitle("Generating base image");
            progMeter.setVisible(true);
            Jiffle jiffle = new Jiffle(script, imgParams);
            baseImageJobID = interp.submit(jiffle);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void onCompletion(JiffleCompletionEvent ev) {
        progMeter.setVisible(false);
        ImageFrame frame;
        RenderedImage img = ev.getJiffle().getImage(INPUT_IMAGE);

        frame = new ImageFrame();
        frame.displayImage(img, "KernelStats demo");

        calculateStats(img);
    }

    private void showProgress(final JiffleProgressEvent ev) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                progMeter.update(ev.getProgress());
            }
        });
    }

    private void calculateStats(RenderedImage img) {
        /**
         * now generate summary statistics for the base image
         */
        KernelJAI kernel = KernelFactory.createCircle(10);
        ParameterBlockJAI pb = new ParameterBlockJAI("KernelStats");

        pb.setSource("source0", img);
        pb.setParameter("kernel", kernel);
        KernelStatistic[] stats = new KernelStatistic[]
        {
            KernelStatistic.MEAN,
            KernelStatistic.SDEV,
        };

        pb.setParameter("stats", stats);

        BorderExtender extender = BorderExtender.createInstance(BorderExtender.BORDER_REFLECT);
        RenderingHints hints = new RenderingHints(JAI.KEY_BORDER_EXTENDER, extender);

        Collection<RenderedImage> statsImages = JAI.createCollection("KernelStats", pb, hints);
        Iterator<RenderedImage> iter = statsImages.iterator();

        int k = 0;
        while (iter.hasNext()) {
            ImageFrame frame = new ImageFrame();
            frame.displayImage(iter.next(), "Neighbourhood statistic: " + stats[k++].toString());
        }

    }
}
