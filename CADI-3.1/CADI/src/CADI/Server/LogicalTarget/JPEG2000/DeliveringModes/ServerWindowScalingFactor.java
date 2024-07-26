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
package CADI.Server.LogicalTarget.JPEG2000.DeliveringModes;

import java.io.PrintStream;
import java.util.ArrayList;

import CADI.Common.LogicalTarget.JPEG2000.RelevantPrecinct;
import CADI.Common.LogicalTarget.JPEG2000.WindowScalingFactor;
import CADI.Common.Network.JPIP.JPIPMessageHeader;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Server.Cache.ServerCacheModel;
import CADI.Server.Core.ResponseData;
import CADI.Server.LogicalTarget.JPEG2000.JP2KServerLogicalTarget;
import GiciException.ErrorException;

/**
 * This class implements an extension of the
 * {@link CADI.Common.LogicalTarget.JPEG2000.WindowScalingFactor} class to be
 * used by the CADIServer.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/01/11
 */
public class ServerWindowScalingFactor extends WindowScalingFactor {

  /**
   * Definition in {@link CADI.Server.LogicalTarget.JPEG2000.JP2KServerLogicalTarget}.
   */
  private JP2KServerLogicalTarget logicalTarget = null;

  /**
	 * 
	 */
  private ArrayList<ResponseData> responseDataList = null;

  // ============================= public methods ==============================
  /**
	 * Constructor.
	 * 
	 * @param logicalTarget definition in {@link #logicalTarget}.
	 * @param serverCache definition in {@link #serverCache}.
	 */
  public ServerWindowScalingFactor(JP2KServerLogicalTarget logicalTarget,
          ServerCacheModel serverCache) {
    super(logicalTarget, serverCache, logicalTarget.getCodestream(0));

    if (logicalTarget == null) throw new NullPointerException();
    if (serverCache == null) throw new NullPointerException();

    this.logicalTarget = logicalTarget;
    this.serverCache = serverCache;
  }

  /**
	 * Constructor.
	 * 
	 * @param logicalTarget
	 * @param serverCache
	 * @param align
	 */
  public ServerWindowScalingFactor(JP2KServerLogicalTarget logicalTarget,
          ServerCacheModel serverCache,
          boolean align) {
    super(logicalTarget, serverCache, logicalTarget.getCodestream(0), align);

    if (logicalTarget == null) throw new NullPointerException();
    if (serverCache == null) throw new NullPointerException();

    this.logicalTarget = logicalTarget;
    this.serverCache = serverCache;
  }

  /**
	 * Calculates the WOI which will be sent to the client using a layers-based
	 * rate-distortion method (file is delivery in the same order that is is
	 * saved). 
	 * 
	 * @param viewWindow the requested Window Of Interest
	 * @throws ErrorException 
	 */
  @Override
  public void runResponseParameters(ViewWindowField viewWindow)
          throws IllegalArgumentException, ErrorException {

    super.runResponseParameters(viewWindow);
  }

  /**
	 * 
	 * @param maximumResponseLength definition in {@link #maximumResponseLength}
	 * 
	 * @throws ErrorException
	 */
  public void runResponseData(ArrayList<ResponseData> responseDataList,
          long maximumResponseLength) throws ErrorException {

    this.responseDataList = responseDataList;

    ArrayList<RelevantPrecinct>relevantPrecincts = runResponseData(maximumResponseLength);

    ArrayList<Long> filePointers = null;
    ArrayList<Integer> lengths = null;

    // Read relevant precincts and save in response data list
    for (RelevantPrecinct precinct : relevantPrecincts) {

      if ((precinct.msgOffset >= 0) && (precinct.msgLength > 0)) {
        boolean lastByte = ((precinct.msgOffset + precinct.msgLength) == logicalTarget.getDataBinLength(precinct.inClassIdentifier)) ? true : false;
        int lastCompleteLayer = logicalTarget.getLastCompleteLayer(precinct.inClassIdentifier, (precinct.msgOffset + precinct.msgLength));
        JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.EXTENDED_PRECINCT, precinct.inClassIdentifier, precinct.msgOffset, precinct.msgLength, lastByte, lastCompleteLayer);

        filePointers = new ArrayList<Long>();
        lengths = new ArrayList<Integer>();
        int cumLength = 0;

        for (int nlayer = precinct.startLayer; (nlayer < precinct.actualNumPacket); nlayer++) {
          int packetLength = logicalTarget.getPacketLength(precinct.inClassIdentifier, nlayer);
          if (cumLength + packetLength > precinct.msgLength) {
            packetLength = (int)precinct.msgLength - cumLength;
          }
          if (packetLength <= 0) {
            break;
          }
          filePointers.add((long)logicalTarget.getPacketOffset(precinct.inClassIdentifier, nlayer));
          lengths.add(packetLength);
          cumLength += packetLength;
        }

        this.responseDataList.add(new ResponseData(jpipMessageHeader, filePointers, lengths));
      }
    }

    // Free memory
    relevantPrecincts.clear();
    relevantPrecincts = null;
  }

  /**
	 * Returns the {@link #jpipMessageHeaders} attribute.
	 * 
	 * @return the {@link #jpipMessageHeaders} attribute.
	 */
  public final ArrayList<ResponseData> getResponseData() {
    return responseDataList;
  }

  /*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
  public String toString() {

    String str = "";
    str = getClass().getName() + " [";

    str += super.toString();

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
  @Override
  public void list(PrintStream out) {

    out.println("-- Server Window Scaling Factor --");
    super.list(out);

    out.flush();
  }
  // ============================ private methods ==============================
  
  
  /**
   * Adds the tile-header to the response.
   * <p>
   * It is a temporal method while tiles are not supported.
   */
  private void addTileHeader(int tileIndex) {
    JPIPMessageHeader jpipMessageHeader =
            new JPIPMessageHeader(-1, JPIPMessageHeader.TILE_HEADER, tileIndex, 0, logicalTarget.getTileHeaderLength(tileIndex), true, -1);
    ArrayList<Long> filePointers = new ArrayList<Long>();
    ArrayList<Integer> lengths = new ArrayList<Integer>();
    filePointers.add(logicalTarget.getTileHeaderFilePointer(tileIndex));
    lengths.add(logicalTarget.getTileHeaderLength(tileIndex));
    responseDataList.add(new ResponseData(jpipMessageHeader, filePointers, lengths));

  }
}