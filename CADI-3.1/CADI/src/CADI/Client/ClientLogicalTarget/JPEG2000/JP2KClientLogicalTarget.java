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
package CADI.Client.ClientLogicalTarget.JPEG2000;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import CADI.Client.ImageData;
import CADI.Client.Cache.ClientCacheManagement;
import CADI.Common.Cache.MetaDataBin;
import CADI.Common.Cache.PrecinctDataBin;
import CADI.Common.Log.CADILog;
import CADI.Common.LogicalTarget.JPEG2000.JPCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Codestream.JPCMainHeaderDecoder;
import CADI.Common.LogicalTarget.JPEG2000.Codestream.JPKMainHeaderDecoder;
import CADI.Common.LogicalTarget.JPEG2000.Codestream.PacketHeaderDataInputStream;
import CADI.Common.LogicalTarget.JPEG2000.Codestream.PacketHeadersDecoder;
import CADI.Common.LogicalTarget.JPEG2000.File.JP2BoxTypes;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.COCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.QCCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters;
import CADI.Common.Network.JPIP.ClassIdentifiers;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Common.LogicalTarget.JPEG2000.Indexing.JPEG2KBox;
import GiciException.ErrorException;
import GiciException.WarningException;
import GiciStream.BufferedDataInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/03/06
 */
public class JP2KClientLogicalTarget {

  /**
   * Contains the last view window of the image decompressed.
   */
  private ViewWindowField viewWindow = null;

  /**
   * Definition in {@link CADI.Client.Session.ClientSessionTarget#cache}.
   */
  private ClientCacheManagement clientCache = null;

  /**
   * Definition in {@link CADI.Client.Session.ClientSessionTarget#imageData}.
   */
  protected ImageData imageData = null;

  /**
   * Definition in {@link CADI.Client.Client#log}.
   */
  protected CADILog log = null;

  // INTERNAL ATTRIBUTES
  /**
   *
   */
  private ClientJPEG2KCodestream codestream = null;

  private ClientJPEG2KTile tileObj = null;

  //FIXME: update attribute documentation
  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Codestream.PacketHeadersDecoder}
   */
  private PacketHeadersDecoder packetHeadersDecoder = null;

  /**
   * This object is used to decode a JPEG2000 codestream.
   */
  private JPEG2KDecoder jpeg2kDecoder = null;

  /**
   * Definition in {@link CADI.Client.ImageData#imageSamplesFloat}.
   * <p>
   * Temporary structure.
   */
  private float[][][] imageSamplesFloat = null;

  /**
   *
   */
  protected int[] components = null;

  /**
   * Definition in {@link  CADI.Client.ClientLogicalTarget.JPEG2000.JPEG2KDecoder#numThreads}.
   */
  private int numThreads = -1;

  // POSIBLES PARAMETROS A BORRAR
  /**
   * Indicates if the main header has been decoded.
   */
  private boolean isMainHeaderDecoded = false;

  /**
   *
   */
  protected int imageDataType = ImageData.SAMPLES_FLOAT;

  /**
   * Definition in {@link CADI.Client.ImageData#bufImage}.
   * <p>
   * Temporal structure.
   */
  private BufferedImage bufferedImage = null;

  /**
   *
   */
  private float[] fImagePixels = null;

  // FIN POSIBLES PARAMETROS A BORRAR
  
  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param imageData
   */
  public JP2KClientLogicalTarget(ImageData imageData) {
    viewWindow = new ViewWindowField();
    this.imageData = imageData;
    if (imageData == null) {
      imageData = new ImageData(ImageData.SAMPLES_FLOAT);
    }
  }

  /**
   * Constructor.
   *
   * @param imageData
   */
  public JP2KClientLogicalTarget(ImageData imageData, ClientCacheManagement clientCache, CADILog log) {

    // Check input parameters
    if (imageData == null) {
      throw new NullPointerException();
    }
    if (clientCache == null) {
      throw new NullPointerException();
    }
    if (log == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.imageData = imageData;
    this.clientCache = clientCache;
    this.log = log;

    viewWindow = new ViewWindowField();
  }

  public JP2KClientLogicalTarget(ImageData imageData,
                                 ClientCacheManagement clientCache,
                                 CADILog log, int numThreads) {

    // Check input parameters
    if (imageData == null) {
      throw new NullPointerException();
    }
    if (clientCache == null) {
      throw new NullPointerException();
    }
    if (log == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.imageData = imageData;
    this.clientCache = clientCache;
    this.log = log;
    this.numThreads = numThreads;

    viewWindow = new ViewWindowField();
  }

  /**
   * This method must implement the decode engine of the logical target.
   *
   * @param viewWindow the Window Of Interest to decode.
   * @param dstImageData an object where the decode image must be saved.
   *
   * @throws ErrorException if the Window Of Interest cannot be decoded.
   */
  public void decode(ViewWindowField requestViewWindow, ImageData dstImageData)
          throws ErrorException {
    decode(requestViewWindow, null, dstImageData);
  }

  /**
   * This method must implement the decode engine of the logical target. If
   * the second argument (responseViewWindow) is null, this method is equal
   * to the {@link #decode(ViewWindowField, ImageData)} method.
   *
   * @param requestViewWindow the Window Of Interest which the user is
   * 			requesting for.
   * @param responseViewWindow the Window Of Interest which the server has
   * 			sent.
   * @param dstImageData an object where the decode image must be saved.
   *
   * @throws ErrorException if the Window Of Interest cannot be decoded.
   */
  public void decode(ViewWindowField requestViewWindow,
                     ViewWindowField responseViewWindow,
                     ImageData dstImageData) throws ErrorException {

    this.imageData = dstImageData;
    if (imageData == null) {
      imageData = new ImageData(ImageData.SAMPLES_FLOAT);
    }
    imageDataType = imageData.getType();

    //System.out.println("REQUESTED VIEW WINDOW="+requestViewWindow);
    //System.out.println("RESPONSE VIEW WINDOW="+responseViewWindow);

    /* if (imageDataType == ImageData.UNDEFINED) {
     * imageData = new ImageData(ImageData.SAMPLES_FLOAT);
     * imageSamplesFloat = imageData.getData();
     *
     * } else if (imageDataType == ImageData.SAMPLES_FLOAT) {
     * imageSamplesFloat = imageData.getData();
     *
     * } else if (imageDataType == ImageData.BUFFERED) {
     * System.out.println("BUFFERED IMAGE");
     * BufferedImage bufferedImage = imageData.getBufferedImage();
     * if (bufferedImage != null) {
     * Raster raster = bufferedImage.getRaster();
     * DataBuffer dataBuffer = raster.getDataBuffer();
     * System.out.println("BUFFERED IMAGE type: " + dataBuffer.getDataType());
     * if (dataBuffer.getDataType() == DataBuffer.TYPE_FLOAT) {
     * System.out.println(" float");
     * fImagePixels = ((DataBufferFloat)dataBuffer).getData();
     * } else {
     * System.out.println(" unsupported");
     * if (true) System.exit(0);
     * throw new ErrorException("Unsupported image data type");
     * }
     * }
     * } else {
     * throw new ErrorException("Unsupported image data type");
     * }
     */

    //clientCache.list(System.out);

    // Decodes the main header, if it has not been.
    if (!isMainHeaderDecoded) {
      decodeMainHeader();
    }
    viewWindow = calculateWOIToDecode(requestViewWindow, responseViewWindow);
    //System.out.println("WINDOW TO DECODE="+viewWindow.toString()); // DEBUG

    int discardLevels = codestream.determineNumberOfDiscardLevels(viewWindow.fsiz, viewWindow.roundDirection);

    // Adjust the roff to blocks bounds.
    // It must be done because the DWT is no ready to deal with offsets
    // which are not fit to block boundaries
    int blockWidth = codestream.getBlockWidth();
    int tmp = (viewWindow.roff[0] / blockWidth) * blockWidth;
    viewWindow.rsiz[0] += viewWindow.roff[0] - tmp;
    viewWindow.roff[0] = tmp;
    int blockHeight = codestream.getBlockHeight();
    tmp = (viewWindow.roff[1] / blockWidth) * blockHeight;
    viewWindow.rsiz[1] += viewWindow.roff[1] - tmp;
    viewWindow.roff[1] = tmp;


    // UPDATE WOI PARAMETERS
    if (viewWindow.layers < 1) {
      viewWindow.layers = codestream.getNumLayers();
    }

    // Components
    if (viewWindow.comps != null) {
      int numComps = 0;
      for (int i = 0; i < viewWindow.comps.length; i++) {
        numComps += viewWindow.comps[i][1] - viewWindow.comps[i][0] + 1;
      }
      components = new int[numComps];

      int indexComps = 0;
      for (int i = 0; i < viewWindow.comps.length; i++) {
        for (int comp = viewWindow.comps[i][0]; comp <= viewWindow.comps[i][1]; comp++) {
          components[indexComps++] = comp;
        }
      }

    } else {
      int zSize = codestream.getZSize();
      this.viewWindow.comps = new int[1][2];
      this.viewWindow.comps[0][0] = 0;
      this.viewWindow.comps[0][1] = zSize - 1;
      components = new int[zSize];
      for (int i = 0; i < zSize; i++) {
        components[i] = i;
      }
    }

    // GET RELEVANT PRECINTS
    ArrayList<Long> relevantPrecincts = null;
    if (isMultiComponentTransform()) {
      ViewWindowField tmpWOI = new ViewWindowField(viewWindow);
      tmpWOI.comps = getRelevantComponents(viewWindow.comps);
      relevantPrecincts = codestream.findRelevantPrecincts(tmpWOI);
    } else {
      relevantPrecincts = codestream.findRelevantPrecincts(viewWindow);

    }

    // READ PRECINCTS
    packetHeadersDecoder.reset();
    readRelevantPrecincts(relevantPrecincts);


    // DECODE BYTE-STREAM
    //clientCache.list(System.out);
    imageSamplesFloat = jpeg2kDecoder.decode(viewWindow);

    // Unload all precinct except those belonging to the LL subband
    for (long inClassIdentifier : relevantPrecincts) {
      int TCRP[] = codestream.findTCRP(inClassIdentifier);

      codestream.getTile(TCRP[0]).getComponent(TCRP[1]).getResolutionLevel(TCRP[2]).removePrecinct(TCRP[3]);

    }


    // SET DECODED IMAGE DATA
    imageData.lock();
    try {
      imageData.setFrameSize(viewWindow.fsiz[0], viewWindow.fsiz[1]);
      imageData.setRegionOffset(viewWindow.roff[0], viewWindow.roff[1]);
      imageData.setRegionSize(viewWindow.rsiz[0], viewWindow.rsiz[1]);
      imageData.setComponents(components);
      imageData.setResolutionLevel(codestream.getWTLevels() - discardLevels);
      imageData.setLayers(viewWindow.layers);
      imageData.setQuality(0);
      imageData.setPrecision(codestream.getPrecision(codestream.getRelevantComponents(viewWindow.comps)));

      if (imageDataType == ImageData.SAMPLES_FLOAT) {
        imageData.setData(imageSamplesFloat);
      } else {
        imageData.setData(imageSamplesToBufferedImage(imageSamplesFloat));
      }
    } finally {
      imageData.unlock();
    }

  }

  /**
   * Returns the JPC parameters.
   *
   * @return the JPC parameters.
   */
  public ClientJPEG2KCodestream getCodestream() {

    if (isMainHeaderDecoded) {
      return codestream;
    }
    boolean isCorrect = false;
    try {
      isCorrect = decodeMainHeader();
    } catch (ErrorException e) {
      return null;
    }

    return isCorrect ? codestream : null;
  }

  /**
   * Return the image width.
   *
   * @return the image with.
   */
  public final int getWidth() {
    return codestream != null ? codestream.getXSize() : -1;
  }

  /**
   * Return the image height
   *
   * @return the image height
   */
  public final int getHeight() {
    return codestream != null ? codestream.getYSize() : -1;
  }

  /**
   * Returns the maximum number of layers.
   *
   * @return the maximum number of layers
   */
  public int getMaxLayers() {
    return codestream != null ? codestream.getNumLayers() : -1;
  }

  /**
   * This method gets a description of the logical target. It is recomended
   * the description was done as HTML table (for instance, an HTML table with
   * a description of th logical target parameters).
   *
   * @return a string with the logical target description.
   */
  public String getLogicalTargetDescription() {

    JPCParameters jpcParameters = getCodestream().getJPCParameters();

    String str = "";
    str += "<html>";
    str += "<HEAD>" + generateCSS() + "</HEAD>";
    str += "<BODY>";

    str += "<br>";
    str += "<P class=\"title\">JPEG2000 IMAGE PARAMETERS</P>";


    str += generateSIZDescription(jpcParameters.sizParameters);

    str += generateCODDescription(jpcParameters.codParameters);
    str += generateCOCDescription(jpcParameters.cocParametersList);

    str += generateQCDDescription(jpcParameters.qcdParameters);
    str += generateQCCDescription(jpcParameters.qccParametersList);

    str += generateJPKDescription(jpcParameters.jpkParameters);

    str += "</BODY>";
    str += "</html>";

    return str;
  }

  // ============================ private methods ==============================
  /**
   * Decodes the image main header.
   *
   * @throws WarningException
   */
  private boolean decodeMainHeader() throws ErrorException {

    if (isMainHeaderDecoded) {
      return true;
    }
    //decodeMetaData();
    if (clientCache.getMainHeader() == null) {
      return false;
    }

    // Try to decode the main header
    JPCParameters jpcParameters = null;
    BufferedDataInputStream in = null;
    try {
      in = new BufferedDataInputStream(clientCache.getMainHeader());
      JPCMainHeaderDecoder jpcDeheading = new JPCMainHeaderDecoder(in);
      jpcDeheading.run();
      jpcParameters = jpcDeheading.getJPCParameters();
    } catch (ErrorException ee) {

      // Perhaps the main header is a private main header
      // JPK headers are generated by BOICode
      try {
        in = new BufferedDataInputStream(clientCache.getMainHeader());
        JPKMainHeaderDecoder jpkDeheading = new JPKMainHeaderDecoder(in);
        jpkDeheading.run();
        jpcParameters = jpkDeheading.getJPCParameters();
      } catch (ErrorException e) {
        isMainHeaderDecoded = false;
        throw new ErrorException("Main header can not be decoded");
      } catch (Exception e) {
        throw new ErrorException("Main header can not be decoded");
      }
    } catch (Exception e) {
      throw new ErrorException("Main header can not be decoded");
    }


    isMainHeaderDecoded = true;

    //jpcParameters.list(System.out);
    log.logInfo("MAIN HEADER DECODED");

    //jpcParameters.list(System.out);

    // BUILD TILES AND COMPONENTS STRUCTURE
    codestream = new ClientJPEG2KCodestream(0, jpcParameters);

    int numTiles = codestream.getNumTiles();
    int numComponents = 0;
    ClientJPEG2KTile tileObj = null;
    ClientJPEG2KComponent componentObj = null;

    for (int t = 0; t < numTiles; t++) {
      codestream.createTile(t);
      tileObj = codestream.getTile(t);
      numComponents = codestream.getZSize();
      for (int c = 0; c < numComponents; c++) {
        tileObj.createComponent(c);
        componentObj = tileObj.getComponent(c);
        int maxWTLevels = componentObj.getWTLevels();
        for (int r = 0; r <= maxWTLevels; r++) {
          componentObj.createResolutionLevel(r);
        }
      }
    }


    // Initialize the packet deheading
    packetHeadersDecoder = new PacketHeadersDecoder(codestream);

    // Initialize JPEG2000 decoder engine
    jpeg2kDecoder = new JPEG2KDecoder(codestream, numThreads);

    // Set image data properties
    imageData.setMaxComponents(jpcParameters.sizParameters.zSize);
    imageData.setMaxResolutionLevel(jpcParameters.codParameters.WTLevels);
    imageData.setMaxLayers(jpcParameters.codParameters.numLayers);

    return true;
  }

  /**
   * This function calculates the view window which will be decoded taken
   * into account the requested view window (provided by the user
   * application) and the response view window (sent by the server).
   *
   * @param requestViewWindow
   * @param responseViewWindow
   *
   * @return the WOI to be decoded.
   */
  private ViewWindowField calculateWOIToDecode(ViewWindowField requestViewWindow,
                                               ViewWindowField responseViewWindow) {

    // View window to decode is set to requested view window from user application
    ViewWindowField viewWindowToDecode = new ViewWindowField();
    ViewWindowField.deepCopy(requestViewWindow, viewWindowToDecode);

    int imWidth = codestream.getXSize();
    int imHeight = codestream.getYSize();

    if ((viewWindowToDecode.fsiz[0] < 0) || (viewWindowToDecode.fsiz[0] < 1)) {
      viewWindowToDecode.fsiz[0] = imWidth;
      viewWindowToDecode.fsiz[1] = imHeight;
      viewWindowToDecode.roundDirection = ViewWindowField.ROUND_DOWN;
    } else {
      if (viewWindowToDecode.fsiz[0] > imWidth) {
        viewWindowToDecode.fsiz[0] = imWidth;
      }
      if (viewWindowToDecode.fsiz[1] > imHeight) {
        viewWindowToDecode.fsiz[1] = imHeight;
      }
    }

    if ((viewWindowToDecode.roff[0] < 0) || (viewWindowToDecode.roff[1] < 0)) {
      viewWindowToDecode.roff[0] = 0;
      viewWindowToDecode.roff[1] = 0;
    } else {
      assert (viewWindowToDecode.roff[0] >= 0);
      assert (viewWindowToDecode.roff[1] >= 0);
    }

    if ((viewWindowToDecode.rsiz[0] < 0) || (viewWindowToDecode.rsiz[1] < 0)) {
      viewWindowToDecode.rsiz[0] = viewWindowToDecode.fsiz[0] - viewWindowToDecode.roff[0];
      viewWindowToDecode.rsiz[1] = viewWindowToDecode.fsiz[1] - viewWindowToDecode.roff[1];
    } else {
      if ((viewWindowToDecode.roff[0] + viewWindowToDecode.rsiz[0])
              > viewWindowToDecode.fsiz[0]) {
        viewWindowToDecode.rsiz[0] = viewWindowToDecode.fsiz[0]
                - viewWindowToDecode.roff[0];
      }
      if ((viewWindowToDecode.roff[1] + viewWindowToDecode.rsiz[1])
              > viewWindowToDecode.fsiz[1]) {
        viewWindowToDecode.rsiz[1] = viewWindowToDecode.fsiz[1]
                - viewWindowToDecode.roff[1];
      }
    }

    // Get the number of discard levels
    int discardLevels =
            codestream.determineNumberOfDiscardLevels(viewWindowToDecode.fsiz,
            viewWindowToDecode.roundDirection);

    // Fit requested region to the suitable resolution
    codestream.mapRegionToSuitableResolutionGrid(viewWindowToDecode.fsiz,
            viewWindowToDecode.roff,
            viewWindowToDecode.rsiz,
            discardLevels);

    // Update view window with parameters received from server
    if (responseViewWindow != null) {
      if (responseViewWindow.fsiz != null) {
        if ((responseViewWindow.fsiz[0] > 0) && (responseViewWindow.fsiz[1] > 0)) {
          viewWindowToDecode.fsiz[0] = responseViewWindow.fsiz[0];
          viewWindowToDecode.fsiz[1] = responseViewWindow.fsiz[1];
        }
      }
      if (responseViewWindow.rsiz != null) {
        if ((responseViewWindow.rsiz[0] > 0) && (responseViewWindow.rsiz[1] > 0)) {
          viewWindowToDecode.rsiz[0] = responseViewWindow.rsiz[0];
          viewWindowToDecode.rsiz[1] = responseViewWindow.rsiz[1];
        }
      }
      if (responseViewWindow.roff != null) {
        if ((responseViewWindow.roff[0] >= 0) && (responseViewWindow.roff[1] >= 0)) {
          viewWindowToDecode.roff[0] = responseViewWindow.roff[0];
          viewWindowToDecode.roff[1] = responseViewWindow.roff[1];
        }
      }
      if (responseViewWindow.layers != -1) {
        viewWindowToDecode.layers = responseViewWindow.layers;
      }

      if (isMultiComponentTransform()) {
      } else {
        if (responseViewWindow.comps != null) {
          viewWindowToDecode.comps = new int[responseViewWindow.comps.length][2];
          for (int i = 0; i < responseViewWindow.comps.length; i++) {
            viewWindowToDecode.comps[i][0] = responseViewWindow.comps[i][0];
            viewWindowToDecode.comps[i][1] = responseViewWindow.comps[i][1];
          }
        }
      }

    }

    return viewWindowToDecode;
  }

  /**
   * Check if the logical target has a multiple component transformation.
   *
   * @return <code>true</code> if the logical target is spectrally
   * 			transformed. Otherwise, returns <code>false</code>.
   */
  public boolean isMultiComponentTransform() {
    return codestream.isMultiComponentTransform();
  }

  /**
   * Calculates which are the necessary components to invert a multiple
   * component transformation.
   *
   * compsRanges the range of components to be decompressed. The first
   * 			index is an array index of the ranges. An the second index
   * 			indicates: 0 is the first component of the range, and 1 is the
   * 			last component of the range.
   *
   * @return a bi-dimensional array with then required range of components.
   * 			Indexes mean the same as the <code>compsRanges</code> input
   * 			parameter.
   * @throws ErrorException
   */
  public int[][] getRelevantComponents(int[][] comps) throws ErrorException {

    // If comps are null, request for all components
    if (comps == null) {
      return null;
    }

    // If no multiple component transformation is applied,
    // no extra components are necessary
    if (!isMultiComponentTransform()) {
      return comps;
    }

    // Get necessary components to invert the multiple
    // component transformation
    int[][] necCompsRanges = null;
    try {
      if (codestream.getMultiComponentTransform() > 0) {
        CalculateRelevantComponents crc = new CalculateRelevantComponents(
                codestream.getMultiComponentTransform(),
                codestream.getZSize(),
                codestream.getCBDParameters(),
                codestream.getMCTParameters(),
                codestream.getMCCParameters(),
                codestream.getMCOParameters(),
                comps);
        necCompsRanges = crc.run();
      } else if (codestream.getJPKParameters().WT3D != 0) {
        CalculateRelevantComponents crc = new CalculateRelevantComponents(
                codestream.getJPKParameters(), codestream.getZSize(), comps);
        necCompsRanges = crc.run();
      } else {
        assert (true);
      }
    } catch (ErrorException e) {
      throw new ErrorException("Unsupported multicomponent transform");
    }

    return necCompsRanges;
  }

  /**
   * Reads the data from the relevant precincts.
   *
   * @throws ErrorException
   */
  private void readRelevantPrecincts(ArrayList<Long> relevantPrecincts) throws ErrorException {
    //System.out.println("\n=== READING RELEVANT PRECINCTS ==="); // DEBUG

    for (int precIndex = 0; precIndex < relevantPrecincts.size(); precIndex++) {

      long inClassIdentifier = relevantPrecincts.get(precIndex);
      //System.out.println("\n-----------------------------------"); // DEBUG
      //System.out.println("In Class Identifier: " + inClassIdentifier+ " => tile="+TCRP[0]+" z="+TCRP[1]+" rLevel="+TCRP[2]+" precinct="+TCRP[3]); // DEBUG

      if (clientCache.getDatabinLength(ClassIdentifiers.PRECINCT, inClassIdentifier) <= 0) {
        continue;
      }
      
      PrecinctDataBin precinctDataBin = clientCache.getPrecinctDataBin(inClassIdentifier);
      precinctDataBin.seek(0);
      
      int TCRP[] = codestream.findTCRP(inClassIdentifier);
      if (!codestream.getTile(TCRP[0]).getComponent(TCRP[1]).getResolutionLevel(TCRP[2]).isPrecinct(TCRP[3])) {
        codestream.getTile(TCRP[0]).getComponent(TCRP[1]).getResolutionLevel(TCRP[2]).createPrecinct(TCRP[3], precinctDataBin);
      }
      ClientJPEG2KPrecinct precinctObj = codestream.getTile(TCRP[0]).getComponent(TCRP[1]).getResolutionLevel(TCRP[2]).getPrecinct(TCRP[3]);

      boolean finish = false;
      for (int layer = 0; (layer < viewWindow.layers) && (!finish); layer++) {
        
        int[][][][] precinctData = null;	// PecinctData[subband][yBlock][xBlock][cp]
        try {
          precinctData = packetHeadersDecoder.packetHeaderDecoding(new PacketHeaderDataInputStream(precinctDataBin), inClassIdentifier);
        } catch (EOFException e) {
          // End Of Data has been reached.
          // Packet header has not been decoded completely
        } catch (ErrorException e) {
          throw new ErrorException("The packet headers have some error and they can not be decoded correctly.");
        } catch (IOException e) {
          continue;
        }

        // READ PACKET BODY
        if (precinctData != null) {

          for (int subband = 0; subband < precinctData.length; subband++) {
            //System.out.println("\tsubband="+subband); // DEBUG
            if (precinctData[subband] == null) {
              continue;
            }

            for (int yBlock = 0; yBlock < precinctData[subband].length; yBlock++) {
              //System.out.println("\t\tyBlock="+yBlock); // DEBUG
              if (precinctData[subband][yBlock] == null) {
                continue;
              }

              for (int xBlock = 0; xBlock < precinctData[subband][yBlock].length; xBlock++) {
                //System.out.println("\t\t\txBlock="+xBlock); // DEBUG
                if (precinctData[subband][yBlock][xBlock] == null) {
                  continue;
                }

                int[] offsets = new int[precinctData[subband][yBlock][xBlock].length];
                for (int cp = 0; cp < precinctData[subband][yBlock][xBlock].length; cp++) {
                  //System.out.println("\t\t\tsubband="+subband+" yBlock="+yBlock+" xBlock="+xBlock+" cp="+(numCodingPasses+cp)+" offset="+in.getPos()+" length="+precinctData[subband][yBlock][xBlock][cp]);
                  if (precinctData[subband][yBlock][xBlock][cp] != 0) {

                    // Read packet data
                    int dataLength = 0;
                    if (precinctData[subband][yBlock][xBlock][cp] <= precinctDataBin.available()) {
                      dataLength = precinctData[subband][yBlock][xBlock][cp];
                    } else {
                      dataLength = (int) precinctDataBin.available();
                      finish = true;
                    }
                    if (dataLength > 0) {
                      offsets[cp] = (int) precinctDataBin.getPos();
                      precinctData[subband][yBlock][xBlock][cp] = dataLength;

                      precinctDataBin.skipBytes(dataLength);

                    } else {
                      offsets[cp] = 0;
                      precinctData[subband][yBlock][xBlock][cp] = 0;
                    }
                  } else {
                    offsets[cp] = 0;
                    precinctData[subband][yBlock][xBlock][cp] = 0;
                  }
                }
                precinctObj.addOffsetsAndLengths(TCRP[2] == 0 ? 0 : subband + 1, yBlock, xBlock, offsets, precinctData[subband][yBlock][xBlock]);
              }
            }
          }
        }
      } // layers

      try {
        precinctObj.setZeroBitPlanes(packetHeadersDecoder.getZeroBitPlanes(inClassIdentifier));
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * NOTE: this is a temporary method to convert from a three dimensional
   * array representation to a bufferered image representation.
   *
   * @param imageSamplesFloat a three-dimensional array with the image
   * 			samples
   *
   * @return a buffered image.
   */
  private BufferedImage imageSamplesToBufferedImage(float[][][] imageSamplesFloat) {

    if (imageData == null) {
      if (true) {
        System.exit(0);
      }
      throw new IllegalArgumentException();
    }

    // Image sizes
    int zSize = components.length;
    int ySize = imageSamplesFloat[0].length;
    int xSize = imageSamplesFloat[0][0].length;

    int colorModel;
    switch (zSize) {
      case 1:
        colorModel = BufferedImage.TYPE_BYTE_GRAY;
        break;
      case 3:
        colorModel = BufferedImage.TYPE_INT_RGB;
        break;
      default:
        throw new IllegalArgumentException("Only 1 or 3 are supported");
    }

    // Allocate memory only
    // - if the object has not been created
    // - dimensions are different from the previous ones

    if ((fImagePixels == null) || (fImagePixels.length != zSize * ySize * xSize)) {
      fImagePixels = new float[zSize * ySize * xSize];
    } else {
    }

    // Fill the pixels
    int i = 0;
    for (int y = 0; y < ySize; y++) {
      for (int x = 0; x < xSize; x++) {
        for (int z = 0; z < zSize; z++) {
          fImagePixels[i++] = imageSamplesFloat[z][y][x];
        }
      }
    }


    bufferedImage = new BufferedImage(xSize, ySize, colorModel);
    WritableRaster raster = bufferedImage.getRaster();
    raster.setPixels(0, 0, xSize, ySize, fImagePixels);

    return bufferedImage;
  }

  private String generateCSS() {
    String css = "<style type=\"text/css\">";

    css += "p{ color: black; font-size: 10px; }";
    css += "p.title{ color: black; font-size: 20px; text-align:center }";


    css += "p.section {"
            + "font-family: Verdana, Geneva, Arial, Helvetica, sans-serif;"
            + "font-size: 16pt;"
            + "color: #606060;"
            + "padding-top: 10px;"
            + "padding-bottom: 3px;"
            + "font-weight:bold;"
            + "}";

    css += "p.subsection {"
            + "font-family: Verdana, Geneva, Arial, Helvetica, sans-serif;"
            + "font-size: 14pt;"
            + "color: #606060;"
            + "padding-top: 10px;"
            + "padding-bottom: 3px;"
            + "font-weight:bold;"
            + "text-indent:50px;"
            + "}";

    css += "table.tabla {"
            + "font-family: Verdana, Arial, Helvetica, sans-serif;"
            + "font-size:10px;"
            + "text-align: left;"
            + "width: 600px;"
            + "color: #000;"
            + "background-color: #C0C0C0;"
            + "border: 1px #6699CC solid;"
            + "border-collapse: collapse;"
            + "border-spacing: 0px;"
            + "};";

    css += "table.tabla td {"
            + "background-color: #C0C0C0;"
            + "color: #000;"
            + "padding: 4px;"
            + "text-align: left;"
            + "border: 1px #fff solid;"
            + "}";

    css += "table.tabla td.hed {"
            + "background-color: #C0C0C0;"
            + "color: #fff;"
            + "padding: 4px;"
            + "text-align: left;"
            + "border-bottom: 2px #fff solid;"
            + "font-size: 12px;"
            + "font-weight: bold;"
            + "}";

    css += ".oddrowcolor {"
            + "background-color:#C0C0C0;"
            + "}";

    css += ".evenrowcolor {"
            + "background-color:#E0E0E0;"
            + "}";

    css += "</style>";

    return css;
  }

  private String generateSIZDescription(SIZParameters sizParameters) {
    String str = "";
    str += "<P>";
    str += "<P class=\"section\">SIZ Parameters</P>";
    str += "<HR>";
    str += "<TABLE class=\"tabla\" id=\"alternatecolor\">";

    // Rsiz
    str += "<TR class=\"evenrowcolor\"><TD>Rsiz</TD> <TD>" + sizParameters.Rsiz + "</TD></TR>";
    // xSize, ySize
    str += "<TR class=\"oddrowcolor\"><TD>Image size</TD> <TD>"
            + sizParameters.xSize + "x" + sizParameters.ySize + "</TD></TR>";
    // XOsiz, YOsiz
    str += "<TR class=\"evenrowcolor\"><TD>Image offset</TD> <TD>" + sizParameters.XOsize
            + " , " + sizParameters.YOsize + "</TD></TR>";
    str += "<TR class=\"oddrowcolor\"><TD>Tile sizes</TD> <TD>"
            + sizParameters.XTsize + "x" + sizParameters.YTsize + "</TD></TR>";

    str += "<TR class=\"evenrowcolor\"><TD>Tile offset</TD> <TD>" + sizParameters.XTOsize
            + " , " + sizParameters.YTOsize + "</TD></TR>";
    str += "<TR class=\"oddrowcolor\"><TD>Num. components</TD> <TD>" + sizParameters.zSize + "</TD></TR>";

    // Components bits
    if (sizParameters.precision != null) {
      str += "<TR class=\"evenrowcolor\"><TD>Bit depth</TD> <TD>";
      str += "<TABLE border=\"0\"><TR>";
      str += "<TD align=\"right\">" + "Component" + "</TD>";
      for (int i = 0; i < sizParameters.precision.length; i++) {
        str += "<TD align=\"center\">" + i + "</TD>";
      }
      str += "</TR><TR>";
      str += "<TD align=\"right\">" + "Bits" + "</TD>";
      for (int i = 0; i < sizParameters.precision.length; i++) {
        str += "<TD align=\"center\">" + sizParameters.precision[i] + "</TD>";
      }
      str += "</TR></TABLE>";
      str += "</TD></TR>";
    }

    // Signed components
    if (sizParameters.signed != null) {
      str += "<TR class=\"oddrowcolor\"><TD>Signed Components</TD> <TD>";
      str += "<TABLE border=\"0\">";
      str += "<TR>";
      str += "<TD align=\"right\">" + "Component" + "</TD>";
      for (int i = 0; i < sizParameters.signed.length; i++) {
        str += "<TD align=\"center\">" + i + "</TD>";
      }
      str += "</TR><TR>";
      str += "<TD align=\"right\">" + "Signed" + "</TD>";
      for (int i = 0; i < sizParameters.signed.length; i++) {
        str += "<TD align=\"center\">" + sizParameters.signed[i] + "</TD>";
      }
      str += "</TR>";
      str += "</TABLE>";
      str += "</TD>";
    }

    // Samples separation
    if (sizParameters.XRsiz != null) {
      str += "<TR class=\"evenrowcolor\"><TD>Samples separation</TD> <TD>";
      str += "<TABLE border=\"0\">";
      str += "<TR>";
      str += "<TD align=\"right\">" + "Component" + "</TD>";
      for (int i = 0; i < sizParameters.signed.length; i++) {
        str += "<TD align=\"center\">" + i + "</TD>";
      }
      str += "</TR><TR>";
      str += "<TD align=\"right\">" + "XRsiz" + "</TD>";
      for (int i = 0; i < sizParameters.XRsiz.length; i++) {
        str += "<TD align=\"center\">" + sizParameters.XRsiz[i] + "</TD>";
      }
      str += "</TR>";
      str += "</TR><TR>";
      str += "<TD align=\"right\">" + "YRsiz" + "</TD>";
      for (int i = 0; i < sizParameters.YRsiz.length; i++) {
        str += "<TD align=\"center\">" + sizParameters.YRsiz[i] + "</TD>";
      }
      str += "</TR>";
      str += "</TABLE>";
    }
    str += "</TABLE>";

    return str;
  }

  private String generateCODDescription(CODParameters codParameters) {

    String str = "";

    // COD PARAMETERS
    str += "<H3>COD Parameters</H3>";
    str += "<HR>";
    str += "<TABLE class=\"tabla\">";

    // Progression order
    str += "<TR class=\"evenrowcolor\"><TD>Progression Order</TD> <TD>";
    switch (codParameters.progressionOrder) {
      case 0:
        str += "No multiple transform";
        break;
      case 1:
        str += "Component transformation used on components 0, 1, and 2";
        break;
      case 2:
        str += "Array-based";
        break;
      case 3:
        str += "Array-based + DWT-based";
        break;
      case 4:
        str += "DWT-based";
        break;
    }
    str += "</TD></TR>";

    // Layers
    str += "<TR class=\"oddrowcolor\"><TD>Num. Layers</TD> <TD>" + codParameters.numLayers + "</TD></TR>";

    // Multicomponent transform
    str += "<TR class=\"evenrowcolor\"><TD>Multi component transform</TD> <TD>";
    switch (codParameters.multiComponentTransform) {
      case 0:
        str += "LRCP Layer-Resolution-Component-Position";
        break;
      case 1:
        str += "RLCP Resolution-Layer-Component-Position";
        break;
      case 2:
        str += "RPCL Resolution-Position-Component-Layer";
        break;
      case 3:
        str += "PCRL Position-Component-Resolution-Layer";
        break;
      case 4:
        str += "CPRL Component-Position-Resolution-Layer";
        break;
    }
    str += "</TD></TR>";

    // Wavelet type
    str += "<TR class=\"oddnrowcolor\"><TD>Multi component transform</TD> <TD>";
    switch (codParameters.WTType) {
      case 0:
        str += "Nothing";
        break;
      case 1:
        str += "Reversible 5/3 DWT";
        break;
      case 2:
        str += "Irreversible 9/7 DWT";
        break;
    }
    str += "</TD></TR>";

    // Wavelet levels
    str += "<TR class=\"evenrowcolor\"><TD>WT levels</TD> <TD>" + codParameters.WTLevels + "</TD></TR>";

    // MQ Flags
    str += "<TR class=\"oddrowcolor\"><TD>Style of coding passes</TD> <TD>";
    if (codParameters.bypass) {
      str += "BYPASS";
    }
    if (codParameters.reset) {
      str += " RESET";
    }
    if (codParameters.restart) {
      str += " RESTART";
    }
    if (codParameters.causal) {
      str += " CAUSAL";
    }
    if (codParameters.erterm) {
      str += " ER_TERM";
    }
    if (codParameters.segmark) {
      str += " SEG_MARK";
    }
    str += "</TD></TR>";

    // Block sizes
    str += "<TR class=\"evenrowcolor\"><TD>Block sizes</TD> <TD>"
            + (1 << codParameters.blockWidth)
            + "x" + (1 << codParameters.blockHeight) + "</TD></TR>";

    // Precinct sizes
    str += "<TR class=\"oddrowcolor\"><TD>Block Sizes</TD> <TD>";
    str += "<TABLE border=\"0\"><TR>";
    str += "<TD align=\"right\">" + "Res. level" + "</TD>";
    for (int i = 0; i < codParameters.precinctWidths.length; i++) {
      str += "<TD align=\"center\">" + i + "</TD>";
    }
    str += "</TR><TR>";
    str += "<TD align=\"right\">" + "Size" + "</TD>";
    for (int i = 0; i < codParameters.precinctWidths.length; i++) {
      str += "<TD align=\"center\">" + (1 << codParameters.precinctWidths[i])
              + "x" + (1 << codParameters.precinctHeights[i]) + "</TD>";
    }
    str += "</TR></TABLE>";
    str += "</TD></TR>";


    // SOP, EPH
    str += "<TR class=\"evenrowcolor\"><TD>Packet Headers Markers</TD> <TD>";
    str += " SOP=" + codParameters.useSOP;
    str += " EPH=" + codParameters.useEPH;
    str += "</TD></TR>";

    str += "</TABLE>"; // cod parameters

    return str;
  }

  private String generateCOCDescription(HashMap<Integer, COCParameters> cocParametersList) {

    if (cocParametersList == null) {
      return "";
    }

    String str = "";

    str += "<H3>COC Parameters</H3>";
    str += "<HR>";
    for (Map.Entry<Integer, COCParameters> entry : cocParametersList.entrySet()) {

      COCParameters cocParameters = entry.getValue();

      // COD PARAMETERS

      str += "<H5>Component " + entry.getKey() + "</H5>";
      str += "<TABLE class=\"tabla\">";

      // Wavelet type
      str += "<TR class=\"oddnrowcolor\"><TD>Multi component transform</TD> <TD>";
      switch (cocParameters.WTType) {
        case 0:
          str += "Nothing";
          break;
        case 1:
          str += "Reversible 5/3 DWT";
          break;
        case 2:
          str += "Irreversible 9/7 DWT";
          break;
      }
      str += "</TD></TR>";

      // Wavelet levels
      str += "<TR class=\"evenrowcolor\"><TD>WT levels</TD> <TD>" + cocParameters.WTLevels + "</TD></TR>";

      // MQ Flags
      str += "<TR class=\"oddrowcolor\"><TD>Style of coding passes</TD> <TD>";
      if (cocParameters.bypass) {
        str += "BYPASS";
      }
      if (cocParameters.reset) {
        str += " RESET";
      }
      if (cocParameters.restart) {
        str += " RESTART";
      }
      if (cocParameters.causal) {
        str += " CAUSAL";
      }
      if (cocParameters.erterm) {
        str += " ER_TERM";
      }
      if (cocParameters.segmark) {
        str += " SEG_MARK";
      }
      str += "</TD></TR>";

      // Block sizes
      str += "<TR class=\"evenrowcolor\"><TD>Block sizes</TD> <TD>"
              + (1 << cocParameters.blockWidth)
              + "x" + (1 << cocParameters.blockHeight) + "</TD></TR>";

      // Precinct sizes
      str += "<TR class=\"oddrowcolor\"><TD>Block Sizes</TD> <TD>";
      str += "<TABLE border=\"0\"><TR>";
      str += "<TD align=\"right\">" + "Res. level" + "</TD>";
      for (int i = 0; i < cocParameters.precinctWidths.length; i++) {
        str += "<TD align=\"center\">" + i + "</TD>";
      }
      str += "</TR><TR>";
      str += "<TD align=\"right\">" + "Size" + "</TD>";
      for (int i = 0; i < cocParameters.precinctWidths.length; i++) {
        str += "<TD align=\"center\">" + (1 << cocParameters.precinctWidths[i])
                + "x" + (1 << cocParameters.precinctHeights[i]) + "</TD>";
      }
      str += "</TR></TABLE>";
      str += "</TD></TR>";

      str += "</TABLE>"; // cod parameters

    }

    return str;
  }

  private String generateQCDDescription(QCDParameters qcdParameters) {
    String str = "";

    str += "<H3>QCD Parameters</H3>";
    str += "<HR>";

    // Quantization style
    str += "<TR class=\"evenrowcolor\"><TD>Quantization style</TD> <TD>";
    switch (qcdParameters.quantizationStyle) {
      case 1:
        str += "<TD> Reversible </TD>";
        break;
      case 2:
        str += "<TD> Irreversible </TD>";
        break;
    }
    str += "</TD></TR>";


    // Quantization exponents
    if (qcdParameters.exponents != null) {
      str += "<TR class=\"oddrowcolor\"><TD>Exponents</TD>";
      str += "<TD>";
      str += "<TABLE border=\"0\"><TR align=\"center\">";
      str += "<TR><TD>Res. level</TD> <TD>LL</TD> <TD>LH</TD> <TD>HL</TD> <TD>HH</TD></TR>";
      for (int i = 0; i < qcdParameters.exponents.length; i++) {
        str += "<TR>";
        str += "<TD>" + i + "</TD>";
        if (i == 0) {
          str += "<TD>" + qcdParameters.exponents[0][0] + "</TD><TD></TD> <TD></TD> <TD></TD></TR>";
        } else {
          str += "<TD></TD>";
          str += "<TD>" + qcdParameters.exponents[i][0] + "</TD> <TD>"
                  + qcdParameters.exponents[i][1] + "</TD> <TD>"
                  + qcdParameters.exponents[i][2] + "</TD>";

          str += "</TR>";
        }

      }
      str += "</TR>";
      str += "</TABLE>";
    }

    // Quantization mantisas
    if (qcdParameters.exponents != null) {
      str += "<TR class=\"evenrowcolor\"><TD>Mantisas</TD>";
      str += "<TD>";
      str += "<TABLE border=\"0\"><TR align=\"center\">";
      str += "<TR><TD>Res. level</TD> <TD>LL</TD> <TD>LH</TD> <TD>HL</TD> <TD>HH</TD></TR>";
      for (int i = 0; i < qcdParameters.mantisas.length; i++) {
        str += "<TR>";
        str += "<TD>" + i + "</TD>";
        if (i == 0) {
          str += "<TD>" + qcdParameters.mantisas[0][0] + "</TD><TD></TD> <TD></TD> <TD></TD></TR>";
        } else {
          str += "<TD></TD>";
          str += "<TD>" + qcdParameters.mantisas[i][0] + "</TD> <TD>"
                  + qcdParameters.mantisas[i][1] + "</TD> <TD>"
                  + qcdParameters.mantisas[i][2] + "</TD>";

          str += "</TR>";
        }

      }
      str += "</TR>";
      str += "</TABLE>";
    }

    // Guard bits
    str += "<TR class=\"oddrowcolor\"><TD>Guard Bits</TD> <TD>" + qcdParameters.guardBits + "</TD></TR>";

    str += "</TABLE>";

    return str;
  }

  private String generateQCCDescription(HashMap<Integer, QCCParameters> qccParametersList) {

    if (qccParametersList == null) {
      return "";
    }

    String str = "";

    str += "<H3>QCC Parameters</H3>";
    str += "<HR>";

    for (Map.Entry<Integer, QCCParameters> entry : qccParametersList.entrySet()) {

      QCCParameters qccParameters = entry.getValue();

      str += "<H5>Component " + entry.getKey() + "</H5>";
      str += "<TABLE class=\"tabla\">";

      // Quantization style
      str += "<TR class=\"evenrowcolor\"><TD>Quantization style</TD> <TD>";
      switch (qccParameters.quantizationStyle) {
        case 1:
          str += "<TD align=\"center\"> Reversible </TD>";
          break;
        case 2:
          str += "<TD align=\"center\"> Irreversible </TD>";
          break;
      }
      str += "</TD></TR>";


      // Quantization exponents
      if (qccParameters.exponents != null) {
        str += "<TR class=\"oddrowcolor\"><TD>Exponents</TD>";
        str += "<TD>";
        str += "<TABLE border=\"0\"><TR align=\"center\">";
        str += "<TR><TD>Res. level</TD> <TD>LL</TD> <TD>LH</TD> <TD>HL</TD> <TD>HH</TD></TR>";
        for (int i = 0; i < qccParameters.exponents.length; i++) {
          str += "<TR>";
          str += "<TD>" + i + "</TD>";
          if (i == 0) {
            str += "<TD>" + qccParameters.exponents[0][0] + "</TD><TD></TD> <TD></TD> <TD></TD></TR>";
          } else {
            str += "<TD></TD>";
            str += "<TD>" + qccParameters.exponents[i][0] + "</TD> <TD>"
                    + qccParameters.exponents[i][1] + "</TD> <TD>"
                    + qccParameters.exponents[i][2] + "</TD>";

            str += "</TR>";
          }

        }
        str += "</TR>";
        str += "</TABLE>";
      }

      // Quantization mantisas
      if (qccParameters.exponents != null) {
        str += "<TR class=\"evenrowcolor\"><TD>Mantisas</TD>";
        str += "<TD>";
        str += "<TABLE border=\"0\"><TR align=\"center\">";
        str += "<TR><TD>Res. level</TD> <TD>LL</TD> <TD>LH</TD> <TD>HL</TD> <TD>HH</TD></TR>";
        for (int i = 0; i < qccParameters.mantisas.length; i++) {
          str += "<TR>";
          str += "<TD>" + i + "</TD>";
          if (i == 0) {
            str += "<TD>" + qccParameters.mantisas[0][0] + "</TD><TD></TD> <TD></TD> <TD></TD></TR>";
          } else {
            str += "<TD></TD>";
            str += "<TD>" + qccParameters.mantisas[i][0] + "</TD> <TD>"
                    + qccParameters.mantisas[i][1] + "</TD> <TD>"
                    + qccParameters.mantisas[i][2] + "</TD>";

            str += "</TR>";
          }

        }
        str += "</TR>";
        str += "</TABLE>";
      }

      // Guard bits
      str += "<TR class=\"oddrowcolor\"><TD>Guard Bits</TD> <TD>" + qccParameters.guardBits + "</TD></TR>";

      str += "</TABLE>";
    }

    return str;
  }

  private String generateJPKDescription(JPKParameters jpkParameters) {

    if (jpkParameters == null) {
      return "";
    }

    String str = "";

    str += "<H3>JPK Parameters</H3>";
    str += "<HR>";
    str += "<TABLE class=\"tabla\">";

    // Level shift
    str += "<TR class=\"evenrowcolor\"><TD>Level Shift Type</TD> <TD>";
    switch (jpkParameters.LSType) {
      case 0:
        str += "No level shift";
        break;
      case 1:
        str += "JPEG2000 standard level shifting (only non-negative components)";
        break;
      case 2:
        str += "Range center substract";
        break;
      case 3:
        str += "Average substract";
        break;
      case 4:
        str += "Specific values substract (see Subs Values)";
        break;
    }
    str += "</TD></TR>";

    // Level shift components
    if (jpkParameters.LSComponents != null) {
      str += "<TR class=\"oddrowcolor\"><TD>Level shift components</TD> <TD>";
      str += "<TABLE border=\"0\"><TR>";
      str += "<TD align=\"right\">" + "Component" + "</TD>";
      for (int i = 0; i < jpkParameters.LSComponents.length; i++) {
        str += "<TD align=\"center\">" + i + "</TD>";
      }
      str += "</TR><TR>";
      str += "<TD align=\"right\">" + "Applied" + "</TD>";
      for (int i = 0; i < jpkParameters.LSComponents.length; i++) {
        str += "<TD align=\"center\">" + jpkParameters.LSComponents[i] + "</TD>";
      }
      str += "</TR></TABLE>";
      str += "</TD></TR>";
    }

    // Substracted values
    if (jpkParameters.LSSubsValues != null) {
      str += "<TR class=\"evenrowcolor\"><TD>Substracted values</TD> <TD>";
      str += "<TABLE border=\"0\"><TR>";
      str += "<TD align=\"right\">" + "Component" + "</TD>";
      for (int i = 0; i < jpkParameters.LSSubsValues.length; i++) {
        str += "<TD align=\"center\">" + i + "</TD>";
      }
      str += "</TR><TR>";
      str += "<TD align=\"right\">" + "Value" + "</TD>";
      for (int i = 0; i < jpkParameters.LSSubsValues.length; i++) {
        str += "<TD align=\"center\">" + jpkParameters.LSSubsValues[i] + "</TD>";
      }
      str += "</TR></TABLE>";
      str += "</TD></TR>";
    }

    // Range modification
    if (jpkParameters.RMMultValues != null) {
      str += "<TR class=\"oddrowcolor\"> <TD>Range modification</TD> <TD>";
      str += "<TABLE border=\"0\"><TR>";
      str += "<TD align=\"right\">" + "Component" + "</TD>";
      for (int i = 0; i < jpkParameters.RMMultValues.length; i++) {
        str += "<TD align=\"center\">" + i + "</TD>";
      }
      str += "</TR><TR>";
      str += "<TD align=\"right\">" + "Multiplied by" + "</TD>";
      for (int i = 0; i < jpkParameters.RMMultValues.length; i++) {
        str += "<TD align=\"center\">" + jpkParameters.RMMultValues[i] + "</TD>";
      }
      str += "</TR></TABLE>";
      str += "</TD></TR>";
    }

    str += "</TABLE>";
    return str;
  }
  
  
  private void decodeMetaData() {
    System.out.println("=== DECODING META DATA ===");
    int numMetaDataBins = clientCache.getNumMetaDataBins();
    System.out.println("Num. meta datas: "+numMetaDataBins);
    
    for (int i = 0; i < numMetaDataBins; i++) {
      System.out.println("=> Decoding meta data "+i);
      MetaDataBin metaData = clientCache.getMetaDataBin(i);
      BufferedDataInputStream in = new BufferedDataInputStream(metaData.getDataArray());
      
      int len = (int)in.length();
      System.out.println("Len="+len);
      try {
        int LBox = in.readInt();
        System.out.println("LBox="+LBox);
        
        int TBox = in.readInt();
        System.out.println("TBox="+TBox+" => "+Integer.toHexString(TBox)+" => "+JP2BoxTypes.convertIntToString(TBox));
        
        int Flags = in.readInt();
        System.out.println("Flags="+Flags+" => "+Integer.toHexString(Flags)+" => " +Integer.toBinaryString(Flags));
        
        long OrigID = in.readLong();
        System.out.println("OrigID="+OrigID);
        
        //long OrigBH = in.readLong();
        //System.out.println("OrigBH="+OrigBH);
        
      } catch (EOFException ex) {
        Logger.getLogger(JP2KClientLogicalTarget.class.getName()).log(Level.SEVERE, null, ex);
      } catch (IOException ex) {
        Logger.getLogger(JP2KClientLogicalTarget.class.getName()).log(Level.SEVERE, null, ex);
      }
      
    }
  }
}
