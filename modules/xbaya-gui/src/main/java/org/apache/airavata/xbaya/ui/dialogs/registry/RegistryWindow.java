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

package org.apache.airavata.xbaya.ui.dialogs.registry;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;

import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ClientSettings;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryLoader;
import org.apache.airavata.xbaya.registry.PasswordCallbackImpl;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLinkButton;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.apache.airavata.xbaya.util.RegistryConstants;

public class RegistryWindow {

    private XBayaEngine engine;

    private XBayaDialog dialog;

    private XBayaTextField urlTextField;

    private XBayaTextField gatewayTextField;

    private XBayaTextField usernameTextField;

    private JPasswordField passwordTextField;

    private XBayaLinkButton newUserButton;

    private NewRegistryUserDialog newUserWindow;

    private String userName;

    private String password;

    private String regURL;

    private String gateway;

    private static String previousRegURL;

    /**
     * @param engine
     */
    public RegistryWindow(XBayaEngine engine) {
        this.engine = engine;
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
        setRegURL(this.urlTextField.getText());
        setPreviousRegURL(this.urlTextField.getText());
        setUserName(this.usernameTextField.getText());
        setPassword(new String(this.passwordTextField.getPassword()));
        setGateway(this.gatewayTextField.getText());

        if (getRegURL().length() == 0) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.URL_EMPTY);
            return;
        }
        URI url;
        try {
            url = new URI(regURL);
        } catch (URISyntaxException e) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.URL_WRONG, e);
            return;
        }

        AiravataAPI airavataAPI = getAiravataAPI();

//        JCRComponentRegistry registry = null;
//        try {
//            registry = new JCRComponentRegistry(airavataAPI);
//        } catch (Exception e) {
//            this.engine.getGUI().getErrorWindow().error(e.getMessage());
//            return;
//        }
        XBayaConfiguration configuration = this.engine.getConfiguration();
        this.engine.setAiravataAPI(airavataAPI);
        configuration.setAiravataAPI(airavataAPI);
        configuration.setRegigstryUserName(userName);
        configuration.setRegistryPassphrase(password);
        configuration.setRegistryURL(url);
        configuration.setDefaultGateway(gateway);
        engine.updateXBayaConfigurationServiceURLs();
        hide();

//        this.loader.load(registry);
    }

    private void createNewUser() {
        URL specifiedURL = null;
        try {
            specifiedURL = new URL(urlTextField.getText());
        } catch (MalformedURLException e1) {
            // the text box contains invalid url, we'll just ignore it
        }
        if (newUserWindow == null) {
            newUserWindow = new NewRegistryUserDialog(engine);
        }
//        newUserWindow.setUrl(specifiedURL);
        newUserWindow.setUsername(usernameTextField.getText());
        newUserWindow.updateControlData();
        newUserWindow.show();
        if (newUserWindow.isUserCreated()) {
//            urlTextField.setText(newUserWindow.getUrl().toString());
            usernameTextField.setText(newUserWindow.getUsername());
            passwordTextField.setText(newUserWindow.getPassword());
        }
    }

    /**
     * Initializes the GUI.
     */
    private void initGUI() {
        this.urlTextField = new XBayaTextField();
        this.gatewayTextField = new XBayaTextField();
        this.usernameTextField = new XBayaTextField();
        this.passwordTextField = new JPasswordField();
        try {
            if (getPreviousRegURL() != null){
                this.urlTextField.setText(engine.getConfiguration().getRegistryURL().toASCIIString());
            } else if (engine.getConfiguration().isRegURLSetByCMD()){
                this.urlTextField.setText(engine.getConfiguration().getRegistryURL().toASCIIString());
            } else if (ClientSettings.isSettingDefined(XBayaConstants.XBAYA_REGISTRY_URL)){
                this.urlTextField.setText(ClientSettings.getSetting(XBayaConstants.XBAYA_REGISTRY_URL));
            }  else {
                this.urlTextField.setText(engine.getConfiguration().getRegistryURL().toASCIIString());
            }
            if (ClientSettings.isSettingDefined(XBayaConstants.XBAYA_REGISTRY_USER)){
                this.usernameTextField.setText(ClientSettings.getSetting(XBayaConstants.XBAYA_REGISTRY_USER));
            } else {
                this.usernameTextField.setText(engine.getConfiguration().getRegistryUserName());
            }
            if (ClientSettings.isSettingDefined(XBayaConstants.XBAYA_DEFAULT_GATEWAY)){
                this.gatewayTextField.setText(ClientSettings.getSetting(XBayaConstants.XBAYA_DEFAULT_GATEWAY));
            } else {
                this.gatewayTextField.setText(engine.getConfiguration().getDefaultGateway());
            }
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
        }

        try {
            ClientSettings.initializeTrustStore();
        } catch (ApplicationSettingsException e) {
            throw new RuntimeException("An error occurred while initializing client configurations");
        }

        this.passwordTextField.setText(engine.getConfiguration().getRegistryPassphrase());
        XBayaLabel urlLabel = new XBayaLabel("Registry URL", this.urlTextField);
        XBayaLabel gatewayLabel = new XBayaLabel("Gateway", this.gatewayTextField);
        XBayaLabel nameLabel = new XBayaLabel("Username", this.usernameTextField);
        XBayaLabel passLabel = new XBayaLabel("Password", this.usernameTextField);
        this.newUserButton = new XBayaLinkButton("Create new user...");
        newUserButton.setHorizontalAlignment(XBayaLinkButton.RIGHT);
        JLabel emptyLabel = new JLabel("");

        newUserButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createNewUser();
            }
        });

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(urlLabel);
        infoPanel.add(this.urlTextField);
        infoPanel.add(gatewayLabel);
        infoPanel.add(this.gatewayTextField);
        infoPanel.add(nameLabel);
        infoPanel.add(this.usernameTextField);
        infoPanel.add(passLabel);
        infoPanel.add(this.passwordTextField);
        infoPanel.add(emptyLabel);
        infoPanel.add(this.newUserButton);
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

        this.dialog = new XBayaDialog(this.engine.getGUI(), "Configure Airavata Registry", infoPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }

    public AiravataAPI getAiravataAPI(){
        try {
            URI regURI = new URI(getRegURL());
            PasswordCallbackImpl passwordCallback = new PasswordCallbackImpl(userName, password);
            AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(regURI, getGateway(), userName, passwordCallback);
            return airavataAPI;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;

    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getRegURL() {
        return regURL;
    }

    public void setRegURL(String regURL) {
        this.regURL = regURL;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public static String getPreviousRegURL() {
        return previousRegURL;
    }

    public static void setPreviousRegURL(String previousRegURL) {
        RegistryWindow.previousRegURL = previousRegURL;
    }
}