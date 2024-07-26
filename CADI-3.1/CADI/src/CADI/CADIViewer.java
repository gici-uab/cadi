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
/*
 * @(#)CADIViewer.java   07/09/26
 *
 * Copyright 2007-2012 Group on Interactive Coding of Images. All rights reserved.
 * GICI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * Copyright (c) 2007-2012
 */
package CADI;

import CADI.Viewer.Viewer;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 * This class is the launcher of the CADI Viewer applications.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0   2007-2012/10/26
 */
public class CADIViewer {

	public static void main(String s[]) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//Set some window initializations	  
				JFrame frame;			

				frame = new Viewer();
				frame.addWindowListener( new WindowAdapter() {				
					public void windowClosing (WindowEvent e) {
						System.exit(0);
					}
				});
			}			
		});		
	}
}
