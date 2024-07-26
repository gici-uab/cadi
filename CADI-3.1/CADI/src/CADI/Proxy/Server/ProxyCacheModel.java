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
package CADI.Proxy.Server;

import java.io.PrintStream;
import java.util.ArrayList;

import CADI.Common.Cache.ModelElement;
import CADI.Proxy.LogicalTarget.JPEG2000.ProxyJPEG2KCodestream;
import CADI.Common.Cache.CacheModel;
import CADI.Common.Network.JPIP.ClassIdentifiers;
import CADI.Proxy.Client.ProxyCacheManagement;

/**
 * This class implements a proxy's cache model.
 * <p>
 * It extends the basic cache model offering an extended interface with new
 * methods. 
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2011/01/29
 */
public class ProxyCacheModel extends CacheModel {

  
  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param codestream
   */
  public ProxyCacheModel(ProxyJPEG2KCodestream codestream) {
    super(codestream);
  }

  /**
   *
   * @param model
   */
  @Override
  public void update(ArrayList<ModelElement> model) {
    for (ModelElement element : model) {
      super.update(element);
    }
  }

  /**
   *
   * @param model
   */
  public void update(ArrayList<ModelElement> model, ProxyCacheManagement cache) {
    for (ModelElement element : model) {
      if (element.explicitForm != null) {
        if (element.explicitForm.classIdentifier == ClassIdentifiers.MAIN_HEADER) {
          element.explicitForm.inClassIdentifier = 0;
          element.explicitForm.numberOfBytes = (int)cache.getDatabinLength(ClassIdentifiers.MAIN_HEADER, 0);
        }
      }
      update(element);
    }
  }

  /**
   * Resets the variables to its initials values.
   */
  @Override
  public void reset() {
    super.reset();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";
    str += super.toString();

    str += "]";
    return str;
  }

  /**
   * Prints this Server Cache Model and Preferences fields out to the
   * specified output stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Proxy Cache Model --");

    super.list(out);

    out.flush();
  }
  // ============================ private methods ==============================
}
