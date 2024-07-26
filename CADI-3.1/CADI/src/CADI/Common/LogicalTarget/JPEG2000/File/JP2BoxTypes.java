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
package CADI.Common.LogicalTarget.JPEG2000.File;

/**
 * This class  only defines the value which idenfies each JP2 file format
 * boxes.  
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/01/05
 */
public class JP2BoxTypes {

	// JPEG2000 SIGNATURE BOX
	static final int SIGNATURE_BOX_TYPE		= (int)0x6A502020; // 'jP  '
	static final int SIGNATURE_BOX_LENGTH	= (int)0x0000000C; // 12
  static final int SIGNATURE_BOX_CONTENT	= (int)0x0D0A870A;
	
	
	// FILE TYPE BOX
	static final int FILE_TYPE_BOX_TYPE	= (int)0x66747970; // 'ftyp'
	static final int FILE_TYPE_BOX_BRAND	= (int)0x6A703220; // 'jp2 '

	
	// JP2 HEADER BOX
	static final int JP2_HEADER_BOX_TYPE	= (int)0x6A703268; // 'jp2h'
	
	// Image Header Box
	static final int IMAGE_HEADER_BOX_TYPE			= (int)0x69686472; // 'ihdr'
	
	// Bits Per Component Box
	static final int BITS_PER_COMPONENT_BOX_TYPE 	= (int)0x62706363; // 'bpcc'
	
	// Color Specification Box
	static final int COLOR_SPECIFICATION_BOX_TYPE	= (int)0x636F6C72; // 'colr'
	
	// Palette Box
	static final int PALETTE_BOX_TYPE				= (int)0x70636C72; // 'pclr'
	
	// Component Mapping Box
	static final int COMPONENT_MAPPING_BOX_TYPE 		= (int)0x636D6170; // 'cmap'
	
	// Channel Definition Box
	static final int CHANNEL_DEFINITION_BOX_TYPE 	= (int)0x63646566; // 'cdef'
	
	// Resolution Box
	static final int RESOLUTION_BOX_TYPE 			= (int)0x72657320; // 'res '
	
	// Capture Resolution Box
	static final int CAPTURE_RESOLUTION_BOX_TYPE		= (int)0x72657363; // 'resc'
	
	// Capture Resolution Box
	static final int DEFAULT_DISPLAY_RESOLUTION_BOX_TYPE	= (int)0x72657364; // 'resd'
	
	// CONTIGUOUS CODE-STREAM BOX
	static final int CONTIGUOUS_CODESTREAM_BOX_TYPE	= (int)0x6A703263; // 'jp2c'
	
	
	// IPR BOX
	static final int INTELLECTUAL_PROPERTY_BOX_TYPE	= 0x64703269; // 'jp2i'
	
	
	// XML BOX
	static final int XML_BOX_TYPE = 0x786D6C20; // 'xml '
	
	
	// UUID BOX
	static final int UUID_BOX_TYPE = 0x75756964; // 'uuid'
	
	
	// UUID INFO BOX
	static final int UUID_INFO_BOX_TYPE = 0x75696E66; // 'uinf'
  
  
  // ============================= public methods ==============================
  /**
   * Converts a box type from the hexadecimal format to string.
   *
   * @param hex
   *
   * @return
   */
  public static String convertHexToString(String hexValue) {
    StringBuilder result = new StringBuilder();

    //example: 6A502020 is splitted into 4A 50 20 20
    for (int i = 0; i < hexValue.length() - 1; i += 2) {
      String output = hexValue.substring(i, (i + 2));
      int decimal = Integer.parseInt(output, 16);
      result.append((char) decimal);
    }

    return result.toString();
  }

  /**
   * Converts a box type from the integer format to string.
   *
   * @param value
   *
   * @return
   */
  public static String convertIntToString(int value) {
    return convertHexToString(Integer.toHexString(value));
  }

  /**
   * Converts a box type from the string format to hexadecimal.
   *
   * @param str
   *
   * @return
   */
  public static String convertStringToHex(String str) {

    char[] chars = str.toCharArray();

    StringBuilder hex = new StringBuilder();
    for (int i = 0; i < chars.length; i++) {
      hex.append(Integer.toHexString((int) chars[i]));
    }

    return hex.toString();
  }

  /**
   * Converts the box type form the string format to integer.
   *
   * @param str
   *
   * @return
   */
  public static int convertStringToInt(String str) {
    return Integer.parseInt(convertStringToHex(str), 16);
  }
	
}