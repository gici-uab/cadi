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

import CADI.Common.LogicalTarget.JPEG2000.Decode.BlockDecode;
import CADI.Common.LogicalTarget.JPEG2000.Decode.BlockDecodeState;
import CADI.Common.LogicalTarget.JPEG2000.Detransform.Dequantization;
import CADI.Common.LogicalTarget.JPEG2000.Detransform.ImageBuild;
import CADI.Common.LogicalTarget.JPEG2000.Detransform.RangeRecovery;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters;
import CADI.Common.Network.JPIP.ViewWindowField;
import GiciException.ErrorException;
import GiciTransform.InverseWaveletTransform;
import GiciTransform.LevelUnshift;
import GiciTransform.RangeCheck;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * This class implements the JPEG2000 decoder engine.
 * <p>
 * If this class has to be called to decode several WOIs of the same image,
 * this class is ready to be called without destroing and creating a new
 * object. It is more efficient to create the object and call necessary times
 * to the {@link #decode(ViewWindowField, Hashtable, Hashtable)} method. In
 * this case, the three-dimensional array returned by the
 * {@link #decode(ViewWindowField, Hashtable, Hashtable)} method with the
 * decompresed WOI cannot be destroied because it will be re-used in the
 * next call.
 * <p>
 * Usage example:<br>
 * &nbsp; constructor<br>
 * &nbsp; decode<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.3.3 2010/02/11
 */
public class JPEG2KDecoder {

  /**
   *
   */
  private ClientJPEG2KCodestream codestream = null;

  private ClientJPEG2KTile tileObj = null;

  /**
   * Definition in {@link CADI.Client.ClientLogicalTarget.JPEG2000.JPEG2KLogicalTarget#viewWindow}.
   */
  private ViewWindowField viewWindow = null;

  /**
   * Number of threads to be used in the decoding process.
   * <p>
   * Only positive values are allowed.
   * <p>
   * A negative value means the number of threads to be used is the number
   * core processors.
   */
  private int numThreads = -1;

  // INTERNAL ATTRIBUTES
  private BlockDecodeState bdState = null;

  private ImageBuild imageBuild = null;

  private Dequantization dequantization = null;

  private InverseWaveletTransform iwt = null;

  private RangeRecovery rangeRecovery = null;

  private LevelUnshift lu = null;

  private RangeCheck rc = null;

  /**
   * Range type to apply.
   * <p>
   * Valid values are:
   * <ul>
   * 	<li> 0 - No range check
   *  <li> 1 - Solve out of range errors setting sample to max or min
   *  <li> 2 - Throw exception when an out of range is detected
   * </ul>
   */
  private static final int RC_TYPE = 1;

  /**
   * Is an one-dimensional array with the component indexes which belong to
   * the WOI.
   */
  private int[] componentIndexes = null;

  /**
   * Temporal structure.
   */
  private float[][][] imageSamplesFloat = null;

  /**
   * Used for verbose information (time for stage).
   * <p>
   * 0 is initial time.
   */
  private long initStageTime = 0;

  /**
   * Used for verbose information (total time).
   * <p>
   * 0 is initial time.
   */
  private long initTime = 0;

  // ============================= public methods ==============================
  /**
   * Constructor.
   * 
   * @param codestream 
   */
  public JPEG2KDecoder(ClientJPEG2KCodestream codestream) {
    this(codestream, -1);
  }

  /**
   * Constructor.
   * 
   * @param codestream definition in {@link #codestream}.
   * @param numThreads definition in {@link #numThreads}.
   */
  public JPEG2KDecoder(ClientJPEG2KCodestream codestream, int numThreads) {
    // Check and copy input parameters
    if (codestream == null) {
      throw new NullPointerException();
    }
    this.codestream = codestream;
    this.numThreads = numThreads;

    this.tileObj = codestream.getTile(0);
    if (this.numThreads <= 0) {
      this.numThreads = Runtime.getRuntime().availableProcessors();
    }

    bdState = new BlockDecodeState(codestream);
    imageBuild = new ImageBuild(codestream);
    dequantization = new Dequantization(codestream);
    rangeRecovery = new RangeRecovery();
  }

  /**
   * Decodes the <code>viewWindow</code> Windof of Interest.
   * 
   * @param viewWindow definition in {@link  #viewWindow}.
   * @param precinctStreams definition in {@link #precinctStreams}.
   * @return
   * @throws ErrorException when a error has been found in an stage of the
   * 			JPEG2000 decoding process.
   */
  public float[][][] decode(ViewWindowField viewWindow) throws ErrorException {

    // Check input parameters
    if (viewWindow == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.viewWindow = viewWindow;

    // Get components as an array
    if (viewWindow.comps != null) {
      int numComps = 0;
      for (int i = 0; i < viewWindow.comps.length; i++) {
        numComps += viewWindow.comps[i][1] - viewWindow.comps[i][0] + 1;
      }
      componentIndexes = new int[numComps];

      int indexComps = 0;
      for (int i = 0; i < viewWindow.comps.length; i++) {
        for (int comp = viewWindow.comps[i][0]; comp <= viewWindow.comps[i][1]; comp++) {
          componentIndexes[indexComps++] = comp;
        }
      }
    } else {
      int zSize = codestream.getZSize();
      viewWindow.comps = new int[1][2];
      viewWindow.comps[0][0] = 0;
      viewWindow.comps[0][1] = zSize - 1;
      componentIndexes = new int[zSize];
      for (int i = 0; i < zSize; i++) {
        componentIndexes[i] = i;
      }
    }

    //setParameters(viewWindow, precinctStreams, zeroBitPlanes);
    return run();
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
   * Prints this JPEG2K Decoder fields out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- JPEG2K Decoder --");
    out.println("Not implemented yet");
    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * Performs the WOI decoding.
   *
   * @return definition in {@link #imageSamplesFloat}.
   *
   * @throws ErrorException when an error has been occurred in an stage of
   * 			 the JPEG2000 decoding process.
   */
  private float[][][] run() throws ErrorException {

    int discardLevels = codestream.determineNumberOfDiscardLevels(viewWindow.fsiz, viewWindow.roundDirection);

    // If image has multi-component transform, get necessary components to invert it.
    int numComps = codestream.getZSize();
    int[] relevantComponents = codestream.getRelevantComponents(viewWindow.comps);
    if (relevantComponents == null) {
      relevantComponents = new int[numComps];
      for (int z = 0; z < numComps; z++) {
        relevantComponents[z] = z;
      }
    }
    //System.out.print("Relev. comps: "); for(int i=0; i<relevantComponents.length; i++) System.out.print(relevantComponents[i]+" "); System.out.println(); // DEBUG

    //BLOCK DECODE
    float[][][][][][][] imageBlocks = null;
    bdState.initialize(viewWindow, relevantComponents, discardLevels);
    BlockDecode[] bd = new BlockDecode[numThreads];
    for (int thread = 0; thread < numThreads; thread++) {
      bd[thread] = new BlockDecode(bdState);
    }
    Thread[] threads = new Thread[numThreads];
    for (int thread = 0; thread < numThreads; thread++) {
      threads[thread] = new Thread(bd[thread]);
      threads[thread].start();
    }
    for (int thread = 0; thread < numThreads; thread++) {
      try {
        threads[thread].join();
      } catch (InterruptedException e) {
        throw new ErrorException(e.getMessage());
      }
    }
    if (bdState.isError()) {
      throw new ErrorException("Unknown error in the block decode thread.");
    }
    imageBlocks = bdState.getImageBlocks();
    for (int thread = 0; thread < numThreads; thread++) {
      bd[thread] = null;
      threads[thread] = null;
    }
    //bdState = null;
    threads = null;
    bd = null;


    //IMAGE BUILD
    imageBuild.run(imageBlocks, imageSamplesFloat, viewWindow, relevantComponents);
    imageSamplesFloat = imageBuild.getImageSamples();
    //ib.list(System.out);
    imageBlocks = null;

    //DEQUANTIZATION
    dequantization.run(imageSamplesFloat, discardLevels, relevantComponents);
    //showDequantizedImage(); // DEBUG

    //DISCRETE WAVELET DETRANSFORM
    iwt = new InverseWaveletTransform(imageSamplesFloat);
    int[] newWT = new int[codestream.getZSize()];
    for (int z = 0; z < newWT.length; z++) {
      newWT[z] = this.tileObj.getComponent(z).getWTLevels() - discardLevels;
    }
    int[] WTTypes = new int[codestream.getZSize()];
    for (int z = 0; z < newWT.length; z++) {
      WTTypes[z] = this.tileObj.getComponent(z).getWTType();
    }
    iwt.setParameters(WTTypes, newWT);
    try {
      imageSamplesFloat = iwt.run();
    } catch (Exception e) {
      throw new ErrorException();
    }
    iwt = null;

    // MULTI-COMPONENT DETRASNFORM
    if (codestream.isMultiComponentTransform()) {
      JPKParameters jpkParameters = codestream.getJPKParameters();
      if ((jpkParameters == null) || (jpkParameters.WT3D == 0)) {
        // multi-component transform is indicated by means of the jpeg2000 compliant form
        MultiComponentDetransform mcdt =
                new MultiComponentDetransform(codestream, imageSamplesFloat,
                codestream.getMCTParameters(),
                codestream.getMCCParameters(),
                codestream.getMCOParameters(),
                relevantComponents, componentIndexes);
        imageSamplesFloat = mcdt.run();
        mcdt = null;
      } else if ((codestream.getRSize() & 0x0100) > 0) {
        // Multiple component transformation is supported through JPK main headers (JPEG2000 uncompliant form)
        MultiComponentDetransform mcdt =
                new MultiComponentDetransform(codestream, imageSamplesFloat,
                codestream.getJPKParameters(),
                relevantComponents, componentIndexes);
        imageSamplesFloat = mcdt.run();
        mcdt = null;
      }
    }

    //RANGE RECOVERY
    int[] precision = codestream.getPrecision(relevantComponents);
    float[] RMMultValues = codestream.getRMMultValues(relevantComponents);
    rangeRecovery.run(imageSamplesFloat, precision, RMMultValues);
    precision = rangeRecovery.getPrecision();


    //LEVEL UNSHIFTING
    int LSType = 1;
    boolean[] LSComponents = codestream.getLSComponents(relevantComponents);
    int[] LSSubsValues = new int[relevantComponents.length];
    Arrays.fill(LSSubsValues, 0);
    if (codestream.getJPKParameters() != null) {
      LSType = codestream.getJPKParameters().LSType;
      for (int zIdx = 0; zIdx < relevantComponents.length; zIdx++) {
        LSSubsValues[zIdx] =
                codestream.getJPKParameters().LSSubsValues[relevantComponents[zIdx]];
      }
    }
    lu = new LevelUnshift(imageSamplesFloat);
    lu.setParameters(LSType, LSComponents, LSSubsValues, precision);
    imageSamplesFloat = lu.run();
    lu = null;

    //RANGE CHECK
    rc = new RangeCheck(imageSamplesFloat);
    rc.setParameters(RC_TYPE, codestream.isSigned(relevantComponents), precision);
    imageSamplesFloat = rc.run();
    rc = null;

    return imageSamplesFloat;
  }

  /**
   * Show some time and memory usage statistics.
   *
   * @param stage string that will be displayed
   */
  private void showTimeMemory(String stage) {

    long actualTime = System.currentTimeMillis();
    if (initTime == 0) {
      initTime = actualTime;
    }

    // print times are not considered
    long totalMemory = Runtime.getRuntime().totalMemory() / 1048576;
    long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
    // ((float) initTime - (float) actualTime)
    String durationStage = Float.toString((actualTime - initStageTime) / 1000F) + "000";
    durationStage = durationStage.substring(0, durationStage.lastIndexOf(".") + 4);
    String duration = Float.toString((actualTime - initTime) / 1000F) + "000";
    duration = duration.substring(0, duration.lastIndexOf(".") + 4);

    System.out.println("\nSTAGE: " + stage);
    System.out.println("  Memory (USED/TOTAL): " + usedMemory + "/" + totalMemory + " MB");
    System.out.println("  Time (USED/TOTAL)  : " + durationStage + "/" + duration + " secs");

    initStageTime = System.currentTimeMillis();

  }

  /**
   *
   *
   */
  private void showDequantizedImage() {
    System.out.println("-------- IMAGE DEQUANTIZED -----------");
    System.out.println("imageSamplesFloat");
    System.out.println("zSize: " + imageSamplesFloat.length);
    System.out.println("rLevel: " + imageSamplesFloat[0].length);
    System.out.println("subband: " + imageSamplesFloat[0][0].length);
    for (int z = 0; z < imageSamplesFloat.length; z++) {
      for (int y = 0; y < imageSamplesFloat[z].length; y++) {
        for (int x = 0; x < imageSamplesFloat[z][y].length; x++) {
          System.out.print(imageSamplesFloat[z][y][x] + " ");
        }
        System.out.println();
      }
    }
    System.out.println("-----------------------------------");
  }

  /**
   * For debugging purposes.
   */
  private void showDetransformedImage() {
    System.out.println("-------- IMAGE DETRANSFORMED -----------");
    System.out.println("imageSamplesFloat");
    System.out.println("zSize: " + imageSamplesFloat.length);
    System.out.println("rLevel: " + imageSamplesFloat[0].length);
    System.out.println("subband: " + imageSamplesFloat[0][0].length);
    for (int z = 0; z < imageSamplesFloat.length; z++) {
      for (int y = 0; y < imageSamplesFloat[z].length; y++) {
        for (int x = 0; x < imageSamplesFloat[z][y].length; x++) {
          System.out.print(imageSamplesFloat[z][y][x] + " ");
        }
        System.out.println();
      }
    }
    System.out.println("-----------------------------------");
  }

  /**
   * For debugging purposes.
   */
  private void showDecodedImage() {
    System.out.println("-------- DECODED IMAGE -----------");
    System.out.println("imageSamplesFloat");
    System.out.println("zSize: " + imageSamplesFloat.length);
    System.out.println("ySize: " + imageSamplesFloat[0].length);
    System.out.println("xSize: " + imageSamplesFloat[0][0].length);
    for (int zIndex = 0; zIndex < imageSamplesFloat.length; zIndex++) {
      System.out.println("Component: " + componentIndexes[zIndex]);
      for (int y = 0; y < imageSamplesFloat[zIndex].length; y++) {
        for (int x = 0; x < imageSamplesFloat[zIndex][y].length; x++) {
          System.out.print(imageSamplesFloat[zIndex][y][x] + " ");
        }
        System.out.println();
      }
      System.out.println("\n");
    }
    System.out.println("-----------------------------------");
  }
}
