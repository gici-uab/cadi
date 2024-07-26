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

import CADI.Common.LogicalTarget.JPEG2000.JPCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Codestream.JPCMainHeaderDecoder;
import CADI.Server.LogicalTarget.JPEG2000.BoxIndexing;
import GiciException.ErrorException;
import GiciStream.BufferedDataInputStream;

/**
 * This class reads a JP2 file.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1 2008/10/07
 */
public class ReadJP2File {
		
	/**
	 * Is an object with the input stream where the main header is read from.
	 */
	private BufferedDataInputStream in = null;

	/**
	 * Is an object where the main header are saved.
	 */
	private JPCParameters jpcParameters = null;
	
	/**
	 * Is the file pointer to the first byte of the main header.
	 */
	private long mainHeaderInitialPos = 0;
	
	/**
	 * Is the length of the main header.
	 */
	private int mainHeaderLength = 0;
	
	/**
	 * Definition in {@link CADI.Server.LogicalTarget.JPEG2000.JP2LogicalTarget#fileFormatType}
	 */
	private int fileFormat = -1;
	
  private BoxIndexing fileIndexing = null;
	
	// INTERNAL ATTRIBUTES
	
	/**
	 * Indicates whether the JP2 header box has been found.
	 */
	private boolean jp2HeaderBoxFound = false;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param in input stream where data will be read from.
	 */
	public ReadJP2File(BufferedDataInputStream in, BoxIndexing fileIndexing) {
		
		if (in == null) throw new NullPointerException();
    if (fileIndexing == null) throw new NullPointerException();
		
		this.in = in;
    this.fileIndexing = fileIndexing;
		
		jpcParameters = new JPCParameters();
	}
	
	/**
	 * Reads the boxes of the JP2 headers and checks their conformity.
	 *
	 * @throws ErrorException when the header information is ambiguous or incorrect
	 */
	public void run() throws ErrorException, EOFException, IOException {
    
		long length = -1;
		long contentBoxLength = -1;
		int type = -1;
    BoxIndexing boxIndexing = null;
    
		// READ OPTIONAL BOXES
		boolean finish = false;			
		while ( !finish ) {
      
      long pos = in.getPos();
      
			// Length
			length = in.readInt();
			contentBoxLength = 0;

			// Type
			type = in.readInt();

			// Extended length
			if (length == 1) {
				length = in.readLong();
				contentBoxLength = length - 16;
			} else {
				contentBoxLength = length - 8; 
			}
      
      boxIndexing = new BoxIndexing(fileIndexing, type, pos, length, (type == JP2BoxTypes.JP2_HEADER_BOX_TYPE ? true : false));
      fileIndexing.addChild(boxIndexing);
      
			switch (type) {

				case JP2BoxTypes.JP2_HEADER_BOX_TYPE: // JP2 Header SuperBox
					if ( jp2HeaderBoxFound )
						throw new ErrorException("JPEG2000 Signature Box does not conform the standard.");
					jp2HeaderBoxFound = true;
					ReadJP2HeaderBox readJP2HeaderBox = new ReadJP2HeaderBox(in, contentBoxLength, boxIndexing);
					readJP2HeaderBox.run();
					break;

				case JP2BoxTypes.CONTIGUOUS_CODESTREAM_BOX_TYPE: //Reads Contiguous Codestream Box (only length and type; content is J2C)
					if ( !jp2HeaderBoxFound )
						throw new ErrorException("JPEG2000 Signature Box does not conform the standard.");
					readContiguousCodestreamBox(contentBoxLength);
					break;

				case JP2BoxTypes.INTELLECTUAL_PROPERTY_BOX_TYPE:
					readIntPropertyBox(contentBoxLength);
					break;

				case JP2BoxTypes.XML_BOX_TYPE:
					readXMLBox(contentBoxLength);
					break;

				case JP2BoxTypes.UUID_BOX_TYPE:
					readUUIDBox(contentBoxLength);
					break;

				case JP2BoxTypes.UUID_INFO_BOX_TYPE:
					readUUIDInfoBox(contentBoxLength);
					break;	

				default:
					// Skip unknown box
					in.skipBytes(contentBoxLength);
				//throw new ErrorException("JPEG2000 Signature Box does not conform the standard.");
			}

			// Finish conditon: end of file has been reached, or last box length is 0
			if ( (length == 0) || (in.available() == 0) ) {
				finish = true;
			}
		}
    
	}
		
	/**
	 * Returns the {@link #fileFormat} attribute.
	 * 
	 * @return the {@link #fileFormat} attribute.
	 */
	public int getFileFormat() {
		return fileFormat;
	}
		
	/**
	 * Returns the {@link #jpcParameters} attribute.
	 * 
	 * @return the {@link #jpcParameters} attribute.
	 */
	public JPCParameters getJPCParameters() {
		return jpcParameters;
	}
  
	/**
	 * Returns the {@link #mainHeaderInitialPos} attribute.
	 *  
	 * @return the {@link #mainHeaderInitialPos} attribute.
	 */
	public long getMainHeaderInitialPos() {
		return mainHeaderInitialPos;
	}
	
	/**
	 * Returns the {@link #mainHeaderLength} attribute.
	 * 
	 * @return the {@link #mainHeaderLength} attribute.
	 */
	public int getMainHeaderLength() {
		return mainHeaderLength;
	}
	
	// ============================ private methods ==============================	
	/**
	 * Reads the Contiguous Codestream Box and checks its conformity.
	 *
	 * @throws ErrorException when the header information is ambiguous or incorrect
	 */
	private void readContiguousCodestreamBox(long length) throws ErrorException{
		
		JPCMainHeaderDecoder jpcDeheading = new JPCMainHeaderDecoder(in);
		
		try {
			jpcDeheading.run();
		} catch (ErrorException e) {
			throw new ErrorException("JPC main header can not be readed or decoded correctly");
		}
		
		jpcParameters = jpcDeheading.getJPCParameters();
		mainHeaderInitialPos = jpcDeheading.getMainHeaderInitialPos();
		mainHeaderLength = jpcDeheading.getMainHeaderLength();
		
	 }

	/**
	 * 
	 * @param length
	 * @throws IOException
	 */
	private void readIntPropertyBox(long length) throws IOException {
		in.skipBytes(length);
	}
	
	/**
	 * 
	 * @param length
	 * @throws IOException
	 */
	private void readXMLBox(long length) throws IOException {
		in.skipBytes(length);
	}
	
	/**
	 * 
	 * @param length
	 * @throws IOException
	 */
	private void readUUIDBox(long length) throws IOException {
		in.skipBytes(length);
	}
	
	/**
	 * 
	 * @param length
	 * @throws IOException
	 */
	private void readUUIDInfoBox(long length) throws IOException {
		in.skipBytes(length);
	}
	
}