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
package CADI.Proxy;

/**
 * This class defines some default values for the Proxy application.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 23/07/2009
 */
public interface ProxyDefaultValues {

	/**
	 * Indicates the default port where the CADI server is listening to the
	 * client request.
	 */
	int PORT = 8080;

	/**
	 * It is the number of threads that will be launched.
	 */
	int NUM_THREADS = 1;

	/**
	 * Indicates if XML log file format is used. Otherwise, simple text file
	 * format is used. 
	 */
	boolean XML_LOGFILE_FORMAT = false; 
	
	/**
	 * Indicates the type of proxy that will be used. Allowed values are:
	 * &nbsp;1 - transparent proxy: EXPLANATION !!!!!!
	 * &nbsp;2 - cached proxy: EXPLANATION !!!!!!
	 */
	int TRANSPARENT_PROXY = 1;
	int CACHED_PROXY = 2;
	int CACHED_PREFETCHING_PROXY = 3;
	int PROXY_TYPE = CACHED_PREFETCHING_PROXY;

}