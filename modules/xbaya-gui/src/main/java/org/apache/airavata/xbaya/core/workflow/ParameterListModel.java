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
package org.apache.airavata.xbaya.core.workflow;

import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;

import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.system.ParameterNode;

public class ParameterListModel extends AbstractListModel {

    private List<? extends ParameterNode> parameterNodes;

    /**
     * Constructs a ParameterListModel.
     * 
     * @param parameterNodes
     * @param nodes
     */
    public ParameterListModel(List<? extends ParameterNode> parameterNodes) {
        this.parameterNodes = parameterNodes;
    }

    /**
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int index) {
        return this.parameterNodes.get(index).getName();
    }

    /**
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize() {
        return this.parameterNodes.size();
    }

    /**
     * Moves the node at the index up.
     * 
     * @param index
     */
    public void up(int index) {
        if (index < 1 || index >= this.parameterNodes.size()) {
            throw new WorkflowRuntimeException("Illegal index: " + index);
        }
        swap(index - 1, index);
    }

    /**
     * Moves the node at the index down.
     * 
     * @param index
     */
    public void down(int index) {
        if (index < 0 || index >= this.parameterNodes.size() - 1) {
            throw new WorkflowRuntimeException("Illegal index: " + index);
        }
        swap(index, index + 1);
    }

    private void swap(int index0, int index1) {
        Collections.swap(this.parameterNodes, index0, index1);
        fireContentsChanged(this, index0, index1);
    }
}