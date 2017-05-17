/**
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
 */
package org.apache.airavata.xbaya.ui.dialogs.registry.browser;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.airavata.xbaya.XBayaEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCRBrowserDialog extends JDialog {

    /**
	 * 
	 */
    private static final Logger log = LoggerFactory.getLogger(JCRBrowserDialog.class);
    private static final long serialVersionUID = 2866874255829295553L;
    private JPanel contentPanel = new JPanel();
    private XBayaEngine engine;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            JCRBrowserDialog dialog = new JCRBrowserDialog(null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Create the dialog.
     */
    public JCRBrowserDialog(XBayaEngine engine) {
        setEngine(engine);
        initGUI();
    }

    private void initGUI() {
        setModal(true);
        setLocationRelativeTo(null);
        setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
//        contentPanel = new JCRBrowserPanel(getEngine());
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        // contentPanel.setLayout(new BorderLayout(0, 0));
        // {
        // JScrollPane scrollPane = new JScrollPane();
        // contentPanel.add(scrollPane, BorderLayout.CENTER);
        // {
        // JTree tree = new JTree(AiravataTreeNodeFactory.getTreeNode(getJCRRegistry(),null));
        // tree.setCellRenderer(new RegistryTreeCellRenderer());
        // scrollPane.setViewportView(tree);
        // }
        // }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("Close");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        close();
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
        }
    }

    public void close() {
        setVisible(false);
    }

    public void open() {
        setVisible(true);
    }

    public XBayaEngine getEngine() {
        return engine;
    }

    public void setEngine(XBayaEngine engine) {
        this.engine = engine;
    }

}
