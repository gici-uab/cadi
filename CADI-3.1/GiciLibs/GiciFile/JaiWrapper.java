package GiciFile;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import GiciException.WarningException;

/**
 * This is a JAI-dependency-free wrapper for JAI that doesn't require JAI to be installed to
 * compile or run any application using it. Strong use of reflection is required, and code is
 * a little complicated. Equivalent functions are included as comments at the end of this class.
 * 
 * @author ian
 */
public class JaiWrapper {

	static BufferedImage jaiLoad(String imageFile) throws WarningException {
		BufferedImage buffImage = null;
		
		try {
			Method JaiCreate = Class.forName("javax.media.jai.JAI").getMethod("create", String.class, Object.class);
			Method getAsBufferedImage = Class.forName("javax.media.jai.RenderedOp").getMethod("getAsBufferedImage");
			Method dispose = Class.forName("javax.media.jai.RenderedOp").getMethod("dispose");

			//RenderedOp img = JAI.create("FileLoad", imageFile);
			Object img = JaiCreate.invoke(null, "FileLoad", imageFile);

			//BufferedImage buffImage = img.getAsBufferedImage();
			buffImage = (BufferedImage) getAsBufferedImage.invoke(img);
			
			//img.dispose();
			dispose.invoke(img);
		} catch (ClassNotFoundException e){
			throw new Error("Java Advanced Imaging API (JAI) must be installed in order to be able to operate with this specific file format.");
		} catch (InvocationTargetException e) {
			throw new WarningException(e.getTargetException().getMessage());
		} catch (Exception e){
			e.printStackTrace();
			throw new Error("JAI error: " + e.getMessage());
		}

		return buffImage;
	}

	static Raster createRaster (int xSize, int ySize, int zSize, DataBufferFloat dbf) throws WarningException {
		Raster r = null;

		try {
			Method createBandedSampleModel = Class.forName("javax.media.jai.RasterFactory").getMethod("createBandedSampleModel", int.class, int.class, int.class, int.class);
			Method createRaster = Class.forName("javax.media.jai.RasterFactory").getMethod("createRaster", SampleModel.class, DataBuffer.class, Point.class);

			//SampleModel sm = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_FLOAT, xSize, ySize, zSize);
			SampleModel sm = (SampleModel) createBandedSampleModel.invoke(null, DataBuffer.TYPE_FLOAT, xSize, ySize, zSize);
			//Raster r = RasterFactory.createRaster(sm, dbf, new Point(0,0));
			r = (Raster) createRaster.invoke(null, sm, dbf, new Point(0,0));
		} catch (ClassNotFoundException e){
			throw new Error("Java Advanced Imaging API (JAI) must be installed in order to be able to operate with this specific file format.");
		} catch (InvocationTargetException e) {
			throw new WarningException(e.getTargetException().getMessage());
		} catch (Exception e){
			e.printStackTrace();
			throw new Error("JAI error: " + e.getMessage());
		}
		
		return r;
	}
	
	static void jaiSave(BufferedImage buffImage, String imageFile, String format) throws WarningException {
		try {
			Method JaiCreate = Class.forName("javax.media.jai.JAI").getMethod("create", String.class, RenderedImage.class, Object.class, Object.class);
			
			//JAI.create("filestore", buffImage, imageFile, format);
			JaiCreate.invoke(null, "filestore", buffImage, imageFile, format);
		} catch (ClassNotFoundException e){
			throw new Error("Java Advanced Imaging API (JAI) must be installed in order to be able to operate with this specific file format.");
		} catch (InvocationTargetException e) {
			throw new WarningException(e.getTargetException().getMessage());
		} catch (Exception e){
			e.printStackTrace();
			throw new Error("JAI error: " + e.getMessage());
		}
	}
	
	/* Simple functions */
	/*
	static BufferedImage jaiLoad(String imageFile) {
		RenderedOp img = JAI.create("FileLoad", imageFile);
		BufferedImage buffImage = img.getAsBufferedImage();
		img.dispose();
		
		return buffImage;
	}
	
	static Raster createRaster (int xSize, int ySize, int zSize, DataBufferFloat dbf) {
		SampleModel sm = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_FLOAT, xSize, ySize, zSize);
		Raster r = RasterFactory.createRaster(sm, dbf, new Point(0,0));
		
		return r;
	}
	
	static void jaiSave(BufferedImage buffImage, String imageFile, String format) {
		JAI.create("filestore", buffImage, imageFile, format);
	}
	*/
}
