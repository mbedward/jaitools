/*
 * Copyright 2011 Michael Bedward
 *
 * This file is part of jai-tools.
 *
 * jai-tools is free Weakware: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Weakware Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jaitools.jiffle;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

import javax.media.jai.TiledImage;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.runtime.JiffleDirectRuntime;

/**
 * A helper class to compile and run Jiffle scripts while avoiding Jiffle's
 * boiler-plate code.
 * <p>
 * When working with Jiffle objects directly you end up writing a certain 
 * amount of boiler-plate code for image parameters etc. JiffleBuilder offers
 * concise, chained methods to help you get your imges with fewer keystrokes.
 * <p>
 * Here is an example of creating a Jiffle object and retrieving the runtime
 * instance 'by hand'...
 * <pre><code>
 * // A script to sum values from two source images
 * String sumScript = "dest = foo + bar;" ;
 *
 * // Image parameters
 * Map&lt;String, Jiffle.ImageRole.SOURCE&gt; imageParams = CollectionFactory.map();
 * imageParams.put("dest", Jiffle.ImageRole.DEST);
 * imageParams.put("foo", Jiffle.ImageRole.SOURCE);
 * imageParams.put("bar", Jiffle.ImageRole.SOURCE);
 *
 * // Create a compiled Jiffle object
 * Jiffle jiffle = new Jiffle(sumScript, imageParams);
 *
 * // Get the runtime object
 * JiffleDirectRuntime runtime = jiffle.getRuntimeInstance();
 *
 * // Set the source images
 * RenderedImage fooImg = ...
 * RenderedImage barImg = ...
 * runtime.setSourceImage("foo", fooImg);
 * runtime.setSourceImage("bar", barImg);
 *
 * // Create an image for the results and submit it to
 * // the runtime object
 * WritableRenderedImage destImg = ImageUtils.createConstantImage(
 *         fooImg.getWidth(), fooImg.getHeight, 0d);
 * runtime.setDestinationImage("dest", destImg);
 *
 * // Now run the script
 * runtime.evaluateAll(null);
 * </code></pre>
 * Now here is the same task done using JiffleBuilder...
 * <pre><code>
 * // A script to sum values from two source images
 * String sumScript = "dest = foo + bar;" ;
 *
 * RenderedImage fooImg = ...
 * RenderedImage barImg = ...
 *
 * JiffleBuilder jb = new JiffleBuilder();
 * jb.script(sumScript).source("foo", fooImg).script("bar", barImg);
 *
 * // We can get the builder to create the destination image for us
 * jb.dest("dest", fooImg.getWidth(), fooImg.getHeight());
 *
 * // Run the script
 * jb.getRuntime().run();
 *
 * // Since we asked the builder to create the destination image we
 * // now need to get a reference to it
 * RenderedImage destImg = jb.getImage("dest");
 * </code></pre>
 * When a script does not use any source images, {@code JiffleBuilder} makes
 * for very concise code...
 * <pre><code>
 * String script = "waves = sin( 4 * M_PI * x() / width() );" ;
 * JiffleBuilder jb = new JiffleBuilder();
 * RenderedImage wavesImg = jb.script(script).dest("waves", 500, 200).run().getImage("waves");
 * </code></pre>
 * 
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class JiffleBuilder {

    private static class ImageRef {
        Object ref;
        boolean weak;

        ImageRef(RenderedImage image, boolean weak) {
            if (weak) {
                ref = new WeakReference<RenderedImage>(image);
            } else {
                ref = image;
            }
            this.weak = weak;
        }

        RenderedImage get() {
            if (weak) {
                RenderedImage image = ((WeakReference<RenderedImage>) ref).get();
                return image;
            } else {
                return (RenderedImage) ref;
            }
        }
    }

    private String script;
    private final Map<String, Jiffle.ImageRole> imageParams;
    private final Map<String, ImageRef> images;

    /**
     * Creates a new JiffleBuilder instance.
     */
    public JiffleBuilder() {
        imageParams = CollectionFactory.orderedMap();
        images = CollectionFactory.orderedMap();
    }

    /**
     * Clears all attributes in this builder. If destination images
     * were created using the {@code dest} methods with image bounds
     * arguments they will also be freed.
     */
    public void clear() {
        script = null;
        imageParams.clear();
        images.clear();
    }

    /**
     * Sets the script.
     *
     * @param script the script
     *
     * @return the instance of this class to allow method chaining
     */
    public JiffleBuilder script(String script) {
        this.script = script;
        return this;
    }

    /**
     * Sets the script to the contents of the {@code scriptFile}.
     *
     * @param scriptFile file containing the script
     *
     * @return the instance of this class to allow method chaining
     * @throws JiffleException if there were problems reading the file
     */
    public JiffleBuilder script(File scriptFile) throws JiffleException {
        script = readScriptFile(scriptFile);
        return this;
    }

    /**
     * Sets a source image associated with a variable name in the script.
     * The image will be stored by the builder as a weak reference.
     *
     * @param varName variable name
     * @param sourceImage the source image
     * @return the instance of this class to allow method chaining
     */
    public JiffleBuilder source(String varName, RenderedImage sourceImage) {
        imageParams.put(varName, Jiffle.ImageRole.SOURCE);
        // store as weak reference
        images.put(varName, new ImageRef(sourceImage, true));
        return this;
    }

    /**
     * Creates a new destination image and associates it with a variable name
     * in the script.
     * <p>
     * Note: a {@code JiffleBuilder} maintains only {@code WeakReferences}
     * to all source images and any destination images passed to it via
     * the {@link #dest(String, WritableRenderedImage)} method. However,
     * a strong reference is stored to the destination images created with this
     * method. This can be freed later by calling {@link #clear()} or
     * {@link #removeImage(String varName)}.
     *
     * @param varName variable name
     * @param destBounds the bounds of the new destination image
     *
     * @return the instance of this class to allow method chaining
     */
    public JiffleBuilder dest(String varName, Rectangle destBounds) {
        if (destBounds == null || destBounds.isEmpty()) {
            throw new IllegalArgumentException("destBounds argument cannot be null or empty");
        }

        return dest(varName, destBounds.x, destBounds.y, destBounds.width, destBounds.height);
    }

    /**
     * Creates a new destination image and associates it with a variable name
     * in the script. The minimum pixel X and Y ordinates of the destination
     * image will be 0.
     * <p>
     * Note: a {@code JiffleBuilder} maintains only {@code WeakReferences}
     * to all source images and any destination images passed to it via
     * the {@link #dest(String, WritableRenderedImage)} method. However,
     * a strong reference is stored to the destination images created with this
     * method. This can be freed later by calling {@link #clear()} or
     * {@link #removeImage(String varName)}.
     *
     * @param varName variable name
     * @param width image width (pixels)
     * @param height image height (pixels)
     *
     * @return the instance of this class to allow method chaining
     */
    public JiffleBuilder dest(String varName, int width, int height) {
        return dest(varName, 0, 0, width, height);
    }

    /**
     * Creates a new destination image and associates it with a variable name
     * in the script.
     * <p>
     * Note: a {@code JiffleBuilder} maintains only {@code WeakReferences}
     * to all source images and any destination images passed to it via
     * the {@link #dest(String, WritableRenderedImage)} method. However,
     * a strong reference is stored to the destination images created with this
     * method. This can be freed later by calling {@link #clear()} or
     * {@link #removeImage(String varName)}.
     *
     * @param varName variable name
     * @param minx minimum pixel X ordinate
     * @param miny minimum pixel Y ordinate
     * @param width image width (pixels)
     * @param height image height (pixels)
     *
     * @return the instance of this class to allow method chaining
     */
    public JiffleBuilder dest(String varName, int minx, int miny, int width, int height) {
        TiledImage image = ImageUtils.createConstantImage(minx, miny, width, height, 0d);
        imageParams.put(varName, Jiffle.ImageRole.DEST);
        // store as strong reference
        images.put(varName, new ImageRef(image, false));
        return this;
    }

    /**
     * Sets a destination image associated with a variable name in the script.
     * <p>
     * Note: The builder will only hold a Weak reference to {@code destImg} so
     * it's not a good idea to create an image on the fly when calling this
     * method...
     * <pre><code>
     * // creating image on the fly
     * builder.dest("foo", ImageUtils.createConstantImage(width, height, 0d));
     *
     * // later... img will be null here
     * RenderedImage img = builder.getImage("foo");
     * </code></pre>
     * To avoid this problem, create your image locally...
     * <pre><code>
     * WritableRenderedImage img = ImageUtils.createConstantImage(width, height, 0d);
     * builder.dest("foo", img);
     * </code></pre>
     * Or use on of the {@code dest} methods with image bounds arguments to
     * create it for you
     * <pre><code>
     * builder.dest("foo", width, height);
     *
     * // later... we will get a valid reference to the image
     * RenderedImage img = builder.getImage("foo");
     * </code></pre>
     *
     * @param varName variable name
     * @param destImage the destination image
     *
     * @return the instance of this class to allow method chaining
     */
    public JiffleBuilder dest(String varName, WritableRenderedImage destImage) {
        imageParams.put(varName, Jiffle.ImageRole.DEST);
        // store as weak reference
        images.put(varName, new ImageRef(destImage, true));
        return this;
    }
    
    /**
     * Runs the script. Equivalent to calling 
     * {@code builder.getRuntime().evaluateAll(null)}.
     * 
     * @return the instance of this class to allow method chaining
     * 
     * @throws JiffleException if the script has not been set yet or if
     *         compilation errors occur
     */
    public JiffleBuilder run() throws JiffleException {
        getRuntime().evaluateAll(null);
        return this;
    }

    /**
     * Creates a runtime object for the currently set script and images.
     *
     * @return an instance of {@link JiffleDirectRuntime}
     *
     * @throws JiffleException if the script has not been set yet or if
     *         compilation errors occur
     */
    public JiffleDirectRuntime getRuntime() throws JiffleException {
        if (script == null) {
            throw new IllegalStateException("Jiffle script has not been set yet");
        }

        Jiffle jiffle = new Jiffle(script, imageParams);
        JiffleDirectRuntime runtime = jiffle.getRuntimeInstance();
        for (String var : images.keySet()) {
            RenderedImage img = images.get(var).get();
            if (img == null) {
                throw new JiffleException(
                        "Image for variable " + var + " has been garbage collected");
            }

            switch (imageParams.get(var)) {
                case SOURCE:
                    runtime.setSourceImage(var, img);
                    break;

                case DEST:
                    runtime.setDestinationImage(var, (WritableRenderedImage)img);
                    break;
            }
        }

        return runtime;
    }

    /**
     * Get an image associated with a script variable name. The image must
     * have been previously suppolied to the builder using the (@code source}
     * method or one of the {@code dest} methods.
     * <p>
     * In the case of a destination image the object returned can be cast
     * to {@link WritableRenderedImage}.
     * 
     * @param varName variable name
     * 
     * @return the associated image or {@code null} if the variable name is
     *         not recognized or the image has since been garbage collected
     */
    public RenderedImage getImage(String varName) {
        ImageRef ref = images.get(varName);
        if (ref != null) {
            return ref.get();
        }
        return null;
    }

    /**
     * Removes an image associated with a script variable name. The image should
     * have been previously suppolied to the builder using the (@code source}
     * method or one of the {@code dest} methods.
     * <p>
     * In the case of a destination image the object returned can be cast
     * to {@link WritableRenderedImage}.
     *
     * @param varName variable name
     *
     * @return the associated image or {@code null} if the variable name is
     *         not recognized or the image has since been garbage collected
     */
    public RenderedImage removeImage(String varName) {
        ImageRef ref = images.remove(varName);
        if (ref != null) {
            return ref.get();
        }
        return null;
    }

    private String readScriptFile(File scriptFile) throws JiffleException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(scriptFile));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    sb.append(line);
                    sb.append('\n');  // put the newline back on for the parser
                }
            }

            return sb.toString();

        } catch (IOException ex) {
            throw new JiffleException("Could not read the script file", ex);

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

}
