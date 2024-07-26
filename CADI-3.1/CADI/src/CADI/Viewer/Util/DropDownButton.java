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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

/**
 * This class implements a drop down button. The buttons to be included in the
 * panel will be passes to the constructor. This class performs the change of
 * the selected button through the selector button (an arrow). Moreover, only
 * the selected button events are captured but they are not processed, so the
 * parent must be pass the listener throw the {@link #addActionListener(ActionListener)}
 * method and the parent may know the selected button through the
 * {@link #getIndexOfSelectedButton()} method.
 * <p>
 * Usage example:<br>
 * &nbsp; constructor<br>
 * &nbsp; addActionListener<br>
 * &nbsp; getIndexOfSelectedButton<br> 
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2007-2012/12/17
 */
public class DropDownButton extends JPanel {


	/**
	 * The dimensions of the buttons.
	 */
	private Dimension buttonDims = null;
	
	/**
	 * An array with the available buttons of the drop down button.
	 */
	private JButton[] buttons = null;
		
    /**
     * Is the action listener for the selected button.
     */
    private ActionListener actionListener = null;
    
	
	// INTERNAL ATTRIBUTES
	
	/**
	 * Is the button which is visible.
	 */
	private JButton selectedButton = null;
	
	/**
	 * Is the index of the {@link #buttons} array of the
	 * {@link #selectedButton} button.
	 */
	private int indexOfSelectedButton = 0;
	
    /**
     * Selector button (shows an arrow).
     */
    private JButton selectorButton = null;
    
	/**
	 * The popup where the {@link #chooserPanel} will be placed.
	 */
	private Popup popup;

	/**
	 * Button chooser panel.
	 */
	private JPanel chooserPanel;
    
  // ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param icons image icons for the buttons.
	 * @param toolTipTexts tool tip texts for the buttons.
	 * @param buttonWidth width of the buttons.
	 * @param buttonHeight height of the buttons.
	 */
	public DropDownButton(ImageIcon[] icons, String[] toolTipTexts, int buttonWidth, int buttonHeight) {
		buttonDims = new Dimension(buttonWidth, buttonHeight);				
		buttons = new JButton[icons.length];
		for (int i = 0; i < icons.length; i++) {
			buttons[i] = createButton(i, icons[i], toolTipTexts[i]);
		}
		initialize();
	}
		
	/**
	 * Adds the action listener for the selected button. 
	 * 
	 * @param actionListener an action listener.
	 */
	public void addActionListener(ActionListener actionListener) {		
		this.actionListener = actionListener;
		selectedButton.addActionListener(actionListener);
	}
    
	/**
	 * Returns the index of the selected button. The index of the buttons is
	 * the same of the {@link #DropDownButton(ImageIcon[], String[], int, int)}'s
	 * <code>icons</code> parameter. 
	 * 
	 * @return the index of the selected button.
	 */
	public int getIndexOfSelectedButton() {
		return indexOfSelectedButton;
	}	

  // ============================ private methods ==============================
	/**
	 * Creates a button.
	 * 
	 * @param index index of the button in the {@link #buttons} array.
	 * @param icon the icon image.
	 * @param toolTipText the tool tip text
	 * 
	 * @return a JButton.
	 */
	private JButton createButton(final int index, ImageIcon icon, String toolTipText) {
    	
    	JButton button = new JButton(icon);
    	if (toolTipText != null) button.setToolTipText(toolTipText);
    	button.setSize(buttonDims);    	
    	button.setPreferredSize(buttonDims);
    	button.setMinimumSize(buttonDims);
    	button.setMaximumSize(buttonDims);
    	button.setMargin(new Insets(0, 0, 0, 0));
    	
    	button.setRequestFocusEnabled(false);
    	button.setFocusable(true);    	
    	button.setBorderPainted(true);
    	button.setFocusPainted(true);
    	
    	button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {			
				changeButton(index);
			}
    	});
    	
    	return button;
    }        
    
    /**
     * Performs the initializations.
     */
	private void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // Selected button
		selectedButton = new JButton(buttons[0].getIcon());
		selectedButton.setToolTipText(buttons[0].getToolTipText());
		selectedButton.setSize(buttonDims);
		selectedButton.setPreferredSize(buttonDims);
		selectedButton.setMinimumSize(buttonDims);
		selectedButton.setMaximumSize(buttonDims);	
		selectedButton.setRequestFocusEnabled(false);
		selectedButton.setFocusable(false);
		selectedButton.setMargin(new Insets(0, 0, 0, 0));
		selectedButton.addActionListener(actionListener);
		add(selectedButton);
		
		// Icon image for selector button (arrow)        
        Image image = new BufferedImage(buttonDims.width / 3, buttonDims.height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = image.getGraphics();
        g.setColor(Color.BLACK);
        int[] x = {buttonDims.width / 6 - 3, buttonDims.width / 6 + 3, buttonDims.width / 6};
        int[] y = {buttonDims.height / 2, buttonDims.height / 2, buttonDims.height / 2 + 3};
        g.fillPolygon(x, y, 3);
		
        // Selector button
		selectorButton = new JButton(new ImageIcon(image));
		Dimension selectorButtonDims = new Dimension(buttonDims.width / 5, buttonDims.height);
		selectorButton.setSize(selectorButtonDims);
		selectorButton.setPreferredSize(selectorButtonDims);
		selectorButton.setMinimumSize(selectorButtonDims);
		selectorButton.setMaximumSize(selectorButtonDims);
		selectorButton.setRequestFocusEnabled(false);
		selectorButton.setFocusable(false);
		selectorButton.setMargin(new Insets(0, 0, 0, 0));
		selectorButton.addActionListener(
				new ActionListener() {					
					public void actionPerformed(ActionEvent event) {
						if (popup != null) {
							closePopup();
						} else {
                            Point location = getLocationOnScreen(); 
                            location.y += getHeight();
							popup = PopupFactory.getSharedInstance().getPopup(DropDownButton.this, createChooserPanel(), location.x, location.y);						
							popup.show();
						}
					}
				});
		add(selectorButton);
	}
    
	/**
	 * Build the chooser panel. If the it has already been built, just return
	 * the same.
	 *
	 * @return the chooser panel.
	 */
	private JPanel createChooserPanel() {

		if (chooserPanel == null) {
			chooserPanel = new JPanel(new BorderLayout());
			chooserPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
			chooserPanel.setFocusable(false);

			// 
			JPanel panel = new JPanel(new GridLayout(buttons.length, 1));
			panel.setFocusable(false);

			for (int i = 0; i < buttons.length; i++) {
				panel.add(buttons[i]);
			}

			panel.setPreferredSize(new Dimension(buttonDims.width + 2, (buttonDims.height + 2) * buttons.length));
			chooserPanel.add(panel, BorderLayout.CENTER);
		}

		chooserPanel.setSize((buttonDims.width + 3), (buttonDims.height + 3) * buttons.length);
		return chooserPanel;
	}
    
	/**
	 * Change the selected button.
	 * 
	 * @param index index of the new selected button.
	 */
	private void changeButton(int index) {
		if (indexOfSelectedButton != index) {
			indexOfSelectedButton = index;
			selectedButton.setIcon(buttons[index].getIcon());
			selectedButton.setToolTipText(buttons[index].getToolTipText());
			closePopup();
			selectedButton.repaint();
		} else {
			closePopup();
		}
    }

	/**
	 * Close the popup.
	 */
	private void closePopup() {
		if (popup != null) {
			popup.hide();
			popup = null;
		}
	}	

}