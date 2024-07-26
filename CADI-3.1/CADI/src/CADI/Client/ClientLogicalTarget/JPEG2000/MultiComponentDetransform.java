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
import java.util.HashMap;

import CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream;
import CADI.Common.LogicalTarget.JPEG2000.Detransform.ColourDetransform;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.CBDParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.MCCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.MCOParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.MCTParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters;
import GiciException.ErrorException;
import GiciTransform.DPCMSpectralDecorrelator;
import GiciTransform.InverseDWTCore;

/**
 * This class implements several multiple component detransformations.
 * <p>
 * Two constructors are implemented depending on multiple component
 * transformation is specified either by means of the JPEG2000-compliant headers
 * or by means of the uncompliant JPK headers.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1 2008/12/13
 */
public class MultiComponentDetransform {

	/**
	 * Multi-component image which will be detransformed along the component
	 * dimension (spectral dimension).
	 */
	private float[][][] imageSamples = null;
	
	private JPEG2KCodestream codestream = null;
	
	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters}.
	 */
	//private SIZParameters sizParameters = null;
	
	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters}.
	 */
	//private CODParameters codParameters = null;
	
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
	 * Is an one-dimensional array with the component indexes which are
	 * necessaries to invert the multi-component transform. 
	 */
	private int[] relevantComponentIndexes = null;
	
	/**
	 * Is an one-dimensional array with the component indexes which will be
	 * returned after the multi-component transform has been applied.
	 */
	private int[] componentIndexes = null;
	
	
	// INTERNAL ATTRIBUTES

	/**
	 * Indicates whether the multiple component transformation is signalled
	 * by means of either CBD, MCT, MCC, and MCO parameters or the JPK
	 * parameters. 
	 */
	private boolean isNoCompliantMCT = false;
	
	/**
	 * Multi-component image which will be used as a temporary image in order
	 * to apply the inverse multi-component transform. It is used due to the
	 * inverse module has not a canvas coordinate system.
	 */
	private float[][][] tmpImageSamples = null;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param imageSamples definition in {@link #imageSamples}.
	 * @param mctParametersList definition in {@link #mctParametersList}.
	 * @param mccParametersList definition in {@link #mccParametersList}.
	 * @param mcoParameters definition in {@link #mcoParameters}.
	 * @param relevantComponentIndexes definition in {@link #relevantComponentIndexes}.
	 */
	public MultiComponentDetransform(JPEG2KCodestream codestream,
			float[][][] imageSamples,
			HashMap<Integer, MCTParameters> mctParametersList,
			HashMap<Integer, MCCParameters> mccParametersList,
			MCOParameters mcoParameters,
			int[] relevantComponentIndexes,
			int[] componentIndexes) {
		
		// Check input parameters
		if (codestream == null) throw new NullPointerException();
		if (imageSamples == null) throw new NullPointerException();
		if (mctParametersList == null) throw new NullPointerException();
		if (mccParametersList == null) throw new NullPointerException();
		if (mcoParameters == null) throw new NullPointerException();
		
		// Copy input parameters
		this.codestream = codestream;
		this.imageSamples = imageSamples;
		this.mctParametersList =  mctParametersList;
		this.mccParametersList = mccParametersList;
		this.mcoParameters = mcoParameters;
		this.relevantComponentIndexes = relevantComponentIndexes;
		this.componentIndexes = componentIndexes;
		
		multiComponentTransformType = codestream.getMultiComponentTransform();
		isNoCompliantMCT = false;
	}
	
	/**
	 * Constructor.
	 * <p>
	 * It must be used when the multiple component transformation is performed
	 * through the JPK main headers in the codestream. This is a non JEPG2000
	 * compliant headers.
	 * 
	 * @param imageSamples definition in {@link #imageSamples}.
	 * @param jpkParameters definition in {@link #jpkParameters}.
	 * @param relevantComponentIndexes definition in {@link #relevantComponentIndexes}.
	 * @param componentIndexes definition in {@link #componentIndexes}.
	 */
	public MultiComponentDetransform(JPEG2KCodestream codestream,
	                                 float[][][] imageSamples,
	                                 JPKParameters jpkParameters,
	                                 int[] relevantComponentIndexes,
	                                 int[] componentIndexes) {
		// Check input parameters
		if (codestream == null) throw new NullPointerException();
		if (imageSamples == null) throw new NullPointerException();
		if (jpkParameters == null) throw new NullPointerException();
		
		// Copy input parameters
		this.codestream = codestream;
		this.imageSamples = imageSamples;
		this.jpkParameters = jpkParameters;
		this.relevantComponentIndexes = relevantComponentIndexes;
		this.componentIndexes = componentIndexes;
		
		isNoCompliantMCT = true;
	}
	
	/**
	 * Performs the multiple component transformation.
	 * 
	 * @return the {@link #imageSamples} attribute.
	 * 
	 * @throws ErrorException 
	 */
  @SuppressWarnings("fallthrough")
	public float[][][] run() throws ErrorException {
		
		// Original image is mapped into a temporary image which has as
		// components as zSize. This mapping is used because of the multiple
		// component detransformation is not implemented with a canvas
		// coordinate system.
		tmpImageSamples = new float[codestream.getZSize()][codestream.getYSize()][codestream.getXSize()];
		for (int zIndex = 0; zIndex < relevantComponentIndexes.length; zIndex++) {
			tmpImageSamples[relevantComponentIndexes[zIndex]] = imageSamples[zIndex];
		}
		
		
		if (isNoCompliantMCT) { // Multi-component indicated through JPKParameters
			switch (jpkParameters.WT3D) {
				case 1: // 1D + 2D
					discreteWaveletTransform(jpkParameters.WST, jpkParameters.WSL);
					break;
					
				case 4: // DPCM + 1D + 2D
					discreteWaveletTransform(jpkParameters.WST, jpkParameters.WSL);
					
				case 3: // DPCM
					if (jpkParameters.DPCMRestartIndex >= 0) {
						differentialCodingWithRestartIndex(jpkParameters.DPCMRestartIndex);
					} else { // NOTE: This option is not implemented yet !!!
						assert(true);
						int[] restartIndexes = null; 
						differentialCodingWithRestartIndex(restartIndexes);
					}
					break;
				default:
					throw new ErrorException("Unsupported DWT transform");
			}
			
		} else {
			int WST = -1;
			int WSL = -1;
			
			if (multiComponentTransformType == 1) { // Color transform
				ColourDetransform cd = new ColourDetransform(tmpImageSamples, ((codestream.getWTType()==1) ? true : false));
				tmpImageSamples = cd.run();
				//Free unused memory
				cd = null;

			} else if (multiComponentTransformType == 2) { // Multi-component transform
				// Check for unsupported options
				if (mccParametersList.size() > 1) throw new ErrorException("Only one multiple-component transformation is allowed");
				if (mccParametersList.isEmpty()) throw new ErrorException("");

				for (int key : mccParametersList.keySet()) {
					switch (mccParametersList.get(key).DWTType[0]) {
						case 0: WST = 2; break;
						case 1: WST = 1; break;
						default: throw new ErrorException("Unknown multiple spectral DWT transform");
					}
					WSL= mccParametersList.get(key).DWTLevels[0];
				}

				// Applies spectral detransform
				assert(WST >= 0); assert(WSL >= 0);
				discreteWaveletTransform(WST, WSL);
				
			} else {
				assert(true);
			}
		}
		
		imageSamples = new float[componentIndexes.length][][];
		for (int zIndex = 0; zIndex < componentIndexes.length; zIndex++) {
			imageSamples[zIndex] = tmpImageSamples[componentIndexes[zIndex]];
		}
		
		return imageSamples;
	}
	
	/**
	 * Returns the {@link #imageSamples} attribute.
	 * 
	 * @return the {@link #imageSamples} attribute.
	 */
	public float[][][] getImage() {			
		return imageSamples;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";
		
		str = getClass().getName() + " [";
		str += "Not implemented yet";		
		str += "]";
		
		return str;
	}
		
	/**
	 * Prints this Multi-component detransform out to the specified output
	 * stream. This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Multi-Component Detransform --");
		out.println("Not implemented yet");		
		
		out.flush();
	}
	
	// ============================ private methods ==============================
	/**
	 * 
	 * @param type
	 * @param levels
	 * 
	 * @throws ErrorException 
	 */
	private void discreteWaveletTransform(int type, int levels) throws ErrorException {
		
		int numComps = codestream.getZSize();
		int height = codestream.getYSize();
		int width = codestream.getXSize();

		InverseDWTCore hwd = new InverseDWTCore(tmpImageSamples);
		int[] zSubBandSizes = computeSubBandSizes(numComps, levels);
		for(int rLevel = 0; rLevel < levels; rLevel++){
			int zSubBandSize = zSubBandSizes[rLevel];
			hwd.setParameters(type, 2, 0, zSubBandSize-1, 0, height-1, 0, width-1);
			hwd.run();
		}
		tmpImageSamples = hwd.getImageSamples();
		hwd = null;
	}
		
	/**
	 * Performs the differential coding along the components with a periodical
	 * restart.
	 * 
	 * @param restartIndex
	 * 
	 * @throws ErrorException 
	 */
	private void differentialCodingWithRestartIndex(int restartIndex) throws ErrorException {
		DPCMSpectralDecorrelator dpcm = new DPCMSpectralDecorrelator(tmpImageSamples);
		tmpImageSamples = dpcm.inverseDPCM(restartIndex);
		dpcm = null;
	}
	
	/**
	 * Performs the differential coding along the components with a non
	 * periodical restart.
	 * 
	 * @param restartIndexes
	 * 
	 * @throws ErrorException 
	 */
	private void differentialCodingWithRestartIndex(int[] restartIndexes) throws ErrorException {
		DPCMSpectralDecorrelator dpcm = new DPCMSpectralDecorrelator(tmpImageSamples);
		//imageSamples = dpcm.inverseDPCM(restartIndexes);
		dpcm = null;
	}
	
	/**
	 * This function compute the subband sizes for every dimension.
	 *
	 */
	private int[] computeSubBandSizes(int zSize, int WTLevels){
	
		int[] zSubBandSizes = new int[ WTLevels > 0  ? WTLevels : 1];
		
		if (WTLevels > 0 ){
			zSubBandSizes[WTLevels-1] = zSize;
			for(int rLevel = WTLevels-2; rLevel >= 0; rLevel--){
				zSubBandSizes[rLevel] = zSubBandSizes[rLevel+1] / 2 + zSubBandSizes[rLevel+1] % 2;
			}
		}else{
			zSubBandSizes[1] = zSize;
		}
		
		return zSubBandSizes;
	}
	
}