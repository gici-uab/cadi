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

/**
 * 
 * Further and detailed information, see ISO/IEC 15444-2 section A.3.7
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2008/11/20
 */
public class MCTParameters {

	
	/**
	 * Is the spected index of MCT to be read.
	 * <p>
	 * This attribute is only used when the MCT marker is read.
	 */
	public int Zmct = 0;
	
	/**
	 * Is the index of the last Zmct.
	 * <p>
	 * This attribute is only used when the MCT marker is read.
	 */
	public int Ymct = 0;
	
// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public MCTParameters(int index) {
		
	}
	
	/**
	 * Deep copy constructor.
	 * 
	 * @param parameters an object of this class.
	 */
	public MCTParameters(MCTParameters parameters) {
		
	}
	
	/**
	 * Sets the attributes to its initial values.
	 */
	public void reset() {
		
	}
	
	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";
		str = getClass().getName() + " [";
		
		
		str += "]";
		
		return str;
	}
	
	/**
	 * Prints this MCT parameters' fields to the specified output stream.
	 * This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {
		out.println("-- MCT prameters --");
		
		
		
		out.flush();
	}
	
}