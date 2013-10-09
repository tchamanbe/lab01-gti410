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
 * <p>Copyright: Copyright (c) 2003 Colin Barré-Brisebois, Éric Paquette</p>
 * <p>Company: ETS - École de Technologie Supérieure</p>
 * @author unascribed
 * @version $Revision: 1.13 $
 */
public class ImageFiller extends AbstractTransformer {
	private ImageX currentImage;
	private Pixel fillColor = new Pixel(0xFF00FFFF);
	private Pixel colorToFill = new Pixel(0xFF00FFFF);
	private Pixel borderColor = new Pixel(0xFFFFFF00);
	private ArrayList<Pixel> borderColors = new ArrayList<Pixel>();
	private boolean floodFill = true;
	private int hueThreshold = 1;
	private int saturationThreshold = 2;
	private int valueThreshold = 3;
	
	private float borderRed;
	private float borderGreen;
	private float borderBlue;
	private float borderHue;
	private float borderSaturation;
	private float borderValue;
	
	/**
	 * Creates an ImageLineFiller with default parameters.
	 * Default pixel change color is black.
	 */
	public ImageFiller() {
	}
	
	/* (non-Javadoc)
	 * @see controller.AbstractTransformer#getID()
	 */
	public int getID() { return ID_FLOODER; } 
	
	protected boolean mouseClicked(MouseEvent e){
		List intersectedObjects = Selector.getDocumentObjectsAtLocation(e.getPoint());
		if (!intersectedObjects.isEmpty()) {
			Shape shape = (Shape)intersectedObjects.get(0);
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
				if (0 <= ptTransformed.x && ptTransformed.x < currentImage.getImageWidth() &&
				    0 <= ptTransformed.y && ptTransformed.y < currentImage.getImageHeight()) {
					currentImage.beginPixelUpdate();
					colorToFill = currentImage.getPixel(ptTransformed.x,ptTransformed.y);
					if(isFloodFill() && !colorToFill.equals(fillColor)){						
						floodFill(ptTransformed.x, ptTransformed.y);
					}
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
	 * Flood fill with specified color
	 */
	private void floodFill(int x , int y) {
		if(0 <= x && x < currentImage.getImageWidth() &&
				0 <= y && y < currentImage.getImageWidth() &&
					currentImage.getPixel(x,y).equals(colorToFill)){
			currentImage.setPixel(x,y, fillColor);
			floodFill(x+1,y);
			floodFill(x-1,y);
			floodFill(x,y+1);
			floodFill(x,y-1);
		}
	}
	
	/**
	 * boundary fill with specified color
	 */
	private void boundaryFill(int x , int y) {
		if(0 <= x && x < currentImage.getImageWidth() &&
				0 <= y && y < currentImage.getImageWidth() &&
					!currentImage.getPixel(x,y).equals(borderColor) 
						&& !currentImage.getPixel(x,y).equals(fillColor) ){
			currentImage.setPixel(x,y, fillColor);
			boundaryFill(x+1,y);
			boundaryFill(x-1,y);
			boundaryFill(x,y+1);
			boundaryFill(x,y-1);
		}
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
		borderRed = (float)borderColor.getRed()/255;
		borderGreen = (float)borderColor.getGreen()/255;
		borderBlue = (float)borderColor.getBlue()/255;
		
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
