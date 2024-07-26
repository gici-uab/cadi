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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2009/09/12
 */
public class PacketHeaderIndexTable {

	/**
	 * 
	 */
	public Manifest manf = null;
	
	/**
	 * 
	 */
	public HashMap<Long, FragmentArrayIndex> faix = null;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public PacketHeaderIndexTable() {
		faix = new HashMap<Long, FragmentArrayIndex>();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param bh definition in {@link #bh}.
	 */
	public PacketHeaderIndexTable(Manifest manf, HashMap<Long, FragmentArrayIndex> faix) {
		if (manf == null) throw new NullPointerException();
		if (faix == null) throw new NullPointerException();
		
		this.manf = manf;
		this.faix = faix;
	}
	
  public int getOffset(long inClassIdentifier, int layer) {
    return faix.get(inClassIdentifier).getIntOffset(0, layer);
  }
  
  public int getLength(long inClassIdentifier, int layer) {
    return faix.get(inClassIdentifier).getIntLength(0, layer);
  }
  
	/**
	 * Sets the attributes to their initial values.
	 */
	public void reset() {
		manf.reset();
		for (Map.Entry<Long, FragmentArrayIndex> entry : faix.entrySet()) {
      entry.getValue().reset();
    }
		faix.clear();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";
		
		str = getClass().getName() + " [";
		str += manf.toString();
		str += ", mhix=";
		for (Map.Entry<Long, FragmentArrayIndex> entry : faix.entrySet()) {
      str += ", precinct=" + entry.getKey() + " values=" + entry.getValue().toString();
    }
		str += "]";
		
		return str;
	}
	
	/**
	 * Prints the Packet Header Index Table data out to the specified output stream.
	 * This method is useful for debugging.
	 *
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Packet Header Index Table --");
		
		//manf.list(out);
		out.print("mhix: ");
		for (Map.Entry<Long, FragmentArrayIndex> entry : faix.entrySet()) {
      out.println("precinct: " + entry.getKey());
      out.println("\tvalues: ");
      entry.getValue().list(out);
    }
		
		out.flush();
	}

}