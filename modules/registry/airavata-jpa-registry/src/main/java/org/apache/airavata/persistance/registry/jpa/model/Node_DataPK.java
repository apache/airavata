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

package org.apache.airavata.persistance.registry.jpa.model;

public class Node_DataPK {
    private String workflow_instanceID;
    private String node_id;
    private int execution_index;

    public Node_DataPK() {
        ;
    }

    public Node_DataPK(String workflow_instanceID, String node_id, int execution_index) {
        this.workflow_instanceID = workflow_instanceID;
        this.node_id = node_id;
        this.execution_index = execution_index;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public String getWorkflow_instanceID() {
        return workflow_instanceID;
    }

    public void setWorkflow_instanceID(String workflow_instanceID) {
        this.workflow_instanceID = workflow_instanceID;
    }

    public String getNode_id() {
        return node_id;
    }

    public void setNode_id(String node_id) {
        this.node_id = node_id;
    }

    public int getExecution_index() {
        return execution_index;
    }

    public void setExecution_index(int execution_index) {
        this.execution_index = execution_index;
    }
}
