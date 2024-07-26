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
package CADI.Common.LogicalTarget.JPEG2000.Decode;

import CADI.Client.ClientLogicalTarget.JPEG2000.*;
import java.io.PrintStream;

import CADI.Common.Util.CADIPoint;
import GiciException.ErrorException;
import GiciException.WarningException;

/**
 * This class receives image file sizes information and precincts structures and builds new ones without precincts.
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; run<br>
 * &nbsp; [get functions]<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.0 2012/02/12
 */
public class BlockDecode implements Runnable {

  private BlockDecodeState state = null;

  //INTERNAL VARIABLES
  private BPEDecoder bped = null;

  private long inClassIdentifier = -1;

  private CADIPoint blockWithinPrecinct = null;

  private ClientJPEG2KTile tileObj = null;

  private ClientJPEG2KComponent compObj = null;

  private ClientJPEG2KResolutionLevel rLevelObj = null;

  private ClientJPEG2KPrecinct precinctObj = null;
  
  private ClientJPEG2KBlock blockObj = null;

  // ============================= public methods ==============================
  /**
   * 
   * @param codestream
   * @param thread 
   */
  public BlockDecode(BlockDecodeState state) {
    this.state = state;

    bped = new BPEDecoder();
  }

  /**
   * Performs the block building.
   */
  @Override
  public void run() {
    try {
      while (!state.finishedBlocks && !state.error) {
        if (nextBlock()) {
          //precinctObj.lock();
          bped.runAll();
          //precinctObj.unlock(); // TODO: must be in a finally clause
        }
      }
    } catch (ErrorException e) {
      state.error = true;
      System.out.println("RUN ERROR: " + e.getMessage());
    } catch (WarningException e) {
      state.error = true;
      System.out.println("RUN ERROR: " + e.getMessage());
    } catch (Exception e) {
      state.error = true;
      e.printStackTrace();
      state.list(System.out);
      System.out.println("RUN ERROR: " + e.getMessage());
    }
  }

  /**
   * @return imageBlocks definition in {@link #imageBlocks}
   */
  public float[][][][][][][] getImageBlocks() {
    return state.imageBlocks;
  }

  public boolean isError() {
    return state.error;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str += state.toString();
    str += bped.toString();
    str += ", inClassIdentifier=" + inClassIdentifier;
    str += blockWithinPrecinct.toString();

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
    out.println("-- Block Decode --");

    state.list(out);
    bped.list(out);
    out.println("inClassIdentifier: " + inClassIdentifier);
    blockWithinPrecinct.list(out);

    out.flush();

  }

  // ============================ private methods ==============================
  /**
   * Computes the next codeblock to be coded.
   *
   * @param bpe the BPECoder object
   */
  public boolean nextBlock() {

    inClassIdentifier = -1;

    synchronized (state.lock) {
      if (!state.finishedBlocks) {
        //Compute next codeblock to code
        state.xBlock++;
        if (state.xBlock == state.imageBlocks[state.zIndex][state.rLevel][state.subband][state.yBlock].length) {
          state.xBlock = 0;
          state.yBlock++;
          if (state.yBlock == state.imageBlocks[state.zIndex][state.rLevel][state.subband].length) {
            state.yBlock = 0;
            state.subband++;
            if (state.subband == state.imageBlocks[state.zIndex][state.rLevel].length) {
              state.subband = 0;
              state.rLevel++;
              if (state.rLevel == state.imageBlocks[state.zIndex].length) {
                state.rLevel = 0;
                state.zIndex++;
                if (state.zIndex == state.imageBlocks.length) {
                  state.finishedBlocks = true;
                }
              }
            }
          }
        }
      }

      if (!state.finishedBlocks && !state.error) {
        int LLSubband = -1;
        try {

          tileObj = state.codestream.getTile(0);
          compObj = tileObj.getComponent(state.componentIndexes[state.zIndex]);
          rLevelObj = compObj.getResolutionLevel(state.rLevel);

          int WTLevels = compObj.getWTLevels();
          int rLevelSize = 1;
          for (int r = WTLevels - state.discardLevels; r >= state.rLevel && r > 0; r--) {
            rLevelSize <<= 1;
          }
          
          int blockWidth = rLevelObj.getBlockWidth();
          int blockHeight = rLevelObj.getBlockHeight();
          
          int xStartBlockRLevel = (state.roff[0] / rLevelSize) / blockWidth;
          int yStartBlockRLevel = (state.roff[1] / rLevelSize) / blockHeight;
          
          int precinctIndex = rLevelObj.getPrecinctIndex(state.xBlock + xStartBlockRLevel, state.yBlock + yStartBlockRLevel);
          precinctObj = rLevelObj.getPrecinct(precinctIndex);

          if (precinctObj != null) {
            inClassIdentifier = rLevelObj.getInClassIdentifier(precinctIndex);
            blockWithinPrecinct = rLevelObj.getBlockIndexWithinPrecinct(state.xBlock + xStartBlockRLevel, state.yBlock + yStartBlockRLevel);

            LLSubband = state.imageBlocks[state.zIndex][state.rLevel].length == 1 ? 0 : 1;
            
            blockObj = precinctObj.getBlock(state.subband + LLSubband, blockWithinPrecinct.y, blockWithinPrecinct.x);
                        
            bped.swapCodeBlock(blockObj,
                    state.imageBlocks[state.zIndex][state.rLevel][state.subband][state.yBlock][state.xBlock]);

          } else {
            return false;
          }
        } catch (ErrorException e) {
          System.out.println("RUN ERROR: " + e.getMessage());
          e.printStackTrace();
          System.exit(1);
        }
      } else {
        return false;
      }
    }

    return true;
  }
}
