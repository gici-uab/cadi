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
package CADI.Common.Session;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import CADI.Client.Session.ClientJPIPChannel;
import CADI.Common.Cache.CacheManagement;
import CADI.Common.Cache.DataBinsCacheManagement;
import CADI.Common.Log.CADILog;
import CADI.Common.Network.JPIP.JPIPRequestFields;
import CADI.Common.Network.JPIP.JPIPResponseFields;
import CADI.Common.Network.JPIP.ViewWindowField;
import GiciException.ErrorException;
import GiciException.WarningException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is used to save and manage data about a logical target
 * belonging to a client session.
 * <p>
 * Associated with each logical target are:
 * - A single image data return type
 * - A model of client's cache
 * - One or more JPIP channels
 * <p>
 * Usage example:<br>
 * &nbsp; constructor<br>
 * &nbsp; [setMethods]<br>
 * &nbsp; [newChannel]<br>
 * &nbsp; getMethdos<br>
 * <p>
 * 
 * Further information, please see see ISO/IEC 15444-1 section B.2
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.2 2011/03/07
 */
public class ClientSideSessionTarget extends SessionTarget {

  /**
   * Is an unique session identifier.
   */
  protected String sid = null;

  /**
   * Definition in {@link CADI.Common.Network.JPIP.TargetField#target}
   */
  protected String target = null;

  /**
   *
   */
  protected int[] subtarget = null;

  /**
   * Is the server name where logical targets are located.
   */
  protected String server = null;

  /**
   *
   */
  protected int port = 80;

  /**
   * Definition in {@link CADI.Common.Network.JPIP.ServerControlField#type}.
   * <p>
   * Allowed return types to receive data from the JPIP server. Values are saved
   * as a list of strings.
   * <p>
   * Allowed values: <code>jpp-stream</code>.
   */
  protected ArrayList<String> allowedReturnTypes = null;

  /**
   * Contains a view window including those parameters modified by the server.
   */
  protected ViewWindowField responseViewWindow = null;

  /**
   * Is the descriptor form type to be used in cache signaling.
   * <p>
   * Allowed values are:<br>
   * {@link  CADI.Common.Cache.DataBinsCacheManagement#NO_CACHE},
   * {@link  CADI.Common.Cache.DataBinsCacheManagement#EXPLICIT_FORM},
   * {@link  CADI.Common.Cache.DataBinsCacheManagement#IMPLICIT_FORM}
   */
  protected int descriptorType = -1;

  /**
   * Is a qualifier of the {@link #descriptorType}.
   * <p>
   * Allowed values are:<br>
   * {@link  CADI.Common.Cache.DataBinsCacheManagement#INDEX_RANGE},
   * {@link  CADI.Common.Cache.DataBinsCacheManagement#NUMBER_OF_BYTES},
   * {@link  CADI.Common.Cache.DataBinsCacheManagement#NUMBER_OF_LAYERS},
   * {@link  CADI.Common.Cache.DataBinsCacheManagement#WILDCARD}.
   */
  protected int descriptorQualifier = -1;

  /**
   * Is the size of the cache (in bytes).
   * <p>
   * A <code>0</code> value means no limit.
   */
  protected long maxCacheSize = 0;

  protected int managementPolicy;

  protected boolean useKeepAlive = true;

  protected String userAgent = null;
  protected String debug = null;

  protected long maxTargetLength = -1;

  /**
   * Indicates whether the cache is used between two consecutive requests. If
   * <code>true</code> cached data is keep, meanwhile if it is <code>false
   * </code> cached data are deleted.
   */
  protected boolean reuseCache = true;

  /**
   * Definition in {@link CADI.Common.Network.JPIP.ServerControlField#align}.
   */
  protected boolean align = false;

  /**
   * Definition in {@link CADI.Common.Network.JPIP.ServerControlField#wait}.
   */
  protected boolean wait = false;

  /**
   * Definition in {@link CADI.Common.Network.JPIP.ServerControlField#type}.
   */
  protected boolean extendedHeaders = false;

  /**
   *
   */
  protected ArrayList<String> preferredTransportProtocols = null;

  /**
   * Contains the channels which are asociated to this logical target.
   */
  protected Map<String, ClientJPIPChannel> channels = null;

  /**
   * Is used to log client messages.
   */
  protected CADILog log = null;
  
  /**
   * This attribute is used to store a historic of WOIs requested by clients.
   * <p>
   * Its usefulness is to be used by the prefetching module.
   */
  protected ArrayList<ViewWindowField> woiHistory = null;

  /**
   *
   */
  protected boolean recordWOIHistory = false;
  
  protected int MAX_HISTORY_RECORDS = 10;

  /**
   *
   */
  protected final ReentrantLock lock = new ReentrantLock();
  

  /**************************************************************************/
  /**                        PRINCIPAL METHODS                             **/
  /**************************************************************************/
  /**
   *
   * @param server
   * @param port
   * @param target
   * @param log
   */
  public ClientSideSessionTarget(String server, int port, String target, CADILog log) {
    this(server, port, target, null, log);
  }

  /**
   *
   * @param server
   * @param port
   * @param target
   * @param preferredTransportProtocols
   * @param log
   */
  public ClientSideSessionTarget(String server, int port, String target,
                                 ArrayList<String> preferredTransportProtocols,
                                 CADILog log) {

    super();

    if (server == null) {
      throw new NullPointerException("The \'server\' argument can not be null");
    }
    if (port <= 0) {
      throw new IllegalArgumentException("The \'port\' argument must be higher than 0");
    }
    if (target == null) {
      throw new NullPointerException();
    }
    if (preferredTransportProtocols != null) {
      for (String transport : preferredTransportProtocols) {
        if (!(transport.equals("none") || transport.equals("http"))) {
          throw new IllegalArgumentException("The transport protocol \""
                  + transport + "\" is not allowed");
        }
      }
    }
    if (log == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.server = server;
    this.port = port;
    this.target = target;
    this.log = log;

    // Creates a new channel
    channels = new HashMap<String, ClientJPIPChannel>();

    // Create session id
    sid = generateSID();

    allowedReturnTypes = new ArrayList<String>();
    allowedReturnTypes.add("jpp-stream");
    //allowedReturnTypes.add("jpp-stream;ptype=ext");
    //allowedReturnTypes.add("jpt-stream");
    //allowedReturnTypes.add("raw");

    if (preferredTransportProtocols != null) {
      this.preferredTransportProtocols = preferredTransportProtocols;
    } else {
      this.preferredTransportProtocols = new ArrayList<String>();
    }

    // Create WOI's history
    woiHistory = new ArrayList<ViewWindowField>();

  }

  /**
   * Returns the {@link #sid} attribute.
   *
   * @return
   */
  public final String getSessionID() {
    return sid;
  }

  /**
   *
   * @return
   */
  public final String getTarget() {
    return target;
  }

  /**
   *
   * @return
   */
  public final String getServer() {
    return server;
  }

  public final int getPort() {
    return port;
  }

  protected final void setPort(int port) {
    this.port = port;
  }
  
  /**
   * Sets the cache descriptor form type and qualifier.
   * 
   * @param type definition in {@link #descriptorType}.
   * @param qualifier definition in {@link #descritorQualifier}.
   */
  public final void setCacheDescriptor(int type, int qualifier) {
    
    // Check input parameters and compatibilities
    if (!((type == DataBinsCacheManagement.NO_CACHE) ||
            (type == DataBinsCacheManagement.EXPLICIT_FORM)||
            (type == DataBinsCacheManagement.IMPLICIT_FORM))) {
      throw new IllegalArgumentException();
    }
    
    if (!((qualifier == DataBinsCacheManagement.WILDCARD) ||
            (qualifier == DataBinsCacheManagement.INDEX_RANGE) ||
            (qualifier == DataBinsCacheManagement.NUMBER_OF_LAYERS) ||
            (qualifier == DataBinsCacheManagement.NUMBER_OF_BYTES))) {
      throw new IllegalArgumentException();
    }
    
    if ((type == DataBinsCacheManagement.EXPLICIT_FORM)
            && (qualifier == DataBinsCacheManagement.INDEX_RANGE)) {
      throw new IllegalArgumentException("Incompatible type and qualifier");
    }
    
    if ((type == DataBinsCacheManagement.IMPLICIT_FORM)
            && (qualifier == DataBinsCacheManagement.NUMBER_OF_BYTES)) {
      throw new IllegalArgumentException("Incompatible type and qualifier");
    }

    // Copy parameters
    this.descriptorType = type;
    this.descriptorQualifier = qualifier;
  }

  /**
   * OBS: This method must be replaced by the {@link #setCacheDescriptor(int, int)}.
   * 
   * @param descriptorType 
   */
  public final void setCacheDescriptorType(int descriptorType) {
    this.descriptorType = descriptorType;
  }

  /**
   * Return the {@link #descriptorType} attribute.
   * 
   * @return the {@link #descriptorQualifier} attribute.
   */
  public final int getCacheDescriptorType() {
    return descriptorType;
  }
  
  /**
   * Returns the {@link #descriptorQualifier} attribute.
   * 
   * @return the {@link #descriptorQualifier} attribute.
   */
  public final int getCacheDescriptorQualifier() {
    return descriptorQualifier;
  }

  /**
   * OBS: This method must be replaced by the {@link #setCacheDescriptor(int, int)}.
   * 
   * @param useWildcard 
   */
  public final void setUseWildcard(boolean useWildcard) {
    this.descriptorQualifier = DataBinsCacheManagement.WILDCARD;
  }

  public final boolean useWildcard() {
    return (descriptorQualifier == DataBinsCacheManagement.WILDCARD ? true : false);
  }

  /**
   * OBS: This method must be replaced by the {@link #setCacheDescriptor(int, int)}.
   * 
   * @param useIndexRange 
   */
  public final void setUseIndexRange(boolean useIndexRange) {
    this.descriptorQualifier = DataBinsCacheManagement.INDEX_RANGE;
  }

  public final boolean useIndexRange() {
    return (descriptorQualifier == DataBinsCacheManagement.INDEX_RANGE ? true : false);
  }

  /**
   * OBS: This method must be replaced by the {@link #setCacheDescriptor(int, int)}.
   * 
   * @param useNumberOfLayers 
   */
  public final void setUseNumberOfLayers(boolean useNumberOfLayers) {
    this.descriptorQualifier = DataBinsCacheManagement.NUMBER_OF_LAYERS;
  }

  public final boolean useNumberOfLayers() {
    return (descriptorQualifier == DataBinsCacheManagement.NUMBER_OF_LAYERS ? true : false);
  }

  /**
   * OBS: This method must be replaced by the {@link #setCacheDescriptor(int, int)}.
   * 
   * @param useNumberOfBytes 
   */
  public final void setUseNumberOfBytes(boolean useNumberOfBytes) {
    this.descriptorQualifier = DataBinsCacheManagement.NUMBER_OF_BYTES;
  }

  public final boolean useNumberOfBytes() {
    return (descriptorQualifier == DataBinsCacheManagement.NUMBER_OF_BYTES ? true : false);
  }

  public final void setMaxTargetLength(long maxTargetLength) {
    this.maxTargetLength = maxTargetLength;
  }

  public final void setUseKeepAlive(boolean useKeepAlive) {
    this.useKeepAlive = useKeepAlive;
  }

  /**
   * Sets if the client cache must be used. If client cache mustn't be used,
   * every time a new target is requested all previously saved data will be
   * discarded.
   *
   * @param reuseCache <code>true</code> if the cache is reseted every time a
   * 			new target is requested.
   */
  public final void reuseCache(boolean reuseCache) {
    this.reuseCache = reuseCache;
  }

  /**
   * Sets the {@link #align} attribute.
   *
   * @param align definition in {@link #align}.
   */
  public final void setAlign(boolean align) {
    this.align = align;
  }

  /**
   * Sets the {@link #wait} attribute.
   *
   * @param wait definition in {@link #wait}.
   */
  public final void setWait(boolean wait) {
    this.wait = wait;
  }

  /**
   * Sets the return types allowed by the JPIP client to receive data from the
   * JPIP server.
   *
   * @param returnTypes
   * @param extendedHeaders
   */
  public final void setAllowedReturnTypes(ArrayList<String> returnTypes, boolean extendedHeaders) {
    if (returnTypes == null) {
      throw new NullPointerException();
    }
    this.allowedReturnTypes = returnTypes;
    this.extendedHeaders = extendedHeaders;
  }

  /**
   *
   * @param useHTTPSession
   */
  public final void setUseHTTPSession(boolean useHTTPSession) {
    int index = preferredTransportProtocols.indexOf("http");
    if (useHTTPSession && (index < 0)) {
      preferredTransportProtocols.add("http");
    } else if (!useHTTPSession && (index >= 0)) {
      preferredTransportProtocols.remove(index);
    }
  }

  /**
   *
   * @param useHTTPTCPSession
   */
  public final void setUseHTTPTCPSession(boolean useHTTPTCPSession) {
    int index = preferredTransportProtocols.indexOf("http-tcp");
    if (useHTTPTCPSession && (index < 0)) {
      preferredTransportProtocols.add("http-tcp");
    } else if (!useHTTPTCPSession && (index >= 0)) {
      preferredTransportProtocols.remove(index);
    }
  }

  /**
   * 
   * @param userAgent
   */
  public final void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  /**
   *
   * @param userAgent
   */
  public final void setDebug(String debug) {
    this.debug = debug;
  }

  /**
   * Closes the session with the server. Note that data of all logical
   * targets belonging to the current session are also removed, as well
   * the session with the remote server is closed too.
   *
   * @return <code>true</code> if the session has been closed without
   * 			any problem. Otherwise, returns <code>false</code>.
   * @throws ErrorException
   *
   * @throws ErrorException it an error occurs while the session is being
   * 			closed.
   */
  public void closeSession() throws ErrorException {
    
    JPIPRequestFields jpipRequestFields = new JPIPRequestFields();
    jpipRequestFields.channelField.cclose = new ArrayList<String>();
    //jpipRequestFields.channelField.cclose.add("*");
    for (Map.Entry<String, ClientJPIPChannel> entry : channels.entrySet()) {
      if (entry.getValue().getCID() != null) {
        jpipRequestFields.channelField.cclose.add(entry.getValue().getCID());
      }
    }

    if (jpipRequestFields.channelField.cclose.size() == 0) {
      return; // There is no session still opened
    }
        
    // Set the cid from the first cid to be closed
    jpipRequestFields.channelField.cid = jpipRequestFields.channelField.cclose.get(0);
    jpipRequestFields.channelField.cclose.remove(0);
    if (jpipRequestFields.channelField.cclose.isEmpty()) {
      jpipRequestFields.channelField.cclose.add("*");
    }

    try {
      channels.get(getLocalCID()).getRequest(jpipRequestFields);
      copyJPIPResponseFields(getJPIPResponseFields());
    } catch (WarningException e) {
      e.printStackTrace();
      throw new ErrorException(e.getMessage());
      //throw new ErrorException("It is impossible to send the request to the server \"" + getServer() + "\"");
    }
    
    // Close channel
    channels.get(getLocalCID()).close();
  }

  /**
   * Removes all the attributes.
   */
  @Override
  public void remove() {
    super.remove();
    for (Map.Entry<String, ClientJPIPChannel> entry : channels.entrySet()) {
      entry.getValue().close();
    }
    channels.clear();
  }

  /**
   * Returns the number of opened channels.
   *
   * @return the number of opened channels.
   */
  @Override
  public final int numOfChannels() {
    return channels.size();
  }

  /**
   * Checks if the channel <code>cid</code> belongs to this session target.
   *
   * @param cid the unique channel identifier.
   * @return <code>true</code> if the channel belongs to this session target.
   * 			Otherwise, returns <code>false</code>.
   */
  @Override
  public final boolean contains(String cid) {
    return channels.containsKey(cid);
  }

  /**
   * Closes a channel.
   *
   * @param cid the unique channel identifier.
   */
  @Override
  public final void closeChannel(String cid) {
    channels.remove(cid);
  }

  /**
   *
   * @return
   */
  public String newChannel(CacheManagement cache) {
    return newChannel("http", cache);
  }

  /**
   *
   * @param transport
   * @return
   */
  public String newChannel(String transport, CacheManagement cache) {
    ArrayList<String> preferredTransportProtocols = new ArrayList<String>();
    preferredTransportProtocols.add(transport);
    return newChannel(preferredTransportProtocols, cache);
  }

  /**
   *
   * @param preferredTransportProtocols
   * @return
   */
  public String newChannel(ArrayList<String> preferredTransportProtocols,
                           CacheManagement cache) {

    ClientJPIPChannel channel = new ClientJPIPChannel(preferredTransportProtocols, server, port, cache, log);
    channels.put(channel.getLocalCID(), channel);

    return channel.getLocalCID();
  }

  /**
   *
   * @param preferredTransportProtocols
   * @return
   */
  public String newChannel(ArrayList<String> preferredTransportProtocols,
                           CacheManagement cache,
                           String proxyServer, int proxyPort) {

    ClientJPIPChannel channel = new ClientJPIPChannel(preferredTransportProtocols, server, port, proxyServer, proxyPort, cache, log);
    channels.put(channel.getLocalCID(), channel);

    return channel.getLocalCID();
  }

  /**
   *
   * @return
   */
  public final String getRequestedURI() {
    return channels.get(getLocalCID()).getRequestedURI();
  }

  /**
   * Sets the JPIP message counter to 0.
   */
  public final void resetJPIPMessagesCounters() {
    if (!channels.isEmpty()) {
      channels.get(getLocalCID()).resetJPIPMessagesCounters();
    }
  }

  /**
   * Returns the {@link #bytesJPIPMessageHeader} attribute.
   *
   * @return the {@link #bytesJPIPMessageHeader} attribute.
   */
  public final long getBytesJPIPMessageHeader() {
    if (!channels.isEmpty()) {
      return channels.get(getLocalCID()).getBytesJPIPMessageHeader();
    }
    return -1;
  }

  /**
   * Returns the {@link #bytesJPIPMessageBody} attribute.
   *
   * @return the {@link #bytesJPIPMessageBody} attribute.
   */
  public final long getBytesJPIPMessageBody() {
    if (!channels.isEmpty()) {
      return channels.get(getLocalCID()).getBytesJPIPMessageBody();
    }
    return -1;
  }

  /**
   * Returns the total bytes downloaded for the actual logical target.
   *
   * @return the total bytes downloaded.
   */
  public final long getDownloadedBytes() {
    if (!channels.isEmpty()) {
      if (channels.containsKey(getLocalCID())) {
        return channels.get(getLocalCID()).getDownloadedBytes();
      }
    }
    return -1;
  }

  /**
   * Returns a CID beloging to the session target.
   *
   * @return
   */
  public final String getLocalCID() {
    if (channels.isEmpty()) {
      return null;
    }

    Iterator<String> it = getLocalCIDs().iterator();
    return it.next();
  }

  /**
   * Returns an one-dimensional array containing the available CIDs.
   *
   * @return
   */
  public final Set<String> getLocalCIDs() {
    if (channels.isEmpty()) {
      return null;
    }

    return channels.keySet();
  }

  /**
  *
  * @param recordWOIHistoric
  */
 public void setRecordWOIHistory(boolean recordWOIHistory) {
   this.recordWOIHistory = recordWOIHistory;

   if (recordWOIHistory) { // Create WOI history
     woiHistory = new ArrayList<ViewWindowField>();
   }
 }

 /**
  *
  * @param woi
  */
 public void addWOIHistory(ViewWindowField woi) {
   if (woiHistory.size() >= MAX_HISTORY_RECORDS) {
     woiHistory.remove(0);
   }
   woiHistory.add(woi);
 }
 
 /**
  * Returns the {@link #woisHistory} attribute.
  *
  * @return definition in {@link #woisHistory}.
  */
 public ArrayList<ViewWindowField> getWOIHistory() {
   return new ArrayList<ViewWindowField>(woiHistory);
 }
  
  /**
   *
   * @return
   */
  protected JPIPResponseFields getJPIPResponseFields() {
    return channels.get(getLocalCID()).getJPIPResponseFields();
  }

  protected void copyJPIPResponseFields(JPIPResponseFields jpipResponseFields) {

    // Update client parameters with the JPIP server response fields
    if (jpipResponseFields.fsiz[0] != -1) {
      responseViewWindow.fsiz[0] = jpipResponseFields.fsiz[0];
      responseViewWindow.fsiz[1] = jpipResponseFields.fsiz[1];
    }
    if (jpipResponseFields.rsiz[0] != -1) {
      responseViewWindow.rsiz[0] = jpipResponseFields.rsiz[0];
      responseViewWindow.rsiz[1] = jpipResponseFields.rsiz[1];
    }
    if (jpipResponseFields.roff[0] != -1) {
      responseViewWindow.roff[0] = jpipResponseFields.roff[0];
      responseViewWindow.roff[1] = jpipResponseFields.roff[1];
    }
    if (jpipResponseFields.comps != null) {
      responseViewWindow.comps = new int[jpipResponseFields.comps.length][2];
      for (int i = 0; i < jpipResponseFields.comps.length; i++) {
        responseViewWindow.comps[i][0] = jpipResponseFields.comps[i][0];
        responseViewWindow.comps[i][1] = jpipResponseFields.comps[i][1];
      }
    }
    if (jpipResponseFields.layers != -1) {
      responseViewWindow.layers = jpipResponseFields.layers;
    }
    /*if (jpipResponseFields.len != -1) {
    len = jpipResponseFields.len;
    }
    if (jpipResponseFields.type != -1) {
    type = jpipResponseFields.type;
    }*/
    if (jpipResponseFields.tid != null) {
      setTID(jpipResponseFields.tid);
    }
    if (jpipResponseFields.cid != null) {
      channels.get(getLocalCID()).setCID(jpipResponseFields.cid);
    }

    if (jpipResponseFields.port != -1) {
      setPort(jpipResponseFields.port);
    }
  }

   /**
   *
   * @param inClassIdentifier
   * @param layer
   * @return
   */
  public boolean isAvailable(long inClassIdentifier, int layer) {
    throw new UnsupportedOperationException("This method must be implemented by inherited classes!");
  }
  
  /**
   * 
   * @param viewWindow
   * @return 
   */
  public boolean isAvailable(ViewWindowField viewWindow) {
    throw new UnsupportedOperationException("This method must be implemented by inherited classes!");
  }

  /**
   * Fetch the compressed data belonging to the <data>requestViewWindow</data>
   * from the server.
   * <p>
   * Data are fetch but not decoded.
   * <p>
   * @see {@link #getWindow(CADI.Common.Network.JPIP.ViewWindowField, boolean)}.
   *
   * @param requestViewWindow the target Window Of Interest to fetch.
   * @param recordWOI
   *
   * @return
   * @throws ErrorException if an error occurs fetching the WOI.
   */
  public void fetchWindow(ViewWindowField requestViewWindow, boolean recordWOI)
          throws ErrorException {
     throw new UnsupportedOperationException("THIS METHOD HAS NOT BEEN IMPLEMENTED YET!");
  }
  
  /**
   * Gets the original image's window of interest defined by
   * <code>requestViewWindow</code>.
   * 
   * @param requestViewWindow the target Window Of Interest to fetch.
   * @param recordWOI
   *
   * @return
   * @throws ErrorException if an error occurs fetching the WOI.
   */
  /*public void getWindow(ViewWindowField requestViewWindow, boolean recordWOI)
          throws ErrorException {
     throw new UnsupportedOperationException("THIS METHOD HAS NOT BEEN IMPLEMENTED YET!");
  }*/

  public final void lock() {
    lock.lock();
  }

  public final void unlock() {
    lock.unlock();
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

    str += "sid=" + sid;
    str += "tid=" + tid;
    str += "target=" + target;
    //str += "subtarget="+subtarget;
    str += "server=" + server;
    str += "port=" + port;
    if (allowedReturnTypes != null) {
      str += "allowed return types=";
      for (String type : allowedReturnTypes) {
        str += " " + type;
      }
      str += ", ";
    }
    str += "Return type=" + returnType;
    for (Map.Entry<String, ClientJPIPChannel> entry : channels.entrySet()) {
      str += entry.getValue().toString();
    }

    str += "]";

    return str;
  }

  /**
   * Prints this Session out to the specified output stream. This method
   * is useful for debugging.
   *
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Client Side Session Target --");
    super.list(out);

    out.println("sid: " + sid);
    out.println("tid: " + tid);
    out.println("Target: " + target);
    //out.println("subtarget: "+subtarget);
    out.println("Server: " + server);
    out.println("Port: " + port);
    if (allowedReturnTypes != null) {
      out.print("Allowed return types:");
      for (String type : allowedReturnTypes) {
        out.print(" " + type);
      }
      out.println();
    }
    out.println("Return type: " + returnType);

    for (Map.Entry<String, ClientJPIPChannel> entry : channels.entrySet()) {
      entry.getValue().list(out);
    }

    out.flush();
  }

  /**************************************************************************/
  /**                       AUXILIARY METHODS                              **/
  /**************************************************************************/
  /**
   * Generates a unique identifier. The unique identifier generation
   * is base on the current time, it is a time-stamp.
   * <p>
   * NOTICE: this method must be improved adding the Rijndael (or another
   * one), client information, etc. to improve the security.
   *
   * @return the unique channel identifier.
   */
  protected String generateUID() {
    return UUID.randomUUID().toString();
  }

  /**
   * Generates a unique channel identifier. The unique identifier generation
   * is base on the current time, it is a time-stamp.
   * <p>
   * NOTICE: this method must be improved adding the Rijndael (or another
   * one), client information, etc. to improve the security.
   *
   * @return the unique channel identifier.
   */
  private String generateSID() {
    String id = "sid";

    // Adds the date
    Date today = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
    id += "_" + formatter.format(today);

    // Adds the milliseconds
    id += "_" + System.currentTimeMillis();

    return id;
  }
}
