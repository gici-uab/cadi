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
package CADI.Server.Session;

import java.io.PrintStream;

import CADI.Common.Session.ServerSideSession;
import CADI.Server.Cache.ServerCacheModel;
import CADI.Server.LogicalTarget.JPEG2000.JP2KServerLogicalTarget;

/**
 * This class is uses to save information about a client sessions. 
 * <p>
 * For further information about JPIP sessions, see ISO/IEC 15444-9 section B.2 
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.2 2010/12/12
 */
public class ServerClientSession extends ServerSideSession {
	
  
  // ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public ServerClientSession() {
		super();
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
	                                  String returnType, String transport) {
		// Check input parameters
		if (logicalTarget == null) throw new NullPointerException();
		if (returnType == null) throw new NullPointerException();
		if (transport == null) throw new NullPointerException();
		
		// Check if there exist a session target within the session with the same tid
		if (targets.containsKey(logicalTarget.getTID()))
			throw new IllegalArgumentException("There already exist a session for this target.");
	
		// Creates new session target
		ServerClientSessionTarget sessionTarget =
			new ServerClientSessionTarget(logicalTarget.getTID(), logicalTarget, returnType);
		targets.put(sessionTarget.getTID(), sessionTarget);
		
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
	public ServerCacheModel getCache(String cid) {
		if (cid == null) throw new NullPointerException();
		
		ServerClientSessionTarget sessionTarget =
			(ServerClientSessionTarget)getSessionTarget(cid);
		if (sessionTarget != null)
			return sessionTarget.getCache();
		
		return null;
	}
  
  /**
   * 
   * @param cid
   * @return 
   */
  public ServerJPIPChannel getJPIPChannel(String cid) {
    if (cid == null) throw new NullPointerException();
    
    ServerClientSessionTarget sessionTarget =
			(ServerClientSessionTarget)getSessionTarget(cid);
    
    if (sessionTarget != null) 
      return sessionTarget.getChannel(cid);
    
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
	 * Prints this Server Client Session out to the specified output stream. This
	 * method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
  @Override
	public void list(PrintStream out) {

		out.println("-- Server client session --");
		
		super.list(out);
		
		out.flush();
	}
	
	// ============================ private methods ==============================
}