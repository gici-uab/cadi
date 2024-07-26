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
package CADI.Common.LogicalTarget.JPEG2000.Decode;
import CADI.Client.ClientLogicalTarget.JPEG2000.CPByteStream;
import GiciException.*;
import GiciStream.*;
import java.io.EOFException;


/**
 * This class decodes bits from a compatible JPEG2000 codestream (without encoded with the MQ), i.e. when a byte 0xFF is found, the first bit of the following byte is skipped. This class could be used when encoding using the bypass mode of JPEG2000 or in other situations, for instance when decoding the packet headers. Usge example: <br>
 * &nbsp; construct<br>
 * &nbsp; decodeBit<br>
 * &nbsp; decodeBit<br>
 * &nbsp; swapInputByteStream<br>
 * &nbsp; restart<br>
 * &nbsp; decodeBit<br>
 * &nbsp; decodeBit<br>
 * &nbsp; ...<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public class RawDecoder implements Decoder{

	/**
	 * Bytestream from which the MQ decoder reads data.
	 * <p>
	 * The content must be understood as a ByteStream.
	 */
	private CPByteStream inputByteStream;

	/**
	 * Number of bits to transfer (down counter).
	 * <p>
	 * Usually it is initialized to 8, then count down until 0.
	 */
	private int t;

	/**
	 * Byte to transfer.
	 * <p>
	 * Byte flushed to the outputByteStream.
	 */
	private int Tr;

	/**
	 * Byte position to be read from the inputByteStream.
	 *<p>
	 * Only positive values allowed. The first byte is 0.
	 */
	private int L;

	/**
	 * Flag used to know if a byte with a value of 0xFF has been found. In this case a new byte is readed from the bytestream although it does not be used.
	 *<p>
	 * True when a 0xFF byte is found.
	 */
	private boolean foundFF;

	// ============================= public methods ==============================
	/**
	 * Constructor that does not initializes nothing. Before to use this class you should call the swapInputByteStream function.
	 */
	public RawDecoder(){}

	/**
	 * Constructor that initializes the internal registers and outputByteStream.
	 *
	 * @param inputByteStream the input stream of bytes
	 *
	 * @throws ErrorException when end of ByteStream is reached
	 */
	public RawDecoder(CPByteStream inputByteStream) throws ErrorException{
		//Copy the inputByteStream
		swapInputByteStream(inputByteStream);
		//Initialize internal registrers
		restart();
	}

	/**
	 * Decode a bit.
	 *
	 * @return a boolean indicating the bit decoded
	 *
	 * @throws ErrorException when end of ByteStream is reached
	 */
	public boolean decodeBit() throws ErrorException{
		t--;
		if(t == -1){
			try{
				if(foundFF){
					t = 6;
					Tr = (byte) inputByteStream.getByte(L-1);
				}else{
					t = 7;
					Tr = (byte) inputByteStream.getByte(L++);
				}
				if(Tr == (byte) 0xFF){
					foundFF = true;
					//Not really needed (only wrote to assure L is incremented and the Exception is not thrown)
					inputByteStream.getByte(L++);
				}else{
					foundFF = false;
				}
			}catch(EOFException e){
				throw new ErrorException("Reached the end of the ByteStream.");
			}
		}
		return((Tr & (1 << t)) > 0);
	}

	/**
	 * Decode a bit using some context. The context is not used (this function is only for compatibility).
	 *
	 * @param context a byte that indicates the context of the decoded bit
	 * @return a boolean indicating the bit decoded
	 *
	 * @throws ErrorException when end of ByteStream is reached
	 */
	public boolean decodeBit(int context) throws ErrorException{
		return(decodeBit());
	}

	/**
	 * Swaps the current inputByteStream. After calling this function you should call the restart function.
	 *
	 * @param inputByteStream the input stream of bytes
	 */
	public void swapInputByteStream(CPByteStream inputByteStream){
		if(inputByteStream != null){
			this.inputByteStream = inputByteStream;
		}else{
			this.inputByteStream = new CPByteStream(null, 0, 0);
		}
	}

	/**
	 * Fills the Tr register. This function should be called every time a new ByteStream is passed to the decoder.
	 *
	 * @throws ErrorException for compatibility
	 */
	public void restart() throws ErrorException{
		Tr = 0;
		L = 0;
		t = 0;
		foundFF = false;
	}

	/**
	 * For compatibility.
	 */
	public void reset(){}

}