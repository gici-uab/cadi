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
package CADI.Common.LogicalTarget.JPEG2000.Indexing;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2009/09/12
 */
public class Manifest {

	/**
	 * <p>
	 * Further information, see ISO/IEC 15444-9 section I.3.2.3.
	 */
	public ArrayList<byte[]> boxHeaders = null;
		
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public Manifest() {
		boxHeaders = new ArrayList<byte[]>();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param bh definition in {@link #bh}.
	 */
	public Manifest(ArrayList<byte[]> bh) {
		if (bh == null) throw new NullPointerException();
		
		this.boxHeaders = bh;
	}
	
	/**
	 * Adds a new box header.
	 * 
	 * @param bh definition in {@link #bh}.
	 */
	public void add(byte[] bh) {
		if (bh == null) throw new NullPointerException();
		
		this.boxHeaders.add(bh);
	}
	
	/**
	 * Sets the attributes to their initial values.
	 */
	public void reset() {
		for (byte[] tmp : boxHeaders) tmp = null;
		boxHeaders.clear();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";
		
		str = getClass().getName() + " [";
		for (byte[] tmp : boxHeaders) {
			for (int i = 0; i < tmp.length; i++) {
				if ((0xFF & tmp[i]) < 16) str += "0";
				str += Integer.toHexString(0xFF&tmp[i]);							
			}
			str += "\t";
		}
		str += "]";
		
		return str;
	}
	
	/**
	 * Prints the Manifest data out to the specified output stream.
	 * This method is useful for debugging.
	 *
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Manifest --");
		
		for (byte[] tmp : boxHeaders) {
			for (int i = 0; i < tmp.length; i++) {
				if ((0xFF & tmp[i]) < 16) out.print("0");
				out.print(Integer.toHexString(0xFF&tmp[i]));							
			}
			out.println();
		}
		
		out.flush();
	}
		
}