package org.apache.airavata.workflow.core.parser;

import com.google.gson.JsonObject;
import org.apache.airavata.workflow.core.WorkflowParser;
import org.apache.airavata.workflow.core.dag.edge.Edge;
import org.apache.airavata.workflow.core.dag.nodes.ApplicationNode;
import org.apache.airavata.workflow.core.dag.nodes.InputNode;
import org.apache.airavata.workflow.core.dag.nodes.OutputNode;
import org.apache.airavata.workflow.core.dag.port.Port;

import java.util.List;

/**
 * Created by syodage on 1/27/16.
 */
public class JsonWorkflowParser implements WorkflowParser{

    private final String workflow;
    private List<InputNode> inputs;
    private List<OutputNode> outputs;
    private List<ApplicationNode> applications;
    private List<Port> ports;
    private List<Edge> edges;

    public JsonWorkflowParser(String jsonWorkflowString) {
        workflow = jsonWorkflowString;
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

    private Port createPort(JsonObject jPort){
        return null;
    }


    private Edge createEdge(JsonObject jEdge) {
        return null;
    }
}
