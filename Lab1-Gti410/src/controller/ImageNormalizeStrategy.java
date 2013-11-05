package controller;

import model.*;

/**
 * <p>Title: ImageNormalizeStrategy</p>
 * <p>Description: Image-related strategy</p>
 * <p>Company: ETS - École de Technologie Supérieure</p>
 * @author Dominique Jacques-Brissette
 * @version $Revision: 1.0 $
 */
public class ImageNormalizeStrategy extends ImageConversionStrategy {
	/**
	 * Converts an ImageDouble to an ImageX using a normalization strategy (0-255).
	 * We normalize each color channel separately.
	 * We do not process/normalize the ALPHA channel
	 */
	public ImageX convert(ImageDouble image) {
		int imageWidth = image.getImageWidth();
		int imageHeight = image.getImageHeight();
		ImageX newImage = new ImageX(0, 0, imageWidth, imageHeight);
		PixelDouble curPixelDouble = null;
		double 	minRed 	 = 255,	maxRed = 0,
				minGreen = 255, maxGreen = 0,
				minBlue  = 255,	maxBlue = 0;
		
		for (int x = 0; x < imageWidth; x++) {
			for (int y = 0; y < imageHeight; y++) {
				curPixelDouble = image.getPixel(x,y);
				
				// Getting MIN value for each channel
				if ( curPixelDouble.getRed() < minRed ) {		// red
					minRed = curPixelDouble.getRed();
				}
				if ( curPixelDouble.getGreen() < minGreen ) {	// green
					minGreen = curPixelDouble.getGreen();
				}
				if ( curPixelDouble.getBlue() < minBlue ) {		// blue
					minBlue = curPixelDouble.getBlue();
				}
				
				// Getting MAX value for each channel
				if ( curPixelDouble.getRed() > maxRed ) {		// red
					maxRed = curPixelDouble.getRed();
				}
				if ( curPixelDouble.getGreen() > maxGreen ) {	// green
					maxGreen = curPixelDouble.getGreen();
				}
				if ( curPixelDouble.getBlue() > maxBlue ) {		// blue
					maxBlue = curPixelDouble.getBlue();
				}
			}
		}
		
		
		newImage.beginPixelUpdate();
		for (int x = 0; x < imageWidth; x++) {
			for (int y = 0; y < imageHeight; y++) {
				curPixelDouble = image.getPixel(x,y);
				
				newImage.setPixel(x, y, new Pixel((int)(normalize0to255(curPixelDouble.getRed(),	minRed,		maxRed )),
												  (int)(normalize0to255(curPixelDouble.getGreen(), 	minGreen,	maxGreen )),
												  (int)(normalize0to255(curPixelDouble.getBlue(), 	minBlue,	maxBlue )),
												  (int)(curPixelDouble.getAlpha())));
			}
		}
		newImage.endPixelUpdate();
		return newImage;
	}
	
	/**
	 * Uses the min and max value for a specific color channel
	 * in order to determine the new normalized value (over the 0-255 range)
	 * @param value The pixel color original value
	 * @param min The minimum pixel color value in the whole image
	 * @param max The maximum pixel color value in the whole image
	 * @return
	 */
	private double normalize0to255( double value, double min, double max ) {	
		return ((value-min)/(max-min))*255;
	}
}