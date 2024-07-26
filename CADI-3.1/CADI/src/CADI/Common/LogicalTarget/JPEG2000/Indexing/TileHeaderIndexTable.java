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
import java.util.ArrayList;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2009/09/12
 */
public class TileHeaderIndexTable {

	/**
	 * 
	 */
	public Manifest manf = null;
	
	/**
	 * 
	 */
	public ArrayList<HeaderIndexTable> mhix = null;
  
  public long tileHeaderFilePointer = -1;

  public int tileHeaderLength = -1;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public TileHeaderIndexTable() {
		mhix = new ArrayList<HeaderIndexTable>();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param bh definition in {@link #bh}.
	 */
	public TileHeaderIndexTable(Manifest manf, ArrayList<HeaderIndexTable> mhix) {
		if (manf == null) throw new NullPointerException();
		if (mhix == null) throw new NullPointerException();
		
		this.manf = manf;
		this.mhix = mhix;
	}
	
	/**
	 * Sets the attributes to their initial values.
	 */
	public void reset() {
		manf.reset();
		for (HeaderIndexTable header : mhix) header.reset();
		mhix.clear();
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
		for (HeaderIndexTable header : mhix) str += header.toString();
		str += "]";
		
		return str;
	}
	
	/**
	 * Prints the Tile Header Index Table data out to the specified output stream.
	 * This method is useful for debugging.
	 *
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Tile Header Index Table --");
		
		manf.list(out);
		out.print("mhix: ");
		for (HeaderIndexTable header : mhix) header.list(out);
		
		out.flush();
	}

}
