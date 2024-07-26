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
 * This class storages the JPK parameters used in the JPK Headers.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2008/11/27
 */
public class JPKParameters {

	/**
	 * Shift type to apply.
	 * <p>
	 * Valid values are:<br>
	 *   <ul>
	 *     <li> 0 - No level shift
	 *     <li> 1 - JPEG2000 standard level shifting (only non-negative components)
	 *     <li> 2 - Range center substract
	 *     <li> 3 - Average substract
	 *     <li> 4 - Specific values substract (if this case is selectioned, then is necessary to pass LSSubsValues in setParameters function)
	 *   </ul>
	 */
	public int LSType = 0;
	
	/**
	 * Specification of the image component to apply the level shift.
	 * <p>
	 * If true the level shift will be applied, otherwise false.
	 */
	public 	boolean[] LSComponents = null;
	
	/**
	 * Substracted values of each image component.
	 * <p>
	 * If LSType == 4 then this variable cannot be null, otherwise it can be null in setParameters function
	 */
	public int[] LSSubsValues = null;
	
	/**
	 * Value used to modificate the range of each component using a multiplication.
	 * <p>
	 * Only values greater than 0 allowed.
	 */
	public float[] RMMultValues = null;
		
	/**
	 *  Dynamic Range value used to calculate the stepSize
	 */
	public int QDynamicRange = 0;
	

	// SHAPE ADAPTIVE
	
	/**
	 * Shape adaptive discrete wavelet transform to be applied for each component.
	 * <p>
	 * Valid values are:<br>
	 *   <ul>
	 *     <li> 0 - None
	 *     <li> 1 - Shape adaptive wavelet transform
	 *   </ul>
	 */
	public int[] waveletSA = null;
	
	/**
	 * Performs the shape adaptive version for the JPEG2000 bit plane encoder scheme.
	 * <p>
	 * Valid values are:<br>
	 *   <ul>
	 *     <li> 0 - No shape adaptive bit plane encoder (standard version)
	 *     <li> 1 - Shape adaptive bit plane encoder (No standard version)
	 *   </ul>
	 */
	public int[] bitPlaneEncodingSA = null;
	
	/**
	 * Roi type to be applied to the image.
	 * <p>
	 * Valid values are:<br>
	 *   <ul>
	 *     <li> 1 - MaxShift
	 *   </ul>
	 */
	public int RroiType = 0;
	
	/**
	 * Roi values used to each Roi. The first dimension indicates the specific Roi, and the second one can be a sequence number which define the parameters for this Roi.
	 * <p>
	 * Valid values are:<br>
	 *   <ul>
	 *     <li> 0 - Roi identification, which belong to the mask.
	 *     <li> 1 - Parameter for each roi, depending on the type of roi used, could be a sequence of values.
	 *   </ul>
	 */
	public int[] RroisParameters = null;
	
	/**
	 * Contain the scaling factor values for bitpalnesshift roi and for each component
	 */
	public int[] RBitplanesScaling = null;
	
		
	// PARAMETERS FOR 3D (SPECTRALLY TRANSFORMED IMAGES)
	
	/**
	 * Kind of 3D Discrete wavelet transform to be applied.
	 * <p>
	 * Valid values are:<br>
	 *   <ul>
	 *    <li> 0 - 2D DWT in the spatial domain
	 *    <li> 1 - 1D+2D non pyramidal Wavelet Transform
	 *    <li> 2 - 3D pyramidal Wavelet Transform
	 *    <li> 3 - Forward DPCM + Forward Wavelet Transform
	 *    <li> 4 - Forward DPCM + 1D+2D non pyramidal Wavelet Transform
	 *   </ul>
	 */
	public int WT3D = 0;
	
	/**
	 * Discrete wavelet transform type to be applied in the spectral domain.
	 */
	public int WSL = 0;
	
	/**
	 * Discrete wavelet transform levels to be applied in the spectral domain.
	 */
	public int WST = 0;	
	
	/**
	 * 
	 */
	public int DPCMRestartIndex = 0;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public JPKParameters() {
	}
	
	/**
	 * Deep copy constructor.
	 * 
	 * @param parameters an object of this class.
	 */
	public JPKParameters(JPKParameters parameters) {
		LSType = parameters.LSType;
		LSComponents = Arrays.copyOf(parameters.LSComponents, parameters.LSComponents.length);
		LSSubsValues = Arrays.copyOf(parameters.LSSubsValues, parameters.LSSubsValues.length);
		RMMultValues = Arrays.copyOf(parameters.RMMultValues, parameters.RMMultValues.length);
		QDynamicRange = 0;

		// Shape adaptive
		if (parameters.waveletSA != null) waveletSA = Arrays.copyOf(parameters.waveletSA, parameters.waveletSA.length);
		if (parameters.bitPlaneEncodingSA != null) bitPlaneEncodingSA = Arrays.copyOf(parameters.bitPlaneEncodingSA, parameters.bitPlaneEncodingSA.length);
		RroiType = parameters.RroiType;
		if (parameters.RroisParameters != null) RroisParameters = Arrays.copyOf(parameters.RroisParameters, parameters.RroisParameters.length);
		if (parameters.RBitplanesScaling != null) RBitplanesScaling = Arrays.copyOf(parameters.RBitplanesScaling, parameters.RBitplanesScaling.length);

		// Parameters for 3D (spectrally transformed images)
		WT3D = parameters.WT3D;
		WSL = parameters.WSL;
		WST = parameters.WST;	
		DPCMRestartIndex = parameters.DPCMRestartIndex;
	}

	/**
	 * Sets the attributes to its initial values.
	 */
	public void reset() {
		LSType = 0;
		LSComponents = null;
		LSSubsValues = null;
		RMMultValues = null;
		QDynamicRange = 0;

		// Shape adaptive
		waveletSA = null;
		bitPlaneEncodingSA = null;
		RroiType = 0;
		RroisParameters = null;
		RBitplanesScaling = null;

		// Parameters for 3D (spectrally transformed images)
		WT3D = 0;
		WSL = 0;
		WST = 0;	
		DPCMRestartIndex = 0;
	}

	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";
		str = getClass().getName() + " [";

		str += "LSType="+LSType;

		if (LSComponents != null) {
			str += " LSComponents={";
			for (int i = 0; i < LSComponents.length-1; i++) str += LSComponents[i]+",";
			str += LSComponents[LSComponents.length-1]+"}";
		}

		if (LSSubsValues != null) {
			str += " LSSubsValues={";
			for (int i = 0; i < LSSubsValues.length-1; i++) str += LSSubsValues[i]+",";
			str += LSSubsValues[LSSubsValues.length-1]+"}";
		}

		if (RMMultValues != null) {
			str += " RMMultValues={";
			for (int i = 0; i < RMMultValues.length-1; i++) str += RMMultValues[i]+",";
			str += RMMultValues[RMMultValues.length-1]+"}";
		}

		str += " QDynamicRange="+QDynamicRange;


		// Shape adaptive
		if (waveletSA != null) {
			str += " waveletSA={";
			for (int i = 0; i < waveletSA.length-1; i++) str += waveletSA[i]+",";
			str += waveletSA[waveletSA.length-1]+"}";
		}

		if (bitPlaneEncodingSA != null) {
			str += " bitPlaneEncodingSA={";
			for (int i = 0; i < bitPlaneEncodingSA.length-1; i++) str += bitPlaneEncodingSA[i]+",";
			str += bitPlaneEncodingSA[bitPlaneEncodingSA.length-1]+"}";
		}

		if (RroiType != 0) {
			str += " RroiType="+RroiType;
		}

		if (RroisParameters != null) {
			str += " RroisParameters={";
			for (int i = 0; i < RroisParameters.length-1; i++) str += RroisParameters[i]+",";
			str += RroisParameters[RroisParameters.length-1]+"}";
		}

		if (RBitplanesScaling != null) {
			str += " RBitplanesScaling={";
			for (int i = 0; i < RBitplanesScaling.length-1; i++) str += RBitplanesScaling[i]+",";
			str += RBitplanesScaling[RBitplanesScaling.length-1]+"}";
		}

		
		// Parameters for 3D (spectrally transformed images)
		str += " WT3D="+WT3D;
		str += " WSL="+WSL;
		str += " WST="+WST;
		str += " DPCMRestartIndex="+DPCMRestartIndex;
		
		str += "]";
		
		return str;
	}
	
	/**
	 * Prints this JPK parameters' fields to the specified output stream.
	 * This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {
		out.println("-- JPK prameters --");
		
		out.println("LSType: "+LSType);

		if (LSComponents != null) {
			out.print("LSComponents: ");
			for (int i = 0; i < LSComponents.length-1; i++) out.print(LSComponents[i]+",");
			out.println(LSComponents[LSComponents.length-1]);
		}

		if (LSSubsValues != null) {
			out.print("LSSubsValues: ");
			for (int i = 0; i < LSSubsValues.length-1; i++) out.print(LSSubsValues[i]+",");
			out.println(LSSubsValues[LSSubsValues.length-1]);
		}

		if (RMMultValues != null) {
			out.print("RMMultValues: ");
			for (int i = 0; i < RMMultValues.length-1; i++) out.print(RMMultValues[i]+",");
			out.println(RMMultValues[RMMultValues.length-1]);
		}

		if (QDynamicRange != 0) {
			out.println("QDynamicRange: "+QDynamicRange);
		}


		// Shape adaptive
		if (waveletSA != null) {
			out.print("waveletSA: ");
			for (int i = 0; i < waveletSA.length-1; i++) out.print(waveletSA[i]+",");
			out.println(waveletSA[waveletSA.length-1]);
		}

		if (bitPlaneEncodingSA != null) {
			out.print("bitPlaneEncodingSA: ");
			for (int i = 0; i < bitPlaneEncodingSA.length-1; i++) out.print(bitPlaneEncodingSA[i]+",");
			out.println(bitPlaneEncodingSA[bitPlaneEncodingSA.length-1]);
		}

		if (RroiType != 0) {
			out.print("RroiType: "+RroiType);
		}

		if (RroisParameters != null) {
			out.print("RroisParameters: ");
			for (int i = 0; i < RroisParameters.length-1; i++) out.print(RroisParameters[i]+",");
			out.println(RroisParameters[RroisParameters.length-1]);
		}

		if (RBitplanesScaling != null) {
			out.print("RBitplanesScaling: ");
			for (int i = 0; i < RBitplanesScaling.length-1; i++) out.print(RBitplanesScaling[i]+",");
			out.println(RBitplanesScaling[RBitplanesScaling.length-1]);
		}

		
		// Parameters for 3D (spectrally transformed images)
		out.println("WT3D: "+WT3D);
		out.println("WSL: "+WSL);
		out.println("WST: "+WST);
		out.println("DPCMRestartIndex: "+DPCMRestartIndex);
		
		out.flush();
	}
	
}