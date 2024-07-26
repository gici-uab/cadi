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
import java.awt.Font;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;



import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.text.NumberFormat;


import CADI.Client.ImageData;


/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2008/11/10
 */
public class ThresholdsAndColorMaps extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Peferences frame width
	 */
	private static final int DIALOG_WIDTH = 500;

	/**
	 * Peferences frame height
	 */
	private static final int DIALOG_HEIGHT = 550;

	/**
	 * Peferences frame title
	 */
	private static final String TITLE = "Set thresholds and change displayed colormap";


	/**
	 * Reference to the parent object.
	 */
	private JFrame owner = null;

	/**
	 * Class where the invocated method is.
	 */
	private Object obj = null;

	/**
	 * Reference to the container of the displayed image.
	 */
	private ImageData imageData = null;

	/**
	 * Method to invoke a repaint of the displayed image.
	 */
	private Method rePaintImage = null;




	// INTERNAL ATTRIBUTES

	/**
	 * 
	 */
	private Container jFramePane = null;

	private JPanel mainPane = null;

	private JPanel formPane = null;
	
	private JPanel histogramPane = null;
	
	private JPanel thresholdsPane = null;

	private Box maxThresholdBox=null;

	private Box minThresholdBox=null;

	private Box maxThresholdPercentBox=null;

	private Box minThresholdPercentBox=null;

	private Box maxThresholdAbsoluteBox=null;

	private Box minThresholdAbsoluteBox=null;

	private JTextField maxThresholdPercentField = null;

	private JFormattedTextField minThresholdPercentField = null;

	private JFormattedTextField maxThresholdAbsoluteField = null;

	private JTextField minThresholdAbsoluteField = null;

	private JPanel buttonsPane = null;

	private JButton acceptButton = null;

	private JButton cancelButton = null;
	
	private JButton applyButton = null;

	private JPanel sliderPane = null;

	private JSlider sl1 = null;

	private JSlider sl2 = null;

	private JTextField xMinField = null;
	private JTextField yMinField = null;
	private JTextField xMaxField = null;
	private JTextField yMaxField = null;
	private JCheckBox xMinCheck = null;
	private JCheckBox xMaxCheck = null;
	private JCheckBox yMinCheck = null;
	private JCheckBox yMaxCheck = null;

	float[][][] image = null;

	int [][] histogram;


	private NumberFormat amountFormat;
	int maxvalue=0;
	int minvalue=0;
	
	private JCheckBox[] checkBoxHistogramOperations = null;


	// BIBLIOGRAPHY REFERENCES:
	// http://www.ph.tn.tudelft.nl/Courses/FIP/noframes/fip-Contents.html
	// http://homepages.inf.ed.ac.uk/rbf/HIPR2/hipr_top.htm

  // ============================= public methods ==============================
	/**
	 * Constructor.
	 * <p>
	 * This constructor is not allowed.
	 */
	public ThresholdsAndColorMaps() {
		throw new IllegalArgumentException();
	}

	/**
	 * Constructor.
	 * 
	 * @param owner
	 * @param rePaintImage
	 */
	public ThresholdsAndColorMaps(JFrame owner, ImageData imageData, Object obj, Method rePaintImage) {
		super();

		// Check input parameters
		if (obj == null || rePaintImage == null) throw new NullPointerException();

		// Copy input parameters
		this.owner = owner;
		this.imageData = imageData;
		this.obj = obj;
		this.rePaintImage = rePaintImage;

		try {
			this.image = imageData.getSamplesFloat();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}


		// Dialog frame properties
		jFramePane = this.getContentPane();
		jFramePane.setLayout(new BoxLayout(jFramePane, BoxLayout.Y_AXIS));

		setTitle(TITLE);
		setLocationRelativeTo(owner);	
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


		// Set frame size
		Dimension frameSize = new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT);
		setSize(frameSize);
		setPreferredSize(frameSize);
		setMinimumSize(frameSize);
		setMaximumSize(frameSize);

		jFramePane.add(createPane());


		//	Frame settings
		setLocationRelativeTo(owner);
		setVisible(true);
		pack();


		//testLino();
	}

	/*public void paint(Graphics g) {

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setPaint(Color.gray);

		paintGradient(g2, 50, 170, 20, 256);
		paintGradient(g2, 130, 170, 20, 256);
		//printHistogram(histogram,g);
	}*/

	
	// ============================ private methods ==============================
  
  /**
   * 
   * @return 
   */
	private JPanel createPane() {

		if (imageData == null) {
			if (true) System.exit(0);
			throw new IllegalArgumentException();
		}

		if (mainPane == null){

			mainPane = new JPanel();

			Box vBox = Box.createVerticalBox();

			Dimension frameSize = new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT);
			vBox.setSize(frameSize);
			vBox.setPreferredSize(frameSize);
			vBox.setMaximumSize(frameSize);
			vBox.setMinimumSize(frameSize);

			int width = DIALOG_WIDTH - 30;
			int height = 90;
			vBox.add(createHistogramOperations(width, height), BorderLayout.CENTER);
			vBox.add(Box.createVerticalStrut(10));
			vBox.add(createThresholdsPane(width, height), BorderLayout.CENTER);

			mainPane.add(vBox);
		}
		return mainPane;

	}

	/**
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	private JPanel createHistogramOperations(int width, int height) {
		
		if (histogramPane == null) {
			histogramPane = new JPanel();
			histogramPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			
			histogramPane.setBorder(BorderFactory.createTitledBorder("Histogram operations"));
			histogramPane.setLayout(new BoxLayout(histogramPane, BoxLayout.X_AXIS));
			
			checkBoxHistogramOperations = new JCheckBox[2];
			checkBoxHistogramOperations[0] = new JCheckBox("Stretch");
			checkBoxHistogramOperations[1] = new JCheckBox("Equalization");

			final ButtonGroup histogramOperationGroup = new ButtonGroup();
			histogramOperationGroup.add(checkBoxHistogramOperations[0]);
			histogramOperationGroup.add(checkBoxHistogramOperations[1]);

			Box histOpBox = Box.createHorizontalBox();
			histOpBox.add(Box.createHorizontalGlue());
			histOpBox.add(checkBoxHistogramOperations[0]);
			histOpBox.add(Box.createHorizontalGlue());
			histOpBox.add(checkBoxHistogramOperations[1]);
			histOpBox.add(Box.createHorizontalGlue());
		
			
			// Apply button
			JButton apply = new JButton("Apply");
			Dimension bDimensions = new Dimension(100, 25);
			apply.setSize(bDimensions);
			apply.setPreferredSize(bDimensions);
			apply.setMinimumSize(bDimensions);
			apply.setMaximumSize(bDimensions);

			apply.addMouseListener(new MouseListener(){
				public void mouseClicked(MouseEvent e) {
					if (checkBoxHistogramOperations[0].isSelected() ) contrastStretching(image, 0, 255);
					else equalizeImage(image);
					try {
						rePaintImage.invoke(obj, (Object[])null);
					} catch (IllegalArgumentException e1) { e1.printStackTrace();
					} catch (IllegalAccessException e2) { e2.printStackTrace();
					} catch (InvocationTargetException e3) { e3.printStackTrace();
					}
				}

				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
				public void mouseReleased(MouseEvent e) {}

			});

			Box applyBox = Box.createHorizontalBox();
			applyBox.add(Box.createHorizontalGlue());
			applyBox.add(apply);
			
			Box histVertOpBox = Box.createVerticalBox();
			histVertOpBox.add(histOpBox);
			histVertOpBox.add(applyBox);
			histogramPane.add(histVertOpBox);
			
			Dimension maxSizeDims = new Dimension(width, height);
			histogramPane.setSize(maxSizeDims);
			histogramPane.setPreferredSize(maxSizeDims);
			histogramPane.setMinimumSize(maxSizeDims);
			histogramPane.setMaximumSize(maxSizeDims);
		}
		
		return histogramPane;
	}
	
	/**
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	private JPanel createThresholdsPane(int width, int height) {
	
		if (thresholdsPane == null) {
			thresholdsPane = new JPanel();
			thresholdsPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			
			thresholdsPane.setBorder(BorderFactory.createTitledBorder("Thresholds"));
			thresholdsPane.setLayout(new BoxLayout(thresholdsPane, BoxLayout.Y_AXIS));
			
			
			// xMin, yMin row
			JLabel xMinLabel = new JLabel("xMin");
			xMinLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
						
			xMinField = new JTextField(5);
			xMinField.setEditable(true);
			xMinField.setSize(new Dimension(width, 20));
			xMinField.setMinimumSize(new Dimension(width, 20));
			xMinField.setMaximumSize(new Dimension(width, 20));
			xMinField.setPreferredSize(new Dimension(width, 20));
			
			xMinCheck = new JCheckBox("Percentage");
			
			JLabel yMinLabel = new JLabel("yMin");
			yMinLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

			yMinField = new JTextField(5);
			yMinField.setEditable(true);
			yMinField.setSize(new Dimension(width, 20));
			yMinField.setMinimumSize(new Dimension(width, 20));
			yMinField.setMaximumSize(new Dimension(width, 20));
			yMinField.setPreferredSize(new Dimension(width, 20));
			
			yMinCheck = new JCheckBox("Percentage");
			
			
			Box minBox = Box.createHorizontalBox();
			minBox.add(Box.createHorizontalGlue());
			minBox.add(xMinLabel);
			minBox.add(xMinField);
			minBox.add(xMinCheck);
			minBox.add(Box.createHorizontalGlue());
			minBox.add(yMinLabel);
			minBox.add(yMinField);
			minBox.add(yMinCheck);
			minBox.add(Box.createHorizontalGlue());
			
			minBox.setMinimumSize(new Dimension(width, 20));
			minBox.setMaximumSize(new Dimension(width, 20));
			minBox.setPreferredSize(new Dimension(width, 20));
			
			
			// xMax, yMax row
			JLabel xMaxLabel = new JLabel("xMax");
			xMaxLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
						
			xMaxField = new JTextField(5);
			xMaxField.setEditable(true);
			xMaxField.setSize(new Dimension(width, 20));
			xMaxField.setMinimumSize(new Dimension(width, 20));
			xMaxField.setMaximumSize(new Dimension(width, 20));
			xMaxField.setPreferredSize(new Dimension(width, 20));
			
			xMaxCheck = new JCheckBox("Percentage");
			
			JLabel yMaxLabel = new JLabel("yMax");
			yMaxLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

			yMaxField = new JTextField(5);
			yMaxField.setEditable(true);
			yMaxField.setSize(new Dimension(width, 20));
			yMaxField.setMinimumSize(new Dimension(width, 20));
			yMaxField.setMaximumSize(new Dimension(width, 20));
			yMaxField.setPreferredSize(new Dimension(width, 20));
			
			yMaxCheck = new JCheckBox("Percentage");
			
			Box maxBox = Box.createHorizontalBox();
			maxBox.add(Box.createHorizontalGlue());
			maxBox.add(xMaxLabel);
			maxBox.add(xMaxField);
			maxBox.add(xMaxCheck);
			maxBox.add(Box.createHorizontalGlue());
			maxBox.add(yMaxLabel);
			maxBox.add(yMaxField);
			maxBox.add(yMaxCheck);
			maxBox.add(Box.createHorizontalGlue());
			
			maxBox.setMinimumSize(new Dimension(width, 20));
			maxBox.setMaximumSize(new Dimension(width, 20));
			maxBox.setPreferredSize(new Dimension(width, 20));
			
			
			// Apply button
			JButton apply = new JButton("Apply");
			Dimension bDimensions = new Dimension(100, 25);
			apply.setSize(bDimensions);
			apply.setPreferredSize(bDimensions);
			apply.setMinimumSize(bDimensions);
			apply.setMaximumSize(bDimensions);

			apply.addMouseListener(new MouseListener(){
				public void mouseClicked(MouseEvent e) {
					int xMin, xMax, yMin, yMax;
					try {
						xMin = Integer.parseInt(xMinField.getText());
						xMax = Integer.parseInt(xMaxField.getText());
						yMin = Integer.parseInt(yMinField.getText());
						yMax = Integer.parseInt(yMaxField.getText());
					} catch (NumberFormatException nfe) {
						return;
					}
					int[] precision = imageData.getPrecision();
					if (xMinCheck.isSelected()) xMin = (int)((1<<precision[0]) * xMin / 100.0);
					if (xMaxCheck.isSelected()) xMin = (int)((1<<precision[0]) * xMax / 100.0);
					if (yMinCheck.isSelected()) xMin = (int)((1<<precision[0]) * yMin / 100.0);
					if (yMaxCheck.isSelected()) xMin = (int)((1<<precision[0]) * yMax / 100.0);
					
					thresholdImage(image, xMin, yMin, xMax, yMax);
					try {
						rePaintImage.invoke(obj, (Object[])null);
					} catch (IllegalArgumentException e1) { e1.printStackTrace();
					} catch (IllegalAccessException e2) { e2.printStackTrace();
					} catch (InvocationTargetException e3) { e3.printStackTrace();
					}
				}

				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
				public void mouseReleased(MouseEvent e) {}

			});
			
			Box applyBox = Box.createHorizontalBox();
			applyBox.add(Box.createHorizontalGlue());
			applyBox.add(apply);
			
			// Vertical vox
			Box vBox = Box.createVerticalBox();
			vBox.add(minBox);
			vBox.add(maxBox);
			vBox.add(applyBox);
			
			thresholdsPane.add(vBox);
			Dimension maxSizeDims = new Dimension(width, height);
			thresholdsPane.setSize(maxSizeDims);
			thresholdsPane.setPreferredSize(maxSizeDims);
			thresholdsPane.setMinimumSize(maxSizeDims);
			thresholdsPane.setMaximumSize(maxSizeDims);
		}
		
		
		return thresholdsPane;
	}
	
	/**
	 * This method computes the historgram of an image.
	 * 
	 * @param image a three-dimension array with the image samples.
	 * 
	 * @return a bi-dimensional array with the historgram of the image. The
	 * 	first component is the component and the second one is the histogram
	 * 	for that image component.
	 */
	private int[][] calculateHistogram(float[][][] image) {

		// get bit depth of each image component
		int[] precision= imageData.getPrecision();

		// calculate max range (2^precision) for each component
		int[] rango = new int[precision.length];
		for (int i = 0; i < precision.length; i++)
			rango[i] = 1 << precision[i];

		// calculate maximum of the component's image range
		int rangomax = 0;
		for (int i = 0; i < rango.length; i++) {
			if(rango[i] > rangomax) rangomax = rango[i];
		}

		//Image sizes
		int zSize = image.length;
		int ySize = image[0].length;
		int xSize = image[0][0].length;


		// calculate the histogram for each image component
		int[][] imageHistogram = new int[zSize][rangomax];
		for (int z = 0; z < zSize; z++){
			for (int y = 0; y < ySize; y++){
				for (int x = 0; x < xSize; x++){
					imageHistogram[z][(int)image[z][y][x]]++;
				}
			}
		}
		
		return imageHistogram;
	}
	
	/**
	 * Computes the cumulative histogram of an image.
	 * 
	 * @param image
	 * 
	 * @return a bi-dimensional array with the cumulative histogram. The first
	 * 	index corresponds with the image component, and the second index is
	 * 	value of the cumulative histogram for the pixel value. 
	 */
	private int[][] calculateCumulativeHistogram(float[][][] image) {
		int[][] imHist = calculateHistogram(image);
		
		int[][] cumHist = new int[imHist.length][imHist[0].length];
		
		for (int z = 0; z < imHist.length; z++) {
			cumHist[z][0] = imHist[z][0];
			for (int i = 1; i < imHist[z].length; i++)
				cumHist[z][i] = cumHist[z][i-1] + imHist[z][i];
		}
		
		return cumHist;
	}
	
	/**
	 * Adjust an image to its available dynamic range.
	 * 
	 * @param image
	 * @param minOutputRange
	 * @param maxOutputRange
	 */
	private void contrastStretching(float [][][] image, int minOutputRange, int maxOutputRange){

		//Image sizes
		int zSize = image.length;

		//creación de arrays para histogramas
		int[][] imageHistogram = calculateHistogram(image);
		
		// calculate the histogram for each image component
		for (int z = 0; z < zSize; z++){
			
			// find non-zero value's index 
			int minIndex = 0;
			while ((imageHistogram[z][minIndex] == 0) && (minIndex < imageHistogram[z].length)) {
				minIndex++;
			}

			// find non-zero values's index
			int maxIndex = imageHistogram[z].length - 1;
			while ((imageHistogram[z][maxIndex] == 0) && (maxIndex > 0)){
				maxIndex--;
			}
			
			// apply contrast stretching
			for (int y = 0; y < image[z].length; y++) {
				for (int x = 0; x < image[z][y].length; x++) {
					if (image[z][y][x] < minOutputRange) image[z][y][x] = 0;
					else if (image[z][y][x] > maxOutputRange) image[z][y][x] = maxOutputRange;
					else {
						double slope = (double)(maxOutputRange-minOutputRange)/(double)(maxIndex-minIndex);
						image[z][y][x] = (int)((image[z][y][x] - minIndex)*slope + minOutputRange);
					}
				}
			}
		}
	}

	/**
	 * Equalizes the image.
	 * 
	 * @param image
	 */
	private void equalizeImage(float[][][] image) {
      
		// Gets cummulative histogram
		int[][] cumHist = calculateCumulativeHistogram(image);
		
		int ySize = image[0].length;
		int xSize = image[0][0].length;
		int[] precision= imageData.getPrecision();
		
		// performs the equalization
		for (int z = 0; z < image.length; z++) {
			int maxValue = (1 << precision[z])-1;
			for (int y = 0; y < image[z].length; y++) {
				for (int x = 0; x < image[z][y].length; x++) {
				    image[z][y][x] = (int)(1.0*cumHist[z][(int)(image[z][y][x])]*maxValue/(ySize*xSize));
				}
			}
		}
	}
	
	/**
	 * 
	 * @param image
	 * @param xMin
	 * @param yMin
	 * @param xMax
	 * @param yMax
	 */
	private void thresholdImage(float[][][] image, int xMin, int yMin, int xMax, int yMax) {
		double slope = (double)(yMax - yMin) / (double)(xMax - xMin);
		for (int z = 0; z < image.length; z++) {
			for (int y = 0; y < image[z].length; y++) {
				for (int x = 0; x < image[z][y].length; x++) {
					if (image[z][y][x] <= xMin) image[z][y][x] = yMin;
					else if (image[z][y][x] >= xMax) image[z][y][x] = yMax;
					else {
						image[z][y][x] = (int)(slope*(image[z][y][x]-xMin)+yMin);
					}
				}
			}
		}
	}
	
	
	/**************************************************************************/
	/**                   ANTIGUAS FUNCIONES											 **/
	/**************************************************************************/

	private JPanel createFormPane() {
	
		if (formPane == null) {
			formPane = new JPanel();
			formPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			Dimension dimLabel = new Dimension (150,25);
			Dimension dimLabel2 = new Dimension (50,25);
			Dimension dimLabel3= new Dimension (250,25);
			Dimension dimLabel4=new Dimension(350,25);
			int imputTextHeight = 20;
			int widthhorizontal= (DIALOG_WIDTH-2*10-100);
			int width = (widthhorizontal- 10) /2;
			//percent label
			Box percentBox = Box.createHorizontalBox();
			JLabel percent =new JLabel ("Percent");
			percent.setSize(dimLabel3);
			percent.setMinimumSize(dimLabel3);
			percent.setMaximumSize(dimLabel3);
			percent.setPreferredSize(dimLabel3);
			percentBox.add(percent);
			//absolute label
			Box absoluteBox =Box.createHorizontalBox();
			JLabel absolute=new JLabel ("Absolute");
			absolute.setSize(dimLabel4);
			absolute.setMinimumSize(dimLabel4);
			absolute.setMaximumSize(dimLabel4);
			absolute.setPreferredSize(dimLabel4);
			absoluteBox.add(absolute);
			//percentAbsoluteBox
			Box percentAbsoluteBox=Box.createHorizontalBox();

			//percentAbsoluteBox.add(Box.createHorizontalStrut(400));
			percentAbsoluteBox.add(percentBox);
			//percentAbsoluteBox.add(Box.createHorizontalStrut(20));
			percentAbsoluteBox.add(absoluteBox);


			//MAXIMOS
			maxThresholdBox=Box.createHorizontalBox();

			maxThresholdPercentBox=Box.createHorizontalBox();

			JLabel maxThresholdPercentLabel = new JLabel("Max Threshold");
			maxThresholdPercentLabel.setSize(dimLabel);
			maxThresholdPercentLabel.setMinimumSize(dimLabel);
			maxThresholdPercentLabel.setPreferredSize(dimLabel);
			maxThresholdPercentLabel.setMaximumSize(dimLabel);


			maxThresholdPercentField = new JTextField();
			maxThresholdPercentField.setEditable(true);
			//maxThresholdPercentField = new JTextField(" ", 11);
			maxThresholdPercentField.setFont(new Font("Plane", Font.PLAIN, 10));
			maxThresholdPercentField.setSize(new Dimension(50, 20));
			maxThresholdPercentField.setMinimumSize(new Dimension(50, 20));
			maxThresholdPercentField.setMaximumSize(new Dimension(50, 20));
			maxThresholdPercentField.setPreferredSize(new Dimension(50, 20));
			//maxThresholdPercentField.addKeyListener(this);


			maxThresholdPercentBox.add(maxThresholdPercentLabel);
			maxThresholdPercentBox.add(maxThresholdPercentField);

			maxThresholdAbsoluteBox=Box.createHorizontalBox();
			JLabel maxThresholdAbsoluteLabel = new JLabel(" ");
			maxThresholdAbsoluteLabel.setSize(dimLabel2);
			maxThresholdAbsoluteLabel.setMinimumSize(dimLabel2);
			maxThresholdAbsoluteLabel.setPreferredSize(dimLabel2);
			maxThresholdAbsoluteLabel.setMaximumSize(dimLabel2);

			maxThresholdAbsoluteField = new JFormattedTextField(amountFormat);
			maxThresholdAbsoluteField.setEditable(true);
			//maxThresholdAbsoluteField= new JFormattedTextField(" ", 11);
			maxThresholdAbsoluteField.setSize(new Dimension(50, 20));
			maxThresholdAbsoluteField.setMinimumSize(new Dimension(50, 20));
			maxThresholdAbsoluteField.setMaximumSize(new Dimension(50, 20));
			maxThresholdAbsoluteField.setPreferredSize(new Dimension(50, 20));
		
			//maxThresholdPercentField.addInputMethodListener(InputMethodEvent e);
			//maxThresholdAbsoluteField.addInputMethodListener(inputMethodListener);

			maxThresholdAbsoluteBox.add(maxThresholdAbsoluteLabel);
			maxThresholdAbsoluteBox.add(maxThresholdAbsoluteField);

			//maxThresholdBox.createHorizontalStrut(40);
			maxThresholdBox.add(maxThresholdPercentBox);
			//maxThresholdBox.createHorizontalStrut(10);
			maxThresholdBox.add(maxThresholdAbsoluteBox);
			//maxThresholdBox.add(Box.createHorizontalGlue());


			maxThresholdBox.setMinimumSize(new Dimension(widthhorizontal + 20, 20));
			maxThresholdBox.setMaximumSize(new Dimension(widthhorizontal + 20, 20));
			maxThresholdBox.setPreferredSize(new Dimension(widthhorizontal + 20, 20));



			//MINIMOS
			minThresholdBox=Box.createHorizontalBox();

			minThresholdPercentBox=Box.createHorizontalBox();

			JLabel minThresholdPercentLabel = new JLabel("Min Threshold");
			minThresholdPercentLabel.setSize(dimLabel);
			minThresholdPercentLabel.setMinimumSize(dimLabel);
			minThresholdPercentLabel.setPreferredSize(dimLabel);
			minThresholdPercentLabel.setMaximumSize(dimLabel);

			minThresholdPercentField = new JFormattedTextField();
			minThresholdPercentField.setEditable(true);
			//minThresholdPercentField = new JTextField(" ",11);
			minThresholdPercentField.setSize(new Dimension(50, 20));
			minThresholdPercentField.setMinimumSize(new Dimension(50, 20));
			minThresholdPercentField.setMaximumSize(new Dimension(50, 20));
			minThresholdPercentField.setPreferredSize(new Dimension(50, 20));
			//minThresholdPercentField.addKeyListener(this);



			minThresholdPercentBox.add(minThresholdPercentLabel);
			minThresholdPercentBox.add(minThresholdPercentField);

			minThresholdAbsoluteBox=Box.createHorizontalBox();
			JLabel minThresholdAbsoluteLabel = new JLabel(" ");
			minThresholdAbsoluteLabel.setSize(dimLabel2);
			minThresholdAbsoluteLabel.setMinimumSize(dimLabel2);
			minThresholdAbsoluteLabel.setPreferredSize(dimLabel2);
			minThresholdAbsoluteLabel.setMaximumSize(dimLabel2);

			minThresholdAbsoluteField = new JTextField();
			minThresholdAbsoluteField.setEditable(true);
			minThresholdAbsoluteField= new JTextField(" ", 11);
			minThresholdAbsoluteField.setSize(new Dimension(50, 20));
			minThresholdAbsoluteField.setMinimumSize(new Dimension(50, 20));
			minThresholdAbsoluteField.setMaximumSize(new Dimension(50, 20));
			minThresholdAbsoluteField.setPreferredSize(new Dimension(50, 20));
			//minThresholdPercentField.addKeyListener(this);

			minThresholdAbsoluteBox.add(minThresholdAbsoluteLabel);
			minThresholdAbsoluteBox.add(minThresholdAbsoluteField);

			minThresholdBox.add(Box.createHorizontalStrut(40));
			minThresholdBox.add(minThresholdPercentBox);
			minThresholdBox.add(Box.createHorizontalStrut(10));
			minThresholdBox.add(minThresholdAbsoluteBox);
			minThresholdBox.add(Box.createHorizontalGlue());

			minThresholdBox.setMinimumSize(new Dimension(widthhorizontal + 20, 20));
			minThresholdBox.setMaximumSize(new Dimension(widthhorizontal + 20, 20));
			minThresholdBox.setPreferredSize(new Dimension(widthhorizontal + 20, 20));

			// Add horizontal boxes to the vertical
			Box vbox = Box.createVerticalBox();

			vbox.add(percentAbsoluteBox);
			vbox.add(Box.createVerticalStrut(10));
			vbox.add(maxThresholdBox);
			vbox.add(Box.createVerticalStrut(10));
			vbox.add(minThresholdBox);

			formPane.add(vbox);

			/*formPane.add(percentAbsoluteBox);
			formPane.add(Box.createVerticalStrut(10));
			formPane.add(maxThresholdBox);
			formPane.add(Box.createVerticalStrut(10));
			formPane.add(minThresholdBox);*/

		}

		return formPane;
	}

	private JPanel createSliderPane(){

		if (sliderPane==null){

			sliderPane=new JPanel();
			sliderPane.setLayout(new BoxLayout(sliderPane, BoxLayout.X_AXIS));
			sliderPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			sl1=new JSlider(JSlider.VERTICAL);
			sl1.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {	

				}				
			});

			Dimension sliderDims = new Dimension(sl1.getPreferredSize().width, 256);
			sl1.setSize(sliderDims);
			sl1.setMinimumSize(sliderDims);
			sl1.setPreferredSize(sliderDims);
			sl1.setMaximumSize(sliderDims);

			sl2=new JSlider(JSlider.VERTICAL);
			sl2.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {	

				}				
			});
			sl2.setSize(sliderDims);
			sl2.setMinimumSize(sliderDims);
			sl2.setPreferredSize(sliderDims);
			sl2.setMaximumSize(sliderDims);

			Box slider1=Box.createVerticalBox();
			slider1.add(sl1);
			Box slider2=Box.createVerticalBox();
			slider2.add(sl2);


			//Add sliders to panel
			sliderPane.add(Box.createHorizontalStrut(20));
			sliderPane.add(slider1);
			sliderPane.add(Box.createHorizontalStrut(60));
			sliderPane.add(slider2);
			sliderPane.add(Box.createHorizontalGlue());

		}

		return sliderPane;
	}



	/**
	 * 
	 * @param g2
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	private void paintGradient(Graphics2D g2, int x1, int y1, int x2, int y2) {
		GradientPaint whiteToBlack = new GradientPaint(x1, y1, Color.white, x1, y2, Color.black);
		g2.setPaint(whiteToBlack);
		g2.fill(new Rectangle2D.Double(x1, y1, x2, y2));
		g2.setPaint(Color.black);
	}


	
	
	

	public void printHistogram (int [][] hist, Graphics g){

		int colorModel;
		int orig_l= hist.length; //255
		System.out.println(orig_l);

		int [][] hist_print=new int[orig_l][hist[0].length];

		int incr=orig_l/256;

		//int [][] histo=new int[256][2];

		int x=0;
		int media=0;

		//selecionar el modelo RGB o Escala de grises

		switch (hist[0].length) {
			case 1:
				colorModel = BufferedImage.TYPE_BYTE_GRAY;
				break;
			case 3:
				colorModel = BufferedImage.TYPE_INT_RGB;

				break;
			default:
				//throw new IllegalArgumentException("Only 1 or 3 are supported");
				colorModel = BufferedImage.TYPE_INT_RGB;
			System.out.println("entra color model default");

		}

		if (colorModel==BufferedImage.TYPE_BYTE_GRAY){
			/*
			for (int i=0; i<hist_original.length;i+=incr){
				for (int j=i; j<incr; j++ ){
					media+=hist_original[j][z];
					//System.out.println(media);
				}
				media/=incr;
				//System.out.println(media);
				histo [x]=media;
				x++;
			}*/
			//for (int i=0; i<histo.length; i++)System.out.println(histo[i]);

			g.setColor(Color.black);

//			busqueda de máximo en histograma y acotación a 255
			int max=0;
			for (int i=0; i<orig_l; i++){
				if (hist[i][0]>max){
					max=hist[i][0];
				}
			}

			for (int i=0; i<orig_l; i++){
				hist_print[i][0]=(hist[i][0]*255)/max;
			}

			/*
			for (int i=0; i<hist_print.length; i+=incr){
				System.out.println(hist_print[i][0]);
			}*/
			int y=0;
			for (int i=0; i<hist_print.length; i++){
				//System.out.println(y);
				g.drawLine(160, 170+y,160+hist_print[i][0],170+y);
				//System.out.println(hist_print[i][0]);
				y++;
			}

		}else if (colorModel==BufferedImage.TYPE_INT_RGB) {
			System.out.println("entra en else");
			/*for (int i=0; i<histo.length; i++){
				histo[i][0]=i;
				histo[i][1]=(255-i);
			}*/

			for (int z=0;z<hist[0].length; z++){

				switch (z){
					case 0:
						g.setColor(Color.red);
						System.out.println("entra en case0");
						break;
					case 1:
						g.setColor(Color.green);
						System.out.println("entra en case1");
						break;
					case 2: 
						g.setColor(Color.blue);
						System.out.println("entra en case2");
						break;
					default:
						g.setColor(Color.black);
				}


				//busqueda de máximo en histograma y acotación a 255
				int max=0;
				for (int i=0; i<orig_l; i++){
					if (hist[i][z]>max){
						max=hist[i][z];
					}
				}

				for (int i=0; i<orig_l; i++){
					hist_print[i][z]=(hist[i][z]*255)/max;
				}


				/*for (int i=0; i<hist_print.length; i+=incr){
					System.out.println(hist_print[i][z]);
				}*/

				int y=0;
				for (int i=0; i<hist_print.length; i+=incr){
					g.drawLine(160, 170+y,160+hist_print[i][z],170+y);
					y++;
				}
			}
		}

	}

	public void streching(int max, int min, float [][][] image){

		//calculo de la precision y el rango
		int[] precision= imageData.getPrecision();

		double[] rango=new double[precision.length];

		for (int i=0; i<precision.length; i++){
			rango[i]=Math.pow(2D, (double)precision[i]);

		}

		double rangomax=0;
		for (int i=0; i<rango.length; i++){
			if(rango[i]>rangomax) rangomax=rango[i];
		}

		//Image sizes
		int zSize = image.length;
		int ySize = image[0].length;
		int xSize = image[0][0].length;

		//creación de array para histograma original
		int [][]hist_original=new int  [(int)rangomax][zSize];

//		parámetros de la recta que se utiliza para la compresion lineal
		float[]pendiente=new float[zSize];
		float [] minimus=new float [zSize];

//		histograma para cada componente
		for (int z=0; z<zSize; z++){

			for (int y=0; y<ySize; y++){
				for (int x=0; x<xSize; x++){
					hist_original[(int)image[z][y][x]][z]++;
				}
			}

			//COMPRESION 			
			//System.out.println("min: "+min+", max: "+max);

			//pendiente de la recta de compresion
			pendiente[z]= 255F/(float)(max-min);

			/*for (int i=0; i<pendiente.length; i++){
				System.out.println("pendiente: "+pendiente[i]);
			}*/

			//array contenedor de mínimos
			minimus [z]=min;

			for (int y=0; y<ySize; y++){
				for (int x=0; x<xSize; x++){
					image[z][y][x]=(int)(pendiente[z]*(image[z][y][x]-minimus[z]));
				}
			}
			/*int valor;
			for (int i=0; i<hist_original.length; i++){
				valor=(int)(pendiente[z]*(i-minimus[z])); //posicion a la que corresponde en el histograma de 255

				if (valor<0) valor=0;
				if (valor>255) valor=255;
				hist[valor][z]+=hist_original[i][z];	
			}*/
		}




		try {
			rePaintImage.invoke(obj, (Object[])null);
		} catch (IllegalArgumentException e) { e.printStackTrace();
		} catch (IllegalAccessException e) { e.printStackTrace();
		} catch (InvocationTargetException e) { e.printStackTrace();
		}	
	}

	private void testLino() {
		float[][][] samplesFloat = null;
		try {
			samplesFloat = imageData.getSamplesFloat();
		} catch (IllegalAccessException e) { e.printStackTrace(); }

		for (int z = 0; z < samplesFloat.length; z++)
			for (int y = 0; y < samplesFloat[z].length; y++)
				for (int x = 0; x < samplesFloat[z][y].length; x++)
					samplesFloat[z][y][x] /= 20;


		try {
			rePaintImage.invoke(obj, (Object[])null);
		} catch (IllegalArgumentException e) { e.printStackTrace();
		} catch (IllegalAccessException e) { e.printStackTrace();
		} catch (InvocationTargetException e) { e.printStackTrace();
		}

	}

}



