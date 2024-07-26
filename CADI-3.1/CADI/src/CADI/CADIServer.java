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
import CADI.Server.ServerDefaultValues;
import CADI.Server.ServerParser;
import CADI.Server.Core.Scheduler;
import CADI.Common.Network.TrafficShaping;
import GiciException.ErrorException;
import GiciException.ParameterException;

/**
 * Main class of CADIServer application. The CADIServer application is a JPIP
 * server that allows to a compliant JPIP Client to retrive images from
 * the server.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.4 2011/03/03
 */
public class CADIServer {

  public static void main(String[] args) {

    Properties properties = System.getProperties();
    //properties.list(System.out);


    //ARGUMENTS VARIABLES
    int[] ports = null;
    int numThreads = 1;
    String targetPath = null;
    String logFile = null;
    boolean XMLLogFormat;
    boolean logEnabled;
    int logLevel = -1;
    int deliveringMode;
    int deliveringSubtype;
    String cachePath = null;
    int keepAliveTimeout = 0;
    int maxTxRate = 0;
    int trafficShaping = TrafficShaping.NONE;
    boolean independentMessageHeaders = true;
    boolean predictiveModel = false;


    // PARSE COMMAND LINE ARGUMENTS
    ServerParser parser = null;
    try {
      parser = new ServerParser(args);
    } catch (ErrorException e) {
      printShortCopyright();
      System.out.println();
      System.out.println("RUN ERROR:" + e.getMessage());
      System.exit(3);
    } catch (ParameterException e) {
      printShortCopyright();
      System.out.println();
      System.out.println("ARGUMENTS ERROR: " + e.getMessage());
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
      ports[0] = ServerDefaultValues.PORT;
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

    // Targets path
    targetPath = parser.getTargetsPath();
    if (targetPath == null) {
      targetPath = properties.getProperty("user.dir");
    }
    if (!targetPath.endsWith(properties.getProperty("file.separator"))) {
      targetPath += properties.getProperty("file.separator");
    }

    // Log
    logFile = parser.getLogFile();
    XMLLogFormat = parser.isXMLLogFormat();
    logEnabled = parser.isLogEnabled();
    logLevel = parser.getLogLevel();

    // Delivering mode
    deliveringMode = parser.getDeliveringMode();
    deliveringSubtype = parser.getDeliveringSubtype();

    // Cache
    cachePath = parser.getCacheDirectory();

    // Keep alive timeout
    keepAliveTimeout = parser.getKeepAliveTimeout();

    // Max. Rate
    maxTxRate = parser.getMaxRate();
    trafficShaping = parser.getTrafficShaping();

    // JPIP message form
    independentMessageHeaders = parser.getIndependentMessageHeaders();

    // Predictive model
    predictiveModel = parser.getPredictiveModel();


    // CHECK PARAMETERS
    if (targetPath != null) {
      if (!(new File(targetPath)).exists()) {
        System.out.println("ARGUMENTS ERROR: targets path (\"" + targetPath
                + "\") does not exist");
        System.exit(1);
      }
    }

    if (cachePath != null) {
      if (!(new File(cachePath)).exists()) {
        System.out.println("ARGUMENTS ERROR: cache directory (\""
                + cachePath + "\") does not exist");
        System.exit(1);
      }
    }

    // RUN SERVER
    Scheduler server = new Scheduler(ports, numThreads, logEnabled, logLevel,
                                     logFile, XMLLogFormat);
    server.setTargetsPath(targetPath);
    server.setCachePath(cachePath);
    server.setDeliveringMode(deliveringMode, deliveringSubtype);
    server.setKeepAliveTimeout(keepAliveTimeout);
    server.setMaxTxRate(maxTxRate);
    server.setTrafficShaping(trafficShaping);
    server.setIndependentMessageHeaders(independentMessageHeaders);
    server.setPredictiveModel(predictiveModel);
    server.start();
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
