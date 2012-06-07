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

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.common.utils.Pair;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.registry.api.WorkflowExecution;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
import org.apache.airavata.registry.api.workflow.WorkflowServiceIOData;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.graph.DataPort;
import org.apache.airavata.xbaya.graph.ForEachExecutableNode;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.system.EndForEachNode;
import org.apache.airavata.xbaya.graph.system.ForEachNode;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.ws.WSNode;
import org.apache.airavata.xbaya.invoker.Invoker;
import org.apache.airavata.xbaya.util.XBayaUtil;
import org.xmlpull.infoset.XmlElement;
import xsul5.XmlConstants;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ProvenanceReader {

    public String DEFAULT_LIBRARY_FOLDER_NAME = "provenance";

    private String experimentId;

    private AiravataRegistry registry;

    private Node node;

    public ProvenanceReader(Node node,String experimentId,AiravataRegistry registry) {
       this.experimentId = experimentId;
        this.registry = registry;
        this.node = node;
	}

    public Object read() throws Exception {
        try {
            WorkflowExecution workflowExecution = registry.getWorkflowExecution(experimentId);
            List<WorkflowServiceIOData> serviceOutput = workflowExecution.getServiceOutput();
            if (serviceOutput.size() == 0) {
                return null;
            }
            for (WorkflowServiceIOData data : serviceOutput) {
                if (this.node.getID().equals(data.getNodeId())) {
                    return data.getValue();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
