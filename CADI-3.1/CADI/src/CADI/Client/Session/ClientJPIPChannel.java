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
package CADI.Client.Session;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.UUID;

import CADI.Client.Network.HTTPChannel;
import CADI.Client.Network.JPIPRequestEncoder;
import CADI.Common.Cache.CacheManagement;
import CADI.Common.Log.CADILog;
import CADI.Common.Network.JPIP.JPIPRequestFields;
import CADI.Common.Network.JPIP.JPIPResponseFields;
import CADI.Common.Session.JPIPChannel;
import GiciException.WarningException;

/**
 * This class is used to save the channel properties.
 * <p>
 * For further information about JPIP channels, 
 * see ISO/IEC 15444-9 section B.2
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.2 2010/01/25
 */
public class ClientJPIPChannel extends JPIPChannel {

  /**
   * It is a local channel identifier. It is better to use this channel
   * identifier instead of the {@link CADI.Common.Session.JPIPChannel#cid}
   * because the value of the latter one is set by the server, and it could
   * not be set. However, the {@link #localCID} is set by the constructor of
   * the object.
   */
  private String localCID = null;

  /**
   * Definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#host}.
   */
  private String host = null;

  /**
   * Definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#path}.
   */
  private String path = null;

  /**
   * Definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#auxport}.
   */
  private int auxport = -1;

  /**
   *
   */
  private HTTPChannel httpChannel = null;

  /**
   *
   */
  private ArrayList<String> preferredTransportProtocols = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param transport
   * @param cache
   * @param log
   */
  public ClientJPIPChannel(ArrayList<String> preferredTransportProtocols,
                           CacheManagement cache, CADILog log) {
    this(preferredTransportProtocols, "localhost", 80, cache, log);
  }

  /**
   * Constructor.
   *
   * @param transport
   * @param server
   * @param port
   * @param cache
   * @param log
   */
  public ClientJPIPChannel(ArrayList<String> preferredTransportProtocols,
                           String server, int port, CacheManagement cache, CADILog log) {
    this(preferredTransportProtocols, server, port, null, -1, cache, log);
  }

  /**
   * Constructor.
   *
   * @param transport
   * @param server
   * @param port
   * @param proxyServer
   * @param proxyPort
   * @param cache
   * @param log
   */
  public ClientJPIPChannel(ArrayList<String> preferredTransportProtocols,
                           String server, int port, String proxyServer, int proxyPort,
                           CacheManagement cache, CADILog log) {

    // Check input parameters
    if (server == null) throw new NullPointerException();
    if (port <= 0) throw new IllegalArgumentException();
    if (cache == null) throw new NullPointerException();
    if (log == null) throw new NullPointerException();

    this.preferredTransportProtocols = preferredTransportProtocols;

    localCID = generateCID();

    //ArrayList<String>accept = new ArrayList<String>();
    //accept.add("image/jpp-stream");

    httpChannel = new HTTPChannel(server, port, proxyServer, proxyPort, cache, log);
    //httpChannel.setUserAgent("CADI Client");
    //httpChannel.setKeepAlive(true);
    //httpChannel.setAccept(accept);
  }

  /**
   *
   */
  public void close() {
    httpChannel.close();
  }

  /**
   *
   * @param cid
   */
  public void setCID(String cid) {
    this.cid = cid;
  }

  /**
   * Returns the {@link #cid} attribute.
   *
   * @return the {@link #cid} attribute.
   */
  public String getCID() {
    return cid;
  }

  /**
   * Returns the {@link #localCID} attribute.
   *
   * @return the {@link #localCID} attribute.
   */
  public String getLocalCID() {
    return localCID;
  }

  /**
   * Returns the {@link #csf} attribute.
   *
   * @return the {@link #csf} attribute.
   */
  public String getCSF() {
    return csf;
  }

  /**
   * Sets the {@link #csf} attribute.
   *
   * @param csf the {@link #csf} to be set.
   */
  public void setCSF(String csf) {
    this.csf = csf;
  }

  /**
   *
   * @param keepAlive
   */
  public void setKeepAlive(boolean keepAlive) {
    httpChannel.setKeepAlive(keepAlive);
  }

  /**
   * Returns whether the HTTP keep alive feature is or not set.
   *
   * @return whether the HTTP keep alive feature is or not set.
   */
  public boolean isKeepAlive() {
    return httpChannel.isKeepAlive();
  }

  /**
   *
   * @param accept
   */
  public void setAccept(ArrayList<String> accept) {
    httpChannel.setAccept(accept);
  }

  /**
   *
   * @param userAgent
   */
  public void setUserAgent(String userAgent) {
    httpChannel.setUserAgent(userAgent);
  }

  /**
   *
   * @param userAgent
   */
  public void setDebug(String debug) {
    httpChannel.setDebug(debug);
  }

  /**
   *
   * @param userAgent
   */
  public void setVia(String via) {
    httpChannel.setVia(via);
  }

  /**
   *
   * @return
   */
  public String getRequestedURI() {
    return httpChannel.getRequestedURI();
  }

  /**
   *
   * @param requestURI
   * @throws WarningException
   */
  public void getRequest(String requestURI) throws WarningException {
    httpChannel.getRequest(requestURI);
  }

  /**
   *
   * @param jpipRequestFields
   * @throws WarningException
   */
  public void getRequest(JPIPRequestFields jpipRequestFields) throws WarningException {
    // Check input parameters
    if (jpipRequestFields == null) throw new NullPointerException();

    // If a cid is available, it is used
    // because a session has been established
    if (cid != null) jpipRequestFields.channelField.cid = cid;

    if (path != null) jpipRequestFields.channelField.path = path;

    // Check if a new channel is requested
    if ((preferredTransportProtocols != null) && (preferredTransportProtocols.size() > 0)) {
      jpipRequestFields.channelField.cnew = new ArrayList<String>();
      for (String transp : preferredTransportProtocols) {
        jpipRequestFields.channelField.cnew.add(transp);
      }
    }

    // Encode JPIP parameters
    String jpipRequestLine = JPIPRequestEncoder.encodeJPIPRequest(jpipRequestFields);

    getRequest(jpipRequestLine);

    // Copy JPIP Response parameters
    JPIPResponseFields jpipResponseFields = httpChannel.getJPIPResponseFields();

    if (jpipResponseFields.cid != null) cid = jpipResponseFields.cid;
    if (jpipResponseFields.transport > 0) {
      if (jpipResponseFields.transport == JPIPResponseFields.TRANSPORT_HTTP)
        transport = "http";
      else if (jpipResponseFields.transport == JPIPResponseFields.TRANSPORT_HTTP)
        transport = "http-tcp";
      else
        assert (true);
    }
    if (jpipResponseFields.host != null) host = jpipResponseFields.host;
    if (jpipResponseFields.path != null) path = jpipResponseFields.path;
    if (jpipResponseFields.auxport > 0) auxport = jpipResponseFields.auxport;

    // If a new channel has been set, flag of new preferred transports is removed
    if (jpipResponseFields.transport > 0) preferredTransportProtocols = null;
  }

  /**
   *
   * @return
   */
  public JPIPResponseFields getJPIPResponseFields() {
    return httpChannel.getJPIPResponseFields();
  }

  /**
   * Returns the total bytes downloaded for the actual logical target.
   *
   * @return the total bytes downloaded.
   */
  public long getDownloadedBytes() {
    return httpChannel.getDownloadedBytes();
  }

  /**
   * Sets the JPIP message counter to 0.
   */
  public void resetJPIPMessagesCounters() {
    httpChannel.resetJPIPMessagesCounters();
  }

  /**
   * Returns the {@link #bytesJPIPMessageHeader} attribute.
   *
   * @return the {@link #bytesJPIPMessageHeader} attribute.
   */
  public long getBytesJPIPMessageHeader() {
    return httpChannel.getBytesJPIPMessageHeader();
  }

  /**
   * Returns the {@link #bytesJPIPMessageBody} attribute.
   *
   * @return the {@link #bytesJPIPMessageBody} attribute.
   */
  public long getBytesJPIPMessageBody() {
    return httpChannel.getBytesJPIPMessageBody();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str += getClass().getName() + " [";
    str += super.toString();
    str += "local ID=" + cid;
    str += ", Host=" + host;
    str += ", Path=" + path;
    str += ", Auxport=" + auxport;
    if (preferredTransportProtocols != null) {
      str += ", Preferred Transports=";
      for (String transport : preferredTransportProtocols) {
        str += transport + " ";
      }
    }
    if (httpChannel != null) str += httpChannel.toString();
    str += "]";

    return str;
  }

  /**
   * Prints this Channel out to the specified output stream. This method
   * is useful for debugging.
   *
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Channel --");

    super.list(out);
    out.println("Local ID: " + localCID);
    out.println("Host: " + host);
    out.println("Path: " + path);
    out.println("Auxport: " + auxport);
    if (preferredTransportProtocols != null) {
      out.print("Preferred Transports: ");
      for (String transport : preferredTransportProtocols) {
        out.print(transport + " ");
      }
      out.println();
    }
    if (httpChannel != null) httpChannel.list(out);

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * Generates a unique channel identifier. The unique identifier generation
   * is base on the current time, it is a time-stamp.
   * <p>
   * NOTICE: this method could be improved adding the Rijndael (or another
   * one), client information, etc. to improve the security.
   *
   * @return the unique channel identifier.
   */
  protected String generateCID() {
    return UUID.randomUUID().toString();
  }

}
