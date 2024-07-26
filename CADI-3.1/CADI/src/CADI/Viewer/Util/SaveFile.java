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
package CADI.Viewer.Util;
import GiciException.*;

import java.io.*;
import java.nio.*;
import javax.media.jai.*;
import java.awt.image.*;
import java.awt.*;


/**
 * This class receives a 3D array and saves in any JAI format or raw data.<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public class SaveFile{

	/**
	 * This functions saves the image samples to an image file with the format specified by its extension.
	 *
	 * @param imageSamples the image samples
	 * @param QComponentsBits the bit depth of each image component
	 * @param imageFile the name file
	 * @param imageGeometry image geometry needed in the case that the output file is raw or img
	 *
	 * @throws WarningException when the file can not be saved
	 */
	public static void SaveFileExtension(float[][][] imageSamples, int[] QComponentsBits, String imageFile, int[] imageGeometry) throws WarningException{
		//Extract file extension
		String extension = "";
		int dotPos = imageFile.lastIndexOf(".");
		if(dotPos >= 0){
			extension = imageFile.substring(imageFile.lastIndexOf(".") + 1, imageFile.length());
		}else{
			throw new WarningException("The file does not have any extension.");
		}

		//Set file type
		int fileFormat = -1;
		if(extension.compareToIgnoreCase("pnm") == 0)       fileFormat = 0;
		else if(extension.compareToIgnoreCase("pgm") == 0)  fileFormat = 0;
		else if(extension.compareToIgnoreCase("ppm") == 0)  fileFormat = 0;
		else if(extension.compareToIgnoreCase("tiff") == 0) fileFormat = 1;
		else if(extension.compareToIgnoreCase("tif") == 0)  fileFormat = 1;
		else if(extension.compareToIgnoreCase("png") == 0)  fileFormat = 2;
		else if(extension.compareToIgnoreCase("jpg") == 0)  fileFormat = 3;
		else if(extension.compareToIgnoreCase("jpeg") == 0) fileFormat = 3;
		else if(extension.compareToIgnoreCase("bmp") == 0)  fileFormat = 4;
		else if(extension.compareToIgnoreCase("raw") == 0)  fileFormat = -1;
		else if(extension.compareToIgnoreCase("img") == 0)  fileFormat = -1;

		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;
		float[][][] imageSamplesTMP = new float[zSize][ySize][xSize];
		if((fileFormat >= 0) && (fileFormat <= 4)){
			//Chech the sample bit depth
			for(int z = 0; z < zSize; z++){
				if(QComponentsBits[z] > 8){
					throw new WarningException("This format only allow a bit depth <= 8 bits.");
				}
			}
			//Check the number of components
			if((zSize != 1) && (zSize != 3)){
				throw new WarningException("This format only allow 1 or 3 image components.");
			}

			//Rounding
			for(int z = 0; z < imageSamples.length; z++){
			for(int y = 0; y < imageSamples[z].length; y++){
			for(int x = 0; x < imageSamples[z][y].length; x++){
				imageSamplesTMP[z][y][x] = (float) Math.round(imageSamples[z][y][x]);
			}}}
			//Save
			SaveFileFormat(imageSamplesTMP, imageFile, fileFormat);
		}else{
			//Integer representations
			if(imageGeometry[0] < 6){
				zSize = imageSamples.length;
				ySize = imageSamples[0].length;
				xSize = imageSamples[0][0].length;
				imageSamplesTMP = new float[zSize][ySize][xSize];
				for(int z = 0; z < imageSamples.length; z++){
				for(int y = 0; y < imageSamples[z].length; y++){
				for(int x = 0; x < imageSamples[z][y].length; x++){
					imageSamplesTMP[z][y][x] = (float) Math.round(imageSamples[z][y][x]);
				}}}
				SaveFileRaw(imageSamplesTMP, imageFile, imageGeometry[0], imageGeometry[1]);
			}
			SaveFileRaw(imageSamples, imageFile, imageGeometry[0], imageGeometry[1]);
		}
		imageSamplesTMP = null;
	}

	/**
	 * Saves image samples using specified format using JAI. This function uses byte type to save image (because is the only allowed by JAI libraries) and it can manage only images with 1 or 3 components.
	 *
	 * @param imageSamples a 3D float array that contains image samples
	 * @param imageFile file name where raw data will be stored
	 * @param format format type to save image. The value indicates the following:<br>
	 *        <ul>
	 *          <li> 0- PNM (pgm or ppm)
	 *          <li> 1- TIFF
	 *          <li> 2- PNG
	 *          <li> 3- JPEG (not recommended because it degenerates image)
	 *          <li> 4- BMP (not recommended because it is not standard)
	 *        </ul>
	 *
	 * @throws WarningException when the file cannot be saved (incorrect number of components, file format unrecognized, etc.)
	 */
	private static void SaveFileFormat(float[][][] imageSamples, String imageFile, int format) throws WarningException{
		//Image sizes
		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;
		
		//Construct some needed classes
		SampleModel sm = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_FLOAT, xSize, ySize, zSize);
		float[][] imageBanks = new float[zSize][ySize*xSize];
		for(int z = 0; z < zSize; z++){
		for(int y = 0; y < ySize; y++){
		for(int x = 0; x < xSize; x++){
			imageBanks[z][(y*xSize) + x] = imageSamples[z][y][x];
		}}}
		java.awt.image.DataBufferFloat dbf = new java.awt.image.DataBufferFloat(imageBanks, ySize*xSize);
		Raster r = RasterFactory.createRaster(sm, dbf, new Point(0,0));

		//Construct buffered image
		BufferedImage buffImage = null;
		switch(zSize){
		case 1:
			buffImage = new BufferedImage(xSize, ySize, BufferedImage.TYPE_BYTE_GRAY);
			break;
		case 3:
			buffImage = new BufferedImage(xSize, ySize, BufferedImage.TYPE_3BYTE_BGR);
			break;
		default:
			throw new WarningException("This format only supports 1 or 3 components.");
		}
		buffImage.setData(r);

		//File save with JAI
		switch(format){
		case 0: //PNM
			JAI.create("filestore", buffImage, imageFile, "PNM");
			break;
		case 1: //TIFF
			JAI.create("filestore", buffImage, imageFile, "TIFF");
			break;
		case 2: //PNG
			JAI.create("filestore", buffImage, imageFile, "PNG");
			break;
		case 3: //JPEG
			JAI.create("filestore", buffImage, imageFile, "JPEG");
			break;
		case 4: //BMP
			JAI.create("filestore", buffImage, imageFile, "BMP");
			break;
		default:
			throw new WarningException("Format file unrecognized.");
		}

		//Free memory
		sm = null;
		imageBanks = null;
		dbf = null;
		buffImage = null;
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
	private static void SaveFileRaw(float[][][] imageSamples, String imageFile, int sampleType, int byteOrder) throws WarningException{
		//Image sizes
		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;

		//Open file
		File newFile = new File(imageFile);
		FileOutputStream fos = null;
		try{
			if(newFile.exists()){
				newFile.delete();
				newFile.createNewFile();
			}
			fos = new FileOutputStream(newFile);
		}catch(FileNotFoundException e){
			throw new WarningException("File \"" + imageFile + "\" can not be open.");
		}catch(IOException e){
			throw new WarningException("I/O error saving file \"" + imageFile + "\".");
		}

		DataOutputStream dos = new DataOutputStream(fos);

		//Buffer to perform data conversion
		ByteBuffer buffer;
		//Line size in bytes
		int byte_xSize;
		//Set correct line size
		switch(sampleType){
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

		//Save image
		//Further speed improvements can be achieved in the worst case where image width is little by fixing a min read size and not reading less than it
		for(int z = 0; z < zSize; z++){
			for(int y = 0; y < ySize; y++){

				switch(sampleType){
				case 0: //boolean (1 byte)
					for(int x = 0; x < xSize; x++){
						buffer.put(x, (byte)(imageSamples[z][y][x] == 0 ? 0 : 1));
					}
					break;
				case 1: //unsigned int (1 byte)
					for(int x = 0; x < xSize; x++){
						buffer.put(x, (byte)imageSamples[z][y][x]);
					}
					break;
				case 2: //unsigned int (2 bytes)
					CharBuffer cb = buffer.asCharBuffer();
					for(int x = 0; x < xSize; x++){
						cb.put(x, (char)imageSamples[z][y][x]);
					}
					break;
				case 3: //signed short (2 bytes)
					ShortBuffer sb = buffer.asShortBuffer();
					for(int x = 0; x < xSize; x++){
						sb.put(x, (short)imageSamples[z][y][x]);
					}
					break;
				case 4: //signed int (4 bytes)
					IntBuffer ib = buffer.asIntBuffer();
					for(int x = 0; x < xSize; x++){
						ib.put(x, (int)imageSamples[z][y][x]);
					}
					break;
				case 5: //signed long (8 bytes)
					LongBuffer lb = buffer.asLongBuffer();
					for(int x = 0; x < xSize; x++){
						lb.put(x, (long)imageSamples[z][y][x]);
					}
					break;
				case 6: //float (4 bytes)
					buffer.asFloatBuffer().put(imageSamples[z][y]);
					break;
				case 7: //double (8 bytes) - lost of precision
					DoubleBuffer db = buffer.asDoubleBuffer();
					for(int x = 0; x < xSize; x++){
						db.put(x, (double)imageSamples[z][y][x]);
					}
					break;
				}

				try{
					dos.write(buffer.array(), 0, byte_xSize);
				}catch(IOException e){
					throw new WarningException("I/O file writing error.");
				}

			}
		}

		//Close file
		try{
			fos.close();
		}catch(IOException e){
			throw new WarningException("Error closing file \"" + imageFile + "\".");
		}
	}

}
