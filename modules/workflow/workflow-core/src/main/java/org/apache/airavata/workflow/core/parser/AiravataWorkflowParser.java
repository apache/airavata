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
import org.apache.airavata.workflow.core.WorkflowParser;
import org.apache.airavata.workflow.core.dag.edge.DirectedEdge;
import org.apache.airavata.workflow.core.dag.edge.Edge;
import org.apache.airavata.workflow.core.dag.nodes.ApplicationNode;
import org.apache.airavata.workflow.core.dag.nodes.ApplicationNodeImpl;
import org.apache.airavata.workflow.core.dag.nodes.WorkflowInputNode;
import org.apache.airavata.workflow.core.dag.nodes.WorkflowInputNodeImpl;
import org.apache.airavata.workflow.core.dag.nodes.WorkflowNode;
import org.apache.airavata.workflow.core.dag.nodes.WorkflowOutputNode;
import org.apache.airavata.workflow.core.dag.nodes.WorkflowOutputNodeImpl;
import org.apache.airavata.workflow.core.dag.port.InPort;
import org.apache.airavata.workflow.core.dag.port.InputPortIml;
import org.apache.airavata.workflow.core.dag.port.OutPort;
import org.apache.airavata.workflow.core.dag.port.OutPortImpl;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.component.system.ConstantComponent;
import org.apache.airavata.workflow.model.component.system.InputComponent;
import org.apache.airavata.workflow.model.component.system.S3InputComponent;
import org.apache.airavata.workflow.model.graph.DataEdge;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.graph.system.OutputNode;
import org.apache.airavata.workflow.model.graph.system.SystemDataPort;
import org.apache.airavata.workflow.model.graph.ws.WSNode;
import org.apache.airavata.workflow.model.graph.ws.WSPort;
import org.apache.airavata.workflow.model.wf.Workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiravataWorkflowParser implements WorkflowParser {

    private String credentialToken ;

    private ExperimentModel experiment;
    private Map<String, WorkflowNode> wfNodes = new HashMap<String, WorkflowNode>();


    public AiravataWorkflowParser(String experimentId, String credentialToken) throws RegistryException {
        this.experiment = getExperiment(experimentId);
        this.credentialToken = credentialToken;
    }

    public AiravataWorkflowParser(ExperimentModel experiment, String credentialToken) {
        this.credentialToken = credentialToken;
        this.experiment = experiment;
    }

    @Override
    public List<WorkflowInputNode> parse() throws RegistryException, AppCatalogException,
            ComponentException, GraphException {
        return parseWorkflow(getWorkflowFromExperiment(experiment));
    }

    public List<WorkflowInputNode> parseWorkflow(Workflow workflow) {
        List<Node> gNodes = getInputNodes(workflow);
        List<WorkflowInputNode> wfInputNodes = new ArrayList<WorkflowInputNode>();
        List<PortContainer> portContainers = new ArrayList<PortContainer>();
        List<InputDataObjectType> experimentInputs = experiment.getExperimentInputs();
        Map<String,InputDataObjectType> inputDataMap=new HashMap<String, InputDataObjectType>();
        WorkflowInputNode wfInputNode = null;
        for (InputDataObjectType dataObjectType : experimentInputs) {
            inputDataMap.put(dataObjectType.getName(), dataObjectType);
        }
        for (Node gNode : gNodes) {
            wfInputNode = new WorkflowInputNodeImpl(gNode.getID(), gNode.getName());
            wfInputNode.setInputObject(inputDataMap.get(wfInputNode.getId()));
            if (wfInputNode.getInputObject() == null) {
                throw new RuntimeException("Workflow Input object is not set, workflow node id: " + wfInputNode.getId());
            }
            portContainers.addAll(processOutPorts(gNode, wfInputNode));
            wfInputNodes.add(wfInputNode);
        }

        // while port container is not empty iterate graph and build the workflow DAG.
        buildModel(portContainers);

        return wfInputNodes;
    }

    private void buildModel(List<PortContainer> portContainerList) {
        // end condition of recursive call.
        if (portContainerList == null || portContainerList.isEmpty()) {
            return ;
        }
        DataPort dataPort = null;
        InPort inPort = null;
        ApplicationNode wfApplicationNode = null;
        WorkflowOutputNode wfOutputNode = null;
        List<PortContainer> nextPortContainerList = new ArrayList<PortContainer>();
        for (PortContainer portContainer : portContainerList) {
            dataPort = portContainer.getDataPort();
            inPort = portContainer.getInPort();
            Node node = dataPort.getNode();
            if (node instanceof WSNode) {
                WSNode wsNode = (WSNode) node;
                WorkflowNode wfNode = wfNodes.get(wsNode.getID());
                if (wfNode == null) {
                    wfApplicationNode = createApplicationNode(wsNode);
                    wfNodes.put(wfApplicationNode.getId(), wfApplicationNode);
                    nextPortContainerList.addAll(processOutPorts(wsNode, wfApplicationNode));
                } else if (wfNode instanceof ApplicationNode) {
                    wfApplicationNode = (ApplicationNode) wfNode;
                } else {
                    throw new IllegalArgumentException("Only support for ApplicationNode implementation, but found other type for node implementation");
                }
                inPort.setNode(wfApplicationNode);
                wfApplicationNode.addInPort(inPort);

            }else if (node instanceof OutputNode) {
                OutputNode oNode = (OutputNode) node;
                wfOutputNode = createWorkflowOutputNode(oNode);
                wfOutputNode.setInPort(inPort);
                inPort.setNode(wfOutputNode);
                wfNodes.put(wfOutputNode.getId(), wfOutputNode);
            }
        }
        buildModel(nextPortContainerList);

    }

    private WorkflowOutputNode createWorkflowOutputNode(OutputNode oNode) {
        WorkflowOutputNodeImpl workflowOutputNode = new WorkflowOutputNodeImpl(oNode.getID(), oNode.getName());
        OutputDataObjectType outputDataObjectType = new OutputDataObjectType();
        outputDataObjectType.setType(oNode.getParameterType());
        outputDataObjectType.setName(oNode.getID());
        workflowOutputNode.setOutputObject(outputDataObjectType);
        return workflowOutputNode;
    }

    private ApplicationNode createApplicationNode(WSNode wsNode) {
        ApplicationNode applicationNode = new ApplicationNodeImpl(wsNode.getID(),
                wsNode.getComponent().getApplication().getName(),
                wsNode.getComponent().getApplication().getApplicationId());
        return applicationNode;
    }

    private List<PortContainer> processOutPorts(Node node, WorkflowNode wfNode) {
        OutPort outPort ;
        Edge edge;
        InPort inPort = null;
        List<PortContainer> portContainers = new ArrayList<PortContainer>();
        for (DataPort dataPort : node.getOutputPorts()) {
            outPort = createOutPort(dataPort);
            for (DataEdge dataEdge : dataPort.getEdges()) {
                edge = new DirectedEdge();
                edge.setFromPort(outPort);
                outPort.addEdge(edge);
                inPort = createInPort(dataEdge.getToPort());
                edge.setToPort(inPort);
                inPort.addEdge(edge);
                portContainers.add(new PortContainer(dataEdge.getToPort(), inPort));
            }
            outPort.setNode(wfNode);
            if (wfNode instanceof WorkflowInputNode) {
                WorkflowInputNode workflowInputNode = (WorkflowInputNode) wfNode;
                workflowInputNode.setOutPort(outPort);
            } else if (wfNode instanceof ApplicationNode) {
                ApplicationNode applicationNode = ((ApplicationNode) wfNode);
                applicationNode.addOutPort(outPort);
            }
        }
        return portContainers;
    }

    private OutPort createOutPort(DataPort dataPort) {
        OutPortImpl outPort = new OutPortImpl(dataPort.getID());
        OutputDataObjectType outputDataObjectType = new OutputDataObjectType();
        if (dataPort instanceof WSPort) {
            WSPort wsPort = (WSPort) dataPort;
            outputDataObjectType.setName(wsPort.getComponentPort().getName());
            outputDataObjectType.setType(wsPort.getType());
        }else if (dataPort instanceof SystemDataPort) {
            SystemDataPort sysPort = (SystemDataPort) dataPort;
            outputDataObjectType.setName(sysPort.getFromNode().getName());
            outputDataObjectType.setType(sysPort.getType());
        }

        outPort.setOutputObject(outputDataObjectType);
        return outPort;
    }

    private InPort createInPort(DataPort toPort) {
        InPort inPort = new InputPortIml(toPort.getID());
        InputDataObjectType inputDataObjectType = new InputDataObjectType();
        if (toPort instanceof WSPort) {
            WSPort wsPort = (WSPort) toPort;
            inputDataObjectType.setName(wsPort.getName());
            inputDataObjectType.setType(wsPort.getType());
            inputDataObjectType.setApplicationArgument(wsPort.getComponentPort().getApplicationArgument());
            inputDataObjectType.setIsRequired(!wsPort.getComponentPort().isOptional());
            inputDataObjectType.setInputOrder(wsPort.getComponentPort().getInputOrder());

            inPort.setDefaultValue(wsPort.getComponentPort().getDefaultValue());
        }else if (toPort instanceof SystemDataPort) {
            SystemDataPort sysPort = (SystemDataPort) toPort;
            inputDataObjectType.setName(sysPort.getName());
            inputDataObjectType.setType(sysPort.getType());
        }
        inPort.setInputObject(inputDataObjectType);
        return inPort;
    }

    private InputDataObjectType getInputDataObject(DataPort dataPort) {
        InputDataObjectType inputDataObject = new InputDataObjectType();
        inputDataObject.setName(dataPort.getName());
        if (dataPort instanceof WSPort) {
            WSPort port = (WSPort) dataPort;
            inputDataObject.setInputOrder(port.getComponentPort().getInputOrder());
            inputDataObject.setApplicationArgument(port.getComponentPort().getApplicationArgument() == null ?
                    "" : port.getComponentPort().getApplicationArgument());
            inputDataObject.setType(dataPort.getType());
        }
        return inputDataObject;
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

    private Workflow getWorkflowFromExperiment(ExperimentModel experiment) throws RegistryException, AppCatalogException, GraphException, ComponentException {
        WorkflowCatalog workflowCatalog = getWorkflowCatalog();
        return new Workflow(workflowCatalog.getWorkflow(experiment.getExecutionId()).getGraph());
    }

    private WorkflowCatalog getWorkflowCatalog() throws AppCatalogException {
        return RegistryFactory.getAppCatalog().getWorkflowCatalog();
    }

    private ArrayList<Node> getInputNodes(Workflow wf) {
        ArrayList<Node> list = new ArrayList<Node>();
        List<NodeImpl> nodes = wf.getGraph().getNodes();
        for (Node node : nodes) {
            String name = node.getComponent().getName();
            if (InputComponent.NAME.equals(name) || ConstantComponent.NAME.equals(name) || S3InputComponent.NAME.equals(name)) {
                list.add(node);
            }
        }
        return list;
    }

    public Map<String, WorkflowNode> getWfNodes() {
        return wfNodes;
    }
}
