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
 * This class is used to save the JPIP channel properties.
 * <p>
 * For further information about JPIP channels, 
 * see ISO/IEC 15444-9 section B.2
 *
 *	@author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2010/01/25
 */
public class JPIPChannel {
	
	/**	
	 * Definition in {@link CADI.Common.Network.JPIP.ChannelField#cid}.
	 */
	protected String cid = null;
	
	/**
	 * Indicates the transport protocol.
	 * <p>
	 * NOTICE: Only the <code>http</code> transport is available.
	 */
	protected String transport = null; 
	
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.ClientCapPrefField#cap}.
	 */
	protected String cap = null;
	
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.ClientCapPrefField#pref}.
	 */
	protected String pref = null;
	
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.ClientCapPrefField#csf}.
	 */
	protected String csf = null;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public JPIPChannel() {
	}
	
	/**
	 * Constructor.
	 * 
	 * @param transport
	 */
	public JPIPChannel(String transport) {
		if ( !transport.equals("http") ) throw new IllegalArgumentException();
		
		this.transport = transport;
	}
		
	/**
	 * Returns the {@link #cap} attribute. 
	 * 
	 * @return the {@link #cap} attribute.
	 */
	public String getCap() {
		return cap;
	}

	/**
	 * Sets the {@link #cap} attribute.
	 * 
	 * @param cap the {@link #cap} to be set.
	 */
	public void setCap(String cap) {
		this.cap = cap;
	}

	/**
	 * Returns the {@link #cid} attribute.
	 * 
	 * @return the {@link #cid} attribute.
	 */
	public String getCid() {
		return cid;
	}

	/**
	 * Returns the {@link #csf} attribute.
	 * 
	 * @return the {@link #csf} attribute.
	 */
	public String getCsf() {
		return csf;
	}

	/**
	 * Sets the {@link #csf} attribute.
	 * 
	 * @param csf the {@link #csf} to be set.
	 */
	public void setCsf(String csf) {
		this.csf = csf;
	}

	/**
	 * Returns the {@link #pref} attribute.
	 * 
	 * @return the {@link #pref} attribute.
	 */
	public String getPref() {
		return pref;
	}

	/**
	 * Sets the {@link #pref} attribute.
	 * 
	 * @param pref the {@link #pref} to be set.
	 */
	public void setPref(String pref) {
		this.pref = pref;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";
		
		str += getClass().getName() + " [";
		str += "cid="   + cid; 
		str += " cap="  + cap;
		str += " pref=" + pref;
		str += " csf="  + csf;		
		str += "]";
		
		return str;
	}
	
	/**
	 * Prints this Channel out to the specified output stream. This method
	 * is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Channel --");
			
		out.println("cid: "       + cid);
		out.println("transport: " + transport);
		out.println("cap: "       + cap);
		out.println("pref: "      + pref);
		out.println("csf: "       + csf);
			
		out.flush();
	}
		
}