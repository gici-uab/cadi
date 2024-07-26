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

/**
 *
 * Further information, see ISO/IEC 15444-9 sect. C.8.1
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.2 2012/03/16
 */
public class ModelElement extends BinDescriptor {

  /**
   *
   */
  public boolean additive = true;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param form  allowed values are
   * {@link CADI.Common.Cache.BinDescriptor#EXPLICIT_FORM} or
   * {@link CADI.Common.Cache.BinDescriptor#IMPLICIT_FORM}.
   */
  public ModelElement(int form) {
    super(form);
    additive = true;
  }

  /**
   * Constructor based on the explicit form.
   *
   * @param classIdentifier definition in {@link CADI.Common.Cache.ExplicitBinDescriptor#classIdentifier}.
   * @param inClassIdentifier definition in {@link CADI.Common.Cache.ExplicitBinDescriptor#inClassIdentifier}.
   * @param numberOfLayers definition in {@link CADI.Common.Cache.ExplicitBinDescriptor#numberOfLayers}
   * @param numberOfBytes definition in {@link CADI.Common.Cache.ExplicitBinDescriptor#numberOfBytes}.
   * @param additive definition in {@link #additive}.
   */
  public ModelElement(int classIdentifier, long inClassIdentifier,
          int numberOfLayers, int numberOfBytes, boolean additive) {
    super(classIdentifier, inClassIdentifier, numberOfLayers, numberOfBytes);

    this.additive = additive;
  }

  /**
   * Constructor based on the implicit form.
   *
   * @param firstTilePos definition in {@link CADI.Common.Cache.ImplicitBinDescriptor#firstTilePos}.
   * @param lastTilePos definition in {@link CADI.Common.Cache.ImplicitBinDescriptor#lastTilePos}.
   * @param firstComponentPos definition in {@link CADI.Common.Cache.ImplicitBinDescriptor#firstComponentPos}.
   * @param lastComponentPos definition in {@link CADI.Common.Cache.ImplicitBinDescriptor#lastComponentPos}.
   * @param firstResolutionLevelPos definition in {@link CADI.Common.Cache.ImplicitBinDescriptor#firstResolutionLevelPos}.
   * @param lastResolutionLevelPos definition in {@link CADI.Common.Cache.ImplicitBinDescriptor#lastResolutionLevelPos}.
   * @param firstPrecinctPos definition in {@link CADI.Common.Cache.ImplicitBinDescriptor#firstPrecinctPos}.
   * @param lastPrecinctPos definition in {@link CADI.Common.Cache.ImplicitBinDescriptor#lastPrecinctPos}.
   * @param numberOfLayers definition in {@link CADI.Common.Cache.ImplicitBinDescriptor#numberOfLayers}.
   * @param additive definition in {@link #additive}.
   */
  public ModelElement(int firstTilePos, int lastTilePos,
          int firstComponentPos, int lastComponentPos,
          int firstResolutionLevelPos, int lastResolutionLevelPos,
          int firstPrecinctPos, int lastPrecinctPos,
          int numberOfLayers, boolean additive) {
    super(firstTilePos, lastTilePos,
            firstComponentPos, lastComponentPos,
            firstResolutionLevelPos, lastResolutionLevelPos,
            firstPrecinctPos, lastPrecinctPos,
            numberOfLayers);

    this.additive = additive;
  }

  /**
   * Sets the attritutes to its initial values.
   */
  @Override
  public void reset() {
    super.reset();
    additive = true;
  }

  /**
   * For debugging purposes.
   */
  @Override
  public String toString() {
    String str = "";
    str = getClass().getName() + " [";
    str += additive ? "Additive" : "Subtractive";
    str += ", " + super.toString();
    str += "]";
    return str;
  }

  /**
   * Prints this Cache Descriptor fields out to the
   * specified output stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Model Element --");

    super.list(out);
    out.println(additive ? "Additive" : "Sustractive");

    out.flush();
  }
}
