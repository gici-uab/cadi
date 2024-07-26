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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This class creates JPanel and it displays the speed and target on it. This
 * class runs as thread and periodically it get the speed and target bytes and
 * displays them in the status bar.
 * <p>
 * This class needs a reference to a method that gets the speed value, and
 * another reference to a method to get the target bytes.
 * <p>
 * Usage example:<br>
 * &nbsp; constructor<br>  
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2008/05/01
 */
public class SpeedAndTargetMonitor extends JPanel {

	/**
	 * Class where the invocated method is.
	 */
	private Object obj = null;

	/**
	 * Method to invoke for getting the speed.
	 */
	Method getSpeed = null;
	
	/**
	 * Method to invoke for getting the download.
	 */
	Method getDownloadedBytes = null;
	
	/**
	 * Label to print the speed.
	 */
	private JLabel speedLabel = null;
	
	/**
	 * Label to print the target.
	 */
	private JLabel targetLabel = null;
	
	/**
	 * The speed average.
	 */
	private float speed = 0;
	
	/**
	 * Total target downloaded.
	 */
	private long target = 0;
	
	/**
	 * Kilo byte constant.
	 */
	private static int KB = 1024;
	
	/**
	 * Mega byte constant.
	 */
	private static int MB = KB * 1024;
	
	/**
	 * Giga byte constant.
	 */
	private static int GB = MB * 1024;
	
	
	// ============================= public methods ==============================	
	/**
	 * Constructor.
	 * 
	 * @param width width of the panel.
	 * @param height height of the panel.
	 */
	public SpeedAndTargetMonitor(int width, int height, Object obj, Method getSpeed, Method getDownloadedBytes) {
				
		this.obj = obj;
		this.getSpeed = getSpeed;
		this.getDownloadedBytes = getDownloadedBytes;
		
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		setBorder(BorderFactory.createCompoundBorder());
					
		// Create the speed label
		Dimension lDims = new Dimension((int)(0.45*width), height);
		speedLabel = new JLabel();
		speedLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
		speedLabel.setSize(lDims);
		speedLabel.setPreferredSize(lDims);
		speedLabel.setMinimumSize(lDims);
		speedLabel.setMaximumSize(lDims);
		
		// Create the target label
		targetLabel = new JLabel();
		targetLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
		lDims = new Dimension((int)(0.55*width), height);
		targetLabel.setSize(lDims);
		targetLabel.setPreferredSize(lDims);
		targetLabel.setMinimumSize(lDims);
		targetLabel.setMaximumSize(lDims);
		
		// Labels in hbox
		Box hBox = Box.createHorizontalBox();
		hBox.add(speedLabel);
		hBox.add(targetLabel);
		
		add(hBox);
		
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

		// Read speed and target
		try {
			speed = (Float) getSpeed.invoke(obj, (Object[])null);
			target = (Long) getDownloadedBytes.invoke(obj, (Object[])null);
		} catch (IllegalArgumentException e) {
			return;
		} catch (IllegalAccessException e) {
			return;
		} catch (InvocationTargetException e) {
			return;
		}
		
		
		// Speed
		String speedText = "<html><b>Speed: </b>";	
		
		if (speed < KB) {
			speedText += (float)Math.round(speed)/100 + " B/s</html>";
			
		} else if (speed < MB) {
			speedText += (float)Math.round(speed*100/KB)/100 + " KB/s</html>";
			
		} else {
			speedText += (float)Math.round(speed*100/MB)/100 + " MB/s</html>";
		}
		speedLabel.setText(speedText);
		
		
		// Target
		String targetText = "<html><b> Download: </b>";
				
		if (target < KB) {
			targetText += target + " B</html>";
			
		} else if (target < MB) {
			targetText += (float)Math.round(target*100/KB)/100 + " KB</html>";
			
		} else if (target < GB){
			targetText += (float)Math.round(target*100/MB)/100 + " MB</html>";
			
		} else {
			targetText += (float)Math.round(target*100/GB)/100 + " GB</html>";
		}
		
		targetLabel.setText(targetText);
	}

}