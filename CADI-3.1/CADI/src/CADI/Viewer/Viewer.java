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
package CADI.Viewer;

import CADI.Client.Client;
import CADI.Client.ImageData;
import CADI.Common.Cache.DataBinsCacheManagement;
import CADI.Common.Core.Prefetching;
import CADI.Common.Info.CADIInfo;
import CADI.Common.Network.JPIP.ViewWindowField;
import CADI.Viewer.Display.DisplayPane;
import CADI.Viewer.Display.ThumbnailDisplay;
import CADI.Viewer.Util.DisplayInfoFrame;
import CADI.Viewer.Util.DropDownButton;
import CADI.Viewer.Util.LogPreferencesDialog;
import CADI.Viewer.Util.LookAndFeelFrame;
import CADI.Viewer.Util.MemoryMonitorBar;
import CADI.Viewer.Util.NewSessionDialog;
import CADI.Viewer.Util.OpenImageDialog;
import CADI.Viewer.Util.SaveFile;
import CADI.Viewer.Util.SliderAndSpinnerPanel;
import CADI.Viewer.Util.SpeedAndTargetMonitor;
import CADI.Viewer.Util.StatusText;
import CADI.Viewer.Util.ThresholdsAndColorMaps;
import GiciException.ErrorException;
import GiciException.WarningException;
import GiciFile.LoadFile;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * This class defines the main frame of the CADI Viewer application.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.3 2009/12/22
 */
public class Viewer extends JFrame {

  /**
   * The application's title.
   */
  private static final String APPLICATION_TITLE = "CADI Viewer";

  /**
   * Default height of the viewer.
   */
  private static final int DEFAULT_HEIGHT = 730;

  /**
   * Default width of the viewer. Its value is a proportion of the
   * {@link #DEFAULT_HEIGHT} to have a good aspect.
   */
  private static final int DEFAULT_WIDTH = DEFAULT_HEIGHT * 4 / 3;

  /**
   * Width of the control pane. Its value is adjusted to have a good look of
   * the controls' sliders and spinners.
   */
  private static final int CONTROL_PANE_WIDTH = 180;

  /**
   * Is the maximum number of the last request that will be saved to allow
   * the backward.
   */
  private static final int MAX_NUMBER_LAST_REQUEST = 10;

  // PRINCIPAL PANELS
  /**
   * It is the root pane of the application.
   */
  private JPanel jFrameContentPane = null;

  /**
   * Menu bar for the menu options.
   */
  private JMenuBar menuBar = null;

  /**
   * Tool bar where the buttons will be placed.
   */
  private JToolBar toolBar = null;

  /**
   * This pane will contain the {@link #controlPane} on the left side and the
   * {@link #displayPane} on the right side. The width of the left size is
   * fixed (see {@link #CONTROL_PANE_WIDTH}) and it cannot be changed.
   */
  private JSplitPane splitPane = null;

  /**
   * This pane is used to display the image.
   */
  private DisplayPane displayPane = null;

  /**
   * This panel is used to display applications' status information.
   *
   * @see #statusText
   * @see #speedAndTargetMonitor
   * @see #memoryMonitorBar
   */
  private JPanel statusBar = null;

  /**
   * This panel is used to place the application controls (sliders, spinners,
   * thumbnail, ...) and it is the left side of the {@link #splitPane}.
   */
  private JPanel controlPane = null;

  // TOOL BAR
  /**
   * Backward button in the tool bar.
   */
  JButton backwardButton = null;

  /**
   * Forward button in the tool bar.
   */
  JButton forwardButton = null;

  /**
   * Is a text field that is placed in the tool bar and it is used for input
   * an URL.
   */
  private JTextField URLTextField = null;

  // CONTROL PANE
  /**
   * This object is used to select the components of the image in the
   * {@link #controlPane}.
   */
  private JList componentList = null;

  /**
   * It is the list model of the {@link #componentList}.
   */
  DefaultListModel listModel = null;

  /**
   * This object is used to control the resolution level slider and its
   * associated spinner in the {@link #controlPane}.
   */
  private SliderAndSpinnerPanel resolutionLevelPanel = null;

  /**
   * This object is used to control the layer slider and its associated
   * spinner in the {@link #controlPane}.
   */
  private SliderAndSpinnerPanel layerPanel = null;

  /**
   * This object is used to control the quality level slider and its
   * associated spinner in the {@link #controlPane}.
   */
  private SliderAndSpinnerPanel qualityPanel = null;

  /**
   * It is the accept button of the {@link #controlPane}.
   */
  private JButton controlAcceptButton = null;

  /**
   * It is the undo button of the {@link #controlPane}.
   */
  private JButton controlUndoButton = null;

  /**
   * This object contains a thumbnail of the image which is being displayed
   * in the {@link #displayPane}.
   */
  private ThumbnailDisplay thumbnailDisplay = null;

  /**
   * This object contains the thumbnail image to be displayed on the
   * {@link #thumbnailDisplay}.
   */
  private ImageData thumbnailData = null;

  // MENU BAR
  /**
   * It is the item "New Session" of the menu "File". It opens the
   * {@link #newSessionDialog} where information about a new session is
   * required.
   */
  private JMenuItem newSessionItem = null;

  /**
   * It is the item "New Session" of the menu "File". It opens the
   * {@link #newSessionDialog} where information about a new session is
   * required.
   */
  private JMenuItem openSessionItem = null;

  /**
   * It is the item "New Session" of the menu "File". It opens the
   * {@link #newSessionDialog} where information about a new session is
   * required.
   */
  private JMenuItem closeSessionItem = null;

  /**
   * It is the item "New Session" of the menu "File". It opens the
   * {@link #newSessionDialog} where information about a new session is
   * required.
   */
  private JMenuItem loadSessionItem = null;

  /**
   * It is the item "New Session" of the menu "File". It opens the
   * {@link #newSessionDialog} where information about a new session is
   * required.
   */
  private JMenuItem removeSessionItem = null;

  /**
   * It is the item "Open Image" of the menu "File" and it opens a local
   * image.
   */
  private JMenuItem openImage = null;

  /**
   * It is the item "Close image" of the menu "File"
   */
  private JMenuItem closeImage = null;

  /**
   * It is the item "Save as" of the menu "File" and it allows to save the
   * actual image.
   */
  private JMenuItem saveAs = null;

  /**
   * Is the item "Save cache" of the menu "File" and it allows to save the
   * content of the client cache.
   */
  private JMenuItem saveCache = null;

  /**
   * Is the item "Load cache" of the menu "File" and it allows to load the
   * content of a client cache from a file.
   */
  private JMenuItem loadCache = null;

  /**
   * It is the item "Exit" of the menu "File" and it exits from the
   * application.
   */
  private JMenuItem exit = null;

  /**
   * It is the item "Preferences" of the menu "Edit". It opens a
   * window where the client parameters.
   */
  private JMenuItem preferences = null;

  /**
   * It is the item "Look and Feel" of the menu "Edit". It opens a
   * window where the application's look and feel can be changed.
   */
  private JMenuItem lookAndFeel = null;

  /**
   * It is the item "Automatic" of the menu "Edit". It allows to choose
   * the automatic image recovering.
   */
  private JCheckBox automatic = null;

  /**
   * It is the item "Image parameters" of the menu "Tools". It opens a frame
   * (see {@link #showImageParameters()}) where the image data will be shown.
   */
  private JMenuItem imageParameters = null;

  /**
   * It is the item "Frame size up" of the menu "Tools". It increases the
   * frame size of the displayed image.
   */
  private JMenuItem frameSizeUpMenuItem = null;

  /**
   * It is the item "Frame size down" of the menu "Tools". It decreases the
   * frame size of the displayed image.
   */
  private JMenuItem frameSizeDownMenuItem = null;

  /**
   * It is the item "Layer up" of the menu "Tools". It increases the quality
   * layer of the displayed image.
   */
  private JMenuItem layerUpMenuItem = null;

  /**
   * It is the item "Layer up" of the menu "Tools". It decreases the quality
   * layer of the displayed image.
   */
  private JMenuItem layerDownMenuItem = null;

  // STATUS BAR
  /**
   * This object represent the status text of the status bar.
   */
  private StatusText statusText = null;

  /**
   * This object is used to monitor the speed of the transmission and amount
   * of recieved bytes in the status bar.
   */
  private SpeedAndTargetMonitor speedAndTargetMonitor = null;

  /**
   * This object is used to monitor the memory consumption as a progress bar
   * in the status bar.
   */
  private MemoryMonitorBar memoryMonitorBar = null;

  // INTERNAL ATTRIBUTES
  private String sessionID = null;

  private String thumbnailSessionID = null;

  /**
   * Contains the server name where the image are get from.
   */
  private String server = null;

  /**
   * It is the port number of the server.
   *
   * @see #server
   */
  private int port = -1;

  /**
   * Is the name of the proxy server, if it is used.
   */
  private String proxyServer = null;

  /**
   * Is the port of the proxy.
   *
   * @see #server
   */
  private int proxyPort = -1;

  /**
   * It is the resource name (an image).
   */
  private String target = null;

  /**
   * This object is a JPIP client that will be used to get images from the
   * server and to decompress them.
   */
  private Client jpipClient = null;

  /**
   * Contains the url that has been put in the {@link #URLTextField} by the
   * user, or the latest requested URL to be displayed in the
   * {@link #URLTextField}.
   */
  private String url = null;

  private ArrayList<String> urls = null;

  private int indexOfURL = 0;

  /**
   * Contains the indixes of the image component which are being displayed.
   */
  private int[] components = null;

  /**
   * Is the image resolution level that is being displayed.
   */
  private int resolutionLevel = -1;

  /**
   * Is the highest achieved layer of the displayed image.
   */
  private int layer = -1;

  /**
   * Is the quality, in percentage, of the displayed image.
   */
  private int quality = -1;

  /**
   * This object contains the image that is being displayed in the
   * {@link #displayPane}.
   */
  private ImageData imageData = null;

  private BufferedImage bufImage = null;

  private float[] fImagePixels = null;

  /**
   *
   */
  private NewSessionDialog newSessionDialog = null;

  /**
   * This object creates a frame where the user can write the image to be
   * loaded.
   */
  private OpenImageDialog openImageDialog = null;

  /**
   * Indicates whether the automatic mode for recovering the image quality
   * is or not set.
   *
   * @see #automatic
   */
  private boolean automaticMode = false;

  /**
   *
   */
  private boolean imageEqualization = false;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public Viewer() {
    super();

    // Print short copyright and version
    try {
      CADIInfo cadiInfo = new CADIInfo();
      cadiInfo.printVersion();
      cadiInfo.printCopyrightYears();
      cadiInfo.printShortCopyright();
    } catch (Exception e) {
      System.out.println("PARAMETERS ERROR: error reading properties file.");
      System.out.println("Please report this error to: gici-dev@deic.uab.es");
    }


    Properties properties = System.getProperties();

    // Check the java version
    String javaVersion = properties.getProperty("java.version");
    try {
      if (Double.parseDouble(javaVersion.substring(0, 3)) < 1.6) {
        System.out.println("ERROR: CADI Viewer needs the Java version 1.6 or higher");
        System.exit(0);
      }
    } catch (NumberFormatException e) {
      System.out.println("ERROR: Java version cannot be found.");
      System.exit(0);
    }

    // Initialize
    //imageData = new ImageData(ImageData.BUFFERED);
    imageData = new ImageData(ImageData.SAMPLES_FLOAT);
    jpipClient = new Client();
    jpipClient.setLogFile(null);
    jpipClient.setLogEnabled(false);
    //jpipClient.reuseCache(true);
    newSessionDialog = new NewSessionDialog(this);
    openImageDialog = new OpenImageDialog(this);
    urls = new ArrayList<String>();

    // Creates and show the GUI
    createAndShowGUI();

    statusText.setText("Ready");
  }

  /**
   * Get the image at component, resolution level, layer, and quality required
   * by the user.
   * <p>
   * If an error is produced while image is recovering, a error message is
   * shown.
   *
   * @param components definition in {@link #components}
   * @param fsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}
   * @param layer definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}
   * @param quality {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}
   *
   * @return
   * <code>true</code> if there is no error fetching the image.
   * Otherwise, returns
   * <code>false</code>.
   */
  public boolean getImage(int[] components, int[] fsiz, int[] roff, int[] rsiz, int layer, int quality) {

    statusText.setText("Getting image " + target + " from " + server);

    displayPane.setAction(DisplayPane.WAITING);

    // Get image from JPIP client		
    try {

      jpipClient.getTarget(components, fsiz, roff, rsiz, layer, quality, ViewWindowField.ROUND_UP);

    } catch (ErrorException e) {
      statusText.setText("Getting image " + target + " from " + server + ":  " + e.getMessage());
      displayPane.setAction(DisplayPane.NOTHING);
      thumbnailDisplay.clear();
      displayPane.clear();
      JOptionPane.showMessageDialog(this, e.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
      return false;

    } catch (IllegalAccessException e) {
      return false;
    }


    statusText.setText("Recovered image " + target + " from " + server);

    // Update list of urls
    if (urls.size() >= MAX_NUMBER_LAST_REQUEST) {
      urls.remove(0);
    }
    urls.add(jpipClient.getURI());
    indexOfURL = urls.size() - 1;

    // Update URL panel
    url = jpipClient.getURI();
    URLTextField.setText(url);

    // Update components panel
    this.components = imageData.getComponents();
    componentList.setSelectedIndices(components);

    // Update resolution level panel
    this.resolutionLevel = imageData.getResolutionLevel();
    resolutionLevelPanel.setValue(resolutionLevel);

    // Update layer panel
    this.layer = imageData.getLayers();
    layerPanel.setValue(layer);

    // Update quality panel
    this.quality = imageData.getQuality();
    if (quality >= 0) {
      qualityPanel.setValue(quality);
    }

    // Display image
    try {
      bufImage = imageSamplesToBufferedImage(imageData.getSamplesFloat());
      displayPane.displayImage(bufImage, imageData.getFrameSize(), imageData.getRegionOffset(), imageData.getRegionSize());
    } catch (IllegalAccessException e) {
      return false;
    }

    // Update backward button
    backwardButton.setEnabled(true);

    return true;
  }

  /**
   * Get the image at component, resolution level, layer, and quality required
   * by the user.
   * <p>
   * If an error is produced while image is recovering, a error message is
   * shown.
   *
   * @param components definition in {@link #components}
   * @param resolutionLevel definition in {@link #resolutionLevel}
   * @param roff definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}
   * @param rsiz definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}
   * @param layer definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}
   * @param quality {@link CADI.Common.Network.JPIP.JPIPResponseFields#quality}
   *
   * @return
   * <code>true</code> if there is no error fetching the image.
   * Otherwise, returns
   * <code>false</code>.
   */
  public boolean getImage(int[] components, int resolutionLevel, int[] roff, int[] rsiz, int layer, int quality) {

    statusText.setText("Getting image " + target + " from " + server);

    displayPane.setAction(DisplayPane.WAITING);

    // Get image from JPIP client		
    try {

      jpipClient.getTarget(components, imageData.getMaxResolutionLevels() - resolutionLevel, roff, rsiz, layer, quality);

    } catch (ErrorException e) {
      statusText.setText("Getting image " + target + " from " + server + ":  " + e.getMessage());
      displayPane.setAction(DisplayPane.NOTHING);
      thumbnailDisplay.clear();
      displayPane.clear();
      JOptionPane.showMessageDialog(this, e.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
      return false;

    } catch (IllegalAccessException e) {
      return false;
    }


    statusText.setText("Recovered image " + target + " from " + server);

    // Update list of urls
    if (urls.size() >= MAX_NUMBER_LAST_REQUEST) {
      urls.remove(0);
    }
    urls.add(jpipClient.getURI());
    indexOfURL = urls.size() - 1;

    // Update URL panel
    url = jpipClient.getURI();
    URLTextField.setText(url);

    // Update components panel
    components = imageData.getComponents();
    componentList.setSelectedIndices(components);

    // Update resolution level panel
    resolutionLevel = imageData.getResolutionLevel();
    resolutionLevelPanel.setValue(resolutionLevel);

    // Update layer panel
    layer = imageData.getLayers();
    layerPanel.setValue(layer);

    // Update quality panel
    quality = imageData.getQuality();
    if (quality >= 0) {
      qualityPanel.setValue(quality);
    }

    // Display image
    try {
      bufImage = imageSamplesToBufferedImage(imageData.getSamplesFloat());
      displayPane.displayImage(bufImage, imageData.getFrameSize(), imageData.getRegionOffset(), imageData.getRegionSize());
    } catch (IllegalAccessException e) {
      return false;
    }

    // Update backward button
    backwardButton.setEnabled(true);

    return true;
  }

  /**
   * Definition in {@link CADI.Client.Client#getSpeed()}.
   *
   * @return the speed in bytes per second.
   */
  public float getSpeed() {
    return jpipClient.getSpeed();
  }

  /**
   * Definition in {@link CADI.Client.Client#getDownloadedBytes()}.
   *
   * @return the total downloaded bytes of this target.
   */
  public long getDownloadedBytes() {
    return jpipClient.getDownloadedBytes();
  }

  // ============================ private methods ==============================
  /**
   * Creates and shows the graphical user interface.
   */
  private void createAndShowGUI() {

    // Name of frame
    setName(APPLICATION_TITLE);
    setTitle(APPLICATION_TITLE);

    // Look and Feel
    LookAndFeelFrame laf = new LookAndFeelFrame(this);
    LookAndFeelFrame.setLookAndFeel(this, laf.getLookAndFeel(), laf.getTheme());
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Create menu bar
    int menuBarWidth = DEFAULT_WIDTH - getInsets().left - getInsets().right;
    int menuBarHeight = 20;
    setJMenuBar(createMenuBar(menuBarWidth, menuBarHeight));

    // Create panels
    int contentPaneWidth = DEFAULT_WIDTH - getInsets().left - getInsets().right;
    int contentPaneHeight = DEFAULT_HEIGHT - menuBarHeight - getInsets().top - getInsets().bottom;
    setContentPane(createJFrameContentPane(contentPaneWidth, contentPaneHeight));

    // Size and position
    setLocationByPlatform(true);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    setSize(frameSize);
    setPreferredSize(frameSize);
    setMinimumSize(new Dimension(frameSize.width / 2, frameSize.height / 2));
    setMaximumSize(screenSize);
    setResizable(true);

    // View the frame
    setVisible(true);
  }

  /**
   * This method creates the menu bar
   *
   * @return JMenuBar the menu bar.
   */
  private JMenuBar createMenuBar(int width, int height) {

    if (menuBar == null) {

      menuBar = new JMenuBar();

      // FILE MENU
      JMenu fileMenu = new JMenu("File");
      fileMenu.setMnemonic(KeyEvent.VK_F);
      menuBar.add(fileMenu);

      // Create new session
      newSessionItem = new JMenuItem("New Session", KeyEvent.VK_N);
      newSessionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
      newSessionItem.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          createNewSession();
        }
      });
      fileMenu.add(newSessionItem);

      // Close session
      closeSessionItem = new JMenuItem("Close Session", KeyEvent.VK_C);
      closeSessionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
      closeSessionItem.setToolTipText("Closes the current session");
      closeSessionItem.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          closeSession();
        }
      });
      fileMenu.add(closeSessionItem);
      closeSessionItem.setEnabled(false);

      // Open new session
      openSessionItem = new JMenuItem("Open Session", KeyEvent.VK_O);
      openSessionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
      openSessionItem.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
        }
      });
      fileMenu.add(openSessionItem);
      openSessionItem.setEnabled(false);

      // Load new session
      loadSessionItem = new JMenuItem("Close Session", KeyEvent.VK_L);
      loadSessionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
      loadSessionItem.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
        }
      });
      fileMenu.add(loadSessionItem);
      loadSessionItem.setEnabled(false);

      // Remove new session
      removeSessionItem = new JMenuItem("Remove Session", KeyEvent.VK_R);
      removeSessionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
      removeSessionItem.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          jpipClient.removeSession(sessionID);
        }
      });
      fileMenu.add(removeSessionItem);

      // Open image
      openImage = new JMenuItem("Open Image", KeyEvent.VK_I);
      openImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
      openImage.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          openImage();
        }
      });
      fileMenu.add(openImage);

      // Close image
      closeImage = new JMenuItem("Close Image");
      closeImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
      closeImage.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          closeImage();
        }
      });
      closeImage.setEnabled(false);
      fileMenu.add(closeImage);

      // Close session
			/* closeSession = new JMenuItem("Close Session", KeyEvent.VK_W);
       * closeSession.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
       * closeSession.setToolTipText("Closes the current session");
       * closeSession.addActionListener(new ActionListener() {
       * public void actionPerformed(ActionEvent e) {
       * closeSession();
       * }
       * });
       * closeSession.setEnabled(false);
       * fileMenu.add(closeSession); */

      // Separator
      fileMenu.addSeparator();

      // Save as
      saveAs = new JMenuItem("Save As");
      saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
      saveAs.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          saveImage();
        }
      });
      saveAs.setEnabled(false);
      fileMenu.add(saveAs);

      // Save cache
      saveCache = new JMenuItem("Save cache");
      saveCache.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
      saveCache.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          saveCache();
        }
      });
      saveCache.setEnabled(false);
      fileMenu.add(saveCache);

      // Load cache
      loadCache = new JMenuItem("Load cache");
      loadCache.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
      loadCache.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          loadCache();
        }
      });
      loadCache.setEnabled(false);
      fileMenu.add(loadCache);

      // Separator
      fileMenu.addSeparator();

      // Exit
      exit = new JMenuItem("Exit", KeyEvent.VK_E);
      exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK));
      fileMenu.add(exit);
      exit.addMouseListener(new MouseListener() {

        public void mouseClicked(MouseEvent event) {
        }

        public void mouseEntered(MouseEvent event) {
        }

        public void mouseExited(MouseEvent event) {
        }

        public void mousePressed(MouseEvent event) {
        }

        public void mouseReleased(MouseEvent event) {
          exit();
        }
      });
      exit.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          exit();
        }
      });

      // EDIT MENU
      JMenu edit = new JMenu("Edit");
      edit.setMnemonic(KeyEvent.VK_E);
      menuBar.add(edit);

      // Properties
      preferences = new JMenuItem("Preferences", KeyEvent.VK_P);
      preferences.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
      preferences.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          changePreferences();
        }
      });
      edit.add(preferences);

      // Look & Feel
      lookAndFeel = new JMenuItem("Look & Feel", KeyEvent.VK_L);
      lookAndFeel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
      lookAndFeel.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          changeLookAndFeel();
        }
      });
      edit.add(lookAndFeel);

      edit.addSeparator();

      // Automatic
      automatic = new JCheckBox("Atomatic");
      automatic.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          automaticMode = automatic.isSelected();
        }
      });
      edit.add(automatic);
      automatic.setEnabled(false);


      // TOOLS MENU
      JMenu tools = new JMenu("Tools");
      tools.setMnemonic(KeyEvent.VK_T);
      menuBar.add(tools);

      // Image parameters
      imageParameters = new JMenuItem("Image Parameters", KeyEvent.VK_I);
      imageParameters.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
      tools.add(imageParameters);
      imageParameters.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          showImageParameters();
        }
      });

      tools.addSeparator();

      // Frame size up
      frameSizeUpMenuItem = new JMenuItem("Frame size up");
      //frameSizeUpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
      tools.add(frameSizeUpMenuItem);
      frameSizeUpMenuItem.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          displayPane.increaseResolutionLevel();
        }
      });

      // Frame size down
      frameSizeDownMenuItem = new JMenuItem("Frame size down");
      //frameSizeDownMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
      tools.add(frameSizeDownMenuItem);
      frameSizeDownMenuItem.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          displayPane.decreaseResolutionLevel();
        }
      });

      tools.addSeparator();

      // Layer up
      layerUpMenuItem = new JMenuItem("Layer up");
      //layerUpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
      tools.add(layerUpMenuItem);
      layerUpMenuItem.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          displayPane.increaseQualityLayer();
        }
      });

      // Layer down
      layerDownMenuItem = new JMenuItem("Layer down");
      //layerDownMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
      tools.add(layerDownMenuItem);
      layerDownMenuItem.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          displayPane.decreaseQualityLayer();
        }
      });

      // HELP MENU
      JMenu helpMenu = new JMenu("Help");
      helpMenu.setMnemonic(KeyEvent.VK_H);
      menuBar.add(helpMenu);

      // Help contents
      JMenuItem helpContents = new JMenuItem("Help Contents");
      helpContents.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {

          DisplayInfoFrame displayInfo = new DisplayInfoFrame(Viewer.this, 350, 250, "Help");
          String msg = "<HTML><TABLE>"
                  + "<TH>Not implemented yet</TH>"
                  + "</TABLE></html>";
          displayInfo.display(msg);
        }
      });
      helpMenu.add(helpContents);

      // About CADI Viewer
      JMenuItem aboutCadiViewer = new JMenuItem("About CADI");
      aboutCadiViewer.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {

          DisplayInfoFrame displayInfo = new DisplayInfoFrame(Viewer.this, 350, 250, "About CADI");
          String msg = "<HTML><TABLE>"
                  + "<TH>CADI software: a JPIP Client/Server framework</TH>"
                  + "<TR></TR>"
                  + "<TR><TD>Autonomous University of Barcelona, 2007-2012</TD></TR>"
                  + "<TR><a href=\"http://gici.uab.es/CADI\">http://gici.uab.es/CADI</TD></TR>"
                  + "<TR><a href=\"http://sourceforge.net/projects/CADI\">http://sourceforge.net/projects/CADI</TD></TR>"
                  + "</TABLE></html>";
          displayInfo.display(msg);
        }
      });
      helpMenu.add(aboutCadiViewer);

      // About GICI
      JMenuItem aboutGICI = new JMenuItem("About GICI");
      aboutGICI.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          DisplayInfoFrame displayInfo = new DisplayInfoFrame(Viewer.this, 350, 250, "About GICI");
          String msg = "<HTML><TABLE>"
                  + "<TH colspan=\"2\">GROUP ON INTERACTIVE CODING OF IMAGES</TH>"
                  + "<TR></TR>"
                  + "<TR><TD>Web page </TD> <TD><a href=\"http://www.gici.uab.es\">http://www.gici.uab.es</TD></TR>"
                  + "<TR><TD>e-mail </TD> <TD><a href=\"mailto:gici-info@deic.uab.es\">gici-info@deic.uab.es</TD></TR>"
                  + "</TABLE></html>";
          displayInfo.display(msg);
        }
      });
      helpMenu.add(aboutGICI);

    }

    Dimension dims = new Dimension(width, height);
    menuBar.setSize(dims);
    menuBar.setPreferredSize(dims);
    menuBar.setMinimumSize(dims);
    menuBar.setMaximumSize(dims);

    return menuBar;
  }

  /**
   * Creates the principal content pane.
   *
   * @return JPanel the principal content pane.
   */
  private JPanel createJFrameContentPane(int width, int height) {
    if (jFrameContentPane == null) {
      jFrameContentPane = new JPanel();
      jFrameContentPane.setLayout(new BorderLayout());
      jFrameContentPane.add(createToolBar(width, 40), BorderLayout.NORTH);
      jFrameContentPane.add(createSplitPane(), BorderLayout.CENTER);
      jFrameContentPane.add(createStatusAndProgressBar(), BorderLayout.SOUTH);
      jFrameContentPane.setName("JFrameContentPane");
    }
    return jFrameContentPane;
  }

  /**
   * Creates the tool bar.
   *
   * @return JToolBar the tool bar.
   */
  private JToolBar createToolBar(int width, int height) {

    if (toolBar == null) {

      // Tool bar container
      toolBar = new JToolBar();
      toolBar.setFloatable(false);
      Dimension toolBarDims = new Dimension(width, height);
      toolBar.setSize(toolBarDims);
      toolBar.setPreferredSize(toolBarDims);
      toolBar.setMinimumSize(toolBarDims);
      toolBar.setMaximumSize(toolBarDims);
      toolBar.setMargin(new Insets(3, 3, 3, 3));

      // Create a null button to be used as separator						
      JButton buttonSeparator = new JButton();
      Dimension bsDims = new Dimension(2, toolBarDims.height - toolBar.getInsets().top - toolBar.getInsets().bottom);
      buttonSeparator.setSize(bsDims);
      buttonSeparator.setPreferredSize(bsDims);
      buttonSeparator.setMinimumSize(bsDims);
      buttonSeparator.setMaximumSize(bsDims);


      // ADD BUTTONS
      JButton button = null;
      int d = toolBarDims.height - toolBar.getInsets().top - toolBar.getInsets().bottom;
      int hSeparation = 3;


      // Backward
      URL urlImage = getClass().getResource("Images/backward.png");
      ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage));
      backwardButton = createButton(icon, "Backward", d, d);
      backwardButton.setEnabled(false);
      toolBar.add(backwardButton);
      backwardButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          if (indexOfURL > 0) {
            indexOfURL--;
            getImage(urls.get(indexOfURL));
            URLTextField.setText(jpipClient.getURI());
            if (indexOfURL == 0) {
              backwardButton.setEnabled(false);
            }
            forwardButton.setEnabled(true);
          }
        }
      });

      toolBar.add(Box.createHorizontalStrut(4 * hSeparation));

      // Forward
      urlImage = getClass().getResource("Images/forward.png");
      icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage));
      forwardButton = createButton(icon, "Forward", d, d);
      forwardButton.setEnabled(false);
      toolBar.add(forwardButton);
      forwardButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          if (indexOfURL < urls.size() - 1) {
            indexOfURL++;
            getImage(urls.get(indexOfURL));
            URLTextField.setText(jpipClient.getURI());
            if (indexOfURL == (urls.size() - 1)) {
              forwardButton.setEnabled(false);
            }
            backwardButton.setEnabled(true);
          }
        }
      });

      // Separator
      toolBar.add(Box.createHorizontalStrut(4 * hSeparation));
      toolBar.add(createButton(null, null, 2, d));
      toolBar.add(Box.createHorizontalStrut(4 * hSeparation));


      // Resolution level buttons
      ImageIcon[] icons = new ImageIcon[2];
      String[] toolTipTexts = new String[2];
      urlImage = getClass().getResource("Images/resolutionLevelUp.png");
      icons[0] = new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage));
      toolTipTexts[0] = "Resolution Level Up";

      urlImage = getClass().getResource("Images/resolutionLevelDown.png");
      icons[1] = new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage));
      toolTipTexts[1] = "Resolution Level Down";

      final DropDownButton resolutionLevelButton = new DropDownButton(icons, toolTipTexts, d, d);
      resolutionLevelButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          int index = resolutionLevelButton.getIndexOfSelectedButton();
          switch (index) {
            case 0:
              displayPane.increaseResolutionLevel();
              break;
            case 1:
              displayPane.decreaseResolutionLevel();
              break;
          }
        }
      });

      toolBar.add(resolutionLevelButton);


      toolBar.add(Box.createHorizontalStrut(4 * hSeparation));

      // Quality layer buttons
      icons = new ImageIcon[2];
      toolTipTexts = new String[2];
      urlImage = getClass().getResource("Images/layerUp.png");
      icons[0] = new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage));
      toolTipTexts[0] = "Layer Up";

      urlImage = getClass().getResource("Images/layerDown.png");
      icons[1] = new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage));
      toolTipTexts[1] = "Layer Down";

      final DropDownButton layerButton = new DropDownButton(icons, toolTipTexts, d, d);
      layerButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          int index = layerButton.getIndexOfSelectedButton();
          switch (index) {
            case 0:
              displayPane.increaseQualityLayer();
              break;
            case 1:
              displayPane.decreaseQualityLayer();
              break;
          }
        }
      });

      toolBar.add(layerButton);

      // Separator
      toolBar.add(Box.createHorizontalStrut(4 * hSeparation));
      toolBar.add(createButton(null, null, 2, d));
      toolBar.add(Box.createHorizontalStrut(4 * hSeparation));


      // Cursors (default, pan, select)
      icons = new ImageIcon[3];
      toolTipTexts = new String[3];
      urlImage = getClass().getResource("Images/defaultCursor.png");
      icons[0] = new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage));
      toolTipTexts[0] = "Default";

      urlImage = getClass().getResource("Images/pan.png");
      icons[1] = new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage));
      toolTipTexts[1] = "Panning";

      urlImage = getClass().getResource("Images/select.png");
      icons[2] = new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage));
      toolTipTexts[2] = "Select region";

      final DropDownButton cursorButton = new DropDownButton(icons, toolTipTexts, d, d);
      cursorButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          int index = cursorButton.getIndexOfSelectedButton();
          switch (index) {
            case 0:
              statusText.setText("Default");
              displayPane.setAction(DisplayPane.NOTHING);
              break;
            case 1:
              statusText.setText("Panning");
              displayPane.setAction(DisplayPane.PANNING);
              break;
            case 2:
              statusText.setText("Select region");
              displayPane.setAction(DisplayPane.SELECTING);
          }
        }
      });

      toolBar.add(cursorButton);


      // Set thresholds and colormaps			
      toolBar.add(Box.createHorizontalStrut(4 * hSeparation));

      // Equalization
      //urlImage = getClass().getResource("Images/zoom_out.png"); 
      //icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage));

      // Set icon and tool tip
      GradientPaint whiteToBlack = new GradientPaint(0, 0, Color.black, d, d, Color.white);
      BufferedImage imageButton = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = (Graphics2D) imageButton.getGraphics();
      g2.setPaint(whiteToBlack);
      g2.fill(new Rectangle2D.Double(0, 0, d, d));
      g2.setPaint(Color.black);
      icon = new ImageIcon(imageButton);

      button = createButton(icon, "Equalization", d, d);
      toolBar.add(button);
      button.setEnabled(true);
      button.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          setThresholdsAndColorMaps();
        }
      });


      toolBar.add(Box.createHorizontalStrut(4 * hSeparation));
      toolBar.add(createButton(null, null, 2, d));
      toolBar.add(Box.createHorizontalStrut(4 * hSeparation));



      // Zoom in
			/* urlImage = getClass().getResource("Images/zoom_in.png");
       * icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage));
       * final JButton buttonZoomIn = createButton(icon, "Zoom in", d, d);
       * buttonZoomIn.setEnabled(true);
       * toolBar.add(buttonZoomIn);
       * buttonZoomIn.addActionListener(new ActionListener() {
       * public void actionPerformed(ActionEvent e) {
       * if (buttonZoomIn.isSelected()) {
       * imageEqualization = true;
       * } else {
       * imageEqualization = false;
       * }
       * System.out.println("Image equalization: " +imageEqualization);
       * equalizeImage();
       * }
       * }); */

      toolBar.add(Box.createHorizontalStrut(4 * hSeparation));

      // Zoom out
      urlImage = getClass().getResource("Images/zoom_out.png");
      icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage));
      button = createButton(icon, "Zoom out", d, d);
      toolBar.add(button);
      button.setEnabled(false);
      button.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          //displayPane.zoomOut();
        }
      });

      toolBar.add(Box.createHorizontalStrut(4 * hSeparation));
      toolBar.add(createButton(null, null, 2, d)); //toolBar.add(new JSeparator(JSeparator.VERTICAL));
      toolBar.add(Box.createHorizontalStrut(4 * hSeparation));

      // URL text
      if (URLTextField == null) {
        URLTextField = new JTextField();
        URLTextField.setText("jpip://");
        URLTextField.setMargin(new Insets(2, 2, 2, 2));
        URLTextField.addActionListener(new ActionListener() {

          public void actionPerformed(ActionEvent e) {
            getImage(URLTextField.getText());
          }
        });
      }
      toolBar.add(URLTextField);
      URLTextField.setEnabled(true);

      toolBar.add(Box.createHorizontalStrut(hSeparation));

      //	Go button
      button = createButton(null, "GO", d, d);
      button.setText("GO");
      toolBar.add(button);
      button.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          getImage(URLTextField.getText());
        }
      });

      toolBar.add(Box.createHorizontalStrut(4 * hSeparation));
      toolBar.add(createButton(null, null, 2, d)); //toolBar.add(new JSeparator(JSeparator.VERTICAL));			 
      toolBar.add(Box.createHorizontalStrut(4 * hSeparation));

      // Exit button
      urlImage = getClass().getResource("Images/exit.png");
      icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage));
      button = createButton(icon, "Exit", d, d);
      toolBar.add(button);
      button.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          exit();
        }
      });

    }

    return toolBar;
  }

  /**
   * Creates a button with a fixes dimensions.
   *
   * @param icon the button's icon
   * @param toolTipText the text to display in the tool tip.
   * @param width width of the button
   * @param height height of the button
   *
   * @return a JButton
   */
  private JButton createButton(Icon icon, String toolTipText, int width, int height) {

    // Create button and set dimensions
    JButton button = new JButton();
    Dimension buttonDims = new Dimension(width, height);
    button.setSize(buttonDims);
    button.setPreferredSize(buttonDims);
    button.setMinimumSize(buttonDims);
    button.setMaximumSize(buttonDims);
    button.setMargin(new Insets(0, 0, 0, 0));

    // Set icon and tool tip
    if (icon != null) {
      button.setIcon(icon);
    }
    if (toolTipText != null) {
      button.setToolTipText(toolTipText);
    }

    button.setFocusPainted(true);
    button.setRolloverEnabled(false);
    button.setRequestFocusEnabled(true);
    button.setBorderPainted(true);
    button.setFocusable(true);

    button.setVerticalTextPosition(AbstractButton.CENTER);
    button.setHorizontalTextPosition(AbstractButton.CENTER);

    return button;
  }

  /**
   * Creates the split pane.
   *
   * @return JSplitPane the split pane.
   */
  private JSplitPane createSplitPane() {
    if (splitPane == null) {
      splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
      splitPane.setLeftComponent(createControlPane());
      splitPane.setRightComponent(createDisplayPane());
      splitPane.setDividerSize(6);
      splitPane.setResizeWeight(0);
      splitPane.setDividerLocation(CONTROL_PANE_WIDTH + splitPane.getInsets().left);
      splitPane.setContinuousLayout(true);
      splitPane.resetToPreferredSizes();
      splitPane.setOneTouchExpandable(true);
      splitPane.setEnabled(true);
    }
    return splitPane;
  }

  /**
   * Creates the control pane, where the sliders and spinners will be
   * located.
   *
   * @return the control pane.
   */
  private JPanel createControlPane() {
    if (controlPane == null) {
      controlPane = new JPanel();
      controlPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(9), "Controls"));

      Box vBox = Box.createVerticalBox();

      int panelWidth = CONTROL_PANE_WIDTH
              - splitPane.getInsets().left - splitPane.getInsets().right
              - controlPane.getInsets().left - controlPane.getInsets().right;

      // Component
      JPanel componentPanel = createComponentPane(panelWidth);

      // Resolution level
      resolutionLevelPanel = new SliderAndSpinnerPanel("Frame size", 0, 0, 0, 1, panelWidth);
      resolutionLevelPanel.setEnabled(true);
      resolutionLevelPanel.setSnapToTicks(true);

      // Layer
      layerPanel = new SliderAndSpinnerPanel("Layer", 1, 1, 1, 1, panelWidth);
      layerPanel.setEnabled(true);
      layerPanel.setSnapToTicks(false);

      // Quality panel
      qualityPanel = new SliderAndSpinnerPanel("Quality", 0, 100, 0, 1, panelWidth);
      qualityPanel.setEnabled(false);
      qualityPanel.setSnapToTicks(false);

      // Control buttons
      JPanel controlButtonsPanel = createControlButtons();

      // Separator
      JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
      Dimension sepDims = new Dimension(panelWidth, separator.getPreferredSize().height);
      separator.setSize(sepDims);
      separator.setPreferredSize(sepDims);
      separator.setMinimumSize(sepDims);
      separator.setMaximumSize(sepDims);

      // Thumbnail
      thumbnailDisplay = new ThumbnailDisplay(this, (int) (0.80 * CONTROL_PANE_WIDTH), (int) (0.80 * CONTROL_PANE_WIDTH));


      // Add elements to vertical box
      vBox.add(componentPanel);
      vBox.add(Box.createVerticalStrut(20));
      vBox.add(resolutionLevelPanel);
      vBox.add(Box.createVerticalStrut(20));
      vBox.add(layerPanel);
      vBox.add(Box.createVerticalStrut(20));
      vBox.add(qualityPanel);
      vBox.add(Box.createVerticalStrut(25));
      vBox.add(controlButtonsPanel);
      vBox.add(Box.createVerticalStrut(15));
      vBox.add(separator);
      vBox.add(Box.createVerticalStrut(15));
      vBox.add(thumbnailDisplay);
      vBox.add(Box.createVerticalStrut(15));

      controlPane.add(vBox);

    }
    return controlPane;
  }

  /**
   * Creates de display pane. This is the pane where image will be dispalyed.
   *
   * @return the display pane.
   */
  private JPanel createDisplayPane() {
    if (displayPane == null) {
      int width = DEFAULT_WIDTH
              - getInsets().left - getInsets().right
              - splitPane.getInsets().left - splitPane.getInsets().right;

      int height = DEFAULT_HEIGHT
              - getInsets().top - getInsets().bottom
              - splitPane.getInsets().top - splitPane.getInsets().bottom;

      displayPane = new DisplayPane(this, width, height, imageData);
      displayPane.setThumbnailDisplayer(thumbnailDisplay);
    }
    return displayPane;
  }

  /**
   * Creates the status text and the progress bar.
   *
   * @return the status text and the progress bar.
   */
  private JPanel createStatusAndProgressBar() {

    if (statusBar == null) {

      statusBar = new JPanel();
      statusBar.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

      int width = DEFAULT_WIDTH
              - getInsets().left - getInsets().right
              - statusBar.getInsets().left - statusBar.getInsets().right;
      int height = getPreferredSize().height
              - getInsets().top - getInsets().bottom
              - statusBar.getInsets().top - statusBar.getInsets().bottom;


      // Status text
      statusText = new StatusText(width - 275 - 100 - 14 - 14, height);

      // Speed and target
      try {
        Method getSpeed = this.getClass().getMethod("getSpeed", new Class[]{});
        Method getDownload = this.getClass().getMethod("getDownloadedBytes", new Class[]{});
        speedAndTargetMonitor = new SpeedAndTargetMonitor(275, height, this, getSpeed, getDownload);
      } catch (NoSuchMethodException e) {
      }

      // Memory monitor bar
      memoryMonitorBar = new MemoryMonitorBar(100, height);

      Box hBox = Box.createHorizontalBox();

      hBox.add(statusText);
      hBox.add(Box.createHorizontalStrut(7));
      hBox.add(createButton(null, null, 1, height));
      hBox.add(Box.createHorizontalStrut(7));
      hBox.add(speedAndTargetMonitor);
      hBox.add(Box.createHorizontalStrut(7));
      hBox.add(createButton(null, null, 1, height));
      hBox.add(Box.createHorizontalStrut(7));
      hBox.add(memoryMonitorBar);

      // Panel size
      Dimension dims = new Dimension(DEFAULT_WIDTH, height);
      setSize(dims);
      setPreferredSize(dims);
      setMinimumSize(dims);
      setMaximumSize(dims);

      statusBar.add(hBox);
    }
    return statusBar;
  }

  /**
   * Creates the component pane.
   *
   * @see #createControlPane()
   *
   * @return a JPanel with the component pane.
   */
  private JPanel createComponentPane(int panelWidth) {
    JPanel componentPane = new JPanel();

    componentPane.setLayout(new BorderLayout(0, 0));
    panelWidth -= (componentPane.getInsets().left + componentPane.getInsets().right + 5);

    Box labelBox = Box.createHorizontalBox();

    // Label
    JLabel jLabel = new JLabel("Components", SwingConstants.LEFT);
    Dimension labelDims = new Dimension(panelWidth, jLabel.getPreferredSize().height);
    jLabel.setSize(labelDims);
    jLabel.setMinimumSize(labelDims);
    jLabel.setPreferredSize(labelDims);
    jLabel.setMaximumSize(labelDims);
    labelBox.add(jLabel);
    labelBox.add(Box.createHorizontalGlue());

    // Component list
    listModel = new DefaultListModel();
    componentList = new JList(listModel);
    componentList.setBackground(this.getBackground());
    componentList.setVisibleRowCount(3);
    componentList.setFont(new Font("Serif", Font.ROMAN_BASELINE, 12));
    componentList.setSelectionModel(new DefaultListSelectionModel() { // Allow multiple choice without using the CTRL button

      public void setSelectionInterval(int index0, int index1) {

        if (isSelectedIndex(index0)) {
          super.removeSelectionInterval(index0, index1);
        } else {
          super.addSelectionInterval(index0, index1);
        }

        int numSelectedItems = componentList.getSelectedIndices().length;
        if ((numSelectedItems != 1) && (numSelectedItems != 3)) {
          JOptionPane.showMessageDialog(Viewer.this, "Only 1 or 3 components can be selected", "Error Message", JOptionPane.ERROR_MESSAGE);
        }
      }
    });

    JScrollPane scrollingList = new JScrollPane(componentList);
    Dimension compsDims = new Dimension(panelWidth, 20 * 4);
    scrollingList.setSize(compsDims);
    scrollingList.setMinimumSize(compsDims);
    scrollingList.setPreferredSize(compsDims);
    scrollingList.setMaximumSize(compsDims);

    // Add components to vertical box
    Box vBox = Box.createVerticalBox();
    vBox.add(labelBox);
    vBox.add(Box.createVerticalStrut(5));
    vBox.add(scrollingList);

    int vBoxHeight = labelDims.height + compsDims.height + 5;
    Dimension vBoxDims = new Dimension(panelWidth, vBoxHeight + componentPane.getInsets().top + componentPane.getInsets().bottom);

    // Set component size
    componentPane.setSize(vBoxDims);
    componentPane.setMinimumSize(vBoxDims);
    componentPane.setPreferredSize(vBoxDims);
    componentPane.setMaximumSize(vBoxDims);


    // Add box to panel
    componentPane.add(vBox);

    return componentPane;
  }

  /**
   * Creates the control buttons panel of the control panel.
   *
   * @return the control buttons panel
   *
   * @see #createControlPane()
   */
  private JPanel createControlButtons() {
    JPanel controlButtonsPane = new JPanel();

    Dimension bDims = new Dimension(75, 25);

    // Accept button
    if (controlAcceptButton == null) {
      controlAcceptButton = new JButton("Accept");
      controlAcceptButton.setSize(bDims);
      controlAcceptButton.setPreferredSize(bDims);
      controlAcceptButton.setMinimumSize(bDims);
      controlAcceptButton.setMaximumSize(bDims);
      controlAcceptButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          components = componentList.getSelectedIndices();
          int numSelectedItems = components.length;
          if ((numSelectedItems != 1) && (numSelectedItems != 3)) {
            JOptionPane.showMessageDialog(Viewer.this, "Only 1 or 3 components can be selected", "Error Message", JOptionPane.ERROR_MESSAGE);
            return;
          }

          if (components.length == imageData.getMaxComponents()) {
            components = null;
          }

          resolutionLevel = resolutionLevelPanel.getValue();
          layer = layerPanel.getValue();
          quality = qualityPanel.getValue();
          getImage(components, resolutionLevel, imageData.getRegionOffset(), imageData.getRegionSize(), layer, quality);
          // Display image
          try {
            bufImage = imageSamplesToBufferedImage(imageData.getSamplesFloat());
            displayPane.displayImage(bufImage, imageData.getFrameSize(), imageData.getRegionOffset(), imageData.getRegionSize());
          } catch (IllegalAccessException e1) {
            return;
          }

          // Quality		
          quality = imageData.getQuality();
          if (quality >= 0) {
            qualityPanel.setValue(quality);
          }
        }
      });
    }

    // Undo button
    if (controlUndoButton == null) {
      controlUndoButton = new JButton("Undo");
      controlUndoButton.setSize(bDims);
      controlUndoButton.setPreferredSize(bDims);
      controlUndoButton.setMinimumSize(bDims);
      controlUndoButton.setMaximumSize(bDims);
      controlUndoButton.setEnabled(false);
      controlUndoButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
        }
      });
    }

    // Add buttons to horizontal box
    int panelWidth = CONTROL_PANE_WIDTH - splitPane.getInsets().left
            - controlPane.getInsets().left - controlPane.getInsets().right
            - controlButtonsPane.getInsets().left - controlButtonsPane.getInsets().right;

    Box hBox = Box.createHorizontalBox();
    hBox.add(Box.createHorizontalGlue());
    hBox.add(controlAcceptButton);
    int strutWidth = (panelWidth - 2 * bDims.width) / 3;
    hBox.add(Box.createHorizontalStrut(strutWidth));
    hBox.add(controlUndoButton);
    hBox.add(Box.createHorizontalGlue());

    // Box size
    int boxHeight = bDims.height
            + controlButtonsPane.getInsets().top + controlButtonsPane.getInsets().bottom
            + controlAcceptButton.getInsets().top + controlAcceptButton.getInsets().bottom;

    Dimension hBoxDims = new Dimension(panelWidth, boxHeight);

    // Add box to panel and set panel size
    controlButtonsPane.add(hBox);
    controlButtonsPane.setSize(hBoxDims);
    controlButtonsPane.setPreferredSize(hBoxDims);
    controlButtonsPane.setMinimumSize(hBoxDims);
    controlButtonsPane.setMaximumSize(hBoxDims);

    return controlButtonsPane;
  }

  /**
   *
   */
  private void createNewSession() {

    statusText.setText("Input target name and server");

    if (sessionID != null) {
      jpipClient.removeSession(sessionID);
    }

    newSessionDialog = new NewSessionDialog(this);
    newSessionDialog.run();

    sessionID = jpipClient.newSession(imageData,
            newSessionDialog.getServer(),
            newSessionDialog.getPort(),
            newSessionDialog.getProxyServer(),
            newSessionDialog.getProxyPort(),
            newSessionDialog.getTarget(), -1);
    /* sessionID = jpipClient.newSession(imageData,
     * newSessionDialog.getServer(),
     * newSessionDialog.getPort(),
     * newSessionDialog.getProxyServer(),
     * newSessionDialog.getProxyPort(),
     * newSessionDialog.getTarget(), Prefetching.WOI_TYPE_WEIGHTED_WOI); */

    jpipClient.setDefaultSession(sessionID);
    jpipClient.setUseHTTPSession(sessionID, newSessionDialog.useHTTPSession());
    jpipClient.setUseHTTPTCPSession(sessionID, newSessionDialog.useHTTPTCPSession());
    jpipClient.setCacheType(sessionID, newSessionDialog.getCachetype());
    
    int type =  newSessionDialog.getCachetype();
    int qualifier = -1;
    if (newSessionDialog.useWildcard()) {
      qualifier = DataBinsCacheManagement.WILDCARD;
    } else if (newSessionDialog.useIndexRange()) {
      qualifier = DataBinsCacheManagement.INDEX_RANGE;
    } else if (newSessionDialog.useNumberOfLayers()) {
      qualifier = DataBinsCacheManagement.NUMBER_OF_LAYERS;
    } else if (newSessionDialog.useNumberOfBytes()) {
      qualifier = DataBinsCacheManagement.NUMBER_OF_BYTES;
    }
    jpipClient.setCacheDescriptor(sessionID, type, qualifier);
    
    jpipClient.setMaxCacheSize(sessionID, newSessionDialog.getMaxCacheSize());
    jpipClient.setManagementPolicy(sessionID, newSessionDialog.getManagementPolicy());
    jpipClient.setUseKeepAlive(sessionID, newSessionDialog.useKeepAlive());
    jpipClient.setAllowedReturnTypes(sessionID, newSessionDialog.getImageReturnTypes(), newSessionDialog.useExtendedHeaders());
    jpipClient.reuseCache(true);

    resetSlidersAndSpinners();

    // First image view is fit to display size
    Dimension displaySize = displayPane.getSize();
    int[] fsiz = {displaySize.width, displaySize.height};
    int[] roff = {0, 0};
    int[] rsiz = {displaySize.width, displaySize.height};

    // Request 3 components. Only images with 1 or 3 components can be displayed. 
    components = null;
    /* components = new int[3];
     * for (int c = 0; c < components.length; c++) {
     * components[c] = c;
     * } */

    statusText.setText("Getting image " + target + " from " + server);

    displayPane.setAction(DisplayPane.WAITING);

    // Get image form JPIP client		
    try {

      jpipClient.getTarget(components, fsiz, roff, rsiz, layer, quality, ViewWindowField.ROUND_UP);

    } catch (ErrorException e) {
      statusText.setText("Getting image " + target + " from " + server + ":  " + e.getMessage());
      displayPane.setAction(DisplayPane.NOTHING);
      thumbnailDisplay.clear();
      displayPane.clear();
      JOptionPane.showMessageDialog(this, e.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
      return;

    } catch (IllegalAccessException e) {
      return;
    }

    statusText.setText("Recovered image " + target + " from " + server);


    // Set enable menu items
    closeImage.setEnabled(true);
    closeSessionItem.setEnabled(true);
    saveAs.setEnabled(true);
    backwardButton.setEnabled(true);
    saveCache.setEnabled(true);
    loadCache.setEnabled(true);

    // Update list of urls
    if (urls.size() >= 5) {
      urls.remove(0);
    }
    urls.add(jpipClient.getURI());
    indexOfURL = urls.size() - 1;


    // Update URL panel
    url = jpipClient.getURI();
    URLTextField.setText(url);

    // Sliders and spinners, set minimum and maximum values			
    int majorTick;

    // Components
    int maxComps = imageData.getMaxComponents();
    for (int i = 0; i < maxComps; i++) {
      listModel.add(i, "Component " + i);
    }
    components = imageData.getComponents();
    componentList.setSelectedIndices(components);

    // Resolution levels
    int maxResolutionLevel = imageData.getMaxResolutionLevels();
    resolutionLevelPanel.setMaximum(maxResolutionLevel);
    majorTick = (int) (20 * maxResolutionLevel / 100F);
    resolutionLevelPanel.setMajorTickSpacing(majorTick);
    resolutionLevelPanel.setMinorTickSpacing((int) (5 * maxResolutionLevel / 100F));
    if (majorTick >= 1) {
      resolutionLevelPanel.setLabelTable((int) (20 * maxResolutionLevel / 100F));
    }
    resolutionLevel = imageData.getResolutionLevel();
    resolutionLevelPanel.setValue(resolutionLevel);

    // Layers
    int layers = imageData.getMaxLayers();
    layerPanel.setMaximum(layers);
    majorTick = (int) (20 * layers / 100F);
    layerPanel.setMajorTickSpacing(majorTick);
    layerPanel.setMinorTickSpacing((int) (5 * layers / 100F));
    if (majorTick >= 1) {
      layerPanel.setLabelTable((int) (20 * layers / 100F));
    }
    layer = imageData.getLayers();
    layerPanel.setValue(layer);

    // Update quality panel
    quality = imageData.getQuality();
    if (quality >= 0) {
      qualityPanel.setValue(quality);
    }

    // Display image
    try {
      bufImage = imageSamplesToBufferedImage(imageData.getSamplesFloat());
      displayPane.displayImage(bufImage, imageData.getFrameSize(), imageData.getRegionOffset(), imageData.getRegionSize());
    } catch (IllegalAccessException e1) {
      return;
    }

    // Thumbnail image
    displayThumbnail();

  }

  /**
   * Opens the Open Image Dialog window and get the specifies image and
   * properties.
   */
  private void openImage() {

    statusText.setText("Input image name and features");


    // Get dialog parameters
    openImageDialog.run();

    String imageFileName = openImageDialog.getFileName();

    LoadFile loadFile = null;
    try {
      int dotPos = imageFileName.lastIndexOf(".");

      if (dotPos >= 0) {
        if ((imageFileName.substring(dotPos + 1, imageFileName.length()).compareToIgnoreCase("pgm") == 0)
                || (imageFileName.substring(dotPos + 1, imageFileName.length()).compareToIgnoreCase("ppm") == 0)) {
          loadFile = new LoadFile(imageFileName);
        } else {
          loadFile = new LoadFile(openImageDialog.getFileName(), openImageDialog.getZSize(), openImageDialog.getYSize(), openImageDialog.getXSize(),
                  openImageDialog.getSampleType(), openImageDialog.getByteOrder(), openImageDialog.isRGB());
        }
      }


    } catch (WarningException e) {
      e.printStackTrace();
    }


    // Display image
    int[] fsiz = {loadFile.getImage()[0][0].length, loadFile.getImage()[0].length};
    int[] roff = {0, 0};
    int[] rsiz = Arrays.copyOf(fsiz, fsiz.length);
    int zSize = loadFile.getImage().length;

    imageData.setData(loadFile.getImage());
    imageData.setPrecision(loadFile.getPixelBitDepth());
    imageData.setMaxComponents(zSize);
    imageData.setFrameSize(fsiz);
    imageData.setRegionOffset(roff);
    imageData.setRegionSize(rsiz);
    imageData.setMaxResolutionLevel(1);
    imageData.setMaxLayers(1);
    int[] components = new int[zSize];
    for (int z = 0; z < zSize; z++) {
      components[z] = z;
    }
    imageData.setComponents(components);


    // Components
    int maxComps = imageData.getMaxComponents();
    for (int i = 0; i < maxComps; i++) {
      listModel.add(i, "Component " + i);
    }
    int[] selectedComponents = {0, 1, 2};
    componentList.setSelectedIndices(selectedComponents);

    // Display image
    bufImage = imageSamplesToBufferedImage(loadFile.getImage());
    displayPane.displayImage(bufImage, fsiz, roff, rsiz);


  }

  /**
   * This method performs an automatic recovering of the image.
   */
  private void automaticDisplay() {

    while (quality < imageData.getMaxLayers()) {
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        System.exit(0);
      }

      getImage(components, imageData.getFrameSize(), imageData.getRegionOffset(), imageData.getRegionSize(), layer + 1, quality);
      this.displayPane.updateUI();
    }

  }

  /**
   * Close the connection with the jpip client;
   */
  private void closeImage() {
    //jpipClient.closeLogicalTarget();
  }

  /**
   * Close the jpip client session.
   */
  private void closeSession() {
    jpipClient.closeSession(sessionID);
    closeSessionItem.setEnabled(false);
  }

  /**
   * Show the image parameters for image which is being showed.
   */
  private void showImageParameters() {

    String description = jpipClient.getTargetDescription();
    if (description == null) {
      DisplayInfoFrame displayInfo = new DisplayInfoFrame(this, 300, 200, "Image parameters");
      displayInfo.display("<html><H3>None image has been deliveried yet</H3></html>");
    } else {
      DisplayInfoFrame displayInfo = new DisplayInfoFrame(this, 500, 700, "Image parameters");
      displayInfo.display(description);
    }
  }

  /**
   * Closes the CADI Viewer application.
   */
  private void exit() {
    int result;

    result = JOptionPane.showConfirmDialog(this, "Really exit?", "Exit", JOptionPane.YES_NO_OPTION);

    if (result == JOptionPane.YES_OPTION) {
      System.exit(0);
    }
  }

  /**
   * Resets the sliders and spinners with the new image values.
   */
  private void resetSlidersAndSpinners() {
    int value, majorTick;

    components = null;
    resolutionLevel = 0;
    layer = 1;
    quality = 0;

    // Components
    for (int i = listModel.size() - 1; i >= 0; i--) {
      listModel.remove(i);
    }

    // Resolution levels
    value = 0;
    resolutionLevelPanel.setRangeProperties(value, value, value, 0);
    majorTick = (int) (20 * value / 100F);
    resolutionLevelPanel.setMajorTickSpacing(majorTick);
    resolutionLevelPanel.setMinorTickSpacing((int) (5 * value / 100F));
    if (majorTick >= 1) {
      resolutionLevelPanel.setLabelTable((int) (20 * value / 100F));
    }

    // Layers
    value = 1;
    layerPanel.setRangeProperties(value, value, value, value);
    layerPanel.setValue(layer);
    layerPanel.setMaximum(value);
    majorTick = (int) (20 * value / 100F);
    layerPanel.setMajorTickSpacing(majorTick);
    layerPanel.setMinorTickSpacing((int) (5 * value / 100F));
    if (majorTick >= 1) {
      layerPanel.setLabelTable((int) (20 * value / 100F));
    }

    // Quality
    qualityPanel.setValue(quality);
  }

  /**
   * Save the image samples in a file.
   */
  private void saveImage() {
    String fileName = "";
    String fileExtension = "";
    String directoryName = "";

    JFileChooser jFileChooser = new JFileChooser();
    FileNameExtensionFilter PNMFilter = new FileNameExtensionFilter("PNM", "pnm");
    FileNameExtensionFilter PGMFilter = new FileNameExtensionFilter("PGM", "pgm");
    FileNameExtensionFilter PPMFilter = new FileNameExtensionFilter("PPM", "ppm");
    FileNameExtensionFilter TIFFilter = new FileNameExtensionFilter("TIFF", "tiff", "tif");
    FileNameExtensionFilter PNGFilter = new FileNameExtensionFilter("PNG", "png");
    FileNameExtensionFilter BMPFilter = new FileNameExtensionFilter("BMP", "bmp");
    FileNameExtensionFilter JPGFilter = new FileNameExtensionFilter("JPEG", "jpeg", "jpg");

    jFileChooser.addChoosableFileFilter(PNMFilter);
    jFileChooser.addChoosableFileFilter(PGMFilter);
    jFileChooser.addChoosableFileFilter(PPMFilter);
    jFileChooser.addChoosableFileFilter(TIFFilter);
    jFileChooser.addChoosableFileFilter(PNGFilter);
    jFileChooser.addChoosableFileFilter(BMPFilter);
    jFileChooser.addChoosableFileFilter(JPGFilter);

    int selectedOption = jFileChooser.showSaveDialog(this);
    if (selectedOption == JFileChooser.APPROVE_OPTION) {
      fileName = jFileChooser.getSelectedFile().getName();
      directoryName = jFileChooser.getSelectedFile().getParent();
      FileNameExtensionFilter fileFilter = (FileNameExtensionFilter) jFileChooser.getFileFilter();

      int fileFormat = -1;
      if (fileFilter.getDescription().compareTo("PNM") == 0) {
        fileFormat = 0;
        fileExtension = "pnm";
      } else if (fileFilter.getDescription().compareTo("PGM") == 0) {
        fileFormat = 0;
        fileExtension = "pgm";
      } else if (fileFilter.getDescription().compareTo("PPM") == 0) {
        fileFormat = 0;
        fileExtension = "ppm";
      } else if (fileFilter.getDescription().compareTo("TIFF") == 0) {
        fileFormat = 1;
        fileExtension = "tiff";
      } else if (fileFilter.getDescription().compareTo("PNG") == 0) {
        fileFormat = 2;
        fileExtension = "png";
      } else if (fileFilter.getDescription().compareTo("BMP") == 0) {
        fileFormat = 4;
        fileExtension = "bmp";
      } else if (fileFilter.getDescription().compareTo("JPEG") == 0) {
        fileFormat = 3;
        fileExtension = "jpeg";
      } else {
      }

      if (fileFormat != -1) {
        if (!fileName.endsWith(fileExtension)) {
          fileName = fileName + "." + fileExtension;
        }

        float[][][] imageSamplesFloat = null;
        try {
          imageSamplesFloat = imageData.getSamplesFloat();
        } catch (IllegalAccessException e1) {
          e1.printStackTrace();
        }
        try {
          int[] QComponentsBits = new int[imageSamplesFloat.length];
          for (int i = 0; i < QComponentsBits.length; i++) {
            QComponentsBits[i] = 8;
          }
          SaveFile.SaveFileExtension(imageSamplesFloat, QComponentsBits, directoryName + File.separator + fileName, null);
        } catch (WarningException e) {
          JOptionPane.showMessageDialog(this, "File \"" + fileName + "\" cannot be saved", "Error Message", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }

  /**
   * Save the client cache in a file.
   */
  private void saveCache() {
    String fileName = "";
    String fileExtension = "";
    String directoryName = "";


    // Get the JPIP-stream media type
    String jpipStreamType = "jpp";


    JFileChooser jFileChooser = new JFileChooser();
    FileNameExtensionFilter jpipStreamFilter = new FileNameExtensionFilter(jpipStreamType, jpipStreamType.toUpperCase());
    jFileChooser.addChoosableFileFilter(jpipStreamFilter);

    boolean error = false;

    int selectedOption = jFileChooser.showSaveDialog(this);
    if (selectedOption == JFileChooser.APPROVE_OPTION) {
      fileName = jFileChooser.getSelectedFile().getName();
      directoryName = jFileChooser.getSelectedFile().getParent();
      FileNameExtensionFilter fileFilter = (FileNameExtensionFilter) jFileChooser.getFileFilter();

      // Check if file name has a valid extension
      String fileNameExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
      if (fileNameExtension.compareTo(jpipStreamType) != 0) {
        error = true;
        JOptionPane.showMessageDialog(this, "File extension must be ." + jpipStreamType, "Error Message", JOptionPane.ERROR_MESSAGE);
      }
    }

    if (!error) {
      try {
        jpipClient.saveCache(directoryName + File.separator + fileName);
      } catch (ErrorException e) {
      }
    }
  }

  /**
   * Loads a cache from a file.
   */
  private void loadCache() {
    String fileName = "";
    String fileExtension = "jpp-stream";
    String directoryName = "";


    // Get the JPIP-stream media type
    String jpipStreamType = "jpp";

    JFileChooser jFileChooser = new JFileChooser();
    FileNameExtensionFilter jpipStreamFilter = new FileNameExtensionFilter(jpipStreamType, jpipStreamType.toUpperCase());
    jFileChooser.addChoosableFileFilter(jpipStreamFilter);

    boolean error = false;

    int selectedOption = jFileChooser.showOpenDialog(this);
    if (selectedOption == JFileChooser.APPROVE_OPTION) {
      fileName = jFileChooser.getSelectedFile().getName();
      directoryName = jFileChooser.getSelectedFile().getParent();
      FileNameExtensionFilter fileFilter = (FileNameExtensionFilter) jFileChooser.getFileFilter();

      // Check if file name has a valid extension
      String fileNameExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
      if (fileNameExtension.compareTo(jpipStreamType) != 0) {
        error = true;
        JOptionPane.showMessageDialog(this, "File extension must be ." + jpipStreamType, "Error Message", JOptionPane.ERROR_MESSAGE);
      }
    }

    if (!error) {
      try {
        jpipClient.loadCache(directoryName + File.separator + fileName);
      } catch (ErrorException e) {
      }
    }
  }

  /**
   * Gets an image from a URL. This method is used to get an image from the
   * URL which has been input in the {@link #URLTextField}.
   *
   * @param url the URL to locate the
   */
  private void getImage(String url) {

    this.url = url;

    displayPane.setAction(DisplayPane.WAITING);
    resetSlidersAndSpinners();
    statusText.setText("Getting " + url);

    if (url.startsWith("jpip://")) {
    } else if (url.startsWith("file://")) {
      displayPane.setAction(DisplayPane.NOTHING);
      thumbnailDisplay.clear();
      displayPane.clear();
      JOptionPane.showMessageDialog(this, "The protocol ( file:// ) is not available", "Error Message", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // Get image form JPIP client
    try {

      jpipClient.getTarget(url);

    } catch (ErrorException e) {
      statusText.setText("Getting image " + target + " from " + server + ":  " + e.getMessage());
      displayPane.setAction(DisplayPane.NOTHING);
      thumbnailDisplay.clear();
      displayPane.clear();
      JOptionPane.showMessageDialog(this, e.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // Sliders and spinners, set minimum and maximum values			
    int majorTick;

    // Components
    int maxComps = imageData.getMaxComponents();
    for (int i = 0; i < maxComps; i++) {
      listModel.add(i, "Component " + i);
    }
    components = imageData.getComponents();
    componentList.setSelectedIndices(components);

    // Resolution levels
    int maxResolutionLevel = imageData.getMaxResolutionLevels();
    resolutionLevelPanel.setMaximum(maxResolutionLevel);
    majorTick = (int) (20 * maxResolutionLevel / 100F);
    resolutionLevelPanel.setMajorTickSpacing(majorTick);
    resolutionLevelPanel.setMinorTickSpacing((int) (5 * maxResolutionLevel / 100F));
    if (majorTick >= 1) {
      resolutionLevelPanel.setLabelTable((int) (20 * maxResolutionLevel / 100F));
    }
    resolutionLevel = imageData.getResolutionLevel();
    resolutionLevelPanel.setValue(resolutionLevel);

    // Layers
    int layers = imageData.getMaxLayers();
    layerPanel.setMaximum(layers);
    majorTick = (int) (20 * layers / 100F);
    layerPanel.setMajorTickSpacing(majorTick);
    layerPanel.setMinorTickSpacing((int) (5 * layers / 100F));
    if (majorTick >= 1) {
      layerPanel.setLabelTable((int) (20 * layers / 100F));
    }
    layer = imageData.getLayers();
    layerPanel.setValue(layer);

    // Update quality panel
    quality = imageData.getQuality();
    if (quality >= 0) {
      qualityPanel.setValue(quality);
    }

    // Display image
    try {
      bufImage = imageSamplesToBufferedImage(imageData.getSamplesFloat());
      displayPane.displayImage(bufImage, imageData.getFrameSize(), imageData.getRegionOffset(), imageData.getRegionSize());
    } catch (IllegalAccessException e1) {
      return;
    }

    // Set enable menu items
    closeImage.setEnabled(true);
    closeSessionItem.setEnabled(true);
    saveAs.setEnabled(true);

    // Get server, port and target
    server = jpipClient.getServer();
    port = jpipClient.getPort();
    target = jpipClient.getTargetName();

    // Thumbnail image
    displayThumbnail();

  }

  /**
   * Displays a thumbnail of the main image.
   */
  private void displayThumbnail() {

    /*thumbnailData = new ImageData(ImageData.SAMPLES_FLOAT);
    try {
      thumbnailSessionID = jpipClient.newSession(thumbnailData,
              newSessionDialog.getServer(),
              newSessionDialog.getPort(),
              newSessionDialog.getProxyServer(),
              newSessionDialog.getProxyPort(),
              newSessionDialog.getTarget(), -1);

      int[] fsiz = {thumbnailDisplay.getSize().width, thumbnailDisplay.getSize().height};
      int[] roff = {0, 0};
      int[] rsiz = {fsiz[0], fsiz[1]};
      jpipClient.getTarget(thumbnailSessionID, imageData.getComponents(), fsiz, roff, rsiz, imageData.getLayers() / 2, 50, ViewWindowField.ROUND_UP);

    } catch (IllegalAccessException e) {
    } catch (ErrorException e) {
      thumbnailDisplay.clear();
    }

    if (thumbnailData != null) {
      try {
        BufferedImage bufImageThumbnail = imageSamplesToBufferedImageThumbnail(thumbnailData.getSamplesFloat(), thumbnailData.getPrecision());
        thumbnailDisplay.setThumbnail(bufImageThumbnail);
      } catch (IllegalAccessException e) {
      }
    }*/
  }

  /**
   * Changes the the jpip client preferences.
   */
  private void changePreferences() {
    LogPreferencesDialog logPreferencesDialog = null;
    if (jpipClient == null) {
      logPreferencesDialog = new LogPreferencesDialog(this);
    } else {
      logPreferencesDialog = new LogPreferencesDialog(this, jpipClient.isLogEnabled(), jpipClient.useLogXMLFormat(), jpipClient.getLogLevel(), jpipClient.getLogFileName());
    }

    logPreferencesDialog.run();

    jpipClient.setLogEnabled(logPreferencesDialog.isEnabled());
    jpipClient.setLogLevel(logPreferencesDialog.getLogLevel());
    jpipClient.setLogFile(logPreferencesDialog.getLogFileName(), logPreferencesDialog.isXMLFormat());

  }

  /**
   * Changes the look and feel.
   */
  private void changeLookAndFeel() {
    LookAndFeelFrame lookAndFeelFrame = new LookAndFeelFrame(this);
    lookAndFeelFrame.change();
  }

  /**
   * This method sets visible thresholds and changes colormap
   * of the displayed image.
   */
  private void setThresholdsAndColorMaps() {
    Method rePaintImage = null;
    try {
      rePaintImage = this.getClass().getMethod("rePaintImage", new Class[]{});
      System.out.println(rePaintImage.toString());
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    new ThresholdsAndColorMaps(this, imageData, this, rePaintImage);
  }

  /**
   * This method forces the repainting of the displayed image.
   *
   */
  public void rePaintImage() {
    displayPane.rePaintImage();
    try {
      bufImage = imageSamplesToBufferedImage(imageData.getSamplesFloat());
      displayPane.displayImage(bufImage, imageData.getFrameSize(), imageData.getRegionOffset(), imageData.getRegionSize());
    } catch (IllegalAccessException e) {
    }

  }

  /**
   * This method converts from a three dimensional
   * array representation to a bufferered image representation.
   *
   * @param imageSamplesFloat a three-dimensional array with the image
   * samples
   *
   * @return a buffered image.
   */
  private BufferedImage imageSamplesToBufferedImage(float[][][] imageSamplesFloat) {

    if (imageData == null) {
      if (true) {
        System.exit(0);
      }
      throw new IllegalArgumentException();
    }

    // Image sizes
    int zSize = imageSamplesFloat.length;
    int ySize = imageSamplesFloat[0].length;
    int xSize = imageSamplesFloat[0][0].length;

    int colorModel;
    switch (zSize) {
      case 1:
        colorModel = BufferedImage.TYPE_BYTE_GRAY;
        break;
      case 3:
        colorModel = BufferedImage.TYPE_INT_RGB;
        break;
      default:
        //throw new IllegalArgumentException("Only 1 or 3 are supported");
        colorModel = BufferedImage.TYPE_INT_RGB;
        zSize = 3;
    }

    // Allocate memory only
    // - if the object has not been created
    // - dimensions are different from the previous one

    if ((fImagePixels == null) || (fImagePixels.length != zSize * ySize * xSize)) {
      fImagePixels = new float[zSize * ySize * xSize];
      bufImage = new BufferedImage(xSize, ySize, colorModel);
    }

    // Adjust image's range dinamic to range [0, 256]  ( y = a(x-min) )
    int[] precision = imageData.getPrecision();
    float[] slope = new float[precision.length];
    float[] minimums = new float[precision.length];
    boolean adjustPrecision = false;
    for (int z = 0; z < precision.length; z++) {
      if (precision[z] > 8) {
        adjustPrecision = true;
        break;
      }
    }

    if (adjustPrecision) {
      for (int z = 0; z < zSize; z++) {
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;

        for (int y = 0; y < ySize; y++) {
          for (int x = 0; x < xSize; x++) {
            if (imageSamplesFloat[z][y][x] > max) {
              max = imageSamplesFloat[z][y][x];
            }
            if (imageSamplesFloat[z][y][x] < min) {
              min = imageSamplesFloat[z][y][x];
            }
          }
        }
        slope[z] = 255F / (max - min);
        minimums[z] = min;
      }
    } else {
      Arrays.fill(slope, 1);
      Arrays.fill(minimums, 0);
    }

    // Fill the pixels
    int i = 0;
    for (int y = 0; y < ySize; y++) {
      for (int x = 0; x < xSize; x++) {
        for (int z = 0; z < zSize; z++) {
          fImagePixels[i++] = slope[z] * (imageSamplesFloat[z][y][x] - minimums[z]);
        }
      }
    }

    WritableRaster raster = bufImage.getRaster();
    raster.setPixels(0, 0, xSize, ySize, fImagePixels);

    return bufImage;
  }

  /**
   * This method converts from a three dimensional array representation to a
   * bufferered image representation.
   *
   * @param imageSamplesFloatThumbnail a three-dimensional array with the
   * image samples
   * @param precision an one-dimensional array with the precision (bit-depth)
   * for each image component.
   *
   * @return a buffered image.
   */
  private BufferedImage imageSamplesToBufferedImageThumbnail(float[][][] imageSamplesFloatThumbnail, int[] precision) {

    // Image sizes
    int zSize = imageSamplesFloatThumbnail.length;
    int ySize = imageSamplesFloatThumbnail[0].length;
    int xSize = imageSamplesFloatThumbnail[0][0].length;

    int colorModel;
    switch (zSize) {
      case 1:
        colorModel = BufferedImage.TYPE_BYTE_GRAY;
        break;
      case 3:
        colorModel = BufferedImage.TYPE_INT_RGB;
        break;
      default:
        throw new IllegalArgumentException("Only 1 or 3 are supported");
    }

    // Allocate memory only
    // - if the object has not been created
    // - dimensions are different from the previous one

    float[] fImagePixelsThumbnail = null;
    BufferedImage bufImageThumbnail = null;
    if ((fImagePixelsThumbnail == null) || (fImagePixelsThumbnail.length != zSize * ySize * xSize)) {
      fImagePixelsThumbnail = new float[zSize * ySize * xSize];
      bufImageThumbnail = new BufferedImage(xSize, ySize, colorModel);
    }


    // Adjust image's range dinamic to range [0, 256]  ( y = a(x-min) )
    float[] slope = new float[precision.length];
    float[] minimums = new float[precision.length];
    boolean adjustPrecision = false;
    for (int z = 0; z < precision.length; z++) {
      if (precision[z] > 8) {
        adjustPrecision = true;
        break;
      }
    }
    if (adjustPrecision) {
      for (int z = 0; z < zSize; z++) {
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;

        for (int y = 0; y < ySize; y++) {
          for (int x = 0; x < xSize; x++) {
            if (imageSamplesFloatThumbnail[z][y][x] > max) {
              max = imageSamplesFloatThumbnail[z][y][x];
            }
            if (imageSamplesFloatThumbnail[z][y][x] < min) {
              min = imageSamplesFloatThumbnail[z][y][x];
            }
          }
        }
        slope[z] = 255F / (max - min);
        minimums[z] = min;
      }
    } else {
      Arrays.fill(slope, 1);
      Arrays.fill(minimums, 0);
    }

    if (imageEqualization) {

      int[] palete = new int[256];
      for (int i = 0; i < 100; i++) {
        palete[i] = (int) ((200D / 100D) * i);
      }
      for (int i = 100; i < 256; i++) {
        palete[i] = (int) (((256D - 200D) / (256D - 100D)) * i);
      }

      // Fill the pixels
      int i = 0;
      for (int y = 0; y < ySize; y++) {
        for (int x = 0; x < xSize; x++) {
          for (int z = 0; z < zSize; z++) {
            fImagePixelsThumbnail[i++] = palete[(int) (slope[z] * (imageSamplesFloatThumbnail[z][y][x] - minimums[z]))];
          }
        }
      }

    } else {
      // Fill the pixels
      int i = 0;
      for (int y = 0; y < ySize; y++) {
        for (int x = 0; x < xSize; x++) {
          for (int z = 0; z < zSize; z++) {
            fImagePixelsThumbnail[i++] = slope[z] * (imageSamplesFloatThumbnail[z][y][x] - minimums[z]);
          }
        }
      }
    }

    WritableRaster raster = bufImageThumbnail.getRaster();
    raster.setPixels(0, 0, xSize, ySize, fImagePixelsThumbnail);

    return bufImageThumbnail;
  }
}