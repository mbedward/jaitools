/*
 * Copyright 2011 Michael Bedward
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
package jaitools.demo.jiffle;

import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Map;

import jaitools.CollectionFactory;
import jaitools.demo.ImageChoice;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.runtime.JiffleExecutor;
import jaitools.jiffle.runtime.JiffleExecutorResult;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.runtime.JiffleEvent;
import jaitools.jiffle.runtime.JiffleEventListener;
import jaitools.jiffle.runtime.NullProgressListener;
import jaitools.swing.ImageFrame;

/**
 * Demonstrates the use of {@link JiffleExecutor} to run a script.
 * <br>
 * There are two options for running a Jiffle script...
 * <ol type="1">
 * <li>Directly, by getting a {@link jaitools.jiffle.runtime.JiffleRuntime} object
 *     from the compiled {@code Jiffle} object.
 * <li>Indirectly, by submitting a Jiffle object to a
 *     {@link jaitools.jiffle.runtime.JiffleExecutor}.
 * </ol>
 * The advantage of the second method for computationally demanding tasks
 * is that execution is carried out in a separate thread. The caller is informed
 * about completion or failure via {@link JiffleEvent}s and can track progress 
 * using a {@link jaitools.jiffle.runtime.JiffleProgressListener}.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class JiffleExecutorDemo extends JiffleDemoBase {

    private JiffleExecutor executor;
    
    /**
     * Run the demonstration. The optional {@code arg} can be either
     * the path to a user-supplied script or one of "chessboard",
     * "interference", "ripple" or "squircle".
     * 
     * @param args (optional) the script to run
     * @throws Exception on problems compiling the script
     */
    public static void main(String[] args) throws Exception {
        JiffleExecutorDemo demo = new JiffleExecutorDemo();
        File f = JiffleDemoHelper.getScriptFile(args, ImageChoice.RIPPLES);
        demo.compileAndRun(f);        
    }

    /**
     * Constructor. Creates an instance of {@link jaitools.jiffle.runtime.JiffleInterpeter}
     * and sets up interpreter event handlers.
     */
    public JiffleExecutorDemo() {
        executor = new JiffleExecutor(1);
        executor.addEventListener(new JiffleEventListener() {

            public void onCompletionEvent(JiffleEvent ev) {
                onCompletion(ev);
            }

            public void onFailureEvent(JiffleEvent ev) {
                onFailure(ev);
            }
        });
    }

    /**
     * Compiles a script read from a file and submits it for execution.
     * 
     * @param scriptFile file containing the Jiffle script
     * @throws Exception on problems compiling the script
     */
    public void compileAndRun(File scriptFile) throws Exception {
        Map<String, Jiffle.ImageRole> imageParams = CollectionFactory.map();
        imageParams.put("result", Jiffle.ImageRole.DEST);

        Jiffle j = new Jiffle(scriptFile, imageParams);

        Map<String, RenderedImage> images = CollectionFactory.map();
        images.put("result",
                ImageUtils.createConstantImage(WIDTH, HEIGHT, Double.valueOf(0d)));

        if (j.isCompiled()) {
            executor.submit(j, images, new NullProgressListener());
        }
    }

    /**
     * Called when the Jiffle task has been completed successfully.
     * 
     * @param ev the event containing the task results
     */
    private void onCompletion(JiffleEvent ev) {
        JiffleExecutorResult result = ev.getResult();
        RenderedImage img = result.getImages().get("result");

        ImageFrame frame = new ImageFrame(img, "Jiffle image demo");
        frame.setVisible(true);
    }

    /**
     * Called if the Jiffle task fails for some reason.
     * 
     * @param ev the event
     */
    private void onFailure(JiffleEvent ev) {
        System.err.println("Bummer: script failed to run");
    }
    
}
