/*
 * Copyright 2009 Michael Bedward
 *
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jaitools.demo;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.runtime.JiffleCompletionEvent;
import jaitools.jiffle.runtime.JiffleEventAdapter;
import jaitools.jiffle.runtime.JiffleInterpreter;
import jaitools.jiffle.runtime.JiffleProgressEvent;
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

    private static final String[] scriptName = {
        "chessboard",
        "interference",
        "ripple"
    };

    /* single instance of this class */
    private static DemoImageProvider instance;

    private JiffleInterpreter interp;
    private RenderedImage image;

    private static class Job {
        ImageReceiver receiver;
        ProgressMeter progMeter = new ProgressMeter();
    }

    private Map<Integer, Job> jobs = CollectionFactory.orderedMap();

    /**
     * Private constructor
     */
    private DemoImageProvider() {
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

        image = ImageUtils.createConstantImage(width, height, Double.valueOf(0d));

        Map<String, RenderedImage> imgParams = CollectionFactory.map();
        imgParams.put("result", image);

        try {
            Job job = new Job();
            job.receiver = receiver;
            job.progMeter.setTitle("Creating image");
            job.progMeter.setVisible(true);

            Jiffle jiffle = new Jiffle(file, imgParams);
            int jobID = interp.submit(jiffle);
            jobs.put(jobID, job);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Handle completion events from the Jiffle interpreter
     */
    private void onCompletion(JiffleCompletionEvent ev) {
        Job job = jobs.get(ev.getJobId());
        job.progMeter.setVisible(false);
        job.receiver.receiveImage(ev.getJiffle().getImage("result"));
    }

    /**
     * Updates a progress bar to show proportion of the image
     * creation done so far
     */
    private void showProgress(final JiffleProgressEvent ev) {
        final Job job = jobs.get(ev.getJobId());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                job.progMeter.setProgress(ev.getProgress());
            }
        });
    }

}
