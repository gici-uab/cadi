/*
 * CADI Software - a JPIP Client/Server framework
 * Copyright (C) 2007-2012  Group on Interactive Coding of Images (GICI)
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Group on Interactive Coding of Images (GICI) Department of Information and
 * Communication Engineering Autonomous University of Barcelona 08193 -
 * Bellaterra - Cerdanyola del Valles (Barcelona) Spain
 *
 * http://gici.uab.es gici-info@deic.uab.es
 */
package CADI.Proxy.Core;

import CADI.Common.Core.Prefetching;
import java.io.PrintStream;

import CADI.Common.Log.CADILog;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Proxy.Client.ProxySessionTarget;
import CADI.Proxy.Client.ProxySessionTargets;
import CADI.Proxy.Server.ProxyClientSession;
import CADI.Proxy.Server.ProxyClientSessions;
import java.util.ArrayList;

/**
 * This class inherits the {@link CADI.Common.Core.Prefetching} class in order
 * to show a more accurate interface to the Proxy package.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.4 2011/12/03
 */
public class ProxyPrefetching extends Prefetching {

  /**
   *
   */
  protected ProxyClientSessions listOfClientSessions = null;

  /**
   * Definition in {@link CADI.Proxy.Proxy#logicalTargetList}.
   */
  protected ProxySessionTargets proxySessionTargets = null;

  /**
   * Performs the prefetching over the bounding box of the historic wois
   * requested on an image by all clients.
   */
  public static final int MODE_ONLY_IMAGE_HISTORY = 1;

  /**
   *
   */
  public static final int MODE_ONLY_CLIENT_HISTORY = 2;

  /**
   *
   */
  public static final int MODE_LAST_WOI_ALL_CLIENTS = 3;

  /**
   * Allowed values are:<br>
   * {@link #MODE_ONLY_IMAGE_HISTORY}
   * {@link #MODE_ONLY_CLIENT_HISTORY}
   * {@link #MODE_CLIENT_IMAGE_HISTORY}
   */
  protected int prefetchingDataHistory = MODE_ONLY_IMAGE_HISTORY;

  // ============================= public methods ==============================
  /**
   * This constructor is not allowed.
   */
  public ProxyPrefetching() {
    throw new UnsupportedOperationException();
  }

  /**
   * Constructor.
   * 
   * @param threadName definition in {@link java.lang.Thread#name}.
   * @param proxySessionTargets definition in {@link #proxySessionTargets}.
   * @param listOfClientSessions definition in {@link #listOfClientSessions}.
   * @param prefSemaphore definition in {@link #prefSemaphore}.
   * @param log definition in {@link #log}.
   */
  public ProxyPrefetching(
          String threadName,
          ProxySessionTargets proxySessionTargets,
          ProxyClientSessions listOfClientSessions,
          ProxyPrefSemaphore prefSemaphore, CADILog log) {
    this(threadName, proxySessionTargets, listOfClientSessions, prefSemaphore,
         log, MODE_ONLY_IMAGE_HISTORY, WOI_TYPE_BOUNDING_BOX);
  }

  /**
   * Constructor.
   *
   * @param threadName definition in {@link java.lang.Thread#name}.
   * @param proxySessionTargets definition in {@link #proxySessionTargets}.
   * @param listOfClientSessions definition in {@link #listOfClientSessions}.
   * @param prefSemaphore definition in {@link #prefSemaphore}.
   * @param log definition in {@link #log}.
   * @param prefetchingDataHistory definition in {@link #prefetchingDataHistory}.
   * @param prefetchingWOIType definition in {@link #prefetchingWOIType}.
   */
  public ProxyPrefetching(
          String threadName,
          ProxySessionTargets proxySessionTargets,
          ProxyClientSessions listOfClientSessions,
          ProxyPrefSemaphore prefSemaphore, CADILog log,
          int prefetchingDataHistory, int prefetchingWOIType) {

    this(threadName, proxySessionTargets, listOfClientSessions,
         prefSemaphore, log, prefetchingDataHistory, prefetchingWOIType,
         null);
  }

  /**
   * Constructor.
   *
   * @param threadName definition in {@link java.lang.Thread#name}.
   * @param proxySessionTargets definition in {@link #proxySessionTargets}.
   * @param listOfClientSessions definition in {@link #listOfClientSessions}.
   * @param prefSemaphore definition in {@link #prefSemaphore}.
   * @param log definition in {@link #log}.
   * @param prefetchingDataHistory definition in {@link #prefetchingDataHistory}.
   * @param prefetchingWOIType definition in {@link #prefetchingWOIType}.
   * @param movementProbabilities definition in {@link #movements}.
   */
  public ProxyPrefetching(
          String threadName,
          ProxySessionTargets proxySessionTargets,
          ProxyClientSessions listOfClientSessions,
          ProxyPrefSemaphore prefSemaphore, CADILog log,
          int prefetchingDataHistory, int prefetchingWOIType,
          float[] movementProbabilities) {

    super(threadName, prefSemaphore, log, prefetchingWOIType, movementProbabilities);

    // Check input parameters
    if (proxySessionTargets == null) {
      throw new NullPointerException();
    }
    if (listOfClientSessions == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.proxySessionTargets = proxySessionTargets;
    this.listOfClientSessions = listOfClientSessions;
    this.prefetchingDataHistory = prefetchingDataHistory;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {

    this.movements.list(System.err);

    while (!finish) { // main loop

      recomputeWOI = false;
      prefSemaphore.waitChanges();
      prefSemaphore.await();

      if (proxySessionTargets.isEmpty()) {
        // do nothing
        continue;
      }

      ArrayList<ViewWindowField> woiHistory = null;
      if (prefetchingDataHistory == MODE_ONLY_IMAGE_HISTORY) {
        // Performs the prefetching over the bounding box of the historic wois
        // requested on an image by all clients.

        // Loop on logical targets
        for (String sid : proxySessionTargets.SIDKeyList()) {
          if (recomputeWOI) {
            break;
          }

          prefSemaphore.await();

          // Get a logical target
          ProxySessionTarget proxySessionTarget = proxySessionTargets.getBySID(sid);
          woiHistory = proxySessionTarget.getWOIHistory();
          if (woiHistory.isEmpty()) {
            continue;
          }

          doPrefetching(woiHistory, proxySessionTarget,
                        proxySessionTarget.getCodestream(),
                        proxySessionTarget.getCacheManagement(),
                        proxySessionTarget.getPredictiveModel());
        }

      } else if (prefetchingDataHistory == MODE_ONLY_CLIENT_HISTORY) {

        // Loop on client sessions
        for (String sessionID : listOfClientSessions.getKeySet()) {
          if (recomputeWOI) {
            break;
          }
          ProxyClientSession proxyClientSession = listOfClientSessions.getBySID(sessionID);

          // Loop on client session's targets
          for (String tid : proxyClientSession.tidKeySet()) {
            if (recomputeWOI) {
              break;
            }
            woiHistory = proxyClientSession.getWOIHistoryByTID(tid);
            if (woiHistory.isEmpty()) {
              continue;
            }

            ProxySessionTarget proxySessionTarget = proxySessionTargets.getByProxyTID(tid);

            doPrefetching(woiHistory, proxySessionTarget,
                          proxySessionTarget.getCodestream(),
                          proxySessionTarget.getCacheManagement(),
                          proxySessionTarget.getPredictiveModel());
          }
        }

      } else if (prefetchingDataHistory == MODE_LAST_WOI_ALL_CLIENTS) {
        //System.out.println("\tMode: last woi all clients");

        // Loop on targets
        for (String proxyTID : proxySessionTargets.ProxyTIDKeyList()) {
          if (recomputeWOI) {
            break;
          }
          //System.out.println("\nProxyTID=" + proxyTID); // DEBUG
          ProxySessionTarget proxySessionTarget = proxySessionTargets.getByProxyTID(proxyTID);
          prefSemaphore.await();

          woiHistory = new ArrayList<ViewWindowField>();
          //ProxyJPEG2KCodestream codestream = proxySessionTarget.getJP2KProxyLogicalTarget().getCodestream(0);


          // Find clients that have requested on this target
          for (String sessionID : listOfClientSessions.getKeySet()) {
            if (recomputeWOI) {
              break;
            }
            //System.out.println("\tSessionID=" + sessionID); // DEBUG
            ProxyClientSession proxyClientSession = listOfClientSessions.getBySID(sessionID);

            // Check if this session includes the target
            if (!proxyClientSession.containsTID(proxyTID)) {
              continue;
            }

            ViewWindowField woi = proxyClientSession.getLastWOIHistoryByTID(proxyTID);
            if (woi == null) {
              continue;
            }

            if ((woi.fsiz[0] < 0) && (woi.fsiz[1] < 0)
                    && (woi.roff[0] < 0) && (woi.roff[1] < 0)
                    && (woi.rsiz[0] < 0) && (woi.rsiz[1] < 0)) {
              // Maybe the client has requested by headers or metadata
              continue;
            }

            woiHistory.add(woi);
          }

          if (woiHistory.isEmpty()) {
            continue;
          }

          doPrefetching(woiHistory, proxySessionTarget,
                        proxySessionTarget.getCodestream(),
                        proxySessionTarget.getCacheManagement(),
                        proxySessionTarget.getPredictiveModel());

        }

      } else {
        assert (true);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";
    str += super.toString();

    str += "]";

    return str;
  }

  /**
   * Prints this Proxy Prefetching out to the specified output stream. This
   * method is useful for debugging.
   *
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Proxy Prefetching --");
    super.list(out);
    out.flush();
  }
}
