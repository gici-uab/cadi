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

import CADI.Common.Network.JPIP.JPIPMessageHeader;

/**
 * The class <code>JPIPMessageEncoder</code> is useful to encode JPIP messages. It
 * contains the JPIP header that provides descriptive information to identify
 * the JPIP message in the data-bin.
 * <p>
 * Usage example:<br> 
 * &nbsp; constructor<br>
 * &nbsp; [setIndependentForm]<br>
 * &nbsp; encoderHeader<br>
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.2 2010/02/06
 */
public class JPIPMessageEncoder {

	/**
	 * Contains the JPIP message header.
	 */
	private JPIPMessageHeader header = null;

	/**
	 * It is an byte array where the encoded JPIP Message Header is stored. 
	 */
	private byte[] encodedHeader = null;	

	/**
	 * It is an index that indicates the position in jpipMessageHeader. This
	 * variable is used in the <code>encodeHeader</code> and <code>
	 * writeVBASInteger</code> methods.  
	 */
	private int position = 0;	
	
	/**
	 *	Message header can take an independent form and a dependent form. The
	 * independent form is a long form where the message headers are completely
	 * self-describing; their interpretation is independent of any other message
	 * headers. The optional shorter dependent form message headers make use of
	 * information in the headers of previous message; their decoding is
	 * dependant on the previous message. 
	 */		
	private boolean independentForm = true;
	
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.ServerControlField#useExtendedHeaders}.
	 */
	private boolean useExtendedHeader = false;
	
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

	// ============================= public methods ==============================
	/**
	 * Default constructor.
	 */
	public JPIPMessageEncoder() {
		this(true, false);
	}

	/**
	 * Constructor
	 * 
	 * @param independentForm definition in {@link #independentForm}.
	 */
	public JPIPMessageEncoder(boolean independentForm) {
		this(independentForm, false);
	}
	
	/**
	 * Constructor
	 * 
	 * @param independentForm definition in {@link #independentForm}.
	 * @param useExtendedHeader definition in {@link #useExtendedHeader}.
	 */
	public JPIPMessageEncoder(boolean independentForm, boolean useExtendedHeader) {
		header = new JPIPMessageHeader();
		reset();
		this.independentForm = independentForm;
		this.useExtendedHeader = useExtendedHeader;
	}
	
	/** 
	 * Sets the attributes to its initial vaules.
	 */
	public void reset() {		
		independentForm = true;
		useExtendedHeader = false;
		position = 0;
		lastClass = 0;
		lastCSn = 0;		
	}

	/**
	 * Sets the independent form for encoding the JPIP message header or the
	 * dependent form. 
	 * 
	 * @param independentForm if true, the independent form is used. False, the
	 * header is built using the dependent form.
	 */
	public void setIndependentForm(boolean independentForm) {
		reset();
		this.independentForm = independentForm;
	}
	
	/**
	 * Sets the {@link #useExtendedHeader} attribute.
	 * 
	 * @param useExtendedHeaders definition in {@link #useExtendedHeader}.
	 */
	public void setUseExtendedHeaders(boolean useExtendedHeaders) {
		this.useExtendedHeader = useExtendedHeaders;
	}
	
	/**
	 * Encodes a JPIP Message Header following a sequence of variable-length
	 * byte-aligned segments (VBAS).  The message header consists of a sequence
	 * of bytes, all but the last of which has a most significant bit (bit 7) of
	 * 1. The least significant 7 bits of each byte in the VBAS ar concatenated
	 * to form a bit stream which is used in differents ways for differents
	 * VBASs.
	 *  <P>
	 * Message header format: <BR>
	 * 
	 *  Bin-ID [,Class] [,CSn], Msg-Offset, Msg-Length [,Aux] 
	 *  where Bin-ID = BinIDIndicator, completeDataBin InClassIdentifier
	 */
	public byte[] encoderHeader(JPIPMessageHeader header) {
		this.header = header;

		if ( header.isEOR ) {
			encodeEOR();
		} else {
			encoderHeader(header.isLastByte,
              header.inClassIdentifier, header.classIdentifier,
              header.CSn,
              header.msgOffset, header.msgLength,
              header.Aux);
		}
		
		return (encodedHeader);	
	}
	
	/**
	 * Estimates the length of the JPIP message header.
	 * <p>
	 * This methods performs a rough estimation (upper bound) of the JPIP message
	 * length.
	 * 
	 * @param header an {@link CADI.Common.Network.JPIP.JPIPMessageHeader} object.
	 * @return the estimation, in bytes, of the JPIP message header length.
	 */
	public static int estimateLength(JPIPMessageHeader header) {
		
		
		int necessaryBits = (header.inClassIdentifier != 0)
												? (int)Math.ceil(Math.log(header.inClassIdentifier+1) / Math.log(2D) )
												: 1;		
		int necessaryBytes = (int) Math.ceil( (necessaryBits - 4) / 7D ); 	// The byte with the four first bits is not included
		
		int jpipMessageHeaderLength = necessaryBytes + 1;
		jpipMessageHeaderLength += (header.classIdentifier >= 0 )
																? calculateNecessaryBytes(header.classIdentifier)
																: 0;
		jpipMessageHeaderLength += (header.CSn >= 0)
																? calculateNecessaryBytes(header.CSn) : 0;
		jpipMessageHeaderLength += calculateNecessaryBytes(header.msgOffset);
		jpipMessageHeaderLength += calculateNecessaryBytes(header.msgLength);
		jpipMessageHeaderLength += (header.Aux >= 0) ? calculateNecessaryBytes(header.Aux) : 0;
		
		return jpipMessageHeaderLength;
	}
	
	/**
	 * Returns this JPIP Message as a String. It is useful for debugging purposes. 
	 */
  @Override
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";
		if ( !header.isEOR ) {
			str += " InClassIdentifier=" + header.inClassIdentifier;
			str += " ClassIdentifier=" + header.classIdentifier;
			str += " CSn=" + header.CSn;
			str += " MsgOffset=" + header.msgOffset;
			str += " MsgLength=" + header.msgLength;
			str += " Complete Data Bin: " + (header.isLastByte?"TRUE":"FALSE");			
			str += " Aux=" + header.Aux;
		} else {
			str += "EOR Message:";
			str += " Code=" + header.inClassIdentifier;
			str += " MsgLength="+ header.msgLength;
		}
		
		str += "]";

		return str;
	}

	/**
	 * Prints this JPIP Message Encoder fields out to the specified output
	 * stream. This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- JPIP Message Encoder --");		
		
		if ( !header.isEOR ) {
			out.println("InClassIdentifier: " + header.inClassIdentifier);
			out.println("ClassIdentifier: " + header.classIdentifier);
			out.println("CSn: " + header.CSn);
			out.println("MsgOffset: " + header.msgOffset);
			out.println("MsgLength: " + header.msgLength);
			out.println("Complete Data Bin: " + (header.isLastByte ? "true" : "false") );			
			out.println("Aux: " + header.Aux);
		} else {
			out.println("EOR Message");
			out.println("Code: " + header.inClassIdentifier);
			out.println("MsgLength: "+ header.msgLength);
		}	
						
		out.flush();
	}
	
	// ============================ private methods ==============================
	/**
	 * ISO/IEC 15444-9 section D.3
	 */
	private void encodeEOR() {
		//	EORMessage = byteIdentifier ReasonCode MessageBodyLength MessageBody
		// 	byteIdentifier = 0x00
		//		EORCode: two bytes, see ISO/IEC 15444-9 section D.3
		//		EORMessageBodyLength: a VBAS with the EORMessageBody length
		//		EORMessageBody: no normative interpretation
		
		assert( (header.EORCode >= 1) && (header.EORCode <= 0xFF) );
		
		int necessaryBytes = calculateNecessaryBytes(header.msgLength);
		encodedHeader = new byte[2+necessaryBytes];
		encodedHeader[0] = 0x00;
		encodedHeader[1] = (byte)(header.EORCode & 0xFF);
		position = 2;
		writeVBASInteger(header.msgLength);
	}	
	
	/**
	 * 
	 * 
	 * The message header consists of a sequence of bytes, all but the
	 * last of which has a most significant bit (bit 7) of 1. The least 
	 * significant 7 bits of each byte in the VBAS ar concatenatedto form a bit
	 * stream which is used in differents ways for differents VBASs.
	 * <P>
	 * Message header format: <BR>
	 * 
	 *  Bin-ID [,Class] [,CSn], Msg-Offset, Msg-Length [,Aux] 
	 *  where Bin-ID = BinIDIndicator, completeDataBin InClassIdentifier
	 */	
	private byte[] encoderHeader(boolean completeDataBin, long InClassIdentifier,
	                             int Class, int CSn, long MsgOffset,
	                             long MsgLength, int Aux) {
    
    assert(MsgOffset >= 0);
    assert(MsgLength >= 0);
    
		int necessaryBits, necessaryBytes, availableBits; 
		
		// Check parameters		
		if ( (Class < 0) && (CSn >= 0) ) {
			throw new IllegalArgumentException();
		}
		
		// If Aux < 0, Class must be not extended
		if (!useExtendedHeader) {
			if ((Class%2) == 1) Class -= 1;
			Aux = -1;
		}

		
		boolean includeClass = false;
		boolean includeCSn = false;
		
		
		// CALCULATES THE JPIP HEADER LENGTH
		necessaryBits = (InClassIdentifier != 0) ? (int)Math.ceil(Math.log(InClassIdentifier+1) / Math.log(2D) ) : 1;		
		necessaryBytes = (int) Math.ceil( (necessaryBits - 4) / 7D ); 	// The byte with the four first bits is not included
		
		int jpipMessageHeaderLength = necessaryBytes + 1;
		if (independentForm) {
			jpipMessageHeaderLength += (Class >= 0 ) ? calculateNecessaryBytes(Class) : 0;
			includeClass = (Class >= 0) ? true : false;
			
			jpipMessageHeaderLength += (CSn >= 0) ? calculateNecessaryBytes(CSn) : 0;			
			includeCSn = (CSn >= 0) ? true : false;
			
		} else {
			if ((Class >= 0) && (Class != lastClass)) {
				jpipMessageHeaderLength += (Class != -1) ? calculateNecessaryBytes(Class) : 0;
				lastClass = Class;
				includeClass = true;
			}
			if ((CSn >= 0) && (CSn != lastCSn)) {
				jpipMessageHeaderLength += (CSn != -1) ? calculateNecessaryBytes(CSn) : 0;
				lastCSn = CSn;
				includeCSn = true;
			}
		}
		
		jpipMessageHeaderLength += calculateNecessaryBytes(MsgOffset);
		jpipMessageHeaderLength += calculateNecessaryBytes(MsgLength);
		jpipMessageHeaderLength += useExtendedHeader && (Aux >= 0) ? calculateNecessaryBytes(Aux) : 0;
		
		encodedHeader = new byte[jpipMessageHeaderLength];
		
		
		// ENCODES VALUES
		position = 0;
		
		// BinID
		necessaryBits = (InClassIdentifier != 0) ? (int)Math.ceil( Math.log(InClassIdentifier+1) / Math.log(2D) ) : 1;		
		necessaryBytes = (int) Math.ceil( (necessaryBits - 4) / 7D ); 	// The byte with the four first bits is not included
		availableBits = 7 * necessaryBytes + 4;
					
		encodedHeader[0] = (necessaryBytes > 0) ? (byte)0x80 : (byte)0x00; //a bit		
		
		// Set Bin ID indicator (b bits)
		if (includeClass && includeCSn ) {
			encodedHeader[0] |= (byte)0x60;
		} else if (includeClass && !includeCSn){
			encodedHeader[0] |= (byte)0x40;
		} else if (!includeClass && !includeCSn){
			encodedHeader[0] |= (byte)0x20;
		} else {
			assert(true);
		}
		
		if (completeDataBin) {
			encodedHeader[0] |=  (byte)0x10;	//c bit
		}
		
		// d bits
		long mask = 1 << (availableBits-1);
		for (int i = 3; i >= 0; i--) {
			if ( (InClassIdentifier & mask) != 0) { 
				encodedHeader[0] |= (byte) (1 << i);
			}
			mask >>>= 1;
		}

		for (int nbyte = 1; nbyte <= necessaryBytes; nbyte++) {
			encodedHeader[nbyte] = (nbyte <= (necessaryBytes-1)) ? (byte)0x80 : (byte)0x00 ; // If last byte, the MSB must be 0, else 1
			for (int i = 6; i >= 0; i--) {
				if ( (InClassIdentifier & mask) != 0) { 
					encodedHeader[nbyte] |= (byte)(1 << i);
				}
				mask >>>= 1;
			}
		}
		position += necessaryBytes + 1;	
		// Class
		if ( includeClass ) {
			writeVBASInteger(Class);
		}
		
		// CSn
		if ( includeCSn ) {
			writeVBASInteger(CSn);
		}
		
		// Msg-Offset
		assert(MsgOffset >= 0 ); // Only non-negative values are allowed
		writeVBASInteger(MsgOffset);
				
		// Msg-Length			
		assert(MsgLength >= 0 ); // Only non-negative values are allowed
		writeVBASInteger(MsgLength);
				
		// Aux
		if (useExtendedHeader && (Aux >= 0)) {
			assert ((Class%2==1) && (Class!=0) );
			writeVBASInteger(Aux);
		}

		return (encodedHeader);	
	}
	
	/**
	 * Encodes a value with with a VBAS in a buffer. The first encoded byte will
	 * be located in a specified position of the buffer.
	 * 
	 * @param value the value to be encoded.
	 */
	private void writeVBASInteger(long value) {
		int necessaryBytes, availableBits;
		long mask = 0;
		
		necessaryBytes = calculateNecessaryBytes(value);		
		availableBits = 7*necessaryBytes;
		mask = 1 << (availableBits-1);

		for (int nbyte=0; nbyte<necessaryBytes; nbyte++, position++) {			
			encodedHeader[position] = (nbyte==(necessaryBytes-1)) ? (byte)0x00 : (byte)0x80 ; 	// If last byte, the MSB must be 0, else 1			
			for (int bitPos=6; bitPos>=0; bitPos--) {
				if ( (value&mask) != 0) {
					encodedHeader[position] |= (byte)(1<<bitPos);					
				}
				mask >>= 1;
			}		
		}	
		
	}	

	/**
	 * Calculate the number of bytes that are necessary to encode a value
	 * using the VBAS.
	 * 
	 * @param value the value to encode
	 * @return the necessary bytes
	 */
	private static int calculateNecessaryBytes(long value) {
				
		int necessaryBits = (value != 0) ? (int)Math.ceil( Math.log(value+1) / Math.log(2D) ) : 1;
		return ( (int) Math.ceil( necessaryBits / 7D ) );		
	}
	
}