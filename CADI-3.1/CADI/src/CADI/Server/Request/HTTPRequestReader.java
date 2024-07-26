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

import CADI.Client.Network.HTTPClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import CADI.Common.Network.HTTP.HTTPRequest;
import CADI.Common.Network.HTTP.StatusCodes;
import GiciException.ErrorException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class reads HTTP client requests. This class only a simple parse of
 * the requests, check that the request line had the method, request URI,
 * and the HTTP protocol version. Regarding the HTTP headers, it only check
 * that the header keys were an allowed key. 
 * <p>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; readHTTPRequest<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.2 2011/05/31
 */
public class HTTPRequestReader implements StatusCodes {

	/**
	 * Stores the http request sent by a client.
	 */
	private HTTPRequest httpRequest = null;
	
	
	// INTERNAL ATTRIBUTES
	
	/**
	 * Allowed HTTP methods
	 */
  private static final String[] methodsList = HTTPClient.methodsList;
	/*private static final String[] methodsList = {
		"GET", "POST"//, "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
		//"GET", "POST", "HEAD", "PUT", "DELETE", "TRACE", "CONNECT"
	};*/

	/**
	 * @see #methodsList
	 */
	private final static Set<String> methods =
		new HashSet<String>(Arrays.asList(methodsList));

	/**
	 * Allowed HTTP request headers
	 */
  private final static String[] httpRequestHeadersList = HTTPClient.httpRequestHeadersList;
	/*private final static String[] httpRequestHeadersList = {
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
		 };*/

	/**
	 * @see #httpRequestHeadersList
	 */
	private final static Set<String> httpRequestHeaders =
		new HashSet<String>(Arrays.asList(httpRequestHeadersList));
	
	// ============================ private methods ==============================
	/**
	 * Constructor.
	 */
	public HTTPRequestReader() {
		httpRequest = null;
	}

	/**
	 * Reads a HTTP request from the socket.
	 * 
	 * @param bufferedReader buffered reader where the http request will be read.
	 * 
	 * @throws ErrorException
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	public HTTPRequest readHTTPRequest(BufferedReader bufferedReader)
			throws ErrorException, SocketTimeoutException, IOException {
		
		String requestLine = null;
		HashMap<String, String> headers = new HashMap<String, String>();
		
		// Read request line
		requestLine = bufferedReader.readLine();
		if (requestLine == null) {
			throw new IOException();
		}
		
		// Read headers		
		String str;	
		do {
			str = bufferedReader.readLine();
			if (str == null) {
				throw new IOException();
			}
			
			if (str.length() > 0) {
				String[] strParts = str.split(":");
				if (strParts.length != 2) {
					headers.put(strParts[0], "");
				} else {
					headers.put(strParts[0], strParts[1].substring(1));
				}
			}			
		} while(str.length() != 0);
		
		
		// CHECK FORMAT OF REQUEST LINE
		
		// GET REQUEST LINE	(RFC 2616 section 5.1)
		String[] requestLineParts = null;
		
		// Split the url string: Method SP Request-URI SP HTTP-Version CRLF
		requestLineParts = requestLine.split(" ");
		
		if (requestLineParts.length != 3) {
			throw new ErrorException("Request must be: Method SP Request-URI SP" +
					" HTTP-Version", BAD_REQUEST);			
		}			
		
		// Method (only GET are available)
		if ( !methods.contains(requestLineParts[0]) ) {
			throw new ErrorException("Only GET or POST methods are available",
			                         BAD_REQUEST);				
		}		
		
		// Protocol HTTP/1.1
		final String HTTP_VERSION_11 = "HTTP/1.1";
		if ( requestLineParts[2].compareTo(HTTP_VERSION_11) != 0 ) {					
			throw new ErrorException("Protocol is not present", BAD_REQUEST);
		}
		
		
		// CHECK ALLOWED HEADERS
		String key, value;
    for (Map.Entry<String, String> entry : headers.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();

			if ( (key.length() <= 0) || (value.length() <= 0) ) {
				throw new ErrorException("The header \""+key+":"+" value\"is wrong",
				                         BAD_REQUEST);
			}

			if ( !httpRequestHeaders.contains(key) )  {
				throw new ErrorException("The header \""+key+":"+" value\" is not" +
						" supported yet", BAD_REQUEST);
			}
		}
		
		// SAVE REQUEST
		httpRequest = new HTTPRequest();
		httpRequest.setRequestMethod(requestLineParts[0]);
		//httpRequest.setRequestURI(requestLineParts[1]);
		try {
			httpRequest.setURI(requestLineParts[1]);
		} catch (URISyntaxException e) {
			throw new ErrorException("Malformed URL", BAD_REQUEST);
		}
		httpRequest.setVersion(requestLineParts[2]);
		httpRequest.setHeaders(headers);
		

		return httpRequest;
	}

	/**
	 * Returns the {@link #httpRequest} attribute.
	 * 
	 * @return the {@link #httpRequest} attribute.
	 */
	public HTTPRequest getHTTPRequest() {
		return httpRequest;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";

		String requestLine = httpRequest.getRequestURI();
		HashMap<String,String> headers = httpRequest.getHTTPHeaders();
		if (requestLine != null) {
			str += requestLine;
			String key, value;
      for (Map.Entry<String, String> entry : headers.entrySet()) {
				key = entry.getKey();
				value = entry.getValue();
				str += key +": " + value;
			}
		} else {
			str += "<<< None request was found >>>\n";
		}		

		str += "]";
		
		return str;
	}
	
	/**
	 * Prints this HTTP Request Reader fields out to the
	 * specified output stream. This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- HTTP request reader --");		
		
		String requestLine = httpRequest.getRequestURI();
		HashMap<String,String> headers = httpRequest.getHTTPHeaders();
		if (requestLine != null) {
			out.println(requestLine);
			String key, value;
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				key = entry.getKey();
				value = entry.getValue();
				out.println(key +": " + value);
			}
		} else {
			out.println("<<< None request was found >>>");
		}
						
		out.flush();
	}
	
	// ============================ private methods ==============================

}