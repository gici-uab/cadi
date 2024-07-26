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
import java.util.UUID;

import CADI.Common.Session.JPIPChannel;

/**
 * This class is used to save the JPIP channel properties.
 * <p>
 * For further information about JPIP channels, 
 * see ISO/IEC 15444-9 section B.2
 *
 *	@author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2010/01/25
 */
public class ServerJPIPChannel extends JPIPChannel {
	
  
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param transport
	 */
	public ServerJPIPChannel(String transport) {
		super();
		if ( !transport.equals("http") ) throw new IllegalArgumentException();
		
		this.transport = transport;
		
		cid = generateCID();
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
	 * Prints this Server JPIP Channel out to the specified output stream. This
	 * method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
  @Override
	public void list(PrintStream out) {

		out.println("-- Server JPIP Channel --");
		super.list(out);
		out.flush();
	}
	
	// ============================ private methods ==============================	
	/**
	 * Generates a unique channel identifier. The unique identifier generation
	 * is base on the current time, it is a time-stamp.
	 * <p>
	 * NOTICE: this method could be improved adding the Rijndael (or another
	 * one), client information, etc. to improve the security.
	 * 
	 * @return the unique channel identifier.
	 */
	protected String generateCID() {
		return UUID.randomUUID().toString();
	}
		
}