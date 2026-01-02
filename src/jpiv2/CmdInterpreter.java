/*
 * CmdInterpreter.java
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
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Executes external applications, shell commands, and interpretes javacode. The
 * utilized class bsh.Interpreter() is part of the BeanShell project by Pat
 * Niemeyer. For more information about BeanShell see: <a
 * href="http://www.beanshell.org">http://www.beanshell.org</a>
 * 
 */
public class CmdInterpreter {

	/** The command is an external application. */
	public final static int TYPE_APPLICATION = 0;
	/** The command will be executed in a system dependent shell. */
	public final static int TYPE_SHELLCOMMAND = 1;
	/** The command is inline java code. */
	public final static int TYPE_JAVACODE = 2;
	/** The command is a file containing java code. */
	public final static int TYPE_BSHFILE = 3;

	/**
	 * Creates a new instance of a java2.CmdInterpreter. The
	 * java2.CmdInterpreter starts applications, executes native console
	 * commands and interpretes java code. For interpreting java code the
	 * bsh.eval(String str) method of the bsh package (BeanShell) is used.
	 * Methods of an already constructed instance of a jpiv2.JPiv class can be
	 * referred to in a script or command via the automatically generated handle
	 * with the name handleName. Use the get... methods of the jpiv2.JPiv object
	 * to get handles to other objects. e.g.
	 * <code>myHandle = handleName.getSettingsFrame();</code>
	 * 
	 * @param jpiv
	 *            The jpiv2.JPiv object which methods you would like to use.
	 * @param handleName
	 *            The name via that you would like to refere to the jpiv2.JPiv
	 *            object.
	 */
	public CmdInterpreter(jpiv2.JPiv jpiv, String handleName) {
		interpreter = new bsh.Interpreter();
		try {
			this.jpiv = jpiv;
			this.handleName = handleName;
			interpreter.set(handleName, jpiv);
		} catch (bsh.EvalError e) {
			// nothing
		}
	}

	/**
	 * Executes a command. The command and its parameters (or the filename
	 * containing a bsh-script) are given as elements of an array. If javacode
	 * is or a bsh-script file given, only the first array element (index 0) is
	 * taken.
	 * 
	 * @param command
	 *            An array containing a command string and parameters or the
	 *            name of a bsh-script file.
	 * @param type
	 *            A constant. The constant specifies wether the command is
	 *            executed directly, within an operating-system-specific shell
	 *            or by the BeanShell java interpreter.
	 */
	public void execute(String[] command, int type) {
		try {
			if (type == TYPE_APPLICATION) {
				runApplication(command);
			} else if (type == TYPE_SHELLCOMMAND) {
				runShellCommand(command);
			} else if (type == TYPE_JAVACODE) {
				runJavaCode(command[0]);
			} else if (type == TYPE_BSHFILE) {
				runBshFile(command[0]);
			}
		} catch (Exception e) {
			System.err.println("jpiv.ExtApp.execute: " + e.toString());
		}
	}

	/**
	 * Clears all Bean Shell variables.
	 */
	public void clearBshNamespace() {
		try {
			// clear all variables and reinitialize handle to main program
			interpreter.getNameSpace().clear();
			interpreter.set(handleName, jpiv);
		} catch (bsh.EvalError e) {
			System.err
					.println("jpiv2.CmdInterpreter.runJavaCode(String cmd): ");
			System.err.println(e.toString());
		}
	}

	/**
	 * Executes an external application and catches the progammes output. The
	 * output is written to standard out. This doesn't change anything, as long
	 * the main java application doesn't redirect the systems output. Then the
	 * external applications output is also redirected.
	 */
	private void runApplication(String[] command) throws IOException,
			InterruptedException {
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(command);
		BufferedReader out = new BufferedReader(new InputStreamReader(
				pr.getInputStream()));
		String line;
		while ((line = out.readLine()) != null) {
			System.out.println(line);
		}
		pr.waitFor();
	}

	/**
	 * Get's the current operating system and executes <code>command</code> in
	 * an operating system typical command interpreter like command.com for
	 * Microsoft Windows or bash for Unix or Linux.
	 */
	private void runShellCommand(String[] command) throws IOException,
			InterruptedException {
		String os = System.getProperty("os.name");
		os = os.toLowerCase();
		String[] cmd = new String[command.length + 2];
		if (os.indexOf("windows") != -1) {
			if (os.contains("95"))
				cmd[0] = "command.com";
			else if (os.contains("98"))
				cmd[0] = "command.com";
			else
				cmd[0] = "cmd.exe";
			cmd[1] = "/c";
		} else if (os.contains("unix")) {
			cmd[0] = "bash";
			cmd[1] = "-c";
		} else if (os.contains("linux")) {
			cmd[0] = "bash";
			cmd[1] = "-c";
                } else if (os.contains("mac")) {
			cmd[0] = "bash";
			cmd[1] = "-c";
		} else {
			cmd[0] = "unsupported os";
			System.err.println("The execution of shell commands is not yet "
					 + "supported on the current platform: " + os
                                         + ". Please report this on https://github.com/eguvep/jpiv/issues/");
		}
		if (!cmd[0].equals("unsupported os")) {
			for (int i = 0; i < command.length; ++i) {
				cmd[i + 2] = command[i];
			}
			runApplication(cmd);
		}
	}

	/**
	 * Executes a command using the bsh.eval(String str) method.
	 * 
	 * @param cmd
	 *            A string containing java-code.
	 */
	private void runJavaCode(String cmd) {
		if (cmd != null) {
			try {
				interpreter.eval(cmd);
			} catch (bsh.EvalError e) {
				System.err
						.println("jpiv2.CmdInterpreter.runJavaCode(String cmd): ");
				System.err.println(e.toString());
			}
		}
	}

	/**
	 * Executes a bean shell script file using the bsh.source(String str)
	 * method.
	 * 
	 * @param path
	 *            The filename.
	 */
	private void runBshFile(String path) {
		if (path != null) {
			try {
				interpreter.source(path);
			} catch (Exception e) {
				System.err
						.println("jpiv2.CmdInterpreter.runBshFile(String path): ");
				System.err.println(e.toString());
			}
		}
	}

	// Variables declaration
	private bsh.Interpreter interpreter;
	private jpiv2.JPiv jpiv;
	private String handleName;
}
