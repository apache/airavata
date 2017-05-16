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

import javax.swing.tree.DefaultTreeModel;

public class ComponentTreeModel extends DefaultTreeModel {

    private ComponentTreeNode root;

    /**
     * Constructs a ComponentTreeModel.
     * 
     * @param root
     */
    public ComponentTreeModel(ComponentTreeNode root) {
        super(root);
        this.root = root;
    }

    /**
     * @see javax.swing.tree.DefaultTreeModel#getRoot()
     */
    @Override
    public ComponentTreeNode getRoot() {
        return this.root;
    }

    /**
     * @param newChild
     * @param parent
     */
    public void addNodeInto(ComponentTreeNode newChild, ComponentTreeNode parent) {
        insertNodeInto(newChild, parent, parent.getChildCount());
    }

    /**
     * @param parent
     */
    public void removeChildren(ComponentTreeNode parent) {
        int numChild = parent.getChildCount();
        int[] childIndices = new int[numChild];
        Object[] removedChildren = new Object[numChild];
        for (int i = numChild - 1; i >= 0; i--) {
            childIndices[i] = i;
            removedChildren[i] = parent.getChildAt(i);
            parent.remove(i);
        }
        nodesWereRemoved(parent, childIndices, removedChildren);
    }

}