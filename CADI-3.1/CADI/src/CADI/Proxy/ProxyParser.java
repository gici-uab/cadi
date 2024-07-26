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

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import CADI.Common.Log.CADILog;
import CADI.Common.Util.ArgumentsParser;
import CADI.Proxy.Core.ProxyPrefetching;
import CADI.Common.Network.TrafficShaping;
import GiciException.ErrorException;
import GiciException.ParameterException;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/03/04
 */
public class ProxyParser extends ArgumentsParser {

  //ARGUMENTS SPECIFICATION
  private static final String[][] serverArguments = {
    {"-p", "--ports", "{int[int [int [ ...]]]}", ProxyDefaultValues.PORT + "", "0", "1",
     "Ports where server will be listening to the client requests."
    },
    {"-nt", "--numThreads", "{int}", ProxyDefaultValues.NUM_THREADS + "", "0", "1",
     "Number of threads that will be launched to process client requests."
    },
    {"-t", "--type", "{int}", ProxyDefaultValues.PROXY_TYPE + "", "0", "1",
     "Indicates the type of proxy that will be used. Allowed values are:\n"
      + "\t1- transparent proxy\n"
      + "\t2- cached proxy. All data transmitted from servers to clients are cached.\n"
      + "\t3- cache proxy with prefetching. It is an extension of the \"cached proxy\" adding the capability of prefetching.\n"
      + "OBS: Option 3 can be qualified by \"-pwt\" or \"-pm\" parameters. If none of them is set, deafult options to be considered are: \"-pdh=3\", \"-pwt=1\", and \"-mp=0.1\" , respectively\n"
    },
    {"-pdh", "--prefetchingDataHistory", "{int}", "3", "0", "1",
     "Is the data, previous windows of interest requested, used to build the actual WOI to be prefetched. Allowed values:\n"
      + "\t" + ProxyPrefetching.MODE_ONLY_IMAGE_HISTORY + "- uses the history of the windows of interest requested by all clients over an image. View windows are sorted following a FIFO strategy for all clients.\n"
      + "\t" + ProxyPrefetching.MODE_ONLY_CLIENT_HISTORY + "- prediction of windows of interest to be downloaded are done for each client taken into account only the historic wois for that client.\n"
      + "\t" + ProxyPrefetching.MODE_LAST_WOI_ALL_CLIENTS + "- computes the prefetching woi using the lastest window of interest requested by all clients over each image.\n "
      + "\tOBS: This option can only be set when the --type parameter is 3 (cached proxy with prefetching)."
    },
    {"-pwt", "--prefetchingWOIType", "{int}", "1", "0", "1",
     "Allows to choose how the Windows of Interest to be prefetching is built from the historic of data. Allowed values:\n"
      + "\t" + ProxyPrefetching.WOI_TYPE_WEIGHTED_WOI + "- prediction of the window of interest to be prefetched  is based on weighted wois of the historic of windows of interest. Probability of movements can be modified by means of the \"-mp\" parameter.\n"
      + "\t" + ProxyPrefetching.WOI_TYPE_BOUNDING_BOX + "- the window of interest to be prefected is the bounding box of the historic window of interest.\n"
      + "OBS: This parameter is only allowed if the \"--type\" parameter is 3 (cached proxy with prefetching).\n"
      + "OBS: This parameter is not compatible with the \"-pm\" parameter."
    },
    {"-mp", "--movementProbabilities", "{float float float float float float float float float float}", "0.1", "0", "1",
     "Probabilities of the movements to be used by the prefetching. Values must be sorted according with the following criterion: "
      + "right, up-right, up, up-left, left, down-left, down, down_right, zoom in, zoom out.\n"
      + "\tOBS: The sum of all values must be less or equal than 1.\n"
      + "\tOBS: This option can only be set when the --type parameter is 3 (cached proxy with prefetching) and \"-pwt\" is "+ProxyPrefetching.WOI_TYPE_WEIGHTED_WOI+"."
    },
    {"-pm", "--predictiveModel", "{string}", "", "0", "1",
     "This parameter indicates that a predictive model, semantic information, is applied in the image delivering and prefetching."
      +" Predictive model to be applied is read from a text file located at path given by this parameter and whose name is the same as the compressed image but with the extension \"pm\"."
      +" The file must have a line for each spatial region (precinct) with the following format \"precinct_id value\", where the precinct_id is the unique precinct identifier defined in the JPIP protocol and the value is a real number in the range [0, 1] with the relevance of the precinct."
      +" The remainder precincts not included in the file will be considered with relevance 0. And if the beginning-of-line character is an #, it is considered a comment and ignored.\n"
      + "OBS: This parameter is only allowed if the \"--type\" parameter is 3 (cached proxy with prefetching).\n"
      + "OBS: This parameter is not compatible with the \"-pwt\" parameter.\n"
      + "OBS: If there is not a semantic file associated with an image, prefetching is done considering the option "+ProxyPrefetching.WOI_TYPE_WEIGHTED_WOI+" of the -pwt parameter."
    },
    {"-lf", "--logFile", "{string}", "", "0", "1",
     "File where logs are saved."
    },
    {"-lx", "--logXML", "{boolean}", ProxyDefaultValues.XML_LOGFILE_FORMAT ? "1" : "0", "0", "1",
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

  private int prefetchingDataHistory = -1; // ProxyPrefetching.MODE_ONLY_IMAGE_HISTORY;

  private int prefetchingWOIType = -1; // ProxyPrefetching.WOI_TYPE_WEIGHTED_WOI;

  private String predictiveModel = null;

  private int numThreads = 0;

  private int proxyType = ProxyDefaultValues.PROXY_TYPE;

  private String logFile = null;

  private boolean XMLLogFormat = false;

  private boolean logEnabled = false;

  private int logLevel = -1;

  private String cacheDirectory = null;

  private int maxRate = 0;

  private int trafficShaping = TrafficShaping.NONE;

  private float[] movProbabilities = null;

  // ============================= public methods ==============================
  /**
   * Receives program arguments and parses it, setting to arguments variables.
   *
   * @param arguments the array of strings passed at the command line
   *
   * @throws ParameterException when an invalid parsing is detected
   * @throws ErrorException when some problem with method invocation occurs
   */
  public ProxyParser(String[] arguments) throws ParameterException, ErrorException {

    logLevel = (new CADILog()).getLogLevel();

    try {
      Method m = this.getClass().getMethod("parseArgument", new Class[]{int.class, String[].class});
      parse(serverArguments, arguments, this, m);
    } catch (NoSuchMethodException e) {
      throw new ErrorException("Proxy parser error invoking parse function.");
    }

    // Check restrictions
    if (!(proxyType == ProxyDefaultValues.CACHED_PREFETCHING_PROXY)
            && ((prefetchingDataHistory != -1) || (prefetchingWOIType != -1))) {
      throw new ParameterException("\"pdh\" or \"pdt\" are only allowed with \"-t\" equals to " + ProxyDefaultValues.CACHED_PREFETCHING_PROXY);
    }
    if ((proxyType == ProxyDefaultValues.CACHED_PREFETCHING_PROXY)
            && (prefetchingWOIType != -1) && (predictiveModel != null)) {
      throw new ParameterException("\"pwt\" and \"pm\" are not compatible");
    }
    if (proxyType == ProxyDefaultValues.CACHED_PREFETCHING_PROXY) {
      if (prefetchingDataHistory == -1) {
        prefetchingDataHistory = ProxyPrefetching.MODE_LAST_WOI_ALL_CLIENTS;
      }
      if (prefetchingWOIType == -1) {
        prefetchingWOIType = ProxyPrefetching.WOI_TYPE_WEIGHTED_WOI;
      }
    }

    if (movProbabilities != null) {
      float sum = 0;
      for (float prob : movProbabilities) {
        sum += prob;
      }
      if (sum > 1.00001F) { // takes an epsilon to avoid precision problems
        throw new ParameterException("The sum of the movement probabilities must be lower or equal than 1");
      }
    }
  }

  /**
   * Parse an argument using parse functions from super class and put its
   * value/s to the desired variable. This function is called from parse
   * function of the super class.
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
      case 2: // -t  --type
        proxyType = parseIntegerPositive(options);
        break;
      case 3: // -pdh  --prefetchingDataHistory
        prefetchingDataHistory = parseIntegerPositive(options);
        break;
      case 4: // -pwt  --prefetchingWOIType
        prefetchingWOIType = parseIntegerPositive(options);
        break;
      case 5: // -mp  --movementProbabilities
        movProbabilities = parseFloatArray(options, 10);
        break;
      case 6: // -pm  --predictiveModel
        predictiveModel = parseString(options);
        break;
      case 7: // -lf  --logFile
        logFile = parseString(options);
        break;
      case 8: // -lx  --logXML
        XMLLogFormat = parseBoolean(options);
        break;
      case 9: // -le  --logEnabled
        logEnabled = true;
        break;
      case 10: // -ll  --logLevel
        logLevel = parseIntegerPositive(options);
        break;
      case 11: // -cd  --cacheDirectory
        cacheDirectory = parseString(options);
        break;
      case 12: // -mr  --maxRate
        maxRate = parseIntegerPositive(options);
        break;
      case 13: // -ts  --trafficShapping
        trafficShaping = parseIntegerPositive(options);
        break;
      case 14: // -h  --help
        try {
          Properties cadiInfo = new Properties();
          InputStream cadiInfoURL = getClass().getClassLoader().getResourceAsStream("CADI/Common/Info/cadiInfo.properties");
          cadiInfo.load(cadiInfoURL);
          System.out.println("CADIProxy version " + cadiInfo.getProperty("version") + "\n");
        } catch (Exception e) {
          System.out.println("PARAMETERS ERROR: error reading properties file.");
          System.out.println("Please report this error to: gici-dev@deic.uab.es");
        }
        showArgsInfo();
        //showArgsInfoLatexTable();
        System.exit(0);
        break;
      case 15: // -w  --warranty
        printWarranty();
        System.exit(0);
        break;
      case 16: // -l  --liability
        printLiability();
        System.exit(0);
        break;
      case 17: // -c  --copyright
        printCopyright();
        System.exit(0);
        break;
      default:
        assert (true);
    }
  }

  public int[] getPorts() {
    return ports;
  }

  public int getPrefetchingDataHistory() {
    return prefetchingDataHistory;
  }

  public int getPrefetchingWOIType() {
    return prefetchingWOIType;
  }

  public String getPredictiveModel() {
    return predictiveModel;
  }

  public int getNumThreads() {
    return numThreads;
  }

  public int getProxyType() {
    return proxyType;
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

  public String getCacheDirectory() {
    return cacheDirectory;
  }

  public int getMaxRate() {
    return maxRate;
  }

  public int getTrafficShaping() {
    return trafficShaping;
  }

  public float[] getMovementProbabilities() {
    return movProbabilities;
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
