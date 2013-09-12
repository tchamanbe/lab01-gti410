package view;

import java.awt.image.BufferedImage;

import model.ObserverIF;
import model.Pixel;

class CMYKColorMediator extends Object implements SliderObserver, ObserverIF {
	ColorSlider cyanCS;
	ColorSlider magentaCS;
	ColorSlider yellowCS;
	ColorSlider darkCS;
	float cyan;
	float magenta;
	float yellow;
	float dark;
	BufferedImage cyanImage;
	BufferedImage magentaImage;
	BufferedImage yellowImage;
	BufferedImage darkImage;
	int imagesWidth;
	int imagesHeight;
	ColorDialogResult result;
	
	CMYKColorMediator(ColorDialogResult result, int imagesWidth, int imagesHeight) {
		this.imagesWidth = imagesWidth;
		this.imagesHeight = imagesHeight;
		
		float redTemp = (float)result.getPixel().getRed()/255;
		float greenTemp = (float)result.getPixel().getGreen()/255;
		float blueTemp = (float)result.getPixel().getBlue()/255;
		this.dark = 1-Math.max(redTemp, Math.max(greenTemp, blueTemp));
		if(this.dark==1){
			this.cyan=(1-redTemp-this.dark);
			this.magenta=(1-greenTemp-this.dark);
			this.yellow=(1-blueTemp-this.dark);
		}
		else{
			this.cyan=(1-redTemp-this.dark)/(1-this.dark);
			this.magenta=(1-greenTemp-this.dark)/(1-this.dark);
			this.yellow=(1-blueTemp-this.dark)/(1-this.dark);
		}
		
		this.result = result;
		result.addObserver(this);
		
		cyanImage = new BufferedImage(imagesWidth, imagesHeight, BufferedImage.TYPE_INT_ARGB);
		magentaImage = new BufferedImage(imagesWidth, imagesHeight, BufferedImage.TYPE_INT_ARGB);
		yellowImage = new BufferedImage(imagesWidth, imagesHeight, BufferedImage.TYPE_INT_ARGB);
		darkImage = new BufferedImage(imagesWidth, imagesHeight, BufferedImage.TYPE_INT_ARGB);
		computeCyanImage(cyan, magenta, yellow, dark);
		computeMagentaImage(cyan, magenta, yellow, dark);
		computeYellowImage(cyan, magenta, yellow, dark); 	
		computeDarkImage(cyan, magenta, yellow, dark); 
	}
	
	
	/*
	 * @see View.SliderObserver#update(double)
	 */
	public void update(ColorSlider s, int v) {
		boolean updateCyan = false;
		boolean updateMagenta = false;
		boolean updateYellow = false;
		boolean updateDark = false;
		if (s == cyanCS && (float)v/255 != cyan) {
			cyan = (float)v/255;
			updateMagenta = true;
			updateYellow = true;
			updateDark = true;
		}
		if (s == magentaCS && (float)v/255 != magenta) {
			magenta = (float)v/255;
			updateCyan = true;
			updateYellow = true;
			updateDark = true;
		}
		if (s == yellowCS && (float)v/255 != yellow) {
			yellow = (float)v/255;
			updateCyan = true;
			updateMagenta = true;
			updateDark = true;
		}
		if (s == darkCS && (float)v/255 != dark) {
			dark = (float)v/255;
			updateCyan = true;
			updateMagenta = true;
			updateYellow = true;
		}
		if (updateCyan) {
			computeCyanImage(cyan, magenta, yellow, dark);
		}
		if (updateMagenta) {
			computeMagentaImage(cyan, magenta, yellow, dark);
		}
		if (updateYellow) {
			computeYellowImage(cyan, magenta, yellow, dark);
		}
		if (updateDark) {
			computeDarkImage(cyan, magenta, yellow, dark);
		}
		
		int red=(int) (255*(1-cyan)*(1-dark));
		int green=(int) (255*(1-magenta)*(1-dark));
		int blue=(int) (255*(1-yellow)*(1-dark));
		Pixel pixel = new Pixel(red, green, blue, 255); 
		result.setPixel(pixel);
	}
	
	public void computeCyanImage(float cyan, float magenta, float yellow, float dark) { 
		int red=(int) (255*(1-cyan)*(1-dark));
		int green=(int) (255*(1-magenta)*(1-dark));
		int blue=(int) (255*(1-yellow)*(1-dark));
		Pixel p = new Pixel(red, green, blue, 255); 
		
		for (int i = 0; i<imagesWidth; ++i) {
			cyan=((float)i/(float)imagesWidth);
			p.setRed((int)(255*(1-cyan)*(1-dark))); 
			int rgb = p.getARGB();
			for (int j = 0; j<imagesHeight; ++j) {
				cyanImage.setRGB(i, j, rgb);
			}
		}
		if (cyanCS != null) {
			cyanCS.update(cyanImage);
		}
	}
	
	public void computeMagentaImage(float cyan, float magenta, float yellow, float dark) {
		int red=(int) (255*(1-cyan)*(1-dark));
		int green=(int) (255*(1-magenta)*(1-dark));
		int blue=(int) (255*(1-yellow)*(1-dark));
		Pixel p = new Pixel(red, green, blue, 255); 
		
		for (int i = 0; i<imagesWidth; ++i) {
			magenta=((float)i/(float)imagesWidth);
			p.setGreen((int) (255*(1-magenta)*(1-dark))); 
			int rgb = p.getARGB();
			for (int j = 0; j<imagesHeight; ++j) {
				magentaImage.setRGB(i, j, rgb);
			}
		}
		if (magentaCS != null) {
			magentaCS.update(magentaImage);
		}
	}
	
	public void computeYellowImage(float cyan, float magenta, float yellow, float dark) { 
		int red=(int) (255*(1-cyan)*(1-dark));
		int green=(int) (255*(1-magenta)*(1-dark));
		int blue=(int) (255*(1-yellow)*(1-dark));
		Pixel p = new Pixel(red, green, blue, 255); 
		
		for (int i = 0; i<imagesWidth; ++i) {
			yellow=((float)i/(float)imagesWidth);
			p.setBlue((int) (255*(1-yellow)*(1-dark))); 
			int rgb = p.getARGB();
			for (int j = 0; j<imagesHeight; ++j) {
				yellowImage.setRGB(i, j, rgb);
			}
		}
		if (yellowCS != null) {
			yellowCS.update(yellowImage);
		}
	}
	
	public void computeDarkImage(float cyan, float magenta, float yellow, float dark) { 
		int red=(int) (255*(1-cyan)*(1-dark));
		int green=(int) (255*(1-magenta)*(1-dark));
		int blue=(int) (255*(1-yellow)*(1-dark));
		Pixel p = new Pixel(red, green, blue, 255); 
		for (int i = 0; i<imagesWidth; ++i) {
			dark=((float)i/(float)imagesWidth);
			red=(int) (255*(1-cyan)*(1-dark));
			green=(int) (255*(1-magenta)*(1-dark));
			blue=(int) (255*(1-yellow)*(1-dark));
			p.setRed((int)(255*(1-cyan)*(1-dark))); 
			p.setGreen((int) (255*(1-magenta)*(1-dark))); 
			p.setBlue((int) (255*(1-yellow)*(1-dark))); 
			int rgb = p.getARGB();
			for (int j = 0; j<imagesHeight; ++j) {
				darkImage.setRGB(i, j, rgb);
			}
		}
		if (darkCS != null) {
			darkCS.update(darkImage);
		}
	}
	
	/**
	 * @return
	 */
	public BufferedImage getDarkImage() {
		return darkImage;
	}
	
	/**
	 * @return
	 */
	public BufferedImage getYellowImage() {
		return yellowImage;
	}

	/**
	 * @return
	 */
	public BufferedImage getMagentaImage() {
		return magentaImage;
	}

	/**
	 * @return
	 */
	public BufferedImage getCyanImage() {
		return cyanImage;
	}

	/**
	 * @param slider
	 */
	public void setCyanCS(ColorSlider slider) {
		cyanCS = slider;
		slider.addObserver(this);
	}

	/**
	 * @param slider
	 */
	public void setMagentaCS(ColorSlider slider) {
		magentaCS = slider;
		slider.addObserver(this);
	}

	/**
	 * @param slider
	 */
	public void setYellowCS(ColorSlider slider) {
		yellowCS = slider;
		slider.addObserver(this);
	}
		
	/**
	 * @param slider
	 */
	public void setDarkCS(ColorSlider slider) {
		darkCS = slider;
		slider.addObserver(this);
	}
	
	/**
	 * @return
	 */
	public float getCyan() {
		return cyan;
	}

	/**
	 * @return
	 */
	public float getMagenta() {
		return magenta;
	}

	/**
	 * @return
	 */
	public float getYellow() {
		return yellow;
	}
	
	/**
	 * @return
	 */
	public float getDark() {
		return dark;
	}


	/* (non-Javadoc)
	 * @see model.ObserverIF#update()
	 */
	public void update() {
		// When updated with the new "result" color, if the "currentColor"
		// is aready properly set, there is no need to recompute the images.
		int red=(int) (255*(1-cyan)*(1-dark));
		int green=(int) (255*(1-magenta)*(1-dark));
		int blue=(int) (255*(1-yellow)*(1-dark));
		Pixel currentColor = new Pixel(red, green, blue, 255); 
		if(currentColor.getARGB() == result.getPixel().getARGB()) return;
		
		float redTemp = (float)result.getPixel().getRed()/255;
		float greenTemp = (float)result.getPixel().getGreen()/255;
		float blueTemp = (float)result.getPixel().getBlue()/255;
		this.dark = 1-Math.max(redTemp, Math.max(greenTemp, blueTemp));
		if(this.dark==1){
			this.cyan=0;
			this.magenta=0;
			this.yellow=0;
		}
		else{
			this.cyan=(1-redTemp-this.dark)/(1-this.dark);
			this.magenta=(1-greenTemp-this.dark)/(1-this.dark);
			this.yellow=(1-blueTemp-this.dark)/(1-this.dark);
		}
		
		cyanCS.setValue((int)(cyan*255));
		magentaCS.setValue((int)(magenta*255));
		yellowCS.setValue((int)(yellow*255));
		darkCS.setValue((int)(dark*255));
		computeCyanImage(cyan, magenta, yellow, dark);
		computeMagentaImage(cyan, magenta, yellow, dark);
		computeYellowImage(cyan, magenta, yellow, dark);
		computeDarkImage(cyan, magenta, yellow, dark);
		
		// Efficiency issue: When the color is adjusted on a tab in the 
		// user interface, the sliders color of the other tabs are recomputed,
		// even though they are invisible. For an increased efficiency, the 
		// other tabs (mediators) should be notified when there is a tab 
		// change in the user interface. This solution was not implemented
		// here since it would increase the complexity of the code, making it
		// harder to understand.
	}

}
