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

package org.apache.airavata.xbaya.ui.dialogs;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.component.ws.WSComponent;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.util.GraphUtil;
import org.apache.airavata.workflow.model.graph.ws.WSNode;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.jython.runner.JythonRunner;
import org.apache.airavata.xbaya.jython.script.JythonScript;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.ui.monitor.MonitorStarter;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmlpull.infoset.XmlElement;

import xsul5.wsdl.WsdlDefinitions;

public class GridChemRunnerWindow {

    private static final Log logger = LogFactory.getLog(GridChemRunnerWindow.class);

    private XBayaEngine engine;

    private Workflow workflow;

    private XBayaDialog dialog;

    private GridPanel parameterPanel;

    private XBayaTextField topicTextField;

    private XBayaTextField gfacTextField;

    private List<XBayaTextField> parameterTextFields = new ArrayList<XBayaTextField>();

    private List<File> tmpWSDLFiles = new ArrayList<File>();

    private JythonScript script;

    private JythonRunner runner;

    /*
     * Extended
     */
    private XBayaTextField gridchemBridgeTextField;

    private XBayaTextField usernameTextField;

    /**
     * Constructs a JythonRunnerWindow.
     * 
     * @param engine
     * 
     */
    public GridChemRunnerWindow(XBayaEngine engine) {
        this.engine = engine;
        this.runner = new JythonRunner();
        initGUI();
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        this.workflow = this.engine.getGUI().getWorkflow();
        this.script = new JythonScript(this.workflow, this.engine.getConfiguration());

        MonitorConfiguration notifConfig = this.engine.getMonitor().getConfiguration();
        if (notifConfig.getBrokerURL() == null) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.BROKER_URL_NOT_SET_ERROR);
            return;
        }

        // Check if the workflow is valid before the user types in input
        // values.
        ArrayList<String> warnings = new ArrayList<String>();
        if (!this.script.validate(warnings)) {
            StringBuilder buf = new StringBuilder();
            for (String warning : warnings) {
                buf.append("- ");
                buf.append(warning);
                buf.append("\n");
            }
            this.engine.getGUI().getErrorWindow().warning(buf.toString());
            return;
        }

        // Create a script here. It might throw some exception because the
        // validation is not perfect.
        try {
            this.script.create();
        } catch (GraphException e) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.GRAPH_NOT_READY_ERROR, e);
            hide();
            return;
        } catch (RuntimeException e) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            hide();
            return;
        } catch (Error e) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            hide();
            return;
        }

        // Create input fields
        Collection<InputNode> inputNodes = GraphUtil.getInputNodes(this.workflow.getGraph());
        for (InputNode node : inputNodes) {
            String id = node.getID();
            QName parameterType = node.getParameterType();
            JLabel nameLabel = new JLabel(id);
            JLabel typeField = new JLabel(parameterType.getLocalPart());
            XBayaTextField paramField = new XBayaTextField();
            Object value = node.getDefaultValue();

            String valueString;
            if (value == null) {
                valueString = "";
            } else {
                if (value instanceof XmlElement) {
                    XmlElement valueElement = (XmlElement) value;
                    valueString = XMLUtil.xmlElementToString(valueElement);
                } else {
                    // Only string comes here for now.
                    valueString = value.toString();
                }
            }
            paramField.setText(valueString);
            this.parameterPanel.add(nameLabel);
            this.parameterPanel.add(typeField);
            this.parameterPanel.add(paramField);
            this.parameterTextFields.add(paramField);
        }
        this.parameterPanel.layout(inputNodes.size(), 3, GridPanel.WEIGHT_NONE, 2);

        if (notifConfig.getTopic() == null) {
            this.topicTextField.setText(UUID.randomUUID().toString());
        } else {
            this.topicTextField.setText(notifConfig.getTopic());
        }

        XBayaConfiguration config = this.engine.getConfiguration();

        URI gfacURL = config.getGFacURL();
        this.gfacTextField.setText(StringUtil.toString(gfacURL));

        this.dialog.show();
    }

    /**
     * Hides the dialog.
     */
    public void hide() {
        this.dialog.hide();

        this.parameterPanel.getContentPanel().removeAll();
        this.parameterTextFields.clear();
    }

    private void initGUI() {
        this.parameterPanel = new GridPanel(true);

        this.topicTextField = new XBayaTextField();
        XBayaLabel topicLabel = new XBayaLabel("Notification topic", this.topicTextField);

        this.gfacTextField = new XBayaTextField();
        XBayaLabel gfacLabel = new XBayaLabel("GFac URL", this.gfacTextField);

        /*
         * extend
         */
        this.gridchemBridgeTextField = new XBayaTextField();
        this.usernameTextField = new XBayaTextField();
        XBayaLabel gridchemBridgeLabel = new XBayaLabel("GridChem Bridge URL", this.gridchemBridgeTextField);
        XBayaLabel usernameLabel = new XBayaLabel("Username", this.usernameTextField);

        // default value
        gridchemBridgeTextField.setText("http://129.79.49.227:8080/ogce-gridchem-bridge/");

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(topicLabel);
        infoPanel.add(this.topicTextField);
        infoPanel.add(gfacLabel);
        infoPanel.add(this.gfacTextField);

        /*
         * extend
         */
        infoPanel.add(gridchemBridgeLabel);
        infoPanel.add(this.gridchemBridgeTextField);
        infoPanel.add(usernameLabel);
        infoPanel.add(this.usernameTextField);
        infoPanel.layout(4, 2, GridPanel.WEIGHT_NONE, 1);

        GridPanel mainPanel = new GridPanel();
        mainPanel.add(this.parameterPanel);
        mainPanel.add(infoPanel);
        mainPanel.layout(2, 1, 0, 0);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                execute();
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

        this.dialog = new XBayaDialog(this.engine.getGUI(), "Execute Workflow (Jython)", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }

    public void execute() {
        final List<String> arguments = new ArrayList<String>();

        String topic = this.topicTextField.getText();
        if (topic.length() == 0) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.TOPIC_EMPTY_ERROR);
            return;
        }

        // Use topic as a base of workflow instance ID so that the monitor can
        // find it.
        URI workfowInstanceID = URI.create(StringUtil.convertToJavaIdentifier(topic));
        this.workflow.setGPELInstanceID(workfowInstanceID);

        MonitorConfiguration notifConfig = this.engine.getMonitor().getConfiguration();
        notifConfig.setTopic(topic);
        arguments.add("-" + JythonScript.TOPIC_VARIABLE);
        arguments.add(topic);

        // TODO error check for user inputs

        List<InputNode> inputNodes = GraphUtil.getInputNodes(this.workflow.getGraph());
        for (int i = 0; i < inputNodes.size(); i++) {
            InputNode inputNode = inputNodes.get(i);
            XBayaTextField parameterTextField = this.parameterTextFields.get(i);
            String id = inputNode.getID();
            String value = parameterTextField.getText();
            arguments.add("-" + id);
            arguments.add(value);
        }

        XBayaConfiguration config = this.engine.getConfiguration();

        String gfacString = this.gfacTextField.getText();
        if (gfacString.length() != 0) {
            try {
                URI uri = new URI(gfacString).parseServerAuthority();
                config.setGFacURL(uri);
            } catch (URISyntaxException e) {
                this.engine.getGUI().getErrorWindow().error(ErrorMessages.GFAC_URL_WRONG, e);
                return;
            }
            arguments.add("-" + JythonScript.GFAC_VARIABLE);
            arguments.add(gfacString);
        }

        arguments.add("-" + JythonScript.BROKER_URL_VARIABLE);
        arguments.add(notifConfig.getBrokerURL().toString());

        if (notifConfig.isPullMode()) {
            arguments.add("-" + JythonScript.MESSAGE_BOX_URL_VARIABLE);
            arguments.add(notifConfig.getMessageBoxURL().toString());
        }

        try {
            for (WSNode node : GraphUtil.getWSNodes(this.workflow.getGraph())) {
                WSComponent component = node.getComponent();
                QName portTypeQName = component.getPortTypeQName();
                WsdlDefinitions wsdl = component.getWSDL();

                File file = File.createTempFile(".xbaya-" + portTypeQName.getLocalPart(), XBayaConstants.WSDL_SUFFIX);
                this.tmpWSDLFiles.add(file);
                XMLUtil.saveXML(wsdl.xml(), file);

                String wsdlID = JythonScript.getWSDLID(node);
                String wsdlURL = file.toURL().toString();
                arguments.add("-" + wsdlID);
                arguments.add(wsdlURL);
            }
        } catch (IOException e) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            hide();
            return;
        }

        final String scriptString = this.script.getJythonString(arguments);

        /*
         * set input in final, so we can use it in thread
         */
        final String topicString = topic;
        final String bridgeURL = this.gridchemBridgeTextField.getText();
        final String username = this.usernameTextField.getText();

        new Thread() {

            private void notifyGridChemService() {
                /*
                 * Method and data
                 */
                PostMethod method = new PostMethod(bridgeURL + "ogb/register");
                NameValuePair[] data = {
                        new NameValuePair("broker", GridChemRunnerWindow.this.engine.getMonitor().getConfiguration()
                                .getBrokerURL().toString()), new NameValuePair("jobname", topicString),
                        new NameValuePair("username", username), };
                method.setRequestBody(data);

                try {
                    /*
                     * Contact XGMS to update data to DB
                     */
                    HttpClient httpClient = new HttpClient();
                    int ret_status = httpClient.executeMethod(method);

                    if (ret_status != HttpStatus.SC_OK) {
                        GridChemRunnerWindow.this.engine.getGUI().getErrorWindow().error(
                                "GridChem Bridge Service Error " + bridgeURL + ", return code:" + ret_status);
                    }
                } catch (Exception e) {
                    GridChemRunnerWindow.this.engine.getGUI().getErrorWindow().error(e);
                } finally {
                    if (method != null) {
                        method.releaseConnection();
                    }
                }
            }

            @Override
            public synchronized void run() {

                // tell gridchem service
                notifyGridChemService();

                // Start monitoring.
                // Errors are handled in MonitorStarter.
                MonitorStarter monitorStarter = new MonitorStarter(GridChemRunnerWindow.this.engine);
                monitorStarter.start(true);

                try {
                    GridChemRunnerWindow.this.runner.run(scriptString, arguments);
                    logger.info("Done with the execution");
                    // try {
                    // // Wait for all notification to arrive.
                    // Thread.sleep(10 * 1000); // 10 secs
                    // } catch (InterruptedException e) {
                    // logger.error(e.getMessage(), e);
                    // }
                } catch (WorkflowException e) {
                    GridChemRunnerWindow.this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                } catch (RuntimeException e) {
                    GridChemRunnerWindow.this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                } catch (Error e) {
                    GridChemRunnerWindow.this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                } finally {
                    // We don't need this because this might stop a new
                    // subscription initiated by a user.
                    // JythonRunnerWindow.this.engine.getMonitor()
                    // .asynchronousStop();

                    for (File file : GridChemRunnerWindow.this.tmpWSDLFiles) {
                        file.delete();
                    }
                    GridChemRunnerWindow.this.tmpWSDLFiles.clear();
                }
            }
        }.start();

        hide();
    }
}
