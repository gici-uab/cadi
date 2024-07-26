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

import CADI.Common.LogicalTarget.JPEG2000.Parameters.CBDParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.COCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.COMParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.JPKParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.MCCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.MCOParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.MCTParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.QCCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.QCDParameters;
import CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters;

/**
 * This class is a container to save all the JPEG2000 codestream parameters.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.4 2011/03/06
 */
public class JPCParameters {
  // TODO: The name of this class will be changed to MainHeaderParameters

  // Image and tile size parameters (SIZ)
  public SIZParameters sizParameters = null;

  // Coding style default (COD and COC)
  public CODParameters codParameters = null;

  public HashMap<Integer, COCParameters> cocParametersList = null;

  // Quantization default (QCD and QCC)
  public QCDParameters qcdParameters = null;

  public HashMap<Integer, QCCParameters> qccParametersList = null;

  // Region of interest (RGN)
  // Component bit depth (CBD)
  public CBDParameters cbdParameters = null;

  // Multiple component transformation (MCT)
  public HashMap<Integer, MCTParameters> mctParametersList = null;

  // Multiple component transform collection (MCC)
  // The integer of the hash key stand for the stage.
  public HashMap<Integer, MCCParameters> mccParametersList = null;

  // Multiple component transform ordering (MCO)
  public MCOParameters mcoParameters = null;

  // Comments (COM)
  public COMParameters comParameters = null;

  // Wrapped used to store uncompliant parameters.
  public JPKParameters jpkParameters = null;

  // ============================= public methods ==============================
  /**
   * Default Constructor.
   */
  public JPCParameters() {

    sizParameters = new SIZParameters();
    codParameters = new CODParameters();
    cocParametersList = new HashMap<Integer, COCParameters>();
    qcdParameters = new QCDParameters();
    qccParametersList = new HashMap<Integer, QCCParameters>();
    cbdParameters = new CBDParameters();
    mctParametersList = new HashMap<Integer, MCTParameters>();
    mccParametersList = new HashMap<Integer, MCCParameters>();
    mcoParameters = new MCOParameters();
    comParameters = new COMParameters();

    //jpkParameters = new JPKParameters();
  }

  /**
   * Constructor. Deep copy constructor.
   *
   * @param jpcParameters is an object of this class.
   */
  public JPCParameters(JPCParameters jpcParameters) {

    // TODO: check for mandatory options not null, i.e. sizParameters, codParameters, etc.
    
    sizParameters = new SIZParameters(jpcParameters.sizParameters);
    codParameters = new CODParameters(jpcParameters.codParameters);
    if (jpcParameters.cocParametersList != null) {
      for (int key : jpcParameters.cocParametersList.keySet()) {
        cocParametersList.put(key, jpcParameters.cocParametersList.get(key));
      }
    }
    qcdParameters = new QCDParameters(jpcParameters.qcdParameters);
    if (jpcParameters.qccParametersList != null) {
      for (int key : jpcParameters.qccParametersList.keySet()) {
        qccParametersList.put(key, jpcParameters.qccParametersList.get(key));
      }
    }
    cbdParameters = new CBDParameters(jpcParameters.cbdParameters);
    if (jpcParameters.mctParametersList != null) {
      for (int key : jpcParameters.mctParametersList.keySet()) {
        mctParametersList.put(key, jpcParameters.mctParametersList.get(key));
      }
    }
    mcoParameters = new MCOParameters(jpcParameters.mcoParameters);
    mccParametersList = new HashMap<Integer, MCCParameters>();
    if (jpcParameters.mccParametersList != null) {
      for (int key : jpcParameters.mccParametersList.keySet()) {
        mccParametersList.put(key, jpcParameters.mccParametersList.get(key));
      }
    }
    comParameters = new COMParameters(jpcParameters.comParameters);

    if (jpcParameters.jpkParameters != null) {
      jpkParameters = new JPKParameters(jpcParameters.jpkParameters);
    }
  }

  /**
   * Resets the attributes to its initial values.
   */
  public void reset() {
    sizParameters.reset();
    codParameters.reset();
    for (int key : cocParametersList.keySet()) {
      cocParametersList.get(key).reset();
    }
    qcdParameters.reset();
    for (int key : qccParametersList.keySet()) {
      qccParametersList.get(key).reset();
    }
    cbdParameters.reset();
    for (int key : mctParametersList.keySet()) {
      mctParametersList.get(key).reset();
    }
    for (int key : mccParametersList.keySet()) {
      mccParametersList.get(key).reset();
    }
    mcoParameters.reset();
    comParameters.reset();

    if (jpkParameters != null) {
      jpkParameters.reset();
    }
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str += sizParameters.toString();
    str += codParameters.toString();
    if (cocParametersList != null) {
      for (int key : cocParametersList.keySet()) {
        str += cocParametersList.get(key).toString();
      }
    }
    str += qcdParameters.toString();
    if (qccParametersList != null) {
      for (int key : qccParametersList.keySet()) {
        str += qccParametersList.get(key).toString();
      }
    }
    str += cbdParameters.toString();
    if (mctParametersList != null) {
      for (int key : mctParametersList.keySet()) {
        str += mctParametersList.get(key).toString();
      }
    }
    if (mccParametersList != null) {
      for (int key : mccParametersList.keySet()) {
        str += mccParametersList.get(key).toString();
      }
    }
    str += mcoParameters.toString();
    str += comParameters.toString();
    if (jpkParameters != null) {
      str += jpkParameters.toString();
    }

    return str;
  }

  /**
   * Prints the JPC Parameters data out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- JPC Parameters --");

    sizParameters.list(out);
    out.println();
    codParameters.list(out);
    out.println();
    if (cocParametersList != null) {
      for (int key : cocParametersList.keySet()) {
        cocParametersList.get(key).list(out);
      }
    }
    out.println();
    qcdParameters.list(out);
    out.println();
    if (qccParametersList != null) {
      for (int key : qccParametersList.keySet()) {
        qccParametersList.get(key).list(out);
      }
    }
    out.println();
    cbdParameters.list(out);
    out.println();
    if (mctParametersList != null) {
      for (int key : mctParametersList.keySet()) {
        mctParametersList.get(key).list(out);
      }
    }
    out.println();
    if (mccParametersList != null) {
      for (int key : mccParametersList.keySet()) {
        mccParametersList.get(key).list(out);
      }
    }
    out.println();
    mcoParameters.list(out);
    out.println();
    comParameters.list(out);
    out.println();

    if (jpkParameters != null) {
      jpkParameters.list(out);
    }

    out.flush();
  }
}
