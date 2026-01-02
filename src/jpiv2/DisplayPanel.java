/*
 * DisplayPanel.java
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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.ArrayList;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

/**
 * A display panel enabling one to show different parts of an image. This panel
 * was created to switch between the two frames of a double frame PIV image.
 * Frame 0 means the upper half of the image is shown, frame 1 means the lower
 * half and frame 2 referes to the whole image.
 * 
 */
public class DisplayPanel extends javax.swing.JPanel {

	private jpiv2.JPiv jpiv;
	private PlanarImage pi;
	private ArrayList<Shape> shapeList = new ArrayList<Shape>();
	private AffineTransform transform = AffineTransform.getTranslateInstance(
			0d, 0d);
	private int frame = 0;

	/**
	 * Constructor.
	 * 
	 * @param jpiv
	 *            The parent component.
	 * @param pi
	 *            The image.
	 * @param frame
	 *            The frame number (0 upper half, 1 lower half, 2 whole image).
	 */
	public DisplayPanel(jpiv2.JPiv jpiv, PlanarImage pi, int frame) {
		this.jpiv = jpiv;
		setImage(pi);
		showFrame(frame);
	}

	/**
	 * This method is called automatically when the images needs to be repainted
	 * ons screen (e.g. when moving or resizing the window).
	 * 
	 * @param g
	 *            The graphics context.
	 */
	protected void paintComponent(Graphics g) {
		BufferedImage bi = pi.getAsBufferedImage();
		bi = drawShapes(bi);
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawRenderedImage(bi, transform);
	}

	private BufferedImage drawShapes(BufferedImage bi) {
		java.awt.Color c = jpiv.getSettings().shapeColor;
		Graphics2D g2d = (Graphics2D) bi.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(new java.awt.Color(c.getRed() / 255f, c.getGreen() / 255f,
				c.getBlue() / 255f, 0.5f));
		for (int i = 0; i < shapeList.size(); i++) {
			g2d.fill((Shape) shapeList.get(i));
			g2d.draw((Shape) shapeList.get(i));
		}
		return (bi);
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
	public void writeVecImgToFile(String format, String filename) {
		BufferedImage bi = pi.getAsBufferedImage();
		bi = drawShapes(bi);
		int index = filename.lastIndexOf('.');
		if (index != -1)
			filename = filename.substring(0, index);
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(PlanarImage.wrapRenderedImage(bi));
		pb.add(filename + "." + format);
		pb.add(format);
		JAI.create("filestore", pb);
	}

	/**
	 * Replace the old image by a new one.
	 * 
	 * @param pi
	 *            The new image.
	 */
	public void setImage(PlanarImage pi) {
		this.pi = pi;
	}

	/**
	 * Add another shape.
	 * 
	 * @param newShape
	 *            The new shape.
	 */
	public void addShape(Shape newShape) {
		shapeList.add(newShape);
	}

	/**
	 * Returns a list of shapes (e.g. profiles drawn by the user).
	 * 
	 * @return A list of shapes.
	 */
	public ArrayList<Shape> getShapeList() {
		return (shapeList);
	}

	/**
	 * Switch between the visible image parts.
	 * 
	 * @param frame
	 *            The frame (0 upper half, 1 lower half, 2 whole image).
	 */
	public void showFrame(int frame) {
		this.frame = frame;
		if (frame == 0) {
			this.transform.setToTranslation(0, 0);
			this.setPreferredSize(new Dimension(pi.getWidth(),
					pi.getHeight() / 2));
		}
		if (frame == 1) {
			this.transform.setToTranslation(0, -pi.getHeight() / 2);
			this.setPreferredSize(new Dimension(pi.getWidth(),
					pi.getHeight() / 2));
		}
		if (frame == 2) {
			this.transform.setToTranslation(0, 0);
			this.setPreferredSize(new Dimension(pi.getWidth(), pi.getHeight()));
		}
		this.revalidate();
		this.repaint();
	}
}
