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
package CADI.Client;

import java.io.File;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import CADI.Client.Session.ClientSessionTarget;
import CADI.Common.Core.PrefSemaphore;
import CADI.Common.Network.JPIP.*;
import CADI.Common.Util.StopWatch;
import CADI.Common.Log.CADILog;
import CADI.Common.Network.JPIP.JPIPRequestDecoder;
import CADI.Common.Util.ArraysUtil;
import GiciException.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements an API of the JPIP client protocol.
 * <p>
 * The API defined in this class abstracts the user from the underlying JPIP
 * protocol. After construct an object of this class, it is mandatory to
 * requests for a new session specifying a shared memory, server, port, and
 * the requested target. Once the session has been created, several properties
 * can be set by means of the setMethods, and requests to the server can be
 * performed through the getTarget methods.
 * <p>
 * The requested image is stored in a shared object of type
 * <code>ImageData</code>.
 *
 * <p>
 * Usage example:<br>
 * &nbsp; constructor<br>
 * &nbsp; newSession<br>
 * &nbsp; set methods<br>
 * &nbsp; getTarget<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.9 2011/12/03
 */
public class Client {

  /**
   * Defines a table where the client sessions managed by this object are saved.
   * <p>
   * The table is defined as a hash to perform a quick access. The
   * <code>key
   * </code> used to identify the value of the map is a {@link java.lang.String}
   */
  private HashMap<String, ClientSessionTarget> clientSessionTargets = null;

  // JPIP sessions
  /**
   * Indicates whether stateless request or session request will be used.
   */
  private boolean useSession = false;

  /**
   * Indicates if HTTP session is allowed.
   * <p>
   * This attribute is only allowed when <data>useSession</data> attribute is true.
   */
  public boolean useHTTPSession = false;

  /**
   * Indicates if HTTP TCP session is allowed.
   * <p>
   * This attribute is only allowed when <data>useSession</data> attribute is true.
   */
  public boolean useHTTPTCPSession = false;

  // INTERNAL ATTRIBUTES
  /**
   * Contains the value of the HTTP user-agent feature.
   */
  private String userAgent = "CADI Client";

  private String debug = null;

  /**
   * Contains the view window requested by the client application.
   */
  private ViewWindowField requestViewWindow = null;

  /**
   * Is used to log the client messages.
   */
  private CADILog log = null;

  /**
   * Indicates whether the HTTP connection to remote server are persistents.
   */
  private boolean keepAlive = true;

  /**
   * Contains the local host name.
   */
  private String clientHostName = null;

  /**
   * This string keeps the identifier of the default session.
   */
  private String defaultSession = null;

  /**
   *
   */
  private ClientPrefetching prefetching = null;

  /**
   *
   */
  private PrefSemaphore proxyMutex = null;

  // Time steps measures
  /**
   * Used for verbose information (time for stage).
   * <p>
   * 0 is initial time.
   */
  private long initStageTime = 0;

  /**
   * Used for verbose information (total time).
   * <p>
   * 0 is initial time.
   */
  private long initTime = 0;

  /**
   *
   */
  private StopWatch stopWatch = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public Client() {
    this(new CADILog(null, false));
  }

  /**
   * Constructor.
   *
   * @param log definition in {@link #log}.
   */
  public Client(CADILog log) {

    // Check input parameters
    if (log == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.log = log;

    // Create objects
    requestViewWindow = new ViewWindowField();
    clientSessionTargets = new HashMap<String, ClientSessionTarget>();
    stopWatch = new StopWatch();

    // Get the client hostname
    try {
      clientHostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      clientHostName = "localhost";
    }

    proxyMutex = new PrefSemaphore();
  }

  /**
   * Constructor.
   *
   * @param logFile definition in {@link CADI.Common.Log.CADILog#fileName}.
   * @param logLevel definition in {@link CADI.Common.Log.CADILog#logLevel}.
   */
  public Client(String logFile, int logLevel) {
    this(new CADILog(logFile, logLevel));
  }

  //
  // Session management
  //
  /**
   * Creates a new session.
   *
   * @param imageData
   * @param url
   *
   * @return a session identifier. Definition in {@link CADI.Common.Session.ClientSideSessionTarget#sid}.
   */
  public String newSession(ImageData imageData, String url) {
    return null;
  }

  /**
   * Creates a new session.
   *
   * @param imageData
   * @param server definition in {@link CADI.Common.Session.ClientSideSessionTarget#server}.
   * @param port definition in {@link CADI.Common.Session.ClientSideSessionTarget#port}.
   * @param target definition in {@link CADI.Common.Network.JPIP.TargetField#target}
   *
   * @return a session identifier. Definition in {@link CADI.Common.Session.ClientSideSessionTarget#sid}.
   */
  public String newSession(ImageData imageData, String server, int port,
                           String target) {
    return newSession(imageData, server, port, null, -1, target);
  }

  /**
   * Creates a new session.
   *
   * @param imageData
   * @param server definition in {@link CADI.Common.Session.ClientSideSessionTarget#server}.
   * @param port definition in {@link CADI.Common.Session.ClientSideSessionTarget#port}.
   * @param proxyServer definition in {@link CADI.Client.Session.ClientSessionTarget#proxyServer}.
   * @param proxyPort definition in {@link CADI.Client.Session.ClientSessionTarget#proxyPort}.
   * @param target definition in {@link CADI.Common.Network.JPIP.TargetField#target}
   *
   * @return a session identifier. Definition in {@link CADI.Common.Session.ClientSideSessionTarget#sid}.
   */
  public String newSession(ImageData imageData, String server, int port,
                           String proxyServer, int proxyPort, String target) {

    ClientSessionTarget sessionTarget =
            new ClientSessionTarget(server, port, proxyServer, proxyPort, target,
                                    imageData, null, log);
    clientSessionTargets.put(sessionTarget.getSessionID(), sessionTarget);

    if (defaultSession == null) {
      defaultSession = sessionTarget.getSessionID();
    }
    sessionTarget.setRecordWOIHistory(true);

    return sessionTarget.getSessionID();
  }

  /**
   * Creates a new session.
   *
   * @param imageData
   * @param server definition in {@link CADI.Common.Session.ClientSideSessionTarget#server}.
   * @param port definition in {@link CADI.Common.Session.ClientSideSessionTarget#port}.
   * @param proxyServer definition in {@link CADI.Client.Session.ClientSessionTarget#proxyServer}.
   * @param proxyPort definition in {@link CADI.Client.Session.ClientSessionTarget#proxyPort}.
   * @param target definition in {@link CADI.Common.Network.JPIP.TargetField#target}
   * @param numThreads definition in {@link  CADI.Client.ClientLogicalTarget.JPEG2000.JPEG2KDecoder#numThreads}.
   *
   * @return a session identifier. Definition in {@link CADI.Common.Session.ClientSideSessionTarget#sid}.
   */
  public String newSession(ImageData imageData, String server, int port,
                           String proxyServer, int proxyPort, String target, int numThreads) {

    ClientSessionTarget sessionTarget =
            new ClientSessionTarget(server, port, proxyServer, proxyPort, target,
                                    imageData, null, log, numThreads);
    clientSessionTargets.put(sessionTarget.getSessionID(), sessionTarget);

    if (defaultSession == null) {
      defaultSession = sessionTarget.getSessionID();
    }
    sessionTarget.setRecordWOIHistory(true);

    return sessionTarget.getSessionID();
  }

  /**
   * Creates a new session.
   * <p>
   * If the <code>movementProbabilities</code> parameter is <code>null</code>,
   * all movements are considered equally <code>probable</code>.
   *
   * @param imageData
   * @param server definition in {@link CADI.Common.Session.ClientSideSessionTarget#server}.
   * @param port definition in {@link CADI.Common.Session.ClientSideSessionTarget#port}.
   * @param proxyServer definition in {@link CADI.Client.Session.ClientSessionTarget#proxyServer}.
   * @param proxyPort definition in {@link CADI.Client.Session.ClientSessionTarget#proxyPort}.
   * @param target definition in {@link CADI.Common.Network.JPIP.TargetField#target}
   * @param prefetchingWOIType definition in {@link CADI.Common.Core.Prefetching#prefetchingWOIType}.
   * @param movementProbabilities definition in {@link CADI.Common.Core.Prefetching#movements}.
   *
   * @return a session identifier. Definition in {@link CADI.Common.Session.ClientSideSessionTarget#sid}.
   */
  public String newSession(ImageData imageData, String server, int port,
                           String proxyServer, int proxyPort, String target,
                           int prefetchingWOIType, float[] movementProbabilities) {

    return newSession(imageData, server, port, proxyServer, proxyPort,
                      target, -1, prefetchingWOIType, movementProbabilities);
  }

  /**
   * Creates a new session.
   *
   * @param imageData
   * @param server definition in {@link CADI.Common.Session.ClientSideSessionTarget#server}.
   * @param port definition in {@link CADI.Common.Session.ClientSideSessionTarget#port}.
   * @param proxyServer definition in {@link CADI.Client.Session.ClientSessionTarget#proxyServer}.
   * @param proxyPort definition in {@link CADI.Client.Session.ClientSessionTarget#proxyPort}.
   * @param target definition in {@link CADI.Common.Network.JPIP.TargetField#target}
   * @param numThreads definition in {@link  CADI.Client.ClientLogicalTarget.JPEG2000.JPEG2KDecoder#numThreads}.
   * @param prefetchingWOIType definition in {@link CADI.Common.Core.Prefetching#prefetchingWOIType}.
   * @param movementProbabilities definition in {@link CADI.Common.Core.Prefetching#movements}.
   *
   * @return a session identifier. Definition in {@link CADI.Common.Session.ClientSideSessionTarget#sid}.
   */
  public String newSession(ImageData imageData, String server, int port,
                           String proxyServer, int proxyPort, String target, int numThreads,
                           int prefetchingWOIType, float[] movementProbabilities) {

    String sessionID = newSession(imageData, server, port, proxyServer,
                                  proxyPort, target, numThreads);

    if (movementProbabilities == null) {
      movementProbabilities = new float[10];
      movementProbabilities[0] = movementProbabilities[1] = 0.1F;
      movementProbabilities[2] = movementProbabilities[3] = 0.1F;
      movementProbabilities[4] = movementProbabilities[5] = 0.1F;
      movementProbabilities[6] = movementProbabilities[7] = 0.1F;
      movementProbabilities[8] = movementProbabilities[9] = 0.1F;
    }

    prefetching = new ClientPrefetching("Client_Prefetching",
                                        clientSessionTargets, proxyMutex, log, prefetchingWOIType,
                                        movementProbabilities);
    prefetching.start();

    return sessionID;
  }

  /**
   * Returns the identifier of which is considered the default session.
   *
   * @return definition in {@link #defaultSession}.
   */
  public String getDefaultSession() {
    return defaultSession;
  }

  /**
   * Returns a set with the active session identifiers.
   *
   * @return a set with the active session identifiers.
   */
  public Set<String> getSessionIdentifiers() {
    return clientSessionTargets.keySet();
  }

  /**
   * Sets as default session the session identified by the parameter
   * <code>sessionID</code>.
   */
  public void setDefaultSession(String sessionID) {
    for (String key : clientSessionTargets.keySet()) {
      if (key.equals(sessionID)) {
        defaultSession = sessionID;
        return;
      }
    }
  }

  /**
   * Removes a session identified by <code>sessionID</code> identifier.
   *
   * @param sessionID
   */
  public void removeSession(String sessionID) {
    closeSession(sessionID);
    if (clientSessionTargets.containsKey(sessionID)) {
      clientSessionTargets.get(sessionID).remove();
      clientSessionTargets.remove(sessionID);
    }
  }

  /**
   * Closes the session with the server. Note that data of all logical
   * targets belonging to the current session are also removed, as well
   * the session with the remote server is closed too.
   *
   * @return <code>true</code> if the session has been closed without
   * any problem. Otherwise, returns <code>false</code>.
   *
   * @throws ErrorException it an error occurs while the session is being
   * closed.
   */
  public void closeSession(String sessionID) {
    try {
      if (clientSessionTargets.containsKey(sessionID)) {
        clientSessionTargets.get(sessionID).closeSession();
      }
    } catch (ErrorException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sets the {@link #useHTTPSession} attribute.
   *
   * @param useHTTPSession {@link #useHTTPSession}.
   */
  public void setUseHTTPSession(boolean useHTTPSession) {
    if (defaultSession == null) {
      throw new IllegalArgumentException("None session has been set as default.");
    }
    this.useHTTPSession = useHTTPSession;
    setUseHTTPSession(defaultSession, useHTTPSession);
  }

  /**
   *
   * @param sessionID
   * @param useHTTPSession
   */
  public void setUseHTTPSession(String sessionID, boolean useHTTPSession) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    clientSessionTargets.get(sessionID).setUseHTTPSession(useHTTPSession);
  }

  /**
   *
   * @param sessionID
   * @param useHTTPTCPSession
   *
   * @return
   * <code>true</code> if parameter has been set, or
   * <code>false
   * </code> if the session does not exist.
   */
  public void setUseHTTPTCPSession(String sessionID,
                                   boolean useHTTPTCPSession) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \""
              + sessionID + "\"");
    }

    clientSessionTargets.get(sessionID).setUseHTTPTCPSession(useHTTPTCPSession);
  }

  /**
   * Sets the {@link #useHTTPTCPSession} attribute
   *
   * @param useHTTPTCPSession {@link #useHTTPTCPSession}.
   */
  public void setUseHTTPTCPSession(boolean useHTTPTCPSession) {
    setUseHTTPTCPSession(defaultSession, useHTTPTCPSession);
  }

  /**
   * Sets if the client cache must be used. If client cache mustn't be used,
   * every time a new target is requested all previously saved data will be
   * discarded.
   *
   * @param reuseCache
   * <code>true</code> if the cache is reseted every time a
   * new target is requested.
   */
  public void reuseCache(boolean reuseCache) {
    reuseCache(defaultSession, reuseCache);
  }

  /**
   * Sets if the client cache must be used. If client cache mustn't be used,
   * every time a new target is requested all previously saved data will be
   * discarded.
   *
   * @param sessionID
   * @param reuseCache
   * <code>true</code> if the cache is reseted every time a
   * new target is requested
   */
  public void reuseCache(String sessionID, boolean reuseCache) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    clientSessionTargets.get(sessionID).reuseCache(reuseCache);
  }

  /**
   * Sets the cache descriptor form type and qualifier for the session
   * <code>
   * sessionID</code>.
   *
   * @param sessionID unique session identifier.
   * @param type definition in {@link #descriptorType}.
   * @param qualifier definition in {@link #descritorQualifier}.
   */
  public void setCacheDescriptor(String sessionID, int type, int qualifier) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \""
              + sessionID + "\"");
    }
    clientSessionTargets.get(sessionID).setCacheDescriptor(type, qualifier);
  }

  /**
   * OBS: This method must be replaced by the {@link #setCacheDescriptor(java.lang.String, int, int)}.
   *
   * @param sessionID
   * @param cacheType
   *
   * @return
   */
  public void setCacheType(String sessionID, int cacheType) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \""
              + sessionID + "\"");
    }

    clientSessionTargets.get(sessionID).setCacheDescriptorType(cacheType);
  }

  /**
   * OBS: This method must be replaced by the {@link #setCacheDescriptor(java.lang.String, int, int)}.
   *
   * @param sessionID
   * @param useWildcard
   */
  public void setUseWildcard(String sessionID, boolean useWildcard) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \""
              + sessionID + "\"");
    }

    clientSessionTargets.get(sessionID).setUseWildcard(useWildcard);
  }

  /**
   * OBS: This method must be replaced by the {@link #setCacheDescriptor(java.lang.String, int, int)}.
   *
   * @param sessionID
   * @param useIndexRange
   */
  public void setUseIndexRange(String sessionID, boolean useIndexRange) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \""
              + sessionID + "\"");
    }

    clientSessionTargets.get(sessionID).setUseIndexRange(useIndexRange);
  }

  /**
   * OBS: This method must be replaced by the {@link #setCacheDescriptor(java.lang.String, int, int)}.
   *
   * @param sessionID
   * @param useNumberOfLayers
   */
  public void setUseNumberOfLayers(String sessionID, boolean useNumberOfLayers) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \""
              + sessionID + "\"");
    }

    clientSessionTargets.get(sessionID).setUseNumberOfLayers(useNumberOfLayers);
  }

  /**
   * OBS: This method must be replaced by the {@link #setCacheDescriptor(java.lang.String, int, int)}.
   *
   * @param sessionID
   * @param useNumberOfBytes
   */
  public void setUseNumberOfBytes(String sessionID, boolean useNumberOfBytes) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \""
              + sessionID + "\"");
    }

    clientSessionTargets.get(sessionID).setUseNumberOfBytes(useNumberOfBytes);
  }

  /**
   * Definition in {@link CADI.Client.Cache.ClientCache#setMaxCacheSize(long)}.
   *
   * @param maxCacheSize is the maximum size (in bytes) allowed for the
   * cached data. Only positive values are allowed, a value of 0
   * means unlimited.
   */
  public final void setMaxCacheSize(long maxCacheSize) {
    setMaxCacheSize(defaultSession, maxCacheSize);
  }

  /**
   * Definition in {@link CADI.Client.Cache.ClientCache#setMaxCacheSize(long)}.
   *
   * @param sessionID
   * @param maxCacheSize is the maximum size (in bytes) allowed for the
   * cached data. Only positive values are allowed, a value of 0
   * means unlimited.
   */
  public void setMaxCacheSize(String sessionID, long maxCacheSize) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \""
              + sessionID + "\"");
    }

    clientSessionTargets.get(sessionID).setMaxCacheSize(maxCacheSize);
  }

  /**
   * Definition in
   * {@link CADI.Client.Cache.ClientCache#setManagementPolicy(int)}.
   *
   * @param managementPolicy definition in {@link #managementPolicy}.
   */
  public final void setManagementPolicy(int managementPolicy) {
    setManagementPolicy(defaultSession, managementPolicy);
  }

  /**
   * Definition in
   * {@link CADI.Client.Cache.ClientCache#setManagementPolicy(int)}.
   *
   * @param sessionID
   * @param managementPolicy definition in {@link #managementPolicy}.
   */
  public void setManagementPolicy(String sessionID, int managementPolicy) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \""
              + sessionID + "\"");
    }

    clientSessionTargets.get(sessionID).setManagementPolicy(managementPolicy);
  }

  public void setUseKeepAlive(String sessionID, boolean useKeepAlive) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \""
              + sessionID + "\"");
    }

    clientSessionTargets.get(sessionID).setUseKeepAlive(useKeepAlive);
  }

  /**
   * Sets the return types allowed by the JPIP client to receive data from the
   * JPIP server.
   *
   * @param sessionID
   * @param returnTypes
   */
  public void setAllowedReturnTypes(String sessionID,
                                    ArrayList<String> returnTypes) {
    setAllowedReturnTypes(sessionID, returnTypes, false);
  }

  /**
   * Sets the return types allowed by the JPIP client to receive data from the
   * JPIP server.
   * <p>
   * Further information, see {@link CADI.Client.Session.ClientSessionTarget#setAllowedReturnTypes(ArrayList, boolean)}.
   *
   * @param sessionID
   * @param returnTypes
   * @param extendedHeaders
   */
  public void setAllowedReturnTypes(String sessionID,
                                    ArrayList<String> returnTypes,
                                    boolean extendedHeaders) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \""
              + sessionID + "\"");
    }

    clientSessionTargets.get(sessionID).setAllowedReturnTypes(returnTypes, extendedHeaders);
  }

  //
  // Several get targets
  // 
  /**
   * This method fetches a logical target.
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget() throws ErrorException, IllegalAccessException {
    getTarget(-1);
  }

  /**
   * This method fetches a logical target at a fixed length. A positive value
   * of the
   * <code>maxTargetLength</code> parameters means a maximum limit for
   * the compressed logical target; and a negative value means the maximum is
   * unlimited.
   *
   * @param maxTargetLength maximum length of the compressed target. A
   * positive value means a maximum, and a negative value means
   * unlimited.
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(long maxTargetLength)
          throws IllegalAccessException, ErrorException {

    if (defaultSession == null) {
      throw new IllegalArgumentException("None session has been set as default"
              + " yet.");
    }

    int[] components = null;
    getTarget(components, null, null, null, -1, 100, ViewWindowField.ROUND_DOWN,
              maxTargetLength);
  }

  /**
   * This method fetches a logical target at a fixed length. A positive value
   * of the
   * <code>maxTargetLength</code> parameters means a maximum limit for
   * the compressed logical target; and a negative value means the maximum is
   * unlimited.
   *
   * @param maxTargetLength maximum length of the compressed target. A
   * positive value means a maximum, and a negative value means
   * unlimited.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(long maxTargetLength, int layers, int quality)
          throws IllegalAccessException, ErrorException {

    if (defaultSession == null) {
      throw new IllegalArgumentException("None session has been set as default"
              + " yet.");
    }

    int[] components = null;
    getTarget(components, null, null, null, layers, quality > 0 ? quality : 100,
              ViewWindowField.ROUND_DOWN, maxTargetLength);

  }

  /**
   * Get the target which is identified by the URI
   * <code>requestURI</code>
   *
   * @param requestURI is the URI to locate the target.
   *
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(String requestURI) throws ErrorException {
    if (defaultSession == null) {
      throw new IllegalArgumentException("None session has been set as default"
              + " yet.");
    }
    getTarget(defaultSession, requestURI);
  }

  /**
   * Get the target which is identified by the URI
   * <code>requestURI</code>
   *
   * @param requestURI is the URI to locate the target.
   *
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(String sessionID, String requestURI)
          throws ErrorException {

    if (requestURI == null) {
      throw new NullPointerException("The \'URI\'"
              + " argument cannot be null");
    }

    if (log.isLog(CADILog.LEVEL_INFO)) {
      log.logInfo(requestURI);
    }

    initTime = System.currentTimeMillis();
    initStageTime = initTime;

    // Check if malformed and absolute
    URI uri = null;
    try {
      uri = new URI(requestURI);
    } catch (URISyntaxException e) {
      throw new ErrorException("Malformed URI.");
    }

    if (!uri.isAbsolute()) {
      throw new ErrorException("Only absolute URIs can be used.");
    }


    // JPIP REQUEST PARSER
    JPIPRequestDecoder jpipRequestDecoder = new JPIPRequestDecoder();
    try {
      jpipRequestDecoder.decoder(requestURI);
    } catch (WarningException we) {
    }
    JPIPRequestFields jpipRequestFields =
            jpipRequestDecoder.getJPIPRequestFields();

    // Free memory
    jpipRequestDecoder = null;

    boolean isNewTarget = true;
    for (String key : clientSessionTargets.keySet()) {
      if (clientSessionTargets.get(key).getTarget().equals(jpipRequestFields.targetField.target)) {
        isNewTarget = false;
        break;
      }
    }

    ClientSessionTarget sessionTarget = null;
    if (isNewTarget) {
      requestViewWindow.fsiz[0] = requestViewWindow.fsiz[1] = 0;
      requestViewWindow.roff[0] = requestViewWindow.roff[1] = 0;
      requestViewWindow.rsiz[0] = requestViewWindow.rsiz[1] = 0;

      sessionTarget =
              new ClientSessionTarget(uri.getHost(), uri.getPort(), null, -1,
                                      jpipRequestFields.targetField.target,
                                      new ImageData(ImageData.SAMPLES_FLOAT), log);
    }

    ViewWindowField.deepCopy(jpipRequestFields.viewWindowField,
                             requestViewWindow);


    proxyMutex.suspendPrefetching();
    sessionTarget.getWindow(requestViewWindow);
    try {
      sessionTarget.getRequest(requestURI);
    } catch (WarningException e) {
      throw new ErrorException();
    } finally {
      proxyMutex.resumePrefetching();
    }


    sessionTarget.closeSession();
    sessionTarget.remove();
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param components is an one-dimension array with the component indeces.
   * @param maxTargetLength definition in {@link #maxTargetLength}
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(int[] components, long maxTargetLength) throws IllegalAccessException, ErrorException {
    if (defaultSession == null) {
      throw new IllegalArgumentException("None session has been set as default yet.");
    }
    getTarget(defaultSession, components, maxTargetLength);
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param sessionID is the unique session identifier.
   * @param components is an one-dimension array with the component indeces.
   * @param maxTargetLength definition in {@link #maxTargetLength}
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(String sessionID, int[] components, long maxTargetLength) throws IllegalAccessException, ErrorException {
    if (defaultSession == null) {
      throw new IllegalArgumentException("None session has been set as default yet.");
    }
    getTarget(sessionID, components, null, null, null, -1, 100, ViewWindowField.ROUND_DOWN, maxTargetLength);

  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param components is an one-dimension array with the component indeces.
   * @param fsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   * @param maxTargetLength definition in {@link #maxTargetLength}
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(int[] components, int[] fsiz, int[] roff, int[] rsiz, long maxTargetLength) throws IllegalAccessException, ErrorException {
    if (defaultSession == null) {
      throw new IllegalArgumentException("None session has been set as default yet.");
    }
    int maxNumLayers = clientSessionTargets.get(defaultSession).getImageData().getMaxLayers();
    getTarget(defaultSession, components, fsiz, roff, rsiz, maxNumLayers, 100, ViewWindowField.ROUND_DOWN, maxTargetLength);
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param sessionID
   * @param components is an one-dimension array with the component indeces.
   * @param fsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   * @param maxTargetLength definition in {@link #maxTargetLength}
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(String sessionID, int[] components, int[] fsiz, int[] roff, int[] rsiz, long maxTargetLength) throws IllegalAccessException, ErrorException {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }
    int maxNumLayers = clientSessionTargets.get(sessionID).getImageData().getMaxLayers();
    getTarget(sessionID, components, fsiz, roff, rsiz, maxNumLayers, 100, ViewWindowField.ROUND_DOWN, maxTargetLength);
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param components is an one-dimension array with the component indexes.
   * @param resolutionLevel is the desired resolution level of the target.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(int[] components, int discardLevels, int[] roff, int[] rsiz, int layers, int quality) throws IllegalAccessException, ErrorException {
    if (defaultSession == null) {
      throw new IllegalArgumentException("None session has been set as default yet.");
    }
    getTarget(defaultSession, components, discardLevels, roff, rsiz, layers, quality, -1);
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param sessionID
   * @param components is an one-dimension array with the component indexes.
   * @param resolutionLevel is the desired resolution level of the target.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(String sessionID, int[] components, int discardLevels, int[] roff, int[] rsiz, int layers, int quality) throws IllegalAccessException, ErrorException {
    getTarget(sessionID, components, discardLevels, roff, rsiz, layers, quality, -1);
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param comps definition in {@link CADI.Common.Network.JPIP.ViewWindowField#comps}.
   * @param resolutionLevel is the desired resolution level of the target.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(int[][] comps, int discardLevels, int[] roff, int[] rsiz, int layers, int quality) throws IllegalAccessException, ErrorException {
    if (defaultSession == null) {
      throw new IllegalArgumentException("None session has been set as default yet.");
    }

    getTarget(defaultSession, comps, discardLevels, roff, rsiz, layers, quality);
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param sessionID
   * @param comps definition in {@link CADI.Common.Network.JPIP.ViewWindowField#comps}.
   * @param resolutionLevel is the desired resolution level of the target.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(String sessionID, int[][] comps, int discardLevels, int[] roff, int[] rsiz, int layers, int quality) throws IllegalAccessException, ErrorException {

    if (sessionID == null) {
      throw new NullPointerException();
    }
    if (comps == null) {
      throw new NullPointerException();
    }
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    // Calculate num of components
    int numComps = 0;
    for (int i = 0; i < comps.length; i++) {
      numComps += comps[i][1] - comps[i][0] + 1;
    }

    // Convert from range of components to array of components
    int[] components = new int[numComps];
    int index = 0;
    for (int i = 0; i < comps.length; i++) {
      for (int c = comps[i][0]; c <= comps[i][1]; c++) {
        components[index++] = c;
      }
    }

    getTarget(sessionID, components, discardLevels, roff, rsiz, layers, quality);
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param components is an one-dimension array with the component indeces.
   * @param resolutionLevel is the desired resolution level of the target.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   * @param maxTargetLength definition in {@link #maxTargetLength}
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(int[] components, int discardLevels, int[] roff, int[] rsiz, int layers, int quality, long maxTargetLength) throws IllegalAccessException, ErrorException {

    if (defaultSession == null) {
      throw new IllegalArgumentException("None session has been set as default yet.");
    }

    getTarget(defaultSession, components, discardLevels, roff, rsiz, layers, quality, maxTargetLength);
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param components is an one-dimension array with the component indeces.
   * @param resolutionLevel is the desired resolution level of the target.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   * @param maxTargetLength definition in {@link #maxTargetLength}
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(String sessionID, int[] components, int discardLevels, int[] roff, int[] rsiz, int layers, int quality, long maxTargetLength) throws IllegalAccessException, ErrorException {

    if (sessionID == null) {
      throw new NullPointerException();
    }
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    if (discardLevels < 0) {
      throw new ErrorException("The number of discard levels must be greater than or equal to 0");
    }

    if (defaultSession == null) {
      throw new IllegalArgumentException("None session has been set as default yet.");
    }

    proxyMutex.suspendPrefetching();
    clientSessionTargets.get(sessionID).getWindow(components, discardLevels, roff, rsiz, layers, quality, maxTargetLength);
    proxyMutex.resumePrefetching();
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param comps definition in {@link CADI.Common.Network.JPIP.ViewWindowField#comps}.
   * @param discardLevels is the desired number of levels to be discard in the wavelet transform.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   * @param maxTargetLength definition in {@link #maxTargetLength}
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(int[][] comps, int discardLevels, int[] roff, int[] rsiz, int layers, int quality, long maxTargetLength) throws IllegalAccessException, ErrorException {
    if (defaultSession == null) {
      throw new IllegalArgumentException("None session has been set as default yet.");
    }

    getTarget(defaultSession, comps, discardLevels, roff, rsiz, layers, quality, maxTargetLength);
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param sessionID
   * @param comps definition in {@link CADI.Common.Network.JPIP.ViewWindowField#comps}.
   * @param discardLevels is the desired number of levels to be discard in the wavelet transform.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   * @param maxTargetLength definition in {@link #maxTargetLength}
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(String sessionID, int[][] comps, int discardLevels, int[] roff, int[] rsiz, int layers, int quality, long maxTargetLength) throws IllegalAccessException, ErrorException {

    if (sessionID == null) {
      throw new NullPointerException();
    }
    if (comps == null) {
      throw new NullPointerException();
    }
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    // Calculate num of components
    int numComps = 0;
    for (int i = 0; i < comps.length; i++) {
      numComps += comps[i][1] - comps[i][0] + 1;
    }

    // Convert from range of components to array of components
    int[] components = new int[numComps];
    int index = 0;
    for (int i = 0; i < comps.length; i++) {
      for (int c = comps[i][0]; c <= comps[i][1]; c++) {
        components[index++] = c;
      }
    }

    getTarget(sessionID, components, discardLevels, roff, rsiz, layers, quality, maxTargetLength);
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param components is an one-dimension array with the component indexes.
   * @param fsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(int[] components, int[] fsiz, int[] roff, int[] rsiz, int layers, int quality, int round) throws IllegalAccessException, ErrorException {
    if (defaultSession == null) {
      throw new IllegalArgumentException("None session has been set as default yet.");
    }
    getTarget(defaultSession, components, fsiz, roff, rsiz, layers, quality, round, -1);
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param components is an one-dimension array with the component indeces.
   * @param fsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(String sessionID, int[] components, int[] fsiz, int[] roff, int[] rsiz, int layers, int quality, int round) throws IllegalAccessException, ErrorException {
    getTarget(sessionID, components, fsiz, roff, rsiz, layers, quality, round, -1);
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param comps definition in {@link CADI.Common.Network.JPIP.ViewWindowField#comps}.
   * @param fsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(int[][] comps, int[] fsiz, int[] roff, int[] rsiz, int layers, int quality, int round) throws IllegalAccessException, ErrorException {

    if (defaultSession == null) {
      throw new IllegalArgumentException("None session has been set as default yet.");
    }

    getTarget(defaultSession, comps, fsiz, roff, rsiz, layers, quality, round);
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param sessionID
   * @param comps definition in {@link CADI.Common.Network.JPIP.ViewWindowField#comps}.
   * @param fsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(String sessionID, int[][] comps, int[] fsiz, int[] roff, int[] rsiz, int layers, int quality, int round) throws IllegalAccessException, ErrorException {

    if (sessionID == null) {
      throw new NullPointerException();
    }
    if (comps == null) {
      throw new NullPointerException();
    }
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    // Calculate num of components
    int numComps = 0;
    for (int i = 0; i < comps.length; i++) {
      numComps += comps[i][1] - comps[i][0] + 1;
    }

    // Convert from range of components to array of components
    int[] components = new int[numComps];
    int index = 0;
    for (int i = 0; i < comps.length; i++) {
      for (int c = comps[i][0]; c <= comps[i][1]; c++) {
        components[index++] = c;
      }
    }

    getTarget(sessionID, components, fsiz, roff, rsiz, layers, quality, round);
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param components is an one-dimension array with the component indexes.
   * @param fsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   * @param maxTargetLength definition in {@link #maxTargetLength}
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(int[] components, int[] fsiz, int[] roff, int[] rsiz, int layers, int quality, int round, long maxTargetLength) throws IllegalAccessException, ErrorException {

    if (defaultSession == null) {
      throw new IllegalArgumentException("None session has been set as default yet.");
    }

    getTarget(defaultSession, components, fsiz, roff, rsiz, layers, quality, round, maxTargetLength);
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param sessionID
   * @param components is an one-dimension array with the component indexes.
   * @param fsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   * @param maxTargetLength definition in {@link #maxTargetLength}
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(String sessionID, int[] components, int[] fsiz, int[] roff, int[] rsiz, int layers, int quality, int round, long maxTargetLength) throws IllegalAccessException, ErrorException {

    if (sessionID == null) {
      throw new NullPointerException();
    }
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    // Check parameters
    if ((quality < 0) || (quality > 100)) {
      throw new IllegalArgumentException();
    }
    if (!((round == ViewWindowField.ROUND_DOWN) || (round == ViewWindowField.ROUND_UP) || (round == ViewWindowField.CLOSEST))) {
      throw new IllegalArgumentException();
    }
    if ((fsiz == null) && (roff != null)) {
      throw new IllegalArgumentException();
    }
    if ((fsiz == null) && (rsiz != null)) {
      throw new IllegalArgumentException();
    }
    if (fsiz != null) {
      if ((fsiz[0] <= 0) || (fsiz[1] <= 0)) {
        throw new IllegalArgumentException();
      }
    }
    if (roff != null) {
      if ((roff.length != 2) || (roff[0] < 0) || (roff[1] < 0)) {
        throw new IllegalArgumentException();
      }
    }
    if (rsiz != null) {
      if ((rsiz.length != 2) || (rsiz[0] <= 0) || (rsiz[1] <= 0)) {
        throw new IllegalArgumentException();
      }
    }


    initTime = System.currentTimeMillis();
    initStageTime = initTime;

    // Set the view window
    requestViewWindow.reset();

    if (fsiz != null) {
      requestViewWindow.fsiz[0] = fsiz[0];
      requestViewWindow.fsiz[1] = fsiz[1];
      requestViewWindow.roundDirection = round;
    }

    // Region offset
    if (roff == null) {
      requestViewWindow.roff[0] = 0;
      requestViewWindow.roff[1] = 0;
    } else {
      requestViewWindow.roff[0] = roff[0];
      requestViewWindow.roff[1] = roff[1];
    }

    // Region size
    if (rsiz == null) {
      if (fsiz != null) {
        requestViewWindow.rsiz[0] = fsiz[0];
        requestViewWindow.rsiz[1] = fsiz[1];
      }
    } else {
      requestViewWindow.rsiz[0] = (rsiz[0] < requestViewWindow.fsiz[0]) ? rsiz[0] : requestViewWindow.fsiz[0];
      requestViewWindow.rsiz[1] = (rsiz[1] < requestViewWindow.fsiz[1]) ? rsiz[1] : requestViewWindow.fsiz[1];
    }

    // Components
    if (components != null) {
      requestViewWindow.comps = ArraysUtil.indexesToRanges(components);
    }

    // Layers
    if (layers > 0) {
      requestViewWindow.layers = layers;
    }

    // Log
    if (log.isLog(CADILog.LEVEL_INFO)) {
      log.logInfo(requestViewWindow.toString());
    }

    clientSessionTargets.get(sessionID).setMaxTargetLength(maxTargetLength);
    proxyMutex.suspendPrefetching();
    clientSessionTargets.get(sessionID).getWindow(requestViewWindow);
    proxyMutex.resumePrefetching();
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param comps definition in {@link CADI.Common.Network.JPIP.ViewWindowField#comps}.
   * @param fsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   * @param maxTargetLength definition in {@link #maxTargetLength}
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(int[][] comps, int[] fsiz, int[] roff, int[] rsiz, int layers, int quality, int round, long maxTargetLength) throws IllegalAccessException, ErrorException {
    if (defaultSession == null) {
      throw new IllegalArgumentException("None session has been set as default yet.");
    }

    getTarget(defaultSession, comps, fsiz, roff, rsiz, layers, quality, round, maxTargetLength);
  }

  /**
   * This method fetches a logical target defined by the method input
   * parameters.
   *
   * @param sessionID
   * @param comps definition in {@link CADI.Common.Network.JPIP.ViewWindowField#comps}.
   * @param fsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param layers definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
   * @param quality definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   * @param maxTargetLength definition in {@link #maxTargetLength}
   *
   * @throws IllegalAccessException if the method is called before the
   * 			{@link #setServerAndTarget(String, int, String)}.
   * @throws ErrorException if the target (
   * <code>requestURI</code>) cannot be
   * fetched from the server, or the delivery data cannot be
   * decompressed correctly.
   */
  public void getTarget(String sessionID, int[][] comps, int[] fsiz, int[] roff, int[] rsiz, int layers, int quality, int round, long maxTargetLength) throws IllegalAccessException, ErrorException {
    if (sessionID == null) {
      throw new NullPointerException();
    }
    if (comps == null) {
      throw new NullPointerException();
    }
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    // Calculate number of components
    int numComps = 0;
    for (int i = 0; i < comps.length; i++) {
      numComps += comps[i][1] - comps[i][0] + 1;
    }

    // Convert from range of components to array of components
    int[] components = new int[numComps];
    int index = 0;
    for (int i = 0; i < comps.length; i++) {
      for (int c = comps[i][0]; c <= comps[i][1]; c++) {
        components[index++] = c;
      }
    }

    getTarget(sessionID, components, fsiz, roff, rsiz, layers, quality, round, maxTargetLength);
  }

  /**
   * Indicates whether sessions will be used in the JPIP connections.
   *
   * @param useSession
   * <code>true</code> if sessions will be used.
   * <code>
   * 		false</code> if it will not.
   */
  public void setUseSession(boolean useSession) {
    this.useSession = useSession;
  }

  /**
   * Sets the {@link #keepAlive} attribute.
   *
   * @param keepAlive see {@link #keepAlive}.
   */
  public void setKeepAlive(boolean keepAlive) {
    this.keepAlive = keepAlive;
  }

  /**
   * Sets the file name where the log will be done. If the
   * <code>logFileName
   * </code> parameter is null, logs are printed out the default output in
   * plain text.
   *
   * @param logFileName file name of the log file.
   */
  public final void setLogFile(String logFileName) {

    if (logFileName != null) {
      int posFileSeparator = logFileName.lastIndexOf(File.separator);

      if (posFileSeparator < 0) {
        String userDir = System.getProperty("user.dir");
        logFileName = userDir + File.separator + logFileName;
      }
    }

    log.setParameters(logFileName, false);
  }

  /**
   * Sets the file name where the log will be done. If the
   * <code>logFileName
   * </code> parameter is null, logs are printed out the default output.
   *
   * @param logFileName file name of the log file.
   * @param useXMLFormat if
   * <code>true</code> logs are printed out following
   * a XML format. Otherwise, plain text is used.
   */
  public final void setLogFile(String logFileName, boolean useXMLFormat) {

    if (logFileName != null) {
      int posFileSeparator = logFileName.lastIndexOf(File.separator);

      if (posFileSeparator < 0) {
        String userDir = System.getProperty("user.dir");
        logFileName = userDir + File.separator + logFileName;
      }
    }

    log.setParameters(logFileName, useXMLFormat);
  }

  /**
   * Sets the log enabled or disabled.
   *
   * @param enabled if
   * <code>true</code>, logs are enabled.
   * Otherwise, logs are disabled.
   */
  public final void setLogEnabled(boolean enabled) {
    log.setEnabled(enabled);
  }

  /**
   * Sets the log level.
   *
   * @param logLevel
   */
  public final void setLogLevel(int logLevel) {
    log.setLogLevel(logLevel);
  }

  /**
   * This method returns a detailed description of the last decoded logical
   * target. This description is delivery as a html table with the key in the
   * first column and the value in the second one. If none image has been
   * decoded, a
   * <code>null</code> pointer is returned.
   *
   * @return an HTML table description of the logical target. If none logical
   * target has been decoded yet, a
   * <code>null</code> pointer is
   * returned.
   */
  public final String getTargetDescription() {
    return getTargetDescription(defaultSession);
  }

  /**
   * This method returns a detailed description of the last decoded logical
   * target. This description is delivery as a html table with the key in the
   * first column and the value in the second one. If none image has been
   * decoded, a
   * <code>null</code> pointer is returned.
   *
   * @param sessionID
   *
   * @return an HTML table description of the logical target. If none logical
   * target has been decoded yet, a
   * <code>null</code> pointer is
   * returned.
   */
  public String getTargetDescription(String sessionID) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    return clientSessionTargets.get(sessionID).getTargetDescription();
  }

  /**
   * Returns the last requested URI.
   *
   * @return the last requested URI.
   */
  public final String getURI() {
    return getURI(defaultSession);
  }

  /**
   * Returns the last requested URI.
   *
   * @param sessionID
   *
   * @return the last requested URI.
   */
  public String getURI(String sessionID) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    return clientSessionTargets.get(sessionID).getRequestedURI();
  }

  /**
   * Return the sever name of the latest connection.
   *
   * @return server name of the latest connection.
   */
  public final String getServer() {
    return getServer(defaultSession);
  }

  /**
   * Return the sever name of the latest connection.
   *
   * @return server name of the latest connection.
   */
  public final String getServer(String sessionID) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    return clientSessionTargets.get(sessionID).getServer();
  }

  /**
   * Return the port number of the server.
   *
   * @return the port number.
   */
  public final int getPort() {
    return getPort(defaultSession);
  }

  /**
   * Return the port number of the server.
   *
   * @return the port number.
   */
  public final int getPort(String sessionID) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    return clientSessionTargets.get(sessionID).getPort();
  }

  /**
   * Return the latest requested target name.
   *
   * @return the latest requested target name.
   */
  public String getTargetName() {
    return getTargetName(defaultSession);
  }

  /**
   * Return the latest requested target name.
   *
   * @param sessionID
   *
   * @return the latest requested target name.
   */
  public String getTargetName(String sessionID) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    return clientSessionTargets.get(sessionID).getTarget();
  }

  /**
   * Returns the download's speed average.
   *
   * @return the download's speed average.
   */
  public float getSpeed() {
    return getSpeed(defaultSession);
  }

  /**
   * Returns the download's speed average.
   *
   * @param sessionID
   *
   * @return the download's speed average.
   */
  public float getSpeed(String sessionID) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    return clientSessionTargets.get(sessionID).getSpeed();
  }

  /**
   * Returns the total bytes downloadeded bytes for the actual logical target.
   *
   * @return the total downloaded bytes.
   */
  public long getDownloadedBytes() {
    return getDownloadedBytes(defaultSession);
  }

  /**
   * Returns the total bytes downloaded bytes for the actual logical target.
   *
   * @param sessionID
   *
   * @return the total downloaded bytes.
   */
  public long getDownloadedBytes(String sessionID) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    return clientSessionTargets.get(sessionID).getDownloadedBytes();
  }

  /**
   * Sets the JPIP message counter to 0.
   */
  public void resetJPIPMessagesCounters() {
    resetJPIPMessagesCounters(defaultSession);
  }

  /**
   * Sets the JPIP message counter to 0.
   *
   * @param sessionID
   */
  public void resetJPIPMessagesCounters(String sessionID) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    clientSessionTargets.get(sessionID).resetJPIPMessagesCounters();
  }

  /**
   * Returns the {@link #bytesJPIPMessageHeader} attribute.
   *
   * @return the {@link #bytesJPIPMessageHeader} attribute.
   */
  public long getBytesJPIPMessageHeader() {
    return getBytesJPIPMessageHeader(defaultSession);
  }

  /**
   * Returns the {@link #bytesJPIPMessageHeader} attribute.
   *
   * @param sessionID
   *
   * @return the {@link #bytesJPIPMessageHeader} attribute.
   */
  public long getBytesJPIPMessageHeader(String sessionID) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    return clientSessionTargets.get(sessionID).getBytesJPIPMessageHeader();
  }

  /**
   * Returns the {@link #bytesJPIPMessageBody} attribute.
   *
   * @return the {@link #bytesJPIPMessageBody} attribute.
   */
  public long getBytesJPIPMessageBody() {
    return getBytesJPIPMessageBody(defaultSession);
  }

  /**
   * Returns the {@link #bytesJPIPMessageBody} attribute.
   *
   * @param sessionID
   *
   * @return the {@link #bytesJPIPMessageBody} attribute.
   */
  public long getBytesJPIPMessageBody(String sessionID) {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    return clientSessionTargets.get(sessionID).getBytesJPIPMessageBody();
  }

  /**
   * Returns the {@link #keepAlive} attribute.
   *
   * @return the {@link #keepAlive} attribute.
   */
  public boolean isKeepAlive() {
    return keepAlive;
  }

  /**
   * Returns the {@link #useSession} attribute.
   *
   * @return the {@link #useSession} attribute.
   */
  public boolean isUseSession() {
    return useSession;
  }

  /**
   * Returns the {@link #useHTTPSession} attribute.
   *
   * @return the {@link #useHTTPSession} attribute.
   */
  public boolean isUseHTTPSession() {
    return useSession;
  }

  /**
   * Returns the {@link #useHTTPTCPSession} attribute
   *
   * @return the {@link #useHTTPTCPSession} attribute.
   */
  public boolean isUseHTTPTCPSession() {
    return useSession;
  }

  /**
   * Definition in {@link CADI.Client.Cache.ClientCache#getManagementPolicy()}
   *
   * @return definition in {@link CADI.Client.Cache.ClientCache#getManagementPolicy()}
   */
  public int getManagementPolicy() {
    return getManagementPolicy(defaultSession);
  }

  /**
   * Definition in {@link CADI.Client.Cache.ClientCache#getManagementPolicy()}
   *
   * @param sessionID
   *
   * @return definition in {@link CADI.Client.Cache.ClientCache#getManagementPolicy()}
   */
  public int getManagementPolicy(String sessionID) {
    return clientSessionTargets.containsKey(sessionID)
            ? clientSessionTargets.get(sessionID).getManagementPolicy()
            : -1;
  }

  /**
   * Definition in {@link CADI.Client.Cache.ClientCache#getMaxCacheSize()}
   *
   * @return definition in {@link CADI.Client.Cache.ClientCache#getMaxCacheSize()}
   */
  public long getMaxCacheSize() {
    return getMaxCacheSize(defaultSession);
  }

  /**
   * Definition in {@link CADI.Client.Cache.ClientCache#getMaxCacheSize()}
   *
   * @param sessionID
   *
   * @return definition in {@link CADI.Client.Cache.ClientCache#getMaxCacheSize()}
   */
  public long getMaxCacheSize(String sessionID) {
    return clientSessionTargets.containsKey(sessionID)
            ? clientSessionTargets.get(sessionID).getMaxCacheSize()
            : -1L;
  }

  /**
   * Definition in {@link CADI.Common.Log.CADILog#isEnabled()}.
   *
   * @return definition in {@link CADI.Common.Log.CADILog#isEnabled()}.
   */
  public boolean isLogEnabled() {
    return log.isEnabled();
  }

  /**
   * Definition in {@link CADI.Common.Log.CADILog#useXMLFormat()}.
   *
   * @return definition in {@link CADI.Common.Log.CADILog#useXMLFormat()}.
   */
  public boolean useLogXMLFormat() {
    return log.useXMLFormat();
  }

  /**
   * Definition in {@link CADI.Common.Log.CADILog#getFileName()}.
   *
   * @return definition in {@link CADI.Common.Log.CADILog#getFileName()}.
   */
  public String getLogFileName() {
    return log.getFileName();
  }

  /**
   * Definition in {@link CADI.Common.Log.CADILog#getLogLevel()}.
   *
   * @return definition in {@link CADI.Common.Log.CADILog#getLogLevel()}.
   */
  public int getLogLevel() {
    return log.getLogLevel();
  }

  /**
   * Returns the JPIP-stream media type with is beind used. Allowed values
   * are:
   * 1 - for JPP-stream
   * 2 - for JPT-stream
   *
   * @return
   */
  @Deprecated
  public int getJPIPStreamType() {
    return 1;
  }

  /**
   *
   * @param fileName
   *
   * @throws ErrorException
   */
  public void saveCache(String fileName) throws ErrorException {
    saveCache(defaultSession, fileName);
  }

  /**
   *
   * @param fileName
   *
   * @throws ErrorException
   */
  public void saveCache(String sessionID, String fileName) throws ErrorException {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    clientSessionTargets.get(sessionID).saveCache(fileName);
  }

  /**
   *
   * @param fileName
   *
   * @throws ErrorException
   */
  public void loadCache(String fileName) throws ErrorException {
    loadCache(defaultSession, fileName);
  }

  /**
   *
   * @param fileName
   *
   * @throws ErrorException
   */
  public void loadCache(String sessionID, String fileName) throws ErrorException {
    if (!clientSessionTargets.containsKey(sessionID)) {
      throw new IllegalArgumentException("There is none session with the id \"+sessionID+\"");
    }

    clientSessionTargets.get(sessionID).loadCache(fileName);
  }

  /**
   * This method sets the HTTP's user agent header.
   *
   * @param userAgent
   */
  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
    clientSessionTargets.get(defaultSession).setUserAgent(userAgent);
  }

  /**
   * This method sets the HTTP's user agent header.
   *
   * @param userAgent
   */
  public void setDebug(String debug) {
    this.debug = debug;
    clientSessionTargets.get(defaultSession).setDebug(debug);
  }

  /**
   * Starts the prefetching strategy, if it had been stopped.
   */
  public void startPrefetching() {
    prefetching.start();
  }

  /**
   * Stops the prefetching module.
   */
  public void stopPrefetching() {
    prefetching.finish();
    try {
      prefetching.join();
    } catch (InterruptedException ex) {
      Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    return str;
  }

  /**
   * Prints this Client fields out to the specified output stream. This
   * method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Client --");

    out.println("<<< Not implemented yet >>> ");

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * Show some time and memory usage statisticals.
   *
   * @param stage
   * string that will be displayed
   */
  private void showTimeMemory(String stage) {

    long actualTime = System.currentTimeMillis();
    if (initTime == 0) {
      initTime = actualTime;
    }

    // print times are not considered
    long totalMemory = Runtime.getRuntime().totalMemory() / 1048576;
    long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
    // ((float) initTime - (float) actualTime)
    String durationStage = Float.toString((actualTime - initStageTime) / 1000F) + "000";
    durationStage = durationStage.substring(0, durationStage.lastIndexOf(".") + 4);
    String duration = Float.toString((actualTime - initTime) / 1000F) + "000";
    duration = duration.substring(0, duration.lastIndexOf(".") + 4);

    String str = "\nSTAGE: " + stage + "\n";
    str += "  Memory (USED/TOTAL): " + usedMemory + "/" + totalMemory + " MB\n";
    str += "  Time (USED/TOTAL)  : " + durationStage + "/" + duration + " secs\n";
    log.logInfo(str);

    initStageTime = System.currentTimeMillis();

  }
}
