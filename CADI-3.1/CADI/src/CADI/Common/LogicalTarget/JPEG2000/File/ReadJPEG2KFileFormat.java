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
package CADI.Common.LogicalTarget.JPEG2000.File;

import java.io.EOFException;
import java.io.IOException;

import CADI.Common.LogicalTarget.JPEG2000.Codestream.JPCMarkers;
import CADI.Server.LogicalTarget.JPEG2000.BoxIndexing;
import GiciException.ErrorException;
import GiciStream.BufferedDataInputStream;

/**
 * This class is only used to read which is the JPEG2000 file format (jpc, jp2,
 * jpx, or jpk).
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/03/06
 */
public class ReadJPEG2KFileFormat {

	/**
	 * Is an object with the input stream where the main header is read from.
	 */
	private BufferedDataInputStream in = null;
  
  private BoxIndexing fileIndexing = null;
	
	/**
	 * Allowed values are:
   * {@link #FILE_FORMAT_JPC}, {@link #FILE_FORMAT_JP2},
   * {@link #FILE_FORMAT_JPX}, and {@link #FILE_FORMAT_JPK}
	 */
	private int fileFormat = -1;
	public static final int FILE_FORMAT_JPC = 1;
  public static final int FILE_FORMAT_JP2 = 2;
  public static final int FILE_FORMAT_JPX = 4;
  public static final int FILE_FORMAT_JPK = Integer.MAX_VALUE;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param in input stream where data will be read from.
	 */
	public ReadJPEG2KFileFormat(BufferedDataInputStream in, BoxIndexing fileIndexing) {
		if (in == null) throw new NullPointerException();
    if (fileIndexing == null) throw new NullPointerException();
		
		this.in = in;
    this.fileIndexing = fileIndexing;
	}
	
	/**
	 * 
	 * @return the {@link #fileFormat} attribute.
	 * 
	 * @throws IOException 
	 * @throws ErrorException 
	 * @throws EOFException 
	 */
	public int run() throws EOFException, ErrorException, IOException {
		
    BoxIndexing boxIndexing = null;
		long filePointer = in.getPos();
		
		// JPEG2000 SIGNATURE BOX ?
		
		// If the JPEG2000 Signature Box is found, it is a JP2 file format.
		if ( readSignatureBox() ) {
			
      // Signature box indexing info
      boxIndexing = new BoxIndexing(fileIndexing, JPXBoxTypes.SIGNATURE_BOX_TYPE, filePointer, in.getPos() - filePointer, false);
      fileIndexing.addChild(boxIndexing);
      
			// READ FILE TYPE BOX

      // Signature box indexing info
      filePointer = in.getPos();
      
      
			// Box length
			long length = in.readInt();
			long contentBoxLength = 0;

			// Box type
			int type = in.readInt();
			if (type != JPXBoxTypes.FILE_TYPE_BOX_TYPE) 
				throw new ErrorException("JPEG2000 File Type Box does not conform the standard.");

			// Extended length
			if (length == 1) {
				length = in.readLong();
				contentBoxLength = length - 16;
			} else {
				contentBoxLength = length - 8; 
			}
      boxIndexing = new BoxIndexing(fileIndexing, JPXBoxTypes.FILE_TYPE_BOX_TYPE, filePointer, length, false);
      fileIndexing.addChild(boxIndexing);

			// Box content
			readFileTypeBox(contentBoxLength);

		} else  { // JPC file format: 0xFF4F (SOC)
			// Rewind to the initial position the file pointer
			in.seek(filePointer);
			
			// If the Start Of Code marker is found, it is a JPC codestream
			if (in.readShort() == JPCMarkers.SOC) {
				fileFormat = FILE_FORMAT_JPC;
				
			} else {
				fileFormat = FILE_FORMAT_JPK;
				
			}
			
			// Rewind 2 bytes the file pointer
			in.seek(in.getPos()-2);
		}
    
		return fileFormat;
	}
	
	/**
	 * Returns the {@link #fileFormat} attribute.
	 * 
	 * @return the {@link #fileFormat} attribute.
	 */
	public int getFileFormat() {
		return fileFormat;
	}
	
  // ============================ private methods ==============================
	/**
	 * Reads the Signature Box and checks its conformity.
	 *
	 * @throws ErrorException when the header information is ambiguous or incorrect
	 */
	private boolean readSignatureBox() throws ErrorException, EOFException, IOException {
		
		// Box length
		int length = in.readInt();
		if (length != 12) {
			return false;
		}

		// Box type
		int boxType = in.readInt();
		if (boxType != JPXBoxTypes.SIGNATURE_BOX_TYPE) {
			return false;
		}

		// Box content
		int boxContent = in.readInt();
		if (boxContent != JPXBoxTypes.SIGNATURE_BOX_CONTENT) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Reads the File Type Box and checks its conformity.
	 * 
	 * @param length
	 *
	 * @throws ErrorException when the header information is ambiguous or incorrect
	 */
	private void readFileTypeBox(long length) throws ErrorException, EOFException, IOException {
		
		// BOX CONTENT
		// Brand
		int brand = in.readInt();
		if (brand == JPXBoxTypes.FILE_TYPE_BOX_BRAND_JP2) {
			fileFormat = FILE_FORMAT_JP2;
		} else if (brand == JPXBoxTypes.FILE_TYPE_BOX_BRAND_JPX) {
			fileFormat = FILE_FORMAT_JPX;
		} else {
			throw new ErrorException("JPEG2000 Signature Box does not conform the standard.");
		}
		
		// Minor version
		int minorVersion = in.readInt();
		if ( minorVersion != 0){
			throw new ErrorException("JPEG2000 Signature Box does not conform the standard.");
		}
		
		// Compatibility list
		length -= 8; // Length compatibility list = length - (4 bytes from BR) - (4 bytes from MinV)
		if ((length % 4) != 0) throw new ErrorException("Illegal length of number of compatiblity list"); 
		int numCompatibilityList = (int)(length >>> 2);
		
		int[] compatibilityList = new int[numCompatibilityList];
		for (int i = 0; i < numCompatibilityList; i++){
			compatibilityList[i] = in.readInt();
		}
		
		boolean compatibility = false;
		for (int i = 0; i < numCompatibilityList; i++){
			if ( (compatibilityList[i] == JPXBoxTypes.FILE_TYPE_BOX_BRAND_JP2)
				|| (compatibilityList[i] == JPXBoxTypes.FILE_TYPE_BOX_BRAND_JPX) ) {
				compatibility = true;
			}
		}
		compatibilityList = null;
		
		if ( !compatibility ) {
			throw new ErrorException("File Type Box does not conform the standard.");
		}
	}
}