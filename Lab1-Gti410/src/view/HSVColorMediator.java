package view;

import java.awt.image.BufferedImage;

import model.ObserverIF;
import model.Pixel;

class HSVColorMediator extends Object implements SliderObserver, ObserverIF {
	ColorSlider hueCS;
	ColorSlider saturationCS;
	ColorSlider valueCS;
	float hue;
	float saturation;
	float value;
	float redTemp;
	float greenTemp;
	float blueTemp;
	Pixel p;
	float C;
	float X;
	float M;
	BufferedImage hueImage;
	BufferedImage saturationImage;
	BufferedImage valueImage;
	int imagesWidth;
	int imagesHeight;
	ColorDialogResult result;
	
	HSVColorMediator(ColorDialogResult result, int imagesWidth, int imagesHeight) {
		this.imagesWidth = imagesWidth;
		this.imagesHeight = imagesHeight;
		
		redTemp = (float)result.getPixel().getRed()/255;
		greenTemp = (float)result.getPixel().getGreen()/255;
		blueTemp = (float)result.getPixel().getBlue()/255;
		float max = Math.max(redTemp,Math.max(greenTemp,blueTemp));
		float min = Math.min(redTemp,Math.min(greenTemp,blueTemp));
		float delta = max - min;
		this.value = max;
		
		if(delta==0) this.saturation = 0;
		else this.saturation = delta/max;

		if(redTemp==max) this.hue = 60 * (((greenTemp-blueTemp)/delta)%6);
		else if(greenTemp==max) this.hue = 60 * (((blueTemp-redTemp)/delta)+2);
		else this.hue = 60 * (((redTemp-greenTemp)/delta)+4);

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
		if (s == hueCS && (float)v*360/255 != hue) {
			hue = (float)v*360/255;
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
		
		C = (value*saturation)*255;
		X = ((value*saturation) * (1 - Math.abs(((hue / 60)%2) - 1)))*255;
		M = (value - (value*saturation))*255;
		if(0<=hue && hue<60){redTemp=C;greenTemp=X;blueTemp=0;}
		else if(60<=hue && hue<120){redTemp=X;greenTemp=C;blueTemp=0;}
		else if(120<=hue && hue<180){redTemp=0;greenTemp=C;blueTemp=X;}
		else if(180<=hue && hue<240){redTemp=0;greenTemp=X;blueTemp=C;}
		else if(240<=hue && hue<300){redTemp=X;greenTemp=0;blueTemp=C;}
		else{ redTemp=C;greenTemp=0;blueTemp=X;}
		Pixel pixel = new Pixel((int)(redTemp+M),(int)(greenTemp+M),(int)(blueTemp+M));
		result.setPixel(pixel);
	}
	
	public void computeHueImage(float hue, float saturation, float value) { 
		for (int i = 0; i<imagesWidth; ++i) {
			hue= (float)i*(360/(float)imagesWidth);
			C = (value*saturation)*255;
			X = ((value*saturation) * (1 - Math.abs(((hue / 60)%2) - 1)))*255;
			M = (value - (value*saturation))*255;
			if(0<=hue && hue<60){redTemp=C;greenTemp=X;blueTemp=0;}
			else if(60<=hue && hue<120){redTemp=X;greenTemp=C;blueTemp=0;}
			else if(120<=hue && hue<180){redTemp=0;greenTemp=C;blueTemp=X;}
			else if(180<=hue && hue<240){redTemp=0;greenTemp=X;blueTemp=C;}
			else if(240<=hue && hue<300){redTemp=X;greenTemp=0;blueTemp=C;}
			else{ redTemp=C;greenTemp=0;blueTemp=X;}
			p=new Pixel((int)(redTemp+M),(int)(greenTemp+M),(int)(blueTemp+M));
			for (int j = 0; j<imagesHeight; ++j) {
				hueImage.setRGB(i, j, p.getARGB());
			}
		}
		if (hueCS != null) {
			hueCS.update(hueImage);
		}
	}
	
	public void computeSaturationImage(float hue, float saturation, float value) {
		for (int i = 0; i<imagesWidth; ++i) {
			saturation= ((float)i/(float)imagesWidth);
			C = (value*saturation)*255;
			X = ((value*saturation) * (1 - Math.abs(((hue / 60)%2) - 1)))*255;
			M = (value - (value*saturation))*255;
			if(0<=hue && hue<60){redTemp=C;greenTemp=X;blueTemp=0;}
			else if(60<=hue && hue<120){redTemp=X;greenTemp=C;blueTemp=0;}
			else if(120<=hue && hue<180){redTemp=0;greenTemp=C;blueTemp=X;}
			else if(180<=hue && hue<240){redTemp=0;greenTemp=X;blueTemp=C;}
			else if(240<=hue && hue<300){redTemp=X;greenTemp=0;blueTemp=C;}
			else{ redTemp=C;greenTemp=0;blueTemp=X;}
			p=new Pixel((int)(redTemp+M),(int)(greenTemp+M),(int)(blueTemp+M));
			for (int j = 0; j<imagesHeight; ++j) {
				saturationImage.setRGB(i, j, p.getARGB());
			}
		}
		if (saturationCS != null) {
			saturationCS.update(saturationImage);
		}
	}
	
	public void computeValueImage(float hue, float saturation, float value) { 
		for (int i = 0; i<imagesWidth; ++i) {
			value= ((float)i/(float)imagesWidth);
			C = (value*saturation)*255;
			X = ((value*saturation) * (1 - Math.abs(((hue / 60)%2) - 1)))*255;
			M = (value - (value*saturation))*255;
			if(0<=hue && hue<60){redTemp=C;greenTemp=X;blueTemp=0;}
			else if(60<=hue && hue<120){redTemp=X;greenTemp=C;blueTemp=0;}
			else if(120<=hue && hue<180){redTemp=0;greenTemp=C;blueTemp=X;}
			else if(180<=hue && hue<240){redTemp=0;greenTemp=X;blueTemp=C;}
			else if(240<=hue && hue<300){redTemp=X;greenTemp=0;blueTemp=C;}
			else{ redTemp=C;greenTemp=0;blueTemp=X;}
			p=new Pixel((int)(redTemp+M),(int)(greenTemp+M),(int)(blueTemp+M));
			for (int j = 0; j<imagesHeight; ++j) {
				valueImage.setRGB(i, j, p.getARGB());
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
		C = (value*saturation)*255;
		X = ((value*saturation) * (1 - Math.abs(((hue / 60)%2) - 1)))*255;
		M = (value - (value*saturation))*255;
		if(0<=hue && hue<60){redTemp=C;greenTemp=X;blueTemp=0;}
		else if(60<=hue && hue<120){redTemp=X;greenTemp=C;blueTemp=0;}
		else if(120<=hue && hue<180){redTemp=0;greenTemp=C;blueTemp=X;}
		else if(180<=hue && hue<240){redTemp=0;greenTemp=X;blueTemp=C;}
		else if(240<=hue && hue<300){redTemp=X;greenTemp=0;blueTemp=C;}
		else{ redTemp=C;greenTemp=0;blueTemp=X;}
		Pixel currentColor = new Pixel((int)(redTemp+M),(int)(greenTemp+M),(int)(blueTemp+M));
		if(currentColor.getARGB() == result.getPixel().getARGB()) return;
		
		redTemp = (float)result.getPixel().getRed()/255;
		greenTemp = (float)result.getPixel().getGreen()/255;
		blueTemp = (float)result.getPixel().getBlue()/255;
		float max = Math.max(redTemp,Math.max(greenTemp,blueTemp));
		float min = Math.min(redTemp,Math.min(greenTemp,blueTemp));
		float delta = max - min;
		this.value = max;
		
		if(delta==0) this.saturation = 0;
		else this.saturation = delta/max;

		if(delta==0){
			this.hue=0;
		}
		else{
			if(redTemp==max) this.hue = 60 * (((greenTemp-blueTemp)/delta)%6);
			else if(greenTemp==max) this.hue = 60 * (((blueTemp-redTemp)/delta)+2);
			else this.hue = 60 * (((redTemp-greenTemp)/delta)+4);
		}
		
		hueCS.setValue((int)(this.hue/360*255));
		saturationCS.setValue((int)(this.saturation*255));
		valueCS.setValue((int)(this.value*255));
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
