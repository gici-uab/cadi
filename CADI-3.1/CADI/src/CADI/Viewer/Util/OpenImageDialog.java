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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 03/08/2009
 */
public class OpenImageDialog  extends JDialog implements MouseListener, ComponentListener, FocusListener, KeyListener {

	/**
	 * Default with of the dialog window.
	 */
	private static final int DIALOG_WIDTH = 500;
	
	/**
	 * Default height of the dialog window.
	 */
	private static final int DIALOG_HEIGHT = 300;
	
	/**
	 * The owner of this window.
	 */
	private JFrame parent = null;
		
	/**
	 * Image file name. 
	 */
	private String fileName = null;	
	
	private int zSize;
	private int ySize;
	private int xSize;
	private Class sampleType;
	private int byteOrder;
	private boolean RGBComponents;
	
	
	// INTERNAL ATTRIBUTES
	
	/**
	 * Width of the window.
	 */
	private int width = DIALOG_WIDTH;
	
	/**
	 * Height of the window.
	 */
	private int height = DIALOG_HEIGHT;
	
	/**
	 * Pane where the {@link #targetBox}, {@link #serverBox}, and
	 * {@link #portBox} will be p.
	 */
	private JPanel formPane = null;
	
	/**
	 * Box where the target line is placed.
	 */
	private Box fileNameBox = null;
	
	/**
	 * Text field to input the target. 
	 */
	private JTextField targetField = null;		
	
	private Box imageGeometryBox1 = null;
	
	JTextField zSizeText = null;
	JTextField ySizeText = null;
	JTextField xSizeText = null;
	
	private Box imageGeometryBox2 = null;
	
	JComboBox sampleTypeBox = null;
	JComboBox byteOrderBox = null;
	JComboBox rgbBox  = null;
	
	/**
	 * Panel where the buttons will be placed.
	 */
	private JPanel buttonsPane = null;
	
	/**
	 * Accept button
	 */
	private JButton acceptButton = null;
	
	/**
	 * Cancel button
	 */
	private JButton cancelButton = null;

	/**
	 * Examine button used to select an image
	 */
	private JButton examineButton = null;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param owner the parent window.
	 */
	public OpenImageDialog(JFrame owner) {
		super(owner, "Open image dialog", true);
		this.parent = owner;		
	}
	
	/**
	 * Displays the dialog.
	 */
	public void run() {		
		addComponentListener(this);
		addKeyListener(this);
		createAndShowGUI();
	}
	
	/**
	 * Returns the {@link #fileName} attribute.
	 * 
	 * @return the {@link #fileName} attribute.
	 */
	public String getFileName() {
		return targetField.getText();
	}
	
	public int getZSize() {
		return Integer.parseInt(zSizeText.getText());
	}
	
	public int getYSize() {
		return Integer.parseInt(ySizeText.getText());
	}
	
	public int getXSize() {
		return Integer.parseInt(xSizeText.getText());
	}
	
	public int getSampleType() {
		return sampleTypeBox.getSelectedIndex();
	}
	
	public int getByteOrder() {
		return byteOrderBox.getSelectedIndex();
	}
	
	public boolean isRGB() {
		return rgbBox.getSelectedIndex() == 0 ? false : true;
	}
	
	// ============================ private methods ==============================
	/**
	 * Creates and shows the graphical interface and shows it.
	 */
	private void createAndShowGUI() {
				
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
				
		// Create a vertical box imput data and buttons
        Box vbox = Box.createVerticalBox();
        
        Dimension dim = new Dimension(width, height);        
        vbox.setPreferredSize(dim);
        vbox.setMaximumSize(dim);
        vbox.setMinimumSize(dim);
		
        vbox.add(createFormPane(), BorderLayout.CENTER);
        vbox.add(createButtonsPane(), BorderLayout.SOUTH);
		
		contentPane.add(vbox);	
        
		// Frame settings
		setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		setPreferredSize(dim);
		setMinimumSize(dim);
		setMaximumSize(dim);
				
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
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);		
	}
	
	/**
	 * Creates the pane where are place the input text fields.
	 */
	private JPanel createFormPane() {
		if (formPane == null) {
			formPane = new JPanel();
			formPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
						
			Dimension dimExamineButton = new Dimension(100, 25);
			
			// TARGET
			fileNameBox = Box.createHorizontalBox();
			
			int width = DIALOG_WIDTH - 2 * 10 - 20 - 100;
			targetField = new JTextField();
			targetField.setEditable(true);
			targetField = new JTextField();
			targetField.setMargin(new Insets(2,2,2,2));
			targetField.setSize(new Dimension(width, 20));
			targetField.setMinimumSize(new Dimension(width, 20));
			targetField.setMaximumSize(new Dimension(width, 20));
			targetField.setPreferredSize(new Dimension(width, 20));
			targetField.addKeyListener(this);
			
			
			
			examineButton = new JButton("Examine");
			examineButton.setVisible(true);
			examineButton.setSelected(true);
			examineButton.setSize(dimExamineButton);
			examineButton.setPreferredSize(dimExamineButton);
			examineButton.setMinimumSize(dimExamineButton);
			examineButton.setMaximumSize(dimExamineButton);
			examineButton.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					 FileNameExtensionFilter filter = new FileNameExtensionFilter(
					        "raw, img, ppm", "raw", "img", "ppm", "pgm");
					JFileChooser jFileChooser = new JFileChooser();
					jFileChooser.setFileFilter(filter);
					jFileChooser.showOpenDialog(OpenImageDialog.this);	
					String tmpFileName = jFileChooser.getSelectedFile().getName();
					String tmpFolder = jFileChooser.getSelectedFile().getParent();
					targetField.setText(tmpFolder+File.separator+tmpFileName);
				}
				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
				public void mouseReleased(MouseEvent e) {}
				
			});

			
			fileNameBox.setMinimumSize(new Dimension(width + 100, 20));
			fileNameBox.setMaximumSize(new Dimension(width + 100, 20));
			fileNameBox.setPreferredSize(new Dimension(width + 100, 20));

			fileNameBox.add(examineButton);
			fileNameBox.add(Box.createHorizontalGlue());
			fileNameBox.add(targetField);

			
			// IMAGE FEATURES
			
			imageGeometryBox1 = Box.createHorizontalBox();
			JLabel zSizeLabel = new JLabel("zSize");
			zSizeText = new JTextField();
			JLabel ySizeLabel = new JLabel("ySize");
			ySizeText = new JTextField();
			JLabel xSizeLabel = new JLabel("xSize");
			xSizeText = new JTextField();
			
			imageGeometryBox1.add(zSizeLabel);
			imageGeometryBox1.add(zSizeText);
			imageGeometryBox1.add(ySizeLabel);
			imageGeometryBox1.add(ySizeText);
			imageGeometryBox1.add(xSizeLabel);
			imageGeometryBox1.add(xSizeText);
			
			
			imageGeometryBox2 = Box.createHorizontalBox();
			
			JLabel sampleTypeLabel = new JLabel("Sample Type");
			sampleTypeBox = new JComboBox();
			sampleTypeBox.addItem("Boolean");
			sampleTypeBox.addItem("Byte");
			sampleTypeBox.addItem("Char");
			sampleTypeBox.addItem("Short");
			sampleTypeBox.addItem("Integer");
			sampleTypeBox.addItem("Long");
			sampleTypeBox.addItem("Float");
			sampleTypeBox.addItem("Double");
			
			JLabel byteOrderLabel = new JLabel("Byte order");
			byteOrderBox = new JComboBox();
			byteOrderBox.addItem("Big-Endian");
			byteOrderBox.addItem("Little-Endian");
			
			JLabel rgbLabel = new JLabel("RGB");
			rgbBox = new JComboBox();
			rgbBox.addItem("No");
			rgbBox.addItem("Yes");
			
			imageGeometryBox2.add(sampleTypeLabel);
			imageGeometryBox2.add(sampleTypeBox);
			imageGeometryBox2.add(byteOrderLabel);
			imageGeometryBox2.add(byteOrderBox);
			imageGeometryBox2.add(rgbLabel);
			imageGeometryBox2.add(rgbBox);
			
			
			// Add horizontal boxes to the vertical
			Box vbox = Box.createVerticalBox();
			vbox.add(fileNameBox);
			vbox.add(Box.createVerticalStrut(30));
			vbox.add(imageGeometryBox1);
			vbox.add(Box.createVerticalStrut(10));
			vbox.add(imageGeometryBox2);
			
			formPane.add(vbox);			
		}
		
		return formPane;
	}
		
	/**
	 * Creates the panel where buttons will be located.
	 * 
	 * @return a <code>JPanel</code> with the buttons.
	 */
	private JPanel createButtonsPane() {
		
		if (buttonsPane == null) {
			
			// Panel
			buttonsPane = new JPanel();
			buttonsPane.setLayout(new BoxLayout(buttonsPane, BoxLayout.X_AXIS));
			buttonsPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));	
			
			Dimension bDimensions = new Dimension(75, 25);
			
			// Accpet
			if (acceptButton == null) {
				acceptButton = new JButton("Accept");
				acceptButton.setSelected(true);
				acceptButton.setSize(bDimensions);
				acceptButton.setPreferredSize(bDimensions);
				acceptButton.setMinimumSize(bDimensions);
				acceptButton.setMaximumSize(bDimensions);
				acceptButton.addMouseListener(this);
				acceptButton.addKeyListener(this);
			}
			 
			// Cancel
			if (cancelButton == null) {
				cancelButton = new JButton("Cancel");
				cancelButton.setSize(bDimensions);
				cancelButton.setPreferredSize(bDimensions);
				cancelButton.setMinimumSize(bDimensions);
				cancelButton.setMaximumSize(bDimensions);
				cancelButton.addMouseListener(this);
				cancelButton.addKeyListener(this);
			}
			
			// Add buttons to panel
			buttonsPane.add(Box.createHorizontalGlue());
			buttonsPane.add(acceptButton);			
			buttonsPane.add(Box.createHorizontalStrut(10));
			buttonsPane.add(cancelButton);
									
		}	
		
		return buttonsPane;
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
		
		if (e.getSource() == acceptButton) {
			
							
			setVisible(false);
			dispose();	
			
		} else if (e.getSource() == cancelButton) {
			setVisible(false);
			dispose();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	public void componentHidden(ComponentEvent e) {		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	public void componentMoved(ComponentEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	public void componentResized(ComponentEvent e) {
		
		// Adjust target and server fields to the window width		
		Dimension dialogSize = getSize();
		int width = dialogSize.width - 2 * 10 - 50; // dialogWidth - 2 * border - labelWidth
		
		targetField.setSize(new Dimension(width, 20));
		targetField.setMinimumSize(new Dimension(width, 20));
		targetField.setMaximumSize(new Dimension(width, 20));
		targetField.setPreferredSize(new Dimension(width, 20));
		/*targetBox.setSize(new Dimension(width + 50, 20));
		targetBox.setMinimumSize(new Dimension(width + 50, 20));
		targetBox.setMaximumSize(new Dimension(width + 50 , 20));
		targetBox.setPreferredSize(new Dimension(width + 50, 20));
		
		serverField.setSize(new Dimension(width, 20));
		serverField.setMinimumSize(new Dimension(width, 20));
		serverField.setMaximumSize(new Dimension(width, 20));
		serverField.setPreferredSize(new Dimension(width, 20));
		serverBox.setSize(new Dimension(width + 50, 20));
		serverBox.setMinimumSize(new Dimension(width + 50, 20));
		serverBox.setMaximumSize(new Dimension(width + 50 , 20));
		serverBox.setPreferredSize(new Dimension(width + 50, 20));*/
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(ComponentEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public void focusGained(FocusEvent e) {
		/*if (e.getSource() == portField) {
			portField.setBackground(Color.WHITE);
			acceptButton.setEnabled(true);
		}*/
	}

	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public void focusLost(FocusEvent e) {
		/*if (e.getSource() == portField) {
			try {
			 Integer.parseInt(portField.getText());
			} catch (NumberFormatException nfe) {
				portField.setBackground(Color.RED);
				acceptButton.setEnabled(false);
			}
		}*/
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();

		if (keyCode == KeyEvent.VK_ENTER) {
			
			if (e.getSource() == cancelButton) {
				setVisible(false);
				dispose();
			} else {			
				//server = serverField.getText();
				//port = Integer.parseInt(portField.getText());
				//target = targetField.getText();

				setVisible(false);
				dispose();
			} 
			
		} else if (keyCode == KeyEvent.VK_ESCAPE) {
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
	
}