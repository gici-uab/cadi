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

import CADI.Common.Cache.PrecinctDataBin;
import java.io.PrintStream;

import CADI.Common.LogicalTarget.JPEG2000.JPEG2KPrecinct;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel;
import CADI.Common.Util.CADIDimension;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2010/11/13
 */
public class ClientJPEG2KPrecinct extends JPEG2KPrecinct {
  
  private ClientJPEG2KBlock[][][] blocks = null;
  
  protected PrecinctDataBin precinctDataBin = null;

  // ============================= public methods ==============================
  /**
   * 
   * @param parent
   * @param index 
   */
  public ClientJPEG2KPrecinct(ClientJPEG2KResolutionLevel parent, int index) {
    super(parent, index);
  }

  /**
   * 
   * @param parent
   * @param index
   * @param precinctDataBin 
   */
  public ClientJPEG2KPrecinct(ClientJPEG2KResolutionLevel parent, int index,
                              PrecinctDataBin precinctDataBin) {
    super(parent, index);
    
    this.precinctDataBin = precinctDataBin;
    
    int rLevel = this.getResolutionLevel();
    blocks = new ClientJPEG2KBlock[getNumSubbands()][][];
    for (int s = 0; s < blocks.length; s++) {
      blocks[s] = new ClientJPEG2KBlock[getNumBlocksHigh(rLevel == 0 ? 0 : s + 1)][getNumBlocksWide(rLevel == 0 ? 0 : s + 1)];
      for (int yb = 0; yb < blocks[s].length; yb++) {
        for (int xb = 0; xb < blocks[s][yb].length; xb++) {
          blocks[s][yb][xb] = new ClientJPEG2KBlock(this, rLevel == 0 ? 0 : s + 1);
        }
      }
    }
        }

  /**
   * 
   * @param zeroBitPlanes 
   */
  public void setZeroBitPlanes(int[][][] zeroBitPlanes) {
    for (int s = 0; s < zeroBitPlanes.length; s++) {
      for (int yb = 0; yb < zeroBitPlanes[s].length; yb++) {
        for (int xb = 0; xb < zeroBitPlanes[s][yb].length; xb++) {
          blocks[s][yb][xb].setZeroBitPlanes(zeroBitPlanes[s][yb][xb]);
        }
      }
    }
  }
  
  /**
   * Returns the block <code>yBlock</code>,<code>xBlock</code> in the subband
   * <code>subband</code>.
   * 
   * @param subband
   * @param yBlock
   * @param xBlock
   * @return 
   */
  public ClientJPEG2KBlock getBlock(int subband, int yBlock, int xBlock) {
     int rLevel = getResolutionLevel();
    if ((rLevel == 0) && (subband != JPEG2KResolutionLevel.LL)) {
      throw new IllegalArgumentException("Res. level 0 has only one subband");
    }
    
    if ((rLevel != 0) && (subband == JPEG2KResolutionLevel.LL)) {
      throw new IllegalArgumentException("LL subband exists only in res. level 0.");
    }
    
    return blocks[rLevel == 0 ? subband : subband - 1][yBlock][xBlock];
  }
  
  /**
   * 
   * @param subband
   * @param yBlock
   * @param xBlock
   * @param offsets
   * @param lengths 
   */
  public void addOffsetsAndLengths(int subband, int yBlock, int xBlock, int[] offsets, int[] lengths) {
    int rLevel = getResolutionLevel();
    if ((rLevel == 0) && (subband != JPEG2KResolutionLevel.LL)) {
      throw new IllegalArgumentException("Res. level 0 has only one subband");
    }
    
    if ((rLevel != 0) && (subband == JPEG2KResolutionLevel.LL)) {
      throw new IllegalArgumentException("LL subband exists only in res. level 0.");
    }
    
    blocks[rLevel == 0 ? 0 : subband - 1][yBlock][xBlock].addOffsetsAndLengths(offsets, lengths);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";
    str += getClass().getName() + " [";
    str += super.toString();
    
    str += "]";
    
    return str;
  }

  /**
   * Prints this Client JPEG2K Precinct out to the specified output stream. This
   * method is useful for debugging.
   * 
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {
    
    out.println("-- Client JPEG2K Precinct --");
    super.list(out);
    precinctDataBin.listData(out);
    if (blocks != null) {
      for (int sb = 0; sb < blocks.length; sb++) {
        if (blocks[sb] != null) {
          for (int yb = 0; yb < blocks[sb].length; yb++) {
            if (blocks[sb][yb] != null) {
              for (int xb = 0; xb < blocks[sb][yb].length; xb++) {
                out.println("sb="+sb+" yb="+yb+" xb="+xb+" data="+blocks[sb][yb][xb].toString());
              }
            }
          }
        }
      }
    }
  }
}
