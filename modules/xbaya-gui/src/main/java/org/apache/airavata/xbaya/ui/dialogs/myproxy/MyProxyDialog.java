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

package org.apache.airavata.xbaya.ui.dialogs.myproxy;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;

public class MyProxyDialog {

    private static final String TITLE = "Load Security Credential";

    private static final String DESCRIPTION = "Load your Grid credential to access to restricted services.<br>"
            + " If you are lunching XBaya from a portal, your username and passphrase are usually the same as portal's ones.";

    private XBayaEngine engine;

    private MyProxyClient myProxyClient;

    private MyProxyLoader myProxyLoader;

    private XBayaDialog dialog;

    private JButton okButton;

    private XBayaTextField serverField;

    private XBayaTextField portField;

    private XBayaTextField usernameField;

    private JPasswordField passphraseField;

    private XBayaTextField lifetimeField;

    private boolean blocking;

    /**
     * Constructs a SaveWorkflowWindow.
     * 
     * @param engine
     */
    public MyProxyDialog(XBayaEngine engine) {
        this.engine = engine;
        this.myProxyClient = this.engine.getMyProxyClient();
        this.myProxyLoader = new MyProxyLoader(this.engine);
        initGUI();
    }

    /**
     * Shows the window.
     */
    public void show() {
        show(false); // non-blocking (default)
    }

    /**
     * Shows the window.
     * 
     * @param block
     */
    public void show(boolean block) {
        this.blocking = block;

        // set default value
        this.serverField.setText(this.myProxyClient.getServer());
        this.portField.setText(this.myProxyClient.getPort() + "");

        this.lifetimeField.setText(this.myProxyClient.getLifetime() + "");
        this.passphraseField.setText(this.myProxyClient.getPassphrase());
        this.usernameField.setText(this.myProxyClient.getUsername());

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
        String server = this.serverField.getText();
        String portString = this.portField.getText();
        String username = this.usernameField.getText();
        String passphrase = new String(this.passphraseField.getPassword());
        String lifetimeString = this.lifetimeField.getText();

        if (server.length() == 0) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.MYPROXY_HOST_EMPTY);
            return;
        }
        if (portString.length() == 0) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.MYPROXY_PORT_EMPTY);
            return;
        }
        int port;
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.MYPROXY_PORT_WRONG, e);
            return;
        }
        if (username.length() == 0) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.USERNAME_EMPTY);
            return;
        }
        if (passphrase.length() < 6) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.MYPROXY_PASSPHRASE_WRONG);
            return;
        }
        if (lifetimeString.length() == 0) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.MYPROXY_LIFETIME_EMPTY);
            return;
        }
        int lifetime;
        try {
            lifetime = Integer.parseInt(lifetimeString);
        } catch (NumberFormatException e) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.MYPROXY_LIFETIME_WRONG, e);
            return;
        }

        this.myProxyClient.set(server, port, username, passphrase, lifetime);
        if (this.blocking) {
            this.myProxyLoader.load();
            hide();
        } else {
            hide();
            this.myProxyLoader.load();
        }
        this.engine.getConfiguration().setMyProxyLifetime(lifetime);
        this.engine.getConfiguration().setMyProxyPassphrase(passphrase);
        this.engine.getConfiguration().setMyProxyPort(port);
        this.engine.getConfiguration().setMyProxyServer(server);
        this.engine.getConfiguration().setMyProxyUsername(username);
    }

    /**
     * Initializes the GUI
     */
    private void initGUI() {
        this.serverField = new XBayaTextField();
        XBayaLabel hostLabel = new XBayaLabel("MyProxy Server", this.serverField);

        this.portField = new XBayaTextField();
        XBayaLabel portLabel = new XBayaLabel("MyProxy Server Port", this.portField);

        this.usernameField = new XBayaTextField();
        XBayaLabel userLabel = new XBayaLabel("Username", this.usernameField);

        this.passphraseField = new JPasswordField(XBayaTextField.DEFAULT_COLUMNS);
        XBayaLabel passphraseLabel = new XBayaLabel("Passphrase", this.passphraseField);

        this.lifetimeField = new XBayaTextField();
        XBayaLabel lifetimeLabel = new XBayaLabel("Lifetime", this.lifetimeField);

        GridPanel mainPanel = new GridPanel();
        mainPanel.add(hostLabel);
        mainPanel.add(this.serverField);
        mainPanel.add(portLabel);
        mainPanel.add(this.portField);
        mainPanel.add(userLabel);
        mainPanel.add(this.usernameField);
        mainPanel.add(passphraseLabel);
        mainPanel.add(this.passphraseField);
        mainPanel.add(lifetimeLabel);
        mainPanel.add(this.lifetimeField);
        mainPanel.layout(5, 2, GridPanel.WEIGHT_NONE, 1);

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

        this.dialog = new XBayaDialog(this.engine.getGUI(), TITLE, DESCRIPTION, mainPanel, buttonPanel);
        this.dialog.setDefaultButton(this.okButton);
    }
}