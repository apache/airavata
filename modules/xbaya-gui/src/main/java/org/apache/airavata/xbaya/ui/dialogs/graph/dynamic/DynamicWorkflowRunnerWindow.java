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

package org.apache.airavata.xbaya.ui.dialogs.graph.dynamic;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.namespace.QName;

import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.util.GraphUtil;
import org.apache.airavata.workflow.model.graph.ws.WSNode;
import org.apache.airavata.workflow.model.ode.ODEClient;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.ws.monitor.MonitorConfiguration;
import org.apache.airavata.ws.monitor.MonitorException;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.interpretor.GUIWorkflowInterpreterInteractorImpl;
import org.apache.airavata.xbaya.interpretor.WorkflowInterpreter;
import org.apache.airavata.xbaya.interpretor.WorkflowInterpreterConfiguration;
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
import xsul.lead.LeadResourceMapping;

public class DynamicWorkflowRunnerWindow {

    private static final Logger logger = LoggerFactory.getLogger(DynamicWorkflowRunnerWindow.class);

    private XBayaEngine engine;

    private Workflow workflow;

    private XBayaDialog dialog;

    private GridPanel parameterPanel;

    private XBayaTextField topicTextField;

    private List<XBayaTextField> parameterTextFields = new ArrayList<XBayaTextField>();

    private XBayaLabel resourceSelectionLabel;

    // private XBayaComboBox resourceSelectionComboBox;

//    private JComboBox gfacUrlListField;

    private JCheckBox interactChkBox;

	private JCheckBox chkRunWithCrossProduct;

	private XBayaTextField instanceNameTextField;

    protected final static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    /**
     * Constructs a TavernaRunnerWindow.
     * 
     * @param engine
     * 
     */
    public DynamicWorkflowRunnerWindow(XBayaEngine engine) {
        this.engine = engine;
        if (XBayaUtil.acquireJCRRegistry(this.engine)) {
            initGUI();
        }
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        this.workflow = this.engine.getGUI().getWorkflow();
//        List<URI> urlList=null;
//        try {
//            urlList = this.engine.getConfiguration().getAiravataAPI().getAiravataManager().getGFaCURLs();
//        } catch (AiravataAPIInvocationException e) {
//            e.printStackTrace();
//        }
        // When run xbaya continously urls can be repeating, so first remove everything and then add
//        this.gfacUrlListField.removeAllItems();
//        for (URI gfacUrl : urlList) {
//            if (XBayaUtil.isURLExists(gfacUrl + "?wsdl")) {
//                this.gfacUrlListField.addItem(gfacUrl);
//            }
//        }
//        this.gfacUrlListField.setEditable(true);
        MonitorConfiguration notifConfig = this.engine.getMonitor().getConfiguration();
        if (notifConfig.getBrokerURL() == null) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.BROKER_URL_NOT_SET_ERROR);
            return;
        }

        // Create input fields
        List<InputNode> inputNodes = GraphUtil.getInputNodes(this.workflow.getGraph());
        for (Iterator<InputNode> iterator = inputNodes.iterator(); iterator.hasNext();) {
            InputNode node = iterator.next();
            String id = node.getID();
            QName parameterType = node.getParameterType();

            /*
             * If input node has no connection, skip it
             */
            if (parameterType == null) {
                iterator.remove();
                continue;
            }

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

            if (!node.isVisibility()) {
                paramField.setEditable(false);
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

        this.dialog.show();
    }

    /**
     * Hides the dialog.
     */
    public void hide() {
        this.dialog.hide();

        this.parameterPanel.resetPanel();
        this.parameterTextFields.clear();
    }

    /**
     * ReInit Host Name ComboBox
     */
    // public void reinitHostComboBox() {
    // if (this.resourceSelectionComboBox == null)
    // this.resourceSelectionComboBox = new XBayaComboBox(new DefaultComboBoxModel(initHostNameList()));
    // else
    // this.resourceSelectionComboBox.setModel(new DefaultComboBoxModel(initHostNameList()));
    // }

    private void initGUI() {
        this.parameterPanel = new GridPanel(true);

        // reinitHostComboBox();
        // this.resourceSelectionLabel = new XBayaLabel("Select a Compute Resource", this.resourceSelectionComboBox);
        this.instanceNameTextField = new XBayaTextField();
        XBayaLabel instanceNameLabel = new XBayaLabel("Experiment name", this.instanceNameTextField);

        this.topicTextField = new XBayaTextField();
        XBayaLabel topicLabel = new XBayaLabel("Notification topic", this.topicTextField);
//        this.gfacUrlListField = new JComboBox();
//        XBayaLabel gfacURLLabel = new XBayaLabel("GFac URL", this.gfacUrlListField);
        this.interactChkBox = new JCheckBox();
        this.interactChkBox.setSelected(false);
        XBayaLabel interactLabel = new XBayaLabel("Enable Service Interactions", this.interactChkBox);

    	chkRunWithCrossProduct=new JCheckBox();
    	XBayaLabel crossProductLabel = new XBayaLabel("Execute in cross product", chkRunWithCrossProduct);

        GridPanel infoPanel = new GridPanel();
        // infoPanel.add(this.resourceSelectionLabel);
        // infoPanel.add(this.resourceSelectionComboBox);
        infoPanel.add(instanceNameLabel);
        infoPanel.add(this.instanceNameTextField);
        infoPanel.add(topicLabel);
        infoPanel.add(this.topicTextField);
//        infoPanel.add(gfacURLLabel);
//        infoPanel.add(this.gfacUrlListField);
        infoPanel.add(interactLabel);
        infoPanel.add(this.interactChkBox);
        infoPanel.add(crossProductLabel);
        infoPanel.add(chkRunWithCrossProduct);
        
        infoPanel.layout(5, 2, GridPanel.WEIGHT_NONE, 1);

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

        this.dialog = new XBayaDialog(this.engine.getGUI(), "Invoke workflow", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }

    private void execute() {
        final List<String> arguments = new ArrayList<String>();
        String instanceName = this.instanceNameTextField.getText();
        if (instanceName.trim().equals("")){
        	JOptionPane.showMessageDialog(engine.getGUI().getFrame(),
        		    "Experiment name cannot be empty",
        		    "Experiment Name",
        		    JOptionPane.ERROR_MESSAGE);
        	return;
        }
//        if (instanceName.equals("")){
//        	instanceName=workflow.getName();
//        }
        final String instanceNameFinal=instanceName;
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
        Collection<WSNode> wsNodes = GraphUtil.getWSNodes(this.engine.getGUI().getWorkflow().getGraph());
        // This is to enable service interaction with the back end
        if (this.interactChkBox.isSelected()) {
            LinkedList<String> nodeIDs = new LinkedList<String>();
            for (WSNode node : wsNodes) {
                nodeIDs.add(node.getID());
                ((WSNodeGUI) NodeController.getGUI(node)).setInteractiveMode(true);
            }
            notifConfig.setInteractiveNodeIDs(nodeIDs);
        } else {
            for (WSNode node : wsNodes) {
                ((WSNodeGUI) NodeController.getGUI(node)).setInteractiveMode(false);
            }
        }

        final boolean isRunCrossProduct=chkRunWithCrossProduct.isSelected();
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

//        final String gFacUrl = ((URI) this.gfacUrlListField.getSelectedItem()).toASCIIString();
//        if (null != gFacUrl && !"".equals(gFacUrl)) {
//            try {
//                this.engine.getConfiguration().setGFacURL(new URI(gFacUrl));
//            } catch (URISyntaxException e) {
//                this.engine.getGUI().getErrorWindow().error(e);
//            }
//        }
        this.engine.getConfiguration().setTopic(topic);

        /*
         * Load host description from xregistry and add to interpreter
         */
        LeadResourceMapping mapping = null;
        
        final LeadResourceMapping resourceMapping = mapping;
        final String topicString = topic;
        new Thread() {
            /**
             * @see java.lang.Thread#run()
             */
            @Override
            public void run() {
                XBayaConfiguration conf = DynamicWorkflowRunnerWindow.this.engine.getConfiguration();
                WorkflowInterpreterConfiguration workflowInterpreterConfiguration = new WorkflowInterpreterConfiguration(engine.getGUI().getWorkflow(),topicString,conf.getMessageBoxURL(), conf.getBrokerURL(), conf.getAiravataAPI(), conf, DynamicWorkflowRunnerWindow.this.engine.getGUI(), DynamicWorkflowRunnerWindow.this.engine.getMonitor());
                workflowInterpreterConfiguration.setRunWithCrossProduct(isRunCrossProduct);

                WorkflowInterpreter workflowInterpreter = new WorkflowInterpreter(
                		workflowInterpreterConfiguration, new GUIWorkflowInterpreterInteractorImpl(engine, engine.getGUI().getWorkflow()));
                DynamicWorkflowRunnerWindow.this.engine.registerWorkflowInterpreter(workflowInterpreter);
                try {
                    MonitorConfiguration notifConfig = DynamicWorkflowRunnerWindow.this.engine.getMonitor()
                            .getConfiguration();
                    notifConfig.setTopic(topicString);
                    DynamicWorkflowRunnerWindow.this.engine.getMonitor().start();

                    if (resourceMapping != null)
                        workflowInterpreter.setResourceMapping(resourceMapping);

                    workflowInterpreter.scheduleDynamically();
//                    try {
//						engine.getConfiguration().getJcrComponentRegistry().getRegistry().saveWorkflowExecutionName(topicString, instanceNameFinal);
//					} catch (RegistryException e) {
//						e.printStackTrace();
//					}
                } catch (WorkflowException e) {
                    try {
                        workflowInterpreter.cleanup();
                    } catch (MonitorException e1) {
                        DynamicWorkflowRunnerWindow.this.engine.getGUI().getErrorWindow().error(e1);
                    }
                    DynamicWorkflowRunnerWindow.this.engine.getGUI().getErrorWindow().error(e);
                }

            }
        }.start();

        hide();
    }
}