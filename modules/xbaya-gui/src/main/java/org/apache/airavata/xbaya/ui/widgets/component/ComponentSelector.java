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
package org.apache.airavata.xbaya.ui.widgets.component;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.component.ComponentOperationReference;
import org.apache.airavata.workflow.model.component.ComponentReference;
import org.apache.airavata.workflow.model.component.ComponentRegistry;
import org.apache.airavata.workflow.model.component.ComponentRegistryException;
import org.apache.airavata.workflow.model.component.ws.WSComponent;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.registry.ComponentController;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.apache.airavata.xbaya.ui.widgets.XBayaComponent;
import org.apache.airavata.xbaya.ui.widgets.component.ComponentSelectorEvent.ComponentSelectorEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ComponentTreeViewer class shows the selectedComponent tree.
 * 
 */
public class ComponentSelector implements XBayaComponent {

    /**
     * The title.
     */
    public static final String TITLE = "Component List";

    private static final Logger logger = LoggerFactory.getLogger(ComponentSelector.class);

    private XBayaEngine engine;

    private JTree tree;

    private ComponentTreeModel treeModel;

    private ComponentReference selectedComponentReference;

    private Component selectedComponent;

    private List<ComponentSelectorListener> listeners;

    private DragSourceListener dragSourceListener;

    private JPopupMenu popup;

    /**
     * @param engine
     */
    public ComponentSelector(XBayaEngine engine) {
        this.engine = engine;
        this.listeners = new LinkedList<ComponentSelectorListener>();
        initGUI();
    }

    /**
     * @return the Pane
     */
    public JTree getSwingComponent() {
        return this.tree;
    }

    /**
     * Adds a new selectedComponent registry to the end of the tree.
     * 
     * @param componentTree
     */
    public void addComponentTree(ComponentTreeNode componentTree) {
        addComponentTree(-1, componentTree);
    }

    public void removeComponentTree(final ComponentTreeNode componentTree) {
        ComponentSelector.this.treeModel.removeNodeFromParent(componentTree);
//    	SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                ComponentSelector.this.treeModel.removeNodeFromParent(componentTree);
//            }
//
//        });
    }

    public synchronized void removeComponentRegistry(final String componentRegistryName) {
        ComponentTreeNode root = ComponentSelector.this.treeModel.getRoot();
        ComponentTreeNode[] treeNodes = root.getChildren().toArray(new ComponentTreeNode[]{});
        for(ComponentTreeNode treeNode:treeNodes){
            if (treeNode.getComponentRegistry().getName().equals(componentRegistryName)){
                root.remove(treeNode);
            }
        }
        treeModel.reload();
    }
    
    /**
     * Adds a new selectedComponent registry to the specified location.
     * 
     * @param index
     *            The index to ineart a new selectedComponent registry
     * @param componentTree
     */
    public void addComponentTree(final int index, final ComponentTreeNode componentTree) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ComponentTreeNode root = ComponentSelector.this.treeModel.getRoot();
                if (index < 0) {
                    // Have to go through DefaultTreeModol to dynamically
                    // add a node
                    ComponentSelector.this.treeModel.addNodeInto(componentTree, root);
                } else {
                    // Have to go through DefaultTreeModol to dynamically
                    // add a node
                    ComponentSelector.this.treeModel.insertNodeInto(componentTree, root, index);
                }
                makeVisible(componentTree);
            }

        });
    }

    /**
     * Removes a registry currently selected.
     */
    public void removeSelectedRegistry() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TreePath selectionPath = ComponentSelector.this.tree.getSelectionPath();
                ComponentTreeNode selectedNode = (ComponentTreeNode) selectionPath.getLastPathComponent();
                if (selectedNode.getLevel() == 1) {
                    ComponentSelector.this.treeModel.removeNodeFromParent(selectedNode);
                }
            }
        });
    }

    /**
     * @throws ComponentRegistryException
     */
    public void updateSelectedRegistry() throws ComponentRegistryException {
        final TreePath[] selectionPathHolder = new TreePath[1];
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    selectionPathHolder[0] = ComponentSelector.this.tree.getSelectionPath();
                }
            });
        } catch (InterruptedException e) {
            // Should not happen.
            throw new WorkflowRuntimeException(e);
        } catch (InvocationTargetException e) {
            // Should not happen.
            throw new WorkflowRuntimeException(e);
        }

        TreePath selectionPath = selectionPathHolder[0];
        if (selectionPath == null) {
            // TODO this case should be handled in the menu before comming here.
            return;
        }

        if (selectionPath.getPathCount() >= 2) {
            final ComponentTreeNode selectedNode = (ComponentTreeNode) selectionPath.getPath()[1];
            reloadComponentRegistryNode(selectedNode);
        }
    }

	private void reloadComponentRegistryNode(
			final ComponentTreeNode selectedNode)
			throws ComponentRegistryException {
		ComponentRegistry registry = selectedNode.getComponentRegistry();
		final ComponentTreeNode componentTree = ComponentController.getComponentTree(registry);

		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		        ComponentTreeNode root = ComponentSelector.this.treeModel.getRoot();
		        int index = root.getIndex(selectedNode);
		        ComponentSelector.this.treeModel.removeNodeFromParent(selectedNode);
		        ComponentSelector.this.treeModel.insertNodeInto(componentTree, root, index);
		    }
		});
	}

    /**
     * Updates all the registry entries.
     * 
     * @throws ComponentRegistryException
     */
    public void update() throws ComponentRegistryException {
        final List<ComponentRegistry> registries = new ArrayList<ComponentRegistry>();
        if (SwingUtilities.isEventDispatchThread()) {
            getRegistries(registries);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        getRegistries(registries);
                    }
                });
            } catch (InterruptedException e) {
                // Should not happen.
                throw new WorkflowRuntimeException(e);
            } catch (InvocationTargetException e) {
                // Should not happen.
                throw new WorkflowRuntimeException(e);
            }
        }

        final List<ComponentTreeNode> newSubTrees = new ArrayList<ComponentTreeNode>();
        for (ComponentRegistry registry : registries) {
            ComponentTreeNode componentTree = ComponentController.getComponentTree(registry);
            newSubTrees.add(componentTree);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ComponentTreeNode root = ComponentSelector.this.treeModel.getRoot();
                ComponentSelector.this.treeModel.removeChildren(root);
                logger.debug("Removed all");
                for (ComponentTreeNode subTree : newSubTrees) {
                    ComponentSelector.this.treeModel.addNodeInto(subTree, root);
                }
                makeVisible((ComponentTreeNode) root.getFirstChild());
            }
        });

    }

    /**
     * Returns the selectedComponent.
     * 
     * @return The selectedComponent
     */
    public Component getSelectedComponent() {
        return this.selectedComponent;
    }

    /**
     * @param listener
     */
    public synchronized void addComponentSelectorListener(ComponentSelectorListener listener) {
        this.listeners.add(listener);
    }

    /**
     * @param listener
     */
    public synchronized void removeComponentSelectorListener(ComponentSelectorListener listener) {
        this.listeners.remove(listener);
    }

    private void dragGestureRecognized(DragGestureEvent event) {
        if (this.selectedComponentReference != null) {
            event.startDrag(DragSource.DefaultCopyDrop,
                    new ComponentSourceTransferable(this.selectedComponentReference), this.dragSourceListener);
        }
    }

    private List<ComponentRegistry> getRegistries(List<ComponentRegistry> registries) {
        ComponentTreeNode root = this.treeModel.getRoot();
        for (ComponentTreeNode componentTree : root.getChildren()) {
            registries.add(componentTree.getComponentRegistry());
        }
        return registries;
    }

    private void makeVisible(ComponentTreeNode node) {
        // Make sure the user can see the new node, but don't scroll to
        // right.
        TreePath treePath = new TreePath(node.getPath());
        Rectangle bounds = ComponentSelector.this.tree.getPathBounds(treePath);
        if (bounds != null) {
            // Prevent right scroll.
            bounds.x = 0;
            bounds.width = 0;
            this.tree.scrollRectToVisible(bounds);
        } else {
            // null during the initialization.
            this.tree.scrollPathToVisible(treePath);
        }
    }

    /**
     * This method is called when a component is selected. It reads the component information from the server and set
     * the selectedComponent.
     * 
     * @param treePath
     *            The path of the selected selectedComponent.
     */
    private void select(TreePath treePath) {
        final ComponentTreeNode selectedNode = (ComponentTreeNode) treePath.getLastPathComponent();
        final ComponentReference componentReference = selectedNode.getComponentReference();
        selectComponent(null);
        this.selectedComponentReference = null;
        if (componentReference != null) {
            this.selectedComponentReference = componentReference;
            new Thread() {
                @Override
                public void run() {
                    try {
                        // get all components and check the number of
                        // components. If there are multiple, expand the tree.
                        final List<? extends Component> components = componentReference.getComponents();
                        if (components.size() == 1) {
                            selectComponent(components.get(0));
                        } else {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    expandTreeLeaf(selectedNode, components);
                                }
                            });
                        }

                    } catch (ComponentException e) {
                        selectComponent(null);
                        ComponentSelector.this.engine.getGUI().getErrorWindow().error(ErrorMessages.COMPONENT_FORMAT_ERROR, e);
                    } catch (ComponentRegistryException e) {
                        selectComponent(null);
                        ComponentSelector.this.engine.getGUI().getErrorWindow().error(ErrorMessages.COMPONENT_LOAD_ERROR, e);
                    } catch (RuntimeException e) {
                        selectComponent(null);
                        ComponentSelector.this.engine.getGUI().getErrorWindow().error(ErrorMessages.COMPONENT_LOAD_ERROR, e);
                    } catch (Exception e) {
                        selectComponent(null);
                        ComponentSelector.this.engine.getGUI().getErrorWindow().error(ErrorMessages.COMPONENT_LOAD_ERROR, e);
                    }
                }
            }.start();

        }
    }

    private void expandTreeLeaf(ComponentTreeNode selectedNode, List<? extends Component> components) {
        ComponentReference componentReference = selectedNode.getComponentReference();
        ComponentTreeNode newNode = new ComponentTreeNode(componentReference.getName());

        ComponentTreeNode parent = (ComponentTreeNode) selectedNode.getParent();
        int index = this.treeModel.getIndexOfChild(parent, selectedNode);
        this.treeModel.removeNodeFromParent(selectedNode);
        this.treeModel.insertNodeInto(newNode, parent, index);

        for (Component component : components) {
            WSComponent wsComponent = (WSComponent) component;
            String operationName = wsComponent.getOperationName();
            ComponentOperationReference reference = new ComponentOperationReference(operationName, wsComponent);
            ComponentTreeNode child = new ComponentTreeNode(reference);
            this.treeModel.addNodeInto(child, newNode);
        }
        // expand
        TreeNode[] path = newNode.getPath();
        this.tree.expandPath(new TreePath(path));
    }

    private void selectComponent(Component component) {
        this.selectedComponent = component;
        notifyListeners(new ComponentSelectorEvent(ComponentSelectorEventType.COMPONENT_SELECTED, this, component));
    }

    private void showPopupIfNecessary(MouseEvent event) {
        Point point = event.getPoint();
        TreePath path = this.tree.getClosestPathForLocation(point.x, point.y);
        this.tree.setSelectionPath(path);

        if (path.getPathCount() >= 2) {
            this.popup.show(event.getComponent(), point.x, point.y);
        }
    }

    private void notifyListeners(ComponentSelectorEvent event) {
        for (ComponentSelectorListener listener : this.listeners) {
            listener.componentSelectorChanged(event);
        }
    }

    private void initGUI() {
        this.treeModel = new ComponentTreeModel(new ComponentTreeNode("Components"));
        this.tree = new JTree(this.treeModel);

        // Add a tool tip.
        ToolTipManager.sharedInstance().registerComponent(this.tree);
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
            @Override
            public java.awt.Component getTreeCellRendererComponent(JTree t, Object value, boolean sel,
                    boolean expanded, boolean leaf, int row, boolean focus) {
                super.getTreeCellRendererComponent(t, value, sel, expanded, leaf, row, focus);

                ComponentTreeNode node = (ComponentTreeNode) value;
                if (node.getComponentReference() == null) {
                    setToolTipText(null);
                } else {
                    setToolTipText("Drag a component to the composer to add");
                }
                return this;
            }
        };

        // Change icons
        try {
            renderer.setOpenIcon(SwingUtil.createImageIcon("opened.gif"));
            renderer.setClosedIcon(SwingUtil.createImageIcon("closed.gif"));
            renderer.setLeafIcon(SwingUtil.createImageIcon("leaf.gif"));
        } catch (RuntimeException e) {
            logger.warn("Failed to load image icons.  " + "It will use the default icons instead.", e);
        }

        this.tree.setCellRenderer(renderer);
        this.tree.setShowsRootHandles(true);
        this.tree.setEditable(false);
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        this.tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent event) {
                // Doesn't do anything if deselected, which happens during the
                // update.
                if (event.isAddedPath()) {
                    TreePath path = event.getPath();
                    select(path);
                }
            }
        });

        // Drag and dtop
        DragGestureListener dragGestureListener = new DragGestureListener() {
            public void dragGestureRecognized(DragGestureEvent event) {
                ComponentSelector.this.dragGestureRecognized(event);
            }
        };
        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this.tree, DnDConstants.ACTION_COPY_OR_MOVE, dragGestureListener);

        this.dragSourceListener = new DragSourceAdapter() {
            // Overwrite some methods when needed.
        };

        // Popup
        this.tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (event.isPopupTrigger()) {
                    showPopupIfNecessary(event);
                }
            }
        });
        createNodePopupMenu();
    }

    private void createNodePopupMenu() {
        this.popup = new JPopupMenu();
        JMenuItem refreshItem = new JMenuItem("Refresh Registry");
        refreshItem.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            updateSelectedRegistry();
                        } catch (ComponentRegistryException e) {
                            ComponentSelector.this.engine.getGUI().getErrorWindow().error(
                                    ErrorMessages.COMPONENT_LIST_LOAD_ERROR, e);
                        } catch (RuntimeException e) {
                            ComponentSelector.this.engine.getGUI().getErrorWindow().error(
                                    ErrorMessages.COMPONENT_LIST_LOAD_ERROR, e);
                        } catch (Error e) {
                            ComponentSelector.this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                        }
                    }
                }.start();
            }
        });
        this.popup.add(refreshItem);
    }

    public void refresh() {
        this.getSwingComponent().repaint();

        this.tree.repaint();
        this.treeModel.reload();
    }

}