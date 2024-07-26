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
package CADI.Server.LogicalTarget.JPEG2000.Codestream;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import CADI.Common.LogicalTarget.JPEG2000.Codestream.JPCMarkers;
import CADI.Common.LogicalTarget.JPEG2000.JPCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.COCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.COMParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.QCCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters;
import CADI.Common.io.BufferedDataOutputStream;
import GiciException.WarningException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * This class generates the JPC headers from the JPEG2000 parameters. Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; run<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 2.1.0 2010/11/20
 */
public class JPCMainHeaderEncoder {

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters}.
   */
  private SIZParameters sizParameters = null;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters}.
   */
  private CODParameters codParameters = null;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.COCParameters}.
   */
  private HashMap<Integer, COCParameters> cocParametersList = null;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters}.
   */
  private QCDParameters qcdParameters = null;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.QCCParameters}.
   */
  private HashMap<Integer, QCCParameters> qccParametersList = null;

  /**
   *
   */
  private COMParameters comParameters = null;

  /**
   * Contains headers of the JPC file.
   */
  private BufferedDataOutputStream headerStream = null;

  //INTERNAL ATTRIBUTES
  /**
   * Tells if precincts are defined within the headers or not.
   *
   * Index in the array is component index
   */
  private boolean[] definedPrecincts = null;

  /**
   * Number of tile-parts in each tile. Index of array means tile index (0...65534).
   * <p>
   * Values must be between 0 - 254.
   */
  private int[] tileParts = null;

  // ============================= public methods ==============================
  /**
   * Constructor of JPCMainHeaderEncoder. It receives the information about
   * the compressed image needed to build the JPC main headers.
   *
   * @param sizParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters}.
   * @param codParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters}.
   * @param qcdParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters}.
   */
  public JPCMainHeaderEncoder(SIZParameters sizParameters,
                              CODParameters codParameters,
                              QCDParameters qcdParameters) {
    this(sizParameters, codParameters, qcdParameters,
         new HashMap<Integer, COCParameters>(),
         new HashMap<Integer, QCCParameters>());
  }

  /**
   * Constructor of JPCMainHeaderEncoder. It receives the information about
   * the compressed image needed to build the JPC main headers.
   *
   * @param sizParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters}.
   * @param codParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters}.
   * @param qcdParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters}.
   * @param cocParametersList
   * @param qccParametersList
   */
  public JPCMainHeaderEncoder(SIZParameters sizParameters,
                              CODParameters codParameters,
                              QCDParameters qcdParameters,
                              HashMap<Integer, COCParameters> cocParametersList,
                              HashMap<Integer, QCCParameters> qccParametersList) {
    // Check input parameters
    if (sizParameters == null) {
      throw new NullPointerException();
    }
    if (codParameters == null) {
      throw new NullPointerException();
    }
    if (qcdParameters == null) {
      throw new NullPointerException();
    }
    if (cocParametersList == null) {
      throw new NullPointerException();
    }
    if (qccParametersList == null) {
      throw new NullPointerException();
    }

    // Parameters copy
    this.sizParameters = sizParameters;
    this.codParameters = codParameters;
    this.qcdParameters = qcdParameters;
    this.cocParametersList = cocParametersList;
    this.qccParametersList = qccParametersList;

    tileParts = new int[1];
    tileParts[0] = 1;
    definedPrecincts = new boolean[sizParameters.zSize];
    for (int z = 0; z < sizParameters.zSize; z++) {
      definedPrecincts[z] = true;
    }

    headerStream = new BufferedDataOutputStream();
    headerStream.setResizable(true);
  }

  /**
   *
   * @param jpcParameters
   */
  public JPCMainHeaderEncoder(JPCParameters jpcParameters) {

    // Check input parameters
    if (jpcParameters.sizParameters == null) {
      throw new NullPointerException();
    }
    if (jpcParameters.codParameters == null) {
      throw new NullPointerException();
    }
    if (jpcParameters.qcdParameters == null) {
      throw new NullPointerException();
    }
    /*if (jpcParameters.cocParametersList == null) {
      throw new NullPointerException();
    }
    if (jpcParameters.qccParametersList == null) {
      throw new NullPointerException();
    }*/

    // Parameters copy
    sizParameters = jpcParameters.sizParameters;
    codParameters = jpcParameters.codParameters;
    qcdParameters = jpcParameters.qcdParameters;
    if (jpcParameters.cocParametersList != null) {
      cocParametersList = jpcParameters.cocParametersList;
    }
    if (jpcParameters.qccParametersList != null) {
      qccParametersList = jpcParameters.qccParametersList;
    }
    comParameters = jpcParameters.comParameters;

    tileParts = new int[1];
    tileParts[0] = 1;
    definedPrecincts = new boolean[sizParameters.zSize];
    for (int z = 0; z < sizParameters.zSize; z++) {
      definedPrecincts[z] = true;
    }

    headerStream = new BufferedDataOutputStream();
    headerStream.setResizable(true);
  }

  /**
   * Generates the JPC file headers.
   *
   * @return a bytestream with the JPC markers
   *
   * @throws WarningException when the header cannot be generated
   */
  public byte[] run() throws WarningException, IOException {

    // Adds SOC marker
    headerStream.writeShort(JPCMarkers.SOC);

    //Adds SIZ marker segment
    writeSIZ();

    //Adds COD marker segment
    writeCOD();

    //Adds as many COC marker segments as necessary
    if (cocParametersList != null) {
      for (Map.Entry<Integer, COCParameters> e : cocParametersList.entrySet()) {
        writeCOC(e.getKey(), e.getValue());
      }
    }

    //Adds QCD marker segment
    writeQCD();

    //Adds as many QCC marker segments as necessary
    if (qccParametersList != null) {
      for (Map.Entry<Integer, QCCParameters> e : qccParametersList.entrySet()) {
        writeQCC(e.getKey(), e.getValue());
      }
    }

    // Adds COM marker segment
    writeCOM();

    return headerStream.getByteArray(true);
  }

  /**
   * Returns the {@link #headerStream} attribute as a byte array.
   *
   * @return the {@link #headerStream} as a byte array.
   */
  public byte[] getMainHeader() {
    return headerStream.getByteArray(true);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [\n";
    str += "Not implemented yet";
    str += "\n";

    str += "]";
    return str;
  }

  /**
   * Prints this JPC Main Header Encoder out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- JPC Main Header Encoder --");
    out.println("Not implemented yet");
    out.println();
    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * Generates the SIZ marker segment.
   *
   * @throws WarningException when the header cannot be generated
   * @throws IOException when an I/O error has occurred.
   */
  private void writeSIZ() throws WarningException, IOException {

    int LSIZ = 38;
    int Rsiz = 0;
    int XRsiz = 1;
    int YRsiz = 1;
    //BitStream SIZBitStream = new BitStream();

    //SIZ marker
    headerStream.writeShort(JPCMarkers.SIZ);

    //SIZ marker segment's Lsiz
    LSIZ += (3 * sizParameters.zSize); //length of marker segment (not including the marker SIZ)
    if ((LSIZ < 41) || (LSIZ > 49190)) {
      throw new WarningException("LSIZ of marker SIZ is wrong.");
    }
    headerStream.writeShort(LSIZ);

    //SIZ marker segment's Rsiz
    if (Rsiz != 0) {
      throw new WarningException("Rsiz of marker SIZ is wrong.");
    }
    headerStream.writeShort(Rsiz);

    //SIZ marker segment's xSize and ySize
    if ((sizParameters.xSize < 1) || (sizParameters.xSize > (int)Math.pow(2, 32) - 1)) {
      throw new WarningException("xSize is wrong.");
    }
    headerStream.writeInt(sizParameters.xSize);
    if ((sizParameters.ySize < 1) || (sizParameters.ySize > (int)Math.pow(2, 32) - 1)) {
      throw new WarningException("ySize is wrong.");
    }
    headerStream.writeInt(sizParameters.ySize);

    //SIZ marker segment's XOsiz,YOsiz
    if ((sizParameters.XOsize < 0) || (sizParameters.XOsize > (int)Math.pow(2, 32) - 2)) {
      throw new WarningException("XOsize is wrong.");
    }
    headerStream.writeInt(sizParameters.XOsize);
    if ((sizParameters.YOsize < 0) || (sizParameters.YOsize > (int)Math.pow(2, 32) - 2)) {
      throw new WarningException("YOsize is wrong.");
    }
    headerStream.writeInt(sizParameters.YOsize);

    //SIZ marker segment's XTsiz, YTsiz
    if ((sizParameters.XTsize < 1) || (sizParameters.XTsize > (int)Math.pow(2, 32) - 1)) {
      throw new WarningException("TileXSize is wrong.");
    }
    headerStream.writeInt(sizParameters.XTsize);
    if ((sizParameters.YTsize < 1) || (sizParameters.YTsize > (int)Math.pow(2, 32) - 1)) {
      throw new WarningException("TileYSize is wrong.");
    }
    headerStream.writeInt(sizParameters.YTsize);

    //SIZ marker segment's XTOsiz,YTOsiz
    if ((sizParameters.XTOsize < 0) || (sizParameters.XTOsize > (int)Math.pow(2, 32) - 2)) {
      throw new WarningException("XTOsize is wrong.");
    }
    headerStream.writeInt(sizParameters.XTOsize);
    if ((sizParameters.YTOsize < 0) || (sizParameters.YTOsize > (int)Math.pow(2, 32) - 2)) {
      throw new WarningException("YTOsize is wrong.");
    }
    headerStream.writeInt(sizParameters.YTOsize);

    //SIZ marker segment's Csiz
    if ((sizParameters.zSize < 1) || (sizParameters.zSize > 16384)) {
      throw new WarningException("zSize is wrong.");
    }
    headerStream.writeShort(sizParameters.zSize);

    //SIZ marker segment's Sziz, XRsiz, YRsiz for each component
    if ((XRsiz < 1) || (XRsiz > (int)Math.pow(2, 8) - 1)) {
      throw new WarningException("XRsiz is wrong.");
    }
    if ((YRsiz < 1) || (YRsiz > (int)Math.pow(2, 8) - 1)) {
      throw new WarningException("YRsiz is wrong.");
    }
    for (int z = 0; z < sizParameters.zSize; z++) {
      int tempByte = sizParameters.signed[z] ? 0x80 : 0x00;

      if ((sizParameters.precision[z] - 1 < 1) || (sizParameters.precision[z] - 1 > 38)) {
        throw new WarningException("QComponentsBits is wrong.");
      }
      tempByte |= (sizParameters.precision[z] - 1) & 0x7F;
      headerStream.writeByte(tempByte);

      headerStream.writeByte(XRsiz);
      headerStream.writeByte(YRsiz);
    }
  }

  /**
   * Generates the COD marker segment COD has the default coding style for all components; first component is considered to have the default values.
   *
   * @throws WarningException when COD marker segment cannot be generated due to CADI options
   * @throws IOException when an I/O error has occurred.
   */
  private void writeCOD() throws WarningException, IOException {
    int LCOD = 12;

    //COD marker
    headerStream.writeShort(JPCMarkers.COD);

    //COD marker segment's Lcod
    if (definedPrecincts[0]) {
      LCOD += codParameters.WTLevels + 1;
    }
    headerStream.writeShort(LCOD);
    if ((LCOD < 12) || (LCOD > 45)) {
      throw new WarningException("LCOD of marker COD is wrong.");
    }

    //COD marker segment's Scod
    int Scod = 0;
    Scod |= codParameters.useEPH ? 0x04 : 0x00; //End of packet header marker
    Scod |= codParameters.useSOP ? 0x02 : 0x00; //Start of packet header marker
    Scod |= definedPrecincts[0] ? 0x01 : 0x00; //Precincts defined below
    headerStream.writeByte(Scod);

    //COD marker segment's SGcod
    if ((codParameters.progressionOrder < 0) || (codParameters.progressionOrder > 4)) {
      throw new WarningException("FWProgressionOrder is wrong.");
    }
    headerStream.writeByte(codParameters.progressionOrder);
    if ((codParameters.numLayers < 1) || (codParameters.numLayers > (int)Math.pow(2, 16) - 1)) {
      throw new WarningException("LCAchievedNumLayers is wrong.");
    }
    headerStream.writeShort(codParameters.numLayers);
    headerStream.writeByte(codParameters.multiComponentTransform);

    //COD marker segment's SPcod
    if ((codParameters.WTLevels < 0) || (codParameters.WTLevels > 32)) {
      throw new WarningException("WTLevels is wrong.");
    }
    headerStream.writeByte(codParameters.WTLevels);

    //code-block width and height
    if ((codParameters.blockWidth < 2) || (codParameters.blockWidth > 10)
            || (codParameters.blockHeight < 2) || (codParameters.blockHeight > 10)
            || ((codParameters.blockWidth + codParameters.blockHeight) > 12)) {
      throw new WarningException("Wrong block sizes.");
    }
    headerStream.writeByte(codParameters.blockWidth - 2);
    headerStream.writeByte(codParameters.blockHeight - 2);

    //style of code-block coding passes
    int codeBlockStyle = 0;
    codeBlockStyle |= codParameters.segmark ? (1 << 5) : 0x00;
    codeBlockStyle |= codParameters.erterm ? (1 << 4) : 0x00;
    codeBlockStyle |= codParameters.causal ? (1 << 3) : 0x00;
    codeBlockStyle |= codParameters.restart ? (1 << 2) : 0x00;
    codeBlockStyle |= codParameters.reset ? (1 << 1) : 0x00;
    codeBlockStyle |= codParameters.bypass ? (1 << 0) : 0x00;
    headerStream.writeByte(codeBlockStyle);

    //WT type used
    if ((codParameters.WTType != 1) && (codParameters.WTType != 2)) {
      throw new WarningException("WTTypes is wrong.");
    }
    if (codParameters.WTType == 1) {
      headerStream.writeByte(1);
    } else {
      headerStream.writeByte(0);
    }

    // Write precinct sizes
    writePrecinctSizes(0, codParameters.WTLevels, codParameters.precinctHeights, codParameters.precinctWidths);
  }

  /**
   * Generates the COC marker segment, COC has the coding style for a specific component.
   *
   * @param z image component
   *
   * @throws WarningException when COC marker segment cannot be generated due to BOI options
   * @throws IOException when an I/O error has occurred.
   */
  private void writeCOC(int z, COCParameters cocParameters) throws WarningException, IOException {
    int LCOC = 9;

    headerStream.writeShort(JPCMarkers.COC);

    //COD marker segment's Lcod
    if (definedPrecincts[z]) {

      if (sizParameters.zSize < 257) {
        LCOC = 10 + cocParameters.WTLevels;
      } else {
        LCOC = 11 + cocParameters.WTLevels;
      }
    } else {
      if (sizParameters.zSize >= 257) {
        LCOC++;
      }
    }
    if ((LCOC < 9) || (LCOC > 43)) {
      throw new WarningException("LCOC of marker COC is wrong.");
    }
    if (z > 16383) {
      throw new WarningException("Number of COC's has been exceded.");
    }
    headerStream.writeShort(LCOC);
    if (sizParameters.zSize < 257) {
      headerStream.writeByte(z);
    } else {
      headerStream.writeShort(z);
    }

    //COC marker segment's Scoc
    int Scoc = definedPrecincts[z] ? 1 : 0;
    headerStream.writeByte(Scoc);

    //COD marker segment's SPcoc
    if ((cocParameters.WTLevels < 0) || (cocParameters.WTLevels > 32)) {
      throw new WarningException("WTLevels is wrong.");
    }
    headerStream.writeByte(cocParameters.WTLevels);

    //code-block width and height
    if ((codParameters.blockWidth < 2) || (codParameters.blockWidth > 10)
            || (codParameters.blockHeight < 2) || (codParameters.blockHeight > 10)
            || ((codParameters.blockWidth + codParameters.blockHeight) > 12)) {
      throw new WarningException("Wrong block sizes.");
    }
    headerStream.writeByte(codParameters.blockWidth - 2);
    headerStream.writeByte(codParameters.blockHeight - 2);

    //style of code-block coding passes
    int codeBlockStyle = 0;
    codeBlockStyle |= codParameters.segmark ? (1 << 5) : 0x00;
    codeBlockStyle |= codParameters.erterm ? (1 << 4) : 0x00;
    codeBlockStyle |= codParameters.causal ? (1 << 3) : 0x00;
    codeBlockStyle |= codParameters.restart ? (1 << 2) : 0x00;
    codeBlockStyle |= codParameters.reset ? (1 << 1) : 0x00;
    codeBlockStyle |= codParameters.bypass ? (1 << 0) : 0x00;
    headerStream.writeByte(codeBlockStyle);

    //WT type used
    if ((cocParameters.WTType != 1) && (cocParameters.WTType != 2)) {
      throw new WarningException("WTTypes is wrong.");
    }
    if (cocParameters.WTType == 1) {
      headerStream.writeByte(1);
    } else {
      headerStream.writeByte(0);
    }

    writePrecinctSizes(z, codParameters.WTLevels, codParameters.precinctHeights, codParameters.precinctWidths);
  }

  /**
   * Recives the COD or COC BitStream marker and fill it with the precinct Sizes
   *
   * @param z image component
   *
   * @throws WarningException when COD or COC marker segment cannot be generated due to CADI options
   * @throws IOException when an I/O error has occurred.
   */
  private void writePrecinctSizes(int z, int WTLevels,
                                  int[] precinctHeights, int[] precinctWidths)
          throws WarningException, IOException {
    int widthExp, heightExp;
    if (definedPrecincts[z]) {

      for (int i = 0; i < (WTLevels + 1); i++) {
        //first corresponds to LL subband; each successive index corresponds to each resolution level in order
        int precinctSize = 0;

        heightExp = precinctHeights[i]; // + (i == 0 ? 0 : 1);
        if ((heightExp < 0) || (heightExp > Math.pow(2, 4))) {
          throw new WarningException("BDResolutionPrecinctHeights is wrong.");
        }
        precinctSize = (heightExp & 0x0F) << 4;

        widthExp = precinctWidths[i]; // + (i == 0 ? 0 : 1);
        if ((widthExp < 0) || (widthExp > Math.pow(2, 4))) {
          throw new WarningException("BDResolutionPrecinctWidths is wrong.");
        }
        precinctSize |= (widthExp & 0x0F);

        headerStream.writeByte(precinctSize);
      }
    }
  }

  /**
   * Generates the QCD marker segment, QCD has the default quantization parameters for all components.
   *
   * @throws WarningException when QCD marker segment cannot be generated due to CADI options
   * @throws IOException when an I/O error has occurred.
   */
  private void writeQCD() throws WarningException, IOException {
    int LQCD = 4;

    //QCD marker
    headerStream.writeShort(JPCMarkers.QCD);

    //QCD marker segment's Lqcd
    switch (qcdParameters.quantizationStyle) {
      case 0:	//no quantization
        LQCD = 4 + 3 * codParameters.WTLevels;
        break;
      case 1:	//scalar quantization derived
        LQCD = 5;
        break;
      case 2:	//scalar quantization expounded
        LQCD = 5 + (6 * codParameters.WTLevels);
        break;
    }
    if ((LQCD < 4) || (LQCD > 197)) {
      throw new WarningException("LQCD of marker QCD is wrong.");
    }
    headerStream.writeShort(LQCD);

    //QCD marker segment's Sqcd
    //guard bits
    if ((qcdParameters.guardBits < 0)
            || (qcdParameters.guardBits > Math.pow(2, 3) - 1)) {
      throw new WarningException("QGuardBits of marker QCD is wrong.");
    }
    int Sqcd = qcdParameters.guardBits << 5;
    Sqcd |= qcdParameters.quantizationStyle & 0x1F;
    headerStream.writeByte(Sqcd);

    //QCD marker segments' SPqcd
    writeQuantization(0, codParameters.WTLevels, qcdParameters.quantizationStyle, qcdParameters.exponents, qcdParameters.mantisas);
  }

  /**
   * Generates the QCC marker segment, QCC has the quantization parameters for a specific component.
   *
   * @param z image component
   *
   * @throws WarningException when QCC marker segment cannot be generated due to CADI options
   * @throws IOException when an I/O error has occurred.
   */
  private void writeQCC(int z, QCCParameters qccParameters) throws WarningException, IOException {
    int LQCC = 5;

    //QCC marker
    headerStream.writeShort(JPCMarkers.QCC);

    int WTLevels = cocParametersList.containsKey(z)
            ? cocParametersList.get(z).WTLevels
            : codParameters.WTLevels;

    //QCD marker segment's Lqcd
    switch (qccParameters.quantizationStyle) {
      case 0:	//no quantization
        if (sizParameters.zSize < 257) {
          LQCC = 5 + 3 * WTLevels;
        } else {
          LQCC = 6 + 3 * WTLevels;
        }
        break;
      case 1:	//scalar quantization derived
        if (sizParameters.zSize < 257) {
          LQCC = 6;
        } else {
          LQCC = 7;
        }

        break;
      case 2:	//scalar quantization expounded
        if (sizParameters.zSize < 257) {
          LQCC = 6 + 6 * WTLevels;
        } else {
          LQCC = 7 + 6 * WTLevels;
        }
        break;
      default:
        throw new WarningException("Unrecognized quantization type.");
    }
    if (LQCC > 199) {
      throw new WarningException("Length of QCC is wrong.");
    }
    headerStream.writeShort(LQCC);

    //Cqcc component index
    //zSize_MAX
    if (z > 16383) {
      throw new WarningException("Numbers of QCC markers has been exceded.");
    }
    if (sizParameters.zSize < 257) {
      headerStream.writeByte(z);
    } else {
      headerStream.writeShort(z);
    }

    //QCD marker segment's Sqcc
    if ((qccParameters.guardBits < 0)
            || (qccParameters.guardBits > Math.pow(2, 3) - 1)) {
      throw new WarningException("QGuardBits of marker QCD is wrong.");
    }
    int Sqcd = qccParameters.guardBits << 5;
    Sqcd |= qccParameters.quantizationStyle & 0x1F;
    headerStream.writeByte(Sqcd);

    //QCC marker segments' SPqcc
    writeQuantization(z, WTLevels, qcdParameters.quantizationStyle, qcdParameters.exponents, qcdParameters.mantisas);
  }

  /**
   * Receives the COD or COC BitStream marker and fill it with the precinct Sizes
   *
   * @param z image component
   *
   * @throws WarningException when COD or COC marker segment cannot be generated due to CADI options
   * @throws IOException when an I/O error has occurred.
   */
  private void writeQuantization(int z, int WTLevels, int quantizationStyle, int[][] exponents, int[][] mantisas) throws WarningException, IOException {

    int SPqcd = 0;

    switch (quantizationStyle) {
      case 0: //no quantization
        if ((exponents[0][0] < 0) || (exponents[0][0] > Math.pow(2, 5) - 1)) {
          throw new WarningException("Invalid exponent.");
        }
        SPqcd = (exponents[0][0] & 0x1F) << 3;
        headerStream.writeByte(SPqcd);
        for (int rLevel = 1; rLevel <= WTLevels; rLevel++) {
          for (int subband = 0; subband < exponents[rLevel].length; subband++) {
            if ((exponents[rLevel][subband] < 0) || (exponents[rLevel][subband] > Math.pow(2, 5) - 1)) {
              throw new WarningException("Invalid exponent.");
            }
            SPqcd = (exponents[rLevel][subband] & 0x1F) << 3;
            headerStream.writeByte(SPqcd);
          }
        }
        break;
      case 1: //scalar quatization derived
        if ((exponents[0][0] < 0) || (exponents[0][0] > Math.pow(2, 5) - 1)
                || (mantisas[0][0] < 0) || mantisas[0][0] > Math.pow(2, 11) - 1) {
          throw new WarningException("Invalid exponent or Mantisa.");
        }
        SPqcd = (exponents[0][0] & 0x1F) << 11;
        SPqcd |= mantisas[0][0] & 0x007FF;
        headerStream.writeShort(SPqcd);
        break;
      case 2: //scalar quantization expounded
        for (int rLevel = 0; rLevel <= WTLevels; rLevel++) {
          for (int subband = 0; subband < exponents[rLevel].length; subband++) {
            if ((exponents[rLevel][subband] < 0)
                    || (exponents[rLevel][subband] > Math.pow(2, 5) - 1)
                    || (mantisas[rLevel][subband] < 0)
                    || mantisas[rLevel][subband] > Math.pow(2, 11) - 1) {
              throw new WarningException("Invalid exponent or Mantisa.");
            }
            SPqcd = (exponents[rLevel][subband] & 0x1F) << 11;
            SPqcd |= mantisas[rLevel][subband] & 0x007FF;
            headerStream.writeShort(SPqcd);
          }
        }
        break;
    }
  }

  /**
   * Generates the COM marker segment.
   */
  private void writeCOM() throws IOException {

    /** THIS METHOD IS STILL UNDER DEVELOPMENT */
    String boiComment = "BOI Slopes-Lengths v1.0:";
    String kduLayerInfo = "Kdu-Layer-Info: log_2{Delta-D(MSE)/[2^16*Delta-L(bytes)]}, L(bytes)";
    String predModelInfo = "Predictive-Model v1.0:";

    // BOI COMMENTS
    if ((comParameters.boiSlopes != null) && (comParameters.boiLengths != null)) {
      // COM marker
      headerStream.writeShort(JPCMarkers.COM);

      // Write lcom
      int Lcom = 4 + boiComment.length() + 1
              + 4 * comParameters.boiSlopes.length
              + 8 * comParameters.boiLengths.length;
      headerStream.writeShort(Lcom);

      // Write Rcom
      int Rcom = 1;
      headerStream.writeShort(Rcom);

      // Write info comment
      headerStream.writeChars(boiComment + '\n');
    }

    // KAKADU COMMENTS
    if ((comParameters.kduLayerLogSlopes != null) && (comParameters.kduLengths != null)) {
      // COM marker
      headerStream.writeShort(JPCMarkers.COM);

      // Write lcom
      int Lcom = 4 + kduLayerInfo.length() + 1 + (6 + 8 + 2 + 1) * comParameters.kduLayerLogSlopes.length;
      headerStream.writeShort(Lcom);

      // Write Rcom
      int Rcom = 1;
      headerStream.writeShort(Rcom);

      // Write info comment
      headerStream.writeBytes(kduLayerInfo + '\n');
      for (int i = 0; i < comParameters.kduLayerLogSlopes.length; i++) {
        headerStream.writeBytes(String.format(Locale.US, "%6.1f", (comParameters.kduLayerLogSlopes[i] / 256F)));
        headerStream.writeBytes(", ");

        headerStream.writeBytes(String.format(Locale.US, "%8.1e", (double)comParameters.kduLengths[i]));
        headerStream.writeBytes("\n");
      }
    }

    // PREDICTIVE MODEL
    if (comParameters.predictiveModel != null) {
      // COM marker
      headerStream.writeShort(JPCMarkers.COM);

      // Write lcom
      int size = comParameters.predictiveModel.size();
      int Lcom = 4 + predModelInfo.length() + 1
              + (8 + 1)
              + (8 + 1) * size
              + (8 + 1) * size;
      headerStream.writeShort(Lcom);

      // Write Rcom
      int Rcom = 1;
      headerStream.writeShort(Rcom);

      // Write info comment
      headerStream.writeBytes(predModelInfo + '\n');
      headerStream.writeBytes(String.format(Locale.US, "%08d\n", comParameters.predictiveModel.size()));

      for (Map.Entry<Long, Float> entry : comParameters.predictiveModel.entrySet()) {
        String prec = String.format(Locale.US, "%08d", entry.getKey());
        String val = String.format(Locale.US, "%8.6f", entry.getValue());

        headerStream.writeBytes(prec);
        headerStream.writeBytes(",");
        headerStream.writeBytes(val);
        headerStream.writeBytes("\n");
      }
    }
  }
}
