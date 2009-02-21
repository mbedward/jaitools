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
