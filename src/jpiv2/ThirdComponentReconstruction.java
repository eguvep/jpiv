/*
 * ThirdComponentReconstruction.java
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

import java.text.DecimalFormat;

/**
 * Reconstruction of the third velocity component from a stack of 2d
 * measurements under the assumption of mass-conservation.
 */
public class ThirdComponentReconstruction extends Thread {

	private jpiv2.JPiv jpiv;
	private String[] files;
	private double[][][] data;
	private String destFileName;
	private double dz;
	private int skip;
	private boolean linReg;

	/**
	 * Creates a new instance of ThirdComponentReconstruction
	 * 
	 * @param jpiv
	 *            An instance of a jpiv2.JPiv object.
	 */
	public ThirdComponentReconstruction(jpiv2.JPiv jpiv) {
		this.jpiv = jpiv;
		initVariables();
	}

	/**
	 * Overrides run() in java.lang.Thread. Never call run() directly. Use
	 * jpiv2.ThirdComponentReconstruction.start(), inherited from
	 * java.lang.Thread to start an instance of ThirdComponentReconstruction as a
	 * new thread.
	 */
	@Override
	public void run() {
		synchronized (getClass()) {
			if (files == null || files.length < 3) {
				System.out
						.println("Select at least three vector files measured in parallel planes.");
			} else if (chooseDestFileName()) {
				System.out
						.println("Reconstruction of third velocity component started.");
				initVariables();
				reconstructThirdComponent();
				saveData();
				System.out
						.println("Reconstruction of third velocity component finished.");
			}
		}
	}

	private boolean chooseDestFileName() {
		jpiv2.FlexFileChooser flexFileChooser = jpiv.getFlexFileChooser();
		flexFileChooser.setFiletype(FlexFileChooser.JVC, false);
		int approve = flexFileChooser.showSaveDialog(jpiv);
		if (approve == javax.swing.JFileChooser.APPROVE_OPTION) {
			destFileName = flexFileChooser.getSelectedFile().toString();
			int index = destFileName.indexOf('.');
			if (index != -1)
				destFileName = destFileName.substring(0, index);
			destFileName += ".jvc";
			return (true);
		} else
			return (false);
	}

	private void initVariables() {
		files = jpiv.getListFrame().getSelectedElements();
		dz = jpiv.getSettings().thirdCompReconDz;
		skip = jpiv.getSettings().thirdCompReconSkip;
		linReg = jpiv.getSettings().thirdCompReconLinReg;
		data = new double[files.length][][];
		for (int f = 0; f < files.length; f++) {
			data[f] = new jpiv2.PivData(files[f]).getPivData();
		}
	}

	private void reconstructThirdComponent() {
		// calculate derivatives du/dx and dv/dy
		double deriv[][][][] = new double[data.length][][][];
		for (int d = 0; d < data.length; d++) {
			if (linReg) {
				deriv[d] = new PivData(files[d]).getFirstDerivativeLinReg();
			} else {
				deriv[d] = new PivData(files[d]).getFirstDerivative();
			}
		}
		int horSize = deriv[0][0].length;
		int verSize = deriv[0].length;
		// assuming the first layer to have zero z-velocity
		for (int l = 0; l < horSize * verSize; l++) {
			data[0][l][4] = 0.0;
		}
		// integrating along z
		for (int d = 1 + skip; d < data.length; d = d + 1 + skip) {
			for (int i = 0; i < verSize; i++) {
				for (int j = 0; j < horSize; j++) {
					// --------uz(d)-------- ---------uz(d-1)---------
					data[d][i * horSize + j][4] = data[d - 1 - skip][i
							* horSize + j][4]
							-
							// --------------duz(d)/dz--------------
							((deriv[d][i][j][0] + deriv[d][i][j][1]) +
							// --------------duz(d-1)/dz--------------
							(deriv[d - 1 - skip][i][j][0] + deriv[d - 1 - skip][i][j][1]))
							* dz / 2;
				}
			}
		}
	}

	private void saveData() {
		DecimalFormat df = jpiv2.FileHandling.getCounterFormat(files.length);
		int index = destFileName.lastIndexOf('.');
		if (index != -1)
			destFileName = destFileName.substring(0, index);
		for (int d = 0; d < data.length; d = d + 1 + skip) {
			new jpiv2.PivData(data[d]).writeDataToFile(
					destFileName + df.format(d) + ".jvc",
					jpiv.getSettings().loadSaveTecplotHeader);
			jpiv.getListFrame().appendElement(
					destFileName + df.format(d) + ".jvc");
		}
	}

}
