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
package org.apache.airavata.xbaya.ui.experiment;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.Airavata.Client;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.common.utils.JSONUtil;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.error.AiravataClientConnectException;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.experiment.ComputationalResourceScheduling;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationData;
import org.apache.airavata.orchestrator.client.OrchestratorClientFactory;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.system.OutputNode;
import org.apache.airavata.workflow.model.graph.util.GraphUtil;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.ThriftClientData;
import org.apache.airavata.xbaya.ThriftServiceType;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.messaging.MonitorException;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.apache.airavata.xbaya.util.XBayaUtil;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//import org.apache.airavata.registry.api.AiravataRegistry2;

public class WorkflowInterpreterLaunchWindow {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowInterpreterLaunchWindow.class);

    private XBayaEngine engine;

    private Workflow workflow;

    private XBayaDialog dialog;

    private GridPanel parameterPanel;

    private List<XBayaTextField> parameterTextFields = new ArrayList<XBayaTextField>();

	private XBayaTextField instanceNameTextField;

    private Airavata.Client airavataClient;

    private JComboBox host;
    private HashMap<String, String> hostNames;
    private XBayaTextField token;

    /**
     * Constructs a WorkflowInterpreterLaunchWindow.
     * 
     * @param engine
     * 
     */
    public WorkflowInterpreterLaunchWindow(XBayaEngine engine) throws AiravataClientConnectException {
        this.engine = engine;
        airavataClient = AiravataClientFactory.createAiravataClient("127.0.0.1", 8930);
            initGUI();
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        this.workflow = this.engine.getGUI().getWorkflow();

        // Create input fields
        Collection<InputNode> inputNodes = GraphUtil.getInputNodes(this.workflow.getGraph());
        for (InputNode node : inputNodes) {
            String id = node.getID();
            DataType parameterType = node.getParameterType();
            JLabel nameLabel = new JLabel(id);
            JLabel typeField = new JLabel(parameterType.toString());
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

        Map<String, String> hosts = null;

        try {
            hosts = airavataClient.getAllComputeResourceNames();
            if (hosts.isEmpty()) {
                JOptionPane.showMessageDialog(engine.getGUI().getFrame(),
                        "No Compute Resources found",
                        "Compute Resources",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (InvalidRequestException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (AiravataClientException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (AiravataSystemException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (TException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        hostNames = new HashMap<String, String>();
        Iterator it=hosts.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pairs=(Map.Entry)it.next();
            String key = (String) pairs.getKey();
            String value = (String) pairs.getValue();
            if(!hostNames.containsKey(value)){
                hostNames.put(value, key);
            }
        }
        host = new JComboBox();
        it= hostNames.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pairs=(Map.Entry)it.next();
            String key = (String) pairs.getKey();
            host.addItem(key);
        }
        host.setSelectedIndex(0);
        XBayaLabel hostLabel = new XBayaLabel("Host", host);
        this.parameterPanel.add(hostLabel);
        this.parameterPanel.add(host);
        this.parameterPanel.layout(inputNodes.size(), 3, GridPanel.WEIGHT_NONE, 2);
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
        GridPanel infoPanel = new GridPanel();

        this.instanceNameTextField = new XBayaTextField();
        XBayaLabel instanceNameLabel = new XBayaLabel("Experiment name", this.instanceNameTextField);
        infoPanel.add(instanceNameLabel);
        infoPanel.add(this.instanceNameTextField);

        token = new XBayaTextField("");
        JLabel tokenLabel = new JLabel("Token Id: ");
        infoPanel.add(tokenLabel);
        infoPanel.add(token);
        infoPanel.layout(2, 2, GridPanel.WEIGHT_NONE, 1);

        GridPanel mainPanel = new GridPanel();
        mainPanel.getContentPanel().setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        mainPanel.add(infoPanel);
        mainPanel.add(this.parameterPanel);
        mainPanel.layout(2, 1, 0, 0);

        JButton okButton = new JButton("Run");
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
					execute();
				} catch (AiravataClientConnectException e1) {
					e1.printStackTrace();
				} catch (InvalidRequestException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (AiravataClientException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (AiravataSystemException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (TException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
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
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        buttonPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        this.dialog = new XBayaDialog(this.engine.getGUI(), "Launch  workflow", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }

    private void execute() throws AiravataClientConnectException, TException {
        String instanceName = this.instanceNameTextField.getText();
        if (instanceName.trim().equals("")){
            JOptionPane.showMessageDialog(engine.getGUI().getFrame(),
                    "Experiment name cannot be empty",
                    "Experiment Name",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        ThriftClientData thriftClientData = engine.getConfiguration().getThriftClientData(ThriftServiceType.API_SERVICE);
        Client airavataClient = XBayaUtil.getAiravataClient(thriftClientData);
        String gatewayId = engine.getConfiguration().getThriftClientData(ThriftServiceType.API_SERVICE).getGatewayId();

        final List<InputNode> inputNodes = GraphUtil.getInputNodes(this.workflow.getGraph());
        List<InputDataObjectType> inputDataTypes = new ArrayList<InputDataObjectType>();
        List<OutputDataObjectType> outputDataTypes = new ArrayList<OutputDataObjectType>();
        InputDataObjectType input = null;
        for (int i = 0; i < inputNodes.size(); i++) {
            InputNode inputNode = inputNodes.get(i);
            XBayaTextField parameterTextField = this.parameterTextFields.get(i);
            inputNode.getID();
            String value = parameterTextField.getText();
            input = new InputDataObjectType();
//            inputNode.setDefaultValue(value);
            input.setName(inputNode.getID());
            input.setType(inputNode.getDataType());
            input.setValue(value);
            input.setApplicationArgument(inputNode.getApplicationArgument());
            input.setInputOrder(inputNode.getInputOrder());
            inputDataTypes.add(input);

        }
        final List<OutputNode> outputNodes = GraphUtil.getOutputNodes(this.workflow.getGraph());
        OutputDataObjectType output = null;
        for (OutputNode outputNode : outputNodes) {
            output = new OutputDataObjectType();
            output.setName(outputNode.getID());
            output.setType(DataType.STRING);
            outputDataTypes.add(output);
        }

        Workflow workflowClone = workflow.clone();
        workflowClone.setName(workflowClone.getName() + UUID.randomUUID().toString());
        org.apache.airavata.model.Workflow workflowModel = new org.apache.airavata.model.Workflow();
        workflowModel.setName(workflowClone.getName());
        workflowModel.setGraph(JSONUtil.jsonElementToString(workflowClone.toJSON()));
        for (InputDataObjectType inputDataType : inputDataTypes) {
            workflowModel.addToWorkflowInputs(inputDataType);
        }
        for (OutputDataObjectType outputDataType : outputDataTypes) {
            workflowModel.addToWorkflowOutputs(outputDataType);
        }
        workflowModel.setTemplateId(airavataClient.registerWorkflow(gatewayId, workflowModel));
        // Use topic as a base of workflow instance ID so that the monitor can
        // find it.
        Project project = new Project();
        project.setName("project1");
        project.setOwner(thriftClientData.getUsername());
        project.setProjectID(airavataClient.createProject(gatewayId, project));
        final Experiment experiment = new Experiment();
        experiment.setApplicationId(workflowModel.getTemplateId());
        experiment.setName(instanceName);
        experiment.setProjectID(project.getProjectID());
        experiment.setUserName(thriftClientData.getUsername());
        for (InputDataObjectType inputDataType : inputDataTypes) {
            experiment.addToExperimentInputs(inputDataType);
        }
        for (OutputDataObjectType outputDataType : outputDataTypes) {
            experiment.addToExperimentOutputs(outputDataType);
        }
        // Add scheduling configurations
        if (host != null && host.getSelectedIndex() >= 0) {
            String selectedHostName = host.getSelectedItem().toString();
            String computeResouceId = hostNames.get(selectedHostName);
            ComputationalResourceScheduling computationalResourceScheduling;
            if (selectedHostName.equals("localhost")) {
                computationalResourceScheduling = ExperimentModelUtil.createComputationResourceScheduling(
                        computeResouceId, 1, 1, 1, "normal", 1, 0, 1, "test");
            }else if (selectedHostName.equals("trestles.sdsc.xsede.org")) {
                computationalResourceScheduling = ExperimentModelUtil.createComputationResourceScheduling(
                        computeResouceId,1,1,1,"normal", 1,0,1, "sds128");
            }else if (selectedHostName.equals("stampede.tacc.xsede.org")) {
                computationalResourceScheduling = ExperimentModelUtil.createComputationResourceScheduling(
                        computeResouceId, 2, 32, 1, "development", 90, 0, 1, "TG-STA110014S");
            } else if (selectedHostName.equals("bigred2.uits.iu.edu")) {
                computationalResourceScheduling = ExperimentModelUtil.createComputationResourceScheduling(
                        computeResouceId, 1, 1, 1, "normal", 1, 0, 1, null);
            } else {
                // TODO handle for other computer resources too.
                throw new IllegalArgumentException("Computational resource scheduling is not configured for host :" +
                        computeResouceId);
            }
            UserConfigurationData userConfigurationData = new UserConfigurationData();
            userConfigurationData.setAiravataAutoSchedule(false);
            userConfigurationData.setOverrideManualScheduledParams(false);
            userConfigurationData.setComputationalResourceScheduling(computationalResourceScheduling);
            experiment.setUserConfigurationData(userConfigurationData);
        }else {
            throw new RuntimeException("Resource scheduling failed, target computer resource host name is not defined");
        }
/*
// code snippet for load test.
        for (int i = 0; i < 20; i++) {
            experiment.setName(instanceName + "_" + i);

            experiment.setExperimentID(airavataClient.createExperiment(experiment));

            try {
                this.engine.getMonitor().subscribe(experiment.getExperimentID());
                this.engine.getMonitor().fireStartMonitoring(workflow.getName());
            } catch (MonitorException e) {
                logger.error("Error while subscribing with experiment Id : " + experiment.getExperimentID(), e);
            }
            airavataClient.launchExperiment(experiment.getExperimentID(), "testToken");

        }*/
        experiment.setExperimentID(airavataClient.createExperiment(gatewayId, experiment));
        try {
            this.engine.getMonitor().subscribe(experiment.getExperimentID());
            this.engine.getMonitor().fireStartMonitoring(workflow.getName());
        } catch (MonitorException e) {
            logger.error("Error while subscribing with experiment Id : " + experiment.getExperimentID(), e);
        }
        airavataClient.launchExperiment(experiment.getExperimentID(), token.getText());

        clean();
        hide();
    }

    private void clean() {
        this.instanceNameTextField.setText("");
        this.token.setText("");
    }

    private OrchestratorService.Client getOrchestratorClient() {
		final int serverPort = Integer.parseInt(ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ORCHESTRATOR_SERVER_PORT,"8940"));
        final String serverHost = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ORCHESTRATOR_SERVER_HOST, null);
        try {
			return OrchestratorClientFactory.createOrchestratorClient(serverHost, serverPort);
		} catch (AiravataClientConnectException e) {
            logger.error(e.getMessage(), e);
		}
        return null;
	}
}