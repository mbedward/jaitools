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

package jaitools.utils;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

/**
 * A progress bar in a frame for use in Jiffle demonstrations
 * 
 * @author Michael Bedward
 * @since 1.0
 * $Id$
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
