/*
 * EnumerationListCellRenderer.java
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

import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * Numbers JList elements. Use
 * <code>jList.setCellRenderer(new EnumerationRenderer())</code> to print the
 * JList index of each element at the first position of the jLabel.
 * 
 * @see javax.swing.DefaultListCellRenderer
 */

public class EnumerationListCellRenderer extends DefaultListCellRenderer {

	/**
	 * Overwrites super class method.
	 * 
	 * @param list
	 *            The JList we're painting.
	 * @param value
	 *            The value returned by list.getModel().getElementAt(index).
	 * @param index
	 *            The cells index.
	 * @param isSelected
	 *            True if the specified cell was selected.
	 * @param cellHasFocus
	 *            True if the specified cell has the focus.
	 * @return this EnumerationListCellRenderer
	 */
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		String format = "0";
		int digits = (int) (Math.log((list.getModel().getSize()) - 1) / Math
				.log(10));
		for (int n = 0; n < digits; ++n) {
			format += "0";
		}
		DecimalFormat df = new DecimalFormat(format);
		setText(df.format(index + 1) + ". " + value);
		return this;
	}
}
