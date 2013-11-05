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

import java.awt.event.MouseEvent;
import java.util.List;

import model.ImageDouble;
import model.ImageX;
import model.KernelModel;
import model.Pixel;
import model.PixelDouble;
import model.Shape;

/**
 * 
 * <p>Title: FilteringTransformer</p>
 * <p>Description: ... (AbstractTransformer)</p>
 * <p>Copyright: Copyright (c) 2004 S�bastien Bois, Eric Paquette</p>
 * <p>Company: (�TS) - �cole de Technologie Sup�rieure</p>
 * @author unascribed
 * @version $Revision: 1.6 $
 */
public class FilteringTransformer extends AbstractTransformer{
	CustomFilter3x3 filter = new CustomFilter3x3( new PaddingZeroStrategy(), new ImageClampStrategy() );
	
	/**
	 * @param _coordinates
	 * @param _value
	 */
	public void updateKernel(Coordinates _coordinates, float _value) {
		filter.updateKernel( _coordinates, _value );
	}
		
	/**
	 * 
	 * @param e
	 * @return
	 */
	protected boolean mouseClicked(MouseEvent e){
		List intersectedObjects = Selector.getDocumentObjectsAtLocation(e.getPoint());
		if (!intersectedObjects.isEmpty()) {			
			Shape shape = (Shape)intersectedObjects.get(0);			
			if (shape instanceof ImageX) {				
				ImageX currentImage = (ImageX)shape;
				ImageDouble filteredImage = filter.filterToImageDouble(currentImage);
				ImageX filteredDisplayableImage = filter.getImageConversionStrategy().convert(filteredImage);
				currentImage.beginPixelUpdate();
				
				for (int i = 0; i < currentImage.getImageWidth(); ++i) {
					for (int j = 0; j < currentImage.getImageHeight(); ++j) {
						currentImage.setPixel(i, j, filteredDisplayableImage.getPixelInt(i, j));
					}
				}
				currentImage.endPixelUpdate();
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see controller.AbstractTransformer#getID()
	 */
	public int getID() { return ID_FILTER; }

	/**
	 * Sets the image border strategy according to the choice made in the GUI
	 * Uses Zero-padding as default value when a selected choice is not implemented
	 * @param string The border strategy name
	 */
	public void setBorder(String string) {
		System.out.println(string);
		int index = 0;
		for (int i = 0; i < KernelModel.HANDLING_BORDER_ARRAY.length; ++i) {
			if (string.equals(KernelModel.HANDLING_BORDER_ARRAY[i])) {
				index = i;
			}
		}
		
		switch ( index ) {
		case 0:
			System.out.println("Using Zero-padding strategy (#0)");
			this.filter.setPaddingStrategy( new PaddingZeroStrategy() );
			break;
		//case 1:
		//	System.out.println("None (1)");
		//	break;
		//case 2:
		//	System.out.println("copy (2)");
		//	break;
		case 3:
			System.out.println("Using Mirror-padding strategy (#3)");
			this.filter.setPaddingStrategy( new PaddingMirrorStrategy() );
			break;
			
		//case 4:
		//	System.out.println("Circular (4)");
		//	break;

		default:
			System.out.println("Choice not implemented : using Zero-padding strategy");
			this.filter.setPaddingStrategy( new PaddingZeroStrategy() );
			break;
		}
		
	}

	/**
	 * Sets the image value clamping strategy according to the choice made in the GUI
	 * Uses CLAMP 0-255 as default value when a selected choice is not implemented
	 * @param string The clamp strategy name
	 */
	public void setClamp(String string) {
		System.out.println(string);
		int index = 0;
		for (int i = 0; i < KernelModel.CLAMP_ARRAY.length; ++i) {
			if (string.equals(KernelModel.CLAMP_ARRAY[i])) {
				index = i;
			}
		}
		
		switch ( index ) {
		case 0:
			// Clamp 0-255
			System.out.println("Using Clamp 0-255 strategy (#0)");
			this.filter.setImageConversionStrategy( new ImageClampStrategy() );
			break;

		case 3:
			// Normalize 0-255
			System.out.println("Using Normalize 0-255 strategy (#3)");
			this.filter.setImageConversionStrategy( new ImageNormalizeStrategy() );
			break;
		default:
			System.out.println("Choice not implemented : using Clamp 0-255 strategy");
			this.filter.setImageConversionStrategy( new ImageClampStrategy() );
			break;
		}
	}
}
