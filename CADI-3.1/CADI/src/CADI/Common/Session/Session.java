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
import java.util.Map;
import java.util.UUID;

import CADI.Server.LogicalTarget.JPEG2000.JP2KServerLogicalTarget;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is uses to save information about a client sessions. 
 * <p>
 * For further information about JPIP sessions, see ISO/IEC 15444-9 section B.2 
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.2 2010/02/21
 */
public class Session {
	
	/**
	 * 
	 */
	protected String sessionID = null;
	
	/**
	 * Is a mutex to lock the object when a thread is setting some attribute.
	 */
	protected ReentrantReadWriteLock mutex = null;
	
	/**
	 * Is a Map with the logical targets avialable within this session. The key
	 * of the map is the tid (Target IDentifier) and the value is an session
	 * target object.
	 */
	protected HashMap<String, SessionTarget> targets = null;
	
	/**
	 * Expiration time of the session. When this expiration time is reached, the
	 * session will be removed.
	 */
	protected long expirationTime = 0L;	// It has not been implemented yet
	
	/**
	 * The default expiration time.
	 */
	protected final long DEFAULT_EXPIRATION_TIME = 1000*60*60*24; // 24 hours =  miliseconds * seconds * minuts * hours    
		
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public Session() {			
		mutex = new ReentrantReadWriteLock();
		targets = new HashMap<String,SessionTarget>();
		updateExpirationTime();
		
		sessionID = UUID.randomUUID().toString();
	}
	
	/**
	 * Creates a new session.
	 * 
	 * @param returnType
	 * @param transport
	 * 
	 * @return the session identifier
	 */
	public String createSessionTarget(JP2KServerLogicalTarget logicalTarget,
	                                  String returnType) {
		// Check input parameters
		if (logicalTarget == null) throw new NullPointerException();
		
		// Check if there exist a session target within the session with the same tid
		if (targets.containsKey(logicalTarget.getTID()))
			throw new IllegalArgumentException("There exist a session for this target.");
	
		// Creates new session target and its cache
		SessionTarget sessionTarget = new SessionTarget(logicalTarget.getTID(), returnType);
		targets.put(logicalTarget.getTID(), sessionTarget);
		
		updateExpirationTime();
		
		return sessionTarget.getCid();
	}
	
	/**
	 * 
	 * @return
	 */
	public String getSessionID() {
		return sessionID;
	}
	
	/**
	 * Checks if the channel <code>cid</code> belongs to this session.  
	 * 
	 * @param cid the unique channel identifier.
	 * @return <code>true</code> if the channel belongs to this session.
	 * 			Otherwise, returns <code>false</code>.
	 */
	public boolean contains(String cid) {
		if (cid == null) throw new NullPointerException();
		
		for (Map.Entry<String, SessionTarget> entry : targets.entrySet()) {
			if ( entry.getValue().contains(cid) ) {
				return true;
			}
		}
		
		return false;
	}

  /**
   * Check if the current session has a target identified by the <code>tid
   * </code> target identifier.
   *
   * @param tid a target identifier.
   *
   * @return <code>true</code> if the session has the target. Otherwise, returns
   *          <code>false</code>.
   */
  public boolean containsTID(String tid) {
    if (tid == null) throw new NullPointerException();

    for (Map.Entry<String, SessionTarget> entry : targets.entrySet()) {
			if (entry.getValue().getTID().equals(tid))
				return true;
		}
		
    return false;
  }
	
	/**
	 * Opens a new channel within the logical target which the cid belongs to.
	 * 
	 * @param cid a Channel Identifier belonging to a logical target.
	 * 
	 * @return the new cid or <code>null</code> if the cid does not belong
	 * 			to this session. 
	 */
	public String openChannel(String cid) {
		if (cid == null) throw new NullPointerException();
		
		for (Map.Entry<String, SessionTarget> entry : targets.entrySet()) {
			if (entry.getValue().contains(cid)) {
				return entry.getValue().newChannel();
			}
		}
		
		return null;
	}
	
	/**
	 * Closes a channel. If there is no more left channels, the session
	 * target will be removed.
	 * 
	 * @param cid the unique channel identifier.
	 */
	public void closeChannel(String cid) {
		if (cid == null) throw new NullPointerException();
		
		for (Map.Entry<String, SessionTarget> entry : targets.entrySet()) {
			if (entry.getValue().contains(cid)) {
				entry.getValue().closeChannel(cid);
				
				if (entry.getValue().numOfChannels() == 0) {
					String tid = entry.getValue().getTID();
					entry.getValue().remove();
					targets.remove(tid);
				}
			}
			break;
		}	
	}
		
	/**
	 * Removes the session.
	 */
	public void remove() {
		
		for (Map.Entry<String, SessionTarget> entry : targets.entrySet()) {
			entry.getValue().remove();
		}
		
		targets.clear();
	}
	
	/**
	 * 
	 * @param cid
	 * @return
	 */
	public SessionTarget getSessionTarget(String cid) {
		if (cid == null) throw new NullPointerException();
		
		for (Map.Entry<String, SessionTarget> entry : targets.entrySet())
			if (entry.getValue().contains(cid))
				return entry.getValue();
				
		return null;
	}

  /**
	 *
	 * @param cid
	 * @return
	 */
	public SessionTarget getSessionTargetByTID(String tid) {
		if (tid == null) throw new NullPointerException();

		for (Map.Entry<String, SessionTarget> entry : targets.entrySet())
			if (entry.getValue().getTID().equals(tid))
				return entry.getValue();
				
		return null;
	}

  /**
   * 
   * @return
   */
  public Set<String> tidKeySet() {
    return targets.keySet();
  }
  
	/**
	 * 
	 */
	public String getReturnType(String cid) {
		for (Map.Entry<String, SessionTarget> entry : targets.entrySet()) {
			if (entry.getValue().contains(cid))
				return entry.getValue().getReturnType();
		}
		return null;
	}
	
	/**
	 * See {@link CADI.Common.Session.SessionTarget#getTid()}.
	 * 
	 * @param cid the channel identifier.
	 */
	public String getTID(String cid) {
		if (cid == null) throw new NullPointerException();
		
		for (Map.Entry<String, SessionTarget> entry : targets.entrySet()) {
			if (entry.getValue().contains(cid))
				return entry.getValue().getTID();
		}
		
		return null;
	}
			
	/**
	 * Checks whether the session has expired or it has not.
	 * 
	 * @return <code>true</code> if the session has expired. Otherwise,
	 * 			returns <code>false</code>.
	 */
	public boolean hasExpired() {
		return ( expirationTime < System.currentTimeMillis() ? true : false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";
		
		str += getClass().getName() + " [";
		
		str += "SessionID="+sessionID;
		str += "ExpirationTime="+expirationTime;
		
		for (Map.Entry<String, SessionTarget> entry : targets.entrySet())
			str += entry.getValue().toString();
				
		str += "]";
		
		return str;
	}
	
	/**
	 * Prints this Session out to the specified output stream. This method
	 * is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Client Session --");
		
		out.println("SessionID="+sessionID);
		out.println("ExpirationTime="+expirationTime);
		
		for (Map.Entry<String, SessionTarget> entry : targets.entrySet())
			entry.getValue().list(out);
					
		out.flush();
	}
	
	// ============================ private methods ==============================
	/**
	 * 
	 *
	 */
	protected void updateExpirationTime() {
		expirationTime = System.currentTimeMillis() + DEFAULT_EXPIRATION_TIME;
	}
	
}