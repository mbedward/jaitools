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

package jaitools.swing;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * A progress bar in a frame with a title and optional label.
 * This was written to be used with jai-tools demonstration
 * programs.
 * <p>
 * The update methods {@linkplain #setProgress(float progress)}
 * and {@linkplain #setLabel(String label)} may be called from
 * any thread. If the calling thread is not the AWT event dispatch
 * thread the updates will be passed to the dispatch thread.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class ProgressMeter extends JFrame {
    
    private static final int MIN_PROGRESS = 0;
    private static final int MAX_PROGRESS = 100;
    
    JProgressBar progBar;
    JLabel label;
    private boolean preset;


    /**
     * Constructor
     */
    public ProgressMeter() {
        this("Progress");
    }

    /**
     * Constructor. Sets the title but no label.
     */
    public ProgressMeter(String title) {
        this(title, null);
    }

    /**
     * Constructor. Sets the title and progress bar label.
     */
    public ProgressMeter(String title, String labelText) {
        setTitle(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        progBar = new JProgressBar(MIN_PROGRESS, MAX_PROGRESS);
        getContentPane().add(progBar, BorderLayout.CENTER);
        
        label = new JLabel("  ");
        if (labelText != null && labelText.length() > 0) {
            label.setText(labelText);
        }
        getContentPane().add(label, BorderLayout.SOUTH);

        setSize(400, 60);
        setLocationByPlatform(true);
    }
    
    /**
     * Update the progress bar.
     * 
     * @param progress a proportion value between 0 and 1
     */
    public void setProgress(final float progress) {
        final int barValue = (int)Math.ceil((MAX_PROGRESS - MIN_PROGRESS) * progress);

        if (isVisible()) {
            if (EventQueue.isDispatchThread()) {
                progBar.setValue(barValue);

            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        progBar.setValue(barValue);
                    }
                });
            }

        } else {
            progBar.setValue(barValue);
            preset = true;
        }
    }

    /**
     * Update the progress label
     */
    public void setLabel(final String text) {
        if (!isVisible() || EventQueue.isDispatchThread()) {
            label.setText(text);

        } else {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    label.setText(text);
                }
            });
        }
    }

    @Override
    public void setVisible(boolean b) {
        if (b == true) {
            if (!preset) {
                progBar.setValue(MIN_PROGRESS);
            }
        }
        super.setVisible(b);
    }

}
