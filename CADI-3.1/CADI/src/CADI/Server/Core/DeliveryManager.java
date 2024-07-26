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
package CADI.Server.Core;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import CADI.Common.Log.CADILog;
import CADI.Common.Network.JPIP.JPIPMessageHeader;
import CADI.Server.LogicalTarget.JPEG2000.JP2KServerLogicalTarget;
import CADI.Server.Network.HTTPResponseSender;
import CADI.Common.Network.JPIP.JPIPMessageEncoder;
import GiciException.ErrorException;

/**
 * This class is addressed to send the response to a client.
 *
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/05/31
 */
public class DeliveryManager {

  /**
   *
   */
  private JP2KServerLogicalTarget logicalTarget = null;

  /**
   *
   */
  private HTTPResponseSender httpResponseSender = null;

  /**
   *
   */
  private JPIPMessageEncoder jpipMessageEncoder = null;

  /**
   * An array list to save the JPIP message heders which have been sent
   * as a response to the client. This headers will be used by the
   * {@link CADI.Server.Core.Worker} object to update the server cache.
   */
  private ArrayList<ResponseData> deliveryData = null;

  /**
   *
   */
  private CADILog log = null;

  // DEBUG
  /**
   * Attribute only for debugging purposes.
   */
  private long cumMessageHeadersLength = 0;

  /**
   * Attribute only for debugging purposes.
   */
  private long cumMessageBodiesLength = 0;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param logicalTarget definition in {@link #logicalTarget}.
   * @param jpipMessageHeaders definition in {@link #jpipMessageHeaders}.
   * @param httpResponseSender definition in {@link #httpResponseSender}.
   * @param jpipMessageEncoder definition in {@link #jpipMessageEncoder}.
   */
  public DeliveryManager(JP2KServerLogicalTarget logicalTarget,
          ArrayList<ResponseData> deliveryData,
          HTTPResponseSender httpResponseSender,
          JPIPMessageEncoder jpipMessageEncoder,
          CADILog log) {

    // Check input parameters
    if (logicalTarget == null) {
      throw new NullPointerException();
    }
    if (deliveryData == null) {
      throw new NullPointerException();
    }
    if (httpResponseSender == null) {
      throw new NullPointerException();
    }
    if (jpipMessageEncoder == null) {
      throw new NullPointerException();
    }
    if (log == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.logicalTarget = logicalTarget;
    this.deliveryData = deliveryData;
    this.httpResponseSender = httpResponseSender;
    this.jpipMessageEncoder = jpipMessageEncoder;
  }

  /**
   * Performs the dispatching of the data.
   *
   * @throws IOException if an I/O error has ocurred.
   * @throws ErrorException
   */
  public void run() throws IOException, ErrorException {

    for (ResponseData data : deliveryData) {
      //System.out.println(data.toString()); // DEBUG

      if (data.jpipMessageHeader != null) {
        //System.out.println(data.jpipMessageHeader.toString()); // DEBUG
        if (!data.jpipMessageHeader.isEOR) {
          //System.out.println(data.jpipMessageHeader.toString()); // DEBUG
          byte[] jpipHeader = jpipMessageEncoder.encoderHeader(data.jpipMessageHeader);
          httpResponseSender.sendChunk(jpipHeader);
          cumMessageHeadersLength += jpipHeader.length;
          cumMessageBodiesLength += data.jpipMessageHeader.msgLength;
        } else {
          //System.out.println(data.jpipMessageHeader.toString()); // DEBUG
          String EORReasonMessage = data.jpipMessageHeader.EORReasonMessage;
          int EORReasonCode = data.jpipMessageHeader.EORCode;

          byte[] jpipMessageBody = null;
          long length = 0;
          if (EORReasonMessage != null) {
            jpipMessageBody = EORReasonMessage.getBytes();
            length = jpipMessageBody.length;
          }

          byte[] jpipHeader = jpipMessageEncoder.encoderHeader(new JPIPMessageHeader(EORReasonCode, (int)length));
          httpResponseSender.sendChunks(jpipHeader, jpipMessageBody);
          cumMessageBodiesLength += length;
          cumMessageHeadersLength += jpipHeader.length;
        }
      }

      if (data.chunks != null) {
        for (byte[] chunk : data.chunks) {
          httpResponseSender.sendChunk(chunk);
          chunk = null;
        }

      }

      if (data.filePointers != null) {
        int len = 0;
        byte[] jpipMessageBody = null;
        int size = data.filePointers.size();
        for (int index = 0; index < size; index++) {
          len = data.lengths.get(index);
          if((jpipMessageBody == null) || (jpipMessageBody.length < len)) {
            jpipMessageBody = null;
            jpipMessageBody = new byte[len];
          }
          logicalTarget.readFully(data.filePointers.get(index), jpipMessageBody, 0, len);
          httpResponseSender.sendChunk(jpipMessageBody, 0, len);
        }
      }
    }

    httpResponseSender.endOfChunk();
    httpResponseSender.flush();
  }

  /**
   * Resets the counters {@link #cumMessageHeadersLength} and
   * {@link #cumMessageBodiesLength}.
   */
  public void resetLengthCounters() {
    cumMessageHeadersLength = 0;
    cumMessageBodiesLength = 0;
  }

  public long getCumMessageHeadersLength() {
    return cumMessageHeadersLength;
  }

  public long getCumMessageBodiesLength() {
    return cumMessageBodiesLength;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";

    str += "Not implemented yet!";

    str += "]";
    return str;
  }

  /**
   * Prints this Logical Target List fields out to the specified output
   * stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Delivery Managager --");

    out.println("Not implemented yet!");

    out.flush();
  }
  // ============================ private methods ==============================
}
