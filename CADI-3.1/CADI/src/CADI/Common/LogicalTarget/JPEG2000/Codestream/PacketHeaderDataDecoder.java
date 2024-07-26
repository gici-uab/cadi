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

import CADI.Common.LogicalTarget.JPEG2000.JPEG2KPrecinct;
import java.io.PrintStream;

/**
 * This class records the necessary information to decode packet headers
 * belonging to a precinct.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2008/11/07
 */
public class PacketHeaderDataDecoder {

  /**
   * Tag Tree where is the first layer which a packet is included
   */
  public TagTreeDecoder[] TTInclusionInformation = null;

  /**
   * Tag Tree with the number of missing most significant bit planes for each codeblock
   */
  public TagTreeDecoder[] TTZeroBitPlanes = null;

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
   * First layer in which the block is included. Indices means: <br>
   *
   * &nbsp; subband: 0 - HL, 1 - LH, 2 - HH (ifresolutionLevel == 0 --> 0 - LL) <br>
   * &nbsp; yBlock: block row in the subband <br>
   * &nbsp; xBlock: block column in the subband <br>
   * <p>
   * Only positive values allowed (0 value is possible too. If 0 --> block is included in first quality layer).
   */
  public int[][][] firstLayer = null;

  /**
   * Is the number of most significant bit planes which are missing. Indices
   * means: <br>
   *
   * &nbsp; subband: 0 - HL, 1 - LH, 2 - HH (ifresolutionLevel == 0 --> 0 - LL) <br>
   * &nbsp; yBlock: block row in the subband <br>
   * &nbsp; xBlock: block column in the subband <br>
   * <p>
   * Only positive values allowed (0 value is possible too.
   */
  public int[][][] zeroBitPlanes = null;

  /**
   * Is an state attribute which indicates the layer to be decoded.
   */
  public int layerToDecode = -1;

  // ============================= public methods ==============================
  /**
   * Constructor.
   * <p>
   * This constructor will be deprecated.
   * See {@link #PacketHeaderDataDecoder(CADI.Common.LogicalTarget.JPEG2000.JPEG2KPrecinct)}.
   *
   * @param subbandStructure contains the structure of the subband. Its
   * 			indexes mean [subband][yBlock][xBlock]
   * @deprecated
   */
  public PacketHeaderDataDecoder(int[][][] subbandStructure) {

    TTInclusionInformation = new TagTreeDecoder[subbandStructure.length];
    TTZeroBitPlanes = new TagTreeDecoder[subbandStructure.length];
    lBlock = new int[subbandStructure.length][][];
    firstLayer = new int[subbandStructure.length][][];
    zeroBitPlanes = new int[subbandStructure.length][][];

    for (int subband = 0; subband < subbandStructure.length; subband++) {
      lBlock[subband] = new int[subbandStructure[subband].length][];
      firstLayer[subband] = new int[subbandStructure[subband].length][];
      zeroBitPlanes[subband] = new int[subbandStructure[subband].length][];

      if (subbandStructure[subband].length > 0) {
        if (subbandStructure[subband][0].length > 0) {
          int numYBlocks = subbandStructure[subband].length;
          int numXBlocks = subbandStructure[subband][0].length;
          TTInclusionInformation[subband] = new TagTreeDecoder(numYBlocks, numXBlocks);
          TTZeroBitPlanes[subband] = new TagTreeDecoder(numYBlocks, numXBlocks);

          for (int yBlock = 0; yBlock < subbandStructure[subband].length; yBlock++) {
            lBlock[subband][yBlock] = new int[subbandStructure[subband][yBlock].length];
            firstLayer[subband][yBlock] = new int[subbandStructure[subband][yBlock].length];
            zeroBitPlanes[subband][yBlock] = new int[subbandStructure[subband][yBlock].length];

            for (int xBlock = 0; xBlock < subbandStructure[subband][yBlock].length; xBlock++) {
              lBlock[subband][yBlock][xBlock] = 0;
              firstLayer[subband][yBlock][xBlock] = 0;
              zeroBitPlanes[subband][yBlock][xBlock] = 0;
            }
          }
        }
      }
    }
  }

  /**
   * Constructor.
   * 
   * @param precinctObj
   */
  public PacketHeaderDataDecoder(JPEG2KPrecinct precinctObj) {

    int numSubbands = precinctObj.getNumSubbands();
    TTInclusionInformation = new TagTreeDecoder[numSubbands];
    TTZeroBitPlanes = new TagTreeDecoder[numSubbands];
    lBlock = new int[numSubbands][][];
    firstLayer = new int[numSubbands][][];
    zeroBitPlanes = new int[numSubbands][][];

    for (int subband = 0; subband < numSubbands; subband++) {
      int numBlocksHigh = precinctObj.getNumBlocksHigh(numSubbands == 1 ? 0 : subband+1);
      int numBlocksWide = precinctObj.getNumBlocksWide(numSubbands == 1 ? 0 : subband+1);
      lBlock[subband] = new int[numBlocksHigh][];
      firstLayer[subband] = new int[numBlocksHigh][];
      zeroBitPlanes[subband] = new int[numBlocksHigh][];

      if (numBlocksHigh > 0) {
        if (numBlocksWide > 0) {
          int numYBlocks = numBlocksHigh;
          int numXBlocks = numBlocksWide;
          TTInclusionInformation[subband] = new TagTreeDecoder(numYBlocks, numXBlocks);
          TTZeroBitPlanes[subband] = new TagTreeDecoder(numYBlocks, numXBlocks);

          for (int yBlock = 0; yBlock < numBlocksHigh; yBlock++) {
            lBlock[subband][yBlock] = new int[numBlocksWide];
            firstLayer[subband][yBlock] = new int[numBlocksWide];
            zeroBitPlanes[subband][yBlock] = new int[numBlocksWide];

            for (int xBlock = 0; xBlock < numBlocksWide; xBlock++) {
              lBlock[subband][yBlock][xBlock] = 0;
              firstLayer[subband][yBlock][xBlock] = 0;
              zeroBitPlanes[subband][yBlock][xBlock] = 0;
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

      if (lBlock[subband].length > 0) {
        if (lBlock[subband][0].length > 0) {

          for (int yBlock = 0; yBlock < lBlock[subband].length; yBlock++) {
            for (int xBlock = 0; xBlock < lBlock[subband][yBlock].length; xBlock++) {
              lBlock[subband][yBlock][xBlock] = 0;
              firstLayer[subband][yBlock][xBlock] = 0;
              zeroBitPlanes[subband][yBlock][xBlock] = 0;
            }
          }
        }
      }
    }

    layerToDecode = -1;
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

    str += "TTZeroBitPlanes=";
    for (int sb = 0; sb < TTZeroBitPlanes.length; sb++) {
      str += TTZeroBitPlanes[sb] + ", ";
    }

    str += "lBlock=";
    for (int sb = 0; sb < lBlock.length; sb++) {
      for (int yBlock = 0; yBlock < lBlock[sb].length; yBlock++) {
        for (int xBlock = 0; xBlock < lBlock[sb][yBlock].length; xBlock++) {
          str += lBlock[sb][yBlock][xBlock] + " ";
        }
      }
    }

    str += "firstLayer=";
    for (int sb = 0; sb < firstLayer.length; sb++) {
      for (int yBlock = 0; yBlock < firstLayer[sb].length; yBlock++) {
        for (int xBlock = 0; xBlock < firstLayer[sb][yBlock].length; xBlock++) {
          str += firstLayer[sb][yBlock][xBlock] + " ";
        }
      }
    }

    str += "zeroBitPlanes=";
    for (int sb = 0; sb < zeroBitPlanes.length; sb++) {
      for (int yBlock = 0; yBlock < zeroBitPlanes[sb].length; yBlock++) {
        for (int xBlock = 0; xBlock < zeroBitPlanes[sb][yBlock].length; xBlock++) {
          str += zeroBitPlanes[sb][yBlock][xBlock] + " ";
        }
      }
    }

    str += "layerToDecode=" + layerToDecode;

    str += "]";
    return str;
  }

  /**
   * Prints this Packet Header Data Decoder out to the specified output
   * stream. This method is useful for debugging.
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
          out.print(lBlock[sb][yBlock][xBlock] + " ");
        }
      }
    }
    out.println();

    out.println("\nfirstLayer: ");
    for (int sb = 0; sb < firstLayer.length; sb++) {
      for (int yBlock = 0; yBlock < firstLayer[sb].length; yBlock++) {
        for (int xBlock = 0; xBlock < firstLayer[sb][yBlock].length; xBlock++) {
          out.print(firstLayer[sb][yBlock][xBlock] + " ");
        }
      }
    }
    out.println();

    out.println("\nzero bit planes: ");
    for (int sb = 0; sb < zeroBitPlanes.length; sb++) {
      for (int yBlock = 0; yBlock < zeroBitPlanes[sb].length; yBlock++) {
        for (int xBlock = 0; xBlock < zeroBitPlanes[sb][yBlock].length; xBlock++) {
          out.print(zeroBitPlanes[sb][yBlock][xBlock] + " ");
        }
      }
    }
    out.println();

    out.println("layerToDecode: " + layerToDecode);


    out.flush();
  }

}
