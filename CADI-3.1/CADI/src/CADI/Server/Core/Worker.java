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

import CADI.Common.Network.TrafficShaping;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import CADI.Common.Network.HTTP.*;
import CADI.Common.Network.JPIP.*;
import CADI.Common.Log.CADILog;
import GiciStream.BufferedDataInputStream;
import CADI.Server.Cache.ServerCacheModel;
import CADI.Server.LogicalTarget.*;
import CADI.Server.LogicalTarget.JPEG2000.*;
import CADI.Server.Network.*;
import CADI.Server.Request.*;
import CADI.Server.ServerDefaultValues;
import CADI.Server.Session.*;
import GiciException.*;
import java.util.Properties;

/**
 * This class performs the client request processing and it sends the server
 * response to the client. It does the following tasks: manages the client
 * sessions, manages logical targets, check restrictions, sends the response
 * (data or errors), etc.
 * <p>
 * This thread is launched and it will be waiting for a task. Tasks are passed
 * to this worker through a
 * <code>TaskQueue</code> queue (the
 * <code>Scheduler
 * <code> puts the task into the queue and the
 * <code>Worker</code> get them).
 * <p>
 * This thread is always running or waiting for a task. Therefore, it only can
 * be finished if the
 * <code>{@link #finish}</code> is set to
 * <code>true</code>.
 * <p>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; setParameters<br>
 * &nbsp; run<br>
 * &nbsp; ....<br>
 * &nbsp; finish<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.4 2011/03/03
 */
public class Worker extends Thread implements StatusCodes, EORCodes {

  /**
   * Definition in {@link CADI.Server.Core.Scheduler#tasksQueue}
   */
  private TasksQueue tasksQueue = null;

  /**
   * Definition in {@link CADI.Server.Core.Scheduler#logicalTargetList}.
   */
  private ServerLogicalTargetList logicalTargetList = null;

  /**
   * Definition in {@link CADI.Server.Core.Scheduler#clientSessions}.
   */
  private ServerClientSessions listOfClientSessions = null;

  /**
   * Definition in {@link CADI.Server.Core.Scheduler#log}.
   */
  private CADILog log = null;

  /**
   * Is the path directory where the logical targets are stored.
   */
  private String targetsPath = null;

  /**
   * Is the path directory that will be used to save the temporary files
   * used to cache client data.
   */
  private String cachePath = null;

  /**
   * Further information, see {@link CADI.Server.ServerParser#serverArguments}.
   */
  private int deliveringMode = -1;

  /**
   * Indicates a subtype of the {@link  #deliveringMode} attribute. Thus, its
   * value will depend on the value taken by {@link  #deliveringMode}.
   * <p>
   * Further information, see {@link CADI.Server.ServerParser#serverArguments}.
   *
   * {
   *
   * @see #rateDistortionMethod} value.
   */
  private int deliveringSubtype = -1;

  /**
   * Definition in {@link CADI.Server.Core.Scheduler#keepAliveTimeout}.
   */
  private int keepAliveTimeout = 0;

  /**
   * Definition in {@link CADI.Server.Core.Scheduler#maxTxRate}.
   */
  private long maxTxRate = 0L;

  /**
   * Definition in {@link CADI.Server.Core.Scheduler#maxTxRate}.
   */
  private int trafficShaping = TrafficShaping.NONE;

  /**
   * Definition in {@link CADI.Server.Network.JPIPMessageEncoder#independentForm}.
   */
  private boolean independentMessageHeaders = true;

  /**
   * Definition in {@link CADI.Server.Core.Scheduler#predictiveModel}.
   */
  private boolean predictiveModel = false;

  /**
   * Contains the client socket of which request is being processed.
   */
  private Socket socket = null;

  /**
   * Definition in {@link CADI.Common.Network.HTTP.HTTPRequest}.
   */
  private HTTPRequest httpRequest = null;

  /**
   * Definition in {@link CADI.Common.Network.JPIP.JPIPRequestFields}.
   */
  private JPIPRequestFields jpipRequestFields = null;

  /**
   * Indicates whether the thread has to finish when it is working with a
   * request with the keep-alive mode set.
   *
   * @see #finish()
   */
  private boolean finish = false;

  /**
   * Indicates if the thread is carrying out a task. If the thread is waiting
   * for a new task, the
   * <code>busy</code> attribute is
   * <code>false</code>.
   */
  private boolean busy = false;

  // INTERNAL ATTRIBUTES
  /**
   * Is an output stream that will be used to send the server response to the client.
   */
  private OutputStream os = null;

  /**
   *
   */
  private HTTPResponseSender httpResponseSender = null;

  /**
   *
   */
  private HTTPResponse httpResponse = null;

  /**
   *
   */
  private JPIPResponseFields jpipResponseFields = null;

  /**
   * Contains a pointer to the parameters of the logical target which is being
   * requested.
   */
  private JP2KServerLogicalTarget logicalTarget = null;

  /**
   *
   */
  private ServerCacheModel serverCache = null;

  private ServerJPIPChannel jpipChannel = null;

  /**
   * Indicates whether the actual request is a stateles request or a request
   * with a session.
   */
  private boolean isStatelessRequest = true;

  /**
   * Contains the session object where data about the actual session is
   * saved.
   */
  private ServerClientSession session = null;

  /**
   * Definition in {@link CADI.Common.Network.JPIP.ChannelField#cid}.
   */
  private String cid = null;

  /**
   * It is the session identifier of the request which is being processed.
   */
  //private String sessionID = null;
  /**
   *
   */
  private boolean sendLogicalTargetData = false;

  /**
   *
   */
  private static String channelFieldPath = "CADIServer";

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

  private String version = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param threadName is a string with the name of the thread.
   * @param tasksQueue definition in {@link #tasksQueue}.
   * @param logicalTargetList definition in {@link #logicalTargetList}.
   * @param clientSessions definition in {@link #listOfClientSessions}.
   * @param log definition in {@link #log}.
   */
  public Worker(String threadName, TasksQueue tasksQueue,
                ServerLogicalTargetList logicalTargetList,
                ServerClientSessions clientSessions,
                CADILog log) {

    this.tasksQueue = tasksQueue;
    this.logicalTargetList = logicalTargetList;
    this.listOfClientSessions = clientSessions;
    this.log = log;

    setName(threadName);
    jpipResponseFields = new JPIPResponseFields();
    httpResponse = new HTTPResponse();

    try {
      Properties cadiInfo = new Properties();
      cadiInfo.load(getClass().getClassLoader().getResourceAsStream("CADI/Common/Info/cadiInfo.properties"));
      version = cadiInfo.getProperty("version");
    } catch (Exception e) {
    }
  }

  /**
   * Sets the {@link #targetsPath} attribute.
   *
   * @param targetsPath definition in {@link #targetsPath}.
   */
  public void setTargetsPath(String targetsPath) {
    this.targetsPath = targetsPath;
  }

  /**
   * Sets the {@link #cachePath} attribute.
   *
   * @param cachePath definition in {@link #cachePath}.
   */
  public void setCachePath(String cachePath) {
    this.cachePath = cachePath;
  }

  /**
   * Sets the mode used to deliver precinct data to the client.
   *
   * @param deliveringMode definition in {@link #deliveringMode}.
   * @param deliveringSubtype definition in {@link #deliveringSubtype}.
   */
  public void setDeliveringMode(int deliveringMode, int deliveringSubtype) {
    this.deliveringMode = deliveringMode;
    this.deliveringSubtype = deliveringSubtype;
  }

  /**
   * Sets the {@link #keepAliveTimeout} attribute.
   *
   * @param keepAliveTimeout definition in {@link #keepAliveTimeout}.
   */
  public void setKeepAliveTimeout(int keepAliveTimeout) {
    this.keepAliveTimeout = keepAliveTimeout;
  }

  /**
   * Sets the {@link #maxTxRate} attribute.
   *
   * @param maxTxRate definition in {@link #maxTxRate} attribute.
   */
  public void setMaxTxRate(long maxTxRate) {
    this.maxTxRate = maxTxRate;
  }

  /**
   * Sets the {@link #trafficShaping} attribute.
   *
   * @param maxTxRate definition in {@link #trafficShaping} attribute.
   */
  public void setTrafficShaping(int trafficShaping) {
    this.trafficShaping = trafficShaping;
  }

  /**
   * Sets the {@link #independentMesssageHeaders} attribute.
   *
   * @param independentMesssageHeaders definition in {@link #independentMesssageHeaders} attribute.
   */
  public void setIndependentMessageHeaders(boolean independentMessageHeaders) {
    this.independentMessageHeaders = independentMessageHeaders;
  }

  /**
   * Sets the {@link #predictiveModel} attribute.
   *
   * @param predictiveModel definition in {@link #predictiveModel}.
   */
  public void setPredictiveModel(boolean predictiveModel) {
    this.predictiveModel = predictiveModel;
  }

  /**
   * This method implements the control of the thread.
   * <p>
   * When the
   * <code>Scheduler</code> assigns a task, it will be awake. The
   * task may be:<br>
   * <ul>
   * <li> Process a client request.
   * <li> Sends a message error to the client.
   * </ul>
   *
   * Moreover, when the keep-alive mode of a client request is set, the
   * thread is dedicated to wait for a new request until the timeout value
   * is reached.
   * <p>
   * The thread will be running until the
   * <code>finish</code> attribute is
   * <code>false</code>.
   */
  @Override
  public void run() {

    TasksQueueNode task = null;
    int statusCode = -1;
    String reasonPhrase = null;
    boolean keepAlive = true;
    boolean error = false;

    if (log.isLog(CADILog.LEVEL_INFO)) {
      log.logInfo(getName() + ": started");
    }

    // Main loop
    while (!finish) {

      busy = false;

      // Thread is sleeping until a new work is added to the queue
      try {
        task = tasksQueue.get();
      } catch (InterruptedException e1) {
        finish = true;
        break;
      }

      resetTime();
      busy = true;

      statusCode = task.statusCode;
      this.socket = task.socket;
      if (statusCode == 0) {
        this.httpRequest = task.httpRequest;
        this.jpipRequestFields = task.jpipRequestFields;
      } else {
        reasonPhrase = task.reasonPhrase;
      }

      if (log.isLog(CADILog.LEVEL_INFO)) {
        log.logInfo(getName() + ": responding");
      }

      // Gets the output stream
      try {
        os = socket.getOutputStream();
      } catch (IOException e) {
        if (log.isLog(CADILog.LEVEL_INFO)) {
          log.logInfo(getName() + ": error opening output stream");
        }
        // Goes to sleep
        keepAlive = false;
        error = true;
      }

      httpResponseSender = new HTTPResponseSender(os);
      long maxRate = maxTxRate;
      if (jpipRequestFields.clientCapPrefField.pref.maxBandwidth > 0) {
        if (maxRate == 0) {
          maxRate = jpipRequestFields.clientCapPrefField.pref.maxBandwidth;
        } else if (maxRate > jpipRequestFields.clientCapPrefField.pref.maxBandwidth) {
          maxRate = jpipRequestFields.clientCapPrefField.pref.maxBandwidth;
        }
      }
      httpResponseSender.setMaxTxRate(maxRate, trafficShaping);


      if (!error) {

        if (statusCode != 0) {	// Thread has been awoken to send an error message
          sendHTTPResponseError(statusCode, reasonPhrase);
          statusCode = 0;

        } else { // Thread has been awoken to process a request
          keepAlive = true;

          while (keepAlive && !error && !finish) {

            // Process the client request
            try {
              processRequest();
            } catch (WarningException e) {
              error = true;
            }

            if (!error) {
              //	Check if keep-alive mode is set
              String value = httpRequest.getHeaderField("Connection");
              if ((value != null) && !value.equals("keep-alive")) {
                // HTTP keep-alive mode is not set. Thread goes to sleepJPIPResponseFields
                keepAlive = false;
                if (log.isLog(CADILog.LEVEL_INFO)) {
                  log.logInfo(getName() + ": response has been sent");
                }
              } else { // HTTP keep-alive mode is set, then wait for a new request
                if (log.isLog(CADILog.LEVEL_INFO)) {
                  log.logInfo(getName() + ": listening to keep-alive connections");
                }
                try {
                  listenNewRequest();
                } catch (WarningException we) {
                  statusCode = we.getErrorCode();
                  reasonPhrase = we.getMessage();
                  sendHTTPResponseError(statusCode, reasonPhrase);
                  error = true;
                  statusCode = 0;
                } catch (ErrorException ee) {
                  // <<<<<<<<<< to log file
                  keepAlive = false;
                }
              }
            }
          } // while (keepAlive && ....
        }
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
      if (log.isLog(CADILog.LEVEL_INFO)) {
        log.logInfo(getName() + ": going to sleep");
      }

    } // while (!finish)

    if (log.isLog(CADILog.LEVEL_INFO)) {
      log.logInfo(getName() + ": shuting down");
    }

    // Free objects
    socket = null;
    httpRequest = null;
    jpipRequestFields = null;
    httpResponse = null;
    jpipResponseFields = null;
    logicalTarget = null;
    session = null;
    serverCache = null;
    jpipChannel = null;
    reasonPhrase = null;
  }

  /**
   * This method is used to indicate that the thread has to finish.
   */
  public synchronized void finish() {
    this.finish = true;
  }

  /**
   * This method is used to indicate if the thread is or no busy processing a
   * task.
   *
   * @return
   * <code>true</code> if the thread is processing a task. Otherwise,
   * it returns
   * <code>false</code>.
   */
  public synchronized boolean isBusy() {
    return busy;
  }

  @Override
  public synchronized Thread.State getState() {
    return getState();
  }

  // ============================ private methods ==============================
  /**
   * This method performs the core to process the client requests. It is a
   * manager that uses several auxiliary classess and methods to process the
   * client request and to make the server response.
   */
  private void processRequest() throws WarningException {

    System.out.println();
    httpRequest.list(System.out);

    ArrayList<JPIPMessageHeader> jpipMessageHeaders = null;
    jpipResponseFields.reset();
    httpResponse.reset();
    httpResponse.setHeaderField("Server", "CADI Server v" + version);
    sendLogicalTargetData = false;

    if (log.isLog(CADILog.LEVEL_INFO)) {
      log.logInfo(httpRequest.toString());
    }
    //log.logDebug(jpipRequestFields.toString());


    // CHECK JPIP PARAMETERS RESTRICTIONS
    checkJPIPParameters();


    // MANAGE LOGICAL TARGET
    if (jpipRequestFields.channelField.cid != null) {
      session = listOfClientSessions.get(jpipRequestFields.channelField.cid);
      jpipRequestFields.targetField.tid = session.getTID(jpipRequestFields.channelField.cid);
    }
    logicalTargetsManager(jpipRequestFields.targetField.target, jpipRequestFields.targetField.tid);
    //logicalTarget.list(System.out);


    // MANAGE SESSION
    sessionsManager();
    serverCache = session.getCache(cid);
    jpipChannel = session.getJPIPChannel(cid);
    //session.list(System.out);


    // UPDATE CACHE MODEL
    serverCache.update(jpipRequestFields.cacheManagementField.model);

    // PROCESS REQUESTED WINDOW
    ProcessWOI processWOI =
            new ProcessWOI(logicalTarget, serverCache,
            jpipRequestFields.viewWindowField,
            jpipRequestFields.dataLimitField,
            jpipRequestFields.serverControlField.align);
    processWOI.setDeliveringMode(deliveringMode, deliveringSubtype);
    try {
      processWOI.run();
    } catch (IllegalArgumentException e2) {
      e2.printStackTrace();
    } catch (ErrorException e2) {
      sendHTTPResponseError(BAD_REQUEST, "The requested WOI has some parameters which is not correct");
      throw new WarningException();
    }
    ArrayList<ResponseData> responseDataList = processWOI.getResponseData();
    if (responseDataList != null) {
      sendLogicalTargetData = true;
    }

    // SET RESPONSE HEADER FIELDS (http & jpip)
    // Stream return type
    if (isStatelessRequest) {
      // Check which image return type are supported in JPIP client
      if (jpipRequestFields.serverControlField.type.get(0).equals("jpp-stream")) {
        jpipResponseFields.type = "jpp-stream";
        httpResponse.setHeaderField("Content-Type", "image/jpp-stream");
      } else {
        sendHTTPResponseError(NOT_IMPLEMENTED, "I'm sorry, by the CADI server has only implemented the image return type \"JPP-STREAM\" ");
        throw new WarningException();
      }
    } else {
      jpipResponseFields.type = session.getReturnType(cid);
      httpResponse.setHeaderField("Content-Type", "image/jpp-stream");
    }

    // Target-ID
    if (logicalTarget.getTID() != null) {
      jpipResponseFields.tid = logicalTarget.getTID();
    } else {
      if (jpipRequestFields.targetField.tid != null) {
        jpipResponseFields.tid = "0";
      }
    }


    // SET JPIP RESPONSE FIELDS	- view window
    jpipResponseFields.setViewWindow(processWOI.getResponseViewWindow());
    if (processWOI.getQuality() > 0) {
      jpipResponseFields.quality = processWOI.getQuality();
    }


    // DISPATCH REQUEST

    // Send http response header
    if (jpipRequestFields.dataLimitField.len != 0) {
      httpResponse.setHeaderField("Transfer-Encoding", "chunked");
    }
    httpResponse.setResponseCode(StatusCodes.OK);
    httpResponse.setResponseMessage("OK");
    if (httpRequest.getHeaderField("Debug") != null) {
      httpResponse.setHeaderField("Debug", httpRequest.getHeaderField("Debug"));
    }
    sendHTTPResponseHeader(httpResponse);
    httpResponse.list(System.out);


    // Send http response body
    DeliveryManager deliverData = new DeliveryManager(logicalTarget, responseDataList, httpResponseSender,
            new JPIPMessageEncoder(independentMessageHeaders, jpipRequestFields.serverControlField.useExtendedHeaders), log);
    try {
      deliverData.run();
    } catch (IOException e1) {
      if (log.isLog(CADILog.LEVEL_WARNING)) {
        log.logInfo(getName() + ": remote host (" + socket.getRemoteSocketAddress() + ") has been closed unexpectedly or it is unreachable");
      }
      throw new WarningException();
    } catch (ErrorException e1) {
      e1.printStackTrace();
    }
    if (log.isLog(CADILog.LEVEL_INFO)) {
      log.logInfo(getName() + ": response has been sent to client (" + socket.toString() + ")");
    }

    if (httpRequest.getHeaderField("Debug") != null) {
      // Client has sent a debug feature
      String uuid = httpRequest.getHeaderField("Debug");
      System.out.println("uuid=" + uuid
              + " Headers length: " + deliverData.getCumMessageHeadersLength() + " bytes\n"
              + "uuid=" + uuid
              + " Bodies length: " + deliverData.getCumMessageBodiesLength() + " bytes");
    }

    // CLOSE CHANNELS
    closeChannels(jpipRequestFields.channelField.cid, jpipRequestFields.channelField.cclose);
    /* if (!sendLogicalTargetData) {
     * httpResponse.reset();
     * httpResponse.setResponseCode(StatusCodes.OK);
     * httpResponse.setResponseMessage("Channel has been closed");
     * if (httpRequest.getHeaderField("Debug") != null) {
     * httpResponse.setHeaderField("Debug", httpRequest.getHeaderField("Debug"));
     * }
     * sendHTTPResponseHeader(httpResponse);
     * } */


    // CACHE
    if (isStatelessRequest) {
      // Clears the cache in stateless request
      listOfClientSessions.remove(cid);
    } else {
      // Keeps information about the delivered data in the cache
      jpipMessageHeaders = new ArrayList<JPIPMessageHeader>();
      for (ResponseData responseData : responseDataList) {
        if (responseData.jpipMessageHeader == null) {
          continue;
        }
        if (responseData.jpipMessageHeader.isEOR) {
          continue;
        }
        jpipMessageHeaders.add(responseData.jpipMessageHeader);
      }

      updateCache(jpipMessageHeaders);
      //System.out.println("######################################");
      //serverCache.list(System.out);	// DEBUG
      //System.out.println("######################################");
    }


    if (jpipMessageHeaders != null) {
      jpipMessageHeaders.clear();
    }
    if (responseDataList != null) {
      responseDataList.clear();
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

    //	Stateless or session request (see ISO/IEC 15444-9 section C.3.1)

    sendLogicalTargetData = false;

    if ((jpipRequestFields.channelField.cid != null)
            || (jpipRequestFields.channelField.cnew != null)) {
      // Request within a session

      isStatelessRequest = false;

      if (jpipRequestFields.channelField.cid != null) { // Client has a cid

        if (!listOfClientSessions.contains(jpipRequestFields.channelField.cid)) {
          // Client cid isn't registered in the list of registered client's session
          sendHTTPResponseError(BAD_REQUEST, "The \"channel idenfier\" used is wrong or from an old session");
          if (log.isLog(CADILog.LEVEL_DEBUG)) {
            log.logDebug(getName() + ": remote host " + socket.getRemoteSocketAddress()
                    + " has requested with the wrong channel identifier " + jpipRequestFields.channelField.cid);
          }
          throw new WarningException();
        }

        // Client has a registered session from a previous connection
        cid = jpipRequestFields.channelField.cid;

        if (jpipRequestFields.channelField.cnew != null) {	// Request for a new channel withing the same session
          sendHTTPResponseError(NOT_IMPLEMENTED, "I'm sorry, but the CADI server only support one channel by each session.");
          if (log.isLog(CADILog.LEVEL_DEBUG)) {
            log.logDebug(getName() + ": remote host " + socket.getRemoteSocketAddress()
                    + " has requested to create more than one channel in the session");
          }
          throw new WarningException();
        }

        if ((jpipRequestFields.channelField.path != null) && !jpipRequestFields.channelField.path.equals(channelFieldPath)) {
          // Check if the channel path has a correct value
          sendHTTPResponseError(NOT_IMPLEMENTED, "The \"path\" (" + jpipRequestFields.channelField.path
                  + ") sent, it is wrong or from an old session.");
          if (log.isLog(CADILog.LEVEL_DEBUG)) {
            log.logDebug(getName() + ": remote host " + socket.getRemoteSocketAddress()
                    + " has requested with a wrong channel path (" + jpipRequestFields.channelField.path + ")");
          }
          throw new WarningException();
        }

        // Get the client properties
        session = listOfClientSessions.get(cid);
        jpipRequestFields.targetField.tid = session.getTID(cid);
        serverCache = session.getCache(cid);
        jpipChannel = session.getJPIPChannel(cid);

      } else {
        // Client requests a new session

        // Seach http-tcp transports. They are not supported yet, so an http response error is sent.
        for (int i = 0; i < jpipRequestFields.channelField.cnew.size(); i++) {
          if (jpipRequestFields.channelField.cnew.get(i).equals("http-tcp")) {
            sendHTTPResponseError(NOT_IMPLEMENTED, "I'm sorry, but the CADI server only support HTTP channels.");
            if (log.isLog(CADILog.LEVEL_INFO)) {
              log.logInfo(getName() + ": remote host " + socket.getRemoteSocketAddress()
                      + " has requested for an HTTP-TCP channel");
            }
            throw new WarningException();
          }
        }

        // Creates a session
        session = new ServerClientSession();
        cid = session.createSessionTarget(logicalTarget, "jpp-stream", "http");
        listOfClientSessions.add(session);

        jpipResponseFields.cid = cid;
        jpipResponseFields.path = "CADIServer";
        jpipResponseFields.transport = JPIPResponseFields.TRANSPORT_HTTP;

        if (log.isLog(CADILog.LEVEL_INFO)) {
          log.logInfo(getName() + ": for remote host " + socket.getRemoteSocketAddress()
                  + " assigned a new session identifier cid=" + cid + " to the remote host ");
        }

      }

    } else {
      // Stateless request
      isStatelessRequest = true;
      if (log.isLog(CADILog.LEVEL_INFO)) {
        log.logInfo(getName() + ": stateless request");
      }

      // Creates a temporal session
      session = new ServerClientSession();
      cid = session.createSessionTarget(logicalTarget, "jpp-stream", "http");
      listOfClientSessions.add(session);
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

    logicalTarget = null;

    // Logical target may be specified through the target, targetID, or both.
    if ((tid != null) && (tid.compareTo("0") == 0)) {
      tid = null;
    }

    if ((target != null) && (tid != null)) { // logical target is defined by both parameters
      if (logicalTargetList.equals(target, tid)) {
        logicalTarget = logicalTargetList.getByTID(tid);
      } else {
        sendHTTPResponseError(BAD_REQUEST, "The URL logical target identifiers (target and tid) do  not identify the same logical target");
        if (log.isLog(CADILog.LEVEL_DEBUG)) {
          log.logDebug(getName() + ": remote host " + socket.getRemoteSocketAddress()
                  + " requested for logical target with: target=" + jpipRequestFields.targetField.target
                  + " and tid=" + jpipRequestFields.targetField.tid + ". They do not identify the same logical target");
        }
        throw new WarningException();
      }
    } else if (target != null) {	// logical target is defined by target parameter
      logicalTarget = logicalTargetList.getByTarget(target);
    } else if (tid != null) {	// logical target is defined by tid parameter
      logicalTarget = logicalTargetList.getByTID(tid);
    } else {	// logical target is not defined neither target nor tid
      sendHTTPResponseError(BAD_REQUEST, "The URL has not any parameter which identifies the logical target");
      throw new WarningException();
    }


    // If the logical target is not in the linked list, it will be searched in disk
    if (logicalTarget == null) {

      // FIXME: the way a new logicalTarget is added to the logicalTargetList
      // has synchronization problems. They have been detected in the CADIProxy.
      // In the ServerLogicalTargetList the add method should not be used and a
      // new one create session must be implemented as in the ProxySessionTargets.

      // Does the target exist?
      String absoluteFileName = (targetsPath.endsWith(File.separator) ? targetsPath : targetsPath + File.separator) + target;
      if (log.isLog(CADILog.LEVEL_INFO)) {
        log.logInfo("Searching in disk the target: " + absoluteFileName);	// DEBUG
      }

      BufferedDataInputStream in = null;
      try {

        in = new BufferedDataInputStream(absoluteFileName);

      } catch (FileNotFoundException e) {
        sendHTTPResponseError(NOT_FOUND, "I'm sorry, but the requested logical target \"" + jpipRequestFields.targetField.target
                + "\" is not available or you do not have permissions for reading it.");
        if (log.isLog(CADILog.LEVEL_DEBUG)) {
          log.logDebug(getName() + ": remote host " + socket.getRemoteSocketAddress()
                  + " requested for logical target " + jpipRequestFields.targetField.target + " but it is not found");
        }
        throw new WarningException();
      }

      //Check image type
      String relativeFileName = target.substring(target.lastIndexOf("/") + 1, target.length()).toUpperCase();

      if (relativeFileName.endsWith(".JPX") || relativeFileName.endsWith(".JP2") || relativeFileName.endsWith(".JPC") || relativeFileName.endsWith(".JPK")) {

        try {
          JP2KLogicalTargetIndexer jp2kLogicalTargetIndexing = new JP2KLogicalTargetIndexer(getName(), target, in, log);
          if (predictiveModel) {
            int lastPoint = absoluteFileName.lastIndexOf('.');
            jp2kLogicalTargetIndexing.readPredictiveModel(absoluteFileName.substring(0, lastPoint) + ".pm");
          }
          if ((deliveringMode == ServerDefaultValues.DELIVERING_CPI)
                  || (deliveringMode == ServerDefaultValues.DELIVERING_CoRD)) {
            jp2kLogicalTargetIndexing.setReadCodingPasses(true);
          }
          jp2kLogicalTargetIndexing.run();
          logicalTarget = jp2kLogicalTargetIndexing.getLogicalTarget();
        } catch (WarningException we) {
          if (log.isLog(CADILog.LEVEL_WARNING)) {
            log.logWarning("Logical target \"" + absoluteFileName + "\" are corrupted. Reason: " + we.getMessage());
          }
          sendHTTPResponseError(NOT_IMPLEMENTED, "I'm sorry, but the CADI server cannot support some features of the requested target.");
          throw new WarningException();
        }

      } else { // Logical target type: UNKNOWN

        sendHTTPResponseError(NOT_IMPLEMENTED, "I'm sorry, but the CADI server only support JPEG2000 image files.");
        if (log.isLog(CADILog.LEVEL_DEBUG)) {
          log.logDebug(getName() + ": remote host " + socket.getRemoteSocketAddress()
                  + " requested for logical target " + jpipRequestFields.targetField.target + " but it is not supported");
        }
        throw new WarningException();
      }

      // Update common parameters
      logicalTargetList.add(logicalTarget);

    }

    // logical target must not null
    // if not, there is an error in this method
    assert (logicalTarget != null);
  }

  /**
   * This method will be used to read a HTTP client request. It will be used
   * when the HTTP keep-alive mode is set. Then, this thread is assigned to
   * this client. The received request data will be stored in the
   * <code>
   * httpRequestFields</code> (see
   * @link CADI.Server.Scheduler#httpRequestFields}) and
   * <code>
   * jpipRequestFields</code> (see
   * @link CADI.Server.Scheduler#httpRequestFields}) attributes.
   *
   * @throws WarningException when an
   * @throws ErrorException when the client has closed the socket, the
   * socket reaches its timeout, or an I/O error has occured while client
   * request was being received.
   */
  private void listenNewRequest() throws WarningException, ErrorException {

    HTTPRequestReader httpRequestReader = new HTTPRequestReader();

    if (socket.isInputShutdown()) {
      throw new ErrorException();
    }

    // Set keep alive timeout
    if (keepAliveTimeout >= 0) {
      try {
        socket.setSoTimeout(keepAliveTimeout);
      } catch (SocketException e) {
        if (log.isLog(CADILog.LEVEL_WARNING)) {
          log.logWarning(getName() + ": keep alive time out cannot be set");
        }
        throw new ErrorException();
      }
    }

    // Wait for client connections or until timeout is reached
    try {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      httpRequestReader.readHTTPRequest(bufferedReader);
    } catch (ErrorException ee) {
      if (log.isLog(CADILog.LEVEL_INFO)) {
        log.logInfo(getName() + ": malformed URL");
      }
      throw new ErrorException(ee.getMessage(), ee.getErrorCode());
    } catch (SocketTimeoutException ste) {
      if (log.isLog(CADILog.LEVEL_INFO)) {
        log.logInfo(getName() + ": keep alive timeout has been reached");
      }
      throw new ErrorException();
    } catch (IOException e) {
      if (log.isLog(CADILog.LEVEL_INFO)) {
        log.logInfo(getName() + ": remote host (" + socket.getRemoteSocketAddress() + ") has been closed unexpectedly");
      }
      throw new ErrorException();
    }

    httpRequest = httpRequestReader.getHTTPRequest();
    String requestLine = httpRequest.getRequestURI();
    //Hashtable<String, String> headers = httpRequest.getHTTPHeaders();


    if (requestLine == null) {
      throw new ErrorException();
    }

    if (log.isLog(CADILog.LEVEL_INFO)) {
      log.logInfo(getName() + "(Received request):\n" + (socket + "\n" + httpRequestReader.toString()));
    }

    //NOTE: HTTP headers must be parsed here

    // Parses JPIP request and get JPIP fields
    JPIPRequestDecoder jpipRequestDecoder = new JPIPRequestDecoder();
    jpipRequestDecoder.decoder(httpRequest.getRequestURI());
    jpipRequestFields = jpipRequestDecoder.getJPIPRequestFields();
  }

  /**
   *
   * @param httpResponse
   *
   * @throws WarningException
   */
  private void sendHTTPResponseHeader(HTTPResponse httpResponse) throws WarningException {

    assert ((httpResponse.getResponseCode() == StatusCodes.OK)
            || (httpResponse.getResponseCode() == StatusCodes.ACCEPTED)
            || (httpResponse.getResponseCode() == StatusCodes.BAD_REQUEST)
            || (httpResponse.getResponseCode() == StatusCodes.NOT_FOUND)
            || (httpResponse.getResponseCode() == StatusCodes.UNSUPPORTED_MEDIA_TYPE)
            || (httpResponse.getResponseCode() == StatusCodes.NOT_IMPLEMENTED)
            || (httpResponse.getResponseCode() == StatusCodes.SERVICE_UNAVAILABLE));

    encodeJPIPResponseFields();


    if (log.isLog(CADILog.LEVEL_INFO)) {
      log.logInfo(getName() + ": HTTP Response header:\n" + httpResponse.toString());
    }

    try {
      httpResponseSender.sendHeaders(httpResponse);
      httpResponseSender.endOfHeaders();
    } catch (IOException e) {
      if (log.isLog(CADILog.LEVEL_INFO)) {
        log.logInfo(getName() + ": remote host (" + socket.getRemoteSocketAddress() + ") has been closed unexpectedly or it is unreachable");
      }
      throw new WarningException();
    }
  }

  /**
   * This method will be used to send an HTTP error response to the client.
   *
   * @param statusCode definition in {@link CADI.Common.Network.HTTP.StatusCodes}.
   * @param reasonPhrase a reason phrase related with the status code.
   */
  private void sendHTTPResponseError(int statusCode, String reasonPhrase) {
    httpResponse.reset();
    httpResponse.setResponseCode(statusCode);
    httpResponse.setResponseMessage(reasonPhrase);
    try {
      sendHTTPResponseHeader(httpResponse);
    } catch (WarningException e) {
    }

    httpResponse.list(System.out);
  }

  /**
   * JPIP HEADERS (See ISO/IEC 15444-9 Annex D)
   */
  private void encodeJPIPResponseFields() {

    if (jpipResponseFields.tid != null) {
      httpResponse.setHeaderField("JPIP-tid", jpipResponseFields.tid);
    }

    if (jpipResponseFields.cid != null) {
      String jpipCnew = "cid=" + jpipResponseFields.cid;
      if (jpipResponseFields.transport != -1) {
        jpipCnew += ",transport=";
        if (jpipResponseFields.transport == JPIPResponseFields.TRANSPORT_HTTP) {
          jpipCnew += "http";
        } else if (jpipResponseFields.transport == JPIPResponseFields.TRANSPORT_HTTP_TCP) {
          jpipCnew += "http-tcp";
        }
      }
      if (jpipResponseFields.host != null) {
        jpipCnew += ",host=" + jpipResponseFields.host;
      }
      if (jpipResponseFields.path != null) {
        jpipCnew += ",path=" + jpipResponseFields.path;
      }
      if (jpipResponseFields.port != -1) {
        jpipCnew += ",port=" + jpipResponseFields.port;
      }
      if (jpipResponseFields.auxport != -1) {
        jpipCnew += ",auxport=" + jpipResponseFields.auxport;
      }
      httpResponse.setHeaderField("JPIP-cnew", jpipCnew);
    }

    if (jpipResponseFields.qid != -1) {
      httpResponse.setHeaderField("JPIP-qid", Integer.toString(jpipResponseFields.qid));
    }

    int[] fsiz = jpipResponseFields.fsiz;
    if (fsiz != null) {
      if ((fsiz[0] != -1) && (fsiz[1] != -1)) {
        httpResponse.setHeaderField("JPIP-fsiz", fsiz[0] + "," + fsiz[1]);
      }
    }
    fsiz = null;

    int[] rsiz = jpipResponseFields.rsiz;
    if (rsiz != null) {
      if ((rsiz[0] != -1) && (rsiz[1] != -1)) {
        httpResponse.setHeaderField("JPIP-rsiz", rsiz[0] + "," + rsiz[1]);
      }
    }
    rsiz = null;

    int[] roff = jpipResponseFields.roff;
    if (roff != null) {
      if ((roff[0] != -1) && (roff[1] != -1)) {
        httpResponse.setHeaderField("JPIP-roff", roff[0] + "," + roff[1]);
      }
    }
    roff = null;

    int[][] comps = jpipResponseFields.comps;
    if (comps != null) {
      String jpipComps = "";
      for (int i = 0; i < comps.length; i++) {
        jpipComps += comps[i][0];
        if (comps[i][1] >= 0) {
          jpipComps += "-" + comps[i][1];
        }
        if (i < comps.length - 1) {
          jpipComps += ",";
        }
      }
      httpResponse.setHeaderField("JPIP-comps", jpipComps);
    }
    comps = null;

    if (jpipResponseFields.layers != -1) {
      httpResponse.setHeaderField("JPIP-layers", Integer.toString(jpipResponseFields.layers));
    }

    if (jpipResponseFields.srate > 0.0) {
      httpResponse.setHeaderField("JPIP-srate", Double.toString(jpipResponseFields.srate));
    }

    if (jpipResponseFields.quality != -1) {
      httpResponse.setHeaderField("JPIP-quality", Integer.toString(jpipResponseFields.quality));
    }

    if (jpipResponseFields.type != null) {
      httpResponse.setHeaderField("JPIP-type", jpipResponseFields.type);
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
      if (log.isLog(CADILog.LEVEL_INFO)) {
        log.logInfo(getName() + "BAD_REQUEST: The request has some incompatible JPIP fields, wrong combinatio of target and subtarget parameters.");
      }
      sendHTTPResponseError(BAD_REQUEST, "The request has some incompatible JPIP fields, wrong combinatio of target and subtarget parameters.");
      throw new WarningException();
    }

    // Logical target is specified throw one of:
    // 1- a combination of Target, Sub-target and Target ID
    // 2- Channel ID
    // See ISO/IEC 15444-9 section C.2.1
    if (((jpipRequestFields.targetField.target != null) || (jpipRequestFields.targetField.tid != null))
            && (jpipRequestFields.channelField.cid != null)) {
      System.out.println("=== ERROR ===");
      jpipRequestFields.list(System.out);
      if (log.isLog(CADILog.LEVEL_INFO)) {
        log.logInfo(getName() + "BAD_REQUEST: The request has some incompatible JPIP fields, wrong combination of target, tid, and cid parameters.");
      }
      sendHTTPResponseError(BAD_REQUEST, "The request has some incompatible JPIP fields, wrong combination of target, tid, and cid parameters.");
      throw new WarningException();
    }

    // The Offset field is valid only in conjunction with the Frame Size.
    // See ISO/IEC 15444-9 section C.4.3
    if ((jpipRequestFields.viewWindowField.fsiz == null) && (jpipRequestFields.viewWindowField.roff != null)) {
      if (log.isLog(CADILog.LEVEL_INFO)) {
        log.logInfo(getName() + "BAD_REQUEST: The request has some incompatible JPIP fields, wrong combinatio of fsiz and roff parameters.");
      }
      sendHTTPResponseError(BAD_REQUEST, "The request has some incompatible JPIP fields, wrong combinatio of fsiz and roff parameters.");
      throw new WarningException();
    }

    // The Region Size field is valid only in conjunction with the Frame Size.
    // See ISO/IEC 15444-9 section C.4.4
    if ((jpipRequestFields.viewWindowField.fsiz == null) && (jpipRequestFields.viewWindowField.rsiz != null)) {
      if (log.isLog(CADILog.LEVEL_INFO)) {
        log.logInfo(getName() + "BAD_REQUEST: The request has some incompatible JPIP fields, wrong combinatio of fsiz and rsiz parameters.");
      }
      sendHTTPResponseError(BAD_REQUEST, "The request has some incompatible JPIP fields, wrong combinatio of fsiz and rsiz parameters.");
      throw new WarningException();
    }


    // CHANNEL CLOSE FIELD
    // See ISO/IEC 15444-9 section C.3.4
    if (jpipRequestFields.channelField.cclose != null) {

      // Wildcard must be associate with a channel identifier
      if (jpipRequestFields.channelField.cclose.get(0).equals("*") && (jpipRequestFields.channelField.cid == null)) {
        sendHTTPResponseError(BAD_REQUEST, "All channel close is requested but channel identifier is not available");
        throw new WarningException();
      }

      if (jpipRequestFields.channelField.cclose.get(0).equals("*") && !listOfClientSessions.contains(jpipRequestFields.channelField.cid)) {
        sendHTTPResponseError(BAD_REQUEST, "All channel close is requested but channel identifier is wrong");
        throw new WarningException();
      }

      // Check if all the cclose identifiers belongs to the same session
      String cid = (jpipRequestFields.channelField.cid == null) ? jpipRequestFields.channelField.cid : jpipRequestFields.channelField.cclose.get(0);

      if (!((jpipRequestFields.channelField.cclose.size() == 1) && jpipRequestFields.channelField.cclose.get(0).equals("*"))) {
        for (int i = 0; i < jpipRequestFields.channelField.cclose.size(); i++) {
          if (!listOfClientSessions.belongs(cid, jpipRequestFields.channelField.cclose.get(i))) {
            sendHTTPResponseError(BAD_REQUEST, "Channel close identifier do not belong to the same session.");
            throw new WarningException();
          }
        }
      }
    }

    // CHECK WOI RESTRICTIONS
    if ((jpipRequestFields.viewWindowField.fsiz[0] > 0)
            && (jpipRequestFields.viewWindowField.roff[0] > jpipRequestFields.viewWindowField.fsiz[0])) {
      sendHTTPResponseError(BAD_REQUEST, "Region offset cannot be greater than frame size.");
      throw new WarningException();
    }
    if ((jpipRequestFields.viewWindowField.fsiz[1] > 0)
            && (jpipRequestFields.viewWindowField.roff[1] > jpipRequestFields.viewWindowField.fsiz[1])) {
      sendHTTPResponseError(BAD_REQUEST, "Region offset cannot be greater than frame size.");
      throw new WarningException();
    }

  }

  /**
   * This method is used to close session channels.
   *
   * @param cid definition in {@link CADI.Common.Network.JPIP.ChannelField#cid}
   * @param cclose definition in {@link CADI.Common.Network.JPIP.ChannelField#cclose}
   */
  private void closeChannels(String cid, ArrayList<String> cclose) {

    if (cclose == null) {
      return;
    }

    // Wildcard (all channels)
    if (cclose.get(0).equals("*")) {
      //listOfClientSessions.closeChannel(cid);
      listOfClientSessions.remove(cid);
    }

    // A list of channel identifiers
    for (String cidToClose : cclose) {
      session.closeChannel(cidToClose);
    }
  }

  /**
   * This method is used to update the cache with the data that has been sent
   * to the client. Therefore, it must be called after the response has been
   * sent to the client.
   *
   * @param jpipMessageData an array list with the data that has been sent.
   */
  private void updateCache(ArrayList<JPIPMessageHeader> jpipMessageData) {

    int index = 0;
    JPIPMessageHeader jpipMessageHeader = null;

    int numOfMessages = jpipMessageData.size();
    while (index < numOfMessages) {

      // Read JPIP Message Header
      jpipMessageHeader = jpipMessageData.get(index++);
      serverCache.update(true, (jpipMessageHeader.classIdentifier / 2) * 2, jpipMessageHeader.inClassIdentifier,
              (int) (jpipMessageHeader.msgOffset + jpipMessageHeader.msgLength));
      jpipMessageHeader = null;

    }
  }

  /**
   * Gets the available image return types.
   *
   * @return returns an one-dimension array with the available return types.
   */
  private String[] availableReturnTypes() {
    String[] returnTypes = null;

    // Return types in the HTTP Accept field
    String accept = this.httpRequest.getHeaderField("Accept");
    int numReturnTypesAccept = 0;
    String[] acceptTypes = null;
    if (accept != null) {
      acceptTypes = accept.split(",");
      numReturnTypesAccept += acceptTypes.length;
    }

    int numReturnTypesSCF = jpipRequestFields.serverControlField.type != null
            ? jpipRequestFields.serverControlField.type.size() : 0;

    returnTypes = new String[numReturnTypesSCF + numReturnTypesAccept];

    for (int i = 0; i < numReturnTypesSCF; i++) {
      returnTypes[i] = jpipRequestFields.serverControlField.type.get(i);
    }

    for (int i = 0; i < numReturnTypesAccept; i++) {
      returnTypes[numReturnTypesSCF + i] = acceptTypes[i];
    }

    return returnTypes;
  }

  /**
   * Set the time attributes to 0.
   */
  private void resetTime() {
    initTime = 0;
    initStageTime = 0;
  }

  /**
   * Show some time and memory usage statisticals.
   *
   * @param stage string that will be displayed
   */
  private String showTimeMemory(String stage) {

    String str = "";

    long actualTime = System.currentTimeMillis();
    if (initTime == 0) {
      initTime = actualTime;
    }
    if (initStageTime == 0) {
      initStageTime = actualTime;
    }

    //print times are not considered
    long totalMemory = Runtime.getRuntime().totalMemory() / 1048576;
    long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
    //((float) initTime - (float) actualTime)
    String durationStage = Float.toString((actualTime - initStageTime) / 1000F) + "000";
    durationStage = durationStage.substring(0, durationStage.lastIndexOf(".") + 4);
    String duration = Float.toString((actualTime - initTime) / 1000F) + "000";
    duration = duration.substring(0, duration.lastIndexOf(".") + 4);

    str += "STAGE: " + stage + "\n";
    str += "  Memory (USED/TOTAL): " + usedMemory + "/" + totalMemory + " MB" + "\n";
    str += "  Time (USED/TOTAL)  : " + durationStage + "/" + duration + " secs" + "\n";

    initStageTime = System.currentTimeMillis();

    return str;
  }
}
