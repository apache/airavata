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

package org.apache.airavata.xbaya.gpel.component.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryLoader;
import org.apache.airavata.xbaya.gpel.component.GPELRegistry;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.util.RegistryConstants;
import org.apache.airavata.xbaya.workflow.WorkflowClient.WorkflowType;

public class GPELRegistryWindow {

    private XBayaEngine engine;

    private ComponentRegistryLoader loader;

    private XBayaDialog dialog;

    private XBayaTextField maxTextField;

    private JRadioButton templateRadioButton;

    private JRadioButton instanceRadioButton;

    /**
     * @param engine
     */
    public GPELRegistryWindow(XBayaEngine engine) {
        this.engine = engine;
        this.loader = ComponentRegistryLoader.getLoader(this.engine, RegistryConstants.REGISTRY_TYPE_GPEL);
        initGUI();
    }

    /**
     * Displays the dialog.
     */
    public void show() {
        if (this.maxTextField.getText().length() == 0) {
            // Show the default value.
            int max = 20;
            this.maxTextField.setText("" + max);
        }
        this.instanceRadioButton.setEnabled(false); // Not supported
        this.templateRadioButton.setSelected(true); // template is the default.
        this.dialog.show();
    }

    private void hide() {
        this.dialog.hide();
    }

    private void ok() {
        String maxString = this.maxTextField.getText();

        if (maxString.length() == 0) {
            this.engine.getErrorWindow().error(ErrorMessages.GPEL_MAX_EMPTY);
            return;
        }
        int max;
        try {
            max = Integer.parseInt(maxString);
        } catch (NumberFormatException e) {
            this.engine.getErrorWindow().error(ErrorMessages.GPEL_MAX_WRONG, e);
            return;
        }

        WorkflowType type;
        if (this.instanceRadioButton.isSelected()) {
            type = WorkflowType.INSTANCE;
        } else {
            type = WorkflowType.TEMPLATE;
        }

        GPELRegistry registry = new GPELRegistry(this.engine, type, max);

        hide();

        this.loader.load(registry);
    }

    /**
     * Initializes the GUI.
     */
    private void initGUI() {

        this.maxTextField = new XBayaTextField();
        XBayaLabel maxLabel = new XBayaLabel("Maximum", this.maxTextField);

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(maxLabel);
        infoPanel.add(this.maxTextField);
        infoPanel.layout(1, 2, GridPanel.WEIGHT_NONE, 1);

        this.templateRadioButton = new JRadioButton("Template");
        this.instanceRadioButton = new JRadioButton("Instance");
        ButtonGroup serviceTypeButtonGroup = new ButtonGroup();
        serviceTypeButtonGroup.add(this.templateRadioButton);
        serviceTypeButtonGroup.add(this.instanceRadioButton);

        GridPanel radioButtonPanel = new GridPanel();
        radioButtonPanel.add(this.templateRadioButton);
        radioButtonPanel.add(this.instanceRadioButton);
        radioButtonPanel.layout(1, 2, 0, GridPanel.WEIGHT_NONE);

        GridPanel mainPaenl = new GridPanel();
        mainPaenl.add(infoPanel);
        mainPaenl.add(radioButtonPanel);
        mainPaenl.layout(2, 1, GridPanel.WEIGHT_NONE, 0);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, "GPEL Registry", mainPaenl, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }
}