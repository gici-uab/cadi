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
 * 
 * Further and detailed information, see ISO/IEC 15444-2 section A.3.6.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2008/11/19
 */
public class CBDParameters {

	/**
	 * Number of bits per sample used for each component.
	 * <p>
	 * Only positive values allowed.
	 */
	public int[] MCTPrecision = null;
	
	/**
	 * Indicates whether components are signed (positive and negative values) or not.
	 * <p>
	 * True if signed, false otherwise.
	 */
	public boolean[] MCTSigned = null;
	
  // ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public CBDParameters() {
	}
	
	/**
	 * Deep copy constructor.
	 * 
	 * @param parameters an object of this class.
	 */
	public CBDParameters(CBDParameters parameters) {
		if (parameters.MCTPrecision != null)
			MCTPrecision = Arrays.copyOf(parameters.MCTPrecision, parameters.MCTPrecision.length);

		if (parameters.MCTSigned != null)
			MCTSigned = Arrays.copyOf(parameters.MCTSigned, parameters.MCTSigned.length);
	}
	
	/**
	 * Sets the attributes to its initial values.
	 */
	public void reset() {
		MCTPrecision = null;
		MCTSigned = null;
	}
	
	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";
		str = getClass().getName() + " [";

		if (MCTPrecision != null) {
			str += " Precision={";
			for (int i = 0; i < MCTPrecision.length-1; i++) str += MCTPrecision[i]+",";
			str += MCTPrecision[MCTPrecision.length-1]+"}";
		}

		if (MCTSigned !=  null) {
			str += " Sign={";
			for (int i = 0; i < MCTSigned.length-1; i++) str += MCTSigned[i]+",";
			str += MCTSigned[MCTSigned.length-1]+"}";
		}
		
		str += "]";
		
		return str;
	}
	
	/**
	 * Prints this CBD parameters' fields to the specified output stream.
	 * This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {
		out.println("-- CBD prameters --");
		
		if (MCTPrecision != null) {
			out.print("Precision={");
			for (int i = 0; i < MCTPrecision.length-1; i++) out.print(MCTPrecision[i]+",");
			out.println(MCTPrecision[MCTPrecision.length-1]+"}");
		}

		if (MCTSigned !=  null) {
			out.print("Sign={");
			for (int i = 0; i < MCTSigned.length-1; i++) out.print(MCTSigned[i]+",");
			out.println(MCTSigned[MCTSigned.length-1]+"}");
		}
		
		out.flush();
	}
	
}