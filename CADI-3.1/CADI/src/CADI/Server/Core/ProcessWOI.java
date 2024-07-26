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
package CADI.Server.Core;

import CADI.Common.LogicalTarget.JPEG2000.JPCParameters;
import CADI.Common.LogicalTarget.JPEG2000.PredictiveScalingFactors;
import CADI.Common.Network.JPIP.*;
import GiciException.WarningException;
import java.io.IOException;
import java.util.ArrayList;

import CADI.Server.ServerDefaultValues;
import CADI.Server.Cache.ServerCacheModel;
import CADI.Server.LogicalTarget.JPEG2000.Codestream.JPCMainHeaderEncoder;
import CADI.Server.LogicalTarget.JPEG2000.DeliveringModes.CPIDelivery;
import CADI.Server.LogicalTarget.JPEG2000.DeliveringModes.CoRDDelivery;
import CADI.Server.LogicalTarget.JPEG2000.DeliveringModes.FileOrderDelivery;
import CADI.Server.LogicalTarget.JPEG2000.DeliveringModes.ServerWindowScalingFactor;
import CADI.Server.LogicalTarget.JPEG2000.JP2KServerLogicalTarget;
import GiciException.ErrorException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.2 2011/12/13
 */
public class ProcessWOI {

  private JP2KServerLogicalTarget logicalTarget = null;

  private ServerCacheModel serverCache = null;

  private ViewWindowField requestedViewWindow = null;

  private DataLimitField dataLimitField = null;

  /**
   * Further information, see {@link CADI.Server.ServerParser#serverArguments}.
   */
  private int deliveringMode = ServerDefaultValues.DELIVERING_MODE;

  /**
   * Indicates a subtype of the {@link  #deliveringMode} attribute. Thus, its
   * value will depend on the value taken by {@link  #deliveringMode}.
   * <p>
   * Further information, see {@link CADI.Server.ServerParser#serverArguments}.
   *
   * {
   *
   * @see #rateDistortionMethod} value.
   */
  private int deliveringSubtype = -1;

  /**
   * Contains the view window for the image served, if it has been changed by the server.
   */
  private ViewWindowField responseViewWindow;

  /**
   * Definition in {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}.
   */
  private int quality = -1;

  /**
   * Reason code. Its allowed values are defined in
   * {@link CADI.Common.Network.JPIP.EORCodes}
   */
  private int EORReasonCode;

  /**
   * It is and reason message associated with the
   * <code>EORReasonCode</code>.
   * <p>
   * It is an optional attribute.
   */
  private String EORReasonMessage = null;

  /**
   *
   */
  private ArrayList<ResponseData> responseDataList = null;

  /**
   * Definition in {@link CADI.Common.Network.JPIP.ServerControlField#align}.
   */
  private boolean align = false;

  // INTERNAL ATTRIBUTES
  /**
   * This object is used when the image data are sent following the order of
   * the codestream in the file.
   */
  private FileOrderDelivery fileOrderDelivery = null;

  /**
   * This object is used when the image data are sent using the Coding Passes
   * Interleaving (CPI) algorithm.
   */
  private CPIDelivery cpiDelivery = null;

  /**
   * This object is used when the image data are sent using the
   * classic Characterization of Rate Distortion (CoRD) algorithm.
   */
  private CoRDDelivery cordDelivery = null;

  /**
   * This object is used when the image data are sent using the
   * Characterization of Rate Distortion (CoRD) algorithm.
   *
   *
   * Revision 150 in sourceforge
   *
   */
  //private CoRDBasedDelivery cordBasedDelivery = null;
  private ServerWindowScalingFactor WSFDelivery = null;

  /**
   * It is a temporal attribute to accumulate the response length which is
   * sending to the client.
   */
  //private long responseLength = 0;
  /**
   * Definition in {@link CADI.Common.Network.JPIP.DataLimitField#len}
   */
  private long maximumResponseLength = -1;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param logicalTarget
   * @param serverCache
   * @param viewWindowField
   * @param dataLimitField
   * @param serverControlField
   */
  public ProcessWOI(JP2KServerLogicalTarget logicalTarget,
                    ServerCacheModel serverCache,
                    ViewWindowField viewWindowField,
                    DataLimitField dataLimitField) {
    this(logicalTarget, serverCache, viewWindowField, dataLimitField, false);
  }

  /**
   * Constructor.
   *
   * @param logicalTarget
   * @param serverCache
   * @param viewWindowField
   * @param dataLimitField
   * @param align
   */
  public ProcessWOI(JP2KServerLogicalTarget logicalTarget,
                    ServerCacheModel serverCache,
                    ViewWindowField viewWindowField,
                    DataLimitField dataLimitField,
                    boolean align) {

    // Check input parameters
    if (logicalTarget == null) {
      throw new NullPointerException();
    }
    if (serverCache == null) {
      throw new NullPointerException();
    }
    if (viewWindowField == null) {
      throw new NullPointerException();
    }
    if (dataLimitField == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.logicalTarget = logicalTarget;
    this.serverCache = serverCache;
    this.requestedViewWindow = viewWindowField;
    this.dataLimitField = dataLimitField;
    this.align = align;
  }

  /**
   * Sets the mode used to deliver precinct data to the client.
   *
   * @param deliveringMode definition in {@link #deliveringMode}.
   * @param deliveringSubtype definition in {@link #deliveringSubtype}.
   */
  public void setDeliveringMode(int deliveringMode, int deliveringSubtype) {
    this.deliveringMode = deliveringMode;
    this.deliveringSubtype = deliveringSubtype;
  }

  /**
   * @throws ErrorException
   * @throws IllegalArgumentException
   *
   */
  public void run() throws IllegalArgumentException, ErrorException {

    // Initializations
    maximumResponseLength = dataLimitField.len < 0 ? Long.MAX_VALUE : dataLimitField.len;
    responseViewWindow = new ViewWindowField();
    responseDataList = new ArrayList<ResponseData>();

    processRequestedWOI();

    try {
      processWOIData();
    } catch (ErrorException e) {
      e.printStackTrace();
    }
  }

  /**
   *
   */
  public ViewWindowField getResponseViewWindow() {
    return responseViewWindow;
  }

  /**
   * Returns the {@link #quality}.
   *
   * @return the {@link #quality}.
   */
  public final int getQuality() {
    return quality;
  }

  /**
   * Gets the reason code value.
   *
   * @return The reason code value
   *
   * @see #EORReasonCode
   */
  public final int getEORReasonCode() {
    return EORReasonCode;
  }

  /**
   * Gets the reason message.
   *
   * @return The reason message.
   *
   * @see #EORReasonMessage
   */
  public final String getEORReasonMessage() {
    return EORReasonMessage;
  }

  /**
   * Gets the {@link #jpipMessageHeaders} attribute.
   *
   * @return the {@link #jpipMessageHeaders}.
   */
  public final ArrayList<ResponseData> getResponseData() {
    return responseDataList;
  }

  // ============================ private methods ==============================
  /**
   * @throws ErrorException
   * @throws IllegalArgumentException
   *
   */
  private void processRequestedWOI() throws IllegalArgumentException, ErrorException {

    switch (deliveringMode) {

      case ServerDefaultValues.DELIVERING_WINDOW_SCALING_FACTOR:
        WSFDelivery = new ServerWindowScalingFactor(logicalTarget, serverCache, align);
        if (logicalTarget.getCodestream(0).getPredictiveModel() != null) {
          WSFDelivery.setAditionalScalingFactors(
                  new PredictiveScalingFactors(logicalTarget.getCodestream(0).getPredictiveModel()));
        }
        WSFDelivery.runResponseParameters(requestedViewWindow);
        responseViewWindow = WSFDelivery.getResponseViewWindow();
        quality = WSFDelivery.getQuality();
        break;


      case ServerDefaultValues.DELIVERING_FILE_ORDER:

        fileOrderDelivery = new FileOrderDelivery(logicalTarget, serverCache);
        if (this.deliveringSubtype >= 0) {
          fileOrderDelivery.setDeliveryProgressionOrder(deliveringSubtype);
        }

        fileOrderDelivery.runResponseParameters(requestedViewWindow);
        responseViewWindow = fileOrderDelivery.getResponseViewWindow();
        quality = fileOrderDelivery.getQuality();
        break;

      case ServerDefaultValues.DELIVERING_CPI:
        cpiDelivery = new CPIDelivery(logicalTarget, serverCache);
        cpiDelivery.setCPIType(deliveringSubtype);

        cpiDelivery.runResponseParameters(requestedViewWindow);
        responseViewWindow = cpiDelivery.getResponseViewWindow();
        quality = cpiDelivery.getQuality();

        break;

      case ServerDefaultValues.DELIVERING_CoRD:
        if (deliveringSubtype == 1) {
          cordDelivery = new CoRDDelivery(logicalTarget, serverCache);

          cordDelivery.runResponseParameters(requestedViewWindow);
          responseViewWindow = cordDelivery.getResponseViewWindow();
          quality = cordDelivery.getQuality();
        } else {
          assert (true);
          /* cordBasedDelivery = new CoRDBasedDelivery(logicalTarget, serverCache, maximumResponseLength);
           *
           * cordBasedDelivery.runResponseParameters(requestedViewWindow);
           * responseViewWindow = cordBasedDelivery.getResponseViewWindow();
           * quality	= cordBasedDelivery.getQuality();
           */
        }
        break;
    }

  }

  /**
   *
   * @throws ErrorException
   */
  private void processWOIData() throws ErrorException {
    // Set end of response code (default value)
    EORReasonCode = EORCodes.WINDOW_DONE;


    // Delivery data following the rate distortion method
    switch (deliveringMode) {

      case ServerDefaultValues.DELIVERING_WINDOW_SCALING_FACTOR:
        deliveryWindowScalingFactor();
        break;

      case ServerDefaultValues.DELIVERING_FILE_ORDER:
        deliveryDataFileOrder();
        break;

      case ServerDefaultValues.DELIVERING_CPI:
        try {
          deliveryDataCPI();
        } catch (IllegalAccessException e) {
          e.printStackTrace();
          throw new ErrorException();
        }
        break;

      case ServerDefaultValues.DELIVERING_CoRD:
        deliveryDataCoRD();
        break;
    }

  }

  /**
   * Delivery the image WOI following the order of the codestream in the
   * file.
   *
   * @throws ErrorException if an error in the WOI processing has ocurred.
   */
  private void deliveryDataFileOrder() throws ErrorException {

    long responseLength = 0;

    // MAIN HEADER
    if (serverCache.getDataBinLength(ClassIdentifiers.MAIN_HEADER, 0) < logicalTarget.getMainHeaderLength()) {

      // Send metadata bin 0
      addMetadaBin0();

      // Send main header
      //log.logDebug("MAIN HEADER ADDED TO SEND");

      // File pointer and length
      long length = logicalTarget.getMainHeaderLength();
      /* if ((maximumResponseLength > 0) && (maximumResponseLength < length)) {
       * length = maximumResponseLength;
       * } */

      // Send jpip message header
      JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.MAIN_HEADER, 0, 0, length, true, -1);
      ArrayList<Long> filePointers = new ArrayList<Long>();
      filePointers.add(logicalTarget.getMainHeaderInitialPos());
      ArrayList<Integer> lengths = new ArrayList<Integer>();
      lengths.add((int) length);
      responseDataList.add(new ResponseData(jpipMessageHeader, filePointers, lengths));
      responseLength += length;

    } else {
      //log.logDebug("MAIN HEADER IS NOT NECESSARY TO SEND");
    }

    // WINDOW OF INTEREST
    if ((requestedViewWindow.fsiz[0] >= 0) && (requestedViewWindow.fsiz[1] >= 0)) {

      // Send the tile header data-bin message
      if (serverCache.getDataBinLength(JPIPMessageHeader.TILE_HEADER, 0) < logicalTarget.getTileHeaderLength(0)) {
        addTileHeader();
      }

      // Send data
      if (maximumResponseLength > responseLength) {
        fileOrderDelivery.runResponseData(responseDataList, maximumResponseLength - responseLength);
        responseDataList = fileOrderDelivery.getResponseData();
        EORReasonCode = fileOrderDelivery.getEORReasonCode();
      }
    }

    // Send the JPIP End Of Response
    sendEndOfResponse();
  }

  /**
   * Delivery the image WOI following the Coding Passes Interleaving (CPI)
   * strategy.
   *
   * @param httpResponseSender is an object which will be used to send the
   * the messages to the client.
   * @param jpipMessageEncoder is an object used to build the JPIP messages.
   *
   * @throws ErrorException if an error in the WOI processing has ocurred.
   * @throws IllegalAccessException
   */
  private void deliveryDataCPI() throws ErrorException, IllegalAccessException {
    long responseLength = 0;

    // MAIN HEADER
    byte[] mainHeader = logicalTarget.getMainHeader();
    if (mainHeader == null) {
      logicalTarget.setMainHeader(cpiDelivery.getMainHeader());
    }

    if (serverCache.getDataBinLength(ClassIdentifiers.MAIN_HEADER, 0) < logicalTarget.getMainHeaderLength()) {

      // Send metadata bin 0
      addMetadaBin0();

      int bytesInClientCache = serverCache.getMainHeaderLength();

      // File pointer and length
      mainHeader = logicalTarget.getMainHeader();
      int pendingBytes = mainHeader.length - bytesInClientCache;

      if ((maximumResponseLength > 0) && (responseLength + pendingBytes > maximumResponseLength)) {
        pendingBytes = (int) (maximumResponseLength - responseLength);
      }

      // Send jpip message header
      JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.MAIN_HEADER, 0, bytesInClientCache, pendingBytes, true, -1);

      byte[] chunk = new byte[pendingBytes];
      System.arraycopy(mainHeader, bytesInClientCache, chunk, 0, pendingBytes);
      ArrayList<byte[]> chunks = new ArrayList<byte[]>();
      chunks.add(chunk);
      responseDataList.add(new ResponseData(jpipMessageHeader, chunks));
      responseLength += pendingBytes;
    } else {
      //log.logDebug("MAIN HEADER IS NOT NECESSARY TO SEND");
    }

    //System.out.println(requestViewWindow.toString());
    // WINDOW OF INTEREST
    if ((requestedViewWindow.fsiz[0] >= 0) && (requestedViewWindow.fsiz[1] >= 0)) {

      // Send the tile header data-bin message
      if (serverCache.getDataBinLength(JPIPMessageHeader.TILE_HEADER, 0) < logicalTarget.getTileHeaderLength(0)) {
        addTileHeader();
      }

      cpiDelivery.runResponseData(responseDataList, maximumResponseLength - responseLength);
      responseDataList = cpiDelivery.getResponseData();
      EORReasonCode = cpiDelivery.getEORReasonCode();

    }

    // Send the JPIP End Of Response
    sendEndOfResponse();
  }

  /**
   * Delivery the image WOI following the Characterization of Rate Distortion
   * (CoRD) algorithm
   *
   * @param httpResponseSender is an object which will be used to send the
   * the messages to the client.
   * @param jpipMessageEncoder is an object used to build the JPIP messages.
   *
   * @throws IOException if an I/O error has ocurred.
   * @throws ErrorException if an error in the WOI processing has ocurred.
   */
  private void deliveryDataCoRD() throws ErrorException {
    long responseLength = 0;

    // MAIN HEADER
    byte[] mainHeader = logicalTarget.getMainHeader();
    if (mainHeader == null) {
      logicalTarget.setMainHeader(cordDelivery.getMainHeader());
    }

    // MAIN HEADER
    if (serverCache.getDataBinLength(ClassIdentifiers.MAIN_HEADER, 0) < logicalTarget.getMainHeaderLength()) {

      // Send metadata bin 0
      addMetadaBin0();

      int bytesInClientCache = serverCache.getMainHeaderLength();

      // File pointer and length
      mainHeader = logicalTarget.getMainHeader();
      int pendingBytes = mainHeader.length - bytesInClientCache;

      if ((maximumResponseLength > 0) && (responseLength + pendingBytes > maximumResponseLength)) {
        pendingBytes = (int) (maximumResponseLength - responseLength);
      }


      // Send jpip message header
      JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.MAIN_HEADER, 0, bytesInClientCache, pendingBytes, true, -1);

      byte[] chunk = new byte[pendingBytes];
      System.arraycopy(mainHeader, bytesInClientCache, chunk, 0, pendingBytes);
      ArrayList<byte[]> chunks = new ArrayList<byte[]>();
      chunks.add(chunk);
      responseDataList.add(new ResponseData(jpipMessageHeader, chunks));
      responseLength += pendingBytes;
    } else {
      //log.logDebug("MAIN HEADER IS NOT NECESSARY TO SEND");
    }

    // WINDOW OF INTEREST
    if ((requestedViewWindow.fsiz[0] >= 0) && (requestedViewWindow.fsiz[1] >= 0)) {

      // Send the tile header data-bin message
      if (serverCache.getDataBinLength(JPIPMessageHeader.TILE_HEADER, 0) < logicalTarget.getTileHeaderLength(0)) {
        addTileHeader();
      }

      if (deliveringSubtype == 1) {
        cordDelivery.runResponseData(responseDataList, maximumResponseLength - responseLength);
        responseDataList = cordDelivery.getResponseData();
        EORReasonCode = cordDelivery.getEORReasonCode();
      } else {
        assert (true);
        //cordBasedDelivery.runResponseData(responseDataList);
        //responseDataList = cordBasedDelivery.getResponseData();
        //EORReasonCode = cordBasedDelivery.getEORReasonCode();
      }
    }

    // Send the JPIP End Of Response
    sendEndOfResponse();

  }

  /**
   * Delivery the requested image's WOI applying a Window Scaling Factor.
   *
   * @throws ErrorException if an error in the WOI processing has occurred.
   */
  private void deliveryWindowScalingFactor() throws ErrorException {

    long responseLength = 0;

    // MAIN HEADER
    if (serverCache.getDataBinLength(ClassIdentifiers.MAIN_HEADER, 0) < logicalTarget.getMainHeaderLength()) {

      // Send metadata bin 0
      addMetadaBin0();

      // Send main header

      int bytesInClientCache = serverCache.getMainHeaderLength();

      if (logicalTarget.getCodestream(0).getJPCParameters().comParameters.predictiveModel == null) {

        // File pointer and length
        int pendingBytes = logicalTarget.getMainHeaderLength() - bytesInClientCache;

        if (pendingBytes > 0) {
          if ((maximumResponseLength > 0) && (responseLength + pendingBytes > maximumResponseLength)) {
            pendingBytes = (int) (maximumResponseLength - responseLength);
          }

          // Send jpip message header
          JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.MAIN_HEADER, 0, bytesInClientCache, pendingBytes, true, -1);
          ArrayList<Long> filePointers = new ArrayList<Long>();
          filePointers.add(logicalTarget.getMainHeaderInitialPos() + bytesInClientCache);
          ArrayList<Integer> lengths = new ArrayList<Integer>();
          lengths.add(pendingBytes);
          responseDataList.add(new ResponseData(jpipMessageHeader, filePointers, lengths));
          responseLength += pendingBytes;
        }
      } else {
        byte[] mainHeader = logicalTarget.getMainHeader();
        if (mainHeader == null) {
          JPCParameters params = new JPCParameters(logicalTarget.getCodestream(0).getJPCParameters());
          params.comParameters.predictiveModel = null;
          JPCMainHeaderEncoder jpcMainHeaderEncoder = new JPCMainHeaderEncoder(params);
          //JPCMainHeaderEncoder jpcMainHeaderEncoder = new JPCMainHeaderEncoder(logicalTarget.getCodestream(0).getJPCParameters());
          try {
            jpcMainHeaderEncoder.run();
          } catch (WarningException ex) {
            assert (true);
          } catch (IOException ex) {
            assert (true);
          }
          mainHeader = jpcMainHeaderEncoder.getMainHeader();
          logicalTarget.setMainHeader(mainHeader);
        }
        int pendingBytes = mainHeader.length - bytesInClientCache;
        if (pendingBytes > 0) {
          if ((maximumResponseLength > 0) && (responseLength + pendingBytes > maximumResponseLength)) {
            pendingBytes = (int) (maximumResponseLength - responseLength);
          }

          // Send jpip message header
          JPIPMessageHeader jpipMessageHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.MAIN_HEADER, 0, bytesInClientCache, pendingBytes, true, -1);
          byte[] tmpHeader = Arrays.copyOfRange(mainHeader, bytesInClientCache, bytesInClientCache + pendingBytes);
          ArrayList<byte[]> headerChunk = new ArrayList<byte[]>();
          headerChunk.add(tmpHeader);
          responseDataList.add(new ResponseData(jpipMessageHeader, headerChunk));
          responseLength += pendingBytes;
        }
      }
    } else {
      //log.logDebug("MAIN HEADER IS NOT NECESSARY TO SEND");
    }

    // WINDOW OF INTEREST
    if ((requestedViewWindow.fsiz[0] >= 0) && (requestedViewWindow.fsiz[1] >= 0)) {

      // Send the tile header data-bin message
      if (serverCache.getDataBinLength(JPIPMessageHeader.TILE_HEADER, 0) < logicalTarget.getTileHeaderLength(0)) {
        addTileHeader();
      }

      // Send data
      if (maximumResponseLength > responseLength) {
        WSFDelivery.runResponseData(responseDataList, maximumResponseLength - responseLength);
        responseDataList = WSFDelivery.getResponseData();
        EORReasonCode = WSFDelivery.getEORReasonCode();
      }
    }

    // Send the JPIP End Of Response
    sendEndOfResponse();
  }

  /**
   * Adds the metadata bin 0 to the response.
   * <p>
   * It is a temporal method while metadata are not supported.
   *
   * @throws IOException
   */
  private void addMetadaBin0() {
    JPIPMessageHeader jpipMetadataHeader = new JPIPMessageHeader(-1, JPIPMessageHeader.METADATA, 0, 0, 0, true, -1);
    responseDataList.add(new ResponseData(jpipMetadataHeader));
  }

  /**
   * Adds the tile-header to the response.
   * <p>
   * It is a temporal method while tiles are not supported.
   */
  private void addTileHeader() {
    JPIPMessageHeader jpipMessageHeader =
            new JPIPMessageHeader(-1, JPIPMessageHeader.TILE_HEADER, 0, 0, logicalTarget.getTileHeaderLength(0), true, -1);
    ArrayList<Long> filePointers = new ArrayList<Long>();
    ArrayList<Integer> lengths = new ArrayList<Integer>();
    filePointers.add(logicalTarget.getTileHeaderFilePointer(0));
    lengths.add(logicalTarget.getTileHeaderLength(0));
    responseDataList.add(new ResponseData(jpipMessageHeader, filePointers, lengths));

  }

  /**
   * This method is used to send the End of Response of a JPIP message.
   *
   * @param httpResponseSender
   * @param jpipMessageEncoder
   *
   * @throws IOException
   */
  private void sendEndOfResponse() {
    int EORReasonCode = getEORReasonCode();
    String EORReasonMessage = getEORReasonMessage();
    responseDataList.add(new ResponseData(new JPIPMessageHeader(EORReasonCode, EORReasonMessage)));
  }
}
