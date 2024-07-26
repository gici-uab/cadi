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
 * This class storages the quantization default (QCD) and the quantization
 * component (QCC).
 * 
 * Further and detailed information, see ISO/IEC 15444-1 section A.6.4 and
 * section A.6.5, respectively.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 07/11/2008
 */
public class QCDParameters {

	/**
	 * Type of quantization applied.
	 * <p>
	 * Valid values are:<br>
	 *   <ul>
	 *     <li> 0 - Reversible quantization
	 *     <li> 1 - Irreversible quantization
	 *   </ul>
	 */
	public int quantizationStyle = -1;	// Legacy name was QTypes
	
	/**
	 * Quantization exponents for each resolution level and subband (LL, HL, LH, HH).<br>
	 * Index of arrays means:<br>
	 * &nbsp; resolutionLevel: 0 is the LL subband, and 1, 2, ... represents next starting with the little one<br>
	 * &nbsp; subband: 0 - HL, 1 - LH, 2 - HH (if resolutionLevel == 0 --> 0 - LL)
	 * <p>
	 * Values between 0 to 2^5 allowed.
	 */
	public int[][] exponents = null;
	
	/**
	 * Quantization mantisas for each resolution level and subband (LL, HL, LH, HH).<br>
	 * Index of arrays means:<br>
	 * &nbsp; resolutionLevel: 0 is the LL subband, and 1, 2, ... represents next starting with the little one<br>
	 * &nbsp; subband: 0 - HL, 1 - LH, 2 - HH (if resolutionLevel == 0 --> 0 - LL)<br>
	 * <p>
	 * Values between 0 to 2^11 allowed.
	 */
	public int[][] mantisas = null;
	
	/**
	 * Guard Bits are used to increment the number of possible bit planes when
	 * subbands are shifted. It is very recommendable that this variable has
	 * values equal or greater than one to avoid possible errors.
	 * <p>
	 * Possible values are from 0 to 7.
	 */
	public int guardBits;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public QCDParameters() {
		
	}
	
	/**
	 * Deep copy constructor.
	 * 
	 * @param parameters an object of this class.
	 */
	public QCDParameters(QCDParameters parameters) {
		quantizationStyle = parameters.quantizationStyle;
		
		exponents = new int[parameters.exponents.length][];
		mantisas = new int[parameters.mantisas.length][];
		for (int r = 0; r < exponents.length; r++) {
			exponents[r] = parameters.exponents[r];
			mantisas[r] = parameters.mantisas[r];
		}

		guardBits = parameters.guardBits;
	}
	
	/**
	 * Sets the attributes to its initial values.
	 */
	public void reset() {
		quantizationStyle = -1;
		exponents = null;
		mantisas = null;
		guardBits = -1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";
		str = getClass().getName() + " [";
		
		str += "Quantization style={";
		switch(quantizationStyle) {
			case 0:
				str += "Reversible";
				break;
			case 1:
				str += "Irreversible derived";
				break;
			case 2:
				str += "Irreversible expounded";
				break;
			default:
				assert(true);
		}
		
		str += " Exponents={";
		for (int r = 0; r < exponents.length; r++) {
			str += "{";
			for (int s = 0; s < exponents[r].length-1; s++) {
				str += exponents[r][s]+",";
			}
			str += exponents[r][exponents[r].length-1]+"}";
			str += r < (exponents.length-1) ? ", ": "}";
		}
		
		str += " Mantisas={";
		for (int r = 0; r < mantisas.length; r++) {
			str += "{";
			for (int s = 0; s < mantisas[r].length-1; s++) {
				str += mantisas[r][s]+",";
			}
			str += mantisas[r][mantisas[r].length-1]+"}";
			str += r < (mantisas.length-1) ? ", ": "}";
		}
		
		str += " Guard bits="+guardBits;
		
		str += "]";
		return str;
	}
	
	/**
	 * Prints this QCD parameters' fields to the specified output stream.
	 * This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- QCD prameters --");		
	
		out.print("Quantization style: ");
		switch(quantizationStyle) {
			case 0:
				out.println("Reversible");
				break;
			case 1:
				out.println("Irreversible derived");
				break;
			case 2:
				out.println("Irreversible expounded");
				break;
			default:
				assert(true);
		}

		out.print("Exponents: ");
		for (int r = 0; r < exponents.length; r++) {
			out.print("{");
			for (int s = 0; s < exponents[r].length-1; s++) {
				out.print(exponents[r][s]+",");
			}
			out.print(exponents[r][exponents[r].length-1]+"}");
		}
		out.println();

		out.print("Mantisas: ");
		for (int r = 0; r < mantisas.length; r++) {
			out.print("{");
			for (int s = 0; s < mantisas[r].length-1; s++) {
				out.print(mantisas[r][s]+",");
			}
			out.print(mantisas[r][mantisas[r].length-1]+"}");
		}
		out.println();
		
		out.println("Guard bits: "+guardBits);
		
		out.flush();
	}
}