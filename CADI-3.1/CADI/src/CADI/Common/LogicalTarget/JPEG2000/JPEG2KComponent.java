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
 * @version 1.0 2010/12/12
 */
public class JPEG2KComponent {

  /**
   * Records the component.
   * <p>
   * Only positive values are allowed.
   */
  protected int component = -1;

  /**
   * Is a pointer to the parent.
   * <p>
   * It is useful to scan the tree structure from leaves to the root.
   */
  protected JPEG2KTile parent = null;

  /**
   *
   */
  protected HashMap<Integer, JPEG2KResolutionLevel> resolutionLevels = null;

  // INTERNAL ATTRIBUTES
  /**
   * Offset from the origin of the reference grid to the upper-left sample of
   * the image area.
   * <p>
   * It is computed by the {@link #getLocation()} method.ß
   */
  private CADIPoint location = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param parent
   * @param component definition in {@link #component}.
   */
  public JPEG2KComponent(JPEG2KTile parent, int component) {
    if (component < 0) {
      throw new IllegalArgumentException();
    }

    this.parent = parent;
    this.component = component;

    resolutionLevels = new HashMap<Integer, JPEG2KResolutionLevel>(getWTLevels());
  }

  /**
   *
   * @return
   */
  public int getComponent() {
    return component;
  }

  /**
   *
   * @return
   */
  public JPEG2KTile getParent() {
    return parent;
  }

  /**
   * Creates a new resolution level in the tile-component.
   * 
   * @param rLevel definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#rLevel}.
   */
  public void createResolutionLevel(int rLevel) {
    if (!resolutionLevels.containsKey(component)) {
      resolutionLevels.put(rLevel, new JPEG2KResolutionLevel(this, rLevel));
    }
  }

  /**
   * Returns the resolution level whose index is <code>rLevel</code>.
   *
   * @param rLevel definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#rLevel}.
   * 
   * @return a resolution level object.
   */
  public JPEG2KResolutionLevel getResolutionLevel(int rLevel) {
    return (resolutionLevels.containsKey(rLevel))
            ? resolutionLevels.get(rLevel)
            : null;
  }

  /**
   * Removes a resolution level from the tile-component.
   * <p>
   * It also removes all precincts defined in the resolution level.
   * 
   * @param rLevel definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#rLevel}.
   */
  public void removeResolutionLevel(int rLevel) {
    if (resolutionLevels.containsKey(rLevel)) {
      resolutionLevels.get(rLevel).removeAllPrecincts();
      resolutionLevels.remove(rLevel);
    }
  }

  /**
   * Removes all resolution levels from this tile-component.
   */
  public void deleteAllResolutionLevels() {
    for (Map.Entry<Integer, JPEG2KResolutionLevel> entry : resolutionLevels.entrySet()) {
      resolutionLevels.get(entry.getKey()).removeAllPrecincts();
    }
    resolutionLevels.clear();
  }

  /**
   * Returns the number of precinct within the tile-component.
   *
   * @param inClassIdentifier
   * @return
   */
  public int findPrecinct(long inClassIdentifier) {
    int numTiles = parent.parent.getNumTiles();
    int numComps = parent.parent.getZSize();

    int tile = (int)(inClassIdentifier % numTiles);
    int temp = (int)((inClassIdentifier - tile) / (double)numTiles);
    int comp = temp % numComps;
    return ((int)((temp - comp) / (double)numComps));
  }

  /**
   * Returns the resolution level of the precinct within the tile-component.
   * <p>
   * OBS: not implemented yet.
   * 
   * @param inClassIdentifier
   * @return
   */
  public int findResolutionLevel(long inClassIdentifier) {

    return -1;
  }

  // SIZ methods
  /**
   * Returns the horizontal separation of sample of component <code>component
   * </code> with respect to the reference grid.
   * 
   * @param component definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent#component}.
   * 
   * @return  horizontal separation of samples.
   */
  public int getXRsize(int component) {
    return parent.getXRsize(component);
  }

  /**
   * Returns the vertical separation of sample of component <code>component
   * </code> with respect to the reference grid.
   * 
   * @param component definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent#component}.
   * 
   * @return  vertical separation of samples.
   */
  public int getYRsize(int component) {
    return parent.getYRsize(component);
  }

  // COD & COC methods
  public int getWTLevels() {
    return parent.getWTLevels(component);
  }

  public int getWTType() {
    return parent.getWTType(component);
  }

  public boolean isBypass() {
    return parent.isBypass(component);
  }

  public boolean isReset() {
    return parent.isReset(component);
  }

  public boolean isRestart() {
    return parent.isReset(component);
  }

  public boolean isCausal() {
    return parent.isCausal(component);
  }

  public boolean isErterm() {
    return parent.isErterm(component);
  }

  public boolean isSegmark() {
    return parent.isSegmark(component);
  }

  public int getBlockHeight() {
    return parent.getBlockHeight(component);
  }

  public int getBlockWidth() {
    return parent.getBlockWidth(component);
  }

  public int getPrecinctHeights(int rLevel) {
    return parent.getPrecinctHeights(component, rLevel);
  }

  public int getPrecinctWidths(int rLevel) {
    return parent.getPrecinctWidths(component, rLevel);
  }

  //QCD & QCC Methods
  public int getQuantizationStyle() {
    return parent.getQuantizationStyle(component);
  }

  /**
   * Returns the exponent for the resolution level <code>rLevel</code> and
   * subband <code>subband</code>.
   * 
   * @param rLevel definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#rLevel}.
   * @param subband a subband name. Allowed values are {@link #LL}, {@link #HL},
   * {@link #LH}, or {@link #HH}.
   * @return 
   */
  public int getExponent(int rLevel, int subband) {
    if ((rLevel == 0) && (subband != JPEG2KResolutionLevel.LL)) {
      throw new IllegalArgumentException("Res. level 0 has only one subband");
    }
    if ((rLevel != 0) && (subband == JPEG2KResolutionLevel.LL)) {
      throw new IllegalArgumentException("Only res. level 0 has the LL subband");
    }
    return parent.getExponent(component, rLevel, subband);
  }

  /**
   * Returns the mantisa for the resolution level <code>rLevel</code> and
   * subband <code>subband</code>.
   * 
   * @param rLevel definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#rLevel}.
   * @param subband a subband name. Allowed values are {@link #LL}, {@link #HL},
   * {@link #LH}, or {@link #HH}.
   * @return 
   */
  public int getMantisa(int rLevel, int subband) {
    if ((rLevel == 0) && (subband != JPEG2KResolutionLevel.LL)) {
      throw new IllegalArgumentException("Res. level 0 has only one subband");
    }
    if ((rLevel != 0) && (subband == JPEG2KResolutionLevel.LL)) {
      throw new IllegalArgumentException("Only res. level 0 has the LL subband");
    }
    return parent.getMantisa(component, rLevel, subband);
  }

  /**
   * Returns the number of guard bits.
   * 
   * @return number of guard bits.
   */
  public int getGuardBits() {
    return parent.getGuardBits(component);
  }

  /**
   * Offset from the origin of the reference grid to the upper-left sample of
   * the image area.
   *
   * @return
   */
  public CADIPoint getLocation() {
    if (location == null) {
      int x_0 = (int)Math.ceil(1.0 * parent.parent.sizParameters.XOsize
                               / parent.parent.sizParameters.XRsiz[component]);
      int y_0 = (int)Math.ceil(1.0 * parent.parent.sizParameters.YOsize
                               / parent.parent.sizParameters.YRsiz[component]);
      location = new CADIPoint(x_0, y_0);
    }

    return new CADIPoint(location);
  }

  /**
   * Dimensions of the component.
   *
   * @return
   */
  public CADIDimension getSize() {
    CADIRectangle rectangle = getBounds();
    return new CADIDimension(rectangle.width, rectangle.height);
  }

  /**
   * Returns the offset and dimensions of the component.
   *
   * @return offset and dimensions of the component.
   */
  public CADIRectangle getBounds() {
    CADIPoint offset = getLocation();
    int x_1 = (int)Math.ceil(1.0 * parent.parent.sizParameters.xSize
                             / parent.parent.sizParameters.XRsiz[component]);
    int y_1 = (int)Math.ceil(1.0 * parent.parent.sizParameters.ySize
                             / parent.parent.sizParameters.YRsiz[component]);

    return new CADIRectangle(offset.x, offset.y, x_1 - offset.x, y_1 - offset.y);
  }

  /**
   * Returns the offsetand dimensions of the resolution level <code>rLevel</code>.
   * 
   * @param rLevel definition in {@link #CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#rLevel}.
   * @return offset and dimensions of the resolution level.
   */
  public CADIRectangle getBounds(int rLevel) {
    CADIRectangle bounds = parent.getBounds(component);
    int den = 1 << (getWTLevels() - rLevel);
    bounds.x = (int)Math.ceil(1.0 * bounds.x / den);
    bounds.y = (int)Math.ceil(1.0 * bounds.y / den);
    bounds.width = (int)Math.ceil(1.0 * (bounds.x + bounds.width) / den) - bounds.x;
    bounds.height = (int)Math.ceil(1.0 * (bounds.y + bounds.height) / den) - bounds.y;
    return bounds;
  }

  /**
   * Returns the size of the resolution level within the domain of the
   * tile-component.
   *
   * @return
   */
  public CADIDimension getSize(int rLevel) {
    CADIRectangle bounds = getBounds(rLevel);
    return new CADIDimension(bounds.width, bounds.height);
  }

  /**
   * Calculates the frame sizes for each resolution level according to the
   * image parameters that are passed to the function.
   * Further information, see ISO/IEC 15444-9 sect. C4
   *
   * @return an one-dimensional array with the frame sizes for each resolution
   * 			level. The first index stands for the resolution level and the
   * 			second one will be, 0 is for the width and 1 for the height.
   */
  public int[][] availableFrameSizes() {

    int WTLevels = getWTLevels();
    int xSize = parent.parent.getXSize();
    int ySize = parent.parent.getYSize();
    int XOSize = parent.parent.getXOSize();
    int YOSize = parent.parent.getYOSize();
    final int X = 0, Y = 1;

    // Frame sizes (1st index is resolution level, 2nd 0- width and 1- height)
    int[][] frameSizes = new int[WTLevels + 1][2];
    ;
    int D = WTLevels;
    for (int r = 0; r < WTLevels + 1; r++) {
      frameSizes[r][X] = (int)Math.ceil((float)xSize / (1 << (D - r)))
                         - (int)Math.ceil((float)XOSize / (1 << (D - r)));
      frameSizes[r][Y] = (int)Math.ceil((float)ySize / (1 << (D - r)))
                         - (int)Math.ceil((float)YOSize / (1 << (D - r)));
    }
    //System.out.println("Available frame sizes");
    //for(int rLevel=0; rLevel<=WTLevels;rLevel++) System.out.println(" rLevel="+rLevel+" with="+frameSizes[rLevel][X]+" height="+frameSizes[rLevel][Y]);
    return frameSizes;
  }

  /**
   * Returns the maximum number of precincts in the tile-component.
   *
   * @return
   */
  public int getNumPrecincts() {
    int WTLevels = getWTLevels();
    int numPrecincts = 0;

    for (int r = 0; r < WTLevels + 1; r++) {
      numPrecincts += resolutionLevels.get(r).getNumPrecinctsWide()
                      * resolutionLevels.get(r).getNumPrecinctsHeigh();
    }

    return numPrecincts;
  }

  /**
   * Returns the bounds (location and size) of a resolution level.
   *
   * @param rLevel
   * @return
   */
  public CADIRectangle getResolutionLevelBounds(int rLevel) {
    return resolutionLevels.get(rLevel).getBounds();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";
    str += getClass().getName() + " [";

    str += "Component=" + component;

    for (Entry<Integer, JPEG2KResolutionLevel> rLevel : resolutionLevels.entrySet()) {
      str += rLevel.getValue().toString();
    }

    str += "]";

    return str;
  }

  /**
   * Prints this Server JPEG2K Component out to the specified output stream. This
   * method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Server JPEG2K Component --");

    out.println("Component=" + component);

    for (Entry<Integer, JPEG2KResolutionLevel> rLevel : resolutionLevels.entrySet()) {
      rLevel.getValue().list(out);
    }

    out.flush();
  }
  // ============================ private methods ==============================
}
