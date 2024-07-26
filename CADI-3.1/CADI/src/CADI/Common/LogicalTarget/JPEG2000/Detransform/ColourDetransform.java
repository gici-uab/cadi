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
package CADI.Common.LogicalTarget.JPEG2000.Detransform;
import java.io.PrintStream;

import GiciException.*;

/**
 * This class receives a colour image and applies the irreversible colour
 * transform defined in JPEG2000 standard.<br>
 * <p>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; run<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.0 2008/12/13
 */
public class ColourDetransform {

	/**
	 * Definition in {@link CADI.Client.ClientLogicalTarget.JPEG2000.JPEG2KLogicalTarget#imageSamplesFloat}
	 */
	private float[][][] imageSamples = null;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#ySize}
	 */
	private int ySize;

	/**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#xSize}
	 */
	private int xSize;

	/**
	 * If true, a reversible component transformation will be applied.
	 * Otherwise, the component transformation will be irreversible. 
	 */
	private boolean reversible = true;
	
	
	//INTERNAL VARIABLES

	/**
	 * Matrix of inverse ICT
	 * <p>
	 * The values are static.
	 */
	private static float[][] ICT =
		{
			{  1.0F ,  0.0F      ,  1.402F    },
			{  1.0F , -0.344136F , -0.714136F },
			{  1.0F ,  1.772F    ,  0.0F      },
		};

	// ============================= public methods ==============================
	/**
	 * Constructor that receives the original image samples.
	 *
	 * @param imageSamples definition in {@link #imageSamples}
	 * @param reversible definition in {@link #reversible}.
	 */
	public ColourDetransform(float[][][] imageSamples, boolean reversible){
		// Check input parameters
		if (imageSamples == null) throw new NullPointerException();
		
		//Image data copy
		this.imageSamples = imageSamples;
		this.reversible = reversible;

		ySize = imageSamples[0].length;
		xSize = imageSamples[0][0].length;
	}

	/**
	 * Performs the colour detransform and returns the result image.
	 *
	 * @return the colour transformed image
	 *
	 * @throws ErrorException when parameters are not set or unrecognized colour transform is passed
	 */
	public float[][][] run() throws ErrorException{

		if (reversible) { // RCT
			for(int y = 0; y < ySize; y++){
				for(int x = 0; x < xSize; x++){
					float c1 = imageSamples[0][y][x];
					float c2 = imageSamples[1][y][x];
					float c3 = imageSamples[2][y][x];
					imageSamples[1][y][x] = c1 - (float) Math.floor( (c3 + c2) / 4 );
					imageSamples[0][y][x] = c3 + imageSamples[1][y][x];
					imageSamples[2][y][x] = c2 + imageSamples[1][y][x];
				}
			}
		} else { //ICT
			for(int y = 0; y < ySize; y++){
				for(int x = 0; x < xSize; x++){
					float c1 = imageSamples[0][y][x];
					float c2 = imageSamples[1][y][x];
					float c3 = imageSamples[2][y][x];
					for(int z = 0; z < 3; z++){
						imageSamples[z][y][x] = c1 * ICT[z][0] + c2 * ICT[z][1] + c3 * ICT[z][2];
					}
				}
			}
		}
		
		return imageSamples;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {	
		String str = "";
	
		str += "------------------------------------\n";
		str += "      COLOUR DETRANSFORM            \n";
		str += "------------------------------------\n";
	for (int z=0; z<imageSamples.length; z++) {
		for (int y=0; y<imageSamples[z].length; y++) {
			for (int x=0; x<imageSamples[z][y].length; x++) {
				str += imageSamples[z][y][x] + " ";
			}
			str += "\n";
		}
	}
	
		str += "------------------------------------\n";
	
		return str;
	}
	
	/**
	 * Prints this ColourDetransform's fields to the specified output stream.
	 * This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Colour detransform --");		
	
		out.println("Not implemented yet");
		
		out.flush();
	}
}