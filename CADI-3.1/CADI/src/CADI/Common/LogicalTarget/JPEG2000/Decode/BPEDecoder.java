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

import CADI.Client.ClientLogicalTarget.JPEG2000.CPByteStream;
import CADI.Client.ClientLogicalTarget.JPEG2000.ClientJPEG2KBlock;
import GiciException.*;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * This class receives an array of ByteStreams belonging to a code-block and decodes them obtaining the original image samples. Typically it is used in the following way:<br>
 * constructor(receiveing the bytestreams)<br>
 * runAll<br>
 * getBlockSamples<br>
 *
 * You can also use this class to increasingly decode the coding passes: you only have to use the decodeNextCodingPass function as times as coding passes contains the code-block.<br>
 * ATTENTION: In the framework of the JPEG2000 many different code-blocks have to be encoded. It is more efficient to create only one object of this class and change the code-block:<br>
 * constructor(void)<br>
 * swapCodeBlock<br>
 * runAll<br>
 * getBlockSamples<br>
 * swapCodeBlock<br>
 * runAll<br>
 * getBlockSamples<br>
 * ...<br>
 * (of course, the function runAll can be substituted for multiple call of the decodeNextCodingPass function) If this class is used in this last way it is easy to incorporate a multithreading support for the decoding of code-blocks.<br>
 *
 * Since version 1.3 this class uses an optimized way to calculate coefficient contexts. Although it achieves good results in terms of computational complexity, the source code is not very understandable (when computing the coefficient contexts). File BPEDecoder.java.1_2 contains the 1.2 version of this class, that has a more understandable source code. Apart of this, both classes are exactly equal.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.3 2012/01/21
 */
public class BPEDecoder {

  /**
   * 
   */
  private ClientJPEG2KBlock blockObj = null;
  
  /**
   * Definition in {@link Pyrenees.Coder.Code.BPECoder#blockSamples}
   */
  private float[][] blockSamples = null;
  
  private CPByteStream CPByteStreams = null;

  /**
   * Definition in {@link Pyrenees.Coder.Code.BPECoder#ySize}
   */
  private int ySize = -1;

  /**
   * Definition in {@link Pyrenees.Coder.Code.BPECoder#xSize}
   */
  private int xSize = -1;

  /**
   * Definition in {@link Pyrenees.Coder.Code.BPECoder#subband}
   */
  private int subband = -1;

  /**
   * Definition in {@link Pyrenees.Coder.Code.BPECoder#MSBPlane}
   */
  private int MSBPlane = -1;

  /**
   * Definition in {@link Pyrenees.Coder.Code.BPECoder#contextMap}
   */
  private byte[][] contextMap = null;

  /**
   * Definition in {@link Pyrenees.Coder.Code.BPECoder#currentBitPlane}
   */
  private int currentBitPlane = -1;

  /**
   * Definition in {@link Pyrenees.Coder.Code.BPECoder#currentCodingPass}
   */
  private int currentCodingPass = -1;

  /**
   * Definition in {@link Pyrenees.Coder.Code.BPECoder#currentMaskBitPlane}
   */
  private int currentMaskBitPlane = 0;

  /**
   * Mask of the currentBitPlane - 1.
   * <p>
   * The content is 2^(currentBitPlane-1)
   */
  private int maskBitPlaneBefore = 0;

  /**
   * Decoder to read from the inputByteStream.
   * <p>
   * This Decoder must be compatible with the abstract class Decoder.
   */
  private Decoder blockDecoder = null;

  /**
   * Definition in {@link Pyrenees.Coder.Code.BPECoder#stripeHeight}
   */
  private final int stripeHeight = 4;

  /**
   * Definition in {@link Pyrenees.Coder.Code.BPECoder#numBitsRunModeBreak}
   */
  private final int numBitsRunModeBreak = (int) Math.ceil(Math.log(stripeHeight) / Math.log(2));

  /**
   * 
   */
  private int numBitPlanes = -1;
  
  /**
   * 
   */
  private int numCodingPasses = -1;

  // ============================= public methods ==============================
  /**
   * Constructor that does not receive anything. Before use the functions of decodeNextCodingPass the function swapCodeBlock must be used.
   */
  public BPEDecoder() {
    blockDecoder = new MQDecoder();
  }

  /**
   * Constructor that receives block coded bit streams, block sizes, most significant bit plane and block subband.  This information is absolutely necessary to decode bitstream.
   * 
   * @param blockObj definition in {@link #blockObj}.
   * @param blockSamples definition in {@link #blockSamples}.
   * 
   * @throws ErrorException problems with the inputByteStream
   */
  public BPEDecoder(ClientJPEG2KBlock blockObj, float[][] blockSamples) throws ErrorException {
    blockDecoder = new MQDecoder();
    swapCodeBlock(blockObj, blockSamples);
  }

  /**
   * Swaps the code-block decoded by this class. Some initializations performed.
   * 
   * @param blockObj definition in {@link #blockObj}.
   * @param blockSamples definition in {@link #blockSamples}.
   * 
   * @throws ErrorException problems with the inputByteStream
   */
  public void swapCodeBlock(ClientJPEG2KBlock blockObj, float[][] blockSamples) throws ErrorException {
    
        //Data copy
    this.blockObj = blockObj;
    this.blockSamples = blockSamples;
    
    //CDByteStreams = blockObj.getPrecinctStream();
    subband = blockObj.getSubband();
    ySize = blockSamples.length;
    xSize = blockSamples[0].length;
    numCodingPasses = blockObj.getNumCodingPasses();
    MSBPlane = blockObj.getMostSignificantBitPlane();
    numBitPlanes = (int) Math.ceil(1.0 * (numCodingPasses + 2) / 3F);
    
    //Some checks
    if (MSBPlane < 0) {
      throw new ErrorException("MSBPlane cannot be negative.");
    }
    if (MSBPlane + 1 < numBitPlanes) {
      throw new ErrorException("MSBPlane less than bit plane byte streams.");
    }

    //Memory allocation
    if ((contextMap == null) || (contextMap.length != ySize) || (contextMap[0].length != xSize)) {
      contextMap = new byte[ySize][xSize];
    } else {
      for (int i = 0; i < ySize; i++) {
        Arrays.fill(contextMap[i], (byte) 0);
      }
    }

    //Set blockDecoder (we do not create another object, we reutilize the already created decoder)
    blockDecoder.reset();

    //Initialize current variables
    currentBitPlane = this.MSBPlane;
    currentCodingPass = 2; //CP
    currentMaskBitPlane = ((int) 1 << currentBitPlane); //2^bitPlane
    if (currentMaskBitPlane > 1) {
      maskBitPlaneBefore = (currentMaskBitPlane >>> 1);
    } else {
      maskBitPlaneBefore = 0;
    }
  }

  /**
   * Decode the image samples (all bit planes).
   *
   * @throws WarningException when reached the last coding pass
   * @throws ErrorException problems with the inputByteStream
   */
  public void runAll() throws WarningException, ErrorException {
    while ((currentBitPlane >= 0) && (MSBPlane - currentBitPlane < numBitPlanes)) {
      decodeNextCodingPass();
    }
  }

  /**
   * Decodes the next coding pass.
   *
   * @throws WarningException when reached the last coding pass
   * @throws ErrorException problems with the inputByteStream
   */
  public void decodeNextCodingPass() throws WarningException, ErrorException {
    if (currentBitPlane < 0) {
      throw new WarningException("Reached last bit plane.");
    }
    if (MSBPlane - currentBitPlane >= numBitPlanes) {
      throw new WarningException("Reached last ByteStream.");
    }

    int codingLevel = (MSBPlane - currentBitPlane) * 3 + currentCodingPass - 2;
    if (numCodingPasses > codingLevel) {
      CPByteStreams = blockObj.getPrecinctStream(codingLevel);
      if (CPByteStreams != null) {
        //Change the inputByteStream of the blockDecoder
        blockDecoder.swapInputByteStream(CPByteStreams);
        blockDecoder.restart();

        //Decodes the current coding pass
        switch (currentCodingPass) {
          case 0: //SPP
            significancePropagationPass();
            break;
          case 1: //MRP
            magnitudeRefinementPass();
            break;
          case 2: //CP
            cleanupPass();
            break;
        }
      }
    }

    //Prepares the current variables for the next coding pass
    if (currentCodingPass == 2) {
      currentCodingPass = 0;
      currentBitPlane--;
      currentMaskBitPlane = ((int) 1 << currentBitPlane); //2^bitPlane
      if (currentMaskBitPlane > 1) {
        maskBitPlaneBefore = (currentMaskBitPlane >>> 1);
      } else {
        maskBitPlaneBefore = 0;
      }
    } else {
      currentCodingPass++;
    }
  }

  /**
   * @return blockSamples definition in {@link #blockSamples}
   */
  public float[][] getBlockSamples() {
    return blockSamples;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";
    str += getClass().getName() + " [";

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
    out.println("-- BPE Decoder --");

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * Same function as {@link Pyrenees.Coder.Code.BPECoder#signEncode}
   *
   * @throws ErrorException when end of bitstream is reached
   */
  private void signDecode(int x, int y) throws ErrorException {
    int up = y - 1 < 0 ? 0 : contextMap[y - 1][x] >= -8 && contextMap[y - 1][x] <= 8 ? 0 : blockSamples[y - 1][x] < 0 ? -1 : 1;
    int down = y + 1 >= ySize ? 0 : contextMap[y + 1][x] >= -8 && contextMap[y + 1][x] <= 8 ? 0 : blockSamples[y + 1][x] < 0 ? -1 : 1;
    int left = x - 1 < 0 ? 0 : contextMap[y][x - 1] >= -8 && contextMap[y][x - 1] <= 8 ? 0 : blockSamples[y][x - 1] < 0 ? -1 : 1;
    int right = x + 1 >= xSize ? 0 : contextMap[y][x + 1] >= -8 && contextMap[y][x + 1] <= 8 ? 0 : blockSamples[y][x + 1] < 0 ? -1 : 1;

    int v = up + down == 0 ? 0 : up + down < 0 ? -1 : 1;
    int h = left + right == 0 ? 0 : left + right < 0 ? -1 : 1;

    int signContext = 0;
    switch (h) {
      case -1:
        switch (v) {
          case -1:
            signContext = -14;
            break;
          case 0:
            signContext = -13;
            break;
          case 1:
            signContext = -12;
            break;
        }
        break;
      case 0:
        switch (v) {
          case -1:
            signContext = -11;
            break;
          case 0:
            signContext = 10;
            break;
          case 1:
            signContext = 11;
            break;
        }
        break;
      case 1:
        switch (v) {
          case -1:
            signContext = 12;
            break;
          case 0:
            signContext = 13;
            break;
          case 1:
            signContext = 14;
            break;
        }
        break;
    }

    boolean sign = blockDecoder.decodeBit(Math.abs(signContext));
    if (signContext > 0 == sign) {
      blockSamples[y][x] = -blockSamples[y][x];
    }
  }

  /**
   * Same function as {@link Pyrenees.Coder.Code.BPECoder#significancePropagationPass}
   * Minor changes due to differences between coding and decoding.
   *
   * @throws ErrorException when end of bitstream reached
   */
  private void significancePropagationPass() throws ErrorException {
    //First stripe boundaries
    int yBegin = 0;
    int yEnd = stripeHeight < ySize ? stripeHeight : ySize;
    //Stripes
    boolean decodedBit = false;
    do {
      for (int x = 0; x < xSize; x++) {
        for (int y = yBegin; y < yEnd; y++) {
          //Sample decoding
          //contextMap shows the samples that we must visit (only these ones that have significant neighbourhoods)
          if ((contextMap[y][x] > 0) && (contextMap[y][x] <= 8)) {
            decodedBit = blockDecoder.decodeBit(contextMap[y][x]);

            if (decodedBit) {
              blockSamples[y][x] = (float) (currentMaskBitPlane | maskBitPlaneBefore);
              signDecode(x, y);

              //If the sample is significant we have to update contextMap to indicate that the neighbouhoods in the scan must be visited. We will also update the context of the current coefficient.
              updateMap(x, y, false);
            }
          }
        }
      }
      //Next stripe boundaries
      yBegin = yEnd;
      yEnd = yBegin + stripeHeight;
      //Reaching the block boundary
      if (yEnd > ySize) {
        yEnd = ySize;
      }
    } while (yBegin < ySize);
  }

  /**
   * Same function as {@link Pyrenees.Coder.Code.BPECoder#magnitudeRefinementPass}
   * Minor changes due to differences between coding and decoding.
   *
   * @throws ErrorException when end of bitstream reached
   */
  private void magnitudeRefinementPass() throws ErrorException {
    //First stripe boundaries
    int yBegin = 0;
    int yEnd = stripeHeight < ySize ? stripeHeight : ySize;
    //Stripes
    boolean decodedBit = false;
    do {
      for (int x = 0; x < xSize; x++) {
        for (int y = yBegin; y < yEnd; y++) {
          //The coefficient has been significant in the previous bit plane
          if (Math.abs(contextMap[y][x]) >= 15) {
            if (contextMap[y][x] > 0) {
              decodedBit = blockDecoder.decodeBit(contextMap[y][x]);
              if (decodedBit) {
                if (blockSamples[y][x] < 0) {
                  blockSamples[y][x] = -((Math.abs((int) blockSamples[y][x]) | currentMaskBitPlane) | maskBitPlaneBefore);
                } else {
                  blockSamples[y][x] = (((int) blockSamples[y][x] | currentMaskBitPlane) | maskBitPlaneBefore);
                }
              } else {
                if (blockSamples[y][x] < 0) {
                  blockSamples[y][x] = -((Math.abs((int) blockSamples[y][x]) & ~currentMaskBitPlane) | maskBitPlaneBefore);

                } else {
                  blockSamples[y][x] = (((int) blockSamples[y][x] & ~currentMaskBitPlane) | maskBitPlaneBefore);
                }
              }
              //System.out.print((decodedBit ? "1(" : "0(") + context + ")");
              contextMap[y][x] = 17;

            } else {
              //The coefficient is significant in the current bit plane
              contextMap[y][x] = (byte) -contextMap[y][x];
            }
          }
        }
      }
      //Next stripe boundaries
      yBegin = yEnd;
      yEnd = yBegin + stripeHeight;
      //Reaching the block boundary
      if (yEnd > ySize) {
        yEnd = ySize;
      }
    } while (yBegin < ySize);
  }

  /**
   * Same function as {@link Pyrenees.Coder.Code.BPECoder#cleanupPass}
   * Minor changes due to differences between coding and decoding.
   *
   * @throws ErrorException when end of bitstream reached
   */
  private void cleanupPass() throws ErrorException {
    //First stripe boundaries
    int yBegin = 0;
    int yEnd = stripeHeight < ySize ? stripeHeight : ySize;
    //Stripes
    boolean runMode = true;
    boolean decodedBit = false;
    do {
      for (int x = 0; x < xSize; x++) {
        //Test if we can code the column in run mode
        runMode = true;
        if (yEnd - yBegin == stripeHeight) {
          //Check if exists a sample with some significant neighbourhood or the column should not be completely scanned. If one of these conditions is true then the run mode cannot be started
          int y = yBegin;
          do {
            if (contextMap[y][x] != 0) {
              runMode = false;
            }
            y++;
          } while ((runMode) && (y < yEnd));
        } else {
          runMode = false;
        }

        int y = yBegin;
        //If we can start the run mode, we check if the column has any significant coefficient
        if (runMode) {
          decodedBit = blockDecoder.decodeBit(9);
          //System.out.print((decodedBit ? "1(" : "0(") + "9)");
          if (decodedBit) {
            int significantCoefficient = 0;
            for (int bitRunModeBreak = numBitsRunModeBreak - 1; bitRunModeBreak >= 0; bitRunModeBreak--) {
              significantCoefficient |= ((blockDecoder.decodeBit(18) ? 1 : 0) << bitRunModeBreak);
              //System.out.print("X(18)");
            }
            y += significantCoefficient;
            blockSamples[y][x] = currentMaskBitPlane | maskBitPlaneBefore;
            //if(this.currentBitPlane == 1)blockSamples[y][x] -= 0.5F;
            signDecode(x, y);

            //If the sample is significant we have to update contextMap to indicate that the neighbouhoods in the scan must be visited. We will also update the context of the current coefficient.
            updateMap(x, y, true);
          } else {
            y = yEnd;
          }
        }

        //Decode each coefficient
        while (y < yEnd) {
          //contextMap shows the samples that we must visit (only these ones that are not visited yet in the current bit plane)
          if ((contextMap[y][x] >= -8) && (contextMap[y][x] <= 0)) {
            //In the next bit plane this coefficient must be visitied by the SPP
            contextMap[y][x] = (byte) -contextMap[y][x];
            decodedBit = blockDecoder.decodeBit(contextMap[y][x]);
            //System.out.print((decodedBit ? "1(" : "0(") + contextSearch(x, y, 0) + ")");

            if (decodedBit) {
              blockSamples[y][x] = currentMaskBitPlane | maskBitPlaneBefore;
              //if(this.currentBitPlane == 1)blockSamples[y][x] -= 0.5F;
              signDecode(x, y);

              //If the sample is significant we have to update contextMap to indicate that the neighbouhoods in the scan must be visited. We will also update the context of the current coefficient.
              updateMap(x, y, true);
            }
          }
          y++;
        }
      }
      //Next stripe boundaries
      yBegin = yEnd;
      yEnd = yBegin + stripeHeight;
      //Reaching the block boundary
      if (yEnd > ySize) {
        yEnd = ySize;
      }
    } while (yBegin < ySize);
  }

  /**
   * Turn neighbourhoods map samples of (x,y) from 0 to 1 if they can be visited in the current scan.
   *
   * @param x x position of the sample
   * @param y y position of the sample
   * @param CP indicates if the updating is performed by the Cleanup Pass (true, otherwise false)
   */
  private void updateMap(int x, int y, boolean CP) {
    //Context of the current coefficient
    int yNeighBegin = y - 1 >= 0 ? y - 1 : 0;
    int yNeighEnd = y + 1 < ySize ? y + 1 : y;
    int xNeighBegin = x - 1 >= 0 ? x - 1 : 0;
    int xNeighEnd = x + 1 < xSize ? x + 1 : x;
    boolean significantNeighs = false;
    for (int yNeigh = yNeighBegin; yNeigh <= yNeighEnd; yNeigh++) {
      for (int xNeigh = xNeighBegin; xNeigh <= xNeighEnd; xNeigh++) {
        if (Math.abs(contextMap[yNeigh][xNeigh]) >= 15) {
          if (Math.abs(contextMap[yNeigh][xNeigh]) == 15) {
            contextMap[yNeigh][xNeigh] = contextMap[yNeigh][xNeigh] < 0 ? (byte) -16 : (byte) 16;
          }
          significantNeighs = true;
        }
      }
    }
    if (significantNeighs) {
      contextMap[y][x] = CP ? (byte) 16 : (byte) -16;
    } else {
      contextMap[y][x] = CP ? (byte) 15 : (byte) -15;
    }

    //Context of the neighboors
    switch (subband) {
      case 0: //SUBBAND LL
      case 2: //SUBBAND LH
        if (x - 1 >= 0) {
          //LEFT
          switch (Math.abs(contextMap[y][x - 1])) {
            case 0:
              contextMap[y][x - 1] = CP ? (byte) 5 : (byte) -5;
              break;
            case 1:
            case 2:
              contextMap[y][x - 1] = contextMap[y][x - 1] < 0 ? (byte) -6 : (byte) 6;
              break;
            case 3:
            case 4:
              contextMap[y][x - 1] = contextMap[y][x - 1] < 0 ? (byte) -7 : (byte) 7;
              break;
            case 5:
            case 6:
            case 7:
              contextMap[y][x - 1] = contextMap[y][x - 1] < 0 ? (byte) -8 : (byte) 8;
              break;
          }
          if (y - 1 >= 0) {
            //UP-LEFT
            switch (Math.abs(contextMap[y - 1][x - 1])) {
              case 0:
                contextMap[y - 1][x - 1] = CP ? (byte) 1 : (byte) -1;
                break;
              case 1:
                contextMap[y - 1][x - 1] = contextMap[y - 1][x - 1] < 0 ? (byte) -2 : (byte) 2;
                break;
              case 5:
                contextMap[y - 1][x - 1] = contextMap[y - 1][x - 1] < 0 ? (byte) -6 : (byte) 6;
                break;
            }
          }
          if (y + 1 < ySize) {
            //DOWN-LEFT
            switch (Math.abs(contextMap[y + 1][x - 1])) {
              case 0:
                if (CP) {
                  contextMap[y + 1][x - 1] = (y + 1) % stripeHeight != 0 ? (byte) 1 : (byte) -1;
                } else {
                  contextMap[y + 1][x - 1] = (y + 1) % stripeHeight != 0 ? (byte) -1 : (byte) 1;
                }
                break;
              case 1:
                contextMap[y + 1][x - 1] = contextMap[y + 1][x - 1] < 0 ? (byte) -2 : (byte) 2;
                break;
              case 5:
                contextMap[y + 1][x - 1] = contextMap[y + 1][x - 1] < 0 ? (byte) -6 : (byte) 6;
                break;
            }
          }
        }
        if (x + 1 < xSize) {
          //RIGHT
          switch (Math.abs(contextMap[y][x + 1])) {
            case 0:
              contextMap[y][x + 1] = CP ? (byte) -5 : (byte) 5;
              break;
            case 1:
            case 2:
              contextMap[y][x + 1] = contextMap[y][x + 1] < 0 ? (byte) -6 : (byte) 6;
              break;
            case 3:
            case 4:
              contextMap[y][x + 1] = contextMap[y][x + 1] < 0 ? (byte) -7 : (byte) 7;
              break;
            case 5:
            case 6:
            case 7:
              contextMap[y][x + 1] = contextMap[y][x + 1] < 0 ? (byte) -8 : (byte) 8;
              break;
          }
          if (y - 1 >= 0) {
            //UP-RIGHT
            switch (Math.abs(contextMap[y - 1][x + 1])) {
              case 0:
                if (CP) {
                  contextMap[y - 1][x + 1] = y % stripeHeight != 0 ? (byte) -1 : (byte) 1;
                } else {
                  contextMap[y - 1][x + 1] = y % stripeHeight != 0 ? (byte) 1 : (byte) -1;
                }
                break;
              case 1:
                contextMap[y - 1][x + 1] = contextMap[y - 1][x + 1] < 0 ? (byte) -2 : (byte) 2;
                break;
              case 5:
                contextMap[y - 1][x + 1] = contextMap[y - 1][x + 1] < 0 ? (byte) -6 : (byte) 6;
                break;
            }
          }
          if (y + 1 < ySize) {
            //DOWN-RIGHT
            switch (Math.abs(contextMap[y + 1][x + 1])) {
              case 0:
                contextMap[y + 1][x + 1] = CP ? (byte) -1 : (byte) 1;
                break;
              case 1:
                contextMap[y + 1][x + 1] = contextMap[y + 1][x + 1] < 0 ? (byte) -2 : (byte) 2;
                break;
              case 5:
                contextMap[y + 1][x + 1] = contextMap[y + 1][x + 1] < 0 ? (byte) -6 : (byte) 6;
                break;
            }
          }
        }
        //DOWN
        if (y + 1 < ySize) {
          switch (Math.abs(contextMap[y + 1][x])) {
            case 0:
              contextMap[y + 1][x] = CP ? (byte) -3 : (byte) 3;
              break;
            case 1:
            case 2:
              contextMap[y + 1][x] = contextMap[y + 1][x] < 0 ? (byte) -3 : (byte) 3;
              break;
            case 3:
              contextMap[y + 1][x] = contextMap[y + 1][x] < 0 ? (byte) -4 : (byte) 4;
              break;
            case 5:
            case 6:
              contextMap[y + 1][x] = contextMap[y + 1][x] < 0 ? (byte) -7 : (byte) 7;
              break;
          }
        }
        //UP
        if (y - 1 >= 0) {
          switch (Math.abs(contextMap[y - 1][x])) {
            case 0:
              contextMap[y - 1][x] = CP ? (byte) 3 : (byte) -3;
              break;
            case 1:
            case 2:
              contextMap[y - 1][x] = contextMap[y - 1][x] < 0 ? (byte) -3 : (byte) 3;
              break;
            case 3:
              contextMap[y - 1][x] = contextMap[y - 1][x] < 0 ? (byte) -4 : (byte) 4;
              break;
            case 5:
            case 6:
              contextMap[y - 1][x] = contextMap[y - 1][x] < 0 ? (byte) -7 : (byte) 7;
              break;
          }
        }
        break;
      case 1: //SUBBAND HL
        if (x - 1 >= 0) {
          //LEFT
          switch (Math.abs(contextMap[y][x - 1])) {
            case 0:
              contextMap[y][x - 1] = CP ? (byte) 3 : (byte) -3;
              break;
            case 1:
            case 2:
              contextMap[y][x - 1] = contextMap[y][x - 1] < 0 ? (byte) -3 : (byte) 3;
              break;
            case 3:
              contextMap[y][x - 1] = contextMap[y][x - 1] < 0 ? (byte) -4 : (byte) 4;
              break;
            case 5:
            case 6:
              contextMap[y][x - 1] = contextMap[y][x - 1] < 0 ? (byte) -7 : (byte) 7;
              break;
          }
          if (y - 1 >= 0) {
            //UP-LEFT
            switch (Math.abs(contextMap[y - 1][x - 1])) {
              case 0:
                contextMap[y - 1][x - 1] = CP ? (byte) 1 : (byte) -1;
                break;
              case 1:
                contextMap[y - 1][x - 1] = contextMap[y - 1][x - 1] < 0 ? (byte) -2 : (byte) 2;
                break;
              case 5:
                contextMap[y - 1][x - 1] = contextMap[y - 1][x - 1] < 0 ? (byte) -6 : (byte) 6;
                break;
            }
          }
          if (y + 1 < ySize) {
            //DOWN-LEFT
            switch (Math.abs(contextMap[y + 1][x - 1])) {
              case 0:
                if (CP) {
                  contextMap[y + 1][x - 1] = (y + 1) % stripeHeight != 0 ? (byte) 1 : (byte) -1;
                } else {
                  contextMap[y + 1][x - 1] = (y + 1) % stripeHeight != 0 ? (byte) -1 : (byte) 1;
                }
                break;
              case 1:
                contextMap[y + 1][x - 1] = contextMap[y + 1][x - 1] < 0 ? (byte) -2 : (byte) 2;
                break;
              case 5:
                contextMap[y + 1][x - 1] = contextMap[y + 1][x - 1] < 0 ? (byte) -6 : (byte) 6;
                break;
            }
          }
        }
        if (x + 1 < xSize) {
          //RIGHT
          switch (Math.abs(contextMap[y][x + 1])) {
            case 0:
              contextMap[y][x + 1] = CP ? (byte) -3 : (byte) 3;
              break;
            case 1:
            case 2:
              contextMap[y][x + 1] = contextMap[y][x + 1] < 0 ? (byte) -3 : (byte) 3;
              break;
            case 3:
              contextMap[y][x + 1] = contextMap[y][x + 1] < 0 ? (byte) -4 : (byte) 4;
              break;
            case 5:
            case 6:
              contextMap[y][x + 1] = contextMap[y][x + 1] < 0 ? (byte) -7 : (byte) 7;
              break;
          }
          if (y - 1 >= 0) {
            //UP-RIGHT
            switch (Math.abs(contextMap[y - 1][x + 1])) {
              case 0:
                if (CP) {
                  contextMap[y - 1][x + 1] = y % stripeHeight != 0 ? (byte) -1 : (byte) 1;
                } else {
                  contextMap[y - 1][x + 1] = y % stripeHeight != 0 ? (byte) 1 : (byte) -1;
                }
                break;
              case 1:
                contextMap[y - 1][x + 1] = contextMap[y - 1][x + 1] < 0 ? (byte) -2 : (byte) 2;
                break;
              case 5:
                contextMap[y - 1][x + 1] = contextMap[y - 1][x + 1] < 0 ? (byte) -6 : (byte) 6;
                break;
            }
          }
          if (y + 1 < ySize) {
            //DOWN-RIGHT
            switch (Math.abs(contextMap[y + 1][x + 1])) {
              case 0:
                contextMap[y + 1][x + 1] = CP ? (byte) -1 : (byte) 1;
                break;
              case 1:
                contextMap[y + 1][x + 1] = contextMap[y + 1][x + 1] < 0 ? (byte) -2 : (byte) 2;
                break;
              case 5:
                contextMap[y + 1][x + 1] = contextMap[y + 1][x + 1] < 0 ? (byte) -6 : (byte) 6;
                break;
            }
          }
        }
        //DOWN
        if (y + 1 < ySize) {
          switch (Math.abs(contextMap[y + 1][x])) {
            case 0:
              contextMap[y + 1][x] = CP ? (byte) -5 : (byte) 5;
              break;
            case 1:
            case 2:
              contextMap[y + 1][x] = contextMap[y + 1][x] < 0 ? (byte) -6 : (byte) 6;
              break;
            case 3:
            case 4:
              contextMap[y + 1][x] = contextMap[y + 1][x] < 0 ? (byte) -7 : (byte) 7;
              break;
            case 5:
            case 6:
            case 7:
              contextMap[y + 1][x] = contextMap[y + 1][x] < 0 ? (byte) -8 : (byte) 8;
              break;
          }
        }
        //UP
        if (y - 1 >= 0) {
          switch (Math.abs(contextMap[y - 1][x])) {
            case 0:
              contextMap[y - 1][x] = CP ? (byte) 5 : (byte) -5;
              break;
            case 1:
            case 2:
              contextMap[y - 1][x] = contextMap[y - 1][x] < 0 ? (byte) -6 : (byte) 6;
              break;
            case 3:
            case 4:
              contextMap[y - 1][x] = contextMap[y - 1][x] < 0 ? (byte) -7 : (byte) 7;
              break;
            case 5:
            case 6:
            case 7:
              contextMap[y - 1][x] = contextMap[y - 1][x] < 0 ? (byte) -8 : (byte) 8;
              break;
          }
        }
        break;
      case 3: //SUBBAND HH
        if (x - 1 >= 0) {
          //LEFT
          switch (Math.abs(contextMap[y][x - 1])) {
            case 0:
              contextMap[y][x - 1] = CP ? (byte) 1 : (byte) -1;
              break;
            case 1:
              contextMap[y][x - 1] = contextMap[y][x - 1] < 0 ? (byte) -2 : (byte) 2;
              break;
            case 3:
              contextMap[y][x - 1] = contextMap[y][x - 1] < 0 ? (byte) -4 : (byte) 4;
              break;
            case 4:
              contextMap[y][x - 1] = contextMap[y][x - 1] < 0 ? (byte) -5 : (byte) 5;
              break;
            case 6:
              contextMap[y][x - 1] = contextMap[y][x - 1] < 0 ? (byte) -7 : (byte) 7;
              break;
          }
          if (y - 1 >= 0) {
            //UP-LEFT
            switch (Math.abs(contextMap[y - 1][x - 1])) {
              case 0:
                contextMap[y - 1][x - 1] = CP ? (byte) 3 : (byte) -3;
                break;
              case 1:
                contextMap[y - 1][x - 1] = contextMap[y - 1][x - 1] < 0 ? (byte) -4 : (byte) 4;
                break;
              case 2:
                contextMap[y - 1][x - 1] = contextMap[y - 1][x - 1] < 0 ? (byte) -5 : (byte) 5;
                break;
              case 3:
                contextMap[y - 1][x - 1] = contextMap[y - 1][x - 1] < 0 ? (byte) -6 : (byte) 6;
                break;
              case 4:
              case 5:
                contextMap[y - 1][x - 1] = contextMap[y - 1][x - 1] < 0 ? (byte) -7 : (byte) 7;
                break;
              case 6:
              case 7:
                contextMap[y - 1][x - 1] = contextMap[y - 1][x - 1] < 0 ? (byte) -8 : (byte) 8;
                break;
            }
          }
          if (y + 1 < ySize) {
            //DOWN-LEFT
            switch (Math.abs(contextMap[y + 1][x - 1])) {
              case 0:
                if (CP) {
                  contextMap[y + 1][x - 1] = (y + 1) % stripeHeight != 0 ? (byte) 3 : (byte) -3;
                } else {
                  contextMap[y + 1][x - 1] = (y + 1) % stripeHeight != 0 ? (byte) -3 : (byte) 3;
                }
                break;
              case 1:
                contextMap[y + 1][x - 1] = contextMap[y + 1][x - 1] < 0 ? (byte) -4 : (byte) 4;
                break;
              case 2:
                contextMap[y + 1][x - 1] = contextMap[y + 1][x - 1] < 0 ? (byte) -5 : (byte) 5;
                break;
              case 3:
                contextMap[y + 1][x - 1] = contextMap[y + 1][x - 1] < 0 ? (byte) -6 : (byte) 6;
                break;
              case 4:
              case 5:
                contextMap[y + 1][x - 1] = contextMap[y + 1][x - 1] < 0 ? (byte) -7 : (byte) 7;
                break;
              case 6:
              case 7:
                contextMap[y + 1][x - 1] = contextMap[y + 1][x - 1] < 0 ? (byte) -8 : (byte) 8;
                break;
            }
          }
        }
        if (x + 1 < xSize) {
          //RIGHT
          switch (Math.abs(contextMap[y][x + 1])) {
            case 0:
              contextMap[y][x + 1] = CP ? (byte) -1 : (byte) 1;
              break;
            case 1:
              contextMap[y][x + 1] = contextMap[y][x + 1] < 0 ? (byte) -2 : (byte) 2;
              break;
            case 3:
              contextMap[y][x + 1] = contextMap[y][x + 1] < 0 ? (byte) -4 : (byte) 4;
              break;
            case 4:
              contextMap[y][x + 1] = contextMap[y][x + 1] < 0 ? (byte) -5 : (byte) 5;
              break;
            case 6:
              contextMap[y][x + 1] = contextMap[y][x + 1] < 0 ? (byte) -7 : (byte) 7;
              break;
          }
          if (y - 1 >= 0) {
            //UP-RIGHT
            switch (Math.abs(contextMap[y - 1][x + 1])) {
              case 0:
                if (CP) {
                  contextMap[y - 1][x + 1] = y % stripeHeight != 0 ? (byte) -3 : (byte) 3;
                } else {
                  contextMap[y - 1][x + 1] = y % stripeHeight != 0 ? (byte) 3 : (byte) -3;
                }
                break;
              case 1:
                contextMap[y - 1][x + 1] = contextMap[y - 1][x + 1] < 0 ? (byte) -4 : (byte) 4;
                break;
              case 2:
                contextMap[y - 1][x + 1] = contextMap[y - 1][x + 1] < 0 ? (byte) -5 : (byte) 5;
                break;
              case 3:
                contextMap[y - 1][x + 1] = contextMap[y - 1][x + 1] < 0 ? (byte) -6 : (byte) 6;
                break;
              case 4:
              case 5:
                contextMap[y - 1][x + 1] = contextMap[y - 1][x + 1] < 0 ? (byte) -7 : (byte) 7;
                break;
              case 6:
              case 7:
                contextMap[y - 1][x + 1] = contextMap[y - 1][x + 1] < 0 ? (byte) -8 : (byte) 8;
                break;
            }
          }
          if (y + 1 < ySize) {
            //DOWN-RIGHT
            switch (Math.abs(contextMap[y + 1][x + 1])) {
              case 0:
                contextMap[y + 1][x + 1] = CP ? (byte) -3 : (byte) 3;
                break;
              case 1:
                contextMap[y + 1][x + 1] = contextMap[y + 1][x + 1] < 0 ? (byte) -4 : (byte) 4;
                break;
              case 2:
                contextMap[y + 1][x + 1] = contextMap[y + 1][x + 1] < 0 ? (byte) -5 : (byte) 5;
                break;
              case 3:
                contextMap[y + 1][x + 1] = contextMap[y + 1][x + 1] < 0 ? (byte) -6 : (byte) 6;
                break;
              case 4:
              case 5:
                contextMap[y + 1][x + 1] = contextMap[y + 1][x + 1] < 0 ? (byte) -7 : (byte) 7;
                break;
              case 6:
              case 7:
                contextMap[y + 1][x + 1] = contextMap[y + 1][x + 1] < 0 ? (byte) -8 : (byte) 8;
                break;
            }
          }
        }
        //DOWN
        if (y + 1 < ySize) {
          switch (Math.abs(contextMap[y + 1][x])) {
            case 0:
              contextMap[y + 1][x] = CP ? (byte) -1 : (byte) 1;
              break;
            case 1:
              contextMap[y + 1][x] = contextMap[y + 1][x] < 0 ? (byte) -2 : (byte) 2;
              break;
            case 3:
              contextMap[y + 1][x] = contextMap[y + 1][x] < 0 ? (byte) -4 : (byte) 4;
              break;
            case 4:
              contextMap[y + 1][x] = contextMap[y + 1][x] < 0 ? (byte) -5 : (byte) 5;
              break;
            case 6:
              contextMap[y + 1][x] = contextMap[y + 1][x] < 0 ? (byte) -7 : (byte) 7;
              break;
          }
        }
        //UP
        if (y - 1 >= 0) {
          switch (Math.abs(contextMap[y - 1][x])) {
            case 0:
              contextMap[y - 1][x] = CP ? (byte) 1 : (byte) -1;
              break;
            case 1:
              contextMap[y - 1][x] = contextMap[y - 1][x] < 0 ? (byte) -2 : (byte) 2;
              break;
            case 3:
              contextMap[y - 1][x] = contextMap[y - 1][x] < 0 ? (byte) -4 : (byte) 4;
              break;
            case 4:
              contextMap[y - 1][x] = contextMap[y - 1][x] < 0 ? (byte) -5 : (byte) 5;
              break;
            case 6:
              contextMap[y - 1][x] = contextMap[y - 1][x] < 0 ? (byte) -7 : (byte) 7;
              break;
          }
        }
        break;
    }
  }
}
