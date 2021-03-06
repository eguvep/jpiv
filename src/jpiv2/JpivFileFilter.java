/*
 * JpivFileFilter.java
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

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Some file filters that are used by the jpiv2.FlexFileChooser.
 * 
 * @see jpiv2.FlexFileChooser
 */
public class JpivFileFilter extends FileFilter implements java.io.FileFilter {

	private String[] validExtension;
	private String description;

	/**
	 * Constructs a new instance of jpiv2.JpivFileFilter.
	 * 
	 * @param validExtension
	 *            Extenstion of the displayed file type.
	 * @param description
	 *            A description of the file type.
	 */
	public JpivFileFilter(String[] validExtension, String description) {
		this.validExtension = validExtension;
		this.description = description;
	}

	/**
	 * Decides whether file is accepted or not.
	 * 
	 * @param f
	 *            A file object.
	 * @return true if the filename has the specified extension.
	 */
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		String extension = jpiv2.FileHandling.getFileExtension(f);
		for (int n = 0; n < validExtension.length; n++) {
			if (extension != null
					&& extension.equalsIgnoreCase(validExtension[n])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the description of this filter.
	 * 
	 * @return The description String of this filter.
	 */
	public String getDescription() {
		return (description);
	}
}