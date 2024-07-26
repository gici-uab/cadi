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
 * classes 
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/03/05
 */
public class CodingPassID {
	long inClassIdentifier = -1;
  int tile = -1;
	int component = -1;
	int rLevel = -1;
	int precinct = -1;
	int subband = -1;
	int yBlock = -1;
	int xBlock = - 1;
	int codingLevel = -1;
	int layer = -1;
	
	/**
	 * 
	 * @param inClassIdentifier
	 * @param component
	 * @param rLevel
	 * @param precinct
	 * @param subband
	 * @param yBlock
	 * @param xBlock
	 * @param codingLevel
	 */
	public CodingPassID(long inClassIdentifier, int tile, int component, int rLevel, int precinct, int subband, int yBlock, int xBlock, int codingLevel) {
    this.tile = tile;
		this.inClassIdentifier = inClassIdentifier;
		this.component = component;
		this.rLevel = rLevel;
		this.precinct = precinct;
		this.subband = subband;
		this.yBlock = yBlock;
		this.xBlock = xBlock;
		this.codingLevel = codingLevel;
	}
	
	/**
	 * 
	 * @param out
	 */
	public void list(PrintStream out) {
		out.println("inClassIdentifier: "+inClassIdentifier);
    out.println("\ttile: "+tile);
		out.println("\tcomponent: "+component);
		out.println("\trLevel: "+rLevel);
		out.println("\tprecinct: "+precinct);
		out.println("\tsubband: "+subband);
		out.println("\tyBlock: "+yBlock);
		out.println("\txBlock: "+xBlock);
		out.println("\tcodingLevel: "+codingLevel);
		out.flush();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";
		str += inClassIdentifier+"\t"+tile+"\t"+component+"\t"+rLevel+"\t"+precinct+"\t"+subband+"\t"+yBlock+"\t"+xBlock+"\t"+codingLevel+"\t"+layer;
		return str;
	}
}
