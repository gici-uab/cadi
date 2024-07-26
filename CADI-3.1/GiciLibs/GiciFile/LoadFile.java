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
import GiciException.*;

import java.io.*;
import java.nio.*;
import java.util.zip.GZIPInputStream;

import java.awt.image.*;


/**
 * This class receives an image file and loads it.<br>
 * The image file can be a standard format or raw data. Size and data type must be specified if we use raw data loading.<br>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; getImage<br>
 * &nbsp; getTypes<br>
 * &nbsp; getRGBComponents<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.02
 */
public class LoadFile{
	
	/**
	 * Image samples (index meaning [z][y][x]).
	 * <p>
	 * All values allowed.
	 */
	float[][][] imageSamples = null;
	
	
	/**
	 * Number of image components.
	 * <p>
	 * Negative values are not allowed for this field.
	 */
	int zSize;
	
	/**
	 * Image height.
	 * <p>
	 * Negative values are not allowed for this field.
	 */
	int ySize;
	
	/**
	 * Image width.
	 * <p>
	 * Negative values are not allowed for this field.
	 */
	int xSize;
	
	/**
	 * First component where the image is load
	 */
	int zBegin;
	
	/**
	 * Type of the sample (byte, float, etc) for each component.
	 * <p>
	 * Only class types allowed.
	 */
	Class samplesType[];
	
	/**
	 * Indicate if the 3 first components are RGB.
	 * <p>
	 * All values allowed.
	 */
	boolean RGBComponents = false;
	
	/**
	 * Indicates whether pixels are signed 
	 */
	int[] signedPixels = null;
	
	/**
	 * Loads a pgm image.
	 * 
	 * @param imageFile an string that contains the name of the image file
	 * 
	 * @throws WarningException when the file cannot be load 
	 */
	public void LoadPGM(String imageFile) throws WarningException{
		try{
			// test for .gz and read on the fly with GZIPInputStream
			String extension = "";
			int dotPos = imageFile.lastIndexOf(".");
			
			if(dotPos >= 0){
				extension = imageFile.substring(dotPos + 1, imageFile.length());
			} 
			
			BufferedReader br = null;

			if (extension.compareToIgnoreCase("gz") == 0){
				br = new BufferedReader(new InputStreamReader(
						new GZIPInputStream(new FileInputStream(imageFile))));
			} else {
				br = new BufferedReader(new FileReader(imageFile));
			}
						
			String pgmHeader = null;
			String sizeHeader = null;
			String classHeader = null;
			int headerLength = 0;
			
			do {
				pgmHeader = br.readLine();
				headerLength += pgmHeader.length()+1;
			} while(pgmHeader.startsWith("#"));
			
			if (!(pgmHeader.equals("P5") || pgmHeader.equals("P2"))){
				throw new WarningException("File \"" + imageFile + "\" can not be loaded. Problems with the magic number"); 
			}
			
			do{
				sizeHeader = br.readLine();
				headerLength += sizeHeader.length()+1;
			} while(sizeHeader.startsWith("#"));
			
			String xS = sizeHeader.substring(0,sizeHeader.indexOf(" "));
			String yS = sizeHeader.substring(sizeHeader.indexOf(" ") + 1, sizeHeader.length());
			xSize = Integer.parseInt(xS); 
			ySize = Integer.parseInt(yS);
			zSize = 1;
			do{
				classHeader = br.readLine();
				headerLength += classHeader.length()+1;
			}while(classHeader.startsWith("#"));
			int maxValue = Integer.parseInt(classHeader);
			
			imageSamples = new float[zSize][ySize][xSize];
			samplesType = new Class[zSize];
			if(maxValue<=255){
				samplesType[0] = Byte.TYPE;
			} else {
				samplesType[0] = Short.TYPE;
			}
			
			if (pgmHeader.equals("P5")){
				DataInputStream dis = null;
				
				if (extension.compareToIgnoreCase("gz") == 0){
					dis = new DataInputStream(new GZIPInputStream(new FileInputStream(imageFile)));
				} else {
					dis = new DataInputStream(new FileInputStream(imageFile));	
				}
				
				for(int k=0;k<headerLength;k++){
					dis.readByte();
				}
				if (samplesType[0]==Byte.TYPE){
					ByteBuffer buffer = ByteBuffer.allocate(xSize);
					for(int y=0;y<ySize;y++){
						int bytes_read = 0;
						int buffer_fill = 0;
						
						try{
							// Keep trying till we get enough
							byte[] b = buffer.array();
							do {
								buffer_fill += bytes_read;
								bytes_read = dis.read(b, buffer_fill, xSize - buffer_fill);
							} while (bytes_read >= 0 && bytes_read + buffer_fill < xSize);
							
							if (bytes_read >= 0) {
								bytes_read += buffer_fill;
							}
						}catch(IOException e){
							throw new WarningException("I/O file reading error.");
						}
						
						if(bytes_read < 0) {
							throw new WarningException("File reading error (end of file reached before the full file has been read).");
						}
						
						if(bytes_read != xSize){
							throw new WarningException("File reading error (" + bytes_read + " bytes read, but " + xSize + " needed).");
						}
						
						for(int x=0;x<xSize;x++){
							imageSamples[0][y][x] = buffer.get(x) & 0xff;
						}
						
						buffer.clear();
					}
				} else {
					//FIXME this is really slow! it must be as the byte case
					
					for(int y=0;y<ySize;y++){
						for(int x=0;x<xSize;x++){							
							imageSamples[0][y][x] = dis.readChar();
						}
					}
				} 
				dis = null;
			} else {//pgm contains data stored in ascii
				String line = br.readLine();
				int[] integerArray = getIntegerArray(line);
				int arrayPosition = 0;
				for(int y=0;y<ySize;y++){
					for(int x=0;x<xSize;x++){
						if (arrayPosition==integerArray.length){
							arrayPosition = 0;
							line = br.readLine();
							integerArray = getIntegerArray(line);
						}
						imageSamples[0][y][x] = integerArray[arrayPosition];
						arrayPosition++;
						
					}
				}
				
			}
			
			br = null;
			this.signedPixels = new int[zSize];
			for(int z=0;z<zSize;z++){
				this.signedPixels[z] = 0;
			}
		} catch(Exception e){
			throw new WarningException("File \"" + imageFile + "\" can not be loaded."); 
		}
	}
	
	/**
	 * Receives a String that contains a sequence of integer numbers and returns an array with the integers in the String
	 *  
	 * @param line String containing integers separated by blank spaces
	 * 
	 * @return an array of integers containing the numbers of the String in the same order
	 */
	int[] getIntegerArray(String line){
		int[] array=null;
		if (line!=null){
			
			String values[] = line.split(" ");
			array = new int[values.length];
			for(int k=0;k<array.length;k++){
				array[k] = Integer.parseInt(values[k]);
			}
		}
		return array;
	}
	
	/**
	 * Loads an image (JAI loading).
	 *
	 * @param imageFile an string that contains the name of the image file
	 *
	 * @throws WarningException when the file cannot be load
	 */
	public LoadFile(String imageFile) throws WarningException{	
		String extension = "";
		int dotPos = imageFile.lastIndexOf(".");
		
		if(dotPos >= 0){
			extension = imageFile.substring(dotPos + 1, imageFile.length());
		} 
		
		// take into account a possible .gz extension
		int secondToLastPos = imageFile.lastIndexOf(".", dotPos - 1);
		
		if (secondToLastPos >= 0 && extension.compareToIgnoreCase("gz") == 0){
			extension = imageFile.substring(secondToLastPos + 1, dotPos);
		}
		
		if (extension.compareToIgnoreCase("pgm") != 0){
			//Image load with JAI
			BufferedImage buffImage = JaiWrapper.jaiLoad(imageFile);
			
			//Get size
			zSize = buffImage.getRaster().getNumBands();
			ySize = buffImage.getHeight();
			xSize = buffImage.getWidth();
			
			//Memory allocation
			imageSamples = new float[zSize][ySize][xSize];
			samplesType = new Class[zSize];
			
			//Sample type set
			for(int z = 0; z < zSize; z++){
				switch(buffImage.getType()){
				case BufferedImage.TYPE_3BYTE_BGR:
					this.samplesType[z] = Byte.TYPE;
					RGBComponents = true;
					break;
				case BufferedImage.TYPE_4BYTE_ABGR:
					this.samplesType[z] = Byte.TYPE;
					break;
				case BufferedImage.TYPE_4BYTE_ABGR_PRE:
					this.samplesType[z] = Byte.TYPE;
					break;
				case BufferedImage.TYPE_BYTE_BINARY:
					this.samplesType[z] = Byte.TYPE;
					break;
				case BufferedImage.TYPE_BYTE_GRAY:
					this.samplesType[z] = Byte.TYPE;
					break;
				case BufferedImage.TYPE_BYTE_INDEXED:
					this.samplesType[z] = Byte.TYPE;
					break;
				case BufferedImage.TYPE_INT_ARGB:
					this.samplesType[z] = Integer.TYPE;
					break;
				case BufferedImage.TYPE_INT_ARGB_PRE:
					this.samplesType[z] = Integer.TYPE;
					break;
				case BufferedImage.TYPE_INT_BGR:
					this.samplesType[z] = Integer.TYPE;
					break;
				case BufferedImage.TYPE_INT_RGB:
					this.samplesType[z] = Integer.TYPE;
					RGBComponents = true;
					break;
				case BufferedImage.TYPE_USHORT_555_RGB:
					this.samplesType[z] = Short.TYPE;
					RGBComponents = true;
					break;
				case BufferedImage.TYPE_USHORT_565_RGB:
					this.samplesType[z] = Short.TYPE;
					RGBComponents = true;
					break;
				case BufferedImage.TYPE_USHORT_GRAY:
					this.samplesType[z] = Short.TYPE;
					break;
				case BufferedImage.TYPE_CUSTOM:
				default:
					int bitsPixel[] = buffImage.getColorModel().getComponentSize();
				if(bitsPixel[z] <= 8){
					this.samplesType[z] = Byte.TYPE;
				}else{
					if(bitsPixel[z] <= 16){
						this.samplesType[z] = Short.TYPE;
					}else{
						if(bitsPixel[z] <= 32){
							this.samplesType[z] = Integer.TYPE;
						}else{
							if(bitsPixel[z] <= 64){
								this.samplesType[z] = Long.TYPE;
							}else{
								throw new WarningException("Unrecognized sample type loading " + imageFile + ".");
							}
						}
					}
				}
				//Usually this is correct ;-)
				if(zSize == 3){
					RGBComponents = true;
				}
				}
			}
			
			//Set the samples
			float pixel[] = new float[zSize];
			for(int y = buffImage.getMinY(); y < buffImage.getMinY() + ySize; y++){
				for(int x = buffImage.getMinX(); x < buffImage.getMinX() + xSize; x++){
					buffImage.getRaster().getPixel(x,y,pixel);
					for(int z = 0; z < zSize; z++){
						imageSamples[z][y][x] = pixel[z];
					}
				}
			}
			//Free JAI memory
			buffImage.flush();
//			img.dispose();
			buffImage = null;
			//img = null;
		} else {//the pgm is loaded
			LoadPGM(imageFile);
		}
	}

	/**
	 * Interface to call raw data image load.
	 *
	 * @param imageFile an string that contains the name of the image file
	 * @param zSize image depth
	 * @param ySize image height
	 * @param xSize image width
	 * @param sampleType an integer representing the class of image samples type
	 * @param byteOrder 0 if BIG_ENDIAN, 1 if LITTLE_ENDIAN
	 * @param RGBComponents a boolean that indicates if the three first components are RGB (true, otherwise false)
	 *
	 * @throws WarningException when the file cannot be load
	 */
	public LoadFile(String imageFile, int zSize, int ySize, int xSize, int sampleType, int byteOrder, boolean RGBComponents) throws WarningException{
		this(imageFile, zSize, ySize, xSize, sampleType, byteOrder,RGBComponents, 0);
	}

	/**
	 * Interface to call raw data image load.
	 *
	 * @param imageFile an string that contains the name of the image file
	 * @param zSize image depth
	 * @param ySize image height
	 * @param xSize image width
	 * @param sampleType an integer representing the class of image samples type
	 * @param byteOrder 0 if BIG_ENDIAN, 1 if LITTLE_ENDIAN
	 * @param RGBComponents a boolean that indicates if the three first components are RGB (true, otherwise false)
	 * @param zBegin first component where the image is begun to be load
	 *
	 * @throws WarningException when the file cannot be load
	 */
	public LoadFile(String imageFile, int zSize, int ySize, int xSize, int sampleType, int byteOrder, boolean RGBComponents, int zBegin) throws WarningException{
		setSignedPixels(sampleType,zSize);
		this.zBegin = zBegin;
		rawLoad(imageFile, zSize, ySize, xSize, getClass(sampleType), byteOrder,RGBComponents);
	}
	
	/**
	 * Interface to call raw data image load.
	 *
	 * @param imageFile an string that contains the name of the image file
	 * @param zSize an integer of image depth
	 * @param ySize an integer of image height
	 * @param xSize an integer of image width
	 * @param sampleType a Class of image samples type
	 * @param byteOrder 0 if BIG_ENDIAN, 1 if LITTLE_ENDIAN
	 * @param RGBComponents a boolean that indicates if the three first components are RGB (true, otherwise false)
	 *
	 * @throws WarningException when the file cannot be load
	 */
	public LoadFile(String imageFile, int zSize, int ySize, int xSize, Class sampleType, int byteOrder, boolean RGBComponents) throws WarningException{
		this(imageFile, zSize, ySize, xSize, sampleType, byteOrder, RGBComponents, 0);
	}

	/**
	 * Interface to call raw data image load.
	 *
	 * @param imageFile an string that contains the name of the image file
	 * @param zSize an integer of image depth
	 * @param ySize an integer of image height
	 * @param xSize an integer of image width
	 * @param sampleType a Class of image samples type
	 * @param byteOrder 0 if BIG_ENDIAN, 1 if LITTLE_ENDIAN
	 * @param RGBComponents a boolean that indicates if the three first components are RGB (true, otherwise false)
	 * @param zBegin first component where the image is begun to be load
	 *
	 * @throws WarningException when the file cannot be load
	 */
	public LoadFile(String imageFile, int zSize, int ySize, int xSize, Class sampleType, int byteOrder, boolean RGBComponents, int zBegin) throws WarningException{
		this.zBegin = zBegin;
		rawLoad(imageFile, zSize, ySize, xSize, sampleType, byteOrder, RGBComponents);
	}
	
	/**
	 * Indicates whether it is a header-less format or not.
	 * This function is intended to help discern which of the constructors
	 * shall be used.
	 * 
	 * @param fileName the file that will be checked for headers
	 */
	public static boolean isRaw(final String fileName) {
		// By now with this we do
		return (fileName.endsWith(".raw") || fileName.endsWith(".img") || fileName.endsWith(".rawl")
				|| fileName.endsWith(".raw.gz") || fileName.endsWith(".img.gz"));
	}
	
	/**
	 * Loads a raw data image.
	 *
	 * @param imageFile an string that contains the name of the image file
	 * @param zSize an integer of image depth
	 * @param ySize an integer of image height
	 * @param xSize an integer of image width
	 * @param sampleType a Class of image samples type
	 * @param byteOrder 0 if BIG_ENDIAN, 1 if LITTLE_ENDIAN
	 * @param RGBComponents a boolean that indicates if the three first components are RGB (true, otherwise false)
	 *
	 * @throws WarningException when the file cannot be load
	 */
	public void rawLoad(String imageFile, int zSize, int ySize, int xSize, Class sampleType, int byteOrder, boolean RGBComponents) throws WarningException{
		
		assert (imageFile != null);
		
		//Size set
		this.zSize = zSize;
		this.ySize = ySize;
		this.xSize = xSize;

		//Memory allocation
		imageSamples = new float[zSize][ySize][xSize];
		samplesType = new Class[zSize];

		//Sample type set
		for(int z = 0; z < zSize; z++){
			this.samplesType[z] = sampleType;
		}
		this.RGBComponents = RGBComponents;

		//Test first for .gz
		boolean fileIsAGZ = false;
		int dotPos = imageFile.lastIndexOf(".");
		
		if(dotPos >= 0){
			fileIsAGZ = imageFile.substring(dotPos + 1, imageFile.length()).compareToIgnoreCase("gz") == 0;
		} 
		
		//Open file and loads it
		InputStream fis = null;
		try{
			if (fileIsAGZ) {
				fis = new GZIPInputStream(new FileInputStream(imageFile), 1024*1024);
			} else {
				fis = new BufferedInputStream(new FileInputStream(imageFile), 1024*1024);
			}
		}catch(FileNotFoundException e){
			throw new WarningException("File \"" + imageFile + "\" not found.");
		}catch(IOException e) {
			throw new WarningException("File \"" + imageFile + "\" may be corrupted (or not in gzip format).");
		}
		
		DataInputStream dis = new DataInputStream(fis);

		//Buffer to perform data conversion
		ByteBuffer buffer;
		//Line size in bytes
		int byte_xSize;
		int t = getType(sampleType.getName());

		//Set correct line size
		switch(t){
		case 0: //boolean - 1 byte
			byte_xSize = xSize;
			break;
		case 1: //byte
			byte_xSize = xSize;
			break;
		case 2: //char
			byte_xSize = 2 * xSize;
			break;
		case 3: //short
			byte_xSize = 2 * xSize;
			break;
		case 4: //int
			byte_xSize = 4 * xSize;
			break;
		case 5: //long
			byte_xSize = 8 * xSize;
			break;
		case 6: //float
			byte_xSize = 4 * xSize;
			break;
		case 7: //double
			byte_xSize = 8 * xSize;
			break;
		default:
			throw new WarningException("Sample type unrecognized.");
		}
		buffer = ByteBuffer.allocate(byte_xSize);

		switch(byteOrder){
		case 0: //BIG ENDIAN
			buffer = buffer.order(ByteOrder.BIG_ENDIAN);
			break;
		case 1: //LITTLE ENDIAN
			buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);
			break;
		}

		//Read image
		//Further speed improvements can be achieved in the worst case where image width is little by fixing a min read size and not reading less than it
		if (zBegin != 0){
			try{
				dis.skip(zBegin * ySize * byte_xSize);
			}catch(IOException e){
				throw new WarningException("I/O file skipping error.");
			}
		}
		
		for(int z = 0; z < zSize; z++){
			for(int y = 0; y < ySize; y++){
				
				int bytes_read = 0;
				int buffer_fill = 0;
				
				try{
					// Keep trying till we get enough
					do {
						buffer_fill += bytes_read;
						bytes_read = dis.read(buffer.array(), buffer_fill, byte_xSize - buffer_fill);
					} while (bytes_read >= 0 && bytes_read + buffer_fill < byte_xSize);
					
					if (bytes_read >= 0) {
						bytes_read += buffer_fill;
					}
				}catch(IOException e){
					throw new WarningException("I/O file reading error.");
				}
				
				if(bytes_read < 0) {
					throw new WarningException("File reading error (end of file reached before the full file has been read).");
				}
				
				if(bytes_read != byte_xSize){
					throw new WarningException("File reading error (" + bytes_read + " bytes read, but " + byte_xSize + " needed).");
				}
				
				switch(t){
				case 0: //boolean (1 byte)
					for(int x = 0; x < xSize; x++){
						imageSamples[z][y][x] = buffer.get(x) == 0 ? 0.0F : 1.0F;
					}
					break;
				case 1: //unsigned int (1 byte)
					for(int x = 0; x < xSize; x++){
						imageSamples[z][y][x] = buffer.get(x) & 0xff;
					}
					break;
				case 2: //unsigned int (2 bytes)
					CharBuffer cb = buffer.asCharBuffer();
					for(int x = 0; x < xSize; x++){
						imageSamples[z][y][x] = cb.get(x) & 0xffff;
					}
					break;
				case 3: //signed short (2 bytes)
					ShortBuffer sb = buffer.asShortBuffer();
					for(int x = 0; x < xSize; x++){
						imageSamples[z][y][x] = sb.get(x);
					}
					break;
				case 4: //signed int (4 bytes)
					IntBuffer ib = buffer.asIntBuffer();
					for(int x = 0; x < xSize; x++){
						imageSamples[z][y][x] = ib.get(x);
					}
					break;
				case 5: //signed long (8 bytes)
					LongBuffer lb = buffer.asLongBuffer();
					for(int x = 0; x < xSize; x++){
						imageSamples[z][y][x] = lb.get(x);
					}
					break;
				case 6: //float (4 bytes)
					buffer.asFloatBuffer().get(imageSamples[z][y]);
					break;
				case 7: //double (8 bytes) - lost of precision
					DoubleBuffer db = buffer.asDoubleBuffer();
					for(int x = 0; x < xSize; x++){
						imageSamples[z][y][x] = (float) db.get(x);
					}
					break;
				}
			}
		}

		//Close .raw file
		try{
			fis.close();
		}catch(IOException e){
			throw new WarningException("Error closing file \"" + imageFile + "\".");
		}
	}

	/**
	 * Returns the samples of the image.
	 *
	 * @return a 3D float array that contains image samples
	 */
	public float[][][] getImage(){
		return(imageSamples);
	}

	/**
	 * Returns the type of the image components.
	 *
	 * @return a Class array with the type of each component
	 */
	public Class[] getTypes(){
		return(samplesType);
	}

	/**
	 * Returns if the 3 first components are RGB.
	 *
	 * @return true if 3 first components are RGB and false otherwise
	 */
	public boolean getRGBComponents(){
		return(RGBComponents);
	}

	/**
	 * Indicates the bit depth of the loaded image.
	 *
	 * @return an integer array containing the bit depth of each component
	 */
	public int[] getPixelBitDepth(){
		int[] pixelBitDepth = new int[zSize];
		for(int z = 0; z < zSize; z++){
			//int bitDepth=0;
			if(samplesType[z] == Byte.TYPE)           pixelBitDepth[z] = 8;
			else if(samplesType[z] == Character.TYPE) pixelBitDepth[z] = 16;
			else if(samplesType[z] == Short.TYPE)     pixelBitDepth[z] = 16;
			else if(samplesType[z] == Integer.TYPE)   pixelBitDepth[z] = 32;
			else if(samplesType[z] == Long.TYPE)      pixelBitDepth[z] = 64;
			else if(samplesType[z] == Float.TYPE)     pixelBitDepth[z] = 32;
			else if(samplesType[z] == Double.TYPE)    pixelBitDepth[z] = 64;
		}
		return(pixelBitDepth);
	}

	/**
	 * Indicates if the loaded image has signed pixels.
	 *
	 * @return an integer array that indicates if the channel if signed
	 */	
	public void setSignedPixels(int sampleType,int zSize){
		signedPixels = new int[zSize];
		for(int z = 0; z < zSize; z++){
			if(sampleType == 3 || sampleType == 4 || sampleType == 5 || 
					sampleType == 6 || sampleType == 7 ){
				signedPixels[z] = 1;
			} else {
				signedPixels[z] = 0;
			}
		}
	}
	
	/**
	 * Indicates if the loaded image has signed pixels.
	 *
	 * @return an integer array that indicates if the channel if signed
	 */	
	public int[] getSignedPixels(){
		return signedPixels;
	}
	
	/**
	 * Assign a number to a data type.
	 *
	 * @param type an string that represents the data type (byte, boolean, etc.)
	 * @return an integer that represents a data type
	 */
	static public int getType(String type){
		int typeNum = -1;
		if(type.compareTo("boolean") == 0)     typeNum = 0;
		else if(type.compareTo("byte") == 0)   typeNum = 1;
		else if(type.compareTo("char") == 0)   typeNum = 2;
		else if(type.compareTo("short") == 0)  typeNum = 3;
		else if(type.compareTo("int") == 0)    typeNum = 4;
		else if(type.compareTo("long") == 0)   typeNum = 5;
		else if(type.compareTo("float") == 0)  typeNum = 6;
		else if(type.compareTo("double") == 0) typeNum = 7;
		return(typeNum);
	}

	/**
	 * Assign a class to a number data type.
	 *
	 * @param dataType a number representing data type according to function getType
	 * @return the class represented by the number data type
	 */
	static public Class getClass(int dataType){
		Class typeClass = null;
		switch(dataType){
		case 0: //boolean
			typeClass = Boolean.TYPE;
			break;
		case 1: //byte
			typeClass = Byte.TYPE;
			break;
		case 2: //char
			typeClass = Character.TYPE;
			break;
		case 3: //short
			typeClass = Short.TYPE;
			break;
		case 4: //int
			typeClass = Integer.TYPE;
			break;
		case 5: //long
			typeClass = Long.TYPE;
			break;
		case 6: //float
			typeClass = Float.TYPE;
			break;
		case 7: //double
			typeClass = Double.TYPE;
			break;
		}
		return(typeClass);
	}

}
