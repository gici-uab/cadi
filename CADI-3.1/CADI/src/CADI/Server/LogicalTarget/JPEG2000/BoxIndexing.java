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

import CADI.Common.LogicalTarget.JPEG2000.File.JP2BoxTypes;
import CADI.Common.LogicalTarget.JPEG2000.Indexing.JPEG2KBox;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2012/05/29
 */
public class BoxIndexing extends JPEG2KBox {

  private int metadataID = -1;
  
  private long offset = -1;

  private long length = -1;

  private BoxIndexing parent = null;

  private Map<Integer, BoxIndexing> childs = null;
  
  // INTERNAL ATTRIBUTES
  
  /**
   * Attribute used to record the next metadata-bin ID to be assigned following
   * an incremental criterion.
   * <p>
   * See {@link #metadataID} attribute.
   */
  private static int nextID = 0;

  // ============================= public methods ==============================
  /**
   *
   * @param parent
   * @param type
   * @param offset
   */
  public BoxIndexing(BoxIndexing parent, int type, long offset, long length) {
    this(parent, type, offset, length, false);
  }

  /**
   *
   * @param parent
   * @param type
   * @param offset
   */
  public BoxIndexing(BoxIndexing parent, String type, long offset, long length) {
    this(parent, JP2BoxTypes.convertStringToInt(type), offset, length, false);
  }

  /**
   *
   * @param parent
   * @param type
   * @param offset
   * @param superbox
   */
  public BoxIndexing(BoxIndexing parent, int type, long offset, long length, boolean superbox) {
    this.parent = parent;
    this.type = type;
    this.offset = offset;
    this.length = length;
    this.metadataID = nextID++;
    if (superbox) {
      childs = new HashMap<Integer, BoxIndexing>();
    }
  }
  
  public int getID() {
    return metadataID;
  }

  public long getOffset() {
    return offset;
  }

  /**
   *
   * @return
   */
  public boolean isSuperbox() {
    return (childs == null ? false : true);
  }

  public boolean isRoot() {
    return parent == null;
  }

  public boolean isLeaf() {
    return childs == null;
  }

  /**
   *
   * @return
   */
  public BoxIndexing getParent() {
    return parent;
  }

  /**
   *
   * @param child
   */
  public void addChild(BoxIndexing child) {
    assert(!childs.containsKey(child.getID()));

    childs.put(child.getID(), child);
  }

  /**
   *
   * @param id
   *
   * @return
   */
  public BoxIndexing getChild(int id) {
    return childs.get(id);
  }

  public Set<Integer> getChildsKeys() {
    return childs.keySet();
  }

  public Collection<BoxIndexing> getChilds() {
    return childs.values();
  }

  public int getChildType(int id) {
    if (childs.containsKey(id)) {
      return childs.get(id).getType();
    }
    return -1;
  }

  public String getChildTypeAsString(int id) {
    if (childs.containsKey(id)) {
      return childs.get(id).getChildTypeAsString(id);
    }
    return null;
  }
  
  public int getNumChilds() {
    return (isSuperbox() ? childs.size() : 0);
  }
  

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = getClass().getName() + " [";
    
    str += "ID="+metadataID;
    
    if (isRoot()) {
      str += "Is root";
    }
    if (isLeaf()) {
      str += "Is leaf";
    }
    if (isSuperbox()) {
      str += "Is superbox";
    }
    str += "offset=" + offset;

    if (childs != null) {
      str += "childs={";
      for (Map.Entry<Integer, BoxIndexing> entry : childs.entrySet()) {
        str += "child id=" + entry.getKey();
        str += "value=" + entry.getValue().toString();
      }
      str += "};";
    }
    str += "]";

    return str;
  }

  /**
   * Prints this Box out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Box Indexing --");

    out.println("ID: "+metadataID);
    
    super.list(out);
    if (isRoot()) {
      out.println("Is root");
    }
    if (isLeaf()) {
      out.println("Is leaf");
    }
    if (isSuperbox()) {
      out.println("Is superbox");
    }
    out.println("offset: " + offset);
    out.println("length: " + length);

    if (childs != null) {
      out.println("childs: ");
      for (Map.Entry<Integer, BoxIndexing> entry : childs.entrySet()) {
        out.println("child id: " + entry.getKey());
        entry.getValue().list(out);
      }
    }

    out.flush();
  }
  // ============================ private methods ==============================
}
