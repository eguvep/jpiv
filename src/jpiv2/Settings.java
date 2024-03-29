/*
 * Settings.java
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

import java.io.Serializable;

/**
 * Serializable object encapsulating all program settings.
 * 
 */
public class Settings implements Serializable {

	/**
	 * Creates a new instance of Settings.
	 */
	public Settings() {
	}

	// Load/Save
	/**
	 * Set <code>true</code> to save vector files with a Tecplot header.
	 */
	public boolean loadSaveTecplotHeader = false;

	// Preferences - Cmd
	/**
	 * Maximum number of lines in history.
	 */
	public int cmdNumOfLines = 1000;
	/**
	 * Set <code>true</code> to wrap lines. A horizontal scroll bar will be
	 * shown otherwise to display long lines.
	 */
	public boolean cmdLineWrap = true;

	// Preferences - Paths
	/**
	 * Location of additional files, like native libraries, documentation,
	 * scripts.
	 */
        java.util.Properties sysProp = System.getProperties();
	public String jpivLibPath = sysProp.getProperty("user.dir") +
                                    sysProp.getProperty("file.separator") +
                                    "jpivlib";

	// Preferences - Vector
	/**
	 * Scaling factor for plotting vectors.
	 */
	public float vectorScale = 4f;
	/**
	 * Fraction of vector length used by the arrow head.
	 */
	public double vectorHeadLength = 0.45;
	/**
	 * Opening angle of the arrow head.
	 */
	public int vectorAngle = 30;
	/**
	 * Color of the vector plot background.
	 */
	public java.awt.Color vectorBackground = java.awt.Color.DARK_GRAY;
	/**
	 * Set <code>true</code> to use a pixel image as background.
	 */
	public boolean vectorBackgroundImage = false;
	/**
	 * Location of the background image.
	 */
	public String vectorBackgroundImagePath = "path";

	/**
	 * Define a path for the background image for vector plots.
	 * 
	 * @param path
	 *            The complete path of the background image.
	 */
	public void setVectorBackgroundImagePath(String path) {
		vectorBackgroundImagePath = path;
		System.out.println("set vectorBackgroundImagePath = "
				+ vectorBackgroundImagePath);
	}

	/**
	 * Set <code>true</code> to create a color coded background, based on the
	 * values in the specified data column (e.g. vorticity).
	 */
	public boolean vectorBackgroundColorCoding = false;
	/**
	 * Specifys the column number used for the color coded background, zero for
	 * the first column.
	 */
	public int vectorBackgroundColorCodingColumn = 4;
	/**
	 * Value used for the lowest color (blue).
	 */
	public float vectorBackgroundColorCodingMin = -1f;
	/**
	 * Value used for the highest color (red).
	 */
	public float vectorBackgroundColorCodingMax = 1f;
	/**
	 * Color of the vectors.
	 */
	public java.awt.Color vectorColor = java.awt.Color.CYAN;
	/**
	 * Color of invalid vectors (negative value in fifth column).
	 */
	public java.awt.Color vectorColorInvalid = java.awt.Color.MAGENTA;
	/**
	 * Color of shapes (e.g. profiles).
	 */
	public java.awt.Color shapeColor = java.awt.Color.ORANGE;
	/**
	 * Set <code>true</code> to plot invalid vectors in the specified color.
	 */
	public boolean labelInvalidVectors = false;
	/**
	 * Scaling factor for the vector plot.
	 */
	public float vectorZoom = 1.0f;
	/**
	 * Set <code>true</code> to plot a reference vector.
	 */
	public boolean vectorShowReference = true;
	/**
	 * Conversion factor to draw the reference vector in a different unit than
	 * pixel.
	 */
	public float vectorReferenceUnitConversion = 1f;
	/**
	 * Length of the reference vector in pixel.
	 */
	public float vectorReferenceLength = 10f;
	/**
	 * Label of the reference vector (e.g. the unit used).
	 */
	public String vectorReferenceLabel = "pixel";
	/**
	 * Set <code>true</code> to use a color code for the vector length.
	 */
	public boolean vectorMultiColor = true;
	/**
	 * Value represented by the lowest color (blue).
	 */
	public float vectorMultiColorMin = 0f;
	/**
	 * Value represented by the highest color (red).
	 */
	public float vectorMultiColorMax = 10f;
	/**
	 * Transparancy (0=fully transparent, 1=fully opaque).
	 */
	public float vectorMultiColorTransparancy = 1.0f;
	/**
	 * Data type for PIV images. 
	 * One of the constants defined in java.awt.image.DataBuffer.
	 */
	public int imgType = java.awt.image.DataBuffer.TYPE_USHORT;
	/**
	 * Enable transparency for the multi colors.
	 * 
	 * @param f
	 *            A value between 0 (completely transparent) and 1 (opaque).
	 */
	public void setVectorMultiColorTransparancy(float f) {
		vectorMultiColorTransparancy = f;
		System.out.println("set vectorMultiColorTransparancy = "
				+ vectorMultiColorTransparancy);
	}

	/**
	 * Set <code>true</code> to display a color legend.
	 */
	public boolean vectorShowColorBar = true;
	/**
	 * Define a decimal format for the color bar. Default is scientific format:
	 * ##0.000E00.
	 */
	public String vectorColorBarFormatString = "##0.000E00";

	/**
	 * Set the number format for the color bar next to a color coded vector
	 * plot.
	 * 
	 * @param format
	 *            A format string. The default is "##0.000E00"
	 * @see <a
	 *      href="http://java.sun.com/j2se/1.5.0/docs/api/java/text/DecimalFormat.html">DecimalFormat</a>
	 */
	public void setVectorColorBarFormatString(String format) {
		vectorColorBarFormatString = format;
		System.out.println("set vectorColorBarFormatString = "
				+ vectorColorBarFormatString);
	}

	/**
	 * Conversion factor of the color bar labels to assign the colors to values
	 * in a different unit than pixel.
	 */
	public float vectorColorBarUnitConversion = 1f;
	/**
	 * Label of the color legend (e.g. the used unit).
	 */
	public String vectorColorBarLabel = "pixel";
	/**
	 * The background color outside the vector field.
	 */
	public java.awt.Color vectorBorder = java.awt.Color.WHITE;

	/**
	 * Set the background color outside the vector field.
	 * 
	 * @param color
	 *            The color.
	 */
	public void setVectorBorder(java.awt.Color color) {
		vectorBorder = color;
		System.out.println("set setVectorBorder = " + vectorBorder);
	}

	/**
	 * The color of the plot axes.
	 */
	public java.awt.Color vectorAxes = java.awt.Color.BLACK;

	/**
	 * Set the color of the axis and labels.
	 * 
	 * @param color
	 *            The color.
	 */
	public void setVectorAxes(java.awt.Color color) {
		vectorAxes = color;
		System.out.println("set setVectorAxes = " + vectorAxes);
	}

	/**
	 * The spacing of the axes tickmarks.
	 */
	public float vectorTickSpacing = 100f;
	/**
	 * The scaling of the axis labels (e.g. for unit conversion).
	 */
	public float vectorAxisScaling = 1f;
	/**
	 * The label of the axes (e.g. the unit).
	 */
	public String vectorAxisLabel = "[pixel]";
	/**
	 * The number of color levels for the color coding.
	 */
	public int vectorColorLevels = 10;
	/**
	 * The width of the space between plot border and left upright axis.
	 */
	public int vectorXOffset = 80;
	/**
	 * The space between plot border and the top horizontal axis.
	 */
	public int vectorYOffset = 40;
	/**
	 * If true, print all vectors with a uniform length.
	 */
	public boolean vectorUniformLength = false;
	/**
	 * The uniform length, if vectorUniformLength=True
	 */
	public float vectorLength = 10;
	/**
	 * Use scaling below this threshold, if vectorUniformLength=True
	 */
	public float vectorLengthThreshold = 1;

	// Preferences - Img
	/**
	 * Treat imges as double frame imgages if <code>true</code>.
	 */
	public boolean imgTwoFrames = true;
	/**
	 * Zoom factor for image display.
	 */
	public float imgZoom = 1.0f;

	// PIV - General
	/**
	 * Constant. PIV evaluation between frames in double frame images.
	 */
	public static final int PIV_TWO_IMG = 1;
	/**
	 * Constant. PIV evaluation between consecutive images in a list.
	 */
	public static final int PIV_CONSECUTIVE = 2;
	/**
	 * Constant. PIV evaluation between single images with a number of skipped
	 * images in between.
	 */
	public static final int PIV_SKIP = 3;
	/**
	 * Constant. PIV evaluation between single images with increasing number of
	 * skipped images in between.
	 */
	public static final int PIV_CASCADE = 4;
	/**
	 * Constant. PIV evaluation between consecutive image pairs.
	 */
	public static final int PIV_PAIRS = 5;
	/**
	 * One of the constants PIV_TOW_IMG, PIV_CONSECUTIVE, PIV_PAIRS, PIV_SKIP,
	 * PIV_CASCADE to define which images to correlate with each other.
	 */
	public int pivSequence = PIV_TWO_IMG;
	/**
	 * The number of images to skip if pivSequence==PIV_SKIP.
	 */
	public int pivSkip = 0;
	/**
	 * Sum up all correlation functions of an ensemble before searching the
	 * correlation peak position.
	 */
	public boolean pivSumOfCorr = false;
	/**
	 * Use window deformation to enhance accuracy.
	 */
	public boolean pivShearIntWindows = false;

	/**
	 * Enable (true) or disable (false) window deformation.
	 * 
	 * @param shearIntWindows
	 *            Deform interrogation windows (true) or not (false).
	 */
	public void setPivShearIntWindows(boolean shearIntWindows) {
		pivShearIntWindows = shearIntWindows;
	}

	/**
	 * Set <code>true</code> to use the image base name as the default output
	 * filename.
	 */
	public boolean pivUseImageBaseName = false;

	/**
	 * Do not ask for a vector file name, use the image name.
	 * 
	 * @param useImageBaseName
	 *            Do not ask the user for the output file name, if true.
	 */
	public void setPivUseImageBaseName(boolean useImageBaseName) {
		pivUseImageBaseName = useImageBaseName;
		System.out.println("set pivUseImageBaseName = " + pivUseImageBaseName);
	}

	/**
	 * Set <code>true</code> to use a default or software set destination file
	 * name.
	 */
	public boolean pivUseDefaultDestFileName = false;

	/**
	 * Do not ask for a vector file name, use the default file name.
	 * 
	 * @param useDefaultDestFileName
	 *            Do not ask the user for the output file name, if true.
	 */
	public void setPivUseDefaultDestFileName(boolean useDefaultDestFileName) {
		pivUseDefaultDestFileName = useDefaultDestFileName;
		System.out.println("set pivUseDefaultDestFileName = "
				+ pivUseDefaultDestFileName);
	}

	public String pivDefaultDestFileName = "deleteme";

	/**
	 * Set the default file name for PIV output
	 * 
	 * @param defaultDestFileName
	 *            The new default filename for output.
	 */
	public void setPivDefaultDestFileName(String defaultDestFileName) {
		pivDefaultDestFileName = defaultDestFileName;
		System.out.println("set pivDefaultDestFileName = "
				+ pivDefaultDestFileName);
	}

	// PIV - Interrogation Window
	/**
	 * Number of passes.
	 */
	public int pivMultiPass = 3;
	/**
	 * Definition of the interrogation window size, search domain size and
	 * vector spacing.
	 */
	public int[][] pivWindow = { { 64, 32, 32 }, { 64, 32, 32 }, { 32, 8, 8 },
			{ 32, 8, 8 }, { 32, 16, 12 }, { 32, 16, 12 } };
	/**
	 * Constricts the PIV evaluation on a region of interest if
	 * <code>true</code>.
	 */
	public boolean pivROI = false;
	/**
	 * Upper, left boundary of region of interest.
	 */
	public int pivROIP1x = 0;
	/**
	 * Upper, top boundary of region of interest.
	 */
	public int pivROIP1y = 0;
	/**
	 * Lower, right boundary of region of interest.
	 */
	public int pivROIP2x = 512;
	/**
	 * Lower, bottom boundary of region of interes.
	 */
	public int pivROIP2y = 512;
	/**
	 * Horizontal pre-shift of the interrogation window during the first pass.
	 */
	public int pivHorPreShift = 0;
	/**
	 * Vertical pre-shift of the interrogation window during the first pass.
	 */
	public int pivVerPreShift = 0;
	/**
	 * Perform a normalized median test between the passes.
	 */
	public boolean pivNormMedianTest = true;
	/**
	 * Replace invaldid vectors by the median between the passes.
	 */
	public boolean pivReplace = true;
	/**
	 * Apply a median filter between the passes.
	 */
	public boolean pivMedian = false;
	/**
	 * Use 3x3 smoothing (average) between the passes.
	 */
	public boolean pivSmoothing = true;

	// PIV - Single Pixel Correlation
	/**
	 * During single pixel ensemble evaluation, treat images as double frame
	 * images.
	 */
	public boolean singlePixelTwoImg = false;
	/**
	 * Constrict the single pixel ensemble evaluation to a region of interest.
	 */
	public boolean singlePixelROI = true;
	/**
	 * Upper, left border of region of interst.
	 */
	public int singlePixelROIP1x = 0;
	/**
	 * Upper, top border of region of interst.
	 */
	public int singlePixelROIP1y = 0;
	/**
	 * Lower, right border of region of interst.
	 */
	public int singlePixelROIP2x = 64;
	/**
	 * Lower, bottom border of region of interest.
	 */
	public int singlePixelROIP2y = 64;
	/**
	 * Search domain widht for single pixel correlation.
	 */
	public int singlePixelDomainWidth = 7;
	/**
	 * Search domain hight for single pixel correlation.
	 */
	public int singlePixelDomainHeight = 7;
	/**
	 * Horizontal pre-shift for single pixel correlation.
	 */
	public int singlePixelHorPreShift = 0;
	/**
	 * Vertical pre-shift for single pixel correlation.
	 */
	public int singlePixelVerPreShift = 0;
	/**
	 * First do a standard-PIV evaluation to create a pre-shift vector field, if
	 * <code>true</code>.
	 */
	public boolean singlePixelCreatePreShiftVectorField = false;
	/**
	 * Use 3x3 pixel direct correlation, instead of single pixel correlation.
	 * Reduces the amount of neccessary images by a factor of about two.
	 */
	public boolean singlePixel3by3 = false;
	/**
	 * Considers only pixel whose value is above the average value. Speeds up
	 * the calculation and significantly reduces noise. The threshold might be
	 * chosen a bit to high, this might be object of optimization in a future
	 * version.
	 */
	public boolean singlePixelSignalOnly = true;

	// Vector Processing - Profile Extraction
	/**
	 * If <code>true</code>, print some statistics to standard output, when a
	 * profile is drawn into a vector plot.
	 */
	public boolean profilePrintStat = true;
	/**
	 * Prints the data of the interpolated profile to standard output, if
	 * <code>true</code>.
	 */
	public boolean profilePrintData = false;
	/**
	 * Use a quadratic interpolation between the points of a profile.
	 */
	public boolean profileQuadInterp = true;
	/**
	 * Calculate the average of a number of parallel profiles.
	 */
	public boolean profileAverage = false;
	/**
	 * Number of profiles to average.
	 */
	public int profileNumberOf = 3;
	/**
	 * Distance between profiles.
	 */
	public int profileDistance = 16;
	/**
	 * Create a fixed number of data points, independent on the length of the
	 * profile.
	 */
	public boolean profileFixDataPoints = false;
	/**
	 * Specify the number of data points along the profile.
	 */
	public int profileNumOfFixDataPoints = 16;

	// Vector Processing - Normalized Median Test
	/**
	 * Noise Level of the normalized median test (0.1 is a good starting point).
	 */
	public double normMedianTestNoiseLevel = 0.1;
	/**
	 * Threshold of the normalized median test (2 is a good starting point).
	 */
	public double normMedianTestThreshold = 2.0;

	// Vector Processing - Remove Isolated Vectors
	/**
	 * Invalidate a vector if it has less direct valid neighbours than
	 * specified.
	 */
	public int invalidateIsolatedVectorsNumOfNeighbours = 3;

	// Vector Processing - Substract Reference Displacement
	/**
	 * Substract a reference displacement, horizontal component.
	 */
	public double subsRefDispDx = 0;
	/**
	 * Substract a reference displacement, vertical component.
	 */
	public double subsRefDispDy = 0;

	// Vector Processing - Third Component Reconstruction
	/**
	 * Distance between measurement planes (for third component reconstruction
	 * based on mass conservation).
	 */
	public double thirdCompReconDz = 16.0;

	/**
	 * Specify the distance between two measurement planes for the
	 * reconstruction of the third component (based on the assumption of
	 * mass-conservation).
	 * 
	 * @param dz
	 *            The distance between to measurement planes.
	 */
	public void setThirdCompReconDz(double dz) {
		thirdCompReconDz = dz;
		System.out.println("set thirdCompReconDz = " + thirdCompReconDz);
	}

	/**
	 * Skip measurement planes (for third component reconstruction based on mass
	 * conservation).
	 */
	public int thirdCompReconSkip = 0;

	/**
	 * Specify the number of measurement planes to skip.
	 * 
	 * @param skip
	 *            The number of planes to skip, 0 for using every plane
	 */
	public void setThirdCompReconSkip(int skip) {
		thirdCompReconSkip = skip;
		System.out.println("set thirdCompReconSkip = " + thirdCompReconSkip);
	}

	/**
	 * The first step of the third component reconstruction is the calculation
	 * of the first derivative of the velocity field. Normally this calculation
	 * uses central differences (thirdCompReconLinReg = false). You may try
	 * calculating the first derivative based on a linear regression of five
	 * points (thirdCompReconLinReg = true). This method is more robust against
	 * noise.
	 */
	public boolean thirdCompReconLinReg = false;

	/**
	 * Specify whether to base the third component reconstruction on central
	 * differences or on a linear regression over five points.
	 * 
	 * @param useLinReg
	 *            True for using a linear regression instead of central
	 *            differences.
	 */
	public void setThirdCompReconLinReg(boolean useLinReg) {
		thirdCompReconLinReg = useLinReg;
		System.out
				.println("set thirdCompReconLinReg = " + thirdCompReconLinReg);
	}

	/**
	 * If true, a sample ensemble correlation functions is exported every loop.
	 */
	public boolean exportCorrFunction = false;
	/**
	 * Number of the correlation function to export (starting from upper left
	 * corner).
	 */
	public int exportCorrFunctionNum = 1;
	/**
	 * Specify pass when to export the correlation function.
	 */
	public int exportCorrFunctionPass = 1;
	/**
	 * Restrict export to final (summed up) correlation function.
	 */
	public boolean exportCorrFunctionOnlySumOfCorr = false;

	/**
	 * Specify the export of sample correlation functions into the users home
	 * directory.
	 * 
	 * @param export
	 *            If true, a sample ensemble correlation function is exported.
	 * @param num
	 *            Number of the correlation function to export (starting with
	 *            zero from upper left corner, -1 for all).
	 * @param pass
	 *            Specify the pass when to export the correlation function,
	 *            starting with zero, -1 for all.
	 * @param onlySumOfCorr
	 *            If true, export is only applied after all correlations are
	 *            summed up.
	 */
	public void setExportCorrFunction(boolean export, int num, int pass,
			boolean onlySumOfCorr) {
		exportCorrFunction = export;
		exportCorrFunctionNum = num;
		exportCorrFunctionPass = pass;
		exportCorrFunctionOnlySumOfCorr = onlySumOfCorr;
		System.out.println("set exportCorrFunction = " + exportCorrFunction);
		System.out.println("set exportCorrFunctionNum = "
				+ exportCorrFunctionNum);
		System.out.println("set exportCorrFunctionNum = "
				+ exportCorrFunctionPass);
		System.out.println("set exportCorrFunctionOnlySumOfCorr = "
				+ exportCorrFunctionOnlySumOfCorr);
	}
	
	public void printFields(){
		java.lang.reflect.Field[] fields = this.getClass().getFields();
		for(int i=0;i<fields.length;++i){
			System.out.println(fields[i].toString());
		}
		
	}
		
}
