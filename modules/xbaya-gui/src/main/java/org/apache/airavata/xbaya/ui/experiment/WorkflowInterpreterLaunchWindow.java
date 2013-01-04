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

package org.apache.airavata.xbaya.ui.experiment;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.namespace.QName;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.ExperimentAdvanceOptions;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.util.GraphUtil;
import org.apache.airavata.workflow.model.graph.ws.WSNode;
import org.apache.airavata.workflow.model.ode.ODEClient;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.apache.airavata.ws.monitor.MonitorConfiguration;
import org.apache.airavata.ws.monitor.MonitorException;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.jython.script.JythonScript;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.graph.ws.WSNodeGUI;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.apache.airavata.xbaya.util.XBayaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;

import xsul.XmlConstants;
//import org.apache.airavata.registry.api.AiravataRegistry2;

public class WorkflowInterpreterLaunchWindow {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowInterpreterLaunchWindow.class);

    private XBayaEngine engine;

    private Workflow workflow;

    private XBayaDialog dialog;

    private GridPanel parameterPanel;

    private XBayaTextField topicTextField;

    private List<XBayaTextField> parameterTextFields = new ArrayList<XBayaTextField>();

    private XBayaTextField workflowInterpreterTextField;

    private XBayaTextField RegistryTextField;

    private XBayaTextField gfacTextField;

	private XBayaTextField instanceNameTextField;

    protected final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    /**
     * Constructs a WorkflowInterpreterLaunchWindow.
     * 
     * @param engine
     * 
     */
    public WorkflowInterpreterLaunchWindow(XBayaEngine engine) {
        this.engine = engine;
        if (XBayaUtil.acquireJCRRegistry(engine)) {
            initGUI();
        }
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        this.workflow = this.engine.getGUI().getWorkflow();

        MonitorConfiguration notifConfig = this.engine.getMonitor().getConfiguration();
        if (notifConfig.getBrokerURL() == null) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.BROKER_URL_NOT_SET_ERROR);
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
//        this.instanceNameTextField.setText(workflow.getName()+"_"+Calendar.getInstance().getTime().toString());
        this.topicTextField.setText(UUID.randomUUID().toString());

        XBayaConfiguration config = this.engine.getConfiguration();
        this.gfacTextField.setText(config.getGFacURL().toString());
        URI workflowInterpreterURL = config.getWorkflowInterpreterURL();
        if (null != workflowInterpreterURL) {
            this.workflowInterpreterTextField.setText(workflowInterpreterURL.toString());
        } else {
            this.workflowInterpreterTextField.setText(XBayaConstants.DEFAULT_WORKFLOW_INTERPRETER_URL);
        }

        AiravataAPI airavataAPI = config.getAiravataAPI();
        if (null != airavataAPI) {
            this.RegistryTextField.setText(config.getRegistryURL());
        } else {
            this.RegistryTextField.setText(XBayaConstants.REGISTRY_URL.toASCIIString());
        }

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

        this.instanceNameTextField = new XBayaTextField();
        XBayaLabel instanceNameLabel = new XBayaLabel("Experiment name", this.instanceNameTextField);
        
        this.topicTextField = new XBayaTextField();
        XBayaLabel topicLabel = new XBayaLabel("Notification topic", this.topicTextField);
        this.workflowInterpreterTextField = new XBayaTextField();
        XBayaLabel workflowInterpreterLabel = new XBayaLabel("Workflow Interpreter URL",
                this.workflowInterpreterTextField);
        this.RegistryTextField = new XBayaTextField();
        XBayaLabel RegistryLabel = new XBayaLabel("Registry URL", this.RegistryTextField);
        this.gfacTextField = new XBayaTextField();
        XBayaLabel gfacLabel = new XBayaLabel("GFac URL", this.gfacTextField);

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(instanceNameLabel);
        infoPanel.add(this.instanceNameTextField);
//        infoPanel.add(topicLabel);
//        infoPanel.add(this.topicTextField);
        infoPanel.add(workflowInterpreterLabel);
        infoPanel.add(this.workflowInterpreterTextField);
        infoPanel.add(gfacLabel);
        infoPanel.add(this.gfacTextField);
//        infoPanel.add(RegistryLabel);
//        infoPanel.add(this.RegistryTextField);

        infoPanel.layout(3, 2, GridPanel.WEIGHT_NONE, 1);

        GridPanel mainPanel = new GridPanel();
        mainPanel.add(this.parameterPanel);
        mainPanel.add(infoPanel);
        mainPanel.layout(2, 1, 0, 0);

        JButton okButton = new JButton("Run");
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

        this.dialog = new XBayaDialog(this.engine.getGUI(), "Launch  workflow", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }

    private void execute() {
        final List<String> arguments = new ArrayList<String>();

        String topic = this.topicTextField.getText();
        String instanceName = this.instanceNameTextField.getText();
        if (instanceName.trim().equals("")){
        	JOptionPane.showMessageDialog(engine.getGUI().getFrame(),
        		    "Experiment name cannot be empty",
        		    "Experiment Name",
        		    JOptionPane.ERROR_MESSAGE);
        	return;
        }

        //previous instance name
        if (!instanceNameTextField.getText().equals("")){
            this.instanceNameTextField.setText("");
        }
        final String instanceNameFinal=instanceName;
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
        Collection<WSNode> wsNodes = GraphUtil.getWSNodes(this.engine.getGUI().getWorkflow().getGraph());
        for (WSNode node : wsNodes) {
            ((WSNodeGUI) NodeController.getGUI(node)).setInteractiveMode(false);
        }

        // TODO error check for user inputs

        final List<InputNode> inputNodes = GraphUtil.getInputNodes(this.workflow.getGraph());
        builder.newFragment("inputs");
        new ODEClient();
        for (int i = 0; i < inputNodes.size(); i++) {
            InputNode inputNode = inputNodes.get(i);
            XBayaTextField parameterTextField = this.parameterTextFields.get(i);
            inputNode.getID();
            String value = parameterTextField.getText();
            inputNode.setDefaultValue(value);
        }

        final String workflowInterpreterUrl = this.workflowInterpreterTextField.getText();
        if (null != workflowInterpreterUrl && !"".equals(workflowInterpreterUrl)) {
            try {
                this.engine.getConfiguration().setWorkflowInterpreterURL(new URI(workflowInterpreterUrl));
            } catch (URISyntaxException e) {
                this.engine.getGUI().getErrorWindow().error(e);
            }
        }

        final String gFacUrl = this.gfacTextField.getText();
        if (null != gFacUrl && !"".equals(gFacUrl)) {
            try {
                this.engine.getConfiguration().setGFacURL(new URI(gFacUrl));
            } catch (URISyntaxException e) {
                this.engine.getGUI().getErrorWindow().error(e);
            }
        }
        this.engine.getConfiguration().setTopic(topic);

        new Thread() {
            @Override
            public void run() {

                try {
                    List<WorkflowInput> workflowInputs=new ArrayList<WorkflowInput>();
                    for (int i = 0; i < inputNodes.size(); i++) {
                    	InputNode inputNode = inputNodes.get(i);
                    	workflowInputs.add(new WorkflowInput(inputNode.getID(), inputNode.getDefaultValue().toString()));
                    }
                    AiravataAPI api = engine.getConfiguration().getAiravataAPI();
                    
                    ExperimentAdvanceOptions options = api.getExecutionManager().createExperimentAdvanceOptions(instanceNameFinal, api.getCurrentUser(), null);
                    String experimentId = api.getExecutionManager().runExperiment(api.getWorkflowManager().getWorkflowAsString(workflow), workflowInputs,options);
                    try {
                        WorkflowInterpreterLaunchWindow.this.engine.getMonitor().getConfiguration().setTopic(experimentId);
                        WorkflowInterpreterLaunchWindow.this.engine.getMonitor().start();
                    } catch (MonitorException e1) {
                        WorkflowInterpreterLaunchWindow.this.engine.getGUI().getErrorWindow().error(e1);
                    }
                } catch (Exception e) {
                    WorkflowInterpreterLaunchWindow.this.engine.getGUI().getErrorWindow().error(e);
                }
            }
        }.start();

        hide();
    }
}