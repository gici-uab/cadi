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

/** 
 * The class <code>JPIPMessage</code> is useful to store JPIP messages. It
 * contains: the JPIP header that provides descriptive information to identify
 * the JPIP message in the data-bin, and the message body which is a segment
 * from a data-bin.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2007-2012/10/26
 */
public class JPIPMessage {

	/**
	 * Contains the JPIP message header. {@link JPIPMessageHeader}
	 */
	public JPIPMessageHeader header = null;

	/**
	 * Is the body of the JPIP message, i.e., it is data from a data-bin.
	 */
	public byte[] messageBody = null;

	/**
	 * Indicates the length of the JPIP message header. This attribute is
	 * useful for statistics.
	 */
	public long headerLength = 0;
	
	// ============================= public methods ==============================
	/**
	 * Default constructor.
	 */
	public JPIPMessage() {
		header = new JPIPMessageHeader();		
	}

	/** 
	 * Sets the attributes to its initial vaules.
	 */
	public void reset() {
		header.reset();
		messageBody = null;
		headerLength = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";
		if (!header.isEOR) {
			str += "CSn="+header.CSn;
			str += ", ClassIdentifier="+header.classIdentifier;
			str += ", InClassIdentifier="+header.inClassIdentifier;
			str += ", MsgOffset="+header.msgOffset;
			str += ", MsgLength="+header.msgLength;
			str += ", Complete Data Bin="+(header.isLastByte?"TRUE":"FALSE");			
			str += ", Aux="+header.Aux;
			str += ", Header Length="+headerLength;
		} else {
			str += "EOR Message:";
			str += ", Code="+header.EORCode;
			str += ", MsgLength="+header.msgLength;
			str += ", Header Length="+headerLength;
		}
		
		if (messageBody != null) {
			str += " MessageBody=";
			str += " <<< Not displayed >>>";
			/*for (int i=0; i<messageBody.length; i++) {
					if ((0xFF&messageBody[i]) < 16) {
						str += "0";
					}
					str += Integer.toHexString(0xFF&messageBody[i]);
				}*/			
		}
		str += "]";

		return str;
	}	
	
	/**
	 * Prints this JPIPMessage out to the specified output stream. This method
	 * is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- JPIP Message --");
		
		if (!header.isEOR) {
			out.println("CSn: "+header.CSn);
			out.println("ClassIdentifier: "+header.classIdentifier);
			out.println("InClassIdentifier: "+header.inClassIdentifier);
			out.println("MsgOffset: "+header.msgOffset);
			out.println("MsgLength: "+header.msgLength);
			out.println("Complete Data-Bin: "+(header.isLastByte?"TRUE":"FALSE"));			
			out.println("Aux: "+header.Aux);
			out.println("Header Length: "+headerLength);
		} else {
			out.println("EOR Message:");
			out.println(" Code: "+header.EORCode);
			out.println(" MsgLength: "+header.msgLength);
			out.println(" Header Length: "+headerLength);
		}
		
		if (messageBody != null) {
			out.print("MessageBody: ");
			out.println(" <<< Not displayed >>>");
			/*for (int i=0; i<messageBody.length; i++) {
				if ((0xFF&messageBody[i]) < 16) {
					out.print("0");
				}
				out.print(Integer.toHexString(0xFF&messageBody[i]));
			}
			out.println();
			*/
		}
		out.flush();
	}
	
}