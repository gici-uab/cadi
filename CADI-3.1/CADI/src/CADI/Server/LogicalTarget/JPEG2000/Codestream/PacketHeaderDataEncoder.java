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
package CADI.Server.LogicalTarget.JPEG2000.Codestream;

import java.io.PrintStream;

import GiciException.ErrorException;

/**
 * This class records precinct-state information to be used by the {@link
 * CADI.Server.LogicalTarget.JPEG2000.Codestream.PacketHeadersEncoder} class to
 * generate the packet headers.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2008/12/08
 */
public class PacketHeaderDataEncoder {

	/**
	 * Tag Tree where is the first layer which a packet is included
	 */
	public TagTreeEncoder[] TTInclusionInformation = null;

	/**
	 * Tag Tree with the number of missing most significant bit planes for each codeblock
	 */
	public TagTreeEncoder[] TTZeroBitPlanes = null;

	/**
	 * Code-block state variable.
	 * <p>
	 * Value 0 means that the packet has not been incluyed in any layer.
	 * <p>
	 * Indexes mean:
	 * &nbsp; 1st subband
	 * &nbsp; 2nd yBlock
	 * &nbsp; 3rd xBlock
	 */
	public int[][][] lBlock = null;

	/**
	 * Indicates the number of the last layer that has already been encoded. 
	 */
	public int lastEncodedLayer = -1;
	
	/**
	 * Is the layer in which a code block is first-time included.
	 */
	public int[][][] firstLayer = null;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param zeroBitPlanes is the same as {@link CADI.Server.LogicalTarget.JPEG2000.JP2LogicalTarget#zeroBitPlanes}
	 * 			but this one only has the indexes [subband][yBlock][xBlock]
	 * @param firstLayer is the first layer in which the code-block is first
	 * 			included. Indexes mean [subband][yBlock][xBlock]
	 */
	public PacketHeaderDataEncoder(int[][][] zeroBitPlanes, int[][][] firstLayer) throws ErrorException {

		this.firstLayer = firstLayer;
		
		TTInclusionInformation = new TagTreeEncoder[zeroBitPlanes.length];
		TTZeroBitPlanes = new TagTreeEncoder[zeroBitPlanes.length];
		lBlock = new int [zeroBitPlanes.length][][];

		for (int subband = 0; subband < zeroBitPlanes.length; subband++) {
			lBlock[subband] = new int [zeroBitPlanes[subband].length][];

			if(zeroBitPlanes[subband].length > 0){
				if(zeroBitPlanes[subband][0].length > 0){

					TTInclusionInformation[subband] = new TagTreeEncoder(firstLayer[subband]);
					TTZeroBitPlanes[subband] = new TagTreeEncoder(zeroBitPlanes[subband]);

					for (int yBlock = 0; yBlock < zeroBitPlanes[subband].length; yBlock++) {
						lBlock[subband][yBlock] = new int [zeroBitPlanes[subband][yBlock].length];

						for ( int xBlock = 0; xBlock < zeroBitPlanes[subband][yBlock].length; xBlock++) {
							lBlock[subband][yBlock][xBlock] = 0;
						}
					}
				}
			}
		}
	}

	/**
	 * Sets the attributes to its initial values.
	 */
	public void reset() {
		for (int subband = 0; subband < lBlock.length; subband++) {
			TTInclusionInformation[subband].reset();
			TTZeroBitPlanes[subband].reset();

			if(lBlock[subband].length > 0){
				if(lBlock[subband][0].length > 0){

					for (int yBlock = 0; yBlock < lBlock[subband].length; yBlock++) {

						for ( int xBlock = 0; xBlock < lBlock[subband][yBlock].length; xBlock++) {
							lBlock[subband][yBlock][xBlock] = 0;
							firstLayer[subband][yBlock][xBlock] = 0;
						}
					}
				}
			}
		}
		
		lastEncodedLayer = -1;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";
		str += "TTInclusionInformation=";
		for (int sb = 0; sb < TTInclusionInformation.length; sb++) {
			str += TTInclusionInformation[sb] + ", ";
		}

		str += " TTZeroBitPlanes=";
		for (int sb = 0; sb < TTZeroBitPlanes.length; sb++) {
			str += TTZeroBitPlanes[sb] + ", ";
		}

		str += " lBlock=";
		for (int sb = 0; sb < lBlock.length; sb++) {
			for (int yBlock = 0; yBlock < lBlock[sb].length; yBlock++) {
				for (int xBlock = 0; xBlock < lBlock[sb][yBlock].length; xBlock++) {
					str += lBlock[sb][yBlock][xBlock] + " ";		
				}
			}
		}

		str += " firstLayer=";
		for (int sb = 0; sb < firstLayer.length; sb++) {
			for (int yBlock = 0; yBlock < firstLayer[sb].length; yBlock++) {
				for (int xBlock = 0; xBlock < firstLayer[sb][yBlock].length; xBlock++) {
					str += firstLayer[sb][yBlock][xBlock] + " ";		
				}
			}
		}
		
		str += " lastEncodedLayer=" + lastEncodedLayer;
		
		str += "]";
		return str;
	}

	/**
	 * Prints this Packet Header Data out to the specified output stream. This
	 * method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Packet Header Data --");

		out.println("TTInclusionInformation: ");
		for (int sb = 0; sb < TTInclusionInformation.length; sb++) {
			out.println(TTInclusionInformation[sb] + " ");
		}

		out.println("\nTTZeroBitPlanes: ");
		for (int sb = 0; sb < TTZeroBitPlanes.length; sb++) {
			out.println(TTZeroBitPlanes[sb] + " ");
		}

		out.println("\nlBlock: ");
		for (int sb = 0; sb < lBlock.length; sb++) {
			for (int yBlock = 0; yBlock < lBlock[sb].length; yBlock++) {
				for (int xBlock = 0; xBlock < lBlock[sb][yBlock].length; xBlock++) {
					out.println(lBlock[sb][yBlock][xBlock] + " ");		
				}
			}
		}
		
		out.println("\nlFirst layer: ");
		for (int sb = 0; sb < firstLayer.length; sb++) {
			for (int yBlock = 0; yBlock < firstLayer[sb].length; yBlock++) {
				for (int xBlock = 0; xBlock < firstLayer[sb][yBlock].length; xBlock++) {
					out.println(firstLayer[sb][yBlock][xBlock] + " ");		
				}
			}
		}

		out.println("\nlastEncodedLayer: " + lastEncodedLayer);
		
		out.flush();
	}

}