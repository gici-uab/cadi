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
import CADI.Common.Network.JPIP.ViewWindowField;
import GiciException.ErrorException;
import GiciException.WarningException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2011/02/10
 */
public class BlockDecodeState {

  /**
   *
   */
  protected ClientJPEG2KCodestream codestream = null;

  /**
   * Is an one-dimensional array with the component indexes.
   */
  protected int[] componentIndexes = null;

  /**
   * This multidimensional array of ints contains the image divided into blocks. The indices are imageBlocks[z][resolutionLevel][subband][yBlock][xBlock] in the next manner:<br>
   * &nbsp; zIndex: is a index on the image components<br>
   * &nbsp; resolutionLevel: 0 is the LL subband, and 1, 2, ... represents next starting with the little one<br>
   * &nbsp; subband: 0 - HL, 1 - LH, 2 - HH (if resolutionLevel == 0 --> 0 - LL)<br>
   * &nbsp; yBlock: block row in the subband<br>
   * &nbsp; xBlock: block column in the subband<br>
   * &nbsp; y: sample row<br>
   * &nbsp; x: sample column<br>
   * <p>
   * Content is the image pixel coefficients.
   */
  protected float[][][][][][][] imageBlocks = null;

  /**
   *
   */
  protected int discardLevels = -1;

  protected int[] fsiz = null;

  protected int[] roff = null;

  protected int[] rsiz = null;

  protected boolean error = false;

  protected final Object lock = new Object();

  protected int zIndex, rLevel, subband, yBlock, xBlock;

  protected boolean finishedBlocks;

  // ============================= public methods ==============================
  /**
   * Constructor.
   * 
   * @param codestream 
   */
  public BlockDecodeState(ClientJPEG2KCodestream codestream) {
    if (codestream == null) throw new NullPointerException();
    this.codestream = codestream;
  }

  /**
   * 
   * @param viewWindow
   * @param componentIndexes
   * @param precinctStreams
   * @param discardLevels
   * @param numThreads
   * @throws ErrorException 
   */
  public void initialize(ViewWindowField viewWindow, int[] componentIndexes,
          int discardLevels) throws ErrorException {

    // Check input parameters
    if (viewWindow == null) throw new NullPointerException();
    if (componentIndexes == null) throw new NullPointerException();
    if (discardLevels < 0) throw new IllegalArgumentException();

    //Data copy
    this.componentIndexes = componentIndexes;
    this.discardLevels = discardLevels;

    //viewWindow.list(System.out); // DEBUG
    assert ((viewWindow.roff[0] < viewWindow.fsiz[0])
            && (viewWindow.roff[1] < viewWindow.fsiz[1]));
    assert (((viewWindow.roff[0] + viewWindow.rsiz[0]) <= viewWindow.fsiz[0])
            && ((viewWindow.roff[1] + viewWindow.rsiz[1]) <= viewWindow.fsiz[1]));
    
    // Get frame size, offset, and region size
    fsiz = Arrays.copyOf(viewWindow.fsiz, 2);
    roff = Arrays.copyOf(viewWindow.roff, 2);
    rsiz = Arrays.copyOf(viewWindow.rsiz, 2);
    if ((roff[0] < 0) || (roff[1] < 0)) {
      roff = new int[2];
      roff[0] = 0;
      roff[1] = 0;
    }
    if ((rsiz[0] < 0) || (rsiz[1] < 0)) {
      rsiz = fsiz;
    }

    try {
      memoryAllocation();
    } catch (WarningException e) {
      throw new ErrorException(e.getMessage());
    }

    // Variables for multhreading processing
    zIndex = 0;
    rLevel = 0;
    subband = 0;
    yBlock = 0;
    xBlock = -1; //So the first time startThread is called gets the first block

    finishedBlocks = false;
  }

  public boolean isError() {
    return error;
  }

  /**
   * @return imageBlocks definition in {@link #imageBlocks}
   */
  public float[][][][][][][] getImageBlocks() {
    return imageBlocks;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";
    
    str += "error=" + error;
    str += ", finishedBlocks=" + finishedBlocks;
    
    str += ", discard levels= " + discardLevels;
    str += ", fsiz=" + fsiz[0] + "," + fsiz[1];
    str += ", roff=" + roff[0] + "," + roff[1];
    str += ", rsiz=" + rsiz[0] + "," + rsiz[1];
    
    str += ", zIndex=" + zIndex;
    str += ", rLevel=" + rLevel;
    str += ", subband=" + subband;
    str += ", yBlock=" + yBlock;
    str += ", xBlock=" + xBlock;

    str = getClass().getName() + " [";
    for (int z = 0; z < imageBlocks.length; z++) {
      for (int r = 0; r < imageBlocks[z].length; r++) {
        for (int s = 0; s < imageBlocks[z][r].length; s++) {
          for (int yb = 0; yb < imageBlocks[z][r][s].length; yb++) {
            for (int xb = 0; xb < imageBlocks[z][r][s][yb].length; xb++) {
              for (int y = 0; y < imageBlocks[z][r][s][yb][xb].length; y++) {
                for (int x = 0; x < imageBlocks[z][r][s][yb][xb][y].length; x++) {
                  str += "z=" + z + " r=" + r + " s=" + s + " yb=" + yb + " xb=" + xb + " y=" + y + " x=" + x + " -> " + imageBlocks[z][r][s][yb][xb][y][x];
                }
              }

            }
          }

        }
      }
    }
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
    out.println("-- Block Decode State --");

    out.println("error: " + error);
    out.println("finishedBlocks: " + finishedBlocks);
    
    out.println("discard levels: " + discardLevels);
    out.println("fsiz: " + fsiz[0] + "," + fsiz[1]);
    out.println("roff: " + roff[0] + "," + roff[1]);
    out.println("rsiz: " + rsiz[0] + "," + rsiz[1]);
    
    out.println("zIndex: " + zIndex);
    out.println("rLevel: " + rLevel);
    out.println("subband: " + subband);
    out.println("yBlock: " + yBlock);
    out.println("xBlock: " + xBlock);
    
    /*for (int z = 0; z < imageBlocks.length; z++) {
      for (int r = 0; r < imageBlocks[z].length; r++) {
        for (int s = 0; s < imageBlocks[z][r].length; s++) {
          for (int yb = 0; yb < imageBlocks[z][r][s].length; yb++) {
            for (int xb = 0; xb < imageBlocks[z][r][s][yb].length; xb++) {
              for (int y = 0; y < imageBlocks[z][r][s][yb][xb].length; y++) {
                for (int x = 0; x < imageBlocks[z][r][s][yb][xb][y].length; x++) {
                  out.println("z=" + z + " r=" + r + " s=" + s + " yb=" + yb + " xb=" + xb + " y=" + y + " x=" + x + " -> " + imageBlocks[z][r][s][yb][xb][y][x]);
                }
              }
            }
          }

        }
      }
    }*/

    out.flush();

  }

  // ============================ private methods ==============================
  /**
   * Allocates the memory for the {@link #imageBlocks} attribue.
   * 
   * @throws ErrorException
   * @throws WarningException 
   */
  private void memoryAllocation() throws ErrorException, WarningException {

    ClientJPEG2KTile tileObj = codestream.getTile(0);
    ClientJPEG2KComponent compObj = null;
    ClientJPEG2KResolutionLevel rLevelObj = null;

    //	Memory allocation (components)
    imageBlocks = new float[componentIndexes.length][][][][][][];
    //Block partitioning for each image component
    for (int zIdx = 0; zIdx < componentIndexes.length; zIdx++) {
      int z = componentIndexes[zIdx];
      compObj = tileObj.getComponent(z);
      //System.out.println("Component=" + z + " (zIdx=" + zIdx + ")"); // DEBUG
      //System.out.println("Component=" + z); // DEBUG

      int WTLevels = compObj.getWTLevels();

      //Memory allocation (resolution levels)
      imageBlocks[zIdx] = new float[WTLevels - discardLevels + 1][][][][][];
      int rLevelSize = 1;

      //Set level sizes
      int xSubbandSize = fsiz[0]; //rsiz[0];
      int ySubbandSize = fsiz[1]; //rsiz[1];
      //System.out.println("\txSubbandSize=" + xSubbandSize + " ySubbandSize=" + ySubbandSize);

      //Block division for each WT level
      for (int r = WTLevels - discardLevels; r > 0; r--) {
        rLevelSize <<= 1;
        //System.out.println("\trLevel=" + r + " (rLevelSize=" + rLevelSize + ")"); // DEBUG
        //System.out.println("\trLevel=" + r); // DEBUG

        rLevelObj = compObj.getResolutionLevel(r);

        //Size setting for the level
        int xOdd = xSubbandSize % 2;
        int yOdd = ySubbandSize % 2;
        xSubbandSize = xSubbandSize / 2 + xOdd;
        ySubbandSize = ySubbandSize / 2 + yOdd;
        //System.out.println("\t\txSubbandSize=" + xSubbandSize + " ySubbandSize=" + ySubbandSize);

        // LL HL
        // LH HH
        //HL, LH, HH subband
        int[] xBegin = {xSubbandSize, 0, xSubbandSize};
        int[] yBegin = {0, ySubbandSize, ySubbandSize};
        int[] xEnd = {xSubbandSize * 2 - xOdd, xSubbandSize, xSubbandSize * 2 - xOdd};
        int[] yEnd = {ySubbandSize, ySubbandSize * 2 - yOdd, ySubbandSize * 2 - yOdd};
        //System.out.println("\t\txBegin=" + xBegin[0] + " xEnd=" + xEnd[0] + " yBegin=" + xBegin[1] + " yEnd=" + xEnd[1]); // DEBUG

        int blockWidth = rLevelObj.getBlockWidth();
        int blockHeight = rLevelObj.getBlockHeight();
        //System.out.println("\t\tBlock width=" + blockWidth + " block height=" + blockHeight); // DEBUG

        //System.out.println("\t\tBlock width=" + blockSizes[componentIndexes[zIdx]][rLevel][0][0] + " block height=" + blockSizes[componentIndexes[zIdx]][rLevel][1][0]);
        int xStartBlockRLevel = (roff[0] / rLevelSize) / blockWidth;
        int yStartBlockRLevel = (roff[1] / rLevelSize) / blockHeight;
        int xEndBlockRLevel = ((roff[0] + rsiz[0] - 1) / rLevelSize) / blockWidth;
        int yEndBlockRLevel = ((roff[1] + rsiz[1] - 1) / rLevelSize) / blockHeight;
        //System.out.println("\t\txStartBlockRLevel=" + xStartBlockRLevel + " xEndBlockRLevel=" + xEndBlockRLevel + " yStartBlockRLevel=" + yStartBlockRLevel + " yEndBlockRLevel=" + yEndBlockRLevel); // DEBUG

        //Memory allocation (subbands)
        imageBlocks[zIdx][r] = new float[3][][][][];

        //int numPrecinctsWidth = rLevelObj.getNumPrecinctsWide();
        //int blocksPerPrecinctWidths = rLevelObj.getBlocksPerPrecinctWidths();
        //int blocksPerPrecinctHeights = rLevelObj.getBlocksPerPrecinctHeights();
        //System.out.println("\t->blocksPerPrecinctWidths=" + blocksPerPrecinctWidths + " blocksPerPrecinctHeights=" + blocksPerPrecinctHeights); //DEBUG


        //Block division for each subband
        for (int sb = 0; sb < 3; sb++) {
          //System.out.println("\t\tsubbanb=" + sb); // DEBUG
          //System.out.println("\t\txBegin=" + xBegin[sb] + " xEnd=" + xEnd[sb] + " yBegin=" + xBegin[sb] + " yEnd=" + xEnd[sb]); // DEBUG

          //Number of blocks set
          int xNumBlocksReal = (int)Math.ceil((xEnd[sb] - xBegin[sb]) / (float)blockWidth);
          int yNumBlocksReal = (int)Math.ceil((yEnd[sb] - yBegin[sb]) / (float)blockHeight);
          //System.out.println("\t\t\txNumBlocks=" + xNumBlocksReal + " yNumBlocks=" + yNumBlocksReal); // DEBUG

          //Memory allocation (number of blocks)
          int yNumBlocksRead = 0;
          if (yStartBlockRLevel >= yNumBlocksReal) {
            yNumBlocksRead = 0;
          } else {
            if (yEndBlockRLevel >= yNumBlocksReal) {
              yNumBlocksRead = yEndBlockRLevel - yStartBlockRLevel;
            } else {
              yNumBlocksRead = yEndBlockRLevel - yStartBlockRLevel + 1;
            }
          }
          //System.out.println("\t\t\tyNumBlocksRead=" + yNumBlocksRead);
          imageBlocks[zIdx][r][sb] = new float[yNumBlocksRead][][][];

          for (int yb = yStartBlockRLevel; yb <= yEndBlockRLevel && yb < yNumBlocksReal; yb++) {
            //System.out.println("\t\t\tyBlock=" + yb);
            //Memory allocation (number of blocks)
            int xNumBlocksRead = 0;
            if (xStartBlockRLevel >= xNumBlocksReal) {
              xNumBlocksRead = 0;
            } else {
              if (xEndBlockRLevel >= xNumBlocksReal) {
                xNumBlocksRead = xEndBlockRLevel - xStartBlockRLevel;
              } else {
                xNumBlocksRead = xEndBlockRLevel - xStartBlockRLevel + 1;
              }
            }
            //System.out.println("\t\t\t\txNumBlocksRead=" + xNumBlocksRead);
            imageBlocks[zIdx][r][sb][yb - yStartBlockRLevel] = new float[xNumBlocksRead][][];

            for (int xb = xStartBlockRLevel; xb <= xEndBlockRLevel && xb < xNumBlocksReal; xb++) {
              //System.out.println("\t\t\t\txBlock=" + xb);
              //System.out.println("\t\t\tyBlock=" + (yb - yStartBlockRLevel) + " xBlock=" + (xb - xStartBlockRLevel)); // DEBUG

              int realWidth =
                      xb < xNumBlocksReal - 1 || (xEnd[sb] - xBegin[sb]) % blockWidth == 0
                      ? blockWidth
                      : (xEnd[sb] - xBegin[sb]) % blockWidth;
              int realHeight =
                      yb < yNumBlocksReal - 1 || (yEnd[sb] - yBegin[sb]) % blockHeight == 0
                      ? blockHeight
                      : (yEnd[sb] - yBegin[sb]) % blockHeight;
              //System.out.println("\t\t\t\t\tReal block sizes=" + realWidth + "x" + realHeight); // DEBUG

              //Memory allocation (block samples)
              imageBlocks[zIdx][r][sb][yb - yStartBlockRLevel][xb - xStartBlockRLevel] = new float[realHeight][realWidth];
            }
          }
        }
      }

      //LL SUBBAND
      ////rLevelSize <<= 1;
      //System.out.println("\trLevel=" + "0" + " (rLevelSize=" + rLevelSize + ")"); // DEBUG
      //System.out.println("\trLevel=" + "0"); // DEBUG
      //System.out.println("\t\txSubbandSize=" + xSubbandSize + " ySubbandSize=" + ySubbandSize);
      imageBlocks[zIdx][0] = new float[1][][][][];

      rLevelObj = compObj.getResolutionLevel(0);
      int blockWidth = rLevelObj.getBlockWidth();
      int blockHeight = rLevelObj.getBlockHeight();
      //System.out.println("\t\tBlock width=" + blockWidth + " block height=" + blockHeight); // DEBUG

      // System.out.println("\t\tBlock width=" + blockSizes[componentIndexes[zIdx]][0][0][0] + " block height=" + blockSizes[componentIndexes[zIdx]][0][1][0]);
      int xStartBlockRLevel = (roff[0] / rLevelSize) / blockWidth;
      int yStartBlockRLevel = (roff[1] / rLevelSize) / blockHeight;
      int xEndBlockRLevel = ((roff[0] + rsiz[0] - 1) / rLevelSize) / blockWidth;
      int yEndBlockRLevel = ((roff[1] + rsiz[1] - 1) / rLevelSize) / blockHeight;
      //System.out.println("\t\txStartBlockRLevel=" + xStartBlockRLevel + " xEndBlockRLevel=" + xEndBlockRLevel + " yStartBlockRLevel=" + yStartBlockRLevel + " yEndBlockRLevel=" + yEndBlockRLevel); // DEBUG

      //int numPrecinctsWidth = rLevelObj.getNumPrecinctsWide();
      //int blocksPerPrecinctWidths = rLevelObj.getBlocksPerPrecinctWidths();
      //int blocksPerPrecinctHeights = rLevelObj.getBlocksPerPrecinctHeights();
      //System.out.println("\t->blocksPerPrecinctWidths=" + blocksPerPrecinctWidths + " blocksPerPrecinctHeights=" + blocksPerPrecinctHeights); //DEBUG
      //System.out.println("\t\tsubband=0"); // DEBUG

      //Number of blocks set
      int xNumBlocksReal = (int)Math.ceil(xSubbandSize / (float)blockWidth);
      int yNumBlocksReal = (int)Math.ceil(ySubbandSize / (float)blockHeight);
      //System.out.println("\t\t\txNumBlocksreal=" + xNumBlocksReal + " yNumBlocksReal=" + yNumBlocksReal); // DEBUG

      //Memory allocation (number of blocks)
      int yNumBlocksRead = 0;
      if (yStartBlockRLevel >= yNumBlocksReal) {
        yNumBlocksRead = 0;
      } else {
        if (yEndBlockRLevel >= yNumBlocksReal) {
          yNumBlocksRead = yEndBlockRLevel - yStartBlockRLevel;
        } else {
          yNumBlocksRead = yEndBlockRLevel - yStartBlockRLevel + 1;
        }
      }
      //System.out.println("\t\t\tyNumBlocksRead=" + yNumBlocksRead);
      imageBlocks[zIdx][0][0] = new float[yNumBlocksRead][][][];


      for (int yb = yStartBlockRLevel; yb <= yEndBlockRLevel; yb++) {
        //System.out.println("\t\t\tyBlock=" + yb);
        //Memory allocation (number of blocks)
        int xNumBlocksRead = 0;
        if (xStartBlockRLevel >= xNumBlocksReal) {
          xNumBlocksRead = 0;
        } else {
          if (xEndBlockRLevel >= xNumBlocksReal) {
            xNumBlocksRead = xEndBlockRLevel - xStartBlockRLevel;
          } else {
            xNumBlocksRead = xEndBlockRLevel - xStartBlockRLevel + 1;
          }
        }
        //System.out.println("\t\t\t\txNumBlocksRead=" + xNumBlocksRead);
        imageBlocks[zIdx][0][0][yb - yStartBlockRLevel] = new float[xNumBlocksRead][][];

        for (int xb = xStartBlockRLevel; xb <= xEndBlockRLevel; xb++) {
          //System.out.println("\t\t\t\txBlock=" + xb); // DEBUG
          //System.out.println("\t\t\tyBlock=" + (yb - yStartBlockRLevel) + " xBlock=" + (xb - xStartBlockRLevel)); // DEBUG

          //Size of block
          int realWidth =
                  xb < xNumBlocksReal - 1 || xSubbandSize % blockWidth == 0
                  ? blockWidth
                  : xSubbandSize % blockWidth;
          int realHeight =
                  yb < yNumBlocksReal - 1 || ySubbandSize % blockHeight == 0
                  ? blockHeight
                  : ySubbandSize % blockHeight;
          //System.out.println("\t\t\t\t\tReal block sizes=" + realWidth + "x" + realHeight); // DEBUG

          //Memory allocation (block samples)
          imageBlocks[zIdx][0][0][yb - yStartBlockRLevel][xb - xStartBlockRLevel] = new float[realHeight][realWidth];
        }
      }
    }
  }
}
