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
package org.apache.airavata.xbaya.ui.dialogs.registry;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ClientSettings;
import org.apache.airavata.model.error.AiravataClientConnectException;
import org.apache.airavata.xbaya.ThriftClientData;
import org.apache.airavata.xbaya.ThriftServiceType;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryLoader;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaComboBox;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.apache.airavata.xbaya.util.RegistryConstants;

public class RegistryWindow {

    private XBayaEngine engine;

    private XBayaDialog dialog;

    private XBayaTextField serverTextField;

    private XBayaTextField portTextField;

    private XBayaTextField gatewayIdTextField;

    private XBayaTextField usernameTextField;

    private XBayaComboBox serviceTypeCombo;


    private String gatewayId;

    private String username;

    private String serverName;

    private String serverPort;

    private static String previousServerName;

    private ThriftServiceType serviceType = ThriftServiceType.API_SERVICE;
    
    private DefaultComboBoxModel serviceTypeModel;
    
    /**
     * @param engine
     */
    public RegistryWindow(XBayaEngine engine, ThriftServiceType serviceType) {
        this.engine = engine;
        this.serviceType = serviceType;
        ComponentRegistryLoader.getLoader(this.engine, RegistryConstants.REGISTRY_TYPE_JCR);
        initGUI();
    }

    /**
     * Displays the dialog.
     */
    public void show() {
        this.dialog.show();
    }

    private void hide() {
        this.dialog.hide();
    }

    private void ok() {
        setServerName(this.serverTextField.getText());
        setPreviousServerName(this.serverTextField.getText());
        setGatewayId(this.gatewayIdTextField.getText());
        setUsername(new String(this.usernameTextField.getText()));
        setServerPort(this.portTextField.getText());
        setServiceType((ThriftServiceType)serviceTypeModel.getSelectedItem());
        try {
			validateData();
			XBayaConfiguration configuration = this.engine.getConfiguration();
	        configuration.addThriftClientData(new ThriftClientData(getServiceType(),serverName, Integer.parseInt(serverPort), gatewayId, username));
	        hide();
		} catch (Exception e) {
            this.engine.getGUI().getErrorWindow().error(e.getMessage());
		}
    }

    /**
     * Initializes the GUI.
     */
    private void initGUI() {
        this.serverTextField = new XBayaTextField();
        this.portTextField = new XBayaTextField();
        this.gatewayIdTextField = new XBayaTextField();
        this.usernameTextField = new XBayaTextField();
        this.serverTextField.setText("localhost");
        this.portTextField.setText("8930");
        this.gatewayIdTextField.setText("sample");
        this.usernameTextField.setText("airavata");
        ThriftClientData thriftClientData = engine.getConfiguration().getThriftClientData(ThriftServiceType.API_SERVICE);
    	if (thriftClientData!=null){
    		this.serverTextField.setText(thriftClientData.getServerAddress());
            this.gatewayIdTextField.setText(thriftClientData.getGatewayId());
            this.portTextField.setText(String.valueOf(thriftClientData.getServerPort()));
            this.usernameTextField.setText(thriftClientData.getUsername());
    	}

        try {
            ClientSettings.initializeTrustStore();
        } catch (ApplicationSettingsException e) {
            throw new RuntimeException("An error occurred while initializing client configurations");
        }

        XBayaLabel serverAddressLabel = new XBayaLabel("Server Address", this.serverTextField);
        XBayaLabel serverPortLabel = new XBayaLabel("Server Port", this.portTextField);
        XBayaLabel gatewayNameLabel = new XBayaLabel("Gateway ID", this.gatewayIdTextField);
        XBayaLabel gatewayUserLabel = new XBayaLabel("Gateway User", this.usernameTextField);
        serviceTypeModel = new DefaultComboBoxModel(ThriftServiceType.values());
        serviceTypeModel.setSelectedItem(getServiceType());
		this.serviceTypeCombo = new XBayaComboBox(serviceTypeModel);
        JLabel serviceTypeLabel = new JLabel("Airavata Service");

//        serviceTypeCombo.addActionListener(new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
////                createNewUser();
//            }
//        });

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(serviceTypeLabel);
        infoPanel.add(this.serviceTypeCombo);
        infoPanel.add(serverAddressLabel);
        infoPanel.add(this.serverTextField);
        infoPanel.add(serverPortLabel);
        infoPanel.add(this.portTextField);
        infoPanel.add(gatewayNameLabel);
        infoPanel.add(this.gatewayIdTextField);
        infoPanel.add(gatewayUserLabel);
        infoPanel.add(this.usernameTextField);
        infoPanel.layout(5, 2, GridPanel.WEIGHT_NONE, 1);
//        infoPanel.layout(2, 2, GridPanel.WEIGHT_NONE, 1);

        infoPanel.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        GridPanel buttonPanel = new GridPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        buttonPanel.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());

        this.dialog = new XBayaDialog(this.engine.getGUI(), "Configure Airavata Thrift Service", infoPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }

    private void validateData() throws NumberFormatException, AiravataClientConnectException{
    		AiravataClientFactory.createAiravataClient(getServerName(), Integer.parseInt(getServerPort()));
    }

    public String getUserName() {
        return username;
    }

    public String getServerPort() {
        return serverPort;
    }

    public String getServerName() {
        return serverName;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setGatewayId(String gateway) {
        this.gatewayId = gateway;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public static String getPreviousServerName() {
        return previousServerName;
    }

    public static void setPreviousServerName(String previousServerName) {
        RegistryWindow.previousServerName = previousServerName;
    }

	public ThriftServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ThriftServiceType serviceType) {
		this.serviceType = serviceType;
	}
}