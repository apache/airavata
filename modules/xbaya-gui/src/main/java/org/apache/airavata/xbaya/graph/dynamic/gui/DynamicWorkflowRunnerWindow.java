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

package org.apache.airavata.xbaya.graph.dynamic.gui;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.util.GraphUtil;
import org.apache.airavata.xbaya.graph.ws.WSNode;
import org.apache.airavata.xbaya.graph.ws.gui.WSNodeGUI;
import org.apache.airavata.xbaya.gui.*;
import org.apache.airavata.xbaya.interpretor.WorkflowInterpreter;
import org.apache.airavata.xbaya.jython.script.JythonScript;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.monitor.MonitorException;
import org.apache.airavata.xbaya.ode.ODEClient;
import org.apache.airavata.xbaya.util.XBayaUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import xsul.XmlConstants;
import xsul.lead.LeadResourceMapping;

import javax.swing.*;
import javax.xml.namespace.QName;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

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

    // private XBayaTextField xRegistryTextField;

    private JComboBox gfacUrlListField;

    private JCheckBox interactChkBox;

	private JCheckBox chkRunWithCrossProduct;

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
        this.workflow = this.engine.getWorkflow();
        List<String> urlList=null;
		try {
			urlList = this.engine.getConfiguration().getJcrComponentRegistry().getRegistry().getGFacDescriptorList();
		} catch (RegistryException e) {
			engine.getErrorWindow().error(e);
		}
        // When run xbaya continously urls can be repeating, so first remove everything and then add
        this.gfacUrlListField.removeAllItems();
        for (String gfacUrl : urlList) {
            if (XBayaUtil.isURLExists(gfacUrl + "?wsdl")) {
                this.gfacUrlListField.addItem(gfacUrl);
            }
        }
        this.gfacUrlListField.setEditable(true);
        MonitorConfiguration notifConfig = this.engine.getMonitor().getConfiguration();
        if (notifConfig.getBrokerURL() == null) {
            this.engine.getErrorWindow().error(ErrorMessages.BROKER_URL_NOT_SET_ERROR);
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

        this.topicTextField.setText(UUID.randomUUID().toString());

        // XBayaConfiguration config = this.engine.getConfiguration();
        // this.gfacTextField.setText(config.getGFacURL().toString());
        // URI registryURL = config.getXRegistryURL();
        // if (null != registryURL) {
        // this.xRegistryTextField.setText(registryURL.toString());
        // } else {
        // this.xRegistryTextField.setText(XBayaConstants.DEFAULT_XREGISTRY_URL);
        // }

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

    // FIXME: Change this to use JCR registry
    // private Object[] initHostNameList() {
    // XRegistryAccesser xRegAccesser = new XRegistryAccesser(this.engine);
    // HostDescData[] hostDataList = xRegAccesser.searchHostByName("");
    // List<String> nameList = new ArrayList<String>();
    // nameList.add("");
    // for (HostDescData hostData : hostDataList) {
    // nameList.add(hostData.getName().toString());
    // }
    // return nameList.toArray();
    // }

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

        this.topicTextField = new XBayaTextField();
        XBayaLabel topicLabel = new XBayaLabel("Notification topic", this.topicTextField);
        // this.xRegistryTextField = new XBayaTextField();
        // XBayaLabel xRegistryLabel = new XBayaLabel("XRegistry URL", this.xRegistryTextField);
        this.gfacUrlListField = new JComboBox();
        XBayaLabel gfacURLLabel = new XBayaLabel("GFac URL", this.gfacUrlListField);
        this.interactChkBox = new JCheckBox();
        this.interactChkBox.setSelected(false);
        XBayaLabel interactLabel = new XBayaLabel("Enable Service Interactions", this.interactChkBox);

    	chkRunWithCrossProduct=new JCheckBox();
    	XBayaLabel crossProductLabel = new XBayaLabel("Execute in cross product", chkRunWithCrossProduct);

        GridPanel infoPanel = new GridPanel();
        // infoPanel.add(this.resourceSelectionLabel);
        // infoPanel.add(this.resourceSelectionComboBox);
        infoPanel.add(topicLabel);
        infoPanel.add(this.topicTextField);
        // infoPanel.add(xRegistryLabel);
        // infoPanel.add(this.xRegistryTextField);
        // infoPanel.add(gfacLabel);
        // infoPanel.add(this.gfacTextField);
        infoPanel.add(gfacURLLabel);
        infoPanel.add(this.gfacUrlListField);
        infoPanel.add(interactLabel);
        infoPanel.add(this.interactChkBox);
        infoPanel.add(crossProductLabel);
        infoPanel.add(chkRunWithCrossProduct);
        
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

        this.dialog = new XBayaDialog(this.engine, "Invoke workflow", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }

    private void execute() {
        final List<String> arguments = new ArrayList<String>();

        String topic = this.topicTextField.getText();
        if (topic.length() == 0) {
            this.engine.getErrorWindow().error(ErrorMessages.TOPIC_EMPTY_ERROR);
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
        Collection<WSNode> wsNodes = GraphUtil.getWSNodes(this.engine.getWorkflow().getGraph());
        // This is to enable service interaction with the back end
        if (this.interactChkBox.isSelected()) {
            LinkedList<String> nodeIDs = new LinkedList<String>();
            for (WSNode node : wsNodes) {
                nodeIDs.add(node.getID());
                ((WSNodeGUI) node.getGUI()).setInteractiveMode(true);
            }
            notifConfig.setInteractiveNodeIDs(nodeIDs);
        } else {
            for (WSNode node : wsNodes) {
                ((WSNodeGUI) node.getGUI()).setInteractiveMode(false);
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

        // final String xregistryUrl = this.xRegistryTextField.getText();
        // if (null != xregistryUrl && !"".equals(xregistryUrl)) {
        // try {
        // this.engine.getConfiguration().setXRegistryURL(new URI(xregistryUrl));
        // } catch (URISyntaxException e) {
        // this.engine.getErrorWindow().error(e);
        // }
        // }

        final String gFacUrl = (String) this.gfacUrlListField.getSelectedItem();
        if (null != gFacUrl && !"".equals(gFacUrl)) {
            try {
                this.engine.getConfiguration().setGFacURL(new URI(gFacUrl));
            } catch (URISyntaxException e) {
                this.engine.getErrorWindow().error(e);
            }
        }
        this.engine.getConfiguration().setTopic(topic);

        /*
         * Load host description from xregistry and add to interpreter
         */
        LeadResourceMapping mapping = null;
        // String host = this.resourceSelectionComboBox.getText();
        // if (host != null && !host.isEmpty()) {
        // XRegistryAccesser xregistryAccesser = new XRegistryAccesser(this.engine);
        //
        // HostDescriptionRegistrationWindow hostWindow = HostDescriptionRegistrationWindow.getInstance();
        //
        // if (!hostWindow.isEngineSet()) {
        // hostWindow.setXBayaEngine(this.engine);
        // }
        //
        // HostBean hostBean = xregistryAccesser.getHostBean(host);
        //
        // mapping = new LeadResourceMapping(host);
        // try {
        // mapping.setGatekeeperEPR(new URI(hostBean.getGateKeeperendPointReference()));
        // } catch (Exception e) {
        // this.engine.getErrorWindow().error(e);
        // }
        //
        // }

        final LeadResourceMapping resourceMapping = mapping;
        final String topicString = topic;
        new Thread() {
            /**
             * @see java.lang.Thread#run()
             */
            @Override
            public void run() {

                WorkflowInterpreter workflowInterpreter = new WorkflowInterpreter(
                        DynamicWorkflowRunnerWindow.this.engine, topicString);
                workflowInterpreter.setRunWithCrossProduct(isRunCrossProduct);
                try {
                    MonitorConfiguration notifConfig = DynamicWorkflowRunnerWindow.this.engine.getMonitor()
                            .getConfiguration();
                    notifConfig.setTopic(topicString);
                    DynamicWorkflowRunnerWindow.this.engine.getMonitor().start();

                    if (resourceMapping != null)
                        workflowInterpreter.setResourceMapping(resourceMapping);

                    workflowInterpreter.scheduleDynamically();
                } catch (XBayaException e) {
                    try {
                        workflowInterpreter.cleanup();
                    } catch (MonitorException e1) {
                        DynamicWorkflowRunnerWindow.this.engine.getErrorWindow().error(e1);
                    }
                    DynamicWorkflowRunnerWindow.this.engine.getErrorWindow().error(e);
                }

            }
        }.start();

        hide();
    }
}