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

package jaitools.demo.iterators;

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

import jaitools.imageutils.ImageUtils;
import jaitools.imageutils.WindowIterator;
import jaitools.imageutils.WritableSimpleIterator;
import jaitools.swing.SimpleImagePane;
 

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
