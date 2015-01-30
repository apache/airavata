package org.apache.ariavata.simple.workflow.engine.dag.links;

import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowNode;

/**
 * Created by shameera on 1/29/15.
 */
public class DirectedLink implements Link{

    private WorkflowNode _fromNode;

    private WorkflowNode _toNode;

    public DirectedLink(WorkflowNode _fromNode, WorkflowNode _toNode) {
        this._fromNode = _fromNode;
        this._toNode = _toNode;
    }

    @Override
    public WorkflowNode fromNode() {
        return null;
    }

    @Override
    public WorkflowNode toNode() {
        return null;
    }

    public void set_fromNode(WorkflowNode _fromNode) {
        this._fromNode = _fromNode;
    }

    public void set_toNode(WorkflowNode _toNode) {
        this._toNode = _toNode;
    }
}
