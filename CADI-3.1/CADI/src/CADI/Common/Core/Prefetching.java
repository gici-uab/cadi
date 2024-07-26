/*
 * CADI Software - a JPIP Client/Server framework
 * Copyright (C) 2007-2012 Group on Interactive Coding of Images (GICI)
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Group on Interactive Coding of Images (GICI) Department of Information and
 * Communication Engineering Autonomous University of Barcelona 08193 -
 * Bellaterra - Cerdanyola del Valles (Barcelona) Spain
 *
 * http://gici.uab.es gici-info@deic.uab.es
 */
package CADI.Common.Core;

import CADI.Common.Cache.CacheManagement;
import CADI.Common.Log.CADILog;
import CADI.Common.LogicalTarget.JPEG2000.*;
import CADI.Common.Network.JPIP.ClassIdentifiers;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Common.Session.ClientSideSessionTarget;
import CADI.Common.Util.*;
import GiciException.ErrorException;
import java.io.PrintStream;
import java.util.*;

/**
 * Implements a prefetching strategy. <p> This class implements generic
 * prefetching strategies but it is ready to be inherited by another class
 * providing a more suitable interface.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.2 2011/12/03
 */
public class Prefetching extends Thread {

  /**
   * It is an object that will be used to log processes.
   */
  protected CADILog log = null;

  /**
   * Indicates whether the thread has to finish.
   *
   * @see #finish()
   */
  protected boolean finish = false;

  /**
   *
   */
  protected PrefSemaphore prefSemaphore = null;

  /**
   * Prediction of the future window of interest to be requested is based on a
   * weighted woi considering the historic of windows of interest. <p> Precincts
   * are weighted considering their frequency on the historic windows of
   * interest.
   */
  public static final int WOI_TYPE_WEIGHTED_WOI = 1;

  /**
   * Prediction of the future window of interest to be requested is based on the
   * bounding box of the historic of windows of interest.
   */
  public static final int WOI_TYPE_BOUNDING_BOX = 2;

  /**
   *
   */
  protected int prefetchingWOIType = WOI_TYPE_WEIGHTED_WOI;

  /**
   *
   */
  protected StopWatch stopWatch = null;

  protected boolean recomputeWOI = false;

  // INTERNAL ATTRIBUTES
  /**
   * FIXME: this object has to be one different for each logical target.
   */
  private HashMap<Long, RelevantPrecinct> relevantPrecincts = null;

  /**
   *
   */
  protected Movements movements = null;

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.WindowScalingFactor#compressionSlopeThresholds}.
   */
  protected int[] compressionSlopeThresholds = null;

  protected float[] resequencedSlopeThresholds = null;

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
  protected static final int NUM_SLOPE_THRESHOLDS = 25;

  private PredictiveScalingFactors scalingFactors = null;

  private float maxProbMovements = 1F;

  // ============================= public methods ==============================
  /**
   * This class constructor is not allowed.
   */
  public Prefetching() {
    throw new UnsupportedOperationException();
  }

  /**
   * Constructor.
   *
   * @param threadName definition in {@link java.lang.Thread#name}.
   * @param prefSemaphore definition in {@link #prefSemaphore}.
   * @param log definition in {@link #log}.
   */
  public Prefetching(
          String threadName,
          PrefSemaphore prefSemaphore, CADILog log) {

    this(threadName, prefSemaphore, log, WOI_TYPE_BOUNDING_BOX);
  }

  /**
   * Constructor.
   *
   * @param threadName definition in {@link java.lang.Thread#name}.
   * @param prefSemaphore definition in {@link #prefSemaphore}.
   * @param log definition in {@link #log}.
   * @param prefetchingWOIType definition in {@link #prefetchingWOIType}.
   */
  public Prefetching(
          String threadName,
          PrefSemaphore prefSemaphore, CADILog log,
          int prefetchingWOIType) {

    this(threadName, prefSemaphore, log, prefetchingWOIType, null);
  }

  /**
   * Constructor.
   *
   * @param threadName definition in {@link java.lang.Thread#name}.
   * @param prefSemaphore definition in {@link #prefSemaphore}.
   * @param log definition in {@link #log}.
   * @param prefetchingWOIType definition in {@link #prefetchingWOIType}.
   * @param movementProbabilities definition in {@link #movements}.
   */
  public Prefetching(
          String threadName,
          PrefSemaphore prefSemaphore, CADILog log,
          int prefetchingWOIType,
          float[] movementProbabilities) {

    // Check input parameters
    if (prefSemaphore == null) {
      throw new NullPointerException();
    }
    if (log == null) {
      throw new NullPointerException();
    }

    if (movementProbabilities == null) {
      movementProbabilities = new float[10];
      for (int i = 0; i < 10; i++) {
        movementProbabilities[i] = 0.1F;
      }
    }
    if (movementProbabilities.length != 10) {
      throw new IllegalArgumentException();
    }
    for (float prob : movementProbabilities) {
      if ((prob < 0) || (prob > 1)) {
        throw new IllegalArgumentException();
      }
    }

    // Copy input parameters
    this.prefSemaphore = prefSemaphore;
    this.log = log;
    this.prefetchingWOIType = prefetchingWOIType;

    setName(threadName);

    relevantPrecincts = new HashMap<Long, RelevantPrecinct>();
    movements = new Movements(movementProbabilities);
    resequencedSlopeThresholds =
            WindowScalingFactor.computeResequencedSlopeThresholds(NUM_SLOPE_THRESHOLDS);
  }

  /**
   *
   * @param woisHistory
   * @param sessionTarget
   * @param codestream
   * @param cache
   */
  protected void doPrefetching(ArrayList<ViewWindowField> woisHistory,
                               ClientSideSessionTarget sessionTarget,
                               JPEG2KCodestream codestream,
                               CacheManagement cache) {
    doPrefetching(woisHistory, sessionTarget, codestream, cache, null);
                               
  }

  /**
   *
   * @param woisHistory
   * @param sessionTarget
   * @param codestream
   * @param cache
   * @param scalingFactors
   */
  protected void doPrefetching(ArrayList<ViewWindowField> woisHistory,
                               ClientSideSessionTarget sessionTarget,
                               JPEG2KCodestream codestream,
                               CacheManagement cache,
                               PredictiveScalingFactors scalingFactors) {

    // DEBUG
    //System.out.println("\t# historic=" + woisHistory.size()); // DEBUG
    //System.out.println("\tHistoric"); // DEBUG
    //for (ViewWindowField w : woisHistory) {
    //  System.out.println("\t\t" + w.toString());
    //}
    // END DEBUG

    relevantPrecincts.clear();
    boolean doPrefetch = false;
    if (scalingFactors != null) {
      this.scalingFactors = scalingFactors;
      doPrefetch = computeWeightedWOI(woisHistory, codestream, cache);

    } else if (prefetchingWOIType == WOI_TYPE_BOUNDING_BOX) {
      doPrefetch = computeBoundingBox(woisHistory, codestream, cache);

    } else {
      // Default option: WOI_TYPE_WEIGHTED_WOI
      maxProbMovements = 1; // movements.getMaxProbabilities();
      doPrefetch = computeWeightedWOI(woisHistory, codestream, cache);
    }

    if (doPrefetch) {
      compressionSlopeThresholds = WindowScalingFactor.computeCompressionSlopeThresholds(
              codestream.getCOMParameters(), codestream.getNumLayers());

      fetchRelevantPrecincts(codestream, sessionTarget);
    }

    scalingFactors = null;
  }

  /**
   * Indicates that the prefetching has to be finished.
   */
  public final void finish() {
    finish = true;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";

    str += "]";

    return str;
  }

  /**
   * Prints this Prefetching out to the specified output stream. This
   * method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Prefetching --");
    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * Computes the bounding box of a historic wois. This method is used when the
   * the {@link #prefetchingWOIType} attribute is {@link #WOI_TYPE_BOUNDING_BOX}.
   *
   * @param woisHistory
   * @param codestream
   * @param cache
   *
   * @return
   */
  private boolean computeBoundingBox(ArrayList<ViewWindowField> woisHistory,
                                     JPEG2KCodestream codestream,
                                     CacheManagement cache) {

    ViewWindowField boundingBox = new ViewWindowField();

    // Get the highest frame size
    for (ViewWindowField woi : woisHistory) {
      if ((woi.fsiz[0] < 0) && (woi.fsiz[1] < 0)
              && (woi.roff[0] < 0) && (woi.roff[1] < 0)
              && (woi.rsiz[0] < 0) && (woi.rsiz[1] < 0)) {
        // Maybe the client has requested by headers or metadata
        continue;
      }

      if (boundingBox.fsiz[0] < woi.fsiz[0]) {
        boundingBox.fsiz[0] = woi.fsiz[0];
      }
      if (boundingBox.fsiz[1] < woi.fsiz[1]) {
        boundingBox.fsiz[1] = woi.fsiz[1];
      }
    }

    if ((boundingBox.fsiz[0] <= 0) && (boundingBox.fsiz[1] <= 0)) {
      return false;
    }

    // Adjust frame size to a codestream's frame size
    int discardLevels =
            codestream.determineNumberOfDiscardLevels(boundingBox.fsiz, ViewWindowField.CLOSEST);

    CADIDimension frameSize = codestream.determineFrameSize(discardLevels);

    boundingBox.fsiz[0] = frameSize.width;
    boundingBox.fsiz[1] = frameSize.height;
    boundingBox.roff[0] = -1;
    boundingBox.roff[1] = -1;
    boundingBox.rsiz[0] = -1;
    boundingBox.rsiz[1] = -1;


    // Go on roff and rsiz
    CADIPoint roff = new CADIPoint();
    CADIDimension rsiz = new CADIDimension();
    for (ViewWindowField woi : woisHistory) {
      if ((woi.fsiz[0] < 0) && (woi.fsiz[1] < 0)
              && (woi.roff[0] < 0) && (woi.roff[1] < 0)
              && (woi.rsiz[0] < 0) && (woi.rsiz[1] < 0)) {
        // Maybe the client has requested by headers or metadata
        continue;
      }

      roff.x = (woi.roff[0] >= 0) ? woi.roff[0] : 0;
      roff.y = (woi.roff[1] >= 0) ? woi.roff[1] : 0;
      rsiz.width = (woi.rsiz[0] > 0) ? woi.rsiz[0] : woi.fsiz[0] - woi.roff[0];
      rsiz.height = (woi.rsiz[1] > 0) ? woi.rsiz[1] : woi.fsiz[1] - woi.roff[1];

      if ((woi.fsiz[0] != boundingBox.fsiz[0])
              || (woi.fsiz[1] != boundingBox.fsiz[1])) {

        // Frame sizes do not match.
        // Adjust roff and rsiz to the higher frame size
        double factorWidth = 1.0 * boundingBox.fsiz[0] / woi.fsiz[0];
        double factorHeight = 1.0 * boundingBox.fsiz[1] / woi.fsiz[1];

        rsiz.width = (int) Math.ceil((roff.x + rsiz.width) * factorWidth);
        rsiz.height = (int) Math.ceil((roff.y + rsiz.height) * factorHeight);
        roff.x = (int) Math.floor(roff.x * factorWidth);
        roff.y = (int) Math.floor(roff.y * factorHeight);
        rsiz.width -= roff.x;
        rsiz.height -= roff.y;
      }

      // Adjust bounding box, if necessary
      if (boundingBox.roff[0] < 0) {
        boundingBox.roff[0] = roff.x;
      }
      if (boundingBox.roff[0] > roff.x) {
        rsiz.width += boundingBox.roff[0] - roff.x;
        boundingBox.roff[0] = roff.x;
      }

      if (boundingBox.roff[1] < 0) {
        boundingBox.roff[1] = roff.y;
      }
      if (boundingBox.roff[1] > roff.y) {
        rsiz.height += boundingBox.roff[1] - roff.y;
        boundingBox.roff[1] = roff.y;
      }

      if (boundingBox.rsiz[0] < 0) {
        boundingBox.rsiz[0] = rsiz.width;
      }
      if ((boundingBox.roff[0] + boundingBox.rsiz[0]) < (roff.x + rsiz.width)) {
        boundingBox.rsiz[0] = roff.x + rsiz.width - boundingBox.roff[0];
      }

      if (boundingBox.rsiz[1] < 0) {
        boundingBox.rsiz[1] = rsiz.height;
      }
      if ((boundingBox.roff[1] + boundingBox.rsiz[1]) < (roff.y + rsiz.height)) {
        boundingBox.rsiz[1] = roff.y + rsiz.height - boundingBox.roff[1];
      }

      if (recomputeWOI) {
        return false;
      }
    }


    getPrecinctWeights(boundingBox, 1, codestream, cache);

    return true;

  }

  /**
   *
   * REVIEW: Performs the prefetching using the last woi requested by each client over
   * the image. On each last woi it applies a user navigation model to estimate
   * the potencial woi to be requested by a client. Thus, each precinct is
   * weighted by a scaling factor depending on the direction of the movement and
   * the number of clients.
   *
   * @param wois
   * @param codestream
   * @param cache
   *
   * @return
   */
  private boolean computeWeightedWOI(ArrayList<ViewWindowField> wois,
                                     JPEG2KCodestream codestream,
                                     CacheManagement cache) {

    int numRevelantWOIs = 0;

    for (ViewWindowField woi : wois) {

      if ((woi.fsiz[0] < 0) && (woi.fsiz[1] < 0)
              && (woi.roff[0] < 0) && (woi.roff[1] < 0)
              && (woi.rsiz[0] < 0) && (woi.rsiz[1] < 0)) {
        // Maybe the client has requested for headers or metadata

        if (wois.size() == 1) {
          return false;
        }

        continue;
      }

      numRevelantWOIs++;

      if (woi.fsiz[0] > codestream.getXSize()) {
        woi.fsiz[0] = codestream.getXSize();
      }
      if (woi.fsiz[1] > codestream.getYSize()) {
        woi.fsiz[1] = codestream.getYSize();
      }

      int discardLevels =
              codestream.determineNumberOfDiscardLevels(woi.fsiz, woi.roundDirection);

      ViewWindowField tmpWOI = new ViewWindowField(woi);

      // Up movement
      if (woi.roff[1] > 0) {
        tmpWOI.roff[1] = woi.roff[1] - woi.rsiz[1];
        getPrecinctWeights(tmpWOI, movements.getUpProbability() / maxProbMovements, codestream, cache);
      }

      // Down
      if ((woi.roff[1] + woi.rsiz[1]) < woi.fsiz[1]) {
        tmpWOI.roff[1] = woi.roff[1] + woi.rsiz[1];
        getPrecinctWeights(tmpWOI, movements.getDownProbability() / maxProbMovements, codestream, cache);
      }

      tmpWOI.roff[1] = woi.roff[1];

      // Left
      if (woi.roff[0] > 0) {
        tmpWOI.roff[0] = woi.roff[0] - woi.rsiz[0];
        getPrecinctWeights(tmpWOI, movements.getLeftProbability() / maxProbMovements, codestream, cache);
      }

      // Right
      if ((woi.roff[0] + woi.rsiz[0]) < woi.fsiz[0]) {
        tmpWOI.roff[0] = woi.roff[0] + woi.rsiz[0];
        getPrecinctWeights(tmpWOI, movements.getRightProbability() / maxProbMovements, codestream, cache);
      }

      tmpWOI.roff[0] = woi.roff[0];

      // Up-left
      if ((woi.roff[1] > 0) && (woi.roff[0] > 0)) {
        tmpWOI.roff[0] = woi.roff[0] - woi.rsiz[0];
        tmpWOI.roff[1] = woi.roff[1] - woi.rsiz[1];
        getPrecinctWeights(tmpWOI, movements.getUpLeftProbability() / maxProbMovements, codestream, cache);
      }

      // Up-right
      if ((woi.roff[1] > 0) && ((woi.roff[0] + woi.rsiz[0]) < woi.fsiz[0])) {
        tmpWOI.roff[0] = woi.roff[0] + woi.rsiz[0];
        tmpWOI.roff[1] = woi.roff[1] - woi.rsiz[1];
        getPrecinctWeights(tmpWOI, movements.getUpRightProbability() / maxProbMovements, codestream, cache);
      }

      // Down-left
      if ((woi.roff[0] > 0) && ((woi.roff[1] + woi.rsiz[1]) < woi.fsiz[1])) {
        tmpWOI.roff[0] = woi.roff[0] - woi.rsiz[0];
        tmpWOI.roff[1] = woi.roff[1] + woi.rsiz[1];
        getPrecinctWeights(tmpWOI, movements.getDownLeftProbability() / maxProbMovements, codestream, cache);
      }

      // Down-right
      if (((woi.roff[0] + woi.rsiz[0]) < woi.fsiz[0])
              && ((woi.roff[1] + woi.rsiz[1]) < woi.fsiz[1])) {
        tmpWOI.roff[0] = woi.roff[0] + woi.rsiz[0];
        tmpWOI.roff[1] = woi.roff[1] + woi.rsiz[1];
        getPrecinctWeights(tmpWOI, movements.getDownRightProbability() / maxProbMovements, codestream, cache);
      }

      // Zoom-in
      if (discardLevels > 0) {
        CADIDimension fsiz = codestream.determineFrameSize(discardLevels - 1);
        tmpWOI.fsiz[0] = fsiz.width; //woi.fsiz[0] << 1;
        tmpWOI.fsiz[1] = fsiz.height; //woi.fsiz[1] << 1;
        tmpWOI.rsiz[0] = woi.rsiz[0];
        tmpWOI.rsiz[1] = woi.rsiz[1];
        tmpWOI.roff[0] = (int) ((woi.roff[0] + woi.rsiz[0] / 2.0) * 2 - tmpWOI.rsiz[0] / 2.0);
        tmpWOI.roff[1] = (int) ((woi.roff[1] + woi.rsiz[1] / 2.0) * 2 - tmpWOI.rsiz[1] / 2.0);
        getPrecinctWeights(tmpWOI, movements.getZoomInProbability() / maxProbMovements, codestream, cache);
      } else if (discardLevels == 0) {
        tmpWOI.fsiz[0] = woi.fsiz[0];
        tmpWOI.fsiz[1] = woi.fsiz[1];
        tmpWOI.rsiz[0] = woi.rsiz[0];
        tmpWOI.rsiz[1] = woi.rsiz[1];
        tmpWOI.roff[0] = woi.roff[0];
        tmpWOI.roff[1] = woi.roff[1];
        getPrecinctWeights(tmpWOI, movements.getZoomInProbability() / maxProbMovements, codestream, cache);
      }

      // Zoom-out
      if (discardLevels < codestream.getMaxResolutionLevels()) {
        CADIDimension fsiz = codestream.determineFrameSize(discardLevels + 1);
        tmpWOI.fsiz[0] = fsiz.width; //woi.fsiz[0] >> 1;
        tmpWOI.fsiz[1] = fsiz.height; //woi.fsiz[1] >> 1;
        tmpWOI.rsiz[0] = woi.rsiz[0];
        tmpWOI.rsiz[1] = woi.rsiz[1];
        tmpWOI.roff[0] = (int) ((woi.roff[0] + woi.rsiz[0] / 2.0) / 2 - tmpWOI.rsiz[0] / 2.0);
        tmpWOI.roff[1] = (int) ((woi.roff[1] + woi.rsiz[1] / 2.0) / 2 - tmpWOI.rsiz[1] / 2.0);
        getPrecinctWeights(tmpWOI, movements.getZoomOutProbability() / maxProbMovements, codestream, cache);
      }

      if (recomputeWOI) {
        return false;
      }
    }

    // DEBUG
    /* System.out.println("--- WEIGHTED PRECINCTS ---");
     * for (Map.Entry<Long, RelevantPrecinct> entry : relevantPrecincts.entrySet()) {
     * System.out.println("\tprecinct: " + entry.getKey()
     * + " rLevel=" + entry.getValue().rLevel
     * + " precinct=" + entry.getValue().precinct
     * + " wsf: " + entry.getValue().wsf
     * + " startLayer: "+ entry.getValue().startLayer
     * + " lastLayer: "+ entry.getValue().endLayer
     * );
     * } */
    // END DEBUG

    return true;
  }

  /**
   *
   * @param codestream
   * @param sessionTarget
   */
  private void fetchRelevantPrecincts(JPEG2KCodestream codestream,
                                      ClientSideSessionTarget sessionTarget) {

    float slopeThreshold = Float.POSITIVE_INFINITY;
    float nextSlopeThreshold = Float.NEGATIVE_INFINITY;
    int index = 0;
    int maxLayers = codestream.getNumLayers();
    RelevantPrecinct precinct = null;
    JPEG2KTile tileObj = null;
    JPEG2KComponent compObj = null;
    JPEG2KResolutionLevel rLevelObj = null;
    JPEG2KPrecinct precinctObj = null;
    CADIRectangle precinctBounds = null;
    CADIDimension frameSize = null;

    int preemptedLayers = 0; // See method comments
    final int MAX_PREEMPTED_LAYERS = 0; // See method comments

    ViewWindowField precinctWOI;


    while ((slopeThreshold != Float.NEGATIVE_INFINITY) && !recomputeWOI) {
      index = 0;

      for (long inClassIdentifier : relevantPrecincts.keySet()) {
        index++;
        precinct = relevantPrecincts.get(inClassIdentifier);
        //System.out.println("\nPrecinct: " + precinct.inClassIdentifier); // DEBUG

        if (precinct.actualNumPacket >= maxLayers) {
          // All packets/layers of this precinct has been delivered,
          // continue with the next precinct.
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
          // if their weighted threshold are considered
          if (precinct.endLayer >= MAX_PREEMPTED_LAYERS) {
            // End layer greater than preempted layers
            index++;
            continue;
          }
        } else if (slopeThreshold >= ((compressionSlopeThresholds[precinct.endLayer]) * precinct.wsf)) {
          // Weighted precinct threshold is inferior to resequenced layer
          // threshold, therefore, continue with the next relevant precinct
          index++;
          continue;
        }
        precinct.endLayer++;
        assert (precinct.endLayer <= maxLayers);

        tileObj = codestream.getTile(precinct.tile);
        compObj = tileObj.getComponent(precinct.component);
        rLevelObj = compObj.getResolutionLevel(precinct.rLevel);
        precinctObj = rLevelObj.getPrecinct(precinct.precinct);
        precinctBounds = precinctObj.getBounds();
        frameSize = rLevelObj.getSize();

        if (!sessionTarget.isAvailable(precinct.inClassIdentifier, precinct.endLayer)) {
          // Precinct not available in cache

          precinctWOI = new ViewWindowField();
          precinctWOI.fsiz[0] = frameSize.width;
          precinctWOI.fsiz[1] = frameSize.height;
          precinctWOI.roff[0] = precinctBounds.x;
          precinctWOI.roff[1] = precinctBounds.y;
          precinctWOI.rsiz[0] = precinctBounds.width;
          precinctWOI.rsiz[1] = precinctBounds.height;
          precinctWOI.comps = new int[1][2];
          precinctWOI.comps[0][0] = 0; // compObj.getComponent();
          precinctWOI.comps[0][1] = codestream.getZSize() - 1; //compObj.getComponent();
          precinctWOI.layers = precinct.endLayer;

          //if (!sessionTarget.isAvailable(precinctWOI)) {
          // WOI not availabe in cache

          prefSemaphore.await();
          recomputeWOI = prefSemaphore.changes();
          if (recomputeWOI) {
            break;
          }

          // When this point is reached, woi must be fetch
          try {
            sessionTarget.lock();
            sessionTarget.resetJPIPMessagesCounters(); // Necessary? I dont think so
            //System.out.println("PREFETCHING: precinct=" + precinctObj.getInClassIdentifier() + " => " + precinctWOI.toString()); System.out.flush(); // DEBUG
            sessionTarget.setDebug("PREF_" + UUID.randomUUID().toString()); // DEBUG
            sessionTarget.fetchWindow(precinctWOI, false);
            //System.out.println("\theaders=" + sessionTarget.getBytesJPIPMessageHeader() + " body=" + sessionTarget.getBytesJPIPMessageBody());  //System.out.flush(); // DEBUG
          } catch (ErrorException e1) {
            //e1.printStackTrace();
            //continue;
          } finally {
            sessionTarget.unlock();
            // prefSemaphore.unlock();
          }
        }

        precinct.actualNumPacket = precinct.endLayer;

        recomputeWOI = prefSemaphore.changes();
        if (recomputeWOI) {
          break;
        }
      }

      // Update loop conditions
      preemptedLayers++;
      slopeThreshold = nextSlopeThreshold;
      nextSlopeThreshold = Float.NEGATIVE_INFINITY;
    }
  }

  /**
   *
   *
   * @param viewWindowField
   * @param weight
   * @param codestream
   * @param cache
   */
  private void getPrecinctWeights(ViewWindowField viewWindowField,
                                  float weight,
                                  JPEG2KCodestream codestream,
                                  CacheManagement cache) {

    ViewWindowField woi = new ViewWindowField(viewWindowField);

    assert (woi.fsiz[0] > 0);
    assert (woi.fsiz[1] > 0);

    // Check if woi is beyond frame size. If so, precincts must be weighted.
    float woiFactor = 1;
    if ((woi.roff[0] < 0) || (woi.roff[1] < 0)
            || (woi.roff[0] + woi.rsiz[0] > woi.fsiz[0])
            || (woi.roff[0] + woi.rsiz[0] > woi.fsiz[0])) {
      int xStart = Math.max(woi.roff[0], 0);
      int yStart = Math.max(woi.roff[1], 0);
      int xEnd = Math.min(woi.roff[0] + woi.rsiz[0], woi.fsiz[0]);
      int yEnd = Math.min(woi.roff[1] + woi.rsiz[1], woi.fsiz[1]);
      woiFactor = 1.0F * (xEnd - xStart) * (yEnd - yStart) / (woi.rsiz[0] * woi.rsiz[1]);
      if (woi.roff[0] + woi.rsiz[0] > woi.fsiz[0]) {
        woi.rsiz[0] = woi.fsiz[0] - woi.roff[0];
      }
      if (woi.roff[1] + woi.rsiz[1] > woi.fsiz[1]) {
        woi.rsiz[1] = woi.fsiz[1] - woi.roff[1];
      }
      if (woi.roff[0] < 0) {
        woi.rsiz[0] += woi.roff[0];
        woi.roff[0] = 0;
      }
      if (woi.roff[1] < 0) {
        woi.rsiz[1] += woi.roff[1];
        woi.roff[1] = 0;
      }

    }

    // Set missing parameters
    if (woi.comps == null) {
      woi.comps = new int[1][2];
      woi.comps[0][0] = 0;
      woi.comps[0][1] = codestream.getZSize() - 1;
    }
    if (woi.layers < 0) {
      woi.layers = codestream.getNumLayers();
    }

    if ((woi.roff[0] >= woi.fsiz[0]) || (woi.roff[1] >= woi.fsiz[1])) {
      assert (true);
    }

    // Get the number of discard levels
    int discardLevels =
            codestream.determineNumberOfDiscardLevels(woi.fsiz, woi.roundDirection);

    // Fit requested region to the suitable resolution
    codestream.mapRegionToSuitableResolutionGrid(woi.fsiz, woi.roff, woi.rsiz,
            discardLevels);

    assert (discardLevels >= 0);
    assert (woi != null);

    long inClassIdentifier = -1;
    RelevantPrecinct relevantPrecinct = null;
    JPEG2KTile tileObj = null;
    JPEG2KComponent compObj = null;
    JPEG2KResolutionLevel rLevelObj = null;

    int[] components = ArraysUtil.rangesToIndexes(woi.comps);
    ArrayList<Integer> relevantTiles = codestream.calculateRelevantTiles(woi, discardLevels);

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

    // Loop on tiles
    for (int tileIndex : relevantTiles) {
      if (recomputeWOI) {
        break;
      }
      tileObj = codestream.getTile(tileIndex);

      // Loop on res. levels
      for (int rLevel = 0; rLevel <= maxRLevel && !recomputeWOI; rLevel++) {

        // Loop on components
        for (int component : components) {
          if (recomputeWOI) {
            break;
          }

          compObj = tileObj.getComponent(component);
          rLevelObj = compObj.getResolutionLevel(rLevel);

          int maxRLevelsComponent = compObj.getWTLevels();
          if (maxRLevelsComponent >= discardLevels) {
            // Discard the r highest res. levels
            //if (rLevel != maxRLevelsComponent - discardLevels) {
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


          CADIDimension frameSize =
                  codestream.determineFrameSize(maxRLevelsComponent - rLevel);

          // Translation of requested region with from the desired frame size reference grid
          // into the sub-sampled reference grid at this frame size
          CADIRectangle supportRegion =
                  new CADIRectangle(woi.roff[0], woi.roff[1], woi.rsiz[0],
                  woi.rsiz[1]);
          codestream.calculateSupportRegion(tileIndex, component, rLevel,
                  supportRegion, discardLevels);
          //System.out.println("SUPPORT REGION="+supportRegion.toString());

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
          startXPrecinct = (int) (Math.floor(1.0 * supportRegion.x / precinctWidth));
          startYPrecinct = (int) (Math.floor(1.0 * supportRegion.y / precinctHeight));
          endXPrecinct = (int) (Math.ceil(1.0 * (supportRegion.x + supportRegion.width) / precinctWidth));	// not included
          endYPrecinct = (int) (Math.ceil(1.0 * (supportRegion.y + supportRegion.height) / precinctHeight));	// not included

          for (int yPrecinct = startYPrecinct; yPrecinct < endYPrecinct && !recomputeWOI; yPrecinct++) {
            for (int xPrecinct = startXPrecinct; xPrecinct < endXPrecinct && !recomputeWOI; xPrecinct++) {

              inClassIdentifier = rLevelObj.getInClassIdentifier(yPrecinct * numPrecinctsWidth + xPrecinct);

              if (cache.isComplete(ClassIdentifiers.PRECINCT, inClassIdentifier)) {
                continue;
              }

              if (!relevantPrecincts.containsKey(inClassIdentifier)) {
                // First time precinct is considered
                RelevantPrecinct rp = new RelevantPrecinct(inClassIdentifier);
                rp.tile = tileIndex;
                rp.component = component;
                rp.rLevel = rLevel;
                rp.precinct = yPrecinct * numPrecinctsWidth + xPrecinct;

                int lengthOfDataBinSent = (int) cache.getPrecinctDataBinLength(rp.inClassIdentifier);
                int layerOfDataSent = cache.getLastLayerOfPrecinctDataBin(rp.inClassIdentifier);
                if (woi.layers > layerOfDataSent) {
                  layerOfDataSent = woi.layers;
                }
                rp.msgOffset = lengthOfDataBinSent;
                rp.numPacketsInClientCache = layerOfDataSent;
                rp.numBytesInClientCache = lengthOfDataBinSent;
                rp.startLayer = layerOfDataSent;
                rp.endLayer = rp.startLayer;
                rp.actualNumPacket = rp.startLayer;
                rp.wsf = 0;

                relevantPrecincts.put(inClassIdentifier, rp);
              }

              CADIRectangle precinct =
                      new CADIRectangle(xPrecinct * precinctWidth,
                      yPrecinct * precinctHeight, precinctWidth, precinctHeight);
              long areaPrecinct = precinct.getArea();
              precinct.intersection(supportRegion.x, supportRegion.y, supportRegion.width, supportRegion.height);
              float overlapFactor = precinct.getArea() * 1.0F / areaPrecinct;

              relevantPrecinct = relevantPrecincts.get(inClassIdentifier);
              if (relevantPrecinct.endLayer < woi.layers) {
                relevantPrecinct.numBytesInClientCache = woi.layers;
                relevantPrecinct.startLayer = woi.layers;
                relevantPrecinct.actualNumPacket = woi.layers;
                relevantPrecinct.endLayer = woi.layers;
              }

              //relevantPrecinct.wsf += (1 - (getPenalty(overlapFactor * woiFactor))) * weight;
              if (scalingFactors == null) {
                float overlapPenalty = getPenalty(overlapFactor * woiFactor);
                relevantPrecinct.wsf += (1 - overlapPenalty) * weight;
              } else {
                float overlapPenalty = getPenalty(overlapFactor * woiFactor);
                float psfPenalty = getPenalty(scalingFactors.getValue(inClassIdentifier));
                relevantPrecinct.wsf += (1 - overlapPenalty - psfPenalty);
                //float overlapPenalty = getPenalty(overlapFactor * woiFactor * scalingFactors.getValue(inClassIdentifier));
                //relevantPrecinct.wsf += (1-overlapPenalty);
              }

              recomputeWOI = prefSemaphore.changes();
            }
          }
        }
      }
    }
  }

  /**
   *
   * @param scalingFactor
   *
   * @return
   */
  private float getPenalty(float scalingFactor) {
    float wsfPenalty = 0;
    for (int i = 0; i < resequencedSlopeThresholds.length; i++) {
      wsfPenalty = (i == 0) ? 0 : i * 0.001F;
      if (scalingFactor >= resequencedSlopeThresholds[i]) {
        break;
      }
    }
    return wsfPenalty;
  }
}