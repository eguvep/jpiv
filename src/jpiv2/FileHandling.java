/*
 * FileHandling.java
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
//import ucar.nc2.NetcdfFile;
//import ucar.ma2.ArrayFloat;
//import ucar.ma2.ArrayByte;
//import ucar.nc2.Dimension;
//import ucar.nc2.NCdump;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamTokenizer;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * A collection of convenience methods for file handling. All methods are
 * declared 'static', so it is not necessary to create an object to use this
 * methods.
 * 
 */
public final class FileHandling {

	/**
	 * Serializes an object to a file.
	 * 
	 * @param pathname
	 *            Pathname for saving the object.
	 * @param obj
	 *            Any object.
	 * @throws java.io.IOException
	 *             In case the file can not be written.
	 */
	public static void serialize(String pathname, Object obj)
			throws java.io.IOException {
		FileOutputStream fos = new FileOutputStream(pathname);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(obj);
		oos.close();
	}

	/**
	 * Restores an object from a file.
	 * 
	 * @param pathname
	 *            Pathname of the object.
	 * @return obj Any object.
	 * @throws java.io.IOException
	 *             In case the file can not be read.
	 * @throws java.io.FileNotFoundException
	 *             In case the file could not be found.
	 * @throws java.lang.ClassNotFoundException
	 *             In case the object can not be de-serialized. This can happen
	 *             after updates or when the file was edited elsewhere.
	 */
	public static Object deSerialize(String pathname)
			throws java.io.IOException, java.io.FileNotFoundException,
			java.lang.ClassNotFoundException {
		Object obj = new Object();
		File file = new File(pathname);
		if (file.exists()) {
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);
			obj = ois.readObject();
			ois.close();
		} else {
			throw new FileNotFoundException();
		}
		return (obj);
	}

	/**
	 * Get a decimal format for integer counters. Constructs a decimal format
	 * based on a string that has as many zeros as 'number' has digits. The
	 * number 156 for example will lead to a decimal format based on the string
	 * '000'. This method is useful if you want to enumerate something with the
	 * least number of leading zeros.
	 * 
	 * @param number
	 *            The maximum value of your enumeration.
	 * @return The number format.
	 */
	public static java.text.DecimalFormat getCounterFormat(int number) {
		int digits = 1 + (int) (Math.log(number) / Math.log(10));
		String format = new String();
		for (int i = 0; i < digits; i++)
			format += "0";
		return (new java.text.DecimalFormat(format));
	}

	/**
	 * Extracts the file extension from a java.io.File object.
	 * 
	 * @param f
	 *            The java.io.File object.
	 * @return The file extension or null if there is no extension.
	 */
	public static String getFileExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	/**
	 * Extracts the file extension from a String object.
	 * 
	 * @param filename
	 *            The java.io.File object.
	 * @return The file extension or null if there is no extension.
	 */
	public static String getFileExtension(String filename) {
		String ext = null;
		int i = filename.lastIndexOf('.');
		if (i > 0 && i < filename.length() - 1) {
			ext = filename.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	/**
	 * Get the body of a filename.
	 * 
	 * @param f
	 *            The file.
	 * @return The name of this file without an extension and path.
	 */
	public static String stripFilename(File f) {
		String name = f.getName();
		return (stripFilename(name));
	}

	/**
	 * Removes the characters after the last occurence of a "." in path
	 * inclusive the ".".
	 * 
	 * @param path
	 *            A string representing a complete file name inclusive filename
	 *            extension.
	 * @return The same string with the filename extension removed.
	 */
	public static String stripExtension(String path) {
		int index = path.lastIndexOf('.');
		if (index != -1)
			path = path.substring(0, index);
		return path;
	}

	/**
	 * Get the body of a filename.
	 * 
	 * @param path
	 *            The complete filename.
	 * @return The name of this file without an extension and path.
	 */
	public static String stripFilename(String path) {
		int start = path.lastIndexOf(getFileSeparator()) + 1;
		int end = path.lastIndexOf('.');
		if (end == -1)
			end = path.length();
		return (path.substring(start, end));
	}

	/**
	 * Writes an array whitespace delimited to a file.
	 * 
	 * @param array
	 *            A two dimensional array.
	 * @param pathname
	 *            The full pathname of a destination file.
	 * @param df
	 *            A DecimalFormat for formatting the output.
	 * @param header
	 *            A file header.
	 * @throws IOException
	 *             In case the file can not be created.
	 */
	public static void writeArrayToFile(double[][] array, String pathname,
			DecimalFormat df, String header) throws IOException {
		BufferedWriter file = new BufferedWriter(new FileWriter(pathname));
		file.write(header);
		for (int i = 0; i < array.length; ++i) {
			for (int j = 0; j < array[0].length; ++j) {
				file.write(" " + df.format(array[i][j]));
			}
			file.newLine();
		}
		file.flush();
		file.close();
	}

	/**
	 * Writes an array whitespace delimited to a file.
	 * 
	 * @param array
	 *            A two dimensional array.
	 * @param pathname
	 *            The full pathname of a destination file.
	 * @param df
	 *            A DecimalFormat for formatting the output.
	 * @throws IOException
	 *             in case the file can not be created.
	 */
	public static void writeArrayToFile(int[] array, String pathname,
			DecimalFormat df) throws IOException {
		BufferedWriter file = new BufferedWriter(new FileWriter(pathname));
		for (int i = 0; i < array.length; ++i) {
			file.write(" " + df.format(array[i]));
		}
		file.flush();
		file.close();
	}

	/**
	 * Reads whitespace delimited table data from an ascii or unicode file. This
	 * method ignores header lines that contain Tecplot keywords. There is a
	 * limit of 25 columns.
	 * 
	 * @param pathname
	 *            The absolute path of a file containing a whitespace delimited
	 *            table.
	 * @return A two dimensional double array containing the data of the table.
	 * @throws IOException
	 *             in case no file can be created from <code>pathname</code>
	 */
	public static double[][] readArrayFromFile(String pathname)
			throws IOException {
		int numOfRows = 0;
		int i = 0;
		int numOfCol = 0;
		int j = 0;
		double[] data = new double[25];
		// array list for conveniently appending the parsed rows
		// do not use a LinkedList here: bad performance
		ArrayList<double[]> al = new ArrayList<double[]>();
		// count lines that contain Tecplot key words
		BufferedReader br = new BufferedReader(new FileReader(pathname));
		int headerLines = -1;
		String line;
		do {
			line = br.readLine();
			headerLines += 1;
		} while (line.contains("TITLE") || line.contains("VARIABLES")
				|| line.contains("ZONE"));
		br.close();
		br = new BufferedReader(new FileReader(pathname));
		// skip the header
		for (int h = 0; h < headerLines; h++)
			br.readLine();
		// configuring StreamTokenizer
		StreamTokenizer st = new StreamTokenizer(br);
		st.eolIsSignificant(true);
		st.resetSyntax();
		st.wordChars('0', '9');
		st.wordChars('-', '-');
		st.wordChars('+', '+');
		st.wordChars('e', 'e');
		st.wordChars('E', 'E');
		st.wordChars('.', '.');
		st.whitespaceChars(' ', ' ');
		st.whitespaceChars('\t', '\t');
		int type = -1;
		while ((type = st.nextToken()) != StreamTokenizer.TT_EOF) {
			switch (type) {
			case StreamTokenizer.TT_WORD:
				data[j] = Double.parseDouble(st.sval);
				j++;
				break;
			case StreamTokenizer.TT_EOL:
				// at the end of the line, the data is appended at the ArrayList
				// use clone() to copy the object and not just its reference
				al.add(data.clone());
				numOfRows++;
				numOfCol = j;
				j = 0;
				break;
			default:
				break;
			}
		}
		br.close();
		// copying the data from the ArrayList into a double-array
		double[][] array = new double[numOfRows][numOfCol];
		for (i = 0; i < numOfRows; ++i) {
			data = (double[]) al.get(i);
			System.arraycopy(data, 0, array[i], 0, numOfCol);
		}
		al.clear();
		return (array);
	}

	/**
	 * Reads netCDF files that have the format proposed by C. Willert
	 * (http://www.meol.cnrs.fr/LML/EuroPIV2/Pages/netcdf.htm)
	 * 
	 * @param pathname
	 *            The netCDF file name.
	 * @return A double array that contains the pivData in column notation (x,
	 *         y, dx, dy, flag).
	 * @throws java.io.IOException
	 *             If the specified file can not be found.
	 *
	public static double[][] readNetCdfFile(String pathname) throws IOException {
		double[][] array = null;
		try {
			NetcdfFile dataFile = NetcdfFile.open(pathname);
			Dimension data_dim_time = dataFile.getRootGroup().findDimension(
					"data_dim_time");
			Dimension data_dim_z = dataFile.getRootGroup().findDimension(
					"data_dim_z");
			Dimension data_dim_x = dataFile.getRootGroup().findDimension(
					"data_dim_x");
			Dimension data_dim_y = dataFile.getRootGroup().findDimension(
					"data_dim_y");
			if (data_dim_time == null || data_dim_z == null
					|| data_dim_x == null || data_dim_y == null) {
				System.err
						.println("This netCDF file is not compatible to the PivNet 2 standard:");
				System.err
						.println("http://www.meol.cnrs.fr/LML/EuroPIV2/Pages/netcdf.htm");
				ncView(pathname);
				throw (new IOException());
			} else if (data_dim_time.getLength() != 1
					|| data_dim_z.getLength() != 1) {
				System.err
						.println("This netCDF file contains a time series and/or volumetric data.");
				System.err
						.println("The visualization of multi dimensional data is not implemented, yet.");
				ncView(pathname);
				throw (new IOException());
			} else {
				int dim_x = data_dim_x.getLength();
				int dim_y = data_dim_y.getLength();
				ArrayFloat.D1 grid_x = (ArrayFloat.D1) dataFile.read("grid_x",
						true);
				ArrayFloat.D1 grid_y = (ArrayFloat.D1) dataFile.read("grid_y",
						true);
				ArrayFloat.D4 disp_x = (ArrayFloat.D4) dataFile.read("disp_x",
						true);
				ArrayFloat.D4 disp_y = (ArrayFloat.D4) dataFile.read("disp_y",
						true);
				ArrayByte.D4 flags = (ArrayByte.D4) dataFile
						.read("flags", true);
				// rearrange data
				int rows = dim_x * dim_y;
				array = new double[rows][5];
				int xi = 0;
				int yi = 0;
				for (int n = 0; n < rows; n++) {
					if (xi == dim_x) {
						xi = 0;
						yi++;
					}
					array[n][0] = grid_x.get(xi);
					array[n][1] = grid_y.get(yi);
					array[n][2] = disp_x.get(0, 0, yi, xi);
					array[n][3] = disp_y.get(0, 0, yi, xi);
					array[n][4] = flags.get(0, 0, yi, xi);
					xi++;
				}
			}
		} catch (ucar.ma2.InvalidRangeException e) {
			System.err.println(e.toString());
			throw (new java.io.IOException());
		}
		return (array);
	}
	*/

	/**
	 * Dumps the variable definition of any netCDF file to standard output.
	 * 
	 * @param pathname
	 *            The filename of the netCDF file.
	 *
	public static void ncView(String pathname) {
		try {
			System.err
					.println("Try to print the variable definition of the current netCDF file:");
			new NCdump().print(pathname, System.out, false, false, false, true,
					null, null);
		} catch (java.io.IOException e) {
			System.err.println(e.toString());
		}
	}
	*/

	/**
	 * Reads a file.
	 * 
	 * @param pathname
	 *            The absolute pathname of the file.
	 * @return The UTF-8 encoded file content or null in case of an exception.
	 */
	public static String readTextFile(String pathname) {
		try {
			String line = new String();
			StringBuffer sb = new StringBuffer(512);
			BufferedReader br = new BufferedReader(new FileReader(pathname));
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			return (sb.toString());
		} catch (IOException e) {
			System.out.println(e.toString());
			return null;
		}
	}

	/**
	 * Writes a file.
	 * 
	 * @param pathname
	 *            The absolute pathname of the file.
	 * @param s
	 *            The string to be written.
	 * @throws IOException
	 *             In case the file can not be written.
	 */
	public static void writeTextFile(String pathname, String s)
			throws IOException {
		BufferedWriter file = new BufferedWriter(new FileWriter(pathname));
		file.write(s);
		file.flush();
		file.close();
	}

	/**
	 * Copy a file byte by byte.
	 * 
	 * @param source
	 *            Complete path of the source file.
	 * @param dest
	 *            Complete path of the destination file.
	 */
	public static void copyFile(String source, String dest) {
		try {
			FileInputStream in = new FileInputStream(source);
			FileOutputStream out = new FileOutputStream(dest);
			byte[] buf = new byte[4096];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}

	/**
	 * Get the system dependent file separator.
	 * 
	 * @return The file separator.
	 */
	public static String getFileSeparator() {
		java.util.Properties sysProp = System.getProperties();
		return (sysProp.getProperty("file.separator"));
	}

	/**
	 * Get the absolute path to the directory of this jar-archive.
	 * 
	 * @return The pathname.
	 */
	public static String getJarDir() {
		java.util.Properties sysProp = System.getProperties();
		String sep = sysProp.getProperty("file.separator");
		String classPath = sysProp.getProperty("java.class.path");
		// if class is not packed in a jar
		int idx0 = classPath.lastIndexOf(":");
		if (idx0 == -1)
			idx0 = 0;
		// windows filesystem
		else if (classPath.charAt(idx0 + 1) == '\\')
			idx0 = idx0 - 1;
		else
			idx0 = idx0 + 1;
		int idx1 = classPath.lastIndexOf(sep);
		if (idx1 == -1)
			idx1 = classPath.length() - 1;
		String jarDir = classPath.substring(idx0, idx1);
		return (jarDir);
	}
}