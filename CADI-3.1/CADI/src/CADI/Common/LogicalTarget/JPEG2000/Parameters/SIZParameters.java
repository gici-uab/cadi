/*
 * CADI Software - a JPIP Client/Server framework
 * Copyright (C) 2007-2012  Group on Interactive Coding of Images (GICI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Group on Interactive Coding of Images (GICI)
 * Department of Information and Communication Engineering
 * Autonomous University of Barcelona
 * 08193 - Bellaterra - Cerdanyola del Valles (Barcelona)
 * Spain
 *
 * http://gici.uab.es
 * gici-info@deic.uab.es
 */
package CADI.Common.LogicalTarget.JPEG2000.Parameters;

import java.io.PrintStream;
import java.util.Arrays;

/**
 * This class storages the image and tile size parameters (SIZ).
 * Further and detailed information, see ISO/IEC 15444-1 section A.5.1
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2008/11/07
 */
public class SIZParameters {

	/**
	 *  Denotes capabilities that a decoder needs to properly decode the
	 *  codestream
	 *  <p>
	 *  NOTE:
	 *  It would be better defined if it is considered like a enumeration.
	 *  Therefore, the attribute name could be 'capabilities' as is defined
	 *  in the standard, and it would contain only the required capabilities.
	 *  It would avoid to used masks to extract capabilities.	 
	 */
	public int Rsiz = 0;
	
	/**
	 * Image width.
	 * <p>
	 * Negative values are not allowed for this field.
	 */
	public int xSize = -1;
	
	/**
	 * Image height.
	 * <p>
	 * Negative values are not allowed for this field.
	 */
	public int ySize = -1;
	
	/**
	 * Horizontal offset.
	 * <p>
	 * Horitzontal offset from origin of the reference grid to the left side of the image area
	 */
	public int XOsize = -1;
	
	/**
	 * Vertical offset.
	 * <p>
	 * Vertical offset from origin of the reference grid to the left side of the image area
	 */
	public int YOsize = -1;
	
	/**
	 * Horizontal tile size.
	 * <p>
	 * Width of one reference tile with respect ot the reference grid.
	 */
	public int XTsize = -1;
	
	/**
	 * Vertical tile size.
	 * <p>
	 * Height of one reference tile with respect ot the reference grid.
	 */
	public int YTsize = -1;
	
	/**
	 * Horizontal offset Tile
	 * <p>
	 * Horitzontal offset from the origin of the reference grid to the left side of the first tile
	 */
	public int XTOsize = -1;
	
	/**
	 * Vertical offset Tile
	 * <p>
	 * Vertical offset from the origin of the reference grid to the top side of the first tile
	 */
	public int YTOsize = -1;
	
	/**
	 * Number of image components.
	 * <p>
	 * Negative values are not allowed for this field.
	 */
	//public int Csize = -1;
	public int zSize = -1;
	
	/**
	 * Number of bits per sample used for each component.
	 * <p>
	 * Only positive values allowed.
	 */
	public int[] precision = null;
	
	/**
	 * Indicates whether components are signed (positive and negative values) or not.
	 * <p>
	 * True if signed, false otherwise.
	 */
	public boolean[] signed = null;
	
	/**
	 * Horizontal separation of sample of ith component with respect to the
	 * reference grid. There is one occurrence of this parameter for each
	 * component.
	 * <p>
	 * Only positive values shorter than 255 are allowed.
	 */
	public int[] XRsiz = null;
	
	/**
	 * Vertical separation of sample of ith component with respect to the
	 * reference grid. There is one occurrence of this parameter for each
	 * component.
	 * <p>
	 * Only positive values shorter than 255 are allowed.
	 */
	public int[] YRsiz = null;
	
	// ============================= public methods ==============================
	/**
	 * Constructor. 
	 */
	public SIZParameters() {
		
	}
	
	/**
	 * Deep copy constructor.
	 * 
	 * @param parameters
	 */
	public SIZParameters(SIZParameters parameters) {
		Rsiz = parameters.Rsiz;
		xSize = parameters.xSize;
		ySize = parameters.ySize;
		XOsize = parameters.XOsize;
		YOsize = parameters.YOsize;
		XTsize = parameters.XTsize;
		YTsize = parameters.YTsize;
		XTOsize = parameters.XTOsize;
		YTOsize = parameters.YTOsize;
		zSize = parameters.zSize;
		precision = Arrays.copyOf(parameters.precision, parameters.precision.length);
		signed = Arrays.copyOf(parameters.signed, parameters.signed.length);
		XRsiz = Arrays.copyOf(parameters.XRsiz, parameters.XRsiz.length);
		YRsiz = Arrays.copyOf(parameters.YRsiz, parameters.YRsiz.length);
	}
	
	/**
	 * Sets the attributes to its initial values.
	 */
	public void reset() {
		Rsiz = 0;
		xSize = -1;
		ySize = -1;
		XOsize = -1;
		YOsize = -1;
		XTsize = -1;
		YTsize = -1;
		XTOsize = -1;
		YTOsize = -1;
		zSize = -1;
		precision = null;
		signed = null;
		XRsiz = null;
		YRsiz = null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";
		str = getClass().getName() + " [";
		
		str += "Rsiz="+Integer.toBinaryString(Rsiz);
		str += " Xsize="+xSize;
		str += " Ysize="+ySize;
		str += " XOsize="+XOsize;
		str += " YOsize="+YOsize;
		str += " XTsize="+XTsize;
		str += " YTsize="+YTsize;
		str += " XTOsize="+XTOsize;
		str += " YTOsize="+YTOsize;
		str += " Csize="+zSize;
		
		str += " Precision={";
		for (int i = 0; i < precision.length-1; i++) str += precision[i]+",";
		str += precision[precision.length-1]+"}";
		
		str += " Sign={";
		for (int i = 0; i < signed.length-1; i++) str += signed[i]+",";
		str += signed[signed.length-1]+"}";
		
		str += " XRsiz={";
		for (int i = 0; i < XRsiz.length-1; i++) str += XRsiz[i]+",";
		str += XRsiz[XRsiz.length-1]+"}";
		
		str += " YRsiz={";
		for (int i = 0; i < YRsiz.length-1; i++) str += YRsiz[i]+",";
		str += YRsiz[YRsiz.length-1]+"}";
	
		str += "]";
		return str;
	}
	
	/**
	 * Prints this SIZ parameters' fields to the specified output stream.
	 * This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- SIZ prameters --");		
		out.println("Rsiz="+Integer.toBinaryString(Rsiz));
		out.println("Xsize="+xSize);
		out.println("Ysize="+ySize);
		out.println("XOsize="+XOsize);
		out.println("YOsize="+YOsize);
		out.println("XTsize="+XTsize);
		out.println("YTsize="+YTsize);
		out.println("XTOsize="+XTOsize);
		out.println("YTOsize="+YTOsize);
		out.println("Csize="+zSize);
		
		out.print("Precision={");
		if (precision != null) {
			for (int i = 0; i < precision.length-1; i++) out.print(precision[i]+",");
			out.println(precision[precision.length-1]+"}");
		} else {
			out.println("null}");
		}
		
		out.print("Sign={");
		if (signed != null) {
			for (int i = 0; i < signed.length-1; i++) out.print(signed[i]+",");
			out.println(signed[signed.length-1]+"}");
		} else {
			out.println("null}");
		}

		out.print("XRsiz={");
		if (XRsiz != null) {
			for (int i = 0; i < XRsiz.length-1; i++) out.print(XRsiz[i]+",");
			out.println(XRsiz[XRsiz.length-1]+"}");
		} else {
			out.println("null}");
		}

		out.print("YRsiz={");
		if (YRsiz != null){
			for (int i = 0; i < YRsiz.length-1; i++) out.print(YRsiz[i]+",");
			out.println(YRsiz[YRsiz.length-1]+"}");
		} else {
			out.println("null}");
		}

		out.flush();
	}
	
}