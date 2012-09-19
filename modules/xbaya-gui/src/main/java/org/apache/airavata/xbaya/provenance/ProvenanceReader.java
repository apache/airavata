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
package org.apache.airavata.xbaya.provenance;

import java.util.List;

import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.workflow.ExperimentData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNodeData;
import org.apache.airavata.registry.api.workflow.WorkflowNodeIOData;
import org.apache.airavata.workflow.model.graph.Node;

public class ProvenanceReader {

    public String DEFAULT_LIBRARY_FOLDER_NAME = "provenance";

    private String experimentId;

    private AiravataRegistry2 registry;

    private Node node;

    public ProvenanceReader(Node node,String experimentId,AiravataRegistry2 registry) {
       this.experimentId = experimentId;
        this.registry = registry;
        this.node = node;
	}

    public Object read() throws Exception {
        try {
            ExperimentData workflowExecution = registry.getExperiment(experimentId);
            List<WorkflowInstanceNodeData> nodeDataList = workflowExecution.getWorkflowInstanceData().get(0).getNodeDataList();
            if (nodeDataList.size() == 0) {
                return null;
            }
            for (WorkflowInstanceNodeData data : nodeDataList) {
                if (this.node.getID().equals(data.getWorkflowInstanceNode().getNodeId())) {
                    return data.getOutputData().get(0).getValue();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
