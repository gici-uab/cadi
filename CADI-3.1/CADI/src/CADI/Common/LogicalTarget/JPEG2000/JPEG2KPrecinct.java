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

import CADI.Common.Util.CADIDimension;
import CADI.Common.Util.CADIRectangle;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.3 2012/01/14
 */
public class JPEG2KPrecinct {

  /**
   * Records the precinctIndex.
   * <p>
   * Only positive values are allowed.
   */
  protected int precinctIndex = -1;

  /**
   * Definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}.
   */
  protected long inClassIdentifier = -1;

  /**
   * Is a pointer to the parent.
   * <p>
   * It is useful to scan the tree structure from leaves to the root.
   */
  protected JPEG2KResolutionLevel parent = null;

  // INTERNAL ATTRIBUTES
  /**
   * Records the number of blocks in the wide dimension.
   * <p>
   * Used and computed by the {@link #getNumBlocksWide(int)} method.
   */
  private int[] numBlocksWide = {-1, -1, -1, -1};

  private int[] numBlocksHigh = {-1, -1, -1, -1};

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param parent
   * @param precinctIndex
   */
  public JPEG2KPrecinct(JPEG2KResolutionLevel parent,
                        int precinctIndex) {

    if (precinctIndex < 0) {
      throw new IllegalArgumentException();
    }

    this.parent = parent;
    this.precinctIndex = precinctIndex;

    // Calculate the inClassIdentifier
    inClassIdentifier = JPEG2000Util.TCPToInClassIdentifier(
            parent.parent.parent.index,
            parent.parent.parent.parent.getNumTiles(),
            parent.parent.component,
            parent.parent.parent.parent.getZSize(),
            parent.getFirstPrecinctIndex() + precinctIndex);
  }

  /**
   * Returns the {@link #precinctIndex} attribute.
   *
   * @return the {@link #precinctIndex} attribute.
   */
  public int getIndex() {
    return precinctIndex;
  }

  /**
   *
   * @return
   */
  public JPEG2KResolutionLevel getParent() {
    return parent;
  }

  /**
   * Returns the {@link #inClassIdentifier} attribute.
   *
   * @return the {@link #inClassIdentifier} attribute.
   */
  public long getInClassIdentifier() {
    return inClassIdentifier;
  }

  /**
   * Sets attributes to their initial values
   */
  public void reset() {
    precinctIndex = -1;
    inClassIdentifier = -1;
  }

  /**
   *
   * @return
   */
  public CADIRectangle getBounds() {
    return parent.getPrecinctBounds(precinctIndex);
  }

  /**
   * Returns the number of subbands.
   *
   * @return the number of subbands.
   */
  public int getNumSubbands() {
    return parent.rLevel == 0 ? 1 : 3;
  }

  /**
   *
   * @param subband a subband name. Allowed values are
   *      {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#LL},
   *      {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#HL}
   *      {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#LH}
   *      {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#HH}.
   * @return
   */
  public int getNumBlocksWide(int subband) {
    if ((parent.rLevel == 0) && (subband != JPEG2KResolutionLevel.LL)) {
      throw new IllegalArgumentException("Res. level 0 has only one subband");
    }

    if (numBlocksWide[subband] < 0) {

      CADIRectangle subbandBounds = parent.getSubbandBounds(subband);
      subbandBounds.x <<= 1;
      subbandBounds.width <<= 1;

      CADIRectangle precinctBounds = getBounds();
      CADIRectangle intersection = subbandBounds.intersection(precinctBounds);

      numBlocksWide[subband] = (parent.rLevel > 0)
              ? (int) Math.ceil(1.0 * intersection.width / (parent.getBlockWidth() << 1))
              : (int) Math.ceil(1.0 * intersection.width / parent.getBlockWidth());
    }

    return numBlocksWide[subband];
  }

  /**
   *
   * @param subband a subband name. Allowed values are
   *      {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#LL},
   *      {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#HL}
   *      {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#LH}
   *      {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel#HH}.
   * @return
   */
  public int getNumBlocksHigh(int subband) {
    if ((parent.rLevel == 0) && (subband != JPEG2KResolutionLevel.LL)) {
      throw new IllegalArgumentException("Res. level 0 has only one subband");
    }

    if (numBlocksHigh[subband] < 0) {
      CADIRectangle subbandBounds = parent.getSubbandBounds(subband);
      subbandBounds.y <<= 1;
      subbandBounds.height <<= 1;

      CADIRectangle precinctBounds = getBounds();
      CADIRectangle intersection = subbandBounds.intersection(precinctBounds);

      numBlocksHigh[subband] = (parent.rLevel > 0)
              ? (int) Math.ceil(1.0 * intersection.height / (parent.getBlockWidth() << 1))
              : (int) Math.ceil(1.0 * intersection.height / parent.getBlockWidth());
    }
    return numBlocksHigh[subband];
  }

  /**
   * Returns the number of blocks (in wide and high) belonging to this precinct.
   *
   * @param subband
   * @return
   */
  public CADIDimension getNumBlocks(int subband) {
    return new CADIDimension(getNumBlocksWide(subband),
            getNumBlocksHigh(subband));
  }

  /**
   * Computes the wide of the block (<code>yBlock</code>,<code>xBlock</code>)
   * in the subband <code>subband</code>.
   * 
   * @param subband
   * @param yBlock
   * @param xBlock
   * 
   * @return
   */
  public int getBlockWidth(int subband, int yBlock, int xBlock) {
    int blockWidth = parent.getBlockWidth();
    int nBlocksWide = getNumBlocksWide(subband);
    CADIDimension subbandDims = parent.getSubbandSize(subband);

    return ((xBlock < nBlocksWide - 1) || (subbandDims.width % blockWidth == 0))
            ? blockWidth
            : subbandDims.width % blockWidth;
  }
  
  /**
   * Computes the high of the block (<code>yBlock</code>,<code>xBlock</code>)
   * in the subband <code>subband</code>.
   * 
   * @param subband
   * @param yBlock
   * @param xBlock
   * 
   * @return
   */
  public int getBlockHeight(int subband, int yBlock, int xBlock) {
    int blockHeight = parent.getBlockHeight();
    int nBlocksHigh = getNumBlocksHigh(subband);
    CADIDimension subbandDims = parent.getSubbandSize(subband);
    
    return ((yBlock < nBlocksHigh - 1) || (subbandDims.height % blockHeight == 0))
            ? blockHeight
            : subbandDims.height % blockHeight;
  }
  
  /**
   * Returns the real size of the block considering the bounds of the subband
   * and precinct in which it is. 
   * 
   * @param subband
   * @param yBlock
   * @param xBlock
   * @return
   */
  public CADIDimension getBlockSize(int subband, int yBlock, int xBlock) {
    return new CADIDimension(getBlockWidth(subband, yBlock, xBlock),
            getBlockHeight(subband, yBlock, xBlock));
  }

  /**
   * Returns the tile which the precinct belongs.
   *
   * @return the tile index.
   */
  public int getTile() {
    return parent.parent.parent.index;
  }

  /**
   * Returns the component which the precinct belongs.
   *
   * @return the number of component.
   */
  public int getComponent() {
    return parent.parent.component;
  }

  /**
   * Returns the resolution level which the precinct belongs.
   *
   * @return the resolution level.
   */
  public int getResolutionLevel() {
    return parent.rLevel;
  }
  
  /**
   * 
   * @return 
   */
  public JPEG2KTile getTileObj() {
    return parent.parent.parent;
  }
  
  /**
   * 
   * @return 
   */
  public JPEG2KComponent getComponentObj() {
    return parent.parent;
  }
  
  /**
   * 
   * @return 
   */
  public JPEG2KResolutionLevel getResolutionLevelObj() {
    return parent;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";
    str += getClass().getName() + " [";

    str += "PrecinctIndex=" + precinctIndex;
    str += ", InClassIdentifier=" + inClassIdentifier;

    str += ", Num. blocks wide=";
    for (int i = 0; i < numBlocksWide.length; i++) {
      str += numBlocksWide[i]+" ";
    }
    
    str += ", Num. blocks high=";
    for (int i = 0; i < numBlocksHigh.length; i++) {
      str += numBlocksHigh[i]+" ";
    }
    
    str += "]";

    return str;
  }

  /**
   * Prints this JPEG2K Precinct out to the specified output stream. This
   * method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- JPEG2K Precinct --");

    out.println("PrecinctIndex: " + precinctIndex);
    out.println("InClassIdentifier: " + inClassIdentifier);
    out.print("Num. blocks wide: ");
    for (int i = 0; i < numBlocksWide.length; i++) {
      out.print(numBlocksWide[i]+" ");
    }
    out.println();
    out.print("Num. blocks high: ");
    for (int i = 0; i < numBlocksHigh.length; i++) {
      out.print(numBlocksHigh[i]+" ");
    }
    out.println();

    out.flush();
  }
  // ============================ private methods ==============================
}
