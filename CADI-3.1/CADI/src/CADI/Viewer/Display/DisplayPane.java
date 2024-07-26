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

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import CADI.Client.ImageData;
import CADI.Viewer.Viewer;

/**
 * This class implements a panel where the images will be displayed. When image
 * size is greater than the displayer panel, scrolling is supporting
 * automatically.
 * <p>
 * This class needs an object of the parent because when a region size is
 * selected a new window of interest must be requested. This request is
 * performed through a method of the parent object. 
 * <p>
 * Usage example:<br>
 * &nbsp; constructor<br>
 * &nbsp; setThumbnailDisplayer<br>
 * &nbsp; displayImage<br>
 * &nbsp; clear || setAction || increaseResolutionLevel ||
 * 		decreaseResolutionLevel || increaseQualityLayer || decreaseQualityLayer
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2007-2012/12/15
 */
public class DisplayPane extends JPanel implements MouseListener, MouseMotionListener {

	/**
	 * Reference to the parent object.
	 */
	private JFrame parent = null;

	/**
	 * Width of the panel.
	 */
	private int width = 0;

	/**
	 * Height of the panel.
	 */
	private int height = 0;

	/**
	 * Image to be displayer
	 */
	private Image image;
	// private BufferedImage bufferedImage = null;
	private ImageData imageData = null;

	/**
	 * Frame size
	 */
	private int[] fsiz = {0, 0};
	
	/**
	 * Region offset
	 */
	private int[] roff = {0, 0};
	
	/**
	 * Region size
	 */
	private int[] rsiz = {0, 0};
	
	/**
	 * Reference to the thumbnail display panel. It is used to update the
	 * panner, therefore, only the
	 * {@link CADI.Viewer.Display.ThumbnailDisplay#movePanner(int, int, int, int, int, int)}
	 * method is used.
	 */
	private ThumbnailDisplay thumbnailDisplay = null;

	/**
	 * Indicates the action which is doing on the displayer. The cursor type
	 * (see {@link #cursor}) will change depending of the value of this
	 * attribue. The allowed values are:<br>
	 *  
	 * 
	 */
	private int action = NOTHING;

	/**
	 * 
	 */
	public static int NOTHING = 0;

	/**
	 * 
	 */
	public static int PANNING = 1;

	/**
	 * 
	 */
	public static int SELECTING = 2;

	/**
	 * 
	 */
	public static int WAITING = 3;


	// INTERNAL ATTRIBUTES

	/**
	 * Is a scroll pane where the {@link #displayer} will be placed.
	 */
	private JScrollPane scroller = null;

	/**
	 * Horizontal scroll bar of the scroll pane
	 * 
	 * @see #scroller
	 */
	private JScrollBar horizontalScrollBar = null;

	/**
	 * Vertical scroll bar of the scroll pane
	 * 
	 * @see #scroller
	 */
	private JScrollBar verticalScrollBar = null;

	/**
	 * Is the panel where images will be displayed, i.e. the drawing area.
	 */
	private Displayer displayer = null;

	/**
	 * Indicates the cursor type used into the image area. The allowed values
	 * are: <br>
	 * <tt> Cursor.MOVE_CURSOR </tt> for doing a panning <br>
	 * <tt> Cursor.DEFAULT_CURSOR </tt> for the default cursor <br>
	 * <tt> Cursor.CROSSHAIR_CURSOR </tt> for selecting a image region <br>
	 * <p>
	 * The default cursor value is <tt>Cursor.DEFAULT_CURSOR</tt> 
	 */
	private Cursor cursor = null;

	/**
	 * Contains the rectangle coordinates (upper left corner, width and height)
	 * of the selected area on the Image Display Area.
	 */
	private Rectangle selectionArea = null;

	/**
	 * Indicates the color that will be used when a window of interes is
	 * selected
	 *
	 * @see #selectionArea
	 */
	private final Color selectionAreaColor = Color.BLUE;

	/**
	 * Contains the initial mouse coordinates when a panning is being done.
	 */
	private Point panningOrigin = null;

	/**
	 * Scroll bars listener
	 */
	private ScrollBarsAdjustmentListener listener = null;

	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 * 
	 * @param parent reference to the parent window
	 * @param width width of the panel
	 * @param height height of the panel
	 */
	public DisplayPane(JFrame parent, int width, int height, ImageData imageData){
		super();

		this.parent = parent;
		this.width = width;
		this.height = height;
		this.imageData = imageData;

		setLayout(new BorderLayout(0, 0));

		// Create components
		displayer = new Displayer();        
		displayer.addMouseListener(this);
		displayer.addMouseMotionListener(this);

		// Put the drawing area in a scroll pane.
		scroller = new JScrollPane(displayer);
		scroller.setPreferredSize(new Dimension(0, 0));
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		listener = new ScrollBarsAdjustmentListener();
		scroller.getHorizontalScrollBar().addAdjustmentListener(listener);
	   scroller.getVerticalScrollBar().addAdjustmentListener(listener);		
		horizontalScrollBar = scroller.getHorizontalScrollBar();
		verticalScrollBar = scroller.getVerticalScrollBar();

		// Add componets to panel
		add(scroller, BorderLayout.CENTER);

		// Set size
		Dimension dims = new Dimension(width, height);
		setSize(dims);
		setPreferredSize(dims);
		setMinimumSize(dims);
		setMaximumSize(dims);
		
		// Initializations
		cursor = new Cursor(Cursor.DEFAULT_CURSOR);
	}	

	/**
	 * Sets a reference to the thumbnail display panel.
	 * 
	 * @param thumbnailDisplay reference to the thumbnail display panel.
	 */
	public void setThumbnailDisplayer(ThumbnailDisplay thumbnailDisplay) {
		this.thumbnailDisplay = thumbnailDisplay;
	}
	
	/**
	 * Displays an image in the displayer.
	 * 
	 * @param bufImage the image to be displayed.
	 */
	public synchronized void displayImage(BufferedImage bufImage, int[] fsiz, int[] roff, int[] rsiz) {
		if (bufImage == null) throw new NullPointerException();
		
		this.fsiz[0] = fsiz[0];
		this.fsiz[1] = fsiz[1];
		this.roff[0] = roff[0];
		this.roff[1] = roff[1];
		this.rsiz[0] = rsiz[0];
		this.rsiz[1] = rsiz[1];
		
		displayer.displayImage(bufImage);
		cursor = new Cursor(Cursor.DEFAULT_CURSOR);
		setCursor(cursor);
	}
	
	/**
	 * This method forces to displayed image to be repainted.
	 */
	public synchronized void rePaintImage() {
		if (image != null) displayer.repaint();
	}
	
	/**
	 * Clears the thumbnail display.
	 */
	public synchronized void clear() {
		displayer.clear();
	}

	/**
	 * Set the action which is doing in the display pane. This actions are:
	 * <ul>
	 * <li>panning
	 * <li>selecting
	 * <li>waiting
	 * <li>do nothing
	 * </ul>
	 * 
	 * <p>
	 * Moreover, the cursor type changes according to the action.
	 *  
	 * @param action definition in {@link #action}.
	 * 
	 * @see #action
	 */
	public void setAction(int action) {		

		if (action == NOTHING) {
			cursor = new Cursor(Cursor.DEFAULT_CURSOR);
			setCursor(cursor);
			this.action = NOTHING;

		} else if (action == PANNING) {
			// Image dimensions
			Dimension imageSize = displayer.getImageSize();

			// Displayer size
			Dimension displayerSize = getSize();

			if ( (imageSize.width > displayerSize.width) || (imageSize.height > displayerSize.height) ) {
				cursor = new Cursor(Cursor.MOVE_CURSOR);
				setCursor(cursor);
				this.action = PANNING;
			} else {
				cursor = new Cursor(Cursor.DEFAULT_CURSOR);
				setCursor(cursor);
				this.action = NOTHING;
			}


		} else if (action == SELECTING) {
			cursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
			setCursor(cursor);
			this.action = SELECTING;			

		} else if (action == WAITING) {
			cursor = new Cursor(Cursor.WAIT_CURSOR);
			setCursor(cursor);

		} else {
			throw new IllegalArgumentException();

		}

	}
	
	/**
	 * Increases the frame size.
	 */
	public void increaseResolutionLevel() {
		
		int resolutionLevel = imageData.getResolutionLevel() + 1;
		if ( (resolutionLevel < 0 ) || (resolutionLevel > imageData.getMaxResolutionLevels()) ) return;
		
		int[] fsiz = imageData.getFrameSize();
		int[] roff = imageData.getRegionOffset();
		int[] rsiz = imageData.getRegionSize();
		int[] components = imageData.getComponents();

		
		// I'm supposing than frame size are powers of 2. Therefore, a good solution is to
		// have the image available frame sizes in the ImageData class.
		fsiz[0] *= 2; fsiz[1] *= 2;
		roff[0] *= 2; roff[1] *= 2;
		rsiz[0] *= 2; rsiz[1] *= 2;

		int displayWidth = getSize().width;
		int displayHeight = getSize().height;
		
		// Width
		if (rsiz[0] > displayWidth) {
			rsiz[0] = displayWidth;
		}
		if ( (roff[0] + rsiz[0]) > fsiz[0] ) {
			roff[0] = ((roff[0] - rsiz[0]) >= 0) ? (roff[0] - rsiz[0]) : 0;
			rsiz[0] = ((roff[0] - rsiz[0]) > fsiz[0]) ? (fsiz[0] - roff[0]) : rsiz[0];
		}
		
		// Height
		if (rsiz[1] > displayHeight) {
			rsiz[1] = displayHeight;
		}
		if ( (roff[1] + rsiz[1]) > fsiz[1] ) {
			roff[1] = ((roff[1] - rsiz[1]) >= 0) ? (roff[1] - rsiz[1]) : 0;
			rsiz[1] = ((roff[1] - rsiz[1]) > fsiz[1]) ? (fsiz[1] - roff[1]) : rsiz[1];
		}
				
		((Viewer)parent).getImage(components, resolutionLevel, roff, rsiz, imageData.getLayers(), imageData.getQuality());
	}
	
	/**
	 * Decreases the frame size.
	 */
	public void decreaseResolutionLevel() {
		
		int resolutionLevel = imageData.getResolutionLevel() - 1;
		if ( (resolutionLevel < 0 ) || (resolutionLevel > imageData.getMaxResolutionLevels()) ) return;

		int[] fsiz = imageData.getFrameSize();
		int[] roff = imageData.getRegionOffset();
		int[] rsiz = imageData.getRegionSize();
		
		// I'm suppossing than frame size are powers of 2. Therefore, a good solution is to
		// have the image available frame sizes in the ImageData class.
		fsiz[0] /= 2; fsiz[1] /= 2;
		roff[0] /= 2; roff[1] /= 2;
		rsiz[0] /= 2; rsiz[1] /= 2;
				
		int displayWidth = getSize().width;
		int displayHeight = getSize().height;
				
		// Width
		if (rsiz[0] < displayWidth) {
			rsiz[0] = displayWidth;
		}
		if ( (roff[0] + rsiz[0]) > fsiz[0] ) {
			roff[0] = ((roff[0] - rsiz[0]) >= 0) ? (roff[0] - rsiz[0]) : 0;
			rsiz[0] = ((roff[0] - rsiz[0]) > fsiz[0]) ? (fsiz[0] - roff[0]) : rsiz[0];
		}
				
		// Height
		if (rsiz[1] < displayHeight) {
			rsiz[1] = displayHeight;
		}
		if ( (roff[1] + rsiz[1]) > fsiz[1] ) {
			roff[1] = ((roff[1] - rsiz[1]) >= 0) ? (roff[1] - rsiz[1]) : 0;
			rsiz[1] = ((roff[1] - rsiz[1]) > fsiz[1]) ? (fsiz[1] - roff[1]) : rsiz[1];
		}	

		((Viewer)parent).getImage(imageData.getComponents(), resolutionLevel, roff, rsiz, imageData.getLayers(), imageData.getQuality());	
	}
	
	/**
	 * Gets a new quality layer to improve the quality of the recovered image..
	 */
	public void increaseQualityLayer() {
		int layer = imageData.getLayers() + 1;
		if ( (layer < 0 ) || (layer > imageData.getMaxLayers()) ) return;

		((Viewer)parent).getImage(imageData.getComponents(), imageData.getResolutionLevel(), roff, rsiz, layer, imageData.getQuality());	
	}
	
	/**
	 * Reduce the quality layer reducing the of the recovered image.
	 */
	public void decreaseQualityLayer() {
		int layer = imageData.getLayers() - 1;
		if ( (layer < 0 ) || (layer > imageData.getMaxLayers()) ) return;

		((Viewer)parent).getImage(imageData.getComponents(), imageData.getResolutionLevel(), roff, rsiz, layer, imageData.getQuality());
	}	

	/**
	 * Returns the current size of the image scroll pane's view port (the size
	 * of the image display area)
	 *
	 * @return The size of the image display area
	 */
	public Dimension getViewportSize() {
		return getSize();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {

		if ( SwingUtilities.isLeftMouseButton(e) ) {            

		} else {        	

		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		setCursor(cursor);		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {

		if ( SwingUtilities.isLeftMouseButton(e) ) {

			if (action == SELECTING) {				
				// Mouse position
				Point pos = e.getPoint();

				// Get the offset on the displayer. 
				Point offset = displayer.getDisplayOffset();

				// Correct pos with the display offset
				pos.x -= offset.x;
				pos.y -= offset.y;

				// Image dimensions
				Dimension imageSize = displayer.getImageSize();

				// Creates the selection area
				if ( (pos.x >= 0) && (pos.y >= 0) && (pos.x < imageSize.width) && (pos.y < imageSize.height) ) {
					selectionArea = new Rectangle(pos.x, pos.y, 1, 1);
				}

			} else if (action == PANNING) {				
				// Image dimensions
				Dimension imageSize = displayer.getImageSize();

				// Displayer size
				Dimension displayerSize = getSize();

				if ( (imageSize.width > displayerSize.width) || (imageSize.height > displayerSize.height) ) {
					panningOrigin =  e.getPoint();					
				}				
			}
		}

	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		if (action == SELECTING) {
			// Proccess the requested area				
			if (selectionArea == null)  return;

			// Get roff and rsiz from selection area			
			int[] regionOffset = {selectionArea.x + roff[0], selectionArea.y + roff[1]};
			int[] regionSize = {selectionArea.width, selectionArea.height};						

			// Invoke the parent's frame method to get the WOI
			//((Viewer)parent).getImage(regionOffset, regionSize);
			((Viewer)parent).getImage(imageData.getComponents(), imageData.getResolutionLevel(), regionOffset, regionSize, imageData.getLayers(), imageData.getQuality());
			
			selectionArea = null;		

		} else  if (action == PANNING) {
			if (panningOrigin != null) {
				Point pos = e.getPoint();

				int xMoved = pos.x - panningOrigin.x;
				int yMoved = pos.y - panningOrigin.y;

				int hsValue = scroller.getHorizontalScrollBar().getValue();
				scroller.getHorizontalScrollBar().setValue(hsValue - xMoved);

				int vsValue = scroller.getVerticalScrollBar().getValue();
				scroller.getVerticalScrollBar().setValue(vsValue - yMoved);
				
				
				boolean requestData = false;
				
				int hsbValue = horizontalScrollBar.getValue();
            	int visibleWidth = horizontalScrollBar.getVisibleAmount();
            	int roff[] = imageData.getRegionOffset();
        		int rsiz[] = imageData.getRegionSize();
            	
            	if ( (hsbValue < roff[0]) || ( (hsbValue+visibleWidth) > (roff[0]+rsiz[0]) ) ) {            		
            		roff[0] = hsbValue;
            		rsiz[0] = visibleWidth;
            		requestData = true;
            	}      
				
            	
            	int vsbValue = verticalScrollBar.getValue();
            	int visibleHeight = verticalScrollBar.getVisibleAmount();
            	
            	if ( (vsbValue < roff[0]) || ( (vsbValue+visibleHeight) > (roff[0]+rsiz[0]) ) ) {
            		roff[1] = vsbValue;
            		rsiz[1] = visibleHeight;
            		requestData = true;
            	}
            	
            	if ( requestData ) {
            		((Viewer)parent).getImage(imageData.getComponents(), imageData.getResolutionLevel(), roff, rsiz, imageData.getLayers(), imageData.getQuality());
            	} else {				
            		displayer.repaint();
            	}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {

		if ( SwingUtilities.isLeftMouseButton(e) ) {            
			if (action == SELECTING) {
				// Mouse position
				Point pos = e.getPoint();

				// Get the offset on the displayer. 
				Point offset = displayer.getDisplayOffset();

				// Correct pos with the display offset
				pos.x -= offset.x;
				pos.y -= offset.y;
				pos.x = pos.x < 0 ? 0 : pos.x;
				pos.y = pos.y < 0 ? 0 : pos.y;

				// Image dimensions
				Dimension imageSize = displayer.getImageSize();
				pos.x = pos.x > imageSize.width ? imageSize.width - 1 : pos.x;
				pos.y = pos.y > imageSize.height ? imageSize.height - 1 : pos.y;

				// Adjust selection origin and width. It takes into account if the selection is done
				// from the top-left corner to the right-bottom or vice versa.
				selectionArea.width = pos.x > selectionArea.x ? pos.x - selectionArea.x : selectionArea.x + selectionArea.width - pos.x;
				selectionArea.height = pos.y > selectionArea.y ? pos.y - selectionArea.y : selectionArea.y + selectionArea.height - pos.y;
				selectionArea.x = pos.x > selectionArea.x ? selectionArea.x : pos.x;
				selectionArea.y = pos.y > selectionArea.y ? selectionArea.y : pos.y;

				// Force to paint the screen
				displayer.repaint();

			} else if (action == PANNING) {

			}
		}		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {	
	}

	
	// ======================== Adjustement listener class =======================
	/**
	 * This class is used to manage the scroll bars adjustment listener.
	 * Therefore is is called whenever the value of a scrollbar is changed,
     * either by the user or programmatically.
	 */
	class ScrollBarsAdjustmentListener implements AdjustmentListener {
		
       /*
        * (non-Javadoc)
        * @see java.awt.event.AdjustmentListener#adjustmentValueChanged(java.awt.event.AdjustmentEvent)
        */ 
        public void adjustmentValueChanged(AdjustmentEvent evt) {
            Adjustable source = evt.getAdjustable();
            
            if (evt.getValueIsAdjusting()) { // user is currently dragging the scrollbar's knob
                        	            	
            	if (evt.getAdjustmentType() != AdjustmentEvent.TRACK) return;
            	
            	int regionOffset[] = imageData.getRegionOffset();
        		int regionSize[] = imageData.getRegionSize();
            	
            	int orient = source.getOrientation();            	
                if (orient == Adjustable.HORIZONTAL) { // Event from horizontal scrollbar
                	
                	int pos = horizontalScrollBar.getValue();
                	int visibleWidth = horizontalScrollBar.getVisibleAmount();
                	
                	// Check if the displayer region is a image region with data
                	if ( (pos < regionOffset[0]) || ( (pos+visibleWidth) > (regionOffset[0]+regionSize[0]) ) ) {
                		
                		regionOffset[0] = pos;
                		regionSize[0] = visibleWidth;                		
                		((Viewer)parent).getImage(imageData.getComponents(), imageData.getResolutionLevel(), regionOffset, regionSize, imageData.getLayers(), imageData.getQuality());	
                	}      	
                	                	
                } else { // Event from vertical scrollbar
                	
                	int pos = verticalScrollBar.getValue();
                	int visibleHeight = verticalScrollBar.getVisibleAmount();

                	// Check if the displayer region is a image region with data
                	if ( (pos < regionOffset[0]) || ( (pos+visibleHeight) > (regionOffset[0]+regionSize[0]) ) ) {
                		regionOffset[1] = pos;
                		regionSize[1] = visibleHeight;                		
                		((Viewer)parent).getImage(imageData.getComponents(), imageData.getResolutionLevel(), regionOffset, regionSize, imageData.getLayers(), imageData.getQuality());	
                	}
                }
            }
        }
    } // class ScrollBarsAdjustmentListener

	
  // ============================= Displayer class =============================
	/**
	 * This class implements the drawing area where image will be displayed. It
	 * only performs the drawing of the image on the panel and managing of the
	 * tool tip of the image pixels.
	 * <p>
	 * Events are not captured because they are grouped in the
	 * {@link CADI.Viewer.Display.DisplayPane} class.
	 */
	class Displayer extends JPanel {

		/**
		 * The x coordinate of the first corner of the destination rectangle.
		 * 
		 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
		 */
		int dx1 = 0;

		/**
		 * The y coordinate of the first corner of the destination rectangle.
		 * 
		 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver) 
		 */
		int dy1 = 0;

		/**
		 * The x coordinate of the second corner of the destination rectangle.
		 * 
		 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
		 */
		int dx2 = 0;

		/**
		 * The y coordinate of the second corner of the destination rectangle.
		 * 
		 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
		 */
		int dy2 = 0;

		/**
		 * The x coordinate of the first corner of the source rectangle.
		 * 
		 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
		 */
		int sx1 = 0;

		/**
		 * The y coordinate of the first corner of the source rectangle.
		 * 
		 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
		 */
		int sy1 = 0;

		/**
		 * The x coordinate of the second corner of the source rectangle.
		 * 
		 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
		 */
		int sx2 = 0;

		/**
		 * The y coordinate of the second corner of the source rectangle.
		 * 
		 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
		 */
		int sy2 = 0;


		/**
		 * Constructor
		 */
		private Displayer() {
			super();

			setLayout(new BorderLayout(0, 0));
			setOpaque(true);
			setDoubleBuffered(true);
			setToolTipText("");
		}

		/**
		 * Paint the image in the screen
		 * @param bufImage Image read it in a BufferedImage
		 */
		public synchronized void displayImage(BufferedImage bufImage){
			image = (Image) bufImage;
			repaint();
		}

		/**
		 * Clears the thumbnail display.
		 */
		public synchronized void clear() {
			image = null;
			repaint();
		}	

		/**
		 * Paint the image with DoubleBuffering system
		 * @param g2 Graphics2D
		 */
		public void paintInOffScreen(Graphics2D g2) {

			g2.setColor(this.getBackground());
			Color color = Color.black;
			g2.drawImage(image, 0, 0, color, this);
			g2.dispose();			
		}	

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		public synchronized void paintComponent(Graphics g){
			super.paintComponent(g);
			Graphics2D g2D = (Graphics2D)g; 	
			
			if (image != null){
				Graphics2D offG = (Graphics2D)image.getGraphics();
				paintInOffScreen(offG);
				Color color = Color.black;

				int regionWidth = rsiz[0];
				int regionHeight = rsiz[1];
				int imageWidth = fsiz[0];
				int imageHeight = fsiz[1];
				int displayWidth = DisplayPane.this.getSize().width;
				int displayHeight = DisplayPane.this.getSize().height;

				// Update client's preferred size. because the area taken up by the graphics has
				// gotten larger or smaller.
				displayer.setPreferredSize(new Dimension(imageWidth, imageHeight));

				//Let the scroll pane know to update itself and its scrollbars.
				revalidate();

				if ( (imageWidth <= displayWidth) && (imageHeight <= displayHeight) ) {
					// Image size is smaller than window size: it is located in the center of the displayer

					sx1 = sy1 = 0;
					sx2 = regionWidth;
					sy2 = regionHeight;

					dx1 = (displayWidth - regionWidth) / 2;
					dy1 = (displayHeight - regionHeight) / 2;
					dx2 = dx1 + regionWidth;
					dy2 = dy1 + regionHeight;

					g2D.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, color, this);                    

				} else  {
					// Image size is greater than window size

					sx1 = sy1 = 0;
					sx2 = regionWidth;
					sy2 = regionHeight;

					dx1 = roff[0];
					dy1 = roff[1];
					dx2 = dx1 + regionWidth;
					dy2 = dy1 + regionHeight;            		               	

					g2D.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, color, this);
				}

				// Paints the selection area
				if ( selectionArea != null) {
					Color actualColor = g2D.getColor();	// Save the graphics color
					g2D.setColor(selectionAreaColor);
					g2D.drawRect(dx1 + selectionArea.x, dy1 + selectionArea.y, selectionArea.width, selectionArea.height);
					g2D.setColor(actualColor); // Restores the graphic color

				}

				// Updates thumbnail display's panner
				if (thumbnailDisplay != null) {
					int x, y, width, height;

					// Width
					if ( horizontalScrollBar.isVisible() ) {
						x = horizontalScrollBar.getValue();
						width = horizontalScrollBar.getVisibleAmount();
					} else {
						x = roff[0];
						width = rsiz[0];
					}

					// Height
					if ( verticalScrollBar.isVisible() ) {
						y = verticalScrollBar.getValue();
						height = verticalScrollBar.getVisibleAmount();     
					} else {
						y = roff[1];
						height = rsiz[1];
					}

					thumbnailDisplay.movePanner(fsiz[0], fsiz[1], x, y, width, height);
				}
			}
		}

		/**
		 * Returns the offset within the displayer.
		 * 
		 * @return the offset within the displayer.
		 */
		public Point getDisplayOffset() {
			return new Point(dx1, dy1);
		}

		/**
		 * Gets the image size.
		 * 
		 * @return image size.
		 */
		public Dimension getImageSize() {
			Dimension dim = new Dimension(0, 0);
			if (image != null) {
				dim.width = image.getWidth(null);
				dim.height = image.getHeight(null);
			}
			return dim;
		}

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#getToolTipLocation(MouseEvent)
		 */
		public Point getToolTipLocation(MouseEvent e) {
			return new Point(e.getPoint());
		}	

		/* (non-Javadoc) 
		 * @see javax.swing.JComponent#getToolTipText(MouseEvent)
		 */
		public String getToolTipText(MouseEvent e){

			String tipText = null;

			if (action == NOTHING) {
				// Mouse position
				Point pos = e.getPoint();

				// Get the offset on the displayer. 
				Point offset = getDisplayOffset();

				// Correct pos with the display offset
				pos.x -= offset.x;
				pos.y -= offset.y;

				// Image dimensions
				Dimension imageSize = getImageSize();

				//Get the pixel color

				if ( (pos.x >= 0) && (pos.y >= 0) && (pos.x < imageSize.width) && (pos.y < imageSize.height) ) {

					BufferedImage bufImage = (BufferedImage) image;

					int value = bufImage.getRGB((int)pos.x, (int)pos.y);
					int r = (value & 0x00ff0000) >> 16;
				int g = (value & 0x0000ff00) >> 8;
				int b =  value & 0x000000ff;

				tipText = "<html><b>  Pixel:</b> ("+pos.x+","+pos.y+")<br><b>  Value:</b> ("+r+","+g+","+b+")</html>";
				}
			}
			return tipText;
		}

	} // class Displayer

}
