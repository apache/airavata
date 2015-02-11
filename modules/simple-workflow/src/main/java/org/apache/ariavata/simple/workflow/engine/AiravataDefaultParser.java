package org.apache.ariavata.simple.workflow.engine;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.airavata.appcatalog.cpi.WorkflowCatalog;
import org.apache.aiaravata.application.catalog.data.impl.AppCatalogFactory;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.component.system.ConstantComponent;
import org.apache.airavata.workflow.model.component.system.InputComponent;
import org.apache.airavata.workflow.model.component.system.S3InputComponent;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.ariavata.simple.workflow.engine.dag.edge.Edge;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowInputNode;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowNode;
import org.apache.ariavata.simple.workflow.engine.dag.port.OutPort;

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
        List<WorkflowNode> wfNodes = new ArrayList<WorkflowNode>();
        List<InputDataObjectType> experimentInputs = experiment.getExperimentInputs();
        Map<String,InputDataObjectType> inputDataMap=new HashMap<String, InputDataObjectType>();
        WorkflowInputNode wfInputNode = null;
        for (InputDataObjectType dataObjectType : experimentInputs) {
            inputDataMap.put(dataObjectType.getName(), dataObjectType);
        }
        for (Node gNode : gNodes) {
            // create a new wfInputNode instance by passing node name and node Id
            wfInputNode.setInputObject(inputDataMap.get(wfInputNode.getNodeName()));
            if (wfInputNode.getInputObject() == null) {
                // TODO: throw an error and exit.
            }
            Edge edge = null;//= new Edge
            OutPort outPort = null;
            outPort.
            link.setOutPort(null); // new Output
            wfInputNodes.add(wfInputNode);
        }





        return null;
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
