package it.geosolutions.media.jai.opimage;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.util.Map;

import javax.media.jai.ImageLayout;
import javax.media.jai.PointOpImage;

public class ColorMaskOpImage extends PointOpImage{

    private Color transparentColor;
    
    private ColorModel originalColorModel;
    
    private boolean workOnColorMap;
    
    private int transparencyType;
    
    private boolean preserveTransparency;
    
    public ColorMaskOpImage(RenderedImage source, Map config,
            ImageLayout layout, Color transparentColor, Boolean preserveTransparency) {
        super(source, layout, config, true);
        this.transparentColor = transparentColor;
        transparencyType = colorModel.getTransparency();
        this.preserveTransparency = preserveTransparency.booleanValue();
        
        originalColorModel = source.getColorModel();
        if (originalColorModel instanceof IndexColorModel){
            workOnColorMap = true;
            initColorMap();
        } else {
            //TODO 
        }
       
        permitInPlaceOperation();
        
    }

    private void initColorMap() {
        IndexColorModel icm = (IndexColorModel)colorModel;
        final int mapSize = icm.getMapSize();
        int[] rgb = new int[mapSize];
        byte[] reds = new byte[mapSize];
        byte[] greens = new byte[mapSize];
        byte[] blues = new byte[mapSize];
        
        icm.getReds(reds);
        icm.getGreens(greens);
        icm.getBlues(blues);

        // Copy the colormap into the RGB array.
        if(icm.hasAlpha()) {
            byte[] alphas = new byte[mapSize];
            icm.getAlphas(alphas);
            for(int i = 0; i < mapSize; i++) {
                rgb[i] =
                    ((alphas[i] & 0xFF) << 24) |
                    ((reds[i] & 0xFF)   << 16) |
                    ((greens[i] & 0xFF) <<  8) |
                    (blues[i] & 0xFF);
            }
        } else {
            for(int i = 0; i < mapSize; i++) {
                rgb[i] =
                    ((reds[i] & 0xFF)   << 16) |
                    ((greens[i] & 0xFF) <<  8) |
                    (blues[i] & 0xFF);
            }
        }
        
        if (transparencyType == ColorModel.OPAQUE){
            
            //TODO: Check the specified color and set the transparent pixel
            colorModel = new IndexColorModel(icm.getPixelSize(), mapSize,
                rgb, 0, icm.hasAlpha(),
                icm.getTransparentPixel(),
                sampleModel.getTransferType());
        } else if (transparencyType == ColorModel.BITMASK){
            //TODO: Update the transparent index
        } else if (transparencyType == ColorModel.TRANSLUCENT){
            
            //TODO: check transparency and update values
        }
    }
    
    private int getColorMapIndex(Color color){
        int index = -1;
        if (originalColorModel instanceof IndexColorModel){
            final int rgbColor = color.getRGB();
            IndexColorModel icm = (IndexColorModel) originalColorModel;
            final int mapSize = icm.getMapSize();
            for (int i=0;i<mapSize;i++){
                if (icm.getRGB(i)==rgbColor){
                    index = i;
                    break;
                }
            }
        }
        return index;
    }
    
}
