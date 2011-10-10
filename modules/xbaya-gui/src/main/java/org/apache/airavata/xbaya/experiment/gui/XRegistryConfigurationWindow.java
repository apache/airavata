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

package org.apache.airavata.xbaya.experiment.gui;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.*;

import org.apache.airavata.xbaya.XBaya;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.registry.JCRComponentRegistry;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.myproxy.gui.MyProxyChecker;

public class XRegistryConfigurationWindow {

    private XBayaEngine engine;

    private XBayaDialog dialog;

    private JButton okButton;

    private XBayaTextField uriField;

    private XBayaTextField userName;

    private JPasswordField password;

    private MyProxyChecker myProxyChecker;

    /**
     * Constructs a XRegistry Configuration Window.
     * 
     * @param engine
     */
    public XRegistryConfigurationWindow(XBayaEngine engine) {
        this.engine = engine;
        this.myProxyChecker = new MyProxyChecker(this.engine);
        initGUI();
    }

    /**
     * Shows the window.
     */
    public void show() {

//        if (this.uriField.getText().length() == 0) {
            // Show the default value.
//            URI url = this.engine.getConfiguration().getXRegistryURL();
//            if (url == null) {
//                url = XBayaConstants.DEFAULT_XREGISTRY_URL;
//            }
//            this.uriField.setText(url);
//        }
        this.dialog.show();
    }

    /**
     * Hides the window.
     */
    public void hide() {
        this.dialog.hide();
    }

    private void ok() {

        // Set the name and description to the graph.
        String uriString = this.uriField.getText();
        String userName = this.userName.getText();
        String password = this.password.getPassword().toString();
        JCRComponentRegistry registry = null;
        URI url;

        try {
            url = new URI(uriString);
        } catch (URISyntaxException e) {
            this.engine.getErrorWindow().error(ErrorMessages.URL_WRONG, e);
            return;
        }
        try {
            registry = new JCRComponentRegistry(url, userName, password);
        } catch (Exception e) {
            this.engine.getErrorWindow().error(ErrorMessages.CREDENTIALS_WRONG, e);
            return;
        }
        XBayaConfiguration configuration = this.engine.getConfiguration();
        configuration.setJcrComponentRegistry(registry);
        configuration.setRegigstryUserName(userName);
        configuration.setRegistryPassphrase(password);
        configuration.setRegistryURL(url);
        if (uriString.length() == 0) {
            this.engine.getErrorWindow().error(ErrorMessages.XREGISTRY_URL_EMPTY);
            return;
        }

        URI uri;
        try {
            uri = new URI(uriString).parseServerAuthority();
        } catch (URISyntaxException e) {
            this.engine.getErrorWindow().error(ErrorMessages.XREGISTRY_URL_WRONG, e);
            return;
        }

        hide();

//        this.engine.setXRegistryURL(uri);

    }

    /**
     * Initializes the GUI
     */
    private void initGUI() {
        this.uriField = new XBayaTextField();
        this.userName = new XBayaTextField();
        this.password =  new JPasswordField();

        XBayaLabel uriLabel = new XBayaLabel("URL", this.uriField);
        XBayaLabel userNameLabel = new XBayaLabel("Username", this.userName);
        XBayaLabel passwordLabel = new XBayaLabel("Password", this.password);

        GridPanel mainPanel = new GridPanel();
        mainPanel.add(uriLabel);
        mainPanel.add(this.uriField);
        mainPanel.add(userNameLabel);
        mainPanel.add(this.userName);
        mainPanel.add(passwordLabel);
        mainPanel.add(this.password);

        mainPanel.layout(3, 2, GridPanel.WEIGHT_NONE, 1);

        this.okButton = new JButton("OK");
        this.okButton.addActionListener(new AbstractAction() {
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

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(this.okButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, "Configure the Registry Service", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(this.okButton);
    }
}