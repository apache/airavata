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
package org.apache.airavata.registry.api.workflow;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WorkflowNodeGramData {
    String NodeID;
    String rsl;
    String invokedHost;
    String gramJobID;
    private String workflowInstanceId;

    public WorkflowNodeGramData() {
    }

    public WorkflowNodeGramData(String workflowInstanceId, String nodeID, String rsl, String invokedHost, String gramJobID) {
        NodeID = nodeID;
        this.rsl = rsl;
        this.invokedHost = invokedHost;
        this.gramJobID = gramJobID;
        this.setWorkflowInstanceId(workflowInstanceId);
    }

    public void setNodeID(String nodeID) {
        NodeID = nodeID;
    }

    public void setRsl(String rsl) {
        this.rsl = rsl;
    }

    public void setInvokedHost(String invokedHost) {
        this.invokedHost = invokedHost;
    }

    public void setGramJobID(String gramJobID) {
        this.gramJobID = gramJobID;
    }

    public String getNodeID() {
        return NodeID;
    }

    public String getRsl() {
        return rsl;
    }

    public String getInvokedHost() {
        return invokedHost;
    }

    public String getGramJobID() {
        return gramJobID;
    }

	public String getWorkflowInstanceId() {
		return workflowInstanceId;
	}

	public void setWorkflowInstanceId(String workflowInstanceId) {
		this.workflowInstanceId = workflowInstanceId;
	}
}
