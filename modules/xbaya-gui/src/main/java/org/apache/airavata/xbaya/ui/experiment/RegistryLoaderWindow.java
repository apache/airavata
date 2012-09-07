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

package org.apache.airavata.xbaya.ui.experiment;

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

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.registry.RegistryAccesser;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.graph.GraphCanvas;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XbayaEnhancedList;
import org.apache.airavata.xbaya.util.XBayaUtil;

public class RegistryLoaderWindow {

    private XBayaEngine engine;

    private XBayaDialog dialog;

    private JButton okButton;

    private JButton deleteButton;

    private XbayaEnhancedList<RegistrySearchResult> list;

    /**
     * Constructs a RegistryLoaderWindow.
     * 
     * @param engine
     */
    public RegistryLoaderWindow(XBayaEngine engine) {
        this.engine = engine;
        if (XBayaUtil.acquireJCRRegistry(this.engine)) {
            initGUI();
        }
    }

    /**
     * Shows the window.
     */
    public void show() {

        /*
         * this.list.getList().setListData( new String[]{ "Loading the workflow list from the Registry.",
         * "Please wait for a moment."});
         */
        this.list.setEnabled(false);
        this.okButton.setEnabled(false);
        this.deleteButton.setEnabled(false);

        new Thread() {
            @Override
            public void run() {
                try {
                    RegistryAccesser registryAccesser = new RegistryAccesser(RegistryLoaderWindow.this.engine);

                    final Map<String, String> resultList = registryAccesser.getOGCEWorkflowTemplateList();
                    final Set<String> keys = resultList.keySet();

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (resultList == null || resultList.size() == 0) {
                                /*
                                 * OGCEXRegistryLoaderWindow.this.list.getList(). setListData( new
                                 * String[]{"No workflow"});
                                 */
                            } else {
                                Vector<RegistrySearchResult> results = new Vector<RegistrySearchResult>();
                                String val = null;
                                for (String key : keys) {
                                    val = resultList.get(key);
                                    results.add(new RegistrySearchResult(val));
                                }
                                Session session = null;
                                try {
                                    session = val.getSession();
                                } catch (RepositoryException e) {
                                    RegistryLoaderWindow.this.engine.getGUI().getErrorWindow().error(
                                            ErrorMessages.UNEXPECTED_ERROR, e);
                                }
                                if (session != null && session.isLive()) {
                                    session.logout();
                                }
                                RegistryLoaderWindow.this.list.setListData(results);
                                RegistryLoaderWindow.this.list.setEnabled(true);
                            }
                        }
                    });
                } catch (RuntimeException e) {
                    RegistryLoaderWindow.this.engine.getGUI().getErrorWindow().error(
                            ErrorMessages.REGISTRY_WORKFLOW_LIST_LOAD_ERROR, e);
                    hide();
                } catch (Error e) {
                    RegistryLoaderWindow.this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                    hide();
                } catch (RegistryException e) {
                    RegistryLoaderWindow.this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
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
        RegistrySearchResult result = this.list.getSelectedValue();
        hide();

        try {
            Workflow workflow = new RegistryAccesser(this.engine).getWorkflow(result.getResourceName());
            GraphCanvas newGraphCanvas = engine.getGUI().newGraphCanvas(true);
            newGraphCanvas.setWorkflow(workflow);
            //this.engine.setWorkflow(workflow);
            engine.getGUI().getGraphCanvas().setWorkflowFile(null);
//            RegistryLoaderWindow.this.engine.setWorkflow(workflow);
        } catch (Exception e) {
            RegistryLoaderWindow.this.engine.getGUI().getErrorWindow().error(e);
        }
    }

    private void delete() {
        RegistryAccesser registryAccesser = new RegistryAccesser(RegistryLoaderWindow.this.engine);
        for (RegistrySearchResult i : this.list.getSelectedValues()) {
            try {
                registryAccesser.deleteOGCEWorkflow(i.getResourceId());
            } catch (RegistryException e) {
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

        this.list = new XbayaEnhancedList<RegistrySearchResult>();

        this.list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    // double click is same as cliking the OK button.
                    RegistryLoaderWindow.this.okButton.doClick();
                }

                if (RegistryLoaderWindow.this.list.getSelectedIndex() == -2) {
                    RegistryLoaderWindow.this.okButton.setEnabled(false);
                    RegistryLoaderWindow.this.deleteButton.setEnabled(true);
                } else if (RegistryLoaderWindow.this.list.getSelectedIndex() != -1) {
                    RegistryLoaderWindow.this.okButton.setEnabled(true);
                    RegistryLoaderWindow.this.deleteButton.setEnabled(true);
                } else {
                    RegistryLoaderWindow.this.okButton.setEnabled(false);
                    RegistryLoaderWindow.this.deleteButton.setEnabled(false);
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

        this.dialog = new XBayaDialog(this.engine.getGUI(), "Load a Workflow from the Registry", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(this.okButton);
    }
}
