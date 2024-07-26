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

import CADI.Common.LogicalTarget.JPEG2000.Parameters.COCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.QCCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters;
import CADI.Common.Util.CADIDimension;
import CADI.Common.Util.CADIPoint;
import CADI.Common.Util.CADIRectangle;

/**
 * This class stores information to deal with a tile.
 * <p>
 * This class plays a role as a children of the
 * {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestrea} class.
 * <p>
 * Specific parameters (COD, COC, QCD, QCC) can be set for a tile. If some of
 * this parameters has been set, they are applied to this tile and
 * tile-components depending of the tile. Otherwise, the default parameters
 * defined in the {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream}
 * class are applied.
 * <p>
 * As a member of a tree structure there exist an attribute that links with the
 * father ({@link #parent}) and a table containing the children
 * ({@link #components}.
 * <p>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; getMethods<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2010/12/11
 */
public class JPEG2KTile {

  /**
   * Records the tile index.
   * <p>
   * Only positive values are allowed.
   */
  protected int index = -1;

  /**
   * Is a pointer to the parent object.
   * <p>
   * It is useful to scan the tree structure from leaves to the root.
   */
  protected JPEG2KCodestream parent = null;

  /**
   * It is a hash table to store the components in which this tile is divided.
   * <p>
   * The key of the hash is the component index.
   */
  protected HashMap<Integer, JPEG2KComponent> components = null;

  /**
   * Coding style default (COD) for this tile.
   * <p>
   * If this object is <code>null</code> the parameters to be applied to this
   * tile are the default parameters for the codestream,
   * {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream#codParameters}.
   */
  protected CODParameters codParameters = null;

  /**
   * Coding style of component (COC) for this tile.
   * <p>
   * If this object is <code>null</code> the parameters to be applied to this
   * tile are the default parameters for the codestream,
   * {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream#cocParametersList}.
   */
  protected HashMap<Integer, COCParameters> cocParametersList = null;

  /**
   * Quantization default (QCD) for this tile.
   * <p>
   * If this object is <code>null</code> the parameters to be applied to this
   * tile are the default parameters for the codestream,
   * {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream#qcdParameters}.
   */
  protected QCDParameters qcdParameters = null;

  /**
   * Quantization of component (QCC) for this tile.
   * <p>
   * If this object is <code>null</code> the parameters to be applied to this
   * tile are the default parameters for the codestream,
   * {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream#qccParametersList}.
   */
  protected HashMap<Integer, QCCParameters> qccParametersList = null;

  // INTERNAL ATTRIBUTES
  /**
   * Indexes of the tile in terms of horizontal and vertical position.
   * <p>
   * It is used by the {@link #getIndexes()} method.
   */
  private CADIPoint indexes = null;

  /**
   * Coordinates of the upper-left corner in the reference grid.
   * <p>
   * It is used by the {@link #getLocation()} method.
   */
  private CADIPoint location = null;

  /**
   * Bounds (location and size) of the tile in the reference grid.
   * <p>
   * It is computed by the {@link #getBounds()} method.
   */
  private CADIRectangle bounds = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param parent definition in {@link #parent}.
   * @param index definition in {@link #index}.
   */
  public JPEG2KTile(JPEG2KCodestream parent, int index) {
    if (index < 0) {
      throw new IllegalArgumentException();
    }

    this.parent = parent;
    this.index = index;

    components = new HashMap<Integer, JPEG2KComponent>(parent.getZSize());
  }

  /**
   * Returns the {@link #index} attribute.
   *
   * @return the {@link #index} attribute.
   */
  public int getIndex() {
    return index;
  }

  /**
   * Returns the indexes of the tile in terms of horizontal and vertical
   * position.
   *
   * @return the indexes (horizontal and vertical) of the tile.
   */
  public CADIPoint getIndexes() {
    if (indexes == null) {
      int numXTiles =
              (int) Math.ceil(1.0
              * (parent.sizParameters.xSize - parent.sizParameters.XTOsize)
              / parent.sizParameters.XTsize);

      indexes = new CADIPoint(index % numXTiles,
              (int) Math.floor(1.0 * index / numXTiles));
    }

    return new CADIPoint(indexes);
  }

  /**
   * Creates a new component in the tile.
   * 
   * @param component definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent#component}.
   */
  public void createComponent(int component) {
    if (!components.containsKey(component)) {
      components.put(component, new JPEG2KComponent(this, component));
    }
  }

  /**
   * Returns the component whose value is <code>component</code>.
   *
   * @param component definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent#component}.
   * 
   * @return a component object.
   */
  public JPEG2KComponent getComponent(int component) {
    return (components.containsKey(component)) ? components.get(component)
            : null;
  }

  /**
   * Removes a component from the tile.
   * <p>
   * It also removes all resolution levels included in the component.
   * 
   * @param component definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent#component}.
   */
  public void removeComponent(int component) {
    if (components.containsKey(component)) {
      components.get(component).deleteAllResolutionLevels();
      components.remove(component);
    }
  }

  public void removeAllComponents() {
    components.clear();
  }

  /**
   * Returns the coordinates of the upper-left corner in the reference grid.
   *
   * @return the location of the tile in the reference grid.
   */
  public CADIPoint getLocation() {
    if (location == null) {
      getIndexes(); // indexes must have been computed
      int tx_0 =
              Math.max(parent.sizParameters.XTOsize + indexes.x
              * parent.sizParameters.XTsize,
              parent.sizParameters.XOsize);
      int ty_0 =
              Math.max(parent.sizParameters.YTOsize + indexes.y
              * parent.sizParameters.YTsize,
              parent.sizParameters.YOsize);
      return new CADIPoint(tx_0, ty_0);
    }

    return new CADIPoint(location);
  }

  /**
   * Returns the size (width and height) of the tile in the reference grid.
   *
   * @return the size of the tile in the reference grid.
   */
  public CADIDimension getSize() {
    getBounds();
    return new CADIDimension(bounds.width, bounds.height);

  }

  /**
   * Returns the bounds (location and size) of the tile in the reference grid.
   *
   * @return
   */
  public CADIRectangle getBounds() {
    if (bounds == null) {
      getIndexes();
      int tx_0 =
              Math.max(parent.sizParameters.XTOsize + indexes.x
              * parent.sizParameters.XTsize,
              parent.sizParameters.XOsize);
      int ty_0 =
              Math.max(parent.sizParameters.YTOsize + indexes.y
              * parent.sizParameters.YTsize,
              parent.sizParameters.YOsize);
      int tx_1 =
              Math.max(parent.sizParameters.XTOsize + (indexes.x + 1)
              * parent.sizParameters.XTsize,
              parent.sizParameters.XOsize);
      int ty_1 =
              Math.max(parent.sizParameters.YTOsize + (indexes.y + 1)
              * parent.sizParameters.YTsize,
              parent.sizParameters.YOsize);
      bounds = new CADIRectangle(tx_0, ty_0, tx_1 - tx_0, ty_1 - ty_0);
    }

    return new CADIRectangle(bounds);
  }

  /**
   * Returns the upper-left corner of the tile within the domain of the image
   * component.
   *
   * @param component
   * @return location within the domain of the image component.
   */
  public CADIPoint getLocation(int component) {
    CADIPoint tLocation = getLocation();
    tLocation.x =
            (int) Math.ceil(1.0 * tLocation.x
            / parent.sizParameters.XRsiz[component]);
    tLocation.y =
            (int) Math.ceil(1.0 * tLocation.y
            / parent.sizParameters.YRsiz[component]);
    return tLocation;
  }

  /**
   * Returns the size of the tile within the domain of the image component.
   *
   * @param component
   * @return size within the domain of the image component.
   */
  public CADIDimension getSize(int component) {
    CADIRectangle tBounds = getBounds(component);
    return new CADIDimension(tBounds.width, tBounds.height);
  }

  /**
   * Returns the bounds of the tile within the domain of the image component.
   *
   * @param component
   * @return bounds within the domain of the image component.
   */
  public CADIRectangle getBounds(int component) {
    CADIRectangle tBounds = getBounds();

    tBounds.x = (int) Math.ceil(1.0 * tBounds.x
            / parent.sizParameters.XRsiz[component]);
    tBounds.y = (int) Math.ceil(1.0 * tBounds.y
            / parent.sizParameters.YRsiz[component]);
    int tcx_1 = (int) Math.ceil(1.0 * (tBounds.x + tBounds.width)
            / parent.sizParameters.XRsiz[component]);
    int tcy_1 = (int) Math.ceil(1.0 * (tBounds.y + tBounds.height)
            / parent.sizParameters.YRsiz[component]);
    tBounds.width = tcx_1 - tBounds.x;
    tBounds.height = tcy_1 - tBounds.y;

    return tBounds;
  }

  /**
   * Returns the {@link #parent} attribute.
   *
   * @return the {@link #parent} attribute.
   */
  public JPEG2KCodestream getParent() {
    return parent;
  }

  /**
   *
   * @param inClassIdentifier
   * @return
   */
  public int findComponent(long inClassIdentifier) {
    assert (index == (int) (inClassIdentifier % parent.getNumTiles()));
    int temp =
            (int) ((inClassIdentifier - index) / (double) parent.getNumTiles());
    return (temp % parent.getZSize());
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

  // COD methods
  /**
   *
   * @param component
   * @return
   */
  public int getWTLevels(int component) {

    if ((cocParametersList != null) && cocParametersList.containsKey(component)) {
      return cocParametersList.get(component).WTLevels;
    } else if (codParameters != null) {
      return codParameters.WTLevels;
    } else {
      return parent.getWTLevels();
    }
  }

  /**
   *
   * @param component
   * @return
   */
  public int getWTType(int component) {

    if ((cocParametersList != null) && cocParametersList.containsKey(component)) {
      return cocParametersList.get(component).WTType;
    } else if (codParameters != null) {
      return codParameters.WTType;
    } else {
      return parent.getWTType();
    }
  }

  /**
   * Check if the bypass flag is active.
   * 
   * @param component definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent#component}.
   * 
   * @return <code>true</code> if bypass flag is active. Otherwise, returns <code>false</code>.
   */
  public boolean isBypass(int component) {
    if ((cocParametersList != null) && cocParametersList.containsKey(component)) {
      return cocParametersList.get(component).bypass;
    } else if (codParameters != null) {
      return codParameters.bypass;
    } else {
      return parent.isBypass(component);
    }
  }

  /**
   * Check if the reset flag is active.
   * 
   * @param component definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent#component}.
   * 
   * @return <code>true</code> if reset flag is active. Otherwise, returns <code>false</code>.
   */
  public boolean isReset(int component) {

    if ((cocParametersList != null) && cocParametersList.containsKey(component)) {
      return cocParametersList.get(component).reset;
    } else if (codParameters != null) {
      return codParameters.reset;
    } else {
      return parent.isReset(component);
    }
  }

  /**
   * Check if the restart flag is active.
   * 
   * @param component definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent#component}.
   * 
   * @return <code>true</code> if restart flag is active. Otherwise, returns <code>false</code>.
   */
  public boolean isRestart(int component) {

    if ((cocParametersList != null) && cocParametersList.containsKey(component)) {
      return cocParametersList.get(component).restart;
    } else if (codParameters != null) {
      return codParameters.restart;
    } else {
      return parent.isRestart(component);
    }
  }

  /**
   * Check if the causal flag is active.
   * 
   * @param component definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent#component}.
   * 
   * @return <code>true</code> if causal flag is active. Otherwise, returns <code>false</code>.
   */
  public boolean isCausal(int component) {

    if ((cocParametersList != null) && cocParametersList.containsKey(component)) {
      return cocParametersList.get(component).causal;
    } else if (codParameters != null) {
      return codParameters.causal;
    } else {
      return parent.isCausal(component);
    }
  }

  /**
   * Check if the erterm flag is active.
   * 
   * @param component definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent#component}.
   * 
   * @return <code>true</code> if erterm flag is active. Otherwise, returns <code>false</code>.
   */
  public boolean isErterm(int component) {

    if ((cocParametersList != null) && cocParametersList.containsKey(component)) {
      return cocParametersList.get(component).erterm;
    } else if (codParameters != null) {
      return codParameters.erterm;
    } else {
      return parent.isErterm(component);
    }
  }

  /**
   * Check if the segmark flag is active.
   * 
   * @param component definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent#component}.
   * 
   * @return <code>true</code> if segmark flag is active. Otherwise, returns <code>false</code>.
   */
  public boolean isSegmark(int component) {

    if ((cocParametersList != null) && cocParametersList.containsKey(component)) {
      return cocParametersList.get(component).segmark;
    } else if (codParameters != null) {
      return codParameters.segmark;
    } else {
      return parent.isSegmark(component);
    }
  }

  /**
   *
   * @param component
   * @return
   */
  public int getBlockHeight(int component) {

    if ((cocParametersList != null) && cocParametersList.containsKey(component)) {
      return 1 << cocParametersList.get(component).blockHeight;
    } else if (codParameters != null) {
      return 1 << codParameters.blockHeight;
    } else {
      return parent.getBlockHeight(component);
    }
  }

  /**
   *
   * @param component
   * @return
   */
  public int getBlockWidth(int component) {

    if ((cocParametersList != null) && cocParametersList.containsKey(component)) {
      return 1 << cocParametersList.get(component).blockWidth;
    } else if (codParameters != null) {
      return 1 << codParameters.blockWidth;
    } else {
      return parent.getBlockWidth(component);
    }
  }

  /**
   *
   * @param component
   * @param rLevel
   * @return
   */
  public int getPrecinctHeights(int component, int rLevel) {
    int height = -1;
    if ((cocParametersList != null) && cocParametersList.containsKey(component)) {
      height = 1 << cocParametersList.get(component).precinctHeights[rLevel];
    } else if (codParameters != null) {
      height = 1 << codParameters.precinctHeights[rLevel];
    } else {
      height = parent.getPrecinctHeights(component, rLevel);
    }
    CADIDimension rLevelSize = components.get(component).getSize(rLevel);
            //components.get(component).resolutionLevels.get(rLevel).getSize();

    return (height <= rLevelSize.height) ? height : rLevelSize.height;
  }

  /**
   *
   * @param component
   * @param rLevel
   * @return
   */
  public int getPrecinctWidths(int component, int rLevel) {
    int width = -1;
    if ((cocParametersList != null) && cocParametersList.containsKey(component)) {
      width = 1 << cocParametersList.get(component).precinctWidths[rLevel];
    } else if (codParameters != null) {
      width = 1 << codParameters.precinctWidths[rLevel];
    } else {
      width = parent.getPrecinctWidths(component, rLevel);
    }
    CADIDimension rLevelSize = components.get(component).getSize(rLevel);
    //components.get(component).resolutionLevels.get(rLevel).getSize();

    return (width <= rLevelSize.width) ? width : rLevelSize.width;
  }

  // QCD Methods
  /**
   *
   * @param component
   * @return
   */
  public int getQuantizationStyle(int component) {
    if ((qccParametersList != null) && cocParametersList.containsKey(component)) {
      return qccParametersList.get(component).quantizationStyle;
    } else if (qcdParameters != null) {
      return qcdParameters.quantizationStyle;
    } else {
      return parent.getQuantizationStyle();
    }
  }

  /**
   * Returns the exponent for the component <code>component</code>, resolution
   * level<code>rLevel</code> and subband <code>subband</code>.
   * 
   * @param component
   * @param rLevel
   * @param subband
   * @return 
   */
  public int getExponent(int component, int rLevel, int subband) {
    if ((rLevel == 0) && (subband != JPEG2KResolutionLevel.LL)) {
      throw new IllegalArgumentException("Res. level 0 has only one subband");
    }
    if ((rLevel != 0) && (subband == JPEG2KResolutionLevel.LL)) {
      throw new IllegalArgumentException("Only res. level 0 has the LL subband");
    }
    int exponent = -1;
    if ((qccParametersList != null) && qccParametersList.containsKey(component)) {
      exponent = qccParametersList.get(component).exponents[rLevel][rLevel == 0 ? 0 : subband - 1];
    } else if (qcdParameters != null) {
      exponent = qcdParameters.exponents[rLevel][rLevel == 0 ? 0 : subband - 1];
    } else {
      exponent = parent.getExponent(component, rLevel, subband);
    }
    return exponent;
  }

  /**
   * Returns the mantisa for the component <code>component</code>, resolution
   * level<code>rLevel</code> and subband <code>subband</code>.
   * 
   * @param component
   * @param rLevel
   * @param subband
   * @return 
   */
  public int getMantisa(int component, int rLevel, int subband) {
    if ((rLevel == 0) && (subband != JPEG2KResolutionLevel.LL)) {
      throw new IllegalArgumentException("Res. level 0 has only one subband");
    }
    if ((rLevel != 0) && (subband == JPEG2KResolutionLevel.LL)) {
      throw new IllegalArgumentException("Only res. level 0 has the LL subband");
    }
    int mantisa = -1;
    if ((qccParametersList != null) && qccParametersList.containsKey(component)) {
      mantisa = qccParametersList.get(component).mantisas[rLevel][rLevel == 0 ? 0 : subband - 1];
    } else if (qcdParameters != null) {
      mantisa = qcdParameters.mantisas[rLevel][rLevel == 0 ? 0 : subband - 1];
    } else {
      mantisa = parent.getMantisa(component, rLevel, subband);
    }
    return mantisa;
  }

  /**
   *
   * @param component
   * @return
   */
  public int getGuardBits(int component) {
    if ((qccParametersList != null) && cocParametersList.containsKey(component)) {
      return qccParametersList.get(component).guardBits;
    } else if (qcdParameters != null) {
      return qcdParameters.guardBits;
    } else {
      return parent.getGuardBits(component);
    }
  }

  /**
   *
   * @return
   */
  public int getProgressionOrder() {
    return (codParameters != null) ? codParameters.progressionOrder
            : parent.codParameters.progressionOrder;
  }

  /**
   * Returns the number of layers that this tile contains.
   *
   * @return number of layers
   */
  public int getNumLayers() {
    return (codParameters != null) ? codParameters.numLayers
            : parent.codParameters.numLayers;
  }

  /**
   * Check if Start of Packets (SOP) are used.
   *
   * @return <code>true</code> if SOP are used. Otherwise, return <code>false</code>.
   */
  public boolean useSOP() {
    return (codParameters != null) ? codParameters.useSOP
            : parent.useSOP();
  }

  /**
   * Check if End of Packets Headers (EPH) are used.
   * 
   * @return <code>false</code> if EPH are used. Otherwise, return <code>false</code>.
   */
  public boolean useEPH() {
    return (codParameters != null) ? codParameters.useEPH
            : parent.useEPH();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";
    str += getClass().getName() + " [";

    str += "Index=" + index;
    str += codParameters.toString();
    for (Entry<Integer, COCParameters> param : cocParametersList.entrySet()) {
      str += param.getValue().toString();
    }
    str += qcdParameters.toString();
    for (Entry<Integer, QCCParameters> param : qccParametersList.entrySet()) {
      str += param.getValue().toString();
    }

    for (Entry<Integer, JPEG2KComponent> comp : components.entrySet()) {
      str += comp.getValue().toString();
    }

    str += "]";

    return str;
  }

  /**
   * Prints this JPEG2K Tile out to the specified output stream. This
   * method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- JPEG2K Tile --");

    out.println("Index: " + index);

    if (codParameters != null) {
      codParameters.list(out);
    }

    if (cocParametersList != null) {
      for (Entry<Integer, COCParameters> param : cocParametersList.entrySet()) {
        param.getValue().list(out);
      }
    }

    if (qcdParameters != null) {
      qcdParameters.list(out);
    }

    if (qccParametersList != null) {
      for (Entry<Integer, QCCParameters> param : qccParametersList.entrySet()) {
        param.getValue().list(out);
      }
    }

    for (Entry<Integer, JPEG2KComponent> comp : components.entrySet()) {
      comp.getValue().list(out);
    }


    out.flush();
  }
  // ============================ private methods ==============================
}
