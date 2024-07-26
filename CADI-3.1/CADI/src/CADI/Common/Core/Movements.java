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
package CADI.Common.Core;

import java.io.PrintStream;

/**
 * This class defines the probability and distance of movements.
 * <p>
 * The distance of movements is in pixels on the image's highest frame size.
 * <p>
 * 
 * See {@link CADI.Common.Core.Prefetching} class.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.3 20011/11/25
 */
public class Movements {

  // Movements
  public static final int RIGHT = 0;

  public static final int UP_RIGHT = 1;

  public static final int UP = 2;

  public static final int UP_LEFT = 3;

  public static final int LEFT = 4;

  public static final int DOWN_LEFT = 5;

  public static final int DOWN = 6;

  public static final int DOWN_RIGHT = 7;

  public static final int ZOOM_IN = 8;

  public static final int ZOOM_OUT = 9;

  // Probabilities
  private float upProbability = 0.1F;

  private float downProbability = 0.1F;

  private float leftProbability = 0.1F;

  private float rightProbability = 0.1F;

  private float upLeftProbability = 0.1F;

  private float uprightProbability = 0.1F;

  private float downLeftProbability = 0.1F;

  private float downRightProbability = 0.1F;

  private float zoomInProbability = 0.1F;

  private float zoomOutProbability = 0.1F;

  // ============================= public methods ==============================
  /**
   * Default constructor.
   * <p>
   * Probabilities are all set to 0.1 and distances to 128 pixels.
   */
  public Movements() {
  }

  /**
   * Constructor.
   * <p>
   * @param probabilities
   */
  public Movements(float[] probabilities) {
    // Check input parameters
    if (probabilities == null) {
      throw new NullPointerException();
    }
    if (probabilities.length != 10) {
      throw new IllegalArgumentException();
    }
    for (float prob : probabilities) {
      if ((prob < 0) || (prob > 1)) {
        throw new IllegalArgumentException();
      }
    }

    float sum = 0;
    for (float prob : probabilities) {
      sum += prob;
    }
    if (sum > 1.00001F) { // takes an epsilon to avoid precision problems
      throw new IllegalArgumentException("The sum of the movement probabilities must be less or equal than 1");
    }

    rightProbability = probabilities[RIGHT];
    uprightProbability = probabilities[UP_RIGHT];
    upProbability = probabilities[UP];
    upLeftProbability = probabilities[UP_LEFT];
    leftProbability = probabilities[LEFT];
    downLeftProbability = probabilities[DOWN_LEFT];
    downProbability = probabilities[DOWN];
    downRightProbability = probabilities[DOWN_RIGHT];
    zoomInProbability = probabilities[ZOOM_IN];
    zoomOutProbability = probabilities[ZOOM_OUT];
  }

  /**
   * Constructor.
   *
   * @param rightProb
   * @param upRightProb
   * @param upProb
   * @param upLeftProb
   * @param leftProb
   * @param downLeftProb
   * @param downProb
   * @param downRightProb
   * @param zoomInProb
   * @param zoomOutProb
   */
  public Movements(float rightProb, float upRightProb, float upProb,
                   float upLeftProb, float leftProb, float downLeftProb,
                   float downProb, float downRightProb, float zoomInProb,
                   float zoomOutProb) {

    if ((rightProb < 0) || (rightProb > 1)) {
      throw new IllegalArgumentException();
    }
    if ((upRightProb < 0) || (upRightProb > 1)) {
      throw new IllegalArgumentException();
    }
    if ((upProb < 0) || (upProb > 1)) {
      throw new IllegalArgumentException();
    }
    if ((upLeftProb < 0) || (upLeftProb > 1)) {
      throw new IllegalArgumentException();
    }
    if ((leftProb < 0) || (leftProb > 1)) {
      throw new IllegalArgumentException();
    }
    if ((downLeftProb < 0) || (downLeftProb > 1)) {
      throw new IllegalArgumentException();
    }
    if ((downProb < 0) || (downProb > 1)) {
      throw new IllegalArgumentException();
    }
    if ((downRightProb < 0) || (downRightProb > 1)) {
      throw new IllegalArgumentException();
    }
    if ((zoomInProb < 0) || (zoomInProb > 1)) {
      throw new IllegalArgumentException();
    }
    if ((zoomOutProb < 0) || (zoomOutProb > 1)) {
      throw new IllegalArgumentException();
    }

    if (rightProb + upRightProb + upProb + upLeftProb + leftProb + downLeftProb
            + downProb + downRightProb + zoomInProb + zoomOutProb > 1.00001F) {
      // takes an epsilon to avoid precision problems
      throw new IllegalArgumentException("The sum of the movement probabilities must be less or equal than 1");
    }

    rightProbability = rightProb;
    uprightProbability = upRightProb;
    upProbability = upProb;
    upLeftProbability = upLeftProb;
    leftProbability = leftProb;
    downLeftProbability = downLeftProb;
    downProbability = downProb;
    downRightProbability = downRightProb;
    zoomInProbability = zoomInProb;
    zoomOutProbability = zoomOutProb;
  }

  /**
   * Returns the probability of the movement.
   *
   * @param movement allowed values are {@link #RIGHT}, {@link #UP_RIGHT},
   * {@link #UP}, {@link #UP_LEFT}, {@link #LEFT}, {@link #DOWN_LEFT},
   * {@link #DOWN}, {@link #DOWN_RIGHT},  {@link #ZOOM_IN}, {@link #ZOOM_OUT}.
   * @return the probabiity
   */
  public float getProbability(int movement) {
    switch (movement) {
      case RIGHT:
        return rightProbability;
      case UP_RIGHT:
        return uprightProbability;
      case UP:
        return upProbability;
      case UP_LEFT:
        return upLeftProbability;
      case LEFT:
        return leftProbability;
      case DOWN_LEFT:
        return downLeftProbability;
      case DOWN:
        return downProbability;
      case DOWN_RIGHT:
        return downRightProbability;
      case ZOOM_IN:
        return zoomInProbability;
      case ZOOM_OUT:
        return zoomOutProbability;
      default:
        throw new IllegalArgumentException("Unknown movement");
    }
  }

  public float getRightProbability() {
    return rightProbability;
  }

  public float getUpRightProbability() {
    return uprightProbability;
  }

  public float getUpProbability() {
    return upProbability;
  }

  public float getUpLeftProbability() {
    return upLeftProbability;
  }

  public float getLeftProbability() {
    return leftProbability;
  }

  public float getDownLeftProbability() {
    return downLeftProbability;
  }

  public float getDownProbability() {
    return downProbability;
  }

  public float getDownRightProbability() {
    return downRightProbability;
  }

  public float getZoomInProbability() {
    return zoomInProbability;
  }

  public float getZoomOutProbability() {
    return zoomOutProbability;
  }

  public float getMaxProbabilities() {
    float maxProbs = 0F;
    if (maxProbs < upProbability) {
      maxProbs = upProbability;
    }
    if (maxProbs < downProbability) {
      maxProbs = downProbability;
    }
    if (maxProbs < leftProbability) {
      maxProbs = leftProbability;
    }
    if (maxProbs < rightProbability) {
      maxProbs = rightProbability;
    }
    if (maxProbs < upLeftProbability) {
      maxProbs = upLeftProbability;
    }
    if (maxProbs < uprightProbability) {
      maxProbs = uprightProbability;
    }
    if (maxProbs < downLeftProbability) {
      maxProbs = downLeftProbability;
    }
    if (maxProbs < downRightProbability) {
      maxProbs = downRightProbability;
    }
    if (maxProbs < zoomInProbability) {
      maxProbs = zoomInProbability;
    }
    if (maxProbs < zoomOutProbability) {
      maxProbs = zoomOutProbability;
    }

    return maxProbs;
  }
  
  public float getMinProbabilities() {
    float minProbs = 1F;
    if (minProbs > upProbability) {
      minProbs = upProbability;
    }
    if (minProbs > downProbability) {
      minProbs = downProbability;
    }
    if (minProbs > leftProbability) {
      minProbs = leftProbability;
    }
    if (minProbs > rightProbability) {
      minProbs = rightProbability;
    }
    if (minProbs > upLeftProbability) {
      minProbs = upLeftProbability;
    }
    if (minProbs > uprightProbability) {
      minProbs = uprightProbability;
    }
    if (minProbs > downLeftProbability) {
      minProbs = downLeftProbability;
    }
    if (minProbs > downRightProbability) {
      minProbs = downRightProbability;
    }
    if (minProbs > zoomInProbability) {
      minProbs = zoomInProbability;
    }
    if (minProbs > zoomOutProbability) {
      minProbs = zoomOutProbability;
    }

    return minProbs;
  }
  
  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */

  @Override
  public String toString() {
    String str = "";
    str = getClass().getName() + " [";
    str += "up=" + upProbability;
    str += "down=" + downProbability;
    str += "left=" + leftProbability;
    str += "right=" + rightProbability;
    str += "upLeft=" + upLeftProbability;
    str += "upRight=" + uprightProbability;
    str += "downLeft=" + downLeftProbability;
    str += "downRight=" + downRightProbability;
    str += "zoomIn=" + zoomInProbability;
    str += "zoomOut=" + zoomOutProbability;
    str += "]";
    return str;

  }

  /**
   * Prints this Client fields out to the specified output stream. This
   * method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Movements --");
    out.println("\tup: " + upProbability);
    out.println("\tdown: " + downProbability);
    out.println("\tleft: " + leftProbability);
    out.println("\tright: " + rightProbability);
    out.println("\tup left: " + upLeftProbability);
    out.println("\tup right: " + uprightProbability);
    out.println("\tdown left: " + downLeftProbability);
    out.println("\tdown right: " + downRightProbability);
    out.println("\tzoom in: " + zoomInProbability);
    out.println("\tzoom out: " + zoomOutProbability);

    out.flush();
  }
}
