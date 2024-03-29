/*
 * VerifierInt.java
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

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 * Integer verifier. Can be registered on an input component to verify user
 * input.
 * 
 */
public class VerifierInt extends InputVerifier {

	/** Creates a new instance of VerifierInt. */
	public VerifierInt() {
	}

	/**
	 * Checks the validity of a String typed into a JTextField.
	 * 
	 * @param input
	 *            A JTextField
	 * @return true if the String of the JTextField matches the regular
	 *         expression "^-?[0-9]+$".
	 */
	public boolean verify(JComponent input) {
		JTextField tf = (JTextField) input;
		return (tf.getText().matches("^-?[0-9]+$"));
	}

	/**
	 * Calls the verify method.
	 * 
	 * @param input
	 *            The input that needs to be verified.
	 * @return true if input is valid.
	 */
	public boolean shouldYieldFocus(JComponent input) {
		if (verify(input))
			return true;
		else {
			System.err
					.println("Invalid input. Please enter an integer number.");
			return false;
		}
	}
}
