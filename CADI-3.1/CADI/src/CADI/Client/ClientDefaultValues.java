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
package CADI.Client;

/**
 * This class contains default values of the CADI Client.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2011/10/18
 */
public interface ClientDefaultValues {

	/**
	 * Indicates the default port where the CADI server is listening to the
	 * client request.
	 */
	int PORT = 80;
	
	/**
	 * Indicates the default port where the CADI proxy is listening to the
	 * client request.
	 */
	int PROXY_PORT = 8080;

	/**
	 * It is the number of threads that will be run.
	 */
	int NUM_THREADS = 1;

	/**
	 * Indicates if XML log file format is used. Otherwise, simple text file
	 * format is used. 
	 */
	boolean XML_LOGFILE_FORMAT = false; 
	
	/**
	 * Timeout used when HTTP keep-alive feature is used. Value is milliseconds.
	 */
	int KEEP_ALIVE_TIMEOUT = 1000;
	
}