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
package CADI.Proxy;

import java.io.PrintStream;

import CADI.Common.Log.CADILog;
import CADI.Proxy.Client.ProxySessionTargets;
import CADI.Proxy.Core.CachedProxyWorker;
import CADI.Proxy.Core.ProxyPrefSemaphore;
import CADI.Proxy.Core.ProxyPrefetching;
import CADI.Proxy.Core.ProxyWorker;
import CADI.Proxy.Core.TransparentProxyWorker;
import CADI.Proxy.Server.ProxyClientSessions;
import CADI.Common.Network.TrafficShaping;
import CADI.Server.Request.RequestListener;
import CADI.Server.Request.RequestQueue;
import GiciException.ErrorException;

/**
 * This class 
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1 2010/01/24
 */
public class Proxy {

  /**
   * Indicates the type of proxy that will be used.
   * <p>
   * Further information, see {@link CADI.Proxy.ProxyDefaultValues#PROXY_TYPE}.
   */
  private int proxyType = ProxyDefaultValues.PROXY_TYPE;

  /**
   *
   */
  private int prefetchingDataHistory = ProxyPrefetching.MODE_ONLY_IMAGE_HISTORY;

  /**
   *
   */
  private int prefetchingWOIType = ProxyPrefetching.WOI_TYPE_WEIGHTED_WOI;

  /**
   * Ports where the server is listening to the client requests.
   */
  private int[] ports;

  /**
   * Indicates the number of threads of the <code>Worker</code> that will
   * be running.
   */
  private int numOfWorkers = 0;

  /**
   * 
   */
  private long maxTxRate = 0L;

   /**
   *
   */
  private int trafficShaping = TrafficShaping.NONE;

  /**
   * It is the file name where the server logs are stored.
   */
  private String logFile = null;

  /**
   * Indicates whether the log information is stored in XML format or simple
   * text format.
   */
  private boolean XMLLogFormat;

  /**
   * Indicates whether the log is enabled or disabled.
   */
  private boolean logEnabled;

  /**
   * Definition in {@link  CADI.Proxy.Core.Movements}.
   */
  private float[] movementProbabilities = null;
  
  /**
   * Is the path where semantic files are located.
   */
  private String predictiveModel = null;

  // INTERNAL ATTRIBUTES
  /**
   * This thread pool will listen to in a port to receive client requests.
   */
  private RequestListener[] requestListenersPool = null;

  /**
   * It is a queue where the client request are stored. This queue is shared
   * memory between the <data>daemon</data> which stored the client requests
   * and the <data>RequestDispatcher</data> which gets them to process.
   */
  private RequestQueue requestQueue = null;

  /**
   * This thread pool will process the client requests and will send the
   * server responses to the client.
   */
  private ProxyWorker[] workersPool = null;

  /**
   *
   */
  private ProxyPrefetching proxyPrefetching = null;

  /**
   * It is an object that will be used to log the server process
   */
  private CADILog log = null;

  /**
   * Is the proxy's cache where the logical targets will be cached.
   */
  private ProxySessionTargets proxySessionTargets = null;

  /**
   *
   */
  private ProxyClientSessions listOfClientSessions = null;

  /**
   * Obs: the name of the object is not appropriate. Something like
   * prefetchingMutex or blockPrefectching could be better.
   */
  private ProxyPrefSemaphore proxyMutex = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param ports
   * @param numOfWorkers
   * @param logEnabled
   * @param logLevel
   * @param logFile
   * @param XMLLogFormat
   */
  public Proxy(int proxyType, int[] ports, int numOfWorkers, boolean logEnabled,
               int logLevel, String logFile, boolean XMLLogFormat) {

    // Check input parameters
    if (ports == null) {
      throw new NullPointerException();
    }
    if (numOfWorkers <= 0) {
      throw new IllegalArgumentException();
    }

    // Copy parameters
    this.ports = ports;
    this.numOfWorkers = numOfWorkers;
    this.logEnabled = logEnabled;
    this.logFile = logFile;
    this.XMLLogFormat = XMLLogFormat;

    // Initializations
    requestQueue = new RequestQueue();
    workersPool = new ProxyWorker[numOfWorkers];
    requestListenersPool = new RequestListener[ports.length];
    proxySessionTargets = new ProxySessionTargets();
    listOfClientSessions = new ProxyClientSessions();
    proxyMutex = new ProxyPrefSemaphore();

    // Creates CADI proxy log
    log = new CADILog(logFile, XMLLogFormat);
    log.setEnabled(logEnabled);
    log.setLogLevel(logLevel);
  }

  /**
   * Sets the {@link #proxyType} attribute.
   *
   * @param proxyType definition in {@link #proxyType}.
   */
  public void setProxyType(int proxyType) {
    this.proxyType = proxyType;
  }

  /**
   * Sets the {@link #prefetchingDataHistory} attribute.
   *
   * @param prefetchingModel definition in {@link #prefetchingDataHistory}.
   */
  public void setPrefetchingDataHistory(int prefetchingDataHistory) {
    this.prefetchingDataHistory = prefetchingDataHistory;
  }

  /**
   * Sets the {@link #prefetchingWOIType} attribute.
   *
   * @param woiExtension definition in {@link #prefetchingWOIType}.
   */
  public void setPrefetchingWOIType(int prefetchingWOIType) {
    this.prefetchingWOIType = prefetchingWOIType;
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

  public void setMovementProbabilities(float[] movProbs) {
    this.movementProbabilities = movProbs;
  }
  
  public void setPredictiveModel(String predModel) {
    this.predictiveModel = predModel;
  }

  /**
   *
   *
   */
  public void run() {

    log.logInfo("CADI Server starting ...");

    // Launch listeners and workers
    try {
      launchListeners();
      launchProxyWorkers();
      if (proxyType == ProxyDefaultValues.CACHED_PREFETCHING_PROXY) {
        launchProxyPrefetching();
      }
    } catch (ErrorException ee) {
      // Stop listeners and workers
      return;
    }
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";

    str += "]";
    return str;
  }

  /**
   * Prints this Proxy out to the specified output stream. This method
   * is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Proxy --");

    out.println("Proxy type: " + proxyType);
    out.print("Ports: ");
    for (int port : ports) {
      out.print(" " + port);
    }
    out.println();
    out.println("Num. proxy workers: " + numOfWorkers);
    out.println("Log file: " + logFile);
    out.println("\tEnabled: " + logEnabled);
    out.println("\tXML format" + XMLLogFormat);
    out.print("Request listeners pool: ");
    for (RequestListener listener : requestListenersPool) {
      listener.list(out);
    }
    out.println();
    out.println("Request queue: ");
    requestQueue.list(out);
    out.print("Proxy worker: ");
    for (ProxyWorker worker : workersPool) {
      worker.list(out);
    }
    out.println();
    out.println("Proxy prefetching: ");
    proxyPrefetching.list(out);
    out.println("Log: ");
    log.list(out);
    out.println("Proxy session targets: ");
    proxySessionTargets.list(out);
    out.println("Proxy mutex: ");
    proxyMutex.list(out);

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * This method creates and launches the proxy listeners.
   *
   * @throws ErrorException
   */
  private void launchListeners() throws ErrorException {
    for (int i = 0; i < ports.length; i++) {
      requestListenersPool[i] = new RequestListener("Listener-" + i, ports[i],
              requestQueue, log);
      requestListenersPool[i].start();
    }
  }

  /**
   * This method creates and launches the proxy workers.
   */
  private void launchProxyWorkers() {

    for (int i = 0; i < numOfWorkers; i++) {
      if (proxyType == ProxyDefaultValues.TRANSPARENT_PROXY) {
        workersPool[i] = new TransparentProxyWorker("ProxyWorker-" + i,
                requestQueue, log);
        workersPool[i].setMaxTxRate(maxTxRate);
        workersPool[i].start();
      } else {
        workersPool[i] = new CachedProxyWorker("ProxyWorker-" + i, requestQueue,
                proxySessionTargets, listOfClientSessions,
                proxyMutex, log);
        workersPool[i].setMaxTxRate(maxTxRate);
        ((CachedProxyWorker)workersPool[i]).setPredictiveModel(predictiveModel);
        workersPool[i].start();
      }
    }
  }

  /**
   * This method creates and launches the proxy prefetching module.
   */
  private void launchProxyPrefetching() {
    proxyPrefetching = new ProxyPrefetching("ProxyPrefetching",
            proxySessionTargets,
            listOfClientSessions,
            proxyMutex, log,
            prefetchingDataHistory,
            prefetchingWOIType,
            movementProbabilities);

    proxyPrefetching.setPriority(Thread.MIN_PRIORITY);
    proxyPrefetching.start();
  }
}
