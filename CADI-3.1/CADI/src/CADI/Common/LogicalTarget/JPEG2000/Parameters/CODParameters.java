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
package CADI.Common.LogicalTarget.JPEG2000.Parameters;

import java.io.PrintStream;

/**
 * This class storages the coding style default (COD) and the coding style
 * component (COC).
 * 
 * Further and detailed information, see ISO/IEC 15444-1 section A.6.1 and
 * section A.6.2, respectively.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2008/11/07
 */
public class CODParameters {

  /**
   * Progression order used to save the file. <br>
   * <p>
   * Valid values are:<br>
   *  <ul>
   *    <li> 0- LRCP Layer-Resolution-Component-Position
   *    <li> 1- RLCP Resolution-Layer-Component-Position
   *    <li> 2- RPCL Resolution-Position-Component-Layer
   *    <li> 3- PCRL Position-Component-Resolution-Layer
   *    <li> 4- CPRL Component-Position-Resolution-Layer
   *  </ul>
   */
  public int progressionOrder = -1;

  /**
   * Number of layers that the final codestream contains.
   * <p>
   * Only positive values allowed.
   */
  public int numLayers = -1;

  /**
   * Multiple component transformation. Allowed values are:
   *  <p>
   * Valid values are:<br>
   *  <ul>
   *     <li> 0 - No multiple component transformation
   *     <li> 1 - Component transformation used on components 0, 1, and 2
   *     <li> 2 - Array-based
   *     <li> 4 - DWT-based
   *     <li> 3 - Array-based + DWT-based
   *   </ul>
   *
   */
  public int multiComponentTransform = 0;

  /**
   * DWT levels to applied.
   * <p>
   * Negative values not allowed.
   */
  public int WTLevels = -1;

  /**
   * Discrete wavelet transform applied.
   * <p>
   * Valid values are:<br>
   *   <ul>
   *     <li> 0 - Nothing
   *     <li> 1 - Reversible 5/3 DWT
   *     <li> 2 - Irreversible 9/7 DWT
   *   </ul>
   */
  public int WTType = -1;

  /**
   * Style of code-block coding passes.
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Decode.MQDecoder}.
   *
   * Further information, see ISO/IEC 15444-1 section A.6.1 (table A-19).
   */
  public boolean bypass = false;

  public boolean reset = false;

  public boolean restart = false;

  public boolean causal = false;

  public boolean erterm = false;

  public boolean segmark = false;

  /**
   * Block height (exponent of 2).
   * <p>
   * Values greater than 2.
   */
  public int blockHeight = 0;

  /**
   * Block width (exponent of 2).
   * <p>
   * Values greater than 2.
   */
  public int blockWidth = 0;

  /**
   * Precinct width in the transformed domain for each and resolution level.
   * Index means [rLevel] (rLevel==0 is the LL subband, and 1, 2, ... represents
   * next starting with the little one).
   * <p>
   * Values equal or greater than BlockWidth.
   */
  public int[] precinctWidths = null;

  /**
   * Same as resolutionPrecinctWidths but for precinct heights.
   * <p>
   * Values equal or greater than blockHeight.
   */
  public int[] precinctHeights = null;

  /**
   * Use of start and end of packet headers.
   * <p>
   * In both cases true indicates that the marker is used.
   */
  public boolean useSOP = false;

  public boolean useEPH = false;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public CODParameters() {
  }

  /**
   * Deep copy constructor.
   *
   * @param parameters an object of this class.
   */
  public CODParameters(CODParameters parameters) {
    progressionOrder = parameters.progressionOrder;
    numLayers = parameters.numLayers;
    multiComponentTransform = parameters.multiComponentTransform;

    WTLevels = parameters.WTLevels;
    ;
    WTType = parameters.WTType;
    bypass = parameters.bypass;
    reset = parameters.reset;
    restart = parameters.restart;
    causal = parameters.causal;
    erterm = parameters.erterm;
    segmark = parameters.segmark;
    blockWidth = parameters.blockWidth;
    blockHeight = parameters.blockHeight;

    precinctWidths = new int[parameters.precinctWidths.length];
    precinctHeights = new int[parameters.precinctHeights.length];
    for (int i = 0; i < precinctWidths.length; i++) {
      precinctWidths[i] = parameters.precinctWidths[i];
      precinctHeights[i] = parameters.precinctHeights[i];
    }

    useSOP = parameters.useSOP;
    useEPH = parameters.useEPH;
  }

  /**
   * Sets the attributes to its initial values.
   */
  public void reset() {
    progressionOrder = -1;
    numLayers = -1;
    multiComponentTransform = -1;
    WTLevels = -1;
    WTType = -1;
    bypass = false;
    reset = false;
    restart = false;
    causal = false;
    erterm = false;
    segmark = false;
    blockHeight = -1;
    blockWidth = -1;
    precinctWidths = null;
    precinctHeights = null;
    useSOP = false;
    useEPH = false;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";
    str = getClass().getName() + " [";

    str += "Progression order=";
    switch (progressionOrder) {
      case 0:
        str += "Layer-Resolution-Component-Position (LRCP)";
        break;
      case 1:
        str += "Resolution-Layer-Component-Position (RLCP)";
        break;
      case 2:
        str += "Resolution-Position-Component-Layer (RPCL)";
        break;
      case 3:
        str += "Position-Component-Resolution-Layer (PCRL)";
        break;
      case 4:
        str += "Component-Position-Resolution-Layer (CPRL)";
        break;
      default:
        assert (true);
    }

    str += ", numLayers=" + numLayers;
    str += ", multiComponentTransform=" + multiComponentTransform;

    switch (WTType) {
      case 0:
        str += ", WTType=none";
        break;
      case 1:
        str += ", WTType=Reversible 5/3 DWT";
        break;
      case 2:
        str += ", WTType: Irreversible 9/7 DWT";
        break;
      default:
        assert (true);
    }
    str += ", WTLevels=" + WTLevels;

    str += ", Code-block style=(";
    if (bypass) {
      str += " bypass";
    }
    if (reset) {
      str += " resert";
    }
    if (restart) {
      str += " restart";
    }
    if (causal) {
      str += " causal";
    }
    if (erterm) {
      str += " erterm";
    }
    if (segmark) {
      str += " segmark";
    }
    str += ")";

    str += ", Block sizes={" + blockWidth + "," + blockHeight + "}";

    str += ", Precinct sizes={";
    if (precinctWidths != null) {
      for (int r = 0; r < precinctWidths.length - 1; r++) {
        str += precinctWidths[r] + "x" + precinctHeights[r] + ",";
      }
      str += precinctWidths[precinctWidths.length - 1] + "x"
              + precinctHeights[precinctHeights.length - 1] + "}";
      str += "}";

    } else {
      str += "<null>}";
    }

    str += ", SOP=" + useSOP;
    str += ", EPH=" + useEPH;

    str += "]";
    return str;
  }

  /**
   * Prints this COD parameters' fields to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- COD prameters --");

    out.print("Progression order: ");
    switch (progressionOrder) {
      case 0:
        out.println("Layer-Resolution-Component-Position (LRCP)");
        break;
      case 1:
        out.println("Resolution-Layer-Component-Position (RLCP)");
        break;
      case 2:
        out.println("Resolution-Position-Component-Layer (RPCL)");
        break;
      case 3:
        out.println("Position-Component-Resolution-Layer (PCRL)");
        break;
      case 4:
        out.println("Component-Position-Resolution-Layer (CPRL)");
        break;
      default:
        assert (true);
    }

    out.println("Number of layers: " + numLayers);
    out.println("Multi Component Transform: " + multiComponentTransform);

    switch (WTType) {
      case 0:
        out.println("WTType: none");
        break;
      case 1:
        out.println("WTType: Reversible 5/3 DWT");
        break;
      case 2:
        out.println("WTType: Irreversible 9/7 DWT");
        break;
      default:
        assert (true);
    }
    out.println("WTLevels=" + WTLevels);

    out.print("Code-block style=(");
    if (bypass) {
      out.print("bypass");
    }
    if (reset) {
      out.print(" resert");
    }
    if (restart) {
      out.print(" restart");
    }
    if (causal) {
      out.print(" causal");
    }
    if (erterm) {
      out.print(" erterm");
    }
    if (segmark) {
      out.print(" segmark");
    }
    out.println(")");

    out.println("Block sizes={" + blockWidth + "," + blockHeight + "}");


    out.print("Precinct sizes={");
    if (precinctWidths != null) {
      for (int r = 0; r < precinctWidths.length - 1; r++) {
        out.print(precinctWidths[r] + "x" + precinctHeights[r] + ",");
      }
      out.println(precinctWidths[precinctWidths.length - 1] + "x"
              + precinctHeights[precinctHeights.length - 1] + "}");
    } else {
      out.println("null}");
    }

    out.println("SOP: " + useSOP);
    out.println("EPH: " + useEPH);

    out.flush();
  }
}
