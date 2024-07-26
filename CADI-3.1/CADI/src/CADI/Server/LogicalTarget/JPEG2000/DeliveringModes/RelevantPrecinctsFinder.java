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
package CADI.Server.LogicalTarget.JPEG2000.DeliveringModes;

import java.util.ArrayList;

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
 * This class is used to obtain which precincts belong to a WOI and what is the
 * progression order to be delivered. 
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.1 2011/08/21
 */
public class RelevantPrecinctsFinder {

  /**
   * Calculates which are the relevant precincts which belong to the WOI
   * (fsiz, roff, rsiz).
   * <p>
   * Further information, see ISO/IEC 15444-9 section K.4.1
   *
   * @param codestream
   * @param actualViewWindow
   * @param discardLevels
   *
   * @return a array list with the unique precinct number which belongs to the WOI.
   */
  public static ArrayList<Long> TRCPOrder(JPEG2KCodestream codestream,
                                          ViewWindowField actualViewWindow,
                                          int discardLevels) {

    if (codestream == null) {
      throw new NullPointerException();
    }
    if (actualViewWindow == null) {
      throw new NullPointerException();
    }
    if (discardLevels < 0) {
      throw new IllegalArgumentException();
    }

    JPEG2KTile tileObj = null;
    JPEG2KComponent compObj = null;
    JPEG2KResolutionLevel rLevelObj = null;
    ArrayList<Long> relevantPrecincts = new ArrayList<Long>();
    ArrayList<Integer> relevantTiles = codestream.calculateRelevantTiles(actualViewWindow, discardLevels);
    int[] components = null;
    if (actualViewWindow.comps != null) {
      components = ArraysUtil.rangesToIndexes(actualViewWindow.comps);
    } else {
      int maxComps = codestream.getZSize();
      components = new int[maxComps];
      for (int c = 0; c < maxComps; c++) {
        components[c] = c;
      }
    }

    //actualViewWindow.list(System.out);
    //System.out.println("Discard levels="+discardLevels);

    // Calculate the max. resol. level over the tile-components
    int maxRLevel = 0;
    for (int tileIndex : relevantTiles) {
      tileObj = codestream.getTile(tileIndex);
      for (int comp : components) {
        if (maxRLevel < tileObj.getComponent(comp).getWTLevels()) {
          maxRLevel = tileObj.getComponent(comp).getWTLevels();
        }
      }
    }

    for (int tileIndex : relevantTiles) {
      //System.out.println("tile=" + tileIndex); // DEBUG
      tileObj = codestream.getTile(tileIndex);

      for (int rLevel = 0; rLevel <= maxRLevel; rLevel++) {
        //System.out.println("\trLevel=" + rLevel); // DEBUG

        for (int component : components) {
          //System.out.println("\t\tcomponent=" + component); // DEBUG

          compObj = tileObj.getComponent(component);
          rLevelObj = compObj.getResolutionLevel(rLevel);

          // Max. resol. levels of this tile-component
          int maxRLevelsComponent = compObj.getWTLevels();
          //System.out.println("\t\tmaxRLevelsComponent="+maxRLevelsComponent);


          if (maxRLevelsComponent >= discardLevels) {
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
                  codestream.getXSize(), codestream.getYSize(),
                  codestream.getXOSize(), codestream.getYOSize(),
                  maxRLevelsComponent - rLevel);
          //System.out.println("\t\t\tframe size=" + frameSize.toString()); // DEBUG


          // Translation of requested region with from the desired frame size reference grid
          // into the sub-sampled reference grid at this frame size
          CADIRectangle supportRegion = new CADIRectangle(
                  actualViewWindow.roff[0], actualViewWindow.roff[1],
                  actualViewWindow.rsiz[0], actualViewWindow.rsiz[1]);
          codestream.calculateSupportRegion(tileIndex, component, rLevel,
                  supportRegion, discardLevels);
          //System.out.println("\t\t\tsupport region=" + supportRegion.toString()); // DEBUG


          // Number of precincts for each resolution level
          int precinctWidth = rLevelObj.getPrecinctWidth();
          int precinctHeight = rLevelObj.getPrecinctHeight();
          if (precinctWidth > frameSize.width) {
            precinctWidth = frameSize.width;
          }
          if (precinctHeight > frameSize.height) {
            precinctHeight = frameSize.height;
          }
          //System.out.println("\t\t\tprecinct sizes: "+precinctWidth+" x "+precinctHeight); // DEBUG

          int numPrecinctsWidth = rLevelObj.getNumPrecinctsWide();
          int numPrecinctsHeight = rLevelObj.getNumPrecinctsHeigh();
          //System.out.println("\t\t\tnum precincts: " + numPrecinctsWidth + " x " + numPrecinctsHeight);

          // Find the start and end precincts
          int startXPrecinct = 0, endXPrecinct = 0;
          int startYPrecinct = 0, endYPrecinct = 0;
          startXPrecinct = (int)(Math.floor((double)supportRegion.x / (double)(precinctWidth)));
          startYPrecinct = (int)(Math.floor((double)supportRegion.y / (double)(precinctHeight)));
          endXPrecinct = (int)(Math.ceil((double)(supportRegion.x + supportRegion.width) / (double)(precinctWidth)));	// not included
          endYPrecinct = (int)(Math.ceil((double)(supportRegion.y + supportRegion.height) / (double)(precinctHeight)));	// not included
          //System.out.println("\t\t\tstart precinct: "+startXPrecinct+" "+startYPrecinct+"     end precinct: "+(endXPrecinct-1)+" "+(endYPrecinct-1) );

          for (int yPrecinct = startYPrecinct; yPrecinct < endYPrecinct; yPrecinct++) {
            for (int xPrecinct = startXPrecinct; xPrecinct < endXPrecinct; xPrecinct++) {
              //System.out.println("\t\t\t\txp="+ xPrecinct+" yp="+yPrecinct); // DEBUG
              long inClassIdentifier = rLevelObj.getInClassIdentifier(yPrecinct * numPrecinctsWidth + xPrecinct);
              //System.out.println(" => inClassIdentifier=" + inClassIdentifier); // DEBUG
              relevantPrecincts.add(inClassIdentifier);
            }
          }
        }
      }
    }

    //for (RelevantPrecinct rp : relevantPrecincts) System.out.println(rp.toStringSummary()); // DEBUG

    return relevantPrecincts;
  }

  /**
   * Calculates which are the relevants precincts which belong to the WOI
   * (fsiz, roff, rsiz).
   *
   * Further information, see ISO/IEC 15444-9 section K.4.1
   *
   * @param fsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param ySize definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#ySize}.
   * @param xSize definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#xSize}.
   * @param XOsize definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#XOsize}.
   * @param YOsize definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#YOsize}.
   * @param resolutionPrecinctWidths definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#precinctWidths}.
   * @param resolutionPrecinctHeights  definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#precinctHeights}.
   *
   * @return a array list with the unique precinct number which belongs to the WOI.
   */
  public static ArrayList<Long> TCRPOrder(JPEG2KCodestream codestream,
                                          ViewWindowField actualViewWindow,
                                          int discardLevels) {

    assert (discardLevels >= 0);
    if (actualViewWindow == null) {
      throw new NullPointerException();
    }

    JPEG2KTile tileObj = null;
    JPEG2KComponent compObj = null;
    JPEG2KResolutionLevel rLevelObj = null;
    ArrayList<Long> relevantPrecincts = new ArrayList<Long>();
    int[] components = ArraysUtil.rangesToIndexes(actualViewWindow.comps);
    ArrayList<Integer> relevantTiles = codestream.calculateRelevantTiles(actualViewWindow, discardLevels);

    //actualViewWindow.list(System.out);
    //System.out.println("Discard levels="+discardLevels);

    // Calculate the max. resol. level over the tile-components
    int maxRLevel = 0;
    for (int tileIndex : relevantTiles) {
      tileObj = codestream.getTile(tileIndex);
      for (int comp : components) {
        if (maxRLevel < tileObj.getComponent(comp).getWTLevels()) {
          maxRLevel = tileObj.getComponent(comp).getWTLevels();
        }
      }
    }


    for (int tileIndex : relevantTiles) {
      tileObj = codestream.getTile(tileIndex);

      for (int component : components) {
        compObj = tileObj.getComponent(component);

        // Max. resol. levels of this tile-component
        int maxRLevelsComp = compObj.getWTLevels();

        for (int rLevel = 0; rLevel <= maxRLevelsComp; rLevel++) {
          //System.out.println("\tTile="+tile+" rLevel="+rLevel+" component="+component);
          rLevelObj = compObj.getResolutionLevel(rLevel);

          if (maxRLevelsComp >= discardLevels) {
            // Discard the r highest res. levels
            if (rLevel > maxRLevelsComp - discardLevels) {
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
                  codestream.getXSize(), codestream.getYSize(),
                  codestream.getXOSize(), codestream.getYOSize(),
                  maxRLevelsComp - rLevel);
          //System.out.println("FAME SIZE="+frameSize.toString());


          // Translation of requested region with from the desired frame size reference grid
          // into the sub-sampled reference grid at this frame size
          CADIRectangle supportRegion = new CADIRectangle(actualViewWindow.roff[0],
                  actualViewWindow.roff[1],
                  actualViewWindow.rsiz[0],
                  actualViewWindow.rsiz[1]);
          codestream.calculateSupportRegion(tileIndex, component, rLevel, supportRegion, discardLevels);
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
          startXPrecinct = (int)(Math.floor((double)supportRegion.x / (double)(precinctWidth)));
          startYPrecinct = (int)(Math.floor((double)supportRegion.y / (double)(precinctHeight)));
          endXPrecinct = (int)(Math.ceil((double)(supportRegion.x + supportRegion.width) / (double)(precinctWidth)));	// not included
          endYPrecinct = (int)(Math.ceil((double)(supportRegion.y + supportRegion.height) / (double)(precinctHeight)));	// not included
          //System.out.println("Start Precinct: " + startPrecinct[X] + " " + startPrecinct[Y] + "     End Precinct: " + (endPrecinct[X]-1) + " " + (endPrecinct[Y]-1) );

          for (int yPrecinct = startYPrecinct; yPrecinct < endYPrecinct; yPrecinct++) {
            for (int xPrecinct = startXPrecinct; xPrecinct < endXPrecinct; xPrecinct++) {
              long inClassIdentifier = rLevelObj.getInClassIdentifier(yPrecinct * numPrecinctsWidth + xPrecinct);
              //System.out.println("\tJPIP MESSAGE: inClassID=" + inClassIdentifier + " -> z="+component + " r="+rLevel+" xp="+ xPrecinct+" yp="+yPrecinct);
              relevantPrecincts.add(inClassIdentifier);
            }
          }
        }
      }
    }

    //for (RelevantPrecinct rp : relevantPrecincts) System.out.println(rp.toStringSummary()); // DEBUG

    return relevantPrecincts;
  }

  /**
   * Calculates which are the relevants precincts which belong to the WOI
   * (fsiz, roff, rsiz).
   *
   * Further information, see ISO/IEC 15444-9 section K.4.1
   *
   * @param fsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param ySize definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#ySize}.
   * @param xSize definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#xSize}.
   * @param XOsize definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#XOsize}.
   * @param YOsize definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#YOsize}.
   * @param resolutionPrecinctWidths definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#precinctWidths}.
   * @param resolutionPrecinctHeights  definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#precinctHeights}.
   *
   * @return a array list with the unique precinct number which belongs to the WOI.
   */
  public static ArrayList<Long> TCPROrder(JPEG2KCodestream codestream,
                                          ViewWindowField actualViewWindow,
                                          int discardLevels) {

    assert (discardLevels >= 0);
    if (actualViewWindow == null) {
      throw new NullPointerException();
    }


    JPEG2KTile tileObj = null;
    JPEG2KComponent compObj = null;
    JPEG2KResolutionLevel rLevelObj = null;
    ArrayList<Long> relevantPrecincts = new ArrayList<Long>();
    int[] components = ArraysUtil.rangesToIndexes(actualViewWindow.comps);
    ArrayList<Integer> relevantTiles = codestream.calculateRelevantTiles(actualViewWindow, discardLevels);

    //actualViewWindow.list(System.out);
    //System.out.println("Discard levels="+discardLevels);

    // Calculate the max. resol. level over the tile-components
    int maxAllRLevel = 0;
    for (int tileIndex : relevantTiles) {
      tileObj = codestream.getTile(tileIndex);
      for (int comp : components) {
        if (maxAllRLevel < tileObj.getComponent(comp).getWTLevels()) {
          maxAllRLevel = tileObj.getComponent(comp).getWTLevels();
        }
      }
    }


    for (int tileIndex : relevantTiles) {
      tileObj = codestream.getTile(tileIndex);

      for (int component : components) {
        compObj = tileObj.getComponent(component);

        int maxNumPrecincts = compObj.getNumPrecincts();
        int maxRLevelsComp = compObj.getWTLevels() + 1;
        long[][] precinctIdentifiers = new long[maxRLevelsComp][];

        for (int precinct = 0; precinct < maxNumPrecincts; precinct++) {

          for (int rLevel = 0; rLevel < maxRLevelsComp; rLevel++) {
            //System.out.println("\tTile="+tile+" rLevel="+rLevel+" component="+component);
            rLevelObj = compObj.getResolutionLevel(rLevel);

            if (maxRLevelsComp >= discardLevels) {
              // Discard the r highest res. levels
              if (rLevel > maxRLevelsComp - discardLevels) {
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
                    codestream.getXSize(), codestream.getYSize(),
                    codestream.getXOSize(), codestream.getYOSize(),
                    maxRLevelsComp - rLevel);
            //System.out.println("FAME SIZE="+frameSize.toString());


            // Translation of requested region with from the desired frame size reference grid
            // into the sub-sampled reference grid at this frame size
            CADIRectangle supportRegion = new CADIRectangle(actualViewWindow.roff[0],
                    actualViewWindow.roff[1],
                    actualViewWindow.rsiz[0],
                    actualViewWindow.rsiz[1]);
            codestream.calculateSupportRegion(tileIndex, component, rLevel, supportRegion, discardLevels);
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
            startXPrecinct = (int)(Math.floor((double)supportRegion.x / (double)(precinctWidth)));
            startYPrecinct = (int)(Math.floor((double)supportRegion.y / (double)(precinctHeight)));
            endXPrecinct = (int)(Math.ceil((double)(supportRegion.x + supportRegion.width) / (double)(precinctWidth)));	// not included
            endYPrecinct = (int)(Math.ceil((double)(supportRegion.y + supportRegion.height) / (double)(precinctHeight)));	// not included
            //System.out.println("Start Precinct: " + startPrecinct[X] + " " + startPrecinct[Y] + "     End Precinct: " + (endPrecinct[X]-1) + " " + (endPrecinct[Y]-1) );
            int numPrecincts = (endYPrecinct - startYPrecinct) * (endXPrecinct - startXPrecinct);
            precinctIdentifiers[rLevel] = new long[numPrecincts];
            int precIndex = 0;

            for (int yPrecinct = startYPrecinct; yPrecinct < endYPrecinct; yPrecinct++) {
              for (int xPrecinct = startXPrecinct; xPrecinct < endXPrecinct; xPrecinct++) {
                long inClassIdentifier = rLevelObj.getInClassIdentifier(yPrecinct * numPrecinctsWidth + xPrecinct);
                //System.out.println("\tJPIP MESSAGE: inClassID=" + inClassIdentifier + " -> z="+component + " r="+rLevel+" xp="+ xPrecinct+" yp="+yPrecinct);
                precinctIdentifiers[rLevel][precIndex++] = inClassIdentifier;
              }
            }
          }

          // Add precinct indexes to ArrayList
          for (int i = 0; i < maxNumPrecincts; i++) {
            for (int r = 0; r < maxRLevelsComp; r++) {
              if (precinctIdentifiers[r] == null) {
                continue;
              }
              if (precinctIdentifiers[r].length > i) {
                relevantPrecincts.add(precinctIdentifiers[r][i]);
              }
            }
          }

        }
      }
    }

    //for (RelevantPrecinct rp : relevantPrecincts) System.out.println(rp.toStringSummary()); // DEBUG
    return relevantPrecincts;
  }

  /**
   * Calculates which are the relevants precincts which belong to the WOI
   * (fsiz, roff, rsiz).
   *
   * Further information, see ISO/IEC 15444-9 section K.4.1
   *
   * @param fsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param ySize definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#ySize}.
   * @param xSize definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#xSize}.
   * @param XOsize definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#XOsize}.
   * @param YOsize definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#YOsize}.
   * @param resolutionPrecinctWidths definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#precinctWidths}.
   * @param resolutionPrecinctHeights  definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#precinctHeights}.
   *
   * @return a array list with the unique precinct number which belongs to the WOI.
   */
  public static ArrayList<Long> TRPCOrder(JPEG2KCodestream codestream,
                                          ViewWindowField actualViewWindow,
                                          int discardLevels) {

    assert (discardLevels >= 0);
    if (actualViewWindow == null) {
      throw new NullPointerException();
    }

    JPEG2KTile tileObj = null;
    JPEG2KComponent compObj = null;
    JPEG2KResolutionLevel rLevelObj = null;
    ArrayList<Long> relevantPrecincts = new ArrayList<Long>();
    int[] components = ArraysUtil.rangesToIndexes(actualViewWindow.comps);
    ArrayList<Integer> relevantTiles = codestream.calculateRelevantTiles(actualViewWindow, discardLevels);

    //actualViewWindow.list(System.out);
    //System.out.println("Discard levels="+discardLevels);

    // Calculate the max. resol. level over the tile-components
    int maxRLevel = 0;
    for (int tileIndex : relevantTiles) {
      tileObj = codestream.getTile(tileIndex);
      for (int comp : components) {
        if (maxRLevel < tileObj.getComponent(comp).getWTLevels()) {
          maxRLevel = tileObj.getComponent(comp).getWTLevels();
        }
      }
    }


    for (int tileIndex : relevantTiles) {
      tileObj = codestream.getTile(tileIndex);

      for (int rLevel = 0; rLevel <= maxRLevel; rLevel++) {

        long[][] precinctIdentifiers = new long[components.length][];
        int maxLength = 0;

        for (int zIndex = 0; zIndex < components.length; zIndex++) {

          int z = components[zIndex];
          compObj = tileObj.getComponent(z);

          // Max. resol. levels of this tile-component
          int maxRLevelsComp = compObj.getWTLevels();

          if (rLevel > maxRLevelsComp) {
            continue;
          }

          //System.out.println("\tTile="+tile+" rLevel="+rLevel+" component="+component);


          if (maxRLevelsComp >= discardLevels) {
            // Discard the r highest res. levels
            if (rLevel > maxRLevelsComp - discardLevels) {
              continue;
            }
          } else {
            // It should discard more res. levels than ones available in this
            // tile-component. Then, includes only the LL sub-band.
            if (rLevel != 0) {
              continue;
            }
          }

          rLevelObj = compObj.getResolutionLevel(rLevel);


          CADIDimension frameSize = JPEG2000Util.calculateFrameSize(
                  codestream.getXSize(), codestream.getYSize(),
                  codestream.getXOSize(), codestream.getYOSize(),
                  maxRLevelsComp - rLevel);
          //System.out.println("FAME SIZE="+frameSize.toString());


          // Translation of requested region with from the desired frame size reference grid
          // into the sub-sampled reference grid at this frame size
          CADIRectangle supportRegion = new CADIRectangle(actualViewWindow.roff[0],
                  actualViewWindow.roff[1],
                  actualViewWindow.rsiz[0],
                  actualViewWindow.rsiz[1]);
          codestream.calculateSupportRegion(tileIndex, z, rLevel, supportRegion, discardLevels);
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
          startXPrecinct = (int)(Math.floor((double)supportRegion.x / (double)(precinctWidth)));
          startYPrecinct = (int)(Math.floor((double)supportRegion.y / (double)(precinctHeight)));
          endXPrecinct = (int)(Math.ceil((double)(supportRegion.x + supportRegion.width) / (double)(precinctWidth)));	// not included
          endYPrecinct = (int)(Math.ceil((double)(supportRegion.y + supportRegion.height) / (double)(precinctHeight)));	// not included
          //System.out.println("Start Precinct: " + startPrecinct[X] + " " + startPrecinct[Y] + "     End Precinct: " + (endPrecinct[X]-1) + " " + (endPrecinct[Y]-1) );

          int numPrecincts = (endYPrecinct - startYPrecinct) * (endXPrecinct - startXPrecinct);
          if (maxLength < numPrecincts) {
            maxLength = numPrecincts;
          }

          precinctIdentifiers[zIndex] = new long[numPrecincts];
          int precIndex = 0;
          for (int yPrecinct = startYPrecinct; yPrecinct < endYPrecinct; yPrecinct++) {
            for (int xPrecinct = startXPrecinct; xPrecinct < endXPrecinct; xPrecinct++) {
              long inClassIdentifier = rLevelObj.getInClassIdentifier(yPrecinct * numPrecinctsWidth + xPrecinct);
              //System.out.println("\tJPIP MESSAGE: inClassID=" + inClassIdentifier + " -> z="+component + " r="+rLevel+" xp="+ xPrecinct+" yp="+yPrecinct);
              precinctIdentifiers[zIndex][precIndex++] = inClassIdentifier;
            }
          }
        }

        // Add precinct indexes to ArrayList
        for (int i = 0; i < maxLength; i++) {
          for (int zIndex = 0; zIndex < components.length; zIndex++) {
            if (precinctIdentifiers[zIndex].length > i) {
              relevantPrecincts.add(precinctIdentifiers[zIndex][i]);
            }
          }
        }
      }
    }

    //for (RelevantPrecinct rp : relevantPrecincts) System.out.println(rp.toStringSummary()); // DEBUG

    return relevantPrecincts;
  }

  /**
   * Calculates which are the relevants precincts which belong to the WOI
   * (fsiz, roff, rsiz).
   *
   * Further information, see ISO/IEC 15444-9 section K.4.1
   *
   * @param fsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}.
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
   * @param ySize definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#ySize}.
   * @param xSize definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#xSize}.
   * @param XOsize definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#XOsize}.
   * @param YOsize definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.SIZParameters#YOsize}.
   * @param resolutionPrecinctWidths definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#precinctWidths}.
   * @param resolutionPrecinctHeights  definition in {@link CADI.Common.LogicalTarget.JPEG2000.Parameters.CODParameters#precinctHeights}.
   *
   * @return a array list with the unique precinct number which belongs to the WOI.
   */
  public static ArrayList<Long> TPCROrder(JPEG2KCodestream codestream,
                                          ViewWindowField actualViewWindow,
                                          int discardLevels) {

    assert (discardLevels >= 0);
    if (actualViewWindow == null) {
      throw new NullPointerException();
    }

    JPEG2KTile tileObj = null;
    JPEG2KComponent compObj = null;
    JPEG2KResolutionLevel rLevelObj = null;
    ArrayList<Long> relevantPrecincts = new ArrayList<Long>();
    int[] components = ArraysUtil.rangesToIndexes(actualViewWindow.comps);
    ArrayList<Integer> relevantTiles = codestream.calculateRelevantTiles(actualViewWindow, discardLevels);

    //actualViewWindow.list(System.out);
    //System.out.println("Discard levels="+discardLevels);

    // Calculate the max. resol. level over the tile-components
    int maxRLevel = 0;
    for (int tileIndex : relevantTiles) {
      tileObj = codestream.getTile(tileIndex);
      for (int comp : components) {
        if (maxRLevel < tileObj.getComponent(comp).getWTLevels()) {
          maxRLevel = tileObj.getComponent(comp).getWTLevels();
        }
      }
    }


    for (int tileIndex : relevantTiles) {
      tileObj = codestream.getTile(tileIndex);

      long[][][] precinctIdentifiers = new long[components.length][][];
      int maxNumPrecincts = 0;
      int maxResolutionLevel = 0;

      for (int zIndex = 0; zIndex < components.length; zIndex++) {

        int maxRLevelsComp = compObj.getWTLevels() + 1;
        int z = components[zIndex];
        precinctIdentifiers[zIndex] = new long[maxRLevelsComp][];
        if (maxResolutionLevel < maxRLevelsComp) {
          maxResolutionLevel = maxRLevelsComp;
        }

        compObj = tileObj.getComponent(z);

        for (int rLevel = 0; rLevel < maxRLevelsComp; rLevel++) {
          //System.out.println("\tTile="+tile+" rLevel="+rLevel+" component="+component);
          if (rLevel > maxRLevelsComp) {
            continue;
          }

          if (maxRLevelsComp >= discardLevels) {
            // Discard the r highest res. levels
            if (rLevel > maxRLevelsComp - discardLevels) {
              continue;
            }
          } else {
            // It should discard more res. levels than ones available in this
            // tile-component. Then, includes only the LL sub-band.
            if (rLevel != 0) {
              continue;
            }
          }

          rLevelObj = compObj.getResolutionLevel(rLevel);


          CADIDimension frameSize = JPEG2000Util.calculateFrameSize(
                  codestream.getXSize(), codestream.getYSize(),
                  codestream.getXOSize(), codestream.getYOSize(),
                  maxRLevelsComp - rLevel);
          //System.out.println("FAME SIZE="+frameSize.toString());


          // Translation of requested region with from the desired frame size reference grid
          // into the sub-sampled reference grid at this frame size
          CADIRectangle supportRegion = new CADIRectangle(actualViewWindow.roff[0],
                  actualViewWindow.roff[1],
                  actualViewWindow.rsiz[0],
                  actualViewWindow.rsiz[1]);
          codestream.calculateSupportRegion(tileIndex, z, rLevel, supportRegion, discardLevels);
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
          startXPrecinct = (int)(Math.floor((double)supportRegion.x / (double)(precinctWidth)));
          startYPrecinct = (int)(Math.floor((double)supportRegion.y / (double)(precinctHeight)));
          endXPrecinct = (int)(Math.ceil((double)(supportRegion.x + supportRegion.width) / (double)(precinctWidth)));	// not included
          endYPrecinct = (int)(Math.ceil((double)(supportRegion.y + supportRegion.height) / (double)(precinctHeight)));	// not included
          //System.out.println("Start Precinct: " + startPrecinct[X] + " " + startPrecinct[Y] + "     End Precinct: " + (endPrecinct[X]-1) + " " + (endPrecinct[Y]-1) );

          int numPrecincts = (endYPrecinct - startYPrecinct) * (endXPrecinct - startXPrecinct);
          if (maxNumPrecincts < numPrecincts) {
            maxNumPrecincts = numPrecincts;
          }

          precinctIdentifiers[zIndex][rLevel] = new long[numPrecincts];
          int precIndex = 0;


          for (int yPrecinct = startYPrecinct; yPrecinct < endYPrecinct; yPrecinct++) {
            for (int xPrecinct = startXPrecinct; xPrecinct < endXPrecinct; xPrecinct++) {
              long inClassIdentifier = rLevelObj.getInClassIdentifier(yPrecinct * numPrecinctsWidth + xPrecinct);
              //System.out.println("\tJPIP MESSAGE: inClassID=" + inClassIdentifier + " -> z="+component + " r="+rLevel+" xp="+ xPrecinct+" yp="+yPrecinct);
              precinctIdentifiers[zIndex][rLevel][precIndex++] = inClassIdentifier;
            }
          }
        }
      }

      // Add precinct indexes to ArrayList
      for (int i = 0; i < maxNumPrecincts; i++) {
        for (int zIndex = 0; zIndex < components.length; zIndex++) {
          for (int r = 0; r <= maxResolutionLevel; r++) {
            if (precinctIdentifiers[zIndex] == null) {
              continue;
            }
            if (precinctIdentifiers[zIndex][r] == null) {
              continue;
            }
            if (precinctIdentifiers[zIndex][r].length > i) {
              relevantPrecincts.add(precinctIdentifiers[zIndex][r][i]);
            }
          }
        }
      }
    }

    //for (RelevantPrecinct rp : relevantPrecincts) System.out.println(rp.toStringSummary()); // DEBUG

    return relevantPrecincts;
  }

  /**
   *
   * @param actualViewWindow
   * @param discardLevels
   * @return
   */
  @SuppressWarnings("unchecked")
  public static ArrayList<Long>[][] TR_CPOrder(JPEG2KCodestream codestream,
                                               ViewWindowField actualViewWindow,
                                               int discardLevels) {

    assert (discardLevels >= 0);
    if (actualViewWindow == null) {
      throw new NullPointerException();
    }


    ArrayList<Long>[][] relevantPrecincts = new ArrayList[codestream.getNumTiles()][];
    int[] components = ArraysUtil.rangesToIndexes(actualViewWindow.comps);
    ArrayList<Integer> relevantTiles = codestream.calculateRelevantTiles(actualViewWindow, discardLevels);


    //actualViewWindow.list(System.out);
    //System.out.println("Discard levels="+discardLevels);

    // Calculate the max. resol. level over the tile-components
		/*int maxRLevel = 0;
    for (int tileIndex : relevantTiles) {
    for (int comp : components)
    if (maxRLevel < tiles.get(tileIndex).components.get(comp).getWTLevels())
    maxRLevel = tiles.get(tileIndex).components.get(comp).getWTLevels();
    }*/

    JPEG2KTile tileObj = null;
    JPEG2KComponent compObj = null;
    JPEG2KResolutionLevel rLevelObj = null;
    for (int tileIndex : relevantTiles) {
      tileObj = codestream.getTile(tileIndex);

      // Find maximum resolution level
      int maxRLevel = 0;
      for (int z = 0; z < codestream.getZSize(); z++) {
        compObj = tileObj.getComponent(z);
        if (maxRLevel < compObj.getWTLevels()) {
          maxRLevel = compObj.getWTLevels();
        }
      }
      relevantPrecincts[tileIndex] = (ArrayList<Long>[])new ArrayList[maxRLevel + 1];


      for (int rLevel = 0; rLevel <= maxRLevel; rLevel++) {

        relevantPrecincts[tileIndex][rLevel] = new ArrayList<Long>();


        for (int component : components) {
          //System.out.println("\tTile="+tile+" rLevel="+rLevel+" component="+component);
          compObj = tileObj.getComponent(component);
          rLevelObj = compObj.getResolutionLevel(rLevel);

          // Max. resol. levels of this tile-component
          int maxRLevelsComponent = compObj.getWTLevels();
          //System.out.println("\t\tmaxRLevelsComponent="+maxRLevelsComponent);


          if (maxRLevelsComponent >= discardLevels) {
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
                  codestream.getXSize(), codestream.getYSize(),
                  codestream.getXOSize(), codestream.getYOSize(),
                  maxRLevelsComponent - rLevel);
          //System.out.println("FAME SIZE="+frameSize.toString());


          // Translation of requested region with from the desired frame size reference grid
          // into the sub-sampled reference grid at this frame size
          CADIRectangle supportRegion = new CADIRectangle(actualViewWindow.roff[0],
                  actualViewWindow.roff[1],
                  actualViewWindow.rsiz[0],
                  actualViewWindow.rsiz[1]);
          codestream.calculateSupportRegion(tileIndex, component, rLevel, supportRegion, discardLevels);
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
          startXPrecinct = (int)(Math.floor((double)supportRegion.x / (double)(precinctWidth)));
          startYPrecinct = (int)(Math.floor((double)supportRegion.y / (double)(precinctHeight)));
          endXPrecinct = (int)(Math.ceil((double)(supportRegion.x + supportRegion.width) / (double)(precinctWidth)));	// not included
          endYPrecinct = (int)(Math.ceil((double)(supportRegion.y + supportRegion.height) / (double)(precinctHeight)));	// not included
          //System.out.println("Start Precinct: " + startPrecinct[X] + " " + startPrecinct[Y] + "     End Precinct: " + (endPrecinct[X]-1) + " " + (endPrecinct[Y]-1) );

          for (int yPrecinct = startYPrecinct; yPrecinct < endYPrecinct; yPrecinct++) {
            for (int xPrecinct = startXPrecinct; xPrecinct < endXPrecinct; xPrecinct++) {
              long inClassIdentifier = rLevelObj.getInClassIdentifier(yPrecinct * numPrecinctsWidth + xPrecinct);
              //System.out.println("\tJPIP MESSAGE: inClassID=" + inClassIdentifier + " -> z="+component + " r="+rLevel+" xp="+ xPrecinct+" yp="+yPrecinct);
              relevantPrecincts[tileIndex][rLevel].add(inClassIdentifier);
            }
          }
        }
      }
    }

    //for (RelevantPrecinct rp : relevantPrecincts) System.out.println(rp.toStringSummary()); // DEBUG

    return relevantPrecincts;
  }

}
