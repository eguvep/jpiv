/*
 * CmdFrame.java
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
 * A text frame providing some editing and scripting functionality. BeanShell
 * script code can be selected and executed within this frame. System output can
 * be redirected to this window also. This component is beeing designed as a
 * child frame of a jpiv2.JPiv() component.
 */
public class CmdFrame extends javax.swing.JInternalFrame {

	/**
	 * Constructor.
	 * 
	 * @param jpiv
	 *            The parent component.
	 */
	public CmdFrame(jpiv2.JPiv jpiv) {
		initComponents();
		this.jpiv = jpiv;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed"
	// desc=" Generated Code ">//GEN-BEGIN:initComponents
	private void initComponents() {
		jPopupMenu = new javax.swing.JPopupMenu();
		jMenuItemExecSelection = new javax.swing.JMenuItem();
		jSeparator1 = new javax.swing.JSeparator();
		jMenuItemCut = new javax.swing.JMenuItem();
		jMenuItemCopy = new javax.swing.JMenuItem();
		jMenuItemPaste = new javax.swing.JMenuItem();
		jMenuItemDelete = new javax.swing.JMenuItem();
		jSeparator2 = new javax.swing.JSeparator();
		jMenuItemClear = new javax.swing.JMenuItem();
		jScrollPane = new javax.swing.JScrollPane();
		jTextArea = new javax.swing.JTextArea();

		jMenuItemExecSelection.setText("execute selection");
		jMenuItemExecSelection
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jMenuItemExecSelectionActionPerformed(evt);
					}
				});

		jPopupMenu.add(jMenuItemExecSelection);

		jPopupMenu.add(jSeparator1);

		jMenuItemCut.setText("cut");
		jMenuItemCut.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemCutActionPerformed(evt);
			}
		});

		jPopupMenu.add(jMenuItemCut);

		jMenuItemCopy.setText("copy");
		jMenuItemCopy.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemCopyActionPerformed(evt);
			}
		});

		jPopupMenu.add(jMenuItemCopy);

		jMenuItemPaste.setText("paste");
		jMenuItemPaste.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemPasteActionPerformed(evt);
			}
		});

		jPopupMenu.add(jMenuItemPaste);

		jMenuItemDelete.setText("delete");
		jMenuItemDelete.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemDeleteActionPerformed(evt);
			}
		});

		jPopupMenu.add(jMenuItemDelete);

		jPopupMenu.add(jSeparator2);

		jMenuItemClear.setText("clear");
		jMenuItemClear.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemClearActionPerformed(evt);
			}
		});

		jPopupMenu.add(jMenuItemClear);

		setMaximizable(true);
		setResizable(true);
		setTitle("Cmd");
		jTextArea.setFont(new java.awt.Font("DialogInput", 0, 14));
		jTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				checkPopupMenue(evt);
			}

			public void mousePressed(java.awt.event.MouseEvent evt) {
				checkPopupMenue(evt);
			}

			public void mouseReleased(java.awt.event.MouseEvent evt) {
				checkPopupMenue(evt);
			}
		});

		jScrollPane.setViewportView(jTextArea);

		getContentPane().add(jScrollPane, java.awt.BorderLayout.CENTER);

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void jMenuItemExecSelectionActionPerformed(
			java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemExecSelectionActionPerformed
		executeSelection();
	}// GEN-LAST:event_jMenuItemExecSelectionActionPerformed

	private void jMenuItemClearActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemClearActionPerformed
		clear();
	}// GEN-LAST:event_jMenuItemClearActionPerformed

	private void jMenuItemDeleteActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemDeleteActionPerformed
		replaceSelection("");
	}// GEN-LAST:event_jMenuItemDeleteActionPerformed

	private void jMenuItemPasteActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemPasteActionPerformed
		paste();
	}// GEN-LAST:event_jMenuItemPasteActionPerformed

	private void jMenuItemCopyActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemCopyActionPerformed
		copy();
	}// GEN-LAST:event_jMenuItemCopyActionPerformed

	private void jMenuItemCutActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemCutActionPerformed
		cut();
	}// GEN-LAST:event_jMenuItemCutActionPerformed

	private void checkPopupMenue(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_checkPopupMenue
		if (evt.isPopupTrigger()) {
			jPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	}// GEN-LAST:event_checkPopupMenue

	/**
	 * Appends a string at the end of the text.
	 * 
	 * @param str
	 *            The string being appended.
	 */
	public synchronized void append(String str) {
		jTextArea.append(str);
		jTextArea.setCaretPosition(jTextArea.getText().length());
		limitLineCount();
	}

	/**
	 * Hands selected text over to a jpiv2.CmdInterpreter() object. The method
	 * automatically gets a handle to the actual jpiv2.CmdInterpreter() object
	 * by calling the java2.JPiv.getCmdInterpreter() method.
	 */
	public void executeSelection() {
		jpiv2.CmdInterpreter cmdInterpreter = jpiv.getCmdInterpreter();
		String[] str = new String[1];
		str[0] = jTextArea.getSelectedText();
		cmdInterpreter.execute(str, cmdInterpreter.TYPE_JAVACODE);
	}

	/** Deletes the whole text. */
	public void clear() {
		jTextArea.setText("");
	}

	/** Moves the currently selected text to the system clipboard */
	public void cut() {
		jTextArea.cut();
	}

	/** Copies the currently selected text to the system clipboard */
	public void copy() {
		jTextArea.copy();
	}

	/** Copies the currently selected text from the system clipboard */
	public void paste() {
		jTextArea.paste();
		limitLineCount();
	}

	/**
	 * Replaces the selected text with 'content'.
	 * 
	 * @param content
	 *            A string that repaces the selection.
	 */
	public void replaceSelection(String content) {
		jTextArea.replaceSelection(content);
		limitLineCount();
	}

	/**
	 * Sets a new value for the maximum number of rows.
	 * 
	 * @param num
	 *            Maximum number of displayed text lines.
	 */
	public void setNumOfRows(int num) {
		if (num > 0)
			this.numOfRows = num;
		limitLineCount();
	}

	/**
	 * Activates or deactivates line wrap.
	 * 
	 * @param wrap
	 *            If this value is true the lines are wrapped at the border of
	 *            the text field. If this value is false a horizontal scroll bar
	 *            appears in case a line is longer than the width of the text
	 *            field.
	 */
	public void setLineWrap(boolean wrap) {
		jTextArea.setLineWrap(wrap);
	}

	/**
	 * Returns the text of the text area in a single String
	 * 
	 * @return The text of the JTextArea.
	 */
	public String getText() {
		return (jTextArea.getText());
	}

	/** Deletes the first part of the text, leaving 'numOfRows' lines. */
	private void limitLineCount() {
		int lineCount = jTextArea.getLineCount();
		if (lineCount > numOfRows) {
			try {
				jTextArea.replaceRange("", 0,
						jTextArea.getLineEndOffset(lineCount - numOfRows));
			} catch (javax.swing.text.BadLocationException e) {
				System.err.println("jpiv2.CmdFrame.limitLineCount(): ");
				System.err.println(e.toString());
			}
		}
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JMenuItem jMenuItemClear;
	private javax.swing.JMenuItem jMenuItemCopy;
	private javax.swing.JMenuItem jMenuItemCut;
	private javax.swing.JMenuItem jMenuItemDelete;
	private javax.swing.JMenuItem jMenuItemExecSelection;
	private javax.swing.JMenuItem jMenuItemPaste;
	private javax.swing.JPopupMenu jPopupMenu;
	private javax.swing.JScrollPane jScrollPane;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JSeparator jSeparator2;
	private javax.swing.JTextArea jTextArea;
	// End of variables declaration//GEN-END:variables

	private jpiv2.JPiv jpiv;
	private int numOfRows = 1000;
}
