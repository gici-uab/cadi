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
import java.util.ArrayList;
import java.util.Map;

import CADI.Common.Network.JPIP.JPIPMessage;

/**
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.3 2011/09/06
 */
public class CacheManagement extends DataBinsCacheManagement {

  /**
   * Indicates which is the maximum size (in bytes) which is allowed for the
   * cached data.
   */
  protected long maxCacheSize = Long.MAX_VALUE;

  /**
   * Indicates which is the cache management policy. If none policy has
   * been set, the cache size is no limited and it can be augmented
   * indefinitely.
   * <p>
   * Two cache management policies has been defined.
   * LRU (Last Recent Used): value, see {@link #LRU}
   * FIFO (First In, First Out): value see {@link #FIFO}
   */
  protected int managementPolicy = NONE;

  public static final int NONE = 0;

  public static final int LRU = 1;

  public static final int FIFO = 2;

  /**
   * Is a list used to save the data-bins identifiers. It can be used either
   * a LRU or a FIFO.
   */
  protected ArrayList<Long> dataBinsList = null;

  /**
   * Contains the data-bins which has been removed from the cache. It is
   * useful to get a list of the removed data-bins to send the server.
   */
  protected ArrayList<Long> removedDataBins = null;

  /**
   * This attributes is used to save the size of the cached data. Its value
   * must be shorter than {@link #maxCacheSize}.
   */
  protected long cacheSize = 0;
  
  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public CacheManagement() {
    this(null);
  }

  /**
   * This constructor must be used if the client cache is mapped to a file.
   * <p>
   * <B>OBS:<B>It is not being used yet.
   *
   * @param fileName
   */
  public CacheManagement(String fileName) {
    super();
  }

  /**
   * Sets the {@link #managementPolicy} attribute.
   *
   * @param managementPolicy definition in {@link #managementPolicy}.
   */
  public final void setManagementPolicy(int managementPolicy) {
    if ((managementPolicy < 0) || (managementPolicy > 2)) {
      throw new IllegalArgumentException("Wrong management policy value");
    }

    this.managementPolicy = managementPolicy;
  }

  /**
   * Returns the {@link #managementPolicy} attribute.
   *
   * @return the {@link #managementPolicy} attribute.
   */
  public final int getManagementPolicy() {
    return managementPolicy;
  }

  /**
   * Sets the {@link #maxCacheSize} attribute.
   *
   * @param maxCacheSize is the maximum size (in bytes) allowed for the
   * 			cached data. Only positive values are allowed, a value of 0
   * 			means unlimited.
   */
  public final void setMaxCacheSize(long maxCacheSize) {
    if (maxCacheSize < 0) {
      throw new IllegalArgumentException("Wrong maximum cache size value. Only positive values are allowed");
    }

    this.maxCacheSize = (maxCacheSize != 0) ? maxCacheSize : Long.MAX_VALUE;
  }

  /**
   * Returns the {@link #maxCacheSize} attribute.
   *
   * @return the {@link #maxCacheSize} attribute.
   */
  public final long getMaxCacheSize() {
    return maxCacheSize;
  }

  /*
   * (non-Javadoc)
   * @see CADI.Proxy.Cache.DataBinsCacheManagement#addJPIPMessage(CADI.Common.Network.JPIP.JPIPMessage)
   */
  @Override
  public void addJPIPMessage(JPIPMessage jpipMessage) {
    super.addJPIPMessage(jpipMessage);

    if (managementPolicy != NONE) {
      cacheSize += jpipMessage.messageBody.length;
    }
  }

 /**
   * Returns the number of layers (or number of completed packets) for the
   * given precinct data-bin that are stored in the cache.
   *
   * @param inClassIdentifier definition in {@linkplain CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}.
   *
   * @return the number of layers (or number of completed packets).
   */
  public final int getLastLayerOfPrecinctDataBin(long inClassIdentifier) {
    return (precinctsDataBins.get(inClassIdentifier) != null)
            ? precinctsDataBins.get(inClassIdentifier).getNumCompletePackets()
            : 0;
  }

  /**
   * 
   * @param inClassIdentifier
   * @return
   */
  public final long getPrecinctDataBinLength(long inClassIdentifier) {
    return (precinctsDataBins.get(inClassIdentifier) != null)
            ? precinctsDataBins.get(inClassIdentifier).getLength()
            : 0;
  }  
  
  /**
   * Returns the image main header if it is available, otherwise returns null.
   *
   * @return the main header if it is available.
   */
  public final byte[] getMainHeader() {
    if (mainHeaderDataBin.complete) {
      return mainHeaderDataBin.getDataArray();
    } else {
      return null;
    }
  }

  /**
   * Sets the attributes to its initial values.
   */
  @Override
  public void reset() {
    super.reset();
    if (dataBinsList != null) {
      dataBinsList.clear();
    }
    if (removedDataBins != null) {
      removedDataBins.clear();
    }
    cacheSize = 0;
    maxCacheSize = Long.MAX_VALUE;
    managementPolicy = NONE;

    //jpcParameters.reset();
    //jpcParameters = null;
  }

  /**
   * Clears the client cache but not remove the image parameters
   * (the {@link #jpcParameters} attribute).
   */
  public void clear() {
    super.reset();
  }

  /**
   * Check if the cache parameters has been initialized. If it was, the
   * cache is ready. Otherwise, it is not ready.
   *
   * @return <code>true</code> if the cache is ready. Otherwise, return
   * 			<code>false</code>.
   */
  public final boolean isReady() {
    return (codestream == null) ? false : true;
  }

  /**
   *
   *
   */
  public void manage() {

    if (managementPolicy == NONE) {
      return;
    }

    while ((cacheSize > maxCacheSize) && (dataBinsList.size() > 1)) {

      long inClassIdentifier = -1;

      if (managementPolicy == LRU) {
        inClassIdentifier = dataBinsList.get(0);
        dataBinsList.remove(0);
      } else if (managementPolicy == FIFO) {
        inClassIdentifier = dataBinsList.get(0);
        dataBinsList.remove(0);
      }

      if (inClassIdentifier >= 0) {
        cacheSize -= precinctsDataBins.get(inClassIdentifier).getLength();
        precinctsDataBins.get(inClassIdentifier).reset();
        precinctsDataBins.remove(inClassIdentifier);

        removedDataBins.add(inClassIdentifier);
      }
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

    // Main header
    str += "\nMain header={";
    str += "length=" + mainHeaderDataBin.getLength();
    str += ", complete=" + (mainHeaderDataBin.complete ? "yes" : "no") + "}";

    // Precinct data bin
    str += "\nPrecinct data-bins={";
    for (Map.Entry<Long, PrecinctDataBin> entry : precinctsDataBins.entrySet()) {
      str += "\n" + entry.getValue().toString();
    }
    str += "}";

    str += "]";

    return str;
  }

  /**
   * Prints this cache out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Cache Management --");

    // Main header
    out.println("Main header");
    out.println("   length: " + mainHeaderDataBin.getLength());
    out.println("   complete: " + (mainHeaderDataBin.complete ? "yes" : "no"));

    // Precinct data bin
    out.println("Precinct data-bins");
    for (Map.Entry<Long, PrecinctDataBin> entry : precinctsDataBins.entrySet()) {
      entry.getValue().list(out);
    }

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   *
   * @param classIdentifier
   * @param inClassIdentifier
   */
  private void updateLists(int classIdentifier, long inClassIdentifier) {

    if (classIdentifier != BinDescriptor.PRECINCT) {
      return;
    }

    if (managementPolicy == LRU) {
      int index = dataBinsList.indexOf(inClassIdentifier);
      if (index >= 0) {
        dataBinsList.remove(index);
      }
      dataBinsList.add(inClassIdentifier);

    } else if (managementPolicy == FIFO) {
      int index = dataBinsList.indexOf(inClassIdentifier);
      if (index < 0) {
        dataBinsList.add(inClassIdentifier);
      }
    }

  }

  /**
   *
   *
   */
  private byte[] ArrayListToArray(ArrayList<byte[]> src) {
    int length = 0;
    for (int i = src.size() - 1; i >= 0; i--) {
      length += src.get(i).length;
    }

    byte[] dst = new byte[length];
    int offset = 0;
    for (int i = 0; i < src.size(); i++) {
      byte[] data = src.get(i);
      System.arraycopy(data, 0, dst, offset, data.length);
      offset += data.length;
    }

    return dst;
  }

  /**
   * Useful method for printing out a ByteStream. Only for debugging purposes.
   *
   * @param buffer the byte array to be printed.
   */
  private static void printByteStream(byte[] buffer) {

    for (int index = 0; index < buffer.length; index++) {
      if ((0xFF & buffer[index]) < 16) {
        System.out.print("0");
      }
      System.out.print(Integer.toHexString(0xFF & buffer[index]));
    }
  }
}
