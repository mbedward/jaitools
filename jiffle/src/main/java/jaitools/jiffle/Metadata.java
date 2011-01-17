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

import jaitools.jiffle.parser.VarClassifier;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A container for information about variables gathered by 
 * {@link jaitools.jiffle.parser.VarClassifier}. Not intended for
 * use in client code.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class Metadata {
    
    private Map<String, Jiffle.ImageRole> imageParams;
    private Rectangle outImgBounds;
    private Set<String> outImgVars;
    private Set<String> userVars;

    /**
     * Package private constructor
     * 
     * @param imageParams Map of variable : image object supplied by the user
     * with the script
     */
    Metadata(Map<String, Jiffle.ImageRole> imageParams) {
        this.imageParams = imageParams;
    }

    /**
     * Collect data on variables gathered by {@link jaitools.jiffle.parser.VarClassifier}
     * 
     * @param classifier the VarClassifier object
     */
    public void setVarData(VarClassifier classifier) {
        outImgVars = classifier.getOutputImageVars();
        userVars = classifier.getUserVars();
    }
    
    /**
     * Returns an unmodifiable view of the image parameters
     */
    public Map<String, Jiffle.ImageRole> getImageParams() {
        return Collections.unmodifiableMap(imageParams);
    }
    
    /**
     * Retuns the image variable names as an unmodifiable Set
     */
    public Set<String> getImageVars() {
        return Collections.unmodifiableSet(imageParams.keySet());
    }
    
    /**
     * Returns the names of output image variables as an unmodifiable Set
     */
    public Set<String> getOutputImageVars() {
        return Collections.unmodifiableSet(outImgVars);
    }

}
