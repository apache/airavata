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

import org.apache.airavata.api.Airavata.Client;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.error.AiravataClientConnectException;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.experiment.ComputationalResourceScheduling;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationData;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.ThriftClientData;
import org.apache.airavata.xbaya.ThriftServiceType;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.apache.airavata.xbaya.util.XBayaUtil;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.xml.namespace.QName;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LaunchApplicationWindow {

    private static final Logger logger = LoggerFactory.getLogger(LaunchApplicationWindow.class);

    private XBayaEngine engine;

    private Workflow workflow;

    private XBayaDialog dialog;

    private GridPanel parameterPanel;
    
    private ThriftClientData thriftClientData;
    
    private Client airavataClient;

    private List<XBayaTextField> parameterTextFields = new ArrayList<XBayaTextField>();


	private XBayaTextField instanceNameTextField;
	
	private JComboBox host;
	
	private Map<String,String> hostNames;


    /**
     * Constructs a LaunchApplicationWindow.
     * 
     * @param engine
     * 
     */
    public LaunchApplicationWindow(XBayaEngine engine) {
        this.engine = engine;
        thriftClientData = engine.getConfiguration().getThriftClientData(ThriftServiceType.API_SERVICE);
		try {
			airavataClient = XBayaUtil.getAiravataClient(thriftClientData);
		} catch (AiravataClientConnectException e) {
            logger.error(e.getMessage(), e);
		}
		initGUI();
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        this.workflow = this.engine.getGUI().getWorkflow();

        // Create input fields
//        Collection<InputNode> inputNodes = GraphUtil.getInputNodes(this.workflow.getGraph());
//        for (InputNode node : inputNodes) {
//            String id = node.getID();
//            QName parameterType = node.getParameterType();
//            JLabel nameLabel = new JLabel(id);
//            JLabel typeField = new JLabel(parameterType.getLocalPart());
//            XBayaTextField paramField = new XBayaTextField();
//            Object value = node.getDefaultValue();
//
//            String valueString;
//            if (value == null) {
//                valueString = "";
//            } else {
//                if (value instanceof XmlElement) {
//                    XmlElement valueElement = (XmlElement) value;
//                    valueString = XMLUtil.xmlElementToString(valueElement);
//                } else {
//                    // Only string comes here for now.
//                    valueString = value.toString();
//                }
//            }
//            paramField.setText(valueString);
        List<NodeImpl> nodes = workflow.getGraph().getNodes();
        NodeImpl node = null;
    	for(int i=0; i<nodes.size(); i++){
    		node = nodes.get(i);
    		String html = node.getComponent().toHTML();     		
    		String nodeType =html.substring(html.indexOf("<h1>")+4, html.indexOf(":")).trim();    		
    		if(nodeType.equals("Application")){    			
    			break;    			
    		}
    	}
    	List<DataPort> inputPorts = node.getInputPorts();
    	for(DataPort port : inputPorts){
    		String id = port.getName();
    		DataType parameterType = port.getType();
    		JLabel nameLabel = new JLabel(id);
            JLabel typeField = new JLabel(parameterType.toString());
            XBayaTextField paramField = new XBayaTextField();            
            paramField.setText("");
            this.parameterPanel.add(nameLabel);
            this.parameterPanel.add(typeField);
            this.parameterPanel.add(paramField);
            this.parameterTextFields.add(paramField);
    	}
        
		Map<String, String> hosts = null;
		        
        try {
			hosts = airavataClient.getAllComputeResourceNames();
		} catch (InvalidRequestException e) {
            logger.error(e.getMessage(), e);
		} catch (AiravataClientException e) {
            logger.error(e.getMessage(), e);
		} catch (AiravataSystemException e) {
            logger.error(e.getMessage(), e);
		} catch (TException e) {
            logger.error(e.getMessage(), e);
		}
    
		        
       hostNames= new HashMap<String,String>();
       
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
       it=hostNames.entrySet().iterator();
       while(it.hasNext()){
    	   Map.Entry pairs=(Map.Entry)it.next();
    	   String key = (String) pairs.getKey();    	   
    	   host.addItem(key);
       }
       host.setSelectedIndex(1);      
     
       XBayaLabel hostLabel = new XBayaLabel("Host", this.host);
       this.parameterPanel.add(hostLabel);
       this.parameterPanel.add(host);       
      //  this.parameterPanel.layout(inputNodes.size()+1, 2, GridPanel.WEIGHT_NONE, 2);
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
        GridPanel infoPanel = new GridPanel();
        infoPanel.add(instanceNameLabel);
        infoPanel.add(this.instanceNameTextField);       
        infoPanel.layout(1, 2, GridPanel.WEIGHT_NONE, 1);

        GridPanel mainPanel = new GridPanel();
        mainPanel.getContentPanel().setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        mainPanel.add(infoPanel);
        mainPanel.add(this.parameterPanel);
        mainPanel.layout(2, 1, 0, 0);

        JButton okButton = new JButton("Run");
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {                	
					execute();
				} catch (AiravataClientConnectException e) {
                    logger.error(e.getMessage(), e);
				} catch (InvalidRequestException e) {
                    logger.error(e.getMessage(), e);
				} catch (AiravataClientException e) {
                    logger.error(e.getMessage(), e);
				} catch (AiravataSystemException e) {
                    logger.error(e.getMessage(), e);
				} catch (TException e) {
                    logger.error(e.getMessage(), e);
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

    private void execute() throws AiravataClientConnectException, InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	List<NodeImpl> nodes = workflow.getGraph().getNodes();
        String gatewayId = engine.getConfiguration().getThriftClientData(ThriftServiceType.API_SERVICE).getGatewayId();
    	String appId = null;
    	NodeImpl node = null;
    	for(int i=0; i<nodes.size(); i++){
    		node = nodes.get(i);
    		String html = node.getComponent().toHTML();     		
    		String nodeType =html.substring(html.indexOf("<h1>")+4, html.indexOf(":")).trim();    		
    		if(nodeType.equals("Application")){
    			appId=html.substring(html.indexOf("</h2>")+6, html.indexOf("<br")).trim();
    			break;
    		}
    	}
    	
    	String hostId = null;
    	String hostName = (String) host.getSelectedItem();
    	hostId = hostNames.get(hostName);

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
        Project project = new Project();
        project.setName("project1");
        String owner = this.thriftClientData.getUsername();        
        if(owner.equals(""))owner="NotKnown";              
        project.setOwner(owner);
        project.setProjectID(airavataClient.createProject(gatewayId, project));
//        final List<InputNode> inputNodes = GraphUtil.getInputNodes(this.workflow.getGraph());
        final List<DataPort> inputPorts = node.getInputPorts();
        final Experiment experiment = new Experiment();
        experiment.setApplicationId(appId);
        ComputationalResourceScheduling scheduling = new ComputationalResourceScheduling();
        scheduling.setResourceHostId(hostId);
        if(hostName.trim().equals("trestles.sdsc.xsede.org")){
        	scheduling.setComputationalProjectAccount("sds128");
        }
        else if(hostName.trim().equals("stampede.tacc.xsede.org")){
        	scheduling.setComputationalProjectAccount("TG-STA110014S");
        }
        
        scheduling.setNodeCount(1);
        scheduling.setTotalCPUCount(1);
        scheduling.setWallTimeLimit(15);
        scheduling.setQueueName("normal");
        UserConfigurationData userConfigurationData = new UserConfigurationData();
        userConfigurationData.setAiravataAutoSchedule(false);
        userConfigurationData.setOverrideManualScheduledParams(false);
        userConfigurationData.setComputationalResourceScheduling(scheduling);
        experiment.setUserConfigurationData(userConfigurationData);
        experiment.setName(instanceName);
        experiment.setProjectID(project.getProjectID());
        experiment.setUserName(thriftClientData.getUsername());

//        for (int i = 0; i < inputNodes.size(); i++) {
//            InputNode inputNode = inputNodes.get(i);
//            XBayaTextField parameterTextField = this.parameterTextFields.get(i);
//            inputNode.getID();
//            String value = parameterTextField.getText();
//            DataObjectType elem = new DataObjectType();
//            elem.setKey(inputNode.getID());
//            elem.setType(DataType.STRING);
//            elem.setValue(value);
//			experiment.addToExperimentInputs(elem );
//        }
//        final List<OutputNode> outputNodes = GraphUtil.getOutputNodes(this.workflow.getGraph());
//
//        for (int i = 0; i < outputNodes.size(); i++) {
//            OutputNode outputNode = outputNodes.get(i);
//            DataObjectType elem = new DataObjectType();
//            elem.setKey(outputNode.getID());
//            elem.setType(DataType.STRING);

        for (int i = 0; i < inputPorts.size(); i++) {
            DataPort inputPort = inputPorts.get(i);
            XBayaTextField parameterTextField = this.parameterTextFields.get(i);           
            String value = parameterTextField.getText();
            InputDataObjectType elem = new InputDataObjectType();
            elem.setName(inputPort.getName());

            elem.setType(elem.getType());
            elem.setValue(value);
			experiment.addToExperimentInputs(elem );
        }
        final List<DataPort> outputPorts = node.getOutputPorts();
        
        for (int i = 0; i < outputPorts.size(); i++) {
            DataPort outputPort = outputPorts.get(i);
            OutputDataObjectType elem = new OutputDataObjectType();
            elem.setName(outputPort.getName());

            elem.setType(elem.getType());
            elem.setValue("");
			experiment.addToExperimentOutputs(elem );
        }

        experiment.setExperimentID(airavataClient.createExperiment(gatewayId, experiment));
        airavataClient.launchExperiment(experiment.getExperimentID(), "testToken");
        hide();
        JOptionPane.showMessageDialog(null, "Experiment Launched. You will be alerted on completion.");
     
        String status = airavataClient.getExperimentStatus(experiment.getExperimentID()).getExperimentState().toString().trim();      
        while(!status.equals("COMPLETED") && !status.equals("FAILED")){        	
        	try {
				Thread.sleep(1000);
				status = airavataClient.getExperimentStatus(experiment.getExperimentID()).getExperimentState().toString().trim();				
			} catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
			}
        }
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
		}
        
        if(status.equals("COMPLETED")){
        	String output="";;
        	String fullOutput="";
        	while(output.equals("")){
        		output = "";
        		fullOutput = "Experiment Completed Successfully. Output(s) are shown below:\n";
            	List<OutputDataObjectType> outputs = airavataClient.getExperimentOutputs(experiment.getExperimentID());
            	for(int i1=0; i1<outputs.size(); i1++){
            		output = outputs.get(i1).getValue();
            		fullOutput+= outputs.get(i1).getName()+": "+output+"\n";
            	}            	
            } 
        	JOptionPane.showMessageDialog(null, fullOutput);
        }
        else{
        	JOptionPane.showMessageDialog(null, "Experiment Failed");
        	return;
        }
        new Thread() {
            @Override
            public void run() {

            }
        }.start();

        hide();
    }
}