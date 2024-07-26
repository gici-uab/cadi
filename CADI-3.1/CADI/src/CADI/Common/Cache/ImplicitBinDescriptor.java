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

/**
 * This class is used as a container for the implicit-bin-descriptor.
 * <p>
 * Information about data-bins can be set using the {@link #numberOfLayers}
 * attribute.
 * <p>
 * Regarding the ranges (first and last attributes) are both
 * {@link CADI.Common.Cache.BinDescriptor#WILDCARD} it means a wilcard.
 * <p>
 * The meaning of {@link #numberOfLayers} is a wildcard if is
 * {@link CADI.Common.Cache.BinDescriptor#WILDCARD}.
 * <p>
 * Further information, see ISO/IEC 15444-9 sect. C.8.1.3
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/03/06
 */
public class ImplicitBinDescriptor implements ClassIdentifiers {

  /**
   *
   */
  public int firstTilePos = -1;

  /**
   *
   */
  public int lastTilePos = -1;

  /**
   *
   */
  public int firstComponentPos = -1;

  /**
   *
   */
  public int lastComponentPos = -1;

  /**
   *
   */
  public int firstResolutionLevelPos = -1;

  /**
   *
   */
  public int lastResolutionLevelPos = -1;

  /**
   *
   */
  public int firstPrecinctPos = -1;

  /**
   *
   */
  public int lastPrecinctPos = -1;

  /**
   * Indicates the number of layers (packets).
   */
  public int numberOfLayers = 0;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public ImplicitBinDescriptor() {
    //reset();
  }

  /**
   * Constructor.
   *
   * @param implicitForm is an object of this class.
   */
  public ImplicitBinDescriptor(ImplicitBinDescriptor implicitForm) {
    this(implicitForm.firstTilePos, implicitForm.lastTilePos,
            implicitForm.firstComponentPos, implicitForm.lastComponentPos,
            implicitForm.firstResolutionLevelPos, implicitForm.lastResolutionLevelPos,
            implicitForm.firstPrecinctPos, implicitForm.lastPrecinctPos,
            implicitForm.numberOfLayers);
  }

  /**
   * Constructor.
   *
   * @param firstTilePos
   * @param lastTilePos
   * @param firstComponentPos
   * @param lastComponentPos
   * @param firstResolutionLevelPos
   * @param lastResolutionLevelPos
   * @param firstPrecinctPos
   * @param lastPrecinctPos
   * @param numberOfLayers
   */
  public ImplicitBinDescriptor(int firstTilePos,
                               int lastTilePos,
                               int firstComponentPos,
                               int lastComponentPos,
                               int firstResolutionLevelPos,
                               int lastResolutionLevelPos,
                               int firstPrecinctPos,
                               int lastPrecinctPos,
                               int numberOfLayers) {

    this.firstTilePos = firstTilePos;
    this.lastTilePos = lastTilePos;
    this.firstComponentPos = firstComponentPos;
    this.lastComponentPos = lastComponentPos;
    this.firstResolutionLevelPos = firstResolutionLevelPos;
    this.lastResolutionLevelPos = lastResolutionLevelPos;
    this.firstPrecinctPos = firstPrecinctPos;
    this.lastPrecinctPos = lastPrecinctPos;
    this.numberOfLayers = numberOfLayers;
  }

  /**
   * Sets the attritutes to its initial values.
   */
  public void reset() {
    firstTilePos = -1;
    lastTilePos = -1;
    firstComponentPos = -1;
    lastComponentPos = -1;
    firstResolutionLevelPos = -1;
    lastResolutionLevelPos = -1;
    firstPrecinctPos = -1;
    lastPrecinctPos = -1;
    numberOfLayers = 0;
  }

  /**
   * For debugging purposes.
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";

    str += "fistTilePos="
            + (firstTilePos == BinDescriptor.WILDCARD ? '*' : firstTilePos)
            + ", lastTilePos="
            + (lastTilePos == BinDescriptor.WILDCARD ? '*' : lastTilePos);
    str += " ,firstComponentPos="
            + (firstComponentPos == BinDescriptor.WILDCARD ? '*' : firstComponentPos)
            + ", lastComponentPos="
            + (lastComponentPos == BinDescriptor.WILDCARD ? '*' : lastComponentPos);
    str += " ,firstResolutionLevelPos="
            + (firstResolutionLevelPos == BinDescriptor.WILDCARD ? '*' : firstResolutionLevelPos)
            + ", lastResolutionLevelPos="
            + (lastResolutionLevelPos == BinDescriptor.WILDCARD ? '*' : lastResolutionLevelPos);
    str += " ,firstPrecinctLevelPos="
            + (firstPrecinctPos == BinDescriptor.WILDCARD ? '*' : firstPrecinctPos)
            + ", lastPrecinctPos="
            + (lastPrecinctPos == -BinDescriptor.WILDCARD? '*' : lastPrecinctPos);
    str += " ,numberOfLayers="
            + (numberOfLayers == BinDescriptor.WILDCARD ? '*' : numberOfLayers);

    str += "]";

    return str;
  }

  /**
   * Prints this Cache Descriptor fields out to the
   * specified output stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Implicit Bin Descriptor --");

    out.println("fistTilePos: "
            + (firstTilePos == BinDescriptor.WILDCARD ? '*' : firstTilePos));
    out.println("lastTilePos: "
            + (lastTilePos == BinDescriptor.WILDCARD ? '*' : lastTilePos));
    out.println("firstComponentPos: "
            + (firstComponentPos == BinDescriptor.WILDCARD ? '*' : firstComponentPos));
    out.println("lastComponentPos: "
            + (lastComponentPos == BinDescriptor.WILDCARD ? '*' : lastComponentPos));
    out.println("firstResolutionLevelPos: "
            + (firstResolutionLevelPos == BinDescriptor.WILDCARD ? '*' : firstResolutionLevelPos));
    out.println("lastResolutionLevelPos: "
            + (lastResolutionLevelPos == BinDescriptor.WILDCARD ? '*' : lastResolutionLevelPos));
    out.println("firstPrecinctLevelPos: "
            + (firstPrecinctPos == BinDescriptor.WILDCARD ? '*' : firstPrecinctPos));
    out.println("lastPrecinctPos: "
            + (lastPrecinctPos == BinDescriptor.WILDCARD ? '*' : lastPrecinctPos));
    out.println("numberOfLayers: "
            + (numberOfLayers == BinDescriptor.WILDCARD ? '*' : numberOfLayers));

    out.flush();
  }
}
