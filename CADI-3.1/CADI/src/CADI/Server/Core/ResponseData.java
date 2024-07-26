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
package CADI.Server.Core;

import java.io.PrintStream;
import java.util.ArrayList;

import CADI.Common.Network.JPIP.JPIPMessageHeader;

/**
 * This class is used
 *	
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2009/12/05
 */
public class ResponseData {

	/**
	 * 
	 */
	public JPIPMessageHeader jpipMessageHeader = null;

	/**
	 * 
	 */
	public ArrayList<byte[]> chunks = null;
	
	/**
	 * 
	 */
	public ArrayList<Long> filePointers = null;
	
	/**
	 * 
	 */
	public ArrayList<Integer> lengths = null;
	
	// ============================= public methods ==============================
  /**
   * 
   */
	public ResponseData() {
		
	}
	
	/**
	 * 
	 * @param jpipMessageHeader
	 */
	public ResponseData(JPIPMessageHeader jpipMessageHeader) {
		this.jpipMessageHeader = jpipMessageHeader;
	}
	
	public ResponseData(ArrayList<byte[]> chunks) {
		this.chunks = chunks;
	}
	
	public ResponseData(JPIPMessageHeader jpipMessageHeader, ArrayList<byte[]> chunks) {
		this.jpipMessageHeader = jpipMessageHeader;
		this.chunks = chunks;
	}
	
	public ResponseData(ArrayList<Long> filePointers, ArrayList<Integer> lengths) {
		this.filePointers = filePointers;
		this.lengths = lengths;
	}
	
	public ResponseData(JPIPMessageHeader jpipMessageHeader, ArrayList<Long> filePointers, ArrayList<Integer> lengths) {
		this.jpipMessageHeader = jpipMessageHeader;
		this.filePointers = filePointers;
		this.lengths = lengths;
	}
	
	/**
	 * 
	 */
	public void reset() {
		jpipMessageHeader.reset();
		filePointers.clear();
		lengths.clear();
	}
	
	/**
	 * For debugging purpose.
	 */	
	public String toString() {
		String str = "";
		
		str += getClass().getName() + " [";
		str += jpipMessageHeader.toString();
		if (chunks !=  null) {
			int length = 0;
			for (byte[] d : chunks) length += d.length;
			str +=", chunks=<data not displayed>";
			str +=", length="+length;
		}

		if ((filePointers != null) && !filePointers.isEmpty()) {
      int length = filePointers.size();
			str += ", filePointers={";
      for (int i = 0; i < length-1; i++)
        str += filePointers.get(i)+", ";
      str += filePointers.get(length-1);
			str += "}";
		}
		if ((lengths != null) && !lengths.isEmpty()) {
      int length = lengths.size();
			str += ", lengths={";
      for (int i = 0; i < length-1; i++)
        str += lengths.get(i)+", ";
      str += lengths.get(length-1);
			str += "}";
		}
		str += "]";
		
		return str;
	}
		
	/**
	 * Prints this Delivery Data out to the specified output stream. This method
	 * is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Response data --");
		
		jpipMessageHeader.list(out);
		
		out.print("Chunks: ");
		if (chunks != null) { 
				int length = 0;
				for (byte[] d : chunks) length += d.length;
				out.println(", chunks=<data not displayed>");
				out.println("length="+length);
		} else
			out.println("null");
		
		out.print("File pointers: ");
		if (filePointers != null)
			for (long pointer : filePointers) out.print(pointer+" ");
		else out.print("null");
		out.println();
		
		out.print("Lengths: ");
		if (lengths != null) 
		for (int length : lengths) out.print(length+" ");
		else out.print("null");
		out.println("");
		
		out.flush();
	}
	
}
