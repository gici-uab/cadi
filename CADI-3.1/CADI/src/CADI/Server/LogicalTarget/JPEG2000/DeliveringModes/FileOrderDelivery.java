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

import CADI.Common.LogicalTarget.JPEG2000.JPEG2000Util;
import CADI.Common.Network.JPIP.EORCodes;
import CADI.Common.Network.JPIP.JPIPMessageHeader;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Server.Cache.ServerCacheModel;
import CADI.Server.Core.ResponseData;
import CADI.Server.LogicalTarget.JPEG2000.JP2KServerLogicalTarget;
import CADI.Server.LogicalTarget.JPEG2000.ServerJPEG2KCodestream;
import CADI.Server.LogicalTarget.JPEG2000.ServerJPEG2KTile;
import GiciException.ErrorException;

/**
 * This class implements the delivery of the requested WOI following the
 * order of the codestream saved in the file. None transcoding is performed.
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
 * @version 1.0.1 2008/09/08
 */
public class FileOrderDelivery {

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
   *
   */
  private ArrayList<ResponseData> responseDataList = null;
  // INTERNAL ATTRIBUTES

  /**
   *
   */
  private ViewWindowField actualViewWindow = null;

  /**
   * It is a temporary attribute to accumulate the response length which is
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
  private ServerJPEG2KCodestream codestream = null;

  private ServerJPEG2KTile tileObj = null;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#numLayers}
   */
  private int maxNumLayers;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#progressionOrder}
   */
  private int progressionOrder = -1;

  /**
   * Some internal attributes for passing values between diferents methods.
   * They contains the view window attributes, then for a full description
   * see {@link ViewWindowField}
   */
  private int layers = -1;

  /**
   * Is an array list which contains precinct identifiers. This attribute is
   * used to save the order in which precincts will be sent.
   */
  private ArrayList<Long> relevantPrecincts = null;

  /**
   * Is a two-dimension array of array lists which contains precinct
   * identifiers. This attribute is used to save the order in which precincts
   * will be sent.
   * <p>
   * The first index of the array is the tile and the second one is the
   * resolution level.
   */
  private ArrayList<Long>[][] relevantPrecinctsR_CP = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param logicalTarget definition in {@link #logicalTarget}.
   * @param serverCache definition in {@link #serverCache}.
   */
  public FileOrderDelivery(JP2KServerLogicalTarget logicalTarget, ServerCacheModel serverCache) {
    this.logicalTarget = logicalTarget;
    this.serverCache = serverCache;

    // Initializations
    responseViewWindow = new ViewWindowField();
    responseViewWindow.reset();
    responseLength = 0;

    // Copy parameters
    codestream = logicalTarget.getCodestream(0);
    tileObj = codestream.getTile(0);
    maxNumLayers = codestream.getNumLayers();
    progressionOrder = codestream.getProgressionOrder();


    // Set end of response code
    EORReasonCode = EORCodes.WINDOW_DONE;
  }

  /**
   * Sets the {@link #progressionOrder} attribute.
   *
   * @param progressionOrder definition in {@link #progressionOrder}.
   */
  public void setDeliveryProgressionOrder(int progressionOrder) {
    this.progressionOrder = progressionOrder;
  }

  /**
   * Calculates the WOI which will be sent to the client using a layers-based
   * rate-distortion method (file is delivery in the same order that is is
   * saved).
   *
   * @param viewWindow the requested Window Of Interest
   * @throws ErrorException
   */
  public void runResponseParameters(ViewWindowField viewWindow) throws IllegalArgumentException, ErrorException {

    // Check input parameters
    if (viewWindow == null) {
      throw new NullPointerException();
    }

    // If the frame size has been omitted, no compressed image data is sent.
    if ((viewWindow.fsiz[0] < 0) && (viewWindow.fsiz[1] < 0)) {
      responseViewWindow = new ViewWindowField();
      relevantPrecincts = new ArrayList<Long>();
      responseDataList = new ArrayList<ResponseData>();
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
    if (actualViewWindow.layers < 0) {
      actualViewWindow.layers = codestream.getNumLayers();
    }
    if (actualViewWindow.layers > codestream.getNumLayers()) {
      actualViewWindow.layers = codestream.getNumLayers();
    }


    if ((actualViewWindow.roff[0] >= actualViewWindow.fsiz[0])
            || (actualViewWindow.roff[1] >= actualViewWindow.fsiz[1])) {
      throw new ErrorException("");
    }

    // Get the number of discard levels
    discardLevels = JPEG2000Util.determineNumberOfDiscardLevels(
            codestream.getXSize(), codestream.getYSize(),
            codestream.getXOSize(), codestream.getYOSize(),
            actualViewWindow.fsiz, actualViewWindow.roundDirection,
            codestream.getMaxResolutionLevels());

    // Fit requested region to the suitable resolution
    codestream.mapRegionToSuitableResolutionGrid(actualViewWindow.fsiz,
            actualViewWindow.roff,
            actualViewWindow.rsiz,
            discardLevels);

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

    //System.out.println("VIEW WINDOW="+viewWindow.toString());
    //System.out.println("ACTUAL VIEW WINDOW="+actualViewWindow.toString());
    //System.out.println("RESPONSE VIEW WINDOW="+responseViewWindow.toString());

    // FIND PRECINCTS WHICH ARE RELEVANTS FOR THE VIEW-WINDOW
    switch (progressionOrder) {
      case 0:
        relevantPrecincts =
                RelevantPrecinctsFinder.TRCPOrder(codestream, actualViewWindow,
                discardLevels);
        break;
      case 1:
        relevantPrecinctsR_CP =
                RelevantPrecinctsFinder.TR_CPOrder(codestream, actualViewWindow,
                discardLevels);
        break;
      case 2:
        relevantPrecincts =
                RelevantPrecinctsFinder.TRPCOrder(codestream, actualViewWindow,
                discardLevels);
        break;

      case 3:
        relevantPrecincts =
                RelevantPrecinctsFinder.TPCROrder(codestream, actualViewWindow,
                discardLevels);
        break;

      case 4:
        relevantPrecincts =
                RelevantPrecinctsFinder.TCPROrder(codestream, actualViewWindow,
                discardLevels);
        break;

      default:
        assert (true);

    }

    // Adjust the quality response parameter to the quality layers.
    // It is a coarse estimation and it should be improved (but is the easiest method)
    quality = (int) (100D / (double) maxNumLayers) * layers;

  }

  /**
   *
   * @param jpipMessageHeaders definition in {@link #jpipMessageHeaders}
   * @param maximumResponseLength definition in {@link #maximumResponseLength}
   *
   * @throws ErrorException
   * @throws IOException
   */
  public void runResponseData(ArrayList<ResponseData> responseDataList, long maximumResponseLength) throws ErrorException {

    // Copy input parameters
    this.responseDataList = responseDataList;
    this.maximumResponseLength = maximumResponseLength;

    switch (progressionOrder) {
      case 0:
        runResponseDataLayerPosition(relevantPrecincts);
        break;

      case 1:
        runResponseDataRLCP(relevantPrecinctsR_CP);
        break;

      case 2:
        runResponseDataPositionLayer(relevantPrecincts);
        break;

      case 3:
        runResponseDataPositionLayer(relevantPrecincts);
        break;

      case 4:
        runResponseDataPositionLayer(relevantPrecincts);
        break;

      default:
        assert (true);
    }
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
   * Prints this File Order Delivery out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out
   *           an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- File Order Delivery --");

    out.println("Not implemented yet");

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   *
   */
  private void runResponseDataLayerPosition(ArrayList<Long> relevantPrecincts) throws ErrorException {

    boolean finish = false;
    int numberOfRelevantPrecincts = relevantPrecincts.size();
    //System.out.println("Number of relevant precincts: " + numberOfRelevantPrecincts);

    // Loop on layers
    for (int layer = 0; layer < actualViewWindow.layers && !finish; layer++) {
      //System.out.println("\n=> layer="+layer);
      // Loop on precincts
      for (int i = 0; i < numberOfRelevantPrecincts && !finish; i++) {
        long inClassIdentifier = relevantPrecincts.get(i);
        //System.out.println("precinct="+inClassIdentifier);

        int lengthOfDataBinSent = (int) serverCache.getPrecinctDataBinLength(inClassIdentifier);
        //System.out.println("Client has: " + lengthOfDataBinSent +" (bytes)");

        int layerOfDataSent = logicalTarget.getLastCompleteLayer(inClassIdentifier, lengthOfDataBinSent);
        //System.out.println("   client has: " + layerOfDataSent + " in its cache");	// DEBUG

        // Check if data of this layer has been sent
        if (layerOfDataSent > layer) {
          continue;
        }

        // Sends data (or a piece) of this layer
        long offset = logicalTarget.getPacketOffsetWithDataBin(inClassIdentifier, layer);

        int packetLength = logicalTarget.getPacketLength(inClassIdentifier, layer);
        //System.out.println("   layer=" + layer);
        //System.out.println("   offset=" + offset + " packet length=" + packetLength + "  total=" + (offset+packetLength));
        //System.out.println("   Precinct Data Bin Length="+ logicalTarget.getDataBinLength(inClassIdentifier));
        boolean lastByte = ((offset + packetLength) == logicalTarget.getDataBinLength(inClassIdentifier)) ? true : false;

        if (maximumResponseLength != -1) {
          if (responseLength + packetLength >= maximumResponseLength) {
            packetLength = (int) (maximumResponseLength - responseLength);
            EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;
            lastByte = false;
            finish = true;
          }
          responseLength += packetLength;
        }

        int lastCompleteLayer = logicalTarget.getLastCompleteLayer(inClassIdentifier, (offset + packetLength));

        //System.out.println("   Last complete layer=" + lastCompleteLayer);
        JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.EXTENDED_PRECINCT, inClassIdentifier, offset, packetLength, lastByte, lastCompleteLayer);
        // DEBUG
        int[] TCRP = codestream.findTCRP(inClassIdentifier);
        int tile = TCRP[0], z = TCRP[1], rLevel = TCRP[2], precinct = TCRP[3];
        //System.out.println(" - Sending: PrecinctID="+inClassIdentifier+"(z="+z+" r="+rLevel+" p="+precinct+") layer="+layer+" Data Length: "+packetLength+" Offset: "+offset);
        // END DEBUG

        ArrayList<Long> filePointers = new ArrayList<Long>();
        filePointers.add((long) logicalTarget.getPacketOffset(inClassIdentifier, layer));
        ArrayList<Integer> lengths = new ArrayList<Integer>();
        lengths.add(packetLength);
        responseDataList.add(new ResponseData(jpipMessageHeader, filePointers, lengths));
      }
    }

    relevantPrecincts.clear();
  }

  /**
   *
   *
   */
  private void runResponseDataPositionLayer(ArrayList<Long> relevantPrecincts) throws ErrorException {

    boolean finish = false;
    int numberOfRelevantPrecincts = relevantPrecincts.size();
    //System.out.println("Number of relevant precincts: " + numberOfRelevantPrecincts);

    for (int i = 0; i < numberOfRelevantPrecincts && !finish; i++) {

      long inClassIdentifier = relevantPrecincts.get(i);

      // DEBUG
			/*int[] TCRP = JPEG2000Util.InClassIdentifierToTCRP(inClassIdentifier, 1, zSize, xSize, ySize, XOsize, YOsize, WTLevels, jpcParameters.resolutionPrecinctWidths, jpcParameters.resolutionPrecinctHeights);
      //int tile 	 = TCRP[0];
      int z 		 = TCRP[1];
      int rLevel 	 = TCRP[2];
      int precinct = TCRP[3];

      System.out.println("JPIP MESSAGE SERVER: inClassID" + inClassIdentifier + " -> z="+z + " r="+rLevel+" p="+ precinct);
       */

      //System.out.print("\n-- precinct: "+ inClassIdentifier + "  --");		// DEBUG
      //System.out.println("Response length: " + responseLength);
      // END DEBUG

      int lengthOfDataBinSent = (int) serverCache.getPrecinctDataBinLength(inClassIdentifier);
      //System.out.println("Client has: " + lengthOfDataBinSent +" (bytes)");

      int layerOfDataSent = logicalTarget.getLastCompleteLayer(inClassIdentifier, lengthOfDataBinSent);

      //System.out.println("Client has: " + layerOfDataSent + " layers of " + layers + " in its cache");	// DEBUG

      // Sends part of last layer
      if (layerOfDataSent < layers) {

        //	Send the piece of layer
        long offset = logicalTarget.getPacketOffsetWithDataBin(inClassIdentifier, layerOfDataSent);
        long packetFilePointer = logicalTarget.getPacketOffset(inClassIdentifier, layerOfDataSent);
        int packetLength = logicalTarget.getPacketLength(inClassIdentifier, layerOfDataSent);
        boolean lastByte = ((offset + packetLength) == logicalTarget.getDataBinLength(inClassIdentifier)) ? true : false;
        int lastCompleteLayer = logicalTarget.getLastCompleteLayer(inClassIdentifier, (offset + packetLength));
        //System.out.println("Last complete layer: " + lastCompleteLayer);
        //System.out.println("Sended Offset: " + lengthOfDataBinSent);
        //System.out.println("Packet Offset: " + offset + " Packet Length: " + packetLength + "  Total: " + (offset+packetLength));

        // Update pointer and length
        packetFilePointer += (lengthOfDataBinSent - offset);
        packetLength -= (lengthOfDataBinSent - offset);

        //System.out.println("Updated data");
        //System.out.println("Layer Offset: " + offset + " Packet Length: " + packetLength + "  Total: " + (offset+packetLength));
        //System.out.println("Precinct Data Bin Length: "+ logicalTarget.getDataBinLength(inClassIdentifier));

        if (packetLength > 0) {
          //System.out.println(" - Sending: identifier=" + inClassIdentifier + " layer= " +layerOfDataSent + " Data Length: " + packetLength + " Offset: " + offset);
          if (maximumResponseLength != -1) {
            if (responseLength + packetLength >= maximumResponseLength) {
              packetLength = (int) (maximumResponseLength - responseLength);
              EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;
              lastByte = false;
              finish = true;
            }
            responseLength += packetLength;
          }


          //System.out.println("Packet length: " + packetLength);
          JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.EXTENDED_PRECINCT, inClassIdentifier, offset, packetLength, lastByte, lastCompleteLayer);
          //System.out.println(" - identifier: " + inClassIdentifier + " layer: " +layerOfDataSent + " Data Length: " + packetLength + " Offset: " + offset);
          ArrayList<Long> filePointers = new ArrayList<Long>();
          filePointers.add((long) logicalTarget.getPacketOffset(inClassIdentifier, layerOfDataSent));
          ArrayList<Integer> lengths = new ArrayList<Integer>();
          lengths.add(packetLength);
          responseDataList.add(new ResponseData(jpipMessageHeader, filePointers, lengths));
        }
      }

      // Sends the rest of the layers
      for (int layer = layerOfDataSent + 1; layer < layers && !finish; layer++) {

        long offset = logicalTarget.getPacketOffsetWithDataBin(inClassIdentifier, layer);
        int packetLength = logicalTarget.getPacketLength(inClassIdentifier, layer);

        //System.out.println("   layer=" + layer);
        //System.out.println("   offset=" + offset + " packet length=" + packetLength + "  total=" + (offset+packetLength));
        //System.out.println("   Precinct Data Bin Length="+ logicalTarget.getDataBinLength(inClassIdentifier));
        boolean lastByte = ((offset + packetLength) == logicalTarget.getDataBinLength(inClassIdentifier)) ? true : false;

        if (maximumResponseLength != -1) {
          if (responseLength + packetLength >= maximumResponseLength) {
            packetLength = (int) (maximumResponseLength - responseLength);
            EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;
            lastByte = false;
            finish = true;
          }
          responseLength += packetLength;
        }

        int lastCompleteLayer = logicalTarget.getLastCompleteLayer(inClassIdentifier, (offset + packetLength));

        //System.out.println("   Last complete layer=" + lastCompleteLayer);
        JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.EXTENDED_PRECINCT, inClassIdentifier, offset, packetLength, lastByte, lastCompleteLayer);

        //System.out.println(" - Sending: identifier: " + inClassIdentifier + " layer: " +layer + " Data Length: " + packetLength + " Offset: " + offset);
        //System.out.println(" - identifier: " + inClassIdentifier + " layer: " +layer + " Data Length: " + packetLength + " Offset: " + offset);
        ArrayList<Long> filePointers = new ArrayList<Long>();
        filePointers.add((long) logicalTarget.getPacketOffset(inClassIdentifier, layer));
        ArrayList<Integer> lengths = new ArrayList<Integer>();
        lengths.add(packetLength);
        responseDataList.add(new ResponseData(jpipMessageHeader, filePointers, lengths));
      }

    }

    relevantPrecincts.clear();

  }

  /**
   *
   *
   */
  private void runResponseDataRLCP(ArrayList<Long>[][] relevantPrecinctsR_CP) throws ErrorException {

    boolean finish = false;
    int numResolutionLevels = relevantPrecinctsR_CP[0].length;

    // Loop on resolution levels
    for (int rLevel = 0; rLevel < numResolutionLevels && !finish; rLevel++) {

      // Loop on layers
      for (int layer = 0; layer < layers && !finish; layer++) {
        int numberOfRelevantPrecincts = relevantPrecinctsR_CP[0][rLevel].size();
        //System.out.println("Number of relevant precincts: " + numberOfRelevantPrecincts);

        // Loop on precincts
        for (int i = 0; i < numberOfRelevantPrecincts && !finish; i++) {

          long inClassIdentifier = relevantPrecinctsR_CP[0][rLevel].get(i);

          // DEBUG
					/*int[] TCRP = JPEG2000Util.InClassIdentifierToTCRP(inClassIdentifier, 1, zSize, xSize, ySize, XOsize, YOsize, WTLevels, jpcParameters.codParameters.precinctWidths, jpcParameters.codParameters.precinctHeights);
          //int tile 	 = TCRP[0];
          int z 		 = TCRP[1];
          int precinct = TCRP[3];
          System.out.println("JPIP MESSAGE SERVER: inClassID " + inClassIdentifier + " -> z="+z + " r="+rLevel+" p="+ precinct);*/


          //int[] TCP = JPEG2000Util.InClassIdentifierToTCP(inClassIdentifier, 1, 224);
          //System.out.println("identifier: " + inClassIdentifier + " -->  z: " + TCP[1] + " P: " + TCP[2] );
          //System.out.print("\n-- precinct: "+ inClassIdentifier + "  --");		// DEBUG
          //System.out.println("Response length: " + responseLength);

          int lengthOfDataBinSent = (int) serverCache.getPrecinctDataBinLength(inClassIdentifier);
          //System.out.println("Client has: " + lengthOfDataBinSent +" (bytes)");

          int layerOfDataSent = logicalTarget.getLastCompleteLayer(inClassIdentifier, lengthOfDataBinSent);
          //System.out.println("   client has: " + layerOfDataSent + " in its cache");	// DEBUG

          // Check if data of this layer has been sent
          if (layerOfDataSent > layer) {
            continue;
          }

          // Sends data (or a piece) of this layer
          long offset = logicalTarget.getPacketOffsetWithDataBin(inClassIdentifier, layer);
          int packetLength = logicalTarget.getPacketLength(inClassIdentifier, layer);
          //System.out.println("   layer=" + layer);
          //System.out.println("   offset=" + offset + " packet length=" + packetLength + "  total=" + (offset+packetLength));
          //System.out.println("   Precinct Data Bin Length="+ logicalTarget.getDataBinLength(inClassIdentifier));
          boolean lastByte = ((offset + packetLength) == logicalTarget.getDataBinLength(inClassIdentifier)) ? true : false;

          if (maximumResponseLength != -1) {
            if (responseLength + packetLength >= maximumResponseLength) {
              packetLength = (int) (maximumResponseLength - responseLength);
              EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;
              lastByte = false;
              finish = true;
            }
            responseLength += packetLength;
          }

          int lastCompleteLayer = logicalTarget.getLastCompleteLayer(inClassIdentifier, (offset + packetLength));

          //System.out.println("   Last complete layer=" + lastCompleteLayer);
          JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.EXTENDED_PRECINCT, inClassIdentifier, offset, packetLength, lastByte, lastCompleteLayer);
          //System.out.println(" - Sending: identifier: " + inClassIdentifier + " layer: " +layer + " Data Length: " + packetLength + " Offset: " + offset);

          ArrayList<Long> filePointers = new ArrayList<Long>();
          filePointers.add((long) logicalTarget.getPacketOffset(inClassIdentifier, layer));
          ArrayList<Integer> lengths = new ArrayList<Integer>();
          lengths.add(packetLength);
          responseDataList.add(new ResponseData(jpipMessageHeader, filePointers, lengths));
        }

      }
    }

    for (int t = 0; t < relevantPrecinctsR_CP.length; t++) {
      for (int r = 0; r < relevantPrecinctsR_CP[t].length; r++) {
        relevantPrecinctsR_CP[t][r].clear();
      }
    }
    relevantPrecinctsR_CP = null;
  }
}
