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

import CADI.Client.ClientLogicalTarget.JPEG2000.ClientJPEG2KCodestream;
import CADI.Client.Session.ClientSessionTarget;
import CADI.Common.Core.PrefSemaphore;
import CADI.Common.Core.Prefetching;
import CADI.Common.Log.CADILog;
import CADI.Common.Network.JPIP.ViewWindowField;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/11/03
 */
public class ClientPrefetching extends Prefetching {

  HashMap<String, ClientSessionTarget> clientSessionTargets = null;

  // ============================= public methods ==============================
  /**
   * This class constructor is not allowed.
   */
  public ClientPrefetching() {
    throw new UnsupportedOperationException();
  }

  /**
   *
   * @param threadName
   * @param proxySessionTargets
   * @param listOfClientSessions
   * @param prefSemaphore
   * @param log
   */
  public ClientPrefetching(
          String threadName,
          HashMap<String, ClientSessionTarget> clientSessionTargets,
          PrefSemaphore prefSemaphore, CADILog log) {
    this(threadName, clientSessionTargets, prefSemaphore, log,
            WOI_TYPE_BOUNDING_BOX);
  }

  /**
   * 
   * @param threadName
   * @param proxySessionTargets
   * @param listOfClientSessions
   * @param prefSemaphore
   * @param log
   * @param prefetchingDataHistory
   * @param prefetchingWOIType
   */
  public ClientPrefetching(
          String threadName,
          HashMap<String, ClientSessionTarget> clientSessionTargets,
          PrefSemaphore prefSemaphore, CADILog log,
          int prefetchingWOIType) {

    this(threadName, clientSessionTargets, prefSemaphore, log,
            prefetchingWOIType, null);
  }

  /**
   *
   * @param threadName
   * @param proxySessionTargets
   * @param listOfClientSessions
   * @param prefSemaphore
   * @param log
   * @param prefetchingDataHistory
   * @param prefetchingWOIType
   * @param movementProbabilities
   */
  public ClientPrefetching(
          String threadName,
          HashMap<String, ClientSessionTarget> clientSessionTargets,
          PrefSemaphore prefSemaphore, CADILog log,
          int prefetchingWOIType,
          float[] movementProbabilities) {

    super(threadName, prefSemaphore, log, prefetchingWOIType, movementProbabilities);
    this.clientSessionTargets = clientSessionTargets;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {

    while (!finish) { // main loop

      recomputeWOI = false;
      prefSemaphore.waitChanges();
      prefSemaphore.await();

      if ((clientSessionTargets == null) || (clientSessionTargets.isEmpty())) {
        // do nothing
        continue;
      }


      ArrayList<ViewWindowField> woiHistory = null;

      // Loop on logical targets
      for (Map.Entry<String, ClientSessionTarget> entry : clientSessionTargets.entrySet()) {
        if (recomputeWOI) {
          break;
        }
        prefSemaphore.await();

        woiHistory = entry.getValue().getWOIHistory();
        //System.out.println("\t# historic=" + woiHistory.size()); // DEBUG
        //System.out.println("\tHistoric"); // DEBUG
        //for (ViewWindowField w : woiHistory) {
        //  System.out.println("\t\t" + w.toString());
        //} // DEBUG
       
        if (woiHistory.isEmpty()) {
          continue;
        }

        doPrefetching(woiHistory, entry.getValue(),
                entry.getValue().getCodestream(),
                entry.getValue().getCacheManagement());
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

    out.println("-- Client Prefetching --");
    super.list(out);
    out.flush();
  }
  
  // ============================ private methods ==============================
}
