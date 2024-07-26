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
package CADI.Common.LogicalTarget.JPEG2000.Detransform;

import java.io.PrintStream;

import CADI.Client.ClientLogicalTarget.JPEG2000.ClientJPEG2KCodestream;
import CADI.Client.ClientLogicalTarget.JPEG2000.ClientJPEG2KComponent;
import CADI.Client.ClientLogicalTarget.JPEG2000.ClientJPEG2KResolutionLevel;
import CADI.Client.ClientLogicalTarget.JPEG2000.ClientJPEG2KTile;
import CADI.Common.Network.JPIP.ViewWindowField;

/**
 * This class receives a multidimensional array of blocks and builds an image.
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; run<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.4 2012/01/26
 */
public class ImageBuild {

  /**
   * 
   */
  private ClientJPEG2KCodestream codestream = null;

  /**
   * Definition in {@link CADI.Client.ClientLogicalTarget.JPEG2000.BlockDecode#imageBlocks}
   */
  private float[][][][][][][] imageBlocks = null;

  /**
   * Definition in {@link CADI.Client.ClientLogicalTarget.JPEG2000.JPEG2KDecoder#imageSamplesInt}
   */
  private float[][][] imageSamples = null;

  /**
   *
   */
  private int[] components = null;

  // INTERNAL ATTRIBUTES
  
  private ClientJPEG2KTile tileObj = null;

  private ClientJPEG2KComponent componentObj = null;

  private ClientJPEG2KResolutionLevel rLevelObj = null;
  
  private int[] fsiz = null;

  private int[] roff = null;

  private int[] rsiz = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   * 
   * @param codestream definition in {@link #codestream}.
   */
  public ImageBuild(ClientJPEG2KCodestream codestream) {
    if (codestream == null)
      throw new NullPointerException();

    this.codestream = codestream;
    this.tileObj = codestream.getTile(0);
  }

  /**
   * 
   * @param imageBlocks definition in {@link #imageBlocks}
   * @param imageSamples definition in {@link #imageSamples}.
   * @param viewWindow definition in {@link #viewWindow}.
   * @param relevantComponents one-dimension array with the relevant components.
   */
  public void run(float[][][][][][][] imageBlocks, float[][][] imageSamples,
          ViewWindowField viewWindow, int[] relevantComponents) {

    // Check input parameters
    if (imageBlocks == null)
      throw new NullPointerException();
    if (viewWindow == null)
      throw new NullPointerException();
    if (relevantComponents == null)
      throw new NullPointerException();

    assert ((viewWindow.roff[0] < viewWindow.fsiz[0])
            && (viewWindow.roff[1] < viewWindow.fsiz[1]));
    assert (((viewWindow.roff[0] + viewWindow.rsiz[0]) <= viewWindow.fsiz[0])
            && ((viewWindow.roff[1] + viewWindow.rsiz[1]) <= viewWindow.fsiz[1]));

    // Copy input parameters
    this.tileObj = codestream.getTile(0);
    this.imageBlocks = imageBlocks;
    this.imageSamples = imageSamples;
    this.components = relevantComponents;

    // Get frame size, offset, and region size
    fsiz = viewWindow.fsiz;
    roff = viewWindow.roff;
    rsiz = viewWindow.rsiz;
    if ((roff[0] < 0) || (roff[1] < 0)) {
      roff = new int[2];
      roff[0] = 0;
      roff[1] = 0;
    }
    if ((rsiz[0] < 0) || (rsiz[1] < 0)) {
      rsiz = fsiz;
    }
    /* System.out.println("fsiz=" + fsiz[0] + "," + fsiz[1]);
     * System.out.println("roff=" + roff[0] + "," + roff[1]);
     * System.out.println("rsiz=" + rsiz[0] + "," + rsiz[1]);
     */
    //System.out.println("xB="+roff[0]+" xE="+(roff[0]+rsiz[0]));
    //System.out.println("yB="+roff[1]+" yE="+(roff[1]+rsiz[1]));


    doImageBuilding();
  }

  /**
   * Returns the {@link #imageSamplesInt} attribute.
   *
   * @return the {@link #imageSamplesInt} attribute.
   */
  public float[][][] getImageSamples() {
    return imageSamples;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str += getClass().getName() + " [";
    for (int z = 0; z < imageSamples.length; z++) {
      for (int y = 0; y < imageSamples[z].length; y++) {
        for (int x = 0; x < imageSamples[z][y].length; x++) {
          str += imageSamples[z][y][x] + ", ";
        }
      }
    }
    str += "]";

    return str;
  }

  /**
   * Prints this Image Build fields out to the
   * specified output stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Image Build --");

    for (int z = 0; z < imageSamples.length; z++) {
      for (int y = 0; y < imageSamples[z].length; y++) {
        for (int x = 0; x < imageSamples[z][y].length; x++) {
          out.print(imageSamples[z][y][x] + " ");
        }
        out.println();
      }
    }

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * Performs the image built.
   */
  private void doImageBuilding() {

    // Memory allocation
    if (imageSamples == null) {
      imageSamples = new float[components.length][rsiz[1]][rsiz[0]];
    } else {
      if ((imageSamples.length != components.length)
          || (imageSamples[0].length != rsiz[1])
          || (imageSamples[0][0].length != rsiz[0])) {
        imageSamples = null;
        imageSamples = new float[components.length][rsiz[1]][rsiz[0]];
      } else {
        // Do nothing.
      }
    }

    //	Samples copy from each block to image
    for (int zIndex = 0; zIndex < components.length; zIndex++) {
      int z = components[zIndex];
      //System.out.println("Component=" + z); // DEBUG

      //Set level sizes
      int xSubbandSize = rsiz[0];
      int ySubbandSize = rsiz[1];
      //System.out.println("\txSubbandSize=" + xSubbandSize + " ySubbandSize=" + ySubbandSize); // DEBUG

      componentObj = tileObj.getComponent(z);

      int rLevelSize = 1;
      //Block division for each WT level
      for (int rLevel = imageBlocks[zIndex].length - 1; rLevel > 0; rLevel--) {
        rLevelSize *= 2;
        //System.out.println("\trLevel=" + rLevel + " (rLevelSize=" + rLevelSize + ")"); // DEBUG

        rLevelObj = componentObj.getResolutionLevel(rLevel);

        int blockWidth = rLevelObj.getBlockWidth();
        int blockHeight = rLevelObj.getBlockHeight();
        //System.out.println("\t\tBlock width=" + blockWidth + " block height=" + blockHeight); // DEBUG

        int xStartInBlock = (roff[0] / rLevelSize) % blockWidth;
        int yStartInBlock = (roff[1] / rLevelSize) % blockHeight;
        //System.out.println("\t\txStartInBlock=" + xStartInBlock + " yStartInBlock=" + yStartInBlock); // DEBUG

        //	Size setting for the level
        int xOdd = xSubbandSize % 2;
        int yOdd = ySubbandSize % 2;
        xSubbandSize = xSubbandSize / 2 + xOdd;
        ySubbandSize = ySubbandSize / 2 + yOdd;
        //System.out.println("\t\txSubbandSize=" + xSubbandSize + " ySubbandSize=" + ySubbandSize); // DEBUG

        // LL HL
        // LH HH
        // HL, LH, HH subband
        int[] xBegin = {xSubbandSize, 0, xSubbandSize};
        int[] yBegin = {0, ySubbandSize, ySubbandSize};
        int[] xEnd = {xSubbandSize * 2 - xOdd, xSubbandSize, xSubbandSize * 2 - xOdd};
        int[] yEnd = {ySubbandSize, ySubbandSize * 2 - yOdd, ySubbandSize * 2 - yOdd};

        //	Block division for each subband
        for (int subband = 0; subband < 3; subband++) {
          //System.out.println("\t\t\tsubband=" + subband); // DEBUG

          int yBlock = 0;
          int yInBlock = yStartInBlock;
          int lenToCopy = -1;
          for (int yImage = yBegin[subband]; yImage < yEnd[subband]; yImage++) {
            int xBlock = 0;
            int xInBlock = xStartInBlock;
            for (int xImage = xBegin[subband]; xImage < xEnd[subband];) {
              //System.out.println("\t\t\t\txBlock=" + xBlock + " yBlock=" + yBlock + " (xInBlock=" + xInBlock + " yInBlock=" + yInBlock + ")");
              //System.out.println("\t\t\t\tyImage="+yImage+" xImage="+xImage+" <- yInBlock="+yInBlock+" xInBlock="+xInBlock);
              if (xInBlock >= imageBlocks[zIndex][rLevel][subband][yBlock][xBlock][yInBlock].length) {
                xInBlock = 0;
                xBlock++;
              }
              lenToCopy = Math.min(xEnd[subband] - xImage,
                      imageBlocks[zIndex][rLevel][subband][yBlock][xBlock][yInBlock].length - xInBlock);
              //System.out.println("\t\t\t\t\tlenToCopy="+lenToCopy+"\n");
              System.arraycopy(
                      imageBlocks[zIndex][rLevel][subband][yBlock][xBlock][yInBlock], xInBlock,
                      imageSamples[zIndex][yImage], xImage,
                      lenToCopy);
              //imageSamples[zIndex][yImage][xImage] = imageBlocks[zIndex][rLevel][subband][yBlock][xBlock][yInBlock][xInBlock];
              //System.out.println("\t\t\t\tyImage="+yImage+" xImage="+xImage+" value="+imageSamplesInt[zIndex][yImage][xImage]+"  <- "+" yInBlock="+yInBlock + " xInBlock+"+xInBlock+" (yBlock="+(yBlock-0)+" xBlock="+(xBlock-0)+")");
              xInBlock += lenToCopy;
              xImage += lenToCopy;
            }
            if (xBlock < imageBlocks[zIndex][rLevel][subband][yBlock].length) {
              yInBlock++;
              if (yInBlock >= imageBlocks[zIndex][rLevel][subband][yBlock][xBlock].length) {
                yInBlock = 0;
                yBlock++;
              }
            }
          }
        }
      }

      //	LL subband samples copy
      //rLevelSize <<= 1;
      //System.out.println("\trLevel=" + "0" + " (rLevelSize=" + rLevelSize + ")"); // DEBUG
      rLevelObj = componentObj.getResolutionLevel(0);

      int blockWidth = rLevelObj.getBlockWidth();
      int blockHeight = rLevelObj.getBlockHeight();
      //System.out.println("\t\tBlock width=" + blockWidth + " block height=" + blockHeight); // DEBUG

      int xStartInBlock = (roff[0] / rLevelSize) % blockWidth;
      int yStartInBlock = (roff[1] / rLevelSize) % blockHeight;
      //int xStartInBlock = (roff[0] / rLevelSize) % (blockSizes[components[zIndex]][0][0][0]);
      //int yStartInBlock = (roff[1] / rLevelSize) % (blockSizes[components[zIndex]][0][1][0]);
      //System.out.println("\t\txStartInBlock=" + xStartInBlock + " yStartInBlock=" + yStartInBlock); // DEBUG
      //System.out.println("\t\txSubbandSize=" + xSubbandSize + " ySubbandSize=" + ySubbandSize); // DEBUG

      //System.out.println("\t\t\tsubband=" + "0"); // DEBUG

      int yBlock = 0;
      int yInBlock = yStartInBlock;
      int lenToCopy = -1;
      for (int yImage = 0; yImage < ySubbandSize; yImage++) {
        int xBlock = 0;
        int xInBlock = xStartInBlock;
        for (int xImage = 0; xImage < xSubbandSize;) {
          //System.out.println("\t\t\t\txBlock=" + xBlock + " yBlock=" + yBlock + " (xInBlock=" + xInBlock + " yInBlock=" + yInBlock + ")");
          // // System.out.println("   yImage="+yImage+" xImage="+xImage+" <- " + "yBlock="+yBlock+" xBlock="+xBlock+" yInBlock="+yInBlock+" xInBlock+"+xInBlock);
          if (xInBlock >= imageBlocks[zIndex][0][0][yBlock][xBlock][yInBlock].length) {
            xInBlock = 0;
            xBlock++;
          }

          lenToCopy = Math.min(xSubbandSize - xImage,
                  imageBlocks[zIndex][0][0][yBlock][xBlock][yInBlock].length - xInBlock);
          System.arraycopy(
                  imageBlocks[zIndex][0][0][yBlock][xBlock][yInBlock], xInBlock,
                  imageSamples[zIndex][yImage], xImage,
                  lenToCopy);
          //imageSamples[zIndex][yImage][xImage] = imageBlocks[zIndex][0][0][yBlock][xBlock][yInBlock][xInBlock];
          //System.out.println("\t\t\t\tyImage="+yImage+" xImage="+xImage+" value="+imageSamplesInt[zIndex][yImage][xImage]+"  <- "+" yInBlock="+yInBlock + " xInBlock+"+xInBlock+" (yBlock="+(yBlock-0)+" xBlock="+(xBlock-0)+")");
          xInBlock += lenToCopy;
          xImage += lenToCopy;
        }
        yInBlock++;
        if (yInBlock >= imageBlocks[zIndex][0][0][yBlock][xBlock].length) {
          yInBlock = 0;
          yBlock++;
        }
      }
    }
  }
}
