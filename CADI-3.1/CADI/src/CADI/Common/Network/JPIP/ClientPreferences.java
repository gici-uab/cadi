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
 * This class is used to store the client-preference fields.
 * <p>
 * Further information, see ISO/IEC 15444-9 section C.10.2
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2011/03/10
 */
public class ClientPreferences {

	// View window pref
	public int viewWindow = PROGRESSIVE;
	public static final int FULLWINDOW = 1;
	public static final int PROGRESSIVE = 2;
	public boolean viewWindowRequired = false;
	
	// Colour meth pref
	
	// Max bandwidth
	public long maxBandwidth = -1;
	public boolean maxBandwidthRequired = false;
	
	// Placeholder pref
	
	// Codestream seq pref
	
	// Other

	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public ClientPreferences() {
		
	}
	
	/**
	 * Sets the attributes to its initial values.
	 */
	public void reset() {
		viewWindow = PROGRESSIVE;
    viewWindowRequired = false;
		maxBandwidth = -1;
    maxBandwidthRequired = false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";

		str += "viewWindow="+(viewWindow == FULLWINDOW ? "fullwindow"
		                                                 : "progressive");
		if (viewWindowRequired) str += "/r";
		
		str += ", maxBandwidth="+maxBandwidth+" bytes";
		if (maxBandwidthRequired) str += "/r";
		
		str += "]";
		return str;
	}
	
	/**
	 * Prints this Client Preferences fields out to the specified output stream.
	 * This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Client Preferences --");		
		
		out.print("viewWindow:"+(viewWindow == FULLWINDOW ? "fullwindow"
		                                                      : "progressive"));
		if (viewWindowRequired) out.println("/r");
		else out.println();
		
		out.print("maxBandwidth: "+maxBandwidth+" bytes");
		if (maxBandwidthRequired) out.println("/r");
		else out.println();
						
		out.flush();
	}
	
}