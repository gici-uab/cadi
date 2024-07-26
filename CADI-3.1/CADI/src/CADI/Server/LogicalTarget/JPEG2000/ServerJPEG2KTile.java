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

import CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream;
import java.io.PrintStream;

import CADI.Common.LogicalTarget.JPEG2000.JPEG2KTile;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.COCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters;
import CADI.Common.Util.CADIDimension;
import java.util.HashMap;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.3 2012/01/16
 */
public class ServerJPEG2KTile extends JPEG2KTile {

  /**
   * Coding style default (COD) for this tile.
   * <p>
   * If this object is
   * <code>null</code> the parameters to be applied to this
   * tile are the default parameters for the codestream,
   * {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream#codParameters}.
   */
  protected CODParameters orgCODParams = null;

  /**
   * Coding style of component (COC) for this tile.
   * <p>
   * If this object is <code>null</code> the parameters to be applied to this
   * tile are the default parameters for the codestream,
   * {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream#cocParametersList}.
   */
  protected HashMap<Integer, COCParameters> orgCOCParamsList = null;

  // ============================= public methods ==============================
  /**
	 * Constructor.
	 */
  public ServerJPEG2KTile(ServerJPEG2KCodestream parent, int index) {
    super(parent, index);
  }

  @Override
  public ServerJPEG2KCodestream getParent() {
    return (ServerJPEG2KCodestream)parent;
  }

  @Override
  public void createComponent(int component) {
    if (!components.containsKey(component)) {
      components.put(component, new ServerJPEG2KComponent(this, component));
    }
  }

  @Override
  public ServerJPEG2KComponent getComponent(int component) {
    return (ServerJPEG2KComponent)components.get(component);
  }

  @Override
  public void removeComponent(int component) {
    if (components.containsKey(component)) components.remove(component);
  }

  /**
   *
   * @param component
   * @return
   */
  public int getOriginalBlockHeight(int component) {

    if ((orgCOCParamsList != null) && orgCOCParamsList.containsKey(component))
      return 1 << orgCOCParamsList.get(component).blockHeight;
    else if (orgCODParams != null)
      return 1 << orgCODParams.blockHeight;
    else
      return getParent().getOriginalBlockHeight(component);
  }

  /**
   *
   * @param component
   * @return
   */
  public int getOriginalBlockWidth(int component) {

    if ((orgCOCParamsList != null) && orgCOCParamsList.containsKey(component))
      return 1 << orgCOCParamsList.get(component).blockWidth;
    else if (orgCODParams != null)
      return 1 << orgCODParams.blockWidth;
    else
      return getParent().getOriginalBlockWidth(component);
  }

  /**
   *
   * @param component
   * @param rLevel
   * @return
   */
  public int getOriginalPrecinctHeights(int component, int rLevel) {
    int height = -1;
    if ((orgCOCParamsList != null) && orgCOCParamsList.containsKey(component)) {
      height = 1 << orgCOCParamsList.get(component).precinctHeights[rLevel];
    } else if (orgCODParams != null) {
      height = 1 << orgCODParams.precinctHeights[rLevel];
    } else {
      height = getParent().getOriginalPrecinctHeights(component, rLevel);
    }

    CADIDimension rLevelSize =
            components.get(component).getResolutionLevel(rLevel).getSize();

    return (height <= rLevelSize.height) ? height : rLevelSize.height;
  }

  /**
   *
   * @param component
   * @param rLevel
   * @return
   */
  public int getOriginalPrecinctWidths(int component, int rLevel) {
    int width = -1;
    if ((orgCOCParamsList != null) && orgCOCParamsList.containsKey(component))
      width = 1 << orgCOCParamsList.get(component).precinctWidths[rLevel];
    else if (orgCODParams != null)
      width = 1 << orgCODParams.precinctWidths[rLevel];
    else
      width = getParent().getOriginalPrecinctWidths(component, rLevel);

    CADIDimension rLevelSize =
            components.get(component).getResolutionLevel(rLevel).getSize();

    return (width <= rLevelSize.width) ? width : rLevelSize.width;
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
	 * Prints this Server JPEG2K Tile out to the specified output stream. This
	 * method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
  @Override
  public void list(PrintStream out) {

    out.println("-- Server JPEG2K Tile --");

    super.list(out);

  }
  // ============================ private methods ==============================
}
