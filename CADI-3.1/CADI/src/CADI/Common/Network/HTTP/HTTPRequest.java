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
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.2.1 2011/05/31
 */
public class HTTPRequest {

	/**
	 * Indicates the HTTP request method.
	 */
	private String method = null;
	
	/**
	 * The requested URI.
	 */
	private URI uri = null;
	
	/**
	 * Indicates the HTTP version.
	 */
	private String version = null;

	/**
	 * Contains the HTTP request headers.
	 */
	private HashMap<String, String> headers = null;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public HTTPRequest() {
		headers = new HashMap<String, String>();
	}

	/**
	 * Sets the method that will be used to send the HTTP request.
	 * 
	 * @param method the method to be used in the HTTP request.
	 * 
	 * @throws ProtocolException
	 */
	public void setRequestMethod(String method) {
		
		if (method == null) throw new NullPointerException ("The method cannot be null");		
			
		this.method = method;		
	}
	
	/**
	 * Returns the request method.
	 * 
	 * @return the HTTP request method.
	 */
	public String getRequestMethod() {
		return method;
	}
	
	/**
	 * Sets the HTTP version.
	 * 
	 * @param version the HTTP version.
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	/**
	 * Returns the HTTP version.
	 * 
	 * @return the HTTP version.
	 */
	public String getVersion() {
		return version;
	}
		
	/**
	 * Sets the request URI.
	 * 
	 * @param uri the request URI.
	 */
	public void setRequestURI(String uri) {
		try {
			this.uri = new URI(uri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param uri
	 * @throws URISyntaxException
	 */
	public void setURI(String uri) throws URISyntaxException {
		if (uri == null) throw new NullPointerException();
		this.uri = new URI(uri);
	}
	
	/**
	 * 
	 * @param uri
	 * @throws URISyntaxException
	 */
	public void setURI(URI uri) throws URISyntaxException {
		if (uri == null) throw new NullPointerException();
		this.uri = uri;
	}
	
	/**
	 * Returns the {@link #uri} attribute.
	 * 
	 * @return the {@link #uri} attribute.
	 */
	public URI getURI() {
		return uri;
	}
	
	/**
	 * Returns the request URI.
	 * 
	 * @return the request URI.
	 */
	public String getRequestURI() {
		String request = "";
		if (uri.getScheme() != null) request += uri.getScheme()+":";
		if (uri.getSchemeSpecificPart() != null) request += uri.getSchemeSpecificPart();
		if (uri.getFragment() != null) request += uri.getFragment();
		
		return request;
	}
	
	/**
	 * Set the {@link #headers} attribute.
	 * 
	 * @param headers defined in {@link #headers}.
	 */
	public void setHeaders(HashMap<String, String> headers) {
		if (headers == null) throw new NullPointerException();
		
		this.headers = headers;
	}
	
	/**
	 * Adds a new HTTP header.
	 * 
	 * @param key name the name of the header field.
	 * @param value the value of the header field.
	 */
	public void addHeader(String key, String value) {
		if (key == null) throw new NullPointerException ("name is null");
		if (value == null) throw new NullPointerException ("value is null");
		headers.put(key, value);
	}
	
	/**
	 * Puts the value of the named header field.
	 * <p>
	 * This method will be deprecated and replaced by the {@link #addHeader(String, String)}.
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
	 * Returns the {@link #headers} attribute.
	 * 
	 * @return the {@link #headers} attribute.
	 */
	public HashMap<String, String> getHTTPHeaders() {
		return headers;
	}
	
	/**
	 * Returns an unmodifiable Enumeration of the header names.
	 * <p>
	 * The Enumeration keys are Strings that represent the response-header
	 * field names.
	 * 
	 * @return a Enumeration of header fields names. 
	 */
	public Set<String> getHeaders() {
		return headers.keySet();
	}
	
	/**
	 * Sets the attributes to its initial values.
	 */
	public void reset() {
		method = "GET";	
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
		str += "Method=" + method;
		if (uri != null) str += ", URI="+uri.toString();
		str += ", Version=" + version;
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
	 * Prints this HTTPRequest out to the specified output stream. This method
	 * is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- HTTP Request --");
		out.println("Method: " + method);
		if (uri != null) out.println("URI: "+uri.toString());
		out.println("Version: " + version);
		String key, value;		
		for (Map.Entry<String,String> entry : headers.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			out.println(key + ": " + value);
		}		
		out.flush();
	}
	
}