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
package CADI.Common.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import java.util.logging.XMLFormatter;

/** 
 * This class provides a wrapper for abstracting the destination and format
 * of the logs. The destination (standard output or file) and the format (plain
 * text or XML) are selected in the constructor. In addittion, four log types
 * are available: {@link #logDebug(String)}, {@link #logInfo(String)},
 * {@link #logWarning(String)}, {@link #logError(String)}. 
 * <p>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; logInfo or logWarning or logError or logDebug methods<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.3 2009/12/22
 */
public class CADILog {

	/**
	 * Indicate whether logs are or not enabled.
	 */
	private boolean enabled = true;

	/**
	 * Indicates if XML format is used to create the log file. If XML format is
	 * not setted, simple format is used.
	 * <p>
	 * Default value: false (simple format is used) 
	 */
	private boolean useXMLFormat = false;

	/**
	 * Indicates whether logs are saved in a file or they are printed in the
	 * standard output.
	 */
	private boolean logToFile = false;

	/**
	 * Is the file name where logs are done. If <code>null</code> logs are
	 * printed out in the standard output.
	 */
	private String fileName = null;
	
	/**
	 * Indicates which is the severity of the messages that it will record.
	 * Log messages are recorded only if the message level is greater than
	 * or equal to the <code>logLevel</code> attribute.
	 * <p>
	 * Allowed values: {@link #LEVEL_DEBUG}, {@link #LEVEL_INFO},
	 * {@link #LEVEL_INFO} and, {@link #LEVEL_ERROR}.
	 */
	private int logLevel = LEVEL_INFO;
	public static final int LEVEL_DEBUG = 1;
	public static final int LEVEL_INFO = 2;
	public static final int LEVEL_WARNING = 3;
	public static final int LEVEL_ERROR = 4;
	
	
	// INTERNAL ATTRIBUTES
	
	/**
	 * 
	 */	
	//private LogManager logManager = null;
	private Logger logger = null;
	private FileHandler fileHandler = null;
	private StreamHandler streamHandler = null;
	private OutputStream outputStream = null;

	/**
	 * Indicates the maximum number of bytes to write in any file.
	 */
	private int limit = 10000000;

	/**
	 * Indicates the number of files to use.
	 */
	private int count = 1;

  // ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public CADILog() {
		this(null, false);		
	}
	
	/**
	 * Constructor.
	 * 
	 * @param fileName the file name where logs are writen. If it is null, logs
	 * 	are displayed in the standard ouptut.
	 * @param useXMLFormat if true, logs are formated using XML format.
	 * 	Otherwise, standard format is used. 
	 */
	public CADILog(String fileName, boolean useXMLFormat) {
		// Get a logger
		logger = Logger.getLogger("CADI");
		
		configure(fileName, useXMLFormat);		
	}
	
	/**
	 * 
	 * @param fileName
	 * @param logLevel
	 */
	public CADILog(String fileName, int logLevel) {
		this(fileName, false, logLevel);
	}
	/**
	 * 
	 * @param fileName
	 * @param useXMLFormat
	 * @param logLevel
	 */
	public CADILog(String fileName, boolean useXMLFormat, int logLevel) {
		// Get a logger
		logger = Logger.getLogger("CADI");
		
		this.fileName = fileName;
		this.useXMLFormat = useXMLFormat;
		
		configure(fileName, useXMLFormat);
		setLogLevel(logLevel);
	}
	
	/**
	 * Sets the file name and log format parameters.
	 * 
	 * @param fileName the file name where logs are writen. If it is null, logs
	 * 	are displayed in the standard ouptut.
	 * @param useXMLFormat if true, logs are formated using XML format.
	 * 	Otherwise, standard format is used.
	 */
	public void setParameters(String fileName, boolean useXMLFormat) {
		this.fileName = fileName;
		this.useXMLFormat = useXMLFormat;
		configure(fileName, useXMLFormat);
	}
	
	/**
	 * Method used to the <code>DEBUG</code> logs.
	 *  
	 * @param str the string message.
	 */
	public void logDebug(String str) {
		if ( enabled && (logLevel <= LEVEL_DEBUG)) {
			logger.finest(str);			
			flush();
		}
	}
	
	/**
	 * Method used to the <code>INFO</code> logs.
	 *  
	 * @param str the string message.
	 */
	public void logInfo(String str) {
		if ( enabled && (logLevel <= LEVEL_INFO)) {
			logger.info(str);			
			flush();
		}
	}
	
	/**
	 * Method used to the <code>WARNING</code> logs.
	 *  
	 * @param str the string message.
	 */
	public void logWarning(String str) {
		if (enabled && (logLevel <= LEVEL_WARNING)) {
			logger.warning(str);			
			flush();
		}
	}
	
	/**
	 * Method used to the <code>ERROR</code> logs.
	 *  
	 * @param str the string message.
	 */
	public void logError(String str) {
		if (enabled && (logLevel <= LEVEL_ERROR)) {
			logger.severe(str);			
			flush();
		}
	}

  /**
   * 
   * @param level
   * @param str
   */
  public void log(int level, String str) {
    switch (level) {
      case LEVEL_DEBUG:
        logDebug(str);
        break;
      case LEVEL_INFO:
        logInfo(str);
        break;
      case LEVEL_WARNING:
        logWarning(str);
        break;
      case LEVEL_ERROR:
        logError(str);
        break;
      default:
        throw new IllegalArgumentException("Invalid 'level' value");
    }
  }

  /**
   * Check if logs are enabled for the level <code>level</code>.
   *
   * @param level level to be checked.
   * @return <code>true if logs are enabled. Otherwise, returns <code>false</code>.
   */
  public boolean isLog(int level) {
    return (enabled && (level <= logLevel)) ? true : false;
  }
			
	/**
	 * Sets the log enabled or disabled. 
	 *
	 * @param enabled if <code>true</code>, logs are enabled.
	 *		Otherwise, logs are disabled.
	 */
	public void setEnabled(boolean enabled) {		
		this.enabled = enabled;
	}
	
	/**
	 * Sets the {@link #logLevel} attribute.
	 * 
	 * @param logLevel see {@link #logLevel} attribute.
	 */
	public void setLogLevel(int logLevel) {
		if ( (logLevel < LEVEL_DEBUG) || (logLevel > LEVEL_ERROR) ) throw new IllegalArgumentException();
		this.logLevel = logLevel; 
	}
	
	/**
	 * Closes the log.
	 */
	public void close() {
		if (logToFile) {
			fileHandler.close();
		} else {
			streamHandler.close();
		}
	}
	
	/**
	 * Checks whether the logs are enabled.
	 * 
	 * @return <code>true</code> if the logs are enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Checks whether data are logged using plain text or XML format.
	 * 
	 * @return <code>true</code> if data log format is XML. Otherwise,
	 * 			returns <code>false</code>.
	 */
	public boolean useXMLFormat() {
		return useXMLFormat;
	}
		
	/**
	 * Returns the {@link #fileName} attribute.
	 * 
	 * @return the {@link #fileName} attribue.
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Retuns the {@link #logLevel} attribute.
	 * @return the {@link #logLevel} attribute.
	 */
	public int getLogLevel() {
		return logLevel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";

		str += "<<< Not implemented yet >>> ";

		str += "]";
		return str;
	}
	
	/**
	 * Prints this CADI Log fields out to the specified output stream. This
	 * method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- CADI Log --");		
		
		out.println("<<< Not implemented yet >>> ");
						
		out.flush();
	}
	
	// ============================ private methods ==============================
	/**
	 * Configures the file name and log format parameters.
	 * 
	 * @param fileName the file name where logs are writen. If it is null, logs
	 * 	are displayed in the standard ouptut.
	 * @param useXMLFormat if true, logs are formated using XML format.
	 * 	Otherwise, standard format is used.
	 */
	public void configure(String fileName, boolean useXMLFormat) {
		this.useXMLFormat = useXMLFormat;

		// Set where logs are made
		if (fileName == null) {	// Logs to output stream
			logToFile = false;
			outputStream = System.out;
			if (useXMLFormat) {
				streamHandler = new StreamHandler(outputStream, new XMLFormatter());				
			}
			else {
				streamHandler = new StreamHandler(outputStream, new SimpleFormatter());
			}
			logger.addHandler(streamHandler);

		} else { // Logs to file

			logToFile = true;
			try {
				fileHandler = new FileHandler(fileName, limit, count, false );
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (useXMLFormat) {
				fileHandler.setFormatter(new XMLFormatter());
			} else {
				fileHandler.setFormatter(new SimpleFormatter());
			}		
			logger.addHandler(fileHandler);
		}		

		// Stop forwarding log records to ancestor handlers
		logger.setUseParentHandlers(false);	
	}

	/**
	 * Flush any buffered messages.
	 */
	private void flush() {
		if (logToFile) {
			fileHandler.flush();
		} else {
			streamHandler.flush();				
		}	
	}

}
