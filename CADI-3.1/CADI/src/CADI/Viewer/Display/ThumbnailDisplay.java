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
package CADI.Viewer.Display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * This class implements a thumbnail displayer. If the size of thumbnail image
 * is greater than the drawing area size, the thumbnail image is fit to the
 * drawing area size. 
 * <p>
 * Usage example:<br>
 * &nbsp; constructor<br>
 * &nbsp; setThumbnail<br>
 * &nbsp; movePanner<br>
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2008/01/16
 */
public class ThumbnailDisplay extends JPanel {

	/**
	 * Reference to the parent window.
	 */
	private JFrame parent = null;
	
	/**
	 * Width of the panel
	 */
	private int width = 0;
	
	/**
	 * Height of the panel
	 */
	private int height = 0;
	
	/**
	 * Is the thumbnail image.
	 */
	private BufferedImage bufferedImage = null;
		

	// INTERNAL ATTRIBUTES

	/**
	 * Are the panner coordinates (origin and size).
	 */
	private Rectangle panner = null;

	/**
	 * Is the color that will be used to display the panner.
	 * 
	 * @see #panner
	 */
	private final Color pannerColor = Color.BLUE;
	
	/**
	 * Insets of the panel.
	 */
	private Insets insets = new Insets(0, 0, 0, 0);
	
	/**
	 * The x coordinate of the first corner of the destination rectangle.
	 * 
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
	 */
	private int dx1 = 0;
	
	/**
	 * The y coordinate of the first corner of the destination rectangle.
	 * 
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver) 
	 */
	private int dy1 = 0;
	
	/**
	 * The x coordinate of the second corner of the destination rectangle.
	 * 
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
	 */
	private int dx2 = 0;
	
	/**
	 * The y coordinate of the second corner of the destination rectangle.
	 * 
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
	 */
	private int dy2 = 0;
	
	/**
	 * The x coordinate of the first corner of the source rectangle.
	 * 
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
	 */
	private int sx1 = 0;
	
	/**
	 * The y coordinate of the first corner of the source rectangle.
	 * 
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
	 */
	private int sy1 = 0;
	
	/**
	 * The x coordinate of the second corner of the source rectangle.
	 * 
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
	 */
	private int sx2 = 0;
	
	/**
	 * The y coordinate of the second corner of the source rectangle.
	 * 
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
	 */
	private int sy2 = 0;

	/**
	 * Indicates whether the thumbnail image is available or not. The image
	 * will be available if the {@link #setThumbnail(BufferedImage)} method
	 * has been called.
	 */
	private boolean setThumnailImage = false;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param parent reference to the parent window.
	 * @param width width of the panel
	 * @param height height of the panel
	 */
	public ThumbnailDisplay(JFrame parent, int width, int height) {
		super();
			
		this.parent = parent;
		this.width = width;
		this.height = height;
									
		createAndShowGUI();
	}
	
	/**
	 * Sets a new thumbnail to be displayed.
	 * 
	 * @param bufferedImage the new thumbnail image.
	 */
	public synchronized void setThumbnail(BufferedImage bufferedImage) {
		
		if (bufferedImage == null) throw new NullPointerException();
		
		this.bufferedImage = bufferedImage;
		
		setThumnailImage = true;
		
		displayImage();
	}
	
	/**
	 * Clears the thumbnail display.
	 */
	public synchronized void clear() {
		bufferedImage = null;
		panner = null;
		displayImage();
	}
	
	/**
	 * Moves the panner window on the thumbnail image to the new position and
	 * size.
	 * <p>
	 * The coordinates which are passed in this method are referred to the
	 * original image. The dimensions of the original image are also passed to
	 * the method. 
	 * 
	 * @param imgWidth width of the original image
	 * @param imgHeight height of the original image
	 * @param x x coordinate in the original image 
	 * @param y y corrdinate in the original image
	 * @param width width in the original image
	 * @param height height in the original image
	 */
	protected synchronized void movePanner(int imgWidth, int imgHeight, int x, int y, int width, int height) {
		
		if ( setThumnailImage ) {
			int imageWidth = getImageSize().width;
			int imageHeight = getImageSize().height;

			double xFactor = (double)imgWidth / (double)imageWidth; 
			double yFactor = (double)imgHeight / (double)imageHeight;

			int newX = (int)(x / xFactor + 0.5);
			int newY = (int)(y / yFactor + 0.5);
			int newWidth = (int)(width / xFactor + 0.5);
			int newHeight = (int)(height / yFactor + 0.5);

			panner = new Rectangle(newX, newY, newWidth-1, newHeight-1);

			repaint();
			
		} else {
			panner = null;
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent(java.awt.Graphics)
	 */
	public synchronized void paintComponent (Graphics g) {
	
		Graphics2D g2D = (Graphics2D) g;
		super.paintComponent (g2D);
		
		// Draw image
		if (bufferedImage != null) {

			int imageWidth = getImageSize().width;
			int imageHeight = getImageSize().height;
			int displayWidth = getDisplaySize().width;
			int displayHeight = getDisplaySize().height;

			if ( (imageWidth <= displayWidth) && (imageHeight <= displayHeight) ) {
				// First case: image size is smaller than window size
				sx1 = sy1 = 0;
				sx2 = imageWidth;
				sy2 = imageHeight;

				dx1 = (displayWidth - imageWidth) / 2 + insets.left;
				dy1 = (displayHeight - imageHeight) / 2 + insets.top;
				dx2 = dx1 + imageWidth;
				dy2 = dy1 + imageHeight;

				g2D.drawImage(bufferedImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this);                    


			} else  {
				// Second case: image size is greater than window size
				// NOTE: this case will never happen because thumbnail view is adjusted to displayer size

				sx1 = sy1 = 0;
				sx2 = imageWidth;
				sy2 = imageHeight;

				dx1 = insets.left;
				dy1 = insets.right;
				dx2 = dx1 + imageWidth;
				dy2 = dy1 + imageHeight;            		               	

				g2D.drawImage(bufferedImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this);                	
			}
			
			// Draw panner
			if (panner != null) {
	        	Color actualColor = g2D.getColor();	// Save the graphics color
	        	g2D.setColor(pannerColor);
	        	g.drawRect(dx1 + panner.x, dy1 + panner.y, panner.width, panner.height);
            g.drawRect(dx1 + panner.x+1, dy1 + panner.y+1, panner.width-2, panner.height-2);
				g2D.setColor(actualColor); // Restores the graphic color			
			}
		}
	
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getSize(java.awt.Dimension)
	 */
	public Dimension getSize() {
		return super.getSize();
	}
	
	/**
	 * Returns the size of the area where thumbnail is displayed.
	 * 
	 * @return size of the displayer area.
	 */
	public Dimension getDisplaySize() {
		Dimension panelSize = getSize();
		return new Dimension(panelSize.width - insets.left - insets.right, panelSize.height - insets.top - insets.bottom);
	}
	
	/**
	 * Returns the image size.
	 * 
	 * @return the size of the image.
	 */
	public Dimension getImageSize() {
		Dimension imageDims = new Dimension(0, 0);
		
		if (bufferedImage != null) {	
			imageDims.width = bufferedImage.getWidth();
			imageDims.height = bufferedImage.getHeight();
		} 
		
		return imageDims;		
	}
	
	// ============================ private methods ==============================
	/** Display the DrawingPanel with BufferedImage on the applet.**/
	private void createAndShowGUI() {

		// Set layout and border
		setLayout(new BorderLayout(0, 0));
		setBorder(BorderFactory.createEtchedBorder());
		
		insets = getInsets();
				
		// Set sizes
		Dimension dims = new Dimension(width, height);
		setSize(dims);
		setPreferredSize(dims);
		setMinimumSize(dims);
		setMaximumSize(dims);

		displayImage();
	}
		
	/**
	 *  Build a BufferedImage from a pixel array.
	 */
	private void displayImage() {

		if (bufferedImage == null) {
			// If image has not been set, display 
			
			int colorModel = BufferedImage.TYPE_INT_RGB;
			
			int imageWidth  = getSize ().width;
			int imageHeight = getSize ().height;
			float[] fPixels = new float [3 * imageWidth * imageHeight];
			
			int i=0;			
			Color background = getBackground();
			int r = background.getRed();
			int g = background.getGreen();
			int b = background.getBlue();
			for  (int y = 0; y < imageHeight; y++){
				for ( int x = 0; x < imageWidth; x++){					
					fPixels[i++] = r;
					fPixels[i++] = g;
					fPixels[i++] = b;
				}
			}
			
			bufferedImage = new BufferedImage (imageWidth, imageHeight, colorModel);
			WritableRaster raster = bufferedImage.getRaster();
			raster.setPixels(0, 0, imageWidth, imageHeight, fPixels);
			
		} else {			
			bufferedImage = createView(bufferedImage);
		}
					
		repaint();
	}
	
	/**
	 * Creates a new view of the raster image that fits to the thumbnail
	 * display window. If the image size is greater than the window size,
	 * image is scaled to fit the window size keeping the aspect ratio.
	 * Otherwise, do nothing.
	 * 
	 * @param bImage image to be scaled.
	 * @return scaled image.
	 */
	private BufferedImage createView(BufferedImage bImage) {
			
		// See this interesting link: http://today.java.net/pub/a/today/2007-2012/04/03/perils-of-image-getscaledinstance.html
		
		int imageWidth = bImage.getWidth();
		int imageHeight = bImage.getHeight();
		int displayWidth = getDisplaySize().width;
		int displayHeight = getDisplaySize().height;
		
		if ( (imageWidth <= displayWidth) && (imageHeight <= displayHeight) ) {
			return bImage;
		}

		double xScale = (double)displayWidth / (double)imageWidth; 
		double yScale = (double)displayHeight / (double)imageHeight;

		double scaleFactor = (xScale <= yScale) ? xScale : yScale;

		AffineTransform tx = new AffineTransform();
		tx.scale(scaleFactor, scaleFactor);

		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);			
		bufferedImage = op.filter(bImage, null);		

		return bufferedImage;
	}

}