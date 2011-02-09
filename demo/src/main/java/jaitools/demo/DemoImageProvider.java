/*
 * Copyright 2009 Michael Bedward
 *
 * This file is part of jai-tools.
 *
 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jaitools.demo;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.runtime.JiffleEvent;
import jaitools.jiffle.runtime.JiffleEventListener;
import jaitools.jiffle.runtime.JiffleExecutor;
import jaitools.jiffle.runtime.JiffleExecutorResult;
import jaitools.jiffle.runtime.JiffleProgressListener;
import jaitools.swing.ProgressMeter;
import java.awt.image.RenderedImage;
import java.io.File;
import java.net.URL;
import java.util.Map;
import javax.swing.SwingUtilities;

/**
 * A utility class to build demo images from Jiffle scripts.
 * We use this class as a singleton.
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class DemoImageProvider {

    public static final int CHESSBOARD = 0;
    public static final int INTERFERENCE = 1;
    public static final int RIPPLES = 2;
    public static final int SQUIRCLE = 3;

    private static final String[] scriptName = {
        "chessboard",
        "interference",
        "ripple",
        "squircle"
    };

    /* single instance of this class */
    private static DemoImageProvider instance;

    private JiffleExecutor executor;

    private static class JobListener implements JiffleProgressListener {
        private final static long updateInterval = 1000;
        private long taskSize;
        private final ProgressMeter progMeter;

        public JobListener() {
            this.progMeter = new ProgressMeter("Creating image");
        }

        public void start() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progMeter.setVisible(true);
                }
            });
        }

        public void update(long done) {
            progMeter.setProgress((float)done / taskSize);
        }

        public void finish() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progMeter.setVisible(false);
                }
            });
        }

        public void setUpdateInterval(long numPixels) {}

        public void setUpdateInterval(double propPixels) {}

        public long getUpdateInterval() {
            return updateInterval;
        }

        public void setTaskSize(long numPixels) {
            taskSize = numPixels;
        }
        
    }

    private Map<Integer, ImageReceiver> jobs = CollectionFactory.map();

    /**
     * Private constructor
     */
    private DemoImageProvider() {
        executor = new JiffleExecutor();
        executor.addEventListener(new JiffleEventListener() {

            public void onCompletionEvent(JiffleEvent ev) {
                onCompletion(ev);
            }

            public void onFailureEvent(JiffleEvent ev) {
                throw new IllegalStateException("Jiffle script failed to run");
            }
        });
        
    }

    /**
     * Access the single instance of this class
     */
    public static DemoImageProvider getInstance() {
        if (instance == null) {
            instance = new DemoImageProvider();
        }

        return instance;
    }

    /**
     * Request one of the available iamges to be constructed. When the image is
     * ready it will be sent to the ImageReciever object via that object's
     * receiveImage method.
     *
     * @param choice one of CHESSBOARD, INTERFERENCE, RIPPLES
     * @param width image width
     * @param height image height
     * @param receiver the object that is requesting the image
     * @throws java.lang.Exception
     */
    public void requestImage(int choice, int width, int height, ImageReceiver receiver)
            throws Exception {

        String name = "/scripts/" + scriptName[choice] + ".jfl";
        URL url = DemoImageProvider.class.getResource(name);
        File file = new File(url.toURI());

        Map<String, Jiffle.ImageRole> imgParams = CollectionFactory.map();
        imgParams.put("result", Jiffle.ImageRole.DEST);
        
        Map<String, RenderedImage> images = CollectionFactory.map();
        images.put("result", ImageUtils.createConstantImage(width, height, Double.valueOf(0d)));

        try {
            Jiffle jiffle = new Jiffle(file, imgParams);
            int jobID = executor.submit(jiffle, images, new JobListener());
            jobs.put(jobID, receiver);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Handle completion events from the Jiffle interpreter
     */
    private void onCompletion(JiffleEvent ev) {
        JiffleExecutorResult result = ev.getResult();
        ImageReceiver receiver = jobs.remove( result.getJobID() );
        receiver.receiveImage(result.getImages().get("result"));
    }

}
