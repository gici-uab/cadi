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
public class HeaderIndexTable {

	/**
	 * 
	 */
	public long tlen = -1;
	
	/**
	 * 
	 */
	public ArrayList<Integer> m = null;
	
	/**
	 * 
	 */
	public ArrayList<Integer> nr = null;
	
	/**
	 * 
	 */
	public ArrayList<Integer> off = null;
	
	/**
	 * 
	 */
	public ArrayList<Integer> len = null;
  
  /**
	 * Is the file pointer to the first byte of the main header.
	 */
	public long mainHeaderInitialPos = -1;
	
	/**
	 * Is the length of the main header.
	 */
	public int mainHeaderLength = -1;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public HeaderIndexTable() {
		m = new ArrayList<Integer>();
		nr = new ArrayList<Integer>();
		off = new ArrayList<Integer>();
		len = new ArrayList<Integer>();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param tlen
	 * @param m
	 * @param nr
	 * @param off
	 * @param len
	 */
	public HeaderIndexTable(long tlen, ArrayList<Integer> m, ArrayList<Integer> nr, ArrayList<Integer> off, ArrayList<Integer> len) {
		if (m == null) throw new NullPointerException();
		if (nr == null) throw new NullPointerException();
		if (off == null) throw new NullPointerException();
		if (len == null) throw new NullPointerException();
		
		this.tlen = tlen;
		this.m = m;
		this.nr = nr;
		this.off = off;
		this.len = len;
	}
	
	/**
	 * Sets the attributes to their initial values.
	 */
	public void reset() {
		tlen = -1;
		m.clear();
		nr.clear();
		off.clear();
		len.clear();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";
		
		str = getClass().getName() + " [";
		str += "tlen="+tlen;
		str += ", m";
		for (Integer value : m) str += " "+value;
		str += ", nr=";
		for (Integer value : nr) str += " "+value;
		str += ", off=";
		for (Integer value : off) str += " "+value;
		str += ", len=";
		for (Integer value : len) str += " "+value;
		str += "]";
		
		return str;
	}
	
	/**
	 * Prints the Header Index Table data out to the specified output stream.
	 * This method is useful for debugging.
	 *
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Header Index Table --");
		
		out.println("tlen: "+tlen);
		out.print("m: ");
		for (Integer value : m) out.print(" "+value);
		out.println();
		out.print("nr: ");
		for (Integer value : nr) out.print(" "+value);
		out.println();
		out.print("off: ");
		for (Integer value : off) out.print(" "+value);
		out.println();
		out.print("len: ");
		for (Integer value : len) out.print(" "+value);
		out.println();
	
		out.flush();
	}
	
}
