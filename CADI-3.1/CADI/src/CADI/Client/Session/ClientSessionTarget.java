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
import java.util.Map;

import CADI.Client.ImageData;
import CADI.Client.Cache.ClientCacheManagement;
import CADI.Client.ClientLogicalTarget.JPEG2000.ClientJPEG2KCodestream;
import CADI.Client.ClientLogicalTarget.JPEG2000.JP2KClientLogicalTarget;
import CADI.Common.Cache.ModelElement;
import CADI.Common.Log.CADILog;
import CADI.Common.Network.JPIP.JPIPRequestFields;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Common.Session.ClientSideSessionTarget;
import CADI.Common.Util.ArraysUtil;
import CADI.Common.Util.CADIDimension;
import GiciException.ErrorException;
import GiciException.WarningException;

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
 * @version 1.0.6 2011/12/03
 */
public class ClientSessionTarget extends ClientSideSessionTarget {

  /**
   * Is the proxy server used to connect to the server.
   */
  private String proxyServer = null;

  /**
   * Is the port of the proxy used to connect to the server.
   */
  private int proxyPort = -1;

  /**
   * This object is the container where the decoder saves the recovered image
   * and it will read by the client application (usually, a graphical user
   * interface).
   */
  private ImageData imageData = null;

  /**
   * Definition in {@link  CADI.Client.ClientLogicalTarget.JPEG2000.JPEG2KDecoder#numThreads}.
   */
  private int numThreads = -1;

  // INTERNAL ATTRIBUTES
  /**
   * Saves the server cache model of this logical target for all channels.
   */
  private ClientCacheManagement cache = null;

  /**
   * It is the JPEG2000 decoder.
   */
  private JP2KClientLogicalTarget jp2kClientLogicalTarget = null;

  // Partial received data
  /**
   * An one-dimensional array which contains a log of the last amount of
   * downloaded data. It is used to estimate the downloaded rate.
   * <p>
   * This attribute is related to {@link #downloadsTimes} attribute.
   */
  private long[] downloadedBytes = new long[100];

  /**
   * An one-dimensional array which contains the time when the saved data
   * at {@link #downloadedBytes} were downloaded. Both attributes are used
   * to estimate the download read.
   */
  private long[] downloadsTimes = new long[100];

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param server
   * @param port
   * @param proxyServer
   * @param proxyPort
   * @param target
   * @param imageData
   * @param log
   */
  public ClientSessionTarget(String server, int port,
                             String proxyServer, int proxyPort,
                             String target, ImageData imageData,
                             CADILog log) {
    this(server, port, proxyServer, proxyPort, target, imageData, null, log);
  }

  /**
   * Constructor.
   *
   * @param server
   * @param port
   * @param proxyServer
   * @param proxyPort
   * @param target
   * @param imageData
   * @param preferredTransportProtocols
   * @param log
   */
  public ClientSessionTarget(String server, int port,
                             String proxyServer, int proxyPort,
                             String target, ImageData imageData,
                             ArrayList<String> preferredTransportProtocols,
                             CADILog log) {

    this(server, port, proxyServer, proxyPort, target, imageData,
            preferredTransportProtocols, log, -1);
  }

  /**
   * Constructor.
   * 
   * @param server
   * @param port
   * @param proxyServer
   * @param proxyPort
   * @param target
   * @param imageData
   * @param preferredTransportProtocols
   * @param log
   * @param numThreads 
   */
  public ClientSessionTarget(String server, int port,
                             String proxyServer, int proxyPort,
                             String target, ImageData imageData,
                             ArrayList<String> preferredTransportProtocols,
                             CADILog log, int numThreads) {

    super(server, port, target, preferredTransportProtocols, log);

    this.proxyServer = proxyServer;
    this.proxyPort = proxyPort;
    this.imageData = imageData;
    this.numThreads = numThreads;

    // Creates the cache
    cache = new ClientCacheManagement();

    // Create jpeg2000 decoder engine
    jp2kClientLogicalTarget =
            new JP2KClientLogicalTarget(imageData, cache, log, numThreads);

    // Initializations for speed estimation
    for (int i = 0; i < downloadedBytes.length; i++) {
      downloadedBytes[i] = 0L;
      downloadsTimes[i] = 0L;
    }

    userAgent = "CADI Client";
  }

  /**
   *
   * @return
   */
  @Override
  public String newChannel() {
    return super.newChannel(cache);
  }

  /**
   *
   * @param transport
   * @return
   */
  @Override
  public String newChannel(String transport) {
    return super.newChannel(transport, cache);
  }

  /**
   *
   * @param preferredTransportProtocols
   * @return
   */
  public String newChannel(ArrayList<String> preferredTransportProtocols) {
    return super.newChannel(preferredTransportProtocols, cache, proxyServer, proxyPort);
  }

  /**
   *
   * @return
   */
  public JP2KClientLogicalTarget getLogicalTarget() {
    return jp2kClientLogicalTarget;
  }

  /**
   * Definition in {@link CADI.Common.Cache.CacheManagement#getManagementPolicy()}.
   *
   * @return definition in {@link CADI.Common.Cache.CacheManagement#managementPolicy}.
   */
  public int getManagementPolicy() {
    return cache != null ? cache.getManagementPolicy() : managementPolicy;
  }

  /**
   * Definition in {@link CADI.Common.Cache.CacheManagement#setManagementPolicy(int)}.
   *
   * @param managementPolicy definition in {@link CADI.Common.Cache.CacheManagement#managementPolicy}.
   */
  public void setManagementPolicy(int managementPolicy) {
    cache.setManagementPolicy(managementPolicy);
  }

  /**
   * Definition in {@link CADI.Common.Cache.CacheManagement#getMaxCacheSize()}.
   *
   * @return definition in {@link CADI.Common.Cache.CacheManagement#cacheSize}.
   */
  public long getMaxCacheSize() {
    return cache != null ? cache.getMaxCacheSize() : maxCacheSize;
  }

  /**
   * Definition in {@link CADI.Common.Cache.CacheManagement#setMaxCacheSize(long)}.
   *
   * @param maxCacheSize definition in {@link CADI.Common.Cache.CacheManagement#cacheSize}.
   */
  public void setMaxCacheSize(long maxCacheSize) {
    cache.setMaxCacheSize(maxCacheSize);
  }

  /**
   * Removes all the attributes.
   */
  @Override
  public void remove() {
    super.remove();
    cache.reset();
    cache = null;
    cache = null;
    jp2kClientLogicalTarget = null;
  }

  /**
   *
   * @param returnTypes
   */
  public void setAllowedReturnTypes(ArrayList<String> returnTypes) {
    setAllowedReturnTypes(returnTypes, false);
  }

  /**
   *
   * @param requestURI
   * @throws WarningException
   */
  public void getRequest(String requestURI) throws WarningException {

    String channelID = manageChannels();
    channels.get(channelID).getRequest(requestURI);


    responseViewWindow = new ViewWindowField();
    copyJPIPResponseFields(getJPIPResponseFields());
  }

  /**
   * This method fetches a logical target defined by the method's input
   * parameters.
   *
   * @param requestViewWindow the target Window Of Interest to fetch.
   *
   * @throws ErrorException if an error occurs fetching the WOI.
   */
  public void fetchWindow(ViewWindowField requestViewWindow)
          throws ErrorException {
    fetchWindow(requestViewWindow, recordWOIHistory);

  }

  /**
   * This method fetches a logical target defined by the method's input
   * parameters.
   *
   * @param viewWindow the target Window Of Interest to fetch.
   * @param recordWOI
   *
   * throws WarningException if an error occurs fetching the WOI or
   * 			received data cannot be decompressed
   */
  @Override
  public void fetchWindow(ViewWindowField requestViewWindow, boolean recordWOI)
          throws ErrorException {

    assert (requestViewWindow != null);

    lock.lock();

    // Add requested window to historic of WOI's
    if (recordWOI) {
      addWOIHistory(requestViewWindow);
    }

    // Check if image's main header has been downloaded
    ClientJPEG2KCodestream codestream = getLogicalTarget().getCodestream();
    if (codestream == null) {
      getMainHeader();
      codestream = getLogicalTarget().getCodestream();
    }

    // Copy the input parameter
    ViewWindowField viewWindowToSend = new ViewWindowField();
    ViewWindowField.deepCopy(requestViewWindow, viewWindowToSend);

    // Set frame size to image size if it has not been set
    if (viewWindowToSend.fsiz[0] < 0) {
      viewWindowToSend.fsiz[0] = codestream.getXSize();
      viewWindowToSend.fsiz[1] = codestream.getYSize();
      viewWindowToSend.roundDirection = ViewWindowField.ROUND_DOWN;
    }
    
    // Check components limit
    if (viewWindowToSend.comps != null) {
      if (viewWindowToSend.comps[viewWindowToSend.comps.length-1][1]
              >= codestream.getZSize()) {
        throw new IllegalArgumentException("Number of components greater than image components");
      }
    }

    // Check if multiple component transformation
    if (jp2kClientLogicalTarget.isMultiComponentTransform()) {
      viewWindowToSend.comps =
              jp2kClientLogicalTarget.getRelevantComponents(viewWindowToSend.comps);
    }

    if (log.isLog(CADILog.LEVEL_INFO)) {
      log.logInfo("\nNEW VIEW WINDOW\n" + requestViewWindow.toString());
    }

    // Clear cache?
    if (!reuseCache) {
      cache.reset();
    }

    //System.out.println("\n=== ANTES DE ENVIAR REQUEST ===");
    //cache.list(System.out);

    // Check view window in cache.
    if (!cache.isInCache(viewWindowToSend)) {
      if (log.isLog(CADILog.LEVEL_INFO)) {
        log.logInfo("   - Data is not available in client cache"); // DEBUG
      }

      JPIPRequestFields jpipRequestFields = new JPIPRequestFields();


      // SET JPIP PARAMETERS

      // Target fields
      String channelID = manageChannels();
      if (channels.get(channelID).getCID() == null) {
        // Session is not established
        if (getTID() == null) { // Server does not give a target identifier
          assert (getTarget() != null);
          jpipRequestFields.targetField.target = getTarget();
        } else { // Server gave a target identifier
          jpipRequestFields.targetField.target = getTarget();
          jpipRequestFields.targetField.tid = getTID();
        }
      } else {
        // Session is established, server gave a channel identifier
        assert (!useWildcard());
        assert (!useIndexRange());
        jpipRequestFields.channelField.path = null;
        //jpipRequestFields.channelField.cid = channels.get(getLocalCID()).getCID();
        jpipRequestFields.channelField.cid = channels.get(channelID).getCID();
      }

      // View window fields
      if (viewWindowToSend.fsiz != null) {
        jpipRequestFields.viewWindowField.fsiz = viewWindowToSend.fsiz;
      }
      if (viewWindowToSend.roundDirection != ViewWindowField.ROUND_DOWN) {
        jpipRequestFields.viewWindowField.roundDirection = viewWindowToSend.roundDirection;
      }
      if (viewWindowToSend.roff != null) {
        jpipRequestFields.viewWindowField.roff = viewWindowToSend.roff;
      }
      if (viewWindowToSend.rsiz != null) {
        jpipRequestFields.viewWindowField.rsiz = viewWindowToSend.rsiz;
      }
      if (viewWindowToSend.comps != null) {
        jpipRequestFields.viewWindowField.comps = viewWindowToSend.comps;
      }
      if (viewWindowToSend.layers != -1) {
        jpipRequestFields.viewWindowField.layers = viewWindowToSend.layers;
      }

      // Metadata fields

      // Data limit field
      if (maxTargetLength > 0) {
        jpipRequestFields.dataLimitField.len = (int) maxTargetLength;
      }

      // Server control fields
      jpipRequestFields.serverControlField.align = align;
      jpipRequestFields.serverControlField.wait = wait;

      if (returnType != null) {
        jpipRequestFields.serverControlField.type.add(returnType);
      } else {
        for (String type : allowedReturnTypes) {
          if (!extendedHeaders) {
            jpipRequestFields.serverControlField.type.add(type);
          } else if (type.equals("jpp-stream")) {
            jpipRequestFields.serverControlField.type.add(type + ";ptype=ext");
          } else if (type.equals("jpt-stream")) {
            jpipRequestFields.serverControlField.type.add(type + ";ttype=ext");
          } else {
            jpipRequestFields.serverControlField.type.add(type);
          }
        }
      }


      // Cache management fields
      if (channels.get(channelID).getCID() == null) {
        // Send cache status only in stateless

        // Get the cache descriptor
        int descriptorQualifier = -1;
        if (useWildcard()) {
          descriptorQualifier = ClientCacheManagement.WILDCARD;
        } else if (useIndexRange()) {
          descriptorQualifier = ClientCacheManagement.INDEX_RANGE;
        } else if (useNumberOfLayers()) {
          descriptorQualifier = ClientCacheManagement.NUMBER_OF_LAYERS;
        } else if (this.useNumberOfBytes()) {
          descriptorQualifier = ClientCacheManagement.NUMBER_OF_BYTES;
        } else {
          // do nothing
        }

        ArrayList<ModelElement> cacheDescriptor = null;
        int descriptorType = getCacheDescriptorType();
        if (descriptorType == ClientCacheManagement.EXPLICIT_FORM) {
          cacheDescriptor = cache.getCacheDescriptor(viewWindowToSend, ClientCacheManagement.EXPLICIT_FORM, descriptorQualifier);
        } else if (descriptorType == ClientCacheManagement.IMPLICIT_FORM) {
          cacheDescriptor = cache.getCacheDescriptor(viewWindowToSend, ClientCacheManagement.IMPLICIT_FORM, descriptorQualifier);
        } else { // No cache
          // do nothing
        }
        jpipRequestFields.cacheManagementField.model = cacheDescriptor;
      }
      
      responseViewWindow = new ViewWindowField();

      if (debug != null) {
        channels.get(getLocalCID()).setDebug(debug);
      }
      try {
        channels.get(getLocalCID()).getRequest(jpipRequestFields);
        copyJPIPResponseFields(getJPIPResponseFields());
      } catch (WarningException e) {
        throw new ErrorException(e.getMessage());
      }

      if (!channels.get(channelID).isKeepAlive()) {
        channels.get(channelID).close();
      }

    } else {
      if (log.isLog(CADILog.LEVEL_INFO)) {
        log.logInfo("   - Data is available in client cache");
      }
    }

    lock.unlock();

  }

  /**
   * This method fetches a logical target defined by the method's input
   * parameters.
   *
   * @param components is an one-dimension array with the component indexes.
   * @param discardLevels .
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   * @param maxTargetLength definition in {@link #maxTargetLength}
   * 
   * @throws ErrorException 
   */
  public void fetchWindow(int[] components, int discardLevels, int[] roff,
                          int[] rsiz, int layers, int quality,
                          long maxTargetLength) throws ErrorException {
    fetchWindow(components, discardLevels, roff, rsiz, layers, quality,
            maxTargetLength, recordWOIHistory);
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param components is an one-dimension array with the component indexes.
   * @param discardLevels .
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   * @param maxTargetLength definition in {@link #maxTargetLength}
   * @param recordWOI
   *
   * @throws ErrorException  if the target (<code>requestURI</code>) cannot be
   * 			fetched from the server, or the delivery data cannot be
   * 			decompressed correctly.
   */
  public void fetchWindow(int[] components, int discardLevels, int[] roff,
                          int[] rsiz, int layers, int quality,
                          long maxTargetLength, boolean recordWOI)
          throws ErrorException {

    this.maxTargetLength = maxTargetLength;

    // Check if image's main header has been downloaded
    ClientJPEG2KCodestream codestream = getLogicalTarget().getCodestream();
    if (getLogicalTarget().getCodestream() == null) {
      getMainHeader();
    }

    ViewWindowField viewWindow = new ViewWindowField();
    CADIDimension fsiz = codestream.determineFrameSize(discardLevels);
    viewWindow.fsiz[0] = fsiz.width;
    viewWindow.fsiz[1] = fsiz.height;
    viewWindow.fsiz[2] = ViewWindowField.ROUND_DOWN;
    if (roff != null) {
      viewWindow.roff[0] = roff[0];
      viewWindow.roff[1] = roff[1];
    }
    if (rsiz != null) {
      viewWindow.rsiz[0] = rsiz[0];
      viewWindow.rsiz[1] = rsiz[1];
    }

    if (components != null) {
      viewWindow.comps = ArraysUtil.indexesToRanges(components);
    }

    if (layers >= 0) {
      viewWindow.layers = layers;
    }

    fetchWindow(viewWindow, recordWOI);
  }

  /**
   *  Get the target <data>target</data> window of interest.
   *
   * @param requestViewWindow the target Window Of Interest to fetch.
   *
   * @return
   * @throws ErrorException if an error occurs fetching the WOI.
   */
  public void getWindow(ViewWindowField requestViewWindow)
          throws ErrorException {
    getWindow(requestViewWindow, recordWOIHistory);

  }

  /**
   * 
   * @param requestViewWindow
   * @param recordWOI
   * @throws ErrorException 
   */
  public void getWindow(ViewWindowField requestViewWindow, boolean recordWOI)
          throws ErrorException {

    // Download window data
    fetchWindow(requestViewWindow, recordWOI);

    // Decode image window
    if (log.isLog(CADILog.LEVEL_INFO)) {
      log.logInfo(requestViewWindow.toString());
    }
    jp2kClientLogicalTarget.decode(requestViewWindow, responseViewWindow, imageData);
    responseViewWindow.reset();
    cache.manage();

    log.logInfo("Image decoded");
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param components is an one-dimension array with the component indexes.
   * @param discardLevels .
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   * @param maxTargetLength definition in {@link #maxTargetLength}
   * 
   * @throws ErrorException 
   */
  public void getWindow(int[] components, int discardLevels, int[] roff,
                        int[] rsiz, int layers, int quality,
                        long maxTargetLength) throws ErrorException {
    getWindow(components, discardLevels, roff, rsiz, layers, quality,
            maxTargetLength, recordWOIHistory);
  }

  /**
   * 
   * @param components
   * @param discardLevels
   * @param roff
   * @param rsiz
   * @param layers
   * @param quality
   * @param maxTargetLength
   * @param recordWOI
   * @throws ErrorException 
   */
  public void getWindow(int[] components, int discardLevels, int[] roff,
                        int[] rsiz, int layers, int quality,
                        long maxTargetLength, boolean recordWOI)
          throws ErrorException {
    this.maxTargetLength = maxTargetLength;

    // Check if image's main header has been downloaded
    ClientJPEG2KCodestream codestream = getLogicalTarget().getCodestream();
    if (getLogicalTarget().getCodestream() == null) {
      getMainHeader();
    }

    ViewWindowField viewWindow = new ViewWindowField();
    CADIDimension fsiz = codestream.determineFrameSize(discardLevels);
    viewWindow.fsiz[0] = fsiz.width;
    viewWindow.fsiz[1] = fsiz.height;
    viewWindow.fsiz[2] = ViewWindowField.ROUND_DOWN;
    if (roff != null) {
      viewWindow.roff[0] = roff[0];
      viewWindow.roff[1] = roff[1];
    }
    if (rsiz != null) {
      viewWindow.rsiz[0] = rsiz[0];
      viewWindow.rsiz[1] = rsiz[1];
    }

    if (components != null) {
      viewWindow.comps = ArraysUtil.indexesToRanges(components);
    }

    if (layers >= 0) {
      viewWindow.layers = layers;
    }

    getWindow(viewWindow, recordWOI);
  }

  /*
   * (non-Javadoc)
   * @see CADI.Common.Session.ClientSideSessionTarget#closeSession()
   */
  @Override
  public void closeSession() throws ErrorException {
    super.closeSession();
  }

  /**
   * TODO: THIS METHOD DO NOT BE USED.
   *
   * @return 
   */
  public final ClientJPEG2KCodestream getCodestream() {
    return jp2kClientLogicalTarget.getCodestream();
  }

  /**
   * TODO: THIS METHOD DO NOT BE USED.
   *
   * @return
   */
  public ClientCacheManagement getCacheManagement() {
    return cache;
  }

  /**
   * Returns the download's speed average.
   *
   * @return the download's speed average.
   */
  public float getSpeed() {
    return calculateAverageWeighted(downloadedBytes, downloadsTimes);
  }

  /**
   * 
   * @param fileName
   * @throws ErrorException
   */
  public void saveCache(String fileName) throws ErrorException {
    cache.saveCache(fileName);
  }

  /**
   * 
   * @param fileName
   * @throws ErrorException
   */
  public void loadCache(String fileName) throws ErrorException {
    cache.loadCache(fileName);
  }

  /**
   * This method returns a detailed description of the last decoded logical
   * target. This description is delivery as a html table with the key in the
   * first column and the value in the second one. If none image has been
   * decoded, a <code>null</code> pointer is returned.
   *
   * @return an HTML table description of the logical target. If none logical
   *         target has been decoded yet, a <code>null</code> pointer is
   *         returned.
   */
  public final String getTargetDescription() {
    if (getLogicalTarget() == null) {
      return null;
    }
    return getLogicalTarget().getLogicalTargetDescription();
  }

  /**
   *
   * @return
   */
  public final ImageData getImageData() {
    return imageData;
  }

  /**
   * 
   * @param inClassIdentifier
   * @param layer
   * @return
   */
  @Override
  public boolean isAvailable(long inClassIdentifier, int layer) {
    return (layer <= cache.getLastLayerOfPrecinctDataBin(inClassIdentifier))
            ? true : false;
  }

    /**
   * 
   * @param viewWindow
   * @return 
   */
  @Override
  public boolean isAvailable(ViewWindowField viewWindow) {
    return cache.isInCache(viewWindow);
  }

  
  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str += getClass().getName() + " [";
    str += "Proxy server=" + proxyServer;
    str += "Proxy port=" + proxyPort;

    str += " cache=" + "<<< Not displayed >>>";

    str += "]";

    return str;
  }

  /**
   * Prints this Client Session Target out to the specified output stream. This method
   * is useful for debugging.
   *
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Client Session Target --");

    out.println("Proxy server: " + proxyServer);
    out.println("Proxy port: " + proxyPort);

    for (Map.Entry<String, ClientJPIPChannel> entry : channels.entrySet()) {
      entry.getValue().list(out);
    }

    cache.list(out);

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * Gets the main header of the logical target.
   *
   * @throws ErrorException if an error occurs while main header is being
   * 			fetched or received data cannot be decompressed correctly.
   */
  private void getMainHeader() throws ErrorException {

    JPIPRequestFields jpipRequestFields = new JPIPRequestFields();

    int attempts = 0;
    do {
      attempts++;

      // SET JPIP PARAMETERS
      jpipRequestFields.reset();

      // Data limit field
      jpipRequestFields.dataLimitField.len = -1; // No limit
      if (maxTargetLength > 0) {
        jpipRequestFields.dataLimitField.len = (int) maxTargetLength;
      }

      // Server control fields
      if (returnType != null) {
        jpipRequestFields.serverControlField.type.add(returnType);
      } else {
        for (String type : allowedReturnTypes) {
          jpipRequestFields.serverControlField.type.add(type);
        }
      }

      // Target fields
      String channelID = manageChannels();
      if (channels.get(channelID).getCID() == null) {
        jpipRequestFields.targetField.target = getTarget();
        // Session is not established
        if (getTID() == null) { // Server does not give a target identifier
          jpipRequestFields.targetField.tid = "0";
        } else { // Server gave a target identifier
          jpipRequestFields.targetField.target = getTarget();
          jpipRequestFields.targetField.tid = getTID();
        }
      } else {
        // Session is established, server gave a channel identifier
        assert (!useWildcard());
        assert (!useIndexRange());
        jpipRequestFields.targetField.tid = null;
        jpipRequestFields.channelField.path = null;
        jpipRequestFields.channelField.cid = channels.get(channelID).getCID();
      }
      
      // SEND REQUEST AND GET RESPONSE HEADER
      responseViewWindow = new ViewWindowField();

      try {
        //String channelID = manageChannels();
        channels.get(channelID).getRequest(jpipRequestFields);
        copyJPIPResponseFields(getJPIPResponseFields());
      } catch (WarningException e) {
        throw new ErrorException(e.getMessage());
      } catch (Exception ioe) {
        throw new ErrorException("It is impossible to send the request to the server \"" + getServer() + "\"");
      }

    } while ((attempts <= 3) && (jp2kClientLogicalTarget.getCodestream() == null));

    //cache.list(System.out); // DEBUG

    if (jp2kClientLogicalTarget.getCodestream() == null) {
      throw new ErrorException("Main header cannot be downloaded correctly");
    }

    cache.setJPEG2KCodestream(jp2kClientLogicalTarget.getCodestream());
  }

  /**
   *
   * @return a channel identifier.
   */
  private String manageChannels() {

    String localCIDSession = null;
    String localCIDWithoutSession = null;

    // Find a channel
    for (Map.Entry<String, ClientJPIPChannel> entry : channels.entrySet()) {
      if (entry.getValue().getCID() != null) {
        localCIDSession = entry.getValue().getLocalCID();
        break;
      } else {
        localCIDWithoutSession = entry.getValue().getLocalCID();
      }
    }


    if (localCIDSession != null) {
      return localCIDSession;
    } else if (localCIDWithoutSession != null) {
      return localCIDWithoutSession;
    } else {
      // There is no channels available,
      // so that a new one must be created
      ClientJPIPChannel channel = new ClientJPIPChannel(preferredTransportProtocols, server, port, proxyServer, proxyPort, cache, log);
      channel.setKeepAlive(useKeepAlive);
      channel.setUserAgent(userAgent);
      if (debug != null) {
        channel.setDebug(debug);
      }
      ArrayList<String> accept = new ArrayList<String>();
      accept.add("image/jpp-stream");
      channel.setAccept(accept);
      channels.put(channel.getLocalCID(), channel);

      return channel.getLocalCID();
    }

  }

  /**
   * This method updates the {@link #downloadedBytes} and
   * {@link #downloadsTimes} attributes with new new values which are parssed
   * as input parameters.
   *
   * @param bytes the new value to be added in the {@link #downloadedBytes}
   * 			attribute.
   * @param time the new value to be added in the {@link #downloadsTimes}
   * 			attribute.
   */
  private void updateReceivedBytesAndTimes(long bytes, long time) {
    for (int i = 1; i < downloadedBytes.length; i++) {
      downloadedBytes[i - 1] = downloadedBytes[i];
      downloadsTimes[i - 1] = downloadsTimes[i];
    }
    downloadedBytes[downloadedBytes.length - 1] = bytes;
    downloadsTimes[downloadsTimes.length - 1] = time;
  }

  /**
   * Calculates the average of bytes per second. The average is weighted
   * following a gaussian distribution.
   *
   * @param bytes is an one-dimensional array with the amount of bytes.
   * @param times is an one-dimensional array with the time where bytes
   * 			was sent. It is expressed in miliseconds. The first index is
   * 			when the first data was sent, and the last one is the most
   * 			most recently time.
   *
   * @return the average of bytes per second.
   */
  public static float calculateAverageWeighted(long[] bytes, long[] times) {

    // Check input parameters
    if (bytes == null) {
      throw new NullPointerException();
    }
    if (times == null) {
      throw new NullPointerException();
    }
    if (bytes.length != times.length) {
      throw new NullPointerException();
    }
    for (int i = 0; i < bytes.length; i++) {
      if ((bytes[i] < 0) || (times[i] < 0)) {
        throw new IllegalArgumentException();
      }
    }

    int length = bytes.length;

    // Time is passed to sec.
    long[] timesSec = new long[length];
    for (int i = 0; i < length; i++) {
      timesSec[i] = times[i] / 1000L;
    }

    // Calculates weights
    long time = System.currentTimeMillis() / 1000L;
    float[] weights = new float[times.length];
    float sigma = 1F;
    //float mu = 0;
    float factor1 = (float) Math.sqrt(2 * Math.PI) * sigma;
    float factor2 = 2F * sigma * sigma;
    for (int i = 0; i < weights.length; i++) {
      weights[i] = (float) Math.exp(-(time - timesSec[i]) * (time - timesSec[i]) / factor2) / factor1;
    }

    // Calculate weighted average
    float average = 0F;
    for (int i = 0; i < times.length; i++) {
      average += weights[i] * bytes[i];
    }

    return average;
  }
}
