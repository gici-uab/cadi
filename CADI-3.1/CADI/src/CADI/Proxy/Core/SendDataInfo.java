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
package CADI.Proxy.Core;

import java.io.PrintStream;

import CADI.Common.Network.JPIP.ClassIdentifiers;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2009/11/14
 */
public class SendDataInfo {
	
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#classIdentifier}
	 */
	public int classIdentifier;
	public static final int PRECINCT = ClassIdentifiers.PRECINCT;
	public static final int TILE_HEADER = ClassIdentifiers.TILE_HEADER;
	public static final int TILE_DATA = ClassIdentifiers.TILE;
	public static final int MAIN_HEADER = ClassIdentifiers.MAIN_HEADER;
	public static final int METADATA = ClassIdentifiers.METADATA;
	
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}. 
	 */
	public long inClassIdentifier;
	
	/**
	 * Indicates the number of layers (packets).
	 */
	public int layersOffset = -1;
	
	public int layersLength = -1;
		
	/**
	 * Indicates the number of bytes.
	 * <p>
	 * Default value is -1.
	 */
	public int bytesOffset = -1;
	
	public int bytesLength = -1;
	
  // ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public SendDataInfo() {
		reset();
	}
	
	/**
	 * 
	 * @param classIdentifier
	 * @param inClassIdentifier
	 */
	public SendDataInfo(int classIdentifier, long inClassIdentifier) {
		this.classIdentifier = classIdentifier;
		this.inClassIdentifier = inClassIdentifier;
	}
	
	/**
	 * Sets the attritutes to its initial values.
	 */
	public void reset() {
		classIdentifier = -1; 
		inClassIdentifier = -1;
		layersOffset = -1;
		layersLength = -1;
		bytesOffset = -1;
		bytesLength = -1;
	}
			
	/**
	 * For debugging purposes. 
	 */
  @Override
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";
		
		str += "Class=";
		switch(classIdentifier) {
			case PRECINCT:
				str += "Precinct";
				break;
			case TILE_HEADER:
				str += "Tile header";
				break;				
			case TILE_DATA:
				str += "Tile";
				break;
			case MAIN_HEADER:
				str += "Main header";
				break;
			case METADATA:
				str += "Metadata";
				break; 
		}
		
		str += ", inClassIdentifier="+inClassIdentifier;
		
		if (layersOffset >= 0)
			str += ", layersOffset="+layersOffset+", layersLength="+layersLength;

		if (bytesOffset >= 0)
			str += ", bytesOffset="+bytesOffset+", bytesLength="+bytesLength;
		
		
		str += "]";
		
		return str;
	}

	/**
	 * Prints this Cache Descriptor fields out to the
	 * specified output stream. This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Send Data Info--");		
		
		out.print("Class: ");
		switch(classIdentifier) {
			case PRECINCT:
				out.println("precinct");
				break;
			case TILE_HEADER:
				out.println("tile header");
				break;				
			case TILE_DATA:
				out.println("tile");
				break;
			case MAIN_HEADER:
				out.println("main header");
				break;
			case METADATA:
				out.println("metadata");
				break; 
		}
		
		out.println("inClassIdentifier: "+inClassIdentifier);
		
		if (layersOffset >= 0)
			out.println("layersOffset: "+layersOffset+"\nlayersLength: "+layersLength);

		if (bytesOffset >= 0)
			out.println("bytesOffset: "+bytesOffset+"\nbytesLength="+bytesLength);
						
		out.flush();
	}
	
}
