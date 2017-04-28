/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.xbaya.ui.dialogs;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.widgets.XBayaComponent;

public class XBayaDialog {

    private Window owner;

    private String title;

    private String description;

    private JDialog dialog;

    private JComponent mainPanel;

    private JComponent buttonPanel;

    /**
     * Constructs an XBayaDialog.
     * 
     * @param engine
     * @param title
     * @param mainPanel
     * @param buttonPanel
     */
    public XBayaDialog(XBayaGUI xbayaGUI, String title, XBayaComponent mainPanel, XBayaComponent buttonPanel) {
        this(xbayaGUI.getFrame(), title, mainPanel.getSwingComponent(), buttonPanel.getSwingComponent());
    }

    /**
     * Constructs an XBayaDialog.
     * 
     * @param engine
     * @param title
     * @param mainPanel
     * @param buttonPanel
     */
    public XBayaDialog(XBayaGUI xbayaGUI, String title, XBayaComponent mainPanel, JComponent buttonPanel) {
        this(xbayaGUI.getFrame(), title, mainPanel.getSwingComponent(), buttonPanel);
    }

    /**
     * Constructs an XBayaDialog.
     * 
     * @param engine
     * @param title
     * @param description
     * @param mainPanel
     * @param buttonPanel
     */
    public XBayaDialog(XBayaGUI xbayaGUI, String title, String description, XBayaComponent mainPanel,
            JComponent buttonPanel) {
        this(xbayaGUI.getFrame(), title, description, mainPanel.getSwingComponent(), buttonPanel);
    }

    /**
     * Constructs an XBayaDialog.
     * 
     * @param engine
     * @param title
     * @param mainPanel
     * @param buttonPanel
     */
    public XBayaDialog(XBayaGUI xbayaGUI, String title, JComponent mainPanel, JComponent buttonPanel) {
        this(xbayaGUI.getFrame(), title, mainPanel, buttonPanel);
    }

    /**
     * Constructs an XBayaDialog.
     * 
     * @param owner
     * @param title
     * @param mainPanel
     * @param buttonPanel
     */
    public XBayaDialog(Window owner, String title, XBayaComponent mainPanel, JComponent buttonPanel) {
        this(owner, title, mainPanel.getSwingComponent(), buttonPanel);
    }

    /**
     * Constructs an XBayaDialog.
     * 
     * @param owner
     * @param title
     * @param mainPanel
     * @param buttonPanel
     */
    public XBayaDialog(Window owner, String title, JComponent mainPanel, JComponent buttonPanel) {
        this(owner, title, null, mainPanel, buttonPanel);
    }

    /**
     * Constructs an XBayaDialog.
     * 
     * @param owner
     * @param title
     * @param description
     * @param mainPanel
     * @param buttonPanel
     */
    public XBayaDialog(Window owner, String title, String description, JComponent mainPanel, JComponent buttonPanel) {
        this.owner = owner;
        this.title = title;
        this.description = description;
        this.mainPanel = mainPanel;
        this.buttonPanel = buttonPanel;
        init();
    }

    /**
     * @return The dialog.
     */
    public JDialog getDialog() {
        return this.dialog;
    }

    /**
     * Determines whether this component should be visible when its parent is visible. Components are initially visible,
     * with the exception of top level components such as <code>Frame</code> objects.
     * 
     * @return <code>true</code> if the component is visible, <code>false</code> otherwise
     */
    public boolean isVisible() {
        return this.dialog.isVisible();
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        this.dialog.pack();

        // Adjust the size if it's bigger than the screen.
        Dimension size = this.dialog.getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int inset = 100;
        int width = size.width;
        if (width > screenSize.width) {
            width = screenSize.width - inset;
        }
        int height = size.height;
        if (height > screenSize.height) {
            height = screenSize.height - inset;
        }
        this.dialog.setSize(width, height);

        this.dialog.setLocationRelativeTo(this.owner);
        this.dialog.setVisible(true);
    }

    /**
     * Just set dialog visible Do NOTHING to layout
     */
    public void simpeShow() {
        this.dialog.setVisible(true);
    }

    /**
     * Hides the dialog.
     */
    public void hide() {
        this.dialog.setVisible(false);
    }

    /**
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
        this.dialog.setTitle(title);
    }

    /**
     * @param button
     */
    public void setDefaultButton(JButton button) {
        this.dialog.getRootPane().setDefaultButton(button);
    }
    
    public void setCancelButton(final JButton button){
    	this.dialog.getRootPane().addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode()==27){
					button.getAction().actionPerformed(null);
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
			}
    		
    	});
    }

    private void init() {
        if (this.owner instanceof Frame) {
            this.dialog = new JDialog((Frame) this.owner);
        } else if (this.owner instanceof Dialog) {
            this.dialog = new JDialog((Dialog) this.owner);
        } else {
            // This should not happen.
            throw new WorkflowRuntimeException("The owner component was neither Frame or Dialog.");
        }
        this.dialog.setTitle(this.title);
        this.dialog.setModal(true);
        this.dialog.setResizable(true);

        Container contentPane = this.dialog.getContentPane();
        int numRow = 0;
        if (this.description != null && this.description.length() > 0) {
            JLabel descriptionLabel = new JLabel("<html>" + this.description + "</html>");

            contentPane.add(descriptionLabel);
            numRow++;
        }
        contentPane.add(this.mainPanel);
        numRow++;
        contentPane.add(this.buttonPanel);
        numRow++;
        SwingUtil.layoutToGrid(contentPane, numRow, 1, numRow - 2, 0);
    }
}