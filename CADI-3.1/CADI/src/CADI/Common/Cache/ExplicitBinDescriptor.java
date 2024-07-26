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
 * This class is used as a container for the explicit-bin-descriptor.
 * <p>
 * Information about data-bins can be set using the {@link #numberOfBytes} or/
 * and {@link #numberOfLayers} attributes.
 * <p>
 * If both attributes are {@link CADI.Common.Cache.BinDescriptor#WILDCARD} a
 * wildcard is represented, but never can be only one of the attributes.
 * <p>
 * Further information, see ISO/IEC 15444-9 sect. C.8.1.2
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.2 2012/05/17
 */
public class ExplicitBinDescriptor implements ClassIdentifiers {

  /**
   * Indicates the data-bin class.
   * <p>
   * Allowed values, see {@link CADI.Common.Network.JPIP.ClassIdentifiers}.
   *
   * OBS: Cambiar la clase DataBinClass para que solo tenga estes valores, y entonces,
   * los valores EXTENDED activar una opcion en las correspondientes funciones donde
   * se indique que es un valor EXTENDED.
   */
  public int classIdentifier = -1;

  /**
   * Definition in
   * {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}.
   */
  public long inClassIdentifier = -1;

  /**
   * Indicates the number of layers (packets)
   * <p>
   * Default value is -1
   */
  public int numberOfLayers = -1;

  /**
   * Indicates the number of bytes.
   * <p>
   * Default value is -1.
   */
  public int numberOfBytes = -1;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public ExplicitBinDescriptor() {
    //reset();
  }

  /**
   * Constructor.
   *
   * @param classIdentifier definition in {@link #classIdentifier}.
   * @param inClassIdentifier definition in {@link #inClassIdentifier}.
   */
  public ExplicitBinDescriptor(int classIdentifier, long inClassIdentifier) {
    this.classIdentifier = classIdentifier;
    this.inClassIdentifier = inClassIdentifier;
  }

  /**
   * Constructor.
   *
   * @param classIdentifier definition in {@link #classIdentifier}.
   * @param inClassIdentifier definition in {@link #inClassIdentifier}.
   * @param numberOfLayers definition in {@link #numberOfLayers}
   * @param numberOfBytes definition in {@link #numberOfBytes}.
   */
  public ExplicitBinDescriptor(int classIdentifier,
                               long inClassIdentifier,
                               int numberOfLayers,
                               int numberOfBytes) {
    this.classIdentifier = classIdentifier;
    this.inClassIdentifier = inClassIdentifier;
    this.numberOfLayers = numberOfLayers;
    this.numberOfBytes = numberOfBytes;
  }

  /**
   * Constructor.
   *
   * @param descriptor is an object of this class.
   */
  public ExplicitBinDescriptor(ExplicitBinDescriptor descriptor) {
    this.classIdentifier = descriptor.classIdentifier;
    this.inClassIdentifier = descriptor.inClassIdentifier;
    this.numberOfLayers = descriptor.numberOfLayers;
    this.numberOfBytes = descriptor.numberOfBytes;
  }

  public boolean isWildcard() {
    return (numberOfLayers == BinDescriptor.WILDCARD && numberOfBytes == BinDescriptor.WILDCARD)
            ? true : false;
  }

  public void setWilcard() {
    numberOfLayers = BinDescriptor.WILDCARD;
    numberOfBytes = BinDescriptor.WILDCARD;
  }

  /**
   * Sets the attritutes to its initial values.
   */
  public void reset() {
    classIdentifier = -1;
    inClassIdentifier = -1;
    numberOfLayers = -1;
    numberOfBytes = -1;
  }

  /**
   * For debugging purposes.
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";

    str += "Class=";
    switch (classIdentifier) {
      case PRECINCT:
        str += "precinct";
        break;
      case TILE_HEADER:
        str += "tile header";
        break;
      case TILE:
        str += "tile";
        break;
      case MAIN_HEADER:
        str += "main header";
        break;
      case METADATA:
        str += "metadata";
        break;
    }

    str += ", Identifier=" + (inClassIdentifier >= 0 ? inClassIdentifier : '*');

    if (numberOfLayers >= 0) {
      str += ", qualifier=" + numberOfLayers + " layers";
    } else if (numberOfBytes >= 0) {
      str += ", qualifier=" + numberOfBytes + " bytes";
    } else {
      str += ", qualifier=*";
    }

    str += "]";

    return str;
  }

  /**
   * Prints this Explicit Bin Descriptor fields out to the
   * specified output stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Explicit Bin Descriptor --");

    out.print("Class: ");
    switch (classIdentifier) {
      case PRECINCT:
        out.println("precinct");
        break;
      case TILE_HEADER:
        out.println("tile header");
        break;
      case TILE:
        out.println("tile");
        break;
      case MAIN_HEADER:
        out.println("main header");
        break;
      case METADATA:
        out.println("metadata");
        break;
    }

    out.println("Identifier: "
            + (inClassIdentifier >= 0 ? inClassIdentifier : "*"));

    if (numberOfLayers >= 0) {
      out.println("qualifier=" + numberOfLayers + " layers");
    } else if (numberOfBytes >= 0) {
      out.println("qualifier=" + numberOfBytes + " bytes");
    } else {
      out.println("qualifier=*");
    }

    out.flush();
  }
}
