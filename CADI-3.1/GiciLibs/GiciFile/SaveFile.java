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
import GiciFile.SaveRawFile;

import java.io.*;
import java.awt.image.*;


/**
 * This class receives a 3D array and saves in any JAI format or raw data.<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.02
 */
public class SaveFile{

	/**
	 * Saves image samples using specified format using JAI. This function uses byte type to save image (because is the only allowed by JAI libraries) and it can manage only images with 1 or 3 components. File extension is added automatically depending on the format.
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
	public static void SaveFileFormat(float[][][] imageSamples, String imageFile, int format) throws WarningException{
		//Image sizes
		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;

		//Construct some needed classes
		
		float[][] imageBanks = new float[zSize][ySize*xSize];
		for(int z = 0; z < zSize; z++){
			for(int y = 0; y < ySize; y++){
				for(int x = 0; x < xSize; x++){
					imageBanks[z][(y*xSize) + x] = imageSamples[z][y][x];
				}
			}
		}
		
		DataBufferFloat dbf = new DataBufferFloat(imageBanks, ySize*xSize);
		Raster r = JaiWrapper.createRaster(xSize, ySize, zSize, dbf);

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
			JaiWrapper.jaiSave(buffImage, imageFile, "PNM");
			break;
		case 1: //TIFF
			JaiWrapper.jaiSave(buffImage, imageFile, "TIFF");
			break;
		case 2: //PNG
			JaiWrapper.jaiSave(buffImage, imageFile, "PNG");
			break;
		case 3: //JPEG
			JaiWrapper.jaiSave(buffImage, imageFile, "JPEG");
			break;
		case 4: //BMP
			JaiWrapper.jaiSave(buffImage, imageFile, "BMP");
			break;
		default:
			throw new WarningException("Format file unrecognized.");
		}

		//Free memory
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
	public static void SaveFileRaw(float[][][] imageSamples, String imageFile, int sampleType, int byteOrder) throws WarningException{
		//Image sizes
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

		String extension = "";
		int dotPos = imageFile.lastIndexOf(".");
		
		if(dotPos >= 0){
			extension = imageFile.substring(imageFile.lastIndexOf(".") + 1, imageFile.length());
		} 

		if (extension.compareToIgnoreCase("pgm") == 0){
			String header = "P5\n"+String.valueOf(xSize)+" "+String.valueOf(ySize)+"\n";
			if (sampleType==1){
				header = header+"255\n";
			} else {
				header = header+"65535\n";
			}
			try{
				dos.writeBytes(header);
			}catch(IOException e){
				throw new WarningException("I/O error saving file \"" + imageFile + "\".");
			}
		}
		
		// Use SaveRawFile for such purpose
		try {
			SaveRawFile.saveRawData(imageSamples, dos, sampleType, byteOrder);
		} catch (IOException e1) {
			throw new WarningException("I/O error saving file \"" + imageFile + "\".");
		}

		//Close file
		try{
			dos.close();
			fos.close();
		}catch(IOException e){
			throw new WarningException("Error closing file \"" + imageFile + "\".");
		}
	}

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
			if (imageGeometry == null) {
				throw new WarningException("Trying to save to a raw file without specifying geometry.");
			}
			
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
	 * Saves image samples in the format given by the extension.
	 *
	 * @param imageSamples a 3D float array that contains image samples
	 * @param imageFile file name where raw data will be stored
	 * @param imageGeometry a 1D integer array that contains the format when the data is stored in raw data
	 *
	 * @throws WarningException when the file cannot be saved (incorrect number of components, file format unrecognized, etc.)
	 * @throws ParamterException when saving with raw mode and the imageGeometry parameter has some problems
	 */
	public static void SaveFileByExtension(float[][][] imageSamples, String imageFile, int[] imageGeometry) throws WarningException, ParameterException{
		int format = getFormat(imageFile);

		if((format >= 0) && (format <= 4)){
			if(imageGeometry != null){
				if((format == 0) && (imageGeometry[3] != 1)){
					throw new WarningException("PGM, PNM and PPM format only support 8 bits per pixel.");
				}
			}
			SaveFileFormat(imageSamples, imageFile, format);
		}else{
			if(format == 5){
				if(imageGeometry == null){
					throw new ParameterException("To store in raw format imageGeometry is mandatory.");
				}else{
					if(imageGeometry.length != 6){
						throw new ParameterException("Incorrect number of parameters in imageGeometry.");
					}
				}
				int sampleType = imageGeometry[3];
				int byteOrder = imageGeometry[4];
				SaveFileRaw(imageSamples, imageFile, sampleType, byteOrder);
			}else{
				throw new WarningException("The file does not have any known extension.");
			}
		}
	}

	/**
	 * Saves image samples in the format given by the extension.
	 *
	 * @param imageSamples a 3D float array that contains image samples
	 * @param imageFile file name that contains original data
	 * @param imageGeometry a 1D integer array that contains the format when the data is stored in raw data
	 * @param imageFile file name where raw data will be stored
	 *
	 * @throws WarningException when the file cannot be saved (incorrect number of components, file format unrecognized, etc.)
	 * @throws ParamterException when saving with raw mode and the imageGeometry parameter has some problems
	 */
	public static void SaveFileByExtensionAndName(float[][][] imageSamples, String imageFile, int[] imageGeometry, String outputImagefile) throws WarningException, ParameterException{
		int format = getFormat(imageFile);
		int []imageGeometrytmp = null;
		if(imageGeometry != null){
			imageGeometrytmp = new int[imageGeometry.length+1];
			for(int i = 0; i < imageGeometry.length; i++){
				imageGeometrytmp[i] = imageGeometry[i];
			}
			imageGeometrytmp[imageGeometry.length] = 0;
		}
		if((format >= 0) && (format <= 4)){
			if(imageGeometrytmp != null){
				if((format == 0) && (imageGeometrytmp[3] != 1)){
					throw new WarningException("PGM, PNM and PPM format only support 8 bits per pixel.");
				}
			}
			SaveFileFormat(imageSamples, outputImagefile, format);
		}else{
			if(format == 5){
				if(imageGeometrytmp == null){
					throw new ParameterException("To store in raw format imageGeometry is mandatory.");
				}else{
					if(imageGeometrytmp.length != 6){
						throw new ParameterException("Incorrect number of parameters in imageGeometry.");
					}
				}
				int sampleType = imageGeometrytmp[3];
				int byteOrder = imageGeometrytmp[4];
				SaveFileRaw(imageSamples, outputImagefile, sampleType, byteOrder);
			}else{
				throw new WarningException("The file does not have any known extension.");
			}
		}
	}
	
	/**
	 * Gets the extension, if any, of the image to be saved.
	 *
	 * @param imageFile contains the name of the image to be saved
	 * @return an integer that indicated containing the format
	 *
	 * @throws WarningException when the file has no extension
	 */
	public static int getFormat(String imageFile) throws WarningException{
		String extension = "";
		int dotPos = imageFile.lastIndexOf(".");
		int fileFormat = -1;

		if(dotPos >= 0){
			extension = imageFile.substring(imageFile.lastIndexOf(".") + 1, imageFile.length());
			if(extension.compareToIgnoreCase("pnm") == 0)       fileFormat = 0;
			else if(extension.compareToIgnoreCase("pgm") == 0)  fileFormat = 0;
			else if(extension.compareToIgnoreCase("ppm") == 0)  fileFormat = 0;
			else if(extension.compareToIgnoreCase("tiff") == 0) fileFormat = 1;
			else if(extension.compareToIgnoreCase("tif") == 0)  fileFormat = 1;
			else if(extension.compareToIgnoreCase("png") == 0)  fileFormat = 2;
			else if(extension.compareToIgnoreCase("jpg") == 0)  fileFormat = 3;
			else if(extension.compareToIgnoreCase("jpeg") == 0) fileFormat = 3;
			else if(extension.compareToIgnoreCase("bmp") == 0)  fileFormat = 4;
			else if(extension.compareToIgnoreCase("raw") == 0)  fileFormat = 5;
			else if(extension.compareToIgnoreCase("img") == 0)  fileFormat = 5;
		}else{
			throw new WarningException("The file does not have any extension.");
		}
		return(fileFormat);
	}

}
