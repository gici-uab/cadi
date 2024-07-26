/*
 * CADI Software - a JPIP Client/Server framework
 * Copyright (C) 2007-2012 Group on Interactive Coding of Images (GICI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import java.util.ArrayList;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2012/02/15
 */
public class ClientJPEG2KBlock {

  /**
   * 
   */
  private ClientJPEG2KPrecinct parent = null;
  
  /**
   * Allowed values:
   * {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#LL},
   * {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#LH},
   * {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#HL},
   * {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#HH}.
   */
  private int subband = -1;
  
  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Codestream.PacketHeaderDataDecoder#zeroBitPlanes}.
   */
  private int zeroBitPlanes = -1;

  /**
   * 
   */
  private ArrayList<int[]> offsets = null;

  /**
   * 
   */
  private ArrayList<int[]> lengths = null;

  // INTERNAL ATTRIBUTES
  
  /**
   * 
   */
  private int numCodingPasses = 0;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public ClientJPEG2KBlock(ClientJPEG2KPrecinct parent, int subband) {
    this.parent = parent;
    this.subband = subband;
    
    offsets = new ArrayList<int[]>();
    lengths = new ArrayList<int[]>();
  }
  
  /**
   * 
   */
  public ClientJPEG2KPrecinct getParent() {
    return parent;
  }
  
  /**
   * Returns the subband.
   * 
   * @return 
   */
  public int getSubband() {
    return subband;
  }

  /**
   * 
   * @param zeroBitPlanes 
   */
  public void setZeroBitPlanes(int zeroBitPlanes) {
    this.zeroBitPlanes = zeroBitPlanes;
  }

  /**
   * 
   * @return 
   */
  public int getZeroBitPlanes() {
    return zeroBitPlanes;
  }

  /**
   * Definition in {@link #numCodingPasses}.
   * 
   * @return 
   */
  public int getNumCodingPasses() {
    return numCodingPasses;
  }

  /**
   * 
   * @param offsets
   * @param lengths 
   */
  public void addOffsetsAndLengths(int[] offsets, int[] lengths) {
    assert (offsets.length == lengths.length);
    this.offsets.add(offsets);
    this.lengths.add(lengths);
    numCodingPasses += offsets.length;
  }

  /**
   * 
   * @param codingPass
   * @return 
   */
  public int getLength(int codingPass) {
    assert (codingPass <= numCodingPasses);

    int i = 0;
    while ((i < lengths.size()) && (codingPass >= lengths.get(i).length)) {
      codingPass -= lengths.get(i).length;
      i++;
    }
    return lengths.get(i)[codingPass];
  }

  /**
   * 
   * @param codingPass
   * @return 
   */
  public int getOffset(int codingPass) {
    assert (codingPass <= numCodingPasses);

    int i = 0;
    while ((i < offsets.size()) && (codingPass >= offsets.get(i).length)) {
      codingPass -= offsets.get(i).length;
      i++;
    }
    return offsets.get(i)[codingPass];
  }
  
  /**
   * 
   * @return 
   */
  public CPByteStream[] getPrecinctStream() {
    CPByteStream[] CPStreams = null;
    
    if (getZeroBitPlanes() >= 0) {
      CPStreams = new CPByteStream[getNumCodingPasses()];
      for (int cp = 0; cp < CPStreams.length; cp++) {
        CPStreams[cp] = new CPByteStream(parent.precinctDataBin, 
                getOffset(cp), getLength(cp));
      }
    }
    
    return CPStreams;
  }
  
  /**
   * 
   * @param cp
   * @return 
   */
  public CPByteStream getPrecinctStream(int cp) {
    if (cp > numCodingPasses) 
      throw new IllegalArgumentException("Coding pass greater than allowed.");
    
    CPByteStream CPStreams = null;
    if (getZeroBitPlanes() >= 0) {
      CPStreams = new CPByteStream(parent.precinctDataBin,  getOffset(cp), getLength(cp));
    }
    
     return CPStreams;
  }
  
  /**
   * Returns the most significant bit plane of the block.
   * 
   * @return 
   */
  public int getMostSignificantBitPlane() {
    int MSBPlane = parent.getComponentObj().getGuardBits()
                    + parent.getResolutionLevelObj().getExponent(subband) - 2
                    - getZeroBitPlanes();
    
    return MSBPlane;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str += "zeroBitPlanes=" + zeroBitPlanes;
    if (offsets != null) {
      str += ", offsets={";
      for (int[] offs : offsets) {
        for (int off : offs) {
          str += off + " ";
        }
      }
      str += "}";
    }
    
    if (lengths != null) {
      str += ", lengths={";
      for (int[] lens : offsets) {
        for (int len : lens) {
          str += len + " ";
        }
      }
      str += "}";
    }
    
    str += ", numCodingPasses="+numCodingPasses;

    str += "]";

    return str;
  }

  /**
   * Prints this Block Decode out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {
    out.println("-- Client JPEG2K Block --");

     out.println("zeroBitPlanes=" + zeroBitPlanes);
     
    if (offsets != null) {
      out.print("offsets={");
      for (int[] offs : offsets) {
        for (int off : offs) {
          out.print(off + " ");
        }
      }
      out.println("}");
    }
    
    if (lengths != null) {
      out.print("lengths={");
      for (int[] lens : offsets) {
        for (int len : lens) {
          out.print(len + " ");
        }
      }
      out.println("}");
    }
    
    out.println("numCodingPasses="+numCodingPasses);

    out.flush();

  }
}
