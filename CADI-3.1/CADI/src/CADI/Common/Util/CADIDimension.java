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
package CADI.Common.Util;

import java.io.PrintStream;

/**
 * 
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2010/04/18
 */
public class CADIDimension extends java.awt.Dimension {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7011890293259794445L;

	// ============================= public methods ==============================
	public CADIDimension() {
		super();
	}
	
	public CADIDimension(CADIDimension dim) {
		super(dim);
	}
	
	public CADIDimension(int width, int height) {
		super(width, height);
	}
	
	/**
	 * Sets the attributes to its initial values.
	 */
	public void reset() {
		width = height = 0;
	}
		
	/**
	 * For debugging purpose
	 */
  @Override
	public String toString() {
		return getClass().getName() + "[width=" + width + ",height=" + height + "]";
	}

	/**
	 * Prints this CADIDimension out to the specified output stream. This method
	 * is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {
		out.println("-- CADI Dimension --");
		out.println("width: "+width);
		out.println("height: "+height);
		out.flush();
	}
}
