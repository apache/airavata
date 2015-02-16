package org.apache.ariavata.simple.workflow.engine.dag.port;

import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.ariavata.simple.workflow.engine.dag.edge.Edge;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shameera on 2/11/15.
 */
public class OutPortImpl implements OutPort {

    private OutputDataObjectType outputDataObjectType;
    private List<Edge> outEdges = new ArrayList<Edge>();
    private boolean isSatisfy = false;
    private String portId;
    private WorkflowNode node;

    public OutPortImpl(String portId) {
        this.portId = portId;
    }

    @Override
    public void setOutputObject(OutputDataObjectType outputObject) {
        this.outputDataObjectType = outputObject;
    }

    @Override
    public OutputDataObjectType getOutputObject() {
        return this.outputDataObjectType;
    }

    @Override
    public List<Edge> getOutEdges() {
        return this.outEdges;
    }

    @Override
    public void addEdge(Edge edge) {
        this.outEdges.add(edge);
    }

    @Override
    public boolean isSatisfy() {
        return this.outputDataObjectType.getValue() != null
                && !this.outputDataObjectType.getValue().equals("");
    }

    @Override
    public WorkflowNode getNode() {
        return this.node;
    }

    @Override
    public void setNode(WorkflowNode workflowNode) {
        this.node = workflowNode;
    }

    @Override
    public String getId() {
        return portId;
    }
}
