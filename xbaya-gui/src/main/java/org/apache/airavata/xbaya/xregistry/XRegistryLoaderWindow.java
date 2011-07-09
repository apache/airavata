/*
 * Copyright (c) 2008 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: XRegistryLoaderWindow.java,v 1.6 2008/11/13 00:39:10 cherath Exp $
 */
package org.apache.airavata.xbaya.xregistry;

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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaList;
import org.apache.airavata.xbaya.wf.Workflow;

/**
 * @author Chathura Herath
 */

public class XRegistryLoaderWindow {

    // private static final MLogger logger = MLogger.getLogger();

    private XBayaEngine engine;

    private XBayaDialog dialog;

    private JButton okButton;

    private XBayaList<XRSearchResult> list;

    /**
     * Constructs a XRegistryLoaderWindow.
     * 
     * @param engine
     */
    public XRegistryLoaderWindow(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    /**
     * Shows the window.
     */
    // public void show() {
    //
    // this.list.getList().setListData(
    // new String[]{
    // "Loading the workflow list from the XRegistry.",
    // "Please wait for a moment."});
    // this.list.setEnabled(false);
    // this.okButton.setEnabled(false);
    //
    // new Thread() {
    // @Override
    // public void run() {
    // try {
    // XRegistryAccesser xRegistry = new XRegistryAccesser(XRegistryLoaderWindow.this.engine);
    //
    // final Map<QName, ResourceData> resultList = xRegistry.list();
    // final Set<QName> keys = resultList.keySet();
    //
    // SwingUtilities.invokeLater(new Runnable() {
    // public void run() {
    // if (resultList == null || resultList.size() == 0) {
    // XRegistryLoaderWindow.this.list.getList().setListData(
    // new String[]{"No workflow"});
    // } else {
    // Vector<XRSearchResult> results = new Vector<XRSearchResult>();
    // for (QName key : keys) {
    // ResourceData val = resultList.get(key);
    // results.add(new XRSearchResult(val));
    //
    // }
    // XRegistryLoaderWindow.this.list.setListData(results);
    // XRegistryLoaderWindow.this.list.setEnabled(true);
    // }
    // }
    // });
    // } catch (RuntimeException e) {
    // XRegistryLoaderWindow.this.engine.getErrorWindow().error(
    // ErrorMessages.GPEL_WORKFLOW_LIST_LOAD_ERROR, e);
    // hide();
    // } catch (Error e) {
    // XRegistryLoaderWindow.this.engine.getErrorWindow().error(
    // ErrorMessages.UNEXPECTED_ERROR, e);
    // hide();
    // }catch(XRegistryClientException e){
    // XRegistryLoaderWindow.this.engine.getErrorWindow().error(
    // ErrorMessages.UNEXPECTED_ERROR, e);
    // hide();
    // }
    // }
    // }.start();
    //
    // // This has to be the last because it blocks when the dialog is modal.
    // this.dialog.show();
    // }

    /**
     * Hides the window.
     */
    public void hide() {
        this.dialog.hide();
    }

    private void ok() {
        XRSearchResult result = this.list.getSelectedValue();
        hide();
        try {
            Workflow workflow = new XRegistryAccesser(this.engine).getWorkflow(result.getData().getName());
            XRegistryLoaderWindow.this.engine.setWorkflow(workflow);
        } catch (Exception e) {
            XRegistryLoaderWindow.this.engine.getErrorWindow().error(e);
        }
    }

    /**
     * Initializes the GUI
     */
    private void initGUI() {
        this.list = new XBayaList<XRSearchResult>();
        this.list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    if (XRegistryLoaderWindow.this.list.getSelectedIndex() != -1) {
                        XRegistryLoaderWindow.this.okButton.setEnabled(true);
                    }
                }
            }
        });
        this.list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    // double click is same as cliking the OK button.
                    XRegistryLoaderWindow.this.okButton.doClick();
                }
            }
        });
        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            /**
             * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList,
             *      java.lang.Object, int, boolean, boolean)
             */
            @Override
            public Component getListCellRendererComponent(JList jList, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                DefaultListCellRenderer listCellRendererComponent = (DefaultListCellRenderer) super
                        .getListCellRendererComponent(jList, value, index, isSelected, cellHasFocus);
                if (value instanceof XRSearchResult) {
                    QName qname = ((XRSearchResult) value).getQname();
                    listCellRendererComponent.setText(qname.getNamespaceURI() + ": " + qname.getLocalPart());
                }
                return listCellRendererComponent;
            }

        };
        this.list.getList().setCellRenderer(renderer);

        GridPanel mainPanel = new GridPanel();
        TitledBorder border = new TitledBorder(new EtchedBorder(), "Select a workflow to load");
        mainPanel.getSwingComponent().setBorder(border);
        mainPanel.add(this.list);
        mainPanel.layout(1, 1, 0, 0);

        JPanel buttonPanel = new JPanel();
        this.okButton = new JButton("OK");
        this.okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });
        buttonPanel.add(this.okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, "Load a Workflow from the XRegistry", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(this.okButton);
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2008 The Trustees of Indiana University. All rights reserved.
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
