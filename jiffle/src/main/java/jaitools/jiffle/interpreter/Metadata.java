/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.interpreter;

import jaitools.jiffle.parser.VarClassifier;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Michael Bedward
 */
public class Metadata {
    
    private Map<String, RenderedImage> imageParams;
    private Rectangle outImgBounds;
    private Set<String> positionalVars;
    private Set<String> userVars;

    
    public Metadata(Map<String, RenderedImage> imageParams) {
        this.imageParams = imageParams;
    }

    public Rectangle getOutImageBounds() {
        return new Rectangle(outImgBounds);
    }

    public void setOutImageBounds(Rectangle outImgBounds) {
        this.outImgBounds = new Rectangle(outImgBounds);
    }

    public void setVarData(VarClassifier classifier) {
        positionalVars = classifier.getPositionalVars();
        userVars = classifier.getUserVars();
    }
    
    public Set<String> getImageVars() {
        return Collections.unmodifiableSet(imageParams.keySet());
    }

    public Set<String> getPositionalVars() {
        return Collections.unmodifiableSet(positionalVars);
    }

    public Set<String> getUserVars() {
        return Collections.unmodifiableSet(userVars);
    }

    
}
