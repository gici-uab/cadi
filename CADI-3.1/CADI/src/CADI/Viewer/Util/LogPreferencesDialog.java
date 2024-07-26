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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;

import CADI.Common.Log.CADILog;

/**
 * This class implements a window dialog to set the client's log preferences.
 * <p>
 * Usage example:<br>
 * &nbsp; constructor<br>
 * &nbsp; run<br>
 * &nbsp; getMethods<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2009/12/29
 */
public class LogPreferencesDialog extends JDialog {

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

  // LOG DIALOG
  private JCheckBox logEnableButton = null;

  private JRadioButton textFormatButton = null;

  private JRadioButton xmlFormatButton = null;

  private JRadioButton logLevelInfo = null;

  private JRadioButton logLevelWarning = null;

  private JRadioButton logLevelError = null;

  private JRadioButton consoleDestinationButton = null;

  private JRadioButton fileDestinationButton = null;

  private JTextField fileNameTextFile = null;

  private JButton chooserButton = null;

  private boolean isEnabled = false;

  private boolean xmlFormat = false;

  private int logLevel = CADILog.LEVEL_INFO;

  private String logFileName = null;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public LogPreferencesDialog(JFrame owner) {
    this(owner, false, false, CADILog.LEVEL_INFO, null);
  }

  /**
   * Constructor.
   *
   * @param owner
   * @param isEnabled
   * @param xmlFormat
   * @param logLevel
   * @param logFileName
   */
  public LogPreferencesDialog(JFrame owner, boolean isEnabled, boolean xmlFormat, int logLevel, String logFileName) {
    super();

    if (owner == null) throw new NullPointerException();

    this.isEnabled = isEnabled;
    this.xmlFormat = xmlFormat;
    this.logLevel = logLevel;
    this.logFileName = logFileName;

    // Dialog frame properties
    jDialogPane = this.getContentPane();
    jDialogPane.setLayout(new BoxLayout(jDialogPane, BoxLayout.Y_AXIS));

    setTitle(TITLE);
    setLocationRelativeTo(owner);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
  }

  /**
   * The main method. It opens the dialog window.
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

  /**
   * Returns the {@link #isEnabled} attribute.
   *
   * @return the {@link #isEnabled} attribute.
   */
  public boolean isLogEnabled() {
    return isEnabled;
  }

  /**
   * Returns the {@link #xmlFormat} attribute.
   *
   * @return the {@link #xmlFormat} attribute.
   */
  public boolean isXMLFormat() {
    return xmlFormat;
  }

  /**
   * Returns the {@link #logLevel} attribute.
   *
   * @return the {@link #logLevel} attribute.
   */
  public int getLogLevel() {
    return logLevel;
  }

  /**
   * Returns the {@link #logFileName} attribute.
   *
   * @return the {@link #logFileName} attribute.
   */
  public String getLogFileName() {
    return logFileName;
  }

  // ============================ private methods ==============================
  /**
   *
   * @param width
   * @param height
   *
   * @return
   */
  private JPanel createPropertiesPane(int width, int height) {

    if (propertiesPane == null) {
      propertiesPane = new JPanel();

      Dimension panelSize = new Dimension(width, height);
      propertiesPane.setSize(panelSize);
      propertiesPane.setPreferredSize(panelSize);
      propertiesPane.setMinimumSize(panelSize);
      propertiesPane.setMaximumSize(panelSize);

      width = getPreferredSize().width - propertiesPane.getInsets().left - propertiesPane.getInsets().right - 30;
      height = getPreferredSize().height - propertiesPane.getInsets().top - propertiesPane.getInsets().bottom - 30;

      // Create
      JPanel logPanel = createLogDialogBox(width, height);

      // Lay them out
      Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
      logPanel.setBorder(padding);

      propertiesPane.setLayout(new BorderLayout());
      propertiesPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      propertiesPane.add(logPanel, BorderLayout.CENTER);

    }

    return propertiesPane;
  }

  /**
   *
   * @param width
   * @param height
   *
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
   * Creates the dialog to set up the log preferences.
   *
   * @param width panel width
   * @param height panel height
   *
   * @return a JPanel of the log dialog box.
   */
  private JPanel createLogDialogBox(int width, int height) {

    // Enable / Disable checkbox
    Box enableDisableBox = Box.createHorizontalBox();
    logEnableButton = new JCheckBox("Enable");

    logEnableButton.setSelected(isEnabled);
    logEnableButton.addItemListener(new ItemListener() {

      public void itemStateChanged(ItemEvent e) {
        System.out.println("asdklfjdslñajfñlkasdjlkfjdask");
        // Update buttons on the frame
        if (logEnableButton.isSelected()) {
          System.out.println("asdklfjdslñajfñlkasdjlkfjdask1111111");
          textFormatButton.setEnabled(true);
          xmlFormatButton.setEnabled(true);
          logLevelInfo.setEnabled(true);
          logLevelWarning.setEnabled(true);
          logLevelError.setEnabled(true);
          consoleDestinationButton.setEnabled(true);
          fileDestinationButton.setEnabled(true);
          fileNameTextFile.setEditable(true);
          chooserButton.setEnabled(false);
        } else {
          System.out.println("asdklfjdslñajfñlkasdjlkfjdask22222");
          textFormatButton.setEnabled(false);
          xmlFormatButton.setEnabled(false);
          logLevelInfo.setEnabled(false);
          logLevelWarning.setEnabled(false);
          logLevelError.setEnabled(false);
          consoleDestinationButton.setEnabled(false);
          fileDestinationButton.setEnabled(false);
          fileNameTextFile.setEditable(false);
          chooserButton.setEnabled(false);
        }
      }
    });
    Dimension dims = new Dimension(width, logEnableButton.getPreferredSize().height);
    enableDisableBox.add(logEnableButton);
    enableDisableBox.setSize(dims);
    enableDisableBox.setPreferredSize(dims);
    enableDisableBox.setMinimumSize(dims);
    enableDisableBox.setMaximumSize(dims);


    // Log format
    textFormatButton = new JRadioButton("Plain text");
    textFormatButton.setEnabled(isEnabled);

    xmlFormatButton = new JRadioButton("XML format");
    xmlFormatButton.setEnabled(isEnabled);

    final ButtonGroup groupSelectLogFormat = new ButtonGroup();
    groupSelectLogFormat.add(textFormatButton);
    groupSelectLogFormat.add(xmlFormatButton);

    Box formatBox = Box.createVerticalBox();
    formatBox.add(textFormatButton);
    formatBox.add(xmlFormatButton);
    formatBox.setEnabled(isEnabled);
    formatBox.setBorder(BorderFactory.createTitledBorder("Format"));


    // Log level
    logLevelInfo = new JRadioButton("Info");
    logLevelInfo.setEnabled(isEnabled);

    logLevelWarning = new JRadioButton("Warning");
    logLevelWarning.setEnabled(isEnabled);

    logLevelError = new JRadioButton("Error");
    logLevelError.setEnabled(isEnabled);

    final ButtonGroup groupSelectLogLevel = new ButtonGroup();
    groupSelectLogLevel.add(logLevelInfo);
    groupSelectLogLevel.add(logLevelWarning);
    groupSelectLogLevel.add(logLevelError);

    switch (logLevel) {
      case CADILog.LEVEL_DEBUG:
      case CADILog.LEVEL_INFO:
        logLevelInfo.setSelected(true);
        break;
      case CADILog.LEVEL_WARNING:
        logLevelWarning.setSelected(true);
        break;
      case CADILog.LEVEL_ERROR:
        logLevelError.setSelected(true);
        break;
      default:
    }


    Box levelBox = Box.createVerticalBox();
    levelBox.add(logLevelInfo);
    levelBox.add(logLevelWarning);
    levelBox.add(logLevelError);
    levelBox.setBorder(BorderFactory.createTitledBorder("Level"));


    // Set size of format and level box
    dims = new Dimension(width / 2, 4 * textFormatButton.getPreferredSize().height);
    formatBox.setSize(dims);
    formatBox.setPreferredSize(dims);
    formatBox.setMinimumSize(dims);
    formatBox.setMaximumSize(dims);
    levelBox.setSize(dims);
    levelBox.setPreferredSize(dims);
    levelBox.setMinimumSize(dims);
    levelBox.setMaximumSize(dims);


    // Create box for format and level
    Box formatAndLevelBox = Box.createHorizontalBox();
    formatAndLevelBox.add(formatBox);
    formatAndLevelBox.add(levelBox);


    // Console / File
    consoleDestinationButton = new JRadioButton("Console");
    fileDestinationButton = new JRadioButton("File");
    fileDestinationButton.addItemListener(new ItemListener() {

      public void itemStateChanged(ItemEvent e) {
        fileNameTextFile.setEnabled(fileDestinationButton.isSelected());
        chooserButton.setEnabled(fileDestinationButton.isSelected());
      }
    });

    Box fileBox = Box.createHorizontalBox();
    fileNameTextFile = new JTextField();
    fileNameTextFile.setMargin(new Insets(2, 2, 2, 2));
    chooserButton = new JButton("Find");
    chooserButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          fileNameTextFile.setText(fileChooser.getSelectedFile().getAbsolutePath());
        } else {
          fileNameTextFile.setText("");
        }
      }
    });
    fileBox.add(fileNameTextFile);
    fileBox.add(chooserButton);

    final ButtonGroup groupSelectLogDestination = new ButtonGroup();
    groupSelectLogDestination.add(consoleDestinationButton);
    groupSelectLogDestination.add(fileDestinationButton);

    consoleDestinationButton.setEnabled(isEnabled);
    fileDestinationButton.setEnabled(isEnabled);
    fileNameTextFile.setEnabled(isEnabled);
    fileNameTextFile.setEditable(isEnabled);
    chooserButton.setEnabled(isEnabled);

    if (logFileName == null) {
      consoleDestinationButton.setSelected(true);
      chooserButton.setEnabled(false);
    } else {
      fileDestinationButton.setSelected(true);
      fileNameTextFile.setText(logFileName);
    }


    // Set sizes for all boxes
    Box destinationBox = Box.createVerticalBox();
    destinationBox.add(consoleDestinationButton);
    destinationBox.add(fileDestinationButton);
    destinationBox.add(fileBox);
    destinationBox.setBorder(BorderFactory.createTitledBorder("Destination"));

    dims = new Dimension(width, 3 * consoleDestinationButton.getPreferredSize().height + fileNameTextFile.getPreferredSize().height);
    destinationBox.setSize(dims);
    destinationBox.setPreferredSize(dims);
    destinationBox.setMinimumSize(dims);
    destinationBox.setMaximumSize(dims);
    Box horizontalDestBox = Box.createHorizontalBox();
    horizontalDestBox.add(destinationBox);


    Box vBox = Box.createVerticalBox();
    vBox.add(enableDisableBox);
    vBox.add(formatAndLevelBox);
    vBox.add(horizontalDestBox);

    // Lay buttons and label in panel
    JPanel logPanel = new JPanel();
    logPanel.add(vBox);


    return logPanel;
  }
}