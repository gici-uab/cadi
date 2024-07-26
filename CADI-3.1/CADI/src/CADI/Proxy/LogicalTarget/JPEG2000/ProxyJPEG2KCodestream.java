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

import CADI.Common.LogicalTarget.JPEG2000.JPCParameters;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2000Util;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2KCodestream;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2KComponent;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel;
import CADI.Common.LogicalTarget.JPEG2000.JPEG2KTile;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Common.Util.ArraysUtil;
import CADI.Common.Util.CADIDimension;
import CADI.Common.Util.CADIRectangle;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2010/11/13
 */
public class ProxyJPEG2KCodestream extends JPEG2KCodestream {

  // ============================= public methods ==============================
  /**
   * Constructor.
   * 
   * @param identifier
   * @param jpcParameters
   */
  public ProxyJPEG2KCodestream(int identifier, JPCParameters jpcParameters) {
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
      tiles.put(index, new ProxyJPEG2KTile(this, index));
    }
  }

  @Override
  public ProxyJPEG2KTile getTile(int index) {
    return (ProxyJPEG2KTile) tiles.get(index);
  }

  /**
   * 
   * @param woi
   * @param sizParameters
   * @param codParameters
   * @return
   *
   * @deprecated
   */
  public ArrayList<ViewWindowField> getSubWOIs(ViewWindowField woi) {

    if (woi == null) {
      throw new NullPointerException();
    }

    ArrayList<ViewWindowField> subWOIs = new ArrayList<ViewWindowField>();

    /*System.out.println("\n============================================");
    System.out.println(sizParameters.toString());
    System.out.println(codParameters.toString());
    woi.list(System.out);*/

    int discardLevels = determineNumberOfDiscardLevels(woi.fsiz, woi.roundDirection);
    int[] components = ArraysUtil.rangesToIndexes(woi.comps);
    ArrayList<Integer> relevantTiles = calculateRelevantTiles(woi, discardLevels);

    //actualViewWindow.list(System.out);
    //System.out.println("Discard levels="+discardLevels);

    JPEG2KTile tileObj = null;
    JPEG2KComponent compObj = null;
    JPEG2KResolutionLevel rLevelObj = null;

    // Calculate the max. resol. level over the tile-components
    int maxRLevel = 0;
    for (int tileIndex : relevantTiles) {
      tileObj = tiles.get(tileIndex);
      for (int comp : components) {
        if (maxRLevel < tileObj.getComponent(comp).getWTLevels()) {
          maxRLevel = tileObj.getComponent(comp).getWTLevels();
        }
      }
    }


    for (int tileIndex : relevantTiles) {
      tileObj = tiles.get(tileIndex);

      for (int rLevel = 0; rLevel <= maxRLevel; rLevel++) {

        for (int component : components) {
          //System.out.println("\tTile="+tile+" rLevel="+rLevel+" component="+component);					
          compObj = tileObj.getComponent(component);
          rLevelObj = compObj.getResolutionLevel(rLevel);

          // Max. resol. levels of this tile-component
          int maxRLevelsComponent = compObj.getWTLevels();
          //System.out.println("\t\tmaxRLevelsComponent="+maxRLevelsComponent);


          if (rLevel >= discardLevels) {
            // Discard the r highest res. levels  
            if (rLevel > maxRLevelsComponent - discardLevels) {
              continue;
            }
          } else {
            // It should discard more res. levels than ones available in this
            // tile-component. Then, includes only the LL sub-band.
            if (rLevel != 0) {
              continue;
            }
          }

          CADIDimension frameSize = JPEG2000Util.calculateFrameSize(
                  getXSize(), getYSize(),
                  getXOSize(), getYOSize(),
                  maxRLevelsComponent - rLevel);
          //System.out.println("FAME SIZE="+frameSize.toString());


          // Translation of requested region with from the desired frame size reference grid
          // into the sub-sampled reference grid at this frame size
          CADIRectangle supportRegion = new CADIRectangle(woi.roff[0], woi.roff[1],
                  woi.rsiz[0], woi.rsiz[1]);
          calculateSupportRegion(tileIndex, component, rLevel, supportRegion, discardLevels);
          //System.out.println("SUPPORT REGION="+supportRegion.toString());


          // Number of precincts for each resolution level					
          int precinctWidth = rLevelObj.getPrecinctWidth();
          int precinctHeight = rLevelObj.getPrecinctHeight();
          //System.out.println("Precinct Sizes: " + precinctSize[X] + " x " + precinctSize[Y]);
          if (precinctWidth > frameSize.width) {
            precinctWidth = frameSize.width;
          }
          if (precinctHeight > frameSize.height) {
            precinctHeight = frameSize.height;
          }
          //System.out.println("Precinct Sizes: " + precinctWidth + " x " + precinctHeight);

          int numPrecinctsWidth = rLevelObj.getNumPrecinctsWide();
          int numPrecinctsHeight = rLevelObj.getNumPrecinctsHeigh();
          //System.out.println("Num Precincts: " + numPrecinctsWidth + " x " + numPrecinctsHeight);

          // Find the start and end precincts
          int startXPrecinct = 0, endXPrecinct = 0;
          int startYPrecinct = 0, endYPrecinct = 0;
          startXPrecinct = (int) (Math.floor((double) supportRegion.x / (double) (precinctWidth)));
          startYPrecinct = (int) (Math.floor((double) supportRegion.y / (double) (precinctHeight)));
          endXPrecinct = (int) (Math.ceil((double) (supportRegion.x + supportRegion.width) / (double) (precinctWidth)));	// not included
          endYPrecinct = (int) (Math.ceil((double) (supportRegion.y + supportRegion.height) / (double) (precinctHeight)));	// not included
          //System.out.println("Start Precinct: " + startPrecinct[X] + " " + startPrecinct[Y] + "     End Precinct: " + (endPrecinct[X]-1) + " " + (endPrecinct[Y]-1) );

          for (int yPrecinct = startYPrecinct; yPrecinct < endYPrecinct; yPrecinct++) {
            for (int xPrecinct = startXPrecinct; xPrecinct < endXPrecinct; xPrecinct++) {
              long inClassIdentifier = rLevelObj.getInClassIdentifier(yPrecinct * numPrecinctsWidth + xPrecinct);
              //System.out.println("\tJPIP MESSAGE: inClassID=" + inClassIdentifier + " -> z="+component + " r="+rLevel+" xp="+ xPrecinct+" yp="+yPrecinct);

              ViewWindowField subWOI = new ViewWindowField();
              subWOI.fsiz[0] = woi.fsiz[0];
              subWOI.fsiz[1] = woi.fsiz[1];
              subWOI.roff[0] = xPrecinct * precinctWidth;
              subWOI.roff[1] = yPrecinct * precinctHeight;
              subWOI.rsiz[0] = precinctWidth;
              subWOI.rsiz[1] = precinctHeight;
              subWOIs.add(subWOI);
            }
          }
        }
      }
    }

    return subWOIs;
  }

  /**
   * 
   * @param viewWindow
   * @return
   */
  public ArrayList<Long> ViewWindowToInClassIdentifier(ViewWindowField viewWindow) {
    return this.findRelevantPrecincts(viewWindow);

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
   * Prints this Proxy JPEG2K Codestream out to the specified output stream.
   * This method is useful for debugging.
   * 
   * @param out an output stream.
   */
  @Override
  public void list(PrintStream out) {

    out.println("-- Proxy JPEG2K Codestream --");
    super.list(out);
  }
  // ============================ private methods ==============================
}
