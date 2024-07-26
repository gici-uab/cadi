/*
 * CADI Software - a JPIP Client/Server framework
 * Copyright (C) 2007-2012 Group on Interactive Coding of Images (GICI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package CADI.Common.LogicalTarget.JPEG2000.Codestream;

import CADI.Common.LogicalTarget.JPEG2000.JPCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.COCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.MCCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.MCTParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.QCCParameters;
import GiciStream.BufferedDataInputStream;
import GiciException.*;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * This class reads the JPEG2000 code-stream main header and decodes it. An
 * input stream object is passed to, and it reads the global information
 * neccesary to decompress the image.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.4 2011/03/13
 */
public class JPCMainHeaderDecoder {

  /**
   * Is an object with the input stream where the main header is read from.
   */
  private BufferedDataInputStream in = null;

  /**
   * Is an object where the main header are saved.
   */
  private JPCParameters jpcParameters = null;

  /**
   * Is the file pointer to the first byte of the main header.
   */
  private long mainHeaderInitialPos = 0;

  /**
   * Is the length of the main header.
   */
  private int mainHeaderLength = 0;

  // INTERNAL ATTRIBUTES
  /**
   *
   * Tells if precincts are defined within the headers or not (1 or 0).
   * <p>
   * Index in the array is component index
   */
  int[] definedPrecincts = null;

  /**
   * To know if a marker has been found.
   */
  boolean markerFound = false;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param in
   */
  public JPCMainHeaderDecoder(BufferedDataInputStream in) {
    // Check input parameters
    if (in == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.in = in;

    jpcParameters = new JPCParameters();
  }

  /**
   * Reads the J2C file headers and retrieves the parameters contained.
   *
   * @throws ErrorException when the header information is ambiguous or incorrect
   */
  public void run() throws ErrorException {
    boolean finish = false;

    markerFound = false;

    // Acquires the resource
    in.lock();

    try {
      readSOC();
      readSIZ();

      while (!finish) {
        //System.out.println("\nPos: "+in.getPos() +" -> " + Integer.toHexString((int)in.getPos()));

        while (!markerFound && !finish) {
          if (in.length() > 0) {
            int markFF = in.read();
            markerFound = (markFF == (int) 0xFF);
          } else {
            // It is necessary for decompressing the header at client side
            // because the SOT is not sent
            // >>> Posible solucion: leer primero el mainHeader y despues hacer una funcion que solo lo descodifique
            finish = true;
          }
        }

        if (!finish) {
          int marker = in.read();
          //System.out.println("MARKER FOUND 0xFF"+Integer.toHexString(marker));
          //markerFound = false;

          switch (marker) {
            case (int) 0x52://COD marker
              readCOD();
              break;
            case (int) 0x53://COC marker
              readCOC();
              break;
            case (int) 0x5C://QCD marker
              readQCD();
              break;
            case (int) 0x5D://QCC marker
              readQCC();
              break;
            case (int) 0x90://SOT marker
              finish = true;
              break;
            case (int) 0x5E://RGN marker
              throw new ErrorException("CADI cannot decode codestreams with a RGN marker.");
            case (int) 0x5F://POC marker
              throw new ErrorException("CADI cannot decode codestreams with a POC marker.");
            case (int) 0x55://TLM marker
              throw new ErrorException("CADI cannot decode codestreams with a TLM marker.");
            case (int) 0x57://PLM marker
              throw new ErrorException("CADI cannot decode codestreams with a PLM marker.");
            case (int) 0x58://PLT marker
              throw new ErrorException("PLT marker not is allowed in the main header.");
            case (int) 0x60://PPM marker
              throw new ErrorException("CADI cannot decode codestreams with a PPM marker.");
            case (int) 0x61://PPT marker
              throw new ErrorException("PPT marker not is allowed in the main header.");
            case (int) 0x63://CRG marker
              throw new ErrorException("CADI cannot decode codestreams with a CRG marker.");
            case (int) 0x64:// COM marker
              readCOM();
              break;
            case (int) 0x91:// SOP marker
              throw new ErrorException("SOP marker not is allowed in the main header.");
            case (int) 0x74: // MCT
              readMCT();
              break;
            case (int) 0x75: // MCC
              readMCC();
              break;
            case (int) 0x77: // MCO
              readMCO();
              break;
            case (int) 0x78: // CBD
              readCBD();
              break;
            default:
              //throw new ErrorException("Unknown marker.");
              //System.out.println("Unknown marker");
              markerFound = false;
              break;
          }
        }
      }

      mainHeaderLength = (int) (in.getPos() - mainHeaderInitialPos - 2); // Exclude the SOT Marker

    } catch (IOException e) {
      e.printStackTrace();
      throw new ErrorException("I/O error (" + e.toString() + ").");
    } finally {
      in.unlock();
    }

    // Check multi-component transform on components 0, 1, 2
    if (jpcParameters.codParameters.multiComponentTransform == 1) {
      int WTType_C0 = jpcParameters.cocParametersList.containsKey(0)
              ? jpcParameters.cocParametersList.get(0).WTLevels
              : jpcParameters.codParameters.WTLevels;
      int WTType_C1 = jpcParameters.cocParametersList.containsKey(1)
              ? jpcParameters.cocParametersList.get(1).WTLevels
              : jpcParameters.codParameters.WTLevels;
      int WTType_C2 = jpcParameters.cocParametersList.containsKey(2)
              ? jpcParameters.cocParametersList.get(2).WTLevels
              : jpcParameters.codParameters.WTLevels;

      if (!((WTType_C0 == WTType_C1) && (WTType_C0 == WTType_C2))) {
        throw new ErrorException("Component transformation on components 0, 1, and 2 requires the same DWT filter");
      }
    }
  }

  /**
   * Returns the first position of the main header first byte in the image
   * codestream.
   *
   * @return The position of the main header first byte.
   */
  public long getMainHeaderInitialPos() {
    return mainHeaderInitialPos;
  }

  /**
   * Returns the length of the main header.
   *
   * @return The length of the main header.
   */
  public int getMainHeaderLength() {
    return mainHeaderLength;
  }

  /**
   * This function returns the JPC codestream parameters.
   *
   * @return jpcParameters definition in {@link JPCParameters}
   */
  public JPCParameters getJPCParameters() {
    return jpcParameters;
  }

  // ============================ private methods ==============================
  /**
   * Reads the SOC marke and retrieves the parameters contained.
   *
   * @throws ErrorException when the SOC marker is not unique in the codestream
   */
  private void readSOC() throws ErrorException, IOException {
    if (in.readShort() != JPCMarkers.SOC) {
      in.seek(in.getPos() - 2); // Rewind 2 bytes the file pointer

      throw new ErrorException("Start Of Code Marker (SOC) has not been found");
    }
    //End of SOC marker
    mainHeaderInitialPos = in.getPos() - 2;
    markerFound = false;
  }

  /**
   * Reads the SIZ marker segment and retrieves the parameters contained.
   *
   * @throws ErrorException when the SIZ marker or its marker segment is ambiguous or incorrect
   */
  private void readSIZ() throws ErrorException, IOException {

    if (in.readShort() != JPCMarkers.SIZ) {
      in.seek(in.getPos() - 2);
      throw new ErrorException("SIZ Marker has not been found");
    }

    int LSIZ = in.readUnsignedShort();
    if ((LSIZ < 41) || (LSIZ > 49190)) {
      throw new ErrorException("LSIZ of marker SIZ is wrong.");
    }

    jpcParameters.sizParameters.Rsiz = in.readUnsignedShort();
    if (jpcParameters.sizParameters.Rsiz < 0) {
      throw new ErrorException("Rsiz parameter of SIZ marker segment must have value greater than 0.");
    }

    jpcParameters.sizParameters.xSize = in.readInt();
    if ((jpcParameters.sizParameters.xSize < 1) || (jpcParameters.sizParameters.xSize > (int) Math.pow(2, 32) - 1)) {
      throw new ErrorException("xSize is wrong.");
    }

    //SIZ marker segment's Ysiz		
    jpcParameters.sizParameters.ySize = in.readInt();
    if ((jpcParameters.sizParameters.ySize < 1) || (jpcParameters.sizParameters.ySize > (int) Math.pow(2, 32) - 1)) {
      throw new ErrorException("ySize is wrong.");
    }

    //SIZ marker segment's XOsiz
    jpcParameters.sizParameters.XOsize = in.readInt();
    if ((jpcParameters.sizParameters.XOsize < 0) || (jpcParameters.sizParameters.XOsize > (int) Math.pow(2, 32) - 2)) {
      throw new ErrorException("XOsize is wrong.");
    }

    //SIZ marker segment's YOsiz
    jpcParameters.sizParameters.YOsize = in.readInt();
    if ((jpcParameters.sizParameters.YOsize < 0) || (jpcParameters.sizParameters.YOsize > (int) Math.pow(2, 32) - 2)) {
      throw new ErrorException("YOsize is wrong.");
    }

    //SIZ marker segment's XTsiz
    jpcParameters.sizParameters.XTsize = in.readInt();
    if ((jpcParameters.sizParameters.XTsize < 1) || (jpcParameters.sizParameters.XTsize > (int) Math.pow(2, 32) - 1)) {
      throw new ErrorException("TileXSize is wrong.");
    }

    //SIZ marker segment's YTsiz
    jpcParameters.sizParameters.YTsize = in.readInt();
    if ((jpcParameters.sizParameters.YTsize < 1) || (jpcParameters.sizParameters.YTsize > (int) Math.pow(2, 32) - 1)) {
      throw new ErrorException("TileYSize is wrong.");
    }

    //SIZ marker segment's XTOsiz
    jpcParameters.sizParameters.XTOsize = in.readInt();
    if ((jpcParameters.sizParameters.XTOsize < 0) || (jpcParameters.sizParameters.XTOsize > (int) Math.pow(2, 32) - 2)) {
      throw new ErrorException("XTOsize is wrong.");
    }

    //SIZ marker segment's YTOsiz
    jpcParameters.sizParameters.YTOsize = in.readInt();
    if ((jpcParameters.sizParameters.YTOsize < 0) || (jpcParameters.sizParameters.YTOsize > (int) Math.pow(2, 32) - 2)) {
      throw new ErrorException("YTOsize is wrong.");
    }

    //SIZ marker segment's Csiz
    jpcParameters.sizParameters.zSize = in.readShort();
    if ((jpcParameters.sizParameters.zSize < 1) || (jpcParameters.sizParameters.zSize > 16384)) {
      throw new ErrorException("zSize is wrong.");
    }
    if (LSIZ != (38 + (3 * jpcParameters.sizParameters.zSize))) {
      throw new ErrorException("Wrong SIZ marker segment: Csiz is " + jpcParameters.sizParameters.zSize + "and Lsiz is " + LSIZ + ".");
    }

    //SIZ marker segment's Ssiz
    jpcParameters.sizParameters.signed = new boolean[jpcParameters.sizParameters.zSize];
    jpcParameters.sizParameters.precision = new int[jpcParameters.sizParameters.zSize];
    jpcParameters.sizParameters.XRsiz = new int[jpcParameters.sizParameters.zSize];;
    jpcParameters.sizParameters.YRsiz = new int[jpcParameters.sizParameters.zSize];;
    int tempByte = 0;
    for (int z = 0; z < jpcParameters.sizParameters.zSize; z++) {
      tempByte = in.read();
      jpcParameters.sizParameters.signed[z] = (tempByte & 0x80) != 0 ? true : false;
      jpcParameters.sizParameters.precision[z] = (tempByte & 0x7F) + 1;

      if ((jpcParameters.sizParameters.precision[z] - 1 < 1) || (jpcParameters.sizParameters.precision[z] - 1 > 38)) {
        throw new ErrorException("QComponentsBits is wrong.");
      }

      jpcParameters.sizParameters.XRsiz[z] = in.read();
      if ((jpcParameters.sizParameters.XRsiz[z] < 1) || (jpcParameters.sizParameters.XRsiz[z] > (int) Math.pow(2, 8) - 2)) {
        throw new ErrorException("XRsiz is wrong.");
      }

      jpcParameters.sizParameters.YRsiz[z] = in.read();
      if ((jpcParameters.sizParameters.YRsiz[z] < 1) || (jpcParameters.sizParameters.YRsiz[z] > (int) Math.pow(2, 8) - 2)) {
        throw new ErrorException("YRsiz is wrong.");
      }
    }

    //Initializes definedPrecincts
    definedPrecincts = new int[jpcParameters.sizParameters.zSize];
    for (int z = 0; z < jpcParameters.sizParameters.zSize; z++) {
      definedPrecincts[z] = -1;//value not set
    }
    //Initializes WTLevels
		/* jpcParameters.codParameters.WTLevels = new int[jpcParameters.sizParameters.zSize];
     * for (int z = 0; z < jpcParameters.sizParameters.zSize; z++){
     * jpcParameters.codParameters.WTLevels[z] = -1;//value not set
     * }
     * //Initializes WTTypes
     * jpcParameters.codParameters.WTTypes = new int[jpcParameters.sizParameters.zSize];
     * for (int z = 0; z < jpcParameters.sizParameters.zSize; z++){
     * jpcParameters.codParameters.WTTypes[z] = -1;//value not set
     * } */
    //Initializes BDBlock dimensions
		/* jpcParameters.codParameters.blockWidths = new int[jpcParameters.sizParameters.zSize];
     * jpcParameters.codParameters.blockHeights = new int[jpcParameters.sizParameters.zSize];
     * for (int z = 0; z < jpcParameters.sizParameters.zSize; z++){
     * jpcParameters.codParameters.blockWidths[z] = -1;//value not set
     * jpcParameters.codParameters.blockHeights[z] = -1;//value not set
     * } */
    //Initializes BDResolutionPrecinct dimensions partially
    //jpcParameters.codParameters.precinctWidths = new int[jpcParameters.sizParameters.zSize][];
    //jpcParameters.codParameters.precinctHeights = new int[jpcParameters.sizParameters.zSize][];

    //Initializes QTypes
		/* jpcParameters.qcdParameters.QTypes = new int[jpcParameters.sizParameters.zSize];
     * for (int z = 0; z < jpcParameters.sizParameters.zSize; z++){
     * jpcParameters.qcdParameters.QTypes[z] = -1;//value not set
     * } */
    //Initializes QExponents and QMantisas partially
    //jpcParameters.qcdParameters.QExponents = new int[jpcParameters.sizParameters.zSize][][];
    //jpcParameters.qcdParameters.QMantisas = new int[jpcParameters.sizParameters.zSize][][];
    //End of SIZ marker
    markerFound = false;
  }

  /**
   * Reads a COD marker segment and retrieves the parameters contained.
   *
   * @throws ErrorException when the COD marker segment is ambiguous or incorrect
   */
  private void readCOD() throws ErrorException, IOException {
    //COD marker segment's Lcod
    int LCOD = in.readShort();
    if ((LCOD < 12) || (LCOD > 45)) {
      throw new ErrorException("LCOD of marker COD is wrong.");
    }

    //COD marker segment's Scod
    int Scod = in.read();
    jpcParameters.codParameters.useEPH = (Scod & 0x04) == 0 ? false : true;
    //Start of packet header marker
    jpcParameters.codParameters.useSOP = (Scod & 0x02) == 0 ? false : true;
    //Precincts defined below
    definedPrecincts[0] = (Scod & 0x01);

    // COD marker segment's SGcod
    //Progression Order
    jpcParameters.codParameters.progressionOrder = in.read();
    if ((jpcParameters.codParameters.progressionOrder < 0) || (jpcParameters.codParameters.progressionOrder > 4)) {
      throw new ErrorException("FWProgressionOrder is wrong.");
    }

    // Number of layers
    jpcParameters.codParameters.numLayers = in.readShort();
    if ((jpcParameters.codParameters.numLayers < 1) || (jpcParameters.codParameters.numLayers > (int) Math.pow(2, 16) - 1)) {
      throw new ErrorException("LCAchievedNumLayers is wrong.");
    }

    // Multiple component transformation
    jpcParameters.codParameters.multiComponentTransform = in.read();
    if (jpcParameters.codParameters.multiComponentTransform > 4) {
      throw new ErrorException("Multiple component transformation type does not exist.");
    } else {
      if (jpcParameters.codParameters.multiComponentTransform == 1) {
        if (jpcParameters.sizParameters.zSize < 3) {
          throw new ErrorException("Colour transform cannot have been applied to only " + jpcParameters.sizParameters.zSize + " component(s).");
        }
      }
    }

    // COD marker segment's SPcod
    int WTLevels = in.read();
    if ((WTLevels < 0) || (WTLevels > 32)) {
      throw new ErrorException("WTLevels is wrong.");
    }
    jpcParameters.codParameters.WTLevels = WTLevels;
    for (int z = 0; z < jpcParameters.sizParameters.zSize; z++) {
      //if (jpcParameters.codParameters.WTLevels[z] < 0){//Does not set the value if it was already set by a COC marker
      //jpcParameters.codParameters.WTLevels[z] = WTlevels;
      //Finishes the initialization of BDResolutionPrecinct dimensions for those defined by a COD marker
      jpcParameters.codParameters.precinctWidths = new int[WTLevels + 1];
      jpcParameters.codParameters.precinctHeights = new int[WTLevels + 1];
      for (int rLevel = 0; rLevel < (WTLevels + 1); rLevel++) {
        jpcParameters.codParameters.precinctWidths[rLevel] = -1;
        jpcParameters.codParameters.precinctHeights[rLevel] = -1;
      }
      //Finishes the initialization of QExponents and Qmantisas for those defined by a QCD marker
      jpcParameters.qcdParameters.exponents = new int[WTLevels + 1][];
      jpcParameters.qcdParameters.mantisas = new int[WTLevels + 1][];
      jpcParameters.qcdParameters.exponents[0] = new int[1];
      jpcParameters.qcdParameters.mantisas[0] = new int[1];
      jpcParameters.qcdParameters.exponents[0][0] = -1;
      jpcParameters.qcdParameters.mantisas[0][0] = -1;
      for (int rLevel = 1; rLevel < (WTLevels + 1); rLevel++) {
        jpcParameters.qcdParameters.exponents[rLevel] = new int[3];
        jpcParameters.qcdParameters.mantisas[rLevel] = new int[3];
        for (int subband = 0; subband < 3; subband++) {
          jpcParameters.qcdParameters.exponents[rLevel][subband] = -1;
          jpcParameters.qcdParameters.mantisas[rLevel][subband] = -1;
        }
      }
      //}
    }

    // Code-block width and height
    int blockWidth = in.read() + 2;
    int blockHeight = in.read() + 2;
    if ((blockWidth < 2) || (blockWidth > 10)
            || (blockHeight < 2) || (blockHeight > 10)
            || ((blockWidth + blockHeight) > 12)) {
      throw new ErrorException("Wrong block sizes.");
    }
    jpcParameters.codParameters.blockWidth = blockWidth;
    jpcParameters.codParameters.blockHeight = blockHeight;


    // Code-block style (flags MQ)
    int codeBlockStyle = in.read();
    jpcParameters.codParameters.bypass = (codeBlockStyle & (1 << 0)) == 0 ? false : true;
    jpcParameters.codParameters.reset = (codeBlockStyle & (1 << 1)) == 0 ? false : true;
    jpcParameters.codParameters.restart = (codeBlockStyle & (1 << 2)) == 0 ? false : true;
    jpcParameters.codParameters.causal = (codeBlockStyle & (1 << 3)) == 0 ? false : true;
    jpcParameters.codParameters.erterm = (codeBlockStyle & (1 << 4)) == 0 ? false : true;
    jpcParameters.codParameters.segmark = (codeBlockStyle & (1 << 5)) == 0 ? false : true;

    if ((jpcParameters.codParameters.bypass)
            || (jpcParameters.codParameters.reset)
            || (jpcParameters.codParameters.causal)
            || (jpcParameters.codParameters.erterm)
            || (jpcParameters.codParameters.segmark)) {
      throw new ErrorException("CADI can only work with the RESTART"
              + " mode active an the others desactivated.");
    }


    // Wavelet transformation type
    switch ((int) in.read()) {
      case 0:
        jpcParameters.codParameters.WTType = 2;
        break;
      case 1:
        jpcParameters.codParameters.WTType = 1;
        break;
      default:
        throw new ErrorException("WTTypes is wrong.");
    }


    // Precinct sizes (user-defined)
    if (definedPrecincts[0] == 1) {
      int widthExp, heightExp;
      for (int rLevel = 0; rLevel < (WTLevels + 1); rLevel++) {
        int precinctSize = in.read();
        heightExp = (precinctSize & 0xF0) >>> 4;
        if ((heightExp < 0) && (heightExp > Math.pow(2, 4) - 1)) {
          throw new ErrorException("Precinct Heights is wrong.");
        }
        widthExp = precinctSize & 0x0F;
        if ((widthExp < 0) && (widthExp > Math.pow(2, 4) - 1)) {
          throw new ErrorException("Precinct Widths is wrong.");
        }
        jpcParameters.codParameters.precinctHeights[rLevel] = heightExp; // - (rLevel == 0 ? 0: 1);
        jpcParameters.codParameters.precinctWidths[rLevel] = widthExp; // - (rLevel == 0 ? 0: 1);
      }
    } else {//maximum precincts
      for (int rLevel = 0; rLevel < WTLevels + 1; rLevel++) {
        jpcParameters.codParameters.precinctWidths[rLevel] = 15; // - (rLevel == 0 ? 0: 1);
        jpcParameters.codParameters.precinctHeights[rLevel] = 15; // - (rLevel == 0 ? 0: 1);
      }
    }

    //End of COD marker
    markerFound = false;
  }

  /**
   * Reads a COD/COC marker segment and retrieves the parameters contained.
   *
   *
   * @throws ErrorException when the COD/COC marker segment is ambiguous or incorrect
   */
  private void readCOC() throws ErrorException, IOException {

    //COC marker segment's Lcoc
    int LCOC;
    LCOC = in.readShort();
    if ((LCOC < 12) || (LCOC > 43)) {
      throw new ErrorException("LCOC of marker COC is wrong." + LCOC);
    }

    //COC marker segment's Ccoc
    int CCOC;
    if (jpcParameters.sizParameters.zSize < 257) {
      CCOC = in.read();
    } else {
      CCOC = in.readShort();
    }
    if (CCOC > 16383) {
      throw new ErrorException("Ccoc in COC marker segment must be lower than " + (16383 + 1) + " and is " + CCOC + ".");
    }
    //COC marker segment's Scoc
    int Scoc = in.read();

    if (jpcParameters.cocParametersList.containsKey(CCOC)) {
      throw new ErrorException("Only one COC marker per component is allowed");
    }

    COCParameters cocParameters = new COCParameters();


    //COC marker segment's SPcoc
    int WTlevels = in.read();
    if ((WTlevels < 0) || (WTlevels > 32)) {
      throw new ErrorException("WTLevels is wrong.");
    }
    cocParameters.WTLevels = WTlevels;

    //Initializes QExponents and QMantisas for this component
    jpcParameters.qcdParameters.exponents = new int[WTlevels + 1][];
    jpcParameters.qcdParameters.mantisas = new int[WTlevels + 1][];
    jpcParameters.qcdParameters.exponents[0] = new int[1];
    jpcParameters.qcdParameters.mantisas[0] = new int[1];
    jpcParameters.qcdParameters.exponents[0][0] = -1;
    jpcParameters.qcdParameters.mantisas[0][0] = -1;
    for (int rLevel = 1; rLevel < (WTlevels + 1); rLevel++) {
      jpcParameters.qcdParameters.exponents[rLevel] = new int[3];
      jpcParameters.qcdParameters.mantisas[rLevel] = new int[3];
      for (int subband = 0; subband < 3; subband++) {
        jpcParameters.qcdParameters.exponents[rLevel][subband] = -1;
        jpcParameters.qcdParameters.mantisas[rLevel][subband] = -1;
      }
    }

    int blockWidth = in.read() + 2;
    int blockHeight = in.read() + 2;

    // Code-block width and height
    if ((blockWidth < 2) || (blockWidth > 10)
            || (blockHeight < 2) || (blockHeight > 10)
            || ((blockWidth + blockHeight) > 12)) {
      throw new ErrorException("Wrong block sizes.");
    }

    cocParameters.blockWidth = blockWidth;
    cocParameters.blockHeight = blockHeight;

    // Code-block style (flags MQ)
    int codeBlockStyle = in.read();
    cocParameters.bypass = (codeBlockStyle & (1 << 0)) == 0 ? false : true;
    cocParameters.reset = (codeBlockStyle & (1 << 1)) == 0 ? false : true;
    cocParameters.restart = (codeBlockStyle & (1 << 2)) == 0 ? false : true;
    cocParameters.causal = (codeBlockStyle & (1 << 3)) == 0 ? false : true;
    cocParameters.erterm = (codeBlockStyle & (1 << 4)) == 0 ? false : true;
    cocParameters.segmark = (codeBlockStyle & (1 << 5)) == 0 ? false : true;

    if ((cocParameters.bypass)
            || (cocParameters.reset)
            || (cocParameters.causal)
            || (cocParameters.erterm)
            || (cocParameters.segmark)) {
      throw new ErrorException("CADI can only work with the RESTART"
              + " mode active an the others desactivated.");
    }


    // Wavelet transformation type
    switch ((int) in.read()) {
      case 0:
        cocParameters.WTType = 2;
        break;
      case 1:
        cocParameters.WTType = 1;
        break;
      default:
        throw new ErrorException("WTTypes is wrong.");
    }

    // Precinct sizes (user-defined)
    cocParameters.precinctWidths = new int[WTlevels + 1];
    cocParameters.precinctHeights = new int[WTlevels + 1];
    if (Scoc == 1) {
      int widthExp, heightExp;
      for (int rLevel = 0; rLevel < (WTlevels + 1); rLevel++) {
        int precinctSize = in.read();
        heightExp = (precinctSize & 0xF0) >>> 4;
        if ((heightExp < 0) && (heightExp > Math.pow(2, 4) - 1)) {
          throw new ErrorException("Precinct heights is wrong.");
        }
        widthExp = precinctSize & 0x0F;
        if ((widthExp < 0) && (widthExp > Math.pow(2, 4) - 1)) {
          throw new ErrorException("Precinct widths is wrong.");
        }
        cocParameters.precinctHeights[rLevel] = heightExp; // - (rLevel == 0 ? 0: 1);
        cocParameters.precinctWidths[rLevel] = widthExp; // - (rLevel == 0 ? 0: 1);
      }
    } else {//maximum precincts
      for (int rLevel = 0; rLevel < (WTlevels + 1); rLevel++) {
        cocParameters.precinctWidths[rLevel] = 15; // - (rLevel == 0 ? 0: 1);
        cocParameters.precinctHeights[rLevel] = 15; // - (rLevel == 0 ? 0: 1);
      }
    }

    // Adds the COC parameters to the list
    jpcParameters.cocParametersList.put(CCOC, cocParameters);

    //End of COC marker
    markerFound = false;
  }

  /**
   * Reads a QCD/QCC marker segment and retrieves the parameters contained.
   *
   * @throws ErrorException when the QCD/QCC marker segment is ambiguous or incorrect
   */
  private void readQCD() throws ErrorException, IOException {

    //QCD marker segment's Lqcd
    int LQCD = in.readUnsignedShort();
    if ((LQCD < 4) || (LQCD > 197)) {
      throw new ErrorException("LQCD of marker QCD is wrong.");
    }

    if (LQCD == 5) {  // scalar quantization derived
      jpcParameters.qcdParameters.quantizationStyle = 1;

    } else { // no quantization
      if (LQCD == 4 + (3 * jpcParameters.codParameters.WTLevels)) {
        jpcParameters.qcdParameters.quantizationStyle = 0;

      } else {
        if (LQCD == 5 + (6 * jpcParameters.codParameters.WTLevels)) {
          jpcParameters.qcdParameters.quantizationStyle = 2;

        } else {
          throw new ErrorException("Lqcd parameter from QCD marker segment has a wrong value of " + LQCD + ".");
        }
      }
    }

    int tempSqcd = in.read();
    jpcParameters.qcdParameters.guardBits = (tempSqcd >>> 5) & 0x07;
    if (jpcParameters.qcdParameters.guardBits > (Math.pow(2, 3) - 1)) {
      throw new ErrorException("QGuardBits of Component 0 is wrong");
    }
    int Sqcd = tempSqcd & 0x1F;
    if ((Sqcd == 0) && (jpcParameters.qcdParameters.quantizationStyle != 0)) {
      throw new ErrorException("Sqcd parameter tells QType 0 whilst Lqcd tells QType " + jpcParameters.qcdParameters.quantizationStyle + ".");
    }
    if ((Sqcd == 1) && (jpcParameters.qcdParameters.quantizationStyle != 1)) {
      throw new ErrorException("Sqcd parameter tells QType 1 whilst Lqcd tells QType " + jpcParameters.qcdParameters.quantizationStyle + ".");
    }
    if ((Sqcd == 2) && (jpcParameters.qcdParameters.quantizationStyle != 2)) {
      throw new ErrorException("Sqcd parameter tells QType 2 whilst Lqcd tells QType " + jpcParameters.qcdParameters.quantizationStyle + ".");
    }
    if (Sqcd > 2) {
      throw new ErrorException("QType " + Sqcd + " unrecognized in Sqcd parameter.");
    }
    //QCD marker segment's SPqcd
    int exponent;
    int mantisa;
    switch (jpcParameters.qcdParameters.quantizationStyle) {
      case 0://no quantization
        exponent = (in.read() & 0xF8) >>> 3;
        for (int z = 0; z < jpcParameters.sizParameters.zSize; z++) {
          //Does not set the value if it was already set by a QCC marker
          if (jpcParameters.qcdParameters.exponents[0][0] < 0) {
            jpcParameters.qcdParameters.exponents[0][0] = exponent;
            jpcParameters.qcdParameters.mantisas[0][0] = 0;
          }
        }

        for (int rLevel = 1; rLevel < (jpcParameters.codParameters.WTLevels + 1); rLevel++) {
          for (int subband = 0; subband < 3; subband++) {
            exponent = (in.read() & 0xF8) >>> 3;
            for (int z = 0; z < jpcParameters.sizParameters.zSize; z++) {
              //Does not set the value if it was already set by a QCC marker
              if (jpcParameters.qcdParameters.exponents[rLevel][subband] < 0) {
                jpcParameters.qcdParameters.exponents[rLevel][subband] = exponent;
                if ((jpcParameters.qcdParameters.exponents[rLevel][subband] < 0)
                        || (jpcParameters.qcdParameters.exponents[rLevel][subband] > Math.pow(2, 5) - 1)) {
                  throw new ErrorException("Invalid exponent.");
                }
                jpcParameters.qcdParameters.mantisas[rLevel][subband] = 0;
              }
            }
          }
        }
        break;

      case 1://scalar quantization derived
        int SPqcd = in.readShort();
        exponent = (SPqcd & 0xF800) >>> 11;
        mantisa = SPqcd & 0x007FF;
        for (int z = 0; z < jpcParameters.sizParameters.zSize; z++) {
          jpcParameters.qcdParameters.exponents[0][0] = exponent;//LL
          jpcParameters.qcdParameters.mantisas[0][0] = mantisa;
          if ((jpcParameters.qcdParameters.exponents[0][0] < 0)
                  || (jpcParameters.qcdParameters.exponents[0][0] > Math.pow(2, 5) - 1)
                  || (jpcParameters.qcdParameters.mantisas[0][0] < 0)
                  || jpcParameters.qcdParameters.mantisas[0][0] > Math.pow(2, 11) - 1) {
            throw new ErrorException("Invalid exponent or Mantisa.");
          }
          //rLevel = 1
          for (int subband = 0; subband < 3; subband++) {
            if (jpcParameters.codParameters.WTLevels >= 1) {
              jpcParameters.qcdParameters.exponents[1][subband] = jpcParameters.qcdParameters.exponents[0][0];
              jpcParameters.qcdParameters.mantisas[1][subband] = jpcParameters.qcdParameters.mantisas[0][0];
            }
            //other rLevels
            if (jpcParameters.codParameters.WTLevels >= 2) {
              for (int rLevel = 2; rLevel < (jpcParameters.codParameters.WTLevels + 1); rLevel++) {
                jpcParameters.qcdParameters.exponents[rLevel][subband] = jpcParameters.qcdParameters.exponents[rLevel - 1][subband] - 1;
                jpcParameters.qcdParameters.mantisas[rLevel][subband] = jpcParameters.qcdParameters.mantisas[0][0];
              }
            }
          }
        }
        break;

      case 2://scalar quantization expounded
        for (int rLevel = 0; rLevel <= jpcParameters.codParameters.WTLevels; rLevel++) {
          for (int subband = 0; subband < jpcParameters.qcdParameters.exponents[rLevel].length; subband++) {
            SPqcd = in.readShort();
            jpcParameters.qcdParameters.exponents[rLevel][subband] = (SPqcd & 0xF800) >>> 11;
            jpcParameters.qcdParameters.mantisas[rLevel][subband] = SPqcd & 0x007FF;
            if ((jpcParameters.qcdParameters.exponents[rLevel][subband] < 0)
                    || (jpcParameters.qcdParameters.exponents[rLevel][subband] > Math.pow(2, 5) - 1)
                    || (jpcParameters.qcdParameters.mantisas[rLevel][subband] < 0)
                    || jpcParameters.qcdParameters.mantisas[rLevel][subband] > Math.pow(2, 11) - 1) {
              throw new ErrorException("Invalid exponent or Mantisa.");
            }

            for (int z = 1; z < jpcParameters.sizParameters.zSize; z++) {
              for (int rLevel_copy = 0; rLevel_copy <= jpcParameters.codParameters.WTLevels; rLevel_copy++) {
                for (int subband_copy = 0; subband_copy < jpcParameters.qcdParameters.exponents[rLevel_copy].length; subband_copy++) {
                  jpcParameters.qcdParameters.exponents[rLevel_copy][subband_copy] = jpcParameters.qcdParameters.exponents[rLevel_copy][subband_copy];
                  jpcParameters.qcdParameters.mantisas[rLevel_copy][subband_copy] = jpcParameters.qcdParameters.mantisas[rLevel_copy][subband_copy];
                }
              }
            }
          }
        }

        break;
    }
    markerFound = false;
  }

  /**
   * Reads a QCD/QCC marker segment and retrieves the parameters contained.
   *
   * @throws ErrorException when the QCD/QCC marker segment is ambiguous or incorrect
   */
  private void readQCC() throws ErrorException, IOException {
    //QCC marker segment's Lqcd
    int LQCC = in.readShort();
    if ((LQCC < 5) || (LQCC > 199)) {
      throw new ErrorException("LQCC of marker QCC is wrong.");
    }

    //QCC marker segment's Cqcc
    int CQCC;
    if (jpcParameters.sizParameters.zSize < 257) {
      CQCC = in.read();
    } else {
      CQCC = in.readShort();
    }

    if (CQCC > 16383) {
      throw new ErrorException(CQCC + " components are not allowed (maximum 16383).");
    }

    if (jpcParameters.cocParametersList.containsKey(CQCC)) {
      throw new ErrorException("Only one COC marker per component is allowed");
    }

    QCCParameters qccParameters = new QCCParameters();

    int Sqcc = in.read();
    qccParameters.guardBits = (Sqcc & 0xE0) >>> 5;
    //QGuardBits = readInteger(QGuardBits_BITS);
    if (qccParameters.guardBits > (Math.pow(2, 3) - 1)) {
      throw new ErrorException("QGuardBits of Component " + CQCC + " is wrong");
    }
    qccParameters.quantizationStyle = Sqcc & 0x1F;
    //QTypes[CQCC] = readInteger(QTypes_BITS);
    if (qccParameters.quantizationStyle > 2) {
      throw new ErrorException("QType " + (qccParameters.quantizationStyle)
              + " unrecognized in Sqcc parameter for component "
              + CQCC + ".");
    }

    int WTLevels = jpcParameters.cocParametersList.containsKey(CQCC)
            ? jpcParameters.cocParametersList.get(CQCC).WTLevels
            : jpcParameters.codParameters.WTLevels;

    //QCC marker segment's SPqcd
    int exponent;
    int mantisa;
    switch (qccParameters.quantizationStyle) {
      case 0://no quantization
        for (int rLevel = 0; rLevel < WTLevels + 1; rLevel++) {
          for (int subband = 0; subband < qccParameters.exponents[rLevel].length; subband++) {
            qccParameters.exponents[rLevel][subband] = in.read() >>> 3;
            if ((qccParameters.exponents[rLevel][subband] < 0)
                    || (qccParameters.exponents[rLevel][subband] > Math.pow(2, 5) - 1)) {
              throw new ErrorException("Invalid exponent.");
            }
            qccParameters.mantisas[rLevel][subband] = 0;
          }
        }
        break;
      case 1://scalar quantization derived
        int SPqcd = in.readShort();
        exponent = SPqcd >>> 11;
        mantisa = SPqcd & 0x7FF;
        //exponent = readInteger(QExponents_BITS);
        //mantisa  = readInteger(QMantisas_BITS);

        qccParameters.exponents[0][0] = exponent;
        qccParameters.mantisas[0][0] = mantisa;
        if ((qccParameters.exponents[0][0] < 0)
                || (qccParameters.exponents[0][0] > Math.pow(2, 5) - 1)
                || (qccParameters.mantisas[0][0] < 0)
                || qccParameters.mantisas[0][0] > Math.pow(2, 11) - 1) {
          throw new ErrorException("Invalid exponent or Mantisa.");
        }
        //rLevel = 1
        for (int subband = 0; subband < 3; subband++) {
          if (WTLevels >= 1) {
            qccParameters.exponents[1][subband] = qccParameters.exponents[0][0];
            qccParameters.mantisas[1][subband] = qccParameters.mantisas[0][0];
          }
          //other rLevels
          if (WTLevels >= 2) {
            for (int rLevel = 2; rLevel < (WTLevels + 1); rLevel++) {
              qccParameters.exponents[rLevel][subband] = qccParameters.exponents[rLevel - 1][subband] - 1;
              qccParameters.mantisas[rLevel][subband] = qccParameters.mantisas[0][0];
            }
          }
        }
        break;
      case 2://scalar quantization expounded
        for (int rLevel = 0; rLevel <= (WTLevels); rLevel++) {
          for (int subband = 0; subband < qccParameters.mantisas[rLevel].length; subband++) {
            SPqcd = in.readShort();
            qccParameters.exponents[rLevel][subband] = SPqcd >>> 11;
            qccParameters.mantisas[rLevel][subband] = SPqcd & 0x7FF;
            if ((qccParameters.exponents[rLevel][subband] < 0)
                    || (qccParameters.exponents[rLevel][subband] > Math.pow(2, 5) - 1)
                    || (qccParameters.mantisas[rLevel][subband] < 0)
                    || qccParameters.mantisas[rLevel][subband] > Math.pow(2, 11) - 1) {
              throw new ErrorException("Invalid exponent or Mantisa.");
            }
          }
        }
        break;
    }

    // Adds the QCC parameters to the list
    jpcParameters.qccParametersList.put(CQCC, qccParameters);

    markerFound = false;
  }

  /**
   * Reads a COM marker segment and retrieves the parameters contained.
   *
   * @throws ErrorException
   * @throws IOException
   */
  public void readCOM() throws ErrorException, IOException {

    // Read lcom
    int Lcom = in.readUnsignedShort();
    if (Lcom < 5) {
      throw new ErrorException("Lcom value in COM Marker must be"
              + " greater than 5.");
    }

    // Read Rcom
    int Rcom = in.readUnsignedShort();

    // Get Ccom's pointer
    long iniPos = in.getPos();


    String boiComment = "BOI Slopes-Lengths v1.0:";
    String kduLayerInfo =
            "Kdu-Layer-Info: log_2{Delta-D(MSE)/[2^16*Delta-L(bytes)]}, L(bytes)";
    String predModelInfo = "Predictive-Model v1.0:";

    // Read info comment
    String infoString = in.readLine();

    if (infoString.equals(boiComment)) {
      jpcParameters.comParameters.boiSlopes =
              new int[jpcParameters.codParameters.numLayers];
      jpcParameters.comParameters.boiLengths =
              new long[jpcParameters.codParameters.numLayers];

      int index = 0;
      while (in.getPos() <= (iniPos + Lcom - 4)) {
        // BOI stores the layer info as: slope,length;
        String[] line = in.readLine().split(",");

        jpcParameters.comParameters.boiSlopes[index] = Integer.parseInt(line[0]);
        jpcParameters.comParameters.boiLengths[index] = Long.parseLong(line[1]);
        index++;
      }

    } else if (infoString.equals(kduLayerInfo)) {
      // NOTE: this information has been taken from the Kakadu v6.0
      // Kakadu records the layer info as:
      // log_2{Delta-D(MSE)/[2^16*Delta-L(bytes)]} and L(bytes)
      // The log_2 information is recorded as a float with 6 chars (1 for the
      // decimal part) and the L information with 8 chars (1 for decimals) in
      // scientific notation.

      jpcParameters.comParameters.kduLayerLogSlopes =
              new int[jpcParameters.codParameters.numLayers];
      jpcParameters.comParameters.kduLengths =
              new long[jpcParameters.codParameters.numLayers];
      int index = 0;
      Pattern linePattern = Pattern.compile("[eE]");

      while (in.getPos() < (iniPos + Lcom - 4)) {
        // Read R-D & length
        String[] line = in.readLine().split(",");

        // Parse R-D
        jpcParameters.comParameters.kduLayerLogSlopes[index] =
                (int) Math.floor(Float.parseFloat(line[0]) * 256 + 0.5);

        // Parse length
        String[] sValues = linePattern.split(line[1]);
        //String mantisa = line[1].substring(0, line[1].indexOf('e'));
        //String exponente = line[1].substring(line[1].indexOf('e')+1);
        jpcParameters.comParameters.kduLengths[index] =
                (long) Math.floor((Float.parseFloat(sValues[0])
                * Math.pow(10.0, Float.parseFloat(sValues[1]))) + 0.5);

        if (index > 0) {
          // All R-D values decrease monotonically
          if ((jpcParameters.comParameters.kduLayerLogSlopes[index]
                  >= jpcParameters.comParameters.kduLayerLogSlopes[index - 1])
                  || (jpcParameters.comParameters.kduLengths[index]
                  <= jpcParameters.comParameters.kduLengths[index - 1])) {
            jpcParameters.comParameters.kduLayerLogSlopes = null;
            jpcParameters.comParameters.kduLengths = null;
            break;
          }
        }
        index++;
      }
    } else if (infoString.equals(predModelInfo)) {

      jpcParameters.comParameters.predictiveModel = new HashMap<Long, Float>();

      String line = in.readLine();

      int numValues = -1;
      try {
        numValues = Integer.parseInt(line);
        for (int i = 0; i < numValues; i++) {
          line = in.readLine();
          String[] sLine = line.split(",");
          long key = Long.parseLong(sLine[0]);
          float value = Float.parseFloat(sLine[1]);
          jpcParameters.comParameters.predictiveModel.put(key, value);
        }
      } catch (NumberFormatException nfe) {
        throw new ErrorException("Malformed COM marker.");
      }

    } else {
      // Unknown comment; then I skip it
      in.seek(iniPos);
      in.skipBytes(Lcom - 4);
    }

    markerFound = false;
  }

  /**
   *
   * @throws ErrorException
   * @throws IOException
   */
  private void readCBD() throws ErrorException, IOException {

    if ((jpcParameters.codParameters.multiComponentTransform & 0x0100) <= 0) {
      throw new ErrorException("CBD marker is only allowed with multiple component transform capabilityin the Rsiz parameter");
    }

    // Lcbd (length of marker segment)
    int Lcbd = in.readUnsignedShort();

    // Ncbd
    int Ncbd = in.readUnsignedShort();
    boolean sameAllComponents = ((Ncbd & 0x8000) > 0) ? true : false;
    Ncbd &= 0x7FFF;

    // Check parameters
    if (Ncbd > 16384) {
      throw new ErrorException("Maximum number of reconstructed image components is greater than 16384");
    }
    if (sameAllComponents && (Lcbd != 5)) {
      throw new ErrorException("Wrong length in CBD marker");
    }
    if (!sameAllComponents && (Lcbd != (4 + Ncbd))) {
      throw new ErrorException("Wrong length in CBD marker");
    }

    jpcParameters.cbdParameters.MCTPrecision = new int[Ncbd];
    jpcParameters.cbdParameters.MCTSigned = new boolean[Ncbd];

    // DBcbd
    if (sameAllComponents) {
      int DBcbd = in.read();
      if ((DBcbd & 0x007F) > 38) {
        throw new ErrorException("Wrong precision value. Allowed values from 1 bit deep to 38 bits deep");
      }
      Arrays.fill(jpcParameters.cbdParameters.MCTPrecision, (DBcbd & 0x007F) + 1);
      Arrays.fill(jpcParameters.cbdParameters.MCTSigned, ((DBcbd & 0x0080) > 0) ? true : false);
    } else {
      for (int i = 0; i < Ncbd; i++) {
        int DBcbd = in.read();
        if ((DBcbd & 0x007F) > 38) {
          throw new ErrorException("Wrong precision value. Allowed values from 1 bit deep to 38 bits deep");
        }
        Arrays.fill(jpcParameters.cbdParameters.MCTPrecision, (DBcbd & 0x007F) + 1);
        Arrays.fill(jpcParameters.cbdParameters.MCTSigned, ((DBcbd & 0x0080) > 0) ? true : false);
      }
    }

    markerFound = false;
  }

  /**
   *
   * @throws ErrorException
   * @throws IOException
   */
  private void readMCT() throws ErrorException, IOException {

    if ((jpcParameters.sizParameters.Rsiz & 0x0100) <= 0) {
      throw new ErrorException("MCT marker is only allowed with multiple component transform capabilityin the Rsiz parameter");
    }

    // Read Lmct
    int Lmct = in.readUnsignedShort();

    // Read Zmct
    int Zmct = in.readUnsignedShort();
    // Check for marker splitted in series (unsupported option)
    if (Zmct != 0) {
      throw new ErrorException("CADI does not support multiple component transform markers splitted into more than one serie");
    }

    // Read Imct
    int Imct = in.readUnsignedShort();
    int index = Imct & 0x00FF;
    int arrayType = (Imct >>> 8) & 0x3; // 0 - dependency; 1 - decorrelation; 2 - offset
    int arrayElementsType = (Imct >>> 10) & 0x3;
    // Check read values
    if (index == 0) {
      throw new ErrorException("Wrong index in MCT marker");
    }
    if (arrayType == 3) {
      throw new ErrorException("Wrong array type in MCT marker");
    }


    MCTParameters mctParameters = jpcParameters.mctParametersList.get(index);
    if (mctParameters == null) {
      mctParameters = new MCTParameters(index);
      jpcParameters.mctParametersList.put(index, mctParameters);
    }

    if (Zmct != mctParameters.Zmct++) {
      throw new ErrorException("Wrong index in MCT marker");
    }

    // Read Ymct
    mctParameters.Ymct = (Zmct == 0) ? in.readUnsignedShort() : -1;


    // NOT FINISHED YET !!!!!!!!!!!!!!!!!!
    if (true) {
      System.exit(0);
    }

    // Read SPmct
    int numParamters = Lmct - 2 - 2 - (Zmct == 0 ? 2 : 0);



    markerFound = false;
  }

  /**
   * Reads the MCC marker.
   *
   * @throws ErrorException
   * @throws IOException
   */
  private void readMCC() throws ErrorException, IOException {

    if ((jpcParameters.sizParameters.Rsiz & 0x0100) <= 0) {
      throw new ErrorException("MCO marker is only allowed with multiple component transform capabilityin the Rsiz parameter");
    }

    // Read Lmcc
    int Lmcc = in.readUnsignedShort();
    if (Lmcc < 5) {
      throw new ErrorException("Wrong length in MCC marker.");
    }

    // Read Zmcc
    int Zmcc = in.readUnsignedShort();
    // Check for marker splitted in series (unsupported option)
    if (Zmcc != 0) {
      throw new ErrorException("CADI does not support multiple component transform collection splitted into more than one serie");
    }

    // Read Imcc
    int Imcc = in.readUnsignedByte();
    if (Imcc == 0) {
      throw new ErrorException("Wrong index in MCC marker");
    }

    MCCParameters mccParameters = jpcParameters.mccParametersList.get(Imcc);
    if (mccParameters == null) {
      mccParameters = new MCCParameters(Imcc);
      jpcParameters.mccParametersList.put(Imcc, mccParameters);
    }

    if (Zmcc != mccParameters.Zmcc++) {
      throw new ErrorException("Wrong index in MCC marker");
    }

    // Read Ymcc
    mccParameters.Ymcc = (Zmcc == 0) ? in.readUnsignedShort() : 0;
    // Check for marker splitted in series (unsupported option)
    if (mccParameters.Ymcc != 0) {
      throw new ErrorException("CADI does not support multiple component transform collection splitted into more than one serie");
    }

    // Read Qmcc
    mccParameters.Qmcc = (Zmcc == 0) ? in.readUnsignedShort() : 0;
    if (mccParameters.Qmcc != 1) {
      throw new ErrorException("CADI does not support multiple component transform collections");
    }

    // Memory allocations
    mccParameters.MCTType = new int[mccParameters.Qmcc];
    mccParameters.inputIntermediateComponents = new int[mccParameters.Qmcc][];
    mccParameters.outputIntermediateComponents = new int[mccParameters.Qmcc][];
    mccParameters.DWTType = new int[mccParameters.Qmcc];
    mccParameters.DWTLevels = new int[mccParameters.Qmcc];
    mccParameters.indexMCTOffsets = new int[mccParameters.Qmcc];
    mccParameters.componentOffsets = new int[mccParameters.Qmcc];

    // Loop on component collections
    for (int cc = 0; cc < mccParameters.Qmcc; cc++) {

      // Read Xmcc	
      mccParameters.MCTType[cc] = in.read() & 0x03;
      if ((mccParameters.MCTType[cc] > 3) || (mccParameters.MCTType[cc] == 2)) {
        throw new ErrorException();
      }
      // Check for marker splitted in series (unsupported option)
      if (mccParameters.MCTType[cc] != 3) {
        throw new ErrorException("CADI only supports multiple component transform by means of a DWT");
      }

      // Read Nmcc
      int Nmcc = in.readUnsignedShort();
      int numBytesInputComponents = ((Nmcc & 0xA000) > 0) ? 2 : 1;
      int numInputComponents = Nmcc & 0x7FFF;
      if ((numInputComponents == 0) || (numInputComponents > 16384)) {
        throw new ErrorException();
      }

      // Read Cmcc
      mccParameters.inputIntermediateComponents[cc] = new int[numInputComponents];
      for (int j = 0; j < numInputComponents; j++) {
        mccParameters.inputIntermediateComponents[cc][j] = (numBytesInputComponents == 1) ? in.readUnsignedByte() : in.readUnsignedShort();
      }

      // Read Mmcc
      int Mmcc = in.readUnsignedShort();
      int numBytesOutputComponents = ((Mmcc & 0xA000) > 0) ? 2 : 1;
      int numOutputComponents = Mmcc & 0x7FFF;
      if ((numOutputComponents == 0) || (numOutputComponents > 16384)) {
        throw new ErrorException();
      }

      // Read Wmcc
      mccParameters.outputIntermediateComponents[cc] = new int[numOutputComponents];
      for (int j = 0; j < numOutputComponents; j++) {
        mccParameters.outputIntermediateComponents[cc][j] = (numBytesOutputComponents == 1) ? in.readUnsignedByte() : in.readUnsignedShort();
      }

      // Read Tmcc
      int Tmcc = (in.readByte() << 16) | in.readUnsignedShort();

      if ((mccParameters.MCTType[cc] == 1) || (mccParameters.MCTType[cc] == 2)) { // array-based
        throw new ErrorException("CADI only supports multiple component transform by means of a DWT");
      } else if (mccParameters.MCTType[cc] == 3) { // wavelet-based
        // DWT type
        mccParameters.DWTType[cc] = Tmcc & 0xFF;
        if (mccParameters.DWTType[cc] > 1) {
          throw new ErrorException("");
        }

        // DWT levels
        mccParameters.DWTLevels[cc] = (Tmcc >>> 16) & 0x3F;
        if (mccParameters.DWTLevels[cc] > 32) {
          throw new ErrorException("");
        }

      } else {
        assert (true);
      }

      // Read Omcc
      if (mccParameters.MCTType[cc] == 3) { // only available for wavelet-based transform
        mccParameters.componentOffsets[cc] = in.readInt();
      }

    }

    markerFound = false;
  }

  /**
   * Reads the MCO marker.
   *
   * @throws ErrorException
   * @throws IOException
   */
  private void readMCO() throws ErrorException, IOException {

    if ((jpcParameters.sizParameters.Rsiz & 0x0100) <= 0) {
      throw new ErrorException("MCO marker is only allowed with multiple component transform capabilityin the Rsiz parameter");
    }

    // Read Lmco
    int Lmco = in.readUnsignedShort();

    // Read Nmco
    int Nmco = in.readUnsignedByte();
    if (Lmco != (3 + Nmco)) {
      throw new ErrorException("Wrong length in MCO marker");
    }

    if (Nmco > 0) {
      jpcParameters.mcoParameters.order = new int[Nmco];
      for (int i = 0; i < Nmco; i++) {
        jpcParameters.mcoParameters.order[i] = in.readUnsignedByte();
      }
    }

    markerFound = false;
  }
}