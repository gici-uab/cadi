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

import java.io.PrintStream;

import CADI.Common.Network.JPIP.ClassIdentifiers;
import java.io.EOFException;

/**
 * Extends the DataBin class to Meta data-based data (codestream main header).
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.2 2011/10/21
 */
public class MetaDataBin extends DataBin {

  /**
   * Object containing the stream of data.
   */
  protected DataBinStream dataStream = null;
 
  // ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public MetaDataBin() {
		super(ClassIdentifiers.PRECINCT);

    dataStream  = new DataBinStream();
	}
	
	/**
	 * Adds a data-bin message to the cache.
	 * <p>
	 * This method can only be used when there were no gaps between consecutive
	 * message data. Otherwise, and exception is thrown.
	 * 
	 * @param data definition in {@link CADI.Common.Network.JPIP.JPIPMessage#messageBody}
	 * @param offset definition in {@linkplain CADI.Common.Network.JPIP.JPIPMessageHeader#msgOffset}
	 * @param complete definition in {@linkplain CADI.Common.Network.JPIP.JPIPMessageHeader#isLastByte}
	 */
	public synchronized void addStream(byte[] data, long offset, boolean complete) {
		
		if (data == null) return;

    dataStream.addStream(data, offset);
    this.complete = complete;
	}
	
	/**
	 * Returns the length of the stored data. 
	 * 
	 * @return
	 */
	public long getLength() {
		return dataStream.getLength();
	}
	
	/**
	 * 
	 * @return
	 */
	public byte[] getDataArray() {
		byte[] b = new byte[(int)dataStream.getLength()];
    dataStream.seek(0);
    dataStream.readFully(b);
    return b;
	}

   /**
   *
   * @param offset
   */
  public void seek(long offset) {
    dataStream.seek(offset);
  }

  /**
   *
   * @param n
   * @return
   */
  public int skipBytes(int n) {
    return dataStream.skipBytes(n);
  }

  /**
   *
   * @return
   */
  public long getPos() {
    return dataStream.getPos();
  }

  /**
   *
   * @return
   */
  public long getNumBytesLeft() {
    return dataStream.getNumBytesLeft();
  }

  public long available() {
    return getNumBytesLeft();
  }

  /**
   *
   * @return
   * @throws EOFException
   */
  public byte readByte() throws EOFException {
    return dataStream.readByte();
  }

  /**
   *
   * @param b
   */
  public void readFully(byte[] b) {
    dataStream.readFully(b);
  }

  /**
   *
   * @param b
   * @param off
   * @param len
   * @throws EOFException
   */
  public void readFully(byte[] b, int off, int len) throws EOFException {
    dataStream.readFully(b, off, len);
  }
	
	/**
	 * Set attributes to its initial values.
	 */
  @Override
	public void reset() {
		super.reset();
		classIdentifier = -1;		
		dataStream.reset();
		complete = false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		
		String str = "";
		str = getClass().getName() + " [";
		str += super.toString();
		str += dataStream.toString();
		str += "]";
				
		return str;
	}
	
   /**
    * Prints this DataBin out to the specified output stream.
    * This method is useful for debugging.
    *
    * @param out an output stream.
    */
  @Override
	public void list(PrintStream out) {
		out.println("-- Meta Data Bin --");
		
    super.list(out);

    dataStream.list(out);

		out.flush();
	}
	
}