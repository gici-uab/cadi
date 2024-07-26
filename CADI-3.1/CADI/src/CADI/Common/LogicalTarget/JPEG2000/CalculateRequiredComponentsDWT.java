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

/**
 * This class calculates which components are needed to decompres one component
 * when the DWT has been applied.
 * <p>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; run<br>
 * &nbsp; get functions<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2008/09/08
 */
public class CalculateRequiredComponentsDWT {
			
	/**
	 * Is an one-dimension array that indicates which are the components to be
	 * decompressed.
	 */
	private boolean[] inputComponents = null;
		
	/**
	 * DWT levels to apply for each component.
	 * <p>
	 * Negative values not allowed.
	 */
	private int WTLevels;
	
	/**
	 * Discrete wavelet transform to be applied for each component.
	 * <p>
	 * Valid values are:<br>
	 *   <ul>
	 *     <li> 0 - None
	 *     <li> 1 - Reversible 5/3 DWT (JPEG2000)
	 *     <li> 2 - Real Isorange (Irreversible) 9/7 DWT (JPEG2000 standard)
	 *     <li> 3 - Real Isonorm (Irreversible) 9/7 DWT (CCSDS-Recommended)
	 *     <li> 4 - Integer (Reversible) 9/7M DWT (CCSDS-Recommended)
	 *     <li> 5 - Integer 5/3 DWT (Classic construction)
	 *     <li> 6 - Integer 9/7 DWT (Classic construction)
	 *   </ul>
	 */
	private int WTType;
	
	
	//INTERNAL ATTRIBUTES
	
	/**
	 * Maximum number of components.
	 */
	private int maxComponents;
	
	/**
	 * Contains a bitmap that indicates if the component is relevant.
	 */
	private byte[] bitMap = null;
	
	/**
	 * Contains a bitmap that indicates if a component is marked to be checked in the next resolution level. (index meaning [z][rLevel][x,y] one bit bit simulates an x,y position).
	 */
	private byte[][] nextRlevelBitMap = null;
	
	/**
	 * Contains the subband sizes by component and resolution level.
	 * <ul>
	 *   <li> 1 - rLevel
	 *   <li> 2 - subband where 0-HL, 1-LH, 2-HH
	 *   <li> 3 - coordenates where 0-yBegin, 1-xBegin, 2-yEnd, 3-xEnd
	 * </ul>
	 */
	private int[][][] subbandSizes = null;
	
	/**
	 * Position correspondence for filter 5/3 if the coefficient position in a normal domain is in a Even-Even position (2n,2m).
	 * <p>
	 * First array index represents the x position in the next resolution level and the second one y position(p.e. if x=6 and y=4, in the next resolution level for HL subband will have correspondence with (x=3,y=1) and (x=3,y=2).
	 * <ul>
	 *   <li> 0 - x position
	 *   <li> 1 - y position
	 *   <li> 2 - subband where 0-HL, 1-LH, 2-HH, 3-LL
	 * </ul>
	 */
	private static final int[][] filter_5_3_EvenEven = {
		{0,   0,	0},
		{-1,  0,	0},
		{0,   0,	1},
		{0,  -1,	1}, 	
		{0,   0,	2},
		{0,  -1,	2},
		{-1,  0,	2},
		{-1, -1,	2},
		{0,   0,	3},
	};
	
	/**
	 * Position correspondence for filter 5/3 if the coefficient position in a normal domain is in a Even-Odd position (2n,2m).
	 * <p>
	 * First array index represents the x position in the next resolution level and the second one y position.
	 * <ul>
	 *   <li> 0 - x position
	 *   <li> 1 - y position
	 *   <li> 2 - subband where 0-HL, 1-LH, 2-HH, 3-LL
	 * </ul>
	 */
	private static final int[][] filter_5_3_EvenOdd = {
		{-1,  1,	0},
		{0,   1,	0},
		{0,   1,	1},
		{-1,  1,	2},
		{0,   1,	2}, 
		{0,   1,	3},
	};
	
	/**
	 * Position correspondence for filter 5/3 if the coefficient position in a normal domain is in a Odd-Even position (2n,2m).
	 * <p>
	 * First array index represents the x position in the next resolution level and the second one y position.
	 * <ul>
	 *   <li> 0 - x position
	 *   <li> 1 - y position
	 *   <li> 2 - subband where 0-HL, 1-LH, 2-HH, 3-LL
	 * </ul>
	 */
	private static final int[][] filter_5_3_OddEven = {
		{1,   0,	0},
		{1,   0,	1},
		{1,  -1,	1},
		{1,  -1,	2},
		{1,   0,	2},
		{1,   0,	3},
	};
	
	/**
	 * Position correspondence for filter 5/3 if the coefficient position in a normal domain is in a Odd-Odd position (2n,2m).
	 * <p>
	 * First array index represents the x position in the next resolution level and the second one y position.
	 * <ul>
	 *   <li> 0 - x position
	 *   <li> 1 - y position
	 *   <li> 2 - subband where 0-HL, 1-LH, 2-HH, 3-LL
	 * </ul>
	 */
	private static final int[][] filter_5_3_OddOdd = {
		{1,   1,	0},
		{1,   1,	1},
		{1,   0,	2},
		{0,   1,	2},
		{1,   1,	2},
		{1,   1,	3},
	};
	
	/**
	 * Position correspondence for filter 9/7 if the coefficient position in a normal domain is in a any-any position.
	 * <p>
	 * First array index represents the x position in the next resolution level and the second one y position.
	 * <ul>
	 *   <li> 0 - x position
	 *   <li> 1 - y position
	 *   <li> 2 - subband where 0-HL, 1-LH, 2-HH, 3-LL
	 * </ul>
	 */
	private static final int[][] filter_9_7_AnyAny = {
		{-2,  -1,	0},
		{-2,   0,	0},
		{-2,   1,	0},
		{-2,   2,	0},
		{-1,  -1,	0},
		{-1,   0,	0},
		{-1,   1,	0},
		{-1,   2,	0},
		{0,   -1,	0},
		{0,    0,	0},
		{0,    1,	0},
		{0,    2,	0},
		{1,   -1,	0},
		{1,    0,	0},
		{1,    1,	0},
		{1,    2,	0},
		{2,   -1,	0},
		{2,    0,	0},
		{2,    1,	0},
		{2,    2,	0},
		{-1,  -2,	1},
		{-1,  -1,	1},
		{-1,   0,	1},
		{-1,   1,	1},
		{-1,   2,	1},
		{0,   -2,	1},
		{0,   -1,	1},
		{0,    0,	1},
		{0,    1,	1},
		{0,    2,	1},
		{1,   -2,	1},
		{1,   -1,	1},
		{1,    0,	1},
		{1,    1,	1},
		{1,    2,	1},
		{2,   -2,	1},
		{2,   -1,	1},
		{2,    0,	1},
		{2,    1,	1},
		{2,    2,	1},
		{-2,  -2,	2},
		{-2,  -1,	2},
		{-2,   0,	2},
		{-2,   1,	2},
		{-2,   2,	2},
		{-1,  -2,	2},
		{-1,  -1,	2},
		{-1,   0,	2},
		{-1,   1,	2},
		{-1,   2,	2},
		{0,   -2,	2},
		{0,   -1,	2},
		{0,    0,	2},
		{0,    1,	2},
		{0,    2,	2},
		{1,   -2,	2},
		{1,   -1,	2},
		{1,    0,	2},
		{1,    1,	2},
		{1,    2,	2},
		{2,   -2,	2},
		{2,   -1,	2},
		{2,    0,	2},
		{2,    1,	2},
		{2,    2,	2},
		{-1,  -1,	3},
		{-1,   0,	3},
		{-1,   1,	3},
		{-1,   2,	3},
		{0,   -1,	3},
		{0,    0,	3},
		{0,    1,	3},
		{0,    2,	3},
		{1,   -1,	3},
		{1,    0,	3},
		{1,    1,	3},
		{1,    2,	3},
		{2,   -1,	3},
		{2,    0,	3},
		{2,    1,	3},
		{2,    2,	3},
	};

	/**
	 * Position correspondence for filter 9/7 if the coefficient position in a normal domain is in a any-any position, to mark the coefficients in the 2 next columns, special cases for optimization.
	 * <p>
	 * First array index represents the x position in the next resolution level and the second one y position.
	 * <ul>
	 *   <li> 0 - x position
	 *   <li> 1 - y position
	 *   <li> 2 - subband where 0-HL, 1-LH, 2-HH, 3-LL
	 * </ul>
	 */
	private static final int[][] filter_9_7_AnyAny_x = {
		{2,   -1,	0},
		{2,    0,	0},
		{2,    1,	0},
		{2,    2,	0},
		{2,   -2,	1},
		{2,   -1,	1},
		{2,    0,	1},
		{2,    1,	1},
		{2,    2,	1},
		{2,   -2,	2},
		{2,   -1,	2},
		{2,    0,	2},
		{2,    1,	2},
		{2,    2,	2},
		{2,   -1,	3},
		{2,    0,	3},
		{2,    1,	3},
		{2,    2,	3},
		};
	
	/**
	 * Position correspondence for filter 9/7 if the coefficient position in a normal domain is in a any-any position, to mark the coefficients in the 2 next rows, special cases for optimization.
	 * <p>
	 * First array index represents the x position in the next resolution level and the second one y position.
	 * <ul>
	 *   <li> 0 - x position
	 *   <li> 1 - y position
	 *   <li> 2 - subband where 0-HL, 1-LH, 2-HH, 3-LL
	 * </ul>
	 */
	private static final int[][] filter_9_7_AnyAny_y = {
		{-2,   2,	0},
		{-1,   2,	0},
		{0,    2,	0},
		{1,    2,	0},
		{2,    2,	0},
		{-1,   2,	1},
		{0,    2,	1},
		{1,    2,	1},
		{2,    2,	1},
		{-2,   2,	2},
		{-1,   2,	2},
		{0,    2,	2},
		{1,    2,	2},
		{2,    2,	2},
		{-1,   2,	3},
		{0,    2,	3},
		{1,    2,	3},
		{2,    2,	3},
		};
	
	/**
	 *
	 */
	private int ySize = 1;
	
	/**
	 * Maximum number of resolution levels.
	 */
	private int maxRLevel;
	

// ============================= public methods ==============================
	
	/**
	 * Constructor.
	 * 
	 * @param inputComponents definition in {@link #inputComponents}.
	 * @param WTLevels definition in {@link #WTLevels}.
	 * @param WTType definition in {@link #WTType}.
	 */
	public CalculateRequiredComponentsDWT(boolean[] inputComponents, int WTLevels, int WTType){
				
		//Parameters copy
		this.inputComponents = inputComponents;
		this.WTLevels = WTLevels;
		this.WTType = WTType;
		
		maxComponents = inputComponents.length;
	}
	
	/**
	 * Performs the calculus of the number of components which are needed to
	 * inver the DWT.
	 */
	public void run(){
		
		//memory allocation for subband sizes
		bitMap = new byte[(int)Math.ceil((double)(maxComponents)/8)];

		subbandSizes = subbandSizesCalculation();
		nextRlevelBitMap = new byte[WTLevels][];
		for(int rLevel = 0; rLevel < WTLevels; rLevel++) {
			nextRlevelBitMap[rLevel] = new byte[(int)Math.ceil((double)(subbandSizes[rLevel][1][0]*subbandSizes[rLevel][0][1])/8)];
		}

		if(WTLevels == 0) {

			for(int x = 0; x < maxComponents; x++) {
				if( isSelected(x) ) {
					bitMap[x/8] |= (byte)(1 << (7-(x% 8)));
				}
			}

		} else {
			maxRLevel = WTLevels-1;

			//select the raster depending on the wavelet filter
			switch(WTType) {
				case 1:
					rasterWavelet5_3();
					break;
				case 2:
					rasterWavelet9_7();
					break;
				case 3:
					rasterWavelet9_7();
					break;
				default:
							
			}
		}			
	}
	
	/**
	 * Returns an one-dimensional array of booleans. If the value of the
	 * element is true means that this component is required to invert the
	 * DWT. Otherwise, it is not required.
	 * 
	 * @return an one-dimensional array with the relevant components. The array
	 * 			size if the maximum number of components. And its elements are
	 * 			<code>true</code> if the component is relevant, and <code>false
	 * 			</code> if it is not.
	 */
	public boolean[] getRequiredComponents(){
		boolean[] comps = new boolean[maxComponents];
		for (int i = 0; i < maxComponents; i++) {
			byte value1 = bitMap[i/8];
			byte value2 = (byte)(value1 & (1 << (7-i%8)));
			comps[i] = (value2 == 0) ? false : true;
		}
		return comps;
	}
	
	
	// ============================ private methods ==============================
		
	/**
	 * Performs the raster for a component if the wavelet transform is 5/3 (-wt 1)
	 *
	 */
	private void rasterWavelet5_3(){
		
		//Raster the image to set the no-data values
		for(int rLevel = 0; rLevel < WTLevels; rLevel++){
			for(int y = 0; y < subbandSizes[rLevel][1][2]; y++){
				for(int x = 0; x < subbandSizes[rLevel][0][3]; x++){	
					if(rLevel == 0){
						if(isSelected(x)){
							if((x % 2 == 0) && (y % 2 == 0)){ //even even
								evenEven(rLevel, y, x);
							}else if((x % 2 == 0) && (y % 2 == 1)){ //even odd
								if(y > 1 && x < maxComponents-1){
									if(isSelected(x) || isSelected(x+1)){
										evenOdd(rLevel, y, x);
									}else{
										evenEven(rLevel, y, x);
										evenOdd(rLevel, y, x);
									}
								}else{
									evenEven(rLevel, y, x);
									evenOdd(rLevel, y, x);
								}
							}else if((x % 2 == 1) && (y % 2 == 0)){ //odd even
								if(y > 1 && x < maxComponents-1){
									if(isSelected(x-1)){
										oddEven(rLevel, y, x);
									}else{
										evenEven(rLevel, y, x);
										oddEven(rLevel, y, x);
									}
								}else{
									evenEven(rLevel, y, x);
									oddEven(rLevel, y, x);
								}
							}else if((x % 2 == 1) && (y % 2 == 1)){ //odd odd
								if(y > 1 && x < maxComponents-1){
									if(isSelected(x-1) && isSelected(x)){
										oddOdd(rLevel, y, x);
									}else{
										evenEven(rLevel, y, x);
										oddEven(rLevel, y, x);
										evenOdd(rLevel, y, x);
										oddOdd(rLevel, y, x);
									}
								}else{
									evenEven(rLevel, y, x);
									oddEven(rLevel, y, x);
									evenOdd(rLevel, y, x);
									oddOdd(rLevel, y, x);
								}
							}
						}
					}else{
						if(isMarked(rLevel, y, x)){
							if((x % 2 == 0) && (y % 2 == 0)){ //even even
								evenEven(rLevel, y, x);
							}else if((x % 2 == 0) && (y % 2 == 1)){ //even odd
								if(y > 1 && x < maxComponents-1){
									if(isMarked(rLevel,y-1,x) || isMarked(rLevel,y-1,x+1)){
										evenOdd(rLevel, y, x);
									}else{
										evenEven(rLevel, y, x);
										evenOdd(rLevel, y, x);
									}
								}else{
									evenEven(rLevel, y, x);
									evenOdd(rLevel, y, x);
								}
							}else if((x % 2 == 1) && (y % 2 == 0)){ //odd even
								if(y > 1 && x < maxComponents-1){
									if(isMarked(rLevel,y,x-1)){
										oddEven(rLevel, y, x);
									}else{
										evenEven(rLevel, y, x);
										oddEven(rLevel, y, x);
									}
								}else{
									evenEven(rLevel, y, x);
									oddEven(rLevel, y, x);
								}
							}else if((x % 2 == 1) && (y % 2 == 1)){ //odd odd
								if(y > 1 && x < maxComponents-1){
									if(isMarked(rLevel,y,x-1) && isMarked(rLevel,y-1,x)){
										oddOdd(rLevel, y, x);
									}else{
										evenEven(rLevel, y, x);
										oddEven(rLevel, y, x);
										evenOdd(rLevel, y, x);
										oddOdd(rLevel, y, x);
									}
								}else{
									evenEven(rLevel, y, x);
									oddEven(rLevel, y, x);
									evenOdd(rLevel, y, x);
									oddOdd(rLevel, y, x);
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Performs the raster for a component if the wavelet transform is 9/7 (-wt 2)
	 *
	 */
	private void rasterWavelet9_7(){
		
		//Raster the image to set the no-data values
		for(int rLevel = 0; rLevel < WTLevels; rLevel++){
		for(int y = 0; y < subbandSizes[rLevel][1][2]; y++){
		for(int x = 0; x < subbandSizes[rLevel][0][3]; x++){	
			if(rLevel == 0){
				if(isSelected(x)){
					if((x % 2 == 0) && (y % 2 == 0)){ //even even
						if(y > 1 && x > 0 && x < maxComponents-1){
							if(isSelected(x) || isSelected(x) || isSelected(x+1) || isSelected(x+1)) {
								anyAny_y(rLevel, y, x);
							}else{
								if(isSelected(x-2) || isSelected(x-1)) {
									anyAny_x(rLevel, y, x);
								}else{
									anyAny(rLevel, y, x);
								}
							}
						}else{
							anyAny(rLevel, y, x);
						}
						
					}else if((x % 2 == 0) && (y % 2 == 1)){ //even odd
						if(y > 2 && x > 2 && x < maxComponents-1){
							if(isSelected(x) || isSelected(x) || isSelected(x+1) || isSelected(x+1)){
								if(!isSelected(x) || !isSelected(x+1)){
									anyAny_y(rLevel, y, x);
								}
							}else{
								if(isSelected(x-2) || isSelected(x-1) || isSelected(x-2) || isSelected(x-1)){
									anyAny_x(rLevel, y, x);
								}else{
									anyAny(rLevel, y, x);
								}
							}
						}else{
							anyAny(rLevel, y, x);
						}
						
					}else if((x % 2 == 1) && (y % 2 == 0)){ //odd even	
						if(y > 2 && x > 1 && x < maxComponents-1){
							if(isSelected(x) || isSelected(x) || isSelected(x-1) || isSelected(x-1)){
								if(!isSelected(x-1)){
									anyAny_y(rLevel, y, x);
								}
							}else{
								if(isSelected(x-2) || isSelected(x-1)){
									anyAny_x(rLevel, y, x);
								}else{
									anyAny(rLevel, y, x);
								}
							}
						}else{
							anyAny(rLevel, y, x);
						}
					}else if((x % 2 == 1) && (y % 2 == 1)){ //odd odd
						if(y > 1 && x > 1){
							if(isSelected(x) || isSelected(x) || isSelected(x-1) || isSelected(x-1)){
								if(!isSelected(x) || !isSelected(x-1) || !isSelected(x-1)){
									anyAny_y(rLevel, y, x);
								}
							}else{
								if(isSelected(x-2) || isSelected(x-1) || isSelected(x-2) || isSelected(x-1)){
									anyAny_x(rLevel, y, x);
								}else{
									anyAny(rLevel, y, x);
								}
							}
						}else{
							if(!isSelected(x) || !isSelected(x-1) || !isSelected(x-1)){
								anyAny(rLevel, y, x);
							}
						}
					}	
				}
			}else{
				if(isMarked(rLevel,y,x)){
					if((x % 2 == 0) && (y % 2 == 0)){ //even even
						if(y > 1 && x > 0 && x < maxComponents-1){
							if(isMarked(rLevel,y-2,x) || isMarked(rLevel,y-1,x) || isMarked(rLevel,y-2,x+1) || isMarked(rLevel,y-1,x+1)){
								anyAny_y(rLevel, y, x);
							}else{
								if(isMarked(rLevel,y,x-2) || isMarked(rLevel,y,x-1)){
									anyAny_x(rLevel, y, x);
								}else{
									anyAny(rLevel, y, x);
								}
							}
						}else{
							anyAny(rLevel, y, x);
						}
						
					}else if((x % 2 == 0) && (y % 2 == 1)){ //even odd
						if(y > 2 && x > 2 && x < maxComponents-1){
							if(isMarked(rLevel,y-3,x) || isMarked(rLevel,y-2,x) || isMarked(rLevel,y-3,x+1) || isMarked(rLevel,y-2,x+1)){
								if(!isMarked(rLevel,y-1,x) || !isMarked(rLevel,y-1,x+1)){
									anyAny_y(rLevel, y, x);
								}
							}else{
								if(isMarked(rLevel,y,x-2) || isMarked(rLevel,y,x-1) || isMarked(rLevel,y-1,x-2) || isMarked(rLevel,y-1,x-1)){
									anyAny_x(rLevel, y, x);
								}else{
									anyAny(rLevel, y, x);
								}
							}
						}else{
							anyAny(rLevel, y, x);
						}
						
					}else if((x % 2 == 1) && (y % 2 == 0)){ //odd even	
						if(y > 2 && x > 1 && x < maxComponents-1){
							if(isMarked(rLevel,y-2,x) || isMarked(rLevel,y-1,x) || isMarked(rLevel,y-2,x-1) || isMarked(rLevel,y-1,x-1)){
								if(!isMarked(rLevel,y,x-1)){
									anyAny_y(rLevel, y, x);
								}
							}else{
								if(isMarked(rLevel,y,x-2) || isMarked(rLevel,y,x-1)){
									anyAny_x(rLevel, y, x);
								}else{
									anyAny(rLevel, y, x);
								}
							}
						}else{
							anyAny(rLevel, y, x);
						}
					}else if((x % 2 == 1) && (y % 2 == 1)){ //odd odd
						if(y > 1 && x > 1){
							if(isMarked(rLevel,y-2,x) || isMarked(rLevel,y-1,x) || isMarked(rLevel,y-2,x-1) || isMarked(rLevel,y-1,x-1)){
								if(!isMarked(rLevel,y-1,x) || !isMarked(rLevel,y-1,x-1) || !isMarked(rLevel,y,x-1)){
									anyAny_y(rLevel, y, x);
								}
							}else{
								if(isMarked(rLevel,y-1,x-2) || isMarked(rLevel,y-1,x-1) || isMarked(rLevel,y,x-2) || isMarked(rLevel,y,x-1)){
									anyAny_x(rLevel, y, x);
								}else{
									anyAny(rLevel, y, x);
								}
							}
						}else{
							if(!isMarked(rLevel,y-1,x) || !isMarked(rLevel,y-1,x-1) || !isMarked(rLevel,y,x-1)){
								anyAny(rLevel, y, x);
							}
						}
					}
				}
			}	
		}}}
	}
	
	/**
	 * Mark a coefficient in the bitMap.
	 *
	 * @param rLevel ressolution level where belong the x and y postion.
	 * @param y position of the subband and rlevel.
	 * @param x position of the subband and rlevel.
	 * @param subband where belong the x and y postion.
	 * 
	 */
	private void shift(int rLevel, int y, int x, int subband){
		boolean inRange = false;
		//mirror efect
		if(x < 0){
			x = -x;
		}
		if(y < 0){
			y = -y;
		}
		//checking if the x y position belong to the subband domain
		if((rLevel != maxRLevel)){//[z][rLevel][{LH,HL,HH}][{yBegin,xBegin,yEnd,xEnd}]
			if((y < ySize) && (y + subbandSizes[rLevel][subband][0] < subbandSizes[rLevel][subband][2]) && (x < maxComponents) && (x + subbandSizes[rLevel][subband][1] < subbandSizes[rLevel][subband][3])){
				inRange = true;
			}
		}else{//LL
			if((y < ySize) && (y < subbandSizes[rLevel][1][0]) && (x < maxComponents) && (x < subbandSizes[rLevel][0][1])){
				inRange = true;
			}
		}
		if(inRange){
			//mark the coefficient as shifted in the bitMap
			if((subband != 3) && (rLevel < WTLevels)){
				y += subbandSizes[rLevel][subband][0];
				x += subbandSizes[rLevel][subband][1];	
			}
			//mirror efect
			if(y > ySize){
				y = y - (y - ySize);
			}
			if(y == ySize){
				y--;
			}
			if(x > maxComponents){
				x = x - (x - maxComponents);
			}
			if(x == maxComponents){
				x--;
			}
			if((y >= 0) && (y <= ySize) && (x >= 0) && (x <= maxComponents)){
				bitMap[((y * maxComponents) + x) / 8] |= (byte)(1 << (7-(((y * maxComponents) + x) % 8)));
			}
		}	
	}

	/**
	 * Mark the required coefficients if the coefficient is in a position (any,any) only for wavelet filter 9/7.
	 *
	 * @param rLevel is the ressolution level
	 * @param y position
	 * @param x position
	 * 
	 */
	private void anyAny(int rLevel, int y, int x){
		if((x % 2 == 0) && (y % 2 == 0)){ //even even
			x = x/2;
			y = y/2;
		}else if((x % 2 == 0) && (y % 2 == 1)){ //even odd
			y = (y--)/2;
			x = x/2;
		}else if((x % 2 == 1) && (y % 2 == 0)){ //odd even
			x = (x--)/2;
			y = y/2;
		}else if((x % 2 == 1) && (y % 2 == 1)){ //odd odd
			x = (x--)/2;
			y = (y--)/2;
		}
		for(int i = 0; i < filter_9_7_AnyAny.length - 16; i++){
			shift(rLevel, y + filter_9_7_AnyAny[i][1], x + filter_9_7_AnyAny[i][0], filter_9_7_AnyAny[i][2]);
		}
		for(int i = filter_9_7_AnyAny.length - 16; i < filter_9_7_AnyAny.length; i++){
			if(rLevel == maxRLevel){
				shift(rLevel, y + filter_9_7_AnyAny[i][1], x + filter_9_7_AnyAny[i][0], filter_9_7_AnyAny[i][2]);
			}else{
				mark(rLevel, y + filter_9_7_AnyAny[i][1], x + filter_9_7_AnyAny[i][0]);
			}
		}		
	}
	
	/**
	 * Mark the required coefficients if the coefficient is in a position (any,any) and the (any,any-2) has been marked before, only for wavelet filter 9/7.
	 *
	 * @param rLevel is the ressolution level
	 * @param y position
	 * @param x position
	 * 
	 */
	private void anyAny_y(int rLevel, int y, int x){
		if((x % 2 == 0) && (y % 2 == 0)){ //even even
			x = x/2;
			y = y/2;
		}else if((x % 2 == 0) && (y % 2 == 1)){ //even odd
			y = (y--)/2;
			x = x/2;
		}else if((x % 2 == 1) && (y % 2 == 0)){ //odd even
			x = (x--)/2;
			y = y/2;
		}else if((x % 2 == 1) && (y % 2 == 1)){ //odd odd
			x = (x--)/2;
			y = (y--)/2;
		}
		for(int i = 0; i < filter_9_7_AnyAny_y.length - 4; i++){
			shift(rLevel, y + filter_9_7_AnyAny_y[i][1], x + filter_9_7_AnyAny_y[i][0], filter_9_7_AnyAny_y[i][2]);
		}
		for(int i = filter_9_7_AnyAny_y.length - 4; i < filter_9_7_AnyAny_y.length; i++){
			if(rLevel == maxRLevel){
				shift(rLevel, y + filter_9_7_AnyAny_y[i][1], x + filter_9_7_AnyAny_y[i][0], filter_9_7_AnyAny_y[i][2]);
			}else{
				mark(rLevel, y + filter_9_7_AnyAny_y[i][1], x + filter_9_7_AnyAny_y[i][0]);
			}
		}		
	}
	
	/**
	 * Mark the required coefficients if the coefficient is in a position (any,any) and if (any-2,any) has been marked before, only for wavelet filter 9/7.
	 *
	 * @param rLevel is the ressolution level
	 * @param y position
	 * @param x position
	 * 
	 */
	private void anyAny_x(int rLevel, int y, int x){
		if((x % 2 == 0) && (y % 2 == 0)){ //even even
			x = x/2;
			y = y/2;
		}else if((x % 2 == 0) && (y % 2 == 1)){ //even odd
			y = (y--)/2;
			x = x/2;
		}else if((x % 2 == 1) && (y % 2 == 0)){ //odd even
			x = (x--)/2;
			y = y/2;
		}else if((x % 2 == 1) && (y % 2 == 1)){ //odd odd
			x = (x--)/2;
			y = (y--)/2;
		}
		for(int i = 0; i < filter_9_7_AnyAny_x.length - 4; i++){
			shift(rLevel, y + filter_9_7_AnyAny_x[i][1], x + filter_9_7_AnyAny_x[i][0], filter_9_7_AnyAny_x[i][2]);
		}
		for(int i = filter_9_7_AnyAny_x.length - 4; i < filter_9_7_AnyAny_x.length; i++){
			if(rLevel == maxRLevel){
				shift(rLevel, y + filter_9_7_AnyAny_x[i][1], x + filter_9_7_AnyAny_x[i][0], filter_9_7_AnyAny_x[i][2]);
			}else{
				mark(rLevel, y + filter_9_7_AnyAny_x[i][1], x + filter_9_7_AnyAny_x[i][0]);
			}
		}		
	}
	/**
	 * Mark the required coefficients if the coefficient is in a position (2n,2n).
	 *
	 * @param rLevel is the ressolution level
	 * @param x position
	 */
	private void evenEven(int rLevel, int y, int x){
		x = x/2;
		y = y/2;
		for(int i = 0; i < filter_5_3_EvenEven.length - 1; i++){
			shift(rLevel, y + filter_5_3_EvenEven[i][1], x + filter_5_3_EvenEven[i][0], filter_5_3_EvenEven[i][2]);
		}
		for(int i = filter_5_3_EvenEven.length - 1; i < filter_5_3_EvenEven.length; i++){
			if(rLevel == maxRLevel){
				shift(rLevel, y + filter_5_3_EvenEven[i][1], x + filter_5_3_EvenEven[i][0], filter_5_3_EvenEven[i][2]);
			}else{	
				mark(rLevel, y + filter_5_3_EvenEven[i][1], x + filter_5_3_EvenEven[i][0]);
			}
		}
		
	}
	
	/**
	 * Mark the required coefficients if the coefficient is in a position (2n,2n+1).
	 *
	 * @param rLevel is the ressolution level
	 * @param y position
	 * @param x position
	 * 
	 */
	private void evenOdd(int rLevel, int y, int x){
		x = x/2;
		y = (y--)/2;
		for(int i = 0; i < filter_5_3_EvenOdd.length - 1; i++){
			shift(rLevel, y + filter_5_3_EvenOdd[i][1], x + filter_5_3_EvenOdd[i][0],  filter_5_3_EvenOdd[i][2]);
		}
		for(int i = filter_5_3_EvenOdd.length - 1; i < filter_5_3_EvenOdd.length; i++){
			if(rLevel == maxRLevel){
				shift(rLevel, y + filter_5_3_EvenOdd[i][1], x + filter_5_3_EvenOdd[i][0], filter_5_3_EvenOdd[i][2]);
			}else{	
				mark(rLevel, y + filter_5_3_EvenOdd[i][1], x + filter_5_3_EvenOdd[i][0]);
			}
		}
		
	}
	
	/**
	 * Mark the required coefficients if the coefficient is in a position (2n+1,2n).
	 *
	 * @param rLevel is the ressolution level
	 * @param y position
	 * @param x position
	 */
	private void oddEven(int rLevel, int y, int x){
		x = (x--)/2;
		y = y/2;
		for(int i = 0; i < filter_5_3_OddEven.length - 1; i++){
			shift(rLevel, y + filter_5_3_OddEven[i][1], x + filter_5_3_OddEven[i][0], filter_5_3_OddEven[i][2]);
		}
		for(int i = filter_5_3_OddEven.length - 1; i < filter_5_3_OddEven.length; i++){
			if(rLevel == maxRLevel){
				shift(rLevel, y + filter_5_3_OddEven[i][1], x + filter_5_3_OddEven[i][0], filter_5_3_OddEven[i][2]);
			}else{	
				mark(rLevel, y + filter_5_3_OddEven[i][1], x + filter_5_3_OddEven[i][0]);
			}
		}
		
	}
	
	/**
	 * Mark the required coefficients if the coefficient is in a position (2n+1,2n+1).
	 *
	 * @param rLevel is the ressolution level
	 * @param y position
	 * @param x position
	 * 
	 */
	private void oddOdd(int rLevel, int y, int x){
		x = (x--)/2;
		y = (y--)/2;
		for(int i = 0; i < filter_5_3_OddOdd.length - 1; i++){
			shift(rLevel, y + filter_5_3_OddOdd[i][1], x + filter_5_3_OddOdd[i][0], filter_5_3_OddOdd[i][2]);
		}
		for(int i = filter_5_3_OddOdd.length - 1; i < filter_5_3_OddOdd.length; i++){
			if(rLevel == maxRLevel){
				shift(rLevel, y + filter_5_3_OddOdd[i][1], x + filter_5_3_OddOdd[i][0], filter_5_3_OddOdd[i][2]);
			}else{	
				mark(rLevel, y + filter_5_3_OddOdd[i][1], x + filter_5_3_OddOdd[i][0]);
			}
		}
		
	}
	
	/**
	 * Mark a coefficient, this allow to know if the coefficient has to be shifted in the next ressolution level.
	 *
	 * @param rLevel ressolution level where belong the x and y postion.
	 * @param y position of the subband and rlevel.
	 * @param x position of the subband and rlevel. 
	 */
	private void mark(int rLevel, int y, int x){
		byte value = 0;	
		//mirror efect
		if(x < 0){
			x = -x;
		}
		if(y < 0){
			y = -y;
		}
		if((y < subbandSizes[rLevel][1][0]) && (x < subbandSizes[rLevel][0][1])){
			value = (byte)(1 << (7-(((y * subbandSizes[rLevel][0][1]) + x) % 8)));
			if((nextRlevelBitMap[rLevel][((y * subbandSizes[rLevel][0][1]) + x) / 8] & value) == 0){
				nextRlevelBitMap[rLevel][((y * subbandSizes[rLevel][0][1]) + x) / 8] |= value;
			}
		}
	}
	
	/**
	 * Check if a coefficient has to be checked to be shifted
	 *
	 * @param rLevel ressolution level where belong the x and y postion.
	 * @param y position of the subband and rlevel.
	 * @param x position of the subband and rlevel.
	 * 
	 * @return a boolean, true is marked to be checked or false if not
	 * 
	 */
	private boolean isMarked(int rLevel, int y, int x){
		rLevel--;
		boolean isMarked = false;
		//mirror efect
		if(x < 0){
			x = -x;
		}
		if(y < 0){
			y = -y;
		}
		if((y >= 0) && (y < subbandSizes[rLevel][1][0]) && (x >= 0) && (x < subbandSizes[rLevel][0][1])){
			if((nextRlevelBitMap[rLevel][((y * subbandSizes[rLevel][0][1]) + x) / 8] & (byte)(1 << (7-(((y * subbandSizes[rLevel][0][1]) + x) % 8)))) != 0){
				isMarked = true;
			}
		}
		return(isMarked);
	}
		
	/**
	 * Check if a component is selected.
	 *
	 * @param c component
	 * 
	 * @return a boolean, true if the component(x) belongs to the components
	 * 			to be decompressed, and false if (x) does not belong to.  
	 */
	private boolean isSelected(int c){
		boolean isRoi = false;
		if( inputComponents[c] ){
			isRoi = true;
		}
		return(isRoi);
	}
	
	/**
	 * Calculate the subband sizes.
	 *
	 * @return a multi-dimensional array with the subband sizes. Indexes mean
	 * 				subbandSizes[rLevel][LH|HL|HH][yBegin|xBegin|yEnd|xEnd]
	 */
	private int[][][] subbandSizesCalculation(){
		
		int[][][] subbandSizes = new int[WTLevels][3][4];
		
		int xSubbandSize = maxComponents;
		int ySubbandSize = 1;

		for(int rLevel = 0; rLevel < WTLevels; rLevel++){

			//Size setting for the level
			int xOdd = xSubbandSize % 2;
			int yOdd = ySubbandSize % 2;
			xSubbandSize = xSubbandSize / 2 + xOdd;
			ySubbandSize = ySubbandSize / 2 + yOdd;
						
			//[rLevel][{LH,HL,HH}][{yBegin,xBegin,yEnd,xEnd}]
			subbandSizes[rLevel][0][0] = 0;
			subbandSizes[rLevel][0][1] = xSubbandSize;
			subbandSizes[rLevel][0][2] = ySubbandSize;
			subbandSizes[rLevel][0][3] = xSubbandSize*2 - xOdd;
			
			subbandSizes[rLevel][1][0] = ySubbandSize;
			subbandSizes[rLevel][1][1] = 0;
			subbandSizes[rLevel][1][2] = ySubbandSize*2 - yOdd;
			subbandSizes[rLevel][1][3] = xSubbandSize;
			
			subbandSizes[rLevel][2][0] = ySubbandSize;
			subbandSizes[rLevel][2][1] = xSubbandSize;
			subbandSizes[rLevel][2][2] = ySubbandSize*2 - yOdd;
			subbandSizes[rLevel][2][3] = xSubbandSize*2 - xOdd;	
		}
		
		return subbandSizes;
	}
	
}