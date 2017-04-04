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
package org.apache.airavata.xbaya.ui.widgets.amazon;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class S3Tree extends JTree {

    /**
     * 
     * Constructs a S3Tree.
     * 
     */
    public S3Tree() {
        this.setModel(S3TreeModel.getInstance());
        this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.setShowsRootHandles(true);
    }

    /**
	 * 
	 */
    public void clean() {
        this.setModel(S3TreeModel.getInstance().clean());
    }

    /**
	 * 
	 */
    public void refresh() {
        repaint();
    }

    /**
     * 
     * @param child
     * @return
     */
    public DefaultMutableTreeNode addObject(Object child) {
        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = this.getSelectionPath().getParentPath();

        if (parentPath == null) {
            parentNode = (DefaultMutableTreeNode) S3TreeModel.getInstance().getRoot();
        } else {
            parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
        }

        return addObject(parentNode, child, true);
    }

    /**
     * 
     * @param parent
     * @param child
     * @return DefaultMutableTreeNode
     */
    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child) {
        return addObject(parent, child, false);
    }

    /**
     * @param parentName
     * @param child
     * @return
     */
    public DefaultMutableTreeNode addObject(String parentName, Object child) {
        if (parentName.contains("/")) {
            parentName = parentName.substring(0, parentName.indexOf('/'));
        }
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) S3TreeModel.getInstance().getRoot();
        int count = root.getChildCount();
        for (int i = 0; i < count; i++) {
            Object name = ((DefaultMutableTreeNode) root.getChildAt(i)).getUserObject();
            if (parentName.equals(name)) {
                return this.addObject((DefaultMutableTreeNode) root.getChildAt(i), child, true);
            }
        }
        return null;
    }

    /**
     * 
     * @param parent
     * @param child
     * @param shouldBeVisible
     * @return DefaultMutableTreeNode
     */
    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible) {
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);

        if (parent == null) {
            parent = (DefaultMutableTreeNode) S3TreeModel.getInstance().getRoot();
        }

        S3TreeModel.getInstance().insertNodeInto(childNode, parent, parent.getChildCount());

        if (shouldBeVisible) {
            this.scrollPathToVisible(new TreePath(childNode.getPath()));
        }
        return childNode;
    }

    /**
     * 
     * @return DefaultMutableTreeNode
     */
    public DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) this.getLastSelectedPathComponent();
    }
}