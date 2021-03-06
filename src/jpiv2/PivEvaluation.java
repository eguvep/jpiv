/*
 * PivEvaluation.java
 *
 * Copyright 2020 Peter Vennemann
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

import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

/**
 * PIV Evaluation.
 * 
 */
public class PivEvaluation extends Thread {

	private final jpiv2.JPiv jpiv;
	private jpiv2.Settings settings;
	// correlation fields
	private BufferedImage[] corr;
	// A BufferedImage may be used instead of a PlanarImage to force the JAI
	// operation chain to be executed. When using a PlanarImage, all operations
	// are stored in memory until the data is requested (jai pull model). This
	// leads to higher computation speed, but also to higher memory usage.
	private int passes;
	private int[] intWinW;
	private int[] intWinH;
	private int[] searchAreaW;
	private int[] searchAreaH;
	private int[] horVecSpacing;
	private int[] verVecSpacing;
	private int horPreShift;
	private int verPreShift;
	// initial vector field
	private int x0, y0, x1, y1, nx, ny;
	private double[][] shift;
	// first derivative for window deformation
	private double[][][] deriv;
	private int[] dim;
	// file handling
	private String[] files;
	private String destFileName;
	private String lastDestFileName = null;
	private java.text.DecimalFormat fileNumFormat;

	/**
	 * Creates a new instance of PivEvaluation
	 * 
	 * @param jpiv
	 *            The jpiv2.JPiv mother component.
	 */
	public PivEvaluation(jpiv2.JPiv jpiv) {
		this.jpiv = jpiv;
	}

	/**
	 * Overrides run() in java.lang.Thread. Never call run() directly. Use
	 * jpiv2.PivEvaluation.start(), inherited from java.lang.Thread to start an
	 * instance of PivEvaluation as a new thread. When the PIV evaluation is
	 * finished, jpiv.getListFrame().notify() is called. Thus, you can use
	 * jpiv.getListFrame().wait() to stop your thread as long as this thread is
	 * busy. In this way, you can automatically start a couple of PivEvaluation
	 * threads and modify some parameters in between.
	 */
	@Override
	public void run() {
		if (JPiv.verbosity) System.out.println("Called: PivEvaluation.run");
		jpiv2.ListFrame lf = jpiv.getListFrame();
		synchronized (lf) {
			files = lf.getSelectedElements();
			if (files == null) {
				System.out.println("No files selected.");
			} else {
				doPivEvaluation();
				lf.notify();
			}
		}
	}

	/**
	 * Does the actual PIV evaluation. This function does the looping over the
	 * passes and the images and writes the output into files. If you want to
	 * run the PIV evaluation in a separate thread, call the method start()
	 * instead.
	 * 
	 * @return The last used filename.
	 */
	public String doPivEvaluation() {
		if (JPiv.verbosity) System.out.println("Called: PivEvaluation.doPivEvaluation");
		int p = 0;
		initVariables(p);
		lastDestFileName = null;
		if (settings.pivUseImageBaseName || settings.pivUseDefaultDestFileName
				|| chooseDestFileName()) {
			System.out.println("JPiv PIV evaluation started.");
			jpiv2.PivData theData;
			jpiv2.PivImg pivImg;
			int available_cores = Runtime.getRuntime().availableProcessors();
			System.out.println("Available cores: " + available_cores);
			for (p = 0; p < passes; p++) {
				int cores = shift.length / 4000;
				if (cores < 2) cores = 2;
				if (cores > available_cores) cores = available_cores;
				System.out.println("Using " + cores + " cores.");
				for (int f = 0; f < files.length; f++) {
					switch (settings.pivSequence) {
					case jpiv2.Settings.PIV_CONSECUTIVE: {
						if (f == 0)
							f = 1;
						pivImg = new PivImg(jpiv, files[f - 1], files[f]);
						System.out.println("Processing: "
								+ jpiv2.FileHandling
										.stripFilename(files[f - 1]) + " and "
								+ jpiv2.FileHandling.stripFilename(files[f]));
						break;
					}
					case jpiv2.Settings.PIV_SKIP: {
						int skip = settings.pivSkip;
						if (f == 0)
							f = skip + 1;
						pivImg = new PivImg(jpiv, files[f - skip - 1], files[f]);
						System.out.println("Processing: "
								+ jpiv2.FileHandling.stripFilename(files[f
										- skip - 1]) + " and "
								+ jpiv2.FileHandling.stripFilename(files[f]));
						break;
					}
					case jpiv2.Settings.PIV_CASCADE: {
						if (f == 0)
							f = 1;
						pivImg = new PivImg(jpiv, files[0], files[f]);
						System.out.println("Processing: "
								+ jpiv2.FileHandling.stripFilename(files[0])
								+ " and "
								+ jpiv2.FileHandling.stripFilename(files[f]));
						break;
					}
					case jpiv2.Settings.PIV_PAIRS: {
						f++;
						pivImg = new PivImg(jpiv, files[f - 1], files[f]);
						System.out.println("Processing: "
								+ jpiv2.FileHandling
										.stripFilename(files[f - 1]) + " and "
								+ jpiv2.FileHandling.stripFilename(files[f]));
						break;
					}
					default: {
						pivImg = new PivImg(jpiv, files[f]);
						System.out.println("Processing: "
								+ jpiv2.FileHandling.stripFilename(files[f]));
					}
					}
					parallelCorrelation(pivImg, p, cores);
					synchronized(corr) {
						try {
							corr.wait();
						} catch (java.lang.InterruptedException e) {
							System.out.println( e.toString() );
						}
					}
					if (!settings.exportCorrFunctionOnlySumOfCorr
							|| f == files.length - 1) {
						checkExportCorrFunction(f, p);
					}
					if (!settings.pivSumOfCorr) {
						theData = calcDisplacements(p);
						p++;
						while (p < passes) {
							initCorrMap(p);
							System.out.println((p + 1) + " pass");
							theData = doPostprocessing(theData);
							theData.resample(x0, y0, x1, y1, horVecSpacing[p],
									verVecSpacing[p]);						
							shift = theData.getPivDataShift();
							if (settings.pivShearIntWindows)
								deriv = theData.getShearField();
							dim = theData.getDimension();
							parallelCorrelation(pivImg, p, cores);
							synchronized(corr) {
								try {
									corr.wait();
								} catch (java.lang.InterruptedException e) {
									System.out.println( e.toString() );
								}
							}
							if (!settings.exportCorrFunctionOnlySumOfCorr
									|| f == files.length - 1) {
								checkExportCorrFunction(f, p);
							}
							theData = calcDisplacements(p);
							p++;
						}
						lastDestFileName = destFileName
								+ fileNumFormat.format(f) + ".jvc";
						theData.writeDataToFile(lastDestFileName,
								settings.loadSaveTecplotHeader);
						jpiv.getListFrame().appendElement(lastDestFileName);
						// clean up for next evaluation
						p = 0;
						initCorrMap(p);
						initPreShift();
					}
				}
				if (!settings.pivSumOfCorr) {
					System.out.println("JPiv PIV evaluation finished.");
					return (lastDestFileName);
				}
				if (p < passes - 1) {
					theData = calcDisplacements(p);
					System.out.println((p + 2) + " pass");
					theData = doPostprocessing(theData);
					p++;
					theData.resample(x0, y0, x1, y1, horVecSpacing[p],
							verVecSpacing[p]);
					shift = theData.getPivDataShift();
					if (settings.pivShearIntWindows)
					 deriv = theData.getShearField();
					dim = theData.getDimension();
					initCorrMap(p);
					p--;
				}
				
			}
			p--;
			theData = calcDisplacements(p);
			lastDestFileName = destFileName + ".jvc";
			theData.writeDataToFile(lastDestFileName,
					settings.loadSaveTecplotHeader);
			jpiv.getListFrame().appendElement(lastDestFileName);
			System.out.println("PIV evaluation finished.");
		}
		return (lastDestFileName);
	}

	/**
	 * Return last used destination file name.
	 * 
	 * @return Last used destination file name
	 */
	public String getLastUsedDestFileName() {
		if (JPiv.verbosity) System.out.println("Called: PivEvaluation.getLastUsedDestFileName");
		return (destFileName);
	}
	
	
	private void parallelCorrelation(jpiv2.PivImg pivImg, int p, int cores) {
		if (JPiv.verbosity) System.out.println("Called: PivEvaluation.parallelCorrelation");
		int interval = shift.length / cores;
		int from = 0;
		AddCorrelation[] addCorrelation = new AddCorrelation[cores];
		
		for(int c = 0; c < cores-1; c++){
			addCorrelation[c] = new AddCorrelation();
			addCorrelation[c].setParams(pivImg, p, from, from+interval);
			addCorrelation[c].start();
			from+=interval;
		}
		addCorrelation[cores-1] = new AddCorrelation();
		addCorrelation[cores-1].setParams(pivImg, p, from, shift.length);
		addCorrelation[cores-1].start();
	}

	
	private class AddCorrelation extends Thread{
		
		private jpiv2.PivImg pivImg;
		private int p;
		private int from;
		private int to;
		
		public void setParams(jpiv2.PivImg pivImg, int p, int from, int to){
			this.pivImg = pivImg;
			this.p = p;
			this.from = from;
			this.to = to;
		}
		
		public void run() {
			if (JPiv.verbosity) System.out.println("Called: PivEvaluation.AddCorrelation.run");
			PlanarImage tmpCorr;
			PlanarImage tmpCorrSum;
			int x, y;
			for (int c = from; c < to; c++) {
				// without window deformation
				if (!settings.pivShearIntWindows || p == 0) {
					try {
						// central difference
						tmpCorr = PivUtil.correlate(
							pivImg.getSubImage(
								(float) (shift[c][0] - (intWinW[p] + shift[c][2]) / 2),
								(float) (shift[c][1] - (intWinH[p] + shift[c][3]) / 2),
								intWinW[p], intWinH[p], 0),
							pivImg.getSubImage(
								(float) (shift[c][0] - (intWinW[p] - shift[c][2]) / 2),
								(float) (shift[c][1] - (intWinH[p] - shift[c][3]) / 2),
								intWinW[p], intWinH[p], 1));
					} catch (java.lang.IllegalArgumentException tryBackwardDifference) {
						try {
							// backward difference
							tmpCorr = PivUtil.correlate(
								pivImg.getSubImage(
									(float) (shift[c][0]
									- intWinW[p] / 2 - shift[c][2]),
                                                                        (float) (shift[c][1]
									- intWinH[p] / 2 - shift[c][3]),
									intWinW[p], intWinH[p], 0),
								pivImg.getSubImage(
									(float) (shift[c][0] - intWinW[p] / 2),
									(float) (shift[c][1] - intWinH[p] / 2),
									intWinW[p], intWinH[p], 1));
						} catch (java.lang.IllegalArgumentException tryForwardDifference) {
							// forward difference
							try {
								tmpCorr = PivUtil.correlate(
                                                                        pivImg.getSubImage(
                                                                                (float) (shift[c][0] - intWinW[p] / 2),
                                                                                (float) (shift[c][1] - intWinH[p] / 2),
                                                                                intWinW[p], intWinH[p], 0), 
                                                                        pivImg.getSubImage((float) (shift[c][0]
										- intWinW[p] / 2 + shift[c][2]),
										(float) (shift[c][1] - intWinH[p]
										/ 2 + shift[c][3]),
										intWinW[p], intWinH[p], 1));
							} catch (java.lang.IllegalArgumentException fuckingHellNothingHelps) {
								// no preshift, this might happen in the corners of
								// an image, when
								// the window size is not reduced from pass to pass.
								tmpCorr = PivUtil.correlate(
									pivImg.getSubImage(
										(float) (shift[c][0] - intWinW[p] / 2),
										(float) (shift[c][1] - intWinH[p] / 2),
										intWinW[p], intWinH[p], 0),
									pivImg.getSubImage(
										(float) (shift[c][0] - intWinW[p] / 2),
										(float) (shift[c][1] - intWinH[p] / 2),
										intWinW[p], intWinH[p], 1));
								shift[c][2] = 0;
								shift[c][3] = 0;
							}
						}
					}
				}
				// with window deformation (experimental!!!)
				else {
					y = (int) (c / dim[0]);
					x = c - y * dim[0];
					try {
						// central difference
						tmpCorr = PivUtil
								.correlate(
										pivImg.getSubImage(
												(float) (shift[c][0] - (intWinW[p] + shift[c][2]) / 2),
												(float) (shift[c][1] - (intWinH[p] + shift[c][3]) / 2),
												intWinW[p], intWinH[p],
												(float) deriv[y][x][0] / 2,
												(float) deriv[y][x][1] / 2, 0),
										pivImg.getSubImage(
												(float) (shift[c][0] - (intWinW[p] - shift[c][2]) / 2),
												(float) (shift[c][1] - (intWinH[p] - shift[c][3]) / 2),
												intWinW[p], intWinH[p],
												(float) -deriv[y][x][0] / 2,
												(float) -deriv[y][x][1] / 2, 1));
					} catch (java.lang.IllegalArgumentException tryBackwardDifference) {
						try {
							// backward difference
							tmpCorr = PivUtil
									.correlate(
											pivImg.getSubImage(
													(float) (shift[c][0]
															- intWinW[p] / 2 - shift[c][2]),
													(float) (shift[c][1]
															- intWinH[p] / 2 - shift[c][3]),
													intWinW[p], intWinH[p],
													(float) deriv[y][x][0] / 2,
													(float) deriv[y][x][1] / 2, 0),
											pivImg.getSubImage(
													(float) (shift[c][0] - intWinW[p] / 2),
													(float) (shift[c][1] - intWinH[p] / 2),
													intWinW[p], intWinH[p],
													(float) -deriv[y][x][0] / 2,
													(float) -deriv[y][x][1] / 2, 1));
						} catch (java.lang.IllegalArgumentException tryForwardDifference) {
							// forward difference
							try {
								tmpCorr = PivUtil.correlate(pivImg.getSubImage(
										(float) (shift[c][0] - intWinW[p] / 2),
										(float) (shift[c][1] - intWinH[p] / 2),
										intWinW[p], intWinH[p],
										(float) deriv[y][x][0] / 2,
										(float) deriv[y][x][1] / 2, 0), pivImg
										.getSubImage((float) (shift[c][0]
												- intWinW[p] / 2 + shift[c][2]),
												(float) (shift[c][1] - intWinH[p]
														/ 2 + shift[c][3]),
												intWinW[p], intWinH[p],
												(float) -deriv[y][x][0] / 2,
												(float) -deriv[y][x][1] / 2, 1));
							} catch (java.lang.IllegalArgumentException fuckingHellNothingHelps) {
								// no preshift, this might happen in the corners of
								// an image, when
								// the window size is not reduced from pass to pass.
								tmpCorr = PivUtil
										.correlate(
												pivImg.getSubImage(
														(float) (shift[c][0] - intWinW[p] / 2),
														(float) (shift[c][1] - intWinH[p] / 2),
														intWinW[p], intWinH[p],
														(float) deriv[y][x][0] / 2,
														(float) deriv[y][x][1] / 2,
														0),
												pivImg.getSubImage(
														(float) (shift[c][0] - intWinW[p] / 2),
														(float) (shift[c][1] - intWinH[p] / 2),
														intWinW[p],
														intWinH[p],
														(float) -deriv[y][x][0] / 2,
														(float) -deriv[y][x][1] / 2,
														1));
								shift[c][2] = 0;
								shift[c][3] = 0;
							}
						}
					}
				}
				// add correlation matrix
                                synchronized(corr) {
                                        ParameterBlock pb = new ParameterBlock();
                                        pb.addSource(corr[c]);
                                        pb.addSource(tmpCorr);
                                        tmpCorrSum = JAI.create("add", pb, null);
                                        corr[c] = tmpCorrSum.getAsBufferedImage();
				
					if(c == shift.length-1) {
						corr.notify();
					}
				}
			}
		}
	}

	private void checkExportCorrFunction(int file, int p) {
		if (JPiv.verbosity) System.out.println("Called: PivEvaluation.checkExportCorrFunction");
		// save sample function of one specific or all passes
		if (settings.exportCorrFunction
				&& settings.exportCorrFunctionNum != -1
				&& (settings.exportCorrFunctionPass == p || settings.exportCorrFunctionPass == -1))
			exportCorrFunction(corr[settings.exportCorrFunctionNum],
					"_corrMap_file" + file + "_pass" + p + "_vector"
							+ settings.exportCorrFunctionNum);
		// save all functions of one specific or all passes
		if (settings.exportCorrFunction
				&& settings.exportCorrFunctionNum == -1
				&& (settings.exportCorrFunctionPass == p || settings.exportCorrFunctionPass == -1))
			for (int i = 0; i < corr.length; i++)
				exportCorrFunction(corr[i], "_corrMap_file" + file + "_pass"
						+ p + "_vector" + i);
	}

	private void exportCorrFunction(BufferedImage pi, String name) {
		if (JPiv.verbosity) System.out.println("Called: PivEvaluation.exportCorrFunction");
		String baseName = FileHandling.stripExtension(destFileName);
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(pi);
		pb.add(baseName + name + "." + "tif");
		pb.add("tiff");
		JAI.create("filestore", pb);
	}

	private jpiv2.PivData doPostprocessing(jpiv2.PivData theData) {
		if (JPiv.verbosity) System.out.println("Called: PivEvaluation.doPostProcessing");
		if (settings.pivNormMedianTest) {
			theData.normalizedMedianTest(settings.normMedianTestNoiseLevel,
					settings.normMedianTestThreshold);
		}
		if (settings.pivReplace) {
			theData.replaceByMedian(false, false);
		}
		if (settings.pivMedian) {
			theData.replaceByMedian(true, true);
		}
		if (settings.pivSmoothing) {
			theData.smooth(true);
		}
		return (theData);
	}

	private jpiv2.PivData calcDisplacements(int p) {
		if (JPiv.verbosity) System.out.println("Called: PivEvaluation.calcDispacements");
		double[] peak;
		for (int r = 0; r < shift.length; r++) {
			if (settings.pivSumOfCorr) {
				peak = PivUtil.getParabolicPeak(
						PlanarImage.wrapRenderedImage(corr[r]), intWinW[p] / 2
								- searchAreaW[p] / 2, intWinH[p] / 2
								- searchAreaH[p] / 2, searchAreaW[p],
						searchAreaH[p]);
			} else {
				peak = PivUtil.getGaussianPeak(
						PlanarImage.wrapRenderedImage(corr[r]), intWinW[p] / 2
								- searchAreaW[p] / 2, intWinH[p] / 2
								- searchAreaH[p] / 2, searchAreaW[p],
						searchAreaH[p]);
			}
			// valid
			if (peak[2] != -1) {
				shift[r][2] = shift[r][2] + peak[0] - ((double) intWinW[p]) / 2.0;
				shift[r][3] = shift[r][3] + peak[1] - ((double) intWinH[p]) / 2.0;
			}
			// invalid
			else {
				shift[r][2] = 0;
				shift[r][3] = 0;
			}
			shift[r][4] = peak[2];
		}
		return (new PivData(shift));
	}

	private void initVariables(int p) {
		if (JPiv.verbosity) System.out.println("Called: PivEvaluation.initVariables");
		settings = jpiv.getSettings();
		jpiv2.PivImg img = new PivImg(jpiv, files[0]);
		double imgWidth = img.getWidth();
		double imgHeight = img.getHeight();
		if (settings.pivSequence == Settings.PIV_TWO_IMG)
			imgHeight = imgHeight / 2;
		if (settings.pivUseImageBaseName) {
			destFileName = files[0];
			int index = destFileName.lastIndexOf('.');
			if (index != -1)
				destFileName = destFileName.substring(0, index);
		}
		if (settings.pivUseDefaultDestFileName) {
			destFileName = settings.pivDefaultDestFileName;
		}
		passes = settings.pivMultiPass;
		intWinW = settings.pivWindow[0];
		intWinH = settings.pivWindow[1];
		searchAreaW = settings.pivWindow[2];
		searchAreaH = settings.pivWindow[3];
		horVecSpacing = settings.pivWindow[4];
		verVecSpacing = settings.pivWindow[5];
		horPreShift = settings.pivHorPreShift;
		verPreShift = settings.pivVerPreShift;
		if (settings.pivROI) {
			x0 = settings.pivROIP1x + intWinW[0] / 2;
			y0 = settings.pivROIP1y + intWinH[0] / 2;
			x1 = settings.pivROIP1x
					+ (int) ((settings.pivROIP2x - settings.pivROIP1x) / intWinW[0])
					* intWinW[0] - intWinW[0] / 2;
			y1 = settings.pivROIP1y
					+ (int) ((settings.pivROIP2y - settings.pivROIP1y) / intWinH[0])
					* intWinH[0] - intWinH[0] / 2;
		} else {
			x0 = intWinW[0] / 2;
			y0 = intWinH[0] / 2;
			x1 = (int) (imgWidth / intWinW[0]) * intWinW[0] - intWinW[0] / 2;
			y1 = (int) (imgHeight / intWinH[0]) * intWinH[0] - intWinH[0] / 2;
		}
		initCorrMap(p);
		initPreShift();
		// decimal format for destination file numbering
		int digits = 1 + (int) (Math.log(files.length) / Math.log(10));
		String format = new String();
		for (int i = 0; i < digits; i++)
			format += "0";
		fileNumFormat = new java.text.DecimalFormat(format);
	}

	private void initPreShift() {
		if (JPiv.verbosity) System.out.println("Called: PivEvaluation.initPreShift");
		jpiv2.PivData pivData = new jpiv2.PivData(x0, y0, horVecSpacing[0],
				verVecSpacing[0], nx, ny, horPreShift, verPreShift, 0);
		shift = pivData.getPivDataShift();
	}

	private void initCorrMap(int p) {
		if (JPiv.verbosity) System.out.println("Called: PivEvaluation.initCorrMap");
		nx = (x1 - x0) / horVecSpacing[p] + 1;
		ny = (y1 - y0) / verVecSpacing[p] + 1;
		corr = new BufferedImage[nx * ny];
		Float[] bandValues = new Float[1];
		bandValues[0] = 0f;
		ParameterBlock pb = new ParameterBlock();
		pb.add(new Float(intWinW[p]));
		pb.add(new Float(intWinH[p]));
		pb.add(bandValues);
		for (int c = 0; c < corr.length; c++) {
			corr[c] = JAI.create("constant", pb).getAsBufferedImage();
		}
	}

	private boolean chooseDestFileName() {
		if (JPiv.verbosity) System.out.println("Called: PivEvaluation.chooseDestFileName");
		jpiv2.FlexFileChooser flexFileChooser = jpiv.getFlexFileChooser();
		flexFileChooser.setFiletype(FlexFileChooser.JVC, false);
		int approve = flexFileChooser.showSaveDialog(jpiv);
		if (approve == javax.swing.JFileChooser.APPROVE_OPTION) {
			destFileName = flexFileChooser.getSelectedFile().toString();
			int index = destFileName.lastIndexOf('.');
			if (index != -1)
				destFileName = destFileName.substring(0, index);
			return (true);
		} else
			return (false);
	}

}
