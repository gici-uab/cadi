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
package CADI.Common.Cache;

import java.io.EOFException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class is designated to save an stream of bytes. It has basic operations
 * to add new chunks of bytes, read bytes or chunks of bytes, and methods to
 * set or read the pointer of the next byte to be read, skip a number of bytes
 * or the check the number of remainder bytes.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.3 2012/03/12
 */
public class DataBinStream {

  // TODO: assess performance between ArrayList and LinkedList
  // TODO: assess to keep variable chunk sizes or consider all the chunks with the same size.
  /**
   * One-dimensional array with the data of the data-bin.
   */
  private ArrayList<byte[]> chunks = null;

  /**
   * Is the length of all chunks (data bin saved).
   */
  private long length = 0; // maybe a int is enough

  // INTERNAL ATTRIBUTES
  /**
   * Is the index of the chunk.
   */
  private int chunkPos = 0;

  /**
   * Is the index of byte in the chunk pointed by {@link chunkPos}.
   */
  private int chunkIndex = 0;

  /**
   * Is the position in the whole data bin.
   * <p>
   * Its value is deduced from {@link #chunkIndex} and {@link #chunkPos} as:
   * pos = offset(chunkIndex) + chunPos, where offset() is the offset of the
   * chunk in the databin.
   * <p>
   * Although it can be computed it is considered to reduce the cost of
   * computing it value.
   */
  private long dataBinPos = 0; // maybe a int is enough

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public DataBinStream() {
    chunks = new ArrayList<byte[]>();
  }

  /**
   * Constructor.
   * <p>
   * It allows that an initial capacity, number of chunks, being set in order
   * to improve the performance.
   * 
   * @param numChunks initial capacity.
   */
  public DataBinStream(int numChunks) {
    chunks = new ArrayList<byte[]>(numChunks);
  }

  /**
   * Adds a new byte array to the stream.
   * 
   * @param data byte array to be added.
   */
  public void addData(byte[] data) {
    chunks.add(data);
    length += data.length;
  }

  /**
   * Adds a new byte array to the stream beginning at the offset position.
   * <p>
   * Data to be added must be placed just after the previous data or overlapped,
   * but not gaps/holes between the previous and new data are allowed.
   * 
   * @param data byte array to be added.
   * @param offset position in which to add the data.
   */
  public void addData(byte[] data, long offset) {
    addStream(data, offset);
  }

  /**
   * Adds a new byte array to the stream beginning at the offset position.
   * <p>
   * Data to be added must be placed just after the previous data or overlapped,
   * but not gaps/holes between the previous and new data are allowed.
   *
   * @param data byte array to be added.
   * @param offset position in which to add the data.
   */
  public void addStream(byte[] data, long offset) {
    if (offset > length) {
      System.err.println("No holes are allowed.");
      throw new UnsupportedOperationException("No holes are allowed.");
    } else if ((offset + data.length) <= length) {
      // Do nothing
    } else if (offset < length) {
      int srcPos = (int) (length - offset);
      addData(Arrays.copyOfRange(data, srcPos, data.length));
    } else {
      addData(data);
    }
  }

  /**
   * Sets internal attributes to their initial values.
   */
  public void reset() {
    chunkPos = -1;
    chunkIndex = -1;
    dataBinPos = 0;
  }

  /**
   * Sets the position of the pointer in the next byte to be read.
   * 
   * @param offset position of next byte to be read.
   */
  public void seek(long offset) {
    assert (offset >= 0);
    if (offset >= length) {
      throw new IndexOutOfBoundsException();
    }

    dataBinPos = offset;

    chunkIndex = 0;
    while (offset > chunks.get(chunkIndex).length) {
      offset -= chunks.get(chunkIndex).length;
      chunkIndex++;
    }
    chunkPos = (int) offset;
  }

  /**
   * Returns the length of the stored data.
   *
   * @return length of the data stream stored.
   */
  public long getLength() {
    return length;
  }

  /**
   * Skips <code>n</code> bytes in the data stream.
   *
   * @param n number of bytes to skip
   * @return numbe of bytes skipped.
   */
  public int skipBytes(int n) {
    assert (n >= 0);

    if ((getPos() + n) > length) {
      throw new IndexOutOfBoundsException();
    }

    dataBinPos += n;

    while (n > 0) {
      int chunkLen = chunks.get(chunkIndex).length;
      if (n >= (chunkLen - chunkPos)) {
        n -= (chunkLen - chunkPos);
        chunkIndex++;
        chunkPos = 0;
      } else {
        chunkPos += n;
        n = 0;
      }
    }

    return n;
  }

  /**
   * Returns the position of the next byte to be read in the data stream.
   *
   * @return positio of next byte to be read.
   */
  public long getPos() {
    return dataBinPos;
  }

  /**
   * The number of bytes that can be read from the actual position to the end.
   *
   * @return number of bytes that can be read.
   */
  public long getNumBytesLeft() {
    return (int) (length - dataBinPos);
  }

  /**
   * Definition in {@link #getNumBytesLeft() }.
   *
   * @return
   */
  public long available() {
    return getNumBytesLeft();
  }

  /**
   * Reads a byte from the data stream.
   *
   * @return byte read.
   * @throws EOFException
   */
  public byte readByte() throws EOFException {
    byte b = 0;

    if ((chunkIndex >= chunks.size())
            && (chunkPos == 0)) {
      throw new EOFException();
    }

    b = chunks.get(chunkIndex)[chunkPos];

    // Update counters
    dataBinPos++;
    chunkPos++;
    if (chunkPos >= chunks.get(chunkIndex).length) {
      chunkIndex++;
      chunkPos = 0;
    }

    return b;
  }

  /**
   * Reads an array of bytes.
   * 
   * @param b array of bytes to be read.
   */
  public void readFully(byte[] b) {
    int len = (b.length > (int) length) ? (int) length : b.length;
    try {
      readFully(b, 0, len);
    } catch (EOFException ex) {
      assert (true);
    }
  }

  /**
   * Reads <code>len</code> bytes form the data stream saving the in the <code>
   * b</code> array at position <code>off</code>.
   * 
   * @param b
   * @param off
   * @param len
   * 
   * @throws EOFException
   */
  public void readFully(byte[] b, int off, int len) throws EOFException {
    // Check input parameters
    if (b == null) {
      throw new NullPointerException();
    }
    if (len > getNumBytesLeft()) {
      throw new IndexOutOfBoundsException();
    }
    if ((off < 0) || (off > b.length) || (len < 0)
            || ((off + len) > b.length) || ((off + len) < 0)) {
      throw new IndexOutOfBoundsException();
    }

    // Copy data
    byte[] chunk = null;
    while (len > 0) {
      chunk = chunks.get(chunkIndex);
      int numBytes = chunk.length - chunkPos;
      if (numBytes == 0) {
        chunkPos = 0;
        chunkIndex++;
        chunk = chunks.get(chunkIndex);
      }
      if (numBytes > len) {
        numBytes = len;
      }
      System.arraycopy(chunk, chunkPos, b, off, numBytes);
      dataBinPos += numBytes;
      chunkPos += numBytes;
      off += numBytes;
      len -= numBytes;
    }
  }

  /**
   * Trims the capacity of list of objects used to be the list's current size,
   * in order to minimize the storage.
   */
  public void trimToSize() {
    chunks.trimToSize();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    String str = "";
    str = getClass().getName() + " [";
    str += "length=" + length;
    int numChunks = chunks.size();
    if (chunks == null) {
      str += "<null>";
    } else {
      str += "<< not displayed >>";
      /*for (byte[] tmp : chunks) {
        for (int i = 0; i < tmp.length; i++) {
          if ((0xFF & tmp[i]) < 16) {
            str += "0";
          }
          str += Integer.toHexString(0xFF & tmp[i]);
        }
      }
      str += ",";*/
    }
    str += "]";

    return str;
  }

  /**
   * Prints this DataBin out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {
    out.println("-- Data Bin Stream --");

    out.println("length: " + length);

    out.print("data: ");
    if (chunks == null) {
      out.println("<null>");
    } else {
      //out.print("<< not displayed >>");
      for (byte[] tmp : chunks) {
        for (int i = 0; i < tmp.length; i++) {
          if ((0xFF & tmp[i]) < 16) {
            out.print("0");
          }
          out.print(Integer.toHexString(0xFF & tmp[i])); 
        }        
      }
      out.println();
    }

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * 
   *
   */
  private byte[] ArrayListToArray(byte[] dst) {
    if (chunks.isEmpty()) {
      return null;
    }

    int offset = 0;
    for (byte[] buff : chunks) {
      System.arraycopy(buff, 0, dst, offset, buff.length);
      offset += buff.length;
    }
    return dst;
  }
}
