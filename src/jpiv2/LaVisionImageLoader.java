/*
 * LaVisionImageLoader.java
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

/**
 * Wrapper class for the native C++ function: float[] loadImage(String pathname,
 * int isIM7). The function reads DaVis compressed image files (imx, im7) and
 * may be obtained at http://www.lavision.de. The function may be compiled for
 * Linux and Win32. To load the proper library for your system, create one of
 * the following directories and place the appropriate library there:<br>
 * Windows:<br>
 * (jpiv library directory)\win32\readLaVisionImg\readLaVisionImage.dll.<br>
 * Linux:<br>
 * (jpiv library directory)/linux/readLaVisionImg/libReadLaVisionImgage.so.<br>
 * Both image frames (if two) are returned in a single, one dimensional float
 * array.<br>
 * The last four array elements contain some meta information: <br>
 * array[array.length - 4] = xn, horizontal frame size (pixel) <br>
 * array[array.length - 3] = yn, vertical frame size (pixel) <br>
 * array[array.length - 2] = zn, ignored <br>
 * array[array.length - 1] = fn, number of frames <br>
 * 
 */
class LaVisionImageLoader {

	/** Constructor. */
	public LaVisionImageLoader() {
	}

	static {
		String sep = System.getProperty("file.separator");
		String os = System.getProperty("os.name");
		// the system variable "user.dir" is abused for carrying the information
		// of the library path
		String dir = System.getProperty("user.dir");
		os = os.toLowerCase();
		if (os.contains("win")) {
			System.load(dir + sep + "win64" + sep + "readLaVisionImg" + sep
				+ "readLaVisionImage.dll");
		} else if (os.contains("linux")) {
			System.load(dir + sep + "linux64" + sep + "readLaVisionImg" + sep
					+ "libReadLaVisionImage.so");
		} else if (os.indexOf("mac") != -1) {
			System.err.println("Decoding of LaVision Image formats is not yet "
					+ "supported on: " + os);
		} else {
			System.err.println("Decoding of LaVision Image formats is not yet "
					+ "supported on: " + os);
		}
	}

	/**
	 * The java representation of the C++ function
	 * 
	 * @param pathname
	 * @param isIM7
	 * @return
	 */
	public native float[] loadImage(String pathname, int isIM7);
}
