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

package org.apache.airavata.xbaya.component.gui;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryLoader;
import org.apache.airavata.xbaya.component.registry.JCRComponentRegistry;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaLinkButton;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.util.RegistryConstants;

public class JCRRegistryWindow {

    private XBayaEngine engine;

    private ComponentRegistryLoader loader;

    private XBayaDialog dialog;

    private XBayaTextField urlTextField;

    private XBayaTextField usernameTextField;

    private JPasswordField passwordTextField;

    private XBayaLinkButton newUserButton;

    private NewJCRRegistryUserDialog newUserWindow;

    /**
     * @param engine
     */
    public JCRRegistryWindow(XBayaEngine engine) {
        this.engine = engine;
        this.loader = ComponentRegistryLoader.getLoader(this.engine, RegistryConstants.REGISTRY_TYPE_JCR);
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
        String urlString = this.urlTextField.getText();
        String username = this.usernameTextField.getText();
        String password = new String(this.passwordTextField.getPassword());

        if (urlString.length() == 0) {
            this.engine.getErrorWindow().error(ErrorMessages.URL_EMPTY);
            return;
        }
        URI url;
        try {
            url = new URI(urlString);
        } catch (URISyntaxException e) {
            this.engine.getErrorWindow().error(ErrorMessages.URL_WRONG, e);
            return;
        }
        JCRComponentRegistry registry = null;
        try {
            registry = new JCRComponentRegistry(url, username, password);
        } catch (Exception e) {
            this.engine.getErrorWindow().error(e.getMessage());
            return;
        }
        XBayaConfiguration configuration = this.engine.getConfiguration();
        configuration.setJcrComponentRegistry(registry);
        configuration.setRegigstryUserName(username);
        configuration.setRegistryPassphrase(password);
        configuration.setRegistryURL(url);
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
            newUserWindow = new NewJCRRegistryUserDialog(engine);
        }
        newUserWindow.setUrl(specifiedURL);
        newUserWindow.setUsername(usernameTextField.getText());
        newUserWindow.updateControlData();
        newUserWindow.show();
        if (newUserWindow.isUserCreated()) {
            urlTextField.setText(newUserWindow.getUrl().toString());
            usernameTextField.setText(newUserWindow.getUrl().toString());
            passwordTextField.setText(newUserWindow.getPassword());
        }
    }

    /**
     * Initializes the GUI.
     */
    private void initGUI() {
        this.urlTextField = new XBayaTextField();
        this.usernameTextField = new XBayaTextField();
        this.passwordTextField = new JPasswordField();
        this.urlTextField.setText(XBayaConstants.REGISTRY_URL.toASCIIString());
        this.usernameTextField.setText(XBayaConstants.REGISTRY_USERNAME);
        this.passwordTextField.setText(XBayaConstants.REGISTRY_PASSPHRASE);
        XBayaLabel urlLabel = new XBayaLabel("URL", this.urlTextField);
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
        infoPanel.add(nameLabel);
        infoPanel.add(this.usernameTextField);
        infoPanel.add(passLabel);
        infoPanel.add(this.passwordTextField);
        infoPanel.add(emptyLabel);
        infoPanel.add(this.newUserButton);
        infoPanel.layout(4, 2, GridPanel.WEIGHT_NONE, 1);
        
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

        this.dialog = new XBayaDialog(this.engine, "JCR Registry", infoPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }
}