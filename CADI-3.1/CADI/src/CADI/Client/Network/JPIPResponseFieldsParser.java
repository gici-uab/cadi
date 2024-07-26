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

import java.io.PrintStream;

import CADI.Common.Network.HTTP.HTTPResponse;
import CADI.Common.Network.JPIP.JPIPResponseFields;
import GiciException.WarningException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2007-2012/12/14
 */
public class JPIPResponseFieldsParser {

	/**
	 * 
	 */
	HTTPResponse httpResponse = null;
	
	/**
	 * 
	 */
	JPIPResponseFields jpipResponseFields = null;
	
	
	// INTERNAL ATTRIBUTES	 
	
	/**
	 * 
	 */
	//private Hashtable<String, String> headers = null;
	
	/**
	 * 
	 */
	private boolean[] jpipHeaderFieldsFound = null;

	/**
	 * ISO/IEC 15444-9 Annex D
	 */
	private static String[] jpipResponseHeaderFields = {
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
	 * 
	 */
	private static String[] cnewTransportParamFields = {
		"transport",
		"host",
		"path",
		"port",
		"auxport"
	};

  // ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public JPIPResponseFieldsParser() {		
		jpipHeaderFieldsFound = new boolean[jpipResponseHeaderFields.length];
	}

	/**
	 * Parses the JPIP response fields
	 * 
	 * @param headers definition in {@link #headers}.
	 * 
	 * @throws WarningException this exception will be thrown whether a JPIP
	 * 			response parameter is wrong.
	 */
	public void parse(HashMap<String, String> headers) throws WarningException {
		
		//this.headers = headers;
		
		reset();
		
		jpipResponseFields = new JPIPResponseFields();
		
		// Check response headers
		boolean fieldFound = false;		
		int index = 0;
		String key, value;

		//for (Enumeration<String> e = headers.keys(); e.hasMoreElements(); ) {
    for (Map.Entry<String, String> entry : headers.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			fieldFound = false;				

			// Is it a jpip header field?
			for (index = 0; (index < jpipResponseHeaderFields.length) && ( !fieldFound ); index++) {
				if ( key.equals(jpipResponseHeaderFields[index]) ) {
					fieldFound = true;
					if ( jpipHeaderFieldsFound[index] ) {
						throw new WarningException("Wrong response server: the " + jpipResponseHeaderFields[index]+ " is repeated");
					} else {						
						jpipHeaderFieldsFound[index] = true;
						switch(index) {
							case 0:
								targetIDParser(value);
								break;
							case 1:
								channelNewParser(value);
								break;
							case 2:
								requestIDParser(value);
								break;
							case 3:
								fsizParser(value);
								break;
							case 4:
								rsizParser(value);
								break;
							case 5:
								roffParser(value);
								break;
							case 6:
								compsParser(value);
								break;
							case 7:
								streamParser(value);
								break;
							case 8:
								contextParser(value);
								break;
							case 9:
								roiParser(value);
								break;
							case 10:
								layersParser(value);
								break;
							case 11:
								srateParser(value);
								break;
							case 12:
								throw new WarningException("The request parameter (" + key + ") is not supported");
								//metareqParser(value);
								//break;
							case 13:
								lenParser(value);
								break;
							case 14:
								qualityParser(value);
								break;
							case 15:
								typeParser(value);
								break;
							case 16:
								throw new WarningException("The request parameter (" + key + ") is not supported");
								//msetParser(value);
								//break;
							case 17:
								throw new WarningException("The request parameter (" + key + ") is not supported");
								//capParser(value);
								//break;
							case 18:
								throw new WarningException("The request parameter (" + key + ") is not supported");
								//prefParser(value);
								//break;									
						}
					}
				}
			}
			if(!fieldFound) {
				throw new WarningException("Wrong response server: unkwon the field " +  key + ": " + value);
			}
		}

	}
	
	/**
	 * Sets the class atributes to initial values
	 *
	 */
	public void reset() {
		for(int i = 0; i < jpipHeaderFieldsFound.length; i++) {
			jpipHeaderFieldsFound[i] = false;			
		}
	}

	/**
	 * 
	 * @return returns the {@link #jpipResponseFields} attribute.
	 */
	public JPIPResponseFields getJPIPResponseFields() {
		return jpipResponseFields;
	}

	/**
	 * Returns an object which contains the http response field values. 
	 * 
	 * @return returns the {@link #httpResponse} attribute
	 */
	public HTTPResponse getHTTPResponse() {
		return httpResponse;
	}
	
	/**
	 * For debugging purposes. 
	 */
  @Override
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";

		str += "<<< Not implemented yet >>> ";

		str += "]";
		return str;
	}
	
	/**
	 * Prints this JPIP Response Fields out to the specified output stream.
	 * This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- JPIP Response Fields --");		
		
		out.println("<<< Not implemented yet >>> ");
						
		out.flush();
	}
	
  // ============================ private methods ==============================
  //
	// TARGET FIELD PARSERS
  //
	
	/**
	 * 
	 * @param tid definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#tid}
	 * 
	 * @throws Exception when the request element is wrong
	 */
	private void targetIDParser(String tid) throws WarningException {		
		if (tid.length()>256){
			throw new WarningException();
		}
		jpipResponseFields.tid = tid; //.substring(1,tid.length());		
	}

	//
	// CHANNEL FIELD PARSERS
  //
  
	/**
	 * 
	 * @param cnew definition in {@link CADI.Common.Network.JPIP.ChannelField#cnew}
	 * 
	 * @throws WarningException when the request element is wrong
	 */
	private void channelNewParser(String cnew) throws WarningException {

		String[] temp = null;
		String[] cnewParams = cnew.split(",");

		temp = cnewParams[0].split("=");
		if (temp[0].compareTo("cid") == 0) {
			jpipResponseFields.cid = temp[1];
		} else {
			throw new WarningException();
		}

		// If transport-parameters are defined
		boolean[] transportParamFound = new boolean[cnewTransportParamFields.length];	
		for (int i=0; i<transportParamFound.length; i++) {
			transportParamFound[i] = false;
		}
		if (cnewParams.length>1) {
			for (int i=1; i<cnewParams.length; i++) {
				temp =cnewParams[i].split("=");
				// Loop on transport params possibilities
				for (int index=0; index<cnewTransportParamFields.length; index++) {
					if (temp[0].compareTo(cnewTransportParamFields[index]) == 0) {
						if (transportParamFound[index]) {
							throw new WarningException();
						}
						transportParamFound[index] = true;
						switch(index) {
							case 0:	// transport
								if (temp[1].compareTo("http") == 0) {
									jpipResponseFields.transport = JPIPResponseFields.TRANSPORT_HTTP;
								} else {
									if (temp[1].compareTo("http-tcp") ==  0 ) {
										jpipResponseFields.transport = JPIPResponseFields.TRANSPORT_HTTP_TCP;
									} else {
										throw new WarningException();
									}
								}
								break;
							case 1:	// host
								jpipResponseFields.host = temp[1];
								break;
							case 2:	// path
								jpipResponseFields.path = temp[1];
								break;
							case 3:	// port
								jpipResponseFields.port = Integer.parseInt(temp[1]);
								break;
							case 4:	//auxport
								jpipResponseFields.auxport = Integer.parseInt(temp[1]);
								break;
							default:
								throw new WarningException();
						}
					}
				}
			}			
		}	
	}


	/** 
	 * @param qid defined in {@link CADI.Common.Network.JPIP.JPIPResponseFields#qid}
	 * 
	 * @throws WarningException when the request element is wrong
	 */
	private void requestIDParser(String qid) throws WarningException {

		// read comps values 
		try {
			jpipResponseFields.qid = Integer.parseInt(qid);
		} catch (Exception e) {
			throw new WarningException();
		}
		if (jpipResponseFields.qid < 0 ){
			throw new WarningException();
		}		
	}

	//
	// VIEW WINDOW FIELD PARSERS
  //
  
	/** 
	 * @param fsiz defined in {@link CADI.Common.Network.JPIP.JPIPResponseFields#fsiz}
	 * 
	 * @throws WarningException when the request element is wrong
	 */
	private void fsizParser(String fsiz) throws WarningException{				
		String[] fsizString = fsiz.split(",");		

		if ( (fsizString.length>3)||(fsizString.length==0)){
			throw new WarningException();
		}

		// read fx & fy values 
		try {
			jpipResponseFields.fsiz[0] = Integer.parseInt(fsizString[0]);
			jpipResponseFields.fsiz[1] = Integer.parseInt(fsizString[1]);
		} catch (Exception e) {
			throw new WarningException();
		}
		if ( (jpipResponseFields.fsiz[0] < 0) || (jpipResponseFields.fsiz[1] < 0) ){
			throw new WarningException();
		}
	}

	/**
	 * @param roff defined in {@link CADI.Common.Network.JPIP.JPIPResponseFields#roff} 
	 * 
	 * @throws WarningException when the request element is wrong
	 */
	private void roffParser(String roff) throws WarningException{		
		String[] roffString = roff.split(",");

		if ( (roffString.length > 2) || (roffString.length == 0)){
			throw new WarningException();
		}

		// read ox and oy values 
		try {
			jpipResponseFields.roff[0] = Integer.parseInt(roffString[0]);
			jpipResponseFields.roff[1] = Integer.parseInt(roffString[1]);
		} catch (Exception e) {
			throw new WarningException();
		}
		if ( (jpipResponseFields.roff[0] < 0) || (jpipResponseFields.roff[1] < 0) ){
			throw new WarningException();
		}		

	}

	/**
	 * @param rsiz defined in {@link CADI.Common.Network.JPIP.JPIPResponseFields#rsiz} 
	 * 
	 * @throws WarningException when the request element is wrong
	 */
	private void rsizParser(String rsiz) throws WarningException{

		String[] rsizString = rsiz.split(",");

		if ( (rsizString.length > 2) || (rsizString.length == 0) ){
			throw new WarningException();
		}

		// read sx & sy values 
		try {		
			jpipResponseFields.rsiz[0] = Integer.parseInt(rsizString[0]);
			jpipResponseFields.rsiz[1] = Integer.parseInt(rsizString[1]);
		} catch (Exception e) {
			throw new WarningException();
		}

		if ( (jpipResponseFields.rsiz[0] < 0) || (jpipResponseFields.rsiz[1] < 0) ){
			throw new WarningException();			
		}	
	}

	/**
	 * @param comps definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#comps}
	 * 
	 * @throws WarningException when the request element is wrong
	 */
	private void compsParser(String comps) throws WarningException{		
		String [] temp1=null, compsString=null;

		try {
			compsString = comps.split(",");		
			jpipResponseFields.comps = new int[compsString.length][2];
			for (int i = 0; i < compsString.length; i++) {
				jpipResponseFields.comps[i][0] = -1;
				jpipResponseFields.comps[i][1] = -1;			

				temp1 = compsString[i].split("-");
				if (temp1.length == 1) {	// case UINT -
					jpipResponseFields.comps[i][0] = Integer.parseInt(temp1[0]);
				} else {
					if (temp1.length == 2) {		// UINT-UINT
						jpipResponseFields.comps[i][0] = Integer.parseInt(temp1[0]);
						jpipResponseFields.comps[i][1] = Integer.parseInt(temp1[1]);
					} else {
						throw new WarningException();
					}				
				}
			}			
		}catch (Exception e) {			
			throw new WarningException();
		}		
	}

	/**
	 * @param stream defined in {@link CADI.Common.Network.JPIP.JPIPResponseFields#stream}
	 *  
	 * @throws WarningException when the request element is wrong
	 */
	private void streamParser(String stream) throws WarningException {

		// stream = "stream" "=" 1#sampled-range
		// sampled-range = UINT-RANGE [":" sampling-factor]
		// sampling-factor = UINT

		int from, to, samplingFactor;
		String[] streamString=null;
		String[] temp1=null, temp2=null;
		int[][] streamValues=null;

		try {
			streamString = stream.split(",");
			streamValues = new int[streamString.length][3];
			for (int i=0; i<streamString.length; i++) {			
				from=-1; to=-1; samplingFactor=-1;

				//Possibilities:
				// 1.- stream=from
				//	2.- stream=from-to
				//	3.- stream=from-to:samplingFactor
				// 4.- stream=from:samplingFactor

				temp1 = streamString[i].split("-");			
				if(temp1.length==2) {	//case 2 or 3					
					from = Integer.parseInt(temp1[0]);				
					temp2 = temp1[1].split(":");					
					if(temp2.length==1) {	// case 2
						to = Integer.parseInt(temp2[0]);
					} else if (temp2.length == 2) {	// case 3						
						to = Integer.parseInt(temp2[0]);
						samplingFactor = Integer.parseInt(temp2[1]);					
					} else {
						throw new WarningException();
					}				
				} else if (temp1.length == 1) {	// case 1 or 4					
					temp2 = temp1[0].split(":");
					if (temp2.length==1) {	// case 1
						from = Integer.parseInt(temp2[0]);
					} else 	if(temp2.length==2) {
						from = Integer.parseInt(temp2[0]);
						samplingFactor = Integer.parseInt(temp2[1]);
					} else {
						throw new WarningException();
					}
				} else {
					throw new WarningException();
				}

				streamValues[i][0]=from;
				streamValues[i][1]=to;
				streamValues[i][2]=samplingFactor;

			} // 	for (int i=0; i<streamString.length; i++)

		} catch (Exception e) {
			throw new WarningException();
		}
	}

	/**
	 * @param context defined in {@link CADI.Common.Network.JPIP.JPIPResponseFields#context}
	 * 
	 * @throws WarningException when the request element is wrong
	 */
	private void contextParser(String context) throws WarningException {

	}

	protected void roiParser(String string){

	}

	/**
	 * @param layers defined in {@link CADI.Common.Network.JPIP.JPIPResponseFields#layers}
	 * 
	 * @throws WarningException when the request element is wrong
	 */
	private void layersParser(String layers) throws WarningException{

		// read layers value 
		try {
			jpipResponseFields.layers = Integer.parseInt(layers);
		} catch (Exception e) {
			throw new WarningException();
		}
		if (jpipResponseFields.layers < 0){
			throw new WarningException();
		}
	}

	/**
	 * @param srate defined in {@link CADI.Common.Network.JPIP.JPIPResponseFields#srate}
	 * 
	 * @throws WarningException
	 */
	private void srateParser(String srate) throws WarningException{

		// read comps values 
		try {
			jpipResponseFields.srate = Float.parseFloat(srate);
		} catch (Exception e) {
			throw new WarningException();
		}
		if (jpipResponseFields.srate < 0 ){
			throw new WarningException();
		}	
	}

  //
	// METADATA FIELD PARSERS
  //
	
	/**
	 * 
	 * @param metareq defined in {@link CADI.Common.Network.JPIP.JPIPResponseFields#metareq}
	 */
	private void metareqParser(String metareq){

	}

	//
	// DATA LIMIT FIELD PARSERS
  //

	/**
	 * @param len defined in {@link CADI.Common.Network.JPIP.JPIPResponseFields#len}
	 * 
	 * @throws WarningException when the request element is wrong
	 */

	private void lenParser(String len) throws WarningException{

		try {
			jpipResponseFields.len = Integer.parseInt(len);
		} catch (Exception e){
			throw new WarningException();
		}

		if (jpipResponseFields.len < 0){
			throw new WarningException();			
		}	
	}

	/**
	 * @param quality defined in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}
	 * 
	 * @throws WarningException when the request element is wrong
	 */
	private void qualityParser(String quality) throws WarningException{

		try {
			jpipResponseFields.quality = Integer.parseInt(quality);
		} catch (Exception e){
			throw new WarningException();
		}

		if ( (jpipResponseFields.quality < -1) || (jpipResponseFields.quality > 100) ){
			throw new WarningException();			
		}
	}	

  //
	// SERVER CONTROL FIELD PARSERS
  //
	
	/**
	 * 
	 * @param type defined in {@link CADI.Common.Network.JPIP.JPIPResponseFields#type}
	 * 
	 * @throws WarningException
	 */
	private void typeParser(String type) throws WarningException{				

		if ( type.equals("jpp-stream") ) {
			jpipResponseFields.type = "jpp-stream";
		} else {
			if ( type.equals("jpt-stream") ) {
				jpipResponseFields.type = "jpt-stream";				
			} else {
				if ( type.equals("raw") ) {
					jpipResponseFields.type = "raw";					
				} else {
					throw new WarningException();
				}
			}
		}			
	}

	//
	// CACHE MANAGEMENT FIELDS PARSERS
  //
	
	/**
	 * 
	 * @param mset defined in {@link CADI.Common.Network.JPIP.JPIPResponseFields#mset}
	 */
	private void msetParser(String mset){

	}

	//
	// UPLOAD FIELD PARSERS
  //	

  //
	// CLIENT CAPABILITIES PREFERENCES FIELD PARSERS	
  //

	/**
	 * @param cap defined in {@link CADI.Common.Network.JPIP.JPIPResponseFields#cap}
	 */
	private void capParser(String cap){

	}
	
	/**
	 * @param pref defined in {@link CADI.Common.Network.JPIP.JPIPResponseFields#pref}
	 */
	protected void prefParser(String pref){

	}

}