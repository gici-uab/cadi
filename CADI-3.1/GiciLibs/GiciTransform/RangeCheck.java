/*
 * GICI Library -
 * Copyright (C) 2007  Group on Interactive Coding of Images (GICI)
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
package GiciTransform;
import GiciException.*;


/**
 * This class receives an image and check that all image samples have a valid range.<br>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; set functions<br>
 * &nbsp; run<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public class RangeCheck{

	/**
	 * Original image samples (index meaning [z][y][x]).
	 * <p>
	 * All values allowed.
	 */
	float[][][] imageSamples = null;

	/**
	 * Number of components of the image
	 */
	int zSize;

	/**
	 * Height of the image
	 */
	int ySize;

	/**
	 * Width of the image
	 */
	int xSize;

	/**
	 * Range type to apply.
	 * <p>
	 * Valid values are:
	 *   <ul>
	 *     <li> 0 - No range check
	 *     <li> 1 - Solve out of range errors setting sample to max or min
	 *     <li> 2 - Throw exception when an out of range is detected
	 *   </ul>
	 */
	int RCType;

	/**
	 * Level Shift
	 */
	boolean[] LSSignedComponents = null;

	/**
	 * Number of bits for pixel for every component of the image
	 */
	int[] QComponentsBits = null;

	//INTERNAL VARIABLES

	/**
	 * To know if parameters are set.
	 * <p>
	 * True indicates that they are set otherwise false.
	 */
	boolean parametersSet = false;


	/**
	 * Constructor that receives the original image samples.
	 *
	 * @param imageSamples definition in {@link #imageSamples}
	 */
	public RangeCheck(float[][][] imageSamples){
		//Image data copy
		this.imageSamples = imageSamples;

		//Size set
		zSize = imageSamples.length;
		ySize = imageSamples[0].length;
		xSize = imageSamples[0][0].length;
	}

	/**
	 * Set the parameters used to do the range check operation.
	 *
	 * @param RCType definition in {@link #RCType}
	 * @param LSSignedComponents definition in {@link #LSSignedComponents}
	 * @param QComponentsBits definition in {@link #QComponentsBits}
	 */
	public void setParameters(int RCType, boolean[] LSSignedComponents, int[] QComponentsBits){
		parametersSet = true;

		//Parameters copy
		this.RCType = RCType;
		this.LSSignedComponents = LSSignedComponents;
		this.QComponentsBits = QComponentsBits;
	}

	/**
	 * Check image samples range.
	 *
	 * @return the check range image
	 *
	 * @throws ErrorException when some oput of range is detected (if RCType == 2)
	 */
	public float[][][] run() throws ErrorException{
		//If parameters are not set run cannot be executed
		if(!parametersSet){
			throw new ErrorException("Parameters not set.");
		}

		if(RCType != 0){
			//Check that range is in QComponentsBits
			for(int z = 0; z < zSize; z++){
				int min;
				int max;
				if(LSSignedComponents[z]){
					min = (int) Math.pow(2D, QComponentsBits[z] - 1) * (-1);
					max = (int) Math.pow(2D, QComponentsBits[z] - 1) - 1;
				}else{
					min = 0;
					max = (int) Math.pow(2D, QComponentsBits[z]) -1;
				}
				for(int y = 0; y < ySize; y++){
				for(int x = 0; x < xSize; x++){
					if(imageSamples[z][y][x] < min){
						switch(RCType){
						case 1:
							imageSamples[z][y][x] = min;
							break;
						case 2:
							throw new ErrorException("Minimum out of range sample.");
						}
					}
					if(imageSamples[z][y][x] > max){
						switch(RCType){
						case 1:
							imageSamples[z][y][x] = max;
							break;
						case 2:
							throw new ErrorException("Out of range sample.");
						}

					}
				}}
			}
		}

		//Return level unshifted image
		return(imageSamples);
	}

}
