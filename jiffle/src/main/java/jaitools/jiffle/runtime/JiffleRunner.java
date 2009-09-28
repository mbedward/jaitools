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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;
import org.antlr.runtime.RecognitionException;

/**
 * Executes a compiled script contained within a Jiffle object.
 * You can run a script directly by creating an instance of this
 * class and calling its run method as in the following example...
 * <pre><code>
 *  TiledImage inImg = ...  // get an input image
 * 
 *  // create an image to write output values to
 *  TiledImage outImg = JiffleUtilities.createDoubleImage(100, 100);
 *       
 *  // relate variable names in script to image objects
 *  Map<String, TiledImage> imgParams = new HashMap<String, TiledImage>();
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
    
    private static class ImageFnProxy {
        enum Type {POS, INFO};
        
        String varName;
        Type type;
        
        ImageFnProxy(Type type, String varName) {
            this.type = type;  this.varName = varName;
        }
    }
    
    private static Map<String, ImageFnProxy> proxyTable;
    static {
        proxyTable = CollectionFactory.newMap();
        proxyTable.put("width", new ImageFnProxy(ImageFnProxy.Type.INFO, "_width"));
        proxyTable.put("height", new ImageFnProxy(ImageFnProxy.Type.INFO, "_height"));
        proxyTable.put("size", new ImageFnProxy(ImageFnProxy.Type.INFO, "_size"));
        proxyTable.put("x", new ImageFnProxy(ImageFnProxy.Type.POS, "_x"));
        proxyTable.put("y", new ImageFnProxy(ImageFnProxy.Type.POS, "_y"));
        proxyTable.put("row", new ImageFnProxy(ImageFnProxy.Type.POS, "_row"));
        proxyTable.put("col", new ImageFnProxy(ImageFnProxy.Type.POS, "_col"));
    }
    
    private Jiffle jiffle;
    private Metadata metadata;
    private VarTable vars;
    private FunctionTable funcs;

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
    TiledImage refImg;
    int refImgSize;
    
    /*
     * TODO: better way of tracking progress
     */
    int numPixelsProcessed;
    
    
    /**
     * Query whether a function name refers to a positional
     * function, ie. one which returns the current pixel location
     * such as x().
     * 
     * @param name function name
     * @return true if a positional function; false otherwise
     */
    public static boolean isPositionalFunction(String name) {
        ImageFnProxy proxy = proxyTable.get(name);
        if (proxy != null  &&  proxy.type == ImageFnProxy.Type.POS) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Query if a function name refers to an image info function,
     * e.g. width() which returns image width in pixels
     * 
     * @param name function name
     * @return true if an image info function; false otherwise
     */
    public static boolean isInfoFunction(String name) {
        ImageFnProxy proxy = proxyTable.get(name);
        if (proxy != null  &&  proxy.type == ImageFnProxy.Type.INFO) {
            return true;
        }
        
        return false;
    }
    
    /**
     * A function used by the compiler and not intended for client code.
     * Jiffle creates pfoxy variables for image position and info functions
     * so that values can be looked up in a symbol table at runtime 
     * rather than needing a function invocation.
     * 
     * @param funcName function name
     * @return a proxy variable name
     */
    
    public static String getImageFunctionProxyVar(String funcName) {
        return proxyTable.get(funcName).varName;
    }
    
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
        
        vars = new VarTable();
        funcs = new FunctionTable();

        setHandlers();
        setSpecialVars();
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
        return vars.get(varName);
    }

    /**
     * Invoke a general function
     * @param name the function name
     * @param args list of argument values (may be empty but not null)
     * @return the result of the function call as a double
     */
    public double invokeFunction(String name, List<Double> args) {
        return funcs.invoke(name, args);
    }
    
    /**
     * Check if a variable has been assigned
     * @param varName the variable name
     */
    public boolean isVarDefined(String varName) {
        return vars.contains(varName);
    }

    /**
     * Set the value of a variable
     * @param varName variable name
     * @param value the value to assign
     */
    public void setVar(String varName, double value) {
        vars.set(varName, value);
    }
    
    /**
     * Set the value of a variable
     * @param varName variable name
     * @param op assignment operator symbol (e.g. "=", "*=")
     * @param value the value to assign
     */
    public void setVar(String varName, String op, double value) {
        vars.assign(varName, op, value);
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

        for (Entry<String, TiledImage> e : metadata.getImageParams().entrySet()) {
            ImageHandler h = new ImageHandler();
            TiledImage img = e.getValue();

            h.x = h.xmin = img.getMinX();
            h.y = h.ymin = img.getMinY();
            h.xmax = img.getMaxX() - 1;
            h.ymax = img.getMaxY() - 1;
            h.band = 0;
            h.isOutput = metadata.getOutputImageVars().contains(e.getKey());

            if (h.isOutput) {
                h.iter = RandomIterFactory.createWritable(img, img.getBounds());
            } else {
                h.iter = RandomIterFactory.create(img, img.getBounds());
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
        
        List<String> outVars = CollectionFactory.newList();
        outVars.addAll(metadata.getOutputImageVars());
        refImg = metadata.getImageParams().get(outVars.get(0));
        
        Rectangle bounds = refImg.getBounds();
        refImgSize = bounds.width * bounds.height;
        
        vars.set(proxyTable.get("x").varName, bounds.x);
        vars.set(proxyTable.get("y").varName, bounds.y);
        vars.set(proxyTable.get("width").varName, bounds.width);
        vars.set(proxyTable.get("height").varName, bounds.height);
        vars.set(proxyTable.get("size").varName, refImgSize);
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

                numPixelsProcessed++ ;
                
                // @todo remove this hack
                if (firstImg) {
                    vars.set("_x", h.x);
                    vars.set("_y", h.y);
                    vars.set("_col", h.x + 1);
                    vars.set("_row", h.y + 1);
                    firstImg = false;
                    
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
