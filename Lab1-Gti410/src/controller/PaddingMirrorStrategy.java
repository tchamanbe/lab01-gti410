package controller;

import model.*;

/**
 * <p>Title: PaddingMirrorStrategy</p>
 * <p>Description: Padding strategy where values are mirrored at the border if Pixel values are out of range.</p>
 * <p>Company: ETS - École de Technologie Supérieure</p>
 * @author Dominique Jacques-Brissette
 * @version $Revision: 1.0 $
 */
public class PaddingMirrorStrategy extends PaddingStrategy {
	
	/**
	 * Returns and validates the Pixel at the specified coordinate.
	 * If the Pixel is invalid, return a mirrored value
	 * @param image source Image
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return the validated Pixel value at the specified coordinates 
	 */
	public Pixel pixelAt(ImageX image, int x, int y) {
		if ( x < 0 ) {
			x = -1-x; // mirror the left side
		} else if ( x >= image.getImageWidth() ) {
			x = 2*image.getImageWidth() - x - 1; // mirror the right side
		}
		
		if ( y < 0 ) {
			y = -1-y; // mirror the top side
		} else if ( y >= image.getImageHeight() ) {
			y = 2*image.getImageHeight() - y - 1; // mirror the bottom side
		}
		
		return image.getPixel( x, y );
		
	}

	/**
	 * Returns and validates the PixelDouble at the specified coordinate.
	 * Original Pixel is converted to PixelDouble.
	 * If the Pixel is invalid, a new black (0,0,0,0) PixelDouble is returned.
	 * @param image source ImageDouble
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return the validated PixelDouble value at the specified coordinates
	 */	
	public PixelDouble pixelAt(ImageDouble image, int x, int y) {
		if ( x < 0 ) {
			x = -1-x; // mirror the left side
		} else if ( x >= image.getImageWidth() ) {
			x = 2*image.getImageWidth() - x - 1; // mirror the right side
		}
		
		if ( y < 0 ) {
			y = -1-y; // mirror the top side
		} else if ( y >= image.getImageHeight() ) {
			y = 2*image.getImageHeight() - y - 1; // mirror the bottom side
		}
		
		return image.getPixel( x, y );
	}
}
