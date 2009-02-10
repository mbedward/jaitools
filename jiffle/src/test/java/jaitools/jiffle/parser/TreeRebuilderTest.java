/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.parser;

import jaitools.jiffle.interpreter.Metadata;
import java.awt.image.RenderedImage;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Bedward and Murray Ellis
 */
public class TreeRebuilderTest extends TreeWalkerTestBase {

    @Test
    public void testSimpleRebuild() throws Exception {
        System.out.println("testSimpleRebuild");
        
        String input = 
                "w = width();" +
                "h = height();" +
                "a = 1.0" +
                "b = 2.0" +
                "curx = x();" +
                "cury = y();" +
                "result = a * w + h + curx + cury;" ; // weird image :-)
        
        VarClassifier classifier = new VarClassifier(getAST(input));
        classifier.setImageVars(Collections.singleton("result"));
        classifier.start();
        
        assertFalse(classifier.hasError());  // just in case I've stuffed up the input string
        
        Metadata metadata = new Metadata(Collections.singletonMap("result", (RenderedImage)null));
        metadata.setVarData(classifier);
        
        TreeRebuilder rebuilder = new TreeRebuilder(getAST(input));
        rebuilder.setMetadata(metadata);
        rebuilder.setPrint(true);
        rebuilder.start();
        
        assertTrue(true);
    }
}
