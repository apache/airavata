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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XbayaEnhancedList;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.xregistry.XRegistryAccesser;
import org.ogce.xregistry.utils.XRegistryClientException;

import xregistry.generated.OGCEResourceData;

public class OGCEXRegistryLoaderWindow {

    private XBayaEngine engine;

    private XBayaDialog dialog;

    private JButton okButton;

    private JButton deleteButton;

    private XbayaEnhancedList<OGCEXRegistrySearchResult> list;

    /**
     * Constructs a XRegistryLoaderWindow.
     * 
     * @param engine
     */
    public OGCEXRegistryLoaderWindow(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    /**
     * Shows the window.
     */
    public void show() {

        /*
         * this.list.getList().setListData( new String[]{ "Loading the workflow list from the XRegistry.",
         * "Please wait for a moment."});
         */
        this.list.setEnabled(false);
        this.okButton.setEnabled(false);
        this.deleteButton.setEnabled(false);

        new Thread() {
            @Override
            public void run() {
                try {
                    XRegistryAccesser xregistryAccesser = new XRegistryAccesser(OGCEXRegistryLoaderWindow.this.engine);

                    final Map<QName, Node> resultList = xregistryAccesser.getOGCEWorkflowTemplateList();
                    final Set<QName> keys = resultList.keySet();

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (resultList == null || resultList.size() == 0) {
                                /*
                                 * OGCEXRegistryLoaderWindow.this.list.getList(). setListData( new
                                 * String[]{"No workflow"});
                                 */
                            } else {
                                Vector<OGCEXRegistrySearchResult> results = new Vector<OGCEXRegistrySearchResult>();
                                Node val = null;
                                for (QName key : keys) {
                                    val = resultList.get(key);
                                    results.add(new OGCEXRegistrySearchResult(val));
                                }
                                Session session = null;
                                try {
                                    session = val.getSession();
                                } catch (RepositoryException e) {
                                    OGCEXRegistryLoaderWindow.this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                                }
                                if (session != null && session.isLive()) {
                                    session.logout();
                                }
                                OGCEXRegistryLoaderWindow.this.list.setListData(results);
                                OGCEXRegistryLoaderWindow.this.list.setEnabled(true);
                            }
                        }
                    });
                } catch (RuntimeException e) {
                    OGCEXRegistryLoaderWindow.this.engine.getErrorWindow().error(
                            ErrorMessages.XREGISTRY_WORKFLOW_LIST_LOAD_ERROR, e);
                    hide();
                } catch (Error e) {
                    OGCEXRegistryLoaderWindow.this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                    hide();
                } catch (XRegistryClientException e) {
                    OGCEXRegistryLoaderWindow.this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                    hide();
                }
            }
        }.start();

        // This has to be the last because it blocks when the dialog is modal.
        this.dialog.show();
    }

    /**
     * Hides the window.
     */
    public void hide() {
        this.dialog.hide();
    }

    private void ok() {
        OGCEXRegistrySearchResult result = this.list.getSelectedValue();
        hide();

        try {
            Workflow workflow = new XRegistryAccesser(this.engine).getWorkflow(result.getResourceName());
            OGCEXRegistryLoaderWindow.this.engine.setWorkflow(workflow);
        } catch (Exception e) {
            OGCEXRegistryLoaderWindow.this.engine.getErrorWindow().error(e);
        }
    }

    private void delete() {
        XRegistryAccesser xregistryAccesser = new XRegistryAccesser(OGCEXRegistryLoaderWindow.this.engine);
        for (OGCEXRegistrySearchResult i : this.list.getSelectedValues()) {
            try {
                xregistryAccesser.deleteOGCEWorkflow(i.getQname());
            } catch (XRegistryClientException e) {
                e.printStackTrace();
            }
        }
        this.list.removeSelectedRows();
        hide();
    }

    /**
     * Initializes the GUI
     */
    private void initGUI() {

        this.list = new XbayaEnhancedList<OGCEXRegistrySearchResult>();

        this.list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    // double click is same as cliking the OK button.
                    OGCEXRegistryLoaderWindow.this.okButton.doClick();
                }

                if (OGCEXRegistryLoaderWindow.this.list.getSelectedIndex() == -2) {
                    OGCEXRegistryLoaderWindow.this.okButton.setEnabled(false);
                    OGCEXRegistryLoaderWindow.this.deleteButton.setEnabled(true);
                } else if (OGCEXRegistryLoaderWindow.this.list.getSelectedIndex() != -1) {
                    OGCEXRegistryLoaderWindow.this.okButton.setEnabled(true);
                    OGCEXRegistryLoaderWindow.this.deleteButton.setEnabled(true);
                } else {
                    OGCEXRegistryLoaderWindow.this.okButton.setEnabled(false);
                    OGCEXRegistryLoaderWindow.this.deleteButton.setEnabled(false);
                }
            }
        });

        GridPanel mainPanel = new GridPanel();
        TitledBorder border = new TitledBorder(new EtchedBorder(), "Select a workflow to load");
        mainPanel.getSwingComponent().setBorder(border);
        mainPanel.add(this.list);
        mainPanel.layout(1, 1, 0, 0);

        JPanel buttonPanel = new JPanel();
        this.okButton = new JButton("Load");
        this.okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });
        buttonPanel.add(this.okButton);

        this.deleteButton = new JButton("Delete");
        this.deleteButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                delete();
            }
        });
        buttonPanel.add(this.deleteButton);

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