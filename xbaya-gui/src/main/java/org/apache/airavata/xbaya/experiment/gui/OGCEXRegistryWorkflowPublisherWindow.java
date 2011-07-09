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

package org.apache.airavata.xbaya.experiment.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.graph.gui.GraphCanvas;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextArea;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.wf.Workflow;

public class OGCEXRegistryWorkflowPublisherWindow {

    private XBayaEngine engine;

    private XBayaDialog dialog;

    private JButton okButton;

    private boolean makePublic = false;

    private JCheckBox chkMakePublic;

    private XBayaTextArea descriptionTextArea;

    private XBayaTextField nameTextField;

    private Workflow workflow;

    /**
     * Constructs a OGCEXRegistryWorkflowPublisherWindow.
     * 
     * @param engine
     */
    public OGCEXRegistryWorkflowPublisherWindow(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    private void ok() {
        // if(Pattern.matches("[\\s | \\w]+", this.messageField.getText())){
        // this.okButton.setEnabled(false);
        // this.hide();
        // }else{
        // this.engine.getErrorWindow().warning("Invalid name");
        // }
        String name = this.nameTextField.getText();
        // if (name != null && name.equals(StringUtil.convertToJavaIdentifier(name))) {
        String description = this.descriptionTextArea.getText();

        GraphCanvas graphCanvas = this.engine.getGUI().getGraphCanvas();
        graphCanvas.setNameAndDescription(name, description);
        hide();
        // }else{
        // this.nameTextField.setText(StringUtil.convertToJavaIdentifier(name));
        // JOptionPane.showMessageDialog(this.engine.getGUI().getFrame(),
        // "Invalid Name. Please consider the Name suggsted", "Invalid Name", JOptionPane.OK_OPTION);
        // }
    }

    /**
     * Show the workflow name and description
     */
    public void show() {
        this.workflow = this.engine.getWorkflow();
        String name = this.workflow.getName();
        this.nameTextField.setText(name);

        String description = this.workflow.getDescription();
        this.descriptionTextArea.setText(description);

        this.dialog.show();
    }

    /**
     * Hide the workflow name and description
     */
    public void hide() {
        this.dialog.hide();
    }

    /**
     * Intialize UI
     */
    private void initGUI() {

        this.nameTextField = new XBayaTextField();
        XBayaLabel nameLabel = new XBayaLabel("Name", this.nameTextField);

        this.descriptionTextArea = new XBayaTextArea();
        XBayaLabel descriptionLabel = new XBayaLabel("Description", this.descriptionTextArea);

        JPanel buttonPanel = new JPanel();
        this.okButton = new JButton("OK");
        this.okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }

        });
        buttonPanel.add(this.okButton);

        GridPanel mainPanel = new GridPanel();
        TitledBorder border = new TitledBorder(new EtchedBorder(), "Save Workflow to XRegistry");
        mainPanel.getSwingComponent().setBorder(border);
        mainPanel.add(nameLabel);
        mainPanel.add(this.nameTextField);
        mainPanel.add(descriptionLabel);
        mainPanel.add(this.descriptionTextArea);
        chkMakePublic = new JCheckBox("Make public");
        mainPanel.add(chkMakePublic);
        mainPanel.layout(2, 2, 0, 0);

        this.dialog = new XBayaDialog(this.engine, "Save Workflow to XRegistry", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(this.okButton);
    }

    /**
     * Verify if the public checkbox is selected or not
     * 
     * @return true or false
     */
    public boolean isMakePublic() {
        return this.chkMakePublic.isSelected();
    }
}