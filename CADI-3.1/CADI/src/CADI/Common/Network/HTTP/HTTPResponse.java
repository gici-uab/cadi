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
package CADI.Common.Network.HTTP;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/05/31
 */
public class HTTPResponse {

	/**
	 * An <code>int</code> representing the three digit HTTP Status-Code.
	 * 
	 * Allowed values in {@link CADI.Common.Network.HTTP.StatusCodes}
	 */
	private int responseCode = -1;

	/**
	 * It is a short description of the status code provided by the server
	 */
	private String responseMessage = null; 
	
	/**
	 * Contains the HTTP response headers.
	 */
	private HashMap<String, String> headers = null;

	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public HTTPResponse() {
		responseCode = -1;
		responseMessage = null;
		headers = new HashMap<String, String>();
	}

	/**
	 * Sets the response code.
	 * 
	 * @param responseCode the response code value.
	 */
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	
	/**
	 * Retuns the response code.
	 * 
	 * @return the response code.
	 */
	public int getResponseCode() {
		return responseCode;
	}
	
	/**
	 * Sets the response message.
	 * 
	 * @param responseMessage the response message.
	 */
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	
	/**
	 * Returns the response message.
	 * 
	 * @return the response message.
	 */
	public String getResponseMessage() {
		return responseMessage;
	}
	
	/**
	 * Puts the value of the named header field. 
	 * 
	 * @param name the name of the header field.
	 * @param value the value of the header field.
	 */
	public void setHeaderField(String name, String value) {
		if (name == null) throw new NullPointerException ("name is null");
		if (value == null) throw new NullPointerException ("value is null");
		headers.put(name, value);
	}

	/**
	 * Returns the value of the named header field.
	 * <p>
	 * If called on a connection that sets the same header multiple times
	 * with possibly different values, only the last value is returned.
	 *
	 * @param   name   the name of a header field.
	 * @return  the value of the named header field, or <code>null</code>
	 *          if there is no such field in the header.
	 */
	public String getHeaderField(String name) {
		return headers.get(name);
	}
	
	/**
	 * Returns an unmodifiable Enumeration of the header names.
	 * <p>
	 * The Enumeration keys are Strings that represent the response-header
	 * field names.
	 * 
	 * @return a Set of header fields names.
	 */
	public Set<String> getHeaders() {
		return headers.keySet();
	}
			
	/**
	 * Sets the attributes to inital values. 
	 */
	public void reset() {
		responseCode = -1;
		responseMessage = null;
		headers.clear();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";
		str += "Response code=" + responseCode;
		str += ", Response message=" + responseMessage;

		String key, value;		
    for (Map.Entry<String,String> entry : headers.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			str += ", " + key + "=" + value;
		}
		str += "]";

		return str;
	}

	/**
	 * Prints this HTTPResponse out to the specified output stream. This method
	 * is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- HTTP Response --");
		out.println("Response code: " + responseCode);
		out.println("Response message: " + responseMessage);

		String key, value;		
		for (Map.Entry<String,String> entry : headers.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			out.println(key + ": " + value);
		}
		out.flush();
	}

}