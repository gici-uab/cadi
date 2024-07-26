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
package CADI.Server.LogicalTarget.JPEG2000;

import CADI.Common.LogicalTarget.JPEG2000.JPEG2000Util;
import java.io.PrintStream;

import CADI.Common.LogicalTarget.JPEG2000.JPEG2KPrecinct;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel;
import CADI.Common.Util.CADIDimension;
import CADI.Common.Util.CADIRectangle;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/10/10
 */
public class ServerJPEG2KPrecinct extends JPEG2KPrecinct {

  /**
   * The meaning is the same as {@link #precinctIndex}, but it records the
   * original precinct index when transconding is performed.
   */
  protected int orgPrecinctIndex = -1;

  /**
   * The meaning is the same as {@link #inClassIdentifier}, but it records the
   * original in class identifier when transconding is performed.
   */
  protected long orgInClassIdentifier = -1;
  
  protected CADIDimension orgPrecincts = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public ServerJPEG2KPrecinct(ServerJPEG2KResolutionLevel parent, int index) {
    super(parent, index);
  }

  /**
   *
   * @param parent
   * @param precinctIndex
   * @param rPrecinctIndex
   */
  public ServerJPEG2KPrecinct(ServerJPEG2KResolutionLevel parent,
          int precinctIndex, int rPrecinctIndex) {

    this(parent, precinctIndex);

    if (rPrecinctIndex < 0) throw new IllegalArgumentException();

    this.orgPrecinctIndex = rPrecinctIndex;

    // Calculate the vInClassIdentifier
    int tile = parent.getParent().getParent().getIndex();
    int component = parent.getParent().getComponent();
    int resLevel = parent.getResolutionLevel();

    int rPrecinct = 0;
    for (int rLevel = 0; rLevel < resLevel; rLevel++) {
      rPrecinct += ((ServerJPEG2KResolutionLevel)(parent.getParent().
              getResolutionLevel(rLevel))).getNumOriginalPrecincts();
    }
    rPrecinct += rPrecinctIndex;

    orgInClassIdentifier =
            JPEG2000Util.TCPToInClassIdentifier(tile,
            parent.getParent().getParent().getParent().getNumTiles(),
            component,
            parent.getParent().getParent().getParent().getZSize(),
            rPrecinct);
  }

  /**
   * 
   * @return
   */
  @Override
  public ServerJPEG2KResolutionLevel getParent() {
    return (ServerJPEG2KResolutionLevel)parent;
  }

  /**
   *
   * @return
   */
  public CADIRectangle getOriginalBounds() {
    return getParent().getOriginalPrecinctBounds(precinctIndex);
  }

  /**
   * Returns the {@link #rPrecinctIndex} attribute.
   * @return the {@link #rPrecinctIndex} attribute.
   */
  public int getOriginalIndex() {
    return orgPrecinctIndex;
  }

  /**
   * Returns the {@link #vInClassIdentifier} attribute.
   *
   * @return the {@link #vInClassIdentifier} attribute.
   */
  public long getOriginalInClassIdentifier() {
    return orgInClassIdentifier;
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
  public int getNumOriginalBlocksWide(int subband) {
    if ((getResolutionLevel() == 0) && (subband != JPEG2KResolutionLevel.LL))
      throw new IllegalArgumentException("Res. level 0 has only one subband");

    CADIRectangle subbandBounds = getParent().getSubbandBounds(subband);
    subbandBounds.x <<= 1;
    subbandBounds.width <<= 1;

    CADIRectangle precinctBounds = getBounds();
    CADIRectangle intersection = subbandBounds.intersection(precinctBounds);

    int numBlocksWide = (getResolutionLevel() > 0)
            ? (int)Math.ceil(1.0 * intersection.width / (getParent().getOriginalBlockWidth() << 1))
            : (int)Math.ceil(1.0 * intersection.width / getParent().getOriginalBlockWidth());

    return numBlocksWide;
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
  public int getNumOriginalBlocksHigh(int subband) {
    if ((getResolutionLevel() == 0) && (subband != JPEG2KResolutionLevel.LL))
      throw new IllegalArgumentException("Res. level 0 has only one subband");

    CADIRectangle subbandBounds = getParent().getSubbandBounds(subband);
    subbandBounds.y <<= 1;
    subbandBounds.height <<= 1;

    CADIRectangle precinctBounds = getBounds();
    CADIRectangle intersection = subbandBounds.intersection(precinctBounds);

    int numBlocksHigh = (getResolutionLevel() > 0)
            ? (int)Math.ceil(1.0 * intersection.height / (getParent().getOriginalBlockWidth() << 1))
            : (int)Math.ceil(1.0 * intersection.height / getParent().getOriginalBlockWidth());

    return numBlocksHigh;
  }

  /**
   * Returns the number of blocks (in wide and high) belonging to this precinct.
   *
   * @param subband
   * @return
   */
  public CADIDimension getNumOriginalBlocks(int subband) {
    return new CADIDimension(getNumOriginalBlocksWide(subband),
            getNumOriginalBlocksHigh(subband));
  }

  /**
   * Sets attributes to their initial values
   */
  @Override
  public void reset() {
    super.reset();
    orgPrecinctIndex = -1;
    orgInClassIdentifier = -1;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";
    str += getClass().getName() + " [";
    str += super.toString();
    str += ", OriginalPrecinctIndex=" + orgPrecinctIndex;
    str += ", OriginalInClassIdentifier=" + orgInClassIdentifier;

    str += "]";

    return str;
  }

  /**
   * Prints this Server JPEG2K Tile out to the specified output stream. This
   * method is useful for debugging.
   *
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Server JPEG2K Precinct --");
    super.list(out);
    out.println("OriginalPrecinctIndex: " + orgPrecinctIndex);
    out.println("OriginalInClassIdentifier: " + orgInClassIdentifier);

  }
  
  // ============================ private methods ==============================
}
