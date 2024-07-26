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
import GiciException.*;

/**
 * This class receives an image and performs the dead zone dequantization
 * defined in JPEG2000 standard. This class can be used for the ranging stage
 * defined for lossless compression too.<br>
 * Usage example:<br>
 * &nbsp; constructor<br>
 * &nbsp; run<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1 2012/01/26
 */
public class Dequantization {

  /**
   *
   */
  private ClientJPEG2KCodestream codestream = null;

  private ClientJPEG2KTile tileObj = null;

  private ClientJPEG2KComponent compObj = null;

  private ClientJPEG2KResolutionLevel rLevelObj = null;

  /**
   * Definition in {@link CADI.Client.ClientLogicalTarget.JPEG2000.JPEG2KLogicalTarget#imageSamplesFloat}
   */
  /**
   *
   */
  private float[][][] imageSamples = null;

  /**
   * Reconstruction parameter used to adjust better the dequantitzation process (when QTypes == 1).
   * <p>
   * Recommened values between 0 to 1.
   */
  private float subbandReconstructionValue = DEFAULT_SUBBAND_RECONSTRUCTION;

  public static final float DEFAULT_SUBBAND_RECONSTRUCTION = 0.37F;

  //INTERNAL VARIABLES
  /**
   * Log_2 gain bits for each subband (LL, HL, LH, HH).
   * <p>
   * Constant values.
   */
  private final int[] gain = {0, 1, 1, 2};

  // ============================= public methods ==============================
  /**
   * Constructor.
   * 
   * @param codestream definition in {@link #codestream}.
   */
  public Dequantization(ClientJPEG2KCodestream codestream) {
    this(codestream, DEFAULT_SUBBAND_RECONSTRUCTION);
  }

  /**
   * Constructor.
   * 
   * @param codestream definition in {@link #codestream}
   * @param subbandReconstructionValue  definition in {@link #subbandReconstructionValue}.
   */
  public Dequantization(ClientJPEG2KCodestream codestream,
          float subbandReconstructionValue) {
    if (codestream == null) {
      throw new NullPointerException();
    }
    if (!((subbandReconstructionValue >= 0) && (subbandReconstructionValue <= 1))) {
      throw new IllegalArgumentException();
    }

    // Copy input parameters
    this.codestream = codestream;
    this.subbandReconstructionValue = subbandReconstructionValue;
    
    tileObj = codestream.getTile(0);
  }

  /**
   * Performs the dequantization of the image <code>imageSamples</code>.
   * 
   * @param imageSamples 3D array with the image samples to be dequantized.
   * @param discardLevels number of discard levels.
   * @param relevantComponents one-dimensional array with the relevant components.
   * 
   * @throws ErrorException 
   */
  public void run(float[][][] imageSamples, int discardLevels,
          int[] relevantComponents) throws ErrorException {
    if (discardLevels < 0) {
      throw new IllegalArgumentException();
    }
    if (imageSamples == null) {
      throw new NullPointerException();
    }
    
    int zSize = imageSamples.length;
    int ySize = imageSamples[0].length;
    int xSize = imageSamples[0][0].length;

    for (int zIdx = 0; zIdx < zSize; zIdx++) {
      int z = relevantComponents[zIdx];
      compObj = tileObj.getComponent(z);

      //Level size
      int xSubbandSize = xSize;
      int ySubbandSize = ySize;

      int maxWTLevels = compObj.getWTLevels() - discardLevels;

      //Apply quantization for each level
      for (int rLevel = 0; rLevel < maxWTLevels; rLevel++) {
        rLevelObj = compObj.getResolutionLevel(rLevel);

        //Size setting for the level
        int xOdd = xSubbandSize % 2;
        int yOdd = ySubbandSize % 2;
        xSubbandSize = xSubbandSize / 2 + xOdd;
        ySubbandSize = ySubbandSize / 2 + yOdd;

        // LL HL
        // LH HH
        //HL, LH, HH subband
        int[] yBegin = {0, ySubbandSize, ySubbandSize};
        int[] xBegin = {xSubbandSize, 0, xSubbandSize};
        int[] yEnd = {ySubbandSize, ySubbandSize * 2 - yOdd, ySubbandSize * 2 - yOdd};
        int[] xEnd = {xSubbandSize * 2 - xOdd, xSubbandSize, xSubbandSize * 2 - xOdd};

        //Memory allocation and exponent bits for this resolution level
        //int expBits = codestream.getPrecision(z) + gain[3];

        //Apply quantization for each subband
        for (int subband = 0; subband < 3; subband++) {

          //Calculus of the step size, exponents and mantissas
          float stepSize = 0F;
          float subbandReconstruction = 0F;
          int dynamicRange;
          switch (compObj.getQuantizationStyle()) {
            case 0: //Nothing
              dynamicRange = codestream.getPrecision(z) + gain[subband + 1];
              stepSize = 1;
              break;
            case 1: //JPEG2000 - derived
            case 2: //JPEG2000 - expounded
              int LLSubband = compObj.getResolutionLevel(maxWTLevels - rLevel).getNumSubbands() == 1 ? 0 : 1;
              int exponent = compObj.getResolutionLevel(maxWTLevels - rLevel).getExponent(subband + LLSubband);
              int mantisa = compObj.getResolutionLevel(maxWTLevels - rLevel).getMantisa(subband + LLSubband);
              dynamicRange = codestream.getPrecision(z) + gain[subband + 1];
              stepSize = (float)Math.pow(2, dynamicRange - exponent)
                         * (1 + ((float)mantisa / (float)Math.pow(2, 11)));
              subbandReconstruction = subbandReconstructionValue;
              break;
            default:
              throw new ErrorException("Unrecognized dequantization type.");
          }

          //Apply quantization for each sample
          for (int y = yBegin[subband]; y < yEnd[subband]; y++) {
            for (int x = xBegin[subband]; x < xEnd[subband]; x++) {
              imageSamples[zIdx][y][x] = ((Math.abs(imageSamples[zIdx][y][x])
                                        + (imageSamples[zIdx][y][x] != 0 ? subbandReconstruction : 0F))
                                       * stepSize) * (imageSamples[zIdx][y][x] < 0 ? -1 : 1);
            }
          }
        }
      }

      //LL subband
      //Calculus of the step size, exponents and mantissas for LL subband
      float stepSize = 0F;
      float subbandReconstruction = 0F;
      int dynamicRange;
      switch (compObj.getQuantizationStyle()) {
        case 0: //Nothing
          dynamicRange = codestream.getPrecision(z) + gain[0];
          stepSize = 1;
          break;
        case 1: //JPEG2000 - derived
        case 2: //JPEG2000 - expounded
          int LLSubband = compObj.getResolutionLevel(0).getNumSubbands() == 1 ? 0 : 1;
          int exponent = compObj.getResolutionLevel(0).getExponent(0 + LLSubband);
          int mantisa = compObj.getResolutionLevel(0).getMantisa(0 + LLSubband);
          dynamicRange = codestream.getPrecision(z) + gain[0];
          stepSize = (float)Math.pow(2, dynamicRange - exponent)
                     * (1 + ((float)mantisa / (float)Math.pow(2, 11)));
          subbandReconstruction = subbandReconstructionValue;
          break;
      }

      //Quantization calculus
      for (int y = 0; y < ySubbandSize; y++) {
        for (int x = 0; x < xSubbandSize; x++) {
          imageSamples[zIdx][y][x] = ((Math.abs(imageSamples[zIdx][y][x])
                                       + (imageSamples[zIdx][y][x] != 0 ? subbandReconstruction : 0F))
                                      * stepSize) * (imageSamples[zIdx][y][x] < 0 ? -1 : 1);
        }
      }
    }

  }

  /**
   *
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
   * Prints this Dequantization's fields to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Dequantization --");

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
}
