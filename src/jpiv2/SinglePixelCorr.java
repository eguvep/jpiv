/*
 * SinglePixelCorr.java
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
 * Single pixel ensemble correlation
 */
public class SinglePixelCorr extends Thread {

	private jpiv2.JPiv jpiv;
	// correlation matrix
	private float[][][][] corr;
	// settings
	boolean dualImg;
	private int roiP1x;
	private int roiP1y;
	private int roiP2x;
	private int roiP2y;
	private int domainW;
	private int domainH;
	private int horPreShift;
	private int verPreShift;
	private boolean use3by3;
	private boolean signalOnly;
	// deduced convenience values
	private int roiW;
	private int roiH;
	// margins
	private int top;
	private int bot;
	private int lft;
	private int rgt;
	// file handling
	private String[] files;
	private String destFileName;
	// pre-shift file
	private boolean createPreShiftFile;
	private double[][] shift;

	/**
	 * Constructs a new instance of SinglePixelCorr. Use
	 * jpiv2.SinglePixelCorr.start() to run the single pixel evaluation in a
	 * separate thread.
	 * 
	 * @param jpiv
	 *            An instance of a jpiv2.JPiv object.
	 */
	public SinglePixelCorr(jpiv2.JPiv jpiv) {
		this.jpiv = jpiv;
	}

	/**
	 * Overrides run() in java.lang.Thread. Never call run() directly. Use
	 * jpiv2.SinglePixelCorr.start(), inherited from java.lang.Thread to start
	 * an instance of SinglePixelCorr as a new thread.
	 */
	public void run() {
		synchronized (getClass()) {
			initVariables();
			initPreShift();
			if (files == null) {
				System.out.println("No files selected.");
			} else if (chooseDestFileName()) {
				System.out.println("Single pixel PIV evaluation started.");
				jpiv2.PivData theData = sumUpCorrelations();
				theData.writeDataToFile(destFileName,
						jpiv.getSettings().loadSaveTecplotHeader);
				jpiv.getListFrame().appendElement(destFileName);
				System.out.println("Single pixel PIV evaluation finished.");
			}
		}
	}

	private jpiv2.PivData sumUpCorrelations() {
		// loop over files
		PivImg pivImg;
		if (dualImg) {
			for (int f = 0; f < files.length; f++) {
				pivImg = new PivImg(jpiv, files[f]);
				addCorrelation(
						pivImg.getDataFloat(roiP1x, roiP1y, roiW, roiH, 0),
						pivImg.getDataFloat(roiP1x, roiP1y, roiW, roiH, 1));
				System.out.println("Processed file " + (f + 1) + ".");
			}
		} else {
			PivImg pivImgA;
			PivImg pivImgB;
			for (int f = 0; f < (files.length - 1); f += 1) {
				pivImgA = new PivImg(jpiv, files[f]);
				pivImgB = new PivImg(jpiv, files[f + 1]);
				addCorrelation(
						pivImgA.getDataFloat(roiP1x, roiP1y, roiW, roiH, 0),
						pivImgB.getDataFloat(roiP1x, roiP1y, roiW, roiH, 0));
				System.out.println("Processed file " + (f + 1) + " and "
						+ (f + 2) + ".");
			}
		}
		return (getDisplacements());
	}

	private jpiv2.PivData getDisplacements() {
		double[][] displacements = new double[(roiH - top - bot)
				* (roiW - lft - rgt)][5];
		int[] peak = new int[2];
		double[] peakFit = new double[2];
		int row = 0;
		for (int i = 0; i < (roiH - top - bot); i++) {
			for (int j = 0; j < (roiW - lft - rgt); j++) {
				peak = PivUtil.getPeak(corr[i][j]);
				peakFit = PivUtil
						.parabolicPeakFit(corr[i][j], peak[0], peak[1]);
				displacements[row][0] = j + lft;
				displacements[row][1] = i + top;
				displacements[row][2] = peakFit[1] + shift[i * roiW + j][2]
						- domainH / 2;
				displacements[row][3] = peakFit[0] + shift[i * roiW + j][3]
						- domainW / 2;
				// valid peak fit
				if (peakFit[0] != peak[0] && peakFit[1] != peak[1]) {
					displacements[row][4] = corr[i][j][peak[0]][peak[1]];
				}
				// invalid peak fit
				else {
					displacements[row][2] = 0;
					displacements[row][3] = 0;
					displacements[row][4] = -1;
				}
				row++;
			}
		}
		return (new PivData(displacements));
	}

	// m, n loop over the correlation function matrix
	// i, j loop over evaluation area of image A (same size as correlation
	// matrix)
	// k, l loop over the correlation function
	// o, p loop over the search area (same size as correlation function)
	private void addCorrelation(float[][] imgDataA, float[][] imgDataB) {
		float threshold = jpiv2.Statistics.getAverage(imgDataA);
		for (int m = 0, i = top; m < corr.length; m++, i++) {
			for (int n = 0, j = lft; n < corr[0].length; n++, j++) {
				for (int k = 0, o = i - (domainH - 1) / 2
						+ (int) shift[i * roiW + j][2]; k < domainH; k++, o++) {
					for (int l = 0, p = j - (domainW - 1) / 2
							+ (int) shift[i * roiW + j][3]; l < domainW; l++, p++) {
						if (!signalOnly || imgDataA[i][j] > threshold) {
							if (use3by3) {
								corr[m][n][k][l] += imgDataA[i - 1][j - 1]
										* imgDataB[o - 1][p - 1]
										+ imgDataA[i - 1][j]
										* imgDataB[o - 1][p]
										+ imgDataA[i - 1][j + 1]
										* imgDataB[o - 1][p + 1]
										+ imgDataA[i][j - 1]
										* imgDataB[o][p - 1] + imgDataA[i][j]
										* imgDataB[o][p] + imgDataA[i][j + 1]
										* imgDataB[o][p + 1]
										+ imgDataA[i + 1][j - 1]
										* imgDataB[o + 1][p - 1]
										+ imgDataA[i + 1][j]
										* imgDataB[o + 1][p]
										+ imgDataA[i + 1][j + 1]
										* imgDataB[o + 1][p + 1];
							} else {
								// if condition excludes points outside image due to pre-shift
								if (o<imgDataB.length && 
										o>0 && 
										p<imgDataB[0].length && 
										p>0) {
									corr[m][n][k][l] += imgDataA[i][j]
											* imgDataB[o][p];
								}
							}
						}
					}
				}
			}
		}
	}

	private void initVariables() {
		files = jpiv.getListFrame().getSelectedElements();
		// settings
		jpiv2.Settings settings = jpiv.getSettings();
		dualImg = settings.singlePixelTwoImg;
		if (settings.singlePixelROI) {
			roiP1x = settings.singlePixelROIP1x;
			roiP1y = settings.singlePixelROIP1y;
			roiP2x = settings.singlePixelROIP2x;
			roiP2y = settings.singlePixelROIP2y;
		} else {
			roiP1x = 0;
			roiP1y = 0;
			jpiv2.PivImg img = new PivImg(jpiv, files[0]);
			roiP2x = img.getWidth() - 1;
			if (settings.singlePixelTwoImg) {
				roiP2y = img.getHeight() / 2 - 1;
			} else {
				roiP2y = img.getHeight() - 1;
			}
		}
		domainW = settings.singlePixelDomainWidth;
		domainH = settings.singlePixelDomainHeight;
		horPreShift = settings.singlePixelHorPreShift;
		verPreShift = settings.singlePixelVerPreShift;
		use3by3 = settings.singlePixel3by3;
		signalOnly = settings.singlePixelSignalOnly;
		// deduced convenience values
		roiW = roiP2x - roiP1x + 1;
		roiH = roiP2y - roiP1y + 1;
		// margins
		top = (domainH - 1) / 2 - verPreShift;
		if (top < 0)
			top = 0;
		if (use3by3)
			top += 1;
		bot = (domainH - 1) / 2 + verPreShift;
		if (bot < 0)
			bot = 0;
		if (use3by3)
			bot += 1;
		lft = (domainW - 1) / 2 - horPreShift;
		if (lft < 0)
			lft = 0;
		if (use3by3)
			lft += 1;
		rgt = (domainW - 1) / 2 + horPreShift;
		if (rgt < 0)
			rgt = 0;
		if (use3by3)
			rgt += 1;
		corr = new float[(roiH - top - bot)][(roiW - lft - rgt)][domainH][domainW];
		createPreShiftFile = settings.singlePixelCreatePreShiftVectorField;
	}

	private void initPreShift() {
		jpiv2.PivData pivData;
		if (createPreShiftFile) {
			System.out
					.println("You will now be asked to select a filename to store the "
							+ "pre-shift PIV file. Do not forget to edit the standard-PIV "
							+ "settings in a way to produce meaningful shift data!");
			jpiv2.ListFrame lf = jpiv.getListFrame();
			jpiv2.PivEvaluation pivEval = new jpiv2.PivEvaluation(jpiv);
			synchronized (lf) {
				pivEval.start();
				try {
					lf.wait();
				} catch (java.lang.InterruptedException ie) {
					// nothing
				}
			}
			String preShiftFile = pivEval.getLastUsedDestFileName() + ".jvc";
			System.out.println(preShiftFile);
			pivData = new jpiv2.PivData(preShiftFile);
			System.out.println("Resample pre-shift data.");
			pivData.resample(roiP1x, roiP1y, roiP2x, roiP2y, 1, 1);
			System.out
					.println("Now, select a filename for the single pixel ensemble correlation output.");
		} else {
			pivData = new jpiv2.PivData(roiP1x, roiP1y, 1, 1, roiW, roiH,
					horPreShift, verPreShift, 0);
		}
		shift = pivData.getPivDataShift();
	}

	private boolean chooseDestFileName() {
		jpiv2.FlexFileChooser flexFileChooser = jpiv.getFlexFileChooser();
		flexFileChooser.setFiletype(flexFileChooser.JVC, false);
		int approve = flexFileChooser.showSaveDialog(jpiv);
		if (approve == javax.swing.JFileChooser.APPROVE_OPTION) {
			destFileName = flexFileChooser.getSelectedFile().toString();
			int index = destFileName.lastIndexOf('.');
			if (index != -1)
				destFileName = destFileName.substring(0, index);
			destFileName += ".jvc";
			return (true);
		} else
			return (false);
	}
}
