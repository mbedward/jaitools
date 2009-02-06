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

package jaitools.jiffle;

import jaitools.jiffle.interpreter.JiffleFailureEvent;
import jaitools.jiffle.interpreter.JiffleInterpreter;
import com.sun.media.jai.widget.DisplayJAI;
import jaitools.jiffle.interpreter.Jiffle;
import jaitools.jiffle.interpreter.JiffleCompletionEvent;
import jaitools.jiffle.interpreter.JiffleEventListener;
import java.awt.BorderLayout;
import java.awt.image.RenderedImage;
import javax.media.jai.TiledImage;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 *
 * @author Michael Bedward
 */
public class ImageEvalDemo {
    
    private JiffleInterpreter interp;
    

    public static void main(String[] args) {
        ImageEvalDemo demo = new ImageEvalDemo();
        demo.createImageFromCoordExpr();
    }
    
    public ImageEvalDemo() {
        interp = new JiffleInterpreter();
        interp.addEventListener(new JiffleEventListener() {
            public void onCompletionEvent(JiffleCompletionEvent ev) {
                onCompletion(ev);
            }
            
            public void onFailureEvent(JiffleFailureEvent ev) {
                onFailure(ev);
            }
        });
    }
    
    public void createImageFromCoordExpr() {
        final int width = 1000;
        final int height = 1000;
        
        String cmd = 
                "xc = width() / 2; " +
                "yc = height() / 2;" +
                "d = sqrt((x()-xc)^2 + (y()-yc)^2);" +
                "result = sin(8 * PI * d);";
        
        TiledImage tImg = JiffleUtilities.createDoubleImage(width, height, 1);
        
        Jiffle j = new Jiffle(cmd);
        
        if (j.isCompiled()) {
            j.setImageMapping("result", tImg);
            interp.submit(j);
        }
    }
    
    private void onCompletion(JiffleCompletionEvent ev) {
        displayImage(ev.getJiffle().getImage("result"));
    }
    
    private void onFailure(JiffleFailureEvent ev) {
        System.err.println("Bummer: script failed to run");
    }

    private void displayImage(RenderedImage img) {
        JFrame frame = new JFrame("Jiffle image demo");
        
        DisplayJAI disp = new DisplayJAI(img);
        frame.getContentPane().add(new JScrollPane(disp), BorderLayout.CENTER);
        
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

