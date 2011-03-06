/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jaitools.jiffle.runtime;

import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.JiffleException;
import java.util.Map;

/**
 *
 * @author michael
 */
public class MockJiffle extends Jiffle {
    
    private final int imageWidth;
    private final long pixelTime;

    public MockJiffle(int imageWidth, long pixelTime) {
        this.imageWidth = imageWidth;
        this.pixelTime = pixelTime;
    }
    
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
        return new MockJiffleRuntime(imageWidth, pixelTime);
    }
}
