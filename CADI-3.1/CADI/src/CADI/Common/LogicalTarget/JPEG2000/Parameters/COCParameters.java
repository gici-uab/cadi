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
import java.util.Arrays;

/**
 * This class storages the coding style default (COC).
 * 
 * Further and detailed information, see ISO/IEC 15444-1 section A.6.2.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2010/11/19
 */
public class COCParameters {

  /**
   * DWT levels applied.
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
   * Block height (exponent of 2) for each image component.
   * <p>
   * Values greater than 2.
   */
  public int blockHeight = -1;

  /**
   * Block width (exponent of 2) for each image component.
   * <p>
   * Values greater than 2.
   */
  public int blockWidth = -1;

  /**
   * Precinct width in the transformed domain resolution level.
   * Note: rLevel==0 is the LL subband, and 1, 2, ... represents next starting
   * with the little one.
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

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public COCParameters() {
  }

  /**
   * Deep copy constructor.
   *
   * @param parameters an object of this class.
   */
  public COCParameters(COCParameters parameters) {
    WTLevels = parameters.WTLevels;
    WTType = parameters.WTType;
    bypass = parameters.bypass;
    reset = parameters.reset;
    restart = parameters.restart;
    causal = parameters.causal;
    erterm = parameters.erterm;
    segmark = parameters.segmark;
    blockWidth = parameters.blockWidth;
    blockHeight = parameters.blockHeight;
    precinctWidths = Arrays.copyOf(parameters.precinctWidths,
            parameters.precinctWidths.length);
    precinctHeights = Arrays.copyOf(parameters.precinctHeights,
            parameters.precinctHeights.length);
  }

  /**
   * Sets the attributes to its initial values.
   */
  public void reset() {
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
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";
    str = getClass().getName() + " [";

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

    str += " Block sizes={width=" + blockWidth + ", height=" + blockHeight + "}";

    str += " Precinct sizes={";
    if (precinctWidths != null) {
      str += "{";
      for (int r = 0; r < precinctWidths.length - 1; r++) {
        str += precinctWidths[r] + "x" + precinctHeights[r] + ",";
      }
      str += precinctWidths[precinctWidths.length - 1] + "x" + precinctHeights[precinctHeights.length - 1] + "}";
    } else {
      str += "<null>}";
    }

    str += "]";
    return str;
  }

  /**
   * Prints this COC parameters' fields to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- COC prameters --");

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
      out.print(" bypass");
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

    out.println(" Block sizes={width=" + blockWidth + ", height=" + blockHeight + "}");

    out.print(" Precinct sizes={");
    if (precinctWidths != null) {
      out.print("{");
      for (int r = 0; r < precinctWidths.length - 1; r++) {
        out.print(precinctWidths[r] + "x" + precinctHeights[r] + ",");
      }
      out.println(precinctWidths[precinctWidths.length - 1] + "x" + precinctHeights[precinctHeights.length - 1] + "}");
    } else {
      out.println("<null>}");
    }

    out.flush();
  }
}
