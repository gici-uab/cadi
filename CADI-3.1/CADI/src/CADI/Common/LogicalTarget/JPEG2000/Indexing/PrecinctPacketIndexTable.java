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
package CADI.Common.LogicalTarget.JPEG2000.Indexing;

import CADI.Common.LogicalTarget.JPEG2000.JPEG2KPrecinct;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2009/09/12
 */
public class PrecinctPacketIndexTable {

  /**
   * 
   */
  public Manifest manf = null;

  /**
   * 
   */
  private HashMap<Long, FragmentArrayIndex> faix = null;

  private boolean useCodingPasses = false;

  /**
   * Contains a file pointer for each coding pass in the precinct-subband-codeblok. Indexes
   * means:<br>
   * &nbsp; subband
   * &nbsp; yBlock
   * &nbsp; xBlock
   * &nbsp; coding pass
   * 
   * <p>
   * This attribute is only available when the {@link #isCodingPassesInfo}
   * attribute is <code>true</code>
   */
  private HashMap<Long, long[][][][]> fpCodingPasses = null;

  /**
   * Contains the length of each coding pass belonging to the precinct-subband-codeblock.
   * Indexes are the same as in {@link #filePointersCodingPasses}.
   * <p>
   * This attribute is only available when the {@link #isCodingPassesInfo}
   * attribute is <code>true</code>
   */
  private HashMap<Long, int[][][][]> lenCodingPasses = null;

  /**
   * Number of zero bit planes for each block in a precinct-subband. Each
   * entry maps the precinct identifier with a three-dimensional array
   * with the zero bit planes for each subband-block in the precinct.
   * Indexes of the three-dimensional array mean:
   * &nbsp; subband: 0 - HL, 1 - LH, 2 - HH (ifresolutionLevel == 0 --> 0 - LL) <br>
   * &nbsp; yBlock: block row in the subband <br>
   * &nbsp; xBlock: block column in the subband <br>
   * <p>
   * Only positive values allowed (0 value is possible too. If 0 --> block has
   * not empty/0 bit planes).
   */
  private HashMap<Long, int[][][]> zeroBitPlanes = null;

  // TODO: storage of fpCodingPasses, lenCodingPasses, and zeroBitPlanes must be improved.
  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public PrecinctPacketIndexTable() {
    faix = new HashMap<Long, FragmentArrayIndex>();
  }

  /**
   * Constructor.
   * 
   * @param bh definition in {@link #bh}.
   */
  public PrecinctPacketIndexTable(Manifest manf, HashMap<Long, FragmentArrayIndex> faix) {
    if (manf == null) {
      throw new NullPointerException();
    }
    if (faix == null) {
      throw new NullPointerException();
    }

    this.manf = manf;
    this.faix = faix;
  }

  public void initializeIndexTable(long inClassIdentifier, int numLayers) {
    initializeIndexTable(FragmentArrayIndex.VERSION_0, inClassIdentifier, numLayers);
  }

  public void initializeIndexTable(int version, long inClassIdentifier, int numLayers) {
    faix.put(inClassIdentifier, new FragmentArrayIndex(version, 1, numLayers));
  }

  public void initializeIndexTable(int version, long inClassIdentifier,
                                   boolean useCodingPasses, JPEG2KPrecinct precinctObj) {
    if (useCodingPasses) {
      if (fpCodingPasses == null) {
        fpCodingPasses = new HashMap<Long, long[][][][]>();
        lenCodingPasses = new HashMap<Long, int[][][][]>();
        zeroBitPlanes = new HashMap<Long, int[][][]>();
      }
      int numSubbands = precinctObj.getNumSubbands();
      fpCodingPasses.put(inClassIdentifier, new long[numSubbands][][][]);
      lenCodingPasses.put(inClassIdentifier, new int[numSubbands][][][]);
      zeroBitPlanes.put(inClassIdentifier, new int[numSubbands][][]);
      for (int sb = 0; sb < numSubbands; sb++) {
        int numBlocksHigh = precinctObj.getNumBlocksHigh(numSubbands == 1 ? sb : sb + 1);
        int numBlocksWide = precinctObj.getNumBlocksWide(numSubbands == 1 ? sb : sb + 1);
        fpCodingPasses.get(inClassIdentifier)[sb] = new long[numBlocksHigh][numBlocksWide][];
        lenCodingPasses.get(inClassIdentifier)[sb] = new int[numBlocksHigh][numBlocksWide][];
        zeroBitPlanes.get(inClassIdentifier)[sb] = new int[numBlocksHigh][numBlocksWide];
      }
      this.useCodingPasses = useCodingPasses;
      faix = null;
    } else {
      initializeIndexTable(version, inClassIdentifier, precinctObj.getParent().getParent().getParent().getNumLayers());
    }
  }

  public boolean isInitializedIndexTable(long inClassIdentifier) {
    return (!useCodingPasses ? faix.containsKey(inClassIdentifier)
            : fpCodingPasses.containsKey(inClassIdentifier));
  }

  public void setFilePointer(long inClassIdentifier, int layer, int filePointer) {
    faix.get(inClassIdentifier).setOffset(0, layer, filePointer);
  }

  public void setFilePointer(long inClassIdentifier, int layer, long filePointer) {
    faix.get(inClassIdentifier).setOffset(0, layer, filePointer);
  }

  public void setLength(long inClassIdentifier, int layer, int length) {
    faix.get(inClassIdentifier).setLength(0, layer, length);
  }

  public void setLength(long inClassIdentifier, int layer, long length) {
    faix.get(inClassIdentifier).setLength(0, layer, length);
  }

  public long getFilePointer(long inClassIdentifier, int layer) {
    return faix.get(inClassIdentifier).getOffset(0, layer);
  }

  public int getOffset(long inClassIdentifier, int layer) {
    return (int) getFilePointer(inClassIdentifier, layer);
  }

  public long getLength(long inClassIdentifier, int layer) {
    return faix.get(inClassIdentifier).getLength(0, layer);
  }

  public int getLength(long inClassIdentifier) {
    int len = 0;
    FragmentArrayIndex arrayIndex = faix.get(inClassIdentifier);
    int numLayers = arrayIndex.getNumElementsPerRow();
    for (int l = 0; l < numLayers; l++) {
      len += arrayIndex.getLength(0, l);
    }
    return len;
  }

  public void setFilePointer(long inClassIdentifier, int subband, int yBlock, int xBlock, long[] pointers) {
    int numCodingPasses = 0;
    long[] tmp = fpCodingPasses.get(inClassIdentifier)[subband][yBlock][xBlock];
    if (tmp != null) {
      numCodingPasses = tmp.length;
      long[] tempPointers = new long[numCodingPasses + pointers.length];
      System.arraycopy(tmp, 0, tempPointers, 0, numCodingPasses);
      tmp = null;
      tmp = tempPointers;
      System.arraycopy(pointers, 0, tmp, numCodingPasses, pointers.length);
      fpCodingPasses.get(inClassIdentifier)[subband][yBlock][xBlock] = tmp;
    } else {
      fpCodingPasses.get(inClassIdentifier)[subband][yBlock][xBlock] = pointers;
    }
  }

  public long getFilePointer(long inClassIdentifier, int subband, int yBlock, int xBlock, int cp) {
    return fpCodingPasses.get(inClassIdentifier)[subband][yBlock][xBlock][cp];
  }

  public void setLength(long inClassIdentifier, int subband, int yBlock, int xBlock, int[] lengths) {
    int numCodingPasses = 0;
    int[] tmp = lenCodingPasses.get(inClassIdentifier)[subband][yBlock][xBlock];
    if (tmp != null) {
      numCodingPasses = tmp.length;
      int[] tempPointers = new int[numCodingPasses + lengths.length];
      System.arraycopy(tmp, 0, tempPointers, 0, tmp.length);
      tmp = null;
      tmp = tempPointers;
      System.arraycopy(lengths, 0, tmp, numCodingPasses, lengths.length);
      lenCodingPasses.get(inClassIdentifier)[subband][yBlock][xBlock] = tmp;
    } else {
      lenCodingPasses.get(inClassIdentifier)[subband][yBlock][xBlock] = lengths;
    }
  }

  public int getLength(long inClassIdentifier, int subband, int yBlock, int xBlock, int cp) {
    return lenCodingPasses.get(inClassIdentifier)[subband][yBlock][xBlock][cp];
  }

  /**
   * NOTE: METHOD TO BE DEPRECATED
   * 
   * @param inClassIdentifier
   * @return 
   */
  public int[][][][] getLengths(long inClassIdentifier) {
    return lenCodingPasses.get(inClassIdentifier);
  }

  public void setZeroBitPlanes(long inClassIdentifier, int[][][] zeroBitPlanes) {
    for (int sb = 0; sb < zeroBitPlanes.length; sb++) {
      for (int yb = 0; yb < zeroBitPlanes[sb].length; yb++) {
        for (int xb = 0; xb < zeroBitPlanes[sb][yb].length; xb++) {
          this.zeroBitPlanes.get(inClassIdentifier)[sb][yb][xb] = zeroBitPlanes[sb][yb][xb];
        }
      }
    }
  }

  public int[][][] getZeroBitPlanes(long inClassIdentifier) {
    return zeroBitPlanes.get(inClassIdentifier);
  }

  public int getZeroBitPlanes(long inClassIdentifier, int sb, int yBlock, int xBlock) {
    return zeroBitPlanes.get(inClassIdentifier)[sb][yBlock][xBlock];
  }

  /**
   * Sets the attributes to their initial values.
   */
  public void reset() {
    manf.reset();
    for (Map.Entry<Long, FragmentArrayIndex> entry : faix.entrySet()) {
      entry.getValue().reset();
    }
    faix.clear();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";
    str += manf.toString();
    str += ", mhix=";
    for (Map.Entry<Long, FragmentArrayIndex> entry : faix.entrySet()) {
      str += ", precinct=" + entry.getKey() + " values=" + entry.getValue().toString();
    }
    str += "]";

    return str;
  }

  /**
   * Prints the Precinct Packet Index Table data out to the specified output stream.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Precinct Packet Index Table --");

    //manf.list(out);
    if (faix != null) {
      out.print("faix: ");
      for (Map.Entry<Long, FragmentArrayIndex> entry : faix.entrySet()) {
        out.println("precinct: " + entry.getKey());
        out.println("\tvalues: ");
        entry.getValue().list(out);
      }
    }

    if (fpCodingPasses != null) {
      out.println("File pointers / lengths");
      for (Map.Entry<Long, long[][][][]> entry : fpCodingPasses.entrySet()) {
        long[][][][] fp = entry.getValue();
        out.println("InClassIdentifier=" + entry.getKey());
        if (fp != null) {
          for (int sb = 0; sb < fp.length; sb++) {
            for (int yb = 0; yb < fp[sb].length; yb++) {
              for (int xb = 0; xb < fp[sb][yb].length; xb++) {
                if (fp[sb][yb][xb] == null) {
                  continue;
                }
                for (int cp = 0; cp < fp[sb][yb][xb].length; cp++) {
                  out.println("\tsb=" + sb + " yb=" + yb + " xb=" + xb + " cp=" + cp + " cp=" + fp[sb][yb][xb][cp] + " len=" + lenCodingPasses.get(entry.getKey())[sb][yb][xb][cp]);
                }
              }
            }
          }
        }
      }
    }

    if (zeroBitPlanes != null) {
      out.println("Zero bit planes");
      for (Map.Entry<Long, int[][][]> entry : zeroBitPlanes.entrySet()) {
        int[][][] zbp = entry.getValue();
        out.println("InClassIdentifier=" + entry.getKey());
        if (zbp != null) {
          for (int sb = 0; sb < zbp.length; sb++) {
            for (int yb = 0; yb < zbp[sb].length; yb++) {
              for (int xb = 0; xb < zbp[sb][yb].length; xb++) {
                out.println("\tsb=" + sb + " yb=" + yb + " xb=" + xb + " zbp=" + zbp[sb][yb][xb]);

              }
            }
          }
        }
      }
    }


    out.flush();
  }
}
