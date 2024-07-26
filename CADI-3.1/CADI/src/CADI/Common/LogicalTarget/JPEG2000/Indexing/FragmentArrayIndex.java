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
package CADI.Common.LogicalTarget.JPEG2000.Indexing;

import java.io.PrintStream;

/**
 * Further information, please see see ISO/IEC 15444-1 section I.3.2.4.2
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2009/09/12
 */
public class FragmentArrayIndex {

  /**
   * Version.
   */
  private int v = -1;
  public static int VERSION_0 = 0;
  public static int VERSION_1 = 1;
  public static int VERSION_2 = 2;
  public static int VERSION_3 = 3;

  /**
   * Maximum number of valid elements in any row of the array.
   */
  private int elementsPerRow = -1;

  /**
   * Number of rows of the array.
   */
  private int numRows = -1;

  private byte[] off = null;

  private byte[] len = null;

  private byte[] aux = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   * <p>
   * Default version is 0.
   */
  public FragmentArrayIndex() {
    this(0);
  }

  /**
   * Constructor.
   * 
   * @param version definition in {@link #v}.
   */
  public FragmentArrayIndex(int version) {
    this(version, 1, 1);

  }

  /**
   * Constructor.
   * 
   * @param version definition in {@link #v}.
   * @param numElementsRow definition in {@link #nmax}.
   * @param numRows definition in {@link #m}.
   */
  public FragmentArrayIndex(int version, int numRows, int elementsPerRow) {
    if ((version < 0) || (version > 3)) {
      throw new IllegalArgumentException();
    }
    if (elementsPerRow < 0) {
      throw new IllegalArgumentException();
    }
    if (numRows < 0) {
      throw new IllegalArgumentException();
    }

    this.v = version;
    this.elementsPerRow = elementsPerRow;
    this.numRows = numRows;

    // Memory allocation for off and len
    if ((version == 0) || (version == 2)) {
      off = new byte[4 * elementsPerRow * numRows];
      len = new byte[4 * elementsPerRow * numRows];
    } else if ((version == 1) || (version == 3)) {
      off = new byte[8 * elementsPerRow * numRows];
      len = new byte[8 * elementsPerRow * numRows];
    } else {
      assert (true);
    }

    // Memory allocation for aux
    if ((version == 2) || (version == 3)) {
      aux = new byte[4 * elementsPerRow * numRows];
    }
  }

  /**
   * Returns the {@link #v} attribute.
   * 
   * @return the {@link #v} attribute.
   */
  public int getVersion() {
    return v;
  }

  public Class getType() {
    if ((v == 0) || (v == 2)) {
      return Integer.TYPE;
    } else if ((v == 1) || (v == 3)) {
      return Long.TYPE;
    } else {
      assert (true);
    }
    return null;
  }
  
  public int getNumRows() {
    return numRows;
  }
  
  public int getNumElementsPerRow() {
    return elementsPerRow;
  }

  /**
   * Returns the j-th offset in row i of the array.
   * 
   * @param i
   * @param j
   * @return 
   */
  public long getOffset(int row, int elementInRow) {
    if ((v == 0) || (v == 2)) {
      return getIntOffset(row,elementInRow);
    } else if ((v == 1) || (v == 3)) {
      return getLongOffset(row,elementInRow);
    } else {
      assert(true);
    }
    return -1;
  }

  /**
   * Returns the j-th length in row i of the array.
   * 
   * @param i 
   * @param j
   * @return 
   */
  public long getLength(int row, int elementInRow) {
     if ((v == 0) || (v == 2)) {
      return getIntLength(row,elementInRow);
    } else if ((v == 1) || (v == 3)) {
      return getLongLength(row,elementInRow);
    } else {
      assert(true);
    }
    return -1;
  }

  public int getIntOffset(int row, int elementInRow) {
    if ((v == 0) || (v == 2)) {
      int pos = 4 * elementsPerRow * row + 4 * elementInRow;
      return (off[pos] & 0xFF) << 24
              | (off[pos + 1] & 0xFF) << 16
              | (off[pos + 2] & 0xFF) << 8
              | (off[pos + 3] & 0xFF);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public int getIntLength(int row, int elementInRow) {
    if ((v == 0) || (v == 2)) {
      int pos = 4 * elementsPerRow * row + 4 * elementInRow;
      return (len[pos] & 0xFF) << 24
              | (len[pos + 1] & 0xFF) << 16
              | (len[pos + 2] & 0xFF) << 8
              | (len[pos + 3] & 0xFF);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public long getLongOffset(int row, int elementInRow) {
    if ((v == 1) || (v == 3)) {
      int pos = 8 * elementsPerRow * row + 8 * elementInRow;
      return (long) (off[pos] & 0xFF) << 56
              | (long) (off[pos + 1] & 0xFF) << 48
              | (long) (off[pos + 2] & 0xFF) << 40
              | (long) (off[pos + 3] & 0xFF) << 32
              | (long) (off[pos + 4] & 0xFF) << 24
              | (long) (off[pos + 5] & 0xFF) << 16
              | (long) (off[pos + 6] & 0xFF) << 8
              | (long) (off[pos + 7] & 0xFF);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public long getLongLength(int row, int elementInRow) {
    if ((v == 1) || (v == 3)) {
      int pos = 8 * elementsPerRow * row + 8 * elementInRow;
      return (long) (len[pos] & 0xFF) << 56
              | (long) (len[pos + 1] & 0xFF) << 48
              | (long) (len[pos + 2] & 0xFF) << 40
              | (long) (len[pos + 3] & 0xFF) << 32
              | (long) (len[pos + 4] & 0xFF) << 24
              | (long) (len[pos + 5] & 0xFF) << 16
              | (long) (len[pos + 6] & 0xFF) << 8
              | (long) (len[pos + 7] & 0xFF);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public int getAux(int row, int elementInRow) {
    if ((v == 2) || (v == 3)) {
      int pos = 4 * elementsPerRow * row + 4 * elementInRow;
      return (aux[pos] & 0xFF) << 24
              | (aux[pos + 1] & 0xFF) << 16
              | (aux[pos + 2] & 0xFF) << 8
              | (aux[pos + 3] & 0xFF);
    }
    throw new UnsupportedOperationException();
  }

  public void setOffset(int row, int elementInRow, int off) {
    if ((v == 0) || (v == 2)) {
      int pos = 4 * elementsPerRow * row + 4 * elementInRow;
      this.off[pos] = (byte) (0xff & (off >> 24));
      this.off[pos + 1] = (byte) (0xff & (off >> 16));
      this.off[pos + 2] = (byte) (0xff & (off >> 8));
      this.off[pos + 3] = (byte) (0xff & off);
    } else {
      setOffset(row, elementInRow, (long) off);
    }
  }

  public void setLength(int row, int elementInRow, int len) {
    if ((v == 0) || (v == 2)) {
      int pos = 4 * elementsPerRow * row + 4 * elementInRow;
      this.len[pos] = (byte) (0xff & (len >> 24));
      this.len[pos + 1] = (byte) (0xff & (len >> 16));
      this.len[pos + 2] = (byte) (0xff & (len >> 8));
      this.len[pos + 3] = (byte) (0xff & len);
    } else {
      setLength(row, elementInRow, (long) len);
    }
  }

  public void setAux(int row, int elementInRow, int aux) {
    if ((v == 2) || (v == 3)) {
      int pos = 4 * elementsPerRow * row + 4 * elementInRow;
      this.aux[pos] = (byte) (0xff & (aux >> 24));
      this.aux[pos + 1] = (byte) (0xff & (aux >> 16));
      this.aux[pos + 2] = (byte) (0xff & (aux >> 8));
      this.aux[pos + 3] = (byte) (0xff & aux);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void setOffset(int row, int elementInRow, long off) {
    if ((v == 0) || (v == 2)) {
      if (off > (1L << 32)) {
        throw new UnsupportedOperationException();
      }
      setOffset(row, elementInRow, (int) off);
    } else if ((v == 1) || (v == 3)) {
      int pos = 8 * elementsPerRow * row + 8 * elementInRow;
      this.off[pos] = (byte) (0xff & (off >> 56));
      this.off[pos + 1] = (byte) (0xff & (off >> 48));
      this.off[pos + 2] = (byte) (0xff & (off >> 40));
      this.off[pos + 3] = (byte) (0xff & (off >> 32));
      this.off[pos + 4] = (byte) (0xff & (off >> 24));
      this.off[pos + 5] = (byte) (0xff & (off >> 16));
      this.off[pos + 6] = (byte) (0xff & (off >> 8));
      this.off[pos + 7] = (byte) (0xff & off);
    } else {
      assert (true);
    }
  }

  public void setLength(int row, int elementInRow, long len) {
   if ((v == 0) || (v == 2)) {
      if (len > (1L << 32)) {
        throw new UnsupportedOperationException();
      }
      setLength(row, elementInRow, (int) len);
    } else if ((v == 1) || (v == 3)) {
      int pos = 8 * elementsPerRow * row + 8 * elementInRow;
      this.len[pos] = (byte) (0xff & (len >> 56));
      this.len[pos + 1] = (byte) (0xff & (len >> 48));
      this.len[pos + 2] = (byte) (0xff & (len >> 40));
      this.len[pos + 3] = (byte) (0xff & (len >> 32));
      this.len[pos + 4] = (byte) (0xff & (len >> 24));
      this.len[pos + 5] = (byte) (0xff & (len >> 16));
      this.len[pos + 6] = (byte) (0xff & (len >> 8));
      this.len[pos + 7] = (byte) (0xff & len);
    } else {
      assert (true);
    }
  }

  /**
   * Sets the attributes to their initial values.
   */
  public void reset() {
    v = -1;
    elementsPerRow = -1;
    numRows = -1;
    off = null;
    len = null;
    aux = null;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";
    str += "v=" + v;
    str += ", elementsPerRow=" + elementsPerRow;
    str += ", numRows=" + numRows;
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < elementsPerRow; j++) {
        if ((v == 0) || (v == 2)) {
          str += ", off=" + getIntOffset(i, j) + ", len=" + getIntLength(i, j);
        } else if ((v == 1) || (v == 3)) {
          str += ", off=" + getLongOffset(i, j) + ", len=" + getLongLength(i, j);
        } else {
          assert (true);
        }
        if ((v == 2) || (v == 3)) {
          str += ", aux=" + getAux(i, j);
        }
      }
    }
    str += "]";

    return str;
  }

  /**
   * Prints the Fragment Array Index data out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Fragment Array Index --");

    out.println("v: " + v);
    out.println("elementsPerRow: " + elementsPerRow);
    out.println("numRows: " + numRows);
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < elementsPerRow; j++) {
        if ((v == 0) || (v == 2)) {
          out.print("off=" + getIntOffset(i, j) + ", len=" + getIntLength(i, j));
        } else if ((v == 1) || (v == 3)) {
          out.print("off=" + getLongOffset(i, j) + ", len=" + getLongLength(i, j));
        } else {
          assert (true);
        }
        if ((v == 2) || (v == 3)) {
          out.print(", aux=" + getAux(i, j));
        }
        out.println();
      }
    }

    out.flush();
  }
}
