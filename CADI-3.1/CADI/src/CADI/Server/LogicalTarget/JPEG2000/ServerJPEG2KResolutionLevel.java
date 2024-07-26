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
package CADI.Server.LogicalTarget.JPEG2000;

import java.io.PrintStream;

import CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel;
import CADI.Common.Util.CADIDimension;
import CADI.Common.Util.CADIRectangle;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.2 2011/10/12
 */
public class ServerJPEG2KResolutionLevel extends JPEG2KResolutionLevel {

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public ServerJPEG2KResolutionLevel(ServerJPEG2KComponent parent,
                                     int rLevel) {
    super(parent, rLevel);
  }

  /**
   * 
   * @return
   */
  @Override
  public ServerJPEG2KComponent getParent() {
    return (ServerJPEG2KComponent)parent;
  }

  @Override
  public void createPrecinct(int index) {
    if (!precincts.containsKey(index))
      precincts.put(index, new ServerJPEG2KPrecinct(this, index));
  }

  /**
   * Creates a new precinct object.
   * 
   * @param index the precinct index within the tile-component-resolution level.
   * @param vIndex the real precinct index whithin the tile-component-resolution level.
   *
   * @throws IllegalAccessException if the precinct exists.
   */
  public void createPrecinct(int index, int rIndex) {
    if (!precincts.containsKey(index))
      precincts.put(index, new ServerJPEG2KPrecinct(this, index, rIndex));
  }

  /**
   * 
   */
  @Override
  public ServerJPEG2KPrecinct getPrecinct(int index) {
    return (ServerJPEG2KPrecinct)precincts.get(index);
  }
  
  @Override
  public void removePrecinct(int index) {
    if (precincts.containsKey(index)) precincts.remove(index);
  }
  
  @Override
  public void removeAllPrecincts() {
    super.removeAllPrecincts();
  }
  
  /**
   *
   * @return
   */
  public int getOriginalPrecinctWidth() {
    return parent.getParent().getPrecinctWidths(parent.getComponent(), rLevel);
  }

  /**
   *
   * @return
   */
  public int getOriginalPrecinctHeight() {
    return parent.getParent().getPrecinctHeights(parent.getComponent(), rLevel);
  }
  
  /**
   *
   * @return
   */
  public CADIDimension getOriginalPrecinctSizes() {
    return new CADIDimension(getOriginalPrecinctWidth(), getOriginalPrecinctHeight());
  }
  
  public int getOriginalBlockWidth() {
    int precWidth = getParent().getOriginalPrecinctWidths(rLevel);
    if (rLevel > 0) precWidth = (precWidth + 1) >>> 1;
    return Math.min(getParent().getOriginalBlockWidth(), precWidth);
  }

  public int getOriginalBlockHeight() {
    int precHeight = getParent().getOriginalPrecinctHeights(rLevel);
    if (rLevel > 0) precHeight = (precHeight + 1) >>> 1;
    return Math.min(getParent().getOriginalBlockHeight(), precHeight);
  }

  /**
   * Returns the number of virtual precincts in the wide dimension.
   *
   * @return
   */
  public int getNumOriginalPrecinctsWide() {
    CADIRectangle bounds = getBounds();
    int tmp = getParent().getOriginalPrecinctWidths(rLevel);
    return (int)Math.ceil(1.0 * (bounds.x + bounds.width) / tmp)
            - (int)Math.floor(1.0 * bounds.x / tmp);
  }

  /**
   * Returns the number of virtual precincts in the heigh dimension.
   *
   * @return
   */
  public int getNumOriginalPrecinctsHeigh() {
    CADIRectangle bounds = getBounds();
    int tmp = getParent().getOriginalPrecinctHeights(rLevel);
    return (int)Math.ceil(1.0 * (bounds.y + bounds.height) / tmp)
            - (int)Math.floor(1.0 * bounds.y / tmp);
  }

   /**
   * Returns the number of virtual precincts of the tile-component-resolution revel.
   *
   * @return the number of precincts.
   */
  public int getNumOriginalPrecincts() {
    return getNumOriginalPrecinctsWide() * getNumOriginalPrecinctsHeigh();
  }

   /**
   * Returns the number of blocks per precinct.
   * <p>
   * TODO: Method name will be changed to getBlocksPerPrecinctWide
   *
   * @return
   */
  public int getOriginalBlocksPerPrecinctWidths() {
    int blockWidth = getOriginalBlockWidth();
    if (rLevel > 0) blockWidth <<= 1;
    return (int)Math.ceil(1.0 * getOriginalPrecinctWidth() / blockWidth);
  }

  /**
   * Returns the number of blocks per precinct.
   * <p>
   * TODO: Method name will be changed to getBlocksPerPrecinctHigh
   *
   * @return
   */
  public int getOriginalBlocksPerPrecinctHeights() {
    int blockHeight = getOriginalBlockHeight();
    if (rLevel > 0) blockHeight <<= 1;
    return (int)Math.ceil(1.0 * getOriginalPrecinctHeight() / blockHeight);
  }
  
   /**
   * Returns the number of blocks per precinct in the wide direccion.
   *
   * @return
   */
  public int getOriginalBlocksPerPrecinctWide() {
    return (int)Math.ceil(1.0 * getOriginalPrecinctWidth() / getOriginalBlockWidth());
  }

  /**
   * Returns the number of blocks per precinct in the high direccion.
   *
   * @return
   */
  public int getOriginalBlocksPerPrecinctHigh() {
    return (int)Math.ceil(1.0 * getOriginalPrecinctHeight() / getOriginalBlockHeight());
  }

  /**
   * Returns the number of blocks per precinct.
   *
   * @return
   */
  public CADIDimension getOriginalBlocksPerPrecinct() {
    return new CADIDimension(getOriginalBlocksPerPrecinctWidths(),
            getOriginalBlocksPerPrecinctHeights());
  }

   /**
   *
   * @param precinctIndex
   * @return
   */
  public CADIRectangle getOriginalPrecinctBounds(int orgPrecinctIndex) {
    CADIRectangle bounds = new CADIRectangle();

    CADIDimension precDims = getOriginalPrecinctSizes();
    int numXPrecincts = getNumOriginalPrecinctsWide();
    int xPrecinct = orgPrecinctIndex % numXPrecincts;
    int yPrecinct = (int)Math.floor(1.0 * orgPrecinctIndex / numXPrecincts);
    int xEnd = (xPrecinct + 1) * precDims.width;
    int yEnd = (yPrecinct + 1) * precDims.height;

    CADIRectangle rLevelBounds = getBounds();
    if (xEnd > rLevelBounds.x + rLevelBounds.width)
      xEnd = rLevelBounds.x + rLevelBounds.width;

    if (yEnd > rLevelBounds.y + rLevelBounds.height)
      yEnd = rLevelBounds.y + rLevelBounds.height;

    bounds.x = xPrecinct * precDims.width;
    bounds.y = yPrecinct * precDims.height;
    bounds.width = xEnd - bounds.x;
    bounds.height = yEnd - bounds.y;

    // Absolute coordinates
    bounds.x += rLevelBounds.x;
    bounds.y += rLevelBounds.y;

    return bounds;
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

    out.println("-- Server JPEG2K Resolution Level --");
    super.list(out);

  }

  // ============================ private methods ==============================
}
