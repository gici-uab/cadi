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
package CADI.Server.LogicalTarget.JPEG2000.DeliveringModes;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import CADI.Common.LogicalTarget.JPEG2000.JPCParameters;
import CADI.Common.Network.JPIP.EORCodes;
import CADI.Common.Network.JPIP.JPIPMessageHeader;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Common.Util.ArraysUtil;
import CADI.Server.Cache.ServerCacheModel;
import CADI.Server.Core.ResponseData;
import CADI.Server.LogicalTarget.JPEG2000.JP2KServerLogicalTarget;
import CADI.Server.LogicalTarget.JPEG2000.Codestream.JPCMainHeaderEncoder;
import CADI.Server.LogicalTarget.JPEG2000.Codestream.PacketHeadersEncoder;
import CADI.Server.LogicalTarget.JPEG2000.ServerJPEG2KCodestream;
import CADI.Server.LogicalTarget.JPEG2000.ServerJPEG2KComponent;
import CADI.Server.LogicalTarget.JPEG2000.ServerJPEG2KPrecinct;
import CADI.Server.LogicalTarget.JPEG2000.ServerJPEG2KResolutionLevel;
import CADI.Server.LogicalTarget.JPEG2000.ServerJPEG2KTile;
import GiciException.ErrorException;
import GiciException.WarningException;

/**
 * This class implements the Characterization of Rate Distortion (CoRD)
 * algorithm.
 * <p>
 * Furthe 
 * 
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; runResponseParameters<br>
 * &nbsp; getResponseViewWindow<br>
 * &nbsp; getQuality<br>
 * &nbsp; runResponseData<br>
 * &nbsp; getJPIPMessageData<br>
 * &nbsp; getEORReasonCode<br>
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.2 2009/05/18
 */
public class CoRDDelivery {

  /**
   * 
   */
  private ServerJPEG2KCodestream codestream = null;

  /**
   *
   */
  private ArrayList<ResponseData> responseDataList = null;

  /**
   * Contains the encoded main header.
   */
  private byte[] mainHeader = null;

  /**
   * Definition in {@link CADI.Server.LogicalTarget.ServerLogicalTargetManager#responseViewWindow}.
   */
  private ViewWindowField responseViewWindow = null;

  /**
   * Definition in {@link CADI.Server.LogicalTarget.ServerLogicalTargetManager#quality}.
   */
  private int quality = -1;

  /**
   * Definition in {@link CADI.Server.LogicalTarget.ServerLogicalTargetManager#EORReasonCode}.
   */
  private int EORReasonCode;

  /**
   * Definition in {@link CADI.Server.LogicalTarget.ServerLogicalTargetManager#EORReasonCode}.
   */
  private JP2KServerLogicalTarget logicalTarget = null;

  /**
   * This attribute contains the cache data for the client.
   * <p>
   * This reference is passed from the
   *
   */
  private ServerCacheModel serverCache = null;

  /**
   * Definition in {@link CADI.Common.Network.JPIP.DataLimitField#len}
   */
  private long maximumResponseLength = -1;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Codestream.GenerateImageStructure#imageStructure}
   */
  //private int[][][][][][] imageStructure = null;

  /**
   * Definition in {@link CADI.Server.LogicalTarget.JPEG2000.JP2LogicalTarget#MSBPlanes}.
   */
  private int MSBPlane;

  // INTERNAL ATTRIBUTES
  /**
   * It is a temporal attribute to accumulate the response length which is
   * sending to the client.
   */
  private long responseLength = 0;

  private int[][] comps = null;

  private int layers = -1;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#numLayers}
   */
  private int maxNumLayers;

  /**
   * Is a mutiple-dimensional array which will contain the precincts belonging
   * to the WOI.
   */
  private int[][][] precinctsPerRLevel = null;

  /**
   * Is a multiple-dimensional array which will be a mapping between the
   * component-resolutionlevel-precinct and the unique in-class identifier.
   */
  private long[][][] inClassIdentifierCorrespondence = null;

  /**
   * This object is used to build the packet headers.
   */
  private PacketHeadersEncoder packetHeading = null;

  /**
   *
   */
  protected ViewWindowField actualViewWindow = null;

  /**
   *
   */
  protected int discardLevels = -1;

  private ServerJPEG2KTile tileObj = null;

  private ServerJPEG2KComponent compObj = null;

  private ServerJPEG2KResolutionLevel rLevelObj = null;

  private ServerJPEG2KPrecinct precinctObj = null;
  
  // ============================= public methods ==============================
  /**
   *
   * @param logicalTarget
   * @param serverCache
   */
  public CoRDDelivery(JP2KServerLogicalTarget logicalTarget,
                      ServerCacheModel serverCache) {
    this(logicalTarget, serverCache, false);
  }

  /**
   *
   * @param logicalTarget
   * @param serverCache
   * @param align
   */
  public CoRDDelivery(JP2KServerLogicalTarget logicalTarget,
                      ServerCacheModel serverCache, boolean align) {

    // Check input parameters
    if (logicalTarget == null) {
      throw new NullPointerException();
    }
    if (serverCache == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.logicalTarget = logicalTarget;
    this.serverCache = serverCache;

    // Initializations
    responseViewWindow = new ViewWindowField();
    responseViewWindow.reset();
    responseLength = 0;
    maxNumLayers = Integer.MAX_VALUE;

    codestream = logicalTarget.getCodestream(0);
    MSBPlane = logicalTarget.getMSBPlane();

    // Set end of response code
    EORReasonCode = EORCodes.WINDOW_DONE;
  }

  /**
   * Calculates the WOI which will be sent to the client using a layers-based
   * rate-distortion method (file is delivery in the same order that is is
   * saved).
   *
   * @param viewWindow the requested Window Of Interest
   * @throws ErrorException
   */
  public void runResponseParameters(ViewWindowField viewWindow)
          throws ErrorException {

    // Check input parameters
    if (viewWindow == null) {
      throw new NullPointerException();
    }

    actualViewWindow = null;

    // If the frame size has been omitted, no compressed image data is sent.
    if ((viewWindow.fsiz[0] < 0) && (viewWindow.fsiz[1] < 0)) {
      responseViewWindow = new ViewWindowField();
      return;
    }

    // It is the WOI to be delivered.
    // All fields of the object have to be set and check ranges
    actualViewWindow = new ViewWindowField(viewWindow);
    if (actualViewWindow.fsiz[0] > codestream.getXSize()) {
      actualViewWindow.fsiz[0] = codestream.getXSize();
    }
    if (actualViewWindow.fsiz[1] > codestream.getYSize()) {
      actualViewWindow.fsiz[1] = codestream.getYSize();
    }
    if (actualViewWindow.roff[0] < 0) {
      actualViewWindow.roff[0] = 0;
    }
    if (actualViewWindow.roff[1] < 0) {
      actualViewWindow.roff[1] = 0;
    }
    if (actualViewWindow.rsiz[0] < 0) {
      actualViewWindow.rsiz[0] = actualViewWindow.fsiz[0];
    }
    if (actualViewWindow.rsiz[1] < 0) {
      actualViewWindow.rsiz[1] = actualViewWindow.fsiz[1];
    }
    if (actualViewWindow.roff[0] + actualViewWindow.rsiz[0] > actualViewWindow.fsiz[0]) {
      actualViewWindow.rsiz[0] = actualViewWindow.fsiz[0] - actualViewWindow.roff[0];
    }
    if (actualViewWindow.roff[1] + actualViewWindow.rsiz[1] > actualViewWindow.fsiz[1]) {
      actualViewWindow.rsiz[1] = actualViewWindow.fsiz[1] - actualViewWindow.roff[1];
    }
    if (actualViewWindow.comps == null) {
      actualViewWindow.comps = new int[1][2];
      actualViewWindow.comps[0][0] = 0;
      actualViewWindow.comps[0][1] = codestream.getZSize() - 1;
    }

    maxNumLayers = (MSBPlane * 3) * 3;
    if (actualViewWindow.layers < 0) {
      actualViewWindow.layers = maxNumLayers;
    }
    if (actualViewWindow.layers > maxNumLayers) {
      actualViewWindow.layers = maxNumLayers;
    }


    if ((actualViewWindow.roff[0] >= actualViewWindow.fsiz[0])
            || (actualViewWindow.roff[1] >= actualViewWindow.fsiz[1])) {
      throw new ErrorException("The frame size cannot be greater than the region offset");
    }

    // Get the number of discard levels
    discardLevels =
            codestream.determineNumberOfDiscardLevels(actualViewWindow.fsiz,
            actualViewWindow.roundDirection);

    // Fit requested region to the suitable resolution
    codestream.mapRegionToSuitableResolutionGrid(actualViewWindow.fsiz,
            actualViewWindow.roff,
            actualViewWindow.rsiz,
            discardLevels);

    // Determine the bounding box of the requested region
    //CADIRectangle boundingBox = JPEG2000Util.calculatePrecinctsBoundingBox(actualViewWindow,
    //								jpcParameters.sizParameters, jpcParameters.codParameters);

    // Check if geometry has been change.
    // If so, it must be notified to client
    if ((viewWindow.fsiz[0] != actualViewWindow.fsiz[0])
            || (viewWindow.fsiz[1] != actualViewWindow.fsiz[1])) {
      responseViewWindow.fsiz[0] = actualViewWindow.fsiz[0];
      responseViewWindow.fsiz[1] = actualViewWindow.fsiz[1];
    }
    if ((viewWindow.roff[0] != actualViewWindow.roff[0])
            || (viewWindow.roff[1] != actualViewWindow.roff[1])) {
      responseViewWindow.roff[0] = actualViewWindow.roff[0];
      responseViewWindow.roff[1] = actualViewWindow.roff[1];
    }
    if ((viewWindow.rsiz[0] != actualViewWindow.rsiz[0])
            || (viewWindow.rsiz[1] != actualViewWindow.rsiz[1])) {
      responseViewWindow.rsiz[0] = actualViewWindow.rsiz[0];
      responseViewWindow.rsiz[1] = actualViewWindow.rsiz[1];
    }

    /*System.out.println("REQUESTED VIEW WINDOW");
    viewWindow.list(System.out);
    System.out.println("ACTUAL VIEW WINDOW");
    actualViewWindow.list(System.out);
    System.out.println("RESPONSE VIEW WINDOW");
    responseViewWindow.list(System.out);*/

    // Adjust the quality response parameter to the quality layers.
    // It is a coarse estimation and it should be improved (but is the
    // easiest method)
    quality = (int)(100D / (double)maxNumLayers) * layers;
  }

  /**
   * Gets the data of the response window of interest. This data is calculate
   * using the CoRD algorithm.
   *
   * @param httpResponseSender definition in {@link #httpResponseSender}
   * @param jpipMessageEncoder definition in {@link #jpipMessageEncoder}
   * @param jpipMessageHeaders definition in {@link #jpipMessageHeaders}
   *
   * @throws ErrorException
   * @throws IOException
   */
  public void runResponseData(ArrayList<ResponseData> responseDataList,
          long maximumResponseLength) throws ErrorException {

    // Check input parameters
    if (responseDataList == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.responseDataList = responseDataList;


    // RELEVANT PRECINCTS
    ArrayList<Long> relevantPrecincts =
            codestream.findRelevantPrecincts(actualViewWindow);
    //System.out.print("RELEVANT PRECINCTS:"); for (long prec : relevantPrecincts) System.out.print(" "+prec); System.out.println(); // DEBUG

    // PRECINCT DELIVERY ORDER
    ArrayList<BlockOrder> blockOrder = getDeliveryOrder(relevantPrecincts);

    // ADJUST FIRST LAYER
    ArrayList<CodingPassID> scanningOrder = adjustScanningOrder(blockOrder);

    // SEND PRECINCTS
    //if (layers > 0) responsePerQualityLayer(scanningOrder);
    //else responsePerLength(scanningOrder);

    if (maximumResponseLength == Long.MAX_VALUE) {
      responsePerQualityLayer(scanningOrder);
    } else {
      responsePerLength(scanningOrder);
    }

  }

  /**
   * Returns the main header.
   *
   * @return the {@link #mainHeader}.
   */
  public byte[] getMainHeader() {
    encodeMainHeader();
    return mainHeader;
  }

  /**
   * Returns the {@link #EORReasonCode} attribute.
   *
   * @return the {@link #EORReasonCode} attribute.
   */
  public int getEORReasonCode() {
    return EORReasonCode;
  }

  /**
   * Returns the {@link #responseViewWindow} attribute.
   *
   * @return the {@link #responseViewWindow} attribute.
   */
  public ViewWindowField getResponseViewWindow() {
    return responseViewWindow;
  }

  /**
   * Returns the {@link #jpipMessageHeaders} attribute.
   *
   * @return the {@link #jpipMessageHeaders} attribute.
   */
  public ArrayList<ResponseData> getResponseData() {
    return responseDataList;
  }

  /**
   * Returns the {@link #quality} attribute.
   *
   * @return the {@link #quality} attribute.
   */
  public int getQuality() {
    return quality;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    String str = "";

    str = getClass().getName() + " [";

    str += "Not implemente yet";

    str += "]";
    return str;
  }

  /**
   * Prints this CoRD delivery out to the specified output stream. This method
   * is useful for debugging.
   *
   * @param out
   *            an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- CoRD delivery --");

    out.println("Not implemented yet");

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * Gets the main header as a byte array. This method can be called if the
   * transcoding is done.
   */
  private void encodeMainHeader() {

    JPCParameters jpcParameters = new JPCParameters();
    jpcParameters.sizParameters = codestream.getSIZParameters();
    jpcParameters.codParameters = codestream.getCODParameters();
    jpcParameters.qcdParameters = codestream.getQCDParameters();
    jpcParameters.jpkParameters = codestream.getJPKParameters();
    jpcParameters.codParameters.numLayers = maxNumLayers;
    JPCMainHeaderEncoder jpcHeading = new JPCMainHeaderEncoder(jpcParameters);
    try {
      mainHeader = jpcHeading.run();
    } catch (WarningException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * Returns a rate-distortion adjustment for a component, resolution level,
   * and subband.
   *
   * @param z image component
   * @param rLevel 0 is the LL subband, and 1, 2, ... represents
   * 	next starting with the little one
   * @param subband 0 - HL, 1 - LH, 2 - HH (if resolutionLevel == 0 --> 0 -
   * 	LL)
   *
   * @return the rate-distortion adjustment for a component, resolution
   *  level, and subband.
   */
  private int getSubbandWeight(int tile, int z, int rLevel, int subband) {

    tileObj = codestream.getTile(tile);
    compObj = tileObj.getComponent(z);


    int totalRLevels = compObj.getWTLevels();
    int subbandWeight = -1;
    boolean isColorTransform = codestream.getMultiComponentTransform() == 1 ? true : false;

    if (compObj.getQuantizationStyle() == 0) {
      // Reversible quantization
      if (!isColorTransform) {
        // Adjustment depending on rLevel and subband
        //rateDistortionAdjustment = totalRLevels - rLevel - (subband == 2 ? 1: 0);
        subbandWeight = totalRLevels - rLevel - (subband == 2 ? 1 : 0) - 1;
        if (subbandWeight < 0) {
          subbandWeight = 0;
        }
      } else {
        // Adjustment depending on rLevel and subband and more valoration of Component 0 (for RCT)
        subbandWeight = totalRLevels - rLevel - (subband == 2 ? 1 : 0);
        //rateDistortionAdjustment = totalRLevels - rLevel - (subband == 2 ? 1: 0) - 1;
        //if(rateDistortionAdjustment < 0) rateDistortionAdjustment = 0;
        if (z == 0) { //This value is calculated experimentally
          subbandWeight += 2;
        }
      }

    } else { // Irreversible quantization
      // No adjustment
      //rateDistortionAdjustment = 0;
      subbandWeight = totalRLevels - rLevel - (subband == 2 ? 1 : 0) - 1;
      if (subbandWeight < 0) {
        subbandWeight = 0;
      }
    }

    return subbandWeight;
  }

    /**
   * Calculate which is the maximum subband weight for the whole image.
   *
   * @see #getSubbandWeight(int, int, int) method.
   *
   * @return the maximum subband weight.
   */
  private int getMaxSubbandWeight() {

    int maxSubbandWeight = 0;

    for (int tile = codestream.getNumTiles() - 1; tile >= 0; tile--) {
      tileObj = codestream.getTile(tile);
      for (int z = codestream.getZSize() - 1; z >= 0; z--) {
        compObj = tileObj.getComponent(z);
        for (int rLevel = compObj.getWTLevels() - 1; rLevel >= 0; rLevel--) {
          int maxSubbands = (rLevel == 0) ? 1 : 3;
          for (int subband = 0; subband < maxSubbands; subband++) {
            int tmp = getSubbandWeight(tile, z, rLevel, subband);
            if (tmp > maxSubbandWeight) {
              maxSubbandWeight = tmp;
            }
          }
        }
      }
    }

    return maxSubbandWeight;
  }

  /**
   * Performs the block building and reads the needed bytestreams using the
   * number of bytes.
   *
   * @return a bi-dimensional array with the order of precincts. The first
   *         index is the ordering number. And the second index is: if 1,
   *         indicates the current subband bit plane if 2, then number of bit
   *         planes if 3, the coding level if 4, if the coding pass must be
   *         concatenated
   *
   * @throws ErrorException
   *             when some problem reading file occurs
   */
  private ArrayList<BlockOrder> getDeliveryOrder(ArrayList<Long> relevantPrecincts) throws ErrorException {

    int maxResolutionLevel = codestream.getMaxResolutionLevels() - discardLevels;

    //
    getPrecinctsPerRLevel(relevantPrecincts);

    // Range of components to array of components
    int[] components = ArraysUtil.rangesToIndexes(comps);

    // ATTENTION: in the below algorithms we use the subbandBP as an index
    // which means: 0 (HL/LH) and 1 (HH) except for the lowest resolution
    // level, which only has 0 (LL)

    // Construction of two structures which contain the max and min number
    // of bit-planes of the code-blocks per each subband
    // Indexes mean: [rLevel][subbandBP]
    int[][][] maxsBP = new int[components.length][maxResolutionLevel][2];
    int[][][] minsBP = new int[components.length][maxResolutionLevel][2];
    calculateMaxMinBitPlanes(precinctsPerRLevel, maxsBP, minsBP, inClassIdentifierCorrespondence, components);

    // Construction of two structures which contain the estimate slope and
    // the globalSL of each subbandBP
    // Indexes mean: slopes[rLevel][subbandBP][blockBP][SL]
    // Indexes mean: currSLs[rLevel][subbandBP][blockBP]
    // ATTENTION: blockBP indicates the height of the code-block - the
    // actual height (#bit-planes) is blockBP+minsBP[][]
    float[][][][][] slopes = new float[components.length][maxResolutionLevel][2][][];
    int[][][][] currSLs = new int[components.length][maxResolutionLevel][2][];
    for (int z = 0; z < maxsBP.length; z++) {
      for (int rLevel = 0; rLevel < maxsBP[z].length; rLevel++) {

        for (int subbandBP = 0; subbandBP < maxsBP[z][rLevel].length; subbandBP++) {
          slopes[z][rLevel][subbandBP] = new float[maxsBP[z][rLevel][subbandBP] - minsBP[z][rLevel][subbandBP] + 1][];
          currSLs[z][rLevel][subbandBP] = new int[maxsBP[z][rLevel][subbandBP] - minsBP[z][rLevel][subbandBP] + 1];

          for (int blockBP = 0; blockBP < slopes[z][rLevel][subbandBP].length; blockBP++) {
            currSLs[z][rLevel][subbandBP][blockBP] = 0;
            int maxSL = ((minsBP[z][rLevel][subbandBP] + blockBP) * 3) - 2;
            slopes[z][rLevel][subbandBP][blockBP] = new float[maxSL];
          }
        }
      }
    }


    // SLOPE ESTIMATIONS
    calculateSlopeStimations(slopes, minsBP);

    // FIND SUBBAND SCANNING ORDER
    ArrayList<BlockOrder> blockOrders = findSubbandScanningOrder(slopes, currSLs, minsBP, components);

    return blockOrders;
  }

  /**
   * SLOPE ESTIMATIONS
   *
   * @param slopes
   * @param minsBP
   */
  private void calculateSlopeStimations(float[][][][][] slopes, int[][][] minsBP) {
    for (int z = 0; z < slopes.length; z++) {
      int rLevels = slopes[z].length;
      for (int rLevel = 0; rLevel < rLevels; rLevel++) {
        int maxSubbandBP = (rLevel == 0) ? 1 : 2;
        for (int subbandBP = 0; subbandBP < maxSubbandBP; subbandBP++) {
          int numBlockSets = slopes[z][rLevel][subbandBP].length;
          for (int Knum = 0; Knum < numBlockSets; Knum++) {
            int K = minsBP[z][rLevel][subbandBP] + Knum;
            int cMAX = (K * 3) - 2;
            int bjs = (rLevel * 2) + subbandBP;

            float slope;
            boolean inc;
            float dec;
            float correctionFactor;

            // CP
            // slope = (0.09f / numBlockSets) * (numBlockSets - Knum);
            slope = (0.075f / numBlockSets) * (numBlockSets - Knum);
            inc = true;
            dec = 0f;
            // correctionFactor = -0.5f;
            correctionFactor = 0f;
            for (int c = cMAX; c > 0; c -= 3) {
              int bp = ((c - 1) / 3) + 1;
              slopes[z][rLevel][subbandBP][Knum][cMAX - c] = correctionFactor + c + slope;
              if (inc) {
                correctionFactor = 0f;
                slope *= 10;
                if (slope >= 1f) {
                  inc = false;
                  slope = 0.99f;
                  // dec = 1f / (9f - (float)(K-bp));
                  dec = 1f / (K - (float)(K - bp));
                }
              } else {
                slope -= dec;
                if (slope < 0f) {
                  slope = 0f;
                }
              }
            }

            // SPP
            // slope = (0.09f / numBlockSets) * (numBlockSets - Knum);
            slope = (0.005f / numBlockSets) * (numBlockSets - Knum);
            inc = true;
            dec = 0f;
            correctionFactor = 0f;
            for (int c = cMAX - 1; c > 0; c -= 3) {
              int bp = ((c - 1) / 3) + 1;
              slopes[z][rLevel][subbandBP][Knum][cMAX - c] = correctionFactor + c + slope;
              if (inc) {
                correctionFactor = 0f;
                // slope *= 4;
                slope *= 2;
                if (slope >= 1f) {
                  inc = false;
                  slope = 0.99f;
                  dec = 1f / (K - (float)(K - bp));
                }
              } else {
                slope -= dec;
                if (slope < 0f) {
                  slope = 0f;
                }
              }
            }

            // MRP
            slope = 0f;
            inc = true;
            for (int c = cMAX - 2; c > 0; c -= 3) {
              slopes[z][rLevel][subbandBP][Knum][cMAX - c] = c + slope;
              if (inc) {
                inc = false;
                slope = -1f;
              }
            }

          }
        }
      }
    }
    // DEBUG
		/*for(int rLevel = 0; rLevel < maxsBP.length; rLevel++) {
    System.out.println("\nrLevel: "+rLevel);
    for(int subbandBP = 0; subbandBP < maxsBP[rLevel].length; subbandBP++) {
    System.out.println(" subbandBP: "+subbandBP);
    for(int bitPlane = 0; bitPlane < slopes[rLevel][subbandBP].length; bitPlane++) {
    System.out.println(" bitPlane: "+bitPlane);
    System.out.println("----> BLOCKS MAXBP: " + (minsBP[rLevel][subbandBP] + bitPlane));
    int maxSL = (bitPlane+minsBP[rLevel][subbandBP])*3 - 2;
    for(int SL = 0; SL < maxSL; SL++){
    int displaySL = maxSL - SL - 1;
    //System.out.println(" " + displaySL + " " + slopes[rLevel][subbandBP][bitPlane][SL]);
    System.out.println("      SL:"+SL+"\tdisplaySL="+displaySL+"\tslope="+slopes[rLevel][subbandBP][bitPlane][SL]);
    }
    }
    }
    }*/
    // END DEBUG


    // Eliminiation of liars points (like a convex hull, but applied when
    // only the slope is available)
    for (int z = 0; z < slopes.length; z++) {
      for (int rLevel = 0; rLevel < slopes[z].length; rLevel++) {
        for (int subbandBP = 0; subbandBP < slopes[z][rLevel].length; subbandBP++) {
          for (int blockBP = 0; blockBP < slopes[z][rLevel][subbandBP].length; blockBP++) {
            int maxSL = ((blockBP + minsBP[z][rLevel][subbandBP]) * 3) - 2;
            for (int SL = 0; SL < maxSL - 1; SL++) {
              if (slopes[z][rLevel][subbandBP][blockBP][SL] < slopes[z][rLevel][subbandBP][blockBP][SL + 1]) {
                slopes[z][rLevel][subbandBP][blockBP][SL] = slopes[z][rLevel][subbandBP][blockBP][SL + 1];
              }
            }
          }
        }
      }
    }
    // DEBUG
		/*
     * for(int rLevel = 0; rLevel < maxsBP.length; rLevel++) {
     * System.out.println("\nrLevel: "+rLevel); for(int subbandBP = 0;
     * subbandBP < maxsBP[rLevel].length; subbandBP++) {
     * System.out.println(" subbandBP: "+subbandBP); for(int bitPlane = 0;
     * bitPlane < slopes[rLevel][subbandBP].length; bitPlane++) {
     * System.out.println(" bitPlane: "+bitPlane);
     * //System.out.println("----> BLOCKS MAXBP: " +
     * (minsBP[rLevel][subbandBP] + bitPlane)); int maxSL = (bitPlane +
     * minsBP[rLevel][subbandBP])*3 - 2; for(int SL = 0; SL < maxSL; SL++){
     * int displaySL = maxSL - SL - 1; //System.out.println(" " + displaySL + " " +
     * slopes[rLevel][subbandBP][bitPlane][SL]); System.out.println(" SL:
     * "+SL+"\tdisplaySL="+displaySL+"\tslope="+slopes[rLevel][subbandBP][bitPlane][SL]); } } } }
     */
    // END DEBUG
  }

  /**
   *
   * @param slopes
   * @param currSLs
   * @param minsBP
   * @return
   */
  private ArrayList<BlockOrder> findSubbandScanningOrder(float[][][][][] slopes, int[][][][] currSLs, int[][][] minsBP, int[] components) {

    // Structure where the "magical" subband scanning order is stored
    ArrayList<BlockOrder> blockOrders = new ArrayList<BlockOrder>();

    int tile = 0;

    // Greedy algorithm finding the optimal subband scanning order	=> THIS CODE CAN BE OPTIMIZED THROUGHOUT A BISECTION ALGORITHM
    // DEBUG
    //System.out.println("RL\tSBP\tBP\tCP\tCC\tOR\tMSL\tCBBP\tmBP");
    //System.out.println("--\t---\t--\t--\t--\t--\t---\t----\t---");
    // DEBUG
    float curr_slope;
    int curr_z = -1, curr_rLevel = -1, curr_subbandBP = -1, curr_blockBP = -1, curr_SL = -1;
    do {
      curr_slope = 0f;
      for (int z = 0; z < slopes.length; z++) {
        // for(int rLevel = 0; rLevel < slopes.length; rLevel++){
        for (int rLevel = slopes[z].length - 1; rLevel >= 0; rLevel--) {
          int maxSubbandBP = (rLevel == 0) ? 1 : 2;
          // for(int subbandBP = maxSubbandBP-1; subbandBP >= 0; subbandBP--){
          for (int subbandBP = 0; subbandBP < maxSubbandBP; subbandBP++) {
            // for(int blockBP = slopes[rLevel][subbandBP].length-1; blockBP >= 0; blockBP--){
            for (int blockBP = 0; blockBP < slopes[z][rLevel][subbandBP].length; blockBP++) {
              int tmp = currSLs[z][rLevel][subbandBP][blockBP];
              if (slopes[z][rLevel][subbandBP][blockBP][tmp] > curr_slope) {
                curr_slope = slopes[z][rLevel][subbandBP][blockBP][tmp];
                curr_z = z;
                curr_rLevel = rLevel;
                curr_subbandBP = subbandBP;
                curr_blockBP = blockBP;
                curr_SL = tmp;
              }
            }
          }
        }
      }

      int concat = 1;
      slopes[curr_z][curr_rLevel][curr_subbandBP][curr_blockBP][curr_SL] = 0f;
      if (curr_SL < slopes[curr_z][curr_rLevel][curr_subbandBP][curr_blockBP].length - 1) {
        currSLs[curr_z][curr_rLevel][curr_subbandBP][curr_blockBP]++;
        while (curr_SL + concat < slopes[curr_z][curr_rLevel][curr_subbandBP][curr_blockBP].length - 1
                && curr_slope <= slopes[curr_z][curr_rLevel][curr_subbandBP][curr_blockBP][curr_SL + concat]) {
          currSLs[curr_z][curr_rLevel][curr_subbandBP][curr_blockBP]++;
          slopes[curr_z][curr_rLevel][curr_subbandBP][curr_blockBP][curr_SL + concat] = 0f;
          concat++;
        }
      }

      if (curr_slope > 0f) {
        // print prepared for the extractor
        int maxSL = (curr_blockBP + minsBP[curr_z][curr_rLevel][curr_subbandBP]) * 3 - 2;
        int globalSL = maxSL - curr_SL;

        int numBitPlanes = (curr_blockBP + minsBP[curr_z][curr_rLevel][curr_subbandBP]); // num bit planes
        long inClassIdentifier = -1; //inClassIdentifierCorrespondence[component][curr_rLevel][precIndex];
        blockOrders.add(new BlockOrder(inClassIdentifier, tile, components[curr_z], curr_rLevel, curr_subbandBP, numBitPlanes, globalSL, concat));
        //System.out.println(curr_rLevel+"\t"+curr_subbandBP+"\t"+(curr_blockBP + minsBP[curr_rLevel][curr_subbandBP])+"\t"+globalSL+"\t"+concat+"\t"+numOrder+"\t"+maxSL+"\t"+curr_blockBP+"\t"+minsBP[curr_rLevel][curr_subbandBP]);
      }

    } while (curr_slope > 0f);

    return blockOrders;
  }

  /**
   *
   * @param order
   */
  private ArrayList<CodingPassID> adjustScanningOrder(ArrayList<BlockOrder> blockOrders) {

    ArrayList<CodingPassID> orderOfCodingPasses = new ArrayList<CodingPassID>();

    int totalNumCodingPasses = (MSBPlane + getMaxSubbandWeight()) * 3 + 1;

    // INCLUSION FOLLOWING THE SUBBAND SCANNING ORDER
    //System.out.println("NO\tZ\tRL\tP\tSB\tYB\tXB\tCL"); // DEBUG
    //System.out.println("--\t-\t--\t-\t--\t--\t--\t--"); // DEBUG
    int currOrder = 0;
    for (BlockOrder blockOrder : blockOrders) {
      //long inClassIdentifier = blockOrder.inClassIdentifier;
      int tile = blockOrder.tile;
      int component = blockOrder.component;
      int rLevel = blockOrder.rLevel;
      int subbandBP = blockOrder.subband;
      int bitPlanes = blockOrder.numBitPlanes;
      int codingPass = blockOrder.virtualCodingLevel;
      int concat = blockOrder.concat;
      //System.out.println(currOrder+"\t"+component+"\t"+rLevel+"\t"+subbandBP+"\t"+bitPlanes+"\t"+codingPass+"\t"+concat);

      //blockOrder.list(System.out);
      int minSubband = subbandBP == 0 ? 0 : 2;
      int maxSubband = subbandBP == 0 ? (rLevel > 0 ? 2 : 1) : 3;

      for (int precIndex = 0; precIndex < precinctsPerRLevel[component][rLevel].length; precIndex++) {

        int precinct = precinctsPerRLevel[component][rLevel][precIndex];
        long inClassIdentifier = inClassIdentifierCorrespondence[component][rLevel][precIndex];
        int[][][][] codingPassesLengths = logicalTarget.getLengthsOfCodingPasses(inClassIdentifier); //.getLengthsOfCodingPasses(component, rLevel, precinct);

        if (codingPassesLengths != null) {
          for (int subband = minSubband; subband < maxSubband; subband++) {
            int rateDistortionAdjustment = this.getSubbandWeight(tile, component, rLevel, subband);

            if (codingPassesLengths[subband][0][0] != null) {
              int numBitPlanesBlock = (codingPassesLengths[subband][0][0].length) / 3 + 1 + rateDistortionAdjustment;

              if (numBitPlanesBlock == bitPlanes) {
                for (int i = 0; i < concat; i++) {
                  int codingLevelGlobal = totalNumCodingPasses - (codingPass - i);
                  //System.out.println(currOrder+"\t"+component+"\t"+rLevel+"\t"+precinct+"\t"+subband+"\t"+0+"\t"+0+"\t"+codingPass);
                  //System.out.println((currOrder++)+"\t"+component+"\t"+rLevel+"\t"+precinct+"\t"+ subband+"\t"+"0"+"\t"+"0"+"\t"+(totalNumCodingPasses - (codingPass - i)));

                  int yBlock = 0;
                  int xBlock = 0;
                  //System.out.println(currOrder+"\t"+component+"\t"+rLevel+"\t"+precinct+"\t"+ subband+"\t"+yBlock+"\t"+xBlock+"\t"+(totalNumCodingPasses - (codingPass - i))+"\t"+concat);
                  includeCP(orderOfCodingPasses, inClassIdentifier, tile, component, rLevel, precinct, subband, yBlock, xBlock, codingLevelGlobal);
                }

              }

            }
          }
        }
      }
      currOrder++;
    }

    return orderOfCodingPasses;
  }

  /**
   *
   * @param blockOrder
   * @param inClassIdentifier
   * @param component
   * @param rLevel
   * @param precinct
   * @param yBlock
   * @param xBlock
   * @param codingLevel
   * @return
   */
  private ArrayList<CodingPassID> includeCP(ArrayList<CodingPassID> orderOfCodingPasses, long inClassIdentifier, int tile, int component, int rLevel, int precinct, int subband, int yBlock, int xBlock, int codingLevelGlobal) {

    //System.out.println("\t"+component+"\t"+rLevel+"\t"+precinct+"\t"+ subband+"\t"+yBlock+"\t"+xBlock);

    //Calculate the rate distortion adjustment
    int rateDistortionAdjustment = this.getSubbandWeight(tile, component, rLevel, subband);

    int[][][][] codingPassesLengths = logicalTarget.getLengthsOfCodingPasses(inClassIdentifier);
    int totalNumCodingPasses = (MSBPlane + this.getMaxSubbandWeight()) * 3 + 1;

    if (codingPassesLengths[subband][yBlock][xBlock] != null) {

      int MSBPlaneBlock = (codingPassesLengths[subband][yBlock][xBlock].length) / 3;

      //Check if we can include this coding pass
      int numCodingPassesBlock = (MSBPlaneBlock + rateDistortionAdjustment) * 3 + 1;
      int codingPassBlock = (numCodingPassesBlock - totalNumCodingPasses) + codingLevelGlobal;
      //System.out.println("\trda="+rateDistortionAdjustment+" BPB="+MSBPlaneBlock+" #CPB="+numCodingPassesBlock+" cpb="+codingPassBlock);

      if (codingPassBlock >= 0) {
        if (codingPassBlock < numCodingPassesBlock) {

          int numBitPlanesBlock = (codingPassesLengths[subband][yBlock][xBlock].length + 2) / 3;
          int bitPlane = (codingPassBlock + 2) / 3;
          int codingPass = (codingPassBlock + 2) % 3;
          //System.out.println("\t\tbitPlane="+bitPlane+" codingPass="+codingPass+" #BPB="+(MSBPlaneBlock+1)+" CPBlock="+codingPassBlock);

          if (bitPlane < MSBPlaneBlock + 1) {
            if (codingPassBlock < codingPassesLengths[subband][yBlock][xBlock].length) {
              //System.out.println("\t"+component+"\t"+rLevel+"\t"+precinct+"\t"+ subband+"\t"+yBlock+"\t"+xBlock+"\t"+codingPassBlock+"\t->"+codingPassesLengths[subband][yBlock][xBlock][codingPassBlock]);
              if (codingPassesLengths[subband][yBlock][xBlock][codingPassBlock] >= 0) {
                //System.out.println("\tlength="+codingPassesLengths[subband][yBlock][xBlock][codingPass]+"\n");
                orderOfCodingPasses.add(new CodingPassID(inClassIdentifier, tile, component, rLevel, precinct, subband, yBlock, xBlock, codingPassBlock));
              }
            }
          }
        }
      }
    }

    return orderOfCodingPasses;
  }

  /**
   * This method is used to deliver the requested window of interest when the
   * maximum response length parameter has been set.
   * This method, in order to keep the right scanning order given by the CoRD
   * algorithm, sends only one coding-pass per packet.
   *
   * @param scanningOrder is an ArrayList with the order of precincts
   * 		including the subband, yBlock, xBlock and coding passes to be sent.
   *
   * @throws ErrorException
   * @throws IOException
   */
  private void responsePerLength(ArrayList<CodingPassID> scanningOrder) throws ErrorException {

    // Used to save the temporary offset for each data-bin
    HashMap<Long, Long> dataBinOffsets = new HashMap<Long, Long>();

    // Set the first layer
    int[][][][][][] firstLayer = setFirstLayer(scanningOrder);

    // Initalise packet headers
    packetHeading = new PacketHeadersEncoder();

    // LOOP ON PRECINCT-SUBBAND-YBLOCK-XBLOCK-CODINGPASS
    //System.out.println("NO\tZ\tRL\tP\tSB\tYB\tXB\tCL\tLY\tLength"); // DEBUG
    //System.out.println("--\t-\t--\t-\t--\t--\t--\t--\t--\t---"); // DEBUG
    // Include the precincts that have a contribution in this quality layer
    CodingPassID codingPassID = null;
    boolean finish = false;
    int scanningOrderSize = scanningOrder.size();
    for (int index = 0; index < scanningOrderSize && !finish; index++) {
      codingPassID = scanningOrder.get(index);
      long inClassIdentifier = codingPassID.inClassIdentifier;
      int tile = codingPassID.tile;
      int component = codingPassID.component;
      int rLevel = codingPassID.rLevel;
      int precinct = codingPassID.precinct;
      int subband = codingPassID.subband;
      int yBlock = codingPassID.yBlock;
      int xBlock = codingPassID.xBlock;
      int codingLevel = codingPassID.codingLevel;
      //int layerOfCodingLevel = codingPassID.layer;
      //int[][][][] codingPassesLengths = logicalTarget.getLengthsOfCodingPasses(component, rLevel, precinct);
      int[][][][] codingPassesLengths = logicalTarget.getLengthsOfCodingPasses(inClassIdentifier);
      //System.out.println(inClassIdentifier);
      //System.out.println("\t"+subband+" cl="+codingLevel+" length="+codingPassesLengths[subband][0][0][codingLevel]);

      // Does the code-block have code blocks?
      if (codingPassesLengths[subband][yBlock][xBlock] == null) {
        continue;
      }


      // SET FIRST LAYER
      if (!packetHeading.isSet(inClassIdentifier)) {
        packetHeading.setZeroBitPlanesAndFirstLayer(inClassIdentifier, logicalTarget.getZeroBitPlanes(inClassIdentifier), firstLayer[component][rLevel][precinct]);
        dataBinOffsets.put(inClassIdentifier, 0L);
      }
      long dataBinOffset = dataBinOffsets.get(inClassIdentifier);


      // SET LENGTHS OF CODING PASSES TO BE SENT
      int lengthOfCodingLevelToSend = 0;
      int[][][][] lengtsToEncode = new int[codingPassesLengths.length][][][];
      for (int sb = 0; sb < codingPassesLengths.length; sb++) {
        lengtsToEncode[sb] = new int[codingPassesLengths[sb].length][][];

        for (int yb = 0; yb < codingPassesLengths[sb].length; yb++) {
          lengtsToEncode[sb][yb] = new int[codingPassesLengths[sb][yb].length][];

          for (int xb = 0; xb < codingPassesLengths[sb][yb].length; xb++) {
            if ((sb == subband) && (yb == yBlock) && (xb == xBlock)) {
              if (codingPassesLengths[sb][yb][xb] != null) {
                lengtsToEncode[sb][yb][xb] = new int[1];
                lengtsToEncode[sb][yb][xb][0] = codingPassesLengths[sb][yb][xb][codingLevel];
                lengthOfCodingLevelToSend += lengtsToEncode[sb][yb][xb][0];
              }
            } else {
              lengtsToEncode[sb][yb][xb] = null;
            }
          }
        }
      }

      //  DATA IN CLIENT CACHE
      int dataBinLengthInClientCache = (int)serverCache.getPrecinctDataBinLength(inClassIdentifier);

      // PACKET HEADERS

      // Build the packet header
      byte[] packetHeader = null;
      try {
        packetHeader = packetHeading.encodePacketHeader(inClassIdentifier, lengtsToEncode);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
        assert (true);
      } finally {
        lengtsToEncode = null;
      }
      int layer = packetHeading.getLastEncodedLayer(inClassIdentifier);

      //	Has the packet header to be included?
      long packetHeaderToSendLength = packetHeader.length;
      long packetHeaderToSendOffset = 0;
      byte[] packetHeaderToSend = null;

      if ((dataBinOffset + packetHeader.length) < dataBinLengthInClientCache) {
        // Client has the packet header => do nothing
        packetHeaderToSendOffset = 0;
        packetHeaderToSendLength = 0;
        packetHeader = null;
      } else if (dataBinOffset < dataBinLengthInClientCache) {
        // Client does not have the whole packet header
        packetHeaderToSendOffset = dataBinLengthInClientCache - dataBinOffset;
        packetHeaderToSendLength = packetHeader.length - packetHeaderToSendOffset;
      } else {
        // Client does not have any byte of packet header
        packetHeaderToSendOffset = 0;
        packetHeaderToSendLength = packetHeader.length;
      }

      // Check byte response limit
      if ((responseLength + packetHeaderToSendLength) > maximumResponseLength) {
        packetHeaderToSendLength = (int)(maximumResponseLength - responseLength);
        EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;
        finish = true;
      }

      // Update response length
      responseLength += packetHeaderToSendLength;

      // Copy packet header to be sent
      if (packetHeaderToSendLength > 0) {
        packetHeaderToSend = Arrays.copyOfRange(packetHeader, (int)packetHeaderToSendOffset, (int)packetHeaderToSendLength);
      }


      // PACKET BODY
      long codingLevelToSendOffset = 0;
      long codingLevelToSendLength = 0;
      long codingLevelToSendFilePointer = 0;
      byte[] jpipMessageBody = null;

      if (!finish) {

        if (lengthOfCodingLevelToSend > 0) {

          long codingLevelFilePointer = logicalTarget.getFilePointerCodingPass(inClassIdentifier, subband, yBlock, xBlock, codingLevel);
          long codingLevelLength = logicalTarget.getLengthOfCodingPass(inClassIdentifier, subband, yBlock, xBlock, codingLevel);

          if ((dataBinOffset + lengthOfCodingLevelToSend) < dataBinLengthInClientCache) {
            // Client has whole coding pass, do nothing
            codingLevelToSendOffset = 0;
            codingLevelToSendLength = 0;
          } else if (dataBinOffset < dataBinLengthInClientCache) {
            // Client does not have the coding pass
            codingLevelToSendOffset = dataBinLengthInClientCache - dataBinOffset;
            codingLevelToSendLength = codingLevelLength - codingLevelToSendOffset;
          } else {
            // Client does not have any byte of the coding pass
            codingLevelToSendOffset = 0;
            codingLevelToSendLength = codingLevelLength;
          }

          // Check byte response limit
          if ((responseLength + codingLevelToSendLength) > maximumResponseLength) {
            codingLevelToSendLength = (int)(maximumResponseLength - responseLength);
            EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;
            finish = true;
          }


          //System.out.print("\t"+codingLevelToSendLength); //DEBUG
          if (codingLevelToSendLength > 0) {
            //BufferedDataInputStream in = logicalTarget.getInputDataSource();
            //in.seek(codingLevelFilePointer);
            //jpipMessageBody = new byte[(int) codingLevelToSendLength];
            //in.readFully(jpipMessageBody, (int) 0, (int) codingLevelToSendLength);
            codingLevelToSendFilePointer = codingLevelFilePointer + codingLevelToSendOffset;
          }

          // Update response length counter
          responseLength += codingLevelToSendLength;
        }
      }

      // SEND PACKET HEADER AND PACKET BODY
      // Send packet header
      if (packetHeaderToSendLength + codingLevelToSendLength > 0) {
        // JPIP message header
        boolean lastByte = (!finish) ? true : false;
        JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.EXTENDED_PRECINCT, inClassIdentifier, dataBinOffset, packetHeaderToSendLength + codingLevelToSendLength, lastByte, layer);
        responseDataList.add(new ResponseData(jpipMessageHeader));

        // Send packet header
        if (packetHeaderToSendLength > 0) {
          ArrayList<byte[]> chunks = new ArrayList<byte[]>();
          chunks.add(packetHeaderToSend);
          responseDataList.add(new ResponseData(chunks));
        }

        // Send packet body
        if (codingLevelToSendLength > 0) {
          ArrayList<Long> filePointers = new ArrayList<Long>();
          filePointers.add(codingLevelToSendFilePointer);
          ArrayList<Integer> lengths = new ArrayList<Integer>();
          lengths.add((int)codingLevelToSendLength);
          responseDataList.add(new ResponseData(filePointers, lengths));
        }
      }

      // Update data-bin offset
      dataBinOffset += (packetHeaderToSendLength + codingLevelToSendLength);
      dataBinOffsets.put(inClassIdentifier, dataBinOffset);

      // Check to finish, break loop on precincts - coding passes
      if (finish) {
        break;
      }


    } // Loop on precincts

  }

  /**
   * This method is used to deliver the requested window of interest when the
   * layers parameter of the view window has been set.
   * This method, in order to keep the right scanning order given by the CoRD
   * algorithm, sends only one coding-pass per packet.
   *
   * @param scanningOrder
   * @throws ErrorException
   */
  private void responsePerQualityLayer(ArrayList<CodingPassID> scanningOrder) throws ErrorException {

    // Used to save the temporary offset for each data-bin
    HashMap<Long, Long> dataBinOffsets = new HashMap<Long, Long>();

    // Set the first layer
    int[][][][][][] firstLayer = setFirstLayer(scanningOrder);

    // Initalise packet headers
    packetHeading = new PacketHeadersEncoder();


    // LOOP ON PRECINCT-SUBBAND-YBLOCK-XBLOCK-CODINGPASS
    //System.out.println("NO\tZ\tRL\tP\tSB\tYB\tXB\tCL\tLY\tLength"); // DEBUG
    //System.out.println("--\t-\t--\t-\t--\t--\t--\t--\t--\t---"); // DEBUG
    // Include the precincts that have a contribution in this quality layer
    CodingPassID codingPassID = null;
    boolean finish = false;
    int scanningOrderSize = scanningOrder.size();
    for (int index = 0; index < scanningOrderSize && !finish; index++) {
      codingPassID = scanningOrder.get(index);

      long inClassIdentifier = codingPassID.inClassIdentifier;
      int tile = codingPassID.tile;
      int component = codingPassID.component;
      int rLevel = codingPassID.rLevel;
      int precinct = codingPassID.precinct;
      int subband = codingPassID.subband;
      int yBlock = codingPassID.yBlock;
      int xBlock = codingPassID.xBlock;
      int codingLevel = codingPassID.codingLevel;
      //int layerOfCodingLevel = codingPassID.layer;
      //int[][][][] codingPassesLengths = logicalTarget.getLengthsOfCodingPasses(component, rLevel, precinct);
      int[][][][] codingPassesLengths = logicalTarget.getLengthsOfCodingPasses(inClassIdentifier);
      //System.out.println("InClassID="+inClassIdentifier);

      // Does the code-block have code blocks?
      if (codingPassesLengths[subband][yBlock][xBlock] == null) {
        continue;
      }


      // SET FIRST LAYER
      if (!packetHeading.isSet(inClassIdentifier)) {
        packetHeading.setZeroBitPlanesAndFirstLayer(inClassIdentifier, logicalTarget.getZeroBitPlanes(inClassIdentifier), firstLayer[component][rLevel][precinct]);
        dataBinOffsets.put(inClassIdentifier, 0L);
      }
      long dataBinOffset = dataBinOffsets.get(inClassIdentifier);

      // Check if next layer is lower than max. requested layer
      if (packetHeading.getLastEncodedLayer(inClassIdentifier) > layers) {
        continue;
      }


      // SET LENGTHS OF CODING PASSES TO BE SENT
      int lengthOfCodingLevelToSend = 0;
      int[][][][] lengtsToEncode = new int[codingPassesLengths.length][][][];
      for (int sb = 0; sb < codingPassesLengths.length; sb++) {
        lengtsToEncode[sb] = new int[codingPassesLengths[sb].length][][];

        for (int yb = 0; yb < codingPassesLengths[sb].length; yb++) {
          lengtsToEncode[sb][yb] = new int[codingPassesLengths[sb][yb].length][];

          for (int xb = 0; xb < codingPassesLengths[sb][yb].length; xb++) {
            if ((sb == subband) && (yb == yBlock) && (xb == xBlock)) {
              if (codingPassesLengths[sb][yb][xb] != null) {
                lengtsToEncode[sb][yb][xb] = new int[1];
                //System.out.println("\tsb="+sb+" yb="+yb+" xb="+xb+" cl="+codingLevel);
                lengtsToEncode[sb][yb][xb][0] = (int)codingPassesLengths[sb][yb][xb][codingLevel];
                lengthOfCodingLevelToSend += lengtsToEncode[sb][yb][xb][0];
              }
            } else {
              lengtsToEncode[sb][yb][xb] = null;
            }
          }
        }
      }

      //  DATA IN CLIENT CACHE
      int dataBinLengthInClientCache = (int)serverCache.getPrecinctDataBinLength(inClassIdentifier);

      // PACKET HEADERS
      //System.out.println(index+"\t"+component+"\t"+rLevel+"\t"+"0"+"\t"+subband+"\t"+yBlock+"\t"+xBlock+"\t"+codingPassID.codingLevel); // DEBUG
      // Build the packet header
      byte[] packetHeader = null;
      try {
        packetHeader = packetHeading.encodePacketHeader(inClassIdentifier, lengtsToEncode);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
        assert (true);
      } finally {
        lengtsToEncode = null;
      }
      int layer = packetHeading.getLastEncodedLayer(inClassIdentifier);

      //	Has the packet header to be included?
      long packetHeaderToSendLength = packetHeader.length;
      long packetHeaderToSendOffset = 0;
      byte[] packetHeaderToSend = null;

      if ((dataBinOffset + packetHeader.length) < dataBinLengthInClientCache) {
        // Client has the packet header => do nothing
        packetHeaderToSendOffset = 0;
        packetHeaderToSendLength = 0;
        packetHeader = null;
      } else if (dataBinOffset < dataBinLengthInClientCache) {
        // Client does not have the whole packet header
        packetHeaderToSendOffset = dataBinLengthInClientCache - dataBinOffset;
        packetHeaderToSendLength = packetHeader.length - packetHeaderToSendOffset;
      } else {
        // Client does not have any byte of packet header
        packetHeaderToSendOffset = 0;
        packetHeaderToSendLength = packetHeader.length;
      }

      // Check byte response limit
      if ((responseLength + packetHeaderToSendLength) > maximumResponseLength) {
        packetHeaderToSendLength = (int)(maximumResponseLength - responseLength);
        EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;
        finish = true;
      }

      // Update response length
      responseLength += packetHeaderToSendLength;

      // Send packet header
      if (packetHeaderToSendLength > 0) {
        // Copy packet header to be sent
        packetHeaderToSend = Arrays.copyOfRange(packetHeader, (int)packetHeaderToSendOffset, (int)packetHeaderToSendLength);
      }


      // PACKET BODY
      long codingLevelToSendOffset = 0;
      long codingLevelToSendLength = 0;
      long codingLevelToSendFilePointer = 0;
      byte[] jpipMessageBody = null;

      if (!finish) {

        if (lengthOfCodingLevelToSend > 0) {

          long codingLevelFilePointer = logicalTarget.getFilePointerCodingPass(inClassIdentifier, subband, yBlock, xBlock, codingLevel);
          long codingLevelLength = logicalTarget.getLengthOfCodingPass(inClassIdentifier, subband, yBlock, xBlock, codingLevel);

          if ((dataBinOffset + lengthOfCodingLevelToSend) < dataBinLengthInClientCache) {
            // Client has whole coding pass, do nothing
            codingLevelToSendOffset = 0;
            codingLevelToSendLength = 0;
          } else if (dataBinOffset < dataBinLengthInClientCache) {
            // Client does not have the coding pass
            codingLevelToSendOffset = dataBinLengthInClientCache - dataBinOffset;
            codingLevelToSendLength = codingLevelLength - codingLevelToSendOffset;
          } else {
            // Client does not have any byte of the coding pass
            codingLevelToSendOffset = 0;
            codingLevelToSendLength = codingLevelLength;
          }

          // Check byte response limit
          if ((responseLength + codingLevelToSendLength) > maximumResponseLength) {
            codingLevelToSendLength = (int)(maximumResponseLength - responseLength);
            EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;
            finish = true;
          }

          //System.out.print("\t"+codingLevelToSendLength); //DEBUG
          if (codingLevelToSendLength > 0) {
            //BufferedDataInputStream in = logicalTarget.getInputDataSource();
            //in.seek(codingLevelFilePointer);
            codingLevelToSendFilePointer = codingLevelFilePointer + codingLevelToSendOffset;
            //jpipMessageBody = new byte[(int) codingLevelToSendLength];
            //in.readFully(jpipMessageBody, (int) 0, (int) codingLevelToSendLength);
          }

          // Update response length counter
          responseLength += codingLevelToSendLength;
        }
      }


      // SEND PACKET HEADER AND PACKET BODY
      if (packetHeaderToSendLength + codingLevelToSendLength > 0) {
        // JPIP message header
        boolean lastByte = (!finish) ? true : false;
        JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.EXTENDED_PRECINCT, inClassIdentifier, dataBinOffset, packetHeaderToSendLength + codingLevelToSendLength, lastByte, layer);
        responseDataList.add(new ResponseData(jpipMessageHeader));

        // Send packet header
        if (packetHeaderToSendLength > 0) {
          ArrayList<byte[]> chunks = new ArrayList<byte[]>();
          chunks.add(packetHeaderToSend);
          responseDataList.add(new ResponseData(chunks));
        }

        // Send packet body
        if (codingLevelToSendLength > 0) {
          ArrayList<Long> filePointers = new ArrayList<Long>();
          filePointers.add(codingLevelToSendFilePointer);
          ArrayList<Integer> lengths = new ArrayList<Integer>();
          lengths.add((int)codingLevelToSendLength);
          responseDataList.add(new ResponseData(filePointers, lengths));
        }
      }

      // Update data-bin offset
      dataBinOffset += (packetHeaderToSendLength + codingLevelToSendLength);
      dataBinOffsets.put(inClassIdentifier, dataBinOffset);


      // Check to finish, break loop on precincts - coding passes
      if (finish) {
        break;
      }

    } // Loop on precincts
  }

  /**
   *
   * @param scanningOrder
   * @return
   */
  private int[][][][][][] setFirstLayer(ArrayList<CodingPassID> scanningOrder) {

    // System.out.println("**************************************************************************");
    // System.out.println("                      ADJUSTING FIRST LAYER");
    // System.out.println("**************************************************************************");

    ServerJPEG2KTile tileObj = codestream.getTile(0);
    ServerJPEG2KComponent compObj = null;
    ServerJPEG2KResolutionLevel rLevelObj = null;
    ServerJPEG2KPrecinct precinctObj = null;
    
    // Saves the first layer for each precinct (temporal structure)
    // Indixes mean: firstLayerInPrecinct[z][rLevel][precIndex]
    int[][][] actualLayerInPrecinct = new int[codestream.getZSize()][][];

    // Indixes mean: lastAvailableLayer[z][rLevel][precIndex]
    int[][][] lastAvailableLayer = null;

    // Saves the first coding level for each code block (temporal structure)
    // Indixes mean:
    // firstCodingLevel[z][rLevel][precinct][subband][yBlock][xBlock]
    int[][][][][][] firstCodingLevel = new int[codestream.getZSize()][][][][][];
    int[][][][][][] firstLayer = null;

    lastAvailableLayer = new int[codestream.getZSize()][][];

    firstLayer = new int[codestream.getZSize()][][][][][];
    for (int z = 0; z < firstLayer.length; z++) {
      compObj = tileObj.getComponent(z);
      int rLevels = compObj.getWTLevels() +1;
      
      actualLayerInPrecinct[z] = new int[rLevels][];
      firstCodingLevel[z] = new int[rLevels][][][][];
      lastAvailableLayer[z] = new int[rLevels][];
      firstLayer[z] = new int[rLevels][][][][];

      for (int rLevel = 0; rLevel < rLevels; rLevel++) {
        rLevelObj = compObj.getResolutionLevel(rLevel);
        int numPrecincts = rLevelObj.getNumPrecincts();
        
        actualLayerInPrecinct[z][rLevel] = new int[numPrecincts];
        firstCodingLevel[z][rLevel] = new int[numPrecincts][][][];
        lastAvailableLayer[z][rLevel] = new int[numPrecincts];
        firstLayer[z][rLevel] = new int[numPrecincts][][][];

        Arrays.fill(actualLayerInPrecinct[z][rLevel], -1);
        Arrays.fill(lastAvailableLayer[z][rLevel], -1);

        for (int precinct = 0; precinct < numPrecincts; precinct++) {
          precinctObj = rLevelObj.getPrecinct(precinct);
          int numSubbands = precinctObj.getNumSubbands();
          
          firstCodingLevel[z][rLevel][precinct] = new int[numSubbands][][];
          firstLayer[z][rLevel][precinct] = new int[numSubbands][][];

          for (int subband = 0; subband < numSubbands; subband++) {
            int numYBlocks = precinctObj.getNumBlocksHigh(subband);
            int numXBlocks = precinctObj.getNumBlocksWide(subband);
            firstCodingLevel[z][rLevel][precinct][subband] = new int[numYBlocks][numXBlocks];
            firstLayer[z][rLevel][precinct][subband] = new int[numYBlocks][numXBlocks];

            for (int yBlock = 0; yBlock < numYBlocks; yBlock++) {
              Arrays.fill(firstCodingLevel[z][rLevel][precinct][subband][yBlock], -1);
              Arrays.fill(firstLayer[z][rLevel][precinct][subband][yBlock], this.maxNumLayers + 1);
            }
          }
        }
      }
    }

    // System.out.println("NO\tZ\tRL\tP\tSB\tYB\tXB\tCL\tL"); // DEBUG
    // System.out.println("--\t-\t--\t-\t--\t--\t--\t--\t-"); // DEBUG

    CodingPassID codingPassID = null;
    int lastIndex = scanningOrder.size();
    for (int index = 0; index < lastIndex; index++) {
      codingPassID = scanningOrder.get(index);

      int component = codingPassID.component;
      int rLevel = codingPassID.rLevel;
      int precinct = codingPassID.precinct;
      int subband = codingPassID.subband;
      int yBlock = codingPassID.yBlock;
      int xBlock = codingPassID.xBlock;
      int codingLevel = codingPassID.codingLevel;

      if (firstCodingLevel[component][rLevel][precinct][subband][yBlock][xBlock] < 0) {
        firstCodingLevel[component][rLevel][precinct][subband][yBlock][xBlock] = codingLevel;
      }

      if (actualLayerInPrecinct[component][rLevel][precinct] < 0) {
        actualLayerInPrecinct[component][rLevel][precinct] = 0;
        firstLayer[component][rLevel][precinct][subband][yBlock][xBlock] = actualLayerInPrecinct[component][rLevel][precinct];

      } else if (firstLayer[component][rLevel][precinct][subband][yBlock][xBlock] > maxNumLayers) {
        if (rLevel == 0) {
          actualLayerInPrecinct[component][rLevel][precinct]++;
        } else {
          actualLayerInPrecinct[component][rLevel][precinct]++;
        }
        firstLayer[component][rLevel][precinct][subband][yBlock][xBlock] = actualLayerInPrecinct[component][rLevel][precinct];

      } else {
        // do nothing
        if (rLevel == 0) {
          actualLayerInPrecinct[component][rLevel][precinct]++;
        } else {
          actualLayerInPrecinct[component][rLevel][precinct]++;
        }
      }

      codingPassID.codingLevel = codingPassID.codingLevel - firstCodingLevel[component][rLevel][precinct][subband][yBlock][xBlock];
      codingPassID.layer = actualLayerInPrecinct[component][rLevel][precinct];
      lastAvailableLayer[component][rLevel][precinct] = actualLayerInPrecinct[component][rLevel][precinct];

      // System.out.println(index+"\t"+component+"\t"+rLevel+"\t"+"0"+"\t"+subband+"\t"+yBlock+"\t"+xBlock+"\t"+codingPassID.codingLevel+"\t"+actualLayerInPrecinct[component][rLevel][precinct]);
      // // DEBUG
    }

    actualLayerInPrecinct = null;
    firstCodingLevel = null;

    return firstLayer;
  }

  /**
   * Gets the relevant precincts for each subband.
   *
   * @param relevantPrecincts
   *            an array list with the unique precinct identifier of the
   *            relevant precincts.
   */
  public void getPrecinctsPerRLevel(ArrayList relevantPrecincts) {

    int numRelevantPrecincts = relevantPrecincts.size();

    // Find maximum resolution levels
    // Index means: maxResolutionLevel[z]
    int[] maxResolutionLevel = new int[codestream.getZSize()];
    Arrays.fill(maxResolutionLevel, 0);

    // Number of precincts per each resolution level
    // Indexes mean: numPrecinctsPerRLevel[z][rLevel]
    int[][] numPrecinctsPerRLevel = new int[codestream.getZSize()][];
    for (int z = 0; z < codestream.getZSize(); z++) {
      numPrecinctsPerRLevel[z] = new int[codestream.getTile(0).getComponent(z).getWTLevels() + 1];
      Arrays.fill(numPrecinctsPerRLevel[z], 0);
    }

    // Find the number of relevant precincts per each resolution level
    for (int precIndex = 0; precIndex < numRelevantPrecincts; precIndex++) {
      long inClassIdentifier = (Long)relevantPrecincts.get(precIndex);
      int[] TCRP = codestream.findTCRP(inClassIdentifier);

      // int tile = TCRP[0];
      int z = TCRP[1];
      int rLevel = TCRP[2];
      numPrecinctsPerRLevel[z][rLevel]++;
      if (maxResolutionLevel[z] < (rLevel + 1)) {
        maxResolutionLevel[z] = rLevel + 1;
      }
    }


    // Allocates memory. This multi-array indicates which are the relevant
    // precincts for each resolution level
    // Indixes mean: precinctsPerRLevel[z][rLevel][precinct]
    // Indixes mean: inClassIdentifierCorrespondence[z][rLevel][precinct]
    // Indixes mean: rLevelIndex[z][rLevel]
    int zSize = codestream.getZSize();
    precinctsPerRLevel = new int[zSize][][];
    inClassIdentifierCorrespondence = new long[zSize][][];
    int[][] rLevelIndex = new int[zSize][];
    for (int z = 0; z < maxResolutionLevel.length; z++) {
      precinctsPerRLevel[z] = new int[maxResolutionLevel[z]][];
      inClassIdentifierCorrespondence[z] = new long[maxResolutionLevel[z]][];
      rLevelIndex[z] = new int[maxResolutionLevel[z]];
      Arrays.fill(rLevelIndex[z], 0);
      for (int rLevel = 0; rLevel < maxResolutionLevel[z]; rLevel++) {
        precinctsPerRLevel[z][rLevel] = new int[numPrecinctsPerRLevel[z][rLevel]];
        inClassIdentifierCorrespondence[z][rLevel] = new long[numPrecinctsPerRLevel[z][rLevel]];
      }
    }

    // Set the relevant precinct for each resolution level
    for (int precIndex = 0; precIndex < numRelevantPrecincts; precIndex++) {
      long inClassIdentifier = (Long)relevantPrecincts.get(precIndex);
      int[] TCRP = codestream.findTCRP(inClassIdentifier);

      // int tile = TCRP[0];
      int z = TCRP[1];
      int rLevel = TCRP[2];
      int precinct = TCRP[3];

      precinctsPerRLevel[z][rLevel][rLevelIndex[z][rLevel]] = precinct;
      inClassIdentifierCorrespondence[z][rLevel][rLevelIndex[z][rLevel]] = inClassIdentifier;
      rLevelIndex[z][rLevel]++;
    }

    // DEBUG
		/*System.out.println("RELEVANT PRECINCTS");
    for (int z = 0; z < precinctsPerRLevel.length; z++) {
    for (int rLevel = 0; rLevel < precinctsPerRLevel[z].length; rLevel++) {
    for (int precinct = 0; precinct < precinctsPerRLevel[z][rLevel].length; precinct++) {
    System.out.println("\tz=" + z + " rLevel=" + rLevel + " precinct=" + precinctsPerRLevel[z][rLevel][precinct]);
    } } }*/
    // END DEBUG
    numPrecinctsPerRLevel = null;
  }

  /**
   * Construction of two structures which contain the max and min number of
   * bit-planes of the code-blocks, by subbands Indexes mean:
   * [component][rLevel][subbandBP]
   *
   * @param precinctsPerRLevel
   * @param maxsBP
   * @param minsBP
   */
  public void calculateMaxMinBitPlanes(int[][][] precinctsPerRLevel, int[][][] maxsBP, int[][][] minsBP, long[][][] inClassIdentifierCorrespondence, int[] components) {

    int tile = 0;

    // Initialization
    for (int z = 0; z < maxsBP.length; z++) {
      for (int rLevel = 0; rLevel < maxsBP[z].length; rLevel++) {
        for (int subband = 0; subband < maxsBP[z][rLevel].length; subband++) {
          maxsBP[z][rLevel][subband] = Integer.MIN_VALUE;
          minsBP[z][rLevel][subband] = Integer.MAX_VALUE;
        }
      }
      // Just for the rLevel==0, which does not have subbandBP==1
      maxsBP[z][0][1] = 1;
      minsBP[z][0][1] = 1;
    }

    // Search
    for (int z = 0; z < maxsBP.length; z++) {
      int component = components[z];
      for (int rLevel = 0; rLevel < precinctsPerRLevel[component].length; rLevel++) {
        for (int precIndex = 0; precIndex < precinctsPerRLevel[component][rLevel].length; precIndex++) {
          int precinct = precinctsPerRLevel[component][rLevel][precIndex];
          long inClassIdentifier = codestream.getTile(tile).getComponent(component).getResolutionLevel(rLevel).getInClassIdentifier(precinct);
          int[][][][] codingPassesLengths = logicalTarget.getLengthsOfCodingPasses(inClassIdentifier);
          if (codingPassesLengths != null) {
            for (int subband = 0; subband < codingPassesLengths.length; subband++) {
              int subbandBP = subband < 2 ? 0 : 1;
              int rateDistortionAdjustment = getSubbandWeight(tile, component, rLevel, subband);
              for (int yBlock = 0; yBlock < codingPassesLengths[subband].length; yBlock++) {
                for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length; xBlock++) {
                  // Number of bit-planes of this code-block
                  int blockBP = logicalTarget.getMSBPlane(inClassIdentifier, subband, yBlock, xBlock) + 1 + rateDistortionAdjustment;
                  if (maxsBP[z][rLevel][subbandBP] < blockBP) {
                    maxsBP[z][rLevel][subbandBP] = blockBP;
                  }
                  if (minsBP[z][rLevel][subbandBP] > blockBP) {
                    minsBP[z][rLevel][subbandBP] = blockBP;
                  }
                  //System.out.println("z="+z+" p="+precinct+" r="+rLevel+" s="+subband+" yb="+yBlock+" xb="+xBlock+" MSBP="+logicalTarget.getMSBPlane(z, rLevel, precinct, subband, yBlock, xBlock)+" rda="+rateDistortionAdjustment);
                }
              }
            }
          }
        }
      }
    }

    // DEBUG
		/*System.out.println("comp\trLevel\tsubband\t: maxsBP  minsBP");
    for (int z = 0; z < maxsBP.length; z++) {
    int component = components[z];
    for(int rLevel=0;rLevel<maxsBP[z].length;rLevel++){
    for(int subband=0;subband<maxsBP[z][rLevel].length;subband++){
    System.out.println("  "+component+"\t "+rLevel+"\t "+subband+"\t:   "+maxsBP[z][rLevel][subband]+"\t    "+minsBP[z][rLevel][subband]);
    }
    }
    }
    System.out.println("comp\trLevel\tsubband\t: maxsBP  minsBP");
    System.out.println("----\t------\t-------\t: ------  ------");
    for (int z = 0; z < maxsBP.length; z++) {
    int component = components[z];
    for(int rLevel = 0; rLevel < maxsBP[z].length; rLevel++){
    for(int subband = 0; subband < maxsBP[z][rLevel].length; subband++) {
    System.out.println("  "+component+"\t"+rLevel+"\t   "+subband+"\t:   "+maxsBP[z][rLevel][subband]+"\t    "+minsBP[z][rLevel][subband]);
    }
    }
    }
    System.out.println();*/
    // END DEBUG

  }

  /**
   * Useful method for printing a byte array. Only for debugging purposes.
   *
   * @param buffer
   *            the byte array to be printed.
   */
  private static void printByteArray(byte[] buffer) {
    for (int index = 0; index < buffer.length; index++) {
      if ((0xFF & buffer[index]) < 16) {
        System.out.print("0");
      }
      System.out.print(Integer.toHexString(0xFF & buffer[index]));
    }
  }
}
