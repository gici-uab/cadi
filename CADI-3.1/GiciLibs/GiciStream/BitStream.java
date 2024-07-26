/*
 * GICI Library -
 * Copyright (C) 2007  Group on Interactive Coding of Images (GICI)
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
package GiciStream;
import GiciException.*;


/**
 * This class is a bit buffer, useful to store a bit stream.<br>
 * ATTENTION: this class should be optimized like ByteStream. Now it has a very poor performance!!!
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.2
 */
public class BitStream{
	
	/**
	 * Byte array used to store bits.
	 * <p>
	 * All values allowed.
	 */
	byte[] bitBuffer = null;
	
	/**
	 * Number of bits saved in bitBuffer.
	 * <p>
	 * Negative values are not allowed for this field.
	 */
	long numBits;
	
	/**
	 * Actual position in the bitstream (useful to scan bytestream from begin to end)..
	 * <p>
	 * Only positive values allowed.
	 */
	long actualPosition;
	
	/**
	 * Number of bytes used in each step of buffer allocation (when the buffer is full, another one es reserved with buffAllocation bytes more and the contents are copied).
	 * <p>
	 * Negative values are not allowed for this field.
	 */
	int buffAllocation = 64;
	
	
	/**
	 * Class constructor (performs needed initializations).
	 */
	public BitStream(){
		//Initializations
		bitBuffer = new byte[buffAllocation];
		numBits = 0;
		actualPosition = 0;
	}
	
	/**
	 * Class constructor with a specified amount of bits to allocate initially in the buffer.
	 *
	 * @param initialSizeBuffer number of bits that initially the buffer will contain
	 */
	public BitStream(int initialSizeBuffer){
		//Initializations
		bitBuffer = new byte[initialSizeBuffer / 8];
		buffAllocation = initialSizeBuffer / 8;
		numBits = 0;
		actualPosition = 0;
	}
	
	/**
	 * Class constructor that initilizaes the BitStream with a bitBuffer.
	 *
	 * @param bitBuffer a byte array from which the BitStream initializes his bitBuffer
	 */
	public BitStream(byte[] bitBuffer){
		//Initializations
		this.bitBuffer = bitBuffer;
		numBits = 8*bitBuffer.length;
		actualPosition = 0;
	}
	
	/**
	 * Inserts a bit in the bitstream.
	 *
	 * @param bit a boolean that indicates the bit (1=true, 0=false) to insert
	 */
	public void addBit(boolean bit){
		int byteIndex = (int) (numBits/8);
		
		//Bit insertion into bitBuffer
		if(bit){
			byte mask = 1;
			mask = (byte) (mask << (7 - (numBits % 8)));
			bitBuffer[byteIndex] |=  mask;
		}
		numBits++;
		
		//Buffer reaches its capcaity
		if(numBits/8 == bitBuffer.length){
			byte[] bitBufferTMP = new byte[bitBuffer.length + buffAllocation];
			for(int i = 0; i < bitBuffer.length; i++){
				bitBufferTMP[i] = bitBuffer[i];
			}
			bitBuffer = bitBufferTMP;
		}
	}
	
	/**
	 * Inserts a bit in the bitstream.
	 *
	 * @param bit a integer that indicates the bit (1=true, 0=false) to insert
	 */
	public void addBit(int bit){
		if(bit == 0){
			addBit(false);
		}else{
			addBit(true);
		}
	}
	
	/**
	 * Inserts the bits of a byte into the bitstream.
	 *
	 * @param byt the byte to be inserted
	 */
	public void addByte(int byt){
		for(int mask = 7; mask >= 0; mask--){
			addBit(byt & (1 << mask));
		}
	}
	
	/**
	 * Inserts the bits of an int to the bitstream starting at length MSB position of the int passed.
	 *
	 * @param bits a integer that contains the sequence of values to be inserted
	 * @param length a integer that indicates the number of bits to take into account
	 */
	public void addBits(int bits, int length){
		for(int mask = length-1; mask >= 0; mask--){
			addBit(bits & (1 << mask));
		}
	}
	
	/**
	 * Inserts a BitStream in the current object.
	 *
	 * @param bs BitStream object to append.
	 */
	public void addBitStream(BitStream bs){
		int numBits = 0;
		int mask = 0;
		int byt = 0;
		long numBytes = 0;
		byte[] data = null;
		
		if(bs != null){
			data = bs.getBitStream();
			numBytes = (long)(bs.getNumBits() / 8);
			numBits = (int) (bs.getNumBits() % 8);
			
			for(byt = 0; byt < numBytes; byt++){
				addByte(data[byt]);
			}
			
			for(int b = 7; b > (7 - numBits); b--){
				mask = (1 << b);
				addBit(data[byt] & mask);
			}
		}
	}
	
	/**
	 * Deletes a bit of the bitstream (starting at the end of bitstream).
	 */
	public void deleteEndBit(){
		deleteEndBits(1);
	}
	
	/**
	 * Deletes a number of bits of the bitstream (starting at the end of bitstream). If number of bits to delete is greater than bitstream, bitstream length will be 0.
	 *
	 * @param num the number of bits to delete
	 */
	public void deleteEndBits(long num){
		int lastByteIndex = (int) ((numBits-1) / 8);
		numBits = numBits - num < 0 ? 0: numBits - num;
		int firstByteIndex = (int) ((numBits-1) / 8);
		
		//Clean deleted bytes of bitBuffer byte array
		int numBitsFirstByte = (int) (numBits % 8);
		if(numBitsFirstByte > 0){
			byte mask = 1;
			int i = 1;
			for(; i < numBitsFirstByte; i++){
				mask = (byte) ((mask << 1) + 1);
			}
			for(; i < 8; i++){
				mask = (byte) (mask << 1);
			}
			bitBuffer[firstByteIndex] &= mask;
		}
		for(int i = firstByteIndex + 1; i <= lastByteIndex; i++){
			bitBuffer[i] = 0;
		}
	}
	
	/**
	 * Deletes a bit of the bitstream (starting at the begining of bitstream).
	 *
	 * @throws WarningException because it uses addBit functions
	 */
	public void deleteBeginBit() throws WarningException{
		deleteBeginBits(1);
	}
	
	/**
	 * Deletes a number of bits of the bitstream (starting at the begining of bitstream). If number of bits to delete is greater than bitstream, bitstream length will be 0.
	 *
	 * @param num the number of bits to delete
	 *
	 * @throws WarningException because it uses addBit functions
	 */
	public void deleteBeginBits(long num) throws WarningException{
		///////////////////////////////////
		//Poor eficiency - TO BE IMPROVED//
		///////////////////////////////////
		if(num < numBits){
			BitStream tmp = new BitStream();
			for(long bit = num; bit < numBits; bit++){
				tmp.addBit(this.getBit(bit));
			}
			bitBuffer = tmp.getBitStream();
			numBits = numBits - num;
			actualPosition = 0;
		}else{
			bitBuffer = new byte[buffAllocation];
			numBits = 0;
			actualPosition = 0;
		}
	}
	
	/**
	 * Return the bit value indicated.
	 *
	 * @param numBit the number of bit in bitstream (starting at 0)
	 * @return a boolean that represents the bit value indicated (true - 1, false - 0)
	 *
	 * @throws WarningException when numBit is greater than the buffer length
	 */
	public boolean getBit(long numBit) throws WarningException{
		if((numBit >= 0) && (numBit < numBits)){
			int byteIndex = (int) (numBit/8);
			byte originalByte = bitBuffer[byteIndex];
			byte mask = 1;
			mask = (byte) (mask << (7 - (numBit % 8)));
			return((mask & originalByte) != 0 ? true: false);
		}else{
			throw new WarningException("Bit number must be between 0 and number of bits of bitstream.");
		}
	}
	
	/**
	 * Return the actual bit and increments actual bit plus 1.
	 *
	 * @return a boolean that represents the bit value indicated (true - 1, false - 0)
	 *
	 * @throws WarningException when max buffer length is reached
	 */
	public boolean getBit() throws WarningException{
		return(getBit(actualPosition++));
	}
	
	/**
	 * Return length bits like an integer.
	 *
	 * @param length indicated the number of bits that must be given
	 * @return an integer that represents the bits value
	 *
	 * @throws WarningException when max buffer length is reached
	 */
	public int getBits(int length) throws WarningException{
		int bits = 0;
		for( int i=0 ; i < length; i++){
			bits = bits << 1;
			if(getBit()){
				bits++;
			}
		}
		return(bits);
	}
	
	/**
	 * Return the bit stream in an array of bytes (the length of array byte returned is equal or greater than the real bytes used in bitStream -it can be known using getNumBits function-).
	 *
	 * @return a byte array that contains the bit stream
	 */
	public byte[] getBitStream(){
		return(bitBuffer);
	}
	
	/**
	 * Return the number of bits in the bit stream.
	 *
	 * @return a long indicating number of bits in the bit stream
	 */
	public long getNumBits(){
		return(numBits);
	}
	
	/**
	 * Return the byte indicated.
	 *
	 * @param numByte the number of byte in bytestream (starting at 0)
	 * @return the byte asked for
	 *
	 * @throws WarningException when numByte is greater than the buffer length
	 */
	public byte getByte(int numByte) throws WarningException{
		if((numByte >= 0) && (numByte < getNumBytes())){
			return(bitBuffer[numByte]);
		}else{
			throw new WarningException("Byte number must be between 0 and number of bytes of bytestream.");
		}
	}
	
	/**
	 * Return the number of bytes in the bit stream.
	 *
	 * @return a long indicating number of bytes in the bit stream
	 */
	public long getNumBytes(){
		return(numBits <= 0 ? 0: ((numBits-1) / 8) + 1);
	}
	
	/**
	 * Return the position of the bit which is pointed by the bitstream
	 *
	 * @return a long indicating number of the bit which is pointed by the bitstream
	 */
	public long getActualPosition(){
		return(actualPosition);
	}
	
	/**
	 * Reset the bit stream
	 */
	public void reset(){
		bitBuffer = null;
		bitBuffer = new byte[buffAllocation];
		numBits = 0;
		actualPosition = 0;
	}
	
	/**
	 * Rewind the index in the bit stream
	 *
	 * @param numBits indicates the number of bits to be rewinded
	 *
	 * @throws WarningException when the number of bits to be rewinded is greater than the available.
	 */
	public void rewindBits(long numBits) throws WarningException{
		if (actualPosition>=numBits && numBits>0){
			actualPosition -= numBits;
		} else {
			throw new WarningException("It is not possible to rewind this number of bits, since it is greater than the available");
		}
	}
	
}
