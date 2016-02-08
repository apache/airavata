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

package org.apache.airavata.workflow.core.parser;

import com.google.gson.JsonObject;
import org.apache.airavata.workflow.core.dag.edge.Edge;
import org.apache.airavata.workflow.core.dag.nodes.ApplicationNode;
import org.apache.airavata.workflow.core.dag.nodes.InputNode;
import org.apache.airavata.workflow.core.dag.nodes.OutputNode;
import org.apache.airavata.workflow.core.dag.port.Port;

import java.util.ArrayList;
import java.util.List;

public class JsonWorkflowParser implements WorkflowParser {

    private final String workflow;
    private List<InputNode> inputs;
    private List<OutputNode> outputs;
    private List<ApplicationNode> applications;
    private List<Port> ports;
    private List<Edge> edges;

    public JsonWorkflowParser(String jsonWorkflowString) {
        workflow = jsonWorkflowString;

        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        applications = new ArrayList<>();
        ports = new ArrayList<>();
        edges = new ArrayList<>();
    }


    @Override
    public void parse() throws Exception {
        // TODO parse json string and construct components
    }

    @Override
    public List<InputNode> getInputNodes() throws Exception {
        return null;
    }

    @Override
    public List<OutputNode> getOutputNodes() throws Exception {
        return null;
    }

    @Override
    public List<ApplicationNode> getApplicationNodes() throws Exception {
        return null;
    }

    @Override
    public List<Port> getPorts() throws Exception {
        return null;
    }

    @Override
    public List<Edge> getEdges() throws Exception {
        return null;
    }


    private InputNode createInputNode(JsonObject jNode) {
        return null;
    }

    private OutputNode createOutputNode(JsonObject jNode) {
        return null;
    }

    private ApplicationNode createApplicationNode(JsonObject jNode) {
        return null;
    }

    private Port createPort(JsonObject jPort) {
        return null;
    }


    private Edge createEdge(JsonObject jEdge) {
        return null;
    }
}
