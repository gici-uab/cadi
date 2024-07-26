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

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This class is used to save the sessions of the clients. This object will
 * be shared among the {@link CADI.Server.Core.Worker} threads, therefore
 * methods must guarantee the access to the list.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1 2010/12/12
 */
public class Sessions {
	
	/**
	 * Contains a list that is used to save client's sessions.
   * <p>
   * The <code>key</code> of the HasMap is a session identifier and the <code>
   * value</code> is a session.
	 */
	protected HashMap<String, Session> sessions = null;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public Sessions() {
		sessions = new HashMap<String,Session>();
	}
		
	/**
	 * Adds the specified element to the end of the list.
	 * 
	 * @param session element to be appended to this list.
	 * @return true (as specified by Collection.add(E))
	 */
	public synchronized void add(Session session) {
		sessions.put(session.getSessionID(), session);
	}
		
	/**
	 * Removes the element that it is identifier by a channel identifier.
	 *
	 * @param cid the channel identifier.
	 */
	public synchronized void remove(String cid) {
		if (cid == null) throw new NullPointerException();
		
		for (Entry<String,Session> session : sessions.entrySet())
			if (session.getValue().contains(cid)) {
				String sessionID = session.getKey();
				session.getValue().remove();
				sessions.remove(sessionID);
			}		
	}
	
	/**
	 * Returns the first element of the queue if it isn't empty, otherwise null.
	 * 
	 * @return the session.
	 */
	public synchronized Session get(String cid) {
		
		if (cid == null) throw new NullPointerException();
		
		for (Entry<String,Session> session : sessions.entrySet())
			if (session.getValue().contains(cid))
				return session.getValue();
		
		return null;			
	}
	
	/**
	 * Returns true if the clients list contains the specified element.
	 * 
	 * @param cid 
	 * @return true if the list contains the specified element.
	 */
	public synchronized boolean contains(String cid) {
		
		if (cid == null) throw new NullPointerException();
		
		for (Entry<String,Session> session : sessions.entrySet())
			if (session.getValue().contains(cid))
				return true;
		
		return false;	
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
		if (cid1 == null) throw new NullPointerException();
		
		for (Entry<String,Session> session : sessions.entrySet())
			if (session.getValue().contains(cid1)) {
				if (session.getValue().contains(cid2))
					return true;
				
				return false;
			}
		
		return false;		
	}
	
	/**
	 * Closes all channels that belongs to the same sessions, so the session
	 * is closed but not removed. 
	 * 
	 * @param cid the unique channel identifier.
	 */
	public synchronized void closeChannel(String cid) {
		if (cid == null) throw new NullPointerException();
		
		for (Entry<String,Session> session : sessions.entrySet())
			if (session.getValue().contains(cid))
				session.getValue().closeChannel(cid);
	}
	
	/**
	 * Returns the number of sessions.
	 * 
	 * @return the number of sessions.
	 */
	public synchronized int size() {
		return sessions.size();	
	}
	
	/**
	 * Checks if there is a empty list of sessions. 
	 * 
	 * @return <code>true</code> if there is not any sessions. Otherwise,
	 * 			returns <code>false</code>.
	 */
	public synchronized boolean isEmpty() {
		return sessions.isEmpty();
	}	
	
  /**
   * 
   * @return
   */
  public synchronized Set<String> getKeySet() {
    return sessions.keySet();
  }

	/**
	 * Remove all elements from the clients list
	 */
	public synchronized void clear() {
		for (Entry<String,Session> session : sessions.entrySet())
			session.getValue().remove();
		sessions.clear();
	}
		
	/**
	 * For debugging purpose.
	 */	
  @Override
	public synchronized String toString() {
		String str = "";
		str += getClass().getName() + " [";
		
		for (Entry<String,Session> session : sessions.entrySet())
			str += session.getValue().toString();
		
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

		out.println("-- Client Sessions --");
				
		for (Entry<String,Session> session : sessions.entrySet())
			session.getValue().list(out);
		
		out.flush();
	}
	
	// ============================ private methods ==============================
	
}