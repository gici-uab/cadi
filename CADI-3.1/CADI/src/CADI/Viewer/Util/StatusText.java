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
package CADI.Viewer.Util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This class implements a panel where a line of test (status line) will
 * be displayed.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2007-2012/12/09
 */
public class StatusText extends JPanel {
		
	/**
	 * String to be printed in the status bar.
	 */
	private String text = null;

	/**
	 * Label where the text will be printed.
	 */
	private JLabel label = null;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param width width of the panel
	 * @param height height of the panel
	 */
	public StatusText(int width, int height) {
		super();		
		setLayout(new BorderLayout(0, 0));
		setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

		if (label == null) {
			label = new JLabel("");			
			label.setFont(new Font("SansSerif", Font.PLAIN, 10));
			Dimension dims = new Dimension(width, height);
			label.setSize(dims);
			label.setPreferredSize(dims);
			label.setMinimumSize(dims);
			//label.setMaximumSize(dims);
		}
		
		add(label);
	}
	
	/**
	 * Prints a single line of text in the status bar.
	 *  
	 * @param text the line of text to be displayed.
	 */
	public void setText(String text) {
		this.text = text;
		label.setText(this.text);
	}
	
}