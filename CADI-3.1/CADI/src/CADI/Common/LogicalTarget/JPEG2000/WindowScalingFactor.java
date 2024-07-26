/*
 * S * CADI Software - a JPIP Client/Server framework
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
package CADI.Common.LogicalTarget.JPEG2000;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

import CADI.Common.LogicalTarget.JPEG2000.Parameters.COMParameters;
import CADI.Common.Network.JPIP.EORCodes;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Common.Util.ArraysUtil;
import CADI.Common.Util.CADIDimension;
import CADI.Common.Util.CADIRectangle;
import CADI.Common.Cache.CacheModel;
import GiciException.ErrorException;

/**
 * This class implements a delivery of data belonging to the requested WOI
 * following a Window Scaling Factor (WSF) strategy. The WSF applies a
 * weight to each precinct belonging to the WOI according to the percentage
 * of pixels overlapped.
 * <p>
 * Further information about the Window Scaling Factor, see:
 * Taubman, D. and Rosenbaum, R., "Rate-Distortion Optimized Interactive
 * Browsing of JPEG2000 Images"
 * and
 * Lima, L. and Taubman, D. and Leonardi, R., "JPIP Proxy Server for
 * remote browsing of JPEG2000 images"
 *
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2008/09/08
 *
 * <p>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; runResponseParameters<br>
 * &nbsp; runResponseData<br>
 * &nbsp; getResponseViewWindow<br>
 * &nbsp; getQuality<br>
 * &nbsp; getJPIPMessageData<br>
 * &nbsp; getEORReasonCode<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.6 2011/08/12
 */
public class WindowScalingFactor {

  /**
   * Definition in {@link CADI.Server.LogicalTarget.ServerLogicalTargetManager#responseViewWindow}.
   */
  protected ViewWindowField responseViewWindow = null;

  /**
   *
   */
  protected JPEG2KLogicalTarget logicalTarget = null;

  /**
   * Addtional window scaling factors.
   * <p>
   * Defines new scaling factors / weights to be applied. The <code>key</code>
   * of the HashMap is the unique precinct identifier and the <code>value</code>
   * is a real value in the range [0,1] with the weight.
   */
  protected PredictiveScalingFactors scalingFactors = null;

  /**
   * This attribute contains the cache data for the client.
   * <p>
   * This reference is passed from the
   *
   */
  protected CacheModel serverCache = null;

  /**
   *
   */
  protected JPEG2KCodestream codestream = null;

  /**
   * Definition in {@link CADI.Server.LogicalTarget.ServerLogicalTargetManager#quality}.
   */
  protected int quality = -1;

  /**
   * Definition in {@link CADI.Server.LogicalTarget.ServerLogicalTargetManager#EORReasonCode}.
   */
  protected int EORReasonCode;

  /**
   * Definition in {@link CADI.Common.Network.JPIP.DataLimitField#len}
   */
  protected long maximumResponseLength = Long.MAX_VALUE;

  /**
   * Definition in {@link CADI.Common.Network.JPIP.ServerControlField#align}.
   */
  protected boolean align = false;

  // INTERNAL ATTRIBUTES
  /**
   * It is a temporal attribute to accumulate the response length which is
   * sending to the client.
   */
  private long responseLength = 0;

  /**
   *
   */
  private int discardLevels = -1;

  /**
   *
   */
  private ViewWindowField actualViewWindow = null;

  /**
   *
   */
  private ArrayList<RelevantPrecinct> relevantPrecincts = null;

  /**
   *
   */
  private int[] compressionSlopeThresholds = null;

  /**
   *
   */
  private float[] resequencedSlopeThresholds = null;

  /**
   * Number of resequenced quality layers.
   * <p>
   * The paper says that "the number of resequenced layers must generally be
   * larger than the number of original code-stream layers", therefore, the
   * number of resequenced layers must be an image dependent value.
   * <p>
   * In this case, the maximum number of resequenced layer has been set to a
   * fixed value but it will be changed in the next revision.
   */
  private static final int NUM_SLOPE_THRESHOLDS = 25;

  // ============================= public methods ==============================
  /**
   * Constructor.
   * <p>
   * NOTE: Use of this constructor is discouraged, and it only be used by
   * classes that extend this one.
   */
  public WindowScalingFactor() {
  }

  /**
   * Constructor.
   *
   * @param codestream definition in {@link #codestream}.
   * @param serverCache
   * @param codestream
   */
  public WindowScalingFactor(JPEG2KLogicalTarget logicalTarget,
          CacheModel serverCache,
          JPEG2KCodestream codestream) {
    this(logicalTarget, serverCache, codestream, false);
  }

  /**
   * Constructor.
   *
   * @param codestream definition in {@link #codestream}.
   * @param serverCache
   * @param codestream
   * @param align
   */
  public WindowScalingFactor(JPEG2KLogicalTarget logicalTarget,
          CacheModel serverCache,
          JPEG2KCodestream codestream,
          boolean align) {

    // Check input parameters
    if (logicalTarget == null) {
      throw new NullPointerException();
    }
    if (serverCache == null) {
      throw new NullPointerException();
    }
    if (codestream == null) {
      throw new NullPointerException();
    }

    // Get the codestream & initializations
    this.logicalTarget = logicalTarget;
    this.serverCache = serverCache;
    this.codestream = codestream;
    this.align = align;
    responseViewWindow = new ViewWindowField();
    responseLength = 0;

    // Set end of response code
    EORReasonCode = EORCodes.WINDOW_DONE;
  }

  /**
   * Sets the {@link #aditionSF} attribute.
   * 
   * @param aditionalSF definition in {@link #aditionalSF}.
   */
  public void setAditionalScalingFactors(PredictiveScalingFactors scalingFactors) {
    this.scalingFactors = scalingFactors;
  }

  /**
   * Calculates the WOI which will be sent to the client using a layers-based
   * rate-distortion method (file is delivery in the same order that is is
   * saved).
   *
   * @param viewWindow the requested Window Of Interest
   * @throws ErrorException
   */
  public void runResponseParameters(ViewWindowField viewWindow)
          throws ErrorException {

    // Check input parameters
    if (viewWindow == null) {
      throw new NullPointerException();
    }

    actualViewWindow = null;

    // If the frame size has been omitted, no compressed image data is sent.
    if ((viewWindow.fsiz[0] < 0) && (viewWindow.fsiz[1] < 0)) {
      responseViewWindow = new ViewWindowField();
      return;
    }

    // It is the WOI to be delivered.
    // All fields of the object have to be set and check ranges
    actualViewWindow = new ViewWindowField(viewWindow);
    if (actualViewWindow.fsiz[0] > codestream.getXSize()) {
      actualViewWindow.fsiz[0] = codestream.getXSize();
    }
    if (actualViewWindow.fsiz[1] > codestream.getYSize()) {
      actualViewWindow.fsiz[1] = codestream.getYSize();
    }
    if (actualViewWindow.roff[0] < 0) {
      actualViewWindow.roff[0] = 0;
    }
    if (actualViewWindow.roff[1] < 0) {
      actualViewWindow.roff[1] = 0;
    }
    if (actualViewWindow.rsiz[0] < 0) {
      actualViewWindow.rsiz[0] = actualViewWindow.fsiz[0];
    }
    if (actualViewWindow.rsiz[1] < 0) {
      actualViewWindow.rsiz[1] = actualViewWindow.fsiz[1];
    }
    if (actualViewWindow.roff[0] + actualViewWindow.rsiz[0] > actualViewWindow.fsiz[0]) {
      actualViewWindow.rsiz[0] = actualViewWindow.fsiz[0] - actualViewWindow.roff[0];
    }
    if (actualViewWindow.roff[1] + actualViewWindow.rsiz[1] > actualViewWindow.fsiz[1]) {
      actualViewWindow.rsiz[1] = actualViewWindow.fsiz[1] - actualViewWindow.roff[1];
    }
    if (actualViewWindow.comps == null) {
      actualViewWindow.comps = new int[1][2];
      actualViewWindow.comps[0][0] = 0;
      actualViewWindow.comps[0][1] = codestream.getZSize() - 1;
    }
    if (actualViewWindow.layers < 0) {
      actualViewWindow.layers = codestream.getNumLayers();
    }
    if (actualViewWindow.layers > codestream.getNumLayers()) {
      actualViewWindow.layers = codestream.getNumLayers();
    }

    if ((actualViewWindow.roff[0] >= actualViewWindow.fsiz[0])
        || (actualViewWindow.roff[1] >= actualViewWindow.fsiz[1])) {
      throw new ErrorException("The frame size cannot be greater than the region offset");
    }

    // Get the number of discard levels
    discardLevels =
            codestream.determineNumberOfDiscardLevels(actualViewWindow.fsiz,
            actualViewWindow.roundDirection);

    // Fit requested region to the suitable resolution
    codestream.mapRegionToSuitableResolutionGrid(actualViewWindow.fsiz,
            actualViewWindow.roff,
            actualViewWindow.rsiz,
            discardLevels);

    // Determine the bounding box of the requested region
    //CADIRectangle boundingBox = JPEG2000Util.calculatePrecinctsBoundingBox(actualViewWindow,
    //								jpcParameters.sizParameters, jpcParameters.codParameters);

    // Check if geometry has been change.
    // If so, it must be notified to client
    if ((viewWindow.fsiz[0] != actualViewWindow.fsiz[0])
        || (viewWindow.fsiz[1] != actualViewWindow.fsiz[1])) {
      responseViewWindow.fsiz[0] = actualViewWindow.fsiz[0];
      responseViewWindow.fsiz[1] = actualViewWindow.fsiz[1];
    }
    if ((viewWindow.roff[0] != actualViewWindow.roff[0])
        || (viewWindow.roff[1] != actualViewWindow.roff[1])) {
      responseViewWindow.roff[0] = actualViewWindow.roff[0];
      responseViewWindow.roff[1] = actualViewWindow.roff[1];
    }
    if ((viewWindow.rsiz[0] != actualViewWindow.rsiz[0])
        || (viewWindow.rsiz[1] != actualViewWindow.rsiz[1])) {
      responseViewWindow.rsiz[0] = actualViewWindow.rsiz[0];
      responseViewWindow.rsiz[1] = actualViewWindow.rsiz[1];
    }


    // DEBUG
    /*System.out.println("\n\nREQUESTED VIEW WINDOW");
    viewWindow.list(System.out);
    System.out.println("ACTUAL VIEW WINDOW");
    actualViewWindow.list(System.out);
    System.out.println("RESPONSE VIEW WINDOW");
    responseViewWindow.list(System.out);*/

    // Adjust the quality response parameter to the quality layers.
    // It is a coarse estimation and it should be improved (but is the easiest method)
    quality = (int)(100D / (double)codestream.getNumLayers()) * actualViewWindow.layers;
  }

  /**
   *
   * @return
   * @throws ErrorException
   */
  public ArrayList<RelevantPrecinct> runResponseData() throws ErrorException {
    return runResponseData(Long.MAX_VALUE);
  }

  /**
   *
   * @param maximumResponseLength definition in {@link #maximumResponseLength}
   *
   * @throws ErrorException
   */
  public ArrayList<RelevantPrecinct> runResponseData(long maximumResponseLength)
          throws ErrorException {

    // Is there any window?
    if (actualViewWindow == null) {
      return new ArrayList<RelevantPrecinct>();
    }

    // Copy input parameters
    this.maximumResponseLength = (maximumResponseLength > 0)
            ? maximumResponseLength : Long.MAX_VALUE;

    // Initialize thresholds
    resequencedSlopeThresholds = computeResequencedSlopeThresholds(NUM_SLOPE_THRESHOLDS);

    // Compute slope thresholds
    compressionSlopeThresholds = computeCompressionSlopeThresholds(
            codestream.getCOMParameters(), codestream.getNumLayers());

    // Get relevant precincts
    findRelevantPrecinctsResequencedLayers(actualViewWindow);

    //
    packetsSequencing();

    //System.out.println("=== WINDOW SCALING FACTOR - BEGIN ====");
    //for (RelevantPrecinct p : relevantPrecincts) System.out.println(p.toString());
    //System.out.println("=== WINDOW SCALING FACTOR - END ====");

    return relevantPrecincts;
  }

  /**
   * Sets the {@link #align} attribute.
   */
  public final void setAlign(boolean align) {
    this.align = align;
  }

  /**
   * Returns the {@link #EORReasonCode} attribute.
   *
   * @return the {@link #EORReasonCode} attribute.
   */
  public final int getEORReasonCode() {
    return EORReasonCode;
  }

  /**
   * Returns the {@link #responseViewWindow} attribute.
   *
   * @return the {@link #responseViewWindow} attribute.
   */
  public final ViewWindowField getResponseViewWindow() {
    return responseViewWindow;
  }

  /**
   * Returns the {@link #quality} attribute.
   *
   * @return the {@link #quality} attribute.
   */
  public final int getQuality() {
    return quality;
  }

  /**
   *
   */
  public static float[] computeResequencedSlopeThresholds(int numSlopeThresholds) {
    float[] resequencedSlopeThresholds = new float[numSlopeThresholds];

    for (int i = 0; i < numSlopeThresholds; i++) {
      resequencedSlopeThresholds[i] = i + 1;
      resequencedSlopeThresholds[i] =
              1 + (resequencedSlopeThresholds[i] - 1) * (10 - 1) * 1.0F / (numSlopeThresholds - 1);
      resequencedSlopeThresholds[i] = 1 - (float)Math.log10(resequencedSlopeThresholds[i]);
    }

    // DEBUG
    //System.out.println("RESEQUENCED SLOPE THRESHOLDS:");
    //for (int i = 0; i < numSlopeThresholds; i++) {
    //  System.out.println(i + " " + resequencedSlopeThresholds[i]);
    //}
    //System.out.println();
    // END DEBUG

    return resequencedSlopeThresholds;
  }

  /**
   * This method computes the compression slope thresholds used in the Window
   * Scaling Factor algorithm.
   * <p>
   * If thresholds have been saved in the codestream main header's COM marker,
   * their values are used. Otherwise, a log function is used to compute their
   * values.
   */
  public static int[] computeCompressionSlopeThresholds(
          COMParameters comParameters, int numLayers) {

    int[] compressionSlopeThresholds = null;

    if (comParameters.boiSlopes != null) {
      // BOI
      compressionSlopeThresholds = comParameters.boiSlopes;

      if (numLayers >= compressionSlopeThresholds.length) {
        // We expand the length performing a linear estimation of thresholds
        float slope = (compressionSlopeThresholds[compressionSlopeThresholds.length - 1] - Integer.MIN_VALUE) * 1.0F
                      / (numLayers - compressionSlopeThresholds.length);

        for (int i = compressionSlopeThresholds.length; i < numLayers; i++) {
          compressionSlopeThresholds[i] = compressionSlopeThresholds[i - 1] - (int)slope;
        }
      }
    } else if (comParameters.kduLayerLogSlopes != null) {
      // Kakadu
      compressionSlopeThresholds = comParameters.kduLayerLogSlopes;

      if (numLayers >= compressionSlopeThresholds.length) {
        int[] tmpSlopes = new int[numLayers + 1];
        tmpSlopes = Arrays.copyOf(compressionSlopeThresholds, numLayers + 1);

        if (compressionSlopeThresholds.length > 1) {
          // Extends the number of thresholds up to the number of layers. New
          // thresholds are taken as a linear combination of the last ones.
          for (int i = compressionSlopeThresholds.length; i < tmpSlopes.length; i++) {
            tmpSlopes[i] = tmpSlopes[i - 1] - (tmpSlopes[i - 2] - tmpSlopes[i - 1]);
          }
        } else {
          tmpSlopes[1] = compressionSlopeThresholds[0] - 65536;
        }

        compressionSlopeThresholds = tmpSlopes;

        // Kakadu records slopes thresholds applying a conversion. They are
        // adjusted to the range used by CADI.
        for (int i = 0; i < compressionSlopeThresholds.length; i++) {
          compressionSlopeThresholds[i] += 65536;
        }
      }
    } else {

      // The compression slope thresholds have not been saved in the compressed
      // image file, therefore, the values are supposed

      // If the image has properly number of quality layers (we have considered
      // 5) it is chosen. If not, it is a fixed value.
      int numThresholds = numLayers >= 5 ? numLayers + 1 : NUM_SLOPE_THRESHOLDS;

      compressionSlopeThresholds = new int[numThresholds];
      for (int i = 0; i < numThresholds; i++) {
        compressionSlopeThresholds[i] = (int)((Math.log(numThresholds - i)
                                               / Math.log(numThresholds + 1)) * (1 << 16));
      }
    }
    // DEBUG
    //System.err.println("=================================================");
    //System.err.println("COMPRESSION SLOPE THRESHOLDS");
    //for (int i = 0; i < compressionSlopeThresholds.length; i++) {
    //System.err.println(i + " " + compressionSlopeThresholds[i]);}
    //System.err.println();
    //System.err.println("=================================================");
    // END DEBUG

    return compressionSlopeThresholds;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    String str = "";

    str = getClass().getName() + " [";

    str += "Not implemented yet";

    str += "]";
    return str;
  }

  /**
   * Prints this File Order Delivery out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out
   *           an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Window Scaling Factor --");

    out.println("Not implemented yet");

    out.flush();
  }

  // ============================ private methods ==============================
  
  /**
   * This methods determines which precincts are relevant to the requested
   * Window Of Interest.
   * 
   * @param actualViewWindow 
   */
  private void findRelevantPrecinctsResequencedLayers(ViewWindowField actualViewWindow) {

    assert (discardLevels >= 0);
    assert (actualViewWindow != null);
    assert (resequencedSlopeThresholds != null);

    if (scalingFactors == null) {
      scalingFactors = new PredictiveScalingFactors(1F);
    }

    relevantPrecincts = new ArrayList<RelevantPrecinct>();

    int[] components = ArraysUtil.rangesToIndexes(actualViewWindow.comps);
    ArrayList<Integer> relevantTiles = codestream.calculateRelevantTiles(actualViewWindow, discardLevels);

    //actualViewWindow.list(System.out);
    //System.out.println("Discard levels="+discardLevels);

    JPEG2KTile tileObj = null;
    JPEG2KComponent compObj = null;
    JPEG2KResolutionLevel rLevelObj = null;

    // Calculate the max. resol. level for the requested components
    int maxRLevel = 0;
    for (int tileIndex : relevantTiles) {
      tileObj = codestream.getTile(tileIndex);
      for (int comp : components) {
        if (maxRLevel < tileObj.getComponent(comp).getWTLevels()) {
          maxRLevel = tileObj.getComponent(comp).getWTLevels();
        }
      }
    }


    ArrayList<RelevantPrecinct> tmpPrecinctsList = new ArrayList<RelevantPrecinct>();

    for (int tileIndex : relevantTiles) {
      tileObj = codestream.getTile(tileIndex);

      for (int rLevel = 0; rLevel <= maxRLevel; rLevel++) {

        for (int component : components) {

          compObj = tileObj.getComponent(component);
          rLevelObj = compObj.getResolutionLevel(rLevel);

          int maxRLevelsComponent = compObj.getWTLevels();
          if (maxRLevelsComponent >= discardLevels) {
            // Discard the r highest res. levels
            if (rLevel > maxRLevelsComponent - discardLevels) {
              continue;
            }
          } else {
            // It should discard more res. levels than ones available in this
            // tile-component. Then, includes only the LL sub-band.
            if (rLevel != 0) {
              continue;
            }
          }

          CADIDimension frameSize = JPEG2000Util.calculateFrameSize(
                  codestream.getXSize(), codestream.getYSize(),
                  codestream.getXOSize(), codestream.getYOSize(),
                  maxRLevelsComponent - rLevel);

          // Translation of requested region with from the desired frame size reference grid
          // into the sub-sampled reference grid at this frame size
          CADIRectangle supportRegion = new CADIRectangle(actualViewWindow.roff[0],
                  actualViewWindow.roff[1],
                  actualViewWindow.rsiz[0],
                  actualViewWindow.rsiz[1]);
          codestream.calculateSupportRegion(tileIndex, component, rLevel, supportRegion, discardLevels);


          // Number of precincts for each resolution level
          int precinctWidth = rLevelObj.getPrecinctWidth();
          int precinctHeight = rLevelObj.getPrecinctHeight();
          if (precinctWidth > frameSize.width) {
            precinctWidth = frameSize.width;
          }
          if (precinctHeight > frameSize.height) {
            precinctHeight = frameSize.height;
          }

          int numPrecinctsWidth = rLevelObj.getNumPrecinctsWide();
          //int numPrecinctsHeight = rLevelObj.getNumPrecinctsHeigh();

          // Find the start and end precincts
          int startXPrecinct = 0, endXPrecinct = 0;
          int startYPrecinct = 0, endYPrecinct = 0;
          startXPrecinct = (int)(Math.floor((double)supportRegion.x / (double)(precinctWidth)));
          startYPrecinct = (int)(Math.floor((double)supportRegion.y / (double)(precinctHeight)));
          endXPrecinct = (int)(Math.ceil((double)(supportRegion.x + supportRegion.width) / (double)(precinctWidth)));	// not included
          endYPrecinct = (int)(Math.ceil((double)(supportRegion.y + supportRegion.height) / (double)(precinctHeight)));	// not included

          for (int yPrecinct = startYPrecinct; yPrecinct < endYPrecinct; yPrecinct++) {
            for (int xPrecinct = startXPrecinct; xPrecinct < endXPrecinct; xPrecinct++) {
              long inClassIdentifier = rLevelObj.getInClassIdentifier(yPrecinct * numPrecinctsWidth + xPrecinct);

              int lengthOfDataBinSent = (int)serverCache.getPrecinctDataBinLength(inClassIdentifier);
              int layerOfDataSent = serverCache.getPrecinctDataBinLayers(inClassIdentifier);
              if ((lengthOfDataBinSent < 0) && (layerOfDataSent < 0)) {
                lengthOfDataBinSent = 0;
                layerOfDataSent = 0;
              } else if ((lengthOfDataBinSent > 0) && (layerOfDataSent < 0)) {
                layerOfDataSent = logicalTarget.getLastCompleteLayer(inClassIdentifier, lengthOfDataBinSent);
              } /*else if ((lengthOfDataBinSent < 0) && (layerOfDataSent > 0)) {
                lengthOfDataBinSent = logicalTarget.getPacketOffsetWithDataBin(inClassIdentifier, layerOfDataSent)
                                      + logicalTarget.getPacketLength(inClassIdentifier, layerOfDataSent);
              }*/
              
              assert(layerOfDataSent >= 0);

              if (actualViewWindow.layers <= layerOfDataSent) {
                // All layers are already in client cache.
                continue;
              }

              CADIRectangle precinct = new CADIRectangle(xPrecinct * precinctWidth, yPrecinct * precinctHeight, precinctWidth, precinctHeight);
              
              RelevantPrecinct rp = new RelevantPrecinct(inClassIdentifier);
              long areaPrecinct = precinct.getArea();
              
              precinct.intersection(supportRegion.x, supportRegion.y, supportRegion.width, supportRegion.height);
              rp.overlapFactor = precinct.getArea() * 1.0F / areaPrecinct;
              rp.tile = tileIndex;
              rp.component = component;
              rp.rLevel = rLevel;
              rp.precinct = yPrecinct * numPrecinctsWidth + xPrecinct;

              // Window scaling factor
              float wsfPenalty = 0;
              for (int i = 0; i < resequencedSlopeThresholds.length; i++) {
                wsfPenalty = (i == 0) ? 0 : i * 0.001F;
                if (rp.overlapFactor >= resequencedSlopeThresholds[i]) {
                  break;
                }
              }

              // Predictive scaling factor
              float psfPenalty = 0;
              float predScalingFactor = scalingFactors.getValue(inClassIdentifier);
              for (int i = 0; i < resequencedSlopeThresholds.length; i++) {
                psfPenalty = (i == 0) ? 0 : i * 0.001F;
                if (predScalingFactor >= resequencedSlopeThresholds[i]) {
                  break;
                }
              }

              rp.wsf = 1 - wsfPenalty - psfPenalty;

              tmpPrecinctsList.add(rp);
            }
          }
        }

        // TODO: this part of the code must be improved !!!!
        // Maybe the Interface SortedMap<K,V> could be useful.

        // Sort the precincts by ratio of area
				/* Collections.sort(tmpPrecinctsList, new Comparator<RelevantPrecinct>() {
         * public int compare(RelevantPrecinct p1,	RelevantPrecinct p2) {
         * if (p1.overlapFactor < p2.overlapFactor) return 1;
         * else if (p1.overlapFactor > p2.overlapFactor) return -1;
         * else return 0;
         * }
         * });
         *
         * // Add sorted precincts to list of relevant precincts
         * for (RelevantPrecinct p : tmpPrecinctsList)
         * this.relevantPrecincts.add(p);
         */
        for (int i = 0; i < resequencedSlopeThresholds.length; i++) {
          for (RelevantPrecinct p : tmpPrecinctsList) {
            if (!p.visited) {
              if (p.overlapFactor >= resequencedSlopeThresholds[i]) {
                relevantPrecincts.add(p);
                p.visited = true;
              }
            }
          }
        }
        for (RelevantPrecinct p : tmpPrecinctsList) {
          if (!p.visited) {
            relevantPrecincts.add(p);
            p.visited = true;
          }
        }

        tmpPrecinctsList.clear();
        // end of code to be improved

      }
    }

    //for (RelevantPrecinct rp : relevantPrecincts) System.out.println(rp.toStringSummary()); // DEBUG
    tmpPrecinctsList = null;
  }

  /**
   * This method performs the packet sequencing algorithm of the Window Scaling
   * Factor.
   * <p>
   * Brief explanation of the algorithm: the main loop goes through the weighted
   * slope thresholds and a nested loop through the list of relevant precincts.
   * On each iteration of the main loop, a candidate to be the next threshold is
   * searched among all the relevant precincts. The chosen candidate is the
   * precinct which has the greater weighted threshold value for the next layer
   * of the precinct to be considered.
   * <p>
   * In each main loop iteration, only those packets with a weighted slope
   * threshold greater than the loop weighted threshold are considered to be
   * delivered to clients.
   * <p>
   * An exception to this statement is done for the first quality layers. They
   * are prioritized without taken into account the weighted threshold at the
   * beginning of the algorithm. The maximum number of quality layers
   * prioritized is set up by the MAX_PREEMPTED_LAYERS variable, and the
   * preemptedLayers controls which layer is being prioritized.
   * OBS: the idea of prioritizing the first quality layers is based on the
   * assumption that information saved in the first layers have a great
   * contribution in the decoding process. The question about how many layers
   * prioritize depends on the image nature and the compression parameterization.
   */
  protected void packetsSequencing() {

    float slopeThreshold = Float.POSITIVE_INFINITY;
    float nextSlopeThreshold = Float.NEGATIVE_INFINITY;

    int preemptedLayers = 0; // See method comments
    final int MAX_PREEMPTED_LAYERS = 1; // See method comments

    int numRelevantPrecincts = relevantPrecincts.size();

    // Main loop through thresholds
    while ((slopeThreshold != Float.NEGATIVE_INFINITY) && responseLength < maximumResponseLength) {
      int index = 0;

      // Loop on relevant precincts
      while ((index < numRelevantPrecincts) && (responseLength < maximumResponseLength)) {

        // Get the precinct
        RelevantPrecinct precinct = relevantPrecincts.get(index);

        if ((precinct.actualNumPacket >= codestream.getNumLayers())
            || (actualViewWindow.layers <= precinct.endLayer)) {
          // All packets/layers of this precinct has been delivered,
          // continue with the next precinct.
          index++;

          continue;
        }

        // Search for a candidate to be the next threshold
        float tmpThreshold = (compressionSlopeThresholds[precinct.endLayer + 1]) * precinct.wsf;

        // Update the next threshold candidate as the maximum value among
        // all the relevant precincts
        if (tmpThreshold > nextSlopeThreshold) {
          nextSlopeThreshold = tmpThreshold;
        }

        if (preemptedLayers < MAX_PREEMPTED_LAYERS) {
          // Prioritizes first layers although they must not be transmitted
          // if considering their weighted threshold
          if (precinct.endLayer >= MAX_PREEMPTED_LAYERS) {
            index++;
            continue;
          }
        } else if (slopeThreshold >= ((compressionSlopeThresholds[precinct.endLayer]) * precinct.wsf)) {
          // Weighted precinct threshold is inferior to resequenced layer
          // threshold, therefore, continue with the next relevant precinct
          index++;

          continue;
        }
        assert (precinct.endLayer <= codestream.getNumLayers());


        if (precinct.numPacketsInClientCache < 0) {
          // Is the first time this precinct is considered

          int lengthOfDataBinSent = (int)serverCache.getPrecinctDataBinLength(precinct.inClassIdentifier);
          int layerOfDataSent = serverCache.getPrecinctDataBinLayers(precinct.inClassIdentifier);
          if ((lengthOfDataBinSent < 0) && (layerOfDataSent < 0)) {
            lengthOfDataBinSent = 0;
            layerOfDataSent = 0;
          } else if ((lengthOfDataBinSent > 0) && (layerOfDataSent < 0)) {
            layerOfDataSent = logicalTarget.getLastCompleteLayer(precinct.inClassIdentifier, lengthOfDataBinSent);
          } else if ((lengthOfDataBinSent < 0) && (layerOfDataSent > 0)) {
            lengthOfDataBinSent = logicalTarget.getPacketOffsetWithDataBin(precinct.inClassIdentifier, layerOfDataSent)
                                  + logicalTarget.getPacketLength(precinct.inClassIdentifier, layerOfDataSent);
          }

          precinct.msgOffset = lengthOfDataBinSent;
          precinct.numPacketsInClientCache = layerOfDataSent;
          precinct.numBytesInClientCache = lengthOfDataBinSent;
          precinct.startLayer = layerOfDataSent;
          precinct.endLayer = layerOfDataSent;
          precinct.actualNumPacket = layerOfDataSent;

          if ((precinct.actualNumPacket >= codestream.getNumLayers())
              || (actualViewWindow.layers <= precinct.endLayer)) {
            // This point should not be reached because thhis condition
            // is checked in the findRelevantPrecinctsResequencedLayers method.
            index++;
            precinct.endLayer++;

            continue;
          }
        }

        assert (precinct.endLayer < codestream.getNumLayers());

        int pendingBytesToDeliver =
                logicalTarget.getPacketOffsetWithDataBin(precinct.inClassIdentifier, precinct.endLayer)
                + logicalTarget.getPacketLength(precinct.inClassIdentifier, precinct.endLayer)
                - (int)(precinct.msgOffset + precinct.msgLength);

        // JPIP message body has to be, at least, 8 bytes
        // However, an exception must be done for the last packet/layer
        if (pendingBytesToDeliver < 8) {
          if ((precinct.endLayer + 1) < codestream.getNumLayers()) {
            precinct.endLayer++;
            continue;
          }
        }

        // When this point is reached, a packet, of a part, must be transmitted.

        // NOTE
        // We could consider another condition, packets which does not have
        // body. These packets have only the header to signal that none of the
        // precinct's blocks have a contribution. In the JPIP context, they
        // contribute to the payload of messages but they do not contribute
        // to the decompressing process.
        // Packet headers must be decoded to read this information.
        // This hypothesis must be studied performing some simulations and
        // analyzing the results.


        // NOTE
        // An estimation of the JPIP message header will offer a more
        // accurate length in the server responses

        // Estimate the length of the JPIP message header
        //int estJPIPMessageLength = JPIPMessageEncoder.estimateLength(
        //       new JPIPMessageHeader(-1, JPIPMessageHeader.EXTENDED_PRECINCT, precinct.inClassIdentifier, precinct.msgOffset, precinct.getBodyLength()+packetLength, lastByte, lastCompleteLayer));
        int estJPIPMessageLength = 0;

        if (responseLength + estJPIPMessageLength >= maximumResponseLength) {
          // Do nothing.
          EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;

          // Sets response length equals to maximum to finish loops
          responseLength = maximumResponseLength;

        } else if (responseLength + estJPIPMessageLength + pendingBytesToDeliver < maximumResponseLength) {
          // The whole packet can be transmitted

          precinct.endLayer++;
          precinct.actualNumPacket = precinct.endLayer;
          precinct.actualNumBytes = pendingBytesToDeliver;
          precinct.msgLength += pendingBytesToDeliver;

          responseLength += pendingBytesToDeliver;

        } else if (responseLength + estJPIPMessageLength + pendingBytesToDeliver >= maximumResponseLength) {
          // Packet must be resized

          // Sets the end of response code
          EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;

          precinct.endLayer++;
          responseLength += estJPIPMessageLength;

          for (int nlayer = precinct.actualNumPacket;
                  (nlayer < precinct.endLayer) && (responseLength < maximumResponseLength);
                  nlayer++) {
            assert (precinct.msgOffset >= 0);
            
            // Sends layer, or a piece, of it
            int packetLength = -1;
            int off = logicalTarget.getPacketOffsetWithDataBin(precinct.inClassIdentifier, nlayer);
            
            if (precinct.msgOffset + precinct.msgLength > off) {
              packetLength =
                      off + logicalTarget.getPacketLength(precinct.inClassIdentifier, nlayer)
                      - (int)(precinct.msgOffset + precinct.msgLength);
            } else {
              packetLength = logicalTarget.getPacketLength(precinct.inClassIdentifier, nlayer);
            }

            assert (packetLength >= 0);

            if (!align && (responseLength + packetLength >= maximumResponseLength)) {
              packetLength = (int)(maximumResponseLength - responseLength);
            }

            responseLength += packetLength;
            precinct.msgLength += packetLength;
            //precinct.actualNumPacket = nlayer;
            precinct.actualNumBytes += packetLength;
          }
          precinct.actualNumPacket = precinct.endLayer;

          // Equals length to the maximum, therefore, finish loops
          responseLength = maximumResponseLength;

        } else {
          assert (true);
        }

      } //while (index <= numRelevantPrecincts)

      if ((index >= numRelevantPrecincts) && (responseLength < maximumResponseLength)) {
        // Update loop conditions
        preemptedLayers++;
        slopeThreshold = nextSlopeThreshold;
        nextSlopeThreshold = Float.NEGATIVE_INFINITY;
      }

    } // while ((threshold != Float.NEGATIVE_INFINITY)


    // Check if end of response code have to be set to byte limit reached
    if (responseLength >= maximumResponseLength) {
      EORReasonCode = EORCodes.BYTE_LIMIT_REACHED;
    }


    // If the maximum response length has not been achieved and there are some
    // precinct's layers which have been discarded because they have less than
    // 8 bytes, they could be included despite the reduction on the efficiency.
		/* if (responseLength < maximumResponseLength) {
     * for (RelevantPrecinct p : relevantPrecincts) {
     * p.list(System.out);
     * if (p.actualNumPacket < p.endLayer) {
     *
     * }
     * }
     * } */
  }
}
