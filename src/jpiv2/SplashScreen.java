/*
 * SplashScreen.java
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
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;

/**
 * An always on top, borderless frame.
 */
public class SplashScreen extends Frame {

	private Image img;

	/**
	 * Creates a new instance of a SplashScreen
	 * 
	 * @param img
	 *            A BufferedImage to display.
	 */
	public SplashScreen(Image img) {
		this.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent evt) {
				quit();
			}
		});
		this.setUndecorated(true);
		this.setSize(img.getWidth(null), img.getHeight(null));
		this.setAlwaysOnTop(true);
		this.img = img;
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Point p = new Point((int) (screen.getWidth() - img.getWidth(null)) / 2,
				(int) (screen.getHeight() - img.getHeight(null)) / 2);
		setLocation(p);
		setVisible(true);
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
		g2d.drawImage(img, transform, null);
	}

	/**
	 * Close the splash screen.
	 */
	public void quit() {
		this.setVisible(false);
		this.dispose();
	}
}