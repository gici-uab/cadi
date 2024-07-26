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

import java.io.PrintStream;

import CADI.Common.LogicalTarget.JPEG2000.JPCParameters;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream;
import CADI.Common.Util.ArraysUtil;
import GiciException.ErrorException;
import java.util.Arrays;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2010/11/13
 */
public class ClientJPEG2KCodestream extends JPEG2KCodestream {

  // ============================= public methods ==============================
  /**
   *
   * @param identifier
   * @param jpcParameters
   */
  public ClientJPEG2KCodestream(int identifier, JPCParameters jpcParameters) {
    super(identifier, jpcParameters);
  }

  /**
   *
   * @param identifier
   * @throws IllegalAccessException
   */
  @Override
  public void createTile(int index) {
    if (!tiles.containsKey(index)) {
      tiles.put(index, new ClientJPEG2KTile(this, index));
    }
  }

  /*
   * (non-Javadoc)
   * @see CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream#getTile(int)
   */
  @Override
  public ClientJPEG2KTile getTile(int index) {
    return (ClientJPEG2KTile)tiles.get(index);
  }

  /**
   * Calculates which are the necessary components to invert a multiple
   * component transformation.
   *
   * compsRanges the range of components to be decompressed. The first
   * 			index is an array index of the ranges. An the second index
   * 			indicates: 0 is the first component of the range, and 1 is the
   * 			last component of the range.
   *
   * @return an one-dimensional array with required components to inver the
   * 			multiple component transform.
   * @throws ErrorException
   */
  public int[] getRelevantComponents(int[][] comps) throws ErrorException {

    // If comps are null, request for all components
    if (comps == null) {
      return null;
    }

    // If no multiple component transformation is applied,
    // no extra components are necessary
    if (!isMultiComponentTransform()) {
      return ArraysUtil.rangesToIndexes(comps);
    }

    // Get necessary components to invert the multiple
    // component transformation
    int[][] necCompsRanges = null;
    int multiComponentTransform = getMultiComponentTransform();
    try {
      if (multiComponentTransform > 0) {
        CalculateRelevantComponents crc = new CalculateRelevantComponents(
                multiComponentTransform,
                getZSize(),
                cbdParameters,
                mctParametersList,
                mccParametersList,
                mcoParameters,
                comps);
        necCompsRanges = crc.run();
      } else if ((jpkParameters != null) && (jpkParameters.WT3D != 0)) {
        CalculateRelevantComponents crc = new CalculateRelevantComponents(
                jpkParameters,
                getZSize(),
                comps);
        necCompsRanges = crc.run();
      } else {
        assert (true);
      }
    } catch (ErrorException e) {
      throw new ErrorException("Unsupported multicomponent transform");
    }

    return ArraysUtil.rangesToIndexes(necCompsRanges);
  }

  /**
   * Check if the logical target has a multiple component transformation.
   *
   * @return <code>true</code> if the logical target is spectrally
   * 			transformed. Otherwise, returns <code>false</code>.
   */
  public boolean isMultiComponentTransform() {
    if (getMultiComponentTransform() == 0) {
      if ((getJPKParameters() != null) && (getJPKParameters().WT3D != 0)) {
        return true;
      }
      return false;
    } else {
      return true;
    }
  }

  /**
   * Returns the number of bits per sample for the components in
   * <code>relevantComponents</code>.
   * 
   * @param relevantComponents
   * 
   * @return 
   */
  public int[] getPrecision(int[] relevantComponents) {
    int[] precision = new int[relevantComponents.length];
    for (int zIdx = 0; zIdx < relevantComponents.length; zIdx++) {
      precision[zIdx] = getPrecision(relevantComponents[zIdx]);
    }
    return precision;
  }

  /**
   * 
   * @param relevantComponents
   * @return 
   */
  public float[] getRMMultValues(int[] relevantComponents) {
    float[] RMMultValues = new float[relevantComponents.length];
    if (getJPKParameters() == null) {
      Arrays.fill(RMMultValues, 1F);
    } else {
      for (int zIdx = 0; zIdx < relevantComponents.length; zIdx++) {
        RMMultValues[zIdx] = getJPKParameters().RMMultValues[relevantComponents[zIdx]];
      }
    }

    return RMMultValues;
  }

  /**
   * 
   * @param relevantComponents
   * @return 
   */
  public boolean[] getLSComponents(int[] relevantComponents) {
    boolean[] LSComponents = new boolean[relevantComponents.length];

    if (getJPKParameters() == null) {
      for (int zIdx = 0; zIdx < relevantComponents.length; zIdx++) {
        LSComponents[zIdx] = !isSigned(relevantComponents[zIdx]);
      }
    } else {
      for (int zIdx = 0; zIdx < relevantComponents.length; zIdx++) {
        LSComponents[zIdx] = getJPKParameters().LSComponents[relevantComponents[zIdx]];
      }
    }

    return LSComponents;
  }

  public boolean[] isSigned(int[] relevantComponents) {
    boolean[] signed = new boolean[relevantComponents.length];
    for (int zIdx = 0; zIdx < relevantComponents.length; zIdx++) {
      signed[zIdx] = isSigned(relevantComponents[zIdx]);
    }
    return signed;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";
    str += getClass().getName() + " [";
    super.toString();


    str += "]";

    return str;
  }

  /**
   * Prints this Client JPEG2K Codestream out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Client JPEG2K Codestream --");
    super.list(out);
  }
  // ============================ private methods ==============================
}
