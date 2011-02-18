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
 * be worth using a progress listener, we cheat and use fake Jiffle and
 * JiffleRuntime classes (see bottom of source code).
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class ProgressListenerDemo {

    private static final int NUM_PIXELS = 500;
    private static final long PIXEL_TIME = 10;

    public static void main(String[] args) throws Exception {
        ProgressListenerDemo me = new ProgressListenerDemo();
        me.demo();
    }

    private void demo() throws JiffleExecutorException {

        MyProgressListener listener = new MyProgressListener();
        listener.setUpdateInterval(0.1);
        
        JiffleExecutor executor = new JiffleExecutor();

        PretendJiffle jiffle = new PretendJiffle();
        Map<String, RenderedImage> emptyImageMap = CollectionFactory.map();
        executor.submit(jiffle, emptyImageMap, listener);
    }
    

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

    
    class PretendJiffleRuntime extends AbstractDirectRuntime {

        public PretendJiffleRuntime() {
            // set the pretend processing area
            this._width = NUM_PIXELS;
            this._height = 1;
            this._minx = 0;
            this._miny = 0;
        }

        @Override
        protected void initImageScopeVars() {}

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
    }
}
