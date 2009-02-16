/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.interpreter;

import jaitools.jiffle.parser.VarClassifier;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.media.jai.TiledImage;

/**
 *
 * @author Michael Bedward
 */
public class Metadata {
    
    private Map<String, TiledImage> imageParams;
    private Rectangle outImgBounds;
    private Set<String> outImgVars;
    private Set<String> localVars;
    private Set<String> positionalVars;
    private Set<String> userVars;

    
    public Metadata(Map<String, TiledImage> imageParams) {
        this.imageParams = imageParams;
    }

    public Rectangle getOutImageBounds() {
        return new Rectangle(outImgBounds);
    }

    public void setOutImageBounds(Rectangle outImgBounds) {
        this.outImgBounds = new Rectangle(outImgBounds);
    }

    public void setVarData(VarClassifier classifier) {
        outImgVars = classifier.getOutputImageVars();
        localVars = classifier.getLocalVars();
        positionalVars = classifier.getPositionalVars();
        userVars = classifier.getUserVars();
    }
    
    public Map<String, TiledImage> getImageParams() {
        return Collections.unmodifiableMap(imageParams);
    }
    
    public Set<String> getImageVars() {
        return Collections.unmodifiableSet(imageParams.keySet());
    }
    
    public Set<String> getLocalVars() {
        return Collections.unmodifiableSet(localVars);
    }

    public Set<String> getOutputImageVars() {
        return Collections.unmodifiableSet(outImgVars);
    }

    public Set<String> getPositionalVars() {
        return Collections.unmodifiableSet(positionalVars);
    }

    public Set<String> getUserVars() {
        return Collections.unmodifiableSet(userVars);
    }

    
}
