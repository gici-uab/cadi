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
package CADI.Server.LogicalTarget.JPEG2000;

import CADI.Common.LogicalTarget.JPEG2000.Indexing.HeaderIndexTable;
import CADI.Common.LogicalTarget.JPEG2000.Indexing.FileIndex;
import CADI.Common.LogicalTarget.JPEG2000.Indexing.CodestreamIndex;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;

import CADI.Common.Log.CADILog;
import CADI.Common.LogicalTarget.JPEG2000.JPCParameters;
import CADI.Common.LogicalTarget.JPEG2000.Codestream.CodestreamIndexing;
import CADI.Common.LogicalTarget.JPEG2000.Codestream.JPCMainHeaderDecoder;
import CADI.Common.LogicalTarget.JPEG2000.Codestream.JPKMainHeaderDecoder;
import CADI.Common.LogicalTarget.JPEG2000.File.ReadJP2File;
import CADI.Common.LogicalTarget.JPEG2000.File.ReadJPEG2KFileFormat;
import CADI.Common.LogicalTarget.JPEG2000.File.ReadJPXFile;
import CADI.Common.LogicalTarget.JPEG2000.File.ReadPredictiveModel;
import CADI.Common.LogicalTarget.JPEG2000.PredictiveScalingFactors;
import GiciException.ErrorException;
import GiciException.WarningException;
import GiciStream.BufferedDataInputStream;

/**
 * Indexes a logical target file, i.e, reads the logical target structure
 * and it is kept in memory be used later when a client request for a
 * Window of Interest of the logical target. How the logical target
 * structure is kept in memory, it is more easy and faster to find the
 * precincts which belong to the requested WOI.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.4 2012/01/14
 */
public class JP2KLogicalTargetIndexer {

  private String target = null;

  /**
   *
   */
  private BufferedDataInputStream in = null;

  /**
   *
   */
  private CADILog log = null;

  /**
   * Is the logical target which is being delivery. This object contains
   * information about the target geometry, file input stream, ...
   */
  private JP2KServerLogicalTarget logicalTarget = null;

  /**
   * Indicates whether the coding passes structure must be read. Otherwise,
   * codestream is only indexed at packet structure.
   */
  private boolean readCodingPasses = false;

  /**
   * It is the parent name (which invokes an object of this class).
   * <p>
   * This attribute will be used to be passed to the log object as the thread name.
   */
  private String parentName = "";

  /**
   * Is the file name of a file which contains the predictive model values.
   * <p>
   * Further information about predictive model, please see
   * {@link CADI.Server.LogicalTarget.ReadPredictiveModel} class.
   */
  private String predictiveModelFileName = null;

  /**
   *
   */
  private boolean doTranscoding = false;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param parentName
   * @param target
   * @param in
   * @param log
   */
  public JP2KLogicalTargetIndexer(String parentName, String target,
                                  BufferedDataInputStream in, CADILog log) {

    // Check input parameters
    if (target == null) {
      throw new NullPointerException();
    }
    if (in == null) {
      throw new NullPointerException();
    }
    if (log == null) {
      throw new NullPointerException();
    }

    // Copy input parameters
    this.target = target;
    this.parentName = parentName == null ? "" : parentName;
    this.in = in;
    this.log = log;
  }

  /**
   * Sets the {@link #readCodingPasses} attribute.
   *
   * @param readCodingPasses definition in {@link #readCodingPasses}.
   */
  public void setReadCodingPasses(boolean readCodingPasses) {
    this.readCodingPasses = readCodingPasses;
  }

  /**
   * Sets the {@link #predictiveModelFileName} attribute.
   *
   * @param predictiveModelFileName definition in {@link #predictiveModelFileName}.
   */
  public void readPredictiveModel(String predictiveModelFileName) {
    this.predictiveModelFileName = predictiveModelFileName;
  }

  /**
   *
   * @throws WarningException
   */
  public void run() throws WarningException {

    ////////////////////////////////////////////////////
    //
    // ATTENTION: READING JP2 & JPX BOXES HAS NOT BEEN FINISHED YET.
    // IT ONLY READS THE REQUIRED BOXES AND USES THE
    // CONTIGUOUS CODE-STREAM BOX (ONLY 1 CODESTREAM)
    //
    ////////////////////////////////////////////////////

    BoxIndexing fileIndexing = new BoxIndexing(null, 0, -1, -1, true);
    
    // READ FILE FORMAT
    int fileFormat = -1;
    ReadJPEG2KFileFormat readJPEG2KFileFormat = new ReadJPEG2KFileFormat(in, fileIndexing);
    try {
      fileFormat = readJPEG2KFileFormat.run();
    } catch (EOFException e2) {
      e2.printStackTrace();
      throw new WarningException("JP2 main header can not be readed or decoded correctly");
    } catch (ErrorException e2) {
      e2.printStackTrace();
      throw new WarningException("JP2 main header can not be readed or decoded correctly");
    } catch (IOException e2) {
      e2.printStackTrace();
      throw new WarningException("JP2 main header can not be readed or decoded correctly");
    }

    // READ FILE HEADERS
    HeaderIndexTable mhix = new HeaderIndexTable();

    JPCParameters jpcParameters = null;
    switch (fileFormat) {
      case ReadJPEG2KFileFormat.FILE_FORMAT_JPC:
        JPCMainHeaderDecoder jpcDeheading = new JPCMainHeaderDecoder(in);
        try {
          jpcDeheading.run();
        } catch (ErrorException e) {
          throw new WarningException("JPC main header can not be readed or decoded correctly");
        }
        jpcParameters = jpcDeheading.getJPCParameters();
        mhix.mainHeaderInitialPos = jpcDeheading.getMainHeaderInitialPos();
        mhix.mainHeaderLength = jpcDeheading.getMainHeaderLength();
        break;

      case ReadJPEG2KFileFormat.FILE_FORMAT_JP2:
        ReadJP2File jp2Deheading = new ReadJP2File(in, fileIndexing);
        try {
          jp2Deheading.run();
        } catch (Exception e) {
          throw new WarningException("JP2 main header can not be readed or decoded correctly");
        }
        jpcParameters = jp2Deheading.getJPCParameters();
        mhix.mainHeaderInitialPos = jp2Deheading.getMainHeaderInitialPos();
        mhix.mainHeaderLength = jp2Deheading.getMainHeaderLength();
        break;

      case ReadJPEG2KFileFormat.FILE_FORMAT_JPX:
        ReadJPXFile jpxDeheading = new ReadJPXFile(in, fileIndexing);
        try {
          jpxDeheading.run();
        } catch (Exception e) {
          throw new WarningException("JP2 main header can not be readed or decoded correctly");
        }
        jpcParameters = jpxDeheading.getJPCParameters();
        mhix.mainHeaderInitialPos = jpxDeheading.getMainHeaderInitialPos();
        mhix.mainHeaderLength = jpxDeheading.getMainHeaderLength();
        break;

      case ReadJPEG2KFileFormat.FILE_FORMAT_JPK:
        JPKMainHeaderDecoder jpkDeheading = new JPKMainHeaderDecoder(in);
        try {
          jpkDeheading.run();
        } catch (ErrorException e) {
          throw new WarningException("JPk main header can not be readed or decoded correctly");
        }
        jpcParameters = jpkDeheading.getJPCParameters();
        mhix.mainHeaderInitialPos = jpkDeheading.getMainHeaderInitialPos();
        mhix.mainHeaderLength = jpkDeheading.getMainHeaderLength();
        break;

      default:
        assert (true);
    }
    //jpcParameters.list(System.out); // DEBUG
   //fileIndexing.list(System.out);

    // BUILD TILES AND COMPONENTS STRUCTURE
    ServerJPEG2KCodestream codestream = new ServerJPEG2KCodestream(0, jpcParameters);
    CodestreamIndex cidx = new CodestreamIndex();
    cidx.mhix = mhix;

    int numTiles = codestream.getNumTiles();
    int numComponents = 0;
    ServerJPEG2KTile tileObj = null;
    ServerJPEG2KComponent componentObj = null;
    ServerJPEG2KResolutionLevel rLevelObj = null;

    for (int t = 0; t < numTiles; t++) {
      codestream.createTile(t);
      tileObj = codestream.getTile(t);
      numComponents = codestream.getZSize();
      for (int c = 0; c < numComponents; c++) {
        tileObj.createComponent(c);
        componentObj = tileObj.getComponent(c);
        int maxWTLevels = componentObj.getWTLevels();
        for (int r = 0; r <= maxWTLevels; r++) {
          componentObj.createResolutionLevel(r);
          rLevelObj = componentObj.getResolutionLevel(r);
          int numPrecincts = rLevelObj.getNumPrecincts();
          for (int p = 0; p < numPrecincts; p++) {
            rLevelObj.createPrecinct(p);
          }
        }
      }
    }
    
    //codestream.list(System.out); // DEBUG
    
    // FILE INDEXING
    log.logInfo(parentName + " (logical target delivery): file indexing ...");
    CodestreamIndexing fi = null;
    try {
      fi = new CodestreamIndexing(in, codestream);
      
      if (readCodingPasses) {
        fi.setReadingCodingPasses(true);
      }
      fi.run();
    } catch (ErrorException e1) {
      //e1.printStackTrace(System.out);
      throw new WarningException("file can not be indexed correctly (packet header can not be decoded)");
    }

    cidx.tpix = fi.getTilePartIndexTable();
    cidx.thix = fi.getTileHeaderIndexTable();
    cidx.ppix = fi.getPrecinctPacketIndexTable();
    cidx.phix = fi.getPacketHeaderIndexTable();


    // DO TRANSCODING
     /* doTranscoding = false;
     * if (doTranscoding) {
     * codestream.transcode();
     * } */

    // BUILDS THE LOGICAL TARGET
    logicalTarget = new JP2KServerLogicalTarget(target, in, codestream, cidx);

    // READ PREDICTIVE MODEL
    if (predictiveModelFileName != null) {
      try {
        ReadPredictiveModel rpm = new ReadPredictiveModel(predictiveModelFileName);
        logicalTarget.setScalingFactors(new PredictiveScalingFactors(rpm.run()));
      } catch (FileNotFoundException e) {
      } catch (IOException e) {
      }
    }
    
    log.logInfo(parentName + " (logical target delivery): file indexing done");
  }

  /**
   * Returns the {@link #logicalTarget} attribute.
   *
   * @return the {@link #logicalTarget} attribute.
   */
  public JP2KServerLogicalTarget getLogicalTarget() {
    return this.logicalTarget;
  }
  // ============================ private methods ==============================
}