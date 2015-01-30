package org.apache.ariavata.simple.workflow.engine.dag.nodes;

import org.apache.ariavata.simple.workflow.engine.dag.links.Link;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shameera on 1/29/15.
 */
public class ApplicationNode implements WorkflowNode {

    private final String nodeId;
    private NodeState myState = NodeState.WAITING;
    private List<Link> inputLinks = new ArrayList<Link>();
    private List<Link> outputLinks = new ArrayList<Link>();

    public ApplicationNode(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String getNodeId() {
        return this.nodeId;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.APPLICATION;
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

    public List<Link> getInputLinks() {
        return inputLinks;
    }

    public List<Link> getOutputLinks() {
        return outputLinks;
    }

    public void setInputLinks(List<Link> inputLinks) {
        this.inputLinks = inputLinks;
    }

    public void setOutputLinks(List<Link> outputLinks) {
        this.outputLinks = outputLinks;
    }

    public void addInputLink(Link inputLink) {
        inputLinks.add(inputLink);
    }

    public void addOutputLink(Link outputLink) {
        outputLinks.add(outputLink);
    }
}
