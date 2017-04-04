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
package org.apache.airavata.xbaya.ui.menues;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.dialogs.workflow.ParameterPropertyWindow;
import org.apache.airavata.xbaya.ui.dialogs.workflow.WorkflowPropertyWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditMenuItem {

    private XBayaEngine engine;

    private JMenu editMenu;

    private JMenuItem workflowDescriptionItem;

    private JMenuItem parameterReorderingItem;

	private JMenuItem editHostDescription;

	private JMenuItem editServiceDescription;

	private JMenuItem editApplicationDescription;

    private static final Logger logger = LoggerFactory.getLogger(EditMenuItem.class);

    /**
     * Constructs a WorkflowMenu.
     * 
     * @param engine
     */
    public EditMenuItem(XBayaEngine engine) {
        this.engine = engine;
        createWorkflowMenu();
    }

    /**
     * @return The workflow menu.
     */
    public JMenu getMenu() {
        return this.editMenu;
    }

    /**
     * Creates workflow menu.
     */
    private void createWorkflowMenu() {
        this.workflowDescriptionItem = createWorkflowDescriptionItem();
        this.parameterReorderingItem = createParameterReorderingItem();

//        editHostDescription = createEditHostDescription();
//        editServiceDescription = createEditServiceDescription();
//        editApplicationDescription = createEditApplicationDescription();

        editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);

        editMenu.add(this.workflowDescriptionItem);
        editMenu.add(this.parameterReorderingItem);
        
//        editMenu.addSeparator();
        
//        editMenu.add(editHostDescription);
//        editMenu.add(editServiceDescription);
//        editMenu.add(editApplicationDescription);
    }

    private JMenuItem createWorkflowDescriptionItem() {
        JMenuItem menuItem = new JMenuItem("Workflow Properties...");
        menuItem.setMnemonic(KeyEvent.VK_W);
        menuItem.addActionListener(new AbstractAction() {
            private WorkflowPropertyWindow window;

            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = engine.getGUI().getWorkflowPropertyWindow();
                }
                this.window.show();
            }
        });
        return menuItem;
    }

    private JMenuItem createParameterReorderingItem() {
        JMenuItem menuItem = new JMenuItem("Parameter Properties...");
        menuItem.setMnemonic(KeyEvent.VK_P);
        menuItem.addActionListener(new AbstractAction() {
            private ParameterPropertyWindow window;

            public void actionPerformed(ActionEvent e) {
                if (this.window == null) {
                    this.window = new ParameterPropertyWindow(EditMenuItem.this.engine);
                }
                this.window.show();
            }
        });
        return menuItem;
    }

//    private JMenuItem createEditHostDescription() {
//        JMenuItem menuItem = new JMenuItem("Hosts...");
//        menuItem.addActionListener(new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
//            	if (XBayaUtil.acquireJCRRegistry(engine)) {
//					DescriptorEditorDialog dialog = new DescriptorEditorDialog(engine,DescriptorType.HOST);
//					dialog.show();
//				}
//        	}
//        });
//        return menuItem;
//    }
//    
//    private JMenuItem createEditServiceDescription() {
//        JMenuItem menuItem = new JMenuItem("Applications...");
//        menuItem.addActionListener(new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
//            	if (XBayaUtil.acquireJCRRegistry(engine)) {
//					DescriptorEditorDialog dialog = new DescriptorEditorDialog(engine,DescriptorType.SERVICE);
//					dialog.show();
//				}
//            }
//        });
//        return menuItem;
//    }
//    
//    private JMenuItem createEditApplicationDescription() {
//        JMenuItem menuItem = new JMenuItem("Application Descriptions...");
//        menuItem.addActionListener(new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
//            	if (XBayaUtil.acquireJCRRegistry(engine)) {
//					DescriptorEditorDialog dialog = new DescriptorEditorDialog(engine,DescriptorType.APPLICATION);
//					dialog.show();
//				}
//        	}
//        });
//        return menuItem;
//    }
}