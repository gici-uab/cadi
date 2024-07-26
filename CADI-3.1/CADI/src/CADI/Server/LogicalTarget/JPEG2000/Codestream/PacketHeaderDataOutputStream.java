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
package CADI.Server.LogicalTarget.JPEG2000.Codestream;

import java.io.IOException;
import java.io.PrintStream;

import CADI.Common.io.BufferedDataOutputStream;

/**
 * This class implements a data output stream for the packet header encoder
 * machinery. It includes all necessary bits to ensure that the packet header
 * will not contain any of code-stream's delimiting marker codes (0xFF90 to
 * 0xFFFF).
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2008/12/27
 */
public class PacketHeaderDataOutputStream {

	/**
	 * 
	 */
	BufferedDataOutputStream bufferedDataOutputStream = null;
	
	
	// INTERNAL ATTRIBUTES
	
	/**
	 * Index and buffer for bit stuffing
	 */
	byte t = 8, T = 0;
	
	/**
	 * Indicates the default size of the buffer which will be used to encode
	 * the packet header.
	 */
	private static final int DEFAULT_BUFFER_SIZE = 5;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	PacketHeaderDataOutputStream() {
		bufferedDataOutputStream = new BufferedDataOutputStream(DEFAULT_BUFFER_SIZE);
		bufferedDataOutputStream.setResizable(true);
		t = 8;
		T = 0;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param bufferedDataOutputStream
	 */
	PacketHeaderDataOutputStream(BufferedDataOutputStream bufferedDataOutputStream) {
		if (bufferedDataOutputStream == null) throw new NullPointerException();
		if (!bufferedDataOutputStream.isResizable()) throw new IllegalArgumentException("BufferedDataOutputStream must be resizable");
		
		this.bufferedDataOutputStream = bufferedDataOutputStream;
		t = 8;
		T = 0;
	}
	
	/**
	 * Sets attributes to their initial values.
	 */
	public void reset() {
		bufferedDataOutputStream.reset();
		t = 8;
		T = 8;
	}
	
	/**
	 * 
	 * @param bufferedDataOutputStream
	 */
	public void setInput(BufferedDataOutputStream bufferedDataOutputStream) {
		if (bufferedDataOutputStream == null) throw new NullPointerException();
		
		this.bufferedDataOutputStream = null;
		this.bufferedDataOutputStream = bufferedDataOutputStream;
		t = 8;
		T = 0;
	}
			
	/**
	 * Add a bit to the packet header. Ensure that the packet header will not
	 * contain any of code-stream's delimiting marker codes (0xFF90 to 0xFFFF).
	 *
	 * @param x bit to be packect.
	 * 
	 * @throws IOException 
	 */
	public void emitTagBit(byte x) throws IOException{
		t--;
		T += (byte) (1 << t) * x;
		if(t == 0){
			bufferedDataOutputStream.writeByte(T);
			t = 8;
			if(T == (byte)0xFF){
				t = 7;
			}
			T = 0;
		}
	}

	/**
	 * Bit stuffing up to 8 bits.
	 * 
	 * @throws IOException 
	 */
	public void bitStuffing() throws IOException{
		if(t!=8){
			for(; t >= 0; t--){
				T += (byte) (1 << t) * 0;
			}
			bufferedDataOutputStream.writeByte(T);
			t = 8;
			T = 0;
		}
	}
	
	/**
	 * Returns the array of bytes.
	 * 
	 * @return an array of bytes.
	 */
	public byte[] getByteArray() {
		return bufferedDataOutputStream.getByteArray(true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";
		str += bufferedDataOutputStream.toString();
		str += " t="+t;
		str += " T="+T;
		str += "]";
		
		return str;
	}
	
	/**
	 * Prints this Packet Header Data Output Stream out to the
	 * specified output stream. This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Packet Header Data Output Stream --");		
		
		bufferedDataOutputStream.list(out);
		out.println("t: "+t);
		out.println("T: "+T);
						
		out.flush();
	}
	
	
	/**************************************************************************/
	/**                      AUXILARY  METHODS                               **/ 
	/**************************************************************************/
	
}
