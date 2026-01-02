/*
 * PivDataReduce.java
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
 * PIV Data reduction. Wrapper class for all vector operations that have
 * multiple input files and a single output file. The operation is done in a
 * seperate thread.
 */
public class PivDataReduce extends Thread {
	/**
	 * Constant used to specify a data reduction process.
	 */
	public final static int AVERAGE = 0;
	private jpiv2.JPiv jpiv;
	private int op;
	private String[] files;
	private String destPath;

	/**
	 * Creates a new instance of PivDataFilter.
	 * 
	 * @param jpiv
	 *            The mother component.
	 * @param op
	 *            A constant (e.g. PivDataReduce.AVERAGE) that defines the
	 *            operation.
	 */
	public PivDataReduce(jpiv2.JPiv jpiv, int op) {
		this.jpiv = jpiv;
		this.op = op;
		initVariables();
	}

	/**
	 * Do not call this function directly, rather use
	 * jpiv2.PivDataReduce().start() to run the data reduction process in a
	 * seperate thread.
	 */
	public void run() {
		synchronized (getClass()) {
			if (destPath != null && files != null) {
				jpiv2.PivData pivData = null;
				System.out.print("Start data reduction ...");
				switch (op) {
				case AVERAGE: {
					pivData = new jpiv2.PivData(files);
					break;
				}
				}
				// saving the results
				int index = destPath.indexOf('.');
				if (index != -1)
					destPath = destPath.substring(0, index);
				pivData.writeDataToFile(destPath + ".jvc",
						jpiv.getSettings().loadSaveTecplotHeader);
				jpiv.getListFrame().appendElement(destPath + ".jvc");
				System.out.println("done.");
			}
		}
	}

	private void initVariables() {
		this.files = jpiv.getListFrame().getSelectedElements();
		if (files == null) {
			System.out.println("No files selected. Nothing to do.");
		} else {
			this.destPath = chooseDestPath(FlexFileChooser.JVC);
		}
	}

	/**
	 * Brings up a file chooser dialog to determine a destination file.
	 * 
	 * @param type
	 *            The file extension ID.
	 * @return The selected pathname or null if the user cancelled the dialog.
	 */
	private String chooseDestPath(int type) {
		String pathname;
		jpiv2.FlexFileChooser flexFileChooser = jpiv.getFlexFileChooser();
		flexFileChooser.setFiletype(type, false);
		int approve = flexFileChooser.showSaveDialog(jpiv);
		if (approve == javax.swing.JFileChooser.APPROVE_OPTION) {
			pathname = flexFileChooser.getSelectedFile().toString();
			return (pathname);
		} else
			return (null);
	}
}
