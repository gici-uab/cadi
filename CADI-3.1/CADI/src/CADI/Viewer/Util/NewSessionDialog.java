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
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * This class opens a graphical dialog windows to introduce the main parameters
 * of a session: target, server, server's port, proxy, proxy's port. Moreover
 * there exist a button to configure advanced parameters from a new dialog
 * window.
 * <p>
 * Usage example:<br>
 * &nbsp; constructor<br>
 * &nbsp; run<br>
 * &nbsp; get methods<br> 
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1.1 2010/01/13
 */
public class NewSessionDialog extends JDialog implements ComponentListener, KeyListener {

	/**
	 * Default with of the dialog window.
	 */
	private static final int DIALOG_WIDTH = 300;
	
	/**
	 * Default height of the dialog window.
	 */
	private static final int DIALOG_HEIGHT = 300;
	
	/**
	 * The owner of this window.
	 */
	private JFrame parent = null;
		
	/**
	 * The last target name. 
	 */
	private String target = null;	
	
	/**
	 * The last server name.
	 */
	private String server = null;
	
	/**
	 * The last port number.
	 */
	private int port = -1;

	/**
	 * Is the server proxy, if it is used.
	 */
	private String proxyServer = null;
	
	/**
	 * Is the proxy port to be used.
	 */
	private int proxyPort = -1;
	
	
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
	private JPanel serverPane = null;
	
	/**
	 * Box where the target line is placed.
	 */
	private Box targetBox = null;
	
	/**
	 * Box where the server line is placed.
	 */
	private Box serverBox = null;
		
	/**
	 * Box where the port number  line is placed.
	 */
	private Box portBox = null;

	/**
	 * Text field to input the target. 
	 */
	private JTextField targetField = null;		
		
	/**
	 * Text field to input the server name.
	 */
	private JTextField serverField = null;

	/**
	 * Text field to input the port number.
	 */
	private JTextField portField = null;
	
	/**
	 * Pane where the {@link #proxyServerBox} and {@link #proxyPortBox}
	 * boxes will be placed.
	 */
	private JPanel proxyPane = null;
	
	/**
	 * Box where the server line is placed.
	 */
	private Box proxyServerBox = null;
		
	/**
	 * Box where the port number  line is placed.
	 */
	private Box proxyPortBox = null;
	
	/**
	 * Text field to input the server name.
	 */
	private JTextField proxyServerField = null;

	/**
	 * Text field to input the port number.
	 */
	private JTextField proxyPortField = null;
	
	/**
	 * Panel where the buttons will be placed.
	 */
	private JPanel buttonsPane = null;
	
	/**
	 * 
	 */
	private JButton sessionPreferences = null;
	
	/**
	 * 
	 */
	private SessionPreferencesDialog sessionPreferencesDialog = null;
	
	/**
	 * Accept button
	 */
	private JButton acceptButton = null;
	
	/**
	 * Cancel button
	 */
	private JButton cancelButton = null;

	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param owner the parent window.
	 */
	public NewSessionDialog(JFrame owner) {
		super(owner, "New Session Dialog", true);
		this.parent = owner;		
		
		sessionPreferencesDialog = new SessionPreferencesDialog(null);
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
	 * Returns the {@link #target} attribute.
	 * 
	 * @return the {@link #target} attribute.
	 */
	public String getTarget() {
		return target;
	}
	
	/**
	 * Returns the {@link #server} attribute.
	 * 
	 * @return the {@link #server} attribute.
	 */
	public String getServer() {
		return server;
	}
	
	/**
	 * Returns the {@link #port} attribute.
	 * 
	 * @return the {@link #port} attribute.
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Returns the {@link #proxyServer} attribute.
	 * 
	 * @return the {@link #proxyServer} attribute.
	 */
	public String getProxyServer() {
		return proxyServer;
	}
	
	/**
	 * Returns the {@link #proxyPort} attribute.
	 * 
	 * @return the {@link #proxyPort} attribute.
	 */
	public int getProxyPort() {
		return proxyPort;
	}
	
	public boolean useSession() {
		return sessionPreferencesDialog.useSession();
	}
	
	public boolean useHTTPSession() {
		return sessionPreferencesDialog.useHTTPSession();
	}
	
	public boolean useHTTPTCPSession() {
		return sessionPreferencesDialog.useHTTPTCPSession();
	}
	
	public int getCachetype() {
		return sessionPreferencesDialog.getCacheType();
	}
	
	public boolean useWildcard() {
		return sessionPreferencesDialog.useWildcard();
	}
	
	public boolean useIndexRange() {
		return sessionPreferencesDialog.useIndexRange();
	}
	
	public boolean useNumberOfLayers() {
		return sessionPreferencesDialog.useNumberOfLayers();
	}
	
	public boolean useNumberOfBytes() {
		return sessionPreferencesDialog.useNumberOfBytes();
	}
	
	public long getMaxCacheSize() {
		return sessionPreferencesDialog.getMaxCacheSize();
	}
	
	public int getManagementPolicy() {
		return sessionPreferencesDialog.getManagementPolicy();
	}

	public boolean useKeepAlive() {
		return sessionPreferencesDialog.useKeepAlive();
	}
	
	public ArrayList<String> getImageReturnTypes() {
		return sessionPreferencesDialog.getImageReturnTypes();
	}

  public boolean useExtendedHeaders() {
    return sessionPreferencesDialog.useExtendedHeaders();
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
		vbox.add(createProxyPane(), BorderLayout.CENTER);
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
	 * Creates the pane where jpip server data are placed.
	 *
	 * @return a JPanel
	 */
	private JPanel createFormPane() {
		if (serverPane == null) {
			serverPane = new JPanel();
			serverPane.setBorder(BorderFactory.createTitledBorder("JPIP server"));
						
			Dimension dimLabel = new Dimension(50, 25);
			int imputTextHeight = 20;
			
			int width = DIALOG_WIDTH - 2 * 10 - 50;
			
			// TARGET
			targetBox = Box.createHorizontalBox();
			
			JLabel targetLabel = new JLabel("Target");
			targetLabel.setSize(dimLabel);
			targetLabel.setMinimumSize(dimLabel);
			targetLabel.setPreferredSize(dimLabel);
			targetLabel.setMaximumSize(dimLabel);
			
			targetField = new JTextField();
			targetField.setEditable(true);
			targetField = new JTextField("", 20);
			targetField.setSize(new Dimension(width, 20));
			targetField.setMinimumSize(new Dimension(width, 20));
			targetField.setMaximumSize(new Dimension(width, 20));
			targetField.setPreferredSize(new Dimension(width, 20));
			targetField.addKeyListener(this);
						
			targetBox.setMinimumSize(new Dimension(width + 50, 20));
			targetBox.setMaximumSize(new Dimension(width + 50, 20));
			targetBox.setPreferredSize(new Dimension(width + 50, 20));
			
			targetBox.add(targetLabel);
			targetBox.add(targetField);
			
			
			// SERVER
			serverBox = Box.createHorizontalBox();
			
			JLabel serverLabel = new JLabel("Host");
			serverLabel.setSize(dimLabel);
			serverLabel.setMinimumSize(dimLabel);
			serverLabel.setPreferredSize(dimLabel);
			serverLabel.setMaximumSize(dimLabel);
			
			serverField = new JTextField("localhost", 20);
			serverField.setSize(new Dimension(width, 20));
			serverField.setMinimumSize(new Dimension(width, 20));
			serverField.setMaximumSize(new Dimension(width, 20));
			serverField.setPreferredSize(new Dimension(width, 20));
			serverField.addKeyListener(this);
		
			serverBox.setSize(new Dimension(width + 50, 20));
			serverBox.setMinimumSize(new Dimension(width + 50, 20));
			serverBox.setMaximumSize(new Dimension(width + 50 , 20));
			serverBox.setPreferredSize(new Dimension(width + 50, 20));
			
			serverBox.add(serverLabel);		
			serverBox.add(serverField);
			
			
			// PORT
			portBox = Box.createHorizontalBox();
			
			JLabel portLabel = new JLabel("Port");
			portLabel.setSize(dimLabel);
			portLabel.setMinimumSize(dimLabel);
			portLabel.setPreferredSize(dimLabel);
			portLabel.setMaximumSize(dimLabel);
			
			portField = new JTextField("8080", 4);
			portField.setHorizontalAlignment(JTextField.RIGHT);
			portField.setSize(75, imputTextHeight);
			portField.setPreferredSize(new Dimension(75, imputTextHeight));
			portField.setMaximumSize(new Dimension(75, imputTextHeight));			
			portField.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
					portField.setBackground(Color.WHITE);
					acceptButton.setEnabled(true);
				}
				public void focusLost(FocusEvent e) {
					try {
						Integer.parseInt(portField.getText());
					} catch (NumberFormatException nfe) {
						portField.setBackground(Color.RED);
						acceptButton.setEnabled(false);
					}
				}	
			});
			
			portBox.add(portLabel);
			portBox.add(portField);
			portBox.add(Box.createHorizontalGlue());
			
			
			// Add horizontal boxes to the vertical
			Box vbox = Box.createVerticalBox();
			vbox.add(targetBox);
			vbox.add(Box.createVerticalStrut(10));
			vbox.add(serverBox);
			vbox.add(Box.createVerticalStrut(10));
			vbox.add(portBox);
			
			serverPane.add(vbox);			
		}
		
		return serverPane;
	}
	
	/**
	 * Creates the pane where the proxy data are placed.
	 * 
	 * @return
	 */
	private JPanel createProxyPane() {
		if (proxyPane == null) {
			proxyPane = new JPanel();
			proxyPane.setBorder(BorderFactory.createTitledBorder("Proxy server"));
						
			Dimension dimLabel = new Dimension(50, 25);
			int imputTextHeight = 20;
			
			int width = DIALOG_WIDTH - 2 * 10 - 50;
			
			// PROXY SERVER
			proxyServerBox = Box.createHorizontalBox();
			
			JLabel serverLabel = new JLabel("Host");
			serverLabel.setSize(dimLabel);
			serverLabel.setMinimumSize(dimLabel);
			serverLabel.setPreferredSize(dimLabel);
			serverLabel.setMaximumSize(dimLabel);
			
			proxyServerField = new JTextField(null, 20);
			proxyServerField.setSize(new Dimension(width, 20));
			proxyServerField.setMinimumSize(new Dimension(width, 20));
			proxyServerField.setMaximumSize(new Dimension(width, 20));
			proxyServerField.setPreferredSize(new Dimension(width, 20));
			proxyServerField.addKeyListener(this);
		
			proxyServerBox.setSize(new Dimension(width + 50, 20));
			proxyServerBox.setMinimumSize(new Dimension(width + 50, 20));
			proxyServerBox.setMaximumSize(new Dimension(width + 50 , 20));
			proxyServerBox.setPreferredSize(new Dimension(width + 50, 20));
			
			proxyServerBox.add(serverLabel);		
			proxyServerBox.add(proxyServerField);
			
			
			// PROXY PORT
			proxyPortBox = Box.createHorizontalBox();
			
			JLabel portLabel = new JLabel("Port");
			portLabel.setSize(dimLabel);
			portLabel.setMinimumSize(dimLabel);
			portLabel.setPreferredSize(dimLabel);
			portLabel.setMaximumSize(dimLabel);
			
			proxyPortField = new JTextField(null, 4);
			proxyPortField.setHorizontalAlignment(JTextField.RIGHT);
			proxyPortField.setSize(75, imputTextHeight);
			proxyPortField.setPreferredSize(new Dimension(75, imputTextHeight));
			proxyPortField.setMaximumSize(new Dimension(75, imputTextHeight));
			proxyPortField.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
					proxyPortField.setBackground(Color.WHITE);
					acceptButton.setEnabled(true);
				}
				public void focusLost(FocusEvent e) {
					if ((proxyPortField.getText() != null) &&
							(proxyPortField.getText().length() > 0)) {
						try {
							Integer.parseInt(proxyPortField.getText());
						} catch (NumberFormatException nfe) {
							proxyPortField.setBackground(Color.RED);
							acceptButton.setEnabled(false);
						}
					}
				}
			});
			
			proxyPortBox.add(portLabel);
			proxyPortBox.add(proxyPortField);
			proxyPortBox.add(Box.createHorizontalGlue());

			
			// Add horizontal boxes to the vertical
			Box vbox = Box.createVerticalBox();
			vbox.add(proxyServerBox);
			vbox.add(Box.createVerticalStrut(10));
			vbox.add(proxyPortBox);
			
			proxyPane.add(vbox);	
		}
		
		return proxyPane;
	}
		
	/**
	 * Creates the panel where buttons will be located.
	 * 
	 * @return a <code>JPanel</code> with the buttons.
	 */
	private JPanel createButtonsPane() {
		
		if (buttonsPane == null) {

			// Preference button
			sessionPreferences = new JButton("Preferences");
			Dimension bPrefDims = new Dimension(100, 25);
			sessionPreferences.setSelected(true);
			sessionPreferences.setSize(bPrefDims);
			sessionPreferences.setPreferredSize(bPrefDims);
			sessionPreferences.setMinimumSize(bPrefDims);
			sessionPreferences.setMaximumSize(bPrefDims);
			sessionPreferences.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					openSessionPreferences();
				}
				public void mouseEntered(MouseEvent e) {
				}
				public void mouseExited(MouseEvent e) {
				}
				public void mousePressed(MouseEvent e) {
				}
				public void mouseReleased(MouseEvent e) {
				}
			});
			sessionPreferences.addKeyListener(this);

				
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
				acceptButton.addMouseListener(new MouseListener() {
					public void mouseClicked(MouseEvent e) {	
					}
					public void mouseEntered(MouseEvent e) {
					}
					public void mouseExited(MouseEvent e) {
					}
					public void mousePressed(MouseEvent e) {
					}
					public void mouseReleased(MouseEvent e) {
						server = serverField.getText();
						port = Integer.parseInt(portField.getText());
						target = targetField.getText();
						if (proxyServerField.getText().length() > 0)
							proxyServer = proxyServerField.getText();
						if (proxyPortField.getText().length() > 0)
							proxyPort = Integer.parseInt(proxyPortField.getText());
										
						setVisible(false);
						dispose();
					}
				});
			}
			 
			// Cancel
			if (cancelButton == null) {
				cancelButton = new JButton("Cancel");
				cancelButton.setSize(bDimensions);
				cancelButton.setPreferredSize(bDimensions);
				cancelButton.setMinimumSize(bDimensions);
				cancelButton.setMaximumSize(bDimensions);
				cancelButton.addMouseListener(new MouseListener() {
					public void mouseClicked(MouseEvent e) {
					}
					public void mouseEntered(MouseEvent e) {
					}
					public void mouseExited(MouseEvent e) {
					}
					public void mousePressed(MouseEvent e) {
					}
					public void mouseReleased(MouseEvent e) {
						setVisible(false);
						dispose();
					}
				});
			}
			
			// Add buttons to panel
			buttonsPane.add(sessionPreferences);
			buttonsPane.add(Box.createHorizontalGlue());
			buttonsPane.add(acceptButton);			
			buttonsPane.add(Box.createHorizontalStrut(10));
			buttonsPane.add(cancelButton);
									
		}	
		
		return buttonsPane;
	}
	
	private void openSessionPreferences() {
		sessionPreferencesDialog = new SessionPreferencesDialog(null);
		sessionPreferencesDialog.run();	
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
		targetBox.setSize(new Dimension(width + 50, 20));
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
		serverBox.setPreferredSize(new Dimension(width + 50, 20));		  
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(ComponentEvent e) {
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
				server = serverField.getText();
				port = Integer.parseInt(portField.getText());
				target = targetField.getText();
				proxyServer = proxyServerField.getText();
				proxyPort = Integer.parseInt(proxyPortField.getText());
				
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