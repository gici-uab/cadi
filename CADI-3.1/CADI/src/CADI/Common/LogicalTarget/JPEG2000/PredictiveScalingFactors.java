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
package CADI.Common.LogicalTarget.JPEG2000;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2011/02/19
 */
public class PredictiveScalingFactors {

  /**
   * Addtional window scaling factors.
   * <p>
   * Defines new scaling factors / weights to be applied. The <code>key</code>
   * of the HashMap is the unique precinct identifier and the <code>value</code>
   * is a real value of the weight, in the range [0,1].
   */
  private HashMap<Long, Float> scalingFactors = null;

  /**
   * Is the default value of precincts whose weight has not been defined
   * in {@link #scalingFactors}.
   */
  private float defaultScalingFactor = 0F;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public PredictiveScalingFactors() {
    this(0F);
  }

  /**
   *
   * @param defaultScalingFactor
   */
  public PredictiveScalingFactors(float defaultScalingFactor) {
    if ((defaultScalingFactor < 0) || (defaultScalingFactor > 1)) {
      throw new IllegalArgumentException();
    }
    this.defaultScalingFactor = defaultScalingFactor;
    scalingFactors = new HashMap<Long, Float>();

  }

  /**
   * 
   * @param scalingFactors
   */
  public PredictiveScalingFactors(HashMap<Long, Float> scalingFactors) {
    this(scalingFactors, 0F);
  }

  /**
   *
   * @param scalingFactors
   * @param defaultScalingFactor
   */
  public PredictiveScalingFactors(HashMap<Long, Float> scalingFactors,
                                  float defaultScalingFactor) {

    if (scalingFactors == null) {
      throw new NullPointerException();
    }
    if ((defaultScalingFactor < 0) || (defaultScalingFactor > 1)) {
      throw new IllegalArgumentException();
    }

    this.scalingFactors = scalingFactors;
    this.defaultScalingFactor = defaultScalingFactor;
  }

  /**
   *
   * @param inClassIdentifier
   * @param factor
   */
  public void setValue(long inClassIdentifier, float factor) {
    scalingFactors.put(inClassIdentifier, factor);
  }

  public void setDefaultValue(float factor) {
    defaultScalingFactor = factor;
  }

  /**
   *
   * @param inClassIdentifier
   * @return
   */
  public float getValue(long inClassIdentifier) {
    return scalingFactors.containsKey(inClassIdentifier)
            ? scalingFactors.get(inClassIdentifier)
            : defaultScalingFactor;
  }

  /**
   * 
   * @return
   */
  public float getDefaultValue() {
    return defaultScalingFactor;
  }

  /**
   * 
   * @param inClassIdentifier
   * @return
   */
  public boolean contains(long inClassIdentifier) {
    return scalingFactors.containsKey(inClassIdentifier);
  }

  /**
   * 
   * @return
   */
  public Set<Entry<Long, Float>> entrySet() {
    return scalingFactors.entrySet();
  }

  /**
   *
   * @return
   */
  public Set<Long> keySet() {
    return scalingFactors.keySet();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    String str = "";

    str = getClass().getName() + " [";

    if (scalingFactors.isEmpty()) {
      str += " <<< List is empty >>> ";
    } else {
      for (Map.Entry<Long, Float> entry : scalingFactors.entrySet()) {
        str += "key="+entry.getKey()+" value="+entry.getValue().toString()+" ";
      }
      str += "default value="+defaultScalingFactor;
    }

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

    out.println("-- Predictive Scaling Factors --");

    if (scalingFactors.isEmpty()) {
      out.println(" <<< List is empty >>> ");
    } else {
      for (Map.Entry<Long, Float> entry : scalingFactors.entrySet()) {
        out.println("key: "+entry.getKey()+", value: "+entry.getValue());
      }
      out.println("default value: " + defaultScalingFactor);
    }

    out.flush();

    out.flush();
  }
  
}
