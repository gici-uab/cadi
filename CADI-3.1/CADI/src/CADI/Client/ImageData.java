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
package CADI.Client;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class wraps several image types.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2008/03/22
 */
public class ImageData {
	/**
	 * Is an object used to lock and unlock the access to this class.
	 */
	private ReentrantLock lock = null;
	
	/**
	 * Indicates which is the image data type. Allowed values are:
	 * <lu>
	 * 	<li> {@link #UNDEFINED}
	 * 	<li> {@link #SAMPLES_FLOAT}
	 * 	<li> {@link #BUFFERED}
	 * 	<li> {@link #RASTER}
	 * </lu>
	 */
	private int dataType = UNDEFINED;
	
	/**
	 * Indicates the data type has not defined yet.
	 */
	public static final int UNDEFINED = 0;
	
	/**
	 * The image is represented as a thre-dimensional array of floats. See
	 * {@link #imageSamplesFloat}.
	 */
	public static final int SAMPLES_FLOAT = 1;
	
	/**
	 *Image is represented as a <code>BufferedImage</code>. See
	 *{@link #bufImage}.
	 */
	public static final int BUFFERED = 2;
	
	/**
	 * Image is represented as a <code>Raster</code> image. See
	 * {@link #rasterImage}. 
	 */
	public static final int RASTER = 3;
	
	/**
	 * Image is represented as a thre-dimensional array of floats. Indexes
	 * meaning imageSamples[z][y][x]:<br>
	 * &nbsp; z: image component<br>
	 * &nbsp; y: coordinate in the ordinate axis (y-axis)<br>
	 * &nbsp; x: coordinate in the abcisse axis (x-axis)<br>
	 */
	private float[][][] imageSamplesFloat = null;
		
	/**
	 * Image is represented as a <code>BufferedImage</code>. See
	 * {@link java.awt.image.BufferedImage}.
	 */
	private BufferedImage bufImage = null;
		
	/**
	 * Image is represented as a <code>Raster</code>. See
	 * {@link java.awt.image.Raster}. 
	 */
	private Raster rasterImage = null;
	
	
	// Components
	/**
	 * Is the maximum number of image components
	 */
	private int maxComponents;
	
	/**
	 * Is an one-dimensional array with the indexes of the decompressed
	 * image components.
	 */
	private int[] components;
	
	
	// Frame size and offset
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.ViewWindowField#fsiz}.
	 */
	private int[] fsiz = {0, 0};
	
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.ViewWindowField#roff}.
	 */
	private int[] roff = {0, 0};
	
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.ViewWindowField#rsiz}.
	 */
	private int[] rsiz = {0, 0};
	
	
	// Resolution Levels
	/**
	 * Indicates the maximum resolution level of the image.
	 */
	private int maxResolutionLevels = 0;
	
	/**
	 * Is the resolution level of the decompressed image.
	 */
	private int resolutionLevel = 0;
	
	
	// Quality Layers
	private int maxLayers = 0;
	
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.ViewWindowField#layers}.
	 */
	private int layers = 0;
	
	
	// Quality
	/**
	 * Definition in {@link CADI.Common.Network.JPIP.DataLimitField#quality}.
	 */
	private int quality = 0;
	
	// Precision
	/**
	 * Is the precision, in bits per sample, for each image component.
	 */
	private int[] precision = null;
	
	// ============================= public methods ==============================	
	/**
	 * Constructor.
	 * 
	 * @param dataType definition in {@link #dataType}.
	 */
	public ImageData(int dataType) {
		if ( (dataType < 1) || ( dataType > 3) ) throw new IllegalArgumentException();

		this.dataType = dataType;
		
		 lock =  new ReentrantLock();
	}
	
	/**
	 * Returns the type of the image. See {@link #dataType} attribute.
	 * 
	 * @return the type of the image.
	 */
	public int getType() {
		return dataType;
	}
	
	/**
	 * Sets the {@link #maxComponents} attribute.
	 * 
	 * @param maxComponents definition in {@link #maxComponents}.
	 */	
	public void setMaxComponents(int maxComponents) {
		if (maxComponents < 0) throw new IllegalArgumentException();
		
		this.maxComponents = maxComponents;
	}
	
	/**
	 * Returns the {@link #maxComponents} attribute.
	 *  
	 * @return definition in {@link #maxComponents}.
	 */
	public int getMaxComponents() {
		return maxComponents;
	}
	
	/**
	 * Sets the {@link #components} attribute.
	 * 
	 * @param components definition in {@link #components}.
	 */	
	public void setComponents(int[] components) {
		if (components == null) throw new NullPointerException();
		for (int c = 0; c < components.length; c++) {
			if (components[c] < 0) throw new IllegalArgumentException();
		}
		
		this.components = Arrays.copyOf(components, components.length);
	}
	
	/**
	 * Returns the {@link #components} attribute.
	 * 
	 * @return definition in {@link #components}.
	 */	
	public int[] getComponents() {
		return components;
	}
	
	/**
	 * Sets the {@link #fsiz} atribute.
	 * 
	 * @param width is the width of the frame size.
	 * @param height is the height of the frame size.
	 */	
	public void setFrameSize(int width, int height) {
		if ( (width < 0) || (height < 0) ) throw new IllegalArgumentException();
		fsiz[0] = width;
		fsiz[1] = height;
	}
	/**
	 * Sets the {@link #fsiz} attribute.
	 * 
	 * @param fsiz definition in {@link #fsiz}.
	 */		
	public void setFrameSize(int[] fsiz) {
		if (fsiz == null) throw new NullPointerException();
		if ( (fsiz[0] < 0) || (fsiz[1] < 0) ) throw new IllegalArgumentException();
		
		this.fsiz[0] = fsiz[0];
		this.fsiz[1] = fsiz[1];
	}
	
	/**
	 * Returns the width of the frame size.
	 * 
	 * @return with of the frame size.
	 */
	public int getFrameSizeWidth() {
		return fsiz[0];
	}
	
	/**
	 * Returns the height of the frame size.
	 * 
	 * @return height of the frame size.
	 */	
	public int getFrameSizeHeight() {
		return fsiz[1];
	}
	
	/**
	 * Returns the {@link #fsiz} attribute.
	 * 
	 * @return definition in {@link #fsiz}.
	 */	
	public int[] getFrameSize() {
		return fsiz;
	}
	
	/**
	 * Sets the top-left coordinates of the region. See {@link #roff}
	 * attribute.
	 * 
	 * @param x left coordinate.
	 * @param y top coordinate.
	 */
	public void setRegionOffset(int x, int y) {
		if ( (y < 0) || (y < 0) ) throw new IllegalArgumentException();
		
		roff[0] = x;
		roff[1] = y;
	}
	
	/**
	 * Sets the {@link #roff} parameter.
	 * 
	 * @param roff definition in {@link #roff}
	 */	
	public void setRegionOffset(int[] roff) {
		if (roff == null) throw new NullPointerException();
		if ( (roff[0] < 0) || (roff[1] < 0) ) throw new IllegalArgumentException();
		
		this.roff[0] = roff[0];
		this.roff[1] = roff[1];
	}
	
	/**
	 * Gets the {@link #roff} attribute.
	 * 
	 * @return definition in {@link #roff}.
	 */	
	public int[] getRegionOffset() {
		return roff;
	}
	
	/**
	 * Sets the with and height of the region. See {@link #rsiz}.
	 * 
	 * @param width width of the region.
	 * @param height height of the region.
	 */
	public void setRegionSize(int width, int height) {
		if ( (width < 0) || (height < 0) ) throw new IllegalArgumentException();
		
		rsiz[0] = width;
		rsiz[1] = height;
	}
	
	/**
	 * Sets the {@link #rsiz} attribute.
	 * 
	 * @param rsiz definition in {@link #rsiz}.
	 */	
	public void setRegionSize(int[] rsiz) {
		if (rsiz == null) throw new NullPointerException();
		if ( (rsiz[0] < 0) || (rsiz[1] < 0) ) throw new IllegalArgumentException();
		
		this.rsiz[0] = rsiz[0];
		this.rsiz[1] = rsiz[1];
	}
	
	/**
	 * Gets the {@link #rsiz} attribute.
	 * 
	 * @return definition in {@link #rsiz}.
	 */	
	public int[] getRegionSize() {
		return rsiz;
	}
	
	/**
	 * Sets the {@link #maxResolutionLevels} attribute.
	 * 
	 * @param maxResolutionLevels definition in {@link #maxResolutionLevels}.
	 */	
	public void setMaxResolutionLevel(int maxResolutionLevels) {
		if (maxResolutionLevels < 0) throw new IllegalArgumentException();
		
		this.maxResolutionLevels = maxResolutionLevels;
	}
	
	/**
	 * Gets the {@link #maxResolutionLevels} attribute.
	 * 
	 * @return definition in {@link #maxResolutionLevels}
	 */	
	public int getMaxResolutionLevels() {
		return maxResolutionLevels;
	}
	
	/**
	 * Sets the {@link #resolutionLevel} attribute.
	 * 
	 * @param resolutionLevel definition in {@link #resolutionLevel}.
	 */
	public void setResolutionLevel(int resolutionLevel) {
		if (resolutionLevel < 0) throw new IllegalArgumentException();
		
		this.resolutionLevel = resolutionLevel;
	}
	
	/**
	 * Returns the {@link #resolutionLevel} attribute.
	 * 
	 * @return definition in {@link #resolutionLevel}.
	 */	
	public int getResolutionLevel() {
		return resolutionLevel;
	}
	
	/**
	 * Sets the {@link #maxLayers} attribute.
	 * 
	 * @param maxLayers definition in {@link #maxLayers}.
	 */	
	public void setMaxLayers(int maxLayers) {
		if (maxLayers < 0) throw new IllegalArgumentException();
		
		this.maxLayers = maxLayers;
	}
	
	/**
	 * Gets the {@link #maxLayers} attribute.
	 * 
	 * @return definition in {@link #maxLayers}.
	 */
	public int getMaxLayers() {
		return maxLayers;
	}
	
	/**
	 * Sets the {@link #layers} attribute.
	 * 
	 * @param layers definition in {@link #layers}.
	 */
	public void setLayers(int layers) {
		this.layers = layers;
	}
	
	/**
	 * Gets the {@link #layers} attribute.
	 * 
	 * @return the {@link #layers} attribute.
	 */
	public int getLayers() {
		return layers;
	}
	
	/**
	 * Sets the {@link #quality} attribute.
	 * 
	 * @param quality definition in {@link #quality}.
	 */
	public void setQuality(int quality) {
		this.quality = quality;
	}
	
	/**
	 * Gets the {@link #quality} attribute.
	 * 
	 * @return the {@link #quality} attribute.
	 */
	public int getQuality() {
		return quality;
	}
	
	/**
	 * Sets the {@link #precision} attribute.
	 * 
	 * @param precision definition in {@link #precision}.
	 */
	public void setPrecision(int[] precision) {
		this.precision = precision;
	}
	
	/**
	 * Returns the {@link #precision} attribute.
	 * 
	 * @return the {@link #precision} attribute.
	 */
	public int[] getPrecision() {
		return precision;
	}
	
	/**
	 * Sets the {@link #imageSamplesFloat} attribute.
	 * 
	 * @param imageSamples definition in {@link #imageSamplesFloat}.
	 */
	public void setData(float[][][] imageSamples) {
		if (imageSamples == null) throw new NullPointerException();
		if (dataType != SAMPLES_FLOAT) throw new IllegalArgumentException();
		
		this.imageSamplesFloat = imageSamples;
	}
	
	/**
	 * Sets the {@link #bufImage} attribute.
	 * 
	 * @param bufImage definition in {@link #bufImage}.
	 */
	public void setData(BufferedImage bufImage) {
		if (bufImage == null) throw new NullPointerException();
		if (dataType != BUFFERED) throw new IllegalArgumentException();
		
		this.bufImage = bufImage;
	}
	
	/**
	 * Sets the {@link #rasterImage} attribute.
	 * 
	 * @param rasterImage definition in {@link #rasterImage}.
	 */
	public void setData(Raster rasterImage) {
		if (rasterImage == null) throw new NullPointerException();
		if (dataType != RASTER) throw new IllegalArgumentException();
		
		this.rasterImage = rasterImage;
	}
	
	/**
	 * Get the image samples float ({@link #imageSamplesFloat} attribute).
	 * 
	 * @return the image samples float.
	 * 
	 * @throws IllegalAccessException if the method is called but the data type
	 * 				is not a image of sample floats. See {@link #getType()}
	 * 				method.
	 */
	public float[][][] getSamplesFloat() throws IllegalAccessException {
		
		if (dataType != SAMPLES_FLOAT) throw new IllegalAccessException();
		
		return imageSamplesFloat;
	}
	
	/**
	 * Get the buffered image ({@link #bufImage} attribute).
	 * 
	 * @return the buffered image.
	 * 
	 * @throws IllegalAccessException if the method is called but the data type
	 * 				is not a buffered image. See {@link #getType()} method.
	 */
	public BufferedImage getBufferedImage() throws IllegalAccessException {
		
		if (dataType != BUFFERED) throw new IllegalAccessException();
		
		return bufImage;
	}
	
	/**
	 * Get the rasterimage ({@link #rasterImage} attribute).
	 * 
	 * @return the raster image.
	 * 
	 * @throws IllegalAccessException if the method is called but the data type
	 * 				is not a raster image. See {@link #getType()} method.
	 */
	public Raster getRasterImage() throws IllegalAccessException {
		
		if (dataType != RASTER) throw new IllegalAccessException();
		
		return rasterImage;
	}
	
	/*
	 * @see java.util.concurrent.locks.ReentrantLock#lock()
	 */
	public void lock() {
		lock.lock();
	}
	
	/*
	 * @see java.util.concurrent.locks.ReentrantLock#unlock()
	 */
	public void unlock() {
		lock.unlock();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";

		str += "Not implemented yet";

		str += "]";
		return str;
	}
	
	/**
	 * Prints this Image data fields out to the specified output
	 * stream. This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Image Data --");
		out.println("fsiz: "+fsiz[0]+","+fsiz[1]);
		out.println("roff: "+roff[0]+","+roff[1]);
		out.println("rsiz: "+rsiz[0]+","+rsiz[1]);
		out.println("max. num. comps: "+maxComponents);
		out.println("res. level: "+resolutionLevel+" of "+maxResolutionLevels);

		out.print("precision: ");
    if (precision != null) {
      for (int i = 0; i < precision.length; i++) System.out.print(precision[i]+" "); System.out.println();
    } else  {
      System.out.println(" < not defined >");
    }
		out.println("layers: "+layers+ " of "+maxLayers);
		out.println("quality: "+quality);

						
		out.flush();
	}
		
	// ============================ private methods ==============================	
	
}
