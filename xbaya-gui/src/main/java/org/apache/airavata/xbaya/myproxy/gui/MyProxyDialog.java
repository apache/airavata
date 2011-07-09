/*
 * Copyright (c) 2006-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: MyProxyDialog.java,v 1.13 2008/10/21 14:11:49 cherath Exp $
 */

package org.apache.airavata.xbaya.myproxy.gui;

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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;

/**
 * @author Satoshi Shirasuna
 */
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
     * Constructs a MyLeadSaveWorkflowWindow.
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
            this.engine.getErrorWindow().error(ErrorMessages.MYPROXY_HOST_EMPTY);
            return;
        }
        if (portString.length() == 0) {
            this.engine.getErrorWindow().error(ErrorMessages.MYPROXY_PORT_EMPTY);
            return;
        }
        int port;
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            this.engine.getErrorWindow().error(ErrorMessages.MYPROXY_PORT_WRONG, e);
            return;
        }
        if (username.length() == 0) {
            this.engine.getErrorWindow().error(ErrorMessages.USERNAME_EMPTY);
            return;
        }
        if (passphrase.length() < 6) {
            this.engine.getErrorWindow().error(ErrorMessages.MYPROXY_PASSPHRASE_WRONG);
            return;
        }
        if (lifetimeString.length() == 0) {
            this.engine.getErrorWindow().error(ErrorMessages.MYPROXY_LIFETIME_EMPTY);
            return;
        }
        int lifetime;
        try {
            lifetime = Integer.parseInt(lifetimeString);
        } catch (NumberFormatException e) {
            this.engine.getErrorWindow().error(ErrorMessages.MYPROXY_LIFETIME_WRONG, e);
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
        // this.usernameField.setText("biovlab");
        XBayaLabel userLabel = new XBayaLabel("Username", this.usernameField);

        this.passphraseField = new JPasswordField(XBayaTextField.DEFAULT_COLUMNS);
        // this.passphraseField.setText("biovlab100");
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

        // this.dialog = new XBayaDialog(this.engine, TITLE, mainPanel,
        // buttonPanel);
        this.dialog = new XBayaDialog(this.engine, TITLE, DESCRIPTION, mainPanel, buttonPanel);
        this.dialog.setDefaultButton(this.okButton);
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2006-2007 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice, the list of authors in the original
 * source code, this list of conditions and the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * disclaimer listed in this license in the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme! Lab. For further information please
 * visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and wherever such third-party acknowledgments
 * normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall not be used to endorse or promote
 * products derived from this software without prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name nor may "Indiana University" appear in
 * their name, without prior written permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided does not infringe the patent or any other
 * intellectual property rights of any other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE
 * MADE. INDIANA UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF INFRINGEMENT OF
 * THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS. INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS
 * FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE. LICENSEE ASSUMES THE
 * ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF
 * INFORMATION GENERATED USING SOFTWARE.
 */

