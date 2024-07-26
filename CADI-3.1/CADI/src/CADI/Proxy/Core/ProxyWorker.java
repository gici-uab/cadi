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
package CADI.Proxy.Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import CADI.Common.Log.CADILog;
import CADI.Common.Network.HTTP.HTTPRequest;
import CADI.Common.Network.HTTP.HTTPResponse;
import CADI.Common.Network.HTTP.StatusCodes;
import CADI.Common.Network.JPIP.JPIPResponseFields;
import CADI.Server.Network.HTTPResponseSender;
import CADI.Common.Network.TrafficShaping;
import CADI.Server.Request.HTTPRequestReader;
import CADI.Server.Request.RequestQueue;
import GiciException.ErrorException;
import GiciException.WarningException;

/**
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2009/08/22
 */
public class ProxyWorker extends Thread {

	/**
	 * Definition in {@link CADI.Proxy.Proxy#requestQueue}.
	 */
	protected RequestQueue requestQueue = null;

	/**
	 * It is an object that will be used to log the server process
	 */
	protected CADILog log = null;
		
	/**
	 * Indicates whether the thread has to finish when it is working with a 
	 * request with the keep-alive mode set.
	 * 
	 * @see #finish()
	 */
	protected boolean finish = false;
	
	/**
	 * Indicates if the thread is carrying out a task. If the thread is waiting
	 * for a new task, the <code>busy</code> attribute is <code>false</code>.
	 */	
	protected boolean busy = false;
	
	/**
	 * Contains the client socket of which request is being processed.
	 */
	protected Socket socket = null;
	
	/**
	 * Definition in {@link CADI.Common.Network.HTTP.HTTPRequest}.
	 */
	protected HTTPRequest httpRequest = null;
	
	/**
	 * Definition in {@link CADI.Server.Core.Scheduler#keepAliveTimeout}.
	 */
	protected int keepAliveTimeout = 5;
	
	/**
	 * Definition in {@link CADI.Server.Core.Scheduler#maxTxRate}.
	 */
	protected long maxTxRate = 0L;

  protected int trafficShaping = TrafficShaping.NONE;

	
	/**
	 * Definition in {@link CADI.Server.Network.JPIPMessageEncoder#independentForm}.
	 */
	protected boolean independentMessageHeaders = true;
	
	
	// INTERNAL ATTRIBUTES
	
	/**
	 * Is an output stream that will be used to send the server response to the client. 
	 */
	protected OutputStream os = null;
	
	/**
	 * 
	 */
	protected HTTPResponse httpResponse = null;
	
	/**
	 * 
	 */
	protected JPIPResponseFields jpipResponseFields = null;
	
	/**
	 * 
	 */
	protected HTTPResponseSender httpResponseSender = null;

  // DEBUG
  /**
   * Attribute only for debugging purposes.
   */
  protected long cumMessageHeadersLength = 0;

  /**
   * Attribute only for debugging purposes.
   */
  protected long cumMessageBodiesLength = 0;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param threadName
	 * @param requestQueue definition in {@link #requestQueue}.
	 * @param log definition in {@link #log}.
	 */
	public ProxyWorker(String threadName, RequestQueue requestQueue, CADILog log) {
		// Check input parameters
		if (threadName == null) throw new NullPointerException();
		if (requestQueue == null) throw new NullPointerException();
		if (log == null) throw new NullPointerException();
				
		// Copy parameters
		this.requestQueue = requestQueue;
		this.log = log;
		
		setName(threadName);
		setPriority(Thread.MAX_PRIORITY);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
  @Override
	public void run() {
		
	}
	
	/**
	 * This method is used to indicate that the thread has to finish.
	 */
	public synchronized void finish() {
		this.finish = true;
	}
	
	/**
	 * This method is used to indicate if the thread is or no busy processing a
	 * task.
	 * 
	 * @return <code>true</code> if the thread is processing a task. Otherwise,
	 * 			it returns <code>false</code>.
	 */
	public synchronized boolean isBusy() {
		return busy;
	}
	
	@Override
	public synchronized Thread.State getState() {
		return getState();
	}
	
	/**
	 * Sets the {@link #keepAliveTimeout} attribute.
	 * 
	 * @param keepAliveTimeout definition in {@link #keepAliveTimeout}.
	 */
	public void setKeepAliveTimeout(int keepAliveTimeout) {
		this.keepAliveTimeout = keepAliveTimeout;
	}
	
	/**
	 * Sets the {@link #maxTxRate} attribute.
	 * 
	 * @param maxTxRate definition in {@link #maxTxRate} attribute.
	 */
	public void setMaxTxRate(long maxTxRate) {
		this.maxTxRate = maxTxRate;
	}

   /**
   * Sets the {@link #trafficShaping} attribute.
   *
   * @param maxTxRate definition in {@link #trafficShaping} attribute.
   */
  public void setTrafficShaping(int trafficShaping) {
    this.trafficShaping = trafficShaping;
  }
	
	/**
	 * Sets the {@link #independentMesssageHeaders} attribute.
	 * 
	 * @param independentMesssageHeaders definition in {@link #independentMesssageHeaders} attribute.
	 */
	public void setIndependentMessageHeaders(boolean independentMessageHeaders) {
		this.independentMessageHeaders = independentMessageHeaders;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";
		
		str = getClass().getName() + " [";
	
		str += "]";
		return str;
	}

	/**
	 * Prints this ProxyWorker out to the specified output stream. This method
	 * is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Proxy worker --");
	
		out.flush();
	}
	
	// ============================ private methods ==============================
	/**
	 * 
	 * @throws WarningException
	 * @throws ErrorException
	 */
	protected void listenNewRequest() throws WarningException, ErrorException {
				
		HTTPRequestReader httpRequestReader = new HTTPRequestReader();
				
		if (socket.isInputShutdown()) {
			throw new ErrorException();
		} 
		
		// Set keep alive timeout
		if (keepAliveTimeout >= 0) {
			try {
				socket.setSoTimeout(keepAliveTimeout);
			} catch (SocketException e) {
				log.logWarning(getName() + ": keep alive time out cannot be set");
				throw new ErrorException();
			}
		}
		
		
		// Wait for client connections or until timeout is reached
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			httpRequestReader.readHTTPRequest(bufferedReader);
		} catch (ErrorException ee) {
			log.logInfo(getName()+": malformed URL");
			throw new ErrorException(ee.getMessage(), ee.getErrorCode());
		} catch (SocketTimeoutException ste) {
			log.logInfo(getName() + ": keep alive timeout has been reached");			
			throw new ErrorException();
		} catch (IOException e) {
			log.logInfo(getName() + ": remote host (" + socket.getRemoteSocketAddress() + ") has been closed unexpectedly");
			throw new ErrorException();
		}
		
		httpRequest = httpRequestReader.getHTTPRequest();
		
		log.logInfo(getName() + "(Received request):\n" + (socket + "\n" + httpRequest.toString()));
	}
	
	/**
	 * JPIP HEADERS (See ISO/IEC 15444-9 Annex D)
	 */
	private void encodeJPIPResponseFields() {	
		
		if (jpipResponseFields.tid != null) {
			httpResponse.setHeaderField("JPIP-tid", jpipResponseFields.tid);			
		}
		
		if (jpipResponseFields.cid != null) {
			String jpipCnew = "cid=" + jpipResponseFields.cid;			
			if (jpipResponseFields.transport != -1) {					
				jpipCnew += ",transport=";
				if (jpipResponseFields.transport == JPIPResponseFields.TRANSPORT_HTTP) {
					jpipCnew += "http";
				} else if (jpipResponseFields.transport == JPIPResponseFields.TRANSPORT_HTTP_TCP) {
					jpipCnew += "http-tcp";
				}
			}
			if (jpipResponseFields.host != null) {
				jpipCnew += ",host=" + jpipResponseFields.host;
			}
			if (jpipResponseFields.path != null) {
				jpipCnew += ",path=" + jpipResponseFields.path;
			}
			if (jpipResponseFields.port != -1) {
				jpipCnew += ",port=" + jpipResponseFields.port;
			}
			if (jpipResponseFields.auxport != -1) {
				jpipCnew += ",auxport=" + jpipResponseFields.auxport;
			}
			httpResponse.setHeaderField("JPIP-cnew", jpipCnew);			
		}
	
		if (jpipResponseFields.qid != -1) {
			httpResponse.setHeaderField("JPIP-qid", Integer.toString(jpipResponseFields.qid));
		}
		
		int[] fsiz = jpipResponseFields.fsiz;
		if (fsiz != null) {
			if ( (fsiz[0] != -1) && (fsiz[1] != -1) ) {
				httpResponse.setHeaderField("JPIP-fsiz", fsiz[0] + "," + fsiz[1]);
			}
		}			
		fsiz = null;
		
		int[] rsiz = jpipResponseFields.rsiz;
		if (rsiz != null) {
			if ( (rsiz[0] != -1) && (rsiz[1] != -1) ) {
				httpResponse.setHeaderField("JPIP-rsiz", rsiz[0] + "," + rsiz[1]);
			}
		}			
		rsiz = null;

		int[] roff = jpipResponseFields.roff;
		if (roff != null) {
			if ( (roff[0] != -1) && (roff[1] != -1) ) {
				httpResponse.setHeaderField("JPIP-roff", roff[0] + "," + roff[1]);
			}				                                               
		}			
		roff = null;

		int[][] comps = jpipResponseFields.comps;
		if (comps != null) {
			String jpipComps = "";
			for (int i=0; i<comps.length; i++) {
				jpipComps += comps[i][0];
				if (comps[i][1] >= 0) {
					jpipComps += "-" + comps[i][1];
				}
				if (i < comps.length -1) {
					jpipComps += ",";
				}
			}
			httpResponse.setHeaderField("JPIP-comps", jpipComps);
		}			
		comps = null;
	
		if (jpipResponseFields.layers != -1) {
			httpResponse.setHeaderField("JPIP-layers", Integer.toString(jpipResponseFields.layers));
		}
		
		if (jpipResponseFields.srate > 0.0) {
			httpResponse.setHeaderField("JPIP-srate", Double.toString(jpipResponseFields.srate));
		}
		
		if (jpipResponseFields.quality != -1) {
			httpResponse.setHeaderField("JPIP-quality", Integer.toString(jpipResponseFields.quality));
		}
		
		if (jpipResponseFields.type != null) {
			httpResponse.setHeaderField("JPIP-type", jpipResponseFields.type);
		}
	}
	
	/**
	 * 
	 * @param httpResponse
	 * @throws WarningException
	 */
	protected void sendHTTPResponseHeader(HTTPResponse httpResponse) throws WarningException {
		
		assert( (httpResponse.getResponseCode() == StatusCodes.OK)
				||	(httpResponse.getResponseCode() == StatusCodes.ACCEPTED)
				|| 	(httpResponse.getResponseCode() == StatusCodes.BAD_REQUEST)
				|| 	(httpResponse.getResponseCode() == StatusCodes.NOT_FOUND)
				|| 	(httpResponse.getResponseCode() == StatusCodes.UNSUPPORTED_MEDIA_TYPE)
				|| 	(httpResponse.getResponseCode() == StatusCodes.NOT_IMPLEMENTED)
				|| 	(httpResponse.getResponseCode() == StatusCodes.SERVICE_UNAVAILABLE)
		);
		
		encodeJPIPResponseFields();
		

		log.logInfo(getName() + ": HTTP Response header:\n" + httpResponse.toString());
		
		try {
			httpResponseSender.sendHeaders(httpResponse);
			httpResponseSender.endOfHeaders();
		} catch (IOException e) {
			log.logInfo(getName() + ": remote host (" + socket.getRemoteSocketAddress() + ") has been closed unexpectedly or it is unreachable");
			throw new WarningException();
		}
	}
	
	/**
	 * This method will be used to send an HTTP error response to the client.
	 * 
	 * @param statusCode definition in {@link CADI.Common.Network.HTTP.StatusCodes}.
	 * @param reasonPhrase a reason phrase related with the status code.
	 */
	protected void sendHTTPResponseError(int statusCode, String reasonPhrase) {
		httpResponse.reset();
		httpResponse.setResponseCode(statusCode);
		httpResponse.setResponseMessage(reasonPhrase);
		try {
			sendHTTPResponseHeader(httpResponse);
		} catch (WarningException e) {
		}
		
		httpResponse.list(System.out);
	}
}