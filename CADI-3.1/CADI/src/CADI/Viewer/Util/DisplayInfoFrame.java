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
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * This class creates a new window where to display information. It is similar
 * to a JDialog.
 * <p>
 * Usage example:<br>
 * &nbsp; constructor<br>
 * &nbsp; display<br>  
 *  
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2007-2012/12/09
 */
public class DisplayInfoFrame extends JFrame implements MouseListener, KeyListener {

	/**
	 * Reference to the parent window.
	 */
	private JFrame parent = null;
	
	/**
	 * Width of the window.
	 */
	private int width = 0;
	
	/**
	 * Height of the window.
	 */
	private int height = 0;
	
	/**
	 * Title of the window.
	 */
	private String title = "";
	
	/**
	 * Message with the information to show.
	 */
	private String msg = null;
	
	
	// INTERNAL ATTRIBUTES
	
	/**
	 * Button accept to close the window
	 */
	private JButton buttonOK;	
	
	/**
	 * Is the scroll pane where the message will be displayed.
	 */
	JScrollPane scroller = null;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * <p>
	 * If the <code>width</code> and <code>height</code> are <code>0</code> or
	 * smaller, the window preferred size is fit to the size preferred
	 * components. 
	 * 
	 * @param parent reference to the parent window.
	 * @param width preferred width of the window.
	 * @param height preferred height of the window.
	 * @param title title of the window.
	 */
	public DisplayInfoFrame(JFrame parent, int width, int height, String title) {
		super();
		
		this.parent = parent;
		this.width = width;
		this.height = height;
		this.title = title;					
	}
	
	/**
	 * Is used to display a message.
	 * 
	 * @param msg the message to be displayed.
	 */
	public void display(String msg) {
		this.msg = msg;
		createAndShowGUI();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		if (e.getSource() == buttonOK) {			
			setVisible(false);
			dispose();
		}
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();

		if ( (keyCode == KeyEvent.VK_ENTER) || (keyCode == KeyEvent.VK_ESCAPE) ) {
			setVisible(false);			
			dispose();
		}		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
	}
	
	// ============================ private methods ==============================
	/**
	 * Creates a graphic user interface and show the text.
	 */
	private void createAndShowGUI() {

		// Create principal frame		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE );
		getContentPane().setLayout(new BorderLayout(0, 0));
		setTitle(title);
				
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		width = width > screenSize.width ? screenSize.width : width;
		height = height > screenSize.height ? screenSize.height : height;
		
		if ( (width > 0) || (height > 0) ) {
			Dimension dims = new Dimension(width, height);
			setSize(dims);
			setPreferredSize(dims);
			setMinimumSize(dims);
			setMaximumSize(dims);
		}
		
		// Is the size of the border for each element
		int borderSize = 10;
		
		// Label
		JLabel label = new JLabel();
		label.setFont(new Font("SansSerif", Font.PLAIN, 12));
		label.setText(msg);
		
		// JScrollPane
		scroller = new JScrollPane(label);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.setBorder(BorderFactory.createEmptyBorder(borderSize, borderSize, borderSize, borderSize));
		
		// Button OK
		buttonOK = new JButton("OK");
		Dimension bDims = new Dimension(75, 25);
		buttonOK.setSize(bDims);
		buttonOK.setPreferredSize(bDims);
		buttonOK.setMinimumSize(bDims);
		buttonOK.setMaximumSize(bDims);
		buttonOK.addMouseListener(this);
		buttonOK.addKeyListener(this);
		buttonOK.setFocusable(true);
				
		Box buttonBox = Box.createHorizontalBox();
		Dimension boxDims = new Dimension(bDims.width + 2 * borderSize, bDims.height + 2 * borderSize);
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(buttonOK);
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.setSize(boxDims);
		buttonBox.setPreferredSize(boxDims);
		buttonBox.setMinimumSize(boxDims);
		buttonBox.setMaximumSize(boxDims);
		buttonBox.setBorder(BorderFactory.createEmptyBorder(borderSize, borderSize, borderSize, borderSize));
		
		//Adding components to the frame
		getContentPane().add(scroller,BorderLayout.CENTER);
		getContentPane().add(buttonBox,BorderLayout.SOUTH);

		// Positioning the frame on the screen
		if (parent != null) {
			Point parentLocation = parent.getLocation();
			Dimension parentSize = parent.getSize();
			Point center = new Point (parentLocation.x + (parentSize.width / 2), parentLocation.y + (parentSize.height / 2) );

			int xLocation = center.x - width / 2;
			int yLocation = center.y - height / 2;
			if ( (xLocation >= 0) && (yLocation >= 0) ) {
				setLocation(xLocation, yLocation);
			} else {
				setLocationRelativeTo(parent);
			}
		} else {
			setLocationByPlatform(true);
		}
		
		setResizable(true);
		pack();
		setVisible(true);
	}

}
