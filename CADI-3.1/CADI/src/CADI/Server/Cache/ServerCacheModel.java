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
package CADI.Server.Cache;

import java.io.PrintStream;
import java.util.ArrayList;

import CADI.Common.Cache.CacheModel;
import CADI.Common.Cache.ExplicitBinDescriptor;
import CADI.Common.Cache.ModelElement;
import CADI.Common.Network.JPIP.ClassIdentifiers;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Server.LogicalTarget.JPEG2000.JP2KServerLogicalTarget;

/**
 * This class implements a server cache model.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.2 2012/05/19
 */
public class ServerCacheModel extends CacheModel {

  /**
   * Is the logical target
   */
  private JP2KServerLogicalTarget logicalTarget = null;

  // INTERNAL ATTRIBUTES
  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param logicalTarget
   */
  public ServerCacheModel(JP2KServerLogicalTarget logicalTarget) {
    super(logicalTarget.getCodestream(0));

    // Check input parameters
    if (logicalTarget == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.logicalTarget = logicalTarget;
  }

  /**
   * Updates the server cache with data client has sent or with data sent
   * from the server to the client.
   */
  @Override
  public void update(ArrayList<ModelElement> descriptor) {
    update(descriptor, null);
  }

  /**
   * Updates the cache model allowing wildcards for precinct identifiers.
   * 
   * @param descriptor
   * @param woi 
   */
  public void update(ArrayList<ModelElement> descriptor, ViewWindowField woi) {
    if (descriptor == null) {
      return;
    }

    for (ModelElement cacheDescriptor : descriptor) {

      if (cacheDescriptor.implicitForm != null) {
        // IMPLICIT FORM
        super.update(cacheDescriptor);

      } else if (cacheDescriptor.explicitForm != null) { // EXPLICIT FORM

        switch (cacheDescriptor.explicitForm.classIdentifier) {

          case ClassIdentifiers.PRECINCT:

            if (cacheDescriptor.explicitForm.inClassIdentifier != -1) {

              super.update(cacheDescriptor);

            } else { // Is a wildcard (all precincts within a WOI)
              if (woi == null) {
                throw new IllegalArgumentException("Cache descriptor contains wildcards, then a WOI is required");
              }
              
              for (long inClassIdentifier: codestream.findRelevantPrecincts(woi)) {
                cacheDescriptor.explicitForm.inClassIdentifier = inClassIdentifier;
                super.update(descriptor);
              }
            }
            break;

          case ClassIdentifiers.TILE_HEADER:
            if (cacheDescriptor.explicitForm != null) {
              if (cacheDescriptor.explicitForm.numberOfBytes < 0) {
                cacheDescriptor.explicitForm.numberOfBytes = logicalTarget.getTileHeaderLength((int)cacheDescriptor.explicitForm.inClassIdentifier);
              }
              super.update(cacheDescriptor.additive, ExplicitBinDescriptor.TILE_HEADER, cacheDescriptor.explicitForm.inClassIdentifier, cacheDescriptor.explicitForm.numberOfLayers, cacheDescriptor.explicitForm.numberOfBytes);
            } else {
              assert (true);
            }
            break;

          case ClassIdentifiers.TILE:
            break;

          case ClassIdentifiers.MAIN_HEADER:
            if (cacheDescriptor.additive) {
              mainHeaderBinDescriptor.numberOfBytes = logicalTarget.getMainHeaderLength();
            } else {
              mainHeaderBinDescriptor.numberOfBytes = 0;
            }
            break;

          case ClassIdentifiers.METADATA:
            break;
        }
      } else {
        // Do nothing
      }

      cacheDescriptor = null;
    }

    descriptor.clear();
  }

  /**
   * Resets the variables to its initials values.
   */
  @Override
  public void reset() {
    super.reset();
  }

  /*
   * (non-Javadoc)
   * @see CADI.Server.Cache.CacheModel#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";

    str += super.toString();

    str += "]";
    return str;
  }

  /**
   * Prints this Server Cache Model and Preferences fields out to the
   * specified output stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Server Cache Model --");

    super.list(out);

    out.flush();
  }
  // ============================ private methods ==============================
}
