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
 * Further and detailed information, see ISO/IEC 15444-2 section A.3.8 
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2008/11/20
 */
public class MCCParameters {
	
	/**
	 *
	 */
	public int stage = -1;
	
	/**
	 * Is the spected index of MCC to be read.
	 * <p>
	 * This attribute is only used when the MCC marker is read.
	 */
	public int Zmcc = 0;
	
	/**
	 * Is the index of the last Zmcc.
	 * <p>
	 * This attribute is only used when the MCC marker is read.
	 */
	public int Ymcc = 0;
	
	/**
	 * Number of collections.
	 * <p>
	 * This attribute is only used when the MCC marker is read.
	 */
	public int Qmcc = 0;
	
	/**
	 * Type of multiple component transformation
	 * <p>
	 * Valid values are:<br>
	 *   <ul>
	 *     <li> 0 - Array-based dependency transform
	 *     <li> 1 - Array-based decorrelation transform
	 *     <li> 3 - Wavelet-based transform
	 *   </ul>
	 */
	public int[] MCTType = null;
		
	/**
	 * 
	 */
	public int[][] inputIntermediateComponents = null;
	
	/**
	 * 
	 */
	public int[][] outputIntermediateComponents = null;
	
	/**
	 * 
	 */
	public int[] DWTType = null;
	
	/**
	 * 
	 */
	public int[] DWTLevels = null;
	
	/**
	 * 
	 */
	public int[] indexMCTOffsets = null;
	
	/**
	 * 
	 */
	public int[] componentOffsets = null;

	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param stage definition in {@link #stage}.
	 */
	public MCCParameters(int stage) {
		this.stage = stage;
	}
	
	/**
	 * Deep copy constructor.
	 * 
	 * @param parameters an object of this class.
	 */
	public MCCParameters(MCCParameters parameters) {
		
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
	 * Prints this MCC parameters' fields to the specified output stream.
	 * This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {
		out.println("-- MCC prameters --");
		if (stage >= 0) {
			out.println("index: "+stage);
			
			out.print("MCCType: ");
			for (int i = 0; i < MCTType.length-1; i++) {
				switch(MCTType[i]) {
					case 0: out.print("array-based dependency, "); break;
					case 1: out.print("array-based decorrelation, "); break;
					case 3: out.print("wavelet-based transform, "); break;
				}
			}
			switch(MCTType[MCTType.length-1]) {
				case 0: out.println("array-based dependency"); break;
				case 1: out.println("array-based decorrelation"); break;
				case 3: out.println("wavelet-based transform"); break;
			}
			
			out.print("Input intermediate components: ");
			for (int i = 0; i < inputIntermediateComponents.length; i++) {
				out.print("{");
				for (int j = 0; j < inputIntermediateComponents[i].length-1; j++) {
					out.print(inputIntermediateComponents[i][j]+",");
				}
				out.print(inputIntermediateComponents[i][inputIntermediateComponents[i].length-1]+"}");
			}
			out.println();
			
			out.print("Output intermediate components: ");
			for (int i = 0; i < outputIntermediateComponents.length; i++) {
				out.print("{");
				for (int j = 0; j < outputIntermediateComponents[i].length-1; j++) {
					out.print(outputIntermediateComponents[i][j]+",");
				}
				out.print(outputIntermediateComponents[i][outputIntermediateComponents[i].length-1]+"}");
			}
			out.println();
			
			out.print("DWTType: ");
			for (int i = 0; i < MCTType.length; i++) {
				if (MCTType[i] == 1) {

				} else if (MCTType[i] == 2) {

				} else if (MCTType[i] == 3) {
					if (DWTType[i] == 0) out.println("DWT 9/7");
					else if (DWTType[i] == 1) out.println("DWT 5/3");
					else assert(true);
				} else {
					assert(true);
				}
				out.print( (i<DWTType.length-1) ? "," : "\n");
			}
			
			out.print("DWTLevels: ");
			for (int i = 0; i < DWTLevels.length; i++) {
				if (MCTType[i] == 1) {

				} else if (MCTType[i] == 2) {

				} else if (MCTType[i] == 3) {
					out.print(DWTLevels[i]);
				} else {
					assert(true);
				}
				out.print( (i<DWTLevels.length-1) ? "," : "\n");
			}
			
			
			out.print("indexMCTOffsets: ");
			for (int i = 0; i < indexMCTOffsets.length; i++) {
				if (MCTType[i] == 1) {

				} else if (MCTType[i] == 2) {

				} else if (MCTType[i] == 3) {
					out.print(indexMCTOffsets[i]);
				} else {
					assert(true);
				}
				out.print( (i<indexMCTOffsets.length-1) ? "," : "\n");
			}
			
			out.print("componentOffsets: ");
			for (int i = 0; i < componentOffsets.length; i++) {
				if (MCTType[i] == 1) {

				} else if (MCTType[i] == 2) {

				} else if (MCTType[i] == 3) {
					out.print(componentOffsets[i]);
				} else {
					assert(true);
				}
				out.print( (i<componentOffsets.length-1) ? "," : "\n");
			}
		}
		
		out.flush();
	}
	
}