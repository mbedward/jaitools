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

import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.JiffleException;
import jaitools.jiffle.runtime.AbstractDirectRuntime;
import jaitools.jiffle.runtime.AbstractProgressListener;
import jaitools.jiffle.runtime.JiffleDirectRuntime;
import jaitools.jiffle.runtime.JiffleExecutor;
import jaitools.jiffle.runtime.JiffleExecutorException;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.RenderedImage;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * Demonstrates using a JiffleProgressListener with JiffleExecutor.
 * <p>
 * Rather than running a real Jiffle task that will take long enough to 
 * be worth using a progress listener, we cheat and use mock Jiffle and
 * JiffleRuntime classes (see bottom of source code). The runtime class
 * pretends process pixels by just having a little sleep each time its
 * {@code evaluate()} method is called.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class ProgressListenerDemo {

    // Number of pixels in the pretend task
    private static final int NUM_PIXELS = 500;
    
    // Milliseconds to spend pretending to process a pixel
    private static final long PIXEL_TIME = 10;

    /**
     * Runs the demo.
     */
    public static void main(String[] args) throws Exception {
        ProgressListenerDemo me = new ProgressListenerDemo();
        me.demo();
    }

    /**
     * This method shows how you might use a progress listener with
     * JiffleExecutor.
     * 
     * @throws JiffleExecutorException 
     */
    private void demo() throws JiffleExecutorException {
        MyProgressListener listener = new MyProgressListener();
        
        /* 
         * The update interval can be set as number of pixels or 
         * a proportion of total task size. Here we use the latter
         * method to request that the listener is notified after
         * each 10% of the task has been completed.
         */
        listener.setUpdateInterval(0.1);
        
        JiffleExecutor executor = new JiffleExecutor();

        PretendJiffle jiffle = new PretendJiffle();
        Map<String, RenderedImage> emptyImageMap = CollectionFactory.map();
        
        executor.submit(jiffle, emptyImageMap, listener);
    }
    

    /**
     * Our progress listener class extends {@link AbstractProgressListener}
     * and provides start, update and finish methods which we will use to
     * display and update a Swing widget.
     */
    class MyProgressListener extends AbstractProgressListener {
        ProgressMeter meter;

        public MyProgressListener() {
            meter = new ProgressMeter();
            meter.setLocationByPlatform(true);
        }

        public void start() {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    meter.setVisible(true);
                }
            });
        }

        public void update(long done) {
            int percent = (int) (100 * (double) done / taskSize);
            meter.update(percent);
        }

        public void finish() {
            meter.done();
        }
    }
    

    /**
     * Simple Swing widget with a progress bar and a button
     * that is enabled when the task is finished.
     */
    class ProgressMeter extends JDialog {
        private JProgressBar bar;
        private JButton btn;

        public ProgressMeter() {
            setTitle("Trying to look busy");
            setSize(400, 80);
            
            initComponents();
            setModal(true);
        }
        
        private void initComponents() {
            JPanel panel = new JPanel(new BorderLayout());
            
            bar = new JProgressBar();
            bar.setMaximum(100);
            panel.add(bar, BorderLayout.NORTH);
            
            btn = new JButton("Working...");
            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            btn.setEnabled(false);
            panel.add(btn, BorderLayout.SOUTH);
            
            getContentPane().add(panel);
        }

        void update(final int progress) {
            if (EventQueue.isDispatchThread()) {
                bar.setValue(progress);
            } else {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        bar.setValue(progress);
                    }
                });
            }
        }
        
        void done() {
            final String msg = "Finished";
            
            if (EventQueue.isDispatchThread()) {
                btn.setText(msg);
            } else {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        btn.setText(msg);
                    }
                });
            }
            btn.setEnabled(true);
        }
    }

    
    /**
     * The mock Jiffle used in this demo. It delivers our mock runtime
     * object to the executor.
     */
    class PretendJiffle extends Jiffle {
        @Override
        public boolean isCompiled() {
            return true;
        }

        @Override
        public Map<String, ImageRole> getImageParams() {
            Map<String, ImageRole> emptyParams = CollectionFactory.map();
            return emptyParams;
        }

        @Override
        public JiffleDirectRuntime getRuntimeInstance() throws JiffleException {
            return new PretendJiffleRuntime();
        }
    }

    
    /**
     * Mock runtime object that pretends to process pixels by
     * having a little sleep each time.
     */
    class PretendJiffleRuntime extends AbstractDirectRuntime {

        public PretendJiffleRuntime() {
            // set the pretend processing area
            setBounds(0, 0, NUM_PIXELS, 1);
        }

        @Override
        protected void initImageScopeVars() {}

        @Override
        protected void initOptionVars() {}

        /**
         * Pretends to process a pixel (very slowly).
         */
        public void evaluate(int x, int y) {
            try {
                Thread.sleep(PIXEL_TIME);

            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        protected Double getDefaultValue(int index) {
            return null;
        }
    }
}
