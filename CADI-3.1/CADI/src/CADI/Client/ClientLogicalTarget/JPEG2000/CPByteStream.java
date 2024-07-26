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
package CADI.Client.ClientLogicalTarget.JPEG2000;

import CADI.Common.Cache.PrecinctDataBin;
import java.io.EOFException;

/**
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2011/08/27
 */
public class CPByteStream {

  private int offset = -1;

  private int length = -1;

  private PrecinctDataBin dataBin = null;
  
  private byte[] buff = null;
  
  // ============================= public methods ==============================
  /**
   * 
   * @param dataBin
   * @param offset
   * @param length
   */
  public CPByteStream(PrecinctDataBin dataBin, int offset, int length) {
    if (offset < 0) throw new IllegalArgumentException();
    if (length < 0) throw new IllegalArgumentException();

    this.offset = offset;
    this.length = length;
    this.dataBin = dataBin;
  }

  public void init() {
    if (dataBin == null) return;
    dataBin.seek(offset);
  }

  public int getOffset() {
    return offset;
  }
  
  public int getLength() {
    return length;
  }

  public int getNumBytes() {
    return getLength();
  }

  /**
   * OBS: temporary method while the MQCoder is not changed. It must not be used.
   *
   * @param n
   * @return
   * @throws EOFException
   */
  public byte getByte(int n) throws EOFException {
    
    if (buff == null) {
      buff = new byte[length];
      dataBin.lock();
      dataBin.seek(offset);
      dataBin.readFully(buff);
      dataBin.unlock();
    }
    
    return buff[n];
    
    /*if (dataBin.getPos() != (offset + n)) {
      //System.out.println("Seeking...");
      dataBin.seek(offset + n);
    }

    return dataBin.readByte();*/
  }
  
  
  public void lock() {
    dataBin.lock();
  }
  
  public void unlock() {
    dataBin.unlock();
  }
  
  // ============================ private methods ==============================
  /**
   * 
   */
  public void printData() {
    if (dataBin == null) return;
    byte[] data = new byte[length];
    dataBin.seek(offset);
    try {
      dataBin.readFully(data, 0, length);
    } catch (EOFException ex) {
      ex.printStackTrace();
    }
    printByteStream(data, length);
  }

  public void printByteStream(byte[] buffer, int length) {

    for (int index = 0; index < length; index++) {
      if ((0xFF & buffer[index]) < 16)
        System.out.print("0");
      System.out.print(Integer.toHexString(0xFF & buffer[index]));
    }
  }

}
