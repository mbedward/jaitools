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
package jaitools.jiffle.demo;

import jaitools.jiffle.runtime.JiffleProgressEvent;
import jaitools.jiffle.util.ImageUtils;
import jaitools.jiffle.runtime.JiffleFailureEvent;
import jaitools.jiffle.runtime.JiffleInterpreter;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.JiffleCompilationException;
import jaitools.jiffle.runtime.JiffleCompletionEvent;
import jaitools.jiffle.runtime.JiffleEventListener;
import jaitools.jiffle.runtime.JiffleInterpreterException;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.media.jai.TiledImage;
import javax.swing.SwingUtilities;

/**
 * Demonstrates the use of JiffleIterpreter to run a script.
 * <br>
 * There are two options for running executing a Jiffle script...
 * <ol type="1">
 * <li>Directly, by creating an instance of {@link jaitools.jiffle.runtime.JiffleRunner}
 * <li>Indirectly, by submitting a Jiffle object to an instance of
 *     {@link jaitools.jiffle.runtime.JiffleInterpreter}
 * </ol>
 * The advantage of the second method, for computationally expensive scripts,
 * is that execution is carried out in a separate thread. Client code is notified
 * of progress and completion or failure of the run through event listeners.
 * <p>
 * In this example program, the application class listens for progress events
 * and displays a simple progress bar for the user. When a completion event
 * is received the application creates an image viewer to display the result.
 * 
 * @see jaitools.jiffle.runtime.JiffleInterpreter
 * @see jaitools.jiffle.runtime.JiffleEvent
 * 
 * @author Michael Bedward
 */
public class InterpreterDemo extends DemoBase {
    private JiffleInterpreter interp;
    private ProgressMeter progMeter;
    
    final int imgWidth = 400;
    final int imgHeight = 400;

    /**
     * Run the demonstration
     * 
     * @param args ignored
     */
    public static void main(String[] args) throws Exception {
        InterpreterDemo demo = new InterpreterDemo();
        
        URL url = demo.getClass().getResource("/example/ripple.jfl");
        File f = new File(url.toURI());

        demo.compileAndRun(f);
    }

    /**
     * Constructor. Creates an instance of {@link jaitools.jiffle.runtime.JiffleInterpeter}
     * and sets up interpreter event handlers.
     */
    public InterpreterDemo() {
        interp = new JiffleInterpreter();
        interp.addEventListener(new JiffleEventListener() {

            public void onCompletionEvent(JiffleCompletionEvent ev) {
                onCompletion(ev);
            }

            public void onFailureEvent(JiffleFailureEvent ev) {
                onFailure(ev);
            }

            public void onProgressEvent(JiffleProgressEvent ev) {
                onProgress(ev);
            }
        });
        
        progMeter = new ProgressMeter();
    }

    /**
     * Compiles the script in the given text file and submits it to
     * the interpreter to run
     * 
     * @param scriptFile a text file containing the Jiffle script
     * @throws java.lang.Exception
     */
    public void compileAndRun(File scriptFile) throws Exception {
        // create an image to write results to
        TiledImage tImg = ImageUtils.createDoubleImage(imgWidth, imgHeight);

        // link the variable name used in the script (result) to the image
        Map<String, TiledImage> imgParams = new HashMap<String, TiledImage>();
        imgParams.put("result", tImg);

        try {
            Jiffle j = new Jiffle(scriptFile, imgParams);

            if (j.isCompiled()) {
                progMeter.setVisible(true);
                interp.submit(j);
            }

        } catch (JiffleCompilationException cex) {
            cex.printStackTrace();
            return;
        } catch (JiffleInterpreterException iex) {
            iex.printStackTrace();
        }
    }

    /**
     * Respond to an event signalling that the script has been
     * executed successfully. In this case we display the output
     * image.
     * 
     * @param ev the event
     */
    private void onCompletion(JiffleCompletionEvent ev) {
        progMeter.setVisible(false);
        TiledImage img = ev.getJiffle().getImage("result");
        displayImage(img);
    }

    /**
     * Respond to an event signalling that there was a problem
     * running the script.
     * 
     * @param ev the event
     */
    private void onFailure(JiffleFailureEvent ev) {
        System.err.println("Bummer: script failed to run");
        progMeter.setVisible(false);
    }
    
    
    private void onProgress(final JiffleProgressEvent ev) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progMeter.update(ev.getProgress());
            }
        });
    }
}
