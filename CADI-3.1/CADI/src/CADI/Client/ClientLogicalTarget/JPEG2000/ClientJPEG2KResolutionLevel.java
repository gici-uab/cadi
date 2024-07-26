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
package CADI.Client.ClientLogicalTarget.JPEG2000;

import CADI.Common.Cache.PrecinctDataBin;
import java.io.PrintStream;

import CADI.Common.LogicalTarget.JPEG2000.JPEG2KResolutionLevel;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2010/11/13
 */
public class ClientJPEG2KResolutionLevel extends JPEG2KResolutionLevel {


	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public ClientJPEG2KResolutionLevel(ClientJPEG2KComponent parent,
	                                   int rLevel) {
		super(parent, rLevel);
	}
	
  @Override
	public void createPrecinct(int index) {
		if (!precincts.containsKey(index))
			precincts.put(index, new ClientJPEG2KPrecinct(this, index));
	}
  
  /**
   * 
   * @param index
   * @param precinctDataBin 
   */
	public void createPrecinct(int index, PrecinctDataBin precinctDataBin) {
		if (!precincts.containsKey(index))
			precincts.put(index, new ClientJPEG2KPrecinct(this, index, precinctDataBin));
	}
	
  @Override
	public ClientJPEG2KPrecinct getPrecinct(int index) {
    /*if (!precincts.containsKey(index)) {
        newPrecinct(index);
    }*/
		return (ClientJPEG2KPrecinct) precincts.get(index);
	}
  
  @Override
  public void removePrecinct(int index) {
    if (precincts.containsKey(index))
      precincts.remove(index);
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
		
		str += "]";
		
		return str;
	}

	/**
	 * Prints this Client JPEG2K Resolution level out to the specified output
	 * stream. This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
  @Override
	public void list(PrintStream out) {
		
		out.println("-- Client JPEG2K Resolution level --");
		super.list(out);
		
	}
	
	// ============================ private methods ==============================

}
