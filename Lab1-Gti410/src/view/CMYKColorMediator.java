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
	
	/**
	 * Constructeur
	 * 
	 * @param result(ColorDialogResult)
	 * @param imagesWidth (int)
	 * @param imagesHeight (int)
	 */
	CMYKColorMediator(ColorDialogResult result, int imagesWidth, int imagesHeight) {
		this.imagesWidth = imagesWidth;
		this.imagesHeight = imagesHeight;
		
		//Conversion du result (RGB) en valeur en pourcentage (de 0 a 1 au lieu de 0 a 255)
		float redTemp = (float)result.getPixel().getRed()/255;
		float greenTemp = (float)result.getPixel().getGreen()/255;
		float blueTemp = (float)result.getPixel().getBlue()/255;
		
		//Algorithme de conversion de la couleur RGB (0 a 1) vers CMYK (0 a 1)
		this.dark = 1-Math.max(redTemp, Math.max(greenTemp, blueTemp));
		if(this.dark==1){
			this.cyan=(redTemp);
			this.magenta=(greenTemp);
			this.yellow=(blueTemp);
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
		
		//Appel des fonctions de traitement des images
		computeCyanImage(cyan, magenta, yellow, dark);
		computeMagentaImage(cyan, magenta, yellow, dark);
		computeYellowImage(cyan, magenta, yellow, dark); 	
		computeDarkImage(cyan, magenta, yellow, dark); 
	}
	
	
	/**
	 * Methode qui update l'image si le slider est modifié
	 * @params s(ColorSlider)
	 * @params v(int)
	 * @see View.SliderObserver#update(double)
	 **/
	public void update(ColorSlider s, int v) {
		boolean updateCyan = false;
		boolean updateMagenta = false;
		boolean updateYellow = false;
		boolean updateDark = false;
		
		//Si le slider changé est le slider du cyan et que la valeur du cyan n'est pas la même que celle d'avant, reset le cyan 
		//avec la nouvelle valeur et permet le traitement des autres couleurs
		//NB:le v (value) est une valeur de 0 a 255 et doit être mis en pourcentage (division par 255)
		if (s == cyanCS && (float)v/255 != cyan) {
			cyan = (float)v/255;
			updateMagenta = true;
			updateYellow = true;
			updateDark = true;
		}
		//Si le slider changé est le slider du magenta et que la valeur du magenta n'est pas la même que celle d'avant, reset le magenta 
		//avec la nouvelle valeur et permet le traitement des autres couleurs
		//NB:le v (value) est une valeur de 0 a 255 et doit être mis en pourcentage (division par 255)
		if (s == magentaCS && (float)v/255 != magenta) {
			magenta = (float)v/255;
			updateCyan = true;
			updateYellow = true;
			updateDark = true;
		}
		//Si le slider changé est le slider du yellow et que la valeur du yellow n'est pas la même que celle d'avant, reset le yellow 
		//avec la nouvelle valeur et permet le traitement des autres couleurs
		//NB:le v (value) est une valeur de 0 a 255 et doit être mis en pourcentage (division par 255)
		if (s == yellowCS && (float)v/255 != yellow) {
			yellow = (float)v/255;
			updateCyan = true;
			updateMagenta = true;
			updateDark = true;
		}
		//Si le slider changé est le slider du dark et que la valeur du dark n'est pas la même que celle d'avant, reset le dark 
		//avec la nouvelle valeur et permet le traitement des autres couleurs
		//NB:le v (value) est une valeur de 0 a 255 et doit être mis en pourcentage (division par 255)
		if (s == darkCS && (float)v/255 != dark) {
			dark = (float)v/255;
			updateCyan = true;
			updateMagenta = true;
			updateYellow = true;
		}
		//Traitement des images selon les valeurs boolennes setter plus haut
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
		
		//Convertion de CMYK(pourcentage) a RBG(0 a 255) pour reset le pixel du result
		int red=(int) (255*(1-cyan)*(1-dark));
		int green=(int) (255*(1-magenta)*(1-dark));
		int blue=(int) (255*(1-yellow)*(1-dark));
		Pixel pixel = new Pixel(red, green, blue, 255); 
		result.setPixel(pixel);
	}
	
	/**
	 * Méthode qui traite l'image du cyan
	 * @param cyan(float)
	 * @param magenta(float)
	 * @param yellow(float)
	 * @param dark(float)
	 */
	public void computeCyanImage(float cyan, float magenta, float yellow, float dark) { 
		//Convertion du CMYK (pourcentage) en RGB (0 a 255)
		int red=(int) (255*(1-cyan)*(1-dark));
		int green=(int) (255*(1-magenta)*(1-dark));
		int blue=(int) (255*(1-yellow)*(1-dark));
		Pixel p = new Pixel(red, green, blue, 255); 
		
		//Boucle qui set chaque pixel du Slider avec la bonne couleur (en variant le cyan)
		for (int i = 0; i<imagesWidth; ++i) {
			//Determine le cyan selon l'etat de la boucle puis on convertit le cyan en rouge pour établir le pixel
			cyan=((float)i/(float)imagesWidth);
			p.setRed((int)(255*(1-cyan)*(1-dark))); 
			int rgb = p.getARGB();
			//set le resultat de la couleur pour chaque pixel en hauteur(ligne verticale)
			for (int j = 0; j<imagesHeight; ++j) {
				cyanImage.setRGB(i, j, rgb);
			}
		}
		//Si le slider du cyan n'est pas null, update le slider
		if (cyanCS != null) {
			cyanCS.update(cyanImage);
		}
	}
	
	/**
	 * Méthode qui traite l'image du magenta
	 * @param cyan(float)
	 * @param magenta(float)
	 * @param yellow(float)
	 * @param dark(float)
	 */
	public void computeMagentaImage(float cyan, float magenta, float yellow, float dark) {
		//Convertion du CMYK (pourcentage) en RGB (0 a 255)
		int red=(int) (255*(1-cyan)*(1-dark));
		int green=(int) (255*(1-magenta)*(1-dark));
		int blue=(int) (255*(1-yellow)*(1-dark));
		Pixel p = new Pixel(red, green, blue, 255); 
		
		//Boucle qui set chaque pixel du Slider avec la bonne couleur (en variant le magenta)
		for (int i = 0; i<imagesWidth; ++i) {
			//Determine le magenta selon l'etat de la boucle puis on convertit le magenta en vert pour établir le pixel
			magenta=((float)i/(float)imagesWidth);
			p.setGreen((int) (255*(1-magenta)*(1-dark))); 
			int rgb = p.getARGB();
			//set le resultat de la couleur pour chaque pixel en hauteur(ligne verticale)
			for (int j = 0; j<imagesHeight; ++j) {
				magentaImage.setRGB(i, j, rgb);
			}
		}
		//Si le slider du magenta n'est pas null, update le slider
		if (magentaCS != null) {
			magentaCS.update(magentaImage);
		}
	}
	
	/**
	 * Méthode qui traite l'image du yellow
	 * @param cyan(float)
	 * @param magenta(float)
	 * @param yellow(float)
	 * @param dark(float)
	 */
	public void computeYellowImage(float cyan, float magenta, float yellow, float dark) { 
		//Convertion du CMYK (pourcentage) en RGB (0 a 255)
		int red=(int) (255*(1-cyan)*(1-dark));
		int green=(int) (255*(1-magenta)*(1-dark));
		int blue=(int) (255*(1-yellow)*(1-dark));
		Pixel p = new Pixel(red, green, blue, 255); 
		
		//Boucle qui set chaque pixel du Slider avec la bonne couleur (en variant le yellow)
		for (int i = 0; i<imagesWidth; ++i) {
			//Determine le jaune selon l'etat de la boucle puis on convertit le jaune en bleu pour établir le pixel
			yellow=((float)i/(float)imagesWidth);
			p.setBlue((int) (255*(1-yellow)*(1-dark))); 
			int rgb = p.getARGB();
			//set le resultat de la couleur pour chaque pixel en hauteur(ligne verticale)
			for (int j = 0; j<imagesHeight; ++j) {
				yellowImage.setRGB(i, j, rgb);
			}
		}
		//Si le slider du yellow n'est pas null, update le slider
		if (yellowCS != null) {
			yellowCS.update(yellowImage);
		}
	}
	
	/**
	 * Méthode qui traite l'image du dark
	 * @param cyan(float)
	 * @param magenta(float)
	 * @param yellow(float)
	 * @param dark(float)
	 */
	public void computeDarkImage(float cyan, float magenta, float yellow, float dark) { 
		//Convertion du CMYK (pourcentage) en RGB (0 a 255)
		int red=(int) (255*(1-cyan)*(1-dark));
		int green=(int) (255*(1-magenta)*(1-dark));
		int blue=(int) (255*(1-yellow)*(1-dark));
		Pixel p = new Pixel(red, green, blue, 255); 
		
		//Boucle qui set chaque pixel du Slider avec la bonne couleur(en variant le noir)
		for (int i = 0; i<imagesWidth; ++i) {
			//Determine le noir selon l'etat de la boucle puis on convertit le CMYK en RGB pour établir le pixel(depend du noir)
			dark=((float)i/(float)imagesWidth);
			red=(int) (255*(1-cyan)*(1-dark));
			green=(int) (255*(1-magenta)*(1-dark));
			blue=(int) (255*(1-yellow)*(1-dark));
			p.setRed(red); 
			p.setGreen(green); 
			p.setBlue(blue); 
			int rgb = p.getARGB();
			//set le resultat de la couleur pour chaque pixel en hauteur(ligne verticale)
			for (int j = 0; j<imagesHeight; ++j) {
				darkImage.setRGB(i, j, rgb);
			}
		}
		//Si le slider du noir n'est pas null, update le slider
		if (darkCS != null) {
			darkCS.update(darkImage);
		}
	}
	
	/**
	 * Getter pour image du dark
	 * @return darkImage (BufferedImage)
	 */
	public BufferedImage getDarkImage() {
		return darkImage;
	}
	
	/**
	 * Getter pour image du yellow
	 * @return yellowImage (BufferedImage)
	 */
	public BufferedImage getYellowImage() {
		return yellowImage;
	}

	/**
	 * Getter pour image du magenta
	 * @return magentaImage (BufferedImage)
	 */
	public BufferedImage getMagentaImage() {
		return magentaImage;
	}

	/**
	 * Getter pour image du cyan
	 * @return cyanImage (BufferedImage)
	 */
	public BufferedImage getCyanImage() {
		return cyanImage;
	}

	/**
	 * Setter pour le slider du cyan
	 * @param slider(ColorSlider)
	 */
	public void setCyanCS(ColorSlider slider) {
		cyanCS = slider;
		slider.addObserver(this);
	}

	/**
	 * Setter pour le slider du magenta
	 * @param slider(ColorSlider)
	 */
	public void setMagentaCS(ColorSlider slider) {
		magentaCS = slider;
		slider.addObserver(this);
	}

	/**
	 * Setter pour le slider du yellow
	 * @param slider(ColorSlider)
	 */
	public void setYellowCS(ColorSlider slider) {
		yellowCS = slider;
		slider.addObserver(this);
	}
		
	/**
	 * Setter pour le slider du dark
	 * @param slider(ColorSlider)
	 */
	public void setDarkCS(ColorSlider slider) {
		darkCS = slider;
		slider.addObserver(this);
	}
	
	/**
	 * Getter pour Cyan
	 * @return cyan (float)
	 */
	public float getCyan() {
		return cyan;
	}

	/**
	 * Getter pour Magenta
	 * @return magenta (float)
	 */
	public float getMagenta() {
		return magenta;
	}

	/**
	 * Getter pour yellow
	 * @return yellow (float)
	 */
	public float getYellow() {
		return yellow;
	}
	
	/**
	 * Getter pour dark 
	 * @return dark (float)
	 */
	public float getDark() {
		return dark;
	}

	/**
	 * Methode qui update l'image si le slider est modifié et qui traite les image si le changement est fait
	 * sur un autre panel (RGB ou HSV)
	 * @see model.ObserverIF#update()
	 **/
	public void update() {
		// When updated with the new "result" color, if the "currentColor"
		// is already properly set, there is no need to recompute the images.
		//Algorithme de conversion de CMYK vers RGB
		int red=(int) (255*(1-cyan)*(1-dark));
		int green=(int) (255*(1-magenta)*(1-dark));
		int blue=(int) (255*(1-yellow)*(1-dark));
		Pixel currentColor = new Pixel(red, green, blue, 255); 
		if(currentColor.getARGB() == result.getPixel().getARGB()) return;
		
		//Conversion du result (RGB) en valeur en pourcentage (de 0 a 1 au lieu de 0 a 255)
		float redTemp = (float)result.getPixel().getRed()/255;
		float greenTemp = (float)result.getPixel().getGreen()/255;
		float blueTemp = (float)result.getPixel().getBlue()/255;
		
		//Algorithme de conversion de la couleur RGB (0 a 1) vers CMYK (0 a 1)
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
		
		//Set la value des sliders selon la couleur(echelle de 255)
		cyanCS.setValue((int)(cyan*255));
		magentaCS.setValue((int)(magenta*255));
		yellowCS.setValue((int)(yellow*255));
		darkCS.setValue((int)(dark*255));
		
		//Applique le traitement sur les images
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
