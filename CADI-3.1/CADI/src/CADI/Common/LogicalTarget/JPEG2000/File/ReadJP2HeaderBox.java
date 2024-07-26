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

import CADI.Server.LogicalTarget.JPEG2000.BoxIndexing;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;

import GiciException.ErrorException;
import GiciStream.BufferedDataInputStream;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2008/10/07
 */
public class ReadJP2HeaderBox {

  /**
   * Is an object with the input stream where the main header is read from.
   */
  private BufferedDataInputStream in = null;

  /**
   *
   */
  private long length = 0;

  /**
   * Indicates whether JP2 header box is conform with the Part-1 of the
   * standard or with the Part-2 (extensions)
   */
  private boolean isExtension = false;

  /**
   * Boolean indicating if there are components with different bit depths from others
   */
  private boolean bppVariation;
  
  private BoxIndexing fileIndexing = null;

  // INTERNAL ATTRIBUTES
  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#zSize}
   */
  private int zSize;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#ySize}
   */
  private int ySize;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#xSize}
   */
  private int xSize;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#signed}
   */
  private boolean[] LSSignedComponents = null;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#CTType}
   */
  private int CTType;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#precision}
   */
  private int[] QComponentsBits = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param in input stream where data will be read from.
   * @param length
   */
  public ReadJP2HeaderBox(BufferedDataInputStream in, long length,  BoxIndexing fileIndexing) {
    this(in, length, false, fileIndexing);
  }

  /**
   *
   * @param in
   * @param length
   * @param isExtension
   */
  public ReadJP2HeaderBox(BufferedDataInputStream in, long length, boolean isExtension, BoxIndexing fileIndexing) {
    if (in == null) {
      throw new NullPointerException();
    }
    if (length <= 0) {
      throw new IllegalArgumentException();
    }

    this.in = in;
    this.length = length;
    this.isExtension = isExtension;
    this.fileIndexing = fileIndexing;
  }

  /**
   * @throws IOException
   * @throws ErrorException
   * @throws EOFException
   */
  public void run() throws EOFException, ErrorException, IOException {

    BoxIndexing boxIndexing = null;
    
    // Acquires the resource
    in.lock();

    try {

      long pointerLimit = in.getPos() + length;

      boolean isPaletteBox = false;
      boolean isComponentMappingBox = false;
      boolean isColorSpecificationBox = false;

      //Image Header Box
      bppVariation = readImageHeaderBox();

      while (in.getPos() < pointerLimit) {

        long pos = in.getPos();
        
        // Length
        length = in.readInt();
        long contentBoxLength = 0;

        // Type
        int type = in.readInt();

        // Extended length
        if (length == 1) {
          length = in.readLong();
          contentBoxLength = length - 16;
        } else {
          contentBoxLength = length - 8;
        }
        
        boxIndexing = new BoxIndexing(fileIndexing, type, pos, length, false);
        fileIndexing.addChild(boxIndexing);

        switch (type) {

          case JPXBoxTypes.LABEL_BOX_TYPE:
            if (!isExtension) {
              throw new ErrorException("JPEG2000 Label Box does not conform the standard (not allowed in Part-1).");
            }
            readLabelBox(contentBoxLength);
            break;

          case JPXBoxTypes.BITS_PER_COMPONENT_BOX_TYPE: //Bits Per Component Box
            readBitsPerComponentBox(contentBoxLength);
            break;

          case JPXBoxTypes.COLOR_SPECIFICATION_BOX_TYPE: //Colour Specification Box
            readColourSpecificationBox(contentBoxLength);
            isColorSpecificationBox = true;
            break;

          case JPXBoxTypes.PALETTE_BOX_TYPE:
            readPaletteBox(length);
            isPaletteBox = true;
            break;

          case JPXBoxTypes.COMPONENT_MAPPING_BOX_TYPE:
            readComponentMappingBox(contentBoxLength);
            isComponentMappingBox = true;
            break;

          case JPXBoxTypes.CHANNEL_DEFINITION_BOX_TYPE:
            readChannelDefinitionBox(contentBoxLength);
            break;

          case JPXBoxTypes.RESOLUTION_BOX_TYPE:
            readResolutionSuperBox(contentBoxLength);
            break;

          default:
            throw new ErrorException("JPEG2000 Signature Box does not conform the standard.");
        }
      }

      // Test whether palete box and component mapping box are or not.
      if (isPaletteBox && !isComponentMappingBox) {
        throw new ErrorException("File has \"Palette Box\" but not \"Component Mapping Box\"");
      }
      if (!isPaletteBox && isComponentMappingBox) {
        throw new ErrorException("File has \"Component Mapping Box\" but not \"Palette Box\"");
      }

      // Test whether the color specification box is
      if (isExtension && !isColorSpecificationBox) {
        throw new ErrorException("File has \"Component Mapping Box\" but not \"Palette Box\"");
      }


    } finally {
      in.unlock();
    }
  }

  /**
   *
   * @return the {@link #bppVariation} attribute.
   */
  public boolean isBppVariation() {
    return bppVariation;
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
   * Prints this Read JP2 Header Box fields out to the
   * specified output stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Read JP2 Header Box --");

    out.println("<<< Not implemented yet >>> ");

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * Reads the Label Box and retrieves the parameters contained.
   *
   * @throws ErrorException when the header information is ambiguous or incorrect
   */
  private void readLabelBox(long length) throws ErrorException, EOFException, IOException {
    String label = in.readUTF((int) length);
  }

  /**
   * Reads the Image Header Box and retrieves the parameters contained.
   *
   * @return bppVariation - definition in {@link #bppVariation}
   *
   * @throws ErrorException when the header information is ambiguous or incorrect
   */
  private boolean readImageHeaderBox() throws ErrorException, EOFException, IOException {

    // Length
    int length = in.readInt();
    if (length != 22) {
      throw new ErrorException("Image Header Box parameters cannot be read.");
    }

    // Type
    int type = in.readInt();
    if (type != JPXBoxTypes.IMAGE_HEADER_BOX_TYPE) {
      throw new ErrorException("Image Header Box parameters cannot be read.");
    }

    // Contents
    ySize = in.readInt();
    xSize = in.readInt();
    zSize = in.readShort();
    LSSignedComponents = new boolean[zSize]; //initialization
    QComponentsBits = new int[zSize]; //initialization
    int BPC = in.read();

    if (BPC == (int) 0xFF) {
      bppVariation = true;

    } else {
      int bitDepth = (int) (BPC & (int) 0x7F) + 1;
      for (int z = 0; z < zSize; z++) {
        QComponentsBits[z] = bitDepth;
      }
      boolean signedComponents = ((BPC & (int) 0x80) > 0);
      for (int z = 0; z < zSize; z++) {
        LSSignedComponents[z] = signedComponents;
      }
    }
    int C = in.read();
    if (C != 7) {
      throw new ErrorException("Image Header Box parameters cannot be read.");
    }
    int UnkC = in.read();

    if ((UnkC != (int) 0x00) && (UnkC != (int) 0x01)) {//An unknown colourspace is treated as if the colourspace interpretation method do accurately reproduce de image
      throw new ErrorException("Image Header Box parameters cannot be read.");
    }
    int IPR = in.read();

    if (IPR == (int) 0x01) {
      throw new ErrorException("CADI cannot decode an image with intellectual property rights.");
    } else if (IPR != (int) 0x00) {
      throw new ErrorException("Image Header Box parameters cannot be read.");
    }

    return bppVariation;
  }

  /**
   * Reads the Bits Per Component Box and retrieves the parameters contained.
   *
   * @throws ErrorException when the header information is ambiguous or incorrect
   */
  private void readBitsPerComponentBox(long length) throws ErrorException, EOFException, IOException {

    if (length != zSize) {
      throw new ErrorException("Bits Per Component Box parameters cannot be read.");
    }

    // Contents
    for (int z = 0; z < zSize; z++) {
      int BPC = in.read();
      QComponentsBits[z] = (int) (BPC & (int) 0x7F) + 1;
      LSSignedComponents[z] = ((BPC & (int) 0x80) > 0);
    }

  }

  /**
   * Reads the Colour Specification Box and retrieves the parameters contained.
   *
   * @param length the length of the box content
   *
   * @throws ErrorException when the header information is ambiguous or incorrect
   */
  private void readColourSpecificationBox(long length) throws ErrorException, EOFException, IOException {
    
    int METH = in.readUnsignedByte();
    if (!((METH == 1) || (METH == 2))) {
      throw new ErrorException("Illegal value for color specification method");
    }

    if (METH != 1) {
      // Only Enumerated colourspace is allowed
      in.skipBytes(length - 1);
    }

    int PREC = in.read();
    int APPROX = in.readUnsignedByte();

    int EnumCS = 0;
    if (METH != 2) {
      // Read EnumCS
      if (((length - 3) % 4) != 0)
        throw new ErrorException("Malformed Colour Specification Box");
      
      int numEnums = (int)((length - 3) / 4);
      for (int i = 0; i < numEnums; i++) {
        EnumCS = in.readInt();
        // EnumCS must be 16 for sRGB or 17 for greyscale
        if (!((EnumCS == 16) || (EnumCS == 17))) {
          throw new ErrorException("Illegal value for the CnumCS in color specification");
        }
      }
    }

    if (METH != 1) {
      // Read Profile

      // Skipped
      long bytesToSkip = length - 3 - (METH == 2 ? 0 : 4);
      in.skipBytes(bytesToSkip);
    }
  }

  /**
   * Reads the Palette Box and retrives the parameters contained.
   *
   * NOTICE: This method is not finished.
   *
   * @param length the length of the box.
   *
   * @throws IOException
   */
  private void readPaletteBox(long length) throws ErrorException, IOException {

    int NE = in.readShort();
    int NPC = in.read();

    boolean[] signedComponents = new boolean[NPC];
    int[] bitDepthComponents = new int[NPC];
    for (int i = 0; i < NPC; i++) {
      byte tmp = in.readByte();
      signedComponents[i] = ((tmp & (byte) 0x80) == 0) ? false : true;
      bitDepthComponents[i] = (tmp & 0x70) + 1;
      if (bitDepthComponents[i] > 38) {
        throw new ErrorException("Bit depth of components cannot be greater than 38.");
      }
    }

    int[][] C = new int[NPC][NE];
    for (int ne = 0; ne < NE; ne++) {
      for (int npc = 0; npc < NPC; npc++) {
        int numBytes = ((bitDepthComponents[npc] - 1) / 8) + 1;
        C[npc][ne] = 0;
        for (int nb = 0; nb < numBytes; nb++) {
          C[npc][ne] = (C[npc][ne] << 8) | (in.read() & 0xFF);
        }
      }
    }

  }

  /**
   * Reads the Component Mapping Box and retrieves the parameters contained.
   *
   * @param length lenth of the box content.
   *
   * @throws IOException
   */
  private void readComponentMappingBox(long length) throws ErrorException, IOException {

    // Number of channels must be an integer multiple of 4 bytes (2 for CMP, 1 for MTYP, 1 for PCOL)
    if ((length % 4) != 0) {
      throw new ErrorException("");
    }

    int numOfChannels = (int) (length / 4);
    int[] CMP = new int[numOfChannels];
    int[] MTYP = new int[numOfChannels];
    int[] PCOL = new int[numOfChannels];
    for (int channel = 0; channel < numOfChannels; channel++) {
      CMP[channel] = in.readUnsignedShort();
      MTYP[channel] = in.readUnsignedByte();
      PCOL[channel] = in.readUnsignedByte();
    }
  }

  /**
   * Reads the Channel Definition Box and retrives the parameters contained.
   *
   * @param length length of the box content.
   *
   * @throws ErrorException
   * @throws IOException
   */
  private void readChannelDefinitionBox(long length) throws ErrorException, IOException {
    in.skipBytes(length);
  }

  /**
   * Read the resolution box.
   *
   * NOTICE: This method only read the values but they are not saved.
   *
   * @param length
   *
   * @throws IOException
   */
  private void readResolutionSuperBox(long length) throws ErrorException, IOException {

    int numOfBoxes = (length > 18) ? 2 : 1;
    boolean rescRead = false;
    boolean resdRead = false;

    for (int box = 0; box < numOfBoxes; box++) {

      int boxLength = in.readInt(); // box length
      if (boxLength != 18) {
        throw new ErrorException("Wrong box length in resolution box");
      }
      int type = in.readInt(); // box type

      if (type == JPXBoxTypes.CAPTURE_RESOLUTION_BOX_TYPE) {

        if (rescRead) {
          throw new ErrorException("Duplicate \"capture resolution box\"");
        } else {
          rescRead = true;
        }

        int VRcN = in.readShort();
        int VRcD = in.readShort();
        int HRcN = in.readShort();
        int HRcD = in.readShort();
        int VRcE = in.readByte();
        int HRcE = in.readByte();

      } else if (type == JPXBoxTypes.DEFAULT_DISPLAY_RESOLUTION_BOX_TYPE) {

        if (resdRead) {
          throw new ErrorException("Duplicate \"default display resolution box\"");
        } else {
          resdRead = true;
        }

        int VRdN = in.readShort();
        int VRdD = in.readShort();
        int HRdN = in.readShort();
        int HRdD = in.readShort();
        int VRdE = in.readByte();
        int HRdE = in.readByte();

      } else {
        throw new ErrorException("Wrong box type in resolution box");
      }
    }
  }
}