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
package CADI.Common.LogicalTarget.JPEG2000;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import CADI.Common.LogicalTarget.JPEG2000.Parameters.CBDParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.COCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.COMParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.MCCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.MCOParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.MCTParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.QCCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Common.Util.CADIDimension;
import CADI.Common.Util.CADIPoint;
import CADI.Common.Util.CADIRectangle;
import CADI.Server.LogicalTarget.JPEG2000.DeliveringModes.RelevantPrecinctsFinder;
import java.util.Map;

/**
 * This class stores all information necessary to deal with a codestream.
 * <p>
 * This class plays a main role because:<br>
 * 1) it is the root of a tree structure where tiles are the children,
 * components the grandchildren, and so on. 
 * 2) it records the default parameters for all the tile-components.
 * 3) it can be inherited by other classes. Therefore, other classes can offer
 * another interface and add new features.
 * 
 * <p>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; getMethods<br>
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2010/12/11
 */
public class JPEG2KCodestream {

  /**
   * Records the codestream identifier.
   * <p>
   * Only positive values are allowed.
   */
  protected int identifier = -1;

  /**
   * It is a hash table to store the tiles in which this codestream is divided.
   * <p>
   * The key of the hash is the tile index.
   */
  protected HashMap<Integer, JPEG2KTile> tiles = null;

  /**
   * Image and tile size parameters (SIZ).
   */
  protected SIZParameters sizParameters = null;

  /**
   * Coding style default (COD).
   */
  protected CODParameters codParameters = null;

  /**
   * Coding style of component (COC).
   */
  protected HashMap<Integer, COCParameters> cocParametersList = null;

  /**
   * Quantization default (QCD).
   */
  protected QCDParameters qcdParameters = null;

  /**
   * Quantization component (QCC).
   */
  protected HashMap<Integer, QCCParameters> qccParametersList = null;

  // Region of interest (RGN)
  /**
   * Component bit depth (CBD).
   */
  protected CBDParameters cbdParameters = null;

  /**
   * Multiple component transformation (MCT).
   * <p>
   * The integer of the hash key stand for the stage.
   */
  protected HashMap<Integer, MCTParameters> mctParametersList = null;

  /**
   * Multiple component transform collection (MCC).
   * <p>
   * The integer of the hash key stand for the stage.
   */
  protected HashMap<Integer, MCCParameters> mccParametersList = null;

  /**
   * Multiple component transform ordering (MCO).
   */
  protected MCOParameters mcoParameters = null;

  /**
   * Comments (COM).
   */
  protected COMParameters comParameters = null;

  /**
   * Wrapped used to store non-compliant parameters.
   */
  protected JPKParameters jpkParameters = null;

  // INTERNAL ATTRIBUTES
  /**
   * Records the maximum number of resolution levels.
   * <p>
   * This attribute is used by the {@link #getMaxResolutionLevels()} method. The
   * first time method is called this value is computed, otherwise is just read.
   */
  private int maxRLevels = -1;

  // ============================= public methods ==============================
 
  /**
   * Constructor.
   *
   * @param identifier definition in {@link #identifier}.
   * @param jpcParameters and object of the class
   * 							{@link CADI.Common.LogicalTarget.JPEG2000.JPCParameters}.
   */
  public JPEG2KCodestream(int identifier, JPCParameters jpcParameters) {
    // Check input parameters
    if (identifier < 0) {
      throw new IllegalArgumentException();
    }
    if (jpcParameters == null) {
      throw new NullPointerException();
    }

    // Copy parameters
    this.identifier = identifier;
    sizParameters = jpcParameters.sizParameters;
    codParameters = jpcParameters.codParameters;
    cocParametersList = jpcParameters.cocParametersList;
    qcdParameters = jpcParameters.qcdParameters;
    qccParametersList = jpcParameters.qccParametersList;
    cbdParameters = jpcParameters.cbdParameters;
    mctParametersList = jpcParameters.mctParametersList;
    mccParametersList = jpcParameters.mccParametersList;
    mcoParameters = jpcParameters.mcoParameters;
    comParameters = jpcParameters.comParameters;
    jpkParameters = jpcParameters.jpkParameters;

    tiles = new HashMap<Integer, JPEG2KTile>(getNumTiles());
  }

  /**
   * Returns the {@link #identifier} attribute.
   *
   * @return definition in {@link #identifier}.
   */
  public int getIdentifier() {
    return identifier;
  }

  /**
   * Creates a new tile.
   * 
   * @param index is the tile index.
   */
  public void createTile(int index) {
    if (!tiles.containsKey(index)) {
      tiles.put(index, new JPEG2KTile(this, index));
    }
  }

  /**
   * Returns a the tile object <code>index</code>.
   *
   * @param index definition in{@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KTile#index}.
   * @return a tile object.
   */
  public JPEG2KTile getTile(int index) {
    assert (index >= 0);
    return (tiles.containsKey(index)) ? tiles.get(index) : null;
  }

  /**
   * Removes a tile from the list.
   * 
   * @param  index definition in{@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KTile#index}.
   */
  public void removeTile(int index) {
    if (tiles.containsKey(index)) {
      tiles.get(index).removeAllComponents();
      tiles.remove(index);
    }
  }

  /**
   * Removes all tiles.
   */
  public void removeAllTiles() {
    for (Map.Entry<Integer, JPEG2KTile> entry : tiles.entrySet()) {
      entry.getValue().removeAllComponents();
    }
    tiles.clear();
  }

  /**
   * Returns the index of the tile in which the precinct is located.
   *
   * @param inClassIdentifier definition in
   * 			{@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}.
   * @return the tile index.
   */
  public int findTile(long inClassIdentifier) {
    int numTiles = getNumTiles();
    return ((int) (inClassIdentifier % numTiles));
  }

  /**
   * Returns the tile-component-resolution level-precinct of the precinct
   * given by the unique identifier <code>inClassIdentifier</code>.
   *
   * @param inClassIdentifier definition in
   * 			{@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}.
   * @return an one-dimensional array where the first index stands for tile,
   * the second one is the component, the third index is the resolution level,
   * and the fourth is the precinct within the resolution level.
   */
  public int[] findTCRP(long inClassIdentifier) {
    int[] TCRP = new int[4];

    int[] tcp = findTCP(inClassIdentifier);
    int tile = tcp[0];
    int component = tcp[1];
    int precinct = tcp[2];

    int WTLevels = tiles.get(tile).getComponent(component).getWTLevels();
    int numPrecincts = 0;
    for (int rLevel = 0; rLevel <= WTLevels; rLevel++) {
      //System.out.println("\n\n\ntile: " + tile+ " Comp: " + component+ " Res. Level: " + rLevel);	// DEBUG

      int numPrecinctsRLevel = tiles.get(tile).getComponent(component).getResolutionLevel(rLevel).getNumPrecincts();
      if ((numPrecincts + numPrecinctsRLevel) > precinct) {
        TCRP[0] = tile;
        TCRP[1] = component;
        TCRP[2] = rLevel;
        TCRP[3] = precinct - numPrecincts;
        return TCRP;
      } else {
        numPrecincts += numPrecinctsRLevel;
      }
    }

    return null;
  }

  /**
   * Returns the tile-component-resolution level-precinct of the precinct
   * given by the unique identifier <code>inClassIdentifier</code>.
   *
   * @param inClassIdentifier definition in
   * 			{@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}.
   * 
   * @return an one-dimensional array where the first index stands for tile,
   * the second one is the component, the third index is the precinct within
   * component.
   */
  public int[] findTCP(long inClassIdentifier) {
    int[] tcp = new int[3];

    int numTiles = getNumTiles();
    int numComponents = getZSize();

    tcp[0] = (int) (inClassIdentifier % numTiles);
    int temp = (int) ((inClassIdentifier - tcp[0]) / (double) numTiles);
    tcp[1] = temp % numComponents;
    tcp[2] = (int) ((temp - tcp[1]) / (double) numComponents);

    return tcp;
  }

  /**
   * Returns a unique identifier for the tile-component-resolution level-
   * precinct.
   *
   * @param tile tile in which the precinct is.
   * @param component the component which the resolution level belongs.
   * @param rLevel the resolution level within the tile-component.
   * @param precinct the number precinct within the tile-component-resolution
   * 				level following a raster mode
   *
   * @return definition in
   * 			{@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}.
   */
  public long findInClassIdentifier(int tile, int component,
                                    int rLevel, int precinct) {
    return tiles.get(tile).getComponent(component).getResolutionLevel(rLevel).getPrecinct(precinct).inClassIdentifier;
  }

  /**
   * Returns the precinct identified by the unique precinct identifer <code>
   * inClassIdentifier</code>.
   * 
   * @param inClassIdentifier definition in
   * 			{@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}.
   * 
   * @return a precinct object.
   */
  public JPEG2KPrecinct getPrecinct(long inClassIdentifier) {
    int[] TCRP = findTCRP(inClassIdentifier);
    return tiles.get(TCRP[0]).getComponent(TCRP[1]).getResolutionLevel(TCRP[2]).getPrecinct(TCRP[3]);
  }

  // get SIZ methods
  /**
   * 
   * @return 
   */
  public int getRSize() {
    return sizParameters.Rsiz;
  }

  /**
   * 
   * @return 
   */
  public int getXSize() {
    return sizParameters.xSize;
  }

  /**
   * 
   * @return 
   */
  public int getYSize() {
    return sizParameters.ySize;
  }

  /**
   * 
   * @return 
   */
  public int getXOSize() {
    return sizParameters.XOsize;
  }

  /**
   * 
   * @return 
   */
  public int getYOSize() {
    return sizParameters.YOsize;
  }

  /**
   * 
   * @return 
   */
  public int getXTSize() {
    return sizParameters.XTsize;
  }

  /**
   * 
   * @return 
   */
  public int getYTSize() {
    return sizParameters.YTsize;
  }

  /**
   * 
   * @return 
   */
  public int getXTOSize() {
    return sizParameters.XTOsize;
  }

  /**
   * 
   * @return 
   */
  public int getYTOSize() {
    return sizParameters.YTOsize;
  }

  /**
   * 
   * @return 
   */
  public int getZSize() {
    return sizParameters.zSize;
  }

  /**
   * Returns the number of bits per pixel for the component <code>component</code>.
   * 
   * @param component
   * 
   * @return  number of bits per pixel.
   * 
   */
  public int getPrecision(int component) {
    return sizParameters.precision[component];
  }

  /**
   * Check if the component <code>component</code> is signed.
   * 
   * @param component definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent#component}.
   * 
   * @return <code>true</code> if the component is signed. Otherwise, returns
   *          <code>false</code>.
   */
  public boolean isSigned(int component) {
    return sizParameters.signed[component];
  }

  /**
   * Returns the horizontal separation of sample of component <code>component
   * </code> with respect to the reference grid.
   * 
   * @param component definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent#component}.
   * 
   * @return  horizontal separation of samples.
   */
  public int getXRsize(int component) {
    return sizParameters.XRsiz[component];
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
    return sizParameters.YRsiz[component];
  }

  // get COD methods
  /**
   * Returns the file progression order.
   * 
   * @return progression order
   */
  public int getProgressionOrder() {
    return codParameters.progressionOrder;
  }

  /**
   * Returns number of layers that the final codestream contains.
   * 
   * @return number of layers.
   */
  public int getNumLayers() {
    return codParameters.numLayers;
  }

  /**
   * Returns the transform applied across the components, if it has been applied.
   * 
   * @return the transform applied across components.
   */
  public int getMultiComponentTransform() {
    return codParameters.multiComponentTransform;
  }

  /**
   * Returns the number of levels of the Wavelet transform.
   * 
   * @return number of levels of the Wavelet transform.
   */
  public int getWTLevels() {
    return codParameters.WTLevels;
  }

  /**
   * Returns the type of Wavelet transform applied.
   * 
   * @return type of Wavelet transform.
   */
  public int getWTType() {
    return codParameters.WTType;
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
      assert (true);
    }
    return false;
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
      assert (true);
    }
    return false;
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
      assert (true);
    }
    return false;
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
      assert (true);
    }
    return false;
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
      assert (true);
    }
    return false;
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
      assert (true);
    }
    return false;
  }

  /**
   * Returns the block height.
   * <p>
   * ATTENTION: This method will be deprecated in the next version. Use the
   * {@link #getBlockHeight(int)} method.
   *
   * @return block height.
   */
  public int getBlockHeight() {
    return 1 << codParameters.blockHeight;
  }

  /**
   * Returns the block width.
   * <p>
   * ATTENTION: This method will be deprecated in the next version. Use the
   * {@link #getBlockWidth(int)} method.
   *
   * @return block width.
   */
  public int getBlockWidth() {
    return 1 << codParameters.blockWidth;
  }

  /**
   * Returns the block height for the component <code>component</code>.
   * 
   * @param component definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent#component}.
   * 
   * @return block height.
   */
  public int getBlockHeight(int component) {
    int height = -1;
    if ((cocParametersList != null) && cocParametersList.containsKey(component)) {
      height = 1 << cocParametersList.get(component).blockHeight;
    } else if (codParameters != null) {
      height = 1 << codParameters.blockHeight;
    } else {
      assert (true);
    }

    return height;
  }

  /**
   * Returns the block width for the component <code>component</code>.
   * 
   * @param component definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent#component}.
   * 
   * @return block width.
   */
  public int getBlockWidth(int component) {
    int width = -1;
    if ((cocParametersList != null) && cocParametersList.containsKey(component)) {
      width = 1 << cocParametersList.get(component).blockWidth;
    } else if (codParameters != null) {
      width = 1 << codParameters.blockWidth;
    } else {
      assert (true);
    }

    return width;
  }

  public int getPrecinctHeights(int component, int rLevel) {
    int height = -1;
    if ((cocParametersList != null) && cocParametersList.containsKey(component)) {
      height = 1 << cocParametersList.get(component).precinctHeights[rLevel];
    } else if (codParameters != null) {
      height = 1 << codParameters.precinctHeights[rLevel];
    } else {
      assert (true);
    }
    return height;
  }

  public int getPrecinctWidths(int component, int rLevel) {
    int width = -1;
    if ((cocParametersList != null) && cocParametersList.containsKey(component)) {
      width = 1 << cocParametersList.get(component).precinctWidths[rLevel];
    } else if (codParameters != null) {
      width = 1 << codParameters.precinctWidths[rLevel];
    } else {
      assert (true);
    }
    return width;
  }

  /**
   * Check if End of Packets Headers (SOP) are used.
   * 
   * @return <code>false</code> if SOP are used. Otherwise, return <code>false</code>.
   */
  public boolean useSOP() {
    return codParameters.useSOP;
  }

  /**
   * Check if End of Packets Headers (EPH) are used.
   * 
   * @return <code>false</code> if EPH are used. Otherwise, return <code>false</code>.
   */
  public boolean useEPH() {
    return codParameters.useEPH;
  }

  // get QCD Methods
  
  /**
   * Returns the type of quantization applied.
   * 
   * @return type of quantization.
   */
  public int getQuantizationStyle() {
    return qcdParameters.quantizationStyle;
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
      assert (true);
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
      assert (true);
    }
    return mantisa;
  }

  /**
   * Returns the number of guard bits for the component <code>component</code>.
   * 
   * @param component
   * @return 
   */
  public int getGuardBits(int component) {
    int guardBits = -1;
    if ((qccParametersList != null) && qccParametersList.containsKey(component)) {
      guardBits = qccParametersList.get(component).guardBits;
    } else if (qcdParameters != null) {
      guardBits = qcdParameters.guardBits;
    } else {
      assert (true);
    }
    return guardBits;
  }

  // get COM methods
  /**
   * Returns the {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.COMParameters#predictiveModel} attribute.
   *
   * @return the {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.COMParameters#predictiveModel} attribute.
   */
  public HashMap<Long, Float> getPredictiveModel() {
    return comParameters.predictiveModel;
  }

  /**
   * Returns the bounds (offset and size) of the image area on the reference
   * grid.
   *
   * @return a {@link CADI.Common.Util.CADIRectangle} with the bounds of the
   * 					image area.
   */
  public CADIRectangle getImageArea() {
    return new CADIRectangle(sizParameters.XOsize, sizParameters.YOsize,
            sizParameters.xSize - sizParameters.XOsize,
            sizParameters.ySize - sizParameters.YOsize);
  }

  /**
   * Returns the number of tiles wide (X direction).
   *
   * @return number of tiles wide.
   */
  public int getNumTilesWide() {
    return (int) Math.ceil(1.0 * (sizParameters.xSize - sizParameters.XTOsize)
            / sizParameters.XTsize);
  }

  /**
   * Returns the number of tiles high (Y direction).
   *
   * @return number of tiles high.
   */
  public int getNumTilesHigh() {
    return (int) Math.ceil(1.0 * (sizParameters.ySize - sizParameters.YTOsize)
            / sizParameters.YTsize);
  }

  /**
   * Returns the maximum number of tiles.
   *
   * @return the maximum number of tiles.
   */
  public int getNumTiles() {
    return (getNumTilesWide() * getNumTilesHigh());
  }

  /**
   * Returns the maximum number of resolutions levels for all tile-components.
   *
   * @return the maximum number of resolution levels for all tile-components.
   */
  public int getMaxResolutionLevels() {

    if (maxRLevels < 0) {
      // Value must be computed
      JPEG2KTile tileObj = null;
      for (int tileIndex = getNumTiles() - 1; tileIndex >= 0; tileIndex--) {
        tileObj = getTile(tileIndex);
        for (int component = 0; component < sizParameters.zSize; component++) {
          if (maxRLevels < tileObj.getComponent(component).getWTLevels()) {
            maxRLevels = tileObj.getComponent(component).getWTLevels();
          }
        }
      }
    }

    return maxRLevels;
  }

  /**
   * Determines the frame size when <code>discardLevels</code> levels of the DWT
   * are discarded.
   *
   * @param discardLevels the number of DWT levels to discard.
   * @param frameSize an object where the result will be recorded.
   */
  public void determineFrameSize(int discardLevels, CADIDimension frameSize) {
    JPEG2000Util.determineFrameSize(sizParameters.xSize, sizParameters.ySize,
            sizParameters.XOsize, sizParameters.YOsize,
            discardLevels, frameSize);
  }

  /**
   * Determines the frame size when <code>discardLevels</code> levels of the DWT
   * are discarded.
   *
   * @param discardLevels the number of DWT levels to discard.
   * @return the size of the frame
   */
  public CADIDimension determineFrameSize(int discardLevels) {
    CADIDimension frameSize = new CADIDimension();
    JPEG2000Util.determineFrameSize(sizParameters.xSize, sizParameters.ySize,
            sizParameters.XOsize, sizParameters.YOsize,
            discardLevels, frameSize);
    return frameSize;
  }

  /**
   * Determines the number of discard levels to be applied for achieving the
   * best fit of the <code>fsiz</code> to an available frame size.
   *
   * @param fsiz definition in
   * 							{@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}.
   * @param roundDirection definition in
   * 					{@link CADI.Common.Network.JPIP.ViewWindowField#roundDirection}.
   * @return the number of DWT levels to be discarded.
   */
  public int determineNumberOfDiscardLevels(int[] fsiz, int roundDirection) {
    // TODO: change method name to determineDiscardLevels
    return JPEG2000Util.determineNumberOfDiscardLevels(
            sizParameters.xSize, sizParameters.ySize,
            sizParameters.XOsize, sizParameters.YOsize,
            fsiz, roundDirection, getMaxResolutionLevels());
  }

  /**
   * Determines which tiles are relevant to the Window of Interest,
   * <code>viewWindow</code>.
   *
   * @param viewWindow definition in
   * 									{@link CADI.Common.Network.JPIP.ViewWindowField}
   * @param discardLevels the number of DWT levels to discard.
   * @return a list with the tile indexes.
   */
  public ArrayList<Integer> calculateRelevantTiles(ViewWindowField viewWindow,
                                                   int discardLevels) {

    ArrayList<Integer> relevantTileIndexes = new ArrayList<Integer>();

    int numXTiles = getNumTilesWide();

    int[] roff = new int[2];
    int[] rsiz = new int[2];
    JPEG2000Util.mapRegionToHighResolutionGrid(sizParameters.xSize,
            sizParameters.ySize,
            roff, rsiz,
            discardLevels);

    //
    roff[0] -= sizParameters.XTOsize;
    roff[1] -= sizParameters.YTOsize;


    int upperLeftXTile = (int) Math.floor(roff[0] / (double) sizParameters.XTsize);
    int upperLeftYTile = (int) Math.floor(roff[1] / (double) sizParameters.YTsize);
    int lowerRightXTile = (int) Math.ceil((roff[0] + rsiz[0]) / (double) sizParameters.XTsize);
    int lowerRightYTile = (int) Math.ceil((roff[1] + rsiz[1]) / (double) sizParameters.YTsize);


    for (int xTile = upperLeftXTile; xTile <= lowerRightXTile; xTile++) {
      for (int yTile = upperLeftYTile; yTile <= lowerRightYTile; yTile++) {
        relevantTileIndexes.add(yTile * numXTiles + xTile);
      }
    }

    return relevantTileIndexes;
  }

  /**
   * This method calculates the offset and width of the support region taken
   * into account the DWT applied. The input region is received in the <code>
   * supportRegion</code> parameter and the result is saved in the same
   * parameter. The <code>discardLevels</code> indicates the number of discarded
   * DWT levels to achieve the resolution level over the <code>supportRegion
   * </code> has been defined.sadf
   *
   * @param tile the tile index.
   * @param component the component.
   * @param rLevel the resolution level within the tile-component.
   * @param supportRegion it is an input and output parameters. As input, the
   * 				method receives the region of which to calculate the support region.
   * 				As output, the result is saved in it.
   * @param discardLevels number of DWT levels to discard.
   */
  public void calculateSupportRegion(int tile, int component, int rLevel,
                                     CADIRectangle supportRegion,
                                     int discardLevels) {

    int maxRLevelsComponent = tiles.get(tile).components.get(component).getWTLevels();
    int WTType = tiles.get(tile).components.get(component).getWTType();

    assert (rLevel <= maxRLevelsComponent - discardLevels);

    CADIPoint upperLeftCorner = new CADIPoint(2 * supportRegion.x, 2 * supportRegion.y);
    CADIPoint lowerRightCorner = new CADIPoint(2 * (supportRegion.x + supportRegion.width),
            2 * (supportRegion.y + supportRegion.height));

    for (int r = maxRLevelsComponent - discardLevels; r >= rLevel; r--) {
      upperLeftCorner.x = (int) Math.ceil(upperLeftCorner.x / 2.0D);
      upperLeftCorner.y = (int) Math.ceil(upperLeftCorner.y / 2.0D);
      lowerRightCorner.x = (int) Math.floor(lowerRightCorner.x / 2.0D);
      lowerRightCorner.y = (int) Math.floor(lowerRightCorner.y / 2.0D);

      if (r > 0) {
        upperLeftCorner.x -= getExtensionLength(WTType, -1, upperLeftCorner.x);
        upperLeftCorner.y -= getExtensionLength(WTType, -1, upperLeftCorner.y);
        lowerRightCorner.x += getExtensionLength(WTType, 1, lowerRightCorner.x);
        lowerRightCorner.y += getExtensionLength(WTType, 1, lowerRightCorner.y);

        CADIDimension rLevelDims = tiles.get(tile).components.get(component).resolutionLevels.get(r).getSize();
        if (upperLeftCorner.x < 0) {
          upperLeftCorner.x = 0;
        }
        if (upperLeftCorner.y < 0) {
          upperLeftCorner.y = 0;
        }
        if (lowerRightCorner.x > rLevelDims.width) {
          lowerRightCorner.x = rLevelDims.width;
        }
        if (lowerRightCorner.y > rLevelDims.height) {
          lowerRightCorner.y = rLevelDims.height;
        }
      }
    }

    supportRegion.x = upperLeftCorner.x;
    supportRegion.y = upperLeftCorner.y;
    supportRegion.width = lowerRightCorner.x - upperLeftCorner.x;
    supportRegion.height = lowerRightCorner.y - upperLeftCorner.y;
  }

  /**
   * Maps an image region (frame size, region offset and region size) to the
   * suitable codestream frame size (image resolution) and image region.
   * <p>
   * The <code>fsiz</code>, <code>roff</code>, and <code>rsiz</code> are all
   * of them input and output parameters.
   * <p>
   * Further information, see see ISO/IEC 15444-9 section C.4.1
   *
   * @param fsiz definition in
   * 							{@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}.
   * @param roff definition in
   * 							{@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in
   * 							{@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param discardLevels is the number of discarded DWT levels.
   */
  public void mapRegionToSuitableResolutionGrid(int[] fsiz, int[] roff,
                                                int[] rsiz, int discardLevels) {
    JPEG2000Util.mapRegionToSuitableResolutionGrid(sizParameters.xSize, sizParameters.ySize,
            sizParameters.XOsize, sizParameters.YOsize,
            fsiz, roff, rsiz, discardLevels);
  }

  /**
   * Calculates which are the relevant precincts which belong to the WOI
   * (fsiz, roff, rsiz).
   * <p>
   * Further information, see ISO/IEC 15444-9 section K.4.1
   *
   * @param actualViewWindow
   * @param discardLevels
   *
   * @return a array list with the unique precinct number which belongs to the WOI.
   */
  public ArrayList<Long> findRelevantPrecinctsTRCP(ViewWindowField actualViewWindow,
                                                   int discardLevels) {

    return RelevantPrecinctsFinder.TRCPOrder(this,
            actualViewWindow,
            discardLevels);
  }

  /**
   * Determines which are the relevant precincts to the <code>viewWindow</code>
   * when <code>discardLevels</code> DWT levels are discarded.
   * <p>
   * The returned list of precincts is sorted out in the fashion tile-resolution
   * level-component-precinct.
   * <p>
   * Further information, see ISO/IEC 15444-9 section K.4.1
   *
   * @param viewWindow definition in
   * 					{@link CADI.Common.Network.JPIP.ViewWindowField.ViewWindowField}.
   * @param discardLevels is the number of discarded DWT levels.
   *
   * @return an array list the unique precinct identifier of the relevant
   * 								precincts.
   */
  public ArrayList<Long> findRelevantPrecincts(ViewWindowField viewWindow,
                                               int discardLevels) {
    return findRelevantPrecinctsTRCP(viewWindow, discardLevels);
  }

  /**
   * Determines which are the relevant precincts to the <code>viewWindow</code>
   * when <code>discardLevels</code> DWT levels are discarded.
   * <p>
   * The returned list of precincts is sorted out in the fashion tile-resolution
   * level-component-precinct.
   * <p>
   * Further information, see ISO/IEC 15444-9 section K.4.1
   *
   * @param viewWindow definition in
   * 					{@link CADI.Common.Network.JPIP.ViewWindowField.ViewWindowField}.
   *
   * @return an array list the unique precinct identifier of the relevant
   * 								precincts.
   */
  public ArrayList<Long> findRelevantPrecincts(ViewWindowField actualViewWindow) {
    int discardLevels = determineNumberOfDiscardLevels(actualViewWindow.fsiz,
            actualViewWindow.roundDirection);
    return findRelevantPrecinctsTRCP(actualViewWindow, discardLevels);
  }

  // METHODS TO BE DEPRECATED
  /**
   * Returns the main header parameters.
   * <p>
   * A copy of the parameters is performed in order to avoid that internal
   * attribute to be modified.
   *
   * @return main header parameters.
   */
  public JPCParameters getJPCParameters() {
    JPCParameters jpcParams = new JPCParameters();

    jpcParams.sizParameters = new SIZParameters(sizParameters);
    jpcParams.codParameters = new CODParameters(codParameters);
    if (cocParametersList != null) {
      jpcParams.cocParametersList = new HashMap<Integer, COCParameters>();
      for (Map.Entry<Integer, COCParameters> entry : cocParametersList.entrySet()) {
        jpcParams.cocParametersList.put(entry.getKey(), entry.getValue());
      }
    }
    jpcParams.qcdParameters = new QCDParameters(qcdParameters);
    if (qccParametersList != null) {
      jpcParams.qccParametersList = new HashMap<Integer, QCCParameters>();
      for (Map.Entry<Integer, QCCParameters> entry : qccParametersList.entrySet()) {
        jpcParams.qccParametersList.put(entry.getKey(), entry.getValue());
      }
    }
    jpcParams.cbdParameters = new CBDParameters(cbdParameters);
    if (mctParametersList != null) {
      jpcParams.mctParametersList = new HashMap<Integer, MCTParameters>();
      for (Map.Entry<Integer, MCTParameters> entry : mctParametersList.entrySet()) {
        jpcParams.mctParametersList.put(entry.getKey(), entry.getValue());
      }
    }
    if (mccParametersList != null) {
      jpcParams.mccParametersList = new HashMap<Integer, MCCParameters>();
      for (Map.Entry<Integer, MCCParameters> entry : mccParametersList.entrySet()) {
        jpcParams.mccParametersList.put(entry.getKey(), entry.getValue());
      }
    }
    jpcParams.mcoParameters = new MCOParameters(mcoParameters);
    jpcParams.comParameters = new COMParameters(comParameters);
    if (jpkParameters != null) {
      jpcParams.jpkParameters = new JPKParameters(jpkParameters);
    }

    return jpcParams;
  }

  public SIZParameters getSIZParameters() {
    return sizParameters;
  }

  public CODParameters getCODParameters() {
    return codParameters;
  }

  public QCDParameters getQCDParameters() {
    return qcdParameters;
  }

  public CBDParameters getCBDParameters() {
    return cbdParameters;
  }

  public HashMap<Integer, MCTParameters> getMCTParameters() {
    return mctParametersList;
  }

  public HashMap<Integer, MCCParameters> getMCCParameters() {
    return mccParametersList;
  }

  public MCOParameters getMCOParameters() {
    return mcoParameters;
  }

  public COMParameters getCOMParameters() {
    return comParameters;
  }

  // END DEPRECATED METHODS
  /**
   *
   * @return
   */
  public JPKParameters getJPKParameters() {
    return jpkParameters;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";
    str += getClass().getName() + " [";

    str += "Identifier=" + identifier;
    str += sizParameters.toString();
    str += codParameters.toString();
    for (Entry<Integer, COCParameters> param : cocParametersList.entrySet()) {
      str += param.getValue().toString();
    }
    str += qcdParameters.toString();
    for (Entry<Integer, QCCParameters> param : qccParametersList.entrySet()) {
      str += param.getValue().toString();
    }
    str += cbdParameters.toString();
    for (Entry<Integer, MCTParameters> param : mctParametersList.entrySet()) {
      str += param.getValue().toString();
    }
    for (Entry<Integer, MCCParameters> param : mccParametersList.entrySet()) {
      str += param.getValue().toString();
    }
    str += mcoParameters.toString();
    str += comParameters.toString();
    str += jpkParameters.toString();


    for (Entry<Integer, JPEG2KTile> tile : tiles.entrySet()) {
      str += tile.getValue().toString();
    }

    str += "]";

    return str;
  }

  /**
   * Prints this Server JPEG2K Codestream out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- JPEG2K Codestream --");

    listParameters(out);

    for (Entry<Integer, JPEG2KTile> tile : tiles.entrySet()) {
      tile.getValue().list(out);
    }

    out.flush();
  }

  /**
   * This method is similar to {@link #list(PrintStream)} but it only output
   * the parameters.
   *
   * @param out an output stream.
   */
  public void listParameters(PrintStream out) {

    out.println("Identifier=" + identifier);
    sizParameters.list(out);
    codParameters.list(out);
    for (Entry<Integer, COCParameters> param : cocParametersList.entrySet()) {
      out.println("COC component: " + param.getKey());
      param.getValue().list(out);
    }
    qcdParameters.list(out);
    for (Entry<Integer, QCCParameters> param : qccParametersList.entrySet()) {
      out.println("QCC component: " + param.getKey());
      param.getValue().list(out);
    }
    cbdParameters.list(out);
    for (Entry<Integer, MCTParameters> param : mctParametersList.entrySet()) {
      out.println("MCT stage: " + param.getKey());
      param.getValue().list(out);
    }
    for (Entry<Integer, MCCParameters> param : mccParametersList.entrySet()) {
      out.println("MCC stage: " + param.getKey());
      param.getValue().list(out);
    }
    mcoParameters.list(out);
    comParameters.list(out);
    if (jpkParameters != null) {
      jpkParameters.list(out);
    }

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   *
   * @param WTType definition in
   * {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#WTTypes}.
   * @param direction -1 means to the left, and 1 to the right
   * @param sampleIndex
   *
   * @return
   */
  private static int getExtensionLength(int WTType, int direction, int sampleIndex) {

    //if (true)
    //  return 0; // FIXME: fixed only for debugging purposes

    if (WTType == 1) {
      // Reversible 5/3
      if (direction == -1) {
        return ((sampleIndex % 2) == 0) ? 1 : 2;
      } else {
        return ((sampleIndex % 2) == 0) ? 2 : 1;
      }

    } else if (WTType == 2) {
      // Irreversible 9/7
      if (direction == -1) {
        return ((sampleIndex % 2) == 0) ? 3 : 4;
      } else {
        return ((sampleIndex % 2) == 0) ? 4 : 3;
      }

    } else {
      assert (true);
    }

    return 0;
  }
}
