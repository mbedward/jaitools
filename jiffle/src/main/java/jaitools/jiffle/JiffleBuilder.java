/*
 * Copyright 2011 Michael Bedward
 *
 * This file is part of jai-tools.
 *
 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
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
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import javax.media.jai.TiledImage;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.runtime.JiffleDirectRuntime;

/**
 * A helper class to create runtime objects from scripts and images with a bit
 * less code than required when working with {@link Jiffle} objects directly.
 * <p>
 * To illustrate its use, we first look at an example of creating a {@code Jiffle}
 * object and retrieving the runtime instance 'by hand'...
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
 * Here is how we would do the same thing with JiffleBuilder...
 * <pre><code>
 * // A script to sum values from two source images
 * String sumScript = "dest = foo + bar;" ;
 *
 * RenderedImage fooImg = ...
 * RenderedImage barImg = ...
 *
 * // Create a builder and use its chaining methods to associate source images
 * // with script variables
 * JiffleBuilder jb = new JiffleBuilder();
 * jb.script(sumScript).source("foo", fooImg).script("bar", barImg);
 *
 * // Get the builder to create the destination image for us
 * jb.dest("dest", fooImg.getWidth(), fooImg.getHeight());
 *
 * // Run the script
 * jb.getRuntime().evaluateAll(null);
 *
 * // Since we asked the builder to create the destination image we
 * // now need to get a reference to it
 * WritableRenderedImage destImg = jb.getImage("dest");
 * </code></pre>
 * When a script does not use any source images, {@code JiffleBuilder} makes
 * for very concise code...
 * <pre><code>
 * String script = "waves = sin( 4 * M_PI * x() / width() );" ;
 * JiffleBuilder jb = new JiffleBuilder();
 * jb.script(script).dest("waves", 500, 200).getRuntime().evaluateAll(null);
 * RenderedImage wavesImg = jb.getImage("waves");
 * </code></pre>
 * 
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class JiffleBuilder {

    private String script;
    private final Map<String, Jiffle.ImageRole> imageParams;
    private final Map<String, WeakReference<RenderedImage>> weakImages;

    private final List<WritableRenderedImage> strongImages;

    /**
     * Creates a new JiffleBuilder instance.
     */
    public JiffleBuilder() {
        imageParams = CollectionFactory.orderedMap();
        weakImages = CollectionFactory.orderedMap();
        strongImages = CollectionFactory.list();
    }

    /**
     * Clears all attributes in this builder. If destination images
     * were created using the {@code dest} methods with image bounds
     * arguments they will also be freed.
     */
    public void clear() {
        script = null;
        imageParams.clear();
        weakImages.clear();
        strongImages.clear();
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
     * Sets a source image associated with a variable name in the script.
     *
     * @param varName variable name
     * @param destImage the source image
     *
     * @return the instance of this class to allow method chaining
     */
    public JiffleBuilder source(String varName, RenderedImage sourceImage) {
        imageParams.put(varName, Jiffle.ImageRole.SOURCE);
        WeakReference<RenderedImage> ref = new WeakReference<RenderedImage>(sourceImage);
        weakImages.put(varName, ref);
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
     * method. This can be freed later by calling {@link #clear()}.
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
     * method. This can be freed later by calling {@link #clear()}.
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
     * method. This can be freed later by calling {@link #clear()}.
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
        TiledImage img = ImageUtils.createConstantImage(minx, miny, width, height, 0d);
        strongImages.add(img);
        return dest(varName, img);
    }

    /**
     * Sets a destination image associated with a variable name in the script.
     * <p>
     * Note: The builder will only hold a weak reference to {@code destImg} so
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
        WeakReference<RenderedImage> ref = new WeakReference<RenderedImage>(destImage);
        weakImages.put(varName, ref);
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
            throw new IllegalStateException("Jiffle script has not be set yet");
        }

        Jiffle jiffle = new Jiffle(script, imageParams);
        JiffleDirectRuntime runtime = jiffle.getRuntimeInstance();
        for (String var : weakImages.keySet()) {
            RenderedImage img = weakImages.get(var).get();
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
        WeakReference<RenderedImage> ref = weakImages.get(varName);
        if (ref != null) {
            return ref.get();
        }

        return null;
    }
}
