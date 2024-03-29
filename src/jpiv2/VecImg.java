/*
 * VecImg.java
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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.media.jai.InterpolationBilinear;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

//import org.freehep.graphics2d.VectorGraphics;

/**
 * Pixel image representing vector data. This class provides some methods for
 * displaying vector data as pixel images. The data needs to be organized as
 * described in detail at @see jpiv2.PivData.
 * 
 */
public class VecImg {
	private jpiv2.JPiv jpiv;
	private jpiv2.PivData pivData;
	private jpiv2.Settings settings;
	// private VectorGraphics vg;
	private Graphics2D vg;
	private String pathname;
	private ArrayList<Shape> shapeList = new ArrayList<Shape>();
	// convenience values
	private int[] size;
	private int[] imgSize;
	private int xoffset;
	private int yoffset;
	private float zm;

	/**
	 * Creates a new instance of VecImg
	 * 
	 * @param jpiv
	 *            The parent component.
	 * @param pathname
	 *            The pathname of the UTF-8 file containing vector data.
	 */
	public VecImg(jpiv2.JPiv jpiv, String pathname) {
		this.jpiv = jpiv;
		this.settings = jpiv.getSettings();
		this.pathname = pathname;
		this.pivData = new jpiv2.PivData(pathname);
		this.size = pivData.getDimension();
		this.imgSize = pivData.getImgSize();
		this.xoffset = settings.vectorXOffset;
		this.yoffset = settings.vectorYOffset;
		this.zm = settings.vectorZoom;
	}

	/**
	 * Returns the data as a Planar Image
	 * 
	 * @return The PlanarImage.
	 */
	public PlanarImage getAsPlanarImage() {
		BufferedImage bi = new BufferedImage(
				(int) (imgSize[0] * zm + 2.8 * xoffset),
				(int) (imgSize[1] * zm + 2 * yoffset),
				BufferedImage.TYPE_INT_RGB);
		// vg = VectorGraphics.create( bi.createGraphics() );
		vg = bi.createGraphics();
		vg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		drawGraphics();
		return (PlanarImage.wrapRenderedImage(bi));
	}

	/**
	 * Export the vector plot in a vector based format like eps, pdf, svg etc.
	 * 
	 * @param filename
	 *            the filename
	 * @param format
	 *            one of the following format strings: "eps", "pdf", "svg",
	 *            "emf", or "swf"
	 */
	public void drawIntoVectorGraphicsFile(String filename, String format) {
		System.out
				.println("Function temporarily disabled due to an unsolved bug. Use a pixel image export instead. Sorry for the inconvenience");
		/**
		 * int index = filename.lastIndexOf('.'); if(index != -1) filename =
		 * filename.substring(0, index); File file = new File(filename + "." +
		 * format); Dimension dim = new Dimension((int) (imgSize[0]*zm +
		 * 2.8*xoffset), (int) (imgSize[1]*zm + 2*yoffset) ); try { if
		 * (format.equals("eps")) { vg = new
		 * org.freehep.graphicsio.ps.PSGraphics2D(file, dim); } else if
		 * (format.equals("pdf")) { Properties p = new Properties();
		 * p.setProperty("PageSize","A4"); vg = new
		 * org.freehep.graphicsio.pdf.PDFGraphics2D(file, dim); } else if
		 * (format.equals("svg")) { vg = new
		 * org.freehep.graphicsio.svg.SVGGraphics2D(file, dim); } else if
		 * (format.equals("emf")) { vg = new
		 * org.freehep.graphicsio.emf.EMFGraphics2D(file, dim); } else if
		 * (format.equals("swf")) { vg = new
		 * org.freehep.graphicsio.swf.SWFGraphics2D(file, dim); } } catch
		 * (Exception e) { System.out.println( e.toString() ); }
		 * vg.startExport(); drawGraphics(); vg.endExport();
		 */
	}

	/**
	 * Returns the name of the data file.
	 * 
	 * @return The filename (not the pathname).
	 */
	public String getFilename() {
		return (pathname
				.substring(pathname.lastIndexOf(jpiv.fileSeparator) + 1));
	}

	/**
	 * Get the vector at a point x, y. Gets the vector that is closest to a
	 * given point x, y.
	 * 
	 * @param x
	 *            The x coordinate in pixel (zero at upper left corner).
	 * @param y
	 *            The y coordinate in pixel (zero at upper left corner).
	 * @return A single line of the PIV data.
	 */
	public double[] getVectorAt(int x, int y) {
		int xoffset = settings.vectorXOffset;
		int yoffset = settings.vectorYOffset;
		return (pivData.getVectorAt((int) Math.rint((x - xoffset) / zm),
				(int) Math.rint((y - yoffset) / zm)));
	}

	/**
	 * Get the PIV data.
	 * 
	 * @return The data.
	 */
	public jpiv2.PivData getPivData() {
		return (pivData);
	}

	/**
	 * Creates a PlanarImage visualizing PIV-data.
	 * 
	 * @param path
	 *            Absolute pathname of a PIV-data set.
	 * @return A PlanarImage showing the PIV-data as vectors.
	 * @see jpiv2.PivData
	 */
	private void drawGraphics() {
		float tckspc = settings.vectorTickSpacing;
		float axsscl = settings.vectorAxisScaling;
		String unitLbl = settings.vectorAxisLabel;
		// draw background
		vg.setColor(settings.vectorBorder);
		vg.fillRect(0, 0, (int) (imgSize[0] * zm + 2.8 * xoffset),
				(int) (imgSize[1] * zm + 2.8 * yoffset));
		// draw axes
		vg.setColor(settings.vectorAxes);
		vg.drawRect(xoffset, yoffset, (int) (imgSize[0] * zm),
				(int) (imgSize[1] * zm));
		vg.setFont(new Font("Dialog", Font.PLAIN, 18));
		FontMetrics fm = vg.getFontMetrics();
		String label = new String();
		int numHeight = fm.getAscent();
		int strWidth;
		// x axis
		float labelVal = 0;
		for (float x = xoffset; x <= imgSize[0] * zm + xoffset; x += tckspc
				* zm / axsscl) {
			vg.drawLine(Math.round(x), yoffset, Math.round(x), yoffset - 10);
			label = String.valueOf(labelVal);
			labelVal += tckspc;
			strWidth = fm.stringWidth(label);
			vg.drawString(label, x - strWidth / 2, yoffset - numHeight);
		}
		// x unit label
		vg.drawString(unitLbl, (int) (1.5 * xoffset) + imgSize[0] * zm, yoffset
				- numHeight);
		// y axis
		labelVal = 0;
		for (float y = yoffset; y <= imgSize[1] * zm + yoffset; y += tckspc
				* zm / axsscl) {
			vg.drawLine(xoffset, Math.round(y), xoffset - 10, Math.round(y));
			label = String.valueOf(labelVal);
			labelVal += tckspc;
			strWidth = fm.stringWidth(label);
			vg.drawString(label, xoffset - 12 - strWidth, y + numHeight / 2);
		}
		// y unit label
		strWidth = fm.stringWidth(unitLbl);
		vg.drawString(unitLbl, xoffset - 12 - strWidth, (int) (1.9 * yoffset)
				+ imgSize[1] * zm);
		// background
		// draw background
		if (settings.vectorBackgroundImage) {
			PivImg img = new PivImg(jpiv, settings.vectorBackgroundImagePath);
			AffineTransform transform = AffineTransform.getTranslateInstance(
					xoffset, yoffset);
			PlanarImage pi = img.getAsPlanarImageByte(PivImg.RMIN_MAX, 1f);
			pi = scale(pi, zm);
			vg.drawRenderedImage(pi, transform);
		} else {
			vg.setColor(settings.vectorBackground);
			vg.fillRect(xoffset, yoffset, (int) (imgSize[0] * zm),
					(int) (imgSize[1] * zm));
		}
		// draw vectors
		drawVectors();
		// draw color bar
		if (settings.vectorShowColorBar) {
			DecimalFormat df = new DecimalFormat();
			df.applyLocalizedPattern(settings.vectorColorBarFormatString);
			int lvls = settings.vectorColorLevels;
			float cMin = settings.vectorMultiColorMin;
			float cMax = settings.vectorMultiColorMax;
			if (settings.vectorBackgroundColorCoding) {
				cMin = settings.vectorBackgroundColorCodingMin;
				cMax = settings.vectorBackgroundColorCodingMax;
			}
			float c;
			float cLow;
			int xpos = xoffset + Math.round(imgSize[0] * zm + 10);
			int ypos = 0;
			int height = Math.round(imgSize[1] * zm / lvls);
			for (int n = 0; n < lvls; n++) {
				cLow = cMin + (cMax - cMin) * n / lvls;
				c = (cMin + (2 * n + 1) * (cMax - cMin) / (2 * lvls));
				ypos = Math.round(yoffset + n * imgSize[1] * zm / lvls);
				vg.setColor(getMultiColorCode(cMin, cMax, c));
				vg.fillRect(xpos, ypos, 10, height);
				vg.setColor(Color.BLACK);
				vg.drawString(
						df.format(cLow * settings.vectorColorBarUnitConversion),
						xpos + 15, ypos + numHeight / 2);
			}
			vg.drawString(settings.vectorColorBarLabel, xpos + 15, ypos
					+ numHeight / 2 + height);
		}
		// draw stamp
		vg.setColor(Color.BLACK);
		vg.setFont(new Font("Dialog", Font.PLAIN, 8));
		fm = vg.getFontMetrics();
		String stamp = "jpiv";
		vg.drawString(stamp,
				(int) (imgSize[0] * zm + 2.8 * xoffset) - fm.stringWidth(stamp)
						- 2, (int) (imgSize[1] * zm + 2 * yoffset) - 2);
		// draw shapes
		drawShapes();
	}

	private void drawVectors() {
		float zm = settings.vectorZoom;
		int xoffset = settings.vectorXOffset; // border width x
		int yoffset = settings.vectorYOffset; // border width y
		double[][] d = pivData.getPivData(); // the vector data
		int x0, y0; // vector origin
		double dx, dy, // vector components
		l, // vector length
		vs = settings.vectorScale, // vector length scale
		b, // base points of arrow head (-b, +b)
		hl = settings.vectorHeadLength, // head length as a fraction of l
		phi, // pointing direction
		gamma = settings.vectorAngle * Math.PI / 180; // arrow head angle
		float unitScale = settings.vectorReferenceUnitConversion;
		boolean multiColor = settings.vectorMultiColor; // color corresponds to
														// the vector length
		float colorMin = settings.vectorMultiColorMin / unitScale; // this
																	// length
																	// corresponds
																	// to blue
		float colorMax = settings.vectorMultiColorMax / unitScale; // this
																	// length
																	// corresponds
																	// to red
		int[] px = new int[3]; // x coordinates of the arrowhead points
		int[] py = new int[3]; // y coordinates of the arrowhead points
		int[] spc = pivData.getSpacing(); // horizontal and vertical vector
											// spacing
		float colorBMin = settings.vectorBackgroundColorCodingMin / unitScale;
		float colorBMax = settings.vectorBackgroundColorCodingMax / unitScale;
		int col = settings.vectorBackgroundColorCodingColumn;
		// draw color coded background
		if (settings.vectorBackgroundColorCoding) {
			for (int i = 0; i < d.length; i++) {
				vg.setColor(getMultiColorCode(colorBMin, colorBMax,
						(float) d[i][col]));
				vg.fillRect(
						(int) (Math.abs(d[i][0] * zm) + xoffset - (float) spc[0]
								/ 2f * zm), (int) (Math.abs(d[i][1] * zm)
								+ yoffset - (float) spc[1] / 2f * zm),
						(int) (spc[0] * zm), (int) (spc[1] * zm));
			}
		}
		// draw vectors
		for (int i = 0; i < d.length; ++i) {
			vg.setColor(settings.vectorColor);
			x0 = (int) (Math.abs(d[i][0] * zm) + xoffset);
			y0 = (int) (Math.abs(d[i][1] * zm) + yoffset);
			dx = d[i][2];
			dy = d[i][3];
			phi = Math.atan2(dy, dx);
			l = Math.sqrt((dx * dx + dy * dy)) * vs;
			if (multiColor) {
				vg.setColor(getMultiColorCode(colorMin, colorMax,
						(float) (l / vs)));
			}
			if (d[0].length > 4 && settings.labelInvalidVectors) {
				if (d[i][4] <= 0)
					vg.setColor(settings.vectorColorInvalid);
			}
			if (settings.vectorUniformLength && l > settings.vectorLengthThreshold) {
				l=settings.vectorLength;
			}
			b = Math.tan(gamma / 2) * l * hl;
			px[0] = (int) (x0 + (hl * l));
			px[1] = (int) (x0 + (hl * l));
			px[2] = (int) (x0 + l);
			py[0] = (int) ((y0 + b));
			py[1] = (int) ((y0 - b));
			py[2] = y0;
			vg.rotate(phi, x0, y0);
			vg.drawLine(x0, y0, (int) (x0 + l), y0);
			vg.drawLine(x0, y0, (int) (x0 + l), y0);
			vg.fillPolygon(px, py, 3);
			vg.rotate(-phi, x0, y0);
		}
		// draw reference vector
		if (settings.vectorShowReference) {
			vg.setColor(settings.vectorColor);
			x0 = (int) (10 + xoffset);
			y0 = (int) (10 + yoffset);
			dx = settings.vectorReferenceLength;
			dy = 0;
			l = dx * vs / unitScale;
			if (multiColor) {
				vg.setColor(getMultiColorCode(colorMin, colorMax,
						(float) (l / vs)));
			}
			b = Math.tan(gamma / 2) * l * hl;
			px[0] = (int) (x0 + (hl * l));
			px[1] = (int) (x0 + (hl * l));
			px[2] = (int) (x0 + l);
			py[0] = (int) ((y0 + b));
			py[1] = (int) ((y0 - b));
			py[2] = y0;
			vg.drawLine(x0, y0, (int) (x0 + l), y0);
			vg.fillPolygon(px, py, 3);
			FontMetrics fm = vg.getFontMetrics();
			String refLabel = settings.vectorReferenceLabel;
			vg.drawString((dx + " " + refLabel), (int) (x0 + l + 10),
					y0 + fm.getAscent() / 2);
		}
	}

	/**
	 * Copy a list of shapes into the VecImg. The shapes are drawn at the next
	 * call of getAsPlanarImage() or drawIntoVectorGraphicsFile().
	 * 
	 * @param shapeList
	 *            A list of shapes.
	 */
	public void setShapeList(ArrayList<Shape> shapeList) {
		this.shapeList = shapeList;
	}

	private void drawShapes() {
		java.awt.Color c = settings.shapeColor;
		vg.setColor(new java.awt.Color(c.getRed() / 255f, c.getGreen() / 255f,
				c.getBlue() / 255f, 0.5f));
		for (int i = 0; i < shapeList.size(); i++) {
			vg.fill((Shape) shapeList.get(i));
			vg.draw((Shape) shapeList.get(i));
		}
	}

	/**
	 * Gets a linearily interpolated color for <code>val</code> between
	 * <code>min</code> = blue and <code>max</code> = red with green and yellow
	 * inbetween.
	 */
	private Color getMultiColorCode(float min, float max, float val) {
		if (val < min)
			val = min;
		if (val > max)
			val = max;
		float h = 2f / 3f * (1f - ((val - min) / (max - min)));
		float s = 1f;
		float b = 1f;
		Color c = Color.getHSBColor(h, s, b);
		return (new java.awt.Color(c.getRed() / 255f, c.getGreen() / 255f,
				c.getBlue() / 255f, settings.vectorMultiColorTransparancy));
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
		pb.add(new InterpolationBilinear()); // The interpolation
		// Create the scale operation
		return (JAI.create("scale", pb, null));
	}

}