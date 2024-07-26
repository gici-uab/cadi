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
package CADI.Client.Cache;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import CADI.Client.Network.JPIPMessageDecoder;
import CADI.Client.Network.JPIPResponseReader;
import CADI.Common.Cache.*;
import CADI.Common.Network.JPIP.ClassIdentifiers;
import CADI.Common.Network.JPIP.JPIPMessage;
import CADI.Common.Network.JPIP.JPIPMessageHeader;
import CADI.Common.Network.JPIP.JPIPMessageEncoder;
import GiciException.ErrorException;
import java.io.EOFException;

/**
 * This class implements the client-side cache management. It is based on the
 * {@link CADI.Common.Cache.CacheManagement} class providing an interface to
 * access to the generic CacheManagement with methods more suitable for a JPIP
 * client.
 * <p>
 * 
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.3 2011/09/05
 */
public class ClientCacheManagement extends CacheManagement {

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public ClientCacheManagement() {
    super();
  }

  /**
   * Constructor. This constructor should be used when the client cache is not
   * kept completely in the memory but also swapped to a file.
   * <p>
   * <B>OBS: This option is not available yet, therefore the calling to this
   * constructor is redirected to the {@link #ClientCacheManagement()}
   * constructor.</B>
   *
   * @param fileName
   */
  public ClientCacheManagement(String fileName) {
    this();
  }

  /**
   * Sets the attributes to its initial values.
   */
  @Override
  public void reset() {
    super.reset();
  }

  /**
   * Clears the client cache but not remove the image parameters
   * (the {@link #jpcParameters} attribute).
   */
  @Override
  public void clear() {
    super.reset();
  }

  /**
   * 
   * @param fileName
   */
  public void saveCache(String fileName) throws ErrorException {

    // Open file
    FileOutputStream outputFile = null;
    try {
      outputFile = new FileOutputStream(new File(fileName));
    } catch (FileNotFoundException e) {
      throw new ErrorException("File \"" + fileName + "\" cannot be created");
    }
    DataOutputStream outStream = new DataOutputStream(outputFile);

    JPIPMessageHeader jpipHeader = null;
    JPIPMessageEncoder jpipHeaderEncoder = new JPIPMessageEncoder(true, true);
    byte[] header = null;


    // Save main header
    try {
      jpipHeader = new JPIPMessageHeader();
      jpipHeader.CSn = 0;
      jpipHeader.classIdentifier = ClassIdentifiers.MAIN_HEADER;
      jpipHeader.inClassIdentifier = 0;
      jpipHeader.msgOffset = 0;
      jpipHeader.msgLength = mainHeaderDataBin.getLength();
      jpipHeader.isLastByte = mainHeaderDataBin.isComplete();

      header = jpipHeaderEncoder.encoderHeader(jpipHeader);
      outStream.write(header);

      byte[] data = new byte[(int)mainHeaderDataBin.getLength()];
      mainHeaderDataBin.seek(0);
      mainHeaderDataBin.readFully(data);
      outStream.write(data, 0, data.length);

      outStream.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Save precinct data-bins
    try {
      PrecinctDataBin dataBin = null;

      for (Map.Entry<Long, PrecinctDataBin> entry : precinctsDataBins.entrySet()) {
        dataBin = entry.getValue();

        jpipHeader = new JPIPMessageHeader();
        jpipHeader.CSn = 0;
        jpipHeader.classIdentifier = ClassIdentifiers.PRECINCT + 1;
        jpipHeader.inClassIdentifier = dataBin.getInClasIdentifier();
        jpipHeader.msgOffset = 0;
        jpipHeader.msgLength = dataBin.getLength();
        jpipHeader.isLastByte = dataBin.isComplete();
        jpipHeader.Aux = dataBin.getNumCompletePackets();
        header = jpipHeaderEncoder.encoderHeader(jpipHeader);
        outStream.write(header);

        byte[] data = new byte[(int)dataBin.getLength()];
        dataBin.seek(0);
        dataBin.readFully(data);
        outStream.write(data, 0, data.length);
      }
      outStream.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Close file
    try {
      outStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public void loadCache(String fileName) throws ErrorException {

    // Open file
    FileInputStream inputFile = null;
    try {
      inputFile = new FileInputStream(new File(fileName));
    } catch (FileNotFoundException e) {
      throw new ErrorException("File \"" + fileName + "\" cannot be created");
    }

    // Read data bins
    JPIPResponseReader reader = new JPIPResponseReader(inputFile);

    JPIPMessageDecoder msgDecoder = new JPIPMessageDecoder();
    msgDecoder.setParameters(reader);

    JPIPMessage jpipMessage = null;
    try {
      while (!reader.isEndOfStream()) {
        jpipMessage = msgDecoder.readMessage();
        this.addJPIPMessage(jpipMessage);
      }
    } catch (EOFException eofe) {
      // All file has been read
    } catch (IOException e1) {
      e1.printStackTrace();
    }


    // Close file
    try {
      inputFile.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param inClassIdentifier
   * @return
   */
  public final PrecinctDataBin getPrecinctDataBin(long inClassIdentifier) {
    return (PrecinctDataBin)getDataBin(ClassIdentifiers.PRECINCT, inClassIdentifier);
  }
  
  /**
   * 
   * @return 
   */
  public final int getNumMetaDataBins() {
    return (metaDataBins != null ? metaDataBins.size() : 0);
  }
  
  /**
   * 
   * @param inClassIdentifier
   * @return 
   */
  public final MetaDataBin getMetaDataBin(long inClassIdentifier) {
    return (metaDataBins.containsKey(inClassIdentifier)
            ? metaDataBins.get(inClassIdentifier)
            : null);
  }
  
  
  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";
    str += super.toString();
    str += "]";

    return str;
  }

  /**
   * Prints this cache out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Client Cache Management --");
    super.list(out);

    out.flush();
  }

  // ============================ private methods ==============================
}
