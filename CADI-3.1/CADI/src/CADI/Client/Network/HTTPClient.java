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
package CADI.Client.Network;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import CADI.Common.Log.CADILog;
import CADI.Common.Network.HTTP.HTTPRequest;
import CADI.Common.Network.HTTP.HTTPResponse;
import CADI.Common.Network.HTTP.StatusCodes;
import CADI.Common.Network.JPIP.JPIPResponseFields;
import GiciException.WarningException;
import java.util.HashMap;

/**
 * This class implements a HTTP client.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.4 2011/05/31
 */
public class HTTPClient {
	
	/**
	 * This attribute is used to read the http response in a easy way.
	 */
	private HTTPResponseReader httpResponseReader = null;	
	
	/**
	 * This object contains the decoded JPIP response fields of the last
	 * response.
	 */
	private JPIPResponseFields jpipResponseFields = null;

	/**
	 * Indicates if http keep alive mode is set.
	 */
	private boolean keepAlive = false;
	
	/**
	 * Is the local host name.
	 */
	private String host = null;
		
	/**
	 * Is the client user agent.
	 */
	private String userAgent = null;

  /**
	 * Is a feature whose content is not defined. It is only for debugging
   * purposes.
	 */
	private String debug = null;

  /**
   * 
   */
  private String via = null;

	/**
	 * 
	 */
	private ArrayList<String> accept = null;
	
	/**
	 * Definition in {@link CADI.Client.Client#log}
	 */
	private CADILog log = null;

	
	// INTERNAL ATTRIBUTES

	/**
	 * It is the socket that will be used to send the client request to the
	 * server and to receive the response from the server.
	 */
	private ClientSocket clientSocket = null;
	
	/**
	 * Is the maximum number of attempts to send a request or receive a response
	 */
	private static final int MAX_ATTEMPTS = 2;

	/**
	 * Indicates the HTTP version.
	 */
	public static final String HTTP_VERSION_11 = "HTTP/1.1";
	
	/**
	 * Is the carriage and return line.
	 */
	public static final byte[] CRLF = {(byte)13, (byte)10};
	
	
	// REQUEST ATTRIBUTES

	/**
	 * This object contains the HTTP request to be sent.
	 */
	private HTTPRequest httpRequest = null;
		
	/**
	 *  Allowed HTTP methods
	 */
	public static final String[] methodsList = {
		"GET"//, "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
		//"GET", "POST", "HEAD", "PUT", "DELETE", "TRACE", "CONNECT"
	};
	private final static Set methods = new HashSet<String>(Arrays.asList(methodsList));
		
	/**
	 * Allowed HTTP headers.
	 */
	public final static String[] httpRequestHeadersList = {
		 // GENERAL HEADER	(RFC 2616 section 4.5)
		"Cache-Control",
		"Connection",
		"Date",
		"Pragma",
		"Trailer",
		"Transfer-Encoding",
		"Upgrade",
		"Via",
		"Warning",
		// REQUEST HEADER (RFC 2616 section 5.3)
		"Accept",
		"Accept-Charset",
		"Accept-Encoding",
		"Accpet-Language",
		"Authorization",
		"Expect",
		"From",
		"Host",
		"If-Match",
		"If-Modified-Since",
		"If-None-Match",
		"If-Range",
		"If-Unmodified-Since",
		"Max-Forwards",
		"Proxy-Authorization",
		"Range",
		"Referer",
		"TE",
		"User-Agent",
		// ENTITY HEADER 	(RFC 2616 section 7.1)
		"Allow",
		"Content-Encoding",
		"Content-Language",
		"Content-Length",
		"Content-Location",
		"Content-MD5",
		"Content-Range",
		"Content-Type",
		"Expires",
		"Last-Modified",
    // DEBUG PURPOSES
    "Debug"
		 };
	
	/**
	 * @see #httpRequestHeadersList
	 */
	private final static Set httpRequestHeaders = new HashSet<String>(Arrays.asList(httpRequestHeadersList));
		
		
	// RESPONSE ATTRIBUTES
	
	/**
	 * This object contains the last response that has been received.
	 * 
	 * @see CADI.Common.Network.HTTP.HTTPResponse
	 */
	private HTTPResponse httpResponse = null;
	
	/**
	 * This object contains the last received JPIP fields in the HTTP response.
	 * 
	 */
	private JPIPResponseFieldsParser jpipResponseFieldsParser = null;
	
	/**
	 * Allowed HTTP response headers.
	 * <p>
	 * NOTICE: The extended HTTP response headers to support the JPIP protocol
	 * are not included.
	 */
	private final static String[] httpResponseHeadersList = {
		// GENERAL HEADER	(RFC 2616 section 4.5)
		"Cache-Control",
		"Connection",
		"Date",
		"Pragma",
		"Trailer",
		"Transfer-Encoding",
		"Upgrade",
		"Via",
		"Warning",
		// REQUEST HEADER (RFC 2616 section 5.2)
		"Accept-Ranges",
		"Age",
		"ETag",
		"Location",
		"Proxy-Authenticate",
		"Retry-After",
		"Server",
		"Vary",
		"WWW-Authenticate",
		// ENTITY HEADER 	(RFC 2616 section 7.1)
		"Allow",
		"Content-Encoding",
		"Content-Language",
		"Content-Length",
		"Content-Location",
		"Content-MD5",
		"Content-Range",
		"Content-Type",
		"Expires",
		"Last-Modified",
    // DEBUGGING PURPOSES
    "Debug"
	};	
	
	/**
	 * @see #httpResponseHeadersList
	 */
	private final static Set<String> httpResponseHeaders = new HashSet<String>(Arrays.asList(httpResponseHeadersList));
		
	/**
	 * Allowed JPIP headers in the HTTP response.
	 * 
	 * @see #httpResponseHeaders
	 */
	private final static String[] jpipResponseHeadersList = {
		// ISO/IEC 15444-9 Annex D
		"JPIP-tid",
		"JPIP-cnew",
		"JPIP-qid",
		"JPIP-fsiz",
		"JPIP-rsiz",
		"JPIP-roff",
		"JPIP-comps",
		"JPIP-stream",
		"JPIP-context",
		"JPIP-roi",
		"JPIP-layers",
		"JPIP-srate",
		"JPIP-metareq",
		"JPIP-len",
		"JPIP-quality",
		"JPIP-type",
		"JPIP-mset",
		"JPIP-cap",
		"JPIP-pref"
	};	

	/**
	 * @see #jpipResponseHeadersList
	 */
	private final static Set<String> jpipResponseHeaders = new HashSet<String>(Arrays.asList(jpipResponseHeadersList));
	
	// ============================= public methods ==============================
  /**
	 * Constructor.
	 *
	 * @param keepAlive
	 * @param log
	 */
	public HTTPClient(String host, boolean keepAlive, CADILog log) {
		// Check input parameters
		if (host == null) throw new NullPointerException();
		if (log == null) throw new NullPointerException();

		// Copy input parameters
		this.host = host;
		this.keepAlive = keepAlive;
		this.log = log;


		// Initializations
		clientSocket = new ClientSocket();
		httpRequest = new HTTPRequest();
		httpResponseReader = new HTTPResponseReader();
		jpipResponseFieldsParser = new JPIPResponseFieldsParser();
	}

	/**
	 * Constructor.
	 * 
	 * @param keepAlive
	 * @param log
	 */
	public HTTPClient(String host, String userAgent, boolean keepAlive, CADILog log) {
		// Check input parameters
		if (host == null) throw new NullPointerException();
		if (userAgent == null) throw new NullPointerException();
		if (log == null) throw new NullPointerException();
		
		// Copy input parameters
		this.host = host;
		this.userAgent = userAgent;
		this.keepAlive = keepAlive;
		this.log = log;

		
		// Initializations
		clientSocket = new ClientSocket();
		httpRequest = new HTTPRequest();
		httpResponseReader = new HTTPResponseReader();
		jpipResponseFieldsParser = new JPIPResponseFieldsParser();
	}
	
	/**
	 * 
	 * @param requestURI
	 * @throws IOException
	 * @throws WarningException
	 */
	public void sendRequest(String requestURI) throws IOException, WarningException {
		sendRequest(requestURI, null, -1);
	}
	
	/**
	 * Sends a request URI to the server. The request must be an absolute
	 * URI because the scheme and authority are needed to known where the
	 * request will be sent.
	 * 
	 * @param requestURI the request to send.
	 * 
	 * @throws WarningException 
	 * @throws IOException
	 * 
	 * @see #sendRequest(String, int, String)
	 */
	public void sendRequest(String requestURI, String proxyServer, int proxyPort) throws IOException, WarningException {
		
		if (requestURI == null) throw new NullPointerException();
		
		URI uri = null;
		try {
			uri = new URI(requestURI);
		} catch (URISyntaxException e) {
			throw new WarningException("Malformed URI exception");
		}
		
		// Check absolut uri
		if ( !uri.isAbsolute() ) throw new IllegalArgumentException("This method can only be used with absolute URIs");
		
		// Check scheme
		if ( uri.getScheme().compareTo("jpip") != 0 ) throw new WarningException("JPIP URI must start with \"jpip\"");
		
		
		log.logInfo(uri.toString());
		
		// Get server and port
		String server = uri.getHost();
		int port = uri.getPort();
		
		// Send request
		sendRequest(server, port, requestURI, proxyServer, proxyPort);
	}
	
	/**
	 * Sends a request URI to the server. This request can be an absolute or
	 * relative request. If it is an abosolute request, the authorithy and port
	 * (if it is) must be the same of the <code>server</code> and <code>port
	 * </code> parameters. Otherwise, a {@link java.lang.IllegalArgumentException}
	 * will be thrown.
	 * <p>
	 * The <code>server</code> and <code>port</code> parameters are optionals
	 * when the <code>requestURI</code> is an absolute URI.
	 * 
	 * @param server the name of the remote host.
	 * @param port the port number on the remote host.
	 * @param requestURI the request URI to send.
	 * 
	 * @throws WarningException 
	 * @throws IOException
	 * 
	 * @see #sendRequest(String)
	 */
	public void sendRequest(String server, int port, String requestURI) throws IOException, WarningException {
		if (server == null) throw new NullPointerException();
		
		sendRequest(server, port, requestURI, null, -1);
	}
	
	/**
	 * Sends a request URI to the server. This request can be an absolute or
	 * relative request. If it is an abosolute request, the authorithy and port
	 * (if it is) must be the same of the <code>server</code> and <code>port
	 * </code> parameters. Otherwise, a {@link java.lang.IllegalArgumentException}
	 * will be thrown.
	 * <p>
	 * The <code>server</code> and <code>port</code> parameters are optionals
	 * when the <code>requestURI</code> is an absolute URI.
	 * 
	 * @param server the name of the remote host.
	 * @param port the port number on the remote host.
	 * @param requestURI the request URI to send.
	 * @param proxyServer is the proxy server used to connect to the JPIP server
	 * @param proxyPort is the port in the proxy server
	 * 
	 * @throws WarningException 
	 * @throws IOException
	 * 
	 * @see #sendRequest(String)
	 */
	public void sendRequest(String server, int port, String requestURI, String proxyServer, int proxyPort) throws IOException, WarningException {

		// Check input parameters 
		if (requestURI == null) throw new NullPointerException();
		
		URI uri = null;
		try {
			uri = new URI(requestURI);
		} catch (URISyntaxException e) {
			throw new WarningException("Malformed URI exception");
		}
		
		// Check uri
		if (!uri.isAbsolute() && (server == null))
				throw new IllegalArgumentException("The \"server\" input parameter must be with relative URIs");
		
		if ( uri.isAbsolute() ) {
			if ( !uri.getScheme().equals("jpip") ) throw new WarningException("Scheme of the JPIP URI must be \"jpip\"");
			
			if (server == null) {
				server = uri.getHost();
			} else {
				if ( !server.equals(uri.getHost()) )
					throw new IllegalArgumentException("The \"remote host\" of the input parameter and the requestLine parameter is not the same");
			}

			if (port > 0) {
				if ((uri.getPort() > 0) && (port != uri.getPort()))
						throw new IllegalArgumentException("The \"port\" of the input parameter and the requestLine parameter is not the same");
			} else {
				if (uri.getPort() > 0) port = uri.getPort();
			}
		}
		
		// Create request
		if (proxyServer == null) {
			createRequest(server, port, uri.getPath(), uri.getQuery(), false);
		} else {
			createRequest(server, port, uri.getPath(), uri.getQuery(), true);
		}
		
		sendRequest(server, port, httpRequest, proxyServer, proxyPort);
	}
	
	/**
	 * 
	 * @param httpRequest
	 * @throws IOException
	 * @throws WarningException
	 */
	public void sendRequest(HTTPRequest httpRequest) throws IOException, WarningException {
		sendRequest(null, -1, httpRequest, null, -1);
	}
	
	/**
	 * 
	 * @param server
	 * @param port
	 * @param httpRequest
	 * @throws IOException
	 * @throws WarningException
	 */
	public void sendRequest(String server, int port, HTTPRequest httpRequest) throws IOException, WarningException {
		sendRequest(server, port, httpRequest, null, -1);
	}
	
	/**
	 * 
	 * @param server
	 * @param port
	 * @param httpRequest
	 * @param proxyServer
	 * @param proxyPort
	 * @throws IOException
	 * @throws WarningException
	 */
	public void sendRequest(String server, int port, HTTPRequest httpRequest, String proxyServer, int proxyPort) throws IOException, WarningException {
		
		// Check input parameters
		if (httpRequest == null) throw new NullPointerException();
		
		// Copy parameters
		this.httpRequest = httpRequest;
		
		// Check http request
		if ( !httpRequest.getRequestMethod().equalsIgnoreCase("GET") ) throw new IllegalArgumentException("Only the \"GET\" method is allowed");
		if (!httpRequest.getVersion().equalsIgnoreCase("HTTP/1.1")) throw new IllegalArgumentException("Only the HTTP 1.1 version is allowed");
		if (httpRequest.getHeaderField("Host") == null) throw new IllegalArgumentException("HTTP/1.1 version requires the \"Host\" header");
		
		//	 Check uri
		URI uri = httpRequest.getURI();
		
		if (!uri.isAbsolute() && (server == null))
			throw new IllegalArgumentException("The \"server\" input parameter must be with relative URIs");
		
		if ( uri.isAbsolute() ) {
			if ( !uri.getScheme().equals("jpip") ) throw new WarningException("Scheme of the JPIP URI must be \"jpip\"");
			
			if (server == null) {
				server = uri.getHost();
			} else {
				if ( !server.equals(uri.getHost()) )
					throw new IllegalArgumentException("The \"remote host\" of the input parameter and the requestLine parameter is not the same");
			}

			if (port > 0) {
				if ((uri.getPort() > 0) && (port != uri.getPort()))
						throw new IllegalArgumentException("The \"port\" of the input parameter and the requestLine parameter is not the same");
			} else {
				if (uri.getPort() > 0) port = uri.getPort();
			}
		} else {
			if (server.compareTo(httpRequest.getHeaderField("Host")) != 0) {
				throw new IllegalArgumentException("The \"server\" input parameter does not match with the HTTP's \"Host\" header");
			}
		}

		// Proxy: uri must be absolute
		if ((proxyServer != null) && !uri.isAbsolute()) throw new IllegalArgumentException("When a proxy is used, the URI must be absolute");
		
		//	Open connection or reconnect
		if (proxyServer == null) connect(server, port, false);
		else connect(proxyServer, proxyPort, false);
		
		int attempts = 0;
		boolean finish = false;

		while (!finish) {
			try {
				// Send request				
				log.logInfo("\n" +  httpRequest.toString()); // DEBUG
				httpResponseReader.empty();
				sendRequest(clientSocket.getOutputStream(), httpRequest);

				// Read response header
				readResponseHeader(httpResponseReader);
				log.logInfo("\n" + httpResponse.toString()); // DEBUG
				finish = true;
			} catch (IOException ioe) {
				attempts++;
				if (attempts >= MAX_ATTEMPTS) {
					log.logInfo("There is not connection to server " + clientSocket.getRemoteHost() + " on port " + clientSocket.getPort());
					throw new IOException("Connection closed by peer");
				}
				if (proxyServer == null) connect(server, port, true);
				else connect(proxyServer, proxyPort, true);
			}
		}
	}

	/**
	 * Returns the {@link #httpRequest} attribute.
	 * 
	 * @return definition in {@link #httpRequest}.
	 */
	public HTTPRequest getHTTPRequest() {
		return httpRequest;
	}
	
	/**
	 * Returns the http response reader object.
	 * 
	 * @return the http response reader object.
	 * 
	 * @see CADI.Client.Network.HTTPResponseReader
	 */
	public HTTPResponseReader getHTTPResponseReader() {
		return httpResponseReader;
	}

	/**
	 * Definition in {@link CADI.Common.Network.HTTP.HTTPResponse#getResponseCode()}.
	 */
	public int getStatusCode() {
		return httpResponse.getResponseCode();
	}
	
	/**
	 * Definition in {@link CADI.Common.Network.HTTP.HTTPResponse#getResponseMessage()}.
	 */
	public String getResponseMessage() {
		return httpResponse.getResponseMessage();
	}
	
	/**
	 * 
	 * @return
	 */
	public HTTPResponse getHTTPResponse() {
		return httpResponse;
	}
		
	/**
	 * Returns the {@link #jpipResponseFields} object.
	 * 
	 * @return the {@link #jpipResponseFields} object.
	 */
	public JPIPResponseFields getJPIPResponseFields() {		
		return jpipResponseFields;		
	}
	
	/**
	 * Sets the HTTP keep alive feature.
	 * 
	 * @param keepAlive if <code>true</code> the HTTP keep alive feature is set.
	 * 						Otherwise, it is not set.
	 */
	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;		
	}
	
	/**
	 * Returns whether the HTTP keep alive feature is or not set.
	 * 
	 * @return whether the HTTP keep alive feature is or not set.
	 */
	public boolean isKeepAlive() {
		return keepAlive;
	}
		
	/**
	 * Sets the user agent name.
	 * 
	 * @param userAgent user agent name. 
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

  /**
   * Sets the debug feature.
   * 
   * @param debug
   */
  public void setDebug(String debug) {
    this.debug = debug;
  }

  /**
	 * Sets the {@link #via} attribute.
	 *
	 * @param via definition in {@link #via}.
	 */
	public void setVia(String via) {
		this.via = via;
	}
	
	/**
	 * 
	 * @param accept
	 */
	public void setAccept(ArrayList<String> accept) {
		this.accept = accept;
	}
		
	/**
	 * Returns the last requested URI.
	 * 
	 * @return the last requested URI.
	 */
	public String getRequestURI() {
		URI uri = null;
		try {
			uri = new URI(httpRequest.getRequestURI());
		} catch (URISyntaxException e) {			
		}
		
		return "jpip://" + clientSocket.getRemoteHost() + ((uri.getPath() != null) ? uri.getPath() : "") + ((uri.getQuery() != null) ? ("?" + uri.getQuery()) : "");
	}
		
	/**
	 * Returns the decoded query component of the last request.
	 * 
	 * @return the decoded query component.
	 */
	public String getQuery() {
		URI uri = null;
		try {
			uri = new URI(httpRequest.getRequestURI());
		} catch (URISyntaxException e) {
			return null;
		}
		return uri.getQuery();
	}
	
	/**
	 * Sets the client host name.
	 * 
	 * @param host the client host name.
	 */
	public void setLocalHost(String host) {
		this.host = host;
	}
	
	/**
	 * Definition in {@link CADI.Client.Network.ClientSocket#getRemoteHost()}.
	 */
	public String getRemoteHost() {
		return clientSocket.getRemoteHost();
	}	

	/**
	 * Definition in {@link CADI.Client.Network.ClientSocket#getPort()}
	 */
	public int getPort() {
		return clientSocket.getPort();
	}
	
	/**
	 * Check if the HTTP response has a body or not.
	 * 
	 * @return <code>true</code> whether HTTP response has a body. Otherwise,
	 * 			returns <code>false</code>.
	 */
	public boolean isResponseBody() {
		boolean isBody = false;
		
		if (httpResponse.getHeaderField("Transfer-Encoding") != null) {
			isBody = true;
		} else if ( httpResponse.getHeaderField("Content-Length") != null ) {
			isBody = true;
		}
		
		return isBody;
	}
	
	/**
	 * 
	 *
	 */
	public void close() {
		clientSocket.close();
	}
	
	/**
	 * For debugging purpose
	 */
  @Override
	public String toString() {

		String str = "";

		str = getClass().getName() + " [";
		if (httpResponseReader != null) str += httpResponseReader.toString();
		if (jpipResponseFields != null) str += ", "+jpipResponseFields.toString();
		str += ", keep-alive="+keepAlive;
		if (host != null) str += ", host="+host;
		if (userAgent != null) str += ", user-agent="+userAgent;
    if (debug != null) str += ", debug="+debug;
		if (accept != null) str += ", accept="+accept;
		if (log != null) str += log.toString();
		//if (clientSocket != null) str += clientSocket.toString();
		if (httpRequest != null) str += httpRequest.toString();
		str += "]";

		return str;
	}

	/**
	 * Prints this HTTP Client out to the specified output stream. This method is
	 * useful for debugging.
	 * 
	 * @param out
	 *           an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- HTTP client --");
		
		if (httpRequest != null) httpRequest.list(out);
		if (httpResponseReader != null) httpResponseReader.list(out);
		if (jpipResponseFields != null) jpipResponseFields.list(out);
		out.println("keep-alive: "+keepAlive);
		if (host != null) out.println("host: "+host);
		if (userAgent != null) out.println("user-agent: "+userAgent);
    if (debug != null) out.println("debug: "+debug);
		if (accept != null) out.println("accept: "+accept);
		if (log != null) log.list(out);
		//if (clientSocket != null) clientSocket.list(System.out);
		if (httpRequest != null) httpRequest.list(out);

		out.flush();
	}

	
	// ============================ private methods ==============================
	//
	// CLIENT SOCKET
	//
	
	/**
	 * This method is used to open a socket with the remote host. This method
	 * takes into account if the keep alive feature (persistents connections)
	 * is or not set, and  
	 * 
	 * @param server
	 * @param port
	 * @param force
	 * @throws IOException
	 */
	private void connect(String server, int port, boolean force) throws IOException {
				
		assert(server != null);
		assert(port > 0);
		
		
		if ( !force && keepAlive
				&& (server.compareTo(clientSocket.getRemoteHost()) == 0)
				&& (port == clientSocket.getPort()) ) {
			if ( clientSocket.isConnected() ) {
				// Keep alive mode is set and remote host and port are the same, so do nothing
			} else {
				clientSocket.connect(server, port);				
				httpResponseReader.setInputStream(clientSocket.getInputStream());
			}
		} else {
			clientSocket.close();
			clientSocket = new ClientSocket();
			clientSocket.connect(server, port);			
			httpResponseReader.setInputStream(clientSocket.getInputStream());
		}				
	}

	
	//
	// REQUEST METHODS
	//
	
	/**
	 * Sets the method that will be used to send the HTTP request.
	 * 
	 * @param method definition in {@link CADI.Common.Network.HTTP.HTTPRequest#method}.
	 * 
	 * @throws ProtocolException if an error in the underlying protocol has
	 * 			occurred.
	 */
	private void setRequestMethod(String method) throws ProtocolException {	
		if ( !methods.contains(method) ) throw new ProtocolException ("The method \"" + method + "\" is not supported"); 
		httpRequest.setRequestMethod(method);
	}
	
	/**
	 * 
	 * @param server
	 * @param port
	 * @param path
	 * @param query
	 * @param absolute
	 */
	private void createRequest(String server, int port, String path, String query, boolean absolute) {

		assert(path != null);
		assert(server != null);
		assert(query != null);
	
		httpRequest.reset();
		
		// Method
		try {
			setRequestMethod("GET");
		} catch (ProtocolException e) {}
		
		// request uri
		String requestURI = "";
		if ( absolute ) {
			requestURI = "jpip://"+server;
			if (port > 0) requestURI += ":"+port;
			requestURI += path;
			if (query != null) requestURI += "?"+query;

		} else {
			requestURI = path;
			if (query != null) requestURI += "?"+query;
		}		
		httpRequest.setRequestURI(requestURI);
		
		// Version
		httpRequest.setVersion("HTTP/1.1");
		
		// Headers
		httpRequest.setHeaderField("Host", server);
		if (userAgent != null) httpRequest.setHeaderField("User-Agent", userAgent);
    if (debug != null) httpRequest.setHeaderField("Debug", debug);
    if (via != null) httpRequest.setHeaderField("Via", via);
		if ((accept != null) && (accept.size() > 0)) {
			String acceptOptions = accept.get(0);
			for (int i = 1; i < accept.size(); i++)
				acceptOptions += ","+accept.get(i);
			httpRequest.setHeaderField("Accept", acceptOptions);
		}
		httpRequest.setHeaderField("Connection", (keepAlive ? "keep-alive" : "close"));
	}
		
	/**
	 * Sends the HTTP request through the output stream.
	 * 
	 * @param os the output stream
	 * @throws IOException
	 */
	private void sendRequest(OutputStream os) throws IOException {
		sendRequest(os, this.httpRequest);
	}
	
	/**
	 *  Sends the HTTP request through the output stream.
	 * 
	 * @param os the output stream
	 * @param httpRequest
	 *  
	 * @throws IOException
	 */
	private void sendRequest(OutputStream os, HTTPRequest httpRequest) throws IOException {

		String requestLine, header;
		
		// Send request line
		requestLine = httpRequest.getRequestMethod() + " " + httpRequest.getRequestURI() + " " + httpRequest.getVersion();
		
		os.write(requestLine.getBytes());
		os.write(CRLF);
		os.flush();
		log.logInfo(requestLine);

		// Send request headers
		//String key, value;
    String value;
		//for (Enumeration<String> e = httpRequest.getHeaders(); e.hasMoreElements(); ) {
    for (String key : httpRequest.getHeaders()) {
			//key = e.nextElement();
			value = httpRequest.getHeaderField(key);
			header = key +": " + value;						
			os.write(header.getBytes());
			os.write(CRLF);
			os.flush();
		}	
		
		//	Request ends with CRLF		
		os.write(CRLF);
		os.flush();
	}
	
	
	//
	// RESPONSE METHODS
	//
	
	/**
	 * Decodes a http response which is recieved in the inputStream.
	 * 
	 * @param httpResponseReader definition in {@link #httpResponseReader}.
	 * 
	 * @throws WarningException if an error is found while http response is
	 * 	being decoded.
	 * @throws IOException if there is none data to read from the input stream.
	 * 	It might be because input stream is closed by remote host.
	 */
	private void readResponseHeader(HTTPResponseReader httpResponseReader) throws WarningException, IOException {
		
		// Check input parameters
		if (httpResponseReader == null) throw new NullPointerException();
		
		// Create a response object
		httpResponse = new HTTPResponse();
		
		// Read http response line
		String statusLine = httpResponseReader.readLine();				
		if ( (statusLine == null) || (statusLine.length() <= 0) ) throw new IOException();
		
		parseStatusLine(statusLine);
		if ( !((httpResponse.getResponseCode() == StatusCodes.OK) || (httpResponse.getResponseCode() == StatusCodes.ACCEPTED)) ){
			return;
		}
		
		// Read http response header
		String str = null;		
		int pos;
		String key, value;
		while ( (str = httpResponseReader.readLine()) != null) {

			if (str.length() <= 0) break;	// End of section has been reached
			
			pos = str.indexOf(": ");
			if (pos < 0) {
				throw new IOException();
			}
			
			key = str.substring(0, pos);
			value = str.substring(pos+2, str.length());
			
			if ( (key == null) || (value == null)
					|| (key.length() == 0) || (value.length() == 0)) {
				throw new IOException();
			}
			
			if ( !( httpResponseHeaders.contains(key) || jpipResponseHeaders.contains(key) ) ) {
				throw new IOException();
			}
			
			httpResponse.setHeaderField(key, value);			
		}
		
		// Parse http response headers
		parseHTTPResponseHeaders(getHTTPResponseHeaders());
				
		// Parse jpip response headers
		jpipResponseFieldsParser = new JPIPResponseFieldsParser();
		jpipResponseFieldsParser.parse(getJPIPResponseHeaders());
		jpipResponseFields = jpipResponseFieldsParser.getJPIPResponseFields();
		
		// Set the transfer encoding of the message body
		httpResponseReader.setTransferEncoding(httpResponse.getHeaderField("Transfer-Encoding"), httpResponse.getHeaderField("Content-Length") );
	}
	
	/**
	 * Parses the status line of the HTTP response.
	 * 
	 * @param statusLine the status line of the HTTP response.
	 * 
	 * @throws ProtocolException
	 * @throws WarningException
	 */
	private void parseStatusLine(String statusLine) throws ProtocolException, WarningException {

		// Get and analize the status line. The first line must be the status line
		// Line format: HTTP-Version SP Status-Code SP Reason-Phrase CRLF
		
		// HTTP version must be HTTP/1.1
		if ( !statusLine.startsWith(HTTP_VERSION_11) ) {
			throw new ProtocolException("Invalid version of the HTTP protocol");
		}		

		// Get the status code
		int firstSpacePosition = statusLine.indexOf(' ');
		if (firstSpacePosition == -1) {
			throw new WarningException("Wrong server response.");
		}
		
		int secondSpacePosition = statusLine.indexOf(' ', firstSpacePosition+1);
		if (secondSpacePosition == -1) {
			throw new WarningException("Wrong server response.");
		}
		
		httpResponse.setResponseCode(Integer.parseInt(statusLine.substring(firstSpacePosition+1, secondSpacePosition)));
		
		// Get the reason phrase		
		httpResponse.setResponseMessage(statusLine.substring(secondSpacePosition+1));
		
		// Analize status code
		switch(httpResponse.getResponseCode()) {
			case StatusCodes.OK:
				break;

			case StatusCodes.ACCEPTED:
				httpResponse.setResponseCode(StatusCodes.ACCEPTED);
				httpResponse.setResponseMessage("Server error: " + httpResponse.getResponseMessage() + "\n\nExplanation:\nThe view-window request was accetable, but a subsequent view-window request was found");
				break;
				
			case StatusCodes.BAD_REQUEST:
				httpResponse.setResponseCode(StatusCodes.BAD_REQUEST);
				httpResponse.setResponseMessage(httpResponse.getResponseMessage() + "\n\nExplanation:\nClient request is incorrectly formatted\n or contains an unrecognized fields");
				break;
				
			case StatusCodes.NOT_FOUND:
				httpResponse.setResponseCode(StatusCodes.NOT_FOUND);
				httpResponse.setResponseMessage(httpResponse.getResponseMessage() + "\n\nExplanation:\nThe request resource is not in the server,\n or it is an unauthorized access, \n or time limit has expired");
				break;
				
			case StatusCodes.UNSUPPORTED_MEDIA_TYPE:
				httpResponse.setResponseCode(StatusCodes.UNSUPPORTED_MEDIA_TYPE);
				httpResponse.setResponseMessage(httpResponse.getResponseMessage() + "\n\nExplanation:\nThe single image type specified in the Image Return Type request field cannot be servided");
				break;
				
			case StatusCodes.NOT_IMPLEMENTED:
				httpResponse.setResponseCode(StatusCodes.NOT_IMPLEMENTED);
				httpResponse.setResponseMessage("Server error: The request requires services which are not implemented" + "\n\nExplanation:\n" + httpResponse.getResponseMessage());
				break;
				
			case StatusCodes.SERVICE_UNAVAILABLE:
				httpResponse.setResponseCode(StatusCodes.SERVICE_UNAVAILABLE);
				httpResponse.setResponseMessage("Server error: " + httpResponse.getResponseMessage() + "\n\nExplanation:\nThe channel id in the Channel ID field is invalid");
				break;
				
			default:
				throw new WarningException("Server error: " + httpResponse.getResponseMessage(), httpResponse.getResponseCode());
		}	
	}
	
	/**
	 * 
	 * @param httpHeaders
	 */
	private void parseHTTPResponseHeaders(HashMap<String, String> httpHeaders)
          throws ProtocolException, WarningException {
		
		// TODO: this parser has not been done yet
			
	}
	
	/**
	 * Returns the HTTP response headers without the headers added by the JPIP
	 * protocol. 
	 * 
	 * @return the HTTP response headers.
	 */	
	private HashMap<String, String> getHTTPResponseHeaders() {
		HashMap<String, String> httpHeaders = new HashMap<String, String>();
		
     for (String key: httpResponse.getHeaders()) {
			if ( httpResponseHeaders.contains(key) ) {				
				httpHeaders.put(key, httpResponse.getHeaderField(key));
			}			
		}
				
		return httpHeaders;
	}
	
	/**
	 * Returns the HTTP response headers that belongs to the JPIP protocol. 
	 * 
	 * @return the JPIP response headers.
	 */	
	private HashMap<String, String> getJPIPResponseHeaders() {
		HashMap<String, String> jpipHeaders = new HashMap<String, String>();
    
		for (String key: httpResponse.getHeaders()) {
			if ( jpipResponseHeaders.contains(key) ) {				
				jpipHeaders.put(key, httpResponse.getHeaderField(key));
			}			
		}
				
		return jpipHeaders;
	}
		
}