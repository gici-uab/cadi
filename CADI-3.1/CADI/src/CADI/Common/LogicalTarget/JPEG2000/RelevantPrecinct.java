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
package CADI.Common.LogicalTarget.JPEG2000;

import java.io.PrintStream;

/**
 * This class is an auxiliary container to keep status information about
 * precincts.
 * <p>
 * The class is used by the WindowScalingFactor class.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.2 2011/01/18
 */
public class RelevantPrecinct {

  /**
   * Definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#
   * inClassIdentifier}
   */
  public long inClassIdentifier = -1L;

  public int tile = -1;

  public int component = -1;

  public int rLevel = -1;

  public int precinct = -1;

  /**
   * Percentage of pixels belonging to the precinct that also lay in the WOI.
   * It is a value in the range 0 to 1.
   */
  public float overlapFactor = -1F;

  /**
   * Window Scaling Factor.
   */
  public float wsf = 0F;

  /**
   * A temporary attribute. It is only used to record whether a precinct has
   * been visited when it is passed from a temporary list to a permanent.
   */
  public boolean visited = false;

  /**
   * Records the first layer of this precinct to be included.
   */
  public int startLayer = 0;

  /**
   * Records the last layer of this precinct to be taken into account.
   * <p>
   * OBS: this layer is not included.
   */
  public int endLayer = 0;

  /**
   * Records the packet/layer that is being taken into account.
   */
  public int actualNumPacket = 0;
  // OBS: this attribute can be joined with the endLayer performing some
  // changes in the WSF algorithm.
  // This attribute name must be actualLayer (similar to startLayer and endLayer)

  /**
   * Its meaning is the same as {@link #actualNumPacket} but for number of
   * bytes.
   */
  public int actualNumBytes = 0;

  /**
   * State attribute to save the number of packets that has been previously
   * sent to the client.
   * <p>
   * OBS: this attribute will be removed in the next revision since its value
   * can be calculated using the cache server and the actualNumPackets.
   */
  public int numPacketsInClientCache = -1;

  /**
   * It is similar to the {@link #numPacketsInClientCache} but for the number
   * of bytes.
   * <p>
   * OBS: this attribute will be removed in the next revision since its value
   * can be calculated using the cache server and the actualNumBytes.
   */
  public int numBytesInClientCache = -1;

  /**
   * Estimation of the JPIP message length.
   * Temporary variable, it is used in the Window Scaling Factor to keep the
   * length of the JPIP message.
   */
  public int jpipMessageHeaderLength = 0;

  /**
   * Saves the offset of the message to be transmitted.
   */
  public long msgOffset = -1;

  /**
   * Saves the cumulative length of the message to be transmitted.
   */
  public long msgLength = 0;
  
  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param inClassIdentifier definition in {@link #inClassIdentifier}.
   */
  public RelevantPrecinct(long inClassIdentifier) {
    this.inClassIdentifier = inClassIdentifier;
  }

  /**
   * Constructor (copy).
   *
   * @param relevantPrecinct an object of this class.
   */
  public RelevantPrecinct(RelevantPrecinct relevantPrecinct) {
    this(relevantPrecinct.inClassIdentifier);

    overlapFactor = relevantPrecinct.overlapFactor;
    startLayer = relevantPrecinct.startLayer;
    actualNumPacket = relevantPrecinct.actualNumPacket;
    actualNumBytes = relevantPrecinct.actualNumBytes;
    numPacketsInClientCache = relevantPrecinct.numPacketsInClientCache;
    numBytesInClientCache = relevantPrecinct.numBytesInClientCache;
    jpipMessageHeaderLength = relevantPrecinct.jpipMessageHeaderLength;
    msgOffset = relevantPrecinct.msgOffset;
    msgLength = relevantPrecinct.msgLength;
    visited = relevantPrecinct.visited;
  }

  /**
   * Sets attributes to their initial values.
   */
  public void reset() {
    inClassIdentifier = -1L;
    tile = -1;
    component = -1;
    rLevel = -1;
    precinct = -1;
    overlapFactor = -1F;
    wsf = 0F;
    visited = false;
    startLayer = 0;
    endLayer = 0;
    actualNumPacket = 0;
    actualNumBytes = 0;
    numPacketsInClientCache = -1;
    numBytesInClientCache = -1;
    jpipMessageHeaderLength = 0;
    msgOffset = -1;
    msgLength = 0;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toStringSummary() {
    String str = "";
    str += getClass().getName() + " [";
    str += "ID=" + inClassIdentifier
            + ", overlapFactor=" + overlapFactor
            + ", windowScalingFactor=" + wsf
            + ", startLayer=" + startLayer
            + ", endLayer=" + endLayer;
    str += "]";

    return str;
  }

  public void listDebug(PrintStream out) {
    out.println("In class identifier: " + inClassIdentifier);
    out.println("\tActual num. packet: " + actualNumPacket + " Actual num. bytes: " + actualNumBytes);
    out.println("\tNum. packets in client cache: " + numPacketsInClientCache + " Num. bytes in client cache: " + numBytesInClientCache);

    out.println("\tStart layer: " + startLayer + " End layer: " + endLayer);

    out.println("\tOverlap factor: " + overlapFactor + " windowScalingFactor=" + wsf);

    //out.println("JPIP Message header length: "+jpipMessageHeaderLength);
    //out.println("Message offset: "+msgOffset);
    //out.println("Message length: "+msgLength);
    //out.println("Visited: "+visited);
    //out.println("File pointers / lengths: ");
    //for (int i = 0; i < filePointers.size(); i++)
    //out.println("\t"+filePointers.get(i)+" / "+lengths.get(i));
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";
    str += getClass().getName() + " [";

    str += "In class identifier=" + inClassIdentifier;
    str += ", Overlap factor=" + overlapFactor;
    str += ", Start layer=" + startLayer;
    str += ", End layer=" + endLayer;
    str += ", Actual num. packet=" + actualNumPacket;
    str += ", Actual num. bytes=" + actualNumBytes;
    str += ", Num. packets in client cache=" + numPacketsInClientCache;
    str += ", Num. bytes in client cache=" + numBytesInClientCache;
    str += ", JPIP Message header length=" + jpipMessageHeaderLength;
    str += ", Message offset=" + msgOffset;
    str += ", Message length=" + msgLength;
    str += ", Visited=" + visited;
    str += "]";

    return str;
  }

  /**
   * Prints this Relevant Precinct out to the specified output stream. This method
   * is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Relevant Precinct --");

    out.println("In class identifier: " + inClassIdentifier);
    out.println("Overlap factor: " + overlapFactor);
    out.println("Window scaling factor: " + wsf);
    out.println("Start layer: " + startLayer);
    out.println("End layer: " + endLayer);
    out.println("Actual num. packet: " + actualNumPacket);
    out.println("Actual num. bytes: " + actualNumBytes);
    out.println("Num. packets in client cache: " + numPacketsInClientCache);
    out.println("Num. bytes in client cache: " + numBytesInClientCache);
    out.println("JPIP Message header length: " + jpipMessageHeaderLength);
    out.println("Message offset: " + msgOffset);
    out.println("Message length: " + msgLength);
    out.println("Visited: " + visited);
  }

}
