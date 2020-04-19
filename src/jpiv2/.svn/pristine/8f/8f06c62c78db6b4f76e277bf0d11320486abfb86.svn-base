/*
 * PivDataFilter.java
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
 * Filters for PIV data. Wrapper class for all filters that have one input and
 * one output file. The filtering is done in a seperat thread.
 * 
 */
public class PivDataFilter extends Thread {

	/**
	 * Normalized median test.
	 */
	public final static int NORMALIZED_MEDIAN_TEST = 0;
	/**
	 * Replace invalid vectors by median.
	 */
	public final static int REPLACE_INVALID_BY_MEDIAN = 1;
	/**
	 * Remove invalid vectors.
	 */
	public final static int REMOVE_INVALID = 2;
	/**
	 * Median filter.
	 */
	public final static int MEDIAN_FILTER = 3;
	/**
	 * Median filter, also using vectors marked invalid.
	 */
	public final static int MEDIAN_FILTER_ALL = 4;
	/**
	 * 3x3 smoothing.
	 */
	public final static int SMOOTH = 5;
	/**
	 * 3x3 smoothing, also considering vectors marked invalid.
	 */
	public final static int SMOOTH_ALL = 6;
	/**
	 * Remove vectors with less than a specified number of neighbours.
	 */
	public final static int REMOVE_ISOLATED_VECTORS = 7;
	/**
	 * Substract a constant.
	 */
	public final static int SUBSTRACT_REFERENCE_DISPL = 8;
	/**
	 * Calculate vorticity component normal to the measurement plane.
	 */
	public final static int NORMAL_VORTICITY = 9;
	/**
	 * Calculate in-plane shear.
	 */
	public final static int IN_PLANE_SHEAR = 10;
	/**
	 * Calculate extensional strain.
	 */
	public final static int EXTENSIONAL_STRAIN = 11;
	/**
	 * Calculate turn verctor field 180 deg around x-axis.
	 */
	public final static int REVERSE_Y = 12;
	/**
	 * Flip sign of y velocity component.
	 */
	public final static int FLIP_Y = 13;
	private jpiv2.JPiv jpiv;
	private int filter;
	private String[] files;
	private String destPath;
	private java.text.DecimalFormat df;
	private double normMedianTestNoiseLevel;
	private double normMedianTestThreshold;
	private int invalidateIsolatedVectorsNumOfNeighbours;
	private double subsRefDispDx;
	private double subsRefDispDy;

	/**
	 * Creates a new instance of PivDataFilter
	 * 
	 * @param jpiv
	 *            the jpiv2.JPiv parent component.
	 * @param filter
	 *            one of the constants NORMALIZED_MEDIAN_TEST,
	 *            REPLACE_INVALID_BY_MEDIAN, REMOVE_INVALID, MEDIAN_FILTER,
	 *            SMOOTH, REMOVE_ISOLATED_VECTORS, or SUBSTRACT_REFERENCE_DISPL
	 *            to define the type of filter.
	 */
	public PivDataFilter(jpiv2.JPiv jpiv, int filter) {
		this.jpiv = jpiv;
		this.filter = filter;
		initVariables();
	}

	/**
	 * Do not call this function directly, rather use
	 * jpiv2.PivDataFilter().start() to run the data filtering process in a
	 * seperate thread.
	 */
	@Override
	public void run() {
		synchronized (getClass()) {
			if (destPath != null && files != null) {
				for (int f = 0; f < files.length; f++) {
					System.out.print("Processing: " + files[f] + " ...");
					PivData pivData = new PivData(files[f]);
					switch (filter) {
					case NORMALIZED_MEDIAN_TEST: {
						pivData.normalizedMedianTest(normMedianTestNoiseLevel,
								normMedianTestThreshold);
						break;
					}
					case REPLACE_INVALID_BY_MEDIAN: {
						pivData.replaceByMedian(false, false);
						break;
					}
					case REMOVE_INVALID: {
						pivData.removeInvalidVectors();
						break;
					}
					case MEDIAN_FILTER: {
						pivData.replaceByMedian(true, false);
						break;
					}
					case MEDIAN_FILTER_ALL: {
						pivData.replaceByMedian(true, true);
						break;
					}
					case SMOOTH: {
						pivData.smooth(true);
						break;
					}
					case SMOOTH_ALL: {
						pivData.smooth(false);
						break;
					}
					case REMOVE_ISOLATED_VECTORS: {
						pivData.invalidateIsolatedVectors(invalidateIsolatedVectorsNumOfNeighbours);
						break;
					}
					case SUBSTRACT_REFERENCE_DISPL: {
						pivData.subsRefDisp(subsRefDispDx, subsRefDispDy);
						break;
					}
					case NORMAL_VORTICITY: {
						pivData.deformation(true, NORMAL_VORTICITY);
						break;
					}
					case IN_PLANE_SHEAR: {
						pivData.deformation(true, IN_PLANE_SHEAR);
						break;
					}
					case EXTENSIONAL_STRAIN: {
						pivData.deformation(true, EXTENSIONAL_STRAIN);
						break;
					}
					case REVERSE_Y: {
						pivData.reverseY();
						break;
					}
					case FLIP_Y: {
						pivData.flipY();
						break;
					}
					}
					// saving the results
					int index = destPath.lastIndexOf('.');
					if (index != -1)
						destPath = destPath.substring(0, index);
					pivData.writeDataToFile(destPath + df.format(f) + ".jvc",
							jpiv.getSettings().loadSaveTecplotHeader);
					jpiv.getListFrame().appendElement(
							destPath + df.format(f) + ".jvc");
					System.out.println("done.");
				}
				System.out.println("Finished vector processing.");
			}
		}
	}

	private void initVariables() {
		this.files = jpiv.getListFrame().getSelectedElements();
		if (files == null) {
			System.out.println("No files selected. Nothing to do.");
		} else {
			this.df = jpiv2.FileHandling.getCounterFormat(files.length);
			this.destPath = chooseDestPath(FlexFileChooser.JVC);
			this.normMedianTestNoiseLevel = jpiv.getSettings().normMedianTestNoiseLevel;
			this.normMedianTestThreshold = jpiv.getSettings().normMedianTestThreshold;
			this.invalidateIsolatedVectorsNumOfNeighbours = jpiv.getSettings().invalidateIsolatedVectorsNumOfNeighbours;
			this.subsRefDispDx = jpiv.getSettings().subsRefDispDx;
			this.subsRefDispDy = jpiv.getSettings().subsRefDispDy;
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
