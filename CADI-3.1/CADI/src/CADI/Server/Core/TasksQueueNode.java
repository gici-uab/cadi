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
package CADI.Server.Core;

import java.io.PrintStream;
import java.net.Socket;

import CADI.Common.Network.HTTP.HTTPRequest;
import CADI.Common.Network.JPIP.JPIPRequestFields;

/**
 * This is an auxiliary class used to save the tasks that will be done by a
 * <code>Worker</code> thread. Two different types of works can be definded:
 * <ul>
 * 	<li> If the <code>statusCode</code> is 0, it is a work to be processed by
 * 			{@link CADI.Server.Core.Worker} thread.
 *  
 * 	<li> If the <code>statusCode</code> is not 0, the task is for sending
 * 			an error message to the client. The status code of the HTTP response
 * 			is given through the <code>statusCode</code> attribute and the
 * 			reason phrase is given through the <code>reashonPhrase</code>
 * 			attribute. An the socket to send the data to the client is the
 * 			<code>socket</code> attribute.
 * </ul>
 * 
 * @see CADI.Server.Core.TasksQueue
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2008/02/21
 */
public class TasksQueueNode {

	/**
	 * This attribute is used to identify the kind of work. They are:
	 * <ul>
	 * 	<li> If it is 0, a common work is stored. Only the {@link #socket},
	 * 			{@link #httpRequest}, and {@link #jpipRequestFields}
	 * 			attributes are used.
	 * 	<li> Otherwise, the work is for sending an error message to the
	 * 			client. Therefore, the this attribute indicates the HTTP
	 * 			response, and the reasonPhrase of the HTTP response is given
	 * 			in the {@link #reasonPhrase} attribute.
	 * </ul> 
	 */
	int statusCode = -1;
	
	/**
	 * Is the socket that will used to communicate with the client.
	 */
	Socket socket = null;
	
	/**
	 * 
	 * 
	 * @see #statusCode
	 */	
	HTTPRequest httpRequest = null;
		
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.JPIPRequestFields}.
	 */
	JPIPRequestFields jpipRequestFields = null;
	
	/**
	 * Is the reason phrase of the HTTP response.
	 * 
	 * @see #statusCode
	 */
	String reasonPhrase = null;

	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param socket is the client socket.
	 * @param httpRequest
	 * @param jpipRequestFields
	 */	
	public TasksQueueNode(Socket socket, HTTPRequest httpRequest, JPIPRequestFields jpipRequestFields) {
		if (socket == null) throw new NullPointerException();
		if (httpRequest == null) throw new NullPointerException();
		if (jpipRequestFields == null) throw new NullPointerException();
		
		statusCode = 0;
		this.socket = socket;
		this.httpRequest = httpRequest;
		this.jpipRequestFields = jpipRequestFields;		
	}	

	/**
	 * Constructor.
	 * 
	 * @param socket
	 * @param statusCode
	 * @param reasonPhrase
	 */
	public TasksQueueNode(Socket socket, int statusCode, String reasonPhrase) {
		if (socket == null) { throw new NullPointerException(); }
		if (statusCode <= 0) { throw new IllegalArgumentException(); }
		
		this.socket = socket;
		this.statusCode = statusCode;
		this.reasonPhrase = reasonPhrase;
	}
	
	/**
	 * Returns the status code. See {@link #statusCode}.
	 * 
	 * @return the status code
	 */
	public int getStatusCode() {
		return statusCode;
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
	 * @return the {@link #httpRequest}.
	 */
	public HTTPRequest getHttpRequestFields() {
		return httpRequest;
	}

	/**
	 * Returns the {@link #jpipRequestFields} attribute.
	 * 
	 * @return the {@link #jpipRequestFields}.
	 */
	public JPIPRequestFields getJpipRequestFields() {
		return jpipRequestFields;
	}
	
	/**
	 * Returns the {@link #reasonPhrase} attribute.
	 * 
	 * @return the {@link #reasonPhrase}.
	 */
	public String getReasonPhrase() {
		return reasonPhrase;
	}

	/**
	 * Set attributes to its initial values.
	 */
	public void reset() {
		statusCode = -1;
		socket = null;
		httpRequest.reset();
		jpipRequestFields.reset();
	}

	/**
	 * For debugging purposes. 
	 */
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";

		str += "Status code=" + statusCode + "\n";
		str += socket.toString() + "\n";
		if (statusCode == 0) {
			str += httpRequest.toString() + "\n";
			str += jpipRequestFields.toString() + "\n";
		} else {
			str += "Reason phrase=" + reasonPhrase + "\n";
		}

		str += "]";
		return str;
	}
	
	/**
	 * Prints this Tasks Queue Node fields out to the specified output stream.
	 * This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Tasks Queue Node --");		
		
		out.println("Status code: " + statusCode);
		out.println(socket.toString());
		if (statusCode == 0) {
			httpRequest.list(out);
			jpipRequestFields.list(out);
		} else {
			out.println("Reason phrase: " + reasonPhrase);
		}
						
		out.flush();
	}
	
}