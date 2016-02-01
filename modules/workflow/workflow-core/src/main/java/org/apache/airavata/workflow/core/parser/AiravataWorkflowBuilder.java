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

import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.*;
import org.apache.airavata.workflow.core.WorkflowBuilder;
import org.apache.airavata.workflow.core.WorkflowParser;
import org.apache.airavata.workflow.core.dag.edge.DirectedEdge;
import org.apache.airavata.workflow.core.dag.edge.Edge;
import org.apache.airavata.workflow.core.dag.nodes.ApplicationNode;
import org.apache.airavata.workflow.core.dag.nodes.ApplicationNodeImpl;
import org.apache.airavata.workflow.core.dag.nodes.InputNode;
import org.apache.airavata.workflow.core.dag.nodes.WorkflowNode;
import org.apache.airavata.workflow.core.dag.nodes.OutputNode;
import org.apache.airavata.workflow.core.dag.nodes.OutputNodeImpl;
import org.apache.airavata.workflow.core.dag.port.*;

import java.util.*;

public class AiravataWorkflowBuilder implements WorkflowBuilder {

    private String credentialToken ;
    private WorkflowParser workflowParser;
    private ExperimentModel experiment;


    public AiravataWorkflowBuilder(String experimentId, String credentialToken, WorkflowParser workflowParser) throws RegistryException {
        this.experiment = getExperiment(experimentId);
        this.credentialToken = credentialToken;
        this.workflowParser = workflowParser;
    }

    public AiravataWorkflowBuilder(ExperimentModel experiment, String credentialToken , WorkflowParser workflowParser) {
        this.credentialToken = credentialToken;
        this.experiment = experiment;
        this.workflowParser = workflowParser;
    }

    @Override
    public List<InputNode> build() throws Exception {
        return parseWorkflow(getWorkflowFromExperiment(experiment));
    }

    @Override
    public List<InputNode> build(String workflow) throws Exception {
        return parseWorkflow(workflow);
    }

    public List<InputNode> parseWorkflow(String workflow) throws Exception {

        List<InputNode> inputNodes = workflowParser.getInputNodes();
        List<ApplicationNode> applicationNodes = workflowParser.getApplicationNodes();
        List<Port> ports = workflowParser.getPorts();
        List<Edge> edges = workflowParser.getEdges();
        List<OutputNode> outputNodes = workflowParser.getOutputNodes();

        // travel breath first and build relation between each workflow component
        Queue<WorkflowNode> queue = new LinkedList<>();
        List<InputDataObjectType> experimentInputs = experiment.getExperimentInputs();
        Map<String,InputDataObjectType> inputDataMap=new HashMap<String, InputDataObjectType>();
        for (InputDataObjectType dataObjectType : experimentInputs) {
            inputDataMap.put(dataObjectType.getName(), dataObjectType);
        }

        return inputNodes;
    }


    private OutputDataObjectType getOutputDataObject(InputDataObjectType inputObject) {
        OutputDataObjectType outputDataObjectType = new OutputDataObjectType();
        outputDataObjectType.setApplicationArgument(inputObject.getApplicationArgument());
        outputDataObjectType.setName(inputObject.getName());
        outputDataObjectType.setType(inputObject.getType());
        outputDataObjectType.setValue(inputObject.getValue());
        return outputDataObjectType;
    }

    private ExperimentModel getExperiment(String experimentId) throws RegistryException {
        Registry registry = RegistryFactory.getRegistry();
        return (ExperimentModel)registry.getExperimentCatalog().get(ExperimentCatalogModelType.EXPERIMENT, experimentId);
    }

    private String getWorkflowFromExperiment(ExperimentModel experiment) throws RegistryException, AppCatalogException {
        WorkflowCatalog workflowCatalog = getWorkflowCatalog();

        // FIXME: return workflow string
        return null;
    }

    private WorkflowCatalog getWorkflowCatalog() throws AppCatalogException {
        return RegistryFactory.getAppCatalog().getWorkflowCatalog();
    }

}
