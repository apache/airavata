/*
 * Copyright (c) 2004-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: MyLeadConfigurationWindow.java,v 1.10 2008/11/13 16:06:38 cherath Exp $
 */

package org.apache.airavata.xbaya.mylead.gui;

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
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.mylead.MyLeadConfiguration;
import org.apache.airavata.xbaya.myproxy.gui.MyProxyDialog;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

/**
 * @author Satoshi Shirasuna
 */
public class MyLeadConfigurationWindow {

    private XBayaEngine engine;

    private MyLeadConfiguration leadConfiguration;

    private XBayaDialog dialog;

    private XBayaTextField urlTextField;

    private XBayaTextField userIDTextField;

    private XBayaTextField projectTextField;

    /**
     * @param engine
     * 
     */
    public MyLeadConfigurationWindow(XBayaEngine engine) {
        this.engine = engine;
        this.leadConfiguration = engine.getMyLead().getConfiguration();
        initGUI();

    }

    /**
     * Shows the dialog.
     * 
     * @throws GSSException
     */
    public void show() throws GSSException {

        GSSCredential credential = this.engine.getMyProxyClient().getProxy();
        if (null == credential) {
            new MyProxyDialog(this.engine).show(true);
            credential = this.engine.getMyProxyClient().getProxy();
            // if its still null => user cancelled
            if (null == credential) {
                return;
            }

        }

        URI url = this.leadConfiguration.getURL();
        String urlString;
        if (url == null) {
            urlString = "";
        } else {
            urlString = url.toString();
        }
        this.urlTextField.setText(urlString);

        this.userIDTextField.setText(credential.getName().toString());

        String project = this.leadConfiguration.getProject();
        if (project == null) {
            project = "";
        }
        this.projectTextField.setText(project);

        this.dialog.show();
    }

    /**
     * Hides the dialog.
     */
    public void hide() {
        this.dialog.hide();
    }

    private void setLeadConfiguration() {
        String urlString = this.urlTextField.getText();
        String user = this.userIDTextField.getText();
        String project = this.projectTextField.getText();

        if (urlString.length() == 0) {
            this.engine.getErrorWindow().error("MyLead Agent URL cannot be empty");
            return;
        }
        URI url;
        try {
            url = new URI(urlString);
        } catch (URISyntaxException e) {
            this.engine.getErrorWindow().error("MyLead Agent URL is in wrong format", e);
            return;
        }

        if (user.length() == 0) {
            this.engine.getErrorWindow().error("User DN cannot be empty");
            return;
        }

        if (project.length() == 0) {
            this.engine.getErrorWindow().error("Project ID cannot be empty");
            return;
        }

        final URI newURL = url;
        final String newUser = user;
        final String newProject = project;
        new Thread() {
            @Override
            public void run() {
                try {
                    MyLeadConfigurationWindow.this.leadConfiguration.set(newURL, newUser, newProject);
                } catch (RuntimeException e) {
                    MyLeadConfigurationWindow.this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                } catch (Error e) {
                    MyLeadConfigurationWindow.this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                }
            }
        }.start();

        hide();
    }

    /**
     * Initializes the GUI.
     */
    private void initGUI() {

        this.urlTextField = new XBayaTextField();
        XBayaLabel urlLabel = new XBayaLabel("MyLead Agent URL", this.urlTextField);

        this.userIDTextField = new XBayaTextField();
        XBayaLabel userIDLabel = new XBayaLabel("User DN", this.userIDTextField);

        this.projectTextField = new XBayaTextField();
        XBayaLabel projectIdLabel = new XBayaLabel("Project ID", this.projectTextField);

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(urlLabel);
        infoPanel.add(this.urlTextField);
        infoPanel.add(userIDLabel);
        infoPanel.add(this.userIDTextField);
        infoPanel.add(projectIdLabel);
        infoPanel.add(this.projectTextField);
        infoPanel.layout(3, 2, GridPanel.WEIGHT_NONE, 1);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setLeadConfiguration();
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

        this.dialog = new XBayaDialog(this.engine, "MyLead Configuration", infoPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2004-2007 The Trustees of Indiana University. All rights reserved.
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
