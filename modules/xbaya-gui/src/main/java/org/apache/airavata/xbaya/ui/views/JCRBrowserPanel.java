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
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.xbaya.ui.views;
//
//import java.awt.BorderLayout;
//import java.awt.Component;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Observable;
//import java.util.Observer;
//
//import javax.swing.JPanel;
//import javax.swing.JPopupMenu;
//import javax.swing.JScrollPane;
//import javax.swing.JTree;
//import javax.swing.KeyStroke;
//import javax.swing.border.EmptyBorder;
//import javax.swing.tree.DefaultTreeModel;
//
//import org.apache.airavata.client.api.AiravataAPI;
//import org.apache.airavata.workflow.model.component.registry.JCRComponentRegistry;
//import org.apache.airavata.xbaya.XBayaEngine;
//import org.apache.airavata.xbaya.registrybrowser.nodes.AbstractAiravataTreeNode;
//import org.apache.airavata.xbaya.registrybrowser.nodes.AiravataTreeNodeFactory;
//import org.apache.airavata.xbaya.registrybrowser.nodes.RegistryTreeCellRenderer;
//import org.apache.airavata.xbaya.ui.actions.AbstractBrowserActionItem;
//import org.apache.airavata.xbaya.ui.actions.registry.browser.AddAction;
//import org.apache.airavata.xbaya.ui.actions.registry.browser.BrowserAction;
//import org.apache.airavata.xbaya.ui.actions.registry.browser.CopyAction;
//import org.apache.airavata.xbaya.ui.actions.registry.browser.DeleteAction;
//import org.apache.airavata.xbaya.ui.actions.registry.browser.EditAction;
//import org.apache.airavata.xbaya.ui.actions.registry.browser.ImportAction;
//import org.apache.airavata.xbaya.ui.actions.registry.browser.RefreshAction;
//import org.apache.airavata.xbaya.ui.actions.registry.browser.ViewAction;
////import org.apache.airavata.registry.api.AiravataRegistry2;
//
//public class JCRBrowserPanel extends JPanel implements Observer {
//    private List<AbstractBrowserActionItem> browserActions = new ArrayList<AbstractBrowserActionItem>();
//    /**
//	 * 
//	 */
//    private static final long serialVersionUID = -4490110894914580271L;
//    private XBayaEngine engine;
//    private JTree tree;
//    private JPopupMenu popupMenu;
//    private AbstractBrowserActionItem actionDelete;
//
//    /**
//     * Create the dialog.
//     */
//    public JCRBrowserPanel(XBayaEngine engine) {
//        setEngine(engine);
//        initGUI();
//    }
//
//    private void initGUI() {
//        setBounds(100, 100, 450, 300);
//        this.setBorder(new EmptyBorder(5, 5, 5, 5));
//        this.setLayout(new BorderLayout(0, 0));
//        {
//            JScrollPane scrollPane = new JScrollPane();
//            this.add(scrollPane, BorderLayout.CENTER);
//            {
//                tree = new JTree(AiravataTreeNodeFactory.getTreeNode(getJCRRegistry() == null ? "No registry specified"
//                         : getEngine(), null));
//                tree.addKeyListener(new KeyAdapter() {
//                    @Override
//                    public void keyPressed(KeyEvent e) {
//                        if (e.getKeyCode() == KeyEvent.VK_F5) {
//                            triggerNodeAction(RefreshAction.ID);
//                        }
//                    }
//                });
//                tree.setCellRenderer(new RegistryTreeCellRenderer());
//                scrollPane.setViewportView(tree);
//
//                popupMenu = new JPopupMenu();
//                popupMenu.setLabel("");
//                addPopup(tree, popupMenu);
//
//                AbstractBrowserActionItem actionRefresh = new RefreshAction();
//                actionRefresh.getMenuItem().setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
//                actionRefresh.addActionListener(new ActionListener() {
//                    public void actionPerformed(ActionEvent e) {
//                        triggerNodeAction(RefreshAction.ID);
//                    }
//                });
//
//                actionDelete = new DeleteAction();
//                actionDelete.addActionListener(new ActionListener() {
//                    public void actionPerformed(ActionEvent arg0) {
//                        triggerNodeAction(DeleteAction.ID);
//                    }
//                });
//                AddAction actionAdd = new AddAction();
//                actionAdd.addActionListener(new ActionListener() {
//                    public void actionPerformed(ActionEvent arg0) {
//                        triggerNodeAction(AddAction.ID);
//                    }
//                });
//
//                EditAction actionEdit = new EditAction();
//                actionEdit.addActionListener(new ActionListener() {
//                    public void actionPerformed(ActionEvent arg0) {
//                        triggerNodeAction(EditAction.ID);
//                    }
//                });
//                ImportAction actionImport = new ImportAction();
//                actionImport.addActionListener(new ActionListener() {
//                    public void actionPerformed(ActionEvent arg0) {
//                        triggerNodeAction(ImportAction.ID);
//                    }
//                });
//                CopyAction actionCopy = new CopyAction();
//                actionCopy.addActionListener(new ActionListener() {
//                    public void actionPerformed(ActionEvent arg0) {
//                        triggerNodeAction(CopyAction.ID);
//                    }
//                });
//                ViewAction actionView = new ViewAction();
//                actionView.addActionListener(new ActionListener() {
//                    public void actionPerformed(ActionEvent arg0) {
//                        triggerNodeAction(ViewAction.ID);
//                    }
//                });
//                BrowserAction actionBrowser = new BrowserAction();
//                actionBrowser.addActionListener(new ActionListener() {
//                    public void actionPerformed(ActionEvent arg0) {
//                        triggerNodeAction(BrowserAction.ID);
//                    }
//                });
//                
//                tree.addMouseListener(new MouseAdapter(){
//					@Override
//					public void mouseClicked(MouseEvent e) {
//						if (e.getClickCount() == 2){
//							triggerNodeAction(null);
//						}
//					}
//                });
//                browserActions.add(actionAdd);
//                browserActions.add(actionView);
//                browserActions.add(actionImport);
//                browserActions.add(actionEdit);
//                browserActions.add(actionRefresh);
//                browserActions.add(actionDelete);
//                browserActions.add(actionCopy);
//                browserActions.add(actionBrowser);
//
////                popupMenu.add(actionAdd.getMenuItem());
////                popupMenu.add(actionDelete.getMenuItem());
////                popupMenu.add(actionRefresh.getMenuItem());
//            }
//        }
//    }
//
//    public void close() {
//        setVisible(false);
//    }
//
//    public void open() {
//        setVisible(true);
//    }
//
//    public XBayaEngine getEngine() {
//        return engine;
//    }
//
//    public void setEngine(XBayaEngine engine) {
//        if (this.engine != null) {
//            this.engine.getConfiguration().deleteObserver(this);
//        }
//        this.engine = engine;
//        if (this.engine != null) {
//            this.engine.getConfiguration().addObserver(this);
//        }
//    }
//
//    private AiravataAPI getJCRRegistry() {
//        try {
//            return getEngine().getConfiguration().getAiravataAPI();
//        } catch (Exception e) {
//            // JCR registry not specified yet
//            return null;
//        }
//    }
//
//    public void update(Observable observable, Object o) {
//        if (getEngine().getConfiguration() == observable) {
//            if (o instanceof JCRComponentRegistry) {
//                resetModel();
//            } else if (o instanceof AiravataAPI) {
//                resetModel();
//            }
//        }
//    }
//
//    private void resetModel() {
//        tree.setModel(new DefaultTreeModel(AiravataTreeNodeFactory.getTreeNode(
//                getJCRRegistry() == null ? "No registry specified" : getEngine(), null)));
//    }
//
//    private void addPopup(Component component, final JPopupMenu popup) {
//        component.addMouseListener(new MouseAdapter() {
//            public void mousePressed(MouseEvent e) {
//                if (e.isPopupTrigger()) {
//                    showMenu(e);
//                }
//            }
//
//            public void mouseReleased(MouseEvent e) {
//                if (e.isPopupTrigger()) {
//                    showMenu(e);
//                }
//            }
//
//            private void showMenu(MouseEvent e) {
//                int selRow = tree.getRowForLocation(e.getX(), e.getY());
//                if (selRow != -1 && e.isPopupTrigger()) {
//                    tree.setSelectionRow(selRow);
//                    Object o = tree.getLastSelectedPathComponent();
//                    popup.removeAll();
//                    if (o instanceof AbstractAiravataTreeNode) {
//                        AbstractAiravataTreeNode node = ((AbstractAiravataTreeNode) o);
//                        for (AbstractBrowserActionItem action : browserActions) {
//                            boolean actionSupported = node.isActionSupported(action);
//                            action.setVisible(actionSupported);
//                            if (actionSupported) {
//                                action.setCaption(node.getActionCaption(action));
//                                action.setIcon(node.getActionIcon(action));
//                                action.setDescription(node.getActionDescription(action));
//                                popup.add(action.getMenuItem());
//                            }
//                        }
//                    }
//                    
//                    if (popup.getSubElements().length>0) {
//						popup.show(e.getComponent(), e.getX(), e.getY());
//					}
//                }
//            }
//        });
//    }
//
//    private void triggerNodeAction(String action) {
//        Object o = tree.getLastSelectedPathComponent();
//        if (o instanceof AbstractAiravataTreeNode) {
//            AbstractAiravataTreeNode node = ((AbstractAiravataTreeNode) o);
//            try {
//            	if (action==null){
//            		action=node.getDefaultAction();
//            	}
//                if (action!=null) {
//					node.triggerAction(tree, action);
//				}
//            } catch (Exception e) {
//                e.printStackTrace();
//                getEngine().getGUI().getErrorWindow().error(e);
//            }
//        }
//    }
//}
