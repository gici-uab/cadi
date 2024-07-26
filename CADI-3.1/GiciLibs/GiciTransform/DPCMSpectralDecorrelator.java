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
 * This class receives an image and transposes it if required.Typically it is used in the following way:<br>
 * constructor(receiveing the image to be transposed)<br>
 * setParameters<br>
 * run<br>
 *  
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 *
 */
public class DPCMSpectralDecorrelator{
	/**
	 * Definition in Coder
	 */
	float[][][] imageSamples = null;
	
	/**
	 * Constructor that receives the original image samples.
	 *
	 * @param imageSamples definition in Coder
	 */
	public DPCMSpectralDecorrelator(float[][][] imageSamples){
		//Image data copy
		this.imageSamples = imageSamples;
	}
	
	/**
	 * Performs the forward DPCM
	 * 
	 * @return the image with the DPCM coefficients in the spectral domain
	 * 
	 * @throws ErrorException when parameters are not set
	 */
	public float[][][] forwardDPCM() throws ErrorException {

		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;

		for(int y=0;y<ySize;y++){
			for(int x=0;x<xSize;x++){
				for(int z=zSize-1;z>0;z--){
					imageSamples[z][y][x] -= imageSamples[z-1][y][x];
				}
			}
		}
		return imageSamples;
	}
	
	/**
	 * Performs the inverse DPCM
	 * 
	 * @return the image with the recovered DPCM coefficients in the spectral domain
	 * 
	 * @throws ErrorException when parameters are not set
	 */
	public float[][][] inverseDPCM() throws ErrorException {
		
		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;
		
		for(int y=0;y<ySize;y++){
			for(int x=0;x<xSize;x++){
				for(int z=1;z<zSize;z++){
					imageSamples[z][y][x] += imageSamples[z-1][y][x];
				}
			}
		}
		return imageSamples;
	}
	
	public float[][][] forwardDPCM(int restartIndex) throws ErrorException {

		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;
		if (restartIndex == 0){
			restartIndex = zSize;
		}
		for(int y=0;y<ySize;y++){
			for(int x=0;x<xSize;x++){
				for(int z=zSize-1;z>0;z--){
					if (z%restartIndex!=0){
						imageSamples[z][y][x] -= imageSamples[z-1][y][x];
					}
				}
			}
		}
		return imageSamples;
	}
	
	public float[][][] inverseDPCM(int restartIndex) throws ErrorException {
		
		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;
		
		if (restartIndex == 0){
			restartIndex = zSize;
		}
		for(int y=0;y<ySize;y++){
			for(int x=0;x<xSize;x++){
				for(int z=1;z<zSize;z++){
					if (z%restartIndex!=0){
						imageSamples[z][y][x] += imageSamples[z-1][y][x];
					}
				}
			}
		}
		return imageSamples;
	}
	
	public float[][][] forwardDC() throws ErrorException {

		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;
		
		float before = 0;
		float current = 0;
		for (int y = 0; y < ySize; y++){
		for (int x = 0; x < xSize; x++){
			current = imageSamples[0][y][x];
		for (int z = 1; z < zSize; z++){
			before = current;
			current = imageSamples[z][y][x];
			imageSamples[z][y][x] = current - before;
			
		}}}
		return imageSamples;
	}

	
	public float[][][] inverseDC() throws ErrorException {

		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;
		
		float before = 0;
		float current = 0;
		for (int y = 0; y < ySize; y++){
		for (int x = 0; x < xSize; x++){
		for (int z = 1; z < zSize; z++){
			before = imageSamples[z-1][y][x];
			current = imageSamples[z][y][x];
			imageSamples[z][y][x] = current + before;
			
		}}}
		return imageSamples;
	}
	
	public float[][][] forwardDifferentialReference(int referenceIndex) throws ErrorException {
		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;
		
		if (referenceIndex<0 || referenceIndex>=zSize){
			throw new ErrorException("Reference index is not is the proper range");
		}
		for(int y=0;y<ySize;y++){
			for(int x=0;x<xSize;x++){
				for(int z=0;z<zSize;z++){
					if (z!=referenceIndex){
						imageSamples[z][y][x] -= imageSamples[referenceIndex][y][x];
					}
				}
			}
		}
		
		return imageSamples;
	}
	
	
	public float[][][] inverseDifferentialReference(int referenceIndex) throws ErrorException {
		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;
		
		if (referenceIndex<0 || referenceIndex>=zSize){
			throw new ErrorException("Reference index is not is the proper range");
		}
		for(int y=0;y<ySize;y++){
			for(int x=0;x<xSize;x++){
				for(int z=0;z<zSize;z++){
					if (z!=referenceIndex){
						imageSamples[z][y][x] += imageSamples[referenceIndex][y][x];
					}
				}
			}
		}
		
		return imageSamples;
	}
}
