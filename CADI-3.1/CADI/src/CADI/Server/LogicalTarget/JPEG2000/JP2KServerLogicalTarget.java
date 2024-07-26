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
package CADI.Server.LogicalTarget.JPEG2000;

import CADI.Common.LogicalTarget.JPEG2000.*;
import GiciException.WarningException;
import java.io.PrintStream;
import java.util.Map;

import CADI.Common.LogicalTarget.JPEG2000.Indexing.CodestreamIndex;
import GiciStream.BufferedDataInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * This class is used to store the indexed JPEG2000 image in the server.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.4 2012/01/14
 */
public class JP2KServerLogicalTarget extends JPEG2KLogicalTarget {

  /**
   * Definition in {@link CADI.Common.Network.JPIP.TargetField#target}.
   */
  private String target = null;

  /**
   * Is the input stream used to read the file.
   */
  private BufferedDataInputStream in = null;

  //private HashMap<Integer, CodestreamIndex> codestreamIndexes = null;
  private CodestreamIndex codestreamIndex = null;
  
  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.PredictiveScalingFactors}.
   */
  private PredictiveScalingFactors scalingFactors = null;

  // INTERNAL ATTRIBUTES
  /**
   * The Most Significant Bit Plane of the whole image.
   * <p>
   * The least significant bit plane is the 0 and the content of this variable
   * indicates the most significant one.
   * <p>
   * This attribute is only used whenn the
   * <code>RATE_DISTORTION_METHOD_CPI
   * </code> or
   * <code>RATE_DISTORTION_METHOD_CORD</code> rate-distortion methods are used.
   */
  private int MSBPlane = -1;

  private byte[] mainHeader = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param target definition in {@link #target}.
   * @param codestream definition in {@link #codestreams}.
   * @param codestreamIndex definition in {@link #codestreamIndex}.
   */
  public JP2KServerLogicalTarget(String target, BufferedDataInputStream in,
                                 ServerJPEG2KCodestream codestream,
                                 CodestreamIndex codestreamIndex) {

    super();

    // Check input parameters
    if (target == null) {
      throw new NullPointerException();
    }
    if (in == null) {
      throw new NullPointerException();
    }
    if (codestream == null) {
      throw new NullPointerException();
    }
    if (codestreamIndex == null) {
      throw new NullPointerException();
    }
    if (codestreams.containsKey(codestream.getIdentifier())) {
      throw new IllegalArgumentException();
    }

    // Data copy
    this.target = target;
    this.in = in;
    codestreams.put(codestream.getIdentifier(), codestream);
    //this.codestreamIndexes.put(codestream.getIdentifier(), codestreamIndex);
    this.codestreamIndex = codestreamIndex;
  }

  /**
   * Constructor.
   * <p>
   * This constructor must be used when the rate-distortion method is either
   * {@link #RATE_DISTORTION_METHOD_CPI} or {@link #RATE_DISTORTION_METHOD_CoRD}.
   *
   * @param dataBins
   * definition in {@link #dataBins}.
   * @param jpcParameters
   * definition in {@link #jpcParameters}.
   * @param imageStructure
   */
  public JP2KServerLogicalTarget(String target, BufferedDataInputStream in,
                                 ServerJPEG2KCodestream codestream) {

    super();

    // Check input parameters
    if (target == null) {
      throw new NullPointerException();
    }
    if (in == null) {
      throw new NullPointerException();
    }
    if (codestream == null) {
      throw new NullPointerException();
    }

    // Copy data
    this.target = target;
    this.in = in;
    this.codestreams.put(codestream.getIdentifier(), codestream);
  }
  
  public void setScalingFactors(PredictiveScalingFactors scalingFactors) {
    this.scalingFactors = scalingFactors;
  }
  
  public PredictiveScalingFactors getScalingFactors() {
    return scalingFactors;
  }

  /**
   * Returns the {@link #target} attribute.
   *
   * @return the {@link #target} attribute.
   */
  public final String getTarget() {
    return target;
  }

  /**
   *
   * @param identifier
   *
   * @return
   */
  @Override
  public ServerJPEG2KCodestream getCodestream(int identifier) {
    return codestreams.containsKey(identifier)
            ? (ServerJPEG2KCodestream)codestreams.get(identifier)
            : null;
  }

  /**
   * Returns the {@link #in} attribute.
   *
   * @return in the {@link #in} attribute.
   */
  @Deprecated
  public final BufferedDataInputStream getInputDataSource() {
    return in;
  }

  /**
   *
   * @param filePointer
   * @param b
   *
   * @throws IOException
   */
  public void readFully(long filePointer, byte[] b) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    }
    readFully(filePointer, b, 0, b.length);
  }

  /**
   *
   * @param filePointer
   * @param b
   * @param off
   * @param len
   *
   * @throws IOException
   */
  public void readFully(long filePointer, byte[] b, int off, int len) throws IOException {

    // Check input parameters
    if (filePointer < 0) {
      throw new IllegalArgumentException();
    }
    if (b == null) {
      throw new NullPointerException();
    } else {
      if ((off < 0) || (off > b.length) || (len < 0)
              || ((off + len) > b.length) || ((off + len) < 0)) {
        throw new IndexOutOfBoundsException();
      }
    }

    try {
      in.lock();
      in.seek(filePointer);
      in.readFully(b, off, len);
    } finally {
      in.unlock();
    }
  }

  /**
   * Returns the file pointer to the JPEG2000 main header.
   *
   * @see #mainHeaderInitialPos
   */
  public long getMainHeaderInitialPos() {
    return codestreamIndex.mhix.mainHeaderInitialPos;
  }

  /**
   * Returns the length of the JPEG2000 main header.
   *
   * @see #mainHeaderLength
   */
  public int getMainHeaderLength() {
      return codestreamIndex.mhix.mainHeaderLength;
  }
  
  public long getTileHeaderFilePointer(int tileIndex) {
    return codestreamIndex.thix.tileHeaderFilePointer;
  }

  public int getTileHeaderLength(int tileIndex) {
    return codestreamIndex.thix.tileHeaderLength;
  }

  /**
   * Returns the file pointer offset of a packet.
   *
   * @param inClassIdentifier
   * definition in
   *          {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}
   * .
   * @param layer
   * the number of layer.
   *
   * @return the offset of the packet.
   */
  public int getPacketOffset(long inClassIdentifier, int layer) {
    return codestreamIndex.ppix.getOffset(inClassIdentifier, layer);
  }

  /**
   * Returns the offset of a packet in the data bin. Packet is identified
   * by means of its unique identifier <data>inClassIdentifier</data> and its
   * number of layer.
   *
   * @param inClassIdentifier
   * definition in
   *          {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}
   * .
   * @param layer
   * the layer which offset is requested
   *
   * @return the offset of the packet in the data bin.
   */
  @Override
  public int getPacketOffsetWithDataBin(long inClassIdentifier, int layer) {
    assert (inClassIdentifier >= 0);
    assert (layer >= 0);

    int offset = 0;
    for (int l = 0; l < layer; l++) {
      offset += getPacketLength(inClassIdentifier, l);
    }
    return offset;
  }

  /**
   * Returns the length of a packet.
   *
   * @param inClassIdentifier
   * definition in
   *          {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}
   * .
   * @param layer
   * the number of the layer.
   *
   * @return the length of the packet.
   */
  @Override
  public int getPacketLength(long inClassIdentifier, int layer) {
    return (int)codestreamIndex.ppix.getLength(inClassIdentifier, layer);
  }

  @Override
  public long getDataLength(long inClassIdentifier) {
    return codestreamIndex.ppix.getLength(inClassIdentifier);
  }

  /**
   * Returns the length of the data bin of which unique identifier is <data>
   * inClassIdentifier</data>.
   *
   * @param inClassIdentifier
   * definition in
   *          {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}
   * .
   *
   * @return the length of the data bin.
   */
  public int getDataBinLength(long inClassIdentifier) {
    return (int)getDataLength(inClassIdentifier);
  }

  /**
   * Return the last complete layer that it can be whole recovered with
   * <data>dataBinLength</data> bytes.
   *
   * @param inClassIdentifier
   * definition in
   *          {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}
   * .
   * @param dataBinLength
   * is the lenth of the data bin.
   *
   * @return the last complete layer.
   */
  @Override
  public int getLastCompleteLayer(long inClassIdentifier, long dataBinLength) {
    int lastCompleteLayer = 0;
    long length = 0;

    int maxLayers = codestreams.get(0).getNumLayers();
    for (int layer = 0; layer < maxLayers; layer++) {
      length += getPacketLength(inClassIdentifier, layer);
      if (dataBinLength >= length) {
        lastCompleteLayer++;
      } else {
        return lastCompleteLayer;
      }
    }

    return lastCompleteLayer;
  }

  // Methods to be used when file has been indexed
  // using coding passes offsets and lengths
  
  public void setMainHeader(byte[] mainHeader) {
    this.mainHeader = mainHeader;
  }

  public byte[] getMainHeader() {
    return mainHeader;
  }

  /**
   *
   * @param inClassIdentifier
   * @param subband
   * @param yBlock
   * @param xBlock
   * @param codingPass
   *
   * @return
   */
  public long getFilePointerCodingPass(long inClassIdentifier, int subband,
                                       int yBlock, int xBlock, int codingPass) {
    assert (inClassIdentifier >= 0);
    assert (subband >= 0);
    assert (yBlock >= 0);
    assert (xBlock >= 0);
    assert (codingPass >= 0);
    return codestreamIndex.ppix.getFilePointer(inClassIdentifier, subband, yBlock, xBlock, codingPass);
  }

  /**
   * NOTE: METHOD TO BE DEPRECATED.
   *
   * @param inClassIdentifier
   *
   * @return
   */
  public int[][][][] getLengthsOfCodingPasses(long inClassIdentifier) {
    assert (inClassIdentifier >= 0);
    return codestreamIndex.ppix.getLengths(inClassIdentifier);
  }

  /**
   *
   * @param inClassIdentifier
   * @param subband
   * @param yBlock
   * @param xBlock
   * @param codingPass
   *
   * @return
   */
  public int getLengthOfCodingPass(long inClassIdentifier, int subband,
                                   int yBlock, int xBlock, int codingPass) {
    assert (inClassIdentifier >= 0);
    assert (subband >= 0);
    assert (yBlock >= 0);
    assert (xBlock >= 0);
    assert (codingPass >= 0);
    return codestreamIndex.ppix.getLength(inClassIdentifier, subband, yBlock, xBlock, codingPass);
  }

  /**
   * Returns the {@link #zeroBitPlanes} attribute.
   *
   * @return the {@link #zeroBitPlanes} attribute.
   */
  public int[][][] getZeroBitPlanes(long inClassIdentifier) {
    assert (inClassIdentifier >= 0);
    return codestreamIndex.ppix.getZeroBitPlanes(inClassIdentifier);
  }

  /**
   * Returns the {@link #MSBPlane} attribute.
   *
   * @return the {@link #MSBPlane} attribute.
   */
  public int getMSBPlane() {
    if (MSBPlane < 0) {
      MSBPlane = computeMSBPlane(getCodestream(0));
    }
    return MSBPlane;
  }

  /**
   * This methdo returns the Most Significat Bit Plane (MSBP) for each block
   * belonging to a tile-component-resolutionlevel-precinct-subband.
   *
   * @param inClassIdentifier
   * @param subband
   * 0 - HL, 1 - LH, 2 - HH (if resolutionLevel == 0 --> 0 - LL)
   * @param yBlock
   * block row in the subband
   * @param xBlock
   * block column in the subband
   *
   * @return the value of the Most Significant Bit Planes (MSBP).
   */
  public int getMSBPlane(long inClassIdentifier, int subband, int yBlock, int xBlock) {
    assert (inClassIdentifier >= 0);
    assert (subband >= 0);
    assert (yBlock >= 0);
    assert (xBlock >= 0);

    ServerJPEG2KCodestream codestream = getCodestream(0);
    int[] TCRP = codestream.findTCP(inClassIdentifier);
    int tile = TCRP[0];
    int component = TCRP[1];
    int rLevel = TCRP[2];

    ServerJPEG2KTile tileObj = codestream.getTile(tile);
    ServerJPEG2KComponent compObj = tileObj.getComponent(component);

    return (codestream.getTile(tile).getComponent(component).getGuardBits()
            + compObj.getResolutionLevel(rLevel).getExponent(subband) - 2
            - codestreamIndex.ppix.getZeroBitPlanes(inClassIdentifier, subband, yBlock, xBlock));
  }

  /*
   * (non-Javadoc)
   *
   * @see CADI.Server.LogicalTarget.ServerLogicalTarget#toString()
   */
  @Override
  public String toString() {
    String str = "";
    str = getClass().getName() + " [";
    super.toString();

    str += "Target=" + target;
    str += in.toString();

    for (Map.Entry<Integer, JPEG2KCodestream> entry : codestreams.entrySet()) {
      str += ((ServerJPEG2KCodestream)entry.getValue()).toString();
    }

    //str += mainHeader.toString();

    str += "MSBPlane=" + MSBPlane;

    str += "]";

    return str;
  }

  /**
   * Prints the JPC logical target data out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out
   * an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- JP2K server logical target --");

    super.list(out);

    out.println("Target: " + target);
    out.println();
    in.list(out);
    out.println();
    for (Map.Entry<Integer, JPEG2KCodestream> entry : codestreams.entrySet()) {
      ((ServerJPEG2KCodestream)entry.getValue()).list(out);
    }
    out.println();

    out.println();
    out.println("MSBPlane: " + MSBPlane);

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   *
   * @param codestream
   *
   * @return
   */
  private int computeMSBPlane(ServerJPEG2KCodestream codestream) {
    int msbPlane = 0;

    int numTiles = codestream.getNumTiles();
    int numComps = codestream.getZSize();

    int[] rateDistortionAdjustments = new int[numComps];
    for (int tile = 0; tile < numTiles; tile++) {
      ServerJPEG2KTile tileObj = codestream.getTile(tile);
      for (int comp = 0; comp < numComps; comp++) {
        ServerJPEG2KComponent compObj = tileObj.getComponent(comp);
        rateDistortionAdjustments[comp] =
                setRateDistortionAdjustment(codestream.getZSize(), compObj.getQuantizationStyle(), codestream.getMultiComponentTransform());
      }
    }


    for (int tile = 0; tile < numTiles; tile++) {
      ServerJPEG2KTile tileObj = codestream.getTile(tile);
      for (int comp = 0; comp < numComps; comp++) {
        ServerJPEG2KComponent compObj = tileObj.getComponent(comp);
        int numRLevels = compObj.getWTLevels();
        for (int rLevel = 0; rLevel < numRLevels; rLevel++) {
          ServerJPEG2KResolutionLevel rLevelObj = compObj.getResolutionLevel(rLevel);
          int numPrecs = rLevelObj.getNumPrecincts();
          for (int prec = 0; prec < numPrecs; prec++) {
            int[][][] zeroBitPlanes = getZeroBitPlanes(rLevelObj.getInClassIdentifier(prec));
            for (int subband = 0; subband < zeroBitPlanes.length; subband++) {
              int rateDistortionAdjustment = 0;
              try {
                rateDistortionAdjustment = JPEG2000Util.calculateRateDistortionAdjustment(rateDistortionAdjustments, comp, numRLevels, rLevel, subband);
              } catch (WarningException ex) {
                ex.printStackTrace();
              }
              for (int yBlock = 0; yBlock < zeroBitPlanes[subband].length; yBlock++) {
                for (int xBlock = 0; xBlock < zeroBitPlanes[subband][yBlock].length; xBlock++) {
                  int msbPlanesBlock = compObj.getGuardBits()
                          + rLevelObj.getExponent((rLevel == 0) ? 0 : subband + 1) - 2
                          - zeroBitPlanes[subband][yBlock][xBlock];
                  if (msbPlanesBlock + rateDistortionAdjustment > msbPlane) {
                    msbPlane = msbPlanesBlock + rateDistortionAdjustment;
                  }

                }
              }
            }
          }
        }
      }
    }


    return msbPlane;
  }

  /**
   * 674 * Calculates the rate-distortion adjustments for each component.
   * 675 */
  private static int setRateDistortionAdjustment(int zSize, int QTypes, int CTType) {

    int rateDistortionAdjustments = 0;
    for (int z = 0; z < zSize; z++) {
      if (QTypes == 0) {
        if (CTType == 0) {
          rateDistortionAdjustments = 1;
        } else {
          rateDistortionAdjustments = 2;
        }
      } else {
        rateDistortionAdjustments = 0;
      }
    }

    return rateDistortionAdjustments;
  }
}
