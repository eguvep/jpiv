/*
 * SystemOutStream.java
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * This class is used to copy system output to a jpiv2.CmdFrame. To catch the
 * System output, do someting like this:<br>
 * <code>// creating a new CmdFrame<br>
 * cmdFrame = new jpiv2.CmdFrame(this);<br>
 * // creating a new SystemOutStream object from standard out<br>
 * jpiv2.SystemOutStream out = new jpiv.SystemOutStream(System.out);<br>
 * // assigning the CmdFrame to the new SystemOutStream object<br>
 * out.assignCmdFrame(cmdFrame);<br>
 * // redirecting standard output to the new PrintStream object<br>
 * System.setOut(out);</code>
 * <br>
 * jpiv2.SystemOutStream is a PrintStream object. That means that a standard
 * output stream can be redirected to it by System.setOut(PrintStream ps). To
 * get the output to the screen, make this SystemOutStream familiar with your
 * jpiv2.CmdFrame by using the method assignCmdFrame(jpiv2.CmdFrame cmdFrame).
 * The write methods in jpiv2.SystemOutStream (overridden in super-class) write
 * the standard output to the assigned CmdFrame by using
 * jpiv2.CmdFrame.append(String str).
 * 
 */
public class SystemOutStream extends PrintStream {

	/**
	 * Creates a new instance of <code>SystemOutStream</code>.
	 * 
	 * @param ps
	 *            A PrintStream object like <code>System.out</code>.
	 */
	public SystemOutStream(PrintStream ps) {
		super(ps);
	}

	/**
	 * Defines the destination for the system output.
	 * 
	 * @param cmdFrame
	 *            The jpiv2.CmdFrame that serves as a destination for system
	 *            output.
	 */
	public void assignCmdFrame(jpiv2.CmdFrame cmdFrame) {
		this.cmdFrame = cmdFrame;
	}

	/**
	 * Overrides <code>write(int b)</code> in PrintStream. The data is written
	 * to the assigned jpiv2.CmdFrame.
	 * 
	 * @param b
	 *            A value to be written.
	 */
	public void write(int b) {
		cmdFrame.append(String.valueOf(b));
		// uncomment the following line to write output also to standard out:
		// super.write(b);
	}

	/**
	 * Overrides <code>write(byte buf[], int off, int len)</code> in
	 * PrintStream. The data is written to the assigned jpiv2.CmdFrame.
	 * 
	 * @param buf
	 *            A byte buffer to be written.
	 * @param off
	 *            Index of first written buffer element.
	 * @param len
	 *            Number of written buffer elements.
	 */
	public void write(byte buf[], int off, int len) {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new ByteArrayInputStream(buf, off, len)));
		char[] cbuf = new char[len];
		try {
			while ((br.read(cbuf)) != -1) {
				cmdFrame.append(String.valueOf(cbuf));
			}
		} catch (IOException e) {
			System.err
					.println("jpiv2.SystemOutStream.write(byte buf[], int off, int len): ");
			System.err.println(e.toString());
		}
		// uncomment the following line to write output also to standard out:
		// super.write(buf, off, len);
	}

	// Variables declaration
	private jpiv2.CmdFrame cmdFrame;
}
