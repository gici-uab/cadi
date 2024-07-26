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

import CADI.Common.Log.CADILog;
import CADI.Common.Util.ArgumentsParser;
import CADI.Common.Network.TrafficShaping;
import GiciException.*;

import java.io.InputStream;
import java.lang.reflect.*;
import java.util.Properties;

/**
 * Arguments parser for CADI server
 *  
 *	@author Group on Interactive Coding of Images (GICI)
 * @version 1.0.5 2009/05/02
 */
public class ServerParser extends ArgumentsParser {

  //ARGUMENTS SPECIFICATION
  private String[][] serverArguments = {
    {"-p", "--ports", "{int[int [int [ ...]]]}", ServerDefaultValues.PORT + "", "0", "1",
     "Ports where server will listen to the client request."
    },
    {"-nt", "--numThreads", "{int}", ServerDefaultValues.NUM_THREADS + "", "0", "1",
     "Number of threads that will be launched to process the request."
    },
    {"-tp", "--targetsPath", "{string}", "" + "", "0", "1",
     "Root directory where the targets (image files) are stored. Default value is the directory where the server is launched."
    },
    {"-lf", "--logFile", "{string}", "", "0", "1",
     "File where logs are saved."
    },
    {"-lx", "--logXML", "{boolean}", ServerDefaultValues.XML_LOGFILE_FORMAT ? "1" : "0", "0", "1",
     "XML format is used in the log file. Value is a boolean: 0 indicates simple file format is used and 1 indicates XML format is used."
    },
    {"-le", "--logEnabled", "{boolean}", "", "0", "1",
     "Enables or disables the log. See the \"-ll\" parameter for more information about the detail level of logs."
    },
    {"-ll", "--logLevel", "{int}", CADILog.LEVEL_INFO + "", "0", "1",
     "Is the severity of the messages which will be logged. The \"-le\" parameter is set automatically. Available values are:\n"
      + "\t" + CADILog.LEVEL_INFO + "- logs informative messages\n"
      + "\t" + CADILog.LEVEL_WARNING + "- logs warning messages\n"
      + "\t" + CADILog.LEVEL_ERROR + "- logs error messages\n"
      + "when a log level is set, all upper levels are automatically set but lower severity messages are filtered."
    },
    {"-cd", "--cacheDirectory", "{string}", "", "0", "1",
     "Directory used as a temporal directory to save the cache data (not implemented yet)."
    },
    {"-dm", "--deliveringMode", "{int}", ServerDefaultValues.DELIVERING_MODE + "", "0", "1",
     "Indicates the rate distortion method which is used in a JPEG2000 image to calculate de Window Of Interest. Available values are:\n"
      + "\t" + ServerDefaultValues.DELIVERING_FILE_ORDER + "- precinct data are delivered layer by layer just as they appear in the codestream\n"
      + "\t" + ServerDefaultValues.DELIVERING_CPI + "- the CPI method is uses to calculate the WOI. It only can be used with request that belongs to a session.\n"
      + "\t" + ServerDefaultValues.DELIVERING_CoRD + "- the CoRD method is uses to calculate the WOI.\n"
      + "\t" + ServerDefaultValues.DELIVERING_WINDOW_SCALING_FACTOR + "- applies a window scaling factor to each precinct to be delivered consisting on the overlap factor of each precinct with the window of interest requested.\n"
      + "OBS: Option " + ServerDefaultValues.DELIVERING_CoRD + " has been temporary disabled."
    },
    {"-cpit", "--cpiType", "{int}", 0 + "", "0", "1",
     "Indicates, within the CPI rate-distortion method, the subtype which will be used to delivery the requested WOI. This parameter must be only available when the rate-distortion (-rd) parameter is 2. Otherwise, it won't be taken into account. Available values are:\n"
      + "\t1- Only one packet for each precinct is generated. Therefore, delivered image has only one quality layer.\n"
      + "\t2- Creates one packet per each bit plane. Therefore, delivered image has as quality layers as number of bit planes.\n"
      + "\t3- Creates one packet per each coding pass. Therefore, delivered image has as quality layers as number of coding passes.\n"
      + "\t4- Images are delivered following the SCALE method.\n"
      + "This parameter is only allowd if the \"-dm\" parameter is " + ServerDefaultValues.DELIVERING_CPI + "."
    },
    {"-cordt", "--cordType", "{int}", 0 + "", "0", "1",
     "Indicates, within the CPI rate-distortion method, the subtype which will be used to delivery the requested WOI. This parameter must be only available when the rate-distortion (-rd) parameter is 2. Otherwise, it won't be taken into account. Available values are:\n"
      + "\t1- Is the classic CoRD algorithm.\n"
      + "\t2- Is a modification of the CoRD algorithm in order to reduce the overhead of packet and JPIP message headers.\n"
      + "This parameter is only allowd if the \"-dm\" parameter is " + ServerDefaultValues.DELIVERING_CoRD + "."
    },
    {"-dpo", "--deliveryProgressionOrder", "{int}", "" + "", "0", "1",
     "Indicates the progression order which will be used to delivery data. If this parameter is not set, the progression order of the codestream will be used. Otherwise, the progression order of the codestream will not be taken into account. Available values are:\n"
      + "\t0- Layer-Resolution-Component-Position (LRCP).\n"
      + "\t1- Resolution-Layer-Component-Position (RLCP).\n"
      + "\t2- Resolution-Position-Component-Layer (RPCL).\n"
      + "\t3- Position-Component-Resolution-Layer (PCRL).\n"
      + "\t4- Component-Position-Resolution-Layer (CPRL).\n"
      + "This parameter is only allowed if the \"-dm\" parameter is " + ServerDefaultValues.DELIVERING_FILE_ORDER + "."
    },
    {"-kt", "--keepAliveTiemout", "{int}", "" + "1", "0", "1",
     "Specifies the timeout (in milliseconds) that the socket is kept opened and waiting for new client requests."
    },
    {"-mr", "--maxRate", "{int}", "0", "0", "1",
     "Specifies the maximum rate (bytes per second) which will be used to delivery data (0 means unlimited)."
    },
    {"-ts", "--trafficShaping", "{int}", "0", "0", "1",
     "Allows to choose a trafic shaping algorithm. Allowed values are:"
      + "\t" + TrafficShaping.NONE + "- None algoritm is applied."
      + "\t" + TrafficShaping.TOKEN_BUCKET + "- The token-bucket algoritm is applied. Data are transmitted at the constant rate fixed by the \"-mr\" parameter rate, but it also allows data busts."
      + "\t" + TrafficShaping.LEAKY_BUCKET + "- The leaky-bucket algoritm is applied. Data are delivered at the constant rate defined in the \"-mr\" option."
      + "OBS: This parameter requires the \"-mr\" parameter."
    },
    {"-imh", "--independentMessageHeaders", "{booelan}", "1", "0", "1",
     "Indicates which form of JPIP messsage headers will be used (independent or dependent). The independent form is a long form where the message headers are completely self-describing. Meanwhile, the depenent form message headers make use of information in the headers of previous messages. The dependent form makes a shorter message header than the independent form, but sequence ordering of received message must be taken into account."
    },
    {"-pm", "--predictiveModel", "{boolean}", "0", "0", "1",
     "This parameter indicates that a predictive model can be applied in the image delivering. The predictive model to be applied is read from a text file whose name is the same as the compressed image but with the extension \"pm\". The file must have a line for each spatial region (precinct) with the following format \"precinct_id value\", where the precinct_id is the unique precinct identifier defined in the JPIP protocol and the value is a real number in the range [0, 1] with the relevance of the relevance of the precinct. The remainder precincts not included in the file will be considered with relevance 0. And if the beginning-of-line character is an #, it is considered a comment and ignored."
      + "This parameter is only allowed if the \"-dm\" parameter is " + ServerDefaultValues.DELIVERING_WINDOW_SCALING_FACTOR + "."
    },
    {"-h", "--help", "", "", "0", "1",
     "Displays this help and exits program."
    },
    {"-w", "--warranty", "", "", "0", "1",
     ""
    },
    {"-l", "--liability", "", "", "0", "1",
     ""
    },
    {"-c", "--copyright", "", "", "0", "1",
     ""
    }
  };
  //ARGUMENTS VARIABLES

  private int[] ports = null;

  private int numThreads = 0;

  private String targetsPath = null;

  private String logFile = null;

  private boolean XMLLogFormat = false;

  private boolean logEnabled = false;

  private int logLevel = -1;

  private String cacheDirectory = null;

  private int deliveringMode = ServerDefaultValues.DELIVERING_MODE;

  private int deliveringSubtype = -1;

  private int cpiType = -1;

  private int cordType = -1;

  private int deliveryProgressionOrder = -1;

  private int keepAliveTimeout = 1000; // time in miliseconds

  private int maxRate = 0;

  private int trafficShaping = TrafficShaping.NONE;

  private boolean independentMessageHeaders = true;

  private boolean predictiveModel = false;

  // ============================= public methods ==============================
  /**
   * Receives program arguments and parses it, setting to arguments variables.
   *
   * @param arguments the array of strings passed at the command line
   *
   * @throws ParameterException when an invalid parsing is detected
   * @throws ErrorException when some problem with method invocation occurs
   */
  public ServerParser(String[] arguments) throws ParameterException, ErrorException {

    logLevel = (new CADILog()).getLogLevel();

    try {
      Method m = this.getClass().getMethod("parseArgument", new Class[]{int.class, String[].class});
      parse(serverArguments, arguments, this, m);
    } catch (NoSuchMethodException e) {
      throw new ErrorException("Coder parser error invoking parse function.");
    }


    // CHECK PARAMETERS

    if ((deliveringMode < 1) || (deliveringMode > 4)) {
      throw new ParameterException("The delivering mode parameter \"-dm\""
              + " is not in the allowed range");
    }

    // cpit only allowed with delivering mode cpi
    if (cpiType != -1) {
      if ((deliveringMode != ServerDefaultValues.DELIVERING_CPI)) {
        throw new ParameterException("The cpi type (\"-cpit\") parameter can"
                + "only be used when the delivering mode parameter (\"-dm\")"
                + "is " + ServerDefaultValues.DELIVERING_CPI);
      }

      if ((cpiType < 1) || (cpiType > 4)) {
        throw new ParameterException("The delivering mode parameter \"-cpit\""
                + " is not in the allowed range");
      }

      deliveringSubtype = cpiType;
    }

    // cordt only allowed with delivering mode cord
    if (cordType != -1) {
      if ((deliveringMode != ServerDefaultValues.DELIVERING_CoRD)) {
        throw new ParameterException("The cpi type (\"-cordt\") parameter can"
                + "only be used when the delivering mode parameter (\"-dm\")"
                + "is " + ServerDefaultValues.DELIVERING_CoRD);
      }

      if ((cordType < 1) || (cordType > 2)) {
        throw new ParameterException("The delivering mode parameter \"-cpit\""
                + " is not in the allowed range");
      }

      deliveringSubtype = cpiType;
    }


    // progression orders only allowed with delivering mode file
    if (deliveryProgressionOrder != -1) {
      if ((deliveringMode != ServerDefaultValues.DELIVERING_FILE_ORDER)) {
        throw new ParameterException("The cpi type (\"-dpo\") parameter can"
                + "only be used when the delivering mode parameter (\"-dm\")"
                + "is " + ServerDefaultValues.DELIVERING_FILE_ORDER);
      }
      if ((deliveryProgressionOrder < 0) || (deliveryProgressionOrder > 4)) {
        throw new ParameterException("The delivering mode parameter \"-dpo\""
                + " is not in the allowed range");
      }
      deliveringSubtype = deliveryProgressionOrder;
    }

    // traffic shaping needs max. rate parameter

    if ((trafficShaping > 0) && (maxRate <= 0)) {
      throw new ParameterException("The traffic shaping parameter (\"-ts\") can"
              + "only be used when the max. rate (\"-mr\") is defined.");
    }

  }

  /**
   * Parse an argument using parse functions from super class and put its value/s to the desired variable. This function is called from parse function of the super class.
   *
   * @param argFound number of parameter (the index of the array coderArguments)
   * @param options the command line options of the argument
   *
   * @throws ParameterException when some error about parameters passed (type, number of params, etc.) occurs
   */
  public void parseArgument(int argFound, String[] options) throws ParameterException {

    switch (argFound) {

      case 0: // -p  --port
        ports = this.parseIntegerArray(options);
        break;
      case 1: // -nt  --numThreads
        numThreads = parseIntegerPositive(options);
        break;
      case 2: // -tp  --targetsPath
        targetsPath = parseString(options);
        break;
      case 3: // -lf  --logFile
        logFile = parseString(options);
        break;
      case 4: // -lx  --logXML
        XMLLogFormat = parseBoolean(options);
        break;
      case 5: // -le  --logEnabled
        logEnabled = true;
        break;
      case 6: // -ll  --logLevel
        logLevel = parseIntegerPositive(options);
        break;
      case 7: // -cd  --cacheDirectory
        cacheDirectory = parseString(options);
        break;
      case 8: // -dm --deliveringMode
        deliveringMode = parseIntegerPositive(options);
        break;
      case 9: // -cpit --cpiType
        cpiType = parseIntegerPositive(options);
        break;
      case 10: // -cordt --cordType
        cordType = parseIntegerPositive(options);
        break;
      case 11: // -dpo --deliveryProgressionOrder
        deliveryProgressionOrder = parseIntegerPositive(options);
        break;
      case 12: // -kt  --keepAliveTimeout
        keepAliveTimeout = parseIntegerPositive(options);
        break;
      case 13: // -mr  --maxRate
        maxRate = parseIntegerPositive(options);
        break;
      case 14: // -ts  --trafficShaping
        trafficShaping = parseIntegerPositive(options);
        break;
      case 15: // -imh  --independentMessageHeaders
        independentMessageHeaders = parseBoolean(options);
        break;
      case 16: // -pm  --predictiveModel
        predictiveModel = parseBoolean(options);
        break;
      case 17: // -h  --help
        try {
          Properties cadiInfo = new Properties();
          InputStream cadiInfoURL = getClass().getClassLoader().getResourceAsStream("CADI/Common/Info/cadiInfo.properties");
          cadiInfo.load(cadiInfoURL);
          System.out.println("CADIServr version " + cadiInfo.getProperty("version") + "\n");
        } catch (Exception e) {
          System.out.println("PARAMETERS ERROR: error reading properties file.");
          System.out.println("Please report this error to: gici-dev@deic.uab.es");
        }
        showArgsInfo();
        //showArgsInfoLatexTable();
        System.exit(0);
        break;
      case 18: // -w  --warranty
        printWarranty();
        System.exit(0);
        break;
      case 19: // -l  --liability
        printLiability();
        System.exit(0);
        break;
      case 20: // -c  --copyright
        printCopyright();
        System.exit(0);
        break;
      default:
        assert (true);
    }
  }

  //ARGUMENTS GET FUNCTIONS
  public int[] getPorts() {
    return ports;
  }

  public int getNumThreads() {
    return numThreads;
  }

  public String getTargetsPath() {
    return targetsPath;
  }

  public String getLogFile() {
    return logFile;
  }

  public boolean isXMLLogFormat() {
    return XMLLogFormat;
  }

  public boolean isLogEnabled() {
    return logEnabled;
  }

  public int getLogLevel() {
    return logLevel;
  }

  public int getDeliveringMode() {
    return deliveringMode;
  }

  public int getDeliveringSubtype() {
    return deliveringSubtype;
  }

  public String getCacheDirectory() {
    return cacheDirectory;
  }

  public int getKeepAliveTimeout() {
    return keepAliveTimeout;
  }

  public int getMaxRate() {
    return maxRate;
  }

  public int getTrafficShaping() {
    return trafficShaping;
  }

  public boolean getIndependentMessageHeaders() {
    return independentMessageHeaders;
  }

  public boolean getPredictiveModel() {
    return predictiveModel;
  }

  // ============================ private methods ==============================
  /**
   * Prints out the warranty.
   */
  private void printWarranty() {
    try {
      Properties cadiInfo = new Properties();
      InputStream cadiInfoURL = getClass().getClassLoader().getResourceAsStream("CADI/Common/Info/cadiInfo.properties");
      cadiInfo.load(cadiInfoURL);

      System.out.println("CADIClient version " + cadiInfo.getProperty("version") + "\n");
      System.out.println(cadiInfo.getProperty("disclaimerOfWarranty"));

    } catch (Exception e) {
      System.out.println("PARAMETERS ERROR: error reading the disclaimer of warranty");
      System.out.println("Please report this error to: gici-dev@deic.uab.es");
    }
  }

  /**
   * Prints out the liability.
   */
  private void printLiability() {
    try {
      Properties cadiInfo = new Properties();
      InputStream cadiInfoURL = getClass().getClassLoader().getResourceAsStream("CADI/Common/Info/cadiInfo.properties");
      cadiInfo.load(cadiInfoURL);

      System.out.println("CADIClient version " + cadiInfo.getProperty("version") + "\n");
      System.out.println(cadiInfo.getProperty("limitationsOfLiability"));

    } catch (Exception e) {
      System.out.println("PARAMETERS ERROR: error reading limitations of liability");
      System.out.println("Please report this error to: gici-dev@deic.uab.es");
    }
  }

  /**
   * Prints out the copyright.
   */
  private void printCopyright() {
    try {
      Properties cadiInfo = new Properties();
      InputStream cadiInfoURL = getClass().getClassLoader().getResourceAsStream("CADI/Common/Info/cadiInfo.properties");
      cadiInfo.load(cadiInfoURL);

      System.out.println("CADIClient version " + cadiInfo.getProperty("version") + "\n");
      System.out.println(cadiInfo.getProperty("copyright"));

    } catch (Exception e) {
      System.out.println("PARAMETERS ERROR: error reading copyright");
      System.out.println("Please report this error to: gici-dev@deic.uab.es");
    }
  }
}
