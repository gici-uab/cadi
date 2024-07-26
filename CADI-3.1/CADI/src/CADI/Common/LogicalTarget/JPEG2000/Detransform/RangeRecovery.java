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
package CADI.Common.LogicalTarget.JPEG2000.Detransform;

import java.io.PrintStream;

import GiciException.*;
import java.util.Arrays;

/**
 * This class receives an image and change its range (the bit depth) to their original one. Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; run<br>
 * &nbsp; get functions<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2012/01/26
 */
public class RangeRecovery {

  /**
   * Definition in {@link CADI.Client.ClientLogicalTarget.JPEG2000.JPEG2KLogicalTarget#imageSamplesFloat}
   */
  private float[][][] imageSamples = null;

  /**
	 * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#precision}
	 */
  private int[] precision = null;

  /**
	 * Value used to modificate the range of each component using a multiplication.
	 * <p>
	 * Only values greater than 0 allowed.
	 */
  private float[] RMMultValues = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public RangeRecovery() {
  }

  /**
   * Performs the range recovery.
   * 
   * @param imageSamples definition in {@link #imageSamples}.
   * @param precision definition in {@link #precision}.
   */
  public void run(float[][][] imageSamples, int[] precision) {
    RMMultValues = new float[imageSamples.length];
    Arrays.fill(RMMultValues, 1F);
    run(imageSamples, precision, RMMultValues);
  }

  /**
   * Performs the range recovery.
   * 
   * @param imageSamples definition in {@link #imageSamples}.
   * @param precision definition in {@link #precision}.
   * @param RMMultValues  definitio in {@link #RMMultValues}.
   */
  public void run(float[][][] imageSamples, int[] precision, float[] RMMultValues) {

    if (imageSamples == null) throw new NullPointerException();
    if (precision == null) throw new NullPointerException();

    this.imageSamples = imageSamples;
    this.precision = precision;
    if (RMMultValues == null) {
      this.RMMultValues = new float[imageSamples.length];
      Arrays.fill(this.RMMultValues, 1F);
    } else {
      for (float value : RMMultValues) {
        if (value < 0) {
          throw new IllegalArgumentException("Only positive values are allowed");
        }
      }
      this.RMMultValues = RMMultValues;
    }
    
    int zSize = imageSamples.length;
    int ySize = imageSamples[0].length;
    int xSize = imageSamples[0][0].length;

    for (int z = 0; z < zSize; z++) {
      //Apply range modification
      if (RMMultValues[z] != 1F) {
        for (int y = 0; y < ySize; y++) {
          for (int x = 0; x < xSize; x++) {
            imageSamples[z][y][x] /= RMMultValues[z];
          }
        }
        if (RMMultValues[z] < 1F) {
          int bitDepthDiff = (int)Math.ceil(Math.log(1 / RMMultValues[z]) / Math.log(2F));
          precision[z] += bitDepthDiff;
        } else {
          int bitDepthDiff = (int)Math.ceil(Math.log(RMMultValues[z]) / Math.log(2F));
          precision[z] -= bitDepthDiff;
        }
      }
    }
  }

  /**
	 * Returns the {@link #precision} attribute.
	 * 
	 * @return the {@link #precision} attribute.
	 */
  public int[] getPrecision() {
    return precision;
  }

  /*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
  public String toString() {
    String str = "";

    str += "---------------------------------------\n";
    str += "            RANGE RECOVERY             \n";
    str += "---------------------------------------\n";
    for (int z = 0; z < imageSamples.length; z++) {
      for (int y = 0; y < imageSamples[z].length; y++) {
        for (int x = 0; x < imageSamples[z][y].length; x++) {
          str += imageSamples[z][y][x] + " ";
        }
        str += "\n";
      }
    }
    str += "---------------------------------------\n";

    return str;
  }

  /**
	 * Prints this Range recovery's fields to the specified output stream.
	 * This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
  public void list(PrintStream out) {

    out.println("-- Range recovery --");

    out.println("Not implemented yet");

    out.flush();
  }
}