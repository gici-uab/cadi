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
package CADI.Client.ClientLogicalTarget.JPEG2000;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;

import CADI.Common.LogicalTarget.JPEG2000.CalculateRequiredComponentsDPCM;
import CADI.Common.LogicalTarget.JPEG2000.CalculateRequiredComponentsDWT;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.CBDParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.MCCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.MCOParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.MCTParameters;
import CADI.Common.Util.ArraysUtil;
import GiciException.ErrorException;

/**
 * This class calculates which are the relevant components necessaries to
 * invert a multiple component transformation.
 * <p>
 * Usage example:<br>
 * &nbsp; constructor<br>
 * &nbsp; run<br>
 * &nbsp; [getMethods]<br>
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2008/12/10
 */
public class CalculateRelevantComponents {

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#multiComponentTransform}.
	 */
	private int multiComponentTransformType = -1;
	
	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CBDParameters}.
	 */
	private CBDParameters cbdParameters = null;
	
	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.MCTParameters}.
	 */
	private HashMap<Integer, MCTParameters> mctParametersList = null;
	
	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.MCCParameters}.
	 */
	private HashMap<Integer, MCCParameters> mccParametersList = null;
	
	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.MCOParameters}.
	 */
	private MCOParameters mcoParameters = null;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters}.
	 */
	private JPKParameters jpkParameters = null;
	
	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#zSize}
	 */
	private int zSize = 0;
	
	/**
	 * Is the range of components which are needed after the multiple component
	 * transformation has been applied.
	 * <p>
	 * The first index is an array index, from 0 to the maximum number of
	 * ranges. And the second index indicates: 0 stands for the first component
	 * of the range, and 1 stands for the last component of the range (both are
	 * included). 
	 * <p>
	 * This attribute is the same as {@link CADI.Common.Network.JPIP.ViewWindowField#comps}.
	 */
	private int[][] comps = null;
	
	/**
	 * Is a bi-dimensional array with then required range of components.
	 * Indexes mean the same as the {@link #comps} attribute.
	 */
	private int[][] relevantComps = null;
	              
	
	// INTERNAL ATTRIBUTES

	/**
	 * Indicates whether the multiple component transformation is signalled
	 * by means of either CBD, MCT, MCC, and MCO parameters or the JPK
	 * parameters. 
	 */
	private boolean isNoCompliantMCT = false;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param multiComponentTransformType definition in {@link #multiComponentTransformType}.
	 * @param cbdParameters definition in {@link #cbdParameters}.
	 * @param mctParametersList definition in {@link #mctParametersList}.
	 * @param mccParametersList definition in {@link #mccParametersList}.
	 * @param mcoParameters definition in {@link #mcoParameters}.
	 * @param comps definition in {@link #comps}.
	 */
	public CalculateRelevantComponents(
			int multiComponentTransformType,
			int zSize,
			CBDParameters cbdParameters,
			HashMap<Integer, MCTParameters> mctParametersList,
			HashMap<Integer, MCCParameters> mccParametersList,
			MCOParameters mcoParameters,
			int[][] comps) {
		
		// Check input parameters
		if (multiComponentTransformType < 0) throw new IllegalArgumentException();
		if (zSize <= 0) throw new IllegalArgumentException();
		if (cbdParameters == null) throw new NullPointerException();
		if (mctParametersList == null) throw new NullPointerException();
		if (mccParametersList == null) throw new NullPointerException();
		if (mcoParameters == null) throw new NullPointerException();
		
		// Copy input parameters
		this.multiComponentTransformType = multiComponentTransformType;
		this.zSize = zSize;
		this.cbdParameters = cbdParameters;
		this.mctParametersList =  mctParametersList;
		this.mccParametersList = mccParametersList;
		this.mcoParameters = mcoParameters;
		this.comps = comps;
		
		isNoCompliantMCT = false;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param jpkParameters definition in {@link #jpkParameters}.
	 * @param comps definition in {@link #comps}.
	 */
	public CalculateRelevantComponents(JPKParameters jpkParameters, int zSize, int[][] comps) {
		// Check input parameters
		if (jpkParameters == null) throw new NullPointerException();
		if (zSize <= 0) throw new IllegalArgumentException();
		
		// Copy input parameters
		this.jpkParameters = jpkParameters;
		this.zSize = zSize;
		this.comps = comps;
		
		isNoCompliantMCT = true;
	}
	
	/**
	 * 
	 * @return the {@link #relevantComps} attribute.
	 * @throws ErrorException 
	 */
	public int[][] run() throws ErrorException {
		
		// If comps is null, all components are relevants.
		if (comps == null) return null;
		
		
		if (isNoCompliantMCT) { // Multi-component indicated through JPKParameters

			switch (jpkParameters.WT3D) {
				case 1: // 1D + 2D
					relevantComps = getRelevantComponentsDWT(comps, jpkParameters.WST, jpkParameters.WSL);
					break;
					
				case 4: // DPCM + 1D + 2D
					relevantComps = getRelevantComponentsDWT(comps, jpkParameters.WST, jpkParameters.WSL);
					
					// <<<<<< FALTA LA PARTE DE DPCM !!!! >>>>>>>>>>>
					assert(true);
					
				case 3: // DPCM
					CalculateRequiredComponentsDPCM crcDPCM = new CalculateRequiredComponentsDPCM(comps, zSize);
					if (jpkParameters.DPCMRestartIndex > 0) {
						crcDPCM.setRestartPeriodic(jpkParameters.DPCMRestartIndex);
					} else {
						crcDPCM.setClassical();
					}
					try {
						crcDPCM.run();
					} catch (IllegalAccessException e) { e.printStackTrace(); }
					relevantComps = crcDPCM.getRequiredComps();
					break;
					
				default:
					throw new ErrorException("Unsupported DWT transform");
			}
			
		} else {
			
			int WST = -1;
			int WSL = -1;
			
			if (multiComponentTransformType == 1) { // Color transform
				relevantComps = getRelevantComponentsColorTransform(comps);
				
			} else if (multiComponentTransformType == 2) { // Multi-component transform

				// Check for unsupported options
				if (mccParametersList.size() > 1) throw new ErrorException("Only one multiple-component transformation is allowed");
				if (mccParametersList.size() == 0) throw new ErrorException("");

				for (int key : mccParametersList.keySet()) {
					switch (mccParametersList.get(key).DWTType[0]) {
						case 0: WST = 2; break;
						case 1: WST = 1; break;
						default: throw new ErrorException("Unknown multiple spectral DWT transform");
					}
					WSL= mccParametersList.get(key).DWTLevels[0];
				}

				assert(WST >= 0); assert(WSL >= 0);
				relevantComps = getRelevantComponentsDWT(comps, WST, WSL);
			}
		}
		
		
		return relevantComps;
	}
	
	/**
	 * Returns the {@link #relevantComps} attribute.
	 * 
	 * @return the {@link #relevantComps} attribute.
	 */
	public int[][] getRelevantComponents() {
		return relevantComps;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";
		
		str = getClass().getName() + " [";
		
		if (isNoCompliantMCT) { 
			str += "JPEG2000 compliant=false";
			str += " WT3D=";
			switch (jpkParameters.WT3D) {
				case 1: // 1D + 2D
					str += "1D+2D";
					break;
					
				case 4: // DPCM + 1D + 2D
					str += "DPCM+1D+2D";
					break;
										
				case 3: // DPCM
					str += "DPCM";
					break;
					
				default:
					str += "Unknown";
			}
			str += " WST="+jpkParameters.WST;
			str += " WSL="+jpkParameters.WSL;
		} else {
			str += "JPEG2000 compliant=true";
		}
		
		str += " comps={";
		for (int i = 0; i < comps.length; i++) {
			str += comps[i][0]+"-"+comps[i][1];
			str += (i < comps.length-1) ? "," : "}"; 
		}
		
		str += " relevant comps={";
		for (int i = 0; i < relevantComps.length; i++) {
			str += relevantComps[i][0]+"-"+relevantComps[i][1];
			str += (i < relevantComps.length-1) ? "," : "}"; 
		}
		
		str += "]";
		
		return str;
	}
		
	/**
	 * Prints this CalculateRelevantComponents out to the specified output
	 * stream. This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Calculate relevant components --");
		
		if (isNoCompliantMCT) {	
			out.println("JPEG2000 compliant: false");
			out.print("WT3D: ");
			switch (jpkParameters.WT3D) {
				case 1: // 1D + 2D
					out.println("1D+2D");
					break;
					
				case 4: // DPCM + 1D + 2D
					out.println("DPCM+1D+2D");
					break;
										
				case 3: // DPCM
					out.println("DPCM");
					break;
					
				default:
					out.println("Unknown");
			}
			out.println("WST: "+jpkParameters.WST);
			out.println("WSL: "+jpkParameters.WSL);
		} else {
			out.println("JPEG2000 compliant: true");
		}
		
		out.print(" comps={");
		for (int i = 0; i < comps.length; i++) {
			out.print(comps[i][0]+"-"+comps[i][1]);
			out.print((i < comps.length-1) ? "," : "}\n"); 
		}
		
		out.print("relevant comps={");
		for (int i = 0; i < relevantComps.length; i++) {
			out.print(relevantComps[i][0]+"-"+relevantComps[i][1]);
			out.print((i < relevantComps.length-1) ? "," : "}\n"); 
		}
		
		out.flush();
	}
	
	// ============================ private methods ==============================
	/**
	 * This method computes which components are needed to decompress a range
	 * image components which have been DWT spectrally transformed.
	 * 
	 * @param compsRanges the range of components to be decompressed. The first
	 * 			index is an array index of the ranges. An the second index
	 * 			indicates: 0 is the first component of the range, and 1 is the
	 * 			last component of the range.
	 * 
	 * @return a bi-dimensional array with then required range of components.
	 * 			Indexes mean the same as the <code>compsRanges</code> input
	 * 			parameter. 
	 */
	private int[][] getRelevantComponentsDWT(int[][] compsRanges, int DWTType, int DWTLevels) {

		// Changes the identification components from range of components to a one-dimension array
		boolean[] maskComponents = new boolean[zSize];
		Arrays.fill(maskComponents, false);
		for (int i = 0; i < compsRanges.length; i++) {
			for (int c = compsRanges[i][0]; c <= compsRanges[i][1]; c++) {
				maskComponents[c] = true;
			}
		}
		
		// Get required components
		CalculateRequiredComponentsDWT mwd = new CalculateRequiredComponentsDWT(maskComponents, DWTLevels, DWTType);
		mwd.run();
		boolean[] comps = mwd.getRequiredComponents();
		
		
		int[][] necCompsRanges = ArraysUtil.expandedIndexesToRanges(comps);

		return necCompsRanges;
	}
	
	/**
	 * This method calculates which components are needed to invert a multiple
	 * component transformation when a color transform has been applied.
	 * 
	 * @param compsRanges definition in {@link #comps}.
	 * 
	 * @return a bi-dimensional array with then required range of components.
	 * 			Indexes mean the same as the <code>compsRanges</code> input
	 * 			parameter. 
	 */
	private int[][] getRelevantComponentsColorTransform(int[][] compsRanges) {
	
		// Convert range of components to array of components
		boolean[] compsExpanded = ArraysUtil.rangesToExpandedIndexes(compsRanges, zSize);
		
		// Set components 0, 1, and 2
		for (int i = 0; i < 3; i++) compsExpanded[i] = true;
		
		// Convert array of components to range of components
		return ArraysUtil.expandedIndexesToRanges(compsExpanded);
	}
}