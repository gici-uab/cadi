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
package CADI.Viewer.Display;

import java.awt.Dimension;

import javax.swing.JScrollBar;

/**
 * This class extends the JScrollBar adding a new method that gives the
 * thickness of the scroll bar.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version  1.0 2007-2012/12/03
 */
public class ImageScrollBar extends JScrollBar {

	// ============================= public methods ==============================
	/**
	 * Constructor. Creates a scrollbar with the specified orientation, value,
	 * extent, minimum, and maximum. The "extent" is the size of the viewable
	 * area. It is also known as the "visible amount".
	 * 
	 * @param orientation see {@link #setOrientation(int)}.
	 * @param value see {@link #setValue(int)}
	 * @param visible see {@link #setVisible(boolean)}
	 * @param min see {@link #setMinimum(int)}
	 * @param max see {@link #setMaximum(int)}
	 */
	public ImageScrollBar(int orientation, int value, int visible, int min, int max) {
		super(orientation,value,visible,min,max);
	}
	
	/**
	 * Return the thickness of the scroll bar. If the scroll bar is a vertical
	 * scroll bar, the thickness is the width; and if the scroll bar is an
	 * horizontal scroll bar, the thickness is the height.
	 * 
	 * @return the thickness of the scroll bar.
	 */
	public int getThickness() {
		Dimension dim = super.getPreferredSize();
        if (getOrientation() == HORIZONTAL) {
            return dim.height;
        }
        else {
            return dim.width;
        }
	}
		
}
