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
package GiciFile;

import java.io.*;
import java.nio.*;


/**
 * This class receives a 3D array and saves in raw data.<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public final class SaveRawFile {
	private SaveRawFile() {
		// This class shall not be constructed.
	}
	
	private abstract static class BufferFiller {
		protected final int zSize, ySize, xSize;
		protected int z = 0, y = 0;
		private int outOfRangeWarnings = 0;
		
		protected BufferFiller(final int zSize, final int ySize, final int xSize) {
			//this.image = image
			this.zSize = zSize;
			this.ySize = ySize;
			this.xSize = xSize;
		}
		
		public final void inc() {
			y = (y + 1) % ySize;
			
			if (y == 0) {
				z = (z + 1) % zSize;
			}		
		}
		
		public final int getXSize() {
			return xSize;
		}

		public final int getYSize() {
			return ySize;
		}

		public final int getZSize() {
			return zSize;
		}
		
		protected final void warnOutOfRange(final String message) {
			if (outOfRangeWarnings == 0) {
				System.err.println(message);
			}
			outOfRangeWarnings++;
			
			if (outOfRangeWarnings == 11 || outOfRangeWarnings == 101 || outOfRangeWarnings == 1001) {
				System.err.println("[" + (outOfRangeWarnings - 1) + " duplicate warning messages suppressed]");
			} else if (outOfRangeWarnings == 10000) {
				System.err.println("[100000 reached; stopped reporting]");
			}
		}
		
		// Fills one line of image into the buffer
		public abstract void fillBufferBoolean(ByteBuffer buffer);
		public abstract void fillBuffer(ByteBuffer buffer);
		public abstract void fillBuffer(CharBuffer buffer);
		public abstract void fillBuffer(ShortBuffer buffer);
		public abstract void fillBuffer(IntBuffer buffer);
		public abstract void fillBuffer(LongBuffer buffer);
		public abstract void fillBuffer(FloatBuffer buffer);
		public abstract void fillBuffer(DoubleBuffer buffer);
	}
	
	private static class FloatBufferFiller extends BufferFiller {
		final private float[][][] imageSamples;
		
		public FloatBufferFiller(float[][][] image) {
			super(image.length, image[0].length, image[0][0].length);
			this.imageSamples = image;
		}
		
		public void fillBufferBoolean(ByteBuffer buffer) {
			for(int x = 0; x < xSize; x++){
				buffer.put(x, (byte)(imageSamples[z][y][x] == 0 ? 0 : 1));
			}
		}
		
		public void fillBuffer(ByteBuffer buffer) {
			for(int x = 0; x < xSize; x++){
				if (imageSamples[z][y][x] > 255 || imageSamples[z][y][x] < 0) {
					warnOutOfRange("Pixel out of range: " + imageSamples[z][y][x] + " not in [0, 255]");
				}
				
				byte out = (byte) (Math.max(Math.min(imageSamples[z][y][x], 255), 0));
				buffer.put(x, out);
			}
		}

		public void fillBuffer(CharBuffer buffer) {
			for(int x = 0; x < xSize; x++){
				if (imageSamples[z][y][x] > Character.MAX_VALUE || imageSamples[z][y][x] < Character.MIN_VALUE) {
					warnOutOfRange("Pixel out of range: " + imageSamples[z][y][x] + " not in [" + Character.MIN_VALUE + ", " + Character.MAX_VALUE + "]");
				}						
				
				char out = (char) Math.max(Math.min(imageSamples[z][y][x], Character.MAX_VALUE), Character.MIN_VALUE);
				buffer.put(x, out);
			}
		}

		public void fillBuffer(ShortBuffer buffer) {
			for(int x = 0; x < xSize; x++){
				if (imageSamples[z][y][x] > Short.MAX_VALUE || imageSamples[z][y][x] < Short.MIN_VALUE) {
					warnOutOfRange("Pixel out of range: " + imageSamples[z][y][x] + " not in [" + Short.MIN_VALUE + ", " + Short.MAX_VALUE + "]");
				}
				
				short out = (short) Math.max(Math.min(imageSamples[z][y][x], Short.MAX_VALUE), Short.MIN_VALUE);
				buffer.put(x, out);
			}
		}

		public void fillBuffer(IntBuffer buffer) {
			for(int x = 0; x < xSize; x++){
				if (imageSamples[z][y][x] > 2 << 20 || imageSamples[z][y][x] < -2 << 20) {
					warnOutOfRange("Pixel probably out of range.");
				}

				buffer.put(x, (int)imageSamples[z][y][x]);
			}
		}

		public void fillBuffer(LongBuffer buffer) {
			for(int x = 0; x < xSize; x++){
				buffer.put(x, (long)imageSamples[z][y][x]);
			}
		}

		public void fillBuffer(FloatBuffer buffer) {
			buffer.put(imageSamples[z][y]);
		}

		public void fillBuffer(DoubleBuffer buffer) {
			for(int x = 0; x < xSize; x++){
				buffer.put(x, (double)imageSamples[z][y][x]);
			}
		}
	}
	
	private static class IntegerBufferFiller extends BufferFiller {
		final private int[][][] imageSamples;
		
		public IntegerBufferFiller(int[][][] image) {
			super(image.length, image[0].length, image[0][0].length);
			this.imageSamples = image;
		}
		
		public void fillBufferBoolean(ByteBuffer buffer) {
			for(int x = 0; x < xSize; x++){
				buffer.put(x, (byte)(imageSamples[z][y][x] == 0 ? 0 : 1));
			}
		}
		
		public void fillBuffer(ByteBuffer buffer) {
			for(int x = 0; x < xSize; x++){
				if (imageSamples[z][y][x] > 255 || imageSamples[z][y][x] < 0) {
					warnOutOfRange("Pixel out of range: " + imageSamples[z][y][x] + " not in [0, 255]");
				}
				
				byte out = (byte) (Math.max(Math.min(imageSamples[z][y][x], 255), 0));
				buffer.put(x, out);
			}
		}

		public void fillBuffer(CharBuffer buffer) {
			for(int x = 0; x < xSize; x++){
				if (imageSamples[z][y][x] > Character.MAX_VALUE || imageSamples[z][y][x] < Character.MIN_VALUE) {
					warnOutOfRange("Pixel out of range: " + imageSamples[z][y][x] + " not in [" + Character.MIN_VALUE + ", " + Character.MAX_VALUE + "]");
				}						
				
				char out = (char) Math.max(Math.min(imageSamples[z][y][x], Character.MAX_VALUE), Character.MIN_VALUE);
				buffer.put(x, out);
			}
		}

		public void fillBuffer(ShortBuffer buffer) {
			for(int x = 0; x < xSize; x++){
				if (imageSamples[z][y][x] > Short.MAX_VALUE || imageSamples[z][y][x] < Short.MIN_VALUE) {
					warnOutOfRange("Pixel out of range: " + imageSamples[z][y][x] + " not in [" + Short.MIN_VALUE + ", " + Short.MAX_VALUE + "]");
				}
				
				short out = (short) Math.max(Math.min(imageSamples[z][y][x], Short.MAX_VALUE), Short.MIN_VALUE);
				buffer.put(x, out);
			}
		}

		public void fillBuffer(IntBuffer buffer) {
			buffer.put(imageSamples[z][y]);
		}

		public void fillBuffer(LongBuffer buffer) {
			for(int x = 0; x < xSize; x++){
				buffer.put(x, (long)imageSamples[z][y][x]);
			}
		}

		public void fillBuffer(FloatBuffer buffer) {
			for(int x = 0; x < xSize; x++){
				buffer.put(x, (float)imageSamples[z][y][x]);
			}
		}

		public void fillBuffer(DoubleBuffer buffer) {
			for(int x = 0; x < xSize; x++){
				buffer.put(x, (double)imageSamples[z][y][x]);
			}
		}
	}
	
	/**
	 * Saves image samples in raw data.
	 *
	 * @param imageSamples a 3D float array that contains image samples
	 * @param imageFile file name where raw data will be stored
	 * @param sampleType an integer representing the class of image samples type. Samples types can be:
	 *        <ul>
	 *          <li> 0- boolean
	 *          <li> 1- byte
	 *          <li> 2- char
	 *          <li> 3- short
	 *          <li> 4- int
	 *          <li> 5- long
	 *          <li> 6- float
	 *          <li> 7- double
	 *        </ul>
	 * @param byteOrder 0 if BIG_ENDIAN, 1 if LITTLE_ENDIAN
	 *
	 * @throws WarningException when the file cannot be saved (incorrect number of components, file format unrecognized, etc.)
	 */
	private static void saveRawData(BufferFiller bufferFiller, DataOutputStream dos, int sampleType, int byteOrder) throws IOException {		
		final int zSize = bufferFiller.getZSize();
		final int ySize = bufferFiller.getYSize();
		final int xSize = bufferFiller.getXSize();
		
		// Allocate a buffer to speedup I/O.
		
		//Buffer to perform data conversion
		ByteBuffer buffer;
		//Line size in bytes
		int byte_xSize;
		//Set correct line size
		int[] byte_xSize_table = {1/* boolean - 1 byte */, 1/* byte */, 2/* char */, 2/* short */, 4/* int */, 8/* long */, 4/* float */, 8/* double */}; 
		
		if (sampleType < 0 || sampleType >= byte_xSize_table.length) {
			throw new ArrayIndexOutOfBoundsException("Sample type out of range.");
		}
		
		byte_xSize = xSize * byte_xSize_table[sampleType];
		
		buffer = ByteBuffer.allocate(byte_xSize);

		// Set correct endianness
		
		switch (byteOrder) {
		case 0: //BIG ENDIAN
			buffer = buffer.order(ByteOrder.BIG_ENDIAN);
			break;
		case 1: //LITTLE ENDIAN
			buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);
			break;
		}

		//Save image
		//Further speed improvements can be achieved in the worst case where image width is little by fixing a min read size and not reading less than it
		for(int calls = 0; calls < zSize * ySize; calls++){
			switch(sampleType){
			case 0: //boolean (1 byte)
				bufferFiller.fillBufferBoolean(buffer);
				break;
			case 1: //unsigned int (1 byte)
				bufferFiller.fillBuffer(buffer);
				break;
			case 2: //unsigned int (2 bytes)
				bufferFiller.fillBuffer(buffer.asCharBuffer());
				break;
			case 3: //signed short (2 bytes)
				bufferFiller.fillBuffer(buffer.asShortBuffer());
				break;
			case 4: //signed int (4 bytes)
				bufferFiller.fillBuffer(buffer.asIntBuffer());
				break;
			case 5: //signed long (8 bytes)
				bufferFiller.fillBuffer(buffer.asLongBuffer());
				break;
			case 6: //float (4 bytes)
				bufferFiller.fillBuffer(buffer.asFloatBuffer());
				break;
			case 7: //double (8 bytes) - lost of precision
				bufferFiller.fillBuffer(buffer.asDoubleBuffer());
				break;
			}

			bufferFiller.inc();			
			dos.write(buffer.array(), 0, byte_xSize);
		}
	}

	private static void saveRawData(BufferFiller bufferFiller, String imageFile, int sampleType, int byteOrder) throws IOException {	
		//Open file
		File newFile = new File(imageFile);
		FileOutputStream fos = null;

		if (newFile.exists()){
			newFile.delete();
			newFile.createNewFile();
		}
		
		fos = new FileOutputStream(newFile);
		DataOutputStream dos = new DataOutputStream(fos);

		saveRawData(bufferFiller, dos, sampleType, byteOrder);		
		
		// Close file
		fos.close();
	}
	
	/**
	 * Saves image samples in raw data for float samples
	 * @see SaveRawFile
	 */
	public static void saveRawData(float[][][] imageSamples, DataOutputStream dos, int sampleType, int byteOrder) throws IOException {
		assert (imageSamples != null && dos != null);
		BufferFiller bufferFiller = new FloatBufferFiller(imageSamples); 
		saveRawData(bufferFiller, dos, sampleType, byteOrder);	
	}
	
	/**
	 * Saves image samples in raw data for float samples
	 * @see SaveRawFile
	 */
	public static void saveRawData(float[][][] imageSamples, String imageFile, int sampleType, int byteOrder) throws IOException {
		assert (imageSamples != null && imageFile != null);
		BufferFiller bufferFiller = new FloatBufferFiller(imageSamples); 
		saveRawData(bufferFiller, imageFile, sampleType, byteOrder);	
	}
	
	/**
	 * Saves image samples in raw data for float samples
	 * @see SaveRawFile
	 */
	public static void saveRawData(int[][][] imageSamples, String imageFile, int sampleType, int byteOrder) throws IOException {
		assert (imageSamples != null && imageFile != null);
		BufferFiller bufferFiller = new IntegerBufferFiller(imageSamples); 
		saveRawData(bufferFiller, imageFile, sampleType, byteOrder);	
	}
}
