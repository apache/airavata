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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.airavata.api.Airavata.Client;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.ThriftClientData;
import org.apache.airavata.xbaya.ThriftServiceType;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.graph.GraphCanvas;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextArea;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;

public class RegistryWorkflowPublisherWindow {

    private XBayaEngine engine;

    private XBayaDialog dialog;

    private JButton okButton;

    private boolean makePublic = false;

    private JCheckBox chkMakePublic;

    private XBayaTextArea descriptionTextArea;

    private XBayaTextField nameTextField;

    private Workflow workflow;

    /**
     * Constructs a OGCEXRegistryWorkflowPublisherWindow.
     * 
     * @param engine
     */
    public RegistryWorkflowPublisherWindow(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    private void ok() {
        String name = this.nameTextField.getText();
        String description = this.descriptionTextArea.getText();

        GraphCanvas graphCanvas = this.engine.getGUI().getGraphCanvas();
        graphCanvas.setNameAndDescription(name, description);
        workflow.setName(name);
        String workflowTemplateName = workflow.getName();
        try {
        		ThriftClientData thriftClientData = engine.getConfiguration().getThriftClientData(ThriftServiceType.API_SERVICE);
				Client client = AiravataClientFactory.createAiravataClient(thriftClientData.getServerAddress(), thriftClientData.getServerPort());
    			if (client.isWorkflowExistWithName(workflowTemplateName)){
    				int result = JOptionPane.showConfirmDialog(this.dialog.getDialog(), "A workflow with the name "+workflowTemplateName+" already exists. Do you want to update it?", "Register Workflow", JOptionPane.YES_NO_CANCEL_OPTION);
    				if (result==JOptionPane.NO_OPTION){
    					return;
    				} else if (result==JOptionPane.CANCEL_OPTION){
    					hide();
    				}
    			String workflowAsString = XMLUtil.xmlElementToString(engine.getGUI().getGraphCanvas().getWorkflow().toXML());
    			org.apache.airavata.model.Workflow workflowData = new org.apache.airavata.model.Workflow();
    			workflowData.setName(workflowTemplateName);
    			workflowData.setGraph(workflowAsString);
    			client.registerWorkflow(engine.getConfiguration().getThriftClientData(ThriftServiceType.API_SERVICE).getGatewayId(), workflowData);
    			hide();
        	}
		} catch (Exception e) {
            this.engine.getGUI().getErrorWindow().error(e.getMessage());
		}
    }

    /**
     * Show the workflow name and description
     */
    public void show() {
        this.workflow = this.engine.getGUI().getWorkflow();
        String name = this.workflow.getName();
        this.nameTextField.setText(name);

        String description = this.workflow.getDescription();
        this.descriptionTextArea.setText(description);

        this.dialog.show();
    }

    /**
     * Hide the workflow name and description
     */
    public void hide() {
        this.dialog.hide();
    }

    /**
     * Intialize UI
     */
    private void initGUI() {

        this.nameTextField = new XBayaTextField();
        XBayaLabel nameLabel = new XBayaLabel("Name", this.nameTextField);

        this.descriptionTextArea = new XBayaTextArea();
        XBayaLabel descriptionLabel = new XBayaLabel("Description", this.descriptionTextArea);

        JPanel buttonPanel = new JPanel();
        this.okButton = new JButton("OK");
        this.okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }

        });
        buttonPanel.add(this.okButton);

        GridPanel mainPanel = new GridPanel();
        TitledBorder border = new TitledBorder(new EtchedBorder(), "Save Workflow to Registry");
        mainPanel.getSwingComponent().setBorder(border);
        mainPanel.add(nameLabel);
        mainPanel.add(this.nameTextField);
        mainPanel.add(descriptionLabel);
        mainPanel.add(this.descriptionTextArea);
        chkMakePublic = new JCheckBox("Make public");
        mainPanel.add(chkMakePublic);
        mainPanel.layout(2, 2, 0, 0);

        this.dialog = new XBayaDialog(this.engine.getGUI(), "Save Workflow to Registry", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(this.okButton);
    }

    /**
     * Verify if the public checkbox is selected or not
     * 
     * @return true or false
     */
    public boolean isMakePublic() {
        return this.chkMakePublic.isSelected();
    }
}