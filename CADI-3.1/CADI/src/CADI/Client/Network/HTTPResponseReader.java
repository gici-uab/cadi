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
package CADI.Client.Network;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ProtocolException;

/**
 * This class provides a wrapper for abstracting the reading of the HTTP
 * response. It provides functions to read the HTTP response header and to
 * read the HTTP response body as a byte array (abstracting the underlying
 * response body encoding).
 * <p>
 * NOTICE: when the HTTP keep alive is set, the input stream is not closed
 * for all http request and responses. So, after reading an http response, the
 * end of input stream is reached (see {@link #endOfStream}), and it is only
 * disable for reading the next response if the {@link #empty()} method has
 * been called before the HTTP request has been sent. 
 * <p>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; readLine<br>
 * &nbsp; ....<br>
 * &nbsp; readLine<br>
 * &nbsp; read or readFully<br> 
 * &nbsp; ....<br>
 * &nbsp; read or readFully<br>
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0   2007-2012/10/27
 */
public class HTTPResponseReader implements JPIPMessageReader {

	/**
	 * The input stream where data are read from.
	 */
	private InputStream inputStream = null;

	/**
	 * Indicates the transfer encoding mode which is used to delivery the HTTP
	 * reesponse body.
	 */
	private int transferEncoding = TRANSFER_ENCODING_NONE;

	/**
	 * Allowed values for transferEncoding value.
	 */
	public static final int TRANSFER_ENCODING_NONE = 0;
	public static final int TRANSFER_ENCODING_CHUNKED = 1;
	public static final int TRANSFER_ENCODING_CONTENT_LENGTH = 2;
	
	/**
	 * 
	 */
	private int contentLength = 0;

	
	// INTERNAL ATTRIBUTES

	/**
	 * An array of bytes used to buffer the input data. Elements <code>buf[0]
	 * </code> through <code>buf[count-1]</code> are the only bytes that can
	 * ever be read from the buffer;  element <code>buf[pos]</code> is the next
	 * byte to be read/write.
	 */
	private byte buf[] = null;

	/**
	 * The index of the next byte to read from the buffer. This value should
	 * always be nonnegative and not larger than the value of <code>count
	 * </code>. The next byte to be read from the buffer will be <code>buf[pos]
	 * </code>.
	 */
	private int pos;

	/**
	 * The index one greater than the last valid byte in the buffer.  This value
	 * should always be nonnegative and not larger than the length of <code>buf
	 * </code>. It  is one greater than the position of the last byte within
	 * <code>buf</code> that can ever be read from the buffer.
	 */
	private int count;

	/**
	 * This attribute indicates whether the end of stream has been reached.
	 */
	private boolean endOfStream = false;
	
	/**
	 * 
	 */
	private int chunkSize = -1;
	
	/**
	 * 
	 */
	private int tempByte = 0;
	
	/**
	 * 
	 */
	private boolean rewind = false;
	
	/**
	 * It is the carriage return constant
	 */
	private static final int CR = 13;
	
	/**
	 * It is the line feed constant
	 */
	private static final int LF = 10;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public HTTPResponseReader() {
	}
	
	/**
	 * Sets the input parameters. 
	 * 
	 * @param inputStream definition in {@link #inputStream}
	 */
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
		pos = 0;
		count = 0;
		endOfStream = false;
		chunkSize = -1;
		tempByte = 0;
		rewind = false;
	}
	
	/**
	 * Sets the transfer encoding type of the http message body. 
	 *  
	 * @param transferEncoding	definition in {@link #transferEncoding}
	 * @param contentLength		definition in {@link #contentLength}
	 */
	public void setTransferEncoding(String transferEncoding, String contentLength) throws ProtocolException {

		if ( (transferEncoding != null) && (contentLength != null) ) {
			throw new ProtocolException("HTTP entity header has two incompatible headers: Transfer-Encoding and Content-Length)");
		}
		
		this.transferEncoding = TRANSFER_ENCODING_NONE;
		
		if (transferEncoding != null) {
			if ( transferEncoding.equals("chunked")) {
				this.transferEncoding = TRANSFER_ENCODING_CHUNKED;
				
			} else {
				throw new ProtocolException("Unkown entity header value: Transfer-Encoding: " + transferEncoding);
			}
			
		} else if (contentLength != null){
			this.contentLength = Integer.parseInt(contentLength);
			
			if (this.contentLength <= 0) {				
				throw new ProtocolException("Unkown entity header value: Content-Length: " + contentLength);
			}
			
			this.transferEncoding = TRANSFER_ENCODING_CONTENT_LENGTH;
		}
		
		pos = 0;
		count = 0;		
	}
		
	/**
	 * Read a line of text.  A line is considered to be terminated by any one
	 * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
	 * followed immediately by a linefeed.
	 * 
	 * @return A String containing the contents of the line, not including
	 * 			any line-termination characters, or null if the end of the
	 * 			stream has been reached.
	 * @throws IOException if an I/O error occurs
	 */
	public String readLine() throws EOFException, IOException {
		
		String line = null;
		int character = -1;
						
		if ( endOfStream ) {
			return line;
		}
		
		character = read1();
		if (character == -1) {
			endOfStream = true;
		
		} else {
			StringBuffer stringBuffer = new StringBuffer();
			boolean finish = false;
			
			while ( !finish ) {			
				
				if (character == -1) {				
					endOfStream = true;
					finish = true;
		
				} else if (character == CR) {
					character = read1();
				
					if (character == -1) {					
						endOfStream = true;
					
					} else if(character != LF) {
						tempByte = character;
						rewind = true;
					}
					finish = true;
				
				} else if (character == LF) {
					finish = true;
				
				} else {
					stringBuffer.append((char)character);
					character = read1();
				}
				
				
			}
			line = stringBuffer.toString();
		}
		
		return line;
	}
	
	/**
	 * Reads the next byte of data from the input stream. The value byte is
	 * returned as an <code>int</code> in the range <code>0</code> to
	 * <code>255</code>. If no byte is available because the end of the stream
	 * has been reached, an IOException is thrown. This method
	 * blocks until input data is availableor an exception is thrown.
	 *
	 * <p> A subclass must provide an implementation of this method.
	 *
	 * @return     the next byte of data, or <code>-1</code> if the end of the
	 *             stream is reached.
	 * @throws  IOException	if an I/O error occurs or the input stream reaches
	 * 							the end before reading all the bytes.
	 */
	public int read() throws EOFException, IOException {
		if (pos >= count) {
			readChunk();
		}		
		return buf[pos++] & 0xFF;
	}
	
	/**
	 * Reads a byte array form the input stream. The data read is returned in
	 * the byte array that is passed as the first parameters. Memory of the
	 * byte array where data will be written must be reserved before this
	 * function is called. Otherwise, an exception is thrown.
	 *
	 * @param      b     the buffer into which the data is read.
	 * @param      off   the start offset of the data.
	 * @param      len   the number of bytes to read. 
	 * @throws  EOFException  if this input stream reaches the end before
	 *               reading all the bytes.
	 * @throws  IOException   if an I/O error occurs.
	 */
	public void readFully(byte[] b, int off, int len) throws EOFException, IOException {	

		// Checks
		if (b == null) {
			throw new NullPointerException();
		} else {
			if ((off < 0) || (off > b.length) || (len < 0) ||
					((off + len) > b.length) || ((off + len) < 0)) {
				throw new IndexOutOfBoundsException();
			} 
		}
		
		// Read data
		for (int tempCount = 0; len > 0; ) {
			if (pos < count) {				
				tempCount = Math.min(count-pos, len);
				System.arraycopy(buf, pos, b, off, tempCount);
				pos += tempCount;
				off += tempCount;
				len -= tempCount;				
			} else {					
				readChunk();					
			}
		}		
	}	
		
	/**
	 * This method reads data until there is not data in the input stream.
	 * <p> 
	 * It is useful when the HTTP keep alive feature is used. The input stream
	 * will be used for all connections, so data of the last connection that has
	 * not been read must be read before the beginning of the new connection.
	 */
	public void empty() {
		if (inputStream != null) {
			try {
				inputStream.skip(inputStream.available());
			} catch (IOException e) {}
		}
		pos = 0;
		count = 0;
		chunkSize = -1;
		endOfStream = false;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isEndOfStream() {
		return endOfStream;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";

		str += "<<< Not implemented yet >>> ";

		str += "]";
		return str;
	}
	
	/**
	 * Prints this HTTP Response Reader fields out to the specified output
	 * stream. This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- HTTP Response Reader --");		
		
		out.println("<<< Not implemented yet >>> ");
						
		out.flush();
	}
	
	// ============================ private methods ==============================
	/**
	 * Read a chunk of the http entity body. The data read is saved in the
	 * {@link #buf} attribute.
	 * 
	 * @throws EOFExcpetion if the end of the buffer has been reached.
	 * @throws IOException if an I/O error occurs
	 */
	private void readChunk() throws EOFException, IOException {
		
		if (endOfStream) {
			throw new EOFException();
		}
		
		pos = 0;
		switch(transferEncoding) {

			case TRANSFER_ENCODING_CHUNKED:					
				
				if (chunkSize < 0) {
					chunkSize = readChunkSize();
				}
				
				if (chunkSize < 0) {					
					throw new IOException();					
				} else {					
					buf = new byte[chunkSize];
					count = 0;
					while (count < chunkSize) {
						count += inputStream.read(buf, count, chunkSize-count);
					}
					readCRLF();					
				}		
				/*System.out.println("Chunk size: " + chunkSize);
				System.out.print("Chunk data: ");
				for (int i = 0; i < buf.length; i++) {
					if ((0xFF&buf[i]) < 16) {
						System.out.print("0");
					}
					System.out.print(Integer.toHexString(0xFF&buf[i]));
				}
				System.out.println();
				*/
				chunkSize = readChunkSize();
				if (chunkSize == 0) {					
					readTrailer();								
					endOfStream = true;
				}				
				
				break;

			case TRANSFER_ENCODING_CONTENT_LENGTH:
				
				endOfStream = true;

				if ( contentLength > 0 ) {										
					buf = new byte[contentLength];
					count = 0;			
					while (count < contentLength) {
						count += inputStream.read(buf, count, contentLength-count);
					}					
					readCRLF();
					readCRLF();
				}
						
				break;

			default:
				assert(true) : "Unknow mode for reading the http entity body";
		}
		
	}
	
	/**
	 * Reads the length of the http entity body chunk. The chunk extension of
	 * the chunk-size length is ignorated.
	 *  
	 * @return the length of the chunk.
	 * @throws IOException
	 */
	private int readChunkSize() throws IOException {
		int length = 0;
		
		String lineSize = readLine();
		
		int charPos = lineSize.indexOf(";");		
		if (charPos > 0) {
			lineSize = lineSize.substring(0, charPos);
		}		
		length = Integer.parseInt(lineSize, 16);
		
		return length;
	}
	
	/**
	 * Reads the CR-LF
	 * 
	 * @throws IOException
	 */
	private void readCRLF() throws IOException {
		
		int character = read1();
		
		if (character == -1) {
			endOfStream = true;
		} else if (character == CR) {
			character = read1();
			if (character == -1) {
				endOfStream = true;
			} else if (character != LF) {
				tempByte = character;
				rewind = true;
			}
		} else if (character == LF) {			
		} else {
			tempByte = character;
			rewind = true;
		}
	}
	
	/**
	 * Read the trailer of the http chunked-body
	 * @throws IOException
	 */
	private void readTrailer() throws IOException {
		while (readLine().length() != 0);		
	}
	
	/**
	 * Definition in {@link java.io.InputStream#read()}.
	 */
	private int read1() throws IOException {
		if (rewind) {
			rewind = false;
			return tempByte;
		} else {
			return inputStream.read();
		}
	}
	
}