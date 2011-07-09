/*
 * Copyright (c) 2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: 
 */
package org.apache.airavata.xbaya.xregistry.gui;

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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryLoader;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.myproxy.gui.MyProxyChecker;
import org.apache.airavata.xbaya.xregistry.XRegistryComponent;
import org.apache.airavata.xbaya.xregistry.XRegistryComponent.Type;
import org.ietf.jgss.GSSCredential;

/**
 * @author Satoshi Shirasuna
 */
public class XRegistryWindow {

    private XBayaEngine engine;

    private ComponentRegistryLoader loader;

    private XBayaDialog dialog;

    private XBayaTextField urlTextField;

    private JRadioButton abstractRadioButton;

    private JRadioButton concreteRadioButton;

    private JRadioButton publicRadioButton;

    private JRadioButton privateRadioButton;

    private MyProxyChecker myProxyChecker;

    /**
     * @param engine
     */
    public XRegistryWindow(XBayaEngine engine) {
        this.engine = engine;
        this.loader = new ComponentRegistryLoader(engine);
        this.myProxyChecker = new MyProxyChecker(this.engine);
        initGUI();
    }

    /**
     * Displays the dialog.
     */
    public void show() {
        if (this.urlTextField.getText().length() == 0) {
            // Show the default value.
            URI url = this.engine.getConfiguration().getXRegistryURL();
            if (url == null) {
                url = XBayaConstants.DEFAULT_XREGISTRY_URL;
            }
            this.urlTextField.setText(url);
        }
        this.abstractRadioButton.setSelected(true); // AWSDL is the default.
        this.privateRadioButton.setSelected(true); // private is the default;
        this.dialog.show();
    }

    private void hide() {
        this.dialog.hide();
    }

    private void ok() {
        String urlString = this.urlTextField.getText();

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

        Type type;
        if (this.concreteRadioButton.isSelected()) {
            type = Type.CONCRETE;
        } else {
            type = Type.ABSTRACT;
        }

        boolean loadProxy = this.privateRadioButton.isSelected();

        hide();

        GSSCredential proxy;

        if (loadProxy) {
            boolean loaded = this.myProxyChecker.loadIfNecessary();
            if (loaded) {
                MyProxyClient myProxyClient = this.engine.getMyProxyClient();
                proxy = myProxyClient.getProxy();
            } else {
                // Error
                return;
            }
        } else {
            // Use anonymous proxy
            proxy = null;
        }

        XRegistryComponent registry = new XRegistryComponent(url, type, proxy);
        this.engine.setXRegistryURL(url);
        this.loader.load(registry);
    }

    /**
     * Initializes the GUI.
     */
    private void initGUI() {

        this.urlTextField = new XBayaTextField();
        XBayaLabel urlLabel = new XBayaLabel("URL", this.urlTextField);

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(urlLabel);
        infoPanel.add(this.urlTextField);
        infoPanel.layout(1, 2, GridPanel.WEIGHT_NONE, 1);

        this.abstractRadioButton = new JRadioButton("Abstract");
        this.concreteRadioButton = new JRadioButton("Concrete");
        ButtonGroup serviceTypeButtonGroup = new ButtonGroup();
        serviceTypeButtonGroup.add(this.abstractRadioButton);
        serviceTypeButtonGroup.add(this.concreteRadioButton);

        this.publicRadioButton = new JRadioButton("Public");
        this.privateRadioButton = new JRadioButton("Private");
        ButtonGroup accessTypeButtonGroup = new ButtonGroup();
        accessTypeButtonGroup.add(this.publicRadioButton);
        accessTypeButtonGroup.add(this.privateRadioButton);

        GridPanel serviceTypePanel = new GridPanel();
        serviceTypePanel.add(this.abstractRadioButton);
        serviceTypePanel.add(this.concreteRadioButton);
        serviceTypePanel.layout(1, 2, 0, GridPanel.WEIGHT_NONE);

        GridPanel accessTypePanel = new GridPanel();
        accessTypePanel.add(this.publicRadioButton);
        accessTypePanel.add(this.privateRadioButton);
        accessTypePanel.layout(1, 2, 0, GridPanel.WEIGHT_NONE);

        GridPanel mainPaenl = new GridPanel();
        mainPaenl.add(infoPanel);
        mainPaenl.add(serviceTypePanel);
        mainPaenl.add(accessTypePanel);
        mainPaenl.layout(3, 1, GridPanel.WEIGHT_NONE, 0);

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

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, "XRegistry", mainPaenl, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2007 The Trustees of Indiana University. All rights reserved.
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
