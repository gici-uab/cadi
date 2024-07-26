/*
 CADI Software - a JPIP Client/Server framework
 Copyright (C) 2007-2012 Group on Interactive Coding of Images (GICI)

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.

 Group on Interactive Coding of Images (GICI)
 Department of Information and Communication Engineering
 Autonomous University of Barcelona
 08193 - Bellaterra - Cerdanyola del Valles (Barcelona)
 Spain

 http://gici.uab.es
 gici-info@deic.uab.es
 */
package CADI.Common.Cache;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2KTile;
import CADI.Common.Network.JPIP.ClassIdentifiers;
import java.util.ArrayList;

/**
 * This class implements a basic cache model.
 * <p>
 * The main objective of this class is to provide basic attributes and methods
 * to deal with a cache model.
 * <p>
 * Class's attributes and methods have been declared as
 * <code>protected</code>
 * or
 * <code>public</code> in order to be used by other classes which extend this
 * one.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 2.0.1 2012/05/19
 */
public class CacheModel {

  /**
   * Is an object with the codestream properties.
   */
  protected JPEG2KCodestream codestream = null;

  /**
   * Main header data-bin.
   */
  protected ExplicitBinDescriptor mainHeaderBinDescriptor = null;

  /**
   * It is a hash table that storages the length/layers of delivered data-bins.
   * The key of the hash is the unique data-bin identifier calculated as:<br>
   * I = t + (c + s x num_components) + num_tiles<br>
   * where,<br>
   * &nbsp; I is the unique identifier of the precinct within its codestream;<br>
   * &nbsp; t is the index (starting from 0) of the tile to which the precinct belongs;<br>
   * &nbsp; c is the index (starting from 0) of the image component to which the precinct belongs;<br>
   * &nbsp; s is a sequence number which identifies the precinct within its tile-component.<br>
   */
  protected HashMap<Long, ExplicitBinDescriptor> precinctDataBins = null;

  /**
   *
   */
  protected HashMap<Long, ExplicitBinDescriptor> tileHeaderDataBin = null;

  // INTERNAL ATTRIBUTES
  private JPEG2KTile tileObj = null;

  private JPEG2KComponent componentObj = null;

  private JPEG2KResolutionLevel rLevelObj = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @throws IllegalAccessException
   */
  public CacheModel()
          throws IllegalAccessException {
    throw new IllegalAccessException("Constructor not allowed.");
  }

  /**
   * Constructor.
   *
   * @param codestream
   */
  public CacheModel(JPEG2KCodestream codestream) {
    // Check input parameters
    if (codestream == null) {
      throw new NullPointerException();
    }
    this.codestream = codestream;

    // Initializations
    precinctDataBins = new HashMap<Long, ExplicitBinDescriptor>();
    mainHeaderBinDescriptor = new ExplicitBinDescriptor(ExplicitBinDescriptor.MAIN_HEADER, 0, -1, 0);
    tileHeaderDataBin = new HashMap<Long, ExplicitBinDescriptor>();
  }

  /**
   * Sets the attributes to their initial values.
   */
  public void reset() {
    if (mainHeaderBinDescriptor != null) {
      mainHeaderBinDescriptor.reset();
      mainHeaderBinDescriptor = null;
    }

    for (Map.Entry<Long, ExplicitBinDescriptor> descriptor : precinctDataBins.entrySet()) {
      descriptor.getValue().reset();
    }
    precinctDataBins.clear();

    for (Map.Entry<Long, ExplicitBinDescriptor> descriptor : tileHeaderDataBin.entrySet()) {
      descriptor.getValue().reset();
    }
    tileHeaderDataBin.clear();
  }

  /**
   * Updates the cache.
   * 
   * @param additive definition in {@link CADI.Common.Cache.ModelElement#additive}.
   * @param classIdentifier definition in {@link CADI.Common.Cache.ExplicitBinDescriptor#classIdentifier}.
   * @param inClassIdentifier definition in {@link CADI.Common.Cache.ExplicitBinDescriptor#inClassIdentifier}.
   * @param numberOfBytes definition in {@link CADI.Common.Cache.ExplicitBinDescriptor#numberOfBytes}.
   */
  public final void update(boolean additive,
                           int classIdentifier, long inClassIdentifier,
                           int numberOfBytes) {
    update(additive, classIdentifier, inClassIdentifier, -1, numberOfBytes);
  }

  /**
   * Updates the cache model.
   * <p>
   * If
   * <code>numberOfLayers</code> and
   * <code>numberOfBytes</code> are both
   * <code>-1</code> it means a wildcard.
   *
   * @param additive definition in {@link CADI.Common.Cache.ModelElement#additive}.
   * @param classIdentifier definition in {@link CADI.Common.Cache.ExplicitBinDescriptor#classIdentifier}.
   * @param inClassIdentifier definition in {@link CADI.Common.Cache.ExplicitBinDescriptor#inClassIdentifier}.
   * @param numberOfLayers
   * @param numberOfBytes definition in {@link CADI.Common.Cache.ExplicitBinDescriptor#numberOfBytes}.
   */
  public final void update(boolean additive,
                           int classIdentifier, long inClassIdentifier,
                           int numberOfLayers, int numberOfBytes) {

    // Check input parameters
    if (classIdentifier < 0) {
      throw new IllegalArgumentException("classIdentifier cannot be negative");
    }
    if (inClassIdentifier < 0) {
      throw new IllegalArgumentException("inClassIdentifier cannot be negative");
    }
    /* if ((numberOfLayers < 0) && (numberOfBytes < 0)) {
     * throw new IllegalArgumentException("numberOfBytes and numberOfLayers cannot be both negatives");
     * } */


    switch (classIdentifier) {

      case ClassIdentifiers.PRECINCT:
        if (!precinctDataBins.containsKey(inClassIdentifier)) {
          precinctDataBins.put(inClassIdentifier,
                  new ExplicitBinDescriptor(classIdentifier, inClassIdentifier,
                  numberOfLayers, numberOfBytes));
        }
        ExplicitBinDescriptor descriptor = precinctDataBins.get(inClassIdentifier);

        if (additive) { // Additive
          if (numberOfLayers >= 0) {
            if (descriptor.numberOfLayers < numberOfLayers) {
              descriptor.numberOfLayers = numberOfLayers;
              descriptor.numberOfBytes = -1;
            }
          } else if (numberOfBytes >= 0) {
            if (descriptor.numberOfBytes < numberOfBytes) {
              descriptor.numberOfBytes = numberOfBytes;
              descriptor.numberOfLayers = -1;
            }
          } else { // Its a wilcard
            descriptor.setWilcard();
          }

        } else { // Subtractive
          if (numberOfLayers >= 0) {
            if (descriptor.numberOfLayers > numberOfLayers) {
              descriptor.numberOfLayers = numberOfLayers;
              descriptor.numberOfBytes = -1;
            }
          } else if (numberOfBytes >= 0) {
            if (descriptor.numberOfBytes > numberOfBytes) {
              descriptor.numberOfBytes = numberOfBytes;
              descriptor.numberOfLayers = -1;
            }
          } else { // Its a wilcard
            descriptor.numberOfBytes = 0;
            descriptor.numberOfLayers = 0;
          }
        }

        break;

      case ClassIdentifiers.TILE_HEADER:
        if (!tileHeaderDataBin.containsKey(inClassIdentifier)) {
          tileHeaderDataBin.put(inClassIdentifier,
                  new ExplicitBinDescriptor(classIdentifier, inClassIdentifier,
                  numberOfLayers, numberOfBytes));
        }
        ExplicitBinDescriptor thDescriptor = tileHeaderDataBin.get(inClassIdentifier);
        if (additive) {
          if (numberOfLayers >= 0) {
            if (thDescriptor.numberOfLayers < numberOfLayers) {
              thDescriptor.numberOfLayers = numberOfLayers;
              thDescriptor.numberOfBytes = -1;
            }
          } else if (numberOfBytes >= 0) {
            if (thDescriptor.numberOfBytes < numberOfBytes) {
              thDescriptor.numberOfBytes = numberOfBytes;
              thDescriptor.numberOfLayers = -1;
            }
          } else { // Its a wilcard
            thDescriptor.setWilcard();
          }
        } else {
          if (numberOfLayers >= 0) {
            if (thDescriptor.numberOfLayers > numberOfLayers) {
              thDescriptor.numberOfLayers = numberOfLayers;
              thDescriptor.numberOfBytes = -1;
            }
          } else if (numberOfBytes >= 0) {
            if (thDescriptor.numberOfBytes > numberOfBytes) {
              thDescriptor.numberOfBytes = numberOfBytes;
              thDescriptor.numberOfLayers = -1;
            }
          } else { // Its a wilcard
            thDescriptor.numberOfBytes = 0;
            thDescriptor.numberOfLayers = 0;
          }
        }

        break;

      case ClassIdentifiers.TILE:
        break;

      case ClassIdentifiers.MAIN_HEADER:
        if (numberOfLayers >= 0) {
          throw new IllegalArgumentException();
        }
        if (mainHeaderBinDescriptor == null) {
          mainHeaderBinDescriptor =
                  new ExplicitBinDescriptor(ExplicitBinDescriptor.MAIN_HEADER, 0, 0, 0);
        }
        if (additive) {
          if (numberOfBytes >= 0) {
            if (mainHeaderBinDescriptor.numberOfBytes < numberOfBytes) {
              mainHeaderBinDescriptor.numberOfBytes = numberOfBytes;
            }
          } else {
            // Its a wilcard. Not allowed.
            assert (true);
          }
        } else {
          mainHeaderBinDescriptor.numberOfBytes = 0;
        }
        break;

      case ClassIdentifiers.METADATA:
        break;

      default:
        assert (true);
    }
  }

  /**
   * Updates the cache.
   *
   * @param additive definition in {@link CADI.Common.Cache.ModelElement#additive}.
   * @param descriptor
   */
  public final void update(boolean additive, ExplicitBinDescriptor descriptor) {
    if (descriptor == null) {
      throw new NullPointerException();
    }
    update(additive, descriptor.classIdentifier, descriptor.inClassIdentifier,
            descriptor.numberOfLayers, descriptor.numberOfBytes);
  }

  /**
   * Updates the cache.
   *
   * @param additive definition in {@link CADI.Common.Cache.ModelElement#additive}.
   * @param firstTile defintion in {@link CADI.Common.Cache.ImplicitBinDescriptor#firstTilePos}
   * @param lastTile defintion in {@link CADI.Common.Cache.ImplicitBinDescriptor#lastTilePos}
   * @param firstComponent defintion in {@link CADI.Common.Cache.ImplicitBinDescriptor#firstComponentPos}
   * @param lastComponent defintion in {@link CADI.Common.Cache.ImplicitBinDescriptor#lastComponentPos}
   * @param firstResolutionLevel defintion in {@link CADI.Common.Cache.ImplicitBinDescriptor#firstResolutionLevelPos}
   * @param lastResolutionLevel defintion in {@link CADI.Common.Cache.ImplicitBinDescriptor#lastResolutionLevelPos}
   * @param firstPrecinct defintion in {@link CADI.Common.Cache.ImplicitBinDescriptor#firstPrecinctPos}
   * @param lastPrecinct defintion in {@link CADI.Common.Cache.ImplicitBinDescriptor#lastPrecinctPos}
   * @param numberOfLayers defintion in {@link CADI.Common.Cache.ImplicitBinDescriptor#numberOfLayers}
   */
  public final void update(boolean additive,
                           int firstTile, int lastTile,
                           int firstComponent, int lastComponent,
                           int firstResolutionLevel, int lastResolutionLevel,
                           int firstPrecinct, int lastPrecinct,
                           int numberOfLayers) {

    assert ((firstTile >= 0) && (lastTile >= 0));
    assert ((firstComponent >= 0) && (lastComponent >= 0));
    assert ((firstResolutionLevel >= 0) && (lastResolutionLevel >= 0));
    assert ((firstPrecinct >= 0) && (lastPrecinct >= 0));

    for (int t = firstTile; t <= lastTile; t++) {
      tileObj = codestream.getTile(t);
      for (int c = firstComponent; c <= lastComponent; c++) {
        componentObj = tileObj.getComponent(c);
        for (int r = firstResolutionLevel; r <= lastResolutionLevel; r++) {
          rLevelObj = componentObj.getResolutionLevel(r);
          for (int p = firstPrecinct; p <= lastPrecinct; p++) {
            long inClassIdentifier = rLevelObj.getInClassIdentifier(p);
            if (!precinctDataBins.containsKey(inClassIdentifier)) {
              precinctDataBins.put(inClassIdentifier, new ExplicitBinDescriptor());
            }
            update(additive, ExplicitBinDescriptor.PRECINCT,
                    inClassIdentifier, numberOfLayers, -1);
          }
        }
      }
    }

  }

  /**
   * Updates the cache.
   * 
   * @param additive definition in {@link CADI.Common.Cache.ModelElement#additive}.
   * @param descriptor 
   */
  public final void update(boolean additive, ImplicitBinDescriptor descriptor) {
    update(additive, descriptor.firstTilePos, descriptor.lastTilePos,
            descriptor.firstComponentPos, descriptor.lastComponentPos,
            descriptor.firstResolutionLevelPos, descriptor.lastResolutionLevelPos,
            descriptor.firstPrecinctPos, descriptor.lastPrecinctPos,
            descriptor.numberOfLayers);
  }

  /**
   * Updates the cache.
   *
   * @param modelElement
   */
  public void update(ArrayList<ModelElement> modelElement) {
    for (ModelElement element : modelElement) {
      update(element);
    }
  }

  /**
   * Updates the cache.
   *
   * @param modelElement
   */
  public final void update(ModelElement modelElement) {
    if (modelElement.explicitForm != null) {
      // Explicit form
      update(modelElement.additive, modelElement.explicitForm.classIdentifier,
              modelElement.explicitForm.inClassIdentifier,
              modelElement.explicitForm.numberOfLayers,
              modelElement.explicitForm.numberOfBytes);

    } else if (modelElement.implicitForm != null) {
      // Implicit form
      update(modelElement.additive, modelElement.implicitForm);
    }
  }

  /**
   * Returns the length of the data bin given by the <code>inClassIdentifier</code>
   * identifier in the class <code>classIdentifier</code>.
   * <p>
   * The returne value is a non-negative value with the length of the data-bin,
   * except for Precincts (ClassIdentifiers.PRECINCT) where a negative length
   * means the cached information about the precinct must be read from the
   * number of layers.
   *
   * @param classIdentifier definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#classIdentifier}
   * @param inClassIdentifier definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}
   *
   * @return length of the data-bin.
   */
  public final int getDataBinLength(int classIdentifier, long inClassIdentifier) {
   
    // Check input parameters
    if (classIdentifier < 0) {
      throw new IllegalArgumentException();
    }
    if (inClassIdentifier < 0) {
      throw new IllegalArgumentException();
    }

    int length = 0;

    switch (classIdentifier) {
      case ClassIdentifiers.PRECINCT:
        length = getPrecinctDataBinLength(inClassIdentifier);
        break;

      case ClassIdentifiers.TILE_HEADER:
        length = getTileHeaderDataBinLength(inClassIdentifier);
        break;

      case ClassIdentifiers.TILE:
        break;

      case ClassIdentifiers.MAIN_HEADER:
        length = getMainHeaderLength();
        break;

      case ClassIdentifiers.METADATA:
        break;

      default:
        throw new IllegalArgumentException();
    }

    return length;
  }

  /**
   * Returns the length of the main header data-bin.
   *
   * @return length of the main header data-bin.
   */
  public final int getMainHeaderLength() {
    if (mainHeaderBinDescriptor == null) {
      return 0;
    } else if (mainHeaderBinDescriptor.isWildcard()) {
      return Integer.MAX_VALUE;
    } else if (mainHeaderBinDescriptor.numberOfBytes < 0) {
      return 0;
    } else {
      return mainHeaderBinDescriptor.numberOfBytes;
    }
  }

  /**
   * Returns the length of the data-bin.
   * <p>
   * The returned value should be undestood as:
   * - a non-negative value means the length of the data bin.
   * - a negative value means the cached information about the precinct must
   * be read from the number of layers.
   *
   * @param inClassIdentifier definition in
   * {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}
   *
   * @return the length of the data-bin.
   */
  public int getPrecinctDataBinLength(long inClassIdentifier) {
    ExplicitBinDescriptor dataBin = precinctDataBins.get(inClassIdentifier);
    if (dataBin == null) {
      return -1;
    }

    if (dataBin.isWildcard()) {
      return Integer.MAX_VALUE;
    }

    return dataBin.numberOfBytes;
  }

  /**
   * Returns the number of layers of the data-bin
   * <p>
   * The returned value should be undestood as:
   * - a non-negative value means the number of layers of the data bin.
   * - a negative value means the cached information about the precinct must
   * be read from the number of bytes.
   *
   * @param inClassIdentifier
   *
   * @return
   */
  public int getPrecinctDataBinLayers(long inClassIdentifier) {
    ExplicitBinDescriptor dataBin = precinctDataBins.get(inClassIdentifier);
    if (dataBin == null) {
      return -1;
    }

    if (dataBin.isWildcard()) {
      return Integer.MAX_VALUE;
    }

    return dataBin.numberOfLayers;
  }

  /**
   * Returns the length of tile header data-bin.
   *
   * @param inClassIdentifier definition in
   * {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}
   *
   * @return the length of the data-bin.
   */
  public int getTileHeaderDataBinLength(long inClassIdentifier) {
    ExplicitBinDescriptor dataBin = tileHeaderDataBin.get(inClassIdentifier);
    if (dataBin == null) {
      return 0;
    }

    if (dataBin.isWildcard()) {
      return Integer.MAX_VALUE;
    }

    return dataBin.numberOfBytes;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";

    if (mainHeaderBinDescriptor != null) {
      str += mainHeaderBinDescriptor.toString();
    }

    for (Entry<Long, ExplicitBinDescriptor> descriptor : tileHeaderDataBin.entrySet()) {
      str += descriptor.getValue().toString();
    }

    for (Entry<Long, ExplicitBinDescriptor> descriptor : precinctDataBins.entrySet()) {
      str += descriptor.getValue().toString();
    }

    str += "]";
    return str;
  }

  /**
   * Prints this Cache Model fields out to the
   * specified output stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Cache Model --");

    if (mainHeaderBinDescriptor != null) {
      mainHeaderBinDescriptor.list(out);
    }

    for (Entry<Long, ExplicitBinDescriptor> descriptor : tileHeaderDataBin.entrySet()) {
      descriptor.getValue().list(out);
    }

    for (Entry<Long, ExplicitBinDescriptor> descriptor : precinctDataBins.entrySet()) {
      descriptor.getValue().list(out);
    }

    out.flush();
  }
  // ============================ private methods ==============================
}
