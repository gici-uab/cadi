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
package CADI.Server.LogicalTarget.JPEG2000.DeliveringModes;

import java.io.PrintStream;

/**
 * It is an auxiliary class used only by CoRDDelivery and CoRDBasedDelivery
 * classes.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/03/05
 */
public class BlockOrder {

	long inClassIdentifier = 0;
  int tile = 0;
	int component = 0;
	int rLevel = 0;
	int subband = 0;
	int numBitPlanes = 0;
	int virtualCodingLevel = 0;
	int concat = 0;

	/**
	 * Constructor.
	 * 
	 * @param inClassIdentifier
	 * @param component
	 * @param rLevel
	 * @param subband
	 * @param numBitPlanes
	 * @param virtualCodingLevel
	 * @param concat
	 */
	public BlockOrder(long inClassIdentifier, int tile, int component, int rLevel, int subband, int numBitPlanes, int virtualCodingLevel, int concat) {
		this.tile = tile;
    this.inClassIdentifier = inClassIdentifier;
		this.component = component;
		this.rLevel = rLevel;
		this.subband = subband;
		this.numBitPlanes = numBitPlanes;
		this.virtualCodingLevel = virtualCodingLevel;
		this.concat = concat;
	}
	
	/**
	 * 
	 * @param out
	 */
	public void list(PrintStream out) {
    out.println("tile: "+tile);
		out.println("\tinClassIdentifier: "+inClassIdentifier);
		out.println("\tcomponent: "+component);
		out.println("\trLevel: "+rLevel);
		out.println("\tsubband: "+subband);
		out.println("\tnumBitPlanes: "+numBitPlanes);
		out.println("\tvirtualCodingLevel: "+virtualCodingLevel);
		out.println("\tconcat: "+concat);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";
		str += inClassIdentifier+"\t"+tile+"\t"+component+"\t"+rLevel+"\t"+subband
            +"\t"+numBitPlanes+"\t"+virtualCodingLevel+"\t"+concat;
		return str;
	}
}
