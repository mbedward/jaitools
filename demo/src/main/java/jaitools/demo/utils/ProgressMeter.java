/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.demo.utils;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

/**
 * A progress bar in a frame for use in Jiffle demonstrations
 * 
 * @author Michael Bedward
 */
public class ProgressMeter extends JFrame {
    
    private static final int MIN_PROGRESS = 0;
    private static final int MAX_PROGRESS = 100;
    
    JProgressBar progBar;


    /**
     * Constructor
     */
    public ProgressMeter() {
        this("Progress");
    }

    /**
     * Constructor
     */
    public ProgressMeter(String title) {
        setTitle(title);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        progBar = new JProgressBar(MIN_PROGRESS, MAX_PROGRESS);
        getContentPane().add(progBar, BorderLayout.CENTER);
        setSize(400, 60);
        setLocationByPlatform(true);
    }
    
    /**
     * Update the progress bar
     * 
     * @param progress a value between 0 and 1
     */
    public void update(float progress) {
        progBar.setValue((int)Math.ceil(100 * progress));
    }

    @Override
    public void setVisible(boolean b) {
        if (b == true) {
            progBar.setValue(0);
        }
        super.setVisible(b);
    }

}
