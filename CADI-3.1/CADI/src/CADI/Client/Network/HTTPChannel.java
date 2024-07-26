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
package CADI.Client.Network;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import CADI.Common.Cache.CacheManagement;
import CADI.Common.Log.CADILog;
import CADI.Common.Network.HTTP.StatusCodes;
import CADI.Common.Network.JPIP.JPIPMessage;
import CADI.Common.Network.JPIP.JPIPResponseFields;
import CADI.Common.Util.StopWatch;
import GiciException.WarningException;

/**
 * This class implements an HTTP channel.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2009/12/29
 */
public class HTTPChannel {

  /**
   * Is the server name where logical targets are located.
   */
  private String server = null;

  /**
   *
   */
  private int port = -1;

  /**
   * Is the proxy server used to connect to the server.
   */
  private String proxyServer = null;

  /**
   *
   */
  private int proxyPort = -1;

  /**
   * An object to send the request and receive the server response.
   */
  private HTTPClient httpClient = null;

  /**
   * Object used to decode the JPIP response messages.
   */
  private JPIPMessageDecoder jpipMessageDecoder = null;

  /**
   *
   */
  private StopWatch stopWatch = null;

  /**
   *
   */
  private CADILog log = null;

  /**
   *
   */
  private JPIPResponseFields jpipResponseFields = null;

  /**
   *
   */
  private CacheManagement cache = null;

  // INTERNAL ATTRIBUTES
  /**
   * Contains the local host name.
   */
  private String clientHostName = null;

  /**
   *
   */
  private ArrayList<String> accept = null;

  /**
   * Contains the downloaded total bytes for the actual logical target.
   */
  private volatile long totalDownloadedBytes = 0;

  /**
   * Is used to save the amount of received bytes in the JPIP message body.
   */
  private long bytesJPIPMessageBody = 0;

  /**
   * Is used to save the amount of received bytes in the JPIP message header.
   */
  private long bytesJPIPMessageHeader = 0;

  // Partial received data
  /**
   * Is the maximum number of records to be saved in the historic.
   */
  private final int MAX_HIST_RECORDS = 50;
  /**
   * An one-dimensional array which tracks last amount of data downloaded.
   * It is useful estimate the download rate.
   * <p>
   * This attribute is related to {@link #histTimes} attribute.
   */
  private long[] histBytes = new long[MAX_HIST_RECORDS];

  /**
   * An one-dimensional array which contains a time-stamp regarding to data
   * recorded at {@link #downloadedBytes}. Both attributes are used
   * to estimate the download read.
   */
  private long[] histTimes = new long[MAX_HIST_RECORDS];
  
  /**
   * An iterator to simulate a circular queue.
   */
  private int iterator = 0;
  
  // ============================= public methods ==============================
  /**
   *
   * @param server
   * @param port
   * @param proxyServer
   * @param proxyPort
   * @param cache
   * @param log
   */
  public HTTPChannel(String server, int port, String proxyServer, int proxyPort, CacheManagement cache, CADILog log) {

    // Check input parameters
    if (server == null) throw new NullPointerException();
    if (port <= 0) throw new IllegalArgumentException();
    if (cache == null) throw new NullPointerException();
    if (log == null) throw new NullPointerException();

    // Copy input parameters
    this.server = server;
    this.port = port;
    this.proxyServer = proxyServer;
    this.proxyPort = proxyPort;
    this.cache = cache;
    this.log = log;


    // Get local host name
    if (clientHostName == null) {
      try {
        clientHostName = InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException e) {
        clientHostName = "localhost";
      }
    }

    accept = new ArrayList<String>();

    httpClient = new HTTPClient(clientHostName, true, log);
    //httpClient.setLocalHost(clientHostName);

    jpipMessageDecoder = new JPIPMessageDecoder();
    stopWatch = new StopWatch();
  }

  /**
   *
   */
  public void close() {
    httpClient.close();
  }

  public void setClientHostName(String clientHostName) {
    httpClient.setLocalHost(clientHostName);
  }

  public void setKeepAlive(boolean keepAlive) {
    httpClient.setKeepAlive(keepAlive);
  }

  /**
   * Returns whether the HTTP keep alive feature is or not set.
   *
   * @return whether the HTTP keep alive feature is or not set.
   */
  public boolean isKeepAlive() {
    return httpClient.isKeepAlive();
  }

  public void setUserAgent(String userAgent) {
    httpClient.setUserAgent(userAgent);
  }

  /**
   * Definition in {@link CADI.Client.Network.HTTPClient#setDebug(java.lang.String)}.
   *
   * @param debug
   */
  public void setDebug(String debug) {
    httpClient.setDebug(debug);
  }

  /**
   * 
   * @param via
   */
  public void setVia(String via) {
    httpClient.setVia(via);
  }

  public void setAccept(ArrayList<String> accept) {
    httpClient.setAccept(accept);
  }

  public JPIPResponseFields getJPIPResponseFields() {
    return jpipResponseFields;
  }

  public String getRequestedURI() {
    return httpClient.getRequestURI();
  }

  /**
   * Get the target which is identified by the URI <code>requestURI</code>
   *
   * @param requestURI the URI to locate the logical target.
   *
   * throws WarningException if an error occurs fetching the target or
   * 			received data cannot be decompressed
   */
  public void getRequest(String requestURI) throws WarningException {

    // SEND REQUEST
    stopWatch.reset();
    stopWatch.start();

    try {
      httpClient.sendRequest(server, port, requestURI, proxyServer, proxyPort);
    } catch (WarningException we) {
      throw new WarningException("It is impossible to send the request to the server \""
                                 + httpClient.getRemoteHost() + "\"");
    } catch (IOException e1) {
      throw new WarningException("It is impossible to send the request to the server \""
                                 + httpClient.getRemoteHost() + "\"");
    }

    //httpClient.getHTTPRequest().list(System.out); // DEBUG
    //httpClient.getHTTPResponse().list(System.out); // DEBUG

    // Check status code of http response
    if (!((httpClient.getStatusCode() == StatusCodes.OK)
          || (httpClient.getStatusCode() == StatusCodes.ACCEPTED))) {
      throw new WarningException(httpClient.getResponseMessage());
    }

    jpipResponseFields = httpClient.getJPIPResponseFields();

    if (httpClient.isResponseBody()) {

      // READ HTTP RESPONSE BODY AND DECODES JPIP RESPONSE MESSAGE
      jpipMessageDecoder.setParameters(httpClient.getHTTPResponseReader());

      JPIPMessage jpipMessage = null;
      try {
        jpipMessage = jpipMessageDecoder.readMessage();
      } catch (ProtocolException e) {
        throw new WarningException("Data received from server can not be decoded.");
      } catch (IOException e) {
        throw new WarningException("I/O error receiving data from server.");
      }
      //System.out.println(jpipMessage.toString()); // jpipMessage.list(System.out); // DEBUG

      // ADDS RECEIVED DATA TO CLIENT CACHE
      if (jpipMessage != null) {

        while (!jpipMessage.header.isEOR) {

          // Statistic info
          bytesJPIPMessageHeader += jpipMessage.headerLength;
          bytesJPIPMessageBody += jpipMessage.header.msgLength;
          totalDownloadedBytes += jpipMessage.header.msgLength;
          updateStatistics(jpipMessage.headerLength + jpipMessage.header.msgLength, System.currentTimeMillis());
          
          cache.addJPIPMessage(jpipMessage);
          jpipMessage = null;

          try {
            jpipMessage = jpipMessageDecoder.readMessage();
          } catch (ProtocolException e) {
            e.printStackTrace();
            throw new WarningException("Data received from server can not be decoded.");
          } catch (IOException e) {
            e.printStackTrace();
            throw new WarningException("I/O error receiving data from server.");
          }
          //System.out.println(jpipMessage.toString()); // jpipMessage.list(System.out); // DEBUG
        }
      } else {
        throw new WarningException();
      }

      stopWatch.stop();

      /* System.out.println("----------------------\n"
       * + "Time stamp: "+System.currentTimeMillis()+"\n"
       * + "Title: "+userAgent+"\n"
       * //+ "Requested WOI: "+requestViewWindow.toString()+"\n"
       * + "Elapsed time: "+stopWatch.getTime()+"(ms)\n"
       * + "Data received: header="+bytesJPIPMessageHeader+" body="+bytesJPIPMessageBody+" (bytes)\n"
       * +"----------------------\n"
       * ); */
    }
  }

  /**
   * Returns the total bytes downloaded for the actual logical target.
   *
   * @return the total bytes downloaded.
   */
  public long getDownloadedBytes() {
    return totalDownloadedBytes;
  }

  /**
   * Sets the JPIP message counter to 0.
   */
  public void resetJPIPMessagesCounters() {
    bytesJPIPMessageHeader = 0;
    bytesJPIPMessageBody = 0;
  }

  /**
   * Returns the {@link #bytesJPIPMessageHeader} attribute.
   *
   * @return the {@link #bytesJPIPMessageHeader} attribute.
   */
  public long getBytesJPIPMessageHeader() {
    return bytesJPIPMessageHeader;
  }

  /**
   * Returns the {@link #bytesJPIPMessageBody} attribute.
   *
   * @return the {@link #bytesJPIPMessageBody} attribute.
   */
  public long getBytesJPIPMessageBody() {
    return bytesJPIPMessageBody;
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
   * Prints this HTTP Channel out to the specified output stream. This method
   * is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- HTTP Channel --");


    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * This method updates the {@link #downloadedBytes} and
   * {@link #downloadsTimes} attributes with new new values which are parsed
   * as input parameters.
   *
   * @param bytes the new value to be added in the {@link #downloadedBytes}
   * 			attribute.
   * @param time the new value to be added in the {@link #downloadsTimes}
   * 			attribute.
   */
  private void updateStatistics(long bytes, long time) {
    histBytes[iterator] = bytes;
    histTimes[iterator] = time;
    iterator = (iterator+1) % MAX_HIST_RECORDS;
  }
}
