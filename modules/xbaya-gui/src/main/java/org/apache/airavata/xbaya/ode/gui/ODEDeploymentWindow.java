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

package org.apache.airavata.xbaya.ode.gui;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gpel.script.BPELScript;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.gui.Cancelable;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.myproxy.gui.MyProxyChecker;
import org.apache.airavata.xbaya.security.UserX509Credential;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.wf.gui.WorkflowPropertyWindow;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.apache.airavata.xbaya.workflow.proxy.WorkflowProxyClient;
import org.ietf.jgss.GSSCredential;

public class ODEDeploymentWindow implements Cancelable {

    private XBayaEngine engine;

    // private WorkflowClient workflowClient;

    private XBayaDialog dialog;

    private JButton deploy;

    private JButton redeploy;

    private XBayaTextField odeUriField;

    private MyProxyChecker myProxyChecker;

    private WaitDialog invokingDialog;

    private boolean canceled;

    private Thread invokeThread;

    private JCheckBox makePublicChkBox;

    private XBayaTextField xRegistryTextField;

    private XBayaTextField proxyTextField;

    private boolean configMode;

    public ODEDeploymentWindow(XBayaEngine engine, boolean configMode) {
        this.engine = engine;
        this.configMode = configMode;
        initGUI();

    }

    /**
     * Constructs a MyLeadSaveWorkflowWindow.
     * 
     * @param engine
     */
    public ODEDeploymentWindow(XBayaEngine engine) {
        this.engine = engine;
        this.myProxyChecker = new MyProxyChecker(this.engine);
        initGUI();
    }

    /**
     * Constructs a ODEDeploymentWindow.
     * 
     */
    public ODEDeploymentWindow() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Shows the window.
     */
    public void show() {

        BPELScript script = new BPELScript(this.engine.getWorkflow());

        // Check if the workflow is valid before the user types in input
        // values.
        ArrayList<String> warnings = new ArrayList<String>();
        if (!script.validate(warnings)) {
            StringBuilder buf = new StringBuilder();
            for (String warning : warnings) {
                buf.append("- ");
                buf.append(warning);
                buf.append("\n");
            }
            this.engine.getErrorWindow().warning(buf.toString());
            return;
        }

        String uri = this.engine.getConfiguration().getODEURL();
        String uriString;
        if (uri == null) {
            uriString = "";
        } else {
            uriString = uri;
        }
        this.odeUriField.setText(uriString);

        XBayaConfiguration configuration = this.engine.getConfiguration();
        URI xregistryURL = configuration.getXRegistryURL();
        if (xregistryURL == null) {
            xregistryURL = XBayaConstants.DEFAULT_XREGISTRY_URL;
        }
        this.xRegistryTextField.setText(xregistryURL);

        URI proxyURI = configuration.getProxyURI();
        if (null != proxyURI) {
            this.proxyTextField.setText(proxyURI);
        } else {
            this.proxyTextField.setText(XBayaConstants.DEFAULT_PROXY_URI);
        }
        if (this.configMode) {
            this.proxyTextField.setEnabled(false);
            this.xRegistryTextField.setEnabled(false);
            this.makePublicChkBox.setVisible(false);
            this.deploy.setText("OK");
        }

        this.dialog.show();
        ok();
    }

    /**
     * Hides the window.
     */
    public void hide() {
        this.dialog.hide();
    }

    private void ok() {

    }

    /**
     * @throws WorkflowEngineException
     * @throws GraphException
     * 
     */
    public void deploy() throws WorkflowEngineException, GraphException {

        String xregistryUrl = this.xRegistryTextField.getText();
        if (null != xregistryUrl && !"".equals(xregistryUrl)) {
            try {
                this.engine.getConfiguration().setXRegistryURL(new URI(xregistryUrl));
            } catch (URISyntaxException e) {
                this.engine.getErrorWindow().error(e);
            }
        }

        String odeUrl = this.odeUriField.getText();
        if (null != odeUrl && !"".equals(odeUrl)) {
            this.engine.getConfiguration().setOdeURL(odeUrl);
        }

        String odeProxy = this.proxyTextField.getText();

        if (null != odeProxy && !"".equals(odeProxy)) {
            try {
                this.engine.getConfiguration().setProxyURI(new URI(odeProxy));
            } catch (URISyntaxException e) {
                this.engine.getErrorWindow().error(e);
            }
        }

        final Workflow workflow = this.engine.getWorkflow();

        // Check whether the workflow name is Normalised

        WorkflowPropertyWindow workflowPropertyWindow = this.engine.getWorkflowPropertyWindow();

        if (!workflow.getName().equals(StringUtil.convertToJavaIdentifier(workflow.getName()))) {
            JOptionPane.showMessageDialog(this.engine.getGUI().getFrame(),
                    "Invalid Name. Please consider a valid name.", "Invalid Name", JOptionPane.OK_OPTION);

            workflowPropertyWindow.show();
        }

        // its just configuring values for saving the files
        // so just return
        if (this.configMode) {
            return;
        }

        final WorkflowProxyClient client = new WorkflowProxyClient();
        client.setXRegistryUrl(this.engine.getConfiguration().getXRegistryURL());
        client.setEngineURL(this.engine.getConfiguration().getProxyURI());
        client.setXBayaEngine(this.engine);
        GSSCredential proxy = null;
        if (client.isSecure()) {
            // Check if the proxy is loaded.
            boolean loaded = this.myProxyChecker.loadIfNecessary();
            if (!loaded) {
                return;
            }
            // Creates a secure channel in gpel.
            MyProxyClient myProxyClient = this.engine.getMyProxyClient();
            proxy = myProxyClient.getProxy();
            UserX509Credential credential = new UserX509Credential(proxy, XBayaSecurity.getTrustedCertificates());
            try {
                client.setUserX509Credential(credential);
            } catch (WorkflowEngineException e) {
                this.engine.getErrorWindow().error(ErrorMessages.GPEL_ERROR, e);
                return;
            }
        }

        final boolean makePublic = this.makePublicChkBox.isSelected();
        final GSSCredential gssCredential = proxy;
        this.invokeThread = new Thread() {
            @Override
            public void run() {
                new ODEDeploymentClient(ODEDeploymentWindow.this.engine, ODEDeploymentWindow.this.invokingDialog)
                        .deploy(client, workflow, gssCredential, makePublic);
            }

        };
        this.invokeThread.start();
        this.invokingDialog.show();

    }

    /**
     * @see org.apache.airavata.xbaya.gui.Cancelable#cancel()
     */
    public void cancel() {
        this.canceled = true;
        this.invokeThread.interrupt();
    }

    /**
     * Initializes the GUI
     */
    private void initGUI() {
        this.odeUriField = new XBayaTextField();
        XBayaLabel uriLabel = new XBayaLabel("ODE URL", this.odeUriField);

        this.xRegistryTextField = new XBayaTextField();
        XBayaLabel xRegistryLabel = new XBayaLabel("XRegistry URL", this.xRegistryTextField);

        this.proxyTextField = new XBayaTextField();
        XBayaLabel proxyLable = new XBayaLabel("ODE proxy URL", this.proxyTextField);

        this.makePublicChkBox = new JCheckBox();

        GridPanel mainPanel = new GridPanel();
        mainPanel.add(uriLabel);
        mainPanel.add(this.odeUriField);

        mainPanel.add(xRegistryLabel);
        mainPanel.add(this.xRegistryTextField);

        mainPanel.add(proxyLable);
        mainPanel.add(this.proxyTextField);

        mainPanel.add(new JLabel("Make workflow public in X-registry"));
        mainPanel.add(this.makePublicChkBox);
        mainPanel.layout(4, 2, GridPanel.WEIGHT_NONE, 1);

        this.deploy = new JButton("Deploy");
        this.deploy.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    hide();
                    deploy();
                } catch (Exception e1) {
                    ODEDeploymentWindow.this.engine.getErrorWindow().error(e1);
                }
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(this.deploy);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, "Deploy workflow to ODE and XRegistry", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(this.deploy);

        this.invokingDialog = new WaitDialog(this, "Deploying the Workflow.", "Deploying the Workflow."
                + "Please wait for a moment.", this.engine);
    }

}