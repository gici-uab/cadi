/*
 * CADI Software - a JPIP Client/Server framework
 * Copyright (C) 2007-2012 Group on Interactive Coding of Images (GICI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package CADI.Common.LogicalTarget.JPEG2000.Codestream;

import CADI.Common.LogicalTarget.JPEG2000.*;
import CADI.Server.LogicalTarget.JPEG2000.*;
import CADI.Common.LogicalTarget.JPEG2000.Indexing.FragmentArrayIndex;
import CADI.Common.LogicalTarget.JPEG2000.Indexing.PacketHeaderIndexTable;
import CADI.Common.LogicalTarget.JPEG2000.Indexing.PrecinctPacketIndexTable;
import CADI.Common.LogicalTarget.JPEG2000.Indexing.TileHeaderIndexTable;
import CADI.Common.LogicalTarget.JPEG2000.Indexing.TilePartIndexTable;
import GiciStream.BufferedDataInputStream;
import GiciException.*;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class reads the image a store it in a concret structure to decompress.
 * <p>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; run
 * &nbsp; get functions<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.4 2012/01/14
 */
public class CodestreamIndexing {

  /**
   *
   */
  private JPEG2KCodestream codestream = null;

  private JPEG2KTile tileObj = null;

  private JPEG2KComponent componentObj = null;

  private JPEG2KResolutionLevel rLevelObj = null;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Codestream.PacketHeadersDecoder}
   */
  private PacketHeadersDecoder PkDeheading = null;

  /**
   * Is an input stream from where data will be read.
   */
  private BufferedDataInputStream in = null;

  /**
   * Indicates whether the coding passes structure must be read. Otherwise,
   * codestream is only indexed at packet structure.
   */
  private boolean readCodingPasses = false;

  public TilePartIndexTable tpix = null;

  /**
   *
   */
  public TileHeaderIndexTable thix = null;

  /**
   *
   */
  public PrecinctPacketIndexTable ppix = null;

  /**
   *
   */
  public PacketHeaderIndexTable phix = null;

  // ============================= public methods ==============================
  /**
   * Constructor
   *
   * @param in definition in {@link #in}
   * @param codestream
   *
   * @throws ErrorException when the file cannot be loaded.
   */
  public CodestreamIndexing(BufferedDataInputStream in,
                            ServerJPEG2KCodestream codestream) throws ErrorException {

    // Check input parameters
    if (in == null) {
      throw new NullPointerException();
    }
    if (codestream == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.in = in;
    this.codestream = codestream;

    tileObj = codestream.getTile(0);

    this.PkDeheading = new PacketHeadersDecoder(codestream,
                                                tileObj.useSOP(),
                                                tileObj.useEPH());

    tpix = new TilePartIndexTable();
    thix = new TileHeaderIndexTable();
    ppix = new PrecinctPacketIndexTable();
    phix = new PacketHeaderIndexTable();
  }

  /**
   * Sets the {@link #readCodingPasses} attribute.
   *
   * @param readCodingPasses definition in {@link #readCodingPasses}.
   */
  public void setReadingCodingPasses(boolean readCodingPasses) {
    this.readCodingPasses = readCodingPasses;
  }

  /**
   * Reads file with selected progression order.
   *
   * @throws ErrorException when some error occurs
   */
  public void run() throws ErrorException {

    // Acquires the resource
    in.lock();
    try {

      readTilePartHeader();

      //Call progression order functions
      switch (tileObj.getProgressionOrder()) {
        case 0://LRCP
          LRCP();
          break;
        case 1://RLCP
          RLCP();
          break;
        case 2://RPCL
          RPCL();
          break;
        case 3://PCRL
          PCRL();
          break;
        case 4://CPRL
          CPRL();
          break;
      }

    } finally {
      in.unlock();
      PkDeheading.reset();
    }
  }

  public TilePartIndexTable getTilePartIndexTable() {
    return tpix;
  }

  public TileHeaderIndexTable getTileHeaderIndexTable() {
    return thix;
  }

  public PrecinctPacketIndexTable getPrecinctPacketIndexTable() {
    return ppix;
  }

  public PacketHeaderIndexTable getPacketHeaderIndexTable() {
    return phix;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    String str = "";

    return str;
  }

  /**
   * Prints this Codestream Indexing fields out to the specified output
   * stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Codestream Indexing --");

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * Reads a byte from the file and returns its value.
   *
   * @return the byte readed
   *
   * @throws ErrorException when some wrong bitstream or I/O operation occurs
   */
  private int readByte() throws ErrorException {
    int value;

    try {
      value = in.read();
    } catch (IOException e) {
      throw new ErrorException("I/O error (" + e.toString() + ").");
    }
    return (value);
  }

  /**
   * Reads a tile using LRCP progression.
   *
   * @throws ErrorException when the file cannot be load
   */
  private void LRCP() throws ErrorException {

    int maxAllRlevels = maxRlevel() + 1;
    int maxLayers = tileObj.getNumLayers();
    int maxComps = codestream.getZSize();
    int maxRLevel = -1;
    int maxPrecincts = -1;

    for (int layer = 0; layer < maxLayers; layer++) {
      for (int rLevel = 0; rLevel < maxAllRlevels; rLevel++) {
        for (int z = 0; z < maxComps; z++) {
          componentObj = tileObj.getComponent(z);
          maxRLevel = componentObj.getWTLevels() + 1;
          if (rLevel < maxRLevel) {
            rLevelObj = componentObj.getResolutionLevel(rLevel);
            maxPrecincts = rLevelObj.getNumPrecincts();
            for (int precinct = 0; precinct < maxPrecincts; precinct++) {
              readPrecinct(tileObj.getComponent(z).getResolutionLevel(rLevel).getPrecinct(precinct), layer, 1);
            }
          }
        }
      }
    }
  }

  /**
   * Reads a tile using RLCP progression.
   *
   * @throws ErrorException when the file cannot be load
   */
  private void RLCP() throws ErrorException {

    int maxAllRlevels = maxRlevel() + 1;
    int maxLayers = tileObj.getNumLayers();
    int maxComps = codestream.getZSize();
    int maxRLevel = -1;
    int maxPrecincts = -1;

    for (int rLevel = 0; rLevel < maxAllRlevels; rLevel++) {
      for (int layer = 0; layer < maxLayers; layer++) {
        for (int z = 0; z < maxComps; z++) {
          componentObj = tileObj.getComponent(z);
          maxRLevel = componentObj.getWTLevels() + 1;
          if (rLevel < maxRLevel) {
            rLevelObj = componentObj.getResolutionLevel(rLevel);
            maxPrecincts = rLevelObj.getNumPrecincts();
            for (int precinct = 0; precinct < maxPrecincts; precinct++) {
              readPrecinct(tileObj.getComponent(z).getResolutionLevel(rLevel).getPrecinct(precinct), layer, 1);
            }
          }
        }
      }
    }
  }

  /**
   * Reads a tile using RPCL progression.
   *
   * @throws ErrorException when the file cannot be load
   */
  private void RPCL() throws ErrorException {

    int maxPrecincts = maxPrecinct();

    int maxAllRlevels = maxRlevel() + 1;
    int maxLayers = tileObj.getNumLayers();
    int maxComps = codestream.getZSize();
    int maxRLevel = -1;

    for (int rLevel = 0; rLevel < maxAllRlevels; rLevel++) {
      for (int precinct = 0; precinct < maxPrecincts; precinct++) {
        for (int z = 0; z < maxComps; z++) {
          componentObj = tileObj.getComponent(z);
          maxRLevel = componentObj.getWTLevels() + 1;

          if (rLevel < maxRLevel) {
            rLevelObj = componentObj.getResolutionLevel(rLevel);
            if (rLevelObj.getNumPrecincts() > precinct) {
              readPrecinct(tileObj.getComponent(z).getResolutionLevel(rLevel).getPrecinct(precinct), 0, maxLayers);
            }
          }
        }
      }
    }
  }

  /**
   * Reads a tile using PCRL progression.
   *
   * @throws ErrorException when the file cannot be load
   */
  private void PCRL() throws ErrorException {

    int maxComps = codestream.getZSize();
    int maxRLevel = -1;

    int maxPrecinctsHeigh = numMaxPrecinctsHeigh();
    int maxPrecinctsWide = numMaxPrecinctsWide();

    for (int precinctY = 0; precinctY < maxPrecinctsHeigh; precinctY++) {
      for (int precinctX = 0; precinctX < maxPrecinctsWide; precinctX++) {
        for (int z = 0; z < maxComps; z++) {
          componentObj = tileObj.getComponent(z);
          maxRLevel = componentObj.getWTLevels() + 1;
          for (int rLevel = 0; rLevel < maxRLevel; rLevel++) {
            rLevelObj = componentObj.getResolutionLevel(rLevel);
            int px = (int)precinctCorrespondenceX(z, precinctX, rLevel, maxRLevel);
            int py = (int)precinctCorrespondenceY(z, precinctY, rLevel, maxRLevel);
            int numPrecinctsWide = tileObj.getComponent(z).getResolutionLevel(rLevel).getNumPrecinctsWide();
            int precinct = (py * numPrecinctsWide) + px;
            long inClassIdentifier = tileObj.getComponent(z).getResolutionLevel(rLevel).getInClassIdentifier(precinct);
            if (!ppix.isInitializedIndexTable(inClassIdentifier)) {
              readPrecinct(tileObj.getComponent(z).getResolutionLevel(rLevel).getPrecinct(precinct), 0, tileObj.getNumLayers());
            }
          }
        }
      }
    }
  }

  /**
   * Reads tile using CPRL progression.
   *
   * @throws ErrorException when the file cannot be load
   */
  private void CPRL() throws ErrorException {

    int maxRlevel = maxRlevel();
    int numComponents = codestream.getZSize();

    int maxPrecinctsHeigh = numMaxPrecinctsHeigh();
    int maxPrecinctsWide = numMaxPrecinctsWide();
    
    JPEG2KComponent compObj = null;
    JPEG2KResolutionLevel rLevelObj = null;
    JPEG2KPrecinct precinctObj = null;


    for (int z = 0; z < numComponents; z++) {
      compObj = tileObj.getComponent(z);
      for (int precinctY = 0; precinctY < maxPrecinctsHeigh; precinctY++) {
        for (int precinctX = 0; precinctX < maxPrecinctsWide; precinctX++) {
          for (int rLevel = 0; rLevel < maxRlevel; rLevel++) {
            if (rLevel > compObj.getWTLevels()) continue;

            rLevelObj = compObj.getResolutionLevel(rLevel);
            int px = (int)precinctCorrespondenceX(z, precinctX, rLevel, maxRlevel);
            int py = (int)precinctCorrespondenceY(z, precinctY, rLevel, maxRlevel);

            int numPrecinctsWide = rLevelObj.getNumPrecinctsWide();

            int precinct = (py * numPrecinctsWide) + px;
            precinctObj = tileObj.getComponent(z).getResolutionLevel(rLevel).getPrecinct(precinct);
            long inClassIdentifier = rLevelObj.getInClassIdentifier(precinct);
            if (!ppix.isInitializedIndexTable(inClassIdentifier)) {
              readPrecinct(rLevelObj.getPrecinct(precinct), 0, tileObj.getNumLayers());
            }
          }
        }
      }
    }
  }

  /**
   * Read a precinct from a file.
   *
   * @param inClassIdentifier is the precinct identifier.
   * @param startLayer it refers to the first layer to be read.
   * @param numLayers it refers to the number of layers to read.
   *
   * @throws ErrorException when the file cannot be read correctly.
   */
  private void readPrecinct(JPEG2KPrecinct precinctObj, int startLayer, int numLayers) throws ErrorException {

    long inClassIdentifier = precinctObj.getInClassIdentifier();
    try {
      if (readCodingPasses) {

        if (!ppix.isInitializedIndexTable(inClassIdentifier)) {
          ppix.initializeIndexTable(FragmentArrayIndex.VERSION_0, inClassIdentifier, true, precinctObj);
        }
        readCodingPassesStructure(inClassIdentifier, startLayer, numLayers);
      } else {

        if (!ppix.isInitializedIndexTable(inClassIdentifier)) {
          ppix.initializeIndexTable(FragmentArrayIndex.VERSION_0, inClassIdentifier, codestream.getNumLayers());
        }
        readPacketStructure(inClassIdentifier, startLayer, numLayers);
      }
    } catch (IOException e) {
      throw new ErrorException();
    }
  }

  /**
   * Read a precinct from a file.
   *
   * @param inClassIdentifier is the precinct identifier.
   * @param startLayer it refers to the first layer to be read.
   * @param numLayers it refers to the number of layers to read.
   *
   * @throws ErrorException when the file cannot be read correctly.
   */
  private void readPacketStructure(long inClassIdentifier,
                                   int startLayer, int numLayers) throws ErrorException, IOException {

    int[][][][] precinctdata = null;

    for (int layer = startLayer; layer < startLayer + numLayers; layer++) {

      // Gets the file pointer for this packet
      long fpInitialPos = in.getPos();
      ppix.setFilePointer(inClassIdentifier, layer, fpInitialPos);

      precinctdata = PkDeheading.packetHeaderDecoding(new PacketHeaderDataInputStream(in), inClassIdentifier);
      //long fpIniDataPos = in.getPos();

      long bytesToSkip = 0;
      if (precinctdata != null) {
        for (int subband = 0; subband < precinctdata.length; subband++) {
          if (precinctdata[subband] != null) {
            for (int yBlock = 0; yBlock < precinctdata[subband].length; yBlock++) {
              if (precinctdata[subband][yBlock] != null) {
                for (int xBlock = 0; xBlock < precinctdata[subband][yBlock].length; xBlock++) {
                  if (precinctdata[subband][yBlock][xBlock] != null) {
                    for (int cp = 0; cp < precinctdata[subband][yBlock][xBlock].length; cp++) {
                      bytesToSkip += precinctdata[subband][yBlock][xBlock][cp];
                      //System.out.println("\t\t\t\tsubband="+subband+" yBlock="+yBlock+" xBlock="+xBlock+" cp="+cp+" length="+ precinctdata[subband][yBlock][xBlock][cp]);
                      //System.out.println("layer="+layer+" z="+z+" rLevel="+rLevel+" precinct="+precinct+" subband="+subband+" yBlock="+yBlock+" xBlock="+xBlock+" cp="+cp+" offset="+in.getPos()+" length="+ precinctdata[subband][yBlock][xBlock][cp]);
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Skip packet body
      try {
        in.skipBytes(bytesToSkip);
      } catch (IOException e) {
        throw new ErrorException();
      }

      // Gets the packet body length
      ppix.setLength(inClassIdentifier, layer, in.getPos() - fpInitialPos);
    }
  }

  /**
   * Read a precinct in a file up to the coding pass level.
   *
   * @param rLevel whichs belong the precinct
   * @param precinct that will be written in file
   * @param z it refers to the component that belongs the precinct
   * @param layerBegin it refers to the first layer to write
   * @param layerToWrite it refers to the number of layers to write
   *
   * @throws ErrorException when the file cannot be load
   */
  private void readCodingPassesStructure(long inClassIdentifier, int startLayer, int numLayers) throws ErrorException, IOException {
    int[][][][] precinctData = null;

    for (int layer = startLayer; layer < startLayer + numLayers; layer++) {
      precinctData = PkDeheading.packetHeaderDecoding(new PacketHeaderDataInputStream(in), inClassIdentifier);

      if (precinctData != null) { // Subband

        // Read file pointers and lengths of coding passes
        for (int subband = 0; subband < precinctData.length; subband++) {
          if (precinctData[subband] != null) {

            for (int yBlock = 0; yBlock < precinctData[subband].length; yBlock++) {
              if (precinctData[subband][yBlock] != null) {

                for (int xBlock = 0; xBlock < precinctData[subband][yBlock].length; xBlock++) {
                  if (precinctData[subband][yBlock][xBlock] != null) {

                    long[] pointers = new long[precinctData[subband][yBlock][xBlock].length];
                    for (int cp = 0; cp < precinctData[subband][yBlock][xBlock].length; cp++) {
                      pointers[cp] = in.getPos();
                      try {
                        in.skipBytes(precinctData[subband][yBlock][xBlock][cp]);
                      } catch (IOException e) {
                        throw new ErrorException();
                      }
                      /* try {
                       * System.out.println("\t\t\tsb="+subband+" yb="+yBlock+" xb="+xBlock+" cp="+cp+" fp="+pointers[cp]+" len="+precinctData[subband][yBlock][xBlock][cp]+" zbp="+PkDeheading.getZeroBitPlanes(inClassIdentifier)[subband][yBlock][xBlock]);
                       * } catch (IllegalAccessException ex) {
                       * Logger.getLogger(CodestreamIndexing.class.getName()).log(Level.SEVERE, null, ex);
                       * } */
                    }
                    ppix.setFilePointer(inClassIdentifier, subband, yBlock, xBlock, pointers);
                    ppix.setLength(inClassIdentifier, subband, yBlock, xBlock, precinctData[subband][yBlock][xBlock]);
                  }
                }
              }
            }
          }
        }
      }

      try {
        //precinctObj.setZeroBitPlanes(PkDeheading.getZeroBitPlanes(inClassIdentifier));
        ppix.setZeroBitPlanes(inClassIdentifier, PkDeheading.getZeroBitPlanes(inClassIdentifier));
      } catch (IllegalAccessException e) {
        e.printStackTrace();
        assert (true);
      }
    }
  }

  /**
   *
   * @throws ErrorException
   */
  private void readTilePartHeader() throws ErrorException {
    readSOT();//SOT marker segment
    readSOD();//SOD marker
  }

  /**
   * Reads the SOT marker segment.
   *
   * @throws ErrorException when SOT marker segment is incorrect or tile options are not supported by BOI
   */
  private void readSOT() throws ErrorException {

    thix.tileHeaderFilePointer = in.getPos();

    // SOT marker segment's Lsot
    int Lsot = (readByte() << 8) | readByte();
    if (Lsot != 10) {
      throw new ErrorException("SOT marker segment's length is incorrect.");
    }

    //SOT marker segment's Isot
    int Isot = (readByte() << 8) | readByte();
    if (Isot > 0) {
      throw new ErrorException("CADI does not support more than one tile or tile-part.");
    }

    //SOT marker segment's Psot
    int Psot = (readByte() << 24) | (readByte() << 16) | (readByte() << 8) | readByte();
    if ((Psot < 12) || (Psot > (Math.pow(2, 32)) - 1)) {
      throw new ErrorException("Value of PSOT is exceded.");
    }

    //SOT marker segment's TPsot
    int TPsot = readByte();
    if ((TPsot < 0) || (TPsot > (Math.pow(2, 8)) - 2)) {
      throw new ErrorException("Tile-part index is wrong");
    }
    if (TPsot > 0) {
      throw new ErrorException("CADI does not support more than one tile or tile-part.");
    }

    //SOT marker segment's TNsot
    int TNsot = readByte();

    if ((TNsot < 0) || (TNsot > (Math.pow(2, 8)) - 1)) {
      throw new ErrorException("Number of tile-parts of a tile in the codestream is wrong");
    }
    if (TNsot > 1) {
      throw new ErrorException("CADI does not support more than one tile or tile-part.");
    }

    thix.tileHeaderLength = (int)(in.getPos() - thix.tileHeaderFilePointer);
  }

  /**
   * Reads the SOD marker.
   *
   * @throws ErrorException when the SOD marker is incorrect
   */
  private void readSOD() throws ErrorException {
    boolean markerFound = false;
    boolean SODmarkerFound = false;
    if (readByte() == 0xFF) {
      markerFound = true;
    } else {
      throw new ErrorException("Marker expected after SOT.");
    }

    while (!SODmarkerFound) {
      while (!markerFound) {
        markerFound = (readByte() == 0xFF);
      }
      markerFound = false;
      switch (readByte()) {
        case 0x52://COD marker
          throw new ErrorException("CADI cannot decode codestreams with a COD marker in the tile-part header.");
        case 0x53://COC marker
          throw new ErrorException("CADI cannot decode codestreams with a COC marker in the tile-part header.");
        case 0x5C://QCD marker
          throw new ErrorException("CADI cannot decode codestreams with a QCD marker in the tile-part header.");
        case 0x5D://QCC marker
          throw new ErrorException("CADI cannot decode codestreams with a QCC marker in the tile-part header.");
        case 0x5E://RGN marker
          throw new ErrorException("CADI cannot decode codestreams with a RGN marker.");
        case 0x5F://POC marker
          throw new ErrorException("CADI cannot decode codestreams with a POC marker.");
        case 0x58://PLT marker
          throw new ErrorException("CADI cannot decode codestreams with a PLT marker.");
        case 0x61://PPT marker
          throw new ErrorException("CADI cannot decode codestreams with a PPT marker.");
        case 0x64://COM marker
          System.out.println("Comment header will not be considered in this version of CADI");
          break;
        case 0x93://SOD marker
          SODmarkerFound = true;
          break;
      }
    }
  }

  /**
   * Finds the maximum number of precincts in the wide dimension across all
   * components.
   *
   * @return
   */
  private int numMaxPrecinctsWide() {
    int numPrecincts = -1;
    int maxComps = codestream.getZSize();
    int tmp = -1;

    for (int z = 0; z < maxComps; z++) {
      componentObj = tileObj.getComponent(z);
      int maxRLevels = componentObj.getWTLevels() + 1;
      for (int rLevel = 0; rLevel < maxRLevels; rLevel++) {
        rLevelObj = componentObj.getResolutionLevel(rLevel);
        tmp = rLevelObj.getNumPrecinctsWide();
        if (numPrecincts < tmp) {
          numPrecincts = tmp;
        }
      }
    }

    return numPrecincts;
  }

  /**
   * Finds the maximum number of precincts in the heigh dimension across all
   * components.
   *
   * @return
   */
  private int numMaxPrecinctsHeigh() {
    int numPrecincts = -1;
    int maxComps = codestream.getZSize();
    int tmp = -1;

    for (int z = 0; z < maxComps; z++) {
      componentObj = tileObj.getComponent(z);
      int maxRLevels = componentObj.getWTLevels() + 1;
      for (int rLevel = 0; rLevel < maxRLevels; rLevel++) {
        rLevelObj = componentObj.getResolutionLevel(rLevel);
        tmp = rLevelObj.getNumPrecinctsHeigh();
        if (numPrecincts < tmp) {
          numPrecincts = tmp;
        }
      }
    }

    return numPrecincts;
  }

  /**
   * Find the maximum precincts of all ressolution levels and all components
   *
   * @return an int which represents the maximum number of precincts across all resolution levels and all components.
   */
  private int maxPrecinct() {
    int maxPrecincts = -1;
    int maxComps = codestream.getZSize();
    int tmp = -1;

    for (int z = 0; z < maxComps; z++) {
      componentObj = tileObj.getComponent(z);
      int maxRLevels = componentObj.getWTLevels() + 1;
      for (int rLevel = 0; rLevel < maxRLevels; rLevel++) {
        rLevelObj = componentObj.getResolutionLevel(rLevel);
        tmp = rLevelObj.getNumPrecincts();
        if (maxPrecincts < tmp) {
          maxPrecincts = tmp;
        }
      }
    }
    return maxPrecincts;
  }

  /**
   * Find the correspondence in width of a precinct
   *
   * @param z component to know the correspondence
   * @param precinct which to know the correspondence, always the biggest resolution level
   * @param rLevel to know the correspondence
   * @param maxRlevel
   *
   * @return the correspondence of a precinct
   */
  private double precinctCorrespondenceX(int z, int precinct, int rLevel, int maxRlevel) {
    double precinctCorrespondence = -1;
    int xPrecinctRlevel = tileObj.getComponent(z).getResolutionLevel(0).getNumPrecinctsWide();
    int xPrecinctMaxRlevel = xPrecinctRlevel;

    int numPrecinctsRLevel = tileObj.getComponent(z).getResolutionLevel(rLevel).getNumPrecinctsWide();
    while (xPrecinctRlevel < numPrecinctsRLevel) {
      xPrecinctRlevel = xPrecinctRlevel * 2;
    }

    int numPrecinctsMaxRLevel = tileObj.getComponent(z).getResolutionLevel(maxRlevel - 1).getNumPrecinctsWide();
    while (xPrecinctMaxRlevel < numPrecinctsMaxRLevel) {
      xPrecinctMaxRlevel = xPrecinctMaxRlevel * 2;
    }

    precinctCorrespondence = Math.floor(precinct / (xPrecinctMaxRlevel / xPrecinctRlevel));

    return (precinctCorrespondence);
  }

  /**
   * Find the correspondence in hight of a precinct
   *
   * @param z component to know the correspondence
   * @param precinct which to knOw the correspondence
   * @param rLevel to know the correspondence
   * @param maxRlevel
   *
   * @return the correspondence of a precinct always from a greater rlevel
   */
  private double precinctCorrespondenceY(int z, int precinct, int rLevel, int maxRlevel) {
    double precinctCorrespondence = -1;
    int yPrecinctRlevel = tileObj.getComponent(z).getResolutionLevel(0).getNumPrecinctsHeigh();
    int yPrecinctMaxRlevel = yPrecinctRlevel;

    int numPrecinctsRLevel = tileObj.getComponent(z).getResolutionLevel(rLevel).getNumPrecinctsHeigh();
    while (yPrecinctRlevel < numPrecinctsRLevel) {
      yPrecinctRlevel = yPrecinctRlevel * 2;
    }

    int numPrecinctsMaxRLevel = tileObj.getComponent(z).getResolutionLevel(maxRlevel - 1).getNumPrecinctsHeigh();
    while (yPrecinctMaxRlevel < numPrecinctsMaxRLevel) {
      yPrecinctMaxRlevel = yPrecinctMaxRlevel * 2;
    }

    precinctCorrespondence = Math.floor(precinct / (yPrecinctMaxRlevel / yPrecinctRlevel));

    return (precinctCorrespondence);
  }

  /**
   * Find the biggest resolution level within all the components
   *
   * @return an integer which represents the maximum number of resolution levels
   * across all the components.
   */
  private int maxRlevel() {
    int maxRLevel = -1;
    int maxTiles = codestream.getNumTiles();
    int maxComps = codestream.getZSize();
    int rLevels = 0;

    for (int t = 0; t < maxTiles; t++) {
      for (int c = 0; c < maxComps; c++) {
        rLevels = codestream.getTile(t).getComponent(c).getWTLevels()+1;
        if (rLevels > maxRLevel) {
          maxRLevel = rLevels;
        }
      }
    }

    return maxRLevel;
  }
}