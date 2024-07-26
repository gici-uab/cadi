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

import CADI.Common.Util.ArraysUtil;

/**
 * This class is useful to calculate which are the necessary components to
 * invert a DPCM transform.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2008/10/12
 */
public class CalculateRequiredComponentsDPCM {

	/**
	 * Indicates the type of DPCM. Allowed values are: {@link #CLASSIC},
	 * {@link #RESTART_PERIODIC}, and {@link #RESTART_ARBITRARY}.
	 */
	private int type = CLASSIC;
	
	/**
	 * Is the classical DPCM, where each component (apart from first one) is
	 * obtained as a difference from the previous one.
	 */
	public static final int CLASSIC = 1;
	
	/**
	 * Is a clustered DPCM where the restart index is fixed.
	 */
	public static final int RESTART_PERIODIC = 2;
	
	/**
	 * Is a clustered DPCM where the indexes for the restart can take each
	 * value from the value ranges, with and without regular recurrence.
	 */
	public static final int RESTART_ARBITRARY = 3;
	
	/**
	 * Is an one-dimensional array with the inital components values.
	 */
	private int[] compsArray = null;
	
	/**
	 * Is the maximum number of components.
	 */
	private int maxNumComps = 0;
	
	/**
	 * Is the component index used in the {@link #RESTART_PERIODIC} mode.
	 */
	private int restartIndex = -1;
	
	/**
	 * Is a one-dimensional array with component indexes for the
	 * {@value #RESTART_ARBITRARY} mode.
	 */
	private int[] restartIndexes = null;
	
	/**
	 * Contains the required components which will be necessary to invert
	 * the {@link #compsArray} components.
	 */
	private int[] requiredCompsArray = null;
		
	
	// ============================= public methods ==============================	
	
	/**
	 * Constructor.
	 * 
	 * @param compsArray definition {@link #compsArray}. 
	 * @param maxNumComps definition in {@link #maxNumComps}.
	 */
	public CalculateRequiredComponentsDPCM(int[] compsArray, int maxNumComps) {
		if (compsArray == null) throw new NullPointerException();
		if (maxNumComps <= 0) throw new IllegalArgumentException();
		
		this.compsArray = compsArray;
		this.maxNumComps = maxNumComps;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param comps definition in {@link CADI.Common.Network.JPIP.ViewWindowField#comps}.
	 * @param maxNumComps definition in {@link #maxNumComps}.
	 */
	public CalculateRequiredComponentsDPCM(int[][] comps, int maxNumComps) {
		if (comps == null) throw new NullPointerException();
		if (maxNumComps <= 0) throw new IllegalArgumentException();
		
		compsArray = ArraysUtil.rangesToIndexes(comps);
		this.maxNumComps = maxNumComps;
	}
	
	/**
	 * Sets the DPCM type to classic. Further information, see
	 * {@link #CLASSIC}.
	 */
	public void setClassical() {
		type = CLASSIC;
	}
	
	/**
	 * Sets the DPCM type to the restart periodical mode. Further information,
	 * see {@link #RESTART_PERIODIC}. 
	 * 
	 * @param restartIndex definition in  {@link #restartIndex}.
	 */
	public void setRestartPeriodic(int restartIndex) {
		if (restartIndex <= 0) throw new IllegalArgumentException("Restart index must be greater than or equal to 0");
		
		type = RESTART_PERIODIC;
		this.restartIndex = restartIndex;
	}
	
	/**
	 * Sets the DPCM type to the restart fixed mode. Further information,
	 * see {@link #RESTART_ARBITRARY}.
	 * 
	 * @param restartIndexes defintion in {@link #restartIndexes}.
	 */
	public void setRestartArbitrary(int[] restartIndexes) {
		if (restartIndexes == null) throw new NullPointerException();
		for (int i = 0; i < restartIndexes.length; i++) {
			if (restartIndexes[i] < 0) throw new IllegalArgumentException("Restar indexes must be greater than 0");
		}
		
		type = RESTART_ARBITRARY;
		this.restartIndexes = restartIndexes;
	}
	
	/**
	 * Performs the calculus of the required components.
	 */
	public void run() throws IllegalAccessException {
		
		switch (type) {
			case CLASSIC:
				int lastComp = compsArray[compsArray.length-1];
				requiredCompsArray = new int[lastComp+1];
				for (int c = 0; c <= lastComp; c++) requiredCompsArray[c] = c;
				break;
				
			case RESTART_PERIODIC:
				if (restartIndex <= 0) throw new IllegalAccessException();
				requiredCompsArray = calculateRequiredComponentsWithRestartPeriodical();
				break;
				
			case RESTART_ARBITRARY:
				if (restartIndexes == null) throw new IllegalAccessException();
				requiredCompsArray = calculateRequiredComponentsWithRestartFixed();
				break;
				
			default:
				assert(true);
		}
	}
	
	/**
	 * Returns the {@link #requiredCompsArray} attribute.
	 * 
	 * @return the {@link #requiredCompsArray} attribute.
	 */
	public int[] getRequiredCompsArray() {
		return requiredCompsArray;
	}
	
	/**
	 * Returns the {@link #requiredCompsArray} as a range of components.
	 * 
	 * @return the {@link #requiredCompsArray} as a range of components.
	 */
	public int[][] getRequiredComps() {
		return ArraysUtil.indexesToRanges(requiredCompsArray);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";
		
		if (compsArray != null) {
			
			// Initial components
			int[][] rangeOfComps = ArraysUtil.indexesToRanges(compsArray);
			str += "Comps. ranges=";
			for (int i = 0; i < rangeOfComps.length; i++) {
				str += rangeOfComps[i][0]+"-"+rangeOfComps[i][1];
				if(i < (rangeOfComps.length-1)) str += ",";
			}
		
			// Type
			str += "  type = ";
			if (type == CLASSIC) str += "classic";
			else if (type == RESTART_PERIODIC) str += "restart periodical";
			else if (type == RESTART_ARBITRARY) str += "restart fixed";
			
			// Required components
			if (requiredCompsArray != null) {
				int[][] requiredCompsRanges = ArraysUtil.indexesToRanges(requiredCompsArray);
				str += "Required comps. ranges=";
				for (int i = 0; i < requiredCompsRanges.length; i++) {
					str += requiredCompsRanges[i][0]+"-"+requiredCompsRanges[i][1];
					if(i < (requiredCompsRanges.length-1)) str += ",";
				}
			} else {
				str += "Required comps. ranges= THEY HAVE NOT BEEN CALCULATED YET!";
			}
		}
		
		str += "]";
		
		return str;
	}
	
	/**
	 * Prints this Client Capabilities and Preferences fields out to the
	 * specified output stream. This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Calculate Required Components DPCM --");		
				
		if (compsArray != null) {
			
			// Initial components
			int[][] rangeOfComps = ArraysUtil.indexesToRanges(compsArray);
			out.print("Comps. ranges: ");
			for (int i = 0; i < rangeOfComps.length; i++) {
				out.print(rangeOfComps[i][0]+"-"+rangeOfComps[i][1]);
				if(i < (rangeOfComps.length-1)) out.print(",");
			}
			out.println();
			
			// Type
			out.print("type: ");
			if (type == CLASSIC) out.print("classic");
			else if (type == RESTART_PERIODIC) out.print("restart periodical");
			else if (type == RESTART_ARBITRARY) out.print("restart fixed");
			out.println();
			
			// Required components
			if (requiredCompsArray != null) {
				int[][] requiredCompsRanges = ArraysUtil.indexesToRanges(requiredCompsArray);
				out.print("Required comps. ranges: ");
				for (int i = 0; i < requiredCompsRanges.length; i++) {
					out.print(requiredCompsRanges[i][0]+"-"+requiredCompsRanges[i][1]);
					if(i < (requiredCompsRanges.length-1)) out.print(",");
				}
				out.println();
			} else {
				out.print("Required comps. ranges: THEY HAVE NOT BEEN CALCULATED YET!");
			}
		}
								
		out.flush();
	}
	
	
	// ============================ private methods ==============================
	
	/**
	 * Calculates which are the necessary components to inver a DPCM transform
	 * using periodical components. 
	 * 
	 * @return an one-dimensional array with the necessary components.
	 */
	private int[] calculateRequiredComponentsWithRestartPeriodical() {
		
		// Convert from array of components to range of components
		int[][] rangeOfComps = ArraysUtil.indexesToRanges(compsArray);
		
		// Calculate required components
		for (int i = 0; i < rangeOfComps.length; i++) {
			rangeOfComps[i][0] = ((int)Math.floor(rangeOfComps[i][0]/restartIndex))*restartIndex; 
		}
		
		// Join possible overlapped ranges
		rangeOfComps = ArraysUtil.joinOverlapedRanges(rangeOfComps);
		
		// Convert from range of components to arry of components
		requiredCompsArray = ArraysUtil.rangesToIndexes(rangeOfComps);
		
		return requiredCompsArray;
	}
	
	
	/**
	 * Calculates which are the necessary components to invert a DPCM transform
	 * using fixed restart components. 
	 * 
	 * @return an one-dimensional array with the necessary components.
	 */
	private int[] calculateRequiredComponentsWithRestartFixed() {

		// Convert from array of components to range of components
		int[][] rangeOfComps = ArraysUtil.indexesToRanges(compsArray);
		
		// Calculate required components		
		for (int i = 0; i < rangeOfComps.length; i++) {			
			for (int rIndex = 0; rIndex < restartIndexes.length-1; rIndex++) {
				if ( (restartIndexes[rIndex] <= rangeOfComps[i][0]) && (restartIndexes[rIndex+1] > rangeOfComps[i][0]) ) {
					rangeOfComps[i][0] = restartIndexes[rIndex];
					break;
				}
			}
		}
		
		// Join possible overlapped ranges
		rangeOfComps = ArraysUtil.joinOverlapedRanges(rangeOfComps);
		
		// Convert from range of components to arry of components
		requiredCompsArray = ArraysUtil.rangesToIndexes(rangeOfComps);
		
		return requiredCompsArray;
	}
}