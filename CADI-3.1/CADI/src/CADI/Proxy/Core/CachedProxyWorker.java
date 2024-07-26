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
package CADI.Proxy.Core;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;

import CADI.Common.Cache.DataBin;
import CADI.Common.Cache.MainHeaderDataBin;
import CADI.Common.Cache.PrecinctDataBin;
import CADI.Common.Cache.TileHeaderDataBin;
import CADI.Common.Log.CADILog;
import CADI.Common.LogicalTarget.JPEG2000.File.ReadPredictiveModel;
import CADI.Common.LogicalTarget.JPEG2000.PredictiveScalingFactors;
import CADI.Common.LogicalTarget.JPEG2000.RelevantPrecinct;
import CADI.Common.Network.HTTP.HTTPResponse;
import CADI.Common.Network.HTTP.StatusCodes;
import CADI.Common.Network.JPIP.EORCodes;
import CADI.Common.Network.JPIP.JPIPMessageHeader;
import CADI.Common.Network.JPIP.JPIPRequestFields;
import CADI.Common.Network.JPIP.JPIPResponseFields;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Common.Util.StopWatch;
import CADI.Proxy.Client.ProxyCacheManagement;
import CADI.Proxy.Client.ProxySessionTarget;
import CADI.Proxy.Client.ProxySessionTargets;
import CADI.Proxy.LogicalTarget.JPEG2000.ProxyJPEG2KCodestream;
import CADI.Proxy.LogicalTarget.JPEG2000.ProxyWindowScalingFactor;
import CADI.Proxy.Server.ProxyCacheModel;
import CADI.Proxy.Server.ProxyClientSession;
import CADI.Proxy.Server.ProxyClientSessions;
import CADI.Server.ServerDefaultValues;
import CADI.Server.Network.HTTPResponseSender;
import CADI.Common.Network.JPIP.JPIPMessageEncoder;
import CADI.Common.Network.JPIP.JPIPRequestDecoder;
import CADI.Server.Request.RequestQueue;
import CADI.Server.Request.RequestQueueNode;
import GiciException.ErrorException;
import GiciException.WarningException;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 *
 * <p>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; setParameters<br>
 * &nbsp; run<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.2 2012/03/09
 */
public class CachedProxyWorker extends ProxyWorker implements StatusCodes, EORCodes {

  /**
   * Definition in {@link CADI.Proxy.Proxy#logicalTargetList}.
   */
  private ProxySessionTargets proxySessionTargets = null;

  /**
   * Definition in {@link CADI.Server.Core.Scheduler#clientSessions}.
   */
  private ProxyClientSessions listOfClientSessions = null;

  /**
   * Definition in {@link CADI.Proxy.Proxy#proxyMutex}
   */
  private ProxyPrefSemaphore proxyMutex = null;

  // INTERNAL ATTRIBUTES
  /**
   * This attribute is only for debugging and experimental purposes.
   */
  private String clientUserAgent = "CADI Proxy";

  /**
   *
   */
  private ProxySessionTarget proxySessionTarget = null;

  /**
   * Contains the session object where data about the actual session is
   * saved.
   */
  private ProxyClientSession proxySession = null;

  /**
   * Definition in {@link CADI.Common.Network.JPIP.ChannelField#cid}.
   */
  private String cid = null;

  /**
   *
   */
  private ProxyCacheModel proxyCacheModel = null;

  /**
   * Indicates whether the actual request is a stateles request or a request
   * with a session.
   */
  private boolean isStatelessRequest = true;

  /**
   *
   */
  private JPIPRequestFields jpipRequestFields = null;

  /**
   *
   */
  private ViewWindowField responseViewWindow = null;

  private ViewWindowField actualViewWindow = null;

  /**
   *
   */
  private ProxyJPEG2KCodestream codestream = null;

  /**
   *
   */
  private JPIPMessageEncoder jpipMessageEncoder = null;

  /**
   *
   */
  private int cacheDescriptorType = ProxyCacheManagement.EXPLICIT_FORM;

  /**
   *
   */
  private int cacheDescriptorSubType = ProxyCacheManagement.NUMBER_OF_BYTES;

  /**
   * Definition in {@link CADI.Proxy.Proxy#predictiveModel}.
   */
  private String predictiveModel = null;

  /**
   *
   */
  private FetchTarget fetchTarget = null;

  private Thread auxThread = null;

  /**
   *
   */
  private static String channelFieldPath = "CADIServer";

  /**
   *
   */
  private StopWatch stopWatch = null;

  private int EORReasonCode = -1;

  private String EORReasonMessage = null;

  private boolean useExtendedHeaders = false;

  private String proxyVia = null;

  String userAgent = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param threadName
   * @param requestQueue
   * @param proxyCacheLogicalTargets
   * @param proxyRequestedWOIs
   * @param proxyMutex
   * @param log
   */
  public CachedProxyWorker(String threadName, RequestQueue requestQueue,
                           ProxySessionTargets proxySessionTargets,
                           ProxyClientSessions listOfClientSessions,
                           ProxyPrefSemaphore proxyMutex, CADILog log) {

    super(threadName, requestQueue, log);

    // Check input parameters
    if (proxySessionTargets == null) {
      throw new NullPointerException();
    }
    if (listOfClientSessions == null) {
      throw new NullPointerException();
    }
    if (proxyMutex == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.listOfClientSessions = listOfClientSessions;
    this.proxySessionTargets = proxySessionTargets;
    this.proxyMutex = proxyMutex;

    // Initializations
    stopWatch = new StopWatch();
    jpipMessageEncoder = new JPIPMessageEncoder(true);


    String hostName = null;
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      hostName = "localhost";
    }

    String version = null;
    try {
      Properties cadiInfo = new Properties();
      cadiInfo.load(getClass().getClassLoader().getResourceAsStream("CADI/Common/Info/cadiInfo.properties"));
      version = cadiInfo.getProperty("version");
    } catch (Exception e) {
    }

    fetchTarget = new FetchTarget(log);
    proxyVia = "JPIP 1.0 " + hostName + " (CADIProxy v" + version + ")";
  }

  /**
   * Sets the {@link #clientUserAgent} attribute.
   *
   * @param clientUserAgent definition in {@link #clientUserAgent}.
   */
  public void setClientUserAgent(String clientUserAgent) {
    this.clientUserAgent = clientUserAgent;
  }

  /**
   * 
   * @param predModel 
   */
  public void setPredictiveModel(String predModel) {
    this.predictiveModel = predModel;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {

    boolean keepAlive;

    log.logInfo(getName() + ": started");

    // Main loop
    while (!finish) {

      busy = false;
      keepAlive = true;

      // Thread is sleeping until a new work is added to the queue
      RequestQueueNode requestNode = null;
      try {
        requestNode = requestQueue.get();
      } catch (InterruptedException ie) {
        continue;
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
        // Frees resources and goes to sleep
        log.logInfo(getName() + ": error opening output stream");

        try {
          socket.shutdownInput();
          socket.shutdownOutput();
          socket.close();
          os = null;
          httpResponseSender = null;
          socket = null;
        } catch (IOException ioe) {
        }

        // Goes to sleep
        continue;
      }

      while (keepAlive && !finish) {

        proxyMutex.suspendPrefetching();

        // Process the client request
        try {
          processRequest();
        } catch (WarningException e) {
          break;
        } catch (ErrorException e) {
          break;
        } catch (IOException e) {
          break;
        } finally {
          proxyMutex.resumePrefetching();
        }

        //	Check if keep-alive mode is set
        String value = httpRequest.getHeaderField("Connection");
        if ((value != null) && !value.equals("keep-alive")) {
          // HTTP keep-alive mode is not set. Thread goes to sleep
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
            break;
          } catch (ErrorException ee) {
            // <<<<<<<<<< to log file
            keepAlive = false;
          }
        }
      }

      // Frees resources and goes to sleep
      try {
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
        os = null;
        httpResponseSender = null;
        socket = null;
      } catch (IOException ioe) {
      }


      // Thread is sleeping until the scheduler sends a signal
      log.logInfo(getName() + ": going to sleep");

    } // while (!finish)

    busy = false;
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

    out.println("-- Cached Proxy Worker --");
    super.list(out);
    out.flush();
  }

  // ============================ private methods ==============================
  /**
   *
   * @throws ErrorException
   * @throws WarningException
   * @throws IOException
   */
  private void processRequest() throws ErrorException, WarningException, IOException {

    //System.out.println(clientUserAgent+" ----------------------\n"
    // + clientUserAgent
    // + " Time stamp: "+System.currentTimeMillis()+"\n"
    // + clientUserAgent+" Title: "+getName()+"\n"
    // + clientUserAgent+" Client: "+clientUserAgent+"\n"
    // + clientUserAgent+" Request: "+httpRequest.toString()+"\n"
    // + clientUserAgent+" ----------------------\n"
    // );

    //System.out.println();
    //System.out.println("===================================================================================");
    //httpRequest.list(System.out);

    // INITIALIZATIONS
    httpResponse = new HTTPResponse();
    //httpResponseSender = new HTTPResponseSender(socket.getOutputStream());
    //httpResponseSender.setMaxTxRate(maxTxRate);
    jpipResponseFields = new JPIPResponseFields();

    // GET SERVER AND PORT
    URI uri = httpRequest.getURI();
    String server = uri.getHost();
    int port = uri.getPort();
    if (port < 0) {
      port = ServerDefaultValues.PORT;
    }

    // JPIP REQUEST PARSER
    JPIPRequestDecoder jpipRequestDecoder = new JPIPRequestDecoder();
    try {
      jpipRequestDecoder.decoder(httpRequest.getRequestURI());
    } catch (WarningException we) {
      // Send an error response
      sendHTTPResponseError(we.getErrorCode(), we.getMessage());
      return;
    }
    jpipRequestFields = jpipRequestDecoder.getJPIPRequestFields();
    //jpipRequestFields.list(System.out);

    // CHECK JPIP PARAMETERS RESTRICTIONS
    checkJPIPParameters();

    long maxRate = maxTxRate;
    if (jpipRequestFields.clientCapPrefField.pref.maxBandwidth > 0) {
      if (maxRate == 0) {
        maxRate = jpipRequestFields.clientCapPrefField.pref.maxBandwidth;
      } else if (maxRate > jpipRequestFields.clientCapPrefField.pref.maxBandwidth) {
        maxRate = jpipRequestFields.clientCapPrefField.pref.maxBandwidth;
      }
    }
    httpResponseSender.setMaxTxRate(maxRate, trafficShaping);

    userAgent = httpRequest.getHeaderField("User-Agent");
    //System.out.println("\nUSER: "+userAgent+jpipRequestFields.toString());

    // MANAGE LOGICAL TARGET
    if (jpipRequestFields.channelField.cid != null) {
      proxySession = listOfClientSessions.get(jpipRequestFields.channelField.cid);
      if (proxySession != null) {
        jpipRequestFields.targetField.tid =
                proxySession.getTID(jpipRequestFields.channelField.cid);
      }
    }
    logicalTargetsManager(jpipRequestFields.targetField.target, jpipRequestFields.targetField.tid);
    //proxySessionTarget.list(System.out);
    //logicalTarget.list(System.out);


    // MANAGE SESSION
    sessionsManager();

    // MANAGE CACHE
    proxyCacheModel = proxySession.getCache(cid);
    proxyCacheModel.update(jpipRequestFields.cacheManagementField.model, proxySessionTarget.getCacheManagement());

    // RESPONSE TYPE
    useExtendedHeaders = false;
    for (String type : jpipRequestFields.serverControlField.type) {
      if (type.contains("jpp-stream")) {
        // Use extended headers
        if (type.contains("jpp-stream;ptype=ext")) {
          useExtendedHeaders = true;
        }
      }
    }

    // ADJUST REQUESTED VIEW WINDOW
    codestream = proxySessionTarget.getCodestream();
    if (codestream == null) {
      throw new ErrorException("An internal error has occurred.");
    }

    try {
      processViewWindow(jpipRequestFields.viewWindowField);
    } catch (ErrorException e2) {
      sendHTTPResponseError(BAD_REQUEST, "The requested WOI has some parameters which is not correct");
      throw new WarningException();
    }


    // SEND HTTP RESPONSE HEADER

    httpResponse.setResponseCode(StatusCodes.OK);
    httpResponse.setResponseMessage("OK");
    httpResponse.setHeaderField("Transfer-Encoding", "chunked");
    httpResponse.setHeaderField("Content-Type", "image/jpp-stream");
    httpResponse.setHeaderField("Via", proxyVia);
    jpipResponseFields.type = "jpp-stream";
    jpipResponseFields.tid = proxySessionTarget.getProxyTID();
    jpipResponseFields.setViewWindow(responseViewWindow);

    sendHTTPResponseHeader(httpResponse);
    //httpResponse.list(System.out); // DEBUG

    // Send meta-data bin 0
    if (proxyCacheModel.getMainHeaderLength() <= 0) {
      //System.out.println("==> - Sending meta-data bin 0");
      addMetadaBin0(httpResponseSender, jpipMessageEncoder);
    }


    // SEND AVAILABLE DATA & REQUEST FOR THE REMAINING DATA TO THE SERVER


    ProxyWindowScalingFactor pwsf = new ProxyWindowScalingFactor(proxySessionTarget.getJP2KProxyLogicalTarget(), proxyCacheModel);
    pwsf.runResponseParameters(actualViewWindow);
    //actualViewWindow.list(System.out);
    ArrayList<RelevantPrecinct> relevantPrecincts =
            pwsf.runResponseData(jpipRequestFields.dataLimitField.len);

    ArrayList<SendDataInfo> availableData = new ArrayList<SendDataInfo>();
    ArrayList<SendDataInfo> unAvailableData = new ArrayList<SendDataInfo>();
    proxySessionTarget.lock();
    proxySessionTarget.checkAvailableData(actualViewWindow, relevantPrecincts,
            proxyCacheModel,
            availableData, unAvailableData);
    proxySessionTarget.unlock();

    if (unAvailableData.size() > 0) {
      // Request the remainder data to the server
      fetchTarget.initialize(proxySessionTarget, actualViewWindow,
              clientUserAgent + "-" + httpRequest.getHeaderField("User-Agent"),
              httpRequest.getHeaderField("Debug") != null ? httpRequest.getHeaderField("Debug") : null);

      // TODO: create a new thread each time is inefficient.
      auxThread = new Thread(fetchTarget);
      auxThread.setName(getName() + "_fetcher");
      auxThread.start();
      //cacheManagement.list(System.out); // DEBUG
    }

    // Send data which is available in the proxy's cache
    if (availableData.size() > 0) {
      cumMessageHeadersLength = 0;
      cumMessageBodiesLength = 0;

      sendData(availableData);

      if (httpRequest.getHeaderField("Debug") != null) {
        String uuid = httpRequest.getHeaderField("Debug");
        System.out.println("uuid=" + uuid
                + " Cached_header_length=" + cumMessageHeadersLength + " bytes\n"
                + "uuid=" + uuid
                + " Cached_body_length=" + cumMessageBodiesLength + " bytes");
      }

      availableData.clear();
      availableData = null;
    }

    // Wait for the thread which request for the remaining data
    if (unAvailableData.size() > 0) {
      try {
        //fetchTarget.join(millis);
        auxThread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } finally {
        auxThread.interrupt();
      }

      // Send remaining data
      proxySessionTarget.lock();
      proxySessionTarget.getRemainder(proxyCacheModel, unAvailableData);
      proxySessionTarget.unlock();

      cumMessageHeadersLength = 0;
      cumMessageBodiesLength = 0;

      //System.out.println("\n### SENDING REQUESTED DATA ###");
      sendData(unAvailableData);

      if (httpRequest.getHeaderField("Debug") != null) {
        String uuid = httpRequest.getHeaderField("Debug");
        System.out.println("uuid=" + uuid
                + " Requested_header_length=" + cumMessageHeadersLength + " bytes\n"
                + "uuid=" + uuid
                + " Requested_body_length=" + cumMessageBodiesLength + " bytes");
      }

      unAvailableData.clear();
      unAvailableData = null;
    }

    EORReasonCode = EORCodes.IMAGE_DONE;

    // Send the JPIP End Of Response
    sendEndOfResponse(httpResponseSender, jpipMessageEncoder);

    // Send end of chunk
    httpResponseSender.endOfChunk();
    httpResponseSender.flush();

    // Add the requested WOI to historic
    if (!isStatelessRequest) {
      proxySession.addWOIToHistory(cid, jpipRequestFields.viewWindowField);
    }
    proxySessionTarget.addWOIHistory(jpipRequestFields.viewWindowField);


    if (isStatelessRequest) {
      // For stateless connections, clears the cache
      listOfClientSessions.remove(cid);
    } else {
    }

  }

  /**
   * This method tests several JPIP restrictions of the parameters
   *
   * @throws WarningException
   */
  private void checkJPIPParameters() throws WarningException {

    // Sub-target field is always asociate with the target field.
    // See ISO/IEC 15444-9 section C.2.1
    if ((jpipRequestFields.targetField.target == null) && (jpipRequestFields.targetField.subtarget != null)) {
      log.logInfo(getName() + "BAD_REQUEST: The request has some incompatible JPIP fields, wrong combinatio of target and subtarget parameters");
      sendHTTPResponseError(StatusCodes.BAD_REQUEST, "The request has some incompatible JPIP fields, wrong combinatio of target and subtarget parameters.");
      throw new WarningException();
    }

    // Logical target is specified throw one of:
    // 1- a combination of Target, Sub-target and Target ID
    // 2- Channel ID
    // See ISO/IEC 15444-9 section C.2.1
    if (((jpipRequestFields.targetField.target != null) || (jpipRequestFields.targetField.tid != null)) && (jpipRequestFields.channelField.cid != null)) {
      log.logInfo(getName() + "BAD_REQUEST: The request has some incompatible JPIP fields, wrong combination of target, tid, and cid parameters.");
      sendHTTPResponseError(StatusCodes.BAD_REQUEST, "The request has some incompatible JPIP fields, wrong combination of target, tid, and cid parameters.");
      throw new WarningException();
    }

    // The Offset field is valid only in conjunction with the Frame Size.
    // See ISO/IEC 15444-9 section C.4.3
    if ((jpipRequestFields.viewWindowField.fsiz == null) && (jpipRequestFields.viewWindowField.roff != null)) {
      log.logInfo(getName() + "BAD_REQUEST: The request has some incompatible JPIP fields, wrong combinatio of fsiz and roff parameters.");
      sendHTTPResponseError(StatusCodes.BAD_REQUEST, "The request has some incompatible JPIP fields, wrong combinatio of fsiz and roff parameters.");
      throw new WarningException();
    }

    // The Region Size field is valid only in conjunction with the Frame Size.
    // See ISO/IEC 15444-9 section C.4.4
    if ((jpipRequestFields.viewWindowField.fsiz == null) && (jpipRequestFields.viewWindowField.rsiz != null)) {
      log.logInfo(getName() + "BAD_REQUEST: The request has some incompatible JPIP fields, wrong combinatio of fsiz and rsiz parameters.");
      sendHTTPResponseError(StatusCodes.BAD_REQUEST, "The request has some incompatible JPIP fields, wrong combinatio of fsiz and rsiz parameters.");
      throw new WarningException();
    }


    // CHANNEL CLOSE FIELD
    // See ISO/IEC 15444-9 section C.3.4
    if (jpipRequestFields.channelField.cclose != null) {

      // Wildcard must be associate with a channel identifier
      if (jpipRequestFields.channelField.cclose.get(0).equals("*") && (jpipRequestFields.channelField.cid == null)) {
        sendHTTPResponseError(StatusCodes.BAD_REQUEST, "All channel close is requested but channel identifier is not available");
        throw new WarningException();
      }

      if (jpipRequestFields.channelField.cclose.get(0).equals("*") && !listOfClientSessions.contains(jpipRequestFields.channelField.cid)) {
        sendHTTPResponseError(StatusCodes.BAD_REQUEST, "All channel close is requested but channel identifier is wrong");
        throw new WarningException();
      }

      // Check if all the cclose identifiers belongs to the same session
      cid = (jpipRequestFields.channelField.cid == null)
              ? jpipRequestFields.channelField.cid
              : jpipRequestFields.channelField.cclose.get(0);

      if (!((jpipRequestFields.channelField.cclose.size() == 1) && jpipRequestFields.channelField.cclose.get(0).equals("*"))) {
        for (int i = 0; i < jpipRequestFields.channelField.cclose.size(); i++) {
          if (!listOfClientSessions.belongs(cid, jpipRequestFields.channelField.cclose.get(i))) {
            sendHTTPResponseError(StatusCodes.BAD_REQUEST, "Channel close identifier do not belong to the same session.");
            throw new WarningException();
          }
        }
      }
    }

  }

  /**
   * This method is used to manage the client sessions.
   * <p>
   * Tests if it is a stateless request, if it is a request which belongs to
   * a previous session, or it is a request for a new session. When a
   * stateless request is used, a temporary session is created and it must be
   * removed when the request process finishes. When the request is for a new
   * session, a new entry in the
   * <code>listOfClientSessions</code> is created
   * with the client preferences and capabilities. And if the request belongs
   * to a previous session, then, client data (logical target, image return
   * type, cache, preferences, capabilites, etc. ) are read from the
   * <code>
   * listOfClientSessions</code>.
   * <p>
   * When an error in the request is found (p.e. session identifier does not
   * exist or are wrong, logical target does not exist, etc.), an error
   * message is sent to the client and a
   * <code>WarningException</code> is
   * thrown.
   *
   * @throws WarningException when something anomalous (p.e. session identifier
   * does not exist or are wrong, logical target does not exist, etc.) this
   * exception will be thrown.
   */
  public void sessionsManager() throws WarningException {

    // Stateless or session request (see ISO/IEC 15444-9 section C.3.1)

    if ((jpipRequestFields.channelField.cid != null)
            || (jpipRequestFields.channelField.cnew != null)) {
      // Request within a session

      isStatelessRequest = false;

      if (jpipRequestFields.channelField.cid != null) { // Client has a cid

        if (!listOfClientSessions.contains(jpipRequestFields.channelField.cid)) {
          // Client cid isn't registered in the list of registered client's session
          sendHTTPResponseError(BAD_REQUEST, "The \"channel idenfier\" used is wrong or from an old session");
          log.logDebug(getName() + ": remote host " + socket.getRemoteSocketAddress()
                  + " has requested with the wrong channel identifier " + jpipRequestFields.channelField.cid);
          throw new WarningException();
        }

        // Client has a registered session from a previous connection
        cid = jpipRequestFields.channelField.cid;

        if (jpipRequestFields.channelField.cnew != null) {	// Request for a new channel withing the same session
          sendHTTPResponseError(NOT_IMPLEMENTED, "I'm sorry, but the CADI server only support one channel by each session.");
          log.logDebug(getName() + ": remote host " + socket.getRemoteSocketAddress()
                  + " has requested to create more than one channel in the session");
          throw new WarningException();
        }

        if ((jpipRequestFields.channelField.path != null) && !jpipRequestFields.channelField.path.equals(channelFieldPath)) {
          // Check if the channel path has a correct value
          sendHTTPResponseError(NOT_IMPLEMENTED, "The \"path\" (" + jpipRequestFields.channelField.path
                  + ") sent, it is wrong or from an old session.");
          log.logDebug(getName() + ": remote host " + socket.getRemoteSocketAddress()
                  + " has requested with a wrong channel path (" + jpipRequestFields.channelField.path + ")");
          throw new WarningException();
        }

        // Get the client properties
        proxySession = listOfClientSessions.get(cid);
        jpipRequestFields.targetField.tid = proxySession.getTID(cid);
        proxyCacheModel = proxySession.getCache(cid);

      } else {
        // Client requests for a new session

        // Search http-tcp transports. They are not supported yet,
        // then an http response error is sent.
        for (int i = 0; i < jpipRequestFields.channelField.cnew.size(); i++) {
          if (jpipRequestFields.channelField.cnew.get(i).equals("http-tcp")) {
            sendHTTPResponseError(NOT_IMPLEMENTED, "I'm sorry, but the CADI server only support HTTP channels.");
            log.logInfo(getName() + ": remote host " + socket.getRemoteSocketAddress()
                    + " has requested for an HTTP-TCP channel");
            throw new WarningException();
          }
        }

        // Creates a session
        proxySessionTarget.lock();
        proxySession = new ProxyClientSession();
        cid = proxySession.createSessionTarget(
                proxySessionTarget.getProxyTID(),
                proxySessionTarget.getJP2KProxyLogicalTarget().getCodestream(0),
                "jpp-stream", "http", true);

        listOfClientSessions.add(proxySession);
        proxySessionTarget.unlock();

        jpipResponseFields.cid = cid;
        jpipResponseFields.path = "CADIServer";
        jpipResponseFields.transport = JPIPResponseFields.TRANSPORT_HTTP;

        log.logInfo(getName() + ": for remote host " + socket.getRemoteSocketAddress()
                + " assigned a new session identifier cid=" + cid + " to the remote host ");

      }

    } else {
      // Stateless request
      isStatelessRequest = true;
      log.logInfo(getName() + ": stateless request");

      // Creates a temporal session
      proxySessionTarget.lock();
      proxySession = new ProxyClientSession();
      cid = proxySession.createSessionTarget(
              proxySessionTarget.getProxyTID(),
              proxySessionTarget.getJP2KProxyLogicalTarget().getCodestream(0),
              "jpp-stream", "http", true);
      listOfClientSessions.add(proxySession);
      proxySessionTarget.unlock();
    }
  }

  /**
   * This method is used to manage the list of loaded targets. The targed will
   * be passed through the jpip
   * <code>target</code> field, the jpip
   * <code> tid
   * </code> field, or both.
   * <p>
   * If the requested target has been loaded in a previous request, it will be
   * in the list of loaded targets. So, its parameters will be recovered from
   * that list.
   * <p>
   * Otherwise, target will be searched in disk. And if it is in disk, it will
   * be loaded to the list.
   *
   * @param target definition in {@link CADI.Common.Network.JPIP.TargetField#target}
   * @param tid definition in {@link CADI.Common.Network.JPIP.TargetField#tid}
   */
  private void logicalTargetsManager(String target, String tid) throws WarningException {
    //System.err.println("\nUSER: "+userAgent+" =>target="+target+" tid="+tid);
    // Logical target may be specified through the target, targetID, or both.
    if ((tid != null) && (tid.compareTo("0") == 0)) {
      tid = null;
    }

    if ((target != null) && (tid != null)) {
      // logical target is defined by both parameters
      if (proxySessionTargets.equalsByProxyTID(target, tid)) {
        proxySessionTarget = proxySessionTargets.getByProxyTID(tid);
      } else {
        sendHTTPResponseError(BAD_REQUEST, "The URL logical target identifiers (target and tid) do  not identify the same logical target");
        log.logDebug(getName() + ": remote host " + socket.getRemoteSocketAddress()
                + " requested for logical target with: target="
                + jpipRequestFields.targetField.target
                + " and tid=" + jpipRequestFields.targetField.tid
                + ". They do not identify the same logical target");
        throw new WarningException();
      }
    } else if (target != null) {
      // logical target is defined by target parameter
      proxySessionTarget = proxySessionTargets.getByTarget(target);
    } else if (tid != null) {
      // logical target is defined by tid parameter
      proxySessionTarget = proxySessionTargets.getByProxyTID(tid);
    } else {
      // logical target is not defined neither by target nor tid
      sendHTTPResponseError(BAD_REQUEST, "The URL has not any parameter which identifies the logical target");
      throw new WarningException();
    }


    // If the logical target is not in the linked list, it will be fetched from the server
    if (proxySessionTarget == null) {
      // The logical target is not in the list.
      //System.err.println("\nUSER: "+userAgent+" => Logical target is not in the list!!!!");

      URI uri = httpRequest.getURI();
      String server = uri.getHost();
      int port = uri.getPort();
      if (port < 0) {
        port = ServerDefaultValues.PORT;
      }
      ArrayList<String> preferredTransportProtocols = new ArrayList<String>();
      preferredTransportProtocols.add("http");

      proxySessionTarget = proxySessionTargets.create(server, port, target, preferredTransportProtocols, log);

      // Lock and set session properties
      proxySessionTarget.lock();
      ArrayList<String> returnTypes = new ArrayList<String>(1);
      returnTypes.add("jpp-stream");
      proxySessionTarget.setAllowedReturnTypes(returnTypes, true);
      proxySessionTarget.setAlign(true);
      proxySessionTarget.setWait(true);
      proxySessionTarget.setProxyVia(proxyVia);
      proxySessionTarget.setUseHTTPSession(true);
      //proxySessionTarget.setCacheType(BinDescriptor.EXPLICIT_FORM);
      //proxySessionTarget.setUseNumberOfBytes(true);
      //proxySessionTarget.setUseNumberOfLayers(true);
      try {
        proxySessionTarget.getMainHeader();

        if (predictiveModel != null) {
          String absoluteFileName = (predictiveModel.endsWith(File.separator)
                  ? predictiveModel : predictiveModel + File.separator) + target;
          int lastPoint = absoluteFileName.lastIndexOf('.');
       
          ReadPredictiveModel rpm = new ReadPredictiveModel(absoluteFileName.substring(0, lastPoint) + ".pm");
          proxySessionTarget.setPredictiveModel(new PredictiveScalingFactors(rpm.run()));
        }

      } catch (ErrorException e) {
        sendHTTPResponseError(NOT_FOUND,
                "I'm sorry, but the requested logical target \""
                + jpipRequestFields.targetField.target
                + "\" is not available or you do not have permissions for reading it.");
        log.logDebug(getName() + ": remote host " + socket.getRemoteSocketAddress()
                + " requested for logical target "
                + jpipRequestFields.targetField.target
                + " but it is not found");
        throw new WarningException();
      } catch (FileNotFoundException fnfe) {
      } catch (IOException ioe) {
      } finally {
        proxySessionTarget.unlock();
      }

    }

    // Logical target must not null. If not, there is an error in this method
    assert (proxySessionTarget != null);
  }

  /**
   *
   * @param viewWindow
   */
  private void processViewWindow(ViewWindowField viewWindow) throws ErrorException {
    actualViewWindow = new ViewWindowField();
    responseViewWindow = new ViewWindowField();

    // If the frame size has been omitted, no compressed image data is sent.
    if (viewWindow == null) {
      return;
    }
    if ((viewWindow.fsiz[0] <= 0) && (viewWindow.fsiz[1] <= 0)) {
      return;
    }
    if ((viewWindow.fsiz[0] <= 0) || (viewWindow.fsiz[1] <= 0)) {
      assert (true);
    }

    // It is the WOI to be delivered.
    // All fields of the object have to be set and ranges checked
    actualViewWindow = new ViewWindowField(viewWindow);
    if (actualViewWindow.fsiz[0] > codestream.getXSize()) {
      actualViewWindow.fsiz[0] = codestream.getXSize();
    }
    if (actualViewWindow.fsiz[1] > codestream.getYSize()) {
      actualViewWindow.fsiz[1] = codestream.getYSize();
    }
    if (actualViewWindow.roff[0] < 0) {
      actualViewWindow.roff[0] = 0;
    }
    if (actualViewWindow.roff[1] < 0) {
      actualViewWindow.roff[1] = 0;
    }
    if (actualViewWindow.rsiz[0] < 0) {
      actualViewWindow.rsiz[0] = actualViewWindow.fsiz[0];
    }
    if (actualViewWindow.rsiz[1] < 0) {
      actualViewWindow.rsiz[1] = actualViewWindow.fsiz[1];
    }
    if (actualViewWindow.roff[0] + actualViewWindow.rsiz[0] > actualViewWindow.fsiz[0]) {
      actualViewWindow.rsiz[0] = actualViewWindow.fsiz[0] - actualViewWindow.roff[0];
    }
    if (actualViewWindow.roff[1] + actualViewWindow.rsiz[1] > actualViewWindow.fsiz[1]) {
      actualViewWindow.rsiz[1] = actualViewWindow.fsiz[1] - actualViewWindow.roff[1];
    }
    if (actualViewWindow.comps == null) {
      actualViewWindow.comps = new int[1][2];
      actualViewWindow.comps[0][0] = 0;
      actualViewWindow.comps[0][1] = codestream.getZSize() - 1;
    }
    if (actualViewWindow.layers < 0) {
      actualViewWindow.layers = codestream.getNumLayers();
    }


    if ((actualViewWindow.roff[0] >= actualViewWindow.fsiz[0])
            || (actualViewWindow.roff[1] >= actualViewWindow.fsiz[1])) {
      throw new ErrorException("The frame size cannot be greater than the region offset");
    }
    //assert (true);


    // Get the number of discard levels
    int discardLevels =
            codestream.determineNumberOfDiscardLevels(actualViewWindow.fsiz,
            actualViewWindow.roundDirection);

    // Fit requested region to the suitable resolution
    codestream.mapRegionToSuitableResolutionGrid(actualViewWindow.fsiz,
            actualViewWindow.roff,
            actualViewWindow.rsiz,
            discardLevels);

    // Check if geometry has been change.
    // If so, it must be notified to client
    if ((viewWindow.fsiz[0] != actualViewWindow.fsiz[0])
            || (viewWindow.fsiz[1] != actualViewWindow.fsiz[1])) {
      responseViewWindow.fsiz[0] = actualViewWindow.fsiz[0];
      responseViewWindow.fsiz[1] = actualViewWindow.fsiz[1];
    }
    if ((viewWindow.roff[0] != actualViewWindow.roff[0])
            || (viewWindow.roff[1] != actualViewWindow.roff[1])) {
      responseViewWindow.roff[0] = actualViewWindow.roff[0];
      responseViewWindow.roff[1] = actualViewWindow.roff[1];
    }
    if ((viewWindow.rsiz[0] != actualViewWindow.rsiz[0])
            || (viewWindow.rsiz[1] != actualViewWindow.rsiz[1])) {
      responseViewWindow.rsiz[0] = actualViewWindow.rsiz[0];
      responseViewWindow.rsiz[1] = actualViewWindow.rsiz[1];
    }

    // Adjust the quality response parameter to the quality layers.
    // It is a coarse estimation and it should be improved (but is the easiest method)
    //quality = (int)(100D / 1.0*codestream.getNumLayers()) * actualViewWindow.layers;
  }

  /**
   * Adds the metadata bin 0 to the response.
   * <p>
   * It is a temporal method while metadata are not supported.
   *
   * @throws IOException
   */
  private void addMetadaBin0(HTTPResponseSender httpResponseSender, JPIPMessageEncoder jpipMessageEncoder) throws IOException {
    JPIPMessageHeader jpipMetadataHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.METADATA, 0, 0, 0, true, -1);
    byte[] jpipHeader = jpipMessageEncoder.encoderHeader(jpipMetadataHeader);
    httpResponseSender.sendChunk(jpipHeader);
  }

  /**
   *
   */
  private void sendDataNew(ArrayList<RelevantPrecinct> relevantPrecincts) throws IOException {

    stopWatch.reset();
    stopWatch.start();
    long sentDataLength = 0;

    ProxyCacheManagement cacheManagement = proxySessionTarget.getCacheManagement();
    DataBin dataBin = null;
    long length = 0;

    /* for (RelevantPrecinct precinct : relevantPrecincts) {
     * //System.out.println(descriptor.toString());
     *
     * dataBin = cacheManagement.getDataBin(ClassIdentifiers.PRECINCT, precinct.inClassIdentifier);
     * int classIdentifier = useExtendedHeaders ? ClassIdentifiers.PRECINCT+1 : ClassIdentifiers.PRECINCT;
     *
     *
     * length = 0;
     * int maxNumChunks = dataBin.data.size();
     * for (int index = 0; (index < maxNumChunks) && (length < precinct.msgLength); index++) {
     * if ((dataBin.offsets.get(index)+dataBin.data.get(index).length) > precinct.msgOffset) {
     *
     * if (dataBin.offsets.get(index) < precinct.msgOffset) {
     * long diffOffsets = precinct.msgOffset - dataBin.offsets.get(index);
     * JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, classIdentifier, precinct.inClassIdentifier, dataBin.offsets.get(index)+diffOffsets, dataBin.data.get(index).length-diffOffsets, true, dataBin.numCompletedPackets.get(index));
     * httpResponseSender.sendChunk(jpipMessageEncoder.encoderHeader(jpipMessageHeader));
     * byte[] tmpData = Arrays.copyOfRange(dataBin.data.get(index), (int)diffOffsets, dataBin.data.get(index).length);
     * httpResponseSender.sendChunk(tmpData);
     * length += tmpData.length;
     * } else {
     * JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, classIdentifier, precinct.inClassIdentifier, dataBin.offsets.get(index), dataBin.data.get(index).length, true, dataBin.numCompletedPackets.get(index));
     * httpResponseSender.sendChunk(jpipMessageEncoder.encoderHeader(jpipMessageHeader));
     * httpResponseSender.sendChunk(dataBin.data.get(index));
     * length += dataBin.data.get(index).length;
     * }
     * }
     * }
     * sentDataLength += length;
     * } */

    stopWatch.stop();
    /* System.out.println(clientUserAgent+" ----------------------\n"
     * + clientUserAgent+" Time stamp: "+System.currentTimeMillis()+"\n"
     * + clientUserAgent+" Title: "+getName()+"\n"
     * + clientUserAgent+" Client: "+clientUserAgent+"\n"
     * + clientUserAgent+" Requested WOI: "+jpipRequestFields.viewWindowField.toString()+"\n"
     * + clientUserAgent+" Elapsed time: "+stopWatch.getTime()+"(ms)\n"
     * + clientUserAgent+" Data sent: "+sentDataLength+" (bytes) ("+inCacheText+")\n"
     * +clientUserAgent+" ----------------------\n"
     * ); */
  }

  /**
   *
   */
  private void sendData(ArrayList<SendDataInfo> cacheDescriptor) throws IOException {

    JPIPMessageHeader jpipMessageHeader = null;

    stopWatch.reset();
    stopWatch.start();

    ProxyCacheManagement cacheManagement = proxySessionTarget.getCacheManagement();

    for (SendDataInfo descriptor : cacheDescriptor) {

      if (descriptor.bytesLength <= 0) {
        continue;
      }

      if (descriptor.classIdentifier == SendDataInfo.MAIN_HEADER) {

        MainHeaderDataBin dataBin = (MainHeaderDataBin) cacheManagement.getDataBin(SendDataInfo.MAIN_HEADER, 0);

        jpipMessageHeader = new JPIPMessageHeader(-1, descriptor.classIdentifier, 0, descriptor.bytesOffset, descriptor.bytesLength, dataBin.isComplete(), -1);
        byte[] header = jpipMessageEncoder.encoderHeader(jpipMessageHeader);
        httpResponseSender.sendChunk(header);
        cumMessageHeadersLength += header.length;

        dataBin.lock();
        byte[] bheader = new byte[descriptor.bytesLength];
        dataBin.seek(descriptor.bytesOffset);
        dataBin.readFully(bheader);
        dataBin.unlock();
        httpResponseSender.sendChunk(bheader);
        cumMessageBodiesLength += descriptor.bytesLength;

        updateCache(jpipMessageHeader);

      } else if (descriptor.classIdentifier == SendDataInfo.TILE_HEADER) {
        TileHeaderDataBin dataBin = (TileHeaderDataBin) cacheManagement.getDataBin(SendDataInfo.TILE_HEADER, 0);

        jpipMessageHeader = new JPIPMessageHeader(-1, descriptor.classIdentifier, 0, descriptor.bytesOffset, descriptor.bytesLength, dataBin.isComplete(), -1);
        byte[] header = jpipMessageEncoder.encoderHeader(jpipMessageHeader);
        httpResponseSender.sendChunk(header);
        cumMessageHeadersLength += header.length;

        dataBin.lock();
        byte[] bheader = new byte[descriptor.bytesLength];
        dataBin.seek(descriptor.bytesOffset);
        dataBin.readFully(bheader);
        dataBin.unlock();
        httpResponseSender.sendChunk(bheader);
        cumMessageBodiesLength += descriptor.bytesLength;

        updateCache(jpipMessageHeader);

      } else if (descriptor.classIdentifier == SendDataInfo.PRECINCT) {
        int classIdentifier = descriptor.classIdentifier;

        if (useExtendedHeaders) {  // Set extended headers
          classIdentifier = SendDataInfo.PRECINCT + 1;
        }

        PrecinctDataBin dataBin = (PrecinctDataBin) cacheManagement.getDataBin(descriptor.classIdentifier, descriptor.inClassIdentifier);
        if (dataBin == null) {
          log.logWarning(getName() + ": inconsistency in the proxy cache");
          System.err.println("WARNING: inconsistency in the proxy cache.");
          continue;
        }

        int dataBinLen = (int) dataBin.getLength();
        if ((descriptor.bytesOffset + descriptor.bytesLength) > dataBinLen) {
          descriptor.bytesLength = dataBinLen - descriptor.bytesOffset;
        }
        assert (descriptor.bytesLength >= 0);

        jpipMessageHeader = new JPIPMessageHeader(-1, classIdentifier, descriptor.inClassIdentifier, descriptor.bytesOffset, descriptor.bytesLength, dataBin.isComplete(), dataBin.getLastCompleteLayer(descriptor.bytesOffset + descriptor.bytesLength));
        byte[] header = jpipMessageEncoder.encoderHeader(jpipMessageHeader);
        httpResponseSender.sendChunk(header);
        cumMessageHeadersLength += header.length;

        dataBin.lock();
        byte[] tmpData = new byte[descriptor.bytesLength];
        dataBin.seek(descriptor.bytesOffset);
        dataBin.readFully(tmpData);
        dataBin.unlock();
        httpResponseSender.sendChunk(tmpData);
        cumMessageBodiesLength += descriptor.bytesLength;

        updateCache(jpipMessageHeader);

      } else {
        assert (true);
      }
    }

    stopWatch.stop();
  }

  /**
   * This method is used to send the End of Response of a JPIP message.
   *
   * @param httpResponseSender
   * @param jpipMessageEncoder
   *
   * @throws IOException
   */
  private void sendEndOfResponse(HTTPResponseSender httpResponseSender, JPIPMessageEncoder jpipMessageEncoder) throws IOException {

    byte[] jpipMessageBody = null;
    long length = 0;
    if (EORReasonMessage != null) {
      jpipMessageBody = EORReasonMessage.getBytes();
      length = jpipMessageBody.length;
    }

    byte[] jpipHeader = jpipMessageEncoder.encoderHeader(new JPIPMessageHeader(EORReasonCode, (int) length));
    httpResponseSender.sendChunks(jpipHeader, jpipMessageBody);
  }

  /**
   * This method is used to update the cache with the data that has been sent
   * to the client. Therefore, it must be called after the response has been
   * sent to the client.
   *
   * @param jpipMessageData an array list with the data that has been sent.
   */
  private void updateCache(JPIPMessageHeader jpipMessageHeader) {

    proxyCacheModel.update(true, (jpipMessageHeader.classIdentifier / 2) * 2,
            jpipMessageHeader.inClassIdentifier,
            (int) (jpipMessageHeader.msgOffset + jpipMessageHeader.msgLength));
  }
}
