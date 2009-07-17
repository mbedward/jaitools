/*
 * Copyright 2009 Michael Bedward
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

package jaitools.tiledimage;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.RenderableImage;
import java.lang.reflect.Method;
import java.text.AttributedCharacterIterator;
import java.util.Hashtable;
import java.util.Map;
import javax.media.jai.PlanarImage;

/**
 * A Graphics class for drawing into a <code>DiskMemImage</code>.
 * As with JAI's <code>TiledImageGraphics</code> class, java.awt
 * routines do the real work and the purpose of this class is to
 * serve the image data in a form that those routines can handle.
 *
 * @todo It would have been a lot easier to be able to use JAI's
 * TiledImageGraphics class directly but, for some reason, they've
 * made it package private.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class DiskMemImageGraphics extends Graphics2D {

    private DiskMemImage targetImage;
    private ColorModel   colorModel;
    private Hashtable<String, Object> properties;
    private RenderingHints renderingHints;

    public static enum PaintMode {
        PAINT,
        XOR;
    };
    
    /*
     * java.awt.Graphics parameters
     */
    private Point origin;
    private Shape clip;
    private Color color;
    private Font  font;
    private PaintMode paintMode;
    private Color XORColor;

    /*
     * java.awt.Graphics2D parameters
     */
    private Color background;
    private Composite composite;
    private Paint paint;
    private Stroke stroke;
    private AffineTransform transform;

    public static enum OpType {
        DRAW_BUFFERED_IMAGE("drawImage", BufferedImage.class, BufferedImageOp.class, int.class, int.class),
        DRAW_IMAGE("drawImage", Image.class, AffineTransform.class, ImageObserver.class),
        DRAW_SHAPE("draw", Shape.class);

        private String methodName;
        private Class<?>[] paramTypes;

        private OpType(String methodName, Class<?> ...types) {
            this.methodName = methodName;
            this.paramTypes = new Class<?>[types.length];
            for (int i = 0; i < types.length; i++) {
                this.paramTypes[i] = types[i];
            }
        }

        public String getMethodName() {
            return methodName;
        }

        public int getNumParams() {
            return paramTypes.length;
        }

        public Class<?>[] getParamTypes() {
            return paramTypes;
        }
    }

    /**
     * Constructor: create an instance of this class for the given target image
     *
     * @param targetImage the image to be drawn into
     */
    DiskMemImageGraphics(DiskMemImage targetImage) {
        this.targetImage = targetImage;
        setColorModel();
        setProperties();
        setGraphicsParams();
    }

    @Override
    public void draw(Shape s) {
        doDraw(OpType.DRAW_SHAPE, s.getBounds2D(), s);
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        Rectangle bounds = new Rectangle(0, 0, img.getWidth(obs), img.getHeight(obs));
        Rectangle2D xformBounds = xform.createTransformedShape(bounds).getBounds2D();

        return doDraw(OpType.DRAW_IMAGE, xformBounds, img, xform, obs);
    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void drawString(String str, int x, int y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void drawString(String s, float x, float y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fill(Shape s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setComposite(Composite comp) {
        composite = comp;
    }

    @Override
    public void setPaint(Paint p) {
        paint = p;
    }

    @Override
    public void setStroke(Stroke s) {
        stroke = s;
    }

    @Override
    public void setRenderingHint(Key hintKey, Object hintValue) {
        renderingHints.clear();
        renderingHints.put(hintKey, hintValue);
    }

    @Override
    public Object getRenderingHint(Key hintKey) {
        return renderingHints.get(hintKey);
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
        renderingHints.putAll(hints);
    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
        renderingHints.putAll(hints);
    }

    @Override
    public RenderingHints getRenderingHints() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void translate(int x, int y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void translate(double tx, double ty) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void rotate(double theta) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void rotate(double theta, double x, double y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void scale(double sx, double sy) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void shear(double shx, double shy) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void transform(AffineTransform Tx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTransform(AffineTransform Tx) {
        transform = Tx;
    }

    @Override
    public AffineTransform getTransform() {
        return transform;
    }

    @Override
    public Paint getPaint() {
        return paint;
    }

    @Override
    public Composite getComposite() {
        return composite;
    }

    @Override
    public void setBackground(Color color) {
        background = color;
    }

    @Override
    public Color getBackground() {
        return background;
    }

    @Override
    public Stroke getStroke() {
        return stroke;
    }

    @Override
    public void clip(Shape s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns a copy of this object with its current settings
     * @return a new instance of this class
     */
    @Override
    public Graphics create() {
        DiskMemImageGraphics gr = new DiskMemImageGraphics(targetImage);
        copyGraphicsParams(gr);
        return gr;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setColor(Color c) {
        color = c;
    }

    @Override
    public void setPaintMode() {
        paintMode = PaintMode.PAINT;
    }

    @Override
    public void setXORMode(Color color) {
        paintMode = PaintMode.XOR;
        XORColor = color;
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public void setFont(Font font) {
        this.font = font;
    }

    @Override
    public FontMetrics getFontMetrics(Font f) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Rectangle getClipBounds() {
        return clip.getBounds();
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Shape getClip() {
        return clip;
    }

    @Override
    public void setClip(Shape clip) {
        this.clip = clip;
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void dispose() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Perform the graphics operation by partitioning the work across the image's
     * tiles and using Graphics2D routines to draw into each tile.
     *
     * @param opType the type of operation
     * @param bounds bounds of the element to be drawn
     * @param args a variable length list of arguments for the operation
     */
    boolean doDraw(OpType opType, Rectangle2D bounds, Object ...args) {
        try {
            Method method = Graphics2D.class.getMethod(opType.getMethodName(), opType.getParamTypes());

        } catch (NoSuchMethodException nsmEx) {
            // programmer error :-(
            StringBuffer sb = new StringBuffer();
            sb.append("No method ");
            sb.append(opType.getMethodName());
            sb.append("(");
            if (opType.getNumParams() > 0) {
                sb.append(opType.getParamTypes()[0].getSimpleName());
                for (int i = 1; i < opType.getNumParams(); i++) {
                    sb.append(", ");
                    sb.append(opType.getParamTypes()[i].getSimpleName());
                }
            }
            sb.append(")");
            throw new RuntimeException(sb.toString());
        }

        int minTileX = Math.max(targetImage.XToTileX((int)bounds.getMinX()),
                                targetImage.getMinTileX());

        int maxTileX = Math.min(targetImage.XToTileX((int)(bounds.getMaxX() + 0.5)),
                                targetImage.getMaxTileX());

        int minTileY = Math.max(targetImage.YToTileY((int)bounds.getMinY()),
                                targetImage.getMinTileY());

        int maxTileY = Math.min(targetImage.YToTileY((int)(bounds.getMaxY() + 0.5)),
                                targetImage.getMaxTileY());

        for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
            int minY = targetImage.tileYToY(tileY);

            for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
                int minX = targetImage.tileXToX(tileX);

                WritableRaster tile = targetImage.getWritableTile(tileX, tileY);

                // create a live-copy of the tile with the upper-left corner
                // translated to 0,0
                WritableRaster copy = tile.createWritableTranslatedChild(0, 0);

                BufferedImage bufImg = new BufferedImage(
                        colorModel,
                        copy,
                        colorModel.isAlphaPremultiplied(),
                        properties);

                Graphics2D gr = bufImg.createGraphics();
                copyGraphicsParams(gr);
            }
        }

        return true;
    }

    /**
     * Helper method for the constructor. Attempts to get
     * or construct a <code>ColorModel</code> for the target
     * image.
     *
     * @throws UnsupportedOperationException if a compatible <code>ColorModel</code> is not found
     */
    void setColorModel() {
        assert(targetImage != null);

        colorModel = targetImage.getColorModel();

        if (colorModel == null) {
            SampleModel sm = targetImage.getSampleModel();
            colorModel = PlanarImage.createColorModel(sm);

            if (colorModel == null) {
                // try simple default
                if (ColorModel.getRGBdefault().isCompatibleSampleModel(sm)) {
                    colorModel = ColorModel.getRGBdefault();

                } else {
                    // admit defeat
                    throw new UnsupportedOperationException(
                            "Failed to get or construct a ColorModel for the image");
                }
            }
        }
    }

    /**
     * Helper method for the constructor. Retrieves any properties 
     * set for the target image.
     */
    void setProperties() {
        assert(targetImage != null);

        properties = new Hashtable<String, Object>();
        for (String name : targetImage.getPropertyNames()) {
            properties.put(name, targetImage.getProperty(name));
        }

        // TODO: set rendering hints
    }

    /**
     * Helper method for the constructor. Creates a Graphics2D instance
     * based on the target image's first tile and uses its state to set
     * the graphics params of this object
     */
    void setGraphicsParams() {
        assert(targetImage != null);
        assert(colorModel != null);
        assert(properties != null);

        Raster tile = targetImage.getTile(targetImage.getMinTileX(), targetImage.getMinTileY());
        WritableRaster tiny = tile.createCompatibleWritableRaster(1, 1);

        BufferedImage img = new BufferedImage(
                colorModel, tiny, colorModel.isAlphaPremultiplied(), properties);

        Graphics2D gr = img.createGraphics();

        origin = new Point(0, 0);
        clip = targetImage.getBounds();
        color = gr.getColor();
        font = gr.getFont();

        paintMode = PaintMode.PAINT;
        XORColor = null;

        background = gr.getBackground();
        composite = gr.getComposite();
        paint = null;
        stroke = gr.getStroke();
        transform = gr.getTransform();

        gr.dispose();
    }

    /**
     * Copy the current graphics params into the given <code>Graphics2D</code>
     * object
     *
     * @param gr a Graphics2D object
     */
    void copyGraphicsParams(Graphics2D gr) {
        gr.translate(origin.x, origin.y);
        gr.setClip(clip);
        gr.setColor(getColor());

        if(paintMode == PaintMode.PAINT) {
            gr.setPaintMode();
        } else if (XORColor != null) {
            gr.setXORMode(XORColor);
        }

        gr.setFont(font);

        // java.awt.Graphics2D state
        gr.setBackground(background);
        gr.setComposite(composite);
        if(paint != null) {
            gr.setPaint(paint);
        }
        gr.setRenderingHints(renderingHints);
        gr.setStroke(stroke);
        gr.setTransform(transform);
    }

}
