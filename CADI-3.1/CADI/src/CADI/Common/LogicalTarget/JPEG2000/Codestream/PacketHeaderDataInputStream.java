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
package CADI.Common.LogicalTarget.JPEG2000.Codestream;

import CADI.Common.Cache.PrecinctDataBin;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;

import GiciStream.BufferedDataInputStream;

/**
 * This class implements a data input read to be read from the packet headers
 * decoder. It abstracts from reading and discarting the 0xFF bytes which can
 * be found in the packet header stream. Therefore, each call to the
 * {@link #getTagBit()} method returns a valid bit.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.4 2012/03/12
 */
public class PacketHeaderDataInputStream {

  /**
   *
   */
  private BufferedDataInputStream bufferedDataInputStream = null;

  /**
   * 
   */
  private PrecinctDataBin dataBin = null;

  // INTERNAL ATTRIBUTES
  /**
   * Variable used by the getTagBit function, needed to get bit by bit the
   * data input stream
   * <p>
   * Only positive values allowed.
   */
  private int numBit = 0;

  /**
   * Boolean that indicates if a 0xFF has been found in the bytestream.
   * <p>
   * True indicates that a 0xFF byte has been found.
   */
  private boolean foundFF = false;

  /**
   * Variable used by the getTagBit function, needed to get bit by bit the
   * packet data. This is the byte readed.
   * <p>
   * Byte from the packet data.
   */
  private byte readByte = 0;

  /**
   * Variable used by the getTagBit function, needed to get bit by bit the
   * packet data. This variable is used to save the next byte readed when
   * detected an 0xFF (stuffing).
   * <p>
   * Byte from the packet data.
   */
  private byte nextByte;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public PacketHeaderDataInputStream() {
  }

  /**
   * Constructor
   *
   * @param bufferedDataInputStream
   */
  public PacketHeaderDataInputStream(BufferedDataInputStream bufferedDataInputStream) {
    if (bufferedDataInputStream == null) throw new NullPointerException();

    this.bufferedDataInputStream = bufferedDataInputStream;
    resetGetTagBit();
  }

  /**
   * 
   * @param precDataBin
   */
  public PacketHeaderDataInputStream(PrecinctDataBin precDataBin) {
    if (precDataBin == null) throw new NullPointerException();

    this.dataBin = precDataBin;
    resetGetTagBit();
  }

  /**
   *
   * @param bufferedDataInputStream
   */
  public void setInput(BufferedDataInputStream bufferedDataInputStream) {
    this.bufferedDataInputStream = bufferedDataInputStream;
    this.dataBin = null;
    resetGetTagBit();
  }

  /**
   *
   * @param bufferedDataInputStream
   */
  public void setInput(PrecinctDataBin precDataBin) {
    this.bufferedDataInputStream = null;
    this.dataBin = precDataBin;
    resetGetTagBit();
  }

  /**
   * Reset the getTagBit flags to reinitialize the reading.
   */
  public void resetGetTagBit() {
    numBit = 0;
    readByte = 0;
    foundFF = false;
  }

  /**
   * Retrieves a bit from the file. Function used in PacketDeheading.
   *
   * @return the readed bit
   *
   * @throws EOFException when end of file is reached
   * @throws IOException if an I/O error occurs
   *
   */
  public int getTagBit() throws EOFException, IOException {
    numBit--;
    if (numBit < 0) {

      //Read next byte, stuffing is found
      if (foundFF) {
        numBit = (byte)6;
        readByte = nextByte;
      } else {
        numBit = (byte)7;
        readByte = readByte();
      }

      //Check that the byte is an FF or not
      if (readByte == (byte)0xFF) {
        nextByte = readByte();
        foundFF = true;
      } else {
        foundFF = false;
      }
    }
    return ((readByte & (byte)(1 << numBit)) == 0 ? 0 : 1);
  }

  /**
   * 
   * @return
   * @throws EOFException
   * @throws IOException
   */
  public int read() throws EOFException, IOException {
    if (bufferedDataInputStream != null) {
      return bufferedDataInputStream.read();
    } else if (dataBin != null) {
      return (int)(dataBin.readByte() & 0xff);
    } else {
      throw new IOException();
    }
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";
    if (bufferedDataInputStream != null) {
      //str += bufferedDataInputStream.toString();
      str += "pos=" + bufferedDataInputStream.getPos();
    } else if (dataBin != null) {
      //str += dataBin.toString();
      str += "pos=" + dataBin.getPos();
    }
    str += " numBit=" + numBit;
    str += " foundFF=" + foundFF;
    str += " readByte=" + hexToString(readByte);
    str += " nextByte=" + hexToString(nextByte);
 
    str += "]";

    return str;
  }

  /**
   * Prints this Packet Header Data Output Stream out to the
   * specified output stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Packet Header Data Output Stream --");

    if (bufferedDataInputStream != null)
      bufferedDataInputStream.list(out);
    else if (dataBin != null)
      dataBin.list(out);
    out.println("numBit: " + numBit);
    out.println("foundFF: " + foundFF);
    out.println("readByte: " + hexToString(readByte));
    out.println("nextByte: " + hexToString(nextByte));

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * 
   * @return
   */
  private byte readByte() throws IOException {
    if (bufferedDataInputStream != null) {
      return bufferedDataInputStream.readByte();
    } else if (dataBin != null) {
      return dataBin.readByte();
    } else {
      throw new IOException();
    }
  }

  private String hexToString(byte b) {
    String str = "";
    if ((0xFF & b) < 16) {
      str += "0";
    }
    str += Integer.toHexString(0xFF & b);
    return str;
  }
}
