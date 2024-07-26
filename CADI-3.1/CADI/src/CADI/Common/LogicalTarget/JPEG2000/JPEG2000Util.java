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
package CADI.Common.LogicalTarget.JPEG2000;

import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Common.Util.CADIDimension;
import GiciException.WarningException;

/**
 * This class contains useful functions to handle JPEG2000 images or its parameters.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.2 2011/08/21
 */
public class JPEG2000Util {

	/**
	 * Converts from the tile-component-precinct identifier to the unique
	 * inClassIdentifier precinct identifier.
	 * 
	 * @param tile the tile which the precinct belongs.
	 * @param numTiles the maximum number of image tiles
	 * @param component the component which the precinct belongs
	 * @param numComponents the maximum number of image components
	 * @param precinct the number of precinct within the tile-component
	 * 			following a raster mode
	 * 
	 * @return the precinct unique identifier whithin its codestream.
	 */
	public static long TCPToInClassIdentifier (int tile, int numTiles,
	                                           int component, int numComponents,
	                                           int precinct) {
		return tile + (component + precinct * numComponents) * numTiles;
	}
	
	/**
	 * Converts from the unique inClassIdentifier precinct identifier to
	 * tile-component-precinct identifier.
	 * 
	 * @param inClassIdentifier definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}.
	 * @param numTiles the maximum number of image tiles
	 * @param numComponents the maximum number of image components.
	 *  
	 * @return an one-dimension array of integers with the
	 * 			Tile-Component-Precinct. Indexes of the array means:
	 * 			TCP[tile][component][precinct]
	 */
	public static int[] InClassIdentifierToTCP (long inClassIdentifier,
	                                            int numTiles, int numComponents) {
		int[] tcp = new int[3];
		tcp[0] = (int) (inClassIdentifier % numTiles);
		int temp = (int) ( (inClassIdentifier - tcp[0]) / (double)numTiles );
		tcp[1] = temp % numComponents;
		tcp[2] = (int) ( ( temp - tcp[1] ) / (double)numComponents );

		return tcp;
	}
	
	/**
	 * 
	 * @param xSize
	 * @param ySize
	 * @param XOsize
	 * @param YOsize
	 * @param discardLevels
	 * @return
	 */
	public static CADIDimension calculateFrameSize(int xSize, int ySize,
	                                               int XOsize, int YOsize,
	                                               int discardLevels) {
		assert((xSize > 0) && (ySize > 0));
		assert((XOsize >= 0) && (YOsize >= 0));
		assert(discardLevels >= 0);
		
		return new CADIDimension(
		              (int)Math.ceil(1.0*xSize/(1<<discardLevels))
		              - (int)Math.ceil(1.0*XOsize/(1<<discardLevels)),
									(int)Math.ceil(1.0*ySize/(1<<discardLevels))
									- (int)Math.ceil(1.0*YOsize/(1<<discardLevels))
									);
	}

	/**
	 * 
	 * @param xSize
	 * @param ySize
	 * @param XOsize
	 * @param YOsize
	 * @param discardLevels
	 * @param frameSize
	 */
	public static void determineFrameSize(int xSize, int ySize,
                                        int XOsize, int YOsize,
                                        int discardLevels, CADIDimension frameSize) {
		frameSize.width = (int)Math.ceil(1.0*xSize/(1<<discardLevels))
												- (int)Math.ceil(1.0*XOsize/(1<<discardLevels));
		frameSize.height = (int)Math.ceil(1.0*ySize/(1<<discardLevels))
												- (int)Math.ceil(1.0*YOsize/(1<<discardLevels));
	}
	
	/**
	 * 
	 * @param sizParameters
	 * @param fsiz
	 * @param roundDirection
	 * @return
	 */
	public static int determineNumberOfDiscardLevels(int xSize, int ySize,
	                                                 int XOsize, int YOsize,
	                                                 int[] fsiz, int roundDirection,
                                                   int maxWTLevels) {

		int discardLevels = 0;
		CADIDimension frameSize = new CADIDimension(xSize - XOsize, ySize - YOsize);
		
		switch(roundDirection) {
		case ViewWindowField.ROUND_DOWN:
			while (((frameSize.width > fsiz[0]) || (frameSize.height > fsiz[1]))
              && (maxWTLevels > discardLevels)) {
				discardLevels++;
				determineFrameSize(xSize, ySize, XOsize, YOsize, discardLevels, frameSize);
			}				
			break;

		case ViewWindowField.ROUND_UP:
			do {
				discardLevels++;
				determineFrameSize(xSize, ySize, XOsize, YOsize, discardLevels, frameSize);
			} while ((frameSize.width >= fsiz[0]) && (frameSize.height >= fsiz[1]) && (maxWTLevels >= discardLevels));
			discardLevels--;
			break;

		case ViewWindowField.CLOSEST:
			long requestedTargetArea = fsiz[0]*fsiz[1];
    
			while (true) {
				CADIDimension frameSizeShorter = calculateFrameSize(xSize, ySize, XOsize, YOsize, discardLevels+1);
				long areaDiffToUpperFrameSize = Math.abs(requestedTargetArea-frameSize.width*frameSize.height);
				long areaDiffToLowerFrameSize = Math.abs(requestedTargetArea-frameSizeShorter.width*frameSizeShorter.height);

				if (areaDiffToUpperFrameSize == areaDiffToLowerFrameSize) {
					break;
				} else if (areaDiffToUpperFrameSize == 0L) {
					break;
				} else if (areaDiffToLowerFrameSize == 0L) {
					discardLevels++;
					break;
				} else if (areaDiffToUpperFrameSize > areaDiffToLowerFrameSize) {
					if (areaDiffToLowerFrameSize < requestedTargetArea) {
						discardLevels++;
						break;
					}
				} else if (areaDiffToUpperFrameSize < areaDiffToLowerFrameSize) {
					break;
				}
				discardLevels++;
        frameSize.width = (frameSize.width + 1) >>> 1;
        frameSize.height = (frameSize.height + 1) >>> 1;
			}
			break;

		default:
			assert(true);
		}			

		return discardLevels;
	}
	
	/**
	 * Maps a image region (frame size, region offset and region size) to the
	 * suitable codestream image resolution (frame size) and image region.
	 * <p>
	 * Further information, see see ISO/IEC 15444-9 section C.4.1
	 * 
	 * @param xSize
	 * @param ySize
	 * @param XOsize
	 * @param YOsize
	 * @param fsiz
	 * @param roff
	 * @param rsiz
	 * @param discardLevels
	 */
	public static void mapRegionToSuitableResolutionGrid(int xSize, int ySize,
	                                                     int XOsize, int YOsize,
	                                                     int[] fsiz, int[] roff,
	                                                     int[] rsiz, int discardLevels) {
		assert(fsiz != null);
		assert(roff != null);
		assert(rsiz != null);
		assert(discardLevels >= 0);
		assert((fsiz[0] > 0) && (fsiz[1] > 0));
		assert((fsiz[0] <= xSize) && (fsiz[1] <= ySize));
		assert((roff[0] >= 0) && (roff[1] >= 0));
		assert((roff[0] >= 0) && (roff[1] >= 0));
		assert((rsiz[0] > 0) && (rsiz[1] > 0));
    
		// Find out the suitable frame size
    int tmpXOsize = XOsize;
    int tmpYOsize = YOsize;
    int suitableFX = xSize - tmpXOsize;
    int suitableFY = ySize - tmpYOsize;
    for (int i = 0; i < discardLevels; i++) {
      tmpXOsize = (tmpXOsize+1) >> 1;
      tmpYOsize = (tmpYOsize+1) >> 1;
      suitableFX = ((suitableFX+1) >> 1) - tmpXOsize;
      suitableFY = ((suitableFY+1) >> 1) - tmpYOsize;
    }

		// Find region offset and region size
		rsiz[0] = (int)Math.ceil(((double)rsiz[0]*(double)suitableFX/(double)fsiz[0]));
		rsiz[1] = (int)Math.ceil(((double)rsiz[1]*(double)suitableFY/(double)fsiz[1]));
		roff[0] = (int)Math.floor(((double)roff[0]*(double)suitableFX/(double)fsiz[0]));
		roff[1] = (int)Math.floor(((double)roff[1]*(double)suitableFY/(double)fsiz[1]));
		fsiz[0] = suitableFX;
		fsiz[1] = suitableFY;
	}
	
	/**
	 * 
	 * @param xSize
	 * @param ySize
	 * @param roff
	 * @param rsiz
	 * @param discardLevels
	 */
	public static void mapRegionToHighResolutionGrid(int xSize, int ySize,
	                                                 int[] roff, int[] rsiz,
	                                                 int discardLevels) {
		assert(xSize > 0 && ySize > 0);
		assert(roff != null);
		assert(rsiz != null);
		assert(discardLevels >= 0);
		
		roff[0] = roff[0] << discardLevels;
		roff[1] = roff[1] << discardLevels;
		rsiz[0] = rsiz[0] << discardLevels;
		rsiz[1] = rsiz[1] << discardLevels;
		
		if (roff[0]+rsiz[0] > xSize) rsiz[0] = xSize - roff[0];
		if (roff[1]+rsiz[1] > ySize) rsiz[1] = ySize - roff[1];
	}
	
	/**
	 * Calculate the rate disortion adjustment (depending on BCRateDistortionAdjustment) for a resolution level and subband.
	 *
	 * @param BCLCRateDistortionAdjustment rate distortion adjustment to be used for each component
	 * @param z image component
	 * @param totalRLevels the total number of resolution levels
	 * @param rLevel 0 is the LL subband, and 1, 2, ... represents next starting with the little one
	 * @param subband 0 - HL, 1 - LH, 2 - HH (if resolutionLevel == 0 --> 0 - LL)
	 * @return log_2 of the multiplier (the number of bit that have to be shifted)
	 *
	 * @throws WarningException when rateDistortion adjustment type is unrecognized
	 */
	public static int calculateRateDistortionAdjustment(int[] BCLCRateDistortionAdjustment,
	                                                    int z, int totalRLevels,
	                                                    int rLevel, int subband) throws WarningException{
		int rateDistortionAdjustment = 0;
		switch(BCLCRateDistortionAdjustment[z]) {
			case 0: //No adjustment
				rateDistortionAdjustment = 0;
				break;
			case 1: //Adjustment depending on rLevel and subband
				//rateDistortionAdjustment = totalRLevels - rLevel - (subband == 2 ? 1: 0);
				rateDistortionAdjustment = totalRLevels - rLevel - (subband == 2 ? 1: 0) - 1;
				if(rateDistortionAdjustment < 0) rateDistortionAdjustment = 0;
				break;
			case 2: //Adjustment depending on rLevel and subband and more valoration of Component 0 (for RCT)
				rateDistortionAdjustment = totalRLevels - rLevel - (subband == 2 ? 1: 0);
				//rateDistortionAdjustment = totalRLevels - rLevel - (subband == 2 ? 1: 0) - 1;
				//if(rateDistortionAdjustment < 0) rateDistortionAdjustment = 0;
				if(z==0){
					//This value is calculated experimentally
					rateDistortionAdjustment += 2;
				}
				break;
			default:
				throw new WarningException("Unrecognized rate-distortion adjustment in Bit Plane Coding stage.");
		}
		return rateDistortionAdjustment;
	}
  
}