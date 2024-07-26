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
package CADI;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import CADI.Common.Info.CADIInfo;
import CADI.Proxy.Proxy;
import CADI.Proxy.ProxyDefaultValues;
import CADI.Proxy.ProxyParser;
import CADI.Proxy.Core.ProxyPrefetching;
import GiciException.ErrorException;
import GiciException.ParameterException;

/**
 * This class is the main class of the CADIProxy application. The CADIProxy
 * applications is a JPIP proxy addressed to improve JPIP responses. The proxy
 * will catch client request and then:<br>
 * 1) if data are available in the proxy database, a response is inmediatly
 * 	sent to the client<br>
 * 2) if not, the client's request is processed before being sent to server
 * 	requesting by the data. Therefore, client request may be, for instance,
 * 	splitted up into several request to the server. <br>
 * 
 * <p>
 * Furthermore, in order to improve the efficiency and latency of responses,
 * the proxy takes advantage of the dead times between request and, using
 * information from previous request, it fetchs for new portions of the image.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2009/07/05
 */
public class CADIProxy {

	public static void main(String [] args){
				
		//Properties properties = System.getProperties();
		//properties.list(System.out);
		 
		
		//ARGUMENTS VARIABLES
		int[] ports = null;
		int numThreads = 1;
		int proxyType = -1;
		int prefetchingDataHistory = ProxyPrefetching.MODE_ONLY_IMAGE_HISTORY;
		int prefetchingWOIType = ProxyPrefetching.WOI_TYPE_WEIGHTED_WOI;
    String predictiveModel = null;
		String logFile = null;
		boolean XMLLogFormat;
		boolean logEnabled;
		int logLevel = -1;
		String cachePath = null;
    int maxTxRate = 0;
    int trafficShapping = -1;
    float[] movProbs = null;

		
		// PARSE COMMAND LINE ARGUMENTS
		ProxyParser parser = null;
		try{
			parser = new ProxyParser(args);
		}catch(ErrorException e){
			printShortCopyright();
			System.out.println("\nRUN ERROR:" + e.getMessage());
			System.exit(3);
		}catch(ParameterException e){
			printShortCopyright();
			System.out.println("\nARGUMENTS ERROR: " +  e.getMessage());
			System.exit(1);
		}

		// PRINT SHORT COPYRIGTH
		printShortCopyright();
		System.out.println();

		
		// COMMAND LINE PARAMETERS ARE READ OR SET TO THEIR DEFAULT VALUES 
		
		// Port
		ports = parser.getPorts();
		if (ports == null) {
			ports = new int[1];
			ports[0] = ProxyDefaultValues.PORT;
		}
		
		// Thread
		numThreads = parser.getNumThreads();
		if (numThreads <= 0) {
			numThreads = Runtime.getRuntime().availableProcessors();
			/* For tasks that may wait for I/O to complete -- for example, a task that
			 * reads an HTTP request from a socket -- you will want to increase the
			 * pool size beyond the number of available processors, because not all
			 * threads will be working at all times. Using profiling, you can estimate
			 * the ratio of waiting time (WT) to service time (ST) for a typical
			 * request. If we call this ratio WT/ST, for an N-processor system, you'll
			 * want to have approximately N*(1+WT/ST) threads to keep the processors
			 * fully utilized.
			 * http://www.samspublishing.com/articles/article.asp?p=30483&seqNum=6&rl=1
			 */
		}
		
		// Type of proxy
		proxyType = parser.getProxyType();
		
		prefetchingDataHistory = parser.getPrefetchingDataHistory();
		prefetchingWOIType = parser.getPrefetchingWOIType();
    movProbs = parser.getMovementProbabilities();
    predictiveModel = parser.getPredictiveModel();
				
		// Log
		logFile = parser.getLogFile();
		XMLLogFormat = parser.isXMLLogFormat();
		logEnabled = parser.isLogEnabled();
		logLevel = parser.getLogLevel();
		
		// Cache
		cachePath = parser.getCacheDirectory();
		
				
		if (cachePath != null) {
			if ( !(new File(cachePath)).exists() ) {
				System.out.println("ARGUMENTS ERROR: cache directory (\""+cachePath
				                   +"\") does not exist" );
				System.exit(1);
			}
		}

    maxTxRate = parser.getMaxRate();
		
		// RUN PROXY
		Proxy proxy = new Proxy(proxyType, ports, numThreads, logEnabled, logLevel,
		                        logFile, XMLLogFormat);
		proxy.setProxyType(proxyType);
		proxy.setPrefetchingDataHistory(prefetchingDataHistory);
		proxy.setPrefetchingWOIType(prefetchingWOIType);
    proxy.setMaxTxRate(maxTxRate);
    proxy.setMovementProbabilities(movProbs);
    proxy.setPredictiveModel(predictiveModel);
		proxy.run();
	}
	
	/**
	 * Prints a short copyright.
	 */
	private static void printShortCopyright() {
		try {
			CADIInfo cadiInfo = new CADIInfo();
			cadiInfo.printVersion();
			cadiInfo.printCopyrightYears();
			cadiInfo.printShortCopyright();
		} catch (IOException e) {
		}
	}
	
}