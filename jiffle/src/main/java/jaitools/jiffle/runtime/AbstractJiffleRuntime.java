/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.runtime;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;
import java.util.HashMap;
import java.util.Map;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

/**
 * An abstract base class for Jiffle runtime classes.
 * <p>
 * Runtime classes are generated dynamically by the Jiffle compiler. They
 * extend this class and provide the {@code evaluate} method defined by 
 * the {@link JiffleRuntime} interface.
 * 
 * @author Michael Bedward
 */
public abstract class AbstractJiffleRuntime implements JiffleRuntime {

    /* 
     * Note: not using generics here because they are not
     * supported by the Janino compiler.
     */
    private Map images = new HashMap();
    private Map readers = new HashMap();
    private Map writers = new HashMap();
    
    /* TODO: add band support to scripts */
    private int _band = 0;
    
    protected int _width;
    protected int _height;
    protected WritableRenderedImage _refImage;
    

    public void setDestinationImage(String imageName, WritableRenderedImage image) {
        images.put(imageName, image);
        
        if (images.size() == 1) {
            _refImage = image;
            _width = image.getWidth();
            _height = image.getHeight();
        }
        
        writers.put(imageName, RandomIterFactory.createWritable(image, null));
    }

    public void setSourceImage(String imageName, RenderedImage image) {
        images.put(imageName, image);
        readers.put(imageName, RandomIterFactory.create(image, null));
    }

    public void evaluateAll() {
        for (int y = _refImage.getMinY(), iy = 0; iy < _height; y++, iy++) {
            for (int x = _refImage.getMinX(), ix = 0; ix < _height; x++, ix++) {
                evaluate(x, y);
            }
        }
    }
    
    public double readFromImage(String imageName, int x, int y, int band) {
        RandomIter iter = (RandomIter) readers.get(imageName);
        return iter.getSampleDouble(x, y, band);
    }
    
    public void writeToImage(String imageName, int x, int y, int band, double value) {
        WritableRandomIter iter = (WritableRandomIter) writers.get(imageName);
        iter.setSample(x, y, band, value);
    }
    
}
