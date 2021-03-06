/*
 * DataSet.java
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

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * This class provides some functionality for 2D-PIV-data processing. PIV-data
 * is organized in at least four columns. The first column contains the
 * x-coordinate and the second the y-coordinate of a data point. The third and
 * fourth columns contain the x- and the y-velocity components. Other columns
 * might contain some additional information. The columns are separated by
 * whitespaces. The data type is UTF-8. The file might contain a comments
 * or a header <br>
 * e.g:<br>
 * <code>TITLE = "Example"<br>
 * VARIABLES = "x", "y", "ux", "uy", "s"<br>
 * ZONE I=3, J=3, F=POINT<br>
 * # comment<br>
 * 16 16 +0.4 +0.1 0.94<br>
 * 32 16 +0.9 +0.0 0.92<br>
 * 48 16 +1.2 -0.1 0.93<br>
 * 16 32 +1.7 -0.0 0.97<br>
 * 32 32 +2.1 +0.2 0.89<br>
 * 48 32 +1.6 -0.0 0.92<br>
 * 16 48 +1.2 -0.1 0.91<br>
 * 32 48 +1.0 -0.0 0.91<br>
 * 48 48 +0.1 +0.0 0.94</code><br>
 */
public class PivData {

	// the data
	private double[][] pivData = null;
	// total number of vectors in horizontal direction
	private int horSize;
	// total number of vectors in vertical direction
	private int verSize;
	// horizontal vector spacing
	private int horSpacing;
	// vertical vector spacing
	private int verSpacing;
	// tecplot style header
	private String header = "";

	/**
	 * Construction of a <code>jpiv2.PivData</code> object from a single file.
	 * 
	 * @param pathname
	 *            Absolute pathname of a file containing PIV-data.
	 */
	public PivData(String pathname) {
		init(pathname);
	}

	/**
	 * Construction of a <code>jpiv2.PivData</code> object from a list of files.
	 * Only the displacement components (third and fourth column) are averaged.
	 * All other columns are taken from the first file.
	 * 
	 * @param pathnames
	 *            An array containing absolute pathnames of files containing
	 *            PIV-data.
	 */
	public PivData(String[] pathnames) {
		init(pathnames[0]);
		for (int f = 1; f < pathnames.length; f++) {
			this.add(new PivData(pathnames[f]));
		}
		for (int i = 0; i < pivData.length; ++i) {
			pivData[i][2] /= pathnames.length;
			pivData[i][3] /= pathnames.length;
		}
	}

	private void init(String pathname) {
		try {
			if ((FileHandling.getFileExtension(pathname)).equals("nc")) {
			//	this.pivData = FileHandling.readNetCdfFile(pathname);
				System.err.println("NetCDF not yet supported.");
			} else {
				this.pivData = FileHandling.readArrayFromFile(pathname);
				if (pivData[0].length < 5)
					appendColumn(1.0);
			}
		} catch (IOException e) {
			System.err
					.println("The specified file could not be interpreted as vector data.");
			this.pivData = getDefaultData();
		}
		try {
			getVectorFieldSize();
			createHeader();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err
					.println("The content of this file could not be interpreted as a vector field.");
			this.pivData = getDefaultData();
			getVectorFieldSize();
			createHeader();
		}
	}

	/**
	 * Construction of PIV-data from a two dimensional array.
	 * 
	 * @param data
	 *            A two dimensional array containing PIV-data.
	 */
	public PivData(double[][] data) {
		pivData = data;
		getVectorFieldSize();
		createHeader();
	}

	/**
	 * Construction of PIV-data from initial parameters
	 * 
	 * @param x0
	 *            x-coordinate of first vector location
	 * @param y0
	 *            y-coordinate of first vector location
	 * @param dx
	 *            horizontal vector spacing
	 * @param dy
	 *            vertical vector spacing
	 * @param nx
	 *            number of horizontal nodes
	 * @param ny
	 *            number of vertical nodes
	 * @param ux
	 *            initial value for horizontal displacement
	 * @param uy
	 *            initial value for vertical displacement
	 * @param s
	 *            inital value for fifth column
	 */
	public PivData(int x0, int y0, int dx, int dy, int nx, int ny, double ux,
			double uy, double s) {
		this.horSize = nx;
		this.verSize = ny;
		this.horSpacing = dx;
		this.verSpacing = dy;
		this.pivData = new double[nx * ny][5];
		for (int r = 0; r < nx * ny; ++r) {
			pivData[r][0] = (r % nx) * dx + x0;
			pivData[r][1] = ((int) r / nx) * dy + y0;
			pivData[r][2] = ux;
			pivData[r][3] = uy;
			pivData[r][4] = s;
		}
		createHeader();
	}

	/**
	 * Returns the PIV-data set as a two dimensional array.
	 * 
	 * @return A two dimensional array containing PIV-data.
	 */
	public double[][] getPivData() {
		return (pivData);
	}

	/**
	 * Returns the PIV-data set as a two dimensional array. The displacement is
	 * rounded to the closest even, integer value. This data may be used for
	 * integer pre-shifts of interrogation windows.
	 * 
	 * @return A two dimensional array containing PIV-data.
	 */
	public double[][] getPivDataShift() {
		double[][] pivDataShift = (double[][]) pivData.clone();
		for (int i = 0; i < pivData.length; i++) {
			pivDataShift[i][2] = Math.rint(pivData[i][2] / 2) * 2;
			pivDataShift[i][3] = Math.rint(pivData[i][3] / 2) * 2;
		}
		return (pivDataShift);
	}

	/**
	 * Adds annother PIV data set to the current data. Only the two displacement
	 * components are added to the currend PIV data set.
	 * 
	 * @param pivDataToAdd
	 *            The data to add.
	 */
	public void add(PivData pivDataToAdd) {
		double[][] arrayToAdd = pivDataToAdd.getPivData();
		int[] dimension = pivDataToAdd.getDimension();
		if (horSize != dimension[0] || verSize != dimension[1]) {
			System.err
					.println("The size of the PIV vector fields do not match.");
			return;
		}
		for (int i = 0; i < pivData.length; ++i) {
			pivData[i][2] += arrayToAdd[i][2];
			pivData[i][3] += arrayToAdd[i][3];
		}
	}

	/**
	 * Substract a constant reference displacement.
	 * 
	 * @param dxRef
	 *            horizontal reference velocity
	 * @param dyRef
	 *            vertical reference velocity
	 */
	public void subsRefDisp(double dxRef, double dyRef) {
		for (int i = 0; i < pivData.length; i++) {
			pivData[i][2] -= dxRef;
			pivData[i][3] -= dyRef;
		}
	}

	/**
	 * Remove invalid vectors (set velocity components to zero).
	 */
	public void removeInvalidVectors() {
		for (int i = 0; i < pivData.length; ++i) {
			if (pivData[i][4] <= 0) {
				pivData[i][2] = 0;
				pivData[i][3] = 0;
			}
		}
	}

	/**
	 * Invalidates a vector if the number of valid neighbours is less than
	 * specified.
	 * 
	 * @param numOfNeighbours
	 *            Specify the least number of neighbours to keep the vector.
	 */
	public void invalidateIsolatedVectors(int numOfNeighbours) {
		// make shure that there is a fifth column
		if (pivData[0].length < 5) {
			appendColumn(1.0);
		}
		double[][] newData = new double[pivData.length][pivData[0].length];
		// deep-copy of pivData
		for (int i = 0; i < newData.length; i++) {
			System.arraycopy(pivData[i], 0, newData[i], 0, pivData[i].length);
		}
		// invalidate vecors
		double[] nb;
		for (int i = 0; i < newData.length; ++i) {
			nb = getNeighbours(i, false, false, 2);
			if (nb == null || nb.length < numOfNeighbours) {
				newData[i][4] = -1d;
			}
		}
		// deep-copy of newData
		for (int i = 0; i < newData.length; i++) {
			System.arraycopy(newData[i], 0, pivData[i], 0, pivData[i].length);
		}
	}

	/**
	 * Calculates the first derivatives of the velocity field (central
	 * differences, forward/backward differences at the border).
	 * 
	 * @return A double array that contains the first derivatives of the
	 *         velocity field in x- and y-direction. The data is organized in a
	 *         three dimensional array. The first index indicates the vertical
	 *         (y) grid-index, the second one the horizontal (x) grid-index. The
	 *         last index indicates the direction of the derivative at this
	 *         point (dUx/dx, dUy/dy, dUx/dy, dUy/dx).
	 */
	public double[][][] getFirstDerivative() {
		double[][][] d = new double[verSize][horSize][4];
		for (int i = 0; i < verSize; i++) {
			for (int j = 0; j < horSize; j++) {
				// cartesian coordinates are used instead of geometric ones
				// top dUy/dy, dUx/dy forward
				if (i == 0) {
					d[i][j][1] = (-pivData[(i + 1) * horSize + j][3] * -1 + pivData[(i)
							* horSize + j][3]
							* -1)
							/ verSpacing;
					d[i][j][2] = (-pivData[(i + 1) * horSize + j][2] + pivData[(i)
							* horSize + j][2])
							/ verSpacing;
				}
				// bottom dUy/dy, dUx/dy backward
				if (i == verSize - 1) {
					d[i][j][1] = (-pivData[(i) * horSize + j][3] * -1 + pivData[(i - 1)
							* horSize + j][3]
							* -1)
							/ verSpacing;
					d[i][j][2] = (-pivData[(i) * horSize + j][2] + pivData[(i - 1)
							* horSize + j][2])
							/ verSpacing;
				}
				// left dUx/dx, dUy/dx forward
				if (j == 0) {
					d[i][j][0] = (pivData[i * horSize + j + 1][2] - pivData[i
							* horSize + j][2])
							/ horSpacing;
					d[i][j][3] = (pivData[i * horSize + j + 1][3] * -1 - pivData[i
							* horSize + j][3]
							* -1)
							/ horSpacing;
				}
				// right dUx/dx, dUy/dx backward
				if (j == horSize - 1) {
					d[i][j][0] = (pivData[i * horSize + j][2] - pivData[i
							* horSize + j - 1][2])
							/ horSpacing;
					d[i][j][3] = (pivData[i * horSize + j][3] * -1 - pivData[i
							* horSize + j - 1][3]
							* -1)
							/ horSpacing;
				}
				// center dUy/dy, dUx/dy central
				if (i != 0 && i != verSize - 1) {
					d[i][j][1] = (-pivData[(i + 1) * horSize + j][3] * -1 + pivData[(i - 1)
							* horSize + j][3]
							* -1)
							/ verSpacing / 2;
					d[i][j][2] = (-pivData[(i + 1) * horSize + j][2] + pivData[(i - 1)
							* horSize + j][2])
							/ verSpacing / 2;
				}
				// center dUx/dx, dUy/dx central
				if (j != 0 && j != horSize - 1) {
					d[i][j][0] = (pivData[i * horSize + j + 1][2] - pivData[i
							* horSize + j - 1][2])
							/ horSpacing / 2;
					d[i][j][3] = (pivData[i * horSize + j + 1][3] * -1 - pivData[i
							* horSize + j - 1][3]
							* -1)
							/ horSpacing / 2;
				}
			}
		}
		return d;
	}

	/**
	 * Calculates the first derivative of the velocity field (linear regression
	 * over five points, forward/backward differences at the border, central
	 * differences along the second row from the border). This method is more
	 * robust against noise than getFirstDerivative().
	 * 
	 * @return A double array that contains the first derivatives of the
	 *         velocity field in x- and y-direction. The data is organized in a
	 *         three dimensional array. The first index indicates the vertical
	 *         (y) grid-index, the second one the horizontal (x) grid-index. The
	 *         last index indicates the direction of the derivative at this
	 *         point (dUx/dx, dUy/dy, dUx/dy, dUy/dx).
	 */
	public double[][][] getFirstDerivativeLinReg() {
		double[][][] d = new double[verSize][horSize][4];
		double[] hor = { 0, horSpacing, 2 * horSpacing, 3 * horSpacing,
				4 * horSpacing };
		double[] ver = { 0, verSpacing, 2 * verSpacing, 3 * verSpacing,
				4 * verSpacing };
		double[] y = new double[5];
		for (int i = 0; i < verSize; i++) {
			for (int j = 0; j < horSize; j++) {
				// cartesian coordinates are used instead of geometric ones
				// top dUy/dy, dUx/dy forward
				if (i == 0) {
					d[i][j][1] = (-pivData[(i + 1) * horSize + j][3] * -1 + pivData[(i)
							* horSize + j][3]
							* -1)
							/ verSpacing;
					d[i][j][2] = (-pivData[(i + 1) * horSize + j][2] + pivData[(i)
							* horSize + j][2])
							/ verSpacing;
				}
				// bottom dUy/dy, dUx/dy backward
				if (i == verSize - 1) {
					d[i][j][1] = (-pivData[(i) * horSize + j][3] * -1 + pivData[(i - 1)
							* horSize + j][3]
							* -1)
							/ verSpacing;
					d[i][j][2] = (-pivData[(i) * horSize + j][2] + pivData[(i - 1)
							* horSize + j][2])
							/ verSpacing;
				}
				// left dUx/dx, dUy/dx forward
				if (j == 0) {
					d[i][j][0] = (pivData[i * horSize + j + 1][2] - pivData[i
							* horSize + j][2])
							/ horSpacing;
					d[i][j][3] = (pivData[i * horSize + j + 1][3] * -1 - pivData[i
							* horSize + j][3]
							* -1)
							/ horSpacing;
				}
				// right dUx/dx, dUy/dx backward
				if (j == horSize - 1) {
					d[i][j][0] = (pivData[i * horSize + j][2] - pivData[i
							* horSize + j - 1][2])
							/ horSpacing;
					d[i][j][3] = (pivData[i * horSize + j][3] * -1 - pivData[i
							* horSize + j - 1][3]
							* -1)
							/ horSpacing;
				}
				// second line dUy/dy, dUx/dy central
				if (i == 1 || i == verSize - 2) {
					d[i][j][1] = (-pivData[(i + 1) * horSize + j][3] * -1 + pivData[(i - 1)
							* horSize + j][3]
							* -1)
							/ verSpacing / 2;
					d[i][j][2] = (-pivData[(i + 1) * horSize + j][2] + pivData[(i - 1)
							* horSize + j][2])
							/ verSpacing / 2;
				}
				// second line center dUx/dx, dUy/dx central
				if (j == 1 || j == horSize - 2) {
					d[i][j][0] = (pivData[i * horSize + j + 1][2] - pivData[i
							* horSize + j - 1][2])
							/ horSpacing / 2;
					d[i][j][3] = (pivData[i * horSize + j + 1][3] * -1 - pivData[i
							* horSize + j - 1][3]
							* -1)
							/ horSpacing / 2;
				}
				// center dy regression
				if (i != 0 && i != 1 && i != verSize - 1 && i != verSize - 2) {
					y[4] = pivData[(i - 2) * horSize + j][3] * -1;
					y[3] = pivData[(i - 1) * horSize + j][3] * -1;
					y[2] = pivData[i * horSize + j][3] * -1;
					y[1] = pivData[(i + 1) * horSize + j][3] * -1;
					y[0] = pivData[(i + 2) * horSize + j][3] * -1;
					d[i][j][1] = Statistics.getLinearRegressionGradient(ver, y);
					y[4] = pivData[(i - 2) * horSize + j][2];
					y[3] = pivData[(i - 1) * horSize + j][2];
					y[2] = pivData[i * horSize + j][2];
					y[1] = pivData[(i + 1) * horSize + j][2];
					y[0] = pivData[(i + 2) * horSize + j][2];
					d[i][j][2] = Statistics.getLinearRegressionGradient(ver, y);
				}
				// center dx regression
				if (j != 0 && j != 1 && j != horSize - 1 && j != horSize - 2) {
					y[0] = pivData[i * horSize + j - 2][2];
					y[1] = pivData[i * horSize + j - 1][2];
					y[2] = pivData[i * horSize + j][2];
					y[3] = pivData[i * horSize + j + 1][2];
					y[4] = pivData[i * horSize + j + 2][2];
					d[i][j][0] = Statistics.getLinearRegressionGradient(hor, y);
					y[0] = pivData[i * horSize + j - 2][3] * -1;
					y[1] = pivData[i * horSize + j - 1][3] * -1;
					y[2] = pivData[i * horSize + j][3] * -1;
					y[3] = pivData[i * horSize + j + 1][3] * -1;
					y[4] = pivData[i * horSize + j + 2][3] * -1;
					d[i][j][3] = Statistics.getLinearRegressionGradient(hor, y);
				}
			}
		}
		return d;
	}

	/**
	 * Calculates the shear field (central differences, forward/backward
	 * differences at the border). This is bascially the same as the first
	 * derivative with ux and uy flipped.
	 * 
	 * @return A double array that contains the shear values of the velocity
	 *         field in x- and y-direction. The data is organized in a three
	 *         dimensional array. The first index indicates the vertical (y)
	 *         grid-index, the second one the horizontal (x) grid-index. The
	 *         last index indicates the direction of the derivative at this
	 *         point (0 = x-direction, 1 = y-direction).
	 */
	public double[][][] getShearField() {
		double[][][] d = new double[verSize][horSize][2];
		for (int i = 0; i < verSize; i++) {
			for (int j = 0; j < horSize; j++) {
				// top dy forward
				if (i == 0) {
					d[i][j][0] = (pivData[(i + 1) * horSize + j][2] - pivData[(i)
							* horSize + j][2])
							/ verSpacing;
				}
				// bottom dy backward
				if (i == verSize - 1) {
					d[i][j][0] = (pivData[(i) * horSize + j][2] - pivData[(i - 1)
							* horSize + j][2])
							/ verSpacing;
				}
				// left dx forward
				if (j == 0) {
					d[i][j][1] = (pivData[i * horSize + j + 1][3] - pivData[i
							* horSize + j][3])
							/ horSpacing;
				}
				// right dx backward
				if (j == horSize - 1) {
					d[i][j][1] = (pivData[i * horSize + j][3] - pivData[i
							* horSize + j - 1][3])
							/ horSpacing;
				}
				// center dy central
				if (i != 0 && i != verSize - 1) {
					d[i][j][0] = (pivData[(i + 1) * horSize + j][2] - pivData[(i - 1)
							* horSize + j][2])
							/ verSpacing / 2;
				}
				// center dx central
				if (j != 0 && j != horSize - 1) {
					d[i][j][1] = (pivData[i * horSize + j + 1][3] - pivData[i
							* horSize + j - 1][3])
							/ horSpacing / 2;
				}
			}
		}
		return d;
	}

	/**
	 * Returns the width and heigth of the vector field.
	 * 
	 * @return An array containing the horizontal and the vertical number of
	 *         data points.
	 */
	public int[] getDimension() {
		int[] dimension = { horSize, verSize };
		return (dimension);
	}

	/**
	 * Returns the number of data columns, typically five (x, y, dx, dy, flag).
	 * 
	 * @return The number of data columns.
	 */
	public int getNumOfColumns() {
		return pivData[0].length;
	}

	/**
	 * Returns the horizontal and vertical spacing between two adjacent vectors.
	 * 
	 * @return An array that contains the horizontal and the vertical vector
	 *         spacing in the first and second position, respectively.
	 */
	public int[] getSpacing() {
		int[] spacing = { horSpacing, verSpacing };
		return (spacing);
	}

	/**
	 * Trys to reconstruct the original image size from the vector field. A
	 * length (width, height) is assumed to be the sum of the distances between
	 * tow data points plus twice the distance from 0 to the first data point.
	 * 
	 * @return The size of the image (widht at index 0, height at index 1).
	 */
	public int[] getImgSize() {
		int[] imgSize = new int[2];
		// width
		imgSize[0] = (horSize - 1) * horSpacing
				+ (int) (2 * Math.abs(pivData[0][0]));
		// height
		imgSize[1] = (verSize - 1) * verSpacing
				+ (int) (2 * Math.abs(pivData[0][1]));
		return (imgSize);
	}

	/**
	 * Saves a data set as a space delimited unicode (UTF-8) file.
	 * 
	 * @param pathname
	 *            The absolute pathname of a destination file.
	 * @param writeHeader
	 *            Specifies whether a Tecplot header file should be written
	 *            <code>true</code> or not <code>false</code>.
	 */
	public void writeDataToFile(String pathname, boolean writeHeader) {
		if (!writeHeader)
			header = "";
		try {
			// remove any extension
			int index = pathname.lastIndexOf('.');
			if (index != -1)
				pathname = pathname.substring(0, index);
			// define format
			DecimalFormat df = (DecimalFormat) DecimalFormat
					.getInstance(Locale.US);
			df.applyPattern("+0.0000E00;-0.0000E00");
			// save as a file
			FileHandling.writeArrayToFile(pivData, pathname + ".jvc", df,
					header);
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}

	/**
	 * Returns the data point that is closest to x and y.
	 * 
	 * @param x
	 *            The approximate x co-ordinate.
	 * @param y
	 *            The approximate y co-ordinate.
	 * @return A line of the data-set or <code>null</code> if the data point is
	 *         outside the vector field.
	 */
	public double[] getVectorAt(int x, int y) {
		// check whether x and y are not outside of the vector field
		if (x <= (Math.abs(pivData[0][0]) - horSpacing / 2)
				|| y <= (Math.abs(pivData[0][1]) - verSpacing / 2)
				|| x >= (Math.abs(pivData[pivData.length - 1][0]) + horSpacing / 2)
				|| y >= (Math.abs(pivData[pivData.length - 1][1]) + verSpacing / 2)) {
			return (null);
		}
		// convert array notation into geometrical notation
		float horIndex = (float) (x - Math.abs(pivData[0][0])) / horSpacing;
		float verIndex = (float) (y - Math.abs(pivData[0][1])) / verSpacing;
		int line = Math.round(verIndex) * horSize + Math.round(horIndex);
		return (pivData[line]);
	}

	/**
	 * Returns a vector that has been interpolated between the three nearest
	 * neighbours of the position x, y. The third and forth column of PIV data
	 * is interpolated. The first and second columns are replaced by x and y.
	 * Other columns are ignored.
	 * 
	 * @param x
	 *            The x co-ordinate.
	 * @param y
	 *            The y co-ordinate.
	 * @return The interpolated vector (x, y, interp u, interp v).
	 */
	public double[] getInterpVectorAt(float x, float y) {
		double[] theVector = new double[4];
		theVector[0] = x;
		theVector[1] = y;
		double[][] theNeigh = getThreeNearestNeighbours(x, y);
		double x1 = Math.abs((double) theNeigh[0][0]);
		double x2 = Math.abs((double) theNeigh[1][0]);
		double x3 = Math.abs((double) theNeigh[2][0]);
		double y1 = Math.abs((double) theNeigh[0][1]);
		double y2 = Math.abs((double) theNeigh[1][1]);
		double y3 = Math.abs((double) theNeigh[2][1]);
		double u1 = (double) theNeigh[0][2];
		double u2 = (double) theNeigh[1][2];
		double u3 = (double) theNeigh[2][2];
		double v1 = (double) theNeigh[0][3];
		double v2 = (double) theNeigh[1][3];
		double v3 = (double) theNeigh[2][3];
		// interpolation of u
		double t1 = u1 * x2;
		double t3 = u2 * y1;
		double t5 = u3 * x2;
		double t10 = y3 * u2;
		double t15 = y2 * x3;
		double t19 = x * y2;
		double t23 = y * u2;
		double t28 = x1 * y2;
		double t31 = t1 * y + t3 * x - t5 * y + t5 * y1 + u3 * x1 * y + t10
				* x1 + u1 * y3 * x - t10 * x + t15 * u1 - x * y1 * u3 + t19
				* u3 - t1 * y3 - t3 * x3 + t23 * x3 - t23 * x1 - y * u1 * x3
				- t28 * u3 - t19 * u1;
		theVector[2] = -t31
				/ (x3 * y1 - t15 + t28 + y3 * x2 - x2 * y1 - x1 * y3);
		// interpolation of v
		t1 = v1 * x2;
		t3 = v2 * y1;
		t5 = v3 * x2;
		t10 = y3 * v2;
		t23 = y * v2;
		t31 = t1 * y + t3 * x - t5 * y + t5 * y1 + v3 * x1 * y + t10 * x1 + v1
				* y3 * x - t10 * x + t15 * v1 - x * y1 * v3 + t19 * v3 - t1
				* y3 - t3 * x3 + t23 * x3 - t23 * x1 - y * v1 * x3 - t28 * v3
				- t19 * v1;
		theVector[3] = -t31
				/ (x3 * y1 - t15 + t28 + y3 * x2 - x2 * y1 - x1 * y3);
		return (theVector);
	}

	/**
	 * Returns the three data points that are closest to x and y and don't lie
	 * on a straight line.
	 * 
	 * @param x
	 *            The approximate x co-ordinate
	 * @param y
	 *            The approximate y co-ordinate
	 * @return Three lines of the data-set
	 */
	public double[][] getThreeNearestNeighbours(float x, float y) {
		double[] secondVec;
		double[] thirdVec;
		double[] centerVec = getVectorAt(Math.round(x), Math.round(y));
		double[] leftVec = getVectorAt(Math.round(x - horSpacing),
				Math.round(y));
		double[] rightVec = getVectorAt(Math.round(x + horSpacing),
				Math.round(y));
		double[] topVec = getVectorAt(Math.round(x), Math.round(y - verSpacing));
		double[] bottomVec = getVectorAt(Math.round(x),
				Math.round(y + verSpacing));
		if (leftVec == null
				|| (rightVec != null && (Math.abs(centerVec[0]) - Math
						.abs(leftVec[0])) > (Math.abs(rightVec[0]) - Math
						.abs(centerVec[0]))))
			secondVec = rightVec;
		else
			secondVec = leftVec;
		if (topVec == null
				|| (bottomVec != null && (Math.abs(topVec[1]) - Math
						.abs(centerVec[1])) > (Math.abs(centerVec[1]) - Math
						.abs(bottomVec[1]))))
			thirdVec = bottomVec;
		else
			thirdVec = topVec;
		double[][] nearNeig = { centerVec, secondVec, thirdVec };
		return (nearNeig);
	}

	/**
	 * Resamples the vector field by nearest neighbour interpolation. This
	 * method is useful for grid refinement during multipass PIV evaluations.
	 * 
	 * @param x0
	 *            horizontal position of first vector
	 * @param y0
	 *            vertical position of first vector
	 * @param x1
	 *            horizontal positin of last vector
	 * @param y1
	 *            vertical position of last vector
	 * @param dx
	 *            horizontal vector spacing
	 * @param dy
	 *            vertical vector spacing
	 */
	public void resample(int x0, int y0, int x1, int y1, int dx, int dy) {
		int newHorSize = (x1 - x0) / dx + 1;
		int newVerSize = (y1 - y0) / dy + 1;
		double[][] newPivData = new double[newHorSize * newVerSize][pivData[0].length];
		for (int i = 0; i < newPivData.length; ++i) {
			// generate new grid
			newPivData[i][0] = (i % newHorSize) * dx + x0;
			newPivData[i][1] = ((int) i / newHorSize) * dy + y0;
			// interpolate by nearest neighbour
			double[] nearest = getVectorAt((int) newPivData[i][0],
					(int) newPivData[i][1]);
			if (nearest != null) {
				for (int j = 2; j < pivData[0].length; ++j) {
					newPivData[i][j] = nearest[j];
				}
			} else {
				System.err.println("The present grid point (x: "
						+ (int) newPivData[i][0] + " y: "
						+ (int) newPivData[i][1]
						+ ") extends the previous vector field. ");
				for (int j = 2; j < pivData[0].length; ++j) {
					newPivData[i][j] = 0;
				}
			}
		}
		horSize = newHorSize;
		verSize = newVerSize;
		pivData = newPivData;
	}

	/**
	 * Mark vectors as invalid using the normalized median test proposed by
	 * Jerry Westerweel and Fulvio Scarano (Experiments in Fluids (2005) 39:
	 * 1096-1100). The velocity data remains unchanged, but the value in the
	 * fifth column (generally the peak height) is set to -1.
	 * 
	 * @param noiseLevel
	 *            Noise Level of the velocity data in pixel units.
	 * @param threshold
	 *            Data above this threshold will be discarted (the default is
	 *            2).
	 */
	public void normalizedMedianTest(double noiseLevel, double threshold) {
		// make shure that there is a fifth column
		if (pivData[0].length < 5) {
			appendColumn(1.0);
		}
		double[] nb;
		double normResidu;
		// loop over data points
		for (int i = 0; i < pivData.length; ++i) {
			// loop over velocity components x and y
			for (int c = 2; c < 4; c++) {
				nb = getNeighbours(i, false, false, c);
				if (nb != null) {
					normResidu = Math.abs(pivData[i][c]
							- jpiv2.Statistics.getMedian(nb))
							/ (jpiv2.Statistics.getMedian(jpiv2.Statistics
									.getResidualsOfMedian(nb)) + noiseLevel);
					if (normResidu > threshold)
						pivData[i][4] = -1d;
				} else
					pivData[i][4] = -1d;
			}
		}
	}

	/**
	 * Calculate components of the deformation tensor.
	 * 
	 * @param linReg
	 *            Set <code>true</code>, if derivatives should be calculated by
	 *            this.getFirstDerivativeLinReg(). Otherwise,
	 *            this.getFirstDerivative() is used.
	 * @param component
	 *            Either PivDataFilter.NORMAL_VORTICITY,
	 *            PivDataFilter.IN_PLANE_SHEAR, or
	 *            PivDataFilter.EXTENSIONAL_STRAIN
	 */
	public void deformation(boolean linReg, int component) {
		// make shure that there is a fifth column
		if (pivData[0].length < 5) {
			appendColumn(1.0);
		}
		double[][][] deriv;
		if (linReg)
			deriv = getFirstDerivativeLinReg();
		else
			deriv = getFirstDerivative();
		// copy data
		for (int i = 0; i < pivData.length; ++i) {
			switch (component) {
			// 0 = dUx/dx, 1 = dUy/dy, 2 = dUx/dy, 3 = dUy/dx)
			case PivDataFilter.NORMAL_VORTICITY: {
				// dUy/dx - dUx/dy
				pivData[i][4] = deriv[i / horSize][i - i / horSize * horSize][3]
						- deriv[i / horSize][i - i / horSize * horSize][2];
				break;
			}
			case PivDataFilter.IN_PLANE_SHEAR: {
				// dUx/dy + dUy/dx
				pivData[i][4] = deriv[i / horSize][i - i / horSize * horSize][2]
						+ deriv[i / horSize][i - i / horSize * horSize][3];
				break;
			}
			case PivDataFilter.EXTENSIONAL_STRAIN: {
				// dUx/dx + dUy/dy
				pivData[i][4] = deriv[i / horSize][i - i / horSize * horSize][0]
						+ deriv[i / horSize][i - i / horSize * horSize][1];
				break;
			}
			}
		}
	}

	/**
	 * Replaces invalid vectors by the median of their surrounding vectors. A
	 * vector is considered to be invalid if its fifth column (peak height) is
	 * negative or null.
	 * 
	 * @param all
	 *            If all is set <code>true</code> every data-point is replaced
	 *            by the median of a 3x3 array inclusive center point (median
	 *            filtering).
	 * @param includeInvalid
	 *            set <code>false</code> to exclude invalid vectors in the
	 *            median calculation.
	 */
	public void replaceByMedian(boolean all, boolean includeInvalid) {
		double[][] newData = new double[pivData.length][pivData[0].length];
		// deep-copy of pivData
		for (int i = 0; i < newData.length; i++) {
			System.arraycopy(pivData[i], 0, newData[i], 0, pivData[i].length);
		}
		// median filtering
		double[] nb;
		for (int i = 0; i < newData.length; ++i) {
			if (all || newData[i][4] <= 0) {
				nb = getNeighbours(i, all, includeInvalid, 2);
				if (nb != null) {
					newData[i][2] = jpiv2.Statistics.getMedian(nb);
				} else
					newData[i][2] = 0.0d;
				nb = getNeighbours(i, all, includeInvalid, 3);
				if (nb != null) {
					newData[i][3] = jpiv2.Statistics.getMedian(nb);
				} else
					newData[i][3] = 0.0d;
			}
		}
		// deep-copy of newData
		for (int i = 0; i < newData.length; i++) {
			System.arraycopy(newData[i], 0, pivData[i], 0, pivData[i].length);
		}
	}

	/**
	 * Flip y-axis. Turn the vector map 180 degrees around the x-axis.
	 */
	public void reverseY() {
		double[][] oldData = new double[pivData.length][pivData[0].length];
		// copy old Data
		for (int i = 0; i < pivData.length; i++) {
			System.arraycopy(pivData[i], 0, oldData[i], 0, pivData[i].length);
		}
		// reverse vector map
		for (int i = 0; i < verSize; i++) {
			for (int j = 0; j < horSize; j++) {
				System.arraycopy(oldData[i * horSize + j], 2,
						pivData[(verSize - 1 - i) * horSize + j], 2,
						pivData[i].length - 2);
				pivData[(verSize - 1 - i) * horSize + j][3] *= -1;
			}
		}
	}

	/**
	 * Flip sign of y-velocity component.
	 * 
	 */
	public void flipY() {
		for (int i = 0; i < pivData.length; i++) {
			pivData[i][3] *= -1;
		}
	}

	/**
	 * Replaces every vector by the average of the three by three neighbourhood
	 * (inclusive center point).
	 * 
	 * @param includeInvalid
	 *            Set to false to exclude invalid vectors in the average
	 *            calculation.
	 */
	public void smooth(boolean includeInvalid) {
		double[][] newData = new double[pivData.length][pivData[0].length];
		// deep-copy of pivData
		for (int i = 0; i < newData.length; i++) {
			System.arraycopy(pivData[i], 0, newData[i], 0, pivData[i].length);
		}
		// smoothing
		double[] nb;
		for (int i = 0; i < newData.length; ++i) {
			// x component
			nb = getNeighbours(i, true, includeInvalid, 2);
			if (nb != null) {
				newData[i][2] = jpiv2.Statistics.getAverage(nb);
			} else
				newData[i][2] = 0.0d;
			// y component
			nb = getNeighbours(i, true, includeInvalid, 3);
			if (nb != null) {
				newData[i][3] = jpiv2.Statistics.getAverage(nb);
			} else
				newData[i][3] = 0.0d;
		}
		// deep-copy of newData
		for (int i = 0; i < newData.length; i++) {
			System.arraycopy(newData[i], 0, pivData[i], 0, pivData[i].length);
		}
	}

	/**
	 * Adds the product of a random number (-1 < random number < 1) and maxNoise
	 * to the values of the third and fourth column.
	 * 
	 * @param maxNoise
	 *            the scaling factor of the random noise
	 */
	public void addRandomNoise(double maxNoise) {
		for (int i = 0; i < pivData.length; i++) {
			pivData[i][2] += (Math.random() * 2 - 1) * maxNoise;
			pivData[i][3] += (Math.random() * 2 - 1) * maxNoise;
		}
	}

	/**
	 * Appending a column of defaultData.
	 * 
	 * @param defaultValue
	 *            The default data for the new data fields.
	 */
	public void appendColumn(double defaultValue) {
		double[][] newData = new double[pivData.length][pivData[0].length + 1];
		for (int i = 0; i < pivData.length; i++) {
			for (int j = 0; j < pivData[0].length; j++) {
				newData[i][j] = pivData[i][j];
			}
			newData[i][pivData[0].length] = defaultValue;
		}
		pivData = newData;
		createHeader();
	}

	/**
	 * Extracting a horizontal profile.
	 * 
	 * @param row
	 *            The row to extract.
	 * @return The reduced data.
	 */
	public double[][] getHorizontalProfile(int row) {
		double[][] profile = new double[horSize][pivData[0].length];
		for (int i = 0, r = row; i < horSize; r += row, ++i) {
			System.arraycopy(pivData[r], 0, profile[i], 0, pivData[0].length);
		}
		return (profile);
	}

	/**
	 * Extracting a vertical profile.
	 * 
	 * @param col
	 *            The column to extract.
	 * @return The reduced data.
	 */
	public double[][] getVerticalProfile(int col) {
		double[][] profile = new double[verSize][pivData[0].length];
		for (int i = 0, c = col * horSize; i < verSize; ++c, ++i) {
			System.arraycopy(pivData[c], 0, profile[i], 0, pivData[0].length);
		}
		return (profile);
	}

	/**
	 * Extracting a profile between two points. The data points are linearily
	 * interpolated between the closed three neighbours of the given coordinate.
	 * The point density along the profile is about the smallest grid size of
	 * the original data.
	 * 
	 * @param x1
	 *            The x co-ordinate of the first point.
	 * @param y1
	 *            The y co-ordinate of the first point.
	 * @param x2
	 *            The x co-ordinate of the second point.
	 * @param y2
	 *            The y co-ordinate of the second point.
	 * @param spacing
	 *            The distance between the data points. If this value is zero,
	 *            the smallest spacing of the vector field is used.
	 * @return The interpolated profile.
	 */
	public double[][] getFreeProfile(float x1, float y1, float x2, float y2,
			float spacing) {
		// the smallest vector spacing is taken as the point density for the
		// profile
		if (spacing == 0) {
			if (horSpacing < verSpacing)
				spacing = horSpacing;
			else
				spacing = verSpacing;
		}
		// the sampling distance along the profile
		float length = Math.round(Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1)
				* (y2 - y1)));
		int pointsN = Math.round(length / spacing) + 1;
		float xSpacing = Math.abs(x2 - x1) / (pointsN - 1);
		float ySpacing = Math.abs(y2 - y1) / (pointsN - 1);
		// the increment foresign
		int xSign = 1;
		int ySign = 1;
		if (x1 > x2)
			xSign = -1;
		if (y1 > y2)
			ySign = -1;
		float[] currentPoint = { x1, y1 };
		double[][] theProfile = new double[pointsN][4];
		for (int i = 0; i < theProfile.length; i++) {
			theProfile[i] = getInterpVectorAt(currentPoint[0], currentPoint[1]);
			currentPoint[0] += xSpacing * xSign;
			currentPoint[1] += ySpacing * ySign;
		}
		return (theProfile);
	}

	/**
	 * Extracting and averaging a number of profiles between two points. The
	 * data points are linearily interpolated between the closed three
	 * neighbours of the given coordinate. The point density along the profile
	 * is about the smallest grid size of the original data.
	 * 
	 * @param x1
	 *            The x co-ordinate of the first point.
	 * @param y1
	 *            The y co-ordinate of the first point.
	 * @param x2
	 *            The x co-ordinate of the second point.
	 * @param y2
	 *            The y co-ordinate of the second point.
	 * @param numberOf
	 *            Total number of profiles.
	 * @param spacing
	 *            The distance between the data points. If this value is zero,
	 *            the smallest spacing of the vector field is used.
	 * @return An array that contains all profiles. The averaged profile is
	 *         stored in the last element.
	 * @param distance
	 *            The distance between the profiles in pixels.
	 */
	public double[][][] getFreeProfiles(float x1, float y1, float x2, float y2,
			float spacing, int numberOf, int distance) {
		double[][][] theProfiles = new double[numberOf + 1][][];
		// angle of profile with vertical line
		double alpha = Math.atan(Math.abs((y2 - y1) / (x2 - x1)));
		// step size
		float dx = (float) Math.sin(alpha) * distance;
		float dy = (float) Math.cos(alpha) * distance;
		if ((x2 > x1 && y2 > y1) || (x2 < x1 && y2 < y1))
			dy = dy * -1f;
		// starting point
		float x10 = x1 - (numberOf - 1) / 2 * dx;
		float y10 = y1 - (numberOf - 1) / 2 * dy;
		float x20 = x2 - (numberOf - 1) / 2 * dx;
		float y20 = y2 - (numberOf - 1) / 2 * dy;
		// getting the profiles
		for (int p = 0; p < numberOf; p++) {
			theProfiles[p] = getFreeProfile(x10 + p * dx, y10 + p * dy, x20 + p
					* dx, y20 + p * dy, spacing);
		}
		// averaging the profiles
		theProfiles[numberOf] = theProfiles[numberOf - 1];
		for (int p = 0; p < numberOf - 1; p++) {
			for (int i = 0; i < theProfiles[0].length; i++) {
				for (int j = 2; j < theProfiles[0][0].length; j++) {
					theProfiles[numberOf][i][j] += theProfiles[p][i][j];
				}
			}
		}
		for (int i = 0; i < theProfiles[0].length; i++) {
			for (int j = 2; j < theProfiles[0][0].length; j++) {
				theProfiles[numberOf][i][j] = theProfiles[numberOf][i][j]
						/ numberOf;
			}
		}
		return theProfiles;
	}

	/**
	 * Extracts a single data column from the PIV data. This function is used to
	 * do statistics.
	 * 
	 * @param col
	 *            The column to extract.
	 * @return The extracted data column.
	 */
	public double[] getDataColumn(int col) {
		double[] data = new double[pivData.length];
		for (int i = 0; i < pivData.length; i++) {
			data[i] = pivData[i][col];
		}
		return (data);
	}

	/**
	 * Calculates the absolute values of two vector components.
	 * 
	 * @param col1
	 *            The column that contains the first vector component.
	 * @param col2
	 *            The column that contains the second vector component.
	 * @return An array containing the length of the 2D vectors.
	 */
	public double[] getAbsValue(int col1, int col2) {
		double[] data = new double[pivData.length];
		for (int i = 0; i < pivData.length; i++) {
			data[i] = Math.sqrt(pivData[i][col1] * pivData[i][col1]
					+ pivData[i][col2] * pivData[i][col2]);
		}
		return (data);
	}

	/**
	 * Gets the neigbhouring values of a vector component. The values are
	 * returned in ascending index order.
	 * 
	 * @param cnt
	 *            The index of the center vector
	 * @param incl
	 *            <code>true</code> if also the center value itself should be
	 *            returned.
	 * @param all
	 *            Also return invalid values (whose fifth column value is
	 *            negative or null), if <code>true</code>.
	 * @param col
	 *            The vector component to return
	 * @return An array of up to nine values from column <code>col</code> or
	 *         <code>null</null> if no neighbours could be found.
	 */
	private double[] getNeighbours(int cnt, boolean incl, boolean all, int col) {
		double[] nb = new double[9];
		int i = cnt / horSize;
		int j = cnt - i * horSize;
		int numOfNb = 0;
		int n = 0;
		// first row neighbours
		n = cnt - horSize - 1;
		if (i - 1 >= 0 && j - 1 >= 0 && (pivData[n][4] > 0 || all)) {
			numOfNb++;
			nb[numOfNb - 1] = pivData[n][col];
		}
		n = cnt - horSize;
		if (i - 1 >= 0 && (pivData[n][4] > 0 || all)) {
			numOfNb++;
			nb[numOfNb - 1] = pivData[n][col];
		}
		n = cnt - horSize + 1;
		if (i - 1 >= 0 && j + 1 < horSize && (pivData[n][4] > 0 || all)) {
			numOfNb++;
			nb[numOfNb - 1] = pivData[n][col];
		}
		// secont row neighbours
		n = cnt - 1;
		if (j - 1 >= 0 && (pivData[n][4] > 0 || all)) {
			numOfNb++;
			nb[numOfNb - 1] = pivData[n][col];
		}
		n = cnt;
		if (incl && (pivData[n][4] > 0 || all)) {
			numOfNb++;
			nb[numOfNb - 1] = pivData[n][col];
		}
		n = cnt + 1;
		if (j + 1 < horSize && (pivData[n][4] > 0 || all)) {
			numOfNb++;
			nb[numOfNb - 1] = pivData[n][col];
		}
		// third row neighbours
		n = cnt + horSize - 1;
		if (i + 1 < verSize && j - 1 >= 0 && (pivData[n][4] > 0 || all)) {
			numOfNb++;
			nb[numOfNb - 1] = pivData[n][col];
		}
		n = cnt + horSize;
		if (i + 1 < verSize && (pivData[n][4] > 0 || all)) {
			numOfNb++;
			nb[numOfNb - 1] = pivData[n][col];
		}
		n = cnt + horSize + 1;
		if (i + 1 < verSize && j + 1 < horSize && (pivData[n][4] > 0 || all)) {
			numOfNb++;
			nb[numOfNb - 1] = pivData[n][col];
		}
		if (numOfNb > 0) {
			double[] ret = new double[numOfNb];
			System.arraycopy(nb, 0, ret, 0, numOfNb);
			return (ret);
		} else {
			return (null);
		}
	}

	/**
	 * Counts the number of invalid vecors. Take care that a fifth column
	 * exists.
	 * 
	 * @return The number of lines where the value in the fifth column is
	 *         smaller than zero.
	 */
	public int getInvalidVectorCount() {
		int invalid = 0;
		for (int i = 0; i < pivData.length; i++) {
			if (pivData[i][4] < 0)
				invalid += 1;
		}
		return invalid;
	}

	// /** Adding two data sets except the first two columns. */
	// private double[][] addPivData(double[][] setOne, double[][] setTwo) {
	// for(int j = 0; j < setOne[0].length; ++j) {
	// for(int i = 2; i < setOne.length; ++i) {
	// setOne[i][j] = setOne[i][j] + setTwo[i][j];
	// }
	// }
	// return(setOne);
	// }
	//
	// /** Dividing a data set exept the first tow columns by 'number'. */
	// private double[][] dividePivData(double[][] dataSet, int number) {
	// for(int j = 0; j < dataSet[0].length; ++j) {
	// for(int i = 2; i < dataSet.length; ++i) {
	// dataSet[i][j] = dataSet[i][j] / number;
	// }
	// }
	// return(dataSet);
	// }

	/** Getting the dimension of the vector field. */
	private void getVectorFieldSize() throws ArrayIndexOutOfBoundsException {
		int j = 1;
		while (j < pivData.length && pivData[0][0] != pivData[j][0]) {
			++j;
		}
		horSize = j;
		verSize = pivData.length / horSize;
		horSpacing = (int) (Math.abs(pivData[1][0]) - Math.abs(pivData[0][0]));
		verSpacing = (int) (Math.abs(pivData[horSize][1]) - Math
				.abs(pivData[0][1]));
	}

	private void createHeader() {
		header = "TITLE=\"jpiv vector field\"\n"
				+ "VARIABLES=\"x\" \"y\" \"dx\" \"dy\"";
		for (int col = 4; col < pivData[0].length; col++)
			header = header + " \"col " + (col + 1) + "\"";
		header = header + "\nZONE I=" + horSize + " J=" + verSize
				+ " F=POINT\n";
	}

	/**
	 * Set the header string of the data file.
	 * 
	 * @param header
	 *            The header.
	 */
	public void setHeader(String header) {
		this.header = header;
	}

	/** Creating a dummy vector field */
	private double[][] getDefaultData() {
		double[][] defaultData = { { 160, 160, -12, -12 },
				{ 200, 160, 0, -18 }, { 240, 160, 12, -12 },
				{ 160, 200, -18, 0 }, { 200, 200, 0, 0 }, { 240, 200, 18, 0 },
				{ 160, 240, -12, 12 }, { 200, 240, 0, 18 },
				{ 240, 240, 12, 12 } };
		return (defaultData);
	}

	/**
	 * Delete vectors outside a flow domain. This filter starts at grid position
	 * x = 0 and y = yStart. It first moves up, and searches for a point where
	 * the velocity is a) below the threshold and b) increases in magnitude.
	 * From here on, all vectors are set to zero. Then, it does the same for
	 * moving down. Then, x is increased by 1 and the next column is processed.
	 * This is repeated, until the whole vector field is filtered. This filter
	 * might be used to set vectors outside a presumed wall to zero.
	 * 
	 * @param xStart
	 *            The horizontal index of a vector grid line. The wall detection
	 *            starts from here.
	 * @param yStart
	 *            The vertical index of a vector grid line. The wall detection
	 *            starts from here.
	 * @param threshold
	 *            Consider velocities below this value to be close to a wall.
	 */
	public void wallFilterVertical(int xStart, int yStart, double threshold) {
		double vc, vr, vl;
		// loop right to left
		for (int j = xStart; j >= 0; j--) {
			int cStart = yStart * horSize + j;
			// Check, if a wall at the left side is reached
			if (j > 0) {
				vl = Math.sqrt(pivData[cStart - 1][2] * pivData[cStart - 1][2]
						+ pivData[cStart - 1][3] * pivData[cStart - 1][3]);
				vc = Math.sqrt(pivData[cStart][2] * pivData[cStart][2]
						+ pivData[cStart][3] * pivData[cStart][3]);
				if (vc < threshold && vl > vc) {
					// delete rest of row
					for (int c = cStart - 1; c > (yStart) * horSize; c--) {
						pivData[c][2] = 0.0;
						pivData[c][3] = 0.0;
					}
				}
			}
			wallFilterVerticalLine(cStart, threshold);
		}
		// loop from left to right
		for (int j = xStart; j < horSize; j++) {
			int cStart = yStart * horSize + j;
			// Check, if a wall at the right side is reached
			if (j < horSize - 1) {
				vr = Math.sqrt(pivData[cStart + 1][2] * pivData[cStart + 1][2]
						+ pivData[cStart + 1][3] * pivData[cStart + 1][3]);
				vc = Math.sqrt(pivData[cStart][2] * pivData[cStart][2]
						+ pivData[cStart][3] * pivData[cStart][3]);
				if (vc < threshold && vr > vc) {
					// delete rest of row
					for (int c = cStart + 1; c < (yStart + 1) * horSize; c++) {
						pivData[c][2] = 0.0;
						pivData[c][3] = 0.0;
					}
				}
			}
			wallFilterVerticalLine(cStart, threshold);
		}
	}

	/**
	 * Print data to std-out.
	 * 
	 * @return null
	 */
	public void print() {
		for (int i = 0; i < pivData.length; i++) {
			for (int j = 0; j < pivData[0].length; j++) {
				System.out.print(pivData[i][j] + " ");
			}
			System.out.println();
		}
	}

	private void wallFilterVerticalLine(int cStart, double threshold) {
		double vc, vt, vb;
		// go to the top
		for (int c = cStart; c > horSize; c -= horSize) {
			vt = Math.sqrt(pivData[c - horSize][2] * pivData[c - horSize][2]
					+ pivData[c - horSize][3] * pivData[c - horSize][3]);
			vc = Math.sqrt(pivData[c][2] * pivData[c][2] + pivData[c][3]
					* pivData[c][3]);
			if (vc < threshold && vt > vc) {
				// delete rest of column
				for (c = c - horSize; c > 0; c -= horSize) {
					pivData[c][2] = 0.0;
					pivData[c][3] = 0.0;
				}
			}
		}
		// go to the bottom
		for (int c = cStart; c < pivData.length - horSize; c += horSize) {
			vc = Math.sqrt(pivData[c][2] * pivData[c][2] + pivData[c][3]
					* pivData[c][3]);
			vb = Math.sqrt(pivData[c + horSize][2] * pivData[c + horSize][2]
					+ pivData[c + horSize][3] * pivData[c + horSize][3]);
			if (vc < threshold && vb > vc) {
				// delete rest of column
				for (c = c + horSize; c < pivData.length; c += horSize) {
					pivData[c][2] = 0.0;
					pivData[c][3] = 0.0;
				}
			}
		}
	}

}