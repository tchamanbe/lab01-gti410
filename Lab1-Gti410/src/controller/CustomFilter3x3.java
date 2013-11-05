package controller;

import model.*;

/**
 * <p>Title: CustomFilter3x3</p>
 * <p>Description: A 3x3 filter using custom (arbitrary) kernel values.</p>
 * <p>Company: ETS - École de Technologie Supérieure</p>
 * @author Dominique Jacques-Brissette
 * @version $Revision: 1.00 $
 */
public class CustomFilter3x3 extends Filter {	
	private double filterMatrix[][] = null;
	
	/**
	 * Default constructor.
	 * @param paddingStrategy PaddingStrategy used 
	 * @param conversionStrategy ImageConversionStrategy used
	 */
	public CustomFilter3x3(PaddingStrategy paddingStrategy, 
						 ImageConversionStrategy conversionStrategy) {
		super(paddingStrategy, conversionStrategy);	
		filterMatrix = new double[3][3];
		
		filterMatrix[0][0] = filterMatrix[1][0] = filterMatrix[2][0] = 
		filterMatrix[0][1] = filterMatrix[1][1] = filterMatrix[2][1] =
		filterMatrix[0][2] = filterMatrix[1][2] = filterMatrix[2][2] = 0.d;
	}
	
	/**
	 * Filters an ImageX and returns a ImageDouble.
	 */
	public ImageDouble filterToImageDouble(ImageX image) {
		ImageDouble id = conversionStrategy.convert(image);
		ImageDouble filtered = filter(id);
		
		return filtered;
	}
	
	/**
	 * Filters an ImageDouble and returns a ImageDouble.
	 */	
	public ImageDouble filterToImageDouble(ImageDouble image) {
		return filter(image);
	}
	
	/**
	 * Filters an ImageX and returns an ImageX.
	 */	
	public ImageX filterToImageX(ImageX image) {
		ImageDouble filtered = filter(conversionStrategy.convert(image)); 
		return conversionStrategy.convert(filtered);
	}
	
	/**
	 * Filters an ImageDouble and returns a ImageX.
	 */	
	public ImageX filterToImageX(ImageDouble image) {
		ImageDouble filtered = filter(image); 
		return conversionStrategy.convert(filtered);		
	}
	
	/*
	 * Filter Implementation 
	 */
	private ImageDouble filter(ImageDouble image) {
		System.out.println(filterMatrix[0][0] + " " + filterMatrix[0][1] + " " + filterMatrix[0][2]);
		System.out.println(filterMatrix[1][0] + " " + filterMatrix[1][1] + " " + filterMatrix[1][2]);
		System.out.println(filterMatrix[2][0] + " " + filterMatrix[2][1] + " " + filterMatrix[2][2]);
		int imageWidth = image.getImageWidth();
		int imageHeight = image.getImageHeight();
		ImageDouble newImage = new ImageDouble(imageWidth, imageHeight);
		PixelDouble newPixel = null;
	
		double resultRed = 0; 
		double resultGreen = 0; 
		double resultBlue = 0; 
		PixelDouble p;
		System.out.println("filter = " + filterMatrix[0][0]);
		for (int x = 0; x < imageWidth; x++) {
			for (int y = 0; y < imageHeight; y++) {
				newPixel = new PixelDouble();
				resultRed = 0;
				resultGreen = 0;
				resultBlue = 0;
			
				//*******************************
				// Convolution
				for (int i = 0; i <= 2; i++) {
					for (int j = 0; j <= 2; j++) {
						p = getPaddingStrategy().pixelAt(image,x+(i-1),y+(j-1));
						
						resultRed   += filterMatrix[i][j] * p.getRed();
						resultGreen += filterMatrix[i][j] * p.getGreen();
						resultBlue  += filterMatrix[i][j] * p.getBlue();
					}
				}
				
				newPixel.setRed(resultRed);
				newPixel.setGreen(resultGreen);
				newPixel.setBlue(resultBlue);
							
				//*******************************
				// Alpha - Untouched in this filter
				newPixel.setAlpha(getPaddingStrategy().pixelAt(image, x,y).getAlpha());
							 
				//*******************************
				// Done
				newImage.setPixel(x, y, newPixel);
			}
		}
		
		return newImage;
	}
	
	/**
	 * Updates the kernel values
	 * @param _coordinates
	 * @param _value
	 */
	public void updateKernel(Coordinates _coordinates, float _value) {
		this.filterMatrix[ _coordinates.getColumn()-1 ][ _coordinates.getRow()-1 ] = _value;
	}
}
