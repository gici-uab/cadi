/*
 * CADI Software - a JPIP Client/Server framework
 * Copyright (C) 2007-2012 Group on Interactive Coding of Images (GICI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import CADI.Client.Cache.ClientCacheManagement;

/**
 * This class implements the frame where the application preferences can be
 * set. These preferences are:<br>
 * <ul>
 * <li>JPIP session type
 * <li>JPIP cache type
 * <li>Advanced JPIP options
 * </ul>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1 2009/12/19
 */
public class SessionPreferencesDialog extends JDialog {

  private JFrame owner = null;

  /**
   * Peferences frame width
   */
  private static final int DIALOG_WIDTH = 500;

  /**
   * Peferences frame height
   */
  private static final int DIALOG_HEIGHT = 400;

  /**
   * Peferences frame title
   */
  private static final String TITLE = "Preferences Dialog";

  /**
   *
   */
  private Container jDialogPane = null;

  /**
   *
   */
  private JPanel propertiesPane = null;

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
  private PropertiesListener propertiesListener = null;

  // SESSION DIALOG
  private JRadioButton[] selectSessionButtons = null;

  private JCheckBox[] selectSessionTypeButtons = null;

  /**
   * Indicates whether stateless request or session request will be used.
   */
  private boolean useSession = true;

  /**
   * Indicates if HTTP session is allowed.
   * <p>
   * This attribute is only allowed when <data>useSession</data> attribute is true.
   */
  private boolean useHTTPSession = true;

  /**
   * Indicates if HTTP TCP session is allowed.
   * <p>
   * This attribute is only allowed when <data>useSession</data> attribute is true.
   */
  private boolean useHTTPTCPSession = false;

  // CACHE DIALOG
  private JCheckBox[] selectCheckBoxButtons = null;

  private JRadioButton explicitWildcardButton = null;

  private JRadioButton explicitNumberOfLayersButton = null;

  private JRadioButton explicitNumberOfBytesButton = null;

  private JRadioButton implicitWildcardButton = null;

  private JRadioButton implicitIndexRangeButton = null;

  private JRadioButton implicitNumberOfLayersButton = null;

  private JTextField maxCacheSizeField = null;

  private JCheckBox[] managementPolicyCheckButtons = null;

  /**
   *
   */
  private int cacheType = ClientCacheManagement.EXPLICIT_FORM;

  /**
   * Allowed values for cache type
   */
  private static final int NO_CACHE = ClientCacheManagement.NO_CACHE;

  private static final int EXPLICIT = ClientCacheManagement.EXPLICIT_FORM;

  private static final int IMPLICIT = ClientCacheManagement.IMPLICIT_FORM;

  private boolean useWildcard = false;

  private boolean useIndexRange = false;

  private boolean useNumberOfLayers = true;

  private boolean useNumberOfBytes = false;

  private long maxCacheSize = 0L;

  private int managementPolicy = 0;

  // ADVANCED DIALOG
  /**
   *
   */
  private JCheckBox httpKeepAliveButton = null;

  /**
   *
   */
  private JRadioButton jptStreamButton = new JRadioButton();

  private JRadioButton jppStreamButton = new JRadioButton();

  private JRadioButton rawStreamButton = new JRadioButton();

  private JCheckBox extendedHeadersButton = null;

  private boolean useKeepAlive = true;

  private boolean useExtendedHeaders = true;

  private ArrayList<String> imageReturnTypes = new ArrayList<String>();

  // ============================= public methods ==============================
  /**
   * Constructor.
   *
   * @param owner
   */
  public SessionPreferencesDialog(JFrame owner) {
    super();
    this.owner = owner;

    imageReturnTypes.add("jpp-stream");


    // Dialog frame properties
    jDialogPane = this.getContentPane();
    jDialogPane.setLayout(new BoxLayout(jDialogPane, BoxLayout.Y_AXIS));

    setTitle(TITLE);
    setLocationRelativeTo(owner);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
  }

  /**
   *
   *
   */
  public void run() {

    // Set frame size
    Dimension frameSize = new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT);
    setSize(frameSize);
    setPreferredSize(frameSize);
    setMinimumSize(frameSize);
    setMaximumSize(frameSize);


    jDialogPane.add(createPropertiesPane(frameSize.width, (int)(frameSize.height * 0.8)));
    //jDialogPane.add(Box.createVerticalStrut(5));
    jDialogPane.add(createButtonsPane(frameSize.width, (int)(frameSize.height * 0.2)));

    //	Frame settings
    setModal(true);
    setLocationRelativeTo(owner);
    setVisible(true);
    pack();
  }

  public boolean useSession() {
    return useSession;
  }

  public boolean useHTTPSession() {
    return (useSession && useHTTPSession);
  }

  public boolean useHTTPTCPSession() {
    return (useSession && useHTTPTCPSession);
  }

  public int getCacheType() {
    return cacheType;
  }

  public boolean useWildcard() {
    return useWildcard;
  }

  public boolean useIndexRange() {
    return useIndexRange;
  }

  public boolean useNumberOfLayers() {
    return useNumberOfLayers;
  }

  public boolean useNumberOfBytes() {
    return useNumberOfBytes;
  }

  public long getMaxCacheSize() {
    return maxCacheSize;
  }

  public int getManagementPolicy() {
    return managementPolicy;
  }

  public boolean useKeepAlive() {
    return useKeepAlive;
  }

  public ArrayList<String> getImageReturnTypes() {
    return imageReturnTypes;
  }

  public boolean useExtendedHeaders() {
    return useExtendedHeaders;
  }

  // ============================ private methods ==============================
  /**
   *
   * @param width
   * @param height
   * @return
   */
  private JPanel createPropertiesPane(int width, int height) {

    if (propertiesPane == null) {
      propertiesPane = new JPanel();

      propertiesListener = new PropertiesListener();

      Dimension panelSize = new Dimension(width, height);
      propertiesPane.setSize(panelSize);
      propertiesPane.setPreferredSize(panelSize);
      propertiesPane.setMinimumSize(panelSize);
      propertiesPane.setMaximumSize(panelSize);

      width = getPreferredSize().width - propertiesPane.getInsets().left - propertiesPane.getInsets().right - 10;
      height = getPreferredSize().height - propertiesPane.getInsets().top - propertiesPane.getInsets().bottom - 10;

      // Create the components
      JPanel sessionPanel = createSessionDialogBox();
      JPanel cachePanel = createCacheDialogBox(width, (int)(height * 0.4));
      JPanel advancedPanel = createAdvancedDialogBox();

      // Lay them out
      Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
      sessionPanel.setBorder(padding);
      cachePanel.setBorder(padding);
      advancedPanel.setBorder(padding);

      // Tabbed pane
      JTabbedPane tabbedPane = new JTabbedPane();
      tabbedPane.addTab("Session type", null, sessionPanel, "Specifies the session type");
      tabbedPane.addTab("Cache", null, cachePanel, "Cache descriptor");
      tabbedPane.addTab("Advanced", null, advancedPanel, "Advanced properties");


      propertiesPane.setLayout(new BorderLayout());
      propertiesPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      propertiesPane.add(tabbedPane, BorderLayout.CENTER);

    }

    return propertiesPane;
  }

  /**
   *
   * @param width
   * @param height
   * @return
   */
  private JPanel createButtonsPane(int width, int height) {

    if (buttonsPane == null) {
      buttonsPane = new JPanel();
      buttonsPane.setLayout(new BoxLayout(buttonsPane, BoxLayout.X_AXIS));
      buttonsPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

      buttonsPane.add(Box.createHorizontalGlue());
      if (acceptButton == null) {
        acceptButton = new JButton("Accept");
        acceptButton.addMouseListener(new MouseListener() {

          public void mouseClicked(MouseEvent event) {
          }

          public void mouseEntered(MouseEvent event) {
          }

          public void mouseExited(MouseEvent event) {
          }

          public void mousePressed(MouseEvent event) {
          }

          public void mouseReleased(MouseEvent event) {
            setVisible(false);
            dispose();
          }
        });
      }
      buttonsPane.add(acceptButton);

      buttonsPane.add(Box.createHorizontalGlue());

      if (defaultButton == null) {
        defaultButton = new JButton("Restore Defaults");
        defaultButton.setEnabled(false);
        defaultButton.addMouseListener(new MouseListener() {

          public void mouseClicked(MouseEvent event) {
          }

          public void mouseEntered(MouseEvent event) {
          }

          public void mouseExited(MouseEvent event) {
          }

          public void mousePressed(MouseEvent event) {
          }

          public void mouseReleased(MouseEvent event) {
            setVisible(false);
            dispose();
          }
        });
      }
      buttonsPane.add(defaultButton);

      buttonsPane.add(Box.createHorizontalGlue());

      if (cancelButton == null) {
        cancelButton = new JButton("Cancel");
        cancelButton.addMouseListener(new MouseListener() {

          public void mouseClicked(MouseEvent event) {
          }

          public void mouseEntered(MouseEvent event) {
          }

          public void mouseExited(MouseEvent event) {
          }

          public void mousePressed(MouseEvent event) {
          }

          public void mouseReleased(MouseEvent event) {
            setVisible(false);
            dispose();
          }
        });
      }
      buttonsPane.add(cancelButton);

      buttonsPane.add(Box.createHorizontalGlue());


      // Set sizes
      Dimension buttonsDims = new Dimension(width, height);
      buttonsPane.setSize(buttonsDims);
      buttonsPane.setPreferredSize(buttonsDims);
      buttonsPane.setMinimumSize(buttonsDims);
      buttonsPane.setMaximumSize(buttonsDims);
    }

    return buttonsPane;
  }

  /**
   *
   *
   */
  private JPanel createSessionDialogBox() {

    // Create the select session buttons
    selectSessionButtons = new JRadioButton[2];
    selectSessionButtons[0] = new JRadioButton("Stateless");
    selectSessionButtons[1] = new JRadioButton("Session");

    // Group buttons in a ButtonGroup
    final ButtonGroup groupSelectSession = new ButtonGroup();
    for (int i = 0; i < 2; i++) {
      groupSelectSession.add(selectSessionButtons[i]);
    }

    // Create the select session buttons type
    selectSessionTypeButtons = new JCheckBox[2];
    selectSessionTypeButtons[0] = new JCheckBox("Session over HTTP");
    selectSessionTypeButtons[1] = new JCheckBox("Session over HTTP/TCP");
    selectSessionTypeButtons[1].setEnabled(false);

    // Sets default client properties values
    if (useSession) {
      selectSessionButtons[1].setSelected(true);
      selectSessionTypeButtons[0].setSelected(useHTTPSession);
      selectSessionTypeButtons[1].setSelected(useHTTPTCPSession);
      selectSessionTypeButtons[1].setEnabled(false);
    } else {
      selectSessionButtons[0].setSelected(true);
      selectSessionTypeButtons[0].setEnabled(false);
      selectSessionTypeButtons[1].setEnabled(false);
      selectSessionTypeButtons[0].setSelected(useHTTPSession);
      selectSessionTypeButtons[1].setSelected(useHTTPTCPSession);
    }

    // Register a listener
    selectSessionButtons[0].addItemListener(propertiesListener);
    selectSessionButtons[1].addItemListener(propertiesListener);
    selectSessionTypeButtons[0].addItemListener(propertiesListener);
    selectSessionTypeButtons[1].addItemListener(propertiesListener);

    JLabel label = new JLabel("Select the session type to use");
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

    //  Lay select session buttons and label in panel
    JPanel selectSessionBox = new JPanel();
    selectSessionBox.setLayout(new BoxLayout(selectSessionBox, BoxLayout.Y_AXIS));
    selectSessionBox.add(label);

    for (int i = 0; i < 2; i++) {
      selectSessionBox.add(selectSessionButtons[i]);
    }
    selectSessionBox.add(selectSessionTypeButtons[0]);
    selectSessionBox.add(selectSessionTypeButtons[1]);


    JPanel pane = new JPanel();
    pane.setLayout(new BorderLayout());
    pane.add(selectSessionBox, BorderLayout.WEST);

    return pane;
  }

  /**
   * Creates the Cache Dialog Box
   *
   * @return a JPanel with the cache dialog box
   */
  private JPanel createCacheDialogBox(int width, int height) {


    Dimension jpipClientCachePaneDims = new Dimension(width, (int)(height * 0.4));
    Dimension localCachePropertiesPanelDims = new Dimension(width, (int)(height * 0.4));


    // JPIP CLIENT CACHE PREFERENCES

    // Check buttons for selecting the cache type
    selectCheckBoxButtons = new JCheckBox[3];
    selectCheckBoxButtons[0] = new JCheckBox("No cache");
    selectCheckBoxButtons[1] = new JCheckBox("Explicit form");
    selectCheckBoxButtons[2] = new JCheckBox("Implicit form");

    selectCheckBoxButtons[0].setActionCommand("noCacheFormButton");
    selectCheckBoxButtons[1].setActionCommand("explicitFormButton");
    selectCheckBoxButtons[2].setActionCommand("implicitFormButton");

    final ButtonGroup selectGroup = new ButtonGroup();
    selectGroup.add(selectCheckBoxButtons[0]);
    selectGroup.add(selectCheckBoxButtons[1]);
    selectGroup.add(selectCheckBoxButtons[2]);

    // Lay the select buttons
    Box selectCacheTypeBox = Box.createHorizontalBox();
    selectCacheTypeBox.add(selectCheckBoxButtons[0]);
    selectCacheTypeBox.add(selectCheckBoxButtons[1]);
    selectCacheTypeBox.add(selectCheckBoxButtons[2]);

    // Explicit cache descriptor
    JPanel explicitCachePanel = new JPanel();
    explicitCachePanel.setBorder(BorderFactory.createTitledBorder("Explicit form"));
    explicitCachePanel.setLayout(new BoxLayout(explicitCachePanel, BoxLayout.Y_AXIS));

    explicitWildcardButton = new JRadioButton("Wildcard");
    explicitNumberOfLayersButton = new JRadioButton("Number of layers");
    explicitNumberOfBytesButton = new JRadioButton("Number of bytes");

    ButtonGroup group1 = new ButtonGroup();
    group1.add(explicitWildcardButton);
    group1.add(explicitNumberOfLayersButton);
    group1.add(explicitNumberOfBytesButton);
    
    explicitCachePanel.add(explicitWildcardButton);
    explicitCachePanel.add(explicitNumberOfLayersButton);
    explicitCachePanel.add(explicitNumberOfBytesButton);
    
    
    // Implicit cache descriptor
    JPanel implicitCachePanel = new JPanel();
    implicitCachePanel.setBorder(BorderFactory.createTitledBorder("Implicit form"));
    implicitCachePanel.setLayout(new BoxLayout(implicitCachePanel, BoxLayout.Y_AXIS));

    implicitWildcardButton = new JRadioButton("Wildcard");
    implicitIndexRangeButton = new JRadioButton("Index range");
    implicitNumberOfLayersButton = new JRadioButton("Number of layers");

    ButtonGroup group2 = new ButtonGroup();
    group2.add(implicitWildcardButton);
    group2.add(implicitIndexRangeButton);
    group2.add(implicitNumberOfLayersButton);
    
    implicitCachePanel.add(implicitWildcardButton);
    implicitCachePanel.add(implicitIndexRangeButton);
    implicitCachePanel.add(implicitNumberOfLayersButton);


    // Lay the cache implicit and explicit forms on a horizontal box
    Box cacheFormsBox = Box.createHorizontalBox();
    cacheFormsBox.add(explicitCachePanel);
    cacheFormsBox.add(implicitCachePanel);


    // Lay check box buttons and cache form boxes on a JPanel
    JPanel jpipClientCachePropertiesPanel = new JPanel();
    jpipClientCachePropertiesPanel.setBorder(BorderFactory.createTitledBorder("JPIP Client Cache"));
    jpipClientCachePropertiesPanel.setLayout(new BoxLayout(jpipClientCachePropertiesPanel, BoxLayout.Y_AXIS));
    jpipClientCachePropertiesPanel.add(selectCacheTypeBox, BorderLayout.NORTH);
    jpipClientCachePropertiesPanel.add(cacheFormsBox, BorderLayout.CENTER);


    // CLIENT CACHE PREFERENCES (maximum size and management policy)
    JPanel localCachePropertiesPanel = createLocalCachePropertiesPanel(localCachePropertiesPanelDims.width, localCachePropertiesPanelDims.height);


    // Lay the selectors and descriptors
    JPanel cacheDialog = new JPanel();
    cacheDialog.setLayout(new BoxLayout(cacheDialog, BoxLayout.Y_AXIS));
    cacheDialog.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    cacheDialog.add(jpipClientCachePropertiesPanel, BorderLayout.NORTH);
    cacheDialog.add(Box.createVerticalStrut(10));
    cacheDialog.add(localCachePropertiesPanel, BorderLayout.SOUTH);
    Dimension cacheDialogDims = new Dimension(width, height);
    cacheDialog.setSize(cacheDialogDims);
    cacheDialog.setPreferredSize(cacheDialogDims);
    cacheDialog.setMinimumSize(cacheDialogDims);
    cacheDialog.setMaximumSize(cacheDialogDims);


    // REGISTER OF LISTENERS
    selectCheckBoxButtons[0].addItemListener(propertiesListener);
    selectCheckBoxButtons[1].addItemListener(propertiesListener);
    selectCheckBoxButtons[2].addItemListener(propertiesListener);
    explicitWildcardButton.addItemListener(propertiesListener);
    explicitNumberOfLayersButton.addItemListener(propertiesListener);
    explicitNumberOfBytesButton.addItemListener(propertiesListener);
    implicitWildcardButton.addItemListener(propertiesListener);
    implicitIndexRangeButton.addItemListener(propertiesListener);
    implicitNumberOfLayersButton.addItemListener(propertiesListener);

    // Sets default client properties values
    selectCheckBoxButtons[0].setEnabled(true);
    selectCheckBoxButtons[1].setEnabled(true);
    selectCheckBoxButtons[2].setEnabled(true);

    selectCheckBoxButtons[1].setSelected(true);

    explicitWildcardButton.setEnabled(false);
    explicitNumberOfLayersButton.setEnabled(true);
    explicitNumberOfBytesButton.setEnabled(true);

    explicitNumberOfLayersButton.setSelected(true);

    implicitWildcardButton.setEnabled(false);
    implicitIndexRangeButton.setEnabled(false);
    implicitNumberOfLayersButton.setEnabled(false);

    return cacheDialog;
  }

  /**
   * Creates the panel where the controls for the client cache properties
   * will be placed.
   *
   * @param width maximum width of the panel
   * @param height maximum height of the panel
   *
   * @return a JPanel with the controls.
   */
  private JPanel createLocalCachePropertiesPanel(int width, int height) {

    // Main panel
    JPanel localCachePropertiesPanel = new JPanel();
    localCachePropertiesPanel.setBorder(BorderFactory.createTitledBorder("Local Cache Properties"));
    localCachePropertiesPanel.setLayout(new BoxLayout(localCachePropertiesPanel, BoxLayout.X_AXIS));

    // Maximum dimensions for internal areas
    Dimension maxSizeDims = new Dimension((int)(width * 0.45), height - localCachePropertiesPanel.getInsets().top - localCachePropertiesPanel.getInsets().bottom);
    Dimension managementPolicyDims = new Dimension((int)(width * 0.45), height - localCachePropertiesPanel.getInsets().top - localCachePropertiesPanel.getInsets().bottom);
    int separatorHeight = height - localCachePropertiesPanel.getInsets().top - localCachePropertiesPanel.getInsets().bottom;


    // MAXIMUM SIZE
    // Label
    JLabel maxSizeLabel = new JLabel("Maximum Cache Size", SwingConstants.CENTER);

    // Input size
    long maxCacheSize = 0L;

    maxCacheSizeField = new JTextField(Long.toString(maxCacheSize), 6);
    maxCacheSizeField.setHorizontalAlignment(JTextField.RIGHT);

    // Set input-size sizes
    Dimension buffLengthDims = new Dimension(maxCacheSizeField.getPreferredSize().width, maxSizeLabel.getPreferredSize().height);
    maxCacheSizeField.setSize(buffLengthDims);
    maxCacheSizeField.setPreferredSize(buffLengthDims);
    maxCacheSizeField.setMinimumSize(buffLengthDims);
    maxCacheSizeField.setMaximumSize(buffLengthDims);

    // Input size listener
    maxCacheSizeField.addFocusListener(new FocusListener() {

      public void focusGained(FocusEvent e) {
        maxCacheSizeField.setBackground(Color.WHITE);
        acceptButton.setEnabled(true);
      }

      public void focusLost(FocusEvent e) {
        long maxSize = -1;
        try {
          maxSize = Integer.parseInt(maxCacheSizeField.getText());
        } catch (NumberFormatException nfe) {
          maxCacheSizeField.setBackground(Color.RED);
          acceptButton.setEnabled(false);
        }

        if (maxSize >= 0) {
        }
      }
    });

    JLabel measureLabel = new JLabel("bytes");

    Box inputSizeBox = Box.createHorizontalBox();
    inputSizeBox.add(maxCacheSizeField);
    inputSizeBox.add(measureLabel);

    // Lay label and text field on a box
    Box cacheSizeBox = Box.createVerticalBox();
    cacheSizeBox.add(maxSizeLabel);
    cacheSizeBox.add(inputSizeBox);
    cacheSizeBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    // Set box sizes
    cacheSizeBox.setSize(maxSizeDims);
    cacheSizeBox.setPreferredSize(maxSizeDims);
    cacheSizeBox.setMinimumSize(maxSizeDims);
    cacheSizeBox.setMaximumSize(maxSizeDims);


    // VERTICAL SEPARATOR
    JSeparator verticalSeparator = new JSeparator(JSeparator.VERTICAL);
    Dimension separatorDims = new Dimension(verticalSeparator.getPreferredSize().width, separatorHeight);
    verticalSeparator.setSize(separatorDims);
    verticalSeparator.setPreferredSize(separatorDims);
    verticalSeparator.setMinimumSize(separatorDims);
    verticalSeparator.setMaximumSize(separatorDims);


    // MANAGEMENT POLICY
    JLabel managementPolicyLabel = new JLabel("Management Policy", SwingConstants.CENTER);

    managementPolicyCheckButtons = new JCheckBox[3];
    managementPolicyCheckButtons[0] = new JCheckBox("NONE");
    managementPolicyCheckButtons[1] = new JCheckBox("LRU");
    managementPolicyCheckButtons[2] = new JCheckBox("FIFO");

    managementPolicyCheckButtons[0].setSelected(true);

    final ButtonGroup managementPolicyGroup = new ButtonGroup();
    managementPolicyGroup.add(managementPolicyCheckButtons[0]);
    managementPolicyGroup.add(managementPolicyCheckButtons[1]);
    managementPolicyGroup.add(managementPolicyCheckButtons[2]);

    // Listeners of management policy buttons
    managementPolicyCheckButtons[0].addItemListener(new ItemListener() {

      public void itemStateChanged(ItemEvent e) {
      }
    });

    managementPolicyCheckButtons[1].addItemListener(new ItemListener() {

      public void itemStateChanged(ItemEvent e) {
      }
    });

    managementPolicyCheckButtons[2].addItemListener(new ItemListener() {

      public void itemStateChanged(ItemEvent e) {
      }
    });


    // Lay buttons on a box
    Box managementPolicyButtonsBox = Box.createHorizontalBox();
    for (int i = 0; i < managementPolicyCheckButtons.length; i++) {
      managementPolicyButtonsBox.add(managementPolicyCheckButtons[i]);
    }


    // Lay label and buttons box in a box
    Box managementPolicyBox = Box.createVerticalBox();
    managementPolicyBox.add(managementPolicyLabel);
    managementPolicyBox.add(managementPolicyButtonsBox);

    // Set sizes
    managementPolicyBox.setSize(managementPolicyDims);
    managementPolicyBox.setPreferredSize(managementPolicyDims);
    managementPolicyBox.setMinimumSize(managementPolicyDims);
    managementPolicyBox.setMaximumSize(managementPolicyDims);

    // LAY MAX. SIZE BOX AND MANAGEMENT POLICY BOX ON JPANEL
    localCachePropertiesPanel.add(cacheSizeBox);
    localCachePropertiesPanel.add(verticalSeparator);
    localCachePropertiesPanel.add(managementPolicyBox);

    // Set sizes
    Dimension panelDims = new Dimension(width, height);
    localCachePropertiesPanel.setSize(panelDims);
    localCachePropertiesPanel.setPreferredSize(panelDims);
    localCachePropertiesPanel.setMinimumSize(panelDims);
    localCachePropertiesPanel.setMaximumSize(panelDims);

    return localCachePropertiesPanel;
  }

  /**
   *
   */
  private JPanel createAdvancedDialogBox() {

    // Keep alive
    httpKeepAliveButton = new JCheckBox("HTTP keep alive");
    httpKeepAliveButton.setActionCommand("httpKeepAliveButton");
    httpKeepAliveButton.setEnabled(true);

    httpKeepAliveButton.setSelected(useKeepAlive);

    httpKeepAliveButton.addItemListener(new ItemListener() {

      public void itemStateChanged(ItemEvent e) {
        useKeepAlive = httpKeepAliveButton.isSelected();
      }
    });

    // Image return type
    jptStreamButton = new JRadioButton("JPT-STREAM");
    jppStreamButton = new JRadioButton("JPP-STREAM");
    rawStreamButton = new JRadioButton("RAW");

    jppStreamButton.setSelected(true);
    jptStreamButton.setEnabled(false);
    rawStreamButton.setEnabled(false);

    JPanel imageReturnTypePanel = new JPanel();
    imageReturnTypePanel.setLayout(new BoxLayout(imageReturnTypePanel, BoxLayout.Y_AXIS));
    imageReturnTypePanel.setBorder(BorderFactory.createTitledBorder("Image return type"));

    imageReturnTypePanel.add(jptStreamButton, BorderLayout.EAST);
    imageReturnTypePanel.add(jppStreamButton, BorderLayout.EAST);
    imageReturnTypePanel.add(rawStreamButton, BorderLayout.EAST);

    // Client buffer length
		/* JLabel label = new JLabel("Buffer length");
     * int len = clientProperties.bufferLength;
     * if (len < 0) {
     * len = 0;
     * }
     * bufferLength = new JTextField(Integer.toString(len), 4);
     *
     * JPanel bufferLengthPanel = new JPanel();
     * bufferLengthPanel.setLayout(new BoxLayout(bufferLengthPanel, BoxLayout.X_AXIS));
     * bufferLengthPanel.add(label);
     * bufferLengthPanel.add(bufferLength);
     * bufferLengthPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 30));
     */

    // Extended headers
    extendedHeadersButton = new JCheckBox("Extended headers");
    extendedHeadersButton.setActionCommand("extendedHeaders");
    extendedHeadersButton.setEnabled(true);

    extendedHeadersButton.setSelected(useExtendedHeaders);

    extendedHeadersButton.addItemListener(new ItemListener() {

      public void itemStateChanged(ItemEvent e) {
        useExtendedHeaders = extendedHeadersButton.isSelected();
      }
    });

    // LEFT COLUMN
    JPanel leftColumnPanel = new JPanel();
    leftColumnPanel.setLayout(new BoxLayout(leftColumnPanel, BoxLayout.Y_AXIS));

    leftColumnPanel.add(httpKeepAliveButton);
    leftColumnPanel.add(imageReturnTypePanel);
    //leftColumnPanel.add(bufferLengthPanel);

    // RIGHT COLUMN
    JPanel rightColumnPanel = new JPanel();
    rightColumnPanel.setLayout(new BoxLayout(rightColumnPanel, BoxLayout.Y_AXIS));
    rightColumnPanel.add(extendedHeadersButton);

    // Register a listener
    httpKeepAliveButton.addItemListener(propertiesListener);

    // Lay buttons and label in panel
    JPanel advancedPanel = new JPanel();
    advancedPanel.setLayout(new GridLayout(1, 2));
    advancedPanel.add(leftColumnPanel);
    advancedPanel.add(rightColumnPanel);

    return advancedPanel;
  }

  /**
   *
   *
   */
  class PropertiesListener implements ItemListener {

    public void itemStateChanged(ItemEvent e) {

      Object source = e.getItemSelectable();

      if (source == selectSessionButtons[0]) {   // Stateless
        if (e.getStateChange() == ItemEvent.SELECTED) {
          useSession = false;
          
          selectSessionTypeButtons[0].setEnabled(false);
          selectSessionTypeButtons[1].setEnabled(false);

          selectCheckBoxButtons[1].setEnabled(true);
          explicitWildcardButton.setEnabled(selectCheckBoxButtons[1].isSelected());
          explicitNumberOfLayersButton.setEnabled(selectCheckBoxButtons[1].isSelected());
          explicitNumberOfBytesButton.setEnabled(selectCheckBoxButtons[1].isSelected());

          selectCheckBoxButtons[2].setEnabled(true);
          implicitWildcardButton.setEnabled(selectCheckBoxButtons[2].isSelected());
          implicitIndexRangeButton.setEnabled(selectCheckBoxButtons[2].isSelected());
          implicitNumberOfLayersButton.setEnabled(selectCheckBoxButtons[2].isSelected());

        }
      } else if (source == selectSessionButtons[1]) {
        selectSessionTypeButtons[0].setEnabled(true);
        selectSessionTypeButtons[1].setEnabled(false);
        if (!selectSessionTypeButtons[0].isSelected() && !selectSessionTypeButtons[1].isSelected()) {
          selectSessionTypeButtons[0].setSelected(true);
        }

        if (e.getStateChange() == ItemEvent.SELECTED) {
          selectCheckBoxButtons[1].setEnabled(true);
          explicitWildcardButton.setEnabled(false);
          explicitNumberOfLayersButton.setEnabled(selectCheckBoxButtons[1].isSelected());
          explicitNumberOfBytesButton.setEnabled(selectCheckBoxButtons[1].isSelected());

          selectCheckBoxButtons[2].setEnabled(true);
          implicitWildcardButton.setEnabled(selectCheckBoxButtons[2].isSelected());
          implicitIndexRangeButton.setEnabled(false);
          implicitNumberOfLayersButton.setEnabled(selectCheckBoxButtons[2].isSelected());
        }

      } else if (source == selectSessionTypeButtons[0]) {	// HTTP session
        useHTTPSession = true;
      } else if (source == selectSessionTypeButtons[1]) { 	// HTTP-TCP session
        useHTTPTCPSession = true;
      } else if (source == selectCheckBoxButtons[0]) {  // No cache
        if (e.getStateChange() == ItemEvent.SELECTED) {
          explicitWildcardButton.setEnabled(false);
          explicitNumberOfLayersButton.setEnabled(false);
          explicitNumberOfBytesButton.setEnabled(false);

          implicitWildcardButton.setEnabled(false);
          implicitIndexRangeButton.setEnabled(false);
          implicitNumberOfLayersButton.setEnabled(false);

          cacheType = NO_CACHE;

        }
      } else if (source == selectCheckBoxButtons[1]) {  // Explicit descriptor
        if (e.getStateChange() == ItemEvent.SELECTED) {
          explicitWildcardButton.setEnabled(selectSessionButtons[0].isSelected());
          explicitNumberOfLayersButton.setEnabled(true);
          explicitNumberOfBytesButton.setEnabled(true);

          implicitWildcardButton.setEnabled(false);
          implicitIndexRangeButton.setEnabled(false);
          implicitNumberOfLayersButton.setEnabled(false);

          cacheType = EXPLICIT;

        }
      } else if (source == selectCheckBoxButtons[2]) { // Implicit descriptor
        if (e.getStateChange() == ItemEvent.SELECTED) {
          explicitWildcardButton.setEnabled(false);
          explicitNumberOfLayersButton.setEnabled(false);
          explicitNumberOfBytesButton.setEnabled(false);

          implicitWildcardButton.setEnabled(true);
          implicitIndexRangeButton.setEnabled(selectSessionButtons[0].isSelected());
          implicitNumberOfLayersButton.setEnabled(true);

          cacheType = IMPLICIT;

        }
      } else if (source == explicitWildcardButton) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          useWildcard = true;
          useNumberOfLayers = false;
          useNumberOfBytes = false;
          useIndexRange = false;
        } else {
        }
      } else if (source == explicitNumberOfLayersButton) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          useWildcard = false;
          useNumberOfLayers = true;
          useNumberOfBytes = false;
          useIndexRange = false;
        } else {
        }

      } else if (source == explicitNumberOfBytesButton) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          useWildcard = false;
          useNumberOfLayers = false;
          useNumberOfBytes = true;
          useIndexRange = false;
        } else {
        }

      } else if (source == implicitWildcardButton) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          useWildcard = true;
          useNumberOfLayers = false;
          useNumberOfBytes = false;
          useIndexRange = false;
        } else {
        }

      } else if (source == implicitIndexRangeButton) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          useWildcard = false;
          useNumberOfLayers = false;
          useNumberOfBytes = false;
          useIndexRange = true;
        } else {
        }

      } else if (source == implicitNumberOfLayersButton) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          useWildcard = false;
          useNumberOfLayers = true;
          useNumberOfBytes = false;
          useIndexRange = false;
        } else {
        }

      } else {
        return;
      }
    }
  }
}
