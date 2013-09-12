package view;

import java.awt.Color;
import java.awt.image.BufferedImage;

import model.ObserverIF;
import model.Pixel;

class HSVColorMediator extends Object implements SliderObserver, ObserverIF {
	ColorSlider hueCS;
	ColorSlider saturationCS;
	ColorSlider valueCS;
	float[] hsv = new float[3];
	float hue;
	float saturation;
	float value;
	BufferedImage hueImage;
	BufferedImage saturationImage;
	BufferedImage valueImage;
	int imagesWidth;
	int imagesHeight;
	ColorDialogResult result;
	
	HSVColorMediator(ColorDialogResult result, int imagesWidth, int imagesHeight) {
		this.imagesWidth = imagesWidth;
		this.imagesHeight = imagesHeight;
		Color.RGBtoHSB(result.getPixel().getRed(), result.getPixel().getGreen(), result.getPixel().getBlue(), hsv);
		this.hue=hsv[0];
		this.saturation=hsv[1];
		this.value=hsv[2];
		
		this.result = result;
		result.addObserver(this);
		
		hueImage = new BufferedImage(imagesWidth, imagesHeight, BufferedImage.TYPE_INT_ARGB);
		saturationImage = new BufferedImage(imagesWidth, imagesHeight, BufferedImage.TYPE_INT_ARGB);
		valueImage = new BufferedImage(imagesWidth, imagesHeight, BufferedImage.TYPE_INT_ARGB);
		computeHueImage(hue, saturation, value);
		computeSaturationImage(hue, saturation, value);
		computeValueImage(hue, saturation, value); 	
	}
	
	
	/*
	 * @see View.SliderObserver#update(double)
	 */
	public void update(ColorSlider s, int v) {
		boolean updateHue = false;
		boolean updateSaturation = false;
		boolean updateValue = false;
		if (s == hueCS && (float)v/255 != hue) {
			hue = (float)v/255;
			updateSaturation = true;
			updateValue = true;
		}
		if (s == saturationCS && (float)v/255 != saturation) {
			saturation = (float)v/255;
			updateHue = true;
			updateValue = true;
		}
		if (s == valueCS && (float)v/255 != value) {
			value = (float)v/255;
			updateHue = true;
			updateSaturation = true;
		}
		if (updateHue) {
			computeHueImage(hue, saturation, value);
		}
		if (updateSaturation) {
			computeSaturationImage(hue, saturation, value);
		}
		if (updateValue) {
			computeValueImage(hue, saturation, value);
		}
		
		int rgb = Color.HSBtoRGB(hue, saturation, value);
		Pixel pixel = new Pixel(rgb);
		result.setPixel(pixel);
	}
	
	public void computeHueImage(float hue, float saturation, float value) { 
		hsv[0]=hue;
		hsv[1]=saturation;
		hsv[2]=value;
		
		for (int i = 0; i<imagesWidth; ++i) {
			hsv[0]=((float)i/(float)imagesWidth);
			int rgb=Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
			for (int j = 0; j<imagesHeight; ++j) {
				hueImage.setRGB(i, j, rgb);
			}
		}
		if (hueCS != null) {
			hueCS.update(hueImage);
		}
	}
	
	public void computeSaturationImage(float hue, float saturation, float value) {
		hsv[0]=hue;
		hsv[1]=saturation;
		hsv[2]=value;
		
		for (int i = 0; i<imagesWidth; ++i) {
			hsv[1]=((float)i/(float)imagesWidth);
			int rgb=Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
			for (int j = 0; j<imagesHeight; ++j) {
				saturationImage.setRGB(i, j, rgb);
			}
		}
		if (saturationCS != null) {
			saturationCS.update(saturationImage);
		}
	}
	
	public void computeValueImage(float hue, float saturation, float value) { 
		hsv[0]=hue;
		hsv[1]=saturation;
		hsv[2]=value;
		
		for (int i = 0; i<imagesWidth; ++i) {
			hsv[2]=((float)i/(float)imagesWidth);
			int rgb=Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
			for (int j = 0; j<imagesHeight; ++j) {
				valueImage.setRGB(i, j, rgb);
			}
		}
		if (valueCS != null) {
			valueCS.update(valueImage);
		}
	}
	
	/**
	 * @return
	 */
	public BufferedImage getValueImage() {
		return valueImage;
	}

	/**
	 * @return
	 */
	public BufferedImage getSaturationImage() {
		return saturationImage;
	}

	/**
	 * @return
	 */
	public BufferedImage getHueImage() {
		return hueImage;
	}

	/**
	 * @param slider
	 */
	public void setHueCS(ColorSlider slider) {
		hueCS = slider;
		slider.addObserver(this);
	}

	/**
	 * @param slider
	 */
	public void setSaturationCS(ColorSlider slider) {
		saturationCS = slider;
		slider.addObserver(this);
	}

	/**
	 * @param slider
	 */
	public void setValueCS(ColorSlider slider) {
		valueCS = slider;
		slider.addObserver(this);
	}
		
	/**
	 * @return
	 */
	public float getHue() {
		return hue;
	}

	/**
	 * @return
	 */
	public float getSaturation() {
		return saturation;
	}

	/**
	 * @return
	 */
	public float getValue() {
		return value;
	}


	/* (non-Javadoc)
	 * @see model.ObserverIF#update()
	 */
	public void update() {
		// When updated with the new "result" color, if the "currentColor"
		// is aready properly set, there is no need to recompute the images.
		int rgb = Color.HSBtoRGB(hue, saturation, value);
		Pixel currentColor = new Pixel(rgb);
		if(currentColor.getARGB() == result.getPixel().getARGB()) return;
		
		Color.RGBtoHSB(result.getPixel().getRed(), result.getPixel().getGreen(), result.getPixel().getBlue(), hsv);
		this.hue=hsv[0];
		this.saturation=hsv[1];
		this.value=hsv[2];
		
		hueCS.setValue((int)(hsv[0]*255));
		saturationCS.setValue((int)(hsv[1]*255));
		valueCS.setValue((int)(hsv[2]*255));
		computeHueImage(hue, saturation, value);
		computeSaturationImage(hue, saturation, value);
		computeValueImage(hue, saturation, value);
		
		// Efficiency issue: When the color is adjusted on a tab in the 
		// user interface, the sliders color of the other tabs are recomputed,
		// even though they are invisible. For an increased efficiency, the 
		// other tabs (mediators) should be notified when there is a tab 
		// change in the user interface. This solution was not implemented
		// here since it would increase the complexity of the code, making it
		// harder to understand.
	}

}
