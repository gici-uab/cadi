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
package CADI.Proxy.LogicalTarget.JPEG2000;

import java.io.PrintStream;
import java.util.Map;

import CADI.Common.Cache.PrecinctDataBin;
import CADI.Common.Log.CADILog;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2KLogicalTarget;
import CADI.Common.LogicalTarget.JPEG2000.Codestream.JPCMainHeaderDecoder;
import CADI.Common.LogicalTarget.JPEG2000.Codestream.JPKMainHeaderDecoder;
import CADI.Common.Network.JPIP.ClassIdentifiers;
import CADI.Proxy.Client.ProxyCacheManagement;
import GiciException.ErrorException;
import GiciException.WarningException;
import GiciStream.BufferedDataInputStream;

/**
 *
 *
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2011/10/12
 */
public class JP2KProxyLogicalTarget extends JPEG2KLogicalTarget {

  /**
   *
   */
  private ProxyCacheManagement cache = null;

  // ============================= public methods ==============================
  /**
	 * Constructor.
	 * 
	 * @param cache definition in {@link #cache}.
	 * @param log definition in {@link #log}.
	 */
  public JP2KProxyLogicalTarget(String tid, ProxyCacheManagement cache,
          CADILog log) {

    super(tid, log);

    // Check input parameters
    if (cache == null) throw new NullPointerException();

    // Copy input parameters
    this.cache = cache;
  }

  /**
	 * Returns the {@link #codestream} attribute.
	 * 
	 * @return the {@link #codestream} attribute.
	 */
  @Override
  public ProxyJPEG2KCodestream getCodestream(int index) {
    if (!codestreams.containsKey(index)) {
      // It has not been decoded yet, I try it again.
      try {
        decodeMainHeader();
      } catch (ErrorException e) {
        e.printStackTrace();
      }
    }

    return (ProxyJPEG2KCodestream)codestreams.get(index);
  }

  /* (non-Javadoc)
	 * @see CADI.Common.LogicalTarget.JPEG2000.JPEG2KLogicalTarget#getLastCompleteLayer(long, long)
	 */
  @Override
  public int getLastCompleteLayer(long inClassIdentifier, long dataBinLength) {
    if (inClassIdentifier < 0) throw new IllegalArgumentException(); // Change by an assert
    if (dataBinLength < 0) throw new IllegalArgumentException(); // Change by an assert
    PrecinctDataBin dataBin = (PrecinctDataBin)cache.getDataBin(ClassIdentifiers.PRECINCT, inClassIdentifier);
    if (dataBin == null) return 0;

    return dataBin.getLastCompleteLayer(dataBinLength);
  }

  /* (non-Javadoc)
	 * @see CADI.Common.LogicalTarget.JPEG2000.JPEG2KLogicalTarget#getPacketLength(long, int)
	 */
  @Override
  public int getPacketLength(long inClassIdentifier, int layer) {
    if (inClassIdentifier < 0) throw new IllegalArgumentException(); // Change by an assert
    if (layer < 0) throw new IllegalArgumentException(); // Change by an assert

    PrecinctDataBin dataBin = (PrecinctDataBin)cache.getDataBin(ClassIdentifiers.PRECINCT, inClassIdentifier);
    if (dataBin == null) return 0;

    return dataBin.getPacketLength(layer+1);
  }

  /*
	 * (non-Javadoc)
	 * @see CADI.Common.LogicalTarget.JPEG2000.JPEG2KLogicalTarget#getPacketLength(long)
	 */
  @Override
  public long getDataLength(long inClassIdentifier) {
    if (inClassIdentifier < 0) throw new IllegalArgumentException(); // Change by an assert

    PrecinctDataBin dataBin =
            (PrecinctDataBin)cache.getDataBin(ClassIdentifiers.PRECINCT, inClassIdentifier);

    return (dataBin == null) ? 0 : dataBin.getLength();
  }

  /* (non-Javadoc)
	 * @see CADI.Common.LogicalTarget.JPEG2000.JPEG2KLogicalTarget#getPacketOffsetWithDataBin(long, int)
	 */
  @Override
  public int getPacketOffsetWithDataBin(long inClassIdentifier, int layer) {
    PrecinctDataBin dataBin = (PrecinctDataBin)cache.getDataBin(ClassIdentifiers.PRECINCT, inClassIdentifier);
    if (dataBin == null) return 0;    
    return dataBin.getPacketOffset(layer+1);
  }

  /*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
  public String toString() {
    String str = "";

    str += super.toString();

    for (Map.Entry<Integer, JPEG2KCodestream> entry : codestreams.entrySet()) {
      str += entry.getValue().toString();
    }

    return str;
  }

  /**
	 * Prints the JPC logical target data out to the specified output stream.
	 * This method is useful for debugging.
	 *
	 * @param out an output stream.
	 */
  @Override
  public void list(PrintStream out) {

    out.println("-- JP2K Proxy Logical Target --");

    super.list(out);

    for (Map.Entry<Integer, JPEG2KCodestream> entry : codestreams.entrySet()) {
      entry.getValue().list(out);
    }

    out.flush();
  }

  // ============================ private methods ==============================
  /**
	 * Decodes the image main header.
	 * 
	 * @throws WarningException 
	 */
  private boolean decodeMainHeader() throws ErrorException {

    if (cache.getMainHeader() == null) return false;

    // Try to decode the main header
    ProxyJPEG2KCodestream codestream = null;
    try {
      JPCMainHeaderDecoder jpcDeheading =
              new JPCMainHeaderDecoder(new BufferedDataInputStream(cache.getMainHeader()));
      jpcDeheading.run();
      codestream = new ProxyJPEG2KCodestream(0, jpcDeheading.getJPCParameters());
    } catch (ErrorException ee) {

      // Perhaps the main header is a private main header
      try {
        JPKMainHeaderDecoder jpkDeheading =
                new JPKMainHeaderDecoder(new BufferedDataInputStream(cache.getMainHeader()));
        jpkDeheading.run();
        codestream = new ProxyJPEG2KCodestream(0, jpkDeheading.getJPCParameters());
      } catch (ErrorException e) {
        throw new ErrorException("Main header can not be decoded");
      }
    }



    int numTiles = codestream.getNumTiles();
    int numComponents = 0;
    ProxyJPEG2KTile tileObj = null;
    ProxyJPEG2KComponent componentObj = null;
    ProxyJPEG2KResolutionLevel rLevelObj = null;
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
          for (int precinct = 0; precinct < numPrecincts; precinct++) {
            rLevelObj.createPrecinct(precinct);
          }

        }
      }
    }

    codestreams.put(0, codestream);

    log.logInfo("MAIN HEADER DECODED");

    return true;
  }
}