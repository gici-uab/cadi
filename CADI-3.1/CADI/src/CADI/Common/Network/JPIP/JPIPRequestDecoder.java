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
package CADI.Common.Network.JPIP;

import CADI.Common.Cache.BinDescriptor;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;

import CADI.Common.Network.HTTP.StatusCodes;
import CADI.Common.Network.JPIP.ClientPreferences;
import CADI.Common.Network.JPIP.JPIPRequestFields;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Common.Cache.ModelElement;
import CADI.Common.Defaults.ImageReturnTypes;
import GiciException.ParameterException;
import GiciException.WarningException;

/**
 * This class implements a JPIP request parameters decoder.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2010/01/02
 */
public class JPIPRequestDecoder implements StatusCodes, ImageReturnTypes {

  /**
   * Is the lastest decoder URI.
   */
  private URI uri = null;

  /**
   * This object contains the JPIP request fields of the lastest decoded URI.
   */
  private JPIPRequestFields jpipRequestFields = null;

  // INTERNAL ATTTRIBUTES
  /**
   * Contains all possible parameter names which can appear in the client request.
   * Each parameter definition at
   */
  private static final String[] parametersSpecification = {
    // target-field
    "target", "subtarget", "tid",
    // channel-field
    "cid", "cnew", "cclose", "quid",
    // view-window-field
    "fsiz", "roff", "rsiz", "comps", "stream", "context", "srate", "roi", "layers",
    //	metadata-field
    "metareq",
    // data-limit-field
    "len", "quality",
    // server-control-field
    "align", "wait", "type", "drate",
    // cache-management-field
    "model", "tpmodel", "need", "tpneed", "mset",
    // upload-field
    "upload",
    // client-cap-pref-field
    "cap", "pref", "csf"
  };

  boolean[] parametersFound = new boolean[parametersSpecification.length];

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public JPIPRequestDecoder() {
    jpipRequestFields = new JPIPRequestFields();
  }

  /**
   * Decodes an URI.
   *
   * @param requestURI the URI to be decoded.
   * @throws WarningException when an error is found
   */
  public void decoder(String requestURI) throws WarningException {

    jpipRequestFields = new JPIPRequestFields();

    try {
      uri = new URI(requestURI);
    } catch (URISyntaxException e1) {
      throw new WarningException("Request must be: Method SP Request-URI SP HTTP-Version", BAD_REQUEST);
    }

    // Reset variables
    reset();
    // URI must contain the path
    String path = uri.getPath();
    if ((path == null) || (path.length() <= 0)) {
      throw new WarningException("Request must be: Method SP Request-URI SP HTTP-Version", BAD_REQUEST);
    }

    // Parses the Request-URI parameters
    String[] tokens = null;
    try {
      tokens = uri.getQuery().split("&");
    } catch (Exception e) {
      throw new WarningException("Request must be: Method SP Request-URI SP HTTP-Version", BAD_REQUEST);
    }

    // Parses the tokens
    for (String token : tokens) {
      int pos = token.indexOf('=');
      ParameterParser(token.substring(0, pos), token.substring(pos + 1));
    }

    // Find which type of URL type

    //Get the target
		/* Posibles URL:
     * 1.- http://server/imageserver.cgi?target=http://....
     * 2.- http://server/imageserver.cgi?target=http://....
     * 3.- http://server/imageserver.cgi?target=http://...
     * 4.- http://server/imageserver.cgi?cid=1234-5678-af4d-3dca&fsize=....
     * 5.- http://server/picture.jp2?fsize=.....
     * 6.- http://server/picture.jp2?subtarget=1038-12456&....
     * 7.- http://server/imageserver.cgi?tid=1234-5678-af4d-3dca&fsize=....
     */

    //<<<<<<<<<<<<<<<<<<<<<<<De momento considero que es de tipo 5 <<<<<<<<<<<<<<<<<<<<<<<
    // Se debe mirar si se ha puesto target, cid, tid y en funcion de eso se toma la decision

    if (jpipRequestFields.channelField.cid != null) {
      jpipRequestFields.channelField.path = path.substring(1, path.length()); // remove de / character
    } else {
      if (jpipRequestFields.targetField.target == null) {
        try {
          // remove the / character (relative path)
          // decode special characters
          jpipRequestFields.targetField.target = URLDecoder.decode(path.substring(1, path.length()), "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
      }
    }
  }

  /**
   * Sets the attributes to its initial values.
   */
  public void reset() {
    jpipRequestFields.reset();
    for (int i = 0; i < parametersSpecification.length; i++) {
      parametersFound[i] = false;
    }
  }

  /**
   * Gets the View Window of the latest decoded URI.
   * @return the view window.
   */
  public ViewWindowField getViewWindow() {
    return jpipRequestFields.viewWindowField;
  }

  /**
   * Gets the JPIP request fields of the latest decoded URI.
   *
   * @return the JPIP request fields.
   */
  public JPIPRequestFields getJPIPRequestFields() {
    return jpipRequestFields;
  }

  // ============================ private methods ==============================
  /**
   * Parses a JPIP request parameter.
   *
   * @param key parameter name
   * @param value parameter value
   *
   * @throws WarningException when the request element is wrong
   */
  private void ParameterParser(String key, String value) throws WarningException {

    int index = 0;
    boolean paramFound = false;

    while ((index < parametersSpecification.length) && !paramFound) {
      if (key.compareToIgnoreCase(parametersSpecification[index]) == 0) {
        paramFound = true;
        if (parametersFound[index]) {
          throw new WarningException("Request has repeated parameters", BAD_REQUEST);
        } else {
          parametersFound[index] = true;
          // Call the parser method for each parameter
          try {
            switch (index) {
              // Target fields
              case 0:
                targetParser(value);
                break;
              case 1:
                subTargetParser(value);
                break;
              case 2:
                targetIDParser(value);
                break;

              // Channel fields
              case 3:
                channelIDParser(value);
                break;
              case 4:
                channelNewParser(value);
                break;
              case 5:
                channelCloseParser(value);
                break;
              /*
               * case 6:
               * requestIDParser(value);
               * break;
               */

              // View Windowd Fields
              case 7:
                fsizParser(value);
                break;
              case 8:
                roffParser(value);
                break;
              case 9:
                rsizParser(value);
                break;
              case 10:
                compsParser(value);
                break;
              case 11:
                streamParser(value);
                break;
              /*
               * case 12:
               * contextParser(value);
               * break;
               * case 13:
               * srateParser(value);
               * break;
               * case 14:
               * roiParser(value);
               * break;
               */
              case 15:
                layersParser(value);
                break;
              // Metadata Fields
								/* case 16:
               * metareqParser(value);
               * break;
               */

              // Data Limit Fields
              case 17:
                lenParser(value);
                break;
              case 18:
                qualityParser(value);
                break;

              // Server Control Fields
              case 19:
                alignParser(value);
                break;
              case 20:
                waitParser(value);
                break;

              case 21:
                typeParser(value);
                break;
              case 22:
                drateParser(value);
                break;

              // Chache Management Fields
              case 23:
                modelParser(value);
                break;
              /*
               * case 24:
               * tpmodelParser(value);
               * break;
               */
              case 25:
                needParser(value);
                break;
              /*
               * case 26:
               * tpneedParser(value);
               * break;
               *
               * case 27:
               * msetParser(value);
               * break;
               */

              // Upload Fields
								/*
               * case 28:
               * uploadParser(value);
               * break;
               */

              // Client Capabilities Preference Fields
								/*
               * case 29:
               * capParser(value);
               * break;
               */
              case 30:
                prefParser(value);
                break;
              /* case 31:
               * csfParser(value);
               * break;
               */
              default:
                throw new WarningException("The request parameter (" + value + ") is not supported", NOT_IMPLEMENTED);
            } // switch (index)

          } catch (ParameterException e) {
            throw new WarningException("Wrong request parameter: " + key + "=" + value, BAD_REQUEST);
          }
        }
      } else {
        index++;
      }
    } // while

    if (!paramFound) {
      throw new WarningException("Request has, at least, an unknown parameter", BAD_REQUEST);
    }

  }

  //
  // Target field parsers
  //
  /**
   *
   *
   * @param target definition in {@link CADI.Common.Network.JPIP.TargetField#target}
   */
  private void targetParser(String target) {
    this.jpipRequestFields.targetField.target = target;
  }

  /**
   * @param subTarget definition in {@link CADI.Common.Network.JPIP.TargetField#subtarget}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void subTargetParser(String subTarget) throws ParameterException {
    String[] subTargetValues = null;

    subTargetValues = subTarget.split("-");
    if ((subTargetValues.length > 2) || (subTargetValues.length == 0)) {
      throw new ParameterException();
    }

    // read subtarget lower bound values
    try {
      this.jpipRequestFields.targetField.subtarget[0] = Integer.parseInt(subTargetValues[0]);
    } catch (Exception e) {
      throw new ParameterException();
    }

    if (this.jpipRequestFields.targetField.subtarget[0] < 0) {
      throw new ParameterException();
    }

    // read subtarget lower upper values
    if (subTargetValues.length == 2) {
      try {
        this.jpipRequestFields.targetField.subtarget[1] = Integer.parseInt(subTargetValues[1]);
      } catch (Exception e) {
        throw new ParameterException();
      }

      if (this.jpipRequestFields.targetField.subtarget[1] < 0) {
        throw new ParameterException();
      }
    }
  }

  /**
   *
   * @param tid definition in {@link CADI.Common.Network.JPIP.TargetField#tid}
   *
   * @throws Exception when the request element is wrong
   */
  private void targetIDParser(String tid) throws ParameterException {

    if (tid.length() > 255) {
      throw new ParameterException();
    }
    this.jpipRequestFields.targetField.tid = tid;
  }

  //
  // Channel field parsers
  //
  /**
   * @param cid definition in {@link CADI.Common.Network.JPIP.ChannelField#cid}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void channelIDParser(String cid) throws ParameterException {
    this.jpipRequestFields.channelField.cid = cid;
  }

  /**
   *
   * @param cnew definition in {@link CADI.Common.Network.JPIP.ChannelField#cnew}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void channelNewParser(String cnew) throws ParameterException {

    String[] tempString = cnew.split(",");
    if ((tempString.length == 0) || (tempString.length > 2)) {
      throw new ParameterException();

    }

    this.jpipRequestFields.channelField.cnew = new ArrayList<String>();
    for (int i = 0; i < tempString.length; i++) {
      if ((tempString[i].compareTo("http") == 0) || (tempString[i].compareTo("http-tcp") == 0)) {
        this.jpipRequestFields.channelField.cnew.add(tempString[i]);
      } else {
        throw new ParameterException();
      }
    }
  }

  /**
   * @param cclose definition in {@link CADI.Common.Network.JPIP.ChannelField#cclose}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void channelCloseParser(String cclose) throws ParameterException {
    if ((cclose == null) || (cclose.length() == 0)) {
      throw new ParameterException();
    }

    jpipRequestFields.channelField.cclose = new ArrayList<String>();
    if (cclose.equals("*")) {
      jpipRequestFields.channelField.cclose.add(cclose);
    } else {
      for (String cid : cclose.split(",")) {
        if (cid.equals("*")) throw new ParameterException();
        jpipRequestFields.channelField.cclose.add(cid);
      }
    }
  }

  /**
   * @param qid defined in {@link CADI.Common.Network.JPIP.ChannelField#qid}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void requestIDParser(String qid) throws ParameterException {

    // read comps values
    try {
      jpipRequestFields.channelField.qid = Integer.parseInt(qid);
    } catch (Exception e) {
      throw new ParameterException();
    }
    if (jpipRequestFields.channelField.qid < 0) {
      throw new ParameterException();
    }
  }

  //
  // View window field parsers
  //
  /**
   * @param fsiz defined in {@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void fsizParser(String fsiz) throws ParameterException {

    String[] tempString = fsiz.split(",");

    if ((tempString.length > 3) || (tempString.length == 0)) {
      throw new ParameterException();
    }

    this.jpipRequestFields.viewWindowField.fsiz = new int[3];
    // read fx & fy values
    try {
      this.jpipRequestFields.viewWindowField.fsiz[0] = Integer.parseInt(tempString[0]);
      this.jpipRequestFields.viewWindowField.fsiz[1] = Integer.parseInt(tempString[1]);
    } catch (Exception e) {
      throw new ParameterException();
    }
    if ((this.jpipRequestFields.viewWindowField.fsiz[0] < 0) || (this.jpipRequestFields.viewWindowField.fsiz[1] < 0)) {
      throw new ParameterException();
    }

    // Read round-direction value
    if (tempString.length == 3) {
      if (tempString[2].compareToIgnoreCase("round-up") == 0) {
        this.jpipRequestFields.viewWindowField.roundDirection = ViewWindowField.ROUND_UP;
      } else {
        if (tempString[2].compareToIgnoreCase("round-down") == 0) {
          this.jpipRequestFields.viewWindowField.roundDirection = ViewWindowField.ROUND_DOWN;
        } else {
          if (tempString[2].compareToIgnoreCase("closest") == 0) {
            this.jpipRequestFields.viewWindowField.roundDirection = ViewWindowField.CLOSEST;
          } else {
            throw new ParameterException();
          }
        }
      }
    } else {
      this.jpipRequestFields.viewWindowField.roundDirection = ViewWindowField.ROUND_DOWN;
    }
  }

  /**
   * @param roff defined in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void roffParser(String roff) throws ParameterException {
    String[] tempString = roff.split(",");

    if ((tempString.length > 2) || (tempString.length == 0)) {
      throw new ParameterException();
    }

    this.jpipRequestFields.viewWindowField.roff = new int[tempString.length];
    // read ox and oy values
    try {
      this.jpipRequestFields.viewWindowField.roff[0] = Integer.parseInt(tempString[0]);
      this.jpipRequestFields.viewWindowField.roff[1] = Integer.parseInt(tempString[1]);
    } catch (Exception e) {
      throw new ParameterException();
    }

    if ((this.jpipRequestFields.viewWindowField.roff[0] < 0) || (this.jpipRequestFields.viewWindowField.roff[1] < 0)) {
      throw new ParameterException();
    }
  }

  /**
   * @param rsiz defined in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void rsizParser(String rsiz) throws ParameterException {

    String[] tempString = rsiz.split(",");

    if ((tempString.length > 2) || (tempString.length == 0)) {
      throw new ParameterException();
    }

    this.jpipRequestFields.viewWindowField.rsiz = new int[tempString.length];
    // read sx & sy values
    try {
      this.jpipRequestFields.viewWindowField.rsiz[0] = Integer.parseInt(tempString[0]);
      this.jpipRequestFields.viewWindowField.rsiz[1] = Integer.parseInt(tempString[1]);
    } catch (Exception e) {
      throw new ParameterException();
    }
    if ((this.jpipRequestFields.viewWindowField.rsiz[0] < 0) || (this.jpipRequestFields.viewWindowField.rsiz[1] < 0)) {
      throw new ParameterException();
    }
  }

  /**
   * @param comps definition in {@link CADI.Common.Network.JPIP.ViewWindowField#comps}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void compsParser(String comps) throws ParameterException {
    //int[][] compsValues = null;
    String[] tempString = null, compsString = null;

    try {
      compsString = comps.split(",");
      this.jpipRequestFields.viewWindowField.comps = new int[compsString.length][2];
      for (int i = 0; i < compsString.length; i++) {
        this.jpipRequestFields.viewWindowField.comps[i][0] = -1;
        this.jpipRequestFields.viewWindowField.comps[i][1] = -1;

        tempString = compsString[i].split("-");
        if (tempString.length == 1) {	// case UINT -
          this.jpipRequestFields.viewWindowField.comps[i][0] = Integer.parseInt(tempString[0]);
        } else {
          if (tempString.length == 2) {		// UINT-UINT
            this.jpipRequestFields.viewWindowField.comps[i][0] = Integer.parseInt(tempString[0]);
            this.jpipRequestFields.viewWindowField.comps[i][1] = Integer.parseInt(tempString[1]);
          } else {
            throw new ParameterException();
          }
        }
      }

    } catch (Exception e) {
      throw new ParameterException();
    }
  }

  /**
   * @param stream defined in {@link CADI.Common.Network.JPIP.ViewWindowField#stream}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void streamParser(String stream) throws ParameterException {

    String[] tempString = null;
    String[] temp1 = null, temp2 = null;


    try {
      tempString = stream.split(",");
      this.jpipRequestFields.viewWindowField.stream = new int[tempString.length][3];
      for (int i = 0; i < tempString.length; i++) {
        this.jpipRequestFields.viewWindowField.stream[i][0] = -1;
        this.jpipRequestFields.viewWindowField.stream[i][1] = -1;
        this.jpipRequestFields.viewWindowField.stream[i][2] = -1;

        //Possibilities:
        // 1.- stream=from
        //	2.- stream=from-to
        //	3.- stream=from-to:samplingFactor
        // 4.- stream=from:samplingFactor

        temp1 = tempString[i].split("-");
        if (temp1.length == 2) {	//case 2 or 3
          this.jpipRequestFields.viewWindowField.stream[i][0] = Integer.parseInt(temp1[0]);
          temp2 = temp1[1].split(":");
          if (temp2.length == 1) {	// case 2
            this.jpipRequestFields.viewWindowField.stream[i][1] = Integer.parseInt(temp2[0]);
          } else if (temp2.length == 2) {	// case 3
            this.jpipRequestFields.viewWindowField.stream[i][1] = Integer.parseInt(temp2[0]);
            this.jpipRequestFields.viewWindowField.stream[i][2] = Integer.parseInt(temp2[1]);
          } else {
            throw new ParameterException();
          }
        } else if (temp1.length == 1) {	// case 1 or 4
          temp2 = temp1[0].split(":");
          if (temp2.length == 1) {	// case 1
            this.jpipRequestFields.viewWindowField.stream[i][0] = Integer.parseInt(temp2[0]);
          } else if (temp2.length == 2) {
            this.jpipRequestFields.viewWindowField.stream[i][0] = Integer.parseInt(temp2[0]);
            this.jpipRequestFields.viewWindowField.stream[i][2] = Integer.parseInt(temp2[1]);
          } else {
            throw new ParameterException();
          }
        } else {
          throw new ParameterException();
        }
      }
    } catch (Exception e) {
      throw new ParameterException();
    }
  }

  /**
   *
   * @param context defined in {@link CADI.Common.Network.JPIP.ViewWindowField#context}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void contextParser(String context) throws ParameterException {
  }

  /**
   * @param srate defined in {@link CADI.Common.Network.JPIP.ViewWindowField#srate}
   *
   * @throws ParameterException
   */
  private void srateParser(String srate) throws ParameterException {

    // read comps values
    try {
      this.jpipRequestFields.viewWindowField.srate = Float.parseFloat(srate);
    } catch (Exception e) {
      throw new ParameterException();
    }
    if (this.jpipRequestFields.viewWindowField.srate < 0) {
      throw new ParameterException();
    }
  }

  /**
   * @param roi defined in {@link CADI.Common.Network.JPIP.ViewWindowField#roi}
   *
   * @throws ParameterException
   */
  private void roiParser(String roi) {
  }

  /**
   * @param layers defined in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void layersParser(String layers) throws ParameterException {

    // read layers value
    try {
      this.jpipRequestFields.viewWindowField.layers = Integer.parseInt(layers);
    } catch (Exception e) {
      throw new ParameterException();
    }
    if (this.jpipRequestFields.viewWindowField.layers < 0) {
      throw new ParameterException();
    }
  }

  //
  // Metadata field parsers
  //
  /**
   * @param metareq defined in {@link CADI.Common.Network.JPIP.JPIPRequestFields}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void metareqParser(String metareq) {
  }

  //
  // Data limit field parsers
  //
  /**
   * @param len defined in {@link CADI.Common.Network.JPIP.DataLimitField#len}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void lenParser(String len) throws ParameterException {

    try {
      this.jpipRequestFields.dataLimitField.len = Integer.parseInt(len);
    } catch (Exception e) {
      throw new ParameterException();
    }

    if (this.jpipRequestFields.dataLimitField.len < 0) {
      throw new ParameterException();
    }
  }

  /**
   * @param quality defined in {@link CADI.Common.Network.JPIP.DataLimitField#quality}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void qualityParser(String quality) throws ParameterException {

    try {
      this.jpipRequestFields.dataLimitField.quality = Integer.parseInt(quality);
    } catch (Exception e) {
      throw new ParameterException();
    }

    if ((this.jpipRequestFields.dataLimitField.quality < 0) || (this.jpipRequestFields.dataLimitField.quality > 100)) {
      throw new ParameterException();
    }
  }

  //
  // Server control field parsers
  //
  /**
   * @param align defined in {@link CADI.Common.Network.JPIP.ServerControlField#align}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void alignParser(String align) throws ParameterException {

    if (align.compareTo("yes") == 0) {
      jpipRequestFields.serverControlField.align = true;
    } else if (align.compareTo("no") == 0) {
      this.jpipRequestFields.serverControlField.align = false;
    } else {
      throw new ParameterException();
    }
  }

  /**
   * @param wait defined in {@link CADI.Common.Network.JPIP.ServerControlField#wait}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void waitParser(String wait) throws ParameterException {

    if (wait.compareTo("yes") == 0) {
      this.jpipRequestFields.serverControlField.wait = true;
    } else {
      if (wait.compareTo("no") == 0) {
        this.jpipRequestFields.serverControlField.wait = false;
      } else {
        throw new ParameterException();
      }
    }
  }

  /**
   * @param type defined in {@link CADI.Common.Network.JPIP.ServerControlField#type}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void typeParser(String type) throws ParameterException {
    String[] tempString = type.split(",");

    for (int i = 0; i < tempString.length; i++) {
      String tmpType = null;
      if (tempString[i].contains(";")) {

        int pos = tempString[i].indexOf(';');
        tmpType = tempString[i].substring(0, pos);
        String tmpSubType = tempString[i].substring(pos + 1);

        if (tmpSubType.equals("ptype=ext")) {
          jpipRequestFields.serverControlField.useExtendedHeaders = true;
        } else {
          jpipRequestFields.serverControlField.type.clear();
          throw new ParameterException("The request parameter \"" + tempString[i] + "\" is unknown or unsupported");
        }
      } else {
        tmpType = tempString[i];
      }

      if ((tmpType.compareTo("jpp-stream") == 0)
          || (tmpType.compareTo("jpt-stream") == 0)
          || (tmpType.compareTo("raw") == 0)) {
        jpipRequestFields.serverControlField.type.add(tmpType);

      } else {
        jpipRequestFields.serverControlField.type.clear();
        throw new ParameterException("The request parameter \"" + tempString[i] + "\" is unknown or unsupported");
      }
    }
  }

  /**
   * @param drate defined in {@link CADI.Common.Network.JPIP.ServerControlField#drate}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void drateParser(String drate) throws ParameterException {

    try {
      this.jpipRequestFields.serverControlField.drate = Float.parseFloat(drate);
    } catch (Exception e) {
      throw new ParameterException();
    }
  }

  //
  // Cache management field parsers
  //
  /**
   * @param model definition in {@link CADI.Common.Network.JPIP.CacheManagementField#model}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void modelParser(String model) throws ParameterException {

    String[] element = model.split(",");

    // TODO: codestream-qualifier is not evaluated yet.
    // It is supposed we have only one codestream per file.

    jpipRequestFields.cacheManagementField.model = new ArrayList<ModelElement>();
    ModelElement cacheDescriptorElement = null;

    for (int i = 0; i < element.length; i++) {
      if ((element[i] == null) || (element[i].length() == 0)) {
        throw new ParameterException();
      }
      int aditiveOffset = 0;
      boolean additive = true;
      if (element[i].codePointAt(0) == '-') {
        aditiveOffset = 1;
        additive = false;
      }

      switch (element[i].codePointAt(aditiveOffset)) {

        // EXPLICIT FORM
        case 'H':	// Header
          cacheDescriptorElement = new ModelElement(BinDescriptor.EXPLICIT_FORM);
          cacheDescriptorElement.additive = additive;
          if (element[i].length() - aditiveOffset > 1) {
            // Main or Tile header

            if (element[i].codePointAt(aditiveOffset + 1) == 'm') {
              // Main header
              cacheDescriptorElement.explicitForm.classIdentifier = ModelElement.MAIN_HEADER;
              cacheDescriptorElement.explicitForm.inClassIdentifier = 0;
            } else {
              // Tile header
              cacheDescriptorElement.explicitForm.classIdentifier = ModelElement.TILE_HEADER;

              // Read the InClassIdentifier
              int index = aditiveOffset + 1;
              if ((index < element[i].length()) && (element[i].codePointAt(index) >= '0') && (element[i].codePointAt(index) <= '9')) {
                cacheDescriptorElement.explicitForm.inClassIdentifier = 0;
                while ((index < element[i].length()) && (element[i].codePointAt(index) >= '0') && (element[i].codePointAt(index) <= '9')) {
                  cacheDescriptorElement.explicitForm.inClassIdentifier =
                          cacheDescriptorElement.explicitForm.inClassIdentifier * 10 + element[i].codePointAt(index++) - '0';
                }
              } else {
                if ((element[i].codePointAt(index) != '*')) {
                  cacheDescriptorElement.explicitForm.inClassIdentifier = BinDescriptor.WILDCARD;
                }
              }

              // Read the qualifier if it is present
              if ((index < element[i].length()) && (element[i].codePointAt(index++) == ':')) {
                // Read number of bytes
                if ((element[i].codePointAt(index) >= '0') && (element[i].codePointAt(index) <= '9')) {
                  cacheDescriptorElement.explicitForm.numberOfBytes = 0;
                  while ((index < element[i].length()) && (element[i].codePointAt(index) >= '0') && (element[i].codePointAt(index) <= '9')) {
                    cacheDescriptorElement.explicitForm.numberOfBytes =
                            cacheDescriptorElement.explicitForm.numberOfBytes * 10 + (element[i].codePointAt(index++) - '0');
                  }
                } else {
                  if ((element[i].codePointAt(index) == '*')) {
                    cacheDescriptorElement.explicitForm.numberOfBytes = BinDescriptor.WILDCARD;
                  }
                }
              }
            }
          } else {
            throw new ParameterException();
          }
          break;

        case 'M':
          cacheDescriptorElement = new ModelElement(BinDescriptor.EXPLICIT_FORM);
          cacheDescriptorElement.additive = additive;
          break;
        case 'T':	// Tile
          cacheDescriptorElement = new ModelElement(BinDescriptor.EXPLICIT_FORM);
          cacheDescriptorElement.additive = additive;
          break;
        case 'P':	// Precinct
          cacheDescriptorElement = new ModelElement(BinDescriptor.EXPLICIT_FORM);
          cacheDescriptorElement.additive = additive;
          // Element must have more than 1 caracter
          if (element[i].length() - aditiveOffset == 1) {
            throw new ParameterException();
          }

          // 7 posibilities:
          //		PI    where I is the BinID
          //		PI:Ll where I is the BinID and l is the number of layer
          //		PI:L* where I is the BinID
          //		PI:N  where I is the BinID and N is the number of bytes
          //		P*
          //		P*:Ll
          //		P*:N

          int InClassIdentifier = -1;
          int numberOfBytes = -1;
          int numberOfLayers = -1;

          // Read the InClassIdentifier
          int index = aditiveOffset + 1;
          if ((index < element[i].length()) && (element[i].codePointAt(index) >= '0') && (element[i].codePointAt(index) <= '9')) {
            InClassIdentifier = 0;
            while ((index < element[i].length()) && (element[i].codePointAt(index) >= '0') && (element[i].codePointAt(index) <= '9')) {
              InClassIdentifier = InClassIdentifier * 10 + element[i].codePointAt(index++) - '0';
            }
          } else {
            if ((element[i].codePointAt(index) != '*')) {
              InClassIdentifier = BinDescriptor.WILDCARD;
            }
          }

          // Read the qualifier if it is present
          if ((index < element[i].length()) && (element[i].codePointAt(index++) == ':')) {
            if (element[i].codePointAt(index) == 'L') { // Read number of layers
              index++;
              if (index >= element[i].length()) {
                throw new ParameterException();
              }
              if ((element[i].codePointAt(index) >= '0') && (element[i].codePointAt(index) <= '9')) {
                numberOfLayers = 0;
                while ((index < element[i].length()) && (element[i].codePointAt(index) >= '0') && (element[i].codePointAt(index) <= '9')) {
                  numberOfLayers = numberOfLayers * 10 + element[i].codePointAt(index++) - '0';
                }
              } else {
                if ((element[i].codePointAt(index) == '*')) {
                  numberOfLayers = BinDescriptor.WILDCARD;
                }
              }

            } else { // Read number of bytes
              if ((element[i].codePointAt(index) >= '0') && (element[i].codePointAt(index) <= '9')) {
                numberOfBytes = 0;
                while ((index < element[i].length()) && (element[i].codePointAt(index) >= '0') && (element[i].codePointAt(index) <= '9')) {
                  numberOfBytes = numberOfBytes * 10 + (element[i].codePointAt(index++) - '0');
                }
              } else {
                if ((element[i].codePointAt(index) == '*')) {
                  numberOfBytes = BinDescriptor.WILDCARD;
                }
              }

            }
          } else {
            if ((index < element[i].length()) && (element[i].codePointAt(index) != ':')) {
              throw new ParameterException();
            }
          }

          cacheDescriptorElement.explicitForm.classIdentifier = ModelElement.PRECINCT;
          cacheDescriptorElement.explicitForm.inClassIdentifier = InClassIdentifier;
          cacheDescriptorElement.explicitForm.numberOfLayers = numberOfLayers;
          cacheDescriptorElement.explicitForm.numberOfBytes = numberOfBytes;
          break;

        // IMPLICIT FORM
        case 't':
        case 'c':
        case 'r':
        case 'p':
          cacheDescriptorElement = ImplicitFormModelParser(element[i]);
          break;

        default:
          throw new ParameterException();

      } // switch

      if (cacheDescriptorElement != null) {
        this.jpipRequestFields.cacheManagementField.model.add(cacheDescriptorElement);
      }
    }	// for

    // DEBUG
    /*
    System.out.println("*****************************************");
    System.out.println("        END MODEL PARSER  (Element)      ");
    System.out.println("*****************************************");
    if (jpipRequestFields.cacheManagementField.model != null) {
      for (int i = 0; i < jpipRequestFields.cacheManagementField.model.size(); i++) {
        System.out.println(jpipRequestFields.cacheManagementField.model.get(i).toString());
      }
    }
    System.out.println("*****************************************");
    */
    // END DEBUG

  }

  /**
   *
   * @param element definition in {@link CADI.Common.Network.JPIP.CacheManagementField#model}
   *
   * @throws ParameterException when the request element is wrong
   */
  private ModelElement ImplicitFormModelParser(String element) throws ParameterException {
    // This method could be optimized if the String variable is passed to an array

    //System.out.println("Implicit Form Model Parser");
    //System.out.println("Parsing: " + element);

    ModelElement cacheDescriptor = new ModelElement(BinDescriptor.IMPLICIT_FORM);

    int index = 0; 	// Indicates the position in the element
    if (element.codePointAt(0) == '-') {
      cacheDescriptor.additive = false;
      index++;
    }

    // Tile, Component, Resolution Level and Precinct Ranges. The first index
    // indicates the minimum value of the range, and the second the maximum.
    int[] tRange = null;
    int[] cRange = null;
    int[] rRange = null;
    int[] pRange = null;
    int numLayers = -1;

    while (index < element.length()) {
      switch (element.codePointAt(index++)) {
        // TODO each case option has a similar code. Better in a function
        case 't':
          //System.out.println("Parsing Tile:" + element.substring(index));
          if (tRange != null) {	// The Tile prefix is duplicated
            throw new ParameterException();
          }
          tRange = new int[2];
          tRange[0] = -1;
          tRange[1] = -1;

          if (index >= element.length()) {
            throw new ParameterException();
          }

          if ((element.codePointAt(index) >= '0') && (element.codePointAt(index) <= '9')) {

            // Read first index position
            tRange[0] = 0;
            while ((index < element.length()) && (element.codePointAt(index) >= '0') && (element.codePointAt(index) <= '9')) {
              tRange[0] = tRange[0] * 10 + element.codePointAt(index++) - '0';
            }

            // Is range available?
            if ((index < element.length()) && (element.codePointAt(index) == '-')) {
              index++;

              // Read last index position
              if (index >= element.length()) {
                throw new ParameterException();
              }

              // Read first index position
              tRange[1] = 0;
              while ((index < element.length()) && (element.codePointAt(index) >= '0') && (element.codePointAt(index) <= '9')) {
                tRange[1] = tRange[1] * 10 + element.codePointAt(index++) - '0';
              }

              // Check range
              if (tRange[0] > tRange[1]) {
                throw new ParameterException();
              }

            } else {
              tRange[1] = tRange[0];
            }
          } else {
            if (element.codePointAt(index) != '*') {
              tRange[0] = BinDescriptor.WILDCARD;
              tRange[1] = BinDescriptor.WILDCARD;
            }
          }

          break;

        case 'c':
          //System.out.println("Parsing Component:" + element.substring(index));
          if (cRange != null) {	// The Tile prefix is duplicated
            throw new ParameterException();
          }
          cRange = new int[2];
          cRange[0] = -1;
          cRange[1] = -1;

          if (index >= element.length()) {
            throw new ParameterException();
          }

          if ((element.codePointAt(index) >= '0') && (element.codePointAt(index) <= '9')) {

            // Read first index position
            cRange[0] = 0;
            while ((index < element.length()) && (element.codePointAt(index) >= '0') && (element.codePointAt(index) <= '9')) {
              cRange[0] = cRange[0] * 10 + element.codePointAt(index++) - '0';
            }

            // Is range available?
            if ((index < element.length()) && (element.codePointAt(index) == '-')) {
              index++;

              // Read last index position
              if (index >= element.length()) {
                throw new ParameterException();
              }

              // Read first index position
              cRange[1] = 0;
              while ((index < element.length()) && (element.codePointAt(index) >= '0') && (element.codePointAt(index) <= '9')) {
                cRange[1] = cRange[1] * 10 + element.codePointAt(index++) - '0';
              }

              // Check range
              if (cRange[0] > cRange[1]) {
                throw new ParameterException();
              }

            } else {
              cRange[1] = cRange[0];
            }
          } else {
            if (element.codePointAt(index) != '*') {
              cRange[0] = BinDescriptor.WILDCARD;
              cRange[1] = BinDescriptor.WILDCARD;
            }
          }

          break;

        case 'r':
          //System.out.println("Parsing Resolution Level:" + element.substring(index));
          if (rRange != null) {	// The Tile prefix is duplicated
            throw new ParameterException();
          }
          rRange = new int[2];
          rRange[0] = -1;
          rRange[1] = -1;

          if (index >= element.length()) {
            throw new ParameterException();
          }

          if ((element.codePointAt(index) >= '0') && (element.codePointAt(index) <= '9')) {

            // Read first index position
            rRange[0] = 0;
            while ((index < element.length()) && (element.codePointAt(index) >= '0') && (element.codePointAt(index) <= '9')) {
              rRange[0] = rRange[0] * 10 + element.codePointAt(index++) - '0';
            }

            // Is range available?
            if ((index < element.length()) && (element.codePointAt(index) == '-')) {
              index++;

              // Read last index position
              if (index >= element.length()) {
                throw new ParameterException();
              }

              // Read first index position
              rRange[1] = 0;
              while ((index < element.length()) && (element.codePointAt(index) >= '0') && (element.codePointAt(index) <= '9')) {
                rRange[1] = rRange[1] * 10 + element.codePointAt(index++) - '0';
              }

              // Check range
              if (rRange[0] > rRange[1]) {
                throw new ParameterException();
              }

            } else {
              rRange[1] = rRange[0];
            }
          } else {
            if (element.codePointAt(index) != '*') {
              rRange[0] = BinDescriptor.WILDCARD;
              rRange[1] = BinDescriptor.WILDCARD;
            }
          }
          break;

        case 'p':
          //System.out.println("Parsing Precinct:" + element.substring(index));
          if (pRange != null) {	// The Tile prefix is duplicated
            throw new ParameterException();
          }
          pRange = new int[2];
          pRange[0] = pRange[1] = -1;

          if (index >= element.length()) {
            throw new ParameterException();
          }

          if ((element.codePointAt(index) >= '0') && (element.codePointAt(index) <= '9')) {

            // Read first index position
            pRange[0] = 0;
            while ((index < element.length()) && (element.codePointAt(index) >= '0') && (element.codePointAt(index) <= '9')) {
              pRange[0] = pRange[0] * 10 + element.codePointAt(index++) - '0';
            }

            // Is range available?
            if ((index < element.length()) && (element.codePointAt(index) == '-')) {
              index++;

              // Read last index position
              if (index >= element.length()) {
                throw new ParameterException();
              }

              // Read first index position
              pRange[1] = 0;
              while ((index < element.length()) && (element.codePointAt(index) >= '0') && (element.codePointAt(index) <= '9')) {
                pRange[1] = pRange[1] * 10 + element.codePointAt(index++) - '0';
              }

              // Check range
              if (pRange[0] > pRange[1]) {
                throw new ParameterException();
              }

            } else {
              pRange[1] = pRange[0];
            }
          } else {
            if (element.codePointAt(index) != '*') {
              pRange[0] = BinDescriptor.WILDCARD;
              pRange[1] = BinDescriptor.WILDCARD;
            }
          }
          break;

        case ':':	// Number of layers, if it is available
          //	 System.out.println("Parsing Qualifier:" + element.substring(index));
          if (element.codePointAt(index++) == 'L') {
            numLayers = 0;
            while (index < element.length()) {
              if ((element.codePointAt(index) < '0') || (element.codePointAt(index) > '9')) {
                throw new ParameterException();
              }
              numLayers = numLayers * 10 + element.codePointAt(index++) - '0';
            }
            if (numLayers <= 0) {
              throw new ParameterException();
            }
          } else {
            throw new ParameterException();
          }
          break;

        default:
          throw new ParameterException();
      }
    }

    cacheDescriptor.implicitForm.firstTilePos = tRange[0];
    cacheDescriptor.implicitForm.lastTilePos = tRange[1];
    cacheDescriptor.implicitForm.firstComponentPos = cRange[0];
    cacheDescriptor.implicitForm.lastComponentPos = cRange[1];
    cacheDescriptor.implicitForm.firstResolutionLevelPos = rRange[0];
    cacheDescriptor.implicitForm.lastResolutionLevelPos = rRange[1];
    cacheDescriptor.implicitForm.firstPrecinctPos = pRange[0];
    cacheDescriptor.implicitForm.lastPrecinctPos = pRange[1];
    cacheDescriptor.implicitForm.numberOfLayers = numLayers;

    return cacheDescriptor;
  }

  /**
   *
   * @param tpmodel definition in {@link CADI.Common.Network.JPIP.CacheManagementField#tpmodel}
   *
   * @throws ParameterException
   */
  private void tpmodelParser(String tpmodel) {
  }

  /**
   *
   * @param need definition in {@link CADI.Common.Network.JPIP.CacheManagementField#need}
   *
   * @throws ParameterException
   */
  private void needParser(String need) {
  }

  /**
   *
   * @param tpneed definition in {@link CADI.Common.Network.JPIP.CacheManagementField#tpneed}
   *
   * @throws ParameterException
   */
  private void tpneedParser(String tpneed) {
  }

  /**
   * Parsers the mset parameter.
   *
   * @param mset definition in {@link CADI.Common.Network.JPIP.CacheManagementField#mset}
   *
   * @throws ParameterException
   */
  private void msetParser(String mset) throws ParameterException {
    String[] element = mset.split(",");

    this.jpipRequestFields.cacheManagementField.mset = new int[element.length][3];

    try {
      for (int i = 0; i < element.length; i++) {
        String[] tempString = null;
        String[] temp1 = null, temp2 = null;

        this.jpipRequestFields.cacheManagementField.mset[i][0] = -1;
        this.jpipRequestFields.cacheManagementField.mset[i][1] = -1;
        this.jpipRequestFields.cacheManagementField.mset[i][2] = -1;

        //Possibilities:
        // 1.- mset=from
        //	2.- mset=from-to
        //	3.- mset=from-to:samplingFactor
        // 4.- mset=from:samplingFactor

        temp1 = tempString[i].split("-");
        if (temp1.length == 2) {	//case 2 or 3
          this.jpipRequestFields.cacheManagementField.mset[i][0] = Integer.parseInt(temp1[0]);
          temp2 = temp1[1].split(":");
          if (temp2.length == 1) {	// case 2
            this.jpipRequestFields.cacheManagementField.mset[i][1] = Integer.parseInt(temp2[0]);
          } else if (temp2.length == 2) {	// case 3
            this.jpipRequestFields.cacheManagementField.mset[i][1] = Integer.parseInt(temp2[0]);
            this.jpipRequestFields.cacheManagementField.mset[i][2] = Integer.parseInt(temp2[1]);
          } else {
            throw new ParameterException();
          }
        } else if (temp1.length == 1) {	// case 1 or 4
          temp2 = temp1[0].split(":");
          if (temp2.length == 1) {	// case 1
            this.jpipRequestFields.cacheManagementField.mset[i][0] = Integer.parseInt(temp2[0]);
          } else if (temp2.length == 2) {
            this.jpipRequestFields.cacheManagementField.mset[i][0] = Integer.parseInt(temp2[0]);
            this.jpipRequestFields.cacheManagementField.mset[i][2] = Integer.parseInt(temp2[1]);
          } else {
            throw new ParameterException();
          }
        } else {
          throw new ParameterException();
        }
      }
    } catch (Exception e) {
      throw new ParameterException();
    }
  }

  //
  // Upload field parsers
  //
  /**
   * @param upload definition in {@link CADI.Common.Network.JPIP.JPIPRequestFields#upload}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void uploadParser(String upload) {
  }

  //
  // Client capabilities preferences field parsers
  //
  /**
   * @param cap defined in {@link CADI.Common.Network.JPIP.ClientCapPrefField#cap}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void capParser(String cap) {
  }

  /**
   * @param pef defined in {@link CADI.Common.Network.JPIP.ClientCapPrefField#pref}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void prefParser(String prefs) throws ParameterException {

    for (String pref : prefs.split(",")) {

      if (pref.startsWith("fullwindow")) {
        jpipRequestFields.clientCapPrefField.pref.viewWindow =
                ClientPreferences.FULLWINDOW;
        if (pref.endsWith("/r")) {
          jpipRequestFields.clientCapPrefField.pref.viewWindowRequired = true;
        }

      } else if (pref.startsWith("progressive")) {
        jpipRequestFields.clientCapPrefField.pref.viewWindow =
                ClientPreferences.PROGRESSIVE;
        if (pref.endsWith("/r")) {
          jpipRequestFields.clientCapPrefField.pref.viewWindowRequired = true;
        }

      } else if (prefs.startsWith("mbw:")) {

        int index = prefs.length() - 1;
        if (pref.endsWith("/r")) {
          jpipRequestFields.clientCapPrefField.pref.maxBandwidthRequired = true;
          index -= 2;
        }

        long multiplier = 1L;

        if (prefs.charAt(index) == 'K') {
          multiplier *= 1L << 10;
        } else if (prefs.charAt(index) == 'M') {
          multiplier *= 1L << 20;
        } else if (prefs.charAt(index) == 'G') {
          multiplier *= 1L << 30;
        } else if (prefs.charAt(index) == 'T') {
          multiplier *= 1L << 40;
        }

        if (multiplier > 1) {
          index--;
        }

        jpipRequestFields.clientCapPrefField.pref.maxBandwidth =
                Integer.parseInt(prefs.substring(4, index)) * multiplier;

      } else {
        throw new ParameterException();
      }
    }
  }

  /**
   * @param csf defined in {@link CADI.Common.Network.JPIP.ClientCapPrefField#csf}
   *
   * @throws ParameterException when the request element is wrong
   */
  private void csfParser(String csf) {
  }
}
