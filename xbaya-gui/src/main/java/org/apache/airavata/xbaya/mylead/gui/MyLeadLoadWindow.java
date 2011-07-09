/*
 * Copyright (c) 2004-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: MyLeadLoadWindow.java,v 1.17 2008/11/26 21:41:24 cherath Exp $
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaList;
import org.apache.airavata.xbaya.mylead.MyLead;
import org.apache.airavata.xbaya.mylead.MyLeadException;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.myproxy.gui.MyProxyChecker;
import org.apache.airavata.xbaya.security.SecurityUtil;
import org.apache.airavata.xbaya.xregistry.XRegistryUtils;
import org.ietf.jgss.GSSCredential;

import xsul5.MLogger;

/**
 * @author Satoshi Shirasuna
 */
public class MyLeadLoadWindow {

    private static final MLogger logger = MLogger.getLogger();

    private XBayaEngine engine;

    private MyLead myLead;

    private List<MyleadWorkflowMetadata> workflowTemplates;

    private XBayaDialog dialog;

    private XBayaList<String> list;

    private JButton okButton;

    private MyLeadLoader loader;

    private MyProxyChecker myProxyChecker;

    /**
     * Creates a MyLeadLoadWorkflowWindow.
     * 
     * @param engine
     *            The XBayaEngine
     */
    public MyLeadLoadWindow(XBayaEngine engine) {
        this(engine, engine.getMyLead());
    }

    /**
     * Creates a MyLeadLoadWorkflowWindow with a specified MyLeadConnection.
     * 
     * This method is used to load workflows from a specified location, not from the user's default location.
     * 
     * @param engine
     *            The XWF Client
     * @param myLead
     *            The specified MyLeadConnection
     */
    public MyLeadLoadWindow(XBayaEngine engine, MyLead myLead) {
        this.engine = engine;
        this.myLead = myLead;
        this.loader = new MyLeadLoader(engine, myLead);
        this.myProxyChecker = new MyProxyChecker(this.engine);
        initGui();
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        URI url = this.myLead.getConfiguration().getURL();
        if (SecurityUtil.isSecureService(url)) {
            // Check if the proxy is loaded.
            boolean loaded = this.myProxyChecker.loadIfNecessary();
            if (!loaded) {
                return;
            }
            // Creates a secure channel in myLEAD.
            MyProxyClient myProxyClient = this.engine.getMyProxyClient();
            GSSCredential proxy = myProxyClient.getProxy();
            this.myLead.setProxy(proxy);
        }

        this.list.setListData(new String[] { "Loading the workflow list from myLead.", "Please wait for a moment." });
        this.list.setEnabled(false);
        this.okButton.setEnabled(false);

        new Thread() {
            @Override
            public void run() {
                loadList();
            }

        }.start();

        // This has to be the last because it blocks when the dialog is modal.
        this.dialog.show();
    }

    /**
     * This method is invoked in a thread.
     */
    private void loadList() {
        try {
            List<MyleadWorkflowMetadata> templates = MyLeadLoadWindow.this.myLead.list();
            setWorkflowTemplates(templates);
            final String[] names = new String[templates.size()];
            for (int i = 0; i < templates.size(); i++) {
                names[i] = templates.get(i).getName();
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    MyLeadLoadWindow.this.list.setListData(names);
                    MyLeadLoadWindow.this.list.setEnabled(true);
                }
            });
        } catch (MyLeadException e) {
            if (this.dialog.isVisible()) {
                this.engine.getErrorWindow().error(ErrorMessages.MYLEAD_TEMPLATE_NAME_LIST_LOAD_ERROR, e);

                hide();
            } else {
                // The dialog has been closed by user.
                logger.caught(e);
            }
        } catch (RuntimeException e) {
            if (this.dialog.isVisible()) {
                this.engine.getErrorWindow().error(ErrorMessages.MYLEAD_TEMPLATE_NAME_LIST_LOAD_ERROR, e);
                hide();
            } else {
                // The dialog has been closed by user.
                logger.caught(e);
            }
        } catch (Error e) {
            this.engine.getErrorWindow().error(ErrorMessages.MYLEAD_TEMPLATE_NAME_LIST_LOAD_ERROR, e);
            hide();
        }
    }

    private void setWorkflowTemplates(List<MyleadWorkflowMetadata> templates) {
        this.workflowTemplates = templates;
    }

    /**
     * Hides the dialog.
     */
    private void hide() {
        this.dialog.hide();
    }

    /**
     * Loads the selected workflow.
     */
    private void load() {
        int index = this.list.getSelectedIndex();
        MyleadWorkflowMetadata template = this.workflowTemplates.get(index);

        QName resouceID = new QName(template.getId(), XRegistryUtils.getFormattedWorkflowId(template.getName()));

        hide();
        // Non blocking call
        this.loader.load(resouceID, false);
    }

    private void initGui() {
        this.list = new XBayaList<String>();
        this.list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    if (MyLeadLoadWindow.this.list.getSelectedIndex() != -1) {
                        MyLeadLoadWindow.this.okButton.setEnabled(true);
                    }
                }
            }
        });
        this.list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    // double click is same as cliking the OK button.
                    MyLeadLoadWindow.this.okButton.doClick();
                }
            }
        });

        GridPanel listPanel = new GridPanel();
        TitledBorder border = new TitledBorder(new EtchedBorder(), "Select a workflow to load");
        listPanel.add(this.list);
        listPanel.layout(1, 1, 0, 0);
        listPanel.getSwingComponent().setBorder(border);

        this.okButton = new JButton("Load");
        this.okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                load();
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

        this.dialog = new XBayaDialog(this.engine, "Load Workflow from MyLead", listPanel, buttonPanel);
        this.dialog.setDefaultButton(this.okButton);
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
