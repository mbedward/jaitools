/* 
 *  Copyright (c) 2013, Michael Bedward. All rights reserved. 
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

package org.jaitools.testutils;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.RenderedImage;
import java.util.concurrent.CountDownLatch;
import javax.swing.SwingUtilities;
import org.jaitools.swing.ImageFrame;

/**
 * Associates an ImageFrame with a CountDownLatch. When the frame is closing
 * it counts down the latch, the allowing a test method to display an image
 * and wait for the frame to be dismissed.
 *
 * @author michael
 */
public class WaitingImageFrame {

    public static void showImage(final RenderedImage image, 
            final String caption, 
            final CountDownLatch latch) {
        
        if (SwingUtilities.isEventDispatchThread()) {
            doShowImage(image, caption, latch);

        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    doShowImage(image, caption, latch);
                }
            });
        }
    }

    private static void doShowImage(RenderedImage image, String title, final CountDownLatch latch) {
        ImageFrame frame = new ImageFrame(image, title);
        
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                latch.countDown();
            }
        });
        
        frame.setDefaultCloseOperation(ImageFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
    
}
