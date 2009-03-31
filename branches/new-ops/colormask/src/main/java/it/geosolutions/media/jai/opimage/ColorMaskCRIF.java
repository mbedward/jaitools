package it.geosolutions.media.jai.opimage;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.CRIFImpl;
import javax.media.jai.ImageLayout;

import com.sun.media.jai.opimage.RIFUtil;

public class ColorMaskCRIF extends CRIFImpl {

    /** Constructor. */
    public ColorMaskCRIF() {
        super("ColorMask");
    }

    @Override
    public RenderedImage create(ParameterBlock args, RenderingHints renderHints) {
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        return new ColorMaskOpImage(args.getRenderedSource(0), renderHints,
                layout, (Color) args.getObjectParameter(0), (Boolean) args
                        .getObjectParameter(1));

    }

}
