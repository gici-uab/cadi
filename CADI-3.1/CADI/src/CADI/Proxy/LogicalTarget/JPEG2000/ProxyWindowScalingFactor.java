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
package CADI.Proxy.LogicalTarget.JPEG2000;

import java.io.PrintStream;
import java.util.ArrayList;

import CADI.Common.LogicalTarget.JPEG2000.RelevantPrecinct;
import CADI.Common.LogicalTarget.JPEG2000.WindowScalingFactor;
import CADI.Common.Network.JPIP.EORCodes;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Proxy.Server.ProxyCacheModel;
import GiciException.ErrorException;

/**
 * This class implements a delivery of data belonging to the requested WOI
 * following a Window Scaling Factor (WSF) strategy. The WSF applies a
 * weight to each precinct belonging to the WOI according to the percentage
 * of pixels overlapped.
 * <p>
 * Further information about the Window Scaling Factor, see:
 * Taubman, D. and Rosenbaum, R., "Rate-Distortion Optimized Interactive
 * Browsing of JPEG2000 Images" 
 * and
 * Lima, L. and Taubman, D. and Leonardi, R., "JPIP Proxy Server for
 * remote browsing of JPEG2000 images" 
 * 
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2008/09/08
 *
 * <p>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; runResponseParameters<br>
 * &nbsp; runResponseData<br>
 * &nbsp; getResponseViewWindow<br>
 * &nbsp; getQuality<br>
 * &nbsp; getJPIPMessageData<br>
 * &nbsp; getEORReasonCode<br>
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2010/11/07
 */
public class ProxyWindowScalingFactor extends WindowScalingFactor {

  /**
   * Definition in {@link CADI.Server.LogicalTarget.JPEG2000.JP2KServerLogicalTarget}.
   */
  private JP2KProxyLogicalTarget logicalTarget = null;

  /**
   * This attribute contains the cache data for the client.
   * <p>
   * This reference is passed from the
   *
   */
  private ProxyCacheModel serverCache = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param logicalTarget definition in {@link #logicalTarget}.
   * @param serverCache definition in {@link #serverCache}.
   */
  public ProxyWindowScalingFactor(JP2KProxyLogicalTarget logicalTarget,
                                  ProxyCacheModel serverCache) {

    super(logicalTarget, serverCache, logicalTarget.getCodestream(0));

    if (logicalTarget == null) {
      throw new NullPointerException();
    }
    if (serverCache == null) {
      throw new NullPointerException();
    }

    this.logicalTarget = logicalTarget;
    this.serverCache = serverCache;

    super.logicalTarget = logicalTarget;
    super.serverCache = serverCache;
  }

  /**
   *
   * @param logicalTarget
   * @param serverCache
   * @param align
   */
  public ProxyWindowScalingFactor(JP2KProxyLogicalTarget logicalTarget,
                                  ProxyCacheModel serverCache,
                                  boolean align) {

    super(logicalTarget, serverCache, logicalTarget.getCodestream(0), align);

    if (logicalTarget == null) {
      throw new NullPointerException();
    }
    if (serverCache == null) {
      throw new NullPointerException();
    }

    this.logicalTarget = logicalTarget;
    this.serverCache = serverCache;
  }

  /**
   *
   * @return
   * @throws ErrorException
   */
  @Override
  public ArrayList<RelevantPrecinct> runResponseData() throws ErrorException {
    return runResponseData(Long.MAX_VALUE);
  }

  /**
   *
   * @param maximumResponseLength definition in {@link #maximumResponseLength}
   *
   * @throws ErrorException
   */
  @Override
  public ArrayList<RelevantPrecinct> runResponseData(long maximumResponseLength) throws ErrorException {
    return super.runResponseData(maximumResponseLength);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    String str = "";
    str = getClass().getName() + " [";
    str += super.toString();

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
  @Override
  public void list(PrintStream out) {

    out.println("-- Proxy Window Scaling Factor --");
    super.list(out);

    out.flush();
  }

  // ============================ private methods ==============================
}
