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
package CADI.Proxy.Server;

import CADI.Common.Network.JPIP.ViewWindowField;
import java.io.PrintStream;

import CADI.Common.Session.ServerSideSessionTarget;
import CADI.Proxy.LogicalTarget.JPEG2000.ProxyJPEG2KCodestream;
import CADI.Server.Session.ServerJPIPChannel;
import java.util.ArrayList;

/**
 * This class is used to save information about a logical target that
 * belongs to a session.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.2 2011/02/14
 */
public class ProxyClientSessionTarget extends ServerSideSessionTarget {

  /**
   * This attribute is used to store a historic of WOIs requested by clients.
   * <p>
   * Its usefulness is to be used by the prefetching module.
   */
  private ArrayList<ViewWindowField> woiHistory = null;

  /**
   * Is the maximum number of WOIs to be recorded in the historic.
   */
  private int MAX_HISTORY_RECORDS = 10;
  

  // ============================= public methods ==============================
  /**
   * Constructor.
   * 
   * @param tid
   * @param codestream
   */
  public ProxyClientSessionTarget(String tid, ProxyJPEG2KCodestream codestream) {
    this(tid, codestream, "jpp-stream");
  }

  /**
   * Constructor.
   *
   * @param tid
   * @param codestream
   * @param returnType
   */
  public ProxyClientSessionTarget(String tid, ProxyJPEG2KCodestream codestream,
                                  String returnType) {
    this(tid, codestream, returnType, false);
  }

  /**
   * Constructor.
   * 
   * @param tid
   * @param codestream
   * @param returnType
   * @param recordHistoric
   */
  public ProxyClientSessionTarget(String tid, ProxyJPEG2KCodestream codestream,
                                  String returnType, boolean recordHistoric) {
    super(tid, returnType);

    cache = new ProxyCacheModel(codestream);
    if (recordHistoric) {
      woiHistory = new ArrayList<ViewWindowField>();
    }
  }

  /**
   * Returns the cache object.
   *
   * @return the cache object.
   */
  public ProxyCacheModel getCache() {
    return (ProxyCacheModel) cache;
  }

  /**
   *
   * @return
   */
  @Override
  public String newChannel() {
    return newChannel("http");
  }

  /**
   *
   * @param transport
   * @return
   */
  @Override
  public String newChannel(String transport) {
    ServerJPIPChannel channel = new ServerJPIPChannel(transport);
    channels.put(channel.getCid(), channel);

    return channel.getCid();
  }

  /**
   * Removes all the attributes.
   */
  @Override
  public void remove() {
    super.remove();
    cache.reset();
    if (woiHistory != null) woiHistory.clear();
  }

  /**
   * Adds a new WOI to the historic.
   * 
   * @param woi the WOI to be added.
   */
  public void addWOIToHistory(ViewWindowField woi) {
    if (woiHistory.size() >= MAX_HISTORY_RECORDS) {
      woiHistory.remove(0);
    }
    woiHistory.add(woi);
  }

  /**
   * Returns the {@link #woisHistoric} attribute.
   *
   * @return definition in {@link #woisHistoric}.
   */
  public ArrayList<ViewWindowField> getWOIHistory() {
    return new ArrayList<ViewWindowField>(woiHistory);
  }

  /**
   *
   * @return
   */
  public ViewWindowField getLastWOIHistory() {
    if (woiHistory.size() > 0)
      return woiHistory.get(woiHistory.size() - 1);

    return null;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str += getClass().getName() + " [";
    str += super.toString();
    //str += cache.toString();
    str += " cache=" + "<<< Not displayed >>>";
    str += "]";

    return str;
  }

  /**
   * Prints this Proxy Session Target out to the specified output stream. This
   * method is useful for debugging.
   *
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Proxy session target --");

    super.list(out);
    //cache.list(out);
    out.println("cache: " + "<<< Not displayed >>>");

    out.flush();
  }
}
