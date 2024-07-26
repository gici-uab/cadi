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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

import CADI.Common.Log.CADILog;
import CADI.Common.Network.HTTP.HTTPRequest;
import CADI.Common.Network.HTTP.HTTPResponse;
import CADI.Server.Network.HTTPResponseSender;
import GiciException.ErrorException;


/**
 * This class listens to the client requests and enqueues them.
 * <p>
 * None prioritization criterion is performed. Request are enqueued in the same
 * order they are received.
 * <p>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; run<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.2 2009/08/16
 */
public class RequestListener extends Thread {

	/**
	 * Port where the server is listening to.
	 * Definition in {@link CADI.Server.Core.Scheduler#ports}.
	 */
	private int port;

	/**
	 * It is a queue where the client request are stored. This queue is shared
	 * memory between the <data>daemon</data> which stored the client requests
	 * and the <data>RequestDispatcher</data> which gets them to process.
	 * <p>
	 * Definition in {@link CADI.Server.Core.Scheduler#requestQueue}.
	 */
	private RequestQueue requestQueue = null;

	/**
	 * Definition in {@link CADI.Server.Core.Scheduler#log}.
	 */
	private CADILog log = null;	
	
	/**
	 * Indicates if the thread must be finished.
	 */
	volatile boolean finish = false;
	
	
	// INTERNAL ATTRIBUTES

	/**
	 * Server socket where server is listening to client requests.
	 */
	private ServerSocket serverSocket;

	/**
	 * A object that reads the HTTP request from a input stream.
	 */
	private HTTPRequestReader httpRequestReader = null;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param threadName is the name of the thread.
	 * @param port it is the port where the server will listen to the client
	 * 		requests
	 * @param requestQueue it is the queue where the received client request
	 * 		will be saved.
	 * @param log
	 * 
	 * @throws ErrorException if the socket server can not be opened.
	 */
	public RequestListener(String threadName, int port, RequestQueue requestQueue,
													CADILog log) throws ErrorException {
		
		if (port <= 0) {
			throw new IllegalArgumentException();
		}
		
		if (requestQueue == null) {
			throw new NullPointerException();
		}
		
		this.port = port;
		this.requestQueue = requestQueue;
		this.log = log;

		httpRequestReader = new HTTPRequestReader();
		
		setName(threadName);
		
		// Opens the server socket
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			throw new ErrorException("Server socket can not be opened. The port "
					+ port+ " is aready openned or you do not have permissions");
		}
		
		log.logInfo(getName() + ": started");		
	}

	/**
	 * Listens to in a port and receives the client requests. All recieved
	 * request are stored in a shared queue.
	 * <p>
	 * The method will be running meanwhile the <code>finish</code> attribute
	 * is <code>true</code>.  
	 */
  @Override
	public void run() {
		
		boolean error = false;

		log.logInfo(getName()+": listening to client connections (port "+port+")");

			
		// Main loop 	
		while (!finish) {
			Socket socket = null;
			error = false;
			
			// Accept connection request
			try{			
				socket = serverSocket.accept();
			} catch (IOException e) {
				error = true;
			}
			
			if (!error) {
				try {
					// Read HTTP request
					BufferedReader bufferedReader =
						new BufferedReader(new InputStreamReader(socket.getInputStream()));
					HTTPRequest httpRequest =
						httpRequestReader.readHTTPRequest(bufferedReader);				
					
					// Adds request to request queue. Then it runs the request dispatcher.				
					requestQueue.add(new RequestQueueNode(socket, httpRequest));
				} catch (ErrorException ee) {
					sendHTTPResponseError(socket, ee.getErrorCode(), ee.getMessage());
					error = true;
				} catch (SocketTimeoutException ste) {
					error = true;
				} catch (IOException e) {
					error = true;
					// <<<<<< to log file
				}
				
				if (error) {
					try {
						socket.close();
					} catch (IOException e) {}
				}
			}
		}   		

		// Close connections
		try {
			serverSocket.close();
		} catch (IOException e) {}   		

		log.logInfo(getName() + ": stopped");
	}
		
	/**
	 * This method is used to indicate that the thread has to finish.
	 */
	public synchronized void finish() {
		this.finish = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#toString()
	 */
  @Override
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";

		str += "Port="+port;
		if (requestQueue != null) str += ", RequestQueue="+requestQueue.toString();
		if (log != null) str += ", Log="+log.toString();	
		str += ", Finish="+finish;
		//if (serverSocket != null) showSocket(serverSocket);
		if (httpRequestReader != null)
			str += ", HTTPRequestReader="+httpRequestReader.toString();

		str += "]";
		return str;
	}
	
	/**
	 * Prints this Request listener out to the specified output stream.
	 * This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Request listener --");		
		
		out.println("Port: "+port);
		if (requestQueue != null) requestQueue.list(out);
		if (log != null) log.list(out);	
		out.println("Finish: "+finish);
		if (serverSocket != null) showServerSocket(serverSocket);
		if (httpRequestReader != null) httpRequestReader.list(out);
						
		out.flush();
	}
	
	/**
	 * For debugging purposes. It shows all socket information.
	 * 
	 * @param socket the socket whose information will be shown.
	 * @throws IOException this exception will be thrown if an error has occurred.
	 */
	private void showSocket(Socket socket) throws IOException {

		try {
			System.out.println("-------------------------");
			System.out.println("Server Socket Information");
			System.out.println("-------------------------");
			System.out.println("serverSocket         : "+socket);
			System.out.println("Keep Alive           : "+socket.getKeepAlive());
			System.out.println("Receive Buffer Size  : "
					+socket.getReceiveBufferSize());
			System.out.println("Send Buffer Size     : "+socket.getSendBufferSize());
			System.out.println("Is Socket Bound?     : "+socket.isBound());
			System.out.println("Is Socket Connected? : "+socket.isConnected());
			System.out.println("Is Socket Closed?    : "+socket.isClosed());
			System.out.println("So Timeout           : "+socket.getSoTimeout());
			System.out.println("So Linger            : "+socket.getSoLinger());
			System.out.println("TCP No Delay         : "+socket.getTcpNoDelay());
			System.out.println("Traffic Class        : "+socket.getTrafficClass());
			System.out.println("Socket Channel       : "+socket.getChannel());
			System.out.println("Reuse Address?       : "+socket.getReuseAddress());
			System.out.println("\n");

			// --------------------------------------
			// Get (Server) InetAddress / Socket Information
			// --------------------------------------
			InetAddress inetAddrServer = socket.getInetAddress();

			System.out.println("---------------------------");
			System.out.println("Remote (Server) Information");
			System.out.println("---------------------------");
			System.out.println("InetAddress - (Structure): "+inetAddrServer);
			System.out.println("Socket Address - (Remote): "
					+socket.getRemoteSocketAddress());
			System.out.println("Canonical Name           : "
					+inetAddrServer.getCanonicalHostName());
			System.out.println("Host Name                : "
					+inetAddrServer.getHostName());
			System.out.println("Host Address             : "
					+inetAddrServer.getHostAddress());
			System.out.println("Port                     : "+socket.getPort());

			System.out.print("RAW IP Address - (byte[]): ");
			byte[] b1 = inetAddrServer.getAddress();
			for (int i=0; i< b1.length; i++) {
				if (i > 0) {System.out.print(".");}
				System.out.print(b1[i] & 0xff);
			}
			System.out.println();

			System.out.println("Is Loopback Address?  : "
					+inetAddrServer.isLoopbackAddress());
			System.out.println("Is Multicast Address? : "
					+inetAddrServer.isMulticastAddress());
			System.out.println("\n");


			// ---------------------------------------------
			// Get (Client) InetAddress / Socket Information
			// ---------------------------------------------
			InetAddress inetAddrClient = socket.getLocalAddress();

			System.out.println("--------------------------");
			System.out.println("Local (Client) Information");
			System.out.println("--------------------------");
			System.out.println("InetAddress - (Structure): "+inetAddrClient);
			System.out.println("Socket Address - (Local) : "
					+socket.getLocalSocketAddress());
			System.out.println("Canonical Name           : "
					+inetAddrClient.getCanonicalHostName());
			System.out.println("Host Name                : "
					+inetAddrClient.getHostName());
			System.out.println("Host Address             : "
					+inetAddrClient.getHostAddress());
			System.out.println("Port                     : "+socket.getLocalPort());

			System.out.print("RAW IP Address - (byte[])  : ");
			byte[] b2 = inetAddrClient.getAddress();
			for (int i=0; i< b2.length; i++) {
				if (i > 0) {System.out.print(".");}
				System.out.print(b2[i] & 0xff);
			}
			System.out.println();

			System.out.println("Is Loopback Address?     : "
					+inetAddrClient.isLoopbackAddress());
			System.out.println("Is Multicast Address?    : "
					+inetAddrClient.isMulticastAddress());
			System.out.println("\n");

		} catch (IOException e) {

			e.printStackTrace();

		}
	}
	
	/**
	 * For debugging purposes. It shows all socket information.
	 * 
	 * @param socket the socket whose information will be shown.
	 * @throws IOException this exception will be thrown if an error has occurred.
	 */
	private void showServerSocket(ServerSocket socket) {

		try {
			System.out.println("-------------------------");
			System.out.println("Server Socket Information");
			System.out.println("-------------------------");
			System.out.println("serverSocket         : "+socket);
			System.out.println("Receive Buffer Size  : "
					+socket.getReceiveBufferSize());
			System.out.println("Is Socket Bound?     : "+socket.isBound());
			System.out.println("Is Socket Closed?    : "+socket.isClosed());
			System.out.println("So Timeout           : "+socket.getSoTimeout());
			System.out.println("Socket Channel       : "+socket.getChannel());
			System.out.println("Reuse Address?       : "+socket.getReuseAddress());
			System.out.println("\n");

			// --------------------------------------
			// Get (Server) InetAddress / Socket Information
			// --------------------------------------
			InetAddress inetAddrServer = socket.getInetAddress();

			System.out.println("---------------------------");
			System.out.println("Remote (Server) Information");
			System.out.println("---------------------------");
			System.out.println("InetAddress - (Structure): "+inetAddrServer);
			System.out.println("Canonical Name           : "
					+inetAddrServer.getCanonicalHostName());
			System.out.println("Host Name                : "
					+inetAddrServer.getHostName());
			System.out.println("Host Address             : "
					+inetAddrServer.getHostAddress());

			System.out.print("RAW IP Address - (byte[]): ");
			byte[] b1 = inetAddrServer.getAddress();
			for (int i=0; i< b1.length; i++) {
				if (i > 0) {System.out.print(".");}
				System.out.print(b1[i] & 0xff);
			}
			System.out.println();

			System.out.println("Is Loopback Address?  : "
					+inetAddrServer.isLoopbackAddress());
			System.out.println("Is Multicast Address? : "
					+inetAddrServer.isMulticastAddress());
			System.out.println("\n");


			// ---------------------------------------------
			// Get (Client) InetAddress / Socket Information
			// ---------------------------------------------
			SocketAddress inetAddrClient = socket.getLocalSocketAddress();

			System.out.println("--------------------------");
			System.out.println("Local (Client) Information");
			System.out.println("--------------------------");
			System.out.println("InetAddress - (Structure): "+inetAddrClient);
			System.out.println("Socket Address - (Local) : "
					+socket.getLocalSocketAddress());
			System.out.println("Port                     : "+socket.getLocalPort());

		} catch (IOException e) {

			e.printStackTrace();

		}
	}
	
	// ============================ private methods ==============================
	/**
	 * This method will be used to send an HTTP error response to the client.
	 * 
	 * @param statusCode definition in
	 * 	{@link CADI.Common.Network.HTTP.StatusCodes}.
	 * @param reasonPhrase a reason phrase related with the status code.
	 */
	private void sendHTTPResponseError(Socket socket, int statusCode,
																			String reasonPhrase) {
		
		HTTPResponseSender httpResponseSender = null;
		try {
			httpResponseSender = new HTTPResponseSender(socket.getOutputStream());
		} catch (IOException e1) {
			log.logInfo(getName() + ": remote host ("
					+socket.getRemoteSocketAddress()
					+") has been closed unexpectedly or it is unreachable");
			return;
		}
		HTTPResponse httpResponse = new HTTPResponse();
		
		httpResponse.setResponseCode(statusCode);
		httpResponse.setResponseMessage(reasonPhrase);
		
		log.logInfo(getName()+": HTTP Response header:\n"+httpResponse.toString());
		
		try {
			httpResponseSender.sendHeaders(httpResponse);
			httpResponseSender.endOfHeaders();
		} catch (IOException e) {
			log.logInfo(getName()+": remote host ("+socket.getRemoteSocketAddress()
					+ ") has been closed unexpectedly or it is unreachable");
		}
	}
}
