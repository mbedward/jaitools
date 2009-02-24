/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.demo;

import com.sun.media.jai.widget.DisplayJAI;
import java.awt.BorderLayout;
import java.awt.image.RenderedImage;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * Base class for Jiffle demonstration classes
 * 
 * @author Michael Bedward
 */
public abstract class DemoBase {

    /**
     * Displays an image in a scrolling panel
     * 
     * @param img image to be displayed
     */
    protected void displayImage(RenderedImage img) {
        JFrame frame = new JFrame("Jiffle image demo");

        DisplayJAI disp = new DisplayJAI(img);
        frame.getContentPane().add(new JScrollPane(disp), BorderLayout.CENTER);

        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }
    
}
