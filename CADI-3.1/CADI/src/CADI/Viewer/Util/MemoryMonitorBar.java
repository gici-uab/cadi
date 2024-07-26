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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * This class shows an horizontal bar monitoring the memory.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version   1.0   2007-2012/11/29
 */
public class MemoryMonitorBar extends JPanel {

	/**
	 * Label to print the text.
	 */
	private JLabel label = null;
	
	/**
	 * Width of the panel.
	 */
	private int width = 0;
	
	/**
	 * Height of the panel.
	 */
	private int height = 0;
	
	/**
	 * Color of the bar that represents the total memory.
	 */
	private static Color totalMemoryColor = new Color(0x555555);
	
	/**
	 * Color of the bar that represents the memory used.
	 */
	private static Color memoryUsedColor = new Color(0x0072B6);
	
	/**
	 * MegaByte constant.
	 */
	private static int MB = 1024 * 1024;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param width width of the panel.
	 * @param height height of the panel.
	 */
	public MemoryMonitorBar(int width, int height) {
		this.width = width;
		this.height = height;
		
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		
		// Create the label
		label = new JLabel();
		label.setFont(new Font("SansSerif", Font.PLAIN, 12));
		add(label);
		
		// Set sizes
		Dimension dims = new Dimension(width, height);
		setSize(dims);
		setPreferredSize(dims);
		setMinimumSize(dims);
		setMaximumSize(dims);
		
		// Run a thread
		Thread thread = new Thread() {
			public void run() {
				setPriority(Thread.MIN_PRIORITY);
				while(true) {
					repaint();
					try {
						Thread.sleep(1000);
					} catch (Exception e) {}
				}
			}
		};
		
		thread.start();
	}

	
	// ============================ private methods ==============================
	/* (non-Javadoc)
	 * @see avax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Get memory		
		long totalMemory = Runtime.getRuntime().totalMemory();
		long maxMemory = Runtime.getRuntime().maxMemory();
		long freeMemory = Runtime.getRuntime().freeMemory();
		
		long memoryUsed = totalMemory - freeMemory;

		// Write label
		String text = (memoryUsed/MB) + " of " + (maxMemory/MB) + " MB";		
		label.setText(text);
		
		// Bar size
		Insets insets = getInsets();
		int barWidth = this.width - insets.left - insets.right;
		int barHeight = this.height - insets.top - insets.bottom;

		Graphics2D g2d = (Graphics2D)g;
				
		// Memory used
		int x = insets.left;
		int y = insets.top;
		int width = insets.left + (int)(barWidth*memoryUsed/maxMemory);
		int height = insets.top + barHeight;
		g2d.setPaint(new GradientPaint(0, 0, Color.white, 0, barHeight, memoryUsedColor));		
		g2d.fillRect(x, y, width, height);

		// Total memory		
		g2d.setPaint(new GradientPaint(0, 0, Color.white, 0, barHeight, totalMemoryColor));
		//g2d.setColor(totalMemoryColor);
		x = width;		
		width = insets.left + (int)(barWidth*totalMemory/maxMemory);
		g2d.fillRect(x, y, width, height);

		// Background
		g2d.setColor(getBackground());
		x = width;
		width = insets.left + barWidth;
		g2d.fillRect(x, y, width, height);		
	}

}