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

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.apache.airavata.workflow.model.component.ComponentReference;
import org.apache.airavata.workflow.model.component.ComponentRegistry;

public class ComponentTreeNode extends DefaultMutableTreeNode {

    private ComponentReference componentReference;

    private ComponentRegistry componentRegistry;

    /**
     * Constructs a ComponentTreeNode.
     * 
     * @param name
     */
    public ComponentTreeNode(String name) {
        super(name);
    }

    /**
     * Constructs a ComponentTreeNode.
     * 
     * @param componentReference
     */
    public ComponentTreeNode(ComponentReference componentReference) {
        super(componentReference);
        this.componentReference = componentReference;
    }

    /**
     * Constructs a ComponentTreeNode.
     * 
     * @param componentRegistry
     */
    public ComponentTreeNode(ComponentRegistry componentRegistry) {
        super(componentRegistry);
        this.componentRegistry = componentRegistry;
    }

    /**
     * @return The component reference.
     */
    public ComponentReference getComponentReference() {
        return this.componentReference;
    }

    /**
     * @param componentRegistry
     */
    public void setComponentRegistry(ComponentRegistry componentRegistry) {
        super.setUserObject(componentRegistry);
        this.componentRegistry = componentRegistry;
    }

    /**
     * @return The component registry.
     */
    public ComponentRegistry getComponentRegistry() {
        return this.componentRegistry;
    }

    /**
     * @see javax.swing.tree.DefaultMutableTreeNode#insert(javax.swing.tree.MutableTreeNode, int)
     */
    @Override
    public void insert(MutableTreeNode newChild, int childIndex) {
        if (!(newChild instanceof ComponentTreeNode)) {
            throw new IllegalArgumentException();
        }
        super.insert(newChild, childIndex);
    }

    /**
     * @return The children.
     */
    @SuppressWarnings("unchecked")
    public List<ComponentTreeNode> getChildren() {
        return this.children;
    }
}