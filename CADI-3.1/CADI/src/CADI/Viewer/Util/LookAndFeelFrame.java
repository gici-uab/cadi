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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;

/**
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2007-2012/12/03
 */
public class LookAndFeelFrame extends JDialog {
	
	//	 SUN MANUAL: JAVA LOOK AND FEEL DESIGN GUIDELINES
	// http://java.sun.com/products/jlf/ed2/book/index.html

	/**
	 * Properties frame size
	 */
	private static final int DIALOG_WIDTH = 500;
	private static final int DIALOG_HEIGHT = 300;

	/**
	 * 
	 */
	private static final String TITLE = "Change the Look and Feel";

	/**
	 * 
	 */
	private final String DEFAULT_LOOKANDFEEL = "Metal";
	private final String DEFAULT_LOOKANDFEEL_CLASSNAME = "javax.swing.plaf.metal.MetalLookAndFeel";
	/**
	 *	Specify the look and feel to use 
	 */
	private String lookAndFeel = DEFAULT_LOOKANDFEEL;
	private String lookAndFeelClassName = DEFAULT_LOOKANDFEEL_CLASSNAME;
	
	private UIManager.LookAndFeelInfo[] lookAndFeelList = null;

	/**
	 * 
	 */
	private Themes themes = new Themes();

	/**
	 * 
	 */
	private final String DEFAULT_THEME = "Ocean";
	
	/**
	 * If you choose the Metal L&F, you can also choose a theme.
	 * Specify the theme to use by defining the THEME constant
	 * Valid values are: "DefaultMetal", or  "Ocean"
	 */
	private MetalTheme theme = themes.getTheme(DEFAULT_THEME);
	
	/**
	 * 
	 */
	private JFrame parent = null;

	/**
	 * 
	 */
	private Container jDialogPane = null;

	/**
	 * 
	 */
	private JPanel lookAndFeelFrame = null;

	/**
	 * 
	 */
	private JPanel buttonsPane = null;

	/**
	 * 
	 */
	private JButton acceptButton = null;

	/**
	 * 
	 */
	private JButton defaultButton = null;

	/**
	 * 
	 */
	private JButton cancelButton = null;

	/**
	 * 
	 */
	private JRadioButton[] lookAndFeelButtons = null;

	/**
	 * 
	 */
	private JRadioButton[] themeButtons = null;

	/**
	 * 
	 */
	private FrameListener frameListener = null;

  // ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param owner the parent window 
	 */
	public LookAndFeelFrame(JFrame owner) {
		this.parent = owner;	
	}

	/**
	 * 
	 *
	 */
	public void change() {
		
		// Dialog frame properties
		jDialogPane = this.getContentPane();
		jDialogPane.setLayout(new BoxLayout(jDialogPane, BoxLayout.Y_AXIS));
		jDialogPane.add(createFrame());
		jDialogPane.add(Box.createVerticalStrut(20)); 
		jDialogPane.add(createButtonsPane());

		// Frame settings
		setModal(true);
		setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		setTitle(TITLE);
		setLocationRelativeTo(parent);	
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
		pack();
	}
	
	/**
	 * Return the name of the look and feel.
	 * 
	 * @return the name of the look and feel.
	 */
	public String getLookAndFeel() {
		return lookAndFeelClassName;
	}
	
	/**
	 * Returns the theme which is being used.
	 * 
	 * @return the theme which is being used.
	 */
	public MetalTheme getTheme() {
		return theme;
	}
	
	/**
	 * Sets the look and feel and the theme.
	 * 
	 * @param lookAndFeel the class name of the look and feel.
	 * @param theme an object with the theme.
	 */
	public static void setLookAndFeel(Window frame, String lookAndFeel, MetalTheme theme) {

		try {
			
			if (theme != null) {
				MetalLookAndFeel.setCurrentTheme(theme);				
			}
			UIManager.setLookAndFeel(lookAndFeel);			
			SwingUtilities.updateComponentTreeUI(frame);
		
			/*
			try {
	            UIManager.setLookAndFeel(
	                UIManager.getCrossPlatformLookAndFeelClassName());
	        } catch (Exception e) { }
	        */
	                
	        //Make sure we have nice window decorations.
	        //JFrame.setDefaultLookAndFeelDecorated(true);
			
			
		} catch (ClassNotFoundException e) {
			System.err.println("Couldn't find class for specified look and feel:" + lookAndFeel);
			System.err.println("Did you include the L&F library in the class path?");
			System.err.println("Using the default look and feel."); 
		} catch (UnsupportedLookAndFeelException e) {
			System.err.println("Can't use the specified look and feel (" + lookAndFeel + ") on this platform.");
			System.err.println("Using the default look and feel.");
		} catch (Exception e) {
			System.err.println("Couldn't get specified look and feel (" + lookAndFeel + "), for some reason.");
			System.err.println("Using the default look and feel.");
			e.printStackTrace();
		}
	}
	
	// ============================ private methods ==============================
	/**
	 * 
	 */
	private JPanel createFrame() {

		if (lookAndFeelFrame == null) {
			lookAndFeelFrame = new JPanel();			
			frameListener = new FrameListener();

			// Create the components
			JPanel dialogPanel = createDialogBox();

			//lay them out
			Border padding = BorderFactory.createEmptyBorder(20,20,5,20);
			dialogPanel.setBorder(padding);

			lookAndFeelFrame.setLayout(new BorderLayout());
			lookAndFeelFrame.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			lookAndFeelFrame.add(dialogPanel, BorderLayout.CENTER);	
		}

		return lookAndFeelFrame;
	}

	/**
	 * Creates the buttons panel.
	 * 
	 * @return a JPanel with the buttons pane.
	 */
	private JPanel createButtonsPane() {
		if (buttonsPane == null) {
			buttonsPane = new JPanel();
			buttonsPane.setLayout(new BoxLayout(buttonsPane, BoxLayout.X_AXIS));
			buttonsPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));		

			buttonsPane.add(Box.createHorizontalGlue());
			if (acceptButton == null) {
				acceptButton = new JButton("Accept");
				acceptButton.addMouseListener(new MouseListener(){
					public void mouseClicked(MouseEvent event){}
					public void mouseEntered(MouseEvent event){}
					public void mouseExited(MouseEvent event){}
					public void mousePressed(MouseEvent event){}
					public void mouseReleased(MouseEvent event){
						updateLookFeelTheme();
						setVisible(false);
						dispose();
					}
				});
			}
			buttonsPane.add(acceptButton);

			buttonsPane.add(Box.createHorizontalGlue());

			if (defaultButton == null) {
				defaultButton = new JButton("Default");
				defaultButton.setEnabled(true);
				defaultButton.addMouseListener(new MouseListener(){
					public void mouseClicked(MouseEvent event){}
					public void mouseEntered(MouseEvent event){}
					public void mouseExited(MouseEvent event){}
					public void mousePressed(MouseEvent event){}
					public void mouseReleased(MouseEvent event){
						setDefaults();
					}
				});
			}
			buttonsPane.add(defaultButton);

			buttonsPane.add(Box.createHorizontalGlue());

			if (cancelButton == null) {
				cancelButton = new JButton("Cancel");
				cancelButton.addMouseListener(new MouseListener(){
					public void mouseClicked(MouseEvent event){}
					public void mouseEntered(MouseEvent event){}
					public void mouseExited(MouseEvent event){}
					public void mousePressed(MouseEvent event){}
					public void mouseReleased(MouseEvent event){
						setDefaults();
						setVisible(false);
						dispose();
					}
				});
			}
			buttonsPane.add(cancelButton);

			buttonsPane.add(Box.createHorizontalGlue());						
		}	

		return buttonsPane;
	}

	/**
	 * Creates a dialog box.
	 * 
	 * @return a JPanel with the dialog box
	 */
	private JPanel createDialogBox() {

		// LOOK AND FEEL
		// Panel
		JPanel lookAndFeelPanel = new JPanel();
		lookAndFeelPanel.setBorder(BorderFactory.createTitledBorder("Look and Feel"));
		lookAndFeelPanel.setLayout(new BoxLayout(lookAndFeelPanel, BoxLayout.Y_AXIS));
		final ButtonGroup lookAndFeelGroup = new ButtonGroup();
		
		// Availabe look and feels
		lookAndFeelList  = UIManager.getInstalledLookAndFeels();
		
		// Radio buttons
		lookAndFeelButtons = new JRadioButton[lookAndFeelList.length];
		for (int i = 0; i < lookAndFeelList.length; i++) {
			lookAndFeelButtons[i] = new JRadioButton(lookAndFeelList[i].getName());
			lookAndFeelPanel.add(lookAndFeelButtons[i]);
			lookAndFeelGroup.add(lookAndFeelButtons[i]);
		}

		// THEME
		JPanel themePanel = new JPanel();
		themePanel.setBorder(BorderFactory.createTitledBorder("Theme"));
		themePanel.setLayout(new BoxLayout(themePanel, BoxLayout.Y_AXIS));
		final ButtonGroup themeGroup = new ButtonGroup();
		themeButtons = new JRadioButton[themes.themeList.length];
		for (int i = 0; i < themes.themeList.length; i++) {
			themeButtons[i] = new JRadioButton(themes.themeList[i]);
			themePanel.add(themeButtons[i]);
			themeGroup.add(themeButtons[i]);
		}

		// Lay the look and Feel and the Theme panels
		JPanel dialogBoxForm = new JPanel();
		dialogBoxForm.setLayout(new GridLayout(0,2));
		dialogBoxForm.add(lookAndFeelPanel);
		dialogBoxForm.add(themePanel);


		// Register a listener
		for (int i = 0; i < lookAndFeelList.length; i++) {
			lookAndFeelButtons[i].addItemListener(frameListener);
		}
		for (int i = 0; i < themes.themeList.length; i++) {
			themeButtons[i].addItemListener(frameListener);
		}
		
		// Sets the default look and feel and the theme
		for (int i = 0; i < lookAndFeelList.length; i++) {
			if (lookAndFeelList[i].getName().equals(lookAndFeel)) {
				lookAndFeelButtons[i].setSelected(true);	
				break;
			}
		}
		boolean enableTheme = lookAndFeel.equals("Metal") ? true : false;
		for (int b = 0; b < themeButtons.length; b++) {
			themeButtons[b].setEnabled(enableTheme);
			if ( themes.themeList[b].equals(theme.getName()) ) {
				themeButtons[b].setSelected(true);
			}
		}
		
		return dialogBoxForm;
	}

	/**
	 * Updates the look and feel and the theme.
	 */
	private void updateLookFeelTheme() {
		setLookAndFeel(parent, lookAndFeelClassName, theme);
		setLookAndFeel(this, lookAndFeelClassName, theme);
	}

	/**
	 * 
	 *
	 */
	private void setDefaults() {
		lookAndFeel = DEFAULT_LOOKANDFEEL;
		lookAndFeelClassName = DEFAULT_LOOKANDFEEL_CLASSNAME;
		theme = themes.getTheme(DEFAULT_THEME);
		updateLookFeelTheme();
		
		for (int i = 0; i < lookAndFeelList.length; i++) {
			if (lookAndFeelList[i].getName().equals(lookAndFeel)) {
				lookAndFeelButtons[i].setSelected(true);	
				break;
			}
		}
		boolean enableTheme = lookAndFeel.equals("Metal") ? true : false;
		for (int b = 0; b < themeButtons.length; b++) {
			themeButtons[b].setEnabled(enableTheme);
			if ( themes.themeList[b].equals(theme.getName()) ) {
				themeButtons[b].setSelected(true);
			}
		}
	}
	
  // ============================ Auxiliary class ==============================
	/**
	 * Implements the listener of this class.
	 */
	class FrameListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {

			Object source = e.getItemSelectable();
			
			// L&F buttons
			for (int i = 0; i < lookAndFeelButtons.length; i++) {
				if (source == lookAndFeelButtons[i]) {					
					lookAndFeel = lookAndFeelList[i].getName();
					lookAndFeelClassName = lookAndFeelList[i].getClassName();
					boolean enableTheme = lookAndFeel.equals("Metal") ? true : false;
					for (int b = 0; b < themeButtons.length; b++) {
						themeButtons[b].setEnabled(enableTheme);
					}

				}				
			}
			
			// Theme buttons
			for (int i = 0; i < themeButtons.length; i++) {
				if (source == themeButtons[i]) {
					theme = themes.getTheme(themes.themeList[i]);
				}
			}
			
			 updateLookFeelTheme();
		}
	} 

  // ============================ Auxiliary class ==============================
	/**
	 * Defines all available themes which can allow the user to customize
	 * the Java Look and Feel. This themes are only available for the Java
	 * cross-platform L&F (Metal).
	 */
	private class Themes {

		/**
		 * Available theme names.
		 */
		private final String DEFAULT_NAME = "Default";
		private final String OCEAN_NAME = "Ocean";
		private final String AQUA_NAME = "Aqua";
		private final String LARGE_FONT_NAME = "Large font";
		private final String HIGH_CONTRAST = "High contrast";

		private final String[] themeList = {DEFAULT_NAME, OCEAN_NAME, AQUA_NAME, LARGE_FONT_NAME, HIGH_CONTRAST};
		
		
		/**
		 * Gets the list of available themes.
		 * 
		 * @return a list of available themes.
		 */
		public String[] getListOfThemes() {
			return themeList;
		}
		
		/** 
		 * Returns a theme object.
		 *
		 * @param themeName the theme name.
		 * @return a theme object.
		 * @throws IllegalArgumentException if <code>thameName</code> is not
		 * 			available or it is null.
		 */
		public MetalTheme getTheme(String themeName) {
			
			MetalTheme theme = null;
			
			if (themeName == null) throw new IllegalArgumentException(); 
							
			if ( themeName.equals("Default") ) {
				theme = new Default();
				
			} else if ( themeName.equals("Ocean") ) {
				theme = new Ocean();
				
			} else if ( themeName.equals("Aqua") ) {
				theme = new Aqua();
				
			} else if ( themeName.equals("Large font") ) {
				theme = new LargeFont();
				
			} else if ( themeName.equals("High contrast") ) {
				theme = new HighContrast();
				
			} else {
				throw new IllegalArgumentException("Invalid theme name: " + themeName);				
			}
			
			return theme;
		} 

		/**
		 * This class defines the <code>Default</code> theme.
		 */
		private class Default extends DefaultMetalTheme {
			
			public String getName() { return DEFAULT_NAME; }
			
			public final String toString() { return getName(); }		
		}  

		/**
		 * This class defines the <code>Ocean</code> theme.
		 */
		private class Ocean extends OceanTheme {
			
			public String getName(){ return OCEAN_NAME; }
			
			public final String toString() { return getName(); }
		}
		
		/**
		 * This class defines the <code>Aqua</code> theme.
		 */
		private class Aqua extends Default {

			private final ColorUIResource primary1 = new ColorUIResource(102, 153, 153);
			private final ColorUIResource primary2 = new ColorUIResource(128, 192, 192);
			private final ColorUIResource primary3 = new ColorUIResource(159, 235, 235);
			
			public String getName(){ return AQUA_NAME; }
			
			protected ColorUIResource getPrimary1() { return primary1; }
			protected ColorUIResource getPrimary2() { return primary2; }
			protected ColorUIResource getPrimary3() { return primary3; }			
		}  

		/**
		 * This class defines the <code>LargeFont</code> theme.
		 * <p>
		 * Fonts are a bit larger.
		 */
		private class LargeFont extends Default {
			
			private final FontUIResource controlFont = new FontUIResource("Dialog", Font.BOLD, 18);
			private final FontUIResource systemFont = new FontUIResource("Dialog", Font.PLAIN, 18);
			private final FontUIResource windowTitleFont = new FontUIResource("Dialog", Font.BOLD, 18);
			private final FontUIResource userFont = new FontUIResource("SansSerif", Font.PLAIN, 18);
			private final FontUIResource smallFont = new FontUIResource("Dialog", Font.PLAIN, 14);

			public String getName(){ return LARGE_FONT_NAME; }
			
			public FontUIResource getControlTextFont() { return controlFont;}
			public FontUIResource getSystemTextFont() { return systemFont;}
			public FontUIResource getUserTextFont() { return userFont;}
			public FontUIResource getMenuTextFont() { return controlFont;}
			public FontUIResource getWindowTitleFont() { return windowTitleFont;}
			public FontUIResource getSubTextFont() { return smallFont;}
		}  

		/**
		 * This class defines the <code>HighContrast</code> theme.
		 * <p>
		 * This class is bases on the LargerFont and using black and white colours.
		 */
		private class HighContrast extends LargeFont {

			private final ColorUIResource primary1 = new ColorUIResource(0, 0, 0);
			private final ColorUIResource primary2 = new ColorUIResource(204, 204, 204);
			private final ColorUIResource primary3 = new ColorUIResource(255, 255, 255);
			private final ColorUIResource primaryHighlight = new ColorUIResource(102,102,102);
			private final ColorUIResource secondary2 = new ColorUIResource(204, 204, 204);
			private final ColorUIResource secondary3 = new ColorUIResource(255, 255, 255);
			private final ColorUIResource pontrolHighlight = new ColorUIResource(102,102,102);
			
			private final Border fBlackLineBorder = new BorderUIResource(new LineBorder(getBlack()));
			private final Object fTextBorder = new BorderUIResource( new CompoundBorder( 
							fBlackLineBorder, new BasicBorders.MarginBorder()));

			private final int fInternalFrameIconSize = 30;

			private final Integer fScrollBarWidth = new Integer(25);

			
			public String getName() { return HIGH_CONTRAST; }
			
			protected ColorUIResource getPrimary1() { return primary1; } 
			protected ColorUIResource getPrimary2() { return primary2; }
			protected ColorUIResource getPrimary3() { return primary3; }
			protected ColorUIResource getSecondary2() { return secondary2; }
			protected ColorUIResource getSecondary3() { return secondary3; }
	
			public ColorUIResource getPrimaryControlHighlight() { return primaryHighlight;}
			public ColorUIResource getControlHighlight() { return super.getSecondary3(); }
			public ColorUIResource getFocusColor() { return getBlack(); }
			public ColorUIResource getTextHighlightColor() { return getBlack(); }
			public ColorUIResource getHighlightedTextColor() { return getWhite(); }
			public ColorUIResource getMenuSelectedBackground() { return getBlack(); }
			public ColorUIResource getMenuSelectedForeground() { return getWhite(); }
			public ColorUIResource getAcceleratorForeground() { return getBlack(); }
			public ColorUIResource getAcceleratorSelectedForeground() { return getWhite(); }

			public void addCustomEntriesToTable(UIDefaults defaults) {
				super.addCustomEntriesToTable(defaults);

				defaults.put( "ToolTip.border", fBlackLineBorder);
				defaults.put( "TitledBorder.border", fBlackLineBorder);
				defaults.put( "ScrollPane.border", fBlackLineBorder);

				defaults.put( "TextField.border", fTextBorder);
				defaults.put( "PasswordField.border", fTextBorder);
				defaults.put( "TextArea.border", fTextBorder);
				defaults.put( "TextPane.border", fTextBorder);
				defaults.put( "EditorPane.border", fTextBorder);

				defaults.put("InternalFrame.closeIcon", MetalIconFactory.getInternalFrameCloseIcon(fInternalFrameIconSize));
				defaults.put("InternalFrame.maximizeIcon", MetalIconFactory.getInternalFrameMaximizeIcon(fInternalFrameIconSize));
				defaults.put("InternalFrame.iconifyIcon", MetalIconFactory.getInternalFrameMinimizeIcon(fInternalFrameIconSize));
				defaults.put("InternalFrame.minimizeIcon", MetalIconFactory.getInternalFrameAltMaximizeIcon(fInternalFrameIconSize));

				defaults.put( "ScrollBar.width", fScrollBarWidth );
			}	
		}
	}

}