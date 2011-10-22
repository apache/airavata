/*
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
 *
 */

package org.apache.airavata.xbaya.appwrapper;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaComboBox;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.registry.RegistryAccesser;

public class RegisterThroughFileWindow {
    private XBayaDialog dialog;

    private XBayaComboBox docTypeComboBox;
    private XBayaTextField fileLocationField;

    private JButton fileChooserButton;
    private JButton registerButton;
    private JButton closeButton;

    private XBayaEngine engine;

    private static RegisterThroughFileWindow window;

    /**
     * Constructs a AmazonS3UtilsWindow.
     * 
     * @param engine
     */
    private RegisterThroughFileWindow(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    private RegisterThroughFileWindow() {
    }

    /**
     * @return ApplicationRegistrationWindow
     */
    public static RegisterThroughFileWindow getInstance() {
        if (window == null) {
            window = new RegisterThroughFileWindow();
        }

        return window;
    }

    /**
     * @return Status
     */
    public boolean isEngineSet() {
        if (this.engine == null) {
            return false;
        }
        return true;
    }

    /**
	 * 
	 */
    private void initGUI() {
        GridPanel infoPanel = new GridPanel();
        this.docTypeComboBox = new XBayaComboBox(new javax.swing.DefaultComboBoxModel(new String[] { "Host",
                "Application", "Service" }));
        XBayaLabel docTypeLabel = new XBayaLabel("Document Type", this.docTypeComboBox);
        this.fileLocationField = new XBayaTextField();
        XBayaLabel fileLocationLabel = new XBayaLabel("File Location", this.fileLocationField);
        JLabel dummyLabel = new JLabel("");
        this.fileChooserButton = new JButton("Choose File...");
        this.fileChooserButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jf = new JFileChooser();
                int returnVal = jf.showOpenDialog(RegisterThroughFileWindow.this.dialog.getDialog());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    RegisterThroughFileWindow.this.fileLocationField.setText(jf.getSelectedFile().getAbsolutePath());
                }
            }
        });

        infoPanel.add(docTypeLabel);
        infoPanel.add(this.docTypeComboBox);
        infoPanel.add(fileLocationLabel);
        infoPanel.add(this.fileLocationField);
        infoPanel.add(dummyLabel);
        infoPanel.add(this.fileChooserButton);
        infoPanel.layout(3, 2, GridPanel.WEIGHT_NONE, 1);

        JPanel buttonPanel = new JPanel();
        this.registerButton = new JButton("Register");
        this.registerButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!RegisterThroughFileWindow.this.fileLocationField.getText().isEmpty()) {
                    register();
                } else {
                    JOptionPane.showMessageDialog(RegisterThroughFileWindow.this.dialog.getDialog(),
                            "Please set input file path", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        buttonPanel.add(this.registerButton);

        this.closeButton = new JButton("Close");
        this.closeButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        buttonPanel.add(this.closeButton);

        this.dialog = new XBayaDialog(this.engine, "Register Description Through File", infoPanel, buttonPanel);
        this.dialog.setDefaultButton(this.closeButton);
    }

    /**
	 * 
	 */
    protected void register() {
        try {
            RegistryAccesser xregistryAccesser = new RegistryAccesser(this.engine);
            StringBuffer fileData = new StringBuffer(1000);
            BufferedReader reader = new BufferedReader(new FileReader(this.fileLocationField.getText()));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
            reader.close();

            // if (this.docTypeComboBox.getText().equals("Host")) {
            // xregistryAccesser.registerHost(fileData.toString());
            // } else if (this.docTypeComboBox.getText().equals("Application")) {
            // xregistryAccesser.registerApplication(fileData.toString());
            // } else {
            // xregistryAccesser.registerService(fileData.toString());
            // }

            JOptionPane.showMessageDialog(RegisterThroughFileWindow.this.dialog.getDialog(),
                    this.docTypeComboBox.getText() + " description registered successfully", "Successfully",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(RegisterThroughFileWindow.this.dialog.getDialog(),
                    this.docTypeComboBox.getText() + " description registration failed", "Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * @param engine
     */
    public void setXBayaEngine(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    /**
     * hide the dialog
     */
    public void hide() {
        this.dialog.hide();
    }

    /**
     * show the dialog
     */
    public void show() {
        this.dialog.show();
    }
}