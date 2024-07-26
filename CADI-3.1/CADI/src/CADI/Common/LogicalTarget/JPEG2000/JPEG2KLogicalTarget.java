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
package CADI.Common.LogicalTarget.JPEG2000;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import CADI.Common.Log.CADILog;

/**
 *
 *
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2011/01/18
 */
public abstract class JPEG2KLogicalTarget {

  /**
   * Definition in {@link CADI.Common.Network.JPIP.TargetField#tid}.
   */
  protected String tid = null;

  /**
   *
   */
  protected CADILog log = null;

  /**
   *
   */
  protected HashMap<Integer, JPEG2KCodestream> codestreams =
          new HashMap<Integer, JPEG2KCodestream>();

  /**
   * Default length of the {@link #tid}.
   */
  protected final int TID_DEFAULT_LENGTH = 255;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   */
  public JPEG2KLogicalTarget() {
    // Generate the unique target identifier
    generateTid();
  }

  /**
   * Constructor.
   *
   * @param cache definition in {@link #cache}.
   * @param log definition in {@link #log}.
   */
  public JPEG2KLogicalTarget(String tid, CADILog log) {
    // Check input parameters
    if (tid == null) throw new NullPointerException();
    if (log == null) throw new NullPointerException();

    // Copy input parameters
    this.tid = tid;
    this.log = log;
  }

  /**
   * Returns the {@link #tid} attribute.
   *
   * @return definition in {@link #tid}.
   */
  public final String getTID() {
    return tid;
  }

  /**
   * Returns the {@link #codestream} attribute.
   *
   * @return the {@link #codestream} attribute.
   */
  public JPEG2KCodestream getCodestream(int index) {
    return codestreams.get(index);
  }

  /**
   *
   * @param inClassIdentifier
   * @param dataBinLength
   *
   * @return
   */
  // FIXME:  The name of this method must be numOfPacketsCompleted
  public int getLastCompleteLayer(long inClassIdentifier, long dataBinLength) {
    return -1;
  }
	
	
	/**
	 * Returns the length of a packet. 
	 * 
	 * @param inClassIdentifier definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}.
	 * @param layer the number of the layer.
	 * 
	 * @return the length of the packet.
	 */
	public int getPacketLength(long inClassIdentifier, int layer) {
    return -1;
  }
	
   /**
   * Returns the length of the data bin of which unique identifier is <data>
   * inClassIdentifier</data>.
   *
   * @param inClassIdentifier
   *          definition in
   *          {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}
   *          .
   *
   * @return the length of the data bin.
   */
  public long getDataLength(long inClassIdentifier) {
    return -1;
  }

  /**
   * Returns the offset of a packet in the data bin. Packet is identified
   * by means of its unique identifier <data>inClassIdentifier</data> and its
   * number of layer.
   *
   * @param inClassIdentifier definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}.
   * @param layer the layer which offset is requested
   *
   * @return the offset of the packet in the data bin.
   */
  public int getPacketOffsetWithDataBin(long inClassIdentifier, int layer) {
    return -1;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str += "tid=" + tid;
    for (Map.Entry<Integer, JPEG2KCodestream> entry : codestreams.entrySet()) {
      str += entry.getValue().toString();
    }

    return str;
  }

  /**
   * Prints the JPC logical target data out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    /* out.println("-- JPEG2K Logical Target --");
     *
     * out.println("tid: "+tid);
     * for (Map.Entry<Integer, JPEG2KCodestream> entry : codestreams.entrySet()) {
     * entry.getValue().list(out);
     * } */

    out.flush();
  }

  // =========================== protected methods =============================
  /**
   * It will generate a Target-ID for that logical target, i.e. a string that
   * absolutely identifies the logical target. The "Target-ID" shall not
   * exceed 255 character in length.
   * <p>
   * This a generic function, but it is recommended classes that inherits from
   * this one should implement their own methods.
   * <p>
   * NOTICE: This method must be called from the constructor of the class.
   *
   * @see #tid
   */
  protected void generateTid() {
    generateTid(TID_DEFAULT_LENGTH);
  }

  /**
   * This method is used to generate a target identifier for the this logical
   * target, i.e. a string that absolutely identifies the logical target. The
   * "Target-ID" shall not exceed 255 character in length.
   * <p>
   * This a generic function, but classes that inherits from this one can
   * implement their own methods.
   * <p>
   * NOTICE: This method must be called from the constructor of the class.
   *
   * @param length the length of the identifier.
   *
   * @see #tid
   */
  protected void generateTid(int length) {

    if ((length <= 0) || (length >= 256)) {
      throw new IllegalArgumentException("Length value must be between 1 and 255");
    }

    tid = UUID.randomUUID().toString();
  }
  // ============================ private methods ==============================
}