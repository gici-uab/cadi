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
package CADI.Server.LogicalTarget.JPEG2000.Codestream;

import CADI.Common.LogicalTarget.JPEG2000.JPCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.COCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.QCCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters;
import GiciException.*;
import GiciStream.*;

import java.io.PrintStream;
import java.nio.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class generates JPK headers from the CADI server. This heading is
 * useful for some parameters/options allowed in CADI and not allowed in
 * JPEG2000 standard headings. Usage example:<br>
 * 
 * &nbsp; construct<br>
 * &nbsp; run<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/03/06
 */
public class JPKMainHeaderEncoder {

  /**
   * All following variables have the following structure:
   *
   * type nameVar; //variable to be saved at the heading
   * final type nameVar_BITS; //number of bits allowed for this variable in the heading - its range will be from 0 to 2^nameVar_BITS, otherwise a WarningException will be thrown and heading will not be generated
   */
  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#zSize}
   */
  private int zSize;

  private final int zSize_BITS = 14;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#ySize}
   */
  private int ySize;

  private final int ySize_BITS = 20;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#xSize}
   */
  private int xSize;

  private final int xSize_BITS = 20;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#LSType}
   */
  private int LSType;

  private final int LSType_BITS = 4;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#LSComponents}
   */
  private boolean[] LSComponents = null;
  //final int LSComponents_BITS = 1;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#LSSubsValues}
   */
  private int[] LSSubsValues = null;

  private final int LSSubsValues_BITS = 16;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#signed}
   */
  private boolean[] LSSignedComponents = null;
  //final int LSSignedComponents_BITS = 1;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#RMMultValues}
   */
  private float[] RMMultValues = null;
  //final int RMMultValues_BITS = 32;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#CTType}
   */
  private int CTType;

  private final int CTType_BITS = 4;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#CTComponents}
   */
  private int[] CTComponents = null;

  private final int CTComponents_BITS = zSize_BITS;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#WTTypes}
   */
  private int[] WTTypes = null;

  private final int WTTypes_BITS = 4;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#WTLevels}
   */
  private int[] WTLevels = null;

  private final int WTLevels_BITS = 5; //log_2(max(ySize_BITS, xSize_BITS))

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#WT3D}
   */
  private int WT3D = 0;

  private final int WT3D_BITS = 3;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#WSL}
   */
  private int WSL = 0;

  private final int WSL_BITS = 5;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#WST}
   */
  private int WST = 0;

  private final int WST_BITS = 4;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters#QTypes}
   */
  private int[] QTypes = null;

  private final int QTypes_BITS = 4;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#QDynamicRange}
   */
  private int QDynamicRange;

  private final int QDynamicRange_BITS = 4;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#precision}
   */
  private int[] QComponentsBits = null;

  private final int QComponentsBits_BITS = 10;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters#QExponents}
   */
  private int[][][] QExponents = null;

  private final int QExponents_BITS = 8;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters#QMantisas}
   */
  private int[][][] QMantisas = null;

  private final int QMantisas_BITS = 12;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters#QGuardBits}
   */
  private int QGuardBits;

  private final int QGuardBits_BITS = 4;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#blockWidths}
   */
  private int[] blockWidths = null;

  private final int BDBlockWidths_BITS = 5; //log_2(max(ySize_BITS, xSize_BITS))

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#blockHeights}
   */
  private int[] blockHeights = null;

  private final int BDBlockHeights_BITS = 5; //log_2(max(ySize_BITS, xSize_BITS))

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#precinctWidths}
   */
  private int[][] resolutionPrecinctWidths = null;

  private final int BDResolutionPrecinctWidths_BITS = 5; //log_2(max(ySize_BITS, xSize_BITS))

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#precinctHeights}
   */
  private int[][] resolutionPrecinctHeights = null;

  private final int BDResolutionPrecinctHeights_BITS = 5; //log_2(max(ySize_BITS, xSize_BITS))

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#numLayers}
   */
  private int LCAchievedNumLayers;

  private final int LCAchievedNumLayers_BITS = 12;

  /**
   * Is the bitstream type
   * Bitstreams can be:<br>
   * <ul>
   *    <li> 0- J2B JPEG2000 bitstream (without MQ coding) - CANCELLED
   *    <li> 1- J2C JPEG2000 codestream (with MQ coding)
   * </ul>
   */
  private int BitStreamType = 1;

  private final int BitStreamType_BITS = 2;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#progressionOrder}
   */
  private int FWProgressionOrder;

  private final int FWProgressionOrder_BITS = 4;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#useSOP}
   * and {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#useEPH}.
   */
  private boolean[] FWPacketHeaders = null;
  //final int FWPacketHeaders_BITS = 1;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#waveletSA}
   */
  private int[] waveletSA = null;

  private final int CWaveletSA_BITS = 1;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#bitPlaneEncodingSA}
   */
  private int[] bitPlaneEncodingSA = null;

  private final int CBitPlaneEncodingSA_BITS = 1;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#RroiType}
   */
  private int RroiType;

  private final int RroiType_BITS = 8;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#RroisParameters}
   */
  private int[] RroisParameters;

  private final int RroisParameters_BITS = 20;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters#RBitplanesScaling}
   */
  private int[] RBitplanesScaling = null;

  private final int RBitplanesScaling_BITS = 5;//19 + 19 <= 38 log2(19) <= 5

  // ============================= public methods ==============================
  /**
   * Constructor.
   * 
   * @param jpcParameters
   */
  public JPKMainHeaderEncoder(JPCParameters jpcParameters) {
    this(jpcParameters.sizParameters, jpcParameters.codParameters,
            jpcParameters.qcdParameters, jpcParameters.jpkParameters,
            1,
            jpcParameters.cocParametersList, jpcParameters.qccParametersList);
  }

  /**
   *
   *  @param sizParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters}.
   * @param codParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters}.
   * @param qcdParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters}.
   * @param jpkParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters}.
   */
  public JPKMainHeaderEncoder(SIZParameters sizParameters, CODParameters codParameters, QCDParameters qcdParameters, JPKParameters jpkParameters) {
    this(sizParameters, codParameters, qcdParameters, jpkParameters, 1);
  }

  /**
   * Constructor.
   *
   * @param sizParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters}.
   * @param codParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters}.
   * @param qcdParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters}.
   * @param jpkParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters}.
   * @param BitStreamType definition in {@link #BitStreamType}.
   */
  public JPKMainHeaderEncoder(SIZParameters sizParameters, CODParameters codParameters, QCDParameters qcdParameters, JPKParameters jpkParameters, int BitStreamType) {
    this(sizParameters, codParameters, qcdParameters, jpkParameters, BitStreamType,
            new HashMap<Integer, COCParameters>(), new HashMap<Integer, QCCParameters>());
  }

  /**
   * Constructor.
   *
   * @param sizParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters}.
   * @param codParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters}.
   * @param qcdParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters}.
   * @param jpkParameters definition in
   * 	   {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters}.
   * @param BitStreamType definition in {@link #BitStreamType}.
   * @param cocParametersList
   * @param qccParametersList
   */
  public JPKMainHeaderEncoder(SIZParameters sizParameters,
                              CODParameters codParameters,
                              QCDParameters qcdParameters,
                              JPKParameters jpkParameters,
                              int BitStreamType,
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
    if (jpkParameters == null) {
      throw new NullPointerException();
    }
    if (cocParametersList == null) {
      throw new NullPointerException();
    }
    if (qccParametersList == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.zSize = sizParameters.zSize;
    this.ySize = sizParameters.ySize;
    this.xSize = sizParameters.xSize;
    this.LSSignedComponents = sizParameters.signed;
    this.QComponentsBits = sizParameters.precision;

    this.FWProgressionOrder = codParameters.progressionOrder;
    WTTypes = new int[sizParameters.zSize];
    Arrays.fill(WTTypes, codParameters.WTType);
    blockWidths = new int[sizParameters.zSize];
    Arrays.fill(blockWidths, codParameters.blockWidth);
    blockHeights = new int[sizParameters.zSize];
    Arrays.fill(blockHeights, codParameters.blockHeight);
    resolutionPrecinctWidths = new int[sizParameters.zSize][];
    for (int z = 0; z < zSize; z++) {
      resolutionPrecinctWidths[z] = codParameters.precinctWidths;
    }
    resolutionPrecinctHeights = new int[sizParameters.zSize][];
    for (int z = 0; z < zSize; z++) {
      resolutionPrecinctHeights[z] = codParameters.precinctHeights;
    }
    this.CTComponents = new int[3];
    if (codParameters.multiComponentTransform == 1) {
      this.CTType = codParameters.WTType == 1 ? 1 : 0;
      for (int i = 0; i < CTComponents.length; i++) {
        CTComponents[i] = i;
      }
    } else {
      this.CTType = 0;
      for (int i = 0; i < CTComponents.length; i++) {
        CTComponents[i] = 0;
      }
    }

    WTLevels = new int[sizParameters.zSize];
    Arrays.fill(WTLevels, codParameters.WTLevels);
    this.LCAchievedNumLayers = codParameters.numLayers;
    this.FWPacketHeaders = new boolean[2];
    FWPacketHeaders[0] = codParameters.useSOP;
    FWPacketHeaders[0] = codParameters.useEPH;

    QTypes = new int[sizParameters.zSize];
    Arrays.fill(QTypes, qcdParameters.quantizationStyle);

    QExponents = new int[sizParameters.zSize][][];
    for (int z = 0; z < zSize; z++) {
      QExponents[z] = qcdParameters.exponents;
    }

    QMantisas = new int[sizParameters.zSize][][];
    for (int z = 0; z < zSize; z++) {
      QMantisas[z] = qcdParameters.mantisas;
    }

    this.QGuardBits = qcdParameters.guardBits;

    this.waveletSA = jpkParameters.waveletSA;
    this.bitPlaneEncodingSA = jpkParameters.bitPlaneEncodingSA;
    this.RroiType = jpkParameters.RroiType;
    this.RroisParameters = jpkParameters.RroisParameters;
    this.RBitplanesScaling = jpkParameters.RBitplanesScaling;
    this.WT3D = jpkParameters.WT3D;
    this.WSL = jpkParameters.WSL;
    this.WST = jpkParameters.WST;
    this.LSType = jpkParameters.LSType;
    this.LSComponents = jpkParameters.LSComponents;
    this.LSSubsValues = jpkParameters.LSSubsValues;
    this.RMMultValues = jpkParameters.RMMultValues;
    this.QDynamicRange = jpkParameters.QDynamicRange;

    this.BitStreamType = BitStreamType;


    for (Map.Entry<Integer, COCParameters> e : cocParametersList.entrySet()) {
      int z = e.getKey();
      COCParameters cocParams = e.getValue();
      WTLevels[z] = cocParams.WTLevels;
      WTTypes[z] = cocParams.WTType;
      blockHeights[z] = cocParams.blockHeight;
      blockWidths[z] = cocParams.blockWidth;
      resolutionPrecinctHeights[z] = cocParams.precinctHeights;
      resolutionPrecinctWidths[z] = cocParams.precinctWidths;
    }

    for (Map.Entry<Integer, QCCParameters> e : qccParametersList.entrySet()) {
      int z = e.getKey();
      QCCParameters cocParams = e.getValue();
      QTypes[z] = cocParams.quantizationStyle;
      QExponents[z] = cocParams.exponents;
      QMantisas[z] = cocParams.mantisas;
    }
  }

  /**
   * Generates the JPK heading.
   *
   * @throws WarningException when the heading can not be generated due to some variable exceeds the maximum allowed range
   */
  public ByteStream run() throws WarningException {
    BitStream JPKHeading = new BitStream();

    //zSize
    if ((zSize < 0) || (zSize >= (int)Math.pow(2, zSize_BITS))) {
      throw new WarningException("Wrong zSize.");
    }
    JPKHeading.addBits(zSize, zSize_BITS);

    //ySize
    if ((ySize < 0) || (ySize >= (int)Math.pow(2, ySize_BITS))) {
      throw new WarningException("Wrong ySize.");
    }
    JPKHeading.addBits(ySize, ySize_BITS);

    //xSize
    if ((xSize < 0) || (xSize >= (int)Math.pow(2, xSize_BITS))) {
      throw new WarningException("Wrong xSize.");
    }
    JPKHeading.addBits(xSize, xSize_BITS);

    //LSType
    if ((LSType < 0) || (LSType >= (int)Math.pow(2, LSType_BITS))) {
      throw new WarningException("Wrong LSType.");
    }
    JPKHeading.addBits(LSType, LSType_BITS);

    //LSComponents
    //Only put all components LSComponents in heading if all of them are not equal
    boolean LSComponents_ALL = true;
    for (int z = 0; z < zSize; z++) {
      if (!LSComponents[z]) {
        LSComponents_ALL = false;
      }
    }
    JPKHeading.addBit(LSComponents_ALL);
    if (!LSComponents_ALL) {
      for (int z = 0; z < zSize; z++) {
        JPKHeading.addBit(LSComponents[z]);
      }
    }

    //LSSubsValues
    if (LSType > 1) {
      for (int z = 0; z < zSize; z++) {
        JPKHeading.addBits(LSSubsValues[z], LSSubsValues_BITS);
      }
    }

    //LSSignedComponents
    //Only put all components LSSignedComponents in heading if all of them are not equal
    boolean LSSignedComponents_ALL = true;
    for (int z = 0; z < zSize; z++) {
      if (!LSSignedComponents[z]) {
        LSSignedComponents_ALL = false;
      }
    }
    JPKHeading.addBit(LSSignedComponents_ALL);
    if (!LSSignedComponents_ALL) {
      for (int z = 0; z < zSize; z++) {
        JPKHeading.addBit(LSSignedComponents[z]);
      }
    }

    //RMMultValues
    boolean RMMultValues_ALL = true;
    for (int z = 0; z < zSize; z++) {
      if (RMMultValues[z] != 1F) {
        RMMultValues_ALL = false;
      }
    }
    JPKHeading.addBit(RMMultValues_ALL);
    if (!RMMultValues_ALL) {
      for (int z = 0; z < zSize; z++) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.asFloatBuffer().put(RMMultValues[z]);
        for (int i = 0; i < 4; i++) {
          JPKHeading.addByte(bb.get(i));
        }
      }
    }

    //CTType
    if ((CTType < 0) || (CTType >= (int)Math.pow(2, CTType_BITS))) {
      throw new WarningException("Wrong CTType.");
    }
    JPKHeading.addBits(CTType, CTType_BITS);

    //CTComponents
    for (int i = 0; i < CTComponents.length; i++) {
      if ((CTComponents[i] < 0) || (CTComponents[i] >= (int)Math.pow(2, CTComponents_BITS))) {
        throw new WarningException("Wrong CTComponents.");
      }
      JPKHeading.addBits(CTComponents[i], CTComponents_BITS);
    }

    //WTTypes
    //Only put all WTTypes in heading if they are different
    boolean WTypes_ALL = true;
    int WTType = WTTypes[0];
    for (int z = 1; z < zSize; z++) {
      if (WTType != WTTypes[z]) {
        WTypes_ALL = false;
      }
    }
    JPKHeading.addBit(WTypes_ALL);
    if (WTypes_ALL) {
      if ((WTTypes[0] < 0) || (WTTypes[0] >= (int)Math.pow(2, WTTypes_BITS))) {
        throw new WarningException("Wrong WTTypes.");
      }
      JPKHeading.addBits(WTTypes[0], WTTypes_BITS);
    } else {
      for (int z = 0; z < zSize; z++) {
        if ((WTTypes[z] < 0) || (WTTypes[z] >= (int)Math.pow(2, WTTypes_BITS))) {
          throw new WarningException("Wrong WTTypes.");
        }
        JPKHeading.addBits(WTTypes[z], WTTypes_BITS);
      }
    }

    //WTLevels
    //Only put all WTLevels in heading if they are different
    boolean WTLevels_ALL = true;
    int WTLevel = WTLevels[0];
    for (int z = 1; z < zSize; z++) {
      if (WTLevel != WTLevels[z]) {
        WTLevels_ALL = false;
      }
    }
    JPKHeading.addBit(WTLevels_ALL);
    if (WTLevels_ALL) {
      if ((WTLevels[0] < 0) || (WTLevels[0] >= (int)Math.pow(2, WTLevels_BITS))) {
        throw new WarningException("Wrong WTLevels.");
      }
      JPKHeading.addBits(WTLevels[0], WTLevels_BITS);
    } else {
      for (int z = 0; z < zSize; z++) {
        if ((WTLevels[z] < 0) || (WTLevels[z] >= (int)Math.pow(2, WTLevels_BITS))) {
          throw new WarningException("Wrong WTLevels.");
        }
        JPKHeading.addBits(WTLevels[z], WTLevels_BITS);
      }
    }

    //QTypes
    //Only put all QTypes in heading if they are different
    boolean QTypes_ALL = true;
    int QType = QTypes[0];
    for (int z = 1; z < zSize; z++) {
      if (QType != QTypes[z]) {
        QTypes_ALL = false;
      }
    }
    JPKHeading.addBit(QTypes_ALL);
    if (QTypes_ALL) {
      if ((QTypes[0] < 0) || (QTypes[0] >= (int)Math.pow(2, QTypes_BITS))) {
        throw new WarningException("Wrong QTypes.");
      }
      JPKHeading.addBits(QTypes[0], QTypes_BITS);
    } else {
      for (int z = 0; z < zSize; z++) {
        if ((QTypes[z] < 0) || (QTypes[z] >= (int)Math.pow(2, QTypes_BITS))) {
          throw new WarningException("Wrong QTypes.");
        }
        JPKHeading.addBits(QTypes[z], QTypes_BITS);
      }
    }

    //QDynamicRange
    JPKHeading.addBits(QDynamicRange, QDynamicRange_BITS);

    //QComponentsBits
    //Only put all QComponentsBits in heading if they are different
    boolean QComponentsBits_ALL = true;
    int QComponentBits = QComponentsBits[0];
    for (int z = 1; z < zSize; z++) {
      if (QComponentBits != QComponentsBits[z]) {
        QComponentsBits_ALL = false;
      }
    }
    JPKHeading.addBit(QComponentsBits_ALL);
    if (QComponentsBits_ALL) {
      if ((QComponentsBits[0] < 0) || (QComponentsBits[0] >= (int)Math.pow(2, QComponentsBits_BITS))) {
        throw new WarningException("Wrong QComponentsBits.");
      }
      JPKHeading.addBits(QComponentsBits[0], QComponentsBits_BITS);
    } else {
      for (int z = 0; z < zSize; z++) {
        if ((QComponentsBits[z] < 0) || (QComponentsBits[z] >= (int)Math.pow(2, QComponentsBits_BITS))) {
          throw new WarningException("Wrong QComponentsBits.");
        }
        JPKHeading.addBits(QComponentsBits[z], QComponentsBits_BITS);
      }
    }

    //QExponents
    //Only put all QExponents in heading if they are different
    boolean QExponents_ALL = true;
    if (WTLevels_ALL) {
      for (int rLevel = 0; rLevel <= WTLevels[0]; rLevel++) {
        for (int subband = 0; subband < QExponents[0][rLevel].length; subband++) {
          int QExponent = QExponents[0][rLevel][subband];
          for (int z = 1; z < zSize; z++) {
            if (QExponent != QExponents[z][rLevel][subband]) {
              QExponents_ALL = false;
            }
          }
        }
      }
    } else {
      QExponents_ALL = false;
    }
    JPKHeading.addBit(QExponents_ALL);
    if (QExponents_ALL) {
      for (int rLevel = 0; rLevel <= WTLevels[0]; rLevel++) {
        for (int subband = 0; subband < QExponents[0][rLevel].length; subband++) {
          if ((QExponents[0][rLevel][subband] < 0) || (QExponents[0][rLevel][subband] >= (int)Math.pow(2, QExponents_BITS))) {
            throw new WarningException("Wrong QExponents.");
          }
          JPKHeading.addBits(QExponents[0][rLevel][subband], QExponents_BITS);
        }
      }
    } else {
      for (int z = 0; z < zSize; z++) {
        for (int rLevel = 0; rLevel <= WTLevels[z]; rLevel++) {
          for (int subband = 0; subband < QExponents[z][rLevel].length; subband++) {
            if ((QExponents[z][rLevel][subband] < 0) || (QExponents[z][rLevel][subband] >= (int)Math.pow(2, QExponents_BITS))) {
              throw new WarningException("Wrong QExponents.");
            }
            JPKHeading.addBits(QExponents[z][rLevel][subband], QExponents_BITS);
          }
        }
      }
    }

    //QMantisas
    //Only put all QMantisas in heading if they are different
    boolean QMantisas_ALL = true;
    if (WTLevels_ALL) {
      for (int rLevel = 0; rLevel <= WTLevels[0]; rLevel++) {
        for (int subband = 0; subband < QMantisas[0][rLevel].length; subband++) {
          int QMantisa = QMantisas[0][rLevel][subband];
          for (int z = 1; z < zSize; z++) {
            if (QMantisa != QMantisas[z][rLevel][subband]) {
              QMantisas_ALL = false;
            }
          }
        }
      }
    } else {
      QMantisas_ALL = false;
    }
    JPKHeading.addBit(QMantisas_ALL);
    if (QMantisas_ALL) {
      for (int rLevel = 0; rLevel <= WTLevels[0]; rLevel++) {
        for (int subband = 0; subband < QMantisas[0][rLevel].length; subband++) {
          if ((QMantisas[0][rLevel][subband] < 0) || (QMantisas[0][rLevel][subband] >= (int)Math.pow(2, QMantisas_BITS))) {
            throw new WarningException("Wrong QMantisas.");
          }
          JPKHeading.addBits(QMantisas[0][rLevel][subband], QMantisas_BITS);
        }
      }
    } else {
      for (int z = 0; z < zSize; z++) {
        for (int rLevel = 0; rLevel <= WTLevels[z]; rLevel++) {
          for (int subband = 0; subband < QMantisas[z][rLevel].length; subband++) {
            if ((QMantisas[z][rLevel][subband] < 0) || (QMantisas[z][rLevel][subband] >= (int)Math.pow(2, QMantisas_BITS))) {
              throw new WarningException("Wrong QMantisas.");
            }
            JPKHeading.addBits(QMantisas[z][rLevel][subband], QMantisas_BITS);
          }
        }
      }
    }

    //QGuardBits
    if ((QGuardBits < 0) || (QGuardBits >= (int)Math.pow(2, QGuardBits_BITS))) {
      throw new WarningException("Wrong xSize.");
    }
    JPKHeading.addBits(QGuardBits, QGuardBits_BITS);

    //BDBlockWidths
    //Only put all BDBlockWidths in heading if they are different
    boolean BDBlockWidths_ALL = true;
    int BDBlockWidth = blockWidths[0];
    for (int z = 1; z < zSize; z++) {
      if (BDBlockWidth != blockWidths[z]) {
        BDBlockWidths_ALL = false;
      }
    }
    JPKHeading.addBit(BDBlockWidths_ALL);
    if (BDBlockWidths_ALL) {
      if ((blockWidths[0] < 0) || (blockWidths[0] >= (int)Math.pow(2, BDBlockWidths_BITS))) {
        throw new WarningException("Wrong BDBlockWidths.");
      }
      JPKHeading.addBits(blockWidths[0], BDBlockWidths_BITS);
    } else {
      for (int z = 0; z < zSize; z++) {
        if ((blockWidths[z] < 0) || (blockWidths[z] >= (int)Math.pow(2, BDBlockWidths_BITS))) {
          throw new WarningException("Wrong BDBlockWidths.");
        }
        JPKHeading.addBits(blockWidths[z], BDBlockWidths_BITS);
      }
    }

    //BDBlockHeights
    //Only put all BDBlockHeights in heading if they are different
    boolean BDBlockHeights_ALL = true;
    int BDBlockHeight = blockHeights[0];
    for (int z = 1; z < zSize; z++) {
      if (BDBlockHeight != blockHeights[z]) {
        BDBlockHeights_ALL = false;
      }
    }
    JPKHeading.addBit(BDBlockHeights_ALL);
    if (BDBlockHeights_ALL) {
      if ((blockHeights[0] < 0) || (blockHeights[0] >= (int)Math.pow(2, BDBlockHeights_BITS))) {
        throw new WarningException("Wrong BDBlockHeights.");
      }
      JPKHeading.addBits(blockHeights[0], BDBlockHeights_BITS);
    } else {
      for (int z = 0; z < zSize; z++) {
        if ((blockHeights[z] < 0) || (blockHeights[z] >= (int)Math.pow(2, BDBlockHeights_BITS))) {
          throw new WarningException("Wrong BDBlockHeights.");
        }
        JPKHeading.addBits(blockHeights[z], BDBlockHeights_BITS);
      }
    }

    //BDResolutionPrecinctWidths
    //Only put all BDResolutionPrecinctWidths in heading if they are different
    boolean BDResolutionPrecinctWidths_ALL = true;
    if (WTLevels_ALL) {
      for (int rLevel = 0; rLevel <= WTLevels[0]; rLevel++) {
        int BDResolutionPrecinctWidth = resolutionPrecinctWidths[0][rLevel];
        for (int z = 1; z < zSize; z++) {
          if (BDResolutionPrecinctWidth != resolutionPrecinctWidths[z][rLevel]) {
            BDResolutionPrecinctWidths_ALL = false;
          }
        }
      }
    } else {
      BDResolutionPrecinctWidths_ALL = false;
    }
    JPKHeading.addBit(BDResolutionPrecinctWidths_ALL);
    if (BDResolutionPrecinctWidths_ALL) {
      for (int rLevel = 0; rLevel <= WTLevels[0]; rLevel++) {
        if ((resolutionPrecinctWidths[0][rLevel] < 0) || (resolutionPrecinctWidths[0][rLevel] >= (int)Math.pow(2, BDResolutionPrecinctWidths_BITS))) {
          throw new WarningException("Wrong BDResolutionPrecinctWidths.");
        }
        JPKHeading.addBits(resolutionPrecinctWidths[0][rLevel], BDResolutionPrecinctWidths_BITS);
      }
    } else {
      for (int z = 0; z < zSize; z++) {
        for (int rLevel = 0; rLevel <= WTLevels[z]; rLevel++) {
          if ((resolutionPrecinctWidths[z][rLevel] < 0) || (resolutionPrecinctWidths[z][rLevel] >= (int)Math.pow(2, BDResolutionPrecinctWidths_BITS))) {
            throw new WarningException("Wrong BDResolutionPrecinctWidths.");
          }
          JPKHeading.addBits(resolutionPrecinctWidths[z][rLevel], BDResolutionPrecinctWidths_BITS);
        }
      }
    }

    //BDResolutionPrecinctHeights
    //Only put all BDResolutionPrecinctHeights in heading if they are different
    boolean BDResolutionPrecinctHeights_ALL = true;
    if (WTLevels_ALL) {
      for (int rLevel = 0; rLevel <= WTLevels[0]; rLevel++) {
        int BDResolutionPrecinctHeight = resolutionPrecinctHeights[0][rLevel];
        for (int z = 1; z < zSize; z++) {
          if (BDResolutionPrecinctHeight != resolutionPrecinctHeights[z][rLevel]) {
            BDResolutionPrecinctHeights_ALL = false;
          }
        }
      }
    } else {
      BDResolutionPrecinctHeights_ALL = false;
    }
    JPKHeading.addBit(BDResolutionPrecinctHeights_ALL);
    if (BDResolutionPrecinctHeights_ALL) {
      for (int rLevel = 0; rLevel <= WTLevels[0]; rLevel++) {
        if ((resolutionPrecinctHeights[0][rLevel] < 0) || (resolutionPrecinctHeights[0][rLevel] >= (int)Math.pow(2, BDResolutionPrecinctHeights_BITS))) {
          throw new WarningException("Wrong BDResolutionPrecinctHeights.");
        }
        JPKHeading.addBits(resolutionPrecinctHeights[0][rLevel], BDResolutionPrecinctHeights_BITS);
      }
    } else {
      for (int z = 0; z < zSize; z++) {
        for (int rLevel = 0; rLevel <= WTLevels[z]; rLevel++) {
          if ((resolutionPrecinctHeights[z][rLevel] < 0) || (resolutionPrecinctHeights[z][rLevel] >= (int)Math.pow(2, BDResolutionPrecinctHeights_BITS))) {
            throw new WarningException("Wrong BDResolutionPrecinctHeights.");
          }
          JPKHeading.addBits(resolutionPrecinctHeights[z][rLevel], BDResolutionPrecinctHeights_BITS);
        }
      }
    }

    //LCAchievedNumLayers
    if ((LCAchievedNumLayers < 0) || (LCAchievedNumLayers >= (int)Math.pow(2, LCAchievedNumLayers_BITS))) {
      throw new WarningException("Wrong LCAchievedNumLayers.");
    }
    JPKHeading.addBits(LCAchievedNumLayers, LCAchievedNumLayers_BITS);

    //BitStreamType
    if ((BitStreamType < 0) || (BitStreamType >= (int)Math.pow(2, BitStreamType_BITS))) {
      throw new WarningException("Wrong BitStreamType.");
    }
    JPKHeading.addBits(BitStreamType, BitStreamType_BITS);

    //FWProgressionOrder
    if ((FWProgressionOrder < 0) || (FWProgressionOrder >= (int)Math.pow(2, FWProgressionOrder_BITS))) {
      throw new WarningException("Wrong FWProgressionOrder.");
    }
    JPKHeading.addBits(FWProgressionOrder, FWProgressionOrder_BITS);

    //FWPacketHeaders
    JPKHeading.addBit(FWPacketHeaders[0]);
    JPKHeading.addBit(FWPacketHeaders[1]);

    //CWaveletSA
    //Only put all WTShapeAdaptive in heading if they are different
    boolean CWaveletSA_ALL = true;
    int CWaveletSAComponent = waveletSA[0];
    for (int z = 1; z < zSize; z++) {
      if (CWaveletSAComponent != waveletSA[z]) {
        CWaveletSA_ALL = false;
      }
    }
    JPKHeading.addBit(CWaveletSA_ALL);
    if (CWaveletSA_ALL) {
      if ((waveletSA[0] < 0) || (waveletSA[0] > 1)) {
        throw new WarningException("Wrong WTShapeAdaptive values.");
      }
      JPKHeading.addBits(waveletSA[0], CWaveletSA_BITS);
    } else {
      for (int z = 0; z < zSize; z++) {
        if ((waveletSA[z] < 0) || (waveletSA[z] > 1)) {
          throw new WarningException("Wrong WTShapeAdaptive values.");
        }
        JPKHeading.addBits(waveletSA[z], CWaveletSA_BITS);
      }
    }

    //CBitPlaneEncodingSA
    JPKHeading.addBits(bitPlaneEncodingSA[0], CBitPlaneEncodingSA_BITS);

    //RSroiType
    JPKHeading.addBits(RroiType, RroiType_BITS);
    switch (RroiType) {
      case 1://maxSsift
        for (int z = 0; z < zSize; z++) {
          if ((RBitplanesScaling[z] < 0) || (RBitplanesScaling[z] > 19)) {
            throw new WarningException("Wrong RSmaxShiftScaling values.");
          }
          JPKHeading.addBits(RBitplanesScaling[z], RBitplanesScaling_BITS);
        }
        break;
      case 3://Bitplane by Bitplane Shift
        for (int i = 0; i < RroisParameters.length; i++) {
          JPKHeading.addBits(RroisParameters[i], RroisParameters_BITS);
        }
        break;
      case 4://Partial Significance Shift
        JPKHeading.addBits(RBitplanesScaling[0], RBitplanesScaling_BITS);
        break;
      case 6://Scaling based method
        JPKHeading.addBits(RroisParameters.length, RroisParameters_BITS);
        for (int i = 0; i < RroisParameters.length; i++) {
          JPKHeading.addBits(RroisParameters[i], RroisParameters_BITS);
        }
        break;
    }

    JPKHeading.addBits(WT3D, WT3D_BITS);
    if (WT3D != 0) {
      JPKHeading.addBits(WSL, WSL_BITS);
      JPKHeading.addBits(WST, WST_BITS);
    }
    //Change BitStream to ByteStream and return it
    return (new ByteStream(JPKHeading.getBitStream(), (int)JPKHeading.getNumBytes()));
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    String str = "";

    str += "-------------------------------------\n";

    str += "Num Components: " + zSize + "\n";


    if (LSSignedComponents != null) {
      str += "LSSignedComponents:";
      for (int i = 0; i < LSSignedComponents.length; i++) {
        str += " " + LSSignedComponents[i];
      }
      str += "\n";
    }

    str += "CTType: " + CTType + "\n";

    if (WTTypes != null) {
      str += "WTTypes:";
      for (int i = 0; i < WTTypes.length; i++) {
        str += " " + WTTypes[i];
      }
      str += "\n";
    }

    if (WTLevels != null) {
      str += "WTLevels:";
      for (int i = 0; i < WTLevels.length; i++) {
        str += " " + WTLevels[i];
      }
      str += "\n";
    }

    if (QTypes != null) {
      str += "QTypes:";
      for (int i = 0; i < QTypes.length; i++) {
        str += " " + QTypes[i];
      }
      str += "\n";
    }

    if (QComponentsBits != null) {
      str += "QComponentsBits:";
      for (int i = 0; i < QComponentsBits.length; i++) {
        str += " " + QComponentsBits[i];
      }
      str += "\n";
    }

    if (QExponents != null) {
      str += "QExponents:";
      for (int i = 0; i < QExponents.length; i++) {
        for (int j = 0; j < QExponents[i].length; j++) {
          for (int r = 0; r < QExponents[i][j].length; r++) {
            str += " " + QExponents[i][j][r];
          }
        }
      }
      str += "\n";
    }

    if (QMantisas != null) {
      str += "QMantisas:";
      for (int i = 0; i < QMantisas.length; i++) {
        for (int j = 0; j < QMantisas[i].length; j++) {
          for (int r = 0; r < QMantisas[i][j].length; r++) {
            str += " " + QMantisas[i][j][r];
          }
        }
      }
      str += "\n";
    }

    str += "QGuardBits: " + QGuardBits + "\n";

    if (blockHeights != null) {
      str += "blockHeights:";
      for (int i = 0; i < blockHeights.length; i++) {
        str += " " + blockHeights[i];
      }
      str += "\n";
    }

    if (blockWidths != null) {
      str += "blockWidths:";
      for (int i = 0; i < blockWidths.length; i++) {
        str += " " + blockWidths[i];
      }
      str += "\n";
    }

    if (resolutionPrecinctHeights != null) {
      str += "resolutionPrecinctHeights:";
      for (int i = 0; i < resolutionPrecinctHeights.length; i++) {
        for (int j = 0; j < resolutionPrecinctHeights[i].length; j++) {
          str += " " + resolutionPrecinctHeights[i][j];
        }
      }
      str += "\n";
    }

    if (resolutionPrecinctWidths != null) {
      str += "resolutionPrecinctWidths:";
      for (int i = 0; i < resolutionPrecinctWidths.length; i++) {
        for (int j = 0; j < resolutionPrecinctWidths[i].length; j++) {
          str += " " + resolutionPrecinctWidths[i][j];
        }
      }
      str += "\n";
    }



    if (FWPacketHeaders != null) {
      str += "FWPacketHeaders:";
      for (int i = 0; i < FWPacketHeaders.length; i++) {
        str += " " + FWPacketHeaders[i];
      }
      str += "\n";
    }

    if (LSComponents != null) {
      str += "LSComponents:";
      for (int i = 0; i < LSComponents.length; i++) {
        str += " " + LSComponents[i];
      }
      str += "\n";
    }

    if (LSSubsValues != null) {
      str += "LSSubsValues:";
      for (int i = 0; i < LSSubsValues.length; i++) {
        str += " " + LSSubsValues[i];
      }
      str += "\n";
    }

    if (RMMultValues != null) {
      str += "RMMultValues:";
      for (int i = 0; i < RMMultValues.length; i++) {
        str += " " + RMMultValues[i];
      }
      str += "\n";
    }


    if (CTComponents != null) {
      str += "CTComponents:";
      for (int i = 0; i < CTComponents.length; i++) {
        str += " " + CTComponents[i];
      }
      str += "\n";
    }


    // JPK PARAMETERS

    str += "LSType: " + LSType + "\n";

    str += "LSComponents: ";
    for (int i = 0; i < LSComponents.length; i++) {
      str += LSComponents[i] + " ";
    }
    str += "\n";

    str += "LSSubsValues: ";
    for (int i = 0; i < LSSubsValues.length; i++) {
      str += LSSubsValues[i] + " ";
    }
    str += "\n";

    str += "RMMultValues: ";
    for (int i = 0; i < RMMultValues.length; i++) {
      str += RMMultValues[i] + " ";
    }
    str += "\n";

    str += "CTComponents: ";
    for (int i = 0; i < CTComponents.length; i++) {
      str += CTComponents[i] + " ";
    }
    str += "\n";


    // Shape adaptive
    if (waveletSA != null) {
      str += "WaveletSA: ";
      for (int i = 0; i < waveletSA.length; i++) {
        str += waveletSA[i] + " ";
      }
      str += "\n";
    }

    if (bitPlaneEncodingSA != null) {
      str += "bitPlaneEncodingSA: ";
      for (int i = 0; i < bitPlaneEncodingSA.length; i++) {
        str += bitPlaneEncodingSA[i] + " ";
      }
      str += "\n";
    }

    // ROIs
    str += "Roi type: " + RroiType + "\n";

    if (RroisParameters != null) {
      str += "RroisParameters: ";
      for (int i = 0; i < RroisParameters.length; i++) {
        str += RroisParameters[i] + " ";
      }
      str += "\n";
    }

    if (RBitplanesScaling != null) {
      str += "RBitplanesScaling: ";
      for (int i = 0; i < RBitplanesScaling.length; i++) {
        str += RBitplanesScaling[i] + " ";
      }
      str += "\n";
    }


    // 3D parameters (JPK main header)
    if (WT3D != 0) {
      str += "This image is spectrally tranformed";
      str += "WT3D: " + WT3D + "\n";
      str += "WSL: " + WSL + "\n";
      str += "WST: " + WST + "\n";
    }

    str += "-----------------------------------------------------\n";

    return str;
  }

  /**
   * Prints the JPK Main Header Encoder data out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- JPK Main Header Encoder --");

    out.println("Num Components" + zSize);

    if (LSSignedComponents != null) {
      out.print("LSSignedComponents: ");
      for (int i = 0; i < LSSignedComponents.length; i++) {
        out.print(LSSignedComponents[i] + " ");
      }
      out.println();
    }

    out.println("CTType: " + CTType);

    if (WTTypes != null) {
      out.print("WTTypes: ");
      for (int i = 0; i < WTTypes.length; i++) {
        out.print(WTTypes[i] + " ");
      }
      out.println();
    }

    if (WTLevels != null) {
      out.print("WTLevels: ");
      for (int i = 0; i < WTLevels.length; i++) {
        out.print(WTLevels[i] + " ");
      }
      out.println();
    }

    if (QTypes != null) {
      out.print("QTypes: ");
      for (int i = 0; i < QTypes.length; i++) {
        out.print(QTypes[i] + " ");
      }
      out.println();
    }

    if (QComponentsBits != null) {
      out.print("QComponentsBits: ");
      for (int i = 0; i < QComponentsBits.length; i++) {
        out.print(QComponentsBits[i] + " ");
      }
      out.println();
    }

    if (QExponents != null) {
      out.print("QExponents: ");
      for (int i = 0; i < QExponents.length; i++) {
        for (int j = 0; j < QExponents[i].length; j++) {
          for (int r = 0; r < QExponents[i][j].length; r++) {
            out.print(QExponents[i][j][r] + " ");
          }
        }
      }
      out.println();
    }

    if (QMantisas != null) {
      out.print("QMantisas: ");
      for (int i = 0; i < QMantisas.length; i++) {
        for (int j = 0; j < QMantisas[i].length; j++) {
          for (int r = 0; r < QMantisas[i][j].length; r++) {
            out.print(QMantisas[i][j][r] + " ");
          }
        }
      }
      out.println();
    }

    out.println("QGuardBits: " + QGuardBits);

    if (blockHeights != null) {
      out.print("blockHeights: ");
      for (int i = 0; i < blockHeights.length; i++) {
        out.print(blockHeights[i] + " ");
      }
      out.println();
    }

    if (blockWidths != null) {
      out.print("blockWidths: ");
      for (int i = 0; i < blockWidths.length; i++) {
        out.print(blockWidths[i] + " ");
      }
      out.println();
    }

    if (resolutionPrecinctHeights != null) {
      out.print("resolutionPrecinctHeights: ");
      for (int i = 0; i < resolutionPrecinctHeights.length; i++) {
        for (int j = 0; j < resolutionPrecinctHeights[i].length; j++) {
          out.print(resolutionPrecinctHeights[i][j] + " ");
        }
      }
      out.println();
    }

    if (resolutionPrecinctWidths != null) {
      out.print("resolutionPrecinctWidths: ");
      for (int i = 0; i < resolutionPrecinctWidths.length; i++) {
        for (int j = 0; j < resolutionPrecinctWidths[i].length; j++) {
          out.print(resolutionPrecinctWidths[i][j] + " ");
        }
      }
      out.println();
    }

    if (FWPacketHeaders != null) {
      out.print("FWPacketHeaders: ");
      for (int i = 0; i < FWPacketHeaders.length; i++) {
        out.print(FWPacketHeaders[i] + " ");
      }
      out.println();
    }

    if (LSComponents != null) {
      out.println("LSComponents: ");
      for (int i = 0; i < LSComponents.length; i++) {
        out.println(LSComponents[i] + " ");
      }
      out.println();
    }

    if (LSSubsValues != null) {
      out.print("LSSubsValues: ");
      for (int i = 0; i < LSSubsValues.length; i++) {
        out.print(LSSubsValues[i] + " ");
      }
      out.println();
    }

    if (RMMultValues != null) {
      out.print("RMMultValues: ");
      for (int i = 0; i < RMMultValues.length; i++) {
        out.print(RMMultValues[i] + " ");
      }
      out.println();
    }


    if (CTComponents != null) {
      out.print("CTComponents: ");
      for (int i = 0; i < CTComponents.length; i++) {
        out.print(CTComponents[i] + " ");
      }
      out.println();
    }


    // JPK PARAMETERS

    out.println("LSType: " + LSType);

    out.print("LSComponents: ");
    for (int i = 0; i < LSComponents.length; i++) {
      out.print(LSComponents[i] + " ");
    }
    out.println();

    out.print("LSSubsValues: ");
    for (int i = 0; i < LSSubsValues.length; i++) {
      out.print(LSSubsValues[i] + " ");
    }
    out.println();

    out.print("RMMultValues: ");
    for (int i = 0; i < RMMultValues.length; i++) {
      out.print(RMMultValues[i] + " ");
    }
    out.println();

    out.print("CTComponents: ");
    for (int i = 0; i < CTComponents.length; i++) {
      out.print(CTComponents[i] + " ");
    }
    out.println();


    // Shape adaptive
    if (waveletSA != null) {
      out.print("WaveletSA: ");
      for (int i = 0; i < waveletSA.length; i++) {
        out.print(waveletSA[i] + " ");
      }
      out.println();
    }

    if (bitPlaneEncodingSA != null) {
      out.print("bitPlaneEncodingSA: ");
      for (int i = 0; i < bitPlaneEncodingSA.length; i++) {
        out.print(bitPlaneEncodingSA[i] + " ");
      }
      out.println();
    }

    // ROIs
    out.println("Roi type: " + RroiType);

    if (RroisParameters != null) {
      out.print("RroisParameters: ");
      for (int i = 0; i < RroisParameters.length; i++) {
        out.print(RroisParameters[i] + " ");
      }
      out.println();
    }

    if (RBitplanesScaling != null) {
      out.print("RBitplanesScaling: ");
      for (int i = 0; i < RBitplanesScaling.length; i++) {
        out.print(RBitplanesScaling[i] + " ");
      }
      out.println();
    }


    // 3D parameters (JPK main header)
    if (WT3D != 0) {
      out.print("This image is spectrally tranformed");
      out.print("WT3D: " + WT3D);
      out.print("WSL: " + WSL);
      out.print("WST: " + WST);
    }
    out.flush();
  }
}
