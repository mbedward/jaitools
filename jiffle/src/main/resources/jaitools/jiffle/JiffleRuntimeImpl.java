package jaitools.jiffle;

import jaitools.jiffle.runtime.JiffleRuntime;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;
import java.util.HashMap;
import java.util.Map;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

public class JiffleRuntimeImpl implements JiffleRuntime {
    
    /* 
     * Note not using generics here because they are not
     * supported by Janino.
     */
    private Map images = new HashMap();
    private Map readers = new HashMap();
    private Map writers = new HashMap();
    
    private double _width;
    private double _height;
    
    public void evaluate(int _x, int _y, int _band) {
        // COMPILER_BREAK
        
        throw new UnsupportedOperationException("Method body to be provided by Jiffle compiler");
        
        // COMPILER_RESUME
    }
    
    public double readFromImage(String imageName, int x, int y, int band) {
        RandomIter iter = (RandomIter) readers.get(imageName);
        return iter.getSampleDouble(x, y, band);
    }
    
    public void writeToImage(String imageName, int x, int y, int band, double value) {
        WritableRandomIter iter = (WritableRandomIter) writers.get(imageName);
        iter.setSample(x, y, band, value);
    }
    
    public void setDestinationImage(String imageName, WritableRenderedImage image) {
        images.put(imageName, image);
        
        if (images.size() == 1) {
            _width = image.getWidth();
            _height = image.getHeight();
        }
        
        writers.put(imageName, RandomIterFactory.createWritable(image, null));
    }

    public void setSourceImage(String imageName, RenderedImage image) {
        images.put(imageName, image);
        readers.put(imageName, RandomIterFactory.create(image, null));
    }

}
