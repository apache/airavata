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

package org.apache.airavata.xbaya.gpel.gui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gpel.script.BPELScript;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextArea;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.myproxy.gui.MyProxyChecker;
import org.apache.airavata.xbaya.security.UserX509Credential;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GPELDeployWindow {

    private static final Logger logger = LoggerFactory.getLogger(GPELDeployWindow.class);

    protected XBayaEngine engine;

    private MyProxyChecker myProxyChecker;

    protected XBayaDialog dialog;

    private JButton newButton;

    private JButton redeployButton;

    protected XBayaTextField nameTextField;

    protected XBayaTextArea descriptionTextArea;

    private GPELDeployer deployer;

    private Workflow workflow;

    /**
     * Constructs a GPELDeployWindow.
     * 
     * @param engine
     */
    public GPELDeployWindow(XBayaEngine engine) {
        this.engine = engine;
        this.deployer = new GPELDeployer(engine);
        this.myProxyChecker = new MyProxyChecker(this.engine);
        initGUI();
    }

    /**
     * Shows the window.
     */
    public void show() {
        WorkflowClient workflowClient = this.engine.getWorkflowClient();
        if (workflowClient.isSecure()) {
            // Check if the proxy is loaded.
            boolean loaded = this.myProxyChecker.loadIfNecessary();
            if (!loaded) {
                return;
            }
            // Creates a secure channel in gpel.
            MyProxyClient myProxyClient = this.engine.getMyProxyClient();
            GSSCredential proxy = myProxyClient.getProxy();
            UserX509Credential credential = new UserX509Credential(proxy, XBayaSecurity.getTrustedCertificates());
            try {
                workflowClient.setUserX509Credential(credential);
            } catch (WorkflowEngineException e) {
                this.engine.getErrorWindow().error(ErrorMessages.GPEL_ERROR, e);
                return;
            }
        }

        this.workflow = this.engine.getWorkflow();
        BPELScript bpel = new BPELScript(this.workflow);

        // Check if there is any errors in the workflow first.
        ArrayList<String> warnings = new ArrayList<String>();
        if (!bpel.validate(warnings)) {
            StringBuilder buf = new StringBuilder();
            for (String warning : warnings) {
                buf.append("- ");
                buf.append(warning);
                buf.append("\n");
            }
            this.engine.getErrorWindow().warning(buf.toString());
            return;
        }

        String name = this.workflow.getName();
        String description = this.workflow.getDescription();

        this.nameTextField.setText(name);
        this.descriptionTextArea.setText(description);

        if (this.workflow.getGPELTemplateID() == null) {
            this.redeployButton.setEnabled(false);
        } else {
            this.redeployButton.setEnabled(true);
        }

        logger.info("before show");
        this.dialog.show();
    }

    /**
     * Hides the window.
     */
    public void hide() {
        this.dialog.hide();
    }

    protected void deploy(boolean redeploy) {
        // Set the name and description to the graph.
        String name = this.nameTextField.getText();
        String description = this.descriptionTextArea.getText();

        if (name.trim().length() == 0) {
            this.engine.getErrorWindow().warning("You need to set the name of the workflow.");
        } else {
            // Use this method in order to change the name of the tab.
            this.engine.getGUI().getGraphCanvas().setNameAndDescription(name, description);
            hide();
            this.deployer.deploy(redeploy);
        }
    }

    /**
     * Initializes the GUI
     */
    private void initGUI() {
        this.nameTextField = new XBayaTextField();
        XBayaLabel nameLabel = new XBayaLabel("Name", this.nameTextField);

        this.descriptionTextArea = new XBayaTextArea();
        XBayaLabel descriptionLabel = new XBayaLabel("Description", this.descriptionTextArea);

        GridPanel mainPanel = new GridPanel();
        mainPanel.add(nameLabel);
        mainPanel.add(this.nameTextField);
        mainPanel.add(descriptionLabel);
        mainPanel.add(this.descriptionTextArea);
        mainPanel.layout(2, 2, 1, 1);

        this.newButton = new JButton("Deploy New");
        this.newButton.setDefaultCapable(true);
        this.newButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                deploy(false);
            }
        });

        this.redeployButton = new JButton("Redeploy");
        this.redeployButton.setDefaultCapable(true);
        this.redeployButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                deploy(true);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(this.newButton);
        buttonPanel.add(this.redeployButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, "Deploy the Workflow to the BPEL Engine", mainPanel, buttonPanel);
    }
}