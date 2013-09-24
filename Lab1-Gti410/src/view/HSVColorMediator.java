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
	
	/**
	 * Constructeur
	 * 
	 * @param result (ColorDialogResult)
	 * @param imagesWidth (int)
	 * @param imagesHeight (int)
	 */
	HSVColorMediator(ColorDialogResult result, int imagesWidth, int imagesHeight) {
		this.imagesWidth = imagesWidth;
		this.imagesHeight = imagesHeight;
		
		//Conversion du result (RGB) en valeur en pourcentage (de 0 a 1 au lieu de 0 a 255)
		redTemp = (float)result.getPixel().getRed()/255;
		greenTemp = (float)result.getPixel().getGreen()/255;
		blueTemp = (float)result.getPixel().getBlue()/255;
		
		//Determination de certaines valeurs requisent pour l'algorithme de convertion
		float max = Math.max(redTemp,Math.max(greenTemp,blueTemp));
		float min = Math.min(redTemp,Math.min(greenTemp,blueTemp));
		float delta = max - min;
		
		//Algorithme de conversion de la couleur RGB (pourcentage) vers HSV (0 a 360 pour H, pourcentage pour s et v)
		this.value = max;
		if(delta==0){
			this.saturation = 0;
		}
		else{
			this.saturation = delta/max;
		}
		if(delta==0){
			this.hue=0;
		}
		else{
			if(redTemp==max) this.hue = 60 * (((greenTemp-blueTemp)/delta)%6);
			else if(greenTemp==max) this.hue = 60 * (((blueTemp-redTemp)/delta)+2);
			else this.hue = 60 * (((redTemp-greenTemp)/delta)+4);
		}
		if(this.hue<0) this.hue=this.hue+360;
		
		this.result = result;
		result.addObserver(this);
		
		hueImage = new BufferedImage(imagesWidth, imagesHeight, BufferedImage.TYPE_INT_ARGB);
		saturationImage = new BufferedImage(imagesWidth, imagesHeight, BufferedImage.TYPE_INT_ARGB);
		valueImage = new BufferedImage(imagesWidth, imagesHeight, BufferedImage.TYPE_INT_ARGB);
		
		//Appel des fonctions de traitement des images
		computeHueImage(hue, saturation, value);
		computeSaturationImage(hue, saturation, value);
		computeValueImage(hue, saturation, value); 	
	}
	
	
	/**
	 * Methode qui update l'image si le slider est modifié
	 * @params s(ColorSlider)
	 * @params v(int)
	 * @see View.SliderObserver#update(double)
	 **/
	public void update(ColorSlider s, int v) {
		boolean updateHue = false;
		boolean updateSaturation = false;
		boolean updateValue = false;
		
		//Si le slider changé est le slider du hue et que la valeur du hue n'est pas la même que celle d'avant, reset le hue 
		//avec la nouvelle valeur et permet le traitement des autres couleurs
		//NB:le v (value) est une valeur de 0 a 255 et doit être mis en angle de 0 a 360 (division par 255 suvit d'une multiplication par 360)
		if (s == hueCS && (float)v*360/255 != hue) {
			hue = (float)v*360/255;
			updateSaturation = true;
			updateValue = true;
		}
		//Si le slider changé est le slider du saturation et que la valeur du saturation n'est pas la même que celle d'avant, reset le saturation 
		//avec la nouvelle valeur et permet le traitement des autres couleurs
		//NB:le v (value) est une valeur de 0 a 255 et doit être mis en pourcentage (division par 255)
		if (s == saturationCS && (float)v/255 != saturation) {
			saturation = (float)v/255;
			updateHue = true;
			updateValue = true;
		}
		//Si le slider changé est le slider du value et que la valeur du value n'est pas la même que celle d'avant, reset le value 
		//avec la nouvelle valeur et permet le traitement des autres couleurs
		//NB:le v (value) est une valeur de 0 a 255 et doit être mis en pourcentage (division par 255)	
		if (s == valueCS && (float)v/255 != value) {
			value = (float)v/255;
			updateHue = true;
			updateSaturation = true;
		}
		
		//Traitement des images selon les valeurs boolennes setter plus haut
		if (updateHue) {
			computeHueImage(hue, saturation, value);
		}
		if (updateSaturation) {
			computeSaturationImage(hue, saturation, value);
		}
		if (updateValue) {
			computeValueImage(hue, saturation, value);
		}
		
		//Convertion de hsv(0 a 360 et pourcentage) a RBG(0 a 255) pour reset le pixel du result
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
	
	/**
	 * Méthode qui traite l'image du hue
	 * @param hue(float)
	 * @param saturation(float)
	 * @param value(float)
	 */
	public void computeHueImage(float hue, float saturation, float value) { 
		//Boucle qui set chaque pixel du Slider avec la bonne couleur (en variant le hue)
		for (int i = 0; i<imagesWidth; ++i) {
			//Determine le hue selon l'etat de la boucle puis on convertit le HSV vers le RGB pour etablir le pixel
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
			//set le resultat de la couleur pour chaque pixel en hauteur(ligne verticale)
			for (int j = 0; j<imagesHeight; ++j) {
				hueImage.setRGB(i, j, p.getARGB());
			}
		}
		//Si le slider du hue n'est pas null, update le slider
		if (hueCS != null) {
			hueCS.update(hueImage);
		}
	}
	
	/**
	 * Méthode qui traite l'image de la saturation
	 * @param hue(float)
	 * @param saturation(float)
	 * @param value(float)
	 */
	public void computeSaturationImage(float hue, float saturation, float value) {
		//Boucle qui set chaque pixel du Slider avec la bonne couleur (en variant la saturation)
		for (int i = 0; i<imagesWidth; ++i) {
			//Determine la saturation selon l'etat de la boucle puis on convertit le HSV vers le RGB pour etablir le pixel
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
			//set le resultat de la couleur pour chaque pixel en hauteur(ligne verticale)
			for (int j = 0; j<imagesHeight; ++j) {
				saturationImage.setRGB(i, j, p.getARGB());
			}
		}
		//Si le slider du saturation n'est pas null, update le slider
		if (saturationCS != null) {
			saturationCS.update(saturationImage);
		}
	}
	
	/**
	 * Méthode qui traite l'image du value
	 * @param hue(float)
	 * @param saturation(float)
	 * @param value(float)
	 */
	public void computeValueImage(float hue, float saturation, float value) { 
		//Boucle qui set chaque pixel du Slider avec la bonne couleur (en variant le value)
		for (int i = 0; i<imagesWidth; ++i) {
			//Determine le value selon l'etat de la boucle puis on convertit le HSV vers le RGB pour etablir le pixel
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
			//set le resultat de la couleur pour chaque pixel en hauteur(ligne verticale)
			for (int j = 0; j<imagesHeight; ++j) {
				valueImage.setRGB(i, j, p.getARGB());
			}
		}
		//Si le slider du saturation n'est pas null, update le slider
		if (valueCS != null) {
			valueCS.update(valueImage);
		}
	}
	
	/**
	 * Getter pour image du value
	 * @return valueImage (BufferedImage)
	 */
	public BufferedImage getValueImage() {
		return valueImage;
	}

	/**
	 * Getter pour image de la saturation
	 * @return saturationImage (BufferedImage)
	 */
	public BufferedImage getSaturationImage() {
		return saturationImage;
	}

	/**
	 * Getter pour image du hue
	 * @return hueImage (BufferedImage)
	 */
	public BufferedImage getHueImage() {
		return hueImage;
	}

	/**
	 * Setter pour le slider du hue
	 * @param slider(ColorSlider)
	 */
	public void setHueCS(ColorSlider slider) {
		hueCS = slider;
		slider.addObserver(this);
	}

	/**
	 * Setter pour le slider de la saturation
	 * @param slider(ColorSlider)
	 */
	public void setSaturationCS(ColorSlider slider) {
		saturationCS = slider;
		slider.addObserver(this);
	}

	/**
	 * Setter pour le slider de la value
	 * @param slider(ColorSlider)
	 */
	public void setValueCS(ColorSlider slider) {
		valueCS = slider;
		slider.addObserver(this);
	}
		
	/**
	 * Getter pour hue
	 * @return hue (float)
	 */
	public float getHue() {
		return hue;
	}

	/**
	 * Getter pour saturation
	 * @return satuation (float)
	 */
	public float getSaturation() {
		return saturation;
	}

	/**
	 * Getter pour value
	 * @return value (float)
	 */
	public float getValue() {
		return value;
	}


	/**
	 * Methode qui update l'image si le slider est modifié et qui traite les image si le changement est fait
	 * sur un autre panel (RGB ou HSV)
	 * @see model.ObserverIF#update()
	 **/
	public void update() {
		// When updated with the new "result" color, if the "currentColor"
		// is already properly set, there is no need to recompute the images.
		//Algorithme de conversion de HSV ver RGB
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
		
		//Conversion du result (RGB) en valeur en pourcentage (de 0 a 1 au lieu de 0 a 255)
		redTemp = (float)result.getPixel().getRed()/255;
		greenTemp = (float)result.getPixel().getGreen()/255;
		blueTemp = (float)result.getPixel().getBlue()/255;
		
		//Determination de certaines valeurs requisent pour l'algorithme de convertion
		float max = Math.max(redTemp,Math.max(greenTemp,blueTemp));
		float min = Math.min(redTemp,Math.min(greenTemp,blueTemp));
		float delta = max - min;
		
		//Algorithme de conversion de la couleur RGB (pourcentage) vers HSV (0 a 360 pour H, pourcentage pour s et v)
		this.value = max;
		if(delta==0) 
		{
			this.saturation = 0;
		}
		else{
			this.saturation = delta/max;
		}
		if(delta==0){
			this.hue=0;
		}
		else{
			if(redTemp==max) this.hue = 60 * (((greenTemp-blueTemp)/delta)%6);
			else if(greenTemp==max) this.hue = 60 * (((blueTemp-redTemp)/delta)+2);
			else this.hue = 60 * (((redTemp-greenTemp)/delta)+4);
		}
		if(this.hue<0) this.hue=this.hue+360;
		
		//Set la value des sliders selon la couleur(hue:valeur sur 360 et echelle de 255, saturation et value: echelle de 255)
		hueCS.setValue((int)(this.hue/360*255));
		saturationCS.setValue((int)(this.saturation*255));
		valueCS.setValue((int)(this.value*255));
		
		//Applique le traitement sur les images
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
