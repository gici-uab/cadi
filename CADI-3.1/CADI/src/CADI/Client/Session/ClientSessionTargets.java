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
package CADI.Client.Session;

import CADI.Common.Log.CADILog;
import CADI.Common.Session.ClientSideSessionTarget;
import CADI.Common.Session.ClientSideSessionTargets;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a list of cached logical targets. Logical targets
 * are cached when a response from a server is sent to a client.
 * <p>
 * <bold>NOTICE:</bold> Methods are not synchronized yet. It may be a
 * problem with the threads.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2011/10/23
 */
public class ClientSessionTargets extends ClientSideSessionTargets {


  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public ClientSessionTargets() {
    super();
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
  @Override
  public synchronized ClientSessionTarget create(String server, int port, String target,
                                  CADILog log) {
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
  @Override
  public synchronized ClientSessionTarget create(String server, int port,
                                                String target,
                                                ArrayList<String> preferredTransportProtocols,
                                                CADILog log) {
    //return (ProxySessionTarget)super.create(server, port, target, preferredTransportProtocols, log);
    // FIXME: only check if the target exist. But it must check if the target at
    // the same server because there could be exist two targets with the same
    // name but at different servers.
    
    /*ClientSessionTarget session = getByTarget(target);
    if (session == null) {
      session = new ClientSessionTarget(server, port, target,
              preferredTransportProtocols, log);
      clientSessionTargets.put(session.getSessionID(), session);
    }
    return session;*/
    return null;
  }

  /**
   * Removes the element that it is identifier by its session identifier.
   * Shifts any subsequent elements to the left (subtracts one from their
   * indices).
   *
   * @param cid the channel identifier.
   */
  @Override
  public synchronized void remove(String cid) {
    super.remove(cid);
  }

  /**
   *
   *
   * @return the session.
   */
  @Override
  public synchronized ClientSessionTarget getBySID(String sid) {
    return (ClientSessionTarget)super.getBySID(sid);
  }

  /**
   * Returns true if the clients list contains the specified element.
   *
   * @param cid
   * @return true if the list contains the specified element.
   */
  @Override
  public synchronized boolean contains(String cid) {
    return super.contains(cid);
  }

  /**
   *
   * @return
   */
  @Override
  public synchronized ArrayList<String> SIDKeyList() {
    return super.SIDKeyList();
  }

  /**
   * Returns true if the clients list contains the specified element.
   *
   * @param cid
   * @return true if the list contains the specified element.
   */
  @Override
  public synchronized boolean containsSession(String sid) {
    return super.containsSession(sid);
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
  @Override
  public synchronized boolean belongs(String cid1, String cid2) {
    return super.belongs(cid1, cid2);
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
  @Override
  public ClientSessionTarget getByTID(String tid) {
    return (ClientSessionTarget)super.getByTID(tid);
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
  @Override
  public ClientSessionTarget getByTarget(String target) {
    return (ClientSessionTarget)super.getByTarget(target);
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
  @Override
  public boolean equals(String target, String tid) {
    return super.equals(target, tid);
  }
  
  /**
   * Closes all channels that belongs to the same sessions, so the session
   * is closed and removed from the sessions list.
   *
   * @param cid the unique channel identifier.
   */
  @Override
  public synchronized void closeSession(String cid) {
    super.closeSession(cid);
  }

  /**
   * Returns the number of sessions.
   *
   * @return the number of sessions.
   */
  @Override
  public synchronized int size() {
    return super.size();
  }

  /**
   * Checks if there is a empty list of sessions.
   *
   * @return <code>true</code> if there is not any sessions. Otherwise,
   * 			returns <code>false</code>.
   */
  @Override
  public synchronized boolean isEmpty() {
    return super.isEmpty();
  }

  /**
   * Remove all elements from the clients list
   */
  @Override
  public synchronized void clear() {
    super.clear();
  }

  /*
   * (non-Javadoc)
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
   * Prints this Logical Target List fields out to the specified output
   * stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Client Session Targets --");
    super.list(out);
    out.flush();
  }

  // ============================ private methods ==============================
  
}
