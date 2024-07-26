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
package CADI.Proxy.Core;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ProtocolException;
import java.net.Socket;

import CADI.Client.Network.HTTPClient;
import CADI.Client.Network.JPIPMessageDecoder;
import CADI.Common.Log.CADILog;
import CADI.Common.Network.HTTP.HTTPRequest;
import CADI.Common.Network.HTTP.HTTPResponse;
import CADI.Common.Network.HTTP.StatusCodes;
import CADI.Common.Network.JPIP.JPIPMessage;
import CADI.Common.Network.JPIP.JPIPResponseFields;
import CADI.Server.Network.HTTPResponseSender;
import CADI.Common.Network.JPIP.JPIPMessageEncoder;
import CADI.Server.Request.RequestQueue;
import CADI.Server.Request.RequestQueueNode;
import GiciException.ErrorException;
import GiciException.WarningException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class implements a transparent JPIP proxy. It is the most simple kind of
 * proxy only performing a forwarding of client request to the server and
 * sending the server response to clients. It does not neither change any of
 * client request parameters nor the server response. 
 * <p>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; setParameters<br>
 * &nbsp; run<br>
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2010/01/13
 */
public class TransparentProxyWorker extends ProxyWorker {

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param threadName
   * @param requestQueue
   * @param log
   */
  public TransparentProxyWorker(String threadName, RequestQueue requestQueue,
                                CADILog log) {
    super(threadName, requestQueue, log);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {

    boolean keepAlive = true;
    boolean error = false;

    log.logInfo(getName() + ": started");

    // Principal loop
    while (!finish) {

      busy = false;

      // Thread is sleeping until a new work is added to the queue
      RequestQueueNode requestNode = null;
      try {
        requestNode = requestQueue.get();
      } catch (InterruptedException ie) {
        finish = true;
        break;
      }

      // Set status to busy
      busy = true;

      this.socket = requestNode.getSocket();
      this.httpRequest = requestNode.getHTTPRequest();

      log.logInfo(getName() + ": responding");

      // Gets the output stream
      try {
        os = socket.getOutputStream();
        httpResponseSender = new HTTPResponseSender(os);
      } catch (IOException e) {
        log.logInfo(getName() + ": error opening output stream");
        // Goes to sleep
        keepAlive = false;
        error = true;
      }

      if (!error) {

        keepAlive = true;

        while (keepAlive && !error && !finish) {

          // Process the client request
          try {
            processRequest(socket, httpRequest, log);
          } catch (WarningException e) {
            error = true;
          } catch (ErrorException e) {
            error = true;
          } catch (IOException e) {
            error = true;
          }


          if (!error) {
            //	Check if keep-alive mode is set
            String value = httpRequest.getHeaderField("Connection");
            if ((value != null) && !value.equals("keep-alive")) {
              // HTTP keep-alive mode is not set. Thread goes to sleepJPIPResponseFields
              keepAlive = false;
              log.logInfo(getName() + ": response has been sent");

            } else { // HTTP keep-alive mode is set, then wait for a new request
              log.logInfo(getName() + ": listening to keep-alive connections");

              try {
                listenNewRequest();
              } catch (WarningException we) {
                int statusCode = we.getErrorCode();
                String reasonPhrase = we.getMessage();
                sendHTTPResponseError(statusCode, reasonPhrase);
                error = true;
              } catch (ErrorException ee) {
                // <<<<<<<<<< to log file
                keepAlive = false;
              }
            }
          }

        } // while (keepAlive && ....

      }
      try {
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
        os = null;
        httpResponseSender = null;
        socket = null;
      } catch (IOException ioe) {
      }

      keepAlive = false;
      error = false;

      // Thread is sleeping until the scheduler sends a signal
      log.logInfo(getName() + ": going to sleep");

    } // while (!finish)

    log.logInfo(getName() + ": shuting down");

    // Free objects
    socket = null;
    httpRequest = null;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";
    super.toString();
    str += "]";
    return str;
  }

  /**
   * Prints this Transparent Proxy Worker out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Transparent Proxy worker --");
    super.list(out);
    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * Performs the forwarding of the client request to the server and the
   * forwarding of the server response to the client.
   *
   * @throws IOException
   * @throws ErrorException
   * @throws WarningException
   * @throws IOException
   */
  public void processRequest(Socket socketToClient, HTTPRequest httpRequest,
                             CADILog log) throws ErrorException, WarningException, IOException {

    cumMessageHeadersLength = 0;
    cumMessageBodiesLength = 0;

    System.out.println();
    //System.out.println("===================================================================================");
    httpRequest.list(System.out);

    String clientHostName = null;
    if (clientHostName == null) {
      try {
        clientHostName = InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException e) {
        clientHostName = "localhost";
      }
    }
    HTTPClient httpClient = new HTTPClient(clientHostName, true, log);
    HTTPResponseSender httpResponseSender = new HTTPResponseSender(socketToClient.getOutputStream());
    JPIPMessageEncoder jpipMessageEncoder = new JPIPMessageEncoder(true);

    // Forward the client request
    httpClient.sendRequest(httpRequest);

    // Check status code of http response
    if (!((httpClient.getStatusCode() == StatusCodes.OK)
            || (httpClient.getStatusCode() == StatusCodes.ACCEPTED))) {
      throw new ErrorException(httpClient.getResponseMessage());
    }

    // Read HTTP response from server
    HTTPResponse httpResponse = httpClient.getHTTPResponse();
    //httpResponse.list(System.out); // DEBUG

    // Send HTTP response headers to client
    httpResponseSender.sendHeaders(httpResponse);
    httpResponseSender.endOfHeaders();


    // READ JPIP HEADERS
    JPIPResponseFields jpipResponseFields = httpClient.getJPIPResponseFields();
    //jpipResponseFields.list(System.out); // DEBUG
     httpResponse.list(System.out); // DEBUG

    if (httpClient.isResponseBody()) {

      // READ HTTP RESPONSE BODY AND DECODES JPIP RESPONSE MESSAGE
      JPIPMessageDecoder jpipMessageDecoder = new JPIPMessageDecoder();
      jpipMessageDecoder.setParameters(httpClient.getHTTPResponseReader());
      JPIPMessage jpipMessage = null;

      try {
        jpipMessage = jpipMessageDecoder.readMessage();
      } catch (ProtocolException e) {
        throw new ErrorException("Data received from server can not be decoded.");
      }

      //jpipMessage.list(System.out); // DEBUG
      if (jpipMessage != null) {

        // Send JPIP message to client
        // System.out.println(); jpipMessage.list(System.out); // DEBUG
        byte[] jpipHeader = jpipMessageEncoder.encoderHeader(jpipMessage.header);
        httpResponseSender.sendChunk(jpipHeader);
        jpipHeader = null;
        httpResponseSender.sendChunk(jpipMessage.messageBody);
        httpResponseSender.flush();

        while (!jpipMessage.header.isEOR) {
          jpipMessage = null;
          try {
            jpipMessage = jpipMessageDecoder.readMessage();
          } catch (ProtocolException e) {
            throw new ErrorException("Data received from server can not be decoded.");
          }
          // System.out.println(); jpipMessage.list(System.out); // DEBUG

          // Send JPIP message to client
          jpipHeader = jpipMessageEncoder.encoderHeader(jpipMessage.header);
          httpResponseSender.sendChunk(jpipHeader);
          cumMessageHeadersLength += jpipHeader.length;
          jpipHeader = null;
          httpResponseSender.sendChunk(jpipMessage.messageBody);
          cumMessageBodiesLength += jpipMessage.header.msgLength;
          httpResponseSender.flush();
        }

      } else {
        throw new ErrorException("Server has not sent any data");
      }
    }
    httpResponseSender.endOfChunk();

    if (httpRequest.getHeaderField("Debug") != null) {
      String uuid = httpRequest.getHeaderField("Debug");
      System.out.println("uuid=" + uuid
              + " Requested_header_length=" + cumMessageHeadersLength + " bytes\n"
              + "uuid=" + uuid
              + " Requested_body_length=" + cumMessageBodiesLength + " bytes");
    }


    httpClient.close();
  }
}
