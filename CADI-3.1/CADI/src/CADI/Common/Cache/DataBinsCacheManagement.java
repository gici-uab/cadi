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
package CADI.Common.Cache;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream;
import CADI.Common.Network.JPIP.ClassIdentifiers;
import CADI.Common.Network.JPIP.JPIPMessage;
import CADI.Common.Network.JPIP.ViewWindowField;

/**
 * This class implements some useful method for the maganement of a cache of
 * data-bins. It has been conceived so that it can be inherited by other
 * classes increasing their functionality.
 *
 * There are available three kind of methods:
 * <ul>
 * <li>for adding new segments of data-bins, like {@link #addJPIPMessage(JPIPMessage)}.
 * <li>for getting data-bins, like {@link #getDataBin(int, long)}.
 * <li>for getting a description of the cache status, like {@link #getCacheDescriptor(ViewWindowField, int, int)}.
 * </ul>
 *
 * <p>
 * Usage example:<br>
 * &nbsp; constructor<br>
 * &nbsp; setJPEG2KCodestream<br>
 * &nbsp; [add methods] / [get methods]<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.3 2012/03/09
 */
public class DataBinsCacheManagement {

  /**
   * Contains the main header codestream.
   */
  protected MainHeaderDataBin mainHeaderDataBin = null;

  /**
   * Is a hashtable contains the received data for each precinct data-bin.
   * <p>
   * The key of the hash is the unique precinct identifier (see
   * {@link CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}).
   * The hash's value is an object with the precinct data.
   */
  protected Map<Long, PrecinctDataBin> precinctsDataBins = null;

  /**
   * Stores the tile header data received.
   * <p>
   * The hash's key is the tile identifier. And the value is an object to save
   * the tile header data-bin.
   */
  protected Map<Long, TileHeaderDataBin> tileHeaderDataBins = null;

  protected Map<Long, MetaDataBin> metaDataBins = null; // Maybe a Tree is better than a Map

  /**
   * Definition in {@link CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream}.
   * <p>
   * This object can be only read and it cannot be modified or removed
   * because it is a shared object used by other classes.
   */
  protected JPEG2KCodestream codestream = null;

  /**
   *  Allowed values for the form of the cache bin descriptor.
   *  <p>
   *  Further information, see ISO/IEC 15444-9 sections C.8.1.2 y C.8.1.3
   */
  public static final int NO_CACHE = 0;

  public static final int EXPLICIT_FORM = 1;

  public static final int IMPLICIT_FORM = 2;

  /**
   * Allowed values for the qualifier of the cache bin descriptor.
   * <p>
   * Further information, see ISO/IEC 15444-9 sections C.8.1.2 y C.8.1.3
   */
  public static final int WILDCARD = 1;

  public static final int INDEX_RANGE = 2;

  public static final int NUMBER_OF_LAYERS = 3;

  public static final int NUMBER_OF_BYTES = 4;

  // INTERNAL ATTRIBUTES
  /**
   * Mutex used to lock the cache.
   */
  private ReentrantLock mutex = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public DataBinsCacheManagement() {
    mainHeaderDataBin = new MainHeaderDataBin();
    precinctsDataBins = new HashMap<Long, PrecinctDataBin>();
    tileHeaderDataBins = new HashMap<Long, TileHeaderDataBin>();
    metaDataBins = new HashMap<Long, MetaDataBin>();
    mutex = new ReentrantLock();
  }

  /**
   * Sets the {@link #codestream} attribute.
   *
   * @param codestream definition in {@link #codestream}.
   */
  public final void setJPEG2KCodestream(JPEG2KCodestream codestream) {
    // Check and copy input parameters
    if (codestream == null) {
      throw new NullPointerException();
    }
    this.codestream = codestream;
  }

  /**
   * Adds a segment of a data-bin given as a JPIP message.
   *
   * @param jpipMessage definition in {@link CADI.Common.Network.JPIP.JPIPMessage}.
   */
  public void addJPIPMessage(JPIPMessage jpipMessage) {

    byte[] jpipMessageBody = jpipMessage.messageBody;
    int classIdentifier = (jpipMessage.header.classIdentifier / 2) * 2;
    long inClassIdentifier = jpipMessage.header.inClassIdentifier;
    long offset = jpipMessage.header.msgOffset;
    boolean completeDataBin = jpipMessage.header.isLastByte;
    int aux = jpipMessage.header.Aux;

    // Check input parameters
    if (inClassIdentifier < 0) {
      throw new IllegalArgumentException("inClassIdentifier cannot be negative");
    }
    if (offset < 0) {
      throw new IllegalArgumentException("offset cannot be negative");
    }
    if ((classIdentifier < 0) || (classIdentifier > 8)) {
      throw new IllegalArgumentException("classIdentifier must be between 0 and 8");
    }

    switch (classIdentifier) {

      case ClassIdentifiers.PRECINCT:
        if (jpipMessageBody != null) {
          PrecinctDataBin dataBin = precinctsDataBins.get(inClassIdentifier);
          if (dataBin == null) {
            if (codestream == null) {
              dataBin = new PrecinctDataBin(inClassIdentifier);
            } else {
              dataBin = new PrecinctDataBin(inClassIdentifier, codestream.getNumLayers());
            }
            precinctsDataBins.put(inClassIdentifier, dataBin);
          }
          dataBin.lock();
          dataBin.addStream(jpipMessageBody, offset, completeDataBin, aux);
          dataBin.unlock();
        }
        break;

      case ClassIdentifiers.TILE_HEADER:
        if (inClassIdentifier != 0) {
          System.err.println("ONLY ONE TILE PER IMAGE IS ALLOWED");
          assert (true);
        }
        if (jpipMessageBody != null) {
          TileHeaderDataBin thStream = tileHeaderDataBins.get(inClassIdentifier);
          if (thStream == null) {
            thStream = new TileHeaderDataBin();
            tileHeaderDataBins.put(inClassIdentifier, thStream);
          }
          thStream.addStream(jpipMessageBody, offset, completeDataBin);
        }
        break;

      case ClassIdentifiers.TILE:
        break;

      case ClassIdentifiers.MAIN_HEADER:
        if (mainHeaderDataBin == null) {
          mainHeaderDataBin = new MainHeaderDataBin();
        }
        mainHeaderDataBin.addStream(jpipMessageBody, offset, completeDataBin);
        break;

      case ClassIdentifiers.METADATA:
        if (jpipMessageBody != null) {
          System.out.println(jpipMessage.toString());
          MetaDataBin metaDataStream = metaDataBins.get(inClassIdentifier);
          if (metaDataStream == null) {
            metaDataStream = new MetaDataBin();
            metaDataBins.put(inClassIdentifier, metaDataStream);
          }
          metaDataStream.addStream(jpipMessageBody, offset, completeDataBin);
        }
        break;

      default:
        assert (true);
    }
  }

  /**
   * Returns the byte array of the data-bin identified by <code>
   * classIdentifier</code> and the <code>inClassIdentifier</code>.
   * If the data-bin is not in the cache, a <code>null</code> pointer is
   * returned.
   *
   * @param classIdentifier definition in {@linkplain CADI.Common.Network.JPIP.JPIPMessageHeader#classIdentifier}
   * @param inClassIdentifier definition in {@linkplain CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}
   *
   * @return the byte array of the data-bin. A <code> null</code> pointer if
   * 	the data-bin is not in the cache.
   */
  public DataBin getDataBin(int classIdentifier, long inClassIdentifier) {
    DataBin dataBin = null;

    switch (classIdentifier) {

      case ClassIdentifiers.PRECINCT:
        dataBin = precinctsDataBins.get(inClassIdentifier);
        break;

      case ClassIdentifiers.TILE_HEADER:
        dataBin = tileHeaderDataBins.get(inClassIdentifier);
        break;

      case ClassIdentifiers.TILE:
        break;

      case ClassIdentifiers.MAIN_HEADER:
        dataBin = mainHeaderDataBin;
        break;

      case ClassIdentifiers.METADATA:
        dataBin = metaDataBins.get(inClassIdentifier);
        break;

      default:
        assert (true);
    }

    return dataBin;
  }

  /**
   * Check if a data bin is complete.
   * 
   * @param classIdentifier definition in {@linkplain CADI.Common.Network.JPIP.JPIPMessageHeader#classIdentifier}
   * @param inClassIdentifier definition in {@linkplain CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}
   * 
   * @return <code>true</code> if the data bin is complete. Otherwise, returns
   *          <code>false</code>.
   */
  public final boolean isComplete(int classIdentifier, long inClassIdentifier) {

    switch (classIdentifier) {

      case ClassIdentifiers.PRECINCT:
        return (precinctsDataBins.get(inClassIdentifier) != null)
                ? precinctsDataBins.get(inClassIdentifier).complete
                : false;
      case ClassIdentifiers.TILE_HEADER:
        return (tileHeaderDataBins.get(inClassIdentifier) != null)
                ? tileHeaderDataBins.get(inClassIdentifier).complete
                : false;
      case ClassIdentifiers.TILE:
        break;
      case ClassIdentifiers.MAIN_HEADER:
        return mainHeaderDataBin != null
                ? mainHeaderDataBin.complete
                : false;
      case ClassIdentifiers.METADATA:
        return (metaDataBins.get(inClassIdentifier) != null)
                ? metaDataBins.get(inClassIdentifier).complete
                : false;
      default:
        assert (true);
    }

    return false;
  }

  /**
   * Returns the length of a data bin.
   * 
   * @param classIdentifierdefinition in {@linkplain CADI.Common.Network.JPIP.JPIPMessageHeader#classIdentifier}
   * @param inClassIdentifier definition in {@linkplain CADI.Common.Network.JPIP.JPIPMessageHeader#inClassIdentifier}
   * 
   * @return 
   */
  public final long getDatabinLength(int classIdentifier, long inClassIdentifier) {

    // Check input parameters
    if (classIdentifier < 0) {
      throw new IllegalArgumentException("classIdentifier cannot be negative");
    }
    if (inClassIdentifier < 0) {
      throw new IllegalArgumentException("inClassIdentifier cannot be negative");
    }

    switch (classIdentifier) {

      case ClassIdentifiers.PRECINCT:
        if (precinctsDataBins.containsKey(inClassIdentifier)) {
          return precinctsDataBins.get(inClassIdentifier).getLength();
        }
        break;
      case ClassIdentifiers.TILE_HEADER:
        if (tileHeaderDataBins != null) {
          if (tileHeaderDataBins.containsKey(inClassIdentifier)) {
            return tileHeaderDataBins.get(inClassIdentifier).getLength();
          }
        }
        break;

      case ClassIdentifiers.TILE:
        assert (true);
        break;

      case ClassIdentifiers.MAIN_HEADER:
        if (inClassIdentifier != 0) {
          throw new IllegalArgumentException("inClassIdentifier cannot be negative");
        }
        return mainHeaderDataBin.getLength();

      case ClassIdentifiers.METADATA:
        if (metaDataBins.containsKey(inClassIdentifier)) {
          return metaDataBins.get(inClassIdentifier).getLength();
        }
   
      default:
        assert (true);
    }

    return -1;
  }

  /**
   * 
   * @param classIdentifier definition in {@linkplain CADI.Common.Network.JPIP.JPIPMessageHeader#classIdentifier}
   * @return 
   */
  public final Set<Long> keySet(int classIdentifier) {
    Set<Long> keys = null;

    switch (classIdentifier) {
      case ClassIdentifiers.PRECINCT:
        keys = precinctsDataBins.keySet();
        break;
      case ClassIdentifiers.TILE_HEADER:
        keys = tileHeaderDataBins.keySet();
        break;
      case ClassIdentifiers.TILE:
        break;
      case ClassIdentifiers.MAIN_HEADER:
        break;
      case ClassIdentifiers.METADATA:
        break;
      default:
        assert (true);
    }

    return keys;
  }

  /**
   * Sets the attributes to its initial values.
   */
  public void reset() {
    mainHeaderDataBin.reset();

    for (Long inClassIdentifier : precinctsDataBins.keySet()) {
      precinctsDataBins.get(inClassIdentifier).reset();
    }
    precinctsDataBins.clear();

    for (Long inClassIdentifier : tileHeaderDataBins.keySet()) {
      tileHeaderDataBins.get(inClassIdentifier).reset();
    }
    tileHeaderDataBins.clear();
  }

  /**
   * This method is used to check if a requested window of interest can
   * be retrieved from the data stored in the cache.
   *
   * @param viewWindow definition in {@link CADI.Common.Network.JPIP.ViewWindowField}.
   * 
   * @return it will return <code>true</code> if the WOI can be recovered.
   * 	Otherwise, it will return <code>false</code>.
   */
  public boolean isInCache(ViewWindowField viewWindow) {
    // Check input parameters
    if (viewWindow == null) {
      throw new NullPointerException();
    }
    if (codestream == null) {
      return false;
    }

    // Check whether the main header is available
    if (!isComplete(ClassIdentifiers.MAIN_HEADER, 0)) {
      return false;
    }

    ViewWindowField auxViewWindow = new ViewWindowField(viewWindow);
    if (auxViewWindow.roff[0] < 0) {
      auxViewWindow.roff[0] = 0;
      auxViewWindow.roff[1] = 0;
    }

    if (auxViewWindow.rsiz[0] < 0) {
      auxViewWindow.rsiz[0] = auxViewWindow.fsiz[0];
      auxViewWindow.rsiz[1] = auxViewWindow.fsiz[1];
    }

    if (auxViewWindow.layers < 0) {
      auxViewWindow.layers = codestream.getNumLayers();
    }

    // Find relevant precincts
    if ((auxViewWindow.fsiz[0] > 0) || (auxViewWindow.fsiz[1] > 0)) {
      ArrayList<Long> relevantPrecincts = codestream.findRelevantPrecincts(auxViewWindow);

      // Check if relevant precincts are in cache
      for (long inClassIdentifier : relevantPrecincts) {
        PrecinctDataBin dataBin = (PrecinctDataBin) getDataBin(DataBin.PRECINCT, inClassIdentifier);

        if (dataBin == null) {
          relevantPrecincts.clear();
          return false;
        }

        if (dataBin.getNumCompletePackets() < auxViewWindow.layers) {
          relevantPrecincts.clear();
          return false;
        }
      }
    }

    return true;
  }

  /**
   *
   * @param viewWindow
   * @param type allowed values: {@link #NO_CACHE}, {@link #EXPLICIT_FORM},
   *          {@link #IMPLICIT_FORM}.
   * @param subtype allowed values: {@link #WILDCARD}, {@link #INDEX_RANGE},
   *          {@link #NUMBER_OF_LAYERS}, or {@link #NUMBER_OF_BYTES}.
   * @return
   */
  public ArrayList<ModelElement> getCacheDescriptor(ViewWindowField viewWindow,
                                                    int form, int qualifier) {

    // Check input parameters
    if (viewWindow == null) {
      throw new NullPointerException();
    }
    if (!((form == EXPLICIT_FORM) || (form == IMPLICIT_FORM))) {
      throw new IllegalArgumentException();
    }
    if (!((qualifier == WILDCARD) || (qualifier == INDEX_RANGE)
            || (qualifier == NUMBER_OF_LAYERS) || (qualifier == NUMBER_OF_BYTES))) {
      throw new IllegalArgumentException();
    }

    ArrayList<ModelElement> cacheDescriptor = null;
    ArrayList<Long> relevantPrecincts = null;
    if (codestream != null) {
      if ((viewWindow.fsiz[0] > 0) || (viewWindow.fsiz[1] > 0)) {
        relevantPrecincts = codestream.findRelevantPrecincts(viewWindow);
      }
    }

    switch (form) {
      case EXPLICIT_FORM:
        if (codestream != null) {
          cacheDescriptor = explicitForm(relevantPrecincts, qualifier);
        } else {
          cacheDescriptor = explicitForm(qualifier);
        }
        break;
      case IMPLICIT_FORM:
        if (codestream != null) {
          cacheDescriptor = implicitForm(relevantPrecincts);
        }
        break;
      default:
        throw new IllegalArgumentException("Cache form can only take a EXPLICIT_FORM or a IMPLICIT_FORM value");
    }

    return cacheDescriptor;
  }

  /**
   * Locks all the cache's data bins.
   * <p>
   * Since all data bins are locked, it must be used carefully.
   */
  public void lock() {
    mutex.lock();
  }

  /**
   * Unlocks all the cache's data bins.
   */
  public void unlock() {
    mutex.unlock();
  }

  /**
   * 
   * @return
   */
  public boolean isLocked() {
    return mutex.isLocked();
  }

  /**
   * 
   * @return
   */
  public boolean isHeldByCurrentThread() {
    return mutex.isHeldByCurrentThread();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";

    str += "Main header=" + mainHeaderDataBin.toString();

    str += ", Precinct=";
    for (Map.Entry<Long, PrecinctDataBin> entry : precinctsDataBins.entrySet()) {
      str += entry.getValue().toString();
    }

    str += ", Tile header=";
    for (Map.Entry<Long, TileHeaderDataBin> entry : tileHeaderDataBins.entrySet()) {
      str += entry.getValue().toString();
    }
    
    str += ", Meta data=";
    for (Map.Entry<Long, MetaDataBin> entry : metaDataBins.entrySet()) {
      str += entry.getValue().toString();
    }

    str += "]";

    return str;
  }

  /**
   * Prints this cache out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Data Bins Cache --");

    out.println("Main header:");
    this.mainHeaderDataBin.list(System.out);

    out.println("Precinct: ");
    for (Map.Entry<Long, PrecinctDataBin> entry : precinctsDataBins.entrySet()) {
      entry.getValue().list(out);
    }

    out.println("Tile header");
    for (Map.Entry<Long, TileHeaderDataBin> entry : tileHeaderDataBins.entrySet()) {
      entry.getValue().list(out);
    }
    
    out.println("Meta data:");
    for (Map.Entry<Long, MetaDataBin> entry : metaDataBins.entrySet()) {
      entry.getValue().list(out);
    }

    out.flush();
  }

  // ============================ private methods ==============================
  /**
   * Gets the cache descriptor following the explicit form.
   *
   * @param relevantPrecincts an array list with the relevant precincts which
   * 			will be included in the cache descriptor.
   * @param subtype allowed values: {@link #WILDCARD}, {@link #INDEX_RANGE},
   *          {@link #NUMBER_OF_LAYERS}, or {@link #NUMBER_OF_BYTES}.
   *
   * @return an array list with the cache descriptors.
   */
  protected ArrayList<ModelElement> explicitForm(ArrayList<Long> relevantPrecincts, int qualifier) {

    ArrayList<ModelElement> cacheDescriptor = new ArrayList<ModelElement>();
    ModelElement cacheDescriptorElement = null;


    // MAIN HEADER
    cacheDescriptorElement = new ModelElement(BinDescriptor.EXPLICIT_FORM);
    cacheDescriptorElement.explicitForm.classIdentifier = BinDescriptor.MAIN_HEADER;
    if (isComplete(ClassIdentifiers.MAIN_HEADER, 0)) {
      cacheDescriptorElement.additive = true;
    } else {
      cacheDescriptorElement.additive = false;
    }
    cacheDescriptor.add(cacheDescriptorElement);


    // PRECINCTS
    if (relevantPrecincts != null) {
      for (long inClassIdentifier : relevantPrecincts) {
        PrecinctDataBin dataBin = (PrecinctDataBin) getDataBin(DataBin.PRECINCT, inClassIdentifier);

        if (dataBin != null) {
          cacheDescriptorElement = new ModelElement(BinDescriptor.EXPLICIT_FORM);
          cacheDescriptorElement.explicitForm.classIdentifier = BinDescriptor.PRECINCT;
          cacheDescriptorElement.explicitForm.inClassIdentifier = dataBin.getInClasIdentifier();
          if (qualifier == NUMBER_OF_BYTES) {
            cacheDescriptorElement.explicitForm.numberOfBytes = (int) dataBin.getLength();
          } else if (qualifier == NUMBER_OF_LAYERS) {
            cacheDescriptorElement.explicitForm.numberOfLayers = dataBin.getNumCompletePackets();
          } else if (qualifier == WILDCARD) {	// Wildcard
            assert (true);
          } else {
            assert (true);
          }
          cacheDescriptor.add(cacheDescriptorElement);
        }
      }
    }

    // TILE HEADER
    if (tileHeaderDataBins != null) {
      for (Map.Entry<Long, TileHeaderDataBin> entry : tileHeaderDataBins.entrySet()) {
        cacheDescriptorElement = new ModelElement(BinDescriptor.EXPLICIT_FORM);
        cacheDescriptorElement.explicitForm.classIdentifier = BinDescriptor.TILE_HEADER;
        cacheDescriptorElement.explicitForm.inClassIdentifier = entry.getKey();
        cacheDescriptorElement.additive = true;
        cacheDescriptorElement.explicitForm.numberOfBytes = (int) entry.getValue().getLength();
        cacheDescriptor.add(cacheDescriptorElement);
      }
    }

    return cacheDescriptor;
  }

  /**
   * Gets the cache descriptor following the explicit form.
   * 
   * @param subtype allowed values: {@link #WILDCARD}, {@link #INDEX_RANGE},
   *          {@link #NUMBER_OF_LAYERS}, or {@link #NUMBER_OF_BYTES}.
   *
   * @return an array list with the cache descriptors.
   */
  protected ArrayList<ModelElement> explicitForm(int qualifier) {

    ArrayList<ModelElement> cacheDescriptor = new ArrayList<ModelElement>();
    ModelElement cacheDescriptorElement = null;


    // MAIN HEADER
    cacheDescriptorElement = new ModelElement(BinDescriptor.EXPLICIT_FORM);
    cacheDescriptorElement.explicitForm.classIdentifier = BinDescriptor.MAIN_HEADER;
    if (isComplete(ClassIdentifiers.MAIN_HEADER, 0)) {
      cacheDescriptorElement.additive = true;
    } else {
      cacheDescriptorElement.additive = false;
    }
    cacheDescriptor.add(cacheDescriptorElement);

    // PRECINCTS
    for (Long inClassIdentifier : precinctsDataBins.keySet()) {
      PrecinctDataBin dataBin = (PrecinctDataBin) getDataBin(DataBin.PRECINCT, inClassIdentifier);

      if (dataBin.getNumCompletePackets() > 0) {
        cacheDescriptorElement = new ModelElement(BinDescriptor.EXPLICIT_FORM);
        cacheDescriptorElement.explicitForm.classIdentifier = BinDescriptor.PRECINCT;
        cacheDescriptorElement.explicitForm.inClassIdentifier = dataBin.getInClasIdentifier();
        if (qualifier == NUMBER_OF_BYTES) {
          cacheDescriptorElement.explicitForm.numberOfBytes = (int) dataBin.getLength();
        } else if (qualifier == NUMBER_OF_LAYERS) {
          cacheDescriptorElement.explicitForm.numberOfLayers = dataBin.getNumCompletePackets();
        } else if (qualifier == WILDCARD) {	// Wildcard
          assert (true);
        } else {
          assert (true);
        }
        cacheDescriptor.add(cacheDescriptorElement);
      }
    }

    // TILE HEADER
    if (tileHeaderDataBins != null) {
      for (Map.Entry<Long, TileHeaderDataBin> entry : tileHeaderDataBins.entrySet()) {
        cacheDescriptorElement = new ModelElement(BinDescriptor.EXPLICIT_FORM);
        cacheDescriptorElement.explicitForm.classIdentifier = BinDescriptor.TILE_HEADER;
        cacheDescriptorElement.explicitForm.inClassIdentifier = entry.getKey();
        cacheDescriptorElement.additive = true;
        cacheDescriptorElement.explicitForm.numberOfBytes = (int) entry.getValue().getLength();
        cacheDescriptor.add(cacheDescriptorElement);
      }
    }

    return cacheDescriptor;
  }

  /**
   * Gets the cache descriptor following the implicit form.
   *
   * @param relevantPrecincts an array list with the relevant precincts which
   * 			will be included in the cache descriptor.
   *
   * @return an array list with the cache descriptors.
   */
  protected ArrayList<ModelElement> implicitForm(ArrayList<Long> relevantPrecincts) {

    ArrayList<ModelElement> cacheDescriptor = new ArrayList<ModelElement>();

    // PRECINCTS
    ModelElement cacheDescriptorElement = null;
    if (relevantPrecincts != null) {
      for (long inClassIdentifier : relevantPrecincts) {
        PrecinctDataBin dataBin = (PrecinctDataBin) getDataBin(DataBin.PRECINCT, inClassIdentifier);

        if (dataBin != null) {
          if (dataBin.getNumCompletePackets() > 0) {
            int[] tcrp = codestream.findTCRP(inClassIdentifier);
            cacheDescriptorElement = new ModelElement(BinDescriptor.IMPLICIT_FORM);
            cacheDescriptorElement.implicitForm.firstTilePos = cacheDescriptorElement.implicitForm.lastTilePos = tcrp[0];
            cacheDescriptorElement.implicitForm.firstComponentPos = cacheDescriptorElement.implicitForm.lastComponentPos = tcrp[1];
            cacheDescriptorElement.implicitForm.firstResolutionLevelPos = cacheDescriptorElement.implicitForm.lastResolutionLevelPos = tcrp[2];
            cacheDescriptorElement.implicitForm.firstPrecinctPos = cacheDescriptorElement.implicitForm.lastPrecinctPos = tcrp[3];
            cacheDescriptorElement.implicitForm.numberOfLayers = dataBin.getNumCompletePackets();
            cacheDescriptor.add(cacheDescriptorElement);
          }
        }
      }
    }
    return cacheDescriptor;
  }

  /**
   * Useful method for printing out a ByteStream. Only for debugging purposes.
   *
   * @param buffer the byte array to be printed.
   */
  private static void printByteStream(byte[] buffer) {

    for (int index = 0; index < buffer.length; index++) {
      if ((0xFF & buffer[index]) < 16) {
        System.out.print("0");
      }
      System.out.print(Integer.toHexString(0xFF & buffer[index]));
    }
  }
}
