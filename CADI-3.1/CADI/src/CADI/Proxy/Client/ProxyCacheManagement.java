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
package CADI.Proxy.Client;

import java.io.PrintStream;
import java.util.ArrayList;

import CADI.Common.Cache.CacheManagement;
import CADI.Common.Cache.DataBin;
import CADI.Common.Cache.MainHeaderDataBin;
import CADI.Common.Cache.PrecinctDataBin;
import CADI.Common.LogicalTarget.JPEG2000.RelevantPrecinct;
import CADI.Common.Network.JPIP.ClassIdentifiers;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Proxy.Core.SendDataInfo;
import CADI.Proxy.LogicalTarget.JPEG2000.ProxyJPEG2KCodestream;
import CADI.Proxy.Server.ProxyCacheModel;

/**
 * This class is used to save a logical target that is being cached by
 * the CADIProxy. It extends the basic class {@link CacheManagement} adding new
 * features useful for CADIProxy.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.3 2012/03/09
 */
public class ProxyCacheManagement extends CacheManagement {

  /**
   * This flag allows that when a precinct is partialy in the cache, the
   * available data is delivered to client without waiting for the remainder.
   * Although it reduces delivering times it also increases JPIP message headers
   * because the same packet must be signaled twice.
   */
  private boolean ALLOW_PARTIAL_PRECINCTS = true;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public ProxyCacheManagement() {
    super();
  }

  /**
   * 
   * @param viewWindow
   * @param relevantPrecincts
   * @param cacheModel
   * @param availableData
   * @param unAvailableData 
   */
  public void checkAvailableData(ViewWindowField viewWindow,
          ArrayList<RelevantPrecinct> relevantPrecincts,
          ProxyCacheModel cacheModel,
          ArrayList<SendDataInfo> availableData,
          ArrayList<SendDataInfo> unAvailableData) {

    // Check input parameters
    if (viewWindow == null) {
      throw new NullPointerException();
    }
    if (relevantPrecincts == null) {
      throw new NullPointerException();
    }
    if (cacheModel == null) {
      throw new NullPointerException();
    }
    if (availableData == null) {
      throw new NullPointerException();
    }
    if (unAvailableData == null) {
      throw new NullPointerException();
    }
    assert (codestream != null);

    SendDataInfo sendDataInfo = null;

    // MAIN HEADER
    if (!isComplete(ClassIdentifiers.MAIN_HEADER, 0)) {
      assert (true);
    }
    int lengthInClient = cacheModel.getMainHeaderLength();
    if (lengthInClient < mainHeaderDataBin.getLength()) {
      sendDataInfo = new SendDataInfo(SendDataInfo.MAIN_HEADER, 0);
      sendDataInfo.bytesOffset = lengthInClient;
      sendDataInfo.bytesLength = (int)mainHeaderDataBin.getLength() - lengthInClient;
      sendDataInfo.layersOffset = 0;
      sendDataInfo.layersLength = 0;
      availableData.add(sendDataInfo);
    }

    // TILE HEADER
    lengthInClient = cacheModel.getDataBinLength(ClassIdentifiers.TILE_HEADER, 0);
    if (getDatabinLength(ClassIdentifiers.TILE_HEADER, 0) <= 0) {
      sendDataInfo = new SendDataInfo(SendDataInfo.TILE_HEADER, 0);
      sendDataInfo.bytesOffset = lengthInClient;
      unAvailableData.add(sendDataInfo);
    } else {
      int thLength = (int)getDatabinLength(ClassIdentifiers.TILE_HEADER, 0);
      if (lengthInClient < thLength) {
        sendDataInfo = new SendDataInfo(SendDataInfo.TILE_HEADER, 0);
        sendDataInfo.bytesOffset = lengthInClient;
        sendDataInfo.bytesLength = thLength - lengthInClient;
        availableData.add(sendDataInfo);
      }
    }

    // PRECINCTS
    for (RelevantPrecinct relevantPrecinct : relevantPrecincts) {
      long inClassIdentifier = relevantPrecinct.inClassIdentifier;
      //System.out.println("\n"+relevantPrecinct.toStringSummary()); // DEBUG
      // Data cached in proxy's cache
      PrecinctDataBin dataBin = (PrecinctDataBin)getDataBin(DataBin.PRECINCT, inClassIdentifier);

      // Data bin has not cached yet, must be fetched from server
      if (dataBin == null) {
        //System.out.println("\t=> DATABIN: has not been cached yet!!!!!."); // DEBUG
        sendDataInfo = new SendDataInfo(SendDataInfo.PRECINCT, inClassIdentifier);
        sendDataInfo.layersOffset = relevantPrecinct.startLayer;
        sendDataInfo.layersLength = relevantPrecinct.endLayer - relevantPrecinct.startLayer;
        sendDataInfo.bytesOffset = (int)relevantPrecinct.msgOffset;
        //sendDataInfo.bytesLength = (int)relevantPrecinct.msgLength;
        unAvailableData.add(sendDataInfo);
        continue;
      }

      dataBin.lock();

      int numCachedPackets = dataBin.getNumCompletePackets();

      //System.out.println("\n" + relevantPrecinct.toStringSummary()); // DEBUG
      //System.out.println(dataBin.toStringShort()); // DEBUG

      // The whole precinct, or part, has been cached
      if (relevantPrecinct.endLayer <= numCachedPackets) {
        // Available in proxy's cache
        sendDataInfo = new SendDataInfo(SendDataInfo.PRECINCT, inClassIdentifier);
        sendDataInfo.layersOffset = relevantPrecinct.startLayer;
        sendDataInfo.layersLength = relevantPrecinct.endLayer - relevantPrecinct.startLayer;
        sendDataInfo.bytesOffset = (int)relevantPrecinct.msgOffset;
        sendDataInfo.bytesLength = (int)relevantPrecinct.msgLength;
        availableData.add(sendDataInfo);
      } else if (relevantPrecinct.startLayer >= numCachedPackets) {
        sendDataInfo = new SendDataInfo(SendDataInfo.PRECINCT, inClassIdentifier);
        sendDataInfo.layersOffset = relevantPrecinct.startLayer;
        sendDataInfo.layersLength = relevantPrecinct.endLayer - relevantPrecinct.startLayer;
        sendDataInfo.bytesOffset = (int)relevantPrecinct.msgOffset;
        //sendDataInfo.bytesLength = (int)relevantPrecinct.msgLength;
        unAvailableData.add(sendDataInfo);
      } else {
        // Only a part is available in the proxy's cache
        if (ALLOW_PARTIAL_PRECINCTS) {
          // Available data
          sendDataInfo = new SendDataInfo(SendDataInfo.PRECINCT, inClassIdentifier);
          sendDataInfo.layersOffset = relevantPrecinct.startLayer;
          sendDataInfo.layersLength = numCachedPackets - relevantPrecinct.startLayer;
          sendDataInfo.bytesOffset = (int)relevantPrecinct.msgOffset;
          sendDataInfo.bytesLength = (int)(dataBin.getLength() - relevantPrecinct.msgOffset);
          availableData.add(sendDataInfo);

          int tmpOffset = sendDataInfo.bytesOffset + sendDataInfo.bytesLength;

          // Unavailable data
          sendDataInfo = new SendDataInfo(SendDataInfo.PRECINCT, inClassIdentifier);
          sendDataInfo.layersOffset = numCachedPackets;
          sendDataInfo.layersLength = relevantPrecinct.endLayer - numCachedPackets;
          sendDataInfo.bytesOffset = tmpOffset;
          unAvailableData.add(sendDataInfo);

        } else {

          sendDataInfo = new SendDataInfo(SendDataInfo.PRECINCT, inClassIdentifier);
          sendDataInfo.layersOffset = relevantPrecinct.startLayer;
          sendDataInfo.layersLength = relevantPrecinct.endLayer - relevantPrecinct.startLayer;
          sendDataInfo.bytesOffset = (int)relevantPrecinct.msgOffset;
          unAvailableData.add(sendDataInfo);
        }
      }
      dataBin.unlock();

    }
  }

  /**
   *
   * @param viewWindow
   * @param sentData
   *
   * @return
   */
  public void getRemainderData(ProxyCacheModel cacheModel,
          ArrayList<SendDataInfo> unAvailableData) {
    // Check input parameters
    if (unAvailableData == null) {
      throw new NullPointerException();
    }

    for (SendDataInfo dataInfo : unAvailableData) {

      // MAIN HEADER
      if (dataInfo.classIdentifier == SendDataInfo.MAIN_HEADER) {

        if (!isComplete(ClassIdentifiers.MAIN_HEADER, 0)) {
          assert (true);
        }

        MainHeaderDataBin dataBin = (MainHeaderDataBin)getDataBin(DataBin.MAIN_HEADER, 0);
        dataInfo.bytesLength = (int)(dataBin.getLength() - dataInfo.bytesOffset);

        continue;
      } else if (dataInfo.classIdentifier == SendDataInfo.TILE_HEADER) {

        dataInfo.bytesLength = (int)getDatabinLength(ClassIdentifiers.TILE_HEADER, 0) - dataInfo.bytesOffset;

      } else if (dataInfo.classIdentifier == SendDataInfo.PRECINCT) {

        // PRECINCT
        long inClassIdentifier = dataInfo.inClassIdentifier;

        // Data cached in proxy's cache
        PrecinctDataBin dataBin = (PrecinctDataBin)getDataBin(DataBin.PRECINCT, inClassIdentifier);

        if (dataBin == null) {
          // None data has been fetched from the server, then data of this
          // relevant precinct cannot be delivered to the client
          dataInfo.bytesOffset = 0;
          dataInfo.bytesLength = 0;
          dataInfo.layersOffset = 0;
          dataInfo.layersLength = 0;

        } else {
          dataBin.lock();
          // The whole precinct, or part, has been cached
          int numCachedPackets = dataBin.getNumCompletePackets();

          if (dataInfo.layersOffset + dataInfo.layersLength <= numCachedPackets) {
            // Available in proxy's cache.
            int endLayer = dataInfo.layersOffset + dataInfo.layersLength;
            dataInfo.bytesLength = dataBin.getPacketOffset(endLayer) + dataBin.getPacketLength(endLayer) - dataInfo.bytesOffset;

          } else if (dataInfo.layersOffset < numCachedPackets) {
            // Only a part is available in the proxy's cache
            // Available data
            dataInfo.layersLength = numCachedPackets - dataInfo.layersOffset;
            dataInfo.bytesLength = dataBin.getPacketOffset(numCachedPackets) + dataBin.getPacketLength(numCachedPackets) - dataInfo.bytesOffset;
          } else {
            // None data has been fetched from the server, then data of this
            // relevant precinct cannot be delivered to the client
            dataInfo.bytesOffset = 0;
            dataInfo.bytesLength = 0;
            dataInfo.layersOffset = 0;
            dataInfo.layersLength = 0;
          }
          dataBin.unlock();
        }

      }
    }
  }

  /**
   * Returns the {@link #codestream} attribute.
   *
   * @return definition in {@link #codestream}.
   */
  public final ProxyJPEG2KCodestream getProxyJPEG2KCodestream() {
    return (ProxyJPEG2KCodestream)codestream;
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
   * Prints this Proxy Logical Target fields out to the specified output
   * stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Proxy Cache Management --");

    super.list(out);

    out.flush();
  }
  
  // ============================ private methods ==============================
}
