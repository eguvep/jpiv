/*
 * Statistics.java
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

import java.util.Arrays;

/**
 * Some convenient methods for calculating statistical data. All methods are
 * declared static, so that it is not necessary to create a specific instance of
 * this class.
 * 
 */
public class Statistics {

	/**
	 * Gets the minimum value of an array of double values.
	 * 
	 * @param val
	 *            The data
	 * @return The minimum value
	 */
	public static double getMin(double[] val) {
		Arrays.sort(val);
		return (val[0]);
	}

	/**
	 * Gets the maximum value of an array of double values.
	 * 
	 * @param val
	 *            The data
	 * @return The maximum value
	 */
	public static double getMax(double[] val) {
		Arrays.sort(val);
		return (val[val.length - 1]);
	}

	/**
	 * Gets the median of an array of double values.
	 * 
	 * @param val
	 *            The data
	 * @return The median value. If the number of data elements is even the
	 *         artithmetic mean of the two median values is returned.
	 */
	public static double getMedian(double[] val) {
		Arrays.sort(val);
		int mid = val.length / 2;
		// uneven
		if ((val.length % 2) > 0) {
			return (val[mid]);
		}
		// even
		else {
			return ((val[mid] + val[mid - 1]) / 2);
		}
	}

	/**
	 * Gets the residuals r of the median x_m of an array of double values x_i (
	 * r_i = |x_i - x_m| ).
	 * 
	 * @param val
	 *            The data x_i.
	 * @return The residuals r_i of the median value.
	 */
	public static double[] getResidualsOfMedian(double[] val) {
		double median = getMedian(val);
		for (int i = 0; i < val.length; i++) {
			val[i] = Math.abs(val[i] - median);
		}
		return val;
	}

	/**
	 * Gets the arithmetic average of an array of double values.
	 * 
	 * @param val
	 *            The data
	 * @return The arithmetic average of the data.
	 */
	public static double getAverage(double[] val) {
		double sum = 0;
		for (int i = 0; i < val.length; i++) {
			sum += val[i];
		}
		return (sum / val.length);
	}

	/**
	 * Gets the arithmetic average of an array of float values.
	 * 
	 * @param val
	 *            The data
	 * @return The arithmetic average of the data.
	 */
	public static float getAverage(float[] val) {
		float sum = 0;
		for (int i = 0; i < val.length; i++) {
			sum += val[i];
		}
		return (sum / val.length);
	}

	/**
	 * Gets the arithmetic average of a double array of float values.
	 * 
	 * @param val
	 *            The data
	 * @return The arithmetic average of the data.
	 */
	public static float getAverage(float[][] val) {
		float sum = 0;
		for (int i = 0; i < val.length; i++) {
			for (int j = 0; j < val[0].length; j++) {
				sum += val[i][j];
			}
		}
		return (sum / val.length / val[0].length);
	}

	/**
	 * Gets the standard deviation of an array of double values.
	 * 
	 * @param val
	 *            The data
	 * @return The standard deviation of the data.
	 */
	public static double getStandardDeviation(double[] val) {
		double avg = getAverage(val);
		double sq = 0;
		for (int i = 0; i < val.length; i++) {
			sq += (val[i] - avg) * (val[i] - avg);
		}
		return (Math.sqrt(sq / (val.length - 1)));
	}

	/**
	 * Gets the gradient b of the linear regression through the given points x,
	 * y.
	 * 
	 * @param x
	 *            The independent values.
	 * @param y
	 *            The measured values.
	 * @return The gradient b of the regression curve y = a + bx.
	 */
	public static double getLinearRegressionGradient(double[] x, double[] y) {
		double xm = getAverage(x);
		double ym = getAverage(y);
		double en = 0;
		double de = 0;
		for (int i = 0; i < x.length; i++) {
			en += (x[i] - xm) * (y[i] - ym);
			de += (x[i] - xm) * (x[i] - xm);
		}
		return (en / de);
	}

	/**
	 * Calculates the root mean square error (standard deviation) based on
	 * differences with reference data (not the average of the data itself).
	 * Values in the reference data that are exactly zero are skipped.
	 * 
	 * @param referenceData
	 *            the reference data
	 * @param testData
	 *            the test data
	 * @param useBorder
	 *            ignores border values if false
	 * @return An array that contains the standard deviation of the third (dx),
	 *         fourth (dy) and fifth (flag) data colum.
	 */
	public static double[] comparePivDataSets(jpiv2.PivData referenceData,
			jpiv2.PivData testData, boolean useBorder) {
		double[][] ref = referenceData.getPivData();
		double[][] tst = testData.getPivData();
		int[] dim = referenceData.getDimension();
		double[] stddev = { 0.0, 0.0, 0.0 };
		int x, y;
		for (int r = 0; r < ref.length; r++) {
			y = (int) r / dim[0] + 1;
			x = r - (y - 1) * dim[0];
			if (useBorder
					|| (x != 0 && y != 0 && x != dim[0] - 1 && y != dim[1] - 1)) {
				stddev[0] += (ref[r][2] - tst[r][2]) * (ref[r][2] - tst[r][2]);
				stddev[1] += (ref[r][3] - tst[r][3]) * (ref[r][3] - tst[r][3]);
				stddev[2] += (ref[r][4] - tst[r][4]) * (ref[r][4] - tst[r][4]);
			}
		}
		for (int c = 0; c < stddev.length; c++) {
			stddev[c] = Math.sqrt(stddev[c] / (ref.length - 1));
		}
		return stddev;
	}

}
