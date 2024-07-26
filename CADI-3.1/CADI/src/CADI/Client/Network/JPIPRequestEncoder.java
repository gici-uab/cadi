/*
 CADI Software - a JPIP Client/Server framework
 Copyright (C) 2007-2012 Group on Interactive Coding of Images (GICI)

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.

 Group on Interactive Coding of Images (GICI)
 Department of Information and Communication Engineering
 Autonomous University of Barcelona
 08193 - Bellaterra - Cerdanyola del Valles (Barcelona)
 Spain

 http://gici.uab.es
 gici-info@deic.uab.es
 */
package CADI.Client.Network;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import CADI.Common.Network.HTTP.StatusCodes;
import CADI.Common.Network.JPIP.ClassIdentifiers;
import CADI.Common.Network.JPIP.JPIPRequestFields;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Common.Cache.ModelElement;
import CADI.Common.Defaults.ImageReturnTypes;

/**
 This class is useful to build a JPIP string from the JPIP parameters.
 <p>
 Usage example:<br>
 &nbsp; construct<br>
 &nbsp; set functions<br>
 &nbsp; createRequest<br>
 &nbsp; getRequest<br>


 @author Group on Interactive Coding of Images (GICI)
 @version 1.0.2 2011/08/21
 */
public class JPIPRequestEncoder implements StatusCodes, ImageReturnTypes {

  /**
   This attribute contains the JPIP request that will be used to build the
   JPIP string.
   */
  private JPIPRequestFields jpipRequestFields = null;

  /**
	 * It contains the JPIP string (as a url).
	 */
  private String request = null;

  // ============================= public methods ==============================
  /**
	 * Constructor.
	 */
  public JPIPRequestEncoder() {
    jpipRequestFields = new JPIPRequestFields();
  }

  public JPIPRequestEncoder(JPIPRequestFields jpipRequestFields) {
    if (jpipRequestFields == null) {
      throw new NullPointerException();
    }
    this.jpipRequestFields = jpipRequestFields;
  }

  /**
	 * Encodes the JPIP Request Fields into a url.
	 * 
	 * @param jpipRequestFields
	 * @return a string with the JPIP request fields encoded.
	 */
  public static String encodeJPIPRequest(JPIPRequestFields jpipRequestFields) {
    String request = "";

    // TARGET FIELD
    if (jpipRequestFields.channelField.cid == null) {
      if (jpipRequestFields.targetField.target != null) {
        try {
          request += "/" + URLEncoder.encode(jpipRequestFields.targetField.target, "UTF-8");
        } catch (UnsupportedEncodingException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } else {
        assert (true);
      }
    } else {
      request += "/" + jpipRequestFields.channelField.path;
    }

    // ? character
    request += "?";

    // Subtarget
    int[] subtarget = jpipRequestFields.targetField.subtarget;
    if (subtarget != null) {
      if (subtarget[0] != -1) {
        request += "&subtarget=" + subtarget[0];
        if (subtarget[1] != -1) {
          request += "-" + subtarget[1];
        }
      }
    }

    // Target ID
    String tid = jpipRequestFields.targetField.tid;
    if (tid != null) {
      request += "&tid=" + tid;
    }

    // CHANNEL FIELD

    // Channel ID
    String cid = jpipRequestFields.channelField.cid;
    if (cid != null) {
      request += "&cid=" + cid;
    }

    // New Channel
    ArrayList<String> cnew = jpipRequestFields.channelField.cnew;
    if (cnew != null) {
      request += "&cnew=";
      for (int i = 0; i < cnew.size(); i++) {
        request += cnew.get(i);
        if (i < (cnew.size() - 1)) {
          request += ",";
        }
      }
    }

    // Channel close
    ArrayList<String> cclose = jpipRequestFields.channelField.cclose;
    if (cclose != null) {
      request += "&cclose=";
      for (int i = 0; i < cclose.size(); i++) {
        request += cclose.get(i);
        if (i < (cclose.size() - 1)) {
          request += ",";
        }
      }
    }

    // Request ID
    int qid = jpipRequestFields.channelField.qid;
    if (qid != -1) {
      request += "&qid=" + qid;
    }


    // VIEW WINDOW FIELD
    // Frame size
    int[] fsiz = jpipRequestFields.viewWindowField.fsiz;
    if (fsiz != null) {
      if ((fsiz[0] != -1) && (fsiz[1] != -1)) {
        request += "&fsiz=" + fsiz[0] + "," + fsiz[1];

        if (jpipRequestFields.viewWindowField.roundDirection != -1) {
          switch (jpipRequestFields.viewWindowField.roundDirection) {
            case ViewWindowField.ROUND_DOWN:
              request += ",round-down";
              break;
            case ViewWindowField.ROUND_UP:
              request += ",round-up";
              break;
            case ViewWindowField.CLOSEST:
              request += ",closest";
              break;
            default:
              assert (true);
          }
        }
      }
    }

    // Offset
    int[] roff = jpipRequestFields.viewWindowField.roff;
    if (roff != null) {
      if ((roff[0] != -1) && (roff[1] != -1)) {
        request += "&roff=" + roff[0] + "," + roff[1];
      }
    }

    // Region size
    int[] rsiz = jpipRequestFields.viewWindowField.rsiz;
    if (rsiz != null) {
      if ((rsiz[0] != -1) && (rsiz[1] != -1)) {
        request += "&rsiz=" + rsiz[0] + "," + rsiz[1];
      }
    }

    // Components
    int[][] comps = jpipRequestFields.viewWindowField.comps;
    if (comps != null) {
      request += "&comps=";
      for (int i = 0; i < comps.length; i++) {
        request += comps[i][0];
        if (comps[i][1] != -1) {
          request += "-" + comps[i][1];
        }
        if (i < (comps.length - 1)) {
          request += ",";
        }
      }
    }

    // Stream
    int[][] stream = jpipRequestFields.viewWindowField.stream; // array of [][3], where 3 = {from, to, sampling-factor}

    if (stream != null) {
      request += "&stream=";
      for (int i = 0; i < stream.length; i++) {
        assert (stream[i][0] >= 0);
        request += stream[i][0];	// from
        if (stream[i][1] != -1) {	// if "to" is present
          assert ((stream[i][1] >= 0) && (stream[i][1] > stream[i][0]));
          request += "-" + stream[i][1];
          if (stream[i][2] != -1) {	// if "sampling-factor" is present
            assert (stream[i][2] >= 0);
            request += ":" + stream[i][2];
          }
        }
        if (i < (stream.length - 1)) {
          request += ",";
        }
      }
    }

    // Codestream Context


    // Sampling Rate
    float srate = jpipRequestFields.viewWindowField.srate;
    if (!(srate < 0)) {
      request += "&srate=" + srate;
    }

    // Layers
    int layers = jpipRequestFields.viewWindowField.layers;
    if (layers != -1) {
      request += "&layers=" + layers;
    }

    //	 ROI

    // METADATA FIELD


    // DATA LIMIT FIELD

    // len
    int len = jpipRequestFields.dataLimitField.len;
    if (len != -1) {
      request += "&len=" + len;
    }

    // Quality
    int quality = jpipRequestFields.dataLimitField.quality;
    if (quality != -1) {
      request += "&quality=" + quality;
    }

    // SERVER CONTROL FIELD

    // Alignment
    if (jpipRequestFields.serverControlField.align) {
      request += "&align=yes";
    }

    if (jpipRequestFields.serverControlField.wait) {
      request += "&wait=yes";
    }

    // Image return type
    ArrayList<String> type = jpipRequestFields.serverControlField.type;
    if ((type != null) && (type.size() > 0)) {
      request += "&type=";
      request += type.get(0);
      for (int i = 1; i < type.size(); i++) {
        request += "," + type.get(i);
      }
    }

    // Delivery Rate
    float drate = jpipRequestFields.serverControlField.drate;
    if (!(drate < 0)) {
      request += "&drate=" + drate;
    }

    // CACHE MANAGEMENT FIELD

    // Model		
    ArrayList<ModelElement> model = jpipRequestFields.cacheManagementField.model;
    if ((model != null) && (model.size() > 0)) {
      request += "&model=";
      ModelElement cacheDescriptor = null;

      for (int i = 0; i < model.size(); i++) {
        cacheDescriptor = model.get(i);

        // Check whether it is aditive or sustractive
        if (!cacheDescriptor.additive) {
          request += "-";
        }

        if (cacheDescriptor.explicitForm != null) {
          switch (cacheDescriptor.explicitForm.classIdentifier) {
            case ClassIdentifiers.PRECINCT:
              if (cacheDescriptor.explicitForm != null) {
                // In class identifier
                request += "P" + ((cacheDescriptor.explicitForm.inClassIdentifier != -1) ? cacheDescriptor.explicitForm.inClassIdentifier : "*");
              } else {
                // Tile identifier
                if (cacheDescriptor.implicitForm.firstTilePos < cacheDescriptor.implicitForm.lastTilePos) {
                  request += "t" + cacheDescriptor.implicitForm.firstTilePos + "-" + cacheDescriptor.implicitForm.lastTilePos;
                } else {
                  if (cacheDescriptor.implicitForm.firstTilePos == cacheDescriptor.implicitForm.lastTilePos) {
                    request += "t" + cacheDescriptor.implicitForm.firstTilePos;
                  } else {
                    request += "t*";
                  }
                }
                // Component identifier
                if (cacheDescriptor.implicitForm.firstComponentPos < cacheDescriptor.implicitForm.lastComponentPos) {
                  request += "c" + cacheDescriptor.implicitForm.firstComponentPos + "-" + cacheDescriptor.implicitForm.lastComponentPos;
                } else {
                  if (cacheDescriptor.implicitForm.firstComponentPos == cacheDescriptor.implicitForm.lastComponentPos) {
                    request += "c" + cacheDescriptor.implicitForm.firstComponentPos;
                  } else {
                    request += "c*";
                  }
                }
                // Resolution level identifier
                if (cacheDescriptor.implicitForm.firstResolutionLevelPos < cacheDescriptor.implicitForm.lastResolutionLevelPos) {
                  request += "r" + cacheDescriptor.implicitForm.firstResolutionLevelPos + "-" + cacheDescriptor.implicitForm.lastResolutionLevelPos;
                } else {
                  if (cacheDescriptor.implicitForm.firstResolutionLevelPos == cacheDescriptor.implicitForm.lastResolutionLevelPos) {
                    request += "r" + cacheDescriptor.implicitForm.firstResolutionLevelPos;
                  } else {
                    request += "r*";
                  }
                }
                // Precinct identifier
                if (cacheDescriptor.implicitForm.firstPrecinctPos < cacheDescriptor.implicitForm.lastPrecinctPos) {
                  request += "p" + cacheDescriptor.implicitForm.firstPrecinctPos + "-" + cacheDescriptor.implicitForm.lastPrecinctPos;
                } else {
                  if (cacheDescriptor.implicitForm.firstPrecinctPos == cacheDescriptor.implicitForm.lastPrecinctPos) {
                    request += "p" + cacheDescriptor.implicitForm.firstPrecinctPos;
                  } else {
                    request += "p*";
                  }
                }
              }

              // Qualifier
              if (cacheDescriptor.explicitForm.numberOfLayers != -1) {
                request += ":L" + cacheDescriptor.explicitForm.numberOfLayers;
              } else {
                if (cacheDescriptor.explicitForm.numberOfBytes != -1) {
                  request += ":" + cacheDescriptor.explicitForm.numberOfBytes;
                } else {
                  assert (true);
                }
              }

              break;

            case ClassIdentifiers.TILE_HEADER:
              if (cacheDescriptor.explicitForm != null) {
                request += "H" + ((cacheDescriptor.explicitForm.inClassIdentifier != -1) ? cacheDescriptor.explicitForm.inClassIdentifier : "*");
              } else {
                assert (true);
              }
              break;

            case ClassIdentifiers.TILE:
              break;

            case ClassIdentifiers.MAIN_HEADER:
              if (cacheDescriptor.explicitForm != null) {
                request += "Hm";
              } else {
                assert (true);
              }
              break;

            case ClassIdentifiers.METADATA:
              break;
          }
        } else {
          System.err.println("IMPLICIT CACHE SIGNALING IS NOT IMPLMENTED YET !!!");
        }

        if (i < model.size() - 1) {
          request += ",";
        }
      }
    }


    // UPLOAD FIELD

    //CLIENT CAPABILITY PREFERENCE FIELD


    request = request.replaceFirst("&", "");

    return request;

  }

  /**
	 * Encoders the JPIP parameters as a URL string
	 * 
	 * @return a string with the JPIP request.
	 */
  public String createRequest() {
    return encodeJPIPRequest(jpipRequestFields);
  }

  /**
	 * Resests the request line.
	 */
  public void reset() {
    jpipRequestFields.reset();
    request = null;
  }

  /**
	 * For debugging purposes. 
	 */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";
    if (jpipRequestFields != null) {
      str += jpipRequestFields.toString();
    }
    if (request != null) {
      str += " Request=" + request;
    }
    str += "]";
    return str;
  }

  /**
	 * Prints this JPIP Request Encoder fields out to the specified output
	 * stream. This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
  public void list(PrintStream out) {

    out.println("-- JPIP Request Encoder --");

    if (jpipRequestFields != null) {
      jpipRequestFields.list(out);
    }
    if (request != null) {
      out.println("Request: " + request);
    }

    out.flush();
  }
}