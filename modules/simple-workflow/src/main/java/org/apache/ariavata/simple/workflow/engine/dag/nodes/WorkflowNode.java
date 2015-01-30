package org.apache.ariavata.simple.workflow.engine.dag.nodes;

/**
 * Created by shameera on 1/29/15.
 */
public interface WorkflowNode {

    public String getNodeId();

    public NodeType getNodeType();

    public NodeState getNodeState();

    public void setNodeState(NodeState newNodeState);

}
