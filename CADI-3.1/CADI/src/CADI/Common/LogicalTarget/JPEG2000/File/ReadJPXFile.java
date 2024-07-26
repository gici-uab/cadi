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
 * Read a JPX file format (ISO/IEC 15444-2 annex M)
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/03/06
 */
public class ReadJPXFile {

  /**
   * Is an object with the input stream where the main header is read from.
   */
  private BufferedDataInputStream in = null;

  /**
   * Is an object where the main header are saved.
   */
  private JPCParameters jpcParameters = null;

  private BoxIndexing fileIndexing = null;

  // INTERNAL ATTRIBUTES
  boolean readerRequirementsTypeFound = false;

  /**
   * Boolean indicating if there are components with different bit depths from others
   */
  private boolean bppVariation;

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

  /**
   * Is the file pointer to the first byte of the main header.
   */
  private long mainHeaderInitialPos = 0;

  /**
   * Is the length of the main header.
   */
  private int mainHeaderLength = 0;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param in input stream where data will be read from.
   */
  public ReadJPXFile(BufferedDataInputStream in, BoxIndexing fileIndexing) {
    if (in == null) throw new NullPointerException();

    this.in = in;
    this.fileIndexing = fileIndexing;

    jpcParameters = new JPCParameters();
    //jpcParameters.jpkParameters.LSType = 1;
  }

  /**
   *
   *
   */
  public void run() throws ErrorException, EOFException, IOException {

    long length = -1;
    long contentBoxLength = -1;
    int type = -1;
    BoxIndexing boxIndexing = null;

    // Acquires the resource
    in.lock();

    try {

      // READER REQUIREMENTS BOX
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

      if (type != JPXBoxTypes.READER_REQUIREMENTS_BOX_TYPE)
        throw new ErrorException("JPEG2000 Reader Requirements Box is not found after the File Type box.");
      if (readerRequirementsTypeFound)
        throw new ErrorException("JPEG2000 Reader Requirements Box has multiple instances.");

      readerRequirementsTypeFound = true;
      try {
        readRequirementsBox(contentBoxLength);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }



      // READ OPTIONAL BOXES
      boolean finish = false;

      while (!finish) {

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

        // Skip box which do not body
        if (contentBoxLength == 0) continue;

        if (in.length() < contentBoxLength)
          throw new ErrorException("Wrong box length.");

        boxIndexing = new BoxIndexing(fileIndexing, type, pos, length, (type == JP2BoxTypes.JP2_HEADER_BOX_TYPE ? true : false));
      fileIndexing.addChild(boxIndexing);

        switch (type) {
          case JPXBoxTypes.READER_REQUIREMENTS_BOX_TYPE:
            //System.out.println("Reading requirements type, length="+contentBoxLength+" of "+in.length());
            if (readerRequirementsTypeFound)
              throw new ErrorException("JPEG2000 Reader Requirements Box has multiple instances.");
            break;

          case JPXBoxTypes.JP2_HEADER_BOX_TYPE:
            //System.out.println("Reading JP2 header box, length="+contentBoxLength+" of "+in.length());
            ReadJP2HeaderBox readJP2HeaderBox = new ReadJP2HeaderBox(in, contentBoxLength, true, boxIndexing);
            readJP2HeaderBox.run();
            break;

          case JPXBoxTypes.CODESTREAM_HEADER_BOX_TYPE:
            //System.out.println("Reading codestream header box, length="+contentBoxLength+" of "+in.length());
            readCodestreamHeaderBox(contentBoxLength);
            break;

          case JPXBoxTypes.COMPOSITION_LAYER_HEADER_BOX_TYPE:
            //System.out.println("Reading composition layer header box, length="+contentBoxLength+" of "+in.length());
            readCompositionLayerHeaderBox(contentBoxLength);
            break;

          case JPXBoxTypes.CONTIGUOUS_CODESTREAM_BOX_TYPE:
            //long initialCodestreamPos = in.getPos();
            //System.out.println("Reading contiguous codestream, length="+contentBoxLength+" of "+in.length());
            finish = (in.length() == contentBoxLength) ? true : false;
            readContiguousCodestreamBox(contentBoxLength);
            /* long finalCodestreamPos = in.getPos();
             * System.out.println("iniPos: "+initialCodestreamPos);
             * System.out.println("finPos: "+finalCodestreamPos);
             * System.out.println("iniPos+length: "+(initialCodestreamPos+contentBoxLength)); */
            break;

          default:
            //System.out.println("==> UNKNOWN BOX:"+Integer.toHexString(type)+" of "+in.length());
            in.skipBytes(contentBoxLength);

        }

        // Finish condition: end of file has been reached, or last box length is 0
        if ((contentBoxLength == 0) || (in.available() == 0)) {
          finish = true;
        }
      }

    } finally {
      in.unlock();
    }

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
   *
   * @param length
   *
   * @throws IOException
   */
  private void readRequirementsBox(long length) throws IOException, IllegalAccessException {

    // Read ML (1 byte)
    int ML = in.read();

    // FUAM
    int FUAM = in.readUnsignedShort();

    // DCM
    int DCM = in.readUnsignedShort();

    // NSF
    int NSF = in.readUnsignedShort();

    // SF & SM
    int[] SF = new int[NSF];
    int[] SM = new int[NSF];
    for (int i = 0; i < NSF; i++) {
      SF[i] = in.readUnsignedShort();
      SM[i] = in.readUnsignedShort();
    }

    // NVF
    int NVF = in.readShort();

    // VF
    int[] VF = new int[NVF];
    int[] VM = new int[NVF];
    for (int i = 0; i < NVF; i++) {
      VF[i] = in.read();
      VM[i] = in.readInt();
    }
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   * @throws ErrorException
   */
  private void readCodestreamHeaderBox(long length) throws IOException, ErrorException {

    long pointerLimit = in.getPos() + length;

    //Image Header Box
    bppVariation = readImageHeaderBox();

    while (in.getPos() < pointerLimit) {

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
      System.out.println("\tType: " + type);
      switch (type) {

        case JPXBoxTypes.LABEL_BOX_TYPE:
          System.out.println("\tLabel box");
          readLabelBox(contentBoxLength);
          break;

        case JPXBoxTypes.BITS_PER_COMPONENT_BOX_TYPE: //Bits Per Component Box
          System.out.println("\tBits per component");
          readBitsPerComponentBox(contentBoxLength);
          break;

        case JPXBoxTypes.COLOR_SPECIFICATION_BOX_TYPE: //Colour Specification Box
          System.out.println("\tColor specification");
          readColourSpecificationBox(contentBoxLength);
          break;

        case JPXBoxTypes.PALETTE_BOX_TYPE:
          System.out.println("\tPalette box");
          readPaletteBox(length);
          break;
        //throw new ErrorException("This option is not supported by this CADI Server version.");

        case JPXBoxTypes.COMPONENT_MAPPING_BOX_TYPE:
          System.out.println("\tComponent mapping");
          readComponentMappingBox(contentBoxLength);
          break;
        //throw new ErrorException("This option is not supported by this CADI Server version.");

        case JPXBoxTypes.CHANNEL_DEFINITION_BOX_TYPE:
          System.out.println("\tChannel definition");
          readChannelDefinitionBox(contentBoxLength);
          break;
        //throw new ErrorException("This option is not supported by this CADI Server version.");


        case JPXBoxTypes.RESOLUTION_BOX_TYPE:
          System.out.println("\tResolution");
          readResolutionSuperBox(contentBoxLength);
          break;
        //throw new ErrorException("This option is not supported by this CADI Server version.");

        default:
          System.out.println("\tNOT RECONISED");
          throw new ErrorException("JPEG2000 Signature Box does not conform the standard.");
      }
    }
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   */
  private void readCompositionLayerHeaderBox(long length) throws IOException {
    in.skipBytes(length);
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   */
  private void readDataReferenceBox(long length) throws IOException {
    in.skipBytes(length);
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   */
  private void readFragmentTableBox(long length) throws IOException {
    in.skipBytes(length);
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   * @throws ErrorException
   */
  private void readContiguousCodestreamBox(long length) throws IOException, ErrorException {

    JPCMainHeaderDecoder jpcDeheading = new JPCMainHeaderDecoder(in);

    try {
      jpcDeheading.run();
    } catch (ErrorException e) {
      e.printStackTrace();
      throw new ErrorException("JPC main header can not be readed or decoded correctly");
    }

    jpcParameters = jpcDeheading.getJPCParameters();
    mainHeaderInitialPos = jpcDeheading.getMainHeaderInitialPos();
    mainHeaderLength = jpcDeheading.getMainHeaderLength();
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   */
  private void readMediaDataBox(long length) throws IOException {
    in.skipBytes(length);
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   */
  private void readCompositionBox(long length) throws IOException {
    in.skipBytes(length);
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   */
  private void readDesiredReproductionsBox(long length) throws IOException {
    in.skipBytes(length);
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   */
  private void readROIDescriptionBox(long length) throws IOException {
    in.skipBytes(length);
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   */
  private void readCrossReferenceBox(long length) throws IOException {
    in.skipBytes(length);
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   */
  private void readAssociationBox(long length) throws IOException {
    in.skipBytes(length);
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   */
  private void readBinaryFilterBox(long length) throws IOException {
    in.skipBytes(length);
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   */
  private void readDigitalSignatureBox(long length) throws IOException {
    in.skipBytes(length);
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   */
  private void readMPEG7BinayrBox(long length) throws IOException {
    in.skipBytes(length);
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   */
  private void readFreeBox(long length) throws IOException {
    in.skipBytes(length);
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   */
  private void readXMLBox(long length) throws IOException {
    in.skipBytes(length);
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   */
  private void readUUIDBox(long length) throws IOException {
    in.skipBytes(length);
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   */
  private void readIntellectualPropertyRightsBox(long length) throws IOException {
    in.skipBytes(length);
  }

  /**
   *
   * @param length
   *
   * @throws IOException
   */
  private void readUUIDInfoBox(long length) throws IOException {
    in.skipBytes(length);
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

    if (length != 22)
      throw new ErrorException("Image Header Box parameters cannot be read.");

    // Type
    int type = in.readInt();
    if (type != JPXBoxTypes.IMAGE_HEADER_BOX_TYPE)
      throw new ErrorException("Image Header Box parameters cannot be read.");

    // Contents
    ySize = in.readInt();
    xSize = in.readInt();
    zSize = in.readShort();
    LSSignedComponents = new boolean[zSize]; //initialization
    QComponentsBits = new int[zSize]; //initialization
    int BPC = in.read();

    if (BPC == (int)0xFF) {
      bppVariation = true;

    } else {
      int bitDepth = (int)(BPC & (int)0x7F) + 1;
      for (int z = 0; z < zSize; z++) {
        QComponentsBits[z] = bitDepth;
      }
      boolean signedComponents = ((BPC & (int)0x80) > 0);
      for (int z = 0; z < zSize; z++) {
        LSSignedComponents[z] = signedComponents;
      }
    }
    int C = in.read();
    if (C != 7) {
      throw new ErrorException("Image Header Box parameters cannot be read.");
    }
    int UnkC = in.read();

    if ((UnkC != (int)0x00) && (UnkC != (int)0x01)) {//An unknown colourspace is treated as if the colourspace interpretation method do accurately reproduce de image
      throw new ErrorException("Image Header Box parameters cannot be read.");
    }
    int IPR = in.read();

    if (IPR == (int)0x01) {
      throw new ErrorException("CADI cannot decode an image with intellectual property rights.");
    } else if (IPR != (int)0x00) {
      throw new ErrorException("Image Header Box parameters cannot be read.");
    }

    return bppVariation;
  }

  /**
   * Reads the Label Box and retrieves the parameters contained.
   *
   * @throws ErrorException when the header information is ambiguous or incorrect
   */
  private void readLabelBox(long length) throws ErrorException, EOFException, IOException {
    String label = in.readUTF((int)length);
  }

  /**
   * Reads the Bits Per Component Box and retrieves the parameters contained.
   *
   * @throws ErrorException when the header information is ambiguous or incorrect
   */
  private void readBitsPerComponentBox(long length) throws ErrorException, EOFException, IOException {

    if (length != zSize)
      throw new ErrorException("Bits Per Component Box parameters cannot be read.");

    // Contents
    for (int z = 0; z < zSize; z++) {
      int BPC = in.read();
      QComponentsBits[z] = (int)(BPC & (int)0x7F) + 1;
      LSSignedComponents[z] = ((BPC & (int)0x80) > 0);
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
    if (!((METH == 1) || (METH == 2)))
      throw new ErrorException("Illegal value for color specification method");

    int PREC = in.read();
    int APPROX = in.readUnsignedByte();

    int EnumCS = 0;
    if (METH != 2) {
      EnumCS = in.readInt();

      // EnumCS must be 16 for sRGB or 17 for greyscale
      if (!((EnumCS == 16) || (EnumCS == 17)))
        throw new ErrorException("Illegal value for the CnumCS in color specification");
    }

    if (METH != 1) {
      // Skipped
      long bytesToSkip = length - 3 - (METH == 2 ? 4 : 0);
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
      signedComponents[i] = ((tmp & (byte)0x80) == 0) ? false : true;
      bitDepthComponents[i] = (tmp & 0x70) + 1;
      if (bitDepthComponents[i] > 38)
        throw new ErrorException("Bit depth of components cannot be greater than 38.");
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
    if ((length % 4) != 0) throw new ErrorException("");

    int numOfChannels = (int)(length / 4);
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
      if (boxLength != 18)
        throw new ErrorException("Wrong box length in resolution box");
      int type = in.readInt(); // box type

      if (type == JPXBoxTypes.CAPTURE_RESOLUTION_BOX_TYPE) {

        if (rescRead)
          throw new ErrorException("Duplicate \"capture resolution box\"");
        else rescRead = true;

        int VRcN = in.readShort();
        int VRcD = in.readShort();
        int HRcN = in.readShort();
        int HRcD = in.readShort();
        int VRcE = in.readByte();
        int HRcE = in.readByte();

      } else if (type == JPXBoxTypes.DEFAULT_DISPLAY_RESOLUTION_BOX_TYPE) {

        if (resdRead)
          throw new ErrorException("Duplicate \"default display resolution box\"");
        else resdRead = true;

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