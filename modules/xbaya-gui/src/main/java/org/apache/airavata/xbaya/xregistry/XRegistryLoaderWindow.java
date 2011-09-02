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

package org.apache.airavata.xbaya.xregistry;

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

public class XRegistryLoaderWindow {

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