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
package CADI.Server.LogicalTarget.JPEG2000.Codestream;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import GiciException.*;

/**
 * This class implements the packet headers encoder. This class is implemented
 * to store only information of those packets which have been initialize, so
 * only information about these packets will be required.
 * <p>
 * Before any call to the {@link #encodePacketHeader(long, int[][][][])} method
 * (to encode the packet header of a precinct) is is necessary the
 * {@link #setZeroBitPlanesAndFirstLayer(long, int[][][], int[][][])} method
 * had been called in order to set the first layer and number of zero bit
 * planes for this precinct. Otherwise, an exception will be thrown.
 * <p>
 * Each call to the {@link #encodePacketHeader(long, int[][][][])} method
 * generates the packet header belonging to the next quality layer for
 * the precinct passed as the first function's parameter.  
 * 
 * <p>
 * Usage: example:<br>
 * &nbsp; construct<br>
 * &nbsp; [setParameters]<br>
 * &nbsp; setZeroBitPlanesAndFirstLayer<br>
 * &nbsp; encodePacketHeader<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.2.0 2008/12/27
 */
public class PacketHeadersEncoder {

  /**
   * It refers to the zero length packet coding (empty packets). There are two possibilities:
   * &nbsp; 1- emptyHeaderBit = true: a 0 value at first bit in packet header.
   * &nbsp; 2- emptyHeaderBit = false: a 1 value at first bit in	packet header, and then a 0 value for each	packet	in the inclusion information.
   */
  private boolean emptyHeaderBit = true;

  //INTERNAL ATTRIBUTES
  /**
   * This object is used to save the packet headers data objects. It uses the
   * inClassIdentififer (see {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier})
   * as a key. 
   */
  private Map<Long, PacketHeaderDataEncoder> packetHeaders = null;

  /**
   * ByteStream to built each packet header
   */
  private PacketHeaderDataOutputStream packetHeaderDataOutputStream = null;

  /**
   * Definition in {@link CADI.Server.LogicalTarget.JPEG2000.Codestream.PacketHeaderDataEncoder#TTInclusionInformation}.
   */
  private TagTreeEncoder[] TTInclusionInformation = null;

  /**
   * Definition in {@link CADI.Server.LogicalTarget.JPEG2000.Codestream.PacketHeaderDataEncoder#TTZeroBitPlanes}.
   */
  private TagTreeEncoder[] TTZeroBitPlanes = null;

  /**
   * Definition in {@link CADI.Server.LogicalTarget.JPEG2000.Codestream.PacketHeaderDataEncoder#lBlock}.
   */
  private int[][][] lBlock = null;

  /**
   * Definition in {@link CADI.Server.LogicalTarget.JPEG2000.Codestream.PacketHeaderDataEncoder#firstLayer}.
   */
  public int[][][] firstLayer = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public PacketHeadersEncoder() {
    this(true);
  }

  /**
   * Constructor.
   * 
   * @param emptyHeaderBit definition in {@link #emptyHeaderBit}.
   */
  public PacketHeadersEncoder(boolean emptyHeaderBit) {
    this.emptyHeaderBit = emptyHeaderBit;
    packetHeaders = new HashMap<Long, PacketHeaderDataEncoder>();
  }

  /**
   * Set the {@link #emptyHeaderBit} attribute.
   *
   * @param emptyHeaderBit definition in {@link #emptyHeaderBit}.
   */
  public void setParameters(boolean emptyHeaderBit) {
    this.emptyHeaderBit = emptyHeaderBit;
  }

  /**
   * Sets the zero bit planes and first layer for a precinct.
   * <p>
   * This method must be used only when objects are build using the default
   * constructor. Moreover, it has to be called once before the
   * {@link #encodePacketHeader(long, int, int, int, int[][][][])} method
   * was called for each precinct.
   * <p>
   * If it is called more than once, an exception will be thrown. Therefore,
   * in order to avoid it, it is recommended to perform a call to the
   * {@link #isSet(long)} method to check if state data has been set.
   * 
   * @param zeroBitPlanes
   * @param firstLayer
   * 
   * @throws ErrorException 
   */
  public void setZeroBitPlanesAndFirstLayer(long inClassIdentifier, int[][][] zeroBitPlanes, int[][][] firstLayer) throws ErrorException {
    if (inClassIdentifier < 0) {
      throw new IllegalArgumentException();
    }
    if ((zeroBitPlanes == null) || (firstLayer == null)) {
      throw new NullPointerException();
    }
    if (packetHeaders.get(inClassIdentifier) != null) {
      throw new ErrorException("Parameters for this precinct (" + inClassIdentifier + ") has been previously set.");
    }

    PacketHeaderDataEncoder packetHeaderData = new PacketHeaderDataEncoder(zeroBitPlanes, firstLayer);
    packetHeaders.put(inClassIdentifier, packetHeaderData);
  }

  /**
   * Check whether zero bit planes and first layer has been set for a precinct.
   * 
   * @param inClassIdentifier definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}.
   * 
   * @return <code>true</code> if data has been set. Otherwise, returns
   *  <code>false</code>.
   */
  public boolean isSet(long inClassIdentifier) {
    return (packetHeaders.get(inClassIdentifier) != null ? true : false);
  }

  /**
   * Encodes the packet header data in a byte array for the precinct which is
   * referenced to as layer - component - resolution level - precinct, and 
   * whose coding passed are passed in the <code>codingPassesAdded</code>
   * variable.
   * <p>
   * The buffer where the packet header are encoded can be retrieved through
   * the <code>getPacketHeaderBuffer</code> method.
   * 
   * @param inClassIdentifier definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}.
   * @param codingPassesAdded is a multi-dimensional array with the lengths
   * 			 of each coding-pass which will be included in this packet.
   * 			 Indexes means:<br>
   * 			 &nbsp; subband: is the subband in the precinct
   * 			 &nbsp; yBlock: is the yBlock in the subband
   * 			 &nbsp; xBlock: is the xBlock in the subband
   * 			 &nbsp; coding pass: coding passes to be added in this packet
   * @return an one-dimensional array of bytes with the encoded packet header. 
   * 
   * @throws ErrorException when an error ocurrs in PacketHeadingCoding
   * @throws IllegalAccessException 
   */
  public byte[] encodePacketHeader(long inClassIdentifier, int[][][][] codingPassesAdded) throws ErrorException, IllegalAccessException {

    // Check input parameters
    if (inClassIdentifier < 0) {
      throw new IllegalArgumentException();
    }
    if (codingPassesAdded == null) {
      throw new NullPointerException();
    }


    // Get data for the precint from the packet data object
    PacketHeaderDataEncoder packetHeaderData = packetHeaders.get(inClassIdentifier);
    if (packetHeaderData == null) { // If packet data object has not been created, setZeroBitPlanesAndFirstLayer method has not been called
      throw new IllegalAccessException("The setZeroBitPlanesAndFirstLayer method has not been called for this precinct");
    }
    TTInclusionInformation = packetHeaderData.TTInclusionInformation;
    TTZeroBitPlanes = packetHeaderData.TTZeroBitPlanes;
    lBlock = packetHeaderData.lBlock;
    firstLayer = packetHeaderData.firstLayer;
    packetHeaderData.lastEncodedLayer++;

    // Definition of variables
    int subband, yBlock, xBlock;
    int lblock, codingPasses;
    boolean emptyPacket = true, includeBlock = true;


    packetHeaderDataOutputStream = new PacketHeaderDataOutputStream();

    int layer = packetHeaderData.lastEncodedLayer;
    //System.out.println("\t\t   - layer= "  + layer);

    try {
      if (emptyHeaderBit) {
        // Find if none coding pass is added.
        for (subband = 0; subband < codingPassesAdded.length && emptyPacket; subband++) {
          for (yBlock = 0; yBlock < codingPassesAdded[subband].length && emptyPacket; yBlock++) {
            for (xBlock = 0; xBlock < codingPassesAdded[subband][yBlock].length && emptyPacket; xBlock++) {
              if (codingPassesAdded[subband][yBlock][xBlock] != null) {
                emptyPacket = (codingPassesAdded[subband][yBlock][xBlock].length > 0) ? false : true;
              }
            }
          }
        }
        // Packet is empty
        if (emptyPacket) {
          packetHeaderDataOutputStream.emitTagBit((byte) 0);
          //System.out.println("Packet is empty");
        }
      } else {
        // Packet is empty
        if (codingPassesAdded.length == 0) {
          packetHeaderDataOutputStream.emitTagBit((byte) 0);
          //System.out.println("Packet is empty");
        }
      }

      if (!emptyPacket) {
        //System.out.println("Packet is non empty");
        // Packet non empty
        packetHeaderDataOutputStream.emitTagBit((byte) 1);

        // Loop subbands in resolution level
        for (subband = 0; subband < codingPassesAdded.length; subband++) {
          for (yBlock = 0; yBlock < codingPassesAdded[subband].length; yBlock++) {
            for (xBlock = 0; xBlock < codingPassesAdded[subband][yBlock].length; xBlock++) {
              //if(codingPassesAdded[subband][yBlock][xBlock] != null ){
              //System.out.println("   subband=" + subband+" (yblock, xblock) = (" + yBlock + "," + xBlock + ")");

              // Code-block inclusion information					
              if (lBlock[subband][yBlock][xBlock] == 0) {
                includeBlock = false;
                TTInclusionInformation[subband].encoder(packetHeaderDataOutputStream, layer + 1, yBlock, xBlock);

                // First inclusion of the code block => signalling the number of missing most significant bit planes
                //System.out.println("\t\t  firstLayer=" + firstLayer[subband][yBlock][xBlock] + " layer=" + layer); // DEBUG
                if (firstLayer[subband][yBlock][xBlock] == layer) {
                  //System.out.println("First inclusion > firstLayer=" + firstLayer[z][rLevel][precinct][subband][yBlock][xBlock]+" layer="+layer);
                  //System.out.print("\nBit Planes Null: " + mostBitPlanesNull[z][rLevel][precinct][subband][yBlock][xBlock] + "-->");
                  lBlock[subband][yBlock][xBlock] = 3;
                  TTZeroBitPlanes[subband].encoder(packetHeaderDataOutputStream, Integer.MAX_VALUE, yBlock, xBlock);
                  //TTZeroBitPlanes[z][rLevel][precinct][subband].Visualiza(); System.out.println();

                  includeBlock = true;
                } else {
                  includeBlock = false;
                }
              } // Code block include in this layer but it has already been included in a previus layer
              else {
                if (codingPassesAdded[subband][yBlock][xBlock] != null) {
                  //System.out.println("   Se incluye en este layer -->");
                  packetHeaderDataOutputStream.emitTagBit((byte) 1);
                  includeBlock = true;
                } else { // Codeblock not included in this layer
                  //System.out.println("   Code-block NO incluido en este layer -->");
                  packetHeaderDataOutputStream.emitTagBit((byte) 0);
                  includeBlock = false;
                }
              }


              // Codeblock included in this layer
              if (includeBlock) {
                //System.out.print("\nCode-block incluido en este layer");
                lblock = lBlock[subband][yBlock][xBlock];
                //System.out.print("\t\t\tPrecID: "+inClassIdentifier+" subband="+subband+" yBlock="+yBlock+" xBlock="+xBlock);
                //System.out.println(" #CP="+codingPassesAdded[subband][yBlock][xBlock].length);
                //for (int cp = 0; cp < codingPassesAdded[subband][yBlock][xBlock].length; cp++) System.out.println("\t\t\tsb="+subband+" yb="+yBlock+" xb="+xBlock+" cp="+cp+" length="+codingPassesAdded[subband][yBlock][xBlock][cp]); // DEBUG
                codingPasses = codingPassesAdded[subband][yBlock][xBlock].length;
                // Encode number of new coding passes
                //System.out.print("\nCoding Passes: "+ codingPasses + "-->");
                encodeCodingPasses(codingPasses);
                //System.out.print("\nLengths: ");
                // Update of code-block length indicator (lBlock) and encode length of codeword segments
                lblock = encodeLengths(lblock, codingPassesAdded[subband][yBlock][xBlock]);
                lBlock[subband][yBlock][xBlock] = lblock;
              }
              //}
            }
          }
        }
      }

      // Align to byte
      packetHeaderDataOutputStream.bitStuffing();

    } catch (IOException ioe) {
      throw new ErrorException();
    }

    // Retun encoded packet header
    return packetHeaderDataOutputStream.getByteArray();
  }

  /**
   * Sets attributes to its initial values.
   */
  public void reset() {
    for (Map.Entry<Long, PacketHeaderDataEncoder> entry : packetHeaders.entrySet()) {
      entry.getValue().reset();
    }
  }

  /**
   * Returns a byte array with the las packet header which has been encoded.
   * The length of the byte array may be larger than the real length of the
   * encoded packet header. Its real length can be obtained with the <code>
   * getLastPacketHeaderLength</code> method.
   * 
   * @return an one-dimensional array with the encoded packet header.
   */
  public byte[] getPacketHeaderBuffer() {
    return packetHeaderDataOutputStream.getByteArray();
  }

  /**
   * Returns the length of the last encoded packet (in bytes).
   * 
   * @return the length of the last encoded packet.
   */
  public int getPacketHeaderLength() {
    return packetHeaderDataOutputStream.getByteArray().length;
  }

  /**
   * Returns the last encoded layer.
   * 
   * @param inClassIdentifier definition in {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}.
   * 
   * @return last encoded layer.
   */
  public int getLastEncodedLayer(long inClassIdentifier) {
    return packetHeaders.get(inClassIdentifier).lastEncodedLayer;
  }

  @Override
  public String toString() {

    String str = "";

    str = getClass().getName() + " [";

    str += "Not implemented yet";

    str += "]";

    return str;
  }

  /**
   * Prints the Packet Headers Encoder data out to the specified output
   * stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Packet Headers Encoder --");

    for (Map.Entry<Long, PacketHeaderDataEncoder> entry : packetHeaders.entrySet()) {
      out.println("In Class Identifier: " + entry.getKey());
      entry.getValue().list(out);
    }

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * Gives a codeword to the value.
   *
   * @param value value to be coded.
   *
   * @throws ErrorException when passed value is not valid
   * @throws IOException 
   */
  private void encodeCodingPasses(int value) throws ErrorException, IOException {
    if ((value < 1) || (value > 164)) {
      throw new ErrorException("Value to encode is too much large.");
    }
    if (value < 6) {
      switch (value) {
        case 1:
          packetHeaderDataOutputStream.emitTagBit((byte) 0);
          break;
        case 2:
          packetHeaderDataOutputStream.emitTagBit((byte) 1);
          packetHeaderDataOutputStream.emitTagBit((byte) 0);
          break;
        case 3:
          packetHeaderDataOutputStream.emitTagBit((byte) 1);
          packetHeaderDataOutputStream.emitTagBit((byte) 1);
          packetHeaderDataOutputStream.emitTagBit((byte) 0);
          packetHeaderDataOutputStream.emitTagBit((byte) 0);
          break;
        case 4:
          packetHeaderDataOutputStream.emitTagBit((byte) 1);
          packetHeaderDataOutputStream.emitTagBit((byte) 1);
          packetHeaderDataOutputStream.emitTagBit((byte) 0);
          packetHeaderDataOutputStream.emitTagBit((byte) 1);
          break;
        case 5:
          packetHeaderDataOutputStream.emitTagBit((byte) 1);
          packetHeaderDataOutputStream.emitTagBit((byte) 1);
          packetHeaderDataOutputStream.emitTagBit((byte) 1);
          packetHeaderDataOutputStream.emitTagBit((byte) 0);
          break;
      }
    } else {
      if (value < 37) {
        for (int i = 0; i < 4; i++) {
          packetHeaderDataOutputStream.emitTagBit((byte) 1);
        }
        value -= 6;
        int mask = 0x00000010;
        for (int i = 0; i < 5; i++) {
          if ((value & mask) == 0) {
            packetHeaderDataOutputStream.emitTagBit((byte) 0);
          } else {
            packetHeaderDataOutputStream.emitTagBit((byte) 1);
          }
          mask = mask >> 1;
        }
      } else {
        int mask = 0x00000040;
        for (int i = 0; i < 9; i++) {
          packetHeaderDataOutputStream.emitTagBit((byte) 1);
        }
        value -= 37;
        for (int i = 0; i < 7; i++) {
          if ((value & mask) == 0) {
            packetHeaderDataOutputStream.emitTagBit((byte) 0);
          } else {
            packetHeaderDataOutputStream.emitTagBit((byte) 1);
          }
          mask = mask >> 1;
        }
      }
    }
  }

  /**
   * Calculate and codify the Lblock value and length of codeword segment
   *
   * @param lblock current value
   * @param dataCodingPasses bytes contributed to a packet by the code-block
   * @return Lblock updated
   * @throws IOException 
   */
  private int encodeLengths(int lblock, int[] dataCodingPasses) throws IOException {

    int bits = 0, temp1, temp2 = 0, mask, codingPasses;
    int lengthInformation, lblockNew = lblock;
    int codingPassesAdded = 1; // OBS: codingPassesAdded=1 <-> codeword segment terminated at each coding pass (will be used with Bypass MQ mode)

    codingPasses = dataCodingPasses.length;

    // Calculate the necessary bits for codify lengths
    for (int cp = 0; cp < codingPasses; cp++) {
      lengthInformation = dataCodingPasses[cp];
      temp1 = (int) Math.ceil(Math.log(lengthInformation + 1) / Math.log(2D));
      temp2 = (int) Math.floor(Math.log(codingPassesAdded) / Math.log(2D));

      if (temp1 > (lblockNew + temp2)) {
        lblockNew += temp1 - (lblockNew + temp2);
      }
    }

    //System.out.print("\nLBlock: "+lblockNew+" -->");
    // Signaling lblock in bitstream
    for (int i = lblock; i < lblockNew; i++) {
      packetHeaderDataOutputStream.emitTagBit((byte) 1);
    }
    packetHeaderDataOutputStream.emitTagBit((byte) 0);

    //Codify the length of each codeword segment
    for (int cp = 0; cp < codingPasses; cp++) {
      lengthInformation = dataCodingPasses[cp];
      //System.out.print("\nCoding Pass: "+ cp +" longitud datos: "+lengthInformation + "-->");
      bits = lblockNew + (int) Math.floor(Math.log(codingPassesAdded) / Math.log(2D));
      mask = 0x1 << bits - 1;

      for (int i = 0; i < bits; i++) {
        if ((lengthInformation & mask) == 0) {
          packetHeaderDataOutputStream.emitTagBit((byte) 0);
        } else {
          packetHeaderDataOutputStream.emitTagBit((byte) 1);
        }
        mask = mask >> 1;
      }
    }
    return (lblockNew);
  }
}