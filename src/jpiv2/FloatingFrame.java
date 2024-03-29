/*
 * FloatingFrame.java
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

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * An always visible, borderless frame.
 */
public class FloatingFrame extends Window {

	/**
	 * Creates a new instance of FloatingFrame
	 * 
	 * @param owner
	 *            The mother frame.
	 * @param bi
	 *            A BufferedImage to display.
	 */
	public FloatingFrame(Frame owner, BufferedImage bi) {
		super(new Window(owner));
		this.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent evt) {
				quit();
			}
		});
		this.setSize(bi.getWidth(), bi.getHeight());
		this.setAlwaysOnTop(true);
		this.bi = bi;
	}

	/**
	 * Automatically called when the frame has to be painted.
	 * 
	 * @param g
	 *            The graphics context.
	 */
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		AffineTransform transform = AffineTransform
				.getTranslateInstance(0d, 0d);
		g2d.drawRenderedImage(bi, transform);
	}

	private void quit() {
		this.setVisible(false);
		this.dispose();
	}

	private BufferedImage bi;
}