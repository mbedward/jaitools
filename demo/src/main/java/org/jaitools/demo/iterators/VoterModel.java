/* 
 *  Copyright (c) 2011, Michael Bedward. All rights reserved. 
 *   
 *  Redistribution and use in source and binary forms, with or without modification, 
 *  are permitted provided that the following conditions are met: 
 *   
 *  - Redistributions of source code must retain the above copyright notice, this  
 *    list of conditions and the following disclaimer. 
 *   
 *  - Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.   
 *   
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */   

package org.jaitools.demo.iterators;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jaitools.imageutils.ImageUtils;
import org.jaitools.imageutils.WindowIterator;
import org.jaitools.imageutils.WritableSimpleIterator;
import org.jaitools.swing.SimpleImagePane;
 

/**
 * This example uses WindowIterator and WritableSimpleIterator to
 * implement the VoterModel algorithm in which the value of each pixel
 * is replaced by that of one of its neighbours. The resulting image
 * becomes the input for the next iteration.
 * <p>
 * Starting with a random binary image, a pattern of clumps emerges 
 * as the algorithm proceeds.
 * 
 * @author michael
 */
public class VoterModel {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;
    
    private static final int MAX_ITERATIONS = 50;

    // Display update delay in milliseconds
    private static final long DELAY = 500;

    private final WritableRenderedImage[] images;
    private final Random rand;

    // See end of file for the VoterModelFrame class
    private VoterModelFrame frame;
    
    /**
     * Creates a new instance and runs the application.
     */
    public static void main(String[] args) {
        new VoterModel().runDemo();
    }

    /**
     * Initializes the images.
     */
    public VoterModel() {
        rand = new Random();

        images = new WritableRenderedImage[2];
        images[0] = ImageUtils.createConstantImage(WIDTH, HEIGHT, 0);
        images[1] = ImageUtils.createConstantImage(WIDTH, HEIGHT, 0);
    }

    /**
     * Fills the first (source) image with random binary values then
     * applies the Voter Model algorithm, displaying the resulting
     * image after DISPLAY_ITERATIONS number of iterations.
     */
    private void runDemo() {
        int sourceImage = 0;
        int destImage = 1;

        fillRandom(images[sourceImage]);

        Dimension winDim = new Dimension(3, 3);
        Point keyElement = new Point(1, 1);
        WindowIterator winIter = null;
        int[][] dataWindow = null;

        WritableSimpleIterator writeIter = null;
        
        showFrame(images[sourceImage]);

        for (int iter = 1; iter <= MAX_ITERATIONS; iter++) {
            winIter = new WindowIterator(images[sourceImage], null, winDim, keyElement);
            writeIter = new WritableSimpleIterator(images[destImage], null, null);

            do {
                dataWindow = winIter.getWindowInt(dataWindow);
                Point nbr = getRandomNbr(winDim, keyElement);
                writeIter.setSample(dataWindow[nbr.y][nbr.x]);
            } while (winIter.next() && writeIter.next());

            updateFrame(images[destImage], iter);
            
            sourceImage = ++sourceImage % 2;
            destImage = ++destImage % 2;
        }
    }

    /**
     * Fills an image with random binary values.
     * 
     * @param image the image to fill
     */
    private void fillRandom(WritableRenderedImage image) {
        WritableSimpleIterator iter = new WritableSimpleIterator(image, null, null);
        do {
            int value = rand.nextInt(2);
            iter.setSample(0, value);
        } while (iter.next());
    }

    /**
     * Chooses an element in the data window that is not the key element.
     * 
     * @param winDim window dimensions
     * @param keyElement key element position
     * @return selected element position
     */
    private Point getRandomNbr(Dimension winDim, Point keyElement) {
        Point nbr = new Point();
        while (true) {
            nbr.setLocation(rand.nextInt(winDim.width), rand.nextInt(winDim.height));
            if (!nbr.equals(keyElement)) {
                return nbr;
            }
        }
    }

    /**
     * Show the image frame and display the image.
     * 
     * @param image the image to display
     */
    private void showFrame(final RenderedImage image) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame = new VoterModelFrame();
                frame.setVisible(true);
            }
        });
        
        updateFrame(image, 0);
    }

    /**
     * Update the image frame.
     * 
     * @param image the new image
     */
    private void updateFrame(final RenderedImage image, final int iter) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.imagePane.setImage(image);
                frame.statusLine.setText(
                        String.format("Iteration %d of %d", iter, MAX_ITERATIONS));
            }
        });

        pause();
    }

    /**
     * Pause the display.
     */
    private void pause() {
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException ex) {
            // ignored
        }
    }

    /**
     * A JFrame with a child SimpleImagePane.
     */
    class VoterModelFrame extends JFrame {

        SimpleImagePane imagePane;
        JLabel statusLine;

        public VoterModelFrame() {
            super("Voter model");
            
            setSize(VoterModel.WIDTH, VoterModel.HEIGHT + 21);
            setResizable(false);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel panel = new JPanel(new BorderLayout());
            
            imagePane = new SimpleImagePane();
            panel.add(imagePane, BorderLayout.CENTER);

            statusLine = new JLabel("Starting...");
            panel.add(statusLine, BorderLayout.SOUTH);

            add(panel);
        }
        
    }
}
