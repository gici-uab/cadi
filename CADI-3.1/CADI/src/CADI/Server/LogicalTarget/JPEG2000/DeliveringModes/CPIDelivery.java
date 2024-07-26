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
import CADI.Common.Network.JPIP.*;
import CADI.Server.Cache.ServerCacheModel;
import CADI.Server.Core.ResponseData;
import CADI.Server.LogicalTarget.JPEG2000.JP2KServerLogicalTarget;
import CADI.Server.LogicalTarget.JPEG2000.Codestream.JPCMainHeaderEncoder;
import CADI.Server.LogicalTarget.JPEG2000.Codestream.JPKMainHeaderEncoder;
import CADI.Server.LogicalTarget.JPEG2000.Codestream.PacketHeadersEncoder;
import CADI.Server.LogicalTarget.JPEG2000.ServerJPEG2KCodestream;
import CADI.Server.LogicalTarget.JPEG2000.ServerJPEG2KComponent;
import CADI.Server.LogicalTarget.JPEG2000.ServerJPEG2KTile;
import GiciException.ErrorException;
import GiciStream.ByteStream;

/**
 * This class implements the Coding Passes Interleaving (CPI) rate-distortion
 * method.
 * <p>
 * Four CPI approaches have been developed:<br>
 * <dl>
 * <dt>PACKET PER PRECINCT</dt>
 * <dd>This approach delivery the requested window of interest using only one
 * 		packet per precinct. Therefore, the served image only has one quality
 * 		layer. See {@link #deliverOnePacketPerPrecinct()} method. 
 * 
 * <dt>PACKET PER BIT-PLANE</dt>
 * <dd>This approach delivery the requested window of interest building one
 * 		packet per bit-plane. Therefore, the served served image has as quality
 * 		layers as number of bit-planes.
 * 		See {@link #deliverOnePacketPerBitPlane()} method.
 *  
 * <dt>PACKET PER CODING PASS</dt>
 * <dd>This approach delivery the requested window of interest building one
 * 		packet per coding pass. Therefore, the served served image has as
 * 		quality layers as number of coding passes (3 * bit-planes + 1).
 * 		See {@link #deliverOnePacketPerCodingPass()} method.
 * 
 * <dt>SCALE</dt>
 * <dd>This approach delivery the requested window of interest following the
 * 		the SCALE rate-distortion method, where the MRP and CP coding passes
 * 		are joined in the same packet. Therefore, the served served image has
 * 		((K * 2) - 1) quality layers (where K = number of bit-planes).
 * 		See {@link #getResponseScale()} method.
 * 
 * </dl>
 *
 * Moreover, for each CPI approach two complementary alternatives may be
 * applied, individualy or combined:
 * <dl>
 * <dt>APPLY_SUBBAND_WEIGHTS</dt>
 * <dd>
 * <dt>PRECINCT_REORDERING</dt>
 * <dd>Performs a reordering of the precincts at the same coding level and
 * 	resolution level. Packets are rearranged in increasing packet's lengths.
 * <dt>PRECINCT_REORDERING_RLEVEL</dt>
 * <dd>Performs a reordering of the precincts at the same coding level. Packets
 * 	are rearranged in increasing packet's lengths.
 * <dl>
 *
 * <p>
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
 * @version 2.0.0 2009/04/13
 */
public class CPIDelivery {

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
   * Indicates which type of CPI will be used to deliver images.
   * <p>
   * Allowed values:
   * <lu>
   * 	<li>1- Only one packet for each precinct is generated. Therefore, delivered image has only one quality layer.
   *    <li>2- Creates one packet per each bit plane. Therefore, delivered image has as quality layers as number of bit planes.
   *    <li>3- Creates one packet per each coding pass. Therefore, delivered image has as quality layers as number of coding passes.
   *    <li>4- Images are delivered following the SCALE method.
   *    <li>11- Is based on option 1 but precincts within each resolution level at the same coding pass are sorted.
   *    <li>12- Is based on option 2 but precincts within each resolution level at the same coding pass are sorted.
   *    <li>13- Is based on option 3 but precincts within each resolution level at the same coding pass are sorted.
   *    <li>14- Is based on option 4 but precincts within each resolution level at the same coding pass are sorted.
   *    <li>21- Is based on option 1 but all precincts at the same coding pass are sorted.
   *    <li>22- Is based on option 2 but all precincts at the same coding pass are sorted.
   *    <li>23- Is based on option 3 but all precincts at the same coding pass are sorted.
   *    <li>24- Is based on option 4 but all precincts at the same coding pass are sorted.
   * </lu>
   * Note: the sorting of precincts rearranges them in increasing length.
   */
  private int cpiType = PACKET_PER_CODING_PASS;

  // INTERNAL ATTRIBUTES
  /**
   * It is a temporal attribute to accumulate the response length which is
   * sending to the client.
   */
  private long responseLength = 0;

  /**
   *
   */
  private int discardLevels = -1;

  /**
   *
   */
  private ViewWindowField actualViewWindow = null;

  private ServerJPEG2KTile tileObj = null;

  private ServerJPEG2KComponent compObj = null;

  /**
   * Definition in {@link CADI.Server.LogicalTarget.JPEG2000.JP2LogicalTarget#MSBPlane}
   */
  private int MSBPlane;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#numLayers}
   */
  private int maxNumLayers = -1;

  /**
   * 
   */
  private int layers = -1;

  /**
   * This CPI tye delivery the image's window of interest building only one
   * packet, i.e., all coding passes of a precinct are grouped in one packet.
   * <p>
   * It can be uses when the request belongs to a session or with stateless
   * request.
   */
  private static final int PACKET_PER_PRECINCT = 1;

  /**
   * This CPI tye delivery the image's window of interest by bit planes.
   * <p>
   * Builds a packet for each bit-plane. So coding passes of different
   * bit-planes are not grouped in the same packet.
   * <p>
   * It can be uses when the request belongs to a session or with stateless
   * request.
   */
  private static final int PACKET_PER_BIT_PLANE = 2;

  /**
   * This CPI type delivery the image's window of interest building a layer
   * for each coding pass.
   * <p>
   * It is the worst approach because a packet is built for each coding pass,
   * even when two or more coding passes of the same precinct are sent.
   * <p>
   * It can be uses when the request belongs to a session or with stateless
   * request.
   */
  private static final int PACKET_PER_CODING_PASS = 3;

  /**
   * This CPI type delivery the image's window of interest following the
   * SCALE method:
   * l = L - 1 - ((c div 3) * 2)- ((c mod 3) div 2)
   * where:
   * L = (K * 2) - 1
   * K = number of bit-planes
   * <p>
   * It can be uses when the request belongs to a session or with stateless
   * request.
   */
  private static final int SCALE = 4;

  /**
   * Indicates if a subband weight must be applied.
   * <p>
   * Further information, see {@link #getSubbandWeight(int, int, int)} method.
   */
  private static boolean APPLY_SUBBAND_WEIGHTS = false;

  // ============================= public methods ==============================
  /**
   *
   * @param logicalTarget
   * @param serverCache
   */
  public CPIDelivery(JP2KServerLogicalTarget logicalTarget,
                     ServerCacheModel serverCache) {
    this(logicalTarget, serverCache, false);
  }

  /**
   *
   * @param logicalTarget
   * @param serverCache
   * @param align
   */
  public CPIDelivery(JP2KServerLogicalTarget logicalTarget,
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

    codestream = logicalTarget.getCodestream(0);
    MSBPlane = logicalTarget.getMSBPlane();

    // Set end of response code
    EORReasonCode = EORCodes.WINDOW_DONE;
  }

  /**
   * Set the subtype of the CPI.
   *
   * @param cpiType definition in {@link #cpiType}
   */
  public void setCPIType(int cpiType) {
    this.cpiType = cpiType;
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

    maxNumLayers = getMaxNumLayers(cpiType);

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
    // It is a coarse estimation and it should be improved (but is the easiest method)
    switch (cpiType) {
      case PACKET_PER_PRECINCT:
        quality = (int) (100F / (float) maxNumLayers) * actualViewWindow.layers;
        break;

      case PACKET_PER_CODING_PASS:
        quality = (int) (100F / (float) maxNumLayers) * actualViewWindow.layers;
        break;

      case PACKET_PER_BIT_PLANE:
        quality = (int) (100F / (float) maxNumLayers) * actualViewWindow.layers;
        break;

      case SCALE:
        quality = (int) (100F / (float) maxNumLayers) * actualViewWindow.layers;
        break;

      default: // equal to packet per coding pass
        quality = (int) (100F / (float) maxNumLayers) * actualViewWindow.layers;
    }

  }

  /**
   * Gets the data of the response window of interes. This data is calculate
   * using the CPI algorithm.
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

    // Initializations
    EORReasonCode = EORCodes.WINDOW_DONE;

    // Choose type of CPI
    switch (cpiType) {

      case PACKET_PER_PRECINCT:
        this.maximumResponseLength = (maximumResponseLength == -1)
                ? Long.MAX_VALUE : maximumResponseLength;
        deliverOnePacketPerPrecinct();
        break;

      case PACKET_PER_CODING_PASS:
        this.maximumResponseLength = (maximumResponseLength == -1)
                ? Long.MAX_VALUE : maximumResponseLength;
        deliverOnePacketPerCodingPass();
        break;

      case PACKET_PER_BIT_PLANE:
        this.maximumResponseLength = (maximumResponseLength == -1)
                ? Long.MAX_VALUE : maximumResponseLength;
        deliverOnePacketPerBitPlane();
        break;

      case SCALE:
        this.maximumResponseLength = (maximumResponseLength == -1)
                ? Long.MAX_VALUE : maximumResponseLength;
        deliverScale();
        break;

      default:
        this.maximumResponseLength = (maximumResponseLength == -1)
                ? Long.MAX_VALUE : maximumResponseLength;

        deliverOnePacketPerCodingPass();
    }
  }

  /**
   * Returns the main header.
   *
   * @return the {@link #mainHeader}.
   * @throws ErrorException
   */
  public byte[] getMainHeader() throws ErrorException {
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

    str += "Not implemented yet";

    str += "]";
    return str;
  }

  /**
   * Prints this CPI delivery out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out
   *           an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- CPI delivery --");

    out.println("Not implemented yet");

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * Gets the maximum number of layers.
   *
   * @param cpiType definition in {@link #cpiType}.
   * @return definition in {@link #maxNumLayers}.
   */
  private int getMaxNumLayers(int cpiType) {
    switch (cpiType) {
      case PACKET_PER_PRECINCT:
        maxNumLayers = 1;
        break;

      case PACKET_PER_CODING_PASS:
        maxNumLayers = APPLY_SUBBAND_WEIGHTS
                ? (MSBPlane + getMaxSubbandWeight()) * 3 + 1
                : MSBPlane * 3 + 1;
        break;

      case PACKET_PER_BIT_PLANE:
        maxNumLayers = APPLY_SUBBAND_WEIGHTS
                ? MSBPlane + getMaxSubbandWeight() + 1
                : MSBPlane + 1;
        break;

      case SCALE:
        maxNumLayers = 2 * MSBPlane + 1;
        break;

      default:  // equal to packet per coding pass
        maxNumLayers = APPLY_SUBBAND_WEIGHTS
                ? (MSBPlane + getMaxSubbandWeight()) * 3 + 1
                : MSBPlane * 3 + 1;
    }
    return maxNumLayers;
  }

  /**
   * Gets the main header as a byte array. This method can be called if the
   * transcoding is done.
   *
   * @throws ErrorException
   */
  private void encodeMainHeader() throws ErrorException {
    JPCParameters jpcParameters = codestream.getJPCParameters();
    jpcParameters.codParameters.numLayers = getMaxNumLayers(cpiType);
    try {
      if (jpcParameters.jpkParameters == null) {
        jpcParameters.codParameters.numLayers = maxNumLayers;
        JPCMainHeaderEncoder jpcHeading = new JPCMainHeaderEncoder(jpcParameters);
        mainHeader = jpcHeading.run();

      } else {
        jpcParameters.codParameters.numLayers = maxNumLayers;
        JPKMainHeaderEncoder jpkHeading = new JPKMainHeaderEncoder(jpcParameters.sizParameters, jpcParameters.codParameters, jpcParameters.qcdParameters, jpcParameters.jpkParameters);
        ByteStream mainHeaderByteStream = jpkHeading.run();
        mainHeader = new byte[mainHeaderByteStream.getNumBytes()];
        System.arraycopy(mainHeaderByteStream.getByteStream(), 0, mainHeader, 0, mainHeader.length);

      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new ErrorException("Main header cannot be correctly encoded.");
    }
  }

  /**
   * Returns the subband weight for a component, resolution level, and subband.
   *
   * @param z image component
   * @param rLevel 0 is the LL subband, and 1, 2, ... represents
   * 	next starting with the little one
   * @param subband 0 - HL, 1 - LH, 2 - HH (if resolutionLevel == 0 --> 0 -
   * 	LL)
   *
   * @return the subband weight for a component, resolution
   *  level, and subband.
   */
  private int getSubbandWeight(int tile, int z, int rLevel, int subband) {

    // If subband weights are no applied
    if (!APPLY_SUBBAND_WEIGHTS) {
      return 0;
    }

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

    // If subband weights are no applied
    if (!APPLY_SUBBAND_WEIGHTS) {
      return 0;
    }

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
   * This method is used to deliver the requested window of interes using
   * only one packet per precinct. Therefore, the delivered image only has
   * one quality layer.
   * <p>
   * The algorithm of this rate-distortion method is the most simple. But the
   * coding performance achieved when the image is delivered with this
   * algorithm is not the best one.
   * <p>
   * The coding performance is improved when delivered data are weigthed
   * applying a weight to each subband of the tansformed image. Further
   * information about suband weights, see the
   * {@link #getRateDistortionAdjustment(int, int, int)} method.
   */
  private void deliverOnePacketPerPrecinct() throws ErrorException {

    assert (discardLevels >= 0);
    assert (actualViewWindow != null);
    assert (codestream != null);

    // Create packet-headers encoder engine
    PacketHeadersEncoder packetHeading = new PacketHeadersEncoder();

    // Find the precincts which are relevants to the view window
    ArrayList<Long> relevantPrecincts =
            RelevantPrecinctsFinder.TRPCOrder(codestream, actualViewWindow,
            discardLevels);
    int numOfRelevantPrecincts = relevantPrecincts.size();


    // Contains the offset of the actual data-bin. It is useful to allocate
    // each coding level in the data-bin position
    int[] dataBinsOffset = new int[numOfRelevantPrecincts];
    Arrays.fill(dataBinsOffset, 0);

    boolean finish = false;
    layers = this.actualViewWindow.layers;
    int totalNumCodingPasses = APPLY_SUBBAND_WEIGHTS
            ? (3 * (MSBPlane + getMaxSubbandWeight()) + 1)
            : (3 * MSBPlane + 1);

    // LOOP ON CODING LEVELS
    for (int vCodingLevel = totalNumCodingPasses - 1; (vCodingLevel >= 0) && !finish; vCodingLevel--) {
      //System.out.println("\n===============================================");
      //System.out.println("- Virtual coding level: "+vCodingLevel);

      //	LOOP ON PRECINCTS
      int numOrderedRelevantPrecincts = relevantPrecincts.size();
      for (int precIndex = 0; precIndex < numOrderedRelevantPrecincts && !finish; precIndex++) {
        long inClassIdentifier = relevantPrecincts.get(precIndex);

        // If precinct has been sent, go to next one.
        if (packetHeading.isSet(inClassIdentifier)) {
          continue;
        }

        int[][][][] codingPassesLengths = logicalTarget.getLengthsOfCodingPasses(inClassIdentifier);
        // If this precinct has not any contribution, go to next one
        if (codingPassesLengths == null) {
          continue;
        }

        int[] TCRP = codestream.findTCRP(inClassIdentifier);
        int tile = TCRP[0];
        int z = TCRP[1];
        int rLevel = TCRP[2];
        int precinct = TCRP[3];


        // PACKET HEADER
        // Set first layer
        int maxBitPlanes = Integer.MIN_VALUE;
        int[][][] firstLayer = new int[(rLevel == 0 ? 1 : 3)][][];
        for (int subband = 0; subband < (rLevel == 0 ? 1 : 3); subband++) {
          int sbWeight = this.getSubbandWeight(tile, z, rLevel, subband);
          firstLayer[subband] = new int[codingPassesLengths[subband].length][];
          for (int yBlock = 0; yBlock < codingPassesLengths[subband].length; yBlock++) {
            firstLayer[subband][yBlock] = new int[codingPassesLengths[subband][yBlock].length];
            for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length; xBlock++) {
              if (codingPassesLengths[subband][yBlock][xBlock] != null) {
                if (maxBitPlanes < codingPassesLengths[subband][yBlock][xBlock].length + sbWeight) {
                  maxBitPlanes = codingPassesLengths[subband][yBlock][xBlock].length + sbWeight;
                }
                firstLayer[subband][yBlock][xBlock] = 0;
              }
            }
          }
        }

        if (vCodingLevel != maxBitPlanes) {
          continue;
        }


        packetHeading.setZeroBitPlanesAndFirstLayer(inClassIdentifier, logicalTarget.getZeroBitPlanes(inClassIdentifier), firstLayer);

        // Build the packet header
        try {
          packetHeading.encodePacketHeader(inClassIdentifier, codingPassesLengths);
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
        byte[] packetHeader = Arrays.copyOf(packetHeading.getPacketHeaderBuffer(), packetHeading.getPacketHeaderLength());
        //System.err.print("\t\tPacket header: " ); printByteArray(packetHeader); System.out.println(); // DEBUG

        // DAta in client cache
        int dataBinLengthInClientCache = (int) serverCache.getPrecinctDataBinLength(inClassIdentifier);
        if (dataBinLengthInClientCache < 0) {
          dataBinLengthInClientCache = 0;
        }

        //	Has the packet header to be included?
        int packetHeaderToSendLength = 0;
        int packetHeaderToSendOffset = 0;

        if (packetHeader.length < dataBinLengthInClientCache) {
          // Client has the packet header => do nothing
          packetHeaderToSendOffset = 0;
          packetHeaderToSendLength = 0;
          packetHeader = null;
        } else {
          // Client does not have the whole packet header
          packetHeaderToSendOffset = dataBinLengthInClientCache;
          packetHeaderToSendLength = packetHeader.length - packetHeaderToSendOffset;
        }

        // Check byte response limit
        if ((responseLength + packetHeaderToSendLength) > maximumResponseLength) {
          packetHeaderToSendLength = (int) (maximumResponseLength - responseLength);
          EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;
          finish = true;
        }

        // Update response length
        responseLength += packetHeaderToSendLength;

        // Send packet header
        if (packetHeaderToSendLength > 0) {
          // Build and sent JPIP message header
          JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.EXTENDED_PRECINCT, inClassIdentifier, packetHeaderToSendOffset, packetHeaderToSendLength, false, 0);
          byte[] packetHeaderToSend = Arrays.copyOfRange(packetHeader, packetHeaderToSendOffset, packetHeaderToSendLength);

          ArrayList<byte[]> chunks = new ArrayList<byte[]>();
          chunks.add(packetHeaderToSend);
          responseDataList.add(new ResponseData(jpipMessageHeader, chunks));

        }

        // Contains the offset of the actual data-bin. It is useful to allocate
        // each coding level in the data bin position
        int dataBinOffset = dataBinLengthInClientCache > packetHeader.length ? dataBinLengthInClientCache : packetHeader.length;
        packetHeader = null;


        // PACKET BODY

        // Memory allocation for temporary variables codingLevelToSendOffset and codingLevelToSendLength.
        // The former records the coding-pass offset to ben sent, and the later the length.
        // codingLevelToSendOffset[subband][yBlock][xBlock][cp]
        long[][][][] codingLevelToSendOffset = new long[codingPassesLengths.length][][][];
        int[][][][] codingLevelToSendLength = new int[codingPassesLengths.length][][][];

        for (int subband = 0; subband < codingPassesLengths.length; subband++) {
          codingLevelToSendOffset[subband] = new long[codingPassesLengths[subband].length][][];
          codingLevelToSendLength[subband] = new int[codingPassesLengths[subband].length][][];

          for (int yBlock = 0; yBlock < codingPassesLengths[subband].length; yBlock++) {
            codingLevelToSendOffset[subband][yBlock] = new long[codingPassesLengths[subband][yBlock].length][];
            codingLevelToSendLength[subband][yBlock] = new int[codingPassesLengths[subband][yBlock].length][];

            for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length; xBlock++) {
              if (codingPassesLengths[subband][yBlock][xBlock] != null) {
                codingLevelToSendOffset[subband][yBlock][xBlock] = new long[codingPassesLengths[subband][yBlock][xBlock].length];
                codingLevelToSendLength[subband][yBlock][xBlock] = new int[codingPassesLengths[subband][yBlock][xBlock].length];

                Arrays.fill(codingLevelToSendOffset[subband][yBlock][xBlock], 0);
                Arrays.fill(codingLevelToSendLength[subband][yBlock][xBlock], 0);
              }
            }
          }
        }

        // Get the coding passes to be sent
        int bodyLengthToSend = 0;
        for (int subband = 0; subband < codingPassesLengths.length && !finish; subband++) {
          for (int yBlock = 0; yBlock < codingPassesLengths[subband].length && !finish; yBlock++) {
            for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length && !finish; xBlock++) {

              // Does this code-block have any contribution?
              if (codingPassesLengths[subband][yBlock][xBlock] == null) {
                continue;
              }

              for (int codingPass = 0; codingPass < codingPassesLengths[subband][yBlock][xBlock].length && !finish; codingPass++) {
                int codingPassLength = codingPassesLengths[subband][yBlock][xBlock][codingPass];

                int codingPassOffsetToSend = 0;
                int codingPassLengthToSend = 0;
                // Has the packet body to be included?
                if ((dataBinOffset + codingPassLength) < dataBinLengthInClientCache) {
                  // Client has the packet header => do nothing
                  dataBinOffset += codingPassLength;
                  codingPassLengthToSend = 0;
                  codingPassOffsetToSend = 0;
                  continue;

                } else if (dataBinOffset < dataBinLengthInClientCache) {
                  // Client does not have the whole packet header
                  codingPassLengthToSend = dataBinLengthInClientCache - dataBinOffset;
                  codingPassOffsetToSend = codingPass - codingPassLengthToSend;

                } else {
                  // Client does not have any byte of coding pass
                  codingPassOffsetToSend = 0;
                  codingPassLengthToSend = codingPassLength;
                }

                // Check maximum response length
                if ((responseLength + codingPassLengthToSend) > maximumResponseLength) {
                  codingPassLengthToSend = (int) (maximumResponseLength - responseLength);
                  EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;
                  finish = true;
                }

                // Update control variables
                responseLength += codingPassLengthToSend;
                bodyLengthToSend += codingPassLengthToSend;

                codingLevelToSendOffset[subband][yBlock][xBlock][codingPass] = logicalTarget.getFilePointerCodingPass(inClassIdentifier, subband, yBlock, xBlock, codingPass) + codingPassOffsetToSend;
                codingLevelToSendLength[subband][yBlock][xBlock][codingPass] = codingPassLengthToSend;
              }
            }
          }
        }

        // SEND PACKET BODY
        if (bodyLengthToSend > 0) {
          // Build and send JPIP message header
          boolean lastByte = (!finish) ? true : false;
          JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.EXTENDED_PRECINCT, inClassIdentifier, dataBinOffset, bodyLengthToSend, lastByte, 0);

          ArrayList<Long> filePointers = new ArrayList<Long>();
          ArrayList<Integer> lengths = new ArrayList<Integer>();

          // Add coding levels to jpip message data
          for (int subband = 0; subband < codingLevelToSendLength.length; subband++) {
            for (int yBlock = 0; yBlock < codingLevelToSendLength[subband].length; yBlock++) {
              for (int xBlock = 0; xBlock < codingLevelToSendLength[subband][yBlock].length; xBlock++) {
                for (int cp = 0; cp < codingLevelToSendLength[subband][yBlock][xBlock].length; cp++) {
                  if (codingLevelToSendLength[subband][yBlock][xBlock][cp] > 0) {
                    filePointers.add(codingLevelToSendOffset[subband][yBlock][xBlock][cp]);
                    lengths.add(codingLevelToSendLength[subband][yBlock][xBlock][cp]);
                  }
                }
              }
            }
          }

          responseDataList.add(new ResponseData(jpipMessageHeader, filePointers, lengths));
        }

        codingLevelToSendOffset = null;
        codingLevelToSendLength = null;
      } // precincts
    } // coding level
  }

  /**
   * This method is used to deliver the requested window of interes using
   * only one packet per coding pass. Therefore, the delivered image has as
   * layers as coding levels (3 * most significant bit-plane + 1).
   * <p>
   * This rate-distortion method is useful to delivery the image when the
   * request does not belong to a session, so the server does not have any
   * information about the client's cache status. In this case, a fixed image
   * structure is needed because, sometimes, the client includes in its
   * request (stateless) information about the status of its cache.
   * <p>
   * The algorithm of this rate-distortion method is very simple. But the
   * coding performance achieved when the image is delivered with this
   * algorithm is not the best one.
   */
  private void deliverOnePacketPerCodingPass() throws ErrorException {

    // Create packet-headers encoder engine
    PacketHeadersEncoder packetHeading = new PacketHeadersEncoder();


    // Find the precincts which are relevants to the view window
    ArrayList<Long> relevantPrecincts =
            RelevantPrecinctsFinder.TRPCOrder(codestream, actualViewWindow,
            discardLevels);

    // Used to save the temporary offset for each data-bin
    HashMap<Long, Integer> dataBinOffsets = new HashMap<Long, Integer>(relevantPrecincts.size());
    for (long inClassIdentifier : relevantPrecincts) {
      dataBinOffsets.put(inClassIdentifier, 0);
    }

    boolean finish = false;
    layers = this.actualViewWindow.layers;
    int totalNumVirtualCodingLevels = MSBPlane * 3 + 1 + getMaxSubbandWeight();

    // LOOP ON CODING LEVELS
    for (int virtualCodingLevel = totalNumVirtualCodingLevels - 1;
            (virtualCodingLevel >= 0) && (totalNumVirtualCodingLevels - virtualCodingLevel <= layers) && !finish;
            virtualCodingLevel--) {


      //System.out.println("\n===============================================");
      //System.out.println("- Virtual coding level: " + virtualCodingLevel);

      // LOOP ON PRECINCTS
      int numOrderedRelevantPrecincts = relevantPrecincts.size() - 1;
      for (int precIndex = 0; precIndex < numOrderedRelevantPrecincts && !finish; precIndex++) {
        long inClassIdentifier = relevantPrecincts.get(precIndex);
        int[] TCRP = codestream.findTCRP(inClassIdentifier);
        int tile = TCRP[0];
        int z = TCRP[1];
        int rLevel = TCRP[2];
        int[][][][] codingPassesLengths = logicalTarget.getLengthsOfCodingPasses(inClassIdentifier);
        //System.out.println("-----------------------------------");
        //System.out.println("\tprecinct_id=" + inClassIdentifier + " => z=" + z + " r=" + rLevel + " p=" + TCRP[3]);		// DEBUG

        // Does the precinct have code blocks?
        if (codingPassesLengths == null) {
          continue;
        }


        // DATA IN CLIENT CACHE
        int dataBinLengthInClientCache = (int) serverCache.getPrecinctDataBinLength(inClassIdentifier);
        if (dataBinLengthInClientCache < 0) {
          dataBinLengthInClientCache = 0;
        }

        // SET FIRST LAYER
        if (!packetHeading.isSet(inClassIdentifier)) {
          // Adjusting the first layer following the criterion:
          // The block(s) belonging to this precinct which has the maximum number of coding passes,
          // is set on the first layer. And the others precincts, the first layer in which will be
          // included is the difference, in coding passes, between the precinct(s) with the maximum
          // number of layers and the referred precinct.

          // Get max number of coding passes in the precinct
          int maxNumCodingPasses = 0;
          for (int subband = 0; subband < codingPassesLengths.length; subband++) {
            int subbandAdjustment = getSubbandWeight(tile, z, rLevel, subband);
            for (int yBlock = 0; yBlock < codingPassesLengths[subband].length; yBlock++) {
              for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length; xBlock++) {
                if (codingPassesLengths[subband][yBlock][xBlock] != null) {
                  int tmpMax = subbandAdjustment + codingPassesLengths[subband][yBlock][xBlock].length;
                  if (tmpMax > maxNumCodingPasses) {
                    maxNumCodingPasses = tmpMax;
                  }
                }
              }
            }
          }

          int[][][] zbp = logicalTarget.getZeroBitPlanes(inClassIdentifier);
          // Set first layer value
          int[][][] firstLayer = new int[codingPassesLengths.length][][];
          for (int subband = 0; subband < codingPassesLengths.length; subband++) {
            int subbandAdjustment = getSubbandWeight(tile, z, rLevel, subband);
            firstLayer[subband] = new int[codingPassesLengths[subband].length][];
            for (int yBlock = 0; yBlock < codingPassesLengths[subband].length; yBlock++) {
              firstLayer[subband][yBlock] = new int[codingPassesLengths[subband][yBlock].length];
              for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length; xBlock++) {
                if (codingPassesLengths[subband][yBlock][xBlock] != null) {
                  int numCodingPassesBlock = codingPassesLengths[subband][yBlock][xBlock].length;
                  firstLayer[subband][yBlock][xBlock] = totalNumVirtualCodingLevels - (numCodingPassesBlock + subbandAdjustment);
                }
              }
            }
          }

          packetHeading.setZeroBitPlanesAndFirstLayer(inClassIdentifier, logicalTarget.getZeroBitPlanes(inClassIdentifier), firstLayer);
        }

        // PACKET HEADER
        byte[] packetHeader =
                getPacketHeaderOnePacketPerCodingPass(packetHeading,
                inClassIdentifier, tile, z, rLevel,
                virtualCodingLevel,
                codingPassesLengths);
        /*System.out.print("\t\t\theader: ");
        printByteArray(packetHeader);
        System.out.println(" => length:" + packetHeader.length);*/ // DEBUG

        //	Has the packet header to be included?
        int packetHeaderToSendLength = 0;
        int packetHeaderToSendOffset = 0;

        int dataBinOffset = dataBinOffsets.get(inClassIdentifier);
        //System.out.println("dataBinOffset: "+dataBinOffset);

        if ((dataBinOffset + packetHeader.length) <= dataBinLengthInClientCache) {
          // Client has the packet header => do nothing
        } else if (dataBinOffset <= dataBinLengthInClientCache) {
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
          packetHeaderToSendLength = (int) (maximumResponseLength - responseLength);
          EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;
          finish = true;
        }

        if (packetHeaderToSendLength > 0) {
          // Copy packet header to send
          byte[] packetHeaderToSend = Arrays.copyOfRange(packetHeader, packetHeaderToSendOffset, packetHeaderToSendLength);
          // JPIP message header
          JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.EXTENDED_PRECINCT, inClassIdentifier, dataBinOffset, packetHeaderToSendLength, false, totalNumVirtualCodingLevels - virtualCodingLevel);

          ArrayList<byte[]> chunks = new ArrayList<byte[]>();
          chunks.add(packetHeaderToSend);
          responseDataList.add(new ResponseData(jpipMessageHeader, chunks));
        }

        responseLength += packetHeaderToSendLength;
        dataBinOffset += packetHeader.length;
        packetHeader = null;


        // PACKET BODY
        // Get length of coding level to be sent
        int codingLevelLength = 0;
        int tmpDataBinOffset = dataBinOffset;
        int packetBodyToSendLength = 0;
        //ystem.out.println("dataBinOffset: "+tmpDataBinOffset);
        long[][][] codingLevelToSendOffset = new long[codingPassesLengths.length][][];
        int[][][] codingLevelToSendLength = new int[codingPassesLengths.length][][];

        for (int subband = 0; subband < codingPassesLengths.length && !finish; subband++) {
          codingLevelToSendOffset[subband] = new long[codingPassesLengths[subband].length][];
          codingLevelToSendLength[subband] = new int[codingPassesLengths[subband].length][];
          int subbandAdjustment = getSubbandWeight(tile, z, rLevel, subband);

          for (int yBlock = 0; yBlock < codingPassesLengths[subband].length && !finish; yBlock++) {
            codingLevelToSendOffset[subband][yBlock] = new long[codingPassesLengths[subband][yBlock].length];
            codingLevelToSendLength[subband][yBlock] = new int[codingPassesLengths[subband][yBlock].length];

            for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length && !finish; xBlock++) {
              //System.out.println("\t\tsubband=" + subband + " yBlock=" + yBlock + " xBlock=" + xBlock + " cp=" + virtualCodingLevel); // DEBUG
              codingLevelToSendOffset[subband][yBlock][xBlock] = 0;
              codingLevelToSendLength[subband][yBlock][xBlock] = 0;

              // Does this code-block have any contribution?
              if (codingPassesLengths[subband][yBlock][xBlock] == null) {
                continue;
              }

              int numCodingPassesBlock = codingPassesLengths[subband][yBlock][xBlock].length;
              int realCodingLevel = virtualCodingLevel;

              if (realCodingLevel <= subbandAdjustment) {
                continue;
              }
              if (realCodingLevel >= subbandAdjustment + numCodingPassesBlock) {
                continue;
              }

              int cpInBlock = numCodingPassesBlock + subbandAdjustment - 1 - realCodingLevel;
              codingLevelToSendOffset[subband][yBlock][xBlock] = logicalTarget.getFilePointerCodingPass(inClassIdentifier, subband, yBlock, xBlock, cpInBlock);
              codingLevelToSendLength[subband][yBlock][xBlock] = codingPassesLengths[subband][yBlock][xBlock][cpInBlock];
              codingLevelLength = codingPassesLengths[subband][yBlock][xBlock][cpInBlock];

              // Has the packet body to be included?
              if ((tmpDataBinOffset + codingLevelLength) <= dataBinLengthInClientCache) {
                // Client has the packet header => do nothing
                tmpDataBinOffset += codingLevelLength;
                codingLevelToSendOffset[subband][yBlock][xBlock] = 0;
                codingLevelToSendLength[subband][yBlock][xBlock] = 0;

              } else if (tmpDataBinOffset <= dataBinLengthInClientCache) {
                // Client does not have the whole packet header
                codingLevelToSendOffset[subband][yBlock][xBlock] = dataBinLengthInClientCache - tmpDataBinOffset;
                codingLevelToSendLength[subband][yBlock][xBlock] = codingLevelLength - (int) codingLevelToSendOffset[subband][yBlock][xBlock];

              } else {
                // Client does not have any byte of coding pass
                codingLevelToSendOffset[subband][yBlock][xBlock] = 0;
                codingLevelToSendLength[subband][yBlock][xBlock] = codingLevelLength;
              }

              // Check maximum response length
              if (responseLength + codingLevelToSendLength[subband][yBlock][xBlock] > maximumResponseLength) {
                codingLevelToSendLength[subband][yBlock][xBlock] = (int) (maximumResponseLength - responseLength);
                EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;
                finish = true;
              }
              //System.out.println("\t\tCPoffset="+dataBinsOffset[precIndex]+" length="+codingLevelToSendLength[subband][yBlock][xBlock]);
              codingLevelToSendOffset[subband][yBlock][xBlock] += logicalTarget.getFilePointerCodingPass(inClassIdentifier, subband, yBlock, xBlock, cpInBlock);
              responseLength += codingLevelToSendLength[subband][yBlock][xBlock];
              tmpDataBinOffset += codingLevelToSendLength[subband][yBlock][xBlock];
              packetBodyToSendLength += codingLevelToSendLength[subband][yBlock][xBlock];
            }
          }
        }


        if (packetBodyToSendLength > 0) {
          // Build and send JPIP message header
          boolean lastByte = (!finish) ? true : false;
          lastByte = false;
          JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.EXTENDED_PRECINCT, inClassIdentifier, dataBinOffset, packetBodyToSendLength, lastByte, totalNumVirtualCodingLevels - virtualCodingLevel);

          ArrayList<Long> filePointers = new ArrayList<Long>();
          ArrayList<Integer> lengths = new ArrayList<Integer>();

          // Add coding levels to jpip message data
          if (codingLevelToSendLength != null) {

            for (int subband = 0; subband < codingLevelToSendLength.length; subband++) {
              if (codingLevelToSendLength[subband] == null) {
                continue;
              }
              for (int yBlock = 0; yBlock < codingLevelToSendLength[subband].length; yBlock++) {
                if (codingLevelToSendLength[subband][yBlock] == null) {
                  continue;
                }
                for (int xBlock = 0; xBlock < codingLevelToSendLength[subband][yBlock].length; xBlock++) {
                  if (codingLevelToSendLength[subband][yBlock][xBlock] > 0) {
                    filePointers.add(codingLevelToSendOffset[subband][yBlock][xBlock]);
                    lengths.add(codingLevelToSendLength[subband][yBlock][xBlock]);
                  }
                }
              }
            }
          }


          responseDataList.add(new ResponseData(jpipMessageHeader, filePointers, lengths));
        }
        dataBinOffsets.put(inClassIdentifier, tmpDataBinOffset);

      } // relevant precincts

    } // coding level
  }

  /**
   * Builds a packet header for a specific coding level.
   * <p>
   * It is an auxiliary method used by {@link #deliverOnePacketPerCodingPass()}
   * method to build the packet headers.
   *
   * @param z the image component
   * @param rLevel the resolution level in the component
   * @param precinct the precinct in the resolution level
   * @param codingLevel the coding level
   * @param codingPassesLengths a multiple array with the length of all coding
   * 						passes that belong to the same precinct.
   *
   * @return the packet header.
   * @throws ErrorException if the packet header cannot be built because an
   * 							error has been found.
   */
  private byte[] getPacketHeaderOnePacketPerCodingPass(PacketHeadersEncoder packetHeadersEncoder,
                                                       long inClassIdentifier,
                                                       int tile, int z, int rLevel,
                                                       int realCodingLevel,
                                                       int[][][][] codingPassesLengths) throws ErrorException {
    // Get lengths of coding passes
    int[][][][] lengtsToEncode = new int[codingPassesLengths.length][][][];

    for (int subband = 0; subband < codingPassesLengths.length; subband++) {
      lengtsToEncode[subband] = new int[codingPassesLengths[subband].length][][];
      int subbandAdjustment = this.getSubbandWeight(tile, z, rLevel, subband);

      for (int yBlock = 0; yBlock < codingPassesLengths[subband].length; yBlock++) {
        lengtsToEncode[subband][yBlock] = new int[codingPassesLengths[subband][yBlock].length][];

        for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length; xBlock++) {
          if (codingPassesLengths[subband][yBlock][xBlock] != null) {
            int numCodingPassesBlock = codingPassesLengths[subband][yBlock][xBlock].length;

            if ((realCodingLevel > subbandAdjustment)
                    && (realCodingLevel < subbandAdjustment + numCodingPassesBlock)) {
              lengtsToEncode[subband][yBlock][xBlock] = new int[1];
              int cpInBlock = numCodingPassesBlock + subbandAdjustment - 1 - realCodingLevel;
              lengtsToEncode[subband][yBlock][xBlock][0] = codingPassesLengths[subband][yBlock][xBlock][cpInBlock];
            } else {
              lengtsToEncode[subband][yBlock][xBlock] = null;
            }
          }
        }
      }
    }

    // Build the packet header
    try {
      packetHeadersEncoder.encodePacketHeader(inClassIdentifier, lengtsToEncode);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      assert (true);
    }
    int packetHeaderLength = packetHeadersEncoder.getPacketHeaderLength();
    byte[] packetHeader = new byte[packetHeaderLength];
    System.arraycopy(packetHeadersEncoder.getPacketHeaderBuffer(), 0, packetHeader, 0, packetHeaderLength);

    return packetHeader;
  }

  /**
   * This method is used to deliver the requested window of interes using
   * only one packet per bit-plane. Therefore, the delivered image has as
   * layers as coding levels (most significant bit-plane + 1).
   * <p>
   * This rate-distortion method is useful to delivery the image when the
   * request does not belong to a session, so the server does not have any
   * information about the client's cache status. In this case, a fixed image
   * structure is needed because, sometimes, the client includes in its
   * request (stateless) information about the status of its cache.
   * <p>
   * The algorithm of this rate-distortion method is the very simple. But the
   * coding performance achieved when the image is delivered with this
   * algorithm is not the best one.
   */
  private void deliverOnePacketPerBitPlane() throws ErrorException {

    // Create packet-headers encoder engine
    PacketHeadersEncoder packetHeading = new PacketHeadersEncoder();

    // Find the precincts which are relevants to the view window
    ArrayList<Long> relevantPrecincts =
            RelevantPrecinctsFinder.TRPCOrder(codestream, actualViewWindow,
            discardLevels);

    // Temporary structure used to store the offset and length for each coding pass to be sent.
    // Indexes mean: [subband][yBlock][xBlock][cp]
    long[][][][] codingLevelToSendOffset = null;
    int[][][][] codingLevelToSendLength = null;

    //	Used to save the temporary offset for each data-bin
    HashMap<Long, Integer> dataBinOffsets = new HashMap<Long, Integer>(relevantPrecincts.size());
    for (long inClassIdentifier : relevantPrecincts) {
      dataBinOffsets.put(inClassIdentifier, 0);
    }

    // LOOP ON BIT-PLANES
    boolean finish = false;
    layers = this.actualViewWindow.layers;
    int totalNumVirtualBitPlanes = MSBPlane + getMaxSubbandWeight();
    //System.out.println("Total num virtual bit planes: "+totalNumVirtualBitPlanes);
    //System.out.println("# of layers: "+layers);
    for (int virtualBitPlane = 0; (virtualBitPlane <= totalNumVirtualBitPlanes) && (virtualBitPlane < layers) && !finish; virtualBitPlane++) {
      //System.out.println("\n===============================================");
      //System.out.println("- Virtual bit plane: "+virtualBitPlane);

      //ArrayList<Long> orderedRelevantPrecincts = relevantPrecincts; //sortingOnePacketPerBitPlanes(relevantPrecincts, virtualBitPlane, totalNumVirtualBitPlanes);

      // LOOP ON PRECINCTS
      int numOrderedRelevantPrecincts = relevantPrecincts.size();
      for (int precIndex = 0; precIndex < numOrderedRelevantPrecincts && !finish; precIndex++) {
        long inClassIdentifier = relevantPrecincts.get(precIndex);
        int[] TCRP = codestream.findTCRP(inClassIdentifier);
        int tile = TCRP[0];
        int z = TCRP[1];
        int rLevel = TCRP[2];
        int[][][][] codingPassesLengths = logicalTarget.getLengthsOfCodingPasses(inClassIdentifier);
        //System.out.println("\n\tInClassIdentifier: "+inClassIdentifier+" -> z: "+z+" rLevel: "+rLevel+" p: "+TCRP[3]);

        // Does the precinct have code blocks?
        if (codingPassesLengths == null) {
          continue;
        }

        // SET FIRST LAYER
        if (!packetHeading.isSet(inClassIdentifier)) {
          setFirstLayerOnePacketPerBitPlane(packetHeading, inClassIdentifier, tile, z, rLevel, codingPassesLengths);
        }

        //  DATA IN CLIENT CACHE
        int dataBinLengthInClientCache = (int) serverCache.getPrecinctDataBinLength(inClassIdentifier);


        // PACKET HEADER
        byte[] packetHeader = getPacketHeaderOnePacketPerBitPlane(packetHeading, inClassIdentifier, tile, z, rLevel, virtualBitPlane, totalNumVirtualBitPlanes, codingPassesLengths);

        //	Has the packet header to be included?
        int packetHeaderToSendLength = 0;
        int packetHeaderToSendOffset = 0;
        int dataBinOffset = dataBinOffsets.get(inClassIdentifier);

        if (packetHeader != null) {
          //System.out.print("\t\t\theader: " ); printByteArray(packetHeader); System.out.println(" => length:"+packetHeader.length); // DEBUG

          if ((dataBinOffset + packetHeader.length) <= dataBinLengthInClientCache) {
            // Client has the packet header => do nothing
          } else if (dataBinOffset <= dataBinLengthInClientCache) {
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
            packetHeaderToSendLength = (int) (maximumResponseLength - responseLength);
            EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;
            finish = true;
          }

          if (packetHeaderToSendLength > 0) {
            // JPIP message data
            byte[] packetHeaderToSend = Arrays.copyOfRange(packetHeader, packetHeaderToSendOffset, packetHeaderToSendLength);
            JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.EXTENDED_PRECINCT, inClassIdentifier, dataBinOffset, packetHeaderToSendLength, false, virtualBitPlane);

            ArrayList<byte[]> chunks = new ArrayList<byte[]>();
            chunks.add(packetHeaderToSend);
            responseDataList.add(new ResponseData(jpipMessageHeader, chunks));
          }

          responseLength += packetHeaderToSendLength;
          dataBinOffset += packetHeader.length;
        }

        // PACKET BODY
        // Initialization of temporary arrays
        codingLevelToSendOffset = new long[codingPassesLengths.length][][][];
        codingLevelToSendLength = new int[codingPassesLengths.length][][][];

        for (int subband = 0; subband < codingPassesLengths.length; subband++) {
          codingLevelToSendOffset[subband] = new long[codingPassesLengths[subband].length][][];
          codingLevelToSendLength[subband] = new int[codingPassesLengths[subband].length][][];
          int subbandAdjustment = this.getSubbandWeight(tile, z, rLevel, subband);

          for (int yBlock = 0; yBlock < codingPassesLengths[subband].length; yBlock++) {
            codingLevelToSendOffset[subband][yBlock] = new long[codingPassesLengths[subband][yBlock].length][];
            codingLevelToSendLength[subband][yBlock] = new int[codingPassesLengths[subband][yBlock].length][];

            for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length; xBlock++) {
              if (codingPassesLengths[subband][yBlock][xBlock] == null) {
                continue;
              }

              int numCodingPassesBlock = codingPassesLengths[subband][yBlock][xBlock].length;
              //int numBitPlanesBlock = (numCodingPassesBlock-1)/3;
              int numBitPlanesBlock = (numCodingPassesBlock - 1) / 3 + ((numCodingPassesBlock - 1) % 3 == 0 ? 0 : 1);
              int realBitPlane = totalNumVirtualBitPlanes - virtualBitPlane;

              if ((realBitPlane >= subbandAdjustment) && (realBitPlane <= subbandAdjustment + numBitPlanesBlock)) {
                int bitPlaneInBlock = numBitPlanesBlock + subbandAdjustment - realBitPlane;
                codingLevelToSendOffset[subband][yBlock][xBlock] = new long[(bitPlaneInBlock == 0) ? 1 : 3];
                codingLevelToSendLength[subband][yBlock][xBlock] = new int[(bitPlaneInBlock == 0) ? 1 : 3];
                Arrays.fill(codingLevelToSendOffset[subband][yBlock][xBlock], 0);
                Arrays.fill(codingLevelToSendLength[subband][yBlock][xBlock], 0);
              } else {
                codingLevelToSendOffset[subband][yBlock][xBlock] = null;
              }
            }
          }
        }

        // Get length of coding level to send
        int bodyLengthToSend = 0;
        int tmpDataBinOffset = dataBinOffset;

        for (int subband = 0; subband < codingPassesLengths.length && !finish; subband++) {
          int subbandAdjustment = this.getSubbandWeight(tile, z, rLevel, subband);

          for (int yBlock = 0; yBlock < codingPassesLengths[subband].length && !finish; yBlock++) {
            for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length && !finish; xBlock++) {
              //System.out.println("\t\tsubband="+subband+" yBlock="+yBlock+" xBlock= "+xBlock); // DEBUG
              // Does this code-block have any contribution?
              if (codingPassesLengths[subband][yBlock][xBlock] == null) {
                continue;
              }

              int numCodingPassesBlock = codingPassesLengths[subband][yBlock][xBlock].length;
              //int numBitPlanesBlock = (numCodingPassesBlock-1)/3;
              int numBitPlanesBlock = (numCodingPassesBlock - 1) / 3 + ((numCodingPassesBlock - 1) % 3 == 0 ? 0 : 1);
              int realBitPlane = totalNumVirtualBitPlanes - virtualBitPlane;
              //System.out.println("\t\t\ttotalNumVirtualBitPlanes: "+totalNumVirtualBitPlanes); // DEBUG
              ////System.out.println("\t\t\tnumCodingPassesBlock: "+numCodingPassesBlock); // DEBUG
              //System.out.println("\t\t\tnumBitPlanesBlock: "+numBitPlanesBlock); // DEBUG
              ////System.out.println("\t\t\tnumBitPlanesBlock: "+newNumBitPlantesBlock); // DEBUG
              //System.out.println("\t\t\trealBitPlane: "+realBitPlane); // DEBUG

              // Check if virtual bit plane is between subband adjustment and maximum number of bit planes for this code block
              if (realBitPlane < subbandAdjustment) {
                continue;
              }
              if (realBitPlane > subbandAdjustment + numBitPlanesBlock) {
                continue;
              }

              int bitPlaneInBlock = numBitPlanesBlock + subbandAdjustment - realBitPlane;
              int firstCodingPass = (bitPlaneInBlock == 0) ? 0 : 3 * bitPlaneInBlock - 2;
              int numCodingPasses = (bitPlaneInBlock == 0) ? 1 : 3;
              if ((firstCodingPass + numCodingPasses) >= numCodingPassesBlock) {
                numCodingPasses = numCodingPassesBlock - firstCodingPass;
              }

              //System.out.println("\t\t\tbitPlaneInBlock="+bitPlaneInBlock); // DEBUG
              //System.out.println("\t\t\tcodingLevels: "+firstCodingPass+"-"+(firstCodingPass+numCodingPasses-1)); // DEBUG

              for (int cp = 0; cp < numCodingPasses; cp++) {
                //System.out.println("\t\t\tCP= "+(cp+firstCodingPass)); // DEBUG
                int codingLevelLength = codingPassesLengths[subband][yBlock][xBlock][cp + firstCodingPass];

                int codingPassOffsetToSend = 0;
                int codingPassLengthToSend = 0;
                // Has the packet body to be included?
                if ((tmpDataBinOffset + codingLevelLength) <= dataBinLengthInClientCache) {
                  // Client has the packet header => do nothing
                  codingPassLengthToSend = 0;
                  codingPassOffsetToSend = 0;

                } else if (tmpDataBinOffset <= dataBinLengthInClientCache) {
                  // Client does not have the whole packet header
                  codingPassLengthToSend = dataBinLengthInClientCache - tmpDataBinOffset;
                  codingPassOffsetToSend = codingLevelLength - codingPassLengthToSend;

                } else {
                  // Client does not have any byte of coding pass
                  codingPassOffsetToSend = 0;
                  codingPassLengthToSend = codingLevelLength;
                }

                // Check maximum response length
                if ((responseLength + codingPassLengthToSend) > maximumResponseLength) {
                  codingPassLengthToSend = (int) (maximumResponseLength - responseLength);
                  EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;
                  finish = true;
                }

                responseLength += codingPassLengthToSend;
                bodyLengthToSend += codingPassLengthToSend;
                tmpDataBinOffset += codingLevelLength;

                codingLevelToSendOffset[subband][yBlock][xBlock][cp] = logicalTarget.getFilePointerCodingPass(inClassIdentifier, subband, yBlock, xBlock, cp + firstCodingPass) + codingPassOffsetToSend;
                codingLevelToSendLength[subband][yBlock][xBlock][cp] = codingPassLengthToSend;

                //System.out.println("\t\tsubband="+subband+" yBlock="+yBlock+" xBlock="+xBlock+" cl="+(cp+firstCodingPass)+" length="+codingLevelToSendLength[subband][yBlock][xBlock][cp]); // DEBUG
              }
            }
          }
        }

        if (bodyLengthToSend > 0) {

          // Build JPIP message header
          boolean lastByte = (!finish) ? true : false;
          JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.EXTENDED_PRECINCT, inClassIdentifier, dataBinOffset, bodyLengthToSend, lastByte, virtualBitPlane + 1);

          ArrayList<Long> filePointers = new ArrayList<Long>();
          ArrayList<Integer> lengths = new ArrayList<Integer>();

          // Add coding levels to jpip message data
          for (int subband = 0; subband < codingLevelToSendLength.length; subband++) {
            for (int yBlock = 0; yBlock < codingLevelToSendLength[subband].length; yBlock++) {
              for (int xBlock = 0; xBlock < codingLevelToSendLength[subband][yBlock].length; xBlock++) {
                if (codingLevelToSendLength[subband][yBlock][xBlock] != null) {
                  for (int cp = 0; cp < codingLevelToSendLength[subband][yBlock][xBlock].length; cp++) {
                    if (codingLevelToSendLength[subband][yBlock][xBlock][cp] > 0) {
                      //System.out.println("\t\t\tsubband="+subband+" yBlock="+yBlock+" xBlock="+xBlock+" cp="+cp+" length="+codingLevelToSendLength[subband][yBlock][xBlock][cp]); // DEBUG
                      filePointers.add(codingLevelToSendOffset[subband][yBlock][xBlock][cp]);
                      lengths.add(codingLevelToSendLength[subband][yBlock][xBlock][cp]);
                    }
                  }
                }
              }
            }
          }

          responseDataList.add(new ResponseData(jpipMessageHeader, filePointers, lengths));
        }

        dataBinOffsets.put(inClassIdentifier, tmpDataBinOffset);
      } // relevant precincts

    } // coding level
  }

  /**
   * This method adjusts the first layer when requested window of interest is
   * delivered using one packet per bit plane.
   * The criterion followed to adjust the first layer is:
   * 	the block(s) belonging to this precinct which has the maximum number
   * 	of coding passes, its first layer is set to 0. And the
   *
   * @param packetHeading
   * @param inClassIdentifier
   * @param z
   * @param rLevel
   * @param int[][][][] codingPassesLengths
   *
   * @throws ErrorException
   */
  private void setFirstLayerOnePacketPerBitPlane(PacketHeadersEncoder packetHeading, long inClassIdentifier, int tile, int z, int rLevel, int[][][][] codingPassesLengths) throws ErrorException {

    // Get max number of coding passes in the resolution level
    int maxNumBitPlanes = 0;
    for (int subband = 0; subband < codingPassesLengths.length; subband++) {
      int subbandAdjustment = getSubbandWeight(tile, z, rLevel, subband);
      for (int yBlock = 0; yBlock < codingPassesLengths[subband].length; yBlock++) {
        for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length; xBlock++) {
          if (codingPassesLengths[subband][yBlock][xBlock] != null) {
            //System.out.println("\t\tsb="+subband+" yb="+yBlock+" xb="+xBlock+" -> "+codingPassesLengths[subband][yBlock][xBlock].length); // DEBUG
            //int tmpMax = subbandAdjustment + (codingPassesLengths[subband][yBlock][xBlock].length-1)/3;
            int numCodingPassesBlock = codingPassesLengths[subband][yBlock][xBlock].length;
            int numBitPlanesBlock = (numCodingPassesBlock - 1) / 3 + ((numCodingPassesBlock - 1) % 3 == 0 ? 0 : 1);
            int tmpMax = subbandAdjustment + numBitPlanesBlock;
            if (tmpMax > maxNumBitPlanes) {
              maxNumBitPlanes = tmpMax;
            }
          }
        }
      }
    }

    // Set first layer value
    int[][][] firstLayer = new int[codingPassesLengths.length][][];
    for (int subband = 0; subband < codingPassesLengths.length; subband++) {
      int subbandAdjustment = getSubbandWeight(tile, z, rLevel, subband);
      firstLayer[subband] = new int[codingPassesLengths[subband].length][];
      for (int yBlock = 0; yBlock < codingPassesLengths[subband].length; yBlock++) {
        firstLayer[subband][yBlock] = new int[codingPassesLengths[subband][yBlock].length];
        for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length; xBlock++) {
          if (codingPassesLengths[subband][yBlock][xBlock] != null) {
            //int numBitPlanesBlock = (codingPassesLengths[subband][yBlock][xBlock].length-1)/3;
            int numCodingPassesBlock = codingPassesLengths[subband][yBlock][xBlock].length;
            int numBitPlanesBlock = (numCodingPassesBlock - 1) / 3 + ((numCodingPassesBlock - 1) % 3 == 0 ? 0 : 1);
            firstLayer[subband][yBlock][xBlock] = maxNumBitPlanes - (numBitPlanesBlock + subbandAdjustment);
            //System.out.println("\t\tsubband="+subband+" yblock="+yBlock+" xblock="+xBlock+" => first layer: "+firstLayer[subband][yBlock][xBlock]); // DEBUG
          }
        }
      }
    }

    packetHeading.setZeroBitPlanesAndFirstLayer(inClassIdentifier, logicalTarget.getZeroBitPlanes(inClassIdentifier), firstLayer);
  }

  /**
   * Builds a packet header using one packet per bit plane
   * <p>
   * It is an auxiliary method used by {@link #getResponseOnePacketPerBitPlane()}
   * method to build the packet headers.
   *
   * @param packetHeading the packet heading object used to build the header.
   * @param inClassIdentifier
   * @param z the image component.
   * @param rLevel the resolution level in the component.
   * @param virtualBitPlane
   * @param totalNumVirtualBitPlanes
   * @param codingPasses a multiple array with the length of all coding
   * 						passes belonging to the precinct.
   *
   * @return the encoded packet header.
   *
   * @throws ErrorException if the packet header cannot be built because an
   * 							error has been found.
   */
  private byte[] getPacketHeaderOnePacketPerBitPlane(PacketHeadersEncoder packetHeading, long inClassIdentifier, int tile, int z, int rLevel, int virtualBitPlane, int totalNumVirtualBitPlanes, int[][][][] codingPassesLengths) throws ErrorException {
    //System.out.println("\t\tBuilding packet header");

    boolean callPacketHeader = false;

    // Get length of coding passes
    int[][][][] lengthsToEncode = new int[codingPassesLengths.length][][][];

    for (int subband = 0; subband < codingPassesLengths.length; subband++) {
      lengthsToEncode[subband] = new int[codingPassesLengths[subband].length][][];
      int subbandAdjustment = getSubbandWeight(tile, z, rLevel, subband);

      for (int yBlock = 0; yBlock < codingPassesLengths[subband].length; yBlock++) {
        lengthsToEncode[subband][yBlock] = new int[codingPassesLengths[subband][yBlock].length][];

        for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length; xBlock++) {
          //System.out.println("\t\t\tsubband="+subband+" yBlock="+yBlock+" xBlock= "+xBlock); // DEBUG
          // Does this code-block have any contribution?
          lengthsToEncode[subband][yBlock][xBlock] = null;
          if (codingPassesLengths[subband][yBlock][xBlock] == null) {
            continue;
          }

          int numCodingPassesBlock = codingPassesLengths[subband][yBlock][xBlock].length;
          //int numBitPlanesBlock = (numCodingPassesBlock-1)/3;
          int numBitPlanesBlock = (numCodingPassesBlock - 1) / 3 + ((numCodingPassesBlock - 1) % 3 == 0 ? 0 : 1);
          int realBitPlane = totalNumVirtualBitPlanes - virtualBitPlane;
          //System.out.println("\t\t\tnumCodingPassesBlock: "+numCodingPassesBlock); // DEBUG
          //System.out.println("\t\t\tnumBitPlanesBlock: "+numBitPlanesBlock); // DEBUG
          //System.out.println("\t\t\trealBitPlane: "+realBitPlane); // DEBUG

          // Check if virtual bit plane is between subband adjustment and maximum number of bit planes for this code block
          //if (realBitPlane < subbandAdjustment) continue;
          //if (realBitPlane > subbandAdjustment+numBitPlanesBlock) continue;

          if ((realBitPlane >= subbandAdjustment) && (realBitPlane <= subbandAdjustment + numBitPlanesBlock)) {
            int bitPlaneInBlock = numBitPlanesBlock + subbandAdjustment - realBitPlane;
            int firstCodingLevel = (bitPlaneInBlock == 0) ? 0 : 3 * bitPlaneInBlock - 2;
            int lastCodingLevel = (bitPlaneInBlock == 0) ? firstCodingLevel : firstCodingLevel + 2;
            if (lastCodingLevel >= numCodingPassesBlock) {
              lastCodingLevel = numCodingPassesBlock - 1;
            }
            lengthsToEncode[subband][yBlock][xBlock] = new int[(bitPlaneInBlock == 0) ? 1 : 3];
            //System.out.println("\t\t\tfirstCodingLevel: "+firstCodingLevel); // DEBUG
            //System.out.println("\t\t\tlastCodingLevel: "+lastCodingLevel); // DEBUG

            for (int cl = firstCodingLevel, cp = 0; cl <= lastCodingLevel; cl++, cp++) {
              lengthsToEncode[subband][yBlock][xBlock][cp] = codingPassesLengths[subband][yBlock][xBlock][cl];
              callPacketHeader = true;
              //System.out.println("\t\t\tsubband="+subband+" yBlock="+yBlock+" xBlock="+xBlock+" cl="+cl+" length="+lengthsToEncode[subband][yBlock][xBlock][cp]); // DEBUG
            }
          } else {
            //System.out.println("\t\t\tsubband="+subband+" yBlock="+yBlock+" xBlock="+xBlock+" -> null"); // DEBUG
          }

        }
      }
    }

    // Build the packet header
    if (callPacketHeader) {

      try {
        packetHeading.encodePacketHeader(inClassIdentifier, lengthsToEncode);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
        assert (true);
      }
      return packetHeading.getPacketHeaderBuffer();
    } else {
      return null;
    }


  }

  /**
   * This method is used to delivery the requested window of interes using
   * SCALE method. Therefore, the coding passes of the delivered image are
   * allocated in the layers following the SCALE method:
   * l = L - 1 - ((c div 3) * 2) - ((c mod 3) div 2)
   * where:
   * L = (K * 2) - 1
   * K = number of bit-planes
   * <p>
   * This rate-distortion method is useful to delivery the image when the
   * request does not belong to a session, so the server does not have any
   * information about the client's cache status. In this case, a fixed image
   * structure is needed because, sometimes, the client includes in its
   * request (stateless) information about the status of its cache.
   * <p>
   * NOTE: Although the subband weight function is the number of coding passes
   * to be upshifted, this value is mapped to bit planes (multipling by 3)
   * in order to keep the coherence and allow the MRP and CP may be joined.
   */
  private void deliverScale() throws ErrorException {

    // Create packet-headers encoder engine
    PacketHeadersEncoder packetHeading = new PacketHeadersEncoder();

    // Find the precincts which are relevants to the view window
    ArrayList<Long> relevantPrecincts =
            RelevantPrecinctsFinder.TRPCOrder(codestream, actualViewWindow,
            discardLevels);
    int numOfRelevantPrecincts = relevantPrecincts.size();

    // Temporary structure used to store the offset and length
    // for each coding pass to be sent.
    // Indexes mean: [subband][yBlock][xBlock][cp]
    long[][][][] codingLevelToSendOffset = null;
    int[][][][] codingLevelToSendLength = null;

    //	Used to save the temporary offset for each data-bin
    HashMap<Long, Integer> dataBinOffsets = new HashMap<Long, Integer>(relevantPrecincts.size());
    for (long inClassIdentifier : relevantPrecincts) {
      dataBinOffsets.put(inClassIdentifier, 0);
    }

    boolean finish = false;
    layers = this.actualViewWindow.layers;
    int totalNumVirtualCodingLevels = 3 * MSBPlane + 3 * getMaxSubbandWeight();
    System.out.println("Total num virtual CL: " + totalNumVirtualCodingLevels);

    // LOOP ON CODING PASSES
    for (int virtualCodingLevel = 0, layer = 0;
            (virtualCodingLevel < totalNumVirtualCodingLevels) && (layer < layers) && !finish;
            virtualCodingLevel = getNextCodingLevel(virtualCodingLevel), layer = getLayer(virtualCodingLevel + 1)) {
      System.out.println("\n===============================================");
      System.out.println("- Virtual coding level: " + virtualCodingLevel + " of " + totalNumVirtualCodingLevels);

      //ArrayList<Long> orderedRelevantPrecincts = sortingScale(relevantPrecincts, virtualCodingLevel, totalNumVirtualCodingLevels);

      // LOOP ON PRECINCTS
      numOfRelevantPrecincts = relevantPrecincts.size();
      for (int precIndex = 0; precIndex < numOfRelevantPrecincts && !finish; precIndex++) {
        long inClassIdentifier = relevantPrecincts.get(precIndex);
        int[] TCRP = codestream.findTCRP(inClassIdentifier);
        int tile = TCRP[0];
        int z = TCRP[1];
        int rLevel = TCRP[2];
        int[][][][] codingPassesLengths = logicalTarget.getLengthsOfCodingPasses(inClassIdentifier);
        System.out.println("\t------------------------");
        System.out.println("\tprecinct_id=" + inClassIdentifier + " => z=" + z + " r=" + rLevel + " p=" + TCRP[3]);		// DEBUG
        System.out.print("\t\tType of CP=");
        printTypeOfCodingPass(virtualCodingLevel);
        System.out.println();

        // Does the precinct have code blocks?
        if (codingPassesLengths == null) {
          continue;
        }

        // DATA IN CLIENT CACHE
        int dataBinLengthInClientCache = (int) serverCache.getPrecinctDataBinLength(inClassIdentifier);

        // SET FIRST LAYER
        if (!packetHeading.isSet(inClassIdentifier)) {
          setFirstLayerScale(packetHeading, inClassIdentifier, tile, z, rLevel, codingPassesLengths, totalNumVirtualCodingLevels);
        }

        // SKIP PRECINCT ?
        /*if (skipPrecinctScale(inClassIdentifier, tile, z, rLevel, totalNumVirtualCodingLevels, virtualCodingLevel, codingPassesLengths)) {
        continue;
        }*/

        // PACKET HEADER
        byte[] packetHeader = getPacketHeaderScale(packetHeading, inClassIdentifier, tile, z, rLevel, totalNumVirtualCodingLevels, virtualCodingLevel, codingPassesLengths);
        System.out.print("\t\theader: ");
        printByteArray(packetHeader);
        System.out.println(" => length:" + packetHeader.length); // DEBUG

        //	Has the packet header to be included?
        int packetHeaderToSendLength = 0;
        int packetHeaderToSendOffset = 0;
        int dataBinOffset = dataBinOffsets.get(inClassIdentifier);

        if ((dataBinOffset + packetHeader.length) < dataBinLengthInClientCache) {
          // Client has the whole packet header => do nothing
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
          packetHeaderToSendLength = (int) (maximumResponseLength - responseLength);
          EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;
          finish = true;
        }

        if (packetHeaderToSendLength > 0) {
          //	Copy packet header to send
          byte[] packetHeaderToSend = Arrays.copyOfRange(packetHeader, packetHeaderToSendOffset, packetHeaderToSendLength);
          // JPIP message header
          boolean lastByte = (!finish) ? true : false;
          JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.EXTENDED_PRECINCT, inClassIdentifier, dataBinOffset, packetHeaderToSendLength, lastByte, layer + 1);

          ArrayList<byte[]> chunks = new ArrayList<byte[]>();
          chunks.add(packetHeaderToSend);
          responseDataList.add(new ResponseData(jpipMessageHeader, chunks));
        }
        responseLength += packetHeaderToSendLength;
        dataBinOffset += packetHeader.length;
        packetHeader = null;


        // PACKET BODY
        System.out.println("\t\t-Packet body");
        // Initialization of temporary arrays
        codingLevelToSendOffset = new long[codingPassesLengths.length][][][];
        codingLevelToSendLength = new int[codingPassesLengths.length][][][];

        for (int subband = 0; subband < codingPassesLengths.length; subband++) {
          codingLevelToSendOffset[subband] = new long[codingPassesLengths[subband].length][][];
          codingLevelToSendLength[subband] = new int[codingPassesLengths[subband].length][][];
          int subbandAdjustment = 3 * getSubbandWeight(tile, z, rLevel, subband);

          for (int yBlock = 0; yBlock < codingPassesLengths[subband].length; yBlock++) {
            codingLevelToSendOffset[subband][yBlock] = new long[codingPassesLengths[subband][yBlock].length][];
            codingLevelToSendLength[subband][yBlock] = new int[codingPassesLengths[subband][yBlock].length][];

            for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length; xBlock++) {
              if (codingPassesLengths[subband][yBlock][xBlock] == null) {
                continue;
              }

              //System.out.println("\t\t\tsubband="+subband+" yblock="+yBlock+" xblock="+xBlock); // DEBUG
              int numCodingPassesBlock = codingPassesLengths[subband][yBlock][xBlock].length;
            int realCodingLevel = totalNumVirtualCodingLevels - virtualCodingLevel;
            int codingLevelInBlock = numCodingPassesBlock + subbandAdjustment - realCodingLevel;


              if (codingLevelInBlock == -1) {
                // To take into account the first Clean Pass of a code block and subband, when this
                // code-block is placed in a layer greather than 0. In this case, the code block of
                // the precinct with the greatest number of coding passes will belong to the layer 0,
                // and the next code blocks will be adjusted to the layer in function of the difference
                // in number of coding passes. In this case, as MRP and CP are joined, it must be considered
                codingLevelToSendOffset[subband][yBlock][xBlock] = new long[1];
                codingLevelToSendLength[subband][yBlock][xBlock] = new int[1];
                Arrays.fill(codingLevelToSendOffset[subband][yBlock][xBlock], 0);
                Arrays.fill(codingLevelToSendLength[subband][yBlock][xBlock], 0);
              } else if ((realCodingLevel >= subbandAdjustment) && (realCodingLevel <= subbandAdjustment + numCodingPassesBlock)) {
                int numCodingPasses = isMRP(codingLevelInBlock) ? 2 : 1;
                codingLevelToSendOffset[subband][yBlock][xBlock] = new long[numCodingPasses];
                codingLevelToSendLength[subband][yBlock][xBlock] = new int[numCodingPasses];
                Arrays.fill(codingLevelToSendOffset[subband][yBlock][xBlock], 0);
                Arrays.fill(codingLevelToSendLength[subband][yBlock][xBlock], 0);
              } else {
                codingLevelToSendOffset[subband][yBlock][xBlock] = null;
              }

            }
          }
        }

        // Get length of coding level to be sent
        int bodyLengthToSend = 0;
        int tmpDataBinOffset = dataBinOffset;

        for (int subband = 0; subband < codingPassesLengths.length && !finish; subband++) {
          int subbandAdjustment = 3 * getSubbandWeight(tile, z, rLevel, subband);
          for (int yBlock = 0; yBlock < codingPassesLengths[subband].length && !finish; yBlock++) {
            for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length && !finish; xBlock++) {
              System.out.println("\t\t\tsubband=" + subband + " yblock=" + yBlock + " xblock=" + xBlock); // DEBUG
              // Does this code-block have any contribution?
              if (codingPassesLengths[subband][yBlock][xBlock] == null) {
                continue;
              }

              int numCodingPassesBlock = codingPassesLengths[subband][yBlock][xBlock].length;
              int realCodingLevel = totalNumVirtualCodingLevels - virtualCodingLevel;
              int codingLevelInBlock = numCodingPassesBlock + subbandAdjustment - realCodingLevel;
              int numCodingPasses = isMRP(codingLevelInBlock) ? 2 : 1;
              System.out.println("\t\t\t\ttotalNumVirtualCodingLevels=" + totalNumVirtualCodingLevels
                      + " numCodingPassesBlock=" + codingPassesLengths[subband][yBlock][xBlock].length
                      + " virtualCodingLevel=" + virtualCodingLevel
                      + " realCodingLevel=" + realCodingLevel);  // DEBUG

              if (codingLevelInBlock == -1) {
                // To take into account the first Clean Pass of a code block and subband, when this
                // code-block is placed in a layer greather than 0. In this case, the code block of
                // the precinct with the greatest number of coding passes will belong to the layer 0,
                // and the next code blocks will be adjusted to the layer in function of the difference
                // in number of coding passes. In this case, as MRP and CP are joined, it must be considered
                numCodingPasses = 1;
                codingLevelInBlock = 0;
              } else if ((realCodingLevel >= subbandAdjustment) && (realCodingLevel <= subbandAdjustment + numCodingPassesBlock)) {
                // do nothing
              } else {
                continue;
              }
              System.out.println("\t\t\t\tcodingLevelInBlock="+codingLevelInBlock+" numCodingPasses=" + numCodingPasses);

              for (int cp = 0; cp < numCodingPasses; cp++) {
                int codingLevelLength = codingPassesLengths[subband][yBlock][xBlock][codingLevelInBlock + cp];
                int codingPassOffsetToSend = 0;
                int codingPassLengthToSend = 0;
                //System.out.print("\t\tsb="+subband+" yb="+yBlock+" xb="+xBlock+" cp="+(codingLevelInBlock+cp)+" length="+codingLevelLength);
                //System.out.print(" => "); printTypeOfCodingPass(codingLevelInBlock+cp); System.out.println();

                //	Has the packet body to be included?
                if ((tmpDataBinOffset + codingLevelLength) < dataBinLengthInClientCache) {
                  // Client has the packet header => do nothing
                  codingPassLengthToSend = 0;
                  codingPassOffsetToSend = 0;
                  continue;

                } else if (tmpDataBinOffset < dataBinLengthInClientCache) {
                  // Client does not have the whole packet header
                  codingPassLengthToSend = dataBinLengthInClientCache - tmpDataBinOffset;
                  codingPassOffsetToSend = codingLevelLength - codingPassLengthToSend;

                } else {
                  // Client does not have any byte of coding pass
                  codingPassOffsetToSend = 0;
                  codingPassLengthToSend = codingLevelLength;
                }

                // Check maximum response length
                if ((responseLength + codingPassLengthToSend) > maximumResponseLength) {
                  codingPassLengthToSend = (int) (maximumResponseLength - responseLength);
                  EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;
                  finish = true;
                }

                responseLength += codingPassLengthToSend;
                bodyLengthToSend += codingPassLengthToSend;
                tmpDataBinOffset += codingPassLengthToSend;

                codingLevelToSendOffset[subband][yBlock][xBlock][cp] = logicalTarget.getFilePointerCodingPass(inClassIdentifier, subband, yBlock, xBlock, codingLevelInBlock + cp) + codingPassOffsetToSend;
                codingLevelToSendLength[subband][yBlock][xBlock][cp] = codingPassLengthToSend;
              }
            }
          }
        }


        //System.out.println("   Data-bin offset: " + dataBinsOffset[precIndex] + " length: " + bodyLengthToSend); // DEBUG
        if (bodyLengthToSend > 0) {

          // Build JPIP message header
          boolean lastByte = (!finish) ? true : false;
          JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.EXTENDED_PRECINCT, inClassIdentifier, dataBinOffset, bodyLengthToSend, lastByte, layer + 1);

          ArrayList<Long> filePointers = new ArrayList<Long>();
          ArrayList<Integer> lengths = new ArrayList<Integer>();

          // Add coding levels to jpip message data
          for (int subband = 0; subband < codingLevelToSendLength.length; subband++) {
            for (int yBlock = 0; yBlock < codingLevelToSendLength[subband].length; yBlock++) {
              for (int xBlock = 0; xBlock < codingLevelToSendLength[subband][yBlock].length; xBlock++) {
                if (codingLevelToSendLength[subband][yBlock][xBlock] != null) {
                  for (int cp = 0; cp < codingLevelToSendLength[subband][yBlock][xBlock].length; cp++) {
                    if (codingLevelToSendLength[subband][yBlock][xBlock][cp] > 0) {
                      //System.out.println("\t\tsb="+subband+" yb="+yBlock+" xb="+xBlock+" cp="+cp+" length="+codingLevelToSendLength[subband][yBlock][xBlock][cp]); // DEBUG
                      filePointers.add(codingLevelToSendOffset[subband][yBlock][xBlock][cp]);
                      lengths.add(codingLevelToSendLength[subband][yBlock][xBlock][cp]);
                    }
                  }
                }
              }
            }
          }

          responseDataList.add(new ResponseData(jpipMessageHeader, filePointers, lengths));
        }
        dataBinOffsets.put(inClassIdentifier, tmpDataBinOffset);

      } // relevant precincts

    } // coding pass

  }

  /**
   *
   * @param codingLevel
   * @return
   */
  private int getNextCodingLevel(int codingLevel) {
    return isMRP(codingLevel) ? (codingLevel + 2) : (codingLevel + 1);
  }

  /**
   *
   * @param codingLevel
   * @return
   */
  private int getLayer(int codingLevel) {
    return ((codingLevel / 3) * 2) + ((codingLevel % 3) / 2);
  }

  /**
   * Check whether a coding level is or not a MRP pass.
   *
   * @param codingLevel
   * @return
   */
  private boolean isMRP(int codingLevel) {
    if (((codingLevel - 2) % 3) == 0) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * This method adjusts the first layer when requested window of interest is
   * delivered using the SCALE method.
   * The criterion followed to adjust the first layer is:
   * 	the block(s) belonging to this precinct which has the maximum number
   * 	of coding passes, its first layer is set to 0......
   *
   *
   * @param packetHeading
   * @param inClassIdentifier
   * @param z
   * @param rLevel
   * @param codingPassesLengths
   *
   * @throws ErrorException
   */
  private void setFirstLayerScale(PacketHeadersEncoder packetHeading, long inClassIdentifier, int tile, int z, int rLevel, int[][][][] codingPassesLengths, int totalNumVirtualCodingLevels) throws ErrorException {
    System.out.println("\t\t-Setting first layer");
    // Get max number of coding passes in the resolution level
    int maxNumCodingPasses = 0;
    for (int subband = 0; subband < codingPassesLengths.length; subband++) {
      int subbandAdjustment = 3 * getSubbandWeight(tile, z, rLevel, subband);
      for (int yBlock = 0; yBlock < codingPassesLengths[subband].length; yBlock++) {
        for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length; xBlock++) {
          if (codingPassesLengths[subband][yBlock][xBlock] != null) {
            int tmpMax = subbandAdjustment + codingPassesLengths[subband][yBlock][xBlock].length;
            if (tmpMax > maxNumCodingPasses) {
              maxNumCodingPasses = tmpMax;
            }
          }
        }
      }
    }

    // Set first layer value
    int[][][] firstLayer = new int[codingPassesLengths.length][][];
    for (int subband = 0; subband < codingPassesLengths.length; subband++) {
      int subbandAdjustment = 3 * getSubbandWeight(tile, z, rLevel, subband);
      firstLayer[subband] = new int[codingPassesLengths[subband].length][];
      for (int yBlock = 0; yBlock < codingPassesLengths[subband].length; yBlock++) {
        firstLayer[subband][yBlock] = new int[codingPassesLengths[subband][yBlock].length];
        for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length; xBlock++) {
          if (codingPassesLengths[subband][yBlock][xBlock] != null) {
            int numCodingPassesBlock = codingPassesLengths[subband][yBlock][xBlock].length;
            //int codingLevelOfFirstLayer = maxNumCodingPasses - (numCodingPassesBlock + subbandAdjustment);
            int codingLevelOfFirstLayer = totalNumVirtualCodingLevels - (numCodingPassesBlock + subbandAdjustment);
            firstLayer[subband][yBlock][xBlock] = getLayer(codingLevelOfFirstLayer);
            System.out.println("\t\t\tsubband=" + subband + " yblock=" + yBlock + " xblock=" + xBlock + " #CP=" + numCodingPassesBlock + " codingLevelFirstLayer=" + codingLevelOfFirstLayer + " => first layer: " + firstLayer[subband][yBlock][xBlock]); // DEBUG
          }
        }
      }
    }

    packetHeading.setZeroBitPlanesAndFirstLayer(inClassIdentifier, logicalTarget.getZeroBitPlanes(inClassIdentifier), firstLayer);
  }

  /**
   * Builds a packet header for a specific coding level.
   * <p>
   * It is an auxiliary method used by {@link #getResponseOnePacketPerCodingPass()}
   * method to build the packet headers.
   *
   * @param packetHeading the packet heading object used to build the header.
   * @param z the image component.
   * @param rLevel the resolution level in the component.
   * @param precinct the precinct in the resolution level
   * @param initialCodingPass the first coding pass of the packet.
   * @param numOfCodingPasses the number of coding passes of the packet.
   * @param codingPasses a multiple array with the length of all coding
   * 						passes belonging to the precinct.
   * @return the encoded packet header.
   * @throws ErrorException if the packet header cannot be built because an
   * 							error has been found.
   */
  private byte[] getPacketHeaderScale(PacketHeadersEncoder packetHeading, long inClassIdentifier, int tile, int z, int rLevel, int totalNumVirtualCodingLevels, int virtualCodingLevel, int[][][][] codingPassesLengths) throws ErrorException {
    System.out.println("\t\t- Packet Header...");
    int[][][][] lengthsToEncode = new int[codingPassesLengths.length][][][];

    for (int subband = 0; subband < codingPassesLengths.length; subband++) {
      lengthsToEncode[subband] = new int[codingPassesLengths[subband].length][][];
      int subbandAdjustment = 3 * getSubbandWeight(tile, z, rLevel, subband);

      for (int yBlock = 0; yBlock < codingPassesLengths[subband].length; yBlock++) {
        lengthsToEncode[subband][yBlock] = new int[codingPassesLengths[subband][yBlock].length][];

        for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length; xBlock++) {
          if (codingPassesLengths[subband][yBlock][xBlock] != null) {
            System.out.println("\t\tsb=" + subband + " yb=" + yBlock + " xb=" + xBlock); // DEBUG
            int numCodingPassesBlock = codingPassesLengths[subband][yBlock][xBlock].length;
            int realCodingLevel = totalNumVirtualCodingLevels - virtualCodingLevel;
            int codingLevelInBlock = numCodingPassesBlock + subbandAdjustment - realCodingLevel;
            System.out.println("\t\t\tnumCodingPassesBlock=" + numCodingPassesBlock
                    + " realCodingLevel=" + realCodingLevel
                    + " codingLevelInBlock=" + codingLevelInBlock); // DEBUG

            if (codingLevelInBlock == -1) {
              // To take into account the first Clean Pass of a code block and subband, when this
              // code-block is placed in a layer greather than 0. In this case, the code block of
              // the precinct with the greatest number of coding passes will belong to the layer 0,
              // and the next code blocks will be adjusted to the layer in function of the difference
              // in number of coding passes. In this case, as MRP and CP are joined, it must be considered
              lengthsToEncode[subband][yBlock][xBlock] = new int[1];
              lengthsToEncode[subband][yBlock][xBlock][0] = codingPassesLengths[subband][yBlock][xBlock][0];
              System.out.println("\t\tsb=" + subband + " yb=" + yBlock + " xb=" + xBlock + " cp=0" + " length=" + lengthsToEncode[subband][yBlock][xBlock][0]); // DEBUG
            } else if ((realCodingLevel >= subbandAdjustment) && (realCodingLevel <= subbandAdjustment + numCodingPassesBlock)) {
              int numCodingPasses = isMRP(codingLevelInBlock) ? 2 : 1;
              System.out.println("\t\t\tnumCodingPasses=" + numCodingPasses); // DEBUG
              lengthsToEncode[subband][yBlock][xBlock] = new int[numCodingPasses];
              for (int cp = 0; cp < numCodingPasses; cp++) {
                lengthsToEncode[subband][yBlock][xBlock][cp] = codingPassesLengths[subband][yBlock][xBlock][codingLevelInBlock + cp];
                System.out.println("\t\t\tsb=" + subband + " yb=" + yBlock + " xb=" + xBlock + " cp=" + (codingLevelInBlock + cp) + " length=" + lengthsToEncode[subband][yBlock][xBlock][cp]); // DEBUG
              }
            } else {
              lengthsToEncode[subband][yBlock][xBlock] = null;
              //System.out.println("\t\tsb="+subband+" yb="+yBlock+" xb="+xBlock+" length=null"); // DEBUG
            }
          }
        }
      }
    }

    // Build the packet header
    try {
      packetHeading.encodePacketHeader(inClassIdentifier, lengthsToEncode);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      assert (true);
    }

    return packetHeading.getPacketHeaderBuffer();
  }

  /**
   * Check if a precinct has to be, or not, included in a fixed coding level.
   * <p>
   * This method is only used by the {@link #deliverScale()} method.
   *
   * @param inClassIdentifier
   * @param z
   * @param rLevel
   * @param totalNumVirtualCodingLevels
   * @param virtualCodingLevel
   * @param codingPassesLengths
   * @return
   */
  private boolean skipPrecinctScale(long inClassIdentifier, int tile, int z, int rLevel, int totalNumVirtualCodingLevels, int virtualCodingLevel, int[][][][] codingPassesLengths) {
    boolean skipPrecinct = true;
    for (int subband = 0; subband < codingPassesLengths.length && skipPrecinct; subband++) {
      int subbandAdjustment = 3 * getSubbandWeight(tile, z, rLevel, subband);
      for (int yBlock = 0; yBlock < codingPassesLengths[subband].length && skipPrecinct; yBlock++) {
        for (int xBlock = 0; xBlock < codingPassesLengths[subband][yBlock].length && skipPrecinct; xBlock++) {
          int numCodingPassesBlock = codingPassesLengths[subband][yBlock][xBlock].length;
          int realCodingLevel = totalNumVirtualCodingLevels - virtualCodingLevel;
          //System.out.println("\t\t\tnumCodingPassesBlock="+numCodingPassesBlock);  // DEBUG
          //System.out.println("\t\t\ttotalNumVirtualCodingLevels="+totalNumVirtualCodingLevels);  // DEBUG
          //System.out.println("\t\t\trealCodingLevel="+realCodingLevel);  // DEBUG
          //System.out.println("\t\t\tsubbandAdjustment="+subbandAdjustment);  // DEBUG
          if ((realCodingLevel >= subbandAdjustment) && (realCodingLevel <= numCodingPassesBlock + subbandAdjustment + 1)) {
            skipPrecinct = false;
          } else {
          }
        }
      }
    }

    return skipPrecinct;
  }

  /**
   * Useful to print which is the type of each coding level. Only for debugging purposes.
   *
   * @param codingLevel
   */
  private void printTypeOfCodingPass(int codingLevel) {
    if ((codingLevel % 3) == 0) {
      System.out.print("CP");
    } else if ((codingLevel - 1) % 3 == 0) {
      System.out.print("SPP");
    } else if ((codingLevel - 2) % 3 == 0) {
      System.out.print("MRP");
    } else {
      assert (true);
    }
  }

  /**
   * Useful method for printing a byte array. Only for debugging purposes.
   *
   * @param buffer the byte array to be printed.
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
