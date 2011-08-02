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

package org.apache.airavata.xbaya.monitor.gui;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.monitor.KarmaClient.Rate;

public class ProvenanceDialog {

    private XBayaEngine engine;

    private XBayaDialog dialog;

    private ProvenanceRegisterer registerer;

    private XBayaTextField urlTextField;

    private XBayaTextField idTextField;

    private JRadioButton originalButton;

    private JRadioButton periodicButton;

    private JRadioButton batchButton;

    /**
     * @param engine
     * 
     */
    public ProvenanceDialog(XBayaEngine engine) {
        this.engine = engine;
        this.registerer = new ProvenanceRegisterer(engine);
        initGui();
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        XBayaConfiguration configuration = this.engine.getConfiguration();
        this.urlTextField.setText(configuration.getKarmaURL());
        this.idTextField.setText(configuration.getKarmaWorkflowInstanceID());

        this.dialog.show();
    }

    /**
     * Hides the dialog.
     */
    private void hide() {
        this.dialog.hide();
    }

    private void setConfiguration() {
        String urlAsString = this.urlTextField.getText();
        String idAsString = this.idTextField.getText();
        Rate rate = Rate.BATCH;
        if (this.originalButton.isSelected()) {
            rate = Rate.ORIGINAL;
        } else if (this.periodicButton.isSelected()) {
            rate = Rate.PERIODIC;
        } else if (this.batchButton.isSelected()) {
            rate = Rate.BATCH;
        }

        if (urlAsString.length() == 0) {
            this.engine.getErrorWindow().error("Karma URL cannot be empty");
            return;
        }
        URI url;
        try {
            url = new URI(urlAsString).parseServerAuthority();
        } catch (URISyntaxException e) {
            String message = "Karma URL is in a wrong format";
            this.engine.getErrorWindow().error(message, e);
            return;
        }

        if (idAsString.length() == 0) {
            this.engine.getErrorWindow().error("Workflow isntance ID cannot be empty");
            return;
        }
        URI id;
        try {
            id = new URI(idAsString).parseServerAuthority();
        } catch (URISyntaxException e) {
            String message = "Workflow instance ID is in a wrong format";
            this.engine.getErrorWindow().error(message, e);
            return;
        }

        hide();

        this.registerer.register(url, id, rate);
    }

    private void initGui() {
        this.urlTextField = new XBayaTextField();
        XBayaLabel brokerLabel = new XBayaLabel("Karma URL", this.urlTextField);
        this.idTextField = new XBayaTextField();
        XBayaLabel idLabel = new XBayaLabel("Workflow Instance ID", this.idTextField);

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(brokerLabel);
        infoPanel.add(this.urlTextField);
        infoPanel.add(idLabel);
        infoPanel.add(this.idTextField);
        infoPanel.layout(2, 2, GridPanel.WEIGHT_NONE, 1);

        this.originalButton = new JRadioButton("Original");
        this.originalButton.setSelected(true);
        this.periodicButton = new JRadioButton("Periodic");
        this.batchButton = new JRadioButton("Batch");
        ButtonGroup serviceTypeButtonGroup = new ButtonGroup();
        serviceTypeButtonGroup.add(this.originalButton);
        serviceTypeButtonGroup.add(this.periodicButton);
        serviceTypeButtonGroup.add(this.batchButton);

        GridPanel radioButtonPanel = new GridPanel();
        radioButtonPanel.add(this.originalButton);
        radioButtonPanel.add(this.periodicButton);
        radioButtonPanel.add(this.batchButton);
        radioButtonPanel.layout(1, 3, 0, GridPanel.WEIGHT_NONE);

        GridPanel mainPaenl = new GridPanel();
        mainPaenl.add(infoPanel);
        mainPaenl.add(radioButtonPanel);
        mainPaenl.layout(2, 1, GridPanel.WEIGHT_NONE, 0);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setConfiguration();
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

        this.dialog = new XBayaDialog(this.engine, "Load History", mainPaenl, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }
}