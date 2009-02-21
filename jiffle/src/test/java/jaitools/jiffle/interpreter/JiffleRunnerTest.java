/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.interpreter;

import jaitools.jiffle.JiffleUtilities;
import java.util.HashMap;
import java.util.Map;
import javax.media.jai.TiledImage;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Bedward
 */
public class JiffleRunnerTest {

    public JiffleRunnerTest() {
    }

    @Test
    public void testIsPositionalFunction() {
        System.out.println("isPositionalFunction");
        String[] names = {"x", "y", "row", "col"};
        for (String name : names) {
            assertTrue(JiffleRunner.isPositionalFunction(name));
        }
        
        assertFalse(JiffleRunner.isPositionalFunction("foo"));
    }

    @Test
    public void testIsInfoFunction() {
        System.out.println("isInfoFunction");
        String[] names = {"width", "height"};
        for (String name : names) {
            assertTrue(JiffleRunner.isInfoFunction(name));
        }
        
        assertFalse(JiffleRunner.isInfoFunction("foo"));
    }

    @Test
    public void testRun() throws Exception {
        System.out.println("run");
        TiledImage inImg = JiffleUtilities.createDoubleImage(100, 100, new double[]{10d});
        TiledImage outImg = JiffleUtilities.createDoubleImage();
        
        Map<String, TiledImage> imgParams = new HashMap<String, TiledImage>();
        imgParams.put("out", outImg);
        imgParams.put("in", inImg);
        Jiffle jif = new Jiffle("out = in / 2.0;\n", imgParams);
        if (jif.isCompiled()) {
            JiffleRunner runner = new JiffleRunner(jif);
            runner.run();
        }
    }

}