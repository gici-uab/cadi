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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This class creates a JPanel with a JSlider and a JSpinner.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2007-2012/12/07
 */
public class SliderAndSpinnerPanel extends JPanel {

	/**
	 * The panel's label.
	 */
	private String label = null;

	/**
	 * The minimum allowed value.
	 */
	private int min;
	
	/**
	 * The maximum allowed value
	 */
	private int max;
	
	/**
	 * The current value.
	 */
	private int value;
	
	/**
	 * The difference between two consecutive values. 
	 */
	private int stepSize;
	
	/**
	 * Width of the panel.
	 */
	private int width = 0;
	
	
	// INTERNAL ATTRIBUTES
	
	/**
	 * The slider object.
	 */
	private JSlider slider = null;

	/**
	 * The spinner object.
	 */
	private JSpinner spinner = null;

	/**
	 * Used to pass information between the slider and the spinner objects.
	 */
	boolean changed = false;


	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param label the panel name.
	 * @param min the minimum value of the slider.
	 * @param max the maximum value of the slider.
	 * @param value the initial value of the slider.
	 * @param stepSize the difference between elements of the sequence.
	 * 
	 * @throws IllegalArgumentException if the following expression is false: minimum <= value <= maximum
	 */
	public SliderAndSpinnerPanel(String label, int min, int max, int value, int stepSize, int width) {
		super();
			
		// Copy parameters
		this.label = label;
		this.min = min;
		this.max = max;
		this.value = value;
		this.stepSize = stepSize;
		this.width = width;
		
		// Check values
		if (label == null) label = "";
		if (min > max) throw new IllegalArgumentException("max value must be greather than min");
		if ((value < min) || (value > max)) throw new IllegalArgumentException("value must be between min and max");
		if (width <= 0) throw new IllegalArgumentException("width of panel must be greather than 0");
				
		// Creates the panel
		createPanel();
	}

	/**
	 * Sets the minimum value to <code>minimum</code>.
	 * 
	 * @param minimum the new minimum. 
	 * @throws IllegalArgumentException if the minimum is greather than the maximum.
	 */
	public void setMinimum(int minimum) {
		if (minimum > max) throw new IllegalArgumentException("max value must be greather than min");
		this.min = minimum;
		slider.setMinimum(min);
		spinner.setModel(new SpinnerNumberModel(value, min, max, stepSize));
	}
	
	/**
	 * Sets the panel properties.
	 * 
	 * @param min the minimum value of the slider.
	 * @param max the maximum value of the slider.
	 * @param value the initial value of the slider.
	 * @param stepSize the difference between elements of the sequence.
	 * 
	 * @throws IllegalArgumentException if the following expression is false: minimum <= value <= maximum.
	 */
	public void setRangeProperties(int min, int max, int value, int stepSize) {
		if (min > max) throw new IllegalArgumentException("max value must be greather than min");
		if ((value < min) || (value > max)) throw new IllegalArgumentException("value must be between min and max");
		
		slider.setModel(new DefaultBoundedRangeModel(value, 0, min, max)); 
	}
	
	/**
	 * Sets the maximum value to <code>maximum</code>.
	 * 
	 * @param maximum the new maximum. 
	 * @throws IllegalArgumentException if the minimum is greather than the maximum.
	 */
	public void setMaximum(int maximum) {
		if (maximum < min) throw new IllegalArgumentException("max value must be greather than min");
		this.max = maximum;
		slider.setMaximum(max);
		spinner.setModel(new SpinnerNumberModel(value, min, max, stepSize));
	}
	
	/**
	 * Sets the current value to <code>n</code>.
	 * 
	 * @param n the new minimum. 
	 * @throws IllegalArgumentException if the value is not between the minimum
	 * 			and the maximum.
	 */
	public void setValue(int n) {
		if ((n < min) || (n > max)) throw new IllegalArgumentException("value must be between min and max");
		this.value = n;
		changed = false;
		slider.setValue(value);
		//spinner.setModel(new SpinnerNumberModel(value, min, max, stepSize));
	}
	
	/**
	 * Sets the step size value to <code>stepSize</code>.
	 * 
	 * @param stepSize the new stepSize.
	 */
	public void setStepSize(int stepSize) {
		this.stepSize = stepSize;
		spinner.setModel(new SpinnerNumberModel(value, min, max, stepSize));
	}	
	
	/**
	 * Returns the minimum value supported by the panel.
	 * 
	 * @return the minimum value.
	 */
	public int getMinimum() {
		return min;
	}
	
	/**
	 * Returns the maximum value supported by the panel.
	 * 
	 * @return the maximum value.
	 */
	public int getMaximum() {
		return max;
	}
	
	/**
	 * Returns the current value.
	 * 
	 * @return the current value.
	 */
	public int getValue() {
		return value;
	}
	
	/**
	 * Returns the size of the value change..
	 * 
	 * @return the value of the <code>stepSize</code> property.
	 */
	public int getStepSize() {
		return stepSize;
	}	

	@Override
	public void setEnabled(boolean enabled) {
		slider.setEnabled(enabled);
		spinner.setEnabled(enabled);
	}
	
	/**
	 * Definition in {@link javax.swing.JSlider#setSnapToTicks(boolean)}.
	 */
	public void setSnapToTicks(boolean b) {
		slider.setSnapToTicks(b);
	}
		
	/////////////////////////////////
	
	public void setMajorTickSpacing(int majorTick) {
		slider.setMajorTickSpacing(majorTick);
	}
	
	public void setMinorTickSpacing(int minorTick) {
		slider.setMinorTickSpacing(minorTick);
	}
	
	public void setLabelTable(int labels) {
		slider.setLabelTable(slider.createStandardLabels(labels));
	}
	
	
	// ============================ private methods ==============================
	/**
	 * Creates and locates the slicder and the spinner.
	 */
	private void createPanel() {
		setLayout(new BorderLayout());

		Box labelBox = Box.createHorizontalBox();

		// Label
		JLabel jLabel = new JLabel(label, SwingConstants.LEFT);
		Dimension labelDims = new Dimension((int)(0.75*width), jLabel.getPreferredSize().height);
		jLabel.setSize(labelDims);
		jLabel.setMinimumSize(labelDims);
		jLabel.setPreferredSize(labelDims);
		jLabel.setMaximumSize(labelDims);
		labelBox.add(jLabel);
		labelBox.add(Box.createHorizontalGlue());
		
				
		Box hBox = Box.createHorizontalBox();

		// Slider
		slider = new JSlider(JSlider.HORIZONTAL, min, max, value);
		slider.setFont(new Font("SansSerif", Font.PLAIN, 12));
		slider.setMajorTickSpacing(25);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setSnapToTicks(false);
		slider.setValueIsAdjusting(true);		
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {	
				if (changed) {
					value = Integer.parseInt(spinner.getValue().toString());
					slider.setValue(value);
					changed = false;
				} else {						
					spinner.setValue(slider.getValue());
				}
			}				
		});

		Dimension sliderDims = new Dimension((int)(0.75*width), slider.getPreferredSize().height);
		slider.setSize(sliderDims);
		slider.setMinimumSize(sliderDims);
		slider.setPreferredSize(sliderDims);
		slider.setMaximumSize(sliderDims);
				
		// Spinner
		spinner = new JSpinner(new SpinnerNumberModel(value, min, max, stepSize));
		spinner.setFont(new Font("SansSerif", Font.PLAIN, 12));		
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				changed = true;
				value = Integer.parseInt(spinner.getValue().toString());
				slider.setValue(value);
			}
		});
		
		Dimension spinnerDims = new Dimension((int)(0.25*width), spinner.getPreferredSize().height);
		spinner.setSize(spinnerDims);
		spinner.setMinimumSize(spinnerDims);
		spinner.setPreferredSize(spinnerDims);
		spinner.setMaximumSize(spinnerDims);
				
		
		hBox.setMinimumSize(new Dimension(width, 42));
		hBox.setMaximumSize(new Dimension(width, 42));
		hBox.setPreferredSize(new Dimension(width, 42));
		
		hBox.add(slider);
		hBox.add(spinner, Component.TOP_ALIGNMENT);
		
		// Vertical box to place the label with the slider and the spinner
		Box vBox = Box.createVerticalBox();
		vBox.add(labelBox);
		vBox.add(Box.createVerticalStrut(5));
		vBox.add(hBox);
		
		// Size of JPanel
		Dimension panelDims = new Dimension(width, slider.getPreferredSize().height + jLabel.getPreferredSize().height + 5);
		setSize(panelDims);
		setMinimumSize(panelDims);
		setPreferredSize(panelDims);
		setMaximumSize(panelDims);
		
		add(vBox);	
	}		
		
}