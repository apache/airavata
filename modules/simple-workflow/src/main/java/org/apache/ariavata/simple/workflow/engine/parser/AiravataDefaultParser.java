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
import org.apache.ariavata.simple.workflow.engine.dag.port.InPort;
import org.apache.ariavata.simple.workflow.engine.dag.port.InputPortIml;
import org.apache.ariavata.simple.workflow.engine.dag.port.OutPort;
import org.apache.ariavata.simple.workflow.engine.dag.port.OutPortImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.apache.airavata.model.Workflow;

/**
 * Created by shameera on 2/11/15.
 */
public class AiravataDefaultParser implements WorkflowParser {

    private String experimentId;
    private String credentialToken ;
    private Workflow workflow;
    private Experiment experiment;
    private Map<String, ApplicationNode> wfNodes = new HashMap<String, ApplicationNode>();


    public AiravataDefaultParser(String experimentId, String credentialToken) {
        this.experimentId = experimentId;
        this.credentialToken = credentialToken;
    }

    @Override
    public List<WorkflowInputNode> parse() throws RegistryException, AppCatalogException,
            ComponentException, GraphException {
        return parseWorkflow(getWorkflowFromExperiment());
    }

    private List<WorkflowInputNode> parseWorkflow(Workflow workflow) {
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
            for (DataPort dataPort : gNode.getInputPorts()) {
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
                outPort.setOutputObject(getOutputDataObject(wfInputNode.getInputObject()));
            }
            wfInputNodes.add(wfInputNode);
        }

        // while port container empty iterate graph and build the workflow DAG.
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
        WorkflowNode wfNode = null;
        List<PortContainer> nextPortContainerList = new ArrayList<PortContainer>();
        for (PortContainer portContainer : portContainerList) {
            dataPort = portContainer.getDataPort();
            inPort = portContainer.getInPort();
            Node node = dataPort.getNode();
            inPort.setInputObject(getInputDataObject(dataPort));
            if (node instanceof WSNode) {
                WSNode wsNode = (WSNode) node;
                wfNode = wfNodes.get(wsNode.getID());
                if (wfNode == null) {
                    wfNode = new ApplicationNodeImpl(wsNode.getID(),
                            wsNode.getComponent().getApplication().getApplicationId());
                    nextPortContainerList.addAll(processOutPorts(wsNode, wfNode));
                }
            }else if (node instanceof OutputNode) {
                OutputNode oNode = (OutputNode) node;
                wfNode = new WorkflowInputNodeImpl(oNode.getID(), oNode.getName());
            }
            inPort.setNode(wfNode);
            buildModel(nextPortContainerList);
            // set the workflow node to inPort
            // if require check the types of inputs and output ports,
            // add outputPorts to the workflow node
            // add edges to each output port
            // add inport and indataport to the list
            // recursively call the function.
        }

    }

    private List<PortContainer> processOutPorts(Node node, WorkflowNode wfNode) {
        OutPort outPort ;
        Edge edge;
        InPort inPort;
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

    private WorkflowInputNode getWorkflowInputNode(Node inputNode) {
        // FIXME: create a new workflow input node implementation with input node data.
        return null;
    }

    private Workflow getWorkflowFromExperiment() throws RegistryException, AppCatalogException, GraphException, ComponentException {
        Registry registry = RegistryFactory.getDefaultRegistry();
        experiment = (Experiment)registry.get(RegistryModelType.EXPERIMENT, experimentId);
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
}
