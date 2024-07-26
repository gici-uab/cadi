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
package CADI.Common.Session;

import CADI.Common.Log.CADILog;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2011/10/25
 */
public class ClientSideSessionTargets {

  /**
   * A hash table to save the Session Targets.
   * <p>
   * The <code>key</code> records the session target identifier (SID).
   * The <code>value</code> saves the Session Target.
   */
  protected HashMap<String, ClientSideSessionTarget> clientSessionTargets = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public ClientSideSessionTargets() {
    clientSessionTargets = new HashMap<String, ClientSideSessionTarget>();
  }

  /**
   * Creates a new session target.
   *
   * @param server server where the target is hosted.
   * @param port port number of the server.
   * @param target definition in {@link CADI.Common.Network.JPIP.TargetField#target}.
   * @param log a {@link CADI.Common.Log.CADILog} object.
   *
   * @return a {@link CADI.Proxy.Client.ProxySessionTarget} object.
   */
  public synchronized ClientSideSessionTarget create(String server, int port,
                                                     String target, CADILog log) {
    return create(server, port, target, null, log);
  }

  /**
   * Creates a new session target.
   *
   * @param server server where the target is hosted.
   * @param port port number of the server.
   * @param target definition in {@link CADI.Common.Network.JPIP.TargetField#target}.
   * @param preferredTransportProtocols definition in {@link CADI.Common.Session.ClientSideSessionTarget#preferredTransportProtocols}.
   * @param log a {@link CADI.Common.Log.CADILog} object.
   *
   * @return a {@link CADI.Proxy.Client.ProxySessionTarget} object.
   */
  public synchronized ClientSideSessionTarget create(String server, int port,
                                                     String target,
                                                     ArrayList<String> preferredTransportProtocols,
                                                     CADILog log) {
    // FIXME: only check if the target exist. But it must check if the target at
    // the same server because there could be exist two targets with the same
    // name but at different servers.
    ClientSideSessionTarget session = getByTarget(target);
    if (session == null) {
      session = new ClientSideSessionTarget(server, port, target,
              preferredTransportProtocols, log);
      clientSessionTargets.put(session.getSessionID(), session);
    }
    return session;
  }

  /**
   * Removes the element that it is identifier by its session identifier.
   * Shifts any subsequent elements to the left (subtracts one from their
   * indices).
   *
   * @param cid the channel identifier.
   */
  public synchronized void remove(String cid) {
    // Check input parameters
    if (cid == null) throw new NullPointerException();

    String sid = findSID(cid);
    if (sid != null)
      clientSessionTargets.get(sid).remove();
  }

  /**
   *
   *
   * @return the session.
   */
  public synchronized ClientSideSessionTarget getBySID(String sid) {
    // Check input parameters
    if (sid == null) throw new NullPointerException();

    if (clientSessionTargets.containsKey(sid))
      return clientSessionTargets.get(sid);

    return null;
  }

  /**
   * Returns true if the clients list contains the specified element.
   *
   * @param cid
   * @return true if the list contains the specified element.
   */
  public synchronized boolean contains(String cid) {
    // Check input parameters
    if (cid == null) throw new NullPointerException();

    String sid = findSID(cid);
    if (sid != null)
      return true;

    return false;
  }

  /**
   *
   * @return
   */
  public synchronized ArrayList<String> SIDKeyList() {
    ArrayList<String> SIDs = new ArrayList<String>();

    for (String key : clientSessionTargets.keySet()) {
      SIDs.add(key);
    }

    return SIDs;
  }

  /**
   * Returns true if the clients list contains the specified element.
   *
   * @param cid
   * @return true if the list contains the specified element.
   */
  public synchronized boolean containsSession(String sid) {
    // Check input parameters
    if (sid == null) throw new NullPointerException();

    return clientSessionTargets.containsKey(sid);
  }

  /**
   * Checks whether the <code>cid1</code> and <code>cid2</code> channel
   * identifiers belongs to the same session.
   *
   * @param cid1 a channel identifier.
   * @param cid2 a channel identifier.
   *
   * @return <code>true</code> if both channel identifiers belong to the same
   * 			session. Otherwise, returns <code>false</code>.
   */
  public synchronized boolean belongs(String cid1, String cid2) {
    // Check input parameters
    if (cid1 == null) throw new NullPointerException();
    if (cid2 == null) throw new NullPointerException();

    String sid = findSID(cid2);
    if (sid != null)
      if (clientSessionTargets.get(sid).contains(cid1)
              && clientSessionTargets.get(sid).contains(cid2))
        return true;

    return false;
  }

  /**
   * Gets a logical target from the list. The logical target is identified by
   * the target identifier (<code>tid</code>).
   * <p>
   * <bold>NOTICE:</bold> elements are not copied. Their reference are passed.
   *
   * @param tid definition in {@link CADI.Common.Network.JPIP.TargetField#target}.
   *
   * @return an object which contains the logical target information. If the
   * 			<code>tid<code> is not in the list, it returns <code>null
   * 			</code>
   */
  public ClientSideSessionTarget getByTID(String tid) {
    // Check input parameters
    if (tid == null) throw new NullPointerException();

    for (Map.Entry<String, ClientSideSessionTarget> entry : clientSessionTargets.entrySet()) {
      if (entry.getValue().getTID().equals(tid))
        return entry.getValue();
    }

    return null;
  }

  /**
   * Gets a logical target from the list. The logical target is idenfied by
   * the <code>target</code>.
   *
   * <p>
   * <bold>NOTICE:</bold> elmentents are not copied. Its reference is returned.
   *
   * @param target definition in {@link CADI.Common.Network.JPIP.TargetField#target}.
   *
   * @return an object which contains the logical target information. If the
   * 			<code>target<code> is not in the list, it returns <code>null
   * 			</code>
   */
  public ClientSideSessionTarget getByTarget(String target) {
    // Check input parameters
    if (target == null) throw new NullPointerException();

    for (Map.Entry<String, ClientSideSessionTarget> entry : clientSessionTargets.entrySet()) {
      if (entry.getValue().getTarget().equals(target))
        return entry.getValue();
    }

    return null;
  }

  /**
   * Check if the logical target identified
   *
   * @param target definition in {@link CADI.Common.Network.JPIP.TargetField#target}.
   * @param tid definition in {@link CADI.Common.Network.JPIP.TargetField#tid}.
   *
   * @return <code>true</code> if the <code>target</code> belongs to the
   * 			target identifier <code>tid</code>.
   */
  public boolean equals(String target, String tid) {
    if (getTarget(target, tid) == null) return false;
    else return true;
  }

  /**
   * Closes all channels that belongs to the same sessions, so the session
   * is closed and removed from the sessions list.
   *
   * @param cid the unique channel identifier.
   */
  public synchronized void closeSession(String cid) {
    String sid = findSID(cid);
    clientSessionTargets.get(sid).remove();
    clientSessionTargets.remove(sid);
  }

  /**
   * Returns the number of sessions.
   *
   * @return the number of sessions.
   */
  public synchronized int size() {
    return clientSessionTargets.size();
  }

  /**
   * Checks if there is a empty list of sessions.
   *
   * @return <code>true</code> if there is not any sessions. Otherwise,
   * 			returns <code>false</code>.
   */
  public synchronized boolean isEmpty() {
    return clientSessionTargets.isEmpty();
  }

  /**
   * Remove all elements from the clients list
   */
  public synchronized void clear() {
    for (Map.Entry<String, ClientSideSessionTarget> entry : clientSessionTargets.entrySet()) {
      entry.getValue().remove();
    }

    clientSessionTargets.clear();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";

    if (isEmpty()) {
      str += " <<< HashMap is empty >>> ";
    } else {
      for (Map.Entry<String, ClientSideSessionTarget> entry : clientSessionTargets.entrySet()) {
        str += "key=" + entry.getKey();
        str += "value=" + entry.getValue().toString();
      }
    }

    str += "]";
    return str;
  }

  /**
   * Prints this Logical Target List fields out to the specified output
   * stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Client Side Session Targets --");

    if (isEmpty()) {
      out.println(" <<< HashMap is empty >>> ");
    } else {
      for (Map.Entry<String, ClientSideSessionTarget> entry : clientSessionTargets.entrySet()) {
        out.println("key: " + entry.getKey());
        out.print("value: ");
        entry.getValue().list(out);
      }
    }
    out.flush();
  }

  // ============================ private methods ==============================
  /**
   *
   * @param cid
   */
  protected String findSID(String cid) {
    for (Map.Entry<String, ClientSideSessionTarget> entry : clientSessionTargets.entrySet()) {
      if (entry.getValue().contains(cid))
        return entry.getKey();
    }

    return null;
  }
  
   /**
   * Gets a logical target from the list. The logical target is idenfied by
   * the <code>target</code> and/or <code>tid</code>. If the <code>target
   * </code> and <code>tid</code> parameters do not identify the same target
   * an exception is thrown.
   *
   * @param target definition in {@link CADI.Common.Network.JPIP.TargetField#target}.
   * @param tid definition in {@link CADI.Common.Network.JPIP.TargetField#target}.
   *
   *  @return an object which contains the logical target information. If the
   * 			<code>target<code> is not in the list, it returns <code>null
   * 			</code>
   */
  private ClientSideSessionTarget getTarget(String target, String tid) {
    if ((target == null) && (tid == null))
      throw new IllegalArgumentException();
    else if ((target != null) && (tid == null))
      return getByTarget(target);
    else if ((target == null) && (tid != null))
      return getByTID(tid);
    else { // target != null && tid != null
      ClientSideSessionTarget sessionTarget = getByTID(tid);
      if (!sessionTarget.getTarget().equals(target))
        throw new IllegalArgumentException();
      return sessionTarget;
    }
  }

}
