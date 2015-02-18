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

package org.apache.ariavata.simple.workflow.engine.parser;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.airavata.appcatalog.cpi.WorkflowCatalog;
import org.apache.aiaravata.application.catalog.data.impl.AppCatalogFactory;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.RegistryModelType;
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
import org.apache.airavata.workflow.model.graph.ws.WSNode;
import org.apache.airavata.workflow.model.graph.ws.WSPort;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.ariavata.simple.workflow.engine.WorkflowParser;
import org.apache.ariavata.simple.workflow.engine.dag.edge.DirectedEdge;
import org.apache.ariavata.simple.workflow.engine.dag.edge.Edge;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.ApplicationNode;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.ApplicationNodeImpl;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowInputNode;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowInputNodeImpl;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowNode;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowOutputNode;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowOutputNodeImpl;
import org.apache.ariavata.simple.workflow.engine.dag.port.InPort;
import org.apache.ariavata.simple.workflow.engine.dag.port.InputPortIml;
import org.apache.ariavata.simple.workflow.engine.dag.port.OutPort;
import org.apache.ariavata.simple.workflow.engine.dag.port.OutPortImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiravataDefaultParser implements WorkflowParser {

    private String credentialToken ;
    private Workflow workflow;


    private Experiment experiment;
    private Map<String, WorkflowNode> wfNodes = new HashMap<String, WorkflowNode>();


    public AiravataDefaultParser(String experimentId, String credentialToken) throws RegistryException {
        this.experiment = getExperiment(experimentId);
        this.credentialToken = credentialToken;
    }

    public AiravataDefaultParser(Experiment experiment, String credentialToken) {
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
        OutPort outPort = null;
        InPort inPort = null;
        Edge edge = null;
        for (Node gNode : gNodes) {
            wfInputNode = new WorkflowInputNodeImpl(gNode.getID(), gNode.getName());
            wfInputNode.setInputObject(inputDataMap.get(wfInputNode.getNodeName()));
            if (wfInputNode.getInputObject() == null) {
                // TODO: throw an error and exit.
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
        if (portContainerList == null || portContainerList.size() == 0) {
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
            inPort.setInputObject(getInputDataObject(dataPort));
            if (node instanceof WSNode) {
                WSNode wsNode = (WSNode) node;
                WorkflowNode wfNode = wfNodes.get(wsNode.getID());
                if (wfNode == null) {
                    wfApplicationNode = new ApplicationNodeImpl(wsNode.getID(),
                            wsNode.getComponent().getApplication().getApplicationId());
                    wfNodes.put(wfApplicationNode.getNodeId(), wfApplicationNode);
                    nextPortContainerList.addAll(processOutPorts(wsNode, wfApplicationNode));
                } else if (wfNode instanceof ApplicationNode) {
                    wfApplicationNode = (ApplicationNode) wfNode;
                } else {
                    // TODO : handle this scenario
                }
                inPort.setNode(wfApplicationNode);
                wfApplicationNode.addInPort(inPort);

            }else if (node instanceof OutputNode) {
                OutputNode oNode = (OutputNode) node;
                wfOutputNode = new WorkflowOutputNodeImpl(oNode.getID(), oNode.getName());
                wfOutputNode.setInPort(inPort);
                wfNodes.put(wfOutputNode.getNodeId(), wfOutputNode);
            }
        }
        buildModel(nextPortContainerList);

    }

    private List<PortContainer> processOutPorts(Node node, WorkflowNode wfNode) {
        OutPort outPort ;
        Edge edge;
        InPort inPort = null;
        List<PortContainer> portContainers = new ArrayList<PortContainer>();
        for (DataPort dataPort : node.getOutputPorts()) {
            outPort = new OutPortImpl(dataPort.getID());
            for (DataEdge dataEdge : dataPort.getEdges()) {
                edge = new DirectedEdge();
                edge.setFromPort(outPort);
                outPort.addEdge(edge);
                inPort = getInPort(dataEdge.getToPort());
                edge.setToPort(inPort);
                inPort.addEdge(edge);
                portContainers.add(new PortContainer(dataEdge.getToPort(), inPort));
            }
            outPort.setNode(wfNode);
            if (wfNode instanceof WorkflowInputNode) {
                WorkflowInputNode workflowInputNode = (WorkflowInputNode) wfNode;
                workflowInputNode.setOutPort(outPort);
            }else if (wfNode instanceof ApplicationNode) {
                ApplicationNode applicationNode = ((ApplicationNode) wfNode);
                applicationNode.addOutPort(outPort);
//                applicationNode.addInPort(inPort);
            }
        }
        return portContainers;
    }

    private InPort getInPort(DataPort toPort) {
        return new InputPortIml(toPort.getID());
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

    private Experiment getExperiment(String experimentId) throws RegistryException {
        Registry registry = RegistryFactory.getDefaultRegistry();
        return (Experiment)registry.get(RegistryModelType.EXPERIMENT, experimentId);
    }

    private Workflow getWorkflowFromExperiment(Experiment experiment) throws RegistryException, AppCatalogException, GraphException, ComponentException {
        WorkflowCatalog workflowCatalog = getWorkflowCatalog();
        return new Workflow(workflowCatalog.getWorkflow(experiment.getApplicationId()).getGraph());
    }

    private WorkflowCatalog getWorkflowCatalog() throws AppCatalogException {
        return AppCatalogFactory.getAppCatalog().getWorkflowCatalog();
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
