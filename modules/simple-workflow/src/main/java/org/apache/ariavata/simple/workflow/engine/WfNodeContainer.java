package org.apache.ariavata.simple.workflow.engine;

import org.apache.airavata.model.workspace.experiment.WorkflowNodeDetails;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowNode;

/**
 * Created by shameera on 2/9/15.
 */
public class WfNodeContainer {
    private WorkflowNode workflowNode;
    private WorkflowNodeDetails wfNodeDetails;

    public WfNodeContainer(WorkflowNode workflowNode, WorkflowNodeDetails wfNodeDetails) {
        this.workflowNode = workflowNode;
        this.wfNodeDetails = wfNodeDetails;
    }

    public WorkflowNode getWorkflowNode() {
        return workflowNode;
    }

    public void setWorkflowNode(WorkflowNode workflowNode) {
        this.workflowNode = workflowNode;
    }

    public WorkflowNodeDetails getWfNodeDetails() {
        return wfNodeDetails;
    }

    public void setWfNodeDetails(WorkflowNodeDetails wfNodeDetails) {
        this.wfNodeDetails = wfNodeDetails;
    }
}
