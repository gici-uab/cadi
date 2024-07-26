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
package CADI.Common.Cache;

import java.io.PrintStream;
import java.util.ArrayList;

import CADI.Common.Network.JPIP.ClassIdentifiers;
import java.io.EOFException;

/**
 * Extends the DataBin class to support precinct-based data bins.
 * <p>
 * Further information, see ISO/IEC 15444-9 section A.2
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.7 2012/03/12
 */
public class PrecinctDataBin extends DataBin {

  /**
   * Definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}
   */
  private long inClassIdentifier = -1;

  /**
   * Object containing the stream of data.
   */
  private DataBinStream dataStream = null;

  /**
   * An array list which indicates the last full layer achieved with the data
   * stored in the <code>dataBin</code> array list.  A value of 0 indicates
   * that either there is not data saved for this precinct or the server does
   * not signal the layer which data belongs (i.e. Aux parameter in jpip
   * message header has not been used).
   * <p>
   * Definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#Aux}
   */
  private ArrayList<Integer> numCompletedPackets = null;

  /**
   * Records the cumulated lengths of the completed packets.
   */
  private ArrayList<Integer> lengths = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param inClassIdentifier definition in {@link #inClassIdentifier}.
   */
  public PrecinctDataBin(long inClassIdentifier) {
    super(ClassIdentifiers.PRECINCT);

    if (inClassIdentifier < 0) {
      throw new IllegalArgumentException();
    }

    this.inClassIdentifier = inClassIdentifier;

    dataStream = new DataBinStream();
    numCompletedPackets = new ArrayList<Integer>();
    lengths = new ArrayList<Integer>();
  }

  /**
   * Constructor.
   * <p>
   * Passing the number of layers allows a capacity initialization of the stream
   * used to store precinct data bin. Then, performance is improved.
   *
   * @param inClassIdentifier definition in {@link #inClassIdentifier}.
   * @param numLayers
   */
  public PrecinctDataBin(long inClassIdentifier, int numLayers) {
    super(ClassIdentifiers.PRECINCT);

    if (inClassIdentifier < 0) {
      throw new IllegalArgumentException();
    }
    if (numLayers < 0) {
      throw new IllegalArgumentException();
    }

    this.inClassIdentifier = inClassIdentifier;

    dataStream = new DataBinStream(numLayers);
    numCompletedPackets = new ArrayList<Integer>(numLayers);
    lengths = new ArrayList<Integer>(numLayers);
  }

  /**
   * Returns the {@link #inClassIdentifier} attribute.
   * @return the {@link #inClassIdentifier} attribute.
   */
  public long getInClasIdentifier() {
    return inClassIdentifier;
  }

  /**
   * Adds a precinct data-bin message to the cache.
   * <p>
   * This method can only be used when there were no gaps between consecutive
   * message data. Otherwise, and exception is thrown.
   *
   * @param data definition in {@link CADI.Common.Network.JPIP.JPIPMessage#messageBody}
   * @param offset definition in {@linkplain CADI.Common.Network.JPIP.JPIPMessageHeader#msgOffset}
   * @param complete definition in {@linkplain CADI.Common.Network.JPIP.JPIPMessageHeader#isLastByte}
   * @param numCompletedPackets the last full layer (if this information is provived). Otherwise, it is 0.
   */
  public void addStream(byte[] data, long offset, boolean complete,
                        int numCompletedPackets) {

    if (data == null) {
      return;
    }

    assert (lengths.size() == this.numCompletedPackets.size());

    lock();
    try {
      dataStream.addStream(data, offset);
      //if (complete) dataStream.trimToSize();
      this.complete = complete;
      if (numCompletedPackets > 0) {
        saveCompletedPacketsAndLengths(numCompletedPackets, (int) offset + data.length);
      }
    } catch (UnsupportedOperationException uoe) {
      throw new UnsupportedOperationException();
    } finally {
      unlock();
    }

    assert (lengths.size() == this.numCompletedPackets.size());
  }

  /**
   * Returns the number of completed packets.
   * <p>
   * Returned value could not be the actual number of completed packets because
   * it is updated using the JPIP message's auxiliary information. Then, only
   * when the server sends this information is this value updated correctly.
   * <p>
   *
   * @return number of completed packets.
   */
  public int getNumCompletePackets() {
    return (!numCompletedPackets.isEmpty())
            ? numCompletedPackets.get(numCompletedPackets.size() - 1) : 0;
  }

  /**
   * Returns <code>true</code> if the packet <code>layer</code> is enterely in
   * the cache.  <code>layer</code> values are from 1 to max. number of layers.
   *
   * @param layer number of the packet/layer.
   *
   * @return <code>true</code> if it is cached. Otherwise, returns <code>false
   *          </code>
   */
  public boolean isPacketCompleted(int layer) {
    return (layer <= getNumCompletePackets()) ? true : false;
  }

  /**
   * Returns the last completed layer achieved at the byte <code>dataBinLength
   * <code>.
   * 
   * @param dataBinLength
   * 
   * @return the last completed layer.
   */
  public int getLastCompleteLayer(long dataBinLength) {

    int lastCompletedLayer = 0;
    long length = getLength();

    if (lengths.isEmpty() || (length == 0) || dataBinLength == 0) {
      return 0;
    }
    
    if (dataBinLength > length) {
      lastCompletedLayer = getNumCompletePackets();
    } else {
      int nChunks = lengths.size();
      int index = 0;
      while ((index < nChunks) && (dataBinLength >= lengths.get(index))) {
        lastCompletedLayer = numCompletedPackets.get(index);
        index++;
      }
    }

    return lastCompletedLayer;
  }

  public int getLastCompleteLayerNew(long dataBinLength) {

    int lastCompletedLayer = 0;
    long length = lengths.get(lengths.size() - 1);

    if (lengths.isEmpty() || (length == 0) || dataBinLength == 0) {
      return 0;
    }

    if (dataBinLength >= length) {
      return getNumCompletePackets();
    }

    int nChunks = lengths.size();
    int index = 0;
    while ((index < nChunks) && (lengths.get(index) <= dataBinLength)) {
      lastCompletedLayer = numCompletedPackets.get(index);
      index++;
    }

    int ly1 = index == 0 ? 0 : numCompletedPackets.get(index - 1);
    int ly2 = numCompletedPackets.get(index);
    lastCompletedLayer = ly1;
    if (ly1 != ly2 - 1) { // Packets have been joined, then estimate
      while ((ly1 <= ly2) && (getPacketOffset(ly1 + 1) <= dataBinLength)) {
        lastCompletedLayer = ly1;
        ly1++;
      }
    }

    return lastCompletedLayer;
  }

  /**
   * Returns the length of the packet <code>layer</code>. <code>layer</code>
   * values are from 1 to max. number of layers.
   * 
   * @param layer is the packet/layer in the precinct stream.
   * @return length of the packet/layer.
   */
  public int getPacketLength(int layer) {
    assert (layer > 0);

    if (numCompletedPackets.isEmpty() || (layer > getNumCompletePackets())) {
      return 0; // TODO: better to return -1 because it does not exist
    }

    return getPacketOffset(layer + 1) - getPacketOffset(layer);
  }

  /**
   * Returns the offset in the data bin of the packet <code>layer</code>.
   * <code>layer</code> values are from 1 to max. number of layers.
   *
   * @param layer is the packet/layer in the precinct stream.
   * @return offset of the packet/layer in the precinct stream.
   */
  public int getPacketOffset(int layer) {
    assert (layer > 0);

    if (layer == 1) {
      return 0;
    }

    if (numCompletedPackets.isEmpty() || (layer > getNumCompletePackets() + 1)) {
      return 0; // TODO: better to return -1 because it does not exist
    }

    // TODO: search algorithm is sequential, a binary search should be done.
    int idx1 = 0;
    int numChunks = lengths.size();
    while ((idx1 < numChunks) && (numCompletedPackets.get(idx1) < (layer - 1))) {
      idx1++;
    }

    int offset = 0;
    if (numCompletedPackets.get(idx1) == (layer - 1)) {
      // Value is explicity in the list
      offset = lengths.get(idx1);
    } else {
      // Value is no in the list. It is estimated
      int len2 = lengths.get(idx1);
      int ly2 = numCompletedPackets.get(idx1) + 1;
      int len1 = idx1 == 0 ? 0 : lengths.get(idx1 - 1);
      int ly1 = idx1 == 0 ? 1 : numCompletedPackets.get(idx1 - 1) + 1;
      // Approximation by a first order function
      offset = (int) (((1.0 * (len2 - len1)) / (ly2 - ly1)) * (layer - ly1) + len1);
    }

    return offset;
  }

  /**
   * Definition in {@link CADI.Common.Cache.DataBinStream#getLength()}.
   *
   * @return length of the data stream stored.
   */
  public long getLength() {
    return dataStream.getLength();
  }

  /**
   * Definition in {@link CADI.Common.Cache.DataBinStream#seek(long)}.
   * 
   * @param offset
   */
  public void seek(long offset) {
    dataStream.seek(offset);
  }

  /**
   * Definition in {@link CADI.Common.Cache.DataBinStream#skipBytes(int) }.
   * 
   * @param n
   * @return
   */
  public int skipBytes(int n) {
    return dataStream.skipBytes(n);
  }

  /**
   * Definition in {@link CADI.Common.Cache.DataBinStream#getPos() }.
   *
   * @return
   */
  public long getPos() {
    return dataStream.getPos();
  }

  /**
   * Definition in {@link CADI.Common.Cache.DataBinStream#getNumBytesLeft() }.
   * @return
   */
  public long getNumBytesLeft() {
    return dataStream.getNumBytesLeft();
  }

  /**
   * 
   * @return
   */
  public long available() {
    return getNumBytesLeft();
  }

  /**
   * Definition in {@link CADI.Common.Cache.DataBinStream#readByte() }.
   * 
   * @return
   * @throws EOFException
   */
  public byte readByte() throws EOFException {
    return dataStream.readByte();
  }

  /**
   * Definition in {@link CADI.Common.Cache.DataBinStream#readFully(byte[]) }.
   * @param b
   */
  public void readFully(byte[] b) {
    dataStream.readFully(b);
  }

  /**
   * Definition in {@link CADI.Common.Cache.DataBinStream#readFully(byte[], int, int) }.
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
    dataStream.reset();
    inClassIdentifier = -1;
    numCompletedPackets.clear();
    lengths.clear();
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
    str += ", inClassIdentifier=" + inClassIdentifier;
    str += dataStream.toString();
    str += ", {layers, lengths}=";
    if (this.lengths != null) {
      for (int i = 0; i < lengths.size(); i++) {
        str += "{" + numCompletedPackets.get(i) + "," + lengths.get(i) + "},";
      }
    } else {
      str += " < no data available> ";
    }
    str += ", complete: " + complete;

    str += "]";

    return str;
  }

  public String toStringShort() {

    String str = "";
    str = getClass().getName() + " [";
    str += "classIdentifier=";
    switch (classIdentifier) {
      case PRECINCT:
        str += "precinct data-bin";
        break;
      case TILE_HEADER:
        str += "tile header data-bin";
        break;
      case TILE:
        str += "tile data-bin";
        break;
      case MAIN_HEADER:
        str += "main header data-bin";
        break;
      case METADATA:
        str += "metadata data-bin";
        break;
      default:
        assert (true);
    }
    str += ", inClassIdentifier=" + inClassIdentifier;
    str += ", complete: " + complete;
    str += ", {layers, lengths}=";
    if (this.lengths != null) {
      for (int i = 0; i < lengths.size(); i++) {
        str += "{" + numCompletedPackets.get(i) + "," + lengths.get(i) + "},";
      }
    } else {
      str += " < no data available> ";
    }
    str += ", complete: " + complete;

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
    out.println("-- Precinct Data Bin --");

    out.println("classIdentifier: precinct data-bin");
    out.println("inClassIdentifier: " + inClassIdentifier);
    dataStream.list(out);
    out.print("layers, lengths: ");
    if (this.lengths != null) {
      for (int i = 0; i < lengths.size(); i++) {
        out.println("{" + numCompletedPackets.get(i) + ", " + lengths.get(i) + "} ");
      }
    } else {
      out.println(" < no data available > ");
    }
    out.println("complete: " + complete);

    out.flush();
  }

  /**
   * Prints this DataBin out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void listData(PrintStream out) {
    out.println("-- Data Bin --");
    out.println("class: " + this.classIdentifier);
    out.println("inClassID: " + this.inClassIdentifier);
    dataStream.list(out);
    int numChunks = numCompletedPackets.size();
    for (int i = 0; i < numChunks; i++) {
      out.println("\tcompleted packet: " + numCompletedPackets.get(i));
    }
    out.flush();

  }

  // ============================ private methods ==============================
  /**
   * 
   * @param numPackets
   * @param length 
   */
  private void saveCompletedPacketsAndLengths(int completedPacket, int length) {
    int lastPacket = numCompletedPackets.isEmpty() ? 0
            : numCompletedPackets.get(numCompletedPackets.size() - 1);

    if (lastPacket > completedPacket) {
      throw new UnsupportedOperationException();
    }

    if ((lastPacket + 1) == completedPacket) {
      numCompletedPackets.add(completedPacket);
      lengths.add(length);
    } else if (lastPacket < completedPacket) {
      int lastLength = lengths.isEmpty() ? 0 : lengths.get(lengths.size() - 1);


      int len2 = length;
      int ly2 = completedPacket;
      int len1 = lastLength;
      int ly1 = lastPacket;
      // Approximation by a first order function
      for (int ly = ly1 + 1; ly <= ly2; ly++) {
        numCompletedPackets.add(ly);
        lengths.add((int) (((1.0 * (len2 - len1)) / (ly2 - ly1)) * (ly - ly1) + len1));
      }
    }
  }
}
