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
package CADI.Common.LogicalTarget.JPEG2000.Indexing;

import java.io.PrintStream;
import java.util.Map;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 Jan 29, 2008
 */
public class FileIndex {

  public CodestreamIndex cidx = null;

  public Map<Integer, FileFinder> fileFinders = null;

  // ============================= public methods ==============================
  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    String str = getClass().getName() + " [";

    for (Map.Entry<Integer, FileFinder> entry : fileFinders.entrySet()) {
      str += ", key=" + entry.getKey() + " value=" + entry.getValue();
    }

    str += "]";

    return str;
  }

  /**
   * Prints this File Index out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- File Index --");

    for (Map.Entry<Integer, FileFinder> entry : fileFinders.entrySet()) {
      out.println("key: " + entry.getKey() + " value: ");
      entry.getValue().list(out);
    }

    out.flush();
  }
}
