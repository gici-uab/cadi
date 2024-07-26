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
package CADI.Server.Request;

import java.io.PrintStream;
import java.net.Socket;

import CADI.Common.Network.HTTP.HTTPRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * This is an auxiliary class used to save the socket, the HTTP request line,
 * and the HTTP request headers for each client in the <code>RequestQueue
 * </code>.
 * 
 * @see CADI.Server.Request.RequestQueue
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.1 2011/05/31
 */
public class RequestQueueNode {

	/**
	 * Contains the client socket.
	 */
	protected Socket socket = null;
	
	/**
	 * Is the HTTP request sent by the client.
	 */
	protected HTTPRequest httpRequest = null;
	
	// ============================= public methods ==============================	
	/**
	 * Constructor.
	 * 
	 * @param socket definition in {@link #socket}.
	 * @param httpRequest definition in {@link #httpRequest}.
	 */
	public RequestQueueNode(Socket socket, HTTPRequest httpRequest) {
		if (socket == null) throw new NullPointerException();
		if (httpRequest == null) throw new NullPointerException();
		
		this.socket = socket;
		this.httpRequest = httpRequest;
	}
	
	/**
	 * Returns the client socket.
	 * 
	 * @return client socket
	 */
	public Socket getSocket() {
		return socket;
	}
	
	/**
	 * Returns the {@link #httpRequest} attribute.
	 * 
	 * @return the {@link #httpRequest} attribute.
	 */
	public HTTPRequest getHTTPRequest() {
		return httpRequest;
	}
			
	/**
	 * Set attributes to its initials values.
	 */
	public void reset() {
		socket = null;
		httpRequest = null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";

		str = getClass().getName() + " [\n";
		str += socket.toString();
		str += "\n";
		
		String requestLine = httpRequest.getRequestURI();
		HashMap<String,String> headers = httpRequest.getHTTPHeaders();
		if (requestLine != null) {
			str += requestLine + "\n";
			String key, value;
      for (Map.Entry<String,String> entry : headers.entrySet()) {
				key = entry.getKey();
				value = entry.getValue();
				str += key + ": " + value + "\n";
			}
		} else {
			str += "<<< None request has been found >>>\n";
		}
		
		str += "]";
		return str;
	}
	
	/**
	 * Prints this Request queue node fields out to the specified output
	 * stream. This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Request queue node --");		
		
		out.println(socket.toString());
		
		String requestLine = httpRequest.getRequestURI();
		HashMap<String,String> headers = httpRequest.getHTTPHeaders();
		if (requestLine != null) {
			out.println(requestLine);
			String key, value;
			for (Map.Entry<String,String> entry : headers.entrySet()) {
				key = entry.getKey();
				value = entry.getValue();
				out.println(key + ": " + value);
			}
		} else {
			out.println("<<< None request has been found >>>");
		}
		
		out.flush();
	}
	
}