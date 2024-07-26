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

import CADI.Common.Log.CADILog;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Common.Util.StopWatch;
import CADI.Proxy.Client.ProxySessionTarget;
import GiciException.ErrorException;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/06/19
 */
public class FetchTarget implements Runnable {

  /**
   *
   */
  private ViewWindowField viewWindow = null;

  /**
   *
   */
  private CADILog log = null;

  /**
   *
   */
  private ProxySessionTarget proxySessionTarget = null;

  /**
   *
   */
  private StopWatch stopWatch = null;

  /**
   *
   */
  private String clientUserAgent = null;

  private boolean recordWOIHistory = false;

  private String debug = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   * 
   * @param log 
   */
  public FetchTarget(CADILog log) {
    if (log == null) throw new NullPointerException();

    this.log = log;

    stopWatch = new StopWatch();
  }

  /**
   * 
   * @param proxySessionTarget
   * @param viewWindow
   * @param clientUserAgent
   * @param debug 
   */
  public synchronized void initialize(ProxySessionTarget proxySessionTarget,
          ViewWindowField viewWindow, String clientUserAgent, String debug) {
    // Check input parameters
    if (proxySessionTarget == null) {
      throw new NullPointerException();
    }
    if (viewWindow == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.proxySessionTarget = proxySessionTarget;
    this.viewWindow = viewWindow;
    this.clientUserAgent = clientUserAgent;
    this.debug = debug;
  }

  /**
   *
   * @param clientUserAgent
   */
  public void setDebug(String debug) {
    this.debug = debug;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {

    //stopWatch.reset();
    //stopWatch.start();

    proxySessionTarget.lock();
    proxySessionTarget.resetJPIPMessagesCounters();
    try {
      proxySessionTarget.reuseCache(true);
      //proxySessionTarget.setCacheType(BinDescriptor.EXPLICIT_FORM);
      //proxySessionTarget.setUseNumberOfBytes(true);
      //proxySessionTarget.setUseNumberOfLayers(true);
      proxySessionTarget.setUseHTTPSession(true);
      proxySessionTarget.setUserAgent(clientUserAgent);
      proxySessionTarget.setUserAgent("CADI Proxy");
      if (debug != null) proxySessionTarget.setDebug(debug);

      proxySessionTarget.fetchWindow(viewWindow, recordWOIHistory);
    } catch (ErrorException e1) {
      e1.printStackTrace();
    } finally {
      proxySessionTarget.setDebug(null);
      proxySessionTarget.unlock();
    }

    //System.out.println("--- Fetch Target ---");
    //System.out.println("Thread: " + Thread.currentThread().getName());
    //System.out.println(viewWindow.toString());
    ///System.out.println("Headers: " + proxySessionTarget.getBytesJPIPMessageHeader()+ "\t Body: " + proxySessionTarget.getBytesJPIPMessageBody());
    //System.out.println("---------------------");
    //stopWatch.stop();
      /* System.out.println(clientUserAgent+" ----------------------\n"
     * + clientUserAgent+" Time stamp: "+System.currentTimeMillis()+"\n"
     * + clientUserAgent+" Title: "+getName()+"\n"
     * + clientUserAgent+" Client: "+clientUserAgent+"\n"
     * + clientUserAgent+" Requested WOI: "+viewWindow.toString()+"\n"
     * + clientUserAgent+" Elapsed time: "+stopWatch.getTime()+"(ms)\n"
     * + clientUserAgent+" Data received: header="
     * +proxySessionTarget.getBytesJPIPMessageHeader()
     * +" body="+proxySessionTarget.getBytesJPIPMessageBody()+" (bytes)\n"
     * +clientUserAgent+" ----------------------\n"
     * ); */
    //}
  }
  // ============================ private methods ==============================
}
