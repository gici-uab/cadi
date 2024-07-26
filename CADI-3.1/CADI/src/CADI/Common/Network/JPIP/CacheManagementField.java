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
package CADI.Common.Network.JPIP;

import java.io.PrintStream;
import java.util.ArrayList;

import CADI.Common.Cache.ModelElement;

/**
 * This class is used to store the cache management fields.
 * <p>
 * Further information, see ISO/IEC 15444-9 section C.8
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2010/01/24
 */
public class CacheManagementField {

  /**
   *
   */
  public ArrayList<ModelElement> model = null;

  /**
   *
   */
  public String tpmodel = null;

  /**
   *
   */
  public String need = null;

  /**
   *
   */
  public String tpneed = null;

  /**
   *
   */
  public int[][] mset = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public CacheManagementField() {
    model = new ArrayList<ModelElement>();
  }

  /**
   * Sets the attributes to its initial values.
   */
  public void reset() {
    if (model != null) model.clear();
    tpmodel = "";
    need = "";
    tpneed = "";
    mset = null;
  }

  /**
   *
   * @param cacheManagement
   * @param classIdentifier
   * @param inClassIdentifier
   * @return
   */
  private ModelElement getModelElement(int classIdentifier, long inClassIdentifier) {
    assert (classIdentifier >= 0);
    assert (inClassIdentifier >= 0);
    for (int index = model.size() - 1; index >= 0; index--) {
      if ((model.get(index).explicitForm.classIdentifier == classIdentifier)
              && (model.get(index).explicitForm.inClassIdentifier == inClassIdentifier)) {
        return model.get(index);
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";

    if (model != null) {
      for (ModelElement descriptor : model) {
        str += descriptor.toString();
      }
    }

    str += "]";
    return str;
  }

  /**
   * Prints this Cache management fields out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Cache management fields --");
    if (model != null) {
      for (ModelElement descriptor : model) {
        descriptor.list(System.out);
      }
    }

    out.flush();
  }

}
