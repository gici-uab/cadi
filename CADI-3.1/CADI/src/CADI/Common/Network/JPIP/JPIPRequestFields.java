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
package CADI.Common.Network.JPIP;

import java.io.PrintStream;

/**
 * This class is used to group all the JPIP request fields.
 * <p>
 * Further information, see ISO/IEC 15444-9 section C.1
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0   2007-2012/10/26
 */
public class JPIPRequestFields {
		
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.TargetField}.
	 */
	public TargetField targetField = null;
	
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.ChannelField}.
	 */
	public ChannelField channelField = null;
	
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.ViewWindowField}.
	 */
	public ViewWindowField viewWindowField = null;	
	
	/**
	 * 
	 */
	public String metareq = null;
	
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.DataLimitField}.
	 */
	public DataLimitField dataLimitField = null;
	
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.ServerControlField}.
	 */	
	public ServerControlField serverControlField = null;
	
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.CacheManagementField}.
	 */
	public CacheManagementField cacheManagementField = null;
	
	/**
	 * 
	 */
	public String upload = null;
	
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.ClientCapPrefField}.
	 */
	public ClientCapPrefField clientCapPrefField = null;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public JPIPRequestFields() {				
		targetField = new TargetField();
		channelField = new ChannelField();
		viewWindowField = new ViewWindowField();
		metareq = null;
		dataLimitField = new DataLimitField();
		serverControlField = new ServerControlField();
		cacheManagementField = new CacheManagementField();
		upload = null;
		clientCapPrefField =  new ClientCapPrefField();	
	}

	/**
	 * Sets all attributes to its initial values.
	 */
	public void reset() {
		targetField.reset();
		channelField.reset();
		viewWindowField.reset();				
		metareq = null;
		dataLimitField.reset();
		serverControlField.reset();		
		cacheManagementField.reset();
		upload = null;
		clientCapPrefField.reset();
	}
		
	/**
	 * For debugging purposes. 
	 */
  @Override
	public String toString() {
		String str = "";

		str = getClass().getName() + " [\n";

		str += targetField.toString() + "\n";
		str += channelField.toString() + "\n";
		str += viewWindowField.toString() + "\n";
		
		//str += "METADATA";

		str += dataLimitField.toString() + "\n";
		str += serverControlField.toString() + "\n";
		str += cacheManagementField.toString() + "\n";
				
		//str += " UPLOAD";
		
		str += clientCapPrefField.toString() + "\n";

		str += "]";
		return str;
	}
	
	/**
	 * Prints this JPIP Request fields out to the specified output stream.
	 * This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- JPIP Request Fields --");		
		
		targetField.list(out);
		channelField.list(out);
		viewWindowField.list(out);
		
		//str += "METADATA";

		dataLimitField.list(out);
		serverControlField.list(out);
		cacheManagementField.list(out);
				
		//str += " UPLOAD";
		
		clientCapPrefField.list(out);
		
		out.flush();
	}	
	
}