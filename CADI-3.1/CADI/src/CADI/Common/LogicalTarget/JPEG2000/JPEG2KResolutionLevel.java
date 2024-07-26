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
import java.util.HashMap;
import java.util.Map.Entry;

import CADI.Common.Util.CADIDimension;
import CADI.Common.Util.CADIPoint;
import CADI.Common.Util.CADIRectangle;
import java.util.Map;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/03/14
 */
public class JPEG2KResolutionLevel {

  /**
   * Records the component.
   * <p>
   * Only positive values are allowed.
   */
  protected int rLevel = -1;

  /**
   * Is a pointer to the parent.
   * <p>
   * It is useful to scan the tree structure from leaves to the root.
   */
  protected JPEG2KComponent parent = null;

  protected HashMap<Integer, JPEG2KPrecinct> precincts = null;

  // Names of subbands
  public static final int LL = 0;

  public static final int HL = 1;

  public static final int LH = 2;

  public static final int HH = 3;

  // INTERNAL ATTRIBUTES
  /**
   * Position (upper-left corner) of the resolution level within
   * the domain of the tile-component.
   * <p>
   * It is computed by the {@link #getLocation()} method.
   */
  private CADIPoint location = null;

  /**
   * Bounds (location and size) of the resolution level within the
   * domain of the tile-component.
   * <p>
   * It is computed by the {@link  #getBounds()} method.
   */
  private CADIRectangle bounds = null;

  private int maxNumPrecincts = -1;

  /**
   * Records the first precinct index for the tile-component-resolution level.
   */
  private int firstPrecinctIndex = -1;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public JPEG2KResolutionLevel(JPEG2KComponent parent,
          int rLevel) {
    if (rLevel < 0) {
      throw new IllegalArgumentException();
    }

    this.parent = parent;
    this.rLevel = rLevel;

    precincts = new HashMap<Integer, JPEG2KPrecinct>(getNumPrecincts());
  }

  /**
   *
   * @return
   */
  public int getResolutionLevel() {
    return rLevel;
  }

  /**
   *
   * @return
   */
  public JPEG2KComponent getParent() {
    return parent;
  }

  /**
   * Creates a new precinct.
   * 
   * @param index definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KPrecinct#precinctIndex}.
   * 
   * @throws IllegalAccessException
   */
  public void createPrecinct(int index) {
    if (!precincts.containsKey(index)) {
      precincts.put(index, new JPEG2KPrecinct(this, index));
    }
  }

  /**
   * Returns the precinct whose index in the tile-component-resollution level is
   * <ode>index</code>.
   * 
   * @param index definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KPrecinct#precinctIndex}.
   * 
   * @return a precinct object.
   */
  public JPEG2KPrecinct getPrecinct(int index) {
    return (precincts.containsKey(index))
            ? precincts.get(index)
            : null;
  }

  /**
   * Removes a precicnt from the resolution level.
   * 
   * @param index definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KPrecinct#precinctIndex}.
   */
  public void removePrecinct(int index) {
    if (precincts.containsKey(index)) {
      precincts.remove(index);
    }
  }

  /**
   * Removes all precincts from the resolution level.
   */
  public void removeAllPrecincts() {
    for (Map.Entry<Integer, JPEG2KPrecinct> entry : precincts.entrySet()) {
      entry.getValue().reset();
    }
    precincts.clear();
  }

  /**
   * Checks if the precinct whose index in the tile-component-resolution level 
   * is <code>index</code> has been created.
   * 
   * @param index definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KPrecinct#precinctIndex}.
   * 
   * @return <code>true</code> if the precinct has been created. Otherwise,
   *          returns <code>false</code>.
   */
  public boolean isPrecinct(int index) {
    return precincts.containsKey(index);
  }

  /**
   * Returns the unique precinct identifier. See
   * {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}..
   *
   * @return the unique precinct identifier.
   */
  public long getInClassIdentifier(int precinctIndex) {
    return JPEG2000Util.TCPToInClassIdentifier(parent.parent.index,
            parent.parent.parent.getNumTiles(),
            parent.component,
            parent.parent.parent.getZSize(),
            getFirstPrecinctIndex() + precinctIndex);
  }

  /**
   *
   * @return
   */
  public int getPrecinctWidth() {
    return parent.parent.getPrecinctWidths(parent.component, rLevel);
  }

  /**
   *
   * @return
   */
  public int getPrecinctHeight() {
    return parent.parent.getPrecinctHeights(parent.component, rLevel);
  }

  /**
   *
   * @return
   */
  public CADIDimension getPrecinctSizes() {
    return new CADIDimension(getPrecinctWidth(), getPrecinctHeight());
  }

  /**
   * Returns the position (upper-left corner) of the resolution level within
   * the domain of the tile-component.
   *
   * @return
   */
  public CADIPoint getLocation() {
    if (location == null) {
      location = parent.parent.getLocation(parent.component);
      int den = 1 << (parent.getWTLevels() - rLevel);
      location.x = (int)Math.ceil(location.x / den);
      location.y = (int)Math.ceil(location.y / den);
    }
    return location;
  }

  /**
   * Returns the size of the resolution level within the domain of the
   * tile-component.
   *
   * @return
   */
  public CADIDimension getSize() {
    CADIRectangle bounds = getBounds();
    return new CADIDimension(bounds.width, bounds.height);
  }

  /**
   * Returns the bounds (location and size) of the resolution level within the
   * domain of the tile-component.
   *
   * @return
   */
  public CADIRectangle getBounds() {
    if (bounds == null) {
      bounds = parent.parent.getBounds(parent.component);
      int den = 1 << (parent.getWTLevels() - rLevel);
      bounds.x = (int)Math.ceil(1.0 * bounds.x / den);
      bounds.y = (int)Math.ceil(1.0 * bounds.y / den);
      bounds.width = (int)Math.ceil(1.0 * (bounds.x + bounds.width) / den) - bounds.x;
      bounds.height = (int)Math.ceil(1.0 * (bounds.y + bounds.height) / den) - bounds.y;
    }
    return bounds;
  }

  /**
   * Returns the number of subbands.
   * 
   * @return number of subbands.
   */
  public int getNumSubbands() {
    return rLevel == 0 ? 1 : 3;
  }

  /**
   * Returns the location (upper-left corner) of the subband within the
   * domain of the tile-component resolution level.-
   *
   * @param subband a subband name. Allowed values are {@link #LL}, {@link #HL},
   * {@link #LH}, or {@link #HH}.
   *
   * @return
   */
  public CADIPoint getSubbandLocation(int subband) {
    if ((rLevel == 0) && (subband != LL)) {
      throw new IllegalArgumentException("Res. level 0 has only one subband");
    }

    CADIPoint location = parent.parent.getLocation(parent.component);
    int n_b = parent.getWTLevels() + 1 - rLevel;
    if (rLevel == 0) {
      n_b -= 1;
    }

    int xo_b = ((subband == LL) || (subband == LH)) ? 0 : 1;

    int tmp = (1 << (n_b - 1)) * xo_b;
    int tbx_0 = (int)Math.ceil(1.0 * (location.x - tmp) / (1 << n_b));

    int yo_b = ((subband == LL) || (subband == HL)) ? 0 : 1;
    tmp = (1 << (n_b - 1)) * yo_b;
    int tby_0 = (int)Math.ceil(1.0 * (location.y - tmp) / (1 << n_b));

    CADIRectangle rLevelBounds = getBounds();

    location.x = tbx_0 + rLevelBounds.x;
    location.y = tby_0 + rLevelBounds.y;

    return location;
  }

  /**
   * Returns the size of the subband within the domain of the tile-component
   * resolution level.
   *
   * @param subband
   * @return
   */
  public CADIDimension getSubbandSize(int subband) {
    CADIRectangle bounds = getSubbandBounds(subband);
    return new CADIDimension(bounds.width, bounds.height);
  }

  /**
   * Returns the bounds (location and size) of the subband within the
   * domain of the tile-component resolution level.-
   *
   * @param subband a subband name. Allowed values are {@link #LL}, {@link #HL},
   * {@link #LH}, or {@link #HH}.
   *
   * @return
   */
  public CADIRectangle getSubbandBounds(int subband) {
    if ((rLevel == 0) && (subband != LL)) {
      throw new IllegalArgumentException("Res. level 0 has only one subband");
    }
    if ((rLevel != 0) && (subband == LL)) {
      throw new IllegalArgumentException("Only res. level 0 has the LL subband");
    }

    CADIRectangle bounds = parent.parent.getBounds(parent.component);
    int n_b = parent.getWTLevels() + 1 - rLevel;
    if (rLevel == 0) {
      n_b -= 1;
    }

    int xo_b = ((subband == LL) || (subband == LH)) ? 0 : 1;
    int tmp = (1 << (n_b - 1)) * xo_b;
    int tbx_0 = (int)Math.ceil(1.0 * (bounds.x - tmp) / (1 << n_b));
    int tbx_1 = (int)Math.ceil(1.0 * (bounds.x + bounds.width - tmp) / (1 << n_b));

    int yo_b = ((subband == LL) || (subband == HL)) ? 0 : 1;
    tmp = (1 << (n_b - 1)) * yo_b;
    int tby_0 = (int)Math.ceil(1.0 * (bounds.y - tmp) / (1 << n_b));
    int tby_1 = (int)Math.ceil(1.0 * (bounds.y + bounds.height - tmp) / (1 << n_b));

    CADIRectangle rLevelBounds = getBounds();

    bounds.x = tbx_0 + rLevelBounds.x;
    bounds.y = tby_0 + rLevelBounds.y;
    bounds.width = tbx_1 - tbx_0;
    bounds.height = tby_1 - tby_0;

    return bounds;
  }

  /**
   * Returns the number of precincts in the wide dimension.
   *
   * @return number of precincts.
   */
  public int getNumPrecinctsWide() {
    CADIRectangle bounds = getBounds();
    int tmp = parent.getPrecinctWidths(rLevel);
    return (int)Math.ceil(1.0 * (bounds.x + bounds.width) / tmp)
           - (int)Math.floor(1.0 * bounds.x / tmp);
  }

  /**
   * Returns the number of precincts in the height dimension.
   *
   * @return number of precincts
   */
  public int getNumPrecinctsHeigh() {
    CADIRectangle bounds = getBounds();
    int tmp = parent.getPrecinctHeights(rLevel);
    return (int)Math.ceil(1.0 * (bounds.y + bounds.height) / tmp)
           - (int)Math.floor(1.0 * bounds.y / tmp);
  }

  /**
   * Returns the number of precincts of the tile-component-resolution revel.
   *
   * @return the number of precincts.
   */
  public int getNumPrecincts() {
    if (maxNumPrecincts < 0) {
      maxNumPrecincts = getNumPrecinctsWide() * getNumPrecinctsHeigh();
    }
    return maxNumPrecincts;
  }

  /**
   * Returns the width of blocks.
   * 
   * @return width of blocks
   */
  public int getBlockWidth() {
    int precWidth = parent.getPrecinctWidths(rLevel);
    if (rLevel > 0) {
      precWidth = (precWidth + 1) >>> 1;
    }
    return Math.min(parent.getBlockWidth(), precWidth);
  }

  /**
   * Returns the height of blocks.
   * 
   * @return height of blocks
   */
  public int getBlockHeight() {
    int precHeight = parent.getPrecinctHeights(rLevel);
    if (rLevel > 0) {
      precHeight = (precHeight + 1) >>> 1;
    }
    return Math.min(parent.getBlockHeight(), precHeight);
  }

  /**
   * Returns the dimensions (width and height) of blocks.
   *
   * @return dimensios of blocks
   */
  public CADIDimension getBlockSize() {
    return new CADIDimension(getBlockWidth(), getBlockHeight());
  }

  /**
   *
   * @param precinctIndex
   * @return
   */
  public CADIPoint getPrecinctLocation(int precinctIndex) {
    CADIPoint location = new CADIPoint();

    CADIDimension precDims = getPrecinctSizes();
    int numXPrecincts = getNumPrecinctsWide();
    int xPrecinct = precinctIndex % numXPrecincts;
    int yPrecinct = (int)Math.floor(1.0 * precinctIndex / numXPrecincts);

    CADIRectangle rLevelBounds = getBounds();

    location.x = xPrecinct * precDims.width;
    location.y = yPrecinct * precDims.height;

    // Absolute coordinates
    location.x += rLevelBounds.x;
    location.y += rLevelBounds.y;

    return location;
  }

  /**
   *
   * @param precinctIndex
   * @return
   */
  public CADIDimension getPrecinctSize(int precinctIndex) {
    CADIRectangle bounds = getPrecinctBounds(precinctIndex);
    return new CADIDimension(bounds.width, bounds.height);
  }

  /**
   *
   * @param precinctIndex
   * @return
   */
  public CADIRectangle getPrecinctBounds(int precinctIndex) {
    CADIRectangle bounds = new CADIRectangle();

    CADIDimension precDims = getPrecinctSizes();
    int numXPrecincts = getNumPrecinctsWide();
    int xPrecinct = precinctIndex % numXPrecincts;
    int yPrecinct = (int)Math.floor(1.0 * precinctIndex / numXPrecincts);
    int xEnd = (xPrecinct + 1) * precDims.width;
    int yEnd = (yPrecinct + 1) * precDims.height;

    CADIRectangle rLevelBounds = getBounds();
    if (xEnd > rLevelBounds.x + rLevelBounds.width) {
      xEnd = rLevelBounds.x + rLevelBounds.width;
    }

    if (yEnd > rLevelBounds.y + rLevelBounds.height) {
      yEnd = rLevelBounds.y + rLevelBounds.height;
    }

    bounds.x = xPrecinct * precDims.width;
    bounds.y = yPrecinct * precDims.height;
    bounds.width = xEnd - bounds.x;
    bounds.height = yEnd - bounds.y;

    // Absolute coordinates
    bounds.x += rLevelBounds.x;
    bounds.y += rLevelBounds.y;

    return bounds;
  }

  /**
   * Returns the number of blocks per precinct.
   * <p>
   * TODO: Method name will be changed to getBlocksPerPrecinctWide
   *
   * @return
   */
  public int getBlocksPerPrecinctWidths() {
    int blockWidth = getBlockWidth();
    if (rLevel > 0) {
      blockWidth <<= 1;
    }
    return (int)Math.ceil(1.0 * getPrecinctWidth() / blockWidth);
  }

  /**
   * Returns the number of blocks per precinct.
   * <p>
   * TODO: Method name will be changed to getBlocksPerPrecinctHigh
   *
   * @return
   */
  public int getBlocksPerPrecinctHeights() {
    int blockHeight = getBlockHeight();
    if (rLevel > 0) {
      blockHeight <<= 1;
    }
    return (int)Math.ceil(1.0 * getPrecinctHeight() / blockHeight);
  }

  /**
   * Returns the number of blocks per precinct in the wide direccion.
   *
   * @return
   */
  public int getBlocksPerPrecinctWide() {
    return (int)Math.ceil(1.0 * getPrecinctWidth() / getBlockWidth());
  }

  /**
   * Returns the number of blocks per precinct in the high direccion.
   *
   * @return
   */
  public int getBlocksPerPrecinctHigh() {
    return (int)Math.ceil(1.0 * getPrecinctHeight() / getBlockHeight());
  }

  /**
   * Returns the number of blocks per precinct.
   *
   * @return
   */
  public CADIDimension getBlocksPerPrecinct() {
    return new CADIDimension(getBlocksPerPrecinctWidths(),
            getBlocksPerPrecinctHeights());
  }

  /**
   * Returns the first precinct index in the tile-component-resolution level.
   * 
   * @return precinct index.
   */
  public int getFirstPrecinctIndex() {
    if (firstPrecinctIndex < 0) {
      if (rLevel == 0) {
        firstPrecinctIndex = 0;
      } else {
        firstPrecinctIndex = parent.getResolutionLevel(rLevel - 1).getFirstPrecinctIndex()
                             + parent.getResolutionLevel(rLevel - 1).getNumPrecincts();
      }
    }
    return firstPrecinctIndex;
  }

  /**
   * Returns the precinct index in which the code-block defined by <code>xBlock
   * </code> and <code>yBlock</code> indexes is located.
   * 
   * @param xBlock
   * @param yBlock
   * 
   * @return 
   */
  public int getPrecinctIndex(int xBlock, int yBlock) {
    int xPrecinct = xBlock / getBlocksPerPrecinctWidths();
    int yPrecinct = yBlock / getBlocksPerPrecinctHeights();
    return (yPrecinct * getNumPrecinctsWide()) + xPrecinct;
  }

  /**
   * Returns the code-block indexes within the precinct in which is located.
   * 
   * @param xBlock
   * @param yBlock
   * @return 
   */
  public CADIPoint getBlockIndexWithinPrecinct(int xBlock, int yBlock) {
    return new CADIPoint(xBlock % getBlocksPerPrecinctWidths(),
            yBlock % getBlocksPerPrecinctHeights());
  }

  /**
   * Returns the exponent value for the subband <code>subband</code>.
   * 
   * @param subband a subband name. Allowed values are {@link #LL}, {@link #HL},
   * {@link #LH}, or {@link #HH}.
   * @return 
   */
  public int getExponent(int subband) {
    return parent.getExponent(rLevel, subband);
  }

  /**
   * Returns the value of the mantisa for the subband <code>subband</code>.
   * 
   * @param subband a subband name. Allowed values are {@link #LL}, {@link #HL},
   * {@link #LH}, or {@link #HH}.
   * @return 
   */
  public int getMantisa(int subband) {
    return parent.getMantisa(rLevel, subband);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";
    str += getClass().getName() + " [";

    str += "Res. level=" + rLevel;

    for (Entry<Integer, JPEG2KPrecinct> precinct : precincts.entrySet()) {
      str += precinct.getValue().toString();
    }

    str += "]";

    return str;
  }

  /**
   * Prints this Server JPEG2K Tile out to the specified output stream. This
   * method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- JPEG2K Resolution Level --");

    out.println("Res. level=" + rLevel);

    for (Entry<Integer, JPEG2KPrecinct> precinct : precincts.entrySet()) {
      precinct.getValue().list(out);
    }


  }
  // ============================ private methods ==============================
}
