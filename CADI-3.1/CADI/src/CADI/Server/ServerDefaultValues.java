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
package CADI.Server;

/**
 * This class contains default values of the CADI Server.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1 2011/03/03
 */
public interface ServerDefaultValues {

	/**
	 * Indicates the default port where the CADI server is listening to the
	 * client request.
	 */
	int PORT = 80;
	
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
	
	int DELIVERING_FILE_ORDER = 1;
	int DELIVERING_CPI = 2;
	int DELIVERING_CoRD = 3;
  int DELIVERING_WINDOW_SCALING_FACTOR = 4;

  /**
   * Indicates the manner in which precinct data are ordered to be deliver.
   * <p>
   * Allowed values are:
   * {@link #DELIVERING_CPI}, {@link #DELIVERING_CoRD},
   * {@link #DELIVERING_FILE_ORDER} and {@link #DELIVERING_WINDOW_SCALING_FACTOR}
   */
	int DELIVERING_MODE = DELIVERING_WINDOW_SCALING_FACTOR;

}