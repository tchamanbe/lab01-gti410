/*
   This file is part of j2dcg.
   j2dcg is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.
   j2dcg is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   You should have received a copy of the GNU General Public License
   along with j2dcg; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package controller;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import model.ImageX;
import model.Pixel;
import model.Shape;

/**
 * <p>Title: ImageLineFiller</p>
 * <p>Description: Image transformer that inverts the row color</p>
 * <p>Copyright: Copyright (c) 2003 Colin Barr�-Brisebois, �ric Paquette</p>
 * <p>Company: ETS - �cole de Technologie Sup�rieure</p>
 * @author unascribed
 * @version $Revision: 1.13 $
 */
public class ImageFiller extends AbstractTransformer {
	private ImageX currentImage;
	private Pixel fillColor = new Pixel(0xFF00FFFF);
	private Pixel colorToFill = new Pixel(0xFF00FFFF);
	private Pixel borderColor = new Pixel(0xFFFFFF00);
	private boolean floodFill = true;
	private int hueThreshold = 1;
	private int saturationThreshold = 2;
	private int valueThreshold = 3;
	
	private float borderHue;
	private float borderSaturation;
	private float borderValue;

	private float currentHue;
	private float currentSaturation;
	private float currentValue;
	
	/**
	 * Creates an ImageLineFiller with default parameters.
	 * Default pixel change color is black.
	 */
	public ImageFiller() {
		setBorderColor(borderColor);
	}
	
	/* (non-Javadoc)
	 * @see controller.AbstractTransformer#getID()
	 */
	public int getID() { return ID_FLOODER; } 
	
	protected boolean mouseClicked(MouseEvent e){
		List intersectedObjects = Selector.getDocumentObjectsAtLocation(e.getPoint());
		if (!intersectedObjects.isEmpty()) {
			Shape shape = (Shape)intersectedObjects.get(0);
			//gets the current shape
			if (shape instanceof ImageX) {
				currentImage = (ImageX)shape;

				Point pt = e.getPoint();
				Point ptTransformed = new Point();
				try {
					shape.inverseTransformPoint(pt, ptTransformed);
				} catch (NoninvertibleTransformException e1) {
					e1.printStackTrace();
					return false;
				}
				ptTransformed.translate(-currentImage.getPosition().x, -currentImage.getPosition().y);
				//If point clicked is in the current image range
				if (0 <= ptTransformed.x && ptTransformed.x < currentImage.getImageWidth() &&
				    0 <= ptTransformed.y && ptTransformed.y < currentImage.getImageHeight()) {
					currentImage.beginPixelUpdate();
					//gets the color of the current pixel clicked
					colorToFill = currentImage.getPixel(ptTransformed.x,ptTransformed.y);
					//if in the floodFill mode and the color to fill is not equal to the fill color, begin the flood fill
					if(isFloodFill() && !colorToFill.equals(fillColor)){						
						floodFill(ptTransformed.x, ptTransformed.y);
					}
					//if in the boundaryFill mode and the color to fill is not equal to the fill color, begin the boundary fill
					else if(!isFloodFill() && !colorToFill.equals(fillColor)){
						boundaryFill(ptTransformed.x, ptTransformed.y);
					}
					currentImage.endPixelUpdate();											 	
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Flood fill with specified color (recursive method)
	 */
	private void floodFill(int x , int y) {
		//If the current position is in the image range and if the color of the current pixel equals to the color to fill
		if(0 <= x && x < currentImage.getImageWidth() &&
				0 <= y && y < currentImage.getImageWidth() &&
					currentImage.getPixel(x,y).equals(colorToFill)){
			//set the current pixel with the fill color
			currentImage.setPixel(x,y, fillColor);
			//recursive call for the 4 pixel neighbors (4-way)
			floodFill(x+1,y);
			floodFill(x-1,y);
			floodFill(x,y+1);
			floodFill(x,y-1);
		}
	}
	
	/**
	 * boundary fill with specified color (recursive method)
	 */
	private void boundaryFill(int x , int y) {
		//If the current position is in the image range and if the color of the current pixel is not equal to the fill color
		//and if the current pixel color is not in the thresoldRange (see inThresholdRange method)
		if(0 <= x && x < currentImage.getImageWidth() &&
				0 <= y && y < currentImage.getImageWidth() &&
					!currentImage.getPixel(x,y).equals(fillColor) &&
						!inThresholdRange(currentImage.getPixel(x,y))){
			//set the current pixel with the fill color
			currentImage.setPixel(x,y, fillColor);
			//recursive call for the 4 pixel neighbors (4-way)
			boundaryFill(x+1,y);
			boundaryFill(x-1,y);
			boundaryFill(x,y+1);
			boundaryFill(x,y-1);
		}
	}
	
	/**
	 * Methods that returns if the current pixel is in the threshold range
	 * @return boolean
	 */
	private boolean inThresholdRange(Pixel current){		
		//Conversion du result (RGB) en valeur en pourcentage (de 0 a 1 au lieu de 0 a 255)
		float currentRed = (float)current.getRed()/255;
		float currentGreen = (float)current.getGreen()/255;
		float currentBlue = (float)current.getBlue()/255;
		
		//Determination de certaines valeurs requisent pour l'algorithme de convertion
		float max = Math.max(currentRed,Math.max(currentGreen,currentBlue));
		float min = Math.min(currentRed,Math.min(currentGreen,currentBlue));
		float delta = max - min;
		
		//Algorithme de conversion de la couleur RGB (pourcentage) vers HSV (0 a 360 pour H, pourcentage pour s et v)
		currentValue = max;
		if(delta==0){
			currentSaturation = 0;
		}
		else{
			currentSaturation = delta/max;
		}
		if(delta==0){
			currentHue=0;
		}
		else{
			if(currentRed==max) currentHue = 60 * (((currentGreen-currentBlue)/delta)%6);
			else if(currentGreen==max) currentHue = 60 * (((currentBlue-currentRed)/delta)+2);
			else currentHue = 60 * (((currentRed-currentGreen)/delta)+4);
		}
		if(currentHue<0) currentHue=currentHue+360;
		
		//Calcule du range pour le Hue, le Sat et le Val
		float rangeHue = Math.abs(borderHue-currentHue);
		if(rangeHue>180){
			rangeHue = 360-rangeHue;
		}
		float rangeSat = Math.abs(borderSaturation-currentSaturation)*255;
		float rangeVal = Math.abs(borderValue-currentValue)*255;
		
		//Si tout les ranges sont �gals ou inf�rieur au range sp�cifier avec les sliders alors retourne true, sinon retourne false
		if(rangeHue<=getHueThreshold() && rangeSat<=getSaturationThreshold() && rangeVal<=getValueThreshold()){
			return true;
		}
		return false;
	}
	
	/**
	 * @return
	 */
	public Pixel getBorderColor() {
		return borderColor;
	}

	/**
	 * @return
	 */
	public Pixel getFillColor() {
		return fillColor;
	}

	/**
	 * @param pixel
	 */
	public void setBorderColor(Pixel pixel) {
		borderColor = pixel;
		
		//Conversion du result (RGB) en valeur en pourcentage (de 0 a 1 au lieu de 0 a 255)
		float borderRed = (float)borderColor.getRed()/255;
		float borderGreen = (float)borderColor.getGreen()/255;
		float borderBlue = (float)borderColor.getBlue()/255;
		
		//Determination de certaines valeurs requisent pour l'algorithme de convertion
		float max = Math.max(borderRed,Math.max(borderGreen,borderBlue));
		float min = Math.min(borderRed,Math.min(borderGreen,borderBlue));
		float delta = max - min;
		
		//Algorithme de conversion de la couleur RGB (pourcentage) vers HSV (0 a 360 pour H, pourcentage pour s et v)
		borderValue = max;
		if(delta==0){
			borderSaturation = 0;
		}
		else{
			borderSaturation = delta/max;
		}
		if(delta==0){
			borderHue=0;
		}
		else{
			if(borderRed==max) borderHue = 60 * (((borderGreen-borderBlue)/delta)%6);
			else if(borderGreen==max) borderHue = 60 * (((borderBlue-borderRed)/delta)+2);
			else borderHue = 60 * (((borderRed-borderGreen)/delta)+4);
		}
		if(borderHue<0) borderHue=borderHue+360;
		
		System.out.println("new border color");
	}

	/**
	 * @param pixel
	 */
	public void setFillColor(Pixel pixel) {
		fillColor = pixel;
		System.out.println("new fill color");
	}
	/**
	 * @return true if the filling algorithm is set to Flood Fill, false if it is set to Boundary Fill.
	 */
	public boolean isFloodFill() {
		return floodFill;
	}

	/**
	 * @param b set to true to enable Flood Fill and to false to enable Boundary Fill.
	 */
	public void setFloodFill(boolean b) {
		floodFill = b;
		if (floodFill) {
			System.out.println("now doing Flood Fill");
		} else {
			System.out.println("now doing Boundary Fill");
		}
	}

	/**
	 * @return
	 */
	public int getHueThreshold() {
		return hueThreshold;
	}

	/**
	 * @return
	 */
	public int getSaturationThreshold() {
		return saturationThreshold;
	}

	/**
	 * @return
	 */
	public int getValueThreshold() {
		return valueThreshold;
	}

	/**
	 * @param i
	 */
	public void setHueThreshold(int i) {
		hueThreshold = i;
		System.out.println("new Hue Threshold " + i);
	}

	/**
	 * @param i
	 */
	public void setSaturationThreshold(int i) {
		saturationThreshold = i;
		System.out.println("new Saturation Threshold " + i);
	}

	/**
	 * @param i
	 */
	public void setValueThreshold(int i) {
		valueThreshold = i;
		System.out.println("new Value Threshold " + i);
	}

}
