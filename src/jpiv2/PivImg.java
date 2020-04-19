/*
 * PivImg.java
 *
 * Copyright 2008 Peter Vennemann
 * 
 * This file is part of JPIV.
 *
 * JPIV is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPIV is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPIV.  If not, see <http://www.gnu.org/licenses/>. 
 */

package jpiv2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.media.jai.BorderExtender;
import javax.media.jai.DataBufferFloat;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.RenderedImageAdapter;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;
import javax.media.jai.WarpAffine;

/**
 * A double frame pixel image representation. This class provides methods for
 * reading, writing and processing double frame pixel images. The pixel data is
 * by default converted to TYPE_USHORT, regardless of the original data type. This is at
 * cost of performance, but it guarantees high accuracy and decreases the danger
 * of data overflow. The type can be set by the variable 
 * 
 */
public class PivImg {

	/**
	 * Creates a new instance of PivImg.
	 * 
	 * @param jpiv
	 *            The parent component.
	 * @param pathname
	 *            The complete pathname of a pixel image.
	 */
	public PivImg(jpiv2.JPiv jpiv, String pathname) {
		this.jpiv = jpiv;
		this.pathname = pathname;
		pi = getPlanarImage(pathname);
	}

	/**
	 * Creates a new instance of PivImg by combining two single image files to a
	 * double frame image.
	 * 
	 * @param jpiv
	 *            The parent component.
	 * @param pathnameA
	 *            The complete pathname of the upper pixel image.
	 * @param pathnameB
	 *            The complete pathname of the lower pixel image.
	 */
	public PivImg(jpiv2.JPiv jpiv, String pathnameA, String pathnameB) {
		this.jpiv = jpiv;
		PlanarImage piA = getPlanarImage(pathnameA);
		PlanarImage piB = getPlanarImage(pathnameB);
		// translation
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(piB);
		pb.add(0.0f);
		pb.add((float) piA.getHeight());
		pb.add(new InterpolationNearest());
		piB = JAI.create("translate", pb, null);
		// mosaic
		pb = new ParameterBlock();
		pb.addSource(piA);
		pb.addSource(piB);
		pi = JAI.create("mosaic", pb);
	}

	/**
	 * Creates a new instance of PivImg by applying an operation to a file list.
	 * 
	 * @param jpiv
	 * 				The parent component.
	 * @param filelist
	 * 				A list of pixel image pathnames.
	 * @param op
	 * 				Either PivImg.MIN, PivImg.SUM, or PivImg.RM_SLIDING_BACKGRD.
	 */
	public PivImg(jpiv2.JPiv jpiv, String[] filelist, int op) {
		this.jpiv = jpiv;
		PlanarImage pi = new PivImg(jpiv, filelist[0]).getAsPlanarImage();
		ParameterBlock pb = new ParameterBlock();
		switch (op) {
			case jpiv2.PivImg.SUM: {
				for (int f = 1; f < filelist.length; f++) {
					pb.removeSources();
					pb.addSource(pi);
					pb.addSource(new PivImg(jpiv, filelist[f]).getAsPlanarImage());
					pi = JAI.create("add", pb);
				}
				break;
			}
			case jpiv2.PivImg.MIN: {
				for (int f = 1; f < filelist.length; f++) {
					pb.removeSources();
					pb.addSource(pi);
					pb.addSource(new PivImg(jpiv, filelist[f]).getAsPlanarImage());
					pi = JAI.create("min", pb);
				}
				break;
			}
			case jpiv2.PivImg.RM_SLIDING_BACKGRD: {
				pb.removeSources();
				pb.addSource(pi);
				pb.addSource(new PivImg(jpiv, filelist, jpiv2.PivImg.MIN).getAsPlanarImage());
				pi = JAI.create("subtract", pb);
				break;
			}
		}
		this.pi = pi;
	}
	
	/**
	 * Compose a new image with reduced static background.
	 * The minimal grey value of an image series is subtracted from the given image.
	 * 
	 * @param jpiv
	 * 				The parent component.
	 * @param filelist
	 * 				A list of pixel image pathnames.
	 * @param file
	 * 				The file to subtract the background from.
	 * @param pathname
	 * 				Destination path for the new image.
	 */
	

	/**
	 * Get the filename of this image.
	 * 
	 * @return The name of this file, not the path.
	 */
	public String getFilename() {
		return (pathname
				.substring(pathname.lastIndexOf(jpiv.fileSeparator) + 1));
	}

	/**
	 * Get the image as a BufferedImage.
	 * 
	 * @return A BufferedImage.
	 */
	public BufferedImage getAsBufferedImage() {
		return (pi.getAsBufferedImage());
	}
	
	/**
	 * Get the image as a PlanarImage.
	 * 
	 * @return A PlanarImage.
	 */
	public PlanarImage getAsPlanarImage() {
		return(this.pi);
	}
	
	/**
	 * Get the image as a PlanarImage TYPE_FLOAT.
	 * 
	 * @return A PlanarImage.
	 */
	public PlanarImage getAsPlanarImageFloat() {
		// conversion to float
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(pi);
		pb.add(DataBuffer.TYPE_FLOAT);
		PlanarImage floatImg = JAI.create("Format", pb);
		return (floatImg);
	}

	/**
	 * Get the image as a PlanarImage of 8 bit greyscale resolution.
	 * 
	 * @return The normalized 8 bit PlanarImage.
	 * @param zoom
	 *            The magnification factor of the image.
	 * @param range
	 *            The image is normalized on this greyscale band. Possible
	 *            values are defined by the following constants: <br>
	 *            R00064 = 6 bit <br>
	 *            R00128 = 7 bit <br>
	 *            R00256 = 8 bit <br>
	 *            R00512 = 9 bit <br>
	 *            R01024 = 10 bit <br>
	 *            R02048 = 11 bit <br>
	 *            R04096 = 12 bit <br>
	 *            R08192 = 13 bit <br>
	 *            R16384 = 14 bit <br>
	 *            R32768 = 15 bit <br>
	 *            R65536 = 16 bit <br>
	 *            RMIN_MAX = normalize between minimum and maximum;
	 */
	public PlanarImage getAsPlanarImageByte(int range, float zoom) {
		PlanarImage img;
		// rescale image
		double scales[][] = new double[2][1];
		scales[1][0] = 0;
		switch (range) {
		case R00064: {
			scales[0][0] = 255D / 63D;
			break;
		}
		case R00128: {
			scales[0][0] = 255D / 127D;
			break;
		}
		case R00256: {
			scales[0][0] = 1;
			break;
		}
		case R00512: {
			scales[0][0] = 255D / 511D;
			break;
		}
		case R01024: {
			scales[0][0] = 255D / 1023D;
			break;
		}
		case R02048: {
			scales[0][0] = 255D / 2049D;
			break;
		}
		case R04096: {
			scales[0][0] = 255D / 4095D;
			break;
		}
		case R08192: {
			scales[0][0] = 255D / 8191D;
			break;
		}
		case R16384: {
			scales[0][0] = 255D / 16383D;
			break;
		}
		case R32768: {
			scales[0][0] = 255D / 32767D;
			break;
		}
		case R65536: {
			scales[0][0] = 255D / 65535D;
			break;
		}
		default:
			RES_MIN_MAX: {
				scales = getNormalizingScales(pi, 255);
			}
		}
		img = rescaleAmplitude(pi, scales);
		// convert to TYPE_BYTE
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(img);
		pb.add(DataBuffer.TYPE_BYTE);
		img = JAI.create("format", pb);
		// zoom
		return (scale(img, zoom));
	}

	/**
	 * Get the greyvalue at a certain position.
	 * 
	 * @param x
	 *            The x coordinate (zero top left).
	 * @param y
	 *            The y coordinate (zero top left).
	 * @return The greyvalue.
	 */
	public float getIntensityAt(int x, int y) {
		if (x < pi.getWidth() && y < pi.getHeight()) {
			return (pi.getData().getSampleFloat(x, y, 0));
		} else {
			return (-1F);
		}
	}

	/**
	 * Normalize the image.
	 * 
	 * @param dynRange
	 *            The maximum value of the normalized image.
	 */
	public void normalize(float dynRange) {
		pi = rescaleAmplitude(pi, getNormalizingScales(pi, dynRange));
	}

	/**
	 * Get the widht of the image.
	 * 
	 * @return The width of the image in pixel.
	 */
	public int getWidth() {
		return (pi.getWidth());
	}

	/**
	 * Get height of the image.
	 * 
	 * @return The height of the image in pixel. If the image consists of two
	 *         frames, this is the height of a frame times two.
	 */
	public int getHeight() {
		return (pi.getHeight());
	}

	/**
	 * Returns the pixels for a specified rectangle in a float array, one
	 * greyvalue per array element.
	 * 
	 * @param x
	 *            The x-coordinate of the upper-left pixel location.
	 * @param y
	 *            The y-coordinate of the upper-left pixel location.
	 * @param w
	 *            Width of the pixel rectangle.
	 * @param h
	 *            Height of the pixel rectangle.
	 * @return The greyvalues for the specified rectangle of pixels
	 *         (float[height][width]).
	 * @param frame
	 *            Specifies the first or second frame in case of a double frame
	 *            image.
	 */
	public float[][] getDataFloat(int x, int y, int w, int h, int frame) {
		float[] fArray = new float[w * h];
		float[][] imgArray = new float[h][w];
		PlanarImage subImg = getSubImage(x, y, w, h, frame);
		fArray = subImg.getData().getSamples(0, 0, w, h, 0, fArray);
		for (int i = 0; i < h; i++) {
			System.arraycopy(fArray, i * w, imgArray[i], 0, w);
		}
		return (imgArray);
	}

	/**
	 * Get a region of the image
	 * 
	 * @param x
	 *            Horizontal origin of the sub-image.
	 * @param y
	 *            Vertical origin of the sub-image.
	 * @param dx
	 *            Width of the sub-image.
	 * @param dy
	 *            Height of the sub-image.
	 * @param frame
	 *            First frame (0) or second frame (1).
	 * @return The sub-image.
	 * @throws java.lang.IllegalArgumentException
	 *             if the region specified is outside the image.
	 */
	public PlanarImage getSubImage(int x, int y, int dx, int dy, int frame)
			throws java.lang.IllegalArgumentException {
		if (frame == 1)
			y += pi.getHeight() / 2;
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(pi);
		pb.add((float) x);
		pb.add((float) y);
		pb.add((float) dx);
		pb.add((float) dy);
		PlanarImage subImg = JAI.create("crop", pb);
		pb.removeSources();
		pb.removeParameters();
		pb.addSource(subImg);
		pb.add((float) -x);
		pb.add((float) -y);
		pb.add(null);
		return (JAI.create("translate", pb));
	}

	/**
	 * Get a region of the image (interpolated).
	 * 
	 * @param x
	 *            Horizontal origin of the sub-image.
	 * @param y
	 *            Vertical origin of the sub-image.
	 * @param dx
	 *            Width of the sub-image.
	 * @param dy
	 *            Height of the sub-image.
	 * @param frame
	 *            First frame (0) or second frame (1).
	 * @return The sub-image.
	 * @throws java.lang.IllegalArgumentException
	 *             if the region specified is outside the image.
	 */
	/*public PlanarImage getSubImage(float x, float y, int dx, int dy, int frame)
			throws java.lang.IllegalArgumentException {
		if (frame == 1)
			y += ((float) pi.getHeight()) / 2;
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(pi);
		pb.add(-x);
		pb.add(-y);
		pb.add(Interpolation.getInstance(Interpolation.INTERP_BILINEAR));
		PlanarImage transImg = JAI.create("translate", pb);
		pb.removeSources();
		pb.removeParameters();
		pb.addSource(transImg);
		pb.add((float) 0.0);
		pb.add((float) 0.0);
		pb.add((float) dx);
		pb.add((float) dy);
		return (JAI.create("crop", pb));
	}*/
	public PlanarImage getSubImage(float x, float y, int dx, int dy, int frame)
			throws java.lang.IllegalArgumentException {
		if (frame == 1)
			y += ((float) pi.getHeight()) / 2;
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(pi);
		pb.add((float) x);
		pb.add((float) y);
		pb.add((float) dx);
		pb.add((float) dy);
		PlanarImage croppedImg = JAI.create("crop", pb);
		pb.removeSources();
		pb.removeParameters();
		pb.addSource(croppedImg);
		pb.add(-x);
		pb.add(-y);
		pb.add(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
		return (JAI.create("translate", pb));
	}
	/**
	 * Get a region of the image.
	 * 
	 * @param x
	 *            Horizontal origin of the sub-image.
	 * @param y
	 *            Vertical origin of the sub-image.
	 * @param dx
	 *            Width of the sub-image.
	 * @param dy
	 *            Height of the sub-image.
	 * @param frame
	 *            First frame (0) or second frame (1).
	 * @param xShear
	 *            Horizontal shear value (which is basically the tangens of the
	 *            shear-angle).
	 * @param yShear
	 *            Vertical shear value.
	 * @return The sub-image.
	 * @throws java.lang.IllegalArgumentException
	 *             if the region specified is outside the image.
	 */
	public PlanarImage getSubImage(float x, float y, int dx, int dy,
			float xShear, float yShear, int frame)
			throws java.lang.IllegalArgumentException {
		if (frame == 1)
			y += pi.getHeight() / 2;
		// shift image
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(pi);
		pb.add(-x);
		pb.add(-y);
		pb.add(Interpolation.getInstance(Interpolation.INTERP_BILINEAR));
		PlanarImage transImg = JAI.create("translate", pb);
		// get the sub-image
		pb = new ParameterBlock();
		pb.addSource(transImg);
		pb.add((float) 0.0);
		pb.add((float) 0.0);
		pb.add((float) dx);
		pb.add((float) dy);
		PlanarImage subImg = JAI.create("crop", pb);
		// move the origin to the center of the image
		pb = new java.awt.image.renderable.ParameterBlock();
		pb.addSource(subImg);
		pb.add((float) (-subImg.getMinX() - subImg.getWidth() / 2));
		pb.add((float) (-subImg.getMinY() - subImg.getHeight() / 2));
		subImg = JAI.create("translate", pb, null);
		// specify the type of interpolation for the shear operation.
		InterpolationBilinear interp = new InterpolationBilinear(16);
		// create the transform parameter
		double m00 = 1; // scale x
		double m10 = -yShear; // shear y
		double m01 = -xShear; // shear x
		double m11 = 1; // scale y
		double m02 = 0; // translation in x direction
		double m12 = 0; // translation in y direction
		AffineTransform transform = new AffineTransform(m00, m10, m01, m11,
				m02, m12);
		WarpAffine warp = new WarpAffine(transform);
		pb = new java.awt.image.renderable.ParameterBlock();
		pb.addSource(subImg);
		pb.add(warp);
		pb.add(interp);
		subImg = JAI.create("warp", pb);
		// border extension, if necessary
		int dw = dx - subImg.getWidth();
		int dh = dy - subImg.getHeight();
		if (dw >= 0 || dh >= 0) {
			if (dw < 0)
				dw = 0;
			if (dh < 0)
				dh = 0;
			pb = new java.awt.image.renderable.ParameterBlock();
			pb.addSource(subImg);
			pb.add((int) Math.ceil(dw / 2f) + 1);
			pb.add((int) Math.ceil(dw / 2f) + 1);
			pb.add((int) Math.ceil(dh / 2f) + 1);
			pb.add((int) Math.ceil(dh / 2f) + 1);
			pb.add(BorderExtender.createInstance(BorderExtender.BORDER_ZERO));
			subImg = JAI.create("border", pb);
		}
		// crop image
		pb = new ParameterBlock();
		pb.addSource(subImg);
		pb.add(-(float) dx / 2);
		pb.add(-(float) dy / 2);
		pb.add((float) dx);
		pb.add((float) dy);
		subImg = JAI.create("crop", pb);
		// move origin back to first top left pixel
		pb = new ParameterBlock();
		pb.addSource(subImg);
		pb.add(-(float) subImg.getMinX());
		pb.add(-(float) subImg.getMinY());
		pb.add(null);
		subImg = JAI.create("translate", pb);
		return (subImg);
	}

	/**
	 * Get minimum and maximum greyvalue of an image.
	 * 
	 * @param img
	 *            The image.
	 * @return An array containing minimum and maximum of the (whole) image.
	 */
	private float[] getExtrema(PlanarImage img) {
		float[] fExtrema = new float[2];
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(img);
		RenderedOp op = JAI.create("extrema", pb);
		double[][] dExtrema = (double[][]) op.getProperty("extrema");
		fExtrema[0] = (float) dExtrema[0][0];
		fExtrema[1] = (float) dExtrema[1][0];
		return (fExtrema);
	}

	/**
	 * Gets multiplication <code> factor </code> and <code> offset </code> value
	 * for normalization. The normalization is defined by the following pseudo
	 * code: <br>
	 * <code>
	 * destination[x][y] = source[x][y]*factor + offset; <br>
	 * </code>
	 * 
	 * @param img
	 *            The image.
	 * @param dynRange
	 *            Bandwidth of the normalized image.
	 * @return An array containing <code> factor </code> at index [0][0] and
	 *         <code> offset </code> at index [1][0].
	 */
	private double[][] getNormalizingScales(PlanarImage img, float dynRange) {
		float[] extrema = getExtrema(img);
		double[][] scales = new double[2][1];
		scales[0][0] = dynRange / (extrema[1] - extrema[0]);
		scales[1][0] = (dynRange * extrema[0]) / (extrema[0] - extrema[1]);
		return (scales);
	}

	private PlanarImage rescaleAmplitude(PlanarImage img, double[][] scales) {
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(pi);
		pb.add(scales[0]);
		pb.add(scales[1]);
		return (JAI.create("rescale", pb));
	}

	private PlanarImage scale(PlanarImage pi, float factor) {
		// Create a ParameterBlock and specify the source and
		// parameters
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(pi); // The source image
		pb.add(factor); // The xScale
		pb.add(factor); // The yScale
		pb.add(0.0F); // The x translation
		pb.add(0.0F); // The y translation
		pb.add(new InterpolationNearest()); // The interpolation
		// Create the scale operation
		return (JAI.create("scale", pb, null));
	}

	private RenderedImage createRenderedImage(float[] theData, int width,
			int height) {
		int numBands = 1;
		int len = width * height;
		Point origin = new Point(0, 0);
		SampleModel sampleModel = RasterFactory.createBandedSampleModel(
				DataBuffer.TYPE_FLOAT, width, height, numBands);
		ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
		TiledImage tiledImage = new TiledImage(0, 0, width, height, 0, 0,
				sampleModel, colorModel);
		DataBufferFloat dataBuffer = new DataBufferFloat(theData, len);
		Raster raster = RasterFactory.createWritableRaster(sampleModel,
				dataBuffer, origin);
		tiledImage.setData(raster);
		RenderedImageAdapter img = new RenderedImageAdapter(
				(RenderedImage) tiledImage);
		return img;
	}

	private PlanarImage getPlanarImage(String pathname) {
		PlanarImage img;
		String ext = pathname.substring(pathname.lastIndexOf('.') + 1);
		if (ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("pgm")
				|| ext.equalsIgnoreCase("tif") || ext.equalsIgnoreCase("tiff")
				|| ext.equalsIgnoreCase("gif") || ext.equalsIgnoreCase("bmp")
				|| ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")) {
			img = getJAICompatibleImage(pathname);
		} else if (ext.equalsIgnoreCase("imx")) {
			img = getLaVisionImage(pathname, 0);
		} else if (ext.equalsIgnoreCase("im7")) {
			img = getLaVisionImage(pathname, 1);
		} else {
			img = getDefaultImage();
		}
		if(jpiv.getSettings().imgType != java.awt.image.DataBuffer.TYPE_UNDEFINED) {
			// use smaller tiles than default for higher computation speed of
			// correlations
			javax.media.jai.ImageLayout layout = new javax.media.jai.ImageLayout(
					img);
			layout.setTileGridXOffset(0);
			layout.setTileGridYOffset(0);
			layout.setTileWidth(64);
			layout.setTileHeight(64);
			java.awt.RenderingHints hints = new java.awt.RenderingHints(
					javax.media.jai.JAI.KEY_IMAGE_LAYOUT, layout);
			ParameterBlock pb = new ParameterBlock();
			pb.addSource(img);
			pb.add(jpiv.getSettings().imgType);
			return (JAI.create("Format", pb, hints));
		} else {
			return (img);
		}
	}

	/**
	 * Export the image.
	 * 
	 * @param format
	 *            One of the supported format strings like 'png', 'jpg', 'tif'
	 *            etc.
	 * @param filename
	 *            Complete destination path.
	 */
	public void writePivImageToFile(String format, String filename) {
		int index = filename.lastIndexOf('.');
		if (index != -1)
			filename = filename.substring(0, index);
		if (format.equalsIgnoreCase("RAW")) {
			PlanarImage img = getAsPlanarImageByte(RMIN_MAX, 1f);
			int[] theRawData = new int[img.getWidth() * img.getHeight()];
			theRawData = img.getData().getSamples(0, 0, img.getWidth(),
					img.getHeight(), 0, theRawData);
			writeRawImageData(theRawData, filename + ".RAW");
		} else {
			ParameterBlock pb = new ParameterBlock();
			pb.addSource(getAsPlanarImageByte(RMIN_MAX, 1f));
			pb.addSource(getAsPlanarImage());
			pb.add(filename + "." + format);
			pb.add(format);
			JAI.create("filestore", pb);
		}
	}

	private void writeRawImageData(int[] dataBuffer, String pathname) {
		try {
			BufferedOutputStream output = new BufferedOutputStream(
					new FileOutputStream(pathname));
			byte[] greyvalue = new byte[2];
			for (int i = 0; i < dataBuffer.length; i++) {
				greyvalue[0] = (byte) (dataBuffer[i] - dataBuffer[i] / 256);
				greyvalue[1] = (byte) (dataBuffer[i] / 256);
				output.write(greyvalue);
			}
			output.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}

	private PlanarImage getJAICompatibleImage(String pathname) {
		PlanarImage img = JAI.create("fileload", pathname);
		ParameterBlock pb;
		// conversion to greyscale
		if (img.getSampleModel().getNumBands() == 3) {
			pb = new ParameterBlock();
			double[][] matrix = { { 0.114D, 0.587D, 0.299D, 0.0D } };
			pb.addSource(img);
			pb.add(matrix);
			img = JAI.create("bandcombine", pb);
		}
		return (img);
	}

	private PlanarImage getLaVisionImage(String pathname, int isIM7) {
		// reading file with native method loadImage(pathname, isIM7)
		// first, the library path is copied into the System variable "user.dir"
		// this silly workaround is necessary, because the LaVisionImageLoader
		// does not have access to the Settings object, so the user.dir variable
		// is abused for this purpose
		String userDir = System.getProperty("user.dir");
		System.setProperty("user.dir", jpiv.getSettings().jpivLibPath);
		jpiv2.LaVisionImageLoader imageLoader = new jpiv2.LaVisionImageLoader();
		float[] data = imageLoader.loadImage(pathname, isIM7);
		int xn = (int) data[data.length - 4];
		int yn = (int) data[data.length - 3];
		// int zn = (int) data[data.length - 2];
		// number of frames
		int fn = (int) data[data.length - 1];
		// int dataLength = data.length - 4;
		System.setProperty("user.dir", userDir);
		return ((PlanarImage) createRenderedImage(data, xn, yn * fn));
	}

	private PlanarImage getDefaultImage() {
		BufferedImage img = new BufferedImage(512, 256,
				BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g2 = (Graphics2D) img.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.LIGHT_GRAY);
		java.awt.Font font = new java.awt.Font("SansSerif",
				java.awt.Font.PLAIN, 18);
		g2.setFont(font);
		g2.drawString("Sorry, image format not supported!", 10, 46);
		g2.drawString("Sorry, image format not supported!", 160, 174);
		return (PlanarImage.wrapRenderedImage(img));
	}

	private jpiv2.JPiv jpiv;
	private PlanarImage pi;
	private String pathname;
	/**
	 * Normalization between 0 and 64 counts grey value (bandwidth 256 counts).
	 */
	public static final int R00064 = 6;
	/**
	 * Normalization between 0 and 128 counts grey value (bandwidth 256 counts).
	 */
	public static final int R00128 = 7;
	/**
	 * Normalization between 0 and 256 counts grey value (bandwidth 256 counts).
	 */
	public static final int R00256 = 8;
	/**
	 * Normalization between 0 and 512 counts grey value (bandwidth 256 counts).
	 */
	public static final int R00512 = 9;
	/**
	 * Normalization between 0 and 1024 counts grey value (bandwidth 256
	 * counts).
	 */
	public static final int R01024 = 10;
	/**
	 * Normalization between 0 and 2048 counts grey value (bandwidth 256
	 * counts).
	 */
	public static final int R02048 = 11;
	/**
	 * Normalization between 0 and 4096 counts grey value (bandwidth 256
	 * counts).
	 */
	public static final int R04096 = 12;
	/**
	 * Normalization between 0 and 8192 counts grey value (bandwidth 256
	 * counts).
	 */
	public static final int R08192 = 13;
	/**
	 * Normalization between 0 and 16384 counts grey value (bandwidth 256
	 * counts).
	 */
	public static final int R16384 = 14;
	/**
	 * Normalization between 0 and 32768 counts grey value (bandwidth 256
	 * counts).
	 */
	public static final int R32768 = 15;
	/**
	 * Normalization between 0 and 65536 counts grey value (bandwidth 256
	 * counts).
	 */
	public static final int R65536 = 16;
	/**
	 * Normalization between minimum and maximum grey value (bandwidth 256
	 * pixels).
	 */
	public static final int RMIN_MAX = 0;
	/**
	 * Determine minimal pixel grey values for a series of images.
	 */
	public static final int MIN = 100;
	/**
	 * Sum up the pixel grey values for a series of images.
	 */
	public static final int SUM = 200;
	/**
	 * Subtract the minimum of a series of images from an image.
	 */
	public static final int RM_SLIDING_BACKGRD = 300;
}
