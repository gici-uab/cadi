/*
 CADI Software - a JPIP Client/Server framework
 Copyright (C) 2007-2012 Group on Interactive Coding of Images (GICI)

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.

 Group on Interactive Coding of Images (GICI)
 Department of Information and Communication Engineering
 Autonomous University of Barcelona
 08193 - Bellaterra - Cerdanyola del Valles (Barcelona)
 Spain

 http://gici.uab.es
 gici-info@deic.uab.es
 */
package CADI.Proxy.Client;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;

import CADI.Client.Cache.ClientCacheManagement;
import CADI.Client.Session.ClientJPIPChannel;
import CADI.Common.Cache.ModelElement;
import CADI.Common.Log.CADILog;
import CADI.Common.LogicalTarget.JPEG2000.PredictiveScalingFactors;
import CADI.Common.LogicalTarget.JPEG2000.RelevantPrecinct;
import CADI.Common.Network.JPIP.ClassIdentifiers;
import CADI.Common.Network.JPIP.JPIPRequestFields;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Common.Session.ClientSideSessionTarget;
import CADI.Proxy.Core.SendDataInfo;
import CADI.Proxy.LogicalTarget.JPEG2000.JP2KProxyLogicalTarget;
import CADI.Proxy.LogicalTarget.JPEG2000.ProxyJPEG2KCodestream;
import CADI.Proxy.Server.ProxyCacheModel;
import GiciException.ErrorException;
import GiciException.WarningException;
import java.util.HashMap;

/**
 This class is used to save information and data of a logical target that is
 being served by the proxy.

 @author Group on Interactive Coding of Images (GICI)
 @version 1.0.4 2012/03/09
 */
public class ProxySessionTarget extends ClientSideSessionTarget {

  /**
   Is the Target Identifier (TID) of this logical target given by the JPIP
   Proxy server. Usually, the identifier could be the same as the
   <code>tid</code> but it is not mandatory.
   <p>
   Note: this attribute could be replaced by the {@link CADI.Common.Session.ClientSideSessionTarget#sid}.
   */
  private String proxyTID = null;

  // INTERNAL ATTRIBUTES
  /**
   * It is a cache of logical target's data downloaded from the server.
   */
  private ProxyCacheManagement cache = null;

  /**
   *
   */
  private JP2KProxyLogicalTarget jp2kProxyLogicalTarget = null;

  /**
   * 
   */
  private String proxyVia = null;
  
  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.PredictiveScalingFactors}.
   */
  private PredictiveScalingFactors scalingFactors = null;

  // ============================= public methods ==============================
  /**
   *
   * @param server
   * @param port
   * @param target
   * @param log
   */
  public ProxySessionTarget(String server, int port, String target,
          CADILog log) {
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
  public ProxySessionTarget(String server, int port, String target,
          ArrayList<String> preferredTransportProtocols,
          CADILog log) {
    super(server, port, target, preferredTransportProtocols, log);

    // Create the cache
    cache = new ProxyCacheManagement();

    // Creates the proxy tid
    proxyTID = "Proxy-" + generateUID();

    // Create jpeg2000 decoder
    jp2kProxyLogicalTarget = new JP2KProxyLogicalTarget(proxyTID, cache, log);

    userAgent = "CADI Proxy";
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
    return super.newChannel(preferredTransportProtocols, cache);
  }

  /**
   * Returns the {@link #proxyTID} attribute.
   *
   * @return definition in {@link #proxyTID}.
   */
  public String getProxyTID() {
    return proxyTID;
  }

  public int getManagementPolicy() {
    return cache != null ? cache.getManagementPolicy() : managementPolicy;
  }

  public long getMaxCacheSize() {
    return cache != null ? cache.getMaxCacheSize() : maxCacheSize;
  }

  /**
   * Removes all the attributes.
   */
  @Override
  public void remove() {
    super.remove();
    proxyTID = null;
    cache.reset();
    cache = null;
    cache = null;
  }

  public void setMaxCacheSize(long maxCacheSize) {
    cache.setMaxCacheSize(maxCacheSize);
  }

  public void setManagementPolicy(int managementPolicy) {
    cache.setManagementPolicy(managementPolicy);
  }

  public void setAllowedReturnTypes(int[] returnTypes) {
    if (returnTypes == null) {
      throw new NullPointerException();
    }

    assert (true);
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
   * Get the target <data>target</data> window of interest.
   *
   * @param viewWindow the target Window Of Interest to fetch.
   *
   * @return <code>true</code> if the requested WOI is available in the cache.
   * 	Otherwise, returns <code>false</code>.
   * 
   * @throws ErrorException if an error occurs fetching the WOI.
   */
  public void fetchWindow(ViewWindowField requestViewWindow) throws ErrorException {
    fetchWindow(requestViewWindow, recordWOIHistory);
  }

  /**
   *  Get the target <data>target</data> window of interest.
   *
   * @param requestViewWindow the target Window Of Interest to fetch.
   * @param recordWOI
   * 
   * @return
   * @throws ErrorException if an error occurs fetching the WOI.
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

    // Check if multiple component transformation
    ViewWindowField viewWindowToSend = new ViewWindowField();
    ViewWindowField.deepCopy(requestViewWindow, viewWindowToSend);


    //log.logInfo("\nNEW VIEW WINDOW\n" + requestViewWindow.toString());


    // Clear cache?
    if (!reuseCache) {
      cache.clear();
    }

    // Check view window in cache.
    //If the maxTargetLength parameter is set, cache data is increased with new maxTargetLength data from server.
    boolean isInCache = cache.isInCache(viewWindowToSend);


    if (!isInCache) {
      log.logInfo("   - Data is not available in client cache"); // DEBUG

      JPIPRequestFields jpipRequestFields = new JPIPRequestFields();


      // SET JPIP PARAMETERS

      // Target fields
      String channelID = manageChannels();
      channels.get(channelID).setUserAgent(userAgent);
      if (channels.get(channelID).getCID() == null) {
        // Session is not established
        if (tid == null) { // Server does not give a target identifier
          assert (getTarget() != null);
          jpipRequestFields.targetField.target = getTarget();
        } else { // Server gave a target identifier
          jpipRequestFields.targetField.target = getTarget();
          jpipRequestFields.targetField.tid = tid;
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
        jpipRequestFields.dataLimitField.len = (int)maxTargetLength;
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
        e.printStackTrace();
        throw new ErrorException(e.getMessage());
      } catch (UnsupportedOperationException uoe) {
        uoe.printStackTrace();
        throw new UnsupportedOperationException();
      }

      if (!channels.get(channelID).isKeepAlive()) {
        channels.get(channelID).close();
      }

    } else {
      log.logInfo("   - Data is available in client cache");
    }

    log.logInfo(requestViewWindow.toString());


    responseViewWindow.reset();
    cache.manage();

    lock.unlock();

    log.logInfo("Image decoded");
  }

  /**
   * Gets the main header of the logical target.
   *
   * @throws ErrorException if an error occurs while main header is being
   * 			fetched or received data cannot be decompressed correctly.
   */
  public void getMainHeader() throws ErrorException {

    lock.lock();

    JPIPRequestFields jpipRequestFields = new JPIPRequestFields();

    // SET JPIP PARAMETERS
    jpipRequestFields.targetField.target = getTarget();
    if (tid == null) {
      jpipRequestFields.targetField.tid = "0";
    } // Request a tid

    // Data limit field
    if (maxTargetLength > 0) {
      jpipRequestFields.dataLimitField.len = (int)maxTargetLength;
    }

    // Server control fields
    if (returnType != null) {
      jpipRequestFields.serverControlField.type.add(returnType);
    } else {
      for (String type : allowedReturnTypes) {
        jpipRequestFields.serverControlField.type.add(type);
      }
    }


    // SEND REQUEST AND GET RESPONSE HEADER
    responseViewWindow = new ViewWindowField();

    try {
      String channelID = manageChannels();
      channels.get(channelID).getRequest(jpipRequestFields);
      copyJPIPResponseFields(getJPIPResponseFields());
    } catch (WarningException e) {
      //e.printStackTrace();
      throw new ErrorException(e.getMessage());
    } catch (Exception ioe) {
      //ioe.printStackTrace();
      throw new ErrorException("It is impossible to send the request to the server \""
              + getServer() + "\"");
    }

    // DECODES MAIN HEADER

    if (!cache.isComplete(ClassIdentifiers.MAIN_HEADER, 0)) { //if (!cache.isCompleteMainHeader()) {
      throw new ErrorException();
    }


    cache.setJPEG2KCodestream(jp2kProxyLogicalTarget.getCodestream(0));
    lock.unlock();
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
   * Returns the {@link #jpcParameters} attribute.
   *
   * @return definition in {@link #jpcParameters}.
   */
  public final ProxyJPEG2KCodestream getCodestream() {
    return cache.getProxyJPEG2KCodestream();
  }

  /**
   * Returns the {@link #jp2kProxyLogicalTarget} attribute.
   *
   * @return definition in {@link #jp2kProxyLogicalTarget}.
   */
  public final JP2KProxyLogicalTarget getJP2KProxyLogicalTarget() {
    return jp2kProxyLogicalTarget;
  }

  /**
   *
   * @return
   */
  public ProxyCacheManagement getCacheManagement() {
    return cache;
  }

  /**
   *
   * @param viewWindow
   * @param relevantPrecincts
   * @param cacheManagement
   * @param availableData
   * @param unAvailableData
   */
  public void checkAvailableData(ViewWindowField viewWindow,
          ArrayList<RelevantPrecinct> relevantPrecincts,
          ProxyCacheModel cacheModel,
          ArrayList<SendDataInfo> availableData,
          ArrayList<SendDataInfo> unAvailableData) {
    
    cache.checkAvailableData(viewWindow, relevantPrecincts, cacheModel,
            availableData, unAvailableData);
  }

  /**
   *
   * @param unAvailableData
   */
  public void getRemainder(ProxyCacheModel cacheModel,
          ArrayList<SendDataInfo> unAvailableData) {
    cache.getRemainderData(cacheModel, unAvailableData);
  }

  /**
   * 
   * @param via
   */
  public void setProxyVia(String via) {
    this.proxyVia = via;
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


 public void setPredictiveModel(PredictiveScalingFactors scalingFactors) {
   this.scalingFactors = scalingFactors;
 }
 
 public PredictiveScalingFactors getPredictiveModel() {
   return scalingFactors;
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
    str += "Proxy TID=" + proxyTID;
    str += cache.toString();

    if (woiHistory != null) {
      System.out.println("History of WOIs");
      for (ViewWindowField woi : woiHistory) {
        str += woi.toString();
      }
    }

    str += "]";

    return str;
  }

  /**
   * Prints this Proxy Session Target out to the specified output stream. This method
   * is useful for debugging.
   *
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Proxy Session Target --");
    super.list(out);

    out.println("Proxy TID=" + proxyTID);
    cache.list(out);

    if (woiHistory != null) {
      System.out.println("History of WOIs");
      for (ViewWindowField woi : woiHistory) {
        woi.list(out);
      }
    }

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   *
   * @return
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
      ClientJPIPChannel channel = new ClientJPIPChannel(preferredTransportProtocols, server, port, cache, log);
      channel.setKeepAlive(useKeepAlive);
      if (userAgent != null) {
        channel.setUserAgent(userAgent);
      }
      if (proxyVia != null) {
        channel.setVia(proxyVia);
      }
      ArrayList<String> accept = new ArrayList<String>();
      accept.add("image/jpp-stream");
      channel.setAccept(accept);
      channels.put(channel.getLocalCID(), channel);

      return channel.getLocalCID();
    }

  }
}
