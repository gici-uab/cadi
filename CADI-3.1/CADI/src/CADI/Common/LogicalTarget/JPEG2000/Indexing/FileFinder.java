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
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 Jan 29, 2008
 */
public class FileFinder extends JPEG2KBox {

  /**
   *
   */
  private long ooff = -1;

  /**
   *
   */
  private byte[] obh = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param ooff
   * @param obh
   */
  public FileFinder(long ooff, byte[] obh) {
    type = (int)0x66707472; //'fptr'
    this.ooff = ooff;
    this.obh = obh;
  }

  /**
   * Constructor.
   *
   * @param ooff
   * @param length
   * @param TBox
   */
  public FileFinder(long ooff, long length, int TBox) {
    type = (int)0x66707472; //'fptr'
    this.ooff = ooff;

    boolean extLen = length > (1L<<32) ? true : false;
    obh = new byte[extLen ? 16 : 8];
    writeLBox(extLen ? 1 : (int)length);
    writeTBox(TBox);
    if (extLen) writeLXBox(length);

  }

  public long getOffset() {
    return ooff;
  }

  /**
   *
   * @return
   */
  public int getTBox() {
    return ((obh[4] & 0xFF) << 24)
            | ((obh[5] & 0xFF) << 16)
            | ((obh[6] & 0xFF) << 8)
            | (obh[7] & 0xFF);
  }

  /**
   *
   * @return
   */
  public long getLength() {
    int LBox = readLBox();
    if (LBox != 1) {
      return LBox;
    } else {
      return readLXBox();
    }
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    String str = getClass().getName() + " [";

    str += super.toString();
    str += "ooff=" + ooff;
    int lbox = readLBox();
    str += ", LBox=" + lbox;
    str += ", TBox=" + getTBox();
    if (lbox == 1) str += ", XLBox=" + readLXBox();

    str += "]";

    return str;
  }

  /**
   * Prints this File Finder out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- File Finder --");

    super.list(out);
    out.println("ooff: " + ooff);
    int lbox = readLBox();
    out.println("LBox: " + lbox);
    out.println("TBox: " + getTBox());
    if (lbox == 1) out.println("XLBox=" + readLXBox());

    out.flush();
  }

  // ============================ private methods ==============================
  private int readLBox() {
    return ((obh[0] & 0xFF) << 24)
            | ((obh[1] & 0xFF) << 16)
            | ((obh[2] & 0xFF) << 8)
            | (obh[3] & 0xFF);
  }

  private long readLXBox() {
    return (long)(((long)(obh[8] & 0xFF) << 56)
                  | ((long)(obh[9] & 0xFF) << 48)
                  | ((long)(obh[10] & 0xFF) << 40)
                  | ((long)(obh[11] & 0xFF) << 32)
                  | ((long)(obh[12] & 0xFF) << 24)
                  | ((long)(obh[13] & 0xFF) << 16)
                  | ((long)(obh[14] & 0xFF) << 8)
                  | (long)(obh[15] & 0xFF));
  }

  private void writeLBox(int len) {
    obh[0] = ((byte)(0xff & (len >> 24)));
    obh[1] = ((byte)(0xff & (len >> 16)));
    obh[2] = ((byte)(0xff & (len >> 8)));
    obh[3] = ((byte)(0xff & len));
  }

  private void writeTBox(int TBox) {
    obh[4] = ((byte)(0xff & (TBox >> 24)));
    obh[5] = ((byte)(0xff & (TBox >> 16)));
    obh[6] = ((byte)(0xff & (TBox >> 8)));
    obh[7] = ((byte)(0xff & TBox));
  }

  private void writeLXBox(long len) {

    obh[8] = ((byte)(0xff & (len >> 56)));
    obh[9] = ((byte)(0xff & (len >> 48)));
    obh[10] = ((byte)(0xff & (len >> 40)));
    obh[11] = ((byte)(0xff & (len >> 32)));
    obh[12] = ((byte)(0xff & (len >> 24)));
    obh[13] = ((byte)(0xff & (len >> 16)));
    obh[14] = ((byte)(0xff & (len >> 8)));
    obh[15] = ((byte)(0xff & len));
  }
}
