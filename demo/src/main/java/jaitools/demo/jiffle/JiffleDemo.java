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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.RenderedImage;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import jaitools.demo.ImageChoice;
import jaitools.demo.SimpleImagePane;
import jaitools.jiffle.JiffleBuilder;
import jaitools.jiffle.JiffleException;
import java.awt.Font;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

/**
 * A browser for Jiffle example scripts. Displays the script in a text
 * window and the destination image in an adjacent window.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class JiffleDemo extends JFrame {
    private SimpleImagePane imagePane;
    private JTextArea scriptPane;
    private JSplitPane splitPane;
    
    private int imageWidth = 400;
    private int imageHeight = 400;

    
    public static void main(String[] args) {
        JiffleDemo me = new JiffleDemo();
        me.setSize(800, 500);
        me.setVisible(true);
    }


    private JiffleDemo() {
        super("Jiffle scripting language");
        initComponents();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    
    @Override
    public void setVisible(boolean vis) {
        if (vis) {
            splitPane.setDividerLocation((int) (getWidth() * 0.45));
        }
        super.setVisible(vis);
    }
    

    private void initComponents() {
        imagePane = new SimpleImagePane();
        
        scriptPane = new JTextArea();
        scriptPane.setEditable(false);
        Font font = new Font("Courier", Font.PLAIN, 12);
        scriptPane.setFont(font);
        
        JScrollPane imageScroll = new JScrollPane(imagePane);
        JScrollPane scriptScroll = new JScrollPane(scriptPane);
        
        splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                scriptScroll, imageScroll);
        
        Dimension minSize = new Dimension(100, 100);
        imageScroll.setMinimumSize(minSize);
        scriptScroll.setMinimumSize(minSize);
        
        getContentPane().add(splitPane);
        
        JMenuItem item;
        JMenuBar menuBar = new JMenuBar();
        JMenu mainMenu = new JMenu("File");
        
        JMenu scriptMenu = new JMenu("Example scripts");
        
        for (final ImageChoice choice : ImageChoice.values()) {
            item = new JMenuItem(choice.toString());
        
            item.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    loadScript(choice);
                }
            } );
            
            scriptMenu.add(item);
        }
        
        mainMenu.add(scriptMenu);
        menuBar.add(mainMenu);
        
        JMenu viewMenu = new JMenu("View");
        
        item = new JMenuItem("Bigger font");
        item.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                setFontSize(1);
            }
        });
        item.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_UP, 
                KeyEvent.SHIFT_DOWN_MASK|KeyEvent.CTRL_DOWN_MASK));
        viewMenu.add(item);
        
        item = new JMenuItem("Smaller font");
        item.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                setFontSize(-1);
            }
        });
        item.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_DOWN, 
                KeyEvent.SHIFT_DOWN_MASK|KeyEvent.CTRL_DOWN_MASK));
        viewMenu.add(item);
        
        menuBar.add(viewMenu);
        setJMenuBar(menuBar);
    }
    
    private void loadScript(ImageChoice imageChoice) {
        try {
            String script = JiffleDemoHelper.getScript(imageChoice);
            runScript(script, imageChoice.getDestImageVarName());
            
        } catch (JiffleException ex) {
            JOptionPane.showMessageDialog(this, 
                    "Problem loading the example script", 
                    "Bummer", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void runScript(String script, String destVarName) {
        try {
            scriptPane.setText(script);
            
            JiffleBuilder builder = new JiffleBuilder();
            builder.script(script).dest(destVarName, imageWidth, imageHeight);
            RenderedImage image = builder.run().getImage(destVarName);
            imagePane.setImage(image);
            
        } catch (JiffleException ex) {
            JOptionPane.showMessageDialog(this, 
                    "Errors compiling or running the script", 
                    "Bummer", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void setFontSize(int delta) {
        Font font = scriptPane.getFont();
        Font font2 = font.deriveFont((float) font.getSize() + delta);
        scriptPane.setFont(font2);
    }
}
