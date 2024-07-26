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
import java.util.HashMap;
import java.util.Map;

/**
 * This class storages the image and tile size parameters (COM).
 * Further and detailed information, see ISO/IEC 15444-1 section A.5.1
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.3 2011/12/14
 */
public class COMParameters {
	
	public int[] kduLayerLogSlopes = null;
	public long[] kduLengths = null;
	
	public int[] boiSlopes = null;
	public long[] boiLengths = null;
	
	/**
	 * Contains a map with the relevance of each precinct to be applied in the
	 * deliver policy.
	 */
	public HashMap<Long, Float> predictiveModel = null; 
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public COMParameters() {
		
	}
	
	/**
	 * Constructor. This constructor is useful to perform a deep copy.
	 * 
	 * @param parameters an object of this class.
	 */
	public COMParameters(COMParameters parameters) {
		if (parameters.kduLayerLogSlopes != null)
			kduLayerLogSlopes = Arrays.copyOf(parameters.kduLayerLogSlopes,
			                                  parameters.kduLayerLogSlopes.length);
		if (parameters.kduLengths != null)
			kduLengths = Arrays.copyOf(parameters.kduLengths,
			                           parameters.kduLengths.length);
		
		if (parameters.boiSlopes != null)
			boiSlopes = Arrays.copyOf(parameters.boiSlopes,
			                          parameters.boiSlopes.length);
		
		if (parameters.boiLengths != null)
			boiLengths = Arrays.copyOf(parameters.boiLengths,
			                           parameters.boiLengths.length);
		
		if (parameters.predictiveModel != null) {
      predictiveModel = new HashMap<Long,Float>();
			for (Map.Entry<Long, Float>entry : parameters.predictiveModel.entrySet())
				predictiveModel.put(entry.getKey(), entry.getValue());
    }
	}
	
	/**
	 * Sets the attributes to its initial values.
	 */
	public void reset() {
		kduLayerLogSlopes = null;
		kduLengths = null;
		
		boiSlopes = null;
		boiLengths = null;
			
		if (predictiveModel != null) {
			predictiveModel.clear();
			predictiveModel = null;
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";
		str = getClass().getName() + " [";
		
		if (kduLayerLogSlopes != null) {
			str += "Kdu layer info (R-D,length)={";			
			for (int i = 0; i < kduLayerLogSlopes.length-1; i++)
				str += "{"+kduLayerLogSlopes[i]+","+kduLengths[i]+"}";
			str += "{"+kduLayerLogSlopes[kduLayerLogSlopes.length-1]+","
				+kduLengths[kduLengths.length-1]+"}}";
		}
		
		if (boiSlopes != null) {
			str += "BOI layer info (slopes,length): {";
			for (int i = 0; i < boiSlopes.length-1; i++)
				str += "{"+boiSlopes[i]+","+boiLengths[i]+"}";
			str +="{"+boiSlopes[boiSlopes.length-1]+","+
				boiLengths[boiLengths.length-1]+"}}";
		}
		
		if (predictiveModel != null) {
			str += "Predictive model (precinct,relevance): {";
			for (Map.Entry<Long, Float>entry : predictiveModel.entrySet()) {
				str += "{"+entry.getKey()+","+entry.getValue()+"} ";
			}
			str += "}";
		}
		
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

		out.println("-- COM prameters --");
		
		if (kduLayerLogSlopes != null) {
			out.print("Kdu layer info (R-D,length): {");			
			for (int i = 0; i < kduLayerLogSlopes.length-1; i++)
				out.print("{"+kduLayerLogSlopes[i]+","+kduLengths[i]+"}");
			out.println("{"+kduLayerLogSlopes[kduLayerLogSlopes.length-1]+","
				+kduLengths[kduLengths.length-1]+"}}");
		}
		
		if (boiSlopes != null) {
			out.print("BOI layer info (slopes,length): {");			
			for (int i = 0; i < boiSlopes.length-1; i++)
				out.print("{"+boiSlopes[i]+","+boiLengths[i]+"}");
			out.println("{"+boiSlopes[boiSlopes.length-1]+","+
				boiLengths[boiLengths.length-1]+"}}");
		}

		if (predictiveModel != null) {
			out.println("Predictive model (precinct,relevance):");
			for (Map.Entry<Long, Float>entry : predictiveModel.entrySet()) {
				out.println("\t{"+entry.getKey()+","+entry.getValue()+"} ");
			}
		}
		
		out.flush();
	}
	
}