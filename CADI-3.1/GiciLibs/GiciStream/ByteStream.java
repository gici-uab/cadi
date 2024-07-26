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
 * This class is a byte buffer, useful to store a byte stream. When the length of the ByteStream is known a priori, this class should be instantianed using the constructor with the initialSizeBuffer. When the length it is not known, this class begins instantiating a small buffer and duplicates its size (copying all contents) when it is full. The duplication of the buffer length is performed until maxBuffAllocation is reached, then is increased the number of bytes of this variable. Variables initialBuffAllocation and maxBuffAllocation controls the performance of this method; default values ara intialized to be efficient when containing small buffers (not more than 10 KB). If you need larger buffer capacities, you should change these variables.<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1
 */
public class ByteStream{

	/**
	 * Byte array used to store bytes.
	 * <p>
	 * All values allowed.
	 */
	private byte[] byteBuffer = null;

	/**
	 * Index of the current position of the byte buffer (number of bytes saved in byteBuffer minus 1).
	 * <p>
	 * Negative values are not allowed for this field.
	 */
	private int index = -1;

	/**
	 * Initial length of the byte buffer.
	 * <p>
	 * Negative values are not allowed for this field.
	 */
	private final int initialBuffAllocation = 8;

	/**
	 * When the buffer is full, its length is duplicated until maxBuffAllocation is reached. Then the size of the buffer is incremented this amount of bytes.
	 * <p>
	 * Negative values are not allowed for this field.
	 */
	private final int maxBuffAllocation = 512;


	/**
	 * Class constructor (performs needed initializations).
	 */
	public ByteStream(){
		//Initializations
		byteBuffer = new byte[initialBuffAllocation];
	}

	/**
	 * Class constructor with a specified amount of bytes to allocate initially in the buffer.
	 *
	 * @param initialSizeBuffer number of bytes that initially the buffer will contain
	 */
	public ByteStream(int initialSizeBuffer){
		//Initializations
		byteBuffer = new byte[initialSizeBuffer];
	}

	/**
	 * Class constructor allowing to insert a byte array (performs needed initializations).
	 *
	 * @param byteBuffer the byte buffer to use
	 * @param numBytes of bytes valid in byte buffer
	 */
	public ByteStream(byte[] byteBuffer, int numBytes){
		//Initializations
		this.byteBuffer = byteBuffer;
		this.index = numBytes - 1;
	}

	/**
	 * Inserts a byte in the ByteStream.
	 *
	 * @param b the byte that will be inserted into byte stream
	 */
	public void addByte(byte b){
		index++;
		//Buffer reaches its capcaity
		if(index == byteBuffer.length){
			byte[] byteBufferTMP = null;
			if(byteBuffer.length < maxBuffAllocation){
				byteBufferTMP = new byte[byteBuffer.length + byteBuffer.length];
			}else{
				byteBufferTMP = new byte[byteBuffer.length + maxBuffAllocation];
			}
			System.arraycopy(byteBuffer, 0, byteBufferTMP, 0, byteBuffer.length);
			byteBuffer = byteBufferTMP;
		}
		byteBuffer[index] = b;
	}

	/**
	 * Inserts bytes in the bytestream.
	 *
	 * @param bytes an array of bytes that will be inserted into byte stream
	 * @param numBytes the number of bytes to insert in the bytestream
	 */
	public void addBytes(byte[] bytes, int numBytes){
		//Buffer reaches its capcaity
		if(index + numBytes >= byteBuffer.length){
			byte[] byteBufferTMP = new byte[byteBuffer.length + numBytes + (byteBuffer.length + numBytes >= maxBuffAllocation ? maxBuffAllocation: byteBuffer.length + numBytes)];
			System.arraycopy(byteBuffer, 0, byteBufferTMP, 0, index + 1);
			byteBuffer = byteBufferTMP;
		}
		System.arraycopy(bytes, 0, byteBuffer, index + 1, numBytes);
		index += numBytes;
	}

	/**
	 * Deletes a number of bytes of the bytestream (at the end of bytestream). If number of bytes to delete is greater than bytestream, bytestream length will be 0.
	 *
	 * @param num the number of bytes to delete
	 */
	public void deleteEndBytes(int num){
		index = index - num < -1 ? -1: index - num;
	}

	/**
	 * Deletes a number of bytes of the bytestream (at the begining of bytestream). If number of bytes to delete is greater than bytestream, bytestream length will be 0.
	 * ATTENTION: this function compels to copy the whole ByteStream. It is poor efficient!!!
	 *
	 * @param num the number of bytes to delete
	 */
	public void deleteBeginBytes(int num){
		if(num <= index){
			byte[] byteBufferTMP = new byte[byteBuffer.length];
			System.arraycopy(byteBuffer, num, byteBufferTMP, 0, index + 1 - num);
			byteBuffer = byteBufferTMP;
			index -= num;
		}else{
			index = -1;
		}
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
		if((numByte >= 0) && (numByte <= index)){
			return(byteBuffer[numByte]);
		}else{
			throw new WarningException("Byte number must be between 0 and number of bytes of bytestream.");
		}
	}

	/**
	 * Return the byte stream in an array of bytes (the length of array byte returned is equal or greater than the real bytes contained in byteStream -it can be known using getNumBytes function-).
	 *
	 * @return a byte array that contains the byte stream
	 */
	public byte[] getByteStream(){
		return(byteBuffer);
	}

	/**
	 * Return the number of bytes in the byte stream.
	 *
	 * @return an integer indicating number of bytes in the byte stream
	 */
	public int getNumBytes(){
		return(index+1);
	}

}
