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

package org.apache.airavata.xbaya.gpel.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaList;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowClient.WorkflowType;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.gpel.client.GcSearchList;
import org.gpel.client.GcSearchResult;

public class GPELLoadWindow {

    private XBayaEngine engine;

    private GPELLoader loader;

    private XBayaDialog dialog;

    private JButton okButton;

    private XBayaList<GcSearchResult> list;

    /**
     * Constructs a GPELLoadWindow.
     * 
     * @param engine
     */
    public GPELLoadWindow(XBayaEngine engine) {
        this.engine = engine;
        this.loader = new GPELLoader(engine);
        initGUI();
    }

    /**
     * Shows the window.
     */
    public void show() {

        this.list.getList().setListData(
                new String[] { "Loading the workflow list from the GPEL Engine.", "Please wait for a moment." });
        this.list.setEnabled(false);
        this.okButton.setEnabled(false);

        new Thread() {
            @Override
            public void run() {
                try {
                    WorkflowClient workflowClient = GPELLoadWindow.this.engine.getWorkflowClient();

                    final GcSearchList resultList = workflowClient.list();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (resultList == null || resultList.size() == 0) {
                                GPELLoadWindow.this.list.getList().setListData(new String[] { "No workflow" });
                            } else {
                                Vector<GcSearchResult> results = new Vector<GcSearchResult>();
                                for (GcSearchResult result : resultList.results()) {
                                    results.add(result);
                                }
                                GPELLoadWindow.this.list.setListData(results);
                                GPELLoadWindow.this.list.setEnabled(true);
                            }
                        }
                    });
                } catch (WorkflowEngineException e) {
                    GPELLoadWindow.this.engine.getErrorWindow().error(ErrorMessages.GPEL_WORKFLOW_LIST_LOAD_ERROR, e);
                    hide();
                } catch (RuntimeException e) {
                    GPELLoadWindow.this.engine.getErrorWindow().error(ErrorMessages.GPEL_WORKFLOW_LIST_LOAD_ERROR, e);
                    hide();
                } catch (Error e) {
                    GPELLoadWindow.this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
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
        GcSearchResult result = this.list.getSelectedValue();
        hide();
        this.loader.load(result.getId(), WorkflowType.TEMPLATE, false);
    }

    /**
     * Initializes the GUI
     */
    private void initGUI() {
        this.list = new XBayaList<GcSearchResult>();
        this.list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    if (GPELLoadWindow.this.list.getSelectedIndex() != -1) {
                        GPELLoadWindow.this.okButton.setEnabled(true);
                    }
                }
            }
        });
        this.list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    // double click is same as cliking the OK button.
                    GPELLoadWindow.this.okButton.doClick();
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
                if (value instanceof GcSearchResult) {
                    listCellRendererComponent.setText(((GcSearchResult) value).getTitle());
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
            @Override
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });
        buttonPanel.add(this.okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, "Load a Workflow from the BPEL Engine", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(this.okButton);
    }
}