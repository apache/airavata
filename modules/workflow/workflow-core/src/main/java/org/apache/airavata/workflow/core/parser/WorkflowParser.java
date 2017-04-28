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
package org.apache.airavata.workflow.core.parser;

import org.apache.airavata.workflow.core.WorkflowInfo;
import org.apache.airavata.workflow.core.dag.edge.Edge;
import org.apache.airavata.workflow.core.dag.nodes.ApplicationNode;
import org.apache.airavata.workflow.core.dag.nodes.InputNode;
import org.apache.airavata.workflow.core.dag.nodes.OutputNode;
import org.apache.airavata.workflow.core.dag.port.Port;

import java.util.List;

public interface WorkflowParser {

    public WorkflowInfo parse() throws Exception;

    public List<InputNode> getInputNodes() throws Exception;

    public List<OutputNode> getOutputNodes() throws Exception;

    public List<ApplicationNode> getApplicationNodes() throws Exception;

    public List<Port> getPorts() throws Exception;

    public List<Edge> getEdges() throws Exception;

}
