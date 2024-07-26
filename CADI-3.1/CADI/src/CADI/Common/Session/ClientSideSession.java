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

/**
 * This class is uses to save information about a client sessions. 
 * <p>
 * For further information about JPIP sessions, see ISO/IEC 15444-9 section B.2 
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.2 2010/12/12
 */
public class ClientSideSession extends Session {
	
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public ClientSideSession() {
		super();
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

		out.println("-- Client Session --");
		super.list(out);
					
		out.flush();
	}
	
	// ============================ private methods ==============================		
}