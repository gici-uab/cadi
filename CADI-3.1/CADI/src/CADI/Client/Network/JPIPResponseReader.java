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
package CADI.Client.Network;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2011/03/16
 */
public class JPIPResponseReader implements JPIPMessageReader {

  /**
   * The input stream where data are read from.
   */
  private InputStream inputStream = null;

  /**
   * This attribute indicates whether the end of stream has been reached.
   */
  private boolean endOfStream = false;

  // ============================= public methods ==============================
  
  /**
   * 
   * @param inputStream 
   */
  public JPIPResponseReader(InputStream inputStream) {
    if (inputStream == null) {
      throw new NullPointerException();
    }
    this.inputStream = inputStream;
  }

  @Override
  public int read() {
    try {
      return inputStream.read();
    } catch (EOFException eofe) {
      endOfStream = true;
    } catch (IOException ex) {
      endOfStream = true;
    }

    return -1;
  }

  @Override
  public void readFully(byte[] b, int off, int len) {
    int tmpLen = 0;
    try {
      while (tmpLen < len) {
        tmpLen = inputStream.read(b, off + tmpLen, len);
      }
    } catch (EOFException eofe) {
      endOfStream = true;
    } catch (IOException ex) {
      endOfStream = true;
    }
  }

  public boolean isEndOfStream() {
    return endOfStream;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";

    str += "<<< Not implemented yet >>> ";

    str += "]";
    return str;
  }

  /**
   * Prints this JPIP Response Reader fields out to the specified output
   * stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- JPIP Response Reader --");

    out.println("<<< Not implemented yet >>> ");

    out.flush();
  }
  // ============================ private methods ==============================
}
