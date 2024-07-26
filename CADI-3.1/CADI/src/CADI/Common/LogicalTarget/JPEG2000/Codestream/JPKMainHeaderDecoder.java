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
package CADI.Common.LogicalTarget.JPEG2000.Codestream;

import CADI.Common.LogicalTarget.JPEG2000.JPCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters;
import GiciException.*;
import GiciStream.*;

import java.io.*;
import java.nio.*;
import java.util.Arrays;

/**
 * This class reads a JPK heading from a file. Then, get functions can be used
 * to retrieve read values. This class receives an input stream and advance
 * read up to heading end. Usage example:<br>
 * 
 * &nbsp; construct<br>
 * &nbsp; run<br>
 * &nbsp; get functions<br>
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.2 2008/11/08
 */
public class JPKMainHeaderDecoder {

	/**
	 * Is an object where the main header are saved.
	 */
	private JPCParameters jpcParameters = null;
	
	/**
	 * Is the file pointer to the first byte of the main header.
	 */
	private long mainHeaderInitialPos = 0;
	
	/**
	 * Is the length of the main header.
	 */
	private int mainHeaderLength = 0;
		
	/**
	 * All following variables have the following structure:
	 *
	 * type nameVar; //variable saved at the heading
	 * final type nameVar_BITS; //number of bits allowed for this variable in the heading - its range will be from 0 to 2^nameVar_BITS
	 */

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#zSize}
	 */
	private int zSize;
	private final int zSize_BITS = 14;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#ySize}
	 */
	private int ySize;
	private final int ySize_BITS = 20;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#xSize}
	 */
	private int xSize;
	private final int xSize_BITS = 20;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#LSType}
	 */
	private int LSType;
	private final int LSType_BITS = 4;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#LSComponents}
	 */
	private boolean[] LSComponents = null;
	//final int LSComponents_BITS = 1;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#LSSubsValues}
	 */
	private int[] LSSubsValues = null;
	private final int LSSubsValues_BITS = 16;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#signed}
	 */
	private boolean[] LSSignedComponents = null;
	//final int LSSignedComponents_BITS = 1;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#RMMultValues}
	 */
	private float[] RMMultValues = null;
	//final int RMMultValues_BITS = 32;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#CTType}
	 */
	private int CTType;
	private final int CTType_BITS = 4;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#CTComponents}
	 */
	private int[] CTComponents = null;
	private final int CTComponents_BITS = zSize_BITS;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#WTTypes}
	 */
	private int[] WTTypes = null;
	private final int WTTypes_BITS = 4;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#WTLevels}
	 */
	private int[] WTLevels = null;
	private final int WTLevels_BITS = 5; //log_2(max(ySize_BITS, xSize_BITS))

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#WT3D}
	 */
	private int WT3D = 0;
	private final int WT3D_BITS = 3;
	
	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#WSL}
	 */
	private int WSL = 0;
	private final int WSL_BITS = 5;
	
	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#WST}
	 */
	private int WST = 0;
	private final int WST_BITS = 4;
		
	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters#QTypes}
	 */
	private int[] QTypes = null;
	private final int QTypes_BITS = 4;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#QDynamicRange}
	 */
	private int QDynamicRange;
	private final int QDynamicRange_BITS = 4;
	
	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#precision}
	 */
	private int[] QComponentsBits = null;
	private final int QComponentsBits_BITS = 10;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters#QExponents}
	 */
	private int QExponents[][][] = null;
	private final int QExponents_BITS = 8;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters#QMantisas}
	 */
	private int QMantisas[][][] = null;
	private final int QMantisas_BITS = 12;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters#QGuardBits}
	 */
	private int QGuardBits;
	private final int QGuardBits_BITS = 4;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#blockWidths}
	 */
	private int[] BDBlockWidths = null;
	private final int BDBlockWidths_BITS = 5; //log_2(max(ySize_BITS, xSize_BITS))

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#blockHeights}
	 */
	private int[] BDBlockHeights = null;
	private final int BDBlockHeights_BITS = 5; //log_2(max(ySize_BITS, xSize_BITS))

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#precinctWidths}
	 */
	private int[][] BDResolutionPrecinctWidths = null;
	private final int BDResolutionPrecinctWidths_BITS = 5; //log_2(max(ySize_BITS, xSize_BITS))

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#precinctHeights}
	 */
	private int[][] BDResolutionPrecinctHeights = null;
	private final int BDResolutionPrecinctHeights_BITS = 5; //log_2(max(ySize_BITS, xSize_BITS))

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#numLayers}
	 */
	private int LCAchievedNumLayers;
	private final int LCAchievedNumLayers_BITS = 12;

	/**
	 * Bitstream type.
	 * <p>
	 * Bitstreams can be:<br>
	 *  <ul>
	 *    <li> 0- J2B JPEG2000 bitstream (without MQ coding)
	 *    <li> 1- J2C JPEG2000 codestream (with MQ coding)
	 *  </ul>
	 */
	private int BitStreamType;
	private final int BitStreamType_BITS = 2;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#progressionOrder}
	 */
	private int FWProgressionOrder;
	private final int FWProgressionOrder_BITS = 4;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#useSOP} and
	 * {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#useEPH}.
	 */
	private boolean[] FWPacketHeaders = null;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#waveletSA}
	 */
	private int[] DWaveletSA = null;
	private final int DWaveletSA_BITS = 1;
	
	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#bitPlaneEncodingSA}
	 */
	private int[] DBitPlaneEncodingSA = null;
	private final int DBitPlaneEncodingSA_BITS = 1;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#RroiType}
	 */
	private int RroiType;
	private final int RroiType_BITS = 8;
	
	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#RroisParameters}
	 */
	private int[] RroisParameters = null;
	private final int RroisParameters_BITS = 20;
	
	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#RBitplanesScaling}
	 */
	private int[] RBitplanesScaling = null;
	private final int RBitplanesScaling_BITS = 5;//19 + 19 <= 38 log2(19) <= 5
	
	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#DPCMRestartIndex}
	 */
	private int DPCMRestartIndex = 0;
	private final int DPCMRestartIndex_BITS = 16;
	
	/**
	 * Definition in {@link CADI.Client.ClientLogicalTarget.JPEG2000.JPEG2KLogicalTarget#in}
	 */
	private BufferedDataInputStream in = null;
	
	
	//INTERNAL VARIABLES

	/**
	 * Temporal bitstream where the values are saved from the file.
	 * <p>
	 * Content is a buffer of bits.
	 */
	private BitStream JPKHeading;

	// ============================= public methods ==============================
	/**
	 * Constructor of JPKDeheading. It receives the information about the compressed image needed to be put in JPKHeading.
	 *
	 * @param in definition in {@link #in}
	 */
	public JPKMainHeaderDecoder(BufferedDataInputStream in){
		if (in == null) throw new NullPointerException();
		
		//Parameters copy		
		this.in = in;
		
		// Initializations
		jpcParameters = new JPCParameters();
    jpcParameters.jpkParameters = new JPKParameters();
		jpcParameters.jpkParameters.LSType = 1;
	}
	
	/**
	 * Reads the JPK heading.
	 *
	 * @throws ErrorException when some I/O error occurs
	 */
	public void run() throws ErrorException{
		
		// Acquires the resource
		in.lock();

		try {

			JPKHeading = new BitStream();
			//zSize
			zSize = readInteger(zSize_BITS);

			//ySize
			ySize = readInteger(ySize_BITS);

			//xSize
			xSize = readInteger(xSize_BITS);

			//LSType
			LSType = readInteger(LSType_BITS);

			//LSComponents
			LSComponents = new boolean[zSize];
			boolean LSComponents_ALL = readBoolean();
			if(LSComponents_ALL){
				for(int z = 0; z < zSize; z++){
					LSComponents[z] = true;
				}
			}else{
				for(int z = 0; z < zSize; z++){
					LSComponents[z] = readBoolean();
				}
			}

			//LSSubsValues
			LSSubsValues = new int[zSize];
			if(LSType > 1){
				for(int z = 0; z < zSize; z++){
					LSSubsValues[z] = readInteger(LSSubsValues_BITS);
				}
			}

			//LSSignedComponents
			LSSignedComponents = new boolean[zSize];
			boolean LSSignedComponents_ALL = readBoolean();
			if(LSSignedComponents_ALL){
				for(int z = 0; z < zSize; z++){
					LSSignedComponents[z] = true;
				}
			}else{
				for(int z = 0; z < zSize; z++){
					LSSignedComponents[z] = readBoolean();
				}
			}

			//RMMultValues
			RMMultValues = new float[zSize];
			boolean RMMultValues_ALL = readBoolean();
			if(RMMultValues_ALL){
				for(int z = 0; z < zSize; z++){
					RMMultValues[z] = 1F;
				}
			}else{
				for(int z = 0; z < zSize; z++){
					RMMultValues[z] = readFloat();
				}
			}

			//CTType
			CTType = readInteger(CTType_BITS);

			//CTComponents
			CTComponents = new int[3];
			for(int i = 0; i < CTComponents.length; i++){
				CTComponents[i] = readInteger(CTComponents_BITS);
			}

			//WTTypes
			WTTypes = new int[zSize];
			boolean WTypes_ALL = readBoolean();
			if(WTypes_ALL){
				WTTypes[0] = readInteger(WTTypes_BITS);
				for(int z = 1; z < zSize; z++){
					WTTypes[z] = WTTypes[0];
				}
			}else{
				for(int z = 0; z < zSize; z++){
					WTTypes[z] = readInteger(WTTypes_BITS);
				}
			}

			//WTLevels
			WTLevels = new int[zSize];
			boolean WTLevels_ALL = readBoolean();
			if(WTLevels_ALL){
				WTLevels[0] = readInteger(WTLevels_BITS);
				for(int z = 1; z < zSize; z++){
					WTLevels[z] = WTLevels[0];
				}
			}else{
				for(int z = 0; z < zSize; z++){
					WTLevels[z] = readInteger(WTLevels_BITS);
				}
			}

			//QTypes
			QTypes = new int[zSize];
			boolean QTypes_ALL = readBoolean();
			if(QTypes_ALL){
				QTypes[0] = readInteger(QTypes_BITS);
				for(int z = 1; z < zSize; z++){
					QTypes[z] = QTypes[0];
				}
			}else{
				for(int z = 0; z < zSize; z++){
					QTypes[z] = readInteger(QTypes_BITS);
				}
			}

			//QDynamicRange
			QDynamicRange = readInteger(QDynamicRange_BITS);

			//QComponentsBits
			QComponentsBits = new int[zSize];
			boolean QComponentsBits_ALL = readBoolean();
			if(QComponentsBits_ALL){
				QComponentsBits[0] = readInteger(QComponentsBits_BITS);
				for(int z = 1; z < zSize; z++){
					QComponentsBits[z] = QComponentsBits[0];
				}
			}else{
				for(int z = 0; z < zSize; z++){
					QComponentsBits[z] = readInteger(QComponentsBits_BITS);
				}
			}

			//QExponents
			QExponents = new int[zSize][][];
			for(int z = 0; z < zSize; z++){
				QExponents[z] = new int[WTLevels[z]+1][];
				for(int rLevel = 0; rLevel <= WTLevels[z]; rLevel++){
					if(rLevel == 0){
						QExponents[z][rLevel] = new int[1];
					}else{
						QExponents[z][rLevel] = new int[3];
					}
				}
			}
			boolean QExponents_ALL = readBoolean();
			if(QExponents_ALL){
				for(int rLevel = 0; rLevel <= WTLevels[0]; rLevel++){
					for(int subband = 0; subband < QExponents[0][rLevel].length; subband++){
						QExponents[0][rLevel][subband] = readInteger(QExponents_BITS);
						for(int z = 1; z < zSize; z++){
							QExponents[z][rLevel][subband] = QExponents[0][rLevel][subband];
						}
					}}
			}else{
				for(int z = 0; z < zSize; z++){
					for(int rLevel = 0; rLevel <= WTLevels[z]; rLevel++){
						for(int subband = 0; subband < QExponents[z][rLevel].length; subband++){
							QExponents[z][rLevel][subband] = readInteger(QExponents_BITS);
						}}}
			}

			//QMantisas
			QMantisas = new int[zSize][][];
			for(int z = 0; z < zSize; z++){
				QMantisas[z] = new int[WTLevels[z]+1][];
				for(int rLevel = 0; rLevel <= WTLevels[z]; rLevel++){
					if(rLevel == 0){
						QMantisas[z][rLevel] = new int[1];
					}else{
						QMantisas[z][rLevel] = new int[3];
					}
				}
			}
			boolean QMantisas_ALL = readBoolean();
			if(QMantisas_ALL){
				for(int rLevel = 0; rLevel <= WTLevels[0]; rLevel++){
					for(int subband = 0; subband < QMantisas[0][rLevel].length; subband++){
						QMantisas[0][rLevel][subband] = readInteger(QMantisas_BITS);
						for(int z = 1; z < zSize; z++){
							QMantisas[z][rLevel][subband] = QMantisas[0][rLevel][subband];
						}
					}}
			}else{
				for(int z = 0; z < zSize; z++){
					for(int rLevel = 0; rLevel <= WTLevels[z]; rLevel++){
						for(int subband = 0; subband < QMantisas[z][rLevel].length; subband++){
							QMantisas[z][rLevel][subband] = readInteger(QMantisas_BITS);
						}}}
			}

			//QGuardBits
			QGuardBits = readInteger(QGuardBits_BITS);

			//BDBlockWidths
			BDBlockWidths = new int[zSize];
			boolean BDBlockWidths_ALL = readBoolean();
			if(BDBlockWidths_ALL){
				BDBlockWidths[0] = readInteger(BDBlockWidths_BITS);
				for(int z = 1; z < zSize; z++){
					BDBlockWidths[z] = BDBlockWidths[0];
				}
			}else{
				for(int z = 0; z < zSize; z++){
					BDBlockWidths[z] = readInteger(BDBlockWidths_BITS);
				}
			}

			//BDBlockHeights
			BDBlockHeights = new int[zSize];
			boolean BDBlockHeights_ALL = readBoolean();
			if(BDBlockHeights_ALL){
				BDBlockHeights[0] = readInteger(BDBlockHeights_BITS);
				for(int z = 1; z < zSize; z++){
					BDBlockHeights[z] = BDBlockHeights[0];
				}
			}else{
				for(int z = 0; z < zSize; z++){
					BDBlockHeights[z] = readInteger(BDBlockHeights_BITS);
				}
			}

			//BDResolutionPrecinctWidths
			BDResolutionPrecinctWidths = new int[zSize][];
			for(int z = 0; z < zSize; z++){
				BDResolutionPrecinctWidths[z] = new int[WTLevels[z]+1];
			}
			boolean BDResolutionPrecinctWidths_ALL = readBoolean();
			if(BDResolutionPrecinctWidths_ALL){
				for(int rLevel = 0; rLevel <= WTLevels[0]; rLevel++){
					BDResolutionPrecinctWidths[0][rLevel] = readInteger(BDResolutionPrecinctWidths_BITS);
					for(int z = 1; z < zSize; z++){
						BDResolutionPrecinctWidths[z][rLevel] = BDResolutionPrecinctWidths[0][rLevel];
					}
				}
			}else{
				for(int z = 0; z < zSize; z++){
					for(int rLevel = 0; rLevel <= WTLevels[z]; rLevel++){
						BDResolutionPrecinctWidths[z][rLevel] = readInteger(BDResolutionPrecinctWidths_BITS);
					}}
			}

			//BDResolutionPrecinctHeights
			BDResolutionPrecinctHeights = new int[zSize][];
			for(int z = 0; z < zSize; z++){
				BDResolutionPrecinctHeights[z] = new int[WTLevels[z]+1];
			}
			boolean BDResolutionPrecinctHeights_ALL = readBoolean();
			if(BDResolutionPrecinctHeights_ALL){
				for(int rLevel = 0; rLevel <= WTLevels[0]; rLevel++){
					BDResolutionPrecinctHeights[0][rLevel] = readInteger(BDResolutionPrecinctHeights_BITS);
					for(int z = 1; z < zSize; z++){
						BDResolutionPrecinctHeights[z][rLevel] = BDResolutionPrecinctHeights[0][rLevel];
					}
				}
			}else{
				for(int z = 0; z < zSize; z++){
					for(int rLevel = 0; rLevel <= WTLevels[z]; rLevel++){
						BDResolutionPrecinctHeights[z][rLevel] = readInteger(BDResolutionPrecinctHeights_BITS);
					}}
			}

			//LCAchievedNumLayers
			LCAchievedNumLayers = readInteger(LCAchievedNumLayers_BITS);

			//BitStreamType
			BitStreamType = readInteger(BitStreamType_BITS);

			//FWProgressionOrder
			FWProgressionOrder = readInteger(FWProgressionOrder_BITS);

			//FWPacketHeaders
			FWPacketHeaders = new boolean[2];
			FWPacketHeaders[0] = readBoolean();
			FWPacketHeaders[1] = readBoolean();

			try{
				int byte_0xFF = in.read();
				int byte_0x90 = in.read();
				if(byte_0xFF != 0xFF && byte_0x90 != 0x90){

					in.seek(in.getPos()-2);

					//DWaveletSA
					DWaveletSA = new int[zSize];
					boolean DWaveletSA_ALL = readBoolean();
					if(DWaveletSA_ALL){
						DWaveletSA[0] = readInteger(DWaveletSA_BITS);
						for(int z = 1; z < zSize; z++){
							DWaveletSA[z] = DWaveletSA[0];
						}
					}else{
						for(int z = 0; z < zSize; z++){
							DWaveletSA[z] = readInteger(DWaveletSA_BITS);
						}
					}

					//DBitPlaneEncodingSA
					DBitPlaneEncodingSA = new int[1];
					DBitPlaneEncodingSA[0] = readBoolean() ? 1 : 0;

					//RSroiType
					RroiType = readInteger(RroiType_BITS);
					switch(RroiType){
					case 1:
						RBitplanesScaling = new int[zSize];
						for(int z = 0; z < zSize; z++){
							RBitplanesScaling[z] = readInteger(RBitplanesScaling_BITS);
						}
						break;
					case 3:
						RroisParameters = new int[2];
						RroisParameters[0] = readInteger(RroisParameters_BITS);
						RroisParameters[1] = readInteger(RroisParameters_BITS);
						RBitplanesScaling = new int[zSize];
						for(int z = 0; z < zSize; z++){
							RBitplanesScaling[z] = RroisParameters[0] + RroisParameters[1];
						}
						break;
					case 4:
						RBitplanesScaling = new int[1];
						RBitplanesScaling[0] = readInteger(RBitplanesScaling_BITS);
						//JPKHeading.addBits(RBitplanesScaling[0], RBitplanesScaling_BITS);
						break;
					case 6:
						RroisParameters = new int[readInteger(RroisParameters_BITS)];
						RBitplanesScaling = new int[1];
						for(int i = 0; i < RroisParameters.length; i++){
							RroisParameters[i] = readInteger(RroisParameters_BITS);
						}
						RBitplanesScaling[0] = RroisParameters[1];
					}

					WT3D = readInteger(WT3D_BITS);

					switch (WT3D) {
					case 0: // 2D DWT in the spatial domain => no value is stored
						break;
					case 1: // 1D + 2D hybrid  DWT => only values for 1D are stored
						WSL = readInteger(WSL_BITS);
						WST = readInteger(WST_BITS);
						break;
					case 2: // 3D pyramidal DWT
						WSL = readInteger(WSL_BITS);
						WST = readInteger(WST_BITS);
						break;
					case 3: // Forward DPCM + Forward Wavelet Transform => only the restartIndex of DPCM is stored
						DPCMRestartIndex = readInteger(DPCMRestartIndex_BITS);
						break;
					case 4: // Forward DPCM + 1D+2D non pyramidal Wavelet Transform => only restartIndex of DPCM  and 1D values are stored
						DPCMRestartIndex = readInteger(DPCMRestartIndex_BITS);
						WSL = readInteger(WSL_BITS);
						WST = readInteger(WST_BITS);
						break;
					default:
						assert(true);
					}

					//Read SOT marker (0xFF 0x90)
					if (in.available() >= 2) {
						while(in.read() != 0xFF);
						while(in.read() != 0x90);
					}
				}

				mainHeaderLength = (int)in.getPos()-2;
			}catch(IOException e){
				throw new ErrorException("No SOT found.");
			}

		} finally {
			in.unlock();
		}
	}

	/**
	 * Returns the {@link #jpcParameters} attribute.
	 * 
	 * @return the {@link #jpcParameters} attribute
	 */
	public JPCParameters getJPCParameters() {
				
		jpcParameters.sizParameters.XOsize = 0;
		jpcParameters.sizParameters.YOsize = 0;
		jpcParameters.sizParameters.XTsize = xSize;
		jpcParameters.sizParameters.YTsize = ySize;
		jpcParameters.sizParameters.zSize = zSize;
		jpcParameters.sizParameters.ySize = ySize;
		jpcParameters.sizParameters.xSize = xSize;
		jpcParameters.jpkParameters.LSType = LSType;
		jpcParameters.jpkParameters.LSComponents = LSComponents;
		jpcParameters.jpkParameters.LSSubsValues = LSSubsValues;
		jpcParameters.sizParameters.signed = LSSignedComponents;
		jpcParameters.jpkParameters.RMMultValues = RMMultValues;
		if (CTType > 0) jpcParameters.codParameters.multiComponentTransform = 1;
		jpcParameters.codParameters.WTType = WTTypes[0];
		jpcParameters.codParameters.WTLevels = WTLevels[0];
		jpcParameters.qcdParameters.quantizationStyle = QTypes[0];
		jpcParameters.sizParameters.precision = QComponentsBits;
		jpcParameters.qcdParameters.exponents = QExponents[0];
		jpcParameters.qcdParameters.mantisas = QMantisas[0];
		jpcParameters.qcdParameters.guardBits = QGuardBits;
		jpcParameters.codParameters.blockWidth = 1 << BDBlockWidths[0];
		jpcParameters.codParameters.blockHeight = 1 << BDBlockHeights[0];
		jpcParameters.codParameters.precinctWidths =  BDResolutionPrecinctWidths[0];
		jpcParameters.codParameters.precinctHeights = BDResolutionPrecinctHeights[0];
		jpcParameters.codParameters.numLayers = LCAchievedNumLayers;
		jpcParameters.codParameters.progressionOrder = FWProgressionOrder;
		jpcParameters.codParameters.useSOP = FWPacketHeaders[0];
		jpcParameters.codParameters.useEPH = FWPacketHeaders[1];
		
		jpcParameters.codParameters.bypass = false;
		jpcParameters.codParameters.reset = false;
		jpcParameters.codParameters.restart = true;
		jpcParameters.codParameters.causal = false;
		jpcParameters.codParameters.erterm = false;
		jpcParameters.codParameters.segmark = false;				
		
		jpcParameters.jpkParameters.QDynamicRange = QDynamicRange;
		
		// Shape adaptive
		jpcParameters.jpkParameters.waveletSA = DWaveletSA;
		jpcParameters.jpkParameters.bitPlaneEncodingSA = DBitPlaneEncodingSA;
		
		// ROIs
		jpcParameters.jpkParameters.RroiType = RroiType;
		jpcParameters.jpkParameters.RroisParameters = RroisParameters;
		jpcParameters.jpkParameters.RBitplanesScaling = RBitplanesScaling;
		
		// 3D parameters
		jpcParameters.jpkParameters.WT3D = WT3D;
		jpcParameters.jpkParameters.WSL = WSL;
		jpcParameters.jpkParameters.WST = WST;
		jpcParameters.jpkParameters.DPCMRestartIndex = DPCMRestartIndex;
		
		return jpcParameters;
	}
	
	/**
	 * Returns the {@link #mainHeaderInitialPos} attribute.
	 * 
	 * @return the {@link #mainHeaderInitialPos} attribute.
	 */
	public long getMainHeaderInitialPos() {
		return mainHeaderInitialPos;
	}
	
	/**
	 * Returns the {@link #mainHeaderLength} attribute.
	 * 
	 * @return the {@link #mainHeaderLength} attribute.
	 */
	public int getMainHeaderLength() {
		return mainHeaderLength;
	}
	
	// ============================ private methods ==============================
	/**
	 * Reads a boolean from the file and returns its value.
	 *
	 * @return the boolean read
	 *
	 * @throws ErrorException when some wrong bitstream or I/O operation occurs
	 */
	private boolean readBoolean() throws ErrorException{
		boolean value;

		if(JPKHeading.getNumBits() < 1){
			JPKHeading.addByte((byte) readByte());
		}
		
		try{
			value = JPKHeading.getBit(0);
			JPKHeading.deleteBeginBit();
		}catch(WarningException e){
			throw new ErrorException("BitStream error.");
		}
		return(value);
	}

	/**
	 * Reads an integer from the file and returns its value.
	 *
	 * @param numBits the number of bits of the integer
	 * @return the integer read
	 *
	 * @throws ErrorException when some wrong bitstream or I/O operation occurs
	 */
	private int readInteger(int numBits) throws ErrorException{
		int value = 0;

		while(JPKHeading.getNumBits() < numBits){			
			JPKHeading.addByte((byte) readByte());			
		}
		
		int mask = (1 << (numBits-1));
		for(int bit = 0; bit < numBits; bit++){
			try{
				if(JPKHeading.getBit(bit)){
					value |= mask;
				}
			}catch(WarningException e){
				throw new ErrorException("BitStream reaches end of buffer.");
			}
			mask >>= 1;
		}
		try{
			JPKHeading.deleteBeginBits(numBits);
		}catch(WarningException e){
			throw new ErrorException("BitStream error.");
		}
		return(value);
	}

	/**
	 * Reads a float from the file and returns its value.
	 *
	 * @return the float read
	 *
	 * @throws ErrorException when some wrong bitstream or I/O operation occurs
	 */
	private float readFloat() throws ErrorException{
		float value = 0F;

		while(JPKHeading.getNumBits() < 32){
			JPKHeading.addByte((byte) readByte());
		}
		
		byte[] byteFloat = new byte[4];
		for(int byteFloatIndex = 3; byteFloatIndex >= 0; byteFloatIndex--){
			int mask = (1 << 7);
			for(int bit = 0; bit < 8; bit++){
				try{
					if(JPKHeading.getBit(bit)){
						byteFloat[byteFloatIndex] |= mask;
					}
				}catch(WarningException e){
					throw new ErrorException("BitStream reaches end of bufferJPCParameters.");
				}
				mask >>= 1;
			}
			try{
				JPKHeading.deleteBeginBits(8);
			}catch(WarningException e){
				throw new ErrorException("BitStream error.");
			}
		}
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.put(byteFloat[3]);
		bb.put(byteFloat[2]);
		bb.put(byteFloat[1]);
		bb.put(byteFloat[0]);
		bb.rewind();
		value = bb.getFloat();
		return(value);
	}
		
	/**
	 * Reads a byte from the file and returns its value.
	 *
	 * @return the byte readed
	 *
	 * @throws ErrorException when some wrong bitstream or I/O operation occurs
	 */
	private int readByte() throws ErrorException{
		int value;
		
		try{
			value = in.read();			
		}catch(IOException e){
			throw new ErrorException("I/O error (" + e.toString() + ").");
		}
		return(value);
	}	
	
}