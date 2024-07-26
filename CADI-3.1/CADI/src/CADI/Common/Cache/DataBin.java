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
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is a container for JPEG2000 Data-bins. Data-bins contain portions
 * of a JPEG2000 file or codestream data, which can be based of precincts,
 * tiles, headers, or metadata.
 * <p>
 * Further information, see ISO/IEC 15444-9 section A.3
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.2 2011/09/06
 */
public class DataBin implements ClassIdentifiers {

  /**
   * Definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#classIdentifier}
   * <p>
   * Allowed values, see {@link CADI.Common.Network.JPIP.ClassIdentifiers}. 
   */
  protected int classIdentifier = -1;

  // TODO: if this attribute is included, MainHeaderDataBin and TileHeaderDataBin classes could be removed.
  //protected DataBinStream dataStream = null;
  /**
   * A boolean  which indicates whether all data for this data bin has been
   * received. 
   * <p>
   * Definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#isLastByte}
   */
  protected boolean complete = false;

  // INTERNAL ATTRIBUTES
  /**
   *
   */
  private ReentrantLock mutex = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   * 
   * @param inClassIdentifier definition in {@link #inClassIdentifier}.
   */
  public DataBin(int classIdentifier) {
    if ((classIdentifier < 0) || (classIdentifier > 8)) {
      throw new IllegalArgumentException();
    }

    this.classIdentifier = classIdentifier;
    mutex = new ReentrantLock();
  }

  /**
   * Returns the {@link #classIdentifier} attribute.
   *
   * @return the {@link #classIdentifier} attribute.
   */
  public int getClassIdentifier() {
    return classIdentifier;
  }

  public final boolean isComplete() {
    return complete;
  }

  /**
   * Set attributes to its initial values.
   */
  public void reset() {
    classIdentifier = -1;
    complete = false;
  }

  /**
   * (non-Javadoc)
   * @see java.util.concurrent.locks.ReentrantLock#lock()
   */
  public void lock() {
    mutex.lock();
  }

  /*
   * (non-Javadoc)
   * @see java.util.concurrent.locks.ReentrantLock#unlock()
   */
  public void unlock() {
    mutex.unlock();
  }

  /*
   * (non-Javadoc)
   * @see java.util.concurrent.locks.ReentrantLock#isLocked()
   */
  public boolean isLocked() {
    return mutex.isLocked();
  }

  /*
   * (non-Javadoc)
   * @see java.util.concurrent.locks.ReentrantLock#isHeldByCurrentThread()
   */
  public boolean isHeldByCurrentThread() {
    return mutex.isHeldByCurrentThread();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

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
  public void list(PrintStream out) {
    out.println("-- Data Bin --");

    out.print("classIdentifier: ");
    switch (classIdentifier) {
      case PRECINCT:
        out.println("precinct data-bin");
        break;
      case TILE_HEADER:
        out.println("tile header data-bin");
        break;
      case TILE:
        out.println("tile data-bin");
        break;
      case MAIN_HEADER:
        out.println("main header data-bin");
        break;
      case METADATA:
        out.println("metadata data-bin");
        break;
      default:
        assert (true);
    }

    out.println("complete: " + complete);

    out.flush();
  }
}