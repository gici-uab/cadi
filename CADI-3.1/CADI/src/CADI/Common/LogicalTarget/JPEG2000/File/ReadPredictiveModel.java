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
package CADI.Common.LogicalTarget.JPEG2000.File;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class reads a file containing the predictive relevance of each image
 * precinct to be applied in the delivery stage.
 * <p>
 * The file must be a text file where each line records the precinct identifier
 * and a value representing the relevance, both separated by a comma character.
 * Each line starting with the number sign character (#) will be considered
 * as a comment and it will be ignored. Regarding the values, the precinct
 * identifier must be a positive long value, and the relevance must be a float
 * value between 0 and 1. Both values must be separated by a comma, whitespace,
 * or a tab.
 * 
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2010/07/14
 */
public class ReadPredictiveModel {

  /**
   * Records the values to be applied in a predictive model for each precinct.
   * The <code>key</code> of the map is a unique precinct identifier, while
   * the <code>value</code> is a float in the range [0,1] with the relevance
   * associated with the precinct.
   */
  private HashMap<Long, Float> predictiveModel = null;

  /**
   * A stream to the file to be read. 
   */
  private BufferedReader fileReader = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   * 
   * @throws FileNotFoundException 
   */
  public ReadPredictiveModel(String fileName) throws FileNotFoundException {
    this(new File(fileName));
  }

  public ReadPredictiveModel(File file) throws FileNotFoundException {
    // Check input parameters
    if (file == null) {
      throw new NullPointerException();
    }

    if (!file.exists()) {
      throw new FileNotFoundException("File \"" + file.getAbsolutePath()
              + "\" does not exist.");
    }

    fileReader = new BufferedReader(new FileReader(file));

    // Initializations
    predictiveModel = new HashMap<Long, Float>();
  }

  /**
   * Performs the reading of the file where the predictive model is saved.
   * 
   * @return the {@link #predictiveModel} attribute.
   * @throws IOException 
   */
  public HashMap<Long, Float> run() throws IOException {

    assert (fileReader != null);

    String line = null;
    Long inClassIdentifier = -1L;
    float predictiveValue = 0F;
    int pos;

    Pattern linePattern = Pattern.compile("[,\\s\\t]");

    try {
      while ((line = fileReader.readLine()) != null) {
        if (line.startsWith("#")) {
          continue;
        }

        // Split line
        String[] sValues = linePattern.split(line);
        if (sValues.length != 2) {
          throw new IOException("Illegal file format");
        }

        // Parse and convert values
        try {
          inClassIdentifier = Long.parseLong(sValues[0]);
          predictiveValue = Float.parseFloat(sValues[1]);
        } catch (NumberFormatException nfe) {
          throw new IOException("Wrong predictive value");
        }

        // Check ranges
        if ((predictiveValue < 0F) || (predictiveValue > 1F)) {
          throw new IOException("Wrong predictive value");
        }

        if (predictiveModel.containsKey(inClassIdentifier)) {
          throw new IOException("Duplicated identifier.");
        }

        predictiveModel.put(inClassIdentifier, predictiveValue);
      }
    } catch (NumberFormatException e) {
      throw new IOException("Illegal number format.");
    } catch (EOFException e) {
    }

    return predictiveModel;
  }

  /**
   * Returns the {@link #predictiveModel} attribue.
   * 
   * @return the {@link #predictiveModel} attribue.
   */
  public HashMap<Long, Float> getPredictiveModel() {
    return predictiveModel;
  }
  
  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";

    if (predictiveModel.isEmpty()) {
      str += " <<< List is empty >>> ";
    } else {
      for (Map.Entry<Long, Float> entry : predictiveModel.entrySet()) {
        str += "key=" + entry.getKey();
        str += "value=" + entry.getValue().toString();
      }
    }

    str += "]";
    return str;
  }

  /**
   * Prints this Predictive Model fields out to the specified output
   * stream. This method is useful for debugging.
   * 
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Read Predictive Model --");

    if (predictiveModel.isEmpty()) {
      out.println(" <<< List is empty >>> ");
    } else {
      for (Map.Entry<Long, Float> entry : predictiveModel.entrySet()) {
        out.println("key: " + entry.getKey() + " value: " + entry.getValue());
      }
    }

    out.flush();
  }
}