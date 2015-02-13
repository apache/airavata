package org.apache.ariavata.simple.workflow.engine.dag.port;

import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.ariavata.simple.workflow.engine.dag.edge.Edge;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowNode;

import java.util.List;

/**
 * Created by shameera on 2/11/15.
 */
public class OutPortImpl implements OutPort {

    private OutputDataObjectType outputDataObjectType;
    private List<Edge> outEdges;
    private boolean isSatisfy = false;
    private String portId;

    public OutPortImpl(String portId) {
        this.portId = portId;
    }

    @Override
    public void setOutputObject(OutputDataObjectType outputObject) {
        // TODO: Auto generated method body.
    }

    @Override
    public OutputDataObjectType getOutputObject() {
        return null; // TODO: Auto generated method body.
    }

    @Override
    public List<Edge> getOutEdges() {
        return null; // TODO: Auto generated method body.
    }

    @Override
    public void addEdge(Edge edge) {
        // TODO: Auto generated method body.
    }

    @Override
    public boolean isSatisfy() {
        return false; // TODO: Auto generated method body.
    }

    @Override
    public WorkflowNode getNode() {
        return null; // TODO: Auto generated method body.
    }

    @Override
    public void setNode(WorkflowNode workflowNode) {
        // TODO: Auto generated method body.
    }

    @Override
    public String getId() {
        return null; // TODO: Auto generated method body.
    }
}
