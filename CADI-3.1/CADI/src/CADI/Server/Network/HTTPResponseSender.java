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
package CADI.Server.Network;

import CADI.Common.Network.TrafficShaping;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import CADI.Common.Network.HTTP.HTTPResponse;
import CADI.Common.Network.HTTP.StatusCodes;

/**
 * This class implements a wrapper to send an HTTP Response. 
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.3 2012/03/22
 */
public class HTTPResponseSender {

  /**
   * Is the carriage-return and line-feed characters.
   */
  private static final String CRLF = "\r\n";

  /**
   * Is the carriage and return line-feed bytes.
   */
  public static final byte[] CRLF_Bytes = {(byte)13, (byte)10};

  /**
   * Is an output stream that will be used to send the server response to the client.
   */
  private OutputStream os = null;

  /**
   * Definition in {@link CADI.Server.Core.Scheduler#maxTxRate}. But a O
   * value does not mean unlimited rate delivery, it means to delivery at
   * 0 bytes/sec.
   */
  private long maxTxRate = Long.MAX_VALUE;

  // INTERNAL ATTRIBUTES
  private static final String HTTP_VERSION_11 = "HTTP/1.1";

  /**
   * Not used yet.
   */
  private boolean chunkedMode = true;

  /**
   *
   */
  private long[] sentBytes = new long[10];

  /**
   *
   */
  private long[] times = new long[10];

  /**
   *
   */
  private TrafficShaping trafficShaping = null;
  // TODO:

  private long maxChunkLength = 1024; // 1 KB

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param os the output stream to send the HTTP data.
   *
   * @throws NullPointerException if the output stream parameter is null.
   */
  public HTTPResponseSender(OutputStream os) {
    if (os == null) {
      throw new NullPointerException();
    }

    this.os = os;

    for (int i = 0; i < sentBytes.length; i++) {
      sentBytes[i] = 0L;
      times[i] = 0L;
    }

    trafficShaping = new TrafficShaping(-1, -1, TrafficShaping.NONE);
  }

  /**
   * Sets the {@link #maxTxRate} attribute.
   *
   * @param maxTxRate definition in {@link #maxTxRate}.
   */
  public final void setMaxTxRate(long maxTxRate) {
    setMaxTxRate(maxTxRate, TrafficShaping.TOKEN_BUCKET);
  }

  /**
   * 
   * @param maxTxRate
   * @param algorithm
   */
  public final void setMaxTxRate(long maxTxRate, int algorithm) {
    this.maxTxRate = (maxTxRate > 0F) ? maxTxRate : Long.MAX_VALUE;
    trafficShaping = new TrafficShaping((long)(this.maxTxRate * 1.2),
            this.maxTxRate, algorithm);
  }

  /**
   * Sends the HTTP protocol's headers. Headers must be passed as a
   * HTTPResponse object.
   *
   * @param headers the HTTP protocol's header to send.
   *
   * @throws IOException if there is not connection to the remote host or the
   * 						operation has been interrupted.
   */
  public void sendHeaders(HTTPResponse headers) throws IOException {

    assert ((headers.getResponseCode() == StatusCodes.OK)
            || (headers.getResponseCode() == StatusCodes.ACCEPTED)
            || (headers.getResponseCode() == StatusCodes.BAD_REQUEST)
            || (headers.getResponseCode() == StatusCodes.NOT_FOUND)
            || (headers.getResponseCode() == StatusCodes.UNSUPPORTED_MEDIA_TYPE)
            || (headers.getResponseCode() == StatusCodes.NOT_IMPLEMENTED)
            || (headers.getResponseCode() == StatusCodes.SERVICE_UNAVAILABLE));


    // Status line
    os.write((HTTP_VERSION_11 + " " + headers.getResponseCode() + " " + headers.getResponseMessage() + " " + CRLF).getBytes());

    // General header: date
    os.write(("Date: " + (new Date().toString()) + CRLF).getBytes());	// rfc 2616 section 14.18

    if ((headers.getResponseCode() == StatusCodes.OK) || (headers.getResponseCode() == StatusCodes.ACCEPTED)) {
      String value;
      for (String key : headers.getHeaders()) {
        value = headers.getHeaderField(key);
        os.write((key + ": " + value + CRLF).getBytes());
      }
    }
  }

  /**
   *
   * @param args
   * @throws IOException
   */
  public void sendHeaders(String... args) throws IOException {
    for (int i = 0; i < args.length; i++) {
      if ((args[i] != null) || (args[i].length() > 0)) {
        os.write((args[i] + CRLF).getBytes());
      }
    }
  }

  /**
   *
   * @param header
   *
   * @throws IOException if there is not connection to the remote host or the
   * 						operation has been interrupted.
   */
  public void sendHeader(String header) throws IOException {
    if (header == null) {
      return;
    }
    if (header.length() == 0) {
      return;
    }

    os.write(header.getBytes());
    os.write(CRLF_Bytes);
  }

  /**
   *
   * @param key
   * @param value
   *
   * @throws IOException if there is not connection to the remote host or the
   * 						operation has been interrupted.
   */
  public void sendHeader(String key, String value) throws IOException {
    if ((key == null) || (key.length() == 0)) {
      return;
    }
    if ((value == null) || (value.length() == 0)) {
      return;
    }

    os.write((key + ": " + value + CRLF).getBytes());
  }

  /**
   *
   * @throws IOException if there is not connection to the remote host or the
   * 						operation has been interrupted.
   */
  public void endOfHeaders() throws IOException {
    os.write(CRLF_Bytes);
  }

  /**
   * Sends a chunk of the HTTP body. If the <code>chunk<code> data is <code>
   * null</code>, this method returns and does nothing.
   * <p>
   * This method is only used when the HTTP chunked mode mode is set.
   *
   * @param chunk a byte array with chunk data to send.
   *
   * @throws IOException if there is not connection to the remote host or the
   * 						operation has been interrupted.
   */
  public void sendChunk(byte[] chunk) throws IOException {
    if (chunk == null) {
      return;
    }
    sendChunk(chunk, 0, chunk.length);
  }
  
  /**
   *  Sends a chunk of the HTTP body. If the <code>chunk<code> data is <code>
   * null</code>, this method returns and does nothing.
   * <p>
   * This method is only used when the HTTP chunked mode mode is set.
   *
   * @param chunk a byte array with chunk data to send.
   * @param off offset of the first byte to be sent.
   * @param len length of data to be sent.
   * 
   * @throws IOException if there is not connection to the remote host or the
   * 						operation has been interrupted.
   */
  public void sendChunk(byte[] chunk, int off, int len) throws IOException {
    if (chunk == null) {
      return;
    }
    if ((off < 0) || (len < 0)) throw new IllegalArgumentException();

    int offChunk = off;
    int lenChunk = 0;
    byte[] lenBytes = null;
    int lenAllowed = 0;
    int offInChunk = 0;
    int lenInChunk = 0;

    do {
      lenChunk = (offChunk + maxChunkLength < len)
              ? (int)maxChunkLength
              : len - offChunk;

      // Send chunk length
      lenBytes = Long.toHexString(lenChunk).getBytes();
      lenAllowed = trafficShaping.getTokens(lenBytes.length + 2);
      assert (lenAllowed == (lenBytes.length + 2));
      os.write(lenBytes);
      os.write(CRLF_Bytes);

      // Send chunk data
      offInChunk = offChunk;
      lenInChunk = lenChunk;
      do {
        lenInChunk = offChunk + lenChunk - offInChunk;
        lenAllowed = trafficShaping.getTokens(lenInChunk);
        assert (lenAllowed > 0);
        os.write(chunk, offInChunk, lenAllowed);
        offInChunk += lenAllowed;
      } while (offInChunk < (offChunk + lenChunk));

      lenAllowed = trafficShaping.getTokens(2);
      assert (lenAllowed == 2);
      os.write(CRLF_Bytes);

      offChunk += lenChunk;

    } while (offChunk < len);
  }

  /**
   * Sends multiple chunks as a only one chunk. The sent chunk length is the
   * total amount of all chunk parameters. If some of the <code>chunk<code>
   * parameter is <code>null</code>, it is discarded.
   * <p>
   * This method is only used when the HTTP chunked mode mode is set.
   *
   * @param args the chunk data
   *
   * @throws IOException if there is not connection to the remote host or the
   * 						operation has been interrupted.
   */
  public void sendChunks(byte[]... args) throws IOException {
    for (int i = 0; i < args.length; i++) {
      sendChunk(args[i]);
    }
  }

  /**
   * Flushes this output stream and forces any buffered output bytes to be sent.
   *
   * @throws IOException if there is not connection to the remote host or the
   * 						operation has been interrupted.
   */
  public void flush() throws IOException {
    os.flush();
  }

  /**
   * Sends the end of the chunk. The end of the chunk consists of:
   * 0<br>
   * CRLF<br>
   * CRLF<br>
   *
   * @throws IOException if there is not connection to the remote host or the
   * 						operation has been interrupted.
   */
  public void endOfChunk() throws IOException {
    os.write(Integer.toHexString(0).getBytes());
    os.write(CRLF_Bytes);
    os.write(CRLF_Bytes);
    os.flush();
  }

  // ============================ private methods ==============================
  /**
   * This method updates the {@link #bytesSent} and {@link #times} attributes
   * with new new values which are parssed as input parameters.
   *
   * @param bytes the new value to be added in the {@link #bytesSent}
   * 			attribute.
   * @param time the new value to be added in the {@link #times} attribute.
   */
  private void updateBytesSentAndTimes(long bytes, long time) {
    for (int i = 1; i < sentBytes.length; i++) {
      sentBytes[i - 1] = sentBytes[i];
      times[i - 1] = times[i];
    }
    sentBytes[sentBytes.length - 1] = bytes;
    times[times.length - 1] = time;
  }
}
