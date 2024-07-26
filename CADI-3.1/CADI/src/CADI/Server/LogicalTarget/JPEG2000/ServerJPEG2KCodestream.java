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

import java.io.PrintStream;

import CADI.Common.LogicalTarget.JPEG2000.JPCParameters;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.*;
import CADI.Common.Util.CADIDimension;
import java.util.HashMap;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/01/16
 */
public class ServerJPEG2KCodestream extends JPEG2KCodestream {

  /**
   * Image and tile size parameters (SIZ) of the original image.
   */
  protected SIZParameters orgSIZParams = null;

  /**
   * Coding style default (COD) of the original image.
   */
  protected CODParameters orgCODParams = null;

  /**
   * Coding style of component (COC) of the original image.
   */
  protected HashMap<Integer, COCParameters> orgCOCParamsList = null;

  /**
   * Quantization default (QCD) of the original image.
   */
  protected QCDParameters orgQCDParams = null;

  /**
   * Quantization component (QCC) of the original image.
   */
  protected HashMap<Integer, QCCParameters> orgQCCParamsList = null;

  // Region of interest (RGN)
  /**
   * Component bit depth (CBD) of the original image.
   */
  protected CBDParameters orgCBDParams = null;

  /**
   * Multiple component transformation (MCT) of the original image.
   * <p>
   * The integer of the hash key stand for the stage.
   */
  protected HashMap<Integer, MCTParameters> orgMCTParamsList = null;

  /**
   * Multiple component transform collection (MCC) of the original image.
   * <p>
   * The integer of the hash key stand for the stage.
   */
  protected HashMap<Integer, MCCParameters> orgMCCParamsList = null;

  /**
   * Multiple component transform ordering (MCO) of the original image.
   */
  protected MCOParameters orgMCOParams = null;

  /**
   * Comments (COM) of the original image.
   */
  protected COMParameters orgCOMParams = null;

  /**
   * Wrapped used to store non-compliant parameters of the original image.
   */
  protected JPKParameters orgJPKParams = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   * 
   * @param identifier definition in {@link #identifier}.
   * @param jpcParameters an object with the codestream parameters. See
   * {@link  CADI.Common.LogicalTarget.JPEG2000.JPCParameters}.
   */
  public ServerJPEG2KCodestream(int identifier, JPCParameters jpcParameters) {
    super(identifier, jpcParameters);
  }

  /**
   * Cosntructor.
   * 
   * @param identifier definition in {@link #identifier}.
   * @param jpcParameters an object with the codestream parameters. See
   * {@link  CADI.Common.LogicalTarget.JPEG2000.JPCParameters}.
   * @param orgJPCParameters an object with the original codestream parameters. See
   * {@link  CADI.Common.LogicalTarget.JPEG2000.JPCParameters}.
   */
  public ServerJPEG2KCodestream(int identifier, JPCParameters jpcParameters,
          JPCParameters orgJPCParameters) {
    super(identifier, jpcParameters);

    orgSIZParams = orgJPCParameters.sizParameters;
    orgCODParams = orgJPCParameters.codParameters;
    orgCOCParamsList = orgJPCParameters.cocParametersList;
    orgQCDParams = orgJPCParameters.qcdParameters;
    orgQCCParamsList = orgJPCParameters.qccParametersList;
    orgCBDParams = orgJPCParameters.cbdParameters;
    orgMCTParamsList = orgJPCParameters.mctParametersList;
    orgMCCParamsList = orgJPCParameters.mccParametersList;
    orgMCOParams = orgJPCParameters.mcoParameters;
    orgCOMParams = orgJPCParameters.comParameters;
    orgJPKParams = orgJPCParameters.jpkParameters;
  }

  /**
   * 
   * @param index
   * 
   * @throws IllegalAccessException
   */
  @Override
  public void createTile(int index) {
    if (!tiles.containsKey(index)) {
      tiles.put(index, new ServerJPEG2KTile(this, index));
    }

    
  }

  @Override
  public ServerJPEG2KTile getTile(int index) {
    return (ServerJPEG2KTile) tiles.get(index);
  }

  @Override
  public ServerJPEG2KPrecinct getPrecinct(long inClassIdentifier) {
    int[] TCRP = findTCRP(inClassIdentifier);
    return (ServerJPEG2KPrecinct) tiles.get(TCRP[0]).getComponent(TCRP[1]).getResolutionLevel(TCRP[2]).getPrecinct(TCRP[3]);
  }

  public void transcode() {

    int numTiles = getNumTiles();
    int numComponents = 0;
    ServerJPEG2KTile tileObj = null;
    ServerJPEG2KComponent componentObj = null;
    ServerJPEG2KResolutionLevel rLevelObj = null;

    for (int t = 0; t < numTiles; t++) {
      tileObj = getTile(t);
      numComponents = getZSize();
      for (int c = 0; c < numComponents; c++) {
        componentObj = tileObj.getComponent(c);
        int maxWTLevels = componentObj.getWTLevels();
        for (int r = 0; r <= maxWTLevels; r++) {
          rLevelObj = componentObj.getResolutionLevel(r);
          int numPrecincts = rLevelObj.getNumPrecincts();
          for (int p = 0; p < numPrecincts; p++) {
            rLevelObj.removePrecinct(p);
          }
        }
      }
    }


    CADIDimension frameSize = null;
    // COD Parameters
    orgCODParams = new CODParameters(codParameters);
    for (int rLevel = 0; rLevel < codParameters.precinctWidths.length; rLevel++) {
      int discardLevels = orgCODParams.WTLevels - rLevel;
      frameSize = determineFrameSize(discardLevels);
      codParameters.precinctWidths[rLevel] = int2log_2(frameSize.width);
      codParameters.precinctHeights[rLevel] = int2log_2(frameSize.height);
      if (codParameters.precinctWidths[rLevel] >  codParameters.blockWidth)
        codParameters.precinctWidths[rLevel] = codParameters.blockWidth;
      if (codParameters.precinctHeights[rLevel] > codParameters.blockHeight)
        codParameters.precinctHeights[rLevel] = codParameters.blockHeight;
    }
    
  }

  /**
   * Returns the block height.
   * <p>
   * This method will be deprecated in the next version. Use the
   * {@link #getBlockHeight(int)} method.
   *
   * @return block height.
   */
  @Deprecated
  public int getOriginalBlockHeight() {
    return (orgCODParams != null) ? (1 << orgCODParams.blockHeight) : getBlockHeight();
  }

  /**
   * Returns the block width.
   * <p>
   * This method will be deprecated in the next version. Use the
   * {@link #getBlockWidth(int)} method.
   *
   * @return block width.
   */
  @Deprecated
  public int getOriginalBlockWidth() {
    return (orgCODParams != null) ? (1 << orgCODParams.blockWidth) : getBlockWidth();
  }

  public int getOriginalBlockHeight(int component) {
    if ((orgCOCParamsList != null) && orgCOCParamsList.containsKey(component)) {
      return 1 << orgCOCParamsList.get(component).blockHeight;
    } else if (orgCODParams != null) {
      return 1 << orgCODParams.blockHeight;
    } else {
      return getBlockHeight(component);
    }
  }

  public int getOriginalBlockWidth(int component) {
    if ((orgCOCParamsList != null) && orgCOCParamsList.containsKey(component)) {
      return 1 << orgCOCParamsList.get(component).blockWidth;
    } else if (orgCODParams != null) {
      return 1 << orgCODParams.blockWidth;
    } else {
      return getBlockWidth(component);
    }
  }

  public int getOriginalPrecinctHeights(int component, int rLevel) {
    int height = -1;
    if ((orgCOCParamsList != null) && orgCOCParamsList.containsKey(component)) {
      height = 1 << orgCOCParamsList.get(component).precinctHeights[rLevel];
    } else if (orgCODParams != null) {
      height = 1 << orgCODParams.precinctHeights[rLevel];
    } else {
      height = getPrecinctHeights(component, rLevel);
    }
    return height;
  }

  public int getOriginalPrecinctWidths(int component, int rLevel) {
    int width = -1;
    if ((orgCOCParamsList != null) && orgCOCParamsList.containsKey(component)) {
      width = 1 << orgCOCParamsList.get(component).precinctWidths[rLevel];
    } else if (orgCODParams != null) {
      width = 1 << orgCODParams.precinctWidths[rLevel];
    } else {
      width = getPrecinctWidths(component, rLevel);
    }
    return width;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";
    str += getClass().getName() + " [";
    super.toString();


    str += "]";

    return str;
  }

  /**
   * Prints this Server JPEG2K Codestream out to the specified output stream.
   * This method is useful for debugging.
   * 
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Server JPEG2K Codestream --");
    super.list(out);
  }

  // ============================ private methods ==============================
  /**
   * 
   * @param value
   * @return 
   */
  static private int int2log_2(int value) {
    int res = 0;
    for (res = 0; (value >>> res) > 0; res++);
    res--;
    if ((1 << res) != value) {
      return -1;
    } else {
      return res;
    }
  }
}
