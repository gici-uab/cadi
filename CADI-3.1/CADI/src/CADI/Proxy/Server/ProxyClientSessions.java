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
import java.util.Map.Entry;

import CADI.Common.Session.Session;
import CADI.Common.Session.Sessions;

/**
 * 
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2010/12/13
 */
public class ProxyClientSessions extends Sessions {
	
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public ProxyClientSessions() {
		super();
	}
		
	/**
	 * Adds the specified element to the end of the list.
	 * 
	 * @param session element to be appended to this list.
	 */
	public synchronized void add(ProxyClientSession session) {	
		sessions.put(session.getSessionID(), session);
	}
		
	/**
	 * Removes the element that it is identified by a channel identifier.
	 *
	 * @param cid the channel identifier.
	 */
	public synchronized void remove(String cid) {
		if (cid == null) throw new NullPointerException();
		
		ProxyClientSession session = get(cid);
		String sessionID = session.getSessionID();
		session.remove();
		sessions.remove(sessionID);
	}
	
	/**
	 * Returns the first element of the queue if it isn't empty, otherwise null.
	 * 
	 * @return the session.
	 */
	public synchronized ProxyClientSession get(String cid) {
		if (cid == null) throw new NullPointerException();
		
		for (Entry<String,Session> session : sessions.entrySet())
			if (session.getValue().contains(cid))
				return (ProxyClientSession)session.getValue();
		
		return null;				
	}

  /**
   *
   *
   * @return the session.
   */
  public synchronized ProxyClientSession getBySID(String sid) {
    if (sid == null) throw new NullPointerException();

    if (sessions.containsKey(sid))
      return (ProxyClientSession)sessions.get(sid);

    return null;
  }
	
	/**
	 * Closes all channels that belongs to the same sessions, so the session
	 * is closed but not removed. 
	 * 
	 * @param cid the unique channel identifier.
	 */
	public synchronized void closeChannel(String cid) {
		if (cid == null) throw new NullPointerException();
		
		ProxyClientSession session = get(cid);
		session.closeChannel(cid);

	}
	
	/**
	 * Remove all elements from the clients list
	 */
	public synchronized void clear() {
		super.clear();
	}
		
	/**
	 * For debugging purpose.
	 */	
	public synchronized String toString() {
		String str = "";
		str += getClass().getName() + " [";
		str += super.toString();
		str += "]";
		
		return str;
	}
		
	/**
	 * Prints this ClientSessions out to the specified output stream. This method
	 * is useful for debugging.
	 * 
	 * @param out
	 *           an output stream.
	 */
	public synchronized void list(PrintStream out) {

		out.println("-- Proxy client sessions --");
		super.list(out);
		out.flush();
	}
	
  // ============================ private methods ==============================
}