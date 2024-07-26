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
package CADI.Client.Network;

import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ProtocolException;

import CADI.Common.Network.JPIP.JPIPMessage;

/**
 * This class reads JPIP messages as they are defined in ISO/IEC 15444-9
 * section A.2. Each time the <code>{@link #readMessage()}</code>  method is
 * called, only one JPIP message is read. And the method who is calling must be
 * taken into account when a JPIP End Of Response message is received, then it
 * must not call the <code>{@link #readMessage()}</code> method. If it call
 * after the JPIP End Of Response message is received, a <code>IOException
 * </code> will be thrown.
 * <p>
 * This class needs a input stram where data are readed from. This class
 * only needs a <code>read</code> method to read a <code>byte</code> value and
 * a <code>readFully</code> method to read a byte array. 
 * <p>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; setParameters<br>
 * &nbsp; readMessage<br> 
 * &nbsp; ....<br>
 * &nbsp; readMessage<br>
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2008/01/10
 */
public class JPIPMessageDecoder {

	/**
	 * It is the input stream where data are read from.
	 */
	JPIPMessageReader inputStreamReader = null;
	
	
	// INTERNAL ATTRIBUTES

	/**
	 * Contains the last Class value. It is a state variable used when dependent
	 * form is used.
	 */
	private int lastClass = 0;

	/**
	 * Contains the last CSn value. It is a state variables used when dependent
	 * form is used.
	 */
	private int lastCSn = 0;

	/**
	 * Indicates the length of the JPIP message header. This attribute is
	 * useful for statistics.
	 */
	private long headerLength = 0;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public JPIPMessageDecoder() {
	}			

	/**
	 * Sets the input stream where the data are read from. This method must be
	 * called before another one is called.
	 * 
	 * @param inputStreamReader definition in {@link #inputStreamReader}
	 */
	/*public void setParameters (HTTPResponseReader inputStreamReader) {
		this.inputStreamReader = inputStreamReader;
	}*/

  public void setParameters (JPIPMessageReader inputStreamReader) {
		this.inputStreamReader = inputStreamReader;
	}
	
	/**
	 * This method is used to read a JPIP messase. Data are read from the input
	 * stream, they are decoded, and a <code>JPIPMessage</code> object is built. 
	 * 
	 * @return a <code>JPIPMessage</code> object.
	 * 
	 * @throws ProtocolException if received data can not be decoded correctly.
	 * @throws IOException if data can not be read from the input stream. It may
	 *  be because the link is broken, the server closed the connection, ...
	 */
	public JPIPMessage readMessage() throws ProtocolException, IOException {

		int BinIDIndicator = 0;
		long inClassIdentifier = 0;
		boolean completeDataBin = false;
		int tempByte = 0;
		
		// Initialization
		headerLength = 0;
		JPIPMessage jpipMessage = new JPIPMessage();
		
		//	Bin-ID
		tempByte = inputStreamReader.read();
		headerLength++;

		if (tempByte == 0x00) { // EOR is reached
			jpipMessage = readEORMessage();
			jpipMessage.headerLength = headerLength;
			return jpipMessage;
		}

		// b bits
		BinIDIndicator = (tempByte >>> 5) & 0x03;
		if ( (BinIDIndicator < 1) || (BinIDIndicator > 3) ) {
			throw new ProtocolException("Wrong server response: impossible to decode it correctly");
		}

		// c bit
		completeDataBin = (tempByte & 0x10)==0 ? false : true;

		// d bits (In-Class ID)
		inClassIdentifier = tempByte & 0x0F;
		if ((tempByte >>> 7) > 0 ) {
			int numBytesVBAS = 1;
			do {
				tempByte = inputStreamReader.read();
				headerLength++;
				if (tempByte == -1) {
					throw new EOFException("There is not data available to read the VBAS");
				}
				inClassIdentifier = (inClassIdentifier << 7) | (long)(tempByte & 0x7F);
				numBytesVBAS++;
				
				if (numBytesVBAS > 9) {	// maximum long value is 2^63 - 1 => 9 bytes VBAS 
					throw new ProtocolException("VBAS length is larger than 63 bits (which is the maximum of long)");
				}
			} while ( (tempByte & 0x80) != 0 );
		}
		
		jpipMessage.header.isLastByte = completeDataBin;
		jpipMessage.header.inClassIdentifier = (int)inClassIdentifier;

		// Class		
		if ( (BinIDIndicator == 2) || (BinIDIndicator == 3) )  {
			jpipMessage.header.classIdentifier = (int)readVBAS();
			lastClass = jpipMessage.header.classIdentifier;

		} else {
			jpipMessage.header.classIdentifier = lastClass;
		}
		if ((jpipMessage.header.classIdentifier < 0)
            || (jpipMessage.header.classIdentifier > 8) ) {
			throw new ProtocolException("Wrong server response: invalid value for Class identifier)");
    }
    
		// CSn				
		if ( BinIDIndicator == 3) {
			jpipMessage.header.CSn = (int)readVBAS();
			lastCSn = jpipMessage.header.CSn;
			
		} else {
			jpipMessage.header.CSn = lastCSn;
		}
		
		// Msg-Offset
		jpipMessage.header.msgOffset = (int)readVBAS();
    
		// Msg-Length						
		jpipMessage.header.msgLength = (int)readVBAS();			

		// Aux
		if ( (jpipMessage.header.classIdentifier % 2) == 1 ) {
			jpipMessage.header.Aux = (int)readVBAS();
		}
		
		// Read jpip message body
		if (jpipMessage.header.msgLength > 0) {
			jpipMessage.messageBody = new byte[(int)jpipMessage.header.msgLength];
			inputStreamReader.readFully(jpipMessage.messageBody, 0,
			                            (int)jpipMessage.header.msgLength);					
		}
    
		jpipMessage.headerLength = headerLength;
	
		return jpipMessage;
	}	

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";

		str += "<<< Not implemented yet >>> ";

		str += "]";
		return str;
	}
	
	/**
	 * Prints this JPIP Message Decoder fields out to the specified output
	 * stream. This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- JPIP Message Decoder --");		
		
		out.println("<<< Not implemented yet >>> ");
						
		out.flush();
	}
	
// ============================ private methods ==============================
	/**
	 * Reads a Variable-length Byte-Aligned Segment. 
	 * 
	 * @return the value which has been read from the VBAS.
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	private long readVBAS() throws IOException {
		long value = 0;
		int tempByte;
		int numBytesVBAS = 0;
		
		do {			
			tempByte = inputStreamReader.read();
			headerLength++;
			
			if (tempByte == -1) {
				throw new EOFException("There is not data available to read the VBAS");
			}

			value = (value << 7) | (long) (tempByte & 0x7F);
			numBytesVBAS++;

			if (numBytesVBAS > 9) {	// maximum long value is 2^63 - 1 => 9 bytes VBAS 
				throw new ProtocolException("VBAS length is larger than 63 bits (which is the maximum of long)");
			}

		} while ( (tempByte & 0x80) != 0 );					
		
		return value;
	}
	
	/**
	 * Reads the End Of Response message. Further information see
	 * ISO/IEC 15444-9 Annex D.3
	 * 
	 * @return a JPIPMessage object with the End Of Response. 
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	private JPIPMessage readEORMessage() throws IOException {
		JPIPMessage jpipMessage = null; 

		jpipMessage = new JPIPMessage();			
		jpipMessage.header.isEOR = true;

		// Read EOR code		
		jpipMessage.header.EORCode = inputStreamReader.read();
		headerLength++;
		
		// Read EOR body length
		int EORBodyLength = (int)readVBAS();
		jpipMessage.header.msgLength = EORBodyLength;

		// Read EOR body
		if (EORBodyLength > 0 ) {
			jpipMessage.messageBody = new byte[EORBodyLength];
			inputStreamReader.readFully(jpipMessage.messageBody, 0, EORBodyLength);
		}
		
		return jpipMessage;
	}

}