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

package jaitools.jiffle.runtime;

import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.Metadata;
import jaitools.jiffle.parser.ImageCalculator;
import static jaitools.numeric.DoubleComparison.*;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;
import org.antlr.runtime.RecognitionException;

/**
 * Executes a compiled script contained within a Jiffle object.
 * You can run a script directly by creating an instance of this
 * class and calling its run method as in the following example...
 * <pre><code>
 *  RenderedImage inImg = ...  // get an input image
 * 
 *  // create an image to write output values to
 *  TiledImage outImg = JiffleUtilities.createDoubleImage(100, 100);
 *       
 *  // relate variable names in script to image objects
 *  Map<String, RenderedImage> imgParams = CollectionFactory.newMap();
 *  imgParams.put("result", outImg);
 *  imgParams.put("img1", inImg);
 *
 *  // get the script as a string and create a Jiffle object
 *  String script = ... 
 *  boolean success = false;
 *  try {
 *      Jiffle jif = new Jiffle(script, imgParams);
 *      if (jif.isCompiled()) {
 *         JiffleRunner runner = new JiffleRunner(jif);
 *         success = runner.run();
 *      }
 *  } catch (JiffleCompilationException cex) {
 *      cex.printStackTrace();
 *  } catch (JiffleInterpeterException iex) {
 *      iex.printStackTrace();
 *  }
 * 
 *  if (success) {
 *     // display result ...
 *  }
 * </code></pre>
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class JiffleRunner {

    private Jiffle jiffle;
    private Metadata metadata;
    private VarTable varTable;
    private FunctionTable functionTable;

    private static class ImageHandler {
        int x;
        int y;
        int xmin;
        int xmax;
        int ymin;
        int ymax;
        int band;
        boolean isOutput;
        RandomIter iter;

        boolean contains(int x, int y) {
            return (x >= xmin && x <= xmax && y >= ymin && y <= ymax);
        }
    }

    private Map<String, ImageHandler> handlerTable;

    private float propComplete;
    private boolean finished;
    
    private Set<RunProgressListener> progressListeners;
    private static final float PROGRESS_INCREMENT = 0.01f;
    
    /*
     * TODO: as a hack for development we use first output
     * image var as a reference - change this later when
     * allowing images with different bounds
     */
    RenderedImage refImg;
    int refImgSize;
    
    /*
     * TODO: better way of tracking progress
     */
    int numPixelsProcessed;
    
    /**
     * Constructor. Takes a compiled Jiffle and prepares the runtime
     * system.
     * 
     * @param jiffle a compiled Jiffle
     * @throws a JiffleInterpreterException if the Jiffle is not compiled
     */
    public JiffleRunner(Jiffle jiffle) throws JiffleInterpreterException {
        if (!jiffle.isCompiled()) {
            throw new JiffleInterpreterException("The jiffle is not compiled");
        }
        
        this.jiffle = jiffle;
        this.metadata = jiffle.getMetadata();
        
        varTable = new VarTable();
        functionTable = new FunctionTable();

        setSpecialVars();
        setHandlers();
        finished = false;
        numPixelsProcessed = 0;
        propComplete = 0.0f;
        
        progressListeners = CollectionFactory.newSet();
    }

    /**
     * Get the value of an image at the current location
     * @param imgName the image variable name
     * @return value as a double
     * @throws RuntimeException if the image variable has not been registered
     */
    public double getImageValue(String imgName) {
        ImageHandler h = handlerTable.get(imgName);
        if (h == null) {
            throw new RuntimeException("unknown image var name: " + imgName);
        }

        return h.iter.getSampleDouble(h.x, h.y, h.band);
    }
    
    /**
     * Get the value of a pixel in an image's neighbourhood.
     * @param imgName the image variable name
     * @param xOffset x offset (positive means right, negative left)
     * @param yOffset y offset (positive means below, negative above)
     * @return value as a double
     */
    public double getImageValue(String imgName, double xOffset, double yOffset) {
        ImageHandler h = handlerTable.get(imgName);
        if (h == null) {
            throw new RuntimeException("unknown image var name: " + imgName);
        }

        int xSign = dcomp(xOffset, 0d);
        int ySign = dcomp(yOffset, 0d);
        
        int x = h.x + toInt(xOffset);
        int y = h.y + toInt(yOffset);
        
        if (!h.contains(x, y)) {
            return Double.NaN;
        }
        
        return h.iter.getSampleDouble(x, y, h.band);
    }    

    /**
     * Get the current value of a variable
     * @param varName the variable name
     * @return value as a double
     * @throws a RuntimeException if the variable has not been assigned
     */
    public double getVar(String varName) {
        return varTable.get(varName);
    }

    /**
     * Invoke a general function
     * @param name the function name
     * @param args list of argument values (may be empty but not null)
     * @return the result of the function call as a double
     */
    public double invokeFunction(String name, List<Double> args) {
        return functionTable.invoke(name, args);
    }


    /**
     * Invoke an operator with a single argument
     * @param op the identifying {@code LogicalOp} constant
     * @param arg the argument
     * @return the result of the operator as a double
     */
    public double invokeLogicalOp(LogicalOp op, Double arg) {
        return functionTable.invoke(op.toString(), arg);
    }

    /**
     * Invoke an operator with two arguments
     * @param op the identifying {@code LogicalOp} constant
     * @param arg1 first argument
     * @param arg2 second argument
     * @return the result of the operator as a double
     */
    public double invokeLogicalOp(LogicalOp op, Double arg1, Double arg2) {
        return functionTable.invoke(op.toString(), arg1, arg2);
    }

    /**
     * Check if a variable has been assigned
     * @param varName the variable name
     */
    public boolean isVarDefined(String varName) {
        return varTable.contains(varName);
    }

    /**
     * Set the value of a variable
     * @param varName variable name
     * @param value the value to assign
     */
    public void setVar(String varName, double value) {
        varTable.set(varName, value);
    }
    
    /**
     * Set the value of a variable
     * @param varName variable name
     * @param op assignment operator symbol (e.g. "=", "*=")
     * @param value the value to assign
     */
    public void setVar(String varName, String op, double value) {
        varTable.assign(varName, op, value);
    }

    /**
     * Write a value to the current image pixel location
     * @param imgName image variable name
     * @param value value to write
     */
    public void writeToImage(String imgName, double value) {
        ImageHandler h = handlerTable.get(imgName);
        if (h == null) {
            throw new RuntimeException("unknown image var name: " + imgName);
        }

        ((WritableRandomIter) h.iter).setSample(h.x, h.y, h.band, value);
    }
    
    /**
     * Executes the script. This method can only be called once. If the
     * script is to be re-run a new JiffleRunner instance should be
     * created.
     * 
     * @return success (true) or failure (false)
     */
    public boolean run() throws JiffleInterpreterException {
        if (finished) {
            throw new JiffleInterpreterException("JiffleRunner.run() can only be called once");
        }
        
        ImageCalculator calc = new ImageCalculator(jiffle.getRuntimeAST());
        calc.setRunner(this);
        
        /* 
         * Evalute the AST at each pixel position
         */
        while (!finished) {
            try {
                calc.start();
            } catch (RecognitionException rex) {
                throw new JiffleInterpreterException("ImageCalculator failed: " + rex.getMessage());
            }
            calc.reset();
            nextPixel();
        }
        
        return true;
    }

    /**
     * Package private method used by the interpreter system to add
     * a progress listener
     * 
     * @param listener
     */
    void addProgressListener(RunProgressListener listener) {
        progressListeners.add(listener);
    }

    /**
     * Set handlers for each input and output image. A handler keeps track of
     * current image position and owns an iterator to read or write pixel
     * values
     */
    private void setHandlers() {
        handlerTable = CollectionFactory.newMap();

        for (Entry<String, RenderedImage> e : metadata.getImageParams().entrySet()) {
            ImageHandler h = new ImageHandler();
            RenderedImage image = e.getValue();
            Rectangle bounds = null;

            h.x = h.xmin = image.getMinX();
            h.y = h.ymin = image.getMinY();
            h.band = 0;
            h.isOutput = metadata.getOutputImageVar().equals(e.getKey());

            if (image instanceof PlanarImage) {
                PlanarImage pImage = (PlanarImage)image;
                bounds = pImage.getBounds();
                h.xmax = pImage.getMaxX() - 1;
                h.ymax = pImage.getMaxY() - 1;

            } else if (image instanceof BufferedImage) {
                BufferedImage bImage = (BufferedImage)image;
                bounds = new Rectangle(
                        bImage.getMinX(), bImage.getMinY(),
                        bImage.getWidth(), bImage.getHeight());

                h.xmax = bounds.x + bounds.width - 2;
                h.ymax = bounds.y + bounds.height - 2;
            }

            if (h.isOutput) {
                h.iter = RandomIterFactory.createWritable((WritableRenderedImage)image, bounds);
            } else {
                h.iter = RandomIterFactory.create(image, bounds);
            }

            handlerTable.put(e.getKey(), h);
        }
    }
    
    /**
     * Set up the special variables that are proxies for jiffle image
     * info and positional functions
     */
    private void setSpecialVars() {
        // TODO: as a hack for development we use first output
        // image var as a reference - change this later when
        // allowing images with different bounds
        
        refImg = metadata.getImageParams().get(metadata.getOutputImageVar());
        
        Rectangle bounds = null;
        if (refImg instanceof PlanarImage) {
            bounds = ((PlanarImage)refImg).getBounds();

        } else if (refImg instanceof BufferedImage) {
            BufferedImage bImage = (BufferedImage)refImg;
            bounds = new Rectangle(
                    bImage.getMinX(), bImage.getMinY(),
                    bImage.getWidth(), bImage.getHeight());
        }

        refImgSize = bounds.width * bounds.height;

        functionTable.setRuntimeBounds(bounds);
    }

    /**
     * Advance to the next pixel position. If all pixels have been
     * processed set the finished flag.
     */
    private void nextPixel() {
        if (!finished) {
            boolean firstImg = true;  // @todo remove this hack
            
            for (ImageHandler h : handlerTable.values()) {
                h.x++;
                if (h.x > h.xmax) {
                    h.x = h.xmin;
                    h.y++;

                    if (h.y > h.ymax) {
                        finished = true;
                    }
                }

                // @todo remove this hack
                if (firstImg) {
                    varTable.set("_x", h.x - h.xmin);
                    varTable.set("_y", h.y - h.ymin);
                    varTable.set("_col", h.x - h.xmin + 1);
                    varTable.set("_row", h.y - h.ymin + 1);
                    firstImg = false;

                    numPixelsProcessed++ ;
                    publishProgress();
                }
            }
        }
    }
    
    private void publishProgress() {
        float prop = (float)numPixelsProcessed / refImgSize;
        if (prop - propComplete >= PROGRESS_INCREMENT) {
            propComplete = prop;
            for (RunProgressListener listener : progressListeners) {
                listener.onProgress(propComplete);
            }
        }
    }
}
