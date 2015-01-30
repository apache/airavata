package org.apache.ariavata.simple.workflow.engine.dag.links;

import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowNode;

/**
 * Created by shameera on 1/29/15.
 */
public interface Link {

    public WorkflowNode fromNode();

    public WorkflowNode toNode();

}
