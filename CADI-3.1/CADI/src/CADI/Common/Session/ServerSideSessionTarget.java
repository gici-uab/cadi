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

import CADI.Common.Cache.CacheModel;

/**
 * This class is used to save information about a logical target that
 * belongs to a session.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2010/12/12
 */
public class ServerSideSessionTarget extends SessionTarget {
	
	/**
	 * 
	 */
	protected CacheModel cache = null;
	
	// ============================= public methods ==============================	
	/**
	 * Constructor.
	 * 
	 * @param tid
	 */
	public ServerSideSessionTarget(String tid) {
		this(tid, "jpp-stream");
	}
	
	/**
	 * Constructor.
	 * 
	 * @param tid definition in {@link #tid}.
	 * @param returnType definition in {@link #returnType}
	 * @param transport definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#transport}.
	 */
	public ServerSideSessionTarget(String tid, String returnType) {
		super(tid, returnType);
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

		out.println("-- Session target --");
		super.list(out);		
		out.flush();
	}
	
}
