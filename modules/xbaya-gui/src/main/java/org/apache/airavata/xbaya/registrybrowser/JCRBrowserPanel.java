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

package org.apache.airavata.xbaya.registrybrowser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeModel;

import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.registry.JCRComponentRegistry;
import org.apache.airavata.xbaya.registrybrowser.menu.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.registrybrowser.menu.AddAction;
import org.apache.airavata.xbaya.registrybrowser.menu.DeleteAction;
import org.apache.airavata.xbaya.registrybrowser.menu.EditAction;
import org.apache.airavata.xbaya.registrybrowser.menu.RefreshAction;
import org.apache.airavata.xbaya.registrybrowser.nodes.AbstractAiravataTreeNode;
import org.apache.airavata.xbaya.registrybrowser.nodes.AiravataTreeNodeFactory;
import org.apache.airavata.xbaya.registrybrowser.nodes.RegistryTreeCellRenderer;

public class JCRBrowserPanel extends JPanel implements Observer {
    private List<AbstractBrowserActionItem> browserActions = new ArrayList<AbstractBrowserActionItem>();
    /**
	 * 
	 */
    private static final long serialVersionUID = -4490110894914580271L;
    private XBayaEngine engine;
    private JTree tree;
    private JPopupMenu popupMenu;
    private AbstractBrowserActionItem actionDelete;

    /**
     * Create the dialog.
     */
    public JCRBrowserPanel(XBayaEngine engine) {
        setEngine(engine);
        initGUI();
    }

    private void initGUI() {
        setBounds(100, 100, 450, 300);
        this.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.setLayout(new BorderLayout(0, 0));
        {
            JScrollPane scrollPane = new JScrollPane();
            this.add(scrollPane, BorderLayout.CENTER);
            {
                tree = new JTree(AiravataTreeNodeFactory.getTreeNode(getJCRRegistry() == null ? "No registry specified"
                        : getJCRRegistry(), null));
                tree.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_F5) {
                            triggerNodeAction(RefreshAction.ID);
                        }
                    }
                });
                tree.setCellRenderer(new RegistryTreeCellRenderer());
                scrollPane.setViewportView(tree);

                popupMenu = new JPopupMenu();
                popupMenu.setLabel("");
                addPopup(tree, popupMenu);

                AbstractBrowserActionItem actionRefresh = new RefreshAction();
                actionRefresh.getMenuItem().setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
                actionRefresh.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        triggerNodeAction(RefreshAction.ID);
                    }
                });

                actionDelete = new DeleteAction();
                actionDelete.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        triggerNodeAction(DeleteAction.ID);
                    }
                });
                AddAction actionAdd = new AddAction();
                actionAdd.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        triggerNodeAction(AddAction.ID);
                    }
                });

                EditAction actionEdit = new EditAction();
                actionEdit.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        triggerNodeAction(EditAction.ID);
                    }
                });

                browserActions.add(actionRefresh);
                browserActions.add(actionAdd);
                browserActions.add(actionDelete);
                browserActions.add(actionEdit);

//                popupMenu.add(actionAdd.getMenuItem());
//                popupMenu.add(actionDelete.getMenuItem());
//                popupMenu.add(actionRefresh.getMenuItem());
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
        if (this.engine != null) {
            this.engine.getConfiguration().deleteObserver(this);
        }
        this.engine = engine;
        if (this.engine != null) {
            this.engine.getConfiguration().addObserver(this);
        }
    }

    private Registry getJCRRegistry() {
        try {
            return getEngine().getConfiguration().getJcrComponentRegistry().getRegistry();
        } catch (Exception e) {
            // JCR registry not specified yet
            return null;
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        if (getEngine().getConfiguration() == observable) {
            if (o instanceof JCRComponentRegistry) {
                resetModel();
            } else if (o instanceof Registry) {
                resetModel();
            }
        }
    }

    private void resetModel() {
        tree.setModel(new DefaultTreeModel(AiravataTreeNodeFactory.getTreeNode(
                getJCRRegistry() == null ? "No registry specified" : getJCRRegistry(), null)));
    }

    private void addPopup(Component component, final JPopupMenu popup) {
        component.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            private void showMenu(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                if (selRow != -1 && e.isPopupTrigger()) {
                    tree.setSelectionRow(selRow);
                    Object o = tree.getLastSelectedPathComponent();
                    popup.removeAll();
                    if (o instanceof AbstractAiravataTreeNode) {
                        AbstractAiravataTreeNode node = ((AbstractAiravataTreeNode) o);
                        for (AbstractBrowserActionItem action : browserActions) {
                            boolean actionSupported = node.isActionSupported(action);
                            action.setVisible(actionSupported);
                            if (actionSupported) {
                                action.setCaption(node.getActionCaption(action));
                                action.setIcon(node.getActionIcon(action));
                                action.setDescription(node.getActionDescription(action));
                                popup.add(action.getMenuItem());
                            }
                        }
                    }
                    
                    if (popup.getSubElements().length>0) {
						popup.show(e.getComponent(), e.getX(), e.getY());
					}
                }
            }
        });
    }

    private void triggerNodeAction(String action) {
        Object o = tree.getLastSelectedPathComponent();
        if (o instanceof AbstractAiravataTreeNode) {
            AbstractAiravataTreeNode node = ((AbstractAiravataTreeNode) o);
            try {
                node.triggerAction(tree, action);
            } catch (Exception e) {
                e.printStackTrace();
                getEngine().getErrorWindow().error(e);
            }
        }
    }
}
