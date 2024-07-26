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
package CADI.Common.io;

import java.io.DataOutput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;

/**
 * This class provides a wrapper for abstracting from the data destination, which can
 * be a file or a buffer. The data destination is choosed with the class's constructor.
 * If the data destination is a file, then, data from file are buffered for a better 
 * efficiency in writting.  
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version beta 0.1 2007-2012/10/26
 */
public class BufferedDataOutputStream implements DataOutput {

  /**
   * An array of bytes used to buffer the input/output data.
   * Elements <code>buf[0]</code>
   * through <code>buf[count-1]</code> are the
   * only bytes that can ever be read/write from/to the
   * buffer;  element <code>buf[pos]</code> is
   * the next byte to be read/write.
   */
  volatile byte buf[] = null;

  /**
   * The index of the next byte to read/write from/to the buffer. This value
   * should always be nonnegative and not shorter than <code>offset</offset>
   * and not larger than the value of <code>count</code>. The next byte to be
   * read/write from/to the buffer will be <code>buf[pos]</code>.
   */
  private int pos;

  /**
   * The index one greater than the last valid byte in the buffer.
   * This value should always be nonnegative
   * and not larger than the length of <code>buf</code>.
   * It  is one greater than the position of
   * the last byte within <code>buf</code> that
   * can ever be read/write from/to the buffer.
   */
  private int count;

  /**
   * Indicates the first available data in the buffer.
   */
  private int offset;

  /**
   *
   */
  private RandomAccessFile file = null;

  /**
   * Indicates the offset of the file where data are from.
   */
  private long filePos;

  /**
   * Indicates the data source, whether data is readed from file or no.
   */
  private boolean writeToFile;

  /**
   * Indicates if the buf has been changed since the last time it was written
   * in the file.
   */
  private boolean isChangedBuf = false;

  /**
   * Indicates if the buffer size can be resized. This attribute will only be
   * taken into account whe data are stored in the buffer, i.e., <code>
   * writeToFile</code> is false.
   */
  private boolean isResizeAllowed = false;

  /**
   * It is the default size for the buffer when this was not passed
   */
  private static int DEFAULT_BUFFER_SIZE = 100;	// 1KB

  private static int DEFAULT_BUFFER_RESIZE_INCREMENT = 1000; // 1KB

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public BufferedDataOutputStream() {
    this(DEFAULT_BUFFER_SIZE);
  }

  /**
   * Constructor.
   *
   * @param sz buffer size
   */
  public BufferedDataOutputStream(int sz) {
    if (sz <= 0) throw new IllegalArgumentException("Buffer size 1 or higher");

    this.buf = new byte[sz];
    count = 0;
    offset = 0;
    filePos = 0;
    pos = 0;
    writeToFile = false;
    isResizeAllowed = false;
  }

  /**
   * Constructor.
   *
   * @param buf
   */
  public BufferedDataOutputStream(byte[] buf) {
    if (buf == null) throw new NullPointerException();

    this.buf = buf;
    count = buf.length;
    offset = 0;
    filePos = 0;
    pos = 0;
    writeToFile = false;
    isResizeAllowed = false;
  }

  /**
   * Constructor.
   *
   * @param buf
   * @param offset
   */
  public BufferedDataOutputStream(byte[] buf, int offset) {
    if (buf == null) throw new NullPointerException();

    this.buf = buf;
    count = this.buf.length - offset + 1;
    this.offset = offset;
    filePos = 0;
    pos = offset;
    writeToFile = false;
    isResizeAllowed = false;
  }

  /**
   * Constructor.
   *
   * @param buf
   * @param offset
   * @param len
   */
  public BufferedDataOutputStream(byte[] buf, int offset, int len) {
    if (buf == null) throw new NullPointerException();

    this.buf = buf;
    this.offset = offset;
    this.count = len;
    filePos = 0;
    pos = offset;
    writeToFile = false;
    isResizeAllowed = false;
  }

  /**
   * Constructor.
   *
   * @param name
   *
   * @throws FileNotFoundException
   */
  public BufferedDataOutputStream(String name) throws FileNotFoundException {
    this(name != null ? new File(name) : null);
  }

  /**
   * Constructor.
   *
   * @param file
   *
   * @throws FileNotFoundException
   */
  public BufferedDataOutputStream(File file) throws FileNotFoundException {
    this(new RandomAccessFile(file, "w"));
  }

  /**
   * Constructor.
   *
   * @param file
   *
   * @throws FileNotFoundException
   */
  public BufferedDataOutputStream(RandomAccessFile file) {

    // Comprobar que el fichero NO tiene permisos solo de lectura !!!!!!!!!!!!!!

    if (file == null) throw new NullPointerException();

    this.file = file;
    buf = new byte[DEFAULT_BUFFER_SIZE];
    count = 0;
    offset = 0;
    filePos = 0;
    pos = 0;
    writeToFile = true;
  }

  /**
   * Sets the buffer resizable or not.
   *
   * @param isResizeAllowed definition in {@link #isResizeAllowed}
   */
  public void setResizable(boolean isResizeAllowed) {
    this.isResizeAllowed = isResizeAllowed;
  }

  /**
   * Returns the {@link #isResizeAllowed} attribute.
   *
   * @return the {@link #isResizeAllowed} attribute.
   */
  public boolean isResizable() {
    return isResizeAllowed;
  }

  /**
   * Writes to the output stream the eight
   * low-order bits of the argument <code>b</code>.
   * The 24 high-order  bits of <code>b</code>
   * are ignored.
   *
   * @param      b   the byte to be written.
   * @throws     IOException  if an I/O error occurs.
   */
  public void write(int b) throws IOException {
    if (pos >= buf.length) {
      flush();
    }
    if ((buf.length - pos) <= 0) {
      resizeBuffer(buf.length + DEFAULT_BUFFER_RESIZE_INCREMENT);
    }
    buf[pos++] = (byte)(b & 0xFF);
    count++;
    isChangedBuf = true;
  }

  /**
   * Writes to the output stream all the bytes in array <code>b</code>.
   * If <code>b</code> is <code>null</code>,
   * a <code>NullPointerException</code> is thrown.
   * If <code>b.length</code> is zero, then
   * no bytes are written. Otherwise, the byte
   * <code>b[0]</code> is written first, then
   * <code>b[1]</code>, and so on; the last byte
   * written is <code>b[b.length-1]</code>.
   *
   * @param      b   the data.
   * @throws     IOException  if an I/O error occurs.
   */
  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  /**
   * Writes <code>len</code> bytes from array
   * <code>b</code>, in order,  to
   * the output stream.  If <code>b</code>
   * is <code>null</code>, a <code>NullPointerException</code>
   * is thrown.  If <code>off</code> is negative,
   * or <code>len</code> is negative, or <code>off+len</code>
   * is greater than the length of the array
   * <code>b</code>, then an <code>IndexOutOfBoundsException</code>
   * is thrown.  If <code>len</code> is zero,
   * then no bytes are written. Otherwise, the
   * byte <code>b[off]</code> is written first,
   * then <code>b[off+1]</code>, and so on; the
   * last byte written is <code>b[off+len-1]</code>.
   *
   * @param      b     the data.
   * @param      off   the start offset in the data.
   * @param      len   the number of bytes to write.
   * @throws     IOException  if an I/O error occurs.
   */
  public void write(byte[] b, int off, int len) throws IOException {

    if (b == null) {
      throw new NullPointerException();
    }

    if ((off | len | (b.length - (len + off)) | (off + len)) < 0) {
      throw new IndexOutOfBoundsException();
    }

    if (((buf.length - pos) < b.length) && !isResizeAllowed) { // data can not be stored in buf and resize is not allowed
      throw new BufferOverflowException();
    }

    if ((buf.length - pos) < b.length) { // data size is greather than available size in the buf
      resizeBuffer(buf.length + b.length);
    }

    System.arraycopy(b, off, buf, pos, len);
    pos += len;
    isChangedBuf = true;
    count += len;
  }

  /**
   * Writes a <code>boolean</code> to the underlying output stream as
   * a 1-byte value. The value <code>true</code> is written out as the
   * value <code>(byte)1</code>; the value <code>false</code> is
   * written out as the value <code>(byte)0</code>. If no exception is
   * thrown, the counter <code>written</code> is incremented by
   * <code>1</code>.
   *
   * @param      v   a <code>boolean</code> value to be written.
   * @exception  IOException  if an I/O error occurs.
   * @see        java.io.FilterOutputStream#out
   */
  public void writeBoolean(boolean v) throws IOException {
    write(v ? 1 : 0);
  }

  public void writeByte(int v) throws IOException {
    write(v);
  }

  public void writeShort(int v) throws IOException {
    write((byte)(0xff & (v >>> 8)));
    write((byte)(0xff & v));
  }

  public void writeChar(int v) throws IOException {
    // TODO Auto-generated method stub
  }

  public void writeInt(int v) throws IOException {
    write((byte)(0xff & (v >> 24)));
    write((byte)(0xff & (v >> 16)));
    write((byte)(0xff & (v >> 8)));
    write((byte)(0xff & v));
  }

  public void writeLong(long v) throws IOException {
    write((byte)(0xff & (v >> 56)));
    write((byte)(0xff & (v >> 48)));
    write((byte)(0xff & (v >> 40)));
    write((byte)(0xff & (v >> 32)));
    write((byte)(0xff & (v >> 24)));
    write((byte)(0xff & (v >> 16)));
    write((byte)(0xff & (v >> 8)));
    write((byte)(0xff & v));
  }

  public void writeFloat(float v) throws IOException {
    writeInt(Float.floatToIntBits(v));
  }

  public void writeDouble(double v) throws IOException {
    writeLong(Double.doubleToLongBits(v));
  }

  public void writeBytes(String s) throws IOException {
    int len = s.length();
    for (int i = 0; i < len; i++) {
      write((byte)s.charAt(i));
    }
  }

  public void writeChars(String s) throws IOException {
    int len = s.length();
    for (int i = 0; i < len; i++) {
      int v = s.charAt(i);
      write((v >>> 8) & 0xFF);
      write((v >>> 0) & 0xFF);
    }

  }

  public void writeUTF(String s) throws IOException {
    // TODO Auto-generated method stub
  }

  /**
   * Returns the position where the next byte will be wrote.
   *
   * @return the position where the next byte will be wrote.
   */
  public long getPos() {
    if (writeToFile) {
      return (long)(filePos + pos);
    } else {
      return (long)pos;
    }
  }

  /**
   * Returns the number of bytes that can be written in this buffer without
   * blocking. The value returned is  <code>count&nbsp;- pos</code>, which
   * is the number of bytes remaining to be written in the output buffer.
   *
   * @return  the number of bytes that can be written in the buffer without
   * 		 blocking.
   */
  public synchronized long available() throws IOException {
    return (long)(buf.length - pos);
  }

  /**
   * Returns the number of bytes that they have been written in this file,
   * taking into account any buffering.
   *
   * @return The length of the stream, in bytes.
   **/
  public synchronized long length() {
    if (writeToFile) {
      return (filePos + pos - offset);
    } else {
      return (long)(count);
    }
  }

  /**
   * Returns the byte array that contains the stored data.
   * <p>
   * The length of the byte array is equal or greather than the amount of
   * stored data. Its real length can be known using the <code>length</code>
   * function.
   * <p>
   * Moreover, data can be started in any position of the byte array. The
   * position of the first data, offset in the byte array, can be known using
   * through the <code>getOffset</code> function.
   *
   * @return an one-dimensional byte array which contains the internal buffer.
   */
  public byte[] getBuffer() {
    return buf;
  }

  /**
   * Returns the {@link #offset} attribute.
   *
   * @return the {@link #offset} attribute.
   */
  public int getOffset() {
    return offset;
  }

  /**
   * Gets the buffer as a byte array. The length of the returned byte array
   * can be equal or greather than the length of stored data, then <data>
   * offset</data> and <data>length</data> should be taken into account.
   *
   * @return an one-dimensional byte array which contains the internal buffer.
   */
  public byte[] getByteArray() {
    return getByteArray(false);
  }

  /**
   * Gets the buffer as a byte array.
   * <p>
   * If the <code>adjust</code> parameter is <data>true</data> the length of
   * the returned byte array is adjusted to the number of bytes stored in the
   * array. Data buffer is copied.
   * <p>
   * Otherwise, the byte array is retorned. In this case the offset and length
   * must be taken into account. Buffer pointer is returned.
   * See {@link #offset} and {@link #length()}.
   * <p>
   * When data is stored in a file, buffered data is only retorned.
   *
   * @param adjust if <data>true</data>, the returned byte array is adjusted
   *    to the stored data length. Otherwise, the returned byte array can be
   *    equal or greather than the stored data length.
   *
   * @return an one-dimensional byte array which contains the internal buffer.
   */
  public byte[] getByteArray(boolean adjust) {
    if (adjust) {
      byte[] tempBuf = new byte[count];
      System.arraycopy(buf, offset, tempBuf, 0, count);
      buf = null;
      buf = tempBuf;
    }
    return buf;
  }

  /**
   * Data stored in the <code>buff</code> must be written in a file.
   * Buffer is reseted to its initial state.
   *
   * @exception IOException If an I/O error ocurred.
   * */
  public final void flush() throws IOException {
    if (writeToFile) {		// buffer is stored in the file
      if (isChangedBuf) {
        file.write(buf, offset, count);
        isChangedBuf = false;
        pos = offset;
        count = 0;
      }
    }
  }

  /**
   * Resets the buffer restoring the pointer to its initial position.
   */
  public void reset() {
    pos = offset;
    count = 0;
  }

  /**
   * Closes the buffered data output stream
   *
   * @exception IOException If an I/O error ocurred.
   * */
  public final void close() throws IOException {
    if (writeToFile) {
      flush();
      file.close();
      buf = null;
    }
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [\n";

    if (writeToFile) {
      str += "data destination: FILE\n";
    } else {
      str += "data destination: BUFFER\n";
    }

    str += "length=" + count + "\n";
    str += "offset=" + offset + "\n";
    str += "pointer=" + pos + "\n";
    str += "buffer=";

    for (int i = 0; i < buf.length; i++) {
      if ((0xFF & buf[i]) < 16) {
        str += "0";
      }
      str += Integer.toHexString(0xFF & buf[i]);
    }
    str += "\n";

    str += "]";
    return str;
  }

  /**
   * Prints this Buffered Data Output Stream out to the
   * specified output stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Buffered Data Output Stream --");

    if (writeToFile) {
      out.println("Data destination: FILE");
    } else {
      out.println("Data destination: BUFFER");
    }

    out.println("length: " + count);
    out.println("offset: " + offset);
    out.println("pointer: " + pos);
    out.print("buffer: ");

    for (int i = 0; i < buf.length; i++) {
      if ((0xFF & buf[i]) < 16) {
        out.print("0");
      }
      out.print(Integer.toHexString(0xFF & buf[i]));
    }
    out.println();

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   *
   */
  private void resizeBuffer(int newSize) {
    byte[] newBuffer = new byte[newSize];
    System.arraycopy(buf, 0, newBuffer, 0, buf.length);
    buf = null;
    buf = newBuffer;
  }

}
