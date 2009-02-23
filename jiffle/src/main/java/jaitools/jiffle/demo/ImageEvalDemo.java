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

import jaitools.jiffle.util.ImageUtils;
import jaitools.jiffle.runtime.JiffleFailureEvent;
import jaitools.jiffle.runtime.JiffleInterpreter;
import com.sun.media.jai.widget.DisplayJAI;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.JiffleCompilationException;
import jaitools.jiffle.runtime.JiffleCompletionEvent;
import jaitools.jiffle.runtime.JiffleEventAdapter;
import jaitools.jiffle.runtime.JiffleInterpreterException;
import java.awt.BorderLayout;
import java.awt.image.RenderedImage;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.media.jai.TiledImage;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * Demonstrates creating an image from a mathematical expression
 * involving pixel position. This is equivalent to plotting a function
 * in the x,y plane.
 * 
 * @author Michael Bedward
 */
public class ImageEvalDemo {

    private JiffleInterpreter interp;
    
    final int imgWidth = 500;
    final int imgHeight = 500;

    /**
     * Main function - runs the demo
     * @param args ignored
     */
    public static void main(String[] args) throws Exception {
        ImageEvalDemo demo = new ImageEvalDemo();
        demo.createImageFromCoordExpr();
    }

    /**
     * Constructor. Creates an instance of JiffleInterpeter and 
     * sets up interpreter event handlers.
     */
    public ImageEvalDemo() {
        interp = new JiffleInterpreter();
        interp.addEventListener(new JiffleEventAdapter() {

            @Override
            public void onCompletionEvent(JiffleCompletionEvent ev) {
                onCompletion(ev);
            }

            @Override
            public void onFailureEvent(JiffleFailureEvent ev) {
                onFailure(ev);
            }
        });
    }

    /**
     * Create an image of concentric ripples calculated as: 
     * <pre>{@code \u0000 
     * f(x,y) = sin(8\u03c0d)
     * }</pre>
     * where {@code d} is distance from image centre.
     * <p>
     * The jiffle code for this function is:
     * <pre>{@code \u0000
     * xorigin = imgWidth() / 2;
     * yorigin = imgHeight() / 2;
     * dx = (x() - xc) / xc;
     * dy = (y() - yc) / yc;
     * d = sqrt(dx^2 + dy^2);
     * result = sin(8 * PI * d);
     * }</pre>
     * where the variable {@code result} is linked to the output {@link javax.media.jai.TiledImage}
     * 
     */
    public void createImageFromCoordExpr() throws Exception {

        URL url = getClass().getResource("/example/ripple.jfl");
        File f = new File(url.toURI());

        TiledImage tImg = ImageUtils.createDoubleImage(imgWidth, imgHeight);

        Map<String, TiledImage> imgParams = new HashMap<String, TiledImage>();
        imgParams.put("result", tImg);

        try {
            Jiffle j = new Jiffle(f, imgParams);

            if (j.isCompiled()) {
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
     * Called when a completion event is received from the jiffle interpreter
     * @param ev the event
     */
    private void onCompletion(JiffleCompletionEvent ev) {
        TiledImage img = ev.getJiffle().getImage("result");
        displayImage(img);
    }

    /**
     * Called if a failure event is received from the jiffle interpreter
     * @param ev the event
     */
    private void onFailure(JiffleFailureEvent ev) {
        System.err.println("Bummer: script failed to run");
    }

    /**
     * Displays the image in a simple widget
     * @param img image to be displayed
     */
    private void displayImage(RenderedImage img) {
        JFrame frame = new JFrame("Jiffle image demo");

        DisplayJAI disp = new DisplayJAI(img);
        frame.getContentPane().add(new JScrollPane(disp), BorderLayout.CENTER);

        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

