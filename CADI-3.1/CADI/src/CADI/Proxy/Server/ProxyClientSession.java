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
package CADI.Proxy.Server;

import java.io.PrintStream;
import java.util.ArrayList;

import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Common.Session.ServerSideSession;
import CADI.Proxy.LogicalTarget.JPEG2000.ProxyJPEG2KCodestream;

/**
 * This class is uses to save information about a client sessions. 
 * <p>
 * For further information about JPIP sessions, see ISO/IEC 15444-9 section B.2 
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/02/14
 */
public class ProxyClientSession extends ServerSideSession {

  
  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public ProxyClientSession() {
    super();
  }

  /**
   *
   *
   * @param tid
   * @param codestream
   * @param returnType
   * @param transport
   * @return
   */
  public String createSessionTarget(String tid,
                                    ProxyJPEG2KCodestream codestream,
                                    String returnType, String transport) {
    return createSessionTarget(tid, codestream, returnType, transport, false);
  }

  /**
   * This method creates a new session target within the client session.
   *
   * @param tid
   * @param codestream
   * @param returnType
   * @param transport
   * @return
   */
  public String createSessionTarget(String tid,
                                    ProxyJPEG2KCodestream codestream,
                                    String returnType, String transport,
                                    boolean recordHistoric) {
    // Check input parameters
    if (tid == null)
      throw new NullPointerException();
    if (codestream == null)
      throw new NullPointerException();
    if (returnType == null)
      throw new NullPointerException();
    if (transport == null)
      throw new NullPointerException();

    // Check if there exist a session target within the session with the same tid
    if (targets.containsKey(tid))
      throw new IllegalArgumentException("There already exist a session for this target.");


    // Creates new session target
    ProxyClientSessionTarget sessionTarget =
            new ProxyClientSessionTarget(tid, codestream, returnType, recordHistoric);
    targets.put(tid, sessionTarget);

    String cid = sessionTarget.newChannel(transport);

    updateExpirationTime();

    return cid;
  }

  /**
   * Returns the cache which is associated with the channel.
   *
   * @param cid
   *
   * @return return cache object.
   */
  public ProxyCacheModel getCache(String cid) {
    if (cid == null)
      throw new NullPointerException();

    ProxyClientSessionTarget sessionTarget =
            (ProxyClientSessionTarget) getSessionTarget(cid);
    if (sessionTarget != null)
      return sessionTarget.getCache();

    return null;
  }

  /**
   * Adds a new WOI to the historic.
   *
   * @param woi the WOI to be added.
   */
  public void addWOIToHistory(String cid, ViewWindowField woi) {
    if (cid == null)
      throw new NullPointerException();

    ProxyClientSessionTarget sessionTarget =
            (ProxyClientSessionTarget) getSessionTarget(cid);
    if (sessionTarget != null)
      sessionTarget.addWOIToHistory(woi);
  }

  public ArrayList<ViewWindowField> getWOIsHistoric(String cid) {
    if (cid == null)
      throw new NullPointerException();

    ProxyClientSessionTarget sessionTarget =
            (ProxyClientSessionTarget) getSessionTarget(cid);
    if (sessionTarget != null)
      return sessionTarget.getWOIHistory();

    return null;
  }

  /**
   * Returns the {@link #woisHistory} attribute.
   *
   * @return definition in {@link #woisHistory}.
   */
  public ArrayList<ViewWindowField> getWOIHistoryByTID(String tid) {
    if (tid == null)
      throw new NullPointerException();

    ProxyClientSessionTarget sessionTarget =
            (ProxyClientSessionTarget) getSessionTargetByTID(tid);
    if (sessionTarget != null)
      return sessionTarget.getWOIHistory();

    return null;
  }

  /**
   *
   * @return
   */
  public ViewWindowField getLastWOIHistoryByTID(String tid) {

    if (tid == null)
      throw new NullPointerException();

    ProxyClientSessionTarget sessionTarget =
            (ProxyClientSessionTarget) getSessionTargetByTID(tid);
    if (sessionTarget != null)
      return sessionTarget.getLastWOIHistory();

    return null;
  }

  /**
   * Removes the session.
   */
  @Override
  public void remove() {
    super.remove();
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

    out.println("-- Proxy client session --");
    super.list(out);
    out.flush();
  }
  
  // ============================ private methods ==============================
}
