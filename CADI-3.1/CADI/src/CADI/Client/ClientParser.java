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

import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Common.Util.ArgumentsParser;
import CADI.Server.ServerDefaultValues;
import GiciException.*;

import java.io.InputStream;
import java.lang.reflect.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Arguments parser for CADI client
 *  
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2008/04/28
 */
public class ClientParser extends ArgumentsParser {

  //ARGUMENTS SPECIFICATION
  private String[][] clientArguments = {
    {"-s", "--server", "{string}", "localhost" + "", "0", "1",
     "Server name where the image to be retrieved is."
    },
    {"-p", "--port", "{int}", ClientDefaultValues.PORT + "", "0", "1",
     "Port number where the server is listening to client requests."
    },
    {"-ps", "--proxyServer", "{string}", "" + "", "0", "1",
     "Server name where the image to be retrieved is."
    },
    {"-pp", "--proxyPort", "{int}", ClientDefaultValues.PROXY_PORT + "", "0", "1",
     "Port number where the server is listening to client requests."
    },
    {"-t", "--target", "{string}", "" + "", "0", "1",
     "Targets to be retrieved from the server. If this parameter is not set, the uri (\"-u\") parameter must be appear."
    },
    {"-u", "--uri", "{string}", "" + "", "0", "1",
     "Is an Uniform Resource Identifier of the target to be retrieved. If this parameter is not set, the target (\"-t\") parameter must be appear."
    },
    {"-o", "--outputImage", "{string}", "", "1", "0",
     "Output image file name where recovered image samples will be stored. Image file type will be decided depending on the extension. Valid file types are:\n    RAW/IMG raw data (if outputImage is raw data, \"-og\" argument is mandatory)\n    PNM/PGM/PPM PGM is used when image have 1 component, PPM when there are 3 components and PNM for both\n    TIFF\n    JPEG (not recommended because it degenerates recovered image)\n    BMP (not recommended)\n    ATTENTION: if recovered image samples are signed only RAW/IMG data with a signed data type will preserve image samples consistently. Using other file format will cause image data damages."
    },
    {"-og", "--ouputImageGeometry", "{int int}", "", "0", "1",
     "Image raw data type. Parameters are:\n    1- data type. Possible values are:\n      0- boolean (1 byte)\n      1- unsigned int (1 byte)\n      2- unsigned int (2 bytes)\n      3- signed int (2 bytes)\n      4- signed int (4 bytes)\n      5- signed int (8 bytes)\n      6- float (4 bytes)\n      7- double (8 bytes)\n    2- Byte order (0 if BIG ENDIAN, 1 if LITTLE ENDIAN)"
    },
    {"-cs", "--compnents", "{int[int [int [ ...]]]}", "" + "", "0", "1",
     "Components of the image to be retrieved. This parameters is not compatible with the components ranges (\"-cr\") parameter"
    },
    {"-cr", "--componentRanges", "{int-int [int-int [int-int [ ...]]]}", "", "0", "1",
     "Ranges of components. This parameters is not compatible with the components (\"-cr\") parameter. \n ATTENTION: This option is not implemented yet!"
    },
    {"-rl", "--resolutionLevel", "{int}", "", "0", "1",
     "Is the resolution level at which the image is retrieved. This parameter is not compatible with frame size (\"-fs\") parameter."
    },
    {"-fs", "--frameSize", "{int int}", "", "0", "1",
     "Is the resolution level associate with the Window of Interest. The first value is the width, and the second one is the height. This parameter is not compatible with resolution level (\"-rl\") parameter."
    },
    {"-ro", "--regionOffset", "{int int}", "", "0", "1",
     "Is the top-left coordinates of the Window of Interest. The first value is the left coordinate, and the second on is the top coordinate."
    },
    {"-rs", "--regionSize", "{int int}", "", "0", "1",
     "Is the size of the Window of Interest. The first value is the width, and the second one is the height."
    },
    {"-ly", "--layers", "{int}", "0", "0", "1",
     "Specifies the maximum number of layers which will retrieved for the target."
    },
    {"-q", "--quality", "{int}", "100", "0", "1",
     "Specifies the maximum quality which will retrieved for the target.\n ATTENTION: This option is not implemented yet!"
    },
    {"-rd", "--roundDirection", "{int}", "" + ViewWindowField.ROUND_DOWN, "0", "1",
     "Specifies how the image resolution shall be selected. Allowed values:\n     " + ViewWindowField.ROUND_DOWN + "- round-down\n     " + ViewWindowField.ROUND_UP + "- round-up\n     " + ViewWindowField.CLOSEST + "- closest"
    },
    {"-ml", "--maxTargetLength", "{int}", "0", "0", "1",
     "Specifies the maximum amount of bytes the client wants the server to send in response to the client request."
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
  private String server = "localhost";

  private int port = ClientDefaultValues.PORT;

  private String proxyServer = null;

  private int proxyPort = ClientDefaultValues.PROXY_PORT;

  private String target = null;

  private String uri = null;

  private int[] components = null;

  private int resolutionLevel = -1;

  private int[] fsiz = null;

  private int[] roff = null;

  private int[] rsiz = null;

  private int len = -1;

  private int layers = -1;

  private int quality = -1;

  private int round = -1;

  private String[] imageFiles = null;

  private int[] imageGeometry = null;

  // INTERNAL ATTRIBUTES
  //Number of imageFiles specified
  private int ifs = 0;
  // Ranges of components

  private int[][] componentsRanges = null;

  // ============================= public methods ==============================
  /**
   * Receives program arguments and parses it, setting to arguments variables.
   *
   * @param arguments the array of strings passed at the command line
   *
   * @throws ParameterException when an invalid parsing is detected
   * @throws ErrorException when some problem with method invocation occurs
   */
  public ClientParser(String[] arguments) throws ParameterException, ErrorException {

    try {
      Method m = this.getClass().getMethod("parseArgument", new Class[]{int.class, String[].class});
      parse(clientArguments, arguments, this, m);
    } catch (NoSuchMethodException e) {
      throw new ErrorException("CADI Client parser error invoking parse function.");
    }

    // Check conditional parameters compatiblity
    if ((target == null) && (uri == null)) {
      throw new ParameterException("Neither the target (\"-t\") parameter nor the uri (\"-u\") parameter appear in the command line. One of them must be appear");
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
      case 0: // -s  --server
        server = parseString(options);
        break;
      case 1: // -p  --port
        port = parseIntegerPositive(options);
        break;
      case 2: // -ps  --proxyServer
        proxyServer = parseString(options);
        break;
      case 3: // -pp  --proxyPort
        proxyPort = this.parseIntegerPositive(options);
        break;
      case 4: // -t  --target
        if (uri != null)
          throw new ParameterException("The target (\"-t\") parameter is not compatible with the uri (\"-u\") parameter");
        target = parseString(options);
        break;
      case 5: // -u --uri
        if (target != null)
          throw new ParameterException("The uri (\"-u\") parameter is not compatible with the target (\"-t\") parameter");
        uri = parseString(options);
        try {
          new URI(uri);
        } catch (URISyntaxException e) {
          throw new ParameterException(e.getMessage());
        }
        break;
      case 6: // -o  --outputImage
        if (imageFiles != null) {
          String[] imageFilesTMP = new String[imageFiles.length + 1];
          for (int file = 0; file < imageFiles.length; file++) {
            imageFilesTMP[file] = imageFiles[file];
          }
          imageFiles = imageFilesTMP;
        } else {
          imageFiles = new String[1];
        }
        imageFiles[ifs] = parseString(options);
        if (imageFiles[ifs].endsWith(".raw") || imageFiles[ifs].endsWith(".img")) {
          clientArguments[7][4] = "1";
        }
        ifs++;
        break;
      case 7: // -og  --outputImageGeometry
        imageGeometry = parseIntegerArray(options, 2);
        break;
      case 8: // -cs  --components
        if (componentsRanges != null)
          throw new ParameterException("The components (\"-cs\") parameter is not compatible with the components ranges (\"-cr\") parameter");
        components = parseIntegerArray(options);
        break;
      case 9: // -cr  --componentRanges
        if (components != null)
          throw new ParameterException("The components ranges (\"-cr\") parameter is not compatible with the components (\"-cs\") parameter");
        componentsRanges = parseIntegerRangesArray(options);
        int numComps = 0;
        for (int i = 0; i < componentsRanges.length; i++) {
          numComps += componentsRanges[i][1] - componentsRanges[i][0] + 1;
        }
        components = new int[numComps];
        int compIndex = 0;
        for (int i = 0; i < componentsRanges.length; i++) {
          for (int c = componentsRanges[i][0]; c <= componentsRanges[i][1]; c++) {
            components[compIndex++] = c;
          }
        }
        break;
      case 10: // -rl --resolutionLevel
        if (fsiz != null)
          throw new ParameterException("The resolution level (\"-rl\") parameter is not compatible with the frame size (\"-fs\") parameter");
        resolutionLevel = parseIntegerPositive(options);
        break;
      case 11: // -fs  --frameSize
        if (resolutionLevel >= 0)
          throw new ParameterException("The frame size (\"-fs\") parameter is not compatible with the resolution level (\"-rl\") parameter");
        fsiz = parseIntegerArray(options, 2);
        break;
      case 12: // -ro  --regionOffset
        roff = parseIntegerArray(options, 2);
        break;
      case 13: // -rs  --regionSize
        rsiz = parseIntegerArray(options, 2);
        break;
      case 14: // -ly  --layers
        layers = parseIntegerPositive(options);
        break;
      case 15: // -q  --quality
        quality = parseIntegerPositive(options);
        break;
      case 16: // -rd  --roundDirection
        round = parseIntegerPositive(options);
        break;
      case 17: // -ml  --maxTargetLength
        len = parseIntegerPositive(options);
        break;
      case 18: // -h  --help
        try {
          Properties cadiInfo = new Properties();
          InputStream cadiInfoURL = getClass().getClassLoader().getResourceAsStream("CADI/Common/Info/cadiInfo.properties");
          cadiInfo.load(cadiInfoURL);
          System.out.println("CADIClient version " + cadiInfo.getProperty("version") + "\n");
        } catch (Exception e) {
          System.out.println("PARAMETERS ERROR: error reading properties file.");
          System.out.println("Please report this error to: gici-dev@deic.uab.es");
        }
        showArgsInfo();
        //showArgsInfoLatexTable();
        System.exit(0);
        break;
      case 19: // -w  --warranty
        printWarranty();
        System.exit(0);
        break;
      case 20: // -l  --liability
        printLiability();
        System.exit(0);
        break;
      case 21: // -c  --copyright
        printCopyright();
        System.exit(0);
        break;
      default:
        assert (true);
    }
  }

  //ARGUMENTS GET FUNCTIONS
  public String getServer() {
    return server;
  }

  public int getPort() {
    return port;
  }

  public String getProxyServer() {
    return proxyServer;
  }

  public int getProxyPort() {
    return proxyPort;
  }

  public String getTarget() {
    return target;
  }

  public String getURI() {
    return uri;
  }

  public int[] getComponents() {
    return components;
  }

  public int getResolutionLevel() {
    return resolutionLevel;
  }

  public int[] getFrameSize() {
    return fsiz;
  }

  public int[] getRegionOffset() {
    return roff;
  }

  public int[] getRegionSize() {
    return rsiz;
  }

  public int getTargetLength() {
    return len;
  }

  public int getLayers() {
    return layers;
  }

  public int getQuality() {
    return quality;
  }

  public int getRound() {
    return round;
  }

  public String[] getImageFiles() {
    return (imageFiles);
  }

  public int[] getImageGeometry() {
    return (imageGeometry);
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
