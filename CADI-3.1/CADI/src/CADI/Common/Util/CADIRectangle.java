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

import java.awt.Rectangle;
import java.io.PrintStream;

/**
 * 
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2012/03/04
 */
public class CADIRectangle extends java.awt.Rectangle {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5298099440370295176L;

	// ============================= public methods ==============================
  /**
   * 
   */
	public CADIRectangle() {
		super();
	}
	
	public CADIRectangle(CADIDimension d) {
		super(d);
	}
	
	public CADIRectangle(CADIRectangle r) {
		super(r);
	}
	
	public CADIRectangle(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	public CADIRectangle(int width, int height) {
		super(width, height);
	}
	
	public CADIRectangle(CADIPoint p) {
		super(p);
	}
	
	public CADIRectangle(CADIPoint p, CADIDimension d) {
		super(p,d);
	}
	
	/**
	 * Sets the attributes to its initial values.
	 */
	public void reset() {
		width = height = 0;
	}
	
	/**
	 * Performs an intersection between the current object and the rectangle
	 * given by the input parameter.
	 * 
	 * @param rect
	 */
	public CADIRectangle intersection(CADIRectangle rect) {
		assert(rect != null);
		Rectangle rec = super.intersection(rect);
		x = rec.x; y = rec.y;
		width = rec.width; height = rec.height;
		return this;
	}
	
	/**
	 * Performs an intersection between the current object and the rectangle
	 * given by the input parameter. Result is save in the current object.
	 * 
	 * @param rect
	 */
	public void intersection(int x, int y, int width, int height) {
				
		// Check size 
		if (this.x+this.width > x+width) this.width = x+width-this.x;
		if (this.y+this.height > y+height) this.height = y+height-this.y;

		// Check origin
		if (this.x < x) {
			this.width = this.x + this.width - x;
			this.x = x;
		}
		if (this.y < y) {
			this.height = this.y + this.height - y;
			this.y = y;
		}
    
    if (this.width < 0) this.width = 0;
    if (this.height < 0) this.height = 0;
	}
	
	/**
	 * 
	 * @param r
	 * @return
	 */
	public boolean intersects(CADIRectangle r) {
		return super.intersects(r);
	}
	
	/**
	 * 
	 * @return
	 */
	public long getArea() {
		return width*height;
	}
	
	/**
	 * For debugging purpose
	 */
  @Override
	public String toString() {
		return super.toString();
	}

	/**
	 * Prints this CADIDimension out to the specified output stream. This method
	 * is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {
		out.println("-- CADI Rectangle --");
		out.println("x: "+x);
		out.println("y: "+y);
		out.println("width: "+width);
		out.println("height: "+height);
		out.flush();
	}
}
