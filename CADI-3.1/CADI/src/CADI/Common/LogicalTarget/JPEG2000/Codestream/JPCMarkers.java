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
package CADI.Common.LogicalTarget.JPEG2000.Codestream;

/**
 * This interfaces defines the marker values of the JPEG2000
 * codestream.
 * <p>
 * Further information, see ISO/IEC 15444-1 (Annex A.2)
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2008/04/18
 */
public class JPCMarkers {

  //DELIMITING MARKERS AND MARKER SEGMENTS
  /**
   * Start of codestream
   */
  public static short SOC = (short) 0xFF4F;

  /**
   * Start of tile-part
   */
  public static short SOT = (short) 0xFF90;

  /**
   * Start of data
   */
  public static short SOD = (short) 0xFF93;

  /**
   * End of codestream
   */
  public static short EOC = (short) 0xffd9;

  // FIXED INFORMATION MARKER SEGMENTS
  /**
   * Image and tile size
   */
  public static short SIZ = (short) 0xFF51;

  // FUNCTIONAL MARKER SEGMENTS
  /**
   * Coding style default
   */
  public static short COD = (short) 0xFF52;

  /**
   * Coding style component
   */
  public static short COC = (short) 0xFF53;

  /**
   * Region-of-interest
   */
  public static short RGN = (short) 0xFF5E;

  /**
   * Quantization default
   */
  public static short QCD = (short) 0xFF5C;

  /**
   * Quantization component
   */
  public static short QCC = (short) 0xFF5D;

  /**
   * Progression order change
   */
  public static short POC = (short) 0xFF5F;

  // POINTER MARKER SEGMENTS
  /**
   * Tile-part lengths
   */
  public static short TLM = (short) 0xFF55;

  /**
   * Packet length, main header
   */
  public static short PLM = (short) 0xFF57;

  /**
   * Packet length, tile-part header
   */
  public static short PLT = (short) 0xFF58;

  /**
   * Packed packet headers, main header
   */
  public static short PPM = (short) 0xFF60;

  /**
   * Packed packet headers, tile-part header
   */
  public static short PPT = (short) 0xFF61;

  // IN BIT STREAM MARKERS AND MARKER SEGMENTS
  /**
   * Start pf packet
   */
  public static short SOP = (short) 0xFF91;

  /**
   * End of packet header
   */
  public static short EPH = (short) 0xFF92;

  // INFORMATION MARKER SEGMENTS
  /**
   * Component registration (CRG)
   */
  public static short CRG = (short) 0xFF63;

  /**
   * Comment (COM)
   */
  public static short COM = (short) 0xFF64;

}
