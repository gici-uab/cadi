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
 * This class decodes bits for the fractional bit plane decoder of JPEG2000, decoding them with the MQ decoder. This class allows the use of the following MQ options: restart, reset, bypass. Usage example: <br>
 * &nbsp; construct<br>
 * &nbsp; decodeBit<br>
 * &nbsp; decodeBit<br>
 * &nbsp; decodeBit<br>
 * &nbsp; swapInputByteStream<br>
 * &nbsp; reset<br>
 * &nbsp; restart<br>
 * &nbsp; decodeBit<br>
 * &nbsp; decodeBit<br>
 * &nbsp; ...<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.2
 */
public class MQDecoder implements Decoder{

	/**
	 * Bytestream from which the MQ decoder reads data.
	 * <p>
	 * The content must be understood as a ByteStream.
	 */
	private CPByteStream inputByteStream;

	/**
	 * Number of contexts (defined by JPEG2000).
	 * <p>
	 * Positive number.
	 */
	private final int numContexts = 19;

	/**
	 * MQ context state initialization (defined by JPEG2000 standard).
	 * <p>
	 * First array index represents the context and second is: (p.e. [2][0] is the initial state of context 2)
	 * <ul>
	 *   <li> 0 - Initial state for the context
	 *   <li> 1 - Initial expected symbol for the context
	 * </ul>
	 */
	private final int[][] stateSymbolInit = {
		{4,  0}, //context  0
		{0,  0}, //context  1
		{0,  0}, //context  2
		{0,  0}, //context  3
		{0,  0}, //context  4
		{0,  0}, //context  5
		{0,  0}, //context  6
		{0,  0}, //context  7
		{0,  0}, //context  8
		{3,  0}, //context  9
		{0,  0}, //context 10
		{0,  0}, //context 11
		{0,  0}, //context 12
		{0,  0}, //context 13
		{0,  0}, //context 14
		{0,  0}, //context 15
		{0,  0}, //context 16
		{0,  0}, //context 17
		{46, 0}  //context 18
	};

	/**
	 * MQ probability state transition (defined by JPEG2000 standard).
	 * <p>
	 * First array index represents the current state and second is: (i.e. [1][0] is the transition to do when code a MPS symbol and the state of the context is 1)
	 * <ul>
	 *   <li> 0 - MPS transition
	 *   <li> 1 - LPS transition
	 *   <li> 2 - If 1 change MPS by LPS, nothing otherwise
	 *   <li> 3 - estimation (probability is--> 0xXXX / (2^16 * alpha) -alpha usually is 0.708-)
	 * </ul>
	 */
	private final int[][] stateTrans = {
		{ 1,  1, 1, 0x5601}, //state  0
		{ 2,  6, 0, 0x3401}, //state  1
		{ 3,  9, 0, 0x1801}, //state  2
		{ 4, 12, 0, 0x0AC1}, //state  3
		{ 5, 29, 0, 0x0521}, //state  4
		{38, 33, 0, 0x0221}, //state  5
		{ 7,  6, 1, 0x5601}, //state  6
		{ 8, 14, 0, 0x5401}, //state  7
		{ 9, 14, 0, 0x4801}, //state  8
		{10, 14, 0, 0x3801}, //state  9
		{11, 17, 0, 0x3001}, //state 10
		{12, 18, 0, 0x2401}, //state 11
		{13, 20, 0, 0x1C01}, //state 12
		{29, 21, 0, 0x1601}, //state 13
		{15, 14, 1, 0x5601}, //state 14
		{16, 14, 0, 0x5401}, //state 15
		{17, 15, 0, 0x5101}, //state 16
		{18, 16, 0, 0x4801}, //state 17
		{19, 17, 0, 0x3801}, //state 18
		{20, 18, 0, 0x3401}, //state 19
		{21, 19, 0, 0x3001}, //state 20
		{22, 19, 0, 0x2801}, //state 21
		{23, 20, 0, 0x2401}, //state 22
		{24, 21, 0, 0x2201}, //state 23
		{25, 22, 0, 0x1C01}, //state 24
		{26, 23, 0, 0x1801}, //state 25
		{27, 24, 0, 0x1601}, //state 26
		{28, 25, 0, 0x1401}, //state 27
		{29, 26, 0, 0x1201}, //state 28
		{30, 27, 0, 0x1101}, //state 29
		{31, 28, 0, 0x0AC1}, //state 30
		{32, 29, 0, 0x09C1}, //state 31
		{33, 30, 0, 0x08A1}, //state 32
		{34, 31, 0, 0x0521}, //state 33
		{35, 32, 0, 0x0441}, //state 34
		{36, 33, 0, 0x02A1}, //state 35
		{37, 34, 0, 0x0221}, //state 36
		{38, 35, 0, 0x0141}, //state 37
		{39, 36, 0, 0x0111}, //state 38
		{40, 37, 0, 0x0085}, //state 39
		{41, 38, 0, 0x0049}, //state 40
		{42, 39, 0, 0x0025}, //state 41
		{43, 40, 0, 0x0015}, //state 42
		{44, 41, 0, 0x0009}, //state 43
		{45, 42, 0, 0x0005}, //state 44
		{45, 43, 0, 0x0001}, //state 45
		{46, 46, 0, 0x5601}  //state 46
	};

	/**
	 * Interval range.
	 * <p>
	 * From right to left: 8 register bits, 3 spacer bits, 8 partial code byte, 1 carry bit.
	 */
	private int A;

	/**
	 * Lower down interval.
	 * <p>
	 * From right to left: 8 register bits, 3 spacer bits, 8 partial code byte, 1 carry bit.
	 */
	private int C;

	/**
	 * Number of bits to transfer (down counter).
	 * <p>
	 * Usually it is initialized to 8, unless carry situations that it initilized to 7. Then count down until 0.
	 */
	private int t;

	/**
	 * Byte to transfer.
	 * <p>
	 * Byte flushed to the outputByteStream.
	 */
	private int Tr;

	/**
	 * Number of bytes transferred to the outputByteStream.
	 * <p>
	 * Byte flushed to the output ByteStream.
	 */
	private int L;

	/**
	 * State and Most Probable Symbol of each context.
	 * <p>
	 * Array index is the context and the State [context][0] or MPS [context][1]. It must correspond to some stateTrans.
	 */
	private int[][] statesMPS = null;

	// ============================= public methods ==============================
	/**
	 * Constructor that initializes internal registers of the MQ decoder but does not initialize the inputByteStream. Before using the MQ decoder the function swapInputByteStream should be called.
	 */
	public MQDecoder(){
		//Initialize context status
		statesMPS = new int[numContexts][2];
		reset();
	}

	/**
	 * Constructor that initializes internal registers of the MQ decoder.
	 *
	 * @param inputByteStream the input stream of bytes
	 *
	 * @throws ErrorException when end of ByteStream is reached
	 */
	public MQDecoder(CPByteStream inputByteStream) throws ErrorException{
		//Copy the inputByteStream
		swapInputByteStream(inputByteStream);
		//Initialize context status
		statesMPS = new int[numContexts][2];
		reset();
		//Initialize internal registrers
		restart();
	}

	/**
	 * Decode a bit using the context 18 (equivalent probabilities).
	 *
	 * @return a boolean indicating the bit decoded
	 *
	 * @throws ErrorException when trying to read after the end of the inputByteStream
	 */
	public boolean decodeBit() throws ErrorException{
		return(decodeBit((byte) 18));
	}

	/**
	 * Decode a bit.
	 *
	 * @param context a byte that indicates the context of the decoded bit
	 * @return a boolean indicating the bit decoded
	 *
	 * @throws ErrorException when trying to read after the end of the inputByteStream
	 */
	public boolean decodeBit(int context) throws ErrorException{

		//OPTIMIZATION OF JPEG2000 BOOK (p 646) by Taubman/Marcellin
		int p = stateTrans[statesMPS[context][0]][3];
		int s = statesMPS[context][1];
		int x = s;

		A -= p;
		if((C & 0x00FFFF00) >= (p << 8)){
			//C_active = C_active - p
			C = ((C & ~0xFFFFFF00) | ((C & 0x00FFFF00) - (p << 8)));
			if(A < (1 << 15)){
				if(A < p){
					x = 1 - s;
					if(stateTrans[statesMPS[context][0]][2] == 1){
						statesMPS[context][1] = statesMPS[context][1] == 0 ? 1: 0; //Switch MPS/LPS if necessary
					}
					statesMPS[context][0] = stateTrans[statesMPS[context][0]][1];
				}else{
					statesMPS[context][0] = stateTrans[statesMPS[context][0]][0];
				}
				while(A < ((int) 1 << 15)){
					if(t == 0){
						fillLSB();
					}
					A <<= 1;
					C <<= 1;
					t--;
				}
			}
		}else{
			if(A < p){
				statesMPS[context][0] = stateTrans[statesMPS[context][0]][0];
			}else{
				x = 1 - s;
				if(stateTrans[statesMPS[context][0]][2] == 1){
					statesMPS[context][1] = statesMPS[context][1] == 0 ? 1: 0; //Switch MPS/LPS if necessary
				}
				statesMPS[context][0] = stateTrans[statesMPS[context][0]][1];
			}
			A = p;
			while(A < ((int) 1 << 15)){
				if(t == 0){
					fillLSB();
				}
				A <<= 1;
				C <<= 1;
				t--;
			}
		}

		/* "NORMAL" ALGORITHM
		int x;
		int s = statesMPS[context][1];
		int p = stateTrans[statesMPS[context][0]][3];

		//Construction of the new sub-interval. Assign new values to C and A from the probability p.
		A -= p;
		//Conditional exchange of MPS and LPS in order to associate the highest length interval (p or A) to the MPS.
		if(A < p){
			s = 1 - s;
		}
		int C_active = (C & 0x00FFFF00) >>> 8;
		if(C_active < p){ //Compare active region of C
			x = 1 - s;
			A = p;
		}else{
			x = s;
			C_active -= p;
			C_active <<= 8;
			C &= (~0xFFFFFF00);
			C |= C_active;
		}
		if(A < ((int) 1 << 15)){
			if(x == statesMPS[context][1]){ //The symbol was a real MPS
				statesMPS[context][0] = stateTrans[statesMPS[context][0]][0];
			}else{ //The symbol was a real LPS
				if(stateTrans[statesMPS[context][0]][2] == 1){
					statesMPS[context][1] = statesMPS[context][1] == 0 ? 1: 0; //Switch MPS/LPS if necessary
				}
				statesMPS[context][0] = stateTrans[statesMPS[context][0]][1];
			}
		}

		//Perform renormalization shift
		while(A < ((int) 1 << 15)){
			if(t == 0){
				fillLSB();
			}
			A <<= 1;
			C <<= 1;
			t--;
		}
		*/

		return(x == 1);
	}

	/**
	 * Fill the C register with a new byte of inputByteStream. If the end of the ByteStream is reached, then this function fills the new byte with a value of 0xFF.
	 *
	 * @throws ErrorException when trying to read after the end of the inputByteStream
	 */
	private void fillLSB() throws ErrorException{
		byte BL = 0;
			t = 8;
			if(L < inputByteStream.getNumBytes()){
				try{
					BL = inputByteStream.getByte(L);
				}catch(EOFException e){
					//This cannot happen!
					throw new ErrorException("Trying to read over the end of ByteStream.");
				}
			}
			//Reached the end of the inputByteStream OR readed a marker code (this should not happen)
			if((L == inputByteStream.getNumBytes()) || ((Tr == 0xFF) && (BL > 0x8F))){
				C += 0xFF;
				//User should be advised
				//if(L != inputByteStream.getNumBytes()){
				//	throw new WarningException("Readed a marker code inside a inputByteStream. This should not happend, but continue decoding filling the rest of the ByteStream with 0xFF.");
				//}
			}else{
				if(Tr == 0xFF){
					t = 7;
				}
				Tr = (0x000000FF & (int) BL);
				L++;
				C += (Tr << (8 - t));
			}
	}

	/**
	 * Swaps the current inputByteStream. After the call of this function you should restart de MQ decoder calling the restart function (it fills the internal registers with the bytes of the new inputByteStream).
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

	/* (non-Javadoc)
	 * @see CADI.Common.LogicalTarget.JPEG2000.Decode.Decoder#reset()
	 */
	public void reset(){
		for(int i = 0; i < numContexts; i++){
			statesMPS[i][0] = stateSymbolInit[i][0];
			statesMPS[i][1] = stateSymbolInit[i][1];
		}
	}

	/**
	 * Start the value of the internal variables A, C, ... of the MQ decoder filling them with the values of the inputByteStream. The context probabilities are maintained. This function should be called every time a new ByteStream is passed to the decoder.
	 *
	 * @throws ErrorException when end of ByteStream is reached
	 */
	public void restart() throws ErrorException{
		Tr = 0;
		L  = 0;
		C  = 0;
		fillLSB();
		C <<= t;
		fillLSB();
		C <<= 7;
		t -= 7;
		A = 0x8000;
	}

}
