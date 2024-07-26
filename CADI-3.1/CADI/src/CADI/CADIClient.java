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

import java.io.IOException;
import java.util.Properties;

import CADI.Client.Client;
import CADI.Client.ClientParser;
import CADI.Client.ImageData;
import CADI.Common.Info.CADIInfo;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Server.ServerDefaultValues;
import GiciException.ErrorException;
import GiciException.ParameterException;
import GiciException.WarningException;
import GiciFile.SaveFile;

/**
 * Main class of CADIClient application. The CADIClient is a command line
 * front-end of the JPIP client.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2011/06/29
 */
public class CADIClient {
	
	public static void main(String [] args){
				
		Properties properties = System.getProperties();
		//properties.list(System.out);
		
		//ARGUMENTS VARIABLES
		String server = "localhost";
		int port = 80;
		String proxyServer = null;
		int proxyPort = 80;
		String target = null;
		String uri = null;
		int[] components = null;
		int resolutionLevel = -1;
		int[] fsiz = null;
		int[] roff = null;
		int[] rsiz = null;
		int maxTargetLength = -1;
		int layers = -1;
		int quality = -1;
		int round = -1;
		String[] outputImageFiles = null;
		int[] imageGeometry = null;
		
		
		// PARSE COMMAND LINE ARGUMENTS
		ClientParser parser = null;
		try{
			parser = new ClientParser(args);
		}catch(ErrorException e){
			printShortCopyright();
			System.out.println();
			System.out.println("RUN ERROR:" + e.getMessage());
			System.exit(3);
		}catch(ParameterException e){
			printShortCopyright();
			System.out.println();
			System.out.println("ARGUMENTS ERROR: " +  e.getMessage());
			System.exit(1);
		}

		// PRINT SHORT COPYRIGTH
		printShortCopyright();
		System.out.println();
		

		// COMMAND LINE PARAMETERS ARE READ OR SET TO THEIR DEFAULT VALUES
    
		// Server and port
		server = parser.getServer();
		if (server == null) {
			server = "localhost";
		}
		port = parser.getPort();
		if (port < 0) {
			port = ServerDefaultValues.PORT;
		}
		
		// Proxy and port
		proxyServer = parser.getProxyServer();
		proxyPort = parser.getProxyPort();
		
		// Target name (an image)
		target = parser.getTarget();
		uri = parser.getURI();
    
		// Image parameters
		components = parser.getComponents();
		resolutionLevel = parser.getResolutionLevel();
		fsiz = parser.getFrameSize();
		roff = parser.getRegionOffset();
		rsiz = parser.getRegionSize();
		maxTargetLength = parser.getTargetLength();
		layers = parser.getLayers();
		quality = parser.getQuality();
		round = parser.getRound();
		outputImageFiles = parser.getImageFiles();
		imageGeometry = parser.getImageGeometry();

		// RUN JPIP CLIENT
		ImageData imageData = new ImageData(ImageData.SAMPLES_FLOAT);
		Client jpipClient = new Client();
		jpipClient.setLogEnabled(false);

		try {
			if (uri != null)  {
				jpipClient.getTarget(uri);
				
			} else {
				jpipClient.newSession(imageData, server, port, proxyServer, proxyPort,
				                      target);

				if ((fsiz == null) && (resolutionLevel < 0) ) {
					if (layers < 0) {
						jpipClient.getTarget(maxTargetLength);
					} else {
						jpipClient.getTarget(maxTargetLength, layers, -1);
					}

				} else if ((fsiz == null) && (resolutionLevel >= 0) ) {
					if (components == null) {
						components = new int[imageData.getMaxComponents()];
						for (int c = 0; c < components.length; c++) components[c] = c;
					}
					if (layers < 0) layers = imageData.getMaxLayers();
					if (quality < 0) quality =100;
					if (round < 0) round = ViewWindowField.ROUND_DOWN;
					jpipClient.getTarget(components, resolutionLevel, roff, rsiz, layers,
					                     quality, maxTargetLength);

				} else if ((fsiz != null) ) {
					if (quality < 0) quality =100;
					if (round < 0) round = ViewWindowField.ROUND_DOWN;
					jpipClient.getTarget(components, fsiz, roff, rsiz, layers, quality,
					                     round, maxTargetLength);

				} else {

				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.out.println("ERROR: " + e.getMessage());
			System.exit(1);
		} catch (ErrorException e) {
			e.printStackTrace();
			System.out.println("ERROR: " + e.getMessage());
			System.exit(1);
		}
		
		// SAVE IMAGE
		float[][][] imageSamplesFloat = null;
		int[] precision  = null;
		try {
			imageSamplesFloat = imageData.getSamplesFloat();
			precision = imageData.getPrecision();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}
		
		for (int f = 0; f < outputImageFiles.length; f++) {
			try{
				SaveFile.SaveFileExtension(imageSamplesFloat, precision,
				                           outputImageFiles[f], imageGeometry);									
			}catch(WarningException e){
				System.out.println("File \""+outputImageFiles[f]+"\" cannot be saved");
			}
		}
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