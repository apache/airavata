package org.apache.ariavata.simple.workflow.engine.dag.nodes;

/**
 * Created by shameera on 1/29/15.
 */
public class WorkflowInputNode implements WorkflowNode {

    private NodeState myState = NodeState.READY;
    private final String nodeId;

    public WorkflowInputNode(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String getNodeId() {
        return null;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.WORKFLOW_INPUT;
    }

    @Override
    public NodeState getNodeState() {
        return myState;
    }

    @Override
    public void setNodeState(NodeState newNodeState) {
        // TODO: node state can't be reversed , correct order WAITING --> READY --> EXECUTING --> EXECUTED --> COMPLETE
        myState = newNodeState;
    }
}
