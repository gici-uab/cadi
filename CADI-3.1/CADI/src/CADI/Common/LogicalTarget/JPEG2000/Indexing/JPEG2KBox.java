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
package CADI.Common.LogicalTarget.JPEG2000.Indexing;

import java.io.PrintStream;

/**
 *
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2012/05/29
 */
public class JPEG2KBox {

  protected int type = -1;

  // ============================= public methods ==============================
  /**
   * Returns the {@link #type} attribute.
   *
   * @return the {@link #type} attribute.
   */
  public final int getType() {
    return type;
  }

  /**
   * Returns the {@link #type} attribute as a {@link java.lang.Object#toString()}.
   *
   * @return the {@link #type} attribute.
   */
  public final String getTypeAsString(String hex) {
    return JPEG2KBox.convertIntToString(type);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = getClass().getName() + " [";
    str += "type=" + getTypeAsString(Integer.toHexString(type));
    str += "]";

    return str;
  }

  /**
   * Prints this BoxIndexing out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- JPEG2K Box --");

    out.println("type: " + getTypeAsString(Integer.toHexString(type)));

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   *
   * @param hex
   *
   * @return
   */
  private static String convertHexToString(String hexValue) {
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
   *
   * @param value
   *
   * @return
   */
  private static String convertIntToString(int value) {
    return JPEG2KBox.convertHexToString(Integer.toHexString(value));
  }

  /**
   *
   * @param str
   *
   * @return
   */
  private static String convertStringToHex(String str) {

    char[] chars = str.toCharArray();

    StringBuilder hex = new StringBuilder();
    for (int i = 0; i < chars.length; i++) {
      hex.append(Integer.toHexString((int) chars[i]));
    }

    return hex.toString();
  }

  /**
   *
   * @param str
   *
   * @return
   */
  private static int convertStringToInt(String str) {
    return Integer.parseInt(convertStringToHex(str), 16);
  }
}
