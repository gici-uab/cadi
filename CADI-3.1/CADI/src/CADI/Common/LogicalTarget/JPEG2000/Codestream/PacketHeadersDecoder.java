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
package CADI.Common.LogicalTarget.JPEG2000.Codestream;

import CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2KPrecinct;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2KTile;
import GiciException.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class performs the packet header decoding.
 * <p/>
 * Usage: example:<br>
 * &nbsp; construct <br>
 * &nbsp; [reset] <br>
 * &nbsp; packetHeaderDecoding <br>
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.2.1 2012/03/12
 */
public class PacketHeadersDecoder {

  /**
   * 
   */
  private long inClassIdentifier = -1; // TODO: remove this attribute

  /**
   *
   */
  private JPEG2KCodestream codestream = null;

  /**
   * Is a input stream where bits of packet headers will be read from.
   */
  private PacketHeaderDataInputStream PHDInputStream = null;

  /**
   * Indicates whether the Start of Packet marker is in the codestream and it
   * must be read.
   */
  private boolean SOP = false;

  /**
   * Indicates whether the End of Packet marker is in the codestream and it
   * must be read.
   */
  private boolean EPH = false;

  // INTERNAL ATTRIBUTES
  /**
   * This object is used to save the packet headers data objects. It uses the
   * inClassIdentififer (see {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier})
   * as a key.
   */
  private HashMap<Long, PacketHeaderDataDecoder> packetHeaders = null;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Codestream.PacketHeaderDataDecoder#TTInclusionInformation}.
   */
  private TagTreeDecoder[] TTInclusionInformation = null;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Codestream.PacketHeaderDataDecoder#TTZeroBitPlanes}.
   */
  private TagTreeDecoder[] TTZeroBitPlanes = null;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Codestream.PacketHeaderDataDecoder#lBlock}.
   */
  private int[][][] lBlock = null;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Codestream.PacketHeaderDataDecoder#firstLayer}.
   */
  private int[][][] firstLayer = null;

  /**
   *
   */
  private int[][][] zeroBitPlanes = null;

  /**
   * Packet sequence number. The first packet in a coded tile is assigned the
   * value 0. When the maximum number -65535- is reached, the number rolls
   * over to zero.
   * <p>
   * Valid values between 0 to 65535.
   */
  private int Nsop = 0;

  private int NsopInHeader;

  // ============================= public methods ==============================
  /**
   * Constructor.
   * <p>
   * This constructor assumes Start Of Packet (SOP) and End of Packet Headers
   * (EPH) are read from the codestream information.
   *
   * @param imageStructure definition in {@link #imageStructure}
   */
  public PacketHeadersDecoder(JPEG2KCodestream codestream) {
    this(codestream, codestream.useSOP(), codestream.useEPH());
  }

  /**
   * Constructor.
   *
   * @param imageStructure definition in {@link #imageStructure}
   * @param sop definition in {@link #SOP}
   * @param eph definition in {@link #EPH}
   */
  public PacketHeadersDecoder(JPEG2KCodestream codestream,
                              boolean sop, boolean eph) {

    if (codestream == null) {
      throw new NullPointerException();
    }

    this.codestream = codestream;
    this.SOP = sop;
    this.EPH = eph;

    packetHeaders = new HashMap<Long, PacketHeaderDataDecoder>();
    PHDInputStream = new PacketHeaderDataInputStream();
    Nsop = 0;
  }

  /**
   * Sets the internal attributes (states) to its initial values.
   *
   * @throws ErrorException when the internal attributes cannot be reseted.
   */
  public void reset() throws ErrorException {
    for (Map.Entry<Long, PacketHeaderDataDecoder> entry : packetHeaders.entrySet()) {
      entry.getValue().reset();
    }
  }

  /**
   * Decodes the packet header for a precinct of a given layer, z, rLevel, precinct
   *
   * @param PHDataInputStream a input stream where packet header is read from.
   * @param inClassIdentifier definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}.
   *
   * @return A vector which will contain the data length for a given subband, yBlock, xBlock and coding pass
   *         The index are: [subband][yBlock][xBlock][codingPasses] = data length
   *
   * @throws ErrorException when the packet header is corrupted
   * @throws EOFException if the end of the input stream has been reached.
   * @throws IOException if a I/O error has ocurred.
   */
  public int[][][][] packetHeaderDecoding(
          PacketHeaderDataInputStream BuffInputStream, long inClassIdentifier)
          throws ErrorException, EOFException, IOException {

    // Check input parameters
    if (inClassIdentifier < 0) throw new IllegalArgumentException();
    if (BuffInputStream == null) throw new NullPointerException();
    
    // Copy input parameters
    this.PHDInputStream = BuffInputStream;
    this.inClassIdentifier = inClassIdentifier;

    // Get packet header data
    PacketHeaderDataDecoder packetHeaderData = packetHeaders.get(inClassIdentifier);
    if (packetHeaderData == null) { // Packet has not been initialized
      int[] TCRP = codestream.findTCRP(inClassIdentifier);
      JPEG2KTile tileObj = codestream.getTile(TCRP[0]);
      JPEG2KComponent compObj = tileObj.getComponent(TCRP[1]);
      JPEG2KResolutionLevel rLevelObj = compObj.getResolutionLevel(TCRP[2]);
      JPEG2KPrecinct precinctObj = rLevelObj.getPrecinct(TCRP[3]);

      packetHeaderData = new PacketHeaderDataDecoder(precinctObj);
      packetHeaders.put(inClassIdentifier, packetHeaderData);
    }

    TTInclusionInformation = packetHeaderData.TTInclusionInformation;
    TTZeroBitPlanes = packetHeaderData.TTZeroBitPlanes;
    lBlock = packetHeaderData.lBlock;
    firstLayer = packetHeaderData.firstLayer;
    zeroBitPlanes = packetHeaderData.zeroBitPlanes;

    int layer = ++packetHeaderData.layerToDecode;


    int subband, yBlock, xBlock, lblock, codingPasses, wmsb, wmsb_temp;
    int[][][][] precinctData = null;
    boolean includeBlock = true;

    // Read Start of Packet Marker
    if (SOP) {
      readSOP();
    }

    if (getBit() == 0) {
      // Packet is empty
      return precinctData;
    }
    
    precinctData = new int[zeroBitPlanes.length][][][];
    for (subband = 0; subband < zeroBitPlanes.length; subband++) {
      precinctData[subband] = new int[zeroBitPlanes[subband].length][][];
      
      for (yBlock = 0; yBlock < zeroBitPlanes[subband].length; yBlock++) {
        precinctData[subband][yBlock] = new int[zeroBitPlanes[subband][yBlock].length][];

        for (xBlock = 0; xBlock < zeroBitPlanes[subband][yBlock].length; xBlock++) {

          includeBlock = true;
          // Code-block inclusion information

          // First inclusion of the code block
          if (lBlock[subband][yBlock][xBlock] == 0) {
            firstLayer[subband][yBlock][xBlock] =
                    TTInclusionInformation[subband].Decoder(layer + 1, yBlock, xBlock, this.PHDInputStream);
            includeBlock = false;

            if (firstLayer[subband][yBlock][xBlock] <= layer) {

              if (firstLayer[subband][yBlock][xBlock] == layer) {
                lBlock[subband][yBlock][xBlock] = 3;

                wmsb = wmsb_temp = 1;
                while (wmsb >= wmsb_temp) {
                  wmsb = TTZeroBitPlanes[subband].Decoder(++wmsb_temp, yBlock, xBlock, this.PHDInputStream);
                }
                zeroBitPlanes[subband][yBlock][xBlock] = wmsb;
                
                includeBlock = true;
              }
            } else {
              includeBlock = false;
            }
          } else if (getBit() == 0) {
            includeBlock = false;
            //precinctData[subband][yBlock] = null;
          }

          if (includeBlock) {
            // Decode number of coding passes
            codingPasses = DecodeCodingPasses();
            precinctData[subband][yBlock][xBlock] = new int[codingPasses];

            // Decode LBlock
            lblock = DecodeLblock();
            lBlock[subband][yBlock][xBlock] += lblock;

            // Decode length of codeword segment
            precinctData[subband][yBlock][xBlock] = DecodeLengths(lBlock[subband][yBlock][xBlock], codingPasses);
          }
        }
      }
    }


    //	Read End of Packet Marker
    if (EPH) {
      readEPH();
    }

    return (precinctData);
  }

  /**
   * Decodes the packet header for a precinct of a given layer, z, rLevel, precinct
   *
   * @param PHDataInputStream a input stream where packet header is read from.
   * @param inClassIdentifier definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}.
   *
   * @return A vector which will contain the data length for a given subband, yBlock, xBlock and coding pass
   *         The index are: [subband][yBlock][xBlock][0- number of coding passes, 1- data length]
   *
   * @throws ErrorException when the packet header is corrupted
   * @throws EOFException if the end of the input stream has been reached.
   * @throws IOException if a I/O error has ocurred.
   */
  public int[][][][] packetHeaderDecodingNew(
          PacketHeaderDataInputStream BuffInputStream, long inClassIdentifier)
          throws ErrorException, EOFException, IOException {

    // Check input parameters
    if (inClassIdentifier < 0) {
      throw new IllegalArgumentException();
    }
    if (BuffInputStream == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.PHDInputStream = BuffInputStream;
    this.inClassIdentifier = inClassIdentifier;
    //this.PHDInputStream.setInput(PHDataInputStream);
    //this.PHDInputStream.resetGetTagBit();

    // Get packet header data
    PacketHeaderDataDecoder packetHeaderData = packetHeaders.get(inClassIdentifier);
    if (packetHeaderData == null) { // Packet has not been initialized
      int[] TCRP = codestream.findTCRP(inClassIdentifier);
      JPEG2KTile tileObj = codestream.getTile(TCRP[0]);
      JPEG2KComponent compObj = tileObj.getComponent(TCRP[1]);
      JPEG2KResolutionLevel rLevelObj = compObj.getResolutionLevel(TCRP[2]);
      JPEG2KPrecinct precinctObj = rLevelObj.getPrecinct(TCRP[3]);

      //packetHeaderData = new PacketHeaderDataDecoder(precinctObj.getSubbandsStructure());
      packetHeaderData = new PacketHeaderDataDecoder(precinctObj);
      packetHeaders.put(inClassIdentifier, packetHeaderData);
    }

    TTInclusionInformation = packetHeaderData.TTInclusionInformation;
    TTZeroBitPlanes = packetHeaderData.TTZeroBitPlanes;
    lBlock = packetHeaderData.lBlock;
    firstLayer = packetHeaderData.firstLayer;
    zeroBitPlanes = packetHeaderData.zeroBitPlanes;

    int layer = ++packetHeaderData.layerToDecode;


    int subband, yBlock, xBlock, lblock, codingPasses, wmsb, wmsb_temp;
    int[][][][] precinctData = null;
    boolean includeBlock = true;

    // Read Start of Packet Marker
    if (SOP) {
      readSOP();
    }

    //System.out.println("PACKET HEADER DECODING");
    //System.out.println("Layer: "+ layer+" Component: "+z+" Resol. Level: "+rLevel+" Precinct: "+precinct);

    //System.out.println("Decodificando cabecera...");

    //System.out.print("Packet header:");
    // Zero/non-zero packet length

    if (getBit() == 0) {
      // Faltaria actualizar las variables de salida
      //System.out.println(" Packet empty");
      return (precinctData);
    }
    //System.out.println(" Packet non empty");

    precinctData = new int[zeroBitPlanes.length][][][];
    // Loop subbands in resolution level
    for (subband = 0; subband < zeroBitPlanes.length; subband++) {
      //System.out.println("--> Subband: " + subband);
      precinctData[subband] = new int[zeroBitPlanes[subband].length][][];
      for (yBlock = 0; yBlock < zeroBitPlanes[subband].length; yBlock++) {
        precinctData[subband][yBlock] = new int[zeroBitPlanes[subband][yBlock].length][2];
        //System.out.println("--> yBlock="+ yBlock);
        for (xBlock = 0; xBlock < zeroBitPlanes[subband][yBlock].length; xBlock++) {
          //System.out.println("--> xBlock="+ xBlock);
          //System.out.println("\tSubband: "+subband + " yBlock: " + yBlock+" xBlock: " +xBlock);

          includeBlock = true;
          // Code-block inclusion information
          //System.out.print("Inclusion Information:");

          // First inclusion of the code block
          if (lBlock[subband][yBlock][xBlock] == 0) {
            //System.out.println(TTInclusionInformation[z][rLevel][precinct][subband].toString());
            firstLayer[subband][yBlock][xBlock] =
                    TTInclusionInformation[subband].Decoder(layer + 1, yBlock, xBlock, this.PHDInputStream);
            //System.out.print("Tag Tree");
            includeBlock = false;
            //System.out.println("First Layer=" + PBFirstLayer[z][rLevel][precinct][subband][yBlock][xBlock]);
            if (firstLayer[subband][yBlock][xBlock] <= layer) {

              if (firstLayer[subband][yBlock][xBlock] == layer) {
                //System.out.println(" --> Se incluye en este paquete por primera vez");
                lBlock[subband][yBlock][xBlock] = 3;

                wmsb = wmsb_temp = 1;
                while (wmsb >= wmsb_temp) {
                  wmsb = TTZeroBitPlanes[subband].Decoder(++wmsb_temp, yBlock, xBlock, this.PHDInputStream);
                }
                zeroBitPlanes[subband][yBlock][xBlock] = wmsb;
                //System.out.println("z="+z+" rLevel="+ rLevel+" precinct="+precinct+" subband="+subband+" yBlock="+yBlock+" xBlock="+xBlock);
                //System.out.println("Bit Planes Null: "+wmsb);
                includeBlock = true;
              }
            } else {
              includeBlock = false;
            }
          } else if (getBit() == 0) {
            //System.out.println("No se incluye en este layer: ");
            includeBlock = false;
            //precinctData[subband][yBlock] = null;
          }

          if (includeBlock) {
            //System.out.println("-> Se incluye en este layer: ");

            // Decode number of coding passes
            precinctData[subband][yBlock][xBlock][0] = DecodeCodingPasses();
            //System.out.println("Coding Passes: " + codingPasses);

            // Decode LBlock
            lblock = DecodeLblock();
            lBlock[subband][yBlock][xBlock] += lblock;
            //System.out.println("Lblock: " + lBlock[z][rLevel][precinct][subband][yBlock][xBlock]);
            //System.out.println("z="+z+" rLevel="+rLevel+" precinct="+precinct+" subband="+subband+" yBlock="+yBlock+" xBlock="+xBlock+" ->LBlock= " +lBlock[z][rLevel][precinct][subband][yBlock][xBlock]);

            // Decode length of codeword segment
            precinctData[subband][yBlock][xBlock][1] = DecodeLengthsNew(lBlock[subband][yBlock][xBlock], precinctData[subband][yBlock][xBlock][0]);

            //for (int i=0; i< precinctData[subband][yBlock][xBlock].length; i++) System.out.println("\tCoding Pass: "+ i+" ->Length Encoding: " +precinctData[subband][yBlock][xBlock][i]);
            //for (int i=0; i< precinctData[subband][yBlock][xBlock].length; i++) {
            //System.out.println("z="+z+" rLevel="+rLevel+" precinct="+precinct+" subband="+subband+" yBlock="+yBlock+" xBlock="+xBlock+" CP="+ i+" ->Length Encoding: " +precinctData[subband][yBlock][xBlock][i]);
            //}

            //System.out.println();
          }
        } // xBlock
      } // yBlock
    } // Subband


    //	Read End of Packet Marker
    if (EPH) {
      readEPH();
    }

    return (precinctData);
  }

  /**
   * Returns the zero bit planes for a tile-component-resolution-precinct.
   *
   * @param inClassIdentifier
   *
   * @return definition in {@link CADI.Common.LogicalTarget.JPEG2000.Codestream.PacketHeaderDataDecoder#zeroBitPlanes}
   *
   * @throws IllegalAccessException
   */
  public int[][][] getZeroBitPlanes(long inClassIdentifier) throws IllegalAccessException {
    PacketHeaderDataDecoder packetHeaderData = packetHeaders.get(inClassIdentifier);
    if (packetHeaderData == null) {
      throw new IllegalAccessException("Packet header has not been initialized for this identifier");
    }

    return packetHeaderData.zeroBitPlanes;
  }

  /**
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";
    str = getClass().getName() + " [";
    for (long inClassIdentifier : packetHeaders.keySet()) {
      str += " precinct=" + inClassIdentifier;
      packetHeaders.get(inClassIdentifier).toString();
    }
    str += "]";

    return str;
  }

  /**
   * Prints this Packet headers Decoder's fields to the specified output
   * stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {
    out.println("-- Packet Headers Decoder --");

    for (long inClassIdentifier : packetHeaders.keySet()) {
      out.println("Precinct: " + inClassIdentifier);
      packetHeaders.get(inClassIdentifier).list(out);
    }

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * Decodify the Lblock value
   *
   * @return lblock value
   *
   * @throws ErrorException when the packet header is corrupted
   */
  private int DecodeLblock() throws EOFException, IOException {

    int LblockValue = 0;

    while (getBit() == 1) {
      LblockValue++;
    }

    return (LblockValue);
  }

  /**
   * Decodify the length of codeword segment
   *
   * @param lblock        current value
   * @param codingPasses  coding passes number of the codeblock
   * @return              length of code-block
   *
   * @throws ErrorException when the packet header is corrupted
   */
  private int[] DecodeLengths(int lblock, int codingPasses) throws EOFException, IOException {

    int numBits;
    int codingPassesAdded = 1;	// OBS: codingPassesAdded=1 because codeword segment is terminated at each coding pass
    int[] lengthInformation = new int[codingPasses];

    for (int cp = 0; cp < codingPasses; cp++) {
      lengthInformation[cp] = 0;
      numBits = lblock + (int) Math.floor(Math.log(codingPassesAdded) / Math.log(2D));
      for (int nb = numBits - 1; nb >= 0; nb--) {
        lengthInformation[cp] += (1 << nb) * getBit();
      }
    }

    return (lengthInformation);
  }

  /**
   * Decode the length of codeword segment
   *
   * @param lblock current value
   * @param codingPasses number of coding passes within the segment
   *
   * @return length of codeblock
   * @throws ErrorException when the packet header is corrupted
   */
  private int DecodeLengthsNew(int lblock, int codingPasses) throws EOFException, IOException {
    int numBits;
    int length = 0;

    numBits = lblock + (int) Math.floor(Math.log(codingPasses) / Math.log(2D));
    for (int nb = numBits - 1; nb >= 0; nb--) {
      length += (1 << nb) * getBit();
    }
    return (length);
  }

  /**
   * Decodify the coding passes
   *
   * @return coding passes
   *
   * @throws ErrorException when the packet header is corrupted
   */
  private int DecodeCodingPasses() throws EOFException, IOException {

    int codingPasses = 0;

    if (getBit() == 0) {
      codingPasses = 1;
    } else {
      if (getBit() == 0) {
        codingPasses = 2;
      } else {

        // Will be more than 2
        if (getBit() == 0) {

          if (getBit() == 0) {
            codingPasses = 3;
          } else {
            codingPasses = 4;
          }
        } else {

          if (getBit() == 0) {
            codingPasses = 5;
          } else {
            // Will be more than 5
            for (int i = 4; i >= 0; i--) {
              codingPasses += (1 << i) * getBit();
            }
            if (codingPasses <= 30) {
              codingPasses += 6;
            } else {
              // Will be more than 36
              codingPasses = 0;
              for (int i = 6; i >= 0; i--) {
                codingPasses += (1 << i) * getBit();
              }
              codingPasses += 37;
            }
          }
        }
      }
    }

    return (codingPasses);
  }

  /**
   * Reads SOP marker if it is.
   *
   * @throws ErrorException when the file cannot be load
   */
  private void readSOP() throws ErrorException, EOFException, IOException {
    boolean markerFound = false;
    boolean SOPMarkerFound = false;

    // Read SOP marker
    if (Nsop == 0) {
      while (!SOPMarkerFound) {
        while (!markerFound) {
          markerFound = (PHDInputStream.read() == 0xFF);
        }
        markerFound = false;
        SOPMarkerFound = (PHDInputStream.read() == 0x91);
      }
      SOPMarkerFound = false;
    } else {
      markerFound = (PHDInputStream.read() == 0xFF);
      SOPMarkerFound = (PHDInputStream.read() == 0x91);
      if ((markerFound == false) || (SOPMarkerFound == false)) {
        throw new ErrorException("Error reading CodeStream, expected SOP and it's not found.");
      }
    }
    // Read Lsop marker
    if ((PHDInputStream.read() == 0x00) == false) {
      throw new ErrorException("Error reading CodeStream, expected SOP and it's not found.");
    }
    if ((PHDInputStream.read() == 0x04) == false) {
      throw new ErrorException("Error reading CodeStream, expected SOP and it's not found.");
    }
    // Read Nsop marker
    NsopInHeader = (PHDInputStream.read() << 8) | PHDInputStream.read();
    if (NsopInHeader != Nsop) {
      throw new ErrorException("Error reading CodeStream, expected SOP and it's not found.");
    }
    Nsop = Nsop == 0xFFFF ? 0 : ++Nsop;

  }

  /**
   * Reads EPH marker if it is.
   *
   * @throws ErrorException when the file cannot be load
   */
  private void readEPH() throws ErrorException, EOFException, IOException {

    if ((PHDInputStream.read() == 0xFF) == false) {
      throw new ErrorException("Error reading CodeStream, expected EPH and it's not found.");
    }
    if ((PHDInputStream.read() == 0x92) == false) {
      throw new ErrorException("Error reading CodeStream, expected EPH and it's not found.");
    }
  }

  /**
   * Returns the bit readed from the file.
   *
   * @return an integer that represents the bit readed from the file
   */
  private int getBit() throws EOFException, IOException {
    return PHDInputStream.getTagBit();
  }
}
