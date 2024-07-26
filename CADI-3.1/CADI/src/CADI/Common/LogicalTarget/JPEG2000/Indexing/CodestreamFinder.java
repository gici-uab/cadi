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

/**
 * 
 * <p>
 * Further information, see ISO/IEC 15444-9 section I.3.2.2
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2009/09/12
 */
public class CodestreamFinder {

	/**
	 * 
	 */
	public int dr = -1;
	
	/**
	 * 
	 */
	public int cont = -1;
	public static final int ENTIRE_CODESTREAM = 0;
	public static final int FRAGMENTED_CODESTREAM = 1;
	
	/**
	 * 
	 */
	public long coff = -1;
	
	/**
	 * 
	 */
	public long clen = -1;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public CodestreamFinder() {
	}
	
	/**
	 * Constructor.
	 * 
	 * @param dr definition in {@link #dr}.
	 * @param cont defintion in {@link #cont}.
	 * @param coff definition in {@link #coff}.
	 * @param clen definition in {@link #clen}.
	 */
	public CodestreamFinder(int dr, int cont, long coff, long clen) {
		this.dr = dr;
		this.cont = cont;
		this.coff = coff;
		this.clen = clen;
	}
	
	/**
	 * Sets the attributes to their initial values.
	 */
	public void reset() {
		dr = -1;
		cont = -1;
		coff = -1;
		clen = -1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";
		
		str = getClass().getName() + " [";
		str += "dr="+dr;
		str += ", cont="+cont;
		str += ", coff="+coff;
		str += ", clen="+clen;
		str += "]";
		
		return str;
	}
	
	/**
	 * Prints the Codestream Finder data out to the specified output stream.
	 * This method is useful for debugging.
	 *
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Manifest --");
		
		out.println("dr: "+dr);
		out.println("cont: "+cont);
		out.println("coff: "+coff);
		out.println("clen: "+clen);
		
		out.flush();
	}
	
}