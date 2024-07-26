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
package CADI.Common.Util;

import java.io.PrintStream;

/**
 * This class is used to record ranges of values. The range of values are from
 * <code>from</code> to <code>to</code>. Moreover, the range of values can
 * optionally be sub-sampled by the <code>samplingFactor</code> factor.
 * <p>
 * Only non-negative ranges are allowed.
 * <p>
 * If the <code>from</code> value is greater than or equal to the <code>to
 * </code>value, the range is empty.
 * <p>
 * For further examples about sampled range, see ISO/IEC 15444-9 section C.2.3,
 * C.4.5, C.4.6, C.4.7
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2011/12/15
 */
public class SampledRange {

  /**
   * Lower bound of the range.
   * <p>
   * It is included.
   */
  private int from = -1;

  /**
   * Upper bound of the range.
   * <p>
   * Its inclusion depends on the {@link #samplingFactor}.
   */
  private int to = -1;

  /**
   * Sub-sampling factor (an step).
   */
  private int samplingFactor = 1;

  // ============================= public methods ==============================
  /**
   * Constructor.
   * <p>
   * Creates a empty range.
   */
  public SampledRange() {
  }

  /**
   * Constructor.
   * <p>
   * Default sub-sampling factor is 1.
   * 
   * @param from definition in {@link #from}
   * @param to definition in {@link #to}
   */
  public SampledRange(int from, int to) {
    this(from, to, 1);
  }

  /**
   * Constructor.
   * 
   * @param from definition in {@link #from}
   * @param to definition in {@link #to}
   * @param samplingFactor definition in {@link #samplingFactor}
   */
  public SampledRange(int from, int to, int samplingFactor) {
    if (from < 0 || to < 0)
      throw new IllegalArgumentException("Only non-negative ranges are allowed");
    if (samplingFactor < 1)
      throw new IllegalArgumentException("Sub-sampling factor must be non-negative");

    this.from = from;
    this.to = to;
    this.samplingFactor = samplingFactor;
  }

  /**
   * Sets the {@link #from} attribute.
   * 
   * @param 
   */
  public void setFrom(int from) {
    if (from < 0)
      throw new IllegalArgumentException("Only non-negative ranges are allowed");
    this.from = from;
  }

  /**
   * 
   */
  public void setTo(int to) {
    if (to < 0)
      throw new IllegalArgumentException("Only non-negative ranges are allowed");
    this.to = to;
  }

  /**
   * 
   */
  public void setSamplingFactor(int samplingFactor) {
    if (samplingFactor < 0)
      throw new IllegalArgumentException("Sub-sampling factor must be non-negative");
    this.samplingFactor = samplingFactor;
  }

  /**
   * Returns the {@link #from} attribute.
   * @return 
   */
  public int getFrom() {
    return from;
  }

  /**
   * Returns the {@link #to} attribute.
   * 
   * @return 
   */
  public int getTo() {
    return to;
  }

  /**
   * Returns the {@link #samplingFactor} attribute.
   * 
   * @return 
   */
  public int getSamplingFactor() {
    return samplingFactor;
  }

  /**
   * 
   * @return 
   */
  public boolean isEmpty() {
    return (from > to) ? true : false;
  }

  /**
   * Returns a one-dimensional array with the range values.
   * 
   * @return a one-dimensional array with the range values.
   */
  public int[] getAsArray() {
    int numValues = (int)Math.ceil(1.0 * (to - from) / samplingFactor);
    int[] values = new int[numValues];
    for (int i = 0; i < numValues; i++) {
      values[i] = from + i * samplingFactor;
    }

    return values;
  }

  /**
	 * Sets the attributes to its initial values.
	 */
  public void reset() {
    from = -1;
    to = -1;
    samplingFactor = 1;
  }

  /**
	 * For debugging purpose
	 */
  @Override
  public String toString() {
    return getClass().getName() + "[from=" + from + ", to=" + to + "samplingFactor="
           + samplingFactor + "]";
  }

  /**
	 * Prints this CADIPoint out to the specified output stream. This method
	 * is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
  public void list(PrintStream out) {
    out.println("-- Sampled Range --");
    out.println("from: " + from);
    out.println("to: " + to);
    out.println("samplingFactor: " + samplingFactor);
    out.flush();
  }
}